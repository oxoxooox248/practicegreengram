package practicegreengram.feed.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FeedSelVo {
    private int ifeed;
    private String contents;
    private String location;
    private String createdAt;
    private int writerIuser;
    private String writerNm;
    private String writerPic;
    private int isFav;
    private List<String> pics;
    private List<FeedCommentSelVo> comments;
    private int isMoreComment;
}
