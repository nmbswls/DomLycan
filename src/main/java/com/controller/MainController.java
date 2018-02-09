package com.controller;

import com.bean.Roles;
import com.services.MainServiceImpl;
import com.utils.JavaWebToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import javax.json.Json;
import javax.json.JsonArray;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Controller
public class MainController {

//    @RequestParam("name") String name,

    @Resource
    private MainServiceImpl mainServiceImpl;

    @RequestMapping("/rng")
    public String rng( Model model){



        return "createRoom";
    }
    @RequestMapping("/createRoomOK")
    public String createRoomOK(@RequestParam(name = "village",defaultValue = "4")Integer village,
                       @RequestParam(name = "lycan",defaultValue = "4")Integer lycan,
                       @RequestParam(name = "defaults",required = false)String[] defaults,
                       @RequestParam(name = "customs", required = false)String[] customs,
                               @RequestParam(name = "roomId")String roomId,
                               Model model){
        int totalPlayer = village + lycan;
        List<String> rolesOrigin = new ArrayList<String>();
        for(int i=0;i<village;i++)rolesOrigin.add("村民");
        for(int i=0;i<lycan;i++)rolesOrigin.add("狼人");
        if(defaults == null){
            defaults = new String[0];
        }
        if(customs == null){
            customs = new String[0];
        }
        for(int i =0;i<customs.length;i++){
            rolesOrigin.add(customs[i]);
            totalPlayer++;
        }
        for(int i =0;i<defaults.length;i++){
            rolesOrigin.add(defaults[i]);
            totalPlayer++;
        }


        Collections.shuffle(rolesOrigin);
        for(int i=0;i<rolesOrigin.size();i++){
            System.out.println(rolesOrigin.get(i)+" ");
        }

        Jedis jedis = new Jedis("127.0.0.1",6379);

        for(int i=0;i<rolesOrigin.size();i++){
            jedis.hset("roles_of_room_"+roomId,(i+1)+"",rolesOrigin.get(i));
        }



//        mainServiceImpl.deal(rolesOrigin);
        model.addAttribute("roomId",roomId);
        return "enterRoom";
    }

    @RequestMapping("/getRoleDesc")
    public String getRoleDesc(@RequestParam(name = "roomId")Integer roomId,
                          @RequestParam(name = "seatId")Integer seatId,
                          HttpServletRequest request,
                          Model model){

        //判断是否被占位
        Jedis jedis = new Jedis("127.0.0.1",6379);
        String role = jedis.hget("roles_of_room_"+roomId,seatId+"");
        String nightInfo = "你是个闭眼玩家";
        if(role.equals("狼人")){
            nightInfo = "杀人";
        }else if(role.equals("预言家")){
            nightInfo = "预言";
        }
        model.addAttribute("roomId",roomId);
        model.addAttribute("seatId",seatId);
        model.addAttribute("role",role);

        return "room";
    }
//    @RequestMapping("/room")
//    public String getRoom(Model model){
//        model.addAttribute("roomId",-1);
//        return "room";
//    }

    @RequestMapping("/enterRoom")
    public String enterRoom(HttpServletRequest request,Model model){
        return "enterRoom";
    }

    @RequestMapping("/enterRoomCheck")
    public String enterRoomCheck(@RequestParam(name = "roomId") String roomId,
                                 @RequestParam(name = "seatId") String seatId,
                                 HttpServletRequest request,Model model){
        //验证
        return "room";
    }


//    @RequestMapping("/createRoom")
//    public String createRoom(HttpServletRequest request,Model model){
//
//        return "createRoom";
//    }

    @RequestMapping("/homepage")
    public String homepage(HttpServletRequest request, Model model){

        return "homepage";
    }

    @RequestMapping("/shuffle")
    public String shuffle(Integer roomId, Model model){

        Jedis jedis = new Jedis("127.0.0.1",6379);
        Map<String,String> map = jedis.hgetAll("roles_of_room_"+roomId);
        List<String> roles = new ArrayList<String>();
        for(Map.Entry<String,String> entry:map.entrySet()){
            roles.add(entry.getValue());
        }
        Collections.shuffle(roles);
        for(int i=0;i<roles.size();i++){
            jedis.hset("roles_of_room_"+roomId,(i+1)+"",roles.get(i));
        }

        for(int i=0;i<roles.size();i++){
            System.out.println(roles.get(i)+" ");
        }

//        mainServiceImpl.deal(rolesOrigin);
        model.addAttribute("roomId",roomId);
        return "enterRoom";
    }

    @RequestMapping("/reCreate")
    public String reCreate(Integer roomId, Model model){
        model.addAttribute("roomId",roomId);
        return "createRoom";
    }


    @RequestMapping("/login")
    public String login(@RequestParam(name = "phoneNumber") String phoneNumber,
                        @RequestParam(name = "validCode") String validCode,
                        Model model, HttpServletRequest request, HttpServletResponse response){

        //验证
        if(validCode.equals("8888")){
            Jedis jedis = new Jedis("127.0.0.1",6379);
            //验证成功.
            Map<String ,Object> claims = new HashMap<String, Object>();
            claims.put("phoneNumber",phoneNumber);
            String token = JavaWebToken.createJavaWebToken(claims, (long) (3600*24*30));
            jedis.set("user_token_"+phoneNumber,token);
            Cookie cookie = new Cookie("userToken", token);
            cookie.setMaxAge(60 * 60 * 24 *30);// 设置为30min
            cookie.setPath("/");
            response.addCookie(cookie);

            //开始房间
            if(jedis.exists("room_of_user_"+phoneNumber)){
                //已有建主，判断该房间是否过期
                String roomId = jedis.get("room_of_user_"+phoneNumber);
                if(jedis.exists("room_"+roomId)){
                    //房间未过期，返回该房间
                    jedis.expire("room_"+roomId,3600*24);
                    model.addAttribute("roomId",roomId);
                    return "createRoom";
                }else{
                    //房间已过期，进入下一步

                }


            }
            // 没有建主，或房间已过期，随即分配房号
            int start = (int)(Math.floor(Math.random()*100000000));
            int i = start;
            while(jedis.exists("room_"+i)){
                i = (i+1)%100000000;
                if(start == i){
                    //无空房间，报错
                    model.addAttribute("msg","网络繁忙");
                    return "homepage";
                }
            }
            //有空房间 分配到i 更新room_of_user_X 以方便查找
            jedis.set("room_of_user_"+phoneNumber,i+"");
            //设置房间信息
            jedis.hset("room_"+i,"1","Y");
            //设置报废时间
            jedis.expire("room_"+i,24*3600);
            model.addAttribute("roomId",i);
            return "createRoom";


        }else{
            model.addAttribute("msg","验证失败");
            return "login";
        }




    }




    @RequestMapping("/logValidation")
    public String logValidation(Model model,HttpServletRequest request){
        Jedis jedis = new Jedis("127.0.0.1",6379);

        Cookie cookies[]=request.getCookies(); //读出用户硬盘上的Cookie，并将所有的Cookie放到一个cookie对象数组里面
        Cookie sCookie=null;
        String requestToken = null;
        for(int i=0;i<cookies.length;i++){    //用一个循环语句遍历刚才建立的Cookie对象数组
            sCookie=cookies[i];   //取出数组中的一个Cookie对象
            if(sCookie!=null){
                if(("userToken").equals(sCookie.getName())){
                    requestToken = sCookie.getValue();
                }
            }
        }
        if(requestToken == null){
            model.addAttribute("msg","请先登录");
            return "login";
        }
        Map<String,Object> claims = JavaWebToken.parserJavaWebToken(requestToken);
        for(Map.Entry<String,Object> entry:claims.entrySet()){
            System.out.println(entry.getKey()+"--"+entry.getValue());
        }
        Long exp = null;
        Long nbf = null;
        String user = null;
        try{
            exp = Long.parseLong(claims.get("exp").toString());
            nbf = Long.parseLong(claims.get("nbf").toString());
            user = claims.get("phoneNumber").toString();
        }catch (Exception e){
            model.addAttribute("msg","非法的令牌");
            return "login";
        }

        Long now = System.currentTimeMillis()/1000;
        System.out.println("now:"+now);
        System.out.println("exp"+exp);
        if(now<nbf){
            model.addAttribute("msg","登录令牌出错");

        }
        if(now>exp){
            model.addAttribute("msg","登录已过期");

        }else{
            //有效的令牌，判断服务器端是否存在信息

            if(jedis.exists("user_token_"+user)){
               //此时存在令牌，判断与服务器端是否一致
               String userToken = jedis.get("user_token_"+user);
               if(userToken.equals(requestToken)){
                    //验证成功，登录正确，判断是否已有建主
                   if(jedis.exists("room_of_user_"+user)){
                       //已有建主，判断该房间是否过期
                       String roomId = jedis.get("room_of_user_"+user);
                       if(jedis.exists("room_"+roomId)){
                           //房间未过期，返回该房间
                           jedis.expire("room_"+roomId,3600*24);
                           model.addAttribute("roomId",roomId);
                           return "createRoom";
                       }else{
                           //房间已过期，进入下一步

                       }


                   }
                    // 没有建主，或房间已过期，随即分配房号
                   int start = (int)(Math.floor(Math.random()*100000000));
                   int i = start;
                   while(jedis.exists("room_"+i)){
                       i = (i+1)%100000000;
                       if(start == i){
                           //无空房间，报错
                           model.addAttribute("msg","网络繁忙");
                           return "homepage";
                       }
                   }
                   //有空房间 分配到i 更新room_of_user_X 以方便查找
                   jedis.set("room_of_user_"+user,i+"");
                   //设置房间信息
                   jedis.hset("room_"+i,"1","Y");
                   //设置报废时间
                   jedis.expire("room_"+i,24*3600);
                   model.addAttribute("roomId",i);
                   return "createRoom";

               }else{
                   //令牌不一致，判断在另一台机器上登录
                   // 重新登录
                   model.addAttribute("msg","重复登录，请验证");
               }

            }else{
                //令牌是瞎几把搞的，服务器端都没信息
                //重新登录
                model.addAttribute("msg","请登录");
            }

        }



        return "login";

    }




    @RequestMapping("/kill")
    @ResponseBody
    public List<String> kill(Integer roomId, Integer seatId, HttpServletRequest request){
        String kill = request.getParameter("kill");
        System.out.println(kill);
        List<Roles> list = new ArrayList<Roles>();
        Jedis jedis = new Jedis("127.0.0.1",6379);

        jedis.hset("kill_of_room_"+roomId,seatId+"",kill);
        Map<String,String> map = jedis.hgetAll("kill_of_room_"+roomId);
        List<String> kills = new ArrayList<String>();
        for(Map.Entry<String,String> entry:map.entrySet()){
            System.out.println("杀"+entry.getValue());
            kills.add(entry.getKey()+"号杀"+entry.getValue()+"号");
        }
        return kills;
    }


}
