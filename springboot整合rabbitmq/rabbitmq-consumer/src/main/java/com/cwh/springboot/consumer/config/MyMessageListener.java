package com.cwh.springboot.consumer.config;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.stereotype.Component;

/**
 * @author cwh
 * @date 2021/6/23 16:50
 */
@Component
public class MyMessageListener implements ChannelAwareMessageListener {

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
//       类似于消息id
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try{
//            对消息的自定义处理
            System.out.println("Message:"+message.toString());
            System.out.println("消息来自："+message.getMessageProperties().getConsumerQueue());
//          确认消息
//          第二个参数会是否开启批处理，true则表示一次性确认小于等于传入值的所有消息
            channel.basicAck(deliveryTag,true);

        }catch (Exception e){
            channel.basicReject(deliveryTag,false);
            e.printStackTrace();
        }
    }
}
