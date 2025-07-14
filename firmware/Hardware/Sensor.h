#ifndef __SENSOR_H
#define __SENSOR_H
#include "stm32f10x.h"                  // Device header
extern volatile uint8_t Sensor_Flag;

void Sensor_Init(void);

#endif
