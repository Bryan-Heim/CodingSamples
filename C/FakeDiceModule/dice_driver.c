/*
	A Linux kernel module /dev/dice that will simulate a dice roll 
	dice_read is the function called when a process calls read() on /dev/dice.  
	It writes a random number (between 0-5) to the buffer passed in the read() call.
	By Bryan Heim
*/


#include <linux/fs.h>
#include <linux/init.h>
#include <linux/miscdevice.h>
#include <linux/module.h>
#include <linux/random.h>
#include <asm/uaccess.h>

static unsigned char get_random_byte(int);
char char_buffer[1];
int len;

static ssize_t dice_read(struct file * file, char * buf, size_t count, loff_t *ppos)
{
	char *dice_random = char_buffer; 
	*dice_random = get_random_byte(5);
	len = 1; 
	if (count < len)
		return -EINVAL;
	if (*ppos != 0)
		return 0;
	if (copy_to_user(buf, dice_random, len))
		return -EINVAL;
	*ppos = len;
	return len;
}

static unsigned char get_random_byte(int max) {
	unsigned char c;
	get_random_bytes(&c,1);
	return c%max;	
}

static const struct file_operations dice_fops = {
	.owner		= THIS_MODULE,
	.read		= dice_read,
};

static struct miscdevice dice_dev = {
	MISC_DYNAMIC_MINOR,
	"dice",
	&dice_fops
};

static int __init
dice_init(void)
{
	int ret;
	ret = misc_register(&dice_dev);
	if (ret)
		printk(KERN_ERR "Unable to register \"Dice Driver!\" misc device\n");
	return ret;
}

module_init(dice_init);

static void __exit
dice_exit(void)
{
	misc_deregister(&dice_dev);
}

module_exit(dice_exit);

MODULE_LICENSE("GPL");
MODULE_AUTHOR("Bryan H <bph11@pitt.edu>");
MODULE_DESCRIPTION("\"Dice Driver!\" minimal module");
MODULE_VERSION("dev");
