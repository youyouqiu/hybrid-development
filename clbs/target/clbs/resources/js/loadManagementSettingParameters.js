(function($,window){
    var vid = $("#pr_vehicle_id").val();
    var sensorID = $("#pr_sensor_type").val();
    var sensorType = 0x70;
    var onLineStatus = false; // 标识车辆是否在线
	parameters = {
		init: function(){
            parameters.privateParameterSettingsClick();
		},
		//私有参数
        privateParameterSettingsClick: function () {
            $("#privateVid").val(vid);
            if (sensorID == "1") {
                sensorType = 0x71;
            }
            $("#private_parameter_result_str").val("");
            parameters.checkVehicleOnlineStatus();
        },
        //私有参数-判断设备是否在线
        privateParameterSettingsCall:function (data) {
            if (!data.success) {
                layer.msg(data.msg,{move:false});
                return;
            }
        }// 判断车辆是否在线
        ,checkVehicleOnlineStatus : function () {
            var url = "/clbs/v/oilmassmgt/oilcalibration/checkVehicleOnlineStatus";
            var data = {"vehicleId" :vid};
            json_ajax("POST", url, "json", false, data, parameters.checkVehicleOnlineStatusCallBack);
        },
        // 判断车辆是否在线回调
        checkVehicleOnlineStatusCallBack : function (data) {
            if (data.success) {
                onLineStatus = true; // 在线
                return true;
            }else if (!data.success && data.msg == null) {
                onLineStatus = false; // 不在线
                layer.msg("终端离线");
                return false;
            }else if (!data.success && data.msg != null){
                layer.msg(data.msg,{move:false});
            }
        },
        //私有参数下发
        privateParameterSettingsSend: function () {
            if(!onLineStatus){
                layer.msg("终端离线，暂不支持私有参数设置");
                return;
            }
            var commandStr = $("#commandStr").val().trim();
            if(commandStr==''){
                layer.msg("请设置下发内容");
                return;
            }
            //var checkStr = /^[A-Fa-f0-9]{1}$/;
            var checkStr = /^[0-9]{1}$/;
            var commandTStr = commandStr.toString().replace(/[ ]/g,"");
            var blank = commandStr.length - commandTStr.length;
            if(commandTStr.length % 2 != 0 || blank != commandTStr.length / 2 - 1){
                layer.msg("下发内容不是合法格式");
                return;
            }
            for (var i = 0, m = commandTStr.length; i < m; i++) {
                var str = commandTStr.substr(i, 1);
                if(!checkStr.test(str)){
                    layer.msg("下发内容出现非法字符:["+str+"]");
                    return;
                }
            }
            var url = '/clbs//v/loadmgt/loadvehiclesetting/getF3PrivateParam';
            json_ajax("POST", url, "json", false, {
                "vid": vid,
                "sensorID": sensorType,
                "commandStr": commandStr
            }, parameters.privateParameterSettings);
        },
        //私有参数下发-结果
        privateParameterSettings: function (data) {
            if (data != null) {
                if (data.success) {
                    var msgid = $.parseJSON(data.msg).msgId;
                    var sendstatusname="send_status_"+msgid;
                    $("#private_parameter_result_str").attr("data-id",sendstatusname);
                    $("#private_parameter_result_str").addClass(sendstatusname);
                    $("#private_parameter_result_str").val("参数下发中");//下发状态
                    parameters.createSocket0900StatusMonitor(data.msg);
                    setTimeout(function () {
                        layer.load(2);
                    }, 0);
                } else{
                    layer.msg(data.msg,{move:false});
                }
            }
        },
        //创建消息监听
        createSocket0900StatusMonitor:function (msg) {
            var msg = $.parseJSON(msg);
            var requestStrS = {
                "desc": {
                    "cmsgSN": msg.msgId,
                    "UserName": msg.userName
                },
                "data": []
            };
            headers = {"UserName": msg.userName};
            webSocket.subscribe(headers, "/user/topic/t808_currency_response", parameters.currencyResponse, "/app/vehicle/oil/setting", requestStrS);
            webSocket.subscribe(headers, "/user/topic/deviceReportLog", parameters.deviceReportLog, "/app/vehicle/oil/setting", requestStrS);
        },
        //处理获取设备下发的结果
        currencyResponse:function (msg) {
            if (msg == null)
                return;
            var result = $.parseJSON(msg.body);
            var msgid =result.data.msgBody.msgSNACK;
            var status =result.data.msgBody.result;
            if(status==0){
                $(".send_status_"+msgid).val("终端处理中");
            }
            if(status==1||status==2||status==3){
                layer.closeAll();
                $(".send_status_"+msgid).val("参数下发失败");
            }
            return;
        },
        //处理获取设备下发的结果
        deviceReportLog:function (msg) {
            if (msg == null)
                return;
            var result = $.parseJSON(msg.body);
            $("#private_parameter_result_str").val(result.data.msgBody.data_result);
            myTable.refresh();
            return;
        },
	}
	$(function(){
		$('input').inputClear();
		parameters.init();
	})
})($,window)