(function ($, window) {
    var vid = $("#vid").val();
    var brand = $("#brand").val();

    var isRead = false;
    var _timeout_load;
    var params = [];

    obdManagerSettingParameter = {
        init:function(){
            $(".modal-body").addClass("modal-body-overflow");
            $(".modal-body").css({"height":"auto","max-height":($(window).height()-194) +"px"});

            $("#paramTitle").html('查看OBD数据：' + brand);
            // 请求后台，获取所有订阅的车
            // webSocket.init('/clbs/vehicle');
            //监听窗口关闭事件，当窗口闭关时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
            window.onbeforeunload = function () {
                var cancelStrS = {
                    "desc": {
                        "MsgId": 40964,
                        "UserName": $("#userName").text()
                    },
                    "data": params
                };
                webSocket.unsubscribealarm(headers, "/app/vehicle/unsubscribestatus", cancelStrS);
            }
            var requestStrS = {
                "desc": {
                    "MsgId": 40964,
                    "UserName": $("#userName").text()
                },
                "data": params
            };
            webSocket.subscribe(headers, "/user/topic/realLocation", obdManagerSettingParameter.getSensor0104Param,"/app/vehicle/realLocation", requestStrS);
            var url = '/clbs/v/obdManager/obdManagerSetting/getF3Param';
            json_ajax("POST", url, "json", false, {
                "vid": vid,
                "sensorID": 0xE5,
                "commandType": 2
            },obdManagerSettingParameter.getF3BaseParamCall);
         },

        refreshData: function () {
            var url = '/clbs/v/obdManager/obdManagerSetting/getF3Param';
            json_ajax("POST", url, "json", false, {
                "vid": vid,
                "sensorID": 0xE5,
                "commandType": 2
            },obdManagerSettingParameter.getF3BaseParamCall);
        },

        //基本信息-下发获取基本信息返回处理方法
        getF3BaseParamCall: function (data) {
            if (!data.success) {
                layer.msg(data.msg);
            }else{
                obdManagerSettingParameter.createSocket0104InfoMonitor(data.msg);
                setTimeout(function () {
                    // layer.load(2);
                    $("#parameterRefresh").html("<i class='fa fa-spinner loading-state'></i>").prop('disabled',true);
                }, 0);
            }
        },

        getSensor0104Param: function(data){
            if (data != null) {
                var result = JSON.parse(data.body);
                if (result != undefined && result != null) {
                    layer.closeAll();
                    $("#parameterRefresh").html("刷新").prop('disabled',false);
                    obdManagerSettingParameter.returnObdInfo(result);
                }
            }
        },

        //创建消息监听
        createSocket0104InfoMonitor: function (msg) {
            var msg = $.parseJSON(msg);
            isRead = true;
            clearTimeout(_timeout_load);
            _timeout_load = window.setTimeout(function () {
                if (isRead) {
                    isRead = false;
                    layer.closeAll();
                    $("#parameterRefresh").html("刷新").prop('disabled', false);
                    layer.msg("获取设备数据失败!");
                }
            }, 60000);
        },

        // 修改OBD信息
        returnObdInfo: function (data) {
            clearTimeout(_timeout_load);
            isRead=false;
            var info = data.data.msgBody.obdObjStr;
            var newHtml = '';
            if(info != null){
                var arr = JSON.parse(info);
                var len = arr.length;
                if(len>0){
                    for (var i = 0; i < len; i++) {
                        var s = arr[i].value ? arr[i].value : '';
                        newHtml += '<tr><td>' + arr[i].name + '</td><td>' + s + '</td></tr>';
                    }
                }else{
                    newHtml='<tr><td>无信息</td></tr>'
                }
            }else{
                newHtml='<tr><td>无信息</td></tr>'
            }

            $('#paramTbody').html(newHtml);
        },
    };
    $(function () {
        obdManagerSettingParameter.init();
        $("#parameterRefresh").on("click", obdManagerSettingParameter.refreshData);
    })
})($, window)