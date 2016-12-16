/*
	Simple program to modify a audio file's ID3 tags
	By Bryan Heim
*/

#include <stdio.h>
#include <strings.h>
#include <stdlib.h>

struct id3tag {
	char title[30];
	char artist[30];
	char album[30];
	char year[4];
	char comment[28];
	char tracknum;
	char genre;
};

int main(int argc,char* argv[])
{
	FILE *fp;
	struct id3tag object;
	struct id3tag *ptr;
	char tag_yes_no[3];
	int i;
	int arg_counter;
	int counter;
	
	 // removes the name of program and filename so counter holds number of elements
	counter = argc - 2;
	
	// sets up the counter for modifying a tag
	arg_counter = 2;
	
	if (argv[1] == NULL)
	{
		printf("No file given to open.");
		return -1;
	}
	fp = fopen(argv[1],"r");
	if (fp == NULL)
	{
		printf("File was not found.");
		return -1;
	}
	else
	{
		// there was a file entered on command prompt, and it was successfully opened
		fclose(argv[1]);
		if (2 == argc)
		{
			 // just name of project and name of file, print tag
			fp = fopen(argv[1],"rb");
			fseek( fp, -128, SEEK_END );
			fread(tag_yes_no, 1, 3, fp);
			if (strncmp(tag_yes_no,"tag",3) != 0)
			{
				printf("Sorry this file does not have id3 tags!");
				return -1;
			}
			else
			{
				 // fill and print the tag
				fseek( fp, -125, SEEK_END );
				fread( ptr, 125, 1, fp);
				printf("Title: %s", ptr->title);
				printf("Artist: %s", ptr->artist);
				printf("Album: %s", ptr->album);
				int year_int = atoi(ptr->year);
				printf("Year: %d", year_int);
				printf("Comment: %s", ptr->comment);
				printf("Comment: %d", ptr->tracknum);
				printf("Comment: %d", ptr->genre);
			}
		}
		else
		{ 
			// modifying the tag
			while(counter!=0)
			{
				if ( strncmp(argv[arg_counter],"-title",6)==0)
				{
					ptr->title = argv[arg_counter+1];
				}
				if ( strncmp(argv[arg_counter],"-artist",7)==0)
				{
					ptr->artist = argv[arg_counter+1];
				}
				if ( strncmp(argv[arg_counter],"-album",6)==0)
				{
					ptr->album = argv[arg_counter+1];
				}
				if ( strncmp(argv[arg_counter],"-year",5)==0)
				{
					ptr->year = argv[arg_counter+1];
				}
				if ( strncmp(argv[arg_counter],"-comment",8)==0)
				{
					ptr->comment = argv[arg_counter+1];
				}
				if ( strncmp(argv[arg_counter],"-track",6)==0)
				{
					ptr->track = argv[arg_counter+1];
				}
				if ( strncmp(argv[arg_counter],"-genre",6)==0)
				{
					ptr->genre = 0;
				}
				
				// subtracts two because input should be in pairs e.g. "-title 'Fun' "
				counter = counter - 2; 
				arg_counter = arg_counter + 2;
			}
			
			fp = fopen(argv[1],"r+b");
			fseek( fp, -125, SEEK_END );
			fwrite( ptr,125,1,fp);
			// write out tag!
			printf("Your tag was successfully modified.");
		}
	}
}