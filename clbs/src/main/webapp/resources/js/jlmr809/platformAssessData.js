// 企业信息数据
(function (window, $) {
    var exportParam = null;
    var messageInfo = $('#platformAssessMessage');// 请求返回提示
    var exportBtn = $('#exportplatformAssessData');// 导出按钮
    var startTime;
    var endTime;

    platformAssessData = {
        /**
         * 日历相关方法
         * */
        timeRender: function () {
            var nowDate = new Date();
            var year = nowDate.getFullYear();
            var month = nowDate.getMonth() + 1;
            var day = nowDate.getDate();
            month = month < 10 ? '0' + month : month;
            day = day < 10 ? '0' + day : day;
            var nowDay = year + '-' + month + '-' + day;
            var maxTime = new Date(nowDate.getTime() + 24 * 60 * 60 * 1000).toLocaleDateString();
            // 年
            laydate.render({
                elem: '#yearTimeInput',
                type: 'year',
                theme: '#6dcff6',
                max: maxTime
            });
            $('#yearTimeInput').val(year);
            // 季度
            refactoringDate.renderSeasonDate('#quarterTimeInput', 1, maxTime);
            $('#quarterTimeInput').val(year + '年1季度');
            // 月
            laydate.render({
                elem: '#monthTimeInput',
                type: 'month',
                theme: '#6dcff6',
                max: maxTime
            });
            $('#monthTimeInput').val(year + '-' + month);
            // 周
            var startTime = new Date(nowDate.getTime() - 6 * 24 * 60 * 60 * 1000);
            var startYear = startTime.getFullYear();
            var startMonth = startTime.getMonth() + 1;
            var startDate = startTime.getDate();
            startMonth = startMonth < 10 ? '0' + startMonth : startMonth;
            startDate = startDate < 10 ? '0' + startDate : startDate;
            var weekDate = startYear + '-' + startMonth + '-' + startDate + ' -- ' + nowDay;
            // $("#weekTimeInput").attr("lay-data", "{autoWeek: true}");
            $('#weekTimeInput').val(weekDate);
            /*laydate.render({
                elem: '#weekTimeInput',
                range: '--',
                max: maxTime,
                weekStart: 1,
                ready: function () {
                    var key = $(this.elem[0]).attr('lay-key');
                    var insTemp = laydatePro.getInstance(key);
                    console.log('insTemp',insTemp);
                    if (insTemp.config.autoWeek) {
                        $('#layui-laydate' + key).on('click', '.layui-laydate-content td:not(.laydate-disabled)', function () {
                            var tds = $(this).parent().find('td:not(.laydate-disabled)');
                            insTemp.setValue(weekDate);
                            insTemp.choose($(this));
                            insTemp.choose(tds.first());
                            insTemp.choose(tds.last());
                        });
                    }
                },
                change:function (value) {
                    console.log('value',value);
                }
            });*/
            $('#weekTimeInput').dateRangePicker({
                dateLimit: 7,
                onlyLimit: 7,
                isOffLineReportFlag: true,
                start_date: startYear + '-' + startMonth + '-' + startDate,
                end_date: nowDay,
                isShowHMS: false
            });
            // 日
            laydate.render({
                elem: '#dateTimeInput',
                type: 'date',
                theme: '#6dcff6',
                value: nowDay,
                max: maxTime
            });
            $('#dateTimeInput').val(nowDay);
        },
        timeTypeChange: function () {
            var curVal = $(this).val();
            var timeType = '';
            switch (curVal) {
                case '1':
                    timeType = '#yearTimeInput';
                    break;
                case '2':
                    timeType = '#quarterTimeInput';
                    break;
                case '3':
                    timeType = '#monthTimeInput';
                    break;
                case '4':
                    timeType = '#weekTimeInput';
                    break;
                case '5':
                    timeType = '#dateTimeInput';
                    break;
                default:
                    break;
            }
            $(timeType).prop('disabled', false).show().siblings().prop('disabled', true).hide();
        },
        // 获取当前查询时间
        getRenderTime: function () {
            var curVal = $('#timeType').val();
            switch (curVal) {
                case '1':// 年
                    startTime = $('#yearTimeInput').val() + '0101';
                    endTime = $('#yearTimeInput').val() + '1231';
                    break;
                case '2':// 季度
                    var curQuarterVal = $('#quarterTimeInput').val();
                    var year = curQuarterVal.substr(0, 4);
                    var quarter = curQuarterVal.substr(-3, 1);// 第几季度
                    if (quarter == '1') {
                        startTime = year + '0101';
                        endTime = year + '0331';
                    } else if (quarter == '2') {
                        startTime = year + '0430';
                        endTime = year + '0630';
                    } else if (quarter == '3') {
                        startTime = year + '0731';
                        endTime = year + '0930';
                    } else if (quarter == '4') {
                        startTime = year + '1031';
                        endTime = year + '1231';
                    }
                    break;
                case '3':// 月
                    var curTime = $('#monthTimeInput').val().replace('-', '');
                    // 获取当月天数
                    var monthDate = laydate.getEndDate(parseInt(curTime.substr(-1, 2)), curTime.substr(0, 4));
                    startTime = curTime + '01';
                    endTime = curTime + monthDate;
                    break;
                case '4':// 周
                    var curTime = $('#weekTimeInput').val().split('--');
                    startTime = curTime[0].replace(/-/g, '');
                    endTime = curTime[1].replace(/-/g, '');
                    break;
                case '5':// 日
                    var curTime = $('#dateTimeInput').val().replace(/-/g, '');
                    startTime = curTime;
                    endTime = curTime;
                    break;
            }
        },
        /**
         * 数据查询
         * */
        platformAssessDataSearch: function () {
            if (!platformAssessData.validates()) {
                return;
            }
            platformAssessData.getRenderTime();
            messageInfo.html('数据请求中...');
            exportBtn.prop('disabled', true);
            var url = '/clbs/jl/vehicle/platform/dataReleased';
            var param = {
                'accountNo': $('#accountNo').val(),
                'timeType': $('#timeType').val(),
                'startTime': startTime,
                'endTime': endTime,
            };
            json_ajax("post", url, "json", false, param, function (data) {
                exportParam = null;
                $('#platformAssessDataTable td').html('--');
                if (data.success) {
                    var info = data.obj;
                    if (info.result == '1') {
                        var newInfo = info.info;
                        exportParam = newInfo;
                        exportBtn.prop('disabled', false);
                        for (key in newInfo) {
                            $('#pa-' + key).html(newInfo[key]);
                        }
                        messageInfo.html(info.info.sendTime + '下发');
                    } else {
                        messageInfo.html(data.obj.msg);
                    }
                } else if (data.msg) {
                    messageInfo.html(data.msg);
                }
            });
        },
        validates: function () {
            return $("#platformAssessFormData").validate({
                rules: {
                    offOperatePlatform: {
                        required: true
                    },
                    accountNo: {
                        required: true
                    },
                    time: {
                        required: true
                    },
                },
                messages: {
                    offOperatePlatform: {
                        required: '请选择平台名称'
                    },
                    accountNo: {
                        required: '请输入平台账号'
                    },
                    time: {
                        required: '请选择时间'
                    },
                }
            }).form();
        },
        exportOrgData: function () {
            if (!exportParam) {
                return;
            }
            var url = '/clbs/jl/vehicle/platform/export';
            exportExcelUseFormGet(url, exportParam);
        },
    };
    $(function () {
        platformAssessData.timeRender();
        $('#timeType').on("change", platformAssessData.timeTypeChange);// 时间类型切换
        $("#platformAssessDataSearch").bind("click", platformAssessData.platformAssessDataSearch);
        exportBtn.bind("click", platformAssessData.exportOrgData);// 导出
    })
}(window, $))