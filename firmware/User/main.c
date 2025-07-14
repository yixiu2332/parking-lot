#include "stm32f10x.h"                  // Device header
#include "Delay.h"
#include "LED.h"
#include "Timer.h"
#include "ESP8266.h"
#include "Serial.h"
#include "OLED.h"
#include "Key.h"
#include "Sensor.h"
#include "SG90.h"
#include "MyRTC.h"
#include "cJSON.h"
#include <stdlib.h>
#include "OV7670.h"
#include "sccb.h"
#include <string.h>

//停车费
double cost1 = 0;
double cost2 = 0;
//车位
uint8_t empty = 0;
uint8_t max = 0;

void create_json(void);
void OLED_ShowShowShow(void);

//主函数
int main(void)
{

	
	uint8_t first = 1;
	LED_Init();
	Sensor_Init();
	Timer_Init();//TIM2
	Timer3_Init();
	SG90_Init(); 
//	MyRTC_Init();
	Serial_Init(2000000);
	ESP_Init(1000000);
	OV7670_Init();
	OV7670_VSY_IRQn();
	OLED_Init();
	SetRotationAngle(0);
	while (1)
	{	
		if(WIFI_Flag && !Socket_Flag && first){
			first = 0;
			//建立TCP连接
//			ESP_SendString("AT+CIPSTART=\"TCP\",\"192.168.137.1\",8086");
			ESP_SendString("AT+CIPSTART=\"TCP\",\"47.122.74.5\",8086");
			Delay_ms(1000);
			if(Socket_Flag){
		    //已经TCP建立连接，开始初始化透穿
			ESP_SendString("AT+CIPMODE=1");
			Delay_ms(100);
			ESP_SendString("AT+CIPSEND");
			Delay_ms(1000);
			ESP_CIPSend("DEVICE_ID:1\r\n");
			}else{
				printf("Socket异常，未开启透穿");
			}
		}
		//Update OLED
		if(ChangeStatus_Flag){
			ChangeStatus_Flag = 0;
			OLED_ShowShowShow();
		}
		//将串口助手发送的数据转发的ESP8266
		if(Serial_RxFlag){
			Serial_RxFlag = 0;
			//结束通传
			if(Serial_RxPacket[0] == '@'){
				ESP_EndEndEnd();
				continue;
			}else if(Serial_RxPacket[0] == '#'){
				FIFO_ReadDataTest();
				continue;
			}
			ESP_SendString(Serial_RxPacket);
		}
		//解析JSON
		if(ESP_RxFlag){
			printf("-------------\r\n");
			printf("%s\r\n",ESP_RxBuffer);
			cJSON *root = cJSON_Parse(ESP_RxBuffer);
			if(root == NULL){
				printf("JSON解析异常");
				continue;
			}
			char *result = cJSON_GetObjectItemCaseSensitive(root,"type")->valuestring;
			if(!strcmp(result,"test")){
				printf("已成功解析JSON");
			}else if(!strcmp(result,"lock")){
				if(cJSON_GetObjectItemCaseSensitive(root,"value") -> valueint){
					SetRotationAngle(90);
				}else if(Sensor_Flag == 0){
					SetRotationAngle(0);
				}
			}else if(!strcmp(result,"info")){
				cost1 = cJSON_GetObjectItemCaseSensitive(root,"cost1")->valuedouble;
				cost2 = cJSON_GetObjectItemCaseSensitive(root,"cost2")->valuedouble;
				empty = cJSON_GetObjectItemCaseSensitive(root,"empty")->valueint;
				max = cJSON_GetObjectItemCaseSensitive(root,"max")->valueint;
				ChangeStatus_Flag = 1;
			}
			ESP_RxFlag = 0;
		}
//		if(ov_flag && Socket_Flag && WIFI_Flag && ESP_Power){
//			printf("ov7670 start");
//			FIFO_ReadDataTest();
//			ov_flag =0;
//			NVIC_EnableIRQ(EXTI15_10_IRQn);
//			ESP_Power = 0;
//		}
		
		
	}
	
}




//其他函数
//问题锁定：先读取wifi和socket的标志位，再读取change的标志位.
//有可能出现，在函数刚进入就中断，如何标志位发生改变，但wifi和socket依然是旧值。
void OLED_ShowShowShow(void){
	uint8_t wifi = WIFI_Flag;
	uint8_t socket = Socket_Flag;
	OLED_Clear();
	if(!wifi){
		OLED_ShowString(0,0,"WIFI叉",OLED_8X16);
		OLED_ShowString(64,0,"TCP叉",OLED_8X16);
	}else if(!socket){
		OLED_ShowString(0,0,"WIFI勾",OLED_8X16);
		OLED_ShowString(64,0,"TCP叉",OLED_8X16);
	}else{
		OLED_ShowString(0,0,"WIFI勾",OLED_8X16);
		OLED_ShowString(64,0,"TCP勾",OLED_8X16);
	}
	
	OLED_Printf(0,16,OLED_8X16,"c1:%.1f",cost1);
	OLED_Printf(64,16,OLED_8X16,"c2:%.1f",cost2);
	OLED_Printf(0,32,OLED_8X16,"emp:%2d",empty);
	OLED_Printf(64,32,OLED_8X16,"max:%2d",max);
	OLED_ShowString(0,48,"Welcome parking",OLED_8X16);
	OLED_Update();
}




