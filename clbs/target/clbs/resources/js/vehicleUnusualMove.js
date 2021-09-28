(function (window, $) {
    //车辆列表
    var vehicleList = [];
    var vehicleListTwo = [];
    var vehicleListThree = [];
    //车辆id列表
    var vehicleId = [];
    var groupId = '';
    var vehicleIdTwo = [];
    var vehicleIdThree = [];
    var myTable;
    var myTableTwo;
    var myTableThree;
    //开始时间
    var startTime;
    var startTimeTwo;
    var startTimeThree;
    //结束时间
    var endTime;
    var endTimeTwo;
    var endTimeThree;
    var checkFlag = false; //判断组织节点是否是勾选操作
    var size; //当前权限监控对象数量

    var startLoc = [];
    var addressMsg = [];

    var zTreeIdJson = {};
    var ifAllCheck = true; //刚进入页面小于5000自动勾选
    var isSearch2 = true;
    var isSearch3 = true;
    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询？
    var getAddressStatus = false;

    var dbValue = false //树双击判断参数
    //公共方法提取
    publicFun = {
        treeInit: function (curObj, treeDemo) {
            //车辆树
            var setting = {
                async: {
                    url: publicFun.getSpeedReportTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "type": "multiple",
                        "icoType": "0"
                    },
                    dataFilter: publicFun.ajaxDataFilter
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
                    beforeClick: roadTransport.beforeClickVehicle,
                    onAsyncSuccess: roadTransport.zTreeOnAsyncSuccess,
                    beforeCheck: roadTransport.zTreeBeforeCheck,
                    onCheck: roadTransport.onCheckVehicle,
                    onExpand: roadTransport.zTreeOnExpand,
                }
            };
            $.fn.zTree.init($("#" + treeDemo), setting, null);
        },
        //获取组织树链接
        getSpeedReportTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle";
            }
        },
        //组织树数据筛选
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
        renderSelect: function (id) { //时间下拉框函数
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
        getDaysInOneMonth: function (year, month) {
            month = parseInt(month, 10);
            var d = new Date(year, month, 0);
            return d.getDate();
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
        // 显示隐藏列
        showMenuText: function (n) {
            //显示隐藏列
            var menu_text = "";
            var makeText = function (index, text) {
                var checkedStr = '';
                checkedStr = 'checked="checked"'
                return "<li><label><input type=\"checkbox\"  " + checkedStr + " class=\"toggle-vis\" data-column=\"" + index + "\" />" + text + "</label></li>"
            }
            menu_text += makeText(1, '道路运输企业');
            for (var i = 0; i < n; i++) { //减去序号，道路运输企业，合计
                menu_text += makeText(i + 2, i + 1);
            }
            menu_text += makeText(n + 2, '合计');
            $("#Ul-menu-text").html(menu_text);
        },
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
        getVehicleColor: function (color) { //根据后台返回数字显示车辆颜色
            switch (color) {
                case '0':
                    return '黑色';
                    break;
                case '1':
                    return '白色';
                    break;
                case '2':
                    return '红色';
                    break;
                case '3':
                    return '蓝色';
                    break;
                case '4':
                    return '紫色';
                    break;
                case '5':
                    return '黄色';
                    break;
                case '6':
                    return '绿色';
                    break;
                case '7':
                    return '粉色';
                    break;
                case '8':
                    return '棕色';
                    break;
                case '9':
                    return '灰色';
                    break;

            }
        },
        // 两个时间相差天数
        datedifference: function (sDate1, sDate2) {
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
        }
    };


    //车辆调度信息道路运输企业统计月报表
    roadTransport = {
        init: function () {
            //显示隐藏列
            var menu_text = "";
            var makeText = function (index, text) {
                var checkedStr = '';
                checkedStr = 'checked="checked"'
                return "<li><label><input type=\"checkbox\"  " + checkedStr + " class=\"toggle-vis\" data-column=\"" + index + "\" />" + text + "</label></li>"
            }
            menu_text += makeText(1, '道路运输企业');
            menu_text += makeText(2, '客运车禁行');
            menu_text += makeText(3, '山区公路禁行');
            menu_text += makeText(4, '合计');
            $("#Ul-menu-text").html(menu_text);

            //车辆树
            var setting = {
                async: {
                    // url: roadTransport.getSpeedReportTreeUrl,//"/clbs/m/functionconfig/fence/bindfence/vehicelTree",
                    url: '/clbs/m/basicinfo/enterprise/professionals/tree',
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "isOrg": "1"
                    },
                    dataFilter: roadTransport.ajaxDataFilter
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
                    beforeClick: roadTransport.beforeClickVehicle,
                    onAsyncSuccess: roadTransport.zTreeOnAsyncSuccess,
                    beforeCheck: roadTransport.zTreeBeforeCheck,
                    onCheck: roadTransport.onCheckVehicle,
                    onExpand: roadTransport.zTreeOnExpand,
                    onDblClick: roadTransport.onDblClickVehicle

                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);


            // 初始渲染表头
            // var nowT = new Date();
            // var yearT = nowT.getFullYear();
            // var monthT = nowT.getMonth() + 1;
            // var nowMonthDays =  publicFun.getDaysInOneMonth(yearT,monthT)

            // 显示隐藏列
            // publicFun.showMenuText(nowMonthDays)

            // roadTransport.renderTableHead($('#useTableHeadRender'),nowMonthDays)
        },
        //渲染表格头
        renderTableHead: function (obj, n) {


            var $obj = obj;


            $obj.html('');
            var html = '';
            html += '<th class="text-center">序号</th>';
            html += '<th class="text-center">道路运输企业</th>';
            for (var i = 0; i < n; i++) {
                html += '<th class="text-center">' + (i + 1) + '</th>'
            }
            html += '<th class="text-center">合计</th>'
            $obj.append(html);

            roadTransport.getTable('#dataTable');
        },
        getSpeedReportTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle";
            }
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
                tMonth = roadTransport.doHandleMonth(tMonth + 1);
                tDate = roadTransport.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " " +
                    "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 *
                    parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = roadTransport.doHandleMonth(endMonth + 1);
                endDate = roadTransport.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate + " " +
                    "23:59:59";
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = roadTransport.doHandleMonth(vMonth + 1);
                vDate = roadTransport.doHandleMonth(vDate);
                startTime = vYear + "-" + vMonth + "-" + vDate + " " +
                    "00:00:00";
                if (day == 1) {
                    endTime = vYear + "-" + vMonth + "-" + vDate + " " +
                        "23:59:59";
                } else {
                    var endNum = -1;
                    var vendtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * parseInt(endNum);
                    var dateEnd = new Date();
                    dateEnd.setTime(vendtoday_milliseconds);
                    var vendYear = dateEnd.getFullYear();
                    var vendMonth = dateEnd.getMonth();
                    var vendDate = dateEnd.getDate();
                    vendMonth = roadTransport.doHandleMonth(vendMonth + 1);
                    vendDate = roadTransport.doHandleMonth(vendDate);
                    endTime = vendYear + "-" + vendMonth + "-" + vendDate + " " +
                        "23:59:59";
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
            startTime = nowDate.getFullYear() +
                "-" +
                (parseInt(nowDate.getMonth() + 1) < 10 ? "0" +
                    parseInt(nowDate.getMonth() + 1) :
                    parseInt(nowDate.getMonth() + 1)) +
                "-" +
                (nowDate.getDate() < 10 ? "0" + nowDate.getDate() :
                    nowDate.getDate()) + " " + "00:00:00";
            endTime = nowDate.getFullYear() +
                "-" +
                (parseInt(nowDate.getMonth() + 1) < 10 ? "0" +
                    parseInt(nowDate.getMonth() + 1) :
                    parseInt(nowDate.getMonth() + 1)) +
                "-" +
                (nowDate.getDate() < 10 ? "0" + nowDate.getDate() :
                    nowDate.getDate()) +
                " " +
                ("23") +
                ":" +
                ("59") +
                ":" +
                ("59");
            var atime = $("#atime").val();
            if (atime != undefined && atime != "") {
                startTime = atime;
            }
        },
        inquireClick: function (number) {
            if (number == 0) {
                roadTransport.getsTheCurrentTime();
            } else if (number == -1) {
                roadTransport.startDay(-1)
            } else if (number == -3) {
                roadTransport.startDay(-3)
            } else if (number == -7) {
                roadTransport.startDay(-7)
            }
            if (number != 1) {
                $('#timeInterval').val(startTime + '--' + endTime);
                startTime = startTime;
                endTime = endTime;
            } else {
                var timeInterval = $('#timeInterval').val().split('--');
                startTime = timeInterval[0];
                endTime = timeInterval[1];
            };

            roadTransport.getCheckedNodes();
            if (!roadTransport.validates()) {
                return;
            }
            $('#simpleQueryParam').val('')
            var url = "/clbs/cb/cbReportManagement/vehicleUnusualMove/companyTransport/list";
            var parameter = {
                orgIds: groupId,
                startTime: startTime,
                endTime: endTime,
            };
            json_ajax("POST", url, "json", true, parameter, roadTransport.getCallback);
        },
        exportAlarm: function () {
            if (getRecordsNum() > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var url = '/clbs/cb/cbReportManagement/vehicleUnusualMove/companyTransport/export';
            var parame = {
                orgIds: groupId,
                startTime: startTime,
                endTime: endTime,
                simpleQueryParam: $('#simpleQueryParam').val()
            }
            json_ajax("post", url, "json", false, parame, function (data) {
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
        validates: function () {
            return $("#speedlist").validate({
                rules: {
                    startTime: {
                        required: true
                    },
                    endTime: {
                        required: true,
                        compareDate: "#timeInterval",
                    },
                    groupSelect: {
                        // zTreeChecked: "treeDemo"
                        required: true
                    }
                },
                messages: {
                    startTime: {
                        required: "请选择开始日期！",
                    },
                    endTime: {
                        required: "请选择结束时间！",
                        compareDate: endtimeComStarttime,
                    },
                    groupSelect: {
                        // zTreeChecked: vehicleSelectBrand,
                        required: vehicleSelectGroup
                    }
                }
            }).form();
        },
        unique: function (arr) {
            var result = [],
                hash = {};
            for (var i = 0, elem;
                (elem = arr[i]) != null; i++) {
                if (!hash[elem]) {
                    result.push(elem);
                    hash[elem] = true;
                }
            }
            return result;
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
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
        onDblClickVehicle: function (e, treeId, treeNode) {
            if (!treeNode) return
            dbValue = true
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.checkAllNodes(false);
            roadTransport.getCheckedNodes("treeDemo");
            // $("#groupSelect").val(treeNode.name);
            // search_ztree('treeDemo', 'groupSelect', 'group');
            treeObj.checkNode(treeNode, !treeNode.checked, false, true);
        },
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (size <= GROUP_MAX_CHECK && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            roadTransport.getCharSelect(treeObj);
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked && !dbValue) {
                var zTree = $.fn.zTree.getZTreeObj("treeDemo");
                var nodes = zTree.getNodesByFilter(function (node) {
                    return node;
                }, false, treeNode);
                var nodesLength = nodes.length;
                if (nodesLength > GROUP_MAX_CHECK) {
                    layer.msg('最多勾选' + GROUP_MAX_CHECK + '个企业' + '<br/>双击名称可选中本组织');
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
                // setTimeout(function () {
                    zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                // }, 1200);
                setTimeout(() => {
                    roadTransport.getCheckedNodes();
                    roadTransport.validates();
                }, 600);
            }
            roadTransport.getCharSelect(zTree);
            roadTransport.getCheckedNodes();
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
                var assign = []; // 当前组织及下级组织的所有分组
                roadTransport.getGroupChild(treeNode, assign);
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
        getCharSelect: function (treeObj) {
            var nodes = treeObj.getCheckedNodes(true);
            var allNodes = treeObj.getNodes();
            if (nodes.length > 0) {
                $("#groupSelect").val(allNodes[0].name);
            } else {
                $("#groupSelect").val("");
            }
        },
        getGroupChild: function (node, assign) { // 递归获取组织及下级组织的分组节点
            var nodes = node.children;
            if (nodes != null && nodes != undefined && nodes.length > 0) {
                for (var i = 0; i < nodes.length; i++) {
                    var node = nodes[i];
                    if (node.type == "assignment") {
                        assign.push(node);
                    } else if (node.type == "group" && node.children != undefined) {
                        roadTransport.getGroupChild(node.children, assign);
                    }
                }
            }
        },
        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
                nodes = zTree
                .getCheckedNodes(true),
                v = "",
                vid = "";

            console.log(nodes)
            for (var i = 0, l = nodes.length; i < l; i++) {
                // if (nodes[i].type == "vehicle") {
                //     v += nodes[i].name + ",";
                //     vid += nodes[i].id + ",";
                // }
                if (nodes[i].type == "group") {
                    v += nodes[i].name + ",";
                    vid += nodes[i].uuid + ",";
                }
            }
            vehicleList = v;
            vehicleId = vid;
            groupId = vid
        },
        getCallback: function (date) {
            getAddressStatus = false;
            if (date.success == true) {
                dataListArray = []; //用来储存显示数据
                if (date.obj != null && date.obj.length != 0) {
                    $("#exportAlarm").removeAttr("disabled");
                    var alarm = date.obj;


                    for (var i = 0; i < alarm.length; i++) {

                        var dateList = [
                            i + 1,
                            alarm[i].orgName,
                            alarm[i].passengerVehicleForbid,
                            alarm[i].mountainRoadForbid,
                            alarm[i].total
                        ]


                        dataListArray.push(dateList);
                    }

                    // roadTransport.getTable('#dataTable');

                    // 显示隐藏列
                    // publicFun.showMenuText(dataListArray[0])

                    roadTransport.reloadData(dataListArray);
                    $("#simpleQueryParam").val("");
                } else {
                    roadTransport.reloadData(dataListArray);
                    $("#simpleQueryParam").val("");
                    $("#exportAlarm").attr("disabled", "disabled");
                }
            } else {
                if (date.msg != null) {
                    layer.msg(date.msg, {
                        move: false
                    });
                }
            }
        },
        getTable: function (table) {
            myTable = $(table).DataTable({
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
                    // "sInfoFiltered": "（数据库中共为 _MAX_ 条记录）",
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
                    // "targets": [0, 2, 3, 4, 5, 6, 7, 8, 9],
                    "searchable": false
                }],
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
            $('.toggle-vis').on('change', function (e) {
                var column = myTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });
            $("#search_button").on("click", function () {
                var tsval = $("#simpleQueryParam").val();
                myTable.column(1).search(tsval, false, false).draw();
                if($('#dataTable .dataTables_empty').length != 0){
                    $('#exportAlarm')[0].disabled = true
                }else {
                    $('#exportAlarm')[0].disabled = false
                }
            });
        },
        reloadData: function (dataList) {
            var currentPage = myTable.page()
            myTable.clear()
            myTable.rows.add(dataList)
            myTable.page(currentPage).draw(false);
            myTable.column(1).search('', false, false).page(currentPage).draw();
        },
        //刷新列表
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            var tsval = $("#simpleQueryParam").val();
            myTable.column(1).search(tsval, false, false).draw();
            if($('#dataTable .dataTables_empty').length > 0){
                $('#exportAlarm').prop('disabled', true)
            }else{
                $('#exportAlarm').prop('disabled', false)
            }
        },
    };


    //车辆调度信息统计月报表
    overspeedVehicle = {
        init: function () {

            overspeedVehicle.showMenuText()

            //车辆树
            var setting = {
                async: {
                    url: overspeedVehicle.getSpeedReportTreeUrl, //"/clbs/m/functionconfig/fence/bindfence/vehicelTree",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "type": "multiple",
                        "icoType": "0"
                    },
                    dataFilter: overspeedVehicle.ajaxDataFilter
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
                    beforeClick: overspeedVehicle.beforeClickVehicle,
                    onAsyncSuccess: overspeedVehicle.zTreeOnAsyncSuccess,
                    beforeCheck: overspeedVehicle.zTreeBeforeCheck,
                    onCheck: overspeedVehicle.onCheckVehicle,
                    onNodeCreated: overspeedVehicle.zTreeOnNodeCreated,
                    onExpand: overspeedVehicle.zTreeOnExpand
                }
            };
            $.fn.zTree.init($("#treeDemoTwo"), setting, null);

            // 初始渲染表头
            // var nowT = new Date();
            // var yearT = nowT.getFullYear();
            // var monthT = nowT.getMonth() + 1;
            // var nowMonthDays =  publicFun.getDaysInOneMonth(yearT,monthT)

            // 显示隐藏列
            // overspeedVehicle.showMenuText(nowMonthDays)
            // overspeedVehicle.renderTableHead($('#useTableHeadRender3'),nowMonthDays)
        },
        showMenuText: function (n) {
            //显示隐藏列
            var menu_text = "";
            var makeText = function (index, text) {
                var checkedStr = '';
                checkedStr = 'checked="checked"'
                return "<li><label><input type=\"checkbox\"  " + checkedStr + " class=\"toggle-vis2\" data-column=\"" + index + "\" />" + text + "</label></li>"
            }
            menu_text += makeText(1, '车牌号');
            menu_text += makeText(2, '车牌颜色');
            menu_text += makeText(3, '车辆类型');
            menu_text += makeText(4, '所属道路运输企业');
            // for (var i = 0; i < n; i++) { //减去序号，道路运输企业，合计
            //     menu_text += makeText(i+5, i+1);
            // }
            menu_text += makeText(5, '客运车禁行');
            menu_text += makeText(6, '山区公路禁行');
            menu_text += makeText(7, '合计');
            $("#Ul-menu-textTwo").html(menu_text);;
        },
        // 渲染表格头
        renderTableHead: function (obj, n) {
            var $obj = obj;
            $obj.html('');
            var html = '';
            html += '<th class="text-center">序号</th>';
            html += '<th class="text-center">车牌号</th>';
            html += '<th class="text-center">车牌颜色</th>';
            html += '<th class="text-center">车辆类型</th>';
            html += '<th class="text-center">所属道路运输企业</th>';
            for (var i = 0; i < n; i++) {
                html += '<th class="text-center">' + (i + 1) + '</th>'
            }
            html += '<th class="text-center">合计</th>'
            $obj.append(html);

            overspeedVehicle.getTable('#dataTableTwo');
        },
        getSpeedReportTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle";
            }
        },
        //开始时间
        startDay: function (day) {
            var timeInterval = $('#timeIntervalTwo').val().split('--');
            var startValue = timeInterval[0];
            var endValue = timeInterval[1];
            if (startValue == "" || endValue == "") {
                var today = new Date();
                var targetday_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * day;
                today.setTime(targetday_milliseconds); //注意，这行是关键代码
                var tYear = today.getFullYear();
                var tMonth = today.getMonth();
                var tDate = today.getDate();
                tMonth = overspeedVehicle.doHandleMonth(tMonth + 1);
                tDate = overspeedVehicle.doHandleMonth(tDate);
                var num = -(day + 1);
                startTimeTwo = tYear + "-" + tMonth + "-" + tDate + " " +
                    "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 *
                    parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = overspeedVehicle.doHandleMonth(endMonth + 1);
                endDate = overspeedVehicle.doHandleMonth(endDate);
                endTimeTwo = endYear + "-" + endMonth + "-" + endDate + " " +
                    "23:59:59";
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = overspeedVehicle.doHandleMonth(vMonth + 1);
                vDate = overspeedVehicle.doHandleMonth(vDate);
                startTimeTwo = vYear + "-" + vMonth + "-" + vDate + " " +
                    "00:00:00";
                if (day == 1) {
                    endTimeTwo = vYear + "-" + vMonth + "-" + vDate + " " +
                        "23:59:59";
                } else {
                    var endNum = -1;
                    var vendtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * parseInt(endNum);
                    var dateEnd = new Date();
                    dateEnd.setTime(vendtoday_milliseconds);
                    var vendYear = dateEnd.getFullYear();
                    var vendMonth = dateEnd.getMonth();
                    var vendDate = dateEnd.getDate();
                    vendMonth = overspeedVehicle.doHandleMonth(vendMonth + 1);
                    vendDate = overspeedVehicle.doHandleMonth(vendDate);
                    endTimeTwo = vendYear + "-" + vendMonth + "-" + vendDate + " " +
                        "23:59:59";
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
            startTimeTwo = nowDate.getFullYear() +
                "-" +
                (parseInt(nowDate.getMonth() + 1) < 10 ? "0" +
                    parseInt(nowDate.getMonth() + 1) :
                    parseInt(nowDate.getMonth() + 1)) +
                "-" +
                (nowDate.getDate() < 10 ? "0" + nowDate.getDate() :
                    nowDate.getDate()) + " " + "00:00:00";
            endTimeTwo = nowDate.getFullYear() +
                "-" +
                (parseInt(nowDate.getMonth() + 1) < 10 ? "0" +
                    parseInt(nowDate.getMonth() + 1) :
                    parseInt(nowDate.getMonth() + 1)) +
                "-" +
                (nowDate.getDate() < 10 ? "0" + nowDate.getDate() :
                    nowDate.getDate()) +
                " " +
                ("23") +
                ":" +
                ("59") +
                ":" +
                ("59");
            var atime = $("#atime").val();
            if (atime != undefined && atime != "") {
                startTimeTwo = atime;
            }
        },
        inquireClick: function (number) {
            if (number == 0) {
                overspeedVehicle.getsTheCurrentTime();
            } else if (number == -1) {
                overspeedVehicle.startDay(-1)
            } else if (number == -3) {
                overspeedVehicle.startDay(-3)
            } else if (number == -7) {
                overspeedVehicle.startDay(-7)
            }
            if (number != 1) {
                $('#timeIntervalTwo').val(startTimeTwo + '--' + endTimeTwo);
                startTimeTwo = startTimeTwo;
                endTimeTwo = endTimeTwo;
            } else {
                var timeInterval = $('#timeIntervalTwo').val().split('--');
                startTimeTwo = timeInterval[0];
                endTimeTwo = timeInterval[1];
            };

            overspeedVehicle.getCheckedNodes();
            if (!overspeedVehicle.validates()) {
                return;
            }
            $('#simpleQueryParamTwo').val('')
            var url = "/clbs/cb/cbReportManagement/vehicleUnusualMove/drive/list";
            var parameter = {
                monitorIds: vehicleIdTwo,
                startTime: startTimeTwo,
                endTime: endTimeTwo,
            };
            json_ajax("POST", url, "json", true, parameter, overspeedVehicle.getCallback);

        },
        exportAlarm: function () {
            if (getRecordsNum('dataTableTwo_info') > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var exportUrl = "/clbs/cb/cbReportManagement/vehicleUnusualMove/drive/export";
            var parameter = {
                monitorIds: vehicleIdTwo,
                startTime: startTimeTwo,
                endTime: endTimeTwo,
                simpleQueryParam: $('#simpleQueryParamTwo').val()
            }
            json_ajax("post", exportUrl, "json", false, parameter, function (data) {
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
        validates: function () {
            return $("#speedlistTwo").validate({
                rules: {
                    startTimeTwo: {
                        required: true
                    },
                    endTimeTwo: {
                        required: true,
                        compareDate: "#timeIntervalTwo"
                    },
                    groupSelectTwo: {
                        zTreeChecked: "treeDemoTwo"
                    }
                },
                messages: {
                    startTimeTwo: {
                        required: "请选择开始日期！"
                    },
                    endTimeTwo: {
                        required: "请选择结束时间！",
                        compareDate: endtimeComStarttime
                    },
                    groupSelectTwo: {
                        zTreeChecked: vehicleSelectBrand
                    }
                }
            }).form();
        },
        unique: function (arr) {
            var result = [],
                hash = {};
            for (var i = 0, elem;
                (elem = arr[i]) != null; i++) {
                if (!hash[elem]) {
                    result.push(elem);
                    hash[elem] = true;
                }
            }
            return result;
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemoTwo");
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
            var zTree = $.fn.zTree.getZTreeObj("treeDemoTwo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemoTwo");
            if (size <= 100 && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            overspeedVehicle.getCharSelect(treeObj);
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj("treeDemoTwo"),
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
                    var zTree = $.fn.zTree.getZTreeObj("treeDemoTwo"),
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
        onCheckVehicle: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemoTwo");
            if (treeNode.checked) {
                //若为取消勾选则不展开节点
                isSearch2 = false;
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    overspeedVehicle.getCheckedNodes();
                    overspeedVehicle.validates();
                }, 600)
            }
            overspeedVehicle.getCharSelect(zTree);
            overspeedVehicle.getCheckedNodes();
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
            var treeObj = $.fn.zTree.getZTreeObj("treeDemoTwo");
            if (treeNode.type == "group") {
                /* var assign = []; // 当前组织及下级组织的所有分组
                 overspeedVehicle.getGroupChild(treeNode, assign);
                 if (assign != null && assign.length > 0) {
                     for (var i = 0; i < assign.length; i++) {
                         var node = assign[i];
                         if (node.type == "assignment" && node.children === undefined) {
                             if (!node.zAsync) { // 判断节点是否进行异步加载，若没有，则先异步加载，避免添加重复节点
                                 treeObj.reAsyncChildNodes(node, "refresh");
                             }
                         }
                     }
                 }*/

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
                $("#groupSelectTwo").val(allNodes[0].name);
            } else {
                $("#groupSelectTwo").val("");
            }
        },
        getGroupChild: function (node, assign) { // 递归获取组织及下级组织的分组节点
            var nodes = node.children;
            if (nodes != null && nodes != undefined && nodes.length > 0) {
                for (var i = 0; i < nodes.length; i++) {
                    var node = nodes[i];
                    if (node.type == "assignment") {
                        assign.push(node);
                    } else if (node.type == "group" && node.children != undefined) {
                        overspeedVehicle.getGroupChild(node.children, assign);
                    }
                }
            }
        },
        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemoTwo"),
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
            vehicleListTwo = v;
            vehicleIdTwo = vid;
        },
        getCallback: function (date) { //wjk

            if (date.success == true) {
                dataListArray = []; //用来储存显示数据
                if (date.obj != null && date.obj.length != 0) {
                    $("#exportAlarmTwo").removeAttr("disabled");
                    var alarm = date.obj;
                    for (var i = 0; i < alarm.length; i++) {
                        // var color = publicFun.getVehicleColor(alarm[i].color)
                        var dateList = [
                            i + 1,
                            alarm[i].monitorName,
                            alarm[i].plateColorStr,
                            alarm[i].vehicleType == 'null' ? '其他车辆' : alarm[i].vehicleType,
                            alarm[i].orgName,
                            alarm[i].passengerVehicleForbid,
                            alarm[i].mountainRoadForbid,
                            alarm[i].total
                        ]
                        // for (var j = 0; j < alarm[i].dateTime.length; j++) {
                        //     dateList.push(alarm[i].dateTime[j])
                        // }
                        // dateList.push(alarm[i].count)
                        dataListArray.push(dateList);
                    }
                    overspeedVehicle.reloadData(dataListArray);
                    $("#simpleQueryParamTwo").val("");
                } else {
                    overspeedVehicle.reloadData(dataListArray);
                    $("#simpleQueryParamTwo").val("");
                    $("#exportAlarmTwo").attr("disabled", "disabled");
                }
            } else {
                if (date.msg != null) {
                    layer.msg(date.msg, {
                        move: false
                    });
                }
            }
            $("#search_buttonTwo").click();
        },
        getTable: function (table) {
            myTableTwo = $(table).DataTable({
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
                    // "sInfoFiltered": "（数据库中共为 _MAX_ 条记录）",
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
                    "targets": [0, 2, 3, 4, 5, 6, 7],
                    "searchable": false
                }],
                "order": [
                    [0, null]
                ], // 第一列排序图标改为默认

            });
            myTableTwo.on('order.dt search.dt', function () {
                myTableTwo.column(0, {
                    search: 'applied',
                    order: 'applied'
                }).nodes().each(function (cell, i) {
                    cell.innerHTML = i + 1;
                });
            }).draw();
            //显示隐藏列
            $('.toggle-vis2').on('change', function (e) {
                var column = myTableTwo.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });
            $("#search_buttonTwo").on("click", function () {
                var tsval = $("#simpleQueryParamTwo").val();
                myTableTwo.column(1).search(tsval, false, false).draw();
                if($('#dataTableTwo .dataTables_empty').length != 0){
                    $('#exportAlarmTwo')[0].disabled = true
                }else {
                    $('#exportAlarmTwo')[0].disabled = false
                }
            });
        },
        reloadData: function (dataList) {
            var currentPage = myTableTwo.page();
            myTableTwo.clear();
            myTableTwo.rows.add(dataList);
            myTableTwo.page(currentPage).draw(false);
            myTable.column(1).search('', false, false).page(currentPage).draw();
        },
        //刷新列表
        refreshTable: function () {
            $("#simpleQueryParamTwo").val("");
            var tsval = $("#simpleQueryParamTwo").val();
            myTableTwo.column(1).search(tsval, false, false).draw();
            if($('#dataTableTwo .dataTables_empty').length > 0){
                $('#exportAlarmTwo').prop('disabled', true)
            }else{
                $('#exportAlarmTwo').prop('disabled', false)
            }
        }
    };


    //车辆调度信息明细表
    vehicleDetail = {
        init: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTableThree tr th:gt(0)");
            // menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(1) + "\" disabled />" + table[0].innerHTML + "</label></li>";
            for (var i = 0; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis3\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-textThree").html(menu_text);

            //车辆树
            var setting = {
                async: {
                    url: vehicleDetail.getSpeedReportTreeUrl, //"/clbs/m/functionconfig/fence/bindfence/vehicelTree",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "type": "multiple",
                        "icoType": "0"
                    },
                    dataFilter: vehicleDetail.ajaxDataFilter
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
                    beforeClick: vehicleDetail.beforeClickVehicle,
                    onAsyncSuccess: vehicleDetail.zTreeOnAsyncSuccess,
                    beforeCheck: vehicleDetail.zTreeBeforeCheck,
                    onCheck: vehicleDetail.onCheckVehicle,
                    onNodeCreated: vehicleDetail.zTreeOnNodeCreated,
                    onExpand: vehicleDetail.zTreeOnExpand
                }
            };
            $.fn.zTree.init($("#treeDemoThree"), setting, null);
        },
        getSpeedReportTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle";
            }
        },
        //开始时间
        startDay: function (day) {
            var timeInterval = $('#timeIntervalThree').val().split('--');
            var startValue = timeInterval[0];
            var endValue = timeInterval[1];
            if (startValue == "" || endValue == "") {
                var today = new Date();
                var targetday_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * day;
                today.setTime(targetday_milliseconds); //注意，这行是关键代码
                var tYear = today.getFullYear();
                var tMonth = today.getMonth();
                var tDate = today.getDate();
                tMonth = vehicleDetail.doHandleMonth(tMonth + 1);
                tDate = vehicleDetail.doHandleMonth(tDate);
                var num = -(day + 1);
                startTimeThree = tYear + "-" + tMonth + "-" + tDate + " " +
                    "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 *
                    parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = vehicleDetail.doHandleMonth(endMonth + 1);
                endDate = vehicleDetail.doHandleMonth(endDate);
                endTimeThree = endYear + "-" + endMonth + "-" + endDate + " " +
                    "23:59:59";
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = vehicleDetail.doHandleMonth(vMonth + 1);
                vDate = vehicleDetail.doHandleMonth(vDate);
                startTimeThree = vYear + "-" + vMonth + "-" + vDate + " " +
                    "00:00:00";
                if (day == 1) {
                    endTimeThree = vYear + "-" + vMonth + "-" + vDate + " " +
                        "23:59:59";
                } else {
                    var endNum = -1;
                    var vendtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * parseInt(endNum);
                    var dateEnd = new Date();
                    dateEnd.setTime(vendtoday_milliseconds);
                    var vendYear = dateEnd.getFullYear();
                    var vendMonth = dateEnd.getMonth();
                    var vendDate = dateEnd.getDate();
                    vendMonth = vehicleDetail.doHandleMonth(vendMonth + 1);
                    vendDate = vehicleDetail.doHandleMonth(vendDate);
                    endTimeThree = vendYear + "-" + vendMonth + "-" + vendDate + " " +
                        "23:59:59";
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
            startTimeThree = nowDate.getFullYear() +
                "-" +
                (parseInt(nowDate.getMonth() + 1) < 10 ? "0" +
                    parseInt(nowDate.getMonth() + 1) :
                    parseInt(nowDate.getMonth() + 1)) +
                "-" +
                (nowDate.getDate() < 10 ? "0" + nowDate.getDate() :
                    nowDate.getDate()) + " " + "00:00:00";
            endTimeThree = nowDate.getFullYear() +
                "-" +
                (parseInt(nowDate.getMonth() + 1) < 10 ? "0" +
                    parseInt(nowDate.getMonth() + 1) :
                    parseInt(nowDate.getMonth() + 1)) +
                "-" +
                (nowDate.getDate() < 10 ? "0" + nowDate.getDate() :
                    nowDate.getDate()) +
                " " +
                ("23") +
                ":" +
                ("59") +
                ":" +
                ("59");
            var atime = $("#atime").val();
            if (atime != undefined && atime != "") {
                startTimeThree = atime;
            }
        },
        inquireClick: function (number,searchFlag) {
            if (number == 0) {
                vehicleDetail.getsTheCurrentTime();
            } else if (number == -1) {
                vehicleDetail.startDay(-1)
            } else if (number == -3) {
                vehicleDetail.startDay(-3)
            } else if (number == -7) {
                vehicleDetail.startDay(-7)
            }
            if (number != 1) {
                $('#timeIntervalThree').val(startTimeThree + '--' + endTimeThree);
                startTimeThree = startTimeThree;
                endTimeThree = endTimeThree;
            } else {
                var timeInterval = $('#timeIntervalThree').val().split('--');
                startTimeThree = timeInterval[0];
                endTimeThree = timeInterval[1];
            };
            vehicleDetail.getCheckedNodes();
            if (!vehicleDetail.validates()) {
                return;
            }
            if(!searchFlag){
                $('#simpleQueryParamThree').val('')
            }
            if(!myTableThree){
                vehicleDetail.getTable()
            }else {
                myTableThree.requestData();
            }
        },
        exportAlarm: function () {
            var exportUrl = "/clbs/cb/cbReportManagement/vehicleUnusualMove/detail/export";
            if (getRecordsNum('dataTableThree_info') > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var alarmType = $('#alermType').val()
            var parameter = {
                "monitorIds": vehicleIdThree,
                "startTime": startTimeThree,
                "endTime": endTimeThree,
                "alarmType": alarmType,
                simpleQueryParam: $('#simpleQueryParamThree').val()
            }
            json_ajax("post", exportUrl, "json", false, parameter, function (data) {
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
        validates: function () {
            return $("#speedlistThree").validate({
                rules: {
                    startTimeTwo: {
                        required: true
                    },
                    endTimeTwo: {
                        required: true,
                        compareDate: "#timeIntervalThree"
                    },
                    groupSelectThree: {
                        zTreeChecked: "treeDemoThree"
                    }
                },
                messages: {
                    startTimeTwo: {
                        required: "请选择开始日期！"
                    },
                    endTimeTwo: {
                        required: "请选择结束时间！",
                        compareDate: endtimeComStarttime
                    },
                    groupSelectThree: {
                        zTreeChecked: vehicleSelectBrand
                    }
                }
            }).form();
        },
        unique: function (arr) {
            var result = [],
                hash = {};
            for (var i = 0, elem;
                (elem = arr[i]) != null; i++) {
                if (!hash[elem]) {
                    result.push(elem);
                    hash[elem] = true;
                }
            }
            return result;
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemoThree");
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
            var zTree = $.fn.zTree.getZTreeObj("treeDemoThree");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemoThree");
            if (size <= 100 && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            vehicleDetail.getCharSelect(treeObj);
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj("treeDemoThree"),
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
                    var zTree = $.fn.zTree.getZTreeObj("treeDemoThree"),
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
        onCheckVehicle: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemoThree");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                isSearch3 = false;
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    vehicleDetail.getCheckedNodes();
                    vehicleDetail.validates();
                }, 600)
            }
            vehicleDetail.getCharSelect(zTree);
            vehicleDetail.getCheckedNodes();
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
            var treeObj = $.fn.zTree.getZTreeObj("treeDemoThree");
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
                $("#groupSelectThree").val(allNodes[0].name);
            } else {
                $("#groupSelectThree").val("");
            }
        },
        getGroupChild: function (node, assign) { // 递归获取组织及下级组织的分组节点
            var nodes = node.children;
            if (nodes != null && nodes != undefined && nodes.length > 0) {
                for (var i = 0; i < nodes.length; i++) {
                    var node = nodes[i];
                    if (node.type == "assignment") {
                        assign.push(node);
                    } else if (node.type == "group" && node.children != undefined) {
                        vehicleDetail.getGroupChild(node.children, assign);
                    }
                }
            }
        },
        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemoThree"),
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
            vehicleListThree = v;
            vehicleIdThree = vid;
        },
        getCallback: function (date) {
            if (date.success == true) {
                dataListArray = []; //用来储存显示数据
                if (date.records != null && date.records.length != 0) {
                    $("#exportAlarmThree").removeAttr("disabled");
                    var alarm = date.records;
                    startLoc = [];
                    for (var i = 0; i < alarm.length; i++) {
                        var alarmType;
                        if (alarm[i].alarmType == 7702) {
                            alarmType = '异动报警（客运车）'
                        }
                        if (alarm[i].alarmType == 7703) {
                            alarmType = '异动报警（山路）'
                        }

                        var dateList = [
                            i + 1,
                            alarm[i].brand,
                            alarm[i].color,
                            alarm[i].groupName,
                            alarm[i].alarmTime,
                            vehicleDetail.tableTextShow(alarm[i].speed),
                            vehicleDetail.tableTextShow(alarm[i].limitSpeed),
                            alarmType,
                            alarm[i].address,
                            // '加载中...'
                        ];
                        dataListArray.push(dateList);

                        startLoc.push(alarm[i].address)
                    }
                    vehicleDetail.reloadData(dataListArray);
                    $("#simpleQueryParamThree").val("");
                } else {
                    vehicleDetail.reloadData(dataListArray);
                    $("#simpleQueryParamThree").val("");
                    $("#exportAlarmThree").attr("disabled", "disabled");
                }
            } else {
                if (date.msg != null) {
                    layer.msg(date.msg, {
                        move: false
                    });
                }
            }
        },
        tableTextShow: function (text) {
            if (text == null) {
                return ''
            }
            return text;

        },
        getTable: function (table) {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTableThree tr th:gt(1)");
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis3\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis3\" data-column=\"" + parseInt(i + 2) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            ;
            $("#Ul-menu-textThree").html(menu_text);
            //表格列定义
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
                },
                {
                    "data": "brand",
                    "class": "text-center",
                },
                {
                    "data": "color",
                    "class": "text-center",
                },
                {
                    "data": "groupName",
                    "class": "text-center",
                },
                {
                    "data": "alarmTime",
                    "class": "text-center",
                },
                {
                    "data": "speed",
                    "class": "text-center",
                    render: function (data) {
                        return vehicleDetail.tableTextShow(data)
                    }
                },
                {
                    "data": "limitSpeed",
                    "class": "text-center",
                    render: function (data) {
                        return vehicleDetail.tableTextShow(data)
                    }
                },
                {
                    "data": "alarmTypeStr",
                    "class": "text-center",
                },
                {
                    "data": "address",
                    "class": "text-center",
                },
            ]
            var ajaxDataParamFun = function (d) {
                d.monitorIds= vehicleIdThree
                d.startTime= startTimeThree
                d.endTime= endTimeThree
                d.simpleQueryParam= $('#simpleQueryParamThree').val()
                d.alarmType = $('#alermType').val()
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/cb/cbReportManagement/vehicleUnusualMove/detail/list',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTableThree', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                ajaxCallBack: function (data) {
                    if(data.records.length == 0){
                        $('#exportAlarmThree')[0].disabled = true
                    }else {
                        $('#exportAlarmThree')[0].disabled = false
                    }
                }
            };
            myTableThree = new TG_Tabel.createNew(setting);
            myTableThree.init();
            //显示隐藏列
            $('.toggle-vis3').on('change', function (e) {
                var column = myTableThree.dataTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });
            $('#search_buttonThree').on('click',function () {
                vehicleDetail.inquireClick(1, true)
            })
        },
        reloadData: function (dataList) {
            var currentPage = myTableThree.page();
            myTableThree.clear();
            myTableThree.rows.add(dataList);
            myTableThree.page(currentPage).draw(false);
            myTable.column(1).search('', false, false).page(currentPage).draw();
        },
        //刷新列表
        refreshTable: function () {
            vehicleDetail.inquireClick(1)
        },
        //对显示的数据进行逆地址解析
        getAddress: function (addressStr) {
            getAddressStatus = true;
            var $dataTableTbody = $("#dataTableThree tbody");
            var dataLength = $dataTableTbody.children("tr").length;
            var num = 0;
            for (var i = 0; i < dataLength; i++) {
                num++;
                var n = $dataTableTbody.children("tr:nth-child(" + (i + 1) + ")").children("td:nth-child(1)").text();
                var startMsg = [];

                if (addressStr[n - 1] != null && parseFloat(addressStr[n - 1].split(",")[0]) >= 73.33 <= 135.05 && parseFloat(addressStr[n - 1].split(",")[1]) >= 3.51 <= 53.33) {
                    startMsg = [addressStr[n - 1].split(",")[0], addressStr[n - 1].split(",")[1]];
                } else if (endStr[n - 1] === '0.0,0.0') {
                    startMsg = [addressStr[n - 1].split(",")[0], addressStr[n - 1].split(",")[1]];
                } else {
                    startMsg = ["124.411991", "29.043817"];
                }
                addressMsg.push(startMsg);
                if (num == dataLength) {
                    var addressIndex = 0;
                    var addressArray = [];
                    backAddressMsg1(addressIndex, addressMsg, null, addressArray, "dataTableThree", 9);
                    addressMsg = [];
                }
            }
        },
        //逆地址解析回调方法
        goBack: function (GeocoderResult) {
            msgArray = GeocoderResult;
            var $dataTableTbody = $("#dataTableThree tbody");
            var dataLength = $dataTableTbody.children("tr").length;
            for (var i = 0; i < dataLength; i++) {
                if (msgArray[i] != undefined) {
                    $dataTableTbody.children("tr:nth-child(" + (i + 1) + ")").children("td:nth-child(9)").text(msgArray[i][0]);

                }
            }
        },
        //模糊查询树
        searchVehicleTree2: function (param) {

            ifAllCheck = false; //模糊查询不自动勾选

            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                // $.fn.zTree.init($("#treeDemo"), setting, null);
                overspeedVehicle.init()
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
                            "queryParam": param
                        },
                        dataFilter: vehicleDetail.ajaxQueryDataFilter
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
                        beforeClick: overspeedVehicle.beforeClickVehicle,
                        onCheck: overspeedVehicle.onCheckVehicle,
                        // beforeCheck: publicFun.zTreeBeforeCheck,
                        onExpand: overspeedVehicle.zTreeOnExpand,
                        //beforeAsync: publicFun.zTreeBeforeAsync,
                        // onAsyncSuccess: publicFun.zTreeOnAsyncSuccess,
                        onNodeCreated: overspeedVehicle.zTreeOnNodeCreated
                    }
                };
                $.fn.zTree.init($("#treeDemoTwo"), querySetting, null);
            }
        },
        searchVehicleTree3: function (param) {

            ifAllCheck = false; //模糊查询不自动勾选

            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                // $.fn.zTree.init($("#treeDemo"), setting, null);
                vehicleDetail.init()
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
                            "type": $('#queryType1').val(),
                            "queryParam": param
                        },
                        dataFilter: vehicleDetail.ajaxQueryDataFilter
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
                        beforeClick: vehicleDetail.beforeClickVehicle,
                        onCheck: vehicleDetail.onCheckVehicle,
                        // beforeCheck: publicFun.zTreeBeforeCheck,
                        onExpand: vehicleDetail.zTreeOnExpand,
                        //beforeAsync: publicFun.zTreeBeforeAsync,
                        // onAsyncSuccess: publicFun.zTreeOnAsyncSuccess,
                        onNodeCreated: vehicleDetail.zTreeOnNodeCreated
                    }
                };
                $.fn.zTree.init($("#treeDemoThree"), querySetting, null);
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
    };


    $(function () {
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                search_ztree('treeDemo', 'groupSelect', 'group');
                treeObj.checkAllNodes(false);
            };
            if (id == 'groupSelectTwo') {
                var param = $("#groupSelectTwo").val();
                vehicleDetail.searchVehicleTree2(param);
            };
            if (id == 'groupSelectThree') {
                var param = $("#groupSelectThree").val();
                vehicleDetail.searchVehicleTree3(param);
            };
        });

        /***持续超速道路运输企业统计表***/
        //初始化页面
        roadTransport.init();
        roadTransport.getTable('#dataTable');

        //当前时间
        roadTransport.getsTheCurrentTime();
        $('#timeInterval').dateRangePicker({
            dateLimit: 31,
            inquireBtn: '#inquireClickOne'
        });
        // publicFun.renderSelect('#select2')
        // publicFun.renderSelect('#select3')


        $("#groupSelect").bind("click", showMenuContent);
        //导出
        $("#exportAlarm").bind("click", roadTransport.exportAlarm);
        $("#refreshTable").bind("click", roadTransport.refreshTable);


        /***持续超速车辆统计表***/
        overspeedVehicle.init();
        overspeedVehicle.getTable('#dataTableTwo');

        //当前时间
        overspeedVehicle.getsTheCurrentTime();
        $('#timeIntervalTwo').dateRangePicker({
            dateLimit: 31,
            inquireBtn: '#inquireClickTwo'
        });


        $('#timeIntervalThree').dateRangePicker({
            dateLimit: 31,
            inquireBtn: '#inquireClickThree'
        });
        $("#groupSelectTwo").bind("click", showMenuContent);
        //导出
        $("#exportAlarmTwo").bind("click", overspeedVehicle.exportAlarm);
        $("#refreshTableTwo").bind("click", overspeedVehicle.refreshTable);


        /***车辆调度明细表***/
        vehicleDetail.init();

        $("#groupSelectThree").bind("click", showMenuContent);
        //导出
        $("#exportAlarmThree").bind("click", vehicleDetail.exportAlarm);
        $("#refreshTableThree").bind("click", vehicleDetail.refreshTable);


        // 模糊查询
        $('#simpleQueryParamTwo').keyup(function (event) {
            if (event.keyCode == 13) {
                $("#search_buttonTwo").click();
            };
        });
        $('#simpleQueryParamThree').keyup(function (event) {
            if (event.keyCode == 13) {
                $("#search_buttonThree").click();
            };
        });
        $("#groupSelect").on('input propertychange', function (value) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.checkAllNodes(false);
            search_ztree('treeDemo', 'groupSelect', 'group');
        });
        /**
         * 监控对象树模糊查询
         */
        var inputChange2;
        $("#groupSelectTwo").on('input propertychange', function (value) {
            if (inputChange2 !== undefined) {
                clearTimeout(inputChange2);
            };
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                isSearch2 = true;
            };
            inputChange2 = setTimeout(function () {
                if (isSearch2) {
                    var param = $("#groupSelectTwo").val();
                    vehicleDetail.searchVehicleTree2(param);
                }
                isSearch2 = true;
            }, 500);
        });
        var inputChange3;
        $("#groupSelectThree").on('input oninput', function (value) {
            if (inputChange3 !== undefined) {
                clearTimeout(inputChange3);
            };
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                isSearch3 = true;
            };
            inputChange3 = setTimeout(function () {
                if (isSearch3) {
                    var param = $("#groupSelectThree").val();
                    vehicleDetail.searchVehicleTree3(param);
                }
                isSearch3 = true;
            }, 500);
        });
        $('#queryType').on('change', function () {
            var param = $("#groupSelectTwo").val();
            vehicleDetail.searchVehicleTree2(param);
        });
        $('#queryType1').on('change', function () {
            var param = $("#groupSelectThree").val();
            vehicleDetail.searchVehicleTree3(param);
        });

    })
}(window, $))