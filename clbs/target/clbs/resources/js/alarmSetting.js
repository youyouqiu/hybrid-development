//# sourceURL=alarmSetting.js
(function ($, window) {
    var $label;
    var driverareaAlarmList = JSON.parse($("#driverareaAlarmList").attr("value"));
    var driverlineAlarmList = JSON.parse($("#driverlineAlarmList").attr("value"));
    var drivertimeAlarmList = JSON.parse($("#drivertimeAlarmList").attr("value"));
    var tempAlarmList = JSON.parse($("#tempAlarmList").attr("value"));
    var humAlarmList = JSON.parse($("#humAlarmList").attr("value"));
    var oilAlarmList = JSON.parse($("#oilAlarmList").attr("value"));
    var ioAlarmList = JSON.parse($("#ioAlarmList").attr("value"));
    var areaAlarmList = JSON.parse($("#areaAlarmList").attr("value"));
    var lineAlarmList = JSON.parse($("#lineAlarmList").attr("value"));
    var pointAlarmList = JSON.parse($("#pointAlarmList").attr("value"));
    var veerAlarmList = JSON.parse($("#veerAlarmList").attr("value"));
    var workHourAlarmList = JSON.parse($("#workHourAlarmList").attr("value"));
    var loadAlarmList = JSON.parse($("#loadAlarmList").attr("value"));
    var tirePressureAlarmList = JSON.parse($("#tirePressureAlarmList").attr("value"));
    var devicePowerAlarmList = JSON.parse($("#devicePowerAlarmList").attr("value"));
    // var levelAlarmList = JSON.parse($("#levelAlarmList").attr("value"));
    var alarmLists = new Array(driverareaAlarmList, driverlineAlarmList, drivertimeAlarmList, tempAlarmList, humAlarmList, oilAlarmList/*, ioAlarmList*/, areaAlarmList, lineAlarmList, pointAlarmList, veerAlarmList/*,levelAlarmList*/, workHourAlarmList, loadAlarmList, tirePressureAlarmList, devicePowerAlarmList);
    var typeId = new Array("20", "21", "22", "65", "66", "68", "72", "73", "119", "124"/*,"68_level"*/, "132", "70", "143", "1881");
    var vpos = new Array("2011", "2111", "2211", "6511", "6611", "6811", "7211", "7311", "11911", "12411"/*,"18177"*/, "13213", "7011", "14300", "18811");
    var typeClass = new Array("driverareaAlarm", "driverlineAlarm", "drivertimeAlarm", "tempalarm", "humidityalarm", "oilalarm", "areaalarm", "linealarm", "pointalarm", "veerAlarmList"/*,"levelAlarm"*/, "workHourAlarmList", "loadAlarmList", "tirePressureAlarmList", "devicePowerAlarmList");



    isRead = false;
    var _timeout = null;

    alarmSettings = {
        init: function () {
            $("[data-toggle='tooltip']").tooltip();
            //车辆list
            var referVehicleList = JSON.parse($("#referVehicleList").attr("value"));
            var referList = new Array();
            // 初始化车辆数据
            var dataList = {value: []};
            if (referVehicleList != null && referVehicleList.length > 0) {
                var vehicleId = $("#vehicleId").val();
                for (var i = 0; i < referVehicleList.length; i++) {
                    //删除重复车辆
                    if (referVehicleList[i].vehicleId == vehicleId) {
                        referVehicleList.splice(i, 1);
                    }
                    var obj = {};
                    //处理参考车牌只存在一条数据时防止浏览器抛出错误信息
                    if (referVehicleList[i] == undefined) {
                        dataList.value.push(obj);
                    } else {
                        obj.id = referVehicleList[i].vehicleId;
                        obj.name = referVehicleList[i].brand;
                        dataList.value.push(obj);
                    }
                }
            }
            $("#brands").bsSuggest({
                indexId: 1, //data.value 的第几个数据，作为input输入框的内容
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
                    url: '/clbs/a/alarmSetting/getAlarmParameter_' + vehicleId + '.gsp',
                    async: false,
                    dataType: 'json',
                    success: function (data) {
                        alarmSettings.setParamVal(data, 'alarmParameterId', 'parameterValue');
                    },
                    error: function () {
                        layer.msg(systemError, {move: false});
                    }
                });
            }).on('onUnsetSelectValue', function () {
            });
            $(".modal-body").addClass("modal-body-overflow");
            $(".modal-body").css({"height": "auto", "max-height": ($(window).height() - 194) + "px"});

            // 监听弹窗关闭
            $('#commonWin').on('hide.bs.modal', function () {
                if (_timeout) clearTimeout(_timeout);
            });

            //初始化日历插件
            laydate.render({
                elem: '.runDateTime'
                , type: 'time'
                , range: '--'
                , format: 'HH:mm'
                , noSecond: true
                , trigger: 'click'
            });
            laydate.render({
                elem: '.dateTime2'
                , type: 'time'
                , range: '--'
                , format: 'HH:mm'
                , noSecond: true
                , trigger: 'click'
            });
            $(".dateTime2").val($("#dateTimeValue2").val());
            laydate.render({
                elem: '.dateTime3'
                , type: 'date'
                , range: '--'
                , format: 'yyyy-MM-dd'
                , trigger: 'click'
            });
            $(".dateTime3").val($("#dateTimeValue3").val());
            laydate.render({
                elem: '.dateTime14'
                , type: 'time'
                , range: '--'
                , noSecond: true
                , format: 'HH:mm'
                , trigger: 'click'
            });
            $(".dateTime14").val($("#dateTimeValue14").val());
            laydate.render({
                elem: '.dateTime15'
                , type: 'date'
                , range: '--'
                , format: 'yyyy-MM-dd'
                , trigger: 'click'
            });
            $(".dateTime15").val($("#dateTimeValue15").val());
//			------------ 勿删  ------------
//			laydate.render({
//				elem: '.dateTime4'
//				,type: 'time'
//				,range: '--'
//				,format: 'HH:mm'
//				,trigger: 'click'
//			});
//			$(".dateTime4").val($("#dateTimeValue4").val());
//			laydate.render({
//				elem: '.dateTime5'
//				,type: 'date'
//				,range: '--'
//				,format: 'yyyy-MM-dd'
//				,trigger: 'click'
//			});
//			$(".dateTime5").val($("#dateTimeValue5").val());
//
            if($("#alarmCollision").val()) {
                $("#alarmCollision").val( $("#alarmCollision").val() / 10)
            }

            if($("#alarmAccelerate").val()) {
                $("#alarmAccelerate").val($("#alarmAccelerate").val() / 10)
            }

            if($("#alarmSpeed").val()) {
                $("#alarmSpeed").val($("#alarmSpeed").val() / 10)
            }

        },
        // 设置参数值(参考对象,设置默认值)
        setParamVal: function (data, checkIdName, paramValName, pushFlag) {
            var result = pushFlag ? data : data.obj;
            if (result.length != 0) {
                //清除当前车辆报警参数
                var selectinfoList = $("input[name='selectinfo']");
                if (pushFlag) {// 读取终端参数时,只清空终端报警参数
                    selectinfoList = $(".isDevice input[name='selectinfo']");
                }

                //清空输入框值(除F3高精度页签);
                // var dataList = ['#home1', '#profile1', '#profile2', '#profile4', '#profile5'];
                var dataList = ['#home1', '#profile1', '#profile2', '#profile4',];
                for (var i = 0; i < dataList.length; i++) {
                    var tabId = dataList[i];
                    $(tabId).find("input[name='parameterValue']").val('');
                }
                $('#roadNetSpeedLimit').prop("checked", false);
                //读取参考车辆报警参数
                var alarm = result;
                for (var i = 0; i < alarm.length; i++) {
                    var checkId = alarm[i][checkIdName];
                    var paramValue = alarm[i][paramValName] ? alarm[i][paramValName] : '';
                    var ignore = alarm[i].ignore;
                    if (checkId == "f1de45d2-4ebc-11e9-a899-000c2984880c" && paramValue == "1") {
                        // 路网报警
                        $('#roadNetSpeedLimit').prop("checked", true);
                    }
                    if (checkId == '1212e170-268d-11e8-b467-0ed5f89f718b') {// 异动报警禁行时段
                        paramValue = alarm[i].parameterValue;
                    }
                    $("#" + checkId).parent().parent().parent().parent().find("input[name='parameterValue']").val(paramValue);
                    if (!pushFlag) {
                        $("#" + checkId).val(alarm[i].alarmPush);
                    } else if (pushFlag && alarm[i].alarmPush == '-1') {
                        $("#" + checkId).val(alarm[i].alarmPush);
                    }

                    if (checkId == "5b9bpa75-bc26-11e6-a4a6-cec0c932ce01") {
                        //路线偏离下拉
                        $(".lineDeviateSelect").val(alarm[i][paramValName])
                    }
                    if (checkId == "5b9b4d5a-bc26-11e6-a4a6-cec0c932ce01") {
                        //异动报警下拉
                        $(".exceptionMoveSelect").val(alarm[i][paramValName])
                    }
                    if (checkId == "5b9bpa76-bc26-11e6-a4a6-cec0c932ce01") {
                        //超速报警下拉
                        $(".overSpeedSelect").val(alarm[i][paramValName])
                    }

                    if(checkId == 'c6e3e5ee-ce76-40e2-a368-33a5b298c02c') {
                        //急加速
                        $("#alarmAccelerate").val(alarm[i][paramValName] / 10);
                    }

                    if(checkId == '48c2f1ae-6e85-40b2-a2f9-c50ea9c21b90') {
                        //急减速
                        $("#alarmSpeed").val(alarm[i][paramValName] / 10);
                    }

                    if(checkId == 'a21f3b28-98c0-44e3-817e-1502fed96ad7') {
                        //碰撞报警
                        $("#alarmCollision").val(alarm[i][paramValName] / 10);
                    }

                    if(checkId == '60cc2fcf-42b6-45d1-83ab-26ae33b42dbe') {
                        $("#alarmElectric").val(alarm[i][paramValName] / 10);
                    }



                    if ($.inArray(alarm[i].pos, vpos) != -1) {
                        var a = $.inArray(alarm[i].pos, vpos);
                        if (!pushFlag) {
                            $("." + typeClass[a]).find(".selectvalue").val(alarm[i].alarmPush);
                        } else if (pushFlag && alarm[i].alarmPush == '-1') {
                            $("." + typeClass[a]).find(".selectvalue").val(alarm[i].alarmPush);
                        }
                    }
                    if (ignore == 1) {
                        if ($.inArray(alarm[i].pos, vpos) != -1) {
                            $($("." + typeClass[a]).find(".ignore")).prop("checked", true);
                        } else {
                            $($("#" + checkId).parent().parent().parent().parent().find(".ignore")).prop("checked", true);
                        }
                    }
                }
                //选中下拉框的值
                $(".exceptionMoveSelect").find("option[value = '" + $("#exceptionMoveSelect").val() + "']").attr("selected", "selected");
                $(".overSpeedSelect").find("option[value = '" + $("#overSpeedSelect").val() + "']").attr("selected", "selected");
                alarmSettings.showRoadNetSpeedLimit();
                alarmSettings.selectPosition();
            }
        },
        //提交
        doSubmit: function () {
            $('#settingForm input[type="text"]').blur();
            //F3高精度表单校验
            alarmSettings.handleFormVerify();
            //组装下拉框选择值至input框
            alarmSettings.selectSetValue();

            //校验标识
            var cFlag = true;
            $("#settingForm").find("label[class='error']").each(function (index) {
                if ($(this).css("display") != "none") {
                    cFlag = false;
                }
            });
            if (cFlag) {
                layer.load(2);
                var checkedParams = [];
                //遍历所有选的selectinfo的值
                var saveAlarmPush = null;
                $("input[name='selectinfo']").each(function (index, item) {
                    var checkedId = $(this).attr("id");
                    var paramValue;
                    var box = $(this).parent().parent().parent().parent().find("input[name='parameterValue']");
                    if (box.length == 0) {
                        box = $(this).parent().parent().parent().parent().find("select[name='parameterValSel']");
                    }
                    if (box.length > 1) {
                        paramValue = $(box[1]).val();
                    } else {
                        var val = $(box).val();
                        var id = $(box).attr('id');

                        if(id == 'alarmAccelerate' || id == 'alarmSpeed' || id == 'alarmCollision' || id == 'alarmElectric') {
                            if(val){
                                val *= 10
                            }
                        }
                        paramValue = val;
                    }
                    var alarmPush = $(this).val();
                    if ($(this).closest('.leftselectbutton').attr("hidden")) {
                        if (saveAlarmPush) alarmPush = saveAlarmPush
                    } else {
                        saveAlarmPush = alarmPush;
                    }
                    var checked = $($(this).parent().parent().parent().parent().find(".ignore")).prop("checked");
                    var ignore = 0;
                    if (checked) {
                        ignore = 1;
                    }
                    var pos = $(this).parent().parent().parent().parent().find(".typeName").attr("id");
                    var paramCode = $(this).parent().next().val();
                    if ($.inArray(pos, typeId) != -1) {
                        var index = $.inArray(pos, typeId);
                        for (var i = 0; i < alarmLists[index].length; i++) {
                            var obj = {};
                            obj.alarmParameterId = alarmLists[index][i].id;
                            obj.parameterValue = paramValue;
                            obj.vehicleId = $("#vehicleId").attr("value");
                            obj.alarmPush = alarmPush; //取button的值
                            obj.pos = alarmLists[index][i].pos;
                            obj.paramCode = paramCode;
                            obj.ignore = ignore;
                            checkedParams.push(obj);
                        }
                    } else {
                        var obj = {};
                        obj.alarmParameterId = checkedId;
                        obj.parameterValue = paramValue;
                        obj.vehicleId = $("#vehicleId").attr("value");
                        obj.alarmPush = alarmPush; //取button的值
                        obj.pos = pos;
                        obj.paramCode = paramCode;
                        obj.ignore = ignore;
                        checkedParams.push(obj);
                    }
                });
                var checkedParams = JSON.stringify(checkedParams);
                var deviceType = $("input[name='deviceCheck']:checked").val();
                $("#checkedParams").val(checkedParams);
                $("#deviceType").val(deviceType);

                addHashCode1($("#settingForm"));
                $("#settingForm").ajaxSubmit(function (data) {
                    layer.closeAll();
                    var data = $.parseJSON(data);
                    if (data.success) {
                        $("#commonWin").modal("hide");
                        var checkedList = new Array();
                        var settingUrl = settingMoreUrl.replace("{id}.gsp", checkedList.toString() + ".gsp?deviceType=" + deviceType);
                        $("#settingMoreBtn").attr("href", settingUrl);
                        layer.msg("设置成功！", {move: false});
                        myTable.refresh();
                    } else {
                        if (data.msg != null) {
                            layer.msg(data.msg, {move: false});
                        }
                    }
                });
            } else {
                layer.msg("请填写正确的参数", {move: false});
            }
        },
        getHighPrecisionId: function () {

        },
        leabelClickFn: function () {
            if ($("#TabCarBox3q").hasClass('active')) {
                var left = $(this).parent().parent().parent().find(".selectbutton").css("left");
                if (left == '103px') {
                    $(this).parent().parent().parent().find(".selectbutton").animate({left: "9px"}, "fast");
                    $(this).parent().parent().parent().find(".selectvalue").val(0);
                } else if (left == '55px') {
                    $(this).parent().parent().parent().find(".selectbutton").animate({left: "103px"}, "fast");
                    $(this).parent().parent().parent().find(".selectvalue").val(2);
                } else {
                    $(this).parent().parent().parent().find(".selectbutton").animate({left: "55px"}, "fast");
                    $(this).parent().parent().parent().find(".selectvalue").val(1);
                }
            } else {
                var left = $(this).parent().parent().parent().find(".selectbutton").css("left");
                if (left == "103px") {
                    $(this).parent().parent().parent().find(".selectbutton").animate({left: "152px"}, "fast");
                    $(this).parent().parent().parent().find(".selectvalue").val(2);
                } else if (left == "152px") {
                    $(this).parent().parent().parent().find(".selectbutton").animate({left: "9px"}, "fast");
                    $(this).parent().parent().parent().find(".selectvalue").val(-1);
                } else if (left == "9px") {
                    $(this).parent().parent().parent().find(".selectbutton").animate({left: "55px"}, "fast");
                    $(this).parent().parent().parent().find(".selectvalue").val(0);
                } else {
                    $(this).parent().parent().parent().find(".selectbutton").animate({left: "103px"}, "fast");
                    $(this).parent().parent().parent().find(".selectvalue").val(1);
                }
            }
        },
        leabelClickFn2: function (tt) {
            var left = $("#" + tt).parent().parent().parent().find(".selectbutton").css("left");
            if (left == "103px") {
                $("#" + tt).parent().parent().parent().find(".selectbutton").animate({left: "152px"}, "fast");
                $("#" + tt).parent().parent().parent().find(".selectvalue").val(2);
            } else if (left == "152px") {
                $("#" + tt).parent().parent().parent().find(".selectbutton").animate({left: "9px"}, "fast");
                $("#" + tt).parent().parent().parent().find(".selectvalue").val(-1);
            } else if (left == "9px") {
                $("#" + tt).parent().parent().parent().find(".selectbutton").animate({left: "55px"}, "fast");
                $("#" + tt).parent().parent().parent().find(".selectvalue").val(0);
            } else {
                $("#" + tt).parent().parent().parent().find(".selectbutton").animate({left: "103px"}, "fast");
                $("#" + tt).parent().parent().parent().find(".selectvalue").val(1);
            }
        },
        //添加温度报警、湿度报警、加漏油报警、IO报警、工时报警
        addinfo: function () {
            var aPush = new Array(
                driverareaAlarmList[0].alarmPush,
                driverlineAlarmList[0].alarmPush,
                drivertimeAlarmList[0].alarmPush,
                tempAlarmList[0].alarmPush,
                humAlarmList[0].alarmPush,
                oilAlarmList[0].alarmPush,
                areaAlarmList[0].alarmPush,
                lineAlarmList[0].alarmPush,
                pointAlarmList[0].alarmPush,
                veerAlarmList[0].alarmPush,
                workHourAlarmList[0].alarmPush,
                loadAlarmList[0].alarmPush,
                tirePressureAlarmList[0].alarmPush,
                devicePowerAlarmList[0].alarmPush
            );
            var ignore = new Array(
                driverareaAlarmList[0].ignore,
                driverlineAlarmList[0].ignore,
                drivertimeAlarmList[0].ignore
            );
            var typeName = new Array("进出区域", "进出线路", "路段行驶时间不足/过长", "温度报警", "湿度报警", "加漏油报警", "进出区域", "进出线路", "关键点报警", "反转报警", "工时报警", "载重报警", "胎压报警", "设备电量报警");
            var alist = [];
            for (var i = 0; i < typeName.length; i++) {
                var a = "<li class='clearfix'>"
                    + "<div class='form-group'>"
                    + "<div class='col-md-2' >"
                    + "<div>";
                if (i < 3) {
                    a += "<div class='leftselectbutton leftselectbutton2 " + typeClass[i] + "'>"
                        + "<input name='selectinfo' class='selectvalue' hidden='true' value=" + aPush[i] + " id='0'/>"
                        + "<span class='selectbutton' style='width:20px;height:20px;position:absolute;left:9px;top:7px;'></span>"
                        + "<span class='button0 button0flag'></span>"
                        + "<span class='button1 button1flag'></span>"
                        + "<span class='button2 button2flag'></span>"
                        + "<span class='button3 button3flag'></span>"
                    // 设备电量报警
                } else if (i === 13) {
                    a = "<li class='clearfix'>"
                        + "<div class='form-group'>"
                        + "<div class='col-md-1' style='margin-right: 40px;' >"
                        + "<div>";
                    a += "<div class='F3leftselectbutton " + typeClass[i] + "'>"
                        + "<input name='selectinfo' class='selectvalue' hidden='true' value=" + aPush[i] + " id='60cc2fcf-42b6-45d1-83ab-26ae33b42dbe'/>"
                        + "<span class='selectbutton' style='width:20px;height:20px;position:absolute;left:9px;top:7px;'></span>"
                        + "<span class='button0'></span>"
                        + "<span class='button1'></span>"
                        + "<span class='button2'></span>";
                } else {
                    a += "<div class='leftselectbutton " + typeClass[i] + "'>"
                        + "<input name='selectinfo' class='selectvalue' hidden='true' value=" + aPush[i] + " id='0'/>"
                        + "<span class='selectbutton' style='width:20px;height:20px;position:absolute;left:9px;top:7px;'></span>"
                        + "<span class='button0'></span>"
                        + "<span class='button1'></span>"
                        + "<span class='button2'></span>"
                        + "<span class='button3'></span>";
                }

                a += "</div>"
                    + "</div>"
                    + "<div></div>"
                    + "</div>";
                if (typeName[i] == '路段行驶时间不足/过长') {
                    a += "<div class='col-md-3  alarmSet'>";
                } else if (typeName[i] == '设备电量报警') {
                    a += "<div class='col-md-2  alarmSet' style='margin-right: 20px;'>";
                } else {
                    a += "<div class='col-md-2  alarmSet'>";
                }
                if (typeName[i] == '设备电量报警') {
                    var paramvalue = '';
                    for (let j = 0; j < devicePowerAlarmList.length; j++) {
                        const element = devicePowerAlarmList[j];
                        if (element.name !== '设备电量报警') {
                            continue
                        }
                        paramvalue = element.parameterValue;
                        break;
                    }
                    a += "<div  class='checkbox-outline'>"
                        + "<label  class='typeName' readonly style='text-decoration: none;cursor:pointer;' id=" + typeId[i] + ">" + typeName[i] + "</label>	"
                        + "</div>"
                        + "</div>"
                        + "<div class='col-md-8 alarmLeftPadding'>"
                        + "<div class='checkbox-outline'>"
                        + "<div class='checkbox-outline'>"
                        + "<div class='col-md-4 alarmLeftPadding'>"
                        + "<input type='text' id='alarmElectric' name='parameterValue' value='" + paramvalue + "' placeholder='请输入参数值' class='form-control'/>"
                        + "<label  for='alarmElectric' class='error' style='display: none;'>" + "请输入0-100范围的正整数" + "</label>	"
                        + "</div>"
                        + "</div>"
                        + "<label  class='col-md-6 alarmLeftPadding' readonly style='text-decoration: none;padding-top:7px;'>" + "电量报警阈值，单位：%" + "</label>	"
                        + "</div>"

                        + "</div>"
                        + "</div>"
                        + "</li>";
                } else {
                    a += "<div  class='checkbox-outline'>"
                        + "<label  class='typeName' onclick='alarmSettings.leabelClickFn2(this.id);' readonly style='text-decoration: none;cursor:pointer;' id=" + typeId[i] + ">" + typeName[i] + "</label>	"
                        + "</div>"
                        + "</div>"
                        + "<div class='col-md-8 alarmLeftPadding'>"
                        + "</div>"
                        + "</div>"
                        + "</li>";
                }
                alist.push(a);
            }
            $("#profile1 ul").append(alist[0], alist[1], alist[2]);
            $("#profile4 ul").find("li").eq("0").before(alist[3]);
            $("#profile4 ul").find("li").eq("1").before(alist[4]);
            $("#profile5 ul").find("li").eq("0").before(alist[6]);
            $("#profile5 ul").find("li").eq("1").before(alist[7]);
            $("#profile5 ul").find("li").eq("2").before(alist[8]);
            $("#profile4q ul").find("li").eq("0").before(alist[13]);
            $("#profile4 ul").append(alist[5]);
            $("#profile4 ul").append(alist[12]);
            $("#profile4 ul").append(alist[9]);
            $("#profile4 ul").append(alist[10]);
            $("#profile4 ul").append(alist[11]);
        },
        selectSwitch: function () {
            $(".leftselectbutton span").on("click", function () {
                var leftbutton = $(this).siblings(".selectbutton").css("left");
                var left = $(this).css("left");
                var classname = $(this).attr("class");
                if (classname.indexOf("button1") > -1) {
                    $(this).siblings("input.selectvalue").val("0");
                } else if (classname.indexOf("button2") > -1) {
                    $(this).siblings("input.selectvalue").val("1");
                } else if (classname.indexOf("button0") > -1) {
                    $(this).siblings("input.selectvalue").val("-1");
                } else {
                    $(this).siblings("input.selectvalue").val("2");
                }
                $(this).siblings(".selectbutton").animate(
                    {left: left}, "fast")
            });

            //F3高精度报警
            $(".F3leftselectbutton span").on("click", function () {
                var left = $(this).css("left");
                var classname = $(this).attr("class");
                if (classname.indexOf("button0") > -1) {
                    $(this).parent().find('.selectvalue').val('0')
                } else if (classname.indexOf('button1') > -1) {
                    $(this).parent().find('.selectvalue').val('1')
                } else {
                    $(this).parent().find('.selectvalue').val('2')
                }
                $(this).siblings(".selectbutton").animate(
                    {left: left}, "fast")
            });
        },
        topswitch: function () {
            // 终端报警页签
            var deviceAlarmList = ["#home1", "#profile1", "#profile2", "#profile3"];
            for (var i = 0; i < deviceAlarmList.length; i++) {
                var $this = deviceAlarmList[i];
                $($this).find(".shielding").on("click", function () {
                    $(this).parent().parent().find(".selectbutton").animate({left: "9px"}, "fast");
                    $(this).parent().parent().find(".selectvalue").val(-1);
                });
                $($this).find(".noneset").on("click", function () {
                    $(this).parent().parent().find(".selectbutton").animate({left: "55px"}, "fast");
                    $(this).parent().parent().find(".selectvalue").val(0);
                });
                $($this).find(".partset").on("click", function () {
                    $(this).parent().parent().find(".selectbutton").animate({left: "103px"}, "fast");
                    $(this).parent().parent().find(".selectvalue").val(1);
                });
                $($this).find(".wholeset").on("click", function () {
                    $(this).parent().parent().find(".selectbutton").animate({left: "152px"}, "fast");
                    $(this).parent().parent().find(".selectvalue").val(2);
                });
            }

            // 平台报警页签
            var platformAlarmList = ["#profile4", "#profile5", "#IOAlarmProfile1", "#IOAlarmProfile2", "#IOAlarmProfile3"];
            for (var j = 0; j < platformAlarmList.length; j++) {
                var $thisPlatform = platformAlarmList[j];
                $($thisPlatform).find(".shielding").on("click", function () {
                    $(this).parent().parent().find(".selectbutton").animate({left: "9px"}, "fast");
                    $(this).parent().parent().find(".selectvalue").val(-1);
                });
                $($thisPlatform).find(".noneset").on("click", function () {
                    $(this).parent().parent().find(".selectbutton").animate({left: "55px"}, "fast");
                    $(this).parent().parent().find(".selectvalue").val(0);
                });
                $($thisPlatform).find(".partset").on("click", function () {
                    $(this).parent().parent().find(".selectbutton").animate({left: "103px"}, "fast");
                    $(this).parent().parent().find(".selectvalue").val(1);
                });
                $($thisPlatform).find(".wholeset").on("click", function () {
                    $(this).parent().parent().find(".selectbutton").animate({left: "152px"}, "fast");
                    $(this).parent().parent().find(".selectvalue").val(2);
                });
            }

            //F3高精度报警
            $("#profile4q").find('.noneset').click(function () {
                $(this).parent().parent().find(".selectbutton").animate({left: "9px"}, "fast");
                $(this).parent().parent().find(".selectvalue").val(0);
            });
            $("#profile4q").find('.partset').click(function () {
                $(this).parent().parent().find(".selectbutton").animate({left: "55px"}, "fast");
                $(this).parent().parent().find(".selectvalue").val(1);
            });
            $("#profile4q").find('.wholeset').click(function () {
                $(this).parent().parent().find(".selectbutton").animate({left: "103px"}, "fast");
                $(this).parent().parent().find(".selectvalue").val(2);
            });
        },
        selectPosition: function () {
            var dataList = ['#home1', '#profile1', '#profile2', '#profile3', '#profile4', '#IOAlarmBox', '#profile5'];
            for (var i = 0; i < dataList.length; i++) {
                var tabId = dataList[i];
                $(tabId + ' .selectvalue').each(function () {
                    var selectvalueno = $(this).val();
                    selectvalueno = parseInt(selectvalueno);
                    if (parseInt(selectvalueno) < 0) {
                        $(this).parent().parent().find(".selectbutton").css("left", "9px");
                    } else if (parseInt(selectvalueno) == 0) {
                        $(this).parent().parent().find(".selectbutton").css("left", "55px");
                    } else if (parseInt(selectvalueno) == 1) {
                        $(this).parent().parent().find(".selectbutton").css("left", "103px");
                    } else {
                        $(this).parent().parent().find(".selectbutton").css("left", "152px");
                    }
                });
            }
            $("#profile4q .selectvalue").each(function () {
                var selectVal = $(this).val();
                selectVal = parseInt(selectVal);
                if (parseInt(selectVal) == 0) {
                    $(this).parent().parent().find(".selectbutton").css("left", "9px");
                } else if (parseInt(selectVal) == 1) {
                    $(this).parent().parent().find(".selectbutton").css("left", "55px");
                } else {
                    $(this).parent().parent().find(".selectbutton").css("left", "103px");
                }
            });
        },

        ignore: function () {
            //屏蔽按钮
            $(".ignore").each(function () {
                var ignore = $(this).val();
                if (ignore == 1) {
                    $(this).prop("checked", true);
                } else {
                    $(this).prop("checked", false);
                }
            })
        },
        //设定input min 和 max 值
        setminmax: function () {
            var idno = [{"id": "#13", "min": "1", "max": "255"},
                {"id": "#14", "min": "1", "max": "65535"},
                {"id": "#29", "min": "1", "max": "1020", "min2": "0", "max2": "79"},
                {"id": "#30", "min": "1", "max": "90"},
                {"id": "#1", "min": "1", "max": "255", "min2": "0", "max2": "79"},
                {"id": "#2", "min": "1", "max": "86400", "min2": "1", "max2": "21600"},
                {"id": "#18", "min": "1", "max": "86400"},
                {"id": "#19", "min": "1", "max": "86400"},
                {"id": "#28", "min": "1", "max": "1000"},
                {"id": "#67", "min": "1", "max": "255"},
                {"id": "#78", "min": "1", "max": "86400"},
                {"id": "#150", "min": "1", "max": "1440", "min2": "10", "max2": "160"},
                {"id": "#151", "min": "1", "max": "1440"},
                {"id": "#158", "min": "1", "max": "1020", "min2": "0", "max2": "79"},
            ]
            for (i = 0; i < idno.length; i++) {
                $(idno[i].id).parent().parent().next().find("input").eq("0").attr("min", idno[i].min);
                $(idno[i].id).parent().parent().next().find("input").eq("0").attr("max", idno[i].max);
                if (i == 2 || i == 4 || i == 5 || i == 13) {
                    $(idno[i].id).parent().parent().parent().parent().next().find("input").eq("1").attr({
                        "min": idno[i].min2,
                        "max": idno[i].max2
                    });
                }
                if (i == 11 || i == 13) {
                    $(idno[i].id).parent().parent().parent().parent().next().find("input").eq("2").attr({
                        "min": idno[i].min2,
                        "max": idno[i].max2
                    });
                }
            }
        },
        //设定input id 和 name 值
        setidname: function () {
            var idname = [{"id": "#13", "idid": "parameterValue1", "name": "parameterValue"},
                {"id": "#14", "idid": "parameterValue2", "name": "parameterValue"},
                {
                    "id": "#29",
                    "idid": "parameterValue3",
                    "name": "parameterValue",
                    "id2": "parameterValue4",
                    "name2": "parameterValue"
                },
                {"id": "#30", "idid": "parameterValue5", "name": "parameterValue"},
                {
                    "id": "#1",
                    "idid": "parameterValue6",
                    "name": "parameterValue",
                    "id2": "parameterValue7",
                    "name2": "parameterValue"
                },
                {
                    "id": "#2",
                    "idid": "parameterValue8",
                    "name": "parameterValue",
                    "id2": "parameterValue9",
                    "name2": "parameterValue"
                },
                {"id": "#18", "idid": "parameterValue10", "name": "parameterValue"},
                {"id": "#19", "idid": "parameterValue11", "name": "parameterValue"},
                {"id": "#28", "idid": "parameterValue12", "name": "parameterValue"},
                {"id": "#67", "idid": "parameterValue13", "name": "parameterValue"},
                {"id": "#78", "idid": "parameterValue15", "name": "parameterValue"},
                {
                    "id": "#150",
                    "idid": "parameterValue16",
                    "name": "parameterValue",
                    "id2": "parameterValue17",
                    "name2": "parameterValue"
                },
                {"id": "#151", "idid": "parameterValue18", "name": "parameterValue"},
                {
                    "id": "#158",
                    "idid": "parameterValue18",
                    "name": "parameterValue",
                    "id2": "parameterValue19",
                    "name2": "parameterValue"
                },
            ]
            for (i = 0; i < idname.length; i++) {
                $(idname[i].id).parent().parent().next().find("input").eq("0").attr({
                    "id": idname[i].idid,
                    "name": idname[i].name
                });
                if (i == 2 || i == 4 || i == 5 || i == 13) {
                    $(idname[i].id).parent().parent().parent().parent().next().find("input").eq("1").attr({
                        "id": idname[i].id2,
                        "name": idname[i].name2
                    });
                }
                if (i == 11) {
                    $(idname[i].id).parent().parent().parent().parent().next().find("input").eq("2").attr({
                        "id": idname[i].id2,
                        "name": idname[i].name2
                    });
                }
            }
        },
        //添加验证的方法
        addrangemethod: function () {
            /*var minno=$(this).attr("minno");
             var maxno=$(this).attr("maxno");
             var value=$(this).val();
             if(value>maxno||value<minno){
             $(this).after("<laber class='error'>请输入"+minno+"到"+maxno+"的值<laber>");

             }else{
             $(this).next().remove();
             }*/
            $(this).validate({
                errorPlacement: function (error, element) {
                    // Append error within linked label
                    $(element).after().html(error);
                },
                errorElement: "span",
                rules: {
                    max: 200,
                    min: 0
                },
                messages: {
                    max: $.validator.format("请输入不大于 {0} 的数值"),
                    min: $.validator.format("请输入不小于 {0} 的数值")
                }
            })

        },
        //下拉框标准改变事件(异动报警、超速报警)
        selectSetValue: function () {
            $("#exceptionMoveSelect").val($(".exceptionMoveSelect").val());
            $("#overSpeedSelect").val($(".overSpeedSelect").val());
            $("#lineDeviateSelect").val($(".lineDeviateSelect").val());
        },
        //显示更多
        showTankOrSensorInfoFn: function () {
            var clickId = $(this).context.id;
            var cli = $("#" + clickId).parents(".clearfix");
            if (!(cli.next().is(":hidden"))) {
                cli.next().slideUp();
                cli.next().next().slideUp();
                // cli.next().next().next().slideUp();
                // cli.next().next().next().next().slideUp();
                // cli.next().next().next().next().next().slideUp();
                // cli.next().next().next().next().next().next().slideUp();
                // cli.next().next().next().next().next().next().next().slideUp();
                $("#" + clickId).children("font").text("显示更多");
                $("#" + clickId).children("span").removeAttr("class").addClass("fa fa-chevron-down");
            } else {
                cli.next().slideDown();
                cli.next().next().slideDown();
                // cli.next().next().next().slideDown();
                // cli.next().next().next().next().slideDown();
                // cli.next().next().next().next().next().slideDown();
                // cli.next().next().next().next().next().next().slideDown();
                // cli.next().next().next().next().next().next().next().slideDown();
                $("#" + clickId).children("font").text("隐藏参数");
                $("#" + clickId).children("span").removeAttr("class").addClass("fa fa-chevron-up");
            }
        },
        showRoadNetSpeedLimit: function () {
            var isChecked = $('#roadNetSpeedLimit').prop('checked');
            var alarmList = $("#roadNetSpeedLimit").parents(".clearfix").nextAll();
            for (var i = 1; i < 7; i++) {
                if (isChecked) {
                    $("#isRoadNetSpeedLimit").val(1);
                    if (i < 6) {
                        $(alarmList[i]).show();
                    } else {
                        $(alarmList[i]).hide();
                    }
                } else {
                    $("#isRoadNetSpeedLimit").val(0);
                    if (i < 6) {
                        $(alarmList[i]).hide();
                    } else {
                        $(alarmList[i]).show();
                    }
                }
            }
        },
        getIOAlarmStateTxt: function (node) {
            var val = $(node).val();
            var index = node.selectedIndex;
            //1号位的val是高电平的值  2号位是高电平的反值
            if (index == 1) {
                if (val == 1) {
                    //如果1号位的val是1   说明1号位是高电平
                    $(node).parent().next().find('input').val('高电平');
                } else {
                    $(node).parent().next().find('input').val('低电平');
                }
            } else if (index == 2) {
                val = (val == 1 ? 2 : 1);
                if (val == 1) {
                    $(node).parent().next().find('input').val('低电平');
                } else {
                    $(node).parent().next().find('input').val('高电平');
                }
            } else {
                $(node).parent().next().find('input').val('');
            }
        },
        // 获取默认值
        getDefaultParam: function () {
            var param = {"deviceType": $('input[name="deviceCheck"]:checked').val()};
            var url = "/clbs/a/alarmSetting/resetDefaultHighPrecisionAlarm";
            // if ($("#profile4q").hasClass('active')) {
            json_ajax("POST", url, "json", false, param, function (data) {
                alarmSettings.setHighPrecisionVal(data, 'id', 'defaultValue');
            });
            // } else {
            var newurl = "/clbs/a/alarmSetting/resetDefaultAlarm";
            json_ajax("POST", newurl, "json", false, param, function (data) {
                alarmSettings.setParamVal(data, 'id', 'defaultValue');
            });
            // }
        },
        // 读取终端参数值
        getReadParam: function () {
            var urlF3 = "/clbs/a/alarmSetting/sendParameter";
            var paramF3 = {
                "vehicleId": $('#vehicleId').val(),
                "paramIds": "0xf44f,0xf450"
            };
            json_ajax("POST", urlF3, "json", false, paramF3, alarmSettings.getF3alarmParamCall);
            webSocket.subscribe(headers, "/user/topic/highPrecisionAlarm", alarmSettings.getSensorParam, null, null);


            var url = "/clbs/a/alarmSetting/getDeviceAlarmParam";
            var param = {
                "monitorId": $('#vehicleId').val(),
                "deviceType": $('input[name="deviceCheck"]:checked').val()
            };
            json_ajax("POST", url, "json", false, param, alarmSettings.getF3BaseParamCall);
            webSocket.subscribe(headers, "/user/topic/oil9999Info", alarmSettings.getSensor0104Param, null, null);


            // var url = "/clbs/a/alarmSetting/sendParameter";
            // var param = {
            //     "vehicleId": $('#vehicleId').val(),
            //     "paramIds": "0xf44f,0xf450"
            // };
            // json_ajax("POST", url, "json", false, param, alarmSettings.getF3alarmParamCall);
            //
            // var newurl = "/clbs/a/alarmSetting/getDeviceAlarmParam";
            // var newParam = {
            //     "monitorId": $('#vehicleId').val(),
            //     "deviceType": $('input[name="deviceCheck"]:checked').val()
            // };
            // json_ajax("POST", newurl, "json", false, newParam, alarmSettings.getF3BaseParamCall);
            //
            // if ($("#profile4q").hasClass('active')) {
            //     webSocket.subscribe(headers, "/user/topic/highPrecisionAlarm", alarmSettings.getSensorParam, null, null);
            // } else {
            //     webSocket.subscribe(headers, "/user/topic/oil9999Info", alarmSettings.getSensor0104Param, null, null);
            // }
        },
        //F3高精度报警
        getF3alarmParamCall: function (data) {
            if (!data.success) {
                if (data.msg) {
                    layer.msg(data.msg);
                } else {
                    layer.msg('终端离线');
                }
            } else {
                $("#readParam").html("<i class='fa fa-spinner loading-state'></i>").prop('disabled', true);
                alarmSettings.createSocket0104InfoMonitorF3(data.msg);
            }
        },

        createSocket0104InfoMonitorF3: function (msg) {
            var msg = $.parseJSON(msg);
            F3temp_send_vehicle_msg_id = msg.msgId;
            headers = {"UserName": msg.userName};

            isRead = true;
            clearTimeout(_timeout);
            _timeout = window.setTimeout(function () {
                if (isRead) {
                    isRead = false;
                    layer.closeAll();
                    $("#readParam").html("读取").prop('disabled', false);
                    layer.msg("获取设备数据失败!");
                }
            }, 60000);
        },

        getSensorParam: function (msg) {
            if (msg == null) return;
            var result = $.parseJSON(msg.body);
            var msgSNAck = result.data.msgBody.msgSNAck;
            console.log('f3高精度报警', msgSNAck, F3temp_send_vehicle_msg_id);
            if (msgSNAck != F3temp_send_vehicle_msg_id) {
                return;
            }
            isRead = false;
            clearTimeout(_timeout);
            layer.closeAll();
            $("#readParam").html("读取").prop('disabled', false);
            var dataVal = result.data.msgBody.params;
            // console.log(dataVal);
            alarmSettings.setAlarmVal(dataVal, 'alarmParameterId', 'parameterValue', true);
        },
        setAlarmVal: function (data, checkIdName, paramValName, pushFlag) {
            var result = pushFlag ? data : data.obj;
            if (result.length != 0) {
                $('#roadNetSpeedLimit').prop("checked", false);

                for (var i = 0; i < result.length; i++) {
                    var id = result[i].id;
                    switch (id) {
                        case 62543:
                            var deviceElectricity = result[i].value.electricitySet.deviceElectricity / 10; //电量报警
                            $("#alarmElectric").val(parseInt(deviceElectricity));
                            break;
                        case 62544:
                            var speedCutAlarm = result[i].value.terminalSet.speedCutAlarm / 10;
                            $("#alarmSpeed").val(speedCutAlarm);
                            var speedUpAlarm = result[i].value.terminalSet.speedUpAlarm / 10;
                            $("#alarmAccelerate").val(speedUpAlarm);
                            var swerveAlarm = result[i].value.terminalSet.swerveAlarm;
                            $("#alarmWheel").val(swerveAlarm);
                            var collisionAlarm = result[i].value.terminalSet.collisionAlarm / 10;
                            $("#alarmCollision").val(collisionAlarm);
                            break;
                    }
                }

                //选中下拉框的值
                $(".exceptionMoveSelect").find("option[value = '" + $("#exceptionMoveSelect").val() + "']").attr("selected", "selected");
                $(".overSpeedSelect").find("option[value = '" + $("#overSpeedSelect").val() + "']").attr("selected", "selected");
                alarmSettings.showRoadNetSpeedLimit();
                alarmSettings.selectPosition();
            }
        },
        //F3高精度读取
        setHighPrecisionVal: function (data) {
            var dataList = data.obj;
            if (data.success) {
                var $ele = $("#profile4q .selectvalue");
                var px;
                for (var i = 0; i < dataList.length; i++) {
                    var id = dataList[i].id;
                    var alarmPush = dataList[i].alarmPush;
                    switch (id) {
                        case "60cc2fcf-42b6-45d1-83ab-26ae33b42dbe":
                            $("#alarmElectric").val(dataList[i].defaultValue  / 10);
                            break;
                        case "c6e3e5ee-ce76-40e2-a368-33a5b298c02c":
                            $("#alarmAccelerate").val(dataList[i].defaultValue / 10);
                            break;
                        case "48c2f1ae-6e85-40b2-a2f9-c50ea9c21b90":
                            $("#alarmSpeed").val(dataList[i].defaultValue / 10);
                            break;
                        case "dbc0fa3a-c0ba-4963-9979-cc6f9d7d6a4f":
                            $("#alarmWheel").val(dataList[i].defaultValue);
                            break;
                        case "a21f3b28-98c0-44e3-817e-1502fed96ad7":
                            $("#alarmCollision").val(dataList[i].defaultValue / 10);
                    }
                    $('#' + id).val(alarmPush);
                    if (alarmPush == 0) {
                        px = 9;
                    } else if (alarmPush == 1) {
                        px = 55;
                    } else {
                        px = 103;
                    }
                }
                $ele.each(function () {
                    $(this).parent().parent().find(".selectbutton").css("left", px.toString() + 'px');
                });
            }
        },

        //基本信息-下发获取基本信息返回处理方法
        getF3BaseParamCall: function (data) {
            if (!data.success) {
                if (data.msg) {
                    layer.msg(data.msg);
                } else {
                    layer.msg('终端离线');
                }
            } else {
                $("#readParam").html("<i class='fa fa-spinner loading-state'></i>").prop('disabled', true);
                alarmSettings.createSocket0104InfoMonitor(data.msg);
            }
        },
        //创建消息监听
        createSocket0104InfoMonitor: function (msg) {
            var msg = $.parseJSON(msg);
            var requestStrS = {
                "desc": {
                    "cmsgSN": msg.msgId,
                    "UserName": msg.userName
                },
                "data": []
            };
            temp_send_vehicle_msg_id = msg.msgId;
            headers = {"UserName": msg.userName};

            isRead = true;
            clearTimeout(_timeout);
            _timeout = window.setTimeout(function () {
                if (isRead) {
                    isRead = false;
                    layer.closeAll();
                    $("#readParam").html("读取").prop('disabled', false);
                    layer.msg("获取设备数据失败!");
                }
            }, 60000);
        },
        //处理获取设备上传数据
        getSensor0104Param: function (msg) {
            if (msg == null) return;
            var result = $.parseJSON(msg.body);
            var msgSNAck = result.data.msgBody.msgSNAck;
            // console.log('其余', msgSNAck, temp_send_vehicle_msg_id)
            if (msgSNAck != temp_send_vehicle_msg_id) {
                return;
            }
            isRead = false;
            clearTimeout(_timeout);
            var status = result.data.msgBody.result;
            if (status == 1) {
                layer.closeAll();
                $("#readParam").html("读取").prop('disabled', false);
                layer.msg("获取设备数据失败!");
                return;
            }
            layer.closeAll();
            $("#readParam").html("读取").prop('disabled', false);
            var dataVal = result.data.msgBody.alarmSettingList;
            alarmSettings.setParamVal(dataVal, 'alarmParameterId', 'parameterValue', true);
        },
        //F3高精度表单验证
        handleFormVerify: function () {
            //电量报警, 急转弯报警
            $("#alarmElectric, #alarmWheel").blur(function () {
                var _this = $(this).val(),
                    reg = /^(?:[1-9]?\d|100)$/;

                if (_this != "") {
                    if (reg.test(_this)) {
                        $(this).parent().find('.error').hide();
                    } else {
                        $(this).parent().find('.error').show().text('请输入0-100范围的正整数');
                        return false;
                    }
                } else {
                    $(this).parent().find('.error').hide();
                }
            });

            $("#alarmAccelerate, #alarmSpeed").blur(function () {
                var _this = $(this).val(),
                    reg = /^[0-2]{1}(\.[0-5])?$/;

                if(_this != "") {
                    if(reg.test(_this)) {
                        $(this).parent().find('.error').hide();
                    }else {
                        $(this).parent().find('.error').show().text('请输入0-2.5范围的数字且保留一位小数');
                        return false;
                    }
                }else{
                    $(this).parent().find('.error').hide();
                }
            });

            //碰撞报警
            $("#alarmCollision").blur(function () {
                var _this = $(this).val();
                var reg = /^[0-9]{1}(.[0-9])?$/;
                if(_this != '') {
                    if(reg.test(_this) && _this <=5) {
                        $(this).parent().find('.error').hide();
                    }else{
                        $(this).parent().find('.error').show().text('请输入0-5范围的数字且保留一位小数');
                        return false;
                    }
                }else{
                    $(this).parent().find('.error').hide();
                }
            });
        },
    }
    ;
    $(function () {
        $('input').inputClear();
        alarmSettings.init();
        myTable.add('commonWin', 'settingForm', {ignore: ''}, null);
        alarmSettings.addinfo();
        $label = $("label:not('.alarmLeftPadding')");
        $label.bind("click", alarmSettings.leabelClickFn);
        if($("#alarmElectric").val()) {
            $("#alarmElectric").val($("#alarmElectric").val() / 10);
        }
        alarmSettings.topswitch();
        //预警按钮设定
        alarmSettings.selectPosition();
        //滑块选择切换
        alarmSettings.selectSwitch();
        //初始化勾选屏蔽字段
        alarmSettings.ignore();
        //添加验证大小值
        //	$('input').unbind().bind("blur",alarmSettings.addrangemethod);
        alarmSettings.setminmax();
        //添加 input id name的值
        alarmSettings.setidname();
        alarmSettings.handleFormVerify();

        $('#commonWin').on('hidden.bs.modal', function (e) { //解决bug #8756 再次点击弹框在火狐显示异常
            $('#TabFenceBox a').trigger('click')
        })
        $("#IOAlarmBox select[name='parameterValSel']").each(function () {
            alarmSettings.getIOAlarmStateTxt(this);
        })
    })
    $label.css("cursor", "pointer");
    $(".noneset").css("cursor", "pointer");
    $(".partset").css("cursor", "pointer");
    $(".wholeset").css("cursor", "pointer");
    $(".typeName").mouseover(function () {
        $(this).css("color", "#6dcff6");
    });
    $(".typeName").mouseleave(function () {
        $(this).css("color", "#5D5F63");
    });
    $("#textinfo span").mouseover(function () {
        $(this).css("color", "#6dcff6");
    });
    $("#textinfo span").mouseleave(function () {
        $(this).css("color", "#5D5F63");
    });
    $("#alarmTextInof span").mouseover(function () {
        $(this).css("color", "#6dcff6");
    });
    $("#alarmTextInof span").mouseleave(function () {
        $(this).css("color", "#5D5F63");
    });

    /*$("#71").before('<i style="margin-left:-19px;z-index:999999;"class="fa fa-question-circle fa-lg" data-toggle="tooltip" data-placement="top" title="" data-original-title="包括门磁行驶门开报警，门磁未到门开报警和门磁超时门开报警等"></i>&ensp;')*/
    $("[data-toggle='tooltip']").tooltip();
    //隐藏显示报警参数值

    //超速报警默认收起
    var cli2 = $("#1212ce92-268d-11e8-b467-0ed5f89f718b").parents(".clearfix").prev();
    var parents = $("#f1de45d2-4ebc-11e9-a899-000c2984880c").parents(".clearfix");
    parents.hide();
    var alarmList = cli2.nextAll();
    for (var i = 1; i < 3; i++) {
        $(alarmList[i]).hide();
    }
    for (var i = 3; i < 9; i++) {
        $(alarmList[i]).hide();
        cli2.before($(alarmList[i]));
    }
    var isRoadNetSpeedLimit = $("#isRoadNetSpeedLimit").val();
    if (isRoadNetSpeedLimit === "1") {
        $('#roadNetSpeedLimit').prop("checked", true);
        var list = $("#roadNetSpeedLimit").parents(".clearfix").nextAll();
        for (var i = 1; i < 7; i++) {
            if (i < 6) {
                $(list[i]).show();
            } else {
                $(list[i]).hide();
            }
        }
    }
    $("#overSpeedHide").bind("click", alarmSettings.showTankOrSensorInfoFn);
    $("#roadNetSpeedLimit").bind("change", alarmSettings.showRoadNetSpeedLimit);

    // 默认值 读取功能
    $('#defaultParam').on('click', alarmSettings.getDefaultParam);
    $('#readParam').on('click', alarmSettings.getReadParam);
}($, window))
