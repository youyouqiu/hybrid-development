var globalVars = {unloaded:false};
$(window).bind('beforeunload', function(){
    globalVars.unloaded = true;
});

function json_ajax(type, url, dataType, async, data, callback) {
    $.ajax(
        {
            type: type,//通常会用到两种：GET,POST。默认是：GET
            url: url,//(默认: 当前页地址) 发送请求的地址
            dataType: dataType, //预期服务器返回的数据类型。"json"
            async: async, // 异步同步，true  false
            data: data,
            timeout: 30000, //超时时间设置，单位毫秒
            beforeSend: beforeSend, //发送请求
            success: callback, //请求成功
            error: error,//请求出错
            complete: complete//请求完成
        });
}

function error(XMLHttpRequest, textStatus, errorThrown) {
    if (globalVars.unloaded) {
        return;
    }

    // layer.closeAll('loading');

    if (textStatus === "timeout") {
        layer.msg("因网络较慢或数据量过大而请求超时，请稍候再试！");
        return;
    }
    if (XMLHttpRequest.responseText.indexOf("<form id=\"loginForm") > 0) {
        window.location.replace("/clbs/login?type=expired");
        return;
    }
    layer.msg("系统响应异常，请稍后再试或联系管理员！");
}

function beforeSend(XMLHttpRequest) {

    // layer.load(2);

}

function complete(msg) {
    if (msg.responseText && msg.responseText.indexOf("<form id=\"loginForm") > 0) {
        window.location.replace("/clbs/login?type=expired");
        return;
    }

    // layer.closeAll('loading');

}

$(function () {
    var isIE;
    //提示语
    $('[data-toggle="tooltip"]').tooltip();

    var main = {
        doughnut: undefined,
        doughnutData:[],
        doughnutCommonOption: {
            tooltip: {
                trigger: 'item',
                formatter: "{a} <br/>{b}: {c} ({d}%)"
            },
            legend: {
                orient: 'vertical',
                x: 'right',
                top: 70,
                bottom: 10,
                data: [],
                align: 'left',
                itemHeight: 16,
                textStyle: {
                    color: 'white',
                    fontSize: 14,
                },
                selectedMode:false
            },
            color: ['rgba(86,82,155,0.6)', 'rgba(78,161,169,0.6)', 'rgba(188,147,48,0.6)', 'rgba(26,94,142,0.6)', 'rgba(106,128,169,0.6)', 'rgba(116,159,131,0.6)',],
            series: [
                {
                    name: '运营类别',
                    type: 'pie',
                    radius: ['60%', '85%'],
                    avoidLabelOverlap: false,
                    label: {
                        opacity: 1,
                        normal: {
                            show: true,
                            position: 'center',
                            textStyle: {
                                fontSize: 44,
                                color: 'white',
                                fontFamily: 'Bgothm',
                                letterSpacing: -10,
                                lineHeight: 60,
                            },
                            formatter: ['{c}', '{title|{b}}'].join('\n'),
                            rich: {
                                title: {
                                    fontSize: 18,
                                    color: '#7ca1d2',
                                    height: 30,
                                    lineHeight: 30,
                                }
                            }
                        },
                        emphasis: {
                            show: false,
                            textStyle: {
                                fontSize: '46',
                                fontWeight: 'bold'
                            }
                        }
                    },
                    labelLine: {
                        normal: {
                            show: false
                        }
                    },
                    data: [],
                    // itemStyle:{
                    //     opacity:0.6
                    // }
                }
            ]
        },
        doughnutPageIndex:0,
        doughnutCenterIndex:0,
        doughnutFace:'front',
        onlineRate: undefined,
        onlineRateCommonOption: {
            animationDuration: 3000,
            grid: {
                left: '2%',
                right: '2%',
                bottom: '10%',
                top:'10%',
                containLabel: true,
            },
            xAxis: {
                type: 'category',
                boundaryGap: false,
                axisTick: {
                    alignWithLabel: true,
                    show: false
                },
                axisLabel: {
                    color: '#7ca1d2',
                },
                axisLine: {
                    show: false
                },

                data: ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23']
            },
            yAxis:
            {
                type: 'value',
                name: '',
                min: 0,
                max: 100,
                interval: 25,
                axisLabel: {
                    formatter: '{value}%',
                    color: '#7ca1d2',
                },
                axisLine: {
                    show: false
                },
                axisTick: {
                    show: false
                },
                splitLine:{
                    lineStyle:{
                        color:'rgba(121,243,212,0.2)',
                    }
                }
            }
            ,
            series: [{
                data: [
                    { name: '0', value: 22 },
                    { name: '1', value: 12 },
                    { name: '2', value: 2 },
                    { name: '3', value: 82 },
                    { name: '4', value: 22 },
                    { name: '5', value: 12 },
                    { name: '6', value: 2 },
                    { name: '7', value: 82 },
                    { name: '08', value: 22 },
                    { name: '09', value: 12 },
                    { name: '10', value: 2 },
                    { name: '11', value: 82 },
                ],
                type: 'line',
                itemStyle: {
                    color: '#79f3d4'
                },
                lineStyle: {
                    color: 'rgb(87, 170, 216)'
                },
                areaStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
                        offset: 0,
                        color: 'rgb(84, 157, 220)'
                    }, {
                        offset: 1,
                        color: 'rgba(84, 157, 220,0.1)'
                    }])
                },
                symbol: 'circle',
                symbolSize: 8,


            }]
        },
        alarmRankData:[],
        alarmRankPageIndex:0,
        alarmRankFace:'front',
        alarmRankMax:0,
        init: function () {
            this.initLogo();
            this.initDoughnut();
            this.initAlarmRank();
            this.initOnlineRate();
            this.initTime();
            this.initAlarmNumber();
            this.initAllMonitorNumber();
            isIE = this.isIEFun();//判断是否为ie浏览器,解决ie翻转动画兼容性问题
            $(window).resize(function(){
                if (main.doughnut !== undefined){
                    main.doughnut.resize();
                }
                if (main.onlineRate !== undefined){
                    main.onlineRate.resize();
                }
            });
        },
        initLogo:function(){
            var url = "/clbs/m/intercomplatform/personalized/find";
            var data = {"uuid": $("#userGroupId").val()};
            json_ajax("POST", url, "json", true, data, function (data) {
                if (data.success){
                    var list = data.obj.list;
                    var homeLogo = "/clbs/resources/img/logo/" + list.homeLogo;
                    $("#logoImg").attr('src', homeLogo);
                }
            });
        },
        isIEFun: function(){
            if (!!window.ActiveXObject || "ActiveXObject" in window) {
                return true;
            }
            else {
                return false;
            }
        },
        initAlarmNumber:function(){
            // var i = 0;
            // var today;
            // var yestoday;
            //
            // var percentage = function(){
            //     if (i === 2){
            //         var diff = today - yestoday;
            //         var percentage;
            //         if (yestoday == 0){
            //             if (today > 0){
            //                 percentage = '100';
            //             } else{
            //                 percentage = '0';
            //             }
            //         } else{
            //             percentage = (diff / yestoday * 100).toFixed(1);
            //         }
            //         $('#comparePercent').html(percentage.toString() + '%');
            //         if (diff > 0){
            //             $('#compareText').html('增长');
            //             $('.green-red').addClass('up');
            //         } else if (diff < 0){
            //             $('.green-red').addClass('down');
            //             $('#compareText').html('下降');
            //         }else{
            //             $('#compareText').html('不变');
            //         }
            //     }
            // }
            json_ajax("POST", '/clbs/adas/lbOrg/show/getNowRisk', "json", true, {}, function (data) {
                $('.real-alarm-num').html(data);
                // today = data;
                // percentage();
            });

            // 昨日风险报警数
            json_ajax("POST", '/clbs/adas/lbOrg/show/getYesterdayRisk', "json", true, {}, function (data) {
                $('#yestodayAlarm').html(data);
                // yestoday = data;
                // percentage();
            });

            // 环比增长
            json_ajax('post', '/clbs/adas/lbOrg/show/getRingRatioRiskEvent', 'json', true, '', function (data) {
                if (data.success) {
                    data = data.obj;

                    $('#comparePercent').html(data.ringRatio);
                    if (data.trend == 1){
                        $('#compareText').html('增长');
                        $('.green-red').addClass('up');
                    } else if (data.trend == -1){
                        $('.green-red').addClass('down');
                        $('#compareText').html('下降');
                    }else{
                        $('#compareText').html('不变');
                    }

                }
            })
        },
        initAllMonitorNumber:function(){
            var getNumber = function () {
                json_ajax("POST", '/clbs/m/functionconfig/fence/bindfence/getRunAndStopMonitorNum', "json", true, {
                    "isNeedMonitorId": false, "userName": $("#userName").text()
                }, function (data) {
                    $("#tline").html( data.obj.onlineNum );
                    $("#tmiss").html(data.obj.allV - data.obj.onlineNum);
                    $("#tall").html( data.obj.allV);
                });
            }
            getNumber();
            // 10 秒一刷新
            setInterval(getNumber,10000);
        },
        initDoughnut: function () {
            main.getDoughnutData(function(data){
                main.doughnutData = data;
                main.doughnutCommonOption.series[0].data = main.doughnutData.map(function(x){
                    return {
                        value: x.value, name: x.name, label: { normal: { show: false, } }
                    };
                });
                main.doughnut = echarts.init(document.getElementById('doughnut-container'));

                var renderDoughnut = function(){
                    var dataSlice = main.doughnutData.slice(main.doughnutPageIndex*5,(main.doughnutPageIndex+1)*5);
                    main.doughnutCommonOption.legend.data = dataSlice.map(function(x){
                        return x.name;
                    });
                    for (var i = 0; i < main.doughnutCommonOption.series[0].data.length; i++) {
                        var element = main.doughnutCommonOption.series[0].data[i];
                        if (element.name === main.doughnutData[main.doughnutCenterIndex].name) {
                            element.label.normal.show = true;
                        }else{
                            element.label.normal.show = false;
                        }
                    }
                    main.doughnut.setOption(main.doughnutCommonOption);
                    
                    if (main.doughnutCenterIndex < main.doughnutData.length - 1) {
                        main.doughnutCenterIndex++;
                    }else{
                        main.doughnutCenterIndex = 0;
                    }
                }

                var renderFace = function(){
                    var $backDivs = $('.dot-list .' + main.doughnutFace);
                    var dataSlice = main.doughnutData.slice(main.doughnutPageIndex*5,(main.doughnutPageIndex+1)*5);
                    $backDivs.each(function(idx,ele){
                        var $ele = $(ele);
                        if (dataSlice[idx] === undefined) {
                            $ele.addClass('no-visible');
                            return;
                        }
                        $ele.removeClass('no-visible');
                        $ele.find('.dot-list-text').html(dataSlice[idx].name);
                        $ele.find('.dot-list-num').html(dataSlice[idx].ratio.replace('%',''));
                    });
                }

                setInterval(function(){
                    main.doughnutPageIndex = main.doughnutPageIndex < parseInt(main.doughnutData.length / 5) ? main.doughnutPageIndex + 1 : 0;

                    main.doughnutFace = (!isIE && main.doughnutFace === 'front') ? 'backend' : 'front';

                    renderFace();
                    renderDoughnut();

                    var $backDivs = $('.dot-list .' + main.doughnutFace);
                    $backDivs.each(function(idx,element){
                        setTimeout((function(ele){
                           return function(){
                               if(!isIE){
                                   $(ele).closest('.dot-list-row').toggleClass('active');
                               }
                           }
                        }(element)), 250*idx);
                    });
                }, 3000);

                renderFace();
                renderDoughnut();
            });
        },
        initAlarmRank:function(){
            main.getAlarmRankData(function(data){
                main.alarmRankData = data.sort(function(a,b){
                    return b.value - a.value;
                });
                for (var i = 0; i < main.alarmRankData.length; i++) {
                    var element = parseFloat(main.alarmRankData[i].value);
                    if(element > main.alarmRankMax){
                        main.alarmRankMax = element;
                    }
                }

                var renderAlarm = function(){
                    var $backDivs = $('.alarm-container .' + main.alarmRankFace);
                    var dataSlice = main.alarmRankData.slice(main.alarmRankPageIndex*5,(main.alarmRankPageIndex+1)*5);
                    $backDivs.each(function(idx,ele){
                        var $ele = $(ele);
                        var realIndex = main.alarmRankPageIndex*5 + idx;
                        var indexColor = 'alarm-index-color4';
                        var numColor = 'alarm-num-color2';
                        switch (realIndex) {
                            case 0:
                                indexColor = 'alarm-index-color1';
                                numColor = 'alarm-num-color1';
                                break;
                            case 1:
                                indexColor = 'alarm-index-color2';
                                numColor = 'alarm-num-color1';
                                break;
                            case 2:
                                indexColor = 'alarm-index-color3';
                                numColor = 'alarm-num-color1';
                                break;
                            default:
                                break;
                        }
                        if (dataSlice[idx] === undefined) {
                            $ele.addClass('no-visible');
                            return;
                        }
                        $ele.removeClass('no-visible');
                        var percentage = (parseFloat(dataSlice[idx].value) / main.alarmRankMax * 100).toFixed(1);
                        var realIndexStr = (realIndex + 1) < 10 ? '0' + (realIndex + 1).toString() : (realIndex + 1).toString();
                        $ele.find('.alarm-index').html(realIndexStr).attr('class','alarm-index ' + indexColor);
                        $ele.find('.alarm-bg>div').css('width',percentage.toString() + '%');
                        $ele.find('.alarm-brand').html(dataSlice[idx].name);
                        $ele.find('.alarm-num').attr('class','alarm-num ' + numColor);
                        $ele.find('.alarm-value').html(dataSlice[idx].value);
                    });
                }

                setInterval(function(){
                    var more = parseInt(main.alarmRankData.length % 5);
                    var num =  parseInt(main.alarmRankData.length / 5);
                    var page = more == 0 ? (num - 1) : num;

                    main.alarmRankPageIndex = main.alarmRankPageIndex < page ? (main.alarmRankPageIndex + 1) : 0;
                    main.alarmRankFace = (!isIE && main.alarmRankFace === 'front') ? 'backend' : 'front';

                    renderAlarm();

                    var $backDivs = $('.alarm-container .' + main.alarmRankFace);
                    $backDivs.each(function(idx,element){
                        setTimeout((function(ele){
                           return function(){
                               if(!isIE){
                                   $(ele).closest('.alarm-row').toggleClass('active');
                               }
                           }
                        }(element)), 250*idx);
                    });
                }, 3000);

                renderAlarm();
            });
        },
        initOnlineRate: function () {
            if (this.onlineRate === undefined) {
                // this.onlineRate = echarts.init(document.getElementById('onlineRate-container'));

                var renderChart = function () {
                    main.getOnlineRateData(function (data) {
                        if (main.onlineRate && main.onlineRate.dispose){
                            main.onlineRate.dispose();
                        }
                        main.onlineRate = echarts.init(document.getElementById('onlineRate-container'));
                        main.onlineRateCommonOption.series[0].data = data;
                        main.onlineRate.setOption(main.onlineRateCommonOption);
                    });
                }
                renderChart();
                setInterval(renderChart,10000)
            }
        },
        initTime: function () {
            var $timeHour1 = $('#timeHour1');
            var $timeHour2 = $('#timeHour2');
            var $timeMinute1 = $('#timeMinute1');
            var $timeMinute2 = $('#timeMinute2');
            var $timeSeperator = $('#time-seperator');
            var show = true;

            var func = function () {
                var now = new Date();
                var hour = now.getHours();
                var minute = now.getMinutes();
                $timeHour1.html(parseInt(hour / 10));
                $timeHour2.html(parseInt(hour % 10));
                $timeMinute1.html(parseInt(minute / 10));
                $timeMinute2.html(parseInt(minute % 10));
                // $timeSeperator.css('visibility',show ? 'visible' : 'hidden');
                // show = !show;
            };

            setInterval(func, 1000);
            func();
        },
        getDoughnutData:function(cb){
            // var fakeData = [];
            // for (let i = 0; i < 13 ;i++) {
            //     fakeData.push({
            //         name: i % 2 === 0 ? '挖掘机技术哪'+i:'中国山东找南'+i,
            //         value:(Math.random()*100).toFixed(1)
            //     });
            // }
            // cb(fakeData);
            json_ajax("POST", '/clbs/adas/lbOrg/show/getOperationCategory', "json", true, {
                provinceCode: 230000,
                cityCode: 0,
                countyCode: 0
            }, function (data) {
                if (data.success == true) {
                    cb(data.obj.map(function (x) {
                        return {
                            name:x.name,
                            value:parseInt(x.number),
                            ratio:x.ratio
                        }
                    }))
                }
            });
        },
        getAlarmRankData:function(cb){
            // var fakeData = [];
            // for (let i = 0; i < 13 ;i++) {
            //     fakeData.push({
            //         name: '蒙B'+i.toString()+i.toString()+i.toString()+i.toString()+i.toString(),
            //         value:(Math.random()*100).toFixed(1)
            //     });
            // }
            // cb(fakeData);

            json_ajax("POST", "/clbs/adas/lb/guide/getRankOfVehicle", "json", true, {}, function (data) {
                if (data.success == true) {
                    cb(data.obj.map(function (x) {
                        return {
                            name:x.brand,
                            value:parseInt(x.total)
                        }
                    }))
                }
            });
        },
        getOnlineRateData:function (cb) {
            json_ajax("POST", "/clbs/adas/lbOrg/show/getVehOnlineTrend", "json", true, {
                groupId: '',
                isToday: true,
            }, function (data) {
                if (data.success) {
                    data = data.obj;

                    var date = new Date();
                    var len = date.getHours();

                    var chartData = [];

                    for (var i = 0; i <= len; i++){
                        var item = {
                            name:main.onlineRateCommonOption.xAxis.data[i],
                            value:0
                        }
                        for (var j = 0; j < data.length;j++){
                            var time = data[j].time.toString().substr(-2);
                            if (parseInt(time) == parseInt(item.name)){
                                item.value = data[j].rate;
                                break;
                            }
                        }
                        chartData.push(item);
                    }

                    cb(chartData);
                }
            });
        }
    };
    main.init();
})