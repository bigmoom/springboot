package com.cwh.springboot.consumer.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author cwh
 * @date 2021/6/23 16:42
 */
@Configuration
public class MessageListenerConfig {

    @Autowired
    private CachingConnectionFactory cachingConnectionFactory;

    @Autowired
    private  MyMessageListener myMessageListener;

    /**
     * 配置listenercontainer,添加自定义listener
     * @return
     */
    @Bean
    public SimpleMessageListenerContainer simpleMessageListenerContainer(){
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(cachingConnectionFactory);
        container.setConcurrentConsumers(1);
        container.setMaxConcurrentConsumers(10);
//        RabbitMq默认是自动确认，这里改为手动确认
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
//      设置queue
        container.setQueueNames("TestQueue");
        container.setMessageListener(myMessageListener);

        return container;
    }
}
