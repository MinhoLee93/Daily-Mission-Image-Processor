package com.dailymission.api.springboot.web.service.image;

import com.dailymission.api.springboot.web.dto.rabbitmq.MessageDto;
import com.dailymission.api.springboot.web.repository.common.S3Util;
import com.dailymission.api.springboot.web.repository.mission.Mission;
import com.dailymission.api.springboot.web.repository.mission.MissionRepository;
import com.dailymission.api.springboot.web.repository.post.Post;
import com.dailymission.api.springboot.web.repository.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ImageService {

    private final S3Util s3Util;
    private final MissionRepository missionRepository;
    private final PostRepository postRepository;

    public String genDir(){
        // get calendar instance
        Calendar cal = Calendar.getInstance();

        // get calc Path (year/month/day)
        String yearPath = "/" + cal.get(Calendar.YEAR);
        String monthPath = yearPath + "/" + new DecimalFormat("00").format(cal.get(Calendar.MONTH));
        String datePath = monthPath + "/" + new DecimalFormat("00").format(cal.get(Calendar.DATE));

        // return final directory path
        return datePath;
    }

    /**
     * /1일1알고리즘/2020/03/28/
     * */
    public MessageDto uploadPostS3(MultipartFile multipartFile, String dirName) throws IOException {
        return s3Util.upload(multipartFile, dirName + genDir());
    }

    /**
     * /1일1알고리즘/
     * */
    public MessageDto uploadMissionS3(MultipartFile multipartFile, String dirName) throws IOException {
        return s3Util.upload(multipartFile, dirName);
    }



    public void resizeMission(MessageDto messageDto) throws IOException {
        // download file
        File download = s3Util.download(messageDto);
        Image image = ImageIO.read(download);

        // resize 300x400 (홈)
        File resize_300_400 = s3Util.resize(messageDto, image, 300, 400, "W");
        String resize_300_400_url =  s3Util.upload(resize_300_400, messageDto.getDirName()).getImageUrl();

        // resize 300x480 (전체/핫)
        File resize_300_480 = s3Util.resize(messageDto, image, 300, 480, "W");
        String resize_300_480_url =  s3Util.upload(resize_300_480, messageDto.getDirName()).getImageUrl();

        // resize 400x600 (디테일)
        File resize_400_600 = s3Util.resize(messageDto, image, 400, 600, "W");
        String resize_400_600_url =  s3Util.upload(resize_400_600, messageDto.getDirName()).getImageUrl();

        // remove download file
        s3Util.removeNewFile(download);

        Optional<Mission> optional = missionRepository.findById(messageDto.getId());
        if (optional.isPresent()) {
            // get mission object
            Mission mission = optional.get();

            // update thumbnail url
            mission.updateThumbnailHome(resize_300_400_url);
            mission.updateThumbnailAll(resize_300_480_url );
            mission.updateThumbnailHot(resize_300_480_url);
            mission.updateThumbnailDetail(resize_400_600_url);

            missionRepository.save(mission);
        }
    }

    public void resizePost(MessageDto messageDto) throws IOException {
        // download file
        File download = s3Util.download(messageDto);
        Image image = ImageIO.read(download);

        // resize 400x880 (리스트)
        File resize_400_880 = s3Util.resize(messageDto, image, 400, 880, "W");
        String resize_400_880_url =  s3Util.upload(resize_400_880, messageDto.getDirName()).getImageUrl();

        // remove download file
        s3Util.removeNewFile(download);

        Optional<Post> optional = postRepository.findById(messageDto.getId());
        if (optional.isPresent()) {
            // get mission object
            Post post = optional.get();

            // update thumbnail url
            post.updateThumbnail(resize_400_880_url);

            postRepository.save(post);
        }
    }
}
