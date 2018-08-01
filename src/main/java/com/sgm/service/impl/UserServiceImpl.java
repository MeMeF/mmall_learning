package com.sgm.service.impl;

import com.sgm.common.Const;
import com.sgm.common.ServerResponse;
import com.sgm.common.TokenCache;
import com.sgm.dao.UserMapper;
import com.sgm.entity.User;
import com.sgm.service.IUserService;
import com.sgm.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if(resultCount == 0 ){
            return ServerResponse.createByErrorMessage("用户名不存在");
        }

        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username, md5Password);
        if(user == null){
            return ServerResponse.createByErrorMessage("密码错误");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登陆成功",user);
    }

    /**
     * 注册操作
     * @param user
     * @return
     */
    public ServerResponse<String> register(User user){
        ServerResponse validResult = this.checkValid(user.getUsername(),Const.USERNAME);
        //当用户名的用户名存在的时候，validResult.isSuccess（）为false
        if (!validResult.isSuccess()){
            return validResult;
        }
        validResult =this.checkValid(user.getEmail(),Const.EMAIL);
        //当注册的邮箱不存在的时候，进行下一个逻辑
        if (!validResult.isSuccess()){
            return validResult;
        }
        //因为是注册，设定权限等级是普通用户
        user.setRole(Const.Role.ROLE_CUSTOMER);
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if (resultCount==0){
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    /**
     * 进行校验操作
     * @param str
     * @param type
     * @return
     */
    public ServerResponse<String> checkValid(String str,String type){
        if (StringUtils.isNotBlank(type)){
            if (Const.USERNAME.equals(type)){
                int resultCount = userMapper.checkUsername(str);
                if (resultCount>0){
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            if (Const.EMAIL.equals(type)){
                int resultCount = userMapper.checkEmail(str);
                if (resultCount>0){
                    return ServerResponse.createByErrorMessage("邮箱已经存在");
                }
            }
        }else {
            ServerResponse.createByErrorMessage("参数校验错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    /**
     * 寻找密保问题
     * @param username
     * @return
     */
    public ServerResponse<String> selectQuestion(String username){
        ServerResponse validResponse = this.checkValid(username,Const.USERNAME);
        if (validResponse.isSuccess()){
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        String question = userMapper.selectQuestion(username);
        if (StringUtils.isNotBlank(question)){
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("尚未设置密保问题");
    }

    /**
     * 匹配密保问题与答案
     * @param username
     * @param question
     * @param answer
     * @return
     */
    public ServerResponse<String> checkAnswer(String username,String question,String answer){
        int resultCount = userMapper.checkAnwser(username,question,answer);
        if (resultCount>0){
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题的答案错误");
    }

    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        if (StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMessage("参数错误，请传入token");
        }
        ServerResponse validResult = checkValid(username,Const.USERNAME);
        if (validResult.isSuccess()){
            return ServerResponse.createByErrorMessage("用户名错误");
        }
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        if (StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMessage("token不存在或已失效");
        }
        if (StringUtils.equals(forgetToken,token)){
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int resultCount = userMapper.resetPassword(username,md5Password);
            if (resultCount>0){
                return ServerResponse.createBySuccessMessage("密码修改成功");
            }
        }
        return ServerResponse.createByErrorMessage("密码修改失败");
    }

    public ServerResponse<String> resetPassword(String passwordOld,String passwordNew,User user){
        //防止横向越权，通过用户id，来锁定该用户，修改其密码
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
        if (resultCount == 0){
            return ServerResponse.createByErrorMessage("旧密码错误");
        }
        String md5PasswordNew = MD5Util.MD5EncodeUtf8(passwordNew);
        user.setPassword(md5PasswordNew);
        resultCount = userMapper.updateByPrimaryKeySelective(user);
        if (resultCount>0){
            return ServerResponse.createBySuccessMessage("密码修改成功");
        }
        return ServerResponse.createByErrorMessage("密码修改失败");
    }

    public ServerResponse<User> update_info(User user){
        //username 不能被修改
        //需要验证eamil，新的邮箱不能和其他user的邮箱相同
        int resultCount =userMapper.checkEmailById(user.getEmail(),user.getId());
        if (resultCount>0){
            return ServerResponse.createByErrorMessage("email已经存在，请更换email后在进行尝试");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPassword(user.getPassword());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());
        int updateResponse = userMapper.updateByPrimaryKeySelective(updateUser);
        if (updateResponse>0){
            return ServerResponse.createBySuccess("成功修改用户信息",updateUser);
        }
        return ServerResponse.createByErrorMessage("修改用户信息失败");
    }

    public ServerResponse<User> getInfomation(int userId){
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null){
            return ServerResponse.createByErrorMessage("找不到当前用户对象");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }
}
