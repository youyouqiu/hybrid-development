;(function (window, $) {
    var resultList = JSON.parse($('#resultList').val());
    console.log(resultList);
    var speedList = {};// 速度设置数据
    var allDataList = {};// 全部数据
    var runList = {}; // 行驶段数据
    var stopList = {}; // 停止数据
    var obdList = {};// OBD数据数据
    var alarmList = {};// 报警数据
    var isNullTab = [];// 一项显示列都未勾选的页签

    tableColumnSetting = {
        init: function () {
            for (var i = 0; i < resultList.length; i++) {
                switch (resultList[i].mark) {
                    case 'TRACKPLAY_SPEED':
                        speedList = resultList[i];
                        var fromValue = speedList.dataList[0].initValue;//"滑块1速度"
                        var toValue = speedList.dataList[1].initValue;//"滑块2速度"
                        var lineInitValue = speedList.dataList[2].initValue;//"轨迹线粗"

                        // 拖拽控制滑块设置速度界点
                        $(".js-range-slider").ionRangeSlider({
                            skin: "round",
                            type: "double",
                            min: 0,
                            max: 150,
                            from: fromValue !== undefined ? fromValue : 30,
                            to: toValue !== undefined ? toValue : 80,
                            grid: true,
                            grid_num: 6,
                            postfix: "km/h",
                            decorate_both: false,
                        });
                        $('#lineInput').attr('data-value', lineInitValue)
                            .find('.line')
                            .css('height', lineInitValue + 'px');
                        break;
                    case 'TRACKPLAY_DATA':
                        allDataList = resultList[i];
                        tableColumnSetting.initTabHtml(allDataList, 'allDataTab');
                        break;
                    case 'TRACKPLAY_STOP':
                        stopList = resultList[i];
                        tableColumnSetting.initTabHtml(stopList, 'stopTab');
                        break;
                    case 'TRACKPLAY_RUN':
                        runList = resultList[i];
                        tableColumnSetting.initTabHtml(runList, 'runTab');
                        break;
                    case 'TRACKPLAY_OBD_LIST':
                        obdList = resultList[i];
                        tableColumnSetting.initTabHtml(obdList, 'obdInfoTab');
                        break;
                    case 'TRACKPLAY_ALARM':
                        alarmList = resultList[i];
                        tableColumnSetting.initTabHtml(alarmList, 'alarmTab');
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
                case 'TRACKPLAY_DATA':
                    isNullTab.push('全部/行驶数据');
                    break;
                case 'TRACKPLAY_OBD_LIST':
                    isNullTab.push('OBD数据');
                    break;
                case 'TRACKPLAY_RUN':
                    isNullTab.push('行驶段数据');
                    break;
                case 'TRACKPLAY_STOP':
                    isNullTab.push('停止段数据');
                    break;
                case 'TRACKPLAY_ALARM':
                    isNullTab.push('报警数据');
                    break;
                default:
                    break;
            }
        },
        dosubmit: function () {
            var checkBoxList = $('.checkBox');
            var allList = [[], allDataList.dataList, obdList.dataList, runList.dataList, stopList.dataList, alarmList.dataList];
            var paramArr = [];
            isNullTab = [];
            // var curActiveTab = $('#columnSetUl li.active').index();//当前tab索引值

            for (var i = 1; i < checkBoxList.length; i++) {
                var labelList = $(checkBoxList[i]).find('.columnCheck:checked');
                if (labelList.length == 0) {// 如果当前tab页,一列显示项都没有勾选
                    var curMark = $(checkBoxList[i]).find('.columnCheck').attr('data-mark');
                    tableColumnSetting.tabIsNull(curMark);
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
            // 组装速度值
            var my_range = $(".js-range-slider").data("ionRangeSlider");
            var lineInitValue = $('#lineInput').attr('data-value');
            speedList.dataList[0].sort = 1;
            speedList.dataList[0].isFix = 0;
            speedList.dataList[0].initValue = my_range.old_from;
            speedList.dataList[1].sort = 2;
            speedList.dataList[1].isFix = 0;
            speedList.dataList[1].initValue = my_range.old_to;
            speedList.dataList[2].sort = 3;
            speedList.dataList[2].isFix = 0;
            speedList.dataList[2].initValue = lineInitValue;
            paramArr = paramArr.concat(speedList.dataList);

            var url = "/clbs/core/uum/custom/addCustomColumnConfig";
            var data = {
                "customColumnConfigJson": JSON.stringify(paramArr),
                "title": '轨迹回放列表',
                "avoidRepeatSubmitToken": $("#avoidRepeatSubmitToken").val()
            };
            json_ajax("POST", url, "json", true, data, tableColumnSetting.dataCallback);
        },
        dataCallback: function (data) {
            if (data.success) {
                $("#commonSm2Win").modal("hide");
                trackPlayback.table.initSetting(true);
                trackPlayback.map.initColorSetting(trackPlayback.map.redrawLine.bind(trackPlayback.map));
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
        },
        lineListsChange: function(){
            var self = $(this);
            var value = self.data('value');

            self.addClass('check').siblings('li').removeClass('check');
            $('#lineInput .line').css('height',  value + 'px');
            $('#lineInput').attr('data-value', value);
            $('#lineLists').slideToggle('fast');
        }
    };
    $(function () {
        tableColumnSetting.init();
        $('.allCheck').on('click', tableColumnSetting.checkAll);
        $('.checkLabel').on('click', function () {
            $(this).addClass('active').siblings('div').removeClass('active');
            $(this).closest('.tab-pane').find('button').removeAttr('disabled');
        });
        $('.sortUp').on('click', function (e) {
            tableColumnSetting.sortUpOrDown(e, 'up');
        });
        $('.sortDown').on('click', function (e) {
            tableColumnSetting.sortUpOrDown(e, 'down');
        });
        $('#doSubmit').on('click', tableColumnSetting.dosubmit);
        $('.columnCheck').on('change', tableColumnSetting.onCheckChange);
        //轨迹线条粗细
        $('#lineInput').on('click', function(){$('#lineLists').slideToggle();});

        $('#lineLists').on('click', 'li', tableColumnSetting.lineListsChange);
    })
})(window, $);

