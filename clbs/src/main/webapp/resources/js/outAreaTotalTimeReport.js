(function (window, $) {
    //开始时间
    var queryDateStr;
    //结束时间
    var endTime;
    var myTable;
    //判断组织节点是否是勾选操作
    var checkFlag = false;
    var dbFlag = false;
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
    var myBrowserType = 0;
    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询
    //用于ie浏览器判断选择节点树时是否需要调用搜索功能
    var isSearch = true;
    var getCount = true;

    outAreaTotalTimeReport = {
        init: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            //var table = $("#dataTable tr th:gt(1)");
            // menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(1) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 0; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-text").html(menu_text);
            //车辆树
            var setting = {
                async: {
                    url: outAreaTotalTimeReport.getParkingReportTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "type": "multiple",
                        "icoType": "0"
                    },
                    dataFilter: outAreaTotalTimeReport.ajaxDataFilter
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
                    beforeClick: outAreaTotalTimeReport.beforeClickVehicle,
                    onAsyncSuccess: outAreaTotalTimeReport.zTreeOnAsyncSuccess,
                    beforeCheck: outAreaTotalTimeReport.zTreeBeforeCheck,
                    onCheck: outAreaTotalTimeReport.onCheckVehicle,
                    onNodeCreated: outAreaTotalTimeReport.zTreeOnNodeCreated,
                    onExpand: outAreaTotalTimeReport.zTreeOnExpand,
                    // onDblClick: outAreaTotalTimeReport.onDblClickVehicle
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
                getCount = true;
                outAreaTotalTimeReport.init();
            } else {
                bflag = true;
                getCount = false;
                var querySetting = {
                    async: {
                        url: "/clbs/a/search/reportFuzzySearch",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"type": $('#queryType').val(), "queryParam": param, 'queryType': 'multiple'},
                        dataFilter: outAreaTotalTimeReport.ajaxQueryDataFilter
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
                        beforeClick: outAreaTotalTimeReport.beforeClickVehicle,
                        onCheck: outAreaTotalTimeReport.onCheckVehicle,
                        onExpand: outAreaTotalTimeReport.zTreeOnExpand,
                        onNodeCreated: outAreaTotalTimeReport.zTreeOnNodeCreated,
                        beforeCheck: outAreaTotalTimeReport.zTreeBeforeCheck,
                    }
                };
                $.fn.zTree.init($("#treeDemo"), querySetting, null);
            }
        },
        onDblClickVehicle: function (e, treeId, treeNode) {
            var userAgent = navigator.userAgent; // 取得浏览器的userAgent字符串
            if (userAgent.indexOf("Firefox") > -1) return; // 判断是否Firefox浏览器
            if (!treeNode) return;
            dbFlag = true;
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.checkAllNodes(false);
            outAreaTotalTimeReport.getCheckedNodes("treeDemo");
            // $("#groupSelect").val(treeNode.name);
            // search_ztree('treeDemo', 'groupSelect', 'group');
            treeObj.checkNode(treeNode, !treeNode.checked, false, true);
        },
        getParkingReportTreeUrl: function (treeId, treeNode) {
            // if (treeId == 'treeDemo') {
            //     // return "/clbs/m/basicinfo/enterprise/professionals/tree";
            // }
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle";
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
            if (getRecordsNum() > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var exportUrl = "/clbs/m/reportManagement/outTotalTime/exportOutAreaDurationStatistics" +
                "?queryParam=" + simpleQueryParam;
            window.location.href = exportUrl
        },
        validates: function () {
            return $("#lifeCycleForm").validate({
                rules: {
                    queryDateStr: {
                        required: true
                    },
                    dateNum: {
                        required: true,
                        min: 0,
                        maxlength: 3,
                        digits: true,
                    },
                    groupSelect: {
                        zTreeChecked: "treeDemo"
                    }
                },
                messages: {
                    queryDateStr: {
                        required: "请选择日期！",
                    },
                    groupSelect: {
                        zTreeChecked: '请至少选择一个监控对象',
                    },
                    dateNum: {
                        required: dateNumError,
                        min: dateNumError,
                        maxlength: dateNumError,
                        digits: dateNumError,
                    },
                }
            }).form();
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            // if (treeId == "treeDemo") {
            //     size = responseData.length;
            //     return responseData;
            // }
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
            if (size <= 100 && ifAllCheck) {
                treeObj.checkAllNodes(true);
                var nodes = treeObj.getNodes();
                for (var i = 0; i < nodes.length; i++) { //设置节点展开
                    treeObj.expandNode(nodes[i], true, false, true);
                }
            }
            outAreaTotalTimeReport.getCharSelect(treeObj);
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
                zTree.expandNode(treeNode, true, true, false, false); // 展开节点
                setTimeout(() => {
                    outAreaTotalTimeReport.getCheckedNodes("treeDemo");
                    outAreaTotalTimeReport.validates();
                }, 600);
            }
            outAreaTotalTimeReport.getCheckedNodes("treeDemo");
            outAreaTotalTimeReport.getCharSelect(zTree);
            crrentSubV = [];
            crrentSubV.push(treeNode.id);
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
            console.log(nodes, 'nodes');
            console.log(allNodes, 'allNodes');
            if (nodes.length > 0) {
                $("#groupSelect").val(allNodes[0].name);
            } else {
                $("#groupSelect").val("");
            }
        },
        getCheckedNodes: function (treeId) {
            var zTree = $.fn.zTree.getZTreeObj(treeId),
                nodes = zTree.getCheckedNodes(true),
                groupIds = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (treeId == "treeDemo" && nodes[i].type == "vehicle") {
                    groupIds += nodes[i].id + ",";
                }
            }
            groupId = groupIds;
        },
        getTable: function (table) {
            $('.toggle-vis').prop('checked', true);
            myTable = $(table).DataTable({
                "columnDefs": [{
                    "targets": [0, 2, 3, 4, 5, 6],
                    "searchable": false
                }],
                "destroy": true,
                /*"dom": 'tiprl',// 自定义显示项*/
                "lengthChange": true, // 是否允许用户自定义显示数量
                "bPaginate": true, // 翻页功能
                "bFilter": false, // 列筛序功能
                "searching": true, // 本地搜索
                "ordering": false, // 排序功能
                "Info": true, // 页脚信息
                "autoWidth": true, // 自动宽度
                "stripeClasses": [],
                "pageLength": 10,
                "lengthMenu": [5, 10, 20, 50, 100, 200],
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
                ], // 第一列排序图标改为默认

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
                simpleQueryParam = queryCondition;
                myTable.column(1).search(queryCondition, false, false).draw();
            });
        },
        //刷新列表
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            outAreaTotalTimeReport.inquireClick(1);
            myTable.column(1).search("", false, false).draw();
            simpleQueryParam = "";
        },
        //获取当前时间，格式YYYY-MM-DD
        getNowFormatDate: function () {
            var date = new Date();
            var separator = "-";
            var year = date.getFullYear();
            var month = date.getMonth() + 1;
            var strDate = date.getDate();
            if (month >= 1 && month <= 9) {
                month = "0" + month;
            }
            if (strDate >= 0 && strDate <= 9) {
                strDate = "0" + strDate;
            }
            return (year + separator + month + separator + strDate);
        },
        //设置时间
        setNewDay: function (day) {
            var timeInterval = $('#timeInterval').val();
            var startTimeIndex = timeInterval.replace(/-/g, "/");
            var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
            var dateList = new Date();
            dateList.setTime(vtoday_milliseconds);
            var vYear = dateList.getFullYear();
            var vMonth = dateList.getMonth();
            var vDate = dateList.getDate();
            vMonth = outAreaTotalTimeReport.doHandleMonth(vMonth + 1);
            vDate = outAreaTotalTimeReport.doHandleMonth(vDate);
            startTime = vYear + "-" + vMonth + "-" + vDate;
            $('#timeInterval').val(startTime);
        },
        inquireClick: function (number) {
            if (number != 1) {
                outAreaTotalTimeReport.setNewDay(number);
            }
            outAreaTotalTimeReport.getCheckedNodes('treeDemo');
            console.log($('#groupSelect').val(), 'groupSelect');
            if (!outAreaTotalTimeReport.validates()) {
                return;
            }
            queryDateStr = $('#timeInterval').val();
            totalTime = $('#totalTime').val();
            var ajaxDataParam = {
                "monitorIds": groupId,
                "endTime": queryDateStr,
                "totalTime": totalTime
            }
            var url = "/clbs/m/reportManagement/outTotalTime/getOutAreaDurationStatisticsList";
            json_ajax("POST", url, "json", true, ajaxDataParam, outAreaTotalTimeReport.getCallback);
        },
        getCallback: function (date) {
            if (date.success == true) {
                dataListArray = []; //用来储存显示数据
                if (date.obj != null && date.obj.length != 0) {
                    $("#exportAlarm").removeAttr("disabled");
                    var result = date.obj;
                    var len = result.length;
                    for (var i = 0; i < len; i++) {
                        var recordData = result[i];
                        var dateList = [
                            i + 1,
                            recordData.plateNumber,
                            recordData.groupName,
                            recordData.plateColor,
                            recordData.vehicleType,
                            recordData.outTime,
                            recordData.outTotalTime,
                            recordData.address
                        ];
                        dataListArray.push(dateList);
                    }
                    outAreaTotalTimeReport.reloadData(dataListArray);
                    $("#simpleQueryParam").val("");
                } else {
                    outAreaTotalTimeReport.reloadData(dataListArray);
                    $("#simpleQueryParam").val("");
                    $("#exportAlarm").attr("disabled", "disabled");
                }
                simpleQueryParam = "";
                myTable.column(1).search("", false, false).draw();
            } else {
                if (date.msg != null) {
                    layer.msg(date.msg, {
                        move: false
                    });
                }
            }
        },
        reloadData: function (dataList) {
            var currentPage = myTable.page();
            myTable.clear();
            myTable.rows.add(dataList);
            myTable.page(currentPage).draw(false);
            myTable.column(1).search('', false, false).page(currentPage).draw();
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
    }
    $(function () {
        outAreaTotalTimeReport.init();
        outAreaTotalTimeReport.getTable('#dataTable');
        $('input').inputClear();
        laydate.render({
            elem: '#timeInterval',
            theme: '#6dcff6',
            max: outAreaTotalTimeReport.getYesterDay()
        });
        $("#timeInterval").val(outAreaTotalTimeReport.getYesterDay());
        $("#groupSelect").bind("click", showMenuContent);
        //导出
        $("#exportAlarm").bind("click", outAreaTotalTimeReport.exportAlarm);
        $("#refreshTable").bind("click", outAreaTotalTimeReport.refreshTable);

        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var param = $("#groupSelect").val();
                outAreaTotalTimeReport.searchVehicleTree(param);
            };
        });
        /**
         * 监控对象树模糊查询
         */
        var inputChange;
        $("#groupSelect").on('input propertychange', function (value) {
            // var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            // treeObj.checkAllNodes(false);
            // search_ztree('treeDemo', 'groupSelect', 'group');
            if (inputChange !== undefined) {
                clearTimeout(inputChange);
            }
            ;
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                isSearch = true;
            }
            $("#charSelect").val("").attr("data-id", "").bsSuggest("destroy");
            inputChange = setTimeout(function () {
                if (isSearch) {
                    var param = $("#groupSelect").val();
                    outAreaTotalTimeReport.searchVehicleTree(param);
                }
                isSearch = true;
            }, 500);
        });
    })
}(window, $))