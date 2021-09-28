(function (window, $) {
    /**
     * 疲劳驾驶企业统计
     * */
    var myTable;
    var orgDetailTable;
    var checkFlag = false;//判断组织节点是否是勾选操作
    var size;//当前权限监控对象数量
    var zTreeIdJson = {};
    var ifAllCheck = true;//刚进入页面小于100自动勾选
    var groupId = [];
    var groupNameDict = {};
    var fuzzyQueryParam = '';
    var searchMonth = '';
    var detailFuzzyQueryParam = '';
    var curSelectOrgId = '';// 当前选择的企业id
    var curSelectTime = '';// 详情抽屉当前选择的时间
    fatigueDrivingOrg = {
        // 组织树相关方法
        init: function () {
            fatigueDrivingOrg.monthDataInit();

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
                    url: fatigueDrivingOrg.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: fatigueDrivingOrg.ajaxDataFilter
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
                    beforeClick: fatigueDrivingOrg.beforeClickVehicle,
                    onAsyncSuccess: fatigueDrivingOrg.zTreeOnAsyncSuccess,
                    beforeCheck: fatigueDrivingOrg.zTreeBeforeCheck,
                    onCheck: fatigueDrivingOrg.onCheckVehicle,
                    onNodeCreated: fatigueDrivingOrg.zTreeOnNodeCreated,
                    onExpand: fatigueDrivingOrg.zTreeOnExpand
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },
        getTreeUrl: function (treeId, treeNode) {
            if (treeId == 'treeDemo') {
                return "/clbs/m/basicinfo/enterprise/professionals/tree";
            }
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
                        required: "请选择日期！",
                    },
                    groupSelect: {
                        zTreeCheckGroup: vehicleSelectGroup,
                    },
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
            fatigueDrivingOrg.getCharSelect(treeObj);
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
                    fatigueDrivingOrg.getCheckedNodes("treeDemo");
                    fatigueDrivingOrg.validates();
                }, 600);
            }
            fatigueDrivingOrg.getCheckedNodes("treeDemo");
            fatigueDrivingOrg.getCharSelect(zTree);
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
                /*"dom": 'tiprl',// 自定义显示项*/
                "lengthChange": true,// 是否允许用户自定义显示数量
                "bPaginate": true, // 翻页功能
                "bFilter": true, // 列筛序功能
                "searching": true,// 本地搜索
                "ordering": false, // 排序功能
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
                myTable.refresh();
            });
        },
        initTable: function () {
            var columnDefs = [{
                //第一列，用来显示序号
                "searchable": false,
                "targets": 0
            }];
            var columns = [
                {
                    //第一列，用来显示序号
                    "data": null,
                    "class": "text-center"
                },
                {
                    "data": "organizationName",
                    "class": "text-center",
                },
                {
                    "data": "monitorNum",
                    "class": "text-center"
                }, {
                    "data": "accumulatedNum",
                    "class": "text-center"
                }, {
                    "data": "dayNum",
                    "class": "text-center"
                }, {
                    "data": "nightNum",
                    "class": "text-center"
                }, {
                    "data": "avgFatigueDuration",
                    "class": "text-center"
                }, {
                    "data": "totalNum",
                    "class": "text-center"
                },
            ];

            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.fuzzyQueryParam = $('#simpleQueryParam').val();
                fuzzyQueryParam = $('#simpleQueryParam').val();
                d.organizationId = groupId.replace(/,/g, '');
                var time = $('#month').val().replace('-', '');
                d.month = time;
                searchMonth = time;
            };
            //表格setting
            var setting = {
                listUrl: "/clbs/cb/cbReportManagement/fatigueDriving/listOrg",
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                drawCallbackFun: function () {
                    var api = myTable.dataTable;
                    var startIndex = api.context[0]._iDisplayStart;//获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                        var organizationId = api.data()[i].organizationId;
                        $(cell).closest('tr').attr('data-orgId', organizationId);
                    });
                },
                ajaxCallBack: fatigueDrivingOrg.drawTableCallbackFun,
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true
            };
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();

            $('#exportAlarm').prop('disabled', false);

            //显示隐藏列
            $('.toggle-vis').off('change').on('change', function (e) {
                var column = myTable.dataTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });

            $('#dataTable tbody').bind('click', 'tr', function (e) {
                if ($(this).find('.dataTables_empty').length === 0) {
                    fatigueDrivingOrg.trClickFun(e);
                }
            });
        },
        drawTableCallbackFun: function () {
            var checks = $("#Ul-menu-text .toggle-vis");
            for (var i = 0, len = checks.length; i < len; i++) {
                (function (index) {
                    var columnIndex = $(checks[index]).attr('data-column'), checked = $(checks[index]).prop("checked");
                    var column = myTable.dataTable.column(columnIndex);
                    column.visible(checked);
                })(i)
            }
        },
        //刷新列表
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            myTable.refresh();
        },
        inquireClick: function (number) {
            if (number != 1) {
                fatigueDrivingOrg.setNewDay(number);
            }
            fatigueDrivingOrg.getCheckedNodes('treeDemo');
            if (!fatigueDrivingOrg.validates()) {
                return;
            }

            fatigueDrivingOrg.initTable();
            var time = $('#month').val().replace('-', '');
            var ajaxDataParam = {
                "organizationId": groupId.replace(/,/g, ''),
                "month": time,
                "isSingle": 1,
            };
            var url = '/clbs/cb/cbReportManagement/fatigueDriving/getOrgGraphicsData';
            json_ajax("post", url, "json", true, ajaxDataParam, function (result) {
                if (result.success) {
                    $('#stretch3-body').slideDown();
                    fatigueDrivingOrg.chartRender(result, 'accountedEchart', 'trendEchart');
                } else {
                    var result = {
                        obj: {
                            statisticInfo: []
                        }
                    };
                    fatigueDrivingOrg.chartRender(result, 'accountedEchart', 'trendEchart');
                    $('#stretch3-body').slideUp();
                }
            });
        },
        /**
         * 图表渲染
         * @param result 数据
         * @param accountedElement 饼图渲染元素
         * @param trendElement 柱状图渲染元素
         * */
        chartRender: function (result, accountedElement, trendElement) {
            var data = result.obj;
            var accountedData = [
                {value: data.totalAccumulatedNum, name: '累计疲劳'},
                {value: data.totalDayNum, name: '日间疲劳'},
                {value: data.totalNightNum, name: '夜间疲劳'},
            ];
            fatigueDrivingOrg.accountedEchart(accountedData, accountedElement);

            // 组装违规趋势走向图表数据
            var statisticInfo = data.statisticInfo;
            var echartData = {
                'xData': [],
                'totalNum': [],
                'monitorNum': [],
                'totalAccumulatedNum': [],
                'totalDayNum': [],
                'totalNightNum': [],
                'sequentialData': [],
                'comparedData': [],
            };
            for (var i = 0; i < statisticInfo.length; i++) {
                var item = statisticInfo[i];
                echartData.xData.push(item.day);
                echartData.totalNum.push(item.totalNum ? item.totalNum : 0);
                echartData.monitorNum.push(item.monitorNum ? item.monitorNum : 0);
                echartData.totalAccumulatedNum.push(item.accumulatedNum ? item.accumulatedNum : 0);
                echartData.totalDayNum.push(item.dayNum ? item.dayNum : 0);
                echartData.totalNightNum.push(item.nightNum ? item.nightNum : 0);
                echartData.sequentialData.push(item.ringRatio);
                echartData.comparedData.push(item.yearRate);
            }

            fatigueDrivingOrg.trendEchart(echartData, trendElement);
        },
        /**
         * 疲劳报警车辆占比情况饼图
         * @param data 饼图数据
         * @param element 渲染元素
         * */
        accountedEchart: function (data, element) {
            var myChart = echarts.init(document.getElementById(element));
            var option = {
                title: {
                    text: '违规车辆占比情况',
                    left: 'center'
                },
                color: ['#ff9f7f', '#ffdb5c', '#37a2da'],
                tooltip: {
                    trigger: 'item',
                    formatter: '{b} : {c} ({d}%)'
                },
                legend: {
                    orient: 'horizontal',
                    bottom: 'bottom',
                    data: ['累计疲劳', '夜间疲劳', '日间疲劳']
                },
                series: [
                    {
                        name: '违规车辆占比',
                        type: 'pie',
                        radius: '55%',
                        data: data,
                        emphasis: {
                            itemStyle: {
                                shadowBlur: 10,
                                shadowOffsetX: 0,
                                shadowColor: 'rgba(0, 0, 0, 0.5)'
                            }
                        }
                    }
                ]
            };
            myChart.clear();
            myChart.setOption(option);
            window.onresize = function () {
                myChart.resize();
            };
        },
        /**
         * 违规趋势走向图表
         * @param echartData 轴数据
         * @param element 渲染元素
         * */
        trendEchart: function (echartData, element) {
            var myChart = echarts.init(document.getElementById(element));
            var option = {
                title: {
                    text: '违规趋势',
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
                            if (i < 5) {
                                result += '<p><i style="background-color: ' + item.color + '"></i>' + item.seriesName + '：' + item.data + '</p>';
                            } else {
                                var showData = 0;
                                if (item.data < 0) {
                                    showData = '下降 ' + Math.abs(item.data);
                                } else if (item.data > 0) {
                                    showData = '上升 ' + item.data;
                                }
                                result += '<span><i style="background-color: ' + item.color + '"></i>' + item.seriesName + '：' + showData + '%</span>';
                            }
                        }
                        result += '</div>';
                        return result;
                    }
                },
                color: ['#c74540', '#5b9fb0', '#ff9f7f', '#37a2da', '#ffdb5c', '#fe18fe', '#ff0000'],
                grid: {
                    top: 60,
                    right: 50,
                },
                legend: {
                    orient: 'horizontal',
                    top: 0,
                    right: 10,
                    type: 'scroll',
                    data: ['累计疲劳', '夜间疲劳', '日间疲劳', '环比率', '同比率']
                },
                dataZoom: [{// 初始化滚动条
                    type: 'slider',
                    show: true,
                    xAxisIndex: [0],
                    left: '9%',
                    right: 80,
                    bottom: -5,
                    start: 0,
                    end: element === 'trendDetailEchart' ? 50 : 70,
                    maxValueSpan: element === 'trendDetailEchart' ? 16 : 20,
                }],
                xAxis: [
                    {
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
                    }
                ],
                yAxis: [
                    {
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
                series: [
                    {
                        name: '疲劳报警车辆数',
                        type: 'bar',
                        barWidth: 1,
                        itemStyle: {// 隐藏该柱状图显示
                            opacity: 0,
                            borderWidth: 0,
                        },
                        data: echartData.monitorNum,
                    },
                    {
                        name: '疲劳报警次数',
                        type: 'bar',
                        barWidth: 1,
                        itemStyle: {// 隐藏该柱状图显示
                            opacity: 0,
                            borderWidth: 0,
                        },
                        data: echartData.totalNum,
                    }, {
                        name: '累计疲劳',
                        type: 'bar',
                        barWidth: 10,
                        barGap: '30%',
                        data: echartData.totalAccumulatedNum,
                    }, {
                        name: '夜间疲劳',
                        type: 'bar',
                        barWidth: 10,
                        barGap: '30%',
                        data: echartData.totalNightNum,
                    }, {
                        name: '日间疲劳',
                        type: 'bar',
                        barWidth: 10,
                        barGap: '30%',
                        data: echartData.totalDayNum,
                    },
                    {
                        name: '环比率',
                        type: 'line',
                        yAxisIndex: 1,
                        symbol: 'none',// 去掉点
                        smooth: true,// 曲线平滑
                        data: echartData.sequentialData
                    },
                    {
                        name: '同比率',
                        type: 'line',
                        yAxisIndex: 1,
                        symbol: 'none',// 去掉点
                        smooth: true,// 曲线平滑
                        data: echartData.comparedData
                    },
                ]
            };
            myChart.clear();
            myChart.setOption(option);
            window.onresize = function () {
                myChart.resize();
            };
        },
        // 列表行点击查看企业疲劳驾驶统计明细信息
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
                var orgId = _this.attr('data-orgid');
                curSelectOrgId = orgId;
                var time = $('#month').val().replace('-', '');
                curSelectTime = time;
                fatigueDrivingOrg.getOrgDetailInfo();
                fatigueDrivingOrg.initDetailTable();
            }
            _this.toggleClass('active');
            _this.siblings('tr').removeClass('active');
        },
        /**
         * 获取企业疲劳驾驶统计明细数据
         * */
        getOrgDetailInfo: function () {
            var ajaxDataParam = {
                "organizationId": curSelectOrgId,
                "month": curSelectTime
            };
            var infoUrl = '/clbs/cb/cbReportManagement/fatigueDriving/getOrgRankData';
            json_ajax("post", infoUrl, "json", true, ajaxDataParam, function (result) {
                if (result.success) {
                    var data = result.obj;
                    // 详情抽屉字段赋值
                    if (data.organizationName) {
                        $('#orgName').html(data.organizationName);
                    }
                    var noStr = '-';
                    $('#totalNum').html(data.totalNum === undefined ? noStr : data.totalNum);
                    $('#monitorNum').html(data.monitorNum === undefined ? noStr : data.monitorNum);
                    $('#accumulatedNum').html(data.accumulatedNum === undefined ? noStr : data.accumulatedNum);
                    $('#dayNum').html(data.dayNum === undefined ? noStr : data.dayNum);
                    $('#nightNum').html(data.nightNum === undefined ? noStr : data.nightNum);
                    $('#ranking').html(data.rank === undefined ? noStr : data.rank);
                } else if (result.msg) {
                    layer.msg(result.msg);
                }
            });
            // 获取详情图表数据
            ajaxDataParam.isSingle = 0;
            var url = '/clbs/cb/cbReportManagement/fatigueDriving/getOrgGraphicsData';
            json_ajax("post", url, "json", true, ajaxDataParam, function (result) {
                if (result.success) {
                    fatigueDrivingOrg.chartRender(result, 'accountedDetailEchart', 'trendDetailEchart');
                } else {
                    var result = {
                        obj: {
                            statisticInfo: []
                        }
                    };
                    fatigueDrivingOrg.chartRender(result, 'accountedDetailEchart', 'trendDetailEchart');
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
            var columns = [
                {
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
                    "data": "accumulatedNum",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data ? data : 0;
                    }
                },
                {
                    "data": "dayNum",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data ? data : 0;
                    }
                }, {
                    "data": "nightNum",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data ? data : 0;
                    }
                },
                {
                    "data": "totalNum",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data ? data : 0;
                    }
                },
            ];

            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.fuzzyQueryParam = $('#detailSimpleQueryParam').val();
                detailFuzzyQueryParam = $('#detailSimpleQueryParam').val();
                d.organizationId = curSelectOrgId;
                d.month = curSelectTime;
            };
            //表格setting
            var setting = {
                listUrl: "/clbs/cb/cbReportManagement/fatigueDriving/getOrgDetailData",
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'detailTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                drawCallbackFun: function () {
                    var api = orgDetailTable.dataTable;
                    var startIndex = api.context[0]._iDisplayStart;//获取到本页开始的条数
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

            $('#detailExport').prop('disabled', false);
            $('#searchDetailTable').on('click', function (e) {
                orgDetailTable.refresh();
            });
        },
        appendZero: function (val) {
            if (val < 10) {
                return '0' + val.toString();
            }
            return val.toString();
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
            var newDate = dateYear + '-' + fatigueDrivingOrg.appendZero(dateMonth);
            $('#detailMonth').html(newDate);
            curSelectTime = newDate.replace('-', '');
            fatigueDrivingOrg.getOrgDetailInfo();
            fatigueDrivingOrg.initDetailTable();
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
            var newDate = dateYear + '-' + fatigueDrivingOrg.appendZero(dateMonth);
            $('#detailMonth').html(newDate);
            curSelectTime = newDate.replace('-', '');
            fatigueDrivingOrg.getOrgDetailInfo();
            fatigueDrivingOrg.initDetailTable();
        },
        export: function () {
            if ($("#dataTable tbody tr td").hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据,无法导出");
                return;
            }
            if(getRecordsNum() > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var paramer = {
                month: searchMonth,
                organizationId: groupId.replace(/,/g, ''),
                fuzzyQueryParam: fuzzyQueryParam,
                module: '企业疲劳驾驶报表'
            };
            var url = "/clbs/cb/cbReportManagement/fatigueDriving/exportOrgListData";
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
        detailExport: function () {
            if ($("#detailTable tbody tr td").hasClass("dataTables_empty")
                || $("#detailTable tbody").find('td').length === 0) {
                layer.msg("列表无任何数据,无法导出");
                return;
            }
            if(getRecordsNum("detailTable_info") > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var paramer = {
                month: curSelectTime,
                organizationId: curSelectOrgId,
                fuzzyQueryParam: detailFuzzyQueryParam,
                module: '企业疲劳驾驶报表'
            };
            var url = "/clbs/cb/cbReportManagement/fatigueDriving/exportOrgDetailData";
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
        getMonth: function (month) {
            if (month >= 1 && month <= 9) {
                return "0" + month
            }
        },
        getNow: function (s) {
            return s < 10 ? "0" + s : s;
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
                elem: '#month'
                , type: 'month'
                , max: maxMonth
                , btns: ['clear', 'confirm']
                , ready: function (date) {
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
                    // 获取当前实际的年月
                    var getNow = fatigueDrivingOrg.getNow;
                    var myDate = new Date();
                    var nowYear = myDate.getFullYear();
                    var nowMonth = myDate.getMonth() + 1;

                    if (year > nowYear) {
                        final = fatigueDrivingOrg.getYesterMonth(1);
                    } else if (year == nowYear) {
                        if (month > nowMonth) {
                            final = fatigueDrivingOrg.getYesterMonth(1)
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
            var getNow = fatigueDrivingOrg.getNow;
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
        closeDetail: function () {
            $('#dataTable tbody tr').removeClass('active');
            $('#detail').removeClass('active');
            $('#monitorDataTable tbody tr').removeClass('active');
            $('#monitorDetail').removeClass('active');
        },
    };
    $(function () {
        /**
         * 疲劳驾驶企业统计报表
         */
        fatigueDrivingOrg.init();
        fatigueDrivingOrg.getTable('#dataTable');
        $("#groupSelect").bind("click", showMenuContent);
        $("#refreshTable").bind("click", fatigueDrivingOrg.refreshTable);
        $('#exportAlarm').on('click', fatigueDrivingOrg.export);
        $('#detailExport').on('click', fatigueDrivingOrg.detailExport);
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                search_ztree('treeDemo', 'groupSelect', 'group');
                treeObj.checkAllNodes(false);
            }
        });
        $('#detailSimpleQueryParam').bind('keyup', function (event) {
            if (event.keyCode == "13") {
                //回车执行查询
                $('#searchDetailTable').click();
            }
        });
        // 监控对象树模糊查询
        $("#groupSelect").on('input propertychange', function (value) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.checkAllNodes(false);
            search_ztree('treeDemo', 'groupSelect', 'group');
        });
        // 点击页面其他地方,关闭详情抽屉
        $(document).on('click', fatigueDrivingOrg.closeDetail);
        $('#detail,#monitorDetail').on('click', function (event) {
            event.stopPropagation();
        });
    })
}(window, $))