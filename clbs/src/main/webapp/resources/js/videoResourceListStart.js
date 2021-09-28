/*(function(window,$){*/
Date.prototype.format = function (format) {
    var o =
        {
            "M+": this.getMonth() + 1, //month
            "d+": this.getDate(),    //day
            "h+": this.getHours(),   //hour
            "m+": this.getMinutes(), //minute
            "s+": this.getSeconds(), //second
            "q+": Math.floor((this.getMonth() + 3) / 3),  //quarter
            "S": this.getMilliseconds() //millisecond
        }
    if (/(y+)/.test(format))
        format = format.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
        if (new RegExp("(" + k + ")").test(format))
            format = format.replace(RegExp.$1, RegExp.$1.length == 1 ? o[k] : ("00" + o[k]).substr(("" + o[k]).length));
    return format;
}
var unitMinute = 30,
    halfHourCount = 30 / unitMinute, //每半个小时的长度
    hourCount = 60 / unitMinute; //每一个小时长度
var unitCount = 24 * (60 / unitMinute); //24小时长度（**）
var unitPx = 0; //每个格子的宽度加边框
var timelineInitLeft = 150; //偏移量
var now = new Date();//从服务端读取服务器时间，使客户端和服务器时间同步
var serverTime = now; //静态页面测试直接赋值为客户端时间
var offsetTime = now - serverTime; //时间差
var tabs = 0; //时间轴层次
var fastStatus = 0;//速度
var validTime = MockTime(0);//时间轴起点
var invalidTime = MockTime(24);//时间轴终点
var timeAxisParameter;//时间参数
var multiple = 1;
var channelNum; //红线高度
var action = false; //播放开关
var videoPlayList = new resourceList.maps();//视频参数
var actionM = 0; //播放次数开关
var initWidth = 150; //红线位置偏移量
var $div = $("#timeLine");
var tableWidth = $("#containers").width() + 9; //容器长度
var _scrollLeft = 0; //滑动条长度
var _flogs = true; //菜单开关
var _brand = 0; //菜单容器长度
window['selectedCar'] = '';
window["time"] = []; //视频开始时间
window["flog"] = true;//全局判断是否为第一次播放
window["data"]; //视频参数
window['LoadData'] = LoadData; //视频参数
var tableWidthHour; //每小时长度
var tableWidthMinutes; //每分钟长度
var tableWidthSeconds; //每秒长度
var down = false;
var deviation;//鼠标偏移量
var pluginTimeBodyClickFlag = false;//插件是否点击Flag

$(function () {
    //获取滑动条长度
    $(".playlist-wave-chart").scroll(function () {
        _scrollLeft = $(this).scrollLeft();
    });
    //判断是否展开菜单栏
    $("#toggle-left").on("click", toggleBtn);
    //时间框点击事件
    $("#timeBody").click(function (event) {
        //点击插件时  清空视频播放集合 ANGBIKE
        videoDataList.clear();
        if (event.target.className == "car  color0  ") {
            pluginTimeBodyClickFlag = true;
        }
        //点击就显示红线
        $("#timeLine").show();
        /* 获取当前鼠标的坐标 */
        var mouse_x = event.pageX + _scrollLeft - _brand;
        //更新红线位置
        $div.css({
            left: mouse_x - 311 + "px"
        });
        //移动控件，当等于0的时候，说明是第一次点击播放，获取初始值
        if (actionM == 0) {
            $.playBar.initThead(mouse_x, $('.TheBar'), getInitTime(tabs));
            actionM++;
        }
        var s;//秒
        deviation = (mouse_x - (159 + 302)) + 150;
        if (tabs == 0) { //层级为零的时候，根据鼠标移动位置获取控件长度,159是通道号长度，302是左侧日历栏长度，下面以此类推，长度除以每秒的长度得出时间
            s = parseInt((mouse_x - (159 + 302)) / ((tableWidth - 159 - 1) / (24 * 60 * 60)));
        } else if (tabs == 1) {
            s = parseInt((mouse_x - (159 + 302)) / ((tableWidth - 159) / (60 * 60)));
        } else if (tabs == 2) {
            s = parseInt((mouse_x - (159 + 302)) / ((tableWidth - 159) / 60));
        }
        var day = Math.floor(s / (24 * 3600)); // Math.floor()向下取整
        var hour = Math.floor((s - day * 24 * 3600) / 3600);
        var minute = Math.floor((s - day * 24 * 3600 - hour * 3600) / 60);
        var second = s - day * 24 * 3600 - hour * 3600 - minute * 60;
        setHMS(hour, minute, second, tabs);//将时间传入，显示红框时间
        if (action) { //true的时候为开始状态，进入，变成停止
            $.playBar.Stop();
            $.playBar.Continue(mouse_x - 311, fastStatus);
        } else { //开始移动
            $.playBar.setThead(mouse_x - 311);
        }
        window["flog"] = false; //将点击播放，红线第一次跳动设置为false，红线点击播放不再跳跃
        getTimes();
    });
    /* 绑定鼠标左键按住事件(红线拖动事件) */
    $div.bind("mousedown", function (event) {
        /* 获取需要拖动节点的坐标 */
        var offset_x = $(this)[0].offsetLeft;//x坐标
        /* 获取当前鼠标的坐标 */
        var mouse_x = event.pageX + _scrollLeft - _brand;
        /* 绑定拖动事件 */
        /* 由于拖动时，可能鼠标会移出元素，所以应该使用全局（document）元素 */
        $(document).bind("mousemove", function (ev) {
            ev.preventDefault();
            /* 计算鼠标移动了的位置 */
            var _x = ev.pageX - mouse_x;
            /* 设置移动后的元素坐标 */
            var now_x = (offset_x + _x) + "px";
            /* 改变目标元素的位置 */
            var s;
            var initWidthB = $('#containers').width() + _scrollLeft - _brand; //获取控件宽度
            if ((offset_x + _x) < initWidth) { //如果鼠标移动小于控件宽度，则判定为鼠标超出控件左边，直接将控件设置为0，红线归位,else if反之
                $(this).unbind("mousemove");
                $div.css({
                    left: initWidth + 'px'
                });
                setHMS(0, 0, 0, tabs);
            } else if (initWidthB < (offset_x + _x)) {
                $div.css({
                    left: initWidthB + 'px'
                });
                setHMS(23, 59, 59, tabs);
            } else { //正常移动 , 以下方法请参照点击控制红线方法,完全一样
                $div.css({
                    left: now_x
                });
                deviation = offset_x + _x;
                if (tabs == 0) {
                    s = parseInt((offset_x + _x - 150) / ((tableWidth - 159 - 1) / (24 * 60 * 60)));
                } else if (tabs == 1) {
                    s = parseInt((offset_x + _x - 150) / ((tableWidth - 159) / (60 * 60)));
                } else if (tabs == 2) {
                    s = parseInt((offset_x + _x - 150) / ((tableWidth - 159) / 60));
                }
                var day = Math.floor(s / (24 * 3600)); // Math.floor()向下取整
                var hour = Math.floor((s - day * 24 * 3600) / 3600);
                var minute = Math.floor((s - day * 24 * 3600 - hour * 3600) / 60);
                var second = s - day * 24 * 3600 - hour * 3600 - minute * 60;
                setHMS(hour, minute, second, tabs)
            }
            if (actionM == 0) {
                $.playBar.initThead(mouse_x, $('.TheBar'), getInitTime(tabs));
                actionM++;
            }
            if (action) {
                $.playBar.Continue(offset_x + _x, fastStatus);
            } else { // 停止信息
                $.playBar.setThead(offset_x + _x);
            }
        });
    });


    /* 当鼠标左键松开，接触事件绑定 */
    $("#containers").bind("mouseup", function (event) {
        if (event.target.className == "TimeBall" || event.target.parentNode.className == "time-info" || event.target.className == 'time-info') { //判断鼠标松开，是否在容器内 ,其他方法和点击拖动一样，没必要解释
            var mouse_x = event.pageX + _scrollLeft - _brand;
            var s;
            if (tabs == 0) {
                s = parseInt((deviation - 150) / ((tableWidth - 159 - 1) / (24 * 60 * 60)));
            } else if (tabs == 1) {
                s = parseInt((deviation - 150) / ((tableWidth - 159) / (60 * 60)));
            } else if (tabs == 2) {
                s = parseInt((deviation - 150) / ((tableWidth - 159) / 60));
            }
            var day = Math.floor(s / (24 * 3600)); // Math.floor()向下取整
            var hour = Math.floor((s - day * 24 * 3600) / 3600);
            var minute = Math.floor((s - day * 24 * 3600 - hour * 3600) / 60);
            var second = s - day * 24 * 3600 - hour * 3600 - minute * 60;
            setHMS(hour, minute, second, tabs);
            getTimes();
            event.preventDefault();
        }
        $(document).unbind("mousemove");
    });
    TimeAxis();
});

//重置点击事件,所有参数归零，感觉也没必要解释
function playListVideoStop() {
    fastStatus = 0;
    action = false;
    if (action) {
        $.playBar.fast(1);
    } else {
        $.playBar.fast(1);
        $.playBar.Stop();
    }
    zero();
};

//快进点击事件,根据data参数将速度传入时间控件中，也没必要解释
function playListVideoGoing() {
    var that = $("#playListVideoGoing");
    ;
    var dataJ = that.attr('data');
    var dataH = $('#playListVideoBack').attr('data');
    if (action) {
        if (dataH == 0) {
            if (dataJ == 0) {
                that.attr('data', 1);
                $.playBar.fast(2);
                fastStatus = 1;
            }
            if (dataJ == 1) {
                that.attr('data', 2);
                $.playBar.fast(4);
                fastStatus = 2;
            }
            if (dataJ == 2) {
                that.attr('data', 3);
                $.playBar.fast(8);
                fastStatus = 3;
            }
            if (dataJ == 3) {
                that.attr('data', 4);
                $.playBar.fast(16);
                fastStatus = 4;
            }
        } else {
            if (dataH == -4) {
                $('#playListVideoBack').attr('data', -3);
                $.playBar.retreat(1 / 8);
                fastStatus = -3;
            }
            if (dataH == -3) {
                $('#playListVideoBack').attr('data', -2);
                $.playBar.retreat(1 / 4);
                fastStatus = -2;
            }
            if (dataH == -2) {
                $('#playListVideoBack').attr('data', -1);
                $.playBar.retreat(1 / 2);
                fastStatus = -1;
            }
            if (dataH == -1) {
                $('#playListVideoBack').attr('data', 0);
                $.playBar.retreat(1);
                fastStatus = 0;
            }
        }
    } else {
        if (dataH == 0) {
            if (dataJ == 0) {
                that.attr('data', 1);
                fastStatus = 1;
            }
            if (dataJ == 1) {
                that.attr('data', 2);
                fastStatus = 2;
            }
            if (dataJ == 2) {
                that.attr('data', 3);
                fastStatus = 3;
            }
            if (dataJ == 3) {
                that.attr('data', 4);
                fastStatus = 4;
            }
        } else {
            if (dataH == -4) {
                $('#playListVideoBack').attr('data', -3);
                fastStatus = -3;
            }
            if (dataH == -3) {
                $('#playListVideoBack').attr('data', -2);
                fastStatus = -2;
            }
            if (dataH == -2) {
                $('#playListVideoBack').attr('data', -1);
                fastStatus = -1;
            }
            if (dataH == -1) {
                $('#playListVideoBack').attr('data', 0);
                fastStatus = 0;
            }
        }
    }
};

//快退点击事件
function playListVideoBack() {
    var that = $("#playListVideoBack");
    ;
    var dataH = that.attr('data');
    var dataJ = $('#playListVideoGoing').attr('data');
    if (action) {

        if (dataJ == 0) {

            if (dataH == 0) {
                that.attr('data', -1);
                $.playBar.retreat(1 / 2);
                fastStatus = -1;
            }
            if (dataH == -1) {
                that.attr('data', -2);
                $.playBar.retreat(1 / 4);
                fastStatus = -2;
            }
            if (dataH == -2) {
                $('#playListVideoBack').attr('data', -3);
                $.playBar.retreat(1 / 8);
                fastStatus = -3;
            }
            if (dataH == -3) {
                $('#playListVideoBack').attr('data', -4);
                $.playBar.retreat(1 / 16);
                fastStatus = -4;
            }
        } else {

            if (dataJ == 4) {
                $('#playListVideoGoing').attr('data', 3);
                $.playBar.fast(8);
                fastStatus = 3;
            }
            if (dataJ == 3) {
                $('#playListVideoGoing').attr('data', 2);
                $.playBar.fast(4);
                fastStatus = 2;
            }
            if (dataJ == 2) {
                $('#playListVideoGoing').attr('data', 1);
                $.playBar.fast(2);
                fastStatus = 1;
            }
            if (dataJ == 1) {
                $('#playListVideoGoing').attr('data', 0);
                $.playBar.fast(1);
                fastStatus = 0;
            }
        }
    } else {
        if (dataJ == 0) {
            if (dataH == 0) {
                that.attr('data', -1);
                fastStatus = -1;
            }
            if (dataH == -1) {
                that.attr('data', -2);
                fastStatus = -2;
            }
            if (dataH == -2) {
                $('#playListVideoBack').attr('data', -3);
                fastStatus = -3;
            }
            if (dataH == -3) {
                $('#playListVideoBack').attr('data', -4);
                fastStatus = -4;
            }
        } else {

            if (dataJ == 4) {
                $('#playListVideoGoing').attr('data', 3);
                fastStatus = 3;
            }
            if (dataJ == 3) {
                $('#playListVideoGoing').attr('data', 2);
                fastStatus = 2;
            }
            if (dataJ == 2) {
                $('#playListVideoGoing').attr('data', 1);
                fastStatus = 1;
            }
            if (dataJ == 1) {
                $('#playListVideoGoing').attr('data', 0);
                fastStatus = 0;
            }
        }
    }

};

//重置参数归零
function zero() {
    unitMinute = 30,
        halfHourCount = 30 / unitMinute, //每半个小时的长度
        hourCount = 60 / unitMinute; //每一个小时长度
    unitCount = 24 * (60 / unitMinute); //24小时长度（**）
    unitPx = 0; //每个格子的宽度加边框
    timelineInitLeft = 150;
    headerHeight = 30;
    now = new Date();//从服务端读取服务器时间，使客户端和服务器时间同步
    serverTime = now; //静态页面测试直接赋值为客户端时间
    offsetTime = now - serverTime;
    validTime = MockTime(0);//（**）
    invalidTime = MockTime(24);//(**)
    fastStatus = 0;//速度
    tabs = 0;
    timeAxisParameter;//时间参数
    multiple = 1;
    channelNum;
    action = false;
    //videoPlayList = new resourceList.maps();//视频参数
    actionM = 0;
    initWidth = 150;
    _scrollLeft = 0;
    _flogs = true;
    _brand = 0;
    window["flog"] = true;//全局判断是否为第一次播放
    down = false;
    $("#timeOutF").text("");
    $("#timeOutS").text("");
    $("#timeLine").css({

        left: initWidth + 'px'
    });
    setHMS(0, 0, 0, tabs);
}

/**
 * 将当前红线位置的开始时间,结束时间,通道号传给视频端
 * @author wangjianyu
 */
function getTimes() {
    var timeLists = [];
    var stations = $("div.station[data-key]"); //获取通道所有参数
    for (var i = 0; i < stations.length; i++) { //循环所有参数
        var stationId = $(stations[i]).attr('data-key'); //根据循环参数获取id
        var carList = window["data"][stationId]; //获取单个通道，每个时间节点
        if (typeof carList == 'undefined') continue; //如果单个通道节点为空，直接pass
        for (var j = 0; j < carList.length; j++) { //循环单个通道每个时间节点，然后根据每个时间和当前时间进行匹配，时间相同，说明已经到达视频开始时间点，将值传出
            var carData = carList[j];
            var startTimes = carData.StartTime;
            startTimes = startTimes.replace(/-/g, '/');
            var endTime = carData.EndTime;
            endTime = endTime.replace(/-/g, '/');
            var _date = startTimes.substring(0, 10);
            var _start = (new Date(startTimes).getHours() * 3600) + (new Date(startTimes).getMinutes() * 60) + new Date(startTimes).getSeconds();
            var _end = (new Date(endTime).getHours() * 3600) + (new Date(endTime).getMinutes() * 60) + new Date(endTime).getSeconds();
            var _current = (parseInt($("#h").html().substring(0, 2)) * 3600) + (parseInt($("#m").html().substring(0, 2)) * 60) + parseInt($("#s").html());
            if (_start <= _current && _end > _current) {
                var _dateStart = (_date + " " + $("#h").html().substring(0, 2) + ":" + $("#m").html().substring(0, 2) + ":" + $("#s").html()).replace(/-/g, '/');
                _dateStart = new Date(_dateStart).getTime() / 1000;
                var _dateEnd = endTime.replace(/-/g, '/');
                _dateEnd = new Date(_dateEnd).getTime() / 1000;
                if (_checkChannelList.get("subChk_" + carData.StationId) == "true") { //判断通道是否显示，是则存入
                    var timeList = [_dateStart, _dateEnd, carData.StationId, false];
                    timeLists.push(timeList);
                    break;
                }
            }
        }
    }
    //判断视频播放后暂停状态下 点击其他时间点数据后  重新点击播放按钮逻辑
    if ($("#playListVideoPlay").hasClass("video-play")) {
        if (timeLists.length > 0) {
            ftpVideoPlayStopFlag = false;
            pluginClickSendPlayEntrance = true;
        }
    }
    resourceList.getThisChannelVideoResourceFile(timeLists); //不管值是否有，都将值传出
}

/**
 * 通过传入值，获取时间,这个感觉也没啥好解释的，唯一要说的就是，1层和2层，都是60秒所以设置的是相同的,要说为撒不删掉，为了规范
 * @param hour(每个层级的时间)
 * @author wangjianyu
 */
function MockTime(hour) {
    var now = new Date();
    var time;
    if (tabs == 0) {
        if (hour == 24) {
            time = new Date(now.getFullYear() + '/' + (now.getMonth() + 1) + '/' + now.getDate() + ' ' + '23:59:59');
        } else {
            time = new Date(now.getFullYear() + '/' + (now.getMonth() + 1) + '/' + now.getDate() + ' ' + hour + ':00:00');
        }

    } else if (tabs == 1) {
        time = new Date(now.getFullYear() + '/' + (now.getMonth() + 1) + '/' + now.getDate() + ' 00:' + hour + ':00');
    } else if (tabs == 2) {
        time = new Date(now.getFullYear() + '/' + (now.getMonth() + 1) + '/' + now.getDate() + ' 00:' + hour + ':00');
    }
    var getTime = time.getTime();
    return getTime;
}

/**
 * 通过层级获取毫秒数
 * @param tabs(层级)
 * @author wangjianyu
 */
function getInitTime(tabs) {
    var timees = 0;
    if (tabs == 0) {
        timees = 1000 * 60 * 60 * 24;
    } else if (tabs == 1) {
        timees = 1000 * 60 * 60;
    } else if (tabs == 2) {
        timees = 1000 * 60;
    }
    return timees;
}

/**
 * 通过时间获取红框显示时间
 * @param hour(时)
 * @param minute(分)
 * @param second(秒)
 * @param tabs(层级)
 * @author wangjianyu
 */
function setHMS(hour, minute, second, tabs) {
    if (tabs == 0) {
        $("#h").html((hour < 10 ? ("0" + hour) : hour) + ":");
        $("#m").html((minute < 10 ? ("0" + minute) : minute) + ":");
        $("#s").html(second < 10 ? ("0" + second) : second);
    } else if (tabs == 1) {
        $("#h").html(($("#timeOutS").html() < 10 ? ("0" + $("#timeOutS").html()) : $("#timeOutS").html()) + ":");
        $("#m").html((minute < 10 ? ("0" + minute) : minute) + ":");
        $("#s").html(second < 10 ? ("0" + second) : second);
    } else if (tabs == 2) {
        $("#h").html(($("#timeOutS").html() < 10 ? ("0" + $("#timeOutS").html()) : $("#timeOutS").html()) + ":");
        $("#m").html(($("#timeOutF").html() < 10 ? ("0" + $("#timeOutF").html()) : $("#timeOutF").html()) + ":");
        $("#s").html(second < 10 ? ("0" + second) : second);
    }
}

/**
 * 通过状态获取速度分值
 * @author wangjianyu
 */
function begin() {
    if (fastStatus == 0) {
        $.playBar.Begin();
    }
    if (fastStatus == 1) {
        $.playBar.fast(2);
    }
    if (fastStatus == 2) {
        $.playBar.fast(4);
    }
    if (fastStatus == 3) {
        $.playBar.fast(8);
    }
    if (fastStatus == 4) {
        $.playBar.fast(16);
    }
    if (fastStatus == -1) {
        $.playBar.fast(1 / 2);
    }
    if (fastStatus == -2) {
        $.playBar.fast(1 / 4);
    }
    if (fastStatus == -3) {
        $.playBar.fast(1 / 8);
    }
    if (fastStatus == -4) {
        $.playBar.fast(1 / 16);
    }
}

/**
 * 菜单栏显示隐藏开关，并获取其长度
 * @author wangjianyu
 */
function toggleBtn() {
    if (_flogs) {
        _brand = $("#header .brand").width() + 11;
        _flogs = false;
    } else {
        _brand = 0;
        _flogs = true;
    }
}

/**
 * 过滤当通道隐藏之后，第一次跳动位置，会过滤掉隐藏的通道
 * @author wangjianyu
 */
function startTime() {
    window["time"] = [];
    var _checkKey = [];
    var _keys = _checkChannelList.keys();
    if (_keys.length > 0) {
        for (var i = 0; i < _keys.length; i++) {
            var _values = _checkChannelList.get(_keys[i]);
            if (_values == "true") {
                _checkKey.push(_keys[i]);
            }
        }
    }

    var _key = videoPlayList.keys();//获取所有通道
    if (_key.length > 0) {
        for (var i = 0; i < _key.length; i++) { //循环遍历所有视频，并组装参数
            for (var z = 0; z < _checkKey.length; z++) {
                if (_key[i] == _checkKey[z].substring(7, 8)) {
                    var _value = videoPlayList.get(_key[i]);
                    for (var j = 0; j < _value.length; j++) {
                        // var _StartTime = getMyDate(_value[j][0] * 1000);
                        // window["time"].push(new Date(_StartTime));
                        var _StartTime = _value[j][0] * 1000;
                        window["time"].push(_StartTime);
                    }
                }
            }
        }
    }
}

/**
 * 播放
 * @author wangjianyu
 */
function videoPlay() {
    if (deviceQueryType_start == 1) { //参数等于1的时候，说明有值，正常播放
        // 得到顶上刻度 写成动参
        if (action) { //同上，不解释
            $.playBar.Stop();
            action = false;
        } else {
            var goSpeed = $("#playListVideoGoing").attr('data'); //获取前进当前移动速度;
            var backSpeed = $("#playListVideoBack").attr('data'); //获取后退当前移动速度;
            if (goSpeed != "0") {
                fastStatus = goSpeed;
            } else {
                fastStatus = backSpeed;
            }
            if (window["flog"]) { //如果为true的时候，说明是第一次点击播放，指针要跳到第一个有视频的位置
                startTime();
                var startTimes = Math.min.apply(null, window["time"]); //获取最小的开始时间
                startTimes = getMyDate(startTimes);
                startTimes = startTimes.replace(/-/g, '/');
                //根据开始时间计算红线要移动的长度
                var leftJoin = ((new Date(startTimes).getHours() * 3600) + (new Date(startTimes).getMinutes() * 60) + new Date(startTimes).getSeconds()) * ((tableWidth - 159) / (24 * 60 * 60));
                //进都进来了，肯定要把开关关了撒
                window["flog"] = false;
                //判断是否是第一次点击播放
                if (actionM == 0) {
                    $.playBar.initThead(leftJoin + 159, $('.TheBar'), getInitTime(tabs));
                    actionM++;
                }
                //暂停，不解释
                if (action) {
                    $.playBar.Stop();
                    $.playBar.Continue(leftJoin + 159, fastStatus);
                    //跟前面点击，拖动逻辑一样，不解释
                } else {
                    var s;
                    if (tabs == 0) {
                        s = parseInt((leftJoin) / ((tableWidth - 159) / (24 * 60 * 60)));
                    } else if (tabs == 1) {
                        s = parseInt((leftJoin) / ((tableWidth - 159) / (60 * 60)));
                    } else if (tabs == 2) {
                        s = parseInt((leftJoin) / ((tableWidth - 159) / (60 * 60)));
                    }
                    var day = Math.floor(s / (24 * 3600)); // Math.floor()向下取整
                    var hour = Math.floor((s - day * 24 * 3600) / 3600);
                    var minute = Math.floor((s - day * 24 * 3600 - hour * 3600) / 60);
                    var second = s - day * 24 * 3600 - hour * 3600 - minute * 60;
                    setHMS(hour, minute, second, tabs);
                    //这里要解释一下，没办法，用了很多时间来算这个偏移量，js是有误差的，通过计算时间的偏移量，来调整红线移动长度，2.5是可变值
                    $.playBar.setThead(leftJoin + initWidth - ((tableWidth - 159) / (24 * 60 * 60)) * ((hour + minute / 60) * 2.5));
                }
            }
            //同上，不解释
            if (actionM == 0) {
                //动参
                $.playBar.addBar($('.TheBar'), getInitTime(tabs), fastStatus);//第一个参数是需要显示播放器的容器，第二个参数为时间，单位毫秒
            } else {
                begin();
            }
            actionM++;
            //播放暂停开关
            action = true;
        }
    } else {
        layer.msg("亲,请先获取数据哦！")
    }
}

/**
 * 终端播放跳跃
 * @author wangjianyu
 */
function terminalJump() {
    window["flog"] = false;
    startTime();
    var startTimes = Math.min.apply(null, window["time"]); //获取最小的开始时间
    startTimes = getMyDate(startTimes);
    startTimes = startTimes.replace(/-/g, '/');
    var leftJoin = ((new Date(startTimes).getHours() * 3600) + (new Date(startTimes).getMinutes() * 60) + new Date(startTimes).getSeconds()) * ((tableWidth - 159) / (24 * 60 * 60));
    var s;
    if (tabs == 0) {
        s = parseInt((leftJoin) / ((tableWidth - 159) / (24 * 60 * 60)));
    } else if (tabs == 1) {
        s = parseInt((leftJoin) / ((tableWidth - 159) / (60 * 60)));
    } else if (tabs == 2) {
        s = parseInt((leftJoin) / ((tableWidth - 159) / (60 * 60)));
    }

    var day = Math.floor(s / (24 * 3600)); // Math.floor()向下取整
    var hour = Math.floor((s - day * 24 * 3600) / 3600);
    var minute = Math.floor((s - day * 24 * 3600 - hour * 3600) / 60);
    var second = s - day * 24 * 3600 - hour * 3600 - minute * 60;

    var hms = (startTimes.split(' ')[1]).split(':');
    var hour_html = Number(hms[0]) > 9 ? Number(hms[0]) : Number(hms[0][1]);
    var minute_html = Number(hms[1]) > 9 ? Number(hms[1]) : Number(hms[1][1]);
    var second_html = Number(hms[2]) > 9 ? Number(hms[2]) : Number(hms[2][1]);

    setHMS(hour_html, minute_html, second_html, tabs);
    $div.css({
        left: (leftJoin + initWidth - ((tableWidth - 159) / (24 * 60 * 60)) * ((hour + minute / 60) * 2.5)) + "px"
    });
    if (actionM == 0) {
        $.playBar.initThead((leftJoin + initWidth - ((tableWidth - 159) / (24 * 60 * 60)) * ((hour + minute / 60) * 2.5)), $('.TheBar'), getInitTime(tabs));
        actionM++;
    }
    if (action) { //true的时候为开始状态，进入，变成停止
        $.playBar.Stop();
        $.playBar.Continue(leftJoin + 150, fastStatus);
    } else { //开始移动
        $.playBar.setThead(leftJoin + initWidth - ((tableWidth - 159) / (24 * 60 * 60)) * ((hour + minute / 60) * 2.5));
    }
    // $.playBar.setThead(leftJoin+ initWidth -((tableWidth - 159) / (24 * 60 * 60))*((hour+minute/60)*2.5));
    // $.playBar.Continue(leftJoin+ initWidth -((tableWidth - 159) / (24 * 60 * 60))*((hour+minute/60)*2.5));

}

/**
 * 时间轴显示方法
 * @author wangjianyu
 */
function TimeAxis() {
    var headerWidth = $("#containers").width() - $("#stationContainer").width();//动态获取时间轴参数
    if (tabs == 0) { //计算每一个刻度的长度，秒这边有2PX的偏移量，不要问我啷个来的，我也是试了N多次试出来的
        unitPx = headerWidth / ((invalidTime - validTime) / 60000 / unitMinute);
    } else {
        unitPx = headerWidth / (((invalidTime - validTime) / 1000 / unitMinute) + 2);//(**)
    }
    $("#timeHeader").html(''); //先将刻度线制空
    for (var i = 0; i < unitCount; i++) { //循环添加时间轴时间点
        var classstr = " ";
        var hourstr = "";
        if (i % hourCount == 0) { //这个是添加中间刻度的
            classstr = "hour";
            hourstr = i / hourCount;
        }
        else if (i % halfHourCount == 0) //这个是添加大刻度的
            classstr = "halfHour";
        var i_unitPx = Number(unitPx) * i
        var unitTime = "<div id='time_" + i + "' style='left: " + i_unitPx + "px' class='unitTime " + classstr + "'>" + hourstr + "</div>";

        // unitTime.css('left', i * unitPx);
        $("#timeHeader").append(unitTime);
        /*var a="time_"+i
        $("#"+a).css('left', i * unitPx);*/
        $("#time_" + i).bind("mouseover", function (e) { //给每个刻度绑定点击事件，方便滚动的时候获取当前时间
            timeAxisParameter = e.currentTarget.id.substring(5, e.currentTarget.id.length);
            if (timeAxisParameter != 0) {
                if (timeAxisParameter % 2 == 0) {
                    timeAxisParameter = timeAxisParameter / 2
                } else {
                    timeAxisParameter = (timeAxisParameter - 1) / 2
                }
            }
        })
    }
    $("#scrollContainer").width(unitCount * unitPx);
    $("#timeContainer").width($("#containers").width() - timelineInitLeft);
    SetTimeNow();
}

/**
 * 设置时间
 * @author wangjianyu
 */
function SetTimeNow() {
    serverTime.setTime(new Date().getTime() + offsetTime);
    SetTimeLineByTime(serverTime.getHours(), serverTime.getMinutes());
}

/**
 * 设置红线长度
 * @author wangjianyu
 */
function SetTimeLineByTime() {
    if (tabs == 0) {
        $("#timeLine").show();
    }
    $("#timeLine").css("left", $("#scrollContainer").position().left + $("#stationContainer").width());
}

/**
 * 设置绿条长度
 * @param car(绿条ID)
 * @param count(长度)
 * @author wangjianyu
 */
function SetCarPositionByTime(car, count) {
    $(car).css('left', count);
    SetHeight($(car));
}

/**
 * 初始红线方法
 * @author wangjianyu
 */
function AutoScroll() {
    try {
        serverTime.setTime(new Date().getTime() + offsetTime);
        SetTimeLineByTime(serverTime.getHours(), serverTime.getMinutes());
    } catch (e) {
    }
}

/**
 * 滚动时间轴切换层级方法
 * @author wangjianyu
 */
$(document).on("mousewheel DOMMouseScroll", function (event) {
    var event = arguments.callee.caller.arguments[0] || window.event; //浏览器兼容
    $("#timeHeader").off('mousewheel DOMMouseScroll').on("mousewheel DOMMouseScroll", function (event) {
        var delta = (event.originalEvent.wheelDelta && (event.originalEvent.wheelDelta > 0 ? 1 : -1)) ||  // chrome & ie
            (event.originalEvent.detail && (event.originalEvent.detail > 0 ? -1 : 1));// firefox
        var _h = parseInt($("#h").text().substring(0, 2));
        var _m = parseInt($("#m").text().substring(0, 2));
        var _s = parseInt($("#s").text().substring(0, 2));
        var count = ((_m * 60) + _s) * ((tableWidth - 159) / (60 * 60));//开始的像素 (分)
        var counts = _s * ((tableWidth - 159) / 60); //秒
        var counth = ((_h * 3600) + (_m * 60) + _s) * ((tableWidth - 159) / (60 * 60 * 24)); //时
        if (delta > 0) {
            // 向上滚
            if (tabs < 2 && tabs >= 0) {
                if (tabs == 0) { //判断层级
                    $("#timeOutS").text(timeAxisParameter); //为隐藏时赋值
                    $.playBar.initThead(count + 150, $('.TheBar'), getInitTime(tabs + 1)); //调用移动方法
                    //判断红线的位置
                    var _h = parseInt($("#h").text().substring(0, 2));
                    if (timeAxisParameter != _h) {
                        $("#timeLine").hide();//红线时间和滑动时间不一致,隐藏
                    }
                } else if (tabs == 1) {
                    $("#timeOutF").text(timeAxisParameter);
                    $.playBar.initThead(counts + 150, $('.TheBar'), getInitTime(tabs + 1));
                    //判断红线的位置
                    var _m = parseInt($("#m").text().substring(0, 2));
                    if (timeAxisParameter != _m) {
                        $("#timeLine").hide();//红线时间和滑动时间不一致,隐藏
                    }
                }
                tabs += 1;
                if (tabs != 0) {
                    unitCount = 60 * (60 / unitMinute);
                    invalidTime = MockTime(59);//(**)
                }
                TimeAxis();
                MockData();
                if (tabs == 1) {
                    if (!action) {
                        $("#timeLine").css({
                            left: (count + 150) + "px"
                        });
                    }
                    $("#stationContainer .time-dimension").text("分钟(" + $("#timeOutS").text() + " - " + (parseInt($("#timeOutS").text()) + 1) + "时)");
                } else if (tabs == 2) {
                    if (!action) {
                        $("#timeLine").css({
                            left: (counts + 150) + "px"
                        });
                    }
                    $("#stationContainer .time-dimension").text("秒(" + $("#timeOutS").text() + ":" + $("#timeOutF").text() + " - " + $("#timeOutS").text() + ":" + (parseInt($("#timeOutF").text()) + 1) + "分)");
                }
            }
        } else if (delta < 0) { //后面逻辑一样，方法一样，没必要写注释了
            // 向下滚
            if (tabs > 0 && tabs < 3) {
                if (tabs == 2) {
                    $.playBar.initThead(count + 150, $('.TheBar'), getInitTime(1));
                    //判断红线的位置
                    var _h = parseInt($("#h").text().substring(0, 2));
                    var timeOutS = $("#timeOutS").text();
                    if (timeOutS != _h) {
                        $("#timeLine").hide();//红线时间和滑动时间不一致,隐藏
                    } else {
                        $("#timeLine").show();
                    }
                } else if (tabs == 1) {
                    $.playBar.initThead(counth + 150, $('.TheBar'), getInitTime(0));
                    $("#timeLine").show();
                }
                tabs -= 1;
                if (tabs == 0) {
                    $("#timeHeader .unitTime").css("width", "55px");
                    unitCount = 24 * (60 / unitMinute);
                    invalidTime = MockTime(24);//(**)
                }
                TimeAxis();
                MockData();
                if (tabs == 1) {
                    if (!action) {
                        $("#timeLine").css({
                            left: (count + 150) + "px"
                        });
                    }
                    $("#stationContainer .time-dimension").text("分钟(" + $("#timeOutS").text() + " - " + (parseInt($("#timeOutS").text()) + 1) + "时)");
                } else if (tabs == 0) {
                    if (!action) {
                        $("#timeLine").css({
                            left: (counth + 150) + "px"
                        });
                    }
                    $("#stationContainer .time-dimension").text("小时");
                }
            }
        }
        //通知浏览器不要执行与事件关联的默认动作
        event.preventDefault();
    });
});

function reset() {
    $.playBar.initThead(150, $('.TheBar'), getInitTime(0));
    $("#timeLine").show();
    $("#timeHeader .unitTime").css("width", "65px");
    unitCount = 24 * (60 / 30);
    tabs = 0;
    invalidTime = MockTime(24);//(**)
    validTime = MockTime(0);//时间轴起点
    TimeAxis();
    MockData();
    $("#timeLine").css({
        left: 150 + "px"
    });

}

$(function () {
    tableWidthHour = (tableWidth - 159) / (24 * 60 * 60);//每秒的像素宽度
    tableWidthMinutes = (tableWidth - 159) / (60 * 60);
    tableWidthSeconds = (tableWidth - 159) / 60;
});

/**
 * 绿条显示方法
 * @param carData(单个绿条属性)
 * @author wangjianyu
 */
function Car(carData) {
    var id = carData.ID; //每个视频ID
    var text = carData.DisplayText; //视频提示文档，暂时没用到，以后可能要用
    var stationId = carData.StationId; //通道id
    var startTime = carData.StartTime; //视频开始时间
    startTime = startTime.replace(/-/g, '/');
    var endTime = carData.EndTime; //视频结束时间
    endTime = endTime.replace(/-/g, '/');
    var isVirtual = carData.IsVirtual; //忘了是干撒子的了
    var colorClass = "color" + 0; //视频颜色

    var hours = new Date(startTime).getHours();
    var minutes = new Date(startTime).getMinutes();
    var seconds = new Date(startTime).getSeconds();
    var hoursEnd = new Date(endTime).getHours();
    var minutesEnd = new Date(endTime).getMinutes();
    var secondsEnd = new Date(endTime).getSeconds();
    if (!$("#car_" + id).length > 0) { //根据id判断div是否存在，存在则大于0
        $("#car_" + id).remove();//防止重复，div删除
    }
    //生成一个视频条
    var car = $("<div id='car_" + id + "' stationId='" + stationId + "' class=\"car  " + colorClass + "  " + (isVirtual ? 'virtual' : '') + "\"><div>" + text + "</div></div>");
    /***计算时间差像素***/
    var count = 0;
    var countEnd = 0;
    if (tabs == 0) {
        count = ((hours * 3600) + (minutes * 60) + seconds) * tableWidthHour;//开始的像素
        countEnd = ((hoursEnd * 3600) + (minutesEnd * 60) + secondsEnd) * tableWidthHour;//结束的像素
    } else if (tabs == 1) {
        if ($("#timeOutS").text() >= hours && $("#timeOutS").text() <= hoursEnd) {
            if ($("#timeOutS").text() == hours && $("#timeOutS").text() != hoursEnd) {
                count = ((minutes * 60) + seconds) * tableWidthMinutes;//开始的像素
                countEnd = $("#timeBody").width();
            } else if ($("#timeOutS").text() == hours && $("#timeOutS").text() == hoursEnd) {
                count = ((minutes * 60) + seconds) * tableWidthMinutes;//开始的像素
                countEnd = ((minutesEnd * 60) + secondsEnd) * tableWidthMinutes;
            } else if ($("#timeOutS").text() != hours && $("#timeOutS").text() == hoursEnd) {
                count = 0;//开始的像素
                countEnd = ((minutesEnd * 60) + secondsEnd) * tableWidthMinutes;
            } else if ($("#timeOutS").text() != hours && $("#timeOutS").text() != hoursEnd) {
                count = 0;//开始的像素
                countEnd = $("#timeBody").width();
            }
        }
    } else if (tabs == 2) {
        if ($("#timeOutS").text() >= hours && $("#timeOutS").text() <= hoursEnd) {
            if ($("#timeOutF").text() >= minutes && $("#timeOutF").text() <= minutesEnd) {
                if ($("#timeOutF").text() == minutes && $("#timeOutF").text() != minutesEnd) {
                    count = seconds * tableWidthSeconds;//开始的像素
                    countEnd = $("#timeBody").width();
                    ;
                } else if ($("#timeOutF").text() == minutes && $("#timeOutF").text() == minutesEnd) {
                    count = seconds * tableWidthSeconds;//开始的像素
                    countEnd = secondsEnd * tableWidthSeconds;//结束的像素
                } else if ($("#timeOutF").text() != minutes && $("#timeOutF").text() == minutesEnd) {
                    count = 0;//开始的像素
                    countEnd = secondsEnd * tableWidthSeconds;//结束的像素
                } else if ($("#timeOutF").text() != minutes && $("#timeOutF").text() != minutesEnd) {
                    count = 0;//开始的像素
                    countEnd = $("#timeBody").width();//结束的像素
                }
            }
        }
    }
    //视频条长度
    car.width(countEnd - count);
    //显示视频条
    Car.prototype.Show = function () {
        $("#station_" + stationId).append(car);
        SetCarPositionByTime(car, count);
    }
}

/**
 * 设置高
 * @param dstCar(单个绿条属性)
 * @author wangjianyu
 */
function SetHeight(dstCar) {
    var cars = $("a[stationid='" + dstCar.attr("stationId") + "']");
    for (var i = 0; i < cars.length; i++) {
        var car = $(cars[i]);
        if (car.attr('id') != dstCar.attr('id') && (car.position().left + car.width()) > dstCar.position().left + 2) {
            dstCar.css('top', car.position().top + car.height() + 4);
        }
    }
}

/**
 * 根据ID添加通道个数
 * @param dstCar(绿条通道ID)
 * @author wangjianyu
 */
function Station(stationId) {
    var station = $("<div id='station_" + stationId + "' class=\"horizontalStation\"></div>");
    station.css("height", "30px");
    Station.prototype.Show = function () {
        $("#timeBody").append(station);
    }
}

function LoadData() {
    return MockData();
}

/**
 * 整理获取所有通道属性
 * @author wangjianyu
 */
function MockData() {
    var _key = videoPlayList.keys();//获取所有通道
    var stations = $("div.station[data-key]");
    var data = new Object();
    var _index = 1;
    window["time"] = []; //视频开始时间集合
    if (_key.length > 0) {
        for (var i = 0; i < _key.length; i++) { //循环遍历所有视频，并组装参数
            for (var z = 0; z < channelNum.length; z++) {
                if (channelNum[z] == _key[i]) {
                    var _value = videoPlayList.get(_key[i]);
                    var _dataList = [];
                    for (var j = 0; j < _value.length; j++) {
                        var _StartTime = getMyDate(_value[j][0] * 1000);
                        var _EndTime = getMyDate(_value[j][1] * 1000)
                        var _StationId = _key[i];
                        var _dataJson = {};
                        _dataJson.ID = _index;
                        _dataJson.DisplayText = "";
                        _dataJson.StationId = _StationId;
                        _dataJson.StartTime = _StartTime;
                        _dataJson.EndTime = _EndTime;
                        _dataList.push(_dataJson);
                        _index++;
                    }
                    data[z + 1] = _dataList;
                }
            }
        }
    }
    window["data"] = data;
    $("#timeBody").html(''); //制空插件视频组
    for (var i = 0; i < stations.length; i++) { //循环显示视频
        var stationId = $(stations[i]).attr('data-key');
        var station = new Station(stationId);
        station.Show();
        var carList = data[stationId];
        if (typeof carList == 'undefined') {
            if (_checkChannelList.get("subChk_" + (i + 1)) == "false") {
                $("#station_" + (i + 1)).hide();
            }
            continue;
        }
        for (var j = 0; j < carList.length; j++) {
            var carData = carList[j];
            var car = new Car(carData);
            car.Show();
        }
        if (_checkChannelList.get("subChk_" + (i + 1)) == "false") {
            $("#station_" + (i + 1)).hide();
        }
    }
}

/**
 * 初始化方法
 * @param _channelData(通道个数)
 * @param videoPlayLists(通道属性)
 * @author wangjianyu
 */
function Loop(_channelData, videoPlayLists) {
    videoPlayList = videoPlayLists;
    channelNum = _channelData;
    $("#timeLine").css("height", channelNum.length * 30 + 30 + "px");
    AutoScroll();
    LoadData();//.complete(function () { setTimeout('Loop()', 20000) });
}

/**
 * 通过时间戳获取时间
 * @param str(时间戳)
 * @author wangjianyu
 */
function getMyDate(str) {
    var oDate = new Date(str),
        oYear = oDate.getFullYear(),
        oMonth = oDate.getMonth() + 1,
        oDay = oDate.getDate(),
        oHour = oDate.getHours(),
        oMin = oDate.getMinutes(),
        oSen = oDate.getSeconds(),
        oTime = oYear + '-' + getzf(oMonth) + '-' + getzf(oDay) + ' ' + getzf(oHour) + ':' + getzf(oMin) + ':' + getzf(oSen);//最后拼接时间
    return oTime;
};

/**
 * 补0操作
 * @param num
 * @author wangjianyu
 */
function getzf(num) {
    if (parseInt(num) < 10) {
        num = '0' + num;
    }
    return num;
}

/**
 * 根据层度设置播放速度
 * @param a(时间戳)
 * @author wangjianyu
 */
function _fast(a) {
    if (a == 0) {
        return 1;
    }
    if (a == 1) {
        return 2;
    }
    if (a == 2) {
        return 4;
    }
    if (a == 3) {
        return 8;
    }
    if (a == 4) {
        return 16;
    }
    if (a == -1) {
        return 1 / 2;
    }
    if (a == -2) {
        return 1 / 4;
    }
    if (a == -3) {
        return 1 / 8;
    }
    if (a == -4) {
        return 1 / 16;
    }
}

/**
 * 播放插件,单独提出
 * @param str(时间戳)
 * @author wangjianyu
 */
(function ($) {
    var isAction = true;
    var width = 0; //插件组宽度
    var thewidth = 0; //距离
    var CurrTime = 0; //这个不管
    var t; //这个也不管
    var alltime = 0; //当前时间
    var addwidth = 0; //每秒的宽度
    var offsetW = 0; //偏移量
    var times = 0; //时间，秒
    var rwidth = 0; //除去偏移量宽度
    var flogTime = 0; //时间开关
    jQuery.playBar = {
        //红线移动位置设置
        addBar: function (DOM, allTime, fastStatus) {
            CleanAll();
            alltime = allTime;

            width = $('.TheBar').width();
            times = allTime / 1000; //120
            rwidth = width - 8;
            addwidth = (width - 1) / times;
            if (fastStatus == 0) {
                OpenBar()
            } else {
                Faster(_fast(fastStatus))
            }
        },
        Stop: function () {
            StopBar()
        },
        Begin: function () {
            OpenBar()
        },
        fast: function (a) {
            Faster(a);
        },
        retreat: function (a) {
            fastRetreater(a);
        },
        Continue: function (a, b) {
            ScrollOpenBar(a, b)
        },
        setThead: function (a) {
            changeTeand(a)
        },
        getTheTime: function () {
            return CurrTime
        },
        initThead: function (a, DOM, allTime) { //这个是暂停后，第二次移动调用方法
            CleanAll();
            alltime = allTime;
            width = $('.TheBar').width();
            times = allTime / 1000;
            rwidth = width - 8;
            addwidth = (width - 1) / times;
            _init(a)
        }
    };

    //参数归零
    function CleanAll() {
        isAction = true;
        CurrTime = 0;
        addHour = 0;
        addMinute = 0;
        addSecond = 0;
        TheHour = 0;
        TheMinute = 0;
        TheSecond = 0;
        offsetW = 0;
        thewidth = 0;
    }

    //移动参数设置,这里的逻辑和上面的一样，请参考上面的逻辑，唯一区别是判断是否时间重复
    function changeBar() {
        if (flogTime != $("#h").html().substring(0, 2) + ":" + $("#m").html().substring(0, 2) + ":" + $("#s").html()) {
            flogTime = $("#h").html().substring(0, 2) + ":" + $("#m").html().substring(0, 2) + ":" + $("#s").html();
            var timeLists = [];
            var stations = $("div.station[data-key]");
            for (var i = 0; i < stations.length; i++) {
                var stationId = $(stations[i]).attr('data-key');
                var carList = window["data"][stationId];
                if (typeof carList == 'undefined') continue;
                for (var j = 0; j < carList.length; j++) {
                    var carData = carList[j];
                    var startTimes = carData.StartTime;
                    startTimes = startTimes.replace(/-/g, '/');
                    var endTime = carData.EndTime;
                    endTime = endTime.replace(/-/g, '/');
                    var hours = new Date(startTimes).getHours();
                    var minutes = new Date(startTimes).getMinutes();
                    var seconds = new Date(startTimes).getSeconds();
                    if (hours == parseInt($("#h").html().substring(0, 2)) && minutes == parseInt($("#m").html().substring(0, 2)) && parseInt($("#s").html()) == seconds) {
                        var _dateStart = startTimes.replace(/-/g, '/');
                        _dateStart = new Date(_dateStart).getTime() / 1000;
                        var _dateEnd = endTime.replace(/-/g, '/');
                        _dateEnd = new Date(_dateEnd).getTime() / 1000;
                        if (_checkChannelList.get("subChk_" + carData.StationId) == "true") {
                            var timeList = [_dateStart, _dateEnd, carData.StationId, true];
                            timeLists.push(timeList);
                            break;
                        }
                    }
                }
            }
            if (timeLists.length > 0) {
                resourceList.getThisChannelVideoResourceFile(timeLists);
            }
        }
        thewidth = thewidth * 1 + addwidth - offsetW;
        var s = parseInt(thewidth / addwidth);
        var day = Math.floor(s / (24 * 3600)); // Math.floor()向下取整
        var hour = Math.floor((s - day * 24 * 3600) / 3600);
        var minute = Math.floor((s - day * 24 * 3600 - hour * 3600) / 60);
        var second = s - day * 24 * 3600 - hour * 3600 - minute * 60;
        setHMS(hour, minute, second, tabs);
        if (offsetW > 0) {
            offsetW = 0
        }
        if (thewidth < 0) thewidth = 0, setHMS(0, 0, 0, tabs);
        var allWidths = $('#containers').width();
        var changWidths = $("#scrollContainer").position().left + $("#stationContainer").width() + thewidth;
        if (changWidths < allWidths - 1 || changWidths == allWidths - 1) {
            $('.TimeBall').css("left", $("#scrollContainer").position().left + $("#stationContainer").width() + thewidth)
        } else {
            action = false;
            StopBar();
            setHMS(23, 59, 59, tabs)
        }
        if (tabs == 1 && $("#m").html().substring(0, 2) == 59 && $("#s").html() == 59 && $("#h").html() != 23) {
            $("#timeLine").css({

                left: initWidth + 'px'
            });
            $("#timeOutS").html((parseInt($("#timeOutS").html()) + 1));
            setHMS((parseInt($("#h").html().substring(0, 2)) + 1), 0, 0, tabs);
            MockData();
            thewidth = 0;
        } else if (tabs == 2 && $("#s").html() == 59 && $("#h").html() != 23) {
            $("#timeLine").css({

                left: initWidth + 'px'
            });
            if ($("#m").html() == "59:") {
                $("#timeOutS").html((parseInt($("#timeOutS").html()) + 1));
                $("#timeOutF").html(0);
                setHMS((parseInt($("#h").html().substring(0, 2)) + 1), 0, 0, tabs);
            } else {
                $("#timeOutF").html((parseInt($("#timeOutF").html()) + 1));
                setHMS($("#h").html().substring(0, 2), (parseInt($("#m").html().substring(0, 2)) + 1), 0, tabs);
            }
            MockData();
            thewidth = 0;
            //$.playBar.initThead(150,$('.TheBar'),getInitTime(tabs+1));
        }
    }

    //快退功能，目前没用到，留着，可能要用
    function fastRetreat() {
        thewidth = thewidth * 1 - addwidth - offsetW;
        var s = parseInt(thewidth / addwidth);
        var day = Math.floor(s / (24 * 3600)); // Math.floor()向下取整
        var hour = Math.floor((s - day * 24 * 3600) / 3600);
        var minute = Math.floor((s - day * 24 * 3600 - hour * 3600) / 60);
        var second = s - day * 24 * 3600 - hour * 3600 - minute * 60;
        setHMS(hour, minute, second, tabs);
        if (offsetW > 0) {
            offsetW = 0
        }
        if (thewidth == 0 || thewidth < 0) {
            $('.TimeBall').css("left", $("#scrollContainer").position().left + $("#stationContainer").width() + thewidth)
        } else {
            action = false;
            StopBar();
            setHMS(0, 0, 0, tabs)
        }
    }

    function StopBar() {
        if (!down) {
            isAction = false
        }
        clearInterval(t)
    }

    function OpenBar() {
        isAction = true;
        t = setInterval(changeBar, 1000 / multiple);
    }

    //暂停控件
    function ScrollOpenBar(a, b) {
        thewidth = a - $("#scrollContainer").position().left - $("#stationContainer").width() - 1
        isAction = true;
        Faster(_fast(b));
    }

    //移动距离控件
    function changeTeand(a) {
        thewidth = a - $("#scrollContainer").position().left - $("#stationContainer").width() - 1;
    }

    function _init(a) {
        thewidth = a - $("#scrollContainer").position().left - $("#stationContainer").width() - 1;
    }

    //移动速度控件
    function Faster(a) {
        clearInterval(t);
        if (oldType != 0) {
            if (a == 8) {
                a = 5.4;
            } else if (a == 16) {
                a = 11;
            }
        }
        t = setInterval(changeBar, 1000 / a);
    }

    function fastRetreater(a) {
        clearInterval(t);
        t = setInterval(changeBar, 1000 / a);
    }
})(jQuery);

/*})(window,$)*/