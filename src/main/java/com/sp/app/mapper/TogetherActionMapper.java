package com.sp.app.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TogetherActionMapper {
    Map<String, Object> findPost(@Param("type") String type, @Param("id") long id);
    Map<String, Object> lockPost(@Param("type") String type, @Param("id") long id);
    int countLikes(@Param("type") String type, @Param("id") long id);
    int liked(@Param("type") String type, @Param("id") long id, @Param("member") long member);
    int addLike(@Param("type") String type, @Param("id") long id, @Param("member") long member);
    int removeLike(@Param("type") String type, @Param("id") long id, @Param("member") long member);
    void increaseShareViews(@Param("id") long id);
    Integer applicationStatus(@Param("type") String type, @Param("id") long id, @Param("member") long member);
    int applyShare(@Param("id") long id, @Param("member") long member);
    int acceptedCount(@Param("id") long id);
    int decideShare(@Param("id") long id, @Param("member") long member, @Param("status") int status);
    List<Map<String, Object>> shareApplicants(@Param("id") long id, @Param("owner") boolean owner, @Param("member") long member);
}
