#include "Sensor.h"
#include "Delay.h"
#include "ESP8266.h"
#include "LED.h"
volatile uint8_t Sensor_Flag = 0;

void Sensor_Init(void){
    /* 开启时钟 */
    RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOC, ENABLE);       // 开启 GPIOC 的时钟
    RCC_APB2PeriphClockCmd(RCC_APB2Periph_AFIO, ENABLE);        // 开启 AFIO 的时钟 (用于外部中断路由)

    /* GPIO 初始化 */
    GPIO_InitTypeDef GPIO_InitStructure;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IPU;               // 上拉输入模式
    GPIO_InitStructure.GPIO_Pin = GPIO_Pin_15;                  // 选择 PC15
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;           // GPIO 速度
    GPIO_Init(GPIOC, &GPIO_InitStructure);                      // 初始化 GPIOC

//    /* 配置外部中断线路 */
//    GPIO_EXTILineConfig(GPIO_PortSourceGPIOC, GPIO_PinSource15); // 将 PC15 路由到 EXTI15

//    /* 配置 EXTI */
//    EXTI_InitTypeDef EXTI_InitStrucature;
//    EXTI_InitStrucature.EXTI_Line = EXTI_Line15;                // 选择 EXTI 线路 15
//    EXTI_InitStrucature.EXTI_LineCmd = ENABLE;                  // 使能 EXTI 线路
//    EXTI_InitStrucature.EXTI_Mode = EXTI_Mode_Interrupt;        // 中断模式
//    EXTI_InitStrucature.EXTI_Trigger = EXTI_Trigger_Rising_Falling; // 上升沿和下降沿触发
//    EXTI_Init(&EXTI_InitStrucature);

//    /* 配置 NVIC */
//    NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);             // 配置中断优先级分组
//    NVIC_InitTypeDef NVIC_InitStructure;
//    NVIC_InitStructure.NVIC_IRQChannel = EXTI15_10_IRQn;        // 选择 EXTI15_10 中断通道
//    NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;             // 使能中断通道
//    NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 1;   // 抢占优先级
//    NVIC_InitStructure.NVIC_IRQChannelSubPriority = 1;          // 子优先级
//    NVIC_Init(&NVIC_InitStructure);
}

//void EXTI15_10_IRQHandler(void) {
//    LED1_Turn(); // 直接翻转 LED
//    EXTI_ClearITPendingBit(EXTI_Line15); // 立即清除中断标志
//}
