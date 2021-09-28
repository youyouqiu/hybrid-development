(function (window, $) {
    var typeDataList;
    var sesecltTypeId = "";
    var temp_signal_ids = "";
    var isResploseState = false;
    var clearResPlose;
    var params;
    ioParSetting = {
        init: function () {
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
            json_ajax("POST", "/clbs/m/switching/type/addAllowlist", "json", true, null, ioParSetting.initTypeCallback);
            var referVehicleList = JSON.parse($("#referVehicleList").attr("value"));
            // 初始化车辆数据
            var dataList = {value: []};
            if (referVehicleList != null && referVehicleList.length > 0) {
                var brands = $("#brand").val();
                for (var i = 0; i < referVehicleList.length; i++) {
                    var obj = {};
                    //删除相同车牌信息
                    if (referVehicleList[i].brand == brands) {
                        referVehicleList.splice(referVehicleList[i].brand.indexOf(brands), 1);
                    }
                    //处理参考车牌只存在一条数据时防止浏览器抛出错误信息
                    if (referVehicleList[i] == undefined) {
                        dataList.value.push(obj);
                    } else {
                        obj.id = referVehicleList[i].vehicleId;
                        obj.name = referVehicleList[i].brand;
                        dataList.value.push(obj);
                    }
                }
                //取消全选勾
                $("#checkAll").prop('checked', false);
                $("input[name=subChk]").prop("checked", false);
            }
            $("#referBrands").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                idField: "id",
                keyField: "name",
                effectiveFields: ["name"],
                searchFields: ["id"],
                data: dataList
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                // 当选择参考车牌
                var vehicleId = keyword.id;
                $.ajax({
                    type: 'POST',
                    url: '/clbs/m/switching/signal/getParameter_' + vehicleId + '.gsp',
                    async: false,
                    dataType: 'json',
                    success: function (data) {
                        if (data.success) {
                            ioParSetting.hideErrorMsg();
                            ioParSetting.setVehicleData(data.msg);
                        } else {
                            layer.msg(data.msg);
                        }
                    },
                    error: function () {
                        layer.msg(systemError, {move: false});
                    }
                });
            }).on('onUnsetSelectValue', function () {
            });
            $("#zeroTypeColor,#oneTypeColor,#twoTypeColor,#threeTypeColor").removeAttr("class");
            $("#zeroTypeColor,#oneTypeColor,#twoTypeColor,#threeTypeColor").addClass("iostateinfo");
            ioParSetting.IOTypeChangeFn(0);
            ioParSetting.IOTypeChangeFn(1);
            ioParSetting.IOTypeChangeFn(2);
            ioParSetting.IOTypeChangeFn(3);
        },
        //参照车辆设置
        setVehicleData: function (data) {
            data = JSON.parse(data);
            ioParSetting.initInput("sensorType3", "threeType", data.signalThreeName, data.signalThree, data.threeType);
            ioParSetting.initInput("sensorType2", "twoType", data.signalTwoName, data.signalTwo, data.twoType);
            ioParSetting.initInput("sensorType1", "oneType", data.signalOneName, data.signalOne, data.oneType);
            ioParSetting.initInput("sensorType0", "zeroType", data.signalZeroName, data.signalZero, data.zeroType);
            ioParSetting.IOTypeChangeFn(0);
            ioParSetting.IOTypeChangeFn(1);
            ioParSetting.IOTypeChangeFn(2);
            ioParSetting.IOTypeChangeFn(3);
        },
        initInput: function (typeid, type, name, dataid, sesctType) {
            $("#" + typeid).val("");
            $("#" + typeid).removeAttr("alt");
            $("#" + typeid).removeAttr("data-id");
            $("#" + typeid).siblings().find("ul").html("");
            if (dataid.length > 5) {
                $("#" + typeid).val(name);
                $("#" + typeid).attr("alt", name);
                $("#" + typeid).attr("data-id", dataid);
            }
            $("#" + type).find("option").removeAttr("selected");
            $("#" + type).find("option[value='" + sesctType + "']").attr("selected", "selected");
        },
        //初始化类型设置
        initTypeCallback: function (data) {
            var datas = data.obj;
            typeDataList = {
                value: []
            }, i = 0;
            while (i < datas.typeList.length) {
                typeDataList.value.push({
                    name: html2Escape(datas.typeList[i].name),
                    id: datas.typeList[i].id,
                });
                i++;
            }
            console.log(typeDataList);
            ioParSetting.setTypeCallback();
        },
        //初始化类型设置
        setTypeCallback: function () {
            $(".sensorType").bsSuggest({
                indexId: 1,
                indexKey: 0,
                idField: "id",
                keyField: "name",
                effectiveFields: ["name"],
                searchFields: ["name"],
                data: typeDataList
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                $("#referBrands").val("");
                ioParSetting.hideErrorMsg();
                var types = $(".sensorType");
                var sesecltTypeId = "";
                for (var i = 0; i < types.length; i++) {
                    sesecltTypeId += $(types[i]).attr("id") + "_" + $(types[i]).attr("data-id") + "#";
                }
                sesecltTypeId = sesecltTypeId.replace($(this).attr("id") + "_" + keyword.id, "");
                if (sesecltTypeId.indexOf(keyword.id) > -1) {
                    ioParSetting.showErrorMsg(signalChannelExist, $(this).attr("id"));
                    return;
                }
            }).on('onUnsetSelectValue', function () {
            });
        },
        //提交
        doSubmits: function () {
            ioParSetting.hideErrorMsg();
            var three = $("#sensorType3").attr("data-id");
            var two = $("#sensorType2").attr("data-id");
            var one = $("#sensorType1").attr("data-id");
            var zero = $("#sensorType0").attr("data-id");
            if ($("#sensorType3").val() == "" && $("#sensorType2").val() == "" && $("#sensorType1").val() == "" && $("#sensorType0").val() == "") {
                ioParSetting.showErrorMsg(signalChannelNull, "sensorType0");
                return;
            }
            temp_signal_ids = "";
            if (!ioParSetting.checkTypeChange("sensorType0", "zeroType"))
                return;
            temp_signal_ids = zero + "#";
            if (!ioParSetting.checkTypeChange("sensorType1", "oneType"))
                return;
            temp_signal_ids += one + "#";
            if (!ioParSetting.checkTypeChange("sensorType2", "twoType"))
                return;
            temp_signal_ids += two + "#";
            if (!ioParSetting.checkTypeChange("sensorType3", "threeType"))
                return;
            $("#signalThree").val(three);
            $("#signalTwo").val(two);
            $("#signalOne").val(one);
            $("#signalZero").val(zero);
            $("#bindForm").ajaxSubmit(function (data) {
                data = JSON.parse(data);
                if (data.success) {
                    $("#commonWin").modal("hide");
                    //关闭弹窗
                    myTable.refresh()
                } else {
                    layer.msg(data.msg, {move: false});
                    return;
                }
            });
        },
        checkTypeChange: function (typeid, type) {
            var oneFlage = false;
            var one = $("#" + typeid).attr("data-id");
            if (one != "" && one != undefined) {
                if (temp_signal_ids.indexOf(one) > -1) {
                    ioParSetting.showErrorMsg(signalChannelExist, typeid);//"sensorType1"
                    return false;
                }
                oneFlage = true;
            }
            var otval = $("#" + type).val();
            var oneTypeFlage = true;
            if (otval == 0) {
                oneTypeFlage = false;
            }
            if (oneFlage && !oneTypeFlage) {
                ioParSetting.showErrorMsg("请设置为常开/常闭或取消该设置信号口", type);// "oneType"
                return false;
            }
            if (oneTypeFlage && !oneFlage) {
                ioParSetting.showErrorMsg(signalChannelSetNull, typeid);
                return false;
            }
            return true;
        },
        showErrorMsg: function (msg, inputId) {
            if ($("#error_label_add").is(":hidden")) {
                $("#error_label_add").text(msg);
                $("#error_label_add").insertAfter($("#" + inputId));
                $("#error_label_add").show();
            } else {
                $("#error_label_add").is(":hidden");
            }
        },
        //错误提示信息隐藏
        hideErrorMsg: function () {
            $("#error_label_add").hide();
        },
        //下发8102
        sendParams: function () {
            // 订阅
            webSocket.subscribe(headers, "/user/topic/realLocationP", ioParSetting.setDeviceData, null, null);
            $("#zeroTypeColor").removeClass("iostateinfoOn");//开
            $("#oneTypeColor").removeClass("iostateinfoOn");//开
            $("#twoTypeColor").removeClass("iostateinfoOn");//开
            $("#threeTypeColor").removeClass("iostateinfoOn");//开

            $("#zeroTypeColor").removeClass("iostateinfoOff");//开
            $("#oneTypeColor").removeClass("iostateinfoOff");//开
            $("#twoTypeColor").removeClass("iostateinfoOff");//开
            $("#threeTypeColor").removeClass("iostateinfoOff");//开
            var vehicleId = $("#vehicleId").val();
            var vid = $("#ri_vehicle_id").val();
            json_ajax("POST", "/clbs/m/switching/signal/sendPosition", "json", false, {
                "vehicleId": vehicleId
            }, ioParSetting.getSendParamCall);
        },
        getSendParamCall: function (data) {
            if (!data.success) {
                layer.msg(data.msg);
            } else {
                var msg = $.parseJSON(data.msg);
                cmsgSN = msg.msgId;
                var params = [{"vehicleID": $("#vid").val()}]
                var requestStrS = {
                    "desc": {
                        "MsgId": 40964,
                        "UserName": msg.userName,
                        "cmsgSN": cmsgSN
                    },
                    "data": params
                };
                var headers = {"UserName": msg.userName};
                clearTimeout(clearResPlose);
                clearResPlose = window.setTimeout(function () {
                    if (!isResploseState) {
                        isResploseState = true;
                        layer.closeAll();
                        layer.msg("暂未获取到信号信息!");
                    }
                }, 15000);
                isResploseState = false;
//                webSocket.subscribe(headers,"/user/" + msg.userName + "/realLocationP", ioParSetting.setDeviceData, "/app/vehicle/realLocationP", requestStrS);
            }
        },
        setDeviceData: function (data) {
            var data = JSON.parse(data.body).data;
            if (data.msgHead.msgId = 513 && cmsgSN == data.msgBody.msgSNAck) {
                isResploseState = true;
                var msgBody = data.msgBody;
                var ioSignalData = msgBody.gpsInfo.ioSignalData;
                if (ioSignalData != null && ioSignalData.length > 0) {
                    var singal0 = ioSignalData[0].signal0;
                    ioParSetting.changeColor(singal0, "zeroType");
                    var singal1 = ioSignalData[0].signal1;
                    ioParSetting.changeColor(singal1, "oneType");
                    var singal2 = ioSignalData[0].signal2;
                    ioParSetting.changeColor(singal2, "twoType");
                    var singal3 = ioSignalData[0].signal3;
                    ioParSetting.changeColor(singal3, "threeType");
                } else {
                    layer.msg("暂未获取到信号信息!");
                    return;
                }
            }
        },
        changeColor: function (v, id) {
            var type = $("#" + id).val();//1常开  2常关
            if (type == 1 || type == "1") {
                if (v == "0") {
                    $("#" + id + "Color").addClass("iostateinfoOn");//开
                } else if (v == "1") {
                    $("#" + id + "Color").addClass("iostateinfoOff");//关
                }
            }
            if (type == 2 || type == "2") {
                if (v == "0") {
                    $("#" + id + "Color").addClass("iostateinfoOff");//关
                } else if (v == "1") {
                    $("#" + id + "Color").addClass("iostateinfoOn");//开
                }
            }
        },
        //常开 常闭选择
        IOTypeChangeFn: function (id) {
            ioParSetting.hideErrorMsg();
            switch (id) {
                case 0:
                    var ztval = $("#zeroType").val();
                    $("#zeroTypeOrSwitch").removeAttr("class");
                    if (ztval == 1) {
                        $("#zeroTypeOrSwitch").addClass("OpenandclosOn");
                    } else if (ztval == 2) {
                        $("#zeroTypeOrSwitch").addClass("OpenandclosOff");
                    }
                    break;
                case 1:
                    var otval = $("#oneType").val();
                    $("#oneTypeOrSwitch").removeAttr("class");
                    if (otval == 1) {
                        $("#oneTypeOrSwitch").addClass("OpenandclosOn");
                    } else if (otval == 2) {
                        $("#oneTypeOrSwitch").addClass("OpenandclosOff");
                    }
                    break;
                case 2:
                    var ttval = $("#twoType").val();
                    $("#twoTypeOrSwitch").removeAttr("class");
                    if (ttval == 1) {
                        $("#twoTypeOrSwitch").addClass("OpenandclosOn");
                    } else if (ttval == 2) {
                        $("#twoTypeOrSwitch").addClass("OpenandclosOff");
                    }
                    break;
                case 3:
                    var thtval = $("#threeType").val();
                    $("#threeTypeOrSwitch").removeAttr("class");
                    if (thtval == 1) {
                        $("#threeTypeOrSwitch").addClass("OpenandclosOn");
                    } else if (thtval == 2) {
                        $("#threeTypeOrSwitch").addClass("OpenandclosOff");
                    }
                    break;
            }
        },
    }
    $(function () {
        ioParSetting.init();
        $('input').inputClear();
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'sensorType0' || id == 'sensorType1' || id == 'sensorType2' || id == 'sensorType3') {
                $("#" + id).attr("data-id", "");
            }
            ;
        });

        //去掉输入框一键删除按钮
        var textInput = $('input[type="text"]');
        $('input[type="text"]').focus(function () {
            textInput.siblings('i.delIcon').remove();
        });
        $("#doSubmits").bind("click", ioParSetting.doSubmits);
        $("#ioParSettingRefresh").bind("click", ioParSetting.sendParams);
    })
})(window, $)