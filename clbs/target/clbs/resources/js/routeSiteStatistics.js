(function (window, $) {
    var table1;
    var table2;
    var table3;
    var checkFlag = false; //判断组织节点是否是勾选操作
    var size; //当前权限监控对象数量
    var zTreeIdJson = {};
    var crrentSubV = []; //模糊查询
    var ifAllCheck = true; //刚进入页面小于TREE_MAX_CHILDREN_LENGTH自动勾选
    var dbValue = false //树双击判断参数

    var yesterday = new Date(new Date() - 24 * 60 * 60 * 1000)

    window.routeSiteStatistics = {
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
        getDate: function (number, referDate) {
            if (referDate) {
                referDate = new Date(referDate).Format('yyyy-MM-dd')
            } else {
                referDate = new Date().Format('yyyy-MM-dd')
            }
            var targetDate = new Date(referDate).getTime() + 24 * 60 * 60 * 1000 * number
            return new Date(targetDate).Format('yyyy-MM-dd')
        },
        setDateRangeValue: function (id, number, isShowHMS) {
            if (number == undefined) {
                return
            }
            var referDate = $('#' + id).val().split('--') // 参考日期
            var endDate = routeSiteStatistics.getDate(-1, referDate[0].split(' ')[0])
            var startDate = routeSiteStatistics.getDate(number + 1, endDate)
            var startTime = number == 0 ? referDate[0].split(' ')[1] : '00:00:00'
            var endTime = number == 0 ? referDate[1].split(' ')[1] : '23:59:59'
            if (number === 0) {
                startDate = routeSiteStatistics.getDate(0, new Date())
                endDate = routeSiteStatistics.getDate(0, new Date())
            }
            if (isShowHMS) {
                $('#' + id).val(startDate + ' ' + startTime + '--' + endDate + ' ' + endTime);
            } else {
                $('#' + id).val(startDate + '--' + endDate);
            }
        },

        // 页面校验
        validate1: function () {
            return $("#alarmForm1").validate({
                rules: {
                    groupSelect1: {
                        required: true,
                    }
                },
                messages: {
                    groupSelect1: {
                        required: "请选择组织",
                    }
                }
            }).form();
        },
        //刷新列表
        refreshTable1: function () {
            $('#simpleQueryParam1').val('');
            table1.requestData()
        },
        // 列表查询1
        query1: function (number) {
            routeSiteStatistics.setDateRangeValue('dateRange1', number, false)
            routeSiteStatistics.doSearch1()
        },
        // 发送查询指令
        doSearch1: function () {
            if (!routeSiteStatistics.validate1()) {
                return
            }
            routeSiteStatistics.renderTable1()
            $('#exportAlarm1').attr('disabled', false)
        },
        // 导出按钮1
        exportAlarm1: function () {
            var length = $("#dataTable1 tbody tr").find("td").length;
            if (length <= 1) {
                layer.msg('无数据可以导出')
                return
            }
            if (!routeSiteStatistics.validate1()) {
                return;
            }
            if (getRecordsNum('dataTable1_info') > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var temp = routeSiteStatistics.getSearchParams(1)
            var parameter = {
                organizationIds: temp.orgIds,
                simpleQueryParam: temp.simpleQueryParam,
                startDate: temp.startTime,
                endDate: temp.endTime,
                module: '途经点统计报表'
            }
            var url = "/clbs/m/reportManagement/point/exportOrgData";
            json_ajax("POST", url, "json", true, parameter, routeSiteStatistics.exportCallback);
        },
        // 渲染列表1
        renderTable1: function () {
            var columnDefs = [{
                //第一列，用来显示序号
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];
            var columns = [{
                    "data": null,
                    "class": "text-center"
                },
                {
                    "data": "orgName",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != undefined) {
                            return data
                        }
                        return '-'
                    }
                },
                {
                    "data": "passPointName",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != undefined) {
                            return data
                        }
                        return '-'
                    }
                },
                {
                    "data": "passNum",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != undefined) {
                            return data
                        }
                        return '-'
                    }
                }
            ];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                var params = routeSiteStatistics.getSearchParams(1)
                d.organizationIds = params.orgIds;
                d.simpleQueryParam = params.simpleQueryParam;
                d.startDate = params.startTime;
                d.endDate = params.endTime;
            };
            //表格setting
            var setting = {
                listUrl: "/clbs/m/reportManagement/point/getOrgData",
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable1', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                drawCallbackFun: function () {
                    var api = table1.dataTable;
                    var startIndex = api.context[0]._iDisplayStart; //获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                    });
                }
            };
            //创建表格
            table1 = new TG_Tabel.createNew(setting);
            table1.init();
        },


        //公用
        getSearchParams: function (index, withTime) {
            var orgIds = treeUtil.getCheckedNodes('treeDemo' + index)
            var date = $('#dateRange' + index).val().split('--')
            var simpleQueryParam = $('#simpleQueryParam' + index).val()
            orgIds = orgIds.map(function (item) {
                if (index == 1) {
                    return item.uuid
                }
                return item.id
            })
            return {
                orgIds: orgIds.join(),
                startTime: withTime ? new Date(date[0]).Format('yyyyMMddhhmmss') : new Date(date[0]).Format('yyyyMMdd'),
                endTime: withTime ? new Date(date[1]).Format('yyyyMMddhhmmss') : new Date(date[1]).Format('yyyyMMdd'),
                simpleQueryParam: simpleQueryParam
            }
        },

        // 页面校验 2
        validate2: function () {
            return $("#alarmForm2").validate({
                rules: {
                    groupSelect2: {
                        required: true,
                    }
                },
                messages: {
                    groupSelect2: {
                        required: "请选择监控对象",
                    }
                }
            }).form();
        },
        //刷新列表2
        refreshTable2: function () {
            $('#simpleQueryParam2').val('');
            table2.requestData()
        },
        // 列表查询2
        query2: function (number) {
            routeSiteStatistics.setDateRangeValue('dateRange2', number, false)
            routeSiteStatistics.doSearch2()
        },
        // 发送查询指令
        doSearch2: function () {
            if (!routeSiteStatistics.validate2()) {
                return
            }
            routeSiteStatistics.renderTable2()
            $('#exportAlarm2').attr('disabled', false)
        },
        // 导出按钮2
        exportAlarm2: function () {
            var length = $("#dataTable2 tbody tr").find("td").length;
            if (length <= 1) {
                layer.msg('无数据可以导出')
                return
            }
            if (!routeSiteStatistics.validate2()) {
                return;
            }
            if (getRecordsNum('dataTable2_info') > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var temp = routeSiteStatistics.getSearchParams(2)
            var parameter = {
                monitorIds: temp.orgIds,
                simpleQueryParam: temp.simpleQueryParam,
                startDate: temp.startTime,
                endDate: temp.endTime,
                module: '途经点统计报表'
            }
            var url = "/clbs/m/reportManagement/point/exportMonitorData";
            json_ajax("POST", url, "json", true, parameter, routeSiteStatistics.exportCallback);
        },
        // 渲染列表2
        renderTable2: function () {
            var columnDefs = [{
                //第一列，用来显示序号
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];
            var columns = [{
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
                    "data": "passPointName",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != undefined) {
                            return data
                        }
                        return '-'
                    }
                },
                {
                    "data": "passNum",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != undefined) {
                            return data
                        }
                        return '-'
                    }
                }
            ];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                var params = routeSiteStatistics.getSearchParams(2)
                d.monitorIds = params.orgIds;
                d.simpleQueryParam = params.simpleQueryParam;
                d.startDate = params.startTime;
                d.endDate = params.endTime;
            };
            //表格setting
            var setting = {
                listUrl: "/clbs/m/reportManagement/point/getMonitorData",
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable2', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                drawCallbackFun: function () {
                    var api = table2.dataTable;
                    var startIndex = api.context[0]._iDisplayStart; //获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                    });
                }
            };
            //创建表格
            table2 = new TG_Tabel.createNew(setting);
            table2.init();
        },

        // 页面校验3
        validate3: function () {
            return $("#alarmForm3").validate({
                rules: {
                    groupSelect3: {
                        required: true,
                    }
                },
                messages: {
                    groupSelect3: {
                        required: "请选择监控对象",
                    }
                }
            }).form();
        },
        //刷新列表3
        refreshTable3: function () {
            $('#simpleQueryParam3').val('');
            table3.requestData()
        },
        // 列表查询3
        query3: function (number) {
            routeSiteStatistics.setDateRangeValue('dateRange3', number, true)
            routeSiteStatistics.doSearch3()
        },
        // 发送查询指令
        doSearch3: function () {
            if (!routeSiteStatistics.validate3()) {
                return
            }
            var parameter = routeSiteStatistics.getSearchParams(3, true)
            routeSiteStatistics.renderTable3()
            $('#exportAlarm3').attr('disabled', false)
        },
        // 导出按钮3
        exportAlarm3: function () {
            var length = $("#dataTable3 tbody tr").find("td").length;
            if (length <= 1) {
                layer.msg('无数据可以导出')
                return
            }
            if (!routeSiteStatistics.validate3()) {
                return;
            }
            if (getRecordsNum('dataTable3_info') > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var temp = routeSiteStatistics.getSearchParams(3, true)
            var parameter = {
                monitorIds: temp.orgIds,
                simpleQueryParam: temp.simpleQueryParam,
                startTime: temp.startTime,
                endTime: temp.endTime,
                module: '途经点统计报表'
            }
            var url = "/clbs/m/reportManagement/point/exportMonitorDetail";
            json_ajax("POST", url, "json", true, parameter, routeSiteStatistics.exportCallback);
        },
        // 渲染列表3
        renderTable3: function () {
            var columnDefs = [{
                //第一列，用来显示序号
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];
            var columns = [{
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
                    "data": "passPointName",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != undefined) {
                            return data
                        }
                        return '-'
                    }
                },
                {
                    "data": "passTime",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != undefined) {
                            return routeSiteStatistics.parserDate(data)
                        }
                        return '-'
                    }
                }
            ];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                var params = routeSiteStatistics.getSearchParams(3, true)
                d.monitorIds = params.orgIds;
                d.simpleQueryParam = params.simpleQueryParam;
                d.startTime = params.startTime;
                d.endTime = params.endTime;
            };
            //表格setting
            var setting = {
                listUrl: "/clbs/m/reportManagement/point/getMonitorDetailData",
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable3', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true
            };
            //创建表格
            table3 = new TG_Tabel.createNew(setting);
            table3.init();
        },
        parserDate: function (dateStr) { //yyyyMMddHHmmss
            if (!dateStr) return ''
            dateStr += ''
            var yyyy = dateStr.substring(0, 4)
            var MM = dateStr.substring(4, 6)
            var dd = dateStr.substring(6, 8)
            var hh = dateStr.substring(8, 10)
            var mm = dateStr.substring(10, 12)
            var ss = dateStr.substring(12, 14)
            return yyyy + '-' + MM + '-' + dd + ' ' + hh + ':' + mm + ':' + ss
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
            if (id != 'treeDemo1') {
                nodes = nodes.filter(function (item) {
                    return item.type == 'vehicle'
                })
                nodes = routeSiteStatistics.uniqueBy(nodes, 'id')
            }
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
    }
    window.tree1 = {
        init: function () {
            var setting = {
                async: {
                    url: "/clbs/m/basicinfo/enterprise/professionals/tree",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "type": "multiple",
                        "icoType": "0"
                    },
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
                    onAsyncSuccess: tree1.zTreeOnAsyncSuccess,
                    beforeCheck: tree1.zTreeBeforeCheck,
                    onCheck: tree1.onCheck,
                    onDblClick: tree1.onDblClickVehicle

                }
            };
            $.fn.zTree.init($("#treeDemo1"), setting, null);
        },
        onDblClickVehicle: function (e, treeId, treeNode) {
            if (!treeNode) return
            dbValue = true
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo1");
            treeObj.checkAllNodes(false);
            tree1.getCheckedNodes("treeDemo1");
            // $("#groupSelect1").val(treeNode.name);
            // search_ztree('treeDemo1', 'groupSelect1', 'group');
            treeObj.checkNode(treeNode, !treeNode.checked, false, true);
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            treeObj.expandAll(true);
            var rootNode = treeObj.getNodes();
            var nodes = treeObj.transformToArray(rootNode);
            if (nodes.length <= TREE_MAX_CHILDREN_LENGTH) {
                treeObj.checkAllNodes(true);
            }
            tree1.onCheck()
        },
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
        zTreeBeforeCheck: function (treeId, treeNode) {
            var currentCheckedNodesLength = 0 //已被勾选的节点数量
            var toCheckedNodesLength = 0 //即将被勾选的节点数量
            if (!treeNode.checked && !dbValue) {
                currentCheckedNodesLength = tree1.getCheckedNodes('treeDemo1').length
                toCheckedNodesLength = tree1.getAllChildNodes(treeNode).length
                if (currentCheckedNodesLength + toCheckedNodesLength > TREE_MAX_CHILDREN_LENGTH) {
                    layer.msg('最多勾选' + TREE_MAX_CHILDREN_LENGTH + '个组织' + '<br/>双击名称可选中本组织');
                    return false;
                }
            }

        },
        onCheck: function (event, treeId, treeNode) {
            dbValue = false;
            console.log(treeNode, 'treeNode')
            var checkedNodes = treeUtil.getCheckedNodes('treeDemo1')
            var names = ''
            checkedNodes.forEach(function (item) {
                names += item.name + ','
            });
            if (treeNode && treeNode.checked) {
                setTimeout(() => {
                    routeSiteStatistics.validate1();
                }, 600);
            }
            $('#groupSelect1').val(names);
        },
        searchTree: function (treeId, inputId) {
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            search_ztree(treeId, inputId, 'group');
            treeObj.checkAllNodes(false);
        },

    }
    window.tree2 = {
        // 默认树
        init: function () {
            var setting = {
                async: {
                    url: tree2.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "type": "multiple",
                        "icoType": "0"
                    },
                    dataFilter: tree2.ajaxDataFilter
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
                    beforeCheck: tree2.zTreeBeforeCheck,
                    onCheck: tree2.onCheck,
                    onExpand: tree2.zTreeOnExpand
                }
            };
            $.fn.zTree.init($("#treeDemo2"), setting, null);
        },
        // 搜索树
        searchTree: function (param) {
            ifAllCheck = false; //模糊查询不自动勾选
            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                tree2.init();
                return
            }
            var querySetting = {
                async: {
                    url: "/clbs/a/search/reportFuzzySearch",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "type": $('#queryType2').val(),
                        "queryParam": param,
                        "queryType": "vehicle"
                    },
                    dataFilter: tree2.ajaxDataFilterSearch
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
                    beforeCheck: tree2.zTreeBeforeCheck,
                    onCheck: tree2.onCheck,
                }
            };
            $.fn.zTree.init($("#treeDemo2"), querySetting, null);
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
        // 服务器数据预处理(搜索树)
        ajaxDataFilterSearch: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData))
            var nodesArr;
            if ($('#queryType2').val() == "vehicle") {
                nodesArr = filterQueryResult(responseData, crrentSubV);
            } else {
                nodesArr = responseData;
            }
            for (var i = 0; i < nodesArr.length; i++) {
                nodesArr[i].open = true;
            }
            return nodesArr;
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
                    if (ifAllCheck) {
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
                    } else {
                        var nodes = zTree.getNodesByFilter(function (node) {
                            return node.type == "people" || node.type == "vehicle";
                        }, false, treeNode); // 查找节点集合
                        nodesLength = nodes.length;
                    }
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
        onCheck: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo2");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    routeSiteStatistics.validate2();
                }, 600);
            }
            treeUtil.setInputValue('treeDemo2', 'groupSelect2')
        },
        zTreeOnExpand: function (event, treeId, treeNode) {
            //判断是否是勾选操作展开的树(是则继续执行，不是则返回)
            if (treeNode.type == "group" && !checkFlag) {
                return;
            }
            //初始化勾选操作判断表示
            checkFlag = false;
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo2");
            if (treeNode.type == "group") {
                var url = "/clbs/m/functionconfig/fence/bindfence/getOrgAssignmentVehicle";
                json_ajax("post", url, "json", false, {
                    "groupId": treeNode.id,
                    "isChecked": treeNode.checked,
                    "monitorType": "0"
                }, function (data) {
                    var result = data.obj;
                    if (result != null && result != undefined) {
                        $.each(result, function (i) {
                            var pid = i; //获取键值
                            var chNodes = result[i]; //获取对应的value
                            if (zTreeIdJson[pid] != undefined) {
                                var parentTid = zTreeIdJson[pid][0];
                                var parentNode = treeObj.getNodeByTId(parentTid);
                                if (parentNode.children === undefined) {
                                    parentNode.zAsync = true;
                                    treeObj.addNodes(parentNode, 0, chNodes);
                                }
                            }
                        });
                    }
                })
            }
            treeUtil.setInputValue('treeDemo2', 'groupSelect2')
        }
    }
    window.tree3 = {
        // 默认树
        init: function () {
            var setting = {
                async: {
                    url: tree3.getAlarmReportTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "type": "multiple",
                        "icoType": "0"
                    },
                    dataFilter: tree3.ajaxDataFilter
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
                    // beforeClick: tree.beforeClickVehicle,
                    beforeCheck: tree3.zTreeBeforeCheck,
                    onCheck: tree3.onCheck,
                    onNodeCreated: tree3.zTreeOnNodeCreated,
                    onExpand: tree3.zTreeOnExpand
                }
            };
            $.fn.zTree.init($("#treeDemo3"), setting, null);
        },
        // 搜索树
        searchTree: function (param) {
            ifAllCheck = false; //模糊查询不自动勾选
            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                tree3.init();
                return
            }
            var querySetting = {
                async: {
                    url: "/clbs/a/search/reportFuzzySearch",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "type": $('#queryType3').val(),
                        "queryParam": param,
                        "queryType": "vehicle"
                    },
                    dataFilter: tree3.ajaxDataFilterSearch
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
                    beforeClick: tree3.beforeClickVehicle,
                    beforeCheck: tree3.zTreeBeforeCheck,
                    onCheck: tree3.onCheckVehicle,
                    onExpand: tree3.zTreeOnExpand,
                    onNodeCreated: tree3.zTreeOnNodeCreated
                }
            };
            $.fn.zTree.init($("#treeDemo3"), querySetting, null);

        },
        // 获取树接口地址
        getAlarmReportTreeUrl: function (treeId, treeNode) {
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
        // 服务器数据预处理(搜索树)
        ajaxDataFilterSearch: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData))
            var nodesArr;
            if ($('#queryType3').val() == "vehicle") {
                nodesArr = filterQueryResult(responseData, crrentSubV);
            } else {
                nodesArr = responseData;
            }
            for (var i = 0; i < nodesArr.length; i++) {
                nodesArr[i].open = true;
            }
            return nodesArr;
        },
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo3");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo3"),
                        nodes = zTree
                        .getCheckedNodes(true),
                        v = "";
                    var nodesLength = 0;
                    if (ifAllCheck) {
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
                    } else {
                        var nodes = zTree.getNodesByFilter(function (node) {
                            return node.type == "people" || node.type == "vehicle";
                        }, false, treeNode); // 查找节点集合
                        nodesLength = nodes.length;
                    }
                } else if (treeNode.type == "people" || treeNode.type == "vehicle") { //若勾选的为监控对象
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo3"),
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
        onCheck: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo3");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    routeSiteStatistics.validate3();
                }, 600);
            }
            treeUtil.setInputValue('treeDemo3', 'groupSelect3')
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
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo3");
            if (treeNode.type == "group") {
                var url = "/clbs/m/functionconfig/fence/bindfence/getOrgAssignmentVehicle";
                json_ajax("post", url, "json", false, {
                    "groupId": treeNode.id,
                    "isChecked": treeNode.checked,
                    "monitorType": "0"
                }, function (data) {
                    var result = data.obj;
                    if (result != null && result != undefined) {
                        $.each(result, function (i) {
                            var pid = i; //获取键值
                            var chNodes = result[i]; //获取对应的value
                            if (zTreeIdJson[pid] != undefined) {
                                var parentTid = zTreeIdJson[pid][0];
                                var parentNode = treeObj.getNodeByTId(parentTid);
                                if (parentNode.children === undefined) {
                                    parentNode.zAsync = true;
                                    treeObj.addNodes(parentNode, 0, chNodes);
                                }
                            }
                        });
                    }
                })
            }
            treeUtil.setInputValue('treeDemo3', 'groupSelect3')
        },
    }

    // 防抖
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
        tree1.init();
        tree2.init();
        tree3.init();
        for (var i = 1; i <= 3; i++) {
            if(i != 3){
                $('#dateRange' + i).dateRangePicker({
                    nowDate: routeSiteStatistics.getYesterDay(),
                    dateLimit: 60,
                    isShowHMS: false,
                    isOffLineReportFlag: true,
                });
            }else{
                $('#dateRange' + i).dateRangePicker({
                    start_date: yesterday.Format('yyyy-MM-dd'),
                    end_date: yesterday.Format('yyyy-MM-dd'),
                    dateLimit: 60,
                    isShowHMS: true,
                    isOffLineReportFlag: true,
                    greater: true
                });
            }
            $('#groupSelect' + i).on('click', showMenuContent)
        }

        // tab切换事件
        $('#stretch li').on('click', function () {
            var idx = $(this).index()
            if (idx == 0) {
                $('#roadData').show()
                $('#carData').hide()
                $('#pathData').hide()
            } else if (idx == 1) {
                $('#roadData').hide()
                $('#carData').show()
                $('#pathData').hide()
            } else {
                $('#roadData').hide()
                $('#carData').hide()
                $('#pathData').show()
            }
        })
        $('input').inputClear().on('onClearEvent', function (e, data) {
            switch (data.id) {
                case 'groupSelect1':
                    tree1.searchTree('treeDemo1', 'groupSelect1');
                    break
                case 'groupSelect2':
                    tree2.searchTree();
                    break
                case 'groupSelect3':
                    tree3.searchTree();
                    break
                default:
            }
        })


        // 显示隐藏列bug处理开始
        $("body").on('click', function (e) {
            if (!$("#Ul-menu-text1")[0].contains(e.target)) {
                $("#Ul-menu-text1").css('display', 'none')
            }
            if (!$("#Ul-menu-text2")[0].contains(e.target)) {
                $("#Ul-menu-text2").css('display', 'none')
            }
            if (!$("#Ul-menu-text3")[0].contains(e.target)) {
                $("#Ul-menu-text3").css('display', 'none')
            }
        })
        // 显示隐藏列 1
        $("#customizeColumns1").on('click', function (e) {
            $("#Ul-menu-text1").toggle()
            e.stopPropagation()
        })
        $('.toggle-vis-1').on('change', function (e) {
            var column = table1.dataTable.column($(this).attr('data-column'));
            column.visible(!column.visible());
        });
        // 显示隐藏列2
        $("#customizeColumns2").on('click', function (e) {
            $("#Ul-menu-text2").toggle()
            e.stopPropagation()
        })
        $('.toggle-vis-2').on('change', function (e) {
            var column = table2.dataTable.column($(this).attr('data-column'));
            column.visible(!column.visible());
        });
        // 显示隐藏列3
        $("#customizeColumns3").on('click', function (e) {
            $("#Ul-menu-text3").toggle()
            e.stopPropagation()
        })
        $('.toggle-vis-3').on('change', function (e) {
            var column = table3.dataTable.column($(this).attr('data-column'));
            column.visible(!column.visible());
        });
        // 显示隐藏列bug处理结束

        // 数据列表搜索按钮
        $('#search_button1').on('click', function () {
            routeSiteStatistics.query1()
        })
        $('#search_button2').on('click', function () {
            routeSiteStatistics.query2()
        })
        $('#search_button3').on('click', function () {
            routeSiteStatistics.query3()
        })


        // 树交互事件
        // 刷新树
        $('#groupSelect1').on('input propertychange', function () {
            tree1.searchTree('treeDemo1', 'groupSelect1')
        });
        $('#groupSelect2').on('input propertychange', debounce(function () {
            tree2.searchTree($('#groupSelect2').val())
        }, 350));
        $('#groupSelect3').on('input propertychange', debounce(function () {
            tree3.searchTree($('#groupSelect3').val())
        }, 350));
        // 搜索类型切换
        $('#queryType2').on('change', function () {
            $("#groupSelect2").val('')
            tree2.searchTree()
        });
        $('#queryType3').on('change', function () {
            $("#groupSelect3").val('')
            tree3.searchTree()
        });
        $('#simpleQueryParam1').on('keydown', function (e) {
            if (e.keyCode == 13) {
                routeSiteStatistics.query1()
            }
        })
        $('#simpleQueryParam2').on('keydown', function (e) {
            if (e.keyCode == 13) {
                routeSiteStatistics.query2()
            }
        })
        $('#simpleQueryParam3').on('keydown', function (e) {
            if (e.keyCode == 13) {
                routeSiteStatistics.query3()
            }
        })
    })
}(window, $))