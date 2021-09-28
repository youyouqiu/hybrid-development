(function (window, $) {
    /**
     * 公共方法
     * */
    var publicObject = {
        init: function () {
            roadTransportEnterprise.objTreeInit();
            vehicleStatistical.monitorTree();

            publicObject.renderTh('#dataTable');
            publicObject.renderTh('#dataTable2');
            publicObject.renderTh('#dataTable3');

            publicObject.monthDataInit('#month');
            publicObject.monthDataInit('#month1');
            publicObject.monthDataInit('#month2');

            publicObject.tableFilter('#dataTable', '#Ul-menu-text');
            publicObject.tableFilter('#dataTable2', '#Ul-menu-text2');
            publicObject.tableFilter('#dataTable3', '#Ul-menu-text3');

            roadTransportEnterprise.getTable('#dataTable');
            vehicleStatistical.getTable('#dataTable2');
            administrativeAreas.getTable('#dataTable3');

            $("[data-toggle='tooltip']").tooltip();
        },
        // 获取每月天数
        getMonthDay: function (year, month) {
            if (!year) {
                var date = new Date();
                year = date.getFullYear();
                month = date.getMonth() + 1;
            }
            var dates = new Date(year, month, 0).getDate(); // 获得是标准时间,需要getDate()获得天数
            return dates;
        },
        // 渲染表格表头信息
        renderTh: function (id, year, month) {
            if (!year) {
                var now = new Date();
                year = now.getFullYear();
                month = now.getMonth() + 1;
            }
            var tr = $(id).find('thead tr');
            tr.empty();
            var dates = 30;
            if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
                dates = 31;
            } else if (month == 2) {
                dates = 28;
                if (year % 100 != 0) {
                    if (year % 4 == 0) {
                        dates = 29;
                    }
                } else {
                    if (year % 400 == 0) {
                        dates = 29;
                    }
                }
            }
            var tmpl = '<th class="text-center">$name</th>';
            tr.append($(tmpl.replace('$name', '序号')));
            if (id === '#dataTable') {
                tr.append($(tmpl.replace('$name', '道路运输企业')));
            } else if (id === '#dataTable2') {
                tr.append($(tmpl.replace('$name', '监控对象')));
            } else {
                tr.append($(tmpl.replace('$name', '行政区域')));
            }

            for (var i = 1; i <= dates; i++) {
                tr.append($(tmpl.replace('$name', i)));
            }
            tr.append($(tmpl.replace('$name', '合计')));
        },
        // 初始化月份选择
        monthDataInit: function (element) {
            var now = new Date();
            var year = now.getFullYear();
            var month = now.getMonth() + 1;
            if (month > 12) {
                year += 1;
                month = 1;
            }
            if (month >= 1 && month <= 9) {
                month = "0" + month
            }
            var lastMonthDate = new Date();
            lastMonthDate.setMonth(lastMonthDate.getMonth());
            var lastYear = lastMonthDate.getFullYear();
            var lastMonthText = lastMonthDate.getMonth() + 1;
            if (lastMonthText >= 1 && lastMonthText <= 9) {
                lastMonthText = "0" + lastMonthText
            }
            var lastMonth = lastYear + '-' + lastMonthText;
            var maxMonth = year + '-' + (month) + "-01 00:00:00";
            $(element).val(lastMonth);
            laydate.render({
                elem: element,
                type: 'month',
                max: maxMonth,
                btns: ['clear', 'confirm'],
                ready: function (date) {
                    $("#layui-laydate1").off('click').on('click', '.laydate-month-list li', function () {
                        $("#layui-laydate1").remove();
                    });
                    $("#layui-laydate2").off('click').on('click', '.laydate-month-list li', function () {
                        $("#layui-laydate2").remove();
                    });
                },
                // 点击月份立即改变input值
                change: function (value, dates, edate) {
                    var final;

                    // 获取选择的年月
                    var year = dates.year;
                    var month = dates.month;
                    var myDate = new Date();
                    var nowYear = myDate.getFullYear();
                    var nowMonth = myDate.getMonth() + 1;

                    if (year > nowYear) {
                        final = publicObject.getYesterMonth(1);
                    } else if (year == nowYear) {
                        if (month > nowMonth) {
                            final = publicObject.getYesterMonth(1)
                        } else {
                            final = value;
                        }
                    } else {
                        final = value;
                    }
                    $(element).val(final);
                },
            });
        },
        tableFilter: function (tableId, menuId) {
            // 显示隐藏列
            var menu_text = "";
            var table = $(tableId + " tr th:gt(0)");
            for (var i = 0; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $(menuId).html(menu_text);
        },
        // 获取上月时间，格式YYYY-MM
        getYesterMonth: function (del, needSecond) {
            var myDate = new Date();
            var year = myDate.getFullYear();
            var month = myDate.getMonth() + 1;
            var date = myDate.getDate();
            var h = myDate.getHours();
            var m = myDate.getMinutes();
            var s = myDate.getSeconds();
            var now;
            if (month == 1) {
                year = year - 1;
                month = 12;
            } else {
                month = month - del; // 几月前
            }

            if (needSecond) {
                now = year + "-" + fillZero(month) + "-" + fillZero(date) + " " + fillZero(h) + ":" + fillZero(m) + ":" + fillZero(s);
            } else {
                now = year + "-" + fillZero(month)
            }
            return now;
        },
        setNewMonth: function (number, element) {
            var curTime = $(element).val();
            if (!curTime) return;
            var curTimeArr = curTime.split('-');
            var currentYear = parseInt(curTimeArr[0]);
            var currentMonth = parseInt(curTimeArr[1]);
            if (number === 0) {
                var dateList = new Date();
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                $(element).val(vYear + '-' + fillZero(vMonth + 1));
            } else {
                var vMonth = currentMonth + number;
                if (vMonth === 0) {
                    currentYear--;
                    vMonth = 12;
                }
                $(element).val(currentYear + '-' + fillZero(vMonth));
            }
        },
        getDateFromMonth: function (month) {
            var date = new Date(month);
            var year = date.getFullYear();
            var m = date.getMonth() + 1;
            var ms = m.toString();
            var ms2 = (m + 1).toString();
            return [year + '-' + ms + '-' + '1', year + '-' + ms2 + '-' + '1'];
        },
        // 组织树预处理加载函数
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            if (treeId == 'treeDemo2') {
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
            }
            size = responseData.length;
            return responseData;
        },
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(treeId);
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        // 获取具体天数的统计数据
        columnRenderFun: function (index) {
            return function (data, type, row, meta) {
                if (data) {
                    return data[index] !== null && data[index] !== undefined ? data[index] : '-';
                }
                return '-';
            };
        },
    };

    /**
     * 按道路运输企业统计tab页签
     * */
    var orgId = []; // 勾选企业
    var myTable;
    var simpleQueryParam;
    var currentMonth1 = null;
    var dbValue = false; //树双击判断参数

    roadTransportEnterprise = {
        objTreeInit: function () {
            var treeSetting = {
                async: {
                    url: roadTransportEnterprise.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    dataFilter: publicObject.ajaxDataFilter
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
                    beforeClick: publicObject.beforeClickVehicle,
                    onCheck: roadTransportEnterprise.onCheckOrg,
                    beforeCheck: roadTransportEnterprise.beforeCheck,
                    // onExpand: vehicleOnlineTimeStatistics.zTreeOnExpand,
                    onAsyncSuccess: roadTransportEnterprise.zTreeOnAsyncSuccess,
                    onDblClick: roadTransportEnterprise.onDblClickVehicle
                    // onNodeCreated: vehicleOnlineTimeStatistics.zTreeOnNodeCreated
                }
            };
            $.fn.zTree.init($("#treeDemo"), treeSetting, null);
        },
        treeUtil: {
            getCheckedNodes: function (id) {
                var treeObj = $.fn.zTree.getZTreeObj(id);
                return treeObj ? treeObj.getCheckedNodes(true) : []
            },
            // 获取子节点
            getAllChildNodes: function (treeNode) {
                var result = []

                function getAll(treeNode) {
                    if (treeNode.children) {
                        result.push(treeNode);
                        treeNode.children.forEach(function (item) {
                            getAll(item);
                        })
                    } else {
                        result.push(treeNode);
                    }
                }

                getAll(treeNode)
                return result;
            },
        },
        onDblClickVehicle: function (e, treeId, treeNode) {
            if (!treeNode) return
            dbValue = true
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.checkAllNodes(false);
            roadTransportEnterprise.getCheckedNodes("treeDemo");
            // $("#groupSelect").val(treeNode.name);
            // search_ztree('treeDemo', 'groupSelect', 'group');
            treeObj.checkNode(treeNode, !treeNode.checked, false, true);
        },
        beforeCheck: function (treeId, treeNode) {
            var currentCheckedNodesLength = 0 //已被勾选的节点数量
            var toCheckedNodesLength = 0 //即将被勾选的节点数量
            if (!treeNode.checked && !dbValue) {
                currentCheckedNodesLength = roadTransportEnterprise.treeUtil.getCheckedNodes('treeDemo').length
                toCheckedNodesLength = roadTransportEnterprise.treeUtil.getAllChildNodes(treeNode).length
                if (currentCheckedNodesLength + toCheckedNodesLength > TREE_MAX_CHILDREN_LENGTH) {
                    layer.msg('最多勾选' + TREE_MAX_CHILDREN_LENGTH + '个组织' + '<br/>双击名称可选中本组织');
                    return false;
                }
            }

        },
        getTreeUrl: function (treeId, treeNode) {
            if (treeId == 'treeDemo2') {
                return "/clbs/c/user/groupTree?type=multiple";
            }
            return '/clbs/m/basicinfo/enterprise/professionals/tree?isOrg=1';
        },
        onCheckOrg: function (e, treeId, treeNode) {
            dbValue = false
            var checkedNodes = roadTransportEnterprise.treeUtil.getCheckedNodes('treeDemo')
            var names = ''
            checkedNodes.forEach(function (item) {
                names += item.name + ','
            });
            if (treeNode && treeNode.checked) {
                setTimeout(() => {
                    roadTransportEnterprise.validates();
                }, 600);
            }
            $('#groupSelect').val(names);
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            treeObj.expandAll(true);
            var rootNode = treeObj.getNodes();
            var nodes = treeObj.transformToArray(rootNode);
            if (nodes.length <= TREE_MAX_CHILDREN_LENGTH) {
                treeObj.checkAllNodes(true);
                roadTransportEnterprise.getCheckedNodes();
                roadTransportEnterprise.getCharSelect(treeObj);
            }
        },
        // 获取到选择的节点
        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
                nodes = zTree.getCheckedNodes(true);
            orgId = [];
            for (var i = 0, l = nodes.length; i < l; i++) {
                orgId.push(nodes[i].uuid);
            }
        },
        getCharSelect: function (treeObj) {
            var groupSelectId = '#groupSelect';
            var nodes = treeObj.getCheckedNodes(true);
            var allNodes = treeObj.getNodes();
            if (nodes.length > 0) {
                $(groupSelectId).val(allNodes[0].name);
            } else {
                $(groupSelectId).val("");
            }
        },
        validates: function () {
            return $("#hourslist").validate({
                rules: {
                    groupSelect: {
                        required: true
                    },
                    month: {
                        required: true
                    }
                },
                messages: {
                    groupSelect: {
                        required: vehicleSelectGroup
                    },
                    month: {
                        required: '请选择月份'
                    }
                }
            }).form();
        },
        monthClick: function (num) {
            if (num !== 1) {
                publicObject.setNewMonth(num, '#month')
            }
            roadTransportEnterprise.getCheckedNodes();
            if (!roadTransportEnterprise.validates()) return;
            $('#simpleQueryParam').val('');
            roadTransportEnterprise.tableInit();
        },
        tableInit: function () {
            // 组织表头
            currentMonth1 = $('#month').val();
            var month = parseInt(currentMonth1.substr(5));
            var year = parseInt(currentMonth1.substr(0, 4));
            publicObject.renderTh('#dataTable', year, month);
            publicObject.tableFilter('#dataTable', '#Ul-menu-text');

            //显示隐藏列
            var columnDefs = [{
                "searchable": true,
                "orderable": false,
                "targets": [0, 1, 2, 3]
            }];
            var columns = [{
                "data": null,
                "class": "text-center",
            }, {
                "data": "orgName",
                "class": "text-center",
            }];
            var dates = publicObject.getMonthDay(year, month);
            for (var i = 0; i < dates; i++) {
                columns.push({
                    "data": 'days',
                    "class": "text-center",
                    render: publicObject.columnRenderFun(i)
                })
            }
            columns.push({
                "data": 'total',
                "class": "text-center",
            });
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.organizationIds = orgId.join(',');
                d.month = currentMonth1.replace('-', '');
                simpleQueryParam = $('#simpleQueryParam').val();
                d.simpleQueryParam = simpleQueryParam;
            };
            //表格setting
            var setting = {
                listUrl: "/clbs/m/reportManagement/vehicleOnlineTime/getOrgData",
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                lengthMenu: [5, 10, 20, 50, 100],
                pageable: true, //是否分页
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                drawCallbackFun: function () {
                    var api = myTable.dataTable;
                    var startIndex = api.context[0]._iDisplayStart; //获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                    });
                },
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                columnsDiv: '#Ul-menu-text',
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
            $('#Ul-menu-text .toggle-vis').off('change').on('change', function (e) {
                var column = myTable.dataTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });
            $("#search_button").off().on("click", function () {
                myTable.requestData();
            });
        },
        getTable: function (table) {
            $('#Ul-menu-text .toggle-vis').prop('checked', 'true');
            myTable = $(table).DataTable({
                "destroy": true,
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
                    "sInfoFiltered": "（数据库中共为 _MAX_ 条记录）",
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
                "columnDefs": [{
                    "targets": [0, 2, 3, 4],
                    "searchable": false
                }],
                "order": [
                    [0, null]
                ], // 第一列排序图标改为默认

            });
            myTable.off('order.dt search.dt').on('order.dt search.dt', function () {
                myTable.column(0, {
                    search: 'applied',
                    order: 'applied'
                }).nodes().each(function (cell, i) {
                    cell.innerHTML = i + 1;
                });
            }).draw();
            $('#Ul-menu-text .toggle-vis').off('change').on('change', function (e) {
                var column = myTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });
            $("#search_button").off('click').on("click", function () {
                var tsval = $("#simpleQueryParam").val();
                myTable.search(tsval, false, false).draw();
            });
        },
        // 刷新列表
        refreshTableMonth: function () {
            roadTransportEnterprise.monthClick(1);
        },
        export: function () {
            if ($("#dataTable tbody tr td").hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据，无法导出");
                return;
            }
            if (getRecordsNum() > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            // 请求路径
            var url = "/clbs/m/reportManagement/vehicleOnlineTime/exportOrgData";
            // 请求参数
            var pdata = {
                "organizationIds": orgId.join(','),
                "month": currentMonth1.replace('-', ''),
                "simpleQueryParam": simpleQueryParam,
                "module": '车辆在线时长统计'
            };
            json_ajax("post", url, "json", true, pdata, function (result) {
                if (result.success) {
                    layer.confirm(exportTitle, {
                        title: '操作确认',
                        icon: 3, // 问号图标
                        btn: ['确定', '导出管理'] //按钮
                    }, function () {
                        layer.closeAll();
                    }, function () {
                        layer.closeAll();
                        // 打开导出管理弹窗
                        pagesNav.showExportManager();
                    });
                } else if (result.msg) {
                    layer.msg(result.msg);
                }
            });
        },
    };

    /**
     * 按车辆统计tab页签
     * */
    var vehicleId = []; // 勾选车辆
    var myTable2;
    var zTreeIdJson = {};
    var checkFlag = false; // 判断组织节点是否是勾选操作
    var size; // 当前权限监控对象数量
    var currentMonth2 = null;
    var crrentSubV = []; //模糊查询
    var searchFlag = true;
    var simpleQueryParam2;
    vehicleStatistical = {
        monitorTree: function () {
            crrentSubV = [];
            //车辆树
            var setting = {
                async: {
                    url: vehicleStatistical.getAlarmReportTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "type": "multiple",
                        "icoType": "0"
                    },
                    dataFilter: publicObject.ajaxDataFilter
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
                    beforeClick: publicObject.beforeClickVehicle,
                    // onAsyncSuccess: vehicleOnlineTimeStatistics.zTreeOnAsyncSuccess,
                    beforeCheck: vehicleStatistical.zTreeBeforeCheck,
                    onCheck: vehicleStatistical.onCheckVehicle,
                    onNodeCreated: vehicleStatistical.zTreeOnNodeCreated,
                    onExpand: vehicleStatistical.zTreeOnExpand
                }
            };
            $.fn.zTree.init($("#treeDemo2"), setting, null);
        },
        getAlarmReportTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle";
            }
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
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo2"),
                        nodes = zTree
                            .getCheckedNodes(true),
                        v = "";
                    var nodesLength = 0;
                    json_ajax("post", "/clbs/m/personalized/ico/getVehicleCount",
                        "json", false, {
                            "parentId": treeNode.id,
                            "type": treeNode.type
                        },
                        function (data) {
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
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo2"),
                        nodes = zTree
                            .getCheckedNodes(true),
                        v = "";
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
        zTreeOnExpand: function (event, treeId, treeNode) {
            // 判断是否是勾选操作展开的树(是则继续执行，不是则返回)
            if (treeNode.type == "group" && !checkFlag) {
                return;
            }
            checkFlag = false;
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo2");
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
                            var chNodes = result[i]; //获取对应的value
                            if (zTreeIdJson[pid] != undefined) {
                                var parentTid = zTreeIdJson[pid][0];
                                var parentNode = treeObj.getNodeByTId(parentTid);
                                if (parentNode && parentNode.children === undefined) {
                                    treeObj.addNodes(parentNode, []);
                                }
                            }
                        });
                    }
                })
            }
        },
        getCharSelect: function (treeObj) {
            var groupSelectId = '#groupSelect2';
            var nodes = treeObj.getCheckedNodes(true);
            var allNodes = treeObj.getNodes();
            if (nodes.length > 0) {
                $(groupSelectId).val(allNodes[0].name);
            } else {
                $(groupSelectId).val("");
            }
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(treeId);
            // 若为取消勾选则不展开节点
            if (treeNode.checked) {
                searchFlag = false;
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    vehicleStatistical.getCheckedNodes();
                    vehicleStatistical.validates();
                }, 600);
            }
            crrentSubV = [];
            crrentSubV.push(treeNode.id);
            vehicleStatistical.getCheckedNodes();
            vehicleStatistical.getCharSelect(zTree);
        },
        // 获取到选择的节点
        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo2"),
                nodes = zTree.getCheckedNodes(true);
            vehicleId = [];
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == "vehicle") {
                    vehicleId.push(nodes[i].id);
                }
            }
        },
        //模糊查询树
        searchVehicleTree: function (param) {
            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                vehicleStatistical.monitorTree();
            } else {
                bflag = true;
                var querySetting = {
                    async: {
                        url: "/clbs/a/search/reportFuzzySearch",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {
                            "type": $('#queryType').val(),
                            "queryParam": param,
                            "queryType": "vehicle"
                        },
                        dataFilter: vehicleStatistical.ajaxQueryDataFilter
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
                        beforeClick: publicObject.beforeClickVehicle,
                        beforeCheck: vehicleStatistical.zTreeBeforeCheck,
                        onCheck: vehicleStatistical.onCheckVehicle,
                        onExpand: vehicleStatistical.zTreeOnExpand,
                        onNodeCreated: vehicleStatistical.zTreeOnNodeCreated
                    }
                };
                $.fn.zTree.init($("#treeDemo2"), querySetting, null);
            }
        },
        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            var responseData = JSON.parse(ungzip(responseData));
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
        monthClick: function (num) {
            if (num !== 1) {
                publicObject.setNewMonth(num, '#month1')
            }
            if (!vehicleStatistical.validates()) return;
            $('#simpleQueryParam2').val('');
            vehicleStatistical.getCheckedNodes();
            vehicleStatistical.tableInit();
        },
        validates: function () {
            return $("#hourslist2").validate({
                rules: {
                    groupSelect: {
                        zTreeChecked: "treeDemo2"
                    },
                    month: {
                        required: true
                    }
                },
                messages: {
                    groupSelect: {
                        zTreeChecked: vehicleSelectBrand
                    },
                    month: {
                        required: '请选择月份'
                    }
                }
            }).form();
        },
        tableInit: function () {
            // 组织表头
            currentMonth2 = $('#month1').val();
            var month = parseInt(currentMonth2.substr(5));
            var year = parseInt(currentMonth2.substr(0, 4));
            publicObject.renderTh('#dataTable2', year, month);
            publicObject.tableFilter('#dataTable2', '#Ul-menu-text2');

            //显示隐藏列
            var columnDefs = [{
                "searchable": true,
                "orderable": false,
                "targets": [0, 1, 2, 3]
            }];
            var columns = [{
                "data": null,
                "class": "text-center",
            }, {
                "data": "monitorName",
                "class": "text-center",
            }];
            var dates = publicObject.getMonthDay(year, month);
            for (var i = 0; i < dates; i++) {
                columns.push({
                    "data": 'days',
                    "class": "text-center",
                    render: publicObject.columnRenderFun(i)
                })
            }
            columns.push({
                "data": 'total',
                "class": "text-center",
            });
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.monitorIds = vehicleId.join(',');
                d.month = currentMonth2.replace('-', '');
                simpleQueryParam2 = $('#simpleQueryParam2').val();
                d.simpleQueryParam = simpleQueryParam2;
            };
            //表格setting
            var setting = {
                listUrl: "/clbs/m/reportManagement/vehicleOnlineTime/getMonitorData",
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable2', //表格
                lengthMenu: [5, 10, 20, 50, 100],
                pageable: true, //是否分页
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                drawCallbackFun: function () {
                    var api = myTable2.dataTable;
                    var startIndex = api.context[0]._iDisplayStart; //获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                    });
                },
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                columnsDiv: '#Ul-menu-text2',
            };
            //创建表格
            myTable2 = new TG_Tabel.createNew(setting);
            myTable2.init();
            $('#Ul-menu-text2 .toggle-vis').off('change').on('change', function (e) {
                var column = myTable2.dataTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $("#keepOpen2").addClass("open");
            });
            $("#search_button2").off('click').on("click", function () {
                myTable2.requestData();
            });
        },
        getTable: function (table) {
            $('#Ul-menu-text2 .toggle-vis').prop('checked', 'true');
            myTable2 = $(table).DataTable({
                "destroy": true,
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
                    "sInfoFiltered": "（数据库中共为 _MAX_ 条记录）",
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
                "columnDefs": [{
                    "targets": [0, 2, 3, 4],
                    "searchable": false
                }],
                "order": [
                    [0, null]
                ], // 第一列排序图标改为默认

            });
            myTable2.off('order.dt search.dt').on('order.dt search.dt', function () {
                myTable2.column(0, {
                    search: 'applied',
                    order: 'applied'
                }).nodes().each(function (cell, i) {
                    cell.innerHTML = i + 1;
                });
            }).draw();
            $('#Ul-menu-text2 .toggle-vis').off('change').on('change', function (e) {
                var column = myTable2.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $("#keepOpen2").addClass("open");
            });
            $("#search_button2").off('click').on("click", function () {
                var tsval = $("#simpleQueryParam2").val();
                myTable2.search(tsval, false, false).draw();
            });
        },
        refreshTableMonth: function () {
            vehicleStatistical.monthClick(1);
        },
        export: function () {
            if ($("#dataTable2 tbody tr td").hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据，无法导出");
                return;
            }
            if (getRecordsNum('dataTable2_info') > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            // 请求路径
            var url = "/clbs/m/reportManagement/vehicleOnlineTime/exportMonitorData";
            // 请求参数
            var pdata = {
                "monitorIds": vehicleId.join(','),
                "month": currentMonth2.replace('-', ''),
                "simpleQueryParam": simpleQueryParam2,
                "module": '车辆在线统计',
            };
            json_ajax("post", url, "json", true, pdata, function (result) {
                if (result.success) {
                    layer.confirm(exportTitle, {
                        title: '操作确认',
                        icon: 3, // 问号图标
                        btn: ['确定', '导出管理'] //按钮
                    }, function () {
                        layer.closeAll();
                    }, function () {
                        layer.closeAll();
                        // 打开导出管理弹窗
                        pagesNav.showExportManager();
                    });
                } else if (result.msg) {
                    layer.msg(result.msg);
                }
            });
        },
    };

    /**
     * 按行政区域统计tab页签
     * */
    var myTable3;
    var currentMonth3 = null;
    var simpleQueryParam3;
    var searchParam = null;
    var areaSelectData = {
        city: [],
        county: [],
    };
    administrativeAreas = {
        validates: function () {
            return $("#hourslist3").validate({
                rules: {
                    month: {
                        required: true
                    },
                    province: {
                        required: true
                    }
                },
                messages: {
                    month: {
                        required: '请选择月份'
                    },
                    province: {
                        required: '请选择行政区域'
                    }
                }
            }).form();
        },
        paramRender: function (city, county) {
            var simpleQueryParam3 = $('#simpleQueryParam3').val();
            if (city) {
                if (county) {
                    if ($('#county').val().indexOf(simpleQueryParam3) === -1) {
                        county = '-1';
                    }
                } else {
                    var newCounty = [];
                    var dataList = areaSelectData.county;
                    for (var i = 1; i < dataList.length; i++) {
                        var item = $(dataList[i]);
                        if (item.text().indexOf(simpleQueryParam3) !== -1) {
                            newCounty.push(item.attr('data-code').substring(4));
                        }
                    }
                    county = newCounty.join(',') || '-1';
                }
            } else {
                var newCity = [];
                var dataList = areaSelectData.city;
                for (var i = 1; i < dataList.length; i++) {
                    var item = $(dataList[i]);
                    if (item.text().indexOf(simpleQueryParam3) !== -1) {
                        newCity.push(item.attr('data-code').substring(2, 4));
                    }
                }
                city = newCity.join(',') || '-1';
            }
            return {
                city,
                county
            };
        },
        monthClick: function (num) {
            if (num !== 1 && num !== 2) {
                publicObject.setNewMonth(num, '#month2')
            }
            if (!administrativeAreas.validates()) {
                searchParam = null;
                return;
            }
            currentMonth3 = $('#month2').val();
            // 请求路径
            var url = "/clbs/m/reportManagement/vehicleOnlineTime/getDivisionData";
            // 请求参数
            var province = $('#province1 option:selected').attr('data-code');
            var city = $('#city1 option:selected').attr('data-code').substring(2, 4);
            var county = $('#county option:selected').attr('data-code').substring(4);
            if (num === 2) {
                var data = administrativeAreas.paramRender(city, county);
                city = data.city;
                county = data.county;
            } else {
                areaSelectData = {
                    city: $('#city1 option'),
                    county: $('#county option'),
                };
                $('#simpleQueryParam3').val('');
                if (!city) {
                    var cityArr = [];
                    var cityOption = $('#city1 option');
                    for (var i = 1; i < cityOption.length; i++) {
                        var code = $(cityOption[i]).attr('data-code').substring(2, 4);
                        cityArr.push(code);
                    }
                    city = cityArr.join(',');
                }
                if (!county) {
                    var countryArr = [];
                    var countryOption = $('#county option');
                    for (var i = 1; i < countryOption.length; i++) {
                        var code = $(countryOption[i]).attr('data-code').substring(4);
                        countryArr.push(code);
                    }
                    county = countryArr.join(',');
                }
            }

            searchParam = {
                "provinceCode": province.substring(0, 2),
                "cityCodes": city,
                "countyCodes": county,
                "month": currentMonth3.replace('-', ''),
                // "dataLevel": county ? 3 : (city ? 2 : 1),
            };
            // 发送请求给服务器
            json_ajax("POST", url, "json", true, searchParam, administrativeAreas.tableRender);
        },
        getDataCol: function (dates, item, index) {
            var result = [index, item.divisionName];
            for (var i = 0; i < dates; i++) {
                result.push(item.days[i] || '');
            }
            result.push(item.total);
            return result;
        },
        tableRender: function (data) {
            // 组织表头
            var month = parseInt(currentMonth3.substr(5));
            var year = parseInt(currentMonth3.substr(0, 4));
            publicObject.renderTh('#dataTable3', year, month);
            var dates = publicObject.getMonthDay(year, month);
            publicObject.tableFilter('#dataTable3', '#Ul-menu-text3');
            administrativeAreas.getTable('#dataTable3');
            if (data.success == true) {
                var dataList = [];
                if (data.obj) {
                    for (var i = 0; i < data.obj.length; i++) {
                        var item = data.obj[i];
                        var index = i + 1;
                        var list = administrativeAreas.getDataCol(dates, item, index);
                        dataList.push(list); // 图表
                    }
                }
                administrativeAreas.reloadData(dataList);
            } else {
                layer.msg(data.msg || publicError, {
                    move: false
                });
            }
            $('#Ul-menu-text3 .toggle-vis').off('change').on('change', function (e) {
                var column = myTable3.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $("#keepOpen3").addClass("open");
            });
        },
        reloadData: function (dataList) {
            var currentPage = myTable3.page();
            myTable3.clear();
            myTable3.rows.add(dataList);
            myTable3.page(currentPage).draw(false);
            myTable.search('', false, false).page(currentPage).draw();
        },
        getTable: function (table) {
            $('#Ul-menu-text3 .toggle-vis').prop('checked', 'true');
            myTable3 = $(table).DataTable({
                "destroy": true,
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
                    "sInfoFiltered": "（数据库中共为 _MAX_ 条记录）",
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
                "columnDefs": [{
                    "targets": [0, 1, 3, 4, 5],
                    "searchable": false
                }],
                "order": [
                    [0, null]
                ], // 第一列排序图标改为默认

            });
            myTable3.off('order.dt search.dt').on('order.dt search.dt', function () {
                myTable3.column(0, {
                    search: 'applied',
                    order: 'applied'
                }).nodes().each(function (cell, i) {
                    cell.innerHTML = i + 1;
                });
            }).draw();
            $('#Ul-menu-text3 .toggle-vis').off('change').on('change', function (e) {
                var column = myTable3.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $("#keepOpen3").addClass("open");
            });
            $("#search_button3").off('click').on("click", function () {
                if (searchParam) {
                    simpleQueryParam3 = $("#simpleQueryParam3").val();
                    administrativeAreas.monthClick(simpleQueryParam3 ? 2 : 1);
                }
            });
        },
        // 刷新列表
        refreshTableMonth: function () {
            administrativeAreas.monthClick(1);
        },
        export: function () {
            if ($("#dataTable3 tbody tr td").hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据，无法导出");
                return;
            }
            if (getRecordsNum('dataTable3_info') > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var param = searchParam;
            param.module = '车辆在线时长统计';
            // 请求路径
            var url = "/clbs/m/reportManagement/vehicleOnlineTime/exportDivisionData";
            json_ajax("post", url, "json", true, searchParam, function (result) {
                if (result.success) {
                    layer.confirm(exportTitle, {
                        title: '操作确认',
                        icon: 3, // 问号图标
                        btn: ['确定', '导出管理'] //按钮
                    }, function () {
                        layer.closeAll();
                    }, function () {
                        layer.closeAll();
                        // 打开导出管理弹窗
                        pagesNav.showExportManager();
                    });
                } else if (result.msg) {
                    layer.msg(result.msg);
                }
            });
        },
    };

    $(function () {
        publicObject.init(); // 初始化页面

        $("#groupSelect,#groupSelect2").bind("click", showMenuContent); // 组织下拉显示
        $("#exportMileage").bind("click", roadTransportEnterprise.export);
        $("#exportMileage2").bind("click", vehicleStatistical.export);
        $("#exportMileage3").bind("click", administrativeAreas.export);

        $("#refreshTableMonth").bind("click", roadTransportEnterprise.refreshTableMonth);
        $("#refreshTableMonth2").bind("click", vehicleStatistical.refreshTableMonth);
        $("#refreshTableMonth3").bind("click", administrativeAreas.refreshTableMonth);

        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            var treeObj;
            if (id == 'groupSelect') {
                search_ztree('treeDemo', 'groupSelect', 'group');
                treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                treeObj.checkAllNodes(false);
            }
            if (id == 'groupSelect2') {
                vehicleStatistical.monitorTree();
            }
        });
        $('#simpleQueryParam').bind('keyup', function (event) {
            if (event.keyCode == "13") {
                //回车执行查询
                $('#search_button').click();
            }
        });
        $('#simpleQueryParam2').bind('keyup', function (event) {
            if (event.keyCode == "13") {
                //回车执行查询
                $('#search_button2').click();
            }
        });
        $('#simpleQueryParam3').bind('keyup', function (event) {
            if (event.keyCode == "13") {
                //回车执行查询
                $('#search_button3').click();
            }
        });
        $("#groupSelect").on('input propertychange', function (value) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.checkAllNodes(false);
            search_ztree('treeDemo', 'groupSelect', 'group');
        });
        var inputChange;
        $("#groupSelect2").on('input propertychange', function (value) {
            if (inputChange !== undefined) {
                clearTimeout(inputChange);
            }
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                searchFlag = true;
            }
            inputChange = setTimeout(function () {
                if (searchFlag) {
                    var param = $("#groupSelect2").val();
                    vehicleStatistical.searchVehicleTree(param);
                }
                searchFlag = true;
            }, 500);
        });
        $('#queryType').on('change', function () {
            var param = $("#groupSelect2").val();
            vehicleStatistical.searchVehicleTree(param);
        });
    })
})(window, $)