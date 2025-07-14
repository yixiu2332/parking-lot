#ifndef __TIMER_H
#define __TIMER_H
#include "stm32f10x.h"                  // Device header

void Timer_Init(void);
void Timer3_Init(void);
extern volatile uint8_t ESP_Power;
#endif
