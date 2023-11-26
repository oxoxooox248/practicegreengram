package practicegreengram.feed.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FeedCommentSelDto {
    private int ifeed;
    private int startIdx;
    private int rowCount;
}
