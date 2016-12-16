/*
	An implementation of a Unix shell appropriately named "Bryan's Shell"
	It supports "cd", "exit", and any other Unix command including the option to:
		- output redirect to a file - ">"
		- output append to a file - ">>"
		- input redirect from a file - "<"
	By Bryan Heim
*/


#include <stdio.h> 
#include <stdlib.h> 
#include <string.h> 
#include <unistd.h>

int main(){

	char in_holder[512]; // this buffer will be used to holder userinput
	char *tok_holder; // this will be used to get the returned values from strtok()
	char delimeter[8]; // 8 because it includes delimiters for space, tab, newline, (, ), |, &, ; 
	char *arg_holder[50]; // support up to 50 different options, not all this space is need but just incase
	int arg_count;
	int arg_count_fcheck; // will be used to determine if file operations needed 
	int exit_flag;
	int ret_val;	// used to get the return values and print errors if needed
	int line_count;

	delimeter[0] = 32; // ascii value for space
	delimeter[1] = 9; // ascii for tab
	delimeter[2] = 10; // ascii for newline
	delimeter[3] = 40; // ascii for (
	delimeter[4] = 41; // ascii for )
	delimeter[5] = 124; // ascii for |
	delimeter[6] = 38; // ascii for &
	delimeter[7] = 59; // ascii for ;
	
	line_count = 1;
	exit_flag = 0;
	while(exit_flag == 0)
	{
		arg_count = 0;
		printf("(%d) Bryan's Shell $ ",line_count);
		fgets(in_holder,512,stdin);
		
		 // fgets used to obtain the user string entered
		if(in_holder[0]=='\n')
			printf("");
		else
		{
			// only add to the shell's line number if they do something
			line_count++;
			tok_holder = strtok(in_holder,delimeter);
			while(tok_holder != NULL)
			{
				//printf("Test token found!: %s\n",tok_holder);
				arg_holder[arg_count] = tok_holder;
				arg_count++;	
				tok_holder = strtok(NULL, delimeter);
			}
			arg_holder[arg_count] = NULL;
			arg_count_fcheck = arg_count-2; 
			// setup for use in handling >, >>, and < operators
			// the -2 is used to get rid of the null and file provided and gives the index of the >,>>, or <
			if(strncmp(arg_holder[0],"exit",4)==0)
			{
				exit_flag = 1;
				printf("Shell now exiting. Goodbye!\n");
			}
			else
			{
				// they hit enter and arent exiting
				if(strncmp(arg_holder[0],"cd",2)==0) 
				{
					// they want to change directory
					ret_val = chdir(arg_holder[1]);
					if(ret_val == 0)
						printf("Directory changed successfully.\n");
					else // there was an error
						perror("Directory NOT changed!");		
				}
				else
				{
					 // they wish to do something that is not exit or change directory
					if(arg_count == 1 || arg_count == 2) //command\ wihtouth < or >
					{
						if(fork()==0)
						{
							ret_val = execvp(arg_holder[0],arg_holder);
							if(ret_val == -1)
								perror("Oh No! Error!");
							exit(0);
						}
						else
						{
							int status;
							wait(&status);
							//printf("Operation complete.\n");
						}
					}
					else
					{
						 // command may contain a < or > that must be done
						if(strncmp(arg_holder[arg_count_fcheck],">>",2) == 0)
						{
							if(fork()==0)
							{
								FILE *fp;
								fp = freopen(arg_holder[arg_count-1],"a",stdout);
								arg_holder[arg_count-1] = NULL;
								arg_holder[arg_count_fcheck] = NULL;
								ret_val = execvp(arg_holder[0],arg_holder);
								if(ret_val == -1)
									perror("Oh No! Error!");
								fclose(fp);
								exit(0);
							}
							else
							{
								int status;
								wait(&status);
							}
						}
						else if(strncmp(arg_holder[arg_count_fcheck],">",1) == 0)
						{
							if(fork()==0)
							{
								FILE *fp;
								fp = freopen(arg_holder[arg_count-1],"w+",stdout);
								arg_holder[arg_count-1] = NULL; // so the output filename isnt sent as an argument
								arg_holder[arg_count_fcheck] = NULL; // > is not part of the arugments
								ret_val = execvp(arg_holder[0], arg_holder);
								if(ret_val == -1)
									perror("Oh No! Error!");
								fclose(fp);
								exit(0);
							}
							else
							{
								int status;
								wait(&status);
							}
						}
						else if(strncmp(arg_holder[arg_count_fcheck],"<",1) == 0)
						{
							if(fork()==0)
							{
								FILE *fp;
								fp = freopen(arg_holder[arg_count-1],"r",stdin);
								arg_holder[arg_count-1] = NULL;
								arg_holder[arg_count_fcheck] = NULL;
								ret_val = execvp(arg_holder[0],arg_holder);
								if(ret_val == -1)
									perror("Oh No! Error!");
								fclose(fp);
								exit(0);
							}
							else
							{
								int status;
								wait(&status);
							}
						}
						else
						{
							 // command was longer than 2 and not a file operation
							if(fork()==0)
							{
								ret_val = execvp(arg_holder[0],arg_holder);
								if(ret_val == -1)
									perror("Oh No! Error!");
								exit(0);
							}
							else
							{
								int status;
								wait(&status);
							}
						}
					}
				}
			}
		}
	}
	exit(0);
}
