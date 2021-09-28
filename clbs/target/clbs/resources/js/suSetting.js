//# sourceURL=suSetting.js
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
    var oldPressUnit = 'psi';//胎压原单位*/

    var suSetting = {
        init: function () {
            $(".modal-body").addClass("modal-body-overflow");
            $(".modal-body").css({"height": "auto", "max-height": ($(window).height() - 194) + "px"});
            // 参考对象渲染
            suSetting.referenceObjRender();
            suSetting.referenceTireModel();//轮胎型号渲染
            // 修改界面,已设置的参数渲染
            if (settingParam.length > 0) {
                suSetting.paramValueRender(settingParam, true);
            }
            if(settingPlat.length > 0) {
                suSetting.platformValueRender(settingPlat);
            }
            // 表单验证方法
            suSetting.forwardValidates();//前向参数设置
            suSetting.driverBehaviorValidates();//驾驶员参数设置
            suSetting.platformParamValidates();//平台参数设置
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
                            suSetting.paramValueRender(result.alarmParam);
                            suSetting.platformValueRender(result.platformParam);
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
                $('#tyreNumber_156608').val(tireModelId);
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
                var commonParamSetting = item.commonParamSetting;// 通用参数
                var paramType = commonParamSetting.paramType;
                suSetting.setParamValue(commonParamSetting, paramType);

                // console.log('报警参数', adasAlarmParamSettings);
                for (var j = 0, adasLen = adasAlarmParamSettings.length; j < adasLen; j++) {
                    var data = adasAlarmParamSettings[j];
                    var alarmId = data.riskFunctionId;
                    suSetting.setParamValue(data, alarmId);
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
                        suSetting.touchStatusChange(curTarget);
                    }
                    if (key.indexOf('larmEnable') != -1) {// 一级二级报警切换
                        suSetting.oneTwoLevelAlarmChange(curTarget);
                    }
                    if (key.indexOf('roadSign') != -1) {// 道路标识识别切换
                        suSetting.roadSignRecognitionChange(curTarget);
                    }
                }

                if(key == 'tyreNumberName'){
                    $('#tyreNumber').val(curVal);
                }
                var targetId = $('#' + key + '_' + id);
                targetId.val(curVal);
            }
        },
        platformValueRender: function (dataList) {
            for(var i = 0; i < dataList.length; i++){
                var data = dataList[i];
                var alarmId = data.riskFunctionId;
                suSetting.newSetParamValue(data, alarmId);
            }
        },
        newSetParamValue: function (data, id) {
            for (key in data) {
                var curVal = (data[key] == '-1' ? '' : data[key]);
                var newId=key + '_' + id;
                var targetId = $('input[name="' +newId + '"]');
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
                targetId = $('select[name="' +newId + '"]');
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
                // if (!defineNewJiSetting[validates]()) return;
                var paramType = curForm.find('.paramType').val();
                if(paramType == 0){
                    var platformParamForm = suSetting.setFormPlatformParam(curForm);
                    for(key in platformParamForm){
                        parameter.platformParam.push(platformParamForm[key]);
                    }
                }else{
                    var result = suSetting.setFormParam(curForm);
                    parameter.alarmParam.push(result);
                }

                var validates = curForm.attr('id').replace('Form', '') + 'Validates';
                if (!suSetting[validates]()) return;
                curTabSend.prop('disabled', true);
                var curTabId = $('.nav-tabs li.active').attr('id');
                if (!disabledTabObj[curTabId]) {
                    disabledTabObj[curTabId] = setTimeout(function () {
                        disabledTabObj[curTabId] = null;
                        suSetting.curTabSendDisabled('timeOut');
                    }, 5000)
                }
            } else {
                if (!suSetting.forwardValidates()
                    || !suSetting.driverBehaviorValidates()
                    || !suSetting.blindSpotValidates()
                    || !suSetting.tirePressureValidates()
                    || !suSetting.platformParamValidates()
                ) {
                    layer.msg('设置参数有误');
                    return;
                }
                var forwardResult = suSetting.setFormParam($("#forwardForm"));
                var driverBehaviorResult = suSetting.setFormParam($("#driverBehaviorForm"));
                var blindSpotResult = suSetting.setFormParam($("#blindSpotForm"));//盲区
                var tirePressureResult = suSetting.setFormParam($("#tirePressureForm"));//胎压
                parameter.alarmParam.push(
                    forwardResult,
                    driverBehaviorResult,
                    blindSpotResult,
                    tirePressureResult
                );

                var platformParamForm = suSetting.setFormPlatformParam($("#platformParamForm"));
                for(key in platformParamForm){
                    parameter.platformParam.push(platformParamForm[key]);
                }
            }
            // console.log( parameter.platformParam);
            parameter.alarmParam = JSON.stringify(parameter.alarmParam);
            parameter.platformParam = JSON.stringify(parameter.platformParam);
            var url = settingParam.length == 0 ? addSendUrl : editSendUrl;
            json_ajax("POST", url, "json", true, parameter, function (data) {
                suSetting.paramSendCallback(data, flag);
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
            var vehicleId = $('#vehicleId').val();
            var commonParamSettingObj = {// 通用参数
                'vehicleId': vehicleId,
                'protocolType': 15,
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
                            'vehicleId': vehicleId,
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
                        range: [0, 3600]
                    },
                    distancePhotoInterval: {
                        decimalOne: true,
                        range: [0, 60]
                    },
                    speedLimit: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_156401: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_156402: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_156403: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_156404: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_156405: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_156407: {
                        digits: true,
                        range: [0, 60]
                    },
                    photographNumber_156401: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_156402: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_156403: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_156404: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_156405: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_156407: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_156409: {
                        digits: true,
                        range: [0, 10]
                    },
                    timeDistanceThreshold_156401: {
                        decimalOne: true,
                        range: [1, 5]
                    },
                    timeDistanceThreshold_156403: {
                        decimalOne: true,
                        range: [1, 5]
                    },
                    timeDistanceThreshold_156404: {
                        decimalOne: true,
                        range: [1, 5]
                    },
                    timeDistanceThreshold_156407: {
                        decimalOne: true,
                        range: [1, 5]
                    },
                    alarmLevelSpeedThreshold_156401: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_156402: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_156403: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_156404: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_156405: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_156407: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    timeSlotThreshold_156405: {
                        digits: true,
                        range: [30, 120]
                    },
                    frequencyThreshold_156405: {
                        digits: true,
                        range: [3, 10]
                    },

                },
                messages: {
                    frequencyThreshold_156405: {
                        digits: frequencyError,
                        range: frequencyError
                    },
                    timeSlotThreshold_156405: {
                        digits: timeSlotError,
                        range: timeSlotError
                    },
                    alarmLevelSpeedThreshold_156401: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_156402: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_156403: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_156404: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_156405: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_156407: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    timeDistanceThreshold_156407: {
                        decimalOne: timeDistanceError,
                        range: timeDistanceError
                    },
                    timeDistanceThreshold_156404: {
                        decimalOne: timeDistanceError,
                        range: timeDistanceError
                    },
                    timeDistanceThreshold_156403: {
                        decimalOne: timeDistanceError,
                        range: timeDistanceError
                    },
                    timeDistanceThreshold_156401: {
                        decimalOne: timeDistanceError,
                        range: timeDistanceError
                    },
                    photographNumber_156401: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_156402: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_156403: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_156404: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_156405: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_156407: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_156409: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    videoRecordingTime_156407: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_156405: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_156404: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_156403: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_156402: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_156401: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    photographNumber: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    timingPhotoInterval: {
                        digits: timingPhoto1Error,
                        range: timingPhoto1Error
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
                    videoRecordingTime_156513: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_156503: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_156502: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_156508: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_156510: {
                        digits: true,
                        range: [0, 60]
                    },
                    photographNumber_156502: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_156503: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_156508: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_156510: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_156513: {
                        digits: true,
                        range: [0, 10]
                    },
                    alarmLevelSpeedThreshold_156502: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_156503: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_156508: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_156510: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_156513: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                },
                messages: {
                    photographNumber_156502: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_156503: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_156508: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_156510: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_156513: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    alarmLevelSpeedThreshold_156502: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_156503: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_156508: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_156510: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_156513: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    videoRecordingTime_156510: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_156508: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_156502: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_156503: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_156513: {
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
        //盲区表单验证
        blindSpotValidates: function(){
            return $("#blindSpotForm").validate({
                ignore: '',
                rules: {
                    rear_156704: {
                        digits: true,
                        range: [1, 10]
                    },
                    sideRear_156704: {
                        digits: true,
                        range: [1, 10]
                    },
                },
                messages: {
                    rear_156704: {
                        digits: "取值范围0-10正整数",
                        range: "取值范围0-10正整数",
                    },
                    sideRear_156704: {
                        digits: "取值范围0-10正整数",
                        range: "取值范围0-10正整数"
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
                    tyreNumber_156608: {
                        required:true
                    },
                    pressure_156608: {
                        digits:true,
                        range: [1, 500]
                    },
                    pressureThreshold_156608: {
                        digits:true,
                        range: [0, 100]
                    },
                    slowLeakThreshold_156608: {
                        digits:true,
                        range: [0, 100]
                    },
                    lowPressure_156608: {
                        digits:true,
                        range: [1, 500]
                    },
                    highPressure_156608: {
                        digits: true,
                        range: [1, 500]
                    },
                    highTemperature_156608: {
                        digits: true,
                        range: [0, 150]
                    },
                    electricityThreshold_156608: {
                        digits: true,
                        range: [0, 100]
                    },
                    uploadTime_156608: {
                        digits: true,
                        range: [0, 3600]
                    },
                },
                messages: {
                    tyreNumberName:{
                        required: '请选择轮胎规格型号'
                    },
                    tyreNumber_156608: {
                        required: '请选择轮胎规格型号'
                    },
                    pressure_156608: {
                        digits: "取值范围1-500的正整数",
                        range: "取值范围1-500的正整数"
                    },
                    pressureThreshold_156608: {
                        digits: "取值范围0-100的正整数",
                        range: "取值范围0-100的正整数"
                    },
                    slowLeakThreshold_156608: {
                        digits: "取值范围0-100的正整数",
                        range: "取值范围0-100的正整数"
                    },
                    lowPressure_156608: {
                        digits: "取值范围1-500的正整数",
                        range: "取值范围1-500的正整数"
                    },
                    highPressure_156608: {
                        digits:"取值范围1-500的正整数",
                        range: "取值范围1-500的正整数"
                    },
                    highTemperature_156608: {
                        digits: "取值范围0-150的正整数",
                        range: "取值范围0-150的正整数"
                    },
                    electricityThreshold_156608: {
                        digits: "取值范围0-100的正整数",
                        range: "取值范围0-100的正整数"
                    },
                    uploadTime_156608: {
                        digits: "取值范围0-3600的正整数",
                        range: "取值范围0-3600的正整数"
                    },
                }
            }).form()
        },
        // 平台参数设置表单验证
        platformParamValidates: function () {
            return $("#platformParamForm").validate({
                rules:{
                    //疲劳驾驶
                    processingIntervalOne_156513: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156513: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_156513: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156513: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_156513: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156513: {
                        digits:true,
                        range: [5, 100]
                    },
                    //抽烟
                    processingIntervalOne_156503: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156503: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_156503: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156503: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_156503: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156503: {
                        digits:true,
                        range: [5, 100]
                    },
                    //接打电话
                    processingIntervalOne_156502: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156502: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_156502: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156502: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_156502: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156502: {
                        digits:true,
                        range: [5, 100]
                    },
                    //分神驾驶
                    processingIntervalOne_156505: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156505: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_156505: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156505: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_156505: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156505: {
                        digits:true,
                        range: [5, 100]
                    },
                    //驾驶员异常
                    processingIntervalOne_156504: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156504: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_156504: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156504: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_156504: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156504: {
                        digits:true,
                        range: [5, 100]
                    },
                    //驾驶员变更
                    processingIntervalOne_156516: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156516: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_156516: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156516: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_156516: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156516: {
                        digits:true,
                        range: [5, 100]
                    },
                    //前向碰撞
                    processingIntervalOne_156401: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156401: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_156401: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156401: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_156401: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156401: {
                        digits:true,
                        range: [5, 100]
                    },
                    //车道偏离
                    processingIntervalOne_156402: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156402: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_156402: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156402: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_156402: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156402: {
                        digits:true,
                        range: [5, 100]
                    },
                    //车距过近
                    processingIntervalOne_156403: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156403: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_156403: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156403: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_156403: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156403: {
                        digits:true,
                        range: [5, 100]
                    },
                    //行人碰撞
                    processingIntervalOne_156404: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156404: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_156404: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156404: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_156404: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156404: {
                        digits:true,
                        range: [5, 100]
                    },
                    //频繁变道
                    processingIntervalOne_156405: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156405: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_156405: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156405: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_156405: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156405: {
                        digits:true,
                        range: [5, 100]
                    },
                    //道路标识超限
                    processingIntervalOne_156409: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156409: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_156409: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156409: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_156409: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156409: {
                        digits:true,
                        range: [5, 100]
                    },
                    //障碍物
                    processingIntervalOne_156407: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156407: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_156407: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156407: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_156407: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156407: {
                        digits:true,
                        range: [5, 100]
                    },
                    //胎压过高
                    processingIntervalOne_156601: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156601: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_156601: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156601: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_156601: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156601: {
                        digits:true,
                        range: [5, 100]
                    },
                    //胎压过低
                    processingIntervalOne_156602: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156602: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_156602: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156602: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_156602: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156602: {
                        digits:true,
                        range: [5, 100]
                    },
                    //胎温过高
                    processingIntervalOne_156603: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156603: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_156603: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156603: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_156603: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156603: {
                        digits:true,
                        range: [5, 100]
                    },
                    //传感器异常
                    processingIntervalOne_156604: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156604: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_156604: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156604: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_156604: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156604: {
                        digits:true,
                        range: [5, 100]
                    },
                    //胎压不平衡
                    processingIntervalOne_156605: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156605: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_156605: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156605: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_156605: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156605: {
                        digits:true,
                        range: [5, 100]
                    },
                    //慢漏气
                    processingIntervalOne_156606: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156606: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_156606: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156606: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_156606: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156606: {
                        digits:true,
                        range: [5, 100]
                    },
                    //电池电量低
                    processingIntervalOne_156607: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156607: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_156607: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156607: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_156607: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156607: {
                        digits:true,
                        range: [5, 100]
                    },
                    //后方接近
                    processingIntervalOne_156701: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156701: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_156701: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156701: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_156701: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156701: {
                        digits:true,
                        range: [5, 100]
                    },
                    //左侧后方接近
                    processingIntervalOne_156702: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156702: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_156702: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156702: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_156702: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156702: {
                        digits:true,
                        range: [5, 100]
                    },
                    //右侧后方接近
                    processingIntervalOne_156703: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156703: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_156703: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156703: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_156703: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156703: {
                        digits:true,
                        range: [5, 100]
                    },
                },
                messages:{
                    //疲劳驾驶
                    processingIntervalOne_156513: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156513: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_156513: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156513: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156513: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156513: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //抽烟
                    processingIntervalOne_156503: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156503: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_156503: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156503: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156503: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156503: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //接打电话
                    processingIntervalOne_156502: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156502: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_156502: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156502: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156502: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156502: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //分神驾驶
                    processingIntervalOne_156505: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156505: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_156505: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156505: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156505: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156505: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //驾驶员异常
                    processingIntervalOne_156504: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156504: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_156504: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156504: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156504: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156504: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //驾驶员变更
                    processingIntervalOne_156516: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156516: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_156516: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156516: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156516: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156516: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //前向碰撞
                    processingIntervalOne_156401: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156401: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_156401: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156401: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156401: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156401: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //车道偏离
                    processingIntervalOne_156402: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156402: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_156402: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156402: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156402: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156402: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //车距过近
                    processingIntervalOne_156403: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156403: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_156403: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156403: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156403: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156403: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //行人碰撞
                    processingIntervalOne_156404: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156404: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_156404: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156404: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156404: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156404: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //频繁变道
                    processingIntervalOne_156405: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156405: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_156405: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156405: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156405: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156405: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //道路标识超限
                    processingIntervalOne_156409: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156409: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_156409: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156409: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156409: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156409: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //障碍物
                    processingIntervalOne_156407: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156407: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_156407: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156407: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156407: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156407: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //胎压过高
                    processingIntervalOne_156601: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156601: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_156601: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156601: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156601: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156601: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //胎压过低
                    processingIntervalOne_156602: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156602: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_156602: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156602: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156602: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156602: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //胎温过高
                    processingIntervalOne_156603: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156603: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_156603: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156603: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156603: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156603: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //传感器异常
                    processingIntervalOne_156604: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156604: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_156604: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156604: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156604: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156604: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //胎压不平衡
                    processingIntervalOne_156605: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156605: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_156605: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156605: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156605: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156605: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //慢漏气
                    processingIntervalOne_156606: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156606: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_156606: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156606: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156606: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156606: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //电池电量低
                    processingIntervalOne_156607: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156607: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_156607: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156607: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156607: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156607: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //后方接近
                    processingIntervalOne_156701: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156701: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_156701: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156701: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156701: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156701: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //左侧后方接近
                    processingIntervalOne_156702: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156702: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_156702: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156702: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156702: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156702: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //右侧后方接近
                    processingIntervalOne_156703: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156703: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_156703: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156703: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156703: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156703: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                }
            }).form();
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
                $(this).parent().parent().parent().find('.selectvalue').val('0')
                $(this).parent().parent().parent().find('.col-md-10').slideUp();
            }
        },
        //匹配轮胎型号名称
        tireModel:function (){
            var value = $(this).val();
            var tyreNumberList = JSON.parse($("#tireModel").val());
            var res = -1;

            for(var i=0;i<tyreNumberList.length;i++) {
                var item = tyreNumberList[i];
                if(value == item.name){
                    res = item.tireModelId;
                    break;
                }
            }

            $('#tyreNumber_156608').val(res);
        },
    };
    $(function () {
        /**
         * 初始化
         */
        suSetting.init();
        $("input").inputClear();

        /**
         * 页面交互
         * */
        // 查看/隐藏更多参数
        $(".hiddenparameter").bind("click", suSetting.hiddenparameterFn);
        // 触发状态单选按钮切换切换
        $('.touchStatus').on('change', suSetting.touchStatusChange);
        // 一级报警与二级报警状态切换
        $('.oneLevelAlarm,.twoLevelAlarm').on('change', suSetting.oneTwoLevelAlarmChange);
        // 道路标识识别单选按钮切换
        $('.roadSignRecognition').on('change', suSetting.roadSignRecognitionChange);
        // 监听报警速度阈值变化
        $('.speedLimit').on('input propertychange', suSetting.speedLimitChange);
        // 监听页签切换,控制本页签下发按钮是否禁用
        $('.nav-tabs li').on('click', suSetting.curTabSendDisabled);
        // 滑块选择切换
        suSetting.selectSwitch();
        suSetting.topSwitch();
        $(".typeName").bind('click',suSetting.labelClickFn);
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
            suSetting.paramSend('all');
        });
        $('#curTabSend').on('click', function () {
            suSetting.paramSend('curTab');
        });
        $('#doSubmits').on('click', function () {
            suSetting.paramSend('save');
        });
        $('#tyreNumber').on('blur', suSetting.tireModel);
    })
})(window, $);