//# sourceURL=vehiclePollEdit.js
(function (window, $) {
    var addSensorIndex = 2;
    var peripheralDataList;
    var pollingMaxTime;
    var editFlag = false;// 是否是修改界面
    editvehiclePeripheralPolling = {
        init: function () {
            // json_ajax("POST","/clbs/v/sensorConfig/vehiclePeripheral/addAllowlist","json",true,{"id" : ""},editvehiclePeripheralPolling.InitPeripheralCallback);
            var referVehicleList = JSON.parse($("#referVehicleList").attr("value"));
            // 初始化车辆数据
            if ($("#pollingsList").attr("value") != '') {
                // var isShowData=false;
                // var showInterval= setInterval(function(){
                //if(peripheralDataList!=undefined){
                //clearInterval(showInterval);
                var pollingList = JSON.parse($("#pollingsList").attr("value"));
                if (pollingList != null && pollingList.length > 0) {
                    editvehiclePeripheralPolling.setPollingList(pollingList);
                }
                editFlag = true;
                //}
                //},500);
            } else {
                json_ajax("POST", "/clbs/v/sensorConfig/vehiclePeripheral/addAllowlist", "json", true, {"id": ""}, editvehiclePeripheralPolling.InitPeripheralCallback);
            }
            // 初始化车辆数据
            var dataList = {value: []};
            if (referVehicleList != null && referVehicleList.length > 0) {
                for (var i = 0; i < referVehicleList.length; i++) {
                    var obj = {};
                    if ($("#vehicleId").val() != referVehicleList[i].vehicleId) {
                        obj.id = referVehicleList[i].vehicleId;
                        obj.name = referVehicleList[i].plate;
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
                    url: '/clbs/v/sensorConfig/vehiclePoll/getPollingParameter_' + vehicleId + '.gsp',
                    async: false,
                    dataType: 'json',
                    success: function (data) {
                        if (data.obj.length != 0) {
                            editvehiclePeripheralPolling.setPollingList(data.obj);
                        }
                    },
                    error: function () {
                        layer.msg(systemError, {move: false});
                    }
                });
                editvehiclePeripheralPolling.editAdaptiveHeight();
            }).on('onUnsetSelectValue', function () {
            });
        },
        //数据回调
        InitPeripheralCallback: function (data) {
            if (data.success) {
                datas = data.obj;
                peripheralDataList = {
                    value: []
                }, i = 0;
                while (i < datas.peripheralList.length) {
                    peripheralDataList.value.push({
                        name: datas.peripheralList[i].name,
                        id: datas.peripheralList[i].id,
                    });
                    i++;
                }
                editvehiclePeripheralPolling.setPeripheralVal();
            } else {
                layer.msg(data.msg, {move: false});
            }
        },
        addSensorAndTime: function () {
            editvehiclePeripheralPolling.hideErrorMsg();
            var isAllowFlag = editvehiclePeripheralPolling.checkSetSensorType();//允许新增
            if (!isAllowFlag) {
                return;
            }
            addSensorIndex++;
            var sensorTypeFirstTime = $(".sensorTypeFirstTime").val();
            var html =
                '<div class="form-group">' +
                '<label class="col-md-2 control-label"><label class="text-danger">*</label> 传感器类型：</label>' +
                '<div class="col-md-3">' +
                '<div class="input-group">' +
                '<input type="text" class="form-control sensorType"  id="sensorType_' + addSensorIndex + '">' +
                '<input name="sensorType" type="hidden" class="form-control" id="hidden_sensorType_' + addSensorIndex + '">' +
                '<div class="input-group-btn">' +
                '<button type="button" class="btn btn-white dropdown-toggle" data-toggle="dropdown">' +
                '<span class="caret"></span>' +
                '</button>' +
                '<ul class="dropdown-menu dropdown-menu-right" role="menu" style="width:100%;"></ul>' +
                '</div>' +
                '</div>' +
                '</div>' +
                '<label class="col-md-2 control-label"><label class="text-danger">*</label> 轮询时间(s)：</label>' +
                '<div class="col-md-3">' +
                '<input type="text" class="form-control readonly sensorTypeTime" value="' + sensorTypeFirstTime + '" name="pollingTime" readonly id="pollingTime_' + addSensorIndex + '" placeholder="请输入轮询时间"/>' +
                '</div>' +
                '<div class="col-md-1">' +
                '<button type="button" class="btn btn-danger SensorAndTimeDelete deleteIcon"><span class="glyphicon glyphicon-trash" aria-hidden="true"></span></button>' +
                '</div>' +
                '</div>';
            $("#sensorOrTime").append(html);
            var sensorTypeTimeLength = $(".sensorTypeTime").length;
            if (sensorTypeTimeLength > 2) {
                sensorTypeFirstTime = 2 * sensorTypeTimeLength;
                $(".sensorTypeTime").val(sensorTypeFirstTime);
            }
            if (sensorTypeFirstTime == null || sensorTypeFirstTime == '') {
                sensorTypeFirstTime = 5;
            }
            $(".SensorAndTimeDelete").on("click", function () {
                $(this).parent().parent().remove();
            });
            editvehiclePeripheralPolling.setPeripheralVal();
            editvehiclePeripheralPolling.editAdaptiveHeight();
            pollingMaxTime = $(".sensorTypeFirstTime").val();
        },
        pollingTimeVerification: function (index) {
            // 设置
            if (index == undefined) {
                var pollingTypeNum = $("#sensorOrTime").find("div.form-group").length;
                if (pollingTypeNum < 3) {
                    var senTime = $(".sensorTypeFirstTime").val();
                    if (parseInt(senTime) < 3) {
                        editvehiclePeripheralPolling.showErrorMsg("轮询时间不能小于3秒！", "pollingTime");
                        setTimeout(function () {
                            $(".sensorTypeFirstTime").val(3);
                            $(".sensorTypeTime").val(3);
                        }, 300);
                    } else {
                        editvehiclePeripheralPolling.hideErrorMsg();
                    }
                } else {
                    var senTime = $(".sensorTypeFirstTime").val();
                    if (parseInt(senTime) < parseInt(pollingMaxTime)) {
                        editvehiclePeripheralPolling.showErrorMsg("轮询时间不能小于" + pollingMaxTime + "秒！", "pollingTime");
                        setTimeout(function () {
                            $(".sensorTypeFirstTime").val(pollingMaxTime);
                            $(".sensorTypeTime").val(pollingMaxTime);
                        }, 300);
                    } else {
                        editvehiclePeripheralPolling.hideErrorMsg();
                    }
                }
            }
            // 修改
            else {
                var pollingTypeNum = $("#sensorOrTime").find("div.form-group").length;
                var index = $(".sensorTypeFirstTime").attr('id').replace('pollingTime_', '');
                if (pollingTypeNum < 3) {
                    var senTime = $("#pollingTime_" + index).val();
                    if (parseInt(senTime) < 3) {
                        editvehiclePeripheralPolling.showErrorMsg("轮询时间不能小于3秒！", "pollingTime_" + index);
                        setTimeout(function () {
                            $(".sensorTypeFirstTime").val(3);
                            $(".sensorTypeTime").val(3);
                        }, 300);
                    } else {
                        editvehiclePeripheralPolling.hideErrorMsg();
                    }
                } else {
                    var senTime = $("#pollingTime_" + index).val();
                    var pollingTypeNums = $("#sensorOrTime").find("div.form-group").length;
                    var editMinValue = parseInt(pollingTypeNums * 2);
                    if (parseInt(senTime) < editMinValue) {
                        editvehiclePeripheralPolling.showErrorMsg("轮询时间不能小于" + editMinValue + "秒！", "pollingTime_" + index);
                        setTimeout(function () {
                            $(".sensorTypeFirstTime").val(editMinValue);
                            $(".sensorTypeTime").val(editMinValue);
                        }, 300);
                    } else {
                        editvehiclePeripheralPolling.hideErrorMsg();
                    }
                }
            }
        },
        doSubmits: function () {
            editvehiclePeripheralPolling.hideErrorMsg();
            if ($.trim($("#brands").val()) == "") {
                editvehiclePeripheralPolling.showErrorMsg(selectMonitoringObjec, "brands");
                return;
            }
            var timeList = $('input[name="pollingTime"]');
            for (var i = 0; i < timeList.length; i++) {
                var time = $(timeList[i]).val();
                var id = $(timeList[i]).attr('id');
                if (!/^[-\+]?\d+$/.test(time)) {
                    editvehiclePeripheralPolling.showErrorMsg("请输入正确的格式", id);
                    return;
                }
            }
            var isAllowFlag = editvehiclePeripheralPolling.checkSetSensorType();//允许新增
            if (!isAllowFlag) {
                return;
            }
            // $('.sensorTypeFirstTime').blur();
            editvehiclePeripheralPolling.pollingTimeVerification(editFlag ? 1 : undefined);
            if ($('label.error:visible').length > 0) return;
            addHashCode($("#editForm"));
            $("#editForm").ajaxSubmit(function (data) {
                var data = $.parseJSON(data);
                if (data.success) {
                    $("#commonWin").modal("hide");
                    layer.msg(data.msg, {move: false});
                    /* 关闭弹窗 */
                    myTable.refresh();
                } else {
                    layer.msg(data.msg, {move: false});
                }
            });
        },
        showErrorMsg: function (msg, inputId) {
            var error = $("#error_label_add").length;
            if (error == 0) {
                $(".modal-footer").append("<label id='error_label_add' class='error' style='display: none;'></label>");
            }
            var target = $("#" + inputId).siblings('#error_label_add');
            if (target.length > 0) {
                if (target.is(":hidden")) {
                    target.html(msg);
                    target.show();
                } else {
                    target.html(msg);
                }
            } else {
                $("<label id='error_label_add' class='error'>" + msg + "</label>").insertAfter($("#" + inputId));
            }

        },
        //错误提示信息隐藏
        hideErrorMsg: function () {
            $("#vehicleModalBody label.error").hide();
            $("#vehicleModalBody label.error").remove();
        },
        setPollingList: function (pollingList) {
            $("#sensorOrTime").html("");
            for (var i = 0; i < pollingList.length; i++) {
                addSensorIndex++;
                if (i == 0) {
                    var html =
                        '<div class="form-group">' +
                        '<label class="col-md-2 control-label"><label class="text-danger">*</label> 传感器类型：</label>' +
                        '<div class="col-md-3">' +
                        '<div class="input-group">' +
                        '<input type="text" class="form-control sensorType" data-id="' + pollingList[i].sensorType + '" value="' + pollingList[i].pollingName + '" id="sensorType">' +
                        '<input name="sensorType" type="hidden" value="' + pollingList[i].sensorType + '" class="form-control" id="hidden_sensorType">' +
                        '<div class="input-group-btn">' +
                        '<button type="button" class="btn btn-white dropdown-toggle" data-toggle="dropdown">' +
                        '<span class="caret"></span>' +
                        '</button>' +
                        '<ul class="dropdown-menu dropdown-menu-right" role="menu"></ul>' +
                        '</div>' +
                        '</div>' +
                        '</div>' +
                        '<label class="col-md-2 control-label"><label class="text-danger">*</label> 轮询时间(s)：</label>' +
                        '<div class="col-md-3">' +
                        '<input type="text" class="form-control sensorTypeTime sensorTypeFirstTime" onBlur="editvehiclePeripheralPolling.pollingTimeVerification(' + addSensorIndex + ')" value="' + pollingList[i].pollingTime + '" name="pollingTime"  id="pollingTime_' + addSensorIndex + '" onkeyup="value=value.replace(/[^0-9]/g,\'\')" placeholder="请输入轮询时间"/>' +
                        '</div>' +
                        '<div class="col-md-1">' +
                        '<button id="sensorOrTime-add-btn" onclick="editvehiclePeripheralPolling.addSensorAndTime();" type="button" class="btn btn-primary addIcon"><span class="glyphicon glyphiconPlus" aria-hidden="true"></span></button>' +
                        '</div>' +
                        '</div>';
                    $("#sensorOrTime").append(html);
                } else {
                    var html =
                        '<div class="form-group">' +
                        '<label class="col-md-2 control-label"><label class="text-danger">*</label> 传感器类型：</label>' +
                        '<div class="col-md-3">' +
                        '<div class="input-group">' +
                        '<input type="text" class="form-control sensorType" data-id="' + pollingList[i].sensorType + '" value="' + pollingList[i].pollingName + '" id="sensorType_' + addSensorIndex + '">' +
                        '<input name="sensorType" type="hidden" value="' + pollingList[i].sensorType + '" class="form-control" id="hidden_sensorType_' + addSensorIndex + '">' +
                        '<div class="input-group-btn">' +
                        '<button type="button" class="btn btn-white dropdown-toggle" data-toggle="dropdown">' +
                        '<span class="caret"></span>' +
                        '</button>' +
                        '<ul class="dropdown-menu dropdown-menu-right" role="menu" style="width:100%;"></ul>' +
                        '</div>' +
                        '</div>' +
                        '</div>' +
                        '<label class="col-md-2 control-label"><label class="text-danger">*</label> 轮询时间(s)：</label>' +
                        '<div class="col-md-3">' +
                        '<input type="text" class="form-control readonly sensorTypeTime" value="' + pollingList[i].pollingTime + '" name="pollingTime" readonly id="pollingTime_' + addSensorIndex + '" placeholder="请输入轮询时间"/>' +
                        '</div>' +
                        '<div class="col-md-1">' +
                        '<button type="button" class="btn btn-danger SensorAndTimeDelete deleteIcon"><span class="glyphicon glyphicon-trash" aria-hidden="true"></span></button>' +
                        '</div>' +
                        '</div>';
                    $("#sensorOrTime").append(html);
                }
                $(".SensorAndTimeDelete").on("click", function () {
                    $(this).parent().parent().remove();
                });
                editvehiclePeripheralPolling.setPeripheralVal();
                editvehiclePeripheralPolling.editAdaptiveHeight();
                $(".modal-footer").append("<label id='error_label_add' class='error' style='display: none;'></label>");
                $(".sensorTypeFirstTime").bind("keyup", function () {
                    var time = $(".sensorTypeFirstTime").val();
                    if (time > 255)
                        time = 255;
                    $(".sensorTypeTime").val(time);
                });
                editPollingMaxTime = $(".sensorTypeFirstTime").val();
            }
            setTimeout('$(".delIcon").hide()', 100);
            json_ajax("POST", "/clbs/v/sensorConfig/vehiclePeripheral/addAllowlist", "json", true, {"id": ""}, editvehiclePeripheralPolling.InitPeripheralCallback)
        },
        setPeripheralVal: function () {
            if (peripheralDataList != undefined) {
                $(".sensorType").bsSuggest({
                    indexId: 1,
                    indexKey: 0,
                    keyField: "name",
                    effectiveFields: ["name"],
                    searchFields: ["name"],
                    data: peripheralDataList
                }).on('onDataRequestSuccess', function (e, result) {

                }).on('onSetSelectValue', function (e, keyword, data) {
                    $(this).siblings("#error_label_add").remove();
                }).on('onUnsetSelectValue', function () {

                });
            }
            // 解决IE浏览器下拉表默认展开问题
            setTimeout(function () {
                $('.dropdown-menu-right').hide();
            }, 500);
        },
        checkSetSensorType: function () {
            var isAllowFlag = true;//允许新增
            var typeids = "";
            $(".sensorType").each(function (index, obj) {
                var id = $(this).attr("id");
                var dataId = $(this).attr("data-id");
                if (obj.value == null || obj.value == '' || !dataId) {
                    editvehiclePeripheralPolling.showErrorMsg(peripheralPollTypeNull, id);
                    isAllowFlag = false;
                    return isAllowFlag;
                }
                var hid = "#hidden_" + id;
                var typeid = $(this).attr("data-id");
                if (typeid == "") {
                    typeid = $(hid).val();
                }
                var typeName = $(this).attr("value");
                var reg = new RegExp("^.*" + typeid + ".*$");
                if (reg.test(typeids)) {
                    editvehiclePeripheralPolling.showErrorMsg(peripheralPollTypeError, id);
                    isAllowFlag = false;
                }
                typeids += typeid + "#";
                $(hid).val(typeid);
            });
            $(".sensorTypeTime").each(function (index, obj) {
                if (obj.value == null || obj.value == '') {
                    var id = $(this).attr("id");
                    editvehiclePeripheralPolling.showErrorMsg(peripheralPollTimeNull, id);
                    isAllowFlag = false;
                }
                if (obj.value.length > 255) {
                    var id = $(this).attr("id");
                    editvehiclePeripheralPolling.showErrorMsg(peripheralPollMaxTime, id);
                    isAllowFlag = false;
                }
            });
            return isAllowFlag;
        },
        editAdaptiveHeight: function () {
            var wh = $(window).height();
            if (wh != "" || wh != null || wh != undefined) {
                var vmb = wh - 199;
                var sstNum = Math.floor(vmb / 53);
                var fgLength = $("#vehicleModalBody>div>div").find("div.form-group").length;
                if (fgLength >= sstNum) {
                    $("#vehicleModalBody").css({"overflow-y": "auto", "overflow-x": "hidden"});
                } else {
                    $("#vehicleModalBody").css({"overflow-y": "visible", "overflow-x": "visible"});
                }
            }
        },
    }
    $(function () {
        $('input').inputClear();
        editvehiclePeripheralPolling.init();
        $(".delIcon").on("click", function () {
            $(".dropdown-menu-right").css("display", "block");
        })
        $("#doSubmits").bind("click", editvehiclePeripheralPolling.doSubmits);
        $(".sensorTypeFirstTime").bind("keyup", function () {
            var time = $(".sensorTypeFirstTime").val();
            if (time > 255)
                time = 255;
            $(".sensorTypeTime").val(time);
        });
    })
})(window, $)