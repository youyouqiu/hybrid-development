(function (window, $) {
    var oldAlarmType;
    var oldStatusValue;
    var oldSalarmStartTime;
    var oldAlarmEndTime;
    var checked = true;
    var myTable;
    var oldVehicleList;
    var startTime, endTime;//开始时间，结束时间
    var vehicleList = [];
    var setChar; // 树设置
    var checkFlag = false; //判断组织节点是否是勾选操作
    var zTreeIdJson = {};
    var size;//当前权限监控对象数量
    var typePos = '';
    var checkExpand = false;//判断是否展开过车辆树（只用于全局报警、实时监控判断）
    var listSize = -1;//记录有多少个分组需要展开（只用于全局报警、实时监控）
    var typeMsg = $("#groupSelect").val();
    switchSignalAlarm = {
        //组织树
        init: function () {
            //组织树
            setChar = {
                async: {
                    url: switchSignalAlarm.getIcoTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    sync: false,
                    otherParam: {"type": "multiple"},
                    dataFilter: switchSignalAlarm.ajaxDataFilter
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
                    fontCss: setFontCss_ztree,
                    countClass: "group-number-statistics"
                },
                data: {
                    simpleData: {
                        enable: true,
                    }
                },
                callback: {
                    beforeClick: switchSignalAlarm.beforeClickVehicle,
                    onAsyncSuccess: switchSignalAlarm.zTreeOnAsyncSuccess,
                    beforeCheck: switchSignalAlarm.zTreeBeforeCheck,
                    onCheck: switchSignalAlarm.onCheckVehicle,
                    onExpand: switchSignalAlarm.zTreeOnExpand,
                    onNodeCreated: switchSignalAlarm.zTreeOnNodeCreated
                }
            };
            $.fn.zTree.init($("#treeDemo"), setChar, null);
        },
        getIcoTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/functionconfig/fence/bindfence/alarmSearchTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=monitor";
            }
        },
        //报警类型树结构
        alarmTreeInit: function () {
            //组织树
            setChar = {
                async: {
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    sync: false,
                    otherParam: {"type": "multiple"},
                    dataFilter: switchSignalAlarm.ajaxDataFilter
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
                    showIcon:false,
                    dblClickExpand: false,
                    nameIsHTML: true,
                    fontCss: setFontCss_ztree,
                    countClass: "group-number-statistics"
                },
                data: {
                    simpleData: {
                        enable: true,
                        idKey:'id',
                        pidKey:'pId',
                    },
                    key:{
                        name:'name'
                    }
                },
                callback: {
                    onCheck: switchSignalAlarm.onCheckType,
                    onExpand: switchSignalAlarm.zTreeOnExpand,
                    onClick: switchSignalAlarm.onClickBack
                }
            };
            var data=JSON.parse($("#alarmTypeName").val());
            for(var i=0;i<data.tree.length;i++){
                if(data.tree[i].isCondition){
                    typePos += data.tree[i].name+",";
                }
            }
            if(typePos.length > 0){
                typePos = typePos.substring(0,typePos.length-1);
            }
            data.tree.push({name: "全部", isParent:true,id:0});
            $.fn.zTree.init($("#treeTypeDemo"), setChar, data.tree);
            var treeObj = $.fn.zTree.getZTreeObj("treeTypeDemo");  //得到该tree
            treeObj.checkAllNodes(true);
            var node = treeObj.getNodeByTId("treeTypeDemo_1");  //选中第一个节点
            treeObj.expandNode(node, true, false, true);  //打开节点
        },
        onClickBack: function (e, treeId, treeNode, clickFlag) {
            var zTreeObj = $.fn.zTree.getZTreeObj("treeTypeDemo");
            zTreeObj.checkNode(treeNode, !treeNode.checked, true);
            switchSignalAlarm.onCheckType(e, treeId, treeNode);
        },
        onCheckType: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeTypeDemo");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
            }
            switchSignalAlarm.getTypeSelect(zTree);
            switchSignalAlarm.getTypeCheckedNodes();
        },
        getTypeSelect: function (treeObj) {
            var nodes = treeObj.getCheckedNodes(true);
            var allNodes = treeObj.getNodes();
            if (nodes.length > 0) {
                $("#groupSelect").val(allNodes[0].name);
            } else {
                $("#groupSelect").val("");
            }
        },
        getTypeCheckedNodes: function () {
            typePos = '';
            var typezTree = $.fn.zTree.getZTreeObj("treeTypeDemo"),
                nodes = typezTree.getCheckedNodes(true),
                v = "", typeMsg = "";
            var noCheckLen = typezTree.getCheckedNodes(false).length;
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].id != "" && nodes[i].id != undefined && nodes[i].id != null) {
                    if(nodes[i].isCondition){
                        v += nodes[i].name + ",";
                        typeMsg += nodes[i].name + ",";
                    }
                }
            }
            if(typeMsg.length > 0){
                typeMsg = typeMsg.substring(0,v.length-1);
            }
            if(v.length > 0){
                v = v.substring(0,v.length-1);
            }
            typePos = v;

            if (noCheckLen != 0) {
                typePos = typePos.replace("-1,", '');
                typeMsg = typeMsg.replace("全部,", '');
            }else{
                typeMsg = "全部";
            }
            $("#groupSelect").val(typeMsg);
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
                    if (data[i].type == "group") {
                        data[i].open = true;
                    }
                    if (data[i].count == '0') {
                        data[i].isParent = false;
                    }
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
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            var type = $("#atype").val();
            if (type != "" && (type == 0 || type == 2) && !checkExpand) { //若没有进行树展开操作则执行（只用于全局报警、实时监控）
                switchSignalAlarm.findTree();
                checkExpand = true;
            }
            var avid = $("#vehicleId").val();
            if ((size <= 5000 || listSize == 0) && avid != undefined && avid != "") {
                var alarmVid = avid.split(",");
                for (var j = 0; j < alarmVid.length; j++) {
                    var node = treeObj.getNodesByParam("id", alarmVid[j], null);
                    for (var i = 0; i < node.length; i++) {
                        if (node != undefined && node.length > 0) {
                            treeObj.checkNode(node[i], true, true);
                        }
                    }

                }
                $("#status").val(-1);
                switchSignalAlarm.inquireClick();
            }
            listSize--;

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
                    "monitorType": "monitor"
                }, function (data) {
                    var result = data.obj;
                    if (result != null && result != undefined) {
                        $.each(result, function (i) {
                            var pid = i; //获取键值
                            var chNodes = result[i] //获取对应的value
                            var parentTid = zTreeIdJson[pid][0];
                            var parentNode = treeObj.getNodeByTId(parentTid);
                            if (parentNode.children === undefined) {
                                treeObj.addNodes(parentNode, chNodes);
                            }
                        });
                    }
                })
            }
            if (vehicleList.length > 0) {
                switchSignalAlarm.checkCurrentNodes(treeNode);
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
                        switchSignalAlarm.getGroupChild(node, assign);
                    }
                }
            }
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;

                    json_ajax("post", "/clbs/a/search/getMonitorNum",
                        "json", false, {"id": treeNode.id, "type": treeNode.type}, function (data) {
                            if (data.success) {
                                nodesLength += data.obj;
                            } else {
                                layer.msg("限制校验异常！");
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

                if (nodesLength > 5000) {
                    layer.msg("最多勾选5000个监控对象！");
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
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
            }
            switchSignalAlarm.vehicleListId(); // 记录勾选的节点
        },

        zTreeOnNodeCreated: function (event, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            var id = treeNode.id.toString();
            var list = [];
            if (zTreeIdJson[id] == undefined || zTreeIdJson[id] == null) {
                list = [treeNode.tId];
                zTreeIdJson[id] = list;
            } else {
                zTreeIdJson[id].push(treeNode.tId)
            }
        },
        //模糊查询
        inputTextAutoSearch: function () {
            search_ztree('treeDemo', 'search_condition', 'vehicle');
        },
        findTree: function () {
            var assignIds = $("#assignIds").val();
            var test = assignIds.split(",");
            test.splice(test.length - 1, 1); //删除最后一个空元素
            var list = switchSignalAlarm.sortList(test);
            listSize = list.length;
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            for (var i = 0; i < list.length; i++) {
                var node = treeObj.getNodeByParam("id", list[i]);
                if (node != null) {
                    treeObj.expandNode(node, true, false);//指定选中ID节点展开
                } else {
                    listSize--;
                }
            }
        },
        sortList: function (data) {
            var res = [];
            var json = {};
            for (var i = 0; i < data.length; i++) {
                if (!json[data[i]]) {
                    res.push(data[i]);
                    json[data[i]] = 1;
                }
            }
            return res;
        },
        /**              
          * 时间戳转换日期              
          * @param <int> unixTime    待时间戳(秒)              
          * @param <bool> isFull    返回完整时间(Y-m-d 或者 Y-m-d H:i:s)              
          * @param <int>  timeZone   时区              
          */
        UnixToDate: function (unixTime, isFull, timeZone) {
            if (unixTime == 0 || unixTime == null) {
                return "";
            }
            if (typeof (timeZone) == 'number') {
                unixTime = parseInt(unixTime) + parseInt(timeZone) * 60 * 60;
            }
            var time = new Date(unixTime * 1000);
            var ymdhis = "";
            ymdhis += time.getFullYear() + "-";
            ymdhis += (time.getMonth() + 1) < 10 ? ("0" + (time.getMonth() + 1) + "-") : ((time.getMonth() + 1) + "-");
            ymdhis += time.getDate() < 10 ? ("0" + time.getDate()) : (time.getDate());
            ;
            if (isFull === true) {
                ymdhis += " " + (time.getHours() < 10 ? ("0" + time.getHours()) : time.getHours()) + ":";
                ymdhis += (time.getMinutes() < 10 ? ("0" + time.getMinutes()) : time.getMinutes()) + ":";
                ymdhis += (time.getSeconds() < 10 ? ("0" + time.getSeconds()) : time.getSeconds());
            }
            return ymdhis;
        },
        validates: function () {
            return $("#alarmForm").validate({
                rules: {
                    endTime: {
                        required: true,
                        compareDate: "#timeInterval"
                    },
                    startTime: {
                        required: true
                    }
                },
                messages: {
                    endTime: {
                        required: "请选择结束日期！",
                        compareDate: "结束日期必须大于开始日期!"
                    },
                    startTime: {
                        required: "请选择开始日期！",
                    }
                }
            }).form();
        },
        vehicleListId: function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            var nodes = zTree.getCheckedNodes(true);
            if (nodes.length == 0) {
                checked = true;
            }
            v = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == "vehicle" || nodes[i].type == "people" ||nodes[i].type == "thing" ) {
                    checked = false;
                    v += nodes[i].id + ",";
                }
            }
            vehicleList = v;
        },
        alarmDataTable: function () {
            var url = "/clbs/v/switchSignalAlarm/list";
            var parameter = {
                "alarmTypeNames": typePos,
                "status": statusValue,
                "startTime": alarmStartTime,
                "endTime": alarmEndTime,
                "vehicleIds": vehicleList
            };
            json_ajax("POST", url, "json", true, parameter, function (data) {
                if (data.success == true) {
                    myTable.refresh();
                   // myTable.requestData();
                }
            });
        },
        inquireClick: function () {
            switchSignalAlarm.vehicleListId();
            if (checked) {
                layer.msg("请选择监控对象！");
                return;
            }
            if (!switchSignalAlarm.validates()) {
                return;
            }
            ;
            if (typePos == '' || typePos == null) {
                layer.msg("请选择报警类型！");
                return;
            }
            statusValue = $('#status').val();
            var timeInterval = $('#timeInterval').val().split('--');
            alarmStartTime = timeInterval[0];
            alarmEndTime = timeInterval[1];
            //获取查询前参数
            oldAlarmType = typePos;
            oldStatusValue = statusValue;
            oldSalarmStartTime = alarmStartTime;
            oldAlarmEndTime = alarmEndTime;
            oldVehicleList = vehicleList;
            //发送查询请求
            var url = "/clbs/v/switchSignalAlarm/list";
            var parameter = {
                "alarmTypeNames": typePos,
                "status": statusValue,
                "startTime": alarmStartTime,
                "endTime": alarmEndTime,
                "vehicleIds": vehicleList
            };
            json_ajax("POST", url, "json", true, parameter, switchSignalAlarm.getCallback);

        },
        exportAlarm: function () {
            var length = $("#dataTable tbody tr").find("td").length;
            if (length > 1) {
                if(getRecordsNum() > 60000){
                    return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
                }
                //layer.msg('正在处理导出数据,请耐心等待', {icon: 16, time: false, shade: [0.1, true], skin: "layui-layer-border layui-layer-hui"});
                var url = "/clbs/v/switchSignalAlarm/export";
                //switchSignalAlarm.getStatue(url);
                window.location.href = url;
            }
        },
        getStatue: function (url) {
            $.ajax({
                type: "POST",
                url: url,
                dataType: "json",
                async: true,
                success: function (data) {
                    if (data.success == true) {
                        layer.closeAll();
                    } else {
                        setTimeout(function () {
                            switchSignalAlarm.getStatue(url);
                        }, 1000);
                    }
                }
            });
        },
        getCallback: function (data) {
            if (data.success == true) {
                $("#alarmExport").removeAttr("disabled");

                var columnDefs = [{
                    //第一列，用来显示序号
                    "searchable": false,
                    "orderable": false,
                    "targets": 0
                }];
                var columns = [{
                    //第一列，用来显示序号
                    "data": null,
                    "class": "text-center"
                }, {
                    "data": "plateNumber",
                    "class": "text-center"
                }, {
                    "data": "plateColor",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return getPlateColor(data);
                    }
                },{
                    "data": "name",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == null || data == undefined) {
                            return '';
                        }
                        else {
                            return data;
                        }
                    }
                }, {
                    "data": "assignmentName",
                    "class": "text-center"
                }, {
                    "data": "professionalsName",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == null || data == undefined) {
                            return ''
                        }
                        else {
                            return data;
                        }
                    }
                }, {
                    "data": "status",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        var str = "-";
                        if (row.professionalsName != null) {
                            str = row.professionalsName;
                        }
                        var dataString = "" + row.startTime + "|" + row.alarmType + "|" + row.description + "|" + row.id + "|" + row.plateNumber + "|" + row.swiftNumber + "|" + row.monitorType + "|" + row.vehicleId + "|" + str + "|" + row.assignmentName + "|" + row.alarmSource + "|" + row.alarmEndTime + "";
                        if (row.status == 0) {
                            return '<a onclick="switchSignalAlarm.warningManage(\'' + dataString + '\')">未处理</a>'
                        } else {
                            return "已处理";
                        }
                    }
                },{
                    "data": "description",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == null || data == undefined) {
                            return '';
                        }
                        else {
                            return data;
                        }
                    }
                },{
                    "data": "speed",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        data = switchSignalAlarm.fiterNumber(data);
                        return data;
                    }
                },{
                    "data": "startTime",
                    "class": "text-center",
                },{
                    "data": "endTime",
                    "class": "text-center",
                },{
                    "data": "alarmStartLocation",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            var str = data.split(",");
                            var coord = str[0];
                            var indexOf = coord.indexOf(".") + 4;
                            var startLong = coord.substring(0, indexOf);
                            return startLong;
                        } else {
                            return "-";
                        }
                    }

                }, {
                    "data": "alarmStartLocation",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            var str = data.split(",");
                            var coord = str[1];
                            var indexOf = coord.indexOf(".") + 4;
                            var startLong = coord.substring(0, indexOf);
                            return startLong;
                        } else {
                            return "-";
                        }
                    }

                }, {
                    "data": "alarmEndLocation",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            var str = data.split(",");
                            var coord = str[0];
                            var indexOf = coord.indexOf(".") + 4;
                            var startLong = coord.substring(0, indexOf);
                            return startLong;
                        } else {
                            return "-";
                        }
                    }

                }, {
                    "data": "alarmEndLocation",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            var str = data.split(",");
                            var coord = str[1];
                            var indexOf = coord.indexOf(".") + 4;
                            var startLong = coord.substring(0, indexOf);
                            return startLong;
                        } else {
                            return "-";
                        }
                    }

                }, {
                    "data": "alarmStartSpecificLocation",
                    "class": "text-center"
                }, {
                    "data": "alarmEndSpecificLocation",
                    "class": "text-center"
                }, {
                    "data": "personName",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == null || data == undefined) {
                            return ''
                        }
                        else {
                            return data;
                        }
                    }
                }, {
                    "data": "handleTime",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == null || data == undefined || row.status == "0") {
                            return ''
                        }
                        else {
                            return data;
                        }
                    }
                }, {
                    "data": "handleType",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == null || data == undefined) {
                            return ''
                        }
                        else {
                            return data;
                        }
                    }
                }, {
                    "data": "remark",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == null || data == undefined) {
                            return ''
                        }
                        else {
                            return data;
                        }
                    }
                }];
                //表格setting
                var url = "/clbs/v/switchSignalAlarm/getIoAlarmList";
                var setting = {
                    listUrl: url,
                    columnDefs: columnDefs, //表格列定义
                    columns: columns, //表格列
                    dataTableDiv: 'dataTable', //表格
                    pageable: true, //是否分页
                    showIndexColumn: true, //是否显示第一列的索引列
                    enabledChange: true,
                    getAddress: false,//是否逆地理编码
                    address_index: 16
                };
                //创建表格
                myTable = new TG_Tabel.createNew(setting);
                myTable.init();
            }
        },

        updateState: function (data) {
            json_ajax("POST", "/clbs/v/switchSignalAlarm/updateState", "json", false, {"id": data}, function (result) {
                if (result.success) {
                    $("#" + data + "").parent().text("已处理");
                    $("#personName" + data + "").text(result.obj.msg.personName);
                    $("#handleTime" + data + "").text(result.obj.msg.handleTime);
                    $("#handleType" + data + "").text(result.obj.msg.handleType);
                }
            })
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
            var atime = $("#alarmTime").val();
            if (atime != undefined && atime != "") {
                startTime = atime;
            }
            $('#timeInterval').val(startTime + '--' + endTime);
        },
        //模糊查询
        inputTextAutoSearch: function () {
            search_ztree('treeDemo', 'search_condition', 'vehicle');
        },
        endTimeStyle: function () {
            var width = $(this).width();
            var offset = $(this).offset();
            var left = offset.left - (207 - width);
            $("#laydate_box").css("left", left + "px");
        },

        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData));
//         	switchSignalAlarm.vehicleListId();
            var list = [];
            if (vehicleList != null && vehicleList != undefined && vehicleList != "") {
                var str = (vehicleList.slice(vehicleList.length - 1) == ',') ? vehicleList.slice(0, -1) : vehicleList;
                list = str.split(",");
            }
            return filterQueryResult(responseData, list);
        },
        searchVehicleTree: function (param) {
            var setQueryChar = {
                async: {
                    url: "/clbs/a/search/monitorTreeFuzzy",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    sync: false,
                    otherParam: {"type": "multiple", "queryParam": param, "webType": "1"},
                    dataFilter: switchSignalAlarm.ajaxQueryDataFilter
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
                    fontCss: setFontCss_ztree,
                    countClass: "group-number-statistics"
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    beforeClick: switchSignalAlarm.beforeClickVehicle,
                    onAsyncSuccess: switchSignalAlarm.fuzzyZTreeOnAsyncSuccess,
                    //beforeCheck: switchSignalAlarm.fuzzyZTreeBeforeCheck,
                    onCheck: switchSignalAlarm.fuzzyOnCheckVehicle,
                    //onExpand: switchSignalAlarm.zTreeOnExpand,
                    //onNodeCreated: switchSignalAlarm.zTreeOnNodeCreated,
                }
            };
            $.fn.zTree.init($("#treeDemo"), setQueryChar, null);
            //fuzzyParam = param;
        },
        /**
         * 选中已选的节点
         */
        checkCurrentNodes: function (treeNode) {
            if (vehicleList.length != 0) {
                var crrentSubV = vehicleList.split(",");
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                if (treeNode != undefined && treeNode != null && treeNode.type === "assignment" && treeNode.children != undefined) {
                    var list = treeNode.children;
                    if (list != null && list.length > 0) {
                        for (var j = 0; j < list.length; j++) {
                            var znode = list[j];
                            if (crrentSubV != null && crrentSubV != undefined && crrentSubV.length !== 0 && $.inArray(znode.id, crrentSubV) != -1) {
                                treeObj.checkNode(znode, true, true);
                            }
                        }
                    }
                }
            }
        },
        fuzzyZTreeOnAsyncSuccess: function (event, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.expandAll(true);
        },
        fuzzyZTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                    .getCheckedNodes(true), v = "";
                var nodesLength = 0;
                for (var i = 0; i < nodes.length; i++) {
                    if (nodes[i].type == "people" || nodes[i].type == "vehicle") {
                        nodesLength += 1;
                    }
                }
                if (treeNode.type == "group" || treeNode.type == "assignment") { // 判断若勾选节点数大于5000，提示
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo")
                    json_ajax("post", "/clbs/v/switchSignalAlarm/monitorTreeFuzzyCount",
                        "json", false, {"type": "multiple", "queryParam": fuzzyParam}, function (data) {
                            nodesLength += data;
                        })
                } else if (treeNode.type == "people" || treeNode.type == "vehicle") {
                    nodesLength += 1;
                }
                if (nodesLength > 5000) {
                    layer.msg("最多勾选5000个监控对象！");
                    flag = false;
                }
            }
            return flag;
        },
        fuzzyOnCheckVehicle: function (e, treeId, treeNode) {
            //获取树结构
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            //获取勾选状态改变的节点
            var changeNodes = zTree.getChangeCheckedNodes();
            if (treeNode.checked) { //若是取消勾选事件则不触发5000判断
                var checkedNodes = zTree.getCheckedNodes(true);
                var nodesLength = 0;
                for (var i = 0; i < checkedNodes.length; i++) {
                    if (checkedNodes[i].type == "people" || checkedNodes[i].type == "vehicle") {
                        nodesLength += 1;
                    }
                }

                if (nodesLength > 5000) {
                    //zTree.checkNode(treeNode,false,true);
                    layer.msg("最多勾选5000个监控对象！");
                    for (var i = 0; i < changeNodes.length; i++) {
                        changeNodes[i].checked = false;
                        zTree.updateNode(changeNodes[i]);
                    }
                }
            }
            //获取勾选状态被改变的节点并改变其原来勾选状态（用于5000准确校验）
            for (var i = 0; i < changeNodes.length; i++) {
                changeNodes[i].checkedOld = changeNodes[i].checked;
            }
            switchSignalAlarm.vehicleListId(); // 记录勾选的节点
        },
        initSearch: function () {
            var type = $("#atype").val();
            if (type == "2" || type == "0") {
                $("#status").val(0);
                setTimeout("switchSignalAlarm.inquireClick()", 1500);
            }
        },
        // 以下报警处理功能
        warningManage: function (data) {
            $("#alarmRemark").val("");
            $("#smsTxt").val("");
            $("#time").val("");
            $('#warningManage').modal('show');
            $("#listeningContent,#takePicturesContent,#sendTextMessages,.listenFooter,.takePicturesFooter,.sendTextFooter").hide();
            var dataArray = data.split('|');
            var url = "/clbs/a/search/alarmDeal";
            var data = {"vid": dataArray[7], "type": 0};
            var warningType = "";
            var device = "";
            var sim = "";
            json_ajax("POST", url, "json", false, data, function (result) {
                if (result.success) {
                    warningType = result.obj.type;
                    device = result.obj.device;
                    sim = result.obj.sim;
                }
            });
            //io报警都可以下发短信和拍照
            if (warningType == "9" || warningType == "10" || warningType == "5" ) {
                //$("#warningHiden").removeAttr("style");
                $("#warningManageListening").hide();
                $("#warningManagePhoto").hide();
                $("#warningManageSend").hide();
                $("#sno").val("0");
            } else {
                //$("#warningHiden").attr("style", "text-align:center");
                $("#warningManageListening").show();
                $("#warningManagePhoto").show();
                $("#warningManageSend").show();
                $("#sno").val(dataArray[5]);
            }

            $("#warningCarName").text(dataArray[4]);
            $("#warningPeo").text(dataArray[8]);
            $("#warningGroup").text(dataArray[9]);
            $("#warningTime").text(dataArray[0]);
            $("#warningDescription").text(dataArray[2]);
            $("#vUuid").val(dataArray[7]);
            $("#simcard").val(sim);
            $("#device").val(device);
            $("#warningType").val(dataArray[1]);
            $('#eventId').val(dataArray[3]);
        },
        // 监听下发
        listenForAlarm: function () {
            if (switchSignalAlarm.listenValidate()) {
                // 为车id赋值
                var vehicleId = $("#vUuid").val();
                $("#vidforAlarmListen").val(vehicleId);
                $("#brandListen").val($("#warningCarName").text());
                $("#alarmListen").val($("#warningType").val());
                $("#startTimeListen").val($("#warningTime").text());

                $("#simcardListen").val($('#simcard').val());
                $("#deviceListen").val($("#device").val());
                $("#snoListen").val($("#sno").val());
                $("#handleTypeListen").val("监听");
                $("#descriptionListen").val($("#warningDescription").text());
                $("#remarkListen").val($("#alarmRemark").val());
                $("#goListeningForAlarm").attr("disabled", "disabled");
                $("#listeningAlarm").ajaxSubmit(function (data) {
                    if (JSON.parse(data).success) {
                        layer.msg(publicIssuedSuccess);
                        switchSignalAlarm.alarmDataTable();
                        $("#warningManage").modal('hide')
                    } else {
                        layer.msg(publicIssuedFailure);
                    }
                    $("#warningManage").modal('hide');
                    $("#goListeningForAlarm").removeAttr("disabled");
                });
            }
            $("#goListeningForAlarm").removeAttr("disabled");
        },
        listenValidate: function () {
            return $("#listeningAlarm").validate({
                rules: {
                    monitorPhone: {
                        isNewTel: true,
                        required: true
                    },
                },
                messages: {
                    monitorPhone: {
                        required: '请输入电话号码'
                    },
                }
            }).form();
        },
        takePhotoForAlarm: function () {
            if (switchSignalAlarm.photoValidateForAlarm()) {
                $("#vidforAlarm").val($("#vUuid").val());
                $("#alarmPhoto").val($("#warningType").val());
                $("#startTimePhoto").val($("#warningTime").text());
                $("#brandPhoto").val($("#warningCarName").text());
                $("#simcardPhoto").val($('#simcard').val());
                $("#devicePhoto").val($("#device").val());
                $("#snoPhoto").val($("#sno").val());
                $("#handleTypePhoto").val("拍照");

                $("#takePhotoForAlarm").ajaxSubmit(function (data) {
                    if (JSON.parse(data).success) {
                        layer.msg(publicIssuedSuccess);
                        switchSignalAlarm.alarmDataTable();
                        $("#warningManage").modal('hide')
                    } else {
                        layer.msg(publicIssuedFailure);
                    }
                    //switchSignalAlarm.alarmDataTable();
                    //myTable.refresh();
                    // $("#warningManage").modal('hide')
                });
            }
        },
        goTxtSendForAlarm: function () {
            // 为车id赋值
            $("#vidSendTxtForAlarm").val($("#vUuid").val());
            $("#brandTxt").val($("#warningCarName").text());
            $("#alarmTxt").val($("#warningType").val());
            $("#startTimeTxt").val($("#warningTime").text());

            $("#simcardTxt").val($('#simcard').val());
            $("#deviceTxt").val($("#device").val());
            $("#snoTxt").val($("#sno").val());
            $("#handleTypeTxt").val("下发短信");

            var smsTxt = $("#smsTxt").val();
            if(smsTxt == null || smsTxt.length == 0){
                switchSignalAlarm.showErrorMsg("下发内容不能为空","smsTxt");
                return;
            }
            if (smsTxt.length > 512) {
                layer.msg("下发内容不能超过512个字符");
                return;
            }
            $("#txtSendForAlarm").ajaxSubmit(function (data) {
                if (JSON.parse(data).success) {
                    layer.msg(publicIssuedSuccess);
                    switchSignalAlarm.alarmDataTable();
                    $("#warningManage").modal('hide')
                } else {
                    layer.msg(publicIssuedFailure);
                }
                //switchSignalAlarm.alarmDataTable();
                //myTable.refresh();
                $("#warningManage").modal('hide')
            });
        },
        handleAlarm: function (handleType) {
            var startTime = $("#warningTime").text();
            var plateNumber = $("#warningCarName").text();
            var vehicleId = $("#vUuid").val();
            var simcard = $("#simcard").val();
            var device = $("#device").val();
            var sno = $("#sno").val();
            var alarm = $("#warningType").val();
            var eventId = $("#eventId").val();
            var remark = $("#alarmRemark").val();
            var url = "/clbs/v/switchSignalAlarm/updateIOAlarm";
            var data = {
                'id': eventId,
                "vehicleId": vehicleId,
                "plateNumber": plateNumber,
                "alarm": alarm,
                "handleType": handleType,
                "startTime": startTime,
                "simcard": simcard,
                "device": device,
                "sno": sno,
                "remark":remark
            };
            json_ajax("POST", url, "json", false, data, null);
            // 报警处理完毕后，延迟3秒进行结果查询
            setTimeout(pagesNav.gethistoryno, 3000);
            switchSignalAlarm.alarmDataTable();
            //myTable.refresh();
            $("#warningManage").modal('hide');
        },
        showOrhide: function (data) {
            if (data == 0) {
                var displays = $('#takePicturesContent').css('display');
                $('#listeningContent').hide();
                $('.listenFooter').hide();
                $('#sendTextMessages').hide();
                $('.sendTextFooter').hide();
                if (displays == 'none') {
                    $('#takePicturesContent').show();
                    $('.takePicturesFooter').show();
                } else {
                    $('#takePicturesContent').hide();
                    $('.takePicturesFooter').hide();
                }
            } else if(data==1) {
                $('#listeningContent').hide();
                $('.listenFooter').hide();
                $('#takePicturesContent').hide();
                $('.takePicturesFooter').hide();
                var display = $('#sendTextMessages').css('display');
                if (display == 'none') {
                    $('#sendTextMessages').show();
                    $('.sendTextFooter').show();
                } else {
                    $('#sendTextMessages').hide();
                    $('.sendTextFooter').hide();
                }
            }else {
                $('#takePicturesContent').hide();
                $('.takePicturesFooter').hide();
                $('#sendTextMessages').hide();
                $('.sendTextFooter').hide();
                var display = $('#listeningContent').css('display');
                if (display == 'none') {
                    $('#listeningContent').show();
                    $('.listenFooter').show();
                } else {
                    $('#listeningContent').hide();
                    $('.listenFooter').hide();
                }
            }
        },
        photoValidateForAlarm: function () {
            return $("#takePhotoForAlarm").validate({
                rules: {
                    wayID: {
                        required: true
                    },
                    time: {
                        required: true,
                        digits: true,
                        range: [0, 65535]
                    },
                    command: {
                        range: [0, 10],
                        required: true
                    },
                    saveSign: {
                        required: true
                    },
                    distinguishability: {
                        required: true
                    },
                    quality: {
                        range: [1, 10],
                        required: true
                    },
                    luminance: {
                        range: [0, 255],
                        required: true
                    },
                    contrast: {
                        range: [0, 127],
                        required: true
                    },
                    saturability: {
                        range: [0, 127],
                        required: true
                    },
                    chroma: {
                        range: [0, 255],
                        required: true
                    },
                },
                messages: {
                    wayID: {
                        required: alarmSearchChannelID
                    },
                    time: {
                        required: alarmSearchIntervalTime,
                        digits: alarmSearchIntervalError,
                        range: alarmSearchIntervalSize
                    },
                    command: {
                        range: alarmSearchPhotoSize,
                        required: alarmSearchPhotoNull
                    },
                    saveSign: {
                        required: alarmSearchSaveNull
                    },
                    distinguishability: {
                        required: alarmSearchResolutionNull
                    },
                    quality: {
                        range: alarmSearchMovieSize,
                        required: alarmSearchMovieNull
                    },
                    luminance: {
                        range: alarmSearchBrightnessSize,
                        required: alarmSearchBrightnessNull
                    },
                    contrast: {
                        range: alarmSearchContrastSize,
                        required: alarmSearchContrastNull
                    },
                    saturability: {
                        range: alarmSearchSaturatedSize,
                        required: alarmSearchSaturatedNull
                    },
                    chroma: {
                        range: alarmSearchColorSize,
                        required: alarmSearchColorNull
                    }
                }
            }).form();
        },
        fiterNumber: function (data) {
            if (data == null || data == undefined || data == "") {
                return data;
            } else {
                var data = data.toString();
                data = parseFloat(data);
                return data;
            }
        },
        // 应答
        responseSocket: function () {
            switchSignalAlarm.isGetSocketLayout();
        },
        isGetSocketLayout: function () {
            setTimeout(function () {
                if (webSocket.conFlag) {
                    webSocket.subscribe(headers, '/user/topic/check', switchSignalAlarm.updateTable, "/app/vehicle/inspect", null);
                } else {
                    switchSignalAlarm.isGetSocketLayout();
                }
            }, 2000);
        },
        // 应答socket回掉函数
        updateTable: function (msg) {
            if (msg != null) {
                var json = $.parseJSON(msg.body);
                var msgData = json.data;
                if (msgData != undefined) {
                    var msgId = msgData.msgHead.msgID;
                    // if (msgId == 0x9300) {
                    //     var dataType = msgData.msgBody.dataType;
                    //     $("#msgDataType").val(dataType);
                    //     $("#infoId").val(msgData.msgBody.data.infoId);
                    //     $("#objectType").val(msgData.msgBody.data.objectType);
                    //     $("#objectId").val(msgData.msgBody.data.objectId);
                    //     $("#question").text(msgData.msgBody.data.infoContent);
                    //     if (dataType == 0x9301) {
                    //         $("#answer").val("");
                    //         $("#msgTitle").text("平台查岗");
                    //         $("#goTraceResponse").modal('show');
                    //         $("#error_label").hidden();
                    //     }
                    //     if (dataType == 0x9302) {
                    //         $("#answer").val("");
                    //         $("#msgTitle").text("下发平台间报文");
                    //         $("#goTraceResponse").modal('show');
                    //     }
                    // }
                }
            }
        },
        // 应答确定
        platformMsgAck: function () {
            var answer = $("#answer").val();
            if (answer == "") {
                switchSignalAlarm.showErrorMsg("应答不能为空", "answer");
                return;
            }
            $("#goTraceResponse").modal('hide');
            var msgDataType = $("#msgDataType").val();
            var infoId = $("#infoId").val();
            var objectType = $("#objectType").val();
            var objectId = $("#objectId").val();
            var url = "/clbs/m/connectionparamsset/platformMsgAck";
            json_ajax("POST", url, "json", false, {
                "infoId": infoId,
                "answer": answer,
                "msgDataType": msgDataType,
                "objectType": objectType,
                "objectId": objectId
            });
        },
        showErrorMsg: function (msg, inputId) {
            if ($("#error_label").is(":hidden")) {
                $("#error_label").text(msg);
                $("#error_label").insertAfter($("#" + inputId));
                $("#error_label").show();
            } else {
                $("#error_label").is(":hidden");
            }
        },

    }
    $(function () {
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'search_condition') {
                switchSignalAlarm.init();
            }
        });
        //初始化组织结构
        switchSignalAlarm.init();
        switchSignalAlarm.alarmTreeInit();
        switchSignalAlarm.responseSocket();
        //设置当前时间显示
        $('#timeInterval').dateRangePicker();
        //当前时间
        switchSignalAlarm.getsTheCurrentTime();
        //改变勾选框
        $("#deviceType").change(function () {
            switchSignalAlarm.getlAlarmType();
            $("#groupSelect").val("全部");
            typePos = "-1";
            /*switchSignalAlarm.alarmListChange();*/
        });
        // 判断报警来源是否被修改
        $("#alarmSource").change(function () {
            $("#groupSelect").val("全部");
            typePos = "-1";
            switchSignalAlarm.getlAlarmType();
            /*switchSignalAlarm.alarmListChange();*/
        });
        //switchSignalAlarm.initSearch();

        $("#warningManageClose").click(function () {
            $("#warningManage").modal('hide')
        });
        $("#warningManageListening").bind("click", function () {
            switchSignalAlarm.showOrhide(3)
        });
        $("#warningManagePhoto").bind("click", function () {
            switchSignalAlarm.showOrhide(0)
        });
        $("#warningManageSend").bind("click", function () {
            switchSignalAlarm.showOrhide(1)
        });
        $("#goListeningForAlarm").bind("click", function () {
            switchSignalAlarm.listenForAlarm()
        });
        $("#goPhotographsForAlarm").bind("click", function () {
            switchSignalAlarm.takePhotoForAlarm()
        });
        $("#goTxtSendForAlarm").bind("click", function () {
            switchSignalAlarm.goTxtSendForAlarm()
        });
        $("#warningManageAffirm").bind("click", function () {
            switchSignalAlarm.handleAlarm("人工确认报警")
        });
        $("#warningManageCancel").bind("click", function () {
            switchSignalAlarm.handleAlarm("不做处理")
        });
        $("#warningManageFuture").bind("click", function () {
            switchSignalAlarm.handleAlarm("将来处理")
        });

        // 模糊搜索
        var inputChange;
        $("#search_condition").on('input propertychange', function (value) {
            if (inputChange !== undefined) {
                clearTimeout(inputChange);
            }
            ;
            inputChange = setTimeout(function () {
                var param = $("#search_condition").val();
                if (param == '') {
                    switchSignalAlarm.init();
                } else {
                    switchSignalAlarm.searchVehicleTree(param);
                }
            }, 500);
        });
        // 滚动展开
//		$("#treeDemo").scroll(function () {
//            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
//            zTreeScroll(zTree, this);
//        });
        //IE9
        if (navigator.appName == "Microsoft Internet Explorer" && navigator.appVersion.split(";")[1].replace(/[ ]/g, "") == "MSIE9.0") {
            var search;
            $("#search_condition").bind("focus", function () {
                search = setInterval(function () {
//                    search_ztree('treeDemo', 'search_condition','vehicle');
                    var param = $("#search_condition").val();
                    if (param == '') {
                        switchSignalAlarm.init();
                    } else {
                        switchSignalAlarm.searchVehicleTree(param);
                    }
                }, 500);
            }).bind("blur", function () {
                clearInterval(search);
            });
        }
        //IE9 end
        //点击查询执行
        $("#inquireClick").bind("click", function () {
            switchSignalAlarm.inquireClick()
        });
        //点击导出执行
        $("#alarmExport").bind("click", switchSignalAlarm.exportAlarm);
        $("#endTime").on('click', switchSignalAlarm.endTimeStyle);
        // 应答确定
        $('#parametersResponse').on('click', switchSignalAlarm.platformMsgAck);
        $("#groupSelect").bind("click", showMenuContent);
    })
})(window, $)