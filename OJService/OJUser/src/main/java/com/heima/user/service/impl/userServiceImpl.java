package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.blog.pojos.OjFans;
import com.heima.model.common.dtos.Bloglistwithstate;
import com.heima.model.common.dtos.LoginDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.OjUser;
import com.heima.user.mapper.FansMapper;
import com.heima.user.mapper.UserMapper;
import com.heima.user.service.UserService;
import com.heima.user.utils.UserContext;
import com.heima.utils.common.AppJwtUtil;
import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class userServiceImpl extends ServiceImpl<UserMapper, OjUser> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FansMapper fansMapper;
    @Override
    public OjUser getu(Long id) {
        LambdaQueryWrapper<OjUser> l = new LambdaQueryWrapper<>();
        l.eq(id != null, OjUser::getId, id);
        OjUser ojUser = userMapper.selectOne(l);
        return ojUser;
    }

    @Override
    public ResponseResult hf(Bloglistwithstate dto) {
        String uid = dto.getUid();
        int subscribeState = dto.getSubscribeState();
        Long user = UserContext.getUser();

        if(user==null)return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);



        LambdaQueryWrapper<OjFans>l = new LambdaQueryWrapper<>();
        l.eq(OjFans::getUid,uid).eq(OjFans::getFid,user);
        OjFans ojFans = fansMapper.selectOne(l);

        if(ojFans==null){
            OjFans of=new OjFans();
            of.setFid(user.toString());
            of.setUid(uid);
            fansMapper.insert(of);
        }

        else {
            fansMapper.deleteById(ojFans.getId());
        }

        UpdateWrapper<OjUser> uw=new UpdateWrapper<>();
        uw.eq("id",uid).setSql(true,"fans=fans+"+(2*subscribeState-1));
        update(uw);
        uw.clear();
        uw.eq("id",user).setSql(true,"subscribe=COALESCE(subscribe,0)+"+(2*subscribeState-1));
        update(uw);
        return ResponseResult.okResult(subscribeState==1?"关注成功":"取关成功");
    }

    @Override
    public ResponseResult cf(Long id) {
        Long me=UserContext.getUser();
        List<Long> cf = fansMapper.cf(id, me);
        List<OjUser> collect = cf.stream().map(i -> {
            return getu(i);
        }).collect(Collectors.toList());

        ResponseResult responseResult = ResponseResult.okResult(collect);
        return responseResult;
    }

    @Override
    public ResponseResult login(LoginDto dto) {

        if (org.apache.commons.lang.StringUtils.isNotBlank(dto.getEmail()) && org.apache.commons.lang.StringUtils.isNotBlank(dto.getPassword())) {
            LambdaQueryWrapper<OjUser> apUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
            apUserLambdaQueryWrapper.eq(OjUser::getEmail, dto.getEmail());
            log.info(dto.getEmail());
            OjUser one = this.getOne(apUserLambdaQueryWrapper);
            if (one == null)
                return ResponseResult.errorResult(AppHttpCodeEnum.AP_USER_DATA_NOT_EXIST, "用户信息不存在");
            else {
                String pswd = dto.getPassword();
                String salt = "sufeoj-salt";
                pswd = DigestUtils.md5DigestAsHex((pswd + salt).getBytes());
                log.info(pswd + ":" + one.getPassword());
                if (!pswd.equals(one.getPassword()))
                    return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
            }
            String token = AppJwtUtil.getToken(one.getId().longValue());
            Map<String, Object> map = new HashMap<>();
            one.setPassword("");
            map.put("token", token);
            map.put("user", one);
            return ResponseResult.okResult(map);
        }

        return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN, "邮箱或密码错误");
    }

    @Override
    public ResponseResult reg(LoginDto dto) {
        if (StringUtils.isNotBlank(dto.getEmail()) && StringUtils.isNotBlank(dto.getPassword())) {

            LambdaQueryWrapper<OjUser> apUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
            apUserLambdaQueryWrapper.eq(OjUser::getEmail, dto.getEmail());

            OjUser one = this.getOne(apUserLambdaQueryWrapper);
            if (one != null) return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, "邮箱已注册");

            String pswd = dto.getPassword();
            String salt = "sufeoj-salt";
            pswd = DigestUtils.md5DigestAsHex((pswd + salt).getBytes());
            log.info(pswd);

            OjUser user = new OjUser();
            user.setNickName(dto.getNickName());
            user.setPassword(pswd);
            user.setEmail(dto.getEmail());
            user.setRole("普通用户");
            user.setRk(999999);
            user.setLevel(1);
            user.setFans(0L);
            user.setSubscribe(0);
            user.setUrl("http://192.168.128.130:9000/leadnews/2024/03/04/9e173a44-294e-4ca7-839d-62933171535e.jpg");

            userMapper.insert(user);

            String token = AppJwtUtil.getToken(user.getId().longValue());
            Map<String, Object> map = new HashMap<>();
            user.setPassword("");
            map.put("token", token);
            map.put("user", user);
            return ResponseResult.okResult(map);
        }
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, "信息不全");
    }

    @Override
    public ResponseResult upi(LoginDto dto) {

        Long id = UserContext.getUser();

        LambdaQueryWrapper<OjUser>l=new LambdaQueryWrapper<>();
        l.eq(OjUser::getId,id);

        OjUser one = getOne(l);
        if(one==null)return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"用户信息不存在");

        UpdateWrapper<OjUser>uw=new UpdateWrapper<>();
        uw.eq("id",id).set("school",dto.getSchool()).set("gender",dto.getGender()).set("location",dto.getLocation()).set("web",dto.getWeb());
        update(uw);

        return ResponseResult.okResult(one);
    }

    @Override
    public ResponseResult gu() {
        Long id = UserContext.getUser();

        LambdaQueryWrapper<OjUser>l=new LambdaQueryWrapper<>();
        l.eq(OjUser::getId,id);

        OjUser one = getOne(l);
        if(one==null)return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"用户信息不存在");
        return ResponseResult.okResult(one);
    }

}
