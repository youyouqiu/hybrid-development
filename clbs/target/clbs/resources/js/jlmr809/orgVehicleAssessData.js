// 企业信息数据
(function (window, $) {
    var orgList = '';// 勾选车辆name
    var orgId = '';// 勾选车辆id
    var exportParam = null;
    var noInfo = $('#orgVehicleMenuContent .noInfo');// 组织树加载信息提示语
    var messageInfo = $('#orgVehicleAssessMessage');// 请求返回提示
    var exportBtn = $('#exportOrgVehicleAssessData');// 导出按钮
    var startTime;
    var endTime;

    orgVehicleAssessData = {
        /**
         * 企业树相关方法
         * */
        treeInit: function () {
            var setting = {
                async: {
                    url: '/clbs/m/basicinfo/enterprise/professionals/tree?isOrg=1',
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    dataFilter: orgVehicleAssessData.ajaxDataFilter
                },
                check: {
                    enable: true,
                    chkStyle: "radio",
                    chkboxType: {
                        "Y": "s",
                        "N": "s"
                    },
                    radioType: "all"
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
                    beforeClick: orgVehicleAssessData.beforeClick,
                    onCheck: orgVehicleAssessData.onCheckOrg,
                    onAsyncSuccess: orgVehicleAssessData.zTreeOnAsyncSuccess
                }
            };
            $.fn.zTree.init($("#orgVehicleTreeDemo"), setting, null);
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            var data = responseData;
            if (data.length === 0) {
                noInfo.html('未查询到匹配项').show();
            } else {
                noInfo.html('未查询到匹配项').hide();
            }
            return data || [];
        },
        beforeClick: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(treeId);
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            treeObj.expandAll(true);
        },
        onCheckOrg: function (e, treeId, treeNode) {
            if (treeNode && treeNode.checked) {
                setTimeout(() => {
                    orgVehicleAssessData.getCheckedNodes();
                    orgVehicleAssessData.validates();
                }, 600);
            }
            orgVehicleAssessData.getCheckedNodes();
            orgVehicleAssessData.getCharSelect(treeId);
        },
        getCharSelect: function (treeId) {
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            var nodes = treeObj.getCheckedNodes(true);
            if (nodes.length > 0) {
                $("#orgVehicleGroupSelect").val(nodes[0].name);
            } else {
                $("#orgVehicleGroupSelect").val("");
            }
        },
        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj("orgVehicleTreeDemo"), nodes = zTree
                .getCheckedNodes(true), v = "", vid = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == "group") {
                    v += nodes[i].name;
                    vid += nodes[i].id;
                }
            }
            orgList = v;
            orgId = vid;
        },

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
                elem: '#orgVehicleYearTimeInput',
                type: 'year',
                theme: '#6dcff6',
                max: maxTime
            });
            $('#orgVehicleYearTimeInput').val(year);
            // 季度
            refactoringDate.renderSeasonDate('#orgVehicleQuarterTimeInput', 1, maxTime);
            $('#orgVehicleQuarterTimeInput').val(year + '年1季度');
            // 月
            laydate.render({
                elem: '#orgVehicleMonthTimeInput',
                type: 'month',
                theme: '#6dcff6',
                max: maxTime
            });
            $('#orgVehicleMonthTimeInput').val(year + '-' + month);
            // 周
            var startTime = new Date(nowDate.getTime() - 6 * 24 * 60 * 60 * 1000);
            var startYear = startTime.getFullYear();
            var startMonth = startTime.getMonth() + 1;
            var startDate = startTime.getDate();
            startMonth = startMonth < 10 ? '0' + startMonth : startMonth;
            startDate = startDate < 10 ? '0' + startDate : startDate;
            var weekDate = startYear + '-' + startMonth + '-' + startDate + ' -- ' + nowDay;
            // $("#orgVehicleWeekTimeInput").attr("lay-data", "{autoWeek: true}");
            $('#orgVehicleWeekTimeInput').val(weekDate);
            /*laydate.render({
                elem: '#orgVehicleWeekTimeInput',
                range: '--',
                max: maxTime,
                ready: function () {
                    var key = $(this.elem[0]).attr('lay-key');
                    var insTemp = laydatePro.getInstance(key);
                    if (insTemp.config.autoWeek) {
                        $('#layui-laydate' + key).on('click', '.layui-laydate-content td:not(.laydate-disabled)', function () {
                            var tds = $(this).parent().find('td:not(.laydate-disabled)');
                            insTemp.choose($(this));
                            insTemp.choose(tds.first());
                            insTemp.choose(tds.last());
                        });
                    }
                }
            });*/
            $('#orgVehicleWeekTimeInput').dateRangePicker({
                dateLimit: 7,
                onlyLimit: 7,
                isOffLineReportFlag: true,
                start_date: startYear + '-' + startMonth + '-' + startDate,
                end_date: nowDay,
                isShowHMS: false
            });
            // 日
            laydate.render({
                elem: '#orgVehicleDateTimeInput',
                type: 'date',
                theme: '#6dcff6',
                value: nowDay,
                max: maxTime
            });
            $('#orgVehicleDateTimeInput').val(nowDay);
        },
        timeTypeChange: function () {
            var curVal = $(this).val();
            var timeType = '';
            switch (curVal) {
                case '1':
                    timeType = '#orgVehicleYearTimeInput';
                    break;
                case '2':
                    timeType = '#orgVehicleQuarterTimeInput';
                    break;
                case '3':
                    timeType = '#orgVehicleMonthTimeInput';
                    break;
                case '4':
                    timeType = '#orgVehicleWeekTimeInput';
                    break;
                case '5':
                    timeType = '#orgVehicleDateTimeInput';
                    break;
                default:
                    break;
            }
            $(timeType).prop('disabled', false).show().siblings().prop('disabled', true).hide();
        },
        // 获取当前查询时间
        getRenderTime: function () {
            var curVal = $('#orgVehicleTimeType').val();
            switch (curVal) {
                case '1':// 年
                    startTime = $('#orgVehicleYearTimeInput').val() + '0101';
                    endTime = $('#orgVehicleYearTimeInput').val() + '1231';
                    break;
                case '2':// 季度
                    var curQuarterVal = $('#orgVehicleQuarterTimeInput').val();
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
                    var curTime = $('#orgVehicleMonthTimeInput').val().replace('-', '');
                    // 获取当月天数
                    var monthDate = laydate.getEndDate(parseInt(curTime.substr(-1, 2)), curTime.substr(0, 4));
                    startTime = curTime + '01';
                    endTime = curTime + monthDate;
                    break;
                case '4':// 周
                    var curTime = $('#orgVehicleWeekTimeInput').val().split('--');
                    startTime = curTime[0].replace(/-/g, '');
                    endTime = curTime[1].replace(/-/g, '');
                    break;
                case '5':// 日
                    var curTime = $('#orgVehicleDateTimeInput').val().replace(/-/g, '');
                    startTime = curTime;
                    endTime = curTime;
                    break;
            }
        },
        /**
         * 数据查询
         * */
        orgVehicleAssessDataSearch: function () {
            if (!orgVehicleAssessData.validates()) {
                return;
            }
            orgVehicleAssessData.getRenderTime();
            messageInfo.html('数据请求中...');
            exportBtn.prop('disabled', true);
            var url = '/clbs/jl/vehicle/corpAlarmCheck/dataReleased';
            var param = {
                'orgId': orgId,
                'corpName': orgList,
                'timeType': $('#orgVehicleTimeType').val(),
                'startTime': startTime,
                'endTime': endTime,
            };
            json_ajax("post", url, "json", false, param, function (data) {
                exportParam = null;
                $('#orgVehicleAssessDataTable td').html('--');
                if (data.success) {
                    var info = data.obj;
                    if (info.result == '1') {
                        var newInfo = info.info;
                        exportParam = newInfo;
                        exportBtn.prop('disabled', false);
                        for (key in newInfo) {
                            $('#orgVehicle-' + key).html(newInfo[key]);
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
            return $("#orgVehicleAssessFormData").validate({
                rules: {
                    offOperatePlatform: {
                        required: true
                    },
                    groupSelect: {
                        zTreeCheckGroup: "orgVehicleTreeDemo"
                    },
                    time: {
                        required: true
                    },
                },
                messages: {
                    offOperatePlatform: {
                        required: '请选择平台名称'
                    },
                    groupSelect: {
                        zTreeCheckGroup: '请选择企业',
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
            var url = '/clbs/jl/vehicle/corpAlarmCheck/export';
            exportExcelUseFormGet(url, exportParam);
        },
    };
    $(function () {
        orgVehicleAssessData.treeInit();
        orgVehicleAssessData.timeRender();
        $('#orgVehicleTimeType').on("change", orgVehicleAssessData.timeTypeChange);// 时间类型切换
        $("#orgVehicleAssessDataSearch").bind("click", orgVehicleAssessData.orgVehicleAssessDataSearch);
        exportBtn.bind("click", orgVehicleAssessData.exportOrgData);// 导出

        $("#orgVehicleGroupSelect").bind("click", showMenuContent);
        $('input').inputClear().on('onClearEvent', function (e, data) {
            if (data.id == 'orgVehicleGroupSelect') {
                orgVehicleAssessData.treeInit();
            }
        });
        // 企业树模糊查询
        $("#orgVehicleGroupSelect").on('input propertychange', function () {
            search_ztree('orgVehicleTreeDemo', 'orgVehicleGroupSelect', 'group');
        });
    })
}(window, $))