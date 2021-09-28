(function($,window){
    var vid = $("#vid").val();
    var brand = $("#brand").val();
    var clearInputs =["uploadTime","faultCode","description"];
    var isRead =false;
    var _timeout;
    var params = [];

    obdManagerSettingFault = {

        init: function(){
            // 请求后台，获取所有订阅的车
            // webSocket.init('/clbs/vehicle');
            //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
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
            webSocket.subscribe(headers, "/user/topic/obdFault", obdManagerSettingFault.getSensor0104Param,"/app/vehicle/realLocation", requestStrS);
            obdManagerSettingFault.clearInputTextValue(clearInputs);
            var url = '/clbs/v/obdManager/obdManagerSetting/getF3Param';
            json_ajax("POST", url, "json", false, {
                "vid": vid,
                "sensorID": 0xE5,
                "commandType": 1
            },obdManagerSettingFault.getF3BaseParamCall);
        },

        refreshData:function () {
            obdManagerSettingFault.clearInputTextValue(clearInputs);
            var url = '/clbs/v/obdManager/obdManagerSetting/getF3Param';
            json_ajax("POST", url, "json", false, {
                "vid": vid,
                "sensorID": 0xE5,
                "commandType": 1
            },obdManagerSettingFault.getF3BaseParamCall);
        },

        //处理平台设置参数-初始化
        clearInputTextValue: function (data) {
            for(var i=0;i<data.length;i++){
                var id = "#"+data[i];
                $(id).val("");
            }
        },

        //基本信息-下发获取基本信息返回处理方法
        getF3BaseParamCall: function (data) {
            if (!data.success) {
                layer.msg(data.msg);
            }else{
                obdManagerSettingFault.createSocket0104InfoMonitor(data.msg);
                setTimeout(function () {
                    // layer.load(2);
                    $("#faultRefresh").html("<i class='fa fa-spinner loading-state'></i>").prop('disabled',true);
                }, 0);
            }
        },

        //创建消息监听
        createSocket0104InfoMonitor:function (msg) {
            isRead =true;
            clearTimeout(_timeout);
            _timeout=window.setTimeout(function () {
                if(isRead){
                    isRead=false;
                    layer.closeAll();
                    $("#faultRefresh").html("刷新").prop('disabled',false);
                    layer.msg("获取设备数据失败!");
                }
            },60000);
        },

        //处理获取设备上传数据
        getSensor0104Param:function (msg) {
            if (msg == null)
                return;
            var result = $.parseJSON(msg.body);
            var obd = result.msgBody;
            isRead=false;
            clearTimeout(_timeout);
            if(obd != null && obd != ""){
                layer.closeAll();
                $("#faultRefresh").html("刷新").prop('disabled',false);
                $("#uploadTime").val(obd.uploadTime ? obd.uploadTime:"");
                $("#faultCode").val(obd.faultCodes ? obd.faultCodes:"");
                $("#description").val(obd.faultDescriptions ? obd.faultDescriptions:"");
                return;
            }else {
                layer.closeAll();
                $("#faultRefresh").html("刷新").prop('disabled',false);
                $("#uploadTime").val("未获得数据");
                $("#faultCode").val("未获得数据");
                $("#description").val("");
                return;
            }
        },
    };
    $(function(){
        obdManagerSettingFault.init();
        $("#faultTitle").html('查看故障码：' + brand);
        $("#faultRefresh").on("click", obdManagerSettingFault.refreshData);
    })
})($,window)
