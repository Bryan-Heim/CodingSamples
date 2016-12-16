/*
	This is the main testing program for the new system calls (located at bottom of sys.c)
	It makes use of the down and up system calls and will	use fork() to create more 
	producer and consumer processes that will all make use of a shared memory buffer.
	By Bryan Heim
*/


// include our syscalls
#include <linux/unistd.h>
// include functs for atoi, printf and mmap
#include <stdio.h>
#include <stdlib.h>
#include <sys/mman.h>
// include functs for waiting
#include <sys/types.h>
#include <sys/wait.h>


// re-defined here as well to work with added syscalls
struct list_node{
	struct task_struct *process;
	struct list_node *next_node;
};
struct my_custom_sem{
	int value;
	struct list_node *root;
};


// define the methods below to remove warning
void down(struct my_custom_sem *);
void up(struct my_custom_sem *);
void producer(struct my_custom_sem *, struct my_custom_sem *, struct my_custom_sem *, int, int *, int);
void consumer(struct my_custom_sem *, struct my_custom_sem *, struct my_custom_sem *, int, int *, int);


// main is responsible for getting paramters from terminal
// then forking off producers and consumers
int main(int argc, char *argv[]){
	
	// check given parameters, make sure their right
	if(argc == 4)
	{
		// used atoi to get integer value
		int num_of_prods = atoi(argv[1]);
		int num_of_cons = atoi(argv[2]);
		int shared_buffer = atoi(argv[3]);
		int shared_buffer_size = sizeof(int)*shared_buffer;
		// struct size times three for lock, empty, full
		int struct_size = (sizeof(struct my_custom_sem)*3);

		
		// processed command line inputs
		// map memory for structs lock, empty, full and shared buffer
		void *structs;
		void *prod_con_buffer;
		structs = mmap(NULL,struct_size,PROT_READ|PROT_WRITE, MAP_SHARED|MAP_ANONYMOUS, 0, 0);
		prod_con_buffer = mmap(NULL,shared_buffer_size,PROT_READ|PROT_WRITE,MAP_SHARED|MAP_ANONYMOUS, 0, 0);

		
		// now create and fill structs
		struct my_custom_sem *empty = structs;
		struct my_custom_sem *full = structs+1;
		struct my_custom_sem *lock = structs+2;
		empty->value = shared_buffer;
		full->value = 0;
		lock->value = 1;
		empty->root = NULL;
		full->root = NULL;
		lock->root = NULL;

		// also setup shared buffer
		int n = shared_buffer;
		int *producer_pointer = prod_con_buffer;
		int *consumer_pointer = prod_con_buffer+1;
		int *buffer_pointer = prod_con_buffer+2;
		*producer_pointer = 0;
		*consumer_pointer = 0;
		// because were starting at the number 0

		// everything is now ready
		// make producers and consumers and let them go!
		printf("made it here!\n");
		int i, j;
		for(i = 0; i < num_of_prods; i++)
		{
			if(fork() == 0)
				producer(empty,full,lock,n,buffer_pointer,i);
		}
		for(j = 0; j < num_of_cons; j++)
		{
			if(fork() == 0)
				consumer(empty,full,lock,n,buffer_pointer,j);
		}
	}
	// invalid arguments, display error
	else
	{
		printf("Sorry, invalid arguments given.\n");
		return 0;
	}
	int status;
	wait(&status);
	return 0;
}


// both the producer and consumer functions are infinite loops
// and should run forever without deadlocking
void producer(struct my_custom_sem *empty, struct my_custom_sem *full, struct my_custom_sem *lock, int n, int *buffer_pointer, int prod_num){
	
	int produced_number;
	int *producer_pointer;
	producer_pointer = buffer_pointer - 2;
	
	while(1)
	{
		down(empty);
		down(lock);
		
		produced_number = *producer_pointer;
		buffer_pointer[produced_number] = produced_number;
		*producer_pointer = (produced_number+1)%n;
		// have the number to print and the buffer is updated
		printf("Producer '%d' Produced: %d\n",prod_num,produced_number);
		
		up(lock);
		up(full);
	}
	return;
}

void consumer(struct my_custom_sem *empty, struct my_custom_sem *full, struct my_custom_sem *lock, int n, int *buffer_pointer, int cons_num){
	
	int consumed_number, consumed_index;
	int *consumer_pointer;
	consumer_pointer = buffer_pointer - 1;
	
	while(1)
	{
		down(full);
		down(lock);
		
		consumed_index = *consumer_pointer;
		consumed_number = buffer_pointer[consumed_index];
		*consumer_pointer = (consumed_index+1)%n;
		// consumer now must print, pointer is updated
		printf("Consumer '%d' Consumed: %d\n",cons_num,consumed_number);
		
		up(lock);
		up(empty);
	}
	return;
}


// these up and down functions are to have a more C style syntax
void down(struct my_custom_sem *sem){
	syscall(__NR_my_custom_down, sem);
}
void up(struct my_custom_sem *sem){
	syscall(__NR_my_custom_up, sem);
}
