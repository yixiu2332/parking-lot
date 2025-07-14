#include "ESP8266.h"
#include "Serial.h"
#include "Delay.h"
#include <string.h>

#define WIFI_GOT_IP "WIFI GOT IP"
#define WIFI_DISCONNECT "WIFI DISCONNECT"
#define CONNECT "CONNECT"
#define CLOSED "CLOSED"
#define JSON_Start "JSONStart"
#define JSON_Stop "JSONStop"

volatile uint8_t WIFI_Flag = 0;
volatile uint8_t Socket_Flag = 0;
volatile uint8_t ChangeStatus_Flag = 1;
volatile uint8_t Data_Run = 0;
char ESP_RxBuffer[BUFFER_SIZE] = {0};
volatile uint8_t ESP_RxFlag = 0;


void MyUSART_Init(uint32_t baudRate){
	/*开启时钟*/
	RCC_APB1PeriphClockCmd(RCC_APB1Periph_USART2, ENABLE);	//开启USART2的时钟
	RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOA, ENABLE);	//开启GPIOA的时钟
	
	/*GPIO初始化*/
	GPIO_InitTypeDef GPIO_InitStructure;
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF_PP;
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_2;
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
	GPIO_Init(GPIOA, &GPIO_InitStructure);					//将PA9引脚初始化为复用推挽输出
	
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IPU;
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_3;
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
	GPIO_Init(GPIOA, &GPIO_InitStructure);					//将PA10引脚初始化为上拉输入
	
	/*USART初始化*/
	USART_InitTypeDef USART_InitStructure;					//定义结构体变量
	USART_InitStructure.USART_BaudRate = baudRate;				//波特率
	USART_InitStructure.USART_HardwareFlowControl = USART_HardwareFlowControl_None;	//硬件流控制，不需要
	USART_InitStructure.USART_Mode = USART_Mode_Tx | USART_Mode_Rx;	//模式，发送模式和接收模式均选择
	USART_InitStructure.USART_Parity = USART_Parity_No;		//奇偶校验，不需要
	USART_InitStructure.USART_StopBits = USART_StopBits_1;	//停止位，选择1位
	USART_InitStructure.USART_WordLength = USART_WordLength_8b;		//字长，选择8位
	USART_Init(USART2, &USART_InitStructure);				//将结构体变量交给USART_Init，配置USART1
	
	/*中断输出配置*/
	USART_ITConfig(USART2, USART_IT_RXNE, ENABLE);			//开启串口接收数据的中断
	
	/*NVIC中断分组*/
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);			//配置NVIC为分组2
	
	/*NVIC配置*/
	NVIC_InitTypeDef NVIC_InitStructure;					//定义结构体变量
	NVIC_InitStructure.NVIC_IRQChannel = USART2_IRQn;		//选择配置NVIC的USART1线
	NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;			//指定NVIC线路使能
	NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 1;		//指定NVIC线路的抢占优先级为1
	NVIC_InitStructure.NVIC_IRQChannelSubPriority = 1;		//指定NVIC线路的响应优先级为1
	NVIC_Init(&NVIC_InitStructure);							//将结构体变量交给NVIC_Init，配置NVIC外设
	
	/*USART使能*/
	USART_Cmd(USART2, ENABLE);								//使能USART1，串口开始运行
}

void ESP_SendByte(uint8_t Byte)
{
	while (USART_GetFlagStatus(USART2, USART_FLAG_TXE) == RESET);	//等待发送完成
	USART_SendData(USART2, Byte);		//将字节数据写入数据寄存器，写入后USART自动生成时序波形
}

void ESP_SendString(const char *pStr)
{
	uint8_t i;
	for (i = 0; pStr[i] != '\0'; i ++)//遍历字符数组（字符串），遇到字符串结束标志位后停止
	{
		ESP_SendByte(pStr[i]);		//依次调用Serial_SendByte发送每个字节数据
	}
	ESP_SendByte('\r');
	ESP_SendByte('\n');
}

void ESP_CIPSend(const char *pStr)
{
	uint8_t i;
	for (i = 0; pStr[i] != '\0'; i ++)//遍历字符数组（字符串），遇到字符串结束标志位后停止
	{
		ESP_SendByte(pStr[i]);		//依次调用Serial_SendByte发送每个字节数据
	}
	
}
void ESP_EndEndEnd(void){
	for(int i =0;i<3;i++){
		ESP_SendByte('+');
	}
}
//初始化为STA（站点）模式
/*
1. AT+CWMODE=1//设置为 STA 模式
AT+RST//重启
AT+RESTORE//清除默认配置
AT+CWLAP//查看周围AP（WIFI热点）
2. AT+CWJAP_DEF="IQOOYYDS","68686868" //加入 WiFi 热点。
3. AT+CIPMUX=0//开启单连接
4. AT+CIPSTART="TCP","192.168.137.1",8086 //建立 TCP 连接到 192.168.4.1:8086
AT+CIPSEND=10//发送10字节数据

查看当前IP ： AT+CIFSR
扫描WIFI：AT+CWLAP
断开TCP ：AT+CIPCLOSE
断开WI-FI：AT+CWQAP
设置波特率：AT+UART_DEF=2000000,8,1,0,0
开启透穿：AT+CIPMODE=1
开始透穿：AT+CIPSEND
结束透穿：+++
*/

void ESP_Init(uint32_t baudRate){
	MyUSART_Init(baudRate);
//	Delay_ms(1000);
//	ESP_SendString("AT+CWMODE=1");
//	Delay_ms(500);
//	ESP_SendString("AT+RST");
//	Delay_ms(2000);
//	ESP_SendString("AT+CWJAP=\"R9000PYYDS\",\"68686868\"");
}



//解析WIFI_GOT_IP连接指令
void processWIFI_GOT_IP(uint8_t ch){
    static uint8_t state = 0;
    // Reset if the character does not match or buffer is full.
    if (ch != WIFI_GOT_IP[state]) {
        state = 0;
    }
    
    // Check for matching characters and update state.
    if (ch == WIFI_GOT_IP[state]) {
        state++;
        // If we've matched the entire string, set flag.
        if (state == sizeof(WIFI_GOT_IP) - 1) { // Exclude null terminator
            WIFI_Flag = 1;
			ChangeStatus_Flag = 1;
            state = 0; // Reset after successful match
			
        }
    }
}
//解析WIFI_DISCONNECT指令
void processWIFI_DISCONNECT(uint8_t ch){
    static uint8_t state = 0;
    
    // Reset if the character does not match or buffer is full.
    if (ch != WIFI_DISCONNECT[state]) {
        state = 0;
    }
  
    // Check for matching characters and update state.
    if (ch == WIFI_DISCONNECT[state]) {
		
        state++;
        
        // If we've matched the entire string, set flag.
        if (state == sizeof(WIFI_DISCONNECT) - 1) { // Exclude null terminator
            WIFI_Flag = 0;
			Socket_Flag = 0;
			ChangeStatus_Flag=1;
            state = 0; // Reset after successful match
        }
    }
}
//解析CONNECT指令
void processCONNECT(uint8_t ch){
    static uint8_t state = 0;
    
    // Reset if the character does not match or buffer is full.
    if (ch != CONNECT[state]) {
        state = 0;
    }
  
    // Check for matching characters and update state.
    if (ch == CONNECT[state]) {
		
        state++;
        
        // If we've matched the entire string, set flag.
        if (state == sizeof(CONNECT) - 1) { // Exclude null terminator
			

			Socket_Flag = 1;
			ChangeStatus_Flag=1;
            state = 0; // Reset after successful match
        }
    }
}
//解析CLOSED指令
void processCLOSED(uint8_t ch){
    static uint8_t state = 0;
    
    // Reset if the character does not match or buffer is full.
    if (ch != CLOSED[state]) {
        state = 0;
    }
  
    // Check for matching characters and update state.
    if (ch == CLOSED[state]) {
		
        state++;
        
        // If we've matched the entire string, set flag.
        if (state == sizeof(CLOSED) - 1) { // Exclude null terminator
			
            Socket_Flag = 0;
			ChangeStatus_Flag=1;
            state = 0; // Reset after successful match
		
        }
    }
}
//解析JSONStart指令
void processJSONStart(uint8_t ch){
    static uint8_t state = 0;
    
    // Reset if the character does not match or buffer is full.
    if (ch != JSON_Start[state]) {
        state = 0;
    }
  
    // Check for matching characters and update state.
    if (ch == JSON_Start[state]) {
		
        state++;
        
        // If we've matched the entire string, set flag.
        if (state == sizeof(JSON_Start) - 1) { // Exclude null terminator
			Data_Run = 1;
            state = 0; // Reset after successful match
        }
    }
}
static uint16_t Buf_index = 0;
//解析JSONStop指令
void processJSONStop(uint8_t ch){
    static uint8_t state = 0;
    
    // Reset if the character does not match or buffer is full.
    if (ch != JSON_Stop[state]) {
        state = 0;
    }
  
    // Check for matching characters and update state.
    if (ch == JSON_Stop[state]) {
		
        state++;
        
        // If we've matched the entire string, set flag.
        if (state == sizeof(JSON_Stop) - 1) { // Exclude null terminator
			Data_Run = 0;
			ESP_RxBuffer[Buf_index - state - 1] = '\0';
			ESP_RxFlag = 1;
			Buf_index = 0;
            state = 0; // Reset after successful match
        }
    }
}

//BUG：当接收到WIFI_DISCONNECT，CONNECT也会触发。
//解决：当WIFI_Flag为0时，不进行Socket验证。
void processReceivedChar(uint8_t ch){
	
	if(!Data_Run){
		processJSONStart(ch);
	//先处理WIFI连接
		if(!WIFI_Flag){
			processWIFI_GOT_IP(ch);
		}else{
			processWIFI_DISCONNECT(ch);
		}
	
	//然后处理Socket连接
		if(WIFI_Flag){
			if(!Socket_Flag){
				processCONNECT(ch);
			}else{
				processCLOSED(ch);
			}
		}
	}else{
		if(ESP_RxFlag) return;
		ESP_RxBuffer[Buf_index]=ch;
		Buf_index++;
		processJSONStop(ch);
	}
	
	Serial_SendByte(ch);
}
/**
  * 函    数：USART2中断函数
  * 参    数：无
  * 返 回 值：无
  * 注意事项：此函数接受数据格式适用于ESP8266-01S模块的通信
  */
void USART2_IRQHandler(void)
{
	if (USART_GetITStatus(USART2, USART_IT_RXNE) == SET)	//判断是否是USART1的接收事件触发的中断
	{
		uint8_t RxData = USART_ReceiveData(USART2);			//读取数据寄存器，存放在接收的数据变量
		processReceivedChar(RxData);
//		Serial_SendByte(RxData);
		USART_ClearITPendingBit(USART2, USART_IT_RXNE);		//清除标志位
	}
}
