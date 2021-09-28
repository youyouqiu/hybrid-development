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

    var alarmTypeList = []
    gangSupervision = {
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
                tMonth = gangSupervision.doHandleMonth(tMonth + 1);
                tDate = gangSupervision.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " "
                    + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24
                    * parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = gangSupervision.doHandleMonth(endMonth + 1);
                endDate = gangSupervision.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate + " "
                    + "23:59:59";
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = gangSupervision.doHandleMonth(vMonth + 1);
                vDate = gangSupervision.doHandleMonth(vDate);
                startTime = vYear + "-" + vMonth + "-" + vDate + " "
                    + "00:00:00";
                if (day == 1) {
                    endTime = vYear + "-" + vMonth + "-" + vDate + " "
                        + "23:59:59";
                } else {
                    var endNum = -1;
                    var vendtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * parseInt(endNum);
                    var dateEnd = new Date();
                    dateEnd.setTime(vendtoday_milliseconds);
                    var vendYear = dateEnd.getFullYear();
                    var vendMonth = dateEnd.getMonth();
                    var vendDate = dateEnd.getDate();
                    vendMonth = gangSupervision.doHandleMonth(vendMonth + 1);
                    vendDate = gangSupervision.doHandleMonth(vendDate);
                    endTime = vendYear + "-" + vendMonth + "-" + vendDate + " "
                        + "23:59:59";
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
            startTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate()) + " " + "00:00:00";
            endTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate())
                + " "
                + ("23")
                + ":"
                + ("59")
                + ":"
                + ("59");
            var atime = $("#atime").val();
            if (atime != undefined && atime != "") {
                startTime = atime;
            }
        },
        inquireClick: function (number) {
            if (number == 0) {
                gangSupervision.getsTheCurrentTime();
            } else if (number == -1) {
                gangSupervision.startDay(-1)
            } else if (number == -3) {
                gangSupervision.startDay(-3)
            } else if (number == -7) {
                gangSupervision.startDay(-7)
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
            if (!gangSupervision.validates()) {
                return;
            }
            var url = "/clbs/m/reportManagement/gangSupervisionReport/getTheDayAllMsg";
            var parameter = {
                "type": $('#businessType').val(),
                "status": $("#status").val(),
                "startTime": startTime,
                "endTime": endTime
            };
            json_ajax("GET", url, "json", true, parameter, gangSupervision.getCallback);
        },
        exportAlarm: function () {
            if(getRecordsNum() > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var url = "/clbs/sx/sxReportManagement/sxSpeedViolationReport/export";
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
                },
                messages: {
                    startTime: {
                        required: "请选择开始日期！"
                    },
                    endTime: {
                        required: "请选择结束时间！",
                        compareDate: endtimeComStarttime
                    },
                }
            }).form();
        },
        unique: function (arr) {
            var result = [], hash = {};
            for (var i = 0, elem; (elem = arr[i]) != null; i++) {
                if (!hash[elem]) {
                    result.push(elem);
                    hash[elem] = true;
                }
            }
            return result;
        },
        getCallback: function (data) {
            if (data.success == true) {
                dataListArray = [];//用来储存显示数据
                if (data.obj != null && data.obj.length != 0) {
                    $("#exportAlarm").removeAttr("disabled");
                    var alarm = data.obj;
                    // startLoc = [];
                    for (var i = 0; i < alarm.length; i++) {
                        var infoContent = alarm[i].infoContent;
                        var warnType = gangSupervision.getAlarmType(alarm[i].warnType, alarm[i].alarmType);
                        var content = infoContent ? infoContent : warnType;
                        if (alarm[i].speed != '' && alarm[i].speed != null) {
                            alarm[i].speed = parseFloat(alarm[i].speed);
                        }
                        var dateList =
                            [
                                i + 1,
                                gangSupervision.resoveEvent(alarm[i].result, alarm[i].type, alarm[i]),
                                gangSupervision.handleType(alarm[i].type),
                                gangSupervision.handleHtml(alarm[i].time),
                                content,
                                alarm[i].ackTime,
                                alarm[i].dealer,
                                alarm[i].ackContent
                            ];
                        dataListArray.push(dateList);
                        // startLoc.push(alarm[i].startLocation);
                    }
                    gangSupervision.reloadData(dataListArray);
                } else {
                    gangSupervision.reloadData(dataListArray);
                    $("#simpleQueryParam").val("");
                    $("#search_button").click();
                    $("#exportAlarm").attr("disabled", "disabled");
                }
            } else {
                if (data.msg != null) {
                    layer.msg(data.msg, {move: false});
                }
            }
        },
        resoveEvent: function (res, type, msg) {
            var msg = JSON.stringify(msg);
            if (res == '0') {
                return "<button  onclick='gangSupervision.resoveEventModel(" + res + "," + type + "," + msg + ")'  type='button' class='editBtn editBtn-info'><i class='fa fa-pencil'></i>处理</button>"
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
                    answer:{
                        required: true,
                        maxlength : 20
                    }
                },
                messages: {
                    answer:{
                        required: "请输入应答内容",
                        maxlength: '输入范围1-20位'
                    }
                }
            }).form()
        },
        // 转换报警类型
        getAlarmType: function (warnType, alarmType) {
            if(warnType == null && alarmType == null){
                return  '';
            }
            var typeName;
            if (alarmType == null) {
                typeName = gangSupervision.getAlarmTypeNameByWarnType(Number(warnType))
            } else {
                var forwardAlarmNameElement = alarmTypeList.find(function (item) {
                    try {
                        var posArr = item.pos.split(",");
                        return posArr.indexOf(alarmType) !== -1
                    } catch (e) {
                    }
                });
                typeName = forwardAlarmNameElement == null || forwardAlarmNameElement.name == null
                    ? gangSupervision.getAlarmType(Number(warnType)) : forwardAlarmNameElement.name;
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
        resoveEventModel: function (res, type, msg) {
            gloabletype = type;
            gloableMsg = msg;
            console.log(msg, 'msg');
            if (type == '0' || type == '2') {
                $("#answer1").val('');
                $("#answer1-error").hide();
                // 标准809查岗
                if (type == '0') {
                    $('#postTitle').html('处理JTT平台查岗');
                    $('.xzInfo').hide();
                    $('#infoContent1').val(msg.infoContent);
                    $('#platformName2').text(msg.platformName);
                }

                // 西藏809查岗
                if (type == '2') {
                    $('#postTitle').html('处理西藏企业查岗');
                    $('.xzInfo').show();
                    $('#xzGroupName').val(msg.enterprise);
                    $('#xzUserName').val(msg.supervisor);
                    $('#infoContent1').val(msg.infoContent);
                    $('#platformName2').text(msg.platformName);
                }
                $('#queryPost').modal('show');
            }

            if (type == '1' || type == '3') {
                if (type == '1') {
                    $('.normalType').show();
                    $('.normalName').show();
                    $('.xzName').hide();
                    $('.xzInfo').hide();

                    $('#brand').val(msg.brand);
                    $('#brandColor').val(gangSupervision.getColor(msg.plateColor));
                    $('#gangAlarmType').val(gangSupervision.getAlarmType(msg.warnType, msg.alarmType));
                    $('#alarmTime1').val(formatDateAll(msg.warnTime * 1000));
                    var warnSrc = '';
                    switch (msg.warnSrc) {
                        case 1:
                            warnSrc = '车载终端';
                            break;
                        case 2:
                            warnSrc = '企业监控平台';
                            break;
                        case 3:
                            warnSrc = '政府监管平台';
                            break;
                        case 9:
                            warnSrc = '其他';
                            break;
                        default:
                            warnSrc = '其他';
                            break;
                    }
                    $('#alarmInfoSourve').val(warnSrc);
                    $('#supervisoryLevel').val(msg.supervisionLevel == 0 ? '紧急' : '一般');
                    $('#supervisoryEndTime').val(msg.expireTime);
                    $('#supervisoryPeople').val(msg.supervisor);
                    $('#supervisoryPhone').val(msg.supervisionTel);
                    $('#supervisoryEmail').val(msg.supervisionEmail);

                    // $('#supervisoryForm').attr('action','/clbs/m/connectionparamsset/platformAlarmAck');
                    $('#alarmSuperviseTit').html('处理JTT报警督办');

                    $('.xzInfo').hide();
                }
                if (type == '3') {
                    $('.normalType').hide();
                    $('.normalName').hide();
                    $('.xzName').show();
                    $('.xzInfo').show();

                    $('#alarmInfoSourve').val(msg.enterprise);
                    $('#supervisoryLevel').val(msg.level == 0 ? '紧急' : '一般');
                    $('#supervisoryEndTime').val(msg.expireTime);
                    $('#supervisoryPeople').val(msg.supervisor);
                    $('#supervisoryPhone').val(msg.supervisionTel);
                    $('#supervisoryEmail').val(msg.supervisionEmail);
                    $('#supervisoryCont').val(msg.infoContent);

                    $('#alarmSuperviseTit').html('处理西藏企业督办')
                }


                $('#alarmSupervise').modal('show');
            }
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
                "lengthChange": true,// 是否允许用户自定义显示数量
                "bPaginate": true, // 翻页功能
                "bFilter": false, // 列筛序功能
                "searching": true,// 本地搜索
                "ordering": false, // 排序功能
                "Info": true,// 页脚信息
                "autoWidth": true,// 自动宽度
                "stripeClasses": [],
                "pageLength": 10,
                "lengthMenu": [5,10, 20, 50, 100, 200],
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
                    "targets": [0, 2, 3, 4, 5, 6, 7],
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
                myTable.column(1).search(tsval, false, false).draw();
            });
        },
        reloadData: function (dataList) {
            var currentPage = myTable.page();
            myTable.clear();
            myTable.rows.add(dataList);
            myTable.page(currentPage).draw(false);
            myTable.columns(1).search('', false, false).page(currentPage).draw();
        },
        //刷新列表
        refreshTable: function () {
            var currentPage = myTable.page();
            gangSupervision.inquireClick(1);
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
                var protocolType = gloableMsg.protocolType; // 协议类型
                var sourceDataType;
                var sourceMsgSn;
                if (protocolType == 100) {
                    // 源子业务类型
                    sourceDataType = gloableMsg.sourceDataType;
                    // 源报文序列号(相当于809-2011版本督办id)
                    sourceMsgSn = gloableMsg.sourceMsgSn;
                } else {
                    // 源子业务类型
                    sourceDataType = 5122;
                    sourceMsgSn = gloableMsg.supervisionId;
                }

                // 标准平台督办
                var param = {
                    monitorId:gloableMsg.monitorId,
                    brand: gloableMsg.vehicleNo,
                    vehicleColor:gloableMsg.vehicleColor,
                    msgSn:gloableMsg.msgSn,
                    sourceDataType:sourceDataType,
                    sourceMsgSn:sourceMsgSn,
                    warnTime: gloableMsg.warnTime,
                    alarmMsgId: gloableMsg.handleId,
                    alarmHandle: answer,
                    alarmStartTime: gloableMsg.alarmStartTime,
                    plateFormId: gloableMsg.platformId,
                    alarmType: gloableMsg.alarmType
                }

                var eventId = gloableMsg.eventId;
                if(eventId){
                    param = $.extend({},param,{
                        "eventId": eventId,
                    });
                }

                json_ajax('post', '/clbs/m/connectionparamsset/platformAlarmAck', 'json', true, param, gangSupervision.dealCallBack)
            }

            if (gloabletype == '3') {
                // 西藏督办
                var param = {
                    infoId: gloableMsg.infoId,
                    msgGNSSCenterId: gloableMsg.msgGnssCenterId,
                    result: answer,
                    serverIp: gloableMsg.serverIp,
                    msgId: gloableMsg.handleId,
                }

                //标准平台查岗
                json_ajax('post', '/clbs/m/connectionparamsset/extendPlatformAlarmAck', 'json', true, param, gangSupervision.dealCallBack)
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
                gangSupervision.refreshTable();
            } else if (data.msg) {
                layer.msg(data.msg);
            }
        },
        saveQueryPost: function () {
            if (gangSupervision.validatesQueryPost()) {
                var url = "/clbs/m/connectionparamsset/checkGroup";
                var groupId = gloableMsg.groupId;
                var objectType = gloableMsg.objectType; // 查岗对象类型
                if (objectType == 3) { // 查岗对象类型为3(下级平台所属所有业户)
                    // 检查应答用户的所属企业是否有经营许可证号
                    json_ajax("POST", url, "json", false, {"groupId": groupId}, function (data) {
                        if (data.success) {
                            gangSupervision.reportAckGang();
                        } else {
                            layer.msg("企业的经营许可证号不能为空，请先完善企业信息");
                        }
                    });
                } else {
                    gangSupervision.reportAckGang();
                }
            }
        },
        reportAckGang:function () {
            var answer = $('#answer1').val();
            var param = {
                infoId: gloableMsg.infoId,
                msgDataType: gloableMsg.dataType,
                answer: answer,
                objectType: gloableMsg.objectType,
                objectId: gloableMsg.objectId,
                serverIp: gloableMsg.serverIp,
                msgGNSSCenterId: gloableMsg.msgGnssCenterId,
                groupId: gloableMsg.groupId,
                gangId: gloableMsg.handleId,
                msgID: gloableMsg.msgId,
                msgSn: gloableMsg.msgSn,
                platFormId:gloableMsg.platformId
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
                    gangSupervision.refreshTable();
                }
            })
        }
    };
    $(function () {
        $('input').inputClear();

        json_ajax('post', '/clbs/m/monitorForwardingAlarmSearch/alarmType', 'json', true, null, function (data) {
            var temp
            if(data){
                var temp = data.replaceAll(/\\/g,'')
                alarmTypeList = JSON.parse(temp)
                console.log(alarmTypeList)
            }

        })
        //动态显示查询条件
        var gangType = $.getUrlParam('gangType');
        if (gangType) {
            $("#businessType").val(gangType);
            $("#status").val('1');
            gangSupervision.inquireClick(0);
            /*switch (gangType) {
                case '11':
                case '12':
                    $("#businessType").val('1');
                    break;
                case '21':
                case '22':
                    $("#businessType").val('2');
                    break;
                default:
                    $("#businessType").val('0');
                    break;
            }*/
        }

        //初始化页面
        gangSupervision.getTable('#dataTable');

        //当前时间
        gangSupervision.getsTheCurrentTime();
        $('#timeInterval').dateRangePicker({
            dateLimit: 7
        });
        //导出
        $("#exportAlarm").bind("click", gangSupervision.exportAlarm);
        $("#refreshTable").bind("click", gangSupervision.refreshTable);

        $('#saveSupervisory').bind("click", gangSupervision.saveSupervisory);

        $('#saveQueryPost').bind("click", gangSupervision.saveQueryPost);

        $('#queryPost').on('hidden.bs.modal', function (e) {
            // do something...
            $('#answer-error').hide();
        })

    })
}(window, $))