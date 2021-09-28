(function (window, $) {
    //车辆列表
    var vehicleList = [];
    //车辆id列表
    var vehicleId = [];
    // 选中的车辆id
    var selectVehicleId = '';
    //开始时间
    var startTime;
    var myTable;
    var myDetailTable;
    //结束时间
    var endTime;
    var checkFlag = false; //判断组织节点是否是勾选操作
    var size;//当前权限监控对象数量
    var zTreeIdJson = {};
    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询
    var ifAllCheck = true; //刚进入页面小于TREE_MAX_CHILDREN_LENGTH自动勾选
    var searchFlag = true;
    var queryCondition = '';
    var getCount = true;

    alarmReport = {
        init: function () {
            //车辆树
            var setting = {
                async: {
                    url: alarmReport.getAlarmReportTreeUrl,//"/clbs/m/functionconfig/fence/bindfence/vehicelTree"
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: alarmReport.ajaxDataFilter
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
                    beforeClick: alarmReport.beforeClickVehicle,
                    onAsyncSuccess: alarmReport.zTreeOnAsyncSuccess,
                    beforeCheck: alarmReport.zTreeBeforeCheck,
                    onCheck: alarmReport.onCheckVehicle,
                    onNodeCreated: alarmReport.zTreeOnNodeCreated,
                    onExpand: alarmReport.zTreeOnExpand
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },

        //模糊查询树
        searchVehicleTree: function (param) {
            ifAllCheck = false;//模糊查询不自动勾选
            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                getCount = false;
                alarmReport.init();
            } else {
                bflag = true;
                getCount = true;
                var querySetting = {
                    async: {
                        url: "/clbs/a/search/reportFuzzySearch",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"type": $('#queryType').val(), "queryParam": param, "queryType": "vehicle"},
                        dataFilter: alarmReport.ajaxQueryDataFilter
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
                        beforeClick: alarmReport.beforeClickVehicle,
                        beforeCheck: alarmReport.zTreeBeforeCheck,
                        onCheck: alarmReport.onCheckVehicle,
                        onExpand: alarmReport.zTreeOnExpand,
                        onNodeCreated: alarmReport.zTreeOnNodeCreated
                    }
                };
                $.fn.zTree.init($("#treeDemo"), querySetting, null);
            }
        },
        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData))
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
        tableFilter: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\"" +
                " data-column=\"" + parseInt(1) + "\" />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\"" +
                    " data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-text").html(menu_text);
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
                tMonth = alarmReport.doHandleMonth(tMonth + 1);
                tDate = alarmReport.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate;
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24
                    * parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = alarmReport.doHandleMonth(endMonth + 1);
                endDate = alarmReport.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate;
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = alarmReport.doHandleMonth(vMonth + 1);
                vDate = alarmReport.doHandleMonth(vDate);
                startTime = vYear + "-" + vMonth + "-" + vDate;
                if (day == 1) {
                    endTime = vYear + "-" + vMonth + "-" + vDate;
                } else {
                    var endNum = -1;
                    var vendtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * parseInt(endNum);
                    var dateEnd = new Date();
                    dateEnd.setTime(vendtoday_milliseconds);
                    var vendYear = dateEnd.getFullYear();
                    var vendMonth = dateEnd.getMonth();
                    var vendDate = dateEnd.getDate();
                    vendMonth = alarmReport.doHandleMonth(vendMonth + 1);
                    vendDate = alarmReport.doHandleMonth(vendDate);
                    endTime = vendYear + "-" + vendMonth + "-" + vendDate;
                }
            }
        },
        doHandleMonth: function (month) {
            var m = month;
            if (month.toString().length == 1) {
                m = "0" + month;
            }
            return m;
        },
        //当前时间
        getsTheCurrentTime: function (flag) {
            var nowDate = new Date();
            if (flag) {// 昨天
                nowDate = new Date(nowDate.getTime() - 1000 * 60 * 60 * 24);
            }
            startTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate());
            endTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate());
            var atime = $("#atime").val();
            if (atime != undefined && atime != "") {
                startTime = atime;
            }
        },
        unique: function (arr) {
            var result = [], hash = {};
            for (var i = 0, elem; (elem = arr[i]) != null; i++) {
                if (!hash[elem]) {
                    result.push(elem);
                    hash[elem] = true;
                }
            }
            return result;
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
                    endTime: {
                        required: true,
                        compareDate: "#timeInterval"
                    },
                    startTime: {
                        required: true
                    },
                    groupSelect: {
                        zTreeChecked: "treeDemo"
                    }
                },
                messages: {
                    endTime: {
                        required: "请选择结束日期!",
                        compareDate: endtimeComStarttime
                    },
                    startTime: {
                        required: "请选择开始日期!",
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
            alarmReport.getCharSelect(treeObj);
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;
                    if (getCount || treeNode.type =='assignment') {
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
                    alarmReport.getCheckedNodes();
                    alarmReport.validates();
                }, 600);
            }
            crrentSubV = [];
            crrentSubV.push(treeNode.id);
            alarmReport.getCharSelect(zTree);
            alarmReport.getCheckedNodes();
        },
        zTreeOnNodeCreated: function (event, treeId, treeNode) {
            var id = treeNode.id.toString();
            var list = [];
            if (zTreeIdJson[id] == undefined || zTreeIdJson[id] == null) {
                list = [treeNode.tId];
                zTreeIdJson[id] = list;
            } else {
                zTreeIdJson[id].push(treeNode.tId);
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
                        alarmReport.getGroupChild(node.children, assign);
                    }
                }
            }
        },
        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                .getCheckedNodes(true), v = "", vid = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == "vehicle") {
                    v += nodes[i].name + ",";
                    vid += nodes[i].id + ",";
                }
            }
            vehicleList = v;
            vehicleId = vid;
        },
        inquireClick: function (number) {
            $(".ToolPanel").css("display", "block");
            if (number == 0) {
                alarmReport.getsTheCurrentTime();
                $('#timeInterval').val(startTime + '--' + endTime);
            } else if (number == 'yes') {// 昨天
                alarmReport.getsTheCurrentTime(true);
                $('#timeInterval').val(startTime + '--' + endTime);
            } else if (number == -1) {
                alarmReport.startDay(-1);
                $('#timeInterval').val(startTime + '--' + endTime);
            } else if (number == -3) {
                alarmReport.startDay(-3);
                $('#timeInterval').val(startTime + '--' + endTime);
            } else if (number == -7) {
                alarmReport.startDay(-7);
                $('#timeInterval').val(startTime + '--' + endTime);
            }
            alarmReport.getCheckedNodes();
            if (!alarmReport.validates()) {
                return;
            }
            alarmReport.initTable(vehicleId);
        },
        // 列表渲染
        initTable: function (id) {
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
                    "data": "orgName",
                    "class": "text-center",
                }, {
                    "data": "groupName",
                    "class": "text-center"
                },
                {
                    "data": "openNum",
                    "class": "text-center",
                },
                {
                    "data": "duration",
                    "class": "text-center",
                }, {
                    "data": "mile",
                    "class": "text-center",
                },
            ];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                var timeInterval = $('#timeInterval').val().split('--');
                startTime = timeInterval[0];
                endTime = timeInterval[1];
                var arr = startTime.split('-');
                var arr2 = endTime.split('-');
                var str = arr.join('');
                var str2 = arr2.join('');
                var newStartTime = str;
                var newEndTime = str2;
                d.monitorIds = id;
                d.startDate = newStartTime;
                d.endDate = newEndTime;
            };
            // 表格setting
            var setting = {
                listUrl: "/clbs/m/reportManagement/accStatistics/page",
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                drawCallbackFun: function () {
                    var api = myTable.dataTable;
                    var startIndex = api.context[0]._iDisplayStart; //获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                        var monitorId = api.data()[i].monitorId;
                        $(cell).closest('tr').attr('data-orgId', monitorId);
                    });
                },
                // pagingType: 'simple',
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true
            };
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
            $('#searchDetailTable').on('click', function (e) {
                myTable.refresh();
            });
            $('#dataTable tbody').bind('click', 'tr', function (e) {
                if ($(this).find('.dataTables_empty').length === 0) {
                    alarmReport.openDrawer(e);
                }
            });
        },
        // 详情列表渲染
        initDetailTable: function (id) {
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
                    "data": "openTime",
                    "class": "text-center",
                },
                {
                    "data": "closeTime",
                    "class": "text-center",
                }, {
                    "data": "duration",
                    "class": "text-center",
                },
                {
                    "data": "mile",
                    "class": "text-center",
                },
                {
                    "data": "openLocation",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data) {
                            return data;
                        }
                        return '<a onclick="alarmReport.getDrivingAddress(this,' + row.openLatitude + ',' + row.openLongitude + ')">点击获取位置信息</a>';
                    }
                }, {
                    "data": "closeLocation",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data) {
                            return data;
                        }
                        return '<a onclick="alarmReport.getDrivingAddress(this,' + row.closeLatitude + ',' + row.closeLongitude + ')">点击获取位置信息</a>';
                    }
                },
            ];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                var arr = startTime.split('-');
                var arr2 = endTime.split('-');
                var str = arr.join('');
                var str2 = arr2.join('');
                var newStartTime = str + '000000';
                var newEndTime = str2 + '235959';
                d.monitorId = id;
                d.startTime = newStartTime;
                d.endTime = newEndTime;
            };
            // 表格setting
            var setting = {
                listUrl: "/clbs/m/reportManagement/accStatistics/detail",
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'detailTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                drawCallbackFun: function () {
                    var api = myDetailTable.dataTable;
                    var startIndex = api.context[0]._iDisplayStart; //获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                    });
                },
                // pagingType: 'simple',
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true
            };
            myDetailTable = new TG_Tabel.createNew(setting);
            myDetailTable.init();
            $('#searchDetailTable').on('click', function (e) {
                myDetailTable.refresh();
            });
        },
        exportAlarm: function () {
            alarmReport.getCheckedNodes();
            if (!alarmReport.validates()) {
                return;
            }
            var length = $("#dataTable tbody tr").find("td").length;
            if (length <= 1) {
                layer.msg('无数据可以导出');
                return;
            }
            if (getRecordsNum() > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var timeInterval = $('#timeInterval').val().split('--');
            startTime = timeInterval[0];
            endTime = timeInterval[1];
            var arr = startTime.split('-');
            var arr2 = endTime.split('-');
            var str = arr.join('');
            var str2 = arr2.join('');
            var newStartTime = str;
            var newEndTime = str2;
            var url = "/clbs/m/reportManagement/accStatistics/export";
            var parameter = {
                "monitorIds": vehicleId,
                "startDate": newStartTime,
                "endDate": newEndTime,
            };
            json_ajax("POST", url, "json", true, parameter, alarmReport.exportCallback);
        },
        exportCallback: function (data) {
            if (data.success) {
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
        },
        accDetailExport: function () {
            alarmReport.getCheckedNodes();
            if (!alarmReport.validates()) {
                return;
            }
            var length = $("#detailTable tbody tr").find("td").length;
            if (length <= 1) {
                layer.msg('无数据可以导出');
                return;
            }
            if (getRecordsNum() > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var timeInterval = $('#timeInterval').val().split('--');
            startTime = timeInterval[0];
            endTime = timeInterval[1];
            var arr = startTime.split('-');
            var arr2 = endTime.split('-');
            var str = arr.join('');
            var str2 = arr2.join('');
            var newStartTime = str + '000000';
            var newEndTime = str2 + '235959';
            var url = "/clbs/m/reportManagement/accStatistics/detail/export";
            var parameter = {
                "monitorId": selectVehicleId,
                "startTime": newStartTime,
                "endTime": newEndTime,
            };
            json_ajax("POST", url, "json", true, parameter, alarmReport.exportCallback2);
        },
        exportCallback2: function (data) {
            if (data.success) {
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
        },
        getTable: function (table) {
            $('.toggle-vis').prop('checked', true);
            myTable = $(table).DataTable({
                "destroy": true,
                "dom": 'tiprl',// 自定义显示项
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
                    "sInfoFiltered": "（数据库中共为 _MAX_ 条记录）",
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
            $("#search_button").on("click", function () {
                var tsval = $("#simpleQueryParam").val();
                queryCondition = tsval;
                myTable.search(tsval, false, false).draw();
            });
        },
        reloadData: function (dataList) {
            var currentPage = myTable.page()
            myTable.clear()
            myTable.rows.add(dataList)
            myTable.page(currentPage).draw(false);
            myTable.search('', false, false).page(currentPage).draw();
        },
        //刷新列表
        refreshTable: function () {
            alarmReport.inquireClick(1, false);
        },
        getYesterDay: function () {
            var nowDate = new Date();
            var date = new Date(nowDate.getTime() - 24 * 60 * 60 * 1000);
            var seperator1 = "-";
            var year = date.getFullYear();
            var month = date.getMonth() + 1;
            var strDate = date.getDate();
            if (month >= 1 && month <= 9) {
                month = "0" + month;
            }
            if (strDate >= 0 && strDate <= 9) {
                strDate = "0" + strDate;
            }
            var yesterdate = year + seperator1 + month + seperator1 + strDate;
            return yesterdate;
        },
        openDrawer: function (e) {
            e.stopPropagation();
            $('#detail').addClass('active');
            var _this = $(e.target);
            if (e.target.nodeName !== 'TR') {
                _this = $(e.target).closest('tr');
            }
            var mId = _this.attr('data-orgId');
            selectVehicleId = mId;
            alarmReport.initDetailTable(mId);
        },
        closeDrawer: function () {
            if ($('#detail').hasClass('active')) {
                $('#detail').removeClass('active');
            }
        },
        //解析位置信息
        getDrivingAddress: function (event, latitude, longitude) {
            var url = '/clbs/v/monitoring/address';
            var param = {addressReverse: [latitude, longitude, '', "", 'vehicle']};
            var _this = $(event);
            $.ajax({
                type: "POST",//通常会用到两种：GET,POST。默认是：GET
                url: url,//(默认: 当前页地址) 发送请求的地址
                dataType: "json", //预期服务器返回的数据类型。"json"
                async: true, // 异步同步，true  false
                data: param,
                traditional: true,
                timeout: 8000, //超时时间设置，单位毫秒
                success: function (data) {//请求成功
                    _this.closest('td').html(data == '[]' ? '未定位' : data);
                },
            });
        },
    }

    $(function () {
        //初始化页面
        alarmReport.init();
        $('input').inputClear();
        $('#timeInterval').dateRangePicker({
            dateLimit: 31,
            isOffLineReportFlag: true,
            nowDate: alarmReport.getYesterDay(),
            isShowHMS: false
        });
        alarmReport.tableFilter();
        alarmReport.getTable('#dataTable');
        //当前时间
        alarmReport.getsTheCurrentTime();
        //组织下拉显示
        $("#groupSelect").bind("click", showMenuContent);
        //导出
        $("#exportAlarm").bind("click", alarmReport.exportAlarm);
        $("#accDetailExport").bind("click", alarmReport.accDetailExport);
        $("#refreshTable").bind("click", alarmReport.refreshTable);
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var param = $("#groupSelect").val();
                alarmReport.searchVehicleTree(param);
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
                    alarmReport.searchVehicleTree(param);
                }
                searchFlag = true;
            }, 500);
        });
        $('#queryType').on('change', function () {
            var param = $("#groupSelect").val();
            alarmReport.searchVehicleTree(param);
        });
    })
}(window, $))