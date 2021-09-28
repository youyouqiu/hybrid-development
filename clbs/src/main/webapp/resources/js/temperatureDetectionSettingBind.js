(function (window, $) {
    var sensorOrder;
    var sensorList = [];//提交的参数
    var flag = false;
    var vehicleList = JSON.parse($("#vehicleList").attr("value"));
    var transduserManage = JSON.parse($("#transduserManage").attr("value"));
    //初始化参考车牌
    var dataList = {value: []};
    var TransduserList = {value: []};
    TemperatureSettings = {
        init: function () {
            if (vehicleList != null && vehicleList.length > 0) {
                var vehicleId = $("#vehicleId").attr("value");
                for (var i = 0; i < vehicleList.length; i++) {
                    var obj = {};
                    obj.id = vehicleList[i].vehicleId;
                    obj.name = vehicleList[i].brand;
                    dataList.value.push(obj);
                }
            }
            $("#brands").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                idField: "id",
                keyField: "name",
                effectiveFields: ["name"],
                searchFields: ["id"],
                data: dataList
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                var vId = keyword.id;
                var url = "/clbs/v/sensorSettings/findVehicleBrand";
                var data = {"id": vId, "sensorType": 1};
                json_ajax("POST", url, "json", false, data, TemperatureSettings.consult);     //发送请求
            }).on('onUnsetSelectValue', function () {
            });
            //传感器型号下拉选
            if (transduserManage != null && transduserManage.length > 0) {
                for (var i = 0; i < transduserManage.length; i++) {
                    var sensor = {};
                    sensor.id = transduserManage[i].id;
                    sensor.name = transduserManage[i].sensorNumber;
                    TransduserList.value.push(sensor);
                }
            }
            //传感器下拉选1
            $("#sensorNumber").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                idField: "id",
                keyField: "name",
                effectiveFields: ["name"],
                searchFields: ["id"],
                data: TransduserList
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                var transduserId = keyword.id;
                $("#brands").val("");
                for (var i = 0; i < transduserManage.length; i++) {
                    if (transduserId == transduserManage[i].id) {
                        $("#errorMsg").hide();
                        $("#sensorId").val(transduserManage[i].id);
                        $("#compensate").val(transduserManage[i].compensate);
                        $("#filterFactor").val(transduserManage[i].filterFactor);
                    }
                }
            }).on('onUnsetSelectValue', function () {
            });
            //传感器下拉选2
            $("#sensorNumber2").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                idField: "id",
                keyField: "name",
                effectiveFields: ["name"],
                searchFields: ["id"],
                data: TransduserList
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                var transduserId = keyword.id;
                for (var i = 0; i < transduserManage.length; i++) {
                    if (transduserId == transduserManage[i].id) {
                        $("#errorMsg2").hide();
                        $("#sensorId2").val(transduserManage[i].id);
                        $("#compensate2").val(transduserManage[i].compensate);
                        $("#filterFactor2").val(transduserManage[i].filterFactor);
                    }
                }
            }).on('onUnsetSelectValue', function () {
            });
            //传感器下拉选3
            $("#sensorNumber3").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                idField: "id",
                keyField: "name",
                effectiveFields: ["name"],
                searchFields: ["id"],
                data: TransduserList
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                var transduserId = keyword.id;
                for (var i = 0; i < transduserManage.length; i++) {
                    if (transduserId == transduserManage[i].id) {
                        $("#errorMsg3").hide();
                        $("#sensorId3").val(transduserManage[i].id);
                        $("#compensate3").val(transduserManage[i].compensate);
                        $("#filterFactor3").val(transduserManage[i].filterFactor);
                    }
                }
            }).on('onUnsetSelectValue', function () {
            });
            //传感器下拉选4
            $("#sensorNumber4").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                idField: "id",
                keyField: "name",
                effectiveFields: ["name"],
                searchFields: ["id"],
                data: TransduserList
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                var transduserId = keyword.id;
                for (var i = 0; i < transduserManage.length; i++) {
                    if (transduserId == transduserManage[i].id) {
                        $("#errorMsg4").hide();
                        $("#sensorId4").val(transduserManage[i].id);
                        $("#compensate4").val(transduserManage[i].compensate);
                        $("#filterFactor4").val(transduserManage[i].filterFactor);
                    }
                }
            }).on('onUnsetSelectValue', function () {
            });
            //传感器下拉选5
            $("#sensorNumber5").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                idField: "id",
                keyField: "name",
                effectiveFields: ["name"],
                searchFields: ["id"],
                data: TransduserList
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                var transduserId = keyword.id;
                for (var i = 0; i < transduserManage.length; i++) {
                    if (transduserId == transduserManage[i].id) {
                        $("#errorMsg5").hide();
                        $("#sensorId5").val(transduserManage[i].id);
                        $("#compensate5").val(transduserManage[i].compensate);
                        $("#filterFactor5").val(transduserManage[i].filterFactor);
                    }
                }
            }).on('onUnsetSelectValue', function () {
            });
        },
        //组装数据
        assembly: function () {
            sensorList = [];//初始化提交数组，防止数据沉积
            var sensorNumber = $("#sensorNumber").val();
            if (sensorNumber != "" && sensorNumber != null) {
                if (TemperatureSettings.efficacy("")) {
                    var sensorId = $("#sensorId").val();
                    var vehicleId = $("#vehicleId").val();
                    var autoTime = $("#autoTime").val();
                    var overValve = $("#overValve").val();
                    var correctionFactorK = $("#correctionFactorK").val();
                    var correctionFactorB = $("#correctionFactorB").val();
                    var alarmUp = $("#alarmUp").val();
                    var alarmDown = $("#alarmDown").val();
                    var remark = $("#remark").val();
                    var compensate = $("#compensate").val();
                    var filterFactor = $("#filterFactor").val();
                    var list1 = [21, sensorId, vehicleId, autoTime, remark, overValve, correctionFactorK, correctionFactorB, alarmUp, alarmDown];
                } else {
                    return;
                }
            }
            var sensorNumber2 = $("#sensorNumber2").val();
            if (sensorNumber2 != "" && sensorNumber2 != null) {
                if (TemperatureSettings.efficacy("2")) {
                    var sensorId2 = $("#sensorId2").val();
                    var vehicleId2 = $("#vehicleId").val();
                    var autoTime2 = $("#autoTime2").val();
                    var overValve2 = $("#overValve2").val();
                    var correctionFactorK2 = $("#correctionFactorK2").val();
                    var correctionFactorB2 = $("#correctionFactorB2").val();
                    var alarmUp2 = $("#alarmUp2").val();
                    var alarmDown2 = $("#alarmDown2").val();
                    var remark2 = $("#remark2").val();
                    var compensate2 = $("#compensate2").val();
                    var filterFactor2 = $("#filterFactor2").val();
                    var list2 = [22, sensorId2, vehicleId2, autoTime2, remark2, overValve2, correctionFactorK2, correctionFactorB2, alarmUp2, alarmDown2];
                } else {
                    return;
                }
            }
            var sensorNumber3 = $("#sensorNumber3").val();
            if (sensorNumber3 != "" && sensorNumber3 != null) {
                if (TemperatureSettings.efficacy("3")) {
                    var sensorId3 = $("#sensorId3").val();
                    var vehicleId3 = $("#vehicleId").val();
                    var autoTime3 = $("#autoTime3").val();
                    var overValve3 = $("#overValve3").val();
                    var correctionFactorK3 = $("#correctionFactorK3").val();
                    var correctionFactorB3 = $("#correctionFactorB3").val();
                    var alarmUp3 = $("#alarmUp3").val();
                    var alarmDown3 = $("#alarmDown3").val();
                    var remark3 = $("#remark3").val();
                    var compensate3 = $("#compensate3").val();
                    var filterFactor3 = $("#filterFactor3").val();
                    var list3 = [23, sensorId3, vehicleId3, autoTime3, remark3, overValve3, correctionFactorK3, correctionFactorB3, alarmUp3, alarmDown3];
                } else {
                    return;
                }
            }
            var sensorNumber4 = $("#sensorNumber4").val();
            if (sensorNumber4 != "" && sensorNumber4 != null) {
                if (TemperatureSettings.efficacy("4")) {
                    var sensorId4 = $("#sensorId4").val();
                    var vehicleId4 = $("#vehicleId").val();
                    var autoTime4 = $("#autoTime4").val();
                    var overValve4 = $("#overValve4").val();
                    var correctionFactorK4 = $("#correctionFactorK4").val();
                    var correctionFactorB4 = $("#correctionFactorB4").val();
                    var alarmUp4 = $("#alarmUp4").val();
                    var alarmDown4 = $("#alarmDown4").val();
                    var remark4 = $("#remark4").val();
                    var compensate4 = $("#compensate4").val();
                    var filterFactor4 = $("#filterFactor4").val();
                    var list4 = [24, sensorId4, vehicleId4, autoTime4, remark4, overValve4, correctionFactorK4, correctionFactorB4, alarmUp4, alarmDown4];
                } else {
                    return;
                }
            }
            var sensorNumber5 = $("#sensorNumber5").val();
            if (sensorNumber5 != "" && sensorNumber5 != null) {
                if (TemperatureSettings.efficacy("5")) {
                    var sensorId5 = $("#sensorId5").val();
                    var vehicleId5 = $("#vehicleId").val();
                    var autoTime5 = $("#autoTime5").val();
                    var overValve5 = $("#overValve5").val();
                    var correctionFactorK5 = $("#correctionFactorK5").val();
                    var correctionFactorB5 = $("#correctionFactorB5").val();
                    var alarmUp5 = $("#alarmUp5").val();
                    var alarmDown5 = $("#alarmDown5").val();
                    var remark5 = $("#remark5").val();
                    var compensate5 = $("#compensate5").val();
                    var filterFactor5 = $("#filterFactor5").val();
                    var list5 = [25, sensorId5, vehicleId5, autoTime5, remark5, overValve5, correctionFactorK5, correctionFactorB5, alarmUp5, alarmDown5];
                } else {
                    return;
                }
            }
            if ((sensorNumber == "" || sensorNumber == null) &&
                (sensorNumber2 == "" || sensorNumber2 == null) &&
                (sensorNumber3 == "" || sensorNumber3 == null) &&
                (sensorNumber4 == "" || sensorNumber4 == null) &&
                (sensorNumber5 == "" || sensorNumber5 == null)
            ) {
                $("#errorMsg").show();
            }
            if (sensorNumber != "" && sensorNumber != null) {
                flag = true;
                sensorList.push(list1);
            }
            if (sensorNumber2 != "" && sensorNumber2 != null) {
                flag = true;
                sensorList.push(list2);
            }
            if (sensorNumber3 != "" && sensorNumber3 != null) {
                flag = true;
                sensorList.push(list3);
            }
            if (sensorNumber4 != "" && sensorNumber4 != null) {
                flag = true;
                sensorList.push(list4);
            }
            if (sensorNumber5 != "" && sensorNumber5 != null) {
                flag = true;
                sensorList.push(list5);
            }
        },
        //提交数据前的效验
        efficacy: function (number) {
            var overValve = parseInt($("#overValve" + number).val());
            var correctionFactorK = parseInt($("#correctionFactorK" + number).val());
            var correctionFactorB = parseInt($("#correctionFactorB" + number).val());
            var alarmUp = $("#alarmUp" + number).val();
            var alarmDown = $("#alarmDown" + number).val();
            var dataId=$("#sensorNumber"+number).attr("data-id");
            if(dataId==""){
                $("#errorMsg"+number).show();
                flag=false;
                return;
            }
            if (overValve == undefined || overValve === "" || isNaN(overValve) || overValve <= 0 || overValve > 65535) {
                layer.msg("温度传感器" + number + ":超出阀值时间阀值输入有误(范围为0~65535之间,不能为空)，请重新输入");
                flag = false;
                return flag;
            }
            if (correctionFactorK == undefined || correctionFactorK === "" || isNaN(correctionFactorK) || correctionFactorK < 1 || correctionFactorK > 200) {
                layer.msg("温度传感器" + number + ":输出修正系数K输入有误(范围为1~200之间,不能为空)，请重新输入");
                flag = false;
                return flag;
            }
            if (correctionFactorB == undefined || correctionFactorB === "" || isNaN(correctionFactorB) || correctionFactorB < 0 || correctionFactorB > 200) {
                layer.msg("温度传感器" + number + ":输出修正系数B输入有误(范围为0~200之间,不能为空)，请重新输入");
                flag = false;
                return flag;
            }
            var reg = /^(\-|\+)?\d+(\.\d+)?$/;//正数、负数、小数校验正则
            if (alarmUp == undefined || alarmUp === "" || isNaN(alarmUp) || !reg.test(alarmUp) || alarmUp < -100 || alarmUp > 200) {
                layer.msg("温度传感器" + number + ":温度报警上阈值输入有误(范围为-100~200之间，不能为空)，请重新输入");
                flag = false;
                return flag;
            }
            if (alarmDown == undefined || alarmDown === "" || isNaN(alarmDown) || !reg.test(alarmDown) || alarmDown < -100 || alarmDown > 200) {
                layer.msg("温度传感器" + number + ":温度报警下阈值输入有误(范围为-100~200之间,不能为空)，请重新输入");
                flag = false;
                return flag;
            }
            if (parseFloat(alarmUp) <= parseFloat(alarmDown)) {
                layer.msg("温度传感器" + number + ":温度报警上阈值必须大于温度报警下阈值，请重新输入");
                flag = false;
                return flag;
            }
            flag = true;
            return flag;
        },
        doSubmit: function () {
            TemperatureSettings.assembly();
            var url = '/clbs/v/sensorSettings/add';
            var sensorType = 1;
            var value = '';
            for(var i = 0; i < sensorList.length; i++) {
                const item = sensorList[i];
                for(var j = 0; j < item.length; j++) {
                    value += item[j]
                }
            }
            value = value.split("").reduce(function (a, b) { a = ((a << 5) - a) + b.charCodeAt(0); return a & a }, 0);

            var data = {"sensorList": sensorList,"avoidRepeatSubmitToken":$("#avoidRepeatSubmitToken").val(),"sensorType":sensorType,'resubmitToken':value};

            if (flag) {
                sensorList.push("");
                address_submit("POST", url, "json", false, data, true, TemperatureSettings.callBack);     //发送请求
            }
        },
        callBack: function (data) {
            if (data.success) {
                layer.msg("绑定成功！");
                $("#commonWin").modal("hide");
            } else {
                layer.msg(data.msg, {move: false});
            }
            myTable.refresh()
        },
        addButton: function (data) {
            if ($("#show3").is(":hidden")) {
                $("#humidityContent").find("div.tab-pane").removeClass("active");
                $("#activeShow li").removeClass("active");
                $("#show3").show();
                $("#show3").addClass("active");
                $("#home3").addClass("active");
                $("#home3").show();
            } else if ($("#show4").is(":hidden")) {
                $("#humidityContent").find("div.tab-pane").removeClass("active");
                $("#activeShow li").removeClass("active");
                $("#show4").show();
                $("#show4").addClass("active");
                $("#home4").addClass("active");
                $("#home4").show();
            } else {
                $("#humidityContent").find("div.tab-pane").removeClass("active");
                $("#activeShow li").removeClass("active");
                $("#show5").show();
                $("#show5").addClass("active");
                $("#home5").addClass("active");
                $("#home5").show();
                $("#addButton").hide();
            }
        },
        clearInput: function () {
            var type = "";
            for (var i = 1; i < 6; i++) {
                if (i == 1) {
                    type = "";
                } else {
                    type = i
                }
                $("#sensorNumber" + type + "").val("");
                $("#compensate" + type + "").val("");
                $("#filterFactor" + type + "").val("");
                $("#remark" + type + "").val("");
            }
        },
        //参考车牌组装数据
        consult: function (data) {
            TemperatureSettings.clearInput();
            var type = "";
            for (var i = 0; i < data.length; i++) {
                if (data[i].sensorOutId == 21) {
                    type = "";
                } else if (data[i].sensorOutId == 22) {
                    type = 2;
                } else if (data[i].sensorOutId == 23) {
                    type = 3;
                    $("#show3").show();
                } else if (data[i].sensorOutId == 24) {
                    type = 4;
                    $("#show3").show();
                    $("#show4").show();
                } else if (data[i].sensorOutId == 25) {
                    type = 5;
                    $("#show3").show();
                    $("#show4").show();
                    $("#show5").show();
                    $("#addButton").hide();
                }
                $("#id" + type + "").val(data[i].id);
                $("#sensorNumber" + type + "").val(data[i].sensorNumber);
                $("#sensorId" + type + "").val(data[i].sensorId);
                $("#autoTime" + type + "").val(data[i].autoTime);
                $("#overValve" + type + "").val(data[i].overValve);
                $("#correctionFactorK" + type + "").val(data[i].correctionFactorK);
                $("#correctionFactorB" + type + "").val(data[i].correctionFactorB);
                $("#alarmUp" + type + "").val(data[i].alarmUp);
                $("#alarmDown" + type + "").val(data[i].alarmDown);
                $("#remark" + type + "").val(data[i].remark);
                $("#compensate" + type + "").val(data[i].compensate);
                $("#filterFactor" + type + "").val(data[i].filterFactor);
            }
        },
    }
    $(function () {
    	$('input').inputClear();
        TemperatureSettings.init();
        $("#doSubmit").bind("click", TemperatureSettings.doSubmit);
        $("#addButton").bind("click", TemperatureSettings.addButton);
    })
})(window, $)