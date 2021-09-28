(function($,window){
    var vid = $("#readConventionalVehicleId").val();
    var setting_id=$("#setting_id").val();
    var _timeout;
    var layer_time;
    //最后一次获取信息想MSGID
	general = {
		init: function(){
			$('#pid').val(45);
            $("#readConventionalsSend").attr("disabled","disabled");
            $("#rclSubjectToTheSensor").attr("disabled","disabled");
            $("#rclSubjectToPlatformSettings").attr("disabled","disabled");
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
            general.initF4ParamCall();
            // 订阅
            webSocket.subscribe(headers, "/user/topic/oil62533Info", general.getSensor0104Param, null, null);
            url = '/clbs/v/oilmgt/fluxsensorbind/getF3Param';
            json_ajax("POST", url, "json", false, {
                "vid": vid,
                "sensorID": 0x45,
                "commandType": 0xF4
            }, general.setreadConventionalCall);
		},
    	//传感器常规参数取值
       	conventionalReadVal: function () {
            //传感器常规参数
            rcNumberPlate = $("#rcNumberPlate").val();
            rcSensorModel = $("#rcSensorModel").val();
            //1.传感器上报参数取值
            sid = $("#sid").val();
            scompensation = $("#scompensation").val();
            sfilteringFactor = $("#sfilteringFactor").val();
            suploadTime = $("#suploadTime").val();
            sk = $("#sk").val();
            sb = $("#sb").val();
            //2.平台设置参数
            pid = $("#pid").val();
            pcompensation = $("#pcompensation").val();
            pfilteringFactor = $("#pfilteringFactor").val();
            puploadTime = $("#puploadTime").val();
            pk = $("#pk").val();
            pb = $("#pb").val();
        },
        //传感器常规参数-获取信息-刷新
        readConventionalRefreshClick: function () {
            $("#poilSendStatus").val("");//下发状态
            $("#poilSendStatus").removeAttr("data-id");
            var vid = $("#readConventionalVehicleId").val();
            $("#readConventionalsSend").attr("disabled","disabled");
            $("#rclSubjectToTheSensor").attr("disabled","disabled");
            $("#rclSubjectToPlatformSettings").attr("disabled","disabled");
            general.initF4ParamCall();
            general.initReadConventionalCall();
            var url = '/clbs/v/oilmgt/fluxsensorbind/getSensorSetting';
            json_ajax("POST", url, "json", false, {
                "id": setting_id,
                "queryType":4
            }, general.readConventionalCall);
            url = '/clbs/v/oilmgt/fluxsensorbind/getF3Param';
            json_ajax("POST", url, "json", false, {
                "vid": vid,
                "sensorID": 0x45,
                "commandType": 0xF4
            }, general.setreadConventionalCall);
        },
        //传感器-常规参数-初始化上班数据
        initF4ParamCall:function(){
            var clearInputs =[
                "sb",
                "sk","suploadTime","sfilteringFactor","scompensation","sid"];
            general.clearInputTextValue(clearInputs);
        },
        //传感器-常规参数-设备上报数据
        queryF4ParamCall:function(result){
            $("#readConventionalsRefresh").removeAttr("disabled");
            $("#readConventionalsSend").removeAttr("disabled");
            $("#rclSubjectToTheSensor").removeAttr("disabled");
            $("#rclSubjectToPlatformSettings").removeAttr("disabled");
            var param = result.data.msgBody.params[0].value;
            $("#sid").val(param.sensorID.toString(16));
            var compensationCanMake =param.oilParam.inertiaCompEn;
            $("#scompensation").val("使能");//补偿使能
            if(compensationCanMake==''){
                $("#scompensation").val("禁用");//补偿使能
            }
            $("#sfilteringFactor").val("实时");//滤波系数
            if(param.oilParam.smoothing=='2'){
                $("#sfilteringFactor").val("平滑");//滤波系数
            }else if(param.oilParam.smoothing=='3'){
                $("#sfilteringFactor").val("平稳");//滤波系数
            }
            $("#suploadTime").val("被动");//自动上传时间
            if(param.oilParam.autoInterval=='2'){
                $("#suploadTime").val("10");//自动上传时间
            }else if(param.oilParam.autoInterval=='3'){
                $("#suploadTime").val("20");//自动上传时间
            }else if(param.oilParam.autoInterval=='4'){
                $("#suploadTime").val("30");//自动上传时间
            }
            $("#sk").val(param.oilParam.outputCorrectionK);//输出修正系数 K
            $("#sb").val(param.oilParam.outputCorrectionB);//输出修正常数 B
            //传感器常规参数取值
            general.conventionalReadVal();
            //判断平台参数及传感器上报参数
            if (rcNumberPlate != null || rcSensorModel != null) {
                if (sid != pid) {
                    $("#sid").css("background-color", "#ffe6e6");
                }
                if (scompensation != pcompensation) {
                    $("#scompensation").css("background-color", "#ffe6e6");
                }
                if (sfilteringFactor != pfilteringFactor) {
                    $("#sfilteringFactor").css("background-color", "#ffe6e6");
                }
                if (suploadTime != puploadTime) {
                    $("#suploadTime").css("background-color", "#ffe6e6");
                }
                if (sk != pk) {
                    $("#sk").css("background-color", "#ffe6e6");
                }
                if (sb != pb) {
                    $("#sb").css("background-color", "#ffe6e6");
                }
            }
        },
        //处理平台设置常规参数-初始化
        initReadConventionalCall: function () {
            var clearInputs =[
                "pb","pk","puploadTime","pfilteringFactor","pcompensation","pid"];
            general.clearInputTextValue(clearInputs);
        },
        //处理平台设置参数-初始化
        clearInputTextValue: function (data) {
            for(var i=0;i<data.length;i++){
                var id = "#"+data[i];
                $(id).val("");
            }
        },
        //处理平台设置常规参数
        readConventionalCall: function (data) {
            if (data.success) {
                var setting = $.parseJSON(data.msg).setting;
                $("#pid").val("45");
                $("#pcompensation").val(setting.inertiaCompEnStr);
                $("#pfilteringFactor").val(setting.filterFactorStr);
                $("#puploadTime").val(setting.autoUploadTimeStr);
                $("#pk").val(setting.outputCorrectionK);
                $("#pb").val(setting.outputCorrectionB);
                $("#poilSendStatus").val("");//下发状态
                //显示
            } else {
                layer.msg(data.msg);
            }
        },
        //传感器常规参数-获取设备数据返回
        setreadConventionalCall: function (data) {
            if (data.success) {
                general.createSocket0104InfoMonitor(data.msg);
                layer_time = window.setTimeout(function () {
                    // layer.load(2);
                    $("#readConventionalsRefresh").html("<i class='fa fa-spinner loading-state'></i>").prop('disabled',true);
                }, 0);
            } else {
                layer.msg(data.msg);
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
                    $("#readConventionalsRefresh").html("刷新").prop('disabled',false);
                    layer.msg("获取设备数据失败!");
                }
            },60000);
//            webSocket.subscribe(headers, "/user/" + msg.userName + "/oil62533Info", general.getSensor0104Param, "/app/vehicle/oil/setting", requestStrS);
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
            $("#readConventionalsSend").removeAttr("disabled");
            var id =result.data.msgBody.params[0].id;
            if(id=="62533"){//常规参数
            	clearTimeout(layer_time);
                layer.closeAll();
                $("#readConventionalsRefresh").html("刷新").prop('disabled',false);
                general.queryF4ParamCall(result);
                return;
            }
        }, //传感器常规参数对比后赋值(以传感器为准)
        rclSubjectToTheSensorClick: function () {
            $("#cg_deal_type").val("report");
            $("#pcompensation").val(scompensation);
            $("#pfilteringFactor").val(sfilteringFactor);
            $("#puploadTime").val(suploadTime);
            $("#pk").val(sk);
            $("#pb").val(sb);
            //传感器常规参数取值
            general.conventionalReadVal();
            if (rcNumberPlate != null || rcSensorModel != null) {
                if (sid == pid) {
                    $("#sid").css("background-color", "#eee");
                }
                if (scompensation == pcompensation) {
                    $("#scompensation").css("background-color", "#eee");
                }
                if (sfilteringFactor == pfilteringFactor) {
                    $("#sfilteringFactor").css("background-color", "#eee");
                }
                if (suploadTime == puploadTime) {
                    $("#suploadTime").css("background-color", "#eee");
                }
                if (sk == pk) {
                    $("#sk").css("background-color", "#eee");
                }
                if (sb == pb) {
                    $("#sb").css("background-color", "#eee");
                }
            }
        }, //传感器常规参数修正下发
        readConventionalsSendClick: function () {
            $("#editReadConventionalForm").ajaxSubmit(function(data) {
                if (data != null) {
                    var result = $.parseJSON(data);
                    if (result.success) {
                        var msgid = $.parseJSON(result.msg).msgId;
                        var sendstatusname="send_status_"+msgid;
                        $("#poilSendStatus").attr("data-id",sendstatusname);
                        $("#poilSendStatus").addClass(sendstatusname);
                        $("#poilSendStatus").val("参数已下发");
                        general.createSocket0900StatusMonitor(result.msg);
                        setTimeout(function () {
                            // layer.load(2);
                            $("#readConventionalsRefresh").html("<i class='fa fa-spinner loading-state'></i>").prop('disabled',true);
                        }, 0);
                    } else{
                        layer.msg(result.msg,{move:false});
                    }
                }
            });
        }, //创建消息监听
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
            webSocket.subscribe(headers, "/user/topic/t808_currency_response", general.currencyResponse, "/app/vehicle/oil/setting", requestStrS);
            webSocket.subscribe(headers, "/user/topic/deviceReportLog", general.deviceReportLog, "/app/vehicle/oil/setting", requestStrS);
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
                $("#readConventionalsRefresh").html("刷新").prop('disabled',false);
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
                $("#readConventionalsRefresh").html("刷新").prop('disabled',false);
                var msgid =result.data.msgBody.ackMSN;
                var result =result.data.msgBody.result;
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
        //传感器常规参数对比后赋值(以平台设置为准)
        rclSubjectToPlatformSettingsClick: function () {
            $("#cg_deal_type").val("pt");
            $("#sid").val(pid);
            $("#scompensation").val(pcompensation);
            $("#sfilteringFactor").val(pfilteringFactor);
            $("#suploadTime").val(puploadTime);
            $("#sk").val(pk);
            $("#sb").val(pb);
            //传感器常规参数取值
            general.conventionalReadVal();
            if (rcNumberPlate != null || rcSensorModel != null) {
                if (sid == pid) {
                    $("#sid").css("background-color", "#eee");
                }
                if (scompensation == pcompensation) {
                    $("#scompensation").css("background-color", "#eee");
                }
                if (sfilteringFactor == pfilteringFactor) {
                    $("#sfilteringFactor").css("background-color", "#eee");
                }
                if (suploadTime == puploadTime) {
                    $("#suploadTime").css("background-color", "#eee");
                }
                if (sk == pk) {
                    $("#sk").css("background-color", "#eee");
                }
                if (sb == pb) {
                    $("#sb").css("background-color", "#eee");
                }
            }
        }
	}
	$(function(){
		$('input').inputClear();
		general.init();
        $("#readConventionalsSend").on("click", general.readConventionalsSendClick);
        $("#readConventionalsRefresh").on("click", general.readConventionalRefreshClick);
         $("#rclSubjectToTheSensor").on("click", general.rclSubjectToTheSensorClick);
        $("#rclSubjectToPlatformSettings").on("click", general.rclSubjectToPlatformSettingsClick);
	})
})($,window)