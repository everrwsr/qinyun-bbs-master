package com.qinyun.bbs.action;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.qinyun.bbs.config.BbsConfig;
import com.qinyun.bbs.config.CaffeineConfig;
import com.qinyun.bbs.es.annotation.EntityType;
import com.qinyun.bbs.es.annotation.EsIndexType;
import com.qinyun.bbs.es.annotation.EsOperateType;
import com.qinyun.bbs.es.service.SearchService;
import com.qinyun.bbs.es.vo.IndexObject;
import com.qinyun.bbs.model.BbsMessage;
import com.qinyun.bbs.model.BbsPost;
import com.qinyun.bbs.model.BbsTopic;
import com.qinyun.bbs.model.BbsUser;
import com.qinyun.bbs.service.BBSService;
import org.apache.commons.lang3.StringUtils;
import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.engine.PageQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.qinyun.bbs.common.WebUtils;
import com.qinyun.bbs.model.BbsModule;
import com.qinyun.bbs.model.BbsReply;
import com.qinyun.bbs.util.AddressUtil;
import com.qinyun.bbs.util.DateUtil;
import com.qinyun.bbs.util.Functions;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping({"/bbs",""})
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "ResultOfMethodCallIgnored"})
public class BBSController {

    SQLManager          sql;
    BBSService bbsService;
    WebUtils            webUtils;
    Functions           functionUtil;
    SearchService searchService;
    CacheManager        cacheManager;
    BbsConfig bbsConfig;
    HttpServletRequest  request;
    HttpServletResponse response;

    Pattern mediaTypePattern = Pattern.compile("(?i)^image/(.+)$");

    @GetMapping({"", "/{p}", "/index", "/index/{p}"})
    public String index(@PathVariable Optional<Integer> p, String keyword) {
        String view;
        if (StringUtils.isBlank(keyword)) {
            view = "/index.html";
            PageQuery query = new PageQuery(p.orElse(1), null);
            //因为用了spring boot缓存,sb是用返回值做缓存,所以service再次返回了pageQuery以缓存查询结果
            query = bbsService.getTopics(query);
            request.setAttribute("topicPage", query);
            request.setAttribute("pageName", "首页综合");
        } else {
            view = "/lucene/lucene.html";
            //查询索引
            PageQuery<IndexObject> searcherKeywordPage = this.searchService.getQueryPage(keyword, p.orElse(1));
            request.setAttribute("searcherPage", searcherKeywordPage);
            request.setAttribute("pageName", keyword);
            request.setAttribute("resultnum", searcherKeywordPage.getTotalRow());
        }
        return view;
    }

    @GetMapping("/myMessage")
    public String myPage() {
        BbsUser user = webUtils.currentUser();
        request.setAttribute("list", bbsService.getMyTopics(user.getId()));
        return "/message.html";
    }

    @GetMapping("/my/{p}")
    public String openMyTopic(@PathVariable int p) {
        BbsUser    user    = webUtils.currentUser();
        BbsMessage message = bbsService.makeOneBbsMessage(user.getId(), p, 0);
        this.bbsService.updateMyTopic(message.getId(), 0);
        return "redirect:/bbs/topic/" + p;
    }

    @GetMapping({"/topic/hot", "/topic/hot/{p}"})
    public String hotTopic(@PathVariable Optional<Integer> p) {
        PageQuery query = new PageQuery(p.orElse(1));
        request.setAttribute("topicPage", bbsService.getHotTopics(query));
        return "/bbs/index.html";
    }

    @GetMapping({"/topic/nice", "/topic/nice/{p}"})
    public String niceTopic(@PathVariable Optional<Integer> p) {
        PageQuery query = new PageQuery(p.orElse(1), null);
        request.setAttribute("topicPage", bbsService.getNiceTopics(query));
        return "/bbs/index.html";
    }

    @GetMapping({"/topic/{id}", "/topic/{id}/{p}"})
    @EsIndexType(entityType = EntityType.BbsTopic, operateType = EsOperateType.UPDATE)
    public String topic(@PathVariable Integer id, @PathVariable Optional<Integer> p) {
        PageQuery query = new PageQuery(p.orElse(1));
        query.setPara("topicId", id);
        query = bbsService.getPosts(query);
        request.setAttribute("postPage", query);

        BbsTopic topic    = bbsService.getTopic(id);
        BbsTopic template = new BbsTopic();
        template.setId(id);
        template.setPv(topic.getPv() + 1);
        sql.updateTemplateById(template);

        request.setAttribute("topic", topic);
        return "/detail.html";
    }

    @GetMapping({"/topic/module/{id}", "/topic/module/{id}/{p}"})
    public String module(@PathVariable Integer id, @PathVariable Optional<Integer> p) {
        PageQuery query = new PageQuery<>(p.orElse(1));
        query.setPara("moduleId", id);
        query = bbsService.getTopics(query);
        request.setAttribute("topicPage", query);
        if (query.getList().size() > 0) {
            BbsTopic bbsTopic = (BbsTopic) query.getList().get(0);
            request.setAttribute("pageName", bbsTopic.getTails().get("moduleName"));
            request.setAttribute("module", this.bbsService.getModule(id));
        }
        return "/index.html";
    }

    /**
     * 文章发布改为Ajax方式提交更友好
     */
    @ResponseBody
    @PostMapping("/topic/save")
    @EsIndexType(entityType = EntityType.BbsTopic, operateType = EsOperateType.ADD, key = "tid")
    @EsIndexType(entityType = EntityType.BbsPost, operateType = EsOperateType.ADD, key = "pid")
    public JSONObject saveTopic(BbsTopic topic, BbsPost post, String code, String title, String postContent) {
        //@TODO， 防止频繁提交
        BbsUser user = webUtils.currentUser();
//		Date lastPostTime = bbsService.getLatestPost(user.getId());
//		long now = System.currentTimeMillis();
//		long temp = lastPostTime.getTime();
//		if(now-temp<1000*10){
//			//10秒之内的提交都不处理
//			throw new RuntimeException("提交太快，处理不了，上次提交是 "+lastPostTime);
//		}

        HttpSession session = request.getSession(true);
        String      verCode = (String) session.getAttribute(LoginController.POST_CODE_NAME);


        JSONObject result = new JSONObject();
        result.put("err", 1);
        if (user == null) {
            result.put("msg", "请先登录后再继续！");
            return result;
        }

        //验证码不要区分大小写
        if (!verCode.equalsIgnoreCase(code)) {
            result.put("msg", "验证码不正确");
            return result;
        }

        if (title.length() < 5 || postContent.length() < 10) {
            //客户端需要完善
            result.put("msg", "标题或内容太短！");
            return result;
        }


        BbsModule module = this.bbsService.getModule(topic.getModuleId());

        if (!isAllowAdd(module)) {
            result.put("msg", "板块 [" + module.getName() + "] 普通用户只能浏览");
            return result;
        }
        //4个小时的提交总数
        Date lastPost = DateUtil.getDate(new Date(), bbsConfig.getTopicCountMinutes());
        int  count    = bbsService.getTopicCount(user, lastPost);
        if (count >= bbsConfig.getTopicCount()) {
            String msg = AddressUtil.getIPAddress(request) + " " + user.getUserName() + " 提交主题太频繁，稍后再提交，紧急问题入群";
            result.put("msg", msg);
            System.out.println(msg);
            return result;
        }
        topic.setIsNice(0);
        topic.setIsUp(0);
        topic.setPv(1);
        topic.setPostCount(1);
        topic.setReplyCount(0);
        post.setHasReply(0);
        topic.setContent(title);
        post.setContent(postContent);
        bbsService.saveTopic(topic, post, user);

        result.put("err", 0);
        result.put("tid", topic.getId());
        result.put("pid", post.getId());
        result.put("msg", "topic/" + topic.getId());


        return result;
    }

    private boolean isAllowAdd(BbsModule module) {
        return functionUtil.allowPost(module, request, response);
    }

    @ResponseBody
    @PostMapping("/post/save")
    @EsIndexType(entityType = EntityType.BbsPost, operateType = EsOperateType.ADD)
    public JSONObject savePost(BbsPost post) {
        JSONObject result = new JSONObject();
        result.put("err", 1);
        if (post.getContent().length() < 5) {
            result.put("msg", "内容太短，请重新编辑！");
        } else {
            post.setHasReply(0);
            post.setCreateTime(new Date());
            BbsUser user = webUtils.currentUser();
            bbsService.savePost(post, user);
            BbsTopic topic     = bbsService.getTopic(post.getTopicId());
            int      totalPost = topic.getPostCount() + 1;
            topic.setPostCount(totalPost);
            bbsService.updateTopic(topic);

            bbsService.notifyParticipant(topic.getId(), user.getId());

            int pageSize = (int) PageQuery.DEFAULT_PAGE_SIZE;
            int page     = (totalPost / pageSize) + (totalPost % pageSize == 0 ? 0 : 1);
            result.put("msg", "topic/" + post.getTopicId() + "/" + page);
            result.put("err", 0);
            result.put("id", post.getId());
        }
        return result;
    }


    /**
     * 回复评论改为Ajax方式提升体验
     */
    @ResponseBody
    @PostMapping("/reply/save")
    @EsIndexType(entityType = EntityType.BbsReply, operateType = EsOperateType.ADD)
    public JSONObject saveReply(BbsReply reply) {
        JSONObject result = new JSONObject();
        result.put("err", 1);
        BbsUser user = webUtils.currentUser();
        if (user == null) {
            result.put("msg", "未登录用户！");
        } else if (reply.getContent().length() < 2) {
            result.put("msg", "回复内容太短，请修改!");
        } else {
            reply.setUserId(user.getId());
            reply.setPostId(reply.getPostId());
            reply.setCreateTime(new Date());
            bbsService.saveReply(reply);
            reply.setUser(user);
            result.put("msg", "评论成功！");
            result.put("err", 0);
            bbsService.notifyParticipant(reply.getTopicId(), user.getId());
            result.put("id", reply.getId());
        }
        return result;
    }

    @RequestMapping("/user/{id}")
    public String saveUser(@PathVariable int id) {
        BbsUser user = sql.unique(BbsUser.class, id);
        request.setAttribute("user", user);
        return "/bbs/user/user.html";
    }


    // ============== 上传文件路径：项目根目录 upload
    @PostMapping("/upload")
    @ResponseBody
    public Map<String, Object> upload(@RequestParam("editormd-image-file") MultipartFile file) {
        Map<String, Object> map = new HashMap<>();
        map.put("success", false);
        try {
            BbsUser user = webUtils.currentUser();
            if (null == user) {
                map.put("error", 1);
                map.put("msg", "上传出错，请先登录！");
                return map;
            }
            //从剪切板粘贴上传没有后缀名，通过此方法可以获取后缀名
            Matcher matcher = mediaTypePattern.matcher(Objects.requireNonNull(file.getContentType()));
            if (matcher.find()) {
                String newName   = UUID.randomUUID().toString() + System.currentTimeMillis() + "." + matcher.group(1);
                String filePaths = "upload" + File.separator;
                File   fileout   = new File(filePaths);
                if (!fileout.exists()) {
                    fileout.mkdirs();
                }
                FileCopyUtils.copy(file.getBytes(), new File(filePaths + newName));
                map.put("file_path", request.getContextPath() + "/showPic/" + newName);
                map.put("msg", "图片上传成功！");
                map.put("success", true);
                return map;
            } else {
                map.put("success", "不支持的上传文件格式！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            map.put("msg", "图片上传出错！");
        }
        return map;
    }

    // ======================= admin

    /**
     * 踩或顶 评论
     */
    @PostMapping("/post/support/{postId}")
    @ResponseBody
    @EsIndexType(entityType = EntityType.BbsPost, operateType = EsOperateType.UPDATE)
    public JSONObject updatePostSupport(@PathVariable Integer postId, @RequestParam Integer num) {
        JSONObject result = new JSONObject();
        result.put("err", 1);
        BbsUser user = webUtils.currentUser();
        if (user == null) {
            result.put("msg", "未登录用户！");
        } else {
            BbsPost post = bbsService.getPost(postId);

            Cache        cache        = cacheManager.getCache(CaffeineConfig.Caches.postSupport.name());
            ValueWrapper valueWrapper = Objects.requireNonNull(cache).get(user.getId() + ":" + post.getId());
            if (valueWrapper != null && valueWrapper.get() != null) {
                result.put("err", 1);
                result.put("msg", "请勿频繁点赞，休息一下吧~~~");
            } else {
                if (num == 0) {
                    int cons = post.getCons() != null ? post.getCons() : 0;
                    post.setCons(++cons);
                    result.put("data", post.getCons());
                } else {
                    int pros = post.getPros() != null ? post.getPros() : 0;
                    post.setPros(++pros);
                    result.put("data", post.getPros());
                }
                bbsService.updatePost(post);

                result.put("id", post.getId());
                result.put("err", 0);
                cache.put(user.getId() + ":" + post.getId(), 1);
            }
        }
        return result;
    }

    /**
     * 提问人或管理员是否已采纳
     */
    @PostMapping("/user/post/accept/{postId}")
    @ResponseBody
    @EsIndexType(entityType = EntityType.BbsPost, operateType = EsOperateType.UPDATE)
    public JSONObject updatePostAccept(@PathVariable Integer postId) {
        JSONObject result = new JSONObject();
        result.put("err", 1);
        BbsUser user = webUtils.currentUser();
        BbsPost post = bbsService.getPost(postId);
        if (user == null || post == null || !webUtils.isAdmin() || !user.getId().equals(post.getUserId())) {
            result.put("err", 1);
            result.put("msg", "无法操作");
        } else {

            post.setIsAccept((post.getIsAccept() == null || post.getIsAccept() == 0) ? 1 : 0);
            result.put("data", post.getIsAccept());
            bbsService.updatePost(post);
            result.put("err", 0);
            result.put("id", post.getId());
        }
        return result;
    }
}
