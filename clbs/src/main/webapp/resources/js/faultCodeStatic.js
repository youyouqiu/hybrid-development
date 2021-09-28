(function (window, $) {
    //时间
    var startTime;
    var endTime;
    //组织树
    var vehicleId = "";
    var checkFlag = false;
    var size;
    var ifAllCheck = true, maxSize = 5000;
    var zTreeIdJson = {};
    var bflag = true;
    var crrentSubV = [];
    var searchFlag = true;
    var inputFlag = false;// 组织树输入框是否输入过标识(用于解决IE浏览器组织树重复加载,导致的闪烁问题)

    var menu_text = "";
    var table = $("#dataTable tr th:gt(0)");

    faultCodeStatic = {
        init: function () {
            //显示隐藏列
            for (var i = 0; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            ;
            $("#Ul-menu-text").html(menu_text);
            faultCodeStatic.initTree();
            faultCodeStatic.getTable();
        },
        //组织树
        initTree: function () {
            //车辆树
            var setting = {
                async: {
                    url: faultCodeStatic.getRiskDisposeTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: faultCodeStatic.ajaxDataFilter
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
                    beforeClick: faultCodeStatic.beforeClickVehicle,
                    onAsyncSuccess: faultCodeStatic.zTreeOnAsyncSuccess,
                    beforeCheck: faultCodeStatic.zTreeBeforeCheck,
                    onCheck: faultCodeStatic.onCheckVehicle,
                    onNodeCreated: faultCodeStatic.zTreeOnNodeCreated,
                    onExpand: faultCodeStatic.zTreeOnExpand
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },
        getRiskDisposeTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/functionconfig/fence/bindfence/alarmSearchTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=monitor";
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
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (size <= maxSize && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            faultCodeStatic.getCharSelect(treeObj);
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
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
                if (nodesLength > maxSize) {
                    // layer.msg(maxSelectItem);
                    layer.msg('最多勾选' + maxSize + '个监控对象');
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
            }

            crrentSubV = [];
            crrentSubV.push(treeNode.id);
            faultCodeStatic.getCharSelect(zTree);
            faultCodeStatic.getCheckedNodes();
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
                    "monitorType": "vehicle"
                }, function (data) {
                    var result = data.obj;
                    var parentNode;
                    if (result != null && result != undefined) {
                        $.each(result, function (i) {
                            var pid = i; //获取键值
                            var chNodes = result[i];//获取对应的value
                            if (zTreeIdJson[pid] != undefined) {
                                var parentTid = zTreeIdJson[pid][0];
                                parentNode = treeObj.getNodeByTId(parentTid);
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
                // $("#groupSelect").siblings('.myHolder').hide();
            } else {
                $("#groupSelect").val("");
                // $("#groupSelect").siblings('.myHolder').show();
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
                        faultCodeStatic.getGroupChild(node.children, assign);
                    }
                }
            }
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
        //组织树监控对象模糊搜索
        searchVehicleTree: function (param) {
            ifAllCheck = false;//模糊查询不自动勾选

            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                if (inputFlag) {
                    inputFlag = false;
                    faultCodeStatic.initTree();
                }
            } else {
                bflag = true;
                inputFlag = true;
                var querySetting = {
                    async: {
                        url: "/clbs/m/functionconfig/fence/bindfence/vehicleTreeFuzzy",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"type": "multiple", "queryParam": param, "queryType": "monitor"},
                        dataFilter: faultCodeStatic.ajaxQueryDataFilter
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
                        beforeClick: faultCodeStatic.beforeClickVehicle,
                        onCheck: faultCodeStatic.onCheckVehicle,
                        onExpand: faultCodeStatic.zTreeOnExpand,
                        onNodeCreated: faultCodeStatic.zTreeOnNodeCreated,
                    }
                };
                $.fn.zTree.init($("#treeDemo"), querySetting, null);
            }
        },
        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData))
            var nodesArr = filterQueryResult(responseData, crrentSubV);
            for (var i = 0; i < nodesArr.length; i++) {
                nodesArr[i].open = true;
            }
            return nodesArr;
        },
        //table
        getTable: function () {
            //表格列定义
            var columnDefs = [{
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];

            var columns = [
                {
                    "data": null,
                    "class": "text-center"
                }, {
                    "data": "monitorNumber",
                    "class": "text-center",
                }, {
                    "data": "assignmentName",
                    "class": "text-center",
                }, {
                    "data": "uploadTime",
                    "class": "text-center",
                }, {
                    "data": "obdName",
                    "class": "text-center",
                }, {
                    "data": "faultCode",
                    "class": "text-center",
                }, {
                    "data": "description",
                    "class": "text-center",
                }];

            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
                d.startDateTime = startTime;
                d.endDateTime = endTime;
                d.monitorIds = vehicleId;
            };

            var setting = {
                listUrl: '/clbs/statistic/faultCodeStatistic/getFaultCodeList',
                enableUrl: '/clbs/c/user/enable_',
                disableUrl: '/clbs/c/user/disable_',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true
            };
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
        },
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            myTable.requestData();
        },
        searchTable: function () {
            if (myTable) {
                myTable.requestData();
            }
        },
        //日期
        doHandleMonth: function (month) {
            var m = month;
            if (month.toString().length == 1) {
                m = "0" + month;
            }
            return m;
        },
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
                tMonth = faultCodeStatic.doHandleMonth(tMonth + 1);
                tDate = faultCodeStatic.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " "
                    + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24
                    * parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = faultCodeStatic.doHandleMonth(endMonth + 1);
                endDate = faultCodeStatic.doHandleMonth(endDate);
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
                vMonth = faultCodeStatic.doHandleMonth(vMonth + 1);
                vDate = faultCodeStatic.doHandleMonth(vDate);
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
                    vendMonth = faultCodeStatic.doHandleMonth(vendMonth + 1);
                    vendDate = faultCodeStatic.doHandleMonth(vendDate);
                    endTime = vendYear + "-" + vendMonth + "-" + vendDate + " "
                        + "23:59:59";
                }
            }
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
        getNowDate: function () {
            var nowDate = new Date();
            var date = new Date(nowDate.getTime());
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
        validates: function () {
            if (vehicleId === '') {
                layer.msg(vehicleSelectBrand);
                return false;
            }
            return true;
        },
        inquireClick: function (number) {
            console.log(number);
            if (number == 0) {
                faultCodeStatic.getsTheCurrentTime();
            } else if (number == -1) {
                faultCodeStatic.startDay(-1)
            } else if (number == -3) {
                faultCodeStatic.startDay(-3)
            } else if (number == -7) {
                faultCodeStatic.startDay(-7)
            }

            if (number != 1) {
                $('#timeInterval').val(startTime + '--' + endTime);
            } else {
                var timeInterval = $('#timeInterval').val().split('--');
                startTime = timeInterval[0];
                endTime = timeInterval[1];
            }
            ;

            faultCodeStatic.getCheckedNodes();
            if (!faultCodeStatic.validates()) {
                return;
            }
            ;
            faultCodeStatic.getTable();
        },
        //导出
        exportAlarm: function () {
            faultCodeStatic.getCheckedNodes("treeDemo");
            if (!faultCodeStatic.validates()) {
                return;
            }

            var url = "/clbs//statistic/faultCodeStatistic/findExportFaultCode";
            var parameter = {
                "monitorIds": vehicleId,
                "startDateTime": startTime,
                "endDateTime": endTime,
                "simpleQueryParam": $('#simpleQueryParam').val()
            };
            json_ajax("POST", url, "json", true, parameter, faultCodeStatic.exportCallback);
        },
        exportCallback: function (data) {
            if (data.success) {
                var url = "/clbs/statistic/faultCodeStatistic/exportFaultCode";
                window.location.href = url;
            } else {
                layer.msg('未查询到数据，无法导出');
            }
        }
    }
    $(function () {
        /*if (!!window.ActiveXObject) {// IE浏览器重写placeholder
            $("#groupSelect").removeAttr('placeholder');
            initPlaceholder('#groupSelect');
        }*/
        faultCodeStatic.init();
        //日期
        $('#timeInterval').dateRangePicker({
            isShowHMS: true,
            nowDate: faultCodeStatic.getNowDate(),
            isOffLineReportFlag: true,
        });

        //导出
        $("#exportAlarm").bind("click", faultCodeStatic.exportAlarm);
        $("#refreshTable").bind("click", faultCodeStatic.refreshTable);

        //组织树
        $("#groupSelect").bind("click", showMenuContent);
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                inputFlag = true;
                var param = $("#groupSelect").val();
                faultCodeStatic.searchVehicleTree(param);
            }
        });
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
                    faultCodeStatic.searchVehicleTree(param);
                }
                searchFlag = true;
            }, 500);
        });
    })
})(window, $)