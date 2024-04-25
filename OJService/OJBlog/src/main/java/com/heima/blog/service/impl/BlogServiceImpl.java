package com.heima.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.api.UserClient;
import com.heima.blog.mapper.BlogMapper;
import com.heima.blog.mapper.BlogUserMapper;
import com.heima.blog.mapper.FansMapper;
import com.heima.blog.service.BlogService;
import com.heima.blog.service.BlogUserService;
import com.heima.file.service.FileStorageService;
import com.heima.model.blog.pojos.OjBlogList;
import com.heima.model.blog.pojos.OjBlogUser;
import com.heima.model.blog.pojos.OjFans;
import com.heima.model.common.dtos.Bloglistwithstate;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.OjUser;
import com.heima.utils.common.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Slf4j
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, OjBlogList> implements BlogService {

    public static final String OJ_BLOG_EXCHANGE="oj.blog.direct";



    @Autowired
    private BlogUserMapper blogUserMapper;

    @Autowired
    private BlogUserService blogUserService;

    @Autowired
    private FansMapper fansMapper;

    @Resource
    public UserClient userClient;

    @Override
    public ResponseResult bl(String type,Integer p,Integer ps) {

        Long user = UserContext.getUser();



        String t = "#"+ type;
        LambdaQueryWrapper<OjBlogList> l = new LambdaQueryWrapper<>();
        l.eq(!t.equals("#"), OjBlogList::getType, t).orderByAsc(OjBlogList::getTime);

        Page<OjBlogList>pa=new Page<>(p,ps);

        page(pa, l);

        List<OjBlogList> list = pa.getRecords();

        List<Bloglistwithstate> res = new ArrayList<>();


        list.stream().forEach(b -> {
            Bloglistwithstate bs = new Bloglistwithstate();

            BeanUtils.copyProperties(b, bs);

            String bid = b.getId();
            String uid = b.getUid();

            OjUser getuser = userClient.getuser(Long.valueOf(uid));
            bs.setUser(getuser);

            if (user == null) {
                bs.setLikeState(0);
                bs.setStarState(0);
            } else {

                LambdaQueryWrapper<OjBlogUser> ll = new LambdaQueryWrapper<>();
                ll.eq(user != null, OjBlogUser::getUid, user).eq(OjBlogUser::getBid, bid);
                OjBlogUser ojBlogUser = blogUserMapper.selectOne(ll);

                if (ojBlogUser != null) {
                    bs.setLikeState(ojBlogUser.getLikeState());
                    bs.setStarState(ojBlogUser.getStarState());
                } else {
                    bs.setLikeState(0);
                    bs.setStarState(0);
                }

            }

            if (user == null) {
                bs.setSubscribeState(0);
            } else {
                LambdaQueryWrapper<OjFans> l1 = new LambdaQueryWrapper<>();
                l1.eq(user != null, OjFans::getUid, uid).eq(OjFans::getFid, user);
                Integer i = fansMapper.selectCount(l1);

                if (i > 0) bs.setSubscribeState(1);
            }
            res.add(bs);
        });

        Page<Bloglistwithstate>np=new Page<>();
        BeanUtils.copyProperties(pa,np,"records");
        np.setRecords(res);
        return ResponseResult.okResult(np);
    }

    @Override
    public ResponseResult bo(Long id) {

        Long user = UserContext.getUser();
        LambdaQueryWrapper<OjBlogList> l = new LambdaQueryWrapper<>();
        l.eq(id != null, OjBlogList::getId, id);
        OjBlogList one = getOne(l);


        Bloglistwithstate b = new Bloglistwithstate();


        BeanUtils.copyProperties(one, b);

        String uid = b.getUid();

        OjUser getuser = userClient.getuser(Long.valueOf(uid));
        b.setUser(getuser);

        if (user == null) {
            b.setLikeState(0);
            b.setStarState(0);
        } else {
            LambdaQueryWrapper<OjBlogUser> ll = new LambdaQueryWrapper<>();//查询本人对文章的点赞情况
            ll.eq(user != null, OjBlogUser::getUid, user).eq(OjBlogUser::getBid, id);
            OjBlogUser ojBlogUser = blogUserMapper.selectOne(ll);

            if (ojBlogUser != null) {
                if (ojBlogUser.getLikeState() == 1) b.setLikeState(1);
                if (ojBlogUser.getStarState() == 1) b.setStarState(1);
            } else {
                b.setLikeState(0);
                b.setStarState(0);
            }
        }

        if (user == null) {
            b.setSubscribeState(0);
        } else {
            LambdaQueryWrapper<OjFans> l1 = new LambdaQueryWrapper<>();//查询本人对作者的粉丝情况
            l1.eq(user != null, OjFans::getUid, uid).eq(OjFans::getFid, user);
            Integer i = fansMapper.selectCount(l1);
            if (i > 0) b.setSubscribeState(1);

        }

        return ResponseResult.okResult(b);

    }

    @Override
    public ResponseResult hl(Bloglistwithstate dto) {

        int likeState = dto.getLikeState();
        int likeCount = dto.getLikeCount();
        Long bid = Long.valueOf(dto.getId());


        System.out.println(likeState);

        Long user = UserContext.getUser();

        if (user == null) return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);


        UpdateWrapper<OjBlogList> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", bid) // 设置条件，这里假设我们要更新id为userId的记录
                .set("like_count", likeCount);// 设置要更新的字段和新的值
        update(null, updateWrapper);


        LambdaQueryWrapper<OjBlogUser> l = new LambdaQueryWrapper<>();
        l.eq(OjBlogUser::getBid, bid).eq(OjBlogUser::getUid, user);
        OjBlogUser ojBlogUser = blogUserMapper.selectOne(l);

        if (ojBlogUser == null) {
            OjBlogUser obu = new OjBlogUser();
            obu.setUid(user.toString());
            obu.setBid(bid.toString());
            obu.setLikeState(likeState);
            blogUserMapper.insert(obu);
        } else {
            ojBlogUser.setLikeState(likeState);
            blogUserService.updateById(ojBlogUser);
        }

        return ResponseResult.okResult("修改点赞状态成功");
    }

    @Override
    public ResponseResult hs(Bloglistwithstate dto) {

        int starState = dto.getStarState();
        int starCount = dto.getStarCount();
        Long bid = Long.valueOf(dto.getId());

        Long user = UserContext.getUser();

        if (user == null) return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);


        UpdateWrapper<OjBlogList> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", bid) // 设置条件，这里假设我们要更新id为userId的记录
                .set("star_count", starCount);// 设置要更新的字段和新的值
        update(null, updateWrapper);


        LambdaQueryWrapper<OjBlogUser> l = new LambdaQueryWrapper<>();
        l.eq(OjBlogUser::getBid, bid).eq(OjBlogUser::getUid, user);
        OjBlogUser ojBlogUser = blogUserMapper.selectOne(l);

        if (ojBlogUser == null) {
            OjBlogUser obu = new OjBlogUser();
            obu.setUid(user.toString());
            obu.setBid(bid.toString());
            obu.setStarState(starState);
            blogUserMapper.insert(obu);
        } else {
            ojBlogUser.setStarState(starState);
            blogUserService.updateById(ojBlogUser);
        }

        return ResponseResult.okResult("修改点赞状态成功");

    }
    @Autowired
    private FileStorageService fileStorageService;
    @Override
    public ResponseResult upl(MultipartFile file) {

        if(file== null || file.getSize() == 0){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        String filename= UUID.randomUUID().toString();
        String oname=file.getOriginalFilename();
        String postfix= oname.substring(oname.lastIndexOf("."));
        String nname=filename+postfix;
        String fileId = null;
        try {
            fileId = fileStorageService.uploadImgFile("", nname, file.getInputStream());
            log.info("上传图片到MinIO中，fileId:{}",fileId);
        } catch (IOException e) {
            throw new RuntimeException("文件传输错误");
        }

        return ResponseResult.okResult(fileId);
    }


    @Override
    public ResponseResult sub(Bloglistwithstate blog) {
        log.info(blog.toString());
        OjBlogList b=new OjBlogList();

        BeanUtils.copyProperties(blog,b);

        log.info(b.toString());
        Long user = UserContext.getUser();
        b.setUid(user.toString());

        saveOrUpdate(b);

        return ResponseResult.okResult("发布成功");

    }

    @Override

    public ResponseResult bal(Integer p,Integer ps) {



        Long user = UserContext.getUser();

        LambdaQueryWrapper<OjBlogList> l = new LambdaQueryWrapper<>();
        l.orderByAsc(OjBlogList::getTime);

        Page<OjBlogList>pa=new Page<>(p,ps);

         page(pa, l);




        List<OjBlogList> list = pa.getRecords();


        List<Bloglistwithstate> res = new ArrayList<>();

        list.stream().forEach(b -> {
            Bloglistwithstate bs = new Bloglistwithstate();

            BeanUtils.copyProperties(b, bs);

            String bid = b.getId();
            String uid = b.getUid();

            OjUser getuser = userClient.getuser(Long.valueOf(uid));
            bs.setUser(getuser);

            if (user == null) {
                bs.setLikeState(0);
                bs.setStarState(0);
            } else {

                LambdaQueryWrapper<OjBlogUser> ll = new LambdaQueryWrapper<>();
                ll.eq(user != null, OjBlogUser::getUid, user).eq(OjBlogUser::getBid, bid);
                OjBlogUser ojBlogUser = blogUserMapper.selectOne(ll);

                if (ojBlogUser != null) {
                    bs.setLikeState(ojBlogUser.getLikeState());
                    bs.setStarState(ojBlogUser.getStarState());
                } else {
                    bs.setLikeState(0);
                    bs.setStarState(0);
                }

            }

            if (user == null) {
                bs.setSubscribeState(0);
            } else {
                LambdaQueryWrapper<OjFans> l1 = new LambdaQueryWrapper<>();
                l1.eq(user != null, OjFans::getUid, uid).eq(OjFans::getFid, user);
                Integer i = fansMapper.selectCount(l1);

                if (i > 0) bs.setSubscribeState(1);
            }
            res.add(bs);
        });

        Page<Bloglistwithstate>np=new Page<>();
        BeanUtils.copyProperties(pa,np,"records");
        np.setRecords(res);
        return ResponseResult.okResult(np);
    }

    @Autowired
    private BlogMapper blogMapper;
    @Override
    public ResponseResult dt() {
        Long user=UserContext.getUser();
        LambdaQueryWrapper<OjFans>l=new LambdaQueryWrapper<>();
        l.eq(OjFans::getFid,user);
        List<OjFans> ojFans = fansMapper.selectList(l);
        List<String> collect = ojFans.stream().map(t -> {//本用户关注的所有人
            return t.getUid();
        }).collect(Collectors.toList());

        if(collect.isEmpty())return ResponseResult.okResult(new ArrayList());

        LambdaQueryWrapper<OjBlogList>l1=new LambdaQueryWrapper<>();
        l1.in(OjBlogList::getUid,collect);
        List<OjBlogList> ojBlogLists = blogMapper.selectList(l1);

        List<Bloglistwithstate> res = new ArrayList<>();

        ojBlogLists.stream().forEach(b -> {
            Bloglistwithstate bs = new Bloglistwithstate();

            BeanUtils.copyProperties(b, bs);

            String bid = b.getId();
            String uid = b.getUid();

            OjUser getuser = userClient.getuser(Long.valueOf(uid));
            bs.setUser(getuser);

            if (user == null) {
                bs.setLikeState(0);
                bs.setStarState(0);
            } else {

                LambdaQueryWrapper<OjBlogUser> ll = new LambdaQueryWrapper<>();
                ll.eq(user != null, OjBlogUser::getUid, user).eq(OjBlogUser::getBid, bid);
                OjBlogUser ojBlogUser = blogUserMapper.selectOne(ll);

                if (ojBlogUser != null) {
                    bs.setLikeState(ojBlogUser.getLikeState());
                    bs.setStarState(ojBlogUser.getStarState());
                } else {
                    bs.setLikeState(0);
                    bs.setStarState(0);
                }

            }

            if (user == null) {
                bs.setSubscribeState(0);
            } else {
                LambdaQueryWrapper<OjFans> l11 = new LambdaQueryWrapper<>();
                l11.eq(user != null, OjFans::getUid, uid).eq(OjFans::getFid, user);
                Integer i = fansMapper.selectCount(l11);

                if (i > 0) bs.setSubscribeState(1);
            }
            res.add(bs);
        });

        return ResponseResult.okResult(res);
    }

    ;
}

