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
        //select 실행
        List<Integer> ifeedList= new ArrayList();
        //select 실행 시 list안에 있는 각 피드들의 ifeed값을 담을 lfeedList 생성
        Map<Integer, FeedSelVo> feedMap= new HashMap();
        // ifeed값(키)에 대응되는 FeedSelVo(값value)를 담는 Map인 feedMap 생성
        for(FeedSelVo vo: list){
            vo.setPics(new ArrayList());
            //나중에 사진들을 담기 위해 vo객체에 있는 pics에 새로운 ArrayList 생성
            ifeedList.add(vo.getIfeed());
            //select 실행할 때 얻은 ifeed값들을 ifeedList에 담는다
            feedMap.put(vo.getIfeed(), vo);
            //select 실행할 때 얻은 ifeed값들을 key에, vo는 value에 담는다.
            List<FeedCommentSelVo> comments= mapper.selCommentAll(FeedCommentSelDto.builder()
                    .ifeed(vo.getIfeed()).startIdx(0).rowCount(4).build());
            /*피드들의 댓글 4개의 정보(FeedCommentSelVo 확인)를 comments에 담는다.
            반복문을 돌면서 해당 페이지의 모든 피드들에서 실행
            3개 이하면 아래의 if문이 실행되지 않는다.*/
            if(comments.size()==4){//댓글이 4개 이상이면
                vo.setIsMoreComment(1);//vo에 있는 isMoreComment에 1을 준다.(댓글 더보기 생김)
                comments.remove(comments.size()-1);
                //위에서 comments에 담았던 댓글 중 제일 최근 거를 하나 지운다.
            }
            vo.setComments(comments);
            /*comments(댓글들)를 vo에 담는다.
            해당 피드의 댓글이 4개 이상이었다면 if문에서 최근 거 하나가 지워져서 comments의 크기는 3이다.*/
        }
        if(ifeedList.size()>0){//해당 페이지의 댓글이 1개 이상이면
            List<FeedPicsVo> feedPicsList = mapper.selFeedPics(ifeedList);
            /*select가 실행되면서 ifeed에 해당하는 pic이 FeedPicsVo에 담긴다.(FeedPicsVo 확인)
            해당 ifeed(아래 예시에서 ifeed=1)에 사진이 두 개 이상이면
            ex)FeedPicsVo(ifeed,pic) >> FeedPicsVo(1,pic#1),FeedPicsVo(1,pic#2)...
            */
            for(FeedPicsVo vo: feedPicsList){
                FeedSelVo feedVo = feedMap.get(vo.getIfeed());
                //해당 ifeed(key)에 대한 FeedSelVo(value)의 주소값을 feedVo에 담는다.
                List<String> strPicsList = feedVo.getPics();
                //해당 피드의 사진들을 담은 리스트(vo.getPics)의 주소값을 strPicsList에 담는다.
                strPicsList.add(vo.getPic());
                //해당 피드의 해당 사진을 한 장씩 반복문을 통해 List(strPicsList)에 추가
            }
        }
        return list;// 다 담은 List<FeedSelVo> list를 리턴
    }
    //피드 삭제
    public ResVo delFeed(FeedDelDto dto){
        Integer targetIfeed= mapper.selFeed(dto);
        //해당 댓글이 로그인한 유저가 쓴 댓글이 맞는지 확인
        if(targetIfeed==null){return new ResVo(0);}
        //댓글이 없거나 그 유저가 쓴글이 아니면 NULL > result에 0을 담은 ResVo객체를 리턴
        int affectedComment= mapper.delCommentByIfeed(targetIfeed);
        //먼저 해당 댓글의 댓글 삭제
        int affectedPics= mapper.delPicsByIfeed(targetIfeed);
        //해당 댓글의 사진들 삭제
        int affectedFav= mapper.delFavByIfeed(targetIfeed);
        //해당 댓글의 좋아요 삭제
        int affectedFeed= mapper.delFeed(targetIfeed);//피드 삭제
        return new ResVo(1);//삭제 완료 시 result에 1을 담은 ResVo객체를 리턴
    }
    //좋아요 토글 처리
    public ResVo toggleFeedFav(FeedFavDto dto){
        int affectedFavCnt= mapper.delFeedFav(dto);//좋아요 삭제를 해본다.
        if(affectedFavCnt==1){return new ResVo(0);}
        //위의 delete가 실행되면 result에 0을 담은 ResVo객체를 리턴
        affectedFavCnt= mapper.insFeedFav(dto);
        return new ResVo(1);//result에 1을 담은 ResVo객체를 리턴
    }
    //댓글 작성
    public ResVo postComment(FeedCommentInsDto dto){
        FeedCommentInsProcDto pDto= new FeedCommentInsProcDto(dto);
        //insert가 실행되면서 생기는 ifeedComment를 담기위해 pDto 생성(FeedCommentInsProcDto확인)
        int affectedRows= mapper.insFeedComment(pDto);
        return new ResVo(pDto.getIfeedComment());
        //실행 완료시 ResVo에 ifeedComment를 담아서 리턴
    }
    //댓글 더보기
    public List<FeedCommentSelVo> getCommentAll(int ifeed){
        return mapper.selCommentAll(FeedCommentSelDto.builder().
                ifeed(ifeed).startIdx(4).rowCount(9999).build());
        //댓글 더보기 클릭이 indexNumber 4부터 9999개의 댓글을 select
        //builder를 이용하여 select문을 실행할 parameter(FeedCommentSelDto) 생성
    }
    //댓글 삭제
    public ResVo delComment(FeedCommentDelDto dto){
        int affectedRows= mapper.delFeedComment(dto);
        //로그인한 유저가 실행하는 선택한 댓글 삭제(FeedCommentDelDto 확인)
        return new ResVo(affectedRows);//성공: 1, 실패: 0
    }
}
