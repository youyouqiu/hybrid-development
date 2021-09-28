var searchParam = "";
var searchTypeValue = "";
var search_ztree_Obj = {};

/**
 * 展开树
 * @param treeId
 */
function expand_ztree(treeId) {
    var treeObj = $.fn.zTree.getZTreeObj(treeId);
    treeObj.expandAll(true);
}

/**
 * 收起树：只展开根节点下的一级节点
 * @param treeId
 */
function close_ztree(treeId) {
    var treeObj = $.fn.zTree.getZTreeObj(treeId);
    var nodes = treeObj.transformToArray(treeObj.getNodes());
    var nodeLength = nodes.length;
    for (var i = 0; i < nodeLength; i++) {
        if (nodes[i].id == '0') {
            //根节点：展开
            treeObj.expandNode(nodes[i], true, true, false);
        } else {
            //非根节点：收起
            treeObj.expandNode(nodes[i], false, true, false);
        }
    }
}

/**
 * 搜索树，高亮显示并展示【模糊匹配搜索条件的节点s】
 * @param treeId
 * @param searchConditionId 文本框的id
 */
function search_ztree(treeId, searchConditionId, type, searchValue) {
    search_ztree_Obj = {
        treeId: treeId,
        searchConditionId: searchConditionId,
        type: type,
        searchValue: searchValue
    };
    searchByFlag_ztree(treeId, searchConditionId, "", type, searchValue);
}

/**
 * 搜索树，高亮显示并展示【模糊匹配搜索条件的节点s】
 * @param treeId
 * @param searchConditionId     搜索条件Id
 * @param flag                  需要高亮显示的节点标识
 */
function searchByFlag_ztree(treeId, searchConditionId, flag, type, searchValue) {
    //<1>.搜索条件
    var searchCondition = searchValue != undefined ? searchValue : $('#' + searchConditionId).val();
    var highlightNodes = [],
        newHighlightNodes = [];
    var allNodes = [];
    var treeObj = $.fn.zTree.getZTreeObj(treeId);
    searchParam = searchCondition;
    if (type == "vehicle") {
        highlightNodes = treeObj.getNodesByFilter(monitorParamFuzzyFilter);
        // allNodes = treeObj.getNodesByFilter(monitorFilter); // 所有type型nodes
        allNodes = treeObj.transformToArray(treeObj.getNodes()); // 所有节点
        newHighlightNodes = highlightNodes;
    } else {
        highlightNodes = treeObj.getNodesByParamFuzzy("name", searchCondition, null); // 满足搜索条件的节点
        allNodes = treeObj.getNodesByParam("type", type, null); // 所有type型nodes
        if (type == 'assignment') {
            allNodes = allNodes.concat(treeObj.getNodesByParam("type", 'group', null))
        }

        for (var i = 0, len = highlightNodes.length; i < len; i++) {
            if (highlightNodes[i].type == type) {
                newHighlightNodes.push(highlightNodes[i]);
            }
        }
    }
    if (searchCondition != "") {
        searchParam = searchCondition;
        if (type == "group" || type == "assignment") { // 企业
            // 需要显示是节点（包含父节点）
            var showNodes = [];
            if (newHighlightNodes != null) {
                for (var i = 0; i < newHighlightNodes.length; i++) {
                    //组装显示节点的父节点的父节点....直到根节点，并展示
                    getParentShowNodes_ztree(treeId, newHighlightNodes[i], showNodes);
                }
                treeObj.hideNodes(allNodes);
                treeObj.showNodes(showNodes);
                treeObj.expandAll(true);
            }
            /*  }else if (type == "vehicle"){
                  treeObj.hideNodes(allNodes)
                  treeObj.showNodes(highlightNodes);
                  treeObj.expandAll(true);*/
        } else {
            //<2>.得到模糊匹配搜索条件的节点数组集合
            //            treeObj.hideNodes(allNodes);
            //            treeObj.showNodes(highlightNodes);
            //            treeObj.expandAll(true);
            // 需要显示是节点（包含父节点）
            var showNodes = [];
            // 只显示直接上级
            if (newHighlightNodes != null) {
                for (var i = 0; i < newHighlightNodes.length; i++) {
                    //组装显示节点的父节点的父节点....直到根节点，并展示
                    getParentShowNodes_ztree(treeId, newHighlightNodes[i], showNodes);
                }
                treeObj.hideNodes(allNodes);
                treeObj.showNodes(showNodes);
                treeObj.expandAll(true);
            }
        }
    } else {
        treeObj.showNodes(allNodes);
        treeObj.expandAll(true);
    }
    //<3>.高亮显示并展示【指定节点s】
    // highlightAndExpand_ztree(treeId, highlightNodes, flag);
}

function realTimeMonitoringFilter(node) { // 模糊搜索从业人员，终端编号
    return (node.type == "vehicle" && node.name.indexOf(searchParam) > -1) ||
        (node.type == "people" && node.name.indexOf(searchParam) > -1) ||
        (node.type == "thing" && node.name.indexOf(searchParam) > -1) ||
        (node.professional != undefined && node.professional != null && node.professional.indexOf(searchParam) > -1) ||
        (node.simcardNumber != undefined && node.simcardNumber != null && node.simcardNumber.indexOf(searchParam) > -1) ||
        (node.assignName != undefined && node.assignName != null && node.assignName.indexOf(searchParam) > -1) ||
        (node.deviceNumber != undefined && node.deviceNumber != null && node.deviceNumber.indexOf(searchParam) > -1)
}

function monitorFilter(node) { // 搜索type等于人或者车
    return node.type == "vehicle" || node.type == "people" || node.type == "thing"
}

function monitorParamFuzzyFilter(node) { // 模糊匹配name,type等于人或者车
    return (node.type == "vehicle" && node.name.indexOf(searchParam) > -1) || (node.type == "people" && node.name.indexOf(searchParam) > -1) || (node.type == "thing" && node.name.indexOf(searchParam) > -1)
}

/**
 *  搜索没有展开的分组节点
 * @param node
 * @returns
 */
function assignmentNotExpandFilter(node) { // 搜索type等于人或者车
    return node.type == "assignment" && node.children != undefined && node.children.length > 0 && node.children[0].open == false;
}

/**
 *  搜索所有的对象
 * @param node
 * @returns
 */
function moniterFilter(node) { // 搜索type等于人或者车
    return (node.type == "vehicle" || node.type == "people" || node.type == "thing") && node.isHidden === false;
}

/**
 * 搜索树，高亮显示并展示【模糊匹配搜索条件的节点s】
 * @param treeId
 * @param searchConditionId     搜索条件Id
 * @param flag                  需要高亮显示的节点标识
 */
function high_search_ztree(treeId, searchConditionId, hasBegun) {
    //<1>.搜索条件
    var searchCondition = $('#' + searchConditionId).val();
    var highlightNodes = [];
    //    var allNodes = [];
    var treeObj = $.fn.zTree.getZTreeObj(treeId);
    searchParam = searchCondition;
    highlightNodes = treeObj.getNodesByFilter(realTimeMonitoringFilter);
    // allNodes = treeObj.getNodesByFilter(monitorFilter); // 所有type型nodes
    var allNodes = treeObj.transformToArray(treeObj.getNodes()); // 所有节点
    if (searchCondition != "") {
        //<2>.得到模糊匹配搜索条件的节点数组集合
        // 需要显示是节点（包含父节点）
        var showNodes = [];
        // 只显示直接上级
        if (highlightNodes != null) {
            for (var i = 0; i < highlightNodes.length; i++) {
                //组装显示节点的父节点的父节点....直到根节点，并展示
                if (hasBegun.indexOf(highlightNodes[i].getParentNode().id) == -1) {
                    hasBegun.push(highlightNodes[i].getParentNode().id)
                    treeObj.expandNode(highlightNodes[i].getParentNode(), true, true, false, true);
                }
                getParentShowNodes_ztree(treeId, highlightNodes[i], showNodes);
            }

            treeObj.hideNodes(allNodes)
            treeObj.showNodes(showNodes);
            // treeObj.expandAll(true);
        }
    } else {
        //	var allNodes1 = treeObj.transformToArray(treeObj.getNodes()); // 所有节点
        //    	treeObj.hideNodes(allNodes)
        treeObj.showNodes(allNodes)
        treeObj.expandAll(true);
    }
    //<3>.高亮显示并展示【指定节点s】
    // highlightAndExpand_ztree(treeId, highlightNodes, flag);
}

function searchTypeFilter(node) { // 模糊搜索从业人员，终端编号
    var value = node['' + searchTypeValue + ''];
    return ((node.type == "vehicle" || node.type == "people" || node.type == "thing") && value != undefined && value != null && value.indexOf(searchParam) > -1)
}

/**
 * 搜索树，根据搜索类型模糊匹配
 * @param treeId
 * @param searchConditionId     搜索条件Id
 * @param searchType            搜索条件type
 * @param flag                  需要高亮显示的节点标识
 */
function search_ztree_by_search_type(treeId, searchConditionId, searchType, hasBegun) {
    //<1>.搜索条件
    var searchCondition = $('#' + searchConditionId).val();
    var highlightNodes = [];
    var treeObj = $.fn.zTree.getZTreeObj(treeId);
    searchParam = searchCondition;
    searchTypeValue = searchType;
    //highlightNodes = treeObj.getNodesByFilter(searchTypeFilter);
    // highlightNodes = treeObj.getNodesByParamFuzzy(searchType, searchCondition, null);
    var allNodes = treeObj.transformToArray(treeObj.getNodes()); // 所有节点
    if (searchCondition != "") {
        //<2>.得到模糊匹配搜索条件的节点数组集合
        // 需要显示是节点（包含父节点）
        var showNodes = [];
        // 只显示直接上级
        if (allNodes != null) {
            for (var i = 0; i < allNodes.length; i++) {
                var node = allNodes[i];
                var value = node['' + searchType + ''];
                if ((node.type == "vehicle" || node.type == "people" || node.type == "thing") && value != undefined && value != null && value.indexOf(searchParam) > -1) {
                    //highlightNodes.push(node);
                    //组装显示节点的父节点的父节点....直到根节点，并展示
                    if (hasBegun.indexOf(node.getParentNode().id) == -1) {
                        hasBegun.push(node.getParentNode().id)
                        treeObj.expandNode(node.getParentNode(), true, true, false, true);
                    }
                    getParentShowNodes_ztree(treeId, node, showNodes);
                }
            }
            treeObj.hideNodes(allNodes)
            treeObj.showNodes(showNodes);
        }

        /*if (highlightNodes != null) {
            for (var i = 0; i < highlightNodes.length; i++) {
                //组装显示节点的父节点的父节点....直到根节点，并展示
                if(hasBegun.indexOf(highlightNodes[i].getParentNode().id)==-1){
                    hasBegun.push(highlightNodes[i].getParentNode().id)
                    treeObj.expandNode(highlightNodes[i].getParentNode(), true, true, false, true);
                }
                getParentShowNodes_ztree(treeId, highlightNodes[i],showNodes);
            }

            treeObj.hideNodes(allNodes)
            treeObj.showNodes(showNodes);
        }*/
    } else {
        treeObj.showNodes(allNodes)
        treeObj.expandAll(true);
    }
}

function showSearchNodes(treeId, checkedList) {
    var treeObj = $.fn.zTree.getZTreeObj(treeId);
    var allNodes = treeObj.transformToArray(treeObj.getNodes()); // 所有节点
    //<2>.得到模糊匹配搜索条件的节点数组集合
    // 需要显示是节点（包含父节点）
    var showNodes = [];
    var checkedNodes = [];
    // 只显示直接上级
    if (allNodes !== null) {
        for (var i = 0; i < allNodes.length; i++) {
            var node = allNodes[i];
            //        	var value = node[''+searchType+''];
            if ((node.type === "vehicle" || node.type === "people" || node.type === "thing")) {
                // 勾选搜索前勾选的车辆
                if (checkedList !== null && checkedList !== undefined && checkedList.length > 0 &&
                    checkedList.indexOf(node.id) !== -1) {
                    treeObj.checkNode(node, true, true);
                }
                //组装显示节点的父节点的父节点....直到根节点，并展示
                if (checkedNodes.indexOf(node.pId) >= 0) {
                    showNodes.push(node);
                    continue;
                }
                checkedNodes.push(node.pId);
                treeObj.expandNode(node.getParentNode(), true, true, false, true);
                getParentShowNodes_ztree(treeId, node, showNodes);
            }
        }
        treeObj.hideNodes(allNodes);
        treeObj.showNodes(showNodes);
    }
}

function filterQueryResult(data, checkedList, filterType) {
    if (data === null) {
        return;
    }

    if (checkedList === undefined || checkedList === null) {
        checkedList = [];
    }

    // 初始化节点为hashMap
    var filterNode = {};
    for (var i = 0; i < data.length; i++) {
        filterNode[data[i].id] = data[i];
    }
    for (i = 0; i < checkedList.length; i++) {
        var curNode = filterNode[checkedList[i]];
        if (curNode != undefined && curNode != null) {
            filterNode[checkedList[i]].checked = true;
        }
    }
    for (i = 0; i < data.length; i++) {
        if ((filterType && data[i].type === filterType) || isMonitorType(data[i].type)) {
            getAllAvailableNodes(data[i], filterNode);
        }
    }

    // 保持与后端传递过来的组织树顺序一致
    var result = [];
    for (i = 0; i < data.length; i++) {
        var key = data[i].id;
        if (filterNode[key].checkdStatus === 'checked') {
            if (filterNode[key].checked) {
                data[i].checked = true;
            }
            result.push(data[i]);
        }
    }
    return result;
}

function getAllAvailableNodes(node, filterNode) {
    var parentNode = filterNode[node.pId];
    filterNode[node.id].checkdStatus = "checked";
    filterNode[node.pId].checkdStatus = "checked";
    if (parentNode && filterNode[parentNode.pId] && filterNode[parentNode.pId].checkdStatus !== "checked") {
        getAllAvailableNodes(parentNode, filterNode);
    }
}

function isMonitorType(type) {
    return type === "vehicle" || type === "people" || type === "thing";
}

function zTreeScroll(zTree, scroll) {
    var prevTop = 0;
    var top = scroll.scrollTop;
    if (prevTop <= top) { //下滚
        // 获取没有展开的分组节点
        var notExpandNodes = zTree.getNodesByFilter(assignmentNotExpandFilter);
        if (notExpandNodes !== undefined && notExpandNodes.length > 0) {
            for (var i = 0; i < notExpandNodes.length; i++) {
                var node = notExpandNodes[i];
                var tid = node.tId + "_a";
                var divHeight = scroll.offsetTop;
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
        prevTop = top;
    }, 0);
}

/**
 * 高亮显示并展示【指定节点s】
 * @param treeId
 * @param highlightNodes 需要高亮显示的节点数组
 * @param flag           需要高亮显示的节点标识
 */
function highlightAndExpand_ztree(treeId, highlightNodes, flag) {
    var treeObj = $.fn.zTree.getZTreeObj(treeId);
    //<1>. 先把全部节点更新为普通样式
    var treeNodes = treeObj.transformToArray(treeObj.getNodes());
    for (var i = 0; i < treeNodes.length; i++) {
        treeNodes[i].highlight = false;
        treeObj.updateNode(treeNodes[i]);
    }
    //<2>.收起树, 只展开根节点下的一级节点
    // close_ztree(treeId);
    //<3>.把指定节点的样式更新为高亮显示，并展开
    if (highlightNodes != null) {
        for (var i = 0; i < highlightNodes.length; i++) {
            if (flag != null && flag != "") {
                if (highlightNodes[i].flag == flag) {
                    //高亮显示节点，并展开
                    highlightNodes[i].highlight = true;
                    treeObj.updateNode(highlightNodes[i]);
                    //高亮显示节点的父节点的父节点....直到根节点，并展示
                    var parentNode = highlightNodes[i].getParentNode();
                    var parentNodes = getParentNodes_ztree(treeId, parentNode);
                    treeObj.expandNode(parentNodes, true, false, true);
                    treeObj.expandNode(parentNode, true, false, true);
                }
            } else {
                //高亮显示节点，并展开
                highlightNodes[i].highlight = true;
                treeObj.updateNode(highlightNodes[i]);
                //高亮显示节点的父节点的父节点....直到根节点，并展示
                // setFontCss_ztree(treeId,highlightNodes[i]);
                var parentNode = highlightNodes[i].getParentNode();
                var parentNodes = getParentNodes_ztree(treeId, parentNode);
                treeObj.expandNode(parentNodes, true, false, true);
                treeObj.expandNode(parentNode, true, false, true);
            }
        }
    }
}

/**
 * 递归得到指定节点的父节点的父节点....直到根节点
 */
function getParentNodes_ztree(treeId, node) {
    if (node !== null) {
        var parentNode = node.getParentNode();
        return getParentNodes_ztree(treeId, parentNode);
    } else {
        return node;
    }
}

/**
 * 递归得到指定节点的父节点的父节点....直到根节点（用于企业搜索）
 */
function getParentShowNodes_ztree(treeId, node, showNodes) {
    if (node !== null) {
        showNodes.push(node);
        var parentNode = node.getParentNode();
        return getParentShowNodes_ztree(treeId, parentNode, showNodes);
    } else {
        return node;
    }
}

/**
 * 设置树节点字体样式
 */

// else if(treeNode.name==='渝A19999   ACC'){
//     return {
//         color: "#53D327",
//         "font-weight": "normal"
//     };
// }
function setFontCss_ztree(treeId, treeNode) {
    if (treeNode.id == 0) {
        //根节点
        return {
            color: "#333",
            "font-weight": "bold"
        };
    } else {
        if (treeNode.vehicleType == "out") { // 车辆树结构有父级组织的车
            if (!!treeNode.highlight) {
                return {
                    color: "#6dcff6",
                    "font-weight": "bold"
                };
            } else {
                return {
                    color: "red"
                };
            }
        } else {
            return (!!treeNode.highlight) ? {
                color: "#6dcff6",
                "font-weight": "bold"
            } : {
                color: "#333",
                "font-weight": "normal"
            };
        }

    }
}

function setFontCss_sphf_ztree(treeId, treeNode) {
    if (treeNode.id == 0) {
        //根节点
        return {
            color: "#333",
            "font-weight": "bold"
        };
    } else {
        if (treeNode.vehicleType == "out") { // 车辆树结构有父级组织的车
            if (!!treeNode.highlight) {
                return {
                    color: "#6dcff6",
                    "font-weight": "bold"
                };
            } else {
                return {
                    color: "red"
                };
            }
        } else if (treeNode.status && treeNode.status != 3) {
            return {
                color: "#45a541"
            };
        } else {
            return (!!treeNode.highlight) ? {
                color: "#6dcff6",
                "font-weight": "bold"
            } : {
                color: "#333",
                "font-weight": "normal"
            };
        }

    }
}
/*树搜索框-搜索类型*/

const treeSearchType = {
    nodeBoxEl: $("#treeSearchTypeBox") || null,
    nodeEl: null,
    value: '',
    text: '',
    treeNode: null,
    treeSetting: null,
    init: function (defaultVal, options, id) {
        // treeSearchType.nodeBoxEl = $("#" + id);
        if (!treeSearchType.nodeBoxEl) {
            return false
        }
        let renderStr = '';
        if (defaultVal == undefined || defaultVal == null) {
            defaultVal = 'assignment';
        }
        renderStr += '<select name="" id="treeSearchType"  value="' + (defaultVal) + '" class="form-control">';
        // 拼装-options
        if (Array.isArray(options)) {
            options.forEach(function (val) {
                renderStr += ' <option value="' + val.value + '">' + val.label + '</option>';
            })
        } else {
            renderStr += ' <option data-placeholder="请输入分组名称"  value="assignment">分组名称</option>';
            renderStr += '  <option data-placeholder="请输入企业名称" value="group">企业名称</option>';
        }
        renderStr += '</select>';
        treeSearchType.nodeBoxEl.html(renderStr)
        treeSearchType.nodeEl = $("#treeSearchType");
        treeSearchType.value = defaultVal;
        treeSearchType.text = $(this).find('option:selected').text();
        treeSearchType.nodeEl.change(treeSearchType.change);
        console.log('success-type')
        // 把搜索类型字段，塞入到树形的初始化参数中
        // $.fn.zTree.init = function (base) {
        //     return function () {
        //         const type = treeSearchType.value;
        //         treeSearchType.treeNode = arguments[0];
        //         let setting = treeSearchType.treeSetting = arguments[1];
        //         let otherParam = setting.async.otherParam;
        //         if (otherParam) {
        //             otherParam.type = type;
        //         } else {
        //             otherParam = { type: type };
        //         }
        //         setting.async.otherParam = otherParam;

        //         base.apply(this, arguments);
        //     }
        // }($.fn.zTree.init);
    },
    update: function (val, options) {
        //如果没有选择节点
        if (!treeSearchType.nodeEl) {
            let renderStr = '';
            renderStr += '<select name="" id="#treeSearchType"  value="" class="form-control">';
            renderStr += '</select>';
            treeSearchType.nodeBoxEl.html(renderStr)
            treeSearchType.nodeEl = $("#treeSearchTypeBox"); //容器节点 
            treeSearchType.nodeEl.change(treeSearchType.change)
        }
        // 拼装-options
        if (Array.isArray(options)) {
            let renderOptions = '';
            options.forEach(function (val) {
                renderOptions += ' <option value="' + val.value + '">' + val.label + '</option>';
            })
            treeSearchType.nodeEl.html(renderStr);
        }
        treeSearchType.nodeEl.val(val);
        treeSearchType.value = defaultVal;
        treeSearchType.text = $(this).find('option:selected').text()
    },
    onChange: function () {
        return false
    },
    change: function () {
        treeSearchType.value = $(this).val();
        const optEl = $(this).find('option:selected');
        treeSearchType.text = optEl.text();
        const params = {
            value: treeSearchType.value,
            text: treeSearchType.text,
            placeholder: optEl.data('placeholder')
        }
        treeSearchType.onChange && treeSearchType.onChange(params)
        // $.fn.zTree.init(treeSearchType.treeNode, treeSearchType.treeSetting)
        // console.log(treeSearchType.treeNode, treeSearchType.treeSetting)
    }
}