(function (window, $) {
    var myTable;
    var orgDetailTable;
    var checkFlag = false; // 判断组织节点是否是勾选操作
    var dbFlag = false;
    var size; // 当前权限监控对象数量
    var zTreeIdJson = {};
    var ifAllCheck = true; // 刚进入页面小于100自动勾选
    var groupId = [];
    var groupNameDict = {};
    var fuzzyQueryParam = '';
    var searchMonth = '';
    var detailFuzzyQueryParam = '';
    var curSelectOrgId = ''; // 当前选择的企业id
    var curSelectOrgName = ''; // 当前选择的企业id
    var curSelectTime = ''; // 详情抽屉当前选择的时间
    var myBrowserType = 0; // 火狐浏览器双击计数

    alarmUnderreport = {
        // 组织树相关方法
        init: function () {
            alarmUnderreport.treeInit();
            alarmUnderreport.monthDataInit();

            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            for (var i = 0; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-text").html(menu_text);
        },
        treeInit: function () {
            //车辆树
            var setting = {
                async: {
                    url: alarmUnderreport.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "type": "multiple",
                        "icoType": "0"
                    },
                    dataFilter: alarmUnderreport.ajaxDataFilter
                },
                check: {
                    enable: true,
                    chkStyle: "checkbox",
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
                    beforeClick: alarmUnderreport.beforeClickVehicle,
                    onAsyncSuccess: alarmUnderreport.zTreeOnAsyncSuccess,
                    beforeCheck: alarmUnderreport.zTreeBeforeCheck,
                    onCheck: alarmUnderreport.onCheckVehicle,
                    onNodeCreated: alarmUnderreport.zTreeOnNodeCreated,
                    onExpand: alarmUnderreport.zTreeOnExpand,
                    onDblClick: alarmUnderreport.onDblClickVehicle,
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },
        onDblClickVehicle: function (e, treeId, treeNode) {
            var userAgent = navigator.userAgent; // 取得浏览器的userAgent字符串
            if (userAgent.indexOf("Firefox") > -1) return; // 判断是否Firefox浏览器
            if (!treeNode) return;
            dbFlag = true;
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.checkAllNodes(false);
            alarmUnderreport.getCheckedNodes("treeDemo");
            // $("#groupSelect").val(treeNode.name);
            // search_ztree('treeDemo', 'groupSelect', 'group');
            treeObj.checkNode(treeNode, !treeNode.checked, false, true);
        },
        getTreeUrl: function (treeId) {
            return "/clbs/m/basicinfo/enterprise/professionals/tree";
        },
        validates: function () {
            return $("#lifeCycleForm").validate({
                rules: {
                    month: {
                        required: true
                    },
                    groupSelect: {
                        zTreeCheckGroup: "treeDemo"
                    }
                },
                messages: {
                    month: {
                        required: "请选择月份",
                    },
                    groupSelect: {
                        zTreeCheckGroup: '请选择组织',
                    },
                }
            }).form();
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            size = responseData.length;
            for (var i = 0; i < size; i++) {
                var item = responseData[i];
                groupNameDict[item.id] = item.name;
            }
            return responseData;
        },
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            // zTree.checkNode(treeNode, !treeNode.checked, true, true);
            var userAgent = navigator.userAgent; // 取得浏览器的userAgent字符串
            if (userAgent.indexOf("Firefox") > -1) { // 判断是否Firefox浏览器
                myBrowserType++;
                setTimeout(function() {
                    if(myBrowserType == 2) {
                        zTree.checkAllNodes(false);
                        zTree.checkNode(treeNode, !treeNode.checked, false, true);
                    } else if(myBrowserType == 1) {
                        zTree.checkNode(treeNode, !treeNode.checked, true, true);
                    }
                    myBrowserType = 0;
                }, 300);
            } else {
                zTree.checkNode(treeNode, !treeNode.checked, true, true);
            }
            return false;
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            // if (ifAllCheck) {
            //     treeObj.checkAllNodes(true);
            //     treeObj.expandAll(true);
            // }
            alarmUnderreport.getCharSelect(treeObj);
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked && !dbFlag) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
                        nodes = zTree
                        .getCheckedNodes(true),
                        v = "";
                    var nodesLength = 0;
                    // json_ajax("post", "/clbs/m/personalized/ico/getVehicleCount",
                    //     "json", false, {
                    //         "parentId": treeNode.id,
                    //         "type": treeNode.type
                    //     },
                    //     function (data) {
                    //         if (data.success) {
                    //             nodesLength += data.obj;
                    //         } else {
                    //             layer.msg(data.msg);
                    //         }
                    //     });

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
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
                        nodes = zTree
                        .getCheckedNodes(true),
                        v = "";
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
                if (myBrowserType > 1) {
                    nodesLength = 1;
                }
                if (nodesLength > 500) {
                    layer.msg('组织最大勾选500个' + '<br/>双击名称可选中本组织');
                    flag = false;
                }
            }
            if (flag) {
                //若组织节点已被勾选，则是勾选操作，改变勾选操作标识
                if (treeNode.type == "group" && !treeNode.checked) {
                    checkFlag = true;
                }
            }
            if (treeNode.checked) {
                dbFlag = false;
            }
            return flag;
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            var nodes = zTree.getCheckedNodes(true);
            if (nodes && nodes.length > 500) {
                zTree.checkAllNodes(false);
                layer.msg('组织最大勾选500个' + '<br/>双击名称可选中本组织');
            }
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                zTree.expandNode(treeNode, true, true, false, false); // 展开节点
                setTimeout(() => {
                    alarmUnderreport.getCheckedNodes("treeDemo");
                    alarmUnderreport.validates();
                }, 600);
            }
            alarmUnderreport.getCheckedNodes("treeDemo");
            alarmUnderreport.getCharSelect(zTree);
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
                nodes = zTree.getCheckedNodes(true);
            groupId = [];
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (treeId == "treeDemo" && nodes[i].type == "group") {
                    groupId.push(nodes[i].uuid);
                }
            }
        },

        // 列表及图形统计相关
        getTable: function (table) {
            $('.toggle-vis').prop('checked', true);
            myTable = $(table).DataTable({
                "columnDefs": [{
                    "targets": 0,
                    "searchable": false // 禁止第一列参与搜索
                }, {
                    "orderable": false
                }],
                "destroy": true,
                "lengthChange": true, // 是否允许用户自定义显示数量
                "bPaginate": true, // 翻页功能
                "bFilter": true, // 列筛序功能
                "searching": true, // 本地搜索
                "ordering": false, // 排序功能
                "Info": true, // 页脚信息
                "autoWidth": true, // 自动宽度
                "stripeClasses": [],
                "pageLength": 10,
                "lengthMenu": [5, 10, 20, 50, 100],
                "pagingType": "simple_numbers", // 分页样式
                "dom": "t" + "<'row'<'col-md-2 col-sm-12 col-xs-12 noPadding'l><'col-md-4 col-sm-12 col-xs-12 noPadding'i><'col-md-6 col-sm-12 col-xs-12 noPadding'p>>",
                "oLanguage": { // 国际语言转化
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
                }
            });
            //显示隐藏列
            $('.toggle-vis').off('change').on('change', function (e) {
                var column = myTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });
            $("#search_button").on("click", function () {
                var tsval = $("#simpleQueryParam").val();
                myTable.columns(1).search(tsval, false, false).draw();
            });
        },
        initTable: function () {
            var time = $('#month').val();
            searchMonth = time.replace('-', '');
            var ajaxDataParam = {
                "organizationIds": groupId.join(','),
                "month": searchMonth,
            };
            var url = "/clbs/adas/omissionAlarm/list";
            json_ajax("POST", url, "json", true, ajaxDataParam, alarmUnderreport.getCallback);
        },
        drawTableCallbackFun: function () {
            var checks = $("#Ul-menu-text .toggle-vis");
            for (var i = 0, len = checks.length; i < len; i++) {
                (function (index) {
                    var columnIndex = $(checks[index]).attr('data-column'),
                        checked = $(checks[index]).prop("checked");
                    var column = myTable.dataTable.column(columnIndex);
                    column.visible(checked);
                })(i)
            }
        },
        //刷新列表
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            alarmUnderreport.initTable();
        },
        setNewMonth: function (number) {
            var curTime = $('#month').val();
            if (!curTime) return;
            var curTimeArr = curTime.split('-');
            var currentYear = parseInt(curTimeArr[0]);
            var currentMonth = parseInt(curTimeArr[1]);
            if (number === 0) {
                var dateList = new Date();
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                $('#month').val(vYear + '-' + fillZero(vMonth + 1));
            } else {
                var vMonth = currentMonth + number;
                if (vMonth === 0) {
                    currentYear--;
                    vMonth = 12;
                }
                $('#month').val(currentYear + '-' + fillZero(vMonth));
            }
        },
        inquireClick: function (number) {
            if (number != 1) {
                alarmUnderreport.setNewMonth(number);
            }
            alarmUnderreport.getCheckedNodes('treeDemo');
            if (!alarmUnderreport.validates()) {
                return;
            }
            alarmUnderreport.initTable();
            var time = $('#month').val();
            searchMonth = time.replace('-', '');
            var ajaxDataParam = {
                "organizationIds": groupId.join(','),
                "month": searchMonth,
            };

            var url = '/clbs/adas/omissionAlarm/orgDayCount';
            json_ajax("post", url, "json", true, ajaxDataParam, function (result) {
                if (result.success) {
                    $('#stretch3-body').slideDown();
                    alarmUnderreport.chartRender(result, 'accountedEchart', 'trendEchart');
                } else {
                    var result = {
                        obj: {
                            data: []
                        }
                    };
                    alarmUnderreport.chartRender(result, 'accountedEchart', 'trendEchart');
                    $('#stretch3-body').slideUp();
                }
            });
        },
        getTotal: function (item) {
            return item.courseDeviation +
                item.refuseStipulatePathDriving +
                item.enterArea +
                (item.outArea || 0) +
                item.overSpeed +
                item.certificateAndPersonMismatch +
                item.fatigueDrive;
        },
        getCallback: function (result) {
            var dataListArray = []; //用来储存显示数据
            $("#simpleQueryParam").val("");
            if (result.success) {
                var list = result.obj.data; // 集合
                if (list && list.length != 0) {
                    for (var i = 0; i < list.length; i++) {
                        $("#exportAlarm").removeAttr("disabled");
                        var orgId = list[i].orgId;
                        var orgName = list[i].orgName;
                        var dateList = [
                            '<span class="tdInfo" data-orgId="' + orgId + '" data-orgName="' + orgName + '">' + parseInt(i + 1) + '</span>',
                            orgName,
                            list[i].courseDeviation,
                            list[i].refuseStipulatePathDriving,
                            list[i].enterArea,
                            list[i].outArea,
                            list[i].overSpeed,
                            list[i].certificateAndPersonMismatch,
                            list[i].fatigueDrive,
                            alarmUnderreport.getTotal(list[i]),
                        ];
                        dataListArray.push(dateList);

                        $('#dataTable tbody').off().on('click', 'tr', function (e) {
                            if ($(this).find('.dataTables_empty').length === 0) {
                                alarmUnderreport.trClickFun(e);
                            }
                        });
                    }
                } else {
                    $("#exportAlarm").attr("disabled", "disabled");
                }
            } else if (result.msg) {
                layer.msg(result.msg);
            }
            alarmUnderreport.reloadData(dataListArray);
        },
        reloadData: function (dataList) {
            var currentPage = myTable.page();
            myTable.clear();
            myTable.rows.add(dataList);
            myTable.page(currentPage).draw(false);
            myTable.columns(1).search('', false, false).draw();
        },
        /**
         * 图表渲染
         * @param result 数据
         * @param accountedElement 饼图渲染元素
         * @param trendElement 柱状图渲染元素
         * */
        chartRender: function (result, accountedElement, trendElement) {
            // 饼图图表数据
            var totalObj = {
                courseDeviation: 0, // 路线偏离
                refuseStipulatePathDriving: 0, // 不按规定路线行驶
                outArea: 0, // 出区域
                inArea: 0, // 进区域
                overSpeed: 0, // 超速
                certificateAndPersonMismatch: 0, // 人证不符
                fatigueDrive: 0, // 疲劳驾驶
            };

            // 柱状图图表数据
            var statisticInfo = result.obj.data || [];
            var echartData = {
                xData: [],
                courseDeviation: [], // 路线偏离
                refuseStipulatePathDriving: [], // 不按规定路线行驶
                outArea: [], // 出区域
                inArea: [], // 进区域
                overSpeed: [], // 超速
                certificateAndPersonMismatch: [], // 人证不符
                fatigueDrive: [], // 疲劳驾驶
                ringRate: [], // 环比
                monthRate: [], // 同比
            };
            for (var i = 0; i < statisticInfo.length; i++) {
                var item = statisticInfo[i];
                echartData.xData.push(item.day);
                echartData.courseDeviation.push(item.courseDeviation || 0);
                echartData.refuseStipulatePathDriving.push(item.refuseStipulatePathDriving || 0);
                echartData.outArea.push(item.outArea || 0);
                echartData.inArea.push(item.enterArea || 0);
                echartData.overSpeed.push(item.overSpeed || 0);
                echartData.certificateAndPersonMismatch.push(item.certificateAndPersonMismatch || 0);
                echartData.fatigueDrive.push(item.fatigueDrive || 0);
                echartData.ringRate.push(item.ringRate || 0);
                echartData.monthRate.push(item.monthRate || 0);

                totalObj.courseDeviation += item.courseDeviation || 0;
                totalObj.refuseStipulatePathDriving += item.refuseStipulatePathDriving || 0;
                totalObj.outArea += item.outArea || 0;
                totalObj.inArea += item.enterArea || 0;
                totalObj.overSpeed += item.overSpeed || 0;
                totalObj.certificateAndPersonMismatch += item.certificateAndPersonMismatch || 0;
                totalObj.fatigueDrive += item.fatigueDrive || 0;
            }
            alarmUnderreport.trendEchart(echartData, trendElement);

            var accountedData = [{
                    value: totalObj.courseDeviation,
                    name: '路线偏离'
                },
                {
                    value: totalObj.inArea,
                    name: '进区域'
                },
                {
                    value: totalObj.outArea,
                    name: '出区域'
                },
                {
                    value: totalObj.overSpeed,
                    name: '超速'
                },
                {
                    value: totalObj.refuseStipulatePathDriving,
                    name: '不按规定路线运行'
                },
                {
                    value: totalObj.certificateAndPersonMismatch,
                    name: '人证不符'
                },
                {
                    value: totalObj.fatigueDrive,
                    name: '疲劳驾驶'
                },
            ];
            alarmUnderreport.accountedEchart(accountedData, accountedElement);

            // 详情抽屉左侧信息渲染
            if (trendElement === 'trendDetailEchart') {
                $('#orgName').text(curSelectOrgName);
                for (key in totalObj) {
                    $('#' + key).text(totalObj[key]);
                }
            }
        },
        /**
         * 报警漏报占比饼图
         * @param data 饼图数据
         * @param element 渲染元素
         * */
        accountedEchart: function (data, element) {
            var myChart = echarts.init(document.getElementById(element));
            var option = {
                title: {
                    text: '报警漏报占比',
                    left: 'center'
                },
                color: ['rgb(58,168,255)', 'rgb(89,212,212)', 'rgb(78,203,115)',
                    'rgb(251,212,55)', 'rgb(242,99,123)', 'rgb(151,95,229)', 'rgb(82,84,207)'
                ],
                tooltip: {
                    trigger: 'item',
                    formatter: '{b} : {c} ({d}%)'
                },
                legend: {
                    orient: 'horizontal',
                    bottom: 'bottom',
                    data: ['路线偏离', '进区域', '出区域', '超速', '不按规定路线运行', '人证不符', '疲劳驾驶']
                },
                series: [{
                    name: '报警漏报占比',
                    type: 'pie',
                    radius: element === 'accountedEchart' ? '55%' : '40%',
                    data: data,
                    emphasis: {
                        itemStyle: {
                            shadowBlur: 10,
                            shadowOffsetX: 0,
                            shadowColor: 'rgba(0, 0, 0, 0.5)'
                        }
                    }
                }]
            };
            myChart.clear();
            myChart.setOption(option);
            window.onresize = function () {
                myChart.resize();
            };
        },
        /**
         * 报警漏报趋势图表
         * @param echartData 轴数据
         * @param element 渲染元素
         * */
        trendEchart: function (echartData, element) {
            var myChart = echarts.init(document.getElementById(element));
            var option = {
                title: {
                    text: '报警漏报趋势',
                    left: '60'
                },
                tooltip: {
                    trigger: 'axis',
                    formatter: function (series) {
                        var time = series[0].axisValue;
                        var timeStr = time.substring(0, 4) + '-' + time.substring(4, 6) + '-' + time.substring(6, 8);
                        var result = '<div class="formatterBox"><h4>' + timeStr + '</h4>';
                        for (var i = 0; i < series.length; i++) {
                            var item = series[i];
                            var unit = '%';
                            var showData = item.data;
                            var seriesName = item.seriesName;
                            if (seriesName.indexOf('环比') === -1 && seriesName.indexOf('同比') === -1) {
                                unit = '';
                            }
                            if (unit) {
                                if (item.data < 0) {
                                    showData = '- ' + Math.abs(item.data) + unit;
                                } else if (item.data > 0) {
                                    showData = '+ ' + item.data + unit;
                                }
                            }
                            if (i < 7) {
                                result += '<p><i style="background-color: ' + item.color + '"></i>' + seriesName + '：' + showData + '</p>';
                            } else {
                                result += '<span><i style="background-color: ' + item.color + '"></i>' + seriesName + '：' + showData + '</span>';
                            }
                        }
                        result += '</div>';
                        return result;
                    }
                },
                color: ['rgb(58,168,255)', 'rgb(89,212,212)', 'rgb(78,203,115)',
                    'rgb(251,212,55)', 'rgb(242,99,123)', 'rgb(151,95,229)', 'rgb(82,84,207)'
                ],
                grid: {
                    top: 60,
                    right: 50,
                    bottom: 100
                },
                legend: [{
                        bottom: 60,
                        data: ['路线偏离', '进区域', '出区域', '超速', '不按规定路线运行', '人证不符', '疲劳驾驶']
                    },
                    {
                        bottom: 30,
                        data: ['环比率', '同比率']
                    }
                ],
                dataZoom: [{ // 初始化滚动条
                    type: 'slider',
                    show: true,
                    xAxisIndex: [0],
                    left: 100,
                    right: 60,
                    bottom: -5,
                    start: 0,
                    // end: element === 'trendDetailEchart' ? 50 : 70,
                    maxValueSpan: element === 'trendDetailEchart' ? 16 : 20,
                }],
                xAxis: [{
                    type: 'category',
                    data: echartData.xData,
                    axisPointer: {
                        type: 'shadow'
                    },
                    axisLabel: {
                        formatter: (val) => {
                            var len = val.length;
                            return val.substring(len - 2, len);
                        }
                    }
                }],
                yAxis: [{
                        type: 'value',
                        name: '',
                        position: 'left',
                        axisLabel: {
                            formatter: '{value}'
                        },
                    },
                    {
                        type: 'value',
                        name: '',
                        position: 'right',
                        axisLabel: {
                            formatter: '{value} %'
                        },
                    }
                ],
                series: [{
                        name: '路线偏离',
                        type: 'bar',
                        barWidth: 10,
                        barGap: '30%',
                        data: echartData.courseDeviation,
                    }, {
                        name: '进区域',
                        type: 'bar',
                        barWidth: 10,
                        barGap: '30%',
                        data: echartData.inArea,
                    }, {
                        name: '出区域',
                        type: 'bar',
                        barWidth: 10,
                        barGap: '30%',
                        data: echartData.outArea,
                    }, {
                        name: '超速',
                        type: 'bar',
                        barWidth: 10,
                        barGap: '30%',
                        data: echartData.overSpeed,
                    }, {
                        name: '不按规定路线运行',
                        type: 'bar',
                        barWidth: 10,
                        barGap: '30%',
                        data: echartData.refuseStipulatePathDriving,
                    }, {
                        name: '人证不符',
                        type: 'bar',
                        barWidth: 10,
                        barGap: '30%',
                        data: echartData.certificateAndPersonMismatch,
                    }, {
                        name: '疲劳驾驶',
                        type: 'bar',
                        barWidth: 10,
                        barGap: '30%',
                        data: echartData.fatigueDrive,
                    },
                    {
                        name: '环比率',
                        type: 'line',
                        yAxisIndex: 1,
                        symbol: 'none', // 去掉点
                        smooth: true, // 曲线平滑
                        data: echartData.ringRate
                    },
                    {
                        name: '同比率',
                        type: 'line',
                        yAxisIndex: 1,
                        symbol: 'none', // 去掉点
                        smooth: true, // 曲线平滑
                        data: echartData.monthRate
                    },
                ]
            };
            myChart.clear();
            myChart.setOption(option);
            window.onresize = function () {
                myChart.resize();
            };
        },
        // 列表行点击查看企业超速统计明细信息
        trClickFun: function (e) {
            e.stopPropagation();
            var _this = $(e.target);
            if (e.target.nodeName !== 'TR') {
                _this = $(e.target).closest('tr');
            }
            if (_this.hasClass('active')) {
                $('#detail').removeClass('active');
            } else {
                $('#detailMonth').html($('#month').val());
                $('#detailSimpleQueryParam').val('');
                $('#detail').addClass('active');
                var orgId = _this.find('.tdInfo').attr('data-orgId');
                curSelectOrgId = orgId;
                curSelectOrgName = _this.find('.tdInfo').attr('data-orgName');
                var time = $('#month').val().replace('-', '');
                curSelectTime = time;
                alarmUnderreport.getOrgDetailInfo();
                // alarmUnderreport.initDetailTable();
            }
            _this.toggleClass('active');
            _this.siblings('tr').removeClass('active');
        },
        /**
         * 获取企业报警漏报明细数据
         * */
        getOrgDetailInfo: function () {
            var ajaxDataParam = {
                "organizationId": curSelectOrgId,
                "month": curSelectTime
            };
            // 获取详情图表数据
            var url = '/clbs/adas/omissionAlarm/orgDetail';
            json_ajax("post", url, "json", true, ajaxDataParam, function (result) {
                if (result.success) {
                    alarmUnderreport.chartRender(result, 'accountedDetailEchart', 'trendDetailEchart');
                } else {
                    var result = {
                        obj: {
                            data: []
                        }
                    };
                    alarmUnderreport.chartRender(result, 'accountedDetailEchart', 'trendDetailEchart');
                    if (result.msg) {
                        layer.msg(result.msg);
                    }
                }
            });
        },
        // 详情抽屉列表渲染
        initDetailTable: function () {
            var columnDefs = [{
                //第一列，用来显示序号
                "searchable": false,
                "targets": 0
            }];
            var columns = [{
                    //第一列，用来显示序号
                    "data": null,
                    "class": "text-center"
                },
                {
                    "data": "monitorName",
                    "class": "text-center",
                },
                {
                    "data": "plateColor",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data) {
                            return getPlateColor(data);
                        }
                        return '';
                    },
                }, {
                    "data": "objectType",
                    "class": "text-center"
                },
                {
                    "data": "less20PerUnder5min",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data ? data : 0;
                    }
                },
                {
                    "data": "less20Per5To10min",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data ? data : 0;
                    }
                }, {
                    "data": "less20PerOver10min",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data ? data : 0;
                    }
                },
                {
                    "data": "cent20To50Per5To10min",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data ? data : 0;
                    }
                },
                {
                    "data": "cent20To50PerOver10min",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data ? data : 0;
                    }
                }, {
                    "data": "cent20To50PerUnder5min",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data ? data : 0;
                    }
                },
                {
                    "data": "over50PerUnder5min",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data ? data : 0;
                    }
                },
                {
                    "data": "over50Per5To10min",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data ? data : 0;
                    }
                }, {
                    "data": "over50PerOver10min",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data ? data : 0;
                    }
                }, {
                    "data": "totalNum",
                    "class": "text-center"
                },
            ];

            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#detailSimpleQueryParam').val();
                detailFuzzyQueryParam = $('#detailSimpleQueryParam').val();
                d.groupId = curSelectOrgId;
                d.time = curSelectTime;
            };
            //表格setting
            var setting = {
                listUrl: "/clbs/cb/cbReportManagement/speedingStatistics/group/upSpeedInfoList",
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'detailTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                drawCallbackFun: function () {
                    var api = orgDetailTable.dataTable;
                    var startIndex = api.context[0]._iDisplayStart; //获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                    });
                },
                pagingType: 'simple',
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true
            };
            orgDetailTable = new TG_Tabel.createNew(setting);
            orgDetailTable.init();

            $('#searchDetailTable').on('click', function (e) {
                orgDetailTable.refresh();
            });
        },
        left_arrow: function () {
            var detailMonth = $('#detailMonth').html();
            var date = new Date(detailMonth.replace(/-/g, '/') + '/01 00:00:00');
            var now = new Date();
            var dateYear = date.getFullYear();
            var dateMonth = date.getMonth();
            var nowYear = now.getFullYear();
            var nowMonth = now.getMonth();
            if (dateYear >= nowYear && dateMonth > nowMonth) {
                return;
            }
            date.setMonth(date.getMonth() - 1);
            dateYear = date.getFullYear();
            dateMonth = date.getMonth() + 1;
            var newDate = dateYear + '-' + fillZero(dateMonth);
            $('#detailMonth').html(newDate);
            curSelectTime = newDate.replace('-', '');
            alarmUnderreport.getOrgDetailInfo();
            alarmUnderreport.initDetailTable();
        },
        right_arrow: function () {
            var detailMonth = $('#detailMonth').html();
            var date = new Date(detailMonth.replace(/-/g, '/') + '/01 00:00:00');
            var now = new Date();
            var dateYear = date.getFullYear();
            var dateMonth = date.getMonth();
            var nowYear = now.getFullYear();
            var nowMonth = now.getMonth();
            if (dateYear >= nowYear && dateMonth + 1 > nowMonth) {
                return;
            }
            date.setMonth(date.getMonth() + 1);
            dateYear = date.getFullYear();
            dateMonth = date.getMonth() + 1;
            var newDate = dateYear + '-' + fillZero(dateMonth);
            $('#detailMonth').html(newDate);
            curSelectTime = newDate.replace('-', '');
            alarmUnderreport.getOrgDetailInfo();
            alarmUnderreport.initDetailTable();
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
                month: searchMonth,
                organizationIds: groupId.join(','),
                simpleQueryParam: fuzzyQueryParam,
            };
            var url = "/clbs/adas/omissionAlarm/export";
            json_ajax("post", url, "json", true, paramer, function (result) {
                if (result.success) {
                    layer.confirm(exportTitle, {
                        title: '操作确认',
                        icon: 3, // 问号图标
                        btn: ['确定', '导出管理'] //按钮
                    }, function () {
                        layer.closeAll();
                    }, function () {
                        layer.closeAll();
                        // 打开导出管理弹窗
                        pagesNav.showExportManager();
                    });
                } else if (result.msg) {
                    layer.msg(result.msg);
                }
            });
        },
        monthDataInit: function () {
            var now = new Date();
            var year = now.getFullYear();
            var month = now.getMonth() + 1;
            if (month > 12) {
                year += 1;
                month = 1;
            }
            if (month >= 1 && month <= 9) {
                month = "0" + month
            }
            var lastMonthDate = new Date();
            lastMonthDate.setMonth(lastMonthDate.getMonth());
            var lastYear = lastMonthDate.getFullYear();
            var lastMonthText = lastMonthDate.getMonth() + 1;
            if (lastMonthText >= 1 && lastMonthText <= 9) {
                lastMonthText = "0" + lastMonthText
            }
            var lastMonth = lastYear + '-' + lastMonthText;
            var maxMonth = year + '-' + (month) + "-01 00:00:00";
            $('#month').val(lastMonth);
            laydate.render({
                elem: '#month',
                type: 'month',
                max: maxMonth,
                btns: ['clear', 'confirm'],
                ready: function (date) {
                    $("#layui-laydate1").off('click').on('click', '.laydate-month-list li', function () {
                        $("#layui-laydate1").remove();
                    });
                },
                // 点击月份立即改变input值
                change: function (value, dates, edate) {
                    var final;

                    // 获取选择的年月
                    var year = dates.year;
                    var month = dates.month;
                    var myDate = new Date();
                    var nowYear = myDate.getFullYear();
                    var nowMonth = myDate.getMonth() + 1;

                    if (year > nowYear) {
                        final = alarmUnderreport.getYesterMonth(1);
                    } else if (year == nowYear) {
                        if (month > nowMonth) {
                            final = alarmUnderreport.getYesterMonth(1)
                        } else {
                            final = value;
                        }
                    } else {
                        final = value;
                    }
                    $('#month').val(final);
                },
            });
        },
        //获取上月时间，格式YYYY-MM
        getYesterMonth: function (del, needSecond) {
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
                now = year + "-" + fillZero(month) + "-" + fillZero(date) + " " + fillZero(h) + ":" + fillZero(m) + ":" + fillZero(s);
            } else {
                now = year + "-" + fillZero(month)
            }
            return now;
        },
        closeDetail: function () {
            $('#dataTable tbody tr').removeClass('active');
            $('#detail').removeClass('active');
            $('#monitorDataTable tbody tr').removeClass('active');
            $('#monitorDetail').removeClass('active');
        },
    };
    $(function () {
        alarmUnderreport.init();
        alarmUnderreport.getTable('#dataTable');
        $("#groupSelect").bind("click", showMenuContent);
        $("#refreshTable").bind("click", alarmUnderreport.refreshTable);
        $('#exportAlarm').on('click', alarmUnderreport.export);
        // $('#detailExport').on('click', alarmUnderreport.detailExport);
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                search_ztree('treeDemo', 'groupSelect', 'group');
                treeObj.checkAllNodes(false);
            }
        });
        // 监控对象树模糊查询
        $("#groupSelect").on('input propertychange', function (value) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.checkAllNodes(false);
            search_ztree('treeDemo', 'groupSelect', 'group');
        });
        // 点击页面其他地方,关闭详情抽屉
        $(document).on('click', alarmUnderreport.closeDetail);
        $('#detail,#monitorDetail').on('click', function (event) {
            event.stopPropagation();
        });
    })
}(window, $));