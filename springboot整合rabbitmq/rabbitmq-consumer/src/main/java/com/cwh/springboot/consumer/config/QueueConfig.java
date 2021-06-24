package com.cwh.springboot.consumer.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author cwh
 * @date 2021/6/23 14:19
 */
@Configuration
public class QueueConfig {

    @Bean
    public Queue directQueue(){
        return new Queue("DirectQueue");
    }

    @Bean
    public Queue fanQueueA(){
        return new Queue("FanQueueA");
    }

    @Bean
    public Queue fanQueueB(){
        return new Queue("FanQueueB");
    }

    @Bean
    public Queue fanQueueC(){
        return new Queue("FanQueueC");
    }

    @Bean
    public Queue testQueue(){
        return  new Queue("TestQueue");
    }

}
