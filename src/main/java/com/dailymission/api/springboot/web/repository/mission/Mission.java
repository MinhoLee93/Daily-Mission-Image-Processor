package com.dailymission.api.springboot.web.repository.mission;

import com.dailymission.api.springboot.web.repository.common.BaseTimeEntity;
import com.dailymission.api.springboot.web.repository.mission.rule.MissionRule;
import com.dailymission.api.springboot.web.repository.participant.Participant;
import com.dailymission.api.springboot.web.repository.post.Post;
import com.dailymission.api.springboot.web.repository.user.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Entity
public class Mission extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "MISSION_RULE_ID")
    @JsonManagedReference
    private MissionRule missionRule;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user;

    @OneToMany(mappedBy = "mission")
    @JsonBackReference
    private List<Participant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "mission")
    private List<Post> posts = new ArrayList<>();

    @Column(name = "TITLE", nullable = false, length = 15)
    @Size(min = 1, max = 20)
    private String title;

    @Column(name = "CONTENT", nullable = false, length = 50)
    @Size(min = 10, max = 50)
    private String content;

    @Column(name = "ORIGINAL_FILE_NAME", nullable = false)
    private String originalFileName;

    @Column(name = "FILE_EXTENSION", nullable = false)
    private String fileExtension;

    @Column(name="IMAGE_URL", nullable = false)
    private String imageUrl;

    // 썸네일 (Hot)
    @Column(name="THUMBNAIL_URL_HOT", nullable = false)
    private String thumbnailUrlHot;

    // 썸네일 (New)
    @Column(name="THUMBNAIL_URL_New", nullable = false)
    private String thumbnailUrlNew;

    // 썸네일 (All)
    @Column(name="THUMBNAIL_URL_ALL", nullable = false)
    private String thumbnailUrlAll;

    // 썸네일 (디테일)
    @Column(name="THUMBNAIL_URL_DETAIL", nullable = false)
    private String thumbnailUrlDetail;

    @Column(name = "CREDENTIAL", nullable = false)
    private String credential;

    @Column(name = "START_DATE", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Column(name = "END_DATE", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Column(name = "ENDED", nullable = false)
    private boolean ended;

    @Column(name = "DELETED", nullable = false)
    private boolean deleted;

    @Builder
    public Mission(MissionRule missionRule, User user, String title, String content,
                   String originalFileName, String fileExtension,  String imageUrl, LocalDate startDate, LocalDate endDate){
        this.missionRule = missionRule;
        this.user = user;
        this.title = title;
        this.content = content;
        this.originalFileName = originalFileName;
        this.fileExtension = fileExtension;
        this.startDate = startDate;
        this.endDate = endDate;


        // credential
        this.credential = createCredential();

        // s3
        this.imageUrl = imageUrl;
        this.thumbnailUrlNew = imageUrl;
        this.thumbnailUrlAll = imageUrl;
        this.thumbnailUrlHot = imageUrl;
        this.thumbnailUrlDetail = imageUrl;

        this.ended = false;
        this.deleted = false;
    }

    // 비밀번호 생성
    private String createCredential(){
        String genId = UUID.randomUUID().toString();
        genId = genId.replace("-","");

        return genId;
    }

    // 비밀번호 확인
    public boolean checkCredential(String credential){
        if(!this.credential.equals(credential)){
            return false;
        }else{
            return true;
        }
    }

    // 종료 및 삭제여부 확인 (Participant)
    public boolean checkStatus(){
        if(this.ended || this.deleted){
            return false;
        }else{
            return true;
        }
    }

    // 시작날짜 확인 (Participant Create)
    public boolean checkStartDate(LocalDate date){
        if(date.isAfter(this.startDate)){
            return false;
        }else{
            return true;
        }
    }

    // 종료날짜 확인 (Mission Create)
    public boolean checkEndDate(LocalDate date){
        if(date.isAfter(this.endDate)){
            return false;
        }else{
            return true;
        }
    }

    // 이미지 변경
    public void updateImage(String imageUrl){
        // 이미지
        this.imageUrl = imageUrl;
        // 썸네일 -> 재생성
        this.thumbnailUrlNew = imageUrl;
        this.thumbnailUrlAll = imageUrl;
        this.thumbnailUrlHot = imageUrl;
        this.thumbnailUrlDetail = imageUrl;
    }

    // 썸네일 업데이트 (홈)
    public void updateThumbnailNew(String thumbnailUrlNew){
        this.thumbnailUrlNew = thumbnailUrlNew;
    }

    // 썸네일 업데이트 (ALL)
    public void updateThumbnailAll(String thumbnailUrlAll){
        this.thumbnailUrlAll = thumbnailUrlAll;
    }

    // 썸네일 업데이트 (Hot)
    public void updateThumbnailHot(String thumbnailUrlHot){
        this.thumbnailUrlHot = thumbnailUrlHot;
    }

    // 썸네일 업데이트 (Detail)
    public void updateThumbnailDetail(String thumbnailUrlDetail){
        this.thumbnailUrlDetail = thumbnailUrlDetail;
    }

    // 업데이트
    public void update(MissionRule missionRule, String title, String content, LocalDate startDate, LocalDate endDate) {
        this.missionRule = missionRule;
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // 삭제
    public void delete(User user){
        if(this.user.getId() != user.getId()){
            throw new IllegalArgumentException("허용되지 않은 유저입니다.");
        }

        if(this.deleted){
            throw new IllegalArgumentException("이미 삭제된 미션입니다.");
        }

        this.deleted = true;
        this.ended = true;
    }

    // 종료
    public void end(){
        this.ended = true;
    }

}
