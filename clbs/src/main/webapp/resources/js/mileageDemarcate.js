(function (window, $) {
    var startTime;
    var endTime;
    var mileValue;
    var timeValue;
    // 订阅车辆消息
    var params = [];
    //开始里程
    var mile_before = 200;
    var time_before = '';
    //结束里程
    var mile_after = 236;
    var time_after = '';
    var min_time_after = '';
    var calibrationVehicleId = '';//当前标定车辆
    var sendStatus = -1; // -1初始化  0 成功 1失败
    var onLineStatus = false; // 标识车辆是否在线
    var calibrationFlag = false; // 标识是否在标定
    var checkConfigSenorFlag = false; // 标识是否绑定传感器
    var checkAllowBindFlag = false; // 是否可以设置里程数据
    var checkAllowQuery = false; // 是否还需要弹出取消确认框
    var deal_timeout;//修改修正数据定时器
    var mileageData = [];
    var speedData = [];
    var accData = [];
    var vTime = [];

    // 车辆list
    var vehicleList = JSON.parse($("#vehicleList").attr("value"));

    mileageDemarcate = {
        // 初始化车辆列表
        initVehicleInfoList: function () {
            //监控关闭刷新等操作
            mileageDemarcate.pageOnbeforeonload();
            // webSocket.init('/clbs/vehicle');
            // 初始化车辆数据
            var selectList = {value: []};
            if (vehicleList != null && vehicleList.length > 0) {
                for (var i = 0; i < vehicleList.length; i++) {
                    var obj = {};
                    obj.name = vehicleList[i].plate;
                    obj.id = vehicleList[i].vehicleId;
                    if (!isPlateNo(obj.name))
                        continue;
                    // 车辆下拉列表显示,去除重复数据
                    if (selectList.value != null && selectList.value.length > 0) {
                        var f = false;
                        for (var j = 0; j < selectList.value.length; j++) {
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
            $("#charSelect").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                data: selectList,
                effectiveFields: ["name"]
            }).on('onDataRequestSuccess', function (e, result) {
            }).on("click", function () {
            }).on('onSetSelectValue', function (e, keyword, data) {
            }).on('onUnsetSelectValue', function () {
            });
        },
        // 获取当前标定状态
        getCalibrationFlag: function () {
            return calibrationFlag;
        },
        // 判断车辆是否在线
        checkVehicleOnlineStatus: function () {
            if ($("#vehicleId").val() != null && $("#vehicleId").val() != "") {
                var url = "/clbs/v/oilmassmgt/oilcalibration/checkVehicleOnlineStatus";
                var data = {"vehicleId": $("#vehicleId").val()};
                json_ajax("POST", url, "json", false, data, mileageDemarcate.checkVehicleOnlineStatusCallBack);
            }
        },
        // 判断车辆是否在线回调
        checkVehicleOnlineStatusCallBack: function (data) {
            if (data.success) {
                onLineStatus = true; // 在线
                return true;
            } else if (!data.success && data.msg == null) {
                onLineStatus = false; // 不在线
                return false;
            } else if (!data.success && data.msg != null) {
                layer.msg(data.msg, {move: false});
            }
        },
        // 获取当前系统时间：yyyy-MM-dd HH:mm:ss
        getNowFormatDate: function () {
            var date = new Date();
            var seperator1 = "-";
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
        showChart: function (data) {
            if (!data.success) {
                layer.msg(data.msg);
                return;
            }
            //显示相关内容
            $("#graphShow").css({"height": "680px", "display": "block"});
            $(".carName,.left-btn,.right-btn,.item-title").css("display", "block");
            $("#carName").text($("input[name='charSelect']").val());
            $("#faChevronDown").removeClass("fa fa-chevron-up");
            $("#faChevronDown").addClass("fa fa-chevron-down");
            var msg = $.parseJSON(data.msg);
            mileageData = [];
            speedData = [];
            accData = [];
            vTime = [];
            mile_before = "";
            mile_after = "";
            mileageDemarcate.mileageChart();
            var positionals = msg.positionals;
            var miles;
            var speeds;
            for (var i = 0; i < positionals.length; i++) {
                miles = Number(positionals[i].mileageTotal === undefined ? 0 : Number(positionals[i].mileageTotal).toFixed(2));
                speeds = Number(positionals[i].mileageSpeed === undefined ? 0 : Number(positionals[i].mileageSpeed).toFixed(2));
                if (miles == NaN || miles == 0) {
                    miles = "";
                }
                if (speeds == null || miles == NaN || miles == 0) {
                    speeds = "";
                }
                var vtime = positionals[i].vtime;
                if (i != positionals.length - 1) {
                    var vtime1 = positionals[i + 1].vtime;
                }
                var time = vtime1 - vtime;
                if (time > 300) {
                    var j = Math.floor(time / 300);
                    for (var k = 0; k < j; k++) {
                        vtime = vtime + 300;
                        var time = mileageDemarcate.getTime(vtime);
                        vTime.push(time);
                        mileageData.push("");
                        speedData.push("");
                        accData.push("");
                    }
                }
                var time = mileageDemarcate.getTime(positionals[i].vtime);
                var acc = parseInt(positionals[i].acc);
                mileageData.push(miles);
                speedData.push(speeds);
                accData.push(acc);
                vTime.push(time);
            }
            $("#travelTime").html(msg.travelTime);
            $("#stopTime").html(msg.idleTime);
            var totalMaile = msg.totalMaile;//.replace(/公里/g, "km");
            totalMaile = mileageDemarcate.fiterNumber(totalMaile);
            $("#totalMileage").html(totalMaile + "km");
            var averageVelocity = msg.averageVelocity;//.replace("公里/时", "km/h");
            averageVelocity = mileageDemarcate.fiterNumber(averageVelocity);
            $("#averageSpeed").html(averageVelocity + "km/h");
            //更新车辆为标定状态
            mileageDemarcate.checkCalibrationStatus();
            checkConfigSenorFlag = msg.isCheckConfig;//true 存在配置  false 不存在配置
            if (!checkConfigSenorFlag) { // 未绑定油箱
                $("#submit_btn").attr("disabled", "disabled");
                $("#actualMileage").attr("disabled", true);
            } else { // 已绑定
                $("#actualMileage").attr("disabled", false);
                setTimeout(function () {
                    mileageDemarcate.checkVehicleOnlineStatus();
                }, 1000);
            }
            //开始里程
            mile_before = mileageDemarcate.fiterNumber(msg.mile_before);
            time_before = msg.time_before;
            min_time_after = msg.min_time_after;
            //结束里程
            mile_after = mileageDemarcate.fiterNumber(msg.mile_after);
            time_after = msg.time_after;
            //显示相关内容
            $("#graphShow").css({"height": "680px", "display": "block"});
            $(".carName,.left-btn,.right-btn,.item-title").css("display", "block");
            $("#carName").text($("input[name='charSelect']").val());
            //查询显示图表
            mileageDemarcate.mileageChart();
        },
        //图表
        mileageChart: function () {
            /*if (searchHandle) {
                for(var i = 0, len = mileageDemarcate.accData.length; i < len; i++){
                    if(mileageDemarcate.accData[i] == 1){
                        mileageDemarcate.accData[i] = 0;
                    }else if(mileageDemarcate.accData[i] == 0){
                        mileageDemarcate.accData[i] = 1
                    };
                };
                searchHandle = false;
            }*/
            //图表
            var myChart = echarts.init(document.getElementById('mileageChart'));
            $("#form_endTime").val(time_after);
            var option = {
                tooltip: {
                    trigger: 'axis',
                    textStyle: {
                        fontSize: 20
                    },
                    formatter: function (a) {
                        var unit = ['km', 'km/h', 'ACC'];
                        var relVal = "";
                        relVal = a[0].name;
                        if (a[0].data == null || a[0].data == 0) {
                            relVal = "无相关数据";

                        } else {
                            for (var i = 0; i < a.length; i++) {
                                if (a[i].seriesName == "ACC") {
                                    if (a[i].data == 1) {
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：关闭";
                                    } else {
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：开启";
                                    }
                                } else {
                                    if (a[i].seriesName == "里程") {
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：" + parseFloat(Number(a[i].data).toFixed(1)) + unit[a[i].seriesIndex] + "";
                                    } else {
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：" + parseFloat(Number(a[i].data).toFixed(2)) + unit[a[i].seriesIndex] + "";
                                    }

                                }
                            }
                        }
                        return relVal;
                    }
                },
                legend: {
                    selected: {
                        '总里程': true,
                        '速度': true,
                        'ACC': true,
                    },
                    left: 'left',
                    data: [
                        {name: '总里程', icon: 'circle'},
                        {name: '速度', icon: 'circle'},
                        {name: 'ACC', icon: 'circle'}
                    ],
                },
                toolbox: {
                    show: false
                },
                xAxis: {
                    type: 'category',
                    boundaryGap: false,
                    min: vTime[0],
                    max: vTime[vTime.length - 1],
                    data: vTime  //数据日期
                },
                yAxis: [
                    {
                        type: 'value',
                        name: '总里程(km)',
                        position: 'left',
                        scale: true,
                        // minInterval: 1,
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine: {
                            show: false
                        }
                    },
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
                        splitLine: {
                            show: false
                        }
                    },
                    {
                        type: 'value',
                        name: 'ACC',
                        scale: true,
                        min: 0,
                        max: 1,
                        splitNumber: 1,
                        position: 'right',
                        offset: 60,
                        axisLabel: {
                            formatter: function (value) {
                                if (value == 0) {
                                    return '开'
                                } else {
                                    return '关'
                                }
                            },
                        },
                        splitLine: {
                            show: false
                        }
                    }
                ],
                dataZoom: [{
                    type: 'inside'

                }, {
                    start: 0,
                    end: 10,
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
                        name: '总里程',
                        yAxisIndex: 0,
                        type: 'line',
                        smooth: true,
                        symbol: 'image://../../../resources/img/circle.png',
                        symbolSize: 16,
                        showSymbol: false,
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(109, 207, 246)'
                            }
                        },
                        data: mileageData,	//总里程
                        markPoint: {
                            symbolSize: [48, 61],
                            symbolOffset: [0, -32],
                            silent: true,
                            data: [
                                {
                                    yAxis: mile_before,
                                    xAxis: time_before,
                                    symbol: 'image://../../../resources/img/mile_before.png',
                                    label: {
                                        normal: {
                                            show: true,
                                            formatter: "",
                                        }
                                    }
                                },
                                {
                                    yAxis: mile_after,
                                    xAxis: time_after,
                                    symbol: 'image://../../../resources/img/mile_after.png',
                                    label: {
                                        normal: {
                                            show: true,
                                            formatter: "",
                                        }
                                    }
                                },
                            ]
                        }
                    },
                    {
                        name: '速度',
                        yAxisIndex: 1,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(145, 218, 0)'
                            }
                        },
                        data: speedData  //速度
                    },
                    {
                        name: 'ACC',
                        yAxisIndex: 2,
                        type: 'line',
                        smooth: false,
                        symbol: 'none',
                        sampling: 'average',
                        step: true,
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
                        data: accData  //ACC状态
                    }
                ]
            };
            myChart.setOption(option);
            myChart.on('click', mileageDemarcate.symbolEvent);
            myChart.dispatchAction({
                type: 'datazoom',
            });
            //设置里程参数
            mileageDemarcate.setMileParam();
            myChart.on('datazoom', mileageDemarcate.dataZoomBack);
            window.onresize = myChart.resize;
        },
        dataZoomBack: function (params) {
            if (params.batch != undefined) {
                startZoom = params.batch[0].start;
                endZoom = params.batch[0].end;
            } else {
                startZoom = params.start;
                endZoom = params.end;
            }
        },// 页面刷新或关闭时的操作
        pageOnbeforeonload: function () {
            window.onbeforeunload = function () {
                // 更新上一次选择的车辆标定状态
                if (!calibrationFlag) { // 如果在标定中，那么不更新上一个车的标定状态
                    mileageDemarcate.cancelCalibration();
                }
            }
            window.onload = function () {
                // 更新上一次选择的车辆标定状态
                if (!calibrationFlag) { // 如果在标定中，那么不更新上一个车的标定状态
                    mileageDemarcate.cancelCalibrationByVid(window.name);
                    window.name = "";
                }
            }
        },//设置开始里程和结束里程的参数
        setMileParam: function () {
            $("#startMiles").val(mile_before);
            $("#endMiles").val(mile_after);
            if (mile_after != null && mile_before != null && mile_before != '' && mile_after != '') {
                $("#drivenDistance").val(parseFloat((mile_after - mile_before).toFixed(2)));
                if (!calibrationFlag && mile_after - mile_before > 0) {
                    $("#submit_btn").removeAttr("disabled");
                }
            }

        },
        //手动设置开始里程或结束里程
        symbolEvent: function (params) {
            //检查是否可标定
            if (!checkConfigSenorFlag) {
                layer.msg("未绑定里程监测配置,请先配置里程监测");
                return;
            }
            if (!checkAllowBindFlag) {
                layer.msg("已被锁定或标定过,不允许操作");
                return;
            }
            if (min_time_after > params.name) {
                layer.msg(min_time_after + "时间的里程已被标定过，不能重复标定");
                return;
            }
            layer.msg('<label style="cursor:pointer;"><input type="radio" data-state="1" name="mileCheck" />开始里程</label>&ensp;&ensp;<label style="cursor:pointer;"><input data-state="2" name="mileCheck" type="radio" />结束里程</label>', {
                time: 10000, //10s后自动关闭
                btn: ['确定', '取消'],
                yes: function (e) {
                    mileValue = params.data;		//总里程值
                    timeValue = params.name;		//时间
                    //获取选择的checkbox
                    var checkValue = $("input[name='mileCheck']:checked").attr("data-state");

                    //判断选择的是开始还是结束
                    if (checkValue == 1) {
                        if (timeValue >= time_after) {
                            layer.msg("开始里程时间不能大于结束里程时间");
                            return;
                        }

                        if (mileValue >= mile_after) {
                            layer.msg("结束总里程需大于开始总里程");
                            return;
                        }
                    } else if (checkValue == 2) {
                        if (timeValue <= time_before) {
                            layer.msg("开始里程时间不能大于结束里程时间");
                            return;
                        }
                        if (mileValue <= mile_before) {
                            layer.msg("结束总里程需大于开始总里程");
                            return;
                        }
                    } else {
                        layer.closeAll();
                        return;
                    }

                    //判断选择的是开始还是结束
                    if (checkValue == 1) {
                        mile_before = mileValue;
                        time_before = timeValue;
                    } else if (checkValue == 2) {
                        mile_after = mileValue;
                        time_after = timeValue;
                    }
                    layer.closeAll();
                    //刷新图表
                    mileageDemarcate.mileageChart();
                },
                btn2: function () {
                    layer.closeAll();
                }
            });
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
                tMonth = mileageDemarcate.doHandleMonth(tMonth + 1);
                tDate = mileageDemarcate.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " " + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * parseInt(num);
                today.setTime(end_milliseconds); // 注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = mileageDemarcate.doHandleMonth(endMonth + 1);
                endDate = mileageDemarcate.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate + " " + "23:59:59";
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = mileageDemarcate.doHandleMonth(vMonth + 1);
                vDate = mileageDemarcate.doHandleMonth(vDate);
                startTime = vYear + "-" + vMonth + "-" + vDate + " "
                    + "00:00:00";
                if (day == 1) {
                    endTime = vYear + "-" + vMonth + "-" + vDate + " "
                        + "23:59:59";
                } else {
                    var endNum = -1;
                    var vendtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * parseInt(endNum);
                    var dateEnd = new Date();
                    dateEnd.setTime(vendtoday_milliseconds);
                    var vendYear = dateEnd.getFullYear();
                    var vendMonth = dateEnd.getMonth();
                    var vendDate = dateEnd.getDate();
                    vendMonth = mileageDemarcate.doHandleMonth(vendMonth + 1);
                    vendDate = mileageDemarcate.doHandleMonth(vendDate);
                    endTime = vendYear + "-" + vendMonth + "-" + vendDate + " "
                        + "23:59:59";
                }
            }
        },
        doHandleMonth: function (month) {
            var m = month;
            if (month.toString().length == 1) {
                m = "0" + month;
            }
            return m;
        },
        // 当前时间
        getsTheCurrentTime: function () {
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
        },
        //上一天
        upDay: function () {
            mileageDemarcate.startDay(1);
            $('#timeInterval').val(startTime + '--' + endTime);
            mileageDemarcate.inquireClick();
        },
        // 今天
        todayClick: function () {
            mileageDemarcate.getsTheCurrentTime();
            $('#timeInterval').val(startTime + '--' + endTime);
            mileageDemarcate.inquireClick();
        },
        // 前一天
        yesterdayClick: function () {
            mileageDemarcate.startDay(-1);
            $('#timeInterval').val(startTime + '--' + endTime);
            mileageDemarcate.inquireClick();
        },
        cancelCalibration: function () {//取消标定状态
            if (calibrationVehicleId == undefined || calibrationVehicleId == '' || calibrationVehicleId == null)
                return;
            //取消标定状态
            var url = "/clbs/v/meleMonitor/mileageDemarcate/updateCalibrationStatus";
            var data = {"vehicleId": calibrationVehicleId, "calibrationStatus": "0"};
            json_ajax("POST", url, "json", true, data, function (respdata) {
            });
        },
        cancelCalibrationByVid: function (vehicleId) {//取消标定状态
            var url = "/clbs/v/meleMonitor/mileageDemarcate/updateCalibrationStatus";
            var data = {"vehicleId": vehicleId, "calibrationStatus": "0"};
            json_ajax("POST", url, "json", true, data, function (respdata) {
            });
        },
        leftClickVehicleClick: function () {
            layer.confirm("是否放弃这次标定?", {btn: ["确认", "取消"]}, function () {
                //清除标定位置
                mileageDemarcate.cancelCalibration();
                checkAllowQuery = true;
                $(".dropdown-toggle").eq(2).click();
                $(".dropdown-toggle").eq(2).click();
                var trIndex = $(".table-condensed tr").size() - 1;
                var nowIndex = 0;
                $(".table-condensed tr").each(function () {
                    if ($(this).attr("data-id") == $("input[name='charSelect']").attr("data-id")) {
                        nowIndex = $(this).attr("data-index");
                    }
                })
                if (0 == nowIndex) {
                    $("input[name='charSelect']").attr("data-id", $(".table-condensed tr").eq(trIndex).attr("data-id"));
                    $("input[name='charSelect']").val($(".table-condensed tr").eq(trIndex).attr("data-key"));
                } else {
                    $("input[name='charSelect']").attr("data-id", $(".table-condensed tr").eq(nowIndex - 1).attr("data-id"));
                    $("input[name='charSelect']").val($(".table-condensed tr").eq(nowIndex - 1).attr("data-key"));
                }
                $("#inquireClick").click();
            });
        },
        rightClickVehicleClick: function () {
            layer.confirm("是否放弃这次标定?", {btn: ["确认", "取消"]}, function () {
                mileageDemarcate.cancelCalibration();
                checkAllowQuery = true;
                $(".dropdown-toggle").eq(2).click();
                $(".dropdown-toggle").eq(2).click();
                var trIndex = $(".table-condensed tr").size() - 1;
                var nowIndex = 0;
                $(".table-condensed tr").each(function () {
                    if ($(this).attr("data-id") == $("input[name='charSelect']").attr("data-id")) {
                        nowIndex = $(this).attr("data-index");
                    }
                })
                if (trIndex == nowIndex) {
                    $("input[name='charSelect']").attr("data-id", $(".table-condensed tr").eq(0).attr("data-id"));
                    $("input[name='charSelect']").val($(".table-condensed tr").eq(0).attr("data-key"));
                } else {
                    var nextIndex = parseInt(nowIndex) + 1;
                    $("input[name='charSelect']").attr("data-id", $(".table-condensed tr").eq(nextIndex).attr("data-id"));
                    $("input[name='charSelect']").val($(".table-condensed tr").eq(nextIndex).attr("data-key"));
                }
                $("#inquireClick").click();
            });
        },
        endTimeClick: function () {
            var width = $(this).width();
            var offset = $(this).offset();
            var left = offset.left - (207 - width);
            $("#laydate_box").css("left", left + "px");
        },
        //查询
        inquireClick: function () {
            //查询显示图表
            mileageDemarcate.hideErrorMsg();
            var vehicleId = $("input[name='charSelect']").attr("data-id");
            var vehicleNum = $("input[name='charSelect']").val();
            if (vehicleId == null || vehicleId == "" || vehicleNum == null || vehicleNum == "") {
                layer.msg("请选择监控对象！");
                return;
            }
            $("#vehicleId").val(vehicleId);
            var timeInterval = $('#timeInterval').val().split('--');
            var startTime = timeInterval[0];
            if (startTime == "") {
                layer.msg("请选择起始时间");
                return;
            }
            var endTime = timeInterval[1];
            if (endTime == "") {
                layer.msg("请选择结束时间");
                return;
            }
            var lastVehicleId = $("#lastVehicleId").val();
            // 选择车车辆之前，把上一次选择的车辆的标定状态还原
            if (!checkAllowQuery && lastVehicleId != undefined && lastVehicleId != "" && vehicleId != lastVehicleId) {
                layer.confirm("是否放弃这次标定?", {btn: ["确认", "取消"]}, function () {
                    mileageDemarcate.cancelCalibration();
                    layer.closeAll();
                    // 重新选择一个车牌后，清空所有表单
                    mileageDemarcate.initForm();
                    $("#submit_btn").attr("disabled", "disabled");
                    //获取数据
                    var url = "/clbs/v/meleMonitor/mileageDemarcate/getHistoryInfoByVid?s=" + Math.random();
                    json_ajax("POST", url, "json", true, {
                        "vehicleId": vehicleId,
                        "startTime": startTime,
                        "endTime": endTime
                    }, mileageDemarcate.showChart);
                });
            } else {
                mileageDemarcate.cancelCalibration();
                layer.closeAll();
                // 重新选择一个车牌后，清空所有表单
                mileageDemarcate.initForm();
                $("#submit_btn").attr("disabled", "disabled");
                //获取数据
                var url = "/clbs/v/meleMonitor/mileageDemarcate/getHistoryInfoByVid?s=" + Math.random();
                json_ajax("POST", url, "json", true, {
                    "vehicleId": vehicleId,
                    "startTime": startTime,
                    "endTime": endTime
                }, mileageDemarcate.showChart);
            }
        },
        //初始化数据
        initForm: function () {
            $("#startMiles").val("");
            $("#endMiles").val("");
            $("#drivenDistance").val("");
            $("#actualMileage").val("");
            $("#travelTime").html("0小时0分0秒");
            $("#stopTime").html("0小时0分0秒");
            $("#totalMileage").html("0km");
            $("#averageSpeed").html("0km/h");
            $("#submit_btn").attr("disabled", "disabled");
            sendStatus = -1; // -1初始化  0 成功 1失败
        },
        rightArrow: function () {
            mileageDemarcate.startDay(-1);
            $('#timeInterval').val(startTime + '--' + endTime);
            //查询
            $("#inquireClick").click();
        },
        leftArrow: function () {
            var dateValue = new Date().getTime();
            mileageDemarcate.startDay(+1);
            var startTimeValue = new Date(startTime.split(" ")[0].replace(/-/g, "/")).getTime();
            if (startTimeValue > dateValue) {
                layer.msg("暂时没办法穿越，明天我再帮您看吧！");
                return;
            }
            $('#timeInterval').val(startTime + '--' + endTime);
            //查询
            $("#inquireClick").click();
        },
        fixItClick: function () {
            var actualMileage = $("#actualMileage").val();//实际里程(km)
            var drivenDistance = $("#drivenDistance").val();//行驶里程
            var form_endTime = $("#form_endTime").val();//标定时间
            var form_vehicleId = $("#form_vehicleId").val();//标定车辆编号
            if (!onLineStatus) {
                layer.msg("车辆不在线不能操作标定");
                return;
            }
            if (form_vehicleId == null) {
                layer.msg("请选定标定车辆!");
                return;
            }
            if (actualMileage == "" || actualMileage == null) {
                layer.msg("实际里程不能为空");
                return;
            }
            if (drivenDistance == "" || drivenDistance == '0') {
                layer.msg("行驶里程不能为0");
                return;
            }
            if (drivenDistance == '0' || Number(drivenDistance) < 0) {
                layer.msg("行驶里程大于0");
                return;
            }
            if (isNaN(actualMileage)) {
                layer.msg("实际里程只能数字值");
                return;
            }
            if (actualMileage == '0' || Number(actualMileage) < 0) {
                layer.msg("实际里程大于0");
                return;
            }
            $("#submit_btn").attr("disabled", "disabled");
            $.post("/clbs/v/meleMonitor/mileStatistics/sendMileage",
                {
                    "vehicleId": form_vehicleId,
                    "endTime": form_endTime,
                    "travelMail": drivenDistance,
                    "realMail": actualMileage
                },
                function (data) {
                    data = $.parseJSON(data);
                    if (data.success) {
                        // 加载框
                        layer.msg("正在处理...", {time: false});
                        //layer.msg("已修正下发!");
                        if (data != null && data.msg != null) {
                            var msg = $.parseJSON(data.msg);
                            $("#msgSN").val(msg.msgIN8103);
                            var rollingRadius = msg.rollingRadius;
                            var username = msg.username;
                            $("#actualRollingRadius").val(rollingRadius);
                            // 下发参数后，获取终端通用应答消息
                            mileageDemarcate.commonResponse(username, msg.msgIN8103);
                        }
                    } else {
                        layer.msg(data.msg);
                    }
                });
        },
        loading: function () {
            setTimeout(function () {
                layer.msg("正在处理...");
            }, 0);
        },
        showErrorMsg: function (msg, inputId) {
            if ($("#error_label").is(":hidden")) {
                $("#error_label").text(msg);
                $("#error_label").insertAfter($("#" + inputId));
                $("#error_label").show();
            } else {
                $("#error_label").is(":hidden");
            }
        },
        hideErrorMsg: function () {
            $("#error_label").hide();
        },
        getVecherId: function () {//获取当前设置的车辆编号
            return $("#vehicleId").val();
        },
        checkCalibrationStatus: function () {// 判断车辆的标定状态：1-占用状态，正在标定；0-空闲状态，可以标定
            var vid = $("#vehicleId").val();
            var url = "/clbs/v/meleMonitor/mileageDemarcate/checkCalibrationStatus";
            var data = {"vehicleId": vid};
            json_ajax("POST", url, "json", false, data, mileageDemarcate.checkCalibrationStatusCallBack);
        },
        //判断车辆的标定状态回调
        checkCalibrationStatusCallBack: function (data) {
            if (data == null || data.obj == null || data.obj.calibrationStatus == null) {
                return true;
            }
            $("#submit_btn").removeAttr("disabled");
            if (data.obj.calibrationStatus != "" && data.obj.calibrationStatus != "0") { // 标定状态为非空闲状态不可以标定
                // 如果提示车辆在标定中，则判断其标定开始时间是多少，如果超过2个小时，则认为是非正常操作数据，并将其标定状态置为空闲状态
                if (data.obj.updateDataTime == "" || typeof(data.obj.updateDateTime) == undefined)
                    return true;
                var curDate = mileageDemarcate.getNowFormatDate();
                var date1 = new Date(curDate).getTime();
                var date2 = new Date(data.obj.updateDataTime).getTime();
                var timeDiff = date1 - date2;
                if (timeDiff >= 7200000) { // 7200000ms == 2小时
                    mileageDemarcate.cancelCalibrationByVid(mileageDemarcate.getVecherId());
                    mileageDemarcate.checkCalibrationStatus();
                } else {
                    $("#submit_btn").attr("disabled", "disabled");
                    $("#lastVehicleId").val("");
                    layer.msg("当前车辆已有用户在标定，不能重复标定");
                    calibrationFlag = true; // 设置标定状态：标定中
                    window.name = "";
                    return false;
                }
            }
            calibrationVehicleId = mileageDemarcate.getVecherId();
            $("#form_vehicleId").val(mileageDemarcate.getVecherId());
            checkAllowBindFlag = true;
            calibrationFlag = false; // 设置标定状态
            // 记录此值，刷新页面后需要它
            window.name = mileageDemarcate.getVecherId();
            return true;
        },// 下发参数后，终端通用应答信息
        commonResponse: function (username, cmsgSN) {
            var requestStrS = {
                "desc": {
                    "UserName": username,
                    "cmsgSN": cmsgSN
                },
                "data": params
            };
            headers = {"UserName": username};
            webSocket.subscribe(headers, "/user/topic/t808_currency_response", mileageDemarcate.currencyResponse, "/app/vehicle/oil/setting", requestStrS);
            webSocket.subscribe(headers, "/user/topic/deviceReportLog", mileageDemarcate.deviceReportLog, "/app/vehicle/oil/setting", requestStrS);
        },
        // 终端通用应答回调
        //处理获取设备下发的结果
        currencyResponse: function (msg) {
            if (msg == null)
                return;
            var result = $.parseJSON(msg.body);
            var msgid = result.data.msgBody.msgSNACK;
            var status = result.data.msgBody.result;
            if (status == 0) {
                layer.msg("终端处理中", {time: false});
            }
            if (status == 1 || status == 2 || status == 3) {
                $("#submit_btn").removeAttr("disabled");
                layer.closeAll();
                layer.msg("参数下发失败");
            }
            return;
        },
        //处理获取设备下发的结果
        deviceReportLog: function (msg) {
            if (msg == null)
                return;
            var result = $.parseJSON(msg.body);

            var msgid = result.data.msgBody.ackMSN;
            var result = result.data.msgBody.result;
            var msgSN = $("#msgSN").val();
            if (msgSN == msgid && (result == 0 || result == "0")) {

                mileageDemarcate.cancelCalibration();
                var url = "/clbs/v/meleMonitor/mileStatistics/updateMileage";
                var form_endTime = $("#form_endTime").val();//标定时间
                var form_vehicleId = $("#form_vehicleId").val();//标定车辆编号
                var actualRollingRadius = $("#actualRollingRadius").val();//标定车辆编号
                var param = {
                    "vehicleId": form_vehicleId,
                    "endTime": form_endTime,
                    "rollingRadius": actualRollingRadius
                };
                $.post(url, param,
                    function (data) {
                        data = $.parseJSON(data);
                        if (data.success) {
                        } else {
                            layer.msg(data.msg);
                        }
                    });
                layer.closeAll();
                layer.msg("修正成功!");
                clearTimeout(deal_timeout);
                deal_timeout = setTimeout(function () {
                    $("#inquireClick").click();
                }, 2000);
            } else {
                layer.closeAll();
                $("#submit_btn").removeAttr("disabled");
                layer.msg("修正失败，参数未生效");
            }
            return;
        },
        // 摊销标定
        cancleSaveBtnClick: function () {
            layer.confirm("确认取消修正吗?", {btn: ["确定", "取消"]}, function () {
                mileageDemarcate.cancelCalibration();
                $("#submit_btn").attr("disabled", "disabled");
                $("#travelTime").html("0小时0分0秒");
                $("#stopTime").html("0小时0分0秒");
                $("#totalMileage").html("0km");
                $("#averageSpeed").html("0km/h");
                //显示相关内容
                $("#graphShow").css({"display": "none"});
                $("#faChevronDown").attr("class", "fa fa-chevron-up");
                $(".carName,.left-btn,.right-btn,.item-title").css("display", "none");
                $("#carName").text("");

                $("#lastVehicleId").val("");
                // 重新选择一个车牌后，清空所有表单
                $("#actualMileage").val("");
                $("#drivenDistance").val("");
                $("#endMiles").val("");
                $("#vehicleId").val("");
                $("#startMiles").val("");
                layer.closeAll();

                mileageDemarcate.getsTheCurrentTime();
                $('#timeInterval').val(startTime + '--' + endTime);
                $("#charSelect").val('');
                $("#charSelect").attr("data-id","");
            }, function () {
            });
        },
        // 获取当前系统时间：yyyy-MM-dd HH:mm:ss
        getNowFormatDate: function () {
            var date = new Date();
            var seperator1 = "-";
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
        faChevronDownShow: function () {
            if ($("#graphShow").is(":hidden")) {
                $("#graphShow").show();
                $("#faChevronDown").removeClass("fa fa-chevron-up");
                $("#faChevronDown").addClass("fa fa-chevron-down");
            } else {
                $("#graphShow").hide();
                $("#faChevronDown").removeClass("fa fa-chevron-down");
                $("#faChevronDown").addClass("fa fa-chevron-up");
            }
        },
        fiterNumber: function (data) {
            if (data == null || data == undefined || data == "") {
                return 0;
            } else {
                var data = data.toString();
                data = parseFloat(data);
                return data;
            }
        },
        // 时间转换
        getTime: function (time) {
            var date = new Date(time * 1000)
                , y = date.getFullYear()
                , m = (date.getMonth() + 1) >= 10 ? (date.getMonth() + 1) : '0' + (date.getMonth() + 1)
                , d = date.getDate() >= 10 ? date.getDate() : '0' + date.getDate()
                , hh = date.getHours() >= 10 ? date.getHours() : '0' + date.getHours()
                , mm = date.getMinutes() >= 10 ? date.getMinutes() : '0' + date.getMinutes()
                , ss = date.getSeconds() >= 10 ? date.getSeconds() : '0' + date.getSeconds()
            return y + '-' + m + '-' + d + ' ' + hh + ':' + mm + ':' + ss;
        },
    }
    $(function () {
        $('input').inputClear();
        mileageDemarcate.initVehicleInfoList();
        //菜单切换重新绘制
        $("#toggle-left").bind("click", function () {
            setTimeout(function () {
                mileageDemarcate.mileageChart();
            }, 500)
        });
        mileageDemarcate.getsTheCurrentTime();
        $('#timeInterval').dateRangePicker();
        //查询
        $("#inquireClick").on("click", mileageDemarcate.inquireClick);
        $("#todayClick").bind("click", mileageDemarcate.todayClick);
        $("#yesterdayClick,#right-arrow").bind("click", mileageDemarcate.yesterdayClick);
        $("#left-arrow").bind("click", mileageDemarcate.upDay);
        //图表显示
        $("#rightClickVehicle").on("click", mileageDemarcate.rightClickVehicleClick);
        $("#leftClickVehicle").on("click", mileageDemarcate.leftClickVehicleClick);
        $("#rightArrow").on("click", mileageDemarcate.rightArrow);
        $("#leftArrow").on("click", mileageDemarcate.leftArrow);
        //修正下发
        $("#submit_btn").on("click", mileageDemarcate.fixItClick);
        $("#cancle_btn").on("click", mileageDemarcate.cancleSaveBtnClick);
        $("#faChevronDown").on("click", mileageDemarcate.faChevronDownShow);
    })
})(window, $)