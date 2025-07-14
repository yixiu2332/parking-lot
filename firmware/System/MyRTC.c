#include "MyRTC.h"


void MyRTC_Init(void)
{
	/*开启时钟*/
	RCC_APB1PeriphClockCmd(RCC_APB1Periph_PWR, ENABLE);		//开启PWR的时钟
	RCC_APB1PeriphClockCmd(RCC_APB1Periph_BKP, ENABLE);		//开启BKP的时钟
	
	/*备份寄存器访问使能*/
	PWR_BackupAccessCmd(ENABLE);							//使用PWR开启对备份寄存器的访问
	
	RCC_LSEConfig(RCC_LSE_ON);							//开启LSE时钟
	while (RCC_GetFlagStatus(RCC_FLAG_LSERDY) != SET);	//等待LSE准备就绪
	
	RCC_RTCCLKConfig(RCC_RTCCLKSource_LSE);				//选择RTCCLK来源为LSE
	RCC_RTCCLKCmd(ENABLE);								//RTCCLK使能
	
	RTC_WaitForSynchro();								//等待同步
	RTC_WaitForLastTask();								//等待上一次操作完成
	
	RTC_SetPrescaler(32768 - 1);						//设置RTC预分频器，预分频后的计数频率为1Hz
	RTC_WaitForLastTask();								//等待上一次操作完成
	
	//2025.1.15 00:00 == 1736870400(unix时间戳)
	RTC_SetCounter(1736870400);
	RTC_WaitForLastTask();
}
