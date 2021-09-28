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
        layer.msg("加载超时，请重试");
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


var safetyboard;

(function (window, $) {
    //地图
    var map;
    var styleObject=[];
    var massMarks = null,
        marker;

    var riskEvents = [],
        riskEventNum = [],//报警事件选中的随机数
        riskNowNumber = 0,//上一个实际报警数数据
        riskWarnData = [],//上一个风险占比数据
        maxValue = 0;

    var timer = null,
        timer3 = null,
        timer4 = null,
        timer2 = null;

    var isInit = true;

    safetyboard = {
        initMap:function(){
            map = new AMap.Map('mapContainer', {
                zoom: 5,//级别
                center: [106.504962, 29.533155],//中心点坐标
                resizeEnable: true,
            });
            map.setMapStyle('amap://styles/grey');

            safetyboard.getVehiclesInfo();
            setInterval(function(){
                safetyboard.getVehiclesInfo();
            }, 10000);
            //监控对象车牌
            marker = new AMap.Marker({
                content: ' ',
                offset: new AMap.Pixel(10,-45),
                map: map,
                closeWhenClickMap: true
            });
        },
        /**
         * 获取地图监控对象信息
         */
        getVehiclesInfo: function () {
            json_ajax('post', '/clbs/adas/lb/guide/getVehiclePositional', 'json', true, {}, function (data) {
                if (data.success && data.obj.length) {
                    var datas =data.obj;
                    var markerAllData = [];

                    for(var i=0;i<datas.length;i++){
                        var item = datas[i];
                        var obj = {
                            lnglat: [item.longitude, item.latitude],
                            id:item.vehicleId,
                            style: safetyboard.getStatus(item.status),
                            name: item.brand
                        };
                        markerAllData.push(obj);
                    }

                    if(!massMarks){
                        safetyboard.markerAllData(markerAllData);
                    }else{
                        massMarks.clear();
                        massMarks.setData(markerAllData);
                    }
                }
            })
        },
        markerAllData: function(markerAllData){
            //创建海量点
            safetyboard.setStyleObject();
            massMarks = new AMap.MassMarks(markerAllData,{
                zIndex: 100,
                style: styleObject,
                alwaysRender: true
            });
            massMarks.setMap(map);

            if(massMarks){
                massMarks.on('mouseover', function (e) {
                    marker.setPosition(e.data.lnglat);
                    var content = '<div class="marker-label">'+ e.data.name +'</div>'
                    marker.setLabel({content: content});
                    marker.show();
                }).on('mouseout', function(){
                    marker.hide();
                });
            }
        },
        getStatus: function(status){
            switch (status){
                case 11://心跳
                    return 0;
                    break;
                case 10://行驶
                    return 1;
                    break;
                case 4://停止
                    return 2;
                    break;
                case 9://超速
                    return 3;
                    break;
                case 5://报警
                    return 4;
                    break;
                case 3://离线
                    return 5;
                    break;
                case 2://未定位
                    return 6;
                    break;
                default:
                    break;
            }
        },
        setStyleObject: function(){
            var statusNum = 7;
            styleObject = [];
            for(var i=0;i<statusNum;i++){
                var obj = {
                    url: safetyboard.getStatusIcon(i),
                    size: new AMap.Size(30, 30),
                    anchor: new AMap.Pixel(5,5)
                };
                styleObject.push(obj);
            }
        },
        getStatusIcon: function(index){
            switch (index){
                case 0://心跳
                    return '/clbs/resources/img/marker1_1.png';
                    break;
                case 1://行驶
                    return '/clbs/resources/img/marker3_1.png';
                    break;
                case 2://停止
                    return '/clbs/resources/img/marker6_1.png';
                    break;
                case 3://超速
                    return '/clbs/resources/img/marker4_1.png';
                    break;
                case 4://报警
                    return '/clbs/resources/img/marker2_1.png';
                    break;
                case 5://离线
                    return '/clbs/resources/img/marker5_1.png';
                    break;
                case 6://未定位
                    return '/clbs/resources/img/marker7_1.png';
                    break;
                default:
                    break;
            }
        },
        /**
         * 昨日此时环比增长
         */
        getRingRatioRiskEvent: function () {
            json_ajax('post', '/clbs/adas/lbOrg/show/getRingRatioRiskEvent', 'json', true, '', function (data) {
                if (data.success) {
                    data = data.obj;
                    var icon = $('#yestody-ratio .y-ratio-icon'),
                        yRatio = $('#yestody-ratio .y-ratio');

                    if (data.trend == 0) {
                        // yRatio.text('环比：' + data.ringRatio);
                        icon.addClass('normal');
                    } else if (data.trend == 1) {
                        // yRatio.text('环比增长：' + data.ringRatio);
                        icon.addClass('up');
                    } else if (data.trend == -1) {
                        // yRatio.text('环比下降：' + data.ringRatio);
                        icon.addClass('down');
                    }

                    var ringRatio = data.ringRatio ? data.ringRatio : 0;
                    yRatio.text('环比：' + ringRatio);
                }
            })
        },
        /**
         * 获取实时风控预警数
         */
        getNowRisk: function () {
            json_ajax('post', '/clbs/adas/lbOrg/show/getNowRisk', 'json', true, '', function (data) {
                if(riskNowNumber != data){
                    $('#risk-number .num-box>span').html(data);
                    $('.num-box').addClass('active');
                    setTimeout(function(){
                        $('.num-box').removeClass('active');
                    }, 400);
                    riskNowNumber = data;
                }
            })
        },
        /**
         * 获取昨日风控预警数
         */
        getYesterdayRisk: function () {
            json_ajax('post', '/clbs/adas/lbOrg/show/getYesterdayRisk', 'json', true, '', function (data) {
                $('#yestoday-number').html(data)
            })
        },
        //风险占比
        riskWarnData: function () {
            var params = {
                groupId: '',
                isToday: true,
            };

            json_ajax("POST", "/clbs/adas/lbOrg/show/getRiskProportion", "json", true, params, function (data) {
                if (data.success) {
                    var datas = data.obj;

                    if(isInit){//初始化dom
                        isInit = false;
                        safetyboard.riskWarnDom(datas);
                        riskWarnData = datas;
                    }else{
                        safetyboard.changeRiskWarn(datas);
                    }
                }
            });
        },
        riskWarnDom: function(data){
            var num = 6,
                delay = 0;
            var html = '';

            for(var i=0;i<num;i++){
                var item = data[i];

                var proportion = 0;
                if(item.proportion != '0.00'){
                    proportion = item.proportion=='100.00' ? 100 : parseFloat(item.proportion).toFixed(1)
                }

                delay += 0.4;
                html+='<div class="flip-box box" style="transition-delay: '+ delay +'s">' +
                    '<div class="front">'+
                    '                    <p>\n' +
                    '                        <span class="num">'+ proportion +'</span>\n' +
                    '                        <span class="per">&ensp;%</span>\n' +
                    '                    </p>\n' +
                    '                    <p class="txt">'+ item.name +'</p>\n' +
                    '                </div>'+
                    '<div class="backend">'+
                    '                    <p>\n' +
                    '                        <span class="num"></span>\n' +
                    '                        <span class="per">&ensp;%</span>\n' +
                    '                    </p>\n' +
                    '                    <p class="txt">'+ item.name +'</p>\n' +
                    '                </div>'+
                    '</div>';
            }

            $('#getRiskProportion').html(html);
        },
        changeRiskWarn: function(data){
            var num = 6;

            for(var i=0;i<num;i++){
                var item = data[i];
                var item2 = riskWarnData[i];

                if( item.proportion != item2.proportion){
                    var dom = $('#getRiskProportion .flip-box').eq(i);

                    var proportion = 0;
                    if(item.proportion != '0.00'){
                        proportion = item.proportion=='100.00' ? 100 : parseFloat(item.proportion).toFixed(1)
                    }

                    var isIE = safetyboard.isIEFun();
                    if(isIE){
                        dom.find('.front .num').text(proportion);
                    }else{
                        var direction = dom.hasClass('active') ? '.front' : '.backend';
                        dom.find(direction + ' .num').text(proportion);
                        dom.toggleClass('active');
                    }

                    riskWarnData[i] = item;
                }
            }
        },
        //风险处置情况
        riskDealData: function () {
            var params = {
                groupId: '',
                isToday: true,
            };

            json_ajax('post', '/clbs/adas/lbOrg/show/getRisksDealInfo', 'json', true, params, function (data) {
                if (data.success) {
                    data = data.obj;
                    var html = '';
                    if(data.length > 0){
                        for(var i=0;i<data.length;i++){
                            var item = data[i];
                            html += '<div class="persent-tit">'+ item.name +'</div>\n' +
                                '            <div class="persent-item clearfix">\n' +
                                '                <div class="persent-box">\n' +
                                '                    <span class="persent" style="width:'+ parseFloat(item.proportion).toFixed(2) +'%"></span>\n' +
                                '                </div>\n' +
                                '                <span class="persent-num">'+ item.number +'</span>\n' +
                                '            </div>';
                        }

                        $('#getRisksDealInfo').html(html);
                    }
                }
            })
        },
        //报警事件
        riskEventData: function () {
            var params = {
                groupId: '',
                isToday: true,
            };
            json_ajax("POST", "/clbs/adas/lbOrg/show/getEventRanking", "json", true, params, function (data) {
                if (data.success) {
                    var datas = data.obj;
                    // var romNum = safetyboard.getRndInteger(0, datas.length, 5);
                    var romNum = [1,2,3,4,5];

                    var html = '';
                    var delay = 0 ;

                    riskEvents = datas;
                    riskEventNum = romNum;//保存第一次选中的数字
                    maxValue = safetyboard.getMax(datas);

                    for(var i=0;i<romNum.length;i++){
                        var key = romNum[i],
                            item = datas[key];
                        var persent = maxValue != 0 ? (parseFloat(item.value) / maxValue) * 100 : 0;
                        delay += 0.4;// 动画延迟

                        html += '<div class="flip-item">' +
                            '<div class="flip-box risk-event" style="transition-delay: '+ delay +'s">' +
                            '                <div class="front">\n' +
                            '                    <div class="persent-tit">'+ item.name +'</div>\n' +
                            '                </div>\n' +
                            '                <div class="backend">\n' +
                            '                    <div class="persent-tit"></div>\n' +
                            '                </div>\n' +
                            '            </div>\n' +
                            '            <div class="flip-box risk-event" style="transition-delay: '+ delay +'s">\n' +
                            '                <div class="front">\n' +
                            '                    <div class="persent-item clearfix">\n' +
                            '                        <div class="persent-box">\n' +
                            '                            <span class="persent" style="width:'+persent+'%"></span>\n' +
                            '                        </div>\n' +
                            '                        <span class="persent-num">'+ item.value +'</span>\n' +
                            '                    </div>\n' +
                            '                </div>\n' +
                            '                <div class="backend">\n' +
                            '                    <div class="persent-item clearfix">\n' +
                            '                        <div class="persent-box">\n' +
                            '                            <span class="persent"></span>\n' +
                            '                        </div>\n' +
                            '                        <span class="persent-num"></span>\n' +
                            '                    </div>\n' +
                            '                </div>\n' +
                            '            </div></div>';
                    }

                    $('#riskEvents').html(html);
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
        //改变数据
        changeRiskEvent: function(){
            var len = Math.floor(Math.random() * 5) + 1;// 随机变换个数
            var domArr = safetyboard.getRndInteger(0, 4, len);// 随机变换dom下标
            var eventLen = riskEvents.length - 1;//报警事件数
            var riskArr = safetyboard.getRndInteger(0, eventLen, len, true);//随机取出报警事件

            for(var i=0; i<riskArr.length;i++){
                var key = riskArr[i],
                    item = riskEvents[key];

                var index = domArr[i];
                var dom = $('.flip-item').eq(index).find('.flip-box');

                var persent = maxValue != 0 ? (parseFloat(item.value) / maxValue) * 100 : 0;

                var isIE = safetyboard.isIEFun();
                if(isIE){
                    dom.find('.front .persent-tit').text(item.name);
                    dom.find('.front .persent-num').text(item.value);
                    dom.find('.front .persent').css({
                        'width': persent + '%'
                    });
                }else{
                    var direction = dom.hasClass('active') ? '.front' : '.backend';
                    dom.find(direction + ' .persent-tit').text(item.name);
                    dom.find(direction + ' .persent-num').text(item.value);
                    dom.find(direction + ' .persent').css({
                        'width': persent + '%'
                    });
                    dom.toggleClass('active');
                }

                riskEventNum[index] = key;
            }
        },
        //获取最大值
        getMax: function(datas){
            var max = 0;
            for(var i=0;i<datas.length;i++){
                var item = datas[i];
                var value = parseFloat(item.value);
                if(value > max){
                    max = value;
                }
            }
            return max;
        },
        /**
         * 获取随机数
         * @param min: 取值最小数
         * @param max: 取值最大值
         * @param len: 取出随机数个数
         * @param repeat: 报警事件重复筛选(true:筛选重复, false:不筛选重复)
         * @returns {Array}
         */
        getRndInteger: function(min, max, len, repeat){
            var arr = [];
            while(arr.length < len){
                var num = Math.floor(Math.random() * (max - min + 1) ) + min;

                if(arr.indexOf(num) == -1){
                    if(!repeat){
                        arr.push(num);
                    }else if(riskEventNum.indexOf(num) == -1){
                        arr.push(num);
                    }
                }
            }

            return arr;
        },
        getNowTime: function(){
            var date = new Date();
            var h = date.getHours(),
                m = date.getMinutes();

            h = h < 10 ? '0'+h : h.toString();
            m = m < 10 ? '0'+m : m.toString();
            var item = $('#timer-container .item');
            item.eq(0).text(h.charAt(0));
            item.eq(1).text(h.charAt(1));
            item.eq(2).text(m.charAt(0));
            item.eq(3).text(m.charAt(1));
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
    }

    $(function () {
        safetyboard.initMap();
        safetyboard.initLogo();
        safetyboard.getNowTime();

        safetyboard.getNowRisk();//实际报警数
        safetyboard.getYesterdayRisk();//昨日报警数
        safetyboard.getRingRatioRiskEvent();//环比增长
        safetyboard.riskEventData();//报警事件
        safetyboard.riskDealData();//风险处置情况
        safetyboard.riskWarnData();//风险占比

        //提示语
        $('[data-toggle="tooltip"]').tooltip();

        //当前时间
        timer = setInterval(safetyboard.getNowTime, 60000);
        //实际报警数更新
        timer2 = setInterval(function(){
            safetyboard.getNowRisk();
            safetyboard.riskDealData();
        }, 30000);
        //风险占比更新
        timer3 = setInterval(function () {
            safetyboard.changeRiskEvent();
            safetyboard.riskWarnData();
        }, 5000);
    });
}(window, $))