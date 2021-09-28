(function (window, $) {
    // 车辆id列表
    var allVid;
    var allVid2;
    var vnameList = [];
    var startime;
    var endtime;
    var number;
    var mileageList;
    var mileList;
    var milcName;
    var barWidth;
    var myTable;
    var myTable2;
    var myTable3;
    var mileage;
    var zTreeIdJson = {};
    var checkFlag = false; // 判断组织节点是否是勾选操作
    var size;// 当前权限监控对象数量
    var currentMonth1 = null;
    var currentMonth2 = null;
    var ifAllCheck = true; //刚进入页面小于5000自动勾选
    var isSearch2 = true;
    var isSearch3 = true;
    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询？
    second2Hour = function (second, hms) {

        if (second === null || second === undefined || isNaN(second)) {
            return second;
        }
        return parseInt(second * 10) / 10;
//		if(!hms){
//			return parseInt(second / 3600 * 100) / 100;
//		}else{
//			var add0 = function(n){
//				if(n<10){
//					return '0' + n.toString();
//				}
//				return n.toString()
//			};
//			var h = parseInt(second / 3600);
//			second -= 3600 * h;
//			var m = parseInt(second / 60);
//			second -= 60 * m ;
//			var s = second;
//			return add0(h) + ':' + add0(m) + ':' + add0(s) ;
//		}
    };
    mainObj = {
        getCurrentTabIndex: function () {
            if ($('#allReport').hasClass('active')) {
                return '';
            }
            if ($('#amountReport').hasClass('active')) {
                return 3;
            }
            if ($('#spillReport').hasClass('active')) {
                return 2;
            }
        },
        getCurrentMonth: function () {
            var now = new Date();
            return mainObj.formatMonth(now);
        },
        formatMonth: function (monthStr) {
            var now = new Date(monthStr);
            var year = now.getFullYear();
            var month = now.getMonth() + 1;
            var add0 = function (n) {
                if (n < 10) {
                    return '0' + n.toString();
                }
                return n.toString()
            };
            return year + '-' + add0(month);
        },

        renderSelect: function (id) {
            var select = $(id);
            var now = new Date();
            var year = now.getFullYear();
            var month = now.getMonth() + 1;
            var tmpl = '<option value="$name">$name</option>';
            var add0 = function (n) {
                if (n < 10) {
                    return '0' + n.toString();
                }
                return n.toString()
            };
            for (var i = 0; i < 12; i++) {
                if (i < month) {
                    select.append($(tmpl.replace(/\$name/g, year + '-' + add0(month - i))));
                } else {
                    select.append($(tmpl.replace(/\$name/g, (year - 1) + '-' + add0(12 - i + month))));
                }
            }
        },
        renderTh: function (id, year, month) {
            var tr = $(id).find('thead tr');
            tr.empty();
            var dates = 30;
            if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
                dates = 31;
            } else if (month == 2) {
                dates = 28;
                if (year % 100 != 0) {
                    if (year % 4 == 0) {
                        dates = 29;
                    }
                } else {
                    if (year % 400 == 0) {
                        dates = 29;
                    }
                }
            }
            var tmpl = '<th class="text-center">$name</th>';
            tr.append($(tmpl.replace('$name', '序号')));
            tr.append($(tmpl.replace('$name', '道路运输企业')));
            for (var i = 1; i <= dates; i++) {
                tr.append($(tmpl.replace('$name', i)));
            }
            tr.append($(tmpl.replace('$name', '合计')));
        },
        renderTh2: function (id, year, month) {
            var tr = $(id).find('thead tr');
            tr.empty();
            var dates = 30;
            if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
                dates = 31;
            } else if (month == 2) {
                dates = 28;
                if (year % 100 != 0) {
                    if (year % 4 == 0) {
                        dates = 29;
                    }
                } else {
                    if (year % 400 == 0) {
                        dates = 29;
                    }
                }
            }
            var tmpl = '<th class="text-center">$name</th>';
            tr.append($(tmpl.replace('$name', '序号')));
            tr.append($(tmpl.replace('$name', '车牌号')));
            tr.append($(tmpl.replace('$name', '车牌颜色')));
            tr.append($(tmpl.replace('$name', '车辆类型')));
            tr.append($(tmpl.replace('$name', '所属道路运输企业')));
            for (var i = 1; i <= dates; i++) {
                tr.append($(tmpl.replace('$name', i)));
            }
            tr.append($(tmpl.replace('$name', '合计')));
        },
        monthClick: function (__i) {
            var toPostMonth = null;
            // 本月
            if (__i == 0) {
                var currentMonth = mainObj.getCurrentMonth();
                $('#select1').val(currentMonth);
                toPostMonth = currentMonth;
            }
            // 上一月
            if (__i == -1) {
                var currentMonth = $('#select1').val();
                var date = new Date(currentMonth);
                date.setMonth(date.getMonth() - 1);
                var str = mainObj.formatMonth(date);
                $('#select1').val(str);
                toPostMonth = str;
            }
            // 查询
            if (__i == 1) {
                toPostMonth = $('#select1').val();
            }
            currentMonth1 = toPostMonth;
            // 请求路径
            var url = "/clbs/cb/cbReportManagement/userOnlineTime/getGroupsOnlineTime";
            var m = mainObj.getDateFromMonth(toPostMonth);
            var groupid = $('#groupSelect').data('groupids');
            // 请求参数
            var pdata = {"groupId": groupid, "nowMonth": m[0], "nextMonth": m[1]};
            // 发送请求给服务器
            json_ajax("POST", url, "json", true, pdata, mainObj.dataTableCB1);
        },
        getDateFromMonth: function (month) {
            var date = new Date(month);
            var year = date.getFullYear();
            var m = date.getMonth() + 1;
            var ms = m.toString();
            var ms2 = (m + 1).toString();
            if (ms.length == 1) {
                ms = '0' + ms;
            }
            if (ms2.length == 1) {
                ms2 = '0' + ms2;
            }
            return [year + '-' + ms, year + '-' + ms2];
        },
        init: function (treeId) {
            var now = new Date();
            var nowYear = now.getFullYear();
            var nowMonth = now.getMonth() + 1;
            mainObj.renderSelect('#select2');
            mainObj.renderTh2('#dataTable2', nowYear, nowMonth);
            $("[data-toggle='tooltip']").tooltip();
            var setting = {
                async: {
                    // url:
                    // "/clbs/m/functionconfig/fence/bindfence/vehicelTree",
                    url: mainObj.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple"},
                    otherParam: {"icoType": "0"},
                    dataFilter: mainObj.ajaxDataFilter
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
                    beforeClick: mainObj.beforeClickVehicle,
                    onCheck: mainObj.onCheckVehicle,
                    beforeCheck: mainObj.zTreeBeforeCheck,
                    onExpand: mainObj.zTreeOnExpand,
                    // beforeAsync: mainObj.zTreeBeforeAsync,
                    onAsyncSuccess: mainObj.zTreeOnAsyncSuccess,
                    onNodeCreated: mainObj.zTreeOnNodeCreated
                }
            };
            if (treeId == null || treeId == undefined) {
                $.fn.zTree.init($("#treeDemo2"), setting, null);
                $.fn.zTree.init($("#treeDemo3"), setting, null);
            }
            if (treeId === 'treeDemo3') {
                $.fn.zTree.init($("#treeDemo3"), setting, null);
            }
            if (treeId === 'treeDemo2') {
                $.fn.zTree.init($("#treeDemo2"), setting, null);
            }
        },
        getTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle";
            }
        },
        // zTreeBeforeAsync: function () {
        // return bflag;
        // },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            if (size <= 100 && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            mainObj.getCharSelect(treeObj);
        },
        zTreeOnNodeCreated: function (event, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(treeId);
            var id = treeNode.id.toString();
            var list = [];
            if (zTreeIdJson[id] == undefined || zTreeIdJson[id] == null) {
                list = [treeNode.tId];
                zTreeIdJson[id] = list;
            } else {
                zTreeIdJson[id].push(treeNode.tId)
            }
        },
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(treeId);
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { // 若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj(treeId), nodes = zTree
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

                    // 存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                    var ns = [];
                    // 节点id
                    var nodeId;
                    for (var i = 0; i < nodes.length; i++) {
                        nodeId = nodes[i].id;
                        if (nodes[i].type == "people" || nodes[i].type == "vehicle") {
                            // 查询该节点是否在勾选组织或分组下，若在则不记录，不在则记录
                            var nd = zTree.getNodeByParam("tId", nodes[i].tId, treeNode);
                            if (nd == null && $.inArray(nodeId, ns) == -1) {
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength += ns.length;
                } else if (treeNode.type == "people" || treeNode.type == "vehicle") { // 若勾选的为监控对象
                    var zTree = $.fn.zTree.getZTreeObj(treeId), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;
                    // 存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                    var ns = [];
                    // 节点id
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
                // 若组织节点已被勾选，则是勾选操作，改变勾选操作标识
                if (treeNode.type == "group" && !treeNode.checked) {
                    checkFlag = true;
                }
            }
            return flag;
        },
        zTreeOnExpand: function (event, treeId, treeNode) {
            //判断是否是勾选操作展开的树(是则继续执行，不是则返回)
            if (treeNode.type == "group" && !checkFlag) {
                return;
            }
            //初始化勾选操作判断表示
            checkFlag = false;
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
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
        getGroupChild: function (node, assign) { // 递归获取组织及下级组织的分组节点
            var nodes = node.children;
            if (nodes != null && nodes != undefined && nodes.length > 0) {
                for (var i = 0; i < nodes.length; i++) {
                    var node = nodes[i];
                    if (node.type == "assignment") {
                        assign.push(node);
                    } else if (node.type == "group" && node.children != undefined) {
                        mainObj.getGroupChild(node.children, assign);
                    }
                }
            }
        },
        getCharSelect: function (treeObj) {
            var treeId = treeObj.setting.treeId;
            var groupSelectId = '#groupSelect';
            if (treeId.indexOf('2') > -1) {
                groupSelectId = groupSelectId + '2';
            } else if (treeId.indexOf('3') > -1) {
                groupSelectId = groupSelectId + '3';
            }
            var nodes = treeObj.getCheckedNodes(true);
            var allNodes = treeObj.getNodes();
            if (nodes.length > 0) {
                $(groupSelectId).val(allNodes[0].name);
                if (treeId == 'treeDemo3') {
                    $(groupSelectId).data('groupids', nodes.filter(function (ele) {
                        return ele.type == 'user';
                    }).map(function (ele) {
                        if (treeId == 'treeDemo3') {
                            if (ele.type == 'user') {
                                return ele.id;
                            }
                        } else {
                            return ele.uuid;
                        }

                    }).join(','))
                } else {
                    $(groupSelectId).data('groupids', nodes.map(function (ele) {
                        return ele.uuid;
                    }).join(','))
                }

            } else {
                $(groupSelectId).val("");
            }
        },
        tableFilter: function () {
            // 显示隐藏列
            var menu_text = "";
            var table = $("#dataTable3 tr th:gt(0)");
            for (var i = 0; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            ;
            $("#Ul-menu-text3").html(menu_text);
        },
        tableFilter2: function () {
            // 显示隐藏列
            var menu_text = "";
            var table = $("#dataTable2 tr th:gt(0)");
            for (var i = 0; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            ;
            $("#Ul-menu-text2").html(menu_text);
        },
        // 开始时间
        startDay: function (day) {
            var timeInterval = $('#timeInterval').val().split('--');
            var startValue = timeInterval[0];
            var endValue = timeInterval[1];
            if (startValue == "" || endValue == "") {
                var today = new Date();
                var targetday_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * day;
                today.setTime(targetday_milliseconds); // 注意，这行是关键代码
                var tYear = today.getFullYear();
                var tMonth = today.getMonth();
                var tDate = today.getDate();
                tMonth = mainObj.doHandleMonth(tMonth + 1);
                tDate = mainObj.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " "
                    + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24
                    * parseInt(num);
                today.setTime(end_milliseconds); // 注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = mainObj.doHandleMonth(endMonth + 1);
                endDate = mainObj.doHandleMonth(endDate);
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
                vMonth = mainObj.doHandleMonth(vMonth + 1);
                vDate = mainObj.doHandleMonth(vDate);
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
                    vendMonth = mainObj.doHandleMonth(vendMonth + 1);
                    vendDate = mainObj.doHandleMonth(vendDate);
                    endTime = vendYear + "-" + vendMonth + "-" + vendDate + " "
                        + "23:59:59";
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
        // 当前时间
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
        // 组织树预处理加载函数
        ajaxDataFilter: function (treeId, parentNode, responseData) {
// if(treeId == 'treeDemo3'){
// var treeObj = $.fn.zTree.getZTreeObj(treeId);
// if (responseData.msg) {
// var obj = JSON.parse(ungzip(responseData.msg));
// var data;
// if (obj.tree != null && obj.tree != undefined) {
// data = obj.tree;
// size = obj.size;
// }else{
// data = obj
// }
// for (var i = 0; i < data.length; i++) {
// data[i].open = true;
// }
// }
// return data;
// }
// size = responseData.length;
// return responseData;
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
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
        onCheckVehicle: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(treeId);
            // 若为取消勾选则不展开节点
            if (treeNode.checked) {
                if (treeId === 'treeDemo2') {
                    isSearch2 = false;
                }
                if (treeId === 'treeDemo3') {
                    isSearch3 = false;
                }
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    mainObj.getCheckedNodes();
                    mainObj.validatesOne();
                    mainObj.validatesTwo();
                }, 600)
            }
            mainObj.getCheckedNodes();
            mainObj.getCharSelect(zTree);
        },
        // 获取到选择的节点
        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo" + mainObj.getCurrentTabIndex().toString()),
                nodes = zTree.getCheckedNodes(true), v = "", vid = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == "vehicle") {
                    v += nodes[i].name + ",";
                    vid += nodes[i].id + ",";
                }
            }
            var index = mainObj.getCurrentTabIndex();
            if (index == 3) {
                allVid = vid;
                vnameList = v;
            } else {
                allVid2 = vid;
            }

        },
        inquireClick: function (num) { // 查询按钮的单击事件
            $(".mileage-Content,.ToolPanel").css("display", "block");
            $("#simpleQueryParam").val("");
            mainObj.getCheckedNodes();
            number = num;
            if (number == 0) {
                mainObj.getsTheCurrentTime();
            } else if (number == -1) {
                mainObj.startDay(-1)
            } else if (number == -3) {

                mainObj.startDay(-3)
            } else if (number == -7) {
                mainObj.startDay(-7)
            }
            if (number != 1) {
                $('#timeInterval').val(startTime + '--' + endTime);
                // 从页面获取到开始时间
                startime = startTime;
                // 从页面获取到结束时间
                endtime = endTime;
            } else {
                var timeInterval = $('#timeInterval').val().split('--');
                startime = timeInterval[0];
                endtime = timeInterval[1];
            }
            ;
            // 请求路径
            var url = "/clbs/cb/cbReportManagement/mileageReport/detailList";
            if (startime > endtime) {
                layer.msg(endtimeComStarttime, {move: false});
                return;
            }
            if (!mainObj.validatesOne()) {
                return;
            }
            // 请求参数
            var pdata = {"vehicleIds": allVid, "startTime": startime, "endTime": endtime, simpleQueryParam: $('#simpleQueryParam3').val()};
            // 发送请求给服务器
            json_ajax("POST", url, "json", true, pdata, mainObj.dataTableCB3);

        },
        dataTableCB1: function (data) {
            if (data.success == true) {
                // 组织表头
                var month = parseInt(currentMonth1.substr(5));
                var year = parseInt(currentMonth1.substr(0, 4));
                mainObj.renderTh('#dataTable', year, month);
                myTable.clear();
                mainObj.tableFilter();
                mainObj.getTable('#dataTable');
                mileageList = [];// 用来存储服务器返回的数据，以便于将数据加载到页面上

                if (data.obj.monthData != null && data.obj.monthData.length != 0) {
                    mileage = data.obj.monthData;
                    var s = 0;
                    for (var i = 0; i < mileage.length; i++) {
                        var item = mileage[i];
                        var list =
                            [
                                s++,
                                item.groupName,
                                second2Hour(item.dayOne),
                                second2Hour(item.dayTwo),
                                second2Hour(item.dayThree),
                                second2Hour(item.dayFour),
                                second2Hour(item.dayFive),
                                second2Hour(item.daySix),
                                second2Hour(item.daySeven),
                                second2Hour(item.dayEight),
                                second2Hour(item.dayNine),
                                second2Hour(item.dayTen),
                                second2Hour(item.dayEleven),
                                second2Hour(item.dayTwelve),
                                second2Hour(item.dayThirteen),
                                second2Hour(item.dayFourteen),
                                second2Hour(item.dayFifteen),
                                second2Hour(item.daySixteen),
                                second2Hour(item.daySeventeen),
                                second2Hour(item.dayEnghteen),
                                second2Hour(item.dayNineteen),
                                second2Hour(item.dayTwenty),
                                second2Hour(item.dayTwentyOne),
                                second2Hour(item.dayTwentyTwo),
                                second2Hour(item.dayTwentyThree),
                                second2Hour(item.dayTwentyFour),
                                second2Hour(item.dayTwentyFive),
                                second2Hour(item.dayTwentySix),
                                second2Hour(item.dayTwentySeven),
                                second2Hour(item.dayTwentyEnght),
                            ];
                        if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
                            list.push(second2Hour(item.dayTwentyNine));
                            list.push(second2Hour(item.dayThirty));
                            list.push(second2Hour(item.dayThirtyOne));
                        } else if (month == 2) {
                            if (year % 100 != 0) {
                                if (year % 4 == 0) {
                                    list.push(second2Hour(item.dayTwentyNine));
                                }
                            } else {
                                if (year % 400 == 0) {
                                    list.push(second2Hour(item.dayTwentyNine));
                                }
                            }
                        } else {
                            list.push(second2Hour(item.dayTwentyNine));
                            list.push(second2Hour(item.dayThirty));
                        }
                        list.push(second2Hour(item.sumNUmber));
                        mileageList.push(list); // 图表
                    }
                    mainObj.reloadData(mileageList);
                    $("#simpleQueryParam").val("");
                    $("#search_button").click();
                }
                else {
                    $("#simpleQueryParam").val("");
                    $("#search_button").click();
                    mainObj.reloadData(mileageList);
                }
            } else {
                layer.msg(data.msg || publicError, {move: false});
            }
        },
        validatesOne: function () {
            return $("#hourslist3").validate({
                rules: {
                    groupSelect3: {
                        zTreeChecked: "treeDemo3"
                    }
                },
                messages: {
                    groupSelect3: {
                        zTreeChecked: vehicleSelectBrand
                    }
                }
            }).form();
        },

        getTable: function (table) {
            $('.toggle-vis').prop('checked', 'true');
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
                "columnDefs": [{
                    "targets": [0, 2, 3, 4],
                    "searchable": false
                }],
                "order": [
                    [0, null]
                ],// 第一列排序图标改为默认

            });
            myTable.off('order.dt search.dt').on('order.dt search.dt', function () {
                myTable.column(0, {
                    search: 'applied',
                    order: 'applied'
                }).nodes().each(function (cell, i) {
                    cell.innerHTML = i + 1;
                });
            }).draw();
            $('.toggle-vis').off('change').on('change', function (e) {
                var column = myTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });
            $("#search_button").off('click').on("click", function () {
                var tsval = $("#simpleQueryParam").val();
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
        /** ************ tab2 start *************** */
        monthClick2: function (__i) {
            var toPostMonth = null;
            // 本月
            if (__i == 0) {
                var currentMonth = mainObj.getCurrentMonth();
                $('#select2').val(currentMonth);
                toPostMonth = currentMonth;
            }
            // 上一月
            if (__i == -1) {
                var currentMonth = $('#select2').val();
                var date = new Date(currentMonth);
                date.setMonth(date.getMonth() - 1);
                var str = mainObj.formatMonth(date);
                var options = $('#select2 option');
                var has = false;
                for (var i = 0; i < options.length; i++) {
                    if (options[i].value == str) {
                        has = true;
                        break;
                    }
                }
                if (!has) {
                    layer.msg('查询时间范围不超过12个月')
                    return;
                }
                $('#select2').val(str);
                toPostMonth = str;
            }
            // 查询
            if (__i == 1) {
                toPostMonth = $('#select2').val();
            }
            if (!mainObj.validatesTwo()) {
                return;
            }
            currentMonth2 = toPostMonth;
            // 请求路径
            var url = "/clbs/cb/cbReportManagement/mileageReport/days";
            var m = mainObj.getDateFromMonth(toPostMonth);
            mainObj.getCheckedNodes();
            // 请求参数
            var pdata = {"vehicleIds": allVid2, "month": m[0],simpleQueryParam: $('#simpleQueryParam2').val()};
            // 发送请求给服务器
            json_ajax("POST", url, "json", true, pdata, mainObj.dataTableCB2);
        },
        validatesTwo: function () {
            return $("#hourslist2").validate({
                rules: {
                    groupSelect2: {
                        zTreeChecked: "treeDemo2"
                    }
                },
                messages: {
                    groupSelect2: {
                        zTreeChecked: vehicleSelectBrand
                    }
                }
            }).form();
        },
        dataTableCB2: function (data) {
            $('#exportMileage2').removeAttr('disabled')
            if (data.success == true) {
                // 组织表头
                var month = parseInt(currentMonth2.substr(5));
                var year = parseInt(currentMonth2.substr(0, 4));
                mainObj.renderTh2('#dataTable2', year, month);
                myTable2.clear();
                mainObj.tableFilter2();
                mainObj.getTable2('#dataTable2');
                mileageList = [];// 用来存储服务器返回的数据，以便于将数据加载到页面上

                if (data.obj != null && data.obj.length != 0) {
                    mileage = data.obj;
                    var s = 0;
                    for (var i = 0; i < mileage.length; i++) {
                        var item = mileage[i];
                        var list =
                            [
                                s++,
                                item.vehicleBrandNumber,
                                item.vehicleBrandColor,
                                item.vehicleType == 'null' ? '其他车辆' : item.vehicleType,
                                item.enterpriseName,
                                second2Hour(item.days[0]),
                                second2Hour(item.days[1]),
                                second2Hour(item.days[2]),
                                second2Hour(item.days[3]),
                                second2Hour(item.days[4]),
                                second2Hour(item.days[5]),
                                second2Hour(item.days[6]),
                                second2Hour(item.days[7]),
                                second2Hour(item.days[8]),
                                second2Hour(item.days[9]),
                                second2Hour(item.days[10]),
                                second2Hour(item.days[11]),
                                second2Hour(item.days[12]),
                                second2Hour(item.days[13]),
                                second2Hour(item.days[14]),
                                second2Hour(item.days[15]),
                                second2Hour(item.days[16]),
                                second2Hour(item.days[17]),
                                second2Hour(item.days[18]),
                                second2Hour(item.days[19]),
                                second2Hour(item.days[20]),
                                second2Hour(item.days[21]),
                                second2Hour(item.days[22]),
                                second2Hour(item.days[23]),
                                second2Hour(item.days[24]),
                                second2Hour(item.days[25]),
                                second2Hour(item.days[26]),
                                second2Hour(item.days[27]),
                            ];
                        if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
                            list.push(second2Hour(item.days[28]));
                            list.push(second2Hour(item.days[29]));
                            list.push(second2Hour(item.days[30]));
                        } else if (month == 2) {
                            if (year % 100 != 0) {
                                if (year % 4 == 0) {
                                    list.push(second2Hour(item.days[28]));
                                }
                            } else {
                                if (year % 400 == 0) {
                                    list.push(second2Hour(item.days[28]));
                                }
                            }
                        } else {
                            list.push(second2Hour(item.days[28]));
                            list.push(second2Hour(item.days[29]));
                        }
                        list.push(second2Hour(item.monthReport));
                        mileageList.push(list); // 图表
                    }
                    mainObj.reloadData2(mileageList);
                    $("#simpleQueryParam2").val("");
                    $("#search_button2").click();
                }
                else {
                    $("#simpleQueryParam2").val("");
                    $("#search_button2").click();
                    mainObj.reloadData2(mileageList);
                    if(mileageList.length == 0 ){
                        $('#exportMileage2').prop('disabled', true)
                    }else{
                        $('#exportMileage2').prop('disabled', false)
                    }
                }
            } else {
                layer.msg(data.msg || publicError, {move: false});
            }
        },
        getTable2: function (table) {
            $('#Ul-menu-text2 .toggle-vis').prop('checked', 'true');
            myTable2 = $(table).DataTable({
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
                "columnDefs": [{
                    /* "targets": [0, 2, 3, 4,],*/
                    "searchable": false
                }],
                "order": [
                    [0, null]
                ],// 第一列排序图标改为默认

            });
            myTable2.off('order.dt search.dt').on('order.dt search.dt', function () {
                myTable2.column(0, {
                    search: 'applied',
                    order: 'applied'
                }).nodes().each(function (cell, i) {
                    cell.innerHTML = i + 1;
                });
            }).draw();
            $('#Ul-menu-text2 .toggle-vis').off('change').on('change', function (e) {
                var column = myTable2.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $("#keepOpen2").addClass("open");
            });
            $("#search_button2").off('click').on("click", function () {
                var tsval = $("#simpleQueryParam2").val();
                myTable2.columns(1).search(tsval, false, false).draw();
                if($('#dataTable2 .dataTables_empty').length > 0){
                    $('#exportMileage2').prop('disabled', true)
                }else{
                    $('#exportMileage2').prop('disabled', false)
                }
            });
        },
        reloadData2: function (dataList) {
            var currentPage = myTable2.page()
            myTable2.clear()
            myTable2.rows.add(dataList)
            myTable2.page(currentPage).draw(false);
        },
        /** ************ tab2 end *************** */

        /** ************ tab3 start *************** */
        dataTableCB3: function (data) {
            $('#exportMileage3').removeAttr('disabled');
            if (data.success == true) {
                // 组织表头
                mileageList = [];// 用来存储服务器返回的数据，以便于将数据加载到页面上

                if (data.obj != null && data.obj.length != 0) {
                    $("#exportMileage3").removeAttr("disabled");
                    mileage = data.obj;
                    var s = 0;
                    for (var i = 0; i < mileage.length; i++) {
                        var item = mileage[i];
                        var list =
                            [
                                s++,
                                item.vehicleBrandNumber,
                                item.vehicleBrandColor,
                                item.vehicleType == 'null' ? '其他车辆' : item.vehicleType,
                                item.groupName,
                                item.timeSection,
                                second2Hour(item.gpsMile)
                            ];
                        mileageList.push(list); // 图表
                    }
                    mainObj.reloadData3(mileageList);
                    $("#simpleQueryParam3").val("");
                    $("#search_button3").click();
                }
                else {
                    $("#simpleQueryParam3").val("");
                    $("#search_button3").click();
                    mainObj.reloadData3(mileageList);
                    $("#exportMileage3").attr("disabled", "disabled");
                }
            } else {
                layer.msg(data.msg || publicError, {move: false});
            }
        },
        getTable3: function (table) {
            $('#Ul-menu-text3 .toggle-vis').prop('checked', 'true');
            myTable3 = $(table).DataTable({
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
                "columnDefs": [{
                    "targets": [0, 2, 3, 4, 5, 6],
                    "searchable": false
                }],
                "order": [
                    [0, null]
                ],// 第一列排序图标改为默认

            });
            myTable3.off('order.dt search.dt').on('order.dt search.dt', function () {
                myTable3.column(0, {
                    search: 'applied',
                    order: 'applied'
                }).nodes().each(function (cell, i) {
                    cell.innerHTML = i + 1;
                });
            }).draw();
            $('#Ul-menu-text3 .toggle-vis').off('change').on('change', function (e) {
                var column = myTable3.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $("#keepOpen3").addClass("open");
            });
            $("#search_button3").off('click').on("click", function () {
                var tsval = $("#simpleQueryParam3").val();
                myTable3.search(tsval, false, false).draw();
                if($('#dataTable3 .dataTables_empty').length != 0){
                    $('#exportMileage3')[0].disabled = true
                }else {
                    $('#exportMileage3')[0].disabled = false
                }
            });
        },
        reloadData3: function (dataList) {
            var currentPage = myTable3.page()
            myTable3.clear()
            myTable3.rows.add(dataList)
            myTable3.page(currentPage).draw(false);
        },
        /** ************ tab3 end *************** */

        toHHMMSS: function (data) {
            var totalSeconds = data * 60 * 60;
            var hour = Math.floor(totalSeconds / 60 / 60);
            var minute = Math.floor(totalSeconds / 60 % 60);
            var second = Math.floor(totalSeconds % 60);
            return hour + "小时" + minute + "分钟" + second + "秒"
        },
        exportMileage: function () {
            var toPostMonth = $('#select1').val();
            // 请求路径
            var url = "/clbs/cb/cbReportManagement/userOnlineTime/exportGroupsOnlineTime";
            var m = mainObj.getDateFromMonth(toPostMonth);
            var groupid = $('#groupSelect').data('groupids');
            // 请求参数
            var pdata = {"groupId": groupid, "nowMonth": m[0], "nextMonth": m[1]};
            // 发送请求给服务器
            exportExcelUseForm(url, pdata);
        },
        exportMileage2: function () {
            var toPostMonth = $('#select2').val();
            var url = "/clbs/cb/cbReportManagement/mileageReport/export/month";
            var parameter = {vehicleIds: allVid2, month: $('#select2').val(), simpleQueryParam: $('#simpleQueryParam2').val()};;
            if(getRecordsNum('dataTable2_info') > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            json_ajax("POST", url, "json", true, parameter, function (data) {
                if (data.success) {
                    layer.confirm("已加入到导出队列,请注意查看导出管理消息提醒", {
                        icon: 3,
                        title: '操作确认',
                        btn: ['确定', '导出管理']
                    }, function () {
                        layer.closeAll();
                    }, function () {
                        layer.closeAll();
                        pagesNav.showExportManager();
                    });
                } else if (data.msg) {
                    layer.msg(data.msg);
                }
            });
        },
        exportMileage3: function () {
            var timeInterval = $('#timeInterval').val().split('--');
            var startime1 = timeInterval[0];
            var endtime1 = timeInterval[1];
            mainObj.getCheckedNodes();
            if (allVid == null || allVid == "") {
                layer.msg(monitoringObjecNull);
                return;
            }
            var parameter = {vehicleIds: allVid, startTime: startime1, endTime: endtime1, simpleQueryParam: $('#simpleQueryParam3').val()};
            var url = "/clbs/cb/cbReportManagement/mileageReport/export/detail";
            if(getRecordsNum('dataTable3_info') > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            json_ajax("POST", url, "json", true, parameter, function (data) {
                if (data.success) {
                    layer.confirm("已加入到导出队列,请注意查看导出管理消息提醒", {
                        icon: 3,
                        title: '操作确认',
                        btn: ['确定', '导出管理']
                    }, function () {
                        layer.closeAll();
                    }, function () {
                        layer.closeAll();
                        pagesNav.showExportManager();
                    });
                } else if (data.msg) {
                    layer.msg(data.msg);
                }
            });
        },
        CallBackExport: function (data) {
            if (data == true) {
                var url = "/clbs/m/reportManagement/mainObj/export"
                window.location.href = url;
            }
            else {
                layer.msg(exportFail, {move: false});
                return;
            }
        },
        // 刷新列表
        refreshTable: function () {
            $('#simpleQueryParam3').val('')
            mainObj.inquireClick(1, false);
        },
        // 刷新列表
        refreshTableMonth1: function () {
            mainObj.monthClick(1);
        },
        refreshTableMonth2: function () {
            $('#simpleQueryParam2').val('')
            mainObj.monthClick2(1);
        },
        //模糊查询树
        searchVehicleTree2: function (param) {

            ifAllCheck = false;//模糊查询不自动勾选

            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                // $.fn.zTree.init($("#treeDemo"), setting, null);
                mainObj.init("treeDemo2")
            } else {
                bflag = true;
                var querySetting = {
                    async: {
                        url: "/clbs/a/search/reportFuzzySearch",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"type": $('#queryType1').val(), "queryParam": param},
                        dataFilter: mainObj.ajaxQueryDataFilter
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
                        beforeClick: mainObj.beforeClickVehicle,
                        onCheck: mainObj.onCheckVehicle,
                        // beforeCheck: mainObj.zTreeBeforeCheck,
                        onExpand: mainObj.zTreeOnExpand,
                        //beforeAsync: mainObj.zTreeBeforeAsync,
                        // onAsyncSuccess: mainObj.zTreeOnAsyncSuccess,
                        onNodeCreated: mainObj.zTreeOnNodeCreated
                    }
                };
                $.fn.zTree.init($("#treeDemo2"), querySetting, null);
            }
        },
        searchVehicleTree3: function (param) {

            ifAllCheck = false;//模糊查询不自动勾选

            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                // $.fn.zTree.init($("#treeDemo"), setting, null);
                mainObj.init("treeDemo3")
            } else {
                bflag = true;
                var querySetting = {
                    async: {
                        url: "/clbs/a/search/reportFuzzySearch",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"type": $('#queryType').val(), "queryParam": param},
                        dataFilter: mainObj.ajaxQueryDataFilter
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
                        beforeClick: mainObj.beforeClickVehicle,
                        onCheck: mainObj.onCheckVehicle,
                        // beforeCheck: mainObj.zTreeBeforeCheck,
                        onExpand: mainObj.zTreeOnExpand,
                        //beforeAsync: mainObj.zTreeBeforeAsync,
                        // onAsyncSuccess: mainObj.zTreeOnAsyncSuccess,
                        onNodeCreated: mainObj.zTreeOnNodeCreated
                    }
                };
                $.fn.zTree.init($("#treeDemo3"), querySetting, null);
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
    }
    $(function () {														// 初始化页面
        mainObj.init();
        $('#timeInterval').dateRangePicker({
            dateLimit: 31
        });
// mainObj.getTable('#dataTable');
        mainObj.tableFilter();
        mainObj.tableFilter2();
        mainObj.getTable2('#dataTable2');
        mainObj.getTable3('#dataTable3');
        mainObj.getsTheCurrentTime();
        $("#groupSelect2,#groupSelect3").bind("click", showMenuContent);			// 组织下拉显示
// $("#exportMileage").bind("click",mainObj.exportMileage);
        $("#exportMileage2").bind("click", mainObj.exportMileage2);
        $("#exportMileage3").bind("click", mainObj.exportMileage3);
        $(window).resize(mainObj.windowResize);										// 监听窗口变化
        // 重新加载图表
// $("#toggle-left").bind("click", function () { //左侧菜单切换重新绘制图表
// setTimeout(function () {
// onlineReport.MileageGraphics();
// }, 500)
// });
        $("#refreshTable").bind("click", mainObj.refreshTable);
        $("#refreshTableMonth").bind("click", mainObj.refreshTableMonth1);
        $("#refreshTableMonth2").bind("click", mainObj.refreshTableMonth2);
        $('#simpleQueryParam3').bind('keyup', function (event) {
            if (event.keyCode == "13") {
                //回车执行查询
                $('#search_button3').click();
            }
        });
        $('#simpleQueryParam2').bind('keyup', function (event) {
            if (event.keyCode == "13") {
                //回车执行查询
                $('#search_button2').click();
            }
        });

        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect2') {
                var param = $("#groupSelect2").val();
                mainObj.searchVehicleTree2(param);
            }
            ;
            if (id == 'groupSelect3') {
                var param = $("#groupSelect3").val();
                mainObj.searchVehicleTree3(param);
            }
            ;
        });

        /**
         * 监控对象树模糊查询
         */
        var inputChange2;
        $("#groupSelect2").on('input propertychange', function (value) {
            if (inputChange2 !== undefined) {
                clearTimeout(inputChange2);
            }
            ;
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                isSearch2 = true;
            }
            ;
            inputChange2 = setTimeout(function () {
                if (isSearch2) {
                    var param = $("#groupSelect2").val();
                    mainObj.searchVehicleTree2(param);
                }
                isSearch2 = true;
            }, 500);
        });
        var inputChange3;
        $("#groupSelect3").on('input oninput', function (value) {
            if (inputChange3 !== undefined) {
                clearTimeout(inputChange3);
            }
            ;
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                isSearch3 = true;
            }
            ;
            inputChange3 = setTimeout(function () {
                if (isSearch3) {
                    var param = $("#groupSelect3").val();
                    mainObj.searchVehicleTree3(param);
                }
                isSearch3 = true;
            }, 500);
        });
        $('#queryType').on('change', function () {
            var param = $("#groupSelect3").val();
            mainObj.searchVehicleTree3(param);
        });
        $('#queryType1').on('change', function () {
            var param = $("#groupSelect2").val();
            mainObj.searchVehicleTree2(param);
        });

    })
}(window, $))