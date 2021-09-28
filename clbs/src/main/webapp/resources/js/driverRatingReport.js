var nvl = function (val, replacer) {
    if (val === undefined || val === null) {
        if (noUndefineOrNull(replacer)) {
            return replacer;
        }
        return '';
    }
    return val;
};
var noUndefineOrNull = function (value) {
    return value !== undefined && value !== null;
};

var getSecondFromText = function (text) {
    var hourReg = /(\d+)小时/;
    var miReg = /(\d+)分/;
    var sReg = /(\d+)秒/;
    var hourMatch = text.match(hourReg);
    var miMatch = text.match(miReg);
    var sMatch = text.match(sReg);

    var second = 0;
    if (hourMatch !== null) {
        second += parseInt(hourMatch[0]) * 3600;
    }
    if (miMatch !== null) {
        second += parseInt(miMatch[0]) * 60;
    }
    if (sMatch !== null) {
        second += parseInt(sMatch[0]);
    }

    return second;
}

/**
 * 保留指定小数位
 * @param {*} source 要转换的对象
 * @param {Number} digit 保留的小数位
 * @param {Boolean}} omitZero 是否省略最末尾的0
 */
var toFixed = function (source, digit, omitZero) {
    var sourceIn = source;
    if (typeof sourceIn !== 'number') {
        try {
            sourceIn = parseFloat(sourceIn);
        } catch (error) {
            return 0;
        }
    }
    if (sourceIn === null || sourceIn === undefined || isNaN(sourceIn)) {
        return 0;
    }
    var afterFixed = sourceIn.toFixed(digit); // 此时 afterFixed 为string类型
    if (omitZero) {
        afterFixed = parseFloat(afterFixed);
    }
    return afterFixed;
};


(function (window, $) {
    //开始时间
    var queryDateStr;
    //结束时间
    var myTable;
    //判断组织节点是否是勾选操作
    var checkFlag = false;
    //当前权限监控对象数量
    var size;
    var zTreeIdJson = {};
    //刚进入页面小于100自动勾选
    var ifAllCheck = true;
    var groupId = [];
    var totalTime = null;
    var simpleQueryParam = '';
    //用来储存显示数据
    var dataListArray = [];
    var riskEventFlag = true;
    var netData = [];
    var detailNetData = [];
    var groupNameDict = {};
    var activeRowIndex = null;
    var eventInfos = null;

    // 模拟数据
    var dataList = {};


    var vehicleIds = []; // 存储批量导出车辆id
    var isRender = false; // 是否重复渲染筛选列框$("#dropDown-q")

    var detailAjaxDataParam; // 获取明细列表ajax参数

    // 自定义选择监控对象
    select = {
        checkall: function (that) {
            if (that.prop("checked")) {
                $("input[name=objCheck-q]:checkbox").each(function () {
                    $(this).prop("checked", true);

                });

                $("#dataTable tbody tr").css({display: 'table-row'});

            } else {
                $("input[name=objCheck-q]:checkbox").each(function () {

                    $("input[name=objCheck-q]").removeAttr("checked")

                });

                $("#dataTable tbody tr").css({display: 'none'});

            }
            ;
        },
        chose: function () {
            //显示隐藏列
            $('.toggle-viq').off('change').on('change', function (e) {

                var nowDat = e.target.value;

                var table = $("#dataTable tr td:nth-child(2)");

                for (var i = 0; i < table.length; i++) {
                    var thisTr = $(table[i]).parents('tr');
                    var data = myTable.row(thisTr).data();

                    if (nowDat == data[10]) {

                        if (thisTr.css('display') == 'table-row') {
                            thisTr.css({display: 'none'});
                        } else {
                            thisTr.css({display: 'table-row'});
                        }
                    }
                }

            });
        },
        renderSelectList: function () {
            var menu_text = "";

            var table = $("#dataTable tr td:nth-child(2)");

            var vehis = []; // 车辆id（每次置空防止累加）
            if (table.length == 0) {
                menu_text = "<li>暂无数据</li>"
            } else {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"choose-all\" data-column=\"" + parseInt(1) + "\"  />全部</label></li>"
                for (var i = 0; i < table.length; i++) {
                    var thisTr = $(table[i]).parents('tr');
                    var data = myTable.row(thisTr).data();
                    menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-viq\" name=\"objCheck-q\" value=\"" + data[10] + "\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"

                    vehis.push(data[10])
                }
            }
            vehicleIds = vehis;

            $("#dropDown-q").html(menu_text);
        },
        showDropDown: function () {
            event.stopPropagation();
            if ($('#dropDown-q').hasClass('show')) {
                $('#dropDown-q').removeClass('show');
            } else {
                // 不要重复渲染
                if (!isRender) {
                    //显示隐藏列

                    select.renderSelectList();
                    isRender = true; // 禁止重复渲染
                }
                $('#dropDown-q').addClass('show');
                select.chose();
                // 全选功能
                $('.choose-all').bind('click', function () {
                    select.checkall($(this));
                });

                $("#dropDown-q").find("input[type='checkbox']").click(function (e) {
                    if (e.target.tagName == 'INPUT' && $(e.target).is(':checked') == false) {//如果有一个取消,全选按钮取消
                        $('.choose-all').prop('checked', false);
                    }
                    var choiceSelect = $('#dropDown-q').find("input[name='objCheck-q']").length;
                    var choiceLength = $('#dropDown-q').find("input[name='objCheck-q']:checked").length;
                    if (choiceSelect == choiceLength) {//所有按钮被选中,全选按钮选中
                        $('.choose-all').prop('checked', true);
                    }
                });
            }
        }
    };

    driverRatingReport = {
        init: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            for (var i = 0; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-text").html(menu_text);
            //车辆树
            var setting = {
                async: {
                    url: driverRatingReport.getParkingReportTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: driverRatingReport.ajaxDataFilter
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
                    beforeClick: driverRatingReport.beforeClickVehicle,
                    onAsyncSuccess: driverRatingReport.zTreeOnAsyncSuccess,
                    beforeCheck: driverRatingReport.zTreeBeforeCheck,
                    onCheck: driverRatingReport.onCheckVehicle,
                    onNodeCreated: driverRatingReport.zTreeOnNodeCreated,
                    onExpand: driverRatingReport.zTreeOnExpand
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);

        },

        getDataCallback: function (data) {
            if (data.success) {
                var obj = data.obj;
                if (!noUndefineOrNull(obj)) {
                    obj = {};
                }
                var averageSpeedText = '-';
                if (noUndefineOrNull(obj.averageSpeed)) {
                    $('#speedUnit').show();
                    $('#averageSpeed').html(obj.averageSpeed > 9999999 ? '9999999.99+' : toFixed(obj.averageSpeed, 2, true));
                } else {
                    $('#averageSpeed').html(averageSpeedText);
                    $('#speedUnit').hide();
                }

                var driverText = '-';
                if (noUndefineOrNull(obj.totalDriver)) {
                    $('#totalDriver').html(obj.totalDriver > 9999999 ? '9999999+' : obj.totalDriver);
                } else {
                    $('#totalDriver').html(driverText);
                }

                var dayOfDriverTimeText = '-';
                if (noUndefineOrNull(dayOfDriverTime)) {
                    $('#dayOfDriverTime').html(obj.dayOfDriverTime);
                } else {
                    $('#dayOfDriverTime').html(dayOfDriverTimeText);
                }

                var badBehaviorText = '-';
                if (noUndefineOrNull(obj.badBehavior)) {
                    $('#badBehavior').html(obj.badBehavior > 9999999 ? '9999999.99+' : obj.badBehavior);
                } else {
                    $('#badBehavior').html(badBehaviorText);
                }

                $('#badBehaviorRingRatio').html(driverRatingReport.formateRatio(obj.badBehaviorRingRatio));

                var hundredMileBadBehaviorText = '-';
                if (noUndefineOrNull(obj.hundredMileBadBehavior)) {
                    $('#hundredMileBadBehavior').html((obj.hundredMileBadBehavior > 9999999 ? '9999999.99+' : toFixed(obj.hundredMileBadBehavior, 2, true)));
                } else {
                    $('#hundredMileBadBehavior').html(hundredMileBadBehaviorText);
                }

                $('#hundredMileBadBehaviorRingRatio').html(driverRatingReport.formateRatio(obj.hundredMileBadBehaviorRingRatio));

                var scoreHTML = '-';
                if (noUndefineOrNull(obj.score)) {
                    scoreHTML = Math.round(obj.score);
                }
                $('#score').html(scoreHTML);
                $('#scoreRingRatio').html(driverRatingReport.formateRatioText(obj.scoreRingRatio));
                $('#behaviorText').html(driverRatingReport.formateBehaviour(obj.badBehaviorRingRatio, obj.maxScoreRange));
                netData = [
                    driverRatingReport.fiexdValue(obj.lucidity),
                    driverRatingReport.fiexdValue(obj.vigilance),
                    driverRatingReport.fiexdValue(obj.focus),
                    driverRatingReport.fiexdValue(obj.consciousness),
                    driverRatingReport.fiexdValue(obj.stationarity),
                ];
                driverRatingReport.echartInit();
            }
        },
        /**
         *
         * @param ratio
         * @param reverse 省略：上升红色，下降绿色，为true反之
         * @returns {string}
         */
        formateRatio: function (ratio, reverse) {
            if (!noUndefineOrNull(ratio)) {
                return '<span>-</span>'
            }
            ratio = toFixed(ratio, 2, true)
            if (ratio == 0) {
                return '<span >' + ratio + '%</span>'
            }
            if (ratio > 0) {
                if (reverse) {
                    return '<span class="greenTxt">' + '+' + ratio + '%</span>'
                }
                return '<span class="redTxt">' + '+' + ratio + '%</span>'
            }
            if (ratio < 0) {
                if (reverse) {
                    return '<span class="redTxt">' + ratio + '%</span>'
                }
                return '<span class="greenTxt">' + ratio + '%</span>'
            }
        },
        formateRatioText: function (scoreRingRatio) {
            if (!noUndefineOrNull(scoreRingRatio)) {
                return '驾驶员综合得分 -';
            }
            if (scoreRingRatio == 0) {
                return '驾驶员综合得分与上月持平，请继续加强管理力度';
            } else if (scoreRingRatio > 0) {
                return '驾驶员综合得分上升<span class="greenTxt">' + Math.abs(scoreRingRatio).toString() + '%</span>，请继续保持管理力度';
            }
            return '驾驶员综合得分下降<span class="redTxt">' + Math.abs(scoreRingRatio).toString() + '%</span>，请加强管理力度';
        },
        formateBehaviour: function (badBehaviorRingRatio, maxScoreRange) {
            if (!noUndefineOrNull(badBehaviorRingRatio) || !noUndefineOrNull(maxScoreRange)) {
                return '不良驾驶行为 - 得分 - 分布最多';
            }

            var text = '不良驾驶行为减少<span class="greenTxt">' + Math.abs(badBehaviorRingRatio).toString() + '%</span>';
            if (badBehaviorRingRatio == 0) {
                text = '不良驾驶行为与上月持平';
            } else if (badBehaviorRingRatio > 0) {
                text = '不良驾驶行为增加<span class="redTxt">' + Math.abs(badBehaviorRingRatio).toString() + '%</span>';
            }
            var a = '';
            switch (maxScoreRange) {
                case 0:
                    a = '0-20';
                    break;
                case 2:
                    a = '21-40';
                    break;
                case 4:
                    a = '41-60';
                    break;
                case 6:
                    a = '61-80';
                    break;
                case 8:
                    a = '81-100';
                    break;
                default:
                    break;
            }
            text += '、得分<span class="greenTxt">' + a + '</span>分布最多';
            return text;
        },
        echartInit: function () {
            var myChart = echarts.init(document.getElementById('myChart'));
            var option = {
                tooltip: {
                    trigger: 'axis'
                },
                grid: {
                    top: 140
                },
                radar: [
                    {
                        indicator: [
                            {
                                text: '清醒度',
                                max: 100,
                                color: 'green',
                                axisLabel: {show: true, textStyle: {fontSize: 12, color: '#999'}}
                            },
                            {text: '警惕性', max: 100, color: 'green'},
                            {text: '专注度', max: 100, color: 'green'},
                            {text: '自觉性', max: 100, color: 'green'},
                            {text: '平稳性', max: 100, color: 'green'},
                        ],
                        radius: 110,
                    },
                ],
                series: [
                    {
                        type: 'radar',
                        tooltip: {
                            trigger: 'item'
                        },
                        itemStyle: {normal: {color: 'rgb(191,231,168)', areaStyle: {type: 'default'}}},
                        data: [
                            {
                                value: netData,
                                name: '驾驶指数'
                            }
                        ]
                    }
                ]
            };
            myChart.setOption(option);
            window.onresize = function () {
                riskEventFlag = true;
                myChart.resize();
            };
        },
        modalEchartInit: function () {

            var modalEchart = echarts.init(document.getElementById('modalMyChart'));
            var option = {
                title: {
                    text: '驾驶指数',
                    left: 'center',
                    top: 10,
                    textStyle: {
                        fontSize: 20,
                        fontWeight: 'normal',
                        color: '#767676'
                    },
                },
                tooltip: {
                    trigger: 'axis'
                },
                radar: [
                    {
                        indicator: [
                            {
                                text: '清醒度',
                                max: 100,
                                color: 'green',
                                axisLabel: {show: true, textStyle: {fontSize: 12, color: '#999'}}
                            },
                            {text: '警惕性', max: 100, color: 'green'},
                            {text: '专注度', max: 100, color: 'green'},
                            {text: '自觉性', max: 100, color: 'green'},
                            {text: '平稳性', max: 100, color: 'green'},
                        ],
                        radius: 80,
                        center: ['45%', '65%'],
                    },
                ],
                series: [
                    {
                        type: 'radar',
                        tooltip: {
                            trigger: 'item'
                        },
                        itemStyle: {normal: {color: 'rgb(191,231,168)', areaStyle: {type: 'default'}}},
                        data: [
                            {
                                value: detailNetData,
                                name: '驾驶指数'
                            }
                        ]
                    }
                ]
            };
            modalEchart.setOption(option);
            window.onresize = function () {
                riskEventFlag = true;
                modalEchart.resize();
            };
        },
        riskEventChart: function () {
            // var riskEventX= ["前向碰撞", "车道左偏离", "车道右偏离", "车距过近", "行人碰撞", "频繁变道", "障碍物", "急加速", "急减速", "急转弯", "道路标识超限", "接打手持电话", "抽烟", "闭眼", "打哈欠", "长时间不目视前方", "分神驾驶", "人证不符", "驾驶员不在驾驶位置", "遮挡", "红外阻断", "疲劳驾驶", "驾驶员行为监测功能失效", "驾驶员异常", "双手全部脱离方向盘", "未系安全带报警", "驾驶辅助功能失效", "怠速", "异常熄火", "空挡滑行", "发动机超转"];

            var totalWidth;
            var barBox = $('#barBox').width();
            if (eventInfos && eventInfos.length > 0) {
                totalWidth = eventInfos.length * 95;
            }

            if (totalWidth < barBox) {
                totalWidth = barBox;
            }
            $('#barChartContainer').width(totalWidth);

            var riskEventX = eventInfos ? eventInfos.map(function (x) {
                return x.name;
            }) : [];
            var riskEventY = eventInfos ? eventInfos.map(function (x) {
                return x.value;
            }) : [];
            var option = {
                backgroundColor: 'rgba(128, 128, 128, 0)',
                tooltip: {
                    trigger: 'axis',
                    axisPointer: { // 坐标轴指示器，坐标轴触发有效
                        type: 'shadow' // 默认为直线，可选为：'line' | 'shadow'
                    },
                    confine: true
                },
                grid: {
                    containLabel: true,
                    width: '100%',
                    left: 0,
                    right: 40,
                    bottom: 100,
                },
                xAxis: {
                    type: 'category',
                    data: riskEventX,
                    axisLabel: { //坐标轴刻度标签
                        show: true,
                        interval: 0,
                        rotate: -60,
                        textStyle: {
                            color: "#2f2f2f",
                            fontSize: 12,
                            fontWeight: 'lighter',
                        },
                    },
                    axisLine: { //坐标轴轴线
                        show: false
                    },
                    axisTick: { //坐标轴刻度
                        show: false
                    }
                },
                yAxis: {
                    type: 'value',
                },
                series: [{
                    data: riskEventY,
                    type: 'bar',
                    name: '报警数据排行',
                    barWidth: 50,
                    itemStyle: {
                        normal: {
                            color: '#198ef0'
                        }
                    },
                    label: {
                        normal: {
                            show: true,
                            // rotate: 45,
                            // distance: 15,
                            position: 'top',
                        }
                    }
                }]
            };
            var riskEventChart = echarts.init(document.getElementById('riskEventChart'));
            riskEventChart.setOption(option);
            window.onresize = function () {
                riskEventFlag = true;
                riskEventChart.resize();
            };
        },
        getParkingReportTreeUrl: function (treeId, treeNode) {
            if (treeId == 'treeDemo') {
                return "/clbs/m/basicinfo/enterprise/professionals/tree";
            }
        },
        doHandleMonth: function (month) {
            var m = month;
            if (month.toString().length == 1) {
                m = "0" + month;
            }
            return m;
        },
        exportAlarm: function () {
            if ($("#dataTable tbody tr td").hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据，无法导出");
                return;
            }
            simpleQueryParam = $("#simpleQueryParam").val();
            var saveUrl = "/clbs/m/reportManagement/outTotalTime/saveExportData";
            var data = {
                "groupId": groupId.replace(/,/g, ''),
                "totalTime": totalTime,
                "endTime": queryDateStr,
                "fuzzyParm": simpleQueryParam
            };
            json_ajax("post", saveUrl, "json", false, data, driverRatingReport.svaeExportDataCallBack);
        },
        svaeExportDataCallBack: function (result) {
            if (result.success) { // 数据存储成则导入
                var url = "/clbs/m/reportManagement/outTotalTime/export";
                window.location.href = url
            }
        },
        validates: function () {
            return $("#lifeCycleForm").validate({
                rules: {
                    month: {
                        required: true
                    },
                    // dateNum: {
                    //     required: true,
                    //     min: 0,
                    //     maxlength: 3,
                    //     digits: true,
                    // },
                    groupSelect: {
                        zTreeCheckGroup: "treeDemo"
                    }
                },
                messages: {
                    month: {
                        required: "请选择日期！",
                    },
                    groupSelect: {
                        zTreeCheckGroup: vehicleSelectGroup,
                    },
                    // dateNum: {
                    //     required: dateNumError,
                    //     min: dateNumError,
                    //     maxlength: dateNumError,
                    //     digits: dateNumError,
                    // },
                }
            }).form();
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            if (treeId == "treeDemo") {
                size = responseData.length;
                for (var i = 0; i < size; i++) {
                    var item = responseData[i];
                    groupNameDict[item.id] = item.name;
                }
                return responseData;
            }
        },
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (ifAllCheck) {
                treeObj.checkAllNodes(true);
                var nodes = treeObj.getNodes();
                treeObj.checkNode(nodes[0]); // 默认勾选单个节点
                $("#groupSelect").val(nodes[0].name); // 更新input框显示值
                for (var i = 0; i < nodes.length; i++) { //设置节点展开
                    treeObj.expandNode(nodes[i], true, false, true);
                }
            }
            driverRatingReport.getCharSelect(treeObj);
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;

                    json_ajax("post", "/clbs/m/personalized/ico/getVehicleCount",
                        "json", false, {"parentId": treeNode.id, "type": treeNode.type}, function (data) {
                            if (data.success) {
                                nodesLength += data.obj;
                            } else {
                                layer.msg(data.msg);
                            }
                        });

                    //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                    var ns = [];
                    //节点id
                    var nodeId;
                    for (var i = 0; i < nodes.length; i++) {
                        nodeId = nodes[i].id;
                        if (nodes[i].type == "people" || nodes[i].type == "vehicle") {
                            //查询该节点是否在勾选组织或分组下，若在则不记录，不在则记录
                            var nd = zTree.getNodeByParam("tId", nodes[i].tId, treeNode);
                            if (nd == null && $.inArray(nodeId, ns) == -1) {
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength += ns.length;
                } else if (treeNode.type == "people" || treeNode.type == "vehicle") { //若勾选的为监控对象
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;
                    //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                    var ns = [];
                    //节点id
                    var nodeId;
                    for (var i = 0; i < nodes.length; i++) {
                        nodeId = nodes[i].id;
                        if (nodes[i].type == "people" || nodes[i].type == "vehicle") {
                            if ($.inArray(nodeId, ns) == -1) {
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength = ns.length + 1;
                }
            }
            if (flag) {
                //若组织节点已被勾选，则是勾选操作，改变勾选操作标识
                if (treeNode.type == "group" && !treeNode.checked) {
                    checkFlag = true;
                }
            }
            return flag;
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                zTree.expandNode(treeNode, true, true, false, false); // 展开节点
                setTimeout(() => {
                    driverRatingReport.getCheckedNodes("treeDemo");
                    driverRatingReport.validates();
                })
            }
            driverRatingReport.getCheckedNodes("treeDemo");
            driverRatingReport.getCharSelect(zTree);
            $('#groupSelect').val(treeNode.name);
        },
        zTreeOnNodeCreated: function (event, treeId, treeNode) {
            var id = treeNode.id.toString();
            var list = [];
            if (zTreeIdJson[id] == undefined || zTreeIdJson[id] == null) {
                list = [treeNode.tId];
                zTreeIdJson[id] = list;
            } else {
                zTreeIdJson[id].push(treeNode.tId)
            }
        },
        zTreeOnExpand: function (event, treeId, treeNode) {
            //判断是否是勾选操作展开的树(是则继续执行，不是则返回)
            if (treeNode.type == "group" && !checkFlag) {
                return;
            }
            //初始化勾选操作判断表示
            checkFlag = false;
        },
        getCharSelect: function (treeObj) {
            var nodes = treeObj.getCheckedNodes(true);
            var allNodes = treeObj.getNodes();
            if (nodes.length > 0) {
                $("#groupSelect").val(allNodes[0].name);
            } else {
                $("#groupSelect").val("");
            }
        },
        getCheckedNodes: function (treeId) {
            var zTree = $.fn.zTree.getZTreeObj(treeId),
                nodes = zTree.getCheckedNodes(true), groupIds = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (treeId == "treeDemo" && nodes[i].type == "group") {
                    groupIds += nodes[i].uuid + ",";
                }
            }
            groupId = groupIds;
        },
        getTable: function (table) {
            $('.toggle-vis').prop('checked', true);
            myTable = $(table).DataTable({
                "columnDefs": [{
                    // "targets": [0, 2, 3, 4, 5, 6],
                    "targets": 0,

                    "searchable": false // 禁止第一列参与搜索
                }, {
                    "targets": [0, 1, 2, 7],
                    "orderable": false
                }],
                "destroy": true,
                /*"dom": 'tiprl',// 自定义显示项*/
                "lengthChange": true,// 是否允许用户自定义显示数量
                "bPaginate": true, // 翻页功能
                "bFilter": true, // 列筛序功能
                "searching": true,// 本地搜索
                "ordering": true, // 排序功能
                "Info": true,// 页脚信息
                "autoWidth": true,// 自动宽度
                "stripeClasses": [],
                "pageLength": 10,
                "lengthMenu": [5, 10, 20, 50, 100],
                "pagingType": "simple_numbers", // 分页样式
                "dom": "t" + "<'row'<'col-md-2 col-sm-12 col-xs-12 noPadding'l><'col-md-4 col-sm-12 col-xs-12 noPadding'i><'col-md-6 col-sm-12 col-xs-12 noPadding'p>>",
                "oLanguage": {// 国际语言转化
                    "oAria": {
                        "sSortAscending": " - click/return to sort ascending",
                        "sSortDescending": " - click/return to sort descending"
                    },
                    "sLengthMenu": "显示 _MENU_ 记录",
                    "sInfo": "当前显示 _START_ 到 _END_ 条，共 _TOTAL_ 条记录。",
                    "sZeroRecords": "我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？",
                    "sEmptyTable": "我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？",
                    "sLoadingRecords": "正在加载数据-请等待...",
                    "sInfoEmpty": "当前显示0到0条，共0条记录",
                    //"sInfoFiltered": "（数据库中共为 _MAX_ 条记录）",
                    "sInfoFiltered": "",
                    "sProcessing": "<img src='../resources/user_share/row_details/select2-spinner.gif'/> 正在加载数据...",
                    "sSearch": "模糊查询：",
                    "sUrl": "",
                    "oPaginate": {
                        "sFirst": "首页",
                        "sPrevious": " 上一页 ",
                        "sNext": " 下一页 ",
                        "sLast": " 尾页 "
                    }
                },
                "order": [
                    [9, 'desc']//第一列正序
                ],// 第一列排序图标改为默认
                "drawCallback": function (settings) {

                    // 重绘表格+更新筛选框
                    select.renderSelectList();


//                     var api = this.api();
// var obj = api.rows( {page:'current'} ).data()
//                     // Output the data for the visible rows to the browser's console
//                     // console.log( api.rows( {page:'current'} ).data() , typeof api.rows( {page:'current'} ).data() ,"====");
//                     Object.keys(obj).map(function(d, i) {
//                         // console.log(d, i)
//                         console.log(obj[d])
//                     })
//
//                     console.log(api.rows.count(), '===')
                },
                customSort: function (a, b, direction, col, test) {
                    if (col === 5) {
                        var aValue, bValue;
                        if (a === '-小时-分-秒') {
                            if (direction === 'asc') {
                                aValue = Number.MAX_VALUE;
                            } else {
                                aValue = -10000;
                            }
                        } else {
                            aValue = getSecondFromText(a)
                        }

                        if (b === '-小时-分-秒') {
                            if (direction === 'asc') {
                                bValue = Number.MAX_VALUE;
                            } else {
                                bValue = -10000;
                            }
                        } else {
                            bValue = getSecondFromText(b)
                        }

                        return aValue - bValue;
                    }
                    return test;
                }
            });
            myTable.on('order.dt search.dt', function () {
                myTable.column(0, {
                    search: 'applied',
                    order: 'applied'
                }).nodes().each(function (cell, i) {
                    cell.innerHTML = i + 1;
                });
            }).draw();
            //显示隐藏列
            $('.toggle-vis').off('change').on('change', function (e) {
                var column = myTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });
            $("#search_button").on("click", function () {
                var queryCondition = $("#simpleQueryParam").val();
                myTable.column(2).search(queryCondition, false, false).draw();

                var table = $("#dataTable tr td:nth-child(2)");

                for (var i = 0; i < table.length; i++) {
                    var thisTr = $(table[i]).parents('tr');
                    thisTr.css({display: 'table-row'});
                }
            });
        },
        //刷新列表
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            driverRatingReport.inquireClick(1);
        },
        inquireClick: function (number) {
            // if($('#month').val() ==''){
            //     layer.msg('df')
            //     return;
            // }
            if (number != 1) {
                driverRatingReport.setNewDay(number);
            }
            driverRatingReport.getCheckedNodes('treeDemo');
            if (!driverRatingReport.validates()) {
                return;
            }
            var time = $('#month').val();
            var ajaxDataParam = {
                "groupId": groupId.replace(/,/g, ''),
                time: time.replace('-', '')
            }
            $('#stretch3-body').slideDown();
            var url = "/clbs/m/reportManagement/driverScore/getIcCardDriverInfoList";
            json_ajax("POST", url, "json", true, ajaxDataParam, driverRatingReport.getCallback);

            var urlB = '/clbs/m/reportManagement/driverScore/getIcCardDriverInfo';
            json_ajax("post", urlB, "json", true, ajaxDataParam, driverRatingReport.getDataCallback);
            // var driverId = $("#select3").val();
            // if (driverId === '0') {
            //     $("#dataTable tbody tr").show();
            // } else {
            //     $("#dataTable tbody tr").hide();
            //     $("#dataTable tbody tr[data-index=" + driverId + "]").show();
            // }
        },
        getCallback: function (date) {
            if (date.obj == "") {
                $("#score").html('-');
                $("#scoreRingRatio").html('-');
                $("#behaviorText").html('-');
                $("#totalDriver").html('-');
                $("#dayOfDriverTime").html('-');
                $("#badBehavior").html('-');
                $("#badBehaviorRingRatio").html('-');
                $("#hundredMileBadBehavior").html('-');
                $("#hundredMileBadBehaviorRingRatio").html('-');
                $("#averageSpeed").html('-');
                driverRatingReport.reloadData([]);
                netData = [];
                driverRatingReport.echartInit();
                return;
            }
            if (date.success == true) {
                $("#dataTable tbody").attr("style", "");
                dataListArray = [];//用来储存显示数据
                var tableArray = [];
                if (date.obj != null && date.obj.length != 0) {
                    $("#exportAlarm").removeAttr("disabled");
                    var result = date.obj;
                    var len = result.length;
                    for (var i = 0; i < len; i++) {
                        var recordData = result[i];
                        var driverNameCardNumberArray
                        if (recordData.driverNameCardNumberVal !== null) {
                            driverNameCardNumberArray = recordData.driverNameCardNumberVal.split('_');
                        } else {
                            driverNameCardNumberArray = ['-', '-'];
                        }
                        var driverName = driverNameCardNumberArray[1];
                        var cardNumber = driverNameCardNumberArray[0];
                        var dateList =
                            [
                                i + 1,
                                driverName,
                                cardNumber,
                                toFixed(recordData.driverMile, 1, true),
                                recordData.driverTimes,
                                // toFixed(recordData.averageDriverTime,2,true),
                                recordData.averageDriverTime,
                                toFixed(recordData.averageSpeed, 2, true),
                                recordData.groupName,
                                recordData.alarm,
                                Math.round(recordData.score),
                                recordData.driverNameCardNumber + "_" + recordData.groupName,
                                recordData.groupId
                            ];
                        tableArray.push(dateList);
                        dataListArray.push(recordData);
                    }
                    driverRatingReport.reloadData(tableArray);
                    $("#simpleQueryParam").val("");
                    $("#search_button").click();
                } else {
                    driverRatingReport.reloadData(tableArray);
                    $("#simpleQueryParam").val("");
                    $("#search_button").click();
                    $("#exportAlarm").attr("disabled", "disabled");
                }
            } else {
                if (date.msg != null) {
                    layer.msg(date.msg, {move: false});
                }
            }
        },
        reloadData: function (dataList) {
            var currentPage = myTable.page();
            myTable.clear();
            myTable.rows.add(dataList);
            myTable.page(currentPage).draw(false);
            myTable.column(2).search('', false, false).page(currentPage).draw();
        },
        trClickFun: function () {
            console.log(1);
            var index = $(this).attr('data-index');
            var curData = dataList[index];
            driverRatingReport.renderHtml(curData);
            if ($(this).hasClass('active')) {
                $('#driverModal').removeClass('active');
            } else if (!$('#driverModal').hasClass('active')) {
                $('#situationTab a').click();
                $('#driverModal').addClass('active');
            }
            $(this).toggleClass('active');
            $(this).siblings('tr').removeClass('active');
        },
        renderHtml: function (data) {
            for (key in data) {
                $('#' + key).html(data[key]);
            }
        },
        appendZero: function (val) {
            if (val < 10) {
                return '0' + val.toString();
            }
            return val.toString();
        },
        closeDetail: function () {
            $('#dataTable tbody tr').removeClass('active');
            activeRowIndex = null;
            $('#detail').removeClass('active')
        },
        tdClick: function (event) {
            event.stopPropagation();

            var thisTr = $(event.currentTarget).closest('tr');
            var data = myTable.row(thisTr).data();
            var index = data[0] - 1;
            if (index !== activeRowIndex) {
                thisTr.siblings().removeClass('active');
                thisTr.addClass('active');
                activeRowIndex = index;
                var data = dataListArray[index];
                var array = data.driverNameCardNumber.split('_');

                var time = $('#month').val();
                var ajaxDataParam = {
                    "groupId": data.groupId,
                    time: time.replace(/-/g, ''),
                    cardNumber: array[0],
                    driverName: array[1]
                }

                $('#detailMonth').html(time);
                var url = "/clbs/m/reportManagement/driverScore/getDriverScoreProfessionalInfo";
                json_ajax("POST", url, "json", true, ajaxDataParam, driverRatingReport.detailCallback);

                $('#detailTbody').empty();
                ajaxDataParam.limit = 50;
                detailAjaxDataParam = ajaxDataParam;
                var url = "/clbs/m/reportManagement/driverScore/getIcCardDriverEventList";
                json_ajax("POST", url, "json", true, ajaxDataParam, driverRatingReport.detailTableCallback);

            } else {
                thisTr.removeClass('active');
                $('#detail').removeClass('active')
                activeRowIndex = null;
            }

        },
        left_arrow: function () {
            var detailMonth = $('#detailMonth').html();
            var date = new Date(detailMonth.replace(/-/g, '/') + '/01 00:00:00');
            var now = new Date();
            var dateYear = date.getFullYear();
            var dateMonth = date.getMonth();
            var nowYear = now.getFullYear();
            var nowMonth = now.getMonth();
            if (dateYear >= nowYear && dateMonth >= nowMonth) {
                return;
            }
            date.setMonth(date.getMonth() - 1);
            dateYear = date.getFullYear();
            dateMonth = date.getMonth() + 1;
            var newDate = dateYear + '-' + driverRatingReport.appendZero(dateMonth);
            $('#detailMonth').html(newDate);
            driverRatingReport.doQueryDetail(activeRowIndex, newDate);
        },
        right_arrow: function () {
            var detailMonth = $('#detailMonth').html();
            var date = new Date(detailMonth.replace(/-/g, '/') + '/01 00:00:00');
            var now = new Date();
            var dateYear = date.getFullYear();
            var dateMonth = date.getMonth();
            var nowYear = now.getFullYear();
            var nowMonth = now.getMonth();
            if (dateYear >= nowYear && dateMonth + 1 >= nowMonth) {
                return;
            }
            date.setMonth(date.getMonth() + 1);
            dateYear = date.getFullYear();
            dateMonth = date.getMonth() + 1;
            var newDate = dateYear + '-' + driverRatingReport.appendZero(dateMonth);
            $('#detailMonth').html(newDate);
            driverRatingReport.doQueryDetail(activeRowIndex, newDate);
        },
        doQueryDetail: function (index, time) {
            var data = dataListArray[index];
            var array = data.driverNameCardNumber.split('_');

            var ajaxDataParam = {
                "groupId": data.groupId,
                time: time.replace(/-/g, ''),
                cardNumber: array[0],
                driverName: array[1]
            }

            var url = "/clbs/m/reportManagement/driverScore/getDriverScoreProfessionalInfo";
            window.fromDetail = true;
            json_ajax("POST", url, "json", true, ajaxDataParam, driverRatingReport.detailCallback);

            $('#detailTbody').empty();
            var url = "/clbs/m/reportManagement/driverScore/getIcCardDriverEventList";
            ajaxDataParam.limit = 50;
            detailAjaxDataParam = ajaxDataParam;
            json_ajax("POST", url, "json", true, ajaxDataParam, driverRatingReport.detailTableCallback);
        },
        detailTableCallback: function (data) {
            if (data.success) {
                var obj = data.obj;
                if (obj == '' || obj.result == '') {
                    eventInfos = [];
                    driverRatingReport.riskEventChart();
                }

                var $tbody = $('#detailTbody');
                var trLength = $tbody.find('tr').length;
                detailAjaxDataParam.searchAfter = obj.searchAfter.join(',');

                if (obj && obj.result && obj.result.length > 0) {
                    var htmlArr = '';
                    var part = obj.result;
                    for (var i = 0; i < part.length; i++) {
                        var d = part[i];
                        var html = '<tr>';
                        html += '<td>' + (trLength + i + 1) + '</td>';
                        html += '<td>' + nvl(d.monitorName) + '</td>';
                        html += '<td>' + nvl(d.event) + '</td>';
                        html += '<td>' + nvl(d.riskLevel) + '</td>';
                        html += '<td>' + nvl(d.eventTime) + '</td>';
                        var _speed = nvl(d.speed);
                        if (_speed !== '') {
                            _speed = toFixed(_speed, 2, true);
                        }
                        html += '<td>' + _speed + '</td>';
                        html += '<td>' + nvl(d.address) + '</td>';
                        html += '</tr>';

                        htmlArr += html;
                    }
                    $tbody.append($(htmlArr));
                }
                window.isAppending = false;
            }
        },
        detailCallback: function (data) {
            var fromDetail = window.fromDetail;
            window.fromDetail = false;
            // if(!$("#detail").hasClass('active')){
            //     $("#situationTab").addClass('active').siblings().removeClass('active');
            //     $("#situationBox").addClass('active').css('display','block').siblings().removeClass('active').css('display','none');
            // }

            if (data.success) {
                var obj = data.obj;
                if (obj == null || obj == "") {
                    $("#detailScore").html('-');
                    $("#detailTip").html('驾驶员综合得分 -');
                    $("#detailBad").html('不良驾驶行为 -');
                    $('#longTip').html('驾驶点评： -');
                    $("#detailBadBehavior").html('-');
                    $("#detailBadBehaviorRingRatio").html('-');
                    $("#detailHundredMileBadBehavior").html('-');
                    $("#detailHundredMileBadBehaviorRingRatio").html('-');
                    $("#driverMile").html('-');
                    $("#detailDayOfDriverTime").html("-");
                    detailNetData = [];

                    driverRatingReport.modalEchartInit();
                    eventInfos = [];

                    if ($('#detailTab').hasClass('active')) {
                        driverRatingReport.riskEventChart();
                    } else {
                        riskEventFlag = true;
                    }
                    $('#detailExport').attr('disabled', 'disabled');
                    return;
                }
                $('#detailExport').removeAttr('disabled');

                if (noUndefineOrNull(obj.score)) {
                    $('#detailScore').html(Math.round(obj.score));
                } else {
                    $('#detailScore').html('-');
                }
                $('#detailTip').html(driverRatingReport.formateDetailTip(obj.score, obj.scoreRingRatio));
                $('#detailBad').html(driverRatingReport.formateBad(obj.badBehaviorRingRatio));
                $('#longTip').html(driverRatingReport.formateLongTip(obj.score, obj.lucidity, obj.vigilance, obj.focus, obj.consciousness, obj.stationarity));

                detailNetData = [driverRatingReport.fiexdValue(obj.lucidity), driverRatingReport.fiexdValue(obj.vigilance), driverRatingReport.fiexdValue(obj.focus), driverRatingReport.fiexdValue(obj.consciousness), driverRatingReport.fiexdValue(obj.stationarity)];

                if (noUndefineOrNull(obj.badBehavior)) {
                    $('#detailBadBehavior').html((obj.badBehavior > 9999999 ? '9999999.99+' : obj.badBehavior));
                } else {
                    $('#detailBadBehavior').html('-');
                }

                $('#detailBadBehaviorRingRatio').html(driverRatingReport.formateRatio(obj.badBehaviorRingRatio));


                if (noUndefineOrNull(obj.hundredMileBadBehavior)) {
                    $('#detailHundredMileBadBehavior').html((obj.hundredMileBadBehavior > 9999999 ? '9999999.99+' : toFixed(obj.hundredMileBadBehavior, 2, true)));
                } else {
                    $('#detailHundredMileBadBehavior').html('-');
                }

                $('#detailHundredMileBadBehaviorRingRatio').html(driverRatingReport.formateRatio(obj.hundredMileBadBehaviorRingRatio));


                if (noUndefineOrNull(obj.dayOfDriverTime)) {
                    $('#detailDayOfDriverTime').html(obj.dayOfDriverTime);
                } else {
                    $('#detailDayOfDriverTime').html('-');
                }

                if (noUndefineOrNull(obj.driverMile)) {
                    $('#driverMile').html(toFixed(obj.driverMile, 1, true) + 'km');
                } else {
                    $('#driverMile').html('-');
                }


                $('#driverName').html(obj.adasProfessionalShow.name);
                $('#postType').html(obj.adasProfessionalShow.type);
                $('#groupName').html(obj.adasProfessionalShow.groupName);
                $('#certificateNo').html(obj.adasProfessionalShow.cardNumber);
                $('#certificateType').html(obj.adasProfessionalShow.qualificationCategory);
                $('#issuingAgencies').html(obj.adasProfessionalShow.icCardAgencies);
                $('#untilDate').html(obj.adasProfessionalShow.icCardEndDateStr);
                $('#detailImg').attr('src', obj.adasProfessionalShow.photograph);
                $('#driverMile').html(obj.driverMile);

                eventInfos = obj.eventInfos;

                if (!fromDetail) {
                    $("#situationTab").addClass('active').siblings().removeClass('active');
                    $("#situationBox").addClass('active').css('display', 'block').siblings().removeClass('active').css('display', 'none');
                }


                if ($('#detailTab').hasClass('active')) {
                    driverRatingReport.riskEventChart();
                } else {
                    riskEventFlag = true;
                }
                $('#detail').addClass('active');
                driverRatingReport.modalEchartInit();
            } else {
                layer.msg(data.msg || data.exceptionDetailMsg);
            }
        },
        fiexdValue: function (val) {
            // if(val == 100){
            //     return val = 100
            // }else{
            //     return (parseInt(val * 100)/100).toFixed(2);
            // }
            return toFixed(val, 2, true);
        },
        formateColor: function (badBehaviorRingRatio) {
            if (badBehaviorRingRatio > 0) {
                return '<span class="redTxt">+' + badBehaviorRingRatio + '%</span>'
            } else if (badBehaviorRingRatio < 0) {
                return '<span class="greenTxt">-' + badBehaviorRingRatio + '%</span>'
            }
            return '<span class="">' + badBehaviorRingRatio + '%</span>'
        },
        formateDetailTip: function (score, scoreRingRatio) {
            if (!noUndefineOrNull(scoreRingRatio)) {
                return '驾驶员综合得分 -';
            }
            if (scoreRingRatio == 0) {
                return '驾驶员综合得分与上月持平，请继续保持！';
            } else if (scoreRingRatio > 0) {
                return '驾驶员驾驶行为得分上升<span class="greenTxt">' + Math.abs(scoreRingRatio).toString() + '%</span>，请继续保持！';
            }
            return '驾驶员驾驶行为得分下降<span class="redTxt">' + Math.abs(scoreRingRatio).toString() + '%</span>，努力加油！';
        },
        formateBad: function (badBehaviorRingRatio) {
            if (!noUndefineOrNull(badBehaviorRingRatio)) {
                return '不良驾驶行为 -';
            }
            if (badBehaviorRingRatio == 0) {
                return '不良驾驶行为与上月持平！';
            } else if (badBehaviorRingRatio > 0) {
                return '不良驾驶行为上升<span class="redTxt">' + Math.abs(badBehaviorRingRatio).toString() + '%</span>';
            }
            return '不良驾驶行为减少<span class="greenTxt">' + Math.abs(badBehaviorRingRatio).toString() + '%</span>';
        },
        formateLongTip: function (score, lucidity, vigilance, focus, consciousness, stationarity) {
            if (!noUndefineOrNull(score)) {
                return '驾驶点评： -';
            }
            var text = '';
            if (score <= 20) {
                text += '驾驶情况极差，请加强自身安全意识！';
            } else if (score <= 40) {
                text += '驾驶情况差，请加强自身安全意识！';
            } else if (score <= 60) {
                text += '驾驶情况较差，请加强自身安全意识！';
            } else if (score <= 80) {
                text += '驾驶情况一般，要继续争优哦！';
            } else if (score <= 100) {
                text += '驾驶情况还不错，要继续保持争优哦！';
            }

            var array = [lucidity, vigilance, focus, consciousness, stationarity];
            var max = Math.max(lucidity, vigilance, focus, consciousness, stationarity);
            var min = Math.min(lucidity, vigilance, focus, consciousness, stationarity);

            var maxTimes = 0;
            var minTimes = 0;

            for (var i = 0; i < array.length; i++) {
                if (array[i] === max) {
                    maxTimes += 1;
                }
                if (array[i] === min) {
                    minTimes += 1;
                }
            }

            if (maxTimes > 1 || minTimes > 1) {
                return '驾驶点评：' + text;
            }

            if (lucidity === max) {
                text += '驾驶清醒度得分超高，精神状态很好嘛，这种习惯值得发扬哦！';
            } else if (lucidity === min) {
                text += '驾驶清醒度分值不佳，要注意适当休息哦！疲劳驾驶是开车事故中最大安全隐患!';
            }
            if (vigilance === max) {
                text += '驾驶警惕性得分超高，这种习惯值得发扬哦！';
            } else if (vigilance === min) {
                text += '驾驶警惕性分值不佳，多注意路面情况，提前做出正确判断！';
            }
            if (focus === max) {
                text += '驾驶专注度得分超高，开车很认真嘛，这种习惯值得发扬哦！';
            } else if (focus === min) {
                text += '驾驶专注度分值不佳，行驶中接打电话、抽烟、注意力分散都是开车事故安全隐患，要尽量避免哦！  ';
            }
            if (consciousness === max) {
                text += '驾驶自觉性得分超高，遵纪守法，这种习惯值得发扬哦！';
            } else if (consciousness === min) {
                text += '驾驶自觉性分值不佳，自觉遵守规章制度，才能更有效工作!';
            }
            if (stationarity === max) {
                text += '驾驶平稳性得分超高，这种习惯值得发扬哦！';
            } else if (stationarity === min) {
                text += '驾驶平稳性分值不佳，频繁变道、车道偏离、急转弯、急加速、急减速、超速都是开车事故隐患，要尽量避免哦！';
            }
            if (text.length > 0) {
                text = '驾驶点评：' + text;
            }
            return text;
        },

        moreExport: function () {
            if ($("#dataTable tbody tr td").hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据,无法导出");
                return;
            }

            if (vehicleIds.length == 0) {
                return;
            }
            // var arr = [];
            // for(var i=0; i<=vehicleIds.length-1; i++){
            //     var str = vehicleIds[i];
            //     var vehicleIdArr = str.split('_');
            //     var vehicleId = vehicleIdArr[0] +'_'+ vehicleIdArr[1];
            //     arr.push(vehicleId);
            // }
            var queryParamsArr = [];

            var trs = $("#dataTable tbody tr:visible");

            for (var i = 0; i < trs.length; i++) {
                var thisTr = $(trs[i]);

                var data = myTable.row(thisTr).data();

                var groupId = data[data.length - 1];
                var cardAndDriver = data[data.length - 2].split('_');
                queryParamsArr.push({
                    groupId: groupId,
                    cardNumber: cardAndDriver[0],
                    driverName: cardAndDriver[1]
                });
            }
            // for(var i =0; i<dataListArray.length; i++){
            //     if(arr.indexOf(dataListArray[i].driverNameCardNumber)!==-1){
            //         var queryParams = {};
            //         queryParams.groupId = dataListArray[i].groupId;
            //         queryParams.cardNumber = dataListArray[i].driverNameCardNumber.split('_')[0];
            //         queryParams.driverName = dataListArray[i].driverNameCardNumber.split('_')[1];
            //         queryParamsArr.push(queryParams);
            //     }
            // }
            var queryParamsStr = JSON.stringify(queryParamsArr);
            queryParamsStr = queryParamsStr.replace(/"/g, '\'');
            var paramer = {
                time: $('#month').val().replace(/-/g, ''),
                queryParamsStr: queryParamsStr
            };
            var url = "/clbs/m/reportManagement/driverScore/exportDriverScoreProfessionalDetails";
            exportExcelUseForm(url, paramer);
        },
        export: function () {
            if ($("#dataTable tbody tr td").hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据,无法导出");
                return;
            }
            if (getRecordsNum() > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var paramer = {
                time: $('#month').val().replace(/-/g, ''),
                groupId: groupId.replace(/,/g, '')
            };
            var url = "/clbs/m/reportManagement/driverScore/exportIcCardDriverInfoList";
            exportExcelUseForm(url, paramer);
        },
        detailExport: function () {
            var data = dataListArray[activeRowIndex];
            var array = data.driverNameCardNumber.split('_');
            if (getRecordsNum() > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var paramer = {
                time: $('#detailMonth').html().replace(/-/g, ''),
                groupId: data.groupId,
                cardNumber: array[0],
                driverName: array[1]
            };

            var url = "/clbs/m/reportManagement/driverScore/exportDriverScoreProfessionalDetail";
            exportExcelUseForm(url, paramer);
        },
        getMonth: function (month) {
            if (month >= 1 && month <= 9) {
                return "0" + month
            }
        },
        getNow: function (s) {
            return s < 10 ? "0" + s : s;
        },
        //获取上月时间，格式YYYY-MM
        getYesterMonth: function (del, needSecond) {
            var getNow = driverRatingReport.getNow;
            var myDate = new Date();
            var year = myDate.getFullYear();
            var month = myDate.getMonth() + 1;
            var date = myDate.getDate();
            var h = myDate.getHours();
            var m = myDate.getMinutes();
            var s = myDate.getSeconds();
            var now;
            if (month == 1) {
                year = year - 1;
                month = 12;
            } else {
                month = month - del; // 几月前
            }

            if (needSecond) {
                now = year + "-" + getNow(month) + "-" + getNow(date) + " " + getNow(h) + ":" + getNow(m) + ":" + getNow(s);
            } else {
                now = year + "-" + getNow(month)
            }
            return now;
        },
    };
    $(function () {
        driverRatingReport.init();

        $('#moreExport').on('click', driverRatingReport.moreExport);
        $('#exportAlarm').on('click', driverRatingReport.export);
        $('#detailExport').on('click', driverRatingReport.detailExport);

        $('#detailTab').on('click', function () {
            if (riskEventFlag) {
                riskEventFlag = false;
                setTimeout(driverRatingReport.riskEventChart, 300);
            }
        });
        $('#situationTab').on('click', function () {
            setTimeout(driverRatingReport.modalEchartInit, 300);
        });
        // $('#dataTable tbody').on('click', 'tr', driverRatingReport.trClickFun);
        // $('#driverModal #doXAdd,#driverModal #doCloseAdd').on('click', function () {
        //     $('#driverModal').removeClass('active');
        //     $('#dataTable tr.active').removeClass('active');
        // });
        driverRatingReport.getTable('#dataTable');
        $("#groupSelect").bind("click", showMenuContent);

        var now = new Date();
        // now.setMonth(now.getMonth() - 1);
        // var lastMonth = now.getFullYear().toString() + '-' + driverRatingReport.appendZero(now.getMonth() + 1);
        // now.setMonth(now.getMonth() + 1);
        // var maxMonth = now.getFullYear().toString() + '-' + driverRatingReport.appendZero(now.getMonth() + 1)+'-01 00:00:00';

        var lastMonthDate = new Date();
        lastMonthDate.setMonth(lastMonthDate.getMonth() - 1);
        var lastYear = lastMonthDate.getFullYear();
        var lastMonthText = lastMonthDate.getMonth() + 1;
        if (lastMonthText >= 1 && lastMonthText <= 9) {
            lastMonthText = "0" + lastMonthText
        }
        var lastMonth = lastYear + '-' + lastMonthText;

        var year = now.getFullYear();
        var month = now.getMonth();
        if (month === 0) {
            month = 12;
            year -= 1;
        }
        if (month >= 1 && month <= 9) {
            month = "0" + month
        }

        var days = new Date(year, month, 0).getDate();
        var maxMonth = year + '-' + (month) + '-' + days + " 23:59:59";
        $('#month').val(lastMonth);
        laydate.render({
            elem: '#month'
            , type: 'month'
            , max: maxMonth
            , btns: ['clear', 'confirm']
            // ,change: function(value, date, endDate){
            //     console.log(value)
            //     var $month = $('#month');
            //     $month.val(value);
            //     $month.blur();
            //     $('.layui-laydate').remove();
            // },
            , ready: function (date) {
                $("#layui-laydate1").off('click').on('click', '.laydate-month-list li', function () {
                    $("#layui-laydate1").remove();
                });
            },
            // 点击月份立即改变input值
            change: function (value, dates, edate) {
                // var newYear =Number( maxMonth.split('-')[0]);
                // var newMonth = Number(maxMonth.split('-')[1]);
                // if(dates.year >= newYear && dates.month >= newMonth){
                //     value = newYear +'-'+ newMonth;
                // }
                var final;

                // 获取选择的年月
                var year = dates.year;
                var month = dates.month;
                // 获取当前实际的年月
                var getNow = driverRatingReport.getNow;
                var myDate = new Date();
                var nowYear = myDate.getFullYear();
                var nowMonth = myDate.getMonth() + 1;

                if (year > nowYear) {
                    final = driverRatingReport.getYesterMonth(1);
                } else if (year == nowYear) {
                    if (month >= nowMonth) {
                        final = driverRatingReport.getYesterMonth(1)
                    } else {
                        final = value;
                    }
                } else {
                    final = value;
                }
                $('#month').val(final);
            },
        });

        //导出
        /*$("#exportAlarm").bind("click", driverRatingReport.exportAlarm);*/
        $("#refreshTable").bind("click", driverRatingReport.refreshTable);

        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                search_ztree('treeDemo', 'groupSelect', 'group');
                treeObj.checkAllNodes(false);
            }
        });
        /**
         * 监控对象树模糊查询
         */
        $("#groupSelect").on('input propertychange', function (value) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.checkAllNodes(false);
            search_ztree('treeDemo', 'groupSelect', 'group');
        });

        $(document).on('click', '#dataTable td', driverRatingReport.tdClick);
        $(document).on('click', driverRatingReport.closeDetail);
        $('#detail').on('click', function (event) {
            event.stopPropagation();
        });
        $('.custom-tab').on('click', function () {
            var $this = $(this);
            if ($this.hasClass('active')) {
                return;
            }
            var $siblings = $this.siblings();
            $this.addClass('active');
            $siblings.removeClass('active');

            var href = $this.find('a').attr('href');
            var href2 = $siblings.find('a').attr('href');
            $(href2).hide();
            $(href).show();
        });
        // 点击页面其他区域 监控对象弹框隐藏
        $(document).bind("click", function (event) {
            if (!$('#dropDown-q').is(event.target) && $('#dropDown-q').has(event.target).length == 0) {
                $('#dropDown-q').removeClass('show');
            }
            if (!$('#popup-q').is(event.target) && $('#popup-q').has(event.target).length == 0) {
                $('#popup-q').removeClass('show');
            }
        });

        // driverRatingReport.getCheckedNodes('treeDemo');
        $('#stretch1-body').on('scroll', function () {
            if (window.isAppending || detailAjaxDataParam.searchAfter[0] === ',') {
                return;
            }
            var $riskEvenTable = $('#riskEvenTable');
            var $container = $('#stretch1-body');
            var scrollTop = $container.scrollTop();
            var scrollHeight = $riskEvenTable[0].scrollHeight;
            var height = $container.height();

            if (scrollTop > (scrollHeight - height) - 50) {

                window.isAppending = true;
                var url = "/clbs/m/reportManagement/driverScore/getIcCardDriverEventList";
                json_ajax("POST", url, "json", true, detailAjaxDataParam, driverRatingReport.detailTableCallback);

            }
        });

    })
}(window, $))