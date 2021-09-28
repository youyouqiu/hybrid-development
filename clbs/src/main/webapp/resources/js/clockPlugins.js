(function () {
    var wraper = function ($selectorOuter, options) {
        var $, Calendar, DAYS, DateRangePicker, MONTHS, TEMPLATE, TEMPLATENOHMS, this_pageX, this_pageY,
            isDegFlag = false, thisPointer,
            isArea, beforeTimeArea, afterTimeArea, thisDay, timepiker;
        var newstartdate = new Date(), newenddate = new Date();

        var isFilterDate = false; //wjk 定义变量判断开始时间和结束时间是否遍历完

        var defaults = {
            'start_date': '',
            'end_date': '',
            'startdate': '',
            'enddate': '',
            'start_time': '00:00:00',
            'end_time': '23:59:59',
            'isShowHMS': true,//是否显示时分秒 author wjk 2018/10/29
            'type': 'before',
            'timeSelect': '1',
            'isTimeLineClick': false,
            'element': '#inquireClick',
            'imgsrc': '',
            'imgsrchover': '',
            'blTimeUpdate': false,
            'brTimeUpdate': false,
            'clearBtn':false,//清空按钮
        };

        $ = jQuery;

        beforeTimeArea = ['前7天', '前30天', '前90天', '前180天', '前360天', '自定义'];

        afterTimeArea = ['后30天', '后60天', '后1年', '后2年', '后3年', '自定义'];

        DAYS = ['日', '一', '二', '三', '四', '五', '六'];

        MONTHS = ['01月', '02月', '03月', '04月', '05月', '06月', '07月', '08月', '09月', '10月', '11月', '12月'];

        TEMPLATE = '<div class="drp-popup">\n  <div class="drp-timeline">\n    <ul class="drp-timeline-presets"></ul>\n    <div class="drp-timeline-bar"></div>\n  </div>\n  <div class="drp-calendars">\n    <div class="drp-calendar drp-calendar-start">\n      <div class="drp-month-picker">\n        <div class="drp-arrow"><</div>\n        <div class="drp-month-title"></div>\n        <div class="drp-arrow drp-arrow-right">></div>\n      </div>\n      <ul class="drp-day-headers"></ul>\n      <ul class="drp-days"></ul>\n <div class=\'col-md-12\' style=\'top:41px;height: 18px;line-height: 18px;\'> <div class=" col-md-5 drp-calendar-date-text">开始时间:</div> <input class="drp-calendar-date col-md-7" style=\'border: none;background-color: #fff;box-shadow: none;padding-left:0px;\'> \n   </div>\n </div>\n    <div class="drp-calendar-separator"></div>\n    <div class="drp-calendar drp-calendar-end">\n      <div class="drp-month-picker">\n        <div class="drp-arrow"><</div>\n        <div class="drp-month-title"></div>\n        <div class="drp-arrow drp-arrow-right">></div>\n      </div>\n      <ul class="drp-day-headers"></ul>\n      <ul class="drp-days"></ul>\n     <div class=\'col-md-12\' style=\'top:41px;height: 18px;line-height: 18px;\'><div class=\'col-md-5 drp-calendar-date-text\'>结束时间:</div> <input class="drp-calendar-date col-md-7" style=\'border: none;background-color: #fff;box-shadow: none;padding-left:0px;\'>\n  </div>\n  </div>\n  </div>\n  <div class="drp-tip">\n</div><div id=\'timepikerdiv\' style=\'height:40px;position: relative;top: 37px;border-top:1px solid #e0e0e0;padding-top:10px\' class=\'col-md-12\'><div id=\'timepiker\'  type=\'button\' class=\'col-md-offset-9 col-md-1\'></div><button type=\'button\' class=\'col-md-1\' id=\'savebutton\' value=\'查询\' style=\'margin-left:20px\' ></button></div>';
        TEMPLATENOHMS = '<div class="drp-popup">\n  <div class="drp-timeline">\n    <ul class="drp-timeline-presets"></ul>\n    <div class="drp-timeline-bar"></div>\n  </div>\n  <div class="drp-calendars">\n    <div class="drp-calendar drp-calendar-start">\n      <div class="drp-month-picker">\n        <div class="drp-arrow"><</div>\n        <div class="drp-month-title"></div>\n        <div class="drp-arrow drp-arrow-right">></div>\n      </div>\n      <ul class="drp-day-headers"></ul>\n      <ul class="drp-days"></ul>\n <div class=\'col-md-12\' style=\'top:41px;height: 18px;line-height: 18px;\'> <div class=" col-md-5 drp-calendar-date-text">开始时间:</div> <input class="drp-calendar-date col-md-7" style=\'border: none;background-color: #fff;box-shadow: none;padding-left:0px;\'> \n   </div>\n </div>\n    <div class="drp-calendar-separator"></div>\n    <div class="drp-calendar drp-calendar-end">\n      <div class="drp-month-picker">\n        <div class="drp-arrow"><</div>\n        <div class="drp-month-title"></div>\n        <div class="drp-arrow drp-arrow-right">></div>\n      </div>\n      <ul class="drp-day-headers"></ul>\n      <ul class="drp-days"></ul>\n     <div class=\'col-md-12\' style=\'top:41px;height: 18px;line-height: 18px;\'><div class=\'col-md-5 drp-calendar-date-text\'>结束时间:</div> <input class="drp-calendar-date col-md-7" style=\'border: none;background-color: #fff;box-shadow: none;padding-left:0px;\'>\n  </div>\n  </div>\n  </div>\n  <div class="drp-tip">\n</div><div id=\'timepikerdiv\' style=\'height:40px;position: relative;top: 37px;border-top:1px solid #e0e0e0;padding-top:10px\' class=\'col-md-12\'><button type=\'button\' class=\'col-md-offset-10 col-md-1\' id=\'savebutton\' value=\'查询\'></button></div>';

        DateRangePicker = (function () {
            function DateRangePicker($select, opt) {
                defaults = $.extend({}, defaults, opt);
                this.$select = $select;//当前select对象
                if (defaults.isShowHMS) {
                    this.$dateRangePicker = $(TEMPLATE);//日历插件结构
                } else {
                    this.$dateRangePicker = $(TEMPLATENOHMS);//日历插件结构
                }
                if(defaults.clearBtn){
                    var $bottomDiv = this.$dateRangePicker.find('#timepikerdiv')
                    if(defaults.isShowHMS){
                        $bottomDiv.append($('<p id="clearRangePicker" style="position: absolute;right: 152px;bottom: -1px;border-radius: 4px;width: 57px;height: 26px;text-align: center;line-height: 24px;color: #777777;cursor: pointer;"><img src="/clbs/resources/img/clearoutline.svg" style="width: 68px;height: 27px;max-width: 150%;"></p>'))
                    }else {
                        $bottomDiv.append($('<p id="clearRangePicker" style="position: absolute;right: 100px;bottom: -1px;border-radius: 4px;width: 57px;height: 26px;text-align: center;line-height: 24px;color: #777777;cursor: pointer;"><img src="/clbs/resources/img/clearoutline.svg" style="width: 68px;height: 27px;max-width: 150%;"></p>'))
                    }
                }
                // this.$dateRangePicker =defaults.isShowHMS ? $(TEMPLATE) : $(TEMPLATENOHMS);//日历插件结构
                //this.$select.attr('tabindex', '-1').before(this.$dateRangePicker);//添加对象
                if (opt && opt.wrap) {
                    $(opt.wrap).append(this.$dateRangePicker);
                } else {
                    $('body').after(this.$dateRangePicker);
                }
                this.isHidden = true;
                this.customOptionIndex = this.$select[0].length - 1;
                this.initBindings();
                this.setRange(defaults.timeSelect, 'init');
                if (defaults.imgsrc == undefined || defaults.imgsrc == '') {

                } else {
                    this.$dateRangePicker.find('#savebutton').css('background-image', defaults.imgsrc);
                    this.$dateRangePicker.find('#savebutton').on('mouseover', function () {
                        $(this).css('background-image', defaults.imgsrc);
                    });
                }
            }

            DateRangePicker.prototype.initBindings = function () {
                var self;
                self = this;
                //select标签点击事件
                this.$select.on('focus mousedown', function (e) {
                    var $select;
                    $select = this;
                    /* setTimeout(function() {
              return $select.blur();
            }, 0);*/
                    // return false;
                });
                //整个日历区域点击事件
                this.$dateRangePicker.click(function (evt) {
                    return evt.stopPropagation();
                });
                var newtime;
                $('body').click(function (evt) {

                    if (evt.target === self.$select[0] && self.isHidden) {
                        var value = evt.target.value.split('--');
                        if(!evt.target.value){
                            var today = new Date().Format('yyyy-MM-dd')
                            if(defaults.isShowHMS){
                                value = [today + ' ' +'00:00:00',today + ' ' +'23:59:59']
                            }else {
                                value = [today,today]
                            }
                        }
                        var default_start = value[0].split(' ');
                        var default_end = value[1].split(' ');
                        defaults.start_time = default_start[1];
                        defaults.end_time = default_end[1];
                        defaults.start_date = default_start[0];
                        defaults.end_date = default_end[0];
                        var startTime = new Date(value[0].replace(/\-/g, '/')).getTime();
                        var endTime = new Date(value[1].replace(/\-/g, '/')).getTime();
                        var this_day = Math.ceil((endTime - startTime) / 1000 / 24 / 60 / 60);
                        self.setRange(this_day);
                        self.show();
                        var reDateTime = /^(?:19|20)[0-9][0-9]-(?:(?:0[1-9])|(?:1[0-2]))-(?:(?:[0-2][1-9])|(?:[1-3][0-1])) (?:(?:[0-2][0-3])|(?:[0-1][0-9])):[0-5][0-9]:[0-5][0-9]$/;
                        newtime = new timepiker(self);

                        self.$dateRangePicker.find('.drp-calendar-date').on('focus', function () {
                            $(this).attr('time', $(this).val());
                        });
                        self.$dateRangePicker.find('#timepiker').on('click', function () {
                            $(this).parent().before(newtime.element);
                            newtime.show(this, self);
                            newtime.hidden = false;
                            newtime.element.find('layui-laydate').css({
                                'position': 'relative',
                                'top': '100px'
                            });
                        });
                        self.$dateRangePicker.find('.drp-calendar-date').each(function () {
                            $(this).on('change', function () {
                                var isDateTime2 = reDateTime.test($(this).val());
                                // $(this).val($(this).attr("time"));
                                var date1 = self.$dateRangePicker.find('.drp-calendar-date').eq(0).val();
                                var date2 = self.$dateRangePicker.find('.drp-calendar-date').eq(1).val();
                                date1 = new Date(date1);
                                date2 = new Date(date2);
                                var time1 = date1.getTime();
                                var time2 = date2.getTime();
                                if (time1 > time2) {
                                    $(this).val($(this).attr('time'));
                                    layer.msg('开始时间不能大于结束时间');
                                    return;
                                } 
                                    if (!isDateTime2) {
                                        $(this).val($(this).attr('time'));
                                    } else {
                                        if (defaults.dateLimit) {
                                            if (defaults.onlyLimit && self.datedifference(date1, date2) != defaults.dateLimit){
                                                $(this).val($(this).attr('time'));
                                                layer.msg('只能查询' + defaults.dateLimit + '天范围的数据！');
                                                return false;
                                            }
                                                if (self.datedifference(date1, date2) > defaults.dateLimit) {
                                                    $(this).val($(this).attr('time'));
                                                    layer.msg('最多只能查询' + defaults.dateLimit + '天范围的数据！');
                                                    return false;
                                                }
                                            
                                        }
                                        if (defaults.isOffLineReportFlag) {
                                            // 如果是离线报表, 结束时间和开始时间都不能大于当天
                                            var nowDateTime = defaults.nowDate + ' 23:59:59';
                                            nowDateTime = new Date(nowDateTime);
                                            if (date1.getTime() > nowDateTime.getTime()
                                                || date2.getTime() > nowDateTime.getTime()) {
                                                $(this).val($(this).attr('time'));
                                                return false;
                                            }
                                        }

                                        self.$select.val(self.$dateRangePicker.find('.drp-calendar-date').eq(0).val() + '--' + self.$dateRangePicker.find('.drp-calendar-date').eq(1).val());

                                        var value = self.$select.val().split('--');
                                        var default_start = value[0].split(' ');
                                        var default_end = value[1].split(' ');
                                        defaults.start_time = default_start[1];
                                        defaults.end_time = default_end[1];
                                        defaults.start_date = default_start[0];
                                        defaults.end_date = default_end[0];

                                        // wjk newstartdate赋值
                                        // newstartdate = new Date(value[0]);
                                        //IE兼容日期 wjk 2018/09/29
                                        newstartdate = new Date(value[0].replace(/-/g, '/'));
                                        newenddate = new Date(value[1].replace(/-/g, '/'));

                                        var startTime = new Date(value[0].replace(/\-/g, '/')).getTime();
                                        var endTime = new Date(value[1].replace(/\-/g, '/')).getTime();
                                        var this_day = Math.ceil((endTime - startTime) / 1000 / 24 / 60 / 60);
                                        self.setRange(this_day);

                                        self.showCustomDate(); //wjk修改输入框的值后改为自定义
                                    }
                                
                            });
                        });
                        self.$dateRangePicker.children().find('#savebutton').unbind().on('click', function () {

                            if (defaults.dateLimit) { //有时间限制范围的话判断选择时间是否大于限制
                                var value = self.$select.val().split('--');
                                var default_start = value[0];
                                var default_end = value[1];
                                var days_interval = self.datedifference(default_start, default_end);

                                if (defaults.onlyLimit && days_interval != defaults.dateLimit){
                                    layer.msg('只能查询' + defaults.dateLimit + '天范围的数据！');
                                    return false;
                                }

                                if (days_interval > defaults.dateLimit) {
                                    layer.msg('最多只能查询' + defaults.dateLimit + '天范围的数据！');
                                    return;
                                }
                            }

                            //点击确认隐藏时间选择框
                            self.hide();

                            if (defaults.inquireBtn) {
                                $(defaults.inquireBtn).trigger('click'); //一个页面多个时间选择器时需要传参
                            } else if (defaults.element == '#inquireClick') {
                                $('#inquireClick').trigger('click');

                            } else if (defaults.element == '#trackPlayQuery') {
                                $('#trackPlayQuery').trigger('click');
                            } else {
                                $(defaults.element).trigger('click');
                            }
                            newtime.element.remove();

                            return newtime = new timepiker(self);

                        });

                    } else if (!self.isHidden) {
                        if (evt.target.className == 'layui-layer-content') return;
                        if (newtime.element) {
                            newtime.element.remove();
                        }
                        return self.hide();
                    }
                });
                //初始化日历时间区间选择
                var defaultSelectTime = defaults.timeSelect;
                var calendarType = defaults.type;
                var timeArea;
                if (calendarType == 'before') {
                    timeArea = beforeTimeArea;
                } else if (calendarType == 'after') {
                    timeArea = afterTimeArea;
                }
                ;
                if (timeArea.indexOf(defaultSelectTime) == -1) {
                    defaultSelectTime = '自定义';
                }
                ;
                for (var i = 0; i < timeArea.length; i++) {
                    self.$dateRangePicker.find('.drp-timeline-presets').append($('<li class=\'' + (timeArea[i].indexOf(defaultSelectTime) != -1 ? 'drp-selected' : '') + '\'>' + (timeArea[i]) + '<div class=\'drp-button\'></div></li>'));
                }
                /*this.$select.children().each(function() {
            //初始化日历时间区间选择
            return self.$dateRangePicker.find('.drp-timeline-presets').append($("<li class='" + ((this.selected && 'drp-selected') || '') + "'>" + ($(this).text()) + "<div class='drp-button'></div></li>"));
          });*/
                //清空事件
                self.$dateRangePicker.find('#clearRangePicker').click(function () {
                    self.$select.val('')
                    self.hide();
                })
                //时间区间选择点击事件
                return this.$dateRangePicker.find('.drp-timeline-presets li').click(function (evt) {
                    defaults.start_date = '';
                    defaults.end_date = '';
                    defaults.isTimeLineClick = true;
                    var presetIndex;

                    if (defaults.dateLimit) { // 超出时间限制代码
                        if (defaults.onlyLimit && defaults.onlyLimit != self.number($(this).index())){
                            layer.msg('只能查询' + defaults.dateLimit + '天范围的数据！');
                            return;
                        }
                        if (defaults.dateLimit < self.number($(this).index())) {
                            layer.msg('最多只能查询' + defaults.dateLimit + '天范围的数据！');
                            return;
                        }
                    }
                    $(this).addClass('drp-selected').siblings().removeClass('drp-selected');//添加高亮
                    presetIndex = $(this).index();
                    self.$select[0].selectedIndex = presetIndex;
                    self.setRange(self.number(presetIndex));
                    if (presetIndex === self.customOptionIndex) {
                        return self.showCustomDate();
                    }
                });


            };

            DateRangePicker.prototype.number = function (index) {
                var value;
                if (defaults.type === 'before') {
                    if (index == 0) {
                        value = 7;
                    }
                    ;
                    if (index == 1) {
                        value = 30;
                    }
                    ;
                    if (index == 2) {
                        value = 90;
                    }
                    ;
                    if (index == 3) {
                        value = 180;
                    }
                    ;
                    if (index == 4) {
                        value = 360;
                    }
                    ;
                } else if (defaults.type === 'after') {
                    if (index == 0) {
                        value = 30;
                    }
                    ;
                    if (index == 1) {
                        value = 60;
                    }
                    ;
                    if (index == 2) {
                        value = 365;
                    }
                    ;
                    if (index == 3) {
                        value = 730;
                    }
                    ;
                    if (index == 4) {
                        value = 1095;
                    }
                    ;
                }
                return value;
            };

            DateRangePicker.prototype.hide = function () {
                this.isHidden = true;
                return this.$dateRangePicker.hide();
            };

            DateRangePicker.prototype.show = function () {
                this.isHidden = false;
                return this.$dateRangePicker.show();
            };

            DateRangePicker.prototype.showCustomDate = function () {
                var text;
                this.$dateRangePicker.find('.drp-timeline-presets li:last-child').addClass('drp-selected').siblings().removeClass('drp-selected');
                text = this.formatDate(this.startDate()) + ' - ' + this.formatDate(this.endDate());
                this.$select.find('option:last-child').text(text);
                return this.$select[0].selectedIndex = this.customOptionIndex;
            };

            DateRangePicker.prototype.formatDate = function (d) {
                return '' + (d.getMonth() + 1) + '/' + (d.getDate()) + '/' + (d.getFullYear().toString().substr(2, 2));
            };
            //根据选择的时间区间日历区域进行变化
            DateRangePicker.prototype.setRange = function (daysAgo, ifInit) {
                var endDate, startDate;
                if (isNaN(daysAgo)) {
                    return false;
                }
                var calendarType = defaults.type;
                if (defaults.start_date == '' && defaults.end_date == '') {
                    daysAgo -= 1;

                    if (defaults.nowDate) { //wjk18/12/05 离线报表选择前七天前30天等的渲染，不能选择今天
                        endDate = new Date(defaults.nowDate);
                        startDate = new Date(defaults.nowDate);
                    } else {
                        endDate = new Date();
                        startDate = new Date();
                    }


                    // endDate = new Date();
                    // startDate = new Date();
                    if (calendarType == 'before') {
                        startDate.setDate(endDate.getDate() - daysAgo);//给开始日历设置日期(置前)
                    } else if (calendarType == 'after') {
                        endDate.setDate(endDate.getDate() + daysAgo);
                    }
                    ;
                } else {
                    daysAgo -= 1;
                    endDate = new Date(defaults.end_date.replace(/\-/g, '/'));
                    startDate = new Date(defaults.start_date.replace(/\-/g, '/'));
                }
                ;
                if (defaults.nowDate && ifInit === 'init') {
                    startDate = new Date(defaults.nowDate);
                    endDate = new Date(defaults.nowDate);
                }
                this.startCalendar = new Calendar(this, this.$dateRangePicker.find('.drp-calendar:first-child'), startDate, true);
                this.endCalendar = new Calendar(this, this.$dateRangePicker.find('.drp-calendar:last-child'), endDate, false);
                return this.draw();
            };

            DateRangePicker.prototype.endDate = function () {
                return this.endCalendar.date;
            };

            DateRangePicker.prototype.startDate = function () {
                return this.startCalendar.date;
            };

            DateRangePicker.prototype.draw = function () {
                this.startCalendar.draw();
                return this.endCalendar.draw();
            };

            //wdq设置最大可选日期
            DateRangePicker.prototype.setMaxSelectedDate = function (startDate, endDate, maxDate) {
                defaults.maxSelectedDate = maxDate;
                this.$dateRangePicker.find('.drp-calendar-start .drp-calendar-date').val(startDate);
                this.$dateRangePicker.find('.drp-calendar-end .drp-calendar-date').val(endDate);
                this.setRange();
            }

            //wdq设置最小可选日期
            DateRangePicker.prototype.setMinSelectedDate = function (startDate, endDate, minDate) {
                defaults.minSelectedDate = minDate;
                this.$dateRangePicker.find('.drp-calendar-start .drp-calendar-date').val(startDate);
                this.$dateRangePicker.find('.drp-calendar-end .drp-calendar-date').val(endDate);
                this.setRange();
            }

            //比较两个时间差多少天 wjk
            DateRangePicker.prototype.datedifference = function (sDate1, sDate2) {
                var dateSpan,
                    tempDate,
                    iDays;
                sDate1 = Date.parse(sDate1);
                sDate2 = Date.parse(sDate2);
                dateSpan = sDate2 - sDate1;
                dateSpan = Math.abs(dateSpan);
                iDays = Math.floor(dateSpan / (24 * 3600 * 1000));
                // return iDays
                return (iDays + 1); //加一才是正常天数
            };

            return DateRangePicker;

        }());

        Calendar = (function () {
            //初始化日历
            function Calendar(dateRangePicker, $calendar, date, isStartCalendar) {
                var self;
                this.dateRangePicker = dateRangePicker;
                this.$calendar = $calendar;//当前日历对象
                this.date = date;//当前日期
                this.isStartCalendar = isStartCalendar;
                self = this;
                this.date.setHours(0, 0, 0, 0);//设置指定时间的小时字段
                this._visibleMonth = this.month();//当前月份
                this._visibleYear = this.year();//当前年份
                this.$title = this.$calendar.find('.drp-month-title');//当前显示年月的div标签
                this.$dayHeaders = this.$calendar.find('.drp-day-headers');//当前显示星期的ul标签
                this.$days = this.$calendar.find('.drp-days');//当前显示日期的ul标签
                this.$dateDisplay = this.$calendar.find('.drp-calendar-date');//底部显示详细时间的div标签
                //切换年月点击事件
                $calendar.find('.drp-arrow').click(function (evt) {
                    if ($(this).hasClass('drp-arrow-right')) {
                        self.showNextMonth();
                    } else {
                        self.showPreviousMonth();
                    }
                    return false;
                });
            }

            Calendar.prototype.showPreviousMonth = function () {
                if (this._visibleMonth === 1) {
                    this._visibleMonth = 12;
                    this._visibleYear -= 1;
                } else {
                    this._visibleMonth -= 1;
                }
                return this.draw(true);
            };

            Calendar.prototype.showNextMonth = function () {
                if (this._visibleMonth === 12) {
                    this._visibleMonth = 1;
                    this._visibleYear += 1;
                } else {
                    this._visibleMonth += 1;
                }
                return this.draw(true);
            };

            Calendar.prototype.setDay = function (day) {
                this.setDate(this.visibleYear(), this.visibleMonth(), day);
                return this.dateRangePicker.showCustomDate();
            };

            Calendar.prototype.setDate = function (year, month, day) {

                this.date = new Date(year, month - 1, day);
                if (this.$dateDisplay.parent().parent().hasClass('drp-calendar-start')) {
                    // this_time = defaults.start_time;
                    if (this.date) {
                        newstartdate = this.date;
                    }
                } else if (this.$dateDisplay.parent().parent().hasClass('drp-calendar-end')) {
                    this_time = defaults.end_time;
                    if (this.date) {
                        newenddate = this.date;
                    }
                }
                return this.dateRangePicker.draw();
            };

            Calendar.prototype.draw = function (avoidCheck) {
                var day, _i, _len;
                this.$dayHeaders.empty();//移除星期标签
                this.$title.text('' + (this.nameOfMonth(this.visibleMonth())) + ' ' + (this.visibleYear()));//移入年月
                //移入星期
                for (_i = 0, _len = DAYS.length; _i < _len; _i++) {
                    day = DAYS[_i];
                    this.$dayHeaders.append($('<li>' + (day.substr(0, 2)) + '</li>'));
                }
                !avoidCheck && this.drawDateDisplay();
                var start_time = this.dateRangePicker.$dateRangePicker.find('.drp-calendar-start .drp-calendar-date').val();
                var end_time = this.dateRangePicker.$dateRangePicker.find('.drp-calendar-end .drp-calendar-date').val();

                // this.drawDays()
                // if (defaults.blTimeUpdate && defaults.brTimeUpdate) {
                //   defaults.blTimeUpdate = false;
                //   defaults.brTimeUpdate = false;
                //   if (start_time != '' && end_time != '' && defaults.dateLimit) {
                //     if (this.datedifference(start_time, end_time) > defaults.dateLimit) {
                //       layer.msg("最多只能查询" + defaults.dateLimit + "天范围的数据！");
                //       this.dateRangePicker.$dateRangePicker.find('#savebutton').attr('disabled',true)
                //       return false;
                //     }
                //   }
                //
                //   this.dateRangePicker.$dateRangePicker.find('#savebutton').attr('disabled',false)
                //   this.dateRangePicker.$select.val(start_time + '--' + end_time);
                // }

                // var value = this.dateRangePicker.$select.val().split('--');
                // if (value[0] == '' || value[1] == '') {
                //   this.dateRangePicker.$select.val(start_time + '--' + end_time);
                // }
                if (!defaults.dateLimit) {
                    this.dateRangePicker.$select.val(start_time + '--' + end_time);
                }

                if (defaults.dateLimit && isFilterDate) { //会执行两次drawDateDisplay方法 第二次执行时再判断

                    var curLimit = this.differentDaysByMillisecond(newstartdate, newenddate);
                    if ((curLimit > (defaults.dateLimit - 1)) || (defaults.onlyLimit && curLimit != (defaults.onlyLimit - 1))) {

                        if (defaults.onlyLimit){
                            layer.msg('只能查询' + defaults.dateLimit + '天范围的数据！');
                            this.dateRangePicker.$dateRangePicker.find('#savebutton').attr('disabled', true);
                            return;
                        }

                        layer.msg('最多只能查询' + defaults.dateLimit + '天范围的数据！');
                        this.dateRangePicker.$dateRangePicker.find('#savebutton').attr('disabled', true);
                    } else {
                        this.dateRangePicker.$select.val(start_time + '--' + end_time);
                        this.dateRangePicker.$dateRangePicker.find('#savebutton').attr('disabled', false);
                    }
                }

                this.drawDays();

            };

            Calendar.prototype.dateIsSelected = function (date) {
                return date.getTime() === this.date.getTime();
            };

            //当前日期高亮
            Calendar.prototype.dateIsSame = function (date) {
                var NowDate = new Date();
                var y = NowDate.getFullYear();
                var m = NowDate.getMonth() + 1;
                var d = NowDate.getDate();
                var time = y + '/' + m + '/' + d;
                return new Date(time).getTime() === date.getTime();
            };

            Calendar.prototype.dateIsInRange = function (date) {
                return date >= this.dateRangePicker.startDate() && date <= this.dateRangePicker.endDate();
            };

            Calendar.prototype.dayClass = function (day, firstDayOfMonth, lastDayOfMonth) {
                var classes, date;
                date = new Date(this.visibleYear(), this.visibleMonth() - 1, day);
                classes = '';

                if (this.dateIsSelected(date)) {
                    classes += ' drp-day-selected';
                } else if (this.dateIsInRange(date)) {
                    classes += ' drp-day-in-range';
                    if (date.getTime() === this.dateRangePicker.endDate().getTime()) {
                        classes += ' drp-day-last-in-range';
                    }
                } else if (this.isStartCalendar) {
                    if (date > this.dateRangePicker.endDate()) {
                        classes += ' drp-day-disabled';
                    }
                } else if (date < this.dateRangePicker.startDate()) {
                    classes += ' drp-day-disabled';
                }

                if (defaults.isOffLineReportFlag) {
                    // 大于当前时间的日期,都添加上disabled
                    if (defaults.greater) {
                        defaults.nowDate = new Date();
                    }
                    if (date > new Date(defaults.nowDate)) {
                        classes += ' drp-day-disabled';
                    }
                }

                if ((day + firstDayOfMonth - 1) % 7 === 0 || day === lastDayOfMonth) {
                    classes += ' drp-day-last-in-row';
                }
                if (this.dateIsSame(date)) {
                    classes += ' drp-day-highLight';
                }
                ;
                return classes;
            };

            Calendar.prototype.drawDays = function () {
                //移入当前月所有日期
                var firstDayOfMonth, i, lastDayOfMonth, self, _i, _j, _ref;
                self = this;
                this.$days.empty();//先移除日期
                firstDayOfMonth = this.firstDayOfMonth(this.visibleMonth(), this.visibleYear());
                lastDayOfMonth = this.daysInMonth(this.visibleMonth(), this.visibleYear());
                for (i = _i = 1, _ref = firstDayOfMonth - 1; _i <= _ref; i = _i += 1) {
                    this.$days.append($('<li class=\'drp-day drp-day-empty\'></li>'));
                }
                for (i = _j = 1; _j <= lastDayOfMonth; i = _j += 1) {
                    this.$days.append($('<li class=\'drp-day ' + (this.dayClass(i, firstDayOfMonth, lastDayOfMonth)) + '\'>' + i + '</li>'));
                }

                this.$calendar.find('.drp-day').mousedown(function (evt) {
                    defaults.isTimeLineClick = false;
                    var $this = $(this);
                    if ($this.hasClass('drp-day-empty')) {
                        return false;
                    }
                    ;
                    if (defaults.isShowHMS && !$this.hasClass('drp-day-disabled')) {
                        thisDay = parseInt($(this).text(), 10);
                        //日期点击事件
                        isDegFlag = true;
                        $this.css('overflow', 'visible').append('<div class="at12"><div class="pointer"></div></div>');
                        thisPointer = $this.find('.pointer');
                        this_pageX = evt.pageX;
                        this_pageY = evt.pageY;
                        var this_time;
                        if ($this.parent().parent().hasClass('drp-calendar-start')) {
                            this_time = defaults.start_time;
                            isArea = 0;//开始
                        } else if ($this.parent().parent().hasClass('drp-calendar-end')) {
                            this_time = defaults.end_time;
                            isArea = 1;//结束
                        }
                        ;
                        thisPointer.parent('div').parent('li').attr('data-settime', this_time);
                        var this_time_array = this_time.split(':');
                        var this_hour = parseInt(this_time_array[0]);
                        var this_deg = 360 / 23 * this_hour;
                        thisPointer.css({
                            'transform': 'rotate(' + (this_deg - 90) + 'deg)'
                        });
                    }
                }).mouseup(function (evt) {
                    isDegFlag = false;
                    var day;
                    if ($(this).hasClass('drp-day-disabled')) {
                        return false;
                    }
                    day = parseInt($(this).text(), 10);
                    if (isNaN(day)) {
                        return false;
                    }
                    return self.setDay(day);
                });
                //转动转盘
                $(document).mousemove(function (e) {

                    if (isDegFlag) {
                        var poorY = parseInt((e.pageY - this_pageY) / 10);
                        var poorX = parseInt((e.pageX - this_pageX) / 10);
                        if (Math.abs(poorY) > 1 || Math.abs(poorX) > 1) {
                            var S = (e.pageX - this_pageX);
                            var X = (this_pageY - e.pageY);
                            var T = 90 / Math.atan(1 / 0);
                            var P = Math.atan(S / X) * T;
                            if (X < 0 && S > 0) {
                                P = 180 + P;
                            }
                            ;
                            if (X < 0 && S <= 0) {
                                P = 180 + P;
                            }
                            ;
                            if (X >= 0 && S < 0) {
                                P = 360 + P;
                            }
                            ;
                            var this_time;
                            if (isArea == 0) {
                                this_time = defaults.start_time;
                            } else if (isArea == 1) {
                                this_time = defaults.end_time;
                            }
                            ;
                            var this_time_array = this_time.split(':');
                            for (var i = 0; i < this_time_array.length; i++) {
                                this_time_array[i] = parseInt(this_time_array[i]);
                            }
                            ;
                            var this_hour;
                            var this_minutes;
                            var this_seconds;
                            if (Math.abs(poorY) > 11 || Math.abs(poorX) > 11) {
                                thisPointer.parent('div').attr('class', 'at36');
                                this_seconds = Math.round(59 * P / 360);
                                this_time_array[2] = this_seconds;
                            } else if ((Math.abs(poorY) > 5 && Math.abs(poorY) < 12) || (Math.abs(poorX) > 5 && Math.abs(poorX) < 12)) {
                                thisPointer.parent('div').attr('class', 'at24');
                                this_minutes = Math.round(59 * P / 360);
                                this_time_array[1] = this_minutes;
                            } else {
                                thisPointer.parent('div').attr('class', 'at12');
                                this_hour = Math.round(23 * P / 360);
                                this_time_array[0] = this_hour;
                            }
                            ;


                            var timeString = (this_time_array[0] < 10 ? '0' + this_time_array[0] : this_time_array[0]) + ':' + (this_time_array[1] < 10 ? '0' + this_time_array[1] : this_time_array[1]) + ':' + (this_time_array[2] < 10 ? '0' + this_time_array[2] : this_time_array[2]);
                            if (isArea == 0) {
                                defaults.start_time = timeString;
                            } else if (isArea == 1) {
                                defaults.end_time = timeString;
                            }
                            ;

                            //this.dateRangePicker.checktime();
                            thisPointer.parent('div').parent('li').attr('data-settime', timeString);
                            thisPointer.css({
                                'transform': 'rotate(' + (P - 90) + 'deg)'
                            });
                        }
                        ;
                    }
                    ;
                }).mouseup(function () {
                    if (isDegFlag) {
                        isDegFlag = false;
                        return self.setDay(thisDay);
                    }
                    ;
                });
            };

            //判断所选时间范围（相差几天）
            Calendar.prototype.differentDaysByMillisecond = function (date1, date2) {
                console.log(date1.getTime());
                days = ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24));
                return Math.floor(days);
            };
            Calendar.prototype.drawDateDisplay = function () {

                //底部时间赋值
                var this_time = '';
                if (defaults.isShowHMS) {
                    if (this.$dateDisplay.parent().parent().hasClass('drp-calendar-start')) {
                        this_time = defaults.start_time;

                        isFilterDate = false; //先遍历的开始时间 isFilterDate设为false wjk

                        if (this.dateRangePicker.formatDate(newstartdate).substring(0, 11) == this.dateRangePicker.formatDate(newenddate).substring(0, 11)) {
                            if (this.changetime(defaults.start_time) > this.changetime(defaults.end_time)) {
                                layer.msg('开始时间不能大于结束时间');
                                // newstartdate = new Date(this.$dateDisplay.val());

                                //IE兼容日期 wjk 2018/09/29
                                var date = new Date(this.$dateDisplay.val().replace(/-/g, '/'));
                                newstartdate = new Date(date);

                                return false;
                            } 

                                //修改时间后要更改newstartdate的值wjk
                                var _formateDate3 = [this.year() < 10 ? '0' + this.year() : this.year(), this.month() < 10 ? '0' + this.month() : this.month(), this.day() < 10 ? '0' + this.day() : this.day()].join('-') + ' ' + this_time;
                                // newstartdate = new Date(_formateDate3)
                                //IE兼容日期 wjk 2018/09/29
                                var date = new Date(_formateDate3.replace(/-/g, '/'));
                                newstartdate = new Date(date);


                                defaults.blTimeUpdate = true;


                                return this.$dateDisplay.val([this.year() < 10 ? '0' + this.year() : this.year(), this.month() < 10 ? '0' + this.month() : this.month(), this.day() < 10 ? '0' + this.day() : this.day()].join('-') + ' ' + this_time);
                            

                        } 
                            //修改时间后要更改newstartdate的值wjk
                            // 更新左边底部时间

                            var _formateDate = [this.year() < 10 ? '0' + this.year() : this.year(), this.month() < 10 ? '0' + this.month() : this.month(), this.day() < 10 ? '0' + this.day() : this.day()].join('-') + ' ' + this_time;

                            var RU_time = $('.drp-calendar-end .drp-calendar-date').val();

                            var start_time = this.dateRangePicker.$dateRangePicker.find('.drp-calendar-start .drp-calendar-date').val();
                            var end_time = this.dateRangePicker.$dateRangePicker.find('.drp-calendar-end .drp-calendar-date').val();

                            console.log(start_time + ' -- ' + end_time);

                            if (this.changetime(_formateDate) > this.changetime(RU_time)) {
                                layer.msg('开始时间不能大于结束时间');
                                return false;
                            } 

                                //IE兼容日期 wjk 2018/09/29
                                var date = new Date(_formateDate.replace(/-/g, '/'));
                                newstartdate = new Date(date);

                                defaults.blTimeUpdate = true;
                                return this.$dateDisplay.val(_formateDate);
                            
                        

                    } else if (this.$dateDisplay.parent().parent().hasClass('drp-calendar-end')) {
                        this_time = defaults.end_time;


                        isFilterDate = true; //再次遍历的结束时间 isFilterDate设为true wjk

                        if (this.dateRangePicker.formatDate(newstartdate).substring(0, 11) == this.dateRangePicker.formatDate(newenddate).substring(0, 11)) {
                            if (this.changetime(defaults.start_time) > this.changetime(defaults.end_time)) {
                                layer.msg('开始时间不能大于结束时间');
                                // newenddate = new Date(this.$dateDisplay.val());
                                //IE兼容日期 wjk 2018/09/29
                                var date = new Date(this.$dateDisplay.val().replace(/-/g, '/'));
                                newenddate = new Date(date);
                                return false;
                            } 

                                //修改时间后要更改newenddate的值wjk
                                var _formateDate2 = [this.year() < 10 ? '0' + this.year() : this.year(), this.month() < 10 ? '0' + this.month() : this.month(), this.day() < 10 ? '0' + this.day() : this.day()].join('-') + ' ' + this_time;
                                // newenddate = new Date(_formateDate2)
                                //IE兼容日期 wjk 2018/09/29
                                var date = new Date(_formateDate2.replace(/-/g, '/'));
                                newenddate = new Date(date);

                                defaults.brTimeUpdate = true;

                                return this.$dateDisplay.val([this.year() < 10 ? '0' + this.year() : this.year(), this.month() < 10 ? '0' + this.month() : this.month(), this.day() < 10 ? '0' + this.day() : this.day()].join('-') + ' ' + this_time);

                            

                        } 

                            //修改时间后要更改newenddate的值wjk
                            // 更新右边底部时间

                            var _formateDate = [this.year() < 10 ? '0' + this.year() : this.year(), this.month() < 10 ? '0' + this.month() : this.month(), this.day() < 10 ? '0' + this.day() : this.day()].join('-') + ' ' + this_time;
                            var RL_time = $('.drp-calendar-start .drp-calendar-date').val();

                            //IE兼容日期 wjk 2018/09/29
                            var date = new Date(_formateDate.replace(/-/g, '/'));
                            newenddate = new Date(date);

                            defaults.brTimeUpdate = true;
                            return this.$dateDisplay.val(_formateDate);

                        

                    }
                    ;
                } else { //不显示时分秒 author wjk 2018/10/29
                    if (this.$dateDisplay.parent().parent().hasClass('drp-calendar-start')) {

                        isFilterDate = false; //先遍历的开始时间 isFilterDate设为false wjk

                        if (this.dateRangePicker.formatDate(newstartdate).substring(0, 11) == this.dateRangePicker.formatDate(newenddate).substring(0, 11)) {

                            //修改时间后要更改newstartdate的值wjk
                            var _formateDate3 = [this.year() < 10 ? '0' + this.year() : this.year(), this.month() < 10 ? '0' + this.month() : this.month(), this.day() < 10 ? '0' + this.day() : this.day()].join('-');
                            //IE兼容日期 wjk 2018/09/29
                            var date = new Date(_formateDate3.replace(/-/g, '/'));
                            newstartdate = new Date(date);
                            defaults.blTimeUpdate = true;
                            return this.$dateDisplay.val([this.year() < 10 ? '0' + this.year() : this.year(), this.month() < 10 ? '0' + this.month() : this.month(), this.day() < 10 ? '0' + this.day() : this.day()].join('-'));

                        } 
                            //修改时间后要更改newstartdate的值wjk
                            // 更新左边底部时间

                            var _formateDate = [this.year() < 10 ? '0' + this.year() : this.year(), this.month() < 10 ? '0' + this.month() : this.month(), this.day() < 10 ? '0' + this.day() : this.day()].join('-');

                            var RU_time = $('.drp-calendar-end .drp-calendar-date').val();

                            var start_time = this.dateRangePicker.$dateRangePicker.find('.drp-calendar-start .drp-calendar-date').val();
                            var end_time = this.dateRangePicker.$dateRangePicker.find('.drp-calendar-end .drp-calendar-date').val();

                            console.log(start_time + ' -- ' + end_time);

                            if (this.changetime(_formateDate) > this.changetime(RU_time)) {
                                layer.msg('开始时间不能大于结束时间');
                                return false;
                            } 

                                //IE兼容日期 wjk 2018/09/29
                                var date = new Date(_formateDate.replace(/-/g, '/'));
                                newstartdate = new Date(date);

                                defaults.blTimeUpdate = true;
                                return this.$dateDisplay.val(_formateDate);
                            
                        

                    } else if (this.$dateDisplay.parent().parent().hasClass('drp-calendar-end')) {
                        isFilterDate = true; //再次遍历的结束时间 isFilterDate设为true wjk

                        if (this.dateRangePicker.formatDate(newstartdate).substring(0, 11) == this.dateRangePicker.formatDate(newenddate).substring(0, 11)) {

                            //修改时间后要更改newenddate的值wjk
                            var _formateDate2 = [this.year() < 10 ? '0' + this.year() : this.year(), this.month() < 10 ? '0' + this.month() : this.month(), this.day() < 10 ? '0' + this.day() : this.day()].join('-');
                            //IE兼容日期 wjk 2018/09/29
                            var date = new Date(_formateDate2.replace(/-/g, '/'));
                            newenddate = new Date(date);

                            defaults.brTimeUpdate = true;

                            return this.$dateDisplay.val([this.year() < 10 ? '0' + this.year() : this.year(), this.month() < 10 ? '0' + this.month() : this.month(), this.day() < 10 ? '0' + this.day() : this.day()].join('-'));

                        } 

                            //修改时间后要更改newenddate的值wjk
                            // 更新右边底部时间
                            var _formateDate = [this.year() < 10 ? '0' + this.year() : this.year(), this.month() < 10 ? '0' + this.month() : this.month(), this.day() < 10 ? '0' + this.day() : this.day()].join('-');
                            var RL_time = $('.drp-calendar-start .drp-calendar-date').val();

                            //IE兼容日期 wjk 2018/09/29
                            var date = new Date(_formateDate.replace(/-/g, '/'));
                            newenddate = new Date(date);

                            defaults.brTimeUpdate = true;
                            return this.$dateDisplay.val(_formateDate);

                        

                    }
                    ;
                }
                ;


            };

            Calendar.prototype.month = function () {
                return this.date.getMonth() + 1;
            };

            Calendar.prototype.day = function () {
                return this.date.getDate();
            };

            Calendar.prototype.dayOfWeek = function () {
                return this.date.getDay() + 1;
            };

            Calendar.prototype.year = function () {
                return this.date.getFullYear();
            };

            Calendar.prototype.visibleMonth = function () {
                return this._visibleMonth;
            };

            Calendar.prototype.visibleYear = function () {
                return this._visibleYear;
            };

            Calendar.prototype.nameOfMonth = function (month) {
                return MONTHS[month - 1];
            };

            Calendar.prototype.firstDayOfMonth = function (month, year) {
                return new Date(year, month - 1, 1).getDay() + 1;
            };

            Calendar.prototype.daysInMonth = function (month, year) {
                month || (month = this.visibleMonth());
                year || (year = this.visibleYear());
                return new Date(year, month, 0).getDate();
            };
            Calendar.prototype.changetime = function (time) {
                if (time.indexOf('-') != -1) {
                    var time = new Date(time.replace('-', '/'));
                    return time.getTime();
                } 
                    var time = time.split(':');
                    return parseInt(time[0]) * 3600 + parseInt(time[1]) * 60 + parseInt(time[2]);
                
            };

            //比较两个时间差多少天 wjk
            Calendar.prototype.datedifference = function (sDate1, sDate2) {
                var dateSpan,
                    tempDate,
                    iDays;
                sDate1 = Date.parse(sDate1);
                sDate2 = Date.parse(sDate2);
                dateSpan = sDate2 - sDate1;
                if (dateSpan < 0) {
                    return false;
                } 
                    // dateSpan = Math.abs(dateSpan);
                    iDays = Math.floor(dateSpan / (24 * 3600 * 1000));
                    // return iDays
                    return (iDays + 1); //加一才是正常天数
                
            };

            return Calendar;

        }());

        timepiker = (function () {
            function timepiker(lastelement) {
                var that = this;
                var element = '<div class=\'ui-laydate ui-laydate-range date-theme-molv\' style=\'border:1px solid #d2d2d2;height:325px\'>' +
                    '<div class=\'ui-laydate-main date-main-list-0 date-time-show\' style=\'float:left\'>' +
                    '<div class=\'ui-laydate-header\'>' +
                    '<div class=\'date-set-ym\'>' +
                    '<span class=\'date-time-text\'>开始时间</span>' +
                    '</div>' +
                    '</div>' +
                    '<div class=\'ui-laydate-content\' style=\'background-color:#fff;height:232px\'>' +
                    '<ul class="ui-laydate-list date-time-list" style="padding:10px" >' +
                    '<li class="timepicker-hour"><p>时</p><ol><li class="ui-this">00</li><li class="">01</li><li class="">02</li><li class="">03</li><li class="">04</li><li class="">05</li><li class="">06</li><li class="">07</li><li class="">08</li><li class="">09</li><li class="">10</li><li class="">11</li><li class="">12</li><li class="">13</li><li class="">14</li><li class="">15</li><li class="">16</li><li class="">17</li><li class="">18</li><li class="">19</li><li class="">20</li><li class="">21</li><li class="">22</li><li class="">23</li></ol></li>' +
                    '<li class="timepicker-minite"><p>分</p><ol><li class="ui-this">00</li><li class="">01</li><li class="">02</li><li class="">03</li><li class="">04</li><li class="">05</li><li class="">06</li><li class="">07</li><li class="">08</li><li class="">09</li><li class="">10</li><li class="">11</li><li class="">12</li><li class="">13</li><li class="">14</li><li class="">15</li><li class="">16</li><li class="">17</li><li class="">18</li><li class="">19</li><li class="">20</li><li class="">21</li><li class="">22</li><li class="">23</li><li class="">24</li><li class="">25</li><li class="">26</li><li class="">27</li><li class="">28</li><li class="">29</li><li class="">30</li><li class="">31</li><li class="">32</li><li class="">33</li><li class="">34</li><li class="">35</li><li class="">36</li><li class="">37</li><li class="">38</li><li class="">39</li><li class="">40</li><li class="">41</li><li class="">42</li><li class="">43</li><li class="">44</li><li class="">45</li><li class="">46</li><li class="">47</li><li class="">48</li><li class="">49</li><li class="">50</li><li class="">51</li><li class="">52</li><li class="">53</li><li class="">54</li><li class="">55</li><li class="">56</li><li class="">57</li><li class="">58</li><li class="">59</li></ol></li>' +
                    '<li class="timepicker-seconds"><p>秒</p><ol><li class="ui-this">00</li><li class="">01</li><li class="">02</li><li class="">03</li><li class="">04</li><li class="">05</li><li class="">06</li><li class="">07</li><li class="">08</li><li class="">09</li><li class="">10</li><li class="">11</li><li class="">12</li><li class="">13</li><li class="">14</li><li class="">15</li><li class="">16</li><li class="">17</li><li class="">18</li><li class="">19</li><li class="">20</li><li class="">21</li><li class="">22</li><li class="">23</li><li class="">24</li><li class="">25</li><li class="">26</li><li class="">27</li><li class="">28</li><li class="">29</li><li class="">30</li><li class="">31</li><li class="">32</li><li class="">33</li><li class="">34</li><li class="">35</li><li class="">36</li><li class="">37</li><li class="">38</li><li class="">39</li><li class="">40</li><li class="">41</li><li class="">42</li><li class="">43</li><li class="">44</li><li class="">45</li><li class="">46</li><li class="">47</li><li class="">48</li><li class="">49</li><li class="">50</li><li class="">51</li><li class="">52</li><li class="">53</li><li class="">54</li><li class="">55</li><li class="">56</li><li class="">57</li><li class="">58</li><li class="">59</li></ol>' +
                    '</li>' +
                    '</ul>' +
                    '</div>' +
                    '</div>' +
                    '<div class=\'ui-laydate-main date-main-list-1 date-time-show\' style=\'float:left\'>' +
                    '<div class=\'ui-laydate-header\'>' +
                    '<div class=\'date-set-ym\'>' +
                    '<span class=\'date-time-text\'>结束时间</span>' +
                    '</div>' +
                    '</div>' +
                    '<div class=\'ui-laydate-content\' style=\'background-color:#fff;height:232px\'>' +
                    '<ul class="ui-laydate-list date-time-list" style="padding:10px">' +
                    '<li class="timepicker-hour2"><p>时</p><ol><li class="ui-this">00</li><li class="">01</li><li class="">02</li><li class="">03</li><li class="">04</li><li class="">05</li><li class="">06</li><li class="">07</li><li class="">08</li><li class="">09</li><li class="">10</li><li class="">11</li><li class="">12</li><li class="">13</li><li class="">14</li><li class="">15</li><li class="">16</li><li class="">17</li><li class="">18</li><li class="">19</li><li class="">20</li><li class="">21</li><li class="">22</li><li class="">23</li></ol></li>'
                    + '<li class="timepicker-minite2"><p>分</p><ol><li class="ui-this">00</li><li class="">01</li><li class="">02</li><li class="">03</li><li class="">04</li><li class="">05</li><li class="">06</li><li class="">07</li><li class="">08</li><li class="">09</li><li class="">10</li><li class="">11</li><li class="">12</li><li class="">13</li><li class="">14</li><li class="">15</li><li class="">16</li><li class="">17</li><li class="">18</li><li class="">19</li><li class="">20</li><li class="">21</li><li class="">22</li><li class="">23</li><li class="">24</li><li class="">25</li><li class="">26</li><li class="">27</li><li class="">28</li><li class="">29</li><li class="">30</li><li class="">31</li><li class="">32</li><li class="">33</li><li class="">34</li><li class="">35</li><li class="">36</li><li class="">37</li><li class="">38</li><li class="">39</li><li class="">40</li><li class="">41</li><li class="">42</li><li class="">43</li><li class="">44</li><li class="">45</li><li class="">46</li><li class="">47</li><li class="">48</li><li class="">49</li><li class="">50</li><li class="">51</li><li class="">52</li><li class="">53</li><li class="">54</li><li class="">55</li><li class="">56</li><li class="">57</li><li class="">58</li><li class="">59</li></ol></li>'
                    + '<li class="timepicker-seconds2"><p>秒</p><ol><li class="ui-this">00</li><li class="">01</li><li class="">02</li><li class="">03</li><li class="">04</li><li class="">05</li><li class="">06</li><li class="">07</li><li class="">08</li><li class="">09</li><li class="">10</li><li class="">11</li><li class="">12</li><li class="">13</li><li class="">14</li><li class="">15</li><li class="">16</li><li class="">17</li><li class="">18</li><li class="">19</li><li class="">20</li><li class="">21</li><li class="">22</li><li class="">23</li><li class="">24</li><li class="">25</li><li class="">26</li><li class="">27</li><li class="">28</li><li class="">29</li><li class="">30</li><li class="">31</li><li class="">32</li><li class="">33</li><li class="">34</li><li class="">35</li><li class="">36</li><li class="">37</li><li class="">38</li><li class="">39</li><li class="">40</li><li class="">41</li><li class="">42</li><li class="">43</li><li class="">44</li><li class="">45</li><li class="">46</li><li class="">47</li><li class="">48</li><li class="">49</li><li class="">50</li><li class="">51</li><li class="">52</li><li class="">53</li><li class="">54</li><li class="">55</li><li class="">56</li><li class="">57</li><li class="">58</li><li class="">59</li></ol></li></ul>'
                    + '</div>'
                    + '</div>'
                    + '<div class="ui-laydate-footer"><span lay-type="datetime" class="date-btns-time"></span><div class="date-footer-btns"><span lay-type="confirm" class="date-btns-cancel">取消</span><span lay-type="confirm" class="date-btns-confirm">确定</span></div></div>'
                    + '</div>';
                this.element = $(element);
                this.element.click(function (evt) {
                    return evt.stopPropagation();
                });
                this.hidden = true;

                function time(h, m, s) {
                    this.h = h;
                    this.m = m;
                    this.s = s;
                }

                var time1 = new time('00', '00', '00');
                var time2 = new time('00', '00', '00');
                that.startime = time1.h + ':' + time1.m + ':' + time1.s;
                that.endtime = time2.h + ':' + time2.m + ':' + time2.s;
                this.element.click(function (evt) {
                    return evt.stopPropagation();
                });
                /*	  this.element.find(".ui-laydate-list").find("ol").each(function(){
                 $(this). find("li").each(function(){
                     $(this).unbind().on("click",function(){
                         $(this).siblings().removeClass("ui-this");
                            $(this).addClass("ui-this");
                     })
              })})*/
                this.element.unbind().on('click', function (evt) {
                    if (evt.target.className === 'date-btns-confirm') {

                        that.confirm(that, lastelement);
                    } else if (evt.target.localName == 'li') {
                        var dd = $(evt.target).parent().parent().parent().hasClass('ui-laydate-list');
                        if (dd) {
                            $(evt.target).siblings().removeClass('ui-this');
                            $(evt.target).addClass('ui-this');
                        }


                    } else if (evt.target.className === 'date-btns-cancel') {
                        that.hide(that, lastelement);
                    }
                });


            }

            timepiker.prototype.confirm = function (that, lastelement) {
                var timelist = [];
                $('.ui-this').each(function () {
                    timelist.push($(this).text());
                });
                defaults.start_time = timelist[0] + ':' + timelist[1] + ':' + timelist[2];
                defaults.end_time = timelist[3] + ':' + timelist[4] + ':' + timelist[5];
                var startdate = lastelement.$dateRangePicker.find('.drp-calendar-date').eq(0).val().substring(0, 11);
                var enddate = lastelement.$dateRangePicker.find('.drp-calendar-date').eq(1).val().substring(0, 11);
                var date1 = startdate + '' + timelist[0] + ':' + timelist[1] + ':' + timelist[2];
                var date2 = enddate + '' + timelist[3] + ':' + timelist[4] + ':' + timelist[5];

                //layer.msg('开始时间不能大于结束时间');
                if (date1 > date2) {
                    layer.msg('开始时间不能大于结束时间');
                    return;
                }

                //选择具体时间后比较两个时间的差值 wjk 2018/09/29
                var compareStartTime = date1.split(' ')[0].replace(/-/g, '/');
                var compareEndTime = date2.split(' ')[0].replace(/-/g, '/');
                var discrepancyTime = this.datedifference(compareStartTime, compareEndTime);

                if (defaults.onlyLimit && discrepancyTime != defaults.dateLimit){
                    layer.msg('只能查询' + defaults.dateLimit + '天范围的数据！');
                    return;
                }

                if (discrepancyTime > defaults.dateLimit) {
                    layer.msg('最多只能查询' + defaults.dateLimit + '天范围的数据！');
                    return;
                }


                defaults.startdate = date1;
                defaults.enddate = date2;

                lastelement.$dateRangePicker.find('.drp-calendar-date').eq(0).val(date1);
                lastelement.$dateRangePicker.find('.drp-calendar-date').eq(1).val(date2);
                lastelement.$select.val(defaults.startdate + '--' + defaults.enddate);

                // bottomLeftStartTime =  defaults.startdate;
                // bottomRightStartTime = defaults.enddate;
                this.hide();
            };

            //比较两个时间差多少天 wjk 2018/09/29
            timepiker.prototype.datedifference = function (sDate1, sDate2) {
                var dateSpan,
                    tempDate,
                    iDays;
                sDate1 = Date.parse(sDate1);
                sDate2 = Date.parse(sDate2);
                dateSpan = sDate2 - sDate1;
                dateSpan = Math.abs(dateSpan);
                iDays = Math.floor(dateSpan / (24 * 3600 * 1000));
                // return iDays
                return (iDays + 1); //加一才是正常天数
            };

            timepiker.prototype.hide = function (that, lastelement) {

                this.element.hide();
            };

            timepiker.prototype.show = function (that, lastelement) {
                var _total = lastelement.$select.val().split('--');

                var _startTime = _total[0].split(' ')[1].split(':');
                var _endTime = _total[1].split(' ')[1].split(':');
                var _time = _startTime.concat(_endTime);

                var hour, minite, seconds, hour2, minite2, seconds2;
                hour = lastelement.$dateRangePicker.find('.timepicker-hour');
                minite = lastelement.$dateRangePicker.find('.timepicker-minite');
                seconds = lastelement.$dateRangePicker.find('.timepicker-seconds');
                hour2 = lastelement.$dateRangePicker.find('.timepicker-hour2');
                minite2 = lastelement.$dateRangePicker.find('.timepicker-minite2');
                seconds2 = lastelement.$dateRangePicker.find('.timepicker-seconds2');

                var eleArray = [hour, minite, seconds, hour2, minite2, seconds2];
                for (var i = 0; i < eleArray.length; i++) {
                    eleArray[i].find('li').removeClass('ui-this').each(function (index, ele) {
                        var $ele = $(ele);
                        if ($ele.html() == _time[i]) {
                            $ele.addClass('ui-this');
                            eleArray[i].find('ol').animate({
                                scrollTop: (index * 30)
                            });
                            return false;
                        }
                    });
                }

                this.element.show();
                //newtime=new timepiker(self);
            };


            return timepiker;
        }());

        return new DateRangePicker($selectorOuter, options);
    };
    $.fn.dateRangePicker = function (options) {
        return wraper(this, options);
    };

}(jQuery));