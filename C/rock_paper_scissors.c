/*
	A rock, paper, scissors game written in C that you can play against the computer
	By Bryan Heim
*/

#include <stdio.h>
#include <strings.h>

int main()
{
  /*
     Create declarations FIRST to make c89 compliant
	 Note: Indentation is slightly off due to using two different editing tools 
  */
  
  int flag;
  int play_flag;
  int play_again_flag;
  int computer_wins;
  int player_wins;
  char start_yes_no[256]; //setup buffer for input
  char again_yes_no[256];
  char player_choice [256];
  char player_assignment[30]; // allowed room to hold all the letters in rock paper scissors including 6 extra spaces incase a null terminator needs to be placed with newline and 6 just incase
  
  play_again_flag = 1;
  computer_wins = 0;
  player_wins = 0;
  printf("Welcome to Rock, Paper, Scissors/n");
  do(
    printf("would you like to play?(lowercase yes/no only):  ");
    scanf("%4s", start_yes_no);
    if(strncmp("yes",start_yes_no,3)==0 || strncmp("no",start_yes_no,2)==0)
    {
       flag = 1;
       if (strncmp("no\n",start_yes_no,3)==0)
       {
         play_flag = 0; 
       }	
    }
    else
    {
      printf("Invalid input, please try again.");
    }
  ) while(1 != flag);
  if (play_flag == 0)
  {
    printf("You chose not to play. Goodbye.");
  }
  else    // game begin
  {
   while (play_again_flag != 0)
   {
     while(computer_wins != 3 || player_wins !=3) // a counter to check if either has won 3 games
     {
           do { //input loop
                scanf("What is your choice? %8s", player_choice);
                if(strncmp("sissors",player_choice,7))
               {
				  player_assignment = 1;  //scissors
                  play_failed_input  = 1;
               }
               if(strncmp("rock",player_choice,4))
              {
				  player_assignment = 2; //rock
                  play_failed_input  = 1;
              }
              if(strncmp("paper",player_choice,5))
             {
			      player_assignment = 3; //paper
                  play_failed_input  = 1;
             }
            else
            { 
				  printf("Invalid input. Please try again.\n");
            }
         }while(0 == play_failed_input )
         //back into game loop
         srand((unsigned int)time(NULL));
         computer_assignment = rand() % (3 - 1 + 1) + 1;
         if (computer_assignment == 1 && player_assignment == 3)
		 {
			computer_wins++;
			printf("The computer chose scissors. You lose this game!\n");
		 }
		 if (computer_assignment == 2 && player_assignment == 1)
		 {
		    computer_wins++;
			printf("The computer chose rock. You lose this game!\n");
		 }
		 if (computer_assignment == 3 && player_assignment == 2)
		 {
			computer_wins++;
			printf("The computer chose paper. You lose this game!\n");
		 }
		 if (player_assignment == 1 && player_assignment == 3)
		 {
			player_wins++;
			printf("The computer chose paper. You win this game!\n");
		 }
		 if (player_assignment == 2 && player_assignment == 1)
		 {
		    player_wins++;
			printf("The computer chose scissors. You win this game!\n");
		 } 
		 if (player_assignment == 3 && player_assignment == 2)
		 {
			player_wins++;
			printf("The computer chose rock. You win this game!\n");
		 }
         else
		 {
			printf("Computer chose the same. Game is a tie.");
		 }
		 printf("\nThe score is now you: %d computer: %d", player_wins,computer_wins);
     }
	// would u like to play again? 
	printf("/nThe match is now over. Would you like to play again?");
	flag = 0; //reset flag to false
	do(
		printf("\nThe match is now over. Would you like to play again?(yes/no)");
		scanf("%s", again_yes_no);
		if(strncmp("yes",again_yes_no,3)==0 || strncmp("no",again_yes_no,2)==0)
		{
			flag = 1;
			if (strncmp("no",again_yes_no,3)==0)
			{
				play_again_flag = 0; 
			}	
		}
		else
		{
			printf("Invalid input, please try again.");
		}
	) while(1 != flag);
	player_wins = 0;
	computer_wins = 0;
	player_assignmnet = 0;
	computer_assignmnet = 0;
  }
  printf("Thanks for playing! Goodbye.");
 }

 }