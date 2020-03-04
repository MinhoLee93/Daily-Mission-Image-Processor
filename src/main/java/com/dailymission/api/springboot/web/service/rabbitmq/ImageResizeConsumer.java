package com.dailymission.api.springboot.web.service.rabbitmq;

import com.dailymission.api.springboot.web.dto.rabbitmq.MessageDto;
import com.dailymission.api.springboot.web.repository.common.S3Util;
import com.dailymission.api.springboot.web.service.image.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageResizeConsumer {

    private final ImageService imageService;

    @RabbitListener(queues = "q.resize.mission")
    public void resize300400(MessageDto message) throws IOException {

          System.out.println(">>>>>>>>>>>>> resize mission");

          // resize mission
          imageService.resizeMission(message);

    }

    @RabbitListener(queues = "q.resize.post")
    public void resize400880(MessageDto message) throws IOException {

        System.out.println(">>>>>>>>>>>>> resize post");

        // resize mission
        imageService.resizePost(message);
    }
}
