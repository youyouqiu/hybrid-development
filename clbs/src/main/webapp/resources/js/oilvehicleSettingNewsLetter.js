(function($,window){
    var vid = $("#readCommunicVehicleId").val();
    var sensorID = $("#poilBoxId").val();
    var sensorType = 0x41;
    var setting_id=$("#readCommunicid").val();
    var _timeout;
    var layer_time;
    //最后一次获取信息想MSGID
	newsletter = {
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
            newsletter.initF5ParamCall();
            url = '/clbs/v/oilmassmgt/oilvehiclesetting/getF3Param';
            if(sensorID=="41"){
                webSocket.subscribe(headers, "/user/topic/oil62785Info", newsletter.getSensor0104Param, null, null);
            }
            if (sensorID == "42") {
            	sensorType = 0x42;
                webSocket.subscribe(headers, "/user/topic/oil62786Info", newsletter.getSensor0104Param, null, null);
            }
            
            json_ajax("POST", url, "json", false, {
                "vid": vid,
                "sensorID": sensorType,
                "commandType": 0xF5
            }, newsletter.setRcSubjectCall);

		},//处理平台设置参数-初始化
        clearInputTextValue: function (data) {
            for(var i=0;i<data.length;i++){
                var id = "#"+data[i];
                $(id).val("");
            }
        },
        //创建消息监听
        createSocket0104InfoMonitor:function (msg) {
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
            _timeout= window.setTimeout(function () {
                if (isRead) {
                    isRead=false;
                    clearTimeout(layer_time);
                    layer.closeAll();
                    $("#readCommunicationRefresh").html("刷新").prop('disabled',false);
                    layer.msg("获取设备数据失败!");
                }
            },60000);
//            if(sensorID=="41"){
//                webSocket.subscribe(headers, "/user/" + msg.userName + "/oil62785Info", newsletter.getSensor0104Param, "/app/vehicle/oil/setting", requestStrS);
//            }
//            if (sensorID == "42") {
//                webSocket.subscribe(headers, "/user/" + msg.userName + "/oil62786Info", newsletter.getSensor0104Param, "/app/vehicle/oil/setting", requestStrS);
//            }
        },
        //获取通讯参数值
        readCommunicationReadValue: function () {
            srcId = $("#srcId").val();
            srcBaudRate = $("#srcBaudRate").val();
            srcParity = $("#srcParity").val();
            prcId = $("#prcId").val();
            prcBaudRate = $("#prcBaudRate").val();
            prcParity = $("#prcParity").val();
        },
        //通讯参数-刷新
        readCommunicationRefreshClick:  function () {
            newsletter.initF5ParamCall();
            if (sensorID != undefined && sensorID != null) {
                var url = '/clbs/v/oilmassmgt/oilvehiclesetting/getF3Param';
                json_ajax("POST", url, "json", false, {
                    "vid": vid,
                    "sensorID": sensorType,
                    "commandType": 0xF5
                }, newsletter.setRcSubjectCall);
            } else {
                layer.msg("该车辆没有绑定油箱");
            }
        },
        //通讯参数-订阅返回数据
        setRcSubjectCall:function (data) {
            if (!data.success) {
                //layer.msg(data.msg);
                layer.msg(data.msg,{move:false});
            }else{
                newsletter.createSocket0104InfoMonitor(data.msg);
                layer_time = window.setTimeout(function () {
                    // layer.load(2);
                    $("#readCommunicationRefresh").html("<i class='fa fa-spinner loading-state'></i>").prop('disabled',true);
                }, 0);
            }
        },
        //通讯参数-设备上报-初始化
        initF5ParamCall: function (temp_data) {
            var clearInputs=["srcId","srcBaudRate","srcParity"]
            newsletter.clearInputTextValue(clearInputs);
            $("#prcSendStatus").val("");//下发状态
        },
        //通讯参数-设备上报
        queryF5ParamCall: function (temp_data) {
            var paramsVaule= temp_data.data.msgBody.params[0].value;
            $("#srcId").val(paramsVaule.sensorID.toString(16));//传感器型号
            var baudRate =paramsVaule.baudRate;
            $("#srcBaudRate").val("2400");//波特率
            if(baudRate=='2'){
                $("#srcBaudRate").val("4800");//波特率
            }else if(baudRate=='3'){
                $("#srcBaudRate").val("9600");//波特率
            }else if(baudRate=='4'){
                $("#srcBaudRate").val("19200");//波特率
            }else if(baudRate=='5'){
                $("#srcBaudRate").val("38400");//波特率
            }else if(baudRate=='6'){
                $("#srcBaudRate").val("57600");//波特率
            }else if(baudRate=='7'){
                $("#srcBaudRate").val("115200");//波特率
            }
            $("#srcParity").val("无校验");//奇偶校验
            var oddEvenCheck =paramsVaule.oddEvenCheck;
            if(oddEvenCheck=='1'){
                $("#srcParity").val("奇校验");//波特率
            }else if(baudRate=='2'){
                $("#srcParity").val("偶校验");//波特率
            }
            //获取通讯参数值
            newsletter.readCommunicationReadValue();
			if (srcId != prcId) {
				$("#srcId").css("background-color", "#ffe6e6");
			}
			if (srcBaudRate != prcBaudRate) {
				$("#srcBaudRate").css("background-color", "#ffe6e6");
			}
			if (srcParity != prcParity) {
				$("#srcParity").css("background-color", "#ffe6e6");
			}
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
            var id =result.data.msgBody.params[0].id;
		    if(id=="62785"||id=="62786"){//通讯参数
				clearTimeout(layer_time);
	            layer.closeAll();
                $("#readCommunicationRefresh").html("刷新").prop('disabled',false);
	            newsletter.queryF5ParamCall(result);
			 	return;
		    }
        }
	}
	$(function(){
		$('input').inputClear();
		newsletter.init();
        $("#readCommunicationRefresh").on("click", newsletter.readCommunicationRefreshClick);
	})
})($,window)