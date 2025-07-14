#ifndef __ESP8266_H
#define __ESP8266_H

#include "stm32f10x.h"// Device header
#define BUFFER_SIZE 256
extern volatile uint8_t ESP_RxFlag;
extern char ESP_RxBuffer[BUFFER_SIZE];
extern volatile uint8_t WIFI_Flag;
extern volatile uint8_t Socket_Flag;
extern volatile uint8_t ChangeStatus_Flag;
void MyUSART_Init(uint32_t baudRate);
void ESP_SendByte(uint8_t Byte);
void ESP_SendString(const char *pStr);
void ESP_Init(uint32_t baudRate);
void processReceivedChar(uint8_t ch);
void processWIFI_GOT_IP(uint8_t ch);
void processWIFI_DISCONNECT(uint8_t ch);
void processCONNECT(uint8_t ch);
void processCLOSED(uint8_t ch);
void ESP_EndEndEnd(void);
void ESP_CIPSend(const char *pStr);
#endif
