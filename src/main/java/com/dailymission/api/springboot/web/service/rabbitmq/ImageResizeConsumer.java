package com.dailymission.api.springboot.web.service.rabbitmq;

import com.dailymission.api.springboot.web.dto.rabbitmq.MessageDto;
import com.dailymission.api.springboot.web.service.image.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageResizeConsumer {

    private final ImageService imageService;

    @RabbitListener(queues = "q.resize.mission")
    @Caching(evict = {
            // Home 미션 List
            @CacheEvict(value = "missionLists", key = "'home'"),
            // All 미션 List
            @CacheEvict(value = "missionLists", key = "'all'"),
            // Hot 미션 List
            @CacheEvict(value = "missionLists", key = "'hot'"),
            // 미션 정보 (detail)
            @CacheEvict(value = "missions", key = "#message.missionId")
    })
    public void resizeMission(MessageDto message) throws IOException {

          System.out.println(">>>>>>>>>>>>> resize mission");

          // resize mission
          imageService.resizeMission(message);

    }


    @RabbitListener(queues = "q.resize.post")
    @Caching(evict = {
            // 전체 포스트 List
            @CacheEvict(value = "postLists", key = "'all'"),
            // 유저별 포스트 List
            @CacheEvict(value = "postLists", key = "'user-'+ #message.userId"),
            // 미션별 포스트 List
            @CacheEvict(value = "postLists", key = "'mission-' + #message.missionId"),
            // 포스트 정보 (detail)
            @CacheEvict(value = "posts" , key = "#message.postId")
    })
    public void resizePost(MessageDto message) throws IOException {

        System.out.println(">>>>>>>>>>>>> resize post");

        // resize mission
        imageService.resizePost(message);
    }
}
