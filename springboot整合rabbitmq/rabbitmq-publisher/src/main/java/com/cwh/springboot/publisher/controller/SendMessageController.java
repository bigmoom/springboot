package com.cwh.springboot.publisher.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author cwh
 * @date 2021/6/23 10:35
 */
@RestController
@RequestMapping("/send")
@Slf4j
public class SendMessageController {

//  RabbitTemplate 提供了发送和接受等等方法
    @Autowired
    private RabbitTemplate rabbitTemplate;


    @PostMapping("/direct")
    public String sendDirectMessage(@RequestParam("message")String message){
        String messageId = String.valueOf(UUID.randomUUID());
        String messageBody = message;
        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Map<String,Object> messageMap = new HashMap<String,Object>();
        messageMap.put("id",messageId);
        messageMap.put("body",messageBody);
        messageMap.put("createTime",createTime);

//        设置消息发送的交换机和路由key
        rabbitTemplate.convertAndSend("DirectExchange","DirectRouting",messageMap);

        return messageMap.toString();
    }

    @PostMapping("/topic/man")
    public String sendManMessage(@RequestParam("message")String message){
        String messageId = String.valueOf(UUID.randomUUID());
        String messageBody = message;
        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Map<String,Object> messageMap = new HashMap<String,Object>();
        messageMap.put("id",messageId);
        messageMap.put("body",messageBody);
        messageMap.put("createTime",createTime);

//        设置消息发送的交换机和路由key
        rabbitTemplate.convertAndSend("TopicExchange","Consumer.man",messageMap);

        return messageMap.toString();
    }

    @PostMapping("/topic/woman")
    public String sendWomanMessage(@RequestParam("message")String message){
        String messageId = String.valueOf(UUID.randomUUID());
        String messageBody = message;
        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Map<String,Object> messageMap = new HashMap<String,Object>();
        messageMap.put("id",messageId);
        messageMap.put("body",messageBody);
        messageMap.put("createTime",createTime);

//        设置消息发送的交换机和路由key
        rabbitTemplate.convertAndSend("TopicExchange","Consumer.woman",messageMap);

        return messageMap.toString();
    }

    @PostMapping("/fanout")
    public String sendFanoutMessage(@RequestParam("message")String message){
        String messageId = String.valueOf(UUID.randomUUID());
        String messageBody = message;
        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Map<String,Object> messageMap = new HashMap<String,Object>();
        messageMap.put("id",messageId);
        messageMap.put("body",messageBody);
        messageMap.put("createTime",createTime);

//        设置消息发送的交换机和路由key
        rabbitTemplate.convertAndSend("FanoutExchange",null,messageMap);

        return messageMap.toString();
    }

    @PostMapping("/test")
    public String testMessageAck(@RequestParam("message")String message){
        String messageId = String.valueOf(UUID.randomUUID());
        String messageBody = message;
        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Map<String,Object> messageMap = new HashMap<String,Object>();
        messageMap.put("id",messageId);
        messageMap.put("body",messageBody);
        messageMap.put("createTime",createTime);

//        设置不存在的exchange
//        rabbitTemplate.convertAndSend("Exchange","DirectRouting",messageMap);
//        rabbitTemplate.convertAndSend("TestExchange","DirectRouting",messageMap);
        rabbitTemplate.convertAndSend("TestExchange2","TestRouting",messageMap);

        return messageMap.toString();
    }
}
