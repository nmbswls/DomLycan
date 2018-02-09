<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<script src="https://cdn.bootcss.com/jquery/1.12.4/jquery.min.js"></script>
<html>
<body>
<h2>请先登录！</h2>
<form action="login">
    手机号：<input type="number" value="" name="phoneNumber"><br>
    验证码：<input type="text" value="" name="validCode"><br>
    <input type="submit" value="登录">
</form>
<br>
<div>${msg}</div>
</body>
</html>
