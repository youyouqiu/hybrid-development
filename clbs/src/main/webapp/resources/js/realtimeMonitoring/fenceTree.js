var FenceTree = function (options, dependency) {
    this.dependency = dependency;

    $("#vFenceSearch").bind('input oninput', FenceTree.prototype.vsearchFenceCarSearch.bind(this));
    $("#fenceToolBtn").on("click", FenceTree.prototype.fenceToolClickSHFn.bind(this));
};

//根据车id查询当前车辆绑定围栏信息
FenceTree.prototype.getCurrentVehicleAllFence = function (vId) {
    var fenceSetting = {
        async: {
            url: "/clbs/m/functionconfig/fence/bindfence/fenceTreeByVid",
            type: "post",
            enable: true,
            autoParam: ["id"],
            dataType: "json",
            otherParam: {"vid": vId}, //监控对象ID
            dataFilter: FenceTree.prototype.FenceAjaxDataFilter.bind(this)
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
            onClick: FenceTree.prototype.vFenceTreeClick.bind(this),
            onCheck: FenceTree.prototype.vFenceTreeCheck.bind(this)
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
};
//当前监控对象围栏点击
FenceTree.prototype.vFenceTreeClick = function (e, treeId, treeNode) {
    var zTree = $.fn.zTree.getZTreeObj("vFenceTree");
    zTree.checkNode(treeNode, !treeNode.checked, null, true);
    return false;
    // this.showZtreeCheckedToMap(treeNode, zTree);
};
//当前监控对象围栏勾选
FenceTree.prototype.vFenceTreeCheck = function (e, treeId, treeNode) {
    var zTree = $.fn.zTree.getZTreeObj("vFenceTree");
    var map = this.dependency.get('map');
    map.showZtreeCheckedToMap(treeNode, zTree);
};
//当前监控对象围栏查询
FenceTree.prototype.vsearchFenceCarSearch = function () {
    search_ztree('vFenceTree', 'vFenceSearch', 'fence');
};
//当前监控对象围栏预处理的函数
FenceTree.prototype.FenceAjaxDataFilter = function (treeId, parentNode, responseData) {
    if (responseData) {
        for (var i = 0; i < responseData.length; i++) {
            var data = responseData[i];
            data.open = false;
            data.name = html2Escape(data.name);
            if (data.markIcon == 1) {
                data.iconSkin = 'zw_m_marker_circle_skin';
            }
        }
    }
    return responseData;
};
//车辆树单双击获取当前围栏信息
FenceTree.prototype.vehicleTreeClickGetFenceInfo = function (treeStatus, treeId) {
    if (treeStatus == true) {
        //清空搜索条件
        if ($("#vFenceSearch").val() != "" || $("#vFenceSearch").val() != null) {
            $("#vFenceSearch").val("");
        }
        //清除围栏集合及地图显示
        var map = this.dependency.get('map');
        map.delFenceListAndMapClear();
        //订阅后查询当前对象绑定围栏信息
        this.getCurrentVehicleAllFence(treeId);
        //显示围栏树及搜索 隐藏消息提示
        $("#vFenceTree").removeClass("hidden");
        $("#vSearchContent").removeClass("hidden");
        $("#vFenceMsg").addClass("hidden");
    } else {
        $("#vFenceTree").html("").addClass("hidden");
        $("#vSearchContent").addClass("hidden");
    }
};
//围栏显示隐藏
FenceTree.prototype.fenceToolClickSHFn = function () {
    if ($("#fenceTool>.dropdown-menu").is(":hidden")) {
        $("#fenceTool>.dropdown-menu").show();
    } else {
        $("#fenceTool>.dropdown-menu").hide();
    }
};
//当点击或选择围栏时，访问后台返回围栏详情
FenceTree.prototype.getFenceDetailInfo = function (fenceNode, showMap) {
    // ajax访问后端查询
    layer.load(2);
    var map = this.dependency.get('map');
    $.ajax({
        type: "POST",
        url: "/clbs/m/functionconfig/fence/bindfence/getFenceDetails",
        data: {
            "fenceNodes": JSON.stringify(fenceNode)
        },
        dataType: "json",
        success: function (data) {
            layer.closeAll('loading');
            if (data.success) {
                var dataList = data.obj;
                if (dataList != null && dataList.length > 0) {
                    if (dataList[0].fenceType == "zw_m_line") {
                        fanceID = dataList[0].fenceData[0].lineId;
                    }
                    for (var i = 0; i < dataList.length; i++) {
                        var fenceData;
                        var fenceType = dataList[i].fenceType;
                        var wayPointArray;
                        if (fenceType == 'zw_m_travel_line') {
                            fenceData = dataList[i].allPoints;
                            wayPointArray = dataList[i].passPointData;
                        } else {
                            fenceData = dataList[i].fenceData;
                        }
                        var lineSpot = dataList[i].lineSpot == undefined ? [] : dataList[i].lineSpot;
                        var lineSegment = dataList[i].lineSegment == undefined ? [] : dataList[i].lineSegment;
                        if (fenceType == "zw_m_marker") { // 标注
                            map.drawMarkToMap(fenceData, showMap);
                        } else if (fenceType == "zw_m_line") { // 线
                            map.drawLineToMap(fenceData, lineSpot, lineSegment, showMap);
                        } else if (fenceType == "zw_m_rectangle") { // 矩形
                            map.drawRectangleToMap(fenceData, showMap);
                        } else if (fenceType == "zw_m_polygon") { // 多边形
                            map.drawPolygonToMap(fenceData, showMap);
                        } else if (fenceType == "zw_m_circle") { // 圆形
                            map.drawCircleToMap(fenceData, showMap);
                        } else if (fenceType == "zw_m_administration") { // 行政区域
                            var aId = dataList[0].aId;
                            map.drawAdministrationToMap(fenceData, aId, showMap);
                        } else if (fenceType == "zw_m_travel_line") { // 行驶路线
                            map.drawTravelLineToMap(fenceData, showMap, dataList[i].travelLine, wayPointArray);
                        }
                    }
                }
            }
        }
    });
};
