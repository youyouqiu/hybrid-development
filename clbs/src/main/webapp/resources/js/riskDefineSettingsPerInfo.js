//# sourceURL=definesettingedit.js
(function ($, window) {
    isRead = true;
    var _timeout;
    perInfoObj = {
        init: function (sensorID) {
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
            var url = '/clbs/r/riskManagement/DefineSettings/getPeripheralInfo';
            if (sensorID == "225") {//ADAS
                webSocket.subscribe(headers, "/user/topic/per62177Info", function (data) {
                    perInfoObj.getSensor0104Param(data, sensorID);
                }, null, null);
            } else {//DSM
                webSocket.subscribe(headers, "/user/topic/per62178Info", function (data) {
                    perInfoObj.getSensor0104Param(data, sensorID);
                }, null, null);
            }
            var vid = $("#vehicleId").val();
            json_ajax("POST", url, "json", false, {
                "vid": vid,
                "sensorID": sensorID,
                "commandType": 242,
            }, perInfoObj.getF3BaseParamCall);
        },
        //处理平台设置参数-初始化
        clearInputTextValue: function (data) {
            for (var i = 0; i < data.length; i++) {
                var id = "#" + data[i];
                $(id).val("");
            }
        },
        //基本信息-下发获取基本信息返回处理方法
        getF3BaseParamCall: function (data) {
            if (!data.success) {
                if (data.msg) {
                    layer.msg(data.msg);
                }
            } else {
                if (isRead) {
                    $("#readPerInfoRefresh").html("<i class='fa fa-spinner loading-state'></i>").prop('disabled', true);
                }
                perInfoObj.createSocket0104InfoMonitor(data.msg);
            }
        },
        //基本信息-刷新
        readInformationRefreshClick: function () {
            isRead = true;
            var url = '/clbs/r/riskManagement/DefineSettings/getPeripheralInfo';
            var vid = $("#vehicleId").val();
            json_ajax("POST", url, "json", false, {//ADAS
                "vid": vid,
                "sensorID": 225,
                "commandType": 242
            }, perInfoObj.getF3BaseParamCall);
            json_ajax("POST", url, "json", false, {//DSM
                "vid": vid,
                "sensorID": 226,
                "commandType": 242
            }, perInfoObj.getF3BaseParamCall);
        },
        //创建消息监听
        createSocket0104InfoMonitor: function (msg) {
            var msg = $.parseJSON(msg);
            temp_send_vehicle_msg_id = msg.msgId;
            headers = {"UserName": msg.userName};
            clearTimeout(_timeout);
            _timeout = setTimeout(function () {
                if (isRead) {
                    isRead = false;
                    layer.closeAll();
                    $("#readPerInfoRefresh").html("刷新").prop('disabled', false);
                    layer.msg("获取设备数据失败!");
                }
            }, 60000);
        },
        //处理获取设备上传数据
        getSensor0104Param: function (msg, sensorID) {
            if (msg == null) {
                return;
            }
            var result = $.parseJSON(msg.body);
            var msgSNAck = result.data.msgBody.msgSNAck;
            /*if (msgSNAck != temp_send_vehicle_msg_id) {
                return;
            }*/
            clearTimeout(_timeout);
            isRead = false;
            layer.closeAll();
            $("#readPerInfoRefresh").html("刷新").prop('disabled', false);
            var status = result.data.msgBody.result;
            var id = result.data.msgBody.params[0].id;
            if (status == 1) {
                layer.msg("获取设备数据失败!");
                return;
            }
            if (id == "62177" || id == "62178") {//基本信息
                perInfoObj.queryF3BaseParamCall(result, sensorID);
            }
        },
        //基本信息-上报获取基本信息返回处理方法
        queryF3BaseParamCall: function (data, sensorID) {
            var value = data.data.msgBody.params[0].value;
            if (sensorID == '225') {//ADAS
                $("#adas-company").val(value.companyName);//公司名称
                $("#adas-kehuCode").val(value.clientCode);//客户代码
                $("#adas-productCode").val(value.productCode);//产品代码
                $("#adas-deviceId").val(value.sensorID);//设备ID
                $("#adas-softwareVersion").val(value.softwareVersionsCode);//软件版本号
                $("#adas-hardwareVersion").val(value.hardwareVersionsCode);//硬件版本号
            } else {//DSM
                $("#dsm-company").val(value.companyName);//公司名称
                $("#dsm-kehuCode").val(value.clientCode);//客户代码
                $("#dsm-productCode").val(value.productCode);//产品代码
                $("#dsm-deviceId").val(value.sensorID);//设备ID
                $("#dsm-softwareVersion").val(value.softwareVersionsCode);//软件版本号
                $("#dsm-hardwareVersion").val(value.hardwareVersionsCode);//硬件版本号
            }
        },
    }
    $(function () {
        perInfoObj.init(225);//ADAS
        perInfoObj.init(226);//DSM
        //刷新
        $("#readPerInfoRefresh").on("click", perInfoObj.readInformationRefreshClick);
    })
})($, window)