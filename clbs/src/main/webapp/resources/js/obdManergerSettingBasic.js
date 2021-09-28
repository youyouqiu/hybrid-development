(function($,window){
    var isRead =false;
    var _timeout;
    var clearInputs =["terminal-vehicleType","srcBaudRate","terminal-vehicleCode","terminal-vehicleTime"];
    var vid = $("#vid").val();
	basicInfo = {
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
            };
            basicInfo.clearInputTextValue(clearInputs);
            // 订阅
        	webSocket.subscribe(headers, "/user/topic/oil61669Info", basicInfo.getSensor0104Param,null, null);
            var url = '/clbs/v/obdManager/obdManagerSetting/getF3Param';
            json_ajax("POST", url, "json", false, {
                "vid": vid,
                "sensorID": 0xE5,
                "commandType": 0xF0
            },basicInfo.getF3BaseParamCall);
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
            	basicInfo.createSocket0104InfoMonitor(data.msg);
                setTimeout(function () {
                    // layer.load(2);
                    $("#readInformationRefresh").html("<i class='fa fa-spinner loading-state'></i>").prop('disabled',true);
                }, 0);
            }
        },
        //基本信息-刷新
        readInformationRefreshClick:function () {

            basicInfo.clearInputTextValue(clearInputs);
            var url = '/clbs/v/obdManager/obdManagerSetting/getF3Param';
            json_ajax("POST", url, "json", false, {
                "vid": vid,
                "sensorID": 0xE5,
                "commandType": 0xF0
            },basicInfo.getF3BaseParamCall);
        },
        //创建消息监听
        createSocket0104InfoMonitor:function (msg) {
            var msg = $.parseJSON(msg);
            isRead =true;
            clearTimeout(_timeout);
            _timeout=window.setTimeout(function () {
                if(isRead){
                    isRead=false;
                    layer.closeAll();
                    $("#readInformationRefresh").html("刷新").prop('disabled',false);
                    layer.msg("获取设备数据失败!");
                }
            },60000);
        },
        //处理获取设备上传数据
        getSensor0104Param:function (msg) {
            if (msg == null)
                return;
            var result = $.parseJSON(msg.body);
            isRead=false;
            clearTimeout(_timeout);
            var id =result.data.msgBody.params[0].id;
            if(id=="61669"){//基本信息
                layer.closeAll();
                $("#readInformationRefresh").html("刷新").prop('disabled',false);
            	basicInfo.queryF3BaseParamCall(result.data.msgBody.params[0].value);
            }
        },
        //基本信息-上报获取基本信息返回处理方法
        queryF3BaseParamCall: function (baseValue) {
            var code16 = baseValue.setStreamObd.vehicleTypeId.toString(16).toUpperCase();
            var len = code16.length;
            for(var i=len;i<9;i++){
                if(code16.length < 8){
                    code16 = "0" + code16;
                }
            }
            code16 = "0x" + code16;
            $("#terminal-vehicleCode").val(code16);//车型ID
            var time = "";
            if(baseValue.setStreamObd.uploadTime != null && baseValue.setStreamObd.uploadTime != -1 ){
                time = parseInt(baseValue.setStreamObd.uploadTime)/1000;
            }
            $("#terminal-vehicleTime").val(time);//OBD采集间隔(秒)

            console.log('信息',baseValue.setStreamObd);
            //根据返回的code去平台查询型号
            if(baseValue.setStreamObd.vehicleTypeId !== null && baseValue.setStreamObd.vehicleTypeId !== ""){
                var url = '/clbs/v/obdManager/obdVehicleType/findByCode';
                json_ajax("POST", url, "json", false, {
                    "code":code16
                },function(data){
                    console.log('信息',data);
                    if(!data.success){
                        if(data.msg != null && data.msg != ''){
                            layer.msg(data.msg);
                            return;
                        }
                    }
                    if(data.obj != null && data.obj.length > 0){
                        $("#srcBaudRate").val(data.obj[0].name);
                        if(data.obj[0].type == 0 ){
                            $("#terminal-vehicleType").val("乘用车");//车型分类
                        }else if(data.obj[0].type == 1){
                            $("#terminal-vehicleType").val("商用车");//车型分类
                        }
                    }
                });
            }
        },
	}
	$(function(){
		 basicInfo.init();
		//刷新
        $("#readInformationRefresh").on("click", basicInfo.readInformationRefreshClick);
	})
})($,window)