package com.cwh.springboot.publisher.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**设置主题订阅交换机
 * @author cwh
 * @date 2021/6/23 11:43
 */
@Configuration
public class TopicRabbitConfig {

//    设置两个topic
    public static final String man = "Consumer.man";
    public static final String woman = "Consumer.woman";

//    全匹配队列
    @Bean
    public Queue allTopicQueue(){
        return new Queue("AllTopicQueue");
    }

//    单一匹配队列
    @Bean
    public Queue manTopicQueue(){
        return new Queue("ManTopicQueue");
    }


    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange("TopicExchange");
    }

//    绑定单一匹配队列和交换机
//    设置匹配路由为唯一键，即与direct相同
    @Bean
    public Binding bindManTopic(){
        return BindingBuilder.bind(manTopicQueue()).to(topicExchange()).with(man);
    }

//    绑定全匹配队列和交换机
//    设置匹配路由为`Consumer.`的主题
    @Bean
    public Binding bindAllTopic(){
        return BindingBuilder.bind(allTopicQueue()).to(topicExchange()).with("Consumer.#");
    }
}
