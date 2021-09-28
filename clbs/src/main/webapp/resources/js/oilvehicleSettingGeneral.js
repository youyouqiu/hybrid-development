(function($,window){
    var vid = $("#readConventionalVehicleId").val();
    var sensorID = $("#pid").val();
    var sensorType = 0x41;
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
            url = '/clbs/v/oilmassmgt/oilvehiclesetting/getF3Param';
            if(sensorID=="41"){
	            webSocket.subscribe(headers, "/user/topic/oil62529Info", general.getSensor0104Param, null, null);
	        }
	        if (sensorID == "42") {
	        	sensorType = 0x42;
	            webSocket.subscribe(headers, "/user/topic/oil62530Info", general.getSensor0104Param, null, null);
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
            suploadTime = $("#suploadTime").val();
            sk = $("#sk").val();
            sb = $("#sb").val();
            ssensorlength = $("#ssensorlength").val();
            sfueltype = $("#sfueltype").val();
            stankshape = $("#stankshape").val();
            stanklength = $("#stanklength").val();
            stankwidth = $("#stankwidth").val();
            stankheight = $("#stankheight").val();
            soiltime = $("#soiltime").val();
            soilamount = $("#soilamount").val();
            soilspilltime = $("#soilspilltime").val();
            soilspill = $("#soilspill").val();
            //2.平台设置参数
            pid = $("#pid").val();
            pcompensation = $("#pcompensation").val();
            pfilteringFactor = $("#pfilteringFactor").val();
            puploadTime = $("#puploadTime").val();
            pk = $("#pk").val();
            pb = $("#pb").val();
            psensorlength = $("#psensorlength").val();
            pfueltype = $("#pfueltype").val();
            ptankshape = $("#ptankshape").val();
            ptanklength = $("#ptanklength").val();
            ptankwidth = $("#ptankwidth").val();
            ptankheight = $("#ptankheight").val();
            poiltime = $("#poiltime").val();
            poilamount = $("#poilamount").val();
            poilspilltime = $("#poilspilltime").val();
            poilspill = $("#poilspill").val();
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
            var url = '/clbs/v/oilmassmgt/oilvehiclesetting/getSensorSetting';
            var sensorType = 0x41;
            json_ajax("POST", url, "json", false, {
                "id": setting_id,
                "queryType":4
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
            var clearInputs =["soilspill","soilspilltime","soilamount","soiltime","stankheight",
                "stankwidth","stanklength","stankshape","sfueltype","ssensorlength","sb",
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
            var compensationCanMake =param.oilQuantity.compensationCanMake;
            $("#scompensation").val("使能");//补偿使能
            if(compensationCanMake==''){
                $("#scompensation").val("禁用");//补偿使能
            }
            $("#sfilteringFactor").val("实时");//滤波系数
            if(param.oilQuantity.filteringFactor=='2'){
                $("#sfilteringFactor").val("平滑");//滤波系数
            }else if(param.oilQuantity.filteringFactor=='3'){
                $("#sfilteringFactor").val("平稳");//滤波系数
            }
            $("#suploadTime").val("被动");//自动上传时间
            if(param.oilQuantity.automaticUploadTime=='2'){
                $("#suploadTime").val("10");//自动上传时间
            }else if(param.oilQuantity.automaticUploadTime=='3'){
                $("#suploadTime").val("20");//自动上传时间
            }else if(param.oilQuantity.automaticUploadTime=='4'){
                $("#suploadTime").val("30");//自动上传时间
            }
            $("#sk").val(param.oilQuantity.outputCorrectionCoefficientK);//输出修正系数 K
            $("#sb").val(param.oilQuantity.outputCorrectionCoefficientB);//输出修正常数 B
            $("#ssensorlength").val(param.oilQuantity.sensorLength/10);//传感器长度
            $("#sfueltype").val("柴油");
            if(param.oilQuantity.fuelOil=="2"){//燃料选择 01-柴油（缺省值），02-汽油，03-LNG，04-CNG；待定
                $("#sfueltype").val("汽油");
            }else if(param.oilQuantity.fuelOil=="3"){
                $("#sfueltype").val("LNG");
            }else if(param.oilQuantity.fuelOil=="4"){
                $("#sfueltype").val("CNG");
            }
            $("#stankshape").val("长方形");//油箱形状 0x01：长方形，0x02：圆柱形，0x03： D 形，0x04：椭圆形，0x05：其它
            if(param.oilQuantity.shape=="2"){
                $("#stankshape").val("圆柱形");
            }else if(param.oilQuantity.shape=="3"){
                $("#stankshape").val("D 形");
            }else if(param.oilQuantity.shape=="4"){
                $("#stankshape").val("椭圆形");
            }else if(param.oilQuantity.shape=="5"){
                $("#stankshape").val("其它");
            }
            $("#stanklength").val(param.oilQuantity.boxLength);//油箱尺寸 1 长
            $("#stankwidth").val(param.oilQuantity.width);//油箱尺寸 2 宽
            $("#stankheight").val(param.oilQuantity.height);////油箱尺寸 3 高
            $("#soiltime").val(param.oilQuantity.addOilTimeThreshold);//加油时间阈值
            $("#soilamount").val(param.oilQuantity.addOilAmountThreshol/10);////加油量阈值
            $("#soilspilltime").val(param.oilQuantity.seepOilTimeThreshold);//漏油时间阈值
            $("#soilspill").val(param.oilQuantity.seepOilAmountThreshol/10);//漏油量阈值
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
                if (ssensorlength != psensorlength) {
                    $("#ssensorlength").css("background-color", "#ffe6e6");
                }
                if (sfueltype != pfueltype) {
                    $("#sfueltype").css("background-color", "#ffe6e6");
                }
                if (stankshape != ptankshape) {
                    $("#stankshape").css("background-color", "#ffe6e6");
                }
                if (stanklength != ptanklength) {
                    $("#stanklength").css("background-color", "#ffe6e6");
                }
                if (stankwidth != ptankwidth) {
                    $("#stankwidth").css("background-color", "#ffe6e6");
                }
                if (stankheight != ptankheight) {
                    $("#stankheight").css("background-color", "#ffe6e6");
                }
                if (soiltime != poiltime) {
                    $("#soiltime").css("background-color", "#ffe6e6");
                }
                if (soilamount != poilamount) {
                    $("#soilamount").css("background-color", "#ffe6e6");
                }
                if (soilspilltime != poilspilltime) {
                    $("#soilspilltime").css("background-color", "#ffe6e6");
                }
                if (soilspill != poilspill) {
                    $("#soilspill").css("background-color", "#ffe6e6");
                }
            }
        },
        //处理平台设置常规参数-初始化
        initReadConventionalCall: function () {
            var clearInputs =["poilspill","poilspilltime","poilamount","poiltime","ptankheight",
                "ptankwidth","ptanklength","ptankshapeval","ptankshape","pfueltype","psensorlength",
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
                $("#pid").val("4"+setting.oilBoxType);
                $("#pcompensation").val(setting.compensationCanMakeStr);
                $("#pfilteringFactor").val(setting.filteringFactorStr);
                $("#puploadTime").val(setting.automaticUploadTimeStr);
                $("#pk").val(setting.outputCorrectionCoefficientK);
                $("#pb").val(setting.outputCorrectionCoefficientB);
                $("#psensorlength").val(setting.sensorLength);
                $("#pfueltype").val(setting.fuelOil);
                $("#ptankshape").val(setting.shapeStr);
                $("#ptankshapeval").val(setting.shape);
                $("#ptanklength").val(setting.boxLength);
                $("#ptankwidth").val(setting.width);
                $("#ptankheight").val(setting.height);
                $("#poiltime").val(setting.addOilTimeThreshold);
                $("#poilamount").val(setting.addOilAmountThreshol);
                $("#poilspilltime").val(setting.seepOilTimeThreshold);
                $("#poilspill").val(setting.seepOilAmountThreshol);
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
            if(id=="62529"||id=="62530"){//常规参数
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
            $("#psensorlength").val(ssensorlength);
            $("#pfueltype").val(sfueltype);
            $("#ptankshape").val(stankshape);
            $("#ptanklength").val(stanklength);
            $("#ptankwidth").val(stankwidth);
            $("#ptankheight").val(stankheight);
            $("#poiltime").val(soiltime);
            $("#poilamount").val(soilamount);
            $("#poilspilltime").val(soilspilltime);
            $("#poilspill").val(soilspill);
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
                if (ssensorlength == psensorlength) {
                    $("#ssensorlength").css("background-color", "#eee");
                }
                if (sfueltype == pfueltype) {
                    $("#sfueltype").css("background-color", "#eee");
                }
                if (stankshape == ptankshape) {
                    $("#stankshape").css("background-color", "#eee");
                }
                if (stanklength == ptanklength) {
                    $("#stanklength").css("background-color", "#eee");
                }
                if (stankwidth == ptankwidth) {
                    $("#stankwidth").css("background-color", "#eee");
                }
                if (stankheight == ptankheight) {
                    $("#stankheight").css("background-color", "#eee");
                }
                if (soiltime == poiltime) {
                    $("#soiltime").css("background-color", "#eee");
                }
                if (soilamount == poilamount) {
                    $("#soilamount").css("background-color", "#eee");
                }
                if (soilspilltime == poilspilltime) {
                    $("#soilspilltime").css("background-color", "#eee");
                }
                if (soilspill == poilspill) {
                    $("#soilspill").css("background-color", "#eee");
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
            $("#ssensorlength").val(psensorlength);
            $("#sfueltype").val(pfueltype);
            $("#stankshape").val(ptankshape);
            $("#stanklength").val(ptanklength);
            $("#stankwidth").val(ptankwidth);
            $("#stankheight").val(ptankheight);
            $("#soiltime").val(poiltime);
            $("#soilamount").val(poilamount);
            $("#soilspilltime").val(poilspilltime);
            $("#soilspill").val(poilspill);
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
                if (ssensorlength == psensorlength) {
                    $("#ssensorlength").css("background-color", "#eee");
                }
                if (sfueltype == pfueltype) {
                    $("#sfueltype").css("background-color", "#eee");
                }
                if (stankshape == ptankshape) {
                    $("#stankshape").css("background-color", "#eee");
                }
                if (stanklength == ptanklength) {
                    $("#stanklength").css("background-color", "#eee");
                }
                if (stankwidth == ptankwidth) {
                    $("#stankwidth").css("background-color", "#eee");
                }
                if (stankheight == ptankheight) {
                    $("#stankheight").css("background-color", "#eee");
                }
                if (soiltime == poiltime) {
                    $("#soiltime").css("background-color", "#eee");
                }
                if (soilamount == poilamount) {
                    $("#soilamount").css("background-color", "#eee");
                }
                if (soilspilltime == poilspilltime) {
                    $("#soilspilltime").css("background-color", "#eee");
                }
                if (soilspill == poilspill) {
                    $("#soilspill").css("background-color", "#eee");
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