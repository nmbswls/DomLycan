<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<script src="https://cdn.bootcss.com/jquery/1.12.4/jquery.min.js"></script>
<html>
<body>
<h2>选择房间</h2>

<br>
<form action="getRoleDesc" method="post">
    房间号：<input type="number" name = "roomId" id="roomId" required="required"/><br>
    座号：<input type="number" name = "seatId" id="seatId" required="required"/>

    <input type="submit" value="进入">
</form>


</body>
<script>
    // function getRoom(id){
    //     window.location="./getRoom?roomId="+id;
    // }


        $(document).ready(function(){
            $("#roomId").val();
            <c:if test="${roomId!=null}">
                $("#roomId").val(${roomId});
            </c:if>
        });




</script>

</html>
