(function (window, $) {
    //开始时间
    var startTime;
    //结束时间
    var endTime;
    //判断组织节点是否是勾选操作
    var checkFlag = false;
    //当前权限监控对象数量
    var size;
    var zTreeIdJson = {};
    //刚进入页面小于100自动勾选
    var ifAllCheck = true;
    var groupId = [];
    var simpleQueryParam = '';

    trafficViolationsReport = {
        init: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            for (var i = 0; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-text").html(menu_text);
            //车辆树
            var setting = {
                async: {
                    url: trafficViolationsReport.getParkingReportTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: trafficViolationsReport.ajaxDataFilter
                },
                check: {
                    enable: true,
                    chkStyle: "radio",
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
                    beforeClick: trafficViolationsReport.beforeClickVehicle,
                    onAsyncSuccess: trafficViolationsReport.zTreeOnAsyncSuccess,
                    beforeCheck: trafficViolationsReport.zTreeBeforeCheck,
                    onCheck: trafficViolationsReport.onCheckVehicle,
                    onNodeCreated: trafficViolationsReport.zTreeOnNodeCreated,
                    onExpand: trafficViolationsReport.zTreeOnExpand
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },
        /**
         * 数据表格相关方法
         * */
        inquireClick: function (number, searchFlag) {
            if (number == 0) {
                trafficViolationsReport.getsTheCurrentTime();
            } else if (number == 'yesterday') {
                trafficViolationsReport.getsTheCurrentTime(true)
            } else if (number == -1) {
                trafficViolationsReport.startDay(-1)
            } else if (number == -3) {
                trafficViolationsReport.startDay(-3)
            } else if (number == -7) {
                trafficViolationsReport.startDay(-7)
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
            trafficViolationsReport.getCheckedNodes('treeDemo');
            if (!trafficViolationsReport.validates()) {
                return;
            }
            if(!searchFlag){
                $('#simpleQueryParam').val('')
            }
            var ajaxDataParam = {
                orgId: groupId.slice(0, -1),
                "startTime": startTime,
                "endTime": endTime,
                simpleQueryParam: $('#simpleQueryParam').val()
            };
            var url = "/clbs/s/cargo/violationRecord/getViolationRecords";
            json_ajax("POST", url, "json", true, ajaxDataParam, trafficViolationsReport.getCallback);
        },
        doHandleMonth: function (month) {
            var m = month;
            if (month.toString().length == 1) {
                m = "0" + month;
            }
            return m;
        },
        getCallback: function (data) {
            $('#exportAlarm').prop('disabled', false);
            // trafficViolationsReport.tableInit();
            myTable.clear()
            myTable.rows.add(data.obj).draw()
            if (!data.success && data.msg) {
                layer.msg(data.msg);
                $('#exportAlarm').prop('disabled', true);
            }
        },
        //创建表格
        tableInit: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            for (var i = 0; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-text").html(menu_text);

            var columnDefs = [{
                //第一列，用来显示序号
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];
            var columns = [{
                //第一列，用来显示序号
                "data": null,
                "class": "text-center"
            }, {
                "data": "orgName",
                "class": "text-center"
            }, {
                "data": "alarmStartTime",
                "class": "text-center"
            }, {
                "data": "brand",
                "class": "text-center"
            }, {
                "data": "driver",
                "class": "text-center",
                render: function (data) {
                    return data ? data : '';
                }
            }, {
                "data": "address",
                "class": "text-center",
                render: function (data) {
                    return data ? data : '';
                }
            }, {
                "data": "violationReason",
                "class": "text-center",
                render: function (data) {
                    return data ? data : '';
                }
            }, {
                "data": "msg",
                "class": "text-center",
                render: function (data) {
                    return data ? data : '';
                }
            }, {
                "data": "dealInfo",
                "class": "text-center",
                render: function (data) {
                    return data ? data : '';
                }
            }, {
                "data": "gpsInfo",
                "class": "text-center",
                render: function (data) {
                    return data ? data : '';
                }
            }, {
                "data": "gpsDealInfo",
                "class": "text-center",
                render: function (data) {
                    return data ? data : '';
                }
            }];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.orgId = groupId.slice(0, -1);
                d.startTime = startTime;
                d.endTime = endTime;
                d.simpleQueryParam = simpleQueryParam;
                d.simpleQueryParam = $('#simpleQueryParam').val();
                simpleQueryParam = $('#simpleQueryParam').val();
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/s/cargo/violationRecord/getViolationRecords',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                getAddress: false,//是否逆地理编码
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
        },
        validates: function () {
            return $("#lifeCycleForm").validate({
                rules: {
                    groupSelect: {
                        zTreeCheckGroup: "treeDemo"
                    }
                },
                messages: {
                    groupSelect: {
                        zTreeCheckGroup: vehicleSelectGroup,
                    },
                }
            }).form();
        },
        getTable: function (table) {
            $('.toggle-vis').prop('checked', true);
            var columns = [
                {
                //第一列，用来显示序号
                "data": null,
                "class": "text-center"
            }, {
                "data": "orgName",
                "class": "text-center"
            }, {
                "data": "time",
                "class": "text-center",
                render: function (data) {
                    if(!data) return ''
                    var y = data.slice(0,4)
                    var m = data.slice(4,6)
                    var d = data.slice(6,8)
                    var h = data.slice(8,10)
                    var mm = data.slice(10,12)
                    var s = data.slice(12,14)
                    return y + '-' + m + '-' + d + ' ' + h + ':' + mm + ':' + s;
                }
            }, {
                "data": "monitorName",
                "class": "text-center"
            }, {
                "data": "driverName",
                "class": "text-center",
                render: function (data) {
                    return data ? data : '';
                }
            }, {
                "data": "address",
                "class": "text-center",
                render: function (data) {
                    return data ? data : '';
                }
            }, {
                "data": "violationReason",
                "class": "text-center",
                render: function (data) {
                    switch (data) {
                        case 1:
                            return '超速'
                        case 2:
                            return '疲劳'
                        default :
                            return '其它'
                    }
                }
            }, {
                "data": "msg",
                "class": "text-center",
                render: function (data) {
                    return data ? data : '';
                }
            }, {
                "data": "dealInfo",
                "class": "text-center",
                render: function (data) {
                    return data ? data : '';
                }
            }, {
                "data": "gpsInfo",
                "class": "text-center",
                render: function (data) {
                    return data ? data : '';
                }
            }, {
                "data": "gpsDealInfo",
                "class": "text-center",
                render: function (data) {
                    return data ? data : '';
                }
            }];
            myTable = $(table).DataTable({
                "columnDefs": [{
                    "targets": [0, 2, 3, 4, 5, 6],
                    "searchable": false
                }],
                columns: columns,
                "destroy": true,
                /*"dom": 'tiprl',// 自定义显示项*/
                "lengthChange": true,// 是否允许用户自定义显示数量
                "bPaginate": true, // 翻页功能
                "bFilter": false, // 列筛序功能
                "searching": true,// 本地搜索
                "ordering": false, // 排序功能
                "Info": true,// 页脚信息
                "autoWidth": true,// 自动宽度
                "stripeClasses": [],
                "pageLength": 10,
                "lengthMenu": [5, 10, 20, 50, 100, 200],
                "pagingType": "simple_numbers", // 分页样式
                "dom": "t" + "<'row'<'col-md-2 col-sm-12 col-xs-12 noPadding'l><'col-md-4 col-sm-12 col-xs-12 noPadding'i><'col-md-6 col-sm-12 col-xs-12 noPadding'p>>",
                "oLanguage": {// 国际语言转化
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
                ],// 第一列排序图标改为默认

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
        },
        searchTable: function () {
            trafficViolationsReport.inquireClick(1, true)
        },
        //刷新列表
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            trafficViolationsReport.inquireClick(1);
        },
        exportAlarm: function () {
            if ($("#dataTable tbody tr td").hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据，无法导出");
                return;
            }
            if(getRecordsNum() > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var url = "/clbs/s/cargo/violationRecord/exportViolationRecords";
            var parameter = {
                orgId: groupId.slice(0, -1),
                startTime: startTime,
                endTime: endTime,
                simpleQueryParam: $('#simpleQueryParam').val()
            };
            json_ajax("post", url, "json", false, parameter, function (data) {
                if (data.success) {
                    layer.confirm("已加入到导出队列,请注意查看导出管理消息提醒", {
                        icon: 3,
                        title: '操作确认',
                        btn: ['确定', '导出管理']
                    }, function () {
                        layer.closeAll();
                    }, function () {
                        layer.closeAll();
                        pagesNav.showExportManager();
                    });
                } else if (data.msg) {
                    layer.msg(data.msg);
                }
            });
        },

        /**
         * 组织树相关方法
         * */
        getParkingReportTreeUrl: function (treeId, treeNode) {
            if (treeId == 'treeDemo') {
                return "/clbs/m/basicinfo/enterprise/professionals/tree";
            }
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
            /* if (size <= 100 && ifAllCheck) {
                 treeObj.checkAllNodes(true);
                 var nodes = treeObj.getNodes();
                 for (var i = 0; i < nodes.length; i++) { //设置节点展开
                     treeObj.expandNode(nodes[i], true, false, true);
                 }
             }*/
            // trafficViolationsReport.getCharSelect(treeObj);
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
                $("#groupSelect").val(treeNode.name);
                zTree.expandNode(treeNode, true, true, false, false); // 展开节点
                setTimeout(() => {
                    trafficViolationsReport.getCheckedNodes("treeDemo");
                    trafficViolationsReport.validates();
                }, 600);
            } else {
                $("#groupSelect").val("");
            }
            trafficViolationsReport.getCheckedNodes("treeDemo");
            // trafficViolationsReport.getCharSelect(zTree);
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
                nodes = zTree.getCheckedNodes(true), groupIds = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (treeId == "treeDemo" && nodes[i].type == "group") {
                    groupIds += nodes[i].uuid + ",";
                }
            }
            groupId = groupIds;
        },

        /**
         * 时间设置相关方法
         * */
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
                tMonth = trafficViolationsReport.doHandleMonth(tMonth + 1);
                tDate = trafficViolationsReport.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " "
                    + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24
                    * parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = trafficViolationsReport.doHandleMonth(endMonth + 1);
                endDate = trafficViolationsReport.doHandleMonth(endDate);
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
                vMonth = trafficViolationsReport.doHandleMonth(vMonth + 1);
                vDate = trafficViolationsReport.doHandleMonth(vDate);
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
                    vendMonth = trafficViolationsReport.doHandleMonth(vendMonth + 1);
                    vendDate = trafficViolationsReport.doHandleMonth(vendDate);
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
    };
    $(function () {
        trafficViolationsReport.init();
        trafficViolationsReport.getTable('#dataTable');
        $('#timeInterval').dateRangePicker({
            dateLimit: 1,
            /* isOffLineReportFlag: true,*/
            nowDate: trafficViolationsReport.getYesterDay(),
            isShowHMS: true
        });
        $("#search_button").bind("click", trafficViolationsReport.searchTable);
        $("#groupSelect").bind("click", showMenuContent);
        //导出
        $("#exportAlarm").bind("click", trafficViolationsReport.exportAlarm);
        $("#refreshTable").bind("click", trafficViolationsReport.refreshTable);
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