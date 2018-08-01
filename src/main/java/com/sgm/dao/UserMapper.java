package com.sgm.dao;

import com.sgm.entity.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    int checkUsername(String username);

    int checkEmail(String email);

    User selectLogin(@Param("username") String username,@Param("password") String password);

    String selectQuestion(String username);

    int checkAnwser(@Param("username") String username,@Param("question") String question,@Param("answer") String answer);

    int resetPassword(@Param("username") String username,@Param("passwordNew") String passwordNew);

    int checkPassword(@Param("passwordOld") String passwordOld,@Param("userId") int userId);

    int checkEmailById(@Param("email")String email,@Param("userId")Integer userId);

}