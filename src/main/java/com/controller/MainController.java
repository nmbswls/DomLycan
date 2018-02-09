package com.controller;

import com.Main;
import com.bean.Roles;
import com.constant.MainConstant;
import com.google.gson.Gson;
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
import javax.management.relation.Role;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Array;
import java.util.*;

@Controller
public class MainController {

//    @RequestParam("name") String name,

    @Resource
    private MainServiceImpl mainServiceImpl;




    public String getCookie(String cookieName,Cookie[] cookies){
        Cookie sCookie=null;
        String requestToken = null;
        for(int i=0;i<cookies.length;i++){    //用一个循环语句遍历刚才建立的Cookie对象数组
            sCookie=cookies[i];   //取出数组中的一个Cookie对象
            if(sCookie!=null){
                if((cookieName).equals(sCookie.getName())){
                    requestToken = sCookie.getValue();
                }
            }
        }
        return requestToken;

    }



    public boolean deleteCookie(String cookieName,Cookie[] cookies){
        Cookie sCookie=null;
        String requestToken = null;
        for(int i=0;i<cookies.length;i++){    //用一个循环语句遍历刚才建立的Cookie对象数组
            sCookie=cookies[i];   //取出数组中的一个Cookie对象
            if(sCookie!=null){
                if((cookieName).equals(sCookie.getName())){
                    sCookie.setMaxAge(0);
                    sCookie.setPath("/");
                    return true;
                }
            }
        }
        System.out.println("删除cookie"+cookieName+"失败");
        return false;

    }




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

        List<Roles> rolesList = new ArrayList<Roles>();
        Collections.shuffle(rolesOrigin);
        for(int i=0;i<rolesOrigin.size();i++){
            rolesList.add(new Roles(Integer.parseInt(roomId),rolesOrigin.get(i),(i+1),"Y","Y"));
        }

        Jedis jedis = new Jedis("127.0.0.1",6379);
        Gson gson = new Gson();
        for(int i=0;i<rolesOrigin.size();i++){
            jedis.hset("roles_of_room_"+roomId,(i+1)+"",gson.toJson(rolesList.get(i)));
        }
        jedis.expire(MainConstant.roleListProfix+roomId,24*3600);



//        mainServiceImpl.deal(rolesOrigin);
        model.addAttribute("roomId",roomId);
        return "enterRoom";
    }

    @RequestMapping("/getRoleDesc")
    public String getRoleDesc(@RequestParam(name = "roomId")Integer roomId,
                          @RequestParam(name = "seatId")Integer seatId,
                          HttpServletRequest request,HttpServletResponse response,
                          Model model){
        Jedis jedis = new Jedis("127.0.0.1",6379);
        //取本地cookie
        Cookie[] cookies=request.getCookies(); //读出用户硬盘上的Cookie，并将所有的Cookie放到一个cookie对象数组里面
        String seatToken = getCookie(MainConstant.seatToken,cookies);

        model.addAttribute("roomId",roomId);

        //判断是否初始化完成
        String json = jedis.hget("roles_of_room_"+roomId,seatId+"");
        if(json == null){
            //没有role
            model.addAttribute("msg","请输入正确的房间座位号，并等待配置角色");
            return "enterRoom";
        }
        Gson gson = new Gson();
        //服务器端roles信息，转化为roles对象
        Roles roles = gson.fromJson(json,Roles.class);

        if(seatToken == null||!JavaWebToken.isTokenValid(JavaWebToken.parserJavaWebToken(seatToken))){
            //没cookie，或是token已过期，认为是首次登录，任意进入位置.
            if(roles.getToken()!=null){
                //位置已被占据，判断token是否有效
                if(JavaWebToken.isTokenValid(JavaWebToken.parserJavaWebToken(roles.getToken()))){
                    model.addAttribute("msg","该位置已被选择");
                    return "enterRoom";
                }else{

                }
            }
            System.out.println("服务器端无token");
            //服务器端和用户端均没有token，或服务器端token失效。
            //生成token并保存在服务器
            Map<String,Object> claims = new HashMap<String, Object>();
            claims.put("roomId",roomId);
            claims.put("seatId",seatId);
            String token = JavaWebToken.createJavaWebToken(claims,(long)3600*24);
            roles.setToken(token);
            jedis.hset("roles_of_room_"+roomId,seatId+"",gson.toJson(roles));

            //将token保存在客户端
            deleteCookie(MainConstant.seatToken,cookies);

            Cookie cookie = new Cookie(MainConstant.seatToken, token);
            cookie.setMaxAge(60 * 60 * 24 );// 设置为一天
            cookie.setPath("/");
            response.addCookie(cookie);

            //进入页面

        }else{
            //本机有合法的token
            Map<String,Object> claimsServer = JavaWebToken.parserJavaWebToken2(roles.getToken());

            //判断服务器是否有合法token

            Map<String,String> mapRoles = jedis.hgetAll(MainConstant.roleListProfix+roomId);
            int flag = 0;
            for(Map.Entry<String,String> entry:mapRoles.entrySet()){
                Roles roleInMap = gson.fromJson(entry.getValue(), Roles.class);
                //存在且相等
                if(roleInMap.getToken()!=null&&roleInMap.getToken().equals(seatToken)){
                    flag = 1;
                    break;
                }
            }

            if(flag==1){
                //当flag有值时，说明云端某一个座位是登陆者的
                if(roles.getToken()!=null&&roles.getToken().equals(seatToken)){
                    //匹配 成功登入
                }else{
                    //不匹配，则这货打算偷看
                    model.addAttribute("msg","老哥别偷看了lv2");
                    return "enterRoom";
                }

            }else{
                //说明云端并没有座位是登陆者的。

                if(roles.getToken()==null){
                    //目标座位为空，用户的token是别的房间遗留的，此时可登陆
                    Map<String,Object> claims = new HashMap<String, Object>();
                    claims.put("roomId",roomId);
                    claims.put("seatId",seatId);
                    String token = JavaWebToken.createJavaWebToken(claims,(long)3600*24);
                    roles.setToken(token);
                    jedis.hset("roles_of_room_"+roomId,seatId+"",gson.toJson(roles));

                    //将token保存在客户端
                    deleteCookie(MainConstant.seatToken,cookies);

                    Cookie cookie = new Cookie(MainConstant.seatToken, token);
                    cookie.setMaxAge(60 * 60 * 24 );// 设置为一天
                    cookie.setPath("/");
                    response.addCookie(cookie);
                }else if(JavaWebToken.parserJavaWebToken2(roles.getToken())==null){
                    model.addAttribute("msg","游戏过期，重新选号。");
                    return "enterRoom";
                }else{
                    //服务器token合法，你不能进入这个位置
                    model.addAttribute("msg","位置已被占据，请更换");
                    return "enterRoom";
                }

            }



//            if(roles.getToken()==null){
//                //服务端没有token，本机有token，非法看别人牌。
//                Map<String,Object> lastClaim = JavaWebToken.parserJavaWebToken(seatToken);
//                if(!JavaWebToken.isTokenValid(lastClaim)){
//                    deleteCookie(MainConstant.seatToken,request.getCookies());
//                    model.addAttribute("msg","游戏过期，重新选号。");
//                    return "enterRoom";
//                }
//                if(lastClaim.get("roomId").equals(roomId)&&lastClaim.get("seatId").equals(seatId)){
//                    //成功登录，是洗牌过后令牌消失的结果
//                    System.out.println("房号："+roomId+";座位号"+seatId);
//                }else{
//                    model.addAttribute("msg", "老哥别偷看!");
//                    return "enterRoom";
//                }
//
//
//            }else{
//                //服务端有token 认为必定是合法的，
//                if(roles.getToken().equals(seatToken)){
//                    //两者相同，是合法的登录。
//
//                }else{
//                    //非法的登录，不符合
//                    Map<String, Object> claims = JavaWebToken.parserJavaWebToken(roles.getToken());
//                    String savedRoomId = claims.get("roomId").toString();
//                    String savedSeatId = claims.get("seatId").toString();
//                    if (!roomId.equals(savedRoomId)) {
//                        //老哥你进错房间了。
//                        model.addAttribute("msg", "老哥你进错房间了.");
//                        return "enterRoom";
//                    } else {
//                        if (!seatId.equals(savedSeatId)) {
//                            //老哥你偷看了
//                            model.addAttribute("msg", "老哥别偷看!");
//                            return "enterRoom";
//                        } else {
//                            //正确进入房间,下一步逻辑
//                            model.addAttribute("msg", "登录过期，请重开房间!");
//                            deleteCookie(MainConstant.seatToken,request.getCookies());
//                            return "enterRoom";
//                        }
//                    }
//
//                }
//
//
//
//            }


        }
        //开始进入房间操作


        //得到角色信息。


        String nightInfo = "你是个闭眼玩家";
        if(roles.getRole().equals("狼人")){
            nightInfo = "狼人";
        }else if(roles.getRole().equals("预言家")){
            nightInfo = "预言家";
        }else if(roles.getRole().equals("女巫")){
            nightInfo = "女巫";
        }
        model.addAttribute("seatId",seatId);
        model.addAttribute("role",roles.getRole());


        //得到房主信息
          //读出用户硬盘上的Cookie，并将所有的Cookie放到一个cookie对象数组里面
        String userToken = getCookie("userToken",cookies);
        Map<String,Object> claims = JavaWebToken.parserJavaWebToken(userToken);
        if(JavaWebToken.isTokenValid(claims)){
            String phoneNumber = claims.get("phoneNumber").toString();
            //如果未失效 可取
            if(jedis.exists(MainConstant.roomInfoProfix+roomId)&&jedis.hget(MainConstant.roomInfoProfix+roomId,"host").equals(phoneNumber)){
                model.addAttribute("isHost","Y");
                int turn = 1;
                if(jedis.exists(MainConstant.turnInfoProfix+roomId)){
                    turn = Integer.parseInt(jedis.get(MainConstant.turnInfoProfix+roomId));
                }
                model.addAttribute("turn",turn+"");
            }else{
                //失效，
                return "login";
            }
        }else{
            //登录失效，重新登录
            return "login";
        }

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

//    @RequestMapping("/enterRoomCheck")
//    public String enterRoomCheck(@RequestParam(name = "roomId") String roomId,
//                                 @RequestParam(name = "seatId") String seatId,
//                                 HttpServletRequest request,Model model){
//        //验证
//        return "room";
//    }


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
        Gson gson = new Gson();
        List<String> rolesNameList = new ArrayList<String>();
        for(Map.Entry<String,String> entry:map.entrySet()){
            rolesNameList.add(gson.fromJson(entry.getValue(),Roles.class).getRole());
        }

        List<Roles> rolesClassList = new ArrayList<Roles>();
        //roles中存放着json格式的数据
        Collections.shuffle(rolesNameList);

        for(int i =0;i<rolesNameList.size();i++){
            rolesClassList.add(new Roles(roomId,rolesNameList.get(i),(i+1),"Y","Y"));
        }

        for(int i=0;i<rolesClassList.size();i++){
            jedis.hset("roles_of_room_"+roomId,(i+1)+"",gson.toJson(rolesClassList.get(i)));
        }
//
//        for(int i=0;i<roles.size();i++){
//            System.out.println(roles.get(i)+" ");
//        }

//        mainServiceImpl.deal(rolesOrigin);
        model.addAttribute("roomId",roomId);
        return "enterRoom";
    }

    @RequestMapping("/reCreate")
    public String reCreate(Integer roomId, Model model){
        model.addAttribute("roomId",roomId);
        Gson gson = new Gson();
        Jedis jedis = new Jedis("127.0.0.1",6379);
        if(jedis.exists(MainConstant.roomInfoProfix+roomId)){
            Map<String,String> msp=jedis.hgetAll(MainConstant.roleListProfix+roomId);
            List<String> roleList=new ArrayList<String>();
            for(Map.Entry<String,String> entry:msp.entrySet()){
                String role = gson.fromJson(entry.getValue(),Roles.class).getRole();
                roleList.add(role);
            }
            int v = 0;
            int l = 0;
            Set<String> god = new HashSet<String>();
            List<String> custom = new ArrayList<String>();
            for(int i=0;i<roleList.size();i++){
                if(roleList.get(i).equals("狼人")){
                    l++;
                }else if(roleList.get(i).equals("村民")){
                    v++;
                }else{
                    if(Arrays.asList(MainConstant.defaultGodNames).contains(roleList.get(i))){
                        god.add(roleList.get(i));
                        System.out.println("神民中加入了"+roleList.get(i));
                    }else{
                        custom.add(roleList.get(i));
                        System.out.println("自定义中加入了"+roleList.get(i));
                    }

                }
            }
            model.addAttribute("villages",v);
            model.addAttribute("lycans",l);
            model.addAttribute("gods",new ArrayList<String>(god));
            model.addAttribute("customs",custom);


        }




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
            jedis.hset("room_"+i,"host",phoneNumber);
            System.out.println("host of "+i+" is:"+phoneNumber);
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


        Cookie cookies[]=request.getCookies();
        Cookie sCookie=null;
        String requestToken = getCookie(MainConstant.userToken,cookies);


        if(requestToken == null){
            model.addAttribute("msg","请先登录");
            return "login";
        }
        Map<String,Object> claims = JavaWebToken.parserJavaWebToken(requestToken);
//        for(Map.Entry<String,Object> entry:claims.entrySet()){
//            System.out.println(entry.getKey()+"--"+entry.getValue());
//        }
        String user = claims.get("phoneNumber").toString();
        if(!JavaWebToken.isTokenValid(claims)){

        } else{
            //有效的令牌，判断服务器端是否存在信息

            if(jedis.exists("user_token_"+user)){
               //此时存在令牌，判断与服务器端是否一致
               String userToken = jedis.get("user_token_"+user);
               if(userToken.equals(requestToken)){
                    //验证成功，登录正确，判断是否已有建主
                   String formerRoom = hasCreatedRoom(user);
                    // 没有建主，或房间已过期，随即分配房号
                   if(formerRoom!=null){
                       model.addAttribute("roomId",formerRoom);
                       return "createRoom";
                   }
                   String createRoomId = randomCreateRoom();
                   if(createRoomId == null){
                       model.addAttribute("msg","网络繁忙");
                       return "homepage";
                   }
                   //若是成功，则开始创建。

                   //有空房间 分配到i 更新room_of_user_X 以方便查找
                   jedis.set("room_of_user_"+user,createRoomId);
                   //设置房间信息
                   jedis.hset("room_"+createRoomId,"host",user);
                   System.out.println("host of "+createRoomId+" is:"+user);
                   //设置报废时间
                   jedis.expire("room_"+createRoomId,24*3600);
                   model.addAttribute("roomId",createRoomId);
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



    @RequestMapping("/turnGo")
    @ResponseBody
    public String turnGo(Integer roomId, HttpServletRequest request){
        Jedis jedis = new Jedis("127.0.0.1",6379);

        jedis.incr(MainConstant.turnInfoProfix+roomId);
        int turn = Integer.parseInt(jedis.get(MainConstant.turnInfoProfix+roomId));
        jedis.expire(MainConstant.turnInfoProfix+roomId,24*3600);
        System.out.println("行动角色由"+MainConstant.turnArrange[(turn-1)]+"变为"+MainConstant.turnArrange[(turn)]);
        return (turn)+"";
    }
    @RequestMapping("/turnFinish")
    @ResponseBody
    public String turnFinish(Integer roomId, HttpServletRequest request){
        Jedis jedis = new Jedis("127.0.0.1",6379);

        Long del = jedis.del(MainConstant.turnInfoProfix+roomId);

        System.out.println(del+"条行动信息被删除");
        return del+"";
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



    public String hasCreatedRoom(String user){
        Jedis jedis = new Jedis("127.0.0.1",6379);
        if(jedis.exists(MainConstant.userRoomProfix+user)){
            //已有建主，判断该房间是否过期
            String roomId = jedis.get(MainConstant.userRoomProfix+user);
            if(jedis.exists(MainConstant.roomInfoProfix+roomId)){
                //房间未过期，返回该房间
                jedis.expire("room_"+roomId,3600*24);
                return roomId;

            }else{
                //房间已过期，进入下一步

            }
        }
        return null;
    }

    public String randomCreateRoom(){
        int start = (int)(Math.floor(Math.random()*100000000));
        int i = start;
        Jedis jedis = new Jedis("127.0.0.1",6379);
        while(jedis.exists("room_"+i)){
            i = (i+1)%100000000;
            if(start == i){
                //无空房间，报错
                return null;
            }
        }

        if(jedis.incr(MainConstant.lockProfix+i)==1){
            //确实没有冲突，定时销毁
            jedis.expire(MainConstant.lockProfix+i,60);
            return i+"";
        }else{
            //冲突
//            model.addAttribute("msg","网络繁忙");
//            return "homepage";
            return null;
        }
    }




}
