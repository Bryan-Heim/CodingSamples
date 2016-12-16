/*
	A library that allows a user to memory map the frame-buffer and render some basic shapes/strings
	I made sure to comment my code throughout to ease in understanding.
	By Bryan Heim
*/

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <sys/select.h>
#include <sys/time.h>
#include <sys/types.h>
#include <linux/fb.h>
#include <termios.h>
#include <unistd.h>
#include "iso_font.h"

int file_descript; // to open framebuffer
void *frame_buf_map; // for memory mapping
int length; // to get length for mmap
int check; // used in a some of the functs to check return values
struct termios yield, take; // to get old terminal, set new settings
typedef unsigned short color_t; // for the color they enter

void init_graphics(){

	struct fb_var_screeninfo first_struct;
	struct fb_fix_screeninfo second_struct;

	file_descript = open("/dev/fb0",O_RDWR);
	if (file_descript == -1)
		return;

	check = ioctl(file_descript,FBIOGET_VSCREENINFO,&first_struct);
	if (check == -1)
		return;
	check = ioctl(file_descript,FBIOGET_FSCREENINFO,&second_struct);
	if (check == -1)
		return;
	length = first_struct.yres_virtual*second_struct.line_length;

	frame_buf_map = (void *)mmap(0,length,PROT_WRITE,MAP_SHARED,file_descript,0);
	if(frame_buf_map == ((void *) -1)) //mapped unsuccessfully
		return;
		
	// if you make it here, everything was successful and
	// the frame-buffer is successfully mapped to memory
	check = ioctl(0,TCGETS,&yield);
	if (check == -1)
		return;
		
	//disable echo and canonical mode
	take = yield;
	take.c_lflag &= ~ECHO;
	take.c_lflag &= ~ICANON;
	check = ioctl(0,TCSETS,&take);
	
	/* 
	* graphics init done at this point
	* file_descript set, frame_buf_map is set
	* and the terminal disabled echo and canonical mode
	*/
	return;
}

void exit_graphics(){
	// enable previous state - echo and keypress
	check = ioctl(0,TCSETS,&yield);
	// close frame buffer and un-map the memory
	int fd_returner = close(file_descript);
	int map_returner = munmap(frame_buf_map,length);
	return;
}

void clear_screen(){
	// simply write the provided string to the terminal
	size_t bytes_returned;
	bytes_returned = write(1,(void *)"\033[2J",6);
	return;
}

char getkey(){
	ssize_t breturned;
	char keypress;
	struct timeval tv;
	tv.tv_sec=0;// set both to 0 because no blocking
	tv.tv_usec=0;
	fd_set readfds;
	FD_ZERO(&readfds);
	FD_SET(0,&readfds);
	check = select(1,&readfds,NULL,NULL,&tv);
	// if they type a key then make call to read
	if(check)
	{
		breturned = read(0,&keypress,1);
		// they typed a key so read it
	}
	return keypress;
}

void sleep_ms(long ms){
	struct timespec to_sleep;
	ms = ms * 1000000;
	to_sleep.tv_sec = 0;
	to_sleep.tv_nsec = ms;
	check = nanosleep(&to_sleep,NULL);
	// get time in ms, scale, and go to sleep
	return;
}

void draw_pixel(int x, int y, color_t color){

	// if they want a pixel out of bounds return
	if(x < 0 || x > 639 || y < 0 || y > 479)
		return;
	// they have a valid place to put a pixel
	// make copy of buffer to then do pointer arithm on
	void *temp_buf_addr = frame_buf_map;
	int scaler = (x+(y*640))*2;// times 2 to fix bug of 2 rectangles
	*((color_t*)(temp_buf_addr+scaler)) = color;
	// set the pixel at the given offset
	return;
}

void draw_rect(int x1, int y1, int width, int height, color_t c){
	int j, i;

	if(x1 < 0 || x1 > 639 || y1 < 0 || y1 > 479 || (x1+width) < 0 || 
	(x1+width) > 639 || (y1+height) < 0 || (y1+height) > 439)
		return; // the rectangle will be out of bounds
	else
	{
		for(j = y1; j<y1+height; j++)
		{
			if(j == y1 || j == (y1+height-1)) //draw horz
			{
				for(i = x1; i<x1+width; i++)
				{
					draw_pixel(i,j,c);
				}
			}
			else // will only draw vertical lines
			{
				draw_pixel(x1,j,c); //draw first
				draw_pixel(x1+width,j,c); //draw last
			}
		}
	}
	return;
}

void fill_rect(int x1, int y1, int width, int height, color_t c){
	int j, i;
	if(x1 < 0 || x1 > 639 || y1 < 0 || y1 > 479 || (x1+width) < 0 || 
	(x1+width) > 639 || (y1+height) < 0 || (y1+height) > 479)
		return; // will be out of bounds
	else
	{
		for(j = y1; j<y1+height; j++)
		{
			for(i = x1; i<x1+width; i++)
			{
				draw_pixel(i,j,c);
			}
		}
		// rectangle will fill at each level
	}
}

void draw_single_char(int x, int y, const char a, color_t c){
	int ascii_needed, i, j, index, tempx, tempy;
	if(x < 0 || x > 639 || y < 0 || y > 479 || (x+8) > 639 || (y+16) 
	> 479)
		return; //bad start or wont fit without going over
	else // the letter will fit
	{
		ascii_needed = (int) a; // get ascii value
		tempx = x; // to set starting x coloumn
		tempy = y; // to set starting y row

		for(j = 0; j<16; j++) // for each of the rows
		{
			index = iso_font[ascii_needed*16+j];
			for(i = 0; i<8; i++) // for each x
			{
				if( (index & ( 1 << i )) )
					draw_pixel(tempx,tempy,c);
				// shift and mask with and
				// if the bit is 1, paint pixel
				tempx++;// next possible x to print to
			}
			tempx = x; // reset for next row
			tempy++; // add 1 because going to next row
		}
	}
	return;
}

// Prints whole string if it fits on the screen
void draw_text(int x, int y, const char *text, color_t c){
	int letter = 0;

	if(x < 0 || x > 639 || y < 0 || y > 479)
		return; // invalid place on screen
	while(text[letter] != '\0')
	{
		draw_single_char(x,y,text[letter],c);
		x = x+9; // next character needs moved over
		// y will remain the same to draw on same line
		letter++;
		// loop through until '/O' and print each letter
	}
	return;
}
