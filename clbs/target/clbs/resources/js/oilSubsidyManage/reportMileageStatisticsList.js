(function (window, $) {
    //车辆列表
    var vehicleList = [];
    //车辆id列表
    var vehicleId = [];
    //开始时间
    // var startTime;
    var myTable;
    //结束时间
    // var endTime;
    var checkFlag = false; //判断组织节点是否是勾选操作
    var size; //当前权限监控对象数量
    var zTreeIdJson = {};

    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询
    var ifAllCheck = true; //刚进入页面小于TREE_MAX_CHILDREN_LENGTH自动勾选
    var searchFlag = true;

    mileageStatistics = {
        init: function () {
            //车辆树
            var setting = {
                async: {
                    url: mileageStatistics.getmileageStatisticsTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "type": "multiple",
                        "icoType": "0"
                    },
                    dataFilter: mileageStatistics.ajaxDataFilter
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
                    beforeClick: mileageStatistics.beforeClickVehicle,
                    onAsyncSuccess: mileageStatistics.zTreeOnAsyncSuccess,
                    beforeCheck: mileageStatistics.zTreeBeforeCheck,
                    onCheck: mileageStatistics.onCheckVehicle,
                    onNodeCreated: mileageStatistics.zTreeOnNodeCreated,
                    onExpand: mileageStatistics.zTreeOnExpand
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
            mileageStatistics.renderTh();
            mileageStatistics.tableFilter();
        },

        //模糊查询树
        searchVehicleTree: function (param) {
            ifAllCheck = false; //模糊查询不自动勾选
            crrentSubV = [];

            if (param == null || param == undefined || param == '') {
                bflag = true;
                mileageStatistics.init();
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
                        dataFilter: mileageStatistics.ajaxQueryDataFilter
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
                        beforeClick: mileageStatistics.beforeClickVehicle,
                        beforeCheck: mileageStatistics.zTreeBeforeCheck,
                        onCheck: mileageStatistics.onCheckVehicle,
                        onExpand: mileageStatistics.zTreeOnExpand,
                        onNodeCreated: mileageStatistics.zTreeOnNodeCreated
                    }
                };
                $.fn.zTree.init($("#treeDemo"), querySetting, null);
            }
        },
        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData));
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

        tableFilter: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\"" +
                " data-column=\"" + parseInt(1) + "\" />" + table[0].innerHTML + "</label></li>";
            for (var i = 1; i < table.length; i++) {
                var $id = 'column' + i;
                menu_text += "<li id=\"" + $id + "\" ><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\"" +
                    " data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-text").html(menu_text);
        },

        getmileageStatisticsTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle";
            }
        },


        //组织树预处理加载函数
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

        //判断日期是否合法,是否选中车辆
        validates: function () {
            return $("#hourslist").validate({
                rules: {
                    groupSelect: {
                        zTreeChecked: "treeDemo"
                    }
                },
                messages: {
                    groupSelect: {
                        zTreeChecked: vehicleSelectBrand,
                    }
                }
            }).form();
        },

        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },

        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (size <= TREE_MAX_CHILDREN_LENGTH && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            mileageStatistics.getCharSelect(treeObj);
        },

        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
                        nodes = zTree.getCheckedNodes(true),
                        v = "";
                    var nodesLength = 0;
                    if (ifAllCheck) {
                        json_ajax("post", "/clbs/m/personalized/ico/getVehicleCount",
                            "json", false, {
                                "parentId": treeNode.id,
                                "type": treeNode.type
                            }, function (data) {
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
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
                        nodes = zTree.getCheckedNodes(true),
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
        onCheckVehicle: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                searchFlag = false;
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
            }

            crrentSubV = [];
            crrentSubV.push(treeNode.id);

            mileageStatistics.getCharSelect(zTree);
            mileageStatistics.getCheckedNodes();
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
            if (!ifAllCheck || (treeNode.type == "group" && !checkFlag)) {
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
                            var chNodes = result[i]; //获取对应的value
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
        getCharSelect: function (treeObj) {
            var nodes = treeObj.getCheckedNodes(true);
            var allNodes = treeObj.getNodes();
            if (nodes.length > 0) {
                $("#groupSelect").val(allNodes[0].name);
            } else {
                $("#groupSelect").val("");
            }
        },

        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
                nodes = zTree.getCheckedNodes(true),
                v = "",
                vid = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == "vehicle") {
                    v += nodes[i].name + ",";
                    vid += nodes[i].id + ",";
                }
            }
            vehicleList = v;
            vehicleId = vid;
        },

        exportAlarm: function () {
            mileageStatistics.getCheckedNodes();
            if (!mileageStatistics.validates()) {
                return;
            }
            var url = '/clbs/m/report/mileage/statistics/export/month';
            window.location.href = url;
        },

        getTable: function (table) {
            $('.toggle-vis').prop('checked', true);
            myTable = $(table).DataTable({
                "destroy": true,
                "dom": 'tiprl', // 自定义显示项
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
                var tsval = $("#simpleQueryParam").val();
                myTable.search(tsval, false, false).draw();
            });
        },

        reloadData: function (dataList) {
            var currentPage = myTable.page();
            myTable.clear();
            myTable.rows.add(dataList);
            myTable.page(currentPage).draw(false);
            myTable.search('', false, false).page(currentPage).draw();
        },

        //刷新列表
        refreshTable: function () {
            mileageStatistics.inquireClick();
        },

        renderSelect: function (id) {
            var select = $(id);
            var now = new Date();
            var year = now.getFullYear();
            var month = now.getMonth() + 1;
            var tmpl = '<option value="$name">$name</option>';
            var add0 = function (n) {
                if (n < 10) {
                    return '0' + n.toString();
                }
                return n.toString()
            };
            for (var i = 0; i < 13; i++) {
                if (i < month) {
                    select.append($(tmpl.replace(/\$name/g, year + '-' + add0(month - i))));
                } else {
                    select.append($(tmpl.replace(/\$name/g, (year - 1) + '-' + add0(12 - i + month))));
                }
            }
        },

        inquireClick: function () {
            if (mileageStatistics.validates()) {
                var time = $("#select").val().split('-');
                var days = mileageStatistics.queryDays(Number(time[0]), Number(time[1]));
                mileageStatistics.renderTh(days);
                mileageStatistics.tableFilter();
                mileageStatistics.getTable('#dataTable');

                mileageStatistics.getCheckedNodes();

                var url = '/clbs/m/report/mileage/statistics/days';
                var parameter = {
                    "vehicleId": vehicleId,
                    "month": $("#select").val()
                };
                json_ajax("POST", url, "json", true, parameter, mileageStatistics.getCallback);
            }
        },
        getCallback: function (date) {
            if (date.success) {
                var dataListArray = []; //用来存储显示数据
                if (date.obj != null && date.obj.length) {
                    var list = date.obj;
                    for (var i = 0; i < list.length; i++) {
                        var dateList = [
                            i + 1,
                            list[i].enterpriseName,
                            list[i].vehicleBrandNumber,
                        ];
                        for (var j = 0; j < list[i].days.length; j++) {
                            dateList.push(list[i].days[j])
                        }
                        ;
                        dateList.push(list[i].monthReport);
                        dataListArray.push(dateList);
                    }

                }
                mileageStatistics.reloadData(dataListArray);

                if (dataListArray.length == 0) {
                    $("#exportAlarm").prop('disabled', true);
                } else {
                    $("#exportAlarm").prop('disabled', false);
                }

            } else {
                layer.msg(data.msg, {
                    move: false
                });
            }
        },
        // 渲染表格表头信息
        renderTh: function (dates) {
            if (!dates) {
                var now = new Date();
                var year = now.getFullYear();
                var month = now.getMonth() + 1;
                dates = new Date(year, month, 0).getDate();
            }
            var tmpl = '<th class="text-center">$name</th>';
            var tr = $('#dataTable thead tr');
            tr.empty();
            tr.append($(tmpl.replace('$name', '序号')));
            tr.append($(tmpl.replace('$name', '组织')));
            tr.append($(tmpl.replace('$name', '车牌号')));

            for (var i = 1; i <= dates; i++) {
                tr.append($(tmpl.replace('$name', i)));
            }
            tr.append($(tmpl.replace('$name', '合计')));
        },
        /**
         * 判断某年是否闰年
         */
        isRuinian: function (year) {
            if (year / 4 == 0 && year / 100 != 0) {
                return 29;
            } else if (year / 400 == 0) {
                return 29;
            } else {
                return 28;
            }
        },

        /**
         * 根据年和月获取该月有几天
         */
        queryDays: function (year, month) {
            switch (month) {
                case 1:
                case 3:
                case 5:
                case 7:
                case 8:
                case 10:
                case 12:
                    return 31;
                    break;
                case 4:
                case 6:
                case 9:
                case 11:
                    return 30;
                    break;
                case 2:
                    return mileageStatistics.isRuinian(year);
            }
        }

    };

    $(function () {
        //初始化页面
        mileageStatistics.init();
        $('input').inputClear();
        mileageStatistics.getTable('#dataTable');
        //时间下拉框
        mileageStatistics.renderSelect("#select");
        //组织下拉显示
        $("#groupSelect").bind("click", showMenuContent);
        //导出
        $("#exportAlarm").bind("click", mileageStatistics.exportAlarm);
        $("#refreshTable").bind("click", mileageStatistics.refreshTable);

        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var param = $("#groupSelect").val();
                mileageStatistics.searchVehicleTree(param);
            }
        });

        /**
         * 监控对象树模糊查询
         */
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
                    mileageStatistics.searchVehicleTree(param);
                }
                searchFlag = true;
            }, 500);
        });

        $('#queryType').on('change', function () {
            var param = $("#groupSelect").val();
            mileageStatistics.searchVehicleTree(param);
        });
    })
})(window, $);