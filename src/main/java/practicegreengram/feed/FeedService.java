package practicegreengram.feed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import practicegreengram.ResVo;
import practicegreengram.feed.model.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FeedService {
    private final FeedMapper mapper;

    //피드 작성
    public ResVo postFeed(FeedInsDto dto){
        if(dto.getPics().size()==0){return new ResVo(2);}
        //입력받은 사진이 없으면 ResVo 객체의 result에 2를 담아 리턴
        FeedInsProcDto pDto= FeedInsProcDto.builder().
                iuser(dto.getIuser()).contents(dto.getContents()).
                location(dto.getLocation()).pics(dto.getPics()).build();
        /*ifeed와 pics를 담을 수 있는 FeedInsProcDto를 builder()를 통해서 객체 생성
        ifeed는 첫번째 insert 때 생성되는 ifeed값을 useGeneratedKeys="true" keyProperty="ifeed"
        를 이용해서 실행 완료 시 피드 pk를 FeedInsProcDto의 ifeed에 담는다.
         */
        int affectedFeedCnt= mapper.insFeed(pDto);//첫 번째 insert: t_feed에 insert
        if(affectedFeedCnt==0||pDto.getIfeed()==0){
            return new ResVo(0);
            //피드가 insert 안되거나 피드pk를 못 받았으면 ResVo 객체의 result에 0을 담아 리턴
        }
        int affectedPicCnt= mapper.insFeedPics(pDto);//두 번째 insert: 피드 작성 시 t_feed_pics에 사진 등록
        //위에서 얻은 ifeed값과 dto로 받았던 pics을 이용하여 실행
        if(affectedPicCnt!=dto.getPics().size()){
            return new ResVo(3);
            //사진이 제대로 등록이 안 됐으면 result: 3
        }
        return new ResVo(pDto.getIfeed());//insert 성공 시 피드 pk를 result에 담아 리턴
    }
    //피드 리스트
    public List<FeedSelVo> getFeedAll(FeedSelDto dto){
        List<FeedSelVo> list= mapper.selFeedAll(dto);
        List<Integer> ifeedList= new ArrayList();
        Map<Integer, FeedSelVo> feedMap= new HashMap();
        for(FeedSelVo vo: list){
            vo.setPics(new ArrayList());
            ifeedList.add(vo.getIfeed());
            feedMap.put(vo.getIfeed(), vo);
            List<FeedCommentSelVo> comments= mapper.selCommentAll(FeedCommentSelDto.builder()
                    .ifeed(vo.getIfeed()).startIdx(0).rowCount(4).build());
            if(comments.size()==4){
                vo.setIsMoreComment(1);
                comments.remove(comments.size()-1);
            }
            vo.setComments(comments);
        }
        if(ifeedList.size()>0){
            List<FeedPicsVo> feedPicsList = mapper.selFeedPics(ifeedList);
            for(FeedPicsVo vo: feedPicsList){
                FeedSelVo feedVo = feedMap.get(vo.getIfeed());
                List<String> strPicsList = feedVo.getPics();
                strPicsList.add(vo.getPic());
            }
        }
        return list;
    }
    //피드 삭제
    public ResVo delFeed(FeedDelDto dto){
        Integer targetIfeed= mapper.selFeed(dto);
        if(targetIfeed==null){return new ResVo(0);}
        int affectedComment= mapper.delCommentByIfeed(targetIfeed);
        int affectedPics= mapper.delPicsByIfeed(targetIfeed);
        int affectedFav= mapper.delFavByIfeed(targetIfeed);
        int affectedFeed= mapper.delFeed(targetIfeed);
        return new ResVo(1);
    }
    //좋아요 토글 처리
    public ResVo toggleFeedFav(FeedFavDto dto){
        int affectedFavCnt= mapper.delFeedFav(dto);
        if(affectedFavCnt==1){return new ResVo(0);}
        affectedFavCnt= mapper.insFeedFav(dto);
        return new ResVo(1);
    }
    //댓글 작성
    public ResVo postComment(FeedCommentInsDto dto){
        FeedCommentInsProcDto pDto= new FeedCommentInsProcDto(dto);
        int affectedRows= mapper.insFeedComment(pDto);
        return new ResVo(pDto.getIfeedComment());
    }
    //댓글 더보기
    public List<FeedCommentSelVo> getCommentAll(int ifeed){
        return mapper.selCommentAll(FeedCommentSelDto.builder().
                ifeed(ifeed).startIdx(4).rowCount(9999).build());
    }
    //댓글 삭제
    public ResVo delComment(FeedCommentDelDto dto){
        int affectedRows= mapper.delFeedComment(dto);
        return new ResVo(affectedRows);
    }
}
