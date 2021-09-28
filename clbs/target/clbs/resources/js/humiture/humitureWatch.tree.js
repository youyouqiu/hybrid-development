;
(function (window, $) {
    var diyueall
        , asyncFlag = true // ztree是否进行异步加载
        , zTreeIdJson = {} // 保存ztree节点ID集合
        , isTreeState = true // 监控对象查询类型
        , ztreeOffLine = [] // 离线
        , ztreeOnLineAndAlarm = [] // 在线和报警车辆
        , ztreeOnLine = [] // 在线
        , ztreeAlarm = [] // 报警
        , allObjNum // 监控对象总的数量
        , searchTimeout // 模糊查询定时器
        , misstype = false // 判断是否为离线树
    ;
    var queryTemperatureIds = [];
    var queryTemperatureInterval = undefined;
    var haveTempValueMonitorId = [];
    var resultTempValue = [];
    var zTreeOnIsExpand = false;
    var saveMonitorMap = new Set();// 用于存储监控对象ID,批量更新其温湿度数据
    var getDataTimeout = null;// 获取温湿度数据的定时器

    humiture = {
        init: function () {
            json_ajax("POST", "/clbs/m/functionconfig/fence/bindfence/getStatistical", "json", true, null, humiture.setNumber);
        },
        setNumber: function (data) {
            $("#tall").text("(" + data.obj.allV + ")");
            $("#tline").text("(" + data.obj.onlineNum + ")");
            $("#tmiss").text("(" + (data.obj.allV - data.obj.onlineNum) + ")");
            var num = data.obj.allV // 监控对象数量
                , url
                , otherParam = null;
            diyueall = data.obj.vehicleIdArray;
            let runVidArray = data.obj.runVidArray;
            let stopVidArray = data.obj.stopVidArray;
            // 在线
            ztreeOnLineAndAlarm = runVidArray.concat(stopVidArray);
            allObjNum = num;
            if (num <= 5000) {
                url = '/clbs/m/functionconfig/fence/bindfence/monitorTree';
            } else {
                url = '/clbs/m/functionconfig/fence/bindfence/bigDataMonitorTree';
            }
            humiture.initZtree(url, otherParam);
        },
        // ztree 初始化构建
        initZtree: function (url, otherParam) {
            asyncFlag = true;
            // ztree 配置文件
            var setting = {
                async: {
                    url: url,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    otherParam: otherParam,
                    dataType: "json",
                    dataFilter: humiture.ajaxDataFilter
                },
                view: {
                    dblClickExpand: false,
                    nameIsHTML: true,
                    fontCss: setFontCss_ztree,
                },
                check: {
                    enable: false
                },
                data: {
                    simpleData: {
                        enable: true
                    },
                    key: {
                        title: "name"
                    }
                },
                callback: {
                    beforeAsync: humiture.zTreeBeforeAsync,
                    onClick: humiture.onClickV,
                    beforeDblClick: humiture.zTreeBeforeDblClick,
                    onDblClick: humiture.onDbClickV,
                    onAsyncSuccess: humiture.zTreeOnAsyncSuccess,
                    onExpand: humiture.zTreeOnExpand,
                    onNodeCreated: humiture.zTreeOnNodeCreated
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting);
            humiture.isGetSocket();
        },
        isGetSocket: function () {
            setTimeout(function () {
                if (webSocket.conFlag) {
                    webSocket.subscribe(headers, '/user/topic/cachestatus', humiture.updataRealTree, null, null);
                    setTimeout(function () {
                        webSocket.subscribe(headers, '/user/topic/alarm', humitureAlarmTable.updataRealAlarmMessage, "/app/vehicle/subscribeStatus", null);
                    }, 1000);
                } else {
                    humiture.isGetSocket();
                }
            }, 2000);
        },
        // ztree 预处理函数
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            if (isTreeState) {
                responseData = JSON.parse(ungzip(responseData));
            }
            if ($('#search_condition').val() != '') {
                responseData = filterQueryResult(responseData, null);
            }
            humiture.scheduleUpdateTemperature();
            return responseData;
        },
        // 异步加载之前的事件回调函数
        zTreeBeforeAsync: function () {
            return asyncFlag;
        },
        // ztree 单击事件
        onClickV: function (event, treeId, treeNode) {
            //去除勾选，消除背景颜色
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.cancelSelectedNode(treeNode);
        },
        // 双击之前事件
        zTreeBeforeDblClick: function (event, treeNode) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.cancelSelectedNode(treeNode);
            var state = false;
            if (treeNode.type === 'vehicle') {
                var id = treeNode.tId;
                var index = $('#' + id + '_ico').attr('class').indexOf('vehicleSkin');
                if (index === -1) {
                    state = true;
                }
            }
            return state;
        },
        // 双击事件
        onDbClickV: function (event, treeId, treeNode) {
            //订阅勾选，显示背景颜色
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.selectNode(treeNode);
            hwMain.dbClickTreeCB(treeNode);
        },
        // 异步加载成功事件
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            asyncFlag = false;
            $('#treeLoading').hide();
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            var notExpandNodeInit = treeObj.getNodesByFilter(assignmentNotExpandFilter);
            /* for (var i = 0, len = notExpandNodeInit.length; i < len; i++) {
                 if (notExpandNodeInit[i].type === 'assignment' && notExpandNodeInit[i].children != undefined) {
                     treeObj.expandNode(notExpandNodeInit[i], true, false, false, true);
                 } else {
                     treeObj.hideNode(notExpandNodeInit[i]);
                 }
             }*/
            var initLen = 0;
            var allMonitor = [];
            for (var i = 0; i < notExpandNodeInit.length; i++) {
                allMonitor = objArrRemoveRepeat(allMonitor.concat(notExpandNodeInit[i].children));
                initLen = allMonitor.length;
                treeObj.expandNode(notExpandNodeInit[i], true, true, false, true);
                if (initLen > 30) {
                    break;
                }
            }
            // 订阅对象颜色变化
//        	var keys = monitoringObjMap.keys();
//        	for (var i = 0; i < keys.length; i++) {
//        		var nodes = treeObj.getNodesByParam("id", keys[i], null);
//        		for (var j = 0; j < nodes.length; j++) {
//        			if (ztreeOnLine.indexOf(keys[i]) != -1) {
//        				$('#' + nodes[j].tId + '_span').addClass('obj_select_online');
//        			} else if (ztreeHeartBeat.indexOf(keys[i]) != -1) {
//        				$('#' + nodes[j].tId + '_span').addClass('obj_select_heartbeat');
//        			}
//        		}
//        	}
        },
        // 节点展开事件
        zTreeOnExpand: function (event, treeId, treeNode) {
            if (treeNode.type === 'assignment' && treeNode.children === undefined) { // 获取分组下面的监控对象节点
                var url = '/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign';
                var data = {'assignmentId': treeNode.id, 'isChecked': true, 'monitorType': 'allMonitor'};
                json_ajax("POST", url, "json", true, data, function (info) {
                    humiture.getGroupList(info, treeNode, false)
                });
            } else if (treeNode.type === 'assignment' && treeNode.children !== undefined) {
                var nodes = treeNode.children;
                var param = [];
                for (var i = 0, len = nodes.length; i < len; i++) {
                    if (nodes[i].type === 'vehicle') {
                        var obj = {};
                        obj.vehicleID = nodes[i].id;
                        param.push(obj);
                    }
                }
                if (param.length > 0) {
                    var requestStrS = {
                        "desc": {
                            "MsgId": 40964,
                            "UserName": $("#userName").text()
                        },
                        "data": param
                    };
                    humiture.isExpendSocket(requestStrS);
                }
            }
            zTreeOnIsExpand = true;
        },
        // 节点创建事件
        zTreeOnNodeCreated: function (event, treeId, treeNode) {
            if (treeNode.type == 'vehicle') {
                var id = treeNode.id;
                if (zTreeIdJson[id] == null) {
                    var list = [treeNode.tId];
                    zTreeIdJson[id] = list;
                } else {
                    zTreeIdJson[id].push(treeNode.tId);
                }
            }
        },
        // 获取ztree 分组下的节点
        getGroupList: function (data, treeNode, flag) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            var addV = treeObj.addNodes(treeNode, JSON.parse(ungzip(data.msg)), flag);
            if (addV !== null && treeNode.type === 'assignment') {
                var param = [];
                for (var i = 0; i < treeNode.children.length; i++) {
                    var obj = {};
                    obj.vehicleID = treeNode.children[i].id;
                    param.push(obj)
                }
                // 订阅所有车辆
                var requestStrS = {
                    "desc": {
                        "MsgId": 40964,
                        "UserName": $("#userName").text()
                    },
                    "data": param
                };
                humiture.isExpendSocket(requestStrS);
                // webSocket.subscribe(headers, '/user/' + $("#userName").text() + '/cachestatus', humiture.updataRealTree, "/app/vehicle/subscribeCacheStatusNew", requestStrS);
            }
        },
        // socket 订阅连接
        isExpendSocket: function (requestStrS) {
            setTimeout(function () {
                if (webSocket.conFlag) {
                    console.log('订阅状态信息参数：', requestStrS);
                    for (var i = 0; i < requestStrS.data.length; i++) {
                        var id = requestStrS.data[i].vehicleID;
                        if (queryTemperatureIds.indexOf(id) === -1) {
                            queryTemperatureIds.push(id);
                        }
                    }
                    webSocket.subscribe(headers, '/user/topic/cachestatus', humiture.updataRealTree, "/app/vehicle/subscribeCacheStatusNew", requestStrS);
                } else {
                    humiture.isExpendSocket(requestStrS);
                }
            }, 1000)
        },
        getRealName: function (name) {
            var index = name.indexOf('[');
            if (index === -1) {
                return name;
            }
            return name.substr(0, index).trim();
        },
        scheduleUpdateTemperature: function (id) {
            if (queryTemperatureInterval !== undefined) {
                clearInterval(queryTemperatureInterval);
            }
            humiture.updateTemperature(id);
            queryTemperatureInterval = setInterval(function () {
                humiture.updateTemperature(id);
            }, 30000);
        },
        updateTemperature: function (id) {
            var url = '/clbs/v/monitoring/humiture/getTreeMonitorTempData';
            var data = {"monitorId": id ? id.join(',') : ""};
            json_ajax("POST", url, "json", false, data, function (r) {
                if (r.success) {
                    if (r.obj) {
                        haveTempValueMonitorId = [];
                        resultTempValue = [];
                        haveTempValueMonitorId = r.obj.monitorId;
                        resultTempValue = r.obj.value;
                        if (!zTreeOnIsExpand) { // 分组节点没展开
                            return;
                        }
                        var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                        // 获取到展开了的节点
                        for (var i = 0; i < haveTempValueMonitorId.length; i++) {
                            var key = haveTempValueMonitorId[i];
                            var list = zTreeIdJson[key];
                            if (list && list.length > 0) {
                                $.each(list, function (index, value) {
                                    var treeNode = treeObj.getNodeByTId(value);
                                    if (treeNode != null && treeNode.iconSkin !== 'vehicleSkin') {
                                        if (resultTempValue[i] != "" && resultTempValue[i] != null) {
                                            treeNode.name = humiture.getRealName(treeNode.name) + ' [' + ((Number(resultTempValue[i]) / 10).toFixed(1) + '℃') + ']';
                                            treeObj.updateNode(treeNode);
                                        }
                                    }
                                })
                            }
                        }
                    }
                } else {
                    console.error(r);
                }
            });
        },
        // ztree 监控对象状态更新
        updataRealTree: function (msg) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            var data = $.parseJSON(msg.body);
            if (data.desc.msgID == 39321) {
                var stateInfo = data.data;
                for (var i = 0, len = stateInfo.length; i < len; i++) {
                    var info = stateInfo[i];
                    var vehicleStatus = info.vehicleStatus;
                    var changeStatus = '';
                    if (vehicleStatus == '3') { // 离线
                        if (!ztreeOffLine.isHas(info.vehicleId)) {
                            ztreeOffLine.push(info.vehicleId);
                        }
                        ztreeAlarm.remove(info.vehicleId);
                        ztreeOnLine.remove(info.vehicleId);
                        ztreeOnLineAndAlarm.remove(info.vehicleId);
                    } else if (vehicleStatus == '5') { // 报警
                        changeStatus = 'alarm';
                        if (!ztreeAlarm.isHas(info.vehicleId)) {
                            ztreeAlarm.push(info.vehicleId);
                        }
                        if (!ztreeOnLineAndAlarm.isHas(info.vehicleId)) {
                            ztreeOnLineAndAlarm.push(info.vehicleId);
                        }
                        ztreeOffLine.remove(info.vehicleId);
                        ztreeOnLine.remove(info.vehicleId);
                    } else if (vehicleStatus == '4' || vehicleStatus == '10' || vehicleStatus == '2' || vehicleStatus == '9' || vehicleStatus == '11') { // 在线
                        changeStatus = 'online';
                        if (!ztreeOnLine.isHas(info.vehicleId)) {
                            ztreeOnLine.push(info.vehicleId);
                        }
                        if (!ztreeOnLineAndAlarm.isHas(info.vehicleId)) {
                            ztreeOnLineAndAlarm.push(info.vehicleId);
                        }
                        ztreeAlarm.remove(info.vehicleId);
                        ztreeOffLine.remove(info.vehicleId);
                    }
                    var vehicleId = info.vehicleId;
                    if (changeStatus) {
                        var list = zTreeIdJson[vehicleId];
                        if (list != null) {
                            for (var s = 0, slen = list.length; s < slen; s++) {
                                var treeNode = zTree.getNodeByTId(list[s]);
                                if (treeNode != null) {
                                    if (treeNode.type == 'vehicle') {
                                        var oldIcon = treeNode.iconSkin;
                                        if (changeStatus === 'alarm') {
                                            treeNode.iconSkin = 'button btnImage iconArea warning';
                                            if ($("#" + list[s] + "_span")[0]) $("#" + list[s] + "_span")[0].style.color = "#ffab2d";
                                        } else {
                                            treeNode.iconSkin = 'button btnImage iconArea onlineDriving';
                                            if ($("#" + list[s] + "_span")[0]) $("#" + list[s] + "_span")[0].style.color = "#78af3a";
                                        }
                                        var newIcon = treeNode.iconSkin;
                                        var oldValue = treeNode.tempValue;
                                        humiture.setTreeMonitorTempValue(treeNode);
                                        var newValue = treeNode.tempValue;
                                        if (oldIcon === newIcon && oldValue === newValue && treeNode.status == vehicleStatus) continue;
                                        treeNode.status = vehicleStatus;
                                        zTree.updateNode(treeNode);
                                        if (misstype) {
                                            zTree.hideNode(treeNode);
                                        }
                                    }
                                }
                                ``
                            }
                        }
                    }

                }
                /*if (ztreeAlarm.length != 0) {
                    for (var i = 0, len = ztreeAlarm.length; i < len; i++) {
                        var list = zTreeIdJson[ztreeAlarm[i]];
                        if (list != null) {
                            for (var s = 0, slen = list.length; s < slen; s++) {
                                var treeNode = zTree.getNodeByTId(list[s]);
                                if (treeNode != null) {
                                    if (treeNode.type == 'vehicle') {
                                        treeNode.iconSkin = 'button btnImage iconArea warning';
                                        humiture.setTreeMonitorTempValue(treeNode);
                                        zTree.updateNode(treeNode);
                                        if ($("#" + list[s] + "_span")[0]) $("#" + list[s] + "_span")[0].style.color = "#ffab2d";
                                        if (misstype) {
                                            zTree.hideNode(treeNode);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (ztreeOnLine.length != 0) {
                    for (var i = 0, len = ztreeOnLine.length; i < len; i++) {
                        var list = zTreeIdJson[ztreeOnLine[i]];
                        if (list != null) {
                            for (var s = 0, slen = list.length; s < slen; s++) {
                                var treeNode = zTree.getNodeByTId(list[s]);
                                if (treeNode != null) {
                                    if (treeNode.type == 'vehicle') {
                                        treeNode.iconSkin = 'button btnImage iconArea onlineDriving';
                                        humiture.setTreeMonitorTempValue(treeNode);
                                        zTree.updateNode(treeNode);
                                        if ($("#" + list[s] + "_span")[0]) $("#" + list[s] + "_span")[0].style.color = "#78af3a";
                                        if (misstype) {
                                            zTree.hideNode(treeNode);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }*/
            } else if (data.desc.msgID == 34952 || data.desc.msgID == 30583) {//新增
                var stateInfo = data.data
                    , state = stateInfo[0].vehicleStatus;
                if (state == '3') { //离线
                    if (!ztreeOffLine.isHas(stateInfo[0].vehicleId)) {
                        ztreeOffLine.push(stateInfo[0].vehicleId);
                    }
                    ztreeAlarm.remove(stateInfo[0].vehicleId);
                    ztreeOnLine.remove(stateInfo[0].vehicleId);
                    ztreeOnLineAndAlarm.remove(stateInfo[0].vehicleId);
                    humiture.getStateList(stateInfo[0].vehicleId, state);
                } else if (state == '5') { //报警
                    if (!ztreeAlarm.isHas(stateInfo[0].vehicleId)) {
                        ztreeAlarm.push(stateInfo[0].vehicleId);
                    }
                    if (!ztreeOnLineAndAlarm.isHas(stateInfo[0].vehicleId)) {
                        ztreeOnLineAndAlarm.push(stateInfo[0].vehicleId);
                    }
                    ztreeOffLine.remove(stateInfo[0].vehicleId);
                    ztreeOnLine.remove(stateInfo[0].vehicleId);
                    humiture.getStateList(stateInfo[0].vehicleId, state);
                } else if (state == '4' || state == '10' || state == '2' || state == '9' || state == '11') { //在线
                    if (!ztreeOnLine.isHas(stateInfo[0].vehicleId)) {
                        ztreeOnLine.push(stateInfo[0].vehicleId);
                    }
                    if (!ztreeOnLineAndAlarm.isHas(stateInfo[0].vehicleId)) {
                        ztreeOnLineAndAlarm.push(stateInfo[0].vehicleId);
                    }
                    ztreeOffLine.remove(stateInfo[0].vehicleId);
                    ztreeAlarm.remove(stateInfo[0].vehicleId);
                    humiture.getStateList(stateInfo[0].vehicleId, state);
                }
                //全部、在线、离线数量实时计算
                if (diyueall.isHas(stateInfo[0].vehicleId)) {
                    $("#tline").text("(" + parseInt(ztreeOnLineAndAlarm.length) + ")");
                    $("#tmiss").text("(" + (parseInt(allObjNum) - parseInt(ztreeOnLineAndAlarm.length)) + ")");
                }
            }
        },
        // 批量更新监控对象温湿度数据
        batchGetTempData: function () {
            var saveMonitorArr = [];
            saveMonitorMap.forEach((value) => {
                saveMonitorArr.push(value);
            });
            if (saveMonitorArr.length === 0) return;
            var url = '/clbs/v/monitoring/humiture/getTreeMonitorTempData';
            var data = {"monitorId": saveMonitorArr.join(',')};
            json_ajax("POST", url, "json", true, data, function (r) {
                if (r.success) {
                    if (r.obj) {
                        var tempValue = r.obj.value;
                        var monitorIds = r.obj.monitorId;
                        if (!monitorIds || !tempValue) return;
                        var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                        // 获取到展开了的节点
                        for (var i = 0; i < monitorIds.length; i++) {
                            var key = monitorIds[i];
                            var list = zTreeIdJson[key];
                            if (list && list.length > 0) {
                                $.each(list, function (index, value) {
                                    var treeNode = treeObj.getNodeByTId(value);
                                    if (treeNode != null && treeNode.iconSkin !== 'vehicleSkin') {
                                        if (tempValue[i] != "" && tempValue[i] != null) {
                                            var newValue = (Number(tempValue[i]) / 10).toFixed(1);
                                            if (treeNode.tempValue === undefined || (treeNode.tempValue && treeNode.tempValue != newValue)) {
                                                treeNode.tempValue = newValue;
                                                treeNode.name = humiture.getRealName(treeNode.name) + ' [' + (newValue + '℃') + ']';
                                                treeObj.updateNode(treeNode);
                                            }
                                        }
                                    }
                                })
                            }
                        }
                    }
                } else {
                    console.error(r);
                }
                saveMonitorMap.clear();
                clearTimeout(getDataTimeout);
                getDataTimeout = null;
            });
        },
        setTreeMonitorTempValue: function (treeNode) {
            var monitorId = treeNode.id;
            var monitorIndex = haveTempValueMonitorId.indexOf(monitorId);
            if (monitorIndex == -1) { // 找到监控对象的温度值
                saveMonitorMap.add(monitorId);
                if (!getDataTimeout) {
                    getDataTimeout = setTimeout(humiture.batchGetTempData, 5000);
                }
                // humiture.throttle(humiture.batchGetTempData, 5000, 10000)();
                /* var url = '/clbs/v/monitoring/humiture/getTreeMonitorTempData';
                 var data = {"monitorId": monitorId};
                 json_ajax("POST", url, "json", true, data, function (r) {
                     if (r.success) {
                         if (r.obj) {
                             var tempValue = r.obj.value;
                             if (tempValue != "" && tempValue != null) {
                                 treeNode.name = humiture.getRealName(treeNode.name) + ' [' + ((Number(tempValue) / 10).toFixed(1) + '℃') + ']';
                             }
                         }
                     } else {
                         console.error(r);
                     }
                 });*/
            } else {
                if (resultTempValue[monitorIndex] != "" && resultTempValue[monitorIndex] != null) {
                    treeNode.tempValue = (Number(resultTempValue[monitorIndex]) / 10).toFixed(1);
                    treeNode.name = humiture.getRealName(treeNode.name) + ' [' + ((Number(resultTempValue[monitorIndex]) / 10).toFixed(1) + '℃') + ']';
                }
            }
        },
        // ztree 获取状态改变节点
        getStateList: function (id, type) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo")
                , nodes = treeObj.getNodesByParam("id", id, null);
            if (nodes != null) {
                for (var i = 0, len = nodes.length; i < len; i++) {
                    humiture.setZtreeListStyle(nodes[i], type);
                }
            }
        },
        // ztree 设置节点样式
        setZtreeListStyle: function (node, type) {
            if (node.type === 'vehicle') {
                var color = '';
                var oldValue = node.tempValue;
                var oldIcon = node.iconSkin;
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                if (type == '3') {
                    node.iconSkin = 'btnImage iconArea offlineIcon';
                    // 离线
                    color = '#b6b6b6';
                    if (node.name != null && node.name != undefined && node.name != "") {
                        node.name = humiture.getRealName(node.name); // 移除
                    }
                } else if (type == '5') {
                    node.iconSkin = 'button btnImage iconArea warning';
                    // 报警
                    color = '#ffab2d';
                    humiture.setTreeMonitorTempValue(node);
                } else {
                    node.iconSkin = 'button btnImage iconArea onlineDriving';
                    // 行驶中
                    color = '#78af3a';
                    humiture.setTreeMonitorTempValue(node);
                }
                if ($("#" + node.tId + "_span")[0]) {
                    $("#" + node.tId + "_span")[0].style.color = color;
                }
                var newIcon = node.iconSkin;
                var newValue = node.tempValue;
                if (!(oldIcon === newIcon && oldValue === newValue && node.status == type)) {
                    node.status = type;
                    treeObj.updateNode(node);
                }
            }
        },
        // ztree 在线离线报警
        ztreeStateSearchOnLine: function (event) {
            queryTemperatureIds = [];
            isTreeState = false;
            $("#search_condition").val("");
            var url = '/clbs/m/basicinfo/monitoring/vehicle/treeStateInfo';
            var otherParam = {'type': event.data.type, 'webType': 1};
            humiture.initZtree(url, otherParam);
        },

        // 刷新树
        refreshTree: function () {
            queryTemperatureIds = [];
            ztreeOffLine = []; // 离线
            ztreeOnLine = []; // 在线
            ztreeAlarm = []; // 报警
            zTreeIdJson = {}; // 所有车辆id集合
            $('#treeLoading').show();
            zTreeOnIsExpand = false;
            misstype = false;
            $("#search_condition").val("");
            var url
                , otherParam = null;
            isTreeState = true;
            if (allObjNum <= 5000) {
                url = '/clbs/m/functionconfig/fence/bindfence/monitorTree';
            } else {
                url = '/clbs/m/functionconfig/fence/bindfence/bigDataMonitorTree';
            }
            humiture.initZtree(url, otherParam);
        },
        // ztree 离线
        ztreeStateSearchOffLine: function (event) {
            queryTemperatureIds = [];
            ztreeOffLine = []; // 离线
            ztreeOnLine = []; // 在线
            ztreeAlarm = []; // 报警
            zTreeIdJson = {}; // 所有车辆id集合
            $('#treeLoading').show();
            zTreeOnIsExpand = false;
            misstype = true;
            $("#search_condition").val("");
            var url
                , otherParam = null;
            if (allObjNum <= 5000) {
                isTreeState = true;
                url = '/clbs/m/basicinfo/monitoring/vehicle/treeStateInfo';
                otherParam = {'type': event.data.type, 'webType': 1};
            } else {
                isTreeState = true;
                url = '/clbs/m/functionconfig/fence/bindfence/bigDataMonitorTree';
            }
            humiture.initZtree(url, otherParam);
        },
        // ztree 报警和在线
        ztreeStateSearchOnLine: function (event) {
            queryTemperatureIds = [];
            zTreeOnIsExpand = false;
            ztreeOffLine = []; // 离线
            ztreeOnLine = []; // 在线
            ztreeAlarm = []; // 报警
            zTreeIdJson = {}; // 所有车辆id集合
            $('#treeLoading').show();
            misstype = false;
            isTreeState = false;
            $("#search_condition").val("");
            var url = '/clbs/m/basicinfo/monitoring/vehicle/treeStateInfo';
            var otherParam = {'type': event.data.type, 'webType': 1};
            humiture.initZtree(url, otherParam);
        },
        // ztree 模糊查询
        ztreeSearch: function () {
            if (searchTimeout !== undefined) {
                clearTimeout(searchTimeout);
            }
            searchTimeout = setTimeout(function () {
                humiture.search_condition();
            }, 500);
        },
        // 模糊查询显示
        search_condition: function () {
            queryTemperatureIds = [];
            ztreeOffLine = []; // 离线
            ztreeOnLine = []; // 在线
            ztreeAlarm = []; // 报警
            zTreeIdJson = {}; // 所有车辆id集合
            $('#treeLoading').show();
            misstype = false;
            var value = $("#search_condition").val()
                , queryType = $("#searchType").val()
                , url
                , otherParam = null;
            isTreeState = true;
            if (value !== '') {
                url = '/clbs/m/functionconfig/fence/bindfence/monitorTreeFuzzy';
                otherParam = {'queryParam': value, 'queryType': queryType};
            } else {
                if (allObjNum <= 5000) {
                    url = '/clbs/m/functionconfig/fence/bindfence/monitorTree';
                } else {
                    url = '/clbs/m/functionconfig/fence/bindfence/bigDataMonitorTree';
                }
            }
            humiture.initZtree(url, otherParam);
        },

        // 数组原型链拓展方法
        arrayExpand: function () {
            // 删除数组指定对象
            Array.prototype.remove = function (obj) {
                for (var i = 0; i < this.length; i++) {
                    var num = this.indexOf(obj);
                    if (num !== -1) {
                        this.splice(num, 1);
                    }
                }
            };
            // 是否包含该对象
            Array.prototype.isHas = function (a) {
                if (this.length === 0) {
                    return false
                }
                ;
                for (var i = 0, len = this.length; i < len; i++) {
                    if (this[i] === a) {
                        return true
                    }
                }
            };
        },

    };

    $(function () {
        humiture.arrayExpand();
        humiture.init();
        $('#chooseAll').on('click', humiture.refreshTree);// 全部
        $('#online').on('click', {type: 1}, humiture.ztreeStateSearchOnLine);// 在线
        $('#chooseMissLine').on('click', {type: 7}, humiture.ztreeStateSearchOffLine);// 离线
        $('#chooseOnline').on('click', {type: 1}, humiture.ztreeStateSearchOnLine);// 在线
        $('#chooseOffline').on('click', {type: 7}, humiture.ztreeStateSearchOffLine);// 离线
        $('#chooseAlarm').on('click', {type: 4}, humiture.ztreeStateSearchOnLine);// 报警
        $('#search_condition').on('input propertychange', humiture.ztreeSearch);// 模糊查询
        $("#searchType").change(humiture.search_condition);// 搜索类型下拉框change事件
        $('#refresh').on('click', humiture.refreshTree);// 刷新树
    })

})(window, $);