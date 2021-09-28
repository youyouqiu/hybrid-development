(function (window, $) {
    //开始时间
    var queryDateStr;
    //结束时间
    var endTime;
    var myTable;
    //判断组织节点是否是勾选操作
    var checkFlag = false;
    //当前权限监控对象数量
    var size;
    var zTreeIdJson = {};
    //刚进入页面小于100自动勾选
    var ifAllCheck = true;
    var groupId = [];
    var simpleQueryParam = '';
    var monthTime = null,
        paramFlag = "",
        timeFlag = true;
    var dbValue = false //树双击判断参数
    cargoSpotCheck = {
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
                    url: cargoSpotCheck.getParkingReportTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "type": "multiple",
                        "icoType": "0"
                    },
                    dataFilter: cargoSpotCheck.ajaxDataFilter
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
                    beforeClick: cargoSpotCheck.beforeClickVehicle,
                    onAsyncSuccess: cargoSpotCheck.zTreeOnAsyncSuccess,
                    beforeCheck: cargoSpotCheck.zTreeBeforeCheck,
                    onCheck: cargoSpotCheck.onCheckVehicle,
                    onNodeCreated: cargoSpotCheck.zTreeOnNodeCreated,
                    onExpand: cargoSpotCheck.zTreeOnExpand,
                    onDblClick: cargoSpotCheck.onDblClickVehicle

                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },
        onDblClickVehicle: function (e, treeId, treeNode) {
            if (!treeNode) return
            dbValue = true
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.checkAllNodes(false);
            cargoSpotCheck.getCheckedNodes("treeDemo");
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
        validates: function () {
            return $("#oilist").validate({
                rules: {
                    groupSelect: {
                        required: true
                    },
                    timeInterval: {
                        required: true
                    }
                },
                messages: {
                    groupSelect: {
                        required: "请至少选择一个企业"
                    },
                    timeInterval: {
                        required: "请选择日期"
                    }
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
            if (size <= 100 && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            cargoSpotCheck.getCharSelect(treeObj);
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked && !dbValue) {
                var treeObj = $.fn.zTree.getZTreeObj('treeDemo');
                var getCheckNode = treeObj.getCheckedNodes("true");
                var nodes = treeObj.getNodesByFilter(function (node) {
                    return node.type == 'group' && !node.checked;
                }, false, treeNode);
                if (getCheckNode.length + nodes.length + 1 > TREE_MAX_CHILDREN_LENGTH) {
                    layer.msg('最多勾选' + TREE_MAX_CHILDREN_LENGTH + '个企业' + '<br/>双击名称可选中本组织');
                    flag = false;
                }
            }
            return flag;
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            dbValue = false
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                zTree.expandNode(treeNode, true, true, false, false); // 展开节点
                setTimeout(() => {
                    cargoSpotCheck.getCheckedNodes("treeDemo");
                    cargoSpotCheck.validates();
                }, 600);
            }
            cargoSpotCheck.getCheckedNodes("treeDemo");
            cargoSpotCheck.getCharSelect(zTree);
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
        //设置时间
        setNewDay: function (day) {
            var timeInterval = $('#timeInterval').val();

            var startTimeIndex = timeInterval.replace(/-/g, "/");
            var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
            var dateList = new Date();
            dateList.setTime(vtoday_milliseconds);
            var vYear = dateList.getFullYear();
            var vMonth = dateList.getMonth();
            var vDate = dateList.getDate();
            vMonth = cargoSpotCheck.doHandleMonth(vMonth + 1);
            vDate = cargoSpotCheck.doHandleMonth(vDate);
            startTime = vYear + "-" + vMonth + "-" + vDate;
            $('#timeInterval').val(startTime);
        },
        //获取当前时间，格式YYYY-MM-DD
        getNowFormatDate: function (timeFlag) {
            var date = new Date();
            var separator = "-";
            var year = date.getFullYear();
            var month = date.getMonth() + 1;
            var strDate = date.getDate();
            var hour = date.getHours();
            var minutes = date.getMinutes();
            if (hour >= 1 && hour <= 9) {
                hour = "0" + hour;
            }
            if (minutes >= 1 && minutes <= 9) {
                minutes = "0" + minutes;
            }
            if (month >= 1 && month <= 9) {
                month = "0" + month;
            }
            if (strDate >= 0 && strDate <= 9) {
                strDate = "0" + strDate;
            }
            if (hour == 0) {
                hour = "00"
            }
            if (minutes == 0) {
                minutes = "00"
            }

            if (!timeFlag) {
                return (year + separator + month + separator + strDate + ' ' + hour + ':' + minutes + ":00");
            }
            return (year + separator + month + separator + strDate + ' ' + 23 + ':' + 59 + ":00");


        },
        //查询
        inquireClick: function (number) {
            if (!cargoSpotCheck.validates()) {
                return;
            }
            if (number != 1) {
                cargoSpotCheck.setNewDay(number);
            }

            paramFlag = true;
            cargoSpotCheck.getCheckedNodes('treeDemo');
            monthTime = $('#timeInterval').val();
            cargoSpotCheck.inquireDataList();
        },
        //数据列表
        inquireDataList: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            for (var i = 0; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-text").html(menu_text);

            var columnDefs = [{
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];
            var columns = [{
                "data": null,
                "class": "text-center"
            }, {
                "data": "brand",
                "class": "text-center"
            }, {
                "data": "groupName",
                "class": "text-center"
            }, {
                "data": "time",
                "class": "text-center"
            }, {
                "data": "address",
                "class": "text-center"
            }, {
                "data": "onlineFlag",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    var onlineFlag = row.onlineFlag;
                    if (onlineFlag == 1) {
                        return "是"
                    }
                    return "否"

                }
            }, {
                "data": "speed",
                "class": "text-center"
            }, {
                "data": "fatigueFlag",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    var fatigueFlag = row.fatigueFlag;
                    if (fatigueFlag == 1) {
                        return "是"
                    }
                    return "否"

                }
            }, {
                "data": "speedFlag",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    var speedFlag = row.speedFlag;
                    if (speedFlag == 1) {
                        return "是"
                    }
                    return "否"

                }
            }, {
                "data": "otherAlarm",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    var otherAlarm = row.otherAlarm;
                    if (otherAlarm == "" || otherAlarm == null) {
                        return "否"
                    }
                    return otherAlarm;

                }
            }, {
                "data": "dealMeasure",
                "class": "text-center"
            }, {
                "data": "dealTime",
                "class": "text-center"
            }, {
                "data": "dealer",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    var dealer = row.dealer;
                    if (dealer == 'null') {
                        return "";
                    }
                    return dealer;

                }
            }, {
                "data": "feedbackTime",
                "class": "text-center"
            }, {
                "data": "dealResult",
                "class": "text-center"
            }];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                if (paramFlag == true) {
                    simpleQueryParam = "";
                    $("#simpleQueryParam").val('');
                } else {
                    simpleQueryParam = $("#simpleQueryParam").val();
                }
                d.groupIds = groupId;
                d.time = monthTime;
                d.search = simpleQueryParam;
            };
            //表格setting
            var setting = {
                listUrl: "/clbs/s/cargo/spotCheck/list",
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                lengthChange: false //不允许用户改变表格每页显示的记录数
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
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
        },
        //搜索
        searchTable: function () {
            if (monthTime == null) {
                return;
            }
            paramFlag = false;
            cargoSpotCheck.inquireDataList();
        },
        //刷新
        refreshTable: function () {
            cargoSpotCheck.inquireClick(1);
        },
        //导出
        exportAlarm: function () {
            if ($("#dataTable tbody tr td").hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据，无法导出");
                return;
            }
            if (getRecordsNum() > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            simpleQueryParam = $("#simpleQueryParam").val();
            var url = "/clbs/s/cargo/spotCheck/export"
            var param = {
                groupIds: groupId,
                time: monthTime,
                search: simpleQueryParam
            }
            exportExcelUsePost(url, param)
            // window.location.href="/clbs/s/cargo/spotCheck/export?groupIds="+groupId+"&time="+monthTime+"&search="+simpleQueryParam;
        },
        //批量处理
        batchDeal: function () {
            var dealMeasure = $("#dealMeasure").val(),
                dealResult = $("#dealResult").val(),
                url = "/clbs/s/cargo/spotCheck/batchDeal",
                param = {
                    "dealMeasure": dealMeasure,
                    "dealResult": dealResult
                };
            json_ajax("POST", url, "json", true, param, function (data) {
                if (data.success) {
                    myTable.refresh();
                    layer.msg("处理成功", {
                        move: false
                    });
                    $("#myModal").modal('hide');
                }
            })
        }
    };
    $(function () {
        cargoSpotCheck.init();
        cargoSpotCheck.getTable('#dataTable');
        //时间
        laydate.render({
            elem: '#timeInterval',
            theme: '#6dcff6',
            type: 'datetime',
            format: 'yyyy-MM-dd HH:mm',
            max: cargoSpotCheck.getNowFormatDate(timeFlag)
        });
        var initTime = cargoSpotCheck.getNowFormatDate();
        var maxTime = initTime.substring(0, initTime.length - 3);
        $("#timeInterval").val(maxTime);
        //下拉框显示隐藏
        $("#groupSelect").bind("click", showMenuContent);
        //导出
        $("#exportAlarm").bind("click", cargoSpotCheck.exportAlarm);
        //刷新
        $("#refreshTable").bind("click", cargoSpotCheck.refreshTable)
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                search_ztree('treeDemo', 'groupSelect', 'group');
                treeObj.checkAllNodes(false);
            };
        });
        //批量处理
        $("#doSubmits").bind("click", cargoSpotCheck.batchDeal);
        $("#batch").bind('click', function (e) {
            if ($("#dataTable tbody tr td").hasClass("dataTables_empty")) {
                $(e.target).attr('data-target', "");
            } else {
                $(e.target).attr('data-target', "#myModal");
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