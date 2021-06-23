package com.cwh.springboot.publisher.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author cwh
 * @date 2021/6/23 14:12
 */
@Configuration
public class FanoutRabbitConfig {

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
    public FanoutExchange fanoutExchange(){
        return new FanoutExchange("FanoutExchange");
    }

    @Bean
    public Binding bindingExchangeA(){
        return BindingBuilder.bind(fanQueueA()).to(fanoutExchange());
    }

    @Bean
    public Binding bindingExchangeB(){
        return BindingBuilder.bind(fanQueueB()).to(fanoutExchange());
    }

    @Bean
    public Binding bindingExchangeC(){
        return BindingBuilder.bind(fanQueueC()).to(fanoutExchange());
    }

}
