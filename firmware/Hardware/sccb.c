#include "stm32f10x.h"
#include "sccb.h"
#include "delay.h"
//乱放测试
void OV7670_VSY_IRQn(void){
		//VSY中断
	RCC_APB2PeriphClockCmd(RCC_APB2Periph_AFIO,ENABLE);
	GPIO_EXTILineConfig(GPIO_PortSourceGPIOA,GPIO_PinSource12);
	
	EXTI_InitTypeDef EXTI_InitStrucature;
	EXTI_InitStrucature.EXTI_Line = EXTI_Line12;
	EXTI_InitStrucature.EXTI_LineCmd = ENABLE;
	EXTI_InitStrucature.EXTI_Mode = EXTI_Mode_Interrupt;
	EXTI_InitStrucature.EXTI_Trigger = EXTI_Trigger_Rising;
	EXTI_Init(&EXTI_InitStrucature);
	
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);
	NVIC_InitTypeDef NVIC_InitStructure;
	NVIC_InitStructure.NVIC_IRQChannel = EXTI15_10_IRQn;
	NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
	NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 1;
	NVIC_InitStructure.NVIC_IRQChannelSubPriority = 1;
	NVIC_Init(&NVIC_InitStructure);
}
/**************************************************************************************
 * 描  述 : OV7670芯片SCCB接口对应IO口初始化
 * 入  参 : 无
 * 返回值 : 无
 **************************************************************************************/

void SCCB_GPIO_Init(void)
{											   	 
 	GPIO_InitTypeDef  GPIO_InitStructure;
	
 	//使能OV7670芯片SCCB接口引脚对应IO端口时钟	
 	RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOA | RCC_APB2Periph_GPIOB, ENABLE);	 
	
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_11;				     //设置SDA对应引脚IO编号
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP; 		 //设置SDA对应引脚IO工作状态为推挽输出
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;		 //设置SDA对应引脚IO操作速度为50MHZ
	GPIO_Init(GPIOB, &GPIO_InitStructure);					     //初始化SDA对应引脚IO 

	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_11;				     //设置SCL对应引脚IO编号
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP; 		 //设置SCL对应引脚IO工作状态为推挽输出
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;		 //设置SCL对应引脚IO操作速度为50MHZ
	GPIO_Init(GPIOA, &GPIO_InitStructure);					     //初始化SCL对应引脚IO			
}			 

/**************************************************************************************
 * 描  述 : 配置OV7670芯片SCCB接口SDA为输出
 * 入  参 : 无
 * 返回值 : 无
 **************************************************************************************/
void SCCB_SDA_OUT(void)
{											   	 
 	GPIO_InitTypeDef  GPIO_InitStructure;
	
 	//使能OV7670芯片SCCB接口SDA引脚对应IO端口时钟	
 	RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOB, ENABLE);	 
	
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_11;				     //设置SDA对应引脚IO编号
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP; 		 //设置SDA对应引脚IO工作状态为推挽输出
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;		 //设置SDA对应引脚IO操作速度为50MHZ
	GPIO_Init(GPIOB, &GPIO_InitStructure);					     //初始化SDA对应引脚IO 
}	

/**************************************************************************************
 * 描  述 : 配置OV7670芯片SCCB接口SDA为输入
 * 入  参 : 无
 * 返回值 : 无
 **************************************************************************************/
void SCCB_SDA_IN(void)
{											   	 
 	GPIO_InitTypeDef  GPIO_InitStructure;
	
 	//使能OV7670芯片SCCB接口SDA引脚对应IO端口时钟	
 	RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOB, ENABLE);	 
	
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_11;				     //设置SDA对应引脚IO编号
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IPU; 		   //设置SDA对应引脚IO工作状态为上拉输入
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;		 //设置SDA对应引脚IO操作速度为50MHZ
	GPIO_Init(GPIOB, &GPIO_InitStructure);					     //初始化SDA对应引脚IO
}	

/**************************************************************************************
 * 描  述 : 配置OV7670芯片SCCB接口起始信号(模拟SCCB)
 * 入  参 : 无
 * 返回值 : 无
 **************************************************************************************/
void SCCB_Start(void)
{
    SCCB_SDA_H;     
    SCCB_SCL_H;	    
    Delay_us(50);  
    SCCB_SDA_L;
    Delay_us(50);	 
    SCCB_SCL_L;	
	Delay_us(50);	
}

/**************************************************************************************
 * 描  述 : 配置OV7670芯片SCCB接口停止信号(模拟SCCB)
 * 入  参 : 无
 * 返回值 : 无
 **************************************************************************************/
void SCCB_Stop(void)
{
    SCCB_SDA_L;
    Delay_us(50);	 
    SCCB_SCL_H;	
    Delay_us(50); 
    SCCB_SDA_H;	
    Delay_us(50);
} 

/**************************************************************************************
 * 描  述 : 配置OV7670芯片SCCB接口产生NAK信号(模拟SCCB)
 * 入  参 : 无
 * 返回值 : 无
 **************************************************************************************/
void SCCB_No_Ack(void)
{
	SCCB_SDA_H;	
	Delay_us(50);
	SCCB_SCL_H;	
	Delay_us(50);
	SCCB_SCL_L;	
	Delay_us(50);
	SCCB_SDA_L;	
	Delay_us(50);
}

/**************************************************************************************
 * 描  述 : OV7670芯片SCCB接口写一个字节数据(模拟SCCB)
 * 入  参 : 无
 * 返回值 : 0,成功;1,失败. 
 **************************************************************************************/
uint8_t SCCB_WriteAbyte(uint8_t dat)
{
	uint8_t i,temp;	 
	for(i=0;i<8;i++)     //循环8次发送一个字节数据
	{
		if(dat&0x80)
			SCCB_SDA_H;	
		else 
			SCCB_SDA_L;
		dat<<=1;
		Delay_us(50);
		SCCB_SCL_H;	
		Delay_us(50);
		SCCB_SCL_L;		   
	}			 
	SCCB_SDA_IN();		  //设置SDA为输入 
	Delay_us(50);
	SCCB_SCL_H;			    //接收第九位,以判断是否发送成功
	Delay_us(50);
	if(SCCB_Read_SDA)
		temp=1;           //SDA信号为高电平表示发送失败，返回1
	else 
		temp=0;           //SDA信号为低电平表示发送成功，返回0
	SCCB_SCL_L;		 
	SCCB_SDA_OUT();		  //读取SDA信号后，需设置SDA为输出    
	return temp;  
}	 

/**************************************************************************************
 * 描  述 : OV7670芯片SCCB接口读一个字节数据(模拟SCCB)
 * 入  参 : 无
 * 返回值 : 读到的一个字节数据
 **************************************************************************************/
uint8_t SCCB_ReadAbyte(void)
{
	uint8_t i,temp;    
	temp=0;             //对临时变量初始化值为0
	SCCB_SDA_IN();		  //设置SDA为输入  
	for(i=8;i>0;i--) 	  //循环8次接收数据
	{		     	  
		Delay_us(50);
		SCCB_SCL_H;
		temp=temp<<1;
		if(SCCB_Read_SDA)
			temp++;   
		Delay_us(50);
		SCCB_SCL_L;
	}	
	SCCB_SDA_OUT();		 //读取SDA信号后，需设置SDA为输出     
	return temp;
} 							    

/**************************************************************************************
 * 描  述 : OV7670芯片SCCB接口写寄存器操作(模拟SCCB)
 * 入  参 : 无
 * 返回值 : 0,成功;1,失败.
 **************************************************************************************/
uint8_t SCCB_Write_Reg(uint8_t reg,uint8_t data)
{
	uint8_t temp=0;
	SCCB_Start(); 					            //启动SCCB传输
	if(SCCB_WriteAbyte(OV7670_ID))
		temp=1;	                          //写器件ID	  
	Delay_us(50);
	if(SCCB_WriteAbyte(reg))
		temp=1;		                        //写寄存器地址	  
	Delay_us(50);
	if(SCCB_WriteAbyte(data))
		temp=1; 	                        //写数据	 
	SCCB_Stop();	                      //结束SCCB传输
	return	temp;
}		  					    

/**************************************************************************************
 * 描  述 : OV7670芯片SCCB接口读寄存器操作(模拟SCCB)
 * 入  参 : 无
 * 返回值 : 读到的寄存器值
 **************************************************************************************/
uint8_t SCCB_Read_Reg(uint8_t reg)
{
	uint8_t temp=0;
	SCCB_Start(); 				             //启动SCCB传输
	SCCB_WriteAbyte(OV7670_ID);		       //写器件ID	  
	Delay_us(100);	 
	SCCB_WriteAbyte(reg);		           //写寄存器地址	  
	Delay_us(100);	  
	SCCB_Stop();   	                   //结束SCCB传输
	Delay_us(100);	   
	SCCB_Start(); 				             //启动SCCB传输
	SCCB_WriteAbyte(OV7670_ID|0X01);   	 //发送读命令	  
	Delay_us(100);
	temp=SCCB_ReadAbyte();		         //读取数据
	SCCB_No_Ack();                     //发送NAK信号 
	SCCB_Stop();  	                   //结束SCCB传输
	return temp;
}

