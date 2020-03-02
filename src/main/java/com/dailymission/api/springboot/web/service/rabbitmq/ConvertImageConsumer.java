package com.dailymission.api.springboot.web.service.rabbitmq;

import com.dailymission.api.springboot.web.dto.rabbitmq.MessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RabbitListener(queues = "q.convert.png")
public class ConvertImageConsumer {

    /**
     * 전달해온 이미지의 형식이 jpg/jpeg/bmp/gif가 아닐경우에 error
     */
    @RabbitHandler
    @SendTo("x.thumbnail/")
    public MessageDto handleInvoiceCreated(MessageDto message){

        String extension = message.getExtension();
        if(!extension.equals(".jpg") || !extension.equals(".jpeg") || !extension.equals(".bmp") || !extension.equals(".gif")){
            throw new IllegalArgumentException("저장가능 이미지는 jpg/jpeg/bmp/gif 입니다.");
        }

        return  message;
    }
}
