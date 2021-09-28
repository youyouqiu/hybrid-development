;(function (window, $) {
    var resultList = JSON.parse($('#resultList').val());
    var speedList = {};// 速度设置数据
    var allDataList = {};// 全部数据
    var stopList = {}; // 停止数据
    var obdList = {};// OBD数据数据
    var alarmList = {};// 报警数据
    var isNullTab = [];// 一项显示列都未勾选的页签

    tableColumnSetting = {
        init: function () {
            for (var i = 0; i < resultList.length; i++) {
                switch (resultList[i].mark) {
                    case 'MULTI_WINDOW_REALTIME_DATA_LIST':
                        allDataList = resultList[i];
                        tableColumnSetting.initTabHtml(allDataList, 'realTimeTab');
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
                case 'MULTI_WINDOW_REALTIME_DATA_LIST':
                    isNullTab.push('实时数据显示设置');
                    break;
                case 'TRACKPLAY_OBD_LIST':
                    isNullTab.push('OBD数据');
                    break;
                case 'TRACKPLAY_STOP':
                    isNullTab.push('停止数据');
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
            var allList = [allDataList.dataList];
            var paramArr = [];
            isNullTab = [];

            // var curActiveTab = $('#columnSetUl li.active').index();//当前tab索引值
            for (var i = 0; i < checkBoxList.length; i++) {
                var labelList = $(checkBoxList[i]).find('.columnCheck:checked');
                if (labelList.length == 0) {// 如果当前tab页,一列显示项都没有勾选
                    var curMark = $(checkBoxList[i]).find('.columnCheck').attr('data-mark');
                    tableColumnSetting.tabIsNull(curMark);
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
                    obj.sort = 1;
                    obj.isFix = 0;//是否是固定列(0:否;1:是)
                    paramArr.push(obj);
                }
            }
            // if (isNullTab.length > 0) {// 如果tab页,一列显示项都没有勾选
            //     layer.msg('【' + isNullTab.join(',') + '】页签下请至少选择一列');
            //     return;
            // }

            var url = "/clbs/core/uum/custom/addCustomColumnConfig";
            var data = {
                "customColumnConfigJson": JSON.stringify(paramArr),
                "title": 'MULTI_WINDOW_REALTIME_DATA_LIST',
                "avoidRepeatSubmitToken": $("#avoidRepeatSubmitToken").val()
            };
            json_ajax("POST", url, "json", true, data, tableColumnSetting.dataCallback);
        },
        dataCallback: function (data) {
            if (data.success) {
                $("#commonSmWin").modal("hide");
                multiWindow.dependency.get('smallWindow').initSetting();
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
    })
})(window, $);

