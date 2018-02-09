<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<script src="https://cdn.bootcss.com/jquery/1.12.4/jquery.min.js"></script>
<%
        Cookie cookies[]=request.getCookies(); //读出用户硬盘上的Cookie，并将所有的Cookie放到一个cookie对象数组里面
        Cookie sCookie=null;
        pageContext.setAttribute("userToken","");
        for(int i=0;i<cookies.length-1;i++){    //用一个循环语句遍历刚才建立的Cookie对象数组
            sCookie=cookies[i];   //取出数组中的一个Cookie对象
            if(sCookie!=null){
                if(("userToken").equals(sCookie.getName())){
                    pageContext.setAttribute("userToken",sCookie.getValue());
                }
            }
        }
//
//        //保存用户名到cookies
//
//        String user_name=request.getParameter("user_name");
//        if(!"".equals(user_name) && request.getParameter("RmbUser")!=null){
//        Cookie cookie=new Cookie("usernamecookie",user_name);
//        cookie.setMaxAge(365*24*60*60); //保存365天
//        response.addCookie(cookie);
%>
<html>

<body>


<input type="button" value="创建房间" onclick="logValidation()"/>

<br>
<a href="./enterRoom" style="text-decoration: none">
    <input type="button" value="进入房间"/>
</a>

<p>${userToken}</p>
<p>${msg}</p>

</body>
<script>

    function logValidation(){
        //判断是否要登录
        window.location="./logValidation";
    }


</script>
</html>
