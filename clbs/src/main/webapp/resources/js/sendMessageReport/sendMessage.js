(function (window, $) {
    // 车辆id列表
    var allVid;
    var vnameList = [];
    var startime;
    var endtime;
    var number;
    var zTreeIdJson = {};
    var checkFlag = false; // 判断组织节点是否是勾选操作
    var size;// 当前权限监控对象数量
    var ifAllCheck = true; //刚进入页面小于5000自动勾选
    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询？

    var messageDataList = [],
        messageTable, //消息表格对象
        messageDefaultList = [],
        messageDetailList = [],
        messageDetailTable;// 消息明细表格对象

    var activeRowIndex = null, activeRow = null, selectedMonitorId = "";  //点击列表index
    var drawerBox = $("#drawerBox"); //弹出消息明细列表抽屉id

    //获取页面部分变量
    var timeInterval = $("#timeInterval"); //时间输入框
    var statisticsObject = $("#statisticsObject"), //统计对象 组织树
        zTreeSearch = true,
        zTreeInput = $("#zTreeInput"); //统计对象 组织树 input框
    var sendMessageTable = $("#sendMessageTable"),  //消息表格
        queryPlateNumber = $("#queryPlateNumber"),//搜索 车牌号 搜索框
        queryPlateBtn = $("#queryPlateBtn");//搜索 车牌号 按钮
    var tableColumnSetting = $("#table-column-setting"), //表格显示列ul
        columnContainer = $("#setting-column-container");//表格显示列div
    var exportDataBtn = $("#exportTableData"),
        detailExportBtn = $("#detailExport"); //导出按钮

    var commonFun = {
        serializeObject: function (form) {
            var a, o, h, i, e;
            a = form.serializeArray();
            o = {};
            h = o.hasOwnProperty;
            for (i = 0; i < a.length; i++) {
                e = a[i];
                if (!h.call(o, e.name)) {
                    o[e.name] = e.value;
                }
            }
            return o;
        }
    }

    // 下发消息统计 页面下方法
    messageCommon = {
        //初始化统计对象组织树
        initTree: function (treeId) {
            var now = new Date();
            var nowYear = now.getFullYear();
            var nowMonth = now.getMonth() + 1;
            $("[data-toggle='tooltip']").tooltip();
            var setting = {
                async: {
                    url: messageCommon.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple"},
                    otherParam: {"icoType": "0"},
                    dataFilter: messageCommon.handleTreeData
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
                    beforeClick: messageCommon.zTreeBeforeClick,
                    onCheck: messageCommon.zTreeOnCheck,
                    beforeCheck: messageCommon.zTreeBeforeCheck,
                    onExpand: messageCommon.zTreeOnExpand,
                    onAsyncSuccess: messageCommon.zTreeOnAsyncSuccess,
                    onNodeCreated: messageCommon.zTreeOnNodeCreated
                }
            };
            if (treeId == null || treeId == undefined) {
                $.fn.zTree.init(statisticsObject, setting, null);
            }
            if (treeId === 'statisticsObject') {
                $.fn.zTree.init(statisticsObject, setting, null);
            }
        },
        getTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle";
            }
        },
        //单击节点之前的事件回调 beforeClick
        zTreeBeforeClick: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(treeId);
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        // checkbox / radio 被勾选 或 取消勾选的事件回调  onCheck
        zTreeOnCheck: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(treeId);
            // 若为取消勾选则不展开节点
            if (treeNode.checked) {
                if (treeId === 'statisticsObject') {
                    zTreeSearch = false;
                }
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    messageCommon.getCheckedNodes();
                    messageCommon.validatesOne();
                }, 600);
            }
            messageCommon.getCheckedNodes();
            messageCommon.getCharSelect(zTree);
        },
        // 勾选 或 取消勾选 之前的事件回调  beforeCheck
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { // 若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj(treeId), nodes = zTree
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

                    // 存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                    var ns = [];
                    // 节点id
                    var nodeId;
                    for (var i = 0; i < nodes.length; i++) {
                        nodeId = nodes[i].id;
                        if (nodes[i].type == "people" || nodes[i].type == "vehicle") {
                            // 查询该节点是否在勾选组织或分组下，若在则不记录，不在则记录
                            var nd = zTree.getNodeByParam("tId", nodes[i].tId, treeNode);
                            if (nd == null && $.inArray(nodeId, ns) == -1) {
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength += ns.length;
                } else if (treeNode.type == "people" || treeNode.type == "vehicle") { // 若勾选的为监控对象
                    var zTree = $.fn.zTree.getZTreeObj(treeId), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;
                    // 存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                    var ns = [];
                    // 节点id
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
                if (nodesLength > 5000) {
                    // layer.msg(maxSelectItem);
                    layer.msg('最多勾选5000个监控对象');
                    flag = false;
                }
            }
            if (flag) {
                // 若组织节点已被勾选，则是勾选操作，改变勾选操作标识
                if (treeNode.type == "group" && !treeNode.checked) {
                    checkFlag = true;
                }
            }
            return flag;
        },
        //节点被展开的事件回调  onExpand
        zTreeOnExpand: function (event, treeId, treeNode) {
            //判断是否是勾选操作展开的树(是则继续执行，不是则返回)
            if (treeNode.type == "group" && !checkFlag) {
                return;
            }
            //初始化勾选操作判断表示
            checkFlag = false;
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
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
        //异步加载正常结束的事件回调 onAsyncSuccess
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            if (size <= 100 && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            messageCommon.getCharSelect(treeObj);
        },
        //节点生成 DOM 后的事件回调 onNodeCreated
        zTreeOnNodeCreated: function (event, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(treeId);
            var id = treeNode.id.toString();
            var list = [];
            if (zTreeIdJson[id] == undefined || zTreeIdJson[id] == null) {
                list = [treeNode.tId];
                zTreeIdJson[id] = list;
            } else {
                zTreeIdJson[id].push(treeNode.tId)
            }
        },
        getCharSelect: function (treeObj) {
            var treeId = treeObj.setting.treeId;
            //var groupSelectId = '#groupSelect';
            var nodes = treeObj.getCheckedNodes(true);
            var allNodes = treeObj.getNodes();
            if (nodes.length > 0) {
                zTreeInput.val(allNodes[0].name);
                if (treeId == 'statisticsObject') {
                    zTreeInput.data('groupids', nodes.filter(function (ele) {
                        return ele.type == 'user';
                    }).map(function (ele) {
                        if (treeId == 'statisticsObject') {
                            if (ele.type == 'user') {
                                return ele.id;
                            }
                        } else {
                            return ele.uuid;
                        }

                    }).join(','))
                } else {
                    zTreeInput.data('groupids', nodes.map(function (ele) {
                        return ele.uuid;
                    }).join(','))
                }
            } else {
                zTreeInput.val("");
            }
        },
        // 获取到选择的节点
        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj("statisticsObject"),
                nodes = zTree.getCheckedNodes(true), v = "", vid = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == "vehicle") {
                    v += nodes[i].name + ",";
                    vid += nodes[i].id + ",";
                }
            }
            allVid = vid;
            vnameList = v;
        },
        //对统计对象组织树返回数据进行预处理
        handleTreeData: function (treeId, parentNode, responseData) {
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
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
        //搜索组织树
        searchVehicleTree: function (param) {
            ifAllCheck = false;//模糊查询不自动勾选
            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                messageCommon.initTree("statisticsObject")
            } else {
                bflag = true;
                var querySetting = {
                    async: {
                        url: "/clbs/a/search/reportFuzzySearch",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        // $('#queryType').val()  选择企业 监控对象 还是分组
                        otherParam: {"type": $('#queryType').val(), "queryParam": param},
                        dataFilter: messageCommon.handleQueryTreeData
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
                        beforeClick: messageCommon.zTreeBeforeClick,
                        onCheck: messageCommon.zTreeOnCheck,
                        onExpand: messageCommon.zTreeOnExpand,
                        onNodeCreated: messageCommon.zTreeOnNodeCreated
                    }
                };
                $.fn.zTree.init(statisticsObject, querySetting, null);
            }
        },
        handleQueryTreeData: function (treeId, parentNode, responseData) {
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
        //获取当前时间
        getsTheCurrentTime: function () {
            var nowDate = new Date();
            startTime = nowDate.getFullYear()
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
                    : nowDate.getDate())
            var atime = $("#atime").val();
            if (atime != undefined && atime != "") {
                startTime = atime;
            }
        },
        //获取月份
        getTheMonth: function (month) {
            var m = month;
            if (month.toString().length == 1) {
                m = "0" + month;
            }
            return m;
        },
        //获取前一天时间
        getBeforeDay: function (day) {
            var timer = timeInterval.val().split('--');
            var startValue = timer[0];
            var endValue = timer[1];
            if (startValue == "" || endValue == "") {
                var today = new Date();
                var targetday_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * day;
                today.setTime(targetday_milliseconds); // 注意，这行是关键代码
                var tYear = today.getFullYear();
                var tMonth = today.getMonth();
                var tDate = today.getDate();
                tMonth = messageCommon.getTheMonth(tMonth + 1);
                tDate = messageCommon.getTheMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate;
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24
                    * parseInt(num);
                today.setTime(end_milliseconds); // 注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = messageCommon.getTheMonth(endMonth + 1);
                endDate = messageCommon.getTheMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate;
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = messageCommon.getTheMonth(vMonth + 1);
                vDate = messageCommon.getTheMonth(vDate);
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
                    vendMonth = messageCommon.getTheMonth(vendMonth + 1);
                    vendDate = messageCommon.getTheMonth(vendDate);
                    endTime = vendYear + "-" + vendMonth + "-" + vendDate;
                }
            }
        },
        // 获取当前月或上一月
        getCurrentMonth: function (parm) {
            var nowDate = new Date();
            var y = nowDate.getFullYear();
            var m = nowDate.getMonth() + 1;
            if (parm) {
                m = nowDate.getMonth();
            }
            if (m < 10) {
                m = '0' + m
            }
            return y + '-' + m;
        },
        // 获取当月天数
        getDaysInOneMonth: function (year, month) {
            month = parseInt(month, 10);
            var d = new Date(year, month, 0);
            return d.getDate();
        },
        //获取后一天
        getTheLastDay: function (date, maxDay) {
            var _year = date[0] * 1, _month = date[1] * 1, _day = date[2] * 1;
            var statu = _day < maxDay;
            if (statu) {
                return _year + "-" + (_month <= 9 ? "0" + _month : _month) + "-" + (_day <= 8 ? "0" + (_day + 1) : _day + 1);
            }
            _year = _month == 12 ? _year + 1 : _year;
            _month = _month == 12 ? 1 : (!statu ? _month + 1 : _month);
            var newDate = _year + "-" + (_month <= 9 ? "0" + _month : _month) + "-01";
            return newDate;
        },
        replaceAll: function (text, before, newText) {
            return text.replace(new RegExp(before, "gm"), newText)
        },
        validatesOne: function () {
            return $("#zTreeForm").validate({
                rules: {
                    zTreeInput: {
                        zTreeChecked: "statisticsObject"
                    }
                },
                messages: {
                    zTreeInput: {
                        zTreeChecked: vehicleSelectBrand
                    }
                }
            }).form();
        },
        //初始化下发消息 表格数据
        getMessageTable: function (table) {
            var _columns = [
                {data: null},
                {
                    data: "monitorName", render: function (data, type, row, meta) {
                        return '<div class="monitorDiv" data-id="' + row.monitorId + '">' + row.monitorName + '</div>';
                        // console.log(data, type, row, meta)
                    }
                },
                {data: "plateColor"},
                {data: "objectType"},
                {
                    data: "groupName",
                    render: function (data, type, row, meta) {
                        var content = html2Escape(data, false);
                        if (content.length < 13) {
                            return '<div>' + content + '</div>';
                        } else {
                            return '<div class="tableCompany" data-toggle="tooltip" alt="' + content + '">' + content.substring(0, 12) + '...</div>';
                        }
                    }
                },
                {data: "totalNum"},
                {data: "successNum"},
                {data: "failNum"},
                {data: "manualIssueSucNum"},
                {data: "manualIssueFailNum"},
                {data: "sysIssueSucNum"},
                {data: "sysIssueFailNum"}
            ];
            tableColumnSetting.find('.toggle-vis').prop('checked', 'true');
            messageTable = $(table).DataTable({
                "destroy": true,
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
                "columnDefs": [{
                    "targets": [0, 2, 3, 4, 5, 6],
                    "searchable": false
                }],
                "columns": _columns,
                "order": [
                    [0, null]
                ]
            });
            messageTable.off('order.dt search.dt').on('order.dt search.dt', function () {
                messageTable.column(0, {
                    search: 'applied',
                    order: 'applied'
                }).nodes().each(function (cell, i) {
                    cell.innerHTML = i + 1;
                });
            }).draw();
            tableColumnSetting.find('.toggle-vis').off('change').on('change', function (e) {
                var column = messageTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                columnContainer.addClass("open");
            });
            queryPlateBtn.off('click').on("click", function () {
                var query = queryPlateNumber.val();
                var newmessages = [];
                messageDefaultList.forEach(function (item) {
                    if (item.monitorName.indexOf(query) != -1) {
                        newmessages.push(item);
                    }
                })
                messageDataList = newmessages;
                messageTable.column(1).search(query, false, false).draw();
            });
        },
        //表格显示列设置
        settingTableColumn: function () {
            var table_column = "";
            var columns = sendMessageTable.find("tr th:gt(0)");
            for (var i = 0; i < columns.length; i++) {
                if (!columns[i].getAttribute("colspan")) {
                    var _column = columns[i].getAttribute("data-index");
                    table_column += "<li>" +
                        "<label>" +
                        "<input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(_column - 1) + "\" />"
                        + columns[i].innerHTML + "</label>" +
                        "</li>"
                }
            }
            ;
            tableColumnSetting.html(table_column);
        },
        //查询按钮点击事件
        inquireClick: function (num) {  // 查询按钮的单击事件
            $(".ToolPanel").css("display", "block");
            queryPlateNumber.val("");
            messageCommon.getCheckedNodes();
            number = num;
            if (number == 0) {
                messageCommon.getsTheCurrentTime();
            } else if (number == -1) {
                messageCommon.getBeforeDay(-1)
            } else if (number == -3) {
                messageCommon.getBeforeDay(-3)
            } else if (number == -7) {
                messageCommon.getBeforeDay(-7)
            }
            if (number != 1) {
                timeInterval.val(startTime + '--' + endTime);
                // 从页面获取到开始时间
                startime = startTime;
                // 从页面获取到结束时间
                endtime = endTime;
            } else {
                var timer = timeInterval.val().split('--');
                startime = timer[0];
                endtime = timer[1];
            }
            ;
            // 请求路径
            var url = "/clbs/lkyw/sendMessageReport/list";
            if (startime > endtime) {
                layer.msg(endtimeComStarttime, {move: false});
                return;
            }
            if (!messageCommon.validatesOne()) {
                return;
            }

            var _start = messageCommon.replaceAll(startime, "-", ""),
                _end = messageCommon.replaceAll(endtime, "-", "");
            // 请求参数
            var param = {"vehicleIds": allVid, "startTime": _start, "endTime": _end};
            // 发送请求给服务器
            json_ajax("POST", url, "json", true, param, messageCommon.handleMessageTable);
        },
        //查询获取数据回调
        handleMessageTable: function (data) {
            if (data.success == true) {
                // 组织表头
                messageList = [];// 用来存储服务器返回的数据，以便于将数据加载到页面上
                if (data.obj != null && data.obj.length != 0) {
                    messageList = data.obj;
                    messageDefaultList = data.obj;
                    messageCommon.reloadMessageTable(messageList);
                    queryPlateNumber.val("");
                    queryPlateBtn.click();
                    exportDataBtn.removeAttr('disabled');
                } else {
                    queryPlateNumber.val("");
                    queryPlateBtn.click();
                    messageCommon.reloadMessageTable(messageList);
                }
            } else {
                layer.msg(data.msg || publicError, {move: false});
            }
        },
        //重载下发消息列表
        reloadMessageTable: function (dataList) {
            messageDataList = dataList;
            var currentPage = messageTable.page();
            messageTable.clear();
            messageTable.rows.add(dataList);
            messageTable.page(currentPage).draw(false);
        },
        //导出表格数据 方法
        exportMessageData: function () {
            if (!messageDataList.length) {
                layer.msg('暂未查询到可以导出的数据');
                return false;
            }
            ;
            var timer = timeInterval.val().split('--');
            var startime = timer[0];
            var endtime = timer[1];
            // 请求参数
            messageCommon.getCheckedNodes();
            if (allVid == null || allVid == "") {
                layer.msg(monitoringObjecNull);
                return;
            }
            var pdata = {
                "vehicleIds": allVid,
                "startTime": messageCommon.replaceAll(startime, "-", ""),
                "endTime": messageCommon.replaceAll(endtime, "-", "")
            };
            console.log(pdata)
            // 请求路径
            var url = "/clbs/lkyw/sendMessageReport/export";
            if(getRecordsNum('sendMessageTable_info') > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            // 发送请求给服务器
            exportExcelUsePost(url, pdata);
        },
        //消息明细 表格初始化
        initMessageDetail: function (table) {
            var _columns = [
                {data: null},
                {data: "monitorName"},
                {data: "signColor"},
                {data: "objectType"},
                {
                    data: "groupName",
                    render: function (data, type, row, meta) {
                        var content = data ? html2Escape(data, false) : '--';
                        if (content.length < 13) {
                            return '<div>' + content + '</div>';
                        } else {
                            return '<div class="tableCompany" data-toggle="tooltip" alt="' + content + '">' + content.substring(0, 12) + '...</div>';
                        }
                    }
                },
                {
                    data: "msgContent",
                    render: function (data, type, row, meta) {
                        var content = data ? html2Escape(data, false) : '--';
                        if (content.length < 13) {
                            return '<div>' + content + '</div>';
                        } else {
                            return '<div class="tableCompany" data-toggle="tooltip" alt="' + html2Escape(content, false) + '">' + content.substring(0, 12) + '...</div>';
                        }
                    }
                },
                {data: "playType"},
                {data: "sendType"},
                {data: "sendUserName"},
                {data: "sendStatus"},
                {data: "sendTime"}
            ];
            messageDetailTable = $("#messageDetail").DataTable({
                "destroy": true,
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
                "columns": _columns,
                "columnDefs": [{
                    "targets": [0, 2, 3, 4, 5, 6, 7, 8, 9],
                    "searchable": false
                }],
                "order": [
                    [0, null]
                ],// 第一列排序图标改为默认
            });
            messageDetailTable.on('order.dt search.dt', function () {
                messageDetailTable.column(0, {
                    search: 'applied',
                    order: 'applied'
                }).nodes().each(function (cell, i) {
                    cell.innerHTML = i + 1;
                });
            }).draw();
            // 模糊查询
            $("input[name='messageInfo']").keyup(function (event) {
                if (event.keyCode == 13) {
                    messageCommon.queryDetailClick();
                }
            });
        },
        //消息列表tr点击事件
        handleMessageClick: function (event) {
            event.stopPropagation();
            var _this = $(this), _index = _this.index(), activeId = $(event.target).parents('tr').find('td:nth-child(2)').find('div').attr('data-id'); // messageTable.data()[_index].monitorId; //_this.index(),this.sectionRowIndex
            if (!messageDataList.length || _index == activeRowIndex || !activeId) return false;
            if (_index !== activeRowIndex) {
                $("#searchDetailForm")[0].reset();
                activeRowIndex = _index;
                activeRow = messageDataList[_index];
                selectedMonitorId = activeId;
                messageDetailTable.clear();
                $("#messageDetail").find("tbody").html("");
                messageDetailList = [];
                drawerBox.addClass('active');
                detailExportBtn.attr("disabled", "disabled");
                messageCommon.queryDetailClick();
            } else {
                messageCommon.closeDrawer();
            }
        },
        queryDetailClick: function () {
            var form = $("#searchDetailForm");
            var msgInfo = commonFun.serializeObject(form);
            if (!activeRow.monitorId) return false;
            var url = "/clbs/lkyw/sendMessageReport/detail";
            var _startime = startime.split("-");
            var _endtime = endtime.split("-");
            var _year = _endtime[0], _month = _endtime[1], _day = _endtime[2];
            var maxDay = messageCommon.getDaysInOneMonth(_year, _month);
            var newEndTime = messageCommon.getTheLastDay(_endtime, maxDay);
            var param = {
                monitorId: selectedMonitorId,
                startTime: messageCommon.replaceAll(startime, "-", "") + "000000",
                endTime: messageCommon.replaceAll(newEndTime, "-", "") + "000000",
                msgContent: msgInfo.messageInfo,
                // sendType: msgInfo.issueWay != 2 ? msgInfo.issueWay : '',
                // sendStatus: msgInfo.issueStatus != 2 ? msgInfo.issueStatus : '',
                //0:系统下发 1:人工下发（不传就是全部）
                //0:下发成功 1: 下发失败（不传就是全部）
            }
            // console.log(msgInfo)
            if (msgInfo.issueWay != 2) param.sendType = msgInfo.issueWay;
            if (msgInfo.issueStatus != 2) param.sendStatus = msgInfo.issueStatus;
            // 发送请求给服务器
            json_ajax("POST", url, "json", true, param, messageCommon.handleMessageDetail);
        },
        handleMessageDetail: function (data) {
            messageDetailList = [];
            detailExportBtn.attr("disabled", "disabled");
            if (data.success == true) {
                messageDetailList = data.obj;
                if (messageDetailList.length) detailExportBtn.removeAttr("disabled");
                messageCommon.updateMessageDetail(messageDetailList);
            } else {
                drawerBox.removeClass('active');
                detailExportBtn.attr("disabled", "disabled");
                layer.msg(data.msg || publicError, {move: false});
            }
        },
        //更新消息明细 表格
        updateMessageDetail: function (dataList) {
            var currentPage = messageDetailTable.page();
            messageDetailTable.clear();
            messageDetailTable.rows.add(dataList);
            messageDetailTable.page(currentPage).draw(false);
        },
        //关闭消息明细 抽屉
        closeDrawer: function () {
            drawerBox.removeClass('active');
            detailExportBtn.attr("disabled", "disabled");
            activeRowIndex = null;
        }
    }

    second2Hour = function (second, hms) {
        if (second === null || second === undefined || isNaN(second)) {
            return second;
        }
        return parseInt(second * 10) / 10;
    };
    $(function () {
        messageCommon.initTree(); //初始化页面组织树
        messageCommon.settingTableColumn(); //初始化表格显示列
        messageCommon.getMessageTable('#sendMessageTable'); //初始化消息表格
        messageCommon.initMessageDetail('#messageDetail'); //初始化消息明细表格
        messageCommon.getsTheCurrentTime();

        $(document).on('mouseover', '.tableCompany', function () {
            var _this = $(this);
            if (_this.attr("alt")) {
                _this.justToolsTip({
                    animation: "moveInTop",
                    width: "auto",
                    contents: _this.attr("alt"),
                    gravity: 'top'
                });
            }
        });
        timeInterval.dateRangePicker({dateLimit: 31, timePicker: false, isShowHMS: false});

        exportDataBtn.bind("click", messageCommon.exportMessageData);  //导出按钮绑定点击事件
        zTreeInput.bind("click", showMenuContent);  //统计对象组织树输入框绑定事件

        //为每行tr绑定点击事件
        sendMessageTable.on("click", "#messageTbody tr", messageCommon.handleMessageClick);

        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'zTreeInput') {
                var param = zTreeInput.val();
                messageCommon.searchVehicleTree(param);
            }
            ;
        });
        /**
         * 监控对象树模糊查询
         */
        var zTreeInputTimer;
        zTreeInput.on('input oninput', function (value) {
            if (zTreeInputTimer !== undefined) {
                clearTimeout(zTreeInputTimer);
            }
            ;
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                zTreeSearch = true;
            }
            ;
            zTreeInputTimer = setTimeout(function () {
                if (zTreeSearch) {
                    var param = zTreeInput.val();
                    messageCommon.searchVehicleTree(param);
                }
                zTreeSearch = true;
            }, 500);
        });
        $('#queryType').on('change', function () {
            var param = zTreeInput.val();
            messageCommon.searchVehicleTree(param);
        });
        //刷新表格
        $("#refreshTableBtn").bind("click", function () {
            queryPlateNumber.val("");
            var query = queryPlateNumber.val();
            messageTable.column(1).search(query, false, false).draw();
        });
        queryPlateNumber.bind('keyup', function (event) {
            if (event.keyCode == "13") {
                //回车执行查询
                queryPlateBtn.click();
            }
        });

        //查询
        $("#filterMsgDetail").on("click", messageCommon.queryDetailClick);
        $(document).on('click', messageCommon.closeDrawer);
        drawerBox.on('click', function (event) {
            event.stopPropagation();
        });

        detailExportBtn.on("click", function () {
            if (!activeRow.monitorId || !messageDetailList.length) return false;
            var _startime = startime.split("-");
            var _endtime = endtime.split("-");
            var _year = _endtime[0], _month = _endtime[1], _day = _endtime[2];
            var maxDay = messageCommon.getDaysInOneMonth(_year, _month);
            var newEndTime = messageCommon.getTheLastDay(_endtime, maxDay);
            var param = {
                monitorId: activeRow.monitorId,
                startTime: _startime.join("") + "000000",
                endTime: newEndTime.split("-").join("") + "000000"
            }
            if(getRecordsNum('messageDetail_info') > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var url = "/clbs/lkyw/sendMessageReport/exportDetail";
            exportExcelUseFormGet(url, param);
        })
    })
})(window, $)
