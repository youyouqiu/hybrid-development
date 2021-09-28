(function (window, $) {
    //车辆id列表
    var vehicleId = [];
    //开始时间
    var startTime;
    var myTable;
    //结束时间
    var endTime;
    var checkFlag = false; //判断组织节点是否是勾选操作
    var size;//当前权限监控对象数量
    var zTreeIdJson = {};
    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询
    var ifAllCheck = true; //刚进入页面小于TREE_MAX_CHILDREN_LENGTH自动勾选
    var searchFlag = true;
    var simpleQueryParam = '';
    var map;

    deliveryLine = {
        init: function () {
            //车辆树
            var setting = {
                async: {
                    url: deliveryLine.getAlarmReportTreeUrl,//"/clbs/m/functionconfig/fence/bindfence/vehicelTree"
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: deliveryLine.ajaxDataFilter
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
                    beforeClick: deliveryLine.beforeClickVehicle,
                    onAsyncSuccess: deliveryLine.zTreeOnAsyncSuccess,
                    beforeCheck: deliveryLine.zTreeBeforeCheck,
                    onCheck: deliveryLine.onCheckVehicle,
                    onNodeCreated: deliveryLine.zTreeOnNodeCreated,
                    onExpand: deliveryLine.zTreeOnExpand
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);

            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            for (var i = 0; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-text").html(menu_text);
        },
        // 绘制路线
        drawLine: function (lineId) {
            var url = '/clbs/adas/deliveryLine/line';
            json_ajax("POST", url, "json", true, {lineUuid: lineId}, function (result) {
                if (result.success) {
                    $('#lineShowModel').modal('show');
                    if (!map) {
                        // 创建地图
                        map = new AMap.Map("lineContainer", {
                            zoom: 18,// 地图显示的缩放级别
                        });
                    }

                    // 折线的节点坐标数组
                    var path = [];
                    for (var i = 0; i < result.obj.length; i++) {
                        var item = result.obj[i];
                        path.push([item.longitude, item.latitude]);
                    }

                    var polyline = new AMap.Polyline({
                        path: path,
                        strokeOpacity: 1,
                        strokeColor: "#3366FF", //线颜色
                        strokeWeight: 5, //线宽
                        strokeStyle: "solid", //线样式
                        strokeDasharray: [10, 5],
                        zIndex: 51
                    });
                    map.clearMap();
                    polyline.setMap(map);
                    // 缩放地图到合适的视野级别
                    setTimeout(function () {
                        map.setFitView([polyline]);
                    }, 300)
                } else if (result.msg) {
                    layer.msg(result.msg);
                }
            });
        },
        //模糊查询树
        searchVehicleTree: function (param) {
            ifAllCheck = false;//模糊查询不自动勾选
            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                deliveryLine.init();
            } else {
                bflag = true;
                var querySetting = {
                    async: {
                        url: "/clbs/a/search/reportFuzzySearch",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"type": $('#queryType').val(), "queryParam": param, "queryType": "vehicle"},
                        dataFilter: deliveryLine.ajaxQueryDataFilter
                    },
                    check: {
                        enable: true,
                        chkStyle: "checkbox",
                        radioType: "all",
                        chkboxType: {
                            "Y": "s",
                            "N": "s"
                        }
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
                        beforeClick: deliveryLine.beforeClickVehicle,
                        beforeCheck: deliveryLine.zTreeBeforeCheck,
                        onCheck: deliveryLine.onCheckVehicle,
                        onExpand: deliveryLine.zTreeOnExpand,
                        onNodeCreated: deliveryLine.zTreeOnNodeCreated
                    }
                };
                $.fn.zTree.init($("#treeDemo"), querySetting, null);
            }
        },
        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData));
            var nodesArr;
            if ($('#queryType').val() == "vehicle") {
                nodesArr = filterQueryResult(responseData, crrentSubV);
            } else {
                nodesArr = responseData;
            }
            for (var i = 0; i < nodesArr.length; i++) {
                nodesArr[i].open = true;
            }
            return nodesArr;
        },
        getAlarmReportTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle";
            }
        },
        //开始时间
        startDay: function (day) {
            var timeInterval = $('#timeInterval').val().split('--');
            var startValue = timeInterval[0];
            var endValue = timeInterval[1];
            if (startValue == "" || endValue == "") {
                var today = new Date();
                var targetday_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * day;
                today.setTime(targetday_milliseconds); //注意，这行是关键代码
                var tYear = today.getFullYear();
                var tMonth = today.getMonth();
                var tDate = today.getDate();
                tMonth = fillZero(tMonth + 1);
                tDate = fillZero(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " "
                    + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24
                    * parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = fillZero(endMonth + 1);
                endDate = fillZero(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate + " "
                    + "23:59:59";
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = fillZero(vMonth + 1);
                vDate = fillZero(vDate);
                startTime = vYear + "-" + vMonth + "-" + vDate + " "
                    + "00:00:00";
                if (day == 1) {
                    endTime = vYear + "-" + vMonth + "-" + vDate + " "
                        + "23:59:59";
                } else {
                    var endNum = -1;
                    var vendtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * parseInt(endNum);
                    var dateEnd = new Date();
                    dateEnd.setTime(vendtoday_milliseconds);
                    var vendYear = dateEnd.getFullYear();
                    var vendMonth = dateEnd.getMonth();
                    var vendDate = dateEnd.getDate();
                    vendMonth = fillZero(vendMonth + 1);
                    vendDate = fillZero(vendDate);
                    endTime = vendYear + "-" + vendMonth + "-" + vendDate + " "
                        + "23:59:59";
                }
            }
        },
        //当前时间
        getsTheCurrentTime: function () {
            var nowDate = new Date();
            startTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate()) + " " + "00:00:00";
            endTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate()) + " "
                + ("23")
                + ":"
                + ("59")
                + ":"
                + ("59");
            var atime = $("#atime").val();
            if (atime != undefined && atime != "") {
                startTime = atime;
            }
        },
        //组织树预处理加载函数
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (responseData.msg) {
                var obj = JSON.parse(ungzip(responseData.msg));
                var data;
                if (obj.tree != null && obj.tree != undefined) {
                    data = obj.tree;
                    size = obj.size;
                } else {
                    data = obj
                }
                for (var i = 0; i < data.length; i++) {
                    data[i].open = true;
                }
            }
            return data;
        },
        //判断日期是否合法,是否选中车辆
        validates: function () {
            return $("#hourslist").validate({
                rules: {
                    timeInterval: {
                        required: true,
                        compareDate: "#timeInterval"
                    },
                    groupSelect: {
                        zTreeChecked: "treeDemo"
                    }
                },
                messages: {
                    timeInterval: {
                        required: "请选择时间",
                        compareDate: "请选择时间"
                    },
                    groupSelect: {
                        zTreeChecked: vehicleSelectBrand,
                    }
                }
            }).form();
        },
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (size <= TREE_MAX_CHILDREN_LENGTH && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            deliveryLine.getCharSelect(treeObj);
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;
                    if ($('#groupSelect').val() === '' || !treeNode.children) {
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
                    } else {
                        var nodes = zTree.getNodesByFilter(function (node) {
                            return node.type == "people" || node.type == "vehicle";
                        }, false, treeNode); // 查找节点集合
                        nodesLength = nodes.length;
                    }
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
                if (nodesLength > TREE_MAX_CHILDREN_LENGTH) {
                    // layer.msg(maxSelectItem);
                    layer.msg('最多勾选' + TREE_MAX_CHILDREN_LENGTH + '个监控对象');
                    flag = false;
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
                searchFlag = false;
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    deliveryLine.getCheckedNodes();
                    deliveryLine.validates();
                }, 600);
            }
            crrentSubV = [];
            crrentSubV.push(treeNode.id);
            deliveryLine.getCharSelect(zTree);
            deliveryLine.getCheckedNodes();
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
            if (!ifAllCheck || (treeNode.type == "group" && !checkFlag)) {
                return;
            }
            //初始化勾选操作判断表示
            checkFlag = false;
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (treeNode.type == "group") {
                var url = "/clbs/m/functionconfig/fence/bindfence/putMonitorByGroup";
                json_ajax("post", url, "json", false, {
                    "groupId": treeNode.id,
                    "isChecked": treeNode.checked,
                    "monitorType": "vehicle"
                }, function (data) {
                    var result = data.obj;
                    if (result != null && result != undefined) {
                        $.each(result, function (i) {
                            var pid = i; //获取键值
                            var chNodes = result[i];//获取对应的value
                            if (zTreeIdJson[pid] != undefined) {
                                var parentTid = zTreeIdJson[pid][0];
                                var parentNode = treeObj.getNodeByTId(parentTid);
                                if (parentNode.children === undefined) {
                                    treeObj.addNodes(parentNode, []);
                                }
                            }
                        });
                    }
                })
            }
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
        getGroupChild: function (node, assign) { // 递归获取组织及下级组织的分组节点
            var nodes = node.children;
            if (nodes != null && nodes != undefined && nodes.length > 0) {
                for (var i = 0; i < nodes.length; i++) {
                    var node = nodes[i];
                    if (node.type == "assignment") {
                        assign.push(node);
                    } else if (node.type == "group" && node.children != undefined) {
                        deliveryLine.getGroupChild(node.children, assign);
                    }
                }
            }
        },
        getCheckedNodes: function () {
            vehicleId = [];
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                .getCheckedNodes(true);
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == "vehicle") {
                    vehicleId.push(nodes[i].id);
                }
            }
        },
        inquireClick: function (number) {
            $(".ToolPanel").css("display", "block");
            if (number === 1) {
                var timeInterval = $('#timeInterval').val().split('--');
                startTime = timeInterval[0];
                endTime = timeInterval[1];
            } else {
                if (number == 0) {
                    deliveryLine.getsTheCurrentTime();
                    $('#timeInterval').val(startTime + '--' + endTime);
                } else if (number == -1) {
                    deliveryLine.startDay(-1);
                    $('#timeInterval').val(startTime + '--' + endTime);
                } else if (number == -3) {
                    deliveryLine.startDay(-3);
                    $('#timeInterval').val(startTime + '--' + endTime);
                } else if (number == -7) {
                    deliveryLine.startDay(-7);
                    $('#timeInterval').val(startTime + '--' + endTime);
                }
            }
            deliveryLine.getCheckedNodes();
            if (!deliveryLine.validates()) {
                return;
            }
            deliveryLine.tableInit();

            //是否可以导出
            $("#exportAlarm").prop("disabled", false);
        },
        //数据列表
        columnRenderFun: function (key) {
            switch (key) {
                case 'lineId':// 路线ID,点击查看
                    return function (data, type, row, meta) {
                        if (data) return '<a onclick="deliveryLine.drawLine(\'' + row.lineUuid + '\')">' + data + '</a>';
                        return '-';
                    };
                default:
                    return null;
            }
        },
        tableInit: function () {
            //显示隐藏列
            var columnDefs = [{
                "searchable": true,
                "orderable": false,
                "targets": [0, 1, 2, 3]
            }];
            var columnKeys = [
                'orgName', 'brand', 'vehicleColorStr', 'lineId', 'receiveTimeStr',
                'dirStatusStr', 'sendTime',
            ];
            var columns = [{
                "data": null,
                "class": "text-center",
            }];
            for (var i = 0; i < columnKeys.length; i++) {
                columns.push({
                    "data": columnKeys[i],
                    "class": "text-center",
                    render: deliveryLine.columnRenderFun(columnKeys[i]) || function (data) {
                        if (data) return data;
                        return '-';
                    }
                })
            }
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.monitorIds = vehicleId.join(',');
                var timeInterval = $('#timeInterval').val().split('--');
                startTime = timeInterval[0];
                endTime = timeInterval[1];
                d.startTime = startTime;
                d.endTime = endTime;
                simpleQueryParam = $('#simpleQueryParam').val();
                d.simpleQueryParam = simpleQueryParam;
            };
            //表格setting
            var setting = {
                listUrl: "/clbs/adas/deliveryLine/list",
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                lengthMenu: [5, 10, 20, 50, 100],
                pageable: true, //是否分页
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
            $('.toggle-vis').prop('checked', true);
            $('.toggle-vis').off().on('change', function (e) {
                var visible = myTable.dataTable.column($(this).attr('data-column')).visible();
                if (visible) {
                    myTable.dataTable.column($(this).attr('data-column')).visible(false);
                } else {
                    myTable.dataTable.column($(this).attr('data-column')).visible(true);
                }
                $(".keep-open").addClass("open");
            });
            $("#search_button").off().on("click", function () {
                myTable.requestData();
            });
        },
        exportAlarm: function (e) {
            if ($("#dataTable tbody tr td").hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据，无法导出");
                return;
            }
            if (getRecordsNum() > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var url = "/clbs/adas/deliveryLine/export";
            var parameter = {
                "monitorIds": vehicleId.join(','),
                "startTime": startTime,
                "endTime": endTime,
                "simpleQueryParam": simpleQueryParam,
            };
            exportExcelUseForm(url, parameter);
        },
        getTable: function () {
            $('.toggle-vis').prop('checked', true);
            myTable = $('#dataTable').DataTable({
                "columnDefs": [{
                    "targets": [0, 2, 3, 4, 5, 6],
                    "searchable": false
                }],
                "destroy": true,
                /*"dom": 'tiprl',// 自定义显示项*/
                "lengthChange": true,// 是否允许用户自定义显示数量
                "bPaginate": true, // 翻页功能
                "bFilter": false, // 列筛序功能
                "searching": true,// 本地搜索
                "ordering": false, // 排序功能
                "Info": true,// 页脚信息
                "autoWidth": true,// 自动宽度
                "stripeClasses": [],
                "pageLength": 10,
                "lengthMenu": [5, 10, 20, 50, 100, 200],
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
                    [0, null]
                ],// 第一列排序图标改为默认

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
        },
        //刷新列表
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            simpleQueryParam = '';
            searchFlag = true;
            deliveryLine.inquireClick(1, false);
        },
        // 查询时间默认显示昨天到今天
        getShowTime: function () {
            var day1 = new Date();
            day1.setTime(day1.getTime() - 24 * 60 * 60 * 1000);
            var s1 = day1.getFullYear() + "-" + fillZero(day1.getMonth() + 1) + "-" + fillZero(day1.getDate()) + ' 00:00:00';
            var day2 = new Date();
            day2.setTime(day2.getTime());
            var s2 = day2.getFullYear() + "-" + fillZero(day2.getMonth() + 1) + "-" + fillZero(day2.getDate()) + ' 23:59:59';
            $("#timeInterval").val(s1 + '--' + s2);
        }
    };
    $(function () {
        //初始化页面
        deliveryLine.init();
        $('input').inputClear();
        $('#timeInterval').dateRangePicker({
            dateLimit: 60,
        });
        deliveryLine.getShowTime();
        deliveryLine.getTable();
        //组织下拉显示
        $("#groupSelect").bind("click", showMenuContent);
        //导出
        $("#exportAlarm").bind("click", deliveryLine.exportAlarm);
        $("#refreshTable").bind("click", deliveryLine.refreshTable);

        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var param = $("#groupSelect").val();
                deliveryLine.searchVehicleTree(param);
            }
        });

        /**
         * 监控对象树模糊查询
         */
        var inputChange;
        $("#groupSelect").on('input propertychange', function (value) {
            if (inputChange !== undefined) {
                clearTimeout(inputChange);
            }
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                searchFlag = true;
            }
            inputChange = setTimeout(function () {
                if (searchFlag) {
                    var param = $("#groupSelect").val();
                    deliveryLine.searchVehicleTree(param);
                }
                searchFlag = true;
            }, 500);
        });
        $('#queryType').on('change', function () {
            var param = $("#groupSelect").val();
            deliveryLine.searchVehicleTree(param);
        });
    })
}(window, $))