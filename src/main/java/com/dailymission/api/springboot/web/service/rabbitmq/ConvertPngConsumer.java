package com.dailymission.api.springboot.web.service.rabbitmq;

import com.dailymission.api.springboot.web.dto.rabbitmq.MessageDto;
import com.dailymission.api.springboot.web.repository.common.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@RabbitListener(queues = "q.convert.image")
public class ConvertPngConsumer {

    private final S3Uploader s3Uploader;

    /**
     * png -> jpeg로 변경 (용량 줄이기)
     * */
    @RabbitHandler
    @SendTo("x.thumbnail/")
    public MessageDto handleInvoiceCreated(MessageDto message){

//        String extension = message.getExtension();
//        if(!extension.equals(".png")){
//            throw new IllegalArgumentException("변환가능 이미지는 png 입니다.");
//        }

        s3Uploader.download(message);

        return  message;
    }
}
