(function (window, $) {
    var myTable;
    var myDetailTable;
    var myDetailTable2;
    //车辆列表
    var vehicleList = [];
    //车辆id列表
    var vehicleId = [];
    //开始时间
    var startTime;
    //结束时间
    var endTime;
    var checkFlag = false; //判断组织节点是否是勾选操作
    var size;//当前权限监控对象数量
    var zTreeIdJson = {};
    var ifAllCheck = true; //刚进入页面小于100自动勾选
    var crrentSubV = []; //模糊查询？
    var bflag = true; //模糊查询
    var isSearch = true;
    var fuzzyParam = '';

    fuelMileageReport = {
        init: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            for (var i = 0; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-text").html(menu_text);

            fuelMileageReport.treeInit();
            $("[data-toggle='tooltip']").tooltip();
        },
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
                tMonth = fuelMileageReport.doHandleMonth(tMonth + 1);
                tDate = fuelMileageReport.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " ";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24
                    * parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = fuelMileageReport.doHandleMonth(endMonth + 1);
                endDate = fuelMileageReport.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate;
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = fuelMileageReport.doHandleMonth(vMonth + 1);
                vDate = fuelMileageReport.doHandleMonth(vDate);
                startTime = vYear + "-" + vMonth + "-" + vDate;
                if (day == 1) {
                    endTime = vYear + "-" + vMonth + "-" + vDate;
                } else {
                    var endNum = -1;
                    var vendtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * parseInt(endNum);
                    var dateEnd = new Date();
                    dateEnd.setTime(vendtoday_milliseconds);
                    var vendYear = dateEnd.getFullYear();
                    var vendMonth = dateEnd.getMonth();
                    var vendDate = dateEnd.getDate();
                    vendMonth = fuelMileageReport.doHandleMonth(vendMonth + 1);
                    vendDate = fuelMileageReport.doHandleMonth(vendDate);
                    endTime = vendYear + "-" + vendMonth + "-" + vendDate;
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
        getsTheCurrentTime: function () {
            var nowDate = new Date();
            startTime = endTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate());
            endTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate());
            var atime = $("#atime").val();
            if (atime != undefined && atime != "") {
                startTime = endTime = atime;
            }
            return startTime;
        },
        // 获取当月、前一月
        getMonthDate: function (pre) {
            var now = new Date(); //当前月
            var nowYear = now.getFullYear(); //年份
            var nowMonth = now.getMonth(); //月份
            if (pre) {// 前一月
                var timeInterval = $('#timeInterval').val().split('--');
                now = new Date(timeInterval[0]);
                nowYear = now.getFullYear(); //年份
                nowMonth = now.getMonth() - 1; //月份
                if (nowMonth < 0) {
                    nowYear = nowYear - 1;
                    nowMonth = 11;
                }
            }

            var monthStartDate = new Date(nowYear, nowMonth, 1);
            var monthEndDate = new Date(nowYear, nowMonth + 1, 1);
            var days = (monthEndDate - monthStartDate) / (1000 * 60 * 60 * 24);
            if (pre) {
                monthEndDate = new Date(nowYear, nowMonth, days);
            } else {// 选择当月时,结束日期为今天
                monthEndDate = now;
            }
            startTime = formatDate(monthStartDate);
            endTime = formatDate(monthEndDate);
        },
        // 日期格式化
        formatDate: function (date) {
            var myyear = date.getFullYear();
            var mymonth = date.getMonth() + 1;
            var myweekday = date.getDate();
            var returnDate = '';

            if (mymonth < 10) {
                mymonth = "0" + mymonth;
            }
            if (myweekday < 10) {
                myweekday = "0" + myweekday;
            }
            return (myyear + "-" + mymonth + "-" + myweekday);
        },
        inquireClick: function (number) {
            if (number == 0) {
                fuelMileageReport.getsTheCurrentTime();
            } else if (number == -1) {
                fuelMileageReport.startDay(-1);
            } else if (number == 'curMonth') {
                fuelMileageReport.getMonthDate();
            } else if (number == 'preMonth') {
                fuelMileageReport.getMonthDate('preMonth');
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
            ;
            fuelMileageReport.getCheckedNodes();
            if (!fuelMileageReport.validates()) {
                return;
            }
            var url = "/clbs/m/reportManagement/oilMassMileReport/getListData";
            var parameter = {"vehicleId": vehicleId, "startDate": startTime, "endDate": endTime};
            json_ajax("POST", url, "json", true, parameter, fuelMileageReport.getCallback);
        },
        exportAlarm: function () {
            if(getRecordsNum() > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var url = "/clbs/m/reportManagement/oilMassMileReport/export?fuzzyParam=" + fuzzyParam;
            window.location.href = url;
        },
        validates: function () {
            return $("#fuellist").validate({
                rules: {
                    startTime: {
                        required: true
                    },
                    endTime: {
                        required: true,
                        compareDate: "#timeInterval"
                    },
                    groupSelect: {
                        zTreeChecked: "treeDemo"
                    }
                },
                messages: {
                    startTime: {
                        required: "请选择开始日期！"
                    },
                    endTime: {
                        required: "请选择结束时间！",
                        compareDate: endtimeComStarttime
                    },
                    groupSelect: {
                        zTreeChecked: vehicleSelectBrand
                    }
                }
            }).form();
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (size <= 100 && ifAllCheck) {
                treeObj.checkAllNodes(true);
                fuelMileageReport.getCharSelect(treeObj);
            }
            bflag = false;
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;

                    json_ajax("post", "/clbs/v/oilmassmgt/oilvehiclesetting/oilSetMonitorNumberCountByParId",
                        "json", false, {"parId": treeNode.id, "type": treeNode.type}, function (data) {
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
                        if (nodes[i].type == "people" || nodes[i].type == "vehicle" || nodes[i].type == "thing") {
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
                isSearch = false;
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    fuelMileageReport.getCheckedNodes();
                    fuelMileageReport.validates();
                }, 600);
            }
            crrentSubV = [];
            crrentSubV.push(treeNode.id);
            fuelMileageReport.getCharSelect(zTree);
            fuelMileageReport.getCheckedNodes();
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
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (treeNode.type == "group") {
                var url = "/clbs/v/oilmassmgt/oilvehiclesetting/oilSetMonitorByGroupId";
                json_ajax("post", url, "json", false, {
                    "groupId": treeNode.id,
                    "isChecked": treeNode.checked,
                    "type": "group"
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
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                .getCheckedNodes(true), v = "", vid = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == "people" || nodes[i].type == "vehicle" || nodes[i].type == "thing") {
                    v += nodes[i].name + ",";
                    vid += nodes[i].id + ",";
                }
            }
            vehicleList = v;
            vehicleId = vid;
        },
        getCallback: function (date) {
            fuzzyParam = '';
            if (date.success == true) {
                dataListArray = [];//用来储存显示数据
                if (date.obj.oilMassMileData != null && date.obj.oilMassMileData.length != 0) {
                    $("#exportAlarm").removeAttr("disabled");
                    var alarm = date.obj.oilMassMileData;
                    for (var i = 0; i < alarm.length; i++) {
                        var oilTank = 0;
                        if (alarm[i].oilTank == null) {
                            oilTank = '';
                        } else if (alarm[i].oilTank < 0) {
                            oilTank = '-';
                        } else {
                            oilTank = alarm[i].oilTank;
                        }
                        var dateList =
                            [
                                i + 1,
                                alarm[i].monitorName,
                                alarm[i].groupName,
                                alarm[i].startDate,
                                alarm[i].endDate,
                                alarm[i].days,
                                oilTank,
                                alarm[i].fuelAmount > 0 ? `<a onclick="fuelMileageReport.getFuelAmountDetails('${alarm[i].monitorStrId}')">${alarm[i].fuelAmount}</a>` : alarm[i].fuelAmount,
                                alarm[i].fuelSpill > 0 ? `<a onclick="fuelMileageReport.getFuelSpillDetails('${alarm[i].monitorStrId}')">${alarm[i].fuelSpill}</a>` : alarm[i].fuelSpill,
                                oilTank !== '' && oilTank !== '-' && alarm[i].gpsMile !== 0 ? (oilTank * 100 / alarm[i].gpsMile).toFixed(2) : '-',
                                alarm[i].gpsMile,
                            ];
                        dataListArray.push(dateList);
                    }
                    fuelMileageReport.reloadData(dataListArray);
                } else {
                    fuelMileageReport.reloadData(dataListArray);
                    $("#simpleQueryParam").val("");
                    $("#exportAlarm").attr("disabled", "disabled");
                }
            } else {
                if (date.msg != null) {
                    layer.msg(date.msg, {move: false});
                }
            }
        },
        // 详情抽屉列表渲染
        initDetailTable: function (id) {
            var columnDefs = [{
                //第一列，用来显示序号
                "searchable": false,
                "targets": 0
            }];
            var columns = [
                {
                    //第一列，用来显示序号
                    "data": null,
                    "class": "text-center"
                },
                {
                    "data": "vTimeStr",
                    "class": "text-center",
                },
                {
                    "data": "oilTank",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (row.oilTankOne && row.oilTankTwo) {
                            return ((parseFloat(row.oilTankOne) + parseFloat(row.oilTankTwo)).toFixed(1)).toString();
                        }
                        return '-';
                    },
                }, {
                    "data": "oilTankOne",
                    "class": "text-center"
                },
                {
                    "data": "oilTankTwo",
                    "class": "text-center",
                },
                {
                    "data": "fuelAmountOne",
                    "class": "text-center",
                }, {
                    "data": "fuelAmountTwo",
                    "class": "text-center",
                },
            ];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                var arr = startTime.split('-');
                var arr2 = endTime.split('-');
                var str = arr.join('');
                var str2 = arr2.join('');
                var newStartTime = str + '000000';
                var newEndTime = str2 + '235959';
                d.vehicleId = id;
                d.startTime = newStartTime;
                d.endTime = newEndTime;
                d.type = 0;
            };
            // 表格setting
            var setting = {
                listUrl: "/clbs/m/reportManagement/oilMassMileReport/getDetailData",
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'detailTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                drawCallbackFun: function () {
                    var api = myDetailTable.dataTable;
                    var startIndex = api.context[0]._iDisplayStart;//获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                    });
                },
                // pagingType: 'simple',
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true
            };
            myDetailTable = new TG_Tabel.createNew(setting);
            myDetailTable.init();

            $('#searchDetailTable').on('click', function (e) {
                myDetailTable.refresh();
            });
        },
        // 详情抽屉列表渲染2
        initDetailTable2: function (id) {
            var columnDefs = [{
                //第一列，用来显示序号
                "searchable": false,
                "targets": 0
            }];
            var columns = [
                {
                    //第一列，用来显示序号
                    "data": null,
                    "class": "text-center"
                },
                {
                    "data": "vTimeStr",
                    "class": "text-center",
                },
                {
                    "data": "oilTank",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (row.oilTankOne && row.oilTankTwo) {
                            return ((parseFloat(row.oilTankOne) + parseFloat(row.oilTankTwo)).toFixed(1)).toString();
                        }
                        return '-';
                    },
                }, {
                    "data": "oilTankOne",
                    "class": "text-center"
                },
                {
                    "data": "oilTankTwo",
                    "class": "text-center",
                },
                {
                    "data": "fuelSpillOne",
                    "class": "text-center",
                }, {
                    "data": "fuelSpillTwo",
                    "class": "text-center",
                },
            ];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                var arr = startTime.split('-');
                var arr2 = endTime.split('-');
                var str = arr.join('');
                var str2 = arr2.join('');
                var newStartTime = str + '000000';
                var newEndTime = str2 + '235959';
                d.vehicleId = id;
                d.startTime = newStartTime;
                d.endTime = newEndTime;
                d.type = 1;
            };
            // 表格setting
            var setting = {
                listUrl: "/clbs/m/reportManagement/oilMassMileReport/getDetailData",
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'detailTable2', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                drawCallbackFun: function () {
                    var api = myDetailTable2.dataTable;
                    var startIndex = api.context[0]._iDisplayStart;//获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                    });
                },
                // pagingType: 'simple',
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true
            };
            myDetailTable2 = new TG_Tabel.createNew(setting);
            myDetailTable2.init();

            $('#searchDetailTable').on('click', function (e) {
                myDetailTable2.refresh();
            });
        },
        getFuelAmountDetails: function (id) {
            $('#detail2').removeClass('active');
            $('#detail').addClass('active');
            fuelMileageReport.initDetailTable(id);
        },
        getFuelSpillDetails: function (id) {
            $('#detail').removeClass('active');
            $('#detail2').addClass('active');
            fuelMileageReport.initDetailTable2(id);
        },
        closeDrawer: function () {
            if ($('#detail').hasClass('active') || $('#detail2').hasClass('active')) {
                $('#detail').removeClass('active');
                $('#detail2').removeClass('active');
            }
        },
        getTable: function (table) {
            myTable = $(table).DataTable({
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
                    "targets": [0, 2, 3, 4, 5, 6, 7, 8, 9],
                    "searchable": false
                }],
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
            $('.toggle-vis').on('change', function (e) {
                var column = myTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });
            $("#search_button").on("click", function () {
                var tsval = $("#simpleQueryParam").val();
                fuzzyParam = tsval;
                myTable.column(1).search(tsval, false, false).draw();
            });
        },
        reloadData: function (dataList) {
            var currentPage = myTable.page();
            myTable.clear();
            myTable.rows.add(dataList);
            myTable.page(currentPage).draw(false);
            myTable.column(1).search('', false, false).page(currentPage).draw();
        },
        //刷新列表
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            var tsval = $("#simpleQueryParam").val();
            fuzzyParam = '';
            myTable.column(1).search(tsval, false, false).draw();
        },
        //模糊查询树
        searchVehicleTree: function (param) {
            ifAllCheck = false;//模糊查询不自动勾选
            crrentSubV = [];
            $('#charSelect').val('');
            if (param == null || param == undefined || param == '') {
                bflag = true;
                fuelMileageReport.treeInit()
            } else {
                bflag = true;
                var querySetting = {
                    async: {
                        url: "/clbs/v/oilmassmgt/oilvehiclesetting/oilSetMonitorTreeFuzzy",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"queryPattern": $('#queryType').val(), "param": param},
                        dataFilter: fuelMileageReport.ajaxQueryDataFilter
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
                        beforeClick: fuelMileageReport.beforeClickVehicle,
                        onCheck: fuelMileageReport.onCheckVehicle,
                        onExpand: fuelMileageReport.zTreeOnExpand,
                        onNodeCreated: fuelMileageReport.zTreeOnNodeCreated
                    }
                };
                $.fn.zTree.init($("#treeDemo"), querySetting, null);
            }
        },
        treeInit: function () {
            var setting = {
                async: {
                    url: fuelMileageReport.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    dataFilter: fuelMileageReport.ajaxDataFilter
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
                    beforeClick: fuelMileageReport.beforeClickVehicle,
                    onCheck: fuelMileageReport.onCheckVehicle,
                    beforeCheck: fuelMileageReport.zTreeBeforeCheck,
                    onExpand: fuelMileageReport.zTreeOnExpand,
                    onAsyncSuccess: fuelMileageReport.zTreeOnAsyncSuccess,
                    onNodeCreated: fuelMileageReport.zTreeOnNodeCreated
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },
        getTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/v/oilmassmgt/oilvehiclesetting/oilSetMonitorTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/v/oilmassmgt/oilvehiclesetting/oilSetMonitorTreeByAssignment?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&type=assignment";
            }
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            var rData;
            if (!responseData.msg) {
                rData = responseData;
            } else {
                rData = responseData.msg;
            }
            var obj = JSON.parse(ungzip(rData));
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
            return data;
        },
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            var resultData;
            if (!responseData.msg) {
                resultData = responseData;
            } else {
                resultData = responseData.msg;
            }
            responseData = JSON.parse(ungzip(resultData));
            var nodesArr = filterQueryResult(responseData, crrentSubV);
            for (var i = 0; i < nodesArr.length; i++) {
                nodesArr[i].open = true;
            }
            return nodesArr;
        },
    }
    $(function () {
        $('input').inputClear();
        //初始化页面
        fuelMileageReport.init();
        fuelMileageReport.getTable('#dataTable');
        //当前时间
        fuelMileageReport.getsTheCurrentTime();
        $('#timeInterval').dateRangePicker({
            dateLimit: 31, isOffLineReportFlag: true, nowDate: fuelMileageReport.getsTheCurrentTime(), isShowHMS: false
        });
        $("#groupSelect").bind("click", showMenuContent); //组织下拉显示
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var param = $("#groupSelect").val();
                fuelMileageReport.searchVehicleTree(param);
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
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                isSearch = true;
            }
            inputChange = setTimeout(function () {
                if (isSearch) {
                    var param = $("#groupSelect").val();
                    fuelMileageReport.searchVehicleTree(param);
                }
                isSearch = true;
            }, 500);
        });
        //导出
        $("#exportAlarm").bind("click", fuelMileageReport.exportAlarm);
        $("#refreshTable").bind("click", fuelMileageReport.refreshTable);
        $('#queryType').on('change', function () {
            var param = $("#groupSelect").val();
            fuelMileageReport.searchVehicleTree(param);
        });
        // 点击页面其他地方,关闭详情抽屉
        // $(document).on('click', fuelMileageReport.closeDrawer);
        // $('#detail').on('click', function (event) {
        //     event.stopPropagation();
        // });
    })
}(window, $))