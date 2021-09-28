(function (window, $) {
    var myTable;
    //车辆列表
    var vehicleList = [];
    //车辆id列表
    var vehicleId = [];
    //开始时间
    var startTime;
    //结束时间
    var endTime;
    var addressMsg = [];
    var myTable;
    var stopLoc = [];
    var checkFlag = false; //判断组织节点是否是勾选操作
    var size;//当前权限监控对象数量
    var zTreeIdJson = {};

    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询
    var ifAllCheck = true; //刚进入页面小于TREE_MAX_CHILDREN_LENGTH自动勾选
    var searchFlag = true;
    var queryCondition = '';
    var getAddressStatus = false;

    terminalParkingSearch = {
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
                    url: terminalParkingSearch.getParkingReportTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: terminalParkingSearch.ajaxDataFilter
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
                    beforeClick: terminalParkingSearch.beforeClickVehicle,
                    onAsyncSuccess: terminalParkingSearch.zTreeOnAsyncSuccess,
                    beforeCheck: terminalParkingSearch.zTreeBeforeCheck,
                    onCheck: terminalParkingSearch.onCheckVehicle,
                    onNodeCreated: terminalParkingSearch.zTreeOnNodeCreated,
                    onExpand: terminalParkingSearch.zTreeOnExpand
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
                terminalParkingSearch.init();
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
                        dataFilter: terminalParkingSearch.ajaxQueryDataFilter
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
                        beforeClick: terminalParkingSearch.beforeClickVehicle,
                        onCheck: terminalParkingSearch.onCheckVehicle,
                        onExpand: terminalParkingSearch.zTreeOnExpand,
                        onNodeCreated: terminalParkingSearch.zTreeOnNodeCreated
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
        getParkingReportTreeUrl: function (treeId, treeNode) {
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
                tMonth = terminalParkingSearch.doHandleMonth(tMonth + 1);
                tDate = terminalParkingSearch.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate;
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24
                    * parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = terminalParkingSearch.doHandleMonth(endMonth + 1);
                endDate = terminalParkingSearch.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate;
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = terminalParkingSearch.doHandleMonth(vMonth + 1);
                vDate = terminalParkingSearch.doHandleMonth(vDate);
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
                    vendMonth = terminalParkingSearch.doHandleMonth(vendMonth + 1);
                    vendDate = terminalParkingSearch.doHandleMonth(vendDate);
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
                    : nowDate.getDate())
                + " "
                + ("23")
                + ":"
                + ("59")
                + ":"
                + ("59");
            var atime = $("#atime").val();
            if (atime != undefined && atime != "") {
                startTime = atime;
            }
            return
        },
        inquireClick: function (number) {
            $(".ToolPanel").css("display", "block");
            if (number == 0) {
                terminalParkingSearch.getToday(number);
            } else if (number == -1) {
                terminalParkingSearch.startDay(-1)
            } else if (number == -3) {
                terminalParkingSearch.startDay(-3)
            } else if (number == -7) {
                terminalParkingSearch.startDay(-7)
            }
            terminalParkingSearch.getCheckedNodes();
            if (!terminalParkingSearch.validates()) {
                return;
            }
            if (number != 1) {
                $('#timeInterval').val(startTime + '--' + endTime);
                //从页面获取到开始时间
                startTime = startTime;
                //从页面获取到结束时间
                endTime = endTime;
            } else {
                var timeInterval = $('#timeInterval').val().split('--');
                startTime = timeInterval[0];
                endTime = timeInterval[1];
            }
            var url = "/clbs/m/reportManagement/terminal/parkingReport/getStopBigDataNew";
            var parameter = {"vehicleId": vehicleId, "startTime": startTime, "endTime": endTime};
            json_ajax("POST", url, "json", true, parameter, terminalParkingSearch.getCallback);
        },
        exportAlarm: function () {
            terminalParkingSearch.getCheckedNodes();
            if (!terminalParkingSearch.validates()) {
                return;
            }
            ;
            var timeInterval = $('#timeInterval').val().split('--');
            startTime = timeInterval[0];
            endTime = timeInterval[1];
            var url = "/clbs/m/reportManagement/terminal/parkingReport/export";
            var parameter = {
                "vehicleId": vehicleId, "startTime": startTime, "endTime": endTime, "exportType": 2
            };
            json_ajax("POST", url, "json", true, parameter, terminalParkingSearch.exportCallback);
        },
        validates: function () {
            return $("#speedlist").validate({
                rules: {
                    startTime: {
                        required: true
                    },
                    endTime: {
                        required: true,
                        compareDate: "#startTime",
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
            if (size <= TREE_MAX_CHILDREN_LENGTH && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            terminalParkingSearch.getCharSelect(treeObj);
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
//       		var flag = true;
//       		if (!treeNode.checked) {
//          		var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
//          												.getCheckedNodes(true), v = "";
//              	var nodesLength = 0;
//              	for (var i=0;i<nodes.length;i++) {
//              		if(nodes[i].type == "people" || nodes[i].type == "vehicle"){
//              			nodesLength += 1;
//              		}
//              	}
//              	if (treeNode.type == "group" || treeNode.type == "assignment"){ // 判断若勾选节点数大于5000，提示
//              		var zTree = $.fn.zTree.getZTreeObj("treeDemo");
//              		json_ajax("post", "/clbs/m/personalized/ico/getCheckedVehicle",
//                      		"json", false, {"parentId": treeNode.id,"type": treeNode.type}, function (data) {
//                      			nodesLength += data.obj;
//                       })
//              	} else if (treeNode.type == "people" || treeNode.type == "vehicle"){
//              		nodesLength += 1;
//              	}
//              	if(nodesLength > 5000){
//              		layer.msg("最多勾选5000个监控对象！");
//              		flag = false;
//              	}
//       		}
//	       	if(flag){
//	       		//若组织节点已被勾选，则是勾选操作，改变勾选操作标识
//	           	if(treeNode.type == "group" && !treeNode.checked){
//	           		checkFlag = true;
//	           	}
//	       	}
//	       	return flag;
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                searchFlag = false;
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    terminalParkingSearch.getCheckedNodes();
                    terminalParkingSearch.validates();
                }, 600);
            }
            crrentSubV = [];
            crrentSubV.push(treeNode.id);
            terminalParkingSearch.getCheckedNodes();
            terminalParkingSearch.getCharSelect(zTree);
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
                        terminalParkingSearch.getGroupChild(node.children, assign);
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
        getCallback: function (date) {
            getAddressStatus = false;
            if (date.success == true) {
                //清除上一次查询的地址存储数组
                stopLoc = [];
                dataListArray = [];//用来储存显示数据
                if (date.obj != null && date.obj.length != 0) {
                    var alarm = date.obj;
                    for (var i = 0; i < alarm.length; i++) {
                        var dateList =
                            [
                                i + 1,
                                alarm[i].monitorName,
                                alarm[i].assignmentName,
                                alarm[i].employeeName,
                                alarm[i].employeePhone,
                                alarm[i].stopNum,
                                alarm[i].stopTime,
                                alarm[i].idleSpeedMile,
                                alarm[i].address ? alarm[i].address : "加载中..."
                            ];
                        dataListArray.push(dateList);
                        stopLoc.push(alarm[i].stopLocation);
                    }
                    terminalParkingSearch.reloadData(dataListArray);
                    $("#simpleQueryParam").val("");
                } else {
                    terminalParkingSearch.reloadData(dataListArray);
                    $("#simpleQueryParam").val("");
                }
                myTable.search("", false, false).draw();
                simpleQueryParam = "";
            }
        },
        exportCallback: function (date) {
            if (date == true) {
                if (getRecordsNum() > 60000) {
                    return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
                }
                var url = "/clbs/m/reportManagement/terminal/parkingReport/export?simpleQueryParam=" + queryCondition;
                window.location.href = url;
            } else {
                layer.msg(exportFail);
            }
        },
        //对显示的数据进行逆地址解析
        getAddress: function (addressStr) {
            getAddressStatus = true;
            var $dataTableTbody = $("#dataTable tbody");
            var dataLength = $dataTableTbody.children("tr").length;
            var num = 0;
            var startIndex = null;
            for (var i = 0; i < dataLength; i++) {
                num++;
                var alarmLocation = $dataTableTbody.children("tr:nth-child(" + (i + 1) + ")").children("td:nth-child(9)").text();
                if (alarmLocation !== '加载中...') continue;
                if (startIndex === null) startIndex = i;
                var n = $dataTableTbody.children("tr:nth-child(" + (i + 1) + ")").children("td:nth-child(1)").text();
                var stopMsg = [];
                //经纬度正则表达式
                var Reg = /^(180\.0{4,7}|(\d{1,2}|1([0-7]\d))\.\d{4,20})(,)(90\.0{4,8}|(\d|[1-8]\d)\.\d{4,20})$/;
                if (addressStr[n - 1] != null && (Reg.test(addressStr[n - 1]) || addressStr[n - 1] === '0.0,0.0')) {
                    stopMsg = [addressStr[n - 1].split(",")[0], addressStr[n - 1].split(",")[1]];
                } else {
                    stopMsg = ["124.411991", "29.043817"];
                }
                addressMsg.push(stopMsg);
                if (num == dataLength) {
                    var addressIndex = 0;
                    var addressArray = [];
                    backAddressMsg1(addressIndex, addressMsg, null, addressArray, "dataTable", 9, startIndex);
                    addressMsg = [];
                }
            }
        },
        getTable: function (table) {
            $('.toggle-vis').prop('checked', true);
            myTable = $(table).DataTable({
                "columnDefs": [{
                    "targets": [0, 2, 3, 4, 5, 6, 7, 8],
                    "searchable": false
                }],
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
                "drawCallback": function (settings) {
                    var newStopLoc = stopLoc;
                    var aiDisplay = settings.aiDisplay;
                    if (aiDisplay.length != '0' && aiDisplay.length < stopLoc.length) {
                        newStopLoc = [];
                        for (var i = 0; i < aiDisplay.length; i++) {
                            newStopLoc.push(stopLoc[aiDisplay[i]]);
                        }
                    }
                    var $dataTableTbody = $("#dataTable tbody");
                    var alarmLocation = $dataTableTbody.children("tr:last-child").children("td:nth-child(9)").text();
                    //报警位置进行逆地址解析
                    if (alarmLocation == "加载中..." && !getAddressStatus) {
                        terminalParkingSearch.getAddress(newStopLoc);
                    }
                },
                "order": [
                    [0, null]
                ],// 第一列排序图标改为默认

            });
            myTable.on('draw', function () {
                setTimeout(function () {
                    getAddressStatus = false;
                }, 300)
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
            $("#simpleQueryParam").val("");
            terminalParkingSearch.inquireClick(1, false);
            myTable.search("", false, false).draw();
            simpleQueryParam = "";
        },
        getToday: function () {
            var nowDate = new Date();
            var date = new Date(nowDate.getTime());
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
            startTime = yesterdate;
            endTime = yesterdate;

            return yesterdate;
        }
    }
    $(function () {
        terminalParkingSearch.init();
        terminalParkingSearch.getTable('#dataTable');
        $('input').inputClear();
        // $('#timeInterval').dateRangePicker({dateLimit:30});
        $('#timeInterval').dateRangePicker({
            dateLimit: 31,
            isOffLineReportFlag: true,
            nowDate: terminalParkingSearch.getToday(),
            isShowHMS: false
        });
        //当前时间
        terminalParkingSearch.getsTheCurrentTime();
        $("#groupSelect").bind("click", showMenuContent);
        //导出
        $("#exportAlarm").bind("click", terminalParkingSearch.exportAlarm);
        $("#refreshTable").bind("click", terminalParkingSearch.refreshTable);

        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var param = $("#groupSelect").val();
                terminalParkingSearch.searchVehicleTree(param);
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
                    terminalParkingSearch.searchVehicleTree(param);
                }
                searchFlag = true;
            }, 500);
        });
        $('#queryType').on('change', function () {
            var param = $("#groupSelect").val();
            terminalParkingSearch.searchVehicleTree(param);
        });
    })
}(window, $))