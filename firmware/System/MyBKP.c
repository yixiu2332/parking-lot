#include "MyBKP.h"

void MyBKP_Init(void){
	/*开启时钟*/
	RCC_APB1PeriphClockCmd(RCC_APB1Periph_PWR, ENABLE);		//开启PWR的时钟
	RCC_APB1PeriphClockCmd(RCC_APB1Periph_BKP, ENABLE);		//开启BKP的时钟
	
	/*备份寄存器访问使能*/
	PWR_BackupAccessCmd(ENABLE);							//使用PWR开启对备份寄存器的访问
	
//	BKP_WriteBackupRegister(BKP_DR1,0x0011);
//	BKP_ReadBackupRegister(BKP_DR1);
}
