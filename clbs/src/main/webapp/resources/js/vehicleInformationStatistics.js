(function (window, $) {
    /**
     * 车辆信息统计
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
    var detailDay = '';
    var curSelectOrgId = '';// 当前选择的企业id
    var curSelectTime = '';// 详情抽屉当前选择的时间
    vehicleInformationStatistics = {
        // 组织树相关方法
        init: function () {
            vehicleInformationStatistics.monthDataInit();

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
                    url: vehicleInformationStatistics.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: vehicleInformationStatistics.ajaxDataFilter
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
                    beforeClick: vehicleInformationStatistics.beforeClickVehicle,
                    onAsyncSuccess: vehicleInformationStatistics.zTreeOnAsyncSuccess,
                    beforeCheck: vehicleInformationStatistics.zTreeBeforeCheck,
                    onCheck: vehicleInformationStatistics.onCheckVehicle,
                    onNodeCreated: vehicleInformationStatistics.zTreeOnNodeCreated,
                    onExpand: vehicleInformationStatistics.zTreeOnExpand
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
            vehicleInformationStatistics.getCharSelect(treeObj);
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
                    vehicleInformationStatistics.getCheckedNodes("treeDemo");
                    vehicleInformationStatistics.validates();
                }, 600);
            }
            vehicleInformationStatistics.getCheckedNodes("treeDemo");
            vehicleInformationStatistics.getCharSelect(zTree);
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
                    "data": "orgName",
                    "class": "text-center",
                },
                {
                    "data": "dailyAvgAccessRate",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data !== null) return data + '%';
                        return '';
                    }
                },
                {
                    "data": "dailyAvgUplineRate",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data !== null) return data + '%';
                        return '';
                    }
                }, {
                    "data": "dailyAvgTrackFullRate",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data !== null) return data + '%';
                        return '';
                    }
                },
                {
                    "data": "dailyAvgPositionalPassRate",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data !== null) return data + '%';
                        return '';
                    }
                }, {
                    "data": "dailyAvgDriftRate",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data !== null) return data + '%';
                        return '';
                    }
                },
                {
                    "data": "dailyAvgOverSpeedNum",
                    "class": "text-center"
                }, {
                    "data": "dailyAvgFatigueDuration",
                    "class": "text-center"
                },
            ];

            //ajax参数
            var ajaxDataParamFun = function (d) {
                fuzzyQueryParam = $('#simpleQueryParam').val();
                d.simpleQueryParam = fuzzyQueryParam;
                d.organizationIds = groupId.join(',');
                var time = $('#month').val().replace('-', '');
                d.month = time;
                searchMonth = time;
            };
            //表格setting
            var setting = {
                listUrl: "/clbs/m/reportManagement/vehicleInformationStatistics/getOrgList",
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                drawCallbackFun: function () {
                    var api = myTable.dataTable;
                    var startIndex = api.context[0]._iDisplayStart;//获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                        var organizationId = api.data()[i].orgId;
                        $(cell).closest('tr').attr('data-orgId', organizationId);
                    });
                },
                ajaxCallBack: vehicleInformationStatistics.drawTableCallbackFun,
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
                    vehicleInformationStatistics.trClickFun(e);
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
                vehicleInformationStatistics.setNewDay(number);
            }
            vehicleInformationStatistics.getCheckedNodes('treeDemo');
            if (!vehicleInformationStatistics.validates()) {
                return;
            }

            vehicleInformationStatistics.initTable();
            var ajaxDataParam = {
                "organizationIds": groupId.join(','),
                "month": $('#month').val().replace('-', ''),
            };
            var url = '/clbs/m/reportManagement/vehicleInformationStatistics/getOrgGraph';
            json_ajax("post", url, "json", true, ajaxDataParam, function (result) {
                if (result.success) {
                    $('#stretch3-body').slideDown();
                    vehicleInformationStatistics.chartRender(result, 'trendEchart');
                } else {
                    if (result.msg) layer.msg(result.msg);
                    var result = {
                        obj: []
                    };
                    vehicleInformationStatistics.chartRender(result, 'trendEchart');
                    $('#stretch3-body').slideUp();
                }
            });
        },
        /**
         * 图表渲染
         * @param result 数据
         * @param trendElement 柱状图渲染元素
         * */
        chartRender: function (result, trendElement) {
            // 组装图表数据
            var statisticInfo = result.obj;
            var echartData = {
                'xData': [],
                'avgAccessRatio': [],
                'avgUplineRatio': [],
                'avgTrackFullRatio': [],
                'avgPassRatio': [],
                'avgGpsDriftRation': [],
                'avgOverSpeedNum': [],
                'avgFatigueDuration': [],
            };
            detailDay = null;
            for (var i = 0; i < statisticInfo.length; i++) {
                var item = statisticInfo[i];
                echartData.xData.push(item.day);
                if (trendElement === 'trendDetailEchart') {
                    if (!detailDay && (item.accessRate || item.uplineRate || item.trackFullRate ||
                        item.positionalPassRate || item.driftRate ||
                        item.overSpeedNum || item.fatigueDuration)) {
                        detailDay = item.day;
                    }
                }
                echartData.avgAccessRatio.push((item.avgAccessRate || item.accessRate) || 0);
                echartData.avgUplineRatio.push((item.avgUplineRate || item.uplineRate) || 0);
                echartData.avgTrackFullRatio.push((item.avgTrackFullRate || item.trackFullRate) || 0);
                echartData.avgPassRatio.push((item.avgPositionalPassRate || item.positionalPassRate) || 0);
                echartData.avgGpsDriftRation.push((item.avgDriftRate || item.driftRate) || 0);
                echartData.avgOverSpeedNum.push((item.avgOverSpeedNum || item.overSpeedNum) || 0);
                echartData.avgFatigueDuration.push((item.avgFatigueDuration || item.fatigueDuration) || 0);
            }
            vehicleInformationStatistics.trendEchart(echartData, trendElement);
        },
        // 详情图表点击事件
        detailChartClick: function (e) {
            e.stopPropagation();
            // var xtrue = 665 < e.clientX && e.clientX < 1299;
            var ytrue = 80 < e.clientY && e.clientY < 274;
            // 只有点击图表范围内才生效
            if (ytrue) {
                orgDetailTable.requestData();
            }
        },
        /**
         * 图表
         * @param echartData 轴数据
         * @param element 渲染元素
         * */
        trendEchart: function (echartData, element) {
            var myChart = echarts.init(document.getElementById(element));
            var infoArr = ['日均车辆入网率', '日均车辆上线率', '日均轨迹完整率', '日均数据合格率', '日均卫星定位漂移率', '日均车辆超速次数', '日均疲劳驾驶时长'];
            if (element === 'trendDetailEchart') {
                infoArr = ['车辆入网率', '车辆上线率', '轨迹完整率', '数据合格率', '卫星定位漂移率', '平均车辆超速次数', '平均疲劳驾驶时长'];
            }
            var legendData = [
                {
                    bottom: 30,
                    data: infoArr.slice(0, 5)
                },
                {
                    bottom: 0,
                    data: infoArr.slice(5)
                }
            ];
            var seriesData = [
                {
                    name: infoArr[0],
                    type: 'line',
                    symbol: echartData.avgAccessRatio.length !== 1 ? 'none' : 'circle',// 去掉点
                    // smooth: true,// 曲线平滑
                    data: echartData.avgAccessRatio,
                }, {
                    name: infoArr[1],
                    type: 'line',
                    symbol: echartData.avgUplineRatio.length !== 1 ? 'none' : 'circle',// 去掉点
                    smooth: true,// 曲线平滑
                    data: echartData.avgUplineRatio,
                },
                {
                    name: infoArr[2],
                    type: 'line',
                    symbol: echartData.avgTrackFullRatio.length !== 1 ? 'none' : 'circle',// 去掉点
                    smooth: true,// 曲线平滑
                    data: echartData.avgTrackFullRatio
                },
                {
                    name: infoArr[3],
                    type: 'line',
                    symbol: echartData.avgPassRatio.length !== 1 ? 'none' : 'circle',// 去掉点
                    smooth: true,// 曲线平滑
                    data: echartData.avgPassRatio
                },
                {
                    name: infoArr[4],
                    type: 'line',
                    symbol: echartData.avgGpsDriftRation.length !== 1 ? 'none' : 'circle',// 去掉点
                    smooth: true,// 曲线平滑
                    data: echartData.avgGpsDriftRation
                },
                {
                    name: infoArr[5],
                    type: 'bar',
                    yAxisIndex: 1,
                    barWidth: 12,
                    data: echartData.avgOverSpeedNum,
                }, {
                    name: infoArr[6],
                    type: 'bar',
                    yAxisIndex: 1,
                    barWidth: 12,
                    data: echartData.avgFatigueDuration,
                }
            ];
            var option = {
                tooltip: {
                    trigger: 'axis',
                    formatter: function (series) {
                        var time = series[0].axisValue;
                        detailDay = time;
                        var timeStr = time.substring(0, 4) + '-' + time.substring(4, 6) + '-' + time.substring(6, 8);
                        var result = '<div class="formatterBox"><h4>' + timeStr + '</h4>';
                        for (var i = 0; i < series.length; i++) {
                            var item = series[i];
                            var seriesName = item.seriesName;
                            var unit = '';
                            if (seriesName.indexOf('次数') === -1 && seriesName.indexOf('时长') === -1) {
                                unit = '%';
                            }
                            if (i < 5) {
                                result += '<p><i style="background-color: ' + item.color + '"></i>' + seriesName + '：' + item.data + unit + '</p>';
                            } else {
                                result += '<span><i style="background-color: ' + item.color + '"></i>' + seriesName + '：' + item.data + unit + '</span>';
                            }
                        }
                        result += '</div>';
                        return result;
                    },
                },
                color: ['#c74540', '#5b9fb0', '#ff9f7f', '#ffdb5c', '#37a2da', '#fe18fe', '#ff0000'],
                grid: {
                    top: 20,
                    left: 60,
                    right: 60,
                    bottom: 100
                },
                legend: legendData,
                dataZoom: [{// 初始化滚动条
                    type: 'slider',
                    show: true,
                    height: 20,
                    xAxisIndex: [0],
                    bottom: 60,
                    left: 80,
                    right: 80,
                    // left: '9%',
                    // start: 0,
                    // end: element === 'trendDetailEchart' ? 50 : 70,
                    // maxValueSpan: element === 'trendDetailEchart' ? 16 : 20,
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
                        position: 'right',
                        axisLabel: {
                            formatter: '{value} %'
                        },
                    },
                    {
                        type: 'value',
                        name: '',
                        position: 'left',
                        axisLabel: {
                            formatter: '{value}'
                        },
                    },
                ],
                series: seriesData
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
                var orgId = _this.attr('data-orgId');
                curSelectOrgId = orgId;
                var time = $('#month').val().replace('-', '');
                curSelectTime = time;
                vehicleInformationStatistics.getOrgDetailInfo();
                vehicleInformationStatistics.initDetailTable();
                document.body.style.overflow = 'hidden'
                $('#maskLayer').show()
            }
            _this.toggleClass('active');
            _this.siblings('tr').removeClass('active');
        },
        /**
         * 获取组织中车辆明细统计数据
         * */
        getOrgDetailInfo: function () {
            var ajaxDataParam = {
                "organizationId": curSelectOrgId,
                "month": curSelectTime
            };
            // 获取详情图表数据
            var url = '/clbs/m/reportManagement/vehicleInformationStatistics/getOrgDetailGraph';
            ajaxDataParam.isSingle = 0;
            json_ajax("post", url, "json", false, ajaxDataParam, function (result) {
                if (result.success) {
                    vehicleInformationStatistics.chartRender(result, 'trendDetailEchart');
                } else {
                    detailDay = curSelectTime + '01';
                    var result = {
                        obj: {
                            data: {
                                statisticInfo: []
                            }
                        }
                    };
                    vehicleInformationStatistics.chartRender(result, 'trendDetailEchart');
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
                /*{
                    "data": "signColor",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data) {
                            return getPlateColor(data);
                        }
                        return '';
                    },
                },*/ {
                    "data": "day",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data === null) return '-';
                        return data.substring(6);
                    }
                },
                {
                    "data": "accessStatus",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data === null) return '-';
                        return data === 1 ? '是' : '否';
                    }
                },
                /*{
                    "data": "operationStatus",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data === null) return '-';
                        return data === 1 ? '营运' : '停运';
                    }
                },*/ {
                    "data": "uplineStatus",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data === null) return '-';
                        return data === 1 ? '正常' : '不在线';
                    }
                },
                {
                    "data": "trackFullRate",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data === null) return '-';
                        return data + '%';
                    }
                },
                {
                    "data": "positionalPassRate",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data === null) return '-';
                        return data + '%';
                    }
                }, {
                    "data": "driftNum",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data !== null ? data : '-';
                    }
                },
                {
                    "data": "overSpeedNum",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data !== null ? data : '-';
                    }
                },
                {
                    "data": "fatigueDuration",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data !== null ? data : '-';
                    }
                }
            ];

            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.organizationId = curSelectOrgId;
                d.date = detailDay;
            };
            //表格setting
            var setting = {
                listUrl: "/clbs/m/reportManagement/vehicleInformationStatistics/getOrgDetailList",
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
            var newDate = dateYear + '-' + vehicleInformationStatistics.appendZero(dateMonth);
            $('#detailMonth').html(newDate);
            curSelectTime = newDate.replace('-', '');
            vehicleInformationStatistics.getOrgDetailInfo();
            vehicleInformationStatistics.initDetailTable();
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
            var newDate = dateYear + '-' + vehicleInformationStatistics.appendZero(dateMonth);
            $('#detailMonth').html(newDate);
            curSelectTime = newDate.replace('-', '');
            vehicleInformationStatistics.getOrgDetailInfo();
            vehicleInformationStatistics.initDetailTable();
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
                organizationIds: groupId.join(','),
                simpleQueryParam: fuzzyQueryParam,
                module: '车辆信息统计'
            };
            var url = "/clbs/m/reportManagement/vehicleInformationStatistics/exportOrgListData";
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
            // exportExcelUseForm(url, paramer);
        },
        detailExport: function () {
            if ($("#detailTable tbody tr td").hasClass("dataTables_empty")
                || $("#detailTable tbody").find('td').length === 0) {
                layer.msg("列表无任何数据,无法导出");
                return;
            }
            var paramer = {
                date: detailDay,
                organizationId: curSelectOrgId,
                module: '车辆信息统计'
            };
            var url = "/clbs/m/reportManagement/vehicleInformationStatistics/exportOrgDetailData";
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
            // exportExcelUseForm(url, paramer);
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
                    var getNow = vehicleInformationStatistics.getNow;
                    var myDate = new Date();
                    var nowYear = myDate.getFullYear();
                    var nowMonth = myDate.getMonth() + 1;

                    if (year > nowYear) {
                        final = vehicleInformationStatistics.getYesterMonth(1);
                    } else if (year == nowYear) {
                        if (month > nowMonth) {
                            final = vehicleInformationStatistics.getYesterMonth(1)
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
            var getNow = vehicleInformationStatistics.getNow;
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
            document.body.style.overflow = 'auto'
            $('#maskLayer').hide()
        },
    };
    $(function () {
        vehicleInformationStatistics.init();
        vehicleInformationStatistics.getTable('#dataTable');
        $("#groupSelect").bind("click", showMenuContent);
        $("#refreshTable").bind("click", vehicleInformationStatistics.refreshTable);
        $('#exportAlarm').on('click', vehicleInformationStatistics.export);
        $('#detailExport').on('click', vehicleInformationStatistics.detailExport);
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
        $(document).on('click', function (e) {
            if (!e.target.id.includes('layui-layer') || !e.target.id) {
                vehicleInformationStatistics.closeDetail()
            }
        });
        $('#detail,#monitorDetail').on('click', function (event) {
            event.stopPropagation();
        });
        $('#trendDetailEchart').on('click', vehicleInformationStatistics.detailChartClick);
    })
}(window, $))