//# sourceURL=loadManagementSettingEdit.js

(function (window, $) {
    // 车辆list
    var vehicleList = JSON.parse($("#vehicleList").attr("value"));
    // 初始化车辆数据
    var dataList = {value: []};
    //初始化油箱参数
    var fuelDataList = {value: []};
    var sensorList = {value: []};

    editObj = {
        init: function () {
            $(".modal-body").addClass("modal-body-overflow");
            $(".modal-body").css({"height": "auto", "max-height": ($(window).height() - 194) + "px"});


            editObj.initSensor();
            editObj.initRefer();
            editObj.initReadonly();
            editObj.initTwoDefault();

            var type = $('#type').val();
            if (type == 1) {
                $("#egine1").removeClass("active");
                $("#egine1Title").removeClass("active");
                $("#egine2").addClass("active");
                $("#egine2Title").addClass("active");
            }
        },

        /**
         * 如果载重2没有数据  用来初始化载重2页面的默认值
         */
        initTwoDefault: function(){
            if($("#loadDefalt2").val() == null || $("#loadDefalt2").val() == ''){
                $("#noLoadThreshold2").val(10);
                $("#lightLoadThreshold2").val(10);
                $("#fullLoadThreshold2").val(10);
                $("#overLoadThreshold2").val(10);
            }
        },


        /**
         * 初始化只读值
         */
        initReadonly: function(){
            var sensorId = $('#formSensorId1').val();
            if (sensorId && sensorId.length > 0) {
                var sensorInfo = editObj.getSensorData(sensorId);
                $('#sensorId').val(sensorInfo.sensorNumber).data('id', sensorInfo.id).attr('data-id', sensorInfo.id);
                editObj.renderReadonly(sensorInfo, 1);

            }
            var twoSensorId = $('#formSensorId2').val();
            if (twoSensorId && twoSensorId.length > 0){
                var sensorInfo2 = editObj.getSensorData(twoSensorId);
                $('#sensorId2').val(sensorInfo2.sensorNumber).data('id', sensorInfo2.id).attr('data-id', sensorInfo2.id);
                editObj.renderReadonly(sensorInfo2, 2);
            }
        },
        /**
         * 初始化参考对象
         */
        initRefer: function () {
            if (vehicleList !== null && vehicleList.length > 0) {
                var vehicleId = $("#vehicleId").val();
                for (var i = 0; i < vehicleList.length; i++) {
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
                    var url = "/clbs/v/loadmgt/loadvehiclesetting/findLoadBingInfo";
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
                                if (sensorInfo){
                                    if (sensorInfo.sensorId && sensorInfo.sensorId.length > 0) {
                                        $('#sensorId').val(sensorInfo.sensorNumber).data('id', sensorInfo.sensorId);
                                        $('#formSensorId1').val(sensorInfo.sensorId);
                                        editObj.renderInput(sensorInfo, 1);

                                    }
                                    if (sensorInfo.twoSensorId && sensorInfo.twoSensorId.length > 0){
                                        $('#sensorId2').val(sensorInfo.twoSensorNumber).data('id', sensorInfo.twoSensorId);
                                        $('#formSensorId2').val(sensorInfo.twoSensorId);
                                        var sensorTwo = {
                                            id: sensorInfo.twoSensorId,
                                            compensate: sensorInfo.twoCompensate,
                                            oddEvenCheck: sensorInfo.twoOddEvenCheck,
                                            baudRate: sensorInfo.twoBaudRate,
                                            filterFactor: sensorInfo.twoFilterFactor,
                                            twoPersonLoadParam: sensorInfo.twoPersonLoadParam

                                        }
                                            editObj.renderInput(sensorTwo, 2);
                                    }
                                }else{
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
            }, 100);
        },
        /**
         * 初始化传感器
         */
        initSensor: function () {
            var _initSensor = function (sensorList, id){
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
                    if (id != null && id !== undefined && id !== ""){
                        sensor = editObj.getSensorData(id);
                        var tabIndex = editObj.getCurrentActiveTabIndex();
                        $('#formSensorId' + tabIndex).val(id);
                    }
                    editObj.renderInput(sensor);
                }).on('onUnsetSelectValue', function () {
                    editObj.renderInput({});
                    var tabIndex = editObj.getCurrentActiveTabIndex();
                    $('#formSensorId' + tabIndex).val('');
                }).on('onClearSelectValue', function () {
                    editObj.renderInput({});
                    var tabIndex = editObj.getCurrentActiveTabIndex();
                    $('#formSensorId' + tabIndex).val('');
                });
            }
            $.ajax({
                type: 'POST',
                url: '/clbs/v/loadmgt/loadvehiclesetting/findsensor',
                async: false,
                dataType: 'json',
                data: {sensorType: 6},
                success: function (data) {
                    if (data.success) {
                        var sensors = data.obj;
                        sensorList.value = sensors;
                        _initSensor({value:sensors}, '#sensorId');
                        _initSensor({value:sensors}, '#sensorId2');
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
        getSensorData: function(id){
            for (var i=0;i<sensorList.value.length;i++){
                var item = sensorList.value[i];
                if (item.id === id){
                    return item;
                }
            }
            return null;
        },
        /**
         * 渲染只读值
         * @param sensor
         * @param tabIndexParam
         */
        renderReadonly: function(sensor, tabIndexParam){
            var tabIndex = tabIndexParam || editObj.getCurrentActiveTabIndex();
            var container = $('#egine' + tabIndex);
            var compensate = {"1": "使能", "2": "禁用"}[sensor.compensate];
            var oddEvenCheck = {"1": "奇校验", "2": "偶校验", "3": "无校验"}[sensor.oddEvenCheck];
            var baudRate = {"1":"2400","2":"4800","3":"9600","4":"19200","5":"38400","6":"57600","7":"115200"}[sensor.baudRate];
            var filterFactor = {"1": "实时", "2": "平滑", "3": "平稳"}[sensor.filterFactor];
            if (tabIndex == 1){
                container.find('[id="compensate"]').val(compensate);
                container.find('[id="oddEvenCheck"]').val(oddEvenCheck);
                container.find('[id="baudRate"]').val(baudRate);
                container.find('[id="filterFactor"]').val(filterFactor);


            } else{
                container.find('[id="compensate2"]').val(compensate);
                container.find('[id="oddEvenCheck2"]').val(oddEvenCheck);
                container.find('[id="baudRate2"]').val(baudRate);
                container.find('[id="filterFactor2"]').val(filterFactor);

            }
        },
        /**
         * 渲染输入框
         * @param sensor
         * @param tabIndexParam
         */
        renderInput: function(sensor, tabIndexParam){
            var tabIndex = tabIndexParam || editObj.getCurrentActiveTabIndex();
            var container = $('#egine' + tabIndex);
            var compensate = {"1": "使能", "2": "禁用"}[sensor.compensate];
            var oddEvenCheck = {"1": "奇校验", "2": "偶校验", "3": "无校验"}[sensor.oddEvenCheck];
            var baudRate = {"1":"2400","2":"4800","3":"9600","4":"19200","5":"38400","6":"57600","7":"115200"}[sensor.baudRate];
            var filterFactor = {"1": "实时", "2": "平滑", "3": "平稳"}[sensor.filterFactor];


            if (tabIndex == 1){
                container.find('[id="compensate"]').val(compensate);
                container.find('[id="oddEvenCheck"]').val(oddEvenCheck);
                container.find('[id="baudRate"]').val(baudRate);
                container.find('[id="filterFactor"]').val(filterFactor);

                // 个性参数
                if(sensor.personLoadParam != null){
                    container.find('[id="loadMeterWay"]').val(sensor.personLoadParam.loadMeterWay);
                    container.find('[id="loadMeterUnit"]').val(sensor.personLoadParam.loadMeterUnit);
                    container.find('[id="noLoadValue"]').val(sensor.personLoadParam.noLoadValue);
                    container.find('[id="noLoadThreshold"]').val(sensor.personLoadParam.noLoadThreshold);
                    container.find('[id="lightLoadValue"]').val(sensor.personLoadParam.lightLoadValue);
                    container.find('[id="lightLoadThreshold"]').val(sensor.personLoadParam.lightLoadThreshold);
                    container.find('[id="fullLoadValue"]').val(sensor.personLoadParam.fullLoadValue);
                    container.find('[id="fullLoadThreshold"]').val(sensor.personLoadParam.fullLoadThreshold);
                    container.find('[id="overLoadValue"]').val(sensor.personLoadParam.overLoadValue);
                    container.find('[id="overLoadThreshold"]').val(sensor.personLoadParam.overLoadThreshold);
                    container.find('[id="listValue"]').val(sensor.adParamJson);
                }

            } else{
                container.find('[id="compensate2"]').val(compensate);
                container.find('[id="oddEvenCheck2"]').val(oddEvenCheck);
                container.find('[id="baudRate2"]').val(baudRate);
                container.find('[id="filterFactor2"]').val(filterFactor);

                // 个性参数
                if(sensor.twoPersonLoadParam != null) {
                    container.find('[id="loadMeterWay2"]').val(sensor.twoPersonLoadParam.loadMeterWay);
                    container.find('[id="loadMeterUnit2"]').val(sensor.twoPersonLoadParam.loadMeterUnit);
                    container.find('[id="noLoadValue2"]').val(sensor.twoPersonLoadParam.noLoadValue);
                    container.find('[id="noLoadThreshold2"]').val(sensor.twoPersonLoadParam.noLoadThreshold);
                    container.find('[id="lightLoadValue2"]').val(sensor.twoPersonLoadParam.lightLoadValue);
                    container.find('[id="lightLoadThreshold2"]').val(sensor.twoPersonLoadParam.lightLoadThreshold);
                    container.find('[id="fullLoadValue2"]').val(sensor.twoPersonLoadParam.fullLoadValue);
                    container.find('[id="fullLoadThreshold2"]').val(sensor.twoPersonLoadParam.fullLoadThreshold);
                    container.find('[id="overLoadValue2"]').val(sensor.twoPersonLoadParam.overLoadValue);
                    container.find('[id="overLoadThreshold2"]').val(sensor.twoPersonLoadParam.overLoadThreshold);
                    container.find('[id="listValue2"]').val(sensor.twoAdParamJson);
                }
            }
        },
        /**
         * 获取当前tab的索引
         * @returns {number} 1：发动机1；2：发动机2
         */
        getCurrentActiveTabIndex: function(){
            if ($('#egine1Title').hasClass('active')) return 1;
            return 2;
        },


        // 提交
        doSubmit: function () {
            var toPost = {
                vehicleId : $('#vehicleId').val(),
                plateNumber: $('#vehicleBrand').val(),
                avoidRepeatSubmitToken:$('#avoidRepeatSubmitToken').val(),
                id:$('#id').val(),
                monitorType:$('#monitorType').val(),
                twoId:$('#twoId').val()
            };
            editObj.hideErrorMsg();
            // 校验发动机1
            var sensorId = $('#formSensorId1').val();
            if (sensorId.length>0){
                if ($('#sensorId').val().length==0){
                    toPost.sensorId = null;
                    sensorId = null;
                }else{
                    toPost.sensorSequence = 0;
                    toPost.sensorId = sensorId;
                    toPost.sensorNumber = $('#sensorId').val();

                    //个性参数
                    var personLoadJson = new Object;
                    var loadMeterWay = $('#loadMeterWay').val();
                    if(loadMeterWay != "0" && loadMeterWay != "1" && loadMeterWay != "2"){
                        editObj.showErrorMsg("请选择载重测量方法！", '#loadMeterWay');
                        return;
                    }
                    var loadMeterUnit = $('#loadMeterUnit').val();
                    var loadMeter;
                    var rangERROStr;
                    switch (loadMeterUnit){
                        case '0':
                        case 0 :
                            loadMeter = 0.1;
                            rangERROStr = "重量单位0.1kg的阈值输入范围为0-999.9";
                            break;
                        case '1':
                        case 1:
                            loadMeter = 1;
                            rangERROStr = "重量单位1kg的阈值输入范围为0-9999.9";
                            break;
                        case '2':
                        case 2:
                            loadMeter = 10;
                            rangERROStr = "重量单位10kg的阈值输入范围为0-99999.9";
                            break;
                        case '3':
                        case 3:
                            loadMeter = 100;
                            rangERROStr = "重量单位100kg的阈值输入范围为0-999999.9";
                            break;
                        default:
                            editObj.showErrorMsg("请选择传感器重量单位！", '#loadMeterUnit');
                            return;

                    }
                    personLoadJson.loadMeterWay = loadMeterWay;
                    personLoadJson.loadMeterUnit = loadMeterUnit;
                    var noLoadValue = $('#noLoadValue').val();
                    var noLoadThreshold = $('#noLoadThreshold').val();
                    var lightLoadValue = $('#lightLoadValue').val();
                    var lightLoadThreshold = $('#lightLoadThreshold').val();
                    var fullLoadValue = $('#fullLoadValue').val();
                    var fullLoadThreshold = $('#fullLoadThreshold').val();
                    var overLoadValue = $('#overLoadValue').val();
                    var overLoadThreshold = $('#overLoadThreshold').val();
                    if(editObj.validates()){
                        noLoadValue = parseFloat(noLoadValue);
                        lightLoadValue = parseFloat(lightLoadValue);
                        fullLoadValue = parseFloat(fullLoadValue);
                        overLoadValue = parseFloat(overLoadValue);

                        if(loadMeter == 0.1){
                            if(noLoadValue >= 1000){
                                editObj.showErrorMsg(rangERROStr, '#noLoadValue');
                                return;
                            }
                            if(lightLoadValue >= 1000){
                                editObj.showErrorMsg(rangERROStr, '#lightLoadValue');
                                return;
                            }
                            if(fullLoadValue >= 1000){
                                editObj.showErrorMsg(rangERROStr, '#fullLoadValue');
                                return;
                            }
                            if(overLoadValue >= 1000){
                                editObj.showErrorMsg(rangERROStr, '#overLoadValue');
                                return;
                            }
                        }else if(loadMeter == 1){
                            if(noLoadValue >= 10000){
                                editObj.showErrorMsg(rangERROStr, '#noLoadValue');
                                return;
                            }
                            if(lightLoadValue >= 10000){
                                editObj.showErrorMsg(rangERROStr, '#lightLoadValue');
                                return;
                            }
                            if(fullLoadValue >= 10000){
                                editObj.showErrorMsg(rangERROStr, '#fullLoadValue');
                                return;
                            }
                            if(overLoadValue >= 10000){
                                editObj.showErrorMsg(rangERROStr, '#overLoadValue');
                                return;
                            }
                        }else if(loadMeter == 10){
                            if(noLoadValue >= 100000){
                                editObj.showErrorMsg(rangERROStr, '#noLoadValue');
                                return;
                            }
                            if(lightLoadValue >= 100000){
                                editObj.showErrorMsg(rangERROStr, '#lightLoadValue');
                                return;
                            }
                            if(fullLoadValue >= 100000){
                                editObj.showErrorMsg(rangERROStr, '#fullLoadValue');
                                return;
                            }
                            if(overLoadValue >= 100000){
                                editObj.showErrorMsg(rangERROStr, '#overLoadValue');
                                return;
                            }
                        }else if(loadMeter == 100){
                            if(noLoadValue >= 1000000){
                                editObj.showErrorMsg(rangERROStr, '#noLoadValue');
                                return;
                            }
                            if(lightLoadValue >= 1000000){
                                editObj.showErrorMsg(rangERROStr, '#lightLoadValue');
                                return;
                            }
                            if(fullLoadValue >= 1000000){
                                editObj.showErrorMsg(rangERROStr, '#fullLoadValue');
                                return;
                            }
                            if(overLoadValue >= 1000000){
                                editObj.showErrorMsg(rangERROStr, '#overLoadValue');
                                return;
                            }
                        }

                        if(noLoadValue < loadMeter){
                            editObj.showErrorMsg("空载阈值不能小于传感器重量单位，请确认！", '#noLoadValue');
                            return;
                        }
                        if(lightLoadValue - noLoadValue < loadMeter){
                            editObj.showErrorMsg("轻载阈值必须比空载阈值大1个传感器重量单位，请确认！", '#lightLoadValue');
                            return;
                        }
                        if(fullLoadValue - lightLoadValue < loadMeter){
                            editObj.showErrorMsg("满载阈值必须比轻载阈值大1个传感器重量单位，请确认", '#fullLoadValue');
                            return;
                        }
                        if(overLoadValue - fullLoadValue < loadMeter){
                            editObj.showErrorMsg("超载阈值必须比满载阈值大1个传感器重量单位，请确认", '#overLoadValue');
                            return;
                        }
                        var listValue = $("#listValue").val();
                        personLoadJson.noLoadValue = noLoadValue;
                        personLoadJson.overLoadThreshold = overLoadThreshold;
                        personLoadJson.lightLoadValue = lightLoadValue;
                        personLoadJson.lightLoadThreshold = lightLoadThreshold;
                        personLoadJson.fullLoadValue = fullLoadValue;
                        personLoadJson.fullLoadThreshold = fullLoadThreshold;
                        personLoadJson.overLoadValue = overLoadValue;
                        personLoadJson.noLoadThreshold = noLoadThreshold;
                    }else {
                        return;
                    }
                    toPost.personLoadJson = JSON.stringify(personLoadJson);
                    toPost.adParamJson = listValue;
                }
            }


            // 校验发动机2
            var twoSensorId = $('#formSensorId2').val();
            if (twoSensorId.length>0){
                if (sensorId.length === 0){
                    layer.msg('请先配置载重1');
                    return;
                }
                if ($('#sensorId2').val().length==0){
                    toPost.twoSensorId = null;
                }else{
                    toPost.twoSensorSequence = 1;
                    toPost.twoSensorId = twoSensorId;
                    toPost.twoSensorNumber = $('#sensorId2').val();
                    //个性参数
                    var twoPersonLoadJson = new Object;
                    var loadMeterWay = $('#loadMeterWay2').val();
                    if(loadMeterWay != "0" && loadMeterWay != "1" && loadMeterWay != "2"){
                        editObj.showErrorMsg("请选择载重测量方法！", '#loadMeterWay2');
                        return;
                    }
                    var loadMeterUnit = $('#loadMeterUnit2').val();
                    var loadMeter;
                    var rangERROStr;
                    switch (loadMeterUnit){
                        case '0':
                        case 0 :
                            loadMeter = 0.1;
                            rangERROStr = "重量单位0.1kg的阈值输入范围为0-999.9";
                            break;
                        case '1':
                        case 1:
                            loadMeter = 1;
                            rangERROStr = "重量单位1kg的阈值输入范围为0-9999.9";
                            break;
                        case '2':
                        case 2:
                            loadMeter = 10;
                            rangERROStr = "重量单位10kg的阈值输入范围为0-99999.9";
                            break;
                        case '3':
                        case 3:
                            loadMeter = 100;
                            rangERROStr = "重量单位100kg的阈值输入范围为0-999999.9";
                            break;
                        default:
                            editObj.showErrorMsg("请选择传感器重量单位！", '#loadMeterUnit2');
                            return;

                    }
                    twoPersonLoadJson.loadMeterWay = loadMeterWay;
                    twoPersonLoadJson.loadMeterUnit = loadMeterUnit;
                    var noLoadValue = $('#noLoadValue2').val();
                    var noLoadThreshold = $('#noLoadThreshold2').val();
                    var lightLoadValue = $('#lightLoadValue2').val();
                    var lightLoadThreshold = $('#lightLoadThreshold2').val();
                    var fullLoadValue = $('#fullLoadValue2').val();
                    var fullLoadThreshold = $('#fullLoadThreshold2').val();
                    var overLoadValue = $('#overLoadValue2').val();
                    var overLoadThreshold = $('#overLoadThreshold2').val();

                    if(editObj.validates2()){
                        noLoadValue = parseFloat(noLoadValue);
                        lightLoadValue = parseFloat(lightLoadValue);
                        fullLoadValue = parseFloat(fullLoadValue);
                        overLoadValue = parseFloat(overLoadValue);

                        if(loadMeter == 0.1){
                            if(noLoadValue >= 1000){
                                editObj.showErrorMsg(rangERROStr, '#noLoadValue2');
                                return;
                            }
                            if(lightLoadValue >= 1000){
                                editObj.showErrorMsg(rangERROStr, '#lightLoadValue2');
                                return;
                            }
                            if(fullLoadValue >= 1000){
                                editObj.showErrorMsg(rangERROStr, '#fullLoadValue2');
                                return;
                            }
                            if(overLoadValue >= 1000){
                                editObj.showErrorMsg(rangERROStr, '#overLoadValue2');
                                return;
                            }
                        }else if(loadMeter == 1){
                            if(noLoadValue >= 10000){
                                editObj.showErrorMsg(rangERROStr, '#noLoadValue2');
                                return;
                            }
                            if(lightLoadValue >= 10000){
                                editObj.showErrorMsg(rangERROStr, '#lightLoadValue2');
                                return;
                            }
                            if(fullLoadValue >= 10000){
                                editObj.showErrorMsg(rangERROStr, '#fullLoadValue2');
                                return;
                            }
                            if(overLoadValue >= 10000){
                                editObj.showErrorMsg(rangERROStr, '#overLoadValue2');
                                return;
                            }
                        }else if(loadMeter == 10){
                            if(noLoadValue >= 100000){
                                editObj.showErrorMsg(rangERROStr, '#noLoadValue2');
                                return;
                            }
                            if(lightLoadValue >= 100000){
                                editObj.showErrorMsg(rangERROStr, '#lightLoadValue2');
                                return;
                            }
                            if(fullLoadValue >= 100000){
                                editObj.showErrorMsg(rangERROStr, '#fullLoadValue2');
                                return;
                            }
                            if(overLoadValue >= 100000){
                                editObj.showErrorMsg(rangERROStr, '#overLoadValue2');
                                return;
                            }
                        }else if(loadMeter == 100){
                            if(noLoadValue >= 1000000){
                                editObj.showErrorMsg(rangERROStr, '#noLoadValue2');
                                return;
                            }
                            if(lightLoadValue >= 1000000){
                                editObj.showErrorMsg(rangERROStr, '#lightLoadValue2');
                                return;
                            }
                            if(fullLoadValue >= 1000000){
                                editObj.showErrorMsg(rangERROStr, '#fullLoadValue2');
                                return;
                            }
                            if(overLoadValue >= 1000000){
                                editObj.showErrorMsg(rangERROStr, '#overLoadValue2');
                                return;
                            }
                        }

                        if(noLoadValue < loadMeter){
                            editObj.showErrorMsg("空载阈值不能小于传感器重量单位，请确认！", '#noLoadValue2');
                            return;
                        }
                        if(lightLoadValue - noLoadValue < loadMeter){
                            editObj.showErrorMsg("轻载阈值必须比空载阈值大1个传感器重量单位，请确认！", '#lightLoadValue2');
                            return;
                        }
                        if(fullLoadValue - lightLoadValue < loadMeter){
                            editObj.showErrorMsg("满载阈值必须比轻载阈值大1个传感器重量单位，请确认", '#fullLoadValue2');
                            return;
                        }
                        if(overLoadValue - fullLoadValue < loadMeter){
                            editObj.showErrorMsg("超载阈值必须比满载阈值大1个传感器重量单位，请确认", '#overLoadValue2');
                            return;
                        }
                        var listValue = $("#listValue2").val();
                        twoPersonLoadJson.noLoadValue = noLoadValue;
                        twoPersonLoadJson.overLoadThreshold = overLoadThreshold;
                        twoPersonLoadJson.lightLoadValue = lightLoadValue;
                        twoPersonLoadJson.lightLoadThreshold = lightLoadThreshold;
                        twoPersonLoadJson.fullLoadValue = fullLoadValue;
                        twoPersonLoadJson.fullLoadThreshold = fullLoadThreshold;
                        twoPersonLoadJson.overLoadValue = overLoadValue;
                        twoPersonLoadJson.noLoadThreshold = noLoadThreshold;
                    }else {
                        return;
                    }
                    toPost.twoPersonLoadJson = JSON.stringify(twoPersonLoadJson);
                    toPost.twoAdParamJson = listValue;
                }

            }
            if (!toPost.sensorId && !toPost.twoSensorId){
                layer.msg('请选择传感器');
                return;
            }


            var value = '';
            for (var key in toPost) {
                value += toPost[key]
            }
            var resubmitToken = value.split("").reduce(function (a, b) { a = ((a << 5) - a) + b.charCodeAt(0); return a & a }, 0);

            toPost.resubmitToken = resubmitToken;



            $.ajax({
                type: 'POST',
                url: '/clbs/v/loadmgt/loadvehiclesetting/updateLoadSetting',
                async: false,
                dataType: 'json',
                data: toPost,
                success: function (data) {
                    var result = data;
                    if (typeof data == 'string'){
                        result = JSON.parse(data);
                    }
                    if (result.success) {
                        $("#commonWin").modal("hide");
                        editObj.closeWindow();
                        layer.msg("修改成功！", {move: false});
                        myTable.refresh();
                    }else {
                        layer.msg("修改失败！", {move: false});
                    }
                },
                error: function () {
                    layer.msg(systemError, {move: false});
                }
            });
        },

        validates: function () {
            return $("#bindForm").validate({
                ignore: '',
                rules: {
                    noLoadValue: {
                        required: true,
                        decimalOneMore: true,
                    },
                    noLoadThreshold: {
                        required: true,
                        isInteger: true,
                        range: [1, 100]
                    },
                    lightLoadValue: {
                        required: true,
                        decimalOneMore: true,
                    },
                    lightLoadThreshold: {
                        required: true,
                        isInteger: true,
                        range: [1, 100]
                    },
                    fullLoadValue: {
                        required: true,
                        decimalOneMore: true,
                    },
                    fullLoadThreshold: {
                        required: true,
                        isInteger: true,
                        range: [1, 100]
                    },
                    overLoadValue: {
                        required: true,
                        decimalOneMore:true,
                    },
                    overLoadThreshold: {
                        required: true,
                        isInteger: true,
                        range: [1, 100]
                    }
                },
                messages: {
                    noLoadValue: {
                        required: loadManagementNoloadvalueNull,
                        decimalOneMore: loadManagementDecimalOne,
                    },
                    noLoadThreshold: {
                        required: loadManagementNoloadthresholdNull,
                        isInteger: publicNumberInt,
                        range: vehicleInsuranceDiscount
                    },
                    lightLoadValue: {
                        required:loadManagementLightloadvalueNull,
                        decimalOneMore:loadManagementDecimalOne,
                    },
                    lightLoadThreshold: {
                        required: loadManagementLightloadthresholdNull,
                        isInteger: publicNumberInt,
                        range: vehicleInsuranceDiscount
                    },
                    fullLoadValue: {
                        required: loadManagementFullloadvalueNull,
                        decimalOneMore: loadManagementDecimalOne,
                    },
                    fullLoadThreshold: {
                        required: loadManagementFullloadthresholdNull,
                        isInteger: publicNumberInt,
                        range: vehicleInsuranceDiscount
                    },
                    overLoadValue: {
                        required: loadManagementOverloadvalueNull,
                        decimalOneMore: loadManagementDecimalOne,
                    },
                    overLoadThreshold: {
                        required: loadManagementOverloadthresholdNull,
                        isInteger: publicNumberInt,
                        range: vehicleInsuranceDiscount
                    }
                }
            }).form();
        },

        validates2: function () {
            return $("#bindForm2").validate({
                ignore: '',
                rules: {
                    noLoadValue2: {
                        required: true,
                        decimalOneMore: true,
                    },
                    noLoadThreshold2: {
                        required: true,
                        isInteger: true,
                        range: [1, 100]
                    },
                    lightLoadValue2: {
                        required: true,
                        decimalOneMore: true,
                    },
                    lightLoadThreshold2: {
                        required: true,
                        isInteger: true,
                        range: [1, 100]
                    },
                    fullLoadValue2: {
                        required: true,
                        decimalOneMore: true,
                    },
                    fullLoadThreshold2: {
                        required: true,
                        isInteger: true,
                        range: [1, 100]
                    },
                    overLoadValue2: {
                        required: true,
                        decimalOneMore:true,
                    },
                    overLoadThreshold2: {
                        required: true,
                        isInteger: true,
                        range: [1, 100]
                    }
                },
                messages: {
                    noLoadValue2: {
                        required: loadManagementNoloadvalueNull,
                        decimalOneMore: loadManagementDecimalOne,
                    },
                    noLoadThreshold2: {
                        required: loadManagementNoloadthresholdNull,
                        isInteger: publicNumberInt,
                        range: vehicleInsuranceDiscount
                    },
                    lightLoadValue2: {
                        required:loadManagementLightloadvalueNull,
                        decimalOneMore:loadManagementDecimalOne,
                    },
                    lightLoadThreshold2: {
                        required: loadManagementLightloadthresholdNull,
                        isInteger: publicNumberInt,
                        range: vehicleInsuranceDiscount
                    },
                    fullLoadValue2: {
                        required: loadManagementFullloadvalueNull,
                        decimalOneMore: loadManagementDecimalOne,
                    },
                    fullLoadThreshold2: {
                        required: loadManagementFullloadthresholdNull,
                        isInteger: publicNumberInt,
                        range: vehicleInsuranceDiscount
                    },
                    overLoadValue2: {
                        required: loadManagementOverloadvalueNull,
                        decimalOneMore: loadManagementDecimalOne,
                    },
                    overLoadThreshold2: {
                        required: loadManagementOverloadthresholdNull,
                        isInteger: publicNumberInt,
                        range: vehicleInsuranceDiscount
                    }
                }
            }).form();
        },

        // 切换主油箱和副油箱的时候，给curBox赋值
        setValue: function (value) {
            $("#curBox").val(value);
        },
        
        closeWindow: function () {
            editObj.renderInput({}, 1);
            editObj.renderInput({}, 2);
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
        showTankOrSensorInfoFn: function () {
            var clickId = $(this).context.id;
            if (!($("#" + clickId + "-content").is(":hidden"))) {
                $("#" + clickId + "-content").slideUp();
                $("#" + clickId).children("font").text("显示更多");
                $("#" + clickId).children("span").removeAttr("class").addClass("fa fa-chevron-down");
            } else {
                $("#" + clickId + "-content").slideDown();
                $("#" + clickId).children("font").text("隐藏参数");
                $("#" + clickId).children("span").removeAttr("class").addClass("fa fa-chevron-up");
            }
        },
    }
    $(function () {
        $('input').inputClear();
        editObj.init();
        $("#doSubmit").bind("click", editObj.doSubmit);
        $("#closeWindow,#closeModal").bind("click", editObj.closeWindow);
        $("#tankBasisInfo2,#tankBasisInfo").bind("click", editObj.showTankOrSensorInfoFn);
    })
})(window, $);