#include "stm32f10x.h"
#include "ov7670.h" 
#include "sccb.h"
#include "Delay.h"		 
#include "ov7670config.h"
#include "Serial.h"
#include "OV7670.h"
#include "ESP8266.h"
//是否有照片可读的标志

/**************************************************************************************
 * 描  述 : OV7670芯片初始化（IO口初始化及SCCB初始化）
 * 入  参 : 无
 * 返回值 : 0,成功;1,复位;2,读ID错误.
 **************************************************************************************/
static uint8_t vsy_flag = 0;
volatile uint8_t ov_flag = 0;
void EXTI15_10_IRQHandler(void){
	if(EXTI_GetITStatus(EXTI_Line12)== SET){
		vsy_flag++;
		if(vsy_flag == 1){
			FIFO_ResetWPoint();
			FIFO_OpenWriteData();
		}else if(vsy_flag == 2){
			NVIC_DisableIRQ(EXTI15_10_IRQn);   // 暂时关闭中断,防止读和写冲突
			FIFO_CloseWriteData();      // 关闭写允许
			vsy_flag = 0;
			ov_flag = 1;

		}

		EXTI_ClearITPendingBit(EXTI_Line12);
	}
	
}
uint8_t OV7670_Init(void)
{
	uint8_t temp;
	uint16_t i=0;

 	GPIO_InitTypeDef  GPIO_InitStructure;
	//使能OV7670芯片使用引脚对应IO端口时钟	
 	RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOA|RCC_APB2Periph_GPIOB|RCC_APB2Periph_GPIOC|RCC_APB2Periph_AFIO, ENABLE);	 //????????
  
	//VSY信号(PA12)
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_12;				         //设置PA1对应引脚IO编号
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IPU; 		       //设置PA1对应引脚IO工作状态为上拉输入
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;		     //设置PA1对应引脚IO操作速度为50MHZ
	GPIO_Init(GPIOA, &GPIO_InitStructure);					         //初始化PA1对应引脚IO
	

	//数据口(D7-D0:PA15,PB1,PB3,PB0,PB4,PA7,PB5,PA6)  	
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_15|GPIO_Pin_7|GPIO_Pin_6; //A组
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IPU; 		       //设置PC0~PC7对应引脚IO工作状态为上拉输入
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;		     //设置PC0~PC7对应引脚IO操作速度为50MHZ
	GPIO_Init(GPIOA, &GPIO_InitStructure);
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_1|GPIO_Pin_3|GPIO_Pin_0|GPIO_Pin_4|GPIO_Pin_5;//B组
	GPIO_Init(GPIOB, &GPIO_InitStructure); 				      
	
	//RESET信号(PB6)
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_6;		    
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP; 		    
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;		     
	GPIO_Init(GPIOB, &GPIO_InitStructure);
	GPIO_SetBits(GPIOB,GPIO_Pin_6);

	//PWDN信号(PA5)
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_5;		    
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP; 		    
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;		     
	GPIO_Init(GPIOA, &GPIO_InitStructure);		
	GPIO_ResetBits(GPIOA,GPIO_Pin_5);
	
	//FIFO引脚(FIFO_RCK:PA4 ,FIFO_WR:PB8 ,FIFO_OE:PA1 ,FIFO_WRST:PB9 ,FIFO_RRST:PC13)
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_4|GPIO_Pin_1;		    
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP; 		    
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;		     
	GPIO_Init(GPIOA, &GPIO_InitStructure);		
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_8|GPIO_Pin_9;
	GPIO_Init(GPIOB, &GPIO_InitStructure);	
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_13;
	GPIO_Init(GPIOC, &GPIO_InitStructure);
	//初始化
	GPIO_SetBits(GPIOB,GPIO_Pin_9);
	
 	SCCB_GPIO_Init();        		                             //初始化OV7670芯片SCCB接口对应IO口  	  
	
	SCCB_Write_Reg(0x12,0x80);
	if(SCCB_Write_Reg(0x12,0x80))                            //SCCB总线写操作OV7670芯片COM7寄存器
		return 1;	                                             //SCCB寄存器复位	  
	Delay_ms(50); 
	//读取产品识别号(需高位识别号和低位识别号都满足方可)
 	temp=SCCB_Read_Reg(0x0b);                                //SCCB总线读操作OV7670芯片VER寄存器 
	if(temp!=0x73) {                                          //判断读出的产品低位识别号是不是0x73
		printf("%x\n",temp);
		return 2; 												//若不是0x73说明芯片不是OV7670或硬件故障
	}		                      							
 	temp=SCCB_Read_Reg(0x0a);                                //SCCB总线读操作OV7670芯片PID寄存器   
	if(temp!=0x76)                                           //判断读出的产品低位识别号是不是0x76
		return 2;                                              //若不是0x76说明芯片不是OV7670或硬件故障


//	//SCCB总线写操作OV7670芯片剩余N个寄存器，以配置OV7670输出效果
	for(i=0;i<sizeof(ov7670_init_reg_table)/sizeof(ov7670_init_reg_table[0]);i++)
	{
	  if(SCCB_Write_Reg(ov7670_init_reg_table[i][0],ov7670_init_reg_table[i][1]))
		  return 0x38;
 	}
	OV7670_Window_Set(12,176,240,320);
   	return sizeof(ov7670_init_reg_table)/sizeof(ov7670_init_reg_table[0]);    //完成对OV7670寄存器操作，返回寄存器个数
} 
/**************************************************************************************

 * 描  述 : FIFO操作函数

 **************************************************************************************/

// 复位写指针
void FIFO_ResetWPoint(void){
	FIFO_Set_WRST(0);
    FIFO_Set_WRST(1);
}
// 复位读指针
void FIFO_ResetRPoint(void){
	FIFO_Set_RRST(0);
	FIFO_Set_RCK(0);
	FIFO_Set_RCK(1);
	FIFO_Set_RCK(0);
	FIFO_Set_RRST(1);
	FIFO_Set_RCK(1);
}

void FIFO_OpenWriteData(void){
	FIFO_Set_WR(1);
}
void FIFO_CloseWriteData(void){
	FIFO_Set_WR(0);
}
void FIFO_OpenReadData(void){
    FIFO_Set_OE(0);     // 允许写入
}
 
void FIFO_CloseReadData(void){
    FIFO_Set_OE(1);     // 禁止写入
}

void FIFO_ReadData(uint8_t* cache, int len){
    FIFO_ResetRPoint();
    FIFO_OpenReadData();
 
    for(int index = 0; index < len; ++index){
        FIFO_Set_RCK(1);
        cache[index] = RD_0 | (RD_1 << 1) | (RD_2 << 2) |(RD_3 << 3) | (RD_4 << 4) | (RD_5 << 5) | (RD_6 << 6) | (RD_7 << 7);
        FIFO_Set_RCK(0);
    }
 
    FIFO_CloseReadData();
}

void FIFO_ReadDataTest(void){
	ESP_SendString("IMG_START");
	uint16_t color;
	uint8_t value1,value2;
	
	FIFO_ResetRPoint();
    FIFO_OpenReadData();

	for(int i=0;i<320*240;i++){
		Delay_us(40);
		FIFO_Set_RCK(1);
        value1 = RD_0 | (RD_1 << 1) | (RD_2 << 2) |(RD_3 << 3) | (RD_4 << 4) | (RD_5 << 5) | (RD_6 << 6) | (RD_7 << 7);
        FIFO_Set_RCK(0);
		Delay_us(50);
		FIFO_Set_RCK(1);
        value2 = RD_0 | (RD_1 << 1) | (RD_2 << 2) |(RD_3 << 3) | (RD_4 << 4) | (RD_5 << 5) | (RD_6 << 6) | (RD_7 << 7);
        FIFO_Set_RCK(0);
		color = (value1 << 8) | value2;
		char hex_string[5]; 
		sprintf(hex_string, "%04X", color);
		ESP_CIPSend(hex_string);
	}
	ESP_CIPSend("\r\n");
	Delay_ms(10);
    FIFO_CloseReadData();
	ESP_CIPSend("\r\n");
}
/**************************************************************************************
 * 描  述 : 配置OV7670芯片相关寄存器以平衡色彩
 * 入  参 : uint8_t mode 场所模式
 * 返回值 : 无
 **************************************************************************************/
void OV7670_Light_Mode(uint8_t mode)
{
	uint8_t reg01val=0;
	uint8_t reg02val=0;
	uint8_t reg13val=0xE7;//默认就是设置为自动白平衡
	
	switch(mode)
	{
		case 1:                  //晴天
			reg01val=0x5A;
			reg02val=0x5C;
		  reg13val=0xE5;
			break;	
		case 2:                  //阴天
			reg01val=0x58;
			reg02val=0x60;
			reg13val=0xE5;
			break;	
		case 3:                  //办公室
			reg01val=0x84;
			reg02val=0x4c;
			reg13val=0xE5;
			break;	
		case 4:                  //居家
			reg01val=0x96;
			reg02val=0x40;
		  reg13val=0xE5;
			break;	
	}
	SCCB_Write_Reg(0x13,reg13val);     //SCCB总线写操作OV7670芯片COM7寄存器 
	SCCB_Write_Reg(0x01,reg01val);     //SCCB总线写操作OV7670芯片BLUE寄存器
	SCCB_Write_Reg(0x02,reg02val);     //SCCB总线写操作OV7670芯片RED寄存器
}				  

/**************************************************************************************
 * 描  述 : 配置OV7670芯片相关寄存器以进行色度设置
 * 入  参 : uint8_t tone 色度
 * 返回值 : 无
 **************************************************************************************/
void OV7670_Color_Saturation(uint8_t tone)
{
	uint8_t reg4fval=0x80;              //0
	uint8_t reg50val=0x80;
 	uint8_t reg52val=0x22;
	uint8_t reg53val=0x5E;
	uint8_t reg54val=0x80;
 	switch(tone)
	{
		case 0:                      //-2
			reg4fval=0x40; 
      reg50val=0x40;		
			reg52val=0x11;
			reg53val=0x2F;
		  reg54val=0x40;
			break;	
		case 1:                      //-1
			reg4fval=0x66;
      reg50val=0x66;		
			reg52val=0x1B;
			reg53val=0x4B;	
      reg54val=0x66;		
			break;	
		case 3:                      //1
			reg4fval=0x99;
      reg50val=0x99;			
			reg52val=0x28;
			reg53val=0x71;	
      reg54val=0x99;			
			break;	
		case 4:                      //2
			reg4fval=0xC0;
      reg50val=0xC0;		
			reg52val=0x33;
			reg53val=0x8D;
      reg54val=0xC0;		
			break;	
	}
	SCCB_Write_Reg(0x4F,reg4fval);	   //SCCB总线写操作OV7670芯片MTX1寄存器 
	SCCB_Write_Reg(0x50,reg50val);     //SCCB总线写操作OV7670芯片MTX2寄存器  
	SCCB_Write_Reg(0x51,0x00);	       //SCCB总线写操作OV7670芯片MTX3寄存器  
	SCCB_Write_Reg(0x52,reg52val);     //SCCB总线写操作OV7670芯片MTX4寄存器 
	SCCB_Write_Reg(0x53,reg53val);     //SCCB总线写操作OV7670芯片MTX5寄存器 
	SCCB_Write_Reg(0x54,reg54val);     //SCCB总线写操作OV7670芯片MTX6寄存器  
	SCCB_Write_Reg(0x58,0x9E);         //SCCB总线写操作OV7670芯片MTXS寄存器 
}

/**************************************************************************************
 * 描  述 : 配置OV7670芯片相关寄存器以进行亮度设置
 * 入  参 : uint8_t bright 亮度
 * 返回值 : 无
 **************************************************************************************/
void OV7670_Brightness(uint8_t bright)
{
	uint8_t reg55val=0x00;                      //0
  switch(bright)
	{
		case 0:                              //-2
			reg55val=0xB0;	 	 
			break;	
		case 1:                              //-1
			reg55val=0x98;	 	 
			break;	
		case 3:                              //1
			reg55val=0x18;	 	 
			break;	
		case 4:                              //2
			reg55val=0x30;	 	 
			break;	
	}
	SCCB_Write_Reg(0x55,reg55val);         //SCCB总线写操作OV7670芯片BRIGHT寄存器 
}

/**************************************************************************************
 * 描  述 : 配置OV7670芯片相关寄存器以进行对比度设置
 * 入  参 : uint8_t contrast 对比度
 * 返回值 : 无
 **************************************************************************************/
void OV7670_Contrast(uint8_t contrast)
{
	uint8_t reg56val=0x40;                      //0
  	switch(contrast)
	{
		case 0:                              //-2
			reg56val=0x30;	 	 
			break;	
		case 1:                              //-1
			reg56val=0x38;	 	 
			break;	
		case 3:                              //1
			reg56val=0x50;	 	 
			break;	
		case 4:                              //2
			reg56val=0x60;	 	 
			break;	
	}
	SCCB_Write_Reg(0x56,reg56val);         //SCCB总线写操作OV7670芯片CONTRAS寄存器 
}
	
/**************************************************************************************
 * 描  述 : 配置OV7670芯片相关寄存器以进行特效设置
 * 入  参 : uint8_t effect 特效
 * 返回值 : 无
 **************************************************************************************/
void OV7670_Special_Effects(uint8_t effect)
{
	uint8_t reg3aval=0x04;                      //普通模式
	uint8_t reg67val=0xC0;
	uint8_t reg68val=0x80;
	switch(effect)
	{
		case 1:                              //负片
			reg3aval=0x24;
			reg67val=0x80;
			reg68val=0x80;
			break;	
		case 2:                              //黑白
			reg3aval=0x14;
			reg67val=0x80;
			reg68val=0x80;
			break;	
		case 3:                              //偏红色
			reg3aval=0x14;
			reg67val=0xC0;
			reg68val=0x80;
			break;	
		case 4:                              //偏绿色
			reg3aval=0x14;
			reg67val=0x40;
			reg68val=0x40;
			break;	
		case 5:                              //偏蓝色
			reg3aval=0x14;
			reg67val=0x80;
			reg68val=0xC0;
			break;	
		case 6:                              //复古
			reg3aval=0x14;
			reg67val=0xA0;
			reg68val=0x40;
			break;	 
	}
	SCCB_Write_Reg(0x3A,reg3aval);         //SCCB总线写操作OV7670芯片TSLB寄存器
	SCCB_Write_Reg(0x67,reg67val);         //SCCB总线写操作OV7670芯片MANU寄存器
	SCCB_Write_Reg(0x68,reg68val);         //SCCB总线写操作OV7670芯片MANV寄存器
}	

/**************************************************************************************
 * 描  述 : 配置OV7670芯片相关寄存器以设置图像输出窗口
 * 入  参 : uint16_t sx,uint16_t sy,uint16_t width,uint16_t height
 * 返回值 : 无
 **************************************************************************************/
void OV7670_Window_Set(uint16_t sx,uint16_t sy,uint16_t width,uint16_t height)
{
	uint16_t endx;
	uint16_t endy;
	uint8_t temp; 
	endx=sx+width*2;	               
 	endy=sy+height*2;
	if(endy>784)endy-=784;
	temp=SCCB_Read_Reg(0x03);			         //SCCB总线读操作OV7670芯片VREF寄存器	
	temp&=0xF0;
	temp|=((endx&0x03)<<2)|(sx&0x03);
	SCCB_Write_Reg(0x03,temp);			       //SCCB总线写操作OV7670芯片VREF寄存器	
	SCCB_Write_Reg(0x19,sx>>2);		         //SCCB总线写操作OV7670芯片VSTRT寄存器
	SCCB_Write_Reg(0x1A,endx>>2);	         //SCCB总线写操作OV7670芯片VSTOP寄存器

	temp=SCCB_Read_Reg(0x32);			         //SCCB总线读操作OV7670芯片HREF寄存器	
	temp&=0xC0;
	temp|=((endy&0x07)<<3)|(sy&0x07);
	SCCB_Write_Reg(0x17,sy>>3);		         //SCCB总线写操作OV7670芯片HSTART寄存器
	SCCB_Write_Reg(0x18,endy>>3);	         //SCCB总线写操作OV7670芯片HSTOP寄存器
}

