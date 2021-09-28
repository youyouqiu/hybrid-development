(function($,window){
    var vid = $("#readConventionalVehicleId").val();
    var sensorID = $("#poilBoxId").val();
    var sensorType = 0x41;
    var setting_id=$("#setting_id").val();
    //var isRead =false;
    var _timeout;
    //最后一次获取信息想MSGID
	calibration = {
		init: function(){
            $(".modal-body").addClass("modal-body-overflow").css({"height":"auto","max-height":($(window).height()-194) +"px"});
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
            calibration.readCalibrationDataClick();

		},
		//标定数据(以传感器为准)
        rcdSubjectToTheSensorClick: function () {
            var cloneO = $("#fuelTankDataOne>tbody").children().clone();
            $("#fuelTankDataTwo>tbody").children().remove();
            $("#fuelTankDataTwo>tbody").append(cloneO);
            $("#bd_setting_type").val("report");
            $("#fuelTankDataOne").find("input").remove();
            $("#rcdSubjectToTheSensor,#rcdSubjectToPlatformSettings").attr("disabled", "disabled");
        },
        //标定数据-刷新
        rcdSubjectToTheSensorRefreshClick: function () {
            $("#bd_setting_type").val("pt");
            $("#fuelTankDataOneSendStatus").val("");
            $("#fuelTankDataOneSendStatus").removeClass($("#fuelTankDataOneSendStatus").attr("data-id"));
            $("#fuelTankDataOneSendStatus").removeAttr("data-id");
            $("#rcdSubjectToTheSensor").attr("disabled","disabled");
            $("#rcdSubjectToPlatformSettings").attr("disabled","disabled");
            $("#readCalibrationDataSend").attr("disabled","disabled");
            //订阅
            if(sensorType=="41"){
                webSocket.subscribe(headers, "/user/topic/oil63041Info", calibration.getSensor0104Param, null, null);
            }
            if (sensorType == "42") {
                webSocket.subscribe(headers, "/user/topic/oil63042Info", calibration.getSensor0104Param, null, null);
            }
            var url = '/clbs/v/oilmassmgt/oilvehiclesetting/getF3Param';
            $(".s_fuelTankDataTwo_body").html("");
            json_ajax("POST", url, "json", false, {
                "vid":vid,
                "sensorID": sensorType,
                "commandType": 0xF6
            }, calibration.setReadCalibrationCall);
        },
        //标定数据-获取设备数据返回
        setReadCalibrationCall: function (data) {
            if (data.success) {
                calibration.createSocket0104InfoMonitor(data.msg);
                // layer_time = setTimeout(function () {
                    // layer.load(2);
                    $("#readCalibrationDataSendRefresh").html("<i class='fa fa-spinner loading-state'></i>").prop('disabled',true);
                // }, 0);
            } else {
                layer.msg(data.msg);
            }
        },
        //标定数据-获取数据
        readCalibrationDataClick: function () {
            $("#bd_setting_type").val("pt");
            if (sensorID == undefined || sensorID == null) {
                layer.msg("该车辆没有绑定油箱");
                return;
            }
			if(sensorID=="41"){
                webSocket.subscribe(headers, "/user/topic/oil63041Info", calibration.getSensor0104Param, null, null);
            }
            if (sensorID == "42") {
            	sensorType = 0x42;
                webSocket.subscribe(headers, "/user/topic/oil63042Info", calibration.getSensor0104Param, null, null);
            }
			var url = '/clbs/v/oilmassmgt/oilvehiclesetting/getF3Param';
			json_ajax("POST", url, "json", false, {
				"vid": vid,
				"sensorID": sensorType,
				"commandType": 0xF6
			}, calibration.setReadCalibrationCall);

			$(".s_fuelTankDataTwo_body").html("");
			//读取平台标定数据
			var url = '/clbs/v/oilmassmgt/oilvehiclesetting/getOilCalibrationList';
			json_ajax("POST", url, "json", false, {
				"id": vid,
				"sensorID": sensorType
			}, calibration.setOilCalibrationCall);
        },
        //标定数据-设备上报
        queryF6ParamCall: function (temp_data) {
            var list = temp_data.data.msgBody.params[0].value.list;
            var phtml="";
            for(var i=0;i<list.length;i++){
                phtml+="<tr>";
                phtml+="<td class=\"text-center\">"+(i+1)+"</td>";
                phtml+="<td class=\"text-center\"><input type='hidden' name='oilLevelHeight' value='"+(list[i].key)+"'/>"+(list[i].key)+"</td>";
                phtml+="<td class=\"text-center\"><input type='hidden' name='oilValue' value='"+(list[i].value)+"'/>"+(list[i].value)+"</td>";
                phtml+="</tr>";
            }
            $(".s_fuelTankDataTwo_body").html(phtml);
            $("#rcdSubjectToTheSensor").removeAttr("disabled");
            $("#rcdSubjectToPlatformSettings").removeAttr("disabled");
            $("#readCalibrationDataSendRefresh").removeAttr("disabled");
            $("#readCalibrationDataSend").removeAttr("disabled");
        },
        //标定数据-平台数据
        setOilCalibrationCall: function(data){
            if (!data.success && data.msg == null) {
                layer.msg("该车辆没有平台标定数据");
                return;
            }else if(!data.success && data.msg != null){
            	layer.msg(data.msg,{move:false});
            	return;
            }
            var oclist = $.parseJSON(data.msg).settingList;
            var setting = $.parseJSON(data.msg).setting;
            $("#bd_setting_type").val("pt");
            var phtml;
            for(var i=0;i<oclist.length;i++){
                phtml+="<tr>";
                phtml+="<td class=\"text-center\">"+(i+1)+"</td>";
                phtml+="<td class=\"text-center\">"+(oclist[i].oilLevelHeight)+"</td>";
                phtml+="<td class=\"text-center\">"+(oclist[i].oilValue)+"</td>";
                phtml+="</tr>";
            }
            $(".p_fuelTankDataTwo_body").html(phtml);
        },
        //标定数据-修正下发-结果
        rcdSendSubjectToPlatformClick:function () {
            $("#readCalibrationDataForm").ajaxSubmit(function(data) {
                if (data != null) {
                    var result = $.parseJSON(data)
                    if (result.success) {
                        $("#rcdSubjectToTheSensor").attr("disabled","disabled");
                        $("#rcdSubjectToPlatformSettings").attr("disabled","disabled");
                        $("#readCalibrationDataSend").attr("disabled","disabled");
                        var msgid = $.parseJSON(result.msg).msgId;
                        var sendstatusname="send_status_"+msgid;
                        $("#fuelTankDataOneSendStatus").attr("data-id",sendstatusname);
                        $("#fuelTankDataOneSendStatus").addClass(sendstatusname);
                        $("#fuelTankDataOneSendStatus").val("参数下发中");//下发状态
                        calibration.createSocket0900StatusMonitor(result.msg);
                        // time_out = setTimeout(function () {
                            // layer.load(2);
                            $("#readCalibrationDataSendRefresh").html("<i class='fa fa-spinner loading-state'></i>").prop('disabled',true);
                        // }, 0);
                        myTable.refresh();
                    } else{
                        layer.msg(result.msg,{move:false});
                        myTable.refresh();
                    }
                }
            });
        },
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
            webSocket.subscribe(headers, "/user/topic/t808_currency_response", calibration.currencyResponse, "/app/vehicle/oil/setting", requestStrS);
            webSocket.subscribe(headers, "/user/topic/deviceReportLog", calibration.deviceReportLog, "/app/vehicle/oil/setting", requestStrS);
        },
        //标定数据(以平台设置为准)
        rcdSubjectToPlatformSettingsClick: function () {
            var cloneT = $("#fuelTankDataTwo>tbody").children().clone();
            $("#fuelTankDataOne>tbody").children().remove();
            $("#fuelTankDataOne>tbody").append(cloneT);
            $("#rcdSubjectToTheSensor,#rcdSubjectToPlatformSettings").attr("disabled", "disabled");
        }
        ,
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
                $("#readCalibrationDataSendRefresh").html("刷新").prop('disabled',false);
                $("#rcdSubjectToTheSensor").removeAttr("disabled");
                $("#rcdSubjectToPlatformSettings").removeAttr("disabled");
                $("#readCalibrationDataSendRefresh").removeAttr("disabled");
                $("#readCalibrationDataSend").removeAttr("disabled");
                $(".send_status_"+msgid).val("参数下发失败");
            }
            return;
        },
        //处理获取设备下发的结果
        deviceReportLog:function (msg) {
            if (msg == null)
                return;
            var result = $.parseJSON(msg.body);
            var type =result.data.msgBody.type;
            if(type==243){
                layer.closeAll();
                $("#readCalibrationDataSendRefresh").html("刷新").prop('disabled',false);
                var msgid =result.data.msgBody.ackMSN;
                var result =result.data.msgBody.result;
                $("#rcdSubjectToTheSensor").removeAttr("disabled");
                $("#rcdSubjectToPlatformSettings").removeAttr("disabled");
                $("#readCalibrationDataSendRefresh").removeAttr("disabled");
                $("#readCalibrationDataSend").removeAttr("disabled");
                if(result==0||result=="0"){
                    $(".send_status_"+msgid).val("参数生效");
                }else{
                    $(".send_status_"+msgid).val("参数未生效");
                }
                return;
            }
            $("#private_parameter_result_str").val(result.data_result);
            myTable.refresh();
            return;
        },
		//创建消息监听
        createSocket0104InfoMonitor:function (msg) {
        	if (msg == null || msg == undefined) {
        		return;
        	}
            var msg = $.parseJSON(msg);
            var requestStrS = {
                "desc": {
                    "cmsgSN": msg.msgId,
                    "UserName": msg.userName
                },
                "data": []
            };
            $("#temp_send_vehicle_msg_id").val(msg.msgId);
            headers = {"UserName": msg.userName};
            isRead=true;
            clearTimeout(_timeout);
            _timeout= setTimeout(function () {
                if (isRead) {
                    isRead=false;
                    layer.closeAll();
                    $("#readCalibrationDataSendRefresh").html("刷新").prop('disabled',false);
                    layer.msg("获取设备数据失败!");
                }
            },60000);
//            if(sensorID=="41"){
//                webSocket.subscribe(headers, "/user/" + msg.userName + "/oil63041Info", calibration.getSensor0104Param, "/app/vehicle/oil/setting", requestStrS);
//            }
//            if (sensorID == "42") {
//                webSocket.subscribe(headers, "/user/" + msg.userName + "/oil63042Info", calibration.getSensor0104Param, "/app/vehicle/oil/setting", requestStrS);
//            }
		},
        //处理获取设备上传数据
        getSensor0104Param:function (msg) {
            if (msg == null || !isRead)
                return;
            var result = $.parseJSON(msg.body);
            var msgSNAck = result.data.msgBody.msgSNAck;

            if (msgSNAck != $("#temp_send_vehicle_msg_id").val()) {
                return;
            }
            isRead=false;
            clearTimeout(_timeout);
            layer.closeAll();
            $("#readCalibrationDataSendRefresh").html("刷新").prop('disabled',false);
            var id =result.data.msgBody.params[0].id
            if(id=="63041"||id=="63042"){//标定参数
                layer.closeAll();
                $("#readCalibrationDataSendRefresh").html("刷新").prop('disabled',false);
                calibration.queryF6ParamCall(result);
                return;
            }
        }
	}
	$(function(){
		calibration.init();
		//标定数据
        $("#readCalibrationDataSendRefresh").on("click", calibration.rcdSubjectToTheSensorRefreshClick);
        $("#rcdSubjectToTheSensor").on("click", calibration.rcdSubjectToTheSensorClick);
        $("#rcdSubjectToPlatformSettings").on("click", calibration.rcdSubjectToPlatformSettingsClick);
        $("#readCalibrationDataSend").on("click", calibration.rcdSendSubjectToPlatformClick);
	})
})($,window)