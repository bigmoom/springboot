package com.cwh.springboot.consumer.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author cwh
 * @date 2021/6/23 11:38
 */
@Component
//设置监听队列
@RabbitListener(queues = "DirectQueue")
public class DirectConsumer {

    @RabbitHandler
    public void process(Map message){
        System.out.println("DirectConsumer:收到消息:"+message);
    }
}
