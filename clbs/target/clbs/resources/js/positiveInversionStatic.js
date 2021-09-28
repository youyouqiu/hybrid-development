(function ($, window) {
    var startTime;
    var endTime;
    var base = +new Date(2016, 5, 1, 00, 00, 00);
    var date = [];//图形表时间
    var dataSets = [];//table数据
    var nullData = [];
    var mileage = [];//里程
    var speed = [];//速度
    var rotateState = [];//旋转状态
    var rotateSpeed = [];//旋转速度
    //var rotateDirection=[];//旋转方向(字符串数组--顺时针和逆时针)
    var rotateOrientation = [];//旋转方向(01-顺时针  02-逆时针)
    var forwardTime = 0;//正传时长
    var reversalTime = 0;//反转时长
    var rotateTotalTime = 0;//旋转总时长
    var stallTime = 0;//停转时长

    var totalDistance = 0;//总里程
    var orthochronous = [];//不做画图处理的时间数组
    var myChart;
    var option;
    var datapositiveInversion = [];
    var status;//旋转状态

    positveInversion = {
        init: function () {
            myChart = echarts.init(document.getElementById('sjcontainer'));
            option = {
                tooltip: {
                    trigger: 'axis',
                    textStyle: {
                        fontSize: 20
                    },
                    formatter: function (a) {
                        var unit = ['km', 'km/h', '旋转状态'];
                        var relVal = "";
                        relVal = a[0].name;
                        if (a[0].data == null) {
                            relVal = "无相关数据";
                        } else {
                            for (var i = 0; i < a.length; i++) {
                                if (a[i].seriesName == "里程") {
                                    if (a[i].data == null || a[i].data === "") {
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：- km";
                                    } else {
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：" + a[i].data + " km";
                                    }
                                } else if (a[i].seriesName == "速度") {
                                    if (a[i].data == null || a[i].data === "") {
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：- km/h";
                                    } else {
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：" + a[i].data + " km/h";
                                    }

                                } else if (a[i].seriesName == "旋转速度(*方向)") {
                                    if (a[i].data.status == '停转') {
                                        //relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + "旋转速度" + "："+a[i].data+"转/分钟";
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + "旋转状态" + "：停转";
                                    } else if (!a[i].data.status || !a[i].data.orientation) {// 异常
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + "旋转速度" + "：-";
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + "旋转方向" + "：-";
                                    } else {// 运行
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + "旋转速度" + "：" + (!a[i].data.value ? '-' : (Math.abs(Number(a[i].data.value)) + " 转/分钟"));
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + "旋转方向" + "：" + a[i].data.orientation;
                                    }
                                }
                            }
                        }
                        return relVal;
                    }
                },
                legend: {
                    data: ['里程', '速度', '旋转速度(*方向)'],
                    left: 'auto'
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
                        name: '里程(km)',
                        position: 'left',
                        // max: milMax,
                        // min: milMin,
                        // scale: true,
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine: {
                            show: false
                        }
                    },
                    {
                        type: 'value',
                        name: '速度',
                        position: 'right',
                        max: 240,
                        min: 0,
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine: {
                            show: false
                        }
                    },
                    {
                        type: 'value',
                        name: '旋转速度(*方向)',
                        position: 'right',
                        offset: 60,
                        max: 60,
                        min: -60,
                        splitNumber: 1,
                        axisLabel: {
                            formatter: '{value}',
                            formatter: function (value) {
                                console.log('value', value);
                                if (value > 0) {
                                    return '60'
                                } else if (value == 0) {
                                    return '0'
                                } else if (value < 0) {
                                    return '-60'
                                }
                            }
                        },
                        splitLine: {
                            show: false
                        }
                    },
                ],
                dataZoom: [{
                    type: 'inside',
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
                        name: '里程',
                        yAxisIndex: 0,
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
                        data: speed
                    },
                    {
                        name: '旋转速度(*方向)',
                        yAxisIndex: 2,
                        type: 'line',
                        symbol: 'none',
                        sampling: 'average',
                        max: 0,
                        min: -1,
                        smooth: true,
                        itemStyle: {
                            normal: {
                                color: 'rgb(255, 70, 131)'
                            }
                        },
                        areaStyle: {
                            normal: {
                                color: 'rgba(255, 70, 131, 0.8)'
                            }
                        },
                        data: rotateSpeed
                    },
                ]
            };
            myChart.setOption(option);
            window.onresize = myChart.resize;
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
                tMonth = positveInversion.doHandleMonth(tMonth + 1);
                tDate = positveInversion.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " " + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * parseInt(num);
                today.setTime(end_milliseconds); // 注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = positveInversion.doHandleMonth(endMonth + 1);
                endDate = positveInversion.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate + " " + "23:59:59";
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = positveInversion.doHandleMonth(vMonth + 1);
                vDate = positveInversion.doHandleMonth(vDate);
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
                    vendMonth = positveInversion.doHandleMonth(vendMonth + 1);
                    vendDate = positveInversion.doHandleMonth(vendDate);
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
        },
        // ajax请求数据
        ajaxList: function (band, startTime, endTime) {
            positveInversion.removeClass();
            $("#allReport").addClass("active");
            var brandName = $("input[name='charSelect']").val();
            if (brandName.length > 8) {
                $("#carName").attr("title", brandName).tooltip('fixTitle');
                brandName = brandName.substring(0, 7) + '...';
            } else {
                $('#carName').removeAttr('data-original-title');
            }
            $("#carName").text(brandName);
            $(".toopTip-btn-left,.toopTip-btn-right").css("display", "inline-block");
            date = [];
            mileage = [];
            speed = [];
            rotateState = [];
            rotateSpeed = [];
            rotateOrientation = [];
            datapositiveInversion = [];
            $.ajax({
                type: "POST",
                url: "/clbs/v/veerManagement/veerStatistics/getWinchInfo",
                data: {"startTime": startTime, "endTime": endTime, "band": band},
                dataType: "json",
                async: true,
                timeout: 30000, //超时时间设置，单位毫秒
                beforeSend: function () {
                    //异步请求时spinner出现
                    layer.load(2);
                },
                success: function (data) {
                    //positiveInversionList=[];
                    datapositiveInversion = [];
                    forwardTime = 0;//正传时长
                    reversalTime = 0;//反转时长
                    rotateTotalTime = 0;//旋转总时长
                    stallTime = 0;//停转时长
                    totalDistance = 0;//总里程
                    orthochronous = [];//不做画图处理的时间数组
                    layer.closeAll('loading');
                    $('#timeInterval').val(startTime + '--' + endTime);
                    if (data.success == true) {
                        var responseData = JSON.parse(ungzip(data.obj.winchStatisics));
                        data.obj.winchStatisics = responseData;
                        if (data.obj.winchStatisics != null && data.obj.winchStatisics.length != 0) {
                            datapositiveInversion = data.obj.winchStatisics;
                            var rtime = 0;
                            var chenageTimes = 0;
                            var miles;
                            var speeds;
                            for (var i = 0, len = data.obj.winchStatisics.length - 1; i <= len; i++) {
                                if (!(Number(data.obj.winchStatisics[i].vTime == 0))) {
                                    date.push(positveInversion.timeStamp2String(positveInversion.UnixToDate(data.obj.winchStatisics[i].vTime, true)));
                                    orthochronous.push(Number(data.obj.winchStatisics[i].vTime));
                                    miles = data.obj.winchStatisics[i].gpsMile;
                                    speeds = data.obj.winchStatisics[i].speed;
                                    if (miles == undefined || miles == null || miles == "null") {
                                        miles = "";
                                    }
                                    if (speeds == null || speeds == "null") {
                                        speeds = "";
                                    }
                                    mileage.push(miles);
                                    speed.push(speeds);
                                    rotateState.push(data.obj.winchStatisics[i].winchStatus);
                                    var speedObj = {
                                        'value': data.obj.winchStatisics[i].winchSpeed,
                                        'status': data.obj.winchStatisics[i].status,
                                        'orientation': data.obj.winchStatisics[i].orientation
                                    };
                                    rotateSpeed.push(speedObj);
                                    // rotateSpeed.push(data.obj.winchStatisics[i].winchSpeed);
                                    rotateOrientation.push(data.obj.winchStatisics[i].orientation ? data.obj.winchStatisics[i].winchOrientation : 0);
                                }
                                if (i != data.obj.winchStatisics.length - 1) {//如果不是最后一条数据
                                    if (positveInversion.GetDateDiff(positveInversion.UnixToDate(data.obj.winchStatisics[i].vTime, true), positveInversion.UnixToDate(data.obj.winchStatisics[i + 1].vTime, true), "second") <= 300
                                        && positveInversion.GetDateDiff(positveInversion.UnixToDate(data.obj.winchStatisics[i].vTime, true), positveInversion.UnixToDate(data.obj.winchStatisics[i + 1].vTime, true), "second") >= 5) {
                                        changeTime = positveInversion.GetDateDiff(positveInversion.UnixToDate(data.obj.winchStatisics[i].vTime, true), positveInversion.UnixToDate(data.obj.winchStatisics[i + 1].vTime, true), "second");
                                        switch (changeTime) {
                                            case 5:
                                                rtime = 5000;
                                                break;
                                            case 10:
                                                rtime = 10000;
                                                break;
                                            case 15:
                                                rtime = 15000;
                                                break;
                                            case 20:
                                                rtime = 20000;
                                                break;
                                            case 25:
                                                rtime = 25000;
                                                break;
                                            case 30:
                                                rtime = 30000;
                                                break;
                                            case 60:
                                                rtime = 60000;
                                                break;
                                            case 300:
                                                rtime = 300000;
                                                break;
                                            default:
                                                rtime = 0;
                                                break
                                        }
                                    }
                                    if (rtime == 5000 || rtime == 10000 || rtime == 15000 || rtime == 20000 || rtime == 25000 || rtime == 30000 || rtime == 60000 || rtime == 300000) {
                                        switch (rtime) {
                                            case 5000:
                                                chenageTimes = 12;
                                                break;
                                            case 10000:
                                                chenageTimes = 6;
                                                break;
                                            case 15000:
                                                chenageTimes = 4;
                                                break;
                                            case 20000:
                                                chenageTimes = 3;
                                                break;
                                            case 25000:
                                                chenageTimes = 2.4;
                                                break;
                                            case 30000:
                                                chenageTimes = 2;
                                                break;
                                            case 60000:
                                                chenageTimes = 1;
                                                break;
                                            case 300000:
                                                chenageTimes = 1 / 5;
                                                break;
                                        }
                                    }
                                    if (positveInversion.GetDateDiff(positveInversion.UnixToDate(data.obj.winchStatisics[i].vTime, true), positveInversion.UnixToDate(data.obj.winchStatisics[i + 1].vTime, true), "second") > 300) {
                                        nullData += positveInversion.GetDateDiff(positveInversion.UnixToDate(data.obj.winchStatisics[i].vTime, true), positveInversion.UnixToDate(data.obj.winchStatisics[i + 1].vTime, true), "hour");
                                        if (rtime == 0) {
                                            var ctime = +positveInversion.timeAdd(positveInversion.UnixToDate(data.obj.winchStatisics[i].vTime, true));
                                            for (var n = 0; n < positveInversion.GetDateDiff(positveInversion.UnixToDate(data.obj.winchStatisics[i].vTime, true), positveInversion.UnixToDate(data.obj.winchStatisics[i + 1].vTime, true), "minute") * 2; n++) {
                                                date.push(positveInversion.timeStamp2String(new Date(ctime += 30000)));
                                                orthochronous.push(((new Date(ctime += 30000)).getTime()) / 1000);
                                                mileage.push(null);
                                                speed.push(null);
                                                rotateState.push(null);
                                                rotateOrientation.push(null);
                                                rotateSpeed.push({
                                                    'value': null,
                                                    'status': null,
                                                    'orientation': null
                                                });

                                            }
                                        } else {
                                            var ctime = +positveInversion.timeAdd(positveInversion.UnixToDate(data.obj.winchStatisics[i].vTime, true));
                                            for (var n = 0; n < positveInversion.GetDateDiff(positveInversion.UnixToDate(data.obj.winchStatisics[i].vTime, true), positveInversion.UnixToDate(data.obj.winchStatisics[i + 1].vTime, true), "minute") * chenageTimes - 1; n++) {
                                                date.push(positveInversion.timeStamp2String(new Date(ctime += rtime)));
                                                orthochronous.push((new Date(ctime += rtime).getTime()) / 1000);
                                                mileage.push(null);
                                                speed.push(null);
                                                rotateState.push(null);
                                                rotateOrientation.push(null);
                                                rotateSpeed.push({
                                                    'value': null,
                                                    'status': null,
                                                    'orientation': null
                                                });
                                            }
                                        }
                                    }
                                }
                            }
                            if (rotateOrientation != null) {//遍历数组,把为2(反转)的数据改为-1
                                for (var s = 0; s < rotateOrientation.length; s++) {
                                    if (rotateOrientation[s] == 2 && rotateOrientation[s]) {
                                        rotateOrientation[s] = -1;
                                    }
                                }
                            }
                            if (rotateState != null && rotateOrientation != null && rotateState.length == rotateOrientation.length) {//遍历数组,把旋转状态为停转的数据对应的旋转方向数据设置为0
                                for (var h = 0; h < rotateState.length; h++) {
                                    if (rotateState[h] == 1 && rotateOrientation[h]) {
                                        rotateOrientation[h] = 0;
                                    }
                                }
                            }
                            //三个状态   正转(1)、反转(-1)、停转(0)
                            if (rotateOrientation != null && rotateOrientation.length == rotateSpeed.length) {
                                for (var k = 0; k < rotateOrientation.length; k++) {
                                    if (rotateOrientation[k] == -1 && rotateSpeed[k].value) {//如果旋转方向为反转,把旋转速度*-1
                                        rotateSpeed[k].value = Number(rotateSpeed[k].value * rotateOrientation[k]);
                                    } else if (rotateOrientation[k] === 0 && rotateSpeed[k].value) {
                                        rotateSpeed[k].value = 0;//如果旋转方向为0  把旋转速度也设置为0
                                    }
                                }
                            }
                            if (rotateOrientation != null && rotateOrientation.length == orthochronous.length) {//如果旋转方向的数组的长度==时间数组的长度
                                for (var f = 0; f < rotateOrientation.length - 1; f++) {
                                    if (f != rotateOrientation.length) {//如果不是最后一条数据
                                        if (rotateOrientation[f] == 1) {//如果旋转方向为正转
                                            forwardTime += (orthochronous[f + 1] - orthochronous[f]);//旋转时间 = 下一条数据的时间-当前数据的时间

                                        } else if (rotateOrientation[f] == 0) {//停转总时长
                                            stallTime += (orthochronous[f + 1] - orthochronous[f]);
                                        } else if (rotateOrientation[f] == -1) {//反转总时长
                                            reversalTime += (orthochronous[f + 1] - orthochronous[f]);
                                        }
                                    }
                                }
                            }
                            rotateTotalTime = forwardTime + reversalTime;//旋转总时长 = 正转总时长 + 反转总时长
                            totalDistance = mileage[mileage.length - 1] - mileage[0];//行驶里程 = 最后一条数据的行驶里程-第一条的行驶里程
                            if (forwardTime == 0) {
                                $("#forwardTime").text("0");
                            } else {
                                $("#forwardTime").text(positveInversion.timeTransition(forwardTime));//正转总时长
                            }

                            if (reversalTime == 0) {
                                $("#reversalTime").text("0");
                            } else {
                                $("#reversalTime").text(positveInversion.timeTransition(reversalTime));//反转总时长
                            }

                            if (stallTime == 0) {
                                $("#stallTime").text("0");
                            } else {
                                $("#stallTime").text(positveInversion.timeTransition(stallTime));//停转总时长
                            }

                            if (rotateTotalTime == 0) {
                                $("#rotateTotalTime").text("0");
                            } else {
                                $("#rotateTotalTime").text(positveInversion.timeTransition(rotateTotalTime));//旋转总时长
                            }

                            $("#mileage").text(parseFloat(totalDistance.toFixed(1)) + "km");//行驶里程
                            //positveInversion.infoinputTab("/clbs/v/veerManagement/veerStatistics/list");
                        } else {
                            $("#showClick").attr("class", "fa fa-chevron-up");
                            $("#graphShow").hide();
                            $("#forwardTime").text("0");
                            $("#reversalTime").text("0");
                            $("#mileage").text("0km");
                            $("#stallTime").text("0");
                            $("#rotateTotalTime").text("0");
                        }
                    } else if (data.success == false) {
                        layer.msg(systemError)
                        dataSets = [];
                        date = [];
                        mileage = [];
                        speed = [];
                        rotateState = [];
                        rotateOrientation = [];
                        rotateSpeed = [];
                    }
                    $("#graphShow").show();
                    $("#showClick").attr("class", "fa fa-chevron-down");
                    positveInversion.init();
                    positveInversion.infoinputTab("/clbs/v/veerManagement/veerStatistics/list");
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    layer.closeAll('loading');
                    if (textStatus == "timeout") {
                        layer.msg(systemLoadingTimout);
                    }
                },
            });
        },
        //上一天
        upDay: function () {
            positveInversion.startDay(1);
            var charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (charNum != "" && groupValue != "") {
                var dateValue = new Date().getTime();
                var startTimeValue = new Date(startTime.split(" ")[0].replace(/-/g, "/")).getTime();
                if (startTimeValue <= dateValue) {
                    positveInversion.ajaxList(charNum, startTime, endTime);
                    positveInversion.validates()
                } else {
                    layer.msg("暂时没办法穿越，明天我再帮您看吧！");
                }
            } else {
                layer.msg("请选择监控对象！", {move: false});
            }
        },
        validates: function () {
            return $("#positiveInversionForm").validate({
                rules: {
                    startTime: {
                        required: true
                    },
                    endTime: {
                        required: true,
                        compareDate: "#timeInterval",
                    },
                    groupSelect: {
                        zTreeChecked: "treeDemo"
                    },
                    charSelect: {
                        required: true
                    }
                },
                messages: {
                    startTime: {
                        required: "请选择开始日期！",
                    },
                    endTime: {
                        required: "请选择结束时间！",
                        compareDate: endtimeComStarttime,
                    },
                    groupSelect: {
                        zTreeChecked: vehicleSelectBrand,
                    },
                    charSelect: {
                        required: vehicleSelectBrand,
                    }
                }
            }).form();
        },
        // 今天
        todayClick: function () {
            positveInversion.nowDay();
            var charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (!positveInversion.validates()) {
                return;
            }
            if (charNum == '') {
                $("#charSelect-error").html('请至少选择一个监控对象').show();
                return;
            } else {
                $("#charSelect-error").hide();
            }
            positveInversion.ajaxList(charNum, startTime, endTime);
        },
        // 前一天
        yesterdayClick: function () {
            positveInversion.startDay(-1);
            var startValue = $("#startTime")
            var charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (!positveInversion.validates()) {
                return;
            }
            if (charNum == '') {
                $("#charSelect-error").html('请至少选择一个监控对象').show();
                return;
            } else {
                $("#charSelect-error").hide();
            }
            positveInversion.ajaxList(charNum, startTime, endTime);
        },
        // 近三天
        nearlyThreeDays: function () {
            positveInversion.startDay(-3);
            var charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (!positveInversion.validates()) {
                return;
            }
            if (charNum == '') {
                $("#charSelect-error").html('请至少选择一个监控对象').show();
                return;
            } else {
                $("#charSelect-error").hide();
            }
            positveInversion.ajaxList(charNum, startTime, endTime);
        },
        // 近七天
        nearlySevenDays: function () {
            positveInversion.startDay(-7);
            var charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (!positveInversion.validates()) {
                return;
            }
            if (charNum == '') {
                $("#charSelect-error").html('请至少选择一个监控对象').show();
                return;
            } else {
                $("#charSelect-error").hide();
            }
            positveInversion.ajaxList(charNum, startTime, endTime);
        },
        // 查询
        inquireClick: function () {
            var groupValue = $("#groupSelect").val();
            var timeInterval = $('#timeInterval').val().split('--');
            startTime = timeInterval[0];
            endTime = timeInterval[1];
            var charNum = $("#charSelect").attr("data-id");
            if (!positveInversion.validates()) {
                return;
            }
            if (charNum == '') {
                $("#charSelect-error").html('请至少选择一个监控对象').show();
                return;
            } else {
                $("#charSelect-error").hide();
            }
            positveInversion.ajaxList(charNum, startTime, endTime);
        },
        // 勾选数据
        //时间戳转换日期
        UnixToDate: function (unixTime, isFull, timeZone) {
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
        showClick: function () {
            if ($(this).hasClass("fa-chevron-up")) {
                $(this).attr("class", "fa fa-chevron-down");
                $("#graphShow").show();
            } else {
                $(this).attr("class", "fa fa-chevron-up");
                $("#graphShow").hide('300');
            }
        },
        draggle: function () {
            $("#showClick").attr("class", "fa fa-chevron-down");
            $("#graphShow").show();
        },
        //创建表格
        infoinputTab: function (url) {
            var columnDefs = [{
                //第一列，用来显示序号
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];
            var columns = [{
                //第一列，用来显示序号
                "data": null,
                "class": "text-center"
            }, {
                "data": "monitorName",
                "class": "text-center"
            }, {
                "data": "vtime",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return positveInversion.timeStamp2String(positveInversion.UnixToDate(row.vTime, true));
                }
            }, {
                "data": "status",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == "" || data == null || data == undefined) {
                        return "-";
                    } else {
                        return data;
                    }

                }
            }, {
                "data": "winchSpeed",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == "" || data == null || data == undefined) {
                        return "-";
                    } else {
                        return data;
                    }

                }
            }, {
                "data": "orientation",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == "" || data == null || data == undefined) {
                        return "-";
                    } else {
                        return data;
                    }

                }
            }, {
                "data": "winchRotateTime",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == "" || data == null || data == undefined) {
                        return "-";
                    } else {
                        return data;
                    }

                }
            }, {
                "data": "winchTime",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == "" || data == null || data == undefined) {
                        return "-";
                    } else {
                        return data;
                    }

                }
            }, {
                "data": "winchCounter",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == "" || data == null || data == undefined) {
                        return "-";
                    } else {
                        return data;
                    }

                }
            }, {
                "data": "gpsMile",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == "" || data == null || data == undefined) {
                        return "-";
                    } else {
                        return parseFloat(Number(data).toFixed(1));
                    }

                }
            }, {
                "data": "speed",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == "" || data == null || data == undefined) {
                        return "-";
                    } else {
                        return parseFloat(Number(data).toFixed(2));
                    }

                }
            }, {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    //return data.longtitude+","+data.latitude
                    return "加载中...";
                }
            }];
            var ajaxDataParamFun = function (d) {
                d.band = $("#charSelect").attr("data-id"); //模糊查询
                var timeInterval = $('#timeInterval').val().split('--');
                startTime = timeInterval[0];
                endTime = timeInterval[1];
                d.startTime = startTime;
                d.endTime = endTime;
            };
            var getdata = function (data, type, row, meta) {
            }
            //表格setting
            var setting = {
                listUrl: url,
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                getAddress: true,//是否逆地理编码
                address_index: 12,
                //drawCallbackFun:getdata,
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
        },
        toHHMMSS: function (data) {
            var totalSeconds = data * 60 * 60;
            var hour = Math.floor(totalSeconds / 60 / 60);
            var minute = Math.floor(totalSeconds / 60 % 60);
            var second = Math.floor(totalSeconds % 60);
            return hour + "小时" + minute + "分" + second + "秒"
        },
        removeClass: function () {
            var dataList = $(".dataTableShow");
            for (var i = 0; i < 3; i++) {
                dataList.children("li").removeClass("active");
            }
        },
        allReportClick: function () {
            positveInversion.removeClass();
            $(this).addClass("active");
            if (date.length != 0) {
                positveInversion.infoinputTab("/clbs/v/veerManagement/veerStatistics/list");
            }
        },
        timeStamp2String: function (time) {
            var time = time.toString();
            var startTimeIndex = time.replace("-", "/").replace("-", "/");
            var val = Date.parse(startTimeIndex);
            var datetime = new Date();
            datetime.setTime(val);
            var year = datetime.getFullYear();
            var month = datetime.getMonth() + 1 < 10 ? "0" + (datetime.getMonth() + 1) : datetime.getMonth() + 1;
            var date = datetime.getDate() < 10 ? "0" + datetime.getDate() : datetime.getDate();
            var hour = datetime.getHours() < 10 ? "0" + datetime.getHours() : datetime.getHours();
            var minute = datetime.getMinutes() < 10 ? "0" + datetime.getMinutes() : datetime.getMinutes();
            var second = datetime.getSeconds() < 10 ? "0" + datetime.getSeconds() : datetime.getSeconds();
            return year + "-" + month + "-" + date + " " + hour + ":" + minute + ":" + second;
        },
        timeAdd: function (time) {
            var str = time.toString();
            str = str.replace(/-/g, "/");
            return new Date(str);
        },
        GetDateDiff: function (startTime, endTime, diffType) {
            // 将xxxx-xx-xx的时间格式，转换为 xxxx/xx/xx的格式
            startTime = startTime.replace(/-/g, "/");
            endTime = endTime.replace(/-/g, "/");
            // 将计算间隔类性字符转换为小写
            diffType = diffType.toLowerCase();
            var sTime = new Date(startTime); // 开始时间
            var eTime = new Date(endTime); // 结束时间
            // 作为除数的数字
            var divNum = 1;
            switch (diffType) {
                case "second":
                    divNum = 1000;
                    break;
                case "minute":
                    divNum = 1000 * 60;
                    break;
                case "hour":
                    divNum = 1000 * 3600;
                    break;
                case "day":
                    divNum = 1000 * 3600 * 24;
                    break;
                default:
                    break;
            }
            return parseFloat((eTime.getTime() - sTime.getTime()) / parseInt(divNum)); //
        },
        //过滤数组空值
        filterTheNull: function (value) {
            for (var i = 0; i < value.length; i++) {
                if (value[i] != 0) {
                    if (value[i] == null || value[i] == "" || typeof(value[i]) == "undefined") {
                        value.splice(i, 1);
                        i = i - 1;
                    }
                }
            }
            return value
        },
        nullValueJudge: function (arr) {//将数组中为null的数据替换为-
            if (arr != null && arr.length != 0) {
                for (var indexs = 0; indexs < arr.length; indexs++) {
                    if (arr[indexs] == null || arr[indexs] == undefined) {
                        arr[indexs] = "-";
                    }
                    if (indexs == 2) {
                        var time = arr[indexs];
                        var times = positveInversion.timeStamp2String(positveInversion.UnixToDate(time, true));
                        arr[indexs] = times;
                    }
                }
            }
        },
        draggle: function () {
            $("#showClick").attr("class", "fa fa-chevron-down");
            $("#graphShow").show();
        },
        timeTransition: function (value) { //将秒转换成时分秒
            var theTime = parseInt(value);// 秒
            var theTime1 = 0;// 分
            var theTime2 = 0;// 小时

            if (theTime > 60) {
                theTime1 = parseInt(theTime / 60);
                theTime = parseInt(theTime % 60);
                if (theTime1 > 60) {
                    theTime2 = parseInt(theTime1 / 60);
                    theTime1 = parseInt(theTime1 % 60);
                }
            }
            var result = "" + parseInt(theTime) + "秒";
            if (theTime1 > 0) {
                result = "" + parseInt(theTime1) + "分" + result;
            }
            if (theTime2 > 0) {
                result = "" + parseInt(theTime2) + "小时" + result;
            }
            return result;
        },
        frontReportinfo: function () {
            $("#frontReport").addClass("active").siblings().removeClass("active");
            url = "/clbs/v/veerManagement/veerStatistics/positiveList";
            //alert("正转数据显示");
            positveInversion.infoinputTab(url);
            //点击显示标注
            $('#dataTable tbody').on('click', 'tr', function () {
                $(this).siblings(".odd").find("td").css('background-color', "#f9f9f9");
                $(this).siblings(".even").find("td").css('background-color', "#fff");
                var backgroundcolor = $(this).find("td").css('background-color');
                if (backgroundcolor == "rgb(220, 245, 255)") {
                    $("#dataTable tbody tr").siblings(".odd").find("td").css('background-color', "#f9f9f9");
                    $("#dataTable tbody tr").siblings(".even").find("td").css('background-color', "#fff");
                    /*delete option.series[2].markPoint;
                    delete option.series[0].markPoint;
                    delete option.series[1].markPoint;
                    myChart.clear();*/
                } else {
                    $(this).find("td").css('background-color', "#DCF5FF");
                    var datainfo = $(this).find("td").eq("2").text();
                    var rotationspeed = $(this).find("td").eq("4").text();
                }
                myChart.setOption(option);
            });
        },
        oppositeReportinfo: function () {
            $("#oppositeReport").addClass("active").siblings().removeClass("active");
            url = "/clbs/v/veerManagement/veerStatistics/inversionList";
            //alert("反转数据显示");
            positveInversion.infoinputTab(url);
            //点击显示标注
            $('#dataTable tbody').on('click', 'tr', function () {
                $(this).siblings(".odd").find("td").css('background-color', "#f9f9f9");
                $(this).siblings(".even").find("td").css('background-color', "#fff");
                var backgroundcolor = $(this).find("td").css('background-color');
                if (backgroundcolor == "rgb(220, 245, 255)") {
                    $("#dataTable tbody tr").siblings(".odd").find("td").css('background-color', "#f9f9f9");
                    $("#dataTable tbody tr").siblings(".even").find("td").css('background-color', "#fff");
                    //delete  option.series[2].markPoint;
                    var datainfo = $(this).find("td").eq("2").text();
                    var rotationspeed = $(this).find("td").eq("4").text();
                } else {
                    $(this).find("td").css('background-color', "#DCF5FF");
                    var datainfo = $(this).find("td").eq("2").text();
                    var rotationspeed = $(this).find("td").eq("4").text();
                    var allmils = $(this).find("td").eq("9").text();
                    var speed = $(this).find("td").eq("10").text();
                }


            });
        }
        // /**
        //   * 时间戳转换日期
        //   * @param <int> unixTime    待时间戳(秒)
        //   * @param <bool> isFull    返回完整时间(Y-m-d 或者 Y-m-d H:i:s)
        //   * @param <int>  timeZone   时区
        //   */
        // callbackUnixToDate: function(unixTime, isFull, timeZone) {
        //     if(unixTime==0 || unixTime==null){
        //         return "";
        //     }
        //     if (typeof (timeZone) == 'number') {
        //         unixTime = parseInt(unixTime) + parseInt(timeZone) * 60 * 60;
        //     }
        //     var time = new Date(unixTime * 1000);
        //     var ymdhis = "";
        //     ymdhis += time.getFullYear() + "-";
        //     ymdhis += (time.getMonth() + 1)<10? ("0"+(time.getMonth() + 1)+ "-"):((time.getMonth() + 1)+ "-");
        //     ymdhis += time.getDate()<10?("0"+time.getDate()):(time.getDate());;
        //     if (isFull === true) {
        //         ymdhis += " " + (time.getHours()<10?("0"+time.getHours()):time.getHours()) + ":";
        //         ymdhis += (time.getMinutes()<10?("0"+time.getMinutes()):time.getMinutes()) + ":";
        //         ymdhis += (time.getSeconds()<10?("0"+time.getSeconds()):time.getSeconds());
        //     }
        //     return ymdhis;
        // },
    },
        $(function () {
            $('input').inputClear();
            $("#toggle-left").bind("click", function () {
                setTimeout(function () {
                    positveInversion.init();
                }, 500)
            });
            Array.prototype.isHas = function (a) {
                if (this.length === 0) {
                    return false
                }
                ;
                for (var i = 0; i < this.length; i++) {
                    if (this[i].seriesName === a) {
                        return true
                    }
                }
            };
            positveInversion.nowDay();
            $('#timeInterval').dateRangePicker({
                dateLimit: 7
            });
            $("#todayClick").bind("click", positveInversion.todayClick);
            $("#yesterdayClick,#right-arrow").bind("click", positveInversion.yesterdayClick);
            $("#nearlyThreeDays").bind("click", positveInversion.nearlyThreeDays);
            $("#nearlySevenDays").bind("click", positveInversion.nearlySevenDays);
            $("#inquireClick").bind("click", positveInversion.inquireClick);
            $("#showClick").bind("click", positveInversion.showClick);
            $("#left-arrow").bind("click", positveInversion.upDay);
            $("#allReport").bind("click", positveInversion.inquireClick);
            $("#frontReport").bind("click", positveInversion.frontReportinfo);
            $("#oppositeReport").bind("click", positveInversion.oppositeReportinfo);
            $("#stretch").unbind();
        });
})($, window);