#include "stm32f10x.h"

#define SCCB_SDA_L  GPIO_ResetBits(GPIOB,GPIO_Pin_11)
#define SCCB_SDA_H  GPIO_SetBits(GPIOB,GPIO_Pin_11)

#define SCCB_SCL_L  GPIO_ResetBits(GPIOA,GPIO_Pin_11)
#define SCCB_SCL_H  GPIO_SetBits(GPIOA,GPIO_Pin_11)

#define SCCB_Read_SDA  (GPIO_ReadInputDataBit(GPIOB, GPIO_Pin_11) == 1)

#define OV7670_ID   			0x42  

extern void SCCB_GPIO_Init(void);
extern void SCCB_Start(void);
extern void SCCB_Stop(void);
extern void SCCB_No_Ack(void);
extern uint8_t SCCB_WriteAbyte(uint8_t dat);
extern uint8_t SCCB_ReadAbyte(void);
extern uint8_t SCCB_Write_Reg(uint8_t reg,uint8_t data);
extern uint8_t SCCB_Read_Reg(uint8_t reg);










