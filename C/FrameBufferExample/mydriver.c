/*
	A test driver to be used with library.c from Project1.
	It makes use of all of the function calls and will allow
	the user to make a few rectangles then quit. Upon quitting
	a filled rectangle is made and the message "Bye!" is painted.
	By Bryan Heim
*/

#include "library.c"

int main(){
	char entered_char = ' ';
	init_graphics();
	clear_screen();
	sleep_ms(100);
	draw_rect(50,50,350,200,70);
	draw_text(350,240,"HI!",12000);
	do{
		entered_char = getkey();
		if(entered_char == 'a')
			draw_rect(50,50,500,350,95);
		if(entered_char == 's')
			draw_rect(75,75,400,300,155);
		if(entered_char == 'x')
			draw_rect(25,90,100,200,56000);
		if(entered_char == 'w')
			draw_rect(400,100,150,150,24000);
		if(entered_char == 'f')
			fill_rect(300,250,150,100,47000);
		if(entered_char == 'c')
			clear_screen();
		sleep_ms(250);
	}while(entered_char != 'q');
	clear_screen();
	fill_rect(50,50,350,300,34555);
	draw_text(350,240,"BYE!",1200);
	exit_graphics();
	return 0;
}
