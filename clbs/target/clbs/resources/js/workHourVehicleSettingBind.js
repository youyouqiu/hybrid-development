//# sourceURL=workHourVehicleSettingBind.js

(function (window, $) {
    // 车辆list
    var vehicleList = JSON.parse($("#vehicleList").attr("value"));
    // 初始化车辆数据
    var dataList = {value: []};
    //初始化油箱参数
    var fuelDataList = {value: []};
    var sensorList = {value: []};

    bindObj = {
        init: function () {
            $(".modal-body").addClass("modal-body-overflow");
            $(".modal-body").css({"height": "auto", "max-height": ($(window).height() - 194) + "px"});


            bindObj.initSensor();
            bindObj.initRefer();
        },
        /**
         * 初始化参考对象
         */
        initRefer: function () {
            if (vehicleList !== null && vehicleList.length > 0) {
                var vehicleId = $("#vehicleId").val();
                for (i = 0; i < vehicleList.length; i++) {
                    var obj = {};
                    //删除相同车牌信息
                    if (vehicleList[i].vehicleId === vehicleId) {
                        vehicleList.splice(vehicleList[i].vehicleId.indexOf(vehicleId), 1);
                    }
                    //处理参考车牌只存在一条数据时防止浏览器抛出错误信息
                    if (vehicleList[i] === undefined) {
                        dataList.value.push(obj);
                    } else {
                        obj.id = vehicleList[i].vehicleId;
                        obj.name = vehicleList[i].plateNumber;
                        dataList.value.push(obj);
                    }
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
                // 从服务器请求选中的参考车牌的监控对象的信息
                // 当选择参考车牌
                var vehicleId = keyword.id;
                if (vehicleId != null && vehicleId != undefined && vehicleId != "") {
                    // 发送请求
                    var url = "/clbs/v/workhourmgt/workhoursetting/getWorkHourBindInfo";
                    var datas = {"vehicleId": vehicleId};
                    $.ajax({
                        type: 'POST',
                        url: url,
                        async: false,
                        dataType: 'json',
                        data: datas,
                        success: function (data) {
                            if (data.success) {
                                var sensorInfo = data.obj;
                                if (sensorInfo) {
                                    if (sensorInfo.sensorId && sensorInfo.sensorId.length > 0) {
                                        $('#sensorId1').val(sensorInfo.sensorNumber).data('id', sensorInfo.sensorId);
                                        $('#formSensorId1').val(sensorInfo.sensorId);
                                        bindObj.renderInput(sensorInfo, 1);

                                    }
                                    if (sensorInfo.twoSensorId && sensorInfo.twoSensorId.length > 0) {
                                        $('#sensorId2').val(sensorInfo.twoSensorNumber).data('id', sensorInfo.twoSensorId);
                                        $('#formSensorId2').val(sensorInfo.twoSensorId);
                                        var sensorTwo = {
                                            id: sensorInfo.twoSensorId,
                                            detectionMode: sensorInfo.twoDetectionMode,
                                            compensate: sensorInfo.twoCompensate,
                                            oddEvenCheck: sensorInfo.twoOddEvenCheck,
                                            baudRate: sensorInfo.twoBaudRate,
                                            filterFactor: sensorInfo.twoFilterFactor,
                                            lastTime: sensorInfo.twoLastTime,
                                            threshold: sensorInfo.twoThreshold,
                                            smoothingFactor: sensorInfo.twoSmoothingFactor,
                                            thresholdVoltage: sensorInfo.twoThresholdVoltage,
                                            // thresholdWorkFlow: sensorInfo.twoThresholdWorkFlow,
                                            thresholdStandbyAlarm: sensorInfo.twoThresholdStandbyAlarm,
                                            baudRateCalculateNumber: sensorInfo.twoBaudRateCalculateNumber,
                                            baudRateThreshold: sensorInfo.twoBaudRateThreshold,
                                            baudRateCalculateTimeScope: sensorInfo.twoBaudRateCalculateTimeScope
                                        };
                                        bindObj.renderInput(sensorTwo, 2);
                                    }
                                } else {
                                    layer.msg('该参考对象没有传感器信息');
                                }
                            }
                        },
                        error: function () {
                            layer.msg(systemError, {move: false});
                        }
                    });

                }
            }).on('onUnsetSelectValue', function () {
            });
            // 解决IE浏览器下拉表默认展开问题
            setTimeout(function () {
                $('.dropdown-menu-right').hide();
            }, 0);
        },
        /**
         * 初始化传感器
         */
        initSensor: function () {
            var _initSensor = function (sensorList, id) {
                $(id).bsSuggest("destroy"); // 销毁事件
                $(id).bsSuggest({
                    indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                    indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                    idField: "id",
                    keyField: "sensorNumber",
                    effectiveFields: ["sensorNumber"],
                    searchFields: ["id"],
                    data: sensorList
                }).on('onDataRequestSuccess', function (e, result) {
                }).on('onSetSelectValue', function (e, keyword, data) {
                    var id = keyword.id;
                    var sensor = {};
                    if (id != null && id !== undefined && id !== "") {
                        sensor = bindObj.getSensorData(id);
                        var tabIndex = bindObj.getCurrentActiveTabIndex();
                        $('#formSensorId' + tabIndex).val(id);
                    }
                    bindObj.renderInput(sensor);
                }).on('onUnsetSelectValue', function () {
                    bindObj.renderInput({});
                    var tabIndex = bindObj.getCurrentActiveTabIndex();
                    $('#formSensorId' + tabIndex).val('');
                }).on('onClearSelectValue', function () {
                    bindObj.renderInput({});
                    var tabIndex = bindObj.getCurrentActiveTabIndex();
                    $('#formSensorId' + tabIndex).val('');
                });
            }
            $.ajax({
                type: 'POST',
                url: '/clbs/v/sensorSettings/findSensorInfo',
                async: false,
                dataType: 'json',
                data: {sensorType: 4},
                success: function (data) {
                    if (data.success) {
                        var sensors = data.obj;
                        sensorList.value = sensors;
                        _initSensor({value: sensors}, '#sensorId1');
                        _initSensor({value: sensors}, '#sensorId2');
                    }
                },
                error: function () {
                    layer.msg(systemError, {move: false});
                }
            });

        },
        /**
         * 从传感器列表中获取数据
         * @param id
         * @returns {*}
         */
        getSensorData: function (id) {
            for (var i = 0; i < sensorList.value.length; i++) {
                var item = sensorList.value[i];
                if (item.id === id) {
                    return item;
                    return item;
                }
            }
            return null;
        },
        /**
         * 渲染输入框
         * @param sensor
         * @param tabIndexParam
         */
        renderInput: function (sensor, tabIndexParam) {
            var tabIndex = tabIndexParam || bindObj.getCurrentActiveTabIndex();
            var container = $('#egine' + tabIndex);
            var detectionMode = {"1": "电压比较式", "2": "油耗阈值式", "3": "油耗波动式"}[sensor.detectionMode];
            var compensate = {"1": "使能", "2": "禁用"}[sensor.compensate];
            var oddEvenCheck = {"1": "奇校验", "2": "偶校验", "3": "无校验"}[sensor.oddEvenCheck];
            var baudRate = {
                "1": "2400",
                "2": "4800",
                "3": "9600",
                "4": "19200",
                "5": "38400",
                "6": "57600",
                "7": "115200"
            }[sensor.baudRate];
            var filterFactor = {"1": "实时", "2": "平滑", "3": "平稳"}[sensor.filterFactor];

            if (tabIndex == 1) {
                container.find('[name="detectionMode"]').val(detectionMode);
                container.find('[name="compensate"]').val(compensate);
                container.find('[name="oddEvenCheck"]').val(oddEvenCheck);
                container.find('[name="baudRate"]').val(baudRate);
                container.find('[name="filterFactor"]').val(filterFactor);

                // 个性参数
                if (sensor.detectionMode == "2") { //油耗阈值式
                    $('#wfPerson' + tabIndex).show();
                    $('#yaPerson' + tabIndex).hide();
                    $('#liuPerson' + tabIndex).hide();
                    container.find('[name="lastTime"]').val(sensor.lastTime ? sensor.lastTime : '10');
                    container.find('[name="threshold"]').val(sensor.threshold ? sensor.threshold : '50');
                    container.find('[name="smoothingFactor"]').val(sensor.smoothingFactor ? sensor.smoothingFactor : '15');
                    // container.find('[name="thresholdWorkFlow"]').val(sensor.thresholdWorkFlow ? sensor.thresholdWorkFlow : '60');
                } else if (sensor.detectionMode == "3") { // 油耗波动式
                    $('#wfPerson' + tabIndex).hide();
                    $('#yaPerson' + tabIndex).hide();
                    $('#liuPerson' + tabIndex).show();
                    container.find('[name="lastTime"]').val(sensor.lastTime ? sensor.lastTime : '10');
                    container.find('[name="baudRateCalculateNumber"]').val(sensor.baudRateCalculateNumber ? sensor.baudRateCalculateNumber : '8');
                    container.find('[name="smoothingFactor"]').val(sensor.smoothingFactor ? sensor.smoothingFactor : '15');
                    container.find('[name="baudRateThreshold"]').val(sensor.baudRateThreshold ? sensor.baudRateThreshold : '8');
                    container.find('[name="baudRateCalculateTimeScope"]').val(sensor.baudRateCalculateTimeScope ? sensor.baudRateCalculateTimeScope : '4');
                    container.find('[name="speedThreshold"]').val(sensor.speedThreshold ? sensor.speedThreshold : '6');
                } else { // 电压比较式
                    $('#yaPerson' + tabIndex).show();
                    $('#liuPerson' + tabIndex).hide();
                    $('#wfPerson' + tabIndex).hide();
                    container.find('[name="lastTime"]').val(sensor.lastTime ? sensor.lastTime : '10');
                    container.find('[name="thresholdVoltage"]').val(sensor.thresholdVoltage ? sensor.thresholdVoltage : '24.2');
                }
            } else {
                container.find('[name="twoDetectionMode"]').val(detectionMode);
                container.find('[name="twoCompensate"]').val(compensate);
                container.find('[name="twoOddEvenCheck"]').val(oddEvenCheck);
                container.find('[name="twoBaudRate"]').val(baudRate);
                container.find('[name="twoFilterFactor"]').val(filterFactor);

                // 个性参数
                if (sensor.detectionMode == "2") {
                    $('#wfPerson' + tabIndex).show();
                    $('#yaPerson' + tabIndex).hide();
                    $('#liuPerson' + tabIndex).hide();
                    container.find('[name="twoLastTime"]').val(sensor.lastTime ? sensor.lastTime : '10');
                    container.find('[name="twoThreshold"]').val(sensor.threshold ? sensor.threshold : '50');
                    container.find('[name="twoSmoothingFactor"]').val(sensor.smoothingFactor ? sensor.smoothingFactor : '15');
                    // container.find('[name="twoThresholdWorkFlow"]').val(sensor.thresholdWorkFlow ? sensor.thresholdWorkFlow : '60');
                } else if (sensor.detectionMode == "3") {
                    $('#wfPerson' + tabIndex).hide();
                    $('#yaPerson' + tabIndex).hide();
                    $('#liuPerson' + tabIndex).show();
                    container.find('[name="twoLastTime"]').val(sensor.lastTime ? sensor.lastTime : '10');
                    container.find('[name="twoBaudRateCalculateNumber"]').val(sensor.baudRateCalculateNumber ? sensor.baudRateCalculateNumber : '8');
                    container.find('[name="twoSmoothingFactor"]').val(sensor.smoothingFactor ? sensor.smoothingFactor : '15');
                    container.find('[name="twoBaudRateThreshold"]').val(sensor.baudRateThreshold ? sensor.baudRateThreshold : '8');
                    container.find('[name="twoBaudRateCalculateTimeScope"]').val(sensor.twoBaudRateCalculateTimeScope ? sensor.twoBaudRateCalculateTimeScope : '4');
                    container.find('[name="twoSpeedThreshold"]').val(sensor.twoSpeedThreshold ? sensor.twoSpeedThreshold : '6');
                } else {
                    // 电压
                    $('#yaPerson' + tabIndex).show();
                    $('#liuPerson' + tabIndex).hide();
                    $('#wfPerson' + tabIndex).hide();
                    container.find('[name="twoLastTime"]').val(sensor.lastTime ? sensor.lastTime : '10');
                    container.find('[name="twoThresholdVoltage"]').val(sensor.thresholdVoltage ? sensor.thresholdVoltage : '24.2');
                }
            }


        },
        /**
         * 获取当前tab的索引
         * @returns {number} 1：发动机1；2：发动机2
         */
        getCurrentActiveTabIndex: function () {
            if ($('#egine1Title').hasClass('active')) return 1;
            return 2;
        },


        // 提交
        doSubmit: function () {
            var toPost = {
                vehicleId: $('#vehicleId').val(),
                plateNumber: $('#vehicleBrand').val(),
                monitorType: $('#monitorType').val(),
                avoidRepeatSubmitToken: $('#avoidRepeatSubmitToken').val()
            };
            bindObj.hideErrorMsg();
            // 校验发动机1
            var sensorId = $('#formSensorId1').val();
            if (sensorId.length > 0) {
                if ($('#sensorId1').val().length == 0) {
                    toPost.sensorId = null;
                    sensorId = null;
                } else {
                    toPost.sensorSequence = 0;
                    toPost.sensorId = sensorId;
                    toPost.sensorNumber = $('#sensorId1').val();
                    var detectionMode = $('#detectionMode').val();
                    if (detectionMode == "电压比较式") {
                        var lastTimeYa = $('#lastTimeYa').val();
                        if (lastTimeYa.length == 0) {
                            bindObj.showErrorMsg(workHourLastTimeNull, '#lastTimeYa');
                            return;
                        }
                        if (isNaN(lastTimeYa) || parseFloat(lastTimeYa) != parseInt(lastTimeYa)) {
                            bindObj.showErrorMsg(workHourLastTimeInteger, '#lastTimeYa');
                            return;
                        }
                        lastTimeYa = parseInt(lastTimeYa);
                        if (lastTimeYa < 1 || lastTimeYa > 60) {
                            bindObj.showErrorMsg(workHourLastTimeRange, '#lastTimeYa');
                            return;
                        }
                        toPost.lastTime = lastTimeYa;
                        var thresholdVoltage = $('#thresholdVoltage').val();
                        if (thresholdVoltage.length == 0) {
                            bindObj.showErrorMsg(workHourThresholdVoltageNull, '#thresholdVoltage');
                            return;
                        }
                        if (isNaN(thresholdVoltage)) {
                            bindObj.showErrorMsg(workHourThresholdVoltageInteger, '#thresholdVoltage');
                            return;
                        }
                        thresholdVoltage = parseFloat(thresholdVoltage);
                        if (thresholdVoltage < 1 || thresholdVoltage > 6000) {
                            bindObj.showErrorMsg(workHourThresholdVoltageRange, '#thresholdVoltage');
                            return;
                        }
                        toPost.thresholdVoltage = thresholdVoltage.toFixed(1);
                    } else if (detectionMode == "油耗阈值式") {
                        var lastTimeWf = $('#lastTimeWf').val();
                        if (lastTimeWf.length == 0) {
                            bindObj.showErrorMsg(workHourLastTimeNull, '#lastTimeWf');
                            return;
                        }
                        if (isNaN(lastTimeWf) || parseFloat(lastTimeWf) != parseInt(lastTimeWf)) {
                            bindObj.showErrorMsg(workHourLastTimeInteger, '#lastTimeWf');
                            return;
                        }
                        lastTimeWf = parseInt(lastTimeWf);
                        if (lastTimeWf < 1 || lastTimeWf > 60) {
                            bindObj.showErrorMsg(workHourLastTimeRange, '#lastTimeWf');
                            return;
                        }
                        toPost.lastTime = lastTimeWf;

                        var baudRateThreshold = $('#threshold1').val();
                        if (baudRateThreshold.length == 0) {
                            bindObj.showErrorMsg('输入范围1.00-600', '#threshold1');
                            return;
                        }
                        if (isNaN(baudRateThreshold) || parseFloat(baudRateThreshold) != parseFloat(baudRateThreshold).toFixed(2)) {
                            bindObj.showErrorMsg('输入范围1.00-600', '#threshold1');
                            return;
                        }
                        baudRateThreshold = parseFloat(baudRateThreshold);
                        if (baudRateThreshold < 1 || baudRateThreshold > 600) {
                            bindObj.showErrorMsg('输入范围1.00-600', '#threshold1');
                            return;
                        }
                        toPost.threshold = baudRateThreshold.toFixed(2);

                        var smoothingFactor = $('#smoothingFactor3').val();
                        if (smoothingFactor.length === 0) {
                            bindObj.showErrorMsg(workHourSmoothingFactorNull, '#smoothingFactor3');
                            return;
                        }
                        if (isNaN(smoothingFactor) || parseFloat(smoothingFactor) != parseInt(smoothingFactor)) {
                            bindObj.showErrorMsg(workHourSmoothingFactorInteger, '#smoothingFactor3');
                            return;
                        }
                        smoothingFactor = parseInt(smoothingFactor);
                        if (smoothingFactor < 5 || smoothingFactor > 100) {
                            bindObj.showErrorMsg(workHourSmoothingFactorRange, '#smoothingFactor3');
                            return;
                        }
                        toPost.smoothingFactor = smoothingFactor;

                        /*var thresholdWorkFlow = $('#thresholdWorkFlow').val();
                        if (thresholdWorkFlow.length == 0){
                            bindObj.showErrorMsg(workHourThresholdWorkFlowNull,'#thresholdWorkFlow');
                            return;
                        }
                        if (isNaN(thresholdWorkFlow) ){
                            bindObj.showErrorMsg(workHourThresholdWorkFlowInteger, '#thresholdWorkFlow');
                            return;
                        }
                        thresholdWorkFlow = parseFloat(thresholdWorkFlow);
                        if (thresholdWorkFlow < 1 || thresholdWorkFlow > 600){
                            bindObj.showErrorMsg(workHourThresholdWorkFlowRange,'#thresholdWorkFlow');
                            return;
                        }
                        toPost.thresholdWorkFlow = thresholdWorkFlow.toFixed(1);*/
                    } else {
                        // 油耗波动式
                        var lastTimeLiu = $('#lastTimeLiu').val();
                        if (lastTimeLiu.length == 0) {
                            bindObj.showErrorMsg(workHourLastTimeNull, '#lastTimeLiu');
                            return;
                        }
                        if (isNaN(lastTimeLiu) || parseFloat(lastTimeLiu) != parseInt(lastTimeLiu)) {
                            bindObj.showErrorMsg(workHourLastTimeInteger, '#lastTimeLiu');
                            return;
                        }
                        lastTimeLiu = parseInt(lastTimeLiu);
                        if (lastTimeLiu < 1 || lastTimeLiu > 60) {
                            bindObj.showErrorMsg(workHourLastTimeRange, '#lastTimeLiu');
                            return;
                        }
                        toPost.lastTime = lastTimeLiu;

                        var baudRateCalculateNumber = $('#baudRateCalculateNumber').val();
                        if (baudRateCalculateNumber.length == 0) {
                            bindObj.showErrorMsg(workHourBaudRateCalculateNumberNull, '#baudRateCalculateNumber');
                            return;
                        }
                        if (isNaN(baudRateCalculateNumber)) {
                            bindObj.showErrorMsg(workHourBaudRateCalculateNumberInteger, '#baudRateCalculateNumber');
                            return;
                        }
                        baudRateCalculateNumber = parseFloat(baudRateCalculateNumber);
                        if (baudRateCalculateNumber < 4 || baudRateCalculateNumber > 12) {
                            bindObj.showErrorMsg(workHourBaudRateCalculateNumberRange, '#baudRateCalculateNumber');
                            return;
                        }
                        toPost.baudRateCalculateNumber = baudRateCalculateNumber;

                        var smoothingFactor = $('#smoothingFactor').val();
                        if (smoothingFactor.length === 0) {
                            bindObj.showErrorMsg(workHourSmoothingFactorNull, '#smoothingFactor');
                            return;
                        }
                        if (isNaN(smoothingFactor) || parseFloat(smoothingFactor) != parseInt(smoothingFactor)) {
                            bindObj.showErrorMsg(workHourSmoothingFactorInteger, '#smoothingFactor');
                            return;
                        }
                        smoothingFactor = parseInt(smoothingFactor);
                        if (smoothingFactor < 5 || smoothingFactor > 100) {
                            console.log(bindObj)
                            bindObj.showErrorMsg(workHourSmoothingFactorRange, '#smoothingFactor');
                            return;
                        }
                        toPost.smoothingFactor = smoothingFactor;

                        var baudRateThreshold = $('#baudRateThreshold').val();
                        if (baudRateThreshold.length == 0) {
                            bindObj.showErrorMsg(workHourBaudRateThresholdNull, '#baudRateThreshold');
                            return;
                        }
                        if (isNaN(baudRateThreshold) || parseFloat(baudRateThreshold) != parseFloat(baudRateThreshold).toFixed(1)) {
                            bindObj.showErrorMsg(workHourBaudRateThresholdInteger, '#baudRateThreshold');
                            return;
                        }
                        baudRateThreshold = parseFloat(baudRateThreshold);
                        if (baudRateThreshold < 1 || baudRateThreshold > 600) {
                            bindObj.showErrorMsg(workHourBaudRateThresholdRange, '#baudRateThreshold');
                            return;
                        }
                        toPost.baudRateThreshold = baudRateThreshold.toFixed(1);

                        var baudRateCalculateTimeScope = $('#baudRateCalculateTimeScope').val();
                        if (baudRateCalculateTimeScope.length == 0) {
                            bindObj.showErrorMsg("波动计算时段不能为空", '#baudRateCalculateTimeScope');
                            return;
                        }
                        if (isNaN(baudRateCalculateTimeScope)
                            || parseFloat(baudRateCalculateTimeScope) != parseInt(baudRateCalculateTimeScope)) {
                            bindObj.showErrorMsg("波动计算时段 必须为正整数", '#baudRateCalculateTimeScope');
                            return;
                        }
                        toPost.baudRateCalculateTimeScope = baudRateCalculateTimeScope;

                        var speedThreshold = $('#speedThreshold').val();
                        if (speedThreshold.length == 0) {
                            bindObj.showErrorMsg(workSpeedThresholdNull, '#speedThreshold');
                            return;
                        }
                        if (isNaN(speedThreshold) || parseFloat(speedThreshold) != parseFloat(speedThreshold).toFixed(1)) {
                            bindObj.showErrorMsg(workSpeedThresholdInteger, '#speedThreshold');
                            return;
                        }
                        speedThreshold = parseFloat(speedThreshold);
                        if (speedThreshold < 1 || speedThreshold > 200) {
                            bindObj.showErrorMsg(workSpeedThresholdRange, '#speedThreshold');
                            return;
                        }
                        toPost.speedThreshold = speedThreshold;
                    }
                }

            }
            // 校验发动机2
            var twoSensorId = $('#formSensorId2').val();
            if (twoSensorId.length > 0) {
                if (sensorId.length === 0) {
                    layer.msg('请先配置发动机1');
                    return;
                }
                if ($('#sensorId2').val().length == 0) {
                    toPost.twoSensorId = null;
                } else {
                    toPost.twoSensorSequence = 1;
                    toPost.twoSensorId = twoSensorId;
                    toPost.twoSensorNumber = $('#sensorId2').val();
                    var twoDetectionMode = $('#twoDetectionMode').val();
                    console.log(twoDetectionMode);
                    if (twoDetectionMode == "电压比较式") {
                        var twoLastTimeYa = $('#twoLastTimeYa').val();
                        if (twoLastTimeYa.length == 0) {
                            bindObj.showErrorMsg(workHourLastTimeNull, '#twoLastTimeYa');
                            return;
                        }
                        if (isNaN(twoLastTimeYa) || parseFloat(twoLastTimeYa) != parseInt(twoLastTimeYa)) {
                            bindObj.showErrorMsg(workHourLastTimeInteger, '#twoLastTimeYa');
                            return;
                        }
                        twoLastTimeYa = parseInt(twoLastTimeYa);
                        if (twoLastTimeYa < 1 || twoLastTimeYa > 60) {
                            bindObj.showErrorMsg(workHourLastTimeRange, '#twoLastTimeYa');
                            return;
                        }
                        toPost.twoLastTime = twoLastTimeYa;
                        var twoThresholdVoltage = $('#twoThresholdVoltage').val();
                        if (twoThresholdVoltage.length == 0) {
                            bindObj.showErrorMsg(workHourThresholdVoltageNull, '#twoThresholdVoltage');
                            return;
                        }
                        if (isNaN(twoThresholdVoltage)) {
                            bindObj.showErrorMsg(workHourThresholdVoltageInteger, '#twoThresholdVoltage');
                            return;
                        }
                        twoThresholdVoltage = parseFloat(twoThresholdVoltage);
                        if (twoThresholdVoltage < 1 || twoThresholdVoltage > 6000) {
                            bindObj.showErrorMsg(workHourThresholdVoltageRange, '#twoThresholdVoltage');
                            return;
                        }
                        toPost.twoThresholdVoltage = twoThresholdVoltage.toFixed(1);
                    } else if (twoDetectionMode == "油耗阈值式") {
                        var twoLastTimeWf = $('#twoLastTimeWf').val();
                        if (twoLastTimeWf.length == 0) {
                            bindObj.showErrorMsg(workHourLastTimeNull, '#twoLastTimeWf');
                            return;
                        }
                        if (isNaN(twoLastTimeWf) || parseFloat(twoLastTimeWf) != parseInt(twoLastTimeWf)) {
                            bindObj.showErrorMsg(workHourLastTimeInteger, '#twoLastTimeWf');
                            return;
                        }
                        twoLastTimeWf = parseInt(twoLastTimeWf);
                        if (twoLastTimeWf < 1 || twoLastTimeWf > 60) {
                            bindObj.showErrorMsg(workHourLastTimeRange, '#twoLastTimeWf');
                            return;
                        }
                        toPost.twoLastTime = twoLastTimeWf;

                        var baudRateThreshold = $('#threshold').val();
                        if (baudRateThreshold.length == 0) {
                            bindObj.showErrorMsg('输入范围1.00-600', '#threshold1');
                            return;
                        }
                        if (isNaN(baudRateThreshold) || parseFloat(baudRateThreshold) != parseFloat(baudRateThreshold).toFixed(2)) {
                            bindObj.showErrorMsg('输入范围1.00-600', '#threshold');
                            return;
                        }
                        baudRateThreshold = parseFloat(baudRateThreshold);
                        if (baudRateThreshold < 1 || baudRateThreshold > 600) {
                            bindObj.showErrorMsg('输入范围1.00-600', '#threshold');
                            return;
                        }
                        toPost.twoThreshold = baudRateThreshold.toFixed(2);

                        var smoothingFactor = $('#smoothingFactor2').val();
                        if (smoothingFactor.length === 0) {
                            bindObj.showErrorMsg(workHourSmoothingFactorNull, '#smoothingFactor2');
                            return;
                        }
                        if (isNaN(smoothingFactor) || parseFloat(smoothingFactor) != parseInt(smoothingFactor)) {
                            bindObj.showErrorMsg(workHourSmoothingFactorInteger, '#smoothingFactor2');
                            return;
                        }
                        smoothingFactor = parseInt(smoothingFactor);
                        if (smoothingFactor < 5 || smoothingFactor > 100) {
                            bindObj.showErrorMsg(workHourSmoothingFactorRange, '#smoothingFactor2');
                            return;
                        }
                        toPost.twoSmoothingFactor = smoothingFactor;

                        /*var twoThresholdWorkFlow = $('#twoThresholdWorkFlow').val();
                        if (twoThresholdWorkFlow.length == 0){
                            bindObj.showErrorMsg(workHourThresholdWorkFlowNull,'#twoThresholdWorkFlow');
                            return;
                        }
                        if (isNaN(twoThresholdWorkFlow) ){
                            bindObj.showErrorMsg(workHourThresholdWorkFlowInteger,'#twoThresholdWorkFlow');
                            return;
                        }
                        twoThresholdWorkFlow = parseFloat(twoThresholdWorkFlow);
                        if (twoThresholdWorkFlow < 1 || twoThresholdWorkFlow > 600){
                            bindObj.showErrorMsg(workHourThresholdWorkFlowRange,'#twoThresholdWorkFlow');
                            return;
                        }
                        toPost.twoThresholdWorkFlow = twoThresholdWorkFlow.toFixed(1);*/
                    } else {
                        var twoLastTimeLiu = $('#twoLastTimeLiu').val();
                        if (twoLastTimeLiu.length == 0) {
                            bindObj.showErrorMsg(workHourLastTimeNull, '#twoLastTimeLiu');
                            return;
                        }
                        if (isNaN(twoLastTimeLiu) || parseFloat(twoLastTimeLiu) != parseInt(twoLastTimeLiu)) {
                            bindObj.showErrorMsg(workHourLastTimeInteger, '#twoLastTimeLiu');
                            return;
                        }
                        twoLastTimeLiu = parseInt(twoLastTimeLiu);
                        if (twoLastTimeLiu < 1 || twoLastTimeLiu > 60) {
                            bindObj.showErrorMsg(workHourLastTimeRange, '#twoLastTimeLiu');
                            return;
                        }
                        toPost.twoLastTime = twoLastTimeLiu;

                        var twoBaudRateCalculateNumber = $('#twoBaudRateCalculateNumber').val();
                        if (twoBaudRateCalculateNumber.length == 0) {
                            bindObj.showErrorMsg(workHourBaudRateCalculateNumberNull, '#twoBaudRateCalculateNumber');
                            return;
                        }
                        if (isNaN(twoBaudRateCalculateNumber)) {
                            bindObj.showErrorMsg(workHourBaudRateCalculateNumberInteger, '#twoBaudRateCalculateNumber');
                            return;
                        }
                        twoBaudRateCalculateNumber = parseFloat(twoBaudRateCalculateNumber);
                        if (twoBaudRateCalculateNumber < 4 || twoBaudRateCalculateNumber > 12) {
                            bindObj.showErrorMsg(workHourBaudRateCalculateNumberRange, '#twoBaudRateCalculateNumber');
                            return;
                        }
                        toPost.twoBaudRateCalculateNumber = twoBaudRateCalculateNumber;

                        var twoSmoothingFactor = $('#twoSmoothingFactor').val();
                        if (twoSmoothingFactor.length === 0) {
                            bindObj.showErrorMsg(workHourSmoothingFactorNull, '#twoSmoothingFactor');
                            return;
                        }
                        if (isNaN(twoSmoothingFactor) || parseFloat(twoSmoothingFactor) != parseInt(twoSmoothingFactor)) {
                            bindObj.showErrorMsg(workHourSmoothingFactorInteger, '#twoSmoothingFactor');
                            return;
                        }
                        twoSmoothingFactor = parseInt(twoSmoothingFactor);
                        if (twoSmoothingFactor < 5 || twoSmoothingFactor > 100) {
                            bindObj.showErrorMsg(workHourSmoothingFactorRange, '#twoSmoothingFactor');
                            return;
                        }
                        toPost.twoSmoothingFactor = twoSmoothingFactor;

                        var twoBaudRateThreshold = $('#twoBaudRateThreshold').val();
                        if (twoBaudRateThreshold.length == 0) {
                            bindObj.showErrorMsg(workHourBaudRateThresholdNull, '#twoBaudRateThreshold');
                            return;
                        }
                        if (isNaN(twoBaudRateThreshold) || parseFloat(twoBaudRateThreshold) != parseFloat(twoBaudRateThreshold).toFixed(1)) {
                            bindObj.showErrorMsg(workHourBaudRateThresholdInteger, '#twoBaudRateThreshold');
                            return;
                        }
                        twoBaudRateThreshold = parseFloat(twoBaudRateThreshold);
                        if (twoBaudRateThreshold < 1 || twoBaudRateThreshold > 600) {
                            bindObj.showErrorMsg(workHourBaudRateThresholdRange, '#twoBaudRateThreshold');
                            return;
                        }
                        toPost.twoBaudRateThreshold = twoBaudRateThreshold.toFixed(1);

                        var twoBaudRateCalculateTimeScope = $('#twoBaudRateCalculateTimeScope').val();
                        if (twoBaudRateCalculateTimeScope.length == 0) {
                            bindObj.showErrorMsg("波动计算时段不能为空", '#twoBaudRateCalculateTimeScope');
                            return;
                        }
                        if (isNaN(twoBaudRateCalculateTimeScope)
                            || parseFloat(twoBaudRateCalculateTimeScope) != parseInt(twoBaudRateCalculateTimeScope)) {
                            bindObj.showErrorMsg("波动计算时段 必须为正整数", '#twoBaudRateCalculateTimeScope');
                            return;
                        }
                        toPost.twoBaudRateCalculateTimeScope = twoBaudRateCalculateTimeScope;

                        var twoSpeedThreshold = $('#twoSpeedThreshold').val();
                        if (twoSpeedThreshold.length == 0) {
                            bindObj.showErrorMsg(workSpeedThresholdNull, '#twoSpeedThreshold');
                            return;
                        }
                        if (isNaN(twoSpeedThreshold) || parseFloat(twoSpeedThreshold) != parseFloat(twoSpeedThreshold).toFixed(1)) {
                            bindObj.showErrorMsg(workSpeedThresholdInteger, '#twoSpeedThreshold');
                            return;
                        }
                        twoSpeedThreshold = parseFloat(twoSpeedThreshold);
                        if (twoSpeedThreshold < 1 || twoSpeedThreshold > 200) {
                            bindObj.showErrorMsg(workSpeedThresholdRange, '#twoSpeedThreshold');
                            return;
                        }
                        toPost.twoSpeedThreshold = twoSpeedThreshold;
                    }
                }

            }

            if (!toPost.sensorId && !toPost.twoSensorId) {
                layer.msg('请选择传感器');
                return;
            }

            let value = '';
            for(var key in toPost){
                value += toPost[key];
            }
            value = value.split("").reduce(function (a, b) { a = ((a << 5) - a) + b.charCodeAt(0); return a & a }, 0);
            toPost.resubmitToken = value;

            $.ajax({
                type: 'POST',
                url: '/clbs/v/workhourmgt/workhoursetting/addWorkHourSetting',
                async: false,
                dataType: 'json',
                data: toPost,
                success: function (data) {
                    var result = data;
                    if (typeof data == 'string') {
                        result = JSON.parse(data);
                    }
                    if (result.success) {
                        $("#commonWin").modal("hide");
                        bindObj.closeWindow();
                        layer.msg("设置成功！", {move: false});
                        myTable.requestData();
                    } else {
                        $("#commonWin").modal("hide");
                        layer.msg(result.msg, {move: false});
                    }
                },
                error: function () {
                    layer.msg(systemError, {move: false});
                }
            });
        },
        closeWindow: function () {
            bindObj.renderInput({}, 1);
            bindObj.renderInput({}, 2);
        },
        showErrorMsg: function (msg, inputId) {
            if ($("#error_label_add").is(":hidden")) {
                $("#error_label_add").text(msg);
                $("#error_label_add").insertAfter($(inputId));
                $("#error_label_add").show();
            } else {
                $("#error_label_add").is(":hidden");
            }
        },
        //错误提示信息隐藏
        hideErrorMsg: function () {
            $("#error_label_add").is(":hidden");
            $("#error_label_add").hide();
        },
    }
    $(function () {
        $('input').inputClear();
        bindObj.init();
        $("#doSubmit").bind("click", bindObj.doSubmit);
        $("#closeWindow,#closeModal").bind("click", bindObj.closeWindow);
    })
})(window, $);