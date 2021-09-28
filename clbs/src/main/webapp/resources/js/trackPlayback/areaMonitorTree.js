var AreaMonitorTree = function (selector, options, dependency) {
    this.dependency = dependency;
    this.monitorSize = 0;
    this.monitorMaxCheckSize = TREE_MAX_CHILDREN_LENGTH;
    this.zTreeIdJson = {};
    this.menuCrrentSubV = [];
    this.areaVehicleNameList = '';
    this.searchTreeTime = null;// 组织树模糊搜索定时器
    this.searchFlag = true;// 组织树模糊搜索标识,用以解决IE浏览器初次勾选监控对象失效问题
    this.inputFlag = false;// 组织树输入框是否输入过标识(用于解决IE浏览器组织树重复加载,导致的闪烁问题)
    this.selector = $(selector);

    this.treeInit();
    this.eventBind();
};

AreaMonitorTree.prototype.eventBind = function () {
    var _this = this;
    $('input').inputClear().on('onClearEvent', function (e, data) {
        var id = data.id;
        if (id == 'groupSelect') {
            _this.dependency.get('flags').changeFlag = false;
            _this.dependency.get('data').setAreaTreeAllCheck(false);
            this.inputFlag = true;
            _this.treeInit();
        }
    });
    $("#groupSelect").on('input propertychange', function (value) {
        if (_this.searchTreeTime) {
            clearTimeout(_this.searchTreeTime);
        }
        _this.searchTreeTime = setTimeout(function () {
            if (_this.searchFlag) {
                var param = $("#groupSelect").val();
                _this.searchAreaTreeVehicle(param);
            }
            _this.searchFlag = true;
        }, 500);
    });
    $('#queryType').on('change', function () {
        if ($('#groupSelect').val() != '') {
            $('#groupSelect').val('');
            _this.dependency.get('flags').changeFlag = true;
            _this.dependency.get('data').setAreaTreeAllCheck(false);
            _this.treeInit();
        }
    });
};

AreaMonitorTree.prototype.treeInit = function () {
    this.setting = {
        async: {
            url: AreaMonitorTree.prototype.getTreeUrl.bind(this),
            type: "post",
            enable: true,
            autoParam: ["id"],
            dataType: "json",
            otherParam: {"type": "single"},
            dataFilter: AreaMonitorTree.prototype.ajaxDataFilter.bind(this)
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
            beforeCheck: AreaMonitorTree.prototype.zTreeBeforeCheck.bind(this),
            beforeClick: AreaMonitorTree.prototype.zTreeBeforeClick.bind(this),
            onCheck: AreaMonitorTree.prototype.onCheckVehicle.bind(this),
            onAsyncSuccess: AreaMonitorTree.prototype.zTreeOnAsyncSuccess.bind(this),
            onExpand: AreaMonitorTree.prototype.zTreeOnExpand.bind(this),
            onNodeCreated: AreaMonitorTree.prototype.zTreeOnNodeCreated.bind(this),
        }
    };
    this.treeObj = $.fn.zTree.init(this.selector, this.setting, null);
};
// 获取分组下的子节点
AreaMonitorTree.prototype.getChildNode = function (treeNode, treeId) {
    var treeObj = $.fn.zTree.getZTreeObj(treeId);
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
};
//树优化测试代码块todo
AreaMonitorTree.prototype.zTreeOnExpand = function (event, treeId, treeNode) {
    var treeObj = $.fn.zTree.getZTreeObj(treeId);
    treeNodeNew = treeNode; //获取当前展开节点
    var _this = this;
    if (treeNode.pId !== null) {
        if (treeNode.children === undefined && treeNode.type == "assignment") {
            this.getChildNode(treeNode, treeId);
        } else if (treeNode.type == "group") {
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
                        var chNodes = result[i]; //获取对应的value
                        var parentTid = _this.zTreeIdJson[pid][0];
                        var parentNode = treeObj.getNodeByTId(parentTid);
                        if (parentNode.children === undefined) {
                            treeObj.addNodes(parentNode, []);
                        }
                    });
                }
            }.bind(this))
        }
    }
};

AreaMonitorTree.prototype.setMenuCrrentSubV = function (vid) {
    this.menuCrrentSubV = [];
    this.menuCrrentSubV.push(vid);
};

AreaMonitorTree.prototype.checkCurrentNodes = function (treeId) {
    var treeObj = $.fn.zTree.getZTreeObj(treeId);
    var arr = this.menuCrrentSubV;
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
};

AreaMonitorTree.prototype.zTreeBeforeAsync = function () {
    return this.dependency.get('flags').bflag;
};

AreaMonitorTree.prototype.zTreeOnNodeCreated = function (event, treeId, treeNode) {
    var id = treeNode.id.toString();
    var list = [];
    if (this.zTreeIdJson[id] == undefined || this.zTreeIdJson[id] == null) {
        list = [treeNode.tId];
        this.zTreeIdJson[id] = list;
    } else {
        this.zTreeIdJson[id].push(treeNode.tId)
    }
};

//对象树加载成功
AreaMonitorTree.prototype.zTreeOnAsyncSuccess = function (event, treeId, treeNode, msg) {
    var zTree = $.fn.zTree.getZTreeObj(treeId);
    this.dependency.get('flags').bflag1 = false;
    var areaTreeAllCheck = this.dependency.get('data').getAreaTreeAllCheck();
    if (this.monitorSize < 5000 && areaTreeAllCheck) {
        zTree.expandAll(true);
    }
    if (this.monitorSize <= this.monitorMaxCheckSize && areaTreeAllCheck) {
        zTree.checkAllNodes(true);
        if (!this.dependency.get('flags').changeFlag)
            this.getCharSelect(zTree);
    }
    this.dependency.get('data').setAreaTreeAllCheck(false);
};

AreaMonitorTree.prototype.zTreeBeforeCheck = function (treeId, treeNode) {
    var flag = true;
    if (!treeNode.checked) {
        if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
            var zTree = $.fn.zTree.getZTreeObj(treeId), nodes = zTree
                .getCheckedNodes(true), v = "";
            var nodesLength = 0;
            json_ajax("post", "/clbs/m/personalized/ico/getCheckedVehicle",
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
                nodeId = nodes[i].id;
                if (nodes[i].type == "people" || nodes[i].type == "vehicle" || nodes[i].type == "thing") {
                    if ($.inArray(nodeId, ns) == -1) {
                        ns.push(nodeId);
                    }
                }
            }
            nodesLength = ns.length + 1;
        }
        if (nodesLength > this.monitorMaxCheckSize) {
            // layer.msg(maxSelectItem);
            layer.msg('最多勾选' + this.monitorMaxCheckSize + '个监控对象');
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
};

AreaMonitorTree.prototype.getTreeUrl = function (treeId, treeNode) {
    if (treeNode == null) {
        return "/clbs/m/functionconfig/fence/bindfence/getTreeByMonitorCount";
    } else if (treeNode.type == "assignment") {
        return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=monitor";
    }
};

AreaMonitorTree.prototype.ajaxDataFilter = function (treeId, parentNode, responseData) {
    var responseData = JSON.parse(ungzip(responseData.msg));
    // var monitorArr = [];
    this.monitorSize = 0;
    if (responseData) {
        for (var i = 0; i < responseData.length; i++) {
            if (responseData[i].iconSkin != "assignmentSkin") {
                responseData[i].open = true;
            }
            responseData[i].nocheck = false;
            /*if (responseData[i].type == 'vehicle' || responseData[i].type == 'people' || responseData[i].type == 'thing') {
                           monitorArr.push(responseData[i]);
                       }*/
            if (responseData[i].type == 'assignment' && responseData[i].canCheck) {
                this.monitorSize += responseData[i].canCheck;
            }
        }
    }
    // size = monitorArr.length;
    return responseData;
};

AreaMonitorTree.prototype.zTreeBeforeClick = function (treeId, treeNode, clickFlag) {
    var zTree = $.fn.zTree.getZTreeObj(treeId);
    zTree.checkNode(treeNode, !treeNode.checked, true, true);
    return false;
};
//对象树勾选
AreaMonitorTree.prototype.onCheckVehicle = function (e, treeId, treeNode) {
    var zTree = $.fn.zTree.getZTreeObj(treeId);
    //若为取消勾选则不展开节点
    if (treeNode.checked) {
        this.searchFlag = false;
        zTree.expandNode(treeNode, true, true, true, true); // 展开节点
    }
    this.dependency.get('data').setAreaTreeAllCheck(false);
    this.menuCrrentSubV = [];
    this.menuCrrentSubV.push(treeNode.id);
    this.getCharSelect(zTree);
    this.getCheckedNodes();
};
AreaMonitorTree.prototype.getCharSelect = function (treeObj) {
    var nodes = treeObj.getCheckedNodes(true);
    var allNodes = treeObj.getNodes();
    if (nodes.length > 0) {
        $("#groupSelect").val(allNodes[0].name);
    } else {
        $("#groupSelect").val("");
    }
};
//获取到选择的节点
AreaMonitorTree.prototype.getCheckedNodes = function () {
    var zTree = $.fn.zTree.getZTreeObj("areaTreeDemo"), nodes = zTree.getCheckedNodes(true), v = "", vid = "";
    for (var i = 0, l = nodes.length; i < l; i++) {
        if (nodes[i].type == "vehicle" || nodes[i].type == "people" || nodes[i].type == "thing") {
            v += nodes[i].name + ",";
            vid += nodes[i].id + ",";
        }
    }
    this.dependency.get('data').setAreaTreeCheckVid(vid);
    this.areaVehicleNameList = v;
};

//模糊查询树
AreaMonitorTree.prototype.ajaxQueryDataFilter = function (treeId, parentNode, responseData) {
    responseData = JSON.parse(ungzip(responseData));
    if ($('#queryType').val() == "vehicle") {
        return filterQueryResult(responseData, this.menuCrrentSubV);
    } else return responseData;
};
//对象树加载成功
AreaMonitorTree.prototype.searchZTreeOnAsyncSuccess = function (event, treeId, treeNode, msg) {
    var zTree = $.fn.zTree.getZTreeObj(treeId);
    this.dependency.get('flags').bflag1 = false;
    zTree.expandAll(true);
};
AreaMonitorTree.prototype.searchAreaTreeVehicle = function (param) {
    this.dependency.get('data').setAreaTreeAllCheck(false);
    this.menuCrrentSubV = [];
    if (param == null || param == undefined || param == '') {
        if (this.inputFlag) {
            this.inputFlag = false;
            this.treeInit();
        }
    } else {
        this.inputFlag = true;
        var querySetting = {
            async: {
                url: "/clbs/a/search/reportFuzzySearch",
                type: "post",
                enable: true,
                autoParam: ["id"],
                dataType: "json",
                otherParam: {"type": $('#queryType').val(), "queryParam": param, 'queryType': 'multiple'},
                dataFilter: AreaMonitorTree.prototype.ajaxQueryDataFilter.bind(this)
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
                beforeClick: AreaMonitorTree.prototype.zTreeBeforeClick.bind(this),
                onCheck: AreaMonitorTree.prototype.onCheckVehicle.bind(this),
                onExpand: AreaMonitorTree.prototype.zTreeOnExpand.bind(this),
                onAsyncSuccess: AreaMonitorTree.prototype.searchZTreeOnAsyncSuccess.bind(this),
                onNodeCreated: AreaMonitorTree.prototype.zTreeOnNodeCreated.bind(this)
            }
        };
        $.fn.zTree.init(this.selector, querySetting, null);
    }
};
