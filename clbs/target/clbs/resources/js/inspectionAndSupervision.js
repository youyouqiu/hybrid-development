(function (window, $) {
    var myTable;
    //车辆id列表
    var vehicleId = [];
    var myTable;
    var startLoc = [];
    //开始时间
    var startTime;
    //结束时间
    var endTime;
    var gloabletype = 0;
    var gloableMsg = {};

    //组织树刚进入页面小于100自动勾选
    var ifAllCheck = true;
    var groupId = '';
    var checkFlag = false;

    var alarmTypeList = []

    var dbValue = false //树双击判断参数
    inspectionAndSupervision = {
        init: function () {
            //组织树
            var setting = {
                async: {
                    url: inspectionAndSupervision.getParkingReportTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "type": "multiple",
                        "icoType": "0"
                    },
                    dataFilter: inspectionAndSupervision.ajaxDataFilter
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
                    beforeClick: inspectionAndSupervision.beforeClickVehicle,
                    onAsyncSuccess: inspectionAndSupervision.zTreeOnAsyncSuccess,
                    onCheck: inspectionAndSupervision.onCheckVehicle,
                    beforeCheck: inspectionAndSupervision.zTreeBeforeCheck,
                    onDblClick: inspectionAndSupervision.onDblClickVehicle
                    // onExpand: inspectionAndSupervision.zTreeOnExpand
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },
        onDblClickVehicle: function (e, treeId, treeNode) {
            if (!treeNode) return
            dbValue = true
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.checkAllNodes(false);
            inspectionAndSupervision.getCheckedNodes("treeDemo");
            // $("#groupSelect").val(treeNode.name);
            // search_ztree('treeDemo', 'groupSelect', 'group');
            treeObj.checkNode(treeNode, !treeNode.checked, false, true);
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked && !dbValue) {
                var zTree = $.fn.zTree.getZTreeObj(treeId);
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
        getParkingReportTreeUrl: function (treeId, treeNode) {
            if (treeId == 'treeDemo') {
                return "/clbs/m/basicinfo/enterprise/professionals/tree";
            }
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
            if (size <= GROUP_MAX_CHECK && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            inspectionAndSupervision.getCharSelect(treeObj);
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            dbValue = false
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                zTree.expandNode(treeNode, true, true, false, false); // 展开节点
                setTimeout(() => {
                    inspectionAndSupervision.getCheckedNodes("treeDemo");
                    inspectionAndSupervision.validates();
                }, 600);
            }
            inspectionAndSupervision.getCheckedNodes("treeDemo");
            inspectionAndSupervision.getCharSelect(zTree);
        },
        /*zTreeOnExpand: function (event, treeId, treeNode) {
            //判断是否是勾选操作展开的树(是则继续执行，不是则返回)
            if (treeNode.type == "group" && !checkFlag) {
                return;
            }
            //初始化勾选操作判断表示
            checkFlag = false;
        },*/
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
                tMonth = inspectionAndSupervision.doHandleMonth(tMonth + 1);
                tDate = inspectionAndSupervision.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " " +
                    "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 *
                    parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = inspectionAndSupervision.doHandleMonth(endMonth + 1);
                endDate = inspectionAndSupervision.doHandleMonth(endDate);
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
                vMonth = inspectionAndSupervision.doHandleMonth(vMonth + 1);
                vDate = inspectionAndSupervision.doHandleMonth(vDate);
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
                    vendMonth = inspectionAndSupervision.doHandleMonth(vendMonth + 1);
                    vendDate = inspectionAndSupervision.doHandleMonth(vendDate);
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
                inspectionAndSupervision.getsTheCurrentTime();
            } else if (number == -1) {
                inspectionAndSupervision.startDay(-1)
            } else if (number == -3) {
                inspectionAndSupervision.startDay(-3)
            } else if (number == -7) {
                inspectionAndSupervision.startDay(-7)
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

            if (!inspectionAndSupervision.validates()) {
                return;
            }
            inspectionAndSupervision.getCheckedNodes('treeDemo');
            var url = "/clbs/m/reportManagement/inspectionAndSupervision/getList";
            var parameter = {
                "groupIds": groupId,
                "type": $('#businessType').val(),
                "status": $("#status").val(),
                "startTime": startTime,
                "endTime": endTime
            };
            json_ajax("POST", url, "json", true, parameter, inspectionAndSupervision.getCallback);
        },
        exportAlarm: function () {
            if (getRecordsNum() > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var url = "/clbs/m/reportManagement/inspectionAndSupervision/export";
            window.location.href = url;
        },
        validates: function () {
            return $("#speedlist").validate({
                rules: {
                    startTime: {
                        required: true
                    },
                    endTime: {
                        required: true,
                        compareDate: "#timeInterval"
                    },
                    groupSelect: {
                        zTreeCheckGroup: "treeDemo"
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
                        zTreeCheckGroup: vehicleSelectGroup,
                    },
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
        getCallback: function (data) {
            if (data.success == true) {
                dataListArray = []; //用来储存显示数据
                if (data.obj != null && data.obj.length != 0) {
                    $("#exportAlarm").removeAttr("disabled");
                    var alarm = data.obj;
                    for (var i = 0; i < alarm.length; i++) {
                        var infoContent = alarm[i].infoContent;
                        var warnType = inspectionAndSupervision.getAlarmType(alarm[i].warnType, alarm[i].alarmType);
                        var content = infoContent ? infoContent : warnType;
                        if (alarm[i].speed != '' && alarm[i].speed != null) {
                            alarm[i].speed = parseFloat(alarm[i].speed);
                        }
                        var dateList = [
                            i + 1,
                            alarm[i].groupName,
                            alarm[i].brand,
                            alarm[i].platformName,
                            inspectionAndSupervision.handleType(alarm[i].type),
                            inspectionAndSupervision.handleHtml(alarm[i].time),
                            content,
                            inspectionAndSupervision.resoveEvent(alarm[i].result),
                            alarm[i].ackTime,
                            alarm[i].dealer,
                            alarm[i].ackContent
                        ];
                        dataListArray.push(dateList);
                    }
                    inspectionAndSupervision.reloadData(dataListArray);
                } else {
                    inspectionAndSupervision.reloadData(dataListArray);
                    $("#simpleQueryParam").val("");
                    $("#exportAlarm").attr("disabled", "disabled");
                }
            } else {
                if (data.msg != null) {
                    layer.msg(data.msg, {
                        move: false
                    });
                }
            }
        },
        resoveEvent: function (res) {
            if (res == '0') {
                return "未处理"
            }
            if (res == '1') {
                return '已处理';
            }
            if (res == '2') {
                return '已过期';
            }
        },
        validatesQueryPost: function () {
            return $('#queryForm').validate({
                rules: {
                    answer: {
                        required: true,
                        maxlength: 20
                    }
                },
                messages: {
                    answer: {
                        required: "请输入应答内容",
                        maxlength: '输入范围1-20位'
                    }
                }
            }).form()
        },
        // 转换报警类型
        getAlarmType: function (warnType, alarmType) {
            if (warnType == null && alarmType == null) {
                return '';
            }
            var typeName;
            if (alarmType == null) {
                typeName = inspectionAndSupervision.getAlarmTypeNameByWarnType(Number(warnType))
            } else {
                var forwardAlarmNameElement = alarmTypeList.find(function (item) {
                    try {
                        var posArr = item.pos.split(",");
                        return posArr.indexOf(alarmType) !== -1
                    } catch (e) {}
                });
                typeName = forwardAlarmNameElement == null || forwardAlarmNameElement.name == null ?
                    gangSupervision.getAlarmType(Number(warnType)) : forwardAlarmNameElement.name;
            }
            return typeName
        },
        getAlarmTypeNameByWarnType: function (warnType) {
            var alarmType;
            switch (warnType) {
                case 0:
                    alarmType = "未知报警类型";
                    break;
                case 1:
                    alarmType = '超速报警';
                    break;
                case 2:
                    alarmType = '疲劳驾驶报警';
                    break;
                case 3:
                    alarmType = '紧急报警';
                    break;
                case 4:
                    alarmType = '进入指定区域报警';
                    break;
                case 5:
                    alarmType = '离开指定区域报警';
                    break;
                case 6:
                    alarmType = '路段堵塞报警';
                    break;
                case 7:
                    alarmType = '危险路段报警';
                    break;
                case 8:
                    alarmType = '越界报警';
                    break;
                case 9:
                    alarmType = '盗警';
                    break;
                case 10:
                    alarmType = '劫警';
                    break;
                case 11:
                    alarmType = '偏离路线报警';
                    break;
                case 12:
                    alarmType = '车辆移动报警';
                    break;
                case 13:
                    alarmType = '超时驾驶报警';
                    break;
                default:
                    alarmType = '其他报警';
            }
            return alarmType;
        },
        // 车辆颜色
        getColor: function (type) {
            var color = '';
            switch (type) {
                case 1:
                    color = '蓝色';
                    break;
                case 2:
                    color = '黄色';
                    break;
                case 3:
                    color = '黑色';
                    break;
                case 4:
                    color = '白色';
                    break;
                case 5:
                    color = '绿色';
                    break;
                case 9:
                    color = '其他';
                    break;
                default:
                    color = '其他';
            }
            return color;
        },
        handleType: function (type) {
            var type = parseInt(type)
            //计数
            var text = '';
            switch (type) {
                case 0:
                    text = 'JTT平台查岗';
                    break;
                case 1:
                    text = 'JTT报警督办';
                    break;
                case 2:
                    text = '西藏企业查岗';
                    break;
                case 3:
                    text = '西藏企业督办';
                    break;
                default:
                    break;
            }
            return text;
        },
        handleHtml: function (data) {
            return data ? data : '';
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
                    "targets": [0, 2, 3, 4, 5, 6, 7],
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
            var currentPage = myTable.page();
            inspectionAndSupervision.inquireClick(1);
            setTimeout(function () {
                myTable.page(currentPage).draw(false);
            }, 100);
        },
        saveSupervisory: function (answer) {
            /*if($('#alarmResult').val() == '-1'){
                layer.msg('请选择处理结果');
                return;
            }*/

            if (gloabletype == '1') {
                // 标准平台督办
                var param = {
                    brand: gloableMsg.data.msgBody.vehicleNo,
                    objectType: gloableMsg.data.msgHead.protocolType,
                    serverIp: gloableMsg.data.msgHead.serverIp,
                    msgGNSSCenterId: gloableMsg.data.msgHead.msgGNSSCenterId,
                    alarmHandle: answer,
                    supervisionId: gloableMsg.data.msgBody.data.supervisionId,
                    warnTime: gloableMsg.data.msgBody.data.warnTime,
                    alarmMsgId: gloableMsg.data.msgHead.handleId,
                    objectType: gloableMsg.data.msgBody.dataType,
                }
                json_ajax('post', '/clbs/m/connectionparamsset/platformAlarmAck', 'json', true, param, inspectionAndSupervision.dealCallBack)
            }

            if (gloabletype == '3') {
                // 西藏督办
                var param = {
                    infoId: gloableMsg.data.msgBody.data.infoId,
                    msgGNSSCenterId: gloableMsg.data.msgHead.msgGNSSCenterId,
                    result: answer,
                    serverIp: gloableMsg.data.msgHead.serverIp,
                    msgId: gloableMsg.data.msgHead.handleId,
                }
                //标准平台查岗
                json_ajax('post', '/clbs/m/connectionparamsset/extendPlatformAlarmAck', 'json', true, param, inspectionAndSupervision.dealCallBack)
            }
        },
        dealCallBack: function (data) {
            if (data.success) {
                var result = data.obj.handleStatus;
                var msg = '';
                switch (result) {
                    case 0:
                        msg = '已处理';
                        break;
                    case 1:
                        msg = '消息已应答';
                        break;
                    case 2:
                        msg = '督办消息已过期';
                        break;
                    case 3:
                        msg = '未查询到报警信息';
                        break;
                    case 4:
                        msg = '未查询到上级平台督办记录';
                    default:
                        msg = '已处理';
                        break;
                }
                if ($(".normalType").is(":hidden") && result == 4) {
                    msg = '未查询到上级平台督办记录';
                }
                layer.msg(msg);
                $('#alarmSupervise').modal('hide');
                inspectionAndSupervision.refreshTable();
            } else if (data.msg) {
                layer.msg(data.msg);
            }
        },
        saveQueryPost: function () {
            if (inspectionAndSupervision.validatesQueryPost()) {
                var url = "/clbs/m/connectionparamsset/checkGroup";
                var groupId = gloableMsg.data.msgHead.groupId;
                var objectType = gloableMsg.data.msgBody.data.objectType; // 查岗对象类型
                if (objectType == 3) { // 查岗对象类型为3(下级平台所属所有业户)
                    // 检查应答用户的所属企业是否有经营许可证号
                    json_ajax("POST", url, "json", false, {
                        "groupId": groupId
                    }, function (data) {
                        if (data.success) {
                            inspectionAndSupervision.reportAckGang();
                        } else {
                            layer.msg("企业的经营许可证号不能为空，请先完善企业信息");
                        }
                    });
                } else {
                    inspectionAndSupervision.reportAckGang();
                }
            }
        },
        reportAckGang: function () {
            var answer = $('#answer1').val();
            var param = {
                infoId: gloableMsg.data.msgBody.data.infoId,
                msgDataType: gloableMsg.data.msgBody.dataType,
                answer: answer,
                objectType: gloableMsg.data.msgBody.data.objectType,
                objectId: gloableMsg.data.msgBody.data.objectId,
                serverIp: gloableMsg.data.msgHead.serverIp,
                msgGNSSCenterId: gloableMsg.data.msgHead.msgGNSSCenterId,
                groupId: gloableMsg.data.msgHead.groupId,
                gangId: gloableMsg.data.msgHead.handleId,
            };
            //平台查岗
            json_ajax('post', '/clbs/m/connectionparamsset/platformGangAck', 'json', true, param, function (data) {
                if (data.success) {
                    var result = data.obj.handleStatus;
                    var msg = '';
                    switch (result) {
                        case 0:
                            msg = '已处理';
                            break;
                        case 1:
                            msg = '消息已应答';
                            break;
                        case 2:
                            msg = '查岗消息已过期';
                            break;
                        case 3:
                            msg = "未查询到查岗记录";
                            break;
                        default:
                            msg = '未查询到查岗记录';
                            break;
                    }
                    layer.msg(msg);
                    $('#queryPost').modal('hide');
                    inspectionAndSupervision.refreshTable();
                }
            })
        }
    };
    $(function () {
        inspectionAndSupervision.init();
        $("#groupSelect").bind("click", showMenuContent);

        json_ajax('post', '/clbs/m/monitorForwardingAlarmSearch/alarmType', 'json', true, null, function (data) {
            var temp
            if (data) {
                var temp = data.replaceAll(/\\/g, '')
                alarmTypeList = JSON.parse(temp)
                console.log(alarmTypeList)
            }

        })

        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                search_ztree('treeDemo', 'groupSelect', 'group');
                treeObj.checkAllNodes(false);
            }
        });
        /**
         * 组织树模糊查询
         */
        $("#groupSelect").on('input propertychange', function (value) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.checkAllNodes(false);
            search_ztree('treeDemo', 'groupSelect', 'group');
        });

        //动态显示查询条件
        var gangType = $.getUrlParam('gangType');
        if (gangType) {
            $("#businessType").val(gangType);
            $("#status").val('1');
            inspectionAndSupervision.inquireClick(0);
        }

        //初始化页面
        inspectionAndSupervision.getTable('#dataTable');

        //当前时间
        inspectionAndSupervision.getsTheCurrentTime();
        $('#timeInterval').dateRangePicker({
            dateLimit: 31
        });
        //导出
        $("#exportAlarm").bind("click", inspectionAndSupervision.exportAlarm);
        $("#refreshTable").bind("click", inspectionAndSupervision.refreshTable);

        $('#saveSupervisory').bind("click", inspectionAndSupervision.saveSupervisory);

        $('#saveQueryPost').bind("click", inspectionAndSupervision.saveQueryPost);

        $('#queryPost').on('hidden.bs.modal', function (e) {
            // do something...
            $('#answer-error').hide();
        })

    })
}(window, $))