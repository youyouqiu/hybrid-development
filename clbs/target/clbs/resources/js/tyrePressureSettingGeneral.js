(function($,window){
    var vid = $("#readConventionalVehicleId").val();
    var sensorID = $("#pid").val();
    var sensorType = 0xE3;
    var setting_id=$("#setting_id").val();
    var _timeout;
    var layer_time;
    //最后一次获取信息想MSGID
	general = {
		init: function(){
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
            var url = '/clbs/v/oilmassmgt/oilvehiclesetting/getF3Param';
            webSocket.subscribe(headers, "/user/topic/oil62691Info", general.getSensor0104Param, null, null);
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
            suploadTime = $("#suploadTime").val();
            sk = $("#sk").val();
            sb = $("#sb").val();
            spressure = $("#spressure").val();
            spressureThreshold = $("#spressureThreshold").val();
            sslowLeakThreshold = $("#sslowLeakThreshold").val();
            shighTemperature = $("#shighTemperature").val();
            slowPressure = $("#slowPressure").val();
            sheighPressure = $("#sheighPressure").val();
            selectricityThreshold = $("#selectricityThreshold").val();
            //2.平台设置参数
            pid = $("#pid").val();
            pcompensation = $("#pcompensation").val();
            pfilteringFactor = $("#pfilteringFactor").val();
            puploadTime = $("#puploadTime").val();
            pk = $("#pk").val();
            pb = $("#pb").val();
            ppressure = $("#ppressure").val();
            ppressureThreshold = $("#ppressureThreshold").val();
            pslowLeakThreshold = $("#pslowLeakThreshold").val();
            phighTemperature = $("#phighTemperature").val();
            plowPressure = $("#plowPressure").val();
            pheighPressure = $("#pheighPressure").val();
            pelectricityThreshold = $("#pelectricityThreshold").val();
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
            var url = '/clbs/v/tyrepressure/setting/getReferenceInfo';
            var sensorType = 0xE3;
            json_ajax("POST", url, "json", false, {
                "vehicleId": vid,
            }, general.readConventionalCall);
            url = '/clbs/v/oilmassmgt/oilvehiclesetting/getF3Param';
            json_ajax("POST", url, "json", false, {
                "vid": vid,
                "sensorID": sensorType,
                "commandType": 0xF4
            }, general.setreadConventionalCall);
        },
        //传感器-常规参数-初始化上班数据
        initF4ParamCall:function(){
            var clearInputs =["selectricityThreshold","sheighPressure",
                "slowPressure","shighTemperature","sslowLeakThreshold","spressureThreshold","spressure","sb",
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
            $("#sid").val(param.sensorID.toString(16).toUpperCase());
            var compensationCanMake =param.tyreParam.compensatingEnable;
            if(compensationCanMake=='2'){
                $("#scompensation").val("禁用");//补偿使能
            }else if(compensationCanMake=='1'){
                $("#scompensation").val("使能");//补偿使能
            }
            $("#sfilteringFactor").val("实时");//滤波系数
            if(param.tyreParam.smoothing=='2'){
                $("#sfilteringFactor").val("平滑");//滤波系数
            }else if(param.tyreParam.smoothing=='3'){
                $("#sfilteringFactor").val("平稳");//滤波系数
            }
            $("#suploadTime").val("被动");//自动上传时间
            if(param.tyreParam.automaticUploadTime=='2'){
                $("#suploadTime").val("10");//自动上传时间
            }else if(param.tyreParam.automaticUploadTime=='3'){
                $("#suploadTime").val("20");//自动上传时间
            }else if(param.tyreParam.automaticUploadTime=='4'){
                $("#suploadTime").val("30");//自动上传时间
            }
            $("#sk").val(param.tyreParam.compensationFactorK);//输出修正系数 K
            $("#sb").val(param.tyreParam.compensationFactorB);//输出修正常数 B
            $("#spressure").val(param.tyreParam.pressure/10);
            $("#spressureThreshold").val(param.tyreParam.pressureThreshold);
            $("#sslowLeakThreshold").val(param.tyreParam.slowLeakThreshold);
            $("#shighTemperature").val((param.tyreParam.highTemperature-2731)/10);
            $("#slowPressure").val(param.tyreParam.lowPressure/10);
            $("#sheighPressure").val(param.tyreParam.heighPressure/10);
            $("#selectricityThreshold").val(param.tyreParam.electricityThreshold);
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
                if (spressure != ppressure) {
                    $("#sppressure").css("background-color", "#ffe6e6");
                }
                if (spressureThreshold != ppressureThreshold) {
                    $("#spressureThreshold").css("background-color", "#ffe6e6");
                }
                if (sslowLeakThreshold != pslowLeakThreshold) {
                    $("#sslowLeakThreshold").css("background-color", "#ffe6e6");
                }
                if (shighTemperature != phighTemperature) {
                    $("#shighTemperature").css("background-color", "#ffe6e6");
                }
                if (slowPressure != plowPressure) {
                    $("#slowPressure").css("background-color", "#ffe6e6");
                }
                if (sheighPressure != pheighPressure) {
                    $("#sheighPressure").css("background-color", "#ffe6e6");
                }
                if (selectricityThreshold != pelectricityThreshold) {
                    $("#selectricityThreshold").css("background-color", "#ffe6e6");
                }
            }
        },
        //处理平台设置常规参数-初始化
        initReadConventionalCall: function () {
            var clearInputs =["pelectricityThreshold","pheighPressure",
                "plowPressure","phighTemperature","pslowLeakThreshold","ppressureThreshold","ppressure","pb",
                "pk","puploadTime","pfilteringFactor","pcompensation","pid"];
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
                var setting = data.obj;
                $("#pid").val("E3");
                if(setting.compensate == 1){
                    $("#pcompensation").val("使能");
                }else if(setting.compensate == 2){
                    $("#pcompensation").val("禁用");
                }
                if(setting.filterFactor == 1){
                    $("#pfilteringFactor").val("实时");
                }else if(setting.filterFactor == 2){
                    $("#pfilteringFactor").val("平滑");
                }else if(setting.filterFactor == 3){
                    $("#pfilteringFactor").val("平稳");
                }
                if(setting.tyrePressureParameter.automaticUploadTime == 1){
                    $("#puploadTime").val("被动");
                }else if(setting.tyrePressureParameter.automaticUploadTime == 2){
                    $("#puploadTime").val("10");
                }else if(setting.tyrePressureParameter.automaticUploadTime == 3){
                    $("#puploadTime").val("20");
                }else if(setting.tyrePressureParameter.automaticUploadTime == 4){
                    $("#puploadTime").val("30");
                }
                $("#pk").val(setting.tyrePressureParameter.compensationFactorK);
                $("#pb").val(setting.tyrePressureParameter.compensationFactorB);
                $("#ppressure").val(setting.tyrePressureParameter.pressure);
                $("#ppressureThreshold").val(setting.tyrePressureParameter.pressureThreshold);
                $("#pslowLeakThreshold").val(setting.tyrePressureParameter.slowLeakThreshold);
                $("#phighTemperature").val(setting.tyrePressureParameter.highTemperature);
                $("#plowPressure").val(setting.tyrePressureParameter.lowPressure);
                $("#pheighPressure").val(setting.tyrePressureParameter.heighPressure);
                $("#pelectricityThreshold").val(setting.tyrePressureParameter.electricityThreshold);
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
            var id =result.data.msgBody.params[0].id
            if(id=="62691"){//常规参数
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
            $("#ppressure").val(spressure);
            $("#ppressureThreshold").val(spressureThreshold);
            $("#pslowLeakThreshold").val(sslowLeakThreshold);
            $("#phighTemperature").val(shighTemperature);
            $("#plowPressure").val(slowPressure);
            $("#pheighPressure").val(sheighPressure);
            $("#pelectricityThreshold").val(selectricityThreshold);
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
                if (spressure == ppressure) {
                    $("#sppressure").css("background-color", "#eee");
                }
                if (spressureThreshold == ppressureThreshold) {
                    $("#spressureThreshold").css("background-color", "#eee");
                }
                if (sslowLeakThreshold == pslowLeakThreshold) {
                    $("#sslowLeakThreshold").css("background-color", "#eee");
                }
                if (shighTemperature == phighTemperature) {
                    $("#shighTemperature").css("background-color", "#eee");
                }
                if (slowPressure == plowPressure) {
                    $("#slowPressure").css("background-color", "#eee");
                }
                if (sheighPressure == pheighPressure) {
                    $("#sheighPressure").css("background-color", "#eee");
                }
                if (selectricityThreshold == pelectricityThreshold) {
                    $("#selectricityThreshold").css("background-color", "#eee");
                }
            }
        }, //传感器常规参数修正下发
        readConventionalsSendClick: function () {
		    var url = "/clbs/v/tyrepressure/setting/updateSensorSetting";
            var tyrePressureParameterStr = {
                "pressure": $("#ppressure").val(),
                "pressureThreshold": $("#ppressureThreshold").val(),
                "slowLeakThreshold": $("#pslowLeakThreshold").val(),
                "highTemperature": $("#phighTemperature").val(),
                "lowPressure": $("#plowPressure").val(),
                "heighPressure": $("#pheighPressure").val(),
                "electricityThreshold": $("#pelectricityThreshold").val(),
                "automaticUploadTimeStr": $('#puploadTime').val(),
                "compensationFactorK": $('#pk').val(),
                "compensationFactorB": $('#pb').val(),
            };
            var data = {
                "vehicleId": $("#readConventionalVehicleId").val(),
                "dealType":$("#cg_deal_type").val(),
                "id":$("#setting_id").val(),
                "sensorId": $("#sensorId").val(),
                "compensateStr": $("#pcompensation").val(),
                "filterFactorStr": $("#pfilteringFactor").val(),
                "tyrePressureParameterStr": JSON.stringify(tyrePressureParameterStr)
            };
            ajax_submit("POST", url, "json", false, data, true, general.submitcallback);
        },
        submitcallback(data){
            if (data != null) {
                if (data.success) {
                    var msgid = $.parseJSON(data.msg).msgId;
                    var sendstatusname="send_status_"+msgid;
                    $("#poilSendStatus").attr("data-id",sendstatusname);
                    $("#poilSendStatus").addClass(sendstatusname);
                    $("#poilSendStatus").val("参数已下发");
                    general.createSocket0900StatusMonitor(data.msg);
                    setTimeout(function () {
                        // layer.load(2);
                        $("#readConventionalsRefresh").html("<i class='fa fa-spinner loading-state'></i>").prop('disabled',true);
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
            $("#spressure").val(ppressure);
            $("#spressureThreshold").val(ppressureThreshold);
            $("#sslowLeakThreshold").val(pslowLeakThreshold);
            $("#shighTemperature").val(phighTemperature);
            $("#slowPressure").val(plowPressure);
            $("#sheighPressure").val(pheighPressure);
            $("#selectricityThreshold").val(pelectricityThreshold);
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
                if (spressure == ppressure) {
                    $("#sppressure").css("background-color", "#eee");
                }
                if (spressureThreshold == ppressureThreshold) {
                    $("#spressureThreshold").css("background-color", "#eee");
                }
                if (sslowLeakThreshold == pslowLeakThreshold) {
                    $("#sslowLeakThreshold").css("background-color", "#eee");
                }
                if (shighTemperature == phighTemperature) {
                    $("#shighTemperature").css("background-color", "#eee");
                }
                if (slowPressure == plowPressure) {
                    $("#slowPressure").css("background-color", "#eee");
                }
                if (sheighPressure == pheighPressure) {
                    $("#sheighPressure").css("background-color", "#eee");
                }
                if (selectricityThreshold == pelectricityThreshold) {
                    $("#selectricityThreshold").css("background-color", "#eee");
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