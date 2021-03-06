package com.dailymission.api.springboot.web.service.image;

import com.dailymission.api.springboot.web.dto.rabbitmq.MessageDto;
import com.dailymission.api.springboot.web.repository.common.S3Util;
import com.dailymission.api.springboot.web.repository.mission.Mission;
import com.dailymission.api.springboot.web.repository.mission.MissionRepository;
import com.dailymission.api.springboot.web.repository.post.Post;
import com.dailymission.api.springboot.web.repository.post.PostRepository;
import com.dailymission.api.springboot.web.repository.user.User;
import com.dailymission.api.springboot.web.repository.user.UserRepository;
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
    private final UserRepository userRepository;

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

        // resize 300x350 (Hot/New/All)
        File resize_300_350 = s3Util.resize(messageDto, image, 300, 350, "W");
        String resize_300_350_url =  s3Util.upload(resize_300_350, messageDto.getDirName()).getImageUrl();

//        // resize 300x480 (All)
//        File resize_300_480 = s3Util.resize(messageDto, image, 300, 480, "W");
//        String resize_300_480_url =  s3Util.upload(resize_300_480, messageDto.getDirName()).getImageUrl();

        // resize 400x600 (디테일)
        File resize_400_600 = s3Util.resize(messageDto, image, 400, 600, "W");
        String resize_400_600_url =  s3Util.upload(resize_400_600, messageDto.getDirName()).getImageUrl();

        // remove download file
        s3Util.removeNewFile(download);

        Optional<Mission> optional = missionRepository.findById(messageDto.getMissionId());
        if (optional.isPresent()) {
            // get mission object
            Mission mission = optional.get();

            // update thumbnail url
            mission.updateThumbnailHot(resize_300_350_url);
            mission.updateThumbnailNew(resize_300_350_url);
            mission.updateThumbnailAll(resize_300_350_url);
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

        // resize 150x340 (My)
        File resize_150_340 = s3Util.resize(messageDto, image, 150, 340, "W");
        String resize_150_340_url =  s3Util.upload(resize_150_340, messageDto.getDirName()).getImageUrl();

        // resize_300_360 (Mission)
        File resize_300_360 = s3Util.resize(messageDto, image, 300, 360, "W");
        String resize_300_360_url =  s3Util.upload(resize_300_360, messageDto.getDirName()).getImageUrl();


        // remove download file
        s3Util.removeNewFile(download);

        Optional<Post> optional = postRepository.findById(messageDto.getPostId());
        if (optional.isPresent()) {
            // get post object
            Post post = optional.get();

            // update thumbnail url
            post.updateThumbnail(resize_400_880_url);
            post.updateThumbnailMy(resize_150_340_url);
            post.updateThumbnailMission(resize_300_360_url);

            postRepository.save(post);
        }
    }

    public void resizeUser(MessageDto messageDto) throws IOException {
        // download file
        File download = s3Util.download(messageDto);
        Image image = ImageIO.read(download);

        // resize 40x40 (Profile)
        File resize_40_40 = s3Util.resize(messageDto, image, 40, 40, "W");
        String resize_40_40_url =  s3Util.upload(resize_40_40, messageDto.getDirName()).getImageUrl();

        // resize 150x150 (User Info)
        File resize_150_150 = s3Util.resize(messageDto, image, 150, 150, "W");
        String resize_150_150_url =  s3Util.upload(resize_150_150, messageDto.getDirName()).getImageUrl();

        // remove download file
        s3Util.removeNewFile(download);

        Optional<User> optional = userRepository.findById(messageDto.getUserId());
        if (optional.isPresent()) {
            // get user object
            User user = optional.get();

            // update thumbnail url
            user.updateThumbnail(resize_40_40_url);
            user.updateThumbnailUserInfo(resize_150_150_url);

            userRepository.save(user);
        }
    }
}
