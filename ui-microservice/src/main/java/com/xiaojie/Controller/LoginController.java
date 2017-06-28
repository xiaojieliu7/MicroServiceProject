package com.xiaojie.Controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xiaojie.clients.MovieClient;
import com.xiaojie.clients.RatingClient;
import com.xiaojie.clients.RecommendationService;
import com.xiaojie.clients.UserClient;
import com.xiaojie.models.Movie;
import com.xiaojie.models.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import com.xiaojie.models.UserInfo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by hadoop on 17-6-20.
 */
@RestController
public class LoginController {
    @Autowired
    UserClient userClient;
    @Autowired
    MovieClient movieClient;
    @Autowired
    RatingClient ratingClient;

    @Autowired
    RecommendationService recommendationService;


    //登录首页
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView LoadIndex(){
        ModelAndView mav = new ModelAndView("index");
        mav.addObject("userInfo", new UserInfo());
        return mav;
    }

//    @RequestMapping("/index")
//    public String index(Model model){
//        model.addAttribute("name","Ryan");
//        return "index";
//    }

    //提交表单后进行数据读取，并验证用户名密码
    @RequestMapping(value = "/login", params = {"valid"}, method = RequestMethod.POST)
    public ModelAndView Login(ModelAndView mav, UserInfo userInfo, BindingResult result) {
        System.out.println("用户ID：" + userInfo.getUserid());
        System.out.println("密码：" + userInfo.getPassword());
        UserInfo realUserInfo = userClient.findUser(userInfo.getUserid());

        if(result.hasErrors()){
            mav.addObject("MSG", "出错啦！");
            mav.setViewName("index");
        }else{
            if(realUserInfo!=null && realUserInfo.getPassword().equals(userInfo.getPassword())){
                mav.addObject("MSG", "密码校验成功！");

                return Home(realUserInfo.getUserid());
            }else {
                mav.addObject("MSG", "密码错误！");
                mav.addObject("isshow", true);
                mav.setViewName("index");
            }
        }
        return mav;
    }

    //提交表单后进行数据读取，并验证用户名密码
    @RequestMapping(value = "/login1", params = {"valid"}, method = RequestMethod.POST)
    public String Login(ModelMap map, UserInfo userInfo, BindingResult result) {
        System.out.println("用户ID：" + userInfo.getUserid());
        System.out.println("密码：" + userInfo.getPassword());
        return "login";
    }

    //用户界面
    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public ModelAndView Home(@RequestParam(value = "userid",required = true) String userid){
        ModelAndView mav = new ModelAndView("home");

        //给用户推荐的
        List<Product> recommendationList = recommendationService.findProductsByUser("4")
                .getContent()
                .stream()
                .collect(Collectors.toList());
        List<Movie> recommendationMovieList = movieClient.findByIds(recommendationList.stream().map(a->a.getKnownId()).collect(Collectors.joining(",")))
                .getContent()
                .stream()
                .collect(Collectors.toList());
        mav.addObject("recommendationMovieList", recommendationMovieList);

        //用户观看过的
        List<Product> viewedList = ratingClient.findProductsByUser("4")
                .getContent()
                .stream()
                .collect(Collectors.toList());
        List<Movie> viewedMovieList = movieClient.findByIds(viewedList.stream().map(a->a.getKnownId()).collect(Collectors.joining(",")))
                .getContent()
                .stream()
                .collect(Collectors.toList());
        mav.addObject("viewedMovieList", viewedMovieList);

//        UserInfo userInfo = new UserInfo();
//        userInfo.setUserid("User ID");
//        userInfo.setPassword("password");
//        mav.addObject("userInfo", new UserInfo());
        return mav;
    }

    @RequestMapping(value = "/getmovies", method = RequestMethod.GET)
    public JSONObject getmovies(@RequestParam(value = "movieid",required = true) String movieid){
        List<Movie> viewedMovieList = movieClient.findByIds(recommendationService.getSimilarMovies(movieid))
                .getContent()
                .stream()
                .collect(Collectors.toList());

        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for(Movie movie:viewedMovieList){
            JSONObject jo = new JSONObject();
            jo.put("name", movie.getTitle());
            jo.put("url", movie.getUrl());
            jsonArray.add(jo);
        }

        jsonObject.put("movielist", jsonArray);
        jsonObject.put("msg", "成功！");
        return jsonObject;
    }

}
