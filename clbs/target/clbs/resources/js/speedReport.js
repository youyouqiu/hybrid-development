(function (window, $) {
    var myTable;
    //车辆列表
    var vehicleList = [];
    //车辆id列表
    var vehicleId = [];
    //开始时间
    var startTime;
    var endTime;
    var checkFlag = false; //判断组织节点是否是勾选操作
    var size;//当前权限监控对象数量
    var zTreeIdJson = {};

    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询
    var ifAllCheck = true; //刚进入页面小于100自动勾选
    var searchFlag = true;

    speedSearch = {
        init: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            // menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 0; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-text").html(menu_text);
            //车辆树
            var setting = {
                async: {
                    url: speedSearch.getSpeedReportTreeUrl,//"/clbs/m/functionconfig/fence/bindfence/vehicelTree",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: speedSearch.ajaxDataFilter
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
                    beforeClick: speedSearch.beforeClickVehicle,
                    onAsyncSuccess: speedSearch.zTreeOnAsyncSuccess,
                    beforeCheck: speedSearch.zTreeBeforeCheck,
                    onCheck: speedSearch.onCheckVehicle,
                    onNodeCreated: speedSearch.zTreeOnNodeCreated,
                    onExpand: speedSearch.zTreeOnExpand
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
                speedSearch.init();
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
                        dataFilter: speedSearch.ajaxQueryDataFilter
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
                        beforeClick: speedSearch.beforeClickVehicle,
                        onCheck: speedSearch.onCheckVehicle,
                        onExpand: speedSearch.zTreeOnExpand,
                        onNodeCreated: speedSearch.zTreeOnNodeCreated
                    }
                };
                $.fn.zTree.init($("#treeDemo"), querySetting, null);
            }
        },
        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData))
            var nodesArr;
            if($('#queryType').val() == "vehicle"){
                nodesArr = filterQueryResult(responseData, crrentSubV);
            }else {
                nodesArr = responseData;
            }
            for (var i = 0; i < nodesArr.length; i++) {
                nodesArr[i].open = true;
            }
            return nodesArr;
        },
        getSpeedReportTreeUrl: function (treeId, treeNode) {
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
                tMonth = speedSearch.doHandleMonth(tMonth + 1);
                tDate = speedSearch.doHandleMonth(tDate);
                var num = -(day + 1);
                // startTime = tYear + "-" + tMonth + "-" + tDate + " "
                //     + "00:00:00";
                startTime = tYear + "-" + tMonth + "-" + tDate;
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24
                    * parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = speedSearch.doHandleMonth(endMonth + 1);
                endDate = speedSearch.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate;
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = speedSearch.doHandleMonth(vMonth + 1);
                vDate = speedSearch.doHandleMonth(vDate);
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
                    vendMonth = speedSearch.doHandleMonth(vendMonth + 1);
                    vendDate = speedSearch.doHandleMonth(vendDate);
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
                    : nowDate.getDate());/* + " " + "00:00:00";*/
            endTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate());
                /*+ " "
                + ("23")
                + ":"
                + ("59")
                + ":"
                + ("59");*/
            var atime = $("#atime").val();
            if (atime != undefined && atime != "") {
                startTime = atime;
            }
        },
        inquireClick: function (number) {
            if (number == 0) {
                speedSearch.getsTheCurrentTime();
            } else if (number == 'yes') {// 昨天
                speedSearch.getsTheCurrentTime(true);
            } else if (number == -1) {
                speedSearch.startDay(-1)
            } else if (number == -3) {
                speedSearch.startDay(-3)
            } else if (number == -7) {
                speedSearch.startDay(-7)
            }
            if (number != 1) {
                $('#timeInterval').val(startTime + '--' + endTime);
                startTime = startTime;
                endTime = endTime;
            } else {
                var timeInterval = $('#timeInterval').val().split('--');
                startTime = timeInterval[0];
                endTime = timeInterval[1];
            };
            speedSearch.getCheckedNodes();
            if (!speedSearch.validates()) {
                return;
            }
            ;
            var url = "/clbs/m/reportManagement/speedReport/getSpeedingReportList";
            var parameter = {
                "vehicleList": vehicleList,
                "monitorIds": vehicleId,
                "startTime": startTime,
                "endTime": endTime
            };
            json_ajax("POST", url, "json", true, parameter, speedSearch.getCallback);
        },
        exportAlarm: function () {
            speedSearch.getCheckedNodes();
            if (!speedSearch.validates()) {
                return;
            }
            if ($("#dataTable tbody tr td").hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据，无法导出");
                return;
            }
            if(getRecordsNum() > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var timeInterval = $('#timeInterval').val().split('--');
            startTime = timeInterval[0];
            endTime = timeInterval[1];
            var url = "/clbs/m/reportManagement/speedReport/exportSpeedingReportList"
            var param = {monitorId:vehicleId,startTime:startTime,endTime:endTime}
            exportExcelUsePost(url,param)
            // var url = "/clbs/m/reportManagement/speedReport/exportSpeedingReportList?monitorId=" + vehicleId + "&startTime=" + startTime + "&endTime=" + endTime;
            // window.location.href = url;
            // json_ajax("POST", url, "json", true, parameter, speedSearch.exportCallback);
        },
        validates: function () {
            return $("#speedlist").validate({
                rules: {
                    startTime: {
                        required: true
                    },
                    endTime: {
                        required: true,
                        compareDate: "#timeInterval",
                    },
                    groupSelect: {
                        zTreeChecked: "treeDemo"
                    }
                },
                messages: {
                    startTime: {
                        required: "请选择开始日期！",
                    },
                    endTime: {
                        required: "请选择结束时间！",
                        compareDate: endtimeComStarttime,
                    },
                    groupSelect: {
                        zTreeChecked: vehicleSelectBrand,
                    }
                }
            }).form();
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
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (size <= 100 && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            speedSearch.getCharSelect(treeObj);
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
                if (nodesLength > TREE_MAX_CHILDREN_LENGTH) {
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
                    speedSearch.getCheckedNodes();
                    speedSearch.validates();
                }, 600);
            }
            crrentSubV = [];
            crrentSubV.push(treeNode.id);
            speedSearch.getCharSelect(zTree);
            speedSearch.getCheckedNodes();
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
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (treeNode.type == "group") {
                var url = "/clbs/m/functionconfig/fence/bindfence/putMonitorByGroup";
                json_ajax("post", url, "json", false, {
                    "groupId": treeNode.id,
                    "isChecked": treeNode.checked,
                    "monitorType": "vehicle"
                }, function (data) {
                    var result = data.obj;
                    var parentNode;
                    if (result != null && result != undefined) {
                        $.each(result, function (i) {
                            var pid = i; //获取键值
                            var chNodes = result[i];//获取对应的value
                            if (zTreeIdJson[pid] != undefined) {
                                var parentTid = zTreeIdJson[pid][0];
                                parentNode = treeObj.getNodeByTId(parentTid);
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
                        speedSearch.getGroupChild(node.children, assign);
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
        getCallback: function (data) {
            if (data.success == true) {
                dataListArray = [];//用来储存显示数据
                if (data.obj != null && data.obj.length != 0) {
                    var alarm = data.obj;
                    for (var i = 0; i < alarm.length; i++) {
                        var dateList =
                            [
                                i + 1,
                                alarm[i].monitorName,
                                alarm[i].assignmentName,
                                alarm[i].employeeName,
                                alarm[i].overSpeedNum,
                                parseFloat(alarm[i].maxSpeed),
                                parseFloat(alarm[i].minSpeed),
                                parseFloat(alarm[i].averageSpeed)
                            ];
                        dataListArray.push(dateList);
                    }
                    speedSearch.reloadData(dataListArray);
                    $("#simpleQueryParam").val("");
                } else {
                    speedSearch.reloadData(dataListArray);
                    $("#simpleQueryParam").val("");
                }
            } else {
                if (data.msg != null) {
                    layer.msg(data.msg, {move: false});
                }
            }
            $("#exportAlarm").removeAttr("disabled");
        },
        exportCallback: function (date) {
            if (date == true) {
                var url = "/clbs/m/reportManagement/speedReport/export";
                window.location.href = url;
            } else {
                layer.msg(exportFail, {move: false});
            }
        },
        getTable: function (table) {
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
                "lengthMenu": [5,10, 20, 50, 100, 200],
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
            $('.toggle-vis').on('change', function (e) {
                var column = myTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });
            $("#search_button").on("click", function () {
                var tsval = $("#simpleQueryParam").val()
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
            speedSearch.inquireClick(1, false);
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
        }
    }
    $(function () {
        $('input').inputClear();
        //初始化页面
        speedSearch.init();
        speedSearch.getTable('#dataTable');
        //当前时间
        speedSearch.getsTheCurrentTime();
        // $('#timeInterval').dateRangePicker({dateLimit: 30});
        $('#timeInterval').dateRangePicker({
            dateLimit: 31,
            isOffLineReportFlag: true,
            nowDate: speedSearch.getYesterDay(),
            isShowHMS: false
        });
        $("#groupSelect").bind("click", showMenuContent);
        //导出
        $("#exportAlarm").bind("click", speedSearch.exportAlarm);
        $("#refreshTable").bind("click", speedSearch.refreshTable);

        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var param = $("#groupSelect").val();
                speedSearch.searchVehicleTree(param);
            }
            ;
        });

        /**
         * 监控对象树模糊查询
         */
        var inputChange;
        $("#groupSelect").on('input propertychange', function (value) {
            if (inputChange !== undefined) {
                clearTimeout(inputChange);
            }
            ;
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                searchFlag = true;
            }
            ;
            inputChange = setTimeout(function () {
                if (searchFlag) {
                    var param = $("#groupSelect").val();
                    speedSearch.searchVehicleTree(param);
                }
                searchFlag = true;
            }, 500);
        });
        $('#queryType').on('change', function (){
            var param=$("#groupSelect").val();
            speedSearch.searchVehicleTree(param);
        });
    })
}(window, $))