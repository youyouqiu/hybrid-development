(function (window, $) {
    var resultList = JSON.parse($('#resultList').val());
    var realTimeList = {};// 状态信息数据
    var alarmList = {};// 报警记录数据
    var activeSafetyList = {};// 主动安全数据
    var obdList = {};// OBD数据数据
    var logList = {};// 日志记录数据
    var popupList = {}; // 动态弹框
    var isNullTab = [];// 一项显示列都未勾选的页签

    columnSetting = {
        init: function () {
            console.log(resultList, '{{{');
            for (var i = 0; i < resultList.length; i++) {
                switch (resultList[i].mark) {
                    case 'REALTIME_MONITORING_LIST':
                        realTimeList = resultList[i];
                        columnSetting.initTabHtml(realTimeList, 'realTimeTab');
                        break;
                    case 'REALTIME_MONITORING_ALARM_LIST':
                        alarmList = resultList[i];
                        columnSetting.initTabHtml(alarmList, 'alarmTab');
                        break;
                    case 'REALTIME_MONITORING_ACTIVE_SAFETY_LIST':
                        activeSafetyList = resultList[i];
                        columnSetting.initTabHtml(activeSafetyList, 'safetyTab');
                        break;
                    case 'REALTIME_MONITORING_OBD_LIST':
                        obdList = resultList[i];
                        columnSetting.initTabHtml(obdList, 'obdInfoTab');
                        break;
                    case 'REALTIME_MONITORING_LOG_LIST':
                        logList = resultList[i];
                        columnSetting.initTabHtml(logList, 'operationLogTab');
                        break;
                    case 'AlERT_WINDOW_REALTIME_DATA_LIST':
                        popupList =  resultList[i];
                        columnSetting.initTabHtml(popupList, 'popupTab');
                        break;
                    default:
                        break;
                }
            }
        },
        // 显示项初始化
        initTabHtml: function (arrList, targetDiv) {
            var tabBox = $('#' + targetDiv);
            var dataList = arrList.dataList;
            var html = '';
            var checked = 0;
            for (var i = 0; i < dataList.length; i++) {
                html += '<div class="checkLabel">';
                if (dataList[i].status == '0') {
                    html += '<input class="columnCheck" type="checkbox" data-index="' + i + '" data-mark="' + arrList.mark + '" checked>';
                    checked ++;
                } else {
                    html += '<input class="columnCheck" type="checkbox" data-index="' + i + '" data-mark="' + arrList.mark + '">';
                }
                html += '<span class="columnTitle"> ' + dataList[i].title + '</span></div>';
            }
            tabBox.find('.checkBox').html(html);// 显示项
            tabBox.find('input[value=' + arrList.fixSize + ']').prop('checked', true);//锁定列
            if (checked === dataList.length){
                tabBox.find('.allCheck').prop('checked', true);
            }
        },
        // 列排序
        sortUpOrDown: function (e, type) {
            var curActiveLab = $(e.target).closest('.tab-pane').find('.checkLabel.active');
            if (curActiveLab.length == 0) return;
            if (type == 'up') {
                curActiveLab.insertBefore(curActiveLab.prev('div'));
            } else {
                curActiveLab.insertAfter(curActiveLab.next('div'));
            }
        },
        // 全选操作
        checkAll: function (e) {
            var _this = $(e.target);
            if (_this.prop('checked')) {
                _this.closest('label').siblings('.checkBox').find('input').prop('checked', true);
            } else {
                _this.closest('label').siblings('.checkBox').find('input').prop('checked', false);
            }
        },
        tabIsNull: function (mark) {
            switch (mark) {
                case 'REALTIME_MONITORING_LIST':
                    isNullTab.push('状态信息');
                    break;
                case 'REALTIME_MONITORING_ALARM_LIST':
                    isNullTab.push('报警记录');
                    break;
                case 'REALTIME_MONITORING_ACTIVE_SAFETY_LIST':
                    isNullTab.push('主动安全');
                    break;
                case 'REALTIME_MONITORING_OBD_LIST':
                    isNullTab.push('OBD数据');
                    break;
                case 'REALTIME_MONITORING_LOG_LIST':
                    isNullTab.push('日志记录');
                    break;
                case 'AlERT_WINDOW_REALTIME_DATA_LIST':
                    isNullTab.push('动态弹框');
                    break;
                default:
                    break;
            }
        },
        dosubmit: function () {
            var checkBoxList = $('.checkBox');
            var allList = [realTimeList.dataList, alarmList.dataList, activeSafetyList.dataList, obdList.dataList, logList.dataList, popupList.dataList];
            var paramArr = [];
            isNullTab = [];
            // var curActiveTab = $('#columnSetUl li.active').index();//当前tab索引值
            for (var i = 0; i < checkBoxList.length; i++) {
                var labelList = $(checkBoxList[i]).find('.columnCheck:checked');
                if (labelList.length == 0) {// 如果当前tab页,一列显示项都没有勾选
                    var curMark = $(checkBoxList[i]).find('.columnCheck').attr('data-mark');
                    columnSetting.tabIsNull(curMark);
                }
                var fixNum = parseInt($(checkBoxList[i]).siblings('.radioBox').find('input[type="radio"]:checked').val());//锁定列数
                if (fixNum !== 0 && fixNum >= labelList.length) {
                    layer.msg('固定列必须少于总列数');
                    return;
                }
                for (var j = 0; j < labelList.length; j++) {
                    var index = parseInt($(labelList[j]).attr('data-index'));// 存放当前列数据的索引值
                    var mark = $(labelList[j]).attr('data-mark');// 状态信息等tab标识
                    var obj = {
                        "columnId": allList[i][index].columnId,
                        "title": allList[i][index].title,
                        "mark": mark,
                        "columnName": allList[i][index].columnName
                    };
                    obj.sort = parseInt(j) + 1;
                    obj.isFix = 0;//是否是固定列(0:否;1:是)
                    if (fixNum > 0 && obj.sort <= fixNum) {
                        obj.isFix = 1;
                    }
                    paramArr.push(obj);
                }
            }
            if (isNullTab.length > 0) {// 如果tab页,一列显示项都没有勾选
                layer.msg('【' + isNullTab.join(',') + '】页签下请至少选择一列');
                return;
            }
            var url = "/clbs/core/uum/custom/addCustomColumnConfig";
            var data = {
                "customColumnConfigJson": JSON.stringify(paramArr),
                "title": '实时监控状态信息列表',
                "avoidRepeatSubmitToken": $("#avoidRepeatSubmitToken").val()
            };
            json_ajax("POST", url, "json", false, data, columnSetting.dataCallback);
        },
        dataCallback: function (data) {
            if (data.success) {
                $("#commonSmWin").modal("hide");
                newTableOperation.initSetting(true);
            } else if (data.msg) {
                layer.msg(data.msg);
            }
        },
        onCheckChange: function (e) {
            var _this = $(e.target);
            var $checkBox = _this.closest('.checkBox');
            var $input = $checkBox.find('.columnCheck');
            var checked = 0;
            for (var i = 0; i < $input.length; i++){
                if ($($input[i]).prop('checked')){
                    checked ++;
                }
            }
            if (checked === $input.length) {
                $checkBox.siblings('label').find('input').prop('checked', true);
            } else {
                $checkBox.siblings('label').find('input').prop('checked', false);
            }
        }
    };
    $(function () {
        columnSetting.init();
        $('.allCheck').on('click', columnSetting.checkAll);
        $('.checkLabel').on('click', function () {
            var _this = $(this);
            _this.addClass('active').siblings('div').removeClass('active');
            _this.closest('.tab-pane').find('button').removeAttr('disabled');
        });
        $('.sortUp').on('click', function (e) {
            columnSetting.sortUpOrDown(e, 'up');
        });
        $('.sortDown').on('click', function (e) {
            columnSetting.sortUpOrDown(e, 'down');
        });
        $('#doSubmit').on('click', columnSetting.dosubmit);
        $('.columnCheck').on('change', columnSetting.onCheckChange);
    })
})(window, $)