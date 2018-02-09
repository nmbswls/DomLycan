<%@ page import="com.constant.MainConstant" %>
<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@page import="com.constant.MainConstant" %>
<script src="https://cdn.bootcss.com/jquery/1.12.4/jquery.min.js"></script>
<html>
<body>

<%
    String[] ss = MainConstant.turnArrange;
    request.setAttribute("actTurn",ss);
%>


<script>


    <%--<c:forEach items="${indexes}" var="act">--%>

    <%--</c:forEach>--%>
</script>

<script>

</script>
当前房间：${roomId}<br>
座位号：${seatId}<br>
身份：${role}<br>
<div>
    <c:choose>
        <c:when test="${role=='狼人'}">

            <div>
                <input type="number" name="killNo" id="killNo"/>
                <input type="button" value="击杀" onclick="kill()"/>
            </div>
            <div id="killInfo">

            </div>
            <script>

                function kill(){
                    var div = $("#killInfo");
                    $.ajax({
                        url: "http://localhost:8080/test/kill?roomId=${roomId}&seatId=${seatId}",
                        type: "POST",
                        dataType: "json",

                        data: {
                            "kill": $("#killNo").val()
                        },
                        async: true,
                        success: function(data) {
                            alert(data);
                            var jsons = data;
                            div.html("");
                            for(var i = 0; i < jsons.length; i++)
                            {
                                div.append("<div>"+jsons[i]+"</div>");
                            }
                        },
                        error: function(XMLHttpRequest, textStatus, errorThrown) {
                            alert(XMLHttpRequest.status);
                            alert(XMLHttpRequest.readyState);
                            alert(textStatus);
                        }
                    });
                }
            </script>



        </c:when>

        <c:when test="${role == '预言家'}">
            <input type="number" name="foresee_number"/>
            <input type="button" value="悍跳"/>
        </c:when>

        <c:when test="${role == '女巫'}">
            <input type="number" name="poison_number"/>
            <input type="button" value="悍跳"/>
        </c:when>

        <c:otherwise>
            闭眼角色
        </c:otherwise>
    </c:choose>


</div>
<br>
<c:if test="${isHost=='Y'}">
    <input type="button" value="洗牌" onclick="shuffle()">
    <input type="button" value="角色配置" onclick="reCreate()">
    <input type="button" value="开始/继续黑夜" onclick="nightStart()"><br>
    <c:forEach items="${actTurn}" var="actRoleName">
        <p>${actRoleName}时间:<input type="number" value="30" id="${actRoleName}Time"/>秒</p>
    </c:forEach>

    <script>
        var turn = 0;
        function turnGo(){
            $.ajax({
                url: "http://localhost:8080/test/turnGo?roomId=${roomId}",
                type: "Get",
                dataType: "text",
                async: false,
                success: function(data) {
                    alert(data+"请睁眼");
                },
                error: function(XMLHttpRequest, textStatus, errorThrown) {
                    alert(XMLHttpRequest.status);
                    alert(XMLHttpRequest.readyState);
                    alert(textStatus);
                }
            });
        }
        function turnFinish(){
            $.ajax({
                url: "http://localhost:8080/test/turnFinish?roomId=${roomId}",
                type: "Get",
                dataType: "text",
                async: false,
                success: function(data) {
                    alert("行动结算");
                },
                error: function(XMLHttpRequest, textStatus, errorThrown) {
                    alert(XMLHttpRequest.status);
                    alert(XMLHttpRequest.readyState);
                    alert(textStatus);
                }
            });
        }


        function nightStart(){
            var i = 0;
            var turnTime = new Array();
            turnTime[0]=3000;
            turnTime[1]=6000;
            turnTime[2]=9000;
            turnTime[3]=12000;
            while(i<4){
                setTimeout(function(){turnGo();},turnTime[i]);
                i++;
            }
            setTimeout(function(){turnFinish();},turnTime[13000]);

        }


    </script>
</c:if>

<input type="button" value="上帝视角" onclick="godView()">
<input type="button" value="夜间死讯" onclick="deadInfo()">

</body>
<script>


    function deadInfo(){

    }


    function godView(){

    }
    function shuffle(){
        if(confirm("是否确认洗牌")){
            window.location="./shuffle?roomId="+${roomId};
        }


    }

    function reCreate() {
        if(confirm("是否确认重新配置角色")){
            window.location="./reCreate?roomId="+${roomId};
        }
    }
    // function getRoom(id){
    //     window.location="./getRoom?roomId="+id;
    // }

    var turn = ${turn}

</script>

</html>
