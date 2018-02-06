<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<script src="https://cdn.bootcss.com/jquery/1.12.4/jquery.min.js"></script>
<html>
<body>
<h2>Hello World!</h2>



<form action="rngs" method="post" id="identities">
    村民：<input type="number" name="village"><br><br>
    狼人：<input type="number" name="lycan"><br><br>
    <table>
        <tr>
            <td><input type="checkbox" name="defaults" value="猎人" />猎人</td>
            <td><input type="checkbox" name="defaults" value="预言家" />预言家</td>
        </tr>
        <tr>
            <td><input type="checkbox" name="defaults" value="女巫" />女巫</td>
            <td><input type="checkbox" name="defaults" value="白痴" />白痴</td>
        </tr>
        <tr>
            <td><input type="checkbox" name="defaults" value="守卫" />守卫</td>
            <td><input type="checkbox" name="defaults" value="白狼王" />白狼王</td>
        </tr>
        <tr>
            <div style="line-height: 3px" id = "customList">

            </div>

        </tr>
        <tr>
            <td><input type="button" onclick="addInput()" value="添加"></input> </td>
            <td><input type="submit" value="确定"></td>
        </tr>
    </table>


    <table>

    </table>
</form>
<br>

</body>
<script type="text/javascript">

    var i = 0;
    function addInput() {
        var tt = document.getElementById("customList");

        var o = document.createElement('input');
        o.type = 'text';
        o.name = 'customs';
        o.value = '自定义角色' + i;
        o.id = 'customName'+i;
        o.required = true;
        tt.appendChild(o);
        var dd = document.createElement("h");
        dd.innerHTML= '<input type="button" id="'+'customDelete'+i+'" value = "删除" onclick="deleteCustom('+i+');" />';
        // o = document.createElement('input');
        // o.type = 'button';
        // o.value = '删除';
        // o.id = 'customDelete'+i;
        // if (o.attachEvent) {
        //     o.attachEvent('onclick', deleteCustom(i))
        // } else {
        //     o.addEventListener('click', deleteCustom(i))
        // }
        tt.appendChild(dd);
        o.add
        o = document.createElement("div");
        o.innerHTML = "<br/>";
        tt.appendChild(o);
        o = null;
        i = i+1;

    }

    function deleteCustom(var1) {
        document.getElementById("customName"+var1).remove();
        document.getElementById("customDelete"+var1).remove();
        // $("customDelete"+p).remove();

    }
        // if(o.attachEvent){
        //     o.attachEvent('onclick',addInput)
        // }else{
        //     o.addEventListener('click',addInput)
        // }
        // document.body.appendChild(o);
        // $.ajax({
        //     url: "/user/submitUserList_3",
        //     type: "POST",
        //     contentType : 'application/json;charset=utf-8', //设置请求头信息
        //     dataType:"json",
        //     //data: JSON.stringify(customerArray),    //将Json对象序列化成Json字符串，JSON.stringify()原生态方法
        //     data: $.toJSON(customerArray),            //将Json对象序列化成Json字符串，toJSON()需要引用jquery.json.min.js
        //     success: function(data){
        //         alert(data);
        //     },
        //     error: function(res){
        //         alert(res.responseText);
        //     }
        // });


        // (function($){
        //     $.fn.serializeJson = function(){
        //         var jsonData1 = {};
        //         var serializeArray = this.serializeArray();
        //         // 先转换成{"id": ["12","14"], "name": ["aaa","bbb"], "pwd":["pwd1","pwd2"]}这种形式
        //         $(serializeArray).each(function () {
        //             if (jsonData1[this.name]) {
        //                 if ($.isArray(jsonData1[this.name])) {
        //                     jsonData1[this.name].push(this.value);
        //                 } else {
        //                     jsonData1[this.name] = [jsonData1[this.name], this.value];
        //                 }
        //             } else {
        //                 jsonData1[this.name] = this.value;
        //             }
        //         });
        //         // 再转成[{"id": "12", "name": "aaa", "pwd":"pwd1"},{"id": "14", "name": "bb", "pwd":"pwd2"}]的形式
        //         var vCount = 0;
        //         // 计算json内部的数组最大长度
        //         for(var item in jsonData1){
        //             var tmp = $.isArray(jsonData1[item]) ? jsonData1[item].length : 1;
        //             vCount = (tmp > vCount) ? tmp : vCount;
        //         }
        //
        //         if(vCount > 1) {
        //             var jsonData2 = new Array();
        //             for(var i = 0; i < vCount; i++){
        //                 var jsonObj = {};
        //                 for(var item in jsonData1) {
        //                     jsonObj[item] = jsonData1[item][i];
        //                 }
        //                 jsonData2.push(jsonObj);
        //             }
        //             return JSON.stringify(jsonData2);
        //         }else{
        //             return "[" + JSON.stringify(jsonData1) + "]";
        //         }
        //     };
        // })(jQuery);
        //
        // function submitUserList_4() {alert("ok");
        //     var jsonStr = $("#form1").serializeJson();
        //     //console.log("jsonStr:\r\n" + jsonStr);
        //     //alert(jsonStr);
        //     $.ajax({
        //         url: "/user/submitUserList_4",
        //         type: "POST",
        //         contentType : 'application/json;charset=utf-8', //设置请求头信息
        //         dataType:"json",
        //         data: jsonStr,
        //         success: function(data){
        //             alert(data);
        //         },
        //         error: function(res){
        //             alert(res.responseText);
        //         }
        //     });
        // }


</script>
</html>
