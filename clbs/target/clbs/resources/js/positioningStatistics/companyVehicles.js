(function (window, $) {
    var vehiclePositionNames = [],
        vehiclePositionIds = [];

    var monthPositionNames = [],
        monthPositionIds = [],
        monthIsSearch = true;

    var unusualPositionNames = [],
        unusualPositionIds = [],
        unusualIsSearch = true;

    //开始时间
    var startTime;
    var endTime;

    var zTreeIdJson = {};
    var checkFlag = false; // 判断组织节点是否是勾选操作

    var size; // 当前权限监控对象数量
    var ifAllCheck = true; //刚进入页面小于5000自动勾选
    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询？


    var activeRowIndex = null; //点击列表index
    var drawerBox = $("#drawerBox"),
        drawerCompany = $("#drawerCompany"),
        drawerUnusual = $("#drawerUnusual"); //弹出消息明细列表抽屉id

    var groupInputs = $("input[name='groupInput']"); //组织树的输入框

    //企业车辆定位统计 vehiclePosition

    var vehiclePositionInput = $("#vehiclePositionInput"); //组织树vehiclePositionTree输入框
    var vehiclePositionExport = $("#vehiclePositionExport"); //导出按钮

    var vehicleTableObject, //表格对象和数据列表
        vehiclePositionList = [];

    var vehiclePositionTable = $("#vehiclePositionTable"), //表格
        vehiclePositionSearch = $("#vehiclePositionSearch"), //表格搜索按钮
        vehiclePositionQuery = $("#vehiclePositionQuery"), //表格搜索输入框
        vehiclePositionRefresh = $("#vehiclePositionRefresh"), //表格刷新按钮
        vehiclePositionColumn = $("#vehiclePositionColumn"); //表格 列

    var vehiclePositionTime = $("#vehiclePositionTime"); //时间

    //车辆月度定位统计 monthPosition
    var monthPositionInput = $("#monthPositionInput"), //组织树monthPositionTree输入框
        monthPositionType = $("#monthPositionType"); //组织树监控对象选择
    var monthPositionExport = $("#monthPositionExport"); //导出按钮

    var monthTableObject, //表格对象和数据列表
        monthPositionList = [];

    var monthPositionTable = $("#monthPositionTable"), //表格
        monthPositionSearch = $("#monthPositionSearch"), //表格搜索按钮
        monthPositionQuery = $("#monthPositionQuery"), //表格搜索输入框
        monthPositionRefresh = $("#monthPositionRefresh"), //表格刷新按钮
        monthPositionColumn = $("#monthPositionColumn"); //表格 列

    var monthPositionTime = $("#monthPositionTime");

    //异常定位统计 unusualPosition
    var unusualPositionInput = $("#unusualPositionInput"), //组织树monthPositionTree输入框
        unusualPositionType = $("#unusualPositionType"); //组织树监控对象选择;
    var unusualPositionExport = $("#unusualPositionExport"); //导出按钮

    var unusualTableObject, //表格对象和数据列表
        unusualPositionList = [];

    var unusualPositionTable = $("#unusualPositionTable"), //表格
        unusualPositionSearch = $("#unusualPositionSearch"), //表格搜索按钮
        unusualPositionQuery = $("#unusualPositionQuery"), //表格搜索输入框
        unusualPositionRefresh = $("#unusualPositionRefresh"), //表格刷新按钮
        unusualPositionColumn = $("#unusualPositionColumn"); //表格 列

    var unusualPositionTime = $("#unusualPositionTime");

    var detailExport = $("#detailExportBtn"); //明细导出按钮
    var getAddressStatus = false;

    var dbValue = false //树双击判断参数
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

    var publicFunction = {
        //格式化年月
        formatMonth: function (monthStr) {
            var now = new Date(monthStr);
            var year = now.getFullYear();
            var month = now.getMonth() + 1;
            var add0 = function (n) {
                if (n < 10) {
                    return '0' + n.toString();
                }
                return n.toString()
            };
            return year + '-' + add0(month);
        },
        //选择时间下拉框
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
            for (var i = 0; i < 12; i++) {
                if (i < month) {
                    select.append($(tmpl.replace(/\$name/g, year + '-' + add0(month - i))));
                } else {
                    select.append($(tmpl.replace(/\$name/g, (year - 1) + '-' + add0(12 - i + month))));
                }
            }
        },
        //获取当前时间
        getTheCurrentTime: function () {
            var nowDate = new Date();
            startTime = nowDate.getFullYear() +
                "-" +
                (parseInt(nowDate.getMonth() + 1) < 10 ? "0" +
                    parseInt(nowDate.getMonth() + 1) :
                    parseInt(nowDate.getMonth() + 1)) +
                "-" +
                (nowDate.getDate() < 10 ? "0" + nowDate.getDate() :
                    nowDate.getDate());
            endTime = nowDate.getFullYear() +
                "-" +
                (parseInt(nowDate.getMonth() + 1) < 10 ? "0" +
                    parseInt(nowDate.getMonth() + 1) :
                    parseInt(nowDate.getMonth() + 1)) +
                "-" +
                (nowDate.getDate() < 10 ? "0" + nowDate.getDate() :
                    nowDate.getDate());
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
        //获取之前的天数
        getBeforeDay: function (day, times) {
            var timer = times.split('--');
            var startValue = timer[0];
            var endValue = timer[1];
            if (startValue == "" || endValue == "") {
                var today = new Date();
                var targetday_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * day;
                today.setTime(targetday_milliseconds); // 注意，这行是关键代码
                var tYear = today.getFullYear();
                var tMonth = today.getMonth();
                var tDate = today.getDate();
                tMonth = publicFunction.getTheMonth(tMonth + 1);
                tDate = publicFunction.getTheMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate;
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 *
                    parseInt(num);
                today.setTime(end_milliseconds); // 注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = publicFunction.getTheMonth(endMonth + 1);
                endDate = publicFunction.getTheMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate;
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = publicFunction.getTheMonth(vMonth + 1);
                vDate = publicFunction.getTheMonth(vDate);
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
                    vendMonth = publicFunction.getTheMonth(vendMonth + 1);
                    vendDate = publicFunction.getTheMonth(vendDate);
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
        // 判断时间范围
        compareTime: function (sDate1, sDate2) {
            var dateSpan,
                tempDate,
                iDays;
            sDate1 = Date.parse(sDate1);
            sDate2 = Date.parse(sDate2);
            dateSpan = sDate2 - sDate1;
            dateSpan = Math.abs(dateSpan);
            iDays = Math.floor(dateSpan / (24 * 3600 * 1000));
            // return iDays
            return (iDays + 1) //加一才是正常天数
        },
        //导出表格数据
        exportTableData: function (url) {
            window.location.href = url;
        },
        initDataTable: function (table) {
            var tableObject = null;
            tableObject = $(table).DataTable({
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
                ]
            });
            //显示隐藏列
            $(table).parents(".panel-body").find('.toggle-input').on('change', function (e) {
                var column = tableObject.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });
            return tableObject;
        },
        destroyTable: function (tableObject) {
            if (tableObject) {
                if (tableObject.dataTable) {
                    tableObject.dataTable.destroy();
                    tableObject.dataTable.clear();
                    tableObject.dataTable = null;
                    tableObject = null;
                    return false;
                }
                tableObject.destroy();
                tableObject.clear();
                tableObject = null;
            }
        },
        computedNumber: function (number) {
            return Math.round(number * 100) / 100;
        },

    }

    //车辆定位统计
    companyVehicles = {
        tableConfig: {
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
            "columnDefs": [{
                "targets": [0, 2, 3, 4, 5, 6, 7, 8, 9],
                "searchable": false
            }],
            "order": [
                [0, null]
            ], // 第一列排序图标改为默认
        },
        updateIndexColumn: function (oSettings) {
            console.log(oSettings)
            var api = myTable.dataTable;
            var startIndex = api.context[0]._iDisplayStart; //获取到本页开始的条数
            api.column(0).nodes().each(function (cell, i) {
                cell.innerHTML = startIndex + i + 1;
            });
        },
        closeDrawer: function () {
            drawerCompany.find(".nav-tabs li:nth(0)").addClass("active").siblings("li").removeClass("active");
            drawerCompany.find(".tab-content .tab-pane:nth(0)").addClass("active").siblings(".tab-pane").removeClass("active");
            drawerBox.removeClass('active');
            drawerCompany.hide();
            drawerUnusual.hide();
            // $("body").css({"overflow": "auto"});
            detailExport.attr("disabled", "disabled");
            activeRowIndex = null;
            drawerBox.find("input[name=simpleQueryParam]").val("");
        },
        //对显示的数据进行逆地址解析
        getAddress: function (addressStr) {
            getAddressStatus = true;
            var $dataTableTbody = $("#breakDetailTable tbody");
            var dataLength = $dataTableTbody.children("tr").length;
            var num = 0;
            for (var i = 0; i < dataLength; i++) {
                num++;
                var n = $dataTableTbody.children("tr:nth-child(" + (i + 1) + ")").children("td:nth-child(1)").text();
                var startLoc = addressStr[i].startLocation,
                    endLoc = addressStr[i].endLocation;
                var startMsg = [],
                    endMsg = [],
                    address = [];
                //经纬度正则表达式
                var Reg = /^(180\.0{4,7}|(\d{1,2}|1([0-7]\d))\.\d{4,20})(,)(90\.0{4,8}|(\d|[1-8]\d)\.\d{4,20})$/;
                // console.log(startLoc, startLoc != null && Reg.test(startLoc))
                if (startLoc != null && (Reg.test(startLoc) || startLoc === '0.0,0.0')) {
                    startMsg = [startLoc.split(",")[0], startLoc.split(",")[1]];
                } else {
                    startMsg = ["124.411991", "29.043817"];
                };
                // console.log(endLoc, endLoc != null && Reg.test(endLoc))
                if (endLoc != null && (Reg.test(endLoc) || endLoc === '0.0,0.0')) {
                    endMsg = [endLoc.split(",")[0], endLoc.split(",")[1]];
                } else {
                    endMsg = ["124.411991", "29.043817"];
                };
                breakDetail.startAddressMsg.push(startMsg);
                breakDetail.endAddressMsg.push(endMsg);
                if (num == dataLength) {
                    var addressIndex = 0;
                    var addressArray = [];
                    backAddressMsg1(addressIndex, breakDetail.startAddressMsg, null, addressArray, "breakDetailTable", 9);
                    backAddressMsg1(addressIndex, breakDetail.endAddressMsg, null, addressArray, "breakDetailTable", 10);
                    breakDetail.startAddressMsg = [];
                    breakDetail.endAddressMsg = [];
                }
            }
        }
    }
    //企业车辆定位统计
    vehiclePosition = {
        requestFlag: false,
        selectGroupId: '',
        exportBatchUrl: "/clbs/lkyw/vehicle/positioning/statistics/exportAllGroupPositioning",
        exportUrl: "/clbs/lkyw/vehicle/positioning/statistics/exportGroupList\n",
        treeUrl: "/clbs/m/basicinfo/enterprise/professionals/tree",
        treeId: "vehiclePositionTree",
        tableListUrl: "/clbs/lkyw/vehicle/positioning/statistics/group/list",
        tableDetailUrl: "",
        tableEditUrl: "",
        tableDeleteUrl: "",
        tableId: "vehiclePositionTable",
        tableList: [],
        init: function () {
            var nowT = new Date();
            var yearT = nowT.getFullYear();
            var monthT = nowT.getMonth() + 1;
            var dayT = nowT.getDate();
            monthT = monthT <= 9 ? '0' + monthT : monthT;
            dayT = dayT <= 9 ? '0' + dayT : dayT;
            var nowDay = yearT + '-' + monthT + '-' + dayT;
            vehiclePositionTime.dateRangePicker({
                dateLimit: 31,
                isShowHMS: false,
                nowDate: nowDay,
                isOffLineReportFlag: true
            });
            $('[data-toggle=\'tooltip\']').tooltip();
            vehiclePosition.initTree(); //组织树
            vehiclePosition.settingTableColumn(); //表格设置列
            vehicleTableObject = publicFunction.initDataTable('#' + vehiclePosition.tableId);
            //vehiclePosition.initTable(); //初始化表格
            vehiclePositionInput.bind("click", showMenuContent); //为组织树的输入框绑定点击事件
            //组织树输入框 模糊搜索
            vehiclePositionInput.on('input propertychange', function (value) {
                var treeObj = $.fn.zTree.getZTreeObj(vehiclePosition.treeId);
                treeObj.checkAllNodes(false);
                search_ztree(vehiclePosition.treeId, 'vehiclePositionInput', 'group');
            });
        },
        //初始化组织树
        initTree: function () {
            //车辆树  vehiclePositionTree
            var setting = {
                async: {
                    url: vehiclePosition.treeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "type": "multiple",
                        "icoType": "0"
                    },
                    dataFilter: vehiclePosition.ajaxTreeDataFilter
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
                    beforeClick: vehiclePosition.beforeClickVehicle,
                    onAsyncSuccess: vehiclePosition.zTreeOnAsyncSuccess,
                    // beforeCheck: vehiclePosition.zTreeBeforeCheck,
                    onCheck: vehiclePosition.onCheckVehicle,
                    onExpand: vehiclePosition.zTreeOnExpand,
                    onDblClick: vehiclePosition.onDblClickVehicle,

                }
            };
            $.fn.zTree.init($("#" + vehiclePosition.treeId), setting, null);
        },
        onDblClickVehicle: function (e, treeId, treeNode) {
            if (!treeNode) return
            dbValue = true
            var treeObj = $.fn.zTree.getZTreeObj("vehiclePositionTree");
            treeObj.checkAllNodes(false);
            vehiclePosition.getCheckedNodes("vehiclePositionTree");
            // $("#vehiclePositionInput").val(treeNode.name);
            // search_ztree('vehiclePositionTree', 'vehiclePositionInput', 'group');
            treeObj.checkNode(treeNode, !treeNode.checked, false, true);
        },
        //处理组织树数据回调
        ajaxTreeDataFilter: function (treeId, parentNode, responseData) {
            var treeObj = $.fn.zTree.getZTreeObj(vehiclePosition.treeId);
            if (responseData.length) {
                var data = [];
                for (var i = 0; i < responseData.length; i++) { //只取type=group的
                    if (responseData[i].type == 'group') {
                        data.push(responseData[i])
                    }
                }
                for (var j = 0; j < data.length; j++) {
                    data[j].open = true;
                }
            }
            size = data.length;
            return data;
        },
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(vehiclePosition.treeId);
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj(vehiclePosition.treeId);
            if (size <= 100 && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            vehiclePosition.getCharSelect(treeObj);
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true,
                selectedNodes = [];
            var selectedfilter = function (node) {
                return (node.type == "group" && node.checked);
            }
            var allfilter = function (node) {
                return (node.type == "group");
            }
            if (!treeNode.checked && !dbValue) {
                if (treeNode.type == "group") {
                    var zTree = $.fn.zTree.getZTreeObj(treeId),
                        nodes = zTree.getNodesByFilter(selectedfilter),
                        v = "";
                    var nodeItems = zTree.getNodesByFilter(allfilter, false, treeNode);
                    if (nodes.length + nodeItems.length + 1 > 100) {
                        layer.msg("最大支持选中100个组织" + '<br/>双击名称可选中本组织');
                        flag = false;
                    }
                }
            }
            return flag;
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            dbValue = false
            var zTree = $.fn.zTree.getZTreeObj(vehiclePosition.treeId),
                nodes = zTree.getCheckedNodes(true),
                checkedArr = [];
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                // setTimeout(function () {
                    zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                // }, 1200);
                setTimeout(() => {
                    vehiclePosition.getCheckedNodes();
                    vehiclePosition.validates();
                }, 600);
            }
            vehiclePosition.getCharSelect(zTree);
            vehiclePosition.getCheckedNodes();
        },
        zTreeOnExpand: function (event, treeId, treeNode) {
            //判断是否是勾选操作展开的树(是则继续执行，不是则返回)
            if (treeNode.type == "group" && !checkFlag) {
                return;
            }
            //初始化勾选操作判断表示
            checkFlag = false;
            var treeObj = $.fn.zTree.getZTreeObj(vehiclePosition.treeId);
            if (treeNode.type == "group") {
                var assign = []; // 当前组织及下级组织的所有分组
                vehiclePosition.getGroupChild(treeNode, assign);
                if (assign != null && assign.length > 0) {
                    for (var i = 0; i < assign.length; i++) {
                        var node = assign[i];
                        if (node.type == "assignment" && node.children === undefined) {
                            if (!node.zAsync) { // 判断节点是否进行异步加载，若没有，则先异步加载，避免添加重复节点
                                treeObj.reAsyncChildNodes(node, "refresh");
                            }
                        }
                    }
                }
            }
        },
        getGroupChild: function (node, assign) {
            var nodes = node.children;
            if (nodes != null && nodes != undefined && nodes.length > 0) {
                for (var i = 0; i < nodes.length; i++) {
                    var node = nodes[i];
                    if (node.type == "assignment") {
                        assign.push(node);
                    } else if (node.type == "group" && node.children != undefined) {
                        vehiclePosition.getGroupChild(node.children, assign);
                    }
                }
            }
        },
        getCharSelect: function (treeObj) {
            var nodes = treeObj.getCheckedNodes(true);
            var allNodes = treeObj.getNodes();
            //设置组织树 输入框
            if (nodes.length > 0) {
                vehiclePositionInput.val(allNodes[0].name);
            } else {
                vehiclePositionInput.val("");
            }
        },
        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj(vehiclePosition.treeId),
                nodes = zTree
                .getCheckedNodes(true),
                v = "",
                vid = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == "group") {
                    v += nodes[i].name + ",";
                    vid += nodes[i].uuid + ","; //取uuid
                }
            }
            vehiclePositionNames = v;
            vehiclePositionIds = vid;
        },
        //表格显示列设置
        settingTableColumn: function () {
            var table_column = "";
            var columns = vehiclePositionTable.find("tr th:gt(0)");
            var columnData = [{
                    name: "道路运输企业",
                    column: 1
                },
                {
                    name: "车辆数",
                    column: 2
                },
                {
                    name: "合计定位总数",
                    column: 3
                },
                {
                    name: "合计无效定位数",
                    column: 4
                },
                {
                    name: "有效率",
                    column: 5
                },
                {
                    name: "无定位车辆数",
                    column: 6
                },
                {
                    name: "无定位率",
                    column: 7
                },
                {
                    name: "车辆数",
                    column: 8
                },
                {
                    name: "次数",
                    column: 9
                },
                {
                    name: "车辆数",
                    column: 10
                },
                {
                    name: "次数",
                    column: 11
                }
            ];
            for (var i = 0; i < columnData.length; i++) {
                table_column += "<li>" +
                    "<label>" +
                    "<input type=\"checkbox\" checked=\"checked\" class=\"toggle-input toggle-checkbox\" data-column=\"" + parseInt(columnData[i].column) + "\" />" +
                    columnData[i].name + "</label>" +
                    "</li>";
            };
            vehiclePositionColumn.html(table_column);
        },
        //初始化表格
        initTable: function (searchType) {
            publicFunction.destroyTable(vehicleTableObject);
            var _columns = [{
                        data: null
                    },
                    {
                        data: "groupName",
                        render: function (data, type, row, meta) {
                            //250px 长度限制
                            if (data.length < 15) {
                                return '<div>' + data + '</div>';
                            } else {
                                return '<div class="tableCompany" data-toggle="tooltip" alt="' + data + '">' + data.substring(0, 14) + '...</div>';
                            }
                        }
                    },
                    {
                        data: "vehicleNumbers"
                    },
                    {
                        data: "locationTotal"
                    },
                    {
                        data: "invalidLocations"
                    },
                    {
                        data: "locationEfficiency",
                        render: function (data, type, row, meta) {
                            return (row.locationTotal == 0 || !row.locationTotal) ? "--" : publicFunction.computedNumber(data) + "%";
                        }
                    },
                    {
                        data: "vehicleUnLocation"
                    },
                    {
                        data: "unLocationRadio",
                        render: function (data, type, row, meta) {
                            return publicFunction.computedNumber(data) + "%";
                        }
                    },
                    {
                        data: "interruptVehicle"
                    },
                    {
                        data: "interruptNumber"
                    },
                    {
                        data: "offlineVehicle"
                    },
                    {
                        data: "offlineNumber"
                    }
                ],
                _columnDefs = [{
                    //第一列，用来显示序号
                    "searchable": false,
                    "orderable": false,
                    "targets": 0
                }];
            var ajaxDataParamFun = function (param) {
                param.search = vehiclePositionQuery.val(); //模糊查询
                param.groupIds = vehiclePositionIds.length ? vehiclePositionIds : '';
                param.startTime = startTime || '';
                param.endTime = endTime || '';
                param.searchType = vehiclePosition.requestFlag ? 0 : 1;
            };
            //表格setting
            var setting = {
                listUrl: vehiclePosition.tableListUrl,
                columnDefs: _columnDefs, //表格列定义
                columns: _columns, //表格列
                dataTableDiv: vehiclePosition.tableId, //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                ajaxCallBack: vehiclePosition.drawTableCallbackFun,
                drawCallbackFun: function () {
                    var api = vehicleTableObject.dataTable;
                    var startIndex = api.context[0]._iDisplayStart; //获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                    });
                }
            };
            vehicleTableObject = new TG_Tabel.createNew(setting);
            vehicleTableObject.init();
            //搜索
            vehiclePositionSearch.off().on("click", function () {
                vehicleTableObject.requestData();
            });
            // 模糊查询
            vehiclePositionQuery.keyup(function (event) {
                if (event.keyCode == 13) {
                    vehiclePositionSearch.click();
                }
            });
            $('[data-toggle=\'tooltip\']').tooltip();
            //为每行tr绑定点击事件
            vehiclePositionTable.on("click", "tbody tr", vehiclePosition.handleTbodyClick);
            //导出按钮绑定点击事件
            vehiclePositionExport.bind("click", function () {
                if (!vehiclePosition.tableList.length) {
                    layer.msg("暂未查询到可以导出的数据");
                    return false;
                }
                vehiclePosition.getCheckedNodes();
                var param = {
                    groupIds: vehiclePositionIds,
                    startTime: startTime,
                    endTime: endTime
                }
                if (getRecordsNum('vehiclePositionTable_info') > 60000) {
                    return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
                }
                exportExcelUseForm(vehiclePosition.exportUrl, param);
                // publicFunction.exportTableData(vehiclePosition.exportUrl);
            }); //
            vehiclePositionRefresh.bind("click", vehiclePosition.refreshTable); //刷新表格绑定点击事件
            vehiclePositionColumn.find('.toggle-checkbox').on('change', function (e) {
                e.preventDefault();
                var column = vehicleTableObject.dataTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                vehiclePositionColumn.find(".keep-open").addClass("open");
                $(this).parent().parent().parent().parent().addClass("open");
            });
        },
        //
        drawTableCallbackFun: function (data) {
            vehiclePosition.tableList = [];
            vehiclePositionExport.removeAttr("disabled");
            $("#batchDetail").removeAttr("disabled");
            if (data.records != null && data.records.length != 0) {
                vehiclePosition.requestFlag = false;
                vehiclePosition.tableList = data.records.length && data.records;
            } else {
                // vehiclePositionExport.attr("disabled", "disabled");
                // $("#batchDetail").attr("disabled", "disabled");
            }
        },
        //表单验证
        validates: function () {
            return $("#vehiclePositionForm").validate({
                rules: {
                    groupSelect: {
                        // required: true,
                        mustSelectGroups: true
                    }
                },
                messages: {
                    groupSelect: {
                        // required: vehicleSelectGroup,
                        mustSelectGroups: vehicleSelectGroup
                    }
                }
            }).form();
        },
        //查询点击事件
        inquireClick: function (number) {
            vehiclePositionQuery.val(""); // 删除模糊搜索关键字
            var _time = vehiclePositionTime.val();
            if (number == 0) {
                publicFunction.getTheCurrentTime();
            } else if (number == -1) {
                publicFunction.getBeforeDay(-1, _time)
            } else if (number == -3) {
                publicFunction.getBeforeDay(-3, _time)
            } else if (number == -7) {
                publicFunction.getBeforeDay(-7, _time)
            }
            if (number != 1) {
                vehiclePositionTime.val(startTime + '--' + endTime);
                startTime = startTime;
                endTime = endTime;
            } else {
                var timeInterval = vehiclePositionTime.val().split('--');
                startTime = timeInterval[0];
                endTime = timeInterval[1];
            };
            vehiclePosition.getCheckedNodes();
            var ids = vehiclePositionIds.split(",");
            ids = ids.filter(function (item) {
                return item;
            });
            if (number == "batch") {
                vehiclePosition.batchDetail();
                return false;
            }
            if (ids.length > 100) {
                layer.msg("最大支持选中100个组织，您已勾选" + ids.length + "个组织");
                return false;
            }
            if (!vehiclePosition.validates()) {
                return;
            };
            // 判断时间范围
            if (publicFunction.compareTime(startTime, endTime) > 31) {
                layer.msg('最多只能查询31天范围的数据');
                return;
            }
            vehiclePosition.requestFlag = true;
            vehiclePosition.settingTableColumn();
            vehiclePosition.initTable();
        },
        //消息列表tr点击事件
        handleTbodyClick: function (event) {
            event.stopPropagation();
            var _this = $(this),
                _index = _this.index(),
                tableList = vehiclePosition.tableList;
            if (!tableList.length || _this.find("td").length == 1) return false;
            if (activeRowIndex == _index) return false;
            if (_index !== activeRowIndex) {
                // $("body").css({"overflow": "hidden"});
                activeRowIndex = _index;
                var messageItem = tableList[_index];
                vehiclePosition.selectGroupId = messageItem.groupId;
                posDetail.tableListData = [];
                unPosDetail.tableListData = [];
                breakDetail.tableListData = [];
                offlineDetail.tableListData = [];
                unusualDetail.tableListData = [];
                posDetail.tableQuery.val("");
                unPosDetail.tableQuery.val("");
                breakDetail.tableQuery.val("");
                offlineDetail.tableQuery.val("");
                positionDetail.requestPosData('drawerCompany');
                posDetail.tableObject.dataTable.clear();
                unPosDetail.tableObject.dataTable.clear();
                breakDetail.tableObject.dataTable.clear();
                offlineDetail.tableObject.dataTable.clear();
                drawerCompany.find(".nav-tabs li:nth(0)").addClass("active").siblings("li").removeClass("active");
                drawerCompany.find(".tab-content .tab-pane:nth(0)").addClass("active").siblings(".tab-pane").removeClass("active");
                drawerBox.addClass('active');
                drawerCompany.show();
                // posDetail.tableQuery.val("");
                // unPosDetail.tableQuery.val("");
                // breakDetail.tableQuery.val("");
                // offlineDetail.tableQuery.val("");
                // drawerCompany.find(".nav-tabs li").removeAttr("class");
                // drawerCompany.find(".tab-content .tab-pane").attr("class", "tab-pane");
                // if(messageItem.locationTotal == 0 || !messageItem.locationTotal){
                //     drawerCompany.find(".nav-tabs li").eq(0).addClass("hide");
                //     drawerCompany.find(".tab-content .tab-pane").eq(0).addClass("hide");
                //     drawerCompany.find(".nav-tabs li").eq(1).addClass("active");
                //     drawerCompany.find(".tab-content .tab-pane").eq(1).addClass("active");
                // }else{
                //     drawerCompany.find(".nav-tabs li").eq(0).addClass("active");
                //     drawerCompany.find(".tab-content .tab-pane").eq(0).addClass("active");
                // }
            } else {
                companyVehicles.closeDrawer();
            }
        },
        //处理获取的查询的数据
        handleInquireData: function (data) {
            getAddressStatus = false;
            if (data.success == true) {
                dataListArray = []; //用来储存显示数据
                vehiclePositionExport.removeAttr("disabled");
                $("#batchDetail").removeAttr("disabled");
                if (data.records != null && data.records.length != 0) {
                    var exportData = data.records;
                    vehiclePosition.reloadTableData(exportData);
                } else {
                    vehiclePosition.reloadTableData(dataListArray);
                    vehiclePositionQuery.val("");
                    vehiclePositionQuery.click();
                    // vehiclePositionExport.attr("disabled", "disabled");
                    // $("#batchDetail").attr("disabled", "disabled");
                }
            } else {
                if (data.msg != null) {
                    layer.msg(data.msg, {
                        move: false
                    });
                }
            }
        },
        //更新表格数据
        reloadTableData: function (dataList) {
            var api = vehicleTableObject.dataTable;
            var currentPage = api.page();
            api.clear();
            api.rows.add(dataList);
            api.page(currentPage).draw(false);
        },
        //刷新表格
        refreshTable: function () {
            vehiclePositionQuery.val("");
            var tsval = vehiclePositionQuery.val();
            vehicleTableObject.dataTable.columns(1).search(tsval, false, false).draw();
            // vehicleTableObject.dataTable.requestData();
        },
        //批量导出明细
        batchDetail: function () {
            if (!vehiclePosition.tableList.length) {
                layer.msg("暂未查询到可以导出的数据");
                return false;
            }
            if (getRecordsNum('vehiclePositionTable_info') > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var param = {
                groupIds: vehiclePositionIds,
                startTime: startTime,
                endTime: endTime
            }
            exportExcelUseForm(vehiclePosition.exportBatchUrl, param);
        }
    }

    //车辆月度定位统计
    monthPosition = {
        requestFlag: false,
        exportUrl: "/clbs/lkyw/vehicle/positioning/statistics/exportMonthPositioningList",
        treeUrl: "/clbs/a/search/reportFuzzySearch",
        treeId: "monthPositionTree",
        tableListUrl: "/clbs/lkyw/vehicle/positioning/statistics/month/list",
        tableDetailUrl: "",
        tableEditUrl: "",
        tableDeleteUrl: "",
        tableId: "monthPositionTable",
        init: function () {
            // 初始渲染表头
            var nowT = new Date();
            var yearT = nowT.getFullYear();
            var monthT = nowT.getMonth() + 1;
            var nowMonthDays = publicFunction.getDaysInOneMonth(yearT, monthT);
            monthPosition.settingTableColumn(nowMonthDays); //设置表格列
            monthPosition.renderTableHeader($('#monthTableThead'), nowMonthDays, true, 2); //设置表格头部
            monthPosition.initTree(); //组织树
            //monthPosition.initTable(); //初始化表格
            monthPositionInput.bind("click", showMenuContent); //为组织树的输入框绑定点击事件
            //组织树输入框 模糊搜索
            monthPositionInput.on('input propertychange', function (value) {
                var treeObj = $.fn.zTree.getZTreeObj(monthPosition.treeId);
                treeObj.checkAllNodes(false);
                search_ztree(monthPosition.treeId, 'monthPositionInput', 'group');
            });
        },
        inquireClick: function (number) {
            monthPositionQuery.val(""); // 删除模糊搜索关键字
            var month, searchType = number == 1 ? 0 : 1;
            if (number == 1) {
                month = monthPositionTime.val()
            }
            if (number == 'thisMonth') {
                month = publicFunction.getCurrentMonth();
                monthPositionTime.val(month)
            }
            if (number == 'lastMonth') {
                var currentMonth = monthPositionTime.val();
                if (monthPositionTime.val() == $("#monthPositionTime option:last").val()) {
                    layer.msg('查询时间范围不超过12个月');
                    return;
                }
                var date = new Date(currentMonth);
                date.setMonth(date.getMonth() - 1);
                var str = publicFunction.formatMonth(date);
                monthPositionTime.val(str);
                month = monthPositionTime.val()
            }
            monthPosition.getCheckedNodes();
            if (!monthPosition.validates()) {
                return;
            };
            var daysCount = publicFunction.getDaysInOneMonth(month.split('-')[0], month.split('-')[1]);
            if (monthTableObject.dataTable) {
                monthTableObject.dataTable.clear();
            } else {
                monthTableObject.clear();
            }
            monthPosition.settingTableColumn(daysCount);
            monthPosition.requestFlag = true;
            monthPosition.renderTableHeader($('#monthTableThead'), daysCount, false);
            // var parameter = {"vehicleIds": monthPositionIds, "month": month};
            // json_ajax("POST", monthPosition.tableListUrl, "json", true, parameter, monthPosition.handleInquireData);
        },
        handleInquireData: function (date) {
            if (date.success == true) {
                dataListArray = []; //用来储存显示数据
                monthPositionExport.removeAttr("disabled");
                if (date.obj != null && date.obj.length != 0) {
                    var exportData = date.obj;
                    for (var i = 0; i < exportData.length; i++) {
                        var dateList = [
                            i + 1,
                            exportData[i].vehicleBrandNumber,
                            exportData[i].vehicleBrandColor,
                            exportData[i].vehicleType == 'null' ? '其他车辆' : exportData[i].vehicleType,
                            exportData[i].enterpriseName,
                        ]
                        for (var j = 0; j < exportData[i].days.length; j++) {
                            if (exportData[i].days[j] == null) {
                                exportData[i].days[j] = ''
                            }
                            dateList.push(exportData[i].days[j])
                        }
                        dateList.push(exportData[i].monthReport)
                        dataListArray.push(dateList);
                    }
                    monthPosition.reloadTableData(dataListArray);
                } else {
                    monthPosition.reloadTableData(dataListArray);
                    monthPositionQuery.val("");
                    monthPositionQuery.click();
                    // monthPositionExport.attr("disabled", "disabled");
                }
            } else {
                if (date.msg != null) {
                    layer.msg(date.msg, {
                        move: false
                    });
                }
            }
        },
        //监控对象组织树
        initTree: function () {
            var setting = {
                async: {
                    url: monthPosition.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "type": "multiple",
                        "icoType": "0"
                    },
                    dataFilter: monthPosition.ajaxTreeDataFilter
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
                    beforeClick: monthPosition.beforeClickVehicle,
                    onAsyncSuccess: monthPosition.zTreeOnAsyncSuccess,
                    beforeCheck: monthPosition.zTreeBeforeCheck,
                    onCheck: monthPosition.onCheckVehicle,
                    onNodeCreated: monthPosition.zTreeOnNodeCreated,
                    onExpand: monthPosition.zTreeOnExpand
                }
            };
            $.fn.zTree.init($("#" + monthPosition.treeId), setting, null);
        },
        //获取tree的URL
        getTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle";
            }
        },
        ajaxTreeDataFilter: function (treeId, parentNode, responseData) {
            var treeObj = $.fn.zTree.getZTreeObj(monthPosition.treeId);
            if (responseData.msg) {
                var obj = JSON.parse(ungzip(responseData.msg));
                var data;
                // obj.tree.forEach(function (item) {
                //     if(item.name == "渝A00002"){
                //         console.log(item)
                //     }
                // })
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
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(monthPosition.treeId);
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj(monthPosition.treeId);
            if (size <= 100 && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            monthPosition.getCharSelect(treeObj);
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj(monthPosition.treeId),
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
                    var zTree = $.fn.zTree.getZTreeObj(monthPosition.treeId),
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
                if (nodesLength > 5000) {
                    layer.msg(maxSelectItem);
                    // layer.msg('最多勾选100个监控对象');
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
            var zTree = $.fn.zTree.getZTreeObj(monthPosition.treeId);
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                monthIsSearch = false;
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    monthPosition.getCheckedNodes();
                    monthPosition.validates();
                }, 600);
            }
            monthPosition.getCharSelect(zTree);
            monthPosition.getCheckedNodes();
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
            var treeObj = $.fn.zTree.getZTreeObj(monthPosition.treeId);
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
                monthPositionInput.val(allNodes[0].name);
            } else {
                monthPositionInput.val("");
            }
        },
        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj(monthPosition.treeId),
                nodes = zTree
                .getCheckedNodes(true),
                v = "",
                vid = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == "vehicle") {
                    v += nodes[i].name + ",";
                    vid += nodes[i].id + ",";
                }
            }
            monthPositionNames = v;
            monthPositionIds = vid;
        },
        searchVehicleTree: function (param) {
            ifAllCheck = false; //模糊查询不自动勾选
            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                monthPosition.initTree();
            } else {
                bflag = true;
                var querySetting = {
                    async: {
                        url: monthPosition.treeUrl,
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {
                            "type": monthPositionType.val(),
                            "queryParam": param
                        },
                        dataFilter: monthPosition.ajaxQueryTreeFilter
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
                        beforeClick: monthPosition.beforeClickVehicle,
                        onCheck: monthPosition.onCheckVehicle,
                        onExpand: monthPosition.zTreeOnExpand,
                        onNodeCreated: monthPosition.zTreeOnNodeCreated
                    }
                };
                $.fn.zTree.init($("#" + monthPosition.treeId), querySetting, null);
            }
        },
        ajaxQueryTreeFilter: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData))
            var nodesArr;
            if (monthPositionType.val() == "vehicle") {
                nodesArr = filterQueryResult(responseData, crrentSubV);
            } else {
                nodesArr = responseData;
            }
            for (var i = 0; i < nodesArr.length; i++) {
                nodesArr[i].open = true;
            }
            return nodesArr;
        },
        //设置表格列
        settingTableColumn: function (n) {
            //显示隐藏列
            var column_text = "";
            var creatTh = function (index, text) {
                var checkedStr = '';
                checkedStr = 'checked="checked"'
                return "<li><label><input type=\"checkbox\"  " + checkedStr + " class=\"toggle-input toggle-vis\" data-column=\"" + index + "\" />" + text + "</label></li>"
            }
            column_text += creatTh(1, '车牌号');
            column_text += creatTh(2, '车牌颜色');
            column_text += creatTh(3, '车辆类型');
            column_text += creatTh(4, '所属企业');
            // column_text += creatTh(6, '累计定位总数');
            // column_text += creatTh(7, '累计无效数');
            // column_text += creatTh(8, '有效率');
            // for (var i = 0; i < n; i++) { //减去序号，道路运输企业，合计
            //     column_text += creatTh(i + 8, i + 1);
            // }
            monthPositionColumn.html(column_text);
        },
        //渲染表格头部
        renderTableHeader: function (obj, n, isFirst, searchType) {
            var $obj = obj;
            $obj.html('');
            var html = '',
                tr1 = '',
                tr2 = '';
            tr1 = '<th class="text-center" rowspan="2">序号</th>\n' +
                '<th class="text-center" rowspan="2">车牌号</th>\n' +
                '<th class="text-center" rowspan="2">车牌颜色</th>\n' +
                '<th class="text-center" rowspan="2">车辆类型</th>\n' +
                '<th class="text-center" rowspan="2" style="min-width: 250px;">所属企业</th>\n' +
                '<th class="text-center" colspan="3">定位统计</th>';
            tr2 = '<th class="text-center">累计定位总数</th>' +
                '<th class="text-center">累计无效数</th>' +
                '<th class="text-center">有效率</th>';
            for (var i = 0; i < n; i++) { //减去序号，道路运输企业，合计
                var thItem = '<th class="text-center">定位数</th>' +
                    '<th class="text-center">无效数</th>' +
                    '<th class="text-center">有效率</th>';
                tr1 += '<th class="text-center" colspan="3">' + (i + 1) + '</th>';
                tr2 += thItem;
            }
            html = '<tr>' + tr1 + '</tr>' + '<tr>' + tr2 + '</tr>';
            $obj.html(html);
            if (isFirst) {
                monthTableObject = publicFunction.initDataTable('#' + monthPosition.tableId);
            } else {
                publicFunction.destroyTable(monthTableObject);
                monthPosition.initTable(n, searchType);
            }
        },
        //初始化
        initTable: function (n) {
            var _columns = [{
                        data: null
                    },
                    {
                        data: "monitorName"
                    },
                    {
                        data: "plateColor"
                    },
                    {
                        data: "vehicleType"
                    },
                    {
                        data: "groupName",
                        render: function (data, type, row, meta) {
                            //250px 长度限制
                            if (data.length < 15) {
                                return '<div>' + data + '</div>';
                            } else {
                                return '<div class="tableCompany" data-toggle="tooltip" alt="' + data + '">' + data.substring(0, 14) + '...</div>';
                            }
                        }
                    },
                    {
                        data: "totalLocationNum"
                    },
                    {
                        data: "totalInvalidNum"
                    },
                    {
                        data: "totalRatio",
                        render: function (data, type, row, meta) {
                            return (row.totalLocationNum == 0 || !row.totalLocationNum) ? "--" : publicFunction.computedNumber(data) + "%";
                        }
                    },
                ],
                _columnDefs = [{
                    //第一列，用来显示序号
                    "searchable": false,
                    "orderable": false,
                    "targets": 0
                }];
            for (var i = 0; i < n; i++) {
                (function (index) {
                    var dayIndex = index + 1;
                    _columns.push({
                        data: "totalNum" + dayIndex
                    });
                    _columns.push({
                        data: "invalidNum" + dayIndex
                    });
                    _columns.push({
                        data: "ratio" + dayIndex,
                        render: function (data, type, row, meta) {
                            var totalNum = row["totalNum" + dayIndex];
                            return (totalNum == 0 || !totalNum) ? "--" : publicFunction.computedNumber(data) + "%";
                        }
                    });
                }(i))
            }
            var ajaxDataParamFun = function (param) {
                param.search = monthPositionQuery.val(); //模糊查询
                param.monitorIds = monthPositionIds.length ? monthPositionIds : '';
                param.time = monthPositionTime.val() || '';
                param.searchType = monthPosition.requestFlag ? 0 : 1;
            };
            //表格setting
            var setting = {
                listUrl: monthPosition.tableListUrl,
                columnDefs: _columnDefs, //表格列定义
                columns: _columns, //表格列
                dataTableDiv: monthPosition.tableId, //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                ajaxCallBack: monthPosition.drawTableCallbackFun,
                drawCallbackFun: function () {
                    var api = monthTableObject.dataTable;
                    var startIndex = api.context[0]._iDisplayStart; //获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                    });
                }
            };
            monthTableObject = new TG_Tabel.createNew(setting);
            monthTableObject.init();
            //搜索
            monthPositionSearch.off().on("click", function () {
                monthTableObject.requestData();
            });
            // 模糊查询
            monthPositionQuery.keyup(function (event) {
                if (event.keyCode == 13) {
                    monthPositionSearch.click();
                }
            });
            //导出按钮绑定点击事件
            monthPositionExport.bind("click", function () {
                monthPosition.getCheckedNodes();
                if (!monthPosition.tableList.length) {
                    layer.msg("暂未查询到可以导出的数据");
                    return false;
                }
                var param = {
                    monitorIds: monthPositionIds,
                    time: monthPositionTime.val()
                }
                if (getRecordsNum('monthPositionTable_info') > 60000) {
                    return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
                }
                exportExcelUseForm(monthPosition.exportUrl, param);
            });
            monthPositionRefresh.bind("click", monthPosition.refreshTable); //刷新表格绑定点击事件
        },
        drawTableCallbackFun: function (data) {
            monthPosition.tableList = [];
            monthPositionExport.removeAttr("disabled");
            if (data.records != null && data.records.length != 0) {
                data.records.forEach(function (record) {
                    var monthDetailInfoList = record.monthDetailInfoList;
                    monthDetailInfoList.forEach(function (item, index) {
                        record['totalNum' + item.index] = item.totalNum;
                        record['invalidNum' + item.index] = item.invalidNum;
                        record['ratio' + item.index] = item.ratio;
                    })
                })
                monthPosition.requestFlag = false;
                monthPosition.tableList = data.records.length && data.records;
            } else {
                // monthPositionExport.attr("disabled", "disabled");
            }
        },
        //更新表格数据
        reloadTableData: function (dataList) {
            var currentPage = monthTableObject.page();
            monthTableObject.clear();
            monthTableObject.rows.add(dataList);
            monthTableObject.page(currentPage).draw(false);
        },
        //刷新
        refreshTable: function () {
            monthPositionQuery.val("");
            var query = monthPositionQuery.val();
            monthTableObject.dataTable.column(1).search(query, false, false).draw();
        },
        validates: function () {
            return $("#monthPositionForm").validate({
                rules: {
                    groupSelect: {
                        zTreeChecked: monthPosition.treeId
                    }
                },
                messages: {
                    groupSelect: {
                        zTreeChecked: vehicleSelectBrand
                    }
                }
            }).form();
        },
    }

    //异常定位统计
    unusualPosition = {
        requestFlag: false,
        selectMonitorId: '',
        exportUrl: "/clbs/lkyw/vehicle/positioning/statistics/exportExceptionList",
        treeUrl: "/clbs/a/search/reportFuzzySearch",
        treeId: "unusualPositionTree",
        tableListUrl: "/clbs/lkyw/vehicle/positioning/statistics/exception/list",
        tableDetailUrl: "",
        tableEditUrl: "",
        tableDeleteUrl: "",
        tableId: "unusualPositionTable",
        tableList: [],
        init: function () {
            // 初始渲染表头
            var nowT = new Date();
            var yearT = nowT.getFullYear();
            var monthT = nowT.getMonth() + 1;
            var nowMonthDays = publicFunction.getDaysInOneMonth(yearT, monthT);
            unusualPosition.settingTableColumn(nowMonthDays); //设置表格列
            unusualPosition.initTree(); //组织树
            //unusualPosition.initTable(); //初始化表格
            unusualTableObject = publicFunction.initDataTable('#' + unusualPosition.tableId);
            unusualPositionInput.bind("click", showMenuContent); //为组织树的输入框绑定点击事件
            unusualPositionRefresh.bind("click", unusualPosition.refreshTable); //刷新表格绑定点击事件
            //组织树输入框 模糊搜索
            unusualPositionInput.on('input propertychange', function (value) {
                var treeObj = $.fn.zTree.getZTreeObj(unusualPosition.treeId);
                treeObj.checkAllNodes(false);
                search_ztree(unusualPosition.treeId, 'unusualPositionInput', 'group');
            });
        },
        inquireClick: function (number) {
            unusualPositionQuery.val(""); // 删除模糊搜索关键字
            var month, searchType = number == 1 ? 0 : 1;
            if (number == 1) {
                month = unusualPositionTime.val()
            }
            if (number == 'thisMonth') {
                month = publicFunction.getCurrentMonth();
                unusualPositionTime.val(month)
            }
            if (number == 'lastMonth') {
                var currentMonth = unusualPositionTime.val();
                if (unusualPositionTime.val() == $("#unusualPositionTime option:last").val()) {
                    layer.msg('查询时间范围不超过12个月');
                    return;
                }
                var date = new Date(currentMonth);
                date.setMonth(date.getMonth() - 1);
                var str = publicFunction.formatMonth(date);
                unusualPositionTime.val(str);
                month = unusualPositionTime.val()
            }
            unusualPosition.getCheckedNodes();
            if (!unusualPosition.validates()) {
                return;
            };
            unusualPosition.requestFlag = true;
            unusualPosition.settingTableColumn();
            unusualPosition.initTable(searchType);
        },
        handleInquireData: function (data) {
            if (data.success == true) {
                dataListArray = []; //用来储存显示数据
                monthPositionExport.removeAttr("disabled");
                if (data.obj != null && data.obj.length != 0) {
                    var exportData = data.obj;
                    for (var i = 0; i < exportData.length; i++) {
                        var dateList = [
                            i + 1,
                            exportData[i].vehicleBrandNumber,
                            exportData[i].vehicleBrandColor,
                            exportData[i].vehicleType == 'null' ? '其他车辆' : exportData[i].vehicleType,
                            exportData[i].enterpriseName,
                            exportData[i].timeSection,
                            exportData[i].timeSectionNumber
                        ];
                        dataListArray.push(dateList);
                    }
                    unusualPosition.reloadTableData(dataListArray);
                } else {
                    unusualPosition.reloadTableData(dataListArray);
                    unusualPositionQuery.val("");
                    unusualPositionQuery.click();
                    // unusualPositionExport.attr("disabled", "disabled");
                }
            } else {
                if (data.msg != null) {
                    layer.msg(data.msg, {
                        move: false
                    });
                }
            }
        },
        //监控对象组织树
        initTree: function () {
            var setting = {
                async: {
                    url: unusualPosition.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "type": "multiple",
                        "icoType": "0"
                    },
                    dataFilter: unusualPosition.ajaxTreeDataFilter
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
                    beforeClick: unusualPosition.beforeClickVehicle,
                    onAsyncSuccess: unusualPosition.zTreeOnAsyncSuccess,
                    beforeCheck: unusualPosition.zTreeBeforeCheck,
                    onCheck: unusualPosition.onCheckVehicle,
                    onNodeCreated: unusualPosition.zTreeOnNodeCreated,
                    onExpand: unusualPosition.zTreeOnExpand
                }
            };
            $.fn.zTree.init($("#" + unusualPosition.treeId), setting, null);
        },
        //获取tree的URL
        getTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle";
            }
        },
        ajaxTreeDataFilter: function (treeId, parentNode, responseData) {
            var treeObj = $.fn.zTree.getZTreeObj(unusualPosition.treeId);
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
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(unusualPosition.treeId);
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj(unusualPosition.treeId);
            if (size <= 100 && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            unusualPosition.getCharSelect(treeObj);
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj(unusualPosition.treeId),
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
                    var zTree = $.fn.zTree.getZTreeObj(unusualPosition.treeId),
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
                if (nodesLength > 5000) {
                    layer.msg(maxSelectItem);
                    // layer.msg('最多勾选100个监控对象');
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
            var zTree = $.fn.zTree.getZTreeObj(unusualPosition.treeId);
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                unusualIsSearch = false;
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    unusualPosition.getCheckedNodes();
                    unusualPosition.validates();
                }, 600);
            }
            unusualPosition.getCharSelect(zTree);
            unusualPosition.getCheckedNodes();
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
            var treeObj = $.fn.zTree.getZTreeObj(unusualPosition.treeId);
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
                unusualPositionInput.val(allNodes[0].name);
            } else {
                unusualPositionInput.val("");
            }
        },
        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj(unusualPosition.treeId),
                nodes = zTree
                .getCheckedNodes(true),
                v = "",
                vid = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == "vehicle") {
                    v += nodes[i].name + ",";
                    vid += nodes[i].id + ",";
                }
            }
            unusualPositionNames = v;
            unusualPositionIds = vid;
        },
        searchVehicleTree: function (param) {
            ifAllCheck = false; //模糊查询不自动勾选
            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                unusualPosition.initTree();
            } else {
                bflag = true;
                var querySetting = {
                    async: {
                        url: unusualPosition.treeUrl,
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {
                            "type": unusualPositionType.val(),
                            "queryParam": param
                        },
                        dataFilter: unusualPosition.ajaxQueryTreeFilter
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
                        beforeClick: unusualPosition.beforeClickVehicle,
                        onCheck: unusualPosition.onCheckVehicle,
                        onExpand: unusualPosition.zTreeOnExpand,
                        onNodeCreated: unusualPosition.zTreeOnNodeCreated
                    }
                };
                $.fn.zTree.init($("#" + unusualPosition.treeId), querySetting, null);
            }
        },
        ajaxQueryTreeFilter: function (treeId, parentNode, responseData) {
            // responseData = JSON.parse(ungzip(responseData))
            responseData = JSON.parse(ungzip(responseData))
            var nodesArr;
            if (monthPositionType.val() == "vehicle") {
                nodesArr = filterQueryResult(responseData, crrentSubV);
            } else {
                nodesArr = responseData;
            }
            for (var i = 0; i < nodesArr.length; i++) {
                nodesArr[i].open = true;
            }
            return nodesArr;
        },
        //设置表格列
        settingTableColumn: function (n) {
            //显示隐藏列
            var column_text = "";
            var columns = [{
                    name: "车牌号",
                    columnIndex: 1
                },
                {
                    name: "车牌颜色",
                    columnIndex: 2
                },
                {
                    name: "车辆类型",
                    columnIndex: 3
                },
                {
                    name: "所属企业",
                    columnIndex: 4
                },
                {
                    name: "不定位天数",
                    columnIndex: 5
                },
                {
                    name: "有效率",
                    columnIndex: 6
                },
                {
                    name: "无效天数",
                    columnIndex: 7
                },
                {
                    name: "无效定位数",
                    columnIndex: 8
                },
                {
                    name: "有效率",
                    columnIndex: 9
                },
            ]
            var creatTh = function (index, text) {
                var checkedStr = '';
                checkedStr = 'checked="checked"';
                // index = index + 1;
                return "<li><label><input type=\"checkbox\"  " + checkedStr + " class=\"toggle-input toggle-checkbox\" data-column=\"" + index + "\" />" + text + "</label></li>"
            }
            columns.forEach(function (item) {
                column_text += creatTh(item.columnIndex, item.name);
            })
            // column_text += creatTh(1, '车牌号');
            // column_text += creatTh(2, '车牌颜色');
            // column_text += creatTh(3, '车辆类型');
            // column_text += creatTh(4, '所属企业');
            // column_text += creatTh(6, '累计定位总数');
            // column_text += creatTh(7, '累计无效数');
            // column_text += creatTh(8, '有效率');
            // for (var i = 0; i < n; i++) { //减去序号，道路运输企业，合计
            //     column_text += creatTh(i + 8, i + 1);
            // }
            unusualPositionColumn.html(column_text);
        },
        //初始化
        initTable: function () {
            var _columns = [{
                        data: null
                    },
                    {
                        data: "monitorName"
                    },
                    {
                        data: "plateColor"
                    },
                    {
                        data: "vehicleType"
                    },
                    {
                        data: "groupName",
                        render: function (data, type, row, meta) {
                            //250px 长度限制
                            if (data.length < 15) {
                                return '<div>' + data + '</div>';
                            } else {
                                return '<div class="tableCompany" data-toggle="tooltip" alt="' + data + '">' + data.substring(0, 14) + '...</div>';
                            }
                        }
                    },
                    {
                        data: "noLocationDayNum"
                    },
                    {
                        data: "locationRatio",
                        render: function (data, type, row, meta) {
                            return publicFunction.computedNumber(data) + "%";
                        }
                    },
                    {
                        data: "invalidDayNum"
                    },
                    {
                        data: "invalidLocationNum"
                    },
                    {
                        data: "ration",
                        render: function (data, type, row, meta) {
                            return publicFunction.computedNumber(data) + "%";
                        }
                    }
                ],
                _columnDefs = [{
                    //第一列，用来显示序号
                    "searchable": false,
                    "orderable": false,
                    "targets": 0
                }];
            var ajaxDataParamFun = function (param) {
                param.search = unusualPositionQuery.val(); //模糊查询
                param.monitorIds = unusualPositionIds.length ? unusualPositionIds : '';
                param.time = unusualPositionTime.val() || '';
                param.searchType = unusualPosition.requestFlag ? 0 : 1;
                param.locationNumThreshold = $("#unPositionNumber").val();
                param.invalidNumThreshold = $("#invalidPositionNumber").val();
            };
            //表格setting
            var setting = {
                listUrl: unusualPosition.tableListUrl,
                columnDefs: _columnDefs, //表格列定义
                columns: _columns, //表格列
                dataTableDiv: unusualPosition.tableId, //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                ajaxCallBack: unusualPosition.drawTableCallbackFun,
                drawCallbackFun: function () {
                    var api = unusualTableObject.dataTable;
                    var startIndex = api.context[0]._iDisplayStart; //获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                    });
                }
            };
            unusualTableObject = new TG_Tabel.createNew(setting);
            unusualTableObject.init();
            //搜索
            unusualPositionSearch.off().on("click", function () {
                unusualTableObject.requestData();
            });
            // 模糊查询
            unusualPositionQuery.keyup(function (event) {
                console.log(event.keyCode)
                if (event.keyCode == 13) {
                    unusualPositionSearch.click();
                }
            });
            //为每行tr绑定点击事件
            unusualPositionTable.on("click", "tbody tr", unusualPosition.handleTbodyClick);
            //导出按钮绑定点击事件
            unusualPositionExport.bind("click", function () {
                if (!unusualPosition.tableList.length) {
                    layer.msg("暂未查询到可以导出的数据");
                    return false;
                }
                unusualPosition.getCheckedNodes();
                var param = {
                    monitorIds: unusualPositionIds,
                    time: unusualPositionTime.val(),
                    locationNumThreshold: $("#unPositionNumber").val(),
                    invalidNumThreshold: $("#invalidPositionNumber").val()
                }
                if (getRecordsNum('unusualPositionTable_info') > 60000) {
                    return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
                }
                exportExcelUseForm(unusualPosition.exportUrl, param);
            });
            unusualPositionRefresh.bind("click", unusualPosition.refreshTable); //刷新表格绑定点击事件
            unusualPositionColumn.find('.toggle-checkbox').on('change', function (e) {
                e.preventDefault();
                var column = unusualTableObject.dataTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                unusualPositionColumn.find(".keep-open").addClass("open");
                $(this).parent().parent().parent().parent().addClass("open");
            });
        },
        drawTableCallbackFun: function (data) {
            unusualPosition.tableList = [];
            unusualPositionExport.removeAttr("disabled");
            if (data.records != null && data.records.length != 0) {
                unusualPosition.requestFlag = false;
                unusualPosition.tableList = data.records.length && data.records;
            } else {
                // unusualPositionExport.attr("disabled", "disabled");
            }
        },
        //更新表格数据
        reloadTableData: function (dataList) {
            var currentPage = unusualTableObject.page();
            unusualTableObject.clear();
            unusualTableObject.rows.add(dataList);
            unusualTableObject.page(currentPage).draw(false);
        },
        //刷新
        refreshTable: function () {
            unusualPositionQuery.val("");
            var query = unusualPositionQuery.val();
            unusualTableObject.dataTable.column(1).search(query, false, false).draw();
        },
        validates: function () {
            return $("#unusualPositionForm").validate({
                rules: {
                    unPositionNumber: {
                        required: true,
                        digits: true,
                        min: 1,
                        max: 99999
                    },
                    invalidPositionNumber: {
                        required: true,
                        digits: true,
                        min: 1,
                        max: 99999
                    },
                    groupSelect: {
                        zTreeChecked: unusualPosition.treeId
                    }
                },
                messages: {
                    unPositionNumber: {
                        required: "请输入大于0且小于99999的正整数",
                        digits: "请输入正整数",
                        min: "请输入大于0的数",
                        max: "请输入小于99999的数"
                    },
                    invalidPositionNumber: {
                        required: "请输入大于0且小于99999的正整数",
                        digits: "请输入正整数",
                        min: "请输入大于0的数",
                        max: "请输入小于99999的数"
                    },
                    groupSelect: {
                        zTreeChecked: vehicleSelectBrand
                    }
                }
            }).form();
        },
        handleTbodyClick: function (event) {
            event.stopPropagation();
            var _this = $(this),
                _index = _this.index(),
                tableList = unusualPosition.tableList;
            if (!tableList.length || _this.find("td").length == 1) return false;
            if (activeRowIndex == _index) return false;
            if (_index !== activeRowIndex) {
                drawerBox.addClass('active');
                drawerUnusual.show();
                // $("body").css({"overflow": "hidden"});
                activeRowIndex = _index;
                var messageItem = tableList[_index];
                unusualPosition.selectMonitorId = messageItem.monitorId;
                positionDetail.requestPosData('drawerUnusual');
            } else {
                companyVehicles.closeDrawer();
            }
        }
    }

    var posDetail = {
            requestFlag: false,
            tableObject: null,
            tableListData: [],
            tableListUrl: "/clbs/lkyw/vehicle/positioning/statistics/group/locationInfo",
            exportTableUrl: "/clbs/lkyw/vehicle/positioning/statistics/exportLocationPositioning",
            tableId: "posDetailTable",
            tableSearch: $("#posDetailSearch"),
            tableQuery: $("#posDetailQuery")
        },
        unPosDetail = {
            requestFlag: false,
            tableObject: null,
            tableListData: [],
            tableListUrl: "/clbs/lkyw/vehicle/positioning/statistics/group/unLocationInfo",
            exportTableUrl: "/clbs/lkyw/vehicle/positioning/statistics/exportUnLocationPositioning",
            tableId: "unPosDetailTable",
            tableSearch: $("#unPosDetailSearch"),
            tableQuery: $("#unPosDetailQuery")
        },
        breakDetail = {
            requestFlag: false,
            tableObject: null,
            coordinates: [],
            addressMsg: [],
            tableListData: [],
            startCoordinate: [],
            startAddressMsg: [],
            endCoordinate: [],
            endAddressMsg: [],
            tableListUrl: "/clbs/lkyw/vehicle/positioning/statistics/group/interruptInfo",
            exportTableUrl: "/clbs/lkyw/vehicle/positioning/statistics/exportInterruptInfo",
            tableId: "breakDetailTable",
            tableSearch: $("#breakDetailSearch"),
            tableQuery: $("#breakDetailQuery")
        },
        offlineDetail = {
            requestFlag: false,
            tableObject: null,
            tableListData: [],
            tableListUrl: "/clbs/lkyw/vehicle/positioning/statistics/group/offlineInfo",
            exportTableUrl: "/clbs/lkyw/vehicle/positioning/statistics/exportOfflineInfo",
            tableId: "offlineDetailTable",
            tableSearch: $("#offlineDetailSearch"),
            tableQuery: $("#offlineDetailQuery")
        },
        unusualDetail = {
            requestFlag: false,
            tableObject: null,
            tableListData: [],
            tableListUrl: "/clbs/lkyw/vehicle/positioning/statistics/exception/info",
            exportTableUrl: "/clbs/lkyw/vehicle/positioning/statistics/exportExceptionInfo",
            tableId: "unusualDetailTable"
        };
    //企业车辆定位统计明细
    var positionDetail = {
        drawerContainer: "#companyDetail1",
        drawerHeight: '',
        initExportBtnStatus: function (id) {
            var state1 = id == '#companyDetail1' && posDetail.tableListData.length,
                state2 = id == '#companyDetail2' && unPosDetail.tableListData.length,
                state3 = id == '#companyDetail3' && breakDetail.tableListData.length,
                state4 = id == '#companyDetail4' && offlineDetail.tableListData.length,
                state5 = id == '#unusualDetail' && unusualDetail.tableListData.length;
            if (state1 || state2 || state3 || state4 || state5) {
                detailExport.removeAttr("disabled");
            } else {
                detailExport.attr("disabled", "disabled");
            }
            positionDetail.drawerContainer = id;
        },
        init: function () {
            var vh = $(window).height();
            positionDetail.drawerHeight = vh - 260;
            drawerCompany.find(".detailTableView").css({
                height: vh - 260 + "px"
            });
            //企业车辆定位统计明细
            posDetail.tableObject = publicFunction.initDataTable('#' + posDetail.tableId);
            unPosDetail.tableObject = publicFunction.initDataTable('#' + unPosDetail.tableId);
            breakDetail.tableObject = publicFunction.initDataTable('#' + breakDetail.tableId);
            offlineDetail.tableObject = publicFunction.initDataTable('#' + offlineDetail.tableId);
            unusualDetail.tableObject = publicFunction.initDataTable('#' + unusualDetail.tableId);
            //弹出框 tab点击事件
            drawerCompany.on('click', '.nav-tabs a', function (e) {
                e.preventDefault();
                $(this).tab('show');
                positionDetail.initExportBtnStatus($(this).attr("href"));
            })
            drawerBox.on('click', '#detailExportBtn', positionDetail.tableDataExport);
        },
        tableDataExport: function () {
            var url = "",
                param = {};
            switch (positionDetail.drawerContainer) {
                case '#companyDetail1':
                    url = posDetail.exportTableUrl;
                    break;
                case '#companyDetail2':
                    url = unPosDetail.exportTableUrl;
                    break;
                case '#companyDetail3':
                    url = breakDetail.exportTableUrl;
                    break;
                case '#companyDetail4':
                    url = offlineDetail.exportTableUrl;
                    break;
                case '#unusualDetail':
                    url = unusualDetail.exportTableUrl;
                    break;
            }
            if (positionDetail.drawerContainer == "#unusualDetail") {
                param = {
                    monitorId: unusualPosition.selectMonitorId,
                    time: unusualPositionTime.val()
                }
            } else {
                param = {
                    groupIds: vehiclePosition.selectGroupId,
                    startTime: startTime,
                    endTime: endTime
                }
            }
            exportExcelUseForm(url, param);
        },
        requestPosData: function (container) {
            /*posDetail.tableObject.requestData();
            unPosDetail.tableObject.requestData();
            breakDetail.tableObject.requestData();
            offlineDetail.tableObject.requestData();*/
            if (container == 'drawerCompany') {
                posDetail.requestFlag = true;
                unPosDetail.requestFlag = true;
                breakDetail.requestFlag = true;
                offlineDetail.requestFlag = true;
                positionDetail.initPosDetail();
                positionDetail.initUnPosDetail();
                positionDetail.initBreakDetail();
                positionDetail.initOfflineDetail();
            } else {
                unusualDetail.requestFlag = true;
                positionDetail.initUnusualDetail();
            }
        },
        initPosDetail: function () {
            publicFunction.destroyTable(posDetail.tableObject);
            var _columns = [{
                        data: null
                    },
                    {
                        data: "monitorName"
                    },
                    {
                        data: "plateColor"
                    },
                    {
                        data: "vehicleType"
                    },
                    {
                        data: "groupName"
                    },
                    {
                        data: "locationTotal"
                    },
                    {
                        data: "invalidLocations"
                    },
                    {
                        data: "locationEfficiency",
                        render: function (data, type, row, meta) {
                            return (row.locationTotal && row.locationTotal != 0) ? publicFunction.computedNumber(data) + "%" : "--";
                        }
                    }
                ],
                _columnDefs = [{
                    //第一列，用来显示序号
                    "searchable": false,
                    "orderable": false,
                    "targets": 0
                }];
            var ajaxDataParamFun = function (param) {
                param.search = posDetail.tableQuery.val(); //模糊查询
                param.groupIds = vehiclePosition.selectGroupId;
                param.startTime = startTime;
                param.endTime = endTime;
                param.searchType = posDetail.requestFlag ? 0 : 1;
            };
            var ajaxDataCallBack = function (data) {
                posDetail.tableListData = [];
                if (data.records != null && data.records.length != 0) {
                    posDetail.tableListData = data.records;
                    posDetail.requestFlag = false;
                }
                if (drawerCompany.find(".nav-tabs li").eq(0).hasClass("active")) {
                    if (posDetail.tableListData.length) {
                        detailExport.removeAttr("disabled");
                    } else {
                        detailExport.attr("disabled", "disabled");
                    }
                    positionDetail.drawerContainer = '#companyDetail1';
                }
            }
            //表格setting
            var setting = {
                listUrl: posDetail.tableListUrl,
                columnDefs: _columnDefs, //表格列定义
                columns: _columns, //表格列
                dataTableDiv: posDetail.tableId, //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                ajaxCallBack: ajaxDataCallBack,
                drawCallbackFun: function () {
                    var api = posDetail.tableObject.dataTable;
                    var startIndex = api.context[0]._iDisplayStart; //获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                    });
                }
            };
            posDetail.tableObject = new TG_Tabel.createNew(setting);
            posDetail.tableObject.init();
            //搜索
            posDetail.tableSearch.off().on("click", function () {
                posDetail.tableObject.requestData();
            });
            // 模糊查询
            posDetail.tableQuery.keyup(function (event) {
                if (event.keyCode == 13) {
                    posDetail.tableSearch.click();
                }
            });
        },
        initUnPosDetail: function () {
            publicFunction.destroyTable(unPosDetail.tableObject);
            var _columns = [{
                        data: null
                    },
                    {
                        data: "monitorName"
                    },
                    {
                        data: "plateColor"
                    },
                    {
                        data: "vehicleType"
                    },
                    {
                        data: "groupName"
                    },
                    {
                        data: "locationDateStr"
                    },
                    {
                        data: "address"
                    }
                ],
                _columnDefs = [{
                    //第一列，用来显示序号
                    "searchable": false,
                    "orderable": false,
                    "targets": 0
                }];
            var ajaxDataParamFun = function (param) {
                param.search = unPosDetail.tableQuery.val(); //模糊查询
                param.groupIds = vehiclePosition.selectGroupId;
                param.startTime = startTime;
                param.endTime = endTime;
                param.searchType = unPosDetail.requestFlag ? 0 : 1;
            };
            var ajaxDataCallBack = function (data) {
                unPosDetail.tableListData = [];
                if (data.records != null && data.records.length != 0) {
                    unPosDetail.tableListData = data.records;
                    unPosDetail.requestFlag = false;
                }
                if (drawerCompany.find(".nav-tabs li").eq(1).hasClass("active")) {
                    if (unPosDetail.tableListData.length) {
                        detailExport.removeAttr("disabled");
                    } else {
                        detailExport.attr("disabled", "disabled");
                    }
                    positionDetail.drawerContainer = '#companyDetail2';
                }
            }
            //表格setting
            var setting = {
                listUrl: unPosDetail.tableListUrl,
                columnDefs: _columnDefs, //表格列定义
                columns: _columns, //表格列
                dataTableDiv: unPosDetail.tableId, //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                ajaxCallBack: ajaxDataCallBack,
                drawCallbackFun: function () {
                    var api = unPosDetail.tableObject.dataTable;
                    var startIndex = api.context[0]._iDisplayStart; //获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                    });
                }
            };
            unPosDetail.tableObject = new TG_Tabel.createNew(setting);
            unPosDetail.tableObject.init();
            //搜索
            unPosDetail.tableSearch.off().on("click", function () {
                unPosDetail.tableObject.requestData();
            });
            // 模糊查询
            unPosDetail.tableQuery.keyup(function (event) {
                if (event.keyCode == 13) {
                    unPosDetail.tableSearch.click();
                }
            });
        },
        initBreakDetail: function () {
            publicFunction.destroyTable(breakDetail.tableObject);
            var _columns = [{
                        data: null
                    },
                    {
                        data: "monitorName"
                    },
                    {
                        data: "plateColor"
                    },
                    {
                        data: "vehicleType"
                    },
                    {
                        data: "groupName"
                    },
                    {
                        data: "durationStr"
                    },
                    {
                        data: "startTimeStr"
                    },
                    {
                        data: "endTimeStr"
                    },
                    {
                        data: "startLocation"
                    },
                    {
                        data: "endLocation"
                    }
                    // { data: "startAddress" },
                    // { data: "endAddress" }
                ],
                _columnDefs = [{
                    //第一列，用来显示序号
                    "searchable": false,
                    "orderable": false,
                    "targets": 0
                }];
            var ajaxDataParamFun = function (param) {
                param.search = breakDetail.tableQuery.val(); //模糊查询
                param.groupIds = vehiclePosition.selectGroupId;
                param.startTime = startTime;
                param.endTime = endTime;
                param.searchType = breakDetail.requestFlag ? 0 : 1;
            };
            var ajaxDataCallBack = function (data) {
                breakDetail.startCoordinate = [];
                breakDetail.endCoordinate = [];
                breakDetail.coordinates = [];
                breakDetail.tableListData = [];
                if (data.records != null || data.records.length != 0) {
                    data.records.forEach(function (item) {
                        item.durationStr = item.detailInfo.durationStr;
                        item.startTimeStr = item.detailInfo.startTimeStr;
                        item.endTimeStr = item.detailInfo.endTimeStr;
                        item.startAddress = item.detailInfo.startAddress;
                        item.endAddress = item.detailInfo.endAddress;
                        item.startLocation = "加载中...";
                        item.endLocation = "加载中...";
                        breakDetail.startCoordinate.push(item.detailInfo.startLocation);
                        breakDetail.endCoordinate.push(item.detailInfo.endLocation);
                        breakDetail.coordinates.push({
                            startLocation: item.detailInfo.startLocation || "0,0",
                            endLocation: item.detailInfo.endLocation || "0,0"
                        })
                    })
                    breakDetail.tableListData = data.records;
                    breakDetail.requestFlag = false;
                }
                if (drawerCompany.find(".nav-tabs li").eq(2).hasClass("active")) {
                    if (breakDetail.tableListData.length) {
                        detailExport.removeAttr("disabled");
                    } else {
                        detailExport.attr("disabled", "disabled");
                    }
                    positionDetail.drawerContainer = '#companyDetail3';
                }
            }
            //表格setting
            var setting = {
                listUrl: breakDetail.tableListUrl,
                columnDefs: _columnDefs, //表格列定义
                columns: _columns, //表格列
                dataTableDiv: breakDetail.tableId, //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                ajaxCallBack: ajaxDataCallBack,
                drawCallbackFun: function () {
                    var api = breakDetail.tableObject.dataTable;
                    var startIndex = api.context[0]._iDisplayStart; //获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                    });

                    var newStartLoc = breakDetail.startCoordinate,
                        newEndLoc = breakDetail.endCoordinate,
                        newLoc = breakDetail.coordinates;
                    var aiDisplay = api.context[0].aiDisplay;
                    if (aiDisplay.length != 0 && aiDisplay.length < newLoc.length) {
                        newStartLoc = [], newEndLoc = [], newLoc = [];
                        for (var i = 0; i < aiDisplay.length; i++) {
                            newStartLoc.push(breakDetail.startCoordinate[aiDisplay[i]]);
                            newEndLoc.push(breakDetail.endCoordinate[aiDisplay[i]]);
                            newLoc.push(breakDetail.coordinates[aiDisplay[i]]);
                        }
                    }
                    var $dataTableTbody = $("#breakDetailTable tbody");
                    var startLocation = $dataTableTbody.children("tr:last-child").children("td:nth-child(9)").text();
                    var endLocation = $dataTableTbody.children("tr:last-child").children("td:nth-child(10)").text();
                    //报警位置进行逆地址解析
                    if ((startLocation == "加载中..." || endLocation == "加载中...") && !getAddressStatus) {
                        companyVehicles.getAddress(newLoc);
                    }
                },
            };
            breakDetail.tableObject = new TG_Tabel.createNew(setting);
            breakDetail.tableObject.init();
            //搜索
            breakDetail.tableSearch.off().on("click", function () {
                breakDetail.tableObject.requestData();
            });
            // 模糊查询
            breakDetail.tableQuery.keyup(function (event) {
                if (event.keyCode == 13) {
                    breakDetail.tableSearch.click();
                }
            });
        },
        initOfflineDetail: function () {
            publicFunction.destroyTable(offlineDetail.tableObject);
            var _columns = [{
                        data: null
                    },
                    {
                        data: "monitorName"
                    },
                    {
                        data: "plateColor"
                    },
                    {
                        data: "vehicleType"
                    },
                    {
                        data: "groupName"
                    },
                    {
                        data: "offLineStartTimeStr"
                    },
                    {
                        data: "offLineEndTimeStr"
                    },
                    {
                        data: "mileage"
                    }
                ],
                _columnDefs = [{
                    //第一列，用来显示序号
                    "searchable": false,
                    "orderable": false,
                    "targets": 0
                }];
            var ajaxDataParamFun = function (param) {
                param.search = offlineDetail.tableQuery.val(); //模糊查询
                param.groupIds = vehiclePosition.selectGroupId;
                param.startTime = startTime;
                param.endTime = endTime;
                param.searchType = offlineDetail.requestFlag ? 0 : 1;
            };
            var ajaxDataCallBack = function (data) {
                offlineDetail.tableListData = [];
                if (data.records != null && data.records.length != 0) {
                    data.records.forEach(function (item) {
                        item.offLineStartTimeStr = item.detailInfo.offLineStartTimeStr;
                        item.offLineEndTimeStr = item.detailInfo.offLineEndTimeStr;
                        item.mileage = item.detailInfo.displaceMile;
                    })
                    offlineDetail.tableListData = data.records;
                    offlineDetail.requestFlag = false;
                }
                if (drawerCompany.find(".nav-tabs li").eq(3).hasClass("active")) {
                    if (offlineDetail.tableListData.length) {
                        detailExport.removeAttr("disabled");
                    } else {
                        detailExport.attr("disabled", "disabled");
                    }
                    positionDetail.drawerContainer = '#companyDetail4';
                }
            }
            //表格setting
            var setting = {
                listUrl: offlineDetail.tableListUrl,
                columnDefs: _columnDefs, //表格列定义
                columns: _columns, //表格列
                dataTableDiv: offlineDetail.tableId, //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                ajaxCallBack: ajaxDataCallBack,
                drawCallbackFun: function () {
                    var api = offlineDetail.tableObject.dataTable;
                    var startIndex = api.context[0]._iDisplayStart; //获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                    });
                },
            };
            offlineDetail.tableObject = new TG_Tabel.createNew(setting);
            offlineDetail.tableObject.init();
            //搜索
            offlineDetail.tableSearch.off().on("click", function () {
                offlineDetail.tableObject.requestData();
            });
            // 模糊查询
            offlineDetail.tableQuery.keyup(function (event) {
                if (event.keyCode == 13) {
                    offlineDetail.tableSearch.click();
                }
            });
        },
        initUnusualDetail: function () {
            //positionDetail.drawerContainer = '#drawerUnusual';
            publicFunction.destroyTable(unusualDetail.tableObject);
            var _columns = [{
                        data: null
                    },
                    {
                        data: "monitorName"
                    },
                    {
                        data: "plateColor"
                    },
                    {
                        data: "vehicleType"
                    },
                    {
                        data: "groupName"
                    },
                    {
                        data: "locationNum"
                    },
                    {
                        data: "invalidNum"
                    },
                    {
                        data: "timeStr"
                    }
                ],
                _columnDefs = [{
                    //第一列，用来显示序号
                    "searchable": false,
                    "orderable": false,
                    "targets": 0
                }];
            var ajaxDataParamFun = function (param) {
                param.monitorId = unusualPosition.selectMonitorId;
                param.time = unusualPositionTime.val();
                param.searchType = unusualDetail.requestFlag ? 0 : 1;
            };
            var ajaxDataCallBack = function (data) {
                unusualDetail.tableListData = [];
                if (data.records != null && data.records.length != 0) {
                    unusualDetail.tableListData = data.records;
                    unusualDetail.requestFlag = false;
                }
                if (unusualDetail.tableListData.length) {
                    detailExport.removeAttr("disabled");
                } else {
                    detailExport.attr("disabled", "disabled");
                }
                positionDetail.drawerContainer = '#unusualDetail';
                // positionDetail.initExportBtnStatus("#unusualDetail");
            }
            //表格setting
            var setting = {
                listUrl: unusualDetail.tableListUrl,
                columnDefs: _columnDefs, //表格列定义
                columns: _columns, //表格列
                dataTableDiv: unusualDetail.tableId, //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                ajaxCallBack: ajaxDataCallBack,
                drawCallbackFun: function () {
                    var api = unusualDetail.tableObject.dataTable;
                    var startIndex = api.context[0]._iDisplayStart; //获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                    });
                },
            };
            unusualDetail.tableObject = new TG_Tabel.createNew(setting);
            unusualDetail.tableObject.init();
        },
    }

    second2Hour = function (second, hms) {
        if (second === null || second === undefined || isNaN(second)) {
            return second;
        }
        return parseInt(second * 10) / 10;
    };
    $(function () {

        vehiclePosition.init();

        monthPosition.init();

        unusualPosition.init();

        //获取时间并设置开始时间
        publicFunction.getTheCurrentTime();

        $('input').inputClear();

        //初始化时间选择下拉框
        publicFunction.renderSelect('#monthPositionTime'); //车辆月度定位统计
        publicFunction.renderSelect('#unusualPositionTime'); //异常定位统计的

        //监听组织树输入框groupInput
        $("input[name=groupSelect]").inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'vehiclePositionInput') { //企业车辆定位统计
                var treeObj = $.fn.zTree.getZTreeObj("vehiclePositionTree");
                search_ztree('vehiclePositionTree', 'vehiclePositionInput', 'group');
                treeObj.checkAllNodes(false);
            };
            if (id == 'monthPositionInput') {
                var param = monthPositionInput.val();
                monthPosition.searchVehicleTree(param);
            };
            if (id == 'unusualPositionInput') {
                var param = $("#unusualPositionInput").val();
                unusualPosition.searchVehicleTree(param);
            };
        });

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

        /**
         * 监控对象树模糊查询
         */
        var monthTimer;
        monthPositionInput.on('input propertychange', function (value) {
            if (monthTimer !== undefined) {
                clearTimeout(monthTimer);
            };
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                monthIsSearch = true;
            };
            monthTimer = setTimeout(function () {
                if (monthIsSearch) {
                    var param = monthPositionInput.val();
                    monthPosition.searchVehicleTree(param);
                }
                monthIsSearch = true;
            }, 500);
        });
        monthPositionType.on('change', function () {
            var param = monthPositionInput.val();
            monthPosition.searchVehicleTree(param);
        });
        var unusualTimer;
        unusualPositionInput.on('input propertychange', function (value) {
            if (unusualTimer !== undefined) {
                clearTimeout(unusualTimer);
            };
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                unusualIsSearch = true;
            };
            unusualTimer = setTimeout(function () {
                if (unusualIsSearch) {
                    var param = unusualPositionInput.val();
                    unusualPosition.searchVehicleTree(param);
                }
                unusualIsSearch = true;
            }, 500);
        });
        unusualPositionType.on('change', function () {
            var param = unusualPositionInput.val();
            unusualPosition.searchVehicleTree(param);
        });

        positionDetail.init();

        $(document).on('click', companyVehicles.closeDrawer);
        drawerBox.on('click', function (event) {
            event.stopPropagation();
        });
        // 自定义验证
        jQuery.validator.addMethod("mustSelectGroups", function (value, element) {
            var treeObj = $.fn.zTree.getZTreeObj("vehiclePositionTree")
            var checkedNodes = treeObj.getCheckedNodes(true)
            return checkedNodes.length != 0
        })
    })
})(window, $)