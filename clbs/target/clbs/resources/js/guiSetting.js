//# sourceURL=guiSetting.js
// ps: 设置、修改、批量设置共用本页面,settingParam不为空时表示是修改界面
// ps: 页面中的参数id基本上都是以参数名+'_'+报警id命名(便于数据组装与赋值)
(function (window, $) {
    var platformParamSetting = $("#platformParamSetting").val();
    var settingPlat = JSON.parse(platformParamSetting ? platformParamSetting : '[]');
    var riskSettingList = $('#riskSettingList').val();
    var settingParam = JSON.parse(riskSettingList ? riskSettingList : '[]');
    var addSendUrl = '/clbs/adas/standard/param/batch/config';
    var editSendUrl = '/clbs/adas/standard/param/setting.gsp';
    var disabledTabObj = {};// 存储禁用本页签下发按钮的页签
    var curTabSend = $('#curTabSend');// 本页签下发按钮
    //胎压单位换算
    /*var pressureUnits = {
        "psikpa": 6.894757,
        "psibar": 0.068947,
        "psikg/cm²": 0.070307,
        "kpabar": 0.01,
        "kpakg/cm²": 0.0101972,
        "barkg/cm²": 1.0197162,
    };
    var oldPressUnit = 'psi';*///胎压原单位

    var guiSetting = {
        init: function () {
            $(".modal-body").addClass("modal-body-overflow");
            $(".modal-body").css({"height": "auto", "max-height": ($(window).height() - 194) + "px"});

            guiSetting.referenceObjRender();// 参考对象渲染
            guiSetting.referenceTireModel();//轮胎型号渲染
            // 修改界面,已设置的参数渲染
            if (settingParam.length > 0) {
                guiSetting.paramValueRender(settingParam);
            }
            if(settingPlat.length > 0) {
                guiSetting.platformValueRender(settingPlat);
            }

            // 表单验证方法
            guiSetting.forwardValidates();
            guiSetting.driverBehaviorValidates();
            guiSetting.intenseDrivingValidates();
            guiSetting.blindSpotValidates();
            // guiSetting.tirePressureValidates();
            guiSetting.platformParamValidates();
        },
        // 初始化参考对象下拉框数据
        referenceObjRender: function () {
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
                        obj.id = referVehicleList[i].vehicle_id;
                        obj.name = referVehicleList[i].brand;
                        dataList.value.push(obj);
                    }
                }
            }
            $("#riskReference").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                idField: "id",
                keyField: "name",
                effectiveFields: ["name"],
                searchFields: ["id"],
                data: dataList
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {// 选择参考对象
                var vehicleId = keyword.id;
                $.ajax({
                    type: 'GET',
                    url: '/clbs/adas/standard/param/get_' + vehicleId + '.gsp',
                    async: false,
                    dataType: 'json',
                    success: function (data) {
                        if (data.success) {
                            $("#riskReference").val(keyword.key);
                            var result = JSON.parse(data.msg);
                            guiSetting.platformValueRender(result.platformParam);
                            guiSetting.paramValueRender(result.alarmParam);
                        } else {
                            layer.msg("获取参考对象数据失败");
                        }
                    },
                    error: function () {
                        layer.msg(systemError, {move: false});
                    }
                });
            }).on('onUnsetSelectValue', function () {
            });
        },
        //初始化轮胎型号
        referenceTireModel: function(){
            var tyreNumberList = JSON.parse($('#tireModel').val());

            // 初始化车辆数据
            var dataList = {value: []};
            if (tyreNumberList != null && tyreNumberList.length > 0) {
                for(var i=0;i<tyreNumberList.length;i++) {
                    var item = tyreNumberList[i];
                    var obj = {};

                    obj.id = item.tireModelId;
                    obj.name = item.name;
                    dataList.value.push(obj);
                }
            }
            $("#tyreNumber").bsSuggest({
                indexId: 1, //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0,
                idField: "id",
                keyField: "name",
                effectiveFields: ["name"],
                searchFields: ["id"],
                data: dataList
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {// 选择参考对象
                var tireModelId = keyword.id;
                $('#tyreNumber_146608').val(tireModelId);
            }).on('onUnsetSelectValue', function () {
            });
        },
        /**
         * 数据渲染
         * @param dataList
         * (dataList:修改界面或者切换参考对象时传递过来的数据)
         * */
        paramValueRender: function (dataList) {
            for (var i = 0, len = dataList.length; i < len; i++) {
                var item = dataList[i];
                var adasAlarmParamSettings = item.adasAlarmParamSettings;// 报警参数
                // console.log('报警参数', adasAlarmParamSettings);
                var commonParamSetting = item.commonParamSetting;// 通用参数
                var paramType = commonParamSetting.paramType;
                guiSetting.setParamValue(commonParamSetting, paramType);

                for (var j = 0, adasLen = adasAlarmParamSettings.length; j < adasLen; j++) {
                    var data = adasAlarmParamSettings[j];
                    var alarmId = data.riskFunctionId;
                    guiSetting.setParamValue(data, alarmId);
                }
            }
        },
        setParamValue: function (data, id) {
            for (key in data) {
                var curVal = (data[key] == '-1' && key != 'tyreNumber') ? '' : data[key];

                if (key.indexOf('timeDistanceThreshold') != -1 && curVal != '') {// 时距阈值需要除以10
                    curVal = parseFloat((curVal / 10).toFixed(1));
                }
                if (key.indexOf('Status') != -1 || key.indexOf('larmEnable') != -1 || key.indexOf('roadSign') != -1) {
                    var curTarget = $('.' + key + '_' + id + '[value=' + curVal + ']');
                    curTarget.prop('checked', true);
                    if (key.indexOf('Status') != -1) {// 触发状态切换
                        guiSetting.touchStatusChange(curTarget);
                    }
                    if (key.indexOf('AlarmEnable') != -1) {// 一级二级报警切换
                        guiSetting.oneTwoLevelAlarmChange(curTarget);
                    }
                    if (key.indexOf('roadSign') != -1) {// 道路标识识别切换
                        guiSetting.roadSignRecognitionChange(curTarget);
                    }
                    if (key.indexOf('alarmEnable') != -1) {// 激烈驾驶下的报警开关切换
                        guiSetting.alarmEnableChange(curTarget);
                    }
                }
                if(key == 'tyreNumberName'){
                    $('#tyreNumber').val(curVal);
                }

                var targetId = $('#' + key + '_' + id);
                targetId.val(curVal);
            }
        },
        //获取轮胎名称
        /*tyreNumberLists: function(curVal){
            var tyreNumberList = JSON.parse($('#tireModel').val());
            var name = '';
            for(var i=0;i<tyreNumberList.length;i++){
                var item = tyreNumberList[i];
                if(item.tireModelId == curVal){
                    name = item.name;
                    break;
                }
            }
            return name;
        },*/
        platformValueRender: function (dataList) {
            for(var i = 0; i < dataList.length; i++){
                var data = dataList[i];
                var alarmId = data.riskFunctionId;
                guiSetting.newSetParamValue(data, alarmId);
            }
        },
        newSetParamValue: function (data, id) {
            for (key in data) {
                var curVal = (data[key] == '-1' ? '' : data[key]);
                eventId = curVal
                var newId = key + '_' + id;
                var targetId = $('#param-media-content input[name="' +newId + '"]');
                if(key.indexOf('alarmSwitch') != -1){
                    if(data[key] == 1){
                        var blockChange = '.'+newId;
                        $(''+blockChange+'').parent().parent().find('.selectbutton').css("left","9px");
                    }else{
                        $(''+blockChange+'').parent().parent().find('.selectbutton').css("left","55px");
                    }
                }
                if(key.indexOf('processingIntervalOne') != -1 && curVal != ''){
                    curVal = curVal/60;
                }
                if(key.indexOf('processingIntervalTwo') != -1 && curVal != ''){
                    curVal = curVal/60;
                }
                if(key.indexOf('timeThreshold') != -1 && curVal != ''){
                    curVal = curVal/60;
                }
                if(key.indexOf('alarmSwitch') != -1){
                    var alarmSwitch = $('input[name=alarmSwitch_'+id+']');
                    if(curVal == 0){
                        alarmSwitch.parent().parent().parent().find('.col-md-10').css('display','none');
                    }
                    if(curVal == 1){
                        alarmSwitch.parent().parent().parent().find('.col-md-10').css('display','block');
                    }
                }
                targetId.val(curVal);
                targetId = $('#param-media-content select[name="' +newId + '"]');
                targetId.val(curVal);
            }
        },
        /**
         * 提交方法
         * @param:flag
         * ('all':全部下发;'curTab':本页签下发;'save':保存)
         * */
        paramSend: function (flag) {
            var parameter = {
                'vehicleIds': $('#vehicleId').val(),
                'alarmParam': [],
                'platformParam':[],
                'sendFlag': flag == 'save' ? false : true,
            };

            if (flag == 'curTab') {
                var curForm = $('.tab-pane.active').find('form');
                var validates = curForm.attr('id').replace('Form', '') + 'Validates';
                if (!guiSetting[validates]()) {
                    if($('#platformSet').hasClass('active')){
                        layer.msg('设置参数有误');
                    }
                    return;
                }
                var paramType = curForm.find('.paramType').val();
                if(paramType == 0){
                    var platformParamForm = guiSetting.setFormPlatformParam(curForm);
                    for(key in platformParamForm){
                        parameter.platformParam.push(platformParamForm[key]);
                    }
                    parameter.sendFlag = false;
                }else{
                    var result = guiSetting.setFormParam(curForm);
                    parameter.alarmParam.push(result);
                }
                curTabSend.prop('disabled', true);
                var curTabId = $('.nav-tabs li.active').attr('id');
                if (!disabledTabObj[curTabId]) {
                    disabledTabObj[curTabId] = setTimeout(function () {
                        disabledTabObj[curTabId] = null;
                        guiSetting.curTabSendDisabled('timeOut');
                    }, 5000)
                }
            } else {
                if (!guiSetting.forwardValidates()
                    || !guiSetting.driverBehaviorValidates()
                    || !guiSetting.intenseDrivingValidates()
                    || !guiSetting.blindSpotValidates()
                    || !guiSetting.tirePressureValidates()
                    || !guiSetting.platformParamValidates()
                ) {
                    layer.msg('设置参数有误');
                    return;
                }
                var forwardResult = guiSetting.setFormParam($("#forwardForm"));
                var driverBehaviorResult = guiSetting.setFormParam($("#driverBehaviorForm"));
                var intenseDrivingResult = guiSetting.setFormParam($("#intenseDrivingForm"));
                var blindSpotResult = guiSetting.setFormParam($("#blindSpotForm"));//盲区
                var tirePressureResult = guiSetting.setFormParam($("#tirePressureForm"));//胎压
                parameter.alarmParam.push(
                    forwardResult,
                    driverBehaviorResult,
                    intenseDrivingResult,
                    blindSpotResult,
                    tirePressureResult,
                );

                var platformParamForm = guiSetting.setFormPlatformParam($("#platformParamForm"));
                for(key in platformParamForm){
                    parameter.platformParam.push(platformParamForm[key]);
                }

            }
            parameter.alarmParam = JSON.stringify(parameter.alarmParam);
            parameter.platformParam = JSON.stringify(parameter.platformParam);
            var url = settingParam.length == 0 ? addSendUrl : editSendUrl;
            json_ajax("POST", url, "json", true, parameter, function (data) {
                guiSetting.paramSendCallback(data, flag);
            });

            $('.checkAll').prop('checked', false);
        },
        paramSendCallback: function (data, flag) {
            if (data.success) {
                var msgTitle = '下发成功';
                if (flag != 'curTab') {
                    $('#commonWin').modal('hide');
                    if (flag == 'save') {
                        msgTitle = '保存成功';
                    }
                } else {
                    var curForm = $('.tab-pane.active').find('form');
                    var paramType = curForm.find('.paramType').val();
                    if(paramType == 0){
                        var activeName = $('.nav-tabs li.active').text().replace('设置', '');
                        msgTitle = activeName + ' 保存成功';
                    }else{
                        var activeName = $('.nav-tabs li.active').text().replace('设置', '');
                        msgTitle = activeName + ' 下发成功';
                    }
                }
                layer.msg(msgTitle);
                myTable.requestData();
            } else if (data.msg) {
                layer.msg(data.msg);
            }
        },
        // 表单提交数据组装(ps:因功能需要:输入框值为空时,设置该值为-1再传递到后端)
        setFormParam: function (curForm) {
            var paramList = curForm.serializeArray();
            var commonParamSettingObj = {// 通用参数
                'vehicleId': $('#vehicleId').val(),
                'protocolType': 14,
            };
            var paramType = curForm.find('.paramType').val();
            var adasAlarmParamSettings = [];// 报警参数
            var alarmObj = {};
            for (var i = 0, len = paramList.length; i < len; i++) {
                var item = paramList[i];
                var attrName = item.name;
                var attrValue = (item.value == '' ? '-1' : item.value);
                if (attrName.indexOf('_') == -1) {// 组装通用参数
                    commonParamSettingObj[attrName] = attrValue;
                } else if (attrName.indexOf('forward') != -1 || attrName.indexOf('driverBehavior') != -1) {
                    commonParamSettingObj['touchStatus'] = attrValue;
                } else {// 组装报警参数
                    var alarmName = attrName.split('_')[0];
                    var alarmId = attrName.split('_')[1];
                    if (!alarmObj[alarmId]) {
                        alarmObj[alarmId] = {
                            'riskFunctionId': alarmId,
                            'vehicleId': $('#vehicleId').val(),
                            'paramType': paramType,
                        };
                    }
                    if (alarmName.indexOf('timeDistanceThreshold') != -1 && attrValue != '-1') {// 时距阈值需要*10
                        attrValue = attrValue * 10;
                    }
                    alarmObj[alarmId][alarmName] = attrValue;
                }
            }
            for (key in alarmObj) {
                adasAlarmParamSettings.push(alarmObj[key]);
            }
            var result = {
                'commonParamSetting': commonParamSettingObj,
                'adasAlarmParamSettings': adasAlarmParamSettings
            };
            return result;
        },
        // 平台参数设置组装数据
        setFormPlatformParam:function (curForm) {
            var paramList = curForm.serializeArray();
            var alarmObj = {};
            var paramType = curForm.find('.paramType').val();
            for(var i = 1; i < paramList.length; i++){
                var item = paramList[i];
                var attrName = item.name;
                var attrValue = (item.value == '' ? '-1' : item.value);
                var alarmName = attrName.split('_')[0];
                var alarmId = attrName.split('_')[1];
                if (!alarmObj[alarmId]) {
                    alarmObj[alarmId] = {
                        'riskFunctionId': alarmId,
                        'vehicleId': $('#vehicleId').val(),
                        'protocolType': paramType
                    };
                }
                if(alarmName.indexOf('processingIntervalOne') != -1 && attrValue != '-1'){
                    attrValue = attrValue * 60;
                }
                if(alarmName.indexOf('processingIntervalTwo') != -1 && attrValue != '-1'){
                    attrValue = attrValue * 60;
                }
                if(alarmName.indexOf('timeThreshold') != -1 && attrValue != '-1'){
                    attrValue = attrValue * 60;
                }

                alarmObj[alarmId][alarmName] = attrValue;
            }
            return alarmObj;

        },
        // 控制本页签下发按钮是否禁用
        curTabSendDisabled: function (flag) {
            var curTabId = $('.nav-tabs li.active').attr('id');
            if (flag != 'timeOut') {
                curTabId = $(this).attr('id');
            }
            if (!disabledTabObj[curTabId]) {
                curTabSend.prop('disabled', false);
            } else {
                curTabSend.prop('disabled', true);
            }

            if(curTabId == 'platformSet'){
                $("#doSubmits").hide();
                $("#curTabSend").text('保存本页签');
            }else{
                $("#doSubmits").show();
                $("#curTabSend").text('本页签下发');
            }
        },
        /**
         * 表单验证方法
         * */
        // 前向参数设置表单验证
        forwardValidates: function () {
            return $("#forwardForm").validate({
                ignore: '',
                rules: {
                    photographNumber: {
                        digits: true,
                        range: [0, 10]
                    },
                    timingPhotoInterval: {
                        digits: true,
                        range: [60, 60000]
                    },
                    distancePhotoInterval: {
                        decimalOne: true,
                        range: [0, 60]
                    },
                    speedLimit: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_146401: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_146402: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_146403: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_146404: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_146405: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_146407: {
                        digits: true,
                        range: [0, 60]
                    },
                    photographNumber_146401: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_146402: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_146403: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_146404: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_146405: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_146407: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_146409: {
                        digits: true,
                        range: [0, 10]
                    },
                    timeDistanceThreshold_146401: {
                        decimalOne: true,
                        range: [1, 5]
                    },
                    timeDistanceThreshold_146403: {
                        decimalOne: true,
                        range: [1, 5]
                    },
                    timeDistanceThreshold_146404: {
                        decimalOne: true,
                        range: [1, 5]
                    },
                    timeDistanceThreshold_146407: {
                        decimalOne: true,
                        range: [1, 5]
                    },
                    alarmLevelSpeedThreshold_146401: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_146402: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_146403: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_146404: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_146405: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_146407: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    timeSlotThreshold_146405: {
                        digits: true,
                        range: [30, 120]
                    },
                    frequencyThreshold_146405: {
                        digits: true,
                        range: [3, 10]
                    },
                },
                messages: {
                    frequencyThreshold_146405: {
                        digits: frequencyError,
                        range: frequencyError
                    },
                    timeSlotThreshold_146405: {
                        digits: timeSlotError,
                        range: timeSlotError
                    },
                    alarmLevelSpeedThreshold_146401: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_146402: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_146403: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_146404: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_146405: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_146407: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    timeDistanceThreshold_146407: {
                        decimalOne: timeDistanceError,
                        range: timeDistanceError
                    },
                    timeDistanceThreshold_146404: {
                        decimalOne: timeDistanceError,
                        range: timeDistanceError
                    },
                    timeDistanceThreshold_146403: {
                        decimalOne: timeDistanceError,
                        range: timeDistanceError
                    },
                    timeDistanceThreshold_146401: {
                        decimalOne: timeDistanceError,
                        range: timeDistanceError
                    },
                    photographNumber_146401: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_146402: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_146403: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_146404: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_146405: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_146407: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_146409: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    videoRecordingTime_146407: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_146405: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_146404: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_146403: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_146402: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_146401: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    photographNumber: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    timingPhotoInterval: {
                        digits: timingPhotoError,
                        range: timingPhotoError
                    },
                    distancePhotoInterval: {
                        decimalOne: distancePhotoError,
                        range: distancePhotoError
                    },
                    speedLimit: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                }
            }).form();
        },
        // 驾驶员行为参数设置表单验证
        driverBehaviorValidates: function () {
            return $("#driverBehaviorForm").validate({
                ignore: '',
                rules: {
                    photographNumber: {
                        digits: true,
                        range: [0, 10]
                    },
                    timingPhotoInterval: {
                        digits: true,
                        range: [60, 60000]
                    },
                    distancePhotoInterval: {
                        decimalOne: true,
                        range: [0, 60]
                    },
                    speedLimit: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_146513: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_146503: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_146502: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_146508: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_146510: {
                        digits: true,
                        range: [0, 60]
                    },
                    photographNumber_146502: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_146503: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_146508: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_146510: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_146513: {
                        digits: true,
                        range: [0, 10]
                    },
                    alarmLevelSpeedThreshold_146502: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_146503: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_146508: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_146510: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_146513: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                },
                messages: {
                    photographNumber_146502: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_146503: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_146508: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_146510: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_146513: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    alarmLevelSpeedThreshold_146502: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_146503: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_146508: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_146510: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_146513: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    videoRecordingTime_146510: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_146508: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_146502: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_146503: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_146513: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    photographNumber: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    timingPhotoInterval: {
                        digits: timingPhotoError,
                        range: timingPhotoError
                    },
                    distancePhotoInterval: {
                        decimalOne: distancePhotoError,
                        range: distancePhotoError
                    },
                    speedLimit: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                }

            }).form();
        },
        // 激烈驾驶参数设置表单验证
        intenseDrivingValidates: function () {
            return $("#intenseDrivingForm").validate({
                ignore: '',
                rules: {
                    gravityAccelerationThreshold_1464081: {
                        digits: true,
                        range: [1, 100]
                    },
                    gravityAccelerationThreshold_1464082: {
                        digits: true,
                        range: [1, 100]
                    },
                    gravityAccelerationThreshold_1464083: {
                        digits: true,
                        range: [1, 100]
                    },
                    timeThreshold_1464081: {
                        digits: true,
                        range: [1, 10]
                    },
                    timeThreshold_1464082: {
                        digits: true,
                        range: [1, 10]
                    },
                    timeThreshold_1464083: {
                        digits: true,
                        range: [1, 10]
                    },
                    timeThreshold_147001: {//调整
                        digits: true,
                        range: [1, 600]
                    },
                    timeThreshold_147002: {//调整
                        digits: true,
                        range: [1, 600]
                    },
                    timeThreshold_147003: {//调整
                        digits: true,
                        range: [1, 600]
                    },
                    timeThreshold_147004: {//调整
                        digits: true,
                        range: [1, 600]
                    },
                    speedThreshold_147001: {
                        digits: true,
                        range: [1, 30]
                    },
                    speedThreshold_147002: {
                        digits: true,
                        range: [1, 30]
                    },
                    speedThreshold_147003: {
                        digits: true,
                        range: [1, 30]
                    },
                    speedThreshold_147004: {
                        digits: true,
                        range: [1, 30]
                    },
                    engineThreshold_147001: {
                        digits: true,
                        range: [1, 2000]
                    },
                    engineThreshold_147002: {
                        digits: true,
                        range: [1, 2000]
                    },
                    engineThreshold_147003: {
                        digits: true,
                        range: [1, 2000]
                    },
                    engineThreshold_147004: {
                        digits: true,
                        range: [1000, 6000]
                    },
                },
                messages: {
                    engineThreshold_147001: {
                        digits: engineThresholdError,
                        range: engineThresholdError
                    },
                    engineThreshold_147002: {
                        digits: engineThresholdError,
                        range: engineThresholdError
                    },
                    engineThreshold_147003: {
                        digits: engineThresholdError,
                        range: engineThresholdError
                    },
                    engineThreshold_147004: {
                        digits: engineThresholdError,
                        range: engineThresholdError
                    },
                    speedThreshold_147001: {
                        digits: speedThresholdError,
                        range: speedThresholdError
                    },
                    speedThreshold_147002: {
                        digits: speedThresholdError,
                        range: speedThresholdError
                    },
                    speedThreshold_147003: {
                        digits: speedThresholdError,
                        range: speedThresholdError
                    },
                    speedThreshold_147004: {
                        digits: speedThresholdError,
                        range: speedThresholdError
                    },
                    timeThreshold_1464081: {
                        digits: timeThresholdError,
                        range: timeThresholdError
                    },
                    timeThreshold_1464082: {
                        digits: timeThresholdError,
                        range: timeThresholdError
                    },
                    timeThreshold_1464083: {
                        digits: timeThresholdError,
                        range: timeThresholdError
                    },
                    timeThreshold_147001: {
                        digits: timeThresholdError,
                        range: "取值范围1-600"
                    },
                    timeThreshold_147002: {
                        digits: timeThresholdError,
                        range: "取值范围1-600"
                    },
                    timeThreshold_147003: {
                        digits: timeThresholdError,
                        range: "取值范围1-600"
                    },
                    timeThreshold_147004: {
                        digits: timeThresholdError,
                        range: "取值范围1-600"
                    },
                    gravityAccelerationThreshold_1464081: {
                        digits: gravityAccelerationError,
                        range: gravityAccelerationError
                    },
                    gravityAccelerationThreshold_1464082: {
                        digits: gravityAccelerationError,
                        range: gravityAccelerationError
                    },
                    gravityAccelerationThreshold_1464083: {
                        digits: gravityAccelerationError,
                        range: gravityAccelerationError
                    },
                }
            }).form()
        },
        //盲区表单验证
        blindSpotValidates: function(){
            return $("#blindSpotForm").validate({
                ignore: '',
                rules: {
                    rear_146705: {
                        digits: true,
                        range: [1, 10]
                    },
                    sideRear_146705: {
                        digits: true,
                        range: [1, 10]
                    },
                },
                messages: {
                    rear_146705: {
                        digits: "请输入正整数",
                        range: "取值范围0-10",
                    },
                    sideRear_146705: {
                        digits: "请输入正整数",
                        range: "取值范围0-10"
                    },
                }
            }).form()
        },
        //胎压表单验证
        tirePressureValidates: function(){
            return $("#tirePressureForm").validate({
                ignore: '',
                rules: {
                    tyreNumberName:{
                        required:true
                    },
                    tyreNumber_146608: {
                        required:true
                    },
                    pressure_146608: {
                        digits:true,
                        range: [1, 500]
                    },
                    pressureThreshold_146608: {
                        digits:true,
                        range: [0, 100]
                    },
                    slowLeakThreshold_146608: {
                        number:true,
                        range: [0, 100]
                    },
                    lowPressure_146608: {
                        digits:true,
                        range: [1, 500]
                    },
                    highPressure_146608: {
                        digits:true,
                        range: [1, 500]
                    },
                    highTemperature_146608: {
                        digits: true,
                        range: [0, 150]
                    },
                    electricityThreshold_146608: {
                        digits: true,
                        range: [0, 100]
                    },
                    uploadTime_146608: {
                        digits: true,
                        range: [0, 3600]
                    },
                },
                messages: {
                    tyreNumberName:{
                        required: '请选择轮胎规格型号'
                    },
                    tyreNumber_146608: {
                        required: '请选择轮胎规格型号'
                    },
                    pressure_146608: {
                        digits: "取值范围1-500的正整数",
                        range: "取值范围1-500的正整数"
                    },
                    pressureThreshold_146608: {
                        digits: "取值范围0-100正整数",
                        range: "取值范围0-100正整数"
                    },
                    slowLeakThreshold_146608: {
                        digits: "取值范围0-100的正整数",
                        range: "取值范围0-100的正整数"
                    },
                    lowPressure_146608: {
                        digits: "取值范围1-500的正整数",
                        range: "取值范围1-500的正整数"
                    },
                    highPressure_146608: {
                        digits:"取值范围1-500的正整数",
                        range: "取值范围1-500的正整数"
                    },
                    highTemperature_146608: {
                        digits: "取值范围0-150的正整数",
                        range: "取值范围0-150的正整数"
                    },
                    electricityThreshold_146608: {
                        digits: "取值范围0-100的正整数",
                        range: "取值范围0-100的正整数"
                    },
                    uploadTime_146608: {
                        digits: "取值范围0-3600正整数",
                        range: "取值范围0-3600正整数"
                    },
                }
            }).form()
        },
        // 平台参数设置表单验证
        platformParamValidates: function () {
            return $("#platformParamForm").validate({
                ignore: '',
                rules: {
                    //前向碰撞
                    processingIntervalOne_146401: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_146401: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_146401: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_146401: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_146401: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_146401: {
                        digits:true,
                        range: [5, 100]
                    },
                    //车道偏离
                    processingIntervalOne_146402: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_146402: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_146402: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_146402: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_146402: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_146402: {
                        digits:true,
                        range: [5, 100]
                    },
                    //车距过近
                    processingIntervalOne_146403: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_146403: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_146403: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_146403: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_146403: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_146403: {
                        digits:true,
                        range: [5, 100]
                    },
                    //行人碰撞
                    processingIntervalOne_146404: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_146404: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_146404: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_146404: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_146404: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_146404: {
                        digits:true,
                        range: [5, 100]
                    },
                    //频繁变道
                    processingIntervalOne_146405: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_146405: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_146405: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_146405: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_146405: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_146405: {
                        digits:true,
                        range: [5, 100]
                    },
                    //障碍物
                    processingIntervalOne_146407: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_146407: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_146407: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_146407: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_146407: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_146407: {
                        digits:true,
                        range: [5, 100]
                    },
                    //道路标识超限
                    processingIntervalOne_146409: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_146409: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_146409: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_146409: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_146409: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_146409: {
                        digits:true,
                        range: [5, 100]
                    },
                    //驾驶辅助功能失效
                    processingIntervalOne_146519: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_146519: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_146519: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_146519: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_146519: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_146519: {
                        digits:true,
                        range: [5, 100]
                    },
                    //疲劳驾驶
                    processingIntervalOne_146513: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_146513: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_146513: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_146513: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_146513: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_146513: {
                        digits:true,
                        range: [5, 100]
                    },
                    //抽烟
                    processingIntervalOne_146503: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_146503: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_146503: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_146503: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_146503: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_146503: {
                        digits:true,
                        range: [5, 100]
                    },
                    //接打手持电话
                    processingIntervalOne_146502: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_146502: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_146502: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_146502: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_146502: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_146502: {
                        digits:true,
                        range: [5, 100]
                    },
                    //长时间不目视前方
                    processingIntervalOne_146508: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_146508: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_146508: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_146508: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_146508: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_146508: {
                        digits:true,
                        range: [5, 100]
                    },
                    //未检测到驾驶员
                    processingIntervalOne_146510: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_146510: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_146510: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_146510: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_146510: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_146510: {
                        digits:true,
                        range: [5, 100]
                    },
                    //双手同时脱离方向盘
                    processingIntervalOne_146517: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_146517: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_146517: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_146517: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_146517: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_146517: {
                        digits:true,
                        range: [5, 100]
                    },
                    //驾驶员行为监测功能失效
                    processingIntervalOne_146514: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_146514: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_146514: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_146514: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_146514: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_146514: {
                        digits:true,
                        range: [5, 100]
                    },
                    //未系安全带
                    processingIntervalOne_146518: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_146518: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_146518: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_146518: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_146518: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_146518: {
                        digits:true,
                        range: [5, 100]
                    },
                    //驾驶员身份异常
                    processingIntervalOne_146515: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_146515: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_146515: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_146515: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_146515: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_146515: {
                        digits:true,
                        range: [5, 100]
                    },
                    //驾驶员变更
                    processingIntervalOne_146516: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_146516: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_146516: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_146516: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_146516: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_146516: {
                        digits:true,
                        range: [5, 100]
                    },
                    //急加速
                    processingIntervalOne_1464081: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_1464081: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_1464081: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_1464081: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_1464081: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_1464081: {
                        digits:true,
                        range: [5, 100]
                    },
                    //急减速
                    processingIntervalOne_1464082: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_1464082: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_1464082: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_1464082: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_1464082: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_1464082: {
                        digits:true,
                        range: [5, 100]
                    },
                    //急转弯
                    processingIntervalOne_1464083: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_1464083: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_1464083: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_1464083: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_1464083: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_1464083: {
                        digits:true,
                        range: [5, 100]
                    },
                    //异常熄火
                    processingIntervalOne_147002: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_147002: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_147002: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_147002: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_147002: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_147002: {
                        digits:true,
                        range: [5, 100]
                    },
                    //空挡滑行
                    processingIntervalOne_147003: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_147003: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_147003: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_147003: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_147003: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_147003: {
                        digits:true,
                        range: [5, 100]
                    },
                    //发动机超转
                    processingIntervalOne_147004: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_147004: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_147004: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_147004: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_147004: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_147004: {
                        digits:true,
                        range: [5, 100]
                    },
                    //胎压过高
                    processingIntervalOne_146601: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_146601: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_146601: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_146601: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_146601: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_146601: {
                        digits:true,
                        range: [5, 100]
                    },
                    //胎压过低
                    processingIntervalOne_146602: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_146602: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_146602: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_146602: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_146602: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_146602: {
                        digits:true,
                        range: [5, 100]
                    },
                    //胎温过高
                    processingIntervalOne_146603: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_146603: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_146603: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_146603: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_146603: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_146603: {
                        digits:true,
                        range: [5, 100]
                    },
                    //传感器异常
                    processingIntervalOne_146604: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_146604: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_146604: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_146604: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_146604: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_146604: {
                        digits:true,
                        range: [5, 100]
                    },
                    //胎压不平衡
                    processingIntervalOne_146605: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_146605: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_146605: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_146605: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_146605: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_146605: {
                        digits:true,
                        range: [5, 100]
                    },
                    //慢漏气
                    processingIntervalOne_146606: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_146606: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_146606: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_146606: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_146606: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_146606: {
                        digits:true,
                        range: [5, 100]
                    },
                    //电池电量低
                    processingIntervalOne_146607: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_146607: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_146607: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_146607: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_146607: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_146607: {
                        digits:true,
                        range: [5, 100]
                    },
                    //后方接近
                    processingIntervalOne_146701: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_146701: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_146701: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_146701: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_146701: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_146701: {
                        digits:true,
                        range: [5, 100]
                    },
                    //左侧后方接近
                    processingIntervalOne_146702: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_146702: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_146702: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_146702: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_146702: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_146702: {
                        digits:true,
                        range: [5, 100]
                    },
                    //右侧后方接近
                    processingIntervalOne_146703: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_146703: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_146703: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_146703: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_146703: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_146703: {
                        digits:true,
                        range: [5, 100]
                    },
                    //怠速
                    processingIntervalOne_147001: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_147001: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_147001: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_147001: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_147001: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_147001: {
                        digits:true,
                        range: [5, 100]
                    },
                },
                messages: {
                    //前向碰撞
                    processingIntervalOne_146401: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_146401: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_146401: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_146401: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_146401: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_146401: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //车道偏离
                    processingIntervalOne_146402: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_146402: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_146402: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_146402: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_146402: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_146402: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //车距过近
                    processingIntervalOne_146403: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_146403: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_146403: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_146403: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_146403: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_146403: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //行人碰撞
                    processingIntervalOne_146404: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_146404: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_146404: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_146404: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_146404: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_146404: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //频繁变道
                    processingIntervalOne_146405: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_146405: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_146405: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_146405: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_146405: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_146405: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //障碍物
                    processingIntervalOne_146407: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_146407: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_146407: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_146407: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_146407: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_146407: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //道路标识超限
                    processingIntervalOne_146409: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_146409: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_146409: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_146409: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_146409: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_146409: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //驾驶辅助功能失效
                    processingIntervalOne_146519: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_146519: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_146519: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_146519: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_146519: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_146519: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //疲劳驾驶
                    processingIntervalOne_146513: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_146513: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_146513: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_146513: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_146513: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_146513: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //抽烟
                    processingIntervalOne_146503: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_146503: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_146503: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_146503: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_146503: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_146503: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //接打手持电话
                    processingIntervalOne_146502: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_146502: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_146502: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_146502: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_146502: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_146502: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //长时间不目视前方
                    processingIntervalOne_146508: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_146508: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_146508: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_146508: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_146508: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_146508: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //未检测到驾驶员
                    processingIntervalOne_146510: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_146510: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_146510: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_146510: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_146510: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_146510: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //双手同时脱离方向盘
                    processingIntervalOne_146517: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_146517: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_146517: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_146517: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_146517: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_146517: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //驾驶员行为监测功能失效
                    processingIntervalOne_146514: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_146514: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_146514: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_146514: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_146514: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_146514: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //未系安全带
                    processingIntervalOne_146518: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_146518: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_146518: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_146518: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_146518: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_146518: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //驾驶员身份异常
                    processingIntervalOne_146515: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_146515: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_146515: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_146515: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_146515: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_146515: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //驾驶员变更事件
                    processingIntervalOne_146516: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_146516: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_146516: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_146516: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_146516: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_146516: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //急加速
                    processingIntervalOne_1464081: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_1464081: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_1464081: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_1464081: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_1464081: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_1464081: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //急减速
                    processingIntervalOne_1464082: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_1464082: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_1464082: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_1464082: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_1464082: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_1464082: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //急转弯
                    processingIntervalOne_1464083: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_1464083: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_1464083: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_1464083: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_1464083: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_1464083: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //异常熄火
                    processingIntervalOne_147002: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_147002: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_147002: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_147002: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_147002: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_147002: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //空挡滑行
                    processingIntervalOne_147003: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_147003: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_147003: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_147003: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_147003: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_147003: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //发动机超转
                    processingIntervalOne_147004: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_147004: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_147004: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_147004: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_147004: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_147004: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //胎压过高
                    processingIntervalOne_146601: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_146601: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_146601: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_146601: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_146601: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_146601: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //胎压过低
                    processingIntervalOne_146602: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_146602: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_146602: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_146602: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_146602: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_146602: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //胎温过高
                    processingIntervalOne_146603: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_146603: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_146603: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_146603: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_146603: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_146603: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //传感器异常
                    processingIntervalOne_146604: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_146604: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_146604: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_146604: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_146604: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_146604: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //胎压不平衡
                    processingIntervalOne_146605: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_146605: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_146605: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_146605: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_146605: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_146605: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //电池电量低
                    processingIntervalOne_146607: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_146607: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_146607: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_146607: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_146607: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_146607: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //慢漏气
                    processingIntervalOne_146606: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_146606: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_146606: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_146606: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_146606: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_146606: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //后方接近
                    processingIntervalOne_146701: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_146701: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_146701: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_146701: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_146701: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_146701: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //左侧后方接近
                    processingIntervalOne_146702: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_146702: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_146702: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_146702: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_146702: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_146702: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //右侧后方接近
                    processingIntervalOne_146703: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_146703: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_146703: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_146703: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_146703: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_146703: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //怠速
                    processingIntervalOne_147001: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_147001: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_147001: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_147001: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_147001: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_147001: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                }

            }).form()

        },
        /**
         * 页面交互
         * 相关方法
         * */
        // 查看/隐藏更多参数
        hiddenparameterFn: function () {
            var clickId = $(this).attr('id');
            if (!($("#" + clickId + "-content").is(":hidden"))) {
                $("#" + clickId + "-content").slideUp();
                $("#" + clickId).find("font").text("显示更多");
                $("#" + clickId).find("span.fa").removeAttr("class").addClass("fa fa-chevron-down");
            } else {
                $("#" + clickId + "-content").slideDown();
                $("#" + clickId).find("font").text("隐藏参数");
                $("#" + clickId).find("span.fa").removeAttr("class").addClass("fa fa-chevron-up");
            }
        },
        // 触发状态单选按钮切换切换
        touchStatusChange: function (target) {
            var _this = target.target ? $(this) : target;
            var curVal = _this.val();
            var touchStatusInfo = _this.closest('.form-group').siblings('.touchStatusInfo');
            var timingInfo = touchStatusInfo.find('.timingInfo');// 定时拍照
            var distanceInfo = touchStatusInfo.find('.distanceInfo');// 定距拍照
            switch (curVal) {
                case '0':
                    touchStatusInfo.slideUp();
                    touchStatusInfo.find('input,select').prop('disabled', true);
                    break;
                case '1':
                    timingInfo.show();
                    distanceInfo.hide();
                    touchStatusInfo.find('input,select').prop('disabled', false);
                    distanceInfo.find('input,select').prop('disabled', true);
                    touchStatusInfo.slideDown();
                    break;
                case '2':
                    timingInfo.hide();
                    distanceInfo.show();
                    touchStatusInfo.find('input,select').prop('disabled', false);
                    timingInfo.find('input,select').prop('disabled', true);
                    touchStatusInfo.slideDown();
                    break;
                case '3':
                    timingInfo.hide();
                    distanceInfo.hide();
                    touchStatusInfo.find('input,select').prop('disabled', false);
                    timingInfo.find('input,select').prop('disabled', true);
                    distanceInfo.find('input,select').prop('disabled', true);
                    touchStatusInfo.slideDown();
                    break;
            }
        },
        // 一级报警与二级报警状态切换
        oneTwoLevelAlarmChange: function (target) {
            var _this = target.target ? $(this) : target;
            var sibVal = _this.closest('.col-md-4').siblings('.col-md-4').find('input[type="radio"]:checked').val();
            var levelAlarmInfo = _this.closest('.form-group').next('.levelAlarmInfo');
            if (_this.val() == '0' && sibVal == '0') {// 一级报警与二级报警都为关闭状态,隐藏相关参数
                levelAlarmInfo.slideUp();
                levelAlarmInfo.find('input,select').prop('disabled', true);
            } else {
                levelAlarmInfo.slideDown();
                levelAlarmInfo.find('input,select').prop('disabled', false);
            }
        },
        // 道路标识识别单选按钮切换切换
        roadSignRecognitionChange: function (target) {
            var _this = target.target ? $(this) : target;
            var curVal = _this.val();
            var roadSignInfo = _this.closest('.form-group').siblings('.roadSignInfo');
            if (curVal == '0') {
                roadSignInfo.slideUp();
                roadSignInfo.find('input,select').prop('disabled', true);
            } else {
                roadSignInfo.slideDown();
                roadSignInfo.find('input,select').prop('disabled', false);
            }
        },
        // 激烈驾驶下的报警开关单选按钮切换切换
        alarmEnableChange: function (target) {
            var _this = target.target ? $(this) : target;
            var curVal = _this.val();
            var alarmEnableInfo = _this.closest('.form-group').next('.alarmEnableInfo');
            if (curVal == '0') {
                alarmEnableInfo.slideUp();
                alarmEnableInfo.find('input,select').prop('disabled', true);
            } else {
                alarmEnableInfo.slideDown();
                alarmEnableInfo.find('input,select').prop('disabled', false);
            }
        },
        // 监听报警速度阈值变化
        speedLimitChange: function () {
            var thisVal = $(this).val();
            // var newThisVal = thisVal == '' ? '0' : thisVal;
            var speedList = $(this).closest('.tab-pane').find('.alarmLevelSpeedThreshold');
            for (var i = 0, len = speedList.length; i < len; i++) {
                var item = $(speedList[i]);
                var itemId = $(speedList[i]).attr('id');
                var itemVal = item.val();
                var itemError = item.siblings('label.error');
                if ((thisVal == '' && itemVal == '') || ((thisVal != '' && itemVal != '') && (Number(thisVal) < Number(itemVal)))) {
                    itemError.hide();
                } else {
                    if (itemError.length == 0) {
                        item.after('<label id="' + itemId + '-error" class="error" for="' + itemId + '" style="display: inline-block;">必须大于报警判断速度阈值</label>');
                    } else {
                        itemError.show();
                    }
                }
            }
        },

        // 滑块选择切换
        selectSwitch: function () {
            $(".leftselectbutton span").on('click',function () {
                var leftButton =$(this).siblings('.selectbutton').css('left');
                var left = $(this).css('left');
                var leveAlarmInfo = $(this).parent().parent().parent().find('.col-md-10');
                var className = $(this).attr('class');
                if(className == 'button0 button0flag'){
                    $(this).parent().find('.selectvalue').val('1');
                    leveAlarmInfo.slideDown();
                }
                if(className == 'button1 button1flag'){
                    $(this).parent().find('.selectvalue').val('0');
                    leveAlarmInfo.slideUp();
                }

                $(this).siblings('.selectbutton').animate({left:left},'fast');
            });
        },

        // 点击开关滑块切换
        topSwitch: function () {
            $('.open').on('click',function () {
                $(this).parent().parent().find('.selectbutton').animate({left:'9px'}, 'fast');
                $(".selectvalue").val('1');
                $('.clearfix .col-md-10').slideDown();
            });
            $('.off').on('click',function () {
                $(this).parent().parent().find('.selectbutton').animate({left:'55px'}, 'fast');
                $('.selectvalue').val('0')
                $('.clearfix .col-md-10').slideUp();
            })
        },
        // lable点击移动滑块
        labelClickFn: function (){
            var left = $(this).parent().parent().parent().find(".selectbutton").css('left');
            if(left == '55px'){
                $(this).parent().parent().parent().find('.selectbutton').animate({left: "9px"}, "fast");
                $(this).parent().parent().parent().find('.selectvalue').val('1');
                $(this).parent().parent().parent().find('.col-md-10').slideDown();
            }else if(left == '9px'){
                $(this).parent().parent().parent().find('.selectbutton').animate({left: "55px"}, "fast");
                $(this).parent().parent().parent().find('.selectvalue').val('0');
                $(this).parent().parent().parent().find('.col-md-10').slideUp();
            }
        },
        //胎压单位变换
        /*changeShowAndHide: function () {
            var clickId = $(this).attr('id');
            if (!($("#" + clickId + "-body").is(":hidden"))) {
                $("#" + clickId + "-body").slideUp();
                $("#" + clickId).find("font").text("显示更多");
                $("#" + clickId).find("span.fa").removeAttr("class").addClass("fa fa-chevron-down");
            } else {
                $("#" + clickId + "-body").slideDown();
                $("#" + clickId).find("font").text("隐藏参数");
                $("#" + clickId).find("span.fa").removeAttr("class").addClass("fa fa-chevron-up");
            }
        },
        changeUnit :function () {
            var self = $(this);
            var target = self.find('option:selected').text();//目标单位
            var unit = oldPressUnit + target;
            var rate = pressureUnits[unit];

           if(!rate){
               unit = target + oldPressUnit;
               rate = 1 / pressureUnits[unit];
           }

           var pressure = parseFloat($('#pressure_146608').val()),
               lowPressure = parseFloat($('#lowPressure_146608').val()),
               highPressure = parseFloat($('#highPressure_146608').val());

           var pressureRes = (pressure * rate).toFixed(2),
               lowPressureRes = (lowPressure * rate).toFixed(2),
               highPressureRes = (highPressure * rate).toFixed(2);

            $('#pressure_146608').val(pressureRes);
            $('#lowPressure_146608').val(lowPressureRes);
            $('#highPressure_146608').val(highPressureRes);
            oldPressUnit = target;
        },*/
    };
    $(function () {
        guiSetting.init();
        $("input").inputClear();
        /**
         * 页面交互
         * */
        // 查看/隐藏更多参数
        $(".hiddenparameter").bind("click", guiSetting.hiddenparameterFn);
        // 触发状态单选按钮切换切换
        $('.touchStatus').on('change', guiSetting.touchStatusChange);
        // 一级报警与二级报警状态切换
        $('.oneLevelAlarm,.twoLevelAlarm').on('change', guiSetting.oneTwoLevelAlarmChange);
        // 道路标识识别单选按钮切换
        $('.roadSignRecognition').on('change', guiSetting.roadSignRecognitionChange);
        // 激烈驾驶下的报警开关单选按钮切换切换
        $('.alarmEnable').on('change', guiSetting.alarmEnableChange);
        // 监听报警速度阈值变化
        $('.speedLimit').on('input propertychange', guiSetting.speedLimitChange);
        // 监听页签切换,控制本页签下发按钮是否禁用
        $('.nav-tabs li').on('click', guiSetting.curTabSendDisabled);
        // 滑块选择切换
        guiSetting.selectSwitch();
        guiSetting.topSwitch();
        // guiSetting.hideCol();
        $(".typeName").bind('click',guiSetting.labelClickFn);
        // $('.hiddenparameter').on('click',guiSetting.changeShowAndHide);
        // 悬停
        $("[data-toggle='tooltip']").tooltip();
        $("#textinfo span").hover(
            function () {
                $(this).css('color','#6dcff6');
            },
            function () {
                $(this).css('color','#5D5F63')
            }
        );
        $(".typeName").css("cursor","pointer")
        $(".typeName").hover(
            function () {
                $(this).css('color','#6dcff6');
            },
            function () {
                $(this).css('color','#5D5F63')
            }
        );
        /**
         * 下发及保存
         * */
        $('#allParamSend').on('click', function () {
            guiSetting.paramSend('all');
        });
        $('#curTabSend').on('click', function () {
            guiSetting.paramSend('curTab');
        });
        $('#doSubmits').on('click', function () {
            guiSetting.paramSend('save');
        });
    })
})(window, $);