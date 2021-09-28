(function ($, window) {
    var startTime;
    var endTime;
    var base = +new Date(2016, 5, 1, 00, 00, 00);
    var date = [];//图形表时间
    var nullData = [];
    var mileage = [];//里程
    var speed = [];//速度
    var tmp = [];//温度
    var tempOne = [];//一号温度传感器温度
    var tempTwo = [];//二号温度传感器温度
    var tempThree = [];//三号温度传感器温度
    var tempFour = [];//四号温度传感器温度
    var tempFive = [];//五号温度传感器温度
    var myChart;
    var option;
    var tempList = [];
    var dataTemp = [];
    var addressMsg = [];
    var option = {};
    var series = [];
    var ifAllCheck = true; //刚进入页面小于5000自动勾选
    var crrentSubV = []; //模糊查询？
    var zTreeIdJson = {};
    var checkFlag = false;
    var bflag = true; //模糊查询
    var isSearch = true;
    var size;//当前权限监控对象数量
    var curBrand;
    // 车辆list
    var vehicleList = JSON.parse($("#vehicleList").attr("value"));
    tempstatis = {
        init: function () {
            myChart = echarts.init(document.getElementById('sjcontainer'));
            option = {
                tooltip: {
                    trigger: 'axis',
                    textStyle: {
                        fontSize: 20
                    },
                    formatter: function (a) {
                        var unit = ['Km', 'Km/h', '°C'];
                        var relVal = "";
                        relVal = a[0].name;
                        if (a[0].data == null) {
                            relVal = "无相关数据";
                        } else {
                            for (var i = 0; i < a.length; i++) {
                                if (a[i].seriesName == "里程") {
                                    if (a[i].data === "") {
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：- km";
                                    } else {
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：" + a[i].data + "km";
                                    }
                                } else if (a[i].seriesName == "速度") {
                                    if (a[i].data === "") {
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：- km/h";
                                    } else {
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：" + a[i].data + " km/h";
                                    }
                                } else if (a[i].seriesName == "温度传感器温度1") {
                                    if (a[i].data == null) {
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：- ℃";
                                    } else {
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：" + a[i].data + " ℃";
                                    }
                                } else if (a[i].seriesName == "温度传感器温度2") {
                                    if (a[i].data == null) {
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：- ℃";
                                    } else {
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：" + a[i].data + " ℃";
                                    }
                                } else if (a[i].seriesName == "温度传感器温度3") {
                                    if (a[i].data == null) {
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：- ℃";
                                    } else {
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：" + a[i].data + " ℃";
                                    }
                                } else if (a[i].seriesName == "温度传感器温度4") {
                                    if (a[i].data == null) {
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：- ℃";
                                    } else {
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：" + a[i].data + "℃";
                                    }
                                } else if (a[i].seriesName == "温度传感器温度5") {
                                    if (a[i].data == null) {
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：- ℃";
                                    } else {
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：" + a[i].data + "℃";
                                    }
                                }
                            }
                        }
                        return relVal;
                    }
                },
                legend: {
                    data: [{name: '里程'},
                        {name: '速度'},
                        {name: '温度传感器温度1'},
                        {name: '温度传感器温度2'},
                        {name: '温度传感器温度3'},
                        {name: '温度传感器温度4'},
                        {name: '温度传感器温度5'}],
                    left: 'auto'
                },
                grid: {
                    left: 100,
                    right: 170,
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
                        splitLine: {
                            show: false
                        }
                    },
                    {
                        type: 'value',
                        name: '温度(°C)',
                        scale: true,
                        position: 'right',
                        offset: 60,
                        min: -55,
                        max: 125,
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine: {
                            show: false
                        }
                    },
                    {
                        type: 'value',
                        name: '里程(km)',
                        position: 'left',
                        precision: 1,
                        scale: true,
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine: {
                            show: false
                        }
                    }
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
                        yAxisIndex: 2,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(109, 207, 246)'
                            }
                        },
                        data: mileage,
                        /*markPoint: {
                            data: [
                                {name: '周最低', value: 66, xAxis:date[0], yAxis: 66.3}
                            ]
                        },*/
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
                        name: '温度传感器温度1',
                        yAxisIndex: 1,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        symbolSize: 15,
                        showSymbol: false,
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: '#f4b5bd'
                            }
                        },
                        data: tempOne
                    },
                    {
                        name: '温度传感器温度2',
                        yAxisIndex: 1,
                        type: 'line',
                        smooth: false,
                        symbol: 'none',
                        symbolSize: 15,
                        showSymbol: false,
                        itemStyle: {
                            normal: {
                                color: '#e47c8c'
                            }
                        },
                        data: tempTwo
                    },
                    {
                        name: '温度传感器温度3',
                        yAxisIndex: 1,
                        type: 'line',
                        smooth: false,
                        symbol: 'none',
                        symbolSize: 15,
                        showSymbol: false,
                        itemStyle: {
                            normal: {
                                color: '#e35052'
                            }
                        },
                        data: tempThree
                    },
                    {
                        name: '温度传感器温度4',
                        yAxisIndex: 1,
                        type: 'line',
                        smooth: false,
                        symbol: 'none',
                        symbolSize: 15,
                        showSymbol: false,
                        itemStyle: {
                            normal: {
                                color: '#f33168'
                            }
                        },
                        data: tempFour
                    },
                    {
                        name: '温度传感器温度5',
                        yAxisIndex: 1,
                        type: 'line',
                        smooth: false,
                        symbol: 'none',
                        symbolSize: 15,
                        showSymbol: false,
                        itemStyle: {
                            normal: {
                                color: '#a40c0d'
                            }
                        },
                        data: tempFive
                    },
                ]
            };
            /* option.series[0].markPoint =  {
                      data : [
                           {name: '周最低', value: 66, xAxis:date[0], yAxis: 88}
                       ]
                   };     */
            myChart.setOption(option);
            myChart.on('click', function (params) {
            });
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
                tMonth = tempstatis.doHandleMonth(tMonth + 1);
                tDate = tempstatis.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " " + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * parseInt(num);
                today.setTime(end_milliseconds); // 注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = tempstatis.doHandleMonth(endMonth + 1);
                endDate = tempstatis.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate + " " + "23:59:59";
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = tempstatis.doHandleMonth(vMonth + 1);
                vDate = tempstatis.doHandleMonth(vDate);
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
                    vendMonth = tempstatis.doHandleMonth(vendMonth + 1);
                    vendDate = tempstatis.doHandleMonth(vendDate);
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
            curBrand = band;
            tempstatis.removeClass();
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
            mileage = [];
            speed = [];
            date = [];
            tempOne = [];
            tempTwo = [];
            tempThree = [];
            tempFour = [];
            tempFive = [];
            tempList = [];
            dataTemp = [];
            $.ajax({
                type: "POST",
                url: "/clbs/v/temperatureDetection/temperatureStatistics/statisics",
                data: {"startTime": startTime, "endTime": endTime, "band": band},
                dataType: "json",
                async: true,
                timeout: 10000, //超时时间设置，单位毫秒
                beforeSend: function () {
                    //异步请求时spinner出现
                    layer.load(2);
                },
                success: function (data) {
                    layer.closeAll('loading');
                    $('#timeInterval').val(startTime + '--' + endTime);
                    $('#exportBtn').prop('disabled', true);
                    if (data.success == true) {
                        var responseData = JSON.parse(ungzip(data.obj.tempStatisics));
                        data.obj.tempStatisics = responseData;
                        if (data.obj.tempStatisics != null && data.obj.tempStatisics.length != 0) {
                            $('#exportBtn').prop('disabled', false);
                            var addressMsg = [];
                            var msgIndex = 0;
                            dataTemp = data.obj.tempStatisics;
                            var rtime = 0;
                            var chenageTimes = 0;
                            var miles;
                            var speeds;
                            for (var i = 0, len = data.obj.tempStatisics.length - 1; i <= len; i++) {
                                if (!(Number(data.obj.tempStatisics[i].speed) == 0 && Number(data.obj.tempStatisics[i].locationTime) == 0)) {
                                    date.push(tempstatis.timeStamp2String(tempstatis.UnixToDate(data.obj.tempStatisics[i].locationTime, true)));
                                    miles = data.obj.tempStatisics[i].gpsMile;
                                    speeds = data.obj.tempStatisics[i].speed;
                                    if (miles == undefined || miles == null || miles == "null") {
                                        miles = "";
                                    }
                                    if (speeds == null || speeds == "null") {
                                        speeds = "";
                                    }
                                    mileage.push(miles);
                                    speed.push(speeds);
                                    tempOne.push((data.obj.tempStatisics[i].tempValueOne));
                                    tempTwo.push((data.obj.tempStatisics[i].tempValueTwo));
                                    tempThree.push((data.obj.tempStatisics[i].tempValueThree));
                                    tempFour.push((data.obj.tempStatisics[i].tempValueFour));
                                    tempFive.push((data.obj.tempStatisics[i].tempValueFive));
                                }
                                if (i != data.obj.tempStatisics.length - 1) {
                                    if (tempstatis.GetDateDiff(tempstatis.UnixToDate(data.obj.tempStatisics[i].locationTime, true), tempstatis.UnixToDate(data.obj.tempStatisics[i + 1].locationTime, true), "second") <= 300
                                        && tempstatis.GetDateDiff(tempstatis.UnixToDate(data.obj.tempStatisics[i].locationTime, true), tempstatis.UnixToDate(data.obj.tempStatisics[i + 1].locationTime, true), "second") >= 5) {
                                        changeTime = tempstatis.GetDateDiff(tempstatis.UnixToDate(data.obj.tempStatisics[i].locationTime, true), tempstatis.UnixToDate(data.obj.tempStatisics[i + 1].locationTime, true), "second");
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
                                    if (tempstatis.GetDateDiff(tempstatis.UnixToDate(data.obj.tempStatisics[i].locationTime, true), tempstatis.UnixToDate(data.obj.tempStatisics[i + 1].locationTime, true), "second") > 300) {
                                        nullData += tempstatis.GetDateDiff(tempstatis.UnixToDate(data.obj.tempStatisics[i].locationTime, true), tempstatis.UnixToDate(data.obj.tempStatisics[i + 1].locationTime, true), "hour");
                                        if (rtime == 0) {
                                            var ctime = +tempstatis.timeAdd(tempstatis.UnixToDate(data.obj.tempStatisics[i].locationTime, true));
                                            for (var n = 0; n < tempstatis.GetDateDiff(tempstatis.UnixToDate(data.obj.tempStatisics[i].locationTime, true), tempstatis.UnixToDate(data.obj.tempStatisics[i + 1].locationTime, true), "minute") * 2; n++) {
                                                date.push(tempstatis.timeStamp2String(new Date(ctime += 30000)));
                                                mileage.push(null);
                                                speed.push(null);
                                                tempOne.push(null);
                                                tempTwo.push(null);
                                                tempThree.push(null);
                                                tempFour.push(null);
                                                tempFive.push(null);

                                            }
                                        } else {
                                            var ctime = +tempstatis.timeAdd(tempstatis.UnixToDate(data.obj.tempStatisics[i].locationTime, true));
                                            for (var n = 0; n < tempstatis.GetDateDiff(tempstatis.UnixToDate(data.obj.tempStatisics[i].locationTime, true), tempstatis.UnixToDate(data.obj.tempStatisics[i + 1].locationTime, true), "minute") * chenageTimes - 1; n++) {
                                                date.push(tempstatis.timeStamp2String(new Date(ctime += rtime)));
                                                mileage.push(null);
                                                speed.push(null);
                                                tempOne.push(null);
                                                tempTwo.push(null);
                                                tempThree.push(null);
                                                tempFour.push(null);
                                                tempFive.push(null);
                                            }
                                        }
                                    }
                                }
                            }
                            //tempstatis.infoinputTab("/clbs/v/temperatureDetection/temperatureStatistics/list");
                        }
                    } else if (data.success == false) {
                        layer.msg(data.msg, {move: false});
                        mileage = [];
                        speed = [];
                        tempOne = [];
                        tempTwo = [];
                        tempThree = [];
                        tempFour = [];
                        tempFive = [];
                    }
                    $("#graphShow").show();
                    $("#showClick").attr("class", "fa fa-chevron-down");
                    tempstatis.init();
                    tempstatis.infoinputTab("/clbs/v/temperatureDetection/temperatureStatistics/list", 0);
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    layer.closeAll('loading');
                    if (textStatus == "timeout") {
                        layer.msg("加载超时，请重试");
                    }
                },

            });
        },
        //上一天
        upDay: function () {
            tempstatis.startDay(1);
            var charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (charNum != "" && groupValue != "") {
                var dateValue = new Date().getTime();
                var startTimeValue = new Date(startTime.split(" ")[0].replace(/-/g, "/")).getTime();
                if (startTimeValue <= dateValue) {
                    tempstatis.ajaxList(charNum, startTime, endTime);
                    tempstatis.validates()
                } else {
                    layer.msg("暂时没办法穿越，明天我再帮您看吧！");
                }
            } else {
                layer.msg("请选择监控对象！", {move: false});
            }
        },
        validates: function () {
            return $("#tempForm").validate({
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
            tempstatis.nowDay();
            var charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (!tempstatis.validates()) {
                return;
            }
            if (charNum == '') {
                $("#charSelect-error").html('请至少选择一个监控对象').show();
                return;
            } 
                $("#charSelect-error").hide();
            
            tempstatis.ajaxList(charNum, startTime, endTime);
        },
        // 前一天
        yesterdayClick: function () {
            tempstatis.startDay(-1);
            var startValue = $("#startTime")
            var charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (!tempstatis.validates()) {
                return;
            }
            if (charNum == '') {
                $("#charSelect-error").html('请至少选择一个监控对象').show();
                return;
            } 
                $("#charSelect-error").hide();
            
            tempstatis.ajaxList(charNum, startTime, endTime);
        },
        // 近三天
        nearlyThreeDays: function () {
            tempstatis.startDay(-3);
            var charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (!tempstatis.validates()) {
                return;
            }
            if (charNum == '') {
                $("#charSelect-error").html('请至少选择一个监控对象').show();
                return;
            } 
                $("#charSelect-error").hide();
            
            tempstatis.ajaxList(charNum, startTime, endTime);
        },
        // 近七天
        nearlySevenDays: function () {
            tempstatis.startDay(-7);
            var charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (!tempstatis.validates()) {
                return;
            }
            if (charNum == '') {
                $("#charSelect-error").html('请至少选择一个监控对象').show();
                return;
            } 
                $("#charSelect-error").hide();
            
            tempstatis.ajaxList(charNum, startTime, endTime);
        },
        // 查询
        inquireClick: function () {
            var groupValue = $("#groupSelect").val();
            var timeInterval = $('#timeInterval').val().split('--');
            startTime = timeInterval[0];
            endTime = timeInterval[1];
            var charNum = $("#charSelect").attr("data-id");
            if (!tempstatis.validates()) {
                return;
            }
            if (charNum == '') {
                $("#charSelect-error").html('请至少选择一个监控对象').show();
                return;
            } 
                $("#charSelect-error").hide();
            
            tempstatis.ajaxList(charNum, startTime, endTime);
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
        infoinputTab: function (url, state) {
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
                "data": "plateNumber",
                "class": "text-center"
            }, {
                "data": "locationTime",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return tempstatis.timeStamp2String(tempstatis.UnixToDate(data, true));
                }
            }, {
                "data": "gpsMile",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == 'null' || data == null) {
                        return '-';
                    } 
                        return data;
                    
                }
            }, {
                "data": "speed",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == 'null' || data == null) {
                        return '-';
                    } 
                        return data;
                    
                }
            }, {
                "data": "tempValueOne",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (state == 0) {
                        if (data === "" || data == null || data == undefined) {
                            return "-";
                        } 
                            return data;
                        
                    } else if (state == 1) {
                        if (row.tempHighLowOne == 1) {
                            return data;
                        } 
                            return "-";
                        
                    } else if (state == 2) {
                        if (row.tempHighLowOne == 2) {
                            return data;
                        } 
                            return "-";
                        
                    }

                }
            }, {
                "data": "tempValueTwo",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (state == 0) {
                        if (data === "" || data == null || data == undefined) {
                            return "-";
                        } 
                            return data;
                        
                    } else if (state == 1) {
                        if (row.tempHighLowTwo == 1) {
                            return data;
                        } 
                            return "-";
                        
                    } else if (state == 2) {
                        if (row.tempHighLowTwo == 2) {
                            return data;
                        } 
                            return "-";
                        
                    }
                }
            }, {
                "data": "tempValueThree",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (state == 0) {
                        if (data === "" || data == null || data == undefined) {
                            return "-";
                        } 
                            return data;
                        
                    } else if (state == 1) {
                        if (row.tempHighLowThree == 1) {
                            return data;
                        } 
                            return "-";
                        
                    } else if (state == 2) {
                        if (row.tempHighLowThree == 2) {
                            return data;
                        } 
                            return "-";
                        
                    }
                }
            }, {
                "data": "tempValueFour",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (state == 0) {
                        if (data === "" || data == null || data == undefined) {
                            return "-";
                        } 
                            return data;
                        
                    } else if (state == 1) {
                        if (row.tempHighLowFour == 1) {
                            return data;
                        } 
                            return "-";
                        
                    } else if (state == 2) {
                        if (row.tempHighLowFour == 2) {
                            return data;
                        } 
                            return "-";
                        
                    }
                }
            }, {
                "data": "tempValueFive",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (state == 0) {
                        if (data === "" || data == null || data == undefined) {
                            return "-";
                        } 
                            return data;
                        
                    } else if (state == 1) {
                        if (row.tempHighLowFive == 1) {
                            return data;
                        } 
                            return "-";
                        
                    } else if (state == 2) {
                        if (row.tempHighLowFive == 2) {
                            return data;
                        } 
                            return "-";
                        
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
                address_index: 11
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();

            $('#exportBtn').prop('disabled', false);
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
            tempstatis.removeClass();
            $(this).addClass("active");
            if (date.length != 0) {
                tempstatis.infoinputTab(url_oil, 0);
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
            return year + "-" + month + "-" + date + " " + hour + ":" + minute + ":" + second;
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
        /*validates: function(){
            return $("#tempForm").validate({
                rules: {
                    groupId: {
                        required: true
                    },
                    endTime: {
                        required: true,
                        compareDate: "#timeInterval",
                        compareDateDiff: "#timeInterval"
                    },
                    startTime: {
                        required: true
                    },
                    charSelect:{
                        required: true
                    }
                },
                messages: {
                    groupId: {
                        required: "不能为空"
                    },
                    endTime: {
                        required: "不能为空",
                        compareDate: "结束日期必须大于开始日期!",
                        compareDateDiff: "查询的日期必须小于一周"
                    },
                    startTime: {
                        required: "不能为空",
                    },
                    charSelect:{
                        required: "不能为空"
                    }
                }
            }).form();
        },*/
        nullValueJudge: function (arr) {//将数组中为null的数据替换为-
            if (arr != null && arr.length != 0) {
                for (var i = 0; i < arr.length - 1; i++) {
                    if (i == 2) {
                        var time = arr[i];
                        var times = tempstatis.timeStamp2String(tempstatis.UnixToDate(time, true));
                        arr[i] = times;
                    }
                    if (arr[i] == null) {
                        arr[i] = "-";
                    }
                }
            }
        },
        alltempinfo: function () {
            $("#allReport").addClass("active").siblings().removeClass("active");
            tempstatis.infoinputTab("/clbs/v/temperatureDetection/temperatureStatistics/list", 0);
            delete option.series[2].markPoint;
            delete option.series[3].markPoint;
            delete option.series[4].markPoint;
            delete option.series[5].markPoint;
            delete option.series[6].markPoint;
            myChart.clear();
            myChart.setOption(option);
        },
        hightempinfo: function () {
            $("#highttempReport").addClass("active").siblings().removeClass("active");
            url = "/clbs/v/temperatureDetection/temperatureStatistics/highList";
            //alert("高温数据显示");
            tempstatis.infoinputTab(url, 1);
            delete option.series[2].markPoint;
            delete option.series[3].markPoint;
            delete option.series[4].markPoint;
            delete option.series[5].markPoint;
            delete option.series[6].markPoint;
            myChart.clear();
            myChart.setOption(option);
            //点击显示标注
            $('#dataTable tbody').on('click', 'tr', function () {
                $(this).siblings(".odd").find("td").css('background-color', "#f9f9f9");
                $(this).siblings(".even").find("td").css('background-color', "#fff");
                var backgroundcolor = $(this).find("td").css('background-color');
                if (backgroundcolor == "rgb(220, 245, 255)") {
                    $("#dataTable tbody").find(".odd").find("td").css('background-color', "#f9f9f9");
                    $("#dataTable tbody").find(".even").find("td").css('background-color', "#fff");

                } else {
                    $(this).find("td").css('background-color', "#DCF5FF");
                    var datainfo = $(this).find("td").eq("2").text();
                    var tempsensor1 = $(this).find("td").eq("5").text();
                    var tempsensor2 = $(this).find("td").eq("6").text();
                    var tempsensor3 = $(this).find("td").eq("7").text();
                    var tempsensor4 = $(this).find("td").eq("8").text();
                    var tempsensor5 = $(this).find("td").eq("9").text();
                    option.series[2].markPoint = {
                        symbolSize: [60, 63],
                        //    symbolOffset: [0,-32],
                        silent: true,
                        data: [
                            {
                                name: '温度传感器温度1',
                                xAxis: datainfo,
                                value: tempsensor1,
                                yAxis: tempsensor1,
                                label: {
                                    normal: {
                                        show: true,
                                    }
                                },
                                itemStyle: {
                                    normal: {
                                        color: '#f4b5bd',
                                    },
                                },
                            },]
                    }
                    option.series[3].markPoint = {
                        symbolSize: [60, 63],
                        //    symbolOffset: [0,-32],
                        silent: true,
                        data: [
                            {
                                name: '温度传感器温度2',
                                xAxis: datainfo,
                                value: tempsensor2,
                                yAxis: tempsensor2,
                                label: {
                                    normal: {
                                        show: true,
                                    }
                                },
                                itemStyle: {
                                    normal: {
                                        color: '#E47C8C',
                                    },
                                },
                            },]
                    }
                    option.series[4].markPoint = {
                        symbolSize: [60, 63],
                        //    symbolOffset: [0,-32],
                        silent: true,
                        data: [
                            {
                                name: '温度传感器温度3',
                                xAxis: datainfo,
                                value: tempsensor3,
                                yAxis: tempsensor3,
                                label: {
                                    normal: {
                                        show: true,
                                    }
                                },
                                itemStyle: {
                                    normal: {
                                        color: '#e35052',
                                    },
                                },
                            },]
                    }
                    option.series[5].markPoint = {
                        symbolSize: [60, 63],
                        //    symbolOffset: [0,-32],
                        silent: true,
                        data: [
                            {
                                name: '温度传感器温度4',
                                xAxis: datainfo,
                                value: tempsensor4,
                                yAxis: tempsensor4,
                                label: {
                                    normal: {
                                        show: true,
                                    }
                                },
                                itemStyle: {
                                    normal: {
                                        color: '#f33168',
                                    },
                                },
                            },]
                    }
                    option.series[6].markPoint = {
                        symbolSize: [60, 63],
                        //    symbolOffset: [0,-32],
                        silent: true,
                        data: [
                            {
                                name: '温度传感器温度5',
                                xAxis: datainfo,
                                value: tempsensor5,
                                yAxis: tempsensor5,
                                label: {
                                    normal: {
                                        show: true,
                                    }
                                },
                                itemStyle: {
                                    normal: {
                                        color: '#a40c0d',
                                    },
                                },
                            },]
                    }
                }
                myChart.setOption(option);
            });
        },
        lowtempinfo: function () {
            $("#lowtempReport").addClass("active").siblings().removeClass("active");
            url = "/clbs/v/temperatureDetection/temperatureStatistics/lowList";
            //alert("低温数据显示");
            tempstatis.infoinputTab(url, 2);
            delete option.series[2].markPoint;
            delete option.series[3].markPoint;
            delete option.series[4].markPoint;
            delete option.series[5].markPoint;
            delete option.series[6].markPoint;
            myChart.clear();
            myChart.setOption(option);
            //点击显示标注
            $('#dataTable tbody').on('click', 'tr', function () {
                $(this).siblings(".odd").find("td").css('background-color', "#f9f9f9");
                $(this).siblings(".even").find("td").css('background-color', "#fff");
                var backgroundcolor = $(this).find("td").css('background-color');
                if (backgroundcolor == "rgb(220, 245, 255)") {
                    $("#dataTable tbody").find(".odd").find("td").css('background-color', "#f9f9f9");
                    $("#dataTable tbody").find(".even").find("td").css('background-color', "#fff");
                } else {
                    $(this).find("td").css('background-color', "#DCF5FF");
                    var datainfo = $(this).find("td").eq("2").text();
                    var allmiles = $(this).find("td").eq("3").text();
                    var speed = $(this).find("td").eq("4").text();
                    var tempsensor1 = $(this).find("td").eq("5").text();
                    var tempsensor2 = $(this).find("td").eq("6").text();
                    var tempsensor3 = $(this).find("td").eq("7").text();
                    var tempsensor4 = $(this).find("td").eq("8").text();
                    var tempsensor5 = $(this).find("td").eq("9").text();
                    option.series[2].markPoint = {
                        symbolSize: [60, 63],
                        //    symbolOffset: [0,-32],
                        silent: true,
                        data: [
                            {
                                name: '温度传感器温度1',
                                xAxis: datainfo,
                                value: tempsensor1,
                                yAxis: tempsensor1,
                                label: {
                                    normal: {
                                        show: true,
                                    }
                                },
                                itemStyle: {
                                    normal: {
                                        color: '#f4b5bd',
                                    },
                                },
                            },]
                    }
                    option.series[3].markPoint = {
                        symbolSize: [60, 63],
                        //    symbolOffset: [0,-32],
                        silent: true,
                        data: [
                            {
                                name: '温度传感器温度2',
                                xAxis: datainfo,
                                value: tempsensor2,
                                yAxis: tempsensor2,
                                label: {
                                    normal: {
                                        show: true,
                                    }
                                },
                                itemStyle: {
                                    normal: {
                                        color: '#E47C8C',
                                    },
                                },
                            },]
                    }
                    option.series[4].markPoint = {
                        symbolSize: [60, 63],
                        //    symbolOffset: [0,-32],
                        silent: true,
                        data: [
                            {
                                name: '温度传感器温度3',
                                xAxis: datainfo,
                                value: tempsensor3,
                                yAxis: tempsensor3,
                                label: {
                                    normal: {
                                        show: true,
                                    }
                                },
                                itemStyle: {
                                    normal: {
                                        color: '#e35052',
                                    },
                                },
                            },]
                    }
                    option.series[5].markPoint = {
                        symbolSize: [60, 63],
                        //    symbolOffset: [0,-32],
                        silent: true,
                        data: [
                            {
                                name: '温度传感器温度4',
                                xAxis: datainfo,
                                value: tempsensor4,
                                yAxis: tempsensor4,
                                label: {
                                    normal: {
                                        show: true,
                                    }
                                },
                                itemStyle: {
                                    normal: {
                                        color: '#f33168',
                                    },
                                },
                            },]
                    }
                    option.series[6].markPoint = {
                        symbolSize: [60, 63],
                        //    symbolOffset: [0,-32],
                        silent: true,
                        data: [
                            {
                                name: '温度传感器温度5',
                                xAxis: datainfo,
                                value: tempsensor5,
                                yAxis: tempsensor5,
                                label: {
                                    normal: {
                                        show: true,
                                    }
                                },
                                itemStyle: {
                                    normal: {
                                        color: '#a40c0d',
                                    },
                                },
                            },]
                    }

                }
                myChart.setOption(option);
            });

        },
        //点击出现标记
        clickMarkpoint: function () {
            option.series[0].markPoint = {
                data: [
                    {name: '周最低', value: 66, xAxis: date[0], yAxis: 88}
                ]
            };
            myChart.setOption(option);
        },
        showMenu: function (e) {
            if ($("#menuContent").is(":hidden")) {
                var inpwidth = $("#groupSelect").outerWidth();
                $("#menuContent").css("width", inpwidth + "px");
                $(window).resize(function () {
                    var inpwidth = $("#groupSelect").outerWidth();
                    $("#menuContent").css("width", inpwidth + "px");
                })
                $("#menuContent").slideDown("fast");
            } else {
                $("#menuContent").is(":hidden");
            }
            $("body").bind("mousedown", tempstatis.onBodyDown);
        },
        hideMenu: function () {
            $("#menuContent").fadeOut("fast");
            $("body").unbind("mousedown", tempstatis.onBodyDown);
        },
        onBodyDown: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "menuContent" || $(event.target).parents("#menuContent").length > 0)) {
                tempstatis.hideMenu();
            }
        },
        //模糊查询树
        searchVehicleTree: function (param) {
            ifAllCheck = false;//模糊查询不自动勾选
            crrentSubV = [];
            $('#charSelect').val('');
            if (param == null || param == undefined || param == '') {
                bflag = true;
                tempstatis.treeInit()
            } else {
                bflag = true;
                var querySetting = {
                    async: {
                        url: "/clbs/a/search/reportFuzzySearch",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"type": $('#queryType').val(), "queryParam": param, "queryType": "multiple"},
                        dataFilter: tempstatis.ajaxQueryDataFilter
                    },
                    check: {
                        enable: true,
                        chkStyle: "checkbox",
                        radioType: "all",
                        chkboxType: {
                            "Y": "s",
                            "N": "s"
                        }
                    },
                    view: {
                        dblClickExpand: false,
                        nameIsHTML: true,
                        countClass: "group-number-statistics"
                    },
                    data: {
                        simpleData: {
                            enable: true
                        }
                    },
                    callback: {
                        beforeClick: tempstatis.beforeClickVehicle,
                        onCheck: tempstatis.onCheckVehicle,
                        onExpand: tempstatis.zTreeOnExpand,
                        onNodeCreated: tempstatis.zTreeOnNodeCreated
                    }
                };
                $.fn.zTree.init($("#treeDemo"), querySetting, null);
            }
        },
        treeInit: function () {
            var setting = {
                async: {
                    url: tempstatis.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: tempstatis.ajaxDataFilter
                },
                check: {
                    enable: true,
                    chkStyle: "checkbox",
                    radioType: "all",
                    chkboxType: {
                        "Y": "s",
                        "N": "s"
                    }
                },
                view: {
                    dblClickExpand: false,
                    nameIsHTML: true,
                    countClass: "group-number-statistics"
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    beforeClick: tempstatis.beforeClickVehicle,
                    onCheck: tempstatis.onCheckVehicle,
                    beforeCheck: tempstatis.zTreeBeforeCheck,
                    onExpand: tempstatis.zTreeOnExpand,
                    onAsyncSuccess: tempstatis.zTreeOnAsyncSuccess,
                    onNodeCreated: tempstatis.zTreeOnNodeCreated
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
            $("[data-toggle='tooltip']").tooltip();
        },
        getTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/functionconfig/fence/bindfence/alarmSearchTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=monitor";
            }
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            var rData;
            if (!responseData.msg) {
                rData = responseData;
            } else {
                rData = responseData.msg;
            }
            var obj = JSON.parse(ungzip(rData));
            var data;
            if (obj.tree != null && obj.tree != undefined) {
                data = obj.tree;
                size = obj.size;
            } else {
                data = obj
            }
            for (var i = 0; i < data.length; i++) {
                data[i].open = true;
            }
            return data;
        },
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                isSearch = false;
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    tempstatis.validates();
                }, 600);
            }
            if (treeNode.type != "assignment" || (treeNode.type == "assignment" && treeNode.children != undefined)) {
                tempstatis.getCharSelect(zTree);
            }
            crrentSubV = [];
            crrentSubV.push(treeNode.id);
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;

                    json_ajax("post", "/clbs/m/personalized/ico/getCheckedVehicle",
                        "json", false, {"parentId": treeNode.id, "type": treeNode.type}, function (data) {
                            if (data.success) {
                                nodesLength += data.obj;
                            } else {
                                layer.msg(data.msg);
                            }
                        });

                    //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                    var ns = [];
                    //节点id
                    var nodeId;
                    for (var i = 0; i < nodes.length; i++) {
                        nodeId = nodes[i].id;
                        if (nodes[i].type == "people" || nodes[i].type == "vehicle") {
                            //查询该节点是否在勾选组织或分组下，若在则不记录，不在则记录
                            var nd = zTree.getNodeByParam("tId", nodes[i].tId, treeNode);
                            if (nd == null && $.inArray(nodeId, ns) == -1) {
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength += ns.length;
                } else if (treeNode.type == "people" || treeNode.type == "vehicle") { //若勾选的为监控对象
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;
                    //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                    var ns = [];
                    //节点id
                    var nodeId;
                    for (var i = 0; i < nodes.length; i++) {
                        nodeId = nodes[i].id;
                        if (nodes[i].type == "people" || nodes[i].type == "vehicle") {
                            if ($.inArray(nodeId, ns) == -1) {
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength = ns.length + 1;
                }
                if (nodesLength > 5000) {
                    layer.msg(maxSelectItem);
                    flag = false;
                }
            }
            if (flag) {
                //若组织节点已被勾选，则是勾选操作，改变勾选操作标识
                if (treeNode.type == "group" && !treeNode.checked) {
                    checkFlag = true;
                }
            }
            return flag;
        },
        zTreeOnExpand: function (event, treeId, treeNode) {
            //判断是否是勾选操作展开的树(是则继续执行，不是则返回)
            if (treeNode.type == "group" && !checkFlag) {
                return;
            }
            //初始化勾选操作判断表示
            checkFlag = false;
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (treeNode.type == "group") {
                var url = "/clbs/m/functionconfig/fence/bindfence/putMonitorByGroup";
                json_ajax("post", url, "json", false, {
                    "groupId": treeNode.id,
                    "isChecked": treeNode.checked,
                    "monitorType": "vehicle"
                }, function (data) {
                    var result = data.obj;
                    if (result != null && result != undefined) {
                        $.each(result, function (i) {
                            var pid = i; //获取键值
                            var chNodes = result[i] //获取对应的value
                            var parentTid = zTreeIdJson[pid][0];
                            var parentNode = treeObj.getNodeByTId(parentTid);
                            if (parentNode.children === undefined) {
                                treeObj.addNodes(parentNode, []);
                            }
                        });
                    }
                })
            }
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (size <= 5000 && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            tempstatis.getCharSelect(treeObj);

            bflag = false;
        },
        getCharSelect: function (treeObj) {
            var nodes = treeObj.getCheckedNodes(true);
            var allNodes = treeObj.getNodes();
            if (nodes.length > 0) {
                $("#groupSelect").val(allNodes[0].name);
            } else {
                $("#groupSelect").val("");
            }
            $("#charSelect").val("").attr("data-id", "").bsSuggest("destroy");
            var veh = [];
            var vid = [];
            for (var i = 0; i < nodes.length; i++) {
                if (nodes[i].type == "vehicle" || nodes[i].type == "people" || nodes[i].type == "thing") {
                    var isNull = nodes[i].name != null && nodes[i].name != undefined && nodes[i].name != "" && nodes[i].id != null && nodes[i].id != undefined && nodes[i].id != "";
                    if (isNull && $.inArray(nodes[i].id, vid) == -1) {
                        veh.push(nodes[i].name)
                        vid.push(nodes[i].id)
                    }
                }
            }
            var vehName = veh;
            var vehId = vid;
            $("#charSelect").empty();
            var deviceDataList = {value: []};
            for (var j = 0; j < vehName.length; j++) {
                for (var k = 0; k < vehicleList.length; k++) {
                    if (vehId[j] == vehicleList[k].vehicleId) {
                        deviceDataList.value.push({
                            name: vehicleList[k].brand,
                            id: vehId[j]
                        });
                    }
                }
            }
            ;
            $("#charSelect").bsSuggest({
                indexId: 1, //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                data: deviceDataList,
                effectiveFields: ["name"]
            }).on('onDataRequestSuccess', function (e, result) {
            }).on("click", function () {
            }).on('onSetSelectValue', function (e, keyword, data) {
            }).on('onUnsetSelectValue', function () {
            });
            if (deviceDataList.value.length > 0) {
                $("#charSelect").val(deviceDataList.value[0].name).attr("data-id", deviceDataList.value[0].id);
            }
            $("#groupSelect,#groupSelectSpan").bind("click", tempstatis.showMenu);
            $("#button").removeClass('disabled loading-state-button').find('i').attr("class", 'caret');
        },
        zTreeOnNodeCreated: function (event, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            var id = treeNode.id.toString();
            var list = [];
            if (zTreeIdJson[id] == undefined || zTreeIdJson[id] == null) {
                list = [treeNode.tId];
                zTreeIdJson[id] = list;
            } else {
                zTreeIdJson[id].push(treeNode.tId)
            }
        },
        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData))
            var nodesArr;
            if ($('#queryType').val() == "vehicle") {
                nodesArr = filterQueryResult(responseData, crrentSubV);
            } else {
                nodesArr = responseData;
            }
            for (var i = 0; i < nodesArr.length; i++) {
                nodesArr[i].open = true;
            }
            return nodesArr;
        },
        //列表数据导出
        exportFun: function () {
            var index = $('.nav-tabs li.active').attr('data-index');
            if ($('.table:visible tbody tr td').hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据，无法导出");
                return;
            }
            if(getRecordsNum() > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var url = "/clbs/v/temperatureDetection/temperatureStatistics/exportDataList?type=" + index + '&brand=' + curBrand + '&stime=' + startTime + '&ntime=' + endTime;
            window.location.href = url
        },
    };
    $(function () {
        $('input').inputClear();
        tempstatis.treeInit();
        $("#toggle-left").bind("click", function () {
            setTimeout(function () {
                tempstatis.init();
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
        tempstatis.nowDay();
        $('#timeInterval').dateRangePicker({
            dateLimit: 7
        });
        $("#todayClick").bind("click", tempstatis.todayClick);
        $("#yesterdayClick,#right-arrow").bind("click", tempstatis.yesterdayClick);
        $("#nearlyThreeDays").bind("click", tempstatis.nearlyThreeDays);
        $("#nearlySevenDays").bind("click", tempstatis.nearlySevenDays);
        $("#inquireClick").bind("click", tempstatis.inquireClick);
        $("#showClick").bind("click", tempstatis.showClick);
        $("#left-arrow").bind("click", tempstatis.upDay);
        $("#allReport").bind("click", tempstatis.alltempinfo);
        $("#highttempReport").bind("click", tempstatis.hightempinfo);
        $("#lowtempReport").bind("click", tempstatis.lowtempinfo);
        $("#groupSelectSpan,#groupSelect").bind("click", tempstatis.showMenu); //组织下拉显示

        // 导出
        $("#exportBtn").bind("click", tempstatis.exportFun);

        $("#stretch").unbind();
        $(".h1").bind("click", tempstatis.clickMarkpoint);
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var param = $("#groupSelect").val();
                tempstatis.searchVehicleTree(param);
            }
            ;
        });
        /**
         * 监控对象树模糊查询
         */
        var inputChange;
        $("#groupSelect").on('input propertychange', function (value) {
            if (inputChange !== undefined) {
                clearTimeout(inputChange);
            }
            ;
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                isSearch = true;
            }
            $("#charSelect").val("").attr("data-id", "").bsSuggest("destroy");
            inputChange = setTimeout(function () {
                if (isSearch) {
                    var param = $("#groupSelect").val();
                    tempstatis.searchVehicleTree(param);
                    setTimeout(function () {
                        tempstatis.showMenu();
                    }, 300);
                }
                isSearch = true;
            }, 500);
        });
        $('#queryType').on('change', function () {
            var param = $("#groupSelect").val();
            tempstatis.searchVehicleTree(param);
        });
    });
}($, window));