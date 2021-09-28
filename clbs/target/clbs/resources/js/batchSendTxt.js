//# sourceURL=batchSendTxt.js

(function (window, $) {
    var size;//当前权限监控对象数量
    var searchTimeout; // 模糊查询定时器
    var vehicleIds;// 选中的车辆ID
    var vehicles = null;
    var headers = {"UserName": ""};
    var myTable;
    var tableRefreshTask;
    var alasendflag = true;
    window.batchSendTxt = {
        init: function () {
            //菜单隐藏
            $('#rMenu').css({"visibility": "hidden"});
            //模态框添加类样式
            $(".modal-body").addClass("modal-body-overflow");
            $(".modal-dialog").css("width", "60%");
            var url = '/clbs/m/basicinfo/monitoring/vehicle/treeStateInfos';
            var otherParam = {'webType': 1, 'queryType': 'monitor', 'type': 1, 'devType': $('#deviceTypeVal').val()};
            batchSendTxt.initTree(url, otherParam);
            $('#batchCondition').on('input propertychange', batchSendTxt.ztreeSearch);// 模糊查询
        },

        ztreeSearch: function () {
            if (searchTimeout !== undefined) {
                clearTimeout(searchTimeout);
            }
            searchTimeout = setTimeout(function () {
                batchSendTxt.search_condition();
            }, 500);
        },
        search_condition: function (event) {
            var value = $("#batchCondition").val();
            var url = '/clbs/m/basicinfo/monitoring/vehicle/treeStateInfos';
            var otherParam = {'webType': 1, 'queryType': 'monitor', 'type': 1, 'devType': $('#deviceTypeVal').val()};
            if (value != '') {
                otherParam.queryParam = value;
            }
            batchSendTxt.initTree(url, otherParam);
        },
        initTree: function (url, otherParam) {
            var setting = {
                async: {
                    url: url,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: otherParam,
                    dataFilter: batchSendTxt.ajaxDataFilter
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
                    onAsyncSuccess: batchSendTxt.zTreeOnAsyncSuccess,
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            var data = filterQueryResult(responseData, []);
            for (var i = 0; i < data.length; i++) {
                data[i].open = true;
                if (data[i].type == 'vehicle' || data[i].type == 'people' || data[i].type == 'thing') {
                    data[i].isParent = false;
                }
                if (data[i].id == currentRightClickVehicleId) {
                    data[i].checked = true;
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
            if (size <= 5000) {
                treeObj.checkAllNodes(true);
            }
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            // return true;
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;

                    json_ajax("post", "/clbs/m/personalized/ico/getCheckedVehicle",
                        "json", false, {"parentId": treeNode.id, "type": treeNode.type}, function (data) {
                            if (data.success) {
                                nodesLength += data.obj;
                            } else if (data.msg) {
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
                        if (nodes[i].type == "people" || nodes[i].type == "vehicle" || treeNode.type == "thing") {
                            if ($.inArray(nodeId, ns) == -1) {
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength = ns.length + 1;
                }
                if (nodesLength > 5000) {
                    layer.msg(maxSelectItem);
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
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
            }
            batchSendTxt.getCheckedNodes();
        },
        zTreeOnExpand: function (event, treeId, treeNode) {
            return
            //判断是否是勾选操作展开的树(是则继续执行，不是则返回)
            if (treeNode.type == "group" && !checkFlag) {
                return;
            }
            //初始化勾选操作判断表示
            checkFlag = false;
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (treeNode.type == "group") {
                var assign = []; // 当前组织及下级组织的所有分组
                batchSendTxt.getGroupChild(treeNode, assign);
                if (assign != null && assign.length > 0) {
                    for (var i = 0; i < assign.length; i++) {
                        var node = assign[i];
                        if (node.type == "assignment" && node.children === undefined) {
                            if (!node.zAsync) { // 判断节点是否进行异步加载，若没有，则先异步加载，避免添加重复节点
                                treeObj.reAsyncChildNodes(node, "refresh");
                            }
                        }
                    }
                }
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
                        batchSendTxt.getGroupChild(node.children, assign);
                    }
                }
            }
        },
        unique: function (array) {
            var n = {}, r = [], len = array.length, val, type;
            for (var i = 0; i < array.length; i++) {
                val = array[i];
                type = typeof val;
                if (!n[val]) {
                    n[val] = [type];
                    r.push(val);
                } else if (n[val].indexOf(type) < 0) {
                    n[val].push(type);
                    r.push(val);
                }
            }
            return r;
        },
        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
                nodes = zTree.getCheckedNodes(true), vid = [];
            vehicles = {}
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == "vehicle" || nodes[i].type == "people" || nodes[i].type == "thing") {
                    vid.push(nodes[i].id);
                    vehicles[nodes[i].id] = nodes[i];
                }
            }
            vehicleIds = batchSendTxt.unique(vid).join(',');
            return vehicleIds;
        },
        // socket连接
        sendSocket: function (subInfo, list) {
            batchSendTxt.getListData();
            if (alasendflag) {
                // webSocket.subscribe(headers, '/topic/fencestatus', function (data) {
                webSocket.subscribe(headers, "/user/topic/send_txt", function (data) {
                    console.log('连接', data);
                    if (tableRefreshTask != null && tableRefreshTask !== undefined) {
                        clearTimeout(tableRefreshTask);
                    }
                    tableRefreshTask = setTimeout(function () {
                        batchSendTxt.getListData();
                    }, 2000);
                }, "", null);
                alasendflag = false;
            }
        },
        // 获取状态列表
        getListData: function () {
            var url = '/clbs/v/monitoring/getSendTextStatusList';
            var param = {
                "vehicleIds": vehicleIds,
            };
            $('#sendBatchButton').attr("disabled", " disabled");
            json_ajax("POST", url, "json", true, param, function (data) {
                $('#batchSendTbody').html('');
                if (data.success) {
                    var result = data.obj;
                    var allList = [];
                    for (var i = 0; i < result.length; i++) {
                        var item = result[i];
                        var list = [
                            item.plateNumber,
                            batchSendTxt.getStatus(item.status)
                        ];
                        allList.push(list);
                    }
                    batchSendTxt.reloadData(allList);
                }
                $('#sendBatchButton').removeAttr("disabled");
            });
        },
        reloadData: function (dataList) {
            var currentPage = myTable.page();
            myTable.clear();
            myTable.rows.add(dataList);
            myTable.page(currentPage).draw(false);
            myTable.search('', false, false).page(currentPage).draw();
        },
        // 下发状态转义
        getStatus: function (info) {
            var data = '';
            switch (info) {
                case 0:
                    data = '参数已生效';
                    break;
                case 1:
                    data = '参数未生效';
                    break;
                case 2:
                    data = '参数消息有误';
                    break;
                case 3:
                    data = '参数不支持';
                    break;
                case 4:
                    data = '参数下发中';
                    break;
                case 5:
                    data = '终端离线，未下发';
                    break;
                case 7:
                    data = '终端处理中';
                    break;
                case 8:
                    data = '终端接收失败';
                    break;
                default:
                    data = '';
                    break;
            }
            return data;
        },
        //批量文本下发
        sendBatchButton: function () {
            var url = '/clbs/v/monitoring/sendTextByBatch';
            var marks = batchSendTxt.getMarks();
            if (!batchSendTxt.txtSendValidate()) return;
            batchSendTxt.getCheckedNodes();
            console.log('监控对象', vehicleIds);
            if (vehicleIds === '' || vehicleIds == null || vehicleIds === undefined) {
                layer.msg('请勾选监控对象');
                return;
            }
            /*else {
                var vehicleIdArr = vehicleIds.split(",");
                if (vehicleIdArr > TREE_MAX_CHILDREN_LENGTH) {
                    layer.msg('最多勾选' + TREE_MAX_CHILDREN_LENGTH + '个监控对象');
                    return;
                }
            }*/
            var deviceType = $('#deviceTypeVal').val();
            var param = {
                "deviceType": deviceType,
                "sendTextContent": $("#sendTextContent").val(),
                "vehicleIds": vehicleIds,
                "marks": marks
            };
            var textType = '';
            $("#textType input[name='textType']").each(function () {
                if ($(this).is(":checked")) {
                    textType = $(this).attr('value');
                }
            });
            var messageTypeOne = $('#messageTypeOne option:selected').attr('value');
            var messageTypeTwo = $('#messageTypeTwo option:selected').attr('value');
            if (deviceType == '11' || deviceType == "20" || deviceType == "21" || deviceType == "24" || deviceType == "25" || deviceType == "26" || deviceType == "28") {// 协议类型为T808-2019
                url = '/clbs/v/monitoring/sendTextByBatch2019';
                var terminalDisplay = $('#terminalDisplay').is(':checked');
                var terminalTtsPlay = $('#terminalTtsPlay').is(':checked');
                param = {
                    "deviceType": deviceType,
                    "sendTextContent": $("#sendTextContent").val(),
                    "vehicleIds": vehicleIds,
                    "terminalDisplay": terminalDisplay ? 1 : 0,
                    "terminalTtsPlay": terminalTtsPlay ? 1 : 0,
                    "textType": textType,
                    "messageTypeOne": messageTypeOne,
                    "messageTypeTwo": messageTypeTwo,
                };
            }

            $('#sendBatchButton').attr("disabled", " disabled");
            json_ajax("POST", url, "json", true, param, function (data) {
                if (data.success) {
                    batchSendTxt.sendSocket();
                    layer.msg(sendCommandComplete, {time: 2000}, function (refresh) {
                        layer.close(refresh);
                    });
                    setTimeout("realTimeVideLoad.logFindCilck()", 500);
                } else if (data.msg) {
                    layer.msg(data.msg, {time: 2000}, function (refresh) {
                        layer.close(refresh);
                    });
                }
                $('#sendBatchButton').removeAttr("disabled");
            });
        },
        // 下发文本验证
        txtSendValidate: function () {
            return $("#txtSend").validate({
                rules: {
                    sendTextContent: {
                        required: true,
                        maxlength: 512
                    }
                },
                messages: {
                    sendTextContent: {
                        required: textNull,
                        maxlength: '最多输入512个字符'
                    }
                }
            }).form();
        },
        // 获取复选框勾选数据
        getMarks: function () {
            var arr = [];
            $("#batchForm input[name='marks']").each(function () {
                if ($(this).is(":checked")) {
                    arr.push($(this).val())
                }
            });
            return arr.join(',');
        },
        // 表格初始化
        getTable: function (table) {
            myTable = $(table).DataTable({
                "destroy": true,
                "dom": 'tiprl',// 自定义显示项
                "lengthChange": true,// 是否允许用户自定义显示数量
                "bPaginate": true, // 翻页功能
                "bFilter": false, // 列筛序功能
                "searching": true,// 本地搜索
                "ordering": false, // 排序功能
                "Info": true,// 页脚信息
                "autoWidth": true,// 自动宽度
                "stripeClasses": [],
                "lengthMenu": '',
                "pagingType": "simple_numbers", // 分页样式
                "dom": "t" + "<'row'<'col-xs-12 hasMargin'p>>",
                "oLanguage": {// 国际语言转化
                    "oAria": {
                        "sSortAscending": " - click/return to sort ascending",
                        "sSortDescending": " - click/return to sort descending"
                    },
                    "sLengthMenu": "显示 _MENU_ 记录",
                    "sInfo": "当前显示 _START_ 到 _END_ 条，共 _TOTAL_ 条记录。",
                    "sZeroRecords": "无数据",
                    "sEmptyTable": "无数据",
                    "sLoadingRecords": "正在加载数据-请等待...",
                    "sInfoEmpty": "",
                    "sInfoFiltered": "（数据库中共为 _MAX_ 条记录）",
                    "sProcessing": "<img src='../resources/user_share/row_details/select2-spinner.gif'/> 正在加载数据...",
                    "sSearch": "模糊查询：",
                    "sUrl": "",
                    "oPaginate": {
                        "sFirst": "",
                        "sPrevious": " 上一页 ",
                        "sNext": " 下一页 ",
                        "sLast": ""
                    }
                },
                "columnDefs": [{
                    "targets": [0],
                    "searchable": false
                }],
                "order": [
                    [0, null]
                ],// 第一列排序图标改为默认

            });
        },
    }

    $(function () {
        $('input').inputClear();
        batchSendTxt.getTable('#broadDataTable');
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'batchCondition') {
                batchSendTxt.ztreeSearch();
            }
        });
        batchSendTxt.init();
        $('#sendBatchButton').on('click', batchSendTxt.sendBatchButton);
        $('#sendModalClose').on('click', batchSendTxt.close);
    })
}(window, $));