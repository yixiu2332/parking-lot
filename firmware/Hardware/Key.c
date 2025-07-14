#include "Delay.h"
#include "ESP8266.h"
/**
  * 函    数：按键初始化
  * 参    数：无
  * 返 回 值：无
  */
void Key_Init(void)
{
	/*开启时钟*/
	RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOA, ENABLE);		//开启GPIOA的时钟
	
	/*GPIO初始化*/
	GPIO_InitTypeDef GPIO_InitStructure;
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IPU;
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_1;
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
	GPIO_Init(GPIOA, &GPIO_InitStructure);						
	
	RCC_APB2PeriphClockCmd(RCC_APB2Periph_AFIO,ENABLE);
	GPIO_EXTILineConfig(GPIO_PortSourceGPIOA,GPIO_PinSource1);
	
	EXTI_InitTypeDef EXTI_InitStrucature;
	EXTI_InitStrucature.EXTI_Line = EXTI_Line1;
	EXTI_InitStrucature.EXTI_LineCmd = ENABLE;
	EXTI_InitStrucature.EXTI_Mode = EXTI_Mode_Interrupt;
	EXTI_InitStrucature.EXTI_Trigger = EXTI_Trigger_Falling;
	EXTI_Init(&EXTI_InitStrucature);
	
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);
	NVIC_InitTypeDef NVIC_InitStructure;
	NVIC_InitStructure.NVIC_IRQChannel = EXTI1_IRQn;
	NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
	NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 1;
	NVIC_InitStructure.NVIC_IRQChannelSubPriority = 1;
	NVIC_Init(&NVIC_InitStructure);
}

/**
  * 函    数：按键获取键码
  * 参    数：无
  * 返 回 值：按下按键的键码值，范围：0~2，返回0代表没有按键按下
  * 注意事项：此函数是阻塞式操作，当按键按住不放时，函数会卡住，直到按键松手
  */
uint8_t Key_GetNum(void)
{
	uint8_t KeyNum = 0;		//定义变量，默认键码值为0
	
	return KeyNum;			//返回键码值，如果没有按键按下，所有if都不成立，则键码为默认值0
}

void EXTI1_IRQHandler(void){
	Delay_ms(20);
	if(EXTI_GetITStatus(EXTI_Line1)== SET){
		if(!Socket_Flag){
			ESP_SendString("AT+CIPSTART=\"TCP\",\"47.122.74.5\",8086");
		}else{
			ESP_SendString("AT+CIPCLOSE");
		}
	}
	EXTI_ClearITPendingBit(EXTI_Line1);
}
