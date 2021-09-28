(function($,window){
    var isRead =false;
    var _timeout;
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
            }
            var clearInputs =["ri_company","ri_product_code","ri_hardware_version","ri_software_version",
                "ri_device_id",
                "ri_client_code"];
            basicInfo.clearInputTextValue(clearInputs);
            //获取监控对象id
            var monitorId = $("#ri_vehicle_id").val();
            if (monitorId != null && monitorId != "") { //监控对象id不为空，进行获取传感器基本信息下发
                var url = '/clbs/v/sensorConfig/sensorUpgrade/getF3Param';
                var sensorType = "0x21";
                var sensorID =$("#ri_sensor_type").val();
                if (sensorID !==null && sensorID != undefined && sensorID != "") {
                    sensorType =  "0x" + sensorID.replace("0x","");
                    var sensorTypeSub = "0xF8" + sensorID.replace("0x","");
                    webSocket.subscribe(headers, "/user/topic/oil"+parseInt(sensorTypeSub, 16)+"Info", basicInfo.getSensor0104Param, null, null);
                }
                var vid =$("#ri_vehicle_id").val();
                json_ajax("POST", url, "json", false, {
                    "vid": vid,
                    "sensorID": sensorType,
                    "commandType": 0xF8
                },basicInfo.getF3BaseParamCall);
            } else { //监控对象为空，弹出提示语
                layer.msg("监控对象未设置该类型传感器");
            }
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
            var vid = $("#ri_vehicle_id").val();
            if (vid == null || vid == "") { // 监控对象id为空则直接弹出提示语
                layer.msg("监控对象未设置该类型传感器");
                return;
            }
            var ri_brand = $("#ri_brand").val();
            var type = $("#ri_sensorID").val();
            var sensorType = 0x21;// 默认为温度传感器1
            var url = '/clbs/v/sensorConfig/sensorUpgrade/getF3Param';
            var sensorID = $("#ri_sensor_type").val();
            if (sensorID != null && sensorID != undefined && sensorID != "") {
            	sensorType = "0x" + sensorID;
            }
            var clearInputs =["ri_company","ri_product_code","ri_hardware_version","ri_software_version",
                "ri_device_id",
                "ri_client_code"];
            basicInfo.clearInputTextValue(clearInputs);
            json_ajax("POST", url, "json", false, {
                "vid": vid,
                "sensorID": sensorType,
                "commandType": 0xF8
            },basicInfo.getF3BaseParamCall);
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
            temp_send_vehicle_msg_id= msg.msgId;
            headers = {"UserName": msg.userName};
            var sensorID =$("#ri_sensor_type").val();

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
//            if (sensorID == "42") {
//                webSocket.subscribe(headers, "/user/" + msg.userName + "/oil63554Info", basicInfo.getSensor0104Param, "/app/vehicle/oil/setting", requestStrS);
//            }else {
//                webSocket.subscribe(headers, "/user/" + msg.userName + "/oil63553Info", basicInfo.getSensor0104Param, "/app/vehicle/oil/setting", requestStrS);
//            }
        },
        //处理获取设备上传数据
        getSensor0104Param:function (msg) {
            if (msg == null)
                return;
            var result = $.parseJSON(msg.body);
            var msgSNAck = result.data.msgBody.msgSNAck;
            if (msgSNAck != temp_send_vehicle_msg_id) {
                return;
            }
            isRead=false;
            clearTimeout(_timeout);
            var status =result.data.msgBody.result;
            if(status==1){
                layer.closeAll();
                $("#readInformationRefresh").html("刷新").prop('disabled',false);
                layer.msg("获取设备数据失败!");
                return;
            }
            var id =result.data.msgBody.params[0].id;
            // if(id=="63553" || id=="63554"){//基本信息
                layer.closeAll();
                $("#readInformationRefresh").html("刷新").prop('disabled',false);
            	basicInfo.queryF3BaseParamCall(result);
                return;
            // }
        },
        //基本信息-上报获取基本信息返回处理方法
        queryF3BaseParamCall: function (temp_data) {
		    var baseValue = temp_data.data.msgBody.params[0].value;
            $("#ri_company").val(baseValue.companyName);//公司名称
            $("#ri_product_code").val(baseValue.productCode);//产品代码
            $("#ri_hardware_version").val(baseValue.hardwareVersionsCode);//硬件版本号
            $("#ri_software_version").val(baseValue.softwareVersionsCode);//软件版本号
            $("#ri_device_id").val(baseValue.sensorID);//设备ID
            $("#ri_client_code").val(baseValue.clientCode);//客户代码
        },
	}
	$(function(){
		basicInfo.init();
		//刷新
        $("#readInformationRefresh").on("click", basicInfo.readInformationRefreshClick);
	})
})($,window)