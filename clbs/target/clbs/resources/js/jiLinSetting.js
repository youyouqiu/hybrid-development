//# sourceURL=jiLinSetting.js
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
    var jiLinSetting = {
        init: function () {

            $(".modal-body").addClass("modal-body-overflow");
            $(".modal-body").css({"height": "auto", "max-height": ($(window).height() - 194) + "px"});
            //初始化日历插件
            laydate.render({
                elem: '.dateTime'
                , type: 'time'
                , range: '-'
                , format: 'HH:mm'
                , noSecond: true
                , trigger: 'click'
                , beyondTime: true
            });

            // 参考对象渲染
            jiLinSetting.referenceObjRender();
            // 修改界面,已设置的参数渲染
            if (settingParam.length > 0) {
                jiLinSetting.paramValueRender(settingParam, true);
            }
            if(settingPlat.length > 0) {
                jiLinSetting.platformValueRender(settingPlat);
            }

            // 表单验证方法
            jiLinSetting.forwardValidates();
            jiLinSetting.driverBehaviorValidates();
            jiLinSetting.platformParamValidates();
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
                            jiLinSetting.paramValueRender(result.alarmParam);
                            jiLinSetting.platformValueRender(result.platformParam);
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
        platformValueRender: function (dataList) {
            for(var i = 0; i < dataList.length; i++){
                var data = dataList[i];
                var alarmId = data.riskFunctionId;
                jiLinSetting.newSetParamValue(data, alarmId);
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
                jiLinSetting.setParamValue(commonParamSetting, paramType);
                for (var j = 0, adasLen = adasAlarmParamSettings.length; j < adasLen; j++) {
                    var data = adasAlarmParamSettings[j];
                    var alarmId = data.riskFunctionId;
                    jiLinSetting.setParamValue(data, alarmId);
                }
            }
        },
        setParamValue: function (data, id) {
            for (key in data) {
                var curVal = (data[key] == '-1' ? '' : data[key]);
                if (key.indexOf('timeDistanceThreshold') != -1 && curVal != '') {// 时距阈值需要除以10
                    curVal = parseFloat((curVal / 10).toFixed(1));
                }
                if (key.indexOf('Status') != -1 || key.indexOf('larmEnable') != -1 || key.indexOf('roadSign') != -1) {
                    var curTarget = $('.' + key + '_' + id + '[value=' + curVal + ']');
                    curTarget.prop('checked', true);
                    if (key.indexOf('Status') != -1) {// 触发状态切换
                        jiLinSetting.touchStatusChange(curTarget);
                    }
                    if (key.indexOf('larmEnable') != -1) {// 一级二级报警切换
                        jiLinSetting.oneTwoLevelAlarmChange(curTarget);
                    }
                    if (key.indexOf('roadSign') != -1) {// 道路标识识别切换
                        jiLinSetting.roadSignRecognitionChange(curTarget);
                    }
                }
                var targetId = $('#' + key + '_' + id);
                targetId.val(curVal);
            }
        },

        /**
         * 提交方法111
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
                var paramType = curForm.find('.paramType').val();
                if(paramType == 0){
                    var platformParamForm = jiLinSetting.setFormPlatformParam(curForm);
                    for(key in platformParamForm){
                        parameter.platformParam.push(platformParamForm[key]);
                    }
                }else{
                    var result = jiLinSetting.setFormParam(curForm);
                    parameter.alarmParam.push(result);
                }
                var validates = curForm.attr('id').replace('Form', '') + 'Validates';
                if (!jiLinSetting[validates]()) {
                    if($('#platformSet').hasClass('active')){
                        layer.msg('设置参数有误');
                    }
                    return;
                }

                curTabSend.prop('disabled', true);
                var curTabId = $('.nav-tabs li.active').attr('id');
                if (!disabledTabObj[curTabId]) {
                    disabledTabObj[curTabId] = setTimeout(function () {
                        disabledTabObj[curTabId] = null;
                        jiLinSetting.curTabSendDisabled('timeOut');
                    }, 5000)
                }

            } else {
                if (
                    !jiLinSetting.forwardValidates()
                    || !jiLinSetting.driverBehaviorValidates()
                    || !jiLinSetting.blindSpotValidates()
                    || !jiLinSetting.platformParamValidates()) {
                    layer.msg('设置参数有误');
                    return;
                }

                var forwardResult = jiLinSetting.setFormParam($("#forwardForm"));
                var driverBehaviorResult = jiLinSetting.setFormParam($("#driverBehaviorForm"));
                var blindSpotResult = jiLinSetting.setFormParam($("#blindSpotForm"));//盲区
                parameter.alarmParam.push(
                    forwardResult,
                    driverBehaviorResult,
                    blindSpotResult
                );
                var platformParamForm = jiLinSetting.setFormPlatformParam($("#platformParamForm"));
                for(key in platformParamForm){
                    parameter.platformParam.push(platformParamForm[key]);
                }
            }
            // console.log(parameter.platformParam);
            parameter.alarmParam = JSON.stringify(parameter.alarmParam);
            parameter.platformParam = JSON.stringify(parameter.platformParam);
            var url = settingParam.length == 0 ? addSendUrl : editSendUrl;
            json_ajax("POST", url, "json", true, parameter, function (data) {
                jiLinSetting.paramSendCallback(data, flag);
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
                'protocolType': 17,
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
                    //录制时间
                    videoRecordingTime_176401: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_176402: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_176403: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_176404: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_176405: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_176407: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_176410: {
                        digits: true,
                        range: [0, 60]
                    },
                    //拍照张数
                    photographNumber_176401: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_176402: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_176403: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_176404: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_176405: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_166407: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_166409: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_176410: {
                        digits: true,
                        range: [0, 10]
                    },
                    //时距阈值
                    timeDistanceThreshold_176401: {
                        decimalOne: true,
                        range: [1, 5]
                    },
                    timeDistanceThreshold_176403: {
                        decimalOne: true,
                        range: [1, 5]
                    },
                    timeDistanceThreshold_176404: {
                        decimalOne: true,
                        range: [1, 5]
                    },
                    timeDistanceThreshold_166407: {
                        decimalOne: true,
                        range: [1, 5]
                    },
                    //速度阈值
                    alarmLevelSpeedThreshold_176401: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_176402: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_176403: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_176404: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_176405: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_166407: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_176410: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    //时间间隔
                    timeSlotThreshold_176405: {
                        digits: true,
                        range: [30, 120]
                    },
                    //次数阈值
                    frequencyThreshold_176405: {
                        digits: true,
                        range: [3, 10]
                    },

                },
                messages: {
                    frequencyThreshold_176405: {
                        digits: frequencyError,
                        range: frequencyError
                    },
                    timeSlotThreshold_176405: {
                        digits: timeSlotError,
                        range: timeSlotError
                    },
                    alarmLevelSpeedThreshold_176401: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_176402: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_176403: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_176404: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_176405: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_166407: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_176410: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    timeDistanceThreshold_166407: {
                        decimalOne: timeDistanceError,
                        range: timeDistanceError
                    },
                    timeDistanceThreshold_176404: {
                        decimalOne: timeDistanceError,
                        range: timeDistanceError
                    },
                    timeDistanceThreshold_176403: {
                        decimalOne: timeDistanceError,
                        range: timeDistanceError
                    },
                    timeDistanceThreshold_176401: {
                        decimalOne: timeDistanceError,
                        range: timeDistanceError
                    },
                    photographNumber_176401: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_176402: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_176403: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_176404: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_176405: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_166407: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_166409: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_176410: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    videoRecordingTime_166407: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_176405: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_176404: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_176403: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_176402: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_176401: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_176410: {
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
                    //录制时间
                    videoRecordingTime_176513: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_176503: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_176502: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_176504: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_176506: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_176517: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_176505: {
                        digits: true,
                        range: [0, 60]
                    },
                    //拍照张数
                    photographNumber_176513: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_176503: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_176502: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_176504: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_176506: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_176517: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_176505: {
                        digits: true,
                        range: [0, 10]
                    },
                    //速度阈值
                    alarmLevelSpeedThreshold_176513: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_176503: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_176502: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_176504: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_176506: {
                        digits: true,
                        range: [0, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_176517: {
                        digits: true,
                        range: [0, 220],
                        // speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_176505: {
                        digits: true,
                        range: [0, 220],
                        // speedLimitCompare: true
                    },
                    //时间间隔
                    timeSlotThreshold_176503: {
                        digits: true,
                        range: [0, 3600]
                    },
                    timeSlotThreshold_176502: {
                        digits: true,
                        range: [0, 3600]
                    },
                    //最新休息时间
                    minStopTime_176505: {
                        digits: true,
                        range: [0, 1440]
                    },
                    //日间连续驾驶阈值
                    dayTime_176505: {
                        digits: true,
                        range: [0, 24]
                    },
                    //夜间连续驾驶阈值
                    nightTime_176505: {
                        digits: true,
                        range: [0, 24]
                    },
                    //24小时累计驾驶时间阈值
                    oneDayDriveTime_176505: {
                        digits: true,
                        range: [0, 24]
                    },
                },
                messages: {
                    //拍照张数
                    photographNumber_176513: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_176503: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_176502: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_176504: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_176506: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_176517: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_176505: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    //速度阈值
                    alarmLevelSpeedThreshold_176513: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_176503: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_176502: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_176504: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_176506: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_176517: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_176505: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    //录制时间
                    videoRecordingTime_176513: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_176503: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_176502: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_176504: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_176506: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_176517: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_176505: {
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
                    //时间间隔
                    timeSlotThreshold_176503: {
                        digits: '取值范围0-3600的正整数',
                        range: '取值范围0-3600的正整数'
                    },
                    timeSlotThreshold_176502: {
                        digits: '取值范围0-3600的正整数',
                        range: '取值范围0-3600的正整数'
                    },
                    //最新休息时间
                    minStopTime_176505: {
                        digits: '取值范围0-1440的正整数',
                        range: '取值范围0-1440的正整数'
                    },
                    //日间连续驾驶阈值
                    dayTime_176505: {
                        digits: '取值范围0-24的正整数',
                        range: '取值范围0-24的正整数'
                    },
                    //夜间连续驾驶阈值
                    nightTime_176505: {
                        digits: '取值范围0-24的正整数',
                        range: '取值范围0-24的正整数'
                    },
                    //24小时累计驾驶时间阈值
                    oneDayDriveTime_176505: {
                        digits: '取值范围0-24的正整数',
                        range: '取值范围0-24的正整数'
                    },
                }

            }).form();
        },
        //盲区表单验证
        blindSpotValidates: function(){
            return $("#blindSpotForm").validate({
                ignore: '',
                rules: {
                    rear_176706: {
                        digits: true,
                        range: [1, 10]
                    },
                    sideRear_176706: {
                        digits: true,
                        range: [1, 10]
                    },
                },
                messages: {
                    rear_176706: {
                        digits: "取值范围0-10的正整数",
                        range: "取值范围0-10的正整数",
                    },
                    sideRear_176706: {
                        digits: "取值范围0-10的正整数",
                        range: "取值范围0-10的正整数"
                    },
                }
            }).form()
        },
        //平台参数设置表单验证
        platformParamValidates: function () {
            return $("#platformParamForm").validate({
                ignore: '',
                rules: {
                    //前向报警
                    processingIntervalOne_176401: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_176401: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_176401: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_176401: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_176401: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_176401: {
                        digits:true,
                        range: [5, 100]
                    },
                    //车道偏离
                    processingIntervalOne_176402: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_176402: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_176402: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_176402: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_176402: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_176402: {
                        digits:true,
                        range: [5, 100]
                    },
                    //车距过近
                    processingIntervalOne_176403: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_176403: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_176403: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_176403: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_176403: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_176403: {
                        digits:true,
                        range: [5, 100]
                    },
                    //行人碰撞
                    processingIntervalOne_176404: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_176404: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_176404: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_176404: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_176404: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_176404: {
                        digits:true,
                        range: [5, 100]
                    },
                    //频繁变道
                    processingIntervalOne_176405: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_176405: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_176405: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_176405: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_176405: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_176405: {
                        digits:true,
                        range: [5, 100]
                    },
                    //路口快速通过预警
                    processingIntervalOne_166410: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_166410: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_166410: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_166410: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_166410: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_166410: {
                        digits:true,
                        range: [5, 100]
                    },
                    //道路标识超限
                    processingIntervalOne_166409: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_166409: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_166409: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_166409: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_166409: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_166409: {
                        digits:true,
                        range: [5, 100]
                    },
                    //疲劳驾驶
                    processingIntervalOne_176513: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_176513: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_186513: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_176513: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_176513: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_176513: {
                        digits:true,
                        range: [5, 100]
                    },
                    //接打手持电话
                    processingIntervalOne_176502: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_176502: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_176502: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_176502: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_176502: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_176502: {
                        digits:true,
                        range: [5, 100]
                    },
                    //抽烟
                    processingIntervalOne_176503: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_176503: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_176503: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_176503: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_176503: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_176503: {
                        digits:true,
                        range: [5, 100]
                    },
                    //分神驾驶
                    processingIntervalOne_176504: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_176504: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_176504: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_176504: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_176504: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_176504: {
                        digits:true,
                        range: [5, 100]
                    },
                    //驾驶员异常
                    processingIntervalOne_176506: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_176506: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_176506: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_176506: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_176506: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_176506: {
                        digits:true,
                        range: [5, 100]
                    },
                    //探头遮挡
                    processingIntervalOne_166516: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_166516: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_166516: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_166516: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_166516: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_166516: {
                        digits:true,
                        range: [5, 100]
                    },
                    //换人驾驶
                    processingIntervalOne_176517: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_176517: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_176517: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_176517: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_176517: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_176517: {
                        digits:true,
                        range: [5, 100]
                    },
                    //超时驾驶
                    processingIntervalOne_176505: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_176505: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_176505: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_176505: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_176505: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_176505: {
                        digits:true,
                        range: [5, 100]
                    },
                    //驾驶员人脸身份识别事件
                    processingIntervalOne_166517: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_166517: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_166517: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_166517: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_166517: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_166517: {
                        digits:true,
                        range: [5, 100]
                    },
                    //后方接近
                    processingIntervalOne_166701: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_166701: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_166701: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_166701: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_166701: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_166701: {
                        digits:true,
                        range: [5, 100]
                    },
                    //左侧后方接近
                    processingIntervalOne_166702: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_166702: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_166702: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_166702: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_166702: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_166702: {
                        digits:true,
                        range: [5, 100]
                    },
                    //右侧后方接近
                    processingIntervalOne_166703: {
                        digits:true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_166703: {
                        digits:true,
                        range: [30, 60]
                    },
                    timeThreshold_166703: {
                        digits:true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_166703: {
                        digits:true,
                        range: [5, 100]
                    },
                    distanceThreshold_166703: {
                        digits:true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_166703: {
                        digits:true,
                        range: [5, 100]
                    },
                },
                messages: {
                    //前向报警
                    processingIntervalOne_176401: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_176401: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_176401: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_176401: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_176401: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_176401: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //车道偏离
                    processingIntervalOne_176402: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_176402: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_176402: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_176402: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_176402: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_176402: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //车距过近
                    processingIntervalOne_176403: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_176403: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_176403: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_176403: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_176403: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_176403: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //行人碰撞
                    processingIntervalOne_176404: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_176404: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_176404: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_176404: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_176404: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_176404: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //频繁变道
                    processingIntervalOne_176405: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_176405: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_176405: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_176405: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_176405: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_176405: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //路口快速通过预警
                    processingIntervalOne_166410: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_166410: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_166410: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_166410: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_166410: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_166410: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //道路标识超限
                    processingIntervalOne_166409: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_166409: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_166409: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_166409: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_166409: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_166409: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //疲劳驾驶
                    processingIntervalOne_176513: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_176513: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_176513: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_176513: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_176513: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_176513: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //接打手持电话
                    processingIntervalOne_176502: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_176502: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_176502: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_176502: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_176502: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_176502: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //抽烟
                    processingIntervalOne_176503: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_176503: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_176503: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_176503: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_176503: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_176503: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //分神驾驶
                    processingIntervalOne_176504: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_176504: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_176504: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_176504: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_176504: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_176504: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //驾驶员异常
                    processingIntervalOne_176506: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_176506: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_176506: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_176506: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_176506: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_176506: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //探头遮挡
                    processingIntervalOne_166516: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_166516: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_166516: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_166516: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_166516: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_166516: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //换人驾驶
                    processingIntervalOne_176517: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_176517: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_176517: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_176517: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_176517: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_176517: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //超时驾驶
                    processingIntervalOne_176505: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_176505: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_176505: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_176505: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_176505: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_176505: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //驾驶员人脸身份识别事件
                    processingIntervalOne_166517: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_166517: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_166517: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_166517: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_166517: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_166517: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //后方接近
                    processingIntervalOne_166701: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_166701: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_166701: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_166701: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_166701: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_166701: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //左侧后方接近
                    processingIntervalOne_166702: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_166702: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_166702: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_166702: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_166702: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_166702: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    //右侧后方接近
                    processingIntervalOne_166703: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_166703: {
                        digits: "输入范围30-60之间的整数",
                        range:  "输入范围30-60之间的整数"
                    },
                    timeThreshold_166703: {
                        digits:"输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_166703: {
                        digits: '取值范围5-100之间的整数',
                        range:  '取值范围5-100之间的整数'
                    },
                    distanceThreshold_166703: {
                        digits: '取值范围1-100之间的整数',
                        range:  '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_166703: {
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
                var className = $(this).attr('class');
                var leveAlarmInfo = $(this).parent().parent().parent().find('.col-md-10');
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

        // changeShowAndHide: function () {
        //     var clickId = $(this).attr('id');
        //     if (!($("#" + clickId + "-body").is(":hidden"))) {
        //         $("#" + clickId + "-body").slideUp();
        //         $("#" + clickId).find("font").text("显示更多");
        //         $("#" + clickId).find("span.fa").removeAttr("class").addClass("fa fa-chevron-down");
        //     } else {
        //         $("#" + clickId + "-body").slideDown();
        //         $("#" + clickId).find("font").text("隐藏参数");
        //         $("#" + clickId).find("span.fa").removeAttr("class").addClass("fa fa-chevron-up");
        //     }
        // }
    };
    $(function () {
        jiLinSetting.init();
        $("input").inputClear();

        /**
         * 页面交互
         * */
        // 查看/隐藏更多参数
        $(".hiddenparameter").bind("click", jiLinSetting.hiddenparameterFn);
        // 触发状态单选按钮切换切换
        $('.touchStatus').on('change', jiLinSetting.touchStatusChange);
        // 一级报警与二级报警状态切换
        $('.oneLevelAlarm,.twoLevelAlarm').on('change', jiLinSetting.oneTwoLevelAlarmChange);
        // 道路标识识别单选按钮切换
        $('.roadSignRecognition').on('change', jiLinSetting.roadSignRecognitionChange);
        // 监听报警速度阈值变化
        $('.speedLimit').on('input propertychange', jiLinSetting.speedLimitChange);
        // 监听页签切换,控制本页签下发按钮是否禁用
        $('.nav-tabs li').on('click', jiLinSetting.curTabSendDisabled);
        // 滑块选择切换
        jiLinSetting.selectSwitch();
        jiLinSetting.topSwitch();
        $(".typeName").bind('click',jiLinSetting.labelClickFn);
        // $('.hiddenparameter').on('click',jiLinSetting.changeShowAndHide);
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
            jiLinSetting.paramSend('all');
        });
        $('#curTabSend').on('click', function () {
            jiLinSetting.paramSend('curTab');
        });
        $('#doSubmits').on('click', function () {
            jiLinSetting.paramSend('save');
        })
    })
})(window, $);