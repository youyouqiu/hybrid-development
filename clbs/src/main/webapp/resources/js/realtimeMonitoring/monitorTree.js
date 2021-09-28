var MonitorTree = function (selector, options, dependency) {
    this.dependency = dependency;
    this.selector = selector;
    this.zTreeIdJson = {};
    this.crrentSubV = [];
    this.vehicleSize = 0;
    this.inputChange = null;
    this.first = true;
    var self = this;
    $('input').inputClear().on('onClearEvent', function (e, data) {
        var id = data.id;
        if (id == 'citySel') {
            self.init();
            /*var zTree = $.fn.zTree.getZTreeObj('treeDemo');
            zTree.expandAll();*/
            $('#treeEmpty').hide();
        }
    });
    $("#citySel").on('input propertychange', function (value) {
        if (self.inputChange !== null) {
            clearTimeout(self.inputChange);
            self.inputChange = null;
        }

        self.inputChange = setTimeout(function () {
            var param = $("#citySel").val();
            if(param == ''){//解决回退按键input为空后，分组数据错误的bug
                self.init();
                $('#treeEmpty').hide();
                return;
            }

            self.search(param);
            $('#treeEmpty').hide();
            $('#treeLoad').show();
        }, 500);
    });
    //IE9
    if (navigator.appName == "Microsoft Internet Explorer" && navigator.appVersion.split(";")[1].replace(/[ ]/g, "") == "MSIE9.0") {
        var search;
        $("#citySel").bind("focus", function () {
            search = setInterval(function () {
                var param = $("#citySel").val();
                if(param == ''){
                    self.init();
                    return;
                }

                self.search(param);
                $('#treeLoad').show();
            }, 500);
        }).bind("blur", function () {
            clearInterval(search);
        });
    }
    //IE9 end
}

MonitorTree.prototype.init = function () {
    this.setting = {
        async: {
            url: MonitorTree.prototype.getTreeUrl.bind(this),
            type: "post",
            enable: true,
            autoParam: ["id"],
            dataType: "json",
            otherParam: {"type": "single"},
            dataFilter: MonitorTree.prototype.ajaxDataFilter.bind(this)
        },
        check: {
            enable: true,
            chkStyle: "radio",
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
            beforeCheck: MonitorTree.prototype.zTreeBeforeCheck.bind(this),
            onCheck: MonitorTree.prototype.onCheck.bind(this),
            beforeClick: MonitorTree.prototype.zTreeBeforeClick.bind(this),
            onAsyncSuccess: MonitorTree.prototype.zTreeOnAsyncSuccess.bind(this),
            onClick: MonitorTree.prototype.zTreeOnClick.bind(this),
            onExpand: MonitorTree.prototype.zTreeOnExpand.bind(this),
            beforeAsync: MonitorTree.prototype.zTreeBeforeAsync.bind(this),
            onNodeCreated: MonitorTree.prototype.zTreeOnNodeCreated.bind(this),
        }
    };

    this.treeObj = $.fn.zTree.init($(this.selector), this.setting, null);
}

MonitorTree.prototype.search = function (param, paramType) {
    if (param == '') {
        clearTimeout(this.inputChange);
        this.inputChange = null;
    }
    this.setting = {
        async: {
            url: "/clbs/m/functionconfig/fence/bindfence/monitorTreeFuzzy",
            type: "post",
            enable: true,
            autoParam: ["id"],
            dataType: "json",
            otherParam: {
                "type": "single",
                "queryParam": param,
                "queryType": (paramType === null || undefined ? 'name' : paramType)
            },
            dataFilter: MonitorTree.prototype.ajaxQueryDataFilterSearch.bind(this)
        },
        check: {
            enable: true,
            chkStyle: "radio"
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
            beforeCheck: MonitorTree.prototype.zTreeBeforeCheck.bind(this),
            onCheck: MonitorTree.prototype.onCheck.bind(this),
            beforeClick: MonitorTree.prototype.zTreeBeforeClick.bind(this),
            onAsyncSuccess: MonitorTree.prototype.zTreeOnAsyncSuccess.bind(this),
            onClick: MonitorTree.prototype.zTreeOnClick.bind(this),
            onExpand: MonitorTree.prototype.zTreeOnExpand.bind(this),
            beforeAsync: MonitorTree.prototype.zTreeBeforeAsync.bind(this),
            onNodeCreated: MonitorTree.prototype.zTreeOnNodeCreated.bind(this),
        }
    };

    this.treeObj = $.fn.zTree.init($(this.selector), this.setting, null);
}

MonitorTree.prototype.zTreeOnExpand = function (event, treeId, treeNode) {
    var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
    if (treeNode.type == "assignment" && treeNode.children === undefined) {
        var url = "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign";
        json_ajax("post", url, "json", false, {
            "assignmentId": treeNode.id,
            "isChecked": treeNode.checked,
            "monitorType": "monitor"
        }, function (data) {
            var result = JSON.parse(ungzip(data.msg));
            if (result != null && result.length > 0) {
                treeObj.addNodes(treeNode, result);
                this.checkCurrentNodes(treeId);
            }
        }.bind(this))
    }
}

MonitorTree.prototype.checkCurrentNodes = function (treeId) {
    var treeObj = $.fn.zTree.getZTreeObj(treeId);
    var arr = this.crrentSubV;

    if (arr != null && arr != undefined && arr.length !== 0) {
        for (var i = 0; i < arr.length; i++) {
            var list = zTreeIdJson[arr[i]];
            if (list != null && list.length > 0) {
                for (var j = 0; j < list.length; j++) {
                    var value = list[j];
                    var znode = treeObj.getNodeByTId(value);
                    if (znode != null) {
                        treeObj.checkNode(znode, true, true);
                    }
                }
            }
        }
    }
}

MonitorTree.prototype.zTreeBeforeAsync = function () {
    return this.dependency.get('flags').bflag;
}

MonitorTree.prototype.zTreeOnNodeCreated = function (event, treeId, treeNode) {
    var zTree = $.fn.zTree.getZTreeObj("treeDemo");
    var id = treeNode.id.toString();
    var list = [];
    if (this.zTreeIdJson[id] == undefined || this.zTreeIdJson[id] == null) {
        list = [treeNode.tId];
        this.zTreeIdJson[id] = list;
    } else {
        this.zTreeIdJson[id].push(treeNode.tId)
    }
}

//对象树加载成功
MonitorTree.prototype.zTreeOnAsyncSuccess = function (event, treeId, treeNode, msg) {
    $('#treeLoad').hide();
    var zTree = $.fn.zTree.getZTreeObj(treeId);
    var vUuid = this.dependency.get('data').getInitVid();
    if (vUuid != null) {
        var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
        treeObj.expandAll(true); // 展开节点
        if (vUuid !== undefined && vUuid !== null && vUuid != "") {
            var node = treeObj.getNodesByParam("id", vUuid, null);
            if (node != null && node != undefined && node.length > 0) {
                for (var i = 0, len = node.length; i < len; i++) {
                    treeObj.checkNode(node[i], true, true);
                    if (this.crrentSubV.length == 0) { // 存入勾选数组
                        this.crrentSubV.push(node[i].id);
                    }
                }
                this.dependency.get('data').setInitVid(null);
                this.dependency.get('data').setActiveTreeNode(node[0]);
            }
        }
    }
    // 更新节点数量
    zTree.updateNodeCount(treeNode);
    // 默认展开200个节点
    if (this.first) {
        this.first = false;
        var initLen = 0;
        var notExpandNodeInit = zTree.getNodesByFilter(assignmentNotExpandFilter);
        for (i = 0; i < notExpandNodeInit.length; i++) {
            zTree.expandNode(notExpandNodeInit[i], true, true, false, true);
            initLen += notExpandNodeInit[i].children.length;
            if (initLen >= 200) {
                break;
            }
        }
    }

    if (this.inputChange != null || this.vehicleSize != 0) {
        var iptVal = $("#citySel").val();
        if(iptVal !== ""){
            zTree.expandAll(true);
        }
        clearTimeout(this.inputChange);
        this.inputChange = null;
    }
}

MonitorTree.prototype.zTreeBeforeCheck = function (treeId, treeNode) {
    var dataDependency = this.dependency.get('data');
    var oldId = dataDependency.getActiveTreeNode() ? dataDependency.getActiveTreeNode().id : null;
    var id = treeNode.id;
    if(oldId === id){return false;}

    var flag = true;
    if (!treeNode.checked) {
        if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
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

            //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
            var ns = [];
            //节点id
            var nodeId;
            for (var i = 0; i < nodes.length; i++) {
                nodeId = nodes[i].id;
                if (nodes[i].type == "people" || nodes[i].type == "vehicle" || nodes[i].type == "thing") {
                    //查询该节点是否在勾选组织或分组下，若在则不记录，不在则记录
                    var nd = zTree.getNodeByParam("tId", nodes[i].tId, treeNode);
                    if (nd == null && $.inArray(nodeId, ns) == -1) {
                        ns.push(nodeId);
                    }
                }
            }
            nodesLength += ns.length;
        } else if (treeNode.type == "people" || treeNode.type == "vehicle" || treeNode.type == "thing") { //若勾选的为监控对象
            var zTree = $.fn.zTree.getZTreeObj(treeId), nodes = zTree
                .getCheckedNodes(true), v = "";
            var nodesLength = 0;
            //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
            var ns = [];
            //节点id
            var nodeId;
            for (var i = 0; i < nodes.length; i++) {
                zTree.checkNode(nodes[i], false, true);
                nodeId = nodes[i].id;
                if (nodes[i].type == "people" || nodes[i].type == "vehicle" || nodes[i].type == "thing") {
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
}

MonitorTree.prototype.getTreeUrl = function (treeId, treeNode) {
    if (treeNode == null) {
        return "/clbs/m/functionconfig/fence/bindfence/getTreeByMonitorCount";
    } else if (treeNode.type == "assignment") {
        return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=monitor";
    }
}

MonitorTree.prototype.ajaxQueryDataFilterSearch = function (treeId, parentNode, responseData) {
    responseData = JSON.parse(ungzip(responseData));
    var filterData = filterQueryResult(responseData, this.crrentSubV);
    var treeEmpty = $('#treeEmpty');
    if(filterData.length == 0){
        treeEmpty.show();
    }else{
        treeEmpty.hide();
    }
    return filterData;
}

MonitorTree.prototype.ajaxDataFilter = function (treeId, parentNode, responseData) {
    responseData = JSON.parse(ungzip(responseData.msg));
    // console.log('组织树数据', responseData);

    if (responseData) {
        for (var i = 0; i < responseData.length; i++) {
            if (responseData[i].iconSkin != "assignmentSkin") {
                responseData[i].open = true;
            }

            if(responseData[i].type === "vehicle"){
                this.vehicleSize += 1;
            }
            this.dependency.get('data').setTreeData(responseData);
        }
    }
    return responseData;
}

MonitorTree.prototype.zTreeBeforeClick = function (treeId, treeNode, clickFlag) {
    if (treeNode.type === 'group' || treeNode.type === 'assignment') {
        return false;
    }
    return true;
}
//对象树勾选
MonitorTree.prototype.onCheck = function (e, treeId, treeNode) {
    this.dependency.get('data').setActiveTreeNode(treeNode);
    var type = treeNode.deviceType;
    // this.dependency.get('flags').worldType = type;
    // this.dependency.get('flags').objType = treeNode.type;
    // this.dependency.get('calendar').nowMonth = nowDate.getFullYear() + "-" + (parseInt(nowDate.getMonth() + 1) < 10 ? "0" + parseInt(nowDate.getMonth() + 1) : parseInt(nowDate.getMonth() + 1)) + "-01";
    // this.dependency.get('calendar').afterMonth = nowDate.getFullYear() + "-" + (parseInt(nowDate.getMonth() + 2) < 10 ? "0" + parseInt(nowDate.getMonth() + 2) : parseInt(nowDate.getMonth() + 2)) + "-01";
    var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
        nodes = zTree.getCheckedNodes(true),
        v = "";
    // var carPid = nodes[0].id;
    zTree.selectNode(treeNode, false, true);
    // $("#savePid").attr("value", carPid);
    // v = nodes[0].name;
    // var cityObj = $("#citySel");
    // cityObj.val(v);
    // $("#menuContent").hide();
    // this.dependency.get('calendar').getActiveDate(carPid, this.dependency.get('calendar').nowMonth, this.dependency.get('calendar').afterMonth);
    // trackPlayback.showHidePeopleOrVehicle();
    //单击时判断节点是否勾选订阅
    var fenceTree = this.dependency.get('fenceTree');
    fenceTree.vehicleTreeClickGetFenceInfo.bind(fenceTree)(treeNode.checked, treeNode.id);
    // 勾选的车辆
    // this.crrentSubV = [];
    // this.crrentSubV.push(treeNode.id);
}
MonitorTree.prototype.getCharSelect = function (treeObj) {
    var nodes = treeObj.getCheckedNodes(true);
    var allNodes = treeObj.getNodes();
    if (nodes.length > 0) {
        $("#groupSelect").val(allNodes[0].name);
    } else {
        $("#groupSelect").val("");
    }
}
//获取到选择的节点
MonitorTree.prototype.getCheckedNodes = function () {
    var zTree = $.fn.zTree.getZTreeObj("treeDemoMenu"), nodes = zTree.getCheckedNodes(true), v = "", vid = "";
    for (var i = 0, l = nodes.length; i < l; i++) {
        if (nodes[i].type == "vehicle" || nodes[i].type == "people" || nodes[i].type == "thing") {
            v += nodes[i].name + ",";
            vid += nodes[i].id + ",";
        }
    }
    allVid = vid;
    vnameList = v;
}
//对象树点击
MonitorTree.prototype.zTreeOnClick = function (event, treeId, treeNode) {
    var dataDependency = this.dependency.get('data');
    var nowDate = new Date();
    this.dependency.get('calendar').nowMonth = nowDate.getFullYear() + "-" + (parseInt(nowDate.getMonth() + 1) < 10 ? "0" + parseInt(nowDate.getMonth() + 1) : parseInt(nowDate.getMonth() + 1)) + "-01";
    this.dependency.get('calendar').afterMonth = nowDate.getFullYear() + "-" + (parseInt(nowDate.getMonth() + 2) < 10 ? "0" + parseInt(nowDate.getMonth() + 2) : parseInt(nowDate.getMonth() + 2)) + "-01";

    var oldId = dataDependency.getActiveTreeNode() ? dataDependency.getActiveTreeNode().id : null;
    var id = treeNode.id;
    if(oldId === id){return;}
    // $("#savePid").attr("value", id);
    var name = treeNode.name;
    // if (treeNode.type != 'assignment' && treeNode.type != 'group') {
    //     $("#citySel").val(name);
    // } else {
    //     $("#citySel").val('');
    // }
    dataDependency.setActiveTreeNode(treeNode);
    var type = treeNode.deviceType;
    // this.dependency.get('flags').worldType = type;
    // this.dependency.get('flags').objType = treeNode.type;
    var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
    var nodes = treeObj.getCheckedNodes(true);
    for (var i = 0, l = nodes.length; i < l; i++) {
        treeObj.checkNode(nodes[i], false, true);
    }
    treeObj.selectNode(treeNode, false, true);
    treeObj.checkNode(treeNode, true, true);
    this.dependency.get('map').map.clearMap();
    var fenceTree = this.dependency.get('fenceTree');
    fenceTree.vehicleTreeClickGetFenceInfo.bind(fenceTree)(treeNode.checked, treeNode.id);
}