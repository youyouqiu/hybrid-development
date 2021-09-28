//# sourceURL=defineNewSetting.js
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
    var pressureUnits = {
        "psikpa": 6.894757,
        "psibar": 0.068947,
        "psikg/cm²": 0.070307,
        "kpabar": 0.01,
        "kpakg/cm²": 0.0101972,
        "barkg/cm²": 1.0197162,
    };
    var oldPressUnit = 'psi';//胎压原单位

    var defineNewSetting = {
        init: function () {
            $(".modal-body").addClass("modal-body-overflow");
            $(".modal-body").css({"height": "auto", "max-height": ($(window).height() - 194) + "px"});

            // 参考对象渲染
            defineNewSetting.referenceObjRender();
            defineNewSetting.referenceTireModel();//轮胎型号渲染

            // 修改界面,已设置的参数渲染
            if (settingParam.length > 0) {
                defineNewSetting.paramValueRender(settingParam);
            }
            if(settingPlat.length > 0) {
                defineNewSetting.platformValueRender(settingPlat);
            }

            // 表单验证方法
            defineNewSetting.forwardValidates();
            defineNewSetting.driverBehaviorValidates();
            defineNewSetting.intenseDrivingValidates();
            defineNewSetting.blindSpotValidates();
            // defineNewSetting.tirePressureValidates();
            defineNewSetting.platformParamValidates();
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
                            defineNewSetting.platformValueRender(result.platformParam);
                            defineNewSetting.paramValueRender(result.alarmParam);
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
                $('#tyreNumber_126608').val(tireModelId);
            }).on('onUnsetSelectValue', function () {
            });
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

            $('#tyreNumber_126608').val(res);
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
                defineNewSetting.setParamValue(commonParamSetting, paramType);
                for (var j = 0, adasLen = adasAlarmParamSettings.length; j < adasLen; j++) {
                    var data = adasAlarmParamSettings[j];
                    var alarmId = data.riskFunctionId;
                    defineNewSetting.setParamValue(data, alarmId);
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
                        defineNewSetting.touchStatusChange(curTarget);
                    }
                    if (key.indexOf('AlarmEnable') != -1) {// 一级二级报警切换
                        defineNewSetting.oneTwoLevelAlarmChange(curTarget);
                    }
                    if (key.indexOf('roadSign') != -1) {// 道路标识识别切换
                        defineNewSetting.roadSignRecognitionChange(curTarget);
                    }
                    if (key.indexOf('alarmEnable') != -1) {// 激烈驾驶下的报警开关切换
                        defineNewSetting.alarmEnableChange(curTarget);
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
                defineNewSetting.newSetParamValue(data, alarmId);
            }
        },

        newSetParamValue: function (data, id) {
            for (key in data) {
                var curVal = (data[key] == '-1' ? '' : data[key]);
                eventId = curVal
                var newId = key + '_' + id;
                var targetId = $('#param-media-content  input[name="' +newId + '"]');
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

                if(key.indexOf('automaticGetOne') != -1) {
                    if(curVal == 1){
                        targetId.prop('checked',true);
                    }else{
                        targetId.prop('checked',false);
                    }
                }

                if(key.indexOf('automaticGetTwo') != -1) {
                    if(curVal == 1){
                        targetId.prop('checked',true);
                    }else{
                        targetId.prop('checked',false);
                    }
                }
                
                targetId.val(curVal);
                targetId = $('#param-media-content  select[name="' +newId + '"]');
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
                if (!defineNewSetting[validates]())  {
                    if($('#platformSet').hasClass('active')){
                        layer.msg('设置参数有误');
                    }
                    return;
                }
                var paramType = curForm.find('.paramType').val();
                if(paramType == 0){
                    // var result = defineNewSetting.setFormParam(curForm);
                    // parameter.alarmParam.push(result);
                    var platformParamForm = defineNewSetting.setFormPlatformParam(curForm);
                    for(key in platformParamForm){
                        parameter.platformParam.push(platformParamForm[key]);
                    }
                    parameter.sendFlag = false;
                }else{
                    var result = defineNewSetting.setFormParam(curForm);
                    parameter.alarmParam.push(result);
                }
                curTabSend.prop('disabled', true);
                var curTabId = $('.nav-tabs li.active').attr('id');
                if (!disabledTabObj[curTabId]) {
                    disabledTabObj[curTabId] = setTimeout(function () {
                        disabledTabObj[curTabId] = null;
                        defineNewSetting.curTabSendDisabled('timeOut');
                    }, 5000)
                }
            } else {
                if (!defineNewSetting.forwardValidates()
                    || !defineNewSetting.driverBehaviorValidates()
                    || !defineNewSetting.intenseDrivingValidates()
                    || !defineNewSetting.blindSpotValidates()
                    || !defineNewSetting.tirePressureValidates()
                    || !defineNewSetting.platformParamValidates()) {
                    layer.msg('设置参数有误');
                    return;
                }
                var forwardResult = defineNewSetting.setFormParam($("#forwardForm"));
                var driverBehaviorResult = defineNewSetting.setFormParam($("#driverBehaviorForm"));
                var intenseDrivingResult = defineNewSetting.setFormParam($("#intenseDrivingForm"));
                var blindSpotResult = defineNewSetting.setFormParam($("#blindSpotForm"));//盲区
                var tirePressureResult = defineNewSetting.setFormParam($("#tirePressureForm"));//胎压
                parameter.alarmParam.push(
                    forwardResult,
                    driverBehaviorResult,
                    intenseDrivingResult,
                    blindSpotResult,
                    tirePressureResult,
                );

                var platformParamForm = defineNewSetting.setFormPlatformParam($("#platformParamForm"));
                for(key in platformParamForm){
                    parameter.platformParam.push(platformParamForm[key]);
                }

            }
            parameter.alarmParam = JSON.stringify(parameter.alarmParam);
            parameter.platformParam = JSON.stringify(parameter.platformParam);
            var url = settingParam.length == 0 ? addSendUrl : editSendUrl;
            json_ajax("POST", url, "json", true, parameter, function (data) {
                defineNewSetting.paramSendCallback(data, flag);
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
                'protocolType': 12,
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
            console.log('paramList', paramList)
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
                        'protocolType': paramType,
                        'automaticGetOne': $('.automaticGetOne_'+alarmId).val(),
                        'automaticGetTwo': $('.automaticGetTwo_'+alarmId).val(),
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
                    videoRecordingTime_126401: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_126402: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_126403: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_126404: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_126405: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_126407: {
                        digits: true,
                        range: [0, 60]
                    },
                    photographNumber_126401: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_126402: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_126403: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_126404: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_126405: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_126407: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_126409: {
                        digits: true,
                        range: [0, 10]
                    },
                    timeDistanceThreshold_126401: {
                        decimalOne: true,
                        range: [1, 5]
                    },
                    timeDistanceThreshold_126403: {
                        decimalOne: true,
                        range: [1, 5]
                    },
                    timeDistanceThreshold_126404: {
                        decimalOne: true,
                        range: [1, 5]
                    },
                    timeDistanceThreshold_126407: {
                        decimalOne: true,
                        range: [1, 5]
                    },
                    alarmLevelSpeedThreshold_126401: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_126402: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_126403: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_126404: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_126405: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_126407: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    timeSlotThreshold_126405: {
                        digits: true,
                        range: [30, 120]
                    },
                    frequencyThreshold_126405: {
                        digits: true,
                        range: [3, 10]
                    },

                },
                messages: {
                    frequencyThreshold_126405: {
                        digits: frequencyError,
                        range: frequencyError
                    },
                    timeSlotThreshold_126405: {
                        digits: timeSlotError,
                        range: timeSlotError
                    },
                    alarmLevelSpeedThreshold_126401: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_126402: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_126403: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_126404: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_126405: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_126407: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    timeDistanceThreshold_126407: {
                        decimalOne: timeDistanceError,
                        range: timeDistanceError
                    },
                    timeDistanceThreshold_126404: {
                        decimalOne: timeDistanceError,
                        range: timeDistanceError
                    },
                    timeDistanceThreshold_126403: {
                        decimalOne: timeDistanceError,
                        range: timeDistanceError
                    },
                    timeDistanceThreshold_126401: {
                        decimalOne: timeDistanceError,
                        range: timeDistanceError
                    },
                    photographNumber_126401: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_126402: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_126403: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_126404: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_126405: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_126407: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_126409: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    videoRecordingTime_126407: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_126405: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_126404: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_126403: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_126402: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_126401: {
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
                    videoRecordingTime_126513: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_126503: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_126502: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_126508: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_126510: {
                        digits: true,
                        range: [0, 60]
                    },
                    photographNumber_126502: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_126503: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_126508: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_126510: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_126513: {
                        digits: true,
                        range: [0, 10]
                    },
                    alarmLevelSpeedThreshold_126502: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_126503: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_126508: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_126510: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_126513: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                },
                messages: {
                    photographNumber_126502: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_126503: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_126508: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_126510: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_126513: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    alarmLevelSpeedThreshold_126502: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_126503: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_126508: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_126510: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_126513: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    videoRecordingTime_126510: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_126508: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_126502: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_126503: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_126513: {
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
                    gravityAccelerationThreshold_1264081: {
                        digits: true,
                        range: [1, 100]
                    },
                    gravityAccelerationThreshold_1264082: {
                        digits: true,
                        range: [1, 100]
                    },
                    gravityAccelerationThreshold_1264083: {
                        digits: true,
                        range: [1, 100]
                    },
                    timeThreshold_1264081: {
                        digits: true,
                        range: [1, 10]
                    },
                    timeThreshold_1264082: {
                        digits: true,
                        range: [1, 10]
                    },
                    timeThreshold_1264083: {
                        digits: true,
                        range: [1, 10]
                    },
                    timeThreshold_127001: {
                        digits: true,
                        range: [1, 10]
                    },
                    timeThreshold_127002: {
                        digits: true,
                        range: [1, 10]
                    },
                    timeThreshold_127003: {
                        digits: true,
                        range: [1, 10]
                    },
                    timeThreshold_127004: {
                        digits: true,
                        range: [1, 10]
                    },
                    speedThreshold_127001: {
                        digits: true,
                        range: [1, 30]
                    },
                    speedThreshold_127002: {
                        digits: true,
                        range: [1, 30]
                    },
                    speedThreshold_127003: {
                        digits: true,
                        range: [1, 30]
                    },
                    speedThreshold_127004: {
                        digits: true,
                        range: [1, 30]
                    },
                    engineThreshold_127001: {
                        digits: true,
                        range: [1, 2000]
                    },
                    engineThreshold_127002: {
                        digits: true,
                        range: [1, 2000]
                    },
                    engineThreshold_127003: {
                        digits: true,
                        range: [1, 2000]
                    },
                    engineThreshold_127004: {
                        digits: true,
                        range: [1, 2000]
                    },
                },
                messages: {
                    engineThreshold_127001: {
                        digits: engineThresholdError,
                        range: engineThresholdError
                    },
                    engineThreshold_127002: {
                        digits: engineThresholdError,
                        range: engineThresholdError
                    },
                    engineThreshold_127003: {
                        digits: engineThresholdError,
                        range: engineThresholdError
                    },
                    engineThreshold_127004: {
                        digits: engineThresholdError,
                        range: engineThresholdError
                    },
                    speedThreshold_127001: {
                        digits: speedThresholdError,
                        range: speedThresholdError
                    },
                    speedThreshold_127002: {
                        digits: speedThresholdError,
                        range: speedThresholdError
                    },
                    speedThreshold_127003: {
                        digits: speedThresholdError,
                        range: speedThresholdError
                    },
                    speedThreshold_127004: {
                        digits: speedThresholdError,
                        range: speedThresholdError
                    },
                    timeThreshold_1264081: {
                        digits: timeThresholdError,
                        range: timeThresholdError
                    },
                    timeThreshold_1264082: {
                        digits: timeThresholdError,
                        range: timeThresholdError
                    },
                    timeThreshold_1264083: {
                        digits: timeThresholdError,
                        range: timeThresholdError
                    },
                    timeThreshold_127001: {
                        digits: timeThresholdError,
                        range: timeThresholdError
                    },
                    timeThreshold_127002: {
                        digits: timeThresholdError,
                        range: timeThresholdError
                    },
                    timeThreshold_127003: {
                        digits: timeThresholdError,
                        range: timeThresholdError
                    },
                    timeThreshold_127004: {
                        digits: timeThresholdError,
                        range: timeThresholdError
                    },
                    gravityAccelerationThreshold_1264081: {
                        digits: gravityAccelerationError,
                        range: gravityAccelerationError
                    },
                    gravityAccelerationThreshold_1264082: {
                        digits: gravityAccelerationError,
                        range: gravityAccelerationError
                    },
                    gravityAccelerationThreshold_1264083: {
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
                    rear_126704: {
                        digits: true,
                        range: [1, 10]
                    },
                    sideRear_126704: {
                        digits: true,
                        range: [1, 10]
                    },
                },
                messages: {
                    rear_126704: {
                        digits: "请输入正整数",
                        range: "取值范围0-10",
                    },
                    sideRear_126704: {
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
                    tyreNumber_126608: {
                        required:true
                    },
                    pressure_126608: {
                        digits:true,
                        range: [1, 500]
                    },
                    pressureThreshold_126608: {
                        digits:true,
                        range: [0, 100]
                    },
                    slowLeakThreshold_126608: {
                        digits:true,
                        range: [0, 100]
                    },
                    lowPressure_126608: {
                        digits:true,
                        range: [1, 500]
                    },
                    highPressure_126608: {
                        digits:true,
                        range: [1, 500]
                    },
                    highTemperature_126608: {
                        digits: true,
                        range: [0, 150]
                    },
                    electricityThreshold_126608: {
                        digits: true,
                        range: [0, 100]
                    },
                    uploadTime_126608: {
                        digits: true,
                        range: [0, 3600]
                    },
                },
                messages: {
                    tyreNumberName:{
                        required: '请选择轮胎规格型号'
                    },
                    tyreNumber_126608: {
                        required: '请选择轮胎规格型号'
                    },
                    pressure_126608: {
                        digits: "取值范围1-500的正整数",
                        range: "取值范围1-500的正整数"
                    },
                    pressureThreshold_126608: {
                        digits: "取值范围0-100的正整数",
                        range: "取值范围0-100的正整数"
                    },
                    slowLeakThreshold_126608: {
                        digits: "取值范围0-100的正整数",
                        range: "取值范围0-100的正整数"
                    },
                    lowPressure_126608: {
                        digits: "取值范围1-500的正整数",
                        range: "取值范围1-500的正整数"
                    },
                    highPressure_126608: {
                        digits:"取值范围1-500的正整数",
                        range: "取值范围1-500的正整数"
                    },
                    highTemperature_126608: {
                        digits: "取值范围0-150的正整数",
                        range: "取值范围0-150的正整数"
                    },
                    electricityThreshold_126608: {
                        digits: "取值范围0-100的正整数",
                        range: "取值范围0-100的正整数"
                    },
                    uploadTime_126608: {
                        digits: "取值范围0-3600的正整数",
                        range: "取值范围0-3600的正整数"
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
                    processingIntervalOne_126401: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_126401: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_126401: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_126401: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_126401: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_126401: {
                        digits:true,
                        range: [5, 100]
                    },
                    //车道偏离
                    processingIntervalOne_126402: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_126402: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_126402: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_126402: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_126402: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_126402: {
                        digits:true,
                        range: [5, 100]
                    },
                    //车距过近
                    processingIntervalOne_126403: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_126403: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_126403: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_126403: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_126403: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_126403: {
                        digits:true,
                        range: [5, 100]
                    },
                    //行人碰撞
                    processingIntervalOne_126404: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_126404: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_126404: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_126404: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_126404: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_126404: {
                        digits:true,
                        range: [5, 100]
                    },
                    //频繁变道
                    processingIntervalOne_126405: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_126405: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_126405: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_126405: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_126405: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_126405: {
                        digits:true,
                        range: [5, 100]
                    },
                    //障碍物
                    processingIntervalOne_126407: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_126407: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_126407: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_126407: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_126407: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_126407: {
                        digits:true,
                        range: [5, 100]
                    },
                    //道路标识超限
                    processingIntervalOne_126409: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_126409: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_126409: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_126409: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_126409: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_126409: {
                        digits:true,
                        range: [5, 100]
                    },
                    //驾驶辅助功能失效
                    processingIntervalOne_126519: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_126519: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_126519: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_126519: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_126519: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_126519: {
                        digits:true,
                        range: [5, 100]
                    },
                    //疲劳驾驶
                    processingIntervalOne_126513: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_126513: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_126513: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_126513: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_126513: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_126513: {
                        digits:true,
                        range: [5, 100]
                    },
                    //抽烟
                    processingIntervalOne_126503: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_126503: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_126503: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_126503: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_126503: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_126503: {
                        digits:true,
                        range: [5, 100]
                    },
                    //接打手持电话
                    processingIntervalOne_126502: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_126502: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_126502: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_126502: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_126502: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_126502: {
                        digits:true,
                        range: [5, 100]
                    },
                    //长时间不目视前方
                    processingIntervalOne_126508: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_126508: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_126508: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_126508: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_126508: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_126508: {
                        digits:true,
                        range: [5, 100]
                    },
                    //未检测到驾驶员
                    processingIntervalOne_126510: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_126510: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_126510: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_126510: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_126510: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_126510: {
                        digits:true,
                        range: [5, 100]
                    },
                    //双手同时脱离方向盘
                    processingIntervalOne_126517: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_126517: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_126517: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_126517: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_126517: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_126517: {
                        digits:true,
                        range: [5, 100]
                    },
                    //驾驶员行为监测功能失效
                    processingIntervalOne_126514: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_126514: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_126514: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_126514: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_126514: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_126514: {
                        digits:true,
                        range: [5, 100]
                    },
                    //未系安全带
                    processingIntervalOne_126518: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_126518: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_126518: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_126518: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_126518: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_126518: {
                        digits:true,
                        range: [5, 100]
                    },
                    //驾驶员身份异常
                    processingIntervalOne_126515: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_126515: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_126515: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_126515: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_126515: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_126515: {
                        digits:true,
                        range: [5, 100]
                    },
                    //驾驶员变更
                    processingIntervalOne_126516: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_126516: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_126516: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_126516: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_126516: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_126516: {
                        digits:true,
                        range: [5, 100]
                    },
                    //急加速
                    processingIntervalOne_1264081: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_1264081: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_1264081: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_1264081: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_1264081: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_1264081: {
                        digits:true,
                        range: [5, 100]
                    },
                    //急减速
                    processingIntervalOne_1264082: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_1264082: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_1264082: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_1264082: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_1264082: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_1264082: {
                        digits:true,
                        range: [5, 100]
                    },
                    //急转弯
                    processingIntervalOne_1264083: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_1264083: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_1264083: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_1264083: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_1264083: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_1264083: {
                        digits:true,
                        range: [5, 100]
                    },
                    //异常熄火
                    processingIntervalOne_127002: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_127002: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_127002: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_127002: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_127002: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_127002: {
                        digits:true,
                        range: [5, 100]
                    },
                    //空挡滑行
                    processingIntervalOne_127003: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_127003: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_127003: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_127003: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_127003: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_127003: {
                        digits:true,
                        range: [5, 100]
                    },
                    //发动机超转
                    processingIntervalOne_127004: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_127004: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_127004: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_127004: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_127004: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_127004: {
                        digits:true,
                        range: [5, 100]
                    },
                    //胎压过高
                    processingIntervalOne_126601: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_126601: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_126601: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_126601: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_126601: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_126601: {
                        digits:true,
                        range: [5, 100]
                    },
                    //胎压过低
                    processingIntervalOne_126602: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_126602: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_126602: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_126602: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_126602: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_126602: {
                        digits:true,
                        range: [5, 100]
                    },
                    //胎温过高
                    processingIntervalOne_126603: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_126603: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_126603: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_126603: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_126603: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_126603: {
                        digits:true,
                        range: [5, 100]
                    },
                    //传感器异常
                    processingIntervalOne_126604: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_126604: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_126604: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_126604: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_126604: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_126604: {
                        digits:true,
                        range: [5, 100]
                    },
                    //胎压不平衡
                    processingIntervalOne_126605: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_126605: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_126605: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_126605: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_126605: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_126605: {
                        digits:true,
                        range: [5, 100]
                    },
                    //慢漏气
                    processingIntervalOne_126606: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_126606: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_126606: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_126606: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_126606: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_126606: {
                        digits:true,
                        range: [5, 100]
                    },
                    //电池电量低
                    processingIntervalOne_126607: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_126607: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_126607: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_126607: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_126607: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_126607: {
                        digits:true,
                        range: [5, 100]
                    },
                    //后方接近
                    processingIntervalOne_126701: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_126701: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_126701: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_126701: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_126701: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_126701: {
                        digits:true,
                        range: [5, 100]
                    },
                    //左侧后方接近
                    processingIntervalOne_126702: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_126702: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_126702: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_126702: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_126702: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_126702: {
                        digits:true,
                        range: [5, 100]
                    },
                    //右侧后方接近
                    processingIntervalOne_126703: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_126703: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_126703: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_126703: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_126703: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_126703: {
                        digits:true,
                        range: [5, 100]
                    },
                    //怠速
                    processingIntervalOne_127001: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_127001: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_127001: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_127001: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_127001: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_127001: {
                        digits:true,
                        range: [5, 100]
                    },
                },
                messages: {
                    //前向碰撞
                    processingIntervalOne_126401: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_126401: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_126401: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_126401: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_126401: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_126401: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //车道偏离
                    processingIntervalOne_126402: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_126402: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_126402: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_126402: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_126402: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_126402: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //车距过近
                    processingIntervalOne_126403: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_126403: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_126403: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_126403: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_126403: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_126403: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //行人碰撞
                    processingIntervalOne_126404: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_126404: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_126404: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_126404: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_126404: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_126404: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //频繁变道
                    processingIntervalOne_126405: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_126405: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_126405: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_126405: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_126405: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_126405: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //障碍物
                    processingIntervalOne_126407: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_126407: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_126407: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_126407: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_126407: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_126407: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //道路标识超限
                    processingIntervalOne_126409: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_126409: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_126409: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_126409: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_126409: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_126409: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //驾驶辅助功能失效
                    processingIntervalOne_126519: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_126519: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_126519: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_126519: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_126519: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_126519: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //疲劳驾驶
                    processingIntervalOne_126513: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_126513: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_126513: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_126513: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_126513: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_126513: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //抽烟
                    processingIntervalOne_126503: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_126503: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_126503: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_126503: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_126503: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_126503: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //接打手持电话
                    processingIntervalOne_126502: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_126502: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_126502: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_126502: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_126502: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_126502: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //长时间不目视前方
                    processingIntervalOne_126508: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_126508: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_126508: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_126508: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_126508: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_126508: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //未检测到驾驶员
                    processingIntervalOne_126510: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_126510: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_126510: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_126510: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_126510: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_126510: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //双手同时脱离方向盘
                    processingIntervalOne_126517: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_126517: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_126517: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_126517: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_126517: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_126517: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //驾驶员行为监测功能失效
                    processingIntervalOne_126514: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_126514: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_126514: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_126514: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_126514: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_126514: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //未系安全带
                    processingIntervalOne_126518: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_126518: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_126518: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_126518: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_126518: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_126518: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //驾驶员身份异常
                    processingIntervalOne_126515: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_126515: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_126515: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_126515: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_126515: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_126515: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //驾驶员变更事件
                    processingIntervalOne_126516: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_126516: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_126516: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_126516: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_126516: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_126516: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //急加速
                    processingIntervalOne_1264081: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_1264081: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_1264081: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_1264081: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_1264081: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_1264081: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //急减速
                    processingIntervalOne_1264082: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_1264082: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_1264082: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_1264082: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_1264082: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_1264082: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //急转弯
                    processingIntervalOne_1264083: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_1264083: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_1264083: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_1264083: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_1264083: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_1264083: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //异常熄火
                    processingIntervalOne_127002: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_127002: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_127002: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_127002: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_127002: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_127002: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //空挡滑行
                    processingIntervalOne_127003: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_127003: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_127003: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_127003: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_127003: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_127003: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //发动机超转
                    processingIntervalOne_127004: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_127004: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_127004: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_127004: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_127004: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_127004: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //胎压过高
                    processingIntervalOne_126601: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_126601: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_126601: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_126601: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_126601: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_126601: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //胎压过低
                    processingIntervalOne_126602: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_126602: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_126602: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_126602: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_126602: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_126602: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //胎温过高
                    processingIntervalOne_126603: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_126603: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_126603: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_126603: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_126603: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_126603: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //传感器异常
                    processingIntervalOne_126604: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_126604: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_126604: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_126604: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_126604: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_126604: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //胎压不平衡
                    processingIntervalOne_126605: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_126605: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_126605: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_126605: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_126605: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_126605: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //电池电量低
                    processingIntervalOne_126607: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_126607: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_126607: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_126607: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_126607: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_126607: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //慢漏气
                    processingIntervalOne_126606: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_126606: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_126606: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_126606: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_126606: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_126606: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //后方接近
                    processingIntervalOne_126701: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_126701: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_126701: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_126701: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_126701: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_126701: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //左侧后方接近
                    processingIntervalOne_126702: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_126702: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_126702: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_126702: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_126702: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_126702: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //右侧后方接近
                    processingIntervalOne_126703: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_126703: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_126703: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_126703: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_126703: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_126703: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //怠速
                    processingIntervalOne_127001: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_127001: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_127001: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_127001: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_127001: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_127001: {
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

        // 控制自动获取附件处理报警check
        inputClickFn:function () {
            var isChecked = $(this).is(":checked");
            if(isChecked){
                $(this).val(1)
            }else{
                $(this).val(0)
            }
        }
    };
    $(function () {
        defineNewSetting.init();
        $("input").inputClear();
        /**
         * 页面交互
         * */
        // 查看/隐藏更多参数
        $(".hiddenparameter").bind("click", defineNewSetting.hiddenparameterFn);
        // 触发状态单选按钮切换切换
        $('.touchStatus').on('change', defineNewSetting.touchStatusChange);
        // 一级报警与二级报警状态切换
        $('.oneLevelAlarm,.twoLevelAlarm').on('change', defineNewSetting.oneTwoLevelAlarmChange);
        // 道路标识识别单选按钮切换
        $('.roadSignRecognition').on('change', defineNewSetting.roadSignRecognitionChange);
        // 激烈驾驶下的报警开关单选按钮切换切换
        $('.alarmEnable').on('change', defineNewSetting.alarmEnableChange);
        // 监听报警速度阈值变化
        $('.speedLimit').on('input propertychange', defineNewSetting.speedLimitChange);
        // 监听页签切换,控制本页签下发按钮是否禁用
        $('.nav-tabs li').on('click', defineNewSetting.curTabSendDisabled);
        // 滑块选择切换
        defineNewSetting.selectSwitch();
        defineNewSetting.topSwitch();
        // defineNewSetting.hideCol();
        $(".typeName").bind('click',defineNewSetting.labelClickFn);
        $('.hiddenparameter').on('click',defineNewSetting.changeShowAndHide);
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

        // 自动获取附件
        $(".controlCheck").bind('click',defineNewSetting.inputClickFn);

        /**
         * 下发及保存
         * */
        $('#allParamSend').on('click', function () {
            defineNewSetting.paramSend('all');
        });
        $('#curTabSend').on('click', function () {
            defineNewSetting.paramSend('curTab');
        });
        $('#doSubmits').on('click', function () {
            defineNewSetting.paramSend('save');
        });
        // $('#unit_126608').on('change', defineNewSetting.changeUnit);
        $('#tyreNumber').on('blur', defineNewSetting.tireModel);
    })
})(window, $);