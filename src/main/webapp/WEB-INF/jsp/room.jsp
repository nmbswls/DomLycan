<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<script src="https://cdn.bootcss.com/jquery/1.12.4/jquery.min.js"></script>
<html>
<body>
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
<input type="button" value="洗牌" onclick="shuffle()">
<input type="button" value="上帝视角">
<input type="button" value="角色配置" onclick="reCreate()">


</body>
<script>
    function shuffle(){
        window.location="./shuffle?roomId="+${roomId};
    }

    function reCreate() {
        window.location="./reCreate?roomId="+${roomId};
    }
    // function getRoom(id){
    //     window.location="./getRoom?roomId="+id;
    // }

</script>

</html>
