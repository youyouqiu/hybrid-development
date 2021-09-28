(function (window, $) {
    var vehicleId = '';
    var startTime;
    var endTime;
    // 组织树
    var zTreeIdJson = {};
    var checkFlag = false;
    var size;
    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询
    var ifAllCheck = true; //刚进入页面小于TREE_MAX_CHILDREN_LENGTH自动勾选
    var isSearch = true;
    var simpleQueryParam = '';

    itineraryReportList = {
        init: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            // menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(1) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 0; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            ;
            $("#Ul-menu-text").html(menu_text);

            itineraryReportList.initTree();
            itineraryReportList.getObdTripDataList();
        },
        //组织树
        initTree: function () {
            var setting = {
                async: {
                    url: itineraryReportList.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: itineraryReportList.ajaxDataFilter
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
                    beforeClick: itineraryReportList.beforeClickVehicle,
                    onCheck: itineraryReportList.onCheckVehicle,
                    beforeCheck: itineraryReportList.zTreeBeforeCheck,
                    onExpand: itineraryReportList.zTreeOnExpand,
                    //beforeAsync: itineraryReportList.zTreeBeforeAsync,
                    onAsyncSuccess: itineraryReportList.zTreeOnAsyncSuccess,
                    onNodeCreated: itineraryReportList.zTreeOnNodeCreated
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },
        searchVehicleTree: function (param) {
            ifAllCheck = false;//模糊查询不自动勾选

            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                itineraryReportList.init();
            } else {
                bflag = true;
                var querySetting = {
                    async: {
                        url: "/clbs/a/search/reportFuzzySearch",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"type": $('#queryType').val(), "queryParam": param, "queryType":"multiple"},
                        dataFilter: itineraryReportList.ajaxQueryDataFilter
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
                        beforeClick: itineraryReportList.beforeClickVehicle,
                        onCheck: itineraryReportList.onCheckVehicle,
                        onExpand: itineraryReportList.zTreeOnExpand,
                        onNodeCreated: itineraryReportList.zTreeOnNodeCreated,
                        // beforeCheck: itineraryReportList.zTreeBeforeCheck,
                    }
                };
                $.fn.zTree.init($("#treeDemo"), querySetting, null);
            }
        },
        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData));
            var nodesArr;
            if($('#queryType').val() == "vehicle"){
                nodesArr = filterQueryResult(responseData, crrentSubV);
            }else {
                nodesArr = responseData;
            }
            for (var i = 0; i < nodesArr.length; i++) {
                nodesArr[i].open = true;
            }
            return nodesArr;
        },
        getTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
                // return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=allMonitor";
            }
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (size <= TREE_MAX_CHILDREN_LENGTH && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            itineraryReportList.getCharSelect(treeObj);

            bflag = false;
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
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
                        nodes = zTree.getCheckedNodes(true),
                        v = "";
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
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
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
                if (nodesLength > TREE_MAX_CHILDREN_LENGTH) {
                    layer.msg('最多勾选'+TREE_MAX_CHILDREN_LENGTH+'个监控对象');
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
            //itineraryReportList.getCharSelect(treeObj);
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
        onCheckVehicle: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                isSearch = false;
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    itineraryReportList.getCheckedNodes();
                    itineraryReportList.validates();
                }, 600);
            }
            crrentSubV = [];
            crrentSubV.push(treeNode.id);
            itineraryReportList.getCharSelect(zTree);
            itineraryReportList.getCheckedNodes();
        },
        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
                nodes = zTree.getCheckedNodes(true),
                vid = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == "vehicle" || nodes[i].type == "people" || nodes[i].type == "thing") {
                    vid += nodes[i].id + ",";
                }
            }
            vehicleId = vid;
        },
        //时间
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
                tMonth = itineraryReportList.doHandleMonth(tMonth + 1);
                tDate = itineraryReportList.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " "
                    + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24
                    * parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = itineraryReportList.doHandleMonth(endMonth + 1);
                endDate = itineraryReportList.doHandleMonth(endDate);
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
                vMonth = itineraryReportList.doHandleMonth(vMonth + 1);
                vDate = itineraryReportList.doHandleMonth(vDate);
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
                    vendMonth = itineraryReportList.doHandleMonth(vendMonth + 1);
                    vendDate = itineraryReportList.doHandleMonth(vendDate);
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
        validates: function () {
            return $("#oilist").validate({
                rules: {
                    groupSelect: {
                        zTreeChecked: "treeDemo"
                    },
                    groupId: {
                        required: true
                    },
                    endTime: {
                        required: true,
                        compareDate: "#timeInterval",
                        compareDateDiff: "#timeInterval"
                    },
                    startTime: {
                        required: true
                    },
                    charSelect: {
                        required: true
                    }
                },
                messages: {
                    groupSelect: {
                        zTreeChecked: vehicleSelectBrand
                    },
                    groupId: {
                        required: "不能为空"
                    },
                    endTime: {
                        required: "请选择结束日期！",
                        compareDate: "结束日期必须大于开始日期!",
                        compareDateDiff: "查询的日期必须小于一周"
                    },
                    startTime: {
                        required: "请选择开始日期！",
                    },
                    charSelect: {
                        required: "监控对象不能为空"
                    }
                }
            }).form();
        },
        //查询
        inquireClick: function (number, isFirst) {
            if (number == 0) {
                itineraryReportList.getsTheCurrentTime();
            } else if (number == -1) {
                itineraryReportList.startDay(-1)
            } else if (number == -3) {
                itineraryReportList.startDay(-3)
            } else if (number == -7) {
                itineraryReportList.startDay(-7)
            }
            if (number != 1) {
                $('#timeInterval').val(startTime + '--' + endTime);
                startTime = startTime;
                endTime = endTime;
            } else {
                var timeInterval = $('#timeInterval').val().split('--');
                startTime = timeInterval[0];
                endTime = timeInterval[1];
            }
            if (startTime > endTime) {
                layer.msg('结束日期必须大于开始日期', {move: false});
                return;
            }
            if (!isFirst) {
                itineraryReportList.getCheckedNodes();
            }
            if (!itineraryReportList.validates()) return;
            // if (vehicleId == "") {
            //     layer.msg(vehicleSelectBrand);
            //     return;
            // }
            $('#simpleQueryParam').val('');
            simpleQueryParam = '';
            itineraryReportList.getObdTripDataList();
        },
        //OBD行程列表
        getObdTripDataList: function () {
            var ajaxDataParam = {
                "vehicleIds": vehicleId,
                "startTime": startTime,
                "endTime": endTime,
            };
            var url = "/clbs/v/statistic/obdTripReport/getObdTripDataList";
            json_ajax("POST", url, "json", true, ajaxDataParam, function (data) {
                if (data.success) {
                    itineraryReportList.searchInitTabData();
                } else {
                    layer.msg(data.msg);
                }
            });
        },
        searchInitTabData: function () {
            var columnDefs = [{
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];
            var columns = [{
                "data": null,
                "class": "text-center"
            }, {
                "data": "plateNumber",
                "class": "text-center"
            }, {
                "data": "groupName",
                "class": "text-center"
            }, {
                "data": "assignmentName",
                "class": "text-center"
            }, {
                "data": "tripStartTime",
                "class": "text-center"
            }, {
                "data": "tripEndTime",
                "class": "text-center"
            }, {
                "data": "tripDuration",
                "class": "text-center"
            }, {
                "data": "drivingDuration",
                "class": "text-center"
            }, {
                "data": "drivingMileage",
                "class": "text-center"
            }, {
                "data": "idlingNumber",
                "class": "text-center"
            }, {
                "data": "idlingDuration",
                "class": "text-center"
            }, {
                "data": "totalOilConsumption",
                "class": "text-center"
            }, {
                "data": "drivingOilConsumption",
                "class": "text-center"
            }, {
                "data": "driving100KmOilConsumption",
                "class": "text-center"
            }, {
                "data": "idlingOilConsumption",
                "class": "text-center"
            }, {
                "data": "idlingHourOilConsumption",
                "class": "text-center"
            }, {
                "data": "rapidAccelerationNumber",
                "class": "text-center"
            }, {
                "data": "rapidDecelerationNumber",
                "class": "text-center",
            }, {
                "data": "sharpTurnNumber",
                "class": "text-center",
            }, {
                "data": "brakeNumber",
                "class": "text-center",
            }, {
                "data": "clutchNumber",
                "class": "text-center",
            }];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = simpleQueryParam;
            };
            //表格setting
            var setting = {
                listUrl: "/clbs/v/statistic/obdTripReport/getObdTripDataPageList",
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                pageable: true, //是否分页
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
        },
        //刷新
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            simpleQueryParam = '';
            myTable.requestData();
        },
        //搜索
        searchTable: function () {
            simpleQueryParam = $("#simpleQueryParam").val();
            myTable.requestData();
        },
        //导出
        export: function () {
            if ($("#dataTable tbody tr td").hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据，无法导出");
                return;
            }
            if(getRecordsNum() > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var url = "/clbs/v/statistic/obdTripReport/exportObdTripDataList?simpleQueryParam=" + simpleQueryParam;
            window.location.href = url
        },
    }
    $(function () {
        $('input').inputClear();
        itineraryReportList.init();
        //时间
        itineraryReportList.getsTheCurrentTime();
        $('#timeInterval').dateRangePicker({
            dateLimit: 31
        });
        //组织树
        $("#groupSelect").bind("click", showMenuContent);
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var param = $("#groupSelect").val();
                itineraryReportList.searchVehicleTree(param);
            }
        });
        var inputChange;
        $("#groupSelect").on('input propertychange', function (value) {
            if (inputChange !== undefined) {
                clearTimeout(inputChange);
            }
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                isSearch = true;
            }
            inputChange = setTimeout(function () {
                if (isSearch) {
                    var param = $("#groupSelect").val();
                    itineraryReportList.searchVehicleTree(param);
                }
                isSearch = true;
            }, 500);
        });
        //刷新
        $("#refreshTable").bind("click", itineraryReportList.refreshTable);
        $('#queryType').on('change', function (){
            var param=$("#groupSelect").val();
            itineraryReportList.searchVehicleTree(param);
        });
    })
}(window, $))