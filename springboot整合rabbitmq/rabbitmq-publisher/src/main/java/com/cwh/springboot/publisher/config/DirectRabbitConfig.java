package com.cwh.springboot.publisher.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**设置直连型交换机
 * 设置队列
 * 设置交换机
 * 设置将队列和交换机绑定
 * @author cwh
 * @date 2021/6/23 10:17
 */
@Configuration
public class DirectRabbitConfig {

    /**
     * 设置队列
     * Queue(name,durable,exclusive,autoDelete)
     * name: 队列名称
     * durable: 是否持久化，即是否会被存储到磁盘上，当消息代理重启时仍存在，默认为true
     * exclusive：只能被当前创建的连接使用，而且当连接关闭队列后立即删除，默认为false
     * autoDelete: 是否自动删除，当没有生产者或者消费者使用此队列时自动删除,默认为false
     * @return
     */
    @Bean
    public Queue directQueue(){

        return new Queue("DirectQueue",true);
    }

    public Queue testQueue(){
        return  new Queue("TestQueue");
    }
    /**
     * 设置交换机
     * DirectExchange(name,durable,autoDelete)
     * @return
     */
    @Bean
    public DirectExchange directExchange(){

        return new DirectExchange("DirectExchange");
    }

    /**
     * 设置binding，即路由规则
     * bind(Queue()).to(Exchange()).with(routingkey)
     * @return
     */
    @Bean
    public Binding bindDirect(){

        return BindingBuilder.bind(directQueue()).to(directExchange()).with("DirectRouting");
    }

//    设置不绑定的exchange
    @Bean
    public DirectExchange testExchange(){
        return new DirectExchange("TestExchange");
    }

    @Bean
    public DirectExchange testExchange2(){
        return  new DirectExchange("TestExchange2");
    }

    @Bean
    public Binding bindTestDirect(){
        return BindingBuilder.bind(testQueue()).to(testExchange2()).with("TestRouting");
    }
}
