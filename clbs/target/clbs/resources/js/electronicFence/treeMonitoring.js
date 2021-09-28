var setting;
var bflag = true;
var eflag = true;
var onLineIsExpandAll = false;
var cheakedAll = [];
var cheackGourpNum;
var allflag = true;
var missAll = false;
var fzzflag = false;
var stopVidArray = [];
var runVidArray = [];
var lineAndStop = [];
var nmoline = [];
var lineAndmiss = [];
var lineAndRun = [];
var lineAndAlarm = [];
var overSpeed = [];
var heartBeat = [];
var treeNodeNew;
var globalDeviceType;
// 有时间的命令字,控制时间控件开启或关闭
var commandSign = ["8H", "9H", "10H", "11H", "12H", "13H", "14H", "15H"];
var isInitDatePicker = true;// 避免重复初始化

var subscribeVehicleInfo = null;

var obdTypeIdMap = new pageLayout.mapVehicle();

//第一次更新树的时候更新行驶和停止的值
var updateStopAndRunNum = true;
var initArrIsInit = false;
var currentRightClickVehicleId;

var treeMonitoring = {
    // 初始化
    init: function () {
        // 多媒体检索和多媒体上传时间赋值
        var nowDate = new Date();
        msStartTime = nowDate.getFullYear()
            + "-"
            + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                + parseInt(nowDate.getMonth() + 1) : parseInt(nowDate
                .getMonth() + 1))
            + "-"
            + (nowDate.getDate() < 10 ? "0" + nowDate.getDate() : nowDate.getDate())
            + " " + "00:00:00";
        msEndTime = nowDate.getFullYear()
            + "-"
            + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                + parseInt(nowDate.getMonth() + 1) : parseInt(nowDate
                .getMonth() + 1))
            + "-"
            + (nowDate.getDate() < 10 ? "0" + nowDate.getDate() : nowDate.getDate())
            + " " + ("23") + ":" + ("59") + ":" + ("59");

        $("#msStartTime").val(msStartTime);
        $("#msEndTime").val(msEndTime);
        $("#muStartTime").val(msStartTime);
        $("#muEndTime").val(msEndTime);

        if ($("#tall").text() === "(...)") {
            $.ajax(
                {
                    type: 'POST',
                    url: '/clbs/m/functionconfig/fence/bindfence/getRunAndStopMonitorNum',
                    dataType: 'json',
                    async: true,
                    data: {"isNeedMonitorId": true, "userName": $("#userName").text()},
                    timeout: 8000,
                    success: function (data) {
                        treeMonitoring.setNumber(data);
                    }
                }
            );
        }
        //修改终端车牌
        $("#continuousReturnValue").bind('change', function () {
            if ($(this).val() == 0) {
                $('#timeInterval0').show();
                $('#timeInterval2').hide();
                $('#timeInterval1').hide();
            } else if ($(this).val() == 1) {
                $('#timeInterval1').show();
                $('#timeInterval0').hide();
                $('#timeInterval2').hide();
            } else if ($(this).val() == 2) {
                $('#timeInterval2').show();
                $('#timeInterval0').hide();
                $('#timeInterval1').hide();
            }
        });
        //OBD
        $("#classification").bind('change', function () {
            if ($(this).val() == 0) {
                $('#modelName').html("<label class='text-danger'>*</label> 车型名称：");
                treeMonitoring.getOBDVehicle();
            } else if ($(this).val() == 1) {
                $('#modelName').html("<label class='text-danger'>*</label> 发动机类型：");
                treeMonitoring.getOBDVehicle();
            }
        })
    },
    // 双击车个，车个居中
    centerMarker: function (id, type) {
        var id = id
        if (type == 'DBLCLICK') {
            markerFocus = id;
        }
        var zoom = map.getZoom();
        // 判断可视区域集合里面是否已经创建了marker
        if (markerViewingArea.containsKey(id)) {
            var marker = markerViewingArea.get(id)[0];
            map.setZoomAndCenter(18, marker.getPosition());
        } else {
            if (markerAllUpdateData.containsKey(id)) {
                var info = markerAllUpdateData.get(id);
                var markerLngLat = [info[0][2], info[0][3]];
                map.setZoomAndCenter(18, markerLngLat);
            }
        }
        setTimeout(function () {
            amapOperation.markerStateListening();
        }, zoom < 13 ? 500 : 0);
    },
    centerMarkerNo: function () {
        markerFocus = null;
    },
    //取消点
    clearMarker: function (param) {
        for (var i = 0, len = param.length; i < len; i++) {
            var id = param[i].vehicleID; // 监控对象ID

            // 删除所有监控对象集合信息
            if (markerAllUpdateData.containsKey(id)) {
                markerAllUpdateData.remove(id);
            }

            // 删除可视区域内监控对象集合信息
            if (markerViewingArea.containsKey(id)) {
                var marker = markerViewingArea.get(id)[0]
                marker.stopMove();
                map.remove([marker]);
                markerViewingArea.remove(id);
            }

            // 删除可视区域外监控对象集合信息
            if (markerOutside.containsKey(id)) {
                markerOutside.remove(id);
            }

            // 删除车牌号marker图标
            if (carNameMarkerMap.containsKey(id)) {
                var marker = carNameMarkerMap.get(id);
                marker.stopMove();
                map.remove([marker]);
                carNameMarkerMap.remove(id);
            }
        }
        // 关闭地图信息弹窗
        map.clearInfoWindow();
    },
    //组织树预处理函数
    ajaxDataFilter: function (treeId, parentNode, responseData) {
        responseData = JSON.parse(ungzip(responseData));
        return responseData;
    },
    zTreeOnNodeCreated: function (event, treeId, treeNode) {
        var id = treeNode.id.toString();
        var list = [];
        if (zTreeIdJson[id] == null) {
            list = [treeNode.tId];
            zTreeIdJson[id] = list;
        } else {
            zTreeIdJson[id].push(treeNode.tId)
        }
    },
    //车个树加载成功事件
    zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
        var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
        var nodes = treeObj.getCheckedNodes(true);
        allNodes = treeObj.getNodes();
        var childNodes = zTree.transformToArray(allNodes[0]);
        var initLen = 0;
        initArr = [];
        notExpandNodeInit = zTree.getNodesByFilter(assignmentNotExpandFilter);

        // 为了优化ztree update数据卡顿的情况，修改方案为初始时展开监控对象数量为50个
        for (var i = 0; i < notExpandNodeInit.length; i++) {
            initLen += notExpandNodeInit[i].children.length;
            // if (initLen > 30 && i === 0) {
            //   zTree.expandNode(notExpandNodeInit[i], true, true, false, true);
            // }
            // if (initLen <= 30) {
            initArr.push(i);
            if (onLineIsExpandAll) {
                zTree.expandNode(notExpandNodeInit[i], true, true, false, true);
            }
            // }
            if (initLen > 30) {
                break;
            }
        }
        onLineIsExpandAll = false;

        // for (var i = 0; i < notExpandNodeInit.length; i++) {
        //     initArr.push(i)
        //     initLen += notExpandNodeInit[i].children.length;
        //     if (initLen >= 400) {
        //         break;
        //     }
        // }
        // if (onLineIsExpandAll == true) {
        //     for (var j = 0; j < initArr.length; j++) {
        //         zTree.expandNode(notExpandNodeInit[j], true, true, false, true);
        //     }
        //     onLineIsExpandAll = false;
        // }
        initArrIsInit = true;
        var jumpId = $("#jumpId").val();
        if (jumpId == "trackPlayer") {
            var cheakdiyueall = [];
            for (var i = 0; i < nodes.length; i++) {
                if (nodes[i].type === "vehicle" || nodes[i].type === "people" || nodes[i] == "thing") {
                    cheakdiyueall.push(nodes[i].id)
                }
            }
            var param = [];
            cheakdiyuealls = treeMonitoring.unique(cheakdiyueall);
            var requestStrS = {
                "desc": {
                    "MsgId": 40964,
                    "UserName": $("#userName").text(),
                    "isAppFlag": false
                },
                "data": cheakdiyuealls
            };
            webSocket.subscribe(headers, '/user/topic/location', treeMonitoring.updataRealTree, "/app/location/subscribe", requestStrS);
        }
        bflag = false;
    },
    //模糊查询车个树加载成功事件
    searchZTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
        var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
        var nodes = treeObj.getCheckedNodes(true);
        allNodes = treeObj.getNodes();
        var childNodes = zTree.transformToArray(allNodes[0]);
        var initLen = 0;
        initArr = [];
        notExpandNodeInit = zTree.getNodesByFilter(assignmentNotExpandFilter);
        zTree.expandNode(notExpandNodeInit[0], true, true, false, true);
        // wjk11
        // zTree.expandAll(true);

        for (var i = 0; i < notExpandNodeInit.length; i++) {
            initArr.push(i)
            initLen += notExpandNodeInit[i].children.length;
            if (initLen >= 400) {
                break;
            }
        }
        if (onLineIsExpandAll == true) {
            for (var j = 0; j < initArr.length; j++) {
                zTree.expandNode(notExpandNodeInit[j], true, true, false, true);
            }
            onLineIsExpandAll = false;
        }
        initArrIsInit = true;
        var jumpId = $("#jumpId").val();
        if (jumpId == "trackPlayer") {
            var cheakdiyueall = [];
            for (var i = 0; i < nodes.length; i++) {
                if (nodes[i].type === "vehicle" || nodes[i].type === "people" || nodes[i] == "thing") {
                    cheakdiyueall.push(nodes[i].id)
                }
            }
            var param = [];
            cheakdiyuealls = treeMonitoring.unique(cheakdiyueall);
            var requestStrS = {
                "desc": {
                    "MsgId": 40964,
                    "UserName": $("#userName").text(),
                    "isAppFlag": false
                },
                "data": cheakdiyuealls
            };
            webSocket.subscribe(headers, '/user/topic/location', treeMonitoring.updataRealTree, "/app/location/subscribe", requestStrS);
        }
        bflag = false;
    },
    //模糊查询
    searchNeverOnline: function (treeId, searchConditionId) {
        //<2>.得到模糊匹配搜索条件的节点数组集合
        var highlightNodes = new Array();
        if (searchConditionId != "") {
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            highlightNodes = treeObj.getNodesByParamFuzzy("id", searchConditionId, null);
            return highlightNodes;
        }
    },
    // 获取车个状态
    searchByFlag: function (treeId, searchConditionId, flag, type) {
        //<2>.得到模糊匹配搜索条件的节点数组集合
        var highlightNodes = new Array();
        if (searchConditionId != "") {
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            highlightNodes = treeObj.getNodesByParamFuzzy("id", searchConditionId, null);
        }
        //<3>.高亮显示并展示【指定节点s】
        treeMonitoring.highlightAndExpand(treeId, highlightNodes, flag, type);
    },
    highlightAndExpand: function (treeId, highlightNodes, flag, type) {
        var treeObj = $.fn.zTree.getZTreeObj(treeId);
        //<3>.把指定节点的样式更新为高亮显示，并展开
        if (highlightNodes != null) {
            for (var i = 0; i < highlightNodes.length; i++) {
                //高亮显示节点的父节点的父节点....直到根节点，并展示
                treeMonitoring.setFontCss(treeId, highlightNodes[i], type);
                var parentNode = highlightNodes[i].getParentNode();
            }
        }
    },
    // 递归得到指定节点的父节点的父节点....直到根节点
    getParentNodes: function (treeId, node) {
        if (node != null) {
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            var parentNode = node.getParentNode();
            return treeMonitoring.getParentNodes(treeId, parentNode);
        }
        return node;

    },
    // 设置树节点字体样式
    setFontCss: function (treeId, treeNode, type) {
        var treeObj = $.fn.zTree.getZTreeObj(treeId);
        //在线停车图标
        if (type == 1) {
            if (lineAndStop.isHas(treeNode.id)) {
                treeNode.iconSkin = "btnImage iconArea onlineParking";
                var nodeID = treeNode.tId + "_span";
                vnodesId.push(nodeID);
                treeObj.updateNodeIconSkin(treeNode);
            }
            if (nmoline.isHas(treeNode.id)) {
                treeNode.iconSkin = "btnImage iconArea offlineIcon";
                var nodeID = treeNode.tId + "_span";
                vnodemId.push(nodeID);
                treeObj.updateNodeIconSkin(treeNode);
            }
            if (lineAndmiss.isHas(treeNode.id)) {
                treeNode.iconSkin = "btnImage iconArea onlineNotPositioning";
                var nodeID = treeNode.tId + "_span";
                vnodelmId.push(nodeID);
                treeObj.updateNodeIconSkin(treeNode);
            }
            if (lineAndRun.isHas(treeNode.id)) {
                treeNode.iconSkin = "btnImage iconArea onlineDriving";
                var nodeID = treeNode.tId + "_span";
                vnoderId.push(nodeID);
                treeObj.updateNodeIconSkin(treeNode);
            }
            if (lineAndAlarm.isHas(treeNode.id)) {
                treeNode.iconSkin = "btnImage iconArea warning";
                var nodeID = treeNode.tId + "_span";
                vnodeaId.push(nodeID);
                treeObj.updateNodeIconSkin(treeNode);
            }
            if (overSpeed.isHas(treeNode.id)) {
                treeNode.iconSkin = "btnImage iconArea speedLimitWarning";
                var nodeID = treeNode.tId + "_span";
                vnodespId.push(nodeID);
                treeObj.updateNodeIconSkin(treeNode);
            }
            if (heartBeat.isHas(treeNode.id)) {
                treeNode.iconSkin = "btnImage iconArea heartBeatWarning";
                var nodeID = treeNode.tId + "_span";
                vnodespId.push(nodeID);
                treeObj.updateNodeIconSkin(treeNode);
            }
        }
        if (type == 4) {
            if (lineAndStop.isHas(treeNode.id)) {
                treeNode.iconSkin = "btnImage iconArea onlineParking";
                var nodeID = treeNode.tId + "_span";
                treeObj.updateNodeIconSkin(treeNode);
                $("#" + nodeID).css('color', '#c80002');
            } else if (lineAndmiss.isHas(treeNode.id)) {
                treeNode.iconSkin = "btnImage iconArea onlineNotPositioning";
                var nodeID = treeNode.tId + "_span";
                treeObj.updateNodeIconSkin(treeNode);
                $("#" + nodeID).css('color', '#754801');
            } else if (lineAndRun.isHas(treeNode.id)) {
                treeNode.iconSkin = "btnImage iconArea onlineDriving";
                var nodeID = treeNode.tId + "_span";
                treeObj.updateNodeIconSkin(treeNode);
                $("#" + nodeID).css('color', '#78af3a');
            } else if (lineAndAlarm.isHas(treeNode.id)) {
                treeNode.iconSkin = "btnImage iconArea warning";
                var nodeID = treeNode.tId + "_span";
                treeObj.updateNodeIconSkin(treeNode);
                $("#" + nodeID).css('color', '#ffab2d');
            } else if (overSpeed.isHas(treeNode.id)) {
                treeNode.iconSkin = "btnImage iconArea speedLimitWarning";
                var nodeID = treeNode.tId + "_span";
                treeObj.updateNodeIconSkin(treeNode);
                $("#" + nodeID).css('color', '#960ba3');
            } else if (nmoline.isHas(treeNode.id)) {
                treeNode.iconSkin = "btnImage iconArea offlineIcon";
                var nodeID = treeNode.tId + "_span";
                treeObj.updateNodeIconSkin(treeNode);
                $("#" + nodeID).css('color', '#b6b6b6');
            } else if (heartBeat.isHas(treeNode.id)) {
                treeNode.iconSkin = "btnImage iconArea heartBeatWarning";
                var nodeID = treeNode.tId + "_span";
                treeObj.updateNodeIconSkin(treeNode);
                $("#" + nodeID).css('color', '#fb8c96');
            }
        }
        if (type == 2) {
            treeObj.checkNode(treeNode, true, true);
            treeObj.expandNode(treeNode.getParentNode(), true, true, false, true)
        }
        if (type == 3) {
            treeObj.checkNode(treeNode, false, true);
        }
        if (type == 5) {
            treeObj.hideNodes(treeNode)
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
    //车个树取消点击事件
    zTreeBeforeClick: function () {
        return true;
    },
    //单击事件
    onClickV: function (e, treeId, treeNode) {
        var zTree = $.fn.zTree.getZTreeObj("treeDemo");
        if (treeNode.iconSkin != "assignmentSkin" && treeNode.iconSkin != "groupSkin") {
            zTree.selectNode(treeNode, false, true);
        }
        var nodes = zTree.getSelectedNodes(true);
        var nodeName = treeNode.id;
        treeMonitoring.centerMarkerNo();
        if (treeNode.checked) {
            treeMonitoring.centerMarker(nodes[0].id, 'CLICK');
        }
        $("#" + dbclickCheckedId).parent().removeAttr("class", "curSelectedNode_dbClick"); //单击时取消双击Style
        $("#" + ztreeStyleDbclick).children("a").removeAttr("class", "curSelectedNode_dbClick");
        $(".ztree li a").removeClass("curSelectedNode_dbClick");
        //得到当前单击车个的外层id信息
        onClickVId = e.target.id;
        if (treeNode.iconSkin != "assignmentSkin" && treeNode.iconSkin != "groupSkin") {
            $("#" + onClickVId).parent().attr("class", "curSelectedNode");
            $("#" + onClickVId).parent().attr("data-id", nodeName);
        }
        //单击下一个车取消上一个
        if (oldOnClickVId != "") {
            $("#" + oldOnClickVId).parent().removeAttr("class");
        }
        oldOnClickVId = onClickVId;
        //处理单击订阅同一个车
        if (oldOnClickVId = onClickVId) {
            $("#" + onClickVId).parent().attr("class", "curSelectedNode");
            $("#" + onClickVId).parent().attr("data-id", nodeName);
        }
        console.log(treeNode.type)
        dataTableOperation.tableHighlightBlue(treeNode.type, nodeName);
        //单击时判断节点是否勾选订阅 用于围栏查询
        treeMonitoring.vehicleTreeClickGetFenceInfo(treeNode.checked, treeNode.id);
    },
    //双击事件
    onDbClickV: function (e, treeId, treeNode) {
        dbClickHeighlight = true;
        var cheakdiyueall = [];
        var changedNodes;
        var param = [];
        var zTree = $.fn.zTree.getZTreeObj("treeDemo");
        if (treeNode) {
            if (treeNode.children === undefined && treeNode.type === "assignment" && treeNode.isParent === true) {
                treeMonitoring.zTreeOnExpand(e, treeId, treeNode);
                if (treeNode.children !== undefined) {
                    for (var m = 0; m < treeNode.children.length; m++) {
                        treeNode.children[m].checkedOld = false;
                    }
                }
            }
            if (treeNode.iconSkin != "assignmentSkin" && treeNode.iconSkin != "groupSkin") {
                zTree.selectNode(treeNode, false, true);
                if (treeNode.checkedOld == false) {
                    cheakedAll.push(treeNode.id);
                    curDbSubscribeMOnitor = treeNode.id;
                    cheakdiyueall.push(treeNode.id);
                }
            }
            licensePlateInformation = treeNode.id;
            DblclickName = licensePlateInformation;
            // 状态信息table表对应监控对象信息高亮
            dataTableOperation.tableHighlight(treeNode.type, licensePlateInformation);
            groupIconSkin = treeNode.iconSkin;
            var nodes = zTree.getSelectedNodes(true);
            if (nodes[0].checked == false) {
                cheakdiyuealls.push(nodes[0].id)
                var flag = treeMonitoring.getChannel(nodes, map);
                if (!flag) {
                    return true;
                }
            }
            zTree.checkNode(nodes[0], true, true);
            //
            dbclickCheckedId = e.target.id;
            if (treeNode.iconSkin != "assignmentSkin" && treeNode.iconSkin != "groupSkin") {
                $("#" + dbclickCheckedId).parent().attr("class", "curSelectedNode_dbClick");
            }
            //双击下一个车取消上一个
            if (oldDbclickCheckedId != "") {
                $("#" + oldDbclickCheckedId).parent().removeAttr("class");
            }
            oldDbclickCheckedId = dbclickCheckedId;
            //处理双击订阅同一个车
            if (oldDbclickCheckedId = dbclickCheckedId) {
                $("#" + dbclickCheckedId).parent().attr("class", "curSelectedNode_dbClick");
            }
            //创建车个对象参数信息(用于实时视频)
            var vehicleInfo = new Object();
            vehicleInfo.vid = treeNode.id;
            vehicleInfo.brand = treeNode.name;
            vehicleInfo.deviceNumber = treeNode.deviceNumber;
            vehicleInfo.plateColor = treeNode.plateColor;
            vehicleInfo.isVideo = treeNode.isVideo;

            vehicleInfo.simcardNumber = treeNode.simcardNumber

            subscribeVehicleInfo = vehicleInfo; //订阅的车个信息全局变量

            realTimeVideo.setVehicleInfo(vehicleInfo)

            activeSafety.riskInformationDetails(treeNode.id)

            // wjk
            // if (m_videoFlag == 1 && realTimeVideo.ieExplorer()) {
            //     //没有通话只有视频  开启视频
            //     realTimeVideo.windowSet();
            //     clearInterval(computingTimeInt)
            //     clearInterval(computingTimeCallInt)
            //     if ($('#btn-videoRealTime-show i').hasClass('active') && !$('#phoneCall i').hasClass('active')) {
            //         realTimeVideo.beventLiveView(pageLayout.computingTimeIntFun);
            //     }
            //     else if (!$('#btn-videoRealTime-show i').hasClass('active') && $('#phoneCall i').hasClass('active')) {
            //         realTimeVideo.beventLiveIpTalk(pageLayout.computingTimeCallIntFun);
            //     }
            //     else if ($('#btn-videoRealTime-show i').hasClass('active') && $('#phoneCall i').hasClass('active')) {
            //         realTimeVideo.beventLiveView(pageLayout.computingTimeIntFun);
            //         realTimeVideo.beventLiveIpTalk(pageLayout.computingTimeCallIntFun);
            //     }
            // }
            //end

            // authorwjk
            if (m_videoFlag == 1) {
                realtimeMonitoringVideoSeparate.closeTerminalVideo()
                realtimeMonitoringVideoSeparate.initVideoRealTimeShow(vehicleInfo);
            }

            if (nodes[0].type === "vehicle" || nodes[0].type === "people" || nodes[0].type === "thing") {
                var list = zTreeIdJson[nodes[0].id];
                if (list.length > 1) {
                    $.each(list, function (index, value) {
                        var treeNoded = zTree.getNodeByTId(value);
                        zTree.checkNode(treeNoded, true, true);
                        treeNoded.checkedOld = true;
                    })
                }
            }
            if (nodes[0].type == "assignment" || nodes[0].type == "group") {
                changedNodes = zTree.getChangeCheckedNodes();
                var count = 0;
                for (var i = 0, l = changedNodes.length; i < l; i++) {
                    changedNodes[i].checkedOld = true;
                    if (changedNodes[i].type === "vehicle" || changedNodes[i].type === "people" || changedNodes[i].type === "thing") {
                        var list = zTreeIdJson[changedNodes[i].id];
                        if (cheakedAll.length > 400) {
                            layer.alert("我们的监控上限是400个,您刚刚勾选数量超过了400个,请重新勾选！");
                            for (var j = 0, l = changedNodes.length; j < l; j++) {
                                // 只有一个节点
                                if (changedNodes.length == 1) {
                                    zTree.checkNode(changedNodes[j], false, true);
                                } else {
                                    // 如果某个分组树节点未展开，并且已经有勾选的数据，changedNodes中会包含当前树的所有节点
                                    if (j >= 1) {
                                        // cheakdiyueall已经剔除已勾选对象
                                        /*if ($.inArray(changedNodes[j].id, cheakdiyueall) !== -1) {*/
                                        zTree.checkNode(changedNodes[j], false, true);
                                        /*}*/
                                    } else if (j <= 0) {
                                        // 取消父节点勾选
                                        changedNodes[j].checked = false;
                                    }
                                }
                                changedNodes[j].checkedOld = false;
                                crrentSubV.remove(changedNodes[j].id);
                                cheakedAll.remove(changedNodes[j].id);
                            }
                            // crrentSubV 用于刷新左侧树后， 重新勾选已选择树节点,此处不能清空，否则无法勾选树节点。
                            // crrentSubV = [];
                            cheakdiyuealls = [];
                            return;
                        }
                        if (list.length > 1) {
                            $.each(list, function (index, value) {
                                var treeNoded = zTree.getNodeByTId(value);
                                zTree.checkNode(treeNoded, true, true);
                                treeNoded.checkedOld = true;
                            })
                        } else if (list.length == 1) {
                            zTree.checkNode(changedNodes[i], true, true);
                            changedNodes[i].checkedOld = true;
                        }
                        if ($.inArray(changedNodes[i].id, crrentSubV) === -1) {
                            count++;
                            cheakdiyueall.push(changedNodes[i].id);
                            crrentSubV.push(changedNodes[i].id)
                            crrentSubName.push(changedNodes[i].name);
                            cheakedAll.push(changedNodes[i].id)
                        }
                    }
                }
            }
            cheakdiyuealls = treeMonitoring.unique(cheakdiyueall);
            if (treeMonitoring.unique(cheakedAll).length <= 400) {
                var requestStrS = {
                    "desc": {
                        "MsgId": 40964,
                        "UserName": $("#userName").text(),
                        "isAppFlag": false
                    },
                    "data": cheakdiyuealls
                };
                cancelList = [];
                // 状态信息
                webSocket.subscribe(headers, "/user/topic/location", dataTableOperation.updateRealLocation, "/app/location/subscribe", requestStrS);
            } else {
                layer.alert("为了更好的性能,请少于400个监控对象,您刚刚勾选了" + treeMonitoring.unique(cheakedAll).length + "个,请重新勾选！")
                for (var i = 0, l = changedNodes.length; i < l; i++) {
                    // 只有一个节点
                    if (changedNodes.length == 1) {
                        zTree.checkNode(changedNodes[i], false, true);
                    } else {
                        // 如果某个分组树节点未展开，并且已经有勾选的数据，changedNodes中会包含当前树的所有节点
                        if (i >= 1) {
                            // cheakdiyueall已经剔除已勾选对象
                            if ($.inArray(changedNodes[i].id, cheakdiyueall) !== -1) {
                                zTree.checkNode(changedNodes[i], false, true);
                            }
                        } else if (i <= 0) {
                            // 取消父节点勾选
                            changedNodes[i].checked = false;
                        }
                    }
                    changedNodes[i].checkedOld = false;
                    crrentSubV.remove(changedNodes[i].id);
                    cheakedAll.remove(changedNodes[i].id);
                }
                // crrentSubV 用于刷新左侧树后， 重新勾选已选择树节点,此处不能清空，否则无法勾选树节点。
                // crrentSubV = [];
                cheakdiyuealls = [];
            }
            nodes[0].checkedOld = nodes[0].checked;
            treeMonitoring.markerTimeout(nodes);
            treeMonitoring.realTimeDatatAdapt(nodes[0].type);
            //双击时判断节点是否勾选订阅 用于围栏查询
            treeMonitoring.vehicleTreeClickGetFenceInfo(treeNode.checked, treeNode.id)
        }
    },
    // 双击聚焦轮询方法
    markerTimeout: function (nodes) {
        setTimeout(function () {
            var id = nodes[0].id;
            if (markerAllUpdateData.containsKey(id)) {
                treeMonitoring.centerMarker(id, 'DBLCLICK');
            } else if (objAddressIsTrue.indexOf(id) != -1) {
                return true;
            } else {
                treeMonitoring.markerTimeout(nodes);
            }
        }, 250);
    },
    //组织树勾选
    onCheckVehicle: function (e, treeId, treeNode) {
        var zTree = $.fn.zTree.getZTreeObj("treeDemo");
        var curSelect = $('.curSelectedNode');
        var vName = treeNode.name;
        var vId = treeNode.id;

        /*var parNode=treeNode.getParentNode();
         if(!treeNode.isParent){
         if(parNode!=null&&parNode!=undefined) {
         if(parNode.check_Child_State==0){
         parNode.halfCheck=false;
         parNode.checked=false;
         parNode.checkedOld=false;
         }else if(parNode.check_Child_State==1){
         parNode.halfCheck=true;
         parNode.checked=false;
         parNode.checkedOld=false;
         }else if(parNode.check_Child_State==2){
         parNode.halfCheck=false;
         parNode.checked=true;
         parNode.checkedOld=true;
         }
         }
         }else{
         if(treeNode.check_Child_State==0){
         treeNode.halfCheck=false;
         treeNode.checked=false;
         treeNode.checkedOld=false;
         }else if(treeNode.check_Child_State==1){
         treeNode.halfCheck=true;
         treeNode.checked=false;
         treeNode.checkedOld=false;
         }else if(treeNode.check_Child_State==2){
         treeNode.halfCheck=false;
         treeNode.checked=true;
         treeNode.checkedOld=true;
         }
         }*/
        if (treeNode.children === undefined && treeNode.type === "assignment" && treeNode.isParent === true) {
            treeMonitoring.zTreeOnExpand(e, treeId, treeNode);
            if (treeNode.children !== undefined) {
                for (var m = 0; m < treeNode.children.length; m++) {
                    treeNode.children[m].checkedOld = false;
                }
            }
        } else if (treeNode.type === "group" && allflag) {
            treeMonitoring.zTreeOnExpand(e, treeId, treeNode);
            // 查找企业下的监控对象集合并重置其勾选状态
            var paramNode = zTree.getNodesByFilter(function (node) {
                return (node.type == 'vehicle' || node.type == 'people' || node.type == 'thing');
            });
            for (var i = 0; i < paramNode.length; i++) {
                paramNode[i].checkedOld = false;
            }
        }
        var changedNodes = zTree.getChangeCheckedNodes();
        var cancelVehicle = [];//被取消的车个
        var subVeh = [];//订阅的车个
        var subName = [];
        var cheakdiyueall = [];

        for (var i = 0, l = changedNodes.length; i < l; i++) {
            if ((changedNodes[i].type === "vehicle" || changedNodes[i].type === "people" || changedNodes[i].type === "thing") && changedNodes[i].isHidden == false) {
                if (cheakedAll.length > 400) {
                    layer.alert("我们的监控上限是400个,您刚刚勾选数量超过了400个,请重新勾选！");
                    for (var j = 0, l = changedNodes.length; j < l; j++) {
                        if (!crrentSubV.contains(changedNodes[j].id)) {
                            if (changedNodes.length == 1) {
                                zTree.checkNode(changedNodes[j], false, true);
                            }
                            if (changedNodes.length > 1 && j >= 1) {
                                zTree.checkNode(changedNodes[j], false, true);
                            } else if (changedNodes.length > 1 && j == 0) {
                                changedNodes[j].checked = false;
                            }
                            changedNodes[j].checkedOld = false;
                            cheakedAll.remove(changedNodes[j].id)
                        }
                    }
                    checkedVehicles = [];
                    return;
                }
                if (changedNodes[i].checked == false) {

                    //取消订阅
                    if (subscribeVehicleInfo && changedNodes[i].id == subscribeVehicleInfo.vid) {
                        subscribeVehicleInfo = null;

                        realtimeMonitoringVideoSeparate.closeTerminalVideo();//关闭当前视频
                    }

                    cancelVehicle.push(changedNodes[i].id);
                    cheakedAll.remove(changedNodes[i].id);
                    carNameContentLUMap.remove(changedNodes[i].id);
                } else if (!subVeh.contains(changedNodes[i].id)) {
                    subVeh.push(changedNodes[i].id);
                    subName.push(changedNodes[i].name);
                    cheakdiyueall.push(changedNodes[i].id);
                    cheakedAll.push(changedNodes[i].id)
                }
                var list = zTree.getNodesByParam('id', changedNodes[i].id, null);
                if (list !== undefined) {
                    for (var j = 0; j < list.length; j++) {
                        zTree.checkNode(list[j], treeNode.checked, true);
                        list[j].checkedOld = treeNode.checked;
                    }
                } else {
                    changedNodes[i].checkedOld = changedNodes[i].checked;
                }
            } else {
                changedNodes[i].checkedOld = changedNodes[i].checked;
            }
        }
        var param = [];
        cheakdiyuealls = treeMonitoring.unique(cheakdiyueall);
        checkedVehicles = $.merge(checkedVehicles, cheakdiyuealls);//合并
        checkedVehicles.sort();//排序
        for (var i = 0; i < checkedVehicles.length - 1; i++) {//去重
            if (checkedVehicles[i] == checkedVehicles[i + 1]) {
                checkedVehicles.remove(checkedVehicles[i]);
            }
        }
        for (var i = 0; i < cancelVehicle.length; i++) {
            checkedVehicles.remove(cancelVehicle[i]);
        }
        if (treeMonitoring.unique(cheakedAll).length <= 400) {
            var cCheacked = Array.minus(cheakdiyuealls, cheakNodec)
            cheakNodec = cheakdiyuealls;
            var requestStrS = {
                "desc": {
                    "MsgId": 40964,
                    "UserName": $("#userName").text(),
                    "isAppFlag": false
                },
                "data": cCheacked
            };
            var offlineVids = [];
            if (treeNode.checked) {//订阅
                cancelList = [];
                if (subVeh.length > 0) {
                    for (var i = 0; i < subVeh.length; i++) {
                        if (crrentSubV.indexOf(subVeh[i]) == -1) {
                            crrentSubV.push(subVeh[i]);
                            crrentSubName.push(subName[i])
                        }
                    }
                } else if (treeNode.type === "vehicle" || treeNode.type === "people" || treeNode.type === "thing") {
                    crrentSubV.push(treeNode.id);
                    crrentSubName.push(treeNode.name);
                }
                webSocket.subscribe(headers, "/user/topic/location", dataTableOperation.updateRealLocation, "/app/location/subscribe", requestStrS);
                treeMonitoring.realTimeDatatAdapt(treeNode.type);
                var treeType = treeNode.type;
            } else {//取消订阅

                if ((treeNode.type == "assignment") && treeNode.children != undefined) {
                    for (var i = 0; i < treeNode.children.length; i++) {
                        if (treeNode.children[i].isHidden === false) {
                            crrentSubV.removeObj(treeNode.children[i].id)
                            crrentSubName.removeObj(treeNode.children[i].name);
                        }
                    }
                } else if (treeNode.type == "vehicle" || treeNode.type == "people" || treeNode.type == "thing") {
                    crrentSubV.removeObj(treeNode.id)
                    crrentSubName.removeObj(treeNode.name);
                }
                cheakNodec = [];
                param = [];
                var plateNumbers = [];
                if (treeNode.type == "vehicle" || treeNode.type == "people" || treeNode.type == "thing") {
                    plateNumbers.push(treeNode.id);
                    param.push(treeNode.id);
                } else if (treeNode.type == "assignment") {
                    treeMonitoring.getCancelNodes(changedNodes, param, plateNumbers);
                    param = treeMonitoring.removeDuplicates(param);
                } else if (treeNode.type == "group") {
                    for (var i = 0, l = changedNodes.length; i < l; i++) {
                        if ((changedNodes[i].type === "vehicle" || changedNodes[i].type === "people" || changedNodes[i].type == "thing") && changedNodes[i].checked == false) {
                            crrentSubV.removeObj(changedNodes[i].id)
                            crrentSubName.removeObj(changedNodes[i].name)
                            param.push(changedNodes[i].id);
                            plateNumbers.push(changedNodes[i].id)
                        }
                    }
                } else if (treeNode.type == "group" && treeNode.pId == "null") {
                    plateNumbers.push(crrentSubV);
                }
                plateNumbers = treeMonitoring.removeDuplicates(plateNumbers);
                cancelList = plateNumbers;
                dataTableOperation.deleteRowByRealTime(cancelList);
                //取消订阅去掉隐藏车牌号
                for (var i = 0; i < param.length; i++) {
                    var isID = param[i];
                    if (carNameMarkerContentMap.containsKey(isID)) {
                        var thisNameMarker = carNameMarkerContentMap.get(isID);
                        thisNameMarker.stopMove()
                        map.remove([thisNameMarker]);
                    }
                }
                var cancelStrS = {
                    "desc": {
                        "MsgId": 40964,
                        "UserName": $("#userName").text()
                    },
                    "data": param
                };
                webSocket.unsubscribealarm(headers, "/app/location/unsubscribe", cancelStrS);
                treeMonitoring.searchByFlag("treeDemo", treeNode.id, null, 3);
                treeMonitoring.clearMarker(param);
                //取消订阅之后取消单击和双击背景
                var curHighTarget = $("#" + oldDbclickCheckedId).parent();
                var len = $('#' + treeNode.tId).find(curHighTarget).length;//勾选的是双击选中监控对象的上级分组或企业
                if (vId == curHighTarget.attr('data-id') || len > 0) {
                    curHighTarget.removeAttr("class");
                }
                var selectLen = $('#' + treeNode.tId).find('a.curSelectedNode').length;//勾选的是单击选中监控对象的上级分组或企业
                if (selectLen > 0) {
                    $(".ztree li a").removeAttr("class", "curSelectedNode");
                }
                //取消订阅时 使用此Fn判断围栏模块显示隐藏
                treeMonitoring.vehicleTreeClickGetFenceInfo(treeNode.checked, treeNode.id);
                // 删除未定位监控对象元素
                for (var z = 0, zlen = param.length; z < zlen; z++) {
                    var zIndex = objAddressIsTrue.indexOf(param[z]);
                    if (zIndex != -1) {
                        objAddressIsTrue.splice(zIndex, 1);
                    }
                }
                amapOperation.markerStateListening();
            }
        } else {
            layer.alert("我们的监控上限是400个,您刚刚勾选了" + treeMonitoring.unique(cheakedAll).length + "个,请重新勾选！")
            for (var i = 0, l = changedNodes.length; i < l; i++) {
                if (!crrentSubV.contains(changedNodes[i].id)) {
                    if (changedNodes.length == 1) {
                        zTree.checkNode(changedNodes[i], false, true);
                    }
                    if (changedNodes.length > 1 && i >= 1) {
                        zTree.checkNode(changedNodes[i], false, true);
                    } else if (changedNodes.length > 1 && i == 0) {
                        changedNodes[i].checked = false;
                    }
                    changedNodes[i].checkedOld = false;
                    cheakedAll.remove(changedNodes[i].id)
                }
            }
            checkedVehicles = [];
        }
    },
    removeDuplicates: function (arr) {
        var result = [];
        for (var i = 0, n = arr.length; i < n; i++) {
            if (!result.isHas(arr[i])) {
                result.push(arr[i]);
            }
        }
        return result;
    },
    getCancelNodes: function (changedNodes, param, plateNumbers) {
        for (var i = 0; i < changedNodes.length; i++) {
            if ((changedNodes[i].type == 'vehicle' || changedNodes[i].type == 'people' || changedNodes[i].type == 'thing') && changedNodes[i].isHidden == false && changedNodes[i].checked == false) {
                plateNumbers.push(changedNodes[i].id);
                param.push(changedNodes[i].id);
            }
        }
    },
    getChannel: function (fenceNode, showMap) {
        if (fenceNode == null || fenceNode.length == 0 || (fenceNode[0].type !== 'vehicle' && fenceNode[0].type !== 'people' && fenceNode[0].type !== 'thing')) {
            return true;
        }
        if (treeMonitoring.unique(cheakedAll).length <= 400) {
            if ($.inArray(fenceNode[0].id, crrentSubV) === -1) {
                crrentSubV.push(fenceNode[0].id)
                crrentSubName.push(fenceNode[0].name);
            }
            cancelList = [];
            var requestStr = {
                "desc": {
                    "MsgId": 40964,
                    "UserName": $("#userName").text(),
                    "isAppFlag": false
                },
                "data": [fenceNode[0].id]
            };
            //状态信息
            webSocket.subscribe(headers, "/user/topic/location", dataTableOperation.updateRealLocation, "/app/location/subscribe", requestStr);
            return true;
        }
        zTree.checkNode(fenceNode, false, true);
        layer.alert("为了更好的性能,请少于400个监控对象,您刚刚勾选了" + treeMonitoring.unique(cheakedAll).length + "个,请重新勾选！");
        cheakedAll.remove(fenceNode[0].id);
        // crrentSubV = [];
        // cheakdiyuealls = [];
        return false;


    },
    // 实时更新监控对象树状态
    updataRealTree: function (msg) {
        var zTree = $.fn.zTree.getZTreeObj("treeDemo");
        if (suFlag == true && initArrIsInit == true) {
            for (var j = 0; j < initArr.length; j++) {
                zTree.expandNode(notExpandNodeInit[j], true, true, false, true);
            }
            ;
            suFlag = false;
        }

        var data = $.parseJSON(msg.body);
        var msgID = data.desc.msgID;
        if (msgID == 39321) {
            var position = data.data;
            for (var i = 0; i < position.length; i++) {
                if (position[i].vehicleStatus != 3) {
                    if (lineV.indexOf(position[i].vehicleId) == -1) {
                        lineV.push(position[i].vehicleId);
                    }
                }
                lineVid = treeMonitoring.unique(lineV);
                if (position[i].vehicleStatus == 4) {
                    if (lineAs.indexOf(position[i].vehicleId) == -1) {
                        lineAs.push(position[i].vehicleId);
                    }
                    lineAr.remove(position[i].vehicleId);
                    lineAa.remove(position[i].vehicleId);
                    lineAm.remove(position[i].vehicleId);
                    changeMiss.remove(position[i].vehicleId);
                    lineOs.remove(position[i].vehicleId);
                    lineHb.remove(position[i].vehicleId);
                } else if (position[i].vehicleStatus == 10) {
                    if (lineAr.indexOf(position[i].vehicleId) == -1) {
                        lineAr.push(position[i].vehicleId);
                    }
                    lineAs.remove(position[i].vehicleId);
                    lineAa.remove(position[i].vehicleId);
                    lineAm.remove(position[i].vehicleId);
                    changeMiss.remove(position[i].vehicleId);
                    lineOs.remove(position[i].vehicleId);
                    lineHb.remove(position[i].vehicleId);
                } else if (position[i].vehicleStatus == 5) {
                    if (lineAa.indexOf(position[i].vehicleId) == -1) {
                        lineAa.push(position[i].vehicleId);
                    }
                    lineAr.remove(position[i].vehicleId);
                    lineAs.remove(position[i].vehicleId);
                    lineAm.remove(position[i].vehicleId);
                    changeMiss.remove(position[i].vehicleId);
                    lineOs.remove(position[i].vehicleId);
                    lineHb.remove(position[i].vehicleId);
                } else if (position[i].vehicleStatus == 2) {
                    if (lineAm.indexOf(position[i].vehicleId) == -1) {
                        lineAm.push(position[i].vehicleId);
                    }
                    lineAr.remove(position[i].vehicleId);
                    lineAs.remove(position[i].vehicleId);
                    lineAa.remove(position[i].vehicleId);
                    changeMiss.remove(position[i].vehicleId);
                    lineOs.remove(position[i].vehicleId);
                    lineHb.remove(position[i].vehicleId);
                } else if (position[i].vehicleStatus == 3) {//未上线
                    if (changeMiss.indexOf(position[i].vehicleId) == -1) {
                        changeMiss.push(position[i].vehicleId)
                    }
                    lineAm.remove(position[i].vehicleId);
                    lineAr.remove(position[i].vehicleId);
                    lineAs.remove(position[i].vehicleId);
                    lineAa.remove(position[i].vehicleId);
                    lineOs.remove(position[i].vehicleId);
                    lineHb.remove(position[i].vehicleId);
                } else if (position[i].vehicleStatus == 9) {//超速
                    if ($.inArray(position[i].vehicleId, lineOs) === -1) {
                        lineOs.push(position[i].vehicleId)
                        changeMiss.remove(position[i].vehicleId)
                        lineAm.remove(position[i].vehicleId);
                        lineAr.remove(position[i].vehicleId);
                        lineAs.remove(position[i].vehicleId);
                        lineAa.remove(position[i].vehicleId);
                        lineHb.remove(position[i].vehicleId);
                    }
                } else if (position[i].vehicleStatus == 11) {//心跳
                    if (lineHb.indexOf(position[i].vehicleId) == -1) {
                        lineHb.push(position[i].vehicleId);
                    }
                    changeMiss.remove(position[i].vehicleId);
                    lineAm.remove(position[i].vehicleId);
                    lineAr.remove(position[i].vehicleId);
                    lineAs.remove(position[i].vehicleId);
                    lineAa.remove(position[i].vehicleId);
                    lineOs.remove(position[i].vehicleId);
                }
            }
            missVid = changeMiss;
            lineAndStop = lineAs;//停车
            lineAndRun = lineAr;//行驶
            lineAndAlarm = lineAa;////报警
            lineAndmiss = lineAm;//未定位
            overSpeed = lineOs;//超速
            heartBeat = lineHb;//心跳
            if (lineVid != null) {
                nmoline = Array.minus(diyueall, lineVid);
            } else {
                nmoline = diyueall;
            }
            neverOline = Array.minus(nmoline, missVid);
            var attrs;
            if (lineAndStop.length != 0) {
                var len = lineAndStop.length;
                for (var i = 0; i < len; i++) {
                    clineAndStop = lineAndStop[i];
                    var list = zTreeIdJson[clineAndStop];
                    if (list != null) {
                        $.each(list, function (index, value) {
                            var treeNode = zTree.getNodeByTId(value);
                            treeNode.iconSkin = "btnImage iconArea onlineParking"
                            zTree.updateNodeIconSkin(treeNode);
                            $("#" + value + "_span")[0].style.color = "#c80002";
                            if (misstype) {
                                zTree.hideNode(treeNode);
                            }
                        })
                    }
                }
            }
            if (lineAndmiss.length != 0) {
                var len = lineAndmiss.length;
                for (var i = 0; i < len; i++) {
                    clineAndmiss = lineAndmiss[i];
                    var list = zTreeIdJson[clineAndmiss];
                    if (list != null) {
                        $.each(list, function (index, value) {
                            var treeNode = zTree.getNodeByTId(value);
                            treeNode.iconSkin = "button btnImage iconArea onlineNotPositioning"
                            zTree.updateNodeIconSkin(treeNode);
                            $("#" + value + "_span")[0].style.color = "#754801";
                            if (misstype) {
                                zTree.hideNode(treeNode);
                            }
                        })
                    }
                }
            }
            if (lineAndRun.length != 0) {
                var len = lineAndRun.length;
                for (var i = 0; i < len; i++) {
                    clineAndRun = lineAndRun[i];
                    var list = zTreeIdJson[clineAndRun];
                    if (list != null) {
                        $.each(list, function (index, value) {
                            var treeNode = zTree.getNodeByTId(value);
                            treeNode.iconSkin = "button btnImage iconArea onlineDriving"
                            zTree.updateNodeIconSkin(treeNode);
                            $("#" + value + "_span")[0].style.color = "#78af3a";
                            if (misstype) {
                                zTree.hideNode(treeNode);
                            }
                        })
                    }
                }
            }
            if (lineAndAlarm.length != 0) {
                var len = lineAndAlarm.length;
                for (var i = 0; i < len; i++) {
                    clineAndAlarm = lineAndAlarm[i];
                    var list = zTreeIdJson[clineAndAlarm];
                    if (list != null) {
                        $.each(list, function (index, value) {
                            var treeNode = zTree.getNodeByTId(value);
                            treeNode.iconSkin = "button btnImage iconArea warning"
                            zTree.updateNodeIconSkin(treeNode);
                            $("#" + value + "_span")[0].style.color = "#ffab2d";
                            if (misstype) {
                                zTree.hideNode(treeNode);
                            }
                        })
                    }
                }

            }
            if (overSpeed.length != 0) {
                var len = overSpeed.length;
                for (var i = 0; i < len; i++) {
                    coverSpeed = overSpeed[i];
                    var list = zTreeIdJson[coverSpeed];
                    if (list != null) {
                        $.each(list, function (index, value) {
                            var treeNode = zTree.getNodeByTId(value);
                            treeNode.iconSkin = "btnImage iconArea speedLimitWarning"
                            zTree.updateNodeIconSkin(treeNode);
                            $("#" + value + "_span")[0].style.color = "#960ba3";
                            if (misstype) {
                                zTree.hideNode(treeNode);
                            }
                        })
                    }
                }
            }
            if (heartBeat.length != 0) {
                var len = heartBeat.length;
                for (var i = 0; i < len; i++) {
                    var heartbeatValue = heartBeat[i];
                    var list = zTreeIdJson[heartbeatValue];
                    if (list != null) {
                        $.each(list, function (index, value) {
                            var treeNode = zTree.getNodeByTId(value);
                            treeNode.iconSkin = "btnImage iconArea heartBeatWarning"
                            zTree.updateNodeIconSkin(treeNode);
                            $("#" + value + "_span")[0].style.color = "#fb8c96";
                            if (misstype) {
                                zTree.hideNode(treeNode);
                            }
                        })
                    }
                }
            }
            if (misstypes) {
                var nodesList = [];
                var treeNodeChildren = treeNodeNew.children;
                $.each(treeNodeChildren, function (index, value) {
                    if (value.isHidden == false) {
                        nodesList.push(value.id);
                    }
                });
                address_submit("POST", "/clbs/m/functionconfig/fence/bindfence/getNodesList", "json", true, {"nodesList": nodesList}, true, treeMonitoring.getNodesList);
            }
            var Vjiaoji = Array.intersect(lineVid, diyueall);
            var vmiss = params.length - Vjiaoji.length;
        } else if (msgID == 34952) {//新增
            var upPosition = data.data;
            if (upPosition[0].vehicleStatus == 4) {
                if (lineAndStop.indexOf(upPosition[0].vehicleId) == -1) {
                    lineAndStop.push(upPosition[0].vehicleId);
                }
                nmoline.remove(upPosition[0].vehicleId)
                treeMonitoring.searchByFlag("treeDemo", upPosition[0].vehicleId, null, 4);
            } else if (upPosition[0].vehicleStatus == 10) {
                if (lineAndRun.indexOf(upPosition[0].vehicleId) == -1) {
                    lineAndRun.push(upPosition[0].vehicleId);
                }
                // if (nmoline.isHas(upPosition[0].vehicleId)) {
                nmoline.remove(upPosition[0].vehicleId)
                // }
                treeMonitoring.searchByFlag("treeDemo", upPosition[0].vehicleId, null, 4);
            } else if (upPosition[0].vehicleStatus == 5) {
                if (lineAndAlarm.indexOf(upPosition[0].vehicleId) == -1) {
                    lineAndAlarm.push(upPosition[0].vehicleId);
                }
                // if (nmoline.isHas(upPosition[0].vehicleId)) {
                nmoline.remove(upPosition[0].vehicleId)
                // }
                treeMonitoring.searchByFlag("treeDemo", upPosition[0].vehicleId, null, 4);
            } else if (upPosition[0].vehicleStatus == 2) {
                if (lineAndmiss.indexOf(upPosition[0].vehicleId) == -1) {
                    lineAndmiss.push(upPosition[0].vehicleId);
                }

                // if (nmoline.isHas(upPosition[0].vehicleId)) {
                nmoline.remove(upPosition[0].vehicleId)
                // }
                treeMonitoring.searchByFlag("treeDemo", upPosition[0].vehicleId, null, 4);
            } else if (upPosition[0].vehicleStatus == 3) { // 离线
                if (changeMiss.indexOf(upPosition[0].vehicleId) == -1) {
                    changeMiss.push(upPosition[0].vehicleId);
                }
                treeMonitoring.objHeartbeatChange(upPosition[0].vehicleId, 3);
            } else if (upPosition[0].vehicleStatus == 9) {
                if (overSpeed.indexOf(upPosition[0].vehicleId) == -1) {
                    overSpeed.push(upPosition[0].vehicleId);
                }

                // if (nmoline.isHas(upPosition[0].vehicleId)) {
                nmoline.remove(upPosition[0].vehicleId)
                // }
                treeMonitoring.searchByFlag("treeDemo", upPosition[0].vehicleId, null, 4);
            } else if (upPosition[0].vehicleStatus == 11) {
                if (heartBeat.indexOf(upPosition[0].vehicleId) == -1) {
                    heartBeat.push(upPosition[0].vehicleId);
                }
                // if (nmoline.isHas(upPosition[0].vehicleId)) {
                nmoline.remove(upPosition[0].vehicleId)
                // }
                treeMonitoring.searchByFlag("treeDemo", upPosition[0].vehicleId, null, 4);
                treeMonitoring.objHeartbeatChange(upPosition[0].vehicleId, 11);
            }
            var list = zTreeIdJson[upPosition[0].vehicleId];
            var brand = null;
            if (list != null) {
                $.each(list, function (index, value) {
                    brand = zTree.getNodeByTId(value).name;
                })
            }
            if (diyueall.isHas(upPosition[0].vehicleId)) {
                if (upPosition[0].speed < 1) {
                    $tableCarStop.text(parseInt($tableCarStop.text()) + 1);
                    if (stopVidArray.indexOf(upPosition[0].vehicleId) == -1) {
                        stopVidArray.push(upPosition[0].vehicleId);
                    }
                } else {
                    $tableCarRun.text(parseInt($tableCarRun.text()) + 1);
                    if (runVidArray.indexOf(upPosition[0].vehicleId) == -1) {
                        runVidArray.push(upPosition[0].vehicleId);
                    }
                }
                ;
                $("#tline").text("(" + (parseInt($tableCarRun.text()) + parseInt($tableCarStop.text())) + ")");
                $tableCarOnline.text(parseInt($tableCarRun.text()) + parseInt($tableCarStop.text()));
                $tableCarOffline.text(parseInt($tableCarAll.text()) - parseInt($tableCarOnline.text()));
                $tableCarOnlinePercent.text(((parseInt($tableCarOnline.text()) / parseInt($tableCarAll.text())) * 100).toFixed(2) + "%");
                $("#tmiss").text("(" + parseInt($tableCarOffline.text()) + ")");
                if (upPosition[0].brand.length > 8) {
                    upPosition[0].brand = upPosition[0].brand.substring(0, 7) + '...';
                }
                $("#fixSpan").text(upPosition[0].brand + "  " + "  已上线");
                $(".btn-videoRealTime").show();
                $("#fixArea").show();
                if ($("#recentlyC").children().length >= 10) {
                    $($("#recentlyC").children().get(0)).remove();
                }
                $("#recentlyC").append("<p class='carStateShow'>" + $("#fixSpan").text() + "</p>")
            }
        } else if (msgID == 30583) {//更新
            var upPosition = data.data;
            if (upPosition !== null) {
                if (diyueall.isHas(upPosition[0].vehicleId)) {
                    if (upPosition[0].vehicleStatus != 3) {
                        if (upPosition[0].speed < 1) {
                            if (stopVidArray.indexOf(upPosition[0].vehicleId) == -1) {
                                $tableCarStop.text(parseInt($tableCarStop.text()) + 1);
                                $tableCarRun.text(parseInt($tableCarRun.text()) - 1);
                                if (stopVidArray.indexOf(upPosition[0].vehicleId) == -1) {
                                    stopVidArray.push(upPosition[0].vehicleId);
                                }
                                runVidArray.splice(runVidArray.indexOf(upPosition[0].vehicleId), 1);
                            }
                            ;
                        } else {
                            if (runVidArray.indexOf(upPosition[0].vehicleId) == -1) {
                                $tableCarStop.text(parseInt($tableCarStop.text()) - 1);
                                $tableCarRun.text(parseInt($tableCarRun.text()) + 1);
                                if (runVidArray.indexOf(upPosition[0].vehicleId) == -1) {
                                    runVidArray.push(upPosition[0].vehicleId);
                                }
                                stopVidArray.splice(stopVidArray.indexOf(upPosition[0].vehicleId), 1);
                            }
                        }
                        ;
                    }
                    ;
                }
                if (upPosition[0].vehicleStatus == 4) {//停止
                    lineAndRun.remove(upPosition[0].vehicleId);
                    lineAndAlarm.remove(upPosition[0].vehicleId);
                    lineAndmiss.remove(upPosition[0].vehicleId);
                    overSpeed.remove(upPosition[0].vehicleId);
                    nmoline.remove(upPosition[0].vehicleId);
                    heartBeat.remove(upPosition[0].vehicleId);
                    if (lineAndStop.indexOf(upPosition[0].vehicleId) == -1) {
                        lineAndStop.push(upPosition[0].vehicleId);
                    }
                    treeMonitoring.searchByFlag("treeDemo", upPosition[0].vehicleId, null, 4);
                } else if (upPosition[0].vehicleStatus == 10) {
                    lineAndStop.remove(upPosition[0].vehicleId);
                    lineAndAlarm.remove(upPosition[0].vehicleId);
                    lineAndmiss.remove(upPosition[0].vehicleId);
                    overSpeed.remove(upPosition[0].vehicleId);
                    nmoline.remove(upPosition[0].vehicleId);
                    heartBeat.remove(upPosition[0].vehicleId);
                    if (lineAndRun.indexOf(upPosition[0].vehicleId) == -1) {
                        lineAndRun.push(upPosition[0].vehicleId);
                    }
                    treeMonitoring.searchByFlag("treeDemo", upPosition[0].vehicleId, null, 4);
                } else if (upPosition[0].vehicleStatus == 5) {
                    lineAndStop.remove(upPosition[0].vehicleId);
                    lineAndRun.remove(upPosition[0].vehicleId);
                    lineAndmiss.remove(upPosition[0].vehicleId);
                    overSpeed.remove(upPosition[0].vehicleId);
                    nmoline.remove(upPosition[0].vehicleId);
                    heartBeat.remove(upPosition[0].vehicleId);
                    if (lineAndAlarm.indexOf(upPosition[0].vehicleId) == -1) {
                        lineAndAlarm.push(upPosition[0].vehicleId);
                    }
                    treeMonitoring.searchByFlag("treeDemo", upPosition[0].vehicleId, null, 4);
                } else if (upPosition[0].vehicleStatus == 2) {
                    lineAndStop.remove(upPosition[0].vehicleId);
                    lineAndRun.remove(upPosition[0].vehicleId);
                    lineAndAlarm.remove(upPosition[0].vehicleId);
                    overSpeed.remove(upPosition[0].vehicleId);
                    nmoline.remove(upPosition[0].vehicleId);
                    heartBeat.remove(upPosition[0].vehicleId);
                    if (lineAndmiss.indexOf(upPosition[0].vehicleId) == -1) {
                        lineAndmiss.push(upPosition[0].vehicleId);
                    }
                    treeMonitoring.searchByFlag("treeDemo", upPosition[0].vehicleId, null, 4);
                } else if (upPosition[0].vehicleStatus == 3) {//离线
                    treeMonitoring.objHeartbeatChange(upPosition[0].vehicleId, 3);
                    lineAndStop.remove(upPosition[0].vehicleId);
                    lineAndRun.remove(upPosition[0].vehicleId);
                    lineAndAlarm.remove(upPosition[0].vehicleId);
                    lineAndmiss.remove(upPosition[0].vehicleId);
                    overSpeed.remove(upPosition[0].vehicleId);
                    heartBeat.remove(upPosition[0].vehicleId);
                    if (nmoline.indexOf(upPosition[0].vehicleId) == -1) {
                        nmoline.push(upPosition[0].vehicleId);
                    }
                    treeMonitoring.searchByFlag("treeDemo", upPosition[0].vehicleId, null, 4);
                    var list = zTreeIdJson[upPosition[0].vehicleId];
                    var brand = null;
                    if (list != null) {
                        $.each(list, function (index, value) {
                            brand = zTree.getNodeByTId(value).name;
                        })
                    }
                    if (stopVidArray.isHas(upPosition[0].vehicleId)) {
                        $tableCarStop.text(parseInt($tableCarStop.text()) - 1);
                        stopVidArray.splice(stopVidArray.indexOf(upPosition[0].vehicleId), 1);
                    }
                    if (runVidArray.isHas(upPosition[0].vehicleId)) {
                        $tableCarRun.text(parseInt($tableCarRun.text()) - 1);
                        runVidArray.splice(runVidArray.indexOf(upPosition[0].vehicleId), 1);
                    }
                    if (diyueall.isHas(upPosition[0].vehicleId)) {
                        $("#tline").text("(" + (parseInt($tableCarRun.text()) + parseInt($tableCarStop.text())) + ")");
                        $tableCarOnline.text(parseInt($tableCarRun.text()) + parseInt($tableCarStop.text()));
                        $tableCarOffline.text(parseInt($tableCarAll.text()) - parseInt($tableCarOnline.text()));
                        $tableCarOnlinePercent.text(((parseInt($tableCarOnline.text()) / parseInt($tableCarAll.text())) * 100).toFixed(2) + "%");
                        $("#tmiss").text("(" + parseInt($tableCarOffline.text()) + ")");
                        if (upPosition[0].brand.length > 8) {
                            upPosition[0].brand = upPosition[0].brand.substring(0, 7) + '...';
                        }
                        $("#fixSpan").text(upPosition[0].brand + "  " + "  已下线");
                        $(".btn-videoRealTime").show();
                        $("#fixArea").show();
                        if ($("#recentlyC").children().length >= 10) {
                            $($("#recentlyC").children().get(0)).remove();
                        }
                        $("#recentlyC").append("<p class='carStateShow'>" + $("#fixSpan").text() + "</p>")
                    }
                } else if (upPosition[0].vehicleStatus == 9) {//超速
                    lineAndStop.remove(upPosition[0].vehicleId);
                    lineAndRun.remove(upPosition[0].vehicleId);
                    lineAndAlarm.remove(upPosition[0].vehicleId);
                    lineAndmiss.remove(upPosition[0].vehicleId);
                    nmoline.remove(upPosition[0].vehicleId);
                    heartBeat.remove(upPosition[0].vehicleId);
                    if (overSpeed.indexOf(upPosition[0].vehicleId) == -1) {
                        overSpeed.push(upPosition[0].vehicleId)
                    }
                    treeMonitoring.searchByFlag("treeDemo", upPosition[0].vehicleId, null, 4);
                } else if (upPosition[0].vehicleStatus == 11) {//心跳
                    lineAndStop.remove(upPosition[0].vehicleId);
                    lineAndRun.remove(upPosition[0].vehicleId);
                    lineAndAlarm.remove(upPosition[0].vehicleId);
                    lineAndmiss.remove(upPosition[0].vehicleId);
                    nmoline.remove(upPosition[0].vehicleId);
                    overSpeed.remove(upPosition[0].vehicleId);
                    if (heartBeat.indexOf(upPosition[0].vehicleId) == -1) {
                        heartBeat.push(upPosition[0].vehicleId);
                    }
                    treeMonitoring.searchByFlag("treeDemo", upPosition[0].vehicleId, null, 4);
                    treeMonitoring.objHeartbeatChange(upPosition[0].vehicleId, 11);
                    // amapOperation.carNameEvade(carId, carName, lngLatValue, false, "1", null, false, stateInfo);
                }
            }
        }
    },
    // 监控对象状态变更心跳后，更新地图车个状态
    objHeartbeatChange: function (id, stateIndex) {
        // 改变全局位置信息车个状态
        if (markerAllUpdateData.containsKey(id)) {
            var value = markerAllUpdateData.get(id);
            markerAllUpdateData.remove(id);
            value[0][5] = stateIndex;
            markerAllUpdateData.put(id, value);
        }

        // 改变可视区域内的车个状态
        if (markerViewingArea.containsKey(id)) {
            var value = markerViewingArea.get(id);
            markerViewingArea.remove(id);
            var marker = value[0];
            marker.stateInfo = stateIndex;
            value[0] = marker;
            value[6] = stateIndex;
            markerViewingArea.put(id, value);
            amapOperation.carNameEvade(id, marker.name, marker.getPosition(), null, "1", null, false, stateIndex);
        }

        // 改变可视区域外的车俩状态
        if (markerOutside.containsKey(id)) {
            var value = markerOutside.get(id);
            markerOutside.remove(id);
            value[5] = stateIndex;
            markerOutside.put(id, value);
        }
    },
    getNodesList: function (data) {
        var zTree = $.fn.zTree.getZTreeObj("treeDemo");
        var dataObj = data.obj;
        if (dataObj != null) {
            $.each(dataObj, function (index, value) {
                var treeNode = zTree.getNodeByParam("id", value, null);
                zTree.hideNode(treeNode);
            })
        }
    },
    // 处理报警
    updataRealAlarmMessage: function (data) {
        var jsonStr = data.body;
        var obj = JSON.parse(jsonStr);
        var type = obj.desc.type;
        // 判断用户是否有监控对象的权限
        if (diyueall.indexOf(obj.desc.monitorId) != -1) {
            // 报警更新方法
            dataTableOperation.updateAlarmInfoTable(obj);

            // wjk 报警记录更新也为隐藏按钮添加方法
            $("#scalingBtn").unbind("click").bind("click", treeMonitoring.hideDataClick);
        }
    },
    //刷新树
    refreshTree: function () {
        treeMonitoring.alltree();
        bflag = true;
        // json_ajax("POST", "/clbs/m/functionconfig/fence/bindfence/getStatistical", "json", false, null, treeMonitoring.setNumber);
        $("#search_condition").val("");
        $thetree.animate({scrollTop: 0});//回到顶端
    },
    ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
        responseData = JSON.parse(ungzip(responseData));
        return filterQueryResult(responseData, null);
    },
    search_condition: function () {
        fzzflag = true;
        missAll = false;
        misstype = false;
        misstypes = false;
        zTreeIdJson = {}
        suFlag = true;
        allflag = false;
        var queryType = $("#searchType").val();
        var queryParam = $("#search_condition").val();
        if (queryParam !== null && queryParam !== "") {
            var searchTree = {
                async: {
                    url: "/clbs/m/functionconfig/fence/bindfence/monitorTreeFuzzy",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"queryParam": queryParam, "queryType": queryType, "webType": 1},
                    dataFilter: treeMonitoring.ajaxQueryDataFilter
                },
                view: {
                    addHoverDom: treeMonitoring.addHoverDom,
                    removeHoverDom: treeMonitoring.removeHoverDom,
                    dblClickExpand: false,
                    nameIsHTML: true,
                    fontCss: setFontCss_ztree
                },
                check: {
                    enable: true,
                    chkStyle: "checkbox",
                    chkboxType: {
                        "Y": "s",
                        "N": "s"
                    },
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
                    onClick: treeMonitoring.onClickV,
                    onDblClick: treeMonitoring.onDbClickV,
                    beforeCheck: treeMonitoring.zTreeBeforeCheck,
                    beforeClick: treeMonitoring.zTreeBeforeClick,
                    onCheck: treeMonitoring.onCheckVehicle,
                    onAsyncSuccess: treeMonitoring.searchZTreeOnAsyncSuccess,
                    onExpand: treeMonitoring.zTreeOnExpand,
                    onNodeCreated: treeMonitoring.zTreeOnNodeCreated,
                    onRightClick: treeMonitoring.zTreeShowRightMenu
                }
            };
            $.fn.zTree.init($("#treeDemo"), searchTree);
        } else {
            treeMonitoring.alltree();
        }
    },
    //添加树节点悬浮dom
    addHoverDom: function (treeId, treeNode) {
        var sObj = $("#" + treeNode.tId + "_span");
        var id = (100 + newCount);
        var pid = treeNode.id;
        pid = window.encodeURI(window.encodeURI(pid));
        if ($("#" + treeNode.tId + "_ico").hasClass("offlineIcon_ico_docu") || $("#" + treeNode.tId + "_ico").hasClass("onlineDriving_ico_docu") ||
            $("#" + treeNode.tId + "_ico").hasClass("onlineNotPositioning_ico_docu") || $("#" + treeNode.tId + "_ico").hasClass("warning_ico_docu") ||
            $("#" + treeNode.tId + "_ico").hasClass("onlineParking_ico_docu") || $("#" + treeNode.tId + "_ico").hasClass("speedLimitWarning_ico_docu") ||
            $("#" + treeNode.tId + "_ico").hasClass("heartBeatWarning_ico_docu")) {
            var addStr = "<span class='button trackreplay' id='trackreplay_" + treeNode.tId + "'"
                + 'onClick="amapOperation.jumpToTrackPlayer(\'' + treeNode.id + '\',\'' + treeNode.deviceType + '\',\'' + treeNode.pId + '\')"'
                + "></span>";

            if (treeNode.deviceType == '1') {//只有808-2013协议有视频
                addStr += "<span class='button realtime-video-jump' id='realTimeVideoJump_" + treeNode.tId + "'"
                    + 'onClick="treeMonitoring.jumpToRealTimeVideoPage(\'' + treeNode.id + '\')"'
                    + "></span>";
            }
        } else {
            var addStr = "<span class='button trackreplay' id='trackreplay_" + treeNode.tId + "'"
                + 'onClick="amapOperation.jumpToTrackPlayer(\'' + treeNode.id + '\',\'' + treeNode.deviceType + '\',\'' + treeNode.pId + '\')"'
                + "></span>";
        }
        if (!sObj.nextAll().hasClass("trackreplay") && (treeNode.type == "vehicle" || treeNode.type == "people" || treeNode.type == "thing")) {
            sObj.after(addStr);
        }
    },
    //移除树节点悬浮dom
    removeHoverDom: function (treeId, treeNode) {
        $("#trackreplay_" + treeNode.tId).unbind().remove();
        $("#realTimeVideoJump_" + treeNode.tId).unbind().remove();
    },
    //跳转到实时视频页面
    jumpToRealTimeVideoPage: function (sid) {
        var jumpFlag = false;
        var permissionUrls = $("#permissionUrls").val();
        if (permissionUrls != null && permissionUrls != undefined) {
            var urllist = permissionUrls.split(",");
            if (urllist.indexOf("/realTimeVideo/video/list") > -1) {
                jumpFlag = true;
                location.href = "/clbs/realTimeVideo/video/list?videoId=" + sid;
            }
        }
        if (!jumpFlag) {
            layer.msg("无操作权限，请联系管理员");
        }
    },
    //实时监控数据加载
    realTimeDatatAdapt: function (type) {
        $("#scalingBtn").unbind("click").bind("click", treeMonitoring.hideDataClick);
        dataTableOperation.dataTableDbclick(type);
    },
    activeTableLastHeight: null,
    //隐藏数据
    hideDataClick: function () {
        dataTableOperation.closePopover();

        var mapHeightdata = winHeight - headerHeight - headerHeight + 30;
        var num, trHeight = 41;
        //判断不同数据获取不同数据条数
        if ($("#realTimeStatus").hasClass("active")) {
            num = tableStatus.getState().data.length;
        } else if ($("#realTtimeAlarm").hasClass("active")) {
            num = tableAlarm.getState().data.length;
        } else if ($("#obdInfoBoxTab").hasClass("active")) {
            num = tableObd.getState().data.length;
        } else if ($("#operationLog").hasClass("active")) {
            num = tableLog.getState().data.length;
        } else if ($("#activeSafetyTab").hasClass("active")) {
            num = tableSecurity.getState().data.length;
        }
        // if (num > 0) {
        if ($(this).hasClass("fa-chevron-down")) {
            uptFlag = false;
            $MapContainer.css('height', mapHeightdata + 'px');
            $(this).attr("class", "fa fa-chevron-up");
            treeMonitoring.activeTableLastHeight = $(pageLayout.getCurrentActiveTableName(activeIndex)).height();
            $(pageLayout.getCurrentActiveTableName()).css({
                "height": "0px"
            });
        } else {
            uptFlag = true;
            if (num >= 5) {
                $(pageLayout.getCurrentActiveTableName()).css({
                    "height": "266px"
                });
                $MapContainer.css('height', (newMapHeight - (trHeight * 5 + 43 + 17)) + 'px');
            } else {
                $(pageLayout.getCurrentActiveTableName()).css({
                    "height": (trHeight * num + 43 + 17) + "px"
                });
                $MapContainer.css('height', (newMapHeight - (trHeight * num + 43 + 17)) + 'px');
            }
            $(this).attr("class", "fa fa-chevron-down");
        }
        // }
    },
    //车个树右键菜单
    zTreeShowRightMenu: function (event, treeId, treeNode) {
        if (treeNode != null) {
            // 判断用户是否拥有可操作权限
            var data;
            var permission = $('#permission').val();
            var deviceType = treeNode.deviceType;//终端类型（用于区别超待）
            globalDeviceType = deviceType;
            if (deviceType != '8') {
                data = treeNode.id + ';' + treeNode.name + ';' + treeNode.deviceNumber + ';' + treeNode.simcardNumber;
            } else {
                var deviceNumber = treeMonitoring.IntegerMobileIPAddress(treeNode.simcardNumber);
                data = treeNode.id + ';' + treeNode.name + ';' + deviceNumber + ';' + treeNode.simcardNumber;
            }
            // 判断deviceType是否取到 防止抛出错误信息
            if (deviceType != undefined) {
                //获取到节点信息
                if (permission == "true") {
                    if (treeNode && !treeNode.noR) {
                        if (treeNode.type == "vehicle" || treeNode.type == "people" || treeNode.type == "thing") {
                            zTree.selectNode(treeNode, false, true);
                            var menuTopPos = winHeight - event.clientY;
                            $("#rMenu").css("width", "143px");
                            longDeviceType = deviceType;//给超长待机类型全局变量赋值（用作后续判断）
                            if (treeNode.iconSkin != "vehicleSkin" && treeNode.iconSkin != "peopleSkin" && treeNode.iconSkin != "thingSkin" && treeNode.iconSkin != 'btnImage iconArea offlineIcon') {
                                if (deviceType == "9") {
                                    if (menuTopPos <= 152 && menuTopPos > 0) {
                                        treeMonitoring.gsmCdmaShowRmenu(treeNode.id, event.clientX, (event.clientY - 152), data);
                                    } else {
                                        treeMonitoring.gsmCdmaShowRmenu(treeNode.id, event.clientX, event.clientY, data);
                                    }
                                } else if (deviceType == "10") {
                                    if (menuTopPos <= 117 && menuTopPos > 0) {
                                        treeMonitoring.gsmCdmaShowRmenu(treeNode.id, event.clientX, (event.clientY - 117), data);
                                    } else {
                                        treeMonitoring.gsmCdmaShowRmenu(treeNode.id, event.clientX, event.clientY, data);
                                    }
                                } else if (deviceType == "0" || deviceType == "1") {
                                    if ($("#userName").text() == "admin") {//464
                                        if (menuTopPos <= 500 && menuTopPos > 0) {
                                            treeMonitoring.showRMenu(treeNode.id, event.clientX, (event.clientY - 500), data);
                                        } else {
                                            treeMonitoring.showRMenu(treeNode.id, event.clientX, event.clientY, data);
                                        }
                                    } else {
                                        if (menuTopPos <= 500 && menuTopPos > 0) {
                                            treeMonitoring.showRMenu(treeNode.id, event.clientX, (event.clientY - 500), data);
                                        } else {
                                            treeMonitoring.showRMenu(treeNode.id, event.clientX, event.clientY, data);
                                        }
                                    }
                                } else {
                                    treeMonitoring.noShowRMenu(event.clientX, event.clientY, data);
                                }
                            } else {
                                treeMonitoring.noShowRMenu(event.clientX, event.clientY, data);
                            }
                        }
                    }
                }
            }

            $('.curSelectedNode').attr("data-id", treeNode.id);
        }
    },
    // 博实杰伪ID
    IntegerMobileIPAddress: function (sSim) {
        var sTemp = [];
        var sIp = [];
        var iHigt;
        if (sSim.length == 13 && sSim.startsWith("106")) {
            sSim = "1" + sSim.substring(3);
        }
        if (sSim.length == 11) {
            sTemp[0] = parseInt(sSim.substring(3, 5));
            sTemp[1] = parseInt(sSim.substring(5, 7));
            sTemp[2] = parseInt(sSim.substring(7, 9));
            sTemp[3] = parseInt(sSim.substring(9, 11));
            iHigt = parseInt(sSim.substring(1, 3));
            if (iHigt > 45) {
                iHigt -= 46;
            } else {
                iHigt -= 30;
            }
        } else if (sSim.length == 10) {
            sTemp[0] = parseInt(sSim.substring(2, 4));
            sTemp[1] = parseInt(sSim.substring(4, 6));
            sTemp[2] = parseInt(sSim.substring(6, 8));
            sTemp[3] = parseInt(sSim.substring(8, 10));
            iHigt = parseInt(sSim.substring(0, 2));
            if (iHigt > 45) {
                iHigt -= 46;
            } else {
                iHigt -= 30;
            }
        } else if (sSim.length == 9) {
            sTemp[0] = parseInt(sSim.substring(1, 3));
            sTemp[1] = parseInt(sSim.substring(3, 5));
            sTemp[2] = parseInt(sSim.substring(5, 7));
            sTemp[3] = parseInt(sSim.substring(7, 9));
            iHigt = parseInt(sSim.substring(0, 1));
        } else if (sSim.length < 9) {
            switch (sSim.length) {
                case 8:
                    sSim = "140" + sSim;
                    break;
                case 7:
                    sSim = "1400" + sSim;
                    break;
                case 6:
                    sSim = "14000" + sSim;
                    break;
                case 5:
                    sSim = "140000" + sSim;
                    break;
                case 4:
                    sSim = "1400000" + sSim;
                    break;
                case 3:
                    sSim = "14000000" + sSim;
                    break;
                case 2:
                    sSim = "140000000" + sSim;
                    break;
                case 1:
                    sSim = "1400000000" + sSim;
                    break;
            }
            sTemp[0] = parseInt(sSim.substring(3, 5));
            sTemp[1] = parseInt(sSim.substring(5, 7));
            sTemp[2] = parseInt(sSim.substring(7, 9));
            sTemp[3] = parseInt(sSim.substring(9, 11));
            iHigt = parseInt(sSim.substring(1, 3));
            if (iHigt > 45) {
                iHigt -= 46;
            } else {
                iHigt -= 30;
            }
        } else {
            return "";
        }
        if ((iHigt & 0x8) != 0) {
            sIp[0] = sTemp[0] | 128;
        }
        else {
            sIp[0] = sTemp[0];
        }
        if ((iHigt & 0x4) != 0) {
            sIp[1] = sTemp[1] | 128;
        }
        else {
            sIp[1] = sTemp[1];
        }
        if ((iHigt & 0x2) != 0) {
            sIp[2] = sTemp[2] | 128;
        }
        else {
            sIp[2] = sTemp[2];
        }
        if ((iHigt & 0x1) != 0) {
            sIp[3] = sTemp[3] | 128;
        }
        else {
            sIp[3] = sTemp[3];
        }
        return sIp[0] + '' + sIp[1] + '' + sIp[2] + '' + sIp[3];

    },
    gsmCdmaShowRmenu: function (type, x, y, data) {
        treeMonitoring.showLocationTime();//组装定点框
        if (longDeviceType == "9") {//艾赛欧超长待机
            $("#baseStation-add-btn").hide();
            $("#rMenu").html('<div class="col-md-12" id="treeRightMenu-l" style="padding:0px">' +
                '<a onclick="treeMonitoring.reportFrequencySet(\'' + type + '\')">上报频率设置</a>' +
                '<a onclick="treeMonitoring.fixedPointAndTiming(\'' + type + '\')">定点和校时</a>' +
                '<a onclick="treeMonitoring.throughInstruction(\'' + type + '\')">透传指令</a>' +
                '<a class="rmenu-last-a" onclick="treeMonitoring.restart(\'' + type + '\')">远程复位</a>' +
                '<a onclick="treeMonitoring.searchOriginalData(\'' + data + '\')">查询原始数据</a>' +
                '</div>'
            );
        } else {//F3超长待机
            $("#baseStation-add-btn").show();
            $("#rMenu").html('<div class="col-md-12" id="treeRightMenu-l" style="padding:0px">' +
                '<a onclick="treeMonitoring.reportFrequencySet(\'' + type + '\')">上报频率设置</a>' +
                '<a onclick="treeMonitoring.fixedPointAndTiming(\'' + type + '\')">定点和校时</a>' +
                '<a class="rmenu-last-a" onclick="treeMonitoring.locationTailAfter(\'' + type + '\')">位置跟踪</a>' +
                '<a onclick="treeMonitoring.searchOriginalData(\'' + data + '\')">查询原始数据</a>' +
                '</div>'
            );
        }
        treeMonitoring.rMenuUlShowOrPosition(x, y);
    },
    showRMenu: function (type, x, y, data) {
        editUrlPath = "/clbs/m/basicinfo/equipment/simcard/proofreading_" + type;
        $("#rMenu").html(
            '<div class="col-md-12" id="treeRightMenu-l" style="padding:0px">' +
            '<a href= "' + editUrlPath + '" data-toggle="modal" onclick="treeMonitoring.simLog(\'' + type + '\')" data-target="#commonWin">获取终端手机号信息</a>' +
            '<a onclick="treeMonitoring.setOBD(\'' + type + '\')">设置OBD车型信息</a>' +
            '<a onclick="treeMonitoring.callName_(\'' + type + '\')">单次回报(点名)</a>' +
            '<a onclick="treeMonitoring.following(\'' + type + '\')">临时位置跟踪</a>' +
            '<a onclick="treeMonitoring.ContinuousReturn(\'' + type + '\')">连续回报</a>' +
            '<a onclick="treeMonitoring.setPlateNumber(\'' + type + '\')">设置终端车牌号</a>' +
            '<a onclick="treeMonitoring.goPhotograph(\'' + type + '\')">监控对象-拍照</a>' +
            '<a onclick="treeMonitoring.monitoringObjectListening(\'' + type + '\')">监控对象-监听</a>' +
            '<a onclick="treeMonitoring.goVideotape(\'' + type + '\')">监控对象-录像</a>' +
            '<a onclick="treeMonitoring.goOverspeedSetting(\'' + type + '\')">设置超速</a>' +
            '<a onclick="treeMonitoring.sendOriginalCommand(\'' + type + '\')">发送原始命令</a>' +
            // '<a onclick="treeMonitoring.textInfoSend(\'' + type + '\')">批量文本信息下发</a>' +
            '<a href="/clbs/v/monitoring/getRealTimeMonitoringSendTextByBatchPage" data-toggle="modal" data-target="#commonSmWin">批量文本信息下发</a>' +
            '<a class="menu-show-more" onclick="treeMonitoring.showRightMenuMoreClick()">查看更多<span>&gt;</span></a>' +
            '</div>' +
            '<div class="col-md-6 hidden" id="treeRightMenu-r" style="padding:0px 0px 0px 5px;">' +
            '<a onclick="treeMonitoring.askQuestionsIssued(\'' + type + '\')">提问下发</a>' +
            '<a onclick="treeMonitoring.reantimeCallBack(\'' + type + '\')">电话回拨</a>' +
            '<a onclick="treeMonitoring.terminalReset(\'' + type + '\')">终端复位</a>' +
            '<a onclick="treeMonitoring.restoreSettings(\'' + type + '\')">恢复出厂设置</a>' +
            '<a onclick="treeMonitoring.doorLock(\'' + type + '\',\'' + 1 + '\')">车门加锁</a>' +
            '<a onclick="treeMonitoring.doorLock(\'' + type + '\',\'' + 0 + '\')">车门解锁</a>' +
            '<a id="cutoil" onclick="treeMonitoring.cutOilElec(\'' + type + '\'' + ')">断油断电</a>' +
            '<a onclick="treeMonitoring.recordCollection(\'' + type + '\')">行驶记录数据采集</a>' +
            '<a onclick="treeMonitoring.recordSend(\'' + type + '\')">行驶记录参数下传</a>' +
            '<a onclick="treeMonitoring.multimediaSearch(\'' + type + '\')">多媒体检索</a>' +
            '<a onclick="treeMonitoring.multimediaUpload(\'' + type + '\')">多媒体上传</a>' +
            '<a onclick="treeMonitoring.recordingUpload(\'' + type + '\')">录音上传</a>' +
            '<a onclick="treeMonitoring.terminalParameters(\'' + type + '\')">查询终端参数</a>' +
            '<a onclick="treeMonitoring.searchOriginalData(\'' + data + '\')">查询原始数据</a>' +
            '</div>'
        );
        var roleName = $("#allUserRole").attr("value");
        if (roleName.indexOf("POWER_USER") != -1 || roleName.indexOf("ROLE_ADMIN") != -1) {
            $("#cutoil").css("display", "block");
        } else {
            $("#cutoil").css("display", "none");
        }

        currentRightClickVehicleId = type;

        treeMonitoring.rMenuUlShowOrPosition(x, y);
    },
    noShowRMenu: function (x, y, data) {
        $("#rMenu").html('<div class="col-md-12" id="treeRightMenu-l" style="padding:0px">' +
            '<a class="rmenu-last-a" onclick="treeMonitoring.searchOriginalData(\'' + data + '\')">查询原始数据</a>' +
            '</div>'
        );
        treeMonitoring.rMenuUlShowOrPosition(x, y);
    },
    rMenuUlShowOrPosition: function (x, y) {
        $("#rMenu ul").show();
        if (y < 0) {
            rMenu.css({"top": (y - y) + "px", "left": (x + 35) + "px", "visibility": "visible"});
        } else {
            rMenu.css({"top": (y) + "px", "left": (x + 35) + "px", "visibility": "visible"});
        }
        $("body").bind("mousedown", treeMonitoring.onBodyMouseDown);
        //右键显示菜单节点跳动问题
        $("#thetree").scrollTop(scorllDefaultTreeTop);
    },
    showLocationTime: function () {
        $("#baseStation-MainContent").html(
            "<div class='form-group'>" +
            "<label class='col-md-4 control-label'>定点时间：</label>" +
            "<div class='col-md-4'>" +
            "<input type='text' id='locationTimes' name='locationTimes' class='form-control' " +
            "style='cursor: pointer;  background-color: #fafafa;' readonly/>" +
            "</div>" +
            "<div class='col-md-1'>" +
            "<button id='baseStation-add-btn' type='button' class='btn btn-primary addIcon'>" +
            "<span class='glyphicon glyphiconPlus' aria-hidden='true'></span>" +
            "</button>" +
            "</div>" +
            "</div>"
        );
        var loadInitTime =
            +(loadInitNowDate.getHours() < 10 ? "0" + loadInitNowDate.getHours() : loadInitNowDate.getHours()) + ":"
            + (loadInitNowDate.getMinutes() < 10 ? "0" + loadInitNowDate.getMinutes() : loadInitNowDate.getMinutes()) + ":"
            + (loadInitNowDate.getSeconds() < 10 ? "0" + loadInitNowDate.getSeconds() : loadInitNowDate.getSeconds());
        $("#locationTimes").val(loadInitTime);
        $("#baseStation-add-btn").on("click", realTimeMonitoringGsmCdma.addLocationTimeEvent);
    },
    onBodyMouseDown: function (event) {
        if (!(event.target.id == "rMenu" || $(event.target).parents("#rMenu").length > 0)) {
            rMenu.css({"visibility": "hidden"});
        }
    },
    simLog: function (vid) {
        pageLayout.closeVideo();
        var url = "/clbs/m/basicinfo/equipment/simcard/simLog";
        var data = {"vehicleId": vid};
        json_ajax("POST", url, "json", false, data, null);
        setTimeout("dataTableOperation.logFindCilck()", 500);
    },
    callName_: function (type) {
        pageLayout.closeVideo();
        var param = [];
        var obj = new Object();
        obj.vehicleID = type;
        param.push(obj);
        var url = "/clbs/v/oilmassmgt/oilcalibration/getLatestOilData";
        var data = {"vehicleId": type};
        json_ajax_p("POST", url, "json", false, data, amapOperation.getDCallBack);
        amapOperation.subscribeLatestLocation(param);
        layer.msg("点名成功");
        callTheRollId = type;
        amapOperation.callTheRollFun();
        rMenu.css({"visibility": "hidden"});
        setTimeout("dataTableOperation.logFindCilck()", 500);
    },
    following: function (type) {
        pageLayout.closeVideo();
        treeMonitoring.getBrandParameter(type);
        var carBrand = brand;
        parametersID = type;
        $("#traceParameterTitle").html("临时位置跟踪：" + brand);
        $("#goTrace").modal('show');
        rMenu.css({"visibility": "hidden"});
        setTimeout("dataTableOperation.logFindCilck()", 500);
    },
    goPhotograph: function (type) {
        pageLayout.closeVideo();
        treeMonitoring.getBrandParameter(type);
        $("#goPhotographBrand").html("监控对象-拍照：" + brand);
        $("#vid1").val(type);
        $("#goPhotograph").modal('show');
        rMenu.css({"visibility": "hidden"});
    },
    goVideotape: function (type) {
        pageLayout.closeVideo();
        treeMonitoring.getBrandParameter(type);
        $("#videotapeBrand").html("监控对象-录像：" + brand);
        $("#vid2").val(type);
        $("#goVideotape").modal('show');
        rMenu.css({"visibility": "hidden"});
    },
    goOverspeedSetting: function (type) {
        pageLayout.closeVideo();
        var url = "/clbs/v/monitoring/findSpeedParameter";
        var data = {"vehicleId": type};
        json_ajax("POST", url, "json", false, data, treeMonitoring.findSpeedParameterCallBack);
        treeMonitoring.getBrandParameter(type);
        $("#goOverspeedSettingBrand").html("设置超速：" + brand);
        $("#goOverspeedSetting").modal('show');
        $("#vid9").val(type);
        rMenu.css({"visibility": "hidden"});
    },
    //给右键超速设置赋值
    findSpeedParameterCallBack: function (data) {
        var datas = data.obj;
        for (var i = 0; i < datas.length; i++) {
            if (datas[i].alarmParameterId == null) {
                if (datas[i].paramCode == "param1") {
                    $("#masSpeed").val(datas[i].defaultValue);
                } else if (datas[i].paramCode == "param2") {
                    $("#speedTime").val(datas[i].defaultValue);
                }
            } else {
                if (datas[i].paramCode == "param1") {
                    $("#masSpeed").val(datas[i].parameterValue);
                } else if (datas[i].paramCode == "param2") {
                    $("#speedTime").val(datas[i].parameterValue);
                }
            }
        }
    },
    /*    continuousReturnTiming: function (type) {
            pageLayout.closeVideo();
            treeMonitoring.getBrandParameter(type);
            $("#continuousReturnTimingBrand").html("连续回报(定时)：" + brand);
            $("#vid30").val(type);
            $("#continuousReturnTiming").modal('show');
            rMenu.css({"visibility": "hidden"});
        },
        ContinuousReturnFixedDistance: function (type) {
            pageLayout.closeVideo();
            treeMonitoring.getBrandParameter(type);
            $("#continuousReturnFixedDistanceBrand").html("连续回报(定距)：" + brand);
            $("#vid31").val(type);
            $("#continuousReturnFixedDistance").modal('show');
            rMenu.css({"visibility": "hidden"});
        },*/
    ContinuousReturn: function (type) {
        pageLayout.closeVideo();
        treeMonitoring.getBrandParameter(type);
        $("#continuousReturnTimingDistanceBrand").html("连续回报：" + brand);
        $("#vid32").val(type);
        $("#vid31").val(type);
        $("#vid30").val(type);
        $("#continuousReturnTimingDistance").modal('show');
        rMenu.css({"visibility": "hidden"});
        treeMonitoring.setContinuousReturnDefaultValue();
        $('#timeInterval0').show();
        $('#timeInterval1').hide();
        $('#timeInterval2').hide();
        $('#timeInterval2 label.error').hide();
        $('#timeInterval0 label.error').hide();
        $('#timeInterval1 label.error').hide();
    },
    /**
     * 设置连续回报默认值
     */
    setContinuousReturnDefaultValue: function () {
        $("#continuousReturnValue").val(0);
        $("input[name='driverLoggingOutUpTimeSpace']").val(30);
        $("input[name='driverLoggingOutUpDistanceSpace']").val(200);
        $("input[name='dormancyUpTimeSpace']").val(120);
        $("input[name='dormancyUpDistanceSpace']").val(20000);
        $("input[name='emergencyAlarmUpTimeSpace']").val(15);
        $("input[name='emergencyAlarmUpDistanceSpace']").val(100);
        $("input[name='defaultTimeUpSpace']").val(30);
        $("input[name='defaultDistanceUpSpace']").val(500);
    },
    sendOriginalCommand: function (type) {
        pageLayout.closeVideo();
        treeMonitoring.getBrandParameter(type);
        $("#sendOriginalCommandBrand").html("发送原始命令：" + brand);
        $("#sendOriginalCommand").modal('show');
        $("#vid14").val(type);
        rMenu.css({"visibility": "hidden"});
    },
    textInfoSend: function (type) {
        pageLayout.closeVideo();
        treeMonitoring.getBrandParameter(type);
        $("#textInfoSendBrand").html("文本信息下发：" + brand);
        $("#textInfoSend").modal('show');
        $("#vid5").val(type);
        rMenu.css({"visibility": "hidden"});
    },
    askQuestionsIssued: function (type) {
        pageLayout.closeVideo();
        treeMonitoring.getBrandParameter(type);
        $("#askQuestionsIssuedBrand").html("提问下发：" + brand);
        $("#askQuestionsIssued").modal('show');
        $("#vid6").val(type);
        rMenu.css({"visibility": "hidden"});
    },
    reantimeCallBack: function (type) {
        pageLayout.closeVideo();
        treeMonitoring.getBrandParameter(type);
        $("#reantimeCallBackBrand").html("电话回拨：" + brand);
        $("#reantimeCallBack").modal('show');
        $("#vid40").val(type);
        rMenu.css({"visibility": "hidden"});
    },
    informationService: function (type) {
        pageLayout.closeVideo();
        treeMonitoring.getBrandParameter(type);
        $("#informationServiceBrand").html("信息服务：" + brand);
        $("#informationService").modal('show');
        $("#vid16").val(type);
        rMenu.css({"visibility": "hidden"});
    },
    reportFrequencySet: function (type) {
        pageLayout.closeVideo();
        $("label.error").hide();//隐藏错误提示
        laydate.render({elem: '#requiteTime', type: 'time', theme: '#6dcff6'});
        if (longDeviceType == "9") {
            $("#requiteTime").css('background-color', '#DCDCDC');
            $("#requiteTime").attr("disabled", "disabled");
            $("#requiteTimeDiv").hide();
        } else {
            $("#requiteTime").css('background-color', '#fafafa');
            $("#requiteTime").removeAttr("disabled", "disabled");
            $("#requiteTimeDiv").show();
        }
        treeMonitoring.getBrandParameter(type);
        $("#reportFrequencyBrand").html("上报频率设置：" + brand);
        $("#reportFrequencySet").modal('show');
        $("#vid17").val(type);
        $("#hours").val("");
        $("#minute").val("");
        rMenu.css({"visibility": "hidden"});
    },
    fixedPointAndTiming: function (type) {
        pageLayout.closeVideo();
        laydate.render({elem: '#locationTimes', type: 'time', theme: '#6dcff6'});
        treeMonitoring.getBrandParameter(type);
        $("#fixedPointAndTimingBrand").html("定点和校时：" + brand);
        $("#fixedPointAndTiming").modal('show');
        $("#vid18").val(type);
        $("#showTime").val("");
        rMenu.css({"visibility": "hidden"});
    },
    locationTailAfter: function (type) {
        pageLayout.closeVideo();
        treeMonitoring.getBrandParameter(type);
        $("#locationTailAfterBrand").html("位置跟踪：" + brand);
        $("#locationTailAfter").modal('show');
        $("#vid19").val(type);
        $("#tailAfterTime").val("");
        $("#IntervalTime").val("");
        rMenu.css({"visibility": "hidden"});
    },
    throughInstruction: function (type) {
        pageLayout.closeVideo();
        treeMonitoring.getBrandParameter(type);
        $("#throughInstructionBrand").html("透传指令：" + brand);
        $("#throughInstruction").modal('show');
        $("#vid20").val(type);
        rMenu.css({"visibility": "hidden"});
    },
    restart: function (type) {
        layer.confirm("确定要远程复位吗？", {btn: ["确定", "取消"], icon: 3, title: "操作确认"}, function () {
            var url = "/clbs/v/monitoringLong/sendParam";
            var data = {"vid": type, "orderType": 21};
            json_ajax_p("POST", url, "json", false, data, treeMonitoring.restartCallBack);
        });
        setTimeout("dataTableOperation.logFindCilck()", 500);
        rMenu.css({"visibility": "hidden"});
    },
    restartCallBack: function (data) {
        if (data.obj.type) {
            layer.msg("复位指令已发送");
        } else {
            layer.msg("复位指令发送失败");
        }
    },
    terminalReset: function (type) {
        pageLayout.closeVideo();
        var url = "/clbs/v/monitoring/orderMsg"
        json_ajax("post", url, "json", false, {"vid": type, "orderType": 7, "cw": 4}, treeMonitoring.terminalResetBack);
        rMenu.css({"visibility": "hidden"});
    },
    restoreSettings: function (type) {
        pageLayout.closeVideo();
        var url = "/clbs/v/monitoring/orderMsg"
        json_ajax("post", url, "json", false, {"vid": type, "orderType": 7, "cw": 5}, treeMonitoring.terminalResetBack);
        rMenu.css({"visibility": "hidden"});
    },
    doorLock: function (type, f) {
        pageLayout.closeVideo();
        var url = "/clbs/v/monitoring/orderMsg"
        var sign = 0;
        if (f == 1) {
            sign = 1
        }
        json_ajax("post", url, "json", false, {
            "vid": type,
            "orderType": 8,
            "sign": sign
        }, treeMonitoring.terminalResetBack);
        rMenu.css({"visibility": "hidden"});
    },
    //断油电功能
    cutOilElec: function (type) {
        //获取断电油量信息
        var getstopoildata = {
            getlastestoildata: function () {
                var url = "/clbs/v/oilmassmgt/oilcalibration/getLatestOilData";
                json_ajax("post", url, "json", false, {"vehicleId": type, "curBox": null}, getstopoildata.getDCallBack);
            },
            getDCallBack: function (data) {
                msgSNAck = data.obj.msgSN;
                if (msgSNAck != null && msgSNAck != "") {
                    getstopoildata.subscribeLatestLocation2(msgSNAck, type);
                }
            },
            subscribeLatestLocation2: function (msgSNAck, type) {
                var requestStrS = {
                    "desc": {
                        "MsgId": 40964,
                        "UserName": $("#userName").text(),
                        "cmsgSN": msgSNAck
                    },
                    "data": {"vehicleID": type}
                };
                setTimeout(function () {
                    webSocket.subscribe(headers, "/user/topic/realLocationS", getstopoildata.oilElectric, "/app/vehicle/realLocationS", requestStrS);
                });
            },
            oilElectric: function (data) {
                var data = $.parseJSON(data.body);
                var vid = data.desc.monitorId;
                data = data.data.msgBody;
                var formattedAddress = data.positionDescription ? data.positionDescription : "";
                var speed = data.gpsSpeed;

                var status = data.status.toString(2);
                status = status.substr(9, 1);
                if (status == 0) {
                    status = "通油电";
                } else {
                    status = "断油电";
                }
                $("#cutOilElecState").text(status);
                $("#cutOilElecSpeed").text(speed);
                $("#cutOilElecLocation").text(formattedAddress);

                $("#gpCutOilElec").removeAttr("disabled");


                $("#gpCutOilElec").unbind().on("click", function () {
                    if (treeMonitoring.validates()) {
                        var value = $("#cutOilElecToken").val()
                        var oilElectricMsg = $("#cutOilElecToken").val();
                        var checkvalue = $("input:radio[name='cutOilElecOpe']:checked").val();
                        if (!checkvalue || !oilElectricMsg) {
                            layer.msg("请选择您要执行的操作或输入口令");
                        } else {
                            layer.open({
                                closeBtn: false,
                                offset: 't',
                                title: '提示',
                                content: '当前操作影响重大，请再次确认操作',
                                btn: ['确定', '返回'],
                                btn1: function (index, layero) {
                                    Issuedstopoil.clickIssuedstopoil(vid, oilElectricMsg, checkvalue);
                                    layer.close(index);
                                    $("#cutOilElecToken").val('');
                                    $(':input', "#cutOilElecForm").removeAttr('checked');
                                },
                                btn2: function (index, layero) {
                                    getstopoildata.getlastestoildata();
                                    $("#cutOilElecToken").val('');
                                    $(':input', "#cutOilElecForm").removeAttr('checked');
                                }
                            });

                        }
                    }
                })

            }
        }
        getstopoildata.getlastestoildata();
        //断油电指令下发工功能
        var Issuedstopoil = {
            clickIssuedstopoil: function (vid, oilElectricMsg, checkvalue) {
                var url = "/clbs/v/monitoring/orderMsg";
                json_ajax("post", url, "json", false, {
                    "vid": vid,
                    "orderType": 42,
                    "oilElectricMsg": oilElectricMsg,
                    "flag": checkvalue
                }, getDCallBack3);

                function getDCallBack3(data) {
                    if (data.obj.type == true) {
                        layer.msg("口令发送成功");
                    } else {
                        layer.msg("口令错误！");
                    }
                }
            }

        }
        //断油电刷新
        $("#cutOilElecRefresh").unbind().on("click", function () {
            getstopoildata.getlastestoildata(type);
            $("#cutOilElecToken").val('');
            $(':input', "#cutOilElecForm").removeAttr('checked');
        })
        treeMonitoring.getBrandParameter(type);
        $("#cutOilElecBrand").html("断油断电：" + brand);
        $("#cutOilElec").modal('show');
        rMenu.css({"visibility": "hidden"});
    },
    //关闭清空表单
    cutoilclose: function () {
        $("#cutOilElecForm").find("input").not(':button,:submit,:reset,:hidden,:radio').val('').removeAttr('checked');
        $("#cutOilElecState").text("");
        $("#cutOilElecSpeed").text("");
        $("#cutOilElecLocation").text("");
        // $('input').removeAttr('checked');
        $("#cutOilElecToken").val('');
    },
    validates: function () {
        return $("#cutOilElecForm").validate({
            rules: {
                cutOilElecToken: {
                    required: true,
                    minlength: 6,
                }
            },
            messages: {
                cutOilElecToken: {
                    required: "输入内容不能为空",
                    minlength: "不能少于6个字符",
                }
            }
        }).form();
    },
    recordCollection: function (type) {
        //设置当前时间显示
        if (isInitDatePicker) {
            // dateRangePicker 不刷新页面, 二次初始存在问题
            isInitDatePicker = false;
            // $('#recordTimeInterval').dateRangePicker();
        } else {
            treeMonitoring.getCurrentRangeTIme();
        }

        $("#sign").val('0H');
        $("#recordTimeIntervalIsHidden").addClass('hidden');
        treeMonitoring.getBrandParameter(type);
        $("#recordCollectionCommandBrand").html("行驶记录数据采集：" + brand);
        $("#recordCollectionCommand").modal('show');
        $("#vid10").val(type);
        rMenu.css({"visibility": "hidden"});
    },
    getCurrentRangeTIme: function () {
        var nowDate = new Date();
        msStartTime = nowDate.getFullYear()
            + "-"
            + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                + parseInt(nowDate.getMonth() + 1) : parseInt(nowDate
                .getMonth() + 1))
            + "-"
            + (nowDate.getDate() < 10 ? "0" + nowDate.getDate() : nowDate.getDate())
            + " " + "00:00:00";
        msEndTime = nowDate.getFullYear()
            + "-"
            + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                + parseInt(nowDate.getMonth() + 1) : parseInt(nowDate
                .getMonth() + 1))
            + "-"
            + (nowDate.getDate() < 10 ? "0" + nowDate.getDate() : nowDate.getDate())
            + " " + ("23") + ":" + ("59") + ":" + ("59");
        $('#recordTimeInterval').val(msStartTime + "--" + msEndTime);
    }
    ,
    recordSend: function (type) {
        treeMonitoring.getBrandParameter(type);
        $("#recordSendCommandBrand").html("行驶记录参数下传：" + brand);
        $("#recordSendCommand").modal('show');
        $("#recordSend").val(type);
        rMenu.css({"visibility": "hidden"});
    },
    goRecordCollect: function () {
        var vid = $("#recordCollectionCommand").find("#vid10").val();
        var orderType = $("#recordCollectionCommand").find("#orderType").val();
        var sign = $("#recordCollectionCommand").find("#sign").val();
        var param = {
            vid: vid,
            orderType: orderType,
            commandSign: sign,
        };
        if (commandSign.indexOf(sign) != -1) {
            var timeInterval = $('#recordTimeInterval').val().split('--');
            var startTimes = timeInterval[0];
            var endTimes = timeInterval[1];
            // 判断时间范围
            if (startTimes < "2000-01-01 00:00:00" || endTimes > "2099-12-31 23:59:59") {
                layer.msg('已超出范围(范围为: 2000-01-01 00:00:00～2099-12-31 23:59:59)');
                return;
            }
            param.startTime = startTimes;
            param.endTime = endTimes;
        }
        json_ajax("post", "/clbs/v/monitoring/orderMsg",
            "json", false, param, function (data) {
                $("#recordCollectionCommand").modal("hide");
                if (data) {
                    if (data.success) {
                        layer.msg("指令发送成功");
                        setTimeout("dataTableOperation.logFindCilck()", 500);
                    } else {
                        layer.msg(data.msg);
                    }
                }
            });
    },
    monitorSignChange: function (signSelected) {
        var sign = $(signSelected).val();

        if (commandSign.indexOf(sign) != -1) {
            $("#recordTimeIntervalIsHidden").removeClass('hidden');
        } else {
            $("#recordTimeIntervalIsHidden").addClass('hidden');
        }
    }
    ,
    goRecordSend: function () {
        $("#recordSends").ajaxSubmit(function (data) {
            $("#recordSendCommand").modal("hide");
            if (JSON.parse(data).success) {
                layer.msg("指令发送成功")
                setTimeout("dataTableOperation.logFindCilck()", 500);
            } else {
                layer.msg(JSON.parse(data).msg)
            }
        });
    },
    terminalResetBack: function (data) {
        if (data.success) {
            layer.msg("指令发送成功")
        } else {
            layer.msg(JSON.parse(data).msg)
        }
        setTimeout("dataTableOperation.logFindCilck()", 500);
    },
    monitoringObjectListening: function (type) {
        pageLayout.closeVideo();
        treeMonitoring.getBrandParameter(type);
        $("#monitoringObjectListeningBrand").html("监控对象-监听：" + brand);
        $("#monitoringObjectListening").modal('show');
        $("#vid41").val(type);
        rMenu.css({"visibility": "hidden"});
    },
    multimediaSearch: function (type) {
        pageLayout.closeVideo();
        treeMonitoring.getBrandParameter(type);
        $("#multimediaSearchBrand").html("多媒体检索：" + brand);
        $("#multimediaSearch").modal('show');
        $("#vid11").val(type);
        rMenu.css({"visibility": "hidden"});
    },
    multimediaUpload: function (type) {
        pageLayout.closeVideo();
        treeMonitoring.getBrandParameter(type);
        $("#multimediaUploadBrand").html("多媒体上传：" + brand);
        $("#multimediaUpload").modal('show');
        $("#vid12").val(type);
        rMenu.css({"visibility": "hidden"});
    },
    recordingUpload: function (type) {
        pageLayout.closeVideo();
        if ($(".taping-timeline").is(":hidden")) {//没有录音
            treeMonitoring.getBrandParameter(type);
            $("#recordingUploadBrand").html("录音上传：" + brand);
            $("#recordingUpload").modal('show');
            $("#vid13").val(type);
            rMenu.css({"visibility": "hidden"});
        } else {//正在录音
            layer.msg("设备正在录音，请先停止当前录音再下发！");
        }
    },
    terminalParameters: function (type) {
        pageLayout.closeVideo();
        var url = "/clbs/v/monitoring/orderMsg"
        json_ajax("post", url, "json", false, {"vid": type, "orderType": 15}, treeMonitoring.terminalResetBack)
        setTimeout("dataTableOperation.logFindCilck()", 500);
        rMenu.css({"visibility": "hidden"});
    },
    // 查询原始数据
    searchOriginalData: function (data) {
        rMenu.css({"visibility": "hidden"});
        var info = data.split(';');
        var id = info[2];
        var requestData = {
            'socketId': id,
            'deviceNumber': id,
            'plateNumber': info[1],
            'mobile': info[3],
        };
        $('#searchForName').text(info[1]);
        $('#searchDviceNumber').text(info[2]);
        $('#searchSimNumber').text(info[3]);
        if (stompClientSocket != null) {
            stompClientSocket.deactivate();
        }

        $('#controlGetData').text('暂停');
        stompClientSocket = new StompJs.Client({
            // connectHeaders: headers,
            webSocketFactory: () => new SockJS(hostUrl),
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
            onConnect () {
                stompClientSocket.subscribe("/user/" + id + "/t808Msg", treeMonitoring.originalFunCallBack);
                stompClientSocket.publish({ destination: '/app/webSocket/updateDevice', body: JSON.stringify(requestData)});
                $('#originalDataList').html('');
                $('#originalDataModal').animate({bottom: 0});
            },
        });
        stompClientSocket.activate();
    },
    // 原始数据获取回掉函数
    originalFunCallBack: function (data) {
        var msg = data.body;
        var msgArr = msg.split('code:');
        var tit = msgArr[0] + 'code:',
            con = msgArr[1].trim();
        var value = con.replace(/\(.*?\)/g, '');

        var html = '<li>' +
            '<span>' + tit + '</span>' +
            '<span class="code">' + con + '</span>' +
            // '<span class="analysisWrap"><a class="analysis" id="analysis" onclick="treeMonitoring.analysisPost(\''+value+'\')">解析</a></span>'+
            '<a class="analysis" id="analysis" onclick="treeMonitoring.analysisPost(\'' + value + '\')">解析</a>' +
            '</li>';

        $('#originalDataList').append(html);
        treeMonitoring.copyDataFun();

        // $('#originalDataList').on('dblclick','.code',function(){
        //     var self = $(this);
        //     $('#originalDataList .code').removeClass('db-active');
        //     $('#originalDataList .analysisWrap').removeClass('active');
        //     self.addClass('db-active');
        //     self.siblings('.analysisWrap').addClass('active');
        // });
    },
    analysisPost: function (value) {
        var form = $('<form></form>', {
            id: 'analysisForm',
            method: 'get',
            action: 'https://www.zwlbs.cn:8180/decode.html',
            target: "_blank",
            style: 'display:none'
        }).appendTo($('body'));

        var html = '<input type="hidden" name="deviceType" value="' + globalDeviceType + '"/>' +
            '<input type="hidden" name="value" value="' + value + '"/>';
        form.append(html);
        form.submit();
        form.remove();
    },
    // modal关闭事件
    modalCloseFun: function () {
        if (stompClientSocket != null) {
            stompClientSocket.deactivate();
        }
        stompClientSocket = null;
        $('#originalDataModal').animate({bottom: '-300px'});
    },
    // 复制原始数据
    copyDataFun: function () {
        var clipboard = new Clipboard('#copyOriginalData', {
            text: function () {
                return $('#originalDataList').text();
            }
        });
        clipboard.on('success', function (e) {
            layer.msg('复制成功');
        })
    },
    // 清空数据
    clearDataFun: function () {
        $('#originalDataList').html('');
    },
    // 暂停 or获取历史数据
    isGetOriginalData: function () {
        var text = $('#controlGetData').text();
        if (text == '暂停') {
            stompClientSocket.deactivate();
            stompClientSocket = null;
            $('#controlGetData').text('开始');
        } else {
            $('#controlGetData').text('暂停');
            var requestData = {
                'socketId': $("#userName").text(),
                'deviceNumber': $('#searchDviceNumber').text(),
                'plateNumber': $('#searchForName').text(),
                'mobile': $('#searchSimNumber').text(),
            };

            stompClientSocket = new StompJs.Client({
                // connectHeaders: headers,
                webSocketFactory: () => new SockJS(hostUrl),
                reconnectDelay: 5000,
                heartbeatIncoming: 4000,
                heartbeatOutgoing: 4000,
                onConnect () {
                    stompClientSocket.subscribe("/user/" + $("#userName").text() + "/t808Msg", treeMonitoring.originalFunCallBack);
                    stompClientSocket.publish({ destination: '/app/webSocket/updateDevice', body: JSON.stringify(requestData)});
                },
            });
            stompClientSocket.activate();
        }
    },
    showRightMenuMoreClick: function () {
        pageLayout.closeVideo();
        if ($("#treeRightMenu-r").is(":hidden")) {
            $("#rMenu").css("width", "286px");
            $("#treeRightMenu-l").attr("class", "col-md-6");
            $("#treeRightMenu-r").removeAttr("class", "hidden");
            $("#treeRightMenu-r").attr("class", "col-md-6");
            $(".menu-show-more span").html("&lt;");
        } else {
            $("#rMenu").css("width", "143px");
            $("#treeRightMenu-l").attr("class", "col-md-12");
            $("#treeRightMenu-r").attr("class", "col-md-6 hidden");
            $(".menu-show-more span").html("&gt;");
        }
    },
    getBrandParameter: function (type) {
        var url = "/clbs/v/monitoring/getBrandParameter";
        var data = {"vehicleId": type};
        json_ajax_p("POST", url, "json", false, data, treeMonitoring.getBrand);
    },
    initRunAndStopNum: function () {
        if (updateStopAndRunNum) {
            updateStopAndRunNum = false;
            $.ajax(
                {
                    type: 'POST',
                    url: '/clbs/m/functionconfig/fence/bindfence/getRunAndStopMonitorNum',
                    dataType: 'json',
                    async: false,
                    data: {"isNeedMonitorId": false, "userName": $("#userName").text()},
                    timeout: 8000,
                    success: function (data) {
                        $("#tline").text("(" + data.obj.onlineNum + ")");
                        $("#tmiss").text("(" + (data.obj.allV - data.obj.onlineNum) + ")");
                        $("#table-car-online").text(data.obj.onlineNum);
                        $("#table-car-offline").text(data.obj.allV - data.obj.onlineNum);
                        $("#table-car-run").text(data.obj.runArrNum);
                        $("#table-car-stop").text(data.obj.onlineParkNum);
                        $("#tall").text("(" + data.obj.allV + ")");
                        $tableCarAll.text(data.obj.allV);
                        if (data.obj.allV !== 0) {
                            $tableCarOnlinePercent.text((data.obj.onlineNum / data.obj.allV * 100).toFixed(2) + "%");
                        } else {
                            $tableCarOnlinePercent.text("0%")
                        }
                    }
                }
            );
        }
    },
    getBrand: function (data) {
        brand = data;
    },
    parameter: function (type) {
        var validity = $("#validity").val();
        var interval = $("#interval").val();

        if (!treeMonitoring.parametersTraceValidate()) return;
        var listParameters = [];
        listParameters.push(type);
        listParameters.push(interval);
        listParameters.push(validity);
        var url = "/clbs/v/monitoring/parametersTrace";
        var parameters = {"parameters": listParameters};
        ajax_submit("POST", url, "json", true, parameters, true, fenceOperation.parametersTrace);
        setTimeout("dataTableOperation.logFindCilck()", 500);
    },
    // 临时位置跟踪校验
    parametersTraceValidate: function () {
        return $("#parametersTrace").validate({
            rules: {
                interval: {
                    required: true,
                    digits: true,
                    range: [0, 65535]
                },
                validity: {
                    required: true,
                    digits: true,
                    range: [0, 4294967295]
                },
            },
            messages: {
                interval: {
                    required: '请输入回传时间间隔',
                    digits: '请输入0~65535之间的数字',
                    range: '请输入0~65535之间的数字',
                },
                validity: {
                    required: '请输入位置跟踪有效时间',
                    digits: '请输入0~4294967295之间的数字',
                    range: '请输入0~4294967295之间的数字',
                },
            }
        }).form();
    },
    // 获取分组下的子节点
    getChildNode: function (treeNode) {
        var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
        var url = "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign";
        json_ajax("post", url, "json", false, {
            "assignmentId": treeNode.id, "isChecked": treeNode.checked, "monitorType": "monitor", "webType": 1
        }, function (data) {
            var addV = treeObj.addNodes(treeNode, JSON.parse(ungzip(data.msg)));
            if (addV !== null) {
                var param = [];
                for (var i = 0; i < treeNode.children.length; i++) {
                    var obj = new Object();
                    obj.vehicleID = treeNode.children[i].id;
                    param.push(obj)
                }
                // 订阅所有车个
                requestStrS = {
                    "desc": {
                        "MsgId": 40964,
                        "UserName": $("#userName").text()
                    },
                    "data": param
                };
                treeMonitoring.initRunAndStopNum();
                webSocket.subscribe(headers, '/user/' + $("#userName").text() + '/cachestatus', treeMonitoring.updataRealTree, "/app/vehicle/subscribeCacheStatusNew", requestStrS);
                if (crrentSubV.length !== 0) {
                    for (var i = 0; i < crrentSubV.length; i++) {
                        var list = zTreeIdJson[crrentSubV[i]];
                        if (list != null) {
                            $.each(list, function (index, value) {
                                var znode = treeObj.getNodeByTId(value);
                                treeObj.checkNode(znode, true, true);
                                znode.checkedOld = true;
                                treeObj.updateNode(znode);
                            })
                        }
                    }
                }
            }
        })
    },
    //树优化测试代码块todo
    zTreeOnExpand: function (event, treeId, treeNode) {
        var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
        treeNodeNew = treeNode; //获取当前展开节点
        if (treeNode.pId !== null) {
            if (treeNode.children === undefined && treeNode.type == "assignment") {
                treeMonitoring.getChildNode(treeNode);
            } else if (treeNode.type == "group") {
                var url = "/clbs/m/functionconfig/fence/bindfence/putMonitorByGroup";
                json_ajax("post", url, "json", false, {
                    "groupId": treeNode.id
                    , "isChecked": treeNode.checked, "monitorType": "vehicle"
                }, function (data) {
                    var result = data.obj;
                    if (result != null && result != undefined) {
                        $.each(result, function (i) {
                            var pid = i; //获取键值
                            var chNodes = result[i]; //获取对应的value
                            var parentTid = zTreeIdJson[pid][0];
                            var parentNode = treeObj.getNodeByTId(parentTid);
                            if (parentNode.children === undefined) {
                                treeObj.addNodes(parentNode, chNodes);
                            }
                        });
                    }
                })

            } else {
                if (crrentSubV.length !== 0) {
                    for (var i = 0; i < crrentSubV.length; i++) {
                        var list = zTreeIdJson[crrentSubV[i]];
                        if (list != null) {
                            $.each(list, function (index, value) {
                                var znode = treeObj.getNodeByTId(value);
                                treeObj.checkNode(znode, true, true);
                                znode.checkedOld = true;
                                treeObj.updateNode(znode);
                            })
                        }
                    }
                }
                var param = [];
                for (var i = 0; i < treeNode.children.length; i++) {
                    var obj = new Object();
                    obj.vehicleID = treeNode.children[i].id;
                    param.push(obj)
                }
                // 订阅所有车个
                requestStrS = {
                    "desc": {
                        "MsgId": 40964,
                        "UserName": $("#userName").text()
                    },
                    "data": param
                };
                treeMonitoring.isExpendSocket(requestStrS);
            }
        }
    },

    // socket 订阅连接
    isExpendSocket: function (requestStrS) {
        setTimeout(function () {
            if (webSocket.conFlag) {
                treeMonitoring.initRunAndStopNum();
                webSocket.subscribe(headers, '/user/' + $("#userName").text() + '/cachestatus', treeMonitoring.updataRealTree, "/app/vehicle/subscribeCacheStatusNew", requestStrS);
            } else {
                treeMonitoring.isExpendSocket(requestStrS);
            }
        }, 1000);
    },

    getGroupChild: function (node, assign) { // 递归获取组织及下级组织的分组节点
        var nodes = node.children;
        if (nodes != null && nodes != undefined && nodes.length > 0) {
            for (var i = 0; i < nodes.length; i++) {
                var node = nodes[i];
                if (node.type == "assignment") {
                    assign.push(node);
                } else if (node.type == "group" && node.children != undefined) {
                    treeMonitoring.getGroupChild(node.children, assign);
                }
            }
        }
    },

    //筛选在线车个
    onlines: function (event) {
        changeMiss = [];
        lineAs = [];//停车
        lineAr = [];//行驶
        lineAa = [];////报警
        lineAm = [];//未定位
        lineOs = [];//超速
        lineHb = [];//心跳
        misstype = false;
        misstypes = false;
        suFlag = true;
        //用于判断在线车个是否展开节点
        onLineIsExpandAll = true;
        zTreeIdJson = {};
        allflag = false;
        fzzflag = false;
        $("#search_condition").val("");
        var settingTree = {
            async: {
                url: "/clbs/m/basicinfo/monitoring/vehicle/treeStateInfo",
                type: "post",
                enable: true,
                autoParam: ["id"],
                dataType: "json",
                otherParam: {"type": event.data.type, "webType": 1},
            },
            view: {
                addHoverDom: treeMonitoring.addHoverDom,
                removeHoverDom: treeMonitoring.removeHoverDom,
                dblClickExpand: false,
                nameIsHTML: true,
                fontCss: setFontCss_ztree
            },
            check: {
                enable: true,
                chkStyle: "checkbox",
                chkboxType: {
                    "Y": "s",
                    "N": "s"
                },
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
                onClick: treeMonitoring.onClickV,
                onDblClick: treeMonitoring.onDbClickV,
                beforeClick: treeMonitoring.zTreeBeforeClick,
                onCheck: treeMonitoring.onCheckVehicle,
                onAsyncSuccess: treeMonitoring.zTreeOnAsyncSuccess,
                onExpand: treeMonitoring.zTreeOnExpand,
                onNodeCreated: treeMonitoring.zTreeOnNodeCreated,
                onRightClick: treeMonitoring.zTreeShowRightMenu,
            }
        };
        $.fn.zTree.init($("#treeDemo"), settingTree, null);
        if (event.data.type === 1) {
            $online.css("text-decoration", "underline");
            $chooseOverSeep.css("text-decoration", "none");
            $chooseStop.css("text-decoration", "none");
            $chooseRun.css("text-decoration", "none");
            $chooseNot.css("text-decoration", "none");
            $chooseAlam.css("text-decoration", "none");
            $chooseMiss.css("text-decoration", "none");
            $('#chooseHeartBeat').css("text-decoration", "none");
        } else if (event.data.type === 2) {
            $chooseStop.css("text-decoration", "underline");
            $chooseOverSeep.css("text-decoration", "none");
            $online.css("text-decoration", "none");
            $chooseRun.css("text-decoration", "none");
            $chooseNot.css("text-decoration", "none");
            $chooseAlam.css("text-decoration", "none");
            $chooseMiss.css("text-decoration", "none");
            $('#chooseHeartBeat').css("text-decoration", "none");
        } else if (event.data.type === 3) {
            $chooseRun.css("text-decoration", "underline");
            $chooseStop.css("text-decoration", "none");
            $chooseOverSeep.css("text-decoration", "none");
            $online.css("text-decoration", "none");
            $chooseNot.css("text-decoration", "none");
            $chooseAlam.css("text-decoration", "none");
            $chooseMiss.css("text-decoration", "none");
            $('#chooseHeartBeat').css("text-decoration", "none");
        } else if (event.data.type === 4) {
            $chooseAlam.css("text-decoration", "underline");
            $chooseRun.css("text-decoration", "none");
            $chooseStop.css("text-decoration", "none");
            $chooseOverSeep.css("text-decoration", "none");
            $online.css("text-decoration", "none");
            $chooseNot.css("text-decoration", "none");
            $chooseMiss.css("text-decoration", "none");
            $('#chooseHeartBeat').css("text-decoration", "none");
        } else if (event.data.type === 5) {
            $chooseOverSeep.css("text-decoration", "underline");
            $chooseAlam.css("text-decoration", "none");
            $chooseRun.css("text-decoration", "none");
            $chooseStop.css("text-decoration", "none");
            $online.css("text-decoration", "none");
            $chooseNot.css("text-decoration", "none");
            $chooseMiss.css("text-decoration", "none");
            $('#chooseHeartBeat').css("text-decoration", "none");
        } else if (event.data.type === 6) {
            $chooseNot.css("text-decoration", "underline");
            $chooseOverSeep.css("text-decoration", "none");
            $chooseAlam.css("text-decoration", "none");
            $chooseRun.css("text-decoration", "none");
            $chooseStop.css("text-decoration", "none");
            $online.css("text-decoration", "none");
            $chooseMiss.css("text-decoration", "none");
            $('#chooseHeartBeat').css("text-decoration", "none");
        } else if (event.data.type === 9) {
            $('#chooseHeartBeat').css("text-decoration", "underline");
            $chooseNot.css("text-decoration", "none");
            $chooseOverSeep.css("text-decoration", "none");
            $chooseAlam.css("text-decoration", "none");
            $chooseRun.css("text-decoration", "none");
            $chooseStop.css("text-decoration", "none");
            $online.css("text-decoration", "none");
            $chooseMiss.css("text-decoration", "none");
        }
    },
    alltree: function () {
        fzzflag = false;
        missAll = false;
        bflag = true;
        changeMiss = [];
        lineAs = [];//停车
        lineAr = [];//行驶
        lineAa = [];////报警
        lineAm = [];//未定位
        lineOs = [];//超速
        lineHb = [];//心跳
        misstype = false;
        misstypes = false;
        suFlag = true;
        zTreeIdJson = {};
        $("#search_condition").val("");
        var otherParam = null;
        if ($tableCarAll.text() <= 300) {
            var zurl = "/clbs/m/functionconfig/fence/bindfence/monitorTree";
            otherParam = {"webType": 1};
        } else {
            var zurl = "/clbs/m/functionconfig/fence/bindfence/bigDataMonitorTree";
        }
        var allsetting = {
            async: {
                url: zurl,
                type: "post",
                enable: true,
                autoParam: ["id"],
                otherParam: otherParam,
                dataType: "json",
                dataFilter: treeMonitoring.ajaxDataFilter
            },
            view: {
                addHoverDom: treeMonitoring.addHoverDom,
                removeHoverDom: treeMonitoring.removeHoverDom,
                dblClickExpand: false,
                nameIsHTML: true,
                fontCss: setFontCss_ztree
            },
            check: {
                enable: true,
                chkStyle: "checkbox",
                chkboxType: {
                    "Y": "s",
                    "N": "s"
                },
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
                onClick: treeMonitoring.onClickV,
                beforeDblClick: treeMonitoring.zTreeBeforeDblClick,
                onDblClick: treeMonitoring.onDbClickV,
                beforeClick: treeMonitoring.zTreeBeforeClick,
                beforeCheck: treeMonitoring.zTreeBeforeCheck,
                onCheck: treeMonitoring.onCheckVehicle,
                beforeAsync: treeMonitoring.zTreeBeforeAsync,
                onAsyncSuccess: treeMonitoring.zTreeOnAsyncSuccess,
                beforeExpand: treeMonitoring.zTreeBeforeExpand,
                onExpand: treeMonitoring.zTreeOnExpand,
                onNodeCreated: treeMonitoring.zTreeOnNodeCreated,
                onRightClick: treeMonitoring.zTreeShowRightMenu,
            }
        };
        $.fn.zTree.init($("#treeDemo"), allsetting);
        $chooseNot.css("text-decoration", "none");
        $chooseOverSeep.css("text-decoration", "none");
        $chooseAlam.css("text-decoration", "none");
        $chooseRun.css("text-decoration", "none");
        $chooseStop.css("text-decoration", "none");
        $online.css("text-decoration", "none");
        $chooseMiss.css("text-decoration", "none");
        bflag = false
    },
    // 筛选离线车个
    misslines: function (event) {
        fzzflag = false;
        allflag = false;
        missAll = true;
        bflag = true;
        changeMiss = [];
        lineAs = [];//停车
        lineAr = [];//行驶
        lineAa = [];////报警
        lineAm = [];//未定位
        lineOs = [];//超速
        lineHb = [];//心跳
        var numberMiss = 7;
        if (event == 0) {
            misstypes = true;
            numberMiss = 8;
        } else {
            misstypes = false;
        }
        misstype = true;
        suFlag = true;
        zTreeIdJson = {};
        var otherParam = null;
        $("#search_condition").val("");
        if ($("#table-car-offline").text() <= 300) {
            zurl = "/clbs/m/basicinfo/monitoring/vehicle/treeStateInfo";
            otherParam = {"type": numberMiss, "webType": 1}
        } else {
            zurl = "/clbs/m/functionconfig/fence/bindfence/bigDataMonitorTree";
        }
        var settingTree = {
            async: {
                url: zurl,
                type: "post",
                enable: true,
                autoParam: ["id"],
                otherParam: otherParam,
                dataType: "json",
                dataFilter: treeMonitoring.ajaxDataFilter
            },
            view: {
                addHoverDom: treeMonitoring.addHoverDom,
                removeHoverDom: treeMonitoring.removeHoverDom,
                dblClickExpand: false,
                nameIsHTML: true,
                fontCss: setFontCss_ztree
            },
            check: {
                enable: true,
                chkStyle: "checkbox",
                chkboxType: {
                    "Y": "s",
                    "N": "s"
                },
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
                onClick: treeMonitoring.onClickV,
                onDblClick: treeMonitoring.onDbClickV,
                beforeClick: treeMonitoring.zTreeBeforeClick,
                beforeCheck: treeMonitoring.zTreeBeforeCheck,
                onCheck: treeMonitoring.onCheckVehicle,
                beforeAsync: treeMonitoring.zTreeBeforeAsync,
                onAsyncSuccess: treeMonitoring.zTreeOnAsyncSuccess,
                onExpand: treeMonitoring.zTreeOnExpand,
                onNodeCreated: treeMonitoring.zTreeOnNodeCreated,
                onRightClick: treeMonitoring.zTreeShowRightMenu,
            }
        };
        $.fn.zTree.init($("#treeDemo"), settingTree, null);
        $online.css("text-decoration", "none");
        $chooseOverSeep.css("text-decoration", "none");
        $chooseStop.css("text-decoration", "none");
        $chooseRun.css("text-decoration", "none");
        $chooseNot.css("text-decoration", "none");
        $chooseAlam.css("text-decoration", "none");
        $chooseMiss.css("text-decoration", "underline");
        bflag = false;
    },
    setNumber: function (data) {
        $("#tline").text("(" + data.obj.onlineNum + ")");
        $("#tmiss").text("(" + (data.obj.allV - data.obj.onlineNum) + ")");
        $("#table-car-online").text(data.obj.onlineNum);
        $("#table-car-offline").text(data.obj.allV - data.obj.onlineNum);
        $("#table-car-run").text(data.obj.runArrNum);
        $("#table-car-stop").text(data.obj.onlineParkNum);
        $("#tall").text("(" + data.obj.allV + ")");
        $tableCarAll.text(data.obj.allV);
        if (data.obj.allV !== 0) {
            $tableCarOnlinePercent.text((data.obj.onlineNum / data.obj.allV * 100).toFixed(2) + "%");
        } else {
            $tableCarOnlinePercent.text("0%")
        }
        runVidArray = data.obj.runVidArray;
        stopVidArray = data.obj.stopVidArray;
        diyueall = data.obj.vehicleIdArray;
        var otherParam = null;
        if (data.obj.allV <= 300) {
            zurl = "/clbs/m/functionconfig/fence/bindfence/monitorTree";
            otherParam = {"webType": 1};
        } else {
            zurl = "/clbs/m/functionconfig/fence/bindfence/bigDataMonitorTree";
        }
        // 初始化文件树
        setting = {
            async: {
                url: zurl,
                type: "post",
                enable: true,
                autoParam: ["id"],
                otherParam: otherParam,
                dataType: "json",
                dataFilter: treeMonitoring.ajaxDataFilter
            },
            view: {
                addHoverDom: treeMonitoring.addHoverDom,
                removeHoverDom: treeMonitoring.removeHoverDom,
                dblClickExpand: false,
                nameIsHTML: true,
                fontCss: setFontCss_ztree
            },
            check: {
                enable: true,
                chkStyle: "checkbox",
                chkboxType: {
                    "Y": "s",
                    "N": "s"
                }
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
                onClick: treeMonitoring.onClickV,
                beforeDblClick: treeMonitoring.zTreeBeforeDblClick,
                onDblClick: treeMonitoring.onDbClickV,
                beforeClick: treeMonitoring.zTreeBeforeClick,
                beforeCheck: treeMonitoring.zTreeBeforeCheck,
                onCheck: treeMonitoring.onCheckVehicle,
                beforeAsync: treeMonitoring.zTreeBeforeAsync,
                onAsyncSuccess: treeMonitoring.zTreeOnAsyncSuccess,
                beforeExpand: treeMonitoring.zTreeBeforeExpand,
                onExpand: treeMonitoring.zTreeOnExpand,
                onNodeCreated: treeMonitoring.zTreeOnNodeCreated,
                onRightClick: treeMonitoring.zTreeShowRightMenu
            }
        };
        $.fn.zTree.init($("#treeDemo"), setting);
        zTree = $.fn.zTree.getZTreeObj("treeDemo");
        treeMonitoring.isGetSocket();
    },
    isGetSocket: function () {
        if (webSocket.conFlag) {
            treeMonitoring.initRunAndStopNum();
            webSocket.subscribe(headers, '/user/' + $("#userName").text() + '/cachestatus', treeMonitoring.updataRealTree, null, null);
            setTimeout(function () {
                webSocket.subscribe(headers, '/user/' + $("#userName").text() + '/alarm', treeMonitoring.updataRealAlarmMessage, "/app/vehicle/subscribeStatus", null);
                webSocket.subscribe(headers, '/user/' + $("#userName").text() + '/riskInfo', function () {
                    if ($('#activeSafetyTab').hasClass('active')) {
                        dataTableOperation.updataActiveSafetyMessage()
                    }
                }, "/app/vehicle/subscribeStatus", null);
            }, 1000);
        } else {
            setTimeout(function () {
                treeMonitoring.isGetSocket();
            }, 200);
        }

    },
    getIcoTreeUrl: function (treeId, treeNode) {
        if (treeNode == null) {
            return "/clbs/m/personalized/ico/IcoTree";
        } else if (treeNode.type == "assignment") {
            return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked;
        }
    },
    zTreeBeforeAsync: function () {
        return bflag;
    },
    zTreeBeforeCheck: function (treeId, treeNode) {
        var zTree = $.fn.zTree.getZTreeObj("treeDemo");
        var changedNodes = zTree.getChangeCheckedNodes();
        var flag = true;
        if (fzzflag && !treeNode.checked) {
            var queryType = $("#searchType").val();
            var queryParam = $("#search_condition").val();
            if (treeNode.type === 'group' && treeNode.pId === null) {
                json_ajax("POST", "/clbs/a/search/monitorTreeFuzzyCount", "json", false, {
                    "queryParam": queryParam,
                    "queryType": queryType
                }, function (data) {
                    if (data > 400) {
                        cheackGourpNum = data;
                        layer.alert("我们的监控上限是400个,您刚刚勾选了" + cheackGourpNum + "个,请重新勾选！");
                        flag = false;
                    }
                });
            }
            return flag;
        }
        if (treeNode.type === 'group' && !fzzflag) {
            json_ajax("POST", "/clbs/m/functionconfig/fence/bindfence/subGroup", "json", false, {
                "pid": treeNode.id,
                "type": treeNode.type
            }, function (data) {
                if (missAll) {
                    cheackGourpNum = (data.obj.num) - ($("#table-car-online").text());
                    if (cheackGourpNum > 400) {
                        layer.alert("我们的监控上限是400个,您刚刚勾选了" + cheackGourpNum + "个,请重新勾选！");
                        flag = false;
                    }
                } else {
                    if (data.obj.num > 400) {
                        cheackGourpNum = data.obj.num;
                        layer.alert("我们的监控上限是400个,您刚刚勾选了" + data.obj.num + "个,请重新勾选！");
                        flag = false;
                    }
                }
            });
        }
        return flag;
    },
    zTreeBeforeExpand: function () {
        return eflag;
    },
    zTreeBeforeDblClick: function (treeId, treeNode) {
        var flag = true;
        if (treeNode.type === 'group') {
            json_ajax("POST", "/clbs/m/functionconfig/fence/bindfence/subGroup", "json", false, {
                "pid": treeNode.id,
                "type": treeNode.type
            }, function (data) {
                if (data.obj.num > 400) {
                    cheackGourpNum = data.obj.num
                    layer.alert("我们的监控上限是400个,您刚刚勾选了" + data.obj.num + "个,请重新勾选！")
                    flag = false;
                }
            });
        }
        return flag;
    },
    getVehicleArr: function (data) {
        diyueall = data.obj;
    },
    //企业树显示
    enterpriseShow: function () {
        var this_tree = $(this).siblings('div.ztreeModelBox');
        if (this_tree.is(":hidden")) {
            var width = this_tree.parent('div').width();
            this_tree.css('width', width + 'px');
            this_tree.show();
        } else {
            this_tree.hide();
        }
        ;
    },
    //根据车id查询当前车个绑定围栏信息
    getCurrentVehicleAllFence: function (vId) {
        var fenceSetting = {
            async: {
                url: "/clbs/m/functionconfig/fence/bindfence/fenceTreeByVid",
                type: "post",
                enable: true,
                autoParam: ["id"],
                dataType: "json",
                otherParam: {"vid": vId}, //监控对象ID
                dataFilter: treeMonitoring.FenceAjaxDataFilter
            },
            view: {
                dblClickExpand: false,
                nameIsHTML: true,
                fontCss: setFontCss_ztree
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
            data: {
                simpleData: {
                    enable: true
                },
                key: {
                    title: "name"
                }
            },
            callback: {
                onClick: treeMonitoring.vFenceTreeClick,
                onCheck: treeMonitoring.vFenceTreeCheck
            }
        };
        $.fn.zTree.init($("#vFenceTree"), fenceSetting, null);
        //IE9（模糊查询）
        if (navigator.appName == "Microsoft Internet Explorer" && navigator.appVersion.split(";")[1].replace(/[ ]/g, "") == "MSIE9.0") {
            var search;
            $("#vFenceSearch").bind("focus", function () {
                search = setInterval(function () {
                    search_ztree('vFenceTree', 'vFenceSearch', 'fence');
                }, 500);
            }).bind("blur", function () {
                clearInterval(search);
            });
        }
    },
    //围栏集合数据清除及切换后初始化
    delFenceListAndMapClear: function () {
        //清除根据监控对象查询的围栏勾选
        var zTree = $.fn.zTree.getZTreeObj("vFenceTree");
        //处理判断不勾选围栏直接切换至电子围栏后错误问题
        if (zTree != null) {
            var nodes = zTree.getCheckedNodes(true);
            //获取已勾选的节点结合  变换为不勾选
            for (var i = 0, l = nodes.length; i < l; i++) {
                zTree.checkNode(nodes[i], false, false);
            }
            //改变勾选状态checkedOld
            var allNodes = zTree.getChangeCheckedNodes();
            for (var i = 0; i < allNodes.length; i++) {
                allNodes[i].checkedOld = false;
            }
            //删除 标注、线、矩形、圆形、多边形 （集合fenceIdList）
            if (fenceIdList.elements.length > 0) {
                var fLength = fenceIdList.elements.length;
                //遍历当前勾选围栏
                for (var i = 0; i < fLength; i++) {
                    //获取围栏Id
                    var felId = fenceIdList.elements[i].key;
                    //隐藏围栏及删除数组数据
                    var felGs = fenceIdList.get(felId);
                    //AMap.Marker标注    AMap.Polyline线    AMap.Polygon矩形   AMap.Circle圆形
                    if (felGs.CLASS_NAME == "AMap.Marker" || felGs.CLASS_NAME == "AMap.Polyline" || felGs.CLASS_NAME == "AMap.Polygon" || felGs.CLASS_NAME == "AMap.Circle") {
                        felGs.hide();
                    }
                }
                //清空数组
                fenceIdList.clear();
            }
            //删除行政区域 （集合AdministrativeRegionsList）
            if (AdministrativeRegionsList.elements.length > 0) {
                var aLength = AdministrativeRegionsList.elements.length;
                for (var i = 0; i < aLength; i++) {
                    var admId = AdministrativeRegionsList.elements[i].key;
                    var admGs = AdministrativeRegionsList.get(admId);
                    map.remove(admGs);
                }
                AdministrativeRegionsList.clear();
            }
            //删除导航路线 （集合travelLineList）
            if (travelLineList.elements.length > 0) {
                var tLength = travelLineList.elements.length;
                for (var i = 0; i < tLength; i++) {
                    var travelId = travelLineList.elements[i].key;
                    var travelGs = travelLineList.get(travelId);
                    map.remove([travelGs]);
                }
                travelLineList.clear();
            }
        }
    },
    //电子围栏及监控对象切换
    fenceAndVehicleFn: function () {
        var id = $(this).attr("id");
        //隐藏地图工具栏相关
        if (!($("#mapDropSettingMenu").is(":hidden"))) {
            $("#mapDropSettingMenu").hide();//地图设置
        }
        if (!($("#disSetMenu").is(":hidden"))) {
            $("#disSetMenu").hide();//显示设置
        }
        $("#toolOperateClick").animate({marginRight: "-776px"});
        //判断点击电子围栏
        if (id == "TabCarBox") {
            $("#fenceTool").hide();
            treeMonitoring.delFenceListAndMapClear();
        } else {
            $("#fenceTool").show();
        }
    },
    //当前监控对象围栏点击
    vFenceTreeClick: function (e, treeId, treeNode) {
        var zTree = $.fn.zTree.getZTreeObj("vFenceTree");
        zTree.checkNode(treeNode, !treeNode.checked, true);
        treeMonitoring.vFenceTreeCheck(e, treeId, treeNode);
        return false;
        treeMonitoring.showZtreeCheckedToMap(treeNode, zTree);
    },
    //当前监控对象围栏勾选
    vFenceTreeCheck: function (e, treeId, treeNode) {
        var zTree = $.fn.zTree.getZTreeObj("vFenceTree");
        if (treeNode.checked) {
            zTree.expandNode(treeNode, true, true, true, true); // 展开节点
        }
        var nodes = zTree.getCheckedNodes(true);
        treeMonitoring.showZtreeCheckedToMap(treeNode, zTree);
    },
    //显示当前勾选对象围栏到地图
    showZtreeCheckedToMap: function (treeNode, zTree) {
        //判断选中属性
        if (treeNode.checked == true) {
            //获取勾选状态被改变的节点集合
            var changeNodes = zTree.getChangeCheckedNodes();
            for (var i = 0, len = changeNodes.length; i < len; i++) {
                changeNodes[i].checkedOld = true;
            }
            for (var j = 0; j < changeNodes.length; j++) {
                var nodesId = changeNodes[j].id;
                treeMonitoring.showFenceInfo(nodesId, changeNodes[j]);
            }
        } else {
            var changeNodes = zTree.getChangeCheckedNodes();
            for (var i = 0, len = changeNodes.length; i < len; i++) {
                changeNodes[i].checkedOld = false;
                zTree.cancelSelectedNode(changeNodes[i]);
                var nodesId = changeNodes[i].id;
                treeMonitoring.hideFenceInfo(nodesId);
            }
        }
    },
    //当前监控对象围栏查询
    vsearchFenceCarSearch: function () {
        search_ztree('vFenceTree', 'vFenceSearch', 'fence');
    },
    //当前监控对象围栏预处理的函数
    FenceAjaxDataFilter: function (treeId, parentNode, responseData) {
        if (responseData) {
            for (var i = 0; i < responseData.length; i++) {
                responseData[i].open = false;
            }
        }
        return responseData;
    },
    //车个树单双击获取当前围栏信息
    vehicleTreeClickGetFenceInfo: function (treeStatus, treeId) {
        if (treeStatus == true) {
            //清空搜索条件
            if ($("#vFenceSearch").val() != "" || $("#vFenceSearch").val() != null) {
                $("#vFenceSearch").val("");
            }
            //清空围栏数组及地图
            treeMonitoring.delFenceListAndMapClear();
            //订阅后查询当前对象绑定围栏信息
            treeMonitoring.getCurrentVehicleAllFence(treeId);
            //显示围栏树及搜索 隐藏消息提示
            $("#vFenceTree").removeClass("hidden");
            $("#vSearchContent").removeClass("hidden");
            $("#vFenceMsg").addClass("hidden");
        } else {
            $("#vFenceTree").html("").addClass("hidden");
            $("#vSearchContent").addClass("hidden");
            $("#vFenceMsg").removeClass("hidden");
        }
    },
    //围栏显示隐藏
    fenceToolClickSHFn: function () {
        if ($("#fenceTool>.dropdown-menu").is(":hidden")) {
            $("#fenceTool>.dropdown-menu").show();
        } else {
            $("#fenceTool>.dropdown-menu").hide();
        }
    },
    //显示围栏
    getFenceDetailInfo: function (fenceNode, showMap) {
        layer.load(2);

        var params = {
            fenceId:fenceNode[0].shape,
            type:fenceNode[0].type,
        };

        $.ajax({
            type: "POST",
            url: "/clbs/m/regionManagement/fenceManagement/getFenceDetail",
            data: params,
            dataType: "json",
            success: function (data) {
                layer.closeAll('loading');

                if (data.success) {
                    var dataList = data.obj;
                    var fenceType = dataList.type;
                    var fenceData = dataList.fenceInfo;

                    if(dataList && fenceData){
                        switch (fenceType){
                            case 'zw_m_polygon'://多边形
                                treeMonitoring.drawPolygonToMap(fenceData, showMap);
                                break;
                            case 'zw_m_circle'://圆形
                                treeMonitoring.drawCircleToMap(fenceData, showMap);
                                break;
                            case 'zw_m_line'://路线
                                var lineSpot = fenceData.lineSpot == undefined ? [] : fenceData.lineSpot;
                                var lineSegment = fenceData.lineSegment == undefined ? [] : fenceData.lineSegment;
                                treeMonitoring.drawLineToMap(fenceData, lineSpot, lineSegment, showMap);
                                break;
                            case 'zw_m_marker'://标注
                                treeMonitoring.drawMarkToMap(fenceData, showMap);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        });
    },
    //显示行政区域
    drawAdministrationToMap: function (data, aId, showMap) {
        var polygonAarry = [];
        if (AdministrativeRegionsList.containsKey(aId)) {
            var this_fence = AdministrativeRegionsList.get(aId);
            map.remove(this_fence);
            AdministrativeRegionsList.remove(aId);
        }
        for (var i = 0, l = data.length; i < 1; i++) {
            var polygon = new AMap.Polygon({
                map: map,
                strokeWeight: 1,
                strokeColor: '#CC66CC',
                fillColor: '#CCF3FF',
                fillOpacity: 0.5,
                path: data
            });
            polygonAarry.push(polygon);
        }
        AdministrativeRegionsList.put(aId, polygonAarry);
        map.setFitView(polygon);//地图自适应
    },
    //标注
    drawMarkToMap: function (mark, thisMap) {
        var markId = mark.id;
        //判断集合中是否含有指定的元素
        if (fenceIdList.containsKey(markId)) {
            var markerObj = fenceIdList.get(markId);
            thisMap.remove(markerObj);
            fenceIdList.remove(markId);
        }
        var dataArr = [];
        dataArr.push(mark.longitude);
        dataArr.push(mark.latitude);
        polyFence = new AMap.Marker({
            position: dataArr,
            offset: new AMap.Pixel(-9, -23)
        });
        polyFence.setMap(thisMap);
        thisMap.setFitView(polyFence);
        fenceIdList.put(markId, polyFence);

        polyFence.on('mouseover', function (e) {
            $('#createName').html(mark.name);
            $('#createType').html(mark.type);
            $('#createPer').html(mark.createDataUsername);
            $('#createTime').html(mark.createDataTime);
            $('#createDesc').html(mark.description);

            var alertContH = $('#markerInfoWindow').height()
            $('#markerInfoWindow').css({
                'left': e.pixel.x - 98 + 'px',
                'top': e.pixel.y - alertContH - 20 + 'px'
            }).show()
        })

        polyFence.on('mouseout', function (e) {

            $('#markerInfoWindow').hide()
        })
        thisMap.on('resize', function (e) {
            $('#markerInfoWindow').hide()
        })
    },
    //矩形
    drawRectangleToMap: function (rectangle, thisMap) {
        var rectangleId = rectangle.id;
        if (fenceIdList.containsKey(rectangleId)) {
            var thisFence = fenceIdList.get(rectangleId);
            thisFence.show();
            map.setFitView(thisFence);
        }
        else {
            var dataArr = new Array();
            if (rectangle != null) {
                dataArr.push([rectangle.leftLongitude, rectangle.leftLatitude]); // 左上角
                dataArr.push([rectangle.rightLongitude, rectangle.leftLatitude]); // 右上角
                dataArr.push([rectangle.rightLongitude, rectangle.rightLatitude]); // 右下角
                dataArr.push([rectangle.leftLongitude, rectangle.rightLatitude]); // 左下角
            }
            polyFence = new AMap.Polygon({
                path: dataArr,//设置多边形边界路径
                strokeColor: "#FF33FF", //线颜色
                strokeOpacity: 0.2, //线透明度
                strokeWeight: 3, //线宽
                fillColor: "#1791fc", //填充色
                fillOpacity: 0.35
                //填充透明度
            });
            polyFence.setMap(thisMap);
            thisMap.setFitView(polyFence);
            fenceIdList.put(rectangleId, polyFence);
        }
    },
    //多边形
    drawPolygonToMap: function (polygon, thisMap) {
        var polygonId = polygon[0].polygonId;
        if (fenceIdList.containsKey(polygonId)) {
            var thisFence = fenceIdList.get(polygonId);
            thisFence.hide();
            fenceIdList.remove(polygonId);
        }
        var dataArr = new Array();
        if (polygon != null && polygon.length > 0) {
            for (var i = 0; i < polygon.length; i++) {
                dataArr.push([polygon[i].longitude, polygon[i].latitude]);
            }
        }
        polyFence = new AMap.Polygon({
            path: dataArr,//设置多边形边界路径
            strokeColor: "#FF33FF", //线颜色
            strokeOpacity: 0.2, //线透明度
            strokeWeight: 3, //线宽
            fillColor: "#1791fc", //填充色
            fillOpacity: 0.35
            //填充透明度
        });
        polyFence.setMap(thisMap);
        thisMap.setFitView(polyFence);
        fenceIdList.put(polygonId, polyFence);
    },
    //圆形
    drawCircleToMap: function (circle, thisMap) {
        var circleId = circle.id;
        if (fenceIdList.containsKey(circleId)) {
            var thisFence = fenceIdList.get(circleId);
            thisFence.hide();
            fenceIdList.remove(circleId);
        }
        polyFence = new AMap.Circle({
            center: new AMap.LngLat(circle.longitude, circle.latitude),// 圆心位置
            radius: circle.radius, //半径
            strokeColor: "#F33", //线颜色
            strokeOpacity: 1, //线透明度
            strokeWeight: 3, //线粗细度
            fillColor: "#ee2200", //填充颜色
            fillOpacity: 0.35
            //填充透明度
        });
        polyFence.setMap(thisMap);
        thisMap.setFitView(polyFence);
        fenceIdList.put(circleId, polyFence);
    },
    //行驶路线
    drawTravelLineToMap: function (data, thisMap, travelLine, wayPointArray) {
        var lineID = travelLine.id;
        var path = [];
        var start_point_value = [travelLine.startLongitude, travelLine.startLatitude];
        var end_point_value = [travelLine.endLongitude, travelLine.endLatitude];
        var wayValue = [];
        if (wayPointArray != undefined) {
            for (var j = 0, len = wayPointArray.length; j < len; j++) {
                wayValue.push([wayPointArray[j].longitude, wayPointArray[j].latitude]);
            }
        }
        for (var i = 0, len = data.length; i < len; i++) {
            path.push([data[i].longitude, data[i].latitude]);
        }
        if (travelLineList.containsKey(lineID)) {
            var this_line = travelLineList.get(lineID);
            map.remove([this_line]);
            travelLineList.remove(lineID);
        }
        var polyFencec = new AMap.Polyline({
            path: path, //设置线覆盖物路径
            strokeColor: "#3366FF", //线颜色
            strokeOpacity: 1, //线透明度
            strokeWeight: 5, //线宽
            strokeStyle: "solid", //线样式
            strokeDasharray: [10, 5],
            zIndex: 51
        });
        polyFencec.setMap(map);
        map.setFitView(polyFencec);
        travelLineList.put(lineID, polyFencec);
    },
    //线
    drawLineToMap: function (line, lineSpot, lineSegment, thisMap) {
        var lineId = line[0].lineId;
        //是否存在线
        if (fenceIdList.containsKey(lineId)) {
            var thisFence = fenceIdList.get(lineId);
            if (Array.isArray(thisFence)) {
                for (var i = 0; i < thisFence.length; i++) {
                    thisFence[i].hide();
                }
            } else {
                thisFence.hide();
            }
            fenceIdList.remove(lineId);
        }
        //线数据
        var dataArr = new Array();
        var lineSectionArray = [];
        if (line != null && line.length > 0) {
            for (var i in line) {
                if (line[i].type == "0") {
                    dataArr[i] = [line[i].longitude, line[i].latitude];
                }
            }
        }
        //地图画线
        var polyFencec = new AMap.Polyline({
            path: dataArr, //设置线覆盖物路径
            strokeColor: "#3366FF", //线颜色
            strokeOpacity: 1, //线透明度
            strokeWeight: 5, //线宽
            strokeStyle: "solid", //线样式
            strokeDasharray: [10, 5],
            zIndex: 51
            //补充线样式
        });
        lineSectionArray.push(polyFencec);
        fenceIdList.put(lineId, polyFencec);
        polyFencec.setMap(thisMap);
        thisMap.setFitView(polyFencec);
    },
    //围栏隐藏
    hideFenceInfo: function (nodesId) {
        if (fenceIdList.containsKey(nodesId)) {
            var thisFence = fenceIdList.get(nodesId);
            if (Array.isArray(thisFence)) {
                for (var i = 0; i < thisFence.length; i++) {
                    thisFence[i].hide();
                }
            } else {
                thisFence.hide();
            }
        }
        treeMonitoring.hideRegionsOrTravel(nodesId);
    },
    //隐藏行政区划及行驶路线
    hideRegionsOrTravel: function (id) {
        //行政区划
        if (AdministrativeRegionsList.containsKey(id)) {
            var this_fence = AdministrativeRegionsList.get(id);
            map.remove(this_fence);
            AdministrativeRegionsList.remove(id);
        }
        //行驶路线
        if (travelLineList.containsKey(id)) {
            var this_fence = travelLineList.get(id);
            map.remove(this_fence);
            travelLineList.remove(id);
        }
    },
    //围栏显示
    showFenceInfo: function (nodesId, node) {
        //判断集合中是否含有指定的元素
        if (fenceIdList.containsKey(nodesId)) {
            var thisFence = fenceIdList.get(nodesId);

            if (thisFence != undefined) {
                if (Array.isArray(thisFence)) {
                    for (var s = 0; s < thisFence.length; s++) {
                        thisFence[s].show();
                        map.setFitView(thisFence[s]);
                    }
                    ;
                } else {
                    thisFence.show();
                    map.setFitView(thisFence);
                }
            }
        } else {
            treeMonitoring.getFenceDetailInfo([node], map);
        }
    },
    treeToTrackBlack: function () {
        var jumpFlag = false;
        var permissionUrls = $("#permissionUrls").val();
        if (permissionUrls != null && permissionUrls != undefined) {
            var urllist = permissionUrls.split(",");
            if (urllist.indexOf("/v/monitoring/trackPlayback") > -1) {
                jumpFlag = true;
                location.href = "/clbs/v/monitoring/trackPlayback";
            }
        }
        if (!jumpFlag) {
            layer.msg("无操作权限，请联系管理员");
        }
    },
    /**
     * 设置终端车牌号
     * @param type
     */
    setPlateNumber: function (type) {
        pageLayout.closeVideo();
        treeMonitoring.getBrandParameter(type);
        parametersID = type;
        $("#terminalPlate").html("设置终端车牌号：" + brand);
        $("#oldNumber").val(brand);
        $("#plateId").val(type);
        $("#setPlateNumber").modal('show');
        rMenu.css({"visibility": "hidden"});
        setTimeout("dataTableOperation.logFindCilck()", 500);
        $("#brand").val('');
        $("#plateColor").val(-1);
        $("#provinceId").val('');
        $("#cityId").val('');
    },

    /**
     * 设置OBD
     */
    setOBD: function (type) {
        pageLayout.closeVideo();
        treeMonitoring.getBrandParameter(type);
        parametersID = type;
        var OBDMultiplicative = [];
        var OBDCommercial = [];
        var url = "/clbs/v/monitoring/findOBD";
        json_ajax("POST", url, "json", false, null, function (data) {
            if (data.success) {
                var datas = data.obj;
                if (datas != null && datas.length > 0) {
                    for (var i = 0; i < datas.length; i++) {
                        if (datas[i].type === 0) {
                            OBDMultiplicative.push({
                                code: datas[i].code,
                                name: datas[i].name
                            });
                        } else if (datas[i].type === 1) {
                            OBDCommercial.push({
                                code: datas[i].code,
                                name: datas[i].name
                            });
                        }
                        obdTypeIdMap.put(datas[i].code, datas[i].id);
                    }
                    $("#model").val(datas[0].name);
                    $("#OBDVid").val(datas[0].code);
                    $("#obdVehicleTypeId").val(datas[0].id);
                    $("#OBDValue").val(1);
                }
                $("#OBDMultiplicative").val(JSON.stringify(OBDMultiplicative));
                $("#OBDCommercial").val(JSON.stringify(OBDCommercial))
            } else {
                layer.msg("获取OBD车型信息失败");
                return
            }
        });
        setTimeout("dataTableOperation.logFindCilck()", 500);
        $("#OBDtitle").html("设置OBD车型信息：" + brand);
        $("#classification").val(1);
        $('#modelName').html("<label class='text-danger'>*</label> 车型名称：");
        $("#OBDId").val(type);
        treeMonitoring.getOBDVehicle();
        $("#OBD").modal('show');
        rMenu.css({"visibility": "hidden"});
        setTimeout("dataTableOperation.logFindCilck()", 500);
        $("#newPlateNumber").val('');
    },

    /**
     * OBD车个类型下拉选
     * @param data
     */
    getOBDVehicle: function () {
        var data;
        var classification = $("#classification").val();
        if (classification == 0) {
            //乘用车
            data = JSON.parse($("#OBDMultiplicative").val());
        } else if (classification == 1) {
            //商用车
            data = JSON.parse($("#OBDCommercial").val());
        }
        var dataList = {value: []};
        if (data != null && data.length > 0) {
            for (var i = 0; i < data.length; i++) {
                dataList.value.push({
                    name: data[i].name,
                    code: data[i].code
                });
            }
            $("#model").val(data[0].name);
            $("#OBDVid").val(data[0].code);
            $("#obdVehicleTypeId").val(obdTypeIdMap.get(data[0].code));
        }
        $("#OBDVName").val('');
        $("#model").bsSuggest("destroy"); // 销毁事件
        $("#model").bsSuggest({
            indexId: 1, //data.value 的第几个数据，作为input输入框的内容
            indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
            data: dataList,
            effectiveFields: ["name"]
        }).on('onDataRequestSuccess', function (e, result) {
        }).on('onSetSelectValue', function (e, keyword, data) {
            $("#OBDVid").val(keyword.id);
            $("#OBDVName").val(keyword.key);
            $("#obdVehicleTypeId").val(obdTypeIdMap.get(keyword.id));
        }).on('onUnsetSelectValue', function () {
        });
    },

}

function showErrorMsg(msg, inputId) {
    if ($("#error_label").is(":hidden")) {
        $("#error_label").text(msg);
        $("#error_label").insertAfter($("#" + inputId));
        $("#error_label").show();
    } else {
        $("#error_label").is(":hidden");
    }
}