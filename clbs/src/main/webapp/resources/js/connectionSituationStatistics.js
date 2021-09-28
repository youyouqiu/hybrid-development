(function (window, $) {
    var table1;
    var tableDetail1;
    var table2;
    var tableDetail2;
    var $keepOpen = $(".keep-open");
    var clickedRowId
    var _detailTable1_start_index = 1
    var _detailTable1_rendered_index = 0
    window.connectionStatistics = {
        // 导出回调 公用
        exportCallback: function (data) {
            if (data.success) {
                layer.confirm("已加入到导出队列,请注意查看导出管理消息提醒", {
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
        },

        // 页面校验
        validate1: function () {
            return $("#alarmForm1").validate({
                rules: {
                    month1: {
                        required: true,
                    },
                    platformNum: {
                        required: true,
                    }
                },
                messages: {
                    month1: {
                        required: "请选择月份!",
                    },
                    platformNum: {
                        required: "请选择809转发平台",
                    }
                }
            }).form();
        },
        //刷新列表
        refreshTable1: function () {
            table1.requestData()
        },
        // 列表查询1
        query1: function (index) {
            if (!connectionStatistics.validate1()) {
                return
            }
            var $month1 = $('#month1'), month, newDate, inputMonth
            inputMonth = $month1.val()
            if (index != undefined) {
                if (index === 0) { //本月
                    newDate = new Date()
                } else {
                    month = inputMonth ? new Date(inputMonth).getMonth() : new Date().getMonth()
                    newDate = inputMonth ? new Date(new Date(inputMonth).setMonth(month + index)) : new Date(new Date().setMonth(month + index))
                }
                $month1.val(newDate.Format('yyyy-MM'))
            }
            connectionStatistics.doSearch1()
        },
        // 发送查询指令
        doSearch1: function () {
            if (!connectionStatistics.validate1()) {
                return
            }
            connectionStatistics.renderTable1()
            $('#dataTable1').on('click', 'tbody tr', connectionStatistics.listClick1);
            $('#exportAlarm1').attr('disabled', false)
        },
        // 导出按钮1
        exportAlarm1: function () {
            var length = $("#dataTable1 tbody tr").find("td").length;
            if (length <= 1) {
                layer.msg('无数据可以导出')
                return
            }
            if (getRecordsNum('dataTable1_info') > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            if (!connectionStatistics.validate1()) {
                return;
            }
            var url = "/clbs/m/reportManagement/connectionStatistics/exportPlatformList";
            var parameter = connectionStatistics.getSearchParams1()
            parameter.module = '连接情况统计'
            json_ajax("POST", url, "json", true, parameter, connectionStatistics.exportCallback);
        },
        // 渲染列表1
        renderTable1: function () {
            var columnDefs = [{
                //第一列，用来显示序号
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];
            var columns = [
                {
                    "data": null,
                    "class": "text-center"
                },
                {
                    "data": "t809PlatformName",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != undefined) {
                            return data
                        }
                        return '-'
                    }
                },
                {
                    "data": "connectionDate",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != undefined) {
                            return data
                        }
                        return '-'
                    }
                },
                {
                    "data": "countDay",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != undefined) {
                            return data
                        }
                        return '-'
                    }
                },
                {
                    "data": "onlineDuration",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != undefined) {
                            return data
                        }
                        return '-'
                    }
                },
                {
                    "data": "breakNum",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != undefined) {
                            return data
                        }
                        return '-'
                    }
                },
                {
                    "data": "breakDuration",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != undefined) {
                            return data
                        }
                        return '-'
                    }
                },
                {
                    "data": "onlineRate",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != undefined) {
                            return data + '%'
                        }
                        return '-'
                    }
                }
            ];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                var temp = connectionStatistics.getSearchParams1()
                d.t809platformIds = temp.t809platformIds;
                d.month = temp.month;
            };
            //表格setting
            var setting = {
                listUrl: "/clbs/m/reportManagement/connectionStatistics/platformList",
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable1', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                drawCallbackFun: function () {
                    var api = table1.dataTable;
                    var startIndex = api.context[0]._iDisplayStart;//获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                    });
                }
            };
            //创建表格
            table1 = new TG_Tabel.createNew(setting);
            table1.init();
        },
        // 列表行点击事件1
        listClick1: function () {
            $("#governmentModal").modal('show')
            connectionStatistics.renderDetail1()
        },
        // 渲染政府连接情况详细表格
        renderDetail1: function () {
            $('#exportDetail1').attr('disabled', true)
            var columnDefs = [{
                //第一列，用来显示序号
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];
            var columns = [
                {
                    "data": null,
                    "class": "text-center",
                    render: function () {
                        var res = _detailTable1_start_index + _detailTable1_rendered_index + 1
                        _detailTable1_rendered_index++
                        return res
                    }
                },
                {
                    "data": "day",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != undefined) {
                            return data
                        }
                        return '-'
                    }
                },
                {
                    "data": "onlineDuration",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != undefined) {
                            return data
                        }
                        return '-'
                    }
                },
                {
                    "data": "breakNum",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != undefined) {
                            return data
                        }
                        return '-'
                    }
                },
                {
                    "data": "breakDuration",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != undefined) {
                            return data
                        }
                        return '-'
                    }
                },
                {
                    "data": "onlineRate",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != undefined) {
                            return data + '%'
                        }
                        return '-'
                    }
                }
            ];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                var temp = connectionStatistics.getDetailSearchParams1()
                d.t809platformIds = temp.t809platformIds;
                d.startDate = temp.startDate;
                d.endDate = temp.endDate;
            };
            //表格setting
            var setting = {
                listUrl: "/clbs/m/reportManagement/connectionStatistics/platformDetailList",
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'governmentDetail', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                ajaxCallBack: function (data) {
                    _detailTable1_start_index = data.pageSize * (data.page - 1)
                    if (data && data.records.length > 0) {
                        $('#exportDetail1').attr('disabled', false)
                    }
                },
                drawCallbackFun: function () {
                    _detailTable1_rendered_index = 0
                }
            };
            //创建表格
            tableDetail1 = new TG_Tabel.createNew(setting);
            tableDetail1.init();
        },
        // 导出详情1
        exportDetail1: function () {
            if (!connectionStatistics.validate1()) {
                return;
            }
            if (getRecordsNum('governmentDetail_info') > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var url = "/clbs/m/reportManagement/connectionStatistics/exportPlatformDetail";
            var parameter = connectionStatistics.getDetailSearchParams1()
            parameter.module = '连接情况统计'
            json_ajax("POST", url, "json", true, parameter, connectionStatistics.exportCallback);
        },
        getSearchParams1: function () {
            return {
                simpleQueryParam: $('#simpleQueryParam1').val(),
                t809platformIds: $('#platformNum').attr('data-id'),
                month: new Date($('#month1').val()).Format('yyyyMM'),
            }
        },
        getDetailSearchParams1: function () {
            var date = new Date($('#month1').val())
            var endDay = new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate()
            return {
                t809platformIds: $('#platformNum').attr('data-id'),
                startDate: date.Format('yyyyMM') + '01',
                endDate: date.Format('yyyyMM') + endDay
            }
        },

        // 页面校验
        validate2: function () {
            return $("#alarmForm2").validate({
                rules: {
                    month2: {
                        required: true,
                    },
                    groupSelect: {
                        required: true,
                    }
                },
                messages: {
                    month2: {
                        required: "请选择月份!",
                    },
                    groupSelect: {
                        required: "请选择监控对象",
                    }
                }
            }).form();
        },
        //刷新列表
        refreshTable2: function () {
            var url = "/clbs/m/reportManagement/connectionStatistics/monitorList";
            $('#simpleQueryParam2').val('');
            var parameter = connectionStatistics.getSearchParams2();
            connectionStatistics.setTableData(table2, url, parameter)
        },
        // 列表查询2
        query2: function (index) {
            if (!connectionStatistics.validate2()) {
                return
            }
            var $month2 = $('#month2'), month, newDate, inputMonth
            inputMonth = $month2.val()
            if (index != undefined) {
                if (index === 0) { //本月
                    newDate = new Date()
                } else {
                    month = inputMonth ? new Date(inputMonth).getMonth() : new Date().getMonth()
                    newDate = inputMonth ? new Date(new Date(inputMonth).setMonth(month + index)) : new Date(new Date().setMonth(month + index))
                }
                $month2.val(newDate.Format('yyyy-MM'))
            }
            connectionStatistics.doSearch2()
        },
        // 发送查询指令
        doSearch2: function () {
            if (!connectionStatistics.validate2()) {
                return
            }
            connectionStatistics.renderTable2()
            $('#exportAlarm2').attr('disabled', false)
        },
        // 导出按钮2
        exportAlarm2: function () {
            var length = $("#dataTable2 tbody tr").find("td").length;
            if (length <= 1) {
                layer.msg('无数据可以导出')
                return
            }
            if (getRecordsNum('dataTable2_info') > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            if (!connectionStatistics.validate2()) {
                return;
            }
            var parameter = connectionStatistics.getSearchParams2()
            parameter.module = '连接情况统计'
            var url = "/clbs/m/reportManagement/connectionStatistics/exportMonitorList";
            json_ajax("POST", url, "json", true, parameter, connectionStatistics.exportCallback);
        },
        // 渲染列表2
        renderTable2: function () {
            var url = "/clbs/m/reportManagement/connectionStatistics/monitorList"
            var parameter = connectionStatistics.getSearchParams2()
            if (!table2) {
                var columnDefine = [
                    {
                        "data": null,
                        "class": "text-center"
                    },
                    {
                        "data": "monitorName",
                        "class": "text-center",
                        render: function (data, type, row, meta) {
                            if (data != undefined) {
                                return data
                            }
                            return '-'
                        }
                    },
                    {
                        "data": "countDays",
                        "class": "text-center",
                        render: function (data, type, row, meta) {
                            if (data != undefined) {
                                return data
                            }
                            return '-'
                        }
                    },
                    {
                        "data": "onlineDuration",
                        "class": "text-center",
                        render: function (data, type, row, meta) {
                            if (data != undefined) {
                                return data
                            }
                            return '-'
                        }
                    },
                    {
                        "data": "offlineDuration",
                        "class": "text-center",
                        render: function (data, type, row, meta) {
                            if (data != undefined) {
                                return data
                            }
                            return '-'
                        }
                    },
                    {
                        "data": "onlineRate",
                        "class": "text-center",
                        render: function (data, type, row, meta) {
                            if (data != undefined) {
                                return data + '%'
                            }
                            return '-'
                        }
                    }
                ];
                table2 = $("#dataTable2").DataTable({
                    "destroy": true,
                    "columns": columnDefine,
                    "dom": 'tiprl',// 自定义显示项
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
                    "order": [
                        [0, null]
                    ],// 第一列排序图标改为默认
                });
                table2.on('order.dt search.dt', function () {
                    table2.column(0, {
                        search: 'applied',
                        order: 'applied'
                    }).nodes().each(function (cell, i) {
                        cell.innerHTML = i + 1;
                    });
                }).draw();
                $("#search_button2").on("click", function () {
                    var val = $("#simpleQueryParam2").val()
                    table2.columns(1).search(val, false, false).draw();
                });
            }
            connectionStatistics.setTableData(table2, url, parameter)
        },
        setTableData: function (_table, url, parameter, cb) {
            json_ajax("POST", url, "json", true, parameter, function (res) {
                if (res.success) {
                    var dataList = res.obj.data;
                    if (!dataList) return;
                    dataList.sort(function (a, b) {
                        return b.onlineRate - a.onlineRate
                    });
                    _table.clear();
                    _table.rows.add(dataList).draw();
                    if (dataList.length > 0) {
                        if (cb) {
                            cb()
                        }
                    }
                } else {
                    layer.msg(res.msg, {move: false});
                }
            });
        },
        // 列表行点击事件1
        listClick2: function () {
            var data = table2.row(this).data();
            clickedRowId = data.monitorId
            $("#terminalModal").modal('show')
            connectionStatistics.renderDetail2()
        },
        // 渲染政府连接情况详细表格
        renderDetail2: function () {
            $('#exportDetail2').attr('disabled', true)
            var url = "/clbs/m/reportManagement/connectionStatistics/monitorDetailList"
            var parameter = connectionStatistics.getDetailSearchParams2()
            var parserDay = function (day) { //yyyyMMdd
                var y = day.substring(0, 4)
                var m = day.substring(4, 6)
                var d = day.substring(6, 8)
                return y + '-' + m + '-' + d
            }
            if (!tableDetail2) {
                var columnDefine = [
                    {
                        "data": null,
                        "class": "text-center"
                    },
                    {
                        "data": "day",
                        "class": "text-center",
                        render: function (data, type, row, meta) {
                            if (data != undefined) {
                                if (data.indexOf('-') == -1) {
                                    return parserDay(data)
                                }
                                return data
                            }
                            return '-'
                        }
                    },
                    {
                        "data": "onlineDuration",
                        "class": "text-center",
                        render: function (data, type, row, meta) {
                            if (data != undefined) {
                                return data
                            }
                            return '-'
                        }
                    },
                    {
                        "data": "offlineDuration",
                        "class": "text-center",
                        render: function (data, type, row, meta) {
                            if (data != undefined) {
                                return data
                            }
                            return '-'
                        }
                    },
                    {
                        "data": "onlineRate",
                        "class": "text-center",
                        render: function (data, type, row, meta) {
                            if (data != undefined) {
                                return data + '%'
                            }
                            return '-'
                        }
                    }
                ];
                tableDetail2 = $("#terminalDetail").DataTable({
                    "destroy": true,
                    "columns": columnDefine,
                    "dom": 'tiprl',// 自定义显示项
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
                    "order": [
                        [0, null]
                    ],// 第一列排序图标改为默认
                });
                tableDetail2.on('order.dt search.dt', function () {
                    tableDetail2.column(0, {
                        search: 'applied',
                        order: 'applied'
                    }).nodes().each(function (cell, i) {
                        cell.innerHTML = i + 1;
                    });
                }).draw();
            }

            function _setTableData(_table, url, parameter, cb) {
                json_ajax("POST", url, "json", true, parameter, function (res) {
                    if (res.success) {
                        var dataList = res.obj.data[0].dayDetailList
                        if (!dataList) return
                        dataList.sort(function (a, b) {
                            return b.onlineRate - a.onlineRate
                        })
                        _table.clear();
                        _table.rows.add(dataList).draw();
                        if (dataList.length > 0) {
                            if (cb) {
                                cb()
                            }
                        }
                    } else {
                        layer.msg(res.msg, {move: false});
                    }
                });
            }

            _setTableData(tableDetail2, url, parameter, function () {
                $('#exportDetail2').attr('disabled', false)
            })
        },
        // 导出按钮2
        exportDetail2: function () {
            if (!connectionStatistics.validate2()) {
                return;
            }
            if (getRecordsNum('terminalDetail_info') > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var parameter = connectionStatistics.getDetailSearchParams2()
            parameter.module = '连接情况统计'
            var url = "/clbs/m/reportManagement/connectionStatistics/exportMonitorDetail";
            json_ajax("POST", url, "json", true, parameter, connectionStatistics.exportCallback);
        },
        getSearchParams2: function () {
            var getMonitorIds = function () {
                var treeObj = $.fn.zTree.getZTreeObj('treeDemo');
                var nodes = treeObj ? treeObj.getCheckedNodes(true) : [];
                nodes = nodes.filter(function (item) {
                    return item.type == 'vehicle'
                })
                nodes = connectionStatistics.uniqueBy(nodes, 'id');
                return nodes
            };
            return {
                simpleQueryParam: $('#simpleQueryParam2').val(),
                monitorIds: getMonitorIds().map(function (item) {
                    return item.id
                }).join(','),
                month: new Date($('#month2').val()).Format('yyyyMM'),
            }
        },
        getDetailSearchParams2: function () {
            var date = new Date($('#month2').val())
            var endDay = new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate()
            return {
                monitorIds: clickedRowId,
                month: $('#month2').val().replace('-', ''),
                startDate: date.Format('yyyyMM') + '01',
                endDate: date.Format('yyyyMM') + endDay,
                module: "连接情况统计",
            }
        },
        uniqueBy: function (objArr, name) {
            var s = []
            var res = []
            objArr.forEach(function (item) {
                if (!s.includes(item[name])) {
                    res.push(item)
                }
            })
            return res
        }
    }

    window.treeUtil = {
        // 获取子节点
        getAllChildNodes: function (treeNode, isIncludeParent) {
            var result = []

            function getAll(treeNode) {
                if (treeNode.children) {
                    treeNode.children.forEach(function (item) {
                        if (isIncludeParent) {
                            result.push(treeNode);
                        }
                        getAll(item);
                    })
                } else {
                    result.push(treeNode);
                }
            }

            getAll(treeNode)
            return result;
        },
        // 获取勾选节点
        getCheckedNodes: function (id) {
            var treeObj = $.fn.zTree.getZTreeObj(id);
            var nodes = treeObj ? treeObj.getCheckedNodes(true) : []
            nodes = nodes.filter(function (item) {
                return item.type == 'vehicle'
            })
            nodes = treeUtil.uniqueBy(nodes, 'id')
            return nodes
        },
        setInputValue: function (treeId, inputId) {
            var checkedNodes = treeUtil.getCheckedNodes(treeId)
            var names = ''
            checkedNodes.forEach(function (item) {
                if (item.type == 'vehicle') {
                    names += item.name + ','
                }
            })
            $('#' + inputId).val(names)
        },
        uniqueBy: function (objArr, name) {
            var s = []
            var res = []
            objArr.forEach(function (item) {
                if (!s.includes(item[name])) {
                    res.push(item)
                    s.push(item[name])
                }
            })
            return res
        }
    }

    window.tree = {
        // 默认树
        init: function () {
            var setting = {
                async: {
                    url: tree.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: tree.ajaxDataFilter
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
                    beforeCheck: tree.zTreeBeforeCheck,
                    onCheck: tree.onCheck,
                    onExpand: tree.zTreeOnExpand
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },
        // 搜索树
        searchTree: function (param) {
            if (param == null || param == undefined || param == '') {
                tree.init();
                return
            }
            var querySetting = {
                async: {
                    url: "/clbs/a/search/reportFuzzySearch",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": $('#queryType').val(), "queryParam": param, "queryType": "vehicle"},
                    dataFilter: tree.ajaxDataFilterSearch
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
                    beforeCheck: function (treeId, treeNode) {
                        tree.zTreeBeforeCheck(treeId, treeNode, true)
                    },
                    onCheck: tree.onCheck,
                }
            };
            $.fn.zTree.init($("#treeDemo"), querySetting, null);
        },
        // 获取树接口地址
        getTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle";
            }
        },
        // 服务器数据预处理
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            if (responseData.msg) {
                var obj = JSON.parse(ungzip(responseData.msg));
                var data;
                if (obj.tree != null && obj.tree != undefined) {
                    data = obj.tree;
                } else {
                    data = obj
                }
                for (var i = 0; i < data.length; i++) {
                    data[i].open = true;
                }
            }
            return data;
        },
        // 服务器数据预处理(搜索树)
        ajaxDataFilterSearch: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData))
            var nodesArr;
            if ($('#queryType').val() == "vehicle") {
                nodesArr = filterQueryResult(responseData);
            } else {
                nodesArr = responseData;
            }
            for (var i = 0; i < nodesArr.length; i++) {
                nodesArr[i].open = true;
            }
            return nodesArr;
        },
        zTreeBeforeCheck: function (treeId, treeNode, searchTree) {
            var currentCheckedNodesLength = 0 //已被勾选的节点数量
            var toCheckedNodesLength = 0 //即将被勾选的节点数量
            if (treeNode.checked) return true
            currentCheckedNodesLength = treeUtil.getCheckedNodes('treeDemo').length
            if (["group", "assignment"].includes(treeNode.type) && !searchTree) { //1.勾选的为组织或分组 2.搜索树是直接加载了全部的节点，不用再去服务器校验
                var parameter = {"parentId": treeNode.id, "type": treeNode.type}
                json_ajax("post", "/clbs/m/personalized/ico/getVehicleCount", "json", false, parameter, function (data) {
                    if (data.success) {
                        toCheckedNodesLength = data.obj;
                    }
                });
            } else {
                toCheckedNodesLength = 1;
            }
            if (currentCheckedNodesLength + toCheckedNodesLength > TREE_MAX_CHILDREN_LENGTH) {
                layer.msg('最多勾选' + TREE_MAX_CHILDREN_LENGTH + '个监控对象');
                return false;
            }
        },
        onCheck: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    connectionStatistics.validate2();
                }, 600);
            }
            treeUtil.setInputValue('treeDemo', 'groupSelect')
        },
        zTreeOnExpand: function (event, treeId, treeNode) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (treeNode.type == "group") {
                var url = "/clbs/m/functionconfig/fence/bindfence/getOrgAssignmentVehicle";
                var parameter = {
                    "groupId": treeNode.id,
                    "isChecked": treeNode.checked,
                    "monitorType": "0"
                }
                json_ajax("post", url, "json", false, parameter, function (data) {
                    var result = data.obj;
                    if (!result) return
                    for (var key in result) {
                        if (result.hasOwnProperty(key)) {
                            var parentNode = treeObj.getNodeByParam("id", key, null);
                            if (parentNode && !parentNode.children) {
                                parentNode.zAsync = true;
                                treeObj.addNodes(parentNode, 0, result[key]);
                            }
                        }
                    }
                })
            }
            treeUtil.setInputValue('treeDemo', 'groupSelect')
        }
    }

    function debounce(fn, wait) {
        var timerId = null;
        return function () {
            clearTimeout(timerId);
            timerId = setTimeout(function () {
                fn.apply(this, arguments)
            }, wait)
        }
    }

    $(function () {
        tree.init();
        laydate.render({
            elem: '#month1,#month2',
            type: 'month',
            value: new Date().Format('yyyy-MM'),
            max: new Date().Format('yyyy-MM')
        });
        // 809转发平台列表渲染
        var url = "/clbs/m/connectionparamsset/list";
        json_ajax("POST", url, "json", true, null, function (data) {
            var datas = data.obj;
            var dataList = {value: []}, i = datas.length;
            while (i--) {
                dataList.value.push({
                    name: datas[i].platformName,
                    id: datas[i].id
                });
            }
            $("#platformNum").bsSuggest({
                indexId: 1, //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                idField: "id",
                keyField: "name",
                effectiveFields: ["name"],
                searchFields: ["id"],
                data: dataList
            })
        });
        // tab切换事件
        $('#stretch li').on('click', function () {
            if ($(this).index() == 0) {
                $('#governmentData').show()
                $('#terminalData').hide()
            } else {
                $('#governmentData').hide()
                $('#terminalData').show()
            }
        })
        $('input').inputClear().on('onClearEvent', function (e, data) {
            if (data.id == 'groupSelect') {
                tree.searchTree($("#groupSelect").val());
            }
        });

        $("body").on('click', function (e) {
            if (!$("#Ul-menu-text1")[0].contains(e.target)) {
                $("#Ul-menu-text1").css('display', 'none')
            }
        })
        $("#customizeColumns1").on('click', function (e) {
            $("#Ul-menu-text1").toggle()
            e.stopPropagation()
        })

        // 显示隐藏列 1
        $('.toggle-vis-1').on('change', function (e) {
            var column = table1.dataTable.column($(this).attr('data-column'));
            column.visible(!column.visible());
            $keepOpen.addClass("open");
        });
        // 列表点击显示详情 1
        $('#dataTable1').on('click', 'tbody tr', connectionStatistics.listClick1);

        // 显示隐藏列2
        $('.toggle-vis-2').on('change', function (e) {
            var column = table2.column($(this).attr('data-column'));
            column.visible(!column.visible());
            $keepOpen.addClass("open");
        });
        // 列表点击显示详情 2
        $('#dataTable2').on('click', 'tbody tr', connectionStatistics.listClick2);
        $('#search_button2').on('click', function () {
            connectionStatistics.query2()
        })
        $('#simpleQueryParam2').on('keydown', function (e) {
            if (e.keyCode == 13) {
                connectionStatistics.query2()
            }
        })
        // 树交互事件
        $('#groupSelect').on('click', showMenuContent);
        $("#groupSelect").on('input propertychange', debounce(function () {
            tree.searchTree($("#groupSelect").val());
        }, 500));
        $('#queryType').on('change', function () {
            tree.searchTree($("#groupSelect").val());
        });
    })
}(window, $))
