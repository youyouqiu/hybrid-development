(function (window, $) {
    var myTable;
    //判断组织节点是否是勾选操作
    var checkFlag = false;
    //当前权限监控对象数量
    var size;
    var zTreeIdJson = {};
    //刚进入页面小于100自动勾选
    var ifAllCheck = true;
    var groupId = [];
    var maxCheckNum = 100;
    var simpleQueryParam = '';
    //用来储存显示数据
    var dataListArray = [];

    // 位置信息
    var stopLoc = [];
    var addressMsg = [];
    var isGetAddressObj = {};
    var dbValue = false //树双击判断参数
    offlineVehicleDisposalRecords = {
        init: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            // menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(1) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 0; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-text").html(menu_text);
            //车辆树
            var setting = {
                async: {
                    url: offlineVehicleDisposalRecords.getParkingReportTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "type": "multiple",
                        "icoType": "0"
                    },
                    dataFilter: offlineVehicleDisposalRecords.ajaxDataFilter
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
                    beforeClick: offlineVehicleDisposalRecords.beforeClickVehicle,
                    onAsyncSuccess: offlineVehicleDisposalRecords.zTreeOnAsyncSuccess,
                    beforeCheck: offlineVehicleDisposalRecords.zTreeBeforeCheck,
                    onCheck: offlineVehicleDisposalRecords.onCheckVehicle,
                    onNodeCreated: offlineVehicleDisposalRecords.zTreeOnNodeCreated,
                    onExpand: offlineVehicleDisposalRecords.zTreeOnExpand,
                    onDblClick: offlineVehicleDisposalRecords.onDblClickVehicle

                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },
        onDblClickVehicle: function (e, treeId, treeNode) {
            if (!treeNode) return
            dbValue = true
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.checkAllNodes(false);
            offlineVehicleDisposalRecords.getCheckedNodes("treeDemo");
            // $("#groupSelect").val(treeNode.name);
            // search_ztree('treeDemo', 'groupSelect', 'group');
            treeObj.checkNode(treeNode, !treeNode.checked, false, true);
        },
        getParkingReportTreeUrl: function (treeId, treeNode) {
            if (treeId == 'treeDemo') {
                return "/clbs/m/basicinfo/enterprise/professionals/tree";
            }
        },
        doHandleMonth: function (month) {
            var m = month;
            if (month.toString().length == 1) {
                m = "0" + month;
            }
            return m;
        },
        exportAlarm: function () {
            if ($("#dataTable tbody tr td").hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据，无法导出");
                return;
            }
            if (getRecordsNum() > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var saveUrl = "/clbs/GeneralCargo/ReportManagement/offLineReport/export?simpleQueryParam=" + simpleQueryParam;
            window.location.href = saveUrl;
        },
        validates: function () {
            return $("#lifeCycleForm").validate({
                rules: {
                    dateNum: {
                        min: 1,
                        digits: true,
                        maxlength: 3,
                    },
                    groupSelect: {
                        zTreeCheckGroup: "treeDemo"
                    }
                },
                messages: {
                    groupSelect: {
                        zTreeCheckGroup: vehicleSelectGroup,
                    },
                    dateNum: {
                        digits: "离线时长必须是正整数哦！",
                        min: "离线时长必须是正整数哦！",
                        maxlength: dateNumError,
                    },
                }
            }).form();
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            if (treeId == "treeDemo") {
                size = responseData.length;
                return responseData;
            }
        },
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.expandAll(true);
            if (size <= maxCheckNum && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            offlineVehicleDisposalRecords.getCharSelect(treeObj);
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            if (!treeNode.checked && !dbValue) {
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                var isCheckNode = treeObj.getCheckedNodes(true);
                var nodes = treeObj.getNodesByFilter(function (node) {
                    return node.type == 'group' && !node.checked;
                }, false, treeNode);
                if (isCheckNode.length + nodes.length + 1 > maxCheckNum) {
                    layer.msg('最多勾选' + maxCheckNum + '个企业' + '<br/>双击名称可选中本组织');
                    return false;
                }
            }
            return true;
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            dbValue = false;
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                zTree.expandNode(treeNode, true, true, false, false); // 展开节点
                setTimeout(() => {
                    offlineVehicleDisposalRecords.getCheckedNodes("treeDemo");
                    offlineVehicleDisposalRecords.validates();
                }, 600);
            }
            offlineVehicleDisposalRecords.getCheckedNodes("treeDemo");
            offlineVehicleDisposalRecords.getCharSelect(zTree);
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
        getCheckedNodes: function (treeId) {
            var zTree = $.fn.zTree.getZTreeObj(treeId),
                nodes = zTree.getCheckedNodes(true),
                groupIds = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                // if (treeId == "treeDemo" && nodes[i].type == "group") {
                groupIds += nodes[i].uuid + ",";
                // }
            }
            groupId = groupIds;
        },
        //对显示的数据进行逆地址解析
        getAddress: function (addressStr) {
            if ($("#dataTable tbody tr td").hasClass("dataTables_empty")) {
                return;
            }
            var $dataTableTbody = $("#dataTable tbody");
            var dataLength = $dataTableTbody.children("tr").length;
            var num = 0;
            for (var i = 0; i < dataLength; i++) {
                num++;
                var addressText = $dataTableTbody.children("tr:nth-child(" + (i + 1) + ")").children("td:nth-child(5)").text();
                if (addressText == '加载中...') {
                    var n = $dataTableTbody.children("tr:nth-child(" + (i + 1) + ")").children("td:nth-child(1)").text();
                    var stopMsg = [];
                    //经纬度正则表达式
                    var Reg = /^(180\.0{4,7}|(\d{1,2}|1([0-7]\d))\.\d{0,20})(,)(90\.0{4,8}|(\d|[1-8]\d)\.\d{0,20})$/;
                    if (addressStr[n - 1] != null && (Reg.test(addressStr[n - 1]) || addressStr[n - 1] === '0.0,0.0')) {
                        stopMsg = [addressStr[n - 1].split(",")[0], addressStr[n - 1].split(",")[1]];
                    } else {
                        stopMsg = ["124.411991", "29.043817"];
                    }
                    var obj = {
                        'index': i + 1,
                        'latMsg': stopMsg
                    };
                    addressMsg.push(obj);
                    if (myTable) { // 用于防止重复请求逆地址
                        var oldIndex = myTable.page() + '_' + $('.dataTables_length select').val() + '_' + (i + 1);
                        if (isGetAddressObj[oldIndex]) {
                            return;
                        }
                        isGetAddressObj[oldIndex] = '已获取地址';

                    }
                }
                if (num == dataLength && addressMsg.length > 0) {
                    var addressIndex = 0;
                    var addressArray = [];
                    newBackAddressMsg(addressIndex, addressMsg, null, addressArray, "dataTable", 5);
                    addressMsg = [];
                }
            }
        },
        getTable: function (table) {
            $('.toggle-vis').prop('checked', true);
            myTable = $(table).DataTable({
                "columnDefs": [{
                    "targets": [0, 3, 4],
                    "searchable": false
                }],
                "destroy": true,
                /*"dom": 'tiprl',// 自定义显示项*/
                "lengthChange": true, // 是否允许用户自定义显示数量
                "bPaginate": true, // 翻页功能
                "bFilter": false, // 列筛序功能
                "searching": true, // 本地搜索
                "ordering": false, // 排序功能
                "Info": true, // 页脚信息
                "autoWidth": true, // 自动宽度
                "stripeClasses": [],
                "pageLength": 10,
                "lengthMenu": [5, 10, 20, 50, 100, 200],
                "pagingType": "simple_numbers", // 分页样式
                "dom": "t" + "<'row'<'col-md-2 col-sm-12 col-xs-12 noPadding'l><'col-md-4 col-sm-12 col-xs-12 noPadding'i><'col-md-6 col-sm-12 col-xs-12 noPadding'p>>",
                "oLanguage": { // 国际语言转化
                    "oAria": {
                        "sSortAscending": " - click/return to sort ascending",
                        "sSortDescending": " - click/return to sort descending"
                    },
                    "sLengthMenu": "显示 _MENU_ 记录",
                    "sInfo": "当前显示 _START_ 到 _END_ 条，共 _TOTAL_ 条记录。",
                    "sZeroRecords": "我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？",
                    "sEmptyTable": "我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？",
                    "sLoadingRecords": "正在加载数据-请等待...",
                    "sInfoEmpty": "当前显示0到0条，共0条记录",
                    //"sInfoFiltered": "（数据库中共为 _MAX_ 条记录）",
                    "sInfoFiltered": "",
                    "sProcessing": "<img src='../resources/user_share/row_details/select2-spinner.gif'/> 正在加载数据...",
                    "sSearch": "模糊查询：",
                    "sUrl": "",
                    "oPaginate": {
                        "sFirst": "首页",
                        "sPrevious": " 上一页 ",
                        "sNext": " 下一页 ",
                        "sLast": " 尾页 "
                    }
                },
                "drawCallback": function (settings) {
                    var newStopLoc = stopLoc;
                    var aiDisplay = settings.aiDisplay;
                    if (aiDisplay.length != '0' && aiDisplay.length < stopLoc.length) {
                        newStopLoc = [];
                        for (var i = 0; i < aiDisplay.length; i++) {
                            newStopLoc.push(stopLoc[aiDisplay[i]]);
                        }
                    }
                    /* var $dataTableTbody = $("#dataTable tbody");
                     var dataTableTr = $dataTableTbody.find("tr:last-child").children("td:nth-child(5)").text();
                     var flag=flag;
                     for(var i=0;i<dataTableTr.length;i++){
                     }*/
                    //报警位置进行逆地址解析
                    offlineVehicleDisposalRecords.getAddress(newStopLoc);
                },
                "order": [
                    [0, null]
                ], // 第一列排序图标改为默认

            });
            myTable.on('order.dt search.dt', function () {
                myTable.column(0, {
                    search: 'applied',
                    order: 'applied'
                }).nodes().each(function (cell, i) {
                    cell.innerHTML = i + 1;
                });
            }).draw();
            //显示隐藏列
            $('.toggle-vis').off('change').on('change', function (e) {
                var column = myTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });
            $("#search_button").on("click", offlineVehicleDisposalRecords.searchTable);
        },
        //刷新列表
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            offlineVehicleDisposalRecords.inquireClick(1);
        },
        searchTable: function () {
            simpleQueryParam = $("#simpleQueryParam").val();
            isGetAddressObj = {};
            myTable.search(simpleQueryParam, false, false).draw();
        },
        inquireClick: function () {
            offlineVehicleDisposalRecords.getCheckedNodes('treeDemo');
            if (!offlineVehicleDisposalRecords.validates()) {
                return;
            }
            var ajaxDataParam = {
                "groupIds": groupId,
                "day": $('#totalTime').val() ? $('#totalTime').val() : 1,
            };
            var url = "/clbs/GeneralCargo/ReportManagement/offLineReport/list";
            json_ajax("POST", url, "json", true, ajaxDataParam, offlineVehicleDisposalRecords.getCallback);
        },
        getCallback: function (date) {
            if (date.success == true) {
                dataListArray = []; //用来储存显示数据
                stopLoc = [];
                if (date.obj != null && date.obj.length != 0) {
                    isGetAddressObj = {};
                    $("#simpleQueryParam").val("");
                    $("#exportAlarm").removeAttr("disabled");
                    var result = date.obj.list;
                    var len = result.length;
                    for (var i = 0; i < len; i++) {
                        var recordData = result[i];
                        var dateList = [
                            i + 1,
                            recordData.brand,
                            recordData.groupName,
                            recordData.lastTime,
                            recordData.lastLocation ? recordData.lastLocation : '加载中...',
                        ];
                        dataListArray.push(dateList);
                        stopLoc.push(recordData.key);
                    }
                    offlineVehicleDisposalRecords.reloadData(dataListArray);
                } else {
                    offlineVehicleDisposalRecords.reloadData(dataListArray);
                    $("#simpleQueryParam").val("");
                    $("#exportAlarm").attr("disabled", "disabled");
                }
            } else {
                if (date.msg != null) {
                    layer.msg(date.msg, {
                        move: false
                    });
                }
            }
        },
        reloadData: function (dataList) {
            var currentPage = myTable.page();
            myTable.clear();
            myTable.rows.add(dataList);
            myTable.page(currentPage).draw(false);
            myTable.search('', false, false).page(currentPage).draw();
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
    }
    $(function () {
        offlineVehicleDisposalRecords.init();
        offlineVehicleDisposalRecords.getTable('#dataTable');
        $("#timeInterval").val(offlineVehicleDisposalRecords.getYesterDay());
        $("#groupSelect").bind("click", showMenuContent);
        //导出
        $("#exportAlarm").bind("click", offlineVehicleDisposalRecords.exportAlarm);
        $("#refreshTable").bind("click", offlineVehicleDisposalRecords.refreshTable);

        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                search_ztree('treeDemo', 'groupSelect', 'group');
                treeObj.checkAllNodes(false);
            }
        });
        /**
         * 监控对象树模糊查询
         */
        $("#groupSelect").on('input propertychange', function (value) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.checkAllNodes(false);
            search_ztree('treeDemo', 'groupSelect', 'group');
        });
    })
}(window, $))