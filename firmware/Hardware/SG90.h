#ifndef __SG90_H
#define __SG90_H

#include "stm32f10x.h"                  // Device header
extern volatile uint8_t Lock_Falg;

void SG90_Init(void);
void SetRotationAngle(uint8_t angle);

#endif
