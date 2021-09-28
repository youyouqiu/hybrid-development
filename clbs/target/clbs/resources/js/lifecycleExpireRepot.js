(function (window, $) {
    //开始时间
    var queryDateStr;
    //结束时间
    var endTime;
    var myTable;
    //判断组织节点是否是勾选操作
    var checkFlag = false;
    var zTreeIdJson = {};
    //刚进入页面小于100自动勾选
    var ifAllCheck = true;
    var groupId = [];
    //用来储存显示数据
    var dataListArray = [];
    var allSize = 0;
    var dbValue = false //树双击判断参数

    lifeCycleExpire = {
        init: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            //var table = $("#dataTable tr th:gt(1)");
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(1) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            };
            $("#Ul-menu-text").html(menu_text);
            //车辆树
            var setting = {
                async: {
                    url: lifeCycleExpire.getParkingReportTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "type": "multiple",
                        "icoType": "0"
                    },
                    dataFilter: lifeCycleExpire.ajaxDataFilter
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
                    beforeClick: lifeCycleExpire.beforeClickVehicle,
                    onAsyncSuccess: lifeCycleExpire.zTreeOnAsyncSuccess,
                    beforeCheck: lifeCycleExpire.zTreeBeforeCheck,
                    onCheck: lifeCycleExpire.onCheckVehicle,
                    onNodeCreated: lifeCycleExpire.zTreeOnNodeCreated,
                    onExpand: lifeCycleExpire.zTreeOnExpand,
                    onDblClick: lifeCycleExpire.onDblClickVehicle
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },
        onDblClickVehicle: function (e, treeId, treeNode) {
            if (!treeNode) return
            dbValue = true
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.checkAllNodes(false);
            lifeCycleExpire.getCheckedNodes("treeDemo");
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
            lifeCycleExpire.getCheckedNodes("treeDemo");
            if (!lifeCycleExpire.validates()) {
                return;
            }

            var timeInterval = $('#timeInterval').val();
            var lifeCycleType = $('#lifeCycleType').val();
            var simpleQueryParam = $('#simpleQueryParam').val();
            var data = {
                "groupId": groupId,
                'queryDateStr': timeInterval,
                "lifecycleStatus": lifeCycleType,
                "simpleQueryParam": simpleQueryParam
            };
            var url = "/clbs/v/statistic/lifecycleStatistic/findExportLifecycle";
            if (getRecordsNum() > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            json_ajax("POST", url, "json", false, data, lifeCycleExpire.exportCallback); //发送请求
        },
        exportCallback: function (result) {
            if (result.success == true) {
                var url = "/clbs/v/statistic/lifecycleStatistic/exportLifecycle";
                window.location.href = url;
            } else {
                layer.msg(exportFail, {
                    move: false
                });
            }
        },
        validates: function () {
            return $("#lifeCycleForm").validate({
                rules: {
                    queryDateStr: {
                        required: true
                    },
                    groupSelect: {
                        zTreeCheckGroup: "treeDemo"
                    }
                },
                messages: {
                    queryDateStr: {
                        required: "请选择日期！",
                    },
                    groupSelect: {
                        zTreeCheckGroup: vehicleSelectGroup,
                    }
                }
            }).form();
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            allSize = responseData.length;
            if (treeId == "treeDemo") {
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
            if (ifAllCheck) {
                treeObj.expandAll(true);
                if (allSize <= GROUP_MAX_CHECK) {
                    treeObj.checkAllNodes(true);
                }
            }
            lifeCycleExpire.getCharSelect(treeObj);
            //置顶消息跳转服务到期报表
            var fwType = $.getUrlParam('fwType');
            console.log('类型', fwType);
            if (fwType) {
                $("#lifeCycleType").val(fwType);
                setTimeout(function () {
                    lifeCycleExpire.inquireClick(1, true);
                }, 500)
            }
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked && !dbValue) {
                var zTree = $.fn.zTree.getZTreeObj("treeDemo");
                var nodes = zTree.getNodesByFilter(function (node) {
                    return node;
                }, false, treeNode); // 仅查找一个节点
                var nodesLength = nodes.length;
                if (nodesLength > GROUP_MAX_CHECK) {
                    layer.msg('最多勾选' + GROUP_MAX_CHECK + '个企业' + '<br/>双击名称可选中本组织');
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
            dbValue = false;
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                // setTimeout(function () {
                    zTree.expandNode(treeNode, true, true, true, false); // 展开节点
                // }, 1200);
                setTimeout(() => {
                    lifeCycleExpire.getCheckedNodes("treeDemo");
                    lifeCycleExpire.validates();
                }, 600);
            }
            lifeCycleExpire.getCheckedNodes("treeDemo");
            lifeCycleExpire.getCharSelect(zTree);
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
                if (treeId == "treeDemo" && nodes[i].type == "group") {
                    groupIds += nodes[i].uuid + ",";
                }
            }
            groupId = groupIds;
        },
        getTable: function (table) {
            $('.toggle-vis').prop('checked', true);
            myTable = $(table).DataTable({
                "columnDefs": [{
                    "targets": [0, 2, 3, 4, 5, 6],
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
            $("#search_button").on("click", function () {
                var queryCondition = $("#simpleQueryParam").val();
                myTable.column(1).search(queryCondition, false, false).draw();
            });
        },
        //刷新列表
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            myTable.requestData();
        },
        //获取当前时间，格式YYYY-MM-DD
        getNowFormatDate: function () {
            var date = new Date();
            var separator = "-";
            var year = date.getFullYear();
            var month = date.getMonth() + 1;
            var strDate = date.getDate();
            if (month >= 1 && month <= 9) {
                month = "0" + month;
            }
            if (strDate >= 0 && strDate <= 9) {
                strDate = "0" + strDate;
            }
            return (year + separator + month + separator + strDate);
        },
        inquireClick: function (number, type) {
            if (number === 0) {
                queryDateStr = lifeCycleExpire.getNowFormatDate();
                $('#timeInterval').val(queryDateStr);
            } else if (number === 1) {
                queryDateStr = $('#timeInterval').val()
            }
            lifeCycleExpire.getCheckedNodes('treeDemo');
            if (!lifeCycleExpire.validates()) {
                return;
            }
            var ajaxDataParam = {
                "groupId": groupId,
                "queryDateStr": queryDateStr,
                "lifecycleStatus": $('#lifeCycleType').val(),
                "filterType": type ? 1 : 0
            }
            var url = "/clbs/v/statistic/lifecycleStatistic/findLifecycle";
            json_ajax("POST", url, "json", true, ajaxDataParam, lifeCycleExpire.getCallback);
        },
        getCallback: function (date) {
            if (date.success == true) {
                dataListArray = []; //用来储存显示数据
                if (date.obj != null && date.obj.length != 0) {
                    $("#exportAlarm").removeAttr("disabled");
                    for (var i = 0; i < date.obj.length; i++) {
                        var recordData = date.obj[i];
                        // var monitorType = recordData.monitorType;
                        var dateList = [
                            i + 1,
                            recordData.monitorNumber,
                            recordData.monitorType,
                            recordData.groupName,
                            recordData.assignmentName,
                            recordData.lifecycleStatus,
                            recordData.expireDays,
                            recordData.expireDateStr
                        ];
                        dataListArray.push(dateList);
                    }
                    lifeCycleExpire.reloadData(dataListArray);
                    $("#simpleQueryParam").val("");
                } else {
                    lifeCycleExpire.reloadData(dataListArray);
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
            var currentPage = myTable.page()
            myTable.clear()
            myTable.rows.add(dataList)
            myTable.page(currentPage).draw(false);
            myTable.column(1).search('', false, false).page(currentPage).draw();
        }
    }
    $(function () {
        lifeCycleExpire.init();
        lifeCycleExpire.getTable('#dataTable');
        $('input').inputClear();


        laydate.render({
            elem: '#timeInterval',
            theme: '#6dcff6'
        });
        $("#timeInterval").val(lifeCycleExpire.getNowFormatDate());
        $("#groupSelect").bind("click", showMenuContent);
        //导出
        $("#exportAlarm").bind("click", lifeCycleExpire.exportAlarm);
        $("#refreshTable").bind("click", lifeCycleExpire.refreshTable);

        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                search_ztree('treeDemo', 'groupSelect', 'group');
                treeObj.checkAllNodes(false);
            };
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