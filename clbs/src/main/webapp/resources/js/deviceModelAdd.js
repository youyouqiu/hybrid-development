(function (window, $) {
    addTerminalModel = {
        doSubmit: function () {
            if (addTerminalModel.validates()) {
                var formData = $('#modelAddForm').serializeArray();
                var sendObj = {};
                for (var i = 0; i < formData.length; i++) {
                    if (formData[i].value !== '') {
                        if (formData[i].name.indexOf('connectionFlag') == -1) { // 是否连接云台
                            sendObj[formData[i].name] = formData[i].value;
                        }
                        if (formData[i].name == 'terminalManufacturers') { // 终端厂商
                            sendObj['terminalManufacturer'] = $("#terminalManufacturers").find("option:selected").text();
                        }
                    }
                }
                var supportPhotoFlag = sendObj['supportPhotoFlag']; // 是否支持拍照
                if (supportPhotoFlag == 0) { // 不支持拍照,将摄像头个数设置为null
                    sendObj['camerasNumber'] = null;
                }
                var supportVideoFlag = sendObj['supportVideoFlag']; // 是否支持视频
                if (supportVideoFlag == 1) { // 支持视频,组装通道号参数
                    var channelItems = $('.channelItem');
                    var channelArr = [];
                    for (var i = 0, len = channelItems.length; i < len; i++) {
                        var item = $(channelItems[i]).find('.form-control');
                        var obj = {};
                        for (j = 0; j < item.length; j++) {
                            var name = $(item[j]).attr('data-name');
                            obj[name] = $(item[j]).val();
                        }
                        obj['connectionFlag'] = $(channelItems[i]).find('.connectionFlag:checked').val();
                        obj['sort'] = i;
                        channelArr.push(obj);
                    }
                    sendObj["deviceChannelSettingInfoList"] = channelArr;
                } else { // 不支持视频,不组装通道号参数,将音视频格式和通道号个数的
                    sendObj['channelNumber'] = null; // 通道号个数
                    sendObj['audioFormat'] = null; // 音视频格式
                }
                console.log('请求参数', sendObj, channelArr);
                var url = "/clbs/m/basicinfo/equipment/device/addTerminalType";
                var value ='';
                for(var key in sendObj){
                    value += sendObj[key]
                }
                var resubmitToken = value.split("").reduce(function (a, b) { a = ((a << 5) - a) + b.charCodeAt(0); return a & a }, 0);
                var param = {"terminalTypeInfo": JSON.stringify(sendObj),resubmitToken};
                json_ajax("POST", url, "json", false, param, addTerminalModel.callBack);
            }
        },
        callBack: function (data) {
            if (data.success) {
                $("#commonWin").modal("hide");
                layer.msg("添加成功！", {move: false});
                modelTable.requestData();
            } else if (data.msg) {
                layer.msg(data.msg);
            }
        },
        validates: function () {
            return $("#modelAddForm").validate({
                rules: {
                    terminalType: {
                        required: true,
                        minlength: 2,
                        maxlength: 30,
                        isRightSensorModel: true,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/m/basicinfo/equipment/device/verifyTerminalType",
                            data: {
                                terminalType: function () {
                                    return $("#terminalType").val();
                                },
                                terminalManufacturer: function () {
                                    return $("#terminalManufacturers").find("option:selected").text();
                                }
                            },
                            dataFilter: function (data, type) {
                                var result = JSON.parse(data);
                                if (result["success"]) {
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                        }
                    }
                },
                messages: {
                    terminalType: {
                        required: terminalTypeError,
                        isRightSensorModel: terminalTypeError,
                        minlength: terminalTypeError,
                        maxlength: terminalTypeError,
                        remote: terminalTypeRepetition
                    }
                }
            }).form();
        },
        // 通道号参数组组页面渲染
        channelGroupInit: function (num) {
            var html = '';
            for (var i = 0; i < num; i++) {
                html += '<div class="channelItem">' +
                    '     <div class="form-group">' +
                    '       <label class="col-md-2 control-label">物理通道号:</label>' +
                    '       <div class="col-md-4">' +
                    '          <select data-name="physicsChannel"' +
                    '           class="form-control physicsChannel">' +
                    '            <option value="1">1</option>' +
                    '            <option value="2">2</option>' +
                    '            <option value="3">3</option>' +
                    '            <option value="4">4</option>' +
                    '            <option value="5">5</option>' +
                    '            <option value="6">6</option>' +
                    '            <option value="7">7</option>' +
                    '            <option value="8">8</option>' +
                    '            <option value="9">9</option>' +
                    '            <option value="10">10</option>' +
                    '            <option value="11">11</option>' +
                    '            <option value="12">12</option>' +
                    '            <option value="13">13</option>' +
                    '            <option value="14">14</option>' +
                    '            <option value="15">15</option>' +
                    '            <option value="16">16</option>' +
                    '            <option value="17">17</option>' +
                    '            <option value="18">18</option>' +
                    '            <option value="19">19</option>' +
                    '            <option value="20">20</option>' +
                    '            <option value="21">21</option>' +
                    '            <option value="22">22</option>' +
                    '            <option value="23">23</option>' +
                    '            <option value="24">24</option>' +
                    '            <option value="25">25</option>' +
                    '            <option value="26">26</option>' +
                    '            <option value="27">27</option>' +
                    '            <option value="28">28</option>' +
                    '            <option value="29">29</option>' +
                    '            <option value="30">30</option>' +
                    '            <option value="31">31</option>' +
                    '            <option value="32">32</option>' +
                    '            <option value="33">33</option>' +
                    '            <option value="34">34</option>' +
                    '            <option value="35">35</option>' +
                    '            <option value="36">36</option>' +
                    '            <option value="37">37</option>' +
                    '         </select>' +
                    '      </div>' +
                    '      <label class="col-md-2 control-label"><label' +
                    '            class="text-danger">*</label>&ensp;逻辑通道号:</label>' +
                    '      <div class="col-md-4">' +
                    '          <select data-name="logicChannel" class="form-control logicChannel">' +
                    '             <option value="1">1</option>' +
                    '             <option value="2">2</option>' +
                    '             <option value="3">3</option>' +
                    '             <option value="4">4</option>' +
                    '             <option value="5">5</option>' +
                    '             <option value="6">6</option>' +
                    '             <option value="7">7</option>' +
                    '             <option value="8">8</option>' +
                    '             <option value="9">9</option>' +
                    '             <option value="10">10</option>' +
                    '             <option value="11">11</option>' +
                    '             <option value="12">12</option>' +
                    '             <option value="13">13</option>' +
                    '             <option value="14">14</option>' +
                    '             <option value="15">15</option>' +
                    '             <option value="16">16</option>' +
                    '             <option value="17">17</option>' +
                    '             <option value="18">18</option>' +
                    '             <option value="19">19</option>' +
                    '             <option value="20">20</option>' +
                    '             <option value="21">21</option>' +
                    '             <option value="22">22</option>' +
                    '             <option value="23">23</option>' +
                    '             <option value="24">24</option>' +
                    '             <option value="25">25</option>' +
                    '             <option value="26">26</option>' +
                    '             <option value="27">27</option>' +
                    '             <option value="28">28</option>' +
                    '             <option value="29">29</option>' +
                    '             <option value="30">30</option>' +
                    '             <option value="31">31</option>' +
                    '             <option value="32">32</option>' +
                    '             <option value="33">33</option>' +
                    '             <option value="36">36</option>' +
                    '             <option value="37">37</option>' +
                    '             <option value="64">64</option>' +
                    '             <option value="65">65</option>' +
                    '         </select>' +
                    '     </div>' +
                    ' </div>' +
                    ' <div class="form-group">' +
                    '     <label class="col-md-2 control-label">通道类型:</label>' +
                    '     <div class="col-md-4">' +
                    '         <select data-name="channelType" placeholder="请选择通道类型"' +
                    '                 class="form-control audiofrequency">' +
                    '             <option selected="selected" value="0">音视频</option>' +
                    '             <option value="1">音频</option>' +
                    '             <option value="2">视频</option>' +
                    '         </select>' +
                    '     </div>' +
                    '     <label class="col-md-2 control-label">码流类型:</label>' +
                    '     <div class="col-md-4">' +
                    '         <select data-name="streamType" class="form-control streamType">' +
                    '             <option selected="selected" value="1">子码流</option>' +
                    '             <option value="0">主码流</option>' +
                    '         </select>' +
                    '     </div>' +
                    ' </div>' +
                    ' <div class="form-group linkPlatform">' +
                    '     <label class="col-md-2 control-label">是否连接云台:</label>' +
                    '     <div class="col-md-4 isconnectplatform">' +
                    '         <label class="radio-inline"><input type="radio" class="connectionFlag" name="connectionFlag' + i + '" value="1"' +
                    '             checked="checked"> 是</label>' +
                    '         <label class="radio-inline"><input class="connectionFlag" name="connectionFlag' + i + '" type="radio"' +
                    '             value="0">' +
                    '             否</label>' +
                    '     </div>' +
                    ' </div>' +
                    '</div>'
            }
            $('.channelGroup').html(html);
            var channelItems = $('.channelItem');
            for (var i = 0, len = channelItems.length; i < len; i++) {
                $(channelItems[i]).find('.physicsChannel').val(i + 1).attr('lastValue', i + 1);
                $(channelItems[i]).find('.logicChannel').val(i + 1).attr('lastValue', i + 1);
                if (i == 33 || i == 34) {
                    $(channelItems[i]).find('.logicChannel').val(i + 3).attr('lastValue', i + 3);
                }
                if (i == 35 || i == 36) {
                    $(channelItems[i]).find('.logicChannel').val(i + 29).attr('lastValue', i + 29);
                }
                if (i == 32 || i == 33 || i == 34) {
                    $(channelItems[i]).find('.audiofrequency').val('1').prop('disabled', true);
                    $(channelItems[i]).find('.linkPlatform').hide();
                    // $(channelItems[i]).find('.audiofrequency').closest('div').nextAll().hide();
                }
                if (i == 35 || i == 36) {
                    $(channelItems[i]).find('.logicChannel').val(i + 29).attr('lastValue', i + 29);
                }
            }
            $(".physicsChannel").on("change", function (e) {
                addTerminalModel.isRepeat('physicsChannel', e.target);
            });
            $(".logicChannel").on("change", function (e) {
                addTerminalModel.isRepeat('logicChannel', e.target);
            });

            $(".audiofrequency").on('change', function () {
                var thisVal = $(this).val();
                var targetDiv = $(this).parents('.form-group').siblings('.linkPlatform');
                if (thisVal == '1') {
                    targetDiv.slideUp();
                    // $(this).parent().nextAll().hide();
                } else {
                    targetDiv.slideDown();
                    // $(this).parent().nextAll().show();
                }
            });
        },
        //校验是否有重复通道号
        isRepeat: function (className, _this) {
            var infoArr = [];
            var lastValue = $(_this).attr('lastValue');
            var infoList = $("." + className);
            var logicChannelflag = false;
            for (var i = 0, len = infoList.length; i < len; i++) {
                if (infoArr.indexOf($(infoList[i]).val()) != -1) {
                    $(_this).val(lastValue).attr('lastValue', lastValue);
                    logicChannelflag = true;
                    layer.msg("请选择未设置的通道号");
                    break;
                } else {
                    $(infoList[i]).attr('lastValue', $(infoList[i]).val());
                    infoArr.push($(infoList[i]).val());
                }
            }

            var curVal = $(_this).val();
            if (!logicChannelflag && className == 'logicChannel') {
                var targetDiv = $(_this).closest('.channelItem');
                if (curVal == '33' || curVal == '36' || curVal == '37') {
                    targetDiv.find('.audiofrequency').val('1').prop('disabled', true).change();
                } else {
                    targetDiv.find('.audiofrequency').val('0').removeAttr('disabled').change();
                }
            }
        },
        //点击显示隐藏信息
        hiddenparameterFn: function () {
            var clickId = $(this).attr('id');
            if (!($("#" + clickId + "-content").is(":hidden"))) {
                $("#" + clickId + "-content").slideUp();
                $("#" + clickId).children("font").text("显示更多");
                $("#" + clickId).children("span").removeAttr("class").addClass("fa fa-chevron-down");
            } else {
                $("#" + clickId + "-content").slideDown();
                $("#" + clickId).children("font").text("隐藏信息");
                $("#" + clickId).children("span").removeAttr("class").addClass("fa fa-chevron-up");
            }
        },
        // 初始化终端厂商列表
        initTerminalManufacturerList: function () {
            var url = "/clbs/m/basicinfo/equipment/device/TerminalManufacturer";
            json_ajax("GET", url, "json", false, null, function (data) {
                if (data.success) {
                    var terminalManufacturer = data.obj.result;
                    var selector = $("#terminalManufacturers");
                    if (terminalManufacturer != null && terminalManufacturer.length > 0) {
                        for (var i = 0; i < terminalManufacturer.length; i++) {
                            selector.append('<option  value="' + i + '">' + terminalManufacturer[i] + '</option>');
                        }
                    }
                    $("#terminalManufacturers").find("option:contains('[f]F3')").attr("selected", true);
                }
            });
        }
    };
    $(function () {
        $('input').inputClear();
        //初始化
        addTerminalModel.channelGroupInit(4);
        // 初始化终端厂商列表
        addTerminalModel.initTerminalManufacturerList();
        // 监听通道号个数下拉框值变化
        $("#channelNumber").on("change", function () {
            var num = $(this).val();
            addTerminalModel.channelGroupInit(num);
        });

        // 监听是否支持拍照单选框值变化
        $(".takingPictures").on("change", function () {
            var curVal = $('input[name="supportPhotoFlag"]:checked').val();
            if (curVal == '1') {
                $(".picturesInfo").show();
            } else {
                $(".picturesInfo").hide();
            }
        });
        // 监听是否支持视频值变化
        $(".supportVideo").on("change", function () {
            var curVal = $('input[name="supportVideoFlag"]:checked').val();
            if (curVal == '1') {
                $(".channelInfo").slideDown();
            } else {
                $(".channelInfo").slideUp();
            }
        });

        // 参数显示隐藏控制
        $(".hiddenparameter").bind("click", addTerminalModel.hiddenparameterFn);

        //提交
        $("#doSubmit").bind("click", addTerminalModel.doSubmit);
    })
})(window, $)