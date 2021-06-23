package com.cwh.springboot.consumer.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author cwh
 * @date 2021/6/23 11:41
 */
@Component
@RabbitListener(queues = "DirectQueue")
public class DirectConsumer2 {
    @RabbitHandler
    public void process(Map message){
        System.out.println("DirectConsumer2:收到消息:"+message);
    }
}
