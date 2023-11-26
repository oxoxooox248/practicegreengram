package practicegreengram.feed;

import org.apache.ibatis.annotations.Mapper;
import practicegreengram.feed.model.*;

import java.util.List;

@Mapper
public interface FeedMapper {
    int insFeed(FeedInsProcDto dto);
    List<FeedSelVo> selFeedAll(FeedSelDto dto);
    Integer selFeed(FeedDelDto dto);
    int delFeed(int ifeed);
    int insFeedPics(FeedInsProcDto dto);
    List<FeedPicsVo> selFeedPics(List<Integer> ifeedList);
    int delPicsByIfeed(int ifeed);
    int insFeedFav(FeedFavDto dto);
    int delFeedFav(FeedFavDto dto);
    int delFavByIfeed(int feed);
    int insFeedComment(FeedCommentInsProcDto dto);
    List<FeedCommentSelVo> selCommentAll(FeedCommentSelDto dto);
    int delFeedComment(FeedCommentDelDto dto);
    int delCommentByIfeed(int ifeed);

}
