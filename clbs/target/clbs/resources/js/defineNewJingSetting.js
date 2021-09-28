//# sourceURL=defineNewJingSetting.js
// ps: 设置、修改、批量设置共用本页面,settingParam不为空时表示是修改界面
// ps: 页面中的参数id基本上都是以参数名+'_'+报警id命名(便于数据组装与赋值)
(function (window, $) {
    var platformParamSetting = $("#platformParamSetting").val();
    var settingPlat = JSON.parse(platformParamSetting ? platformParamSetting : '[]');
    var riskSettingList = $('#riskSettingList').val();
    var settingParam = JSON.parse(riskSettingList ? riskSettingList : '[]');
    var addSendUrl = '/clbs/adas/standard/param/batch/upsert';
    var editSendUrl = '/clbs/adas/standard/param/batch/upsert';
    var disabledTabObj = {};// 存储禁用本页签下发按钮的页签
    var curTabSend = $('#curTabSend');// 本页签下发按钮
    var defineNewJingSetting = {
        init: function () {
            $(".modal-body").addClass("modal-body-overflow");
            $(".modal-body").css({"height": "auto", "max-height": ($(window).height() - 194) + "px"});

            // 参考对象渲染
            defineNewJingSetting.referenceObjRender();
            // 修改界面,已设置的参数渲染
            if (settingParam.length > 0) {
                defineNewJingSetting.paramValueRender(settingParam, true);
            }
            if(settingPlat.length > 0) {
                defineNewJingSetting.platformValueRender(settingPlat);
            }

            // 表单验证方法
            defineNewJingSetting.driverBehaviorValidates();
            defineNewJingSetting.vehicleRunValidates();
            defineNewJingSetting.platformParamValidates();

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
                    url: '/clbs/adas/standard/param/jing/get_' + vehicleId + '.gsp',
                    async: false,
                    dataType: 'json',
                    success: function (data) {
                        if (data.success) {
                            $("#riskReference").val(keyword.key);
                            var result = JSON.parse(data.msg);
                            defineNewJingSetting.paramValueRender(result.alarmParam);
                            defineNewJingSetting.platformValueRender(result.platformParam);
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
                defineNewJingSetting.newSetParamValue(data, alarmId);
            }
        },

        newSetParamValue: function (data, id) {
            for (key in data) {
                var curVal = (data[key] == '-1' ? '' : data[key]);
                var newId = key + '_' + id;
                var targetId = $('input[name="' +newId + '"]');
                var alarmSwitch = $('input[name=alarmSwitch_'+id+']');
                if(key.indexOf('alarmSwitch') != -1) {
                    if(data[key] == 1) {
                        $('.' + newId).parent().parent().find('.selectbutton').css('left','9px');
                        alarmSwitch.parent().parent().parent().find('.col-md-10').css('display','block');
                    }else{
                        $('.' + newId).parent().parent().find('.selectbutton').css('left','55px');
                        alarmSwitch.parent().parent().parent().find('.col-md-10').css('display','none');
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

                if(key.indexOf('automaticGetThree') != -1) {
                    if(curVal == 1){
                        targetId.prop('checked',true);
                    }else{
                        targetId.prop('checked',false);
                    }
                }

                if(key.indexOf('automaticDealOne') != -1) {
                    if(curVal == 1){
                        targetId.prop('checked',true);
                    }else{
                        targetId.prop('checked',false);
                    }
                }

                if(key.indexOf('automaticDealTwo') != -1) {
                    if(curVal == 1){
                        targetId.prop('checked',true);
                    }else{
                        targetId.prop('checked',false);
                    }
                }

                if(key.indexOf('automaticDealThree') != -1) {
                    if(curVal == 1){
                        targetId.prop('checked',true);
                    }else{
                        targetId.prop('checked',false);
                    }
                }

                if(key.indexOf('processingIntervalOne') != -1 && curVal != ''){
                    curVal = curVal/60;
                }
                if(key.indexOf('processingIntervalTwo') != -1 && curVal != ''){
                    curVal = curVal/60;
                }

                if(key.indexOf('processingIntervalThree') != -1 && curVal != ''){
                    curVal = curVal/60;
                }

                if(key.indexOf('timeThreshold') != -1 && curVal != ''){
                    curVal = curVal/60;
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
            console.log('dataList',dataList);
            for(var i = 0; i < dataList.length; i++){
                var item = dataList[i];
                var alarmId = item.riskFunctionId;
                defineNewJingSetting.setParamValue(item,alarmId);
            };
        },
        setParamValue: function (data, id) {
            for(key in data) {
                var curVal = (data[key] == '-1' ? '' : data[key]);
                if (key.indexOf('alarmLevel') != -1 || key.indexOf('speech') != -1) {
                    var curTarget = $('.' + key + '_' + id + '[value=' + curVal + ']');
                    curTarget.prop('checked',true);
                }

                $('#' + key + '_' + id).val(curVal);
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
                'alarmParam':[],
                'platformParam':[],
                'sendFlag': flag == 'save' ? false : true,
            };
            if (flag == 'curTab') {
                var curForm = $('.tab-pane.active').find('form');
                var paramType = curForm.find('.paramType').val();
                if(paramType == 0){
                    var platformParamForm = defineNewJingSetting.setFormPlatformParam(curForm);
                    for(key in platformParamForm){
                        parameter.platformParam.push(platformParamForm[key]);
                    }

                }else{
                    var result = defineNewJingSetting.setFormParam(curForm);
                    parameter.alarmParam.push(result);
                }
                var validates = curForm.attr('id').replace('Form', '') + 'Validates';
                if (!defineNewJingSetting[validates]()) {
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
                        defineNewJingSetting.curTabSendDisabled('timeOut');
                    }, 5000)
                }

            } else {
                if(!defineNewJingSetting.driverBehaviorValidates() || !defineNewJingSetting.vehicleRunValidates() || !defineNewJingSetting.platformParamValidates()){
                    layer.msg('设置参数有误');
                    return;
                }

                var forwardResult = defineNewJingSetting.setFormParam($("#vehicleRunForm"));
                var driverBehaviorResult = defineNewJingSetting.setFormParam($("#driverBehaviorForm"));
                parameter.alarmParam.push(forwardResult, driverBehaviorResult);
                var platformParamForm = defineNewJingSetting.setFormPlatformParam($("#platformParamForm"));
                for(key in platformParamForm){
                    parameter.platformParam.push(platformParamForm[key]);
                }

            }
            parameter.alarmParam = JSON.stringify(parameter.alarmParam);
            parameter.platformParam = JSON.stringify(parameter.platformParam);
            var url = settingParam.length == 0 ? addSendUrl : editSendUrl;
            json_ajax("POST", url, "json", true, parameter, function (data) {
                defineNewJingSetting.paramSendCallback(data, flag);
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
                // myTable.requestData();
                setTimeout(()=>{
                    console.log('刷新')
                    myTable.requestData();
                },500)
            } else if (data.msg) {
                layer.msg(data.msg);
            }
        },
        // 表单提交数据组装(ps:因功能需要:输入框值为空时,设置该值为-1再传递到后端)
        setFormParam: function (curForm) {
            var paramList = curForm.serializeArray();
            var vehicleId = $('#vehicleId').val();
            var paramType = curForm.find('.paramType').val();
            var alarmObj = {};
            var adasAlarmParamSettings = [];
            for (var i = 0; i < paramList.length; i++) {
                var item = paramList[i];
                var attrName = item.name;
                var attrValue = (item.value == "" ? '-1' : item.value);
                var alarmName = attrName.split('_')[0];
                var alarmId = attrName.split('_')[1];
                if(alarmId == undefined){
                    continue;
                }
                if (!alarmObj[alarmId]) {
                    alarmObj[alarmId] = {
                        'riskFunctionId': alarmId,
                        'vehicleId': vehicleId,
                        'paramType': paramType,
                        'protocolType':24
                    };
                }
                if (alarmName.indexOf('timeDistanceThreshold') != -1 && attrValue != '-1') {// 时距阈值需要*10
                    attrValue = attrValue * 10;
                }
                alarmObj[alarmId][alarmName] = attrValue;
            }
            for (key in alarmObj) {
                adasAlarmParamSettings.push(alarmObj[key]);
            }

            return adasAlarmParamSettings;
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
                        'protocolType': paramType,
                        'automaticDealOne':$('.automaticDealOne_'+alarmId).val(),
                        'automaticDealTwo':$('.automaticDealTwo_'+alarmId).val(),
                        'automaticDealThree':$('.automaticDealThree_'+alarmId).val(),
                        'automaticGetOne':$('.automaticGetOne_'+alarmId).val(),
                        'automaticGetTwo':$('.automaticGetTwo_'+alarmId).val(),
                        'automaticGetThree':$('.automaticGetThree_'+alarmId).val(),
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

                if(alarmName.indexOf('processingIntervalThree') != -1 && attrValue != '-1'){
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
        // 驾驶员行为参数设置表单验证
        driverBehaviorValidates: function () {
            return $("#driverBehaviorForm").validate({
                ignore: '',
                rules: {
                    alarmVideoDuration_246501: {
                        required:true,
                        digits: true,
                        range: [0,60]
                    },
                    photographNumber_246501: {
                        required:true,
                        digits: true,
                        range: [0, 10]
                    },
                    speedLimit_246501: {
                        required:true,
                        digits: true,
                        range: [0, 220]
                    },
                    durationThreshold_246501: {
                        required:true,
                        digits: true,
                        range: [0, 255]
                    },

                    alarmVideoDuration_246502: {
                        required:true,
                        digits: true,
                        range: [0,60]
                    },
                    photographNumber_246502: {
                        required:true,
                        digits: true,
                        range: [0, 10]
                    },
                    speedLimit_246502: {
                        required:true,
                        digits: true,
                        range: [0, 220]
                    },
                    durationThreshold_246502: {
                        required:true,
                        digits: true,
                        range: [0, 255]
                    },

                    alarmVideoDuration_246504: {
                        required:true,
                        digits: true,
                        range: [0,60]
                    },
                    photographNumber_246504: {
                        required:true,
                        digits: true,
                        range: [0, 10]
                    },
                    speedLimit_246504: {
                        required:true,
                        digits: true,
                        range: [0, 220]
                    },
                    durationThreshold_246504: {
                        required:true,
                        digits: true,
                        range: [0, 255]
                    },

                    alarmVideoDuration_246503: {
                        required:true,
                        digits: true,
                        range: [0,60]
                    },
                    photographNumber_246503: {
                        required:true,
                        digits: true,
                        range: [0, 10]
                    },
                    speedLimit_246503: {
                        required:true,
                        digits: true,
                        range: [0, 220]
                    },
                    durationThreshold_246503: {
                        required:true,
                        digits: true,
                        range: [0, 255]
                    },

                    alarmVideoDuration_246518: {
                        required:true,
                        digits: true,
                        range: [0,60]
                    },
                    photographNumber_246518: {
                        required:true,
                        digits: true,
                        range: [0, 10]
                    },
                    speedLimit_246518: {
                        required:true,
                        digits: true,
                        range: [0, 220]
                    },
                    durationThreshold_246518: {
                        required:true,
                        digits: true,
                        range: [0, 255]
                    },

                    alarmVideoDuration_246506: {
                        required:true,
                        digits: true,
                        range: [0,60]
                    },
                    photographNumber_246506: {
                        required:true,
                        digits: true,
                        range: [0, 10]
                    },
                    speedLimit_246506: {
                        required:true,
                        digits: true,
                        range: [0, 220]
                    },
                    durationThreshold_246506: {
                        required:true,
                        digits: true,
                        range: [0, 255]
                    },

                    alarmVideoDuration_246505: {
                        required:true,
                        digits: true,
                        range: [0,60]
                    },
                    photographNumber_246505: {
                        required:true,
                        digits: true,
                        range: [0, 10]
                    },
                    speedLimit_246505: {
                        required:true,
                        digits: true,
                        range: [0, 220]
                    },
                    durationThreshold_246505: {
                        required:true,
                        digits: true,
                        range: [0, 255]
                    },
                },
                messages: {
                    alarmVideoDuration_246501: {
                        digits: videoRecordingError,
                        range: videoRecordingError,
                    },
                    photographNumber_246501: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    speedLimit_246501: {
                        digits: alarmLevelError,
                        range: alarmLevelError,
                    },
                    durationThreshold_246501: {
                        digits: '取值范围0-255之间的整数',
                        range: '取值范围0-255之间的整数'
                    },

                    alarmVideoDuration_246502: {
                        digits: videoRecordingError,
                        range: videoRecordingError,
                    },
                    photographNumber_246502: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    speedLimit_246502: {
                        digits: alarmLevelError,
                        range: alarmLevelError,
                    },
                    durationThreshold_246502: {
                        digits: '取值范围0-255之间的整数',
                        range: '取值范围0-255之间的整数'
                    },

                    alarmVideoDuration_246504: {
                        digits: videoRecordingError,
                        range: videoRecordingError,
                    },
                    photographNumber_246504: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    speedLimit_246504: {
                        digits: alarmLevelError,
                        range: alarmLevelError,
                    },
                    durationThreshold_246504: {
                        digits: '取值范围0-255之间的整数',
                        range: '取值范围0-255之间的整数'
                    },

                    alarmVideoDuration_246503: {
                        digits: videoRecordingError,
                        range: videoRecordingError,
                    },
                    photographNumber_246503: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    speedLimit_246503: {
                        digits: alarmLevelError,
                        range: alarmLevelError,
                    },
                    durationThreshold_246503: {
                        digits: '取值范围0-255之间的整数',
                        range: '取值范围0-255之间的整数'
                    },


                    alarmVideoDuration_246518: {
                        digits: videoRecordingError,
                        range: videoRecordingError,
                    },
                    photographNumber_246518: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    speedLimit_246518: {
                        digits: alarmLevelError,
                        range: alarmLevelError,
                    },
                    durationThreshold_246518: {
                        digits: '取值范围0-255之间的整数',
                        range: '取值范围0-255之间的整数'
                    },

                    alarmVideoDuration_246506: {
                        digits: videoRecordingError,
                        range: videoRecordingError,
                    },
                    photographNumber_246506: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    speedLimit_246506: {
                        digits: alarmLevelError,
                        range: alarmLevelError,
                    },
                    durationThreshold_246506: {
                        digits: '取值范围0-255之间的整数',
                        range: '取值范围0-255之间的整数'
                    },

                    alarmVideoDuration_246505: {
                        digits: videoRecordingError,
                        range: videoRecordingError,
                    },
                    photographNumber_246505: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    speedLimit_246505: {
                        digits: alarmLevelError,
                        range: alarmLevelError,
                    },
                    durationThreshold_246505: {
                        digits: '取值范围0-255之间的整数',
                        range: '取值范围0-255之间的整数'
                    },


                }
            }).form();
        },
        // 车辆运行监测设置表单验证
        vehicleRunValidates: function () {
            return $("#vehicleRunForm").validate({
                ignore: '',
                rules: {
                    alarmVideoDuration_246401: {
                        required:true,
                        digits: true,
                        range: [0,60]
                    },
                    photographNumber_246401: {
                        required:true,
                        digits: true,
                        range: [0, 10]
                    },
                    speedLimit_246401: {
                        required:true,
                        digits: true,
                        range: [0, 220]
                    },
                    durationThreshold_246401: {
                        required:true,
                        digits: true,
                        range: [0, 255]
                    },

                    alarmVideoDuration_246403: {
                        required:true,
                        digits: true,
                        range: [0,60]
                    },
                    photographNumber_246403: {
                        required:true,
                        digits: true,
                        range: [0, 10]
                    },
                    speedLimit_246403: {
                        digits: true,
                        range: [0, 220]
                    },
                    durationThreshold_246403: {
                        required:true,
                        digits: true,
                        range: [0, 255]
                    },

                    alarmVideoDuration_246402: {
                        required:true,
                        digits: true,
                        range: [0,60]
                    },
                    photographNumber_246402: {
                        required:true,
                        digits: true,
                        range: [0, 10]
                    },
                    speedLimit_246402: {
                        required:true,
                        digits: true,
                        range: [0, 220]
                    },
                    durationThreshold_246402: {
                        required:true,
                        digits: true,
                        range: [0, 255]
                    },

                    alarmVideoDuration_246404: {
                        required:true,
                        digits: true,
                        range: [0,60]
                    },
                    photographNumber_246404: {
                        required:true,
                        digits: true,
                        range: [0, 10]
                    },
                    speedLimit_246404: {
                        required:true,
                        digits: true,
                        range: [0, 220]
                    },
                    durationThreshold_246404: {
                        required:true,
                        digits: true,
                        range: [0, 255]
                    },

                    alarmVideoDuration_246405: {
                        required:true,
                        digits: true,
                        range: [0,60]
                    },
                    photographNumber_246405: {
                        required:true,
                        digits: true,
                        range: [0, 10]
                    },
                    speedLimit_246405: {
                        required:true,
                        digits: true,
                        range: [0, 220]
                    },
                    durationThreshold_246405: {
                        required:true,
                        digits: true,
                        range: [0, 255]
                    },

                    alarmVideoDuration_246406: {
                        required:true,
                        digits: true,
                        range: [0,60]
                    },
                    photographNumber_246406: {
                        required:true,
                        digits: true,
                        range: [0, 10]
                    },
                    speedLimit_246406: {
                        required:true,
                        digits: true,
                        range: [0, 220]
                    },
                    durationThreshold_246406: {
                        required:true,
                        digits: true,
                        range: [0, 255]
                    },

                    alarmVideoDuration_246407: {
                        required:true,
                        digits: true,
                        range: [0,60]
                    },
                    photographNumber_246407: {
                        required:true,
                        digits: true,
                        range: [0, 10]
                    },
                    speedLimit_246407: {
                        required:true,
                        digits: true,
                        range: [0, 220]
                    },
                    durationThreshold_246407: {
                        required:true,
                        digits: true,
                        range: [0, 255]
                    },

                    alarmVideoDuration_246408: {
                        required:true,
                        digits: true,
                        range: [0,60]
                    },
                    photographNumber_246408: {
                        required:true,
                        digits: true,
                        range: [0, 10]
                    },
                    speedLimit_246408: {
                        required:true,
                        digits: true,
                        range: [0, 220]
                    },
                    durationThreshold_246408: {
                        required:true,
                        digits: true,
                        range: [0, 255]
                    },

                    alarmVideoDuration_246409: {
                        required:true,
                        digits: true,
                        range: [0,60]
                    },
                    photographNumber_246409: {
                        required:true,
                        digits: true,
                        range: [0, 10]
                    },
                    speedLimit_246409: {
                        required:true,
                        digits: true,
                        range: [0, 220]
                    },
                    durationThreshold_246409: {
                        required:true,
                        digits: true,
                        range: [0, 255]
                    },
                },
                messages: {
                    alarmVideoDuration_246401: {
                        digits: videoRecordingError,
                        range: videoRecordingError,
                    },
                    photographNumber_246401: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    speedLimit_246401: {
                        digits: alarmLevelError,
                        range: alarmLevelError,
                    },
                    durationThreshold_246401: {
                        digits: '取值范围0-255之间的整数',
                        range: '取值范围0-255之间的整数'
                    },

                    alarmVideoDuration_246403: {
                        digits: videoRecordingError,
                        range: videoRecordingError,
                    },
                    photographNumber_246403: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    speedLimit_246403: {
                        digits: alarmLevelError,
                        range: alarmLevelError,
                    },
                    durationThreshold_246403: {
                        digits: '取值范围0-255之间的整数',
                        range: '取值范围0-255之间的整数'
                    },

                    alarmVideoDuration_246402: {
                        digits: videoRecordingError,
                        range: videoRecordingError,
                    },
                    photographNumber_246402: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    speedLimit_246402: {
                        digits: alarmLevelError,
                        range: alarmLevelError,
                    },
                    durationThreshold_246402: {
                        digits: '取值范围0-255之间的整数',
                        range: '取值范围0-255之间的整数'
                    },

                    alarmVideoDuration_246404: {
                        digits: videoRecordingError,
                        range: videoRecordingError,
                    },
                    photographNumber_246404: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    speedLimit_246404: {
                        digits: alarmLevelError,
                        range: alarmLevelError,
                    },
                    durationThreshold_246404: {
                        digits: '取值范围0-255之间的整数',
                        range: '取值范围0-255之间的整数'
                    },

                    alarmVideoDuration_246405: {
                        digits: videoRecordingError,
                        range: videoRecordingError,
                    },
                    photographNumber_246405: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    speedLimit_246405: {
                        digits: alarmLevelError,
                        range: alarmLevelError,
                    },
                    durationThreshold_246405: {
                        digits: '取值范围0-255之间的整数',
                        range: '取值范围0-255之间的整数'
                    },

                    alarmVideoDuration_246406: {
                        digits: videoRecordingError,
                        range: videoRecordingError,
                    },
                    photographNumber_246406: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    speedLimit_246406: {
                        digits: alarmLevelError,
                        range: alarmLevelError,
                    },
                    durationThreshold_246406: {
                        digits: '取值范围0-255之间的整数',
                        range: '取值范围0-255之间的整数'
                    },

                    alarmVideoDuration_246407: {
                        digits: videoRecordingError,
                        range: videoRecordingError,
                    },
                    photographNumber_246407: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    speedLimit_246407: {
                        digits: alarmLevelError,
                        range: alarmLevelError,
                    },
                    durationThreshold_246407: {
                        digits: '取值范围0-255之间的整数',
                        range: '取值范围0-255之间的整数'
                    },

                    alarmVideoDuration_246408: {
                        digits: videoRecordingError,
                        range: videoRecordingError,
                    },
                    photographNumber_246408: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    speedLimit_246408: {
                        digits: alarmLevelError,
                        range: alarmLevelError,
                    },
                    durationThreshold_246408: {
                        digits: '取值范围0-255之间的整数',
                        range: '取值范围0-255之间的整数'
                    },

                    alarmVideoDuration_246409: {
                        digits: videoRecordingError,
                        range: videoRecordingError,
                    },
                    photographNumber_246409: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    speedLimit_246409: {
                        digits: alarmLevelError,
                        range: alarmLevelError,
                    },
                    durationThreshold_246409: {
                        digits: '取值范围0-255之间的整数',
                        range: '取值范围0-255之间的整数'
                    },
                }

            }).form();
        },
        //平台参数设置表单验证
        platformParamValidates: function () {
            return $("#platformParamForm").validate({
                ignore: '',
                rules: {
                    processingIntervalOne_246501:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalTwo_246501:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalThree_246501:{
                        digits: true,
                        range: [30,60]
                    },
                    timeThreshold_246501:{
                        digits: true,
                        range: [1,60]
                    },
                    timeAlarmNumThreshold_246501: {
                        digits: true,
                        range: [5,100]
                    },
                    distanceThreshold_246501: {
                        digits: true,
                        range: [1,100]
                    },
                    distanceAlarmNumThreshold_246501:{
                        digits: true,
                        range: [5,100]
                    },

                    processingIntervalOne_246502:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalTwo_246502:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalThree_246502:{
                        digits: true,
                        range: [30,60]
                    },
                    timeThreshold_246502:{
                        digits: true,
                        range: [1,60]
                    },
                    timeAlarmNumThreshold_246502: {
                        digits: true,
                        range: [5,100]
                    },
                    distanceThreshold_246502: {
                        digits: true,
                        range: [1,100]
                    },
                    distanceAlarmNumThreshold_246502:{
                        digits: true,
                        range: [5,100]
                    },

                    processingIntervalOne_246504:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalTwo_246504:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalThree_246504:{
                        digits: true,
                        range: [30,60]
                    },
                    timeThreshold_246504:{
                        digits: true,
                        range: [1,60]
                    },
                    timeAlarmNumThreshold_246504: {
                        digits: true,
                        range: [5,100]
                    },
                    distanceThreshold_246504: {
                        digits: true,
                        range: [1,100]
                    },
                    distanceAlarmNumThreshold_246504:{
                        digits: true,
                        range: [5,100]
                    },

                    processingIntervalOne_246503:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalTwo_246503:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalThree_246503:{
                        digits: true,
                        range: [30,60]
                    },
                    timeThreshold_246503:{
                        digits: true,
                        range: [1,60]
                    },
                    timeAlarmNumThreshold_246503: {
                        digits: true,
                        range: [5,100]
                    },
                    distanceThreshold_246503: {
                        digits: true,
                        range: [1,100]
                    },
                    distanceAlarmNumThreshold_246503:{
                        digits: true,
                        range: [5,100]
                    },

                    processingIntervalOne_246518:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalTwo_246518:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalThree_246518:{
                        digits: true,
                        range: [30,60]
                    },
                    timeThreshold_246518:{
                        digits: true,
                        range: [1,60]
                    },
                    timeAlarmNumThreshold_246518: {
                        digits: true,
                        range: [5,100]
                    },
                    distanceThreshold_246518: {
                        digits: true,
                        range: [1,100]
                    },
                    distanceAlarmNumThreshold_246518:{
                        digits: true,
                        range: [5,100]
                    },

                    processingIntervalOne_246506:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalTwo_246506:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalThree_246506:{
                        digits: true,
                        range: [30,60]
                    },
                    timeThreshold_246506:{
                        digits: true,
                        range: [1,60]
                    },
                    timeAlarmNumThreshold_246506: {
                        digits: true,
                        range: [5,100]
                    },
                    distanceThreshold_246506: {
                        digits: true,
                        range: [1,100]
                    },
                    distanceAlarmNumThreshold_246506:{
                        digits: true,
                        range: [5,100]
                    },

                    processingIntervalOne_246505:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalTwo_246505:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalThree_246505:{
                        digits: true,
                        range: [30,60]
                    },
                    timeThreshold_246505:{
                        digits: true,
                        range: [1,60]
                    },
                    timeAlarmNumThreshold_246505: {
                        digits: true,
                        range: [5,100]
                    },
                    distanceThreshold_246505: {
                        digits: true,
                        range: [1,100]
                    },
                    distanceAlarmNumThreshold_246505:{
                        digits: true,
                        range: [5,100]
                    },

                    processingIntervalOne_246401:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalTwo_246401:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalThree_246401:{
                        digits: true,
                        range: [30,60]
                    },
                    timeThreshold_246401:{
                        digits: true,
                        range: [1,60]
                    },
                    timeAlarmNumThreshold_246401: {
                        digits: true,
                        range: [5,100]
                    },
                    distanceThreshold_246401: {
                        digits: true,
                        range: [1,100]
                    },
                    distanceAlarmNumThreshold_246401:{
                        digits: true,
                        range: [5,100]
                    },

                    processingIntervalOne_246403:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalTwo_246403:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalThree_246403:{
                        digits: true,
                        range: [30,60]
                    },
                    timeThreshold_246403:{
                        digits: true,
                        range: [1,60]
                    },
                    timeAlarmNumThreshold_246403: {
                        digits: true,
                        range: [5,100]
                    },
                    distanceThreshold_246403: {
                        digits: true,
                        range: [1,100]
                    },
                    distanceAlarmNumThreshold_246403:{
                        digits: true,
                        range: [5,100]
                    },

                    processingIntervalOne_246402:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalTwo_246402:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalThree_246402:{
                        digits: true,
                        range: [30,60]
                    },
                    timeThreshold_246402:{
                        digits: true,
                        range: [1,60]
                    },
                    timeAlarmNumThreshold_246402: {
                        digits: true,
                        range: [5,100]
                    },
                    distanceThreshold_246402: {
                        digits: true,
                        range: [1,100]
                    },
                    distanceAlarmNumThreshold_246402:{
                        digits: true,
                        range: [5,100]
                    },

                    processingIntervalOne_246404:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalTwo_246404:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalThree_246404:{
                        digits: true,
                        range: [30,60]
                    },
                    timeThreshold_246404:{
                        digits: true,
                        range: [1,60]
                    },
                    timeAlarmNumThreshold_246404: {
                        digits: true,
                        range: [5,100]
                    },
                    distanceThreshold_246404: {
                        digits: true,
                        range: [1,100]
                    },
                    distanceAlarmNumThreshold_246404:{
                        digits: true,
                        range: [5,100]
                    },
                    processingIntervalOne_246405:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalTwo_246405:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalThree_246405:{
                        digits: true,
                        range: [30,60]
                    },
                    timeThreshold_246405:{
                        digits: true,
                        range: [1,60]
                    },
                    timeAlarmNumThreshold_246405: {
                        digits: true,
                        range: [5,100]
                    },
                    distanceThreshold_246405: {
                        digits: true,
                        range: [1,100]
                    },
                    distanceAlarmNumThreshold_246405:{
                        digits: true,
                        range: [5,100]
                    },

                    processingIntervalOne_246406:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalTwo_246406:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalThree_246406:{
                        digits: true,
                        range: [30,60]
                    },
                    timeThreshold_246406:{
                        digits: true,
                        range: [1,60]
                    },
                    timeAlarmNumThreshold_246406: {
                        digits: true,
                        range: [5,100]
                    },
                    distanceThreshold_246406: {
                        digits: true,
                        range: [1,100]
                    },
                    distanceAlarmNumThreshold_246406:{
                        digits: true,
                        range: [5,100]
                    },

                    processingIntervalOne_246407:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalTwo_246407:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalThree_246407:{
                        digits: true,
                        range: [30,60]
                    },
                    timeThreshold_246407:{
                        digits: true,
                        range: [1,60]
                    },
                    timeAlarmNumThreshold_246407: {
                        digits: true,
                        range: [5,100]
                    },
                    distanceThreshold_246407: {
                        digits: true,
                        range: [1,100]
                    },
                    distanceAlarmNumThreshold_246407:{
                        digits: true,
                        range: [5,100]
                    },

                    processingIntervalOne_246409:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalTwo_246409:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalThree_246409:{
                        digits: true,
                        range: [30,60]
                    },
                    timeThreshold_246409:{
                        digits: true,
                        range: [1,60]
                    },
                    timeAlarmNumThreshold_246409: {
                        digits: true,
                        range: [5,100]
                    },
                    distanceThreshold_246409: {
                        digits: true,
                        range: [1,100]
                    },
                    distanceAlarmNumThreshold_246409:{
                        digits: true,
                        range: [5,100]
                    },

                    processingIntervalOne_246408:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalTwo_246408:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalThree_246408:{
                        digits: true,
                        range: [30,60]
                    },
                    timeThreshold_246408:{
                        digits: true,
                        range: [1,60]
                    },
                    timeAlarmNumThreshold_246408: {
                        digits: true,
                        range: [5,100]
                    },
                    distanceThreshold_246408: {
                        digits: true,
                        range: [1,100]
                    },
                    distanceAlarmNumThreshold_246408:{
                        digits: true,
                        range: [5,100]
                    },

                    processingIntervalOne_246508:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalTwo_246508:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalThree_246508:{
                        digits: true,
                        range: [30,60]
                    },
                    timeThreshold_246508:{
                        digits: true,
                        range: [1,60]
                    },
                    timeAlarmNumThreshold_246508: {
                        digits: true,
                        range: [5,100]
                    },
                    distanceThreshold_246508: {
                        digits: true,
                        range: [1,100]
                    },
                    distanceAlarmNumThreshold_246508:{
                        digits: true,
                        range: [5,100]
                    },

                    processingIntervalOne_246507:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalTwo_246507:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalThree_246507:{
                        digits: true,
                        range: [30,60]
                    },
                    timeThreshold_246507:{
                        digits: true,
                        range: [1,60]
                    },
                    timeAlarmNumThreshold_246507: {
                        digits: true,
                        range: [5,100]
                    },
                    distanceThreshold_246507: {
                        digits: true,
                        range: [1,100]
                    },
                    distanceAlarmNumThreshold_246507:{
                        digits: true,
                        range: [5,100]
                    },

                    processingIntervalOne_246509:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalTwo_246509:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalThree_246509:{
                        digits: true,
                        range: [30,60]
                    },
                    timeThreshold_246509:{
                        digits: true,
                        range: [1,60]
                    },
                    timeAlarmNumThreshold_246509: {
                        digits: true,
                        range: [5,100]
                    },
                    distanceThreshold_246509: {
                        digits: true,
                        range: [1,100]
                    },
                    distanceAlarmNumThreshold_246509:{
                        digits: true,
                        range: [5,100]
                    },

                    processingIntervalOne_246514:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalTwo_246514:{
                        digits: true,
                        range: [30,60]
                    },
                    processingIntervalThree_246514:{
                        digits: true,
                        range: [30,60]
                    },
                    timeThreshold_246514:{
                        digits: true,
                        range: [1,60]
                    },
                    timeAlarmNumThreshold_246514: {
                        digits: true,
                        range: [5,100]
                    },
                    distanceThreshold_246514: {
                        digits: true,
                        range: [1,100]
                    },
                    distanceAlarmNumThreshold_246514:{
                        digits: true,
                        range: [5,100]
                    },
                },
                messages: {
                    processingIntervalOne_246501:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalTwo_246501:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalThree_246501:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    timeThreshold_246501:{
                        digits: '输入范围1-60之间的整数',
                        range: '输入范围1-60之间的整数'
                    },
                    timeAlarmNumThreshold_246501: {
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },
                    distanceThreshold_246501: {
                        digits: '输入范围1-100之间的整数',
                        range: '输入范围1-100之间的整数',
                    },
                    distanceAlarmNumThreshold_246501:{
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },

                    processingIntervalOne_246502:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalTwo_246502:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalThree_246502:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    timeThreshold_246502:{
                        digits: '输入范围1-60之间的整数',
                        range: '输入范围1-60之间的整数'
                    },
                    timeAlarmNumThreshold_246502: {
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },
                    distanceThreshold_246502: {
                        digits: '输入范围1-100之间的整数',
                        range: '输入范围1-100之间的整数',
                    },
                    distanceAlarmNumThreshold_246502:{
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },

                    processingIntervalOne_246504:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalTwo_246504:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalThree_246504:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    timeThreshold_246504:{
                        digits: '输入范围1-60之间的整数',
                        range: '输入范围1-60之间的整数'
                    },
                    timeAlarmNumThreshold_246504: {
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },
                    distanceThreshold_246504: {
                        digits: '输入范围1-100之间的整数',
                        range: '输入范围1-100之间的整数',
                    },
                    distanceAlarmNumThreshold_246504:{
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },

                    processingIntervalOne_246503:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalTwo_246503:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalThree_246503:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    timeThreshold_246503:{
                        digits: '输入范围1-60之间的整数',
                        range: '输入范围1-60之间的整数'
                    },
                    timeAlarmNumThreshold_246503: {
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },
                    distanceThreshold_246503: {
                        digits: '输入范围1-100之间的整数',
                        range: '输入范围1-100之间的整数',
                    },
                    distanceAlarmNumThreshold_246503:{
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },

                    processingIntervalOne_246518:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalTwo_246518:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalThree_246518:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    timeThreshold_246518:{
                        digits: '输入范围1-60之间的整数',
                        range: '输入范围1-60之间的整数'
                    },
                    timeAlarmNumThreshold_246518: {
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },
                    distanceThreshold_246518: {
                        digits: '输入范围1-100之间的整数',
                        range: '输入范围1-100之间的整数',
                    },
                    distanceAlarmNumThreshold_246518:{
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },

                    processingIntervalOne_246506:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalTwo_246506:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalThree_246506:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    timeThreshold_246506:{
                        digits: '输入范围1-60之间的整数',
                        range: '输入范围1-60之间的整数'
                    },
                    timeAlarmNumThreshold_246506: {
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },
                    distanceThreshold_246506: {
                        digits: '输入范围1-100之间的整数',
                        range: '输入范围1-100之间的整数',
                    },
                    distanceAlarmNumThreshold_246506:{
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },

                    processingIntervalOne_246505:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalTwo_246505:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalThree_246505:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    timeThreshold_246505:{
                        digits: '输入范围1-60之间的整数',
                        range: '输入范围1-60之间的整数'
                    },
                    timeAlarmNumThreshold_246505: {
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },
                    distanceThreshold_246505: {
                        digits: '输入范围1-100之间的整数',
                        range: '输入范围1-100之间的整数',
                    },
                    distanceAlarmNumThreshold_246505:{
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },

                    processingIntervalOne_246401:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalTwo_246401:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalThree_246401:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    timeThreshold_246401:{
                        digits: '输入范围1-60之间的整数',
                        range: '输入范围1-60之间的整数'
                    },
                    timeAlarmNumThreshold_246401: {
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },
                    distanceThreshold_246401: {
                        digits: '输入范围1-100之间的整数',
                        range: '输入范围1-100之间的整数',
                    },
                    distanceAlarmNumThreshold_246401:{
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },

                    processingIntervalOne_246403:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalTwo_246403:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalThree_246403:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    timeThreshold_246403:{
                        digits: '输入范围1-60之间的整数',
                        range: '输入范围1-60之间的整数'
                    },
                    timeAlarmNumThreshold_246403: {
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },
                    distanceThreshold_246403: {
                        digits: '输入范围1-100之间的整数',
                        range: '输入范围1-100之间的整数',
                    },
                    distanceAlarmNumThreshold_246403:{
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },

                    processingIntervalOne_246402:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalTwo_246402:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalThree_246402:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    timeThreshold_246402:{
                        digits: '输入范围1-60之间的整数',
                        range: '输入范围1-60之间的整数'
                    },
                    timeAlarmNumThreshold_246402: {
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },
                    distanceThreshold_246402: {
                        digits: '输入范围1-100之间的整数',
                        range: '输入范围1-100之间的整数',
                    },
                    distanceAlarmNumThreshold_246402:{
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },

                    processingIntervalOne_246404:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalTwo_246404:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalThree_246404:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    timeThreshold_246404:{
                        digits: '输入范围1-60之间的整数',
                        range: '输入范围1-60之间的整数'
                    },
                    timeAlarmNumThreshold_246404: {
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },
                    distanceThreshold_246404: {
                        digits: '输入范围1-100之间的整数',
                        range: '输入范围1-100之间的整数',
                    },
                    distanceAlarmNumThreshold_246404:{
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },

                    processingIntervalOne_246405:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalTwo_246405:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalThree_246405:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    timeThreshold_246405:{
                        digits: '输入范围1-60之间的整数',
                        range: '输入范围1-60之间的整数'
                    },
                    timeAlarmNumThreshold_246405: {
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },
                    distanceThreshold_246405: {
                        digits: '输入范围1-100之间的整数',
                        range: '输入范围1-100之间的整数',
                    },
                    distanceAlarmNumThreshold_246405:{
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },

                    processingIntervalOne_246406:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalTwo_246406:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalThree_246406:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    timeThreshold_246406:{
                        digits: '输入范围1-60之间的整数',
                        range: '输入范围1-60之间的整数'
                    },
                    timeAlarmNumThreshold_246406: {
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },
                    distanceThreshold_246406: {
                        digits: '输入范围1-100之间的整数',
                        range: '输入范围1-100之间的整数',
                    },
                    distanceAlarmNumThreshold_246406:{
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },

                    processingIntervalOne_246407:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalTwo_246407:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalThree_246407:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    timeThreshold_246407:{
                        digits: '输入范围1-60之间的整数',
                        range: '输入范围1-60之间的整数'
                    },
                    timeAlarmNumThreshold_246407: {
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },
                    distanceThreshold_246407: {
                        digits: '输入范围1-100之间的整数',
                        range: '输入范围1-100之间的整数',
                    },
                    distanceAlarmNumThreshold_246407:{
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },

                    processingIntervalOne_246408:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalTwo_246408:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalThree_246408:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    timeThreshold_246408:{
                        digits: '输入范围1-60之间的整数',
                        range: '输入范围1-60之间的整数'
                    },
                    timeAlarmNumThreshold_246408: {
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },
                    distanceThreshold_246408: {
                        digits: '输入范围1-100之间的整数',
                        range: '输入范围1-100之间的整数',
                    },
                    distanceAlarmNumThreshold_246408:{
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },

                    processingIntervalOne_246409:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalTwo_246409:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalThree_246409:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    timeThreshold_246409:{
                        digits: '输入范围1-60之间的整数',
                        range: '输入范围1-60之间的整数'
                    },
                    timeAlarmNumThreshold_246409: {
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },
                    distanceThreshold_246409: {
                        digits: '输入范围1-100之间的整数',
                        range: '输入范围1-100之间的整数',
                    },
                    distanceAlarmNumThreshold_246409:{
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },

                    processingIntervalOne_246507:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalTwo_246507:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalThree_246507:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    timeThreshold_246507:{
                        digits: '输入范围1-60之间的整数',
                        range: '输入范围1-60之间的整数'
                    },
                    timeAlarmNumThreshold_246507: {
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },
                    distanceThreshold_246507: {
                        digits: '输入范围1-100之间的整数',
                        range: '输入范围1-100之间的整数',
                    },
                    distanceAlarmNumThreshold_246507:{
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },

                    processingIntervalOne_246508:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalTwo_246508:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalThree_246508:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    timeThreshold_246508:{
                        digits: '输入范围1-60之间的整数',
                        range: '输入范围1-60之间的整数'
                    },
                    timeAlarmNumThreshold_246508: {
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },
                    distanceThreshold_246508: {
                        digits: '输入范围1-100之间的整数',
                        range: '输入范围1-100之间的整数',
                    },
                    distanceAlarmNumThreshold_246508:{
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },

                    processingIntervalOne_246509:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalTwo_246509:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalThree_246509:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    timeThreshold_246509:{
                        digits: '输入范围1-60之间的整数',
                        range: '输入范围1-60之间的整数'
                    },
                    timeAlarmNumThreshold_246509: {
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },
                    distanceThreshold_246509: {
                        digits: '输入范围1-100之间的整数',
                        range: '输入范围1-100之间的整数',
                    },
                    distanceAlarmNumThreshold_246509:{
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },

                    processingIntervalOne_246514:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalTwo_246514:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    processingIntervalThree_246514:{
                        digits: '输入范围30-60之间的整数',
                        range: '输入范围30-60之间的整数'
                    },
                    timeThreshold_246514:{
                        digits: '输入范围1-60之间的整数',
                        range: '输入范围1-60之间的整数'
                    },
                    timeAlarmNumThreshold_246514: {
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
                    },
                    distanceThreshold_246514: {
                        digits: '输入范围1-100之间的整数',
                        range: '输入范围1-100之间的整数',
                    },
                    distanceAlarmNumThreshold_246514:{
                        digits: '输入范围5-100之间的整数',
                        range: '输入范围5-100之间的整数',
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
        // // 一级报警与二级报警状态切换
        // oneTwoLevelAlarmChange: function (target) {
        //     var _this = target.target ? $(this) : target;
        //     var sibVal = _this.closest('.col-md-4').siblings('.col-md-4').find('input[type="radio"]:checked').val();
        //     var levelAlarmInfo = _this.closest('.form-group').next('.levelAlarmInfo');
        //     if (_this.val() == '0' && sibVal == '0') {// 一级报警与二级报警都为关闭状态,隐藏相关参数
        //         levelAlarmInfo.slideUp();
        //         levelAlarmInfo.find('input,select').prop('disabled', true);
        //     } else {
        //         levelAlarmInfo.slideDown();
        //         levelAlarmInfo.find('input,select').prop('disabled', false);
        //     }
        // },
        // // 道路标识识别单选按钮切换切换
        // roadSignRecognitionChange: function (target) {
        //     var _this = target.target ? $(this) : target;
        //     var curVal = _this.val();
        //     var roadSignInfo = _this.closest('.form-group').siblings('.roadSignInfo');
        //     if (curVal == '0') {
        //         roadSignInfo.slideUp();
        //         roadSignInfo.find('input,select').prop('disabled', true);
        //     } else {
        //         roadSignInfo.slideDown();
        //         roadSignInfo.find('input,select').prop('disabled', false);
        //     }
        // },
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
        defineNewJingSetting.init();
        $("input").inputClear();
        // 设置非空校验失败时提示默认文字
        $.extend($.validator.messages, {
            required: "该字段不能为空"
        });
        /**
         * 页面交互
         * */
        // 查看/隐藏更多参数
        $(".hiddenparameter").bind("click", defineNewJingSetting.hiddenparameterFn);
        // 触发状态单选按钮切换切换
        $('.touchStatus').on('change', defineNewJingSetting.touchStatusChange);
        // // 一级报警与二级报警状态切换
        // $('.oneLevelAlarm,.twoLevelAlarm').on('change', defineNewJingSetting.oneTwoLevelAlarmChange);
        // 道路标识识别单选按钮切换
        $('.roadSignRecognition').on('change', defineNewJingSetting.roadSignRecognitionChange);
        // 监听报警速度阈值变化
        $('.speedLimit').on('input propertychange', defineNewJingSetting.speedLimitChange);
        // 监听页签切换,控制本页签下发按钮是否禁用
        $('.nav-tabs li').on('click', defineNewJingSetting.curTabSendDisabled);
        // 滑块选择切换
        defineNewJingSetting.selectSwitch();
        defineNewJingSetting.topSwitch();
        $(".typeName").bind('click',defineNewJingSetting.labelClickFn);
        $(".controlCheck").bind('click',defineNewJingSetting.inputClickFn);
        // $('.hiddenparameter').on('click',defineNewJingSetting.changeShowAndHide);
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
            defineNewJingSetting.paramSend('all');
        });
        $('#curTabSend').on('click', function () {
            defineNewJingSetting.paramSend('curTab');
        });
        $('#doSubmits').on('click', function () {
            defineNewJingSetting.paramSend('save');
        })
    })
})(window, $);