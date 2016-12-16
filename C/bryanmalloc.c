/*
	An implementation of C's malloc and free
	By Bryan Heim
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

struct Node {
	int size;
	char free;
	struct Node *next;
	struct Node *prev;
};

struct Node *head;
struct Node *end;
struct Node *current;
struct Node *next_current;
struct Node *prev_current
int dif_holder[2];
int temp_difference;
int current_difference;

current_difference = 0;
temp_difference = 0;
dif_holder[0] = -1;	// used for testing to find closest fit
dif_holder[1] = NULL;
head = NULL;
current = NULL; 

void *my_bestfit_malloc(int in_size)
{
	// first time calling my malloc
	if (head == NULL)
	{
		head = sbrk(sizeof(struct Node)+in_size);
		current = head;
		end = head;
		current->size = in_size;
		current->free = 'n';
		current->next = NULL;
		current->prev = NULL;
		return (current+sizeof(Struct Node));

	}
	else // there is at least one node already made
	{
		current = head;
		 // loop will take perfect fit or finish with dif_holder set to size difference + address to start
		while(current != NULL)
		{
			if(current->size = in_size && current->free == 'y')
			{
				// found a perfect fit, take it
				// set current->free = 'n'
				// return current+sizeof(struct Node) so user can have that space
				current->free = 'n';
				return (current+sizeof(struct Node));
			}
			if(current->size > in_size && current->free == 'y')
			{
				temp_difference = ((current->size)-in_size);
				if(current_difference == 0)
				{
					current_difference = temp_difference;
					dif_holder[0] = temp_difference;
					dif_holder[1] = current;
				}
				if(temp_difference < current_difference)
				{
					dif_holder[0] = temp_difference;
					dif_holder[1] = current;
				}
			}
			prev_current = current; // used for after loop
			current = current->next;
		}
		// if gets here made it out of loop without perfect fit, so take the address of best fit, and give them that make new free space.
		if (dif_holder[0] == -1)
		{
			// current will = null if nothing was set and didnt find space
			// put new space onto the end
			current = sbrk(sizeof(struct Node)+in_size);
			prev_current->next = current;
			end = current;
			current->size = in_size;
			current->free = 'n';
			current->next = NULL;
			current->prev = prev_current;
			return (current+sizeof(struct Node));
		}
		else
		{
			// dif_holder was set, meaning found free space
			// it wasnt added to the end or didnt find a perfect fit
			// go to where the shortest address was and take that space
			current = dif_holder[1]; // base address of who to take.
			current->free = 'n';
			dif_holder[0] = -1;
			dif_holder[1] = NULL;
			return (current+sizeof(Struct Node));
		}
		
	}
}

void my_free(void *ptr)
{
	current = ((ptr)-(sizeof(struct Node)));
	current->free = 'y';
	if ((current->prev)->free == 'y')	// the previous is also free
	{
		prev_current = current->prev;
		//add size of node because will be removing a node
		prev_current->size = ((prev_current->size)+(current->size)+sizeof(struct Node));
		prev_current->next = current->next;
		// reset the next space so that previous is set to that new free space.
		(current->next)->prev = prev_current;
		// now nothing should point to current because the previous' next points to currents next	
		// and current's next's prev node now points to prev_current.
		current = prev_current; // so now current is lost and prev_current contains the joint free space
	}
	if ((current->next)->free == 'y')	// the next node is also
	{
		next_current = current->next;
		current->size = ((current->size)+(next_current->size)+sizeof(struct Node));
		current->next = next_current->next;
		// set the previous field of the node after the next node (next node will be merged
		(next_current->next)->prev = current;
	}
}