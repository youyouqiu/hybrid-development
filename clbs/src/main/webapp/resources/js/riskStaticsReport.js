(function (window, $) {
    //车辆列表
    var vehicleList = [];
    //车辆id列表
    var vehicleId = [];
    //开始时间
    var startTime;
    var myTable;
    //结束时间
    var endTime;
    var checkFlag = false; //判断组织节点是否是勾选操作
    var size;//当前权限监控对象数量
    var zTreeIdJson = {};

    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询
    var ifAllCheck = true; //刚进入页面小于TREE_MAX_CHILDREN_LENGTH自动勾选
    var searchFlag = true;
    var simpleQueryParam = '';
    var activeRowIndex = null;
    var tableData = null;
    var searchAfter = null; 	// es返回的写一页标记 [array]
    var detailTableIndex = 0; // 详情表格的序号列
    var commonField = null;
    var commonText = null;
    var detailVehicleId = null;
    var isLoadingDetail = false;
    var detailTableData = null;
    //多媒体
    var mediaIndex = 0;
    var title = [],
        mediaUrl = [],
        mediaType,
        mediaId;
    var detailCurrentRowIndex = null;

    riskStaticsReport = {
        init: function () {

            //车辆树
            var setting = {
                async: {
                    url: riskStaticsReport.getAlarmReportTreeUrl,//"/clbs/m/functionconfig/fence/bindfence/vehicelTree"
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: riskStaticsReport.ajaxDataFilter
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
                    beforeClick: riskStaticsReport.beforeClickVehicle,
                    onAsyncSuccess: riskStaticsReport.zTreeOnAsyncSuccess,
                    beforeCheck: riskStaticsReport.zTreeBeforeCheck,
                    onCheck: riskStaticsReport.onCheckVehicle,
                    onNodeCreated: riskStaticsReport.zTreeOnNodeCreated,
                    onExpand: riskStaticsReport.zTreeOnExpand
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
                riskStaticsReport.init();
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
                        dataFilter: riskStaticsReport.ajaxQueryDataFilter
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
                        beforeClick: riskStaticsReport.beforeClickVehicle,
                        beforeCheck: riskStaticsReport.zTreeBeforeCheck,
                        onCheck: riskStaticsReport.onCheckVehicle,
                        onExpand: riskStaticsReport.zTreeOnExpand,
                        onNodeCreated: riskStaticsReport.zTreeOnNodeCreated
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
        tableFilter: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            // menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\"" +
            //     " data-column=\"" + parseInt(1) + "\" />" + table[0].innerHTML + "</label></li>";
            var disableColumns = ['监控对象', '车牌颜色', '所属企业', '合计'];
            for (var i = 0; i < table.length; i++) {
                if (disableColumns.indexOf(table[i].innerHTML) > -1) {
                    menu_text += "<li><label style='color:gray'><input type=\"checkbox\" checked=\"checked\" disabled class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
                } else {
                    menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
                }
            }
            $("#Ul-menu-text").html(menu_text);
        },
        getAlarmReportTreeUrl: function (treeId, treeNode) {
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
                tMonth = riskStaticsReport.doHandleMonth(tMonth + 1);
                tDate = riskStaticsReport.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate;
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24
                    * parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = riskStaticsReport.doHandleMonth(endMonth + 1);
                endDate = riskStaticsReport.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate;
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = riskStaticsReport.doHandleMonth(vMonth + 1);
                vDate = riskStaticsReport.doHandleMonth(vDate);
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
                    vendMonth = riskStaticsReport.doHandleMonth(vendMonth + 1);
                    vendDate = riskStaticsReport.doHandleMonth(vendDate);
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
        getsTheCurrentTime: function (flag) {
            var nowDate = new Date();
            if (flag) {// 昨天
                nowDate = new Date(nowDate.getTime() - 1000 * 60 * 60 * 24);
            }
            startTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate());
            /* + " " + "00:00:00";*/
            endTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate());
            /*+ " "
            + ("23")
            + ":"
            + ("59")
            + ":"
            + ("59");*/
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
                    data[i].open = true;
                }
            }
            return data;
        },
        //判断日期是否合法,是否选中车辆
        validates: function () {
            return $("#hourslist").validate({
                rules: {
                    endTime: {
                        required: true,
                        compareDate: "#timeInterval"
                    },
                    startTime: {
                        required: true
                    },
                    groupSelect: {
                        zTreeChecked: "treeDemo"
                    }
                },
                messages: {
                    endTime: {
                        required: "请选择结束日期!",
                        compareDate: endtimeComStarttime
                    },
                    startTime: {
                        required: "请选择开始日期!",
                    },
                    groupSelect: {
                        zTreeChecked: vehicleSelectBrand,
                    }
                }
            }).form();
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
            riskStaticsReport.getCharSelect(treeObj);
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;
                    if (ifAllCheck) {
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
                    } else {
                        var nodes = zTree.getNodesByFilter(function (node) {
                            return node.type == "people" || node.type == "vehicle";
                        }, false, treeNode); // 查找节点集合
                        nodesLength = nodes.length;
                    }
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
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                searchFlag = false;
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    riskStaticsReport.getCheckedNodes();
                    riskStaticsReport.validates();
                }, 600);
            }
            crrentSubV = [];
            crrentSubV.push(treeNode.id);
            riskStaticsReport.getCharSelect(zTree);
            riskStaticsReport.getCheckedNodes();
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
            if (!ifAllCheck || (treeNode.type == "group" && !checkFlag)) {
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
                        riskStaticsReport.getGroupChild(node.children, assign);
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
        inquireClick: function (number) {
            $(".ToolPanel").css("display", "block");
            if (number == 0) {
                riskStaticsReport.getsTheCurrentTime();
                $('#timeInterval').val(startTime + '--' + endTime);
            } else if (number == 'yes') {// 昨天
                riskStaticsReport.getsTheCurrentTime(true);
                $('#timeInterval').val(startTime + '--' + endTime);
            } else if (number == -1) {
                riskStaticsReport.startDay(-1);
                $('#timeInterval').val(startTime + '--' + endTime);
            } else if (number == -3) {
                riskStaticsReport.startDay(-3);
                $('#timeInterval').val(startTime + '--' + endTime);
            } else if (number == -7) {
                riskStaticsReport.startDay(-7);
                $('#timeInterval').val(startTime + '--' + endTime);
            }
            riskStaticsReport.getCheckedNodes();
            if (!riskStaticsReport.validates()) {
                return;
            }
            ;
            var timeInterval = $('#timeInterval').val().split('--');
            startTime = timeInterval[0];
            endTime = timeInterval[1];
            // var url = "/clbs/m/reportManagement/riskStaticsReport/getMonitorAlarm";
            // var parameter = {"vehicleId": vehicleId, "startTime": startTime, "endTime": endTime};
            // json_ajax("POST", url, "json", true, parameter, riskStaticsReport.getCallback);
            // var mockData = [];
            // for (var i =0;i<20;i++){
            //     mockData.push({
            //         a:'abc'
            //     })
            // }
            // var mockObj = {
            //     success:true,
            //     obj:mockData
            // }
            // riskStaticsReport.getCallback(mockObj)
            var simpleQueryParam = '';
            riskStaticsReport.searchInitTabData();

            //是否可以导出
            $("#exportAlarm").prop("disabled", false);
        },
        //数据列表
        searchInitTabData: function () {
            myTable.requestData();
        },
        exportAlarm: function (e) {

            riskStaticsReport.getCheckedNodes();
            if (!riskStaticsReport.validates()) {
                return;
            }
            if(getRecordsNum() > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }

            var timeInterval = $('#timeInterval').val().split('--');
            startTime = timeInterval[0];
            endTime = timeInterval[1];
            var url = "/clbs/r/riskManagement/statisticsReport/exportList";
            var parameter = {
                "vehicleIds": vehicleId,
                "startTime": startTime.replace(/-/g, ''),
                "endTime": endTime.replace(/-/g, ''),
            };
            exportExcelUseForm(url, parameter);
        },

        showDetail: function (e) {
            e.stopPropagation();
            var $target = $(e.currentTarget);
            var cellIndex = e.currentTarget.cellIndex;
            var rowIndex = e.currentTarget.parentNode.rowIndex - 1;
            var $th = $target.closest('table').find('thead').find('th:eq(' + cellIndex + ')');
            var field = $th.attr('data-key');
            var text = $th.html();

            if (field === 'index' || field === 'brand' || field === 'plateColor' || field === 'orgName' || field === 'total') {
                return;
            }

            detailTableIndex = 0;
            searchAfter = null;
            detailTableData = [];
            $('#detailTbody').empty();

            $target.closest('tbody').find('td').removeClass('active');
            $target.addClass('active');

            commonField = field;
            commonText = text;
            detailVehicleId = tableData[rowIndex].vehicleId;

            $('#detailAlarmName').html($th.html() + '：');
            $('#detailAlarmValue').html($target.html());
            $('#detailMonitorName').html(tableData[rowIndex].brand);
            $('#detailColor').html(tableData[rowIndex].plateColor);
            $('#detailGroup').html(tableData[rowIndex].orgName);

            riskStaticsReport.loadDetail();

            $('body').addClass('drawer-open');
            $("#header-content").css({'padding-right': "17px"});
            $('#detail').addClass('active');
        },

        loadDetail: function () {
            isLoadingDetail = true;
            json_ajax('POST', "/clbs/r/riskManagement/statisticsReport/search/reportInfo", 'json', true, {
                vehicleId: detailVehicleId,
                commonField: commonField,
                searchAfter: searchAfter && searchAfter[0],
                "startTime": startTime + ' 00:00:00',
                "endTime": endTime + ' 23:59:59',
                limit: 20
            }, function (data) {
                if (data.success) {
                    if (data.obj.search_after && data.obj.search_after.length === 1 && data.obj.search_after[0]) {
                        searchAfter = data.obj.search_after;
                    }

                    if (data.obj && data.obj.data && data.obj.data.length > 0) {
                        var _data = data.obj.data;
                        detailTableData = detailTableData.concat(_data);
                        var html = '';
                        for (var i = 0; i < _data.length; i++) {
                            html += '<tr>';
                            var item = _data[i];
                            detailTableIndex++
                            html += '<td>' + detailTableIndex + '</td>';
                            html += '<td>';
                            if (item.hasPic) {
                                html += '<img src="/clbs/resources/img/previewimg-blue.svg" onclick="riskStaticsReport.showMedia(0,event)" class="detail-img"/>'
                            } else {
                                html += '<img src="/clbs/resources/img/previewimg-grey.svg" class="detail-img"/>'
                            }
                            if (item.hasVideo) {
                                html += '<img src="/clbs/resources/img/video-blue.svg" onclick="riskStaticsReport.showMedia(2,event)" class="detail-img"/>'
                            } else {
                                html += '<img src="/clbs/resources/img/video-grey.svg" class="detail-img"/>'
                            }
                            html += '</td>';
                            html += '<td>' + commonText + '</td>';
                            html += '<td>' + item.starTime + '</td>';
                            html += '<td>' + item.endTime + '</td>';
                            html += '<td>' + item.address + '</td>';
                            html += '</tr>';
                        }
                        $('#detailTbody').append($(html));
                    }
                }
                isLoadingDetail = false;
            })
        },

        detailTableScroll: function (e) {
            if (isLoadingDetail) {
                return;
            }
            var BOTTOM_OFFSET = 50;
            var $table = $('#detailTable');
            var $container = $('#detailTableContainer');
            var $currentWindow = $(window);
            //当前窗口的高度
            var containerHeight = $container.height();
            //当前滚动条从上往下滚动的距离
            var scrollTop = $container.scrollTop();
            //当前文档的高度
            var docHeight = $table.height();
            //当 滚动条距底部的距离 + 滚动条滚动的距离 >= 文档的高度 - 窗口的高度
            //换句话说：（滚动条滚动的距离 + 窗口的高度 = 文档的高度） 这个是基本的公式
            if ((BOTTOM_OFFSET + scrollTop) >= docHeight - containerHeight) {
                //这里可以写判断逻辑
                riskStaticsReport.loadDetail();
            }
        },
        showMedia: function (type, e) {
            var $target = $(e.currentTarget);
            var $td = $target.closest('td');
            var rowIndex = $td.parent().get(0).rowIndex - 1;
            detailCurrentRowIndex = rowIndex;
            mediaType = type;
            mediaId = detailTableData[detailCurrentRowIndex].riskEventId;
            var mediaContent = $('.media-content');
            type == 0 ? mediaContent.removeClass('video_show') : mediaContent.addClass('video_show');
            riskStaticsReport.getEventMediaAjax();
        },
        getEventMediaAjax: function () {
            json_ajax('POST', "/clbs/r/riskManagement/disposeReport/getEventMedia", 'json', true, {
                "eventId": mediaId,
                "mediaType": mediaType
            }, function (datas) {
                var data = datas.obj;
                //重置
                mediaIndex = 0;
                title = [];
                mediaUrl = [];
                if (datas.success) {
                    for (var i = 0, len = data.length; i < len; i++) {
                        var item = data[i];
                        title.push(item.riskType + '--' + item.riskEventType);
                        mediaUrl.push(item.mediaUrl);
                    }
                } else {
                    layer.msg('获取数据失败');
                }

                $('#myModal').modal('show');
                riskStaticsReport.getMediaDom();
            });
        },
        getMediaDom: function () {
            var html = '',
                src = mediaUrl[mediaIndex];

            if (mediaType == 2) {//视频
                if (window.navigator.userAgent.toLowerCase().indexOf('ie') >= 0) {
                    html += '<embed id="videoDom" src="' + src + '" autostart=false style="height:100%;"></embed>';
                } else {
                    html = '<video id="videoDom" src="' + src + '" controls="controls" controlsList="nodownload" style="height:100%;"></video>'
                }
            } else if (mediaType == 0) {//图片
                html = '<img id="imgDom" src="' + src + '" style="height:100%"/>';
            }

            $('#media').html(html);
            riskStaticsReport.getMediaMsg(true);
        },
        getMediaMsg: function (flag) {
            if (flag) {
                $('#myModalLabel').text(title[0]);
            }
            $('#current').text(mediaIndex + 1);
            $('#count').text(mediaUrl.length);
        },
        mediaChange: function () {
            var id = $(this).attr('id');
            var len = mediaUrl.length == 0 ? 0 : mediaUrl.length - 1;

            if (id == 'arrowsLeft') {
                if (mediaIndex == 0) {
                    mediaIndex = 0;
                    layer.msg('已经到头了');
                    return;
                }
                mediaIndex--;
            } else if (id == 'arrowsRight') {
                if (mediaIndex == len) {
                    mediaIndex = len;
                    layer.msg('已经到头了');
                    return;
                }
                mediaIndex++;
            }
            $('#myModalLabel').html(title[mediaIndex]);
            if (mediaType == 2) {
                $('#videoDom').attr('src', mediaUrl[mediaIndex]);
            } else if (mediaType == 0) {
                $('#imgDom').attr('src', mediaUrl[mediaIndex]);
            }
            riskStaticsReport.getMediaMsg();
        },
        exportDetail: function (e) {
            if (!riskStaticsReport.validates()) {
                return;
            }
            ;
            var timeInterval = $('#timeInterval').val().split('--');
            startTime = timeInterval[0];
            endTime = timeInterval[1];
            var url = "/clbs/r/riskManagement/statisticsReport/exportReportInfo";
            var parameter = {
                "vehicleId": detailVehicleId,
                "commonField": commonField,
                "startTime": startTime + ' 00:00:00',
                "endTime": endTime + ' 23:59:59',
            };
            exportExcelUseForm(url, parameter);
        },
        getTable: function (table) {
            //显示隐藏列
            var columnDefs = [{
                "searchable": true,
                "orderable": false,
                "targets": [0, 1, 2, 3]
            }];
            var columnKeys = [
                'brand', 'plateColor', 'orgName', 'fatigueDriving', 'phone',
                'eyeClose', 'yawning', 'smoke', 'changeLanes', 'vehicleOffset', 'leftOffset',
                'rightOffset', 'vehicleCrash', 'distance', 'pedestrianCollisions', 'distractedDriving',
                'driverException', 'abnormalPosture', 'obstacles', 'networkSpeed', 'roadMarkTransfinite',
                'inConformityCertificate', 'timeoutDriving', 'abormalLoad', 'overMan', 'infraredBlocking',
                'keepOut', 'noDriverDetected', 'offWheel', 'turn', 'accelerate',
                'slowDown', 'quickCrossing', 'notWearingSeatBelt', 'leftBlindAlert', 'rightBlindAlert',
                'rightRearApproach', 'leftRearApproach', 'closeBehind', 'imbalanceTirePressure', 'slowLeak',
                'highTirePressure', 'lowTirePressure', 'highTireTemperature', 'neutralTaxiing', 'engineOverdrive',
                'idleSpeed', 'abnormalFlameOut', 'lowBattery', 'sensorAnomaly', 'assistFailure',
                'driverBehaviorMonitorFailure', 'peripheralStateException', 'blindSpotMonitoring',
                'overSpeed', 'lineOffset', 'forbid', 'equipmentAbnormal', 'leftOffsetWarning', 'rightOffsetWarning',
                'other', 'total'
            ]
            var columns = [{
                "data": null,
                "class": "text-center",
            }];
            for (var i = 0; i < columnKeys.length; i++) {
                columns.push({
                    "data": columnKeys[i],
                    "class": "text-center"
                })
            }
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.vehicleIds = vehicleId;
                d.monitorName = simpleQueryParam;
                var timeInterval = $('#timeInterval').val().split('--');
                startTime = timeInterval[0];
                endTime = timeInterval[1];
                d.startTime = startTime.replace(/-/g, '');
                d.endTime = endTime.replace(/-/g, '');
            };
            //表格setting
            var setting = {
                listUrl: "/clbs/r/riskManagement/statisticsReport/list",
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                lengthMenu: [5, 10, 20, 50, 100],
                pageable: true, //是否分页
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                drawCallbackFun: function () {
                    var api = myTable.dataTable;
                    var startIndex = api.context[0]._iDisplayStart;//获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                    });
                },
                ajaxCallBack: function (data) {
                    if (data.success) {
                        tableData = data.records;
                    }
                }
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
            $('.toggle-vis').off().on('change', function (e) {
                var visible = myTable.dataTable.column($(this).attr('data-column')).visible();
                if (visible) {
                    myTable.dataTable.column($(this).attr('data-column')).visible(false);
                } else {
                    myTable.dataTable.column($(this).attr('data-column')).visible(true);
                }
                $(".keep-open").addClass("open");
            });
            $("#search_button").on("click", function () {
                var tsval = $("#simpleQueryParam").val()
                // myTable.dataTable.column(1).search(tsval, false, false).draw();
                myTable.dataTable.filter(function (v) {
                    console.log(arguments)
                })
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
            simpleQueryParam = '';
            searchFlag = 1;
            riskStaticsReport.inquireClick(1, false);
        },
        getYesterDay: function () {
            var nowDate = new Date();
            var date = new Date(nowDate.getTime() - 24 * 60 * 60 * 1000);
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
            return yesterdate;
        },
        closeDetail: function () {
            $('#dataTable tbody tr').removeClass('active');
            activeRowIndex = null;
            $('#detail').removeClass('active');
            $('body').removeClass('drawer-open');
            $("#header-content").css({'padding-right': "0"});
        },
    }

    $(function () {
        //初始化页面
        riskStaticsReport.init();
        $('input').inputClear();
        // $('#timeInterval').dateRangePicker({dateLimit:30});
        $('#timeInterval').dateRangePicker({
            dateLimit: 31,
            isOffLineReportFlag: true,
            nowDate: riskStaticsReport.getYesterDay(),
            isShowHMS: false
        });
        riskStaticsReport.tableFilter();
        riskStaticsReport.getTable('#dataTable');
        //当前时间
        riskStaticsReport.getsTheCurrentTime();
        //组织下拉显示
        $("#groupSelect").bind("click", showMenuContent);
        //导出
        $("#exportAlarm").bind("click", riskStaticsReport.exportAlarm);
        $("#detailExport").bind("click", riskStaticsReport.exportDetail);
        $("#refreshTable").bind("click", riskStaticsReport.refreshTable);
        $('#dataTableContainer').on('click', 'td', riskStaticsReport.showDetail);
        $('#detailTableContainer').on('scroll', riskStaticsReport.detailTableScroll);
        $(document).on('click', riskStaticsReport.closeDetail);
        $('#detailMask').on('click', riskStaticsReport.closeDetail);
        $('#detail').on('click', function (event) {
            event.stopPropagation();
        });
        $('#showMedia .arrows').on('click', riskStaticsReport.mediaChange);
        $('#myModal').on('click', function (event) {
            event.stopPropagation();
        });

        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var param = $("#groupSelect").val();
                riskStaticsReport.searchVehicleTree(param);
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
                    riskStaticsReport.searchVehicleTree(param);
                }
                searchFlag = true;
            }, 500);
        });
        $('#queryType').on('change', function () {
            var param = $("#groupSelect").val();
            riskStaticsReport.searchVehicleTree(param);
        });
    })
}(window, $))