/***********polyfill*************/
if (!Array.prototype.findIndex) {
    Object.defineProperty(Array.prototype, 'findIndex', {
        value: function(predicate) {
            // 1. Let O be ? ToObject(this value).
            if (this == null) {
                throw new TypeError('"this" is null or not defined');
            }

            var o = Object(this);

            // 2. Let len be ? ToLength(? Get(O, "length")).
            var len = o.length >>> 0;

            // 3. If IsCallable(predicate) is false, throw a TypeError exception.
            if (typeof predicate !== 'function') {
                throw new TypeError('predicate must be a function');
            }

            // 4. If thisArg was supplied, let T be thisArg; else let T be undefined.
            var thisArg = arguments[1];

            // 5. Let k be 0.
            var k = 0;

            // 6. Repeat, while k < len
            while (k < len) {
                // a. Let Pk be ! ToString(k).
                // b. Let kValue be ? Get(O, Pk).
                // c. Let testResult be ToBoolean(? Call(predicate, T, « kValue, k, O »)).
                // d. If testResult is true, return k.
                var kValue = o[k];
                if (predicate.call(thisArg, kValue, k, o)) {
                    return k;
                }
                // e. Increase k by 1.
                k++;
            }

            // 7. Return -1.
            return -1;
        }
    });
}
var timeOutId; // 点击加油前设置的超时程序
(function(window,$){
    var onlyOne=false;
    var startTime;
    var endTime;

    // 标定组数最小值
    var min_calibrationSets = 2;
    // 标定组数最大值
    var max_calibrationSets = 50;
    var again = $("#again"); // 重新标定面板
    var tankOne = $("#tankOne"); // 主油箱
    var tankTwo = $("#tankTwo"); // 副邮箱
    var againCalibration = $("#againCalibration"); // 重新标定标签
    var amendmentCalibration = $("#amendmentCalibration"); // 实时修正标签
    var tankOneCutover = $("#tankOneCutover"); // 主油箱 tab
    var tankTwoCutover = $("#tankTwoCutover"); // 副油箱 tab
    var submitBefore = $("#submitBefore"); // 加油前点我
    var submitAfter = $("#submitAfter"); // 加油后点我
    var submitBtn = $("#submitBtn"); // 修正标定 - 修正下发
    var corCancleBtn = $("#corCancleBtn"); // 修正标定 - 取消修正
    var compareBtn = $("#compareDataBtn"); // 修正标定 - 数据对比
    var refreshBtn = $("#refreshCompare"); // 修正标定 - 数据对比弹出框 - 刷新按钮
    var sendCompareBtn=$("#sendCompare");// 修正标定-数据对比弹出框-下发按钮
    var saveOilCalibrationBtn = $("#saveOilCalibrationBtn"); // 重新标定 - 保存下发
    // var saveOilCalibrationBtn2 = $("#saveOilCalibrationBtn2");
    var cancleSaveBtn = $("#cancleSaveBtn"); // 重新标定 - 撤销
    var vehicleList = JSON.parse($("#vehicleList").attr("value")); // 有油量的车辆
    // ，下拉框中的数据
    // 重新标定最后的保存按钮
    var reCalBtnDiv = $("#reCalBtnDiv");
    // 重新标定
    var submitBefore2 = $("#submitBefore2");
    var submitBtn2 = $("#submitBtn2");
    var num = 0; // 油箱1、油箱2总记录数，设置input的id时用
    var seqNo = 1; // 标定数据序号-油箱1
    var seqNo2 = 1; // 标定数据序号-油箱2
    var initFlag = true; // 获取初始数据标识
    var tempOilValue = 0; // 油箱1临时油量值
    var tempOilValue2 = 0; // 油箱2临时油量值
    var re_tank1_oilHeights = ''; // 重新标定-油箱1-液位高度
    var re_tank1_oilValues = ''; // 重新标定-油箱1-油量值
    var re_tank2_oilHeights = ''; // 重新标定-油箱2-液位高度
    var re_tank2_oilValues = ''; // 重新标定-油箱2-油量值
    var onLineStatus = false; // 标识车辆是否在线
    var tankRadio1 = $("#tankRadio1");
    var tankRadio2 = $("#tankRadio2");
    var tankRadio3 = $("#tankRadio3");
    var tankRadio4 = $("#tankRadio4");
    var tankRadio5 = $("#tankRadio5");
    var tankRadio6 = $("#tankRadio6");
    var sendStatus4Amendment=$(".sendStatus4Amendment"); // 修正标定-下发状态
    // 车辆下拉列表
    var dataList = {value: []};
    // 用于记录当前点击的哪个按钮：1-修正标定_加油前点我；2-修正标定_加油后点我；3-重新标定_加油前点我；4-重新标定_实际加油后的提交按钮
    var curClickedBtn = $("#curClickedBtn");
    var calibrationFlag = false; // 标识是否在标定
    // 订阅车辆消息
    var params = [];
    // 追溯标定
    var actualAddOil3 = $("#actualAddOil3");
    var submit_btn = $("#submit_btn");
    var cancle_btn = $("#cancle_btn");
    var time_before; // 加油前时间点-油箱1
    var time_after; // 加油后时间点-油箱1
    var oil_before; // 加油前油量-油箱1
    var oil_after; // 加油后油量-油箱1
    var time_before2; // 加油前时间点-油箱2
    var time_after2; // 加油后时间点-油箱2
    var oil_before2; // 加油前油量-油箱2
    var oil_after2; // 加油后油量-油箱2
    var refreshDataBtn3=$("#refreshDataBtn3");// 追溯标定-刷新按钮
    // echarts图表数据存放
    var myChart;
    var date = [];
    var oil = [];
    var mileage = [];
    var speed = [];
    var oilOne = [];
    var oilTwo = [];
    var oilOneTemp = [];
    var ENVOneTemp = [];
    var oilTwoTemp = [];
    var ENVTwoTemp = [];
    var envTemp = [];
    var oilMax;
    var oilHeight = [];
    var oilHeight2 = [];
    var option;
    var layer_time;
    var _timeout;

    var list1 = {
        obj:{}
    }; // 原始标定数据
    var list2={
        obj:{}
    }; // 原始数据 * 修正系数
    var inCompareMode=false; // 数据对比模式
    var isRead; // 检测是否正在处理结果
    var selected = {
        '总油量' : false,
        '里程' : false,
        '速度' : false,
        '油量1' : true,
        '油量2' : false,
        '燃油温度1' : false,
        '环境温度1' : false,
        '燃油温度2' : false,
        '环境温度2' : false,
        '空调' : false
    };
    // 图表缩放等级
    var startZoom = 0;
    var endZoom = 100;
    var nvl=function(obj,defaultValue){
        if(!obj){
            return defaultValue;
        }
            return obj.length;

    };
    var toFixed=function(source,digit,omitZero){
        if(typeof source === 'string'){
            source = parseFloat(source)
        }
        if(typeof source === 'number'){
            var afterFixed=source.toFixed(digit) // 此时 afterFixed 为string类型
            if(omitZero){
                afterFixed=parseFloat(afterFixed)
            }
            return afterFixed
        }
    }

    olicalibrationPages = {
        init: function(){
            // webSocket.init('/clbs/vehicle');
            olicalibrationPages.initVehicleInfoList();
            // 页面关闭或者刷新时的操作
            olicalibrationPages.pageOnbeforeonload();
        },
        nowDay: function () {
            var nowDate = new Date();
            startTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate()) + " " + "00:00:00";
            endTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate())
                + " "
                + ("23")
                + ":"
                + ("59")
                + ":"
                + ("59");
            $('#timeInterval').val(startTime + '--' + endTime);
        },
        // 开始时间
        startDay: function (day) {
            var timeInterval = $('#timeInterval').val().split('--');
            var startValue = timeInterval[0];
            var endValue = timeInterval[1];
            if (startValue == "" || endValue == "") {
                var today = new Date();
                var targetday_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * day;
                today.setTime(targetday_milliseconds); // 注意，这行是关键代码
                var tYear = today.getFullYear();
                var tMonth = today.getMonth();
                var tDate = today.getDate();
                tMonth = olicalibrationPages.doHandleMonth(tMonth + 1);
                tDate = olicalibrationPages.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " " + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * parseInt(num);
                today.setTime(end_milliseconds); // 注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = olicalibrationPages.doHandleMonth(endMonth + 1);
                endDate = olicalibrationPages.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate + " " + "23:59:59";
            } else {
                var startTimeIndex = startValue.slice(0,10).replace("-","/").replace("-","/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = olicalibrationPages.doHandleMonth(vMonth + 1);
                vDate = olicalibrationPages.doHandleMonth(vDate);
                startTime = vYear + "-" + vMonth + "-" + vDate + " "
                    + "00:00:00";
                var endNum = -1;
                var vendtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * parseInt(endNum);
                var dateEnd = new Date();
                dateEnd.setTime(vendtoday_milliseconds);
                var vendYear = dateEnd.getFullYear();
                var vendMonth = dateEnd.getMonth();
                var vendDate = dateEnd.getDate();
                vendMonth = olicalibrationPages.doHandleMonth(vendMonth + 1);
                vendDate = olicalibrationPages.doHandleMonth(vendDate);
                endTime = vendYear + "-" + vendMonth + "-" + vendDate + " "
                    + "23:59:59";
            }
            $('#timeInterval').val(startTime + '--' + endTime);
        },
        doHandleMonth: function (month) {
            var m = month;
            if (month.toString().length == 1) {
                m = "0" + month;
            }
            return m;
        },
        todayClick: function(){
            olicalibrationPages.nowDay();
            var charNum = $("#brands3").attr("data-id");
            if (charNum != "") {
                if (olicalibrationPages.validates()) {
                    olicalibrationPages.ajaxList(charNum, startTime, endTime);
                }
            } else {
                layer.msg(brandNullMsg, {move: false});
            }
        },
        yesterdayClick: function(){
            olicalibrationPages.startDay(-1);
            var charNum = $("#brands3").attr("data-id");
            if (charNum != "") {
                if (olicalibrationPages.validates()) {
                    olicalibrationPages.ajaxList(charNum, startTime, endTime);
                }
            } else {
                layer.msg(brandNullMsg, {move: false});
            }
        },
        // 查询
        inquireClick: function () {
            var charNum = $("#brands3").attr("data-id");
            var timeInterval = $('#timeInterval').val().split('--');
            startTime = timeInterval[0];
            endTime = timeInterval[1];
            if(charNum!=""){
                if (olicalibrationPages.validates()) {
                    olicalibrationPages.ajaxList(charNum, startTime, endTime);
                }
            }else {
                layer.msg(brandNullMsg, {move: false});
            }
        },
        // 查询是否绑定外设轮询
        getSensorMessage : function (band) {
            var flog;
            var url = "/clbs/v/oilmassmgt/oilquantitystatistics/getSensorMessage";
            var data = {"band": band};
            json_ajax("POST", url, "json", false,data,function(data){
                flog = data;
            });
            return flog;
        },
        // ajax请求图表数据
        ajaxList:function(band, startTime, endTime){
            olicalibrationPages.initForm4();
            $("input[name='oil_value']").val("");
            oil = [];
            oilOne = [];
            oilTwo = [];
            mileage = [];
            speed = [];
            date = [];
            oilOneTemp = [];
            oilTwoTemp = [];
            ENVOneTemp = [];
            ENVTwoTemp = [];
            envTemp = [];
            oilHeight = [];
            oilHeight2 = [];
            time_before = ""; // 加油前时间点-油箱1
            time_after = ""; // 加油后时间点-油箱1
            oil_before = ""; // 加油前油量-油箱1
            oil_after = ""; // 加油后油量-油箱1
            time_before2 = ""; // 加油前时间点-油箱2
            time_after2 = ""; // 加油后时间点-油箱2
            oil_before2 = ""; // 加油前油量-油箱2
            oil_after2 = ""; // 加油后油量-油箱2
            $.ajax({
                type: "POST",
                url: "/clbs/v/oilmassmgt/oilquantitystatistics/getOilInfo",
                data: {"band": band, "startTime": startTime, "endTime": endTime},
                dataType: "json",
                async: true,
                timeout : 30000, // 超时时间设置，单位毫秒
                beforeSend: function () {
                    // 异步请求时spinner出现
                    layer.load(2);
                },
                success: function (data) {
                    oil = [];
                    oilOne = [];
                    oilTwo = [];
                    mileage = [];
                    speed = [];
                    date = [];
                    oilOneTemp = [];
                    oilTwoTemp = [];
                    ENVOneTemp = [];
                    ENVTwoTemp = [];
                    envTemp = [];
                    oilHeight = [];
                    oilHeight2 = [];
                    time_before = ""; // 加油前时间点-油箱1
                    time_after = ""; // 加油后时间点-油箱1
                    oil_before = ""; // 加油前油量-油箱1
                    oil_after = ""; // 加油后油量-油箱1
                    time_before2 = ""; // 加油前时间点-油箱2
                    time_after2 = ""; // 加油后时间点-油箱2
                    oil_before2 = ""; // 加油前油量-油箱2
                    oil_after2 = ""; // 加油后油量-油箱2
                    var responseData = JSON.parse(ungzip(data.obj.oilInfo));
                    data.obj.oilInfo = responseData;
                    layer.closeAll('loading');
                    $('#timeInterval').val(startTime + '--' + endTime);
                    if (data.obj.oilInfo.length != 0) {
                        $("#graphShow").show();
                        $("#showClick").attr("class","fa fa-chevron-down");
                        var nullData=0;
                        var travelTime = 0;
                        var changeTime = 0;
                        var rtime = 0;
                        var chenageTimes=0;
                        var Amount=0;
                        var leak=0;
                        startZoom = 0;
                        endZoom = 100;
                        var miles;
                        var speeds;
                        /* ========================查询是否绑定外设轮询============================================== */
                        flogKey = olicalibrationPages.getSensorMessage(band);
                        /* ====================================================================================== */
                        for (var i = 0, len = data.obj.oilInfo.length-1; i < len; i++) {
                            if(flogKey == "true"){
                                miles = Number(data.obj.oilInfo[i].mileageTotal === undefined ? 0 : data.obj.oilInfo[i].mileageTotal);
                                speeds = Number(data.obj.oilInfo[i].mileageSpeed === undefined ? 0 : data.obj.oilInfo[i].mileageSpeed);
                            }else{
                                miles = Number(data.obj.oilInfo[i].gpsMile === undefined ? 0 : data.obj.oilInfo[i].gpsMile);
                                speeds = Number(data.obj.oilInfo[i].speed === undefined ? 0 : data.obj.oilInfo[i].speed);
                            }
                            if(miles==NaN||miles==0){
                                miles="";
                            }
                            if(speeds==null||miles==NaN||miles==0){
                                speeds="";
                            }
                            if(!(Number(data.obj.oilInfo[i].totalOilwearOne)==0&&Number(data.obj.oilInfo[i].gpsMile)==0&&Number(data.obj.oilInfo[i].speed)==0&&Number(data.obj.oilInfo[i].oiltankTemperatureOne)==0)){
                                if(data.obj.oilInfo[i].vtime!=data.obj.oilInfo[i+1].vtime){
                                    date.push(olicalibrationPages.timeStamp2String(olicalibrationPages.UnixToDate(data.obj.oilInfo[i].vtime,true)));
                                    var oilTatal=(Number(data.obj.oilInfo[i].oilTankOne) + Number(data.obj.oilInfo[i].oilTankTwo)).toFixed(2);
                                    if(oilTatal>=0.5){
                                        oil.push(oilTatal);
                                    }else{
                                        oil.push("-");
                                    }
                                    if(Number(data.obj.oilInfo[i].oilTankOne ) >= 5){
                                        oilOne.push(parseFloat(Number(data.obj.oilInfo[i].oilTankOne).toFixed(2)));
                                    }else {
                                        oilOne.push("");
                                    }
                                    if(Number(data.obj.oilInfo[i].oilTankTwo ) >= 5){
                                        oilTwo.push(parseFloat(Number(data.obj.oilInfo[i].oilTankTwo).toFixed(2)));
                                    }else {
                                        oilTwo.push("")
                                    }
                                    mileage.push(miles);
                                    speed.push(speeds);
                                    if((Number(data.obj.oilInfo[i].fuelTemOne)).toFixed(2)<80&&(Number(data.obj.oilInfo[i].fuelTemOne)).toFixed(2)>0){
                                        oilOneTemp.push(parseFloat((Number(data.obj.oilInfo[i].fuelTemOne)).toFixed(1)));
                                    }else {
                                        oilOneTemp.push("-");
                                    }
                                    if((Number(data.obj.oilInfo[i].fuelTemTwo)).toFixed(2)<80&&(Number(data.obj.oilInfo[i].fuelTemTwo)).toFixed(2)>0){
                                        oilTwoTemp.push(parseFloat((Number(data.obj.oilInfo[i].fuelTemTwo)).toFixed(1)));
                                    }else {
                                        oilTwoTemp.push("-")
                                    }
                                    if((Number(data.obj.oilInfo[i].environmentTemOne)).toFixed(2)<80&&(Number(data.obj.oilInfo[i].environmentTemOne)).toFixed(2)>0){
                                        ENVOneTemp.push(parseFloat((Number(data.obj.oilInfo[i].environmentTemOne)).toFixed(1)));
                                    }else {
                                        ENVOneTemp.push("-")
                                    }
                                    if((Number(data.obj.oilInfo[i].environmentTemTwo)).toFixed(2)<80&&(Number(data.obj.oilInfo[i].environmentTemTwo)).toFixed(2)>0){
                                        ENVTwoTemp.push(parseFloat((Number(data.obj.oilInfo[i].environmentTemTwo)).toFixed(1)));
                                    }else {
                                        ENVTwoTemp.push("-");
                                    }
                                    envTemp.push(Number(data.obj.oilInfo[i].airConditionStatus));
                                    if(data.obj.oilInfo[i].fuelAmountOne!=null||data.obj.oilInfo[i].fuelAmountTwo!=null){
                                        Amount+=(Number(data.obj.oilInfo[i].fuelAmountOne)+Number(data.obj.oilInfo[i].fuelAmountTwo))
                                    }
                                    if(data.obj.oilInfo[i].fuelSpillOne!=null||data.obj.oilInfo[i].fuelSpillTwo!=null){
                                        leak+=(Number(data.obj.oilInfo[i].fuelSpillOne)+Number(data.obj.oilInfo[i].fuelSpillTwo))
                                    }
                                    if(data.obj.oilInfo[i].oilHeightOne != null){
                                        oilHeight.push((parseFloat(data.obj.oilInfo[i].oilHeightOne)).toFixed(1));
                                    } else {
                                        oilHeight.push("");
                                    }
                                    if (data.obj.oilInfo[i].oilHeightTwo != null) {
                                        oilHeight2.push((parseFloat(data.obj.oilInfo[i].oilHeightTwo)).toFixed(1));
                                    } else {
                                        oilHeight2.push("");
                                    }
                                }
                            }
                        }
                        // 自动填充最大值和最小值作为加油前和加油后的数据
                        olicalibrationPages.defaultOilData();
                    }else{
                        $("#showClick").attr("class","fa fa-chevron-up");
                    }
                    oilMax = parseInt(Math.max.apply(null, oil)) + 20;
                    if(data.success==false){
                        layer.msg(systemErrorMsg)
                        oil = [];
                        oilOne = [];
                        oilTwo = [];
                        mileage = [];
                        speed = [];
                        dataSets = [];
                        date = [];
                        oilOneTemp = [];
                        oilTwoTemp = [];
                        ENVOneTemp = [];
                        ENVTwoTemp = [];
                        envTemp = [];
                        oilHeight = [];
                        oilHeight2 = [];
                    };
                    if(oil.length != 0){
                        olicalibrationPages.echartsData();
                        // $("#showClick").on("click",olicalibrationPages.iconChange);
                    }
                },
                error:function(jqXHR, textStatus, errorThrown){
                    layer.closeAll('loading');
                    if(textStatus=="timeout"){
                        layer.msg(systemLoadingTimeout);
                    }
                },

            });
        },
        // 追溯标定，查询油量后，默认将当前最大值和最小值作为默认值显示在油量数据文本框中
        defaultOilData : function () {
            //return
            // 自动填充当前查询范围内的最大值和最小值
            var tank1_max_oil = ""; // 油箱1最大油量值
            var tank1_max_oil_index = ""; // 油箱1最大油量值的下标值，用于获取其对应的液位高度
            var tank1_max_height = ""; // 油箱1最大液位高度
            var tank1_min_oil = ""; // 油箱1最小油量值
            var tank1_min_oil_index = ""; // 油箱1最小油量值的下标值，用于获取其对应的液位高度
            var tank1_min_height = ""; // 油箱1最小液位高度
            var tank2_max_oil = ""; // 油箱2最大油量值
            var tank2_max_oil_index = ""; // 油箱2最大油量值的下标值，用于获取其对应的液位高度
            var tank2_max_height = ""; // 油箱2最大液位高度
            var tank2_min_oil = ""; // 油箱2最小油量值
            var tank2_min_oil_index = ""; // 油箱2最小油量值的下标值，用于获取其对应的液位高度
            var tank2_min_height = ""; // 油箱2最小液位高度
            // 获取上次标定时间对应的索引值

            var _tank1LastTime=$('#tank1LastTime').val(); // 上次标定时间
            var lastOilIndex=date.findIndex(function(ele){
                return ele >= _tank1LastTime
            });
            var validOilOne= olicalibrationPages.removeInValideData(oilOne);
            var validOilTwo= olicalibrationPages.removeInValideData(oilTwo);
            var tempOilOne = validOilOne.slice(lastOilIndex);
            var tempOilTwo = validOilTwo.slice(lastOilIndex);

            oil_before = null; // 加油前油量-油箱1
            oil_after = null; // 加油后油量-油箱1
            oil_before2 = null; // 加油前油量-油箱2
            oil_after2 = null; // 加油后油量-油箱2

            // 先获取最大值，然后在最大值前找最小值

//				tank1_max_oil = parseFloat(Math.max.apply(null, tempOilOne)).toFixed(1);
//				tank1_max_oil_index = olicalibrationPages.getMaxOrMinOilIndex(tank1_max_oil, tempOilOne)+lastOilIndex;
//				if (!isNaN(tank1_max_oil_index)) {
//					oliAfter_y = tank1_max_height = parseFloat(oilHeight[tank1_max_oil_index]).toFixed(1);
//					time_after = date[tank1_max_oil_index];
//					tempOilOne=validOilOne.slice(lastOilIndex,tank1_max_oil_index+1);
//				}
//
//				oil_before = tank1_min_oil = parseFloat(Math.min.apply(null, tempOilOne)).toFixed(1);
//				tank1_min_oil_index = olicalibrationPages.getMaxOrMinOilIndex(tank1_min_oil, tempOilOne)+lastOilIndex;
//
//				if (!isNaN(tank1_min_oil_index)) {
//					tank1_min_height = parseFloat(oilHeight[tank1_min_oil_index]).toFixed(1);
//					time_before = date[tank1_min_oil_index];
//				}

            // 先获取最小值，然后在最小值后找最大值，如果最大值与最小值重合，则不显示
            oil_before = tank1_min_oil = parseFloat(Math.min.apply(null, tempOilOne)).toFixed(1);
            tank1_min_oil_index = olicalibrationPages.getMaxOrMinOilIndex(tank1_min_oil, tempOilOne)+lastOilIndex;
            if (!isNaN(tank1_min_oil_index)) {
                tank1_min_height = parseFloat(oilHeight[tank1_min_oil_index]).toFixed(1);
                time_before = date[tank1_min_oil_index];
                tempOilOne=validOilOne.slice(tank1_min_oil_index);
            }

            oil_after = tank1_max_oil = parseFloat(Math.max.apply(null, tempOilOne)).toFixed(1);
            tank1_max_oil_index = olicalibrationPages.getMaxOrMinOilIndex(tank1_max_oil, tempOilOne)+tank1_min_oil_index;

            if (!isNaN(tank1_max_oil_index) && tank1_max_oil_index != tank1_min_oil_index) {
                tank1_max_height = parseFloat(oilHeight[tank1_max_oil_index]).toFixed(1);
                time_after = date[tank1_max_oil_index];
            }
            // 邮箱2
            oil_before2 = tank2_min_oil = parseFloat(Math.min.apply(null, tempOilTwo)).toFixed(1);
            tank2_min_oil_index = olicalibrationPages.getMaxOrMinOilIndex(tank2_min_oil, tempOilTwo)+lastOilIndex;
            if (!isNaN(tank2_min_oil_index)) {
                tank2_min_height = parseFloat(oilHeight[tank2_min_oil_index]).toFixed(1);
                time_before2 = date[tank2_min_oil_index];
                tempOilTwo=validOilTwo.slice(tank2_min_oil_index);
            }

            oil_after2 = tank2_max_oil = parseFloat(Math.max.apply(null, tempOilTwo)).toFixed(1);
            tank2_max_oil_index = olicalibrationPages.getMaxOrMinOilIndex(tank2_max_oil, tempOilTwo)+tank2_min_oil_index;

            if (!isNaN(tank2_max_oil_index) && tank2_max_oil_index != tank2_min_oil_index) {
                tank2_max_height = parseFloat(oilHeight[tank2_max_oil_index]).toFixed(1);
                time_after2 = date[tank2_max_oil_index];
            }

//				tank2_max_oil = parseFloat(Math.max.apply(null, tempOilTwo)).toFixed(1);
//				tank2_max_oil_index = olicalibrationPages.getMaxOrMinOilIndex(tank2_max_oil, tempOilTwo)+lastOilIndex;
//				if (!isNaN(tank2_max_oil_index)) {
//					tank2_max_height = parseFloat(oilHeight2[tank2_max_oil_index]).toFixed(1);
//					time_after2 = date[tank2_max_oil_index];
//					tempOilTwo = validOilTwo.slice(lastOilIndex,tank2_max_oil_index+1);
//				}
//
//				oil_before2 = tank2_min_oil = parseFloat(Math.min.apply(null, tempOilTwo)).toFixed(1);
//				tank2_min_oil_index = olicalibrationPages.getMaxOrMinOilIndex(tank2_min_oil, tempOilTwo)+lastOilIndex;
//				if (!isNaN(tank2_min_oil_index)) {
//					tank2_min_height = parseFloat(oilHeight2[tank2_min_oil_index]).toFixed(1);
//					time_before2 = date[tank2_min_oil_index];
//				}


            tank1_min_height = (isNaN(tank1_min_height) || parseFloat(tank1_min_height) == Infinity) ? "" : tank1_min_height;
            tank1_min_oil = (isNaN(tank1_min_oil) || parseFloat(tank1_min_oil) == Infinity) ? "" : tank1_min_oil;
            tank1_max_height = (isNaN(tank1_max_height) || parseFloat(tank1_max_height) == Infinity) ? "" : tank1_max_height;
            tank1_max_oil = (isNaN(tank1_max_oil) || parseFloat(tank1_max_oil) == Infinity) ? "" : tank1_max_oil;
            tank2_min_height = (isNaN(tank2_min_height) || parseFloat(tank2_min_height) == Infinity) ? "" : tank2_min_height;
            tank2_min_oil = (isNaN(tank2_min_oil) || parseFloat(tank2_min_oil) == Infinity) ? "" : tank2_min_oil;
            tank2_max_height = (isNaN(tank2_max_height) || parseFloat(tank2_max_height) == Infinity) ? "" : tank2_max_height;
            tank2_max_oil = (isNaN(tank2_max_oil) || parseFloat(tank2_max_oil) == Infinity) ? "" : tank2_max_oil;
            tank1MinHeight = (tank1_min_height == "") ? "" : (tank1_min_height + " mm");
            tank1MinOil = (tank1_min_oil == "") ? "" : (tank1_min_oil + " L");
            tank1MaxHeight = (tank1_max_height == "") ? "" : (tank1_max_height + " mm");
            tank1MaxOil = (tank1_max_oil == "") ? "" : (tank1_max_oil + " L");
            tank2MinHeight = (tank2_min_height == "") ? "" : (tank2_min_height + " mm");
            tank2MinOil = (tank2_min_oil == "") ? "" : (tank2_min_oil + " L");
            tank2MaxHeight = (tank2_max_height == "") ? "" : (tank2_max_height + " mm");
            tank2MaxOil = (tank2_max_oil == "") ? "" : (tank2_max_oil + " L");
            var curBox = $("#curBox").val();
            if (curBox == "1") {
                $("#oil_before_height").val(tank1MinHeight);
                $("#oil_before_value").val(tank1MinOil);
                $("#ascendCalibration_tank1_oilLevelHeight_before").val(tank1MinHeight);
                $("#ascendCalibration_tank1_oilValue_before").val(tank1MinOil);
                if (time_before != "" && time_after != "" && time_before < time_after) {
                    oil_after = tank1_max_oil;
                    $("#oil_after_height").val(tank1MaxHeight);
                    $("#oil_after_value").val(tank1MaxOil);
                    $("#actualAddOil3").attr("placeholder", olicalibrationPages.calculateTheoryAddOil());
                    $("#ascendCalibration_tank1_oilLevelHeight_after").val(tank1MaxHeight);
                    $("#ascendCalibration_tank1_oilValue_after").val(tank1MaxOil);
                }
                $("#ascendCalibration_tank2_oilLevelHeight_before").val(tank2MinHeight);
                $("#ascendCalibration_tank2_oilValue_before").val(tank2MinOil);
                if ((time_before2 != "" && time_after2 != "" && time_before2 < time_after2)) {
                    oil_after2 = tank2_max_oil;
                    $("#ascendCalibration_tank2_oilLevelHeight_after").val(tank2MaxHeight);
                    $("#ascendCalibration_tank2_oilValue_after").val(tank2MaxOil);
                }
            }
            if (curBox == "2") {
                $("#oil_before_height").val(tank2MinHeight);
                $("#oil_before_value").val(tank2MinOil);
                $("#ascendCalibration_tank2_oilLevelHeight_before").val(tank2MinHeight);
                $("#ascendCalibration_tank2_oilValue_before").val(tank2MinOil);
                if (time_before2 != "" && time_after2 != "" && time_before2 < time_after2) {
                    oil_after2 = tank2_max_oil;
                    $("#oil_after_height").val(tank2MaxHeight);
                    $("#oil_after_value").val(tank2MaxOil);
                    $("#actualAddOil3").attr("placeholder", olicalibrationPages.calculateTheoryAddOil());
                    $("#ascendCalibration_tank2_oilLevelHeight_after").val(tank2MaxHeight);
                    $("#ascendCalibration_tank2_oilValue_after").val(tank2MaxOil);
                }
                $("#ascendCalibration_tank1_oilLevelHeight_before").val(tank1MinHeight);
                $("#ascendCalibration_tank1_oilValue_before").val(tank1MinOil);
                if (time_before != "" && time_after != "" && time_before < time_after) {
                    oil_after = tank1_max_oil;
                    $("#ascendCalibration_tank1_oilLevelHeight_after").val(tank1MaxHeight);
                    $("#ascendCalibration_tank1_oilValue_after").val(tank1MaxOil);
                }
            }
        },
        // 根据油量值获取油量最大值或者最小值的下标值
        getMaxOrMinOilIndex : function (maxOrMinVal, oilArr) {
            var index = "";
            if (oilArr != null && oilArr.length > 0) {
                for (var i=0; i<oilArr.length; i++) {
                    if (maxOrMinVal == oilArr[i]) {
                        index = i;
                        break;
                    }
                }
            }
            return index;
        },
        // 去除数组中，不是数字的项
        removeInValideData : function (array) {
            var resultArray = [];
            if (array != null && array.length) {
                for (var i=0; i<array.length; i++) {
                    if ( !isNaN(array[i])) {
                        resultArray.push(array[i]);
                    } else if (array[i] == ""){
                        resultArray.push(null);
                    }
                }
            }
            return resultArray;
        },
        timeStamp2String: function(time) {
            var time = time.toString();
            var startTimeIndex = time.replace("-","/").replace("-","/");
            var val = Date.parse(startTimeIndex);
            var datetime = new Date();
            datetime.setTime(val);
            var year = datetime.getFullYear();
            var month = datetime.getMonth() + 1 < 10 ? "0" + (datetime.getMonth() + 1) : datetime.getMonth() + 1;
            var date = datetime.getDate() < 10 ? "0" + datetime.getDate() : datetime.getDate();
            var hour = datetime.getHours() < 10 ? "0" + datetime.getHours() : datetime.getHours();
            var minute = datetime.getMinutes() < 10 ? "0" + datetime.getMinutes() : datetime.getMinutes();
            var second = datetime.getSeconds() < 10 ? "0" + datetime.getSeconds() : datetime.getSeconds();
            return year + "-" + month + "-" + date + " " + hour + ":" + minute + ":" + second;
        },
        UnixToDate: function(unixTime, isFull, timeZone) {
            if (typeof (timeZone) == 'number') {
                unixTime = parseInt(unixTime) + parseInt(timeZone) * 60 * 60;
            }
            var time = new Date(unixTime * 1000);
            var ymdhis = "";
            ymdhis += time.getFullYear() + "-";
            ymdhis += (time.getMonth() + 1) + "-";
            ymdhis += time.getDate();
            if (isFull === true) {
                ymdhis += " " + time.getHours() + ":";
                ymdhis += time.getMinutes() + ":";
                ymdhis += time.getSeconds();
            }
            return ymdhis;
        },
        // echarts图表
        echartsData: function(){
            myChart = echarts.init(document.getElementById('sjcontainer'));
            option = {
                tooltip: {
                    trigger: 'axis',
                    textStyle: {
                        fontSize: 20
                    },
                    formatter: function (a) {
                        var unit = ['L','km','km/h','L','L','°C','°C','°C','°C','空调'];
                        var relVal = "";
                        relVal = a[0].name;
                        if(a[0].data == null){
                            relVal = oilCalibrationAscendNodata;
                        }else{
                            for(var i = 0; i < a.length; i++){
                                if(a[i].seriesName == "空调"){
                                    if(a[i].data == 0){
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:"+ a[i].color +"'></span>"+ a[i].seriesName +"：关闭";
                                    }else{
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:"+ a[i].color +"'></span>"+ a[i].seriesName +"：开启";
                                    }
                                }else{
                                    var data = a[i].data == '-' ? '无数据' : a[i].data + unit[a[i].seriesIndex];
                                    if(a[i].seriesName == "主油箱" || a[i].seriesName == "副油箱"){
                                        if(a[i].seriesName == "主油箱"){
                                            relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:"+ a[i].color +"'></span>"+ a[i].seriesName +"："+ data +"";
                                            relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:"+ a[i].color +"'></span>液位高度："+ (oilHeight[a[i].dataIndex] == '' ? '无数据':oilHeight[a[i].dataIndex] + "mm");
                                        }else{
                                            relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:"+ a[i].color +"'></span>"+ a[i].seriesName +"："+ data +"";
                                            relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:"+ a[i].color +"'></span>液位高度："+ (oilHeight2[a[i].dataIndex] == '' ? '无数据':oilHeight2[a[i].dataIndex] + "mm");
                                        }
                                    }else{
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:"+ a[i].color +"'></span>"+ a[i].seriesName +"："+ data +"";
                                    }
                                }
                            }
                        }
                        return relVal;
                    }
                },
                legend: {
                    itemHeight:12,
                    itemWidth:12,
                    data: [
                        {name:'总油量',icon:'circle'},
                        {name:'里程',icon:'circle'},
                        {name:'速度',icon:'circle'},
                        {name:'主油箱',icon:'circle'},
                        {name:'副油箱',icon:'circle'},
                        {name:'燃油温度1',icon:'circle'},
                        {name:'环境温度1',icon:'circle'},
                        {name:'燃油温度2',icon:'circle'},
                        {name:'环境温度2',icon:'circle'},
                        {name:'空调',icon:'circle'}
                    ],
                    left: 'auto',
                    selected: selected,
                },
                toolbox: {
                    show: false
                },
                xAxis: {
                    type: 'category',
                    boundaryGap: false,
                    data: date
                },
                yAxis: [
                    {
                        type: 'value',
                        name: '速度(km/h)',
                        scale: true,
                        min: 0,
                        max: 240,
                        position: 'right',
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine:{
                            show:false
                        }
                    },
                    {
                        type: 'value',
                        name: '温度(°C)',
                        scale: true,
                        position: 'right',
                        offset: 60,
                        min: -30,
                        max: 100,
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine:{
                            show:false
                        }
                    },
                    {
                        type: 'value',
                        name: '空调',
                        scale: true,
                        min: 0,
                        max: 1,
                        splitNumber: 1,
                        position: 'right',
                        offset:100,
                        axisLabel: {
                            formatter: '{value}',
                            formatter: function (value) {
                                if (value == 0) {
                                    return '关'
                                }
                                    return '开'

                            },
                        },
                        splitLine:{
                            show:false
                        }
                    },
                    {
                        type: 'value',
                        name: '油量(L)',
                        scale: true,
                        position: 'left',
                        offset: 60,
                        min: 0,
                        max: oilMax,
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine:{
                            show:false
                        },
                    },
                    {
                        type: 'value',
                        name: '里程(km)',
                        position: 'left',
                        scale: true,
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine:{
                            show:false
                        }
                    }
                ],
                dataZoom: [{
                    type: 'inside',
                    start:	startZoom,
                    end: endZoom,
                }, {
                    handleIcon: 'M10.7,11.9v-1.3H9.3v1.3c-4.9,0.3-8.8,4.4-8.8,9.4c0,5,3.9,9.1,8.8,9.4v1.3h1.3v-1.3c4.9-0.3,8.8-4.4,8.8-9.4C19.5,16.3,15.6,12.2,10.7,11.9z M13.3,24.4H6.7V23h6.6V24.4z M13.3,19.6H6.7v-1.4h6.6V19.6z',
                    handleSize: '80%',
                    handleStyle: {
                        color: '#fff',
                        shadowBlur: 3,
                        shadowColor: 'rgba(0, 0, 0, 0.6)',
                        shadowOffsetX: 2,
                        shadowOffsetY: 2
                    }
                }],
                series: [
                    {
                        name: '总油量',
                        yAxisIndex: 3,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(248, 123, 0)'
                            }
                        },

                        label:{
                            normal:{
                                formatter :'{value}L'
                            }
                        },
                        data: oil
                    },
                    {
                        name: '里程',
                        yAxisIndex: 4,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(109, 207, 246)'
                            }
                        },
                        data: mileage
                    },
                    {
                        name: '速度',
                        yAxisIndex: 0,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(145, 218, 0)'
                            }
                        },
                        data: speed
                    },
                    {
                        name: '主油箱',
                        yAxisIndex: 3,
                        type: 'line',
                        smooth: true,
                        symbol: 'image://../../../resources/img/circle.png',
                        symbolSize :15,
                        showSymbol: false,
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(240, 182, 125)'
                            },
                            borderColor :{
                                color: 'rgb(240, 182, 125)'
                            }
                        },
                        data: oilOne,
                        markPoint: {
                            symbolSize: [48,61],
                            symbolOffset: [0,-32],
                            silent: true,
                            data: [
                                {
                                    yAxis: oil_before,
                                    xAxis: time_before,
                                    symbol:'image://../../../resources/img/oil_before.png',
                                    label :{
                                        normal :{
                                            show:true,
                                            formatter : "",
                                        }
                                    }
                                },
                                {
                                    yAxis: oil_after,
                                    xAxis: time_after,
                                    symbol:'image://../../../resources/img/oil_after.png',
                                    label :{
                                        normal :{
                                            show:true,
                                            formatter : "",
                                        }
                                    }
                                },
                            ]
                        }
                    },
                    {
                        name: '副油箱',
                        yAxisIndex: 3,
                        type: 'line',
                        smooth: true,
                        // symbol: 'circle',
                        symbol: 'image://../../../resources/img/circle.png',
                        symbolSize :15,
                        showSymbol: false,
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(150, 78, 7)'
                            }
                        },
                        data: oilTwo,
                        markPoint: {
                            symbolSize: [48,61],
                            symbolOffset: [0,-32],
                            silent: true,
                            data: [
                                {
                                    yAxis: oil_before2,
                                    xAxis: time_before2,
                                    symbol:'image://../../../resources/img/oil_before.png',
                                    label :{
                                        normal :{
                                            show:true,
                                            formatter : ""
                                        }
                                    }
                                },
                                {
                                    yAxis: oil_after2,
                                    xAxis: time_after2,
                                    symbol:'image://../../../resources/img/oil_after.png',
                                    label :{
                                        normal :{
                                            show:true,
                                            formatter : ""
                                        }
                                    }
                                },
                            ]
                        }
                    },
                    {
                        name: '燃油温度1',
                        yAxisIndex: 1,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(245, 0, 0)'
                            }
                        },
                        data: oilOneTemp
                    },
                    {
                        name: '环境温度1',
                        yAxisIndex: 1,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(244, 168, 177)'
                            }
                        },
                        data: ENVOneTemp
                    },
                    {
                        name: '燃油温度2',
                        yAxisIndex: 1,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(240, 89, 89)'
                            }
                        },
                        data: oilTwoTemp
                    },
                    {
                        name: '环境温度2',
                        yAxisIndex: 1,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(168, 4, 23)'
                            }
                        },
                        data: ENVTwoTemp
                    },
                    {
                        name: '空调',
                        yAxisIndex: 2,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(199, 209, 223)'
                            }
                        },
                        areaStyle: {
                            normal: {
                                color: 'rgba(199, 209, 223,0.9)'
                            }
                        },
                        data: envTemp,
                    }
                ]
            };
            myChart.setOption(option);
            myChart.on('click', olicalibrationPages.chartsEvent);
            // 图例点击事件
            myChart.dispatchAction({
                type: 'legendselectchanged',
            },{
                type: 'datazoom',
            });
            myChart.on('legendselectchanged',olicalibrationPages.legendEvent);
            myChart.on('datazoom',olicalibrationPages.dataZoomBack);
            window.onresize = myChart.resize;
        },
        // 图表鼠标事件
        chartsEvent: function(params){
            startZoom = 0;
            endZoom = 100;
            var curBox = $("#curBox").val();
            var oilValue_num = params.data;
            var oilValue_index = params.dataIndex;
            var oilValue_height = oilHeight[oilValue_index];
            var oilValue_height2 = oilHeight2[oilValue_index];
            if (isNaN(oilValue_num)) {
                layer.msg(ascendOilNull);
                return;
            }
            layer.msg('<label style="cursor:pointer;"><input type="radio" data-state="1" name="oilState" />加油前</label>&ensp;&ensp;<label style="cursor:pointer;"><input data-state="2" name="oilState" type="radio" />加油后</label>', {
                time: 10000, // 10s后自动关闭
                btn: ['确定', '取消'],
                yes: function(e){

                    var value = $("input[name='oilState']:checked").attr("data-state");
                    var tank1LastTime = $("#tank1LastTime").val();
                    var tank2LastTime = $("#tank2LastTime").val();
                    if (curBox == "1") { // 油箱1
                        if(value == 1){ // 加油前
                            if (tank1LastTime != "" && params.name < tank1LastTime) {
                                layer.msg("【" + $("#brands3").val() + "-油箱1】" + tank1LastTime + "之前的数据已经被标定过，不能重复标定！", {btn : ["确定"]});
                                return;
                            } else if (!olicalibrationPages.compareTimePoint(params.name, time_after)) {
                                layer.msg(ascendTimeComp);
                                return;
                            } else if (!olicalibrationPages.compareOilValue(oilValue_num, oil_after)) {
                                layer.msg(ascendOilComp);
                                return;
                            }
                                time_before = params.name;
                                oil_before = oilValue_num;
                                $("#oil_before_height").val(oilValue_height + " mm");
                                $("#oil_before_value").val(oilValue_num + " L");
                                // 用于油箱1和油箱2切换时值的回显
                                $("#ascendCalibration_tank1_oilLevelHeight_before").val(oilValue_height + " mm");
                                $("#ascendCalibration_tank1_oilValue_before").val(oilValue_num + " L");
                                layer.closeAll();

                        }else if(value == 2){ // 加油后
                            if (tank1LastTime != "" && params.name < tank1LastTime) {
                                layer.msg("【" + $("#brands3").val() + "-油箱1】" + tank1LastTime + "之前的数据已经被标定过，不能重复标定！");
                                return;
                            } else if (!olicalibrationPages.compareTimePoint(time_before, params.name)) {
                                layer.msg(ascendTimeComp);
                                return;
                            } else if (!olicalibrationPages.compareOilValue(oil_before, oilValue_num)) {
                                layer.msg(ascendOilComp);
                                return;
                            }
                                time_after = params.name;
                                oil_after = oilValue_num;
                                $("#oil_after_height").val(oilValue_height + " mm");
                                $("#oil_after_value").val(oilValue_num + " L");
                                // 用于油箱1和油箱2切换时值的回显
                                $("#ascendCalibration_tank1_oilLevelHeight_after").val(oilValue_height + " mm");
                                $("#ascendCalibration_tank1_oilValue_after").val(oilValue_num + " L");
                                layer.closeAll();

                        }
                    } else { // 油箱2
                        if(value == 1){ // 加油前
                            if (tank2LastTime != "" && params.name < tank2LastTime) {
                                layer.msg("【" + $("#brands3").val() + "-油箱2】" + tank2LastTime + "之前的数据已经被标定过，不能重复标定！");
                                return;
                            } else if (!olicalibrationPages.compareTimePoint(params.name, time_after2)) {
                                layer.msg(ascendTimeComp);
                                return;
                            } else if (!olicalibrationPages.compareOilValue(oilValue_num, oil_after2)) {
                                layer.msg(ascendOilComp);
                                return;
                            }
                                time_before2 = params.name;
                                oil_before2 = oilValue_num;
                                $("#oil_before_height").val(oilValue_height2 + " mm");
                                $("#oil_before_value").val(oilValue_num + " L");
                                // 用于油箱1和油箱2切换时值的回显
                                $("#ascendCalibration_tank2_oilLevelHeight_before").val(oilValue_height2 + " mm");
                                $("#ascendCalibration_tank2_oilValue_before").val(oilValue_num + " L");
                                layer.closeAll();

                        }else if(value == 2){ // 加油后
                            if (tank2LastTime != "" && params.name < tank2LastTime) {
                                layer.msg("【" + $("#brands3").val() + "-油箱2】" + tank2LastTime + "之前的数据已经被标定过，不能重复标定！");
                                return;
                            } else if (!olicalibrationPages.compareTimePoint(time_before2, params.name)) {
                                layer.msg(ascendTimeComp);
                                return;
                            } else if (!olicalibrationPages.compareOilValue(oil_before2, oilValue_num)) {
                                layer.msg(ascendOilComp);
                                return;
                            }
                                time_after2 = params.name;
                                oil_after2 = oilValue_num;
                                $("#oil_after_height").val(oilValue_height2 + " mm");
                                $("#oil_after_value").val(oilValue_num + " L");
                                // 用于油箱1和油箱2切换时值的回显
                                $("#ascendCalibration_tank2_oilLevelHeight_after").val(oilValue_height2 + " mm");
                                $("#ascendCalibration_tank2_oilValue_after").val(oilValue_num + " L");
                                layer.closeAll();

                        }
                    }
                    olicalibrationPages.echartsData();
                    if ($("#oil_before_value").val() != "" && $("#oil_after_value").val() != "") {
                        // 计算理论加油量
                        olicalibrationPages.calculateTheoryAddOil();
                    }
                },
                btn2: function(){
                    layer.closeAll();
                }
            });
        },
        // 缩放事件
        dataZoomBack: function(params){
            if(params.batch != undefined){
                startZoom = params.batch[0].start;
                endZoom = params.batch[0].end;
            }else{
                startZoom = params.start;
                endZoom = params.end;
            }
        },
        // 图例点击事件
        legendEvent: function(params){
            var legendFlag = false;
            if(!myChart){
                return;
            };
            var dataList2 = $("#dataList2");
            var data2Length = dataList2.find("tr").length;
            if (params.name == "主油箱") {
                if(params.selected["主油箱"] == true){
                    selected["主油箱"] = true;
                    selected["副油箱"] = false;
                    legendFlag = true;
                    $("#tankRadio5").click();
                    olicalibrationPages.tankRadio5Click();
                }
            } else if (params.name == "副油箱"){
                if(params.selected["副油箱"] == true && data2Length > 0){
                    selected["主油箱"] = false;
                    selected["副油箱"] = true;
                    legendFlag = true;
                    $("#tankRadio6").click();
                    olicalibrationPages.tankRadio6Click();
                } else {
                    olicalibrationPages.tankRadio5Click();
                    layer.msg("当前车辆【" + $("#brands3").val() + "】只绑定了一个油箱！");
                    return;
                }
            }
            if (params.name == "燃油温度2") {
                if (params.selected["燃油温度2"] == true && data2Length <= 0) {
                    params.selected["燃油温度2"] = false;
                    selected = {
                        '总油量' : true,
                        '里程' : true,
                        '速度' : true,
                        '主油箱' : true,
                        '副油箱':false,
                        '燃油温度1' : true,
                        '环境温度1' : false,
                        '燃油温度2' : false,
                        '环境温度2' : false,
                        '空调' : true
                    };
                    if(!myChart){
                        return;
                    };
                    var optionData = myChart.getOption();
                    optionData.legend.selected = selected;
                    olicalibrationPages.echartsData();
                    layer.msg("当前车辆【" + $("#brands3").val() + "】只绑定了一个油箱！");
                    return;
                }
            }
            if (params.name == "环境温度2") {
                if (params.selected["环境温度2"] == true && data2Length <= 0) {
                    params.selected["环境温度2"] = false;
                    selected = {
                        '总油量' : true,
                        '里程' : true,
                        '速度' : true,
                        '主油箱' : true,
                        '副油箱':false,
                        '燃油温度1' : true,
                        '环境温度1' : false,
                        '燃油温度2' : false,
                        '环境温度2' : false,
                        '空调' : true
                    };
                    if(!myChart){
                        return;
                    };
                    var optionData = myChart.getOption();
                    optionData.legend.selected = selected;
                    olicalibrationPages.echartsData();
                    layer.msg("当前车辆【" + $("#brands3").val() + "】只绑定了一个油箱！");
                    return;
                }
            }
            if (legendFlag) {
                var optionData = myChart.getOption();
                optionData.legend.selected = selected;
                olicalibrationPages.echartsData();
            }
        },
        // 追溯标定比较加油前和加油后的时间节点：加油后的时间节点必须大于加油前的时间节点
        compareTimePoint : function (before, after) {
            if (before != "" && after != "") {
                var start = before;
                var end = after;
                if (end < start) {
                    return false;
                }
            }
            return true;
        },
        // 追溯标定比较加油前和加油后的油量值：加油后的测量值必须大于加油前的油量值
        compareOilValue : function (before, after) {
            if (before != "" && after != "") {
                if (parseFloat(after) <= parseFloat(before)) {
                    return false;
                }
            }
            return true;
        },
        // 判断日期是否符合要求
        validates: function(){
            return $("#oilist").validate({
                rules: {
                    endTime: {
                        required: true,
                        compareDate: "#timeInterval",
                        compareDateDiff: "#timeInterval"
                    },
                    startTime: {
                        required: true
                    },
                    brands3:{
                        required: true,
                        rangelength:[2, 20],
                    }
                },
                messages: {
                    endTime: {
                        required: queryEndtimeNull,
                        compareDate: queryEndCompStart,
                        compareDateDiff: queryPeriod
                    },
                    startTime: {
                        required: queryStarttimeNull,
                    },
                    brands3:{
                        required: brandNullMsg,
                        rangelength: vehicleBrandError
                    }
                }
            }).form();
        },
        // 表格初始化
        getTable: function(table, data){
            myTable = $(table).DataTable({
                "destroy": true,
                "dom": 'tiprl',// 自定义显示项
                "data": data,
                "lengthChange": true,// 是否允许用户自定义显示数量
                "bPaginate": true, // 翻页功能
                "bFilter": false, // 列筛序功能
                "searching": true,// 本地搜索
                "ordering": false, // 排序功能
                "Info": true,// 页脚信息
                "autoWidth": true,// 自动宽度
                "dom" : "t" + "<'row'<'col-md-2 col-sm-12 col-xs-12 noPadding'l><'col-md-4 col-sm-12 col-xs-12 noPadding'i><'col-md-6 col-sm-12 col-xs-12 noPadding'p>>",
                "oLanguage": {// 国际语言转化
                    "oAria": {
                        "sSortAscending": " - click/return to sort ascending",
                        "sSortDescending": " - click/return to sort descending"
                    },
                    "sLengthMenu": "显示 _MENU_ 记录",
                    "sInfo": "当前显示 _START_ 到 _END_ 条，共 _TOTAL_ 条记录。",
                    "sZeroRecords": "我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？",
                    "sEmptyTable": "我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？",
                    "sLoadingRecords": "正在加载数据-请等待...",
                    "sInfoEmpty": "当前显示0到0条，共0条记录",
                    "sInfoFiltered": "（数据库中共为 _MAX_ 条记录）",
                    "sProcessing": "<img src='../resources/user_share/row_details/select2-spinner.gif'/> 正在加载数据...",
                    "sSearch": "模糊查询：",
                    "sUrl": "",
                    "oPaginate": {
                        "sFirst": "首页",
                        "sPrevious": " 上一页 ",
                        "sNext": " 下一页 ",
                        "sLast": " 尾页 "
                    }
                },
                "order": [
                    [0, null]
                ],// 第一列排序图标改为默认

            });
            myTable.on('order.dt search.dt', function () {
                myTable.column(0, {
                    search: 'applied',
                    order: 'applied'
                }).nodes().each(function (cell, i) {
                    cell.innerHTML = i + 1;
                });
            }).draw();
        },
        // 页面刷新或关闭时的操作
        pageOnbeforeonload : function () {
            window.onbeforeunload=function (){
                // 更新上一次选择的车辆标定状态
                if (!calibrationFlag) { // 如果在标定中，那么不更新上一个车的标定状态
                    olicalibrationPages.updateCalibrationStatus($("#vehicleId").val());
                }
            }
            window.onload=function (){
                // 更新上一次选择的车辆标定状态
                if (!calibrationFlag) { // 如果在标定中，那么不更新上一个车的标定状态
                    if(window.name.length>0){
                        olicalibrationPages.updateCalibrationStatus(window.name);
                        window.name = "";
                    }
                }
            }
        },
        // 重新标定点击
        againCalibrationClick: function(){
            var editOrRebuild = $("#editOrRebuild").val();
            var brands = $("#brands").val(); // 修正标定选择车辆-表示已经开始标定
            var brands2 = $("#brands2").val(); // 重新标定选择车辆-表示已经开始标定
            var brands3 = $("#brands3").val(); // 追溯标定选择车辆-表示已经开始标定
            if (brands2!=""||(brands != "" || brands3 != "")) {
                layer.confirm(cancleConfirmMsg, {btn : ["确认", "取消"]}, function () {
                    // 切换后，将上一页签标定的车辆的标定状态还原
                    olicalibrationPages.updateCalibrationStatus($("#vehicleId").val());
                    olicalibrationPages.againInit();
                });
            } else {
                olicalibrationPages.againInit();
            }
        },
        // 修正标定点击
        amendmentCalibrationClick: function(){
            var editOrRebuild = $("#editOrRebuild").val();
            var brands = $("#brands").val(); // 修正标定选择车辆-表示已经开始标定
            var brands2 = $("#brands2").val(); // 重新标定选择车辆-表示已经开始标定
            var brands3 = $("#brands3").val(); // 追溯标定选择车辆-表示已经开始标定
            if (brands!="" || (brands2 != "" || brands3 != "")) {
                layer.confirm(cancleConfirmMsg, {btn : ["确认", "取消"]}, function () {
                    // 切换后，将上一页签标定的车辆的标定状态还原
                    olicalibrationPages.updateCalibrationStatus($("#vehicleId").val());
                    olicalibrationPages.amendmentInit();
                });
            } else {
                olicalibrationPages.amendmentInit();
            }
        },
        // 追溯标定点击
        ascendDemarcateClick : function () {
            var editOrRebuild = $("#editOrRebuild").val();
            var brands = $("#brands").val(); // 修正标定选择车辆-表示已经开始标定
            var brands2 = $("#brands2").val(); // 重新标定选择车辆-表示已经开始标定
            var brands3 = $("#brands3").val(); // 追溯标定选择车辆-表示已经开始标定
            if (brands3!="" || (brands != "" || brands2 != "")) {
                layer.confirm(cancleConfirmMsg, {btn : ["确认", "取消"]}, function () {
                    // 切换后，将上一页签标定的车辆的标定状态还原
                    olicalibrationPages.updateCalibrationStatus($("#vehicleId").val());
                    olicalibrationPages.ascendInit();
                });
            } else {
                olicalibrationPages.ascendInit();
            }
        },
        // 修正标定初始化操作
        amendmentInit : function () {
            $('#curBox').val('1');
            layer.closeAll();
            // 当前点击对象对应的操作页面显示与隐藏
            $("#amendment").show();
            $("#again").hide();
            $("#ascend").hide();
            $("#amendmentCalibration").attr("class", "active");
            $("#againCalibration").attr("class", "");
            $("#ascendDemarcate").attr("class", "");
            $("#brands").val("");
            $("#brands2").val("");
            $("#brands3").val("");
            $("#vehicleId").val("");
            olicalibrationPages.initForm();
            olicalibrationPages.initForm2();
            olicalibrationPages.initForm3();
            $("#editOrRebuild").val("1");
            $("#dataList").empty();
            $("#dataList2").empty();
            // 修正标定隐藏重新标定的保存按钮
            reCalBtnDiv.css("display", "none");
            $("#echartsArea").hide();
            $("#demarcateData").hide();
            sendStatus4Amendment.val('');// 下发状态
            $("#oilBoxVehicleId2").val('');
            $("#oilBoxVehicleId1").val('');
            $("#tankRadio1").children().prop('checked',true);
            $("#tankOneCutover").addClass('active');
            $("#tankTwoCutover").removeClass('active');
            refreshDataBtn3.hide();
            compareBtn.show();
            list1={
                obj:{}
            };
            list2={
                obj:{}
            };
            $('#tankWraper1').removeClass('col-md-6').addClass('col-md-12');
            $('#tankWraper2').removeClass('col-md-6').addClass('col-md-12');
            $('#tankWraper3').addClass('hidden');
            $('#tankWraper4').addClass('hidden');
            $('#tankTitle1').show();
            $('#tankTitle2').show();
        },
        // 重新标定初始化操作
        againInit : function () {
            $('#curBox').val('1');
            layer.closeAll();
            // 当前点击对象对应的操作页面显示与隐藏
            $("#amendment").hide();
            $("#again").show();
            $("#ascend").hide();
            $("#amendmentCalibration").attr("class", "");
            $("#againCalibration").attr("class", "active");
            $("#ascendDemarcate").attr("class", "");
            $("#brands").val("");
            $("#brands2").val("");
            $("#brands3").val("");
            $("#vehicleId").val("");
            olicalibrationPages.initForm();
            olicalibrationPages.initForm2();
            olicalibrationPages.initForm3();
            $("#editOrRebuild").val("2");
            num = 0; // 油箱1、油箱2总记录数，设置input的id时用
            seqNo = 1; // 标定数据序号-油箱1
            seqNo2 = 1; // 标定数据序号-油箱2
            initFlag = true; // 获取初始数据标识
            tempOilValue = 0; // 油箱1临时油量值
            tempOilValue2 = 0; // 油箱2临时油量值
            $("#dataList").empty();
            $("#dataList2").empty();
            list1={
                obj:{}
            };
            list2={
                obj:{}
            };
            // 重新标定显示保存按钮
            reCalBtnDiv.css("display", "");
            $("#echartsArea").hide();
            $("#demarcateData").hide();
            sendStatus4Amendment.val('');// 下发状态
            $("#oilBoxVehicleId2").val('');
            $("#oilBoxVehicleId1").val('');
            $("#tankRadio3").children().prop('checked',true);
            $("#tankOneCutover").addClass('active');
            $("#tankTwoCutover").removeClass('active');
            refreshDataBtn3.hide();
            compareBtn.show();
            $('#tankWraper1').removeClass('col-md-6').addClass('col-md-12');
            $('#tankWraper2').removeClass('col-md-6').addClass('col-md-12');
            $('#tankWraper3').addClass('hidden');
            $('#tankWraper4').addClass('hidden');
            $('#tankTitle1').hide();
            $('#tankTitle2').hide();
        },
        // 追溯标定初始化操作
        ascendInit : function () {
            $('#curBox').val('1');
            layer.closeAll();
            // 当前点击对象对应的操作页面显示与隐藏
            $("#amendment").hide();
            $("#again").hide();
            $("#ascend").show();
            $("#amendmentCalibration").attr("class", "");
            $("#againCalibration").attr("class", "");
            $("#ascendDemarcate").attr("class", "active");
            $("#brands").val("");
            $("#brands2").val("");
            $("#brands3").val("");
            $("#vehicleId").val("");
            olicalibrationPages.initForm();
            olicalibrationPages.initForm2();
            olicalibrationPages.initForm3();
            $("#editOrRebuild").val("3");
            $("#dataList").empty();
            $("#dataList2").empty();
            // 修正标定隐藏重新标定的保存按钮
            reCalBtnDiv.css("display", "none");
            $("#echartsArea").show();
            $("#demarcateData").show();
            sendStatus4Amendment.val('');// 下发状态
            $("#oilBoxVehicleId2").val('');
            $("#oilBoxVehicleId1").val('');
            $("#tankRadio5").children().prop('checked',true);
            $("#tankOneCutover").addClass('active');
            $("#tankTwoCutover").removeClass('active');
            refreshDataBtn3.removeClass('hidden');
            refreshDataBtn3.show();
            compareBtn.hide();
            list1={
                obj:{}
            };
            list2={
                obj:{}
            };
            $('#tankWraper1').removeClass('col-md-12').addClass('col-md-6');
            $('#tankWraper2').removeClass('col-md-12').addClass('col-md-6');
            $('#tankWraper3').removeClass('hidden');
            $('#tankWraper4').removeClass('hidden');
            $('#tankTitle1').show();
            $('#tankTitle2').show();
            $('#brands3-error').remove();
        },
        // 油箱1点击
        tankOneCutoverClick: function(){
            startZoom = 0;
            endZoom = 100;
            $("#curBox").val("1");
            tankOne.css({"display" : "block"});
            tankTwo.css({"display" : "none"});
            var editOrRebuild = $("#editOrRebuild").val();
            if (editOrRebuild == "1") { // 修正标定
                olicalibrationPages.corCalibrationTank1();
                tankRadio1.children().click();
                // 设置"加油前点我"按钮的可点击状态
                olicalibrationPages.setSubmitBeforeBtnStatus();
            } else if (editOrRebuild == "2") { // 重新标定
                olicalibrationPages.reCalibrationTank1();
                tankRadio3.children().click();
            } else if (editOrRebuild == "3") { // 追溯修正
                olicalibrationPages.ascendCalibrationTank1();
                tankRadio5.children().click();
            }
        },
        // radio1：修正标定-油箱1点击
        tankRadio1Click : function () {
            $("#curBox").val("1");
            tankOne.css({"display" : "block"});
            tankTwo.css({"display" : "none"});
            tankOneCutover.attr("class", "active");
            tankTwoCutover.attr("class", "");
            olicalibrationPages.corCalibrationTank1();
            // 设置"加油前点我"按钮的可点击状态
            olicalibrationPages.setSubmitBeforeBtnStatus();
            // 如果还没有获取过副油箱的标定数据，此时获取
            if(!list1.obj.oilCalibration1){
                olicalibrationPages.handleGetCalibration();
            }
        },
        // radio2：重新标定-油箱1点击
        tankRadio3Click : function () {
            $("#curBox").val("1");
            tankOne.css({"display" : "block"});
            tankTwo.css({"display" : "none"});
            tankOneCutover.attr("class", "active");
            tankTwoCutover.attr("class", "");
            olicalibrationPages.reCalibrationTank1();
            // 如果还没有获取过副油箱的标定数据，此时获取
            if(!list1.obj.oilCalibration1){
                olicalibrationPages.handleGetCalibration();
            }
        },
        // 油箱2点击
        tankTwoCutoverClick: function(){
            startZoom = 0;
            endZoom = 100;
            $("#curBox").val("2");
            tankOne.css({"display" : "none"});
            tankTwo.css({"display" : "block"});
            var editOrRebuild = $("#editOrRebuild").val();
            if (editOrRebuild == "1") { // 修正标定
                olicalibrationPages.corCalibrationTank2();
                tankRadio2.children().click();
                // 设置"加油前点我"按钮的可点击状态
                olicalibrationPages.setSubmitBeforeBtnStatus();
            } else if (editOrRebuild == "2") { // 重新标定
                olicalibrationPages.reCalibrationTank2();
                tankRadio4.children().click();
            } else if (editOrRebuild == "3") { // 追溯修正
                olicalibrationPages.ascendCalibrationTank2();
                tankRadio6.children().click();
            }
        },
        tankRadio5Click: function(e){
            // 如果还没有获取过副油箱的标定数据，此时获取
            if(!list1.obj.oilCalibration1){
                olicalibrationPages.handleGetCalibration();
            }
            startZoom = 0;
            endZoom = 100;
            $("#curBox").val("1");
            tankOne.css({"display" : "block"});
            tankTwo.css({"display" : "none"});
            tankOneCutover.attr("class", "active");
            tankTwoCutover.attr("class", "");
            selected = {
                '总油量' : true,
                '里程' : true,
                '速度' : true,
                '主油箱' : true,
                '副油箱':false,
                '燃油温度1' : true,
                '环境温度1' : true,
                '燃油温度2' : false,
                '环境温度2' : false,
                '空调' : true
            };
            if(!myChart){
                return;
            };
            var optionData = myChart.getOption();
            optionData.legend.selected = selected;
            olicalibrationPages.echartsData();
            olicalibrationPages.ascendCalibrationTank1();

        },
        // radio2: 修正标定-油箱2点击
        tankRadio2Click : function () {
            $("#curBox").val("2");

            // 如果还没有获取过副油箱的标定数据，此时获取
            if(!list1.obj.oilCalibration2){
                olicalibrationPages.handleGetCalibration();
            }

            tankOne.css({"display" : "none"});
            tankTwo.css({"display" : "block"});
            tankOneCutover.attr("class", "");
            tankTwoCutover.attr("class", "active");
            olicalibrationPages.corCalibrationTank2();
            // 设置"加油前点我"按钮的可点击状态
            olicalibrationPages.setSubmitBeforeBtnStatus();
        },
        // radio4: 重新标定-油箱2点击
        tankRadio4Click : function () {
            $("#curBox").val("2");

            // 重新标定不需要获取标定数据
//				if(!list1.obj.oilCalibration2){
//					olicalibrationPages.handleGetCalibration();
//				}

            tankOne.css({"display" : "none"});
            tankTwo.css({"display" : "block"});
            tankOneCutover.attr("class", "");
            tankTwoCutover.attr("class", "active");
            olicalibrationPages.reCalibrationTank2();
        },

        tankRadio6Click: function(){
            startZoom = 0;
            endZoom = 100;
            $("#curBox").val("2");

            // 如果还没有获取过副油箱的标定数据，此时获取
            if(!list1.obj.oilCalibration2){
                olicalibrationPages.handleGetCalibration();
            }

            tankOne.css({"display" : "none"});
            tankTwo.css({"display" : "block"});
            tankOneCutover.attr("class", "");
            tankTwoCutover.attr("class", "active");
            selected = {
                '总油量' : true,
                '里程' : true,
                '速度' : true,
                '主油箱' : false,
                '副油箱':true,
                '燃油温度1' : false,
                '环境温度1' : false,
                '燃油温度2' : true,
                '环境温度2' : true,
                '空调' : true
            };
            if(!myChart){
                return;
            };
            var optionData = myChart.getOption();
            optionData.legend.selected = selected;
            olicalibrationPages.echartsData();
            olicalibrationPages.ascendCalibrationTank2();
        },
        // 设置加油前点我按钮的状态：加油后点我获取到数据之后，加油前点我就不能点了
        setSubmitBeforeBtnStatus : function () {
            $("#submitBefore").attr("disabled", false);
            if ($("#oilLevelHeight_after").val() != "" && $("#oilValue_after").val() != "") {
                $("#submitBefore").attr("disabled", true);
            }
            if ($("#submitAfter").attr("disabled") == 'disabled') {
                $("#submitBefore").attr("disabled", true);
            }
        },
        // 修正标定-油箱1数据
        corCalibrationTank1 : function () {
            $("#brands").val($("#corCalibration_brands").val());
            $("#brands").attr("data-id", $("#corCalibration_vehicleId").val());
            $("#settingParamId").val($("#corCalibration_settingParamId").val());
            $("#calibrationParamId").val($("#corCalibration_calibrationParamId").val());
            $("#transmissionParamId").val($("#corCalibration_transmissionParamId").val());
            // $("#oilBoxVehicleId1").val($("#corCalibration_tank1_oilBoxVehicleId").val());
            $("#oilLevelHeight_before").val($("#corCalibration_tank1_oilLevelHeight_before").val());
            $("#oilValue_before").val($("#corCalibration_tank1_oilValue_before").val());
            $("#oilLevelHeight_after").val($("#corCalibration_tank1_oilLevelHeight_after").val());
            $("#oilValue_after").val($("#corCalibration_tank1_oilValue_after").val());
            $("#actualAddOil").val($("#corCalibration_tank1_actualAddOil").val());
            $("#actualAddOil").attr("placeholder", $("#corCalibration_tank1_noteMsg").val());
        },
        // 修正标定-油箱2数据
        corCalibrationTank2 : function () {
            $("#brands").val($("#corCalibration_brands").val());
            $("#oilBoxVehicleId2").val($("#corCalibration_tank2_oilBoxVehicleId").val());
            $("#oilLevelHeight_before").val($("#corCalibration_tank2_oilLevelHeight_before").val());
            $("#oilValue_before").val($("#corCalibration_tank2_oilValue_before").val());
            $("#oilLevelHeight_after").val($("#corCalibration_tank2_oilLevelHeight_after").val());
            $("#oilValue_after").val($("#corCalibration_tank2_oilValue_after").val());
            $("#actualAddOil").val($("#corCalibration_tank2_actualAddOil").val());
            $("#actualAddOil").attr("placeholder", $("#corCalibration_tank2_noteMsg").val());
        },
        // 重新标定-油箱1数据
        reCalibrationTank1 : function () {
            $("#brands2").val($("#ReCalibration_brands").val());
            $("#settingParamId").val($("#ReCalibration_settingParamId").val());
            $("#calibrationParamId").val($("#ReCalibration_calibrationParamId").val());
            $("#transmissionParamId").val($("#ReCalibration_transmissionParamId").val());
            // $("#oilBoxVehicleId1").val($("#ReCalibration_tank1_oilBoxVehicleId").val());
            $("#oilLevelHeight2_before").val($("#ReCalibration_tank1_oilLevelHeight_before").val());
            $("#oilValue2_before").val($("#ReCalibration_tank1_oilValue_before").val());
            $("#oilADHeight").val($("#ReCalibration_tank1_oilADHeight").val());
            $("#actualAddOil2").val($("#ReCalibration_tank1_actualAddOil").val());
        },
        // 重新标定-油箱2数据
        reCalibrationTank2 : function () {
            $("#brands2").val($("#ReCalibration_brands").val());
            $("#oilBoxVehicleId2").val($("#ReCalibration_tank2_oilBoxVehicleId").val());
            $("#oilLevelHeight2_before").val($("#ReCalibration_tank2_oilLevelHeight_before").val());
            $("#oilValue2_before").val($("#ReCalibration_tank2_oilValue_before").val());
            $("#oilADHeight").val($("#ReCalibration_tank2_oilADHeight").val());
            $("#actualAddOil2").val($("#ReCalibration_tank2_actualAddOil").val());
        },
        // 追溯标定-油箱1数据
        ascendCalibrationTank1 : function () {
            olicalibrationPages.calculateTheoryAddOil();
            $("#oil_before_height").val($("#ascendCalibration_tank1_oilLevelHeight_before").val());
            $("#oil_before_value").val($("#ascendCalibration_tank1_oilValue_before").val());
            $("#oil_after_height").val($("#ascendCalibration_tank1_oilLevelHeight_after").val());
            $("#oil_after_value").val($("#ascendCalibration_tank1_oilValue_after").val());
            $("#actualAddOil3").val($("#ascendCalibration_tank1_actualAddOil").val());
            $("#actualAddOil3").attr("placeholder", $("#ascendCalibration_tank1_noteMsg").val());
        },
        // 追溯标定-油箱2数据
        ascendCalibrationTank2 : function () {
            olicalibrationPages.calculateTheoryAddOil();
            $("#oil_before_height").val($("#ascendCalibration_tank2_oilLevelHeight_before").val());
            $("#oil_before_value").val($("#ascendCalibration_tank2_oilValue_before").val());
            $("#oil_after_height").val($("#ascendCalibration_tank2_oilLevelHeight_after").val());
            $("#oil_after_value").val($("#ascendCalibration_tank2_oilValue_after").val());
            $("#actualAddOil3").val($("#ascendCalibration_tank2_actualAddOil").val());
            $("#actualAddOil3").attr("placeholder", $("#ascendCalibration_tank2_noteMsg").val());
        },
        // 追溯标定：实际加油量change事件
        actualAddOil3Change : function () {
            var curBox = $("#curBox").val();
            var actualAddOil3 = $("#actualAddOil3").val();
            if (curBox == "1") { // 油箱1
                $("#ascendCalibration_tank1_actualAddOil").val(actualAddOil3);
            } else if (curBox == "2") {
                $("#ascendCalibration_tank2_actualAddOil").val(actualAddOil3);
            }
        },
        // 实时修正：实际加油量change事件
        actualAddOilChange : function () {
            var curBox = $("#curBox").val();
            var actualAddOil = $("#actualAddOil").val();
            if (curBox == "1") { // 油箱1
                $("#corCalibration_tank1_actualAddOil").val(actualAddOil);
            } else if (curBox == "2") {
                $("#corCalibration_tank2_actualAddOil").val(actualAddOil);
            }
        },
        // 重新标定：实际加油量change事件
        actualAddOil2Change : function () {
            var curBox = $("#curBox").val();
            var actualAddOil = $("#actualAddOil").val();
            if (curBox == "1") { // 油箱1
                $("#ReCalibration_tank1_actualAddOil").val(actualAddOil);
            } else if (curBox == "2") {
                $("#ReCalibration_tank2_actualAddOil").val(actualAddOil);
            }
        },
        // 修正标定-标定数据回显
        corCalibrationReView : function () {
            var tank1_oilHeights = $("#corCalibration_tank1_oilLevelHeights").val();
            var tank1_oilValues = $("#corCalibration_tank1_oilValues").val();
            var tank1_base_oilValues = $("#corCalibration_tank1_base_oilValues").val();
            var tank2_oilHeights = $("#corCalibration_tank2_oilLevelHeights").val();
            var tank2_oilValues = $("#corCalibration_tank2_oilValues").val();
            var tank2_base_oilValues = $("#corCalibration_tank2_base_oilValues").val();
            var editOrRebuild = $("#editOrRebuild").val();
            $("#dataList").empty();
            if (tank1_oilHeights != null && tank1_oilHeights != "" && tank1_oilHeights != 'undefined' && tank1_oilHeights.length > 0) {
                var tank1_oilHeights_arr = tank1_oilHeights.split(",");
                var tank1_oilValues_arr = tank1_oilValues.split(",");
                var tank1_base_oilValues_arr = tank1_base_oilValues.split(",");
                // 设置默认样式
                tankOne.css({"display" : "block"});
                tankTwo.css({"display" : "none"});
                tankOneCutover.attr("class", "active");
                tankTwoCutover.attr("class", "");
                if (editOrRebuild == "1") { // 修正标定
                    tankRadio1.children().click();
                } else { // 重新标定
                    tankRadio3.children().click();
                }
                if (editOrRebuild == "1") { // 修正标定，才显示之前的标定
                    for (var i=0; i<tank1_oilHeights_arr.length; i++) {
                        var str = "";
                        str += "<tr class='odd'>";
                        str += "<td class='text-center'>" + (i+1) + "</td>"; // 序号
                        str += "<td class='text-center'>" +
                            "<input readonly name='oilLevelHeights' id='oilLevelHeights"+i+"' value='" + toFixed(tank1_oilHeights_arr[i],2,true) + "' onkeyup='value=value.replace(/[^0-9]/g,\"\")' maxlength='4' style='border:none;width:80%;text-align:center;background-color:transparent;' />" +
                            "</td>"; // 车牌号
                        str += "<td class='text-center'>" +
                            "<input readonly name='oilValues' id='oilValues"+i+"' value='" + toFixed(tank1_oilValues_arr[i],1,true) + "' onkeyup='value=value.replace(/[^0-9.]/g,\"\")' maxlength='10' style='border:none;width:80%;text-align:center;background-color:transparent;' />" +
                            "<input type='hidden' name='tempOilValues' value='" + tank1_base_oilValues_arr[i] + "' onkeyup='value=value.replace(/[^0-9.]/g,\"\")' maxlength='10' style='border:none;width:80%;text-align:center;background-color:transparent;' />" +
                            "</td>"; // 车辆类型
                        str += "</tr>";
                        $("#dataList").append(str);
                    }
                }
            }
            $("#dataList2").empty();
            if (tank2_oilHeights != null && tank2_oilHeights != "" && tank2_oilHeights != 'undefined' && tank2_oilHeights.length > 0) {
                var tank2_oilHeights_arr = tank2_oilHeights.split(",");
                var tank2_oilValues_arr = tank2_oilValues.split(",");
                var tank2_base_oilValues_arr = tank2_base_oilValues.split(",");

                // 显示油箱2
                tankTwoCutover.css({"display" : ""});
                tankTwo.css({"display" : "none"});
                tankRadio2.css({"display" : ""});
                tankRadio4.css({"display" : ""});

                if (editOrRebuild == "1") { // 修正标定，才显示之前的标定
                    for (var i=0; i<tank2_oilHeights_arr.length; i++) {
                        var str = "";
                        str += "<tr class='odd'>";
                        str += "<td class='text-center'>" + (i+1) + "</td>"; // 序号
                        str += "<td class='text-center'>" +
                            "<input readonly name='oilLevelHeights2' id='oilLevelHeights2"+i+"' value='" + toFixed(tank2_oilHeights_arr[i],2,true) + "' onkeyup='value=value.replace(/[^0-9]/g,\"\")' maxlength='4' style='border:none;width:80%;text-align:center;background-color:transparent;' />" +
                            "</td>"; // 车牌号
                        str += "<td class='text-center'>" +
                            "<input readonly name='oilValues2' id='oilValues2"+i+"' value='" + toFixed(tank2_oilValues_arr[i],1,true) + "' onkeyup='value=value.replace(/[^0-9.]/g,\"\")' maxlength='10' style='border:none;width:80%;text-align:center;background-color:transparent;' />" +
                            "<input type='hidden' name='tempOilValues2' value='" + tank2_base_oilValues_arr[i] + "' onkeyup='value=value.replace(/[^0-9.]/g,\"\")' maxlength='10' style='border:none;width:80%;text-align:center;background-color:transparent;' />" +
                            "</td>"; // 车辆类型
                        str += "</tr>";
                        $("#dataList2").append(str);
                    }
                }
            } else {
                // 隐藏油箱2
                tankTwoCutover.css({"display" : "none"});
                tankTwo.css({"display" : "none"});
                tankRadio2.css({"display" : "none"});
                tankRadio4.css({"display" : "none"});
            }
        },
        // 重新标定-标定数据回显
        reCalibrationReView : function () {
            var tank1_oilHeights = $("#ReCalibration_tank1_oilLevelHeights").val();
            var tank1_oilValues = $("#ReCalibration_tank1_oilValues").val();
            var tank2_oilHeights = $("#ReCalibration_tank2_oilLevelHeights").val();
            var tank2_oilValues = $("#ReCalibration_tank2_oilValues").val();
            seqNo = $("#ReCalibration_tank1_seqNo").val();
            seqNo2 = $("#ReCalibration_tank2_seqNo").val();
            num = $("#ReCalibration_num").val();

            var editOrRebuild = $("#editOrRebuild").val();
            $("#dataList").empty();
            if (tank1_oilHeights != null && tank1_oilHeights != "" && tank1_oilHeights != 'undefined' && tank1_oilHeights.length > 0) {
                var tank1_oilHeights_arr = tank1_oilHeights.split(",");
                var tank1_oilValues_arr = tank1_oilValues.split(",");
                // 设置默认样式
                tankOne.css({"display" : "block"});
                tankTwo.css({"display" : "none"});
                tankOneCutover.attr("class", "active");
                tankTwoCutover.attr("class", "");
                if (editOrRebuild == "1") { // 修正标定
                    tankRadio1.children().click();
                } else { // 重新标定
                    tankRadio3.children().click();
                }
                for (var i=tank1_oilHeights_arr.length-1; i>0; i--) {
                    var str = "";
                    str += "<tr class='odd'>";
                    str += "<td class='text-center'>" + (i) + "</td>"; // 序号
                    str += "<td class='text-center'>" +
                        "<input readonly name='oilLevelHeights' id='oilLevelHeights"+i+"' value='" + toFixed(tank1_oilHeights_arr[i-1],2,true) + "' onkeyup='value=value.replace(/[^0-9]/g,\"\")' maxlength='4' style='border:none;width:80%;text-align:center;background-color:transparent;' />" +
                        "</td>"; // 车牌号
                    str += "<td class='text-center'>" +
                        "<input readonly name='oilValues' id='oilValues"+i+"' value='" + toFixed(tank1_oilValues_arr[i-1],2,true) + "' onkeyup='value=value.replace(/[^0-9.]/g,\"\")' maxlength='10' style='border:none;width:80%;text-align:center;background-color:transparent;' />" +
                        "</td>"; // 车辆类型
                    str += "</tr>";
                    $("#dataList").append(str);
                }
            }
            $("#dataList2").empty();
            if ($("#ReCalibration_tank2_oilBoxVehicleId").val() != "") {
                // 显示油箱2
                tankTwoCutover.css({"display" : ""});
                tankTwo.css({"display" : "none"});
                tankRadio2.css({"display" : ""});
                tankRadio4.css({"display" : ""});
            } else {
                // 隐藏油箱2
                tankTwoCutover.css({"display" : "none"});
                tankTwo.css({"display" : "none"});
                tankRadio2.css({"display" : "none"});
                tankRadio4.css({"display" : "none"});
            }
            if (tank2_oilHeights != null && tank2_oilHeights != "" && tank2_oilHeights != 'undefined' && tank2_oilHeights.length > 0) {
                var tank2_oilHeights_arr = tank2_oilHeights.split(",");
                var tank2_oilValues_arr = tank2_oilValues.split(",");
                for (var i=tank2_oilHeights_arr.length-1; i>0; i--) {
                    var str = "";
                    str += "<tr class='odd'>";
                    str += "<td class='text-center'>" + (i) + "</td>"; // 序号
                    str += "<td class='text-center'>" +
                        "<input readonly name='oilLevelHeights2' id='oilLevelHeights2"+i+"' value='" + toFixed(tank2_oilHeights_arr[i-1],2,true) + "' onkeyup='value=value.replace(/[^0-9]/g,\"\")' maxlength='4' style='border:none;width:80%;text-align:center;background-color:transparent;' />" +
                        "</td>"; // 车牌号
                    str += "<td class='text-center'>" +
                        "<input readonly name='oilValues2' id='oilValues2"+i+"' value='" + toFixed(tank2_oilValues_arr[i-1],1,true) + "' onkeyup='value=value.replace(/[^0-9.]/g,\"\")' maxlength='10' style='border:none;width:80%;text-align:center;background-color:transparent;' />" +
                        "</td>"; // 车辆类型
                    str += "</tr>";
                    $("#dataList2").append(str);
                }
            }
        },
        // 初始化车辆列表
        initVehicleInfoList : function() {
            // 初始化车辆数据
            var selectList = {value: []};
            if (vehicleList != null && vehicleList.length > 0) {
                for (var i=0; i< vehicleList.length; i++) {
                    var obj = {};
                    obj.id = vehicleList[i].vehicleId;
                    obj.name = vehicleList[i].brand;
                    if(!isPlateNo(obj.name))
                        {continue;}
                    obj.oilVehicleId = vehicleList[i].id;
                    obj.settingParamId = vehicleList[i].settingParamId;
                    obj.calibrationParamId = vehicleList[i].calibrationParamId;
                    obj.transmissionParamId = vehicleList[i].transmissionParamId;
                    dataList.value.push(obj);

                    // 车辆下拉列表显示,去除重复数据
                    if (selectList.value != null && selectList.value.length > 0) {
                        var f = false;
                        for (var j=0; j<selectList.value.length; j++) {
                            if (obj.id == selectList.value[j].id) {
                                f = true;
                                break;
                            }
                        }
                        if (!f) {
                            selectList.value.push(obj);
                        }
                    } else {
                        selectList.value.push(obj);
                    }
                }
            }
            $("#brands").bsSuggest({
                indexId: 1, // data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, // data.value 的第几个数据，作为input输入框的内容
                idField: "id",
                keyField: ["name"],
                effectiveFields: ["name"],
                searchFields:["id"],
                showHeader: false,
                data: selectList
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                // 订阅车辆信息参数
                $("#ulHide").hide();
                params = [];
                var vidObj = new Object();
                vidObj.vehicleID = keyword.id;
                params.push(vidObj);
                var lastVehicleId = $("#vehicleId").val(); // 上一个选择的车辆
                if (lastVehicleId != keyword.id) {
                    // 选择车车辆之前，把上一次选择的车辆的标定状态还原
                    if (typeof(lastVehicleId) != undefined && typeof(lastVehicleId) != null && lastVehicleId.length>0 && lastVehicleId != keyword.id && !calibrationFlag) {
                        olicalibrationPages.updateCalibrationStatus(lastVehicleId);
                    }
                    // 重新选择一个车牌后，清空所有表单
                    olicalibrationPages.initForm();
                    $("#corCalibration_brands").val(keyword.key);
                    $("#corCalibration_vehicleId").val(keyword.id);

                    // 判断车辆是否绑定油箱和传感器
                    olicalibrationPages.checkIsBondOilBox(keyword.id);
                    if(calibrationFlag==true){
                        return
                    }
                    $("#vehicleId").val(keyword.id);
                    // 当选择车牌
                    olicalibrationPages.selectVehicleOperator(keyword.id, dataList);

                }

            }).on('onUnsetSelectValue', function () {
                if (!calibrationFlag) {
                    olicalibrationPages.updateCalibrationStatus($("#vehicleId").val());
                    olicalibrationPages.initForm();
                }
            });
            $("#brands2").bsSuggest({
                indexId: 1, // data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, // data.value 的第几个数据，作为input输入框的内容
                idField: "id",
                keyField: "name",
                effectiveFields: ["name"],
                searchFields:["id"],
                data: selectList
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                // 订阅车辆信息参数
                $("#ulHide2").hide();
                params = [];
                var vidObj = new Object();
                vidObj.vehicleID = keyword.id;
                params.push(vidObj);
                var lastVehicleId = $("#vehicleId").val(); // 上一个选择的车辆的标定状态
                if (lastVehicleId != keyword.id) {
                    // 选择车车辆之前，把上一次选择的车辆的标定状态还原
                    if (typeof(lastVehicleId) != undefined && typeof(lastVehicleId) != null && lastVehicleId.length>0 && lastVehicleId != keyword.id && !calibrationFlag) {
                        olicalibrationPages.updateCalibrationStatus(lastVehicleId);
                    }
                    // 重新选择一个车牌后，清空所有表单
                    olicalibrationPages.initForm2();
                    $("#ReCalibration_brands").val(keyword.key);
                    $("#ReCalibration_vehicleId").val(keyword.id);
                    // 判断车辆是否绑定油箱和传感器
                    olicalibrationPages.checkIsBondOilBox(keyword.id);
                    if(calibrationFlag==true){
                        return
                    }
                    $("#vehicleId").val(keyword.id);
                    // 当选择车牌
                    olicalibrationPages.selectVehicleOperator(keyword.id, dataList);

                }
            }).on('onUnsetSelectValue', function () {
                if (!calibrationFlag) {
                    olicalibrationPages.updateCalibrationStatus($("#vehicleId").val());
                    olicalibrationPages.initForm2();
                }
            });
            $("#brands3").bsSuggest({
                indexId: 1, // data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, // data.value 的第几个数据，作为input输入框的内容
                idField: "id",
                keyField: "name",
                effectiveFields: ["name"],
                searchFields:["id"],
                data: selectList
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                $("#showClick").attr("class","fa fa-chevron-up");
                $("#ulHide3").hide();
                // 订阅车辆信息参数
                params = [];
                var vidObj = new Object();
                vidObj.vehicleID = keyword.id;
                params.push(vidObj);
                var lastVehicleId = $("#vehicleId").val(); // 上一个选择的车辆的标定状态
                if (lastVehicleId != keyword.id) {
                    // 选择车车辆之前，把上一次选择的车辆的标定状态还原
                    if (typeof(lastVehicleId) != undefined && typeof(lastVehicleId) != null && lastVehicleId.length>0 && lastVehicleId != keyword.id && !calibrationFlag) {
                        olicalibrationPages.updateCalibrationStatus(lastVehicleId);
                    }
                    // 重新选择一个车牌后，清空所有表单
                    olicalibrationPages.initForm3();
                    // 判断车辆是否绑定油箱和传感器
                    olicalibrationPages.checkIsBondOilBox(keyword.id);
                    if(calibrationFlag==true){
                        return
                    }
                    $("#vehicleId").val(keyword.id);
                    // 当选择车牌
                    olicalibrationPages.selectVehicleOperator(keyword.id, dataList);

                    // 选择车辆后，查询出当前车辆最后一次标定时的时间，然后这时间之前的数据不能进行标定
                    olicalibrationPages.getLastCalibration();


                }
            }).on('onUnsetSelectValue', function () {
                if (!calibrationFlag) {
                    olicalibrationPages.updateCalibrationStatus($("#vehicleId").val());
                    olicalibrationPages.initForm3();
                }
            });
        },
        // 获取车辆最后一次标定的时间
        getLastCalibration : function () {
            var vehicleId = $("#brands3").attr("data-id");
            var url = "/clbs/v/oilmassmgt/oilcalibration/getLastCalibration";
            var data = {"vehicleId" : vehicleId};
            json_ajax("POST", url, "json", true, data, olicalibrationPages.getLastCalibrationCallback);
        },
        // 获取车辆最后一次标定时间的回调
        getLastCalibrationCallback : function (data) {
            if(data.success){
                if (data != null && data.obj != null) {
                    $("#tank1LastTime").val(data.obj.tank1Time);
                    $("#tank2LastTime").val(data.obj.tank2Time);
                    $("#origionalTime_tank1").val(data.obj.tank1Time);
                    $("#origionalTime_tank2").val(data.obj.tank2Time);
                }
            }else{
                layer.msg(data.msg,{move:false});
            }

        },
        // 处理读取标定数据完成，无论成功或者失败，都要调用
        handleGetCalibrationComplete:function(){
            if(isRead){
                isRead=false;
                clearTimeout(layer_time);
                layer.closeAll();
            }
        },
        // 处理读取标定数据失败，弹出提示信息，并禁用所有按钮
        handleGetCalibrationFailed:function(){
            if (isRead) {
                // 调用处理读取标定数据完成
                olicalibrationPages.handleGetCalibrationComplete()

                var errMsg="读取标定数据失败，不能进行修正";
                if(inCompareMode){
                    errMsg="读取标定数据失败";
                }
                layer.msg(errMsg);
                // 禁用所有按钮
                //后来需求改了，不禁用了
//					submitBefore.attr('disabled','disabled'); // 加油前点我
//					submitAfter.attr('disabled','disabled'); // 加油后点我
//					submitBtn.attr('disabled','disabled'); // 修正标定 - 修正下发
//					corCancleBtn.attr('disabled','disabled'); // 修正标定 - 取消修正
            }
        },
        // 处理读取标定数据成功，取消禁用所有按钮，如果之前禁用了的话
        handleGetCalibrationSuccess:function(){
            if (isRead) {
                // 调用处理读取标定数据完成
                olicalibrationPages.handleGetCalibrationComplete()

                // 取消禁用所有按钮
                submitBefore.removeAttr('disabled'); // 加油前点我
                submitAfter.removeAttr('disabled'); // 加油后点我
                submitBtn.removeAttr('disabled'); // 修正标定 - 修正下发
                corCancleBtn.removeAttr('disabled'); // 修正标定 - 取消修正
            }
        },
        // 处理修正下发完成
        handleSendCalibrationComplete:function(){
            if(isRead){
                isRead=false;
                clearTimeout(layer_time);
                clearTimeout(_timeout);
                layer.closeAll();

                submitBtn.removeAttr('disabled');
                sendCompareBtn.removeAttr('disabled');
            }
        },

        // 处理修正下发失败，更新状态信息
        handleSendCalibrationFailed:function(){
            if (isRead) {
                // 调用处理读取标定数据完成
                olicalibrationPages.handleSendCalibrationComplete()
                sendStatus4Amendment.val('终端接收失败');
            }
        },
        // 订阅标定数据
        createSocket0104InfoMonitor:function (msg,sensorID) {
            var msg = $.parseJSON(msg);
            var requestStrS = {
                "desc": {
                    "cmsgSN": msg.msgId,
                    "UserName": msg.userName
                },
                "data": []
            };
            $("#msgSN").val(msg.msgId);
            headers = {"UserName": msg.userName};
            isRead=true;
            clearTimeout(_timeout);
            _timeout= window.setTimeout(olicalibrationPages.handleGetCalibrationFailed,30000);
//				if(sensorID === "1"){
//					webSocket.subscribe(headers, "/user/" + msg.userName + "/oil63041Info", olicalibrationPages.getSensor0104Param, "/app/vehicle/oil/setting", requestStrS);
//				}
//				if (sensorID === "2") {
//					webSocket.subscribe(headers, "/user/" + msg.userName + "/oil63042Info", olicalibrationPages.getSensor0104Param, "/app/vehicle/oil/setting", requestStrS);
//				}
        },
        // 处理获取设备上传数据
        getSensor0104Param:function (msg) {
            if (msg == null || !isRead)
                {return;}

            // 对比流水号
            var result = $.parseJSON(msg.body);
            var msgSNAck = result.data.msgBody.msgSNAck;

            if (msgSNAck != $("#msgSN").val()) {
                return;
            }
            // 调用读取处理完成
            olicalibrationPages.handleGetCalibrationSuccess();

            // 处理数据
            var id =result.data.msgBody.params[0].id
            if(id=="63041"||id=="63042"){// 标定参数
                clearTimeout(layer_time);
                layer.closeAll();

                // 重新组装数据格式
                var tmpList=result.data.msgBody.params[0].value.list.map(function(ele){
                    var innerEle={};
                    innerEle.oilBoxVehicleId=result.desc.vId; // 车辆ID
                    innerEle.oilLevelHeight=ele.key; // 液位高度
                    innerEle.oilValue=ele.value; // 油量值
                    return innerEle;
                });
                if(id=="63041"){
                    list1.obj.oilCalibration1=tmpList;
                }else{
                    list1.obj.oilCalibration2=tmpList;
                }
                // 处理结果，如果inCompareMode
                // 为true，说明点击了“数据对比”按钮，或者“数据对比”弹出框中的刷新按钮，此时，执行针对他俩的回调
                olicalibrationPages.callBack4GetCalibration(list1);
                if(inCompareMode===true){
                    if(inCompareMode){
                        inCompareMode=false;
                        $("#commonWin").modal({
                            remote:'/clbs/v/oilmassmgt/oilcalibration/compare',
                            show:true
                        }).on('shown.bs.modal', function (e) {
                            olicalibrationPages.callBack4Compare();
                        }).on('hidden.bs.modal', function(){
                            inCompareMode=false;
                        })
                    }

                }
                return;
            }
        },
        // 订阅车辆位置信息
        subscribeLatestLocation : function () {
            var requestStrS = {
                "desc": {
                    "MsgId": 40964,
                    "UserName": $("#userName").text()
                },
                "data": params
            };
            setTimeout(function () {
                webSocket.subscribe(headers, "/user/topic/realLocationP", olicalibrationPages.getLastOilDataCallBack, "/app/vehicle/realLocation", requestStrS);
            });
        },
        // 下发参数后，终端通用应答信息
        commonResponse : function () {
            setTimeout(function () {
                webSocket.subscribe(headers, "/user/topic/oil", olicalibrationPages.commonResponseCallback, null, null);
            });
        },
        // 终端通用应答回调
        commonResponseCallback : function (msg) {

            if (msg == null || !isRead)
                {return;}
            if (msg != null && typeof(msg) != undefined) {
                var obj = $.parseJSON(msg.body);

                if (obj != null && typeof(obj) != undefined) {
                    var msgBody = obj.data.msgBody;
                    var msgHead = obj.data.msgHead;
                    var statusText='';

                    // 判断0001
                    if(msgHead.msgID==0x0001){
                        if(msgBody.result==0){
                            statusText='终端处理中';
                        }else if(msgBody.result==1){
                            statusText='参数未生效';
                            olicalibrationPages.handleSendCalibrationComplete();
                        }else if(msgBody.result==2){
                            statusText='参数消息有误';
                            olicalibrationPages.handleSendCalibrationComplete();
                        }else if(msgBody.result==3){
                            statusText='参数不支持';
                            olicalibrationPages.handleSendCalibrationComplete();
                        }
                    }

                    if ( msgHead.msgID == 0x0900 && olicalibrationPages.checkExistsMsgSN(msgBody.ackMSN)) { // 用流水号判断是本次的下发且判断这次的下发是否成功
                        if (msgBody.result == 0) {
                            statusText='参数已生效';
                            $('#actualAddOil').val('');
                            $('#corCalibration_tank1_actualAddOil').val('');
                            $('#corCalibration_tank2_actualAddOil').val('');
                            $('#actualAddOil2').val('');
                            $('#ReCalibration_tank1_actualAddOil').val('');
                            $('#ReCalibration_tank2_actualAddOil').val('');

                            var editOrRebuild = $("#editOrRebuild").val();

                            if (editOrRebuild == "3") { // 追溯标定才执行此步
                                // 下发成功后，保存最后一次标定的时间
                                olicalibrationPages.saveLastCalibration();
                                // 刷新加油前加油后图标以及加油数据
                                olicalibrationPages.inquireClick();
                                // 更新下方表格，用已有的数据更新，不用向服务器获取数据
                                olicalibrationPages.callBack4GetCalibration(list1);
                                $('#actualAddOil3').val('');
                                $('#ascendCalibration_tank1_actualAddOil').val('');
                                $('#ascendCalibration_tank2_actualAddOil').val('');
                            }
                            // 标定成功后，将当前车辆的标定状态还原
                            // 我实在没搞懂为什么要将标定状态还原，因为这个与同一时间只能有一个用户标定冲突，所以注释下面代码
                            // olicalibrationPages.updateCalibrationStatus($("#vehicleId").val());
                            olicalibrationPages.handleSendCalibrationComplete();
                        } else if(msgBody.result==1){
                            statusText='参数未生效';
                            olicalibrationPages.handleSendCalibrationComplete();
                        }
                    }
                    // 更新状态文本
                    sendStatus4Amendment.val(statusText);
                }
            }
        },
        // 判断下发标定后返回的流水号是否在后台返回的流水号当中
        checkExistsMsgSN : function (msgSNACK) {
            var result = false;
            var msgSn = $("#msgSN").val();
            if (msgSn != "" && typeof(msgSn) != undefined && msgSn != 'undefined') {
                msgSnArr = msgSn.split(",");
                if (msgSnArr.length > 0) {
                    for (var i=0; i<msgSnArr.length; i++) {
                        if (msgSNACK == msgSnArr[i]) {
                            result = true;
                            break;
                        }
                    }
                }
            }
            return result;
        },
        // 判断车辆的标定状态：1-占用状态，正在标定；0-空闲状态，可以标定
        checkCalibrationStatus : function () {
            var editOrRebuild = $("#editOrRebuild").val();
            var vid;
            if (editOrRebuild == "1") { // 修正标定
                vid = $("#brands").attr("data-id");
            } else if (editOrRebuild == "2") { // 重新标定
                vid = $("#brands2").attr("data-id");
            } else if (editOrRebuild == "3") {
                vid = $("#brands3").attr("data-id");
            }
            var url = "/clbs/v/oilmassmgt/oilcalibration/checkCalibrationStatus";
            var data = {"vehicleId" : vid};
            json_ajax("POST", url, "json", false, data, olicalibrationPages.checkCalibrationStatusCallBack(vid));
        },

        // 判断车辆的标定状态回调
        checkCalibrationStatusCallBack : function(vid){
            return function (data) {
                if(data.success){
                    if (data != null && data.obj != null && data.obj.calibrationStatus != null) {
                        var editOrRebuild = $("#editOrRebuild").val();
                        if (data.obj.calibrationStatus != "" && data.obj.calibrationStatus != "0") { // 标定状态为非空闲状态不可以标定
                            // 如果提示车辆在标定中，则判断其标定开始时间是多少，如果超过2个小时，则认为是非正常操作数据，并将其标定状态置为空闲状态
                            if (data.obj.updateDataTime != "" && typeof(data.obj.updateDateTime) != undefined) {
                                var curDate = olicalibrationPages.getNowFormatDate();
                                var date1 = new Date(curDate).getTime();
                                var date2 = new Date(data.obj.updateDataTime.replace('-','/').replace('-','/')).getTime();
                                var timeDiff = date1 - date2;
                                if (timeDiff >= 180000) { // 7200000ms == 2小时
                                    olicalibrationPages.updateCalibrationStatus(vid);
                                    olicalibrationPages.checkCalibrationStatus();
                                } else {
                                    layer.msg(reCalMsg);
                                    calibrationFlag = true; // 设置标定状态：标定中
                                    //$("#vehicleId").val("");
                                    if (editOrRebuild == "1") { // 实时修正
                                        $("#brands").attr("data-id", "");
                                        $("#submitBefore").attr("disabled", true);
                                        $("#submitAfter").attr("disabled", true);
                                        $("#submitBtn").attr("disabled", true);
                                    } else if (editOrRebuild == "2") {
                                        $("#brands2").attr("data-id", "");
                                        $("#submitBefore2").attr("disabled", true);
                                        $("#submitBtn2").attr("disabled", true);
                                        $("#saveOilCalibrationBtn").attr("disabled", true);
                                    } else if (editOrRebuild == "3") {
                                        $("#brands3").attr("data-id", "");
                                        $("#submit_btn").attr("disabled", true);
                                        $("#todayClick").attr("disabled", true);
                                        $("#yesterdayClick").attr("disabled", true);
                                        $("#inquireClick").attr("disabled", true);
                                    }
                                    window.name = "";
                                    return false;
                                }
                            }
                        } else {
                            calibrationFlag = false; // 设置标定状态
                            if (editOrRebuild == "1") { // 实时修正
                                $("#submitBefore").attr("disabled", false);
                                $("#submitAfter").attr("disabled", false);
                                $("#submitBtn").attr("disabled", false);
                            } else if (editOrRebuild == "2") {
                                $("#submitBefore2").attr("disabled", false);
                                $("#submitBtn2").attr("disabled", false);
                                $("#saveOilCalibrationBtn").attr("disabled", false);
                            } else if (editOrRebuild == "3") {
                                $("#submit_btn").attr("disabled", false);
                                $("#todayClick").attr("disabled", false);
                                $("#yesterdayClick").attr("disabled", false);
                                $("#inquireClick").attr("disabled", false);
                            }
                            // 记录此值，刷新页面后需要它
                            window.name = $("#vehicleId").val();
                        }
                    }
                    return true;
                }
                    layer.msg(data.msg,{move:false});

            }
        },
        // 获取当前系统时间：yyyy-MM-dd HH:mm:ss
        getNowFormatDate : function () {
            var date = new Date();
            var seperator1 = "/";
            var seperator2 = ":";
            var month = date.getMonth() + 1;
            var strDate = date.getDate();
            if (month >= 1 && month <= 9) {
                month = "0" + month;
            }
            if (strDate >= 0 && strDate <= 9) {
                strDate = "0" + strDate;
            }
            var currentdate = date.getFullYear() + seperator1 + month + seperator1 + strDate
                + " " + date.getHours() + seperator2 + date.getMinutes()
                + seperator2 + date.getSeconds();
            return currentdate;
        },
        // 更新车辆标定状态
        updateCalibrationStatus : function (vid) {
            if(vid&&vid.length>0){
                var url = "/clbs/v/oilmassmgt/oilcalibration/updateCalibrationStatus";
                var data = {"vehicleId" : vid, "calibrationStatus" : "0"};
                json_ajax("POST", url, "json", false, data);
            }
        },
        // 判断车辆是否绑定了油箱和传感器
        checkIsBondOilBox : function (vid) {
            var url = "/clbs/v/oilmassmgt/oilcalibration/checkIsBondOilBox";
            var data = {"vehicleId" : vid};
            json_ajax("POST", url, "json", false, data, olicalibrationPages.checkIsBondOilBoxCallBack);
        },
        // 判断车辆是否绑定油箱和传感器的回调
        checkIsBondOilBoxCallBack : function (data) {
            if (!data.success && data.msg == null) { // 未绑定油箱
                layer.msg(isBoundMsg);
                var editOrRebuild = $("#editOrRebuild").val();
                if (editOrRebuild == "1") { // 实时修正
                    $("#submitBefore").attr("disabled", true);
                    $("#submitAfter").attr("disabled", true);
                    $("#submitBtn").attr("disabled", true);
                } else if (editOrRebuild == "2") {
                    $("#submitBefore2").attr("disabled", true);
                    $("#submitBtn2").attr("disabled", true);
                    $("#saveOilCalibrationBtn").attr("disabled", true);
                } else if (editOrRebuild == "3") {
                    $("#submit_btn").attr("disabled", true);
                    $("#todayClick").attr("disabled", true);
                    $("#yesterdayClick").attr("disabled", true);
                    $("#inquireClick").attr("disabled", true);
                }
                return true;
            } else if(data.success) { // 已绑定
                olicalibrationPages.checkCalibrationStatus();
                return false;
            } else if(!data.success && data.msg != null){
                layer.msg(data.msg,{move:false});
            }
        },
        // 判断车辆是否在线
        checkVehicleOnlineStatus : function () {
            if ($("#vehicleId").val() != null && $("#vehicleId").val() != "") {
                var url = "/clbs/v/oilmassmgt/oilcalibration/checkVehicleOnlineStatus";
                var data = {"vehicleId" : $("#vehicleId").val()};
                json_ajax("POST", url, "json", false, data, olicalibrationPages.checkVehicleOnlineStatusCallBack);
            }
        },
        // 判断车辆是否在线回调
        checkVehicleOnlineStatusCallBack : function (data) {
            if (!data.success && data.msg == null) {
                onLineStatus = false; // 不在线
                layer.msg(isOnlineMsg);
                return false;
            }else if(data.success){
                onLineStatus = true; // 在线
                return true;
            }else if(!data.success && data.msg != null){
                layer.msg(data.msg,{move:false});
            }

        },
        // 选择车牌后的操作
        selectVehicleOperator : function (vehicleId, vehicleList) {
            // 清除前一个车辆的标定数据
            $("#dataList").empty();
            $("#dataList2").empty();
            $("#dataList3").empty();
            $("#dataList4").empty();
            list2={
                obj:{}
            }
            // 清除下发状态
            sendStatus4Amendment.val('');

            olicalibrationPages.getSelectedValue(vehicleId, vehicleList);

            // 取消进入数据对比模式
            inCompareMode=false;
            olicalibrationPages.sendGetCalibration(vehicleId);
        },
        // 下发获取标定数据指令
        sendGetCalibration:function(vehicleId){
            var url = "/clbs/v/oilmassmgt/oilcalibration/checkBoxBondInfo";
            var data = {"vehicleId" : vehicleId};
            json_ajax("POST", url, "json", true, data, olicalibrationPages.checkBoxBoundInfoCallback);
        },
        // 下发获取标定数据指令
        handleGetCalibration:function (){
            // 如果正在标定，不处理
            if(calibrationFlag){
                return;
            }

            var oilBoxType=$("#curBox").val();
            var sensorID;
            if(oilBoxType==="1"){
                sensorID=0x41;
                // 订阅
                webSocket.subscribe(headers, "/user/topic/oil63041Info", olicalibrationPages.getSensor0104Param, null, null);
            }else{
                sensorID=0x42;
                // 订阅
                webSocket.subscribe(headers, "/user/topic/oil63042Info", olicalibrationPages.getSensor0104Param, null, null);
            }

            var vehicleId=$("#vehicleId").val();
            if(vehicleId.length==0){
                layer.msg(brandNullMsg)
                return
            }
            if(onlyOne==true){
                return;
            }
            onlyOne=true;
            var url = "/clbs/v/oilmassmgt/oilvehiclesetting/getF3Param";
            var data2Send = {"vid" : vehicleId, "commandType": 0xF6, "sensorID":sensorID};
            json_ajax("POST", url, "json", true, data2Send, function(data){
                onlyOne=false;
                if (data.success) {
                    olicalibrationPages.createSocket0104InfoMonitor(data.msg,oilBoxType);
                    layer_time = window.setTimeout(function () {
                        // layer.load(2);
                        layer.msg('获取标定数据可能耗时较长，请耐心等待', {
                            icon: 16,
                            shade: 0.01,
                            time: 60000
                        });
                    }, 0);
                } else {
                    layer.msg(data.msg);
                }

            });
        },
        // 获取车辆油箱数据指令回调
        checkBoxBoundInfoCallback:function(data){
            if(data.success===true&&data.obj.oilSetting.oilBoxType){
                // 油箱绑定ID
                $("#oilBoxVehicleId1").val(data.obj.oilSetting.id);
                // 油箱2绑定ID
                $("#oilBoxVehicleId2").val(data.obj.oilSetting.id2);
                if(data.obj.oilSetting.id2 == null){
                    tankRadio2.css({"display" : "none"});
                    tankRadio4.css({"display" : "none"});
                    tankRadio6.css({"display" : "none"});
                }else{
                    tankRadio2.css({"display" : "block"});
                    tankRadio4.css({"display" : "block"});
                    tankRadio6.css({"display" : "block"});
                }
                //重新标定不需要获取标定数据
                if($("#editOrRebuild").val()=="2" && inCompareMode==false){
                    // 如果没有副油箱，隐藏副油箱radio还有下面的
                    if(!data.obj.oilSetting.id2 || data.obj.oilSetting.id2.length==0){
                        tankTwoCutover.css({"display" : "none"});
                        tankTwo.css({"display" : "none"});
                        tankRadio4.css({"display" : "none"});
                    }
                }else{
                    // 默认直接获取主油箱，副油箱只有当选中了才获取
                    olicalibrationPages.handleGetCalibration();
                }


            }else if (data.success===false){
                layer.msg(data.msg)
            }
        },
        // 修正标定选择车牌号后的回调函数
        callBack4GetCalibration : function (data) {
            // 如果数据为空显示默认值

            // 存放修正标定的标定数据，用于从重新标定切换回来时回显
            var tank1_oilHeights = '';
            var tank1_oilValues = '';
            var tank1_base_oilValues = '';
            var tank2_oilHeights = '';
            var tank2_oilValues = '';
            var tank2_base_oilValues = '';
            var oilCalibration1;
            var oilCalibration2;
            var oilCalibration1_list2;
            var oilCalibration2_list2;
            oilCalibration1 = data.obj.oilCalibration1;
            oilCalibration2 = data.obj.oilCalibration2;
            oilCalibration1_list2 = list2.obj.oilCalibration1;
            oilCalibration2_list2 = list2.obj.oilCalibration2;



            if (oilCalibration1 != null || oilCalibration2 != null ) {
                $(".dataarea").show();
            }
            var editOrRebuild = $("#editOrRebuild").val();
            var redStyle='style="background-color:rgb(255,230,230)"';

            // 清空数据表数据
            if(editOrRebuild != "2"){
                $("#dataList").empty();
                $("#dataList2").empty();
                $("#dataList3").empty();
                $("#dataList4").empty();
            }

            if (oilCalibration1 != null && oilCalibration1 != "" && oilCalibration1 != 'undefined' && oilCalibration1.length > 0) {
                // 设置默认样式
                tankOne.css({"display" : "block"});
                tankTwo.css({"display" : "none"});
                tankOneCutover.attr("class", "active");
                tankTwoCutover.attr("class", "");
                if (editOrRebuild == "1") { // 修正标定
                    $("#corCalibration_tank1_oilCalibration").val(oilCalibration1);
                    tankRadio1.children().click();
                    // 取消禁用加油前点我按钮，这个按钮在点击加油后点我时被禁用
                    if(!inCompareMode){
                        $("#submitBefore").attr("disabled", false);
                    }
                } else if (editOrRebuild == "2") { // 重新标定
                    $("#ReCalibration_tank1_oilCalibration").val(oilCalibration1);
                    tankRadio3.children().click();
                } else if (editOrRebuild == "3") { // 追溯标定
                    tankRadio5.children().click();
                }
                if (editOrRebuild == "1" || editOrRebuild == "3") { // 修正标定，才显示之前的标定
                    if(editOrRebuild=="1"){
                        for (var i=0; i<oilCalibration1.length; i++) {
                            tank1_oilHeights += oilCalibration1[i].oilLevelHeight + ",";
                            tank1_oilValues += oilCalibration1[i].oilValue + ",";
                            tank1_base_oilValues += oilCalibration1[i].oilValue + ",";
                            var str = "";
                            str += "<tr class='odd'>";
                            str += "<td class='text-center'>" + (i+1) + "</td>"; // 序号
                            str += "<td class='text-center'>" +
                                "<input readonle name='oilBoxVehicleIds' id='oilBoxVehicleIds"+i+"' value='" + oilCalibration1[i].oilBoxVehicleId +"' type='hidden' />" +
                                "<input readonly name='oilLevelHeights' id='oilLevelHeights"+i+"' value='" + toFixed(oilCalibration1[i].oilLevelHeight,2,true) + "' onkeyup='value=value.replace(/[^0-9]/g,\"\")' maxlength='4' style='border:none;width:80%;text-align:center;background-color:transparent;' />" +
                                "</td>"; // 车牌号
                            str += "<td class='text-center'>" +
                                "<input readonly name='oilValues' id='oilValues"+i+"' value='" + toFixed(oilCalibration1[i].oilValue,1,true) + "' onkeyup='value=value.replace(/[^0-9.]/g,\"\")' maxlength='10' style='border:none;width:80%;text-align:center;background-color:transparent;' />" +
                                "<input type='hidden' name='tempOilValues' value='" + toFixed(oilCalibration1[i].oilValue,1,true) + "' onkeyup='value=value.replace(/[^0-9.]/g,\"\")' maxlength='10' style='border:none;width:80%;text-align:center;background-color:transparent;' />" +
                                "</td>"; // 车辆类型
                            str += "</tr>";
                            $("#dataList").append(str);
                        }
                    }else if(editOrRebuild=="3"){
                        for (var i=0,l=Math.max(nvl(oilCalibration1,0),nvl(oilCalibration1_list2,0)); i<l; i++) {
                            var list2Height='',list2Oil='',list1Height='',list1Oil='';
                            if(oilCalibration1_list2 && i<oilCalibration1_list2.length){
                                list2Height = toFixed(oilCalibration1_list2[i].oilLevelHeight,2,true);
                                list2Oil = toFixed(oilCalibration1_list2[i].oilValue,1,true)
                            }
                            if(i<oilCalibration1.length){
                                list1Height = toFixed(oilCalibration1[i].oilLevelHeight,2,true);
                                list1Oil = toFixed(oilCalibration1[i].oilValue,1,true)
                            }

                            var tmpStyle=list1Oil !== list2Oil && list2.obj.oilCalibration1 ? redStyle : '';

                            var str="";
                            str += "<tr class='odd'>";
                            str += "<td class='text-center'>" + (i+1) + "</td>"; // 序号
                            str += "<td class='text-center' "+tmpStyle+">" + list1Height + "</td>"; // 设备液位高度
                            str += "<td class='text-center' "+tmpStyle+">" + list1Oil + "</td>"; // 设备油量值
                            str += "</tr>";

                            var str2="";
                            str2 += "<tr class='odd'>";
                            str2 += "<td class='text-center'>" + (i+1) + "</td>"; // 序号
                            str2 += "<td class='text-center' "+tmpStyle+">" + list2Height + "</td>"; // 修正液位高度
                            str2 += "<td class='text-center' "+tmpStyle+">" + list2Oil + "</td>"; // 修正油量值
                            str2 += "</tr>";

                            $("#dataList").append(str);
                            $("#dataList3").append(str2);
                        }
                        // 自动执行查询
                        olicalibrationPages.inquireClick();
                    }
                    if (tank1_oilHeights.length > 0) {
                        tank1_oilHeights = tank1_oilHeights.substr(0, tank1_oilHeights.length - 1);
                    }
                    if (tank1_oilValues.length > 0) {
                        tank1_oilValues = tank1_oilValues.substr(0, tank1_oilValues.length - 1);
                    }
                    if (tank1_base_oilValues.length > 0) {
                        tank1_base_oilValues = tank1_base_oilValues.substr(0, tank1_base_oilValues.length - 1);
                    }
                    $("#corCalibration_tank1_oilLevelHeights").val(tank1_oilHeights);
                    $("#corCalibration_tank1_oilValues").val(tank1_oilValues);
                    $("#corCalibration_tank1_base_oilValues").val(tank1_base_oilValues);

                }
            }


            if (oilCalibration2 != null && oilCalibration2 != "" && oilCalibration2 != 'undefined' && oilCalibration2.length > 0) {

                if (editOrRebuild == "1") { // 修正标定
                    $("#corCalibration_tank2_oilCalibration").val(oilCalibration2);
                    tankRadio2.children().click();
                } else if(editOrRebuild == "2"){ // 重新标定
                    $("#ReCalibration_tank2_oilCalibration").val(oilCalibration2);
                    tankRadio4.children().click();
                }else{
                    tankRadio6.children().click();
                }
                if (editOrRebuild == "1" || editOrRebuild == "3") { // 修正标定，才显示之前的标定

                    if(editOrRebuild=="1"){
                        for (var i=0; i<oilCalibration2.length; i++) {
                            tank2_oilHeights += oilCalibration2[i].oilLevelHeight + ",";
                            tank2_oilValues += oilCalibration2[i].oilValue + ",";
                            tank2_base_oilValues += oilCalibration2[i].oilValue + ",";
                            var str = "";
                            str += "<tr class='odd'>";
                            str += "<td class='text-center'>" + (i+1) + "</td>"; // 序号
                            str += "<td class='text-center'>" +
                                "<input readonle name='oilBoxVehicleIds2' id='oilBoxVehicleIds2"+i+"' value='" + oilCalibration2[i].oilBoxVehicleId +"' type='hidden' />" +
                                "<input readonly name='oilLevelHeights2' id='oilLevelHeights2"+i+"' value='" + toFixed(oilCalibration2[i].oilLevelHeight,2,true) + "' onkeyup='value=value.replace(/[^0-9]/g,\"\")' maxlength='4' style='border:none;width:80%;text-align:center;background-color:transparent;' />" +
                                "</td>"; // 车牌号
                            str += "<td class='text-center'>" +
                                "<input readonly name='oilValues2' id='oilValues2"+i+"' value='" + toFixed(oilCalibration2[i].oilValue,1,true) + "' onkeyup='value=value.replace(/[^0-9.]/g,\"\")' maxlength='10' style='border:none;width:80%;text-align:center;background-color:transparent;' />" +
                                "<input type='hidden' name='tempOilValues2' value='" + toFixed(oilCalibration2[i].oilValue,1,true) + "' onkeyup='value=value.replace(/[^0-9.]/g,\"\")' maxlength='10' style='border:none;width:80%;text-align:center;background-color:transparent;' />" +
                                "</td>"; // 车辆类型
                            str += "</tr>";
                            $("#dataList2").append(str);
                        }
                    }else if(editOrRebuild=="3"){
                        for (var i=0,l=Math.max(nvl(oilCalibration2,0),nvl(oilCalibration2_list2,0)); i<l; i++) {

                            var list2Height='',list2Oil='',list1Height='',list1Oil='';
                            if(oilCalibration2_list2 && i<oilCalibration2_list2.length){
                                list2Height = toFixed(oilCalibration2_list2[i].oilLevelHeight,2,true);
                                list2Oil = toFixed(oilCalibration2_list2[i].oilValue,1,true)
                            }
                            if(i<oilCalibration2.length){
                                list1Height = toFixed(oilCalibration2[i].oilLevelHeight,2,true);
                                list1Oil = toFixed(oilCalibration2[i].oilValue,1,true)
                            }

                            var tmpStyle=list1Oil !== list2Oil && list2.obj.oilCalibration2 ? redStyle : '';

                            var str="";
                            str += "<tr class='odd'>";
                            str += "<td class='text-center'>" + (i+1) + "</td>"; // 序号
                            str += "<td class='text-center' "+tmpStyle+">" + list1Height + "</td>"; // 设备液位高度
                            str += "<td class='text-center' "+tmpStyle+">" + list1Oil + "</td>"; // 设备油量值
                            str += "</tr>";

                            var str2="";
                            str2 += "<tr class='odd'>";
                            str2 += "<td class='text-center'>" + (i+1) + "</td>"; // 序号
                            str2 += "<td class='text-center' "+tmpStyle+">" + list2Height + "</td>"; // 修正液位高度
                            str2 += "<td class='text-center' "+tmpStyle+">" + list2Oil + "</td>"; // 修正油量值
                            str2 += "</tr>";

                            $("#dataList2").append(str);
                            $("#dataList4").append(str2);
                        }
                        // 自动执行查询
                        olicalibrationPages.inquireClick();
                    }
                    if (tank2_oilHeights.length > 0) {
                        tank2_oilHeights = tank2_oilHeights.substr(0, tank2_oilHeights.length - 1);
                    }
                    if (tank2_oilValues.length > 0) {
                        tank2_oilValues = tank2_oilValues.substr(0, tank2_oilValues.length - 1);
                    }
                    if (tank2_base_oilValues.length > 0) {
                        tank2_base_oilValues = tank2_base_oilValues.substr(0, tank2_base_oilValues.length - 1);
                    }
                    $("#corCalibration_tank2_oilLevelHeights").val(tank2_oilHeights);
                    $("#corCalibration_tank2_oilValues").val(tank2_oilValues);
                    $("#corCalibration_tank2_base_oilValues").val(tank2_base_oilValues);
                }
            } else if($("#oilBoxVehicleId2").val().length==0){
                // 隐藏油箱2
                tankTwoCutover.css({"display" : "none"});
                tankTwo.css({"display" : "none"});

            }
        },
        // 点击数据对比或者数据对比弹框的刷新按钮的回调
        callBack4Compare: function(){
            // 如果数据为空显示默认值
            var nvl=function(obj,defaultValue){
                if(!obj){
                    return defaultValue;
                }
                    return obj.length;

            };
            var oilCalibration1;
            var oilCalibration2;
            var oilCalibration1_list2;
            var oilCalibration2_list2;
            oilCalibration1 = list1.obj.oilCalibration1;
            oilCalibration2 = list1.obj.oilCalibration2;
            oilCalibration1_list2 = list2.obj.oilCalibration1;
            oilCalibration2_list2 = list2.obj.oilCalibration2;

            var editOrRebuild = $("#editOrRebuild").val();
            var redStyle='style="background-color:rgb(255,230,230)"';
            var _curBox=$("#curBox").val();
            $("#compareDataListOne").empty();
            $("#compareDataListThree").empty();
            if (_curBox=="1"&&oilCalibration1 != null && oilCalibration1 != "" && oilCalibration1 != 'undefined' && oilCalibration1.length > 0) {
                // 显示油箱1
                $("#tankOneInCompare").css({"display" : "block"});
                $("#tankOneCutoverInCompare").css({"display" : "block"});

                if (editOrRebuild == "1" || editOrRebuild == "2") { // 修正标定，才显示之前的标定
                    for (var i=0,l=Math.max(nvl(oilCalibration1,0),nvl(oilCalibration1_list2,0)); i<l; i++) {
                        var list2Height='',list2Oil='',list1Height='',list1Oil='';
                        if(i<oilCalibration1_list2.length){
                            list2Height = toFixed(oilCalibration1_list2[i].oilLevelHeight,2,true);
                            list2Oil = toFixed(oilCalibration1_list2[i].oilValue,1,true)
                        }
                        if(i<oilCalibration1.length){
                            list1Height = toFixed(oilCalibration1[i].oilLevelHeight,2,true);
                            list1Oil = toFixed(oilCalibration1[i].oilValue,1,true)
                        }


                        var tmpStyle=list1Oil !== list2Oil && list2.obj.oilCalibration1 ? redStyle : '';

                        var str="";
                        str += "<tr class='odd'>";
                        str += "<td class='text-center'>" + (i+1) + "</td>"; // 序号
                        str += "<td class='text-center' "+tmpStyle+">" + list1Height + "</td>"; // 设备液位高度
                        str += "<td class='text-center' "+tmpStyle+">" + list1Oil + "</td>"; // 设备油量值
                        str += "</tr>";

                        var str2="";
                        str2 += "<tr class='odd'>";
                        str2 += "<td class='text-center'>" + (i+1) + "</td>"; // 序号
                        str2 += "<td class='text-center' "+tmpStyle+">" + list2Height + "</td>"; // 修正液位高度
                        str2 += "<td class='text-center' "+tmpStyle+">" + list2Oil + "</td>"; // 修正油量值
                        str2 += "</tr>";

                        $("#compareDataListOne").append(str);
                        $("#compareDataListThree").append(str2);
                    }
                }
            }else{
                $("#tankOneInCompare").css({"display" : "none"});
                $("#tankOneCutoverInCompare").css({"display" : "none"});
            }
            $("#compareDataListTwo").empty();
            $("#compareDataListFour").empty();
            if (_curBox=="2"&&oilCalibration2 != null && oilCalibration2 != "" && oilCalibration2 != 'undefined' && oilCalibration2.length > 0) {
                // 显示油箱2
                $("#tankTwoInCompare").css({"display" : "block"});
                $("#tankTwoCutoverInCompare").css({"display" : "block"});

                if (editOrRebuild == "1" || editOrRebuild == "2") { // 修正标定，才显示之前的标定
                    for (var i=0,l=Math.max(nvl(oilCalibration2,0),nvl(oilCalibration2_list2,0)); i<l; i++) {

                        var list2Height='',list2Oil='',list1Height='',list1Oil='';
                        if(i<oilCalibration2_list2.length){
                            list2Height = toFixed(oilCalibration2_list2[i].oilLevelHeight,2,true);
                            list2Oil = toFixed(oilCalibration2_list2[i].oilValue,1,true)
                        }
                        if(i<oilCalibration2.length){
                            list1Height = toFixed(oilCalibration2[i].oilLevelHeight,2,true);
                            list1Oil = toFixed(oilCalibration2[i].oilValue,1,true)
                        }



                        var tmpStyle=list1Oil !== list2Oil && list2.obj.oilCalibration1 ? redStyle : '';

                        var str="";
                        str += "<tr class='odd'>";
                        str += "<td class='text-center'>" + (i+1) + "</td>"; // 序号
                        str += "<td class='text-center' "+tmpStyle+">" + list1Height + "</td>"; // 设备液位高度
                        str += "<td class='text-center' "+tmpStyle+">" + list1Oil + "</td>"; // 设备油量值
                        str += "</tr>";

                        var str2="";
                        str2 += "<tr class='odd'>";
                        str2 += "<td class='text-center'>" + (i+1) + "</td>"; // 序号
                        str2 += "<td class='text-center' "+tmpStyle+">" + list2Height + "</td>"; // 修正液位高度
                        str2 += "<td class='text-center' "+tmpStyle+">" + list2Oil + "</td>"; // 修正油量值
                        str2 += "</tr>";

                        $("#compareDataListTwo").append(str);
                        $("#compareDataListFour").append(str2);

                    }
                }
            } else {
                // 隐藏油箱2
                $("#tankTwoInCompare").css({"display" : "none"});
                $("#tankTwoCutoverInCompare").css({"display" : "none"});

            }

        },
        // 点名车辆位置信息后，回调处理数据
        getLastOilDataCallBack : function (msg) {
            if(timeOutId){
                window.clearTimeout(timeOutId);
                layer.closeAll();
                timeOutId = null;
            }
            var obj = $.parseJSON(msg.body);
            var curBox = $("#curBox").val();
            var curBtn = $("#curClickedBtn").val(); // 标记当前点击的哪个按钮，方便给对应的广西框赋值
            if (obj != null) {
                var msgBody = obj.data.msgBody; // 消息体
                var msgHead = obj.data.msgHead; // 消息头
                if (msgHead.msgID == "513" && msgBody.msgSNAck == $("#msgSN").val()) { // 位置信息查询应答
                    var oilMass = msgBody.gpsInfo.oilMass; // 油量数据
                    if (oilMass != null && oilMass.length > 0) {
                        var tank1Flag = false; // 是否获取到油箱1数据
                        var tank2Flag = false; // 是否获取到油箱2数据
                        for (var i=0; i<oilMass.length; i++) {
                            layer.closeAll();
                            var result1 = isNaN(oilMass[i].oilHeight);
                            var result2 = isNaN(oilMass[i].oilMass);
                            var result3 = isNaN(oilMass[i].aDHeight);
                            if (result1 || result2 || result3) {
                                break;
                            }
                            var oilLevelHeight = parseFloat(oilMass[i].oilHeight) ;
                            var oilValue = parseFloat(oilMass[i].oilMass);
                            var ADHeight = oilMass[i].aDHeight;
                            if (curBox == "1") { // 油箱1
                                if (oilMass[i].id == "65") {
                                    tank1Flag = true;
                                    if (curBtn == "1") { // 修正标定-加油前点我
                                        $("#oilLevelHeight_before").val(oilLevelHeight + " mm");
                                        $("#oilValue_before").val(oilValue + " L");
                                        // 记录当前值，用于页签切换时，值的回显
                                        $("#corCalibration_tank1_oilLevelHeight_before").val(oilLevelHeight + " mm");
                                        $("#corCalibration_tank1_oilValue_before").val(oilValue + " L");
                                    } else if (curBtn == "2") { // 修正标定-加油后点我
                                        $("#oilLevelHeight_after").val(oilLevelHeight + " mm");
                                        $("#oilValue_after").val(oilValue + " L");
                                        // 记录当前值，用于页签切换时，值的回显
                                        $("#corCalibration_tank1_oilLevelHeight_after").val(oilLevelHeight + " mm");
                                        $("#corCalibration_tank1_oilValue_after").val(oilValue + " L");
                                        // 设置"加油前点我"按钮的可点击状态
                                        olicalibrationPages.setSubmitBeforeBtnStatus();
                                        // 计算理论加油量
                                        olicalibrationPages.calculateTheoryAddOil();
                                    } else if (curBtn == "3") { // 重新标定-加油前点我(刷新最新值)
                                        $("#oilLevelHeight2_before").val(oilLevelHeight + " mm");
                                        $("#oilValue2_before").val(oilValue + " L");
                                        $("#oilADHeight").val(ADHeight + " (AD值)");
                                        // 记录当前值，用于页签切换时，值的回显
                                        $("#ReCalibration_tank1_oilLevelHeight_before").val(oilLevelHeight + " mm");
                                        $("#ReCalibration_tank1_oilValue_before").val(oilValue + " L");
                                        $("#ReCalibration_tank1_oilADHeight").val(ADHeight + " (AD值)");
                                    } else if (curBtn == "4") { // 重新标定-实际加油量后面的点击按钮
                                        var actualAddOil = $("#actualAddOil2").val();
                                    }
                                }
                            }
                            if (curBox == "2") { // 油箱2
                                if (oilMass[i].id == "66") {
                                    tank2Flag = true;
                                    if (curBtn == "1") { // 修正标定-加油前点我
                                        $("#oilLevelHeight_before").val(oilLevelHeight + " mm");
                                        $("#oilValue_before").val(oilValue + " L");
                                        // 记录当前值，用于页签切换时，值的回显
                                        $("#corCalibration_tank2_oilLevelHeight_before").val(oilLevelHeight + " mm");
                                        $("#corCalibration_tank2_oilValue_before").val(oilValue + " L");
                                    } else if (curBtn == "2") { // 修正标定-加油后点我
                                        $("#oilLevelHeight_after").val(oilLevelHeight + " mm");
                                        $("#oilValue_after").val(oilValue + " L");
                                        // 记录当前值，用于页签切换时，值的回显
                                        $("#corCalibration_tank2_oilLevelHeight_after").val(oilLevelHeight + " mm");
                                        $("#corCalibration_tank2_oilValue_after").val(oilValue + " L");
                                        // 设置"加油前点我"按钮的可点击状态
                                        olicalibrationPages.setSubmitBeforeBtnStatus();
                                        // 计算理论加油量
                                        olicalibrationPages.calculateTheoryAddOil();
                                    } else if (curBtn == "3") { // 重新标定-加油前点我
                                        $("#oilLevelHeight2_before").val(oilLevelHeight + " mm");
                                        $("#oilValue2_before").val(oilValue + " L");
                                        $("#oilADHeight").val(ADHeight + " (AD值)");
                                        // 记录当前值，用于页签切换时，值的回显
                                        $("#ReCalibration_tank2_oilLevelHeight_before").val(oilLevelHeight + " mm");
                                        $("#ReCalibration_tank2_oilValue_before").val(oilValue + " L");
                                        $("#ReCalibration_tank2_oilADHeight").val(ADHeight + " (AD值)");
                                    } else if (curBtn == "4") { // 重新标定-实际加油量后面的点击按钮
                                        var actualAddOil = $("#actualAddOil2").val();
                                    }
                                }
                            }
                        }
                        if (curBox == "1" && !tank1Flag) {
                            layer.msg(tank1GetDataFailMsg);
                            return;
                        }
                        if (curBox == "2" && !tank2Flag) {
                            layer.msg(tank2GetDataFailMsg);
                            return;
                        }
                    } else {
                        layer.msg(getDataFailMsg);
                    }
                }
            } else {
                layer.msg(getDataFailMsg);
            }
        },
        // 计算理论加油量
        calculateTheoryAddOil : function () {
            var editOrRebuild = $("#editOrRebuild").val();
            var curBox = $("#curBox").val();
            // 计算理论加油量
            var oilValueBefore;
            var oilValueAfter;
            if (editOrRebuild == "1") { // 修正标定
                oilValueBefore = $("#oilValue_before").val();
                oilValueAfter = $("#oilValue_after").val();
            } else if (editOrRebuild == "3") { // 追溯标定
                oilValueBefore = $("#oil_before_value").val();
                oilValueAfter = $("#oil_after_value").val();
            }
            oilValueBefore = oilValueBefore.substr(0, oilValueBefore.length - 2);
            oilValueAfter = oilValueAfter.substr(0, oilValueAfter.length - 2);
            var theoryAddOil = parseFloat(parseFloat(oilValueAfter) - parseFloat(oilValueBefore)).toFixed(1);
            if (typeof(theoryAddOil) == "undefined" || isNaN(theoryAddOil)) {
                theoryAddOil = "";
            }
            if (editOrRebuild == "1") { // 修正标定
                $("#actualAddOil").attr("placeholder", "当前理论加油量为 " + theoryAddOil + " （L）");
                if (curBox == "1") {
                    $("#corCalibration_tank1_noteMsg").val("当前理论加油量为 " + theoryAddOil + " （L）");
                } else if (curBox == "2") {
                    $("#corCalibration_tank2_noteMsg").val("当前理论加油量为 " + theoryAddOil + " （L）");
                }
            } else if (editOrRebuild == "3") { // 追溯标定
                $("#actualAddOil3").attr("placeholder", "当前理论加油量为 " + theoryAddOil + " （L）");
                if (curBox == "1") {
                    $("#ascendCalibration_tank1_noteMsg").val("当前理论加油量为 " + theoryAddOil + " （L）");
                } else if (curBox == "2") {
                    $("#ascendCalibration_tank2_noteMsg").val("当前理论加油量为 " + theoryAddOil + " （L）");
                }
            }
        },
        // 点击车辆的时候，获取当前车辆的信息，
        // 并赋值给相应的文本框-----此方法暂时用哈，因为还没有搞清楚bsSuggest如何获取除id，key之外的其他字段的值
        getSelectedValue : function (keyId, dataList) {
            var editOrRebuild = $("#editOrRebuild").val();
            if (dataList != null && dataList.value != null && dataList.value.length > 0) {
                for (var i=0; i<dataList.value.length; i++) {
                    if (dataList.value[i].id == keyId) {
                        $("#settingParamId").val(dataList.value[i].settingParamId);
                        $("#calibrationParamId").val(dataList.value[i].calibrationParamId);
                        $("#transmissionParamId").val(dataList.value[i].transmissionParamId);
                        if (editOrRebuild == "1") { // 修正标定
                            $("#corCalibration_settingParamId").val(dataList.value[i].settingParamId);
                            $("#corCalibration_calibrationParamId").val(dataList.value[i].calibrationParamId);
                            $("#corCalibration_transmissionParamId").val(dataList.value[i].transmissionParamId);
                        } else if (editOrRebuild == "2") { // 重新标定
                            $("#ReCalibration_settingParamId").val(dataList.value[i].settingParamId);
                            $("#ReCalibration_calibrationParamId").val(dataList.value[i].calibrationParamId);
                            $("#ReCalibration_transmissionParamId").val(dataList.value[i].transmissionParamId);
                        }
                        break;
                    }
                }
            }
        },
        // 修正标定表单初始化
        initForm : function () {
            $(".delIcon").hide();
            $("#vehicleId").val("");
            $("#oilLevelHeight_before").val("");
            $("#oilValue_before").val("");
            $("#oilLevelHeight_after").val("");
            $("#oilValue_after").val("");
            $("#actualAddOil").val("");
            $("#corCalibration_brands").val("");
            $("#corCalibration_vehicleId").val("");
            $("#corCalibration_settingParamId").val("");
            $("#corCalibration_calibrationParamId").val("");
            $("#corCalibration_transmissionParamId").val();
            $("#corCalibration_tank1_oilBoxVehicleId").val("");
            $("#corCalibration_tank1_oilLevelHeight_before").val("");
            $("#corCalibration_tank1_oilValue_before").val("");
            $("#corCalibration_tank1_oilLevelHeight_after").val("");
            $("#corCalibration_tank1_oilValue_after").val("");
            $("#corCalibration_tank1_actualAddOil").val("");
            $("#actualAddOil").attr("placeholder", "请输入实际加油量（L）");
            $("#corCalibration_tank1_noteMsg").val("请输入实际加油量（L）");
            $("#corCalibration_tank1_oilLevelHeights").val("");
            $("#corCalibration_tank1_oilValues").val("");
            $("#corCalibration_tank1_base_oilValues").val("");
            $("#corCalibration_tank2_oilBoxVehicleId").val("");
            $("#corCalibration_tank2_oilLevelHeight_before").val("");
            $("#corCalibration_tank2_oilValue_before").val("");
            $("#corCalibration_tank2_oilLevelHeight_after").val("");
            $("#corCalibration_tank2_oilValue_after").val("");
            $("#corCalibration_tank2_actualAddOil").val("");
            $("#corCalibration_tank2_noteMsg").val("请输入实际加油量（L）");
            $("#corCalibration_tank2_oilLevelHeights").val("");
            $("#corCalibration_tank2_oilValues").val("");
            $("#corCalibration_tank2_base_oilValues").val("");
            $("#settingParamId").val("");
            $("#calibrationParamId").val("");
            $("#transmissionParamId").val("");
            $("#oilBoxVehicleId1").val("");
            $("#oilBoxVehicleId2").val("");
            sendStatus4Amendment.val('');
        },
        // 重新标定表单初始化
        initForm2 : function () {
            num = 0; // 油箱1、油箱2总记录数，设置input的id时用
            seqNo = 1; // 标定数据序号-油箱1
            seqNo2 = 1; // 标定数据序号-油箱2
            initFlag = true; // 获取初始数据标识
            tempOilValue = 0; // 油箱1临时油量值
            tempOilValue2 = 0; // 油箱2临时油量值
            $(".delIcon").hide();
            $("#vehicleId").val("");
            $("#oilLevelHeight2_before").val("");
            $("#oilValue2_before").val("");
            $("#oilADHeight").val("");
            $("#actualAddOil2").val("");
            $("#ReCalibration_brands").val("");
            $("#ReCalibration_vehicleId").val("");
            $("#ReCalibration_settingParamId").val("");
            $("#ReCalibration_calibrationParamId").val("");
            $("#ReCalibration_transmissionParamId").val("");
            $("#ReCalibration_tank1_oilBoxVehicleId").val("");
            $("#ReCalibration_tank1_oilLevelHeight_before").val("");
            $("#ReCalibration_tank1_oilValue_before").val("");
            $("#ReCalibration_tank1_oilADHeight").val("");
            $("#ReCalibration_tank1_actualAddOil").val("");
            $("#ReCalibration_tank1_oilLevelHeights").val("");
            $("#ReCalibration_tank1_oilValues").val("");
            $("#ReCalibration_tank2_oilBoxVehicleId").val("");
            $("#ReCalibration_tank2_oilLevelHeight_before").val("");
            $("#ReCalibration_tank2_oilValue_before").val("");
            $("#ReCalibration_tank2_oilADHeight").val("");
            $("#ReCalibration_tank2_actualAddOil").val("");
            $("#ReCalibration_tank2_oilLevelHeights").val("");
            $("#ReCalibration_tank2_oilValues").val("");
            $("#settingParamId").val("");
            $("#calibrationParamId").val("");
            $("#transmissionParamId").val("");
            $("#oilBoxVehicleId1").val("");
            $("#oilBoxVehicleId2").val("");
            sendStatus4Amendment.val('');
        },
        // 追溯标定表单初始化
        initForm3 : function () {
            $(".delIcon").hide();
            $("#vehicleId").val("");
            $("#graphShow").hide();
            $("#sjcontainer").empty();
            $("#oil_before_height").val("");
            $("#oil_before_value").val("");
            $("#oil_after_height").val("");
            $("#oil_after_value").val("");
            $("#actualAddOil3").val("");
            $("#ascendCalibration_tank1_oilLevelHeight_before").val("");
            $("#ascendCalibration_tank1_oilValue_before").val("");
            $("#ascendCalibration_tank1_oilLevelHeight_after").val("");
            $("#ascendCalibration_tank1_oilValue_after").val("");
            $("#ascendCalibration_tank1_actualAddOil").val("");
            $("#actualAddOil3").attr("placeholder", "请输入实际加油量（L）");
            $("#ascendCalibration_tank1_noteMsg").val("请输入实际加油量（L）");
            $("#ascendCalibration_tank2_oilLevelHeight_before").val("");
            $("#ascendCalibration_tank2_oilValue_before").val("");
            $("#ascendCalibration_tank2_oilLevelHeight_after").val("");
            $("#ascendCalibration_tank2_oilValue_after").val("");
            $("#ascendCalibration_tank2_actualAddOil").val("");
            $("#ascendCalibration_tank2_noteMsg").val("请输入实际加油量（L）");
            $("#settingParamId").val("");
            $("#calibrationParamId").val("");
            $("#transmissionParamId").val("");
            $("#oilBoxVehicleId1").val("");
            $("#oilBoxVehicleId2").val("");
            time_before = "";
            time_after = "";
            oil_before = "";
            oil_after = "";
            time_before2 = "";
            time_after2 = "";
            oil_before2 = "";
            oil_after2 = "";
            sendStatus4Amendment.val('');
        },
        // 追溯标定表单初始化(追溯标定查询时用这个方法)-和initForm3的区别是initForm4里注释的代码没有清空
        initForm4 : function () {
            $("#graphShow").hide();
            $("#sjcontainer").empty();
            $("#oil_before_height").val("");
            $("#oil_before_value").val("");
            $("#oil_after_height").val("");
            $("#oil_after_value").val("");
            $("#actualAddOil3").val("");
            $("#ascendCalibration_tank1_oilLevelHeight_before").val("");
            $("#ascendCalibration_tank1_oilValue_before").val("");
            $("#ascendCalibration_tank1_oilLevelHeight_after").val("");
            $("#ascendCalibration_tank1_oilValue_after").val("");
            $("#ascendCalibration_tank1_actualAddOil").val("");
            $("#actualAddOil3").attr("placeholder", "请输入实际加油量（L）");
            $("#ascendCalibration_tank1_noteMsg").val("请输入实际加油量（L）");
            $("#ascendCalibration_tank2_oilLevelHeight_before").val("");
            $("#ascendCalibration_tank2_oilValue_before").val("");
            $("#ascendCalibration_tank2_oilLevelHeight_after").val("");
            $("#ascendCalibration_tank2_oilValue_after").val("");
            $("#ascendCalibration_tank2_actualAddOil").val("");
            $("#ascendCalibration_tank2_noteMsg").val("请输入实际加油量（L）");
            time_before = "";
            time_after = "";
            oil_before = "";
            oil_after = "";
            time_before2 = "";
            time_after2 = "";
            oil_before2 = "";
            oil_after2 = "";
            sendStatus4Amendment.val('');
        },

        // 点击获取油量的按钮后，弹框提示用户数据加载中
        loading : function () {
            layer.msg(loadingMsg, {
                icon: 16,
                shade: 0.01,
                time:40000
            });
        },
        // 修正标定-“加油前点我”点击事件
        submitBeforeClick : function () {
            sendStatus4Amendment.val('');
            $("#curClickedBtn").val("1");
            var vehicleId = $("#vehicleId").val();
            if (vehicleId == "" || typeof(vehicleId) == undefined || typeof(vehicleId) == 'undefined') {
                layer.msg(brandNullMsg);
                return;
            }
            // 判断车辆在线状态
            olicalibrationPages.checkVehicleOnlineStatus();
            if (onLineStatus) { // 如果车辆在线，才执行如下的操作
                // 订阅车辆位置信息
                olicalibrationPages.subscribeLatestLocation();
                var curBox = $("#curBox").val();
                var url = "/clbs/v/oilmassmgt/oilcalibration/getLatestOilData";
                var data = {"vehicleId" : vehicleId, "curBox" : curBox};
                json_ajax("POST", url, "json", true, data, olicalibrationPages.callBack1);
                // 加载框
                olicalibrationPages.loading();
                timeOutId = window.setTimeout(function(){
                    layer.msg('获取数据超时')
                },20000);
            }
        },
        // 修正标定-“加油后点我”点击事件
        submitAfterClick : function () {
            sendStatus4Amendment.val('');
            $("#curClickedBtn").val("2");
            var vehicleId = $("#vehicleId").val();
            if (vehicleId == "" || typeof(vehicleId) == undefined || typeof(vehicleId) == 'undefined') {
                layer.msg(brandNullMsg);
                return;
            }
            // 判断车辆在线状态
            olicalibrationPages.checkVehicleOnlineStatus();
            if (onLineStatus) {
                // 订阅车辆位置信息
                olicalibrationPages.subscribeLatestLocation();
                if ($("#oilLevelHeight_before").val() == "" || $("#oilValue_before").val() == "") {
                    layer.msg(getBeforeDataMsg);
                    return;
                }
                var curBox = $("#curBox").val();
                var url = "/clbs/v/oilmassmgt/oilcalibration/getLatestOilData";
                var data = {"vehicleId" : vehicleId, "curBox" : curBox};
                json_ajax("POST", url, "json", true, data, olicalibrationPages.callBack2);
                // 加载框
                olicalibrationPages.loading();
            }
        },
        // 修正标定-“加油前点我”事件回调函数
        callBack1 : function (data) {
            if(data.success){
                if (data != null && data.obj != null && data.obj.msgSN != null && data.obj.msgSN != "") {
                    $("#msgSN").val(data.obj.msgSN);
                }
                /*
					 * window.setTimeout(function () { if
					 * ($("#oilLevelHeight_before").val() == "") {
					 * layer.msg(getDataFailMsg); } },7000);
					 */
            }else{
                layer.msg(data.msg,{move:false});
            }
        },
        // 修正标定-“加油后点我”事件回调函数
        callBack2 : function (data) {
            if(data.success){
                if (data != null && data.obj != null && data.obj.msgSN != null && data.obj.msgSN != "") {
                    $("#msgSN").val(data.obj.msgSN);
                }
            }else{
                layer.msg(data.msg,{move:false});
            }
        },
        // 追溯修正-刷新按钮
        refreshDataBtn3Click:function(){
            var vehicleId=$("#vehicleId").val();
            if (vehicleId == "" || typeof(vehicleId) == undefined || typeof(vehicleId) == 'undefined') {
                layer.msg(brandNullMsg);
                return;
            }
            if(!list2.obj.oilCalibration1){
                layer.msg('请先修正下发');
                return;
            }

            if(!vehicleId || vehicleId.length==0){
                return;
            }
            olicalibrationPages.sendGetCalibration(vehicleId);
        },
        // 点击数据对比和弹出框内的刷新按钮执行的事件，他俩绑定同一个方法
        refreshBtnClick : function(){
            var vehicleId=$("#vehicleId").val();
            if (vehicleId == "" || typeof(vehicleId) == undefined || typeof(vehicleId) == 'undefined') {
                layer.msg(brandNullMsg);
                return;
            }
            if(!list2.obj.oilCalibration1){
                layer.msg('请先修正下发');
                return;
            }
            inCompareMode=true; // 进入数据对比模式

            if(!vehicleId || vehicleId.length==0){
                return;
            }
            olicalibrationPages.sendGetCalibration(vehicleId);
        },
        // 修正标定-“提交”按钮点击事件
        submitBtnClick : function () {
            if(inCompareMode){
                olicalibrationPages.sure();
                return;
            }
            var actualAddOil = $("#actualAddOil").val();
            if ($("#brands").val() == "") {
                layer.msg(brandNullMsg);
                return;
            } else if ($("#oilLevelHeight_after").val() == "" || $("#oilValue_after").val() == "") {
                layer.msg(getBothDataMsg);
                return;
            } else if ($("#actualAddOil").val() == "") {
                layer.msg(actualAddOilNullMsg);
                return;
            } else if (isNaN($("#actualAddOil").val())) {
                layer.msg(actualAddOilIsRightNumMsg);
                return;
            } else if (parseFloat($("#actualAddOil").val()) <= 0) {
                layer.msg(actualAddOilIsAboveZeroMsg);
                return;
            }
                // 判断车辆在线状态
                olicalibrationPages.checkVehicleOnlineStatus();
                if (onLineStatus) {

                    var curBox = $("#curBox").val();
                    if (curBox == "1") { // 油箱1实际加油量
                        $("#corCalibration_tank1_actualAddOil").val($("#actualAddOil").val());
                    } else if (curBox == "2") { // 油箱2实际加油量
                        $("#corCalibration_tank2_actualAddOil").val($("#actualAddOil").val());
                    }
                }

            layer.confirm("您确定实际加了 <span style='color:red;'>" + actualAddOil + "L </span> 油吗？", {
                btn : ['确定', '取消']
            }, olicalibrationPages.sure, olicalibrationPages.cancle);
        },
        // 确定操作
        sure : function () {

            // 保存标定并下发给设备
            olicalibrationPages.saveOilcalibration1_2();
        },
        // 取消操作
        cancle : function () {
            // layer.msg("我不确定");
        },
        // 追溯标定修正下发按钮事件
        submit_btnClick : function () {
            var actualAddOil = $("#actualAddOil3").val();
            var brand = $("#brands3").val();
            var tank1LastTime = $("#tank1LastTime").val();
            var tank2LastTime = $("#tank2LastTime").val();
            var curBox = $("#curBox").val();
            if (brand == "") {
                layer.msg(brandNullMsg);
                return;
            } else if ($("#oil_before_height").val() == "" || $("#oil_before_value").val() == "") {
                layer.msg(ascendBeforeDataNull);
                return;
            } else if ($("#oil_after_height").val() == "" || $("#oil_after_value").val() == "") {
                layer.msg(ascendAfterDataNull);
                return;
            } else if (curBox == "1" && (time_before != "" && tank1LastTime != "" && time_before < tank1LastTime)
                || (time_after != "" && tank1LastTime != "" && time_after < tank1LastTime)) {
                layer.confirm("【" + brand + "-油箱1】" + tank1LastTime + "之前的数据已经被标定过，不能重复标定！", {btn : ["确定"]});
                return;
            } else if (curBox == "2" && (time_before2 != "" && tank2LastTime != "" && time_before2 < tank2LastTime)
                || (time_after2 != "" && tank2LastTime != "" && time_after2 < tank2LastTime)) {
                layer.confirm("【" + brand + "-油箱2】" + tank2LastTime + "之前的数据已经被标定过，不能重复标定！", {btn : ["确定"]});
                return;
            } else if (actualAddOil == "") {
                layer.msg(actualAddOilNullMsg);
                return;
            } else if (isNaN(actualAddOil)) {
                layer.msg(actualAddOilIsRightNumMsg);
                return;
            } else if (parseFloat(actualAddOil) <= 0) {
                layer.msg(actualAddOilIsAboveZeroMsg);
                return;
            }
            // 判断车辆在线状态
            olicalibrationPages.checkVehicleOnlineStatus();
            if (onLineStatus) {
                layer.confirm("您确定实际加了 <span style='color:red;'>" + actualAddOil + " L </span> 油吗？", {
                    btn : ['确定', '取消']
                }, olicalibrationPages.sure, olicalibrationPages.cancle);
            }
        },
        // 保存操作-油箱1
        saveOilcalibration : function () {
            var editOrRebuild = $("#editOrRebuild").val();
            var vehicleId = "";
            if (editOrRebuild == "1") {
                vehicleId = $("#brands").attr("data-id");
            } else {
                vehicleId = $("#brands2").attr("data-id");
            }
            var oilBoxVehicleIds = $("#oilBoxVehicleId1").val();
            var oilLevelHeights = "";
            var oilValues = "";
            $("input[name='oilLevelHeights']").each(function(){
                oilLevelHeights += this.value + ",";
            });
            $("input[name='oilValues']").each(function(){
                oilValues += this.value + ",";
            });
            if (oilLevelHeights.length > 0) {
                oilLevelHeights = oilLevelHeights.substr(0, oilLevelHeights.length - 1);
            }
            if (oilValues.length > 0) {
                oilValues = oilValues.substr(0, oilValues.length - 1);
            }
            if (oilLevelHeights.split(",").length < min_calibrationSets) {
                layer.msg("标定数据至少添加" + min_calibrationSets + "组");
                return;
            } else if (oilLevelHeights.split(",").length > max_calibrationSets) {
                layer.msg("标定数据最多添加" + max_calibrationSets + "组");
                return;
            }
            var url = "/clbs/v/oilmassmgt/oilcalibration/saveOilCalibration";
            var data = {"vehicleId" : vehicleId, "oilBoxVehicleIds" : oilBoxVehicleIds, "oilLevelHeights" : oilLevelHeights, "oilValues" : oilValues};
            if(inCompareMode){
                url = "/clbs/v/oilmassmgt/oilcalibration/sendOilCalibration";
            }
            json_ajax("POST", url, "json", true, data, olicalibrationPages.callBack3);
        },
        // 保存操作-油箱2
        saveOilcalibration2 : function () {
            var editOrRebuild = $("#editOrRebuild").val();
            var vehicleId = "";
            if (editOrRebuild == "1") {
                vehicleId = $("#brands").attr("data-id");
            } else {
                vehicleId = $("#brands2").attr("data-id");
            }
            var oilBoxVehicleIds = $("#oilBoxVehicleId2").val();
            var oilLevelHeights = "";
            var oilValues = "";
            $("input[name='oilLevelHeights2']").each(function(){
                oilLevelHeights += this.value + ",";
            });
            $("input[name='oilValues2']").each(function(){
                oilValues += this.value + ",";
            });
            if (oilLevelHeights.length > 0) {
                oilLevelHeights = oilLevelHeights.substr(0, oilLevelHeights.length - 1);
            }
            if (oilValues.length > 0) {
                oilValues = oilValues.substr(0, oilValues.length - 1);
            }
            if (oilLevelHeights.split(",").length < min_calibrationSets) {
                layer.msg("标定数据至少添加" + min_calibrationSets + "组");
                return;
            } else if (oilLevelHeights.split(",").length > max_calibrationSets) {
                layer.msg("标定数据最多添加" + max_calibrationSets + "组");
                return;
            }
            var url = "/clbs/v/oilmassmgt/oilcalibration/saveOilCalibration";
            var data = {"vehicleId" : vehicleId, "oilBoxVehicleIds" : oilBoxVehicleIds, "oilLevelHeights" : oilLevelHeights, "oilValues" : oilValues};
            if(inCompareMode){
                url = "/clbs/v/oilmassmgt/oilcalibration/sendOilCalibration";
            }
            json_ajax("POST", url, "json", true, data, olicalibrationPages.callBack3);
        },
        // 保存操作-油箱1和油箱2
        saveOilcalibration1_2 : function () {
            var editOrRebuild = $("#editOrRebuild").val();
            var vehicleId = "";
            if (editOrRebuild == "1") {
                vehicleId = $("#brands").attr("data-id");
            } else if (editOrRebuild == "2") {
                vehicleId = $("#brands2").attr("data-id");
            } else if (editOrRebuild == "3") {
                vehicleId = $("#brands3").attr("data-id");
            }

            if(!vehicleId || vehicleId.length==0){
                layer.msg(brandNullMsg)
                return;
            }
            // 重新标定 判断是否有数据
            if(editOrRebuild == "2"){
                if($("#curBox").val()=="1"){
                    if($("input[name='oilValues']").length==0){
                        layer.msg('请提交实际加油量')
                        return;
                    }
                }
                // 副油箱
                if($("#curBox").val()=="2"){
                    if($("input[name='oilValues2']").length==0){
                        layer.msg('请提交实际加油量')
                        return;
                    }
                }
            }
            // 确认框
            var msg=saveConfirmMsg;
            if(inCompareMode){
                msg='是否下发这次标定'
            }
            layer.confirm(msg, {btn : ["确定", "取消"]}, function () {
                var curBox = $("#curBox").val();

                var oilLevelHeight_before1;
                var oilLevelHeight_before2;
                var oilValue_before1;
                var oilValue_before2;
                var oilLevelHeight_after1;
                var oilLevelHeight_after2;
                var oilValue_after1;
                var oilValue_after2;
                var actualAddOil1;
                var actualAddOil2;
                var factorK_1 = 1;
                var factorK_2 = 1;


                // 判断车辆在线状态
                olicalibrationPages.checkVehicleOnlineStatus();
                if (!onLineStatus) {
                    return
                }

                if (editOrRebuild == "1") { // 修正标定
                    // 油箱1
                    oilLevelHeight_before1 = $("#corCalibration_tank1_oilLevelHeight_before").val();
                    oilValue_before1 = $("#corCalibration_tank1_oilValue_before").val();
                    oilLevelHeight_after1 = $("#corCalibration_tank1_oilLevelHeight_after").val();
                    oilValue_after1 = $("#corCalibration_tank1_oilValue_after").val();
                    actualAddOil1 = $("#corCalibration_tank1_actualAddOil").val();
                    var theoryAddOil1 = parseFloat(oilValue_after1) - parseFloat(oilValue_before1);
                    if (parseFloat(theoryAddOil1) > 0) {
                        // 修正系数K：K=实际加油量/[加油后油量<测>-加油前油量<测>]
                        factorK_1 = parseFloat(actualAddOil1) / parseFloat(theoryAddOil1);
                    }
                    // 油箱2
                    oilLevelHeight_before2 = $("#corCalibration_tank2_oilLevelHeight_before").val();
                    oilValue_before2 = $("#corCalibration_tank2_oilValue_before").val();
                    oilLevelHeight_after2 = $("#corCalibration_tank2_oilLevelHeight_after").val();
                    oilValue_after2 = $("#corCalibration_tank2_oilValue_after").val();
                    actualAddOil2 = $("#corCalibration_tank2_actualAddOil").val();
                    var theoryAddOil2 = parseFloat(oilValue_after2) - parseFloat(oilValue_before2);
                    if (parseFloat(theoryAddOil2) > 0) {
                        // 修正系数K：K=实际加油量/[加油后油量<测>-加油前油量<测>]
                        factorK_2 = parseFloat(actualAddOil2) / parseFloat(theoryAddOil2);
                    }

                }else if (editOrRebuild == "2"){
                    if($("#curBox").val()=="1"){
                        list2.obj.oilCalibration1=[];
                        $("input[name='oilLevelHeights']").each(function(index,ele){
                            list2.obj.oilCalibration1.push({
                                oilLevelHeight:this.value
                            });
                        });
                        $("input[name='oilValues']").each(function(index,ele){
                            list2.obj.oilCalibration1[index].oilValue=this.value;
                        });
                        if(list2.obj.oilCalibration1.length===0){
                            list2={
                                obj:{}
                            };
                            layer.msg('请提交实际加油量')
                            return;
                        }
                        list2.obj.oilCalibration1=list2.obj.oilCalibration1.sort(function(pre,next){
                            return parseFloat(pre.oilValue) > parseFloat(next.oilValue);
                        });
                    }

                    // 副油箱
                    if($("#curBox").val()=="2"){
                        list2.obj.oilCalibration2=[];
                        $("input[name='oilLevelHeights2']").each(function(){
                            list2.obj.oilCalibration2.push({
                                oilLevelHeight:this.value
                            });
                        });
                        $("input[name='oilValues2']").each(function(){
                            list2.obj.oilCalibration2[index].oilValue=this.value;
                        });
                        if(list2.obj.oilCalibration2.length===0){
                            list2={
                                obj:{}
                            };
                            layer.msg('请提交实际加油量')
                            return;
                        }
                        list2.obj.oilCalibration2=list2.obj.oilCalibration2.sort(function(pre,next){
                            return parseFloat(pre.oilValue) > parseFloat(next.oilValue);
                        });
                    }
                } else if (editOrRebuild == "3") {
                    // 油箱1
                    oilLevelHeight_before1 = $("#ascendCalibration_tank1_oilLevelHeight_before").val();
                    oilValue_before1 = $("#ascendCalibration_tank1_oilValue_before").val();
                    oilLevelHeight_after1 = $("#ascendCalibration_tank1_oilLevelHeight_after").val();
                    oilValue_after1 = $("#ascendCalibration_tank1_oilValue_after").val();
                    actualAddOil1 = $("#ascendCalibration_tank1_actualAddOil").val();
                    var theoryAddOil1 = parseFloat(oilValue_after1) - parseFloat(oilValue_before1);
                    if (parseFloat(theoryAddOil1) > 0) {
                        // 修正系数K：K=实际加油量/[加油后油量<测>-加油前油量<测>]
                        factorK_1 = parseFloat(actualAddOil1) / parseFloat(theoryAddOil1);
                    }
                    // 油箱2
                    oilLevelHeight_before2 = $("#ascendCalibration_tank2_oilLevelHeight_before").val();
                    oilValue_before2 = $("#ascendCalibration_tank2_oilValue_before").val();
                    oilLevelHeight_after2 = $("#ascendCalibration_tank2_oilLevelHeight_after").val();
                    oilValue_after2 = $("#ascendCalibration_tank2_oilValue_after").val();
                    actualAddOil2 = $("#ascendCalibration_tank2_actualAddOil").val();
                    var theoryAddOil2 = parseFloat(oilValue_after2) - parseFloat(oilValue_before2);
                    if (parseFloat(theoryAddOil2) > 0) {
                        // 修正系数K：K=实际加油量/[加油后油量<测>-加油前油量<测>]
                        factorK_2 = parseFloat(actualAddOil2) / parseFloat(theoryAddOil2);
                    }
                    if (actualAddOil1 == "" || isNaN(theoryAddOil1)) { // 油箱1没有填写实际加油量时，不用保存最后一次标定时间
                        time_after = "";
                    }
                    if (actualAddOil2 == "" || isNaN(theoryAddOil2)) { // 油箱2没有填写实际加油量时，不用保存最后一次标定时间
                        time_after2 = "";
                    }
                }
                if( (!list1.obj.oilCalibration1) && (!list1.obj.oilCalibration2) && editOrRebuild != "2"){
                    layer.msg('未获取标定数据');
                    return;
                }

                if(!inCompareMode && editOrRebuild != "2"){
                    if (!isNaN(factorK_1) && list1.obj.oilCalibration1 ) {
                        // 油箱1
                        list2.obj.oilCalibration1=list1.obj.oilCalibration1.map(function(ele){
                            var innerEle={};
                            innerEle.oilBoxVehicleId=ele.oilBoxVehicleId; // 车辆ID
                            innerEle.oilLevelHeight=ele.oilLevelHeight; // 液位高度
                            innerEle.oilValue=toFixed(parseFloat(factorK_1) * parseFloat(ele.oilValue),1,true); // 用最原始的标定数据乘以修正系数，不然数据不准
                            return innerEle;
                        });

                    }
                    if (!isNaN(factorK_2) && list1.obj.oilCalibration2) {
                        // 油箱2
                        list2.obj.oilCalibration2=list1.obj.oilCalibration2.map(function(ele){
                            var innerEle={};
                            innerEle.oilBoxVehicleId=ele.oilBoxVehicleId; // 车辆ID
                            innerEle.oilLevelHeight=ele.oilLevelHeight; // 液位高度
                            innerEle.oilValue=toFixed(parseFloat(factorK_1) * parseFloat(ele.oilValue),1,true); // 用最原始的标定数据乘以修正系数，不然数据不准
                            return innerEle;
                        });
                    }
                }
                //var editOrRebuild = $("#editOrRebuild").val();


                // 油箱1
                var oilBoxVehicleIds = $("#oilBoxVehicleId1").val();

                // 判断数据组数 是否在最大值和最小值之间
                if (list2.obj.oilCalibration1.length < min_calibrationSets) {
                    layer.msg("【油箱1】标定数据至少添加" + min_calibrationSets + "组");
                    list2={
                        obj:{}
                    };
                    return;
                } else if (list2.obj.oilCalibration1.length > max_calibrationSets) {
                    layer.msg("【油箱1】标定数据最多添加" + max_calibrationSets + "组");
                    list2={
                        obj:{}
                    };
                    return;
                }

                // 将数据由数组转换为 逗号分割的字符串
                var oilLevelHeights = list2.obj.oilCalibration1.map(function(ele){
                    return ele.oilLevelHeight;
                }).join(',');
                var oilValues = list2.obj.oilCalibration1.map(function(ele){
                    return ele.oilValue;
                }).join(',');


                // 油箱2
                var oilBoxVehicleIds2 = $("#oilBoxVehicleId2").val();

                var oilLevelHeights2="";
                var oilValues2="";

                // 如有有副邮箱数据才计算
                if(list2.obj.oilCalibration2){
                    // 判断数据组数 是否在最大值和最小值之间
                    if (list2.obj.oilCalibration2.length < min_calibrationSets) {
                        layer.msg("【油箱2】标定数据至少添加" + min_calibrationSets + "组");
                        list2={
                            obj:{}
                        };
                        return;
                    } else if (list2.obj.oilCalibration2.length > max_calibrationSets) {
                        layer.msg("【油箱2】标定数据最多添加" + max_calibrationSets + "组");
                        list2={
                            obj:{}
                        };
                        return;
                    }

                    // 将数据由数组转换为 逗号分割的字符串
                    oilLevelHeights2 = list2.obj.oilCalibration2.map(function(ele){
                        return ele.oilLevelHeight;
                    }).join(',');
                    oilValues2 = list2.obj.oilCalibration2.map(function(ele){
                        return ele.oilValue;
                    });
                }
                // 立即显示追溯标定的修正数据
                if(editOrRebuild=="3"){
                    var oilCalibration1;
                    var oilCalibration2;
                    var oilCalibration1_list2;
                    var oilCalibration2_list2;
                    oilCalibration1 = list1.obj.oilCalibration1;
                    oilCalibration2 = list1.obj.oilCalibration2;
                    oilCalibration1_list2 = list2.obj.oilCalibration1;
                    oilCalibration2_list2 = list2.obj.oilCalibration2;

                    var redStyle='style="background-color:rgb(255,230,230)"';
                    if($("#curBox").val()=="1"){
                        $("#dataList").empty();
                        $("#dataList3").empty();
                        for (var i=0,l=Math.max(nvl(oilCalibration1,0),nvl(oilCalibration1_list2,0)); i<l; i++) {
                            var list2Height='',list2Oil='',list1Height='',list1Oil='';
                            if(oilCalibration1_list2 && i<oilCalibration1_list2.length){
                                list2Height = toFixed(oilCalibration1_list2[i].oilLevelHeight,2,true);
                                list2Oil = toFixed(oilCalibration1_list2[i].oilValue,1,true)
                            }
                            if(i<oilCalibration1.length){
                                list1Height = toFixed(oilCalibration1[i].oilLevelHeight,2,true);
                                list1Oil = toFixed(oilCalibration1[i].oilValue,1,true)
                            }

                            var tmpStyle=list1Oil !== list2Oil && list2.obj.oilCalibration1 ? redStyle : '';

                            var str="";
                            str += "<tr class='odd'>";
                            str += "<td class='text-center'>" + (i+1) + "</td>"; // 序号
                            str += "<td class='text-center' "+tmpStyle+">" + list1Height + "</td>"; // 设备液位高度
                            str += "<td class='text-center' "+tmpStyle+">" + list1Oil + "</td>"; // 设备油量值
                            str += "</tr>";

                            var str2="";
                            str2 += "<tr class='odd'>";
                            str2 += "<td class='text-center'>" + (i+1) + "</td>"; // 序号
                            str2 += "<td class='text-center' "+tmpStyle+">" + list2Height + "</td>"; // 修正液位高度
                            str2 += "<td class='text-center' "+tmpStyle+">" + list2Oil + "</td>"; // 修正油量值
                            str2 += "</tr>";

                            $("#dataList").append(str);
                            $("#dataList3").append(str2);
                        }
                    }else{
                        $("#dataList2").empty();
                        $("#dataList4").empty();
                        for (var i=0,l=Math.max(nvl(oilCalibration2,0),nvl(oilCalibration2_list2,0)); i<l; i++) {

                            var list2Height='',list2Oil='',list1Height='',list1Oil='';
                            if(oilCalibration2_list2 && i<oilCalibration2_list2.length){
                                list2Height = toFixed(oilCalibration2_list2[i].oilLevelHeight,2,true);
                                list2Oil = toFixed(oilCalibration2_list2[i].oilValue,1,true)
                            }
                            if(i<oilCalibration2.length){
                                list1Height = toFixed(oilCalibration2[i].oilLevelHeight,2,true);
                                list1Oil = toFixed(oilCalibration2[i].oilValue,1,true)
                            }

                            var tmpStyle=list1Oil !== list2Oil && list2.obj.oilCalibration2 ? redStyle : '';

                            var str="";
                            str += "<tr class='odd'>";
                            str += "<td class='text-center'>" + (i+1) + "</td>"; // 序号
                            str += "<td class='text-center' "+tmpStyle+">" + list1Height + "</td>"; // 设备液位高度
                            str += "<td class='text-center' "+tmpStyle+">" + list1Oil + "</td>"; // 设备油量值
                            str += "</tr>";

                            var str2="";
                            str2 += "<tr class='odd'>";
                            str2 += "<td class='text-center'>" + (i+1) + "</td>"; // 序号
                            str2 += "<td class='text-center' "+tmpStyle+">" + list2Height + "</td>"; // 修正液位高度
                            str2 += "<td class='text-center' "+tmpStyle+">" + list2Oil + "</td>"; // 修正油量值
                            str2 += "</tr>";

                            $("#dataList2").append(str);
                            $("#dataList4").append(str2);
                        }
                    }

                }
                layer.closeAll();
                layer.load(2);
                var url = "/clbs/v/oilmassmgt/oilcalibration/saveOilCalibration";
                if(inCompareMode){
                    url = "/clbs/v/oilmassmgt/oilcalibration/sendOilCalibration";
                }
                var data = {"vehicleId" : vehicleId,
                    "settingParamId" : $("#settingParamId").val(),
                    "calibrationParamId" : $("#calibrationParamId").val(),
                    "transmissionParamId" : $("#transmissionParamId").val()};
                if($("#curBox").val()=="1"){
                    // 主油箱
                    data.oilBoxVehicleIds=oilBoxVehicleIds;
                    data.oilLevelHeights=oilLevelHeights;
                    data.oilValues=oilValues;

                }else{
                    // 副油箱
                    data.oilBoxVehicleIds2=oilBoxVehicleIds2;
                    data.oilLevelHeights2=oilLevelHeights2;
                    data.oilValues2=oilValues2;
                }
                json_ajax("POST", url, "json", true, data, olicalibrationPages.callBack3);
            },function(){


            });

        },
        callBack3 : function (data) {
            if (data.success) {
                if (data != null && data.obj != null && data.obj.msgSN != null && data.obj.msgSN != "") {
                    $("#msgSN").val(data.obj.msgSN);
                }

                // 禁用修正下发按钮
                submitBtn.attr('disabled','disabled');
                sendCompareBtn.attr('disabled','disabled');
                // 下发参数后，获取终端通用应答消息
                sendStatus4Amendment.val('参数下发中');
                layer_time = window.setTimeout(function () {
                    layer.load(2);
                }, 0);
                isRead=true;
                clearTimeout(_timeout);
                _timeout= window.setTimeout(olicalibrationPages.handleSendCalibrationFailed,60000);
                olicalibrationPages.commonResponse();


            } else {
                layer.msg(data.msg);
            }
        },
        // 追溯标定-保存车辆最后一次标定的时间
        saveLastCalibration : function () {
            // 备注:如果没有填写实际加油量，表示没有标定当前油箱，则当前油箱的最后一次标定的时间不作修改，赋值为从数据库查询出来的最原始的值
            var actualAddOil1 = $("#ascendCalibration_tank1_actualAddOil").val();
            var actualAddOil2 = $("#ascendCalibration_tank2_actualAddOil").val();
            if (actualAddOil1 == "") {
                time_after = $("#origionalTime_tank1").val();
            }
            if (actualAddOil2 == "") {
                time_after2 = $("#origionalTime_tank2").val();
            }
            var vehicleId = $("#brands3").attr("data-id");
            var oilBoxVehicleIds = $("#oilBoxVehicleId1").val();
            var oilBoxVehicleIds2 = $("#oilBoxVehicleId2").val();
            var url = "/clbs/v/oilmassmgt/oilcalibration/saveLastOilCalibration";
            var data = {"vehicleId" : vehicleId};
            if($("#curBox").val()=="1"){
                data.oilBoxVehicleIds=oilBoxVehicleIds;
                data.tank1Last=time_after;
            }else{
                data.oilBoxVehicleIds2=oilBoxVehicleIds2;
                data.tank2Last=time_after2;
            }

            json_ajax("POST", url, "json", true, data, olicalibrationPages.saveLastCalibrationCallback(time_after,time_after2));
        },
        // 追溯标定-保存最后一次标定时间回调
        saveLastCalibrationCallback : function(timeAfterParam,timeAfter2Param){
            return function (data) {
                if (data.success) {
                    $("#tank1LastTime").val(timeAfterParam);
                    $("#tank2LastTime").val(timeAfter2Param);
                    $("#origionalTime_tank1").val(timeAfterParam);
                    $("#origionalTime_tank2").val(timeAfter2Param);
                }else{
                    layer.msg(data.msg,{move:false});
                }
            }
        },
        // 摊销标定
        cancleSaveBtnClick : function () {
            var editOrRebuild = $("#editOrRebuild").val();
            var curBox = $("#curBox").val();
            var msg = cancleConfirmMsg;
            if(editOrRebuild == "2"){
                msg='确定要撤销最近一次标定数据吗？'
            }
            layer.confirm(msg, {btn : ["确定", "取消"]}, function () {
                sendStatus4Amendment.val('');
                if (editOrRebuild == "1") { // 修正标定
                    layer.closeAll();
                    olicalibrationPages.updateCalibrationStatus($("#vehicleId").val());
                    $("#corResetBtn").click();
                    $("#vehicleId").val("");
                    $("#dataList").empty();
                    $("#dataList2").empty();
                    olicalibrationPages.initForm();
                } else if (editOrRebuild == "2") { // 重新标定
                    var v = $($("#dataList tr:eq(0) td:eq(2)").html());
                    var value ;
                    if (typeof(v) != undefined) {
                        value = v.val();
                    } else {
                        value = 0;
                    }
                    // 撤销按钮删除最上面一条数据
                    if (curBox == "1") { // 油箱1
                        $("#dataList tr:eq(0)").remove();
                        tempOilValue = parseFloat(tempOilValue) - parseFloat(value);
                        if (num > 0) {
                            num --;
                        }
                        if (seqNo > 0) {
                            seqNo --;
                        }
                        var re_tank1_oilHeights = $("#ReCalibration_tank1_oilLevelHeights").val();
                        var re_tank1_oilValues = $("#ReCalibration_tank1_oilValues").val();
                        if (re_tank1_oilHeights.length > 0) {
                            re_tank1_oilHeights = re_tank1_oilHeights.substr(0, re_tank1_oilHeights.length - 1);
                            re_tank1_oilHeights = re_tank1_oilHeights.substr(0, re_tank1_oilHeights.lastIndexOf(','));
                            $("#ReCalibration_tank1_oilLevelHeights").val(re_tank1_oilHeights + ",");
                        }
                        if (re_tank1_oilValues.length > 0) {
                            re_tank1_oilValues = re_tank1_oilValues.substr(0, re_tank1_oilValues.length - 1);
                            re_tank1_oilValues = re_tank1_oilValues.substr(0, re_tank1_oilValues.lastIndexOf(','));
                            $("#ReCalibration_tank1_oilValues").val(re_tank1_oilValues + ",");
                        }
                    } else { // 油箱2
                        $("#dataList2 tr:eq(0)").remove();
                        tempOilValue2 = parseFloat(tempOilValue2) - parseFloat(value);
                        if (num > 0) {
                            num --;
                        }
                        if (seqNo2 > 0) {
                            seqNo2 --;
                        }
                        var re_tank2_oilHeights = $("#ReCalibration_tank2_oilLevelHeights").val();
                        var re_tank2_oilValues = $("#ReCalibration_tank2_oilValues").val();
                        if (re_tank2_oilHeights.length > 0) {
                            re_tank2_oilHeights = re_tank2_oilHeights.substr(0, re_tank2_oilHeights.length - 1);
                            re_tank2_oilHeights = re_tank2_oilHeights.substr(0, re_tank2_oilHeights.lastIndexOf(','));
                            $("#ReCalibration_tank2_oilLevelHeights").val(re_tank2_oilHeights + ",");
                        }
                        if (re_tank2_oilValues.length > 0) {
                            re_tank2_oilValues = re_tank2_oilValues.substr(0, re_tank2_oilValues.length - 1);
                            re_tank2_oilValues = re_tank2_oilValues.substr(0, re_tank2_oilValues.lastIndexOf(','));
                            $("#ReCalibration_tank2_oilValues").val(re_tank2_oilValues + ",");
                        }
                    }
                    $("#ReCalibration_tank1_seqNo").val(seqNo);
                    $("#ReCalibration_tank2_seqNo").val(seqNo2);
                    $("#ReCalibration_num").val(num);
                    layer.closeAll();
                } else if (editOrRebuild == "3") { // 追溯标定
                    layer.closeAll();
                    olicalibrationPages.updateCalibrationStatus($("#vehicleId").val());
                    $("#ascendResetBtn").click();
                    $("#vehicleId").val("");
                    $("#brands3").val("");
                    $("#brands3").attr("data-id", "");
                    $("#graphShow").hide();
                    $("#sjcontainer").empty();
                    $("#dataList").empty();
                    $("#dataList2").empty();
                    $("#dataList3").empty();
                    $("#dataList4").empty();
                    olicalibrationPages.initForm3();
                }
            }, function () {});
        },
        // 重新标定-加油前点我事件,"加油前点我"修改为"刷新最新值"
        submitBefore2Click : function () {
            $("#curClickedBtn").val("3");
            initFlag = true;
            var curBox = $("#curBox").val();
            var vehicleId = $("#brands2").attr("data-id");
            if (vehicleId == "" || typeof(vehicleId) == undefined || typeof(vehicleId) == 'undefined') {
                layer.msg(brandNullMsg);
                return;
            }
            // 判断车辆在线状态
            olicalibrationPages.checkVehicleOnlineStatus();
            if (onLineStatus) {
                // 订阅车辆位置信息
                olicalibrationPages.subscribeLatestLocation();
                var url = "/clbs/v/oilmassmgt/oilcalibration/getLatestOilData";
                var data = {"vehicleId" : vehicleId, "curBox" : curBox};
                json_ajax("POST", url, "json", true, data, olicalibrationPages.callBack4);
                // 加载框
                olicalibrationPages.loading();
            }
        },
        callBack4 : function (data) {
            if(data.success){
                if (data != null && data.obj != null && data.obj.msgSN != null && data.obj.msgSN != "") {
                    $("#msgSN").val(data.obj.msgSN);
                }
                window.setTimeout(function () {
                    var editOrRebuild = $("#editOrRebuild").val();
                    if ($("#oilLevelHeight2_before").val() == "" && editOrRebuild == "2") {
                        layer.msg(getDataFailMsg);
                    }
                },7000);
            }else{
                layer.msg(data.msg,{move:false});
            }
        },
        // 重新标定-提交按钮
        submitBtn2Click : function () {
            // 订阅车辆位置信息
            var curBox = $("#curBox").val();
            $("#curClickedBtn").val("4");
            initFlag = false;
            var oilLevelHeight2_before = $("#oilLevelHeight2_before").val();
            var oilValue2_before = $("#oilValue2_before").val();
            if ($("#brands2").val() == "") {
                layer.msg(brandNullMsg);
                return;
            } else if (oilLevelHeight2_before == "" || oilValue2_before == "") {
                layer.msg(reCalRefreshMsg);
                return;
            } else if ($("#actualAddOil2").val() == "") {
                layer.msg(actualAddOilNullMsg);
                return;
            } else if (isNaN($("#actualAddOil2").val())) {
                layer.msg(actualAddOilIsRightNumMsg);
                return;
            } else if (parseFloat($("#actualAddOil2").val()) <= 0) {
                layer.msg(actualAddOilIsAboveZeroMsg);
                return;
            }else if($("#oilADHeight").val()== "0 (AD值)"){
                layer.msg('当前AD值为0，不能提交');
                return;
            }
                if (curBox == "1") { // 油箱1实际加油量
                    $("#ReCalibration_tank1_actualAddOil").val($("#actualAddOil2").val());
                } else if (curBox == "2") { // 油箱2实际加油量
                    $("#ReCalibration_tank2_actualAddOil").val($("#actualAddOil2").val());
                }

            var oilLevelHeight = $("#oilLevelHeight2_before").val();
            oilLevelHeight = oilLevelHeight != "" ? oilLevelHeight.substr(0, oilLevelHeight.length - 3) : "";
            var actualAddOil = $("#actualAddOil2").val();
            var oilValue;
            oilValue = actualAddOil;
            // 添加行之前，判断当前获取的油量值是否比上一条大，如果不比前一条大，则不添加；
            var v;
            if (curBox == "1") { // 油箱1
                v = $($("#dataList tr:eq(0) td:eq(1)").html()); // convert
                                                                // html to
                                                                // jQuery
                                                                // Element:
                                                                // $(html)
                if (typeof(v.val()) != undefined) {
                    if (parseFloat(oilLevelHeight) <= parseFloat(v.val())) {
                        layer.msg(addRowMsg);
                        return;
                    }
                        olicalibrationPages.addRow(curBox, oilLevelHeight, oilValue, actualAddOil);

                }
            } else { // 油箱2
                v = $($("#dataList2 tr:eq(0) td:eq(1)").html()); // convert
                // html
                // to
                // jQuery
                // Element:
                // $(html)
                if (typeof(v.val()) != undefined) {
                    if (parseFloat(oilLevelHeight) <= parseFloat(v.val())) {
                        layer.msg(addRowMsg);
                        return;
                    }
                        olicalibrationPages.addRow(curBox, oilLevelHeight, oilValue, actualAddOil);

                }
            }
        },
        callBack5 : function (data) {
            if(data.success){
                if (data != null && data.obj != null && data.obj.msgSN != null && data.obj.msgSN != "") {
                    $("#msgSN").val(data.obj.msgSN);
                }
            }else{
                layer.msg(data.msg,{move:false});
            }
        },
        addRow : function (curBox, oilLevelHeight, oilValue, actualAddOil) {
            num ++;
            $("#ReCalibration_num").val(num);
            if (curBox == "1") { // 油箱1
                if ($("#dataList").find("tr").length > 0) {
                    var v = $($("#dataList tr:eq(0) td:eq(2)").html());
                    var value ;
                    if (typeof(v) != undefined) {
                        value = v.val();
                    } else {
                        value = 0;
                    }
                    tempOilValue = (parseFloat(value) + parseFloat(actualAddOil)).toFixed(1);
                } else {
                    tempOilValue = parseFloat(oilValue).toFixed(1);
                }
                // 油箱1-记录液位高度和油量值，修正标定和重新标定切换时值的回显
                re_tank1_oilHeights += oilLevelHeight + ",";
                re_tank1_oilValues += tempOilValue + ",";
                $("#ReCalibration_tank1_oilLevelHeights").val(re_tank1_oilHeights);
                $("#ReCalibration_tank1_oilValues").val(re_tank1_oilValues);

                var str = "";
                str += "<tr class='odd'>";
                str += "<td class='text-center'>" + (seqNo++) + "</td>"; // 序号
                str += "<td class='text-center'>" +
                    "<input readonly name='oilLevelHeights' id='oilLevelHeights"+num+"' value='" + toFixed(oilLevelHeight,2,true) + "' onkeyup='value=value.replace(/[^0-9]/g,\"\")' maxlength='4' style='border:none;width:80%;text-align:center;background-color:transparent;' />" +
                    "</td>"; // 车牌号
                str += "<td class='text-center'><input readonly name='oilValues' id='oilValues"+num+"' value='" + toFixed(tempOilValue,1,true) + "' onkeyup='value=value.replace(/[^0-9.]/g,\"\")' maxlength='10' style='border:none;width:80%;text-align:center;background-color:transparent;' /></td>"; // 车辆类型
                str += "</tr>";
                $("#dataList").prepend(str);
                $("#ReCalibration_tank1_seqNo").val(seqNo);
            } else if (curBox == "2") { // 油箱2
                if ($("#dataList2").find("tr").length > 0) {
                    var v = $($("#dataList2 tr:eq(0) td:eq(2)").html());
                    var value ;
                    if (typeof(v) != undefined) {
                        value = v.val();
                    } else {
                        value = 0;
                    }
                    tempOilValue2 = (parseFloat(value) + parseFloat(actualAddOil)).toFixed(1);
                } else {
                    tempOilValue2 = parseFloat(oilValue).toFixed(1);
                }
                // 油箱2-记录液位高度和油量值，修正标定和重新标定切换时值的回显
                re_tank2_oilHeights += oilLevelHeight + ",";
                re_tank2_oilValues += tempOilValue2 + ",";
                $("#ReCalibration_tank2_oilLevelHeights").val(re_tank2_oilHeights);
                $("#ReCalibration_tank2_oilValues").val(re_tank2_oilValues);

                var str = "";
                str += "<tr class='odd'>";
                str += "<td class='text-center'>" + (seqNo2++) + "</td>"; // 序号
                str += "<td class='text-center'>" +
                    "<input readonly name='oilLevelHeights' id='oilLevelHeights"+num+"' value='" + toFixed(oilLevelHeight,2,true) + "' onkeyup='value=value.replace(/[^0-9]/g,\"\")' maxlength='4' style='border:none;width:80%;text-align:center;background-color:transparent;' />" +
                    "</td>"; // 车牌号
                str += "<td class='text-center'><input readonly name='oilValues' id='oilValues"+num+"' value='" + toFixed(tempOilValue2,1,true) + "' onkeyup='value=value.replace(/[^0-9.]/g,\"\")' maxlength='10' style='border:none;width:80%;text-align:center;background-color:transparent;' /></td>"; // 车辆类型
                str += "</tr>";
                $("#dataList2").prepend(str);
                $("#ReCalibration_tank2_seqNo").val(seqNo2);
            }
        },
        iconChange: function(){
            if ($(this).hasClass("fa-chevron-up")) {
                console.log('展开');
                $(this).attr("class", "fa fa-chevron-down");
                $("#graphShow").show();
            } else {
                console.log('关闭');
                $(this).attr("class", "fa fa-chevron-up");
                $("#graphShow").hide();
            }
        },
        // 获取当前标定状态
        getCalibrationFlag : function () {
            return calibrationFlag;
        },
        // 车辆选择框获取焦点事件
        brandsFocus : function () {
            var editOrRebuild = $("#editOrRebuild").val();
            if (editOrRebuild == "1") { // 实时修正
                var value = $("#brands").val();
                if (value == "") {
                    $("#submitBefore").attr("disabled", false);
                    $("#submitAfter").attr("disabled", false);
                    $("#submitBtn").attr("disabled", false);
                }
            } else if (editOrRebuild == "2") {
                var value = $("#brands2").val();
                if (value == "") {
                    $("#submitBefore2").attr("disabled", false);
                    $("#submitBtn2").attr("disabled", false);
                    $("#saveOilCalibrationBtn").attr("disabled", false);
                }
            } else if (editOrRebuild == "3") {
                var value = $("#brands3").val();
                if (value == "") {
                    $("#submit_btn").attr("disabled", false);
                    $("#todayClick").attr("disabled", false);
                    $("#yesterdayClick").attr("disabled", false);
                    $("#inquireClick").attr("disabled", false);
                }
            }
        },
        // 根据当前是在实时修正，重新标定，追溯标定的那个来决定下发按钮执行啥
        dispatchSendCompareBtn:function(){
            //saveOilcalibration1_2 submitBtnClick
            inCompareMode=true;
            var editOrRebuild = $("#editOrRebuild").val();
            if (editOrRebuild == "1") { // 实时修正
                olicalibrationPages.submitBtnClick();
            } else if (editOrRebuild == "2") {
                olicalibrationPages.saveOilcalibration1_2();
            } else if (editOrRebuild == "3") {

            }
        }
    }
    $(function(){
        $('input').inputClear();
        // 初始化
        olicalibrationPages.init();
        $('#timeInterval').dateRangePicker();
        olicalibrationPages.nowDay();

        $("#showClick").on("click",olicalibrationPages.iconChange);

        againCalibration.bind("click",olicalibrationPages.againCalibrationClick);
        amendmentCalibration.bind("click",olicalibrationPages.amendmentCalibrationClick);
        tankOneCutover.bind("click",olicalibrationPages.tankOneCutoverClick);
        tankTwoCutover.bind("click",olicalibrationPages.tankTwoCutoverClick);
        submitBefore.bind("click", olicalibrationPages.submitBeforeClick);
        submitAfter.bind("click", olicalibrationPages.submitAfterClick);
        submitBtn.bind("click", olicalibrationPages.submitBtnClick);
        compareBtn.bind("click", olicalibrationPages.refreshBtnClick);

        corCancleBtn.bind("click", olicalibrationPages.cancleSaveBtnClick);
        saveOilCalibrationBtn.bind("click", olicalibrationPages.saveOilcalibration1_2);
        cancleSaveBtn.bind("click", olicalibrationPages.cancleSaveBtnClick);
        // 实时修正
        $("#brands").bind("focus", olicalibrationPages.brandsFocus);
        $("#actualAddOil").bind("input propertychange", olicalibrationPages.actualAddOilChange);
        // 重新标定
        $("#brands2").bind("focus", olicalibrationPages.brandsFocus);
        $("#actualAddOil2").bind("input propertychange", olicalibrationPages.actualAddOil2Change);
        submitBefore2.bind("click", olicalibrationPages.submitBefore2Click);
        submitBtn2.bind("click", olicalibrationPages.submitBtn2Click);
        tankRadio1.bind("click",olicalibrationPages.tankRadio1Click);
        tankRadio2.bind("click",olicalibrationPages.tankRadio2Click);
        tankRadio3.bind("click",olicalibrationPages.tankRadio3Click);
        tankRadio4.bind("click",olicalibrationPages.tankRadio4Click);
        tankRadio5.bind("click",olicalibrationPages.tankRadio5Click);
        tankRadio6.bind("click",olicalibrationPages.tankRadio6Click);
        // 追溯标定
        $("#brands3").bind("focus", olicalibrationPages.brandsFocus);
        $("#actualAddOil3").bind("input propertychange", olicalibrationPages.actualAddOil3Change);
        $("#todayClick").bind("click", olicalibrationPages.todayClick);
        $("#yesterdayClick").bind("click", olicalibrationPages.yesterdayClick);
        $("#inquireClick").bind("click", olicalibrationPages.inquireClick);
        $("#ascendDemarcate").on("click", olicalibrationPages.ascendDemarcateClick);
        $("#refreshDataBtn3").bind("click",olicalibrationPages.refreshDataBtn3Click);
        submit_btn.bind("click", olicalibrationPages.submit_btnClick);
        cancle_btn.bind("click", olicalibrationPages.cancleSaveBtnClick);
    })
}(window,$))
