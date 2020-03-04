package com.dailymission.api.springboot.web.repository.common;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.dailymission.api.springboot.web.dto.rabbitmq.MessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3Util {
    private final AmazonS3 amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String genSerialNumber(){
        LocalDateTime now = LocalDateTime.now();
        String serial = "" +  new DecimalFormat("0000").format(now.getYear())
                + new DecimalFormat("00").format(now.getMonthValue())
                + new DecimalFormat("00").format(now.getDayOfMonth())
                + new DecimalFormat("00").format(now.getHour())
                + new DecimalFormat("00").format(now.getMinute());

        return serial;
    }

    /**
     * dirName / fileName / keyName
     * */
    public File download(MessageDto message){
        try {

            // s3 object
            S3Object o = amazonS3Client.getObject(bucket, message.getKeyName());
            S3ObjectInputStream s3is = o.getObjectContent();

            // message content
            String originalFileName = message.getOriginalFileName();

            // file
            File download = new File(genSerialNumber()+ "_" + originalFileName);

            // write file stream
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(download));
            byte[] read_buf = new byte[1024];
            int read_len = 0;
            while ((read_len = s3is.read(read_buf)) > 0) {
                bos.write(read_buf, 0, read_len);
            }

            // close stream
            s3is.close();
            bos.close();

            // 다운로드 받은 file
            return download;

        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        return null;
    }

    // resize (position / W : 넓이 중심, H 높이중심
    public File resize(MessageDto message,Image image, int newHeight, int newWidth, String position){
            String format = message.getExtension().substring(1);
            String postFix = "_" + newHeight + "_" + newWidth;
            int imageHeight;
            int imageWidth;
            double ratio;
            int h;
            int w;

            try{
                imageHeight = image.getHeight(null);
                imageWidth = image.getWidth(null);

                if(position.equals("W")){
                    ratio = (double) newWidth / (double) imageWidth;
                    h = (int) (imageHeight * ratio);
                    w = (int) (imageWidth * ratio);
                }else {
                    // "H"
                    ratio = (double) newHeight / (double) imageHeight;
                    h = (int) (imageHeight * ratio);
                    w = (int) (imageWidth * ratio);
                }

                // resize
                Image resizeImage = image.getScaledInstance(w, h, Image.SCALE_FAST);

                // new image
                BufferedImage newImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics g = newImage.getGraphics();
                g.drawImage(resizeImage, 0, 0 , null);
                g.dispose();

                // write
                String originalFileName = message.getOriginalFileName();
                String fileName = originalFileName.substring(0, originalFileName.lastIndexOf("."));
                File resizeFile = new File(genSerialNumber()+ "_" + fileName + postFix + message.getExtension());
                ImageIO.write(newImage, format, resizeFile);
                return resizeFile;

            }catch (Exception e){
                System.err.println(e.getMessage());
            }

            return null;
    }

    // multipart
    public MessageDto upload(MultipartFile multipartFile, String dirName) throws IOException {
        File uploadFile = convert(multipartFile)
                .orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File로 전환이 실패했습니다."));

        return upload(uploadFile, dirName);
    }

    // file
    public MessageDto upload(File uploadFile, String dirName) {

        String fileName = dirName + "/" + uploadFile.getName();
        String uploadImageUrl = putS3(uploadFile, fileName);

        // delete local file
        removeNewFile(uploadFile);

        return MessageDto.builder()
                .dirName(dirName)
                .fileName(uploadFile.getName())
                .keyName(fileName)
                .imageUrl(uploadImageUrl)
                .build();
    }

    public String putS3(File uploadFile, String fileName) {
        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile).withCannedAcl(CannedAccessControlList.PublicRead));
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    // remove
    public void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            log.info("파일이 삭제되었습니다.");
        } else {
            log.info("파일이 삭제되지 못했습니다.");
        }
    }

    public Optional<File> convert(MultipartFile file) throws IOException {
        File convertFile = new File(genSerialNumber()+ "_" + file.getOriginalFilename());
        if(convertFile.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            }
            return Optional.of(convertFile);
        }

        return Optional.empty();
    }
}
