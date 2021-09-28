/**
 * jquery.calendar.js 1.0
 * http://jquerywidget.com
 */
;(function (factory) {
    if (typeof define === "function" && (define.amd || define.cmd) && !jQuery) {
        // AMD或CMD
        define(["jquery"], function () {
            factory(jQuery);
        });
    } else {
        // 全局模式
        factory(jQuery);
    }
}(function ($) {
    $.fn.calendarTrack = function (parameter, getApi) {
        parameter = parameter || {};
        var defaults = {
            prefix: 'widget', //生成日历的class前缀
            isRange: false, //是否选择范围
            limitRange: [], //有效选择区域的范围
            highlightRange: [], //指定日期范围高亮
            stopHighlightRange: [],		//超待停车标记高亮
            peopleHighlightRange: [], //人停车标记
            thingHighlightRange: [], //物停车标记
            onChange: function () {
            }, //当前选中月份修改时触发
            onSelect: function () {
            } //选择日期时触发
        };
        var options = $.extend({}, defaults, parameter);

        var ifDoubleClick = true;

        return this.each(function () {
            var $this = $(this);
            var $table = $('<table>').appendTo($this);
            var $caption = $('<caption>').appendTo($table);
            var $prevYear = $('<a class="' + options.prefix + '-prevYear" href="javascript:;">&lt;&lt;</a>').appendTo($caption);
            var $prevMonth = $('<a class="' + options.prefix + '-prevMonth" href="javascript:;">&lt;</a>').appendTo($caption);
            var $title = $('<span>').appendTo($caption);
            var $nextMonth = $('<a class="' + options.prefix + '-nextMonth" href="javascript:;">&gt;</a>').appendTo($caption);
            var $nextYear = $('<a class="' + options.prefix + '-nextYear" href="javascript:;">&gt;&gt;</a>').appendTo($caption);
            var $back = $('<a class="' + options.prefix + '-back" href="javascript:;"></a>').appendTo($caption);
            var _today, //当天
                _data, //日期数据
                _day, //日历状态
                _range = []; //当前选择范围
            /*****  节点修改 *****/
            $table.append('<thead><tr><th>日</th><th>一</th><th>二</th><th>三</th><th>四</th><th>五</th><th>六</th></tr></thead>');
            var $tbody = $('<tbody>').appendTo($table);
            /***** 私有方法 *****/
            //获取日期数据

            if (trackPlayback.flags.isFlag) {
                var nowYear = trackPlayback.calendar.nowYear;
                var getDateObj = function (year, month, day) {
                    var date = arguments.length && nowYear ? new Date(nowYear, monthIndex - 1, day) : new Date();
                    var obj = {
                        'year': parseInt(nowYear),
                        'month': parseInt(monthIndex),
                        'day': date.getDate(),
                        'week': date.getDay()
                    }
                    obj.code = '' + obj.year + (obj.month > 9 ? obj.month : '0' + obj.month) + (obj.day > 9 ? obj.day : '0' + obj.day);
                    return obj;
                };
            } else {
                var getDateObj = function (year, month, day) {
                    var date = arguments.length && year ? new Date(year, month - 1, day) : new Date();
                    var obj = {
                        'year': date.getFullYear(),
                        'month': date.getMonth() + 1,
                        'day': date.getDate(),
                        'week': date.getDay()
                    }
                    obj.code = '' + obj.year + (obj.month > 9 ? obj.month : '0' + obj.month) + (obj.day > 9 ? obj.day : '0' + obj.day);
                    return obj;
                };
            }
            //获取当月天数
            var getMonthDays = function (obj) {
                var day = new Date(obj.year, obj.month, 0);
                return day.getDate();
            };
            //获取某天日期信息
            var getDateInfo = function (obj) {
                if (options.limitRange.length) {
                    obj.status = 'disabled';
                    for (var i = 0; i < options.limitRange.length; i++) {
                        var start = options.limitRange[i][0];
                        var end = options.limitRange[i][1];
                        if (start == 'today') {
                            start = _today.code;
                        }
                        if (end == 'today') {
                            end = _today.code;
                        }
                        if (start > end) {
                            start = [end, end = start][0];
                        }
                        if (obj.code >= start && obj.code <= end) {
                            obj.status = '';
                            break;
                        }
                    }
                }
                obj.sign = [];
                obj.mileage = [];//存放里程
                if (options.highlightRange.length) {
                    for (var i = 0; i < options.highlightRange.length; i++) {
                        var start = options.highlightRange[i][0];
                        var end = options.highlightRange[i][1];
                        var mileage = options.highlightRange[i][2];
                        if (start == 'today') {
                            start = _today.code;
                        }
                        if (end == 'today') {
                            end = _today.code;
                        }
                        if (start > end) {
                            start = [end, end = start][0];
                        }
                        if (obj.code >= start && obj.code <= end) {
                            obj.sign.push('highlight');
                            obj.mileage.push(mileage);
                            break;
                        }
                    }
                }
                //超待停车时间高亮
                if (options.stopHighlightRange.length) {
                    for (var i = 0; i < options.stopHighlightRange.length; i++) {
                        var start = options.stopHighlightRange[i][0];
                        var end = options.stopHighlightRange[i][1];
                        var mileage = options.stopHighlightRange[i][2];
                        if (start == 'today') {
                            start = _today.code;
                        }
                        if (end == 'today') {
                            end = _today.code;
                        }
                        if (start > end) {
                            start = [end, end = start][0];
                        }
                        if (obj.code >= start && obj.code <= end) {
                            obj.sign.push('stopHighlight');
                            obj.mileage.push(mileage);
                            break;
                        }
                    }
                }
                //人停车时间高亮
                if (options.peopleHighlightRange.length) {
                    for (var i = 0; i < options.peopleHighlightRange.length; i++) {
                        var start = options.peopleHighlightRange[i][0];
                        var end = options.peopleHighlightRange[i][1];
                        var mileage = options.peopleHighlightRange[i][2];
                        if (start == 'today') {
                            start = _today.code;
                        }
                        if (end == 'today') {
                            end = _today.code;
                        }
                        if (start > end) {
                            start = [end, end = start][0];
                        }
                        if (obj.code >= start && obj.code <= end) {
                            obj.sign.push('peopleHighlight');
                            obj.mileage.push(mileage);
                            break;
                        }
                    }
                }
                //物停车时间高亮
                if (options.thingHighlightRange.length) {
                    for (var i = 0; i < options.thingHighlightRange.length; i++) {
                        var start = options.thingHighlightRange[i][0];
                        var end = options.thingHighlightRange[i][1];
                        var mileage = options.thingHighlightRange[i][2];
                        if (start == 'today') {
                            start = _today.code;
                        }
                        if (end == 'today') {
                            end = _today.code;
                        }
                        if (start > end) {
                            start = [end, end = start][0];
                        }
                        if (obj.code >= start && obj.code <= end) {
                            obj.sign.push('thingHighlight');
                            obj.mileage.push(mileage);
                            break;
                        }
                    }
                }

                if (obj.code == _today.code) {
                    obj.sign.push('today');
                }
                return obj;
            };
            var getData = function (obj) {
                if (typeof obj == 'undefined') {
                    obj = _today;
                }
                _day = getDateObj(obj.year, obj.month, 1); //当月第一天
                var days = getMonthDays(_day); //当月天数
                var data = []; //日历信息
                var obj = {};
                //上月日期
                for (var i = _day.week; i > 0; i--) {
                    obj = getDateObj(_day.year, _day.month, _day.day - i);
                    if (obj.month === _day.month) {
                        obj.month--;
                        if (obj.month === 0) {
                            obj.year--;
                            obj.month = 12;
                        }
                        obj.code = obj.year + (obj.month > 9 ? obj.month : ('0' + obj.month)) + (obj.day > 9 ? obj.day : ('0' + obj.day))
                    }
                    var info = getDateInfo(obj);
                    if (!options.limitRange.length) {
                        info.status = 'disabled';
                    }
                    data.push(info);
                }
                //当月日期
                for (var i = 0; i < days; i++) {
                    obj = {
                        'year': _day.year,
                        'month': _day.month,
                        'day': _day.day + i,
                        'week': (_day.week + i) % 7
                    };
                    obj.code = '' + obj.year + (obj.month > 9 ? obj.month : '0' + obj.month) + (obj.day > 9 ? obj.day : '0' + obj.day);
                    var info = getDateInfo(obj);
                    data.push(info);
                }
                //下月日期
                var last = obj;
                for (var i = 1; last.week + i < 7; i++) {
                    obj = getDateObj(last.year, last.month, last.day + i);
                    if (obj.month === _day.month) {
                        obj.month++;
                        if (obj.month > 12) {
                            obj.year++;
                            obj.month = 1;
                        }
                        obj.code = obj.year + (obj.month > 9 ? obj.month : ('0' + obj.month)) + (obj.day > 9 ? obj.day : ('0' + obj.day))
                    }
                    var info = getDateInfo(obj);
                    if (!options.limitRange.length) {
                        info.status = 'disabled';
                    }
                    data.push(info);
                }
                return data;
            };
            var format = function (data) {
                options.onChange(_day);
                var html = '<tr>';
                for (var i = 0, len = data.length; i < len; i++) {
                    var day = data[i];
                    var arr = [];
                    for (var s = 0; s < day.sign.length; s++) {
                        arr.push(options.prefix + '-' + day.sign[s]);
                    }
                    if (day.status) {
                        arr.push(options.prefix + '-' + day.status);
                    }
                    var className = arr.join(' ');
                    html += '<td' + (className ? ' class="' + className + '"' : '') + ' data-id="' + i + '" title="' + ((data[i].mileage[0] === "" || data[i].mileage[0] == undefined || data[i].mileage[0] < 0 || data[i].mileage[0] > 4000) ? "-" : data[i].mileage[0]) + '">\
                        ' + (day.link ? '<a href="' + day.link + '">' + day.day + '</a>' : '<span class="dayShow">' + day.day + '<br/><span class="mileageList">' + ((data[i].mileage[0] === "" || data[i].mileage[0] == undefined || data[i].mileage[0] < 0 || data[i].mileage[0] > 4000) ? "-" : data[i].mileage[0]) + '</span></span>') + '\
                    </td>';
                    if (i % 7 == 6 && i < len - 1) {
                        html += '</tr><tr>';
                    }
                }
                html += '</tr>';
                $title.html(_day.year + '年' + _day.month + '月');
                $tbody.html(html);
            };
            /***** 初始化 *****/
            _today = getDateObj();
            _day = {
                'year': _today.year,
                'month': _today.month
            };
            $prevMonth.click(function () {
                var afterMonth, nowMonth, nowYear;
                var monthString = $(this).next("span").text().replace(/[\u4e00-\u9fa5]+/g, "-");
                afterMonth = monthString.substring(0, monthString.length - 1) + "-01";
                if (parseInt(afterMonth.substring(5, afterMonth.length)) - 1 <= 0) {
                    nowMonth = parseInt(afterMonth.substring(0, 4)) - 1 + "-12" + "-01";
                    nowYear = parseInt(afterMonth.substring(0, 4)) - 1;
                    monthIndex = 12;
                } else {
                    nowMonth = afterMonth.substring(0, 5) + (parseInt(afterMonth.substring(5, afterMonth.length)) - 1) + "-01";
                    nowYear = afterMonth.substring(0, 4);
                    monthIndex = parseInt(afterMonth.substring(5, afterMonth.length)) - 1;
                }
                var carID = trackPlayback.data.getVid();
                var nowDateArray = nowMonth.split("-");
                var afterDateArray = afterMonth.split("-");
                nowMonth = nowDateArray[0] + "-" + (nowDateArray[1] < 10 ? "0" + nowDateArray[1] : nowDateArray[1]) + "-" + nowDateArray[2];
                afterMonth = afterDateArray[0] + "-" + (afterDateArray[1] < 10 ? "0" + afterDateArray[1] : afterDateArray[1]) + "-" + afterDateArray[2];
                trackPlayback.calendar.nowMonth = nowMonth;
                trackPlayback.calendar.afterMonth = afterMonth;
                trackPlayback.calendar.nowYear = nowYear;

                if (carID != "") {
                    trackPlayback.flags.isFlag = true;
                    trackPlayback.data.setNowMonth(nowMonth);
                    trackPlayback.data.setAfterMonth(nowMonth);
                    trackPlayback.calendar.getValidDate();
                }

                _day.month--;
                _data = getData(_day);
                format(_data);

                $('.main-content-toggle-left .calendar3 tbody td').each(function () {
                    if ($(this).hasClass("widget-disabled")) {
                        $(this).removeClass("widget-highlight").removeClass("widget-stopHighlight");
                        $(this).children("span").children("span.mileageList").text("");
                    }
                })
                var trBtnLength = $("#leftPart .calendar3 tbody tr").length;
                if (trBtnLength === 5) {
                    $('.calender-container').css('height', '297px');
                    $('.tree-container').css('height', 'calc(100% - 405px)');
                } else if (trBtnLength === 6) {
                    $('.calender-container').css('height', '341px');
                    $('.tree-container').css('height', 'calc(100% - 449px)');
                }

            });
            $nextMonth.click(function () {
                var nowMonth, afterMonth, nowYear;
                var monthString = $(this).siblings("span").text().replace(/[\u4e00-\u9fa5]+/g, "-");
                var nowMonthString = monthString.substring(0, monthString.length - 1);
                nowMonth = nowMonthString.substring(0, 4) + "-" + (parseInt(nowMonthString.substring(5, nowMonthString.length)) + 1) + "-01";
                if ((parseInt(nowMonthString.substring(5, nowMonthString.length)) + 1) - 12 == 0) {
                    afterMonth = parseInt(nowMonthString.substring(0, 4)) + 1 + "-1" + "-01";
                    nowYear = parseInt(nowMonthString.substring(0, 4));
                    monthIndex = 12;
                } else if ((parseInt(nowMonthString.substring(5, nowMonthString.length)) + 1) - 12 > 0) {
                    afterMonth = parseInt(nowMonthString.substring(0, 4)) + 1 + "-2" + "-01";
                    nowMonth = parseInt(nowMonthString.substring(0, 4)) + 1 + "-1" + "-01";
                    nowYear = parseInt(nowMonthString.substring(0, 4)) + 1;
                    monthIndex = 1;
                } else {
                    afterMonth = nowMonthString.substring(0, 5) + (parseInt(nowMonthString.substring(5, nowMonthString.length)) + 2) + "-01";
                    nowYear = nowMonthString.substring(0, 4);
                    monthIndex = parseInt(nowMonthString.substring(5, nowMonthString.length)) + 1;
                }
                var carID = trackPlayback.data.getVid();
                var nowDateArray = nowMonth.split("-");
                var afterDateArray = afterMonth.split("-");
                nowMonth = nowDateArray[0] + "-" + (nowDateArray[1] < 10 ? "0" + nowDateArray[1] : nowDateArray[1]) + "-" + nowDateArray[2];
                afterMonth = afterDateArray[0] + "-" + (afterDateArray[1] < 10 ? "0" + afterDateArray[1] : afterDateArray[1]) + "-" + afterDateArray[2];
                trackPlayback.calendar.nowMonth = nowMonth;
                trackPlayback.calendar.afterMonth = afterMonth;
                trackPlayback.calendar.nowYear = nowYear;
                if (carID != "") {
                    trackPlayback.flags.isFlag = true;
                    trackPlayback.data.setNowMonth(nowMonth);
                    trackPlayback.data.setAfterMonth(nowMonth);
                    trackPlayback.calendar.getValidDate();
                }

                _day.month++;
                _data = getData(_day);
                format(_data);
                $('.main-content-toggle-left .calendar3 tbody td').each(function () {
                    if ($(this).hasClass("widget-disabled")) {
                        $(this).removeClass("widget-highlight").removeClass("widget-stopHighlight");
                        $(this).children("span").children("span.mileageList").text("");
                    }
                })
                var trBtnLength = $("#leftPart .calendar3 tbody tr").length;
                if (trBtnLength === 5) {
                    $('.calender-container').css('height', '297px');
                    $('.tree-container').css('height', 'calc(100% - 405px)');
                } else if (trBtnLength === 6) {
                    $('.calender-container').css('height', '341px');
                    $('.tree-container').css('height', 'calc(100% - 449px)');
                }
            });
            $prevYear.click(function () {
                var nowMonth, afterMonth, nowYear;
                var monthString = $(this).siblings("span").text().replace(/[\u4e00-\u9fa5]+/g, "-");
                var afterMonthString = monthString.substring(0, monthString.length - 1);
                afterMonth = (parseInt(afterMonthString.substring(0, 4)) - 1) + "-" + (parseInt(afterMonthString.substring(5, afterMonthString.length)) + 1) + "-01";
                nowMonth = (parseInt(afterMonthString.substring(0, 4)) - 1) + "-" + parseInt(afterMonthString.substring(5, afterMonthString.length)) + "-01";
                nowYear = parseInt(afterMonthString.substring(0, 4)) - 1;
                monthIndex = afterMonthString.substring(5, afterMonthString.length);
                var carID = trackPlayback.data.getVid();
                var nowDateArray = nowMonth.split("-");
                var afterDateArray = afterMonth.split("-");
                nowMonth = nowDateArray[0] + "-" + (nowDateArray[1] < 10 ? "0" + nowDateArray[1] : nowDateArray[1]) + "-" + nowDateArray[2];
                afterMonth = afterDateArray[0] + "-" + (afterDateArray[1] < 10 ? "0" + afterDateArray[1] : afterDateArray[1]) + "-" + afterDateArray[2];

                trackPlayback.calendar.nowMonth = nowMonth;
                trackPlayback.calendar.afterMonth = afterMonth;
                trackPlayback.calendar.nowYear = nowYear;

                if (carID != "") {
                    trackPlayback.flags.isFlag = true;
                    trackPlayback.data.setNowMonth(nowMonth);
                    trackPlayback.data.setAfterMonth(nowMonth);
                    trackPlayback.calendar.getValidDate();
                }

                _day.year--;
                _data = getData(_day);
                format(_data);
                var trBtnLength = $("#leftPart .calendar3 tbody tr").length;
                if (trBtnLength === 5) {
                    $('.calender-container').css('height', '297px');
                    $('.tree-container').css('height', 'calc(100% - 405px)');
                } else if (trBtnLength === 6) {
                    $('.calender-container').css('height', '341px');
                    $('.tree-container').css('height', 'calc(100% - 449px)');
                }
            });
            $nextYear.click(function () {
                var nowMonth, afterMonth, nowYear;

                var monthString = $(this).siblings("span").text().replace(/[\u4e00-\u9fa5]+/g, "-");
                var afterMonthString = monthString.substring(0, monthString.length - 1);
                afterMonth = (parseInt(afterMonthString.substring(0, 4)) + 1) + "-" + (parseInt(afterMonthString.substring(5, afterMonthString.length)) + 1) + "-01";
                nowMonth = (parseInt(afterMonthString.substring(0, 4)) + 1) + "-" + parseInt(afterMonthString.substring(5, afterMonthString.length)) + "-01";
                nowYear = parseInt(afterMonthString.substring(0, 4)) + 1;
                monthIndex = afterMonthString.substring(5, afterMonthString.length);
                var carID = trackPlayback.data.getVid();
                var nowDateArray = nowMonth.split("-");
                var afterDateArray = afterMonth.split("-");
                nowMonth = nowDateArray[0] + "-" + (nowDateArray[1] < 10 ? "0" + nowDateArray[1] : nowDateArray[1]) + "-" + nowDateArray[2];
                afterMonth = afterDateArray[0] + "-" + (afterDateArray[1] < 10 ? "0" + afterDateArray[1] : afterDateArray[1]) + "-" + afterDateArray[2];

                trackPlayback.calendar.nowMonth = nowMonth;
                trackPlayback.calendar.afterMonth = afterMonth;
                trackPlayback.calendar.nowYear = nowYear;

                if (carID != "") {
                    trackPlayback.flags.isFlag = true;
                    trackPlayback.data.setNowMonth(nowMonth);
                    trackPlayback.data.setAfterMonth(nowMonth);
                    trackPlayback.calendar.getValidDate();
                }

                _day.year++;
                _data = getData(_day);
                format(_data);
                var trBtnLength = $("#leftPart .calendar3 tbody tr").length;
                if (trBtnLength === 5) {
                    $('.calender-container').css('height', '297px');
                    $('.tree-container').css('height', 'calc(100% - 405px)');
                } else if (trBtnLength === 6) {
                    $('.calender-container').css('height', '341px');
                    $('.tree-container').css('height', 'calc(100% - 449px)');
                }
            });
            $back.click(function () {
                _data = getData();
                format(_data);
            });
            $this.unbind("click").on('click', 'td', function (e) {

                if (!ifDoubleClick) { //防止点击过快wjk
                    return;
                }
                ifDoubleClick = false;
                trackPlayback.data.setClickShowLocation(false);
                try {
                    // trackPlayback.stopMove();
                    $("#playIcon").attr("class", "resultIcon playIcon");
                    $("#playIcon").removeAttr("data-original-title data-placement data-toggle");
                    $("#playIcon").attr({
                        "data-toggle": "tooltip",
                        "data-placement": "top",
                        "data-original-title": "播放"
                    });
                    isSearch = true;
                } catch (e) {
                    $("#playIcon").attr("class", "resultIcon playIcon");
                    $("#playIcon").removeAttr("data-original-title data-placement data-toggle");
                    $("#playIcon").attr({
                        "data-toggle": "tooltip",
                        "data-placement": "top",
                        "data-original-title": "播放"
                    });
                }
                if ($("#chooseStopPoint").attr("checked")) {
                    $("#chooseStopPoint").prop("checked", false).removeAttr("checked", "checked");
                }
                $("#myTab").children("li").removeClass("active");
                $("#myTab").children("li:first-child").addClass("active");
                $("#myTabContent").children("div").removeClass("active in");
                $("#myTabContent").children("div:first-child").addClass("active in");
                //重置播放进度
                $("#progressBar_Track").removeAttr("style");
                //点击日历天数查询时隐藏其他 显示第一个
                $("#peopleGPSData,#stopData,#peopleStopData,#warningData").hide();
                $("#GPSData").addClass("active in").show();
                //日历点击取消报警点勾选
                if ($("#chooseAlarmPoint").attr("checked")) {
                    $("#chooseAlarmPoint").prop("checked", false).removeAttr("checked", "checked");
                }
                $("#gpsTable3>tbody").html('<tr class=""><td valign="top" colspan="12" class="dataTables_empty">我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？</td></tr>');
                //清空报警点集合(markerAlarmList)
                // trackPlayback.markerAlarmClear();
                //加载时隐藏列表
                if ($(this).hasClass("widget-highlight") || $(this).hasClass("widget-peopleHighlight")) {
                    if ($("#scalingBtn").hasClass("fa-chevron-down")) {
                        var windowHeight = $(window).height();
                        headerHeight = $("#header").height();
                        titleHeight = $(".panHeadHeight").height() + 30;
                        demoHeight = $("#Demo").height();
                        var oldMHeight = $("#MapContainer").height();
                        var oldTHeight = $(".trackPlaybackTable .dataTables_scrollBody").height();
                        var mapHeight = windowHeight - headerHeight - titleHeight - demoHeight - 20;
                        $("#MapContainer").css({
                            "height": mapHeight + "px"
                        });
                        $(".trackPlaybackTable .dataTables_scrollBody").css({
                            "height": oldTHeight + "px"
                        });
                        $("#scalingBtn").attr("class", "fa  fa-chevron-down");
                    }
                }
                stopDataFlag = true;
                // trackPlayback.Assemblys();
                var $this = $(this);
                var index = $(this).data('id');
                var day = _data[index];
                // var flag = trackPlayback.disable();
                // if (flag == false) {
                //     return false;
                // }
                //给开始和结束时间赋值(正常车辆)
                if ($this.hasClass("widget-highlight")) {
                    var value = $this.children('span').children('span.mileageList').text();
                    var startTime = day.year + "-" + (day.month < 10 ? "0" + day.month : day.month) + "-" + (day.day < 10 ? "0" + day.day : day.day) + " " + "00:00:00";
                    var endTime = day.year + "-" + (day.month < 10 ? "0" + day.month : day.month) + "-" + (day.day < 10 ? "0" + day.day : day.day) + " " + "23:59:59";
                    // $("#timeInterval").val(startTime + "--" + endTime);
                    // trackPlayback.clears();
                    // playState = true;
                    // trackPlayback.map.clearMap();
                    // $("#allMileage").text(0 + "km");
                    // $("#allTime").text(0);
                    // $("#maxSpeend").text(0 + "km/h");
                    var index = $(".widget-highlight").index($(this));
                    trackPlayback.data.setValidClickedIndex(index);
                    trackPlayback.data.setStartEndTime(startTime, endTime);
                    if ($('#searchTimeInterval').length && $('#searchTimeInterval').is(':hidden')) {
                        $('#searchTimeInterval').val(startTime + '--' + endTime);
                    }
                    // trackPlayback.getHistory(value, index);
                }
                //超待设备
                if ($this.hasClass("widget-stopHighlight")) {
                    var value = $this.children('span').children('span.mileageList').text();
                    var startTime = day.year + "-" + (day.month < 10 ? "0" + day.month : day.month) + "-" + (day.day < 10 ? "0" + day.day : day.day) + " " + "00:00:00";
                    var endTime = day.year + "-" + (day.month < 10 ? "0" + day.month : day.month) + "-" + (day.day < 10 ? "0" + day.day : day.day) + " " + "23:59:59";
                    $("#timeInterval").val(startTime + "--" + endTime);
                    // trackPlayback.clears();
                    // playState = true;
                    // trackPlayback.map.clearMap();
                    // $("#allMileage").text(0 + "km");
                    // $("#allTime").text(0);
                    // $("#maxSpeend").text(0 + "km/h");
                    var index = $(".widget-stopHighlight").index($(this));
                    trackPlayback.data.setValidClickedIndex(index);
                    trackPlayback.data.setStartEndTime(startTime, endTime);
                    // trackPlayback.getHistory(value, index);
                }
                ;
                //人
                if ($this.hasClass("widget-peopleHighlight")) {
                    var value = $this.children('span').children('span.mileageList').text();
                    $(".mileageList").parent().parent().attr("title", value);
                    var startTime = day.year + "-" + (day.month < 10 ? "0" + day.month : day.month) + "-" + (day.day < 10 ? "0" + day.day : day.day) + " " + "00:00:00";
                    var endTime = day.year + "-" + (day.month < 10 ? "0" + day.month : day.month) + "-" + (day.day < 10 ? "0" + day.day : day.day) + " " + "23:59:59";
                    $("#timeInterval").val(startTime + "--" + endTime);
                    // trackPlayback.clears();
                    // playState = true;
                    // trackPlayback.map.clearMap();
                    // $("#allMileage").text(0 + "km");
                    // $("#allTime").text(0);
                    // $("#maxSpeend").text(0 + "km/h");
                    var index = $(".widget-peopleHighlight").index($(this));
                    trackPlayback.data.setValidClickedIndex(index);
                    trackPlayback.data.setStartEndTime(startTime, endTime);
                    // trackPlayback.getHistory(value, index);
                }
                ;
                /*end*/
                if (day.status != 'disabled') {
                    if (options.isRange) {
                        if (_range.length != 1) {
                            _range = [day];
                            format(_data);
                        } else {
                            _range.push(day);
                            _range.sort(function (a, b) {
                                return a.code > b.code;
                            });
                            format(_data);
                            options.onSelect(_range);
                        }
                    } else {
                        _range = [day];
                        format(_data);
                        options.onSelect(_range);
                    }
                }
                $('.main-content-toggle-left .calendar3 tbody td').each(function () {
                    if ($(this).hasClass("widget-disabled")) {
                        $(this).removeClass("widget-highlight").removeClass("widget-stopHighlight");
                        $(this).children("span").children("span.mileageList").text("-");
                    }
                })

                setTimeout(function () {
                    ifDoubleClick = true;
                }, 300)
            });
            _data = getData();
            format(_data);
            var trBtnLength = $("#leftPart .calendar3 tbody tr").length;
            if (trBtnLength === 5) {
                $('.calender-container').css('height', '297px');
                $('.tree-container').css('height', 'calc(100% - 405px)');
            } else if (trBtnLength === 6) {
                $('.calender-container').css('height', '341px');
                $('.tree-container').css('height', 'calc(100% - 449px)');
            }
        });
    };
}));