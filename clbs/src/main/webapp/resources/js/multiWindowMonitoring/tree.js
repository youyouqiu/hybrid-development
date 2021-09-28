var Tree = function (selector, options, dependency) {
    this.dependency = dependency;
    this.selector = selector;
    this.treeObj = null;
    this.allowAsync = true; // 是否允许异步请求
    this.zTreeIdJson = {}; // 树id和监控对象id的对照表
    this.lastScrollTop = null; // 上一次滚动的 scrolltop
    this.dblClickVid = null; // 双击黄色高亮的监控对象id
    this.clickVid = null; // 单击蓝色高亮的监控对象id
    this.textChange = 0;
}

Tree.prototype.init = function () {
    this.getRunAndStopNumber();
}

Tree.prototype.subscribeAllMonitorStatus = function () {
    if (webSocket.conFlag) {
        webSocket.subscribe(headers, '/user/topic/cachestatus', this.updateTreeColor.bind(this),
            "/app/vehicle/subscribeStatus", null);
    } else {
        setTimeout(function () {
            this.subscribeAllMonitorStatus();
        }.bind(this), 1000);
    }
};

Tree.prototype.getRunAndStopNumber = function () {
    var dataDependency = this.dependency.get('data');
    $.ajax(
        {
            type: 'POST',
            url: '/clbs/m/functionconfig/fence/bindfence/getRunAndStopMonitorNum',
            dataType: 'json',
            async: true,
            data: {"isNeedMonitorId": true, "userName": $("#userName").text()},
            timeout: 8000,
            success: function (data) {
                dataDependency.setAllCount(data.obj.allV);
                dataDependency.setOnlineCount(data.obj.onlineNum);
                dataDependency.setOfflineCount(data.obj.allV - data.obj.onlineNum);
                dataDependency.setRunVidArray(data.obj.runVidArray);
                dataDependency.setStopVidArray(data.obj.stopVidArray);
                dataDependency.setAllVidArray(data.obj.vehicleIdArray);
                dataDependency.setTreeType('all');
            }.bind(this)
        }
    );
}

Tree.prototype.onAllCountChange = function () {
    var dataDependency = this.dependency.get('data');
    $('#tall').html('(' + dataDependency.getAllCount() + ')');
}

Tree.prototype.onOnlineCountChange = function () {
    var dataDependency = this.dependency.get('data');
    $('#tline').html('(' + dataDependency.getOnlineCount() + ')');
}

Tree.prototype.onOffCountChange = function () {
    var dataDependency = this.dependency.get('data');
    $('#tmiss').html('(' + dataDependency.getOfflineCount() + ')');
}

Tree.prototype.setTreeType = function(event){
    var dataDependency = this.dependency.get('data');
    this.resetLoad();
    this.textChange = 0;
    dataDependency.setTreeType($(event.currentTarget).data('type'));
}

Tree.prototype.clearSearch = function(event){
    var dataDependency = this.dependency.get('data');
    dataDependency.setTreeType('all');
    this.resetLoad();
}

Tree.prototype.searchChange = function(){
    if (this.inputChange !== undefined) {
        clearTimeout(this.inputChange);
    }
    this.inputChange = setTimeout(function () {
        this.searchTree();
    }.bind(this), 500);
}

Tree.prototype.searchTree = function(){
    var dataDependency = this.dependency.get('data');
    var queryParam = $("#search_condition").val();
    this.resetLoad();
    $("#treeLoad span").text('正在查询，请稍后');
    this.textChange = 1;
    if (queryParam !== null && queryParam !== "") {
        dataDependency.setTreeType('search');
    }else{
        dataDependency.setTreeType('all');
    }
}

Tree.prototype.refreshTree = function(){
    var dataDependency = this.dependency.get('data');
    this.resetLoad(0);
    this.textChange = 0;
    dataDependency.setTreeType('all');
}

Tree.prototype.treeTypeChange = function(){
    var dataDependency = this.dependency.get('data');
    var treeType = dataDependency.getTreeType();
    if (treeType !== 'search'){
        $('#search_condition').val('');
    }
    this.loadTree();
}

Tree.prototype.resetLoad = function (num) {
    $("#treeLoad").show();
    $("#treeLoad i").css('visibility','visible');
    if(num == 0){
        $("#treeLoad span").text('加载中，请稍后');
    }else {
        $("#treeLoad span").text('正在查询，请稍后');
    }
};

Tree.prototype.getTreeUrlAndParam = function(){
    var dataDependency = this.dependency.get('data');
    var treeType = dataDependency.getTreeType();
    var url,param;
    if (treeType === 'all'){
        var allNum = dataDependency.getAllCount();
        if (allNum <= 300) {
            url = "/clbs/m/functionconfig/fence/bindfence/monitorTree";
            param = {"webType": 1};
        } else {
            url = "/clbs/m/functionconfig/fence/bindfence/bigDataMonitorTree";
        }
    }else if (treeType === 'offline'){
        var offlineNum = dataDependency.getOfflineCount();
        if (offlineNum <= 300) {
            url = "/clbs/m/basicinfo/monitoring/vehicle/treeStateInfo";
            param = {"type": 7, "webType": 1};
        } else {
            url = "/clbs/m/functionconfig/fence/bindfence/bigDataMonitorTree";
        }
    }else if(treeType === 'search'){
        url = "/clbs/m/functionconfig/fence/bindfence/monitorTreeFuzzy";
        var queryType = $("#searchType").val();
        var queryParam = $("#search_condition").val();
        param =  {"queryParam": queryParam, "queryType": queryType, "webType": 1};
    }else {
        url= "/clbs/m/basicinfo/monitoring/vehicle/treeStateInfo";
        if (treeType === 'online'){
            param = {"type": 1, "webType": 1};
        }else if (treeType === 'heartBeat') {
            param = {"type": 9, "webType": 1};
        }else if (treeType === 'notPosition') {
            param = {"type": 6, "webType": 1};
        }else if (treeType === 'run') {
            param = {"type": 3, "webType": 1};
        }else if (treeType === 'stop') {
            param = {"type": 2, "webType": 1};
        }else if (treeType === 'overSpeed') {
            param = {"type": 5, "webType": 1};
        }else if (treeType === 'alarm') {
            param = {"type": 4, "webType": 1};
        }
    }
    return {url:url,param:param};
}

Tree.prototype.loadTree = function () {
    var urlParam = this.getTreeUrlAndParam();
    this.allowAsync = true;
    this.zTreeIdJson = {};
    var setting = {
        async: {
            url: urlParam.url,
            type: "post",
            enable: true,
            autoParam: ["id"],
            otherParam: urlParam.param,
            dataType: "json",
            dataFilter: this.ajaxDataFilter.bind(this)
        },
        view: {
            // addHoverDom: treeMonitoring.addHoverDom,
            // removeHoverDom: treeMonitoring.removeHoverDom,
            dblClickExpand: false,
            nameIsHTML: true,
            fontCss: setFontCss_ztree,
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
            onClick: this.onClick.bind(this),
            // beforeDblClick: treeMonitoring.zTreeBeforeDblClick,
            onDblClick: this.onDbClick.bind(this),
            beforeClick: this.treeBeforeClick.bind(this),
            beforeCheck: this.treeBeforeCheck.bind(this),
            onCheck: this.treeOnCheck.bind(this),
            beforeAsync: this.zTreeBeforeAsync.bind(this),
            onAsyncSuccess: this.zTreeOnAsyncSuccess.bind(this),
            beforeExpand: this.zTreeBeforeExpand.bind(this),
            onExpand: this.zTreeOnExpand.bind(this),
            onNodeCreated: this.zTreeOnNodeCreated.bind(this),
            // onRightClick: treeMonitoring.zTreeShowRightMenu,
        }
    };
    this.treeObj = $.fn.zTree.init($("#treeDemo"), setting);
}

Tree.prototype.ajaxDataFilter = function (treeId, parentNode, responseData) {
    var dataDependency = this.dependency.get('data');
    var treeType = dataDependency.getTreeType();
    switch (treeType){
        case 'all':
        case 'offline':
        case 'search':
            responseData = JSON.parse(ungzip(responseData));
            break;
        default:
            break;
    }

    for (var i = 0; i < responseData.length; i++){
        if (responseData[i].type === 'assignment' || responseData[i].type === 'group'){
            responseData[i].nocheck = true;
        }
    }
    if (treeType === 'search'){
        responseData = filterQueryResult(responseData, null);
    }
    return responseData;
}

Tree.prototype.zTreeBeforeAsync= function () {
    return this.allowAsync;
}

Tree.prototype.zTreeOnAsyncSuccess= function (event, treeId, treeNode, msg) {
    var dataDependency = this.dependency.get('data');

    var treeObj = this.treeObj;
    var nodes = treeObj.getCheckedNodes(true);
    allNodes = treeObj.getNodes();
    if(allNodes.length == 0){
        $("#treeLoad i").css('visibility','hidden');
        if(this.textChange == 0){
            $("#treeLoad span").text('您没有数据');
        }else{
            $("#treeLoad span").text('未找到查询对象');
        }
    }else{
        $("#treeLoad").hide();
    }
    var childNodes = treeObj.transformToArray(allNodes[0]);
    var initLen = 0;
    initArr = [];
    notExpandNodeInit = treeObj.getNodesByFilter(assignmentNotExpandFilter);

    // 为了优化ztree update数据卡顿的情况，修改方案为初始时展开监控对象数量为50个
    var allMonitor = [];
    for (var i = 0; i < notExpandNodeInit.length; i++) {
        allMonitor = objArrRemoveRepeat(allMonitor.concat(notExpandNodeInit[i].children));
        initLen = allMonitor.length;
        // initLen += notExpandNodeInit[i].children.length;
        // if (initLen > 30 && i === 0) {
        //   zTree.expandNode(notExpandNodeInit[i], true, true, false, true);
        // }
        initArr.push(i);
        // if (onLineIsExpandAll) {
        treeObj.expandNode(notExpandNodeInit[i], true, true, false, true);
        // }
        if (initLen > 30) {
            break;
        }
    }

    this.allowAsync = false;

    var activeWindowIndex = dataDependency.getActiveWindowIndex();
    if (activeWindowIndex !== null){
        var subscribeObjArray = dataDependency.getSubscribObjArray();
        for (var i = 0; i < subscribeObjArray.length; i++){
            if (subscribeObjArray[i].windowIndex === activeWindowIndex){
                this.yellowHighlightSameVid(subscribeObjArray[i].vid);
            }
        }
    }
}

Tree.prototype.zTreeBeforeExpand = function (treeId, parentNode, responseData) {
    return true;
}

Tree.prototype.zTreeOnExpand= function (event, treeId, treeNode) {
    var treeObj = this.treeObj;
    var zTreeIdJson = this.zTreeIdJson;
    var subscribVidArray = this.dependency.get('data').getSubscribVidArray();

    treeNodeNew = treeNode; //获取当前展开节点
    if (treeNode.pId !== null) {
        if (treeNode.children === undefined && treeNode.type == "assignment") {
            this.getChildNode4Assignment(treeNode);
        } else if (treeNode.type == "group") {
            this.getChildNode4Group(treeNode);
        } else {
            if (subscribVidArray.length !== 0) {
                for (var i = 0; i < subscribVidArray.length; i++) {
                    var list = zTreeIdJson[subscribVidArray[i]];
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
                param.push(treeNode.children[i].id)
            }
            this.subscribeStatus(param);
        }
    }
}

Tree.prototype.getChildNode4Assignment= function (treeNode) {
    var treeObj = this.treeObj;
    var subscribVidArray = this.dependency.get('data').getSubscribVidArray();
    var zTreeIdJson = this.zTreeIdJson;

    var url = "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign";
    json_ajax("post", url, "json", false, {
        "assignmentId": treeNode.id, "isChecked": treeNode.checked, "monitorType": "monitor", "webType": 1
    }, function (data) {
        // var nodes = [];
        // for (var i = 0; i < 500; i++){
        //     nodes.push({
        //         aliases: "",
        //         assignName: "货运平台分组1",
        //         deviceNumber: "2415572",
        //         deviceType: "13",
        //         iconSkin: "vehicleSkin",
        //         id: "79d85f3b-f5ee-483a-8363-3b6197a0ef1a",
        //         isVideo: 1,
        //         name: "桂AFY292",
        //         pId: "86677b49-fc33-4d3f-bf60-19909c4f4d84",
        //         plateColor: 2,
        //         professional: "",
        //         simcardNumber: "13302415572",
        //         type: "vehicle"
        //     })
        // }
        var nodes = JSON.parse(ungzip(data.msg));
        var addV = treeObj.addNodes(treeNode, nodes);
        if (addV !== null) {
            var param = [];
            for (var i = 0; i < treeNode.children.length; i++) {
                param.push(treeNode.children[i].id)
            }

            this.subscribeStatus(param);
            if (subscribVidArray.length !== 0) {
                for (var i = 0; i < subscribVidArray.length; i++) {
                    var list = zTreeIdJson[subscribVidArray[i]];
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
    }.bind(this))
}

Tree.prototype.getChildNode4Group = function(treeNode){
    var treeObj = this.treeObj;
    var zTreeIdJson = this.zTreeIdJson;

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
}

Tree.prototype.subscribeStatus = function(vidArray){
    if (webSocket.conFlag) {
        var param = vidArray.map(function(x){
            return {vehicleID:x};
        })
        var requestStrS = {
            "desc": {
                "MsgId": 40964,
                "UserName": $("#userName").text()
            },
            "data": param
        };
        webSocket.subscribe(headers, '/user/topic/cachestatus', this.updateTreeColor.bind(this), "/app/vehicle/subscribeCacheStatusNew", requestStrS);
    } else {
        setTimeout(function () {
            this.subscribeStatus(vidArray);
        }.bind(this), 1000);
    }

}

Tree.prototype.subscribeLocation = function(vidArray){
    if (webSocket.conFlag) {
        var requestStrS = {
            "desc": {
                "MsgId": 40964,
                "UserName": $("#userName").text()
            },
            "data": vidArray
        };
        webSocket.subscribe(headers, '/user/topic/location', this.updateLocation.bind(this), "/app/location/subscribe", requestStrS);
    } else {
        setTimeout(function () {
            this.subscribeLocation(vidArray);
        }.bind(this), 1000);
    }

}

Tree.prototype.cancelSubscribeLocation = function(){
    var dataDependency = this.dependency.get('data');

    var latestUpdateVid = dataDependency.getLatestUpdateVid();
    var activeWindowIndex = dataDependency.getActiveWindowIndex();
    var vidInfo = dataDependency.getSubscribVidInfo(latestUpdateVid);

    var treeObj = this.treeObj;
    var zTreeIdJson = this.zTreeIdJson;

    this.checkSameVid(latestUpdateVid,false);

    var requestStrS = {
        "desc": {
            "MsgId": 40964,
            "UserName": $("#userName").text()
        },
        "data": [latestUpdateVid]
    };
    webSocket.unsubscribealarm(headers, "/app/location/unsubscribe", requestStrS);
}

Tree.prototype.updateTreeNodeColor = function(status,vid){
    var treeObj = this.treeObj;
    var zTreeIdJson = this.zTreeIdJson;

    var iconClass,textColor;
    switch (status){
        case 'heartBeat': // 心跳
            iconClass = 'btnImage iconArea heartBeatWarning';
            textColor = '#fb8c96';
            break;
        case 'notPosition': // 未定位
            iconClass = 'btnImage iconArea onlineNotPositioning';
            textColor = '#754801';
            break;
        case 'alarm': // 报警
            iconClass = 'btnImage iconArea warning';
            textColor = '#ffab2d';
            break;
        case 'run': // 行驶
            iconClass = 'btnImage iconArea onlineDriving';
            textColor = '#78af3a';
            break;
        case 'stop': // 停止
            iconClass = 'btnImage iconArea onlineParking';
            textColor = '#c80002';
            break;
        case 'overSpeed': // 超速
            iconClass = 'btnImage iconArea speedLimitWarning';
            textColor = '#960ba3';
            break;
        case 'offline': // 离线
            iconClass = 'btnImage iconArea offlineIcon';
            textColor = '#b6b6b6';
            break;
    }
    if (iconClass !== undefined){
        var list = zTreeIdJson[vid];
        if (list != null) {
            $.each(list, function (index, value) {
                var treeNode = treeObj.getNodeByTId(value);
                treeNode.iconSkin = iconClass;
                treeObj.updateNodeIconSkin(treeNode);
                $("#" + value + "_span")[0].style.color = textColor;
            })
        }
    }
}

Tree.prototype.updateTreeColor = function (msg) {
    // 39321 订阅立刻返回状态
    // 34952 上线，之前没上线的上线了
    // 30583 状态变化(ps:心跳-行驶)

    // 4:停止；5：报警，3：离线；2：未定位；9：超速；10：行驶；11：心跳

    var dataDependency = this.dependency.get('data');
    var runVidArray = dataDependency.getRunVidArray();
    var stopVidArray = dataDependency.getStopVidArray();
    var allVidArray = dataDependency.getAllVidArray();

    var data = $.parseJSON(msg.body);
    var msgID = data.desc.msgID;
    var positions = data.data;
    // 和实时监控针对不同消息ID做不同的处理不同，我们简单一些，不管你什么消息ID，我只针对当前消息返回回来的监控对象状态更新树节点颜色
    // 而总数也是可以通过最开始的接口获取的ID数组判断，这些数据存放在 data._runVidArray,data._stopVidArray,data._allVidArray

    for (var i = 0; i < positions.length; i++){
        var status = Util.statusNumber2Text(positions[i].vehicleStatus);
        var vid = positions[i].vehicleId;

        this.updateTreeNodeColor(status,vid);
        dataDependency.setStatusVidObj(vid,status);

        var runIndex = runVidArray.indexOf(vid);
        var stopIndex = stopVidArray.indexOf(vid);

        if (status === 'offline'){
            if (runIndex > -1){
                runVidArray.splice(runIndex,1);
            }
            if (stopIndex > -1){
                stopVidArray.splice(stopIndex,1);
            }
        } else if(runIndex === -1 && stopIndex === -1){
            if (runIndex === -1){
                runVidArray.push(vid);
            }else if (stopIndex === -1){
                stopVidArray.push(vid);
            }
        }
    }

    var newOnlineCount = runVidArray.length + stopVidArray.length;
    var newOfflineCount = allVidArray.length - newOnlineCount;

    dataDependency.setOnlineCount(newOnlineCount);
    dataDependency.setOfflineCount(newOfflineCount);
    dataDependency.setRunVidArray(runVidArray);
    dataDependency.setStopVidArray(stopVidArray);

    var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
    var treeType = dataDependency.getTreeType();
    if (treeType === 'offline') {//不在线组织树隐藏在线车辆
        var onlineList = runVidArray.concat(stopVidArray);
        for (var i = 0; i < onlineList.length; i++) {
            var treeNode = treeObj.getNodesByParam('id', onlineList[i]);
            treeObj.hideNodes(treeNode);
        }
    }
}

Tree.prototype.zTreeOnNodeCreated=function (event, treeId, treeNode) {
    var zTreeIdJson = this.zTreeIdJson;
    var id = treeNode.id.toString();
    var list = [];
    if (zTreeIdJson[id] == null) {
        list = [treeNode.tId];
        zTreeIdJson[id] = list;
    } else {
        zTreeIdJson[id].push(treeNode.tId)
    }
}

Tree.prototype.onClick = function(e, treeId, treeNode){
    if (treeNode.type == "assignment" || treeNode.type == "group") {
        return false;
    }
    var dataDependency = this.dependency.get('data');
    var smallWindowDependency = this.dependency.get('smallWindow');

    var vid = treeNode.id;
    smallWindowDependency.relocate(vid);
    dataDependency.setActiveWindowIndex(null);
}

Tree.prototype.treeBeforeClick = function(e, treeNode){
    if (treeNode.type == "assignment" || treeNode.type == "group") {
       return false;
    }
    return true;
}

Tree.prototype.onDbClick = function(e, treeId, treeNode){
    var dataDependency = this.dependency.get('data');

    var checked = treeNode.checked;
    if (!checked){
        this.treeObj.checkNode(treeNode,true,false,true);
        this.treeObj.cancelSelectedNode(treeNode);
    }else{
        var dbclickCheckedId = e.target.id;
        if (treeNode.type == "assignment" || treeNode.type == "group") {
            return;
        }
        $("#" + dbclickCheckedId).parent().attr("class", "curSelectedNode_dbClick");
        var subscribObjArray = dataDependency.getSubscribObjArray();
        var windowCount = dataDependency.getWindowCount();

        var vid = treeNode.id;
        var vidInfo = subscribObjArray.find(function (x) {
            return x.vid === vid;
        });
        var windowIndex = vidInfo.windowIndex;
        if (windowIndex >= windowCount){
            dataDependency.setWindowCount(windowIndex + 1);
        }
        dataDependency.setActiveWindowIndex(windowIndex);
    }

}

Tree.prototype.updateLocation = function (msg) {
    var dataDependency = this.dependency.get('data');
    var data = $.parseJSON(msg.body);

    var vid;
    var status = 'offline';
    var duration = '--';
    var locateType = '';
    var locateTime = '--';
    var location = '--';
    var longitude = null;
    var latitude = null;
    var icon = null;
    var angle = null;
    var type = null;
    // var todayMileageArray = null;
    var gpsTimeArray = null;
    var positionArray = null;
    var speed = 50;
    var sensorInfo = {};

    var vidInfo;

    if (data.desc === "neverOnline") {
        vid = data.vid;
        vidInfo = dataDependency.getSubscribVidInfo(vid);
        locateTime = '未上线';
    }else{
        var msgHead = data.data.msgHead;
        var msgBody = data.data.msgBody;
        var monitorInfo = msgBody.monitorInfo;
        var now = new Date();
        console.log(msgBody);

        vid = monitorInfo.monitorId;
        vidInfo = dataDependency.getSubscribVidInfo(vid);
        status = Util.nvl(dataDependency.getStatusVidObj()[vid], status);
        locateTime = Util.formateGpsTime(msgBody.gpsTime);
        // 如果状态为离线，那么状态持续时间为当前时间 - 最后一条位置信息定位时间
        if (status === 'offline'){
            var diff = now.getTime() - (new Date(locateTime.replace(/-/g,'/'))).getTime();
            duration = "离线: " + Util.formatDuring(diff);
        }else{
            if (msgBody.durationTime !== undefined && msgBody.durationTime !== null) {
                duration = Number(msgBody.gpsSpeed) <= 1 ?
                    "停止: " + Util.formatDuring(msgBody.durationTime)
                    : "行驶: " + Util.formatDuring(msgBody.durationTime)
            } else {
                duration = Number(msgBody.gpsSpeed) <= 1 ? "停止" : "行驶"
            }
        }
        locateType = Util.formateLocateType(msgBody.locationPattern);
        location = Util.formateLocation(msgBody.positionDescription);
        longitude = msgBody.longitude;
        latitude = msgBody.latitude;
        icon = monitorInfo.monitorIcon;
        angle = Number(msgBody.direction) + 270; //
        // 监控对象类型： 0 车， 1 人， 2 物
        type = (monitorInfo.monitorType == null || monitorInfo.monitorType == 'null' || monitorInfo.monitorType == undefined)
            ? 0 : monitorInfo.monitorType;

        // todayMileageArray = Util.shadowCopyArray(Util.findByPath(vidInfo, ['basicInfo','todayMileageArray'], []));
        gpsTimeArray = Util.shadowCopyArray(Util.findByPath(vidInfo, ['basicInfo','gpsTimeArray'], []));
        positionArray = Util.shadowCopyArray(Util.findByPath(vidInfo, ['basicInfo','positionArray'], []));

        // todayMileageArray.push(msgBody.dayMileage);
        gpsTimeArray.push(new Date(locateTime.replace(/-/g,'/')));
        positionArray.push([longitude,latitude]);

        if (positionArray.length > 1){
            var distance = Util.calcDistance(Util.arrayLast(positionArray), Util.arrayLast(positionArray, 2));
            speed = Util.markerMoveSpeed(distance,gpsTimeArray);
        }

        sensorInfo = Util.findByPath(vidInfo, ['sensorInfo'], {});
        sensorInfo.elec = Util.findByPath(msgBody, ['elecData','deviceElectricity'], null);
        sensorInfo.satellitesNumber = Util.findByPath(msgBody, ['satellitesNumber'], null);
        sensorInfo.wifi = Util.findByPath(msgBody, ['wifiSignalStrength'], null);
        sensorInfo.signalStrength = Util.findByPath(msgBody, ['signalStrength'], null);
        sensorInfo.signalType = Util.findByPath(msgBody, ['signalType'], null);
        sensorInfo.todayMileage = msgBody.dayMileage;
        sensorInfo.currentSpeed = msgBody.gpsSpeed;
        sensorInfo.speedValueSource = msgBody.speedValueSource;
        sensorInfo.acc = msgBody.acc;
        sensorInfo.oilMass = Util.findByPath(msgBody,['oilMass'],null);
        sensorInfo.oilExpend = Util.findByPath(msgBody,['oilExpend'],null);
        sensorInfo.temp = Util.findByPath(msgBody,['temperatureSensor'],null);
        sensorInfo.humi = Util.findByPath(msgBody,['temphumiditySensor'],null);
        sensorInfo.workhour = Util.findByPath(msgBody,['workHourSensor'],null);
        sensorInfo.reverse = Util.findByPath(msgBody,['positiveNegative'],null);
        sensorInfo.weight = Util.findByPath(msgBody,['loadInfos'],null);
        sensorInfo.tire = Util.findByPath(msgBody,['tyreInfos','list'],null);
    }


    vidInfo.basicInfo = {
        vid:vid,
        status:status,
        duration:duration,
        locateType:locateType,
        locateTime:locateTime,
        location:location,
        longitude:longitude,
        latitude:latitude,
        icon:icon,
        angle:angle,
        type:type,
        speed:speed,
        // todayMileageArray:todayMileageArray,
        gpsTimeArray:gpsTimeArray,
        positionArray:positionArray,
    };
    vidInfo.sensorInfo = sensorInfo;
    vidInfo.shouldMove = true;
    dataDependency.updateSubscribVid(vid,vidInfo);
}

Tree.prototype.treeBeforeCheck = function(treeId, treeNode){
    var checked = !treeNode.checked;
    if (checked){
        var dataDependency = this.dependency.get('data');
        var subscribVidArray = dataDependency.getSubscribVidArray();
        var windowCount = dataDependency.getWindowCount();
        var activeWindowIndex = dataDependency.getActiveWindowIndex();
        if (subscribVidArray.length  === 10 && activeWindowIndex === null){
            layer.alert("同时最多可订阅10个监控对象");
            return false;
        }
    }
    return true;
}

Tree.prototype.treeOnCheck = function (e, treeId, treeNode) {
    var dataDependency = this.dependency.get('data');

    var activeWindowIndex = dataDependency.getActiveWindowIndex();
    var subscribObjArray = dataDependency.getSubscribObjArray();
    var windowCount = dataDependency.getWindowCount();

    var vid = treeNode.id;
    var checked = treeNode.checked;
    if (checked){
        // 确定窗口索引
        var windowIndex;

        if (activeWindowIndex !== null){
            windowIndex = activeWindowIndex;
            // cancel previous subscribed vehicle
            var preiousVidInfo = subscribObjArray.find(function (x) {
                return x.windowIndex === activeWindowIndex;
            });
            if (preiousVidInfo){
                dataDependency.removeSubscribVid(preiousVidInfo.vid);
            }
        } else{
            var windowIndexArray = subscribObjArray.map(function(x){
                return x.windowIndex;
            });
            var maxWindowIndex = windowIndexArray.length > 0 ?  Math.max.apply(null, windowIndexArray) : -1;
            var max = Math.max(maxWindowIndex, windowCount - 1);
            for (var i = 0; i <= max; i++){
                if (windowIndexArray.indexOf(i) === -1){
                    windowIndex = i;
                    break;
                }
            }
            var newWindowCount;
            if (windowIndex === undefined){
                // 尽可能的完全显示所有订阅监控对象
                if (windowCount < maxWindowIndex + 2){
                    newWindowCount = maxWindowIndex + 2;
                }
                windowIndex = maxWindowIndex + 1;
            }else {
                newWindowCount = maxWindowIndex + 1;
            }
            if (newWindowCount > windowCount){
                dataDependency.setWindowCount(newWindowCount);
            }
        }
        dataDependency.addSubscribVid(vid,{
            vid:vid,
            windowIndex:windowIndex,
            brandName:treeNode.name,
            openFollowPath:true,
            keepCenter:true,
            positionIndex:0
        });
        this.subscribeLocation([vid]);
    } else{
        var vidInfo = dataDependency.getSubscribVidInfo(vid);
        if (activeWindowIndex === vidInfo.windowIndex){
            dataDependency.setActiveWindowIndex(null);
        }
        dataDependency.removeSubscribVid(vid);
    }
    this.checkSameVid(vid, checked);
    if (checked && activeWindowIndex !== null){
        dataDependency.setActiveWindowIndex(activeWindowIndex);
    }
}

Tree.prototype.updateStatus = function (vid,status) {
    var dataDependency = this.dependency.get('data');

    vidInfo = dataDependency.getSubscribVidInfo(vid);
    if (!vidInfo){
        // 说明没有订阅位置信息
        return;
    }
    vidInfo.basicInfo.status = status;
    dataDependency.updateSubscribVid(vid,vidInfo);
}

/*Tree.prototype.onScroll = function () {
    var zTree = this.treeObj;
    var $theTree = $("#thetree");
    var p = $theTree.scrollTop();
    // console.log('p', p);
    if (this.lastScrollTop <= p) {//下滚
        // 获取没有展开的分组节点
        var notExpandNodes = zTree.getNodesByFilter(assignmentNotExpandFilter);
        if (notExpandNodes != undefined && notExpandNodes.length > 0) {
            for (var i = 0; i < notExpandNodes.length; i++) {
                var node = notExpandNodes[i];
                var tid = node.tId + "_a";
                var divHeight = $theTree.offset().top;
                var nodeHeight = $("#" + tid).offset().top;
                if (nodeHeight - divHeight > 696) {
                    break;
                }
                if (nodeHeight - divHeight > 0 && nodeHeight - divHeight < 696) {
                    zTree.expandNode(node, true, true, false, true);
                    node.children[0].open = true;
                }
            }
        }
    }
    setTimeout(function () {
        this.lastScrollTopt = p;
    }.bind(this), 0);
}*/

Tree.prototype.checkSameVid = function (vid, checked) {
    var treeObj = this.treeObj;
    var zTreeIdJson = this.zTreeIdJson;

    var list = zTreeIdJson[vid];
    if (list != null) {
        for (var i = 0; i < list.length; i++){
            var value = list[i];
            var znode = treeObj.getNodeByTId(value);
            treeObj.checkNode(znode, checked, false,false);
            znode.checkedOld = checked;
            treeObj.updateNode(znode);
        }

    }
}

Tree.prototype.yellowHighlightSameVid = function (vid) {
    var treeObj = this.treeObj;
    var zTreeIdJson = this.zTreeIdJson;

    var list = zTreeIdJson[vid];
    if (list != null) {
        for (var i = 0; i < list.length; i++){
            var value = list[i];
            $("#" + value).children("a").addClass("curSelectedNode_dbClick");
        }

    }
}

Tree.prototype.activeWindowIndexChange = function() {
    var dataDependency = this.dependency.get('data');
    var activeWindowIndex = dataDependency.getActiveWindowIndex();

    var treeObj = this.treeObj;

    // 取消双击高亮的和单击高亮的背景颜色
    $(".ztree li a").removeClass("curSelectedNode_dbClick");

    if (activeWindowIndex !== null) {
        var selectedNodes = treeObj.getSelectedNodes();
        if (selectedNodes && selectedNodes.length > 0){
            for (var i = 0; i < selectedNodes.length; i ++){
                treeObj.cancelSelectedNode(selectedNodes[i]);
            }
        }

        var subscribObjArray = dataDependency.getSubscribObjArray();
        var vidInfo = subscribObjArray.find(function (x) {
            return x.windowIndex === activeWindowIndex;
        });
        if (!vidInfo){
            return;
        }
        var vid = vidInfo.vid;
        this.yellowHighlightSameVid(vid);
    }
}