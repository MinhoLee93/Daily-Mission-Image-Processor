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

import java.io.*;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3Uploader {
    private final AmazonS3 amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String genUUID(){
        String genId = UUID.randomUUID().toString();
        genId = genId.replace("-","");

        return genId;
    }

    /**
     * dirName / fileName / keyName
     * */
    public void download(MessageDto message){
        try {

            // s3 object
            S3Object o = amazonS3Client.getObject(bucket, message.getKeyName());
            S3ObjectInputStream s3is = o.getObjectContent();

            // message content
            String extension = message.getExtension();
            String keyName = message.getKeyName();
            String dirName = message.getDirName();
            String fileName = message.getFileName();

            // convert png -> jpg
            if(extension.equals(".png")){
                int index = fileName.lastIndexOf(".");
                fileName = fileName.substring(0, index) + ".jpg";
                System.out.println(">>>>>>>>>>>>> converted to : " + fileName);
            }

            // file
            File download = new File(fileName);

            // gzip
            Deflater df = new Deflater(Deflater.BEST_COMPRESSION, true);
            GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(download)){
                {
                    def.setLevel(Deflater.BEST_COMPRESSION);
                }
            };

            // write file stream
            BufferedOutputStream bos = new BufferedOutputStream(gos);
            byte[] read_buf = new byte[1024];
            int read_len = 0;
            while ((read_len = s3is.read(read_buf)) > 0) {
                bos.write(read_buf, 0, read_len);
            }

            // close stream
            s3is.close();
            bos.close();

            // 변경파일 재 업로드
            upload(download, dirName);

            // 기존 파일 삭제

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
    }

    // multipart
    public MessageDto upload(MultipartFile multipartFile, String dirName) throws IOException {
        File uploadFile = convert(multipartFile)
                .orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File로 전환이 실패했습니다."));

        return upload(uploadFile, dirName);
    }


    private MessageDto upload(File uploadFile, String dirName) {
        String fileName = dirName + "/" + uploadFile.getName();
        String uploadImageUrl = putS3(uploadFile, fileName);

        // delete local file
        // removeNewFile(uploadFile);

        return MessageDto.builder()
                .keyName(fileName)
                .imageUrl(uploadImageUrl)
                .build();
    }

    private String putS3(File uploadFile, String fileName) {
        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile).withCannedAcl(CannedAccessControlList.PublicRead));
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            log.info("파일이 삭제되었습니다.");
        } else {
            log.info("파일이 삭제되지 못했습니다.");
        }
    }

    private Optional<File> convert(MultipartFile file) throws IOException {
        File convertFile = new File(genUUID()+ "_" + file.getOriginalFilename());
        if(convertFile.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            }
            return Optional.of(convertFile);
        }

        return Optional.empty();
    }
}
