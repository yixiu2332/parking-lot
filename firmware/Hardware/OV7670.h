#include "stm32f10x.h"
			
#define OV7670_RRST_H   GPIO_SetBits(GPIOE,GPIO_Pin_6)
#define OV7670_RRST_L   GPIO_ResetBits(GPIOE,GPIO_Pin_6)
#define OV7670_RCK_H    GPIO_SetBits(GPIOF,GPIO_Pin_15)
#define OV7670_RCK_L    GPIO_ResetBits(GPIOF,GPIO_Pin_15)
#define OV7670_CS_H    GPIO_SetBits(GPIOG,GPIO_Pin_8)
#define OV7670_CS_L    GPIO_ResetBits(GPIOG,GPIO_Pin_8)
#define OV7670_WRST_H   GPIO_SetBits(GPIOC,GPIO_Pin_13)
#define OV7670_WRST_L   GPIO_ResetBits(GPIOC,GPIO_Pin_13)
#define OV7670_WEN_H   GPIO_SetBits(GPIOG,GPIO_Pin_5)
#define OV7670_WEN_L   GPIO_ResetBits(GPIOG,GPIO_Pin_5)

#define OV7670_DATA   GPIO_ReadInputData(GPIOC,0x00FF) 					//数据输入端口
#define CHANGE_REG_NUM 							171			                    //需要配置的寄存器总数		  
	
//数据口(D7-D0:PA15,PB1,PB3,PB0,PB4,PA7,PB5,PA6)  	
#define RD_7 GPIO_ReadInputDataBit(GPIOA,GPIO_Pin_15)
#define RD_6 GPIO_ReadInputDataBit(GPIOB,GPIO_Pin_1)
#define RD_5 GPIO_ReadInputDataBit(GPIOB,GPIO_Pin_3)
#define RD_4 GPIO_ReadInputDataBit(GPIOB,GPIO_Pin_0)
#define RD_3 GPIO_ReadInputDataBit(GPIOB,GPIO_Pin_4)
#define RD_2 GPIO_ReadInputDataBit(GPIOA,GPIO_Pin_7)
#define RD_1 GPIO_ReadInputDataBit(GPIOB,GPIO_Pin_5)
#define RD_0 GPIO_ReadInputDataBit(GPIOA,GPIO_Pin_6)
//FIFO引脚(FIFO_RCK:PA4 ,FIFO_WR:PB8 ,FIFO_OE:PA1 ,FIFO_WRST:PB9 ,FIFO_RRST:PC13)
#define FIFO_Set_RCK(val)  GPIO_WriteBit(GPIOA,GPIO_Pin_4,(BitAction)val)
#define FIFO_Set_WR(val)   GPIO_WriteBit(GPIOB,GPIO_Pin_8,(BitAction)val)
#define FIFO_Set_OE(val)   GPIO_WriteBit(GPIOA,GPIO_Pin_1,(BitAction)val)
#define FIFO_Set_WRST(val) GPIO_WriteBit(GPIOB,GPIO_Pin_9,(BitAction)val)
#define FIFO_Set_RRST(val) GPIO_WriteBit(GPIOC,GPIO_Pin_13,(BitAction)val)
//其他引脚
#define Read_VSY GPIO_ReadInputDataBit(GPIOA,GPIO_Pin_15)
#define Set_RESET(val)  GPIO_WriteBit(GPIOB,GPIO_Pin_6,(BitAction)val)
#define Set_PWDN(val)  GPIO_WriteBit(GPIOA,GPIO_Pin_5,(BitAction)val)

extern volatile uint8_t ov_flag;
extern u8 OV7670_Init(void);		  	   		 
extern void OV7670_Light_Mode(uint8_t mode);
extern void OV7670_Color_Saturation(uint8_t tone);
extern void OV7670_Brightness(uint8_t bright);
extern void OV7670_Contrast(uint8_t contrast);
extern void OV7670_Special_Effects(uint8_t effect);
extern void OV7670_Window_Set(uint16_t sx,uint16_t sy,uint16_t width,uint16_t height);
void FIFO_ResetWPoint(void);
void FIFO_ResetRPoint(void);
void FIFO_OpenReadData(void);
void FIFO_CloseReadData(void);
void FIFO_OpenWriteData(void);
void FIFO_CloseWriteData(void);
void FIFO_ReadData(uint8_t* cache, int len);
void FIFO_ReadDataTest(void);
void OV7670_VSY_IRQn(void);

