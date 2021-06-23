package com.cwh.springboot.consumer.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author cwh
 * @date 2021/6/23 13:35
 */
@Component
@RabbitListener(queues = "AllTopicQueue")
public class AllTopicConsumer {
    @RabbitHandler
    public void process(Map message){
        System.out.println("AllTopicConsumer:收到消息:"+message);
    }
}
