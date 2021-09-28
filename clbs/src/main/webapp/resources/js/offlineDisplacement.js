(function (window, $) {
    //车辆列表
    var vehicleList = [];
    //车辆id列表
    var vehicleId = [];
    //开始时间
    var startTime;
    //结束时间
    window.myTable = ''
    var firstAsync = true
    var checkFlag = false; //判断组织节点是否是勾选操作
    var size;//当前权限监控对象数量
    var zTreeIdJson = {};
    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询
    var ifAllCheck = true; //刚进入页面小于100自动勾选
    var searchFlag = true;
    var selectedRows = ''
    var records = []
    var geocoder = new AMap.Geocoder({
        radius: 1000,
        extensions: "base"
    });
    offlineDisplacement = {
        init: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            // menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-text").html(menu_text);
            //车辆树
            var setting = {
                async: {
                    url: offlineDisplacement.getParkingReportTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: offlineDisplacement.ajaxDataFilter
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
                    beforeClick: offlineDisplacement.beforeClickVehicle,
                    onAsyncSuccess: offlineDisplacement.zTreeOnAsyncSuccess,
                    beforeCheck: offlineDisplacement.zTreeBeforeCheck,
                    onCheck: offlineDisplacement.onCheckVehicle,
                    onNodeCreated: offlineDisplacement.zTreeOnNodeCreated,
                    onExpand: offlineDisplacement.zTreeOnExpand
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },
        //
        getParkingReportTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle";
            }
        },
        //模糊查询树
        searchVehicleTree: function (param) {
            ifAllCheck = false;//模糊查询不自动勾选
            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                offlineDisplacement.init();
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
                        dataFilter: offlineDisplacement.ajaxQueryDataFilter
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
                        beforeClick: offlineDisplacement.beforeClickVehicle,
                        onCheck: offlineDisplacement.onCheckVehicle,
                        onExpand: offlineDisplacement.zTreeOnExpand,
                        onNodeCreated: offlineDisplacement.zTreeOnNodeCreated,
                        beforeCheck: offlineDisplacement.zTreeBeforeCheck22,
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
        doHandleMonth: function (month) {
            var m = month;
            if (month.toString().length == 1) {
                m = "0" + month;
            }
            return m;
        },
        inquireClick: function (number) {
            if (!offlineDisplacement.validates()) {
                return;
            }
            if (number != 1) {
                offlineDisplacement.setNewDay(number);
            }
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            // menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-text").html(menu_text);
            offlineDisplacement.getCheckedNodes('treeDemo');
            offlineDisplacement.getTable()
            // myTable.refresh()
        },
        // 导出数据
        exportData: function () {
            offlineDisplacement.getCheckedNodes();
            if (!offlineDisplacement.validates()) {
                return;
            }
            if ($("#dataTable tbody tr td").hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据，无法导出");
                return;
            }
            if(getRecordsNum() > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var url = "/clbs/m/reportManagement/offlineDisplacement/export";
            var parameter = {
                monitorIds: offlineDisplacement.getCheckedVehicleIds('treeDemo'), //监控对象id(多个以逗号隔开)
                offlineTime: Number($("#offlineTime").val()) * 60 * 60, //离线时长(单位:秒,默认1800秒)
                moveDistance: Number($("#offsetDistance").val()) * 1000, //移动距离(单位:米, 默认50000米)
                date: new Date($('#timeInterval').val()).Format('yyyyMMdd') //日期(格式:yyyyMMdd)
            };
            json_ajax("POST", url, "json", true, parameter, offlineDisplacement.saveExportData);
        },
        // 保存导出数据
        saveExportData: function (data) {
            if (data.success == true) {
                // $("#exportManager").modal('show')
                $("#downloadBtn").click()
            } else {
                layer.msg('导出失败');
            }
        },
        // 表单验证
        validates: function () {
            return $("#queryParamsForm").validate({
                rules: {
                    queryDateStr: {
                        required: true
                    },
                    groupSelect: {
                        regularChar: true,
                        zTreeChecked: "treeDemo"
                    },
                    offsetDistance: {
                        required: true,
                        min: 50,
                        max: 5000,
                        digits: true,
                    },
                    offlineTime: {
                        required: true,
                        decimal_one: true,
                    },
                },
                messages: {
                    queryDateStr: {
                        required: "请选择日期！",
                    },
                    groupSelect: {
                        zTreeChecked: '请选择监控对象',
                    },
                    offsetDistance: {
                        required: '请输入50-5000之间的整数',
                        min: '请输入50-5000之间的整数',
                        max: '请输入50-5000之间的整数',
                        digits: '请输入50-5000之间的整数',
                    },
                    offlineTime: {
                        required: '请输入0.5-24之间的整数/(一位小数)',
                        decimal_one: '请输入0.5-24之间的整数/(一位小数)'
                    },
                }
            }).form();
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
            if (size <= 100 && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            offlineDisplacement.getCharSelect(treeObj);
            firstAsync ? firstAsync = false : offlineDisplacement.validates();
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
                        if (nodes[i].type == "people" || nodes[i].type == "vehicle") {
                            //查询该节点是否在勾选组织或分组下，若在则不记录，不在则记录
                            var nd = zTree.getNodeByParam("tId", nodes[i].tId, treeNode);
                            if (nd == null && $.inArray(nodeId, ns) == -1) {
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength += ns.length;
                } else if (treeNode.type == "people" || treeNode.type == "vehicle") { //若勾选的为监控对象
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree.getCheckedNodes(true);
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
        zTreeBeforeCheck22: function (treeId, treeNode) {
            var flag = true;
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            var checkedNodesLength = offlineDisplacement.getCheckedVehicleIds("treeDemo").split(',').length
            var toCheckNodesLength = 0
            if (!treeNode.checked) {
                var nodes = treeObj.transformToArray(treeNode);
                nodes.forEach(function (item) {
                    if (item.type == "vehicle") {
                        toCheckNodesLength++
                    }
                })
            }
            if (toCheckNodesLength + checkedNodesLength > TREE_MAX_CHILDREN_LENGTH) {
                layer.msg('最多勾选' + TREE_MAX_CHILDREN_LENGTH + '个监控对象');
                flag = false;
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

            offlineDisplacement.getCheckedNodes();
            offlineDisplacement.getCharSelect();
            offlineDisplacement.validates()
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
        getCharSelect: function (firstRender) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            var nodes = treeObj.getCheckedNodes(true);
            var allNodes = treeObj.getCheckedNodes();
            if (nodes.length > 0) {
                $("#groupSelect").val(allNodes[0].name);
            } else {
                $("#groupSelect").val("");
            }
            // if(!firstRender){
            //     offlineDisplacement.validates()
            // }
        },
        getGroupChild: function (node, assign) { // 递归获取组织及下级组织的分组节点
            var nodes = node.children;
            if (nodes != null && nodes != undefined && nodes.length > 0) {
                for (var i = 0; i < nodes.length; i++) {
                    var node = nodes[i];
                    if (node.type == "assignment") {
                        assign.push(node);
                    } else if (node.type == "group" && node.children != undefined) {
                        offlineDisplacement.getGroupChild(node.children, assign);
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
        // 获取勾选的车辆Ids
        getCheckedVehicleIds: function (treeId) {
            var zTree = $.fn.zTree.getZTreeObj(treeId),
                nodes = zTree.getCheckedNodes(true), vehicleIds = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == "vehicle" && !vehicleIds.includes(nodes[i].id)) {
                    vehicleIds += nodes[i].id + ",";
                }
            }
            return vehicleIds
        },
        // 初始化列表
        getTable: function () {
            $("#fakeInfo").hide()
            //表格列定义
            var columnDefs = [{
                //第一列，用来显示序号
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];
            var columnDefs = [{
                "targets": [0, 1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12],
                "searchable": false
            }]
            var parserDate = function (dateStr) {
                var y = dateStr.slice(0, 4)
                var mo = dateStr.slice(4, 6)
                var d = dateStr.slice(6, 8)
                var h = dateStr.slice(8, 10)
                var m = dateStr.slice(10, 12)
                var s = dateStr.slice(12, 14)
                return y + '-' + mo + '-' + d + ' ' + h + ':' + m + ':' + s
            }
            var columns = [
                {
                    data: null
                },
                {
                    "data": null,
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        var result = '';
                        if (row.monitorProcess) {
                            result += '<input type="checkbox" name="subChk" value="' + row.primaryKey + '" id="' + row.primaryKey + '" disabled="disabled"/>';
                        } else {
                            result += '<input type="checkbox" name="subChk" value="' + row.primaryKey + '" id="' + row.primaryKey + '"/>';
                        }
                        return result;
                    }
                },
                {
                    "data": "monitorName",
                    "class": "text-center",
                    render: function (data) {
                        return data || '--';
                    }
                }, {
                    "data": "startTime", //这里的<startTime>是指离线开始时间，对应列表中的<离线时间>！！！
                    "class": "text-center",
                    render: function (data) {
                        return data ? parserDate(data) : '--';
                    }
                }, {
                    "data": "endTime", //这里的<endTime>是指离线结束时间，对应列表中的<启动时间>！！！
                    "class": "text-center",
                    render: function (data) {
                        return data ? parserDate(data) : '--';
                    }
                },
                {
                    "data": "level",
                    "class": "text-center",
                    render: function (data) {
                        return data || '--';
                    }
                },
                {
                    "data": "dataStatus",
                    "class": "text-center",
                    render: function (data) {
                        return data || '--';
                    }
                }, {
                    "data": "speed",
                    "class": "text-center",
                    render: function (data) {
                        if (data == 0) return 0
                        return data + 'km/h' || '--';
                    }
                }, {
                    "data": "displaceMile",
                    "class": "text-center",
                    render: function (data) {
                        return data + 'km' || '--';
                    }
                },
                {
                    "data": "startAddress",
                    "class": "text-center startLocation",
                    render: function (data, type, row, meta) {
                        if (!row.startAddress) {
                            offlineDisplacement.getAddressByLngLat(row.startLocation.split(','), row, 'startLocation')
                        }
                        return data || '--';
                    }
                }, {
                    "data": "endAddress",
                    "class": "text-center endLocation",
                    render: function (data, type, row, meta) {
                        if (!row.endAddress) {
                            offlineDisplacement.getAddressByLngLat(row.endLocation.split(','), row, 'endLocation')
                        }
                        return data || '--';
                    }
                },
                {
                    "data": "monitorProcess",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (!data) return '<span style="color: #006bfa;cursor: pointer" onclick="offlineDisplacement.showMonitorHandleModal(this)"  data-rowId="' + row.primaryKey + '" >未处理</span>'
                        var str = ['①', '②', '③', '④']
                        var datas = data.split(',')
                        var td = []
                        datas.forEach(function (item1, index) {
                            var index = ['1', '2', '3', '4'].findIndex(function (item2) {
                                return item1 == item2
                            })
                            index != -1 && td.push(str[index])
                        })
                        return td.join(' ')
                    }
                },
                {
                    "data": "remark",
                    "class": "text-center",
                    render: function (data) {
                        if (data) {
                            return data.replace(/</g, "&lt")
                        }
                        return '--';
                    }
                },
            ];
            //ajax参数
            var ajaxDataParamFun = function (params) {
                params.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
                params.monitorIds = offlineDisplacement.getCheckedVehicleIds('treeDemo'); //监控对象id(多个以逗号隔开)
                params.offlineTime = Number($("#offlineTime").val()) * 60 * 60; //离线时长(单位:秒,默认1800秒)
                params.moveDistance = Number($("#offsetDistance").val()) * 1000; //移动距离(单位:米, 默认50000米)
                params.date = new Date($('#timeInterval').val()).Format('yyyyMMdd') //日期(格式:yyyyMMdd)
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/m/reportManagement/offlineDisplacement/list',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                ajaxCallBack: function (data) {
                    records = data.records
                },
                drawCallbackFun: function () {
                    var api = myTable.dataTable;
                    var startIndex = api.context[0]._iDisplayStart;//获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                    });
                },
            };
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
            $("#dataTable_length select").on('change', function () {
                $("#checkAll").prop("checked", false)
            })
            // 全选
            $("#checkAll").on('change', offlineDisplacement.checkAllClickFn)
        },
        reloadData: function (dataList) {
            var currentPage = myTable.page()
            myTable.clear()
            myTable.rows.add(dataList)
            myTable.page(currentPage).draw(false);
        },
        // 地址解析
        getAddressByLngLat: function (lngLat, row, className) {
            if (!lngLat || lngLat == '0.0.0.0') return
            geocoder.getAddress(lngLat, function (status, result) {
                if (status === 'complete' && result.regeocode) {
                    var address = result.regeocode.formattedAddress;
                    if (className == 'endLocation') {
                        $($("#" + row.primaryKey).parent().nextAll()[8]).html(address)
                    } else {
                        $($("#" + row.primaryKey).parent().nextAll()[7]).html(address)
                    }
                } else {
                    console.log('根据经纬度查询地址失败')
                }
            });
        },
        //刷新列表
        refreshTable: function () {
            $("#simpleQueryParam").val('')
            offlineDisplacement.inquireClick(1, false);
        },
        // 设置时间
        setNewDay: function (day) {
            var timeInterval = $('#timeInterval').val();
            var startTimeIndex = timeInterval.replace(/-/g, "/");
            var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
            var dateList = new Date();
            dateList.setTime(vtoday_milliseconds);
            var vYear = dateList.getFullYear();
            var vMonth = dateList.getMonth();
            var vDate = dateList.getDate();
            vMonth = offlineDisplacement.doHandleMonth(vMonth + 1);
            vDate = offlineDisplacement.doHandleMonth(vDate);
            startTime = vYear + "-" + vMonth + "-" + vDate;

            $('#timeInterval').val(startTime);
        },
        // 得到昨天
        getYesterday: function () {
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
        // 显示弹窗
        showMonitorHandleModal: function (el) {
            if (el) {
                var rowId = $(el).attr('data-rowId')
                var rowData = records.find(function (item) {
                    return rowId == item.primaryKey
                })
                if (rowData) {
                    selectedRows = rowData
                }
            } else {
                var checkedRows = $("input[name='subChk']:checked");
                var checkedNum = checkedRows.length
                if (checkedNum == 0) {
                    layer.msg('不能因为你长的好看，就可以什么都不选吧，怎么也要选一个吧:)')
                    return
                }
                selectedRows = []
                checkedRows.each(function (row) {
                    var rowId = $(checkedRows[row]).val()
                    var rowData = records.find(function (record) {
                        return rowId == record.primaryKey
                    })
                    if (rowData) {
                        selectedRows.push(rowData)
                    }
                })
            }
            $("#monitorHandleModal").modal('show')
        },
        // 发送处理结果
        submitResult: function () {
            var singleUrl = '/clbs/m/reportManagement/offlineDisplacement/deal'
            var batchUrl = '/clbs/m/reportManagement/offlineDisplacement/batchDeal'
            var handleResult = '';
            $("#monitorTable input:checked").each(function () {
                handleResult += $(this).val() + ','
            });
            if (Object.prototype.toString.call(selectedRows) == "[object Object]") { //单个处理
                var params = {
                    monitorId: selectedRows.monitorId, //监控对象id
                    offlineMoveEndTime: selectedRows.endTime, //离线位移结束时间(格式:yyyyMMddHHmmss)
                    handleResult: handleResult, //处理结果(多个以逗号隔开) 1,2,3,4
                    remark: $("#comment").val(), //备注
                }
                json_ajax("POST", singleUrl, "json", false, params, function (res) {
                    if (res.success) {
                        layer.msg('设置成功')
                        myTable.refresh()
                    }
                });
            } else { //批处理
                var primaryKeys = '' // 离线位移主键(监控对象id_离线位移结束时间(格式:yyyyMMddHHmmss) (多个以逗号隔开)
                selectedRows.forEach(function (item) {
                    primaryKeys += item.primaryKey + ','
                })
                var params = {
                    primaryKeys: primaryKeys, //离线位移主键
                    handleResult: handleResult, //处理结果(多个以逗号隔开) 1,2,3,4
                    remark: $("#comment").val(), //备注
                }
                json_ajax("POST", batchUrl, "json", false, params, function (res) {
                    if (res.success) {
                        layer.msg('设置成功')
                        myTable.refresh()
                    }
                });
            }
        },
        // 全选
        checkAllClickFn: function () {
            $("input[name='subChk']:not(:disabled)").prop("checked", this.checked)
        },
    },
        $(function () {
            offlineDisplacement.init();
            $('input').inputClear();
            laydate.render({
                elem: '#timeInterval',
                theme: '#6dcff6',
                max: offlineDisplacement.getYesterday(),
                btns: ['confirm']
            });
            $("#timeInterval").val(offlineDisplacement.getYesterday());
            $("#groupSelect").bind("click", showMenuContent);
            //导出
            $("#exportData").on("click", offlineDisplacement.exportData);
            $("#refreshTable").on("click", offlineDisplacement.refreshTable);

            $('input').inputClear().on('onClearEvent', function (e, data) {
                var id = data.id;
                if (id == 'groupSelect') {
                    offlineDisplacement.searchVehicleTree();
                }
            });

            /**
             * 监控对象树模糊查询
             */
            var inputChange;
            // 树模糊搜索
            $("#groupSelect").on('input propertychange', function (value) {
                if (inputChange !== undefined) {
                    clearTimeout(inputChange);
                }
                ;
                if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                    searchFlag = true;
                }
                inputChange = setTimeout(function () {
                    if (searchFlag) {
                        var param = $("#groupSelect").val();
                        offlineDisplacement.searchVehicleTree(param);
                    }
                    searchFlag = true;
                }, 500);
            });
            // 搜索表格
            $("#search_button").on("click", function () {
                var val = $("#simpleQueryParam").val();
                $("#checkAll").prop("checked", false)
                myTable.dataTable.search(val, false, false).draw()
            });
            // 备注输入验证 100任意字符
            $("#comment").on('input propertychange', function () {
                $(this).val($(this).val().slice(0, 100))
            })
            // 监控对象树模糊查询
            // $("#groupSelect").on('input propertychange', function (value) {
            //     var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            //     treeObj.checkAllNodes(false);
            //     search_ztree('treeDemo', 'groupSelect', 'group');
            // });
            // 弹框提交按钮点击
            $("#submitBtn").on('click', function () {
                if ($("#monitorTable input:checked").length == 0) {
                    layer.msg('请选择处理结果')
                    return
                }
                offlineDisplacement.submitResult()
                $("#monitorHandleModal").modal('hide')
            })
            // 批量处理按钮点击
            $("#handleBatch").on('click', function () {
                offlineDisplacement.showMonitorHandleModal()
            })
            // 模态框隐藏事件
            $("#monitorHandleModal").on('hide.bs.modal', function (e) {
                $("#monitorHandleModal input").attr('checked', false)
                $("#monitorHandleModal textarea").val('已加入月统计名单')
            })
            // 自定义验证 0.5-24 一位小数
            jQuery.validator.addMethod("decimal_one", function (value, element) {
                var valueNumber = Number(value)
                if (!valueNumber) return false
                if (valueNumber > 24) return false
                if (valueNumber < 0.5) return false
                if (value.split('.').length > 1) {
                    return value.split('.')[1] && value.split('.')[1].length == 1
                }
                return true
            })
            $('#queryType').on('change', function () {
                var param = $("#groupSelect").val();
                offlineDisplacement.searchVehicleTree(param);
            });

            $("#exportManager").on('hide.bs.modal', function (e) {
                offlineDisplacement.inquireClick(1, false);
                // //显示隐藏列
                // var menu_text = "";
                // var table = $("#dataTable tr th:gt(0)");
                // for (var i = 1; i < table.length; i++) {
                //     menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
                // }
                // console.log('menu_text',menu_text)
                // $("#Ul-menu-text").html(menu_text);
            })
        })
}(window, $))