// (function (window, $) {
var handleTime = null;
var videoFreeTime = null;
var numFlag = false; //用于判断顶部是否有置顶消息
var newChatTime = null; // 讨论组消息显示定时器
var TREE_MAX_CHILDREN_LENGTH = $('#maxNumberAssignmentMonitor').val() ? $('#maxNumberAssignmentMonitor').val() : 500;
var MAX_SUBSCRIBTION_LENGTH = 400;
localStorage.setItem('treeMaxSelect', TREE_MAX_CHILDREN_LENGTH);
var assignmentMaxCarNum = '下的监控对象数已经达到上限' + TREE_MAX_CHILDREN_LENGTH;
var alarmTimer = null; // 全局报警请求定时器
var MAX_MENU_LENGTH = 20;
var answerTimer = null; // 巡检应答倒计时
var initAnswer = true;
var forwardAlarmName;
var GROUP_MAX_CHECK = 500; // 控制企业组织树最多可勾选数量
var emailVoiceFlag = $('#userName').text() + '_mailVoice'
var alarmVoiceFlag = $('#userName').text() + '_alarmVoice'
var alarmUrlType = 9;
var PlatformAlarmUrl = '';
var specialAlarmUrl = '';
var PlatformAlarmUrl3 = '';
var msgBoxType = false;

/**
 * 导出管理相关变量
 * */
var downloadTable;

//显示错误提示信息
function showErrorMsg(msg, inputId) {
    if ($("#_error_label").is(":hidden")) {
        $("#_error_label").text(msg);
        $("#_error_label").insertAfter($("#" + inputId));
        $("#_error_label").show();
    } else {
        $("#_error_label").is(":hidden");
    }
}

var allAlarmList = [];
var alarmSetId = [];
var userName = $("#userName");
var leftToolTip = $("#toggle-left-tooltip");
var profile = $("#profile").val();
// 邮件声音
var handleMailClick = function () {
    var oldValue = localStorage.getItem(emailVoiceFlag)
    if ( oldValue == 'true' ) {
        $('#voiceImgSwitch').attr('src', '/clbs/resources/img/mute.png')
        localStorage.setItem(emailVoiceFlag, 'false')
        voiceControl(1, false)
    } else {
        $('#voiceImgSwitch').attr('src', '/clbs/resources/img/voiceOpen.png')
        localStorage.setItem(emailVoiceFlag, 'true');
    }
}
// 报警声音
var handleAlarmClick = function () {
    var oldValue = localStorage.getItem(alarmVoiceFlag)
    if (oldValue == 'true') {
        $('#emaliVoiceImg').attr('src', '/clbs/resources/img/mute.png')
        localStorage.setItem(alarmVoiceFlag, 'false')
        voiceControl(2, false)
    } else {
        $('#emaliVoiceImg').attr('src', '/clbs/resources/img/voiceOpen.png')
        localStorage.setItem(alarmVoiceFlag, 'true');
    }
}
// 初始化声音选项
var initVoice = function () {
    var oldMailValue = localStorage.getItem(emailVoiceFlag)
    var oldAlarmValue = localStorage.getItem(alarmVoiceFlag)
    if( oldMailValue === null || oldMailValue == 'true') {
        localStorage.setItem(emailVoiceFlag, 'true')
        $('#voiceImgSwitch').attr('src', '/clbs/resources/img/voiceOpen.png')
    }else {
        $('#voiceImgSwitch').attr('src', '/clbs/resources/img/mute.png')
    }
    if( oldAlarmValue === null || oldAlarmValue == 'true') {
        localStorage.setItem(alarmVoiceFlag, 'true')
        $('#emaliVoiceImg').attr('src', '/clbs/resources/img/voiceOpen.png')
    }else {
        $('#emaliVoiceImg').attr('src', '/clbs/resources/img/mute.png')
    }
}
/**
 * 报警声音控制
 * @param type number 1邮件声音 2全局报警声音
 * @param action boolean 开启/关闭
 */
var voiceControl = function (type, action) {
    var email = $("#noticeAutoOff"); // "嘟~"
    var alarm = $("#noticeIEalarmMsg"); // "您有报警信息，请及时处理"
    if( type == 1 ) {
        action ? email[0].play() : email[0].pause()
    }else if ( type == 2 ) {
        action ? alarm[0].play() : alarm[0].pause()
    }
}
pagesNav = {
    // 属性
    chatVisible: false,
    init: function () {
        $("#main-content").css("min-height", ($(window).height() - 126) + "px");
        if (userName.html() === "experience1") {
            $("#editPwd").remove();
        }
        if (typeof returnCitySN !== 'undefined') {
            var ip = returnCitySN.cip;
            $.cookie('ip', ip);
        }

        $("#toggle-left-button").on("mouseover", function () {
            leftToolTip.addClass("in");
            leftToolTip.css({
                "top": "31px",
                "left": "292px"
            })
        }).on("mouseout", function () {
            leftToolTip.removeClass("in");
        });
        webSocket.init('/clbs/ws?access_token=' + headers.access_token, headers, null, null, null, null);
        pagesNav.isConnectSocket();
        // var username = $("#userName").text();
        var setBtn = $("#noticeBtn1");
        /*if (username == "admin") {
          setBtn.show();
        } else {
          setBtn.hide();
        }*/
        pagesNav.isHealth();
        pagesNav.initChat();
        if (profile === "dev") {
            $(".user-nav ul").prepend("<li><label style=\"font-weight:normal\">" + $("#version").val() + "</label></li>");
        }

    },
    // socket连接建立
    isConnectSocket: function () {
        setTimeout(function () {
            if (webSocket.conFlag) {
                console.log(headers, 'headers');
                webSocket.subscribe(headers, "/user/topic/alarmGlobal", pagesNav.getintimeno, null, null);
                webSocket.subscribe(headers, "/user/topic/specialReport", pagesNav.specialAlarm, null, null);
                webSocket.subscribe(headers, '/user/topic/checkGlobal', pagesNav.updateTable, null, null);
                //809断线重连
                webSocket.subscribe(headers, '/user/topic/T809OfflineReconnect', pagesNav.update809MessageData, null, null);
                // 磁盘存储情况
                webSocket.subscribe(headers, '/topic/diskInfo', pagesNav.diskInfoCallBack, null, null);
                // 导出管理列表记录更新
                webSocket.subscribe(headers, '/user/topic/offlineExport', pagesNav.updateExportMananer, null, null);
                //信息配置、车辆管理、人员管理、物品管理、终端管理、Sim卡管理、从业人员管理、分组管理导入
                webSocket.subscribe(headers, '/user/topic/import/progress', pagesNav.updateImportMessage, null, null);
            } else {
                pagesNav.isConnectSocket();
            }
        }, 1000);
    },

    //导入
    updateImportMessage: function (data) {
        var body = JSON.parse(data.body);
        if (window.msgImport) { //信息配置导入
            window.msgImport.setProgressBar(body);
        }

        //车辆管理、人员管理、物品管理、终端管理、Sim卡管理、从业人员管理、分组管理
        if (window.importSocketFileV) {
            window.importSocketFileV.setProgressBar(body);
        }
    },

    // 磁盘阈值情况
    diskInfoCallBack: function (data) {
        var body = JSON.parse(data.body);
        // 更新视频操作时间
        var videoPlayTime = Number(body.videoPlayTime) * 1000;
        if (videoPlayTime !== handleTime) {
            handleTime = videoPlayTime;
        }

        var videoStopTime = Number(body.videoStopTime) * 1000;
        if (videoFreeTime !== videoStopTime) {}

        var maxValue = body.memoryRate;
        var memory = body.memory;
        if (maxValue <= memory) {
            $('#memoryWarningInfo').text('存储容量报警（空间已达' + memory + '%）');
        } else {
            $('#memoryWarningInfo').text('无任何信息');
        }
    },

    //心跳
    isHealth: function () {
        setTimeout(function () {
            if (webSocket.conFlag) {
                webSocket.subscribe(headers, "/user/topic/health", pagesNav.healthBack, "/app/isHealth", null);
            } else {
                pagesNav.isHealth();
            }
        }, 1000);
    },

    healthBack: function (data) {
        if (data.body === "isOk") {
            webSocket.send("/app/ackHealth", headers, userName.text());
        }
    },

    logout: function () {
        var url4 = "/clbs/out";
        $.ajax({
            type: "POST",
            url: url4,
            dataType: "json",
            async: false,
            data: null,
            timeout: 30000,
            success: function () {
                location.href = "/clbs/logout";
            },
            error: function () {
                location.href = "/clbs/logout";
            }
        });
    },
    getOverseeNum: function () {
        // var name = $("#userName").text();
        json_ajax("GET", "/clbs/m/reportManagement/gangSupervisionReport/getPlatformMsgNumber", "json", true, null, function (data) {
            if (!data.success) return;
            var result = data.obj;
            $('#jttNum').html(result.standard809GangNum);
            $('#xzNum').html(result.extend809GangNum);
            $('#jttAlarmNum').html(result.standard809AlarmNum);
            $('#xzAlarmNum').html(result.extend809AlarmNum);
            if (numFlag && result.standard809GangNum == 0 && result.extend809GangNum == 0 &&
                result.standard809AlarmNum == 0 && result.extend809AlarmNum == 0) {
                $("li.noInfo").show();
                $(".numOne").hide();
                $("#noticeRedIcon").hide();
                return;
            }
            $("li.noInfo").hide();
            $("#noticeRedIcon").show();
            if (result.standard809GangNum != 0) {
                $('.jttNum').show()
            }
            if (result.extend809GangNum != 0) {
                $('.xzNum').show()
            }
            if (result.standard809AlarmNum != 0) {
                $('.jttAlarmNum').show()
            }
            if (result.extend809AlarmNum != 0) {
                $('.xzAlarmNum').show()
            }
        })
    },
    //809断开连接提醒(报警声音)
    reconnectVoice: function (flag) {
        var voiceFlag = localStorage.getItem(emailVoiceFlag)
        if(voiceFlag !== 'true') return
        //声音
        if (navigator.userAgent.indexOf('MSIE') >= 0) {
            if (flag) {
                $("#noticeIEalarmMsg")[0].play();
            } else {
                $("#noticeIEalarmMsg")[0].pause();
            }
        } else {
            if (flag) {
                $("#noticeAutoOff")[0].play();
            } else {
                $("#noticeAutoOff")[0].pause();
            }
        }
    },
    gethistoryno: function (isCalledBySocketCallback) {
        var name = $("#userName").text();
        $.ajax({
            type: 'POST', //通常会用到两种：GET,POST。默认是：GET
            url: '/clbs/subscribe/global', //(默认: 当前页地址) 发送请求的地址
            contentType: 'application/x-www-form-urlencoded',
            dataType: 'json', //预期服务器返回的数据类型。"json"
            async: true, // 异步同步，true  false
            data: {
                "userName": name
            },
            timeout: 30000, //超时时间设置，单位毫秒
            success: function (data) {
                $("#menu2 li").hide();
                $("li.noInfo").show();
                if (data.success) {
                    allAlarmList = data.obj.result;
                    if (data.obj.result.length > 0) {
                        $("#alarmno").show();
                    } else {
                        $("#alarmno").hide();
                    }
                    alarmSetId = data.obj.result;
                    //获取运输证和行驶证满即将到期足要求的提醒条数
                    $("#menu2 li").hide();
                    $("li.noInfo").hide();
                    if (data.obj.expireInsuranceIdList == '0' && data.obj.expireRoadTransportList == '0' &&
                        data.obj.expireDrivingLicenseList == '0' && data.obj.expireMaintenanceList == '0' && data.obj.lifecycleExpireNumber == '0') {
                        $(".numTwo").hide();
                        numFlag = true;
                    }
                    // 809连接断开
                    if (data.obj.t809OfflineReconnectStatus == 'false') {
                        numFlag = false;
                        $('.conactInfo').show();
                        $('#noticeWaves').show();
                        if( localStorage.getItem(emailVoiceFlag) == "true" && isCalledBySocketCallback){
                            voiceControl(1, true)
                        }
                    } else {
                        if (numFlag) numFlag = true;
                        $('.conactInfo').hide();
                        $('#noticeWaves').hide();
                        pagesNav.reconnectVoice(false);
                    }
                    if (data.obj.lifecycleExpireNumber != 0) {
                        $(".fwNum").show();
                    }
                    if (data.obj.expireMaintenanceList != 0) {
                        $(".byNum").show();
                    }
                    if (data.obj.expireInsuranceIdList != 0) {
                        $(".bxNum").show();
                    }
                    if (data.obj.expireRoadTransportList != 0) {
                        $(".yszNum").show();
                    }
                    if (data.obj.expireDrivingLicenseList != 0) {
                        $(".xszNum").show();
                    }
                    $("#fwNum").text(data.obj.lifecycleExpireNumber);
                    $("#byNum").text(data.obj.expireMaintenanceList);
                    $("#bxNum").text(data.obj.expireInsuranceIdList);
                    $("#yszNum").text(data.obj.expireRoadTransportList);
                    $("#xszNum").text(data.obj.expireDrivingLicenseList);
                    pagesNav.getOverseeNum();
                    pagesNav.subscribeStatusAlarm();
                }
            }, //请求成功
        });
    },
    subscribeStatusAlarm: function () {
        setTimeout(function () {
            if (webSocket.conFlag) {
                webSocket.send('/app/vehicle/subscribeStatus', headers, null);
            } else {
                pagesNav.subscribeStatusAlarm();
            }
        }, 500);
    },
    getintimeno: function (data) {
        var json = JSON.parse(data.body);
        var msgID = json.desc.msgID;
        if (msgID === 3088) {
            var id = json.desc.monitorId;
            var flag = $.inArray(id, allAlarmList);
            if (flag === -1) {
                allAlarmList.push(id);
            }
            /*$("#alarmno").text(allAlarmList.length);
            if (allAlarmList.length > 99) {
                $("#alarmno").text('99+');
            }*/
            if (allAlarmList.length > 0) {
                $("#alarmno").show();
            } else {
                $("#alarmno").hide();
            }
            //声音
            if( localStorage.getItem(alarmVoiceFlag) == "true" ){
                voiceControl(2, true)
            }

            //闪烁
            $("#alarmWaves").css("visibility", "visible");
            setTimeout("$('#alarmWaves').css('visibility','hidden');", 2000);
        } else if (msgID === 3071) {
            id = json.desc.monitorId;
            flag = $.inArray(id, allAlarmList);
            if (flag !== -1) {
                for (var i = 0; i < allAlarmList.length; i++) {
                    if (id === allAlarmList[i]) {
                        allAlarmList.splice(i, 1);
                    }
                }
            }
            if (allAlarmList.length > 0) {
                $("#alarmno").show();
            } else {
                $("#alarmno").hide();
            }
        }
    },
    alarmDeal: function () {
        var jumpFlag = false;
        var permissionUrls = $("#permissionUrls").val();
        if (permissionUrls != null && permissionUrls !== undefined) {
            var urllist = permissionUrls.split(",");
            if (urllist.indexOf("/a/search/list") > -1) {
                var str = "";
                jumpFlag = true;
                if (allAlarmList.length !== 0) {
                    for (var j = 0; j < allAlarmList.length; j++) {
                        str += allAlarmList[j] + ",";
                    }
                    var data = {
                        "vehicleId": str
                    };
                    var url = "/clbs/a/search/addSession";
                    json_ajax("POST", url, "json", false, data, function (result) {
                        if (result) {
                            location.href = "/clbs/a/search/list?atype=2";
                        } else {
                            location.href = "/clbs/a/search/list";
                        }
                    });
                } else {
                    location.href = "/clbs/a/search/list";
                }
            }
        }
        if (!jumpFlag) {
            layer.msg("无操作权限，请联系管理员");
        }
    },
    specialAlarm: function (data) {
        var json = JSON.parse(data.body);
        var confirmMsg = json.data // 后端推送消息已经组装好，不需要上面再解析
        var monitorName = json.data.split('发')[0].replace('监控对象:', '');
        var videoUrl = "/clbs/realTimeVideo/video/list";
        if (!json.desc.monitorId) {
            return;
        }
        alarmUrlType = 1;
        $('#bomMsgBoxContent').text(confirmMsg);
        pagesNav.bomMsgShow();
        specialAlarmUrl = videoUrl + '?videoId=' + json.desc.monitorId + '&videoName=' + monitorName;
    },
    init809ForwardAlarmName: function () {
        $.ajax({
            type: 'GET',
            url: '/clbs/m/monitorForwardingAlarmSearch/get809ForwardAlarmName',
            contentType: 'application/x-www-form-urlencoded',
            dataType: 'json',
            async: true,
            timeout: 30000,
            success: function (data) {
                if (data.success) {
                    forwardAlarmName = data.obj;
                } else {
                    console.log("获取809转发报警名称异常")
                }
            },
            error: function () {
                layer.msg(systemError, {
                    move: false
                });
            }
        });
    },
    getAlarmType: function (type) {
        var alarmType = '';
        switch (type) {
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
    updateTable: function (data) {
        if (data != null) {
            var json = $.parseJSON(data.body);
            var msgData = json.data;
            if (msgData == null || msgData == undefined) {
                return;
            }
            //转发平台状态改变则刷新列表
            if (msgData.msgHead != undefined && msgData.msgHead.msgID == 0x0D06) {
                platformCheck.refreshTable();
            }
            var msgId = msgData.msgHead.msgID;
            var serverIp = msgData.msgHead.serverIp; //转发平台地址
            if (msgId == 0x9400) {
                // 根据协议区分前端需要展示的内容
                var dataType = json.data.msgBody.dataType;
                $("#_error_label").hide();
                if (dataType == 0x9401) { //JTT报警督办
                    // var alarm = msgData.msgBody.alarm;
                    $("#msgTitle_alarm").text("处理JTT报警督办");
                    // 报警类型  warnType  alarmType
                    var alarm = json.data.msgBody.data.warnType;
                    if (alarm == null) {
                        alarm = json.data.msgBody.data.alarmType;
                    }
                    // 车牌号
                    var brand = json.data.msgBody.vehicleNo;
                    $("#alarm_handle").val('-1');
                    $(".normalType1").show();
                    $(".normalName1").show();
                    $(".xzName1").hide();
                    $("#question_alarm").text(brand + "报警，报警标识：" + alarm);
                    $("#alarm_brand").val(brand);
                    var result = json.data.msgBody;
                    $("#vehicleNo").val(result.vehicleNo);
                    $("#vehicleColor").val(getPlateColor(result.vehicleColor));
                    // 车牌颜色标识(数字)
                    $("#vehicleColorNumber").val(result.vehicleColor);
                    // 报警类型
                    var alarmType = json.data.msgBody.data.alarmType;
                    var warnTypeName;
                    if (alarmType == null) {
                        warnTypeName = pagesNav.getAlarmType(alarm)
                    } else {
                        var forwardAlarmNameElement = forwardAlarmName[alarmType];
                        warnTypeName = forwardAlarmNameElement == null ? pagesNav.getAlarmType(alarm) : forwardAlarmNameElement;
                    }
                    $("#warnType").val(warnTypeName);
                    // 报警时间
                    $("#alarmTime").val(result.data.warnTime);
                    $("#warnTime").val(formatDateAll(result.data.warnTime * 1000));
                    // 报警开始时间
                    $("#alarmStartTime").val(result.data.alarmStartTime);
                    // 转发平台id
                    $("#plateFormId").val(json.desc.t809PlatId);
                    // 报警类型标识(808报警标识)
                    $("#alarmType").val(result.data.alarmType);
                    // 监控对象id
                    $("#monitorId").val(result.data.monitorId);
                    $("#eventId").val(result.data.eventId);
                    var protocolType = json.data.msgHead.protocolType;
                    if (protocolType == 100 || protocolType == 1011 || protocolType == 1012 || protocolType == 1091 || protocolType == 1013) {
                        // 源子业务类型
                        $("#sourceDataType").val(result.data.dataType);
                        // 源报文序列号(相当于809-2011版本督办id)
                        $("#sourceMsgSn").val(result.data.msgSn);
                    } else {
                        // 源子业务类型
                        $("#sourceDataType").val(5122);
                        // 源报文序列号(相当于809-2011版本督办id)
                        $("#sourceMsgSn").val(result.data.supervisionId);
                    }
                    $("#msgSn").val(json.data.msgHead.msgSn);
                    var warnSrc = '';
                    switch (result.data.warnSrc) {
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
                    $("#warnSrc").val(warnSrc);
                    $("#alarmMsgId").val(json.data.msgHead.handleId);
                    $("#supervisionLevel").val(result.data.supervisionLevel == 0 ? '紧急' : '一般');
                    $("#supervisionEndTime").val(formatDateAll(result.data.supervisionEndTime * 1000));
                    $("#supervisor").val(result.data.supervisor);
                    $("#supervisionTel").val(result.data.supervisionTel);
                    $("#supervisionEmal").val(result.data.supervisionEmal);

                    $("#goTrace_alarm").modal('show');
                }
                return;
            } else if (msgId == 0x9300) {
                var dataType = json.data.msgBody.dataType;
                $("#_error_label").hide();
                if (dataType == 0x9301) { //JTT平台查岗
                    $("#navAnswer").val('');
                    $("#msgDataType9301").val(dataType);
                    $("#infoId9301").val(json.data.msgBody.data.infoId);
                    $("#objectType9301").val(json.data.msgBody.data.objectType);
                    $("#objectId9301").val(json.data.msgBody.data.objectId);
                    $("#question9301").val(json.data.msgBody.data.infoContent);
                    $("#serverIp9301").val(serverIp);
                    $("#msgGNSSCenterId9301").val(json.data.msgHead.msgGNSSCenterId);
                    $("#groupId9301").val(json.data.msgHead.groupId);
                    $("#gangId").val(json.data.msgHead.handleId);
                    $("#msgSn9301").val(json.data.msgHead.msgSn);
                    $("#msgID9301").val(json.data.msgBody.dataType); // 子业务类型标识
                    // 转发平台id
                    $("#platFormId9301").val(json.desc.t809PlatId);
                    $("#navAnswer").val("");
                    $("#msgTitle9301").text("处理JTT平台查岗");
                    // $("#superiorPlatform").modal('show'); //针对上级平台的查岗弹框，新增上级平台名称的显示
                    $("#superiorPlatform").show();
                    $('#platformName').text(json.data.msgBody.platformName);
                    $(".xzInfo").hide();
                    $("#goTrace9301").modal('show');
                } else if (dataType == 0x9302) {
                    $("#msgDataType9302").val(dataType);
                    $("#infoId9302").val(json.data.msgBody.data.infoId);
                    $("#objectType9302").val(json.data.msgBody.data.objectType);
                    $("#objectId9302").val(json.data.msgBody.data.objectId);
                    $("#question9302").val(json.data.msgBody.data.infoContent);
                    $("#msgGNSSCenterId9302").val(json.data.msgHead.msgGNSSCenterId);
                    $("#groupId9302").val(json.data.msgHead.groupId);
                    $("#serverIp9302").val(serverIp);
                    $("#answer9302").val("");
                    $("#msgTitle9302").text("下发平台间报文");
                    $("#msgSn9302").val(json.data.msgHead.msgSn);
                    $("#msgID9302").val(json.data.msgBody.dataType);
                    $("#platFormId9302").val(json.desc.t809PlatId);
                    // $("#superiorPlatform").modal('hide');
                    $("#superiorPlatform").hide();
                    $("#goTrace9302").modal('show');
                } else if (dataType == 0x9305) { //西藏平台查岗
                    $("#navAnswer").val('');
                    $("#msgTitle9301").text("处理西藏企业查岗");
                    $(".xzInfo").show();
                    $("#navAnswer").val("");
                    $("#msgDataType9301").val(dataType);
                    $("#infoId9301").val(json.data.msgBody.data.infoId);
                    $("#objectType9301").val(json.data.msgBody.data.objectType);
                    $("#objectId9301").val(json.data.msgBody.data.objectId);
                    $("#question9301").val(json.data.msgBody.data.infoContent);
                    $("#serverIp9301").val(serverIp);
                    $("#msgGNSSCenterId9301").val(json.data.msgHead.msgGNSSCenterId);
                    $("#groupId9301").val(json.data.msgHead.groupId);
                    $("#gangId").val(json.data.msgHead.handleId);
                    $("#enterprise").val(json.data.msgBody.data.enterprise);
                    $("#gangman").val(json.data.msgBody.data.gangman);
                    $("#goTrace9301").modal('show');
                    $('#platformName').text(json.data.msgBody.data.platformName);
                    $("#superiorPlatform").show();
                } else if (dataType == 0x9306) { //西藏报警督办
                    $("#msgTitle_alarm").text("处理西藏企业督办");
                    $(".normalType1").hide();
                    $(".normalName1").hide();
                    $(".xzName1").show();
                    $("#alarm_handle").val('-1');
                    var result = json.data.msgBody;
                    // 809转发平台接入码
                    $("#msgGNSSCenterId9041").val(json.data.msgHead.msgGNSSCenterId);
                    // 主链路ip
                    $("#serverIp9041").val(serverIp);
                    // 子业务类型标识
                    $("#objectType9041").val(dataType);
                    // 运输企业名称
                    $("#warnSrc").val(result.data.enterprise);
                    // 809督办查岗消息表id
                    $("#alarmMsgId").val(json.data.msgHead.handleId);
                    // 督办级别
                    $("#supervisionLevel").val(result.data.level == 0 ? '紧急' : '一般');
                    // 督办截止时间
                    $("#supervisionEndTime").val(formatDateAll(result.data.deadTime * 1000));
                    // 督办人
                    $("#supervisor").val(result.data.gangman);
                    // 督办人电话
                    $("#supervisionTel").val(result.data.tel);
                    // 督办人油箱
                    $("#supervisionEmal").val(result.data.email);
                    // 本次督办id
                    $("#supervisionId9041").val(result.data.infoId);
                    // 督办内容
                    $("#infoContent").val(result.data.infoContent);
                    $("#goTrace_alarm").modal('show');
                    $("#superiorPlatform").hide();
                } else if (dataType == 0x9310) { // 巡检应答推送
                    var msgBody = json.data.msgBody.data;
                    msgBody.answercountdown = '';
                    msgBody.answerType = '2'; // 1:列表应答，2：弹窗应答
                    numFlag = false;
                    $('.inspectionInfo').show();
                    $('#noticeWaves').show();
                    pagesNav.reconnectVoice(true);
                    pagesNav.showAnswerModal(msgBody);
                }
            }
        }
    },
    // 接收809断线重连状态
    update809MessageData: function (data) {
        var isConnection = data.body;
        if (isConnection === 'true') {
            $('.conactInfo').hide();
            $('#noticeWaves').hide();
            if (numFlag) {
                $('#menu2 .noInfo').show();
                $('#noticeRedIcon').hide();
            }
            pagesNav.reconnectVoice(false);
        } else {
            $('#noticeRedIcon').show();
            $('#noticeWaves').show();
            $('#menu2 .noInfo').hide();
            $('.conactInfo').show();
            pagesNav.reconnectVoice(true);
        }
    },
    platformAlarmAck: function (answer) {
        $("#_error_label").hide();
        var alarmTime = $("#alarmTime").val();
        var alarmMsgId = $("#alarmMsgId").val();
        var objectType = $("#objectType9041").val();
        var serverIp = $("#serverIp9041").val();
        var msgGNSSCenterId = $("#msgGNSSCenterId9041").val();
        var brand = $("#alarm_brand").val();
        if ($(".normalType1").is(":hidden")) { // 西藏报警督办
            var url = "/clbs/m/connectionparamsset/extendPlatformAlarmAck";
            json_ajax("POST", url, "json", false, {
                "msgId": alarmMsgId,
                "serverIp": serverIp,
                "msgGNSSCenterId": msgGNSSCenterId,
                "result": answer,
                "infoId": $("#supervisionId9041").val()
            }, pagesNav.dealCallBack)
        } else { // 平台报警督办
            var url = "/clbs/m/connectionparamsset/platformAlarmAck";
            json_ajax("POST", url, "json", false, {
                "monitorId": $("#monitorId").val(),
                "eventId": $("#eventId").val(),
                "brand": brand,
                "vehicleColor": $("#vehicleColorNumber").val(),
                "sourceDataType": $("#sourceDataType").val(),
                "sourceMsgSn": $("#sourceMsgSn").val(),
                "msgSn": $("#msgSn").val(),
                "warnTime": alarmTime,
                "alarmMsgId": alarmMsgId,
                "alarmHandle": answer,
                "alarmStartTime": $("#alarmStartTime").val(),
                "plateFormId": $("#plateFormId").val(),
                "alarmType": $("#alarmType").val()
            }, pagesNav.dealCallBack);
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
            if ($(".normalType1").is(":hidden") && result == 3) {
                msg = '未查询到督办记录';
            }
            layer.msg(msg);
            $("#goTrace_alarm").modal('hide');
        } else if (data.msg) {
            layer.msg(data.msg);
        }
    },

    platformMsgAck: function (type, formId) {
        // hideErrorMsg();
        if (formId) { // 标准809查岗
            var answer = $("#navAnswer").val();
            if (answer == "") {
                showErrorMsg("应答不能为空", "navAnswer");
                return;
            }
            var url = "/clbs/m/connectionparamsset/checkGroup";
            var groupId = $("#groupId9301").val();
            var objectType = $("#objectType9301").val(); // 查岗对象类型
            if (objectType == 3) { // 查岗对象类型为3(下级平台所属所有业户)
                // 检查应答用户的所属企业是否有经营许可证号
                json_ajax("POST", url, "json", false, {
                    "groupId": groupId
                }, function (data) {
                    if (data.success) {
                        pagesNav.ackStandard809Gang(type, formId);
                    } else {
                        layer.msg("企业的经营许可证号不能为空，请先完善企业信息");
                    }
                });
            } else {
                pagesNav.ackStandard809Gang(type, formId);
            }
        } else { // 平台间报文
            var audio = document.getElementById("laid-off");
            audio.pause();
            audio.currentTime = 0;
            var answer = $("#answer" + type).val();
            if (answer == "") {
                showErrorMsg("应答不能为空", "answer" + type);
                return;
            }
            $("#goTrace" + type).modal('hide');
            var msgDataType = $("#msgDataType" + type).val();
            var infoId = $("#infoId" + type).val();
            var objectType = $("#objectType" + type).val();
            var objectId = $("#objectId" + type).val();
            var serverIp = $("#serverIp" + type).val();
            var msgGNSSCenterId = $("#msgGNSSCenterId" + type).val();
            var groupId = $("#groupId" + type).val();
            var msgSn = $("#msgSn" + type).val();
            var msgID = $("#msgID" + type).val();
            var platFormId = $("#platFormId" + type).val();
            var url = "/clbs/m/connectionparamsset/platformMsgAck";
            json_ajax("POST", url, "json", false, {
                "infoId": infoId,
                "answer": answer,
                "msgDataType": msgDataType,
                "objectType": objectType,
                "objectId": objectId,
                "serverIp": serverIp,
                "msgGNSSCenterId": msgGNSSCenterId,
                "groupId": groupId,
                "msgSn": msgSn,
                "msgID": msgID,
                "platFormId": platFormId
            }, pagesNav.callback());
        }
    },
    ackStandard809Gang: function (type, formId) { // 应答标准809查岗
        $(formId).ajaxSubmit(function (data) {
            var result = JSON.parse(data).obj.handleStatus;
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
            $("#goTrace" + type).modal('hide');
        });
    },
    callback: function () {

    },
    //字体修改提示
    helpCenterModal: function () {
        $("#helpCenterModal").modal('show');
    },
    languageModal: function () {
        $("#languageModal").modal('show');

    },
    // 跳转查岗督办报表
    goGangSupervision: function (num) {
        var jumpFlag = false;
        var permissionUrls = $("#permissionUrls").val();
        if (permissionUrls != null && permissionUrls !== undefined) {
            var urllist = permissionUrls.split(",");
            if (urllist.indexOf("/m/reportManagement/gangSupervisionReport/list") > -1) {
                jumpFlag = true;
                location.href = "/clbs/m/reportManagement/gangSupervisionReport/list?gangType=" + num + "";
            }
        }
        if (!jumpFlag) {
            layer.msg("无操作权限，请联系管理员");
        }
    },
    goAlarmUrl: function (num) {
        var jumpFlag = false;
        var permissionUrls = $("#permissionUrls").val();
        if (permissionUrls != null && permissionUrls !== undefined) {
            var urllist = permissionUrls.split(",");
            if (urllist.indexOf("/m/basicinfo/monitoring/vehicle/list") > -1) {
                jumpFlag = true;
                location.href = "/clbs/m/basicinfo/monitoring/vehicle/list?tipType=" + num + "";
            }
        }
        if (!jumpFlag) {
            layer.msg("无操作权限，请联系管理员");
        }
    },
    //跳转809转发平台连接页面
    goConnection: function () {
        var jumpFlag = false;
        var permissionUrls = $("#permissionUrls").val();
        if (permissionUrls != null && permissionUrls !== undefined) {
            var urllist = permissionUrls.split(",");
            if (urllist.indexOf("/m/connectionparamsset/list") > -1) {
                jumpFlag = true;
                location.href = "/clbs/m/connectionparamsset/list";
            }
        }
        if (!jumpFlag) {
            layer.msg("无操作权限，请联系管理员");
        }
    },
    //跳转监管平台巡检监控人员页面
    inspectionPeople: function () {
        var jumpFlag = false;
        var permissionUrls = $("#permissionUrls").val();
        if (permissionUrls != null && permissionUrls !== undefined) {
            var urllist = permissionUrls.split(",");
            if (urllist.indexOf("/page?listPage=modules/reportManagement/inspectionPeople") > -1) {
                jumpFlag = true;
                location.href = "/clbs/page?listPage=modules/reportManagement/inspectionPeople";
            }
        }
        if (!jumpFlag) {
            layer.msg("无操作权限，请联系管理员");
        }
    },
    //跳转服务到期报表
    goFwUrl: function () {
        var jumpFlag = false;
        var permissionUrls = $("#permissionUrls").val();
        if (permissionUrls != null && permissionUrls !== undefined) {
            var urllist = permissionUrls.split(",");
            if (urllist.indexOf("/v/statistic/lifecycleStatistic/list") > -1) {
                jumpFlag = true;
                location.href = "/clbs/v/statistic/lifecycleStatistic/list?fwType=2";
            }
        }
        if (!jumpFlag) {
            layer.msg("无操作权限，请联系管理员");
        }
    },
    goBxUrl: function () {
        var jumpFlag = false;
        var permissionUrls = $("#permissionUrls").val();
        if (permissionUrls != null && permissionUrls !== undefined) {
            var urllist = permissionUrls.split(",");
            if (urllist.indexOf("/m/basicinfo/monitoring/vehicle/insurance/list") > -1) {
                jumpFlag = true;
                location.href = "/clbs/m/basicinfo/monitoring/vehicle/insurance/list?insuranceTipType=1";
            }
        }
        if (!jumpFlag) {
            layer.msg("无操作权限，请联系管理员");
        }
    },
    initChat: function () {
        var content = $('#chatWin .modal-content');
        var iframe = content.find('iframe');
        var userName = $('#userName').html();
        var path = '/clbs/cb/chatPage?token=' + headers.access_token;
        //var path = '/clbs/cb/chatPage?token=47818110-2437-4109-ae8b-972b10d66475' ;
        iframe.attr('src', path);
    },
    openChat: function () {
        var content = $('#chatWin .modal-content');
        var iframe = content.find('iframe');
        var url = iframe.attr('src');
        json_ajax("GET", url, "html", false, {}, function () {
            pagesNav.chatVisible = true;
            pagesNav.clearMsgTitleFun();
            $('#chatWin').modal('show');
            pagesNav.showChatRedIcon(0);
        });
    },
    chatNewMsg: function () {
        if (!pagesNav.chatVisible) {
            pagesNav.showChatRedIcon(1);
        }
    },
    showChatRedIcon: function (flag) {
        if (flag === 0) {
            $("#noticeRedIcon1").hide();
            $('#newMsgTitle').animate({
                width: 0,
                opacity: 0
            }).hide();
        } else {
            // $("#noticeRedIcon1").show();
            if (newChatTime) return;
            $('#newMsgTitle').show().animate({
                width: "210px",
                opacity: 1
            });
            $('#newMsgTitle').on('click', pagesNav.openChat);
            newChatTime = setTimeout(function () {
                pagesNav.clearMsgTitleFun();
                $("#noticeRedIcon1").show();
            }, 5000);
        }
    },
    clearMsgTitleFun: function () {
        $('#newMsgTitle').animate({
            width: 0,
            opacity: 0
        }).hide();
        clearTimeout(newChatTime);
        newChatTime = null;
    },
    closeChat: function () {
        $('#chatWin').modal('hide');
        pagesNav.chatVisible = false;
    },
    PlatformAlarm: function () {
        setTimeout(function () {
            if (webSocket.conFlag) {
                webSocket.subscribe(headers, "/user/topic/platformRemind", function (data) {
                    var data = JSON.parse(data.body),
                        brand = data.brand,
                        vehicleId = data.vehicleId,
                        time = new Date(data.warmTime),
                        year = time.getFullYear(),
                        month = time.getMonth() + 1,
                        day = time.getDate(),
                        hours = time.getHours(),
                        minutes = time.getMinutes(),
                        seconds = time.getSeconds();
                    if (month >= 1 && month <= 9) {
                        month = '0' + month;
                    }
                    if (day >= 1 && day <= 9) {
                        day = '0' + day;
                    }
                    if (hours >= 1 && hours <= 9) {
                        hours = '0' + hours;
                    }
                    if (minutes >= 1 && minutes <= 9) {
                        minutes = '0' + minutes;
                    }
                    if (seconds >= 1 && seconds <= 9) {
                        seconds = '0' + seconds;
                    }
                    if (minutes == 0) {
                        minutes = '00';
                    }
                    if (seconds == 0) {
                        seconds = '00';
                    }
                    var warmTime = year + '-' + month + '-' + day + ' ' + hours + ':' + minutes + ':' + seconds;
                    var content = '监控对象：' + brand + ' 在' + warmTime + '产生的报警长时间未处理，请及时处理，谢谢！';
                    alarmUrlType = 2;
                    $('#bomMsgBoxContent').text(content);
                    PlatformAlarmUrl = '/clbs/r/riskManagement/disposeReport/list?vehicleId=' + vehicleId + '&status=1' + '&brand=' + brand;
                    pagesNav.bomMsgShow();
                }, "/app/risk/security/subVehicleUser", "a");
            } else {
                pagesNav.PlatformAlarm();
            }
        }, 1000);
    },
    setCarouselActive: function () {
        $('.carousel').carousel({
            pause: null,
            wrap: false
        });
        $('.carousel').carousel('pause');
        $('.rightHover').mouseover(function () {
            $(this).attr('src', '/clbs/resources/img/hoverRight.png');
        }).mouseout(function () {
            $(this).attr('src', '/clbs/resources/img/languageRight.png');
        });
        $('.leftHover').mouseover(function () {
            $(this).attr('src', '/clbs/resources/img/hoverLeft.png');
        }).mouseout(function () {
            $(this).attr('src', '/clbs/resources/img/languageLeft.png');
        });
    },
    /**
     * 下载导出文件
     * @param url 下载地址
     * @param fileName 下载文件名
     * @param event 当前点击按钮
     * */
    downLoadFile: function (url, fileName, event) {
        var a = document.createElement('a');
        if (event) {
            var url = $(event).attr('href');
            a.href = encodeURI(url);
        } else {
            a.href = encodeURI(url);
        }
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        a.remove();
    },
    // 显示下载中心弹窗
    showExportManager: function () {
        $('#downloadStatus').val('');
        $('#downloadFileName').val('');

        //表格列定义
        var columnDefs = [{
            //第一列，用来显示序号
            "searchable": false,
            "orderable": false,
            "targets": 0
        }];
        var columns = [{
            //第一列，用来显示序号
            "data": null,
            "class": "text-center"
        }, {
            "data": null,
            "class": "text-center",
            render: function (data, type, row, meta) {
                if (row.status == '2') {
                    return '<button onclick="pagesNav.downLoadFile(\'' + row.assemblePath + '\',\'' + row.fileName + '\')" id="' + row.digestId + '" class="btn btn-primary">下载</button>';
                }
                return '<button onclick="pagesNav.downLoadFile(null,\'' + row.fileName + '\',this)" id="' + row.digestId + '" class="btn btn-default" disabled>下载</button>';
            }
        }, {
            "data": 'createDateTime',
            "class": "text-center",
        }, {
            "data": "fileName",
            "class": "text-center"
        }, {
            "data": "doubleFileSize",
            "class": "text-center",
        }, {
            "data": "module",
            "class": "text-center",
        }, {
            "data": "status",
            "class": "text-center",
            render: function (data) {
                var result = '';
                switch (data) {
                    case 0:
                        result = '待执行';
                        break;
                    case 1:
                        result = '执行中';
                        break;
                    case 2:
                        result = '成功';
                        break;
                    case 3:
                        result = '失败';
                        break;
                    default:
                        result = '';
                        break;
                }
                return '<span class="status">' + result + '</span>';
            }
        }];
        //ajax参数
        var ajaxDataParamFun = function (d) {
            d.status = $('#downloadStatus').val(); // 下载状态
            d.fileName = $('#downloadFileName').val(); // 文件名
        };
        //表格setting
        var setting = {
            listUrl: '/clbs/offline/export/list',
            columnDefs: columnDefs, //表格列定义
            columns: columns, //表格列
            dataTableDiv: 'downloadDataTable', //表格
            ajaxDataParamFun: ajaxDataParamFun, //ajax参数
            pageable: true, //是否分页
            showIndexColumn: true, //是否显示第一列的索引列
            enabledChange: true,
            pageNumber: 10,
            setPageNumber: true,
        };
        //创建表格
        downloadTable = new TG_Tabel.createNew(setting);
        //表格初始化
        downloadTable.init();

        $('#exportManager').modal('show');
        $('#downloadRedIcon').hide();
    },
    // 更新导出管理弹窗列表记录状态
    updateExportMananer: function (data) {
        if ($('#exportManager').is(':visible')) {
            $('#downloadRedIcon').hide();
            var result = JSON.parse(data.body);
            var curDownload = $('#' + result.id);
            if (curDownload.length > 0) {
                var curRecord = curDownload.closest('tr');
                curDownload.attr({
                    'class': 'btn btn-primary',
                    'href': result.url
                }).prop('disabled', false);
                curRecord.find('.status').text('成功');
                downloadTable.refresh();
            }
        } else {
            $('#downloadRedIcon').show();
        }
    },
    /**
     * 巡检应答弹窗相关方法
     */
    clearAnserTimer: function () {
        if (answerTimer) {
            clearInterval(answerTimer);
            answerTimer = null;
        }
    },
    errBack: function (e) {
    },
    answerImgPreview: function () {
        var reader = new FileReader();
        reader.onload = function (e) {
            $('#answerPhotoImg').attr('src', e.target.result).show();
            $('#answerNoImgInfo,#takingPicturesBox').hide();
        };
        $('#answerImgFile').on('change', function (event) {
            if (event.target.files.length == 0) {
                return;
            }
            var file = event.target.files[0];
            if (/^image\//.test(file.type)) {
                var isLt1M = file.size / 1024 / 1024 < 1;
                if (!isLt1M) {
                    layer.msg('照片大小超过1M，请重新上传');
                    $('#answerImgFile').val('');
                    return;
                }
                reader.readAsDataURL(file);
            } else {
                layer.msg('请选择图片');
            }
        });
    },
    // 调用摄像头
    getCamera: function () {
        var video = document.getElementById('picturesVideo');
        var mediaConfig = {
            video: true
        };
        if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
            navigator.mediaDevices.getUserMedia(mediaConfig).then(function (stream) {
                video.srcObject = stream;
                video.play();
            });
        } else if (navigator.getUserMedia) { // Standard
            navigator.getUserMedia(mediaConfig, function (stream) {
                video.src = stream;
                video.play();
            }, pagesNav.errBack);
        } else if (navigator.webkitGetUserMedia) { // WebKit-prefixed
            navigator.webkitGetUserMedia(mediaConfig, function (stream) {
                video.src = window.webkitURL.createObjectURL(stream);
                video.play();
            }, pagesNav.errBack);
        } else if (navigator.mozGetUserMedia) { // Mozilla-prefixed
            navigator.mozGetUserMedia(mediaConfig, function (stream) {
                video.src = window.URL.createObjectURL(stream);
                video.play();
            }, pagesNav.errBack);
        }
        if (navigator.mediaDevices && navigator.mediaDevices.enumerateDevices) {
            navigator.mediaDevices.enumerateDevices().then(function (deviceInfos) {
                if (deviceInfos.length > 0 && deviceInfos[0].kind === 'videoinput') {
                    $('#takingPicturesBox').show();
                } else {
                    layer.msg('无可用摄像头');
                }
            }).catch(function (handleError) {
                layer.msg('调用摄像头失败');
            });
        }
    },
    // 拍照
    takingPicturesFun: function () {
        var canvas = document.getElementById('pictureCanvas');
        var context = canvas.getContext('2d');
        var video = document.getElementById('picturesVideo');
        context.drawImage(video, 0, 0, 200, 241);
        var imageData = canvas.toDataURL('Image/jpeg', 1); //获取canvas中绘制的数据
        $("#answerNoImgInfo").hide();
        $("#answerPhotoImg").attr('src', imageData).show(); //将canvas中的数据转化为图片显示
    },
    // 转换图片为blob对象
    base64DateUrlToBlob: function (base64DataUrl, type) {
        //去掉url的头，并转换为bytes
        var bytes = window.atob(base64DataUrl.split(',')[1]);
        //处理异常,将ascii码小于0的转换为大于0
        var ab = new ArrayBuffer(bytes.length);
        var ia = new Uint8Array(ab);
        for (var i = 0; i < bytes.length; i++) {
            ia[i] = bytes.charCodeAt(i);
        }
        return new File([ab], 'answer.jpeg', {
            type: type
        });
    },
    // 获取巡检应答信息
    getAnswerInfo: function (id, dealStatus) {
        var status = true;
        if (!dealStatus) { // 状态为未应答
            // 检查应答是否已经超时
            json_ajax("GET", '/clbs/adas/inspectUser/check/' + id, "json", false, null, function (result) {
                if (!result.success) {
                    if (result.msg) {
                        layer.msg(result.msg);
                    }
                    status = false;
                }
            });
        }
        if (!status) return;
        json_ajax("GET", '/clbs/adas/inspectUser/answer/' + id, "json", false, null, function (result) {
            if (result) {
                result.answerId = id;
                result.answercountdown = '';
                result.answerType = '1'; // 1:列表应答，2：弹窗应答
                pagesNav.showAnswerModal(result);
            }
        });
    },
    showAnswerModal: function (data) {
        $('#answerPhotoImg').attr('src', '').hide();
        $('#answerNoImgInfo,#selectImgBox,#answerCountdownGroup,#submitAnswer,#answerNoImgInfo h4').show();
        $('#takingPicturesBox').hide();
        $('#submitAnswer').prop('disabled', true);
        pagesNav.renderAnswerParam(data);
        pagesNav.clearAnserTimer();

        var maxTime = 0;
        var nowTime = new Date(data.serverTime).getTime();
        var expireTime = new Date(data.expireTime).getTime();
        if (expireTime > nowTime) {
            maxTime = Math.floor((expireTime - nowTime) / 1000);
        }
        if (data.answerStatus === 0 && maxTime > 0) { // 未应答状态,且还在应答有效期内
            answerTimer = setInterval(function () {
                if (maxTime >= 0) {
                    var minutes = fillZero(Math.floor(maxTime / 60));
                    var seconds = fillZero(Math.floor(maxTime % 60));
                    $('#answercountdown').val(minutes + ':' + seconds);
                    --maxTime;
                } else {
                    $('#submitAnswer').prop('disabled', true);
                    if (data.answerType === '2') {
                        $('#inspectionInfo').hide();
                    }
                    clearInterval(answerTimer);
                    answerTimer = null;
                }
            }, 1000);
            $('#submitAnswer').prop('disabled', false);
        } else {
            $('#answercountdown').val('00:00');
        }
        if (initAnswer) {
            pagesNav.answerImgPreview();
            initAnswer = false;
        }
        $('#patrolAnswerModel').modal('show');
    },
    renderAnswerParam: function (data) {
        if (data.answerStatus === 1) { // 正常应答时,只用展示应答信息
            $('#selectImgBox,#submitAnswer,#answerCountdownGroup').hide();
            if (data.mediaUrl) {
                $('#answerNoImgInfo').hide();
                $('#answerPhotoImg').attr('src', data.mediaUrl).show();
            } else {
                $('#answerNoImgInfo').show();
                $('#answerNoImgInfo h4').hide();
            }
        }
        if (data) {
            for (key in data) {
                $('#' + key).val(data[key]);
            }
        }
    },
    submitAnswer: function () {
        var formData = $("#answerForm").serializeArray();
        var parameter = new FormData();
        $.each(formData, function (index, item) {
            parameter.append(item.name, item.value || '');
        });
        var img = $('#answerPhotoImg').attr('src');
        if (img) {
            var imgData = pagesNav.base64DateUrlToBlob(img, 'image/jpeg');
            var isLt1M = imgData.size / 1024 / 1024 < 1;
            if (!isLt1M) {
                layer.msg('照片大小超过1M，请重新上传');
                return;
            }
            parameter.append('image', imgData);
        } else {
            layer.msg('照片为空，请拍照上传！');
            return;
        }
        if ($('#answerUserTel').val() === '') {
            layer.msg('联系电话为空，请到用户信息完善信息！');
            return;
        } else if ($('#answerUserIdentityNumber').val() === '') {
            layer.msg('身份证号为空，请到用户信息完善信息！');
            return;
        } else if ($('#socialSecurityNumber').val() === '') {
            layer.msg('社会保险号为空，请到用户信息完善信息！');
            return;
        }
        $.ajax({
            url: '/clbs/adas/inspectUser/answer', // 后台接口
            type: 'POST',
            processData: false, // processData和contentType必须指定为false
            contentType: false,
            cache: false,
            data: parameter,
            success(res) {
                var result = JSON.parse(res);
                $('#takingPicturesBox').hide();
                if (result.success) {
                    pagesNav.clearAnserTimer();
                    $('#patrolAnswerModel').modal('hide');
                    layer.msg('应答成功');
                    if (answerTable) { // 如果在监管平台巡检监控人员页面,应答后需刷新列表
                        var currentPage = answerTable.dataTable.page();
                        answerTable.dataTable.page(currentPage).draw(false);
                    }
                } else if (result.msg) {
                    layer.msg(result.msg);
                }
            }
        })
    },
    bomMsgShow: function () {
        msgBoxType = true;
        $('#bomMsgBoxClickShow').show();
        $('#bomMsgBoxClickShowBtm').show();
        $('#bomMsgBox').addClass('bomMsgBoxActive');
    },
    bomMsgClose: function () {
        msgBoxType = true;
        $('#bomMsgBoxClickShow').show();
        $('#bomMsgBoxClickShowBtm').show();
        $('#bomMsgBox').removeClass('bomMsgBoxActive');
    },
    okClick: function () {
        msgBoxType = false;
        $('#bomMsgBoxClickShow').hide();
        $('#bomMsgBoxClickShowBtm').hide();
        if (alarmUrlType === 2) {
            window.location.href = PlatformAlarmUrl;
        } else if(alarmUrlType === 1) {
            var jumpFlag = false;
            var permissionUrls = $("#permissionUrls").val();
            if (permissionUrls !== null && permissionUrls !== undefined) {
                var urllist = permissionUrls.split(",");
                if (urllist.indexOf("/realTimeVideo/video/list") > -1) {
                    jumpFlag = true;
                    window.location.href = specialAlarmUrl;
                }
            }
            if (!jumpFlag) {
                layer.msg("无操作权限，请联系管理员");
            }

        } else if(alarmUrlType === 3) {
            window.location.href = PlatformAlarmUrl3;
        } else {
            return;
        }
    },
    noClick: function () {
        msgBoxType = false;
        $('#bomMsgBoxClickShow').hide();
        $('#bomMsgBoxClickShowBtm').hide();
        $('#bomMsgBox').removeClass('bomMsgBoxActive');
    },
};
$(function () {
    


    pagesNav.init809ForwardAlarmName();
    pagesNav.gethistoryno();
    pagesNav.init();
    renderPlateColorSelect(); // 渲染车牌颜色下拉框
    pagesNav.PlatformAlarm();
    pagesNav.setCarouselActive();
    $('#bomMsgBoxClickShow').hide();
    $('#bomMsgBoxClickShowBtm').hide();
    $("#getCameraBtn").bind('click', pagesNav.getCamera); // 调用摄像头
    $("#pictureSnap").bind('click', pagesNav.takingPicturesFun); // 拍照
    $("#pictureClose").bind('click', function () {
        $('#takingPicturesBox').hide();
    });
    $("#submitAnswer").bind('click', pagesNav.submitAnswer); // 巡检应答
    // 巡检应答弹窗关闭,清除定时器
    $("#patrolAnswerModel").on("hidden.bs.modal", function () {
        pagesNav.clearAnserTimer();
    });

    $("#language").bind('click', pagesNav.languageModal); // 多语言插弹框显示
    $("#downloadBox").bind('click', pagesNav.showExportManager); // 下载中心弹框显示
    $('#downloadSearch').bind('click', function () {
        downloadTable.refresh();
    });
    $('.carousel').on('slid.bs.carousel', function () {
        $('.carousel').carousel({
            pause: null,
            wrap: false
        });
        $('.carousel').carousel('pause');
    });

    $("#updateFontSize").bind("click", pagesNav.helpCenterModal);

    $("#alarm_handle").on('change', function () {
        var curVal = $(this).val();
        var errorBox = $("#goTrace_alarm #_error_label");
        if (curVal != -1) {
            errorBox.hide();
        } else {
            if (errorBox.text() != '') {
                errorBox.show();
            }
        }
    });

    $("#navAnswer").on('input propertychange', function () {
        var curVal = $(this).val();
        var errorBox = $("#goTrace9301 #_error_label");
        if (curVal != '') {
            errorBox.hide();
        } else {
            if (errorBox.text() != '') {
                errorBox.show();
            }
        }
    });

    $("#answer9302").on('input propertychange', function () {
        var curVal = $(this).val();
        var errorBox = $("#goTrace9302 #_error_label");
        if (curVal != '') {
            errorBox.hide();
        } else {
            if (errorBox.text() != '') {
                errorBox.show();
            }
        }
    });
    $('#noticeWaves,#noticeBtn1').on('click', function () {
        // $('#menu2').toggle('show');
        var menu = $('#menu2');
        if (menu.is(':visible')) {
            menu.slideUp();
        } else {
            menu.slideDown();
        }
    });

    // 读取本地报警声音设置并初始化
    initVoice()
    $('#noticeBox1').on('contextmenu',function (e) {
        $('#navVoices').toggle()
        e.preventDefault()
    })
    $('#navVoices').on('click', handleMailClick);
    $('#alarmIcoHide').on('contextmenu',function (e) {
        $('#emaliVoice').toggle()
        e.preventDefault()
    })
    $('#emaliVoiceImg').on('click', handleAlarmClick);
    /**
     * 实现导入文件按钮,鼠标悬停时显示文件名称效果
     * */
    $('body').on('mouseover', 'label.changeFile', function () {
        var curInput = $(this).prev('input[type="file"]')[0];
        var length = curInput.files.length;
        if (length === 0) {
            $(this).attr('title', '未选择文件。')
        } else {
            $(this).attr('title', curInput.files[0].name);
        }
    });
});