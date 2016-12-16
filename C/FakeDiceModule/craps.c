/* 
	A simple craps game that was used to test the /dev/dice module
	By Bryan Heim
*/

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

int main() 
{
	int dice_roll1;
	int dice_roll2;
	int dice_total;
	int point;
	int play_flag;
	int play_again_flag;
	int win_lose_flag;
	char name_buffer[50]; // 50 to ensure long names can be entered
	char play_buffer[4]; // 4 because either play or quit
	char again_buffer[3]; // 3 because either yes or no
	
	play_flag = 0;
	printf("Welcome to Bryan's Casino!\n");
	printf("Please enter your name: ");
	scanf("%50s", name_buffer); // %50 to ensure that only the first 50 chars entered are taken
	printf("%s, would you like to Play or Quit?: ");
	
	while(play_flag == 0)	// will loop through until a valid input (play or quit) is given
	{
		scanf("%4s", play_buffer); // %4 because play or quit has a max of 4 letters
		if(strncmp(play_buffer,"play",4)==0 || strncmp(play_buffer,"Play",4)==0 || strncmp(play_buffer,"PLAY",4)==0 )
			play_flag = 1;	// they want to play
		if(strncmp(play_buffer,"quit",4)==0 || strncmp(play_buffer,"Quit",4)==0 || strncmp(play_buffer,"QUIT",4)==0 )
			play_flag = -1; // they quit
		if(play_flag == 0)
			printf("\nYou have entered an invalid input, please try again: ");
	}
	if(play_flag == -1) // if they chose to quit show goodbye and exit
	{
		printf("\nYou chose to quit! Goodbye!\n");
		return 0;
	}
	play_again_flag = 1; // they wanted to play the first time.
	
	// open the /dev/dice to be used in simulation a dice roll
	FILE* fd = fopen("/dev/dice", "r");

	if(fd != NULL)
	{
		while(play_again_flag == 1) // Overall game loop, will run at least once if they typed play
		{
			win_lose_flag = 0;
			printf("\n");
			fread(&dice_roll1, sizeof(char), 1, fd);
			fread(&dice_roll2, sizeof(char), 1, fd);
			dice_total = dice_roll1+dice_roll2;
			printf("You first rolled a: %d + %d = %d\n", dice_roll1,dice_roll2,dice_total);
			if (dice_total == 7 || dice_total == 11)
				win_lose_flag = 1; // user won on first roll
			if (dice_total == 2 || dice_total == 3 || dice_total == 12)
				win_lose_flag = -1; // user loss on first roll
			while (win_lose_flag == 0)	// they didnt win or lose, so roll until either a 7 or they reroll dice_total
			{
				fread(&dice_roll1, sizeof(char), 1, fd);
				fread(&dice_roll2, sizeof(char), 1, fd);
				point = dice_roll1 + dice_roll2;
				printf("Your next roll was: %d + %d = %d\n", dice_roll1,dice_roll2,point);
				if(point == 7) //you lose
					win_lose_flag = -1;
				if(point == dice_total)
					win_lose_flag = 1;	// they rolled the "point" and won
			}
			if(win_lose_flag == 1)
				printf("You WIN!\n\n");
			if(win_lose_flag == -1)
				printf("Sorry, you lost.\n\n");
			printf("Would you like to play again? (yes/no): ");
			play_again_flag = 0; // set back to neutral for validating input
			while(play_again_flag == 0)
			{
				scanf("%3s", again_buffer); // %3 because it will either read yes or no
				if(strncmp(again_buffer,"no",2)==0 || strncmp(again_buffer,"No",2)==0 || strncmp(again_buffer,"NO",2)==0 )
					play_again_flag = -1;
				if(strncmp(again_buffer,"yes",3)==0 || strncmp(again_buffer,"Yes",3)==0 || strncmp(again_buffer,"YES",3)==0 )
					play_again_flag = 1;
				if(play_again_flag == 0)
					printf("\nYou have entered an invalid input, please try again: ");
			}
			// if they chose "yes" then it will go back to beginning of game loop, else print goodbye
		}
		printf("Goodbye %s! Hope to see you soon.\n\n", name_buffer);
		return 0;
	}
	else
		printf("Unable to open the dice module, ensure it is installed.");
}
