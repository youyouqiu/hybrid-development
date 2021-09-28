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
    //刚进入页面小于TREE_MAX_CHILDREN_LENGTH自动勾选
    var ifAllCheck = true;

    var allVid;
    var vnameList = [];
    //用来储存显示数据
    var dataListArray = [];
    var simpleQueryParam = '';
    var searchStatus = false;

    latestLocation = {
        init: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            // menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(1) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 0; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            ;
            $("#Ul-menu-text").html(menu_text);

            // 组织树
            $("[data-toggle='tooltip']").tooltip();
            var setting = {
                async: {
                    url: latestLocation.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple"},
                    // otherParam: {"icoType": "0"},
                    dataFilter: latestLocation.ajaxDataFilter
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
                    beforeClick: latestLocation.beforeClickVehicle,
                    onCheck: latestLocation.onCheckVehicle,
                    beforeCheck: latestLocation.zTreeBeforeCheck,
                    onExpand: latestLocation.zTreeOnExpand,
                    //beforeAsync: latestLocation.zTreeBeforeAsync,
                    onAsyncSuccess: latestLocation.zTreeOnAsyncSuccess,
                    onNodeCreated: latestLocation.zTreeOnNodeCreated
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },
        //模糊查询树
        searchVehicleTree: function (param) {
            ifAllCheck = false;//模糊查询不自动勾选

            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                searchStatus = false;
                latestLocation.init();
            } else {
                bflag = true;
                searchStatus = true;
                var querySetting = {
                    async: {
                        url: "/clbs/a/search/reportFuzzySearch",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"type": $('#queryType').val(), "queryParam": param, "queryType": "multiple"},
                        dataFilter: latestLocation.ajaxQueryDataFilter
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
                        beforeClick: latestLocation.beforeClickVehicle,
                        onCheck: latestLocation.onCheckVehicle,
                        // onExpand: latestLocation.zTreeOnExpand,
                        onNodeCreated: latestLocation.zTreeOnNodeCreated,
                        beforeCheck: latestLocation.zTreeBeforeCheck,
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
        getTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
                // return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=allMonitor";
            }
        },
        //zTreeBeforeAsync: function () {
        //   return bflag;
        //},
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (size <= TREE_MAX_CHILDREN_LENGTH && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            latestLocation.getCharSelect(treeObj);

            bflag = false;
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
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;
                    if (!searchStatus) {
                        json_ajax("post", "/clbs/m/personalized/ico/getCheckedVehicle",
                            "json", false, {"parentId": treeNode.id, "type": treeNode.type}, function (data) {
                                if (data.success) {
                                    nodesLength += data.obj;
                                } else {
                                    layer.msg(data.msg);
                                }
                            });
                    }

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

                    /* function filter(node) {
                        if ((node.type == "people" || node.type == "vehicle" || node.type == "thing") && !node.checked) {
                            return true;
                        }
                        return false;
                    }
                   var curChecknodes = zTree.getNodesByFilter(filter, false, treeNode); // 查找节点集合
                    nodesLength += curChecknodes.length;*/
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
                        if (nodes[i].type == "people" || nodes[i].type == "vehicle" || nodes[i].type == "thing") {
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
                    "monitorType": "monitor"
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
            //latestLocation.getCharSelect(treeObj);
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
        onCheckVehicle: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                searchFlag = false;
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    latestLocation.getCheckedNodes();
                    latestLocation.validates();
                }, 600);
            }
            crrentSubV = [];
            crrentSubV.push(treeNode.id);
            latestLocation.getCharSelect(zTree);
            latestLocation.getCheckedNodes();
        },
        //获取到选择的节点
        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree.getCheckedNodes(true), v = "", vid = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == "vehicle" || nodes[i].type == "people" || nodes[i].type == "thing") {
                    v += nodes[i].name + ",";
                    vid += nodes[i].id + ",";
                }
            }
            allVid = vid;
            vnameList = v;
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
            $("#search_button").on("click", function () {
                var queryCondition = $("#simpleQueryParam").val();
                simpleQueryParam = queryCondition;
                myTable.column(1).search(queryCondition, false, false).draw();
            });
        },
        //刷新列表
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            latestLocation.inquireClick(1);
            myTable.column(1).search("", false, false).draw();
            simpleQueryParam = "";
        },
        //获取当前时间，格式YYYY-MM-DD
        getNowFormatDate: function () {
            var date = new Date();
            var separator = "-";
            var year = date.getFullYear();
            var month = date.getMonth() + 1;
            var strDate = date.getDate();
            var hour = date.getHours();
            var minutes = date.getMinutes();
            var seconds = date.getSeconds();
            if (hour >= 1 && hour <= 9) {
                hour = "0" + hour;
            }
            if (minutes >= 1 && minutes <= 9) {
                minutes = "0" + minutes;
            }
            if (seconds >= 1 && seconds <= 9) {
                seconds = "0" + seconds;
            }
            if (month >= 1 && month <= 9) {
                month = "0" + month;
            }
            if (strDate >= 0 && strDate <= 9) {
                strDate = "0" + strDate;
            }
            return (year + separator + month + separator + strDate + ' ' + hour + ':' + minutes + ':' + seconds);
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
            vMonth = latestLocation.doHandleMonth(vMonth + 1);
            vDate = latestLocation.doHandleMonth(vDate);
            var hour = dateList.getHours();
            var minutes = dateList.getMinutes();
            var seconds = dateList.getSeconds();
            if (hour >= 1 && hour <= 9) {
                hour = "0" + hour;
            }
            if (minutes >= 1 && minutes <= 9) {
                minutes = "0" + minutes;
            }
            if (seconds >= 1 && seconds <= 9) {
                seconds = "0" + seconds;
            }
            startTime = vYear + "-" + vMonth + "-" + vDate + ' ' + hour + ':' + minutes + ':' + seconds;
            $('#timeInterval').val(startTime);
        },
        inquireClick: function (number) {
            if (number != 1 && number != 0) {
                latestLocation.setNewDay(number);
            }
            if (number == 0) {
                $('#timeInterval').val(latestLocation.getNowFormatDate());
            }
            latestLocation.getCheckedNodes();
            if (!latestLocation.validates()) {
                return;
            }
            queryDateStr = $('#timeInterval').val();
            var ajaxDataParam = {
                "vehicleIds": allVid,
                "queryTime": queryDateStr,
            }
            var url = "/clbs/v/statistic/latestLocationInfoStatistics/listByPass";
            json_ajax("POST", url, "json", true, ajaxDataParam, latestLocation.getCallback);
        },
        getCallback: function (data) {
            if (data.success == true) {
                dataListArray = [];//用来储存显示数据
                if (data.obj != null && data.obj.length != 0) {
                    for (var i = 0; i < data.obj.length; i++) {
                        var recordData = data.obj[i];
                        var dataList =
                            [
                                i + 1,
                                recordData.plateNumber,
                                recordData.monitorTypeStr,
                                recordData.groupName,
                                recordData.isOnlineStr ? recordData.isOnlineStr : '',
                                recordData.deviceTypeStr ? recordData.deviceTypeStr : '',
                                recordData.locationTime ? recordData.locationTime : '',
                                recordData.speed ? recordData.speed : '',
                                recordData.accStatus ? recordData.accStatus : '',
                                recordData.location ? recordData.location : '<a onclick="latestLocation.getDrivingAddress(this,' + recordData.latitude + ',' + recordData.longtitude + ')">点击获取位置信息</a>'
                            ];
                        dataListArray.push(dataList);
                    }
                    latestLocation.reloadData(dataListArray);
                    $("#simpleQueryParam").val("");
                } else {
                    latestLocation.reloadData(dataListArray);
                    $("#simpleQueryParam").val("");
                }
                simpleQueryParam = "";
                myTable.column(1).search("", false, false).draw();

            } else {
                if (data.msg != null) {
                    layer.msg(data.msg, {move: false});
                }
            }
        },
        //解析位置信息
        getDrivingAddress: function (event, latitude, longitude) {
            var url = '/clbs/v/monitoring/address';
            var param = {addressReverse: [latitude, longitude, '', "", 'vehicle']};
            var _this = $(event);
            $.ajax({
                type: "POST",//通常会用到两种：GET,POST。默认是：GET
                url: url,//(默认: 当前页地址) 发送请求的地址
                dataType: "json", //预期服务器返回的数据类型。"json"
                async: true, // 异步同步，true  false
                data: param,
                traditional: true,
                timeout: 8000, //超时时间设置，单位毫秒
                success: function (data) {//请求成功
                    _this.closest('td').html(data == '[]' ? '未定位' : data);
                },
            });
        },
        doHandleMonth: function (month) {
            var m = month;
            if (month.toString().length == 1) {
                m = "0" + month;
            }
            return m;
        },
        exportMileage: function () {
            if ($("#dataTable tbody tr td").hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据，无法导出");
                return;
            }
            if(getRecordsNum() > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var url = '/clbs/v/statistic/latestLocationInfoStatistics/export?simpleQueryParam=' + simpleQueryParam;
            window.location.href = url
        },
        validates: function () {
            return $("#latestList").validate({
                rules: {
                    timeInterval: {
                        required: true
                    },
                    groupSelect: {
                        zTreeChecked: "treeDemo"
                    }
                },
                messages: {
                    timeInterval: {
                        required: "请选择日期！",
                    },
                    groupSelect: {
                        zTreeChecked: vehicleSelectBrand,
                    },
                }
            }).form();
        },
        reloadData: function (dataList) {
            var currentPage = myTable.page();
            myTable.clear();
            myTable.rows.add(dataList);
            myTable.page(currentPage).draw(false);
            myTable.column(1).search('', false, false).page(currentPage).draw();
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
        latestLocation.init();
        latestLocation.getTable('#dataTable');
        $('input').inputClear();
        laydate.render({
            elem: '#timeInterval',
            theme: '#6dcff6',
            type: 'datetime',
            max: latestLocation.getNowFormatDate()
        });
        $("#timeInterval").val(latestLocation.getNowFormatDate());
        $("#groupSelect").bind("click", showMenuContent);
        //导出
        $("#exportMileage").bind("click", latestLocation.exportMileage);
        $("#refreshTable").bind("click", latestLocation.refreshTable);

        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                search_ztree('treeDemo', 'groupSelect', 'vehicle');
                treeObj.checkAllNodes(false);
            }
            ;
        });
        /**
         * 监控对象树模糊查询
         */
        $("#groupSelect").on('input propertychange', function (value) {
            var param = $(this).val();
            latestLocation.searchVehicleTree(param);
        });
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var param = $("#groupSelect").val();
                searchStatus = false;
                latestLocation.searchVehicleTree(param);
            }
        });
        $('#queryType').on('change', function () {
            var param = $("#groupSelect").val();
            latestLocation.searchVehicleTree(param);
        });
    })
}(window, $))