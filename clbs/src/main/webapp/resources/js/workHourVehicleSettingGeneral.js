//# sourceURL=workHourVehicleSettingGeneral.js
(function($,window){
    var vid = $("#readConventionalVehicleId").val();
    var sensorID = $("#sensorSequence").val();
    var sensorType = 0x80;
    var setting_id=$("#setting_id").val();
    var _timeout;
    var layer_time;
    //最后一次获取信息想MSGID
	general = {
		init: function(){
            var pdetectionMode = $('#pdetectionMode').val();
		    if (pdetectionMode=='3'){
		        $('#smoothFactorGroup').removeClass('hidden');
                $('#baudRateCalculateNumberGroup').removeClass('hidden');
                $('#baudRateThresholdGroup').removeClass('hidden');
                $('#baudRateCalculateTimeScopeGroup').removeClass('hidden');
                $("#speedThresholdGroup").removeClass('hidden');
            } else if(pdetectionMode == '2') {
                $('#smoothFactorGroup').removeClass('hidden');
            }else if(pdetectionMode == '1') {
                $('#thresholdVoltageGroup').removeClass('hidden');
            }

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
            url = '/clbs/v/workhourmgt/workhoursetting/getF3Param';
            if(sensorID=="0"){
                /*F480(10进制):62592*/
	            webSocket.subscribe(headers, "/user/topic/oil62592Info", general.getSensor0104Param, null, null);
	        }
	        if (sensorID == "1") {
	        	sensorType = 0x81;
	            webSocket.subscribe(headers, "/user/topic/oil62593Info", general.getSensor0104Param, null, null);
	        }
            json_ajax("POST", url, "json", false, {
                "vid": vid,
                "sensorID": sensorType,
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
            sdetectionMode = $("#sdetectionMode").val();
            slastTime = $("#slastTime").val();
            sthresholdVoltage = $("#sthresholdVoltage").val();
            sthreshTwo = $('#sthreshTwo').val();
            threshold = $('#threshold').val();
            sBaudRateCalculateNumber = $('#sBaudRateCalculateNumber').val();
            sBaudRateCalculateTimeScope = $("#sBaudRateCalculateTimeScope").val();

            //2.平台设置参数
            pid = $("#pid").val();
            pcompensation = $("#pcompensation").val();
            pfilteringFactor = $("#pfilteringFactor").val();
            pdetectionMode = $("#pdetectionMode").val();
            plastTime = $("#plastTime").val();
            pthresholdVoltage = $("#pthresholdVoltage").val();
            pthreshTwo = $('#pthreshTwo').val();
            thresholdTwo = $('#thresholdTwo').val();
            pBaudRateCalculateNumber = $('#pBaudRateCalculateNumber').val();
            pBaudRateCalculateTimeScope = $("#pBaudRateCalculateTimeScope").val();
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
            var url = '/clbs/v/workhourmgt/workhoursetting/getSensorSetting';
            var sensorType = 0x80;
            if (sensorID == "1") {
                sensorType = 0x81;
            }
            json_ajax("POST", url, "json", false, {
                "id": setting_id,
                "queryType":4
            }, general.readConventionalCall);
            url = '/clbs/v/workhourmgt/workhoursetting/getF3Param';
            json_ajax("POST", url, "json", false, {
                "vid": vid,
                "sensorID": sensorType,
                "commandType": 0xF4
            }, general.setreadConventionalCall);
        },
        //传感器-常规参数-初始化上班数据
        initF4ParamCall:function(){
            var clearInputs =["sthreshTwo","sBaudRateCalculateTimeScope","sBaudRateCalculateNumber",
                "sthresholdVoltage","slastTime","sfilteringFactor","scompensation","sid"];
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

            var compensationCanMake =param.workHourSettingParam.compensate;
            $("#scompensation").val("使能");//补偿使能
            if(compensationCanMake=='禁用'){
                $("#scompensation").val("禁用");//补偿使能
            }
            $("#sfilteringFactor").val($("#pfilteringFactor").val());//滤波系数

            $("#slastTime").val(param.workHourSettingParam.lastTime);
            var pdetectionMode = $("#pdetectionMode").val();
            // 根据平台的检测方式来确认数据
            if(pdetectionMode == '1') {
                $("#sthresholdVoltage").val((param.workHourSettingParam.threshOne / 10).toFixed(1));
            }
            $('#sthreshTwo').val(param.workHourSettingParam.smoothParam);
            $('#sBaudRateCalculateNumber').val(param.workHourSettingParam.waveNum);
            var waveTime = param.workHourSettingParam.waveTime;
            $("#sBaudRateCalculateTimeScope").val(general.getWaveTimeSecond(waveTime));
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

                if (sdetectionMode != pdetectionMode) {
                    $("#sdetectionMode").css("background-color", "#ffe6e6");
                }
                if (slastTime != plastTime) {
                    $("#slastTime").css("background-color", "#ffe6e6");
                }
                if ($("#pdetectionMode").val() ==1 && sthresholdVoltage != pthresholdVoltage) {
                    $("#sthresholdVoltage").css("background-color", "#ffe6e6");
                }
                if (sthreshTwo != pthreshTwo) {
                    $("#sthreshTwo").css("background-color", "#ffe6e6");
                }
                if (threshold != thresholdTwo) {
                    $("#threshold").css("background-color", "#ffe6e6");
                }
                if (sBaudRateCalculateNumber != pBaudRateCalculateNumber) {
                    $("#sBaudRateCalculateNumber").css("background-color", "#ffe6e6");
                }
                if (sBaudRateCalculateTimeScope != pBaudRateCalculateTimeScope) {
                    $("#sBaudRateCalculateTimeScope").css("background-color", "#ffe6e6");
                }
            }
        },
        getWaveTimeSecond: function (waveTime) {
            var waveTimeSecond = 30;
            switch (waveTime) {
                case 1:
                    waveTimeSecond = 10;
                    break;
                case 2:
                    waveTimeSecond = 15;
                    break;
                case 3:
                    waveTimeSecond = 20;
                    break;
                case 4:
                    waveTimeSecond = 30;
                    break;
                case 5:
                    waveTimeSecond = 60;
                    break;
                default:
                    break;
            }
            return waveTimeSecond;
        }
        ,
        //处理平台设置常规参数-初始化
        initReadConventionalCall: function () {
            var clearInputs =["pthreshTwo","thresholdTwo",
                "pthresholdVoltage","plastTime","pfilteringFactor","pcompensation","pid","pBaudRateCalculateNumber"
                ,"pBaudRateThreshold","pBaudRateCalculateTimeScope","pSpeedThreshold"];
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
                $("#pid").val(setting.sensorPeripheralID);
                $("#pcompensation").val(setting.compensateStr);
                $("#pfilteringFactor").val(setting.filterFactorStr);
                $("#pdetectionMode").val(setting.detectionMode);
                $("#plastTime").val(setting.lastTime);
                $("#pdetectionMode").val() ==1 && $("#pthresholdVoltage").val(setting.thresholdVoltage);
                $('#pthreshTwo').val(setting.smoothingFactor);
                $('#thresholdTwo').val(setting.threshold);
                $("#pBaudRateCalculateNumber").val(setting.baudRateCalculateNumber);
                $("#pBaudRateThreshold").val(setting.baudRateThreshold);
                $("#pSpeedThreshold").val(setting.speedThreshold);
                var waveTime = setting.baudRateCalculateTimeScope;
                console.log(waveTime)
                $("#pBaudRateCalculateTimeScope").val(general.getWaveTimeSecond(waveTime));
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
                    $("#readConventionalsRefresh").html("<i class='fa fa-spinner loading-state'></i>").prop('disabled', true);
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
                    $("#readConventionalsRefresh").html("刷新").prop('disabled', false);
                    layer.msg("获取设备数据失败!");
                }
            },60000);
//            if(sensorID=="41"){
//                webSocket.subscribe(headers, "/user/" + msg.userName + "/oil62529Info", general.getSensor0104Param, "/app/vehicle/oil/setting", requestStrS);
//            }
//            if (sensorID == "42") {
//                webSocket.subscribe(headers, "/user/" + msg.userName + "/oil62530Info", general.getSensor0104Param, "/app/vehicle/oil/setting", requestStrS);
//            }
        },
        //处理获取设备上传数据
        getSensor0104Param:function (msg) {
            if (msg == null || !isRead)
                {return;}
            var result = $.parseJSON(msg.body);
            var msgSNAck = result.data.msgBody.msgSNAck;
            if (msgSNAck != $("#temp_send_vehicle_msg_id").val()) {
                return;
            }
            isRead=false;
            clearTimeout(_timeout);
            $("#readConventionalsSend").removeAttr("disabled");
            var id =result.data.msgBody.params[0].id
            if(id=="62592"||id=="62593"){//常规参数
            	clearTimeout(layer_time);
                layer.closeAll();
                $("#readConventionalsRefresh").html("刷新").prop('disabled', false);
                general.queryF4ParamCall(result);
                return;
            }
        }, //传感器常规参数对比后赋值(以传感器为准)
        rclSubjectToTheSensorClick: function () {
            $("#cg_deal_type").val("report");
            $("#pcompensation").val(scompensation);
            $("#pfilteringFactor").val(sfilteringFactor);
            $("#plastTime").val(slastTime);
            $("#pdetectionMode").val(sdetectionMode);
            $("#pdetectionMode").val() ==1 && $("#pthresholdVoltage").val(sthresholdVoltage);
            $('#pthreshTwo').val(sthreshTwo);
            $('#thresholdTwo').val(threshold);

            $("#pBaudRateCalculateNumber").val(sBaudRateCalculateNumber);
            $("#pBaudRateCalculateTimeScope").val(sBaudRateCalculateTimeScope);
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
                if (sdetectionMode == pdetectionMode) {
                    $("#sdetectionMode").css("background-color", "#eee");
                }
                if (slastTime == plastTime) {
                    $("#slastTime").css("background-color", "#eee");
                }
                if ($("#pdetectionMode").val() ==1 && sthresholdVoltage == pthresholdVoltage) {
                    $("#sthresholdVoltage").css("background-color", "#eee");
                }
                if (sthreshTwo == pthreshTwo) {
                    $("#sthreshTwo").css("background-color", "#eee");
                }
                if (sBaudRateCalculateNumber == pBaudRateCalculateNumber) {
                    $("#sBaudRateCalculateNumber").css("background-color", "#eee");
                }
                if (sBaudRateCalculateTimeScope == pBaudRateCalculateTimeScope) {
                    $("#sBaudRateCalculateNumber").css("background-color", "#eee");
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
                            $("#readConventionalsRefresh").html("<i class='fa fa-spinner loading-state'></i>").prop('disabled', true);
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
                {return;}
            var result = $.parseJSON(msg.body);
            var msgid =result.data.msgBody.msgSNACK;
            var status =result.data.msgBody.result;
            if(status==0){
                $(".send_status_"+msgid).val("终端处理中");
            }
            if(status==1||status==2||status==3){
                layer.closeAll();
                $("#readConventionalsRefresh").html("刷新").prop('disabled', false);
                $(".send_status_"+msgid).val("参数下发失败");
            }
            return;
        },
        //处理获取设备下发的结果
        deviceReportLog:function (msg) {
            if (msg == null)
                {return;}
            var result = $.parseJSON(msg.body);
            var type =result.data.msgBody.type;
            if(type==243){
                layer.closeAll();
                $("#readConventionalsRefresh").html("刷新").prop('disabled', false);
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
            $("#sdetectionMode").val(pdetectionMode);
            $("#slastTime").val(plastTime);
            $("#pdetectionMode").val() ==1 && $("#sthresholdVoltage").val(pthresholdVoltage);
            $('#sthreshTwo').val(pthreshTwo);
            $('#threshold').val(thresholdTwo);
            $('#sBaudRateCalculateNumber').val(pBaudRateCalculateNumber);
            $("#sBaudRateCalculateTimeScope").val(pBaudRateCalculateTimeScope);
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
                if (sdetectionMode == pdetectionMode) {
                    $("#sdetectionMode").css("background-color", "#eee");
                }
                if (slastTime == plastTime) {
                    $("#slastTime").css("background-color", "#eee");
                }
                if ($("#pdetectionMode").val() ==1 && sthresholdVoltage == pthresholdVoltage) {
                    $("#sthresholdVoltage").css("background-color", "#eee");
                }
                if (sthreshTwo == pthreshTwo) {
                    $("#sthreshTwo").css("background-color", "#eee");
                }
                if (threshold == thresholdTwo) {
                    $("#threshold").css("background-color", "#eee");
                }
                if (sBaudRateCalculateNumber == pBaudRateCalculateNumber) {
                    $("#sBaudRateCalculateNumber").css("background-color", "#eee");
                }
                if (sBaudRateCalculateTimeScope == pBaudRateCalculateTimeScope) {
                    $("#sBaudRateCalculateTimeScope").css("background-color", "#eee");
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
}($,window))