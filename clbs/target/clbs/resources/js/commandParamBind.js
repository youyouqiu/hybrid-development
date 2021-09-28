(function (window, $) {
    var referVehicleList = JSON.parse($("#referVehicleList").attr("value"));
    var addEventIndex = 2, addPhoneIndex = 2, addInfoDemandIndex = 2, addBaseStationIndex = 2;
    var loadTime;
    var commandType = $("#commandType").val();//指令类型
    var vid = $("#vid").val();//车辆id
    var commandTypeName = $("#commandTypeName").val();//指令类型名称
    var deviceType;
    isRead = true;
    _timeout = 0;
    layer_time = 0;
    isCameraTimeFlag = false; //定时定距读取判断
    isCameraDistanceFlag = false;
    // 多媒体检索
    isMediaRead = true;
    mediaTimeout = null;
    var serialNumber = '';
    var addUpgradeFlag = true;// 阻止表单重复提交

    commandParamBind = {
        init: function () {
            if (commandTypeName.indexOf('设置') == -1 && commandTypeName.indexOf('查询') == -1) {
                $("#bindTitle").html(commandTypeName + '设置');
            } else {
                $("#bindTitle").html(commandTypeName);
            }
            if (commandTypeName == '无线升级' || commandTypeName == '电话参数') {
                $('#bindForm .modal-body').css('overflow', 'visible')
            } else {
                $('#bindForm .modal-body').css('overflow', 'hidden auto')
            }
            deviceType = $('.deviceCheck:checked').val();
            if (deviceType == '11') {// T808-2019协议
                $('.addItemInfo').show();
                $('.removeItemInfo').hide();
            } else {
                $('.addItemInfo').hide();
                $('.removeItemInfo').show();
            }

            if (parseInt(commandType) > 18 && parseInt(commandType) < 30) {
                $('#commandTypeLabel').removeClass('col-md-3').addClass('col-md-2');
            }

            window.onbeforeunload = function () {
                var cancelStrS = {
                    "desc": {
                        "MsgId": 40964,
                        "UserName": $("#userName").text()
                    },
                    "data": params
                };
                webSocket.unsubscribealarm(headers, "/app/vehicle/unsubscribestatus", cancelStrS);
                clearTimeout(_timeout);
                if (mediaTimeout) clearTimeout(mediaTimeout);
            };
            // 监听弹窗关闭
            $('#commonWin').on('hide.bs.modal', function () {
                clearTimeout(_timeout);
                if (mediaTimeout) clearTimeout(mediaTimeout);
            });

            var dataArr = {value: referVehicleList};
            $("#referent").bsSuggest({
                indexId: 1, //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                idField: "vid",
                keyField: "brand",
                effectiveFields: ["brand"],
                searchFields: ["vid"],
                data: dataArr
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {//选择参考对象
                json_ajax("POST", "/clbs/v/monitoring/commandParam/getReferenceInfo", "json", false, {
                    "vid": keyword.id,
                    "commandType": commandType
                }, function (data) {
                    if (data.success) {
                        var dataObj = data.obj.result;
                        commandParamBind.setReferenceInfo(dataObj);
                    }
                });
            }).on('onUnsetSelectValue', function () {
            });
            var requestStrS = {
                "desc": {
                    "MsgId": 40964,
                    "UserName": $("#userName").text()
                },
                "data": params
            };
            if (commandType != 23) {
                webSocket.subscribe(headers, "/user/topic/deviceReportLog", commandParamBind.getSensor0104Param, "/app/vehicle/oil/setting", requestStrS);
            } else {
                webSocket.subscribe(headers, "/topic/deviceProperty", commandParamBind.getSensor0104Param, null, null);
            }
            //23 27 28 29进页面读取
            if (commandType == 23 || commandType == 27 || commandType == 28 || commandType == 29) {
                commandParamBind.getParam();
            }

            commandParamBind.setInputAndSelectDisable('.allLocationInfo', true);
            commandParamBind.setInputAndSelectDisable('.distanceInfo', true);
            commandParamBind.setInputAndSelectDisable('.timingInfo', false);

            commandParamBind.mediaInit();

            // 下发升级包,文件管理表格初始化
            if (commandType == 138) {
                commandParamBind.fileTableInit();
                commandParamBind.renderFileSelectHtml();
            }

            laydate.render({elem: '#baseStationFixedTime', type: 'time', theme: '#6dcff6'});
            $("[data-toggle='tooltip']").tooltip();
        },
        // 多媒体检索和多媒体上传时间赋值
        mediaInit: function () {
            laydate.render({elem: '#msStartTime', type: 'datetime', theme: '#6dcff6'});
            laydate.render({elem: '#msEndTime', type: 'datetime', theme: '#6dcff6'});
            var nowDate = new Date();
            msStartTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1) : parseInt(nowDate
                    .getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate() : nowDate.getDate())
                + " " + "00:00:00";
            msEndTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1) : parseInt(nowDate
                    .getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate() : nowDate.getDate())
                + " " + ("23") + ":" + ("59") + ":" + ("59");

            $("#msStartTime").val(msStartTime);
            $("#msEndTime").val(msEndTime);
        },
        // 依据操作类型控制界面显示
        setOperationType: function (flag) {
            if (flag == '1') {
                // 事件设置操作类型下拉框
                if ($('#eventOperateType').val() == 0) {
                    $('.insertDiv').hide();
                } else {
                    $('.insertDiv').show();
                }
                if ($('#eventOperateType').val() == 3) {
                    $('.eventSelectId').show();
                    $('.eventId').hide();
                }
                if ($('#eventOperateType').val() == 4) {
                    $('.insertDiv').find('label.control-label:eq(1)').hide();
                    $('.insertDiv').find('div:eq(1)').hide();
                    $('.eventSelectId').show();
                    $('.eventId').hide();
                }
            }

            if (flag == '2') {
                // 电话本设置操作类型下拉框
                if ($('#phoneBookOperateType').val() == 0) {
                    $('.insertDiv').hide();
                } else {
                    $('.insertDiv').show();
                }
                if ($('#phoneBookOperateType').val() == 3) {
                    $('.phoneBookSelectId').show();
                    $('.phoneBookId').hide();
                }
            }
            if (flag == '3') {
                // 信息点播操作类型下拉框
                if ($('#infoDemandOperateType').val() == 0) {
                    $('.insertDiv').hide();
                } else {
                    $('.insertDiv').show();
                }
                if ($('#infoDemandOperateType').val() == 3) {
                    $('.infoDemandSelect').show();
                    $('.infoDemandId').hide();
                }

            }
        },
        //处理平台设置参数-初始化
        clearInputTextValue: function () {
            $("#bindForm input").each(function () {
                if ($(this).attr("id") != "brand" && $(this).attr("id") != "vid") {
                    $(this).val("");
                }
            });
        },
        // 清空参考车牌
        resetReferencePlate: function () {
            $("#brands").val("");
            $("#brands").attr("data-id", "");
        },
        //设置参考对象信息
        setReferenceInfo: function (dataObj) {
            switch (commandType) {
                case "11":
                    $("#reportMainApn").val(dataObj.mainServerAPN);
                    $("#reportMainAddress").val(dataObj.mainServerAddress);
                    $("#slaveServerApn").val(dataObj.slaveServerAPN);
                    $("#slaveServerAddress").val(dataObj.slaveServerAddress);
                    $("#reportBackupApn").val(dataObj.backupServerAPN);
                    $("#reportBackupAddress").val(dataObj.backupServerAddress);
                    $("#reportServerAccound").val(dataObj.mainServerCallUserName);
                    $("#reportServerPwd").val(dataObj.mainServerCallUserPwd);
                    $("#slaveServerCallUserName").val(dataObj.slaveServerCallUserName);
                    $("#slaveServerCallUserPwd").val(dataObj.slaveServerCallUserPwd);
                    $("#reportBackupAccound").val(dataObj.backupServerCallUserName);
                    $("#reportBackupPwd").val(dataObj.backupServerCallUserPwd);
                    if (deviceType == '1') {
                        $("#reportServerTcp").val(dataObj.serverTCPPort);
                        $("#reportServerUdp").val(dataObj.serverUDPPort);
                    } else {
                        /*$("#slaveServerApn").val(dataObj.slaveServerAPN);
                         $("#slaveServerAddress").val(dataObj.slaveServerAddress);
                         $("#slaveServerCallUserName").val(dataObj.slaveServerCallUserName);
                         $("#slaveServerCallUserPwd").val(dataObj.slaveServerCallUserPwd);*/
                    }
                    break;
                case "12":
                    $("#terminalSendTime").val(dataObj.heartSpace);
                    $("#terminalAnswerTime").val(dataObj.tcpAckTimeOut);
                    $("#terminalAnswerTcp").val(dataObj.tcpReUpTimes);
                    $("#terminalAnswerUdp").val(dataObj.udpAckTimeOut);
                    $("#terminalUdpNum").val(dataObj.udpReUpTimes);
                    $("#terminalAnswerSms").val(dataObj.smsAckTimeOut);
                    $("#terminalSmsNum").val(dataObj.smsReUpTimes);
                    $("#inflectionPointAdditional").val(dataObj.inflectionPointAdditional);
                    $("#electronicFenceRadius").val(dataObj.electronicFenceRadius);
                    break;
                case "131":
                    $("#UpgradeDial").val(dataObj.wDailName);
                    $("#UpgradeDialName").val(dataObj.wDailUserName);
                    $("#UpgradeDialPwd").val(dataObj.wDailPwd);
                    $("#UpgradeAddress").val(dataObj.wAddress);
                    $("#UpgradeTcpTort").val(dataObj.wTcpPort);
                    $("#UpgradeUdpTort").val(dataObj.wUdpPort);
                    $("#UpgradeManufacturer").val(dataObj.manufactorId);
                    $("#UpgradeHardware").val(dataObj.hardwareVersion);
                    $("#UpgradeFirmware").val(dataObj.firmwareVersion);
                    $("#UpgradeUrlAddress").val(dataObj.url);
                    $("#UpgradeTimeLimit").val(dataObj.wTimeLimit);
                    break;
                case "132":
                    $("#specifyServerConnect").val(dataObj.accessControl);
                    $("#specifyServerAuthCode").val(dataObj.authCode);
                    $("#specifyServerDial").val(dataObj.dailName);
                    $("#specifyServerDialName").val(dataObj.dailUserName);
                    $("#specifyServerDialPwd").val(dataObj.dailPwd);
                    $("#specifyServerAddress").val(dataObj.address);
                    $("#specifyServerTcpPort").val(dataObj.tcpPort);
                    $("#specifyServerUdpPort").val(dataObj.udpPort);
                    $("#specifyServerTimeLimit").val(dataObj.timeLimit);
                    break;
                case "14":
                    $("#locationTactics").val(dataObj.positionUpTactics);
                    commandParamBind.connectionControlSH();
                    $("#locationProgram").val(dataObj.positionUpScheme);
                    $(".locationDefaultDistance").val(dataObj.defaultDistanceUpSpace);
                    $(".locationDefaultTime").val(dataObj.defaultTimeUpSpace);
                    $(".locationSleepDistance").val(dataObj.dormancyUpDistanceSpace);
                    $(".locationSleep").val(dataObj.dormancyUpTimeSpace);
                    $(".locationAlarmDistance").val(dataObj.emergencyAlarmUpDistanceSpace);
                    $(".locationAlarmTime").val(dataObj.emergencyAlarmUpTimeSpace);
                    $(".locationNoLoginDistance").val(dataObj.driverLoggingOutUpDistanceSpace);
                    $(".locationNoLogin").val(dataObj.driverLoggingOutUpTimeSpace);
                    break;
                case "16":
                    $("#telephoneNumber").val(dataObj.platformPhoneNumber);
                    $("#telephoneResetNumber").val(dataObj.resetPhoneNumber);
                    $("#telephoneSetNumber").val(dataObj.reInitialPhoneNumber);
                    $("#telephoneSnsNumber").val(dataObj.platformSMSPhoneNumber);
                    $("#telephoneSnsAlarm").val(dataObj.receiveDeviceSMSTxtAlarmPhoneNumber);
                    $("#telephoneStrategy").val(dataObj.deviceAnswerPhoneType);
                    $("#telephoneMaxTime").val(dataObj.timesMaxCallTime);
                    $("#telephoneMonthTime").val(dataObj.monthlyMaxCallTime);
                    $("#telephoneMonitor").val(dataObj.listenPhoneNumber);
                    $("#monitorPrivilege").val(dataObj.platformPrivilegeSMSNumber);
                    break;
                case "17":
                    $("#videoTactics").val(dataObj.videoTactics);
                    commandParamBind.connectionControlSH();
                    $("#videoCameraSwitchOne").val(dataObj.cameraTimerOpenFlag1);
                    $("#videoCameraStorageOne").val(dataObj.cameraTimerSaveFlag1);
                    $("#videoCameraSwitchTwo").val(dataObj.cameraTimerOpenFlag2);
                    $("#videoCameraStorageTwo").val(dataObj.cameraTimerSaveFlag2);
                    $("#videoCameraSwitchThree").val(dataObj.cameraTimerOpenFlag3);
                    $("#videoCameraStorageThree").val(dataObj.cameraTimerSaveFlag3);
                    $("#videoCameraSwitchFour").val(dataObj.cameraTimerOpenFlag4);
                    $("#videoCameraStorageFour").val(dataObj.cameraTimerSaveFlag4);
                    $("#videoCameraSwitchFive").val(dataObj.cameraTimerOpenFlag5);
                    $("#videoCameraStorageFive").val(dataObj.cameraTimerSaveFlag5);
                    $("#videoCameraTimeUnit").val(dataObj.timingUnit);
                    $("#videoCameraTimeInterval").val(dataObj.timingSpace);
                    $("#videoCameraSwitchMarkOne").val(dataObj.cameraDistanceOpenFlag1);
                    $("#videoCameraStorageMarkOne").val(dataObj.cameraDistanceSaveFlag1);
                    $("#videoCameraSwitchMarkTwo").val(dataObj.cameraDistanceOpenFlag2);
                    $("#videoCameraStorageMarkTwo").val(dataObj.cameraDistanceSaveFlag2);
                    $("#videoCameraSwitchMarkThree").val(dataObj.cameraDistanceOpenFlag3);
                    $("#videoCameraStorageMarkThree").val(dataObj.cameraDistanceSaveFlag3);
                    $("#videoCameraSwitchMarkFour").val(dataObj.cameraDistanceOpenFlag4);
                    $("#videoCameraStorageMarkFour").val(dataObj.cameraDistanceSaveFlag4);
                    $("#videoCameraSwitchMarkFive").val(dataObj.cameraDistanceOpenFlag5);
                    $("#videoCameraStorageMarkFive").val(dataObj.cameraDistanceSaveFlag5);
                    $("#videoCameraDistanceUnit").val(dataObj.distanceUnit);
                    $("#videoCameraDistanceInterval").val(dataObj.distanceSpace);
                    $("#videoCameraQuality").val(dataObj.pictureQuality);
                    $("#videoCameraBrightness").val(dataObj.luminance);
                    $("#videoCameraContrast").val(dataObj.contrast);
                    $("#videoCameraSaturation").val(dataObj.saturation);
                    $("#videoCameraChroma").val(dataObj.chroma);
                    break;
                case "18":
                    $("#GPSFlag").val(dataObj.gPSFlag);
                    $("#beidouFlag").val(dataObj.beidouFlag);
                    $("#GLONASSFlag").val(dataObj.gLONASSFlag);
                    $("#GalileoFlag").val(dataObj.galileoFlag);
                    $("#GNSSBaudRate").val(dataObj.gNSSBaudRate);
                    $("#GNSSPositionOutputRate").val(dataObj.gNSSPositionOutputRate);
                    $("#GNSSPositionCollectRate").val(dataObj.gNSSPositionCollectRate);
                    $("#gnss").val(dataObj.gNSSPositionUploadType);
                    break;
                case "19":
                    addEventIndex = 0;
                    $("#eventMain-container").html('');
                    $(".insertDiv").remove();
                    for (var i = 0; i < dataObj.length; i++) {
                        $("#eventOperateType").val(dataObj[i].operationType);
                        commandParamBind.addEventInputOrSelect(dataObj[i].operationType);
                        $("#eventId_" + addEventIndex).val(dataObj[i].eventId);
                        $("#eventContent_" + addEventIndex).val(dataObj[i].eventContent);
                        /*if (i == 0) {
                         commandParamBind.eventSettingUpdateOrDel();
                         $("#eventOperateType").val(dataObj[i].operationType);
                         $("#eventId_2").val(dataObj[i].eventId);
                         $("#eventContent_2").val(dataObj[i].eventContent);
                         } else {
                         commandParamBind.addEventSetting();
                         $("#eventId_" + addEventIndex).val(dataObj[i].eventId);
                         $("#eventContent_" + addEventIndex).val(dataObj[i].eventContent);
                         }*/
                        commandParamBind.setOperationType('1');
                    }
                    break;
                case "20":
                    addPhoneIndex = 0;
                    $("#phoneBook-MainContent").html('');
                    for (var i = 0; i < dataObj.length; i++) {
                        $("#phoneBookOperateType").val(dataObj[i].operationType);
                        commandParamBind.addPhoneBookInputOrSelect(dataObj[i].operationType);
                        $("#phoneBookId_" + addPhoneIndex).val(dataObj[i].phoneBookId);
                        $("#phoneBookContact_" + addPhoneIndex).val(dataObj[i].contact);
                        $("#phoneBookNumber_" + addPhoneIndex).val(dataObj[i].phoneNo);
                        $("#phoneBookOperateType_" + addPhoneIndex).val(dataObj[i].callType);
                        commandParamBind.setOperationType('2');
                        /*if (i == 0) {
                         commandParamBind.resetPhoneBookSetting();
                         $("#phoneBookOperateType").val(dataObj[i].operationType);
                         $("#phoneBookId_2").val(dataObj[i].phoneBookId);
                         $("#phoneBookContact_2").val(dataObj[i].contact);
                         $("#phoneBookNumber_2").val(dataObj[i].phoneNo);
                         $("#phoneBookOperateType_2").val(dataObj[i].callType);
                         } else {
                         commandParamBind.addPhoneBookEvent();
                         $("#phoneBookId_" + addPhoneIndex).val(dataObj[i].phoneBookId);
                         $("#phoneBookContact_" + addPhoneIndex).val(dataObj[i].contact);
                         $("#phoneBookNumber_" + addPhoneIndex).val(dataObj[i].phoneNo);
                         $("#phoneBookOperateType_" + addPhoneIndex).val(dataObj[i].callType);
                         }*/
                    }
                    break;
                case "21":
                    addInfoDemandIndex = 0;
                    $(".insertDiv").remove();
                    for (var i = 0; i < dataObj.length; i++) {
                        $("#infoDemandOperateType").val(dataObj[i].operationType);
                        commandParamBind.addInfoDemandInputOrSelect(dataObj[i].operationType);
                        $("#infoDemandId_" + addInfoDemandIndex).val(dataObj[i].infoId);
                        $("#infoDemandName_" + addInfoDemandIndex).val(dataObj[i].infoContent);
                        $("#messageContent_" + addInfoDemandIndex).val(dataObj[i].messageContent);
                        $("#sendFrequency_" + addInfoDemandIndex).val(dataObj[i].sendFrequency);
                        commandParamBind.setOperationType('3');
                        /*if (i == 0) {
                         commandParamBind.resetInfoDemandMenuSetting();
                         $("#infoDemandOperateType").val(dataObj[i].operationType);
                         $("#infoDemandId_2").val(dataObj[i].infoId);
                         $("#infoDemandName_2").val(dataObj[i].infoContent);
                         } else {
                         commandParamBind.addInfoDemandEvent();
                         $("#infoDemandId_" + addInfoDemandIndex).val(dataObj[i].infoId);
                         $("#infoDemandName_" + addInfoDemandIndex).val(dataObj[i].infoContent);
                         }*/
                    }
                    break;
                case "22":
                    $("#baseStationReportMode").val(dataObj.requitePattern);
                    $("#baseStationpositioningMode").val(dataObj.locationPattern);
                    $("#baseStationStartTimePoint").val(dataObj.requiteTime);
                    $("#baseStationReportInterval").val(dataObj.requiteInterval);
                    commandParamBind.baseStationReportModeCheckFn();
                    var locationTime = dataObj.locationTime;
                    if (locationTime != null && locationTime.length > 0) {
                        var locationTimes = locationTime.split(";");
                        $("#baseStationFixedTime").val(locationTimes[0]);
                        for (var i = 1; i < locationTimes.length; i++) {
                            var html = "<div class='form-group'><label class='col-md-3 control-label'>定点时间：</label><div class='col-md-3'><input type='text' id='baseStationFixedTime_" + i + "' name='locationTime' value='" + locationTimes[i] + "' onclick='' class='form-control'/></div><div class='col-md-1'><button type='button' class='btn btn-danger baseStationDelete deleteIcon'><span class='glyphicon glyphicon-trash' aria-hidden='true'></span></button></div><div class='col-md-5'></div></div>";
                            $("#baseStation-MainContent").append(html);
                        }
                    }
                    break;
                case "24":
                    if (dataObj.length > 0) {
                        var html = '';
                        $("#RS232serialPortParameters .insertAddr").html('');
                        $("#RS232serialPortParameters .insertDiv").remove();
                        $("#RS232Numbers").val(dataObj.length);
                        for (var i = 0; i < dataObj.length; i++) {
                            var serialPortNumber = dataObj[i].serialPortNumber;
                            var baudRate = dataObj[i].baudRate == 255 ? -1 : dataObj[i].baudRate;
                            var dataPosition = dataObj[i].dataBits == 255 ? -1 : dataObj[i].dataBits;
                            var stopPosition = dataObj[i].stopBit == 255 ? -1 : dataObj[i].stopBit;
                            var checkPosition = dataObj[i].parityBit == 255 ? -1 : dataObj[i].parityBit;
                            var flowControl = dataObj[i].flowControl == 255 ? -1 : dataObj[i].flowControl;
                            var dataAcceptanceTimeoutTime = dataObj[i].dataAcceptanceTimeoutTime;
                            html += '<div class="insertDiv">    ' +
                                '<div class="form-group"><label class="col-md-3 control-label">串口' + i + '参数</label></div>' +
                                '<div class="form-group">' +
                                '<label class="col-md-3 control-label"><label class="text-danger">*</label>串口序号：</label>' +
                                '<div class="col-md-3">' +
                                '<input type="text" name="serialPortNumber" class="form-control" readonly value="' + serialPortNumber + '">' +
                                '</div></div>' +
                                '<div class="form-group">' +
                                '<label class="col-md-3 control-label">波特率：</label>' +
                                '<div class="col-md-3">' +
                                '<select name="baudRate" class="form-control" value="' + baudRate + '">' +
                                '<option value="1">2400</option>' +
                                '<option value="2">4800</option>' +
                                '<option value="3">9600</option>' +
                                '<option value="4">19200</option>' +
                                '<option value="5">38400</option>' +
                                '<option value="6">57600</option>' +
                                '<option value="7">115200</option>' +
                                '<option value="-1">不修改波特率</option>' +
                                '</select>' +
                                '</div>' +
                                '<label class="col-md-2 control-label">数据位：</label>' +
                                '<div class="col-md-3">' +
                                '<select name="dataBits" class="form-control" value="' + dataPosition + '">' +
                                '<option value="5">5</option>' +
                                '<option value="6">6</option>' +
                                '<option value="7">7</option>' +
                                '<option value="8">8</option>' +
                                '<option value="-1">不修改数据位</option>' +
                                '</select>' +
                                '</div></div>' +
                                '<div class="form-group">' +
                                '<label class="col-md-3 control-label">停止位：</label>' +
                                '<div class="col-md-3">' +
                                '<select name="stopBit" class="form-control" value="' + stopPosition + '">' +
                                '<option value="1">1</option>' +
                                '<option value="2">2</option>' +
                                '<option value="-1">不修改停止位</option>' +
                                '</select>' +
                                '</div>' +
                                '<label class="col-md-2 control-label">校验位：</label>' +
                                '<div class="col-md-3">' +
                                '<select name="parityBit" class="form-control" value="' + checkPosition + '">' +
                                '<option value="1">奇校验</option>' +
                                '<option value="2">偶校验</option>' +
                                '<option value="3">无校验</option>' +
                                '<option value="-1">不修改校验位</option>' +
                                '</select>' +
                                '</div>' +
                                '</div>' +
                                '<div class="form-group">' +
                                '<label class="col-md-3 control-label">流控：</label>' +
                                '<div class="col-md-3">' +
                                '<select name="flowControl" class="form-control" value="' + flowControl + '">' +
                                '<option value="1">无流控</option>' +
                                '<option value="2">硬件流控</option>' +
                                '<option value="3">软件流控</option>' +
                                '<option value="-1">不修改流控</option>' +
                                '</select>' +
                                '</div>' +
                                '<label class="col-md-2 control-label">数据接收超时(毫秒)：</label>' +
                                '<div class="col-md-3">' +
                                '<div class="col-md-15">' +
                                '<input type="text" name="dataAcceptanceTimeoutTime" id="data232_' + i + '" class="form-control"  value="' + dataAcceptanceTimeoutTime + '">' +
                                '</div></div></div></div>'
                        }
                        $("#RS232serialPortParameters .insertAddr").html(html);
                        var targetSelect = $(".insertDiv select");
                        for (var i = 0; i < targetSelect.length; i++) {
                            $(targetSelect[i]).val($(targetSelect[i]).attr('value'));
                        }
                    }
                    break;
                case "25":
                    if (dataObj.length > 0) {
                        var html = '';
                        $("#RS485serialPortParameters .insertAddr").html('');
                        $("#RS485serialPortParameters .insertDiv").remove();
                        $("#RS485Numbers").val(dataObj.length);
                        for (var i = 0; i < dataObj.length; i++) {
                            var serialPortNumber = dataObj[i].serialPortNumber;
                            var baudRate = dataObj[i].baudRate == 255 ? -1 : dataObj[i].baudRate;
                            var dataPosition = dataObj[i].dataBits == 255 ? -1 : dataObj[i].dataBits;
                            var stopPosition = dataObj[i].stopBit == 255 ? -1 : dataObj[i].stopBit;
                            var checkPosition = dataObj[i].parityBit == 255 ? -1 : dataObj[i].parityBit;
                            var flowControl = dataObj[i].flowControl == 255 ? -1 : dataObj[i].flowControl;
                            var dataAcceptanceTimeoutTime = dataObj[i].dataAcceptanceTimeoutTime;
                            html += '<div class="insertDiv">    ' +
                                '<div class="form-group"><label class="col-md-3 control-label">串口' + i + '参数</label></div>' +
                                '<div class="form-group">' +
                                '<label class="col-md-3 control-label"><label class="text-danger">*</label>串口序号：</label>' +
                                '<div class="col-md-3">' +
                                '<input type="text" name="serialPortNumber" class="form-control" readonly value="' + serialPortNumber + '">' +
                                '</div></div>' +
                                '<div class="form-group">' +
                                '<label class="col-md-3 control-label">波特率：</label>' +
                                '<div class="col-md-3">' +
                                '<select name="baudRate" class="form-control" value="' + baudRate + '">' +
                                '<option value="1">2400</option>' +
                                '<option value="2">4800</option>' +
                                '<option value="3">9600</option>' +
                                '<option value="4">19200</option>' +
                                '<option value="5">38400</option>' +
                                '<option value="6">57600</option>' +
                                '<option value="7">115200</option>' +
                                '<option value="-1">不修改波特率</option>' +
                                '</select>' +
                                '</div>' +
                                '<label class="col-md-2 control-label">数据位：</label>' +
                                '<div class="col-md-3">' +
                                '<select name="dataBits" class="form-control" value="' + dataPosition + '">' +
                                '<option value="5">5</option>' +
                                '<option value="6">6</option>' +
                                '<option value="7">7</option>' +
                                '<option value="8">8</option>' +
                                '<option value="-1">不修改数据位</option>' +
                                '</select>' +
                                '</div></div>' +
                                '<div class="form-group">' +
                                '<label class="col-md-3 control-label">停止位：</label>' +
                                '<div class="col-md-3">' +
                                '<select name="stopBit" class="form-control" value="' + stopPosition + '">' +
                                '<option value="1">1</option>' +
                                '<option value="2">2</option>' +
                                '<option value="-1">不修改停止位</option>' +
                                '</select>' +
                                '</div>' +
                                '<label class="col-md-2 control-label">校验位：</label>' +
                                '<div class="col-md-3">' +
                                '<select name="parityBit" class="form-control" value="' + checkPosition + '">' +
                                '<option value="1">奇校验</option>' +
                                '<option value="2">偶校验</option>' +
                                '<option value="3">无校验</option>' +
                                '<option value="-1">不修改校验位</option>' +
                                '</select>' +
                                '</div>' +
                                '</div>' +
                                '<div class="form-group">' +
                                '<label class="col-md-3 control-label">流控：</label>' +
                                '<div class="col-md-3">' +
                                '<select name="flowControl" class="form-control" value="' + flowControl + '">' +
                                '<option value="1">无流控</option>' +
                                '<option value="2">硬件流控</option>' +
                                '<option value="3">软件流控</option>' +
                                '<option value="-1">不修改流控</option>' +
                                '</select>' +
                                '</div>' +
                                '<label class="col-md-2 control-label">数据接收超时(毫秒)：</label>' +
                                '<div class="col-md-3">' +
                                '<div class="col-md-15">' +
                                '<input type="text" name="dataAcceptanceTimeoutTime" id="data485_' + i + '" class="form-control"  value="' + dataAcceptanceTimeoutTime + '">' +
                                '</div></div></div></div>'
                        }
                        $("#RS485serialPortParameters .insertAddr").html(html);
                        var targetSelect = $(".insertDiv select");
                        for (var i = 0; i < targetSelect.length; i++) {
                            $(targetSelect[i]).val($(targetSelect[i]).attr('value'));
                        }
                    }
                    break;
                case "26":
                    if (dataObj.length > 0) {
                        var html = '';
                        $("#CANserialPortParameters .insertAddr").html('');
                        $("#CANserialPortParameters .insertDiv").remove();
                        $("#CANNumbers").val(dataObj.length);
                        for (var i = 0; i < dataObj.length; i++) {
                            var serialPortNumber = dataObj[i].serialPortNumber;
                            var baudRate = dataObj[i].baudRate == 255 ? -1 : dataObj[i].baudRate;
                            var dataPosition = dataObj[i].dataBits == 255 ? -1 : dataObj[i].dataBits;
                            var stopPosition = dataObj[i].stopBit == 255 ? -1 : dataObj[i].stopBit;
                            var checkPosition = dataObj[i].parityBit == 255 ? -1 : dataObj[i].parityBit;
                            var flowControl = dataObj[i].flowControl == 255 ? -1 : dataObj[i].flowControl;
                            var dataAcceptanceTimeoutTime = dataObj[i].dataAcceptanceTimeoutTime;
                            html += '<div class="insertDiv">    ' +
                                '<div class="form-group"><label class="col-md-3 control-label">串口' + i + '参数</label></div>' +
                                '<div class="form-group">' +
                                '<label class="col-md-3 control-label"><label class="text-danger">*</label>串口序号：</label>' +
                                '<div class="col-md-3">' +
                                '<input type="text" name="serialPortNumber" class="form-control" readonly value="' + serialPortNumber + '">' +
                                '</div></div>' +
                                '<div class="form-group">' +
                                '<label class="col-md-3 control-label">波特率：</label>' +
                                '<div class="col-md-3">' +
                                '<select name="baudRate" class="form-control" value="' + baudRate + '">' +
                                '<option value="1">2400</option>' +
                                '<option value="2">4800</option>' +
                                '<option value="3">9600</option>' +
                                '<option value="4">19200</option>' +
                                '<option value="5">38400</option>' +
                                '<option value="6">57600</option>' +
                                '<option value="7">115200</option>' +
                                '<option value="-1">不修改波特率</option>' +
                                '</select>' +
                                '</div>' +
                                '<label class="col-md-2 control-label">数据位：</label>' +
                                '<div class="col-md-3">' +
                                '<select name="dataBits" class="form-control" value="' + dataPosition + '">' +
                                '<option value="5">5</option>' +
                                '<option value="6">6</option>' +
                                '<option value="7">7</option>' +
                                '<option value="8">8</option>' +
                                '<option value="-1">不修改数据位</option>' +
                                '</select>' +
                                '</div></div>' +
                                '<div class="form-group">' +
                                '<label class="col-md-3 control-label">停止位：</label>' +
                                '<div class="col-md-3">' +
                                '<select name="stopBit" class="form-control" value="' + stopPosition + '">' +
                                '<option value="1">1</option>' +
                                '<option value="2">2</option>' +
                                '<option value="-1">不修改停止位</option>' +
                                '</select>' +
                                '</div>' +
                                '<label class="col-md-2 control-label">校验位：</label>' +
                                '<div class="col-md-3">' +
                                '<select name="parityBit" class="form-control" value="' + checkPosition + '">' +
                                '<option value="1">奇校验</option>' +
                                '<option value="2">偶校验</option>' +
                                '<option value="3">无校验</option>' +
                                '<option value="-1">不修改校验位</option>' +
                                '</select>' +
                                '</div>' +
                                '</div>' +
                                '<div class="form-group">' +
                                '<label class="col-md-3 control-label">流控：</label>' +
                                '<div class="col-md-3">' +
                                '<select name="flowControl" class="form-control" value="' + flowControl + '">' +
                                '<option value="1">无流控</option>' +
                                '<option value="2">硬件流控</option>' +
                                '<option value="3">软件流控</option>' +
                                '<option value="-1">不修改流控</option>' +
                                '</select>' +
                                '</div>' +
                                '<label class="col-md-2 control-label">数据接收超时(毫秒)：</label>' +
                                '<div class="col-md-3">' +
                                '<div class="col-md-15">' +
                                '<input type="text" name="dataAcceptanceTimeoutTime" id="dataCAN_' + i + '" class="form-control"  value="' + dataAcceptanceTimeoutTime + '">' +
                                '</div></div></div></div>'
                        }
                        $("#CANserialPortParameters .insertAddr").html(html);
                        var targetSelect = $(".insertDiv select");
                        for (var i = 0; i < targetSelect.length; i++) {
                            $(targetSelect[i]).val($(targetSelect[i]).attr('value'));
                        }
                    }
                    break;
            }
            $('input').inputClear();
        },
        //处理获取设备下发的结果
        currencyResponse: function (msg) {
            if (msg == null) {
                return;
            }
            var result = $.parseJSON(msg.body);
            var msgid = result.data.msgBody.msgSNACK;
            var status = result.data.msgBody.result;
            if (status == 0) {
                layer.msg("终端处理中", {time: false});
            }
            if (status == 1 || status == 2 || status == 3) {
                $("#submit_btn").removeAttr("disabled");
                layer.closeAll();
                layer.msg("参数下发失败");
            }
            return;
        },
        //下发
        doSubmit: function () {
            if (commandType == '30') {// 多媒体检索
                json_ajax("POST", "/clbs/v/monitoring/commandParam/sendOneMediaSearchUp", "json", false, {
                    "monitorId": vid,
                    "mediaId": $('#mediaId').val(),
                    "deleteSign": $('#deleteSign').is(':checked') ? '1' : '0',
                }, function (data) {
                    if (data.success) {
                        myTable.refresh();
                        $("#commonWin").modal("hide");
                        sendOneMediaMsgAck = data.obj.msgSN;
                        var username = $('#userName').text();
                        // webSocket.subscribe(headers, "/user/topic/fencestatus", function (msg) {
                        webSocket.subscribe(headers, "/user/topic/directive_parameter", function (msg) {
                            if (msg == null) return;
                            var result = $.parseJSON(msg.body);
                            var msgid = result.data.msgBody.msgSNACK;
                            // 流水号一致则刷新表格
                            if (sendOneMediaMsgAck == msgid) {
                                myTable.refresh();
                            }
                        }, null, null);
                    } else if (data.msg) {
                        layer.msg(data.msg);
                    }
                });
                return;
            }

            if (commandType === "31") {
                // 删除终端围栏
                var url = "/clbs/v/monitoring/commandParam/sendDeleteDeviceFence";
                var deviceFence = $("#deviceFence").val();
                var parameter = {"monitorIds": vid, 'commandType': commandType, "deviceFence": deviceFence};
                json_ajax("POST", url, "json", true, parameter, commandParamBind.sendFuelCallback);
                return;
            }

            var paramJsonStr = [];
            var insertDiv = $('.insertDiv');
            if (commandType != '19' && commandType != '20' && commandType != '21' && insertDiv.length == 0) {
                var formData = $('#bindForm').serializeArray();
                var sendObj = {};
                for (var i = 0; i < formData.length; i++) {
                    if (formData[i].value !== '') {
                        if (formData[i].name == "locationTime") {
                            var locationTime = sendObj[formData[i].name] != null ? sendObj[formData[i].name] + ";" : "";
                            sendObj[formData[i].name] = locationTime + formData[i].value;
                        } else {
                            var curInput = $('#bindForm').find('input[name=' + formData[i].name + ']');
                            var curSelect = $('#bindForm').find('select[name=' + formData[i].name + ']');
                            var flag = false;
                            for (var j = 0; j < curInput.length; j++) {
                                if (!$(curInput[j]).is(":hidden")) {
                                    flag = true;
                                }
                            }
                            for (var j = 0; j < curSelect.length; j++) {
                                if (!$(curSelect[j]).is(":hidden")) {
                                    flag = true;
                                }
                            }
                            if (flag) {
                                sendObj[formData[i].name] = formData[i].value;
                            }
                        }
                    }
                }
                if (Object.keys(sendObj).length > 0) {
                    paramJsonStr = [sendObj];
                }
            } else {
                var defaultInput = $(".defaultDiv").find('input');
                var defaultSelect = $(".defaultDiv").find('select');
                var defaultObj = {};
                for (var i = 0; i < defaultInput.length; i++) {
                    if ($(defaultInput[i]).val() !== '' && !$(defaultInput[i]).is(":hidden")) {
                        var name = $(defaultInput[i]).attr('name');
                        defaultObj[name] = $(defaultInput[i]).val();
                    }
                }
                for (var i = 0; i < defaultSelect.length; i++) {
                    if ($(defaultSelect[i]).val() !== '' && !$(defaultSelect[i]).is(":hidden")) {
                        var name = $(defaultSelect[i]).attr('name');
                        defaultObj[name] = $(defaultSelect[i]).val();
                    }
                }

                for (var i = 0; i < insertDiv.length; i++) {
                    newObj = JSON.parse(JSON.stringify(defaultObj));
                    var insertInput = $(insertDiv[i]).find('input');
                    var insertSelect = $(insertDiv[i]).find('select');
                    for (var j = 0; j < insertInput.length; j++) {
                        if ($(insertInput[j]).val() !== '' && !$(insertInput[j]).is(":hidden")) {
                            var name = $(insertInput[j]).attr('name');
                            newObj[name] = $(insertInput[j]).val();
                        }
                    }
                    for (var k = 0; k < insertSelect.length; k++) {
                        if ($(insertSelect[k]).val() !== '' && !$(insertSelect[k]).is(":hidden")) {
                            var name = $(insertSelect[k]).attr('name');
                            newObj[name] = $(insertSelect[k]).val();
                        }
                    }
                    if (Object.keys(newObj).length > 0) {
                        paramJsonStr.push(newObj);
                    }
                }
            }
            if (commandType == '138') {
                $('#upgradeType').prop('disabled', false);
                var formData = $('#bindForm').serializeArray();
                if ($('#upgradeFileSelect').val() === '' && $('#selectFile').val() === '') {
                    layer.msg('请选择升级文件!');
                    if ($('#selectFileBox').is(':hidden')) {
                        $('#upgradeType').prop('disabled', true);
                    }
                    return;
                }
                var paramObj = {
                    "batchUpgradeNum": "",
                    "commandType": 138,
                    "dependSoftVersion": "",
                    "equipmentModel": "",
                    "factoryNumber": "",
                    "manufacturerId": "",
                    "scheduleUpgradeTime": "",
                    "upgradeFileId": "",
                    "upgradeStrategyFlag": "",
                    "upgradeType": "",
                    "softVersion": ""
                };
                for (var i = 0; i < formData.length; i++) {
                    var item = formData[i];
                    if (paramObj[item.name] !== undefined && item.name != 'commandType') {
                        paramObj[item.name] = item.value;
                    }
                }
                var paramArr = [paramObj];
                var infoData = JSON.stringify(paramArr);
                $('#paramJsonStr').val(infoData);
                $("#bindForm").ajaxSubmit(function (data) {
                    var result = JSON.parse(data);
                    if (result.success) {
                        commandParamBind.sendFuel($('#upgradeType').val());
                    } else if (result.msg) {
                        layer.msg(result.msg)
                    }
                    if ($('#selectFileBox').is(':hidden')) {
                        $('#upgradeType').prop('disabled', true);
                    }
                });
                return;
            }
            if (paramJsonStr.length == 0) {
                layer.msg('请至少下发一个参数');
                return;
            }
            if (!commandParamBind.validates()) return;
            json_ajax("POST", "/clbs/v/monitoring/commandParam/saveParamByCommandType", "json", false, {
                "monitorIds": vid,
                "commandType": commandType,
                "paramJsonStr": JSON.stringify(paramJsonStr)
            }, function (data) {
                if (data.success) {
                    commandParamBind.sendFuel();
                } else if (data.msg) {
                    layer.msg(data.msg);
                }
            });
        },
        validates: function () {
            return $("#bindForm").validate({
                rules: {
                    serverTCPPort: {
                        digits: true,
                        min: 0,
                        max: 65535,
                    },
                    serverUDPPort: {
                        digits: true,
                        min: 0,
                        max: 65535,
                    },
                    heartSpace: {
                        digits: true
                    },
                    tcpAckTimeOut: {
                        digits: true
                    },
                    tcpReUpTimes: {
                        digits: true
                    },
                    udpAckTimeOut: {
                        digits: true
                    },
                    udpReUpTimes: {
                        digits: true
                    },
                    smsAckTimeOut: {
                        digits: true
                    },
                    smsReUpTimes: {
                        digits: true
                    },
                    wTcpPort: {
                        digits: true,
                        min: 0,
                        max: 65535,
                    },
                    wUdpPort: {
                        digits: true,
                        min: 0,
                        max: 65535,
                    },
                    wTimeLimit: {
                        digits: true
                    },
                    tcpPort: {
                        digits: true,
                        min: 0,
                        max: 65535,
                    },
                    udpPort: {
                        digits: true,
                        min: 0,
                        max: 65535,
                    },
                    timeLimit: {
                        digits: true
                    },
                    defaultDistanceUpSpace: {
                        digits: true
                    },
                    defaultTimeUpSpace: {
                        digits: true
                    },
                    dormancyUpDistanceSpace: {
                        digits: true
                    },
                    dormancyUpTimeSpace: {
                        digits: true
                    },
                    emergencyAlarmUpDistanceSpace: {
                        digits: true
                    },
                    emergencyAlarmUpTimeSpace: {
                        digits: true
                    },
                    driverLoggingOutUpDistanceSpace: {
                        digits: true
                    },
                    driverLoggingOutUpTimeSpace: {
                        digits: true
                    },
                    platformPhoneNumber: {
                        digits: true
                    },
                    resetPhoneNumber: {
                        digits: true
                    },
                    reInitialPhoneNumber: {
                        digits: true
                    },
                    platformSMSPhoneNumber: {
                        digits: true
                    },
                    receiveDeviceSMSTxtAlarmPhoneNumber: {
                        digits: true
                    },
                    timesMaxCallTime: {
                        digits: true
                    },
                    monthlyMaxCallTime: {
                        digits: true
                    },
                    listenPhoneNumber: {
                        digits: true
                    },
                    platformPrivilegeSMSNumber: {
                        digits: true
                    },
                    timingSpace: {
                        digits: true
                    },
                    distanceSpace: {
                        digits: true
                    },
                    pictureQuality: {
                        digits: true,
                        min: 1,
                        max: 10,
                    },
                    luminance: {
                        digits: true,
                        min: 0,
                        max: 255,
                    },
                    contrast: {
                        digits: true,
                        min: 0,
                        max: 127,
                    },
                    saturation: {
                        digits: true,
                        min: 0,
                        max: 127,
                    },
                    chroma: {
                        digits: true,
                        min: 0,
                        max: 255,
                    },
                    gNSSPositionCollectRate: {
                        digits: true,
                    },
                    dataAcceptanceTimeoutTime: {
                        required: true,
                        digits: true,
                        min: 10,
                        max: 2000,
                    },
                    inflectionPointAdditional: {
                        digits: true,
                        min: 0,
                        max: 179
                    },
                    electronicFenceRadius: {
                        digits: true,
                        min: 0,
                        max: 65535
                    },
                    infoId: {
                        required: true,
                    },
                    infoContent: {
                        required: true,
                    },
                    messageContent: {
                        required: true,
                    },
                    eventId: {
                        required: true,
                    },
                    eventContent: {
                        required: true,
                    },
                    phoneBookId: {
                        required: true,
                    },
                    contact: {
                        required: true,
                    },
                    phoneNo: {
                        required: true,
                    },
                    authCode: {
                        required: true
                    },
                    startTime: {
                        required: true
                    },
                    endTime: {
                        required: true,
                        compareDate: "#msStartTime"
                    },
                },
                messages: {
                    startTime: {
                        required: '请选择开始时间'
                    },
                    endTime: {
                        required: '请选择结束时间',
                        compareDate: "结束时间必须大于开始时间!"
                    },
                    infoId: {
                        required: '请输入信息ID',
                    },
                    infoContent: {
                        required: '请输入信息名称',
                    },
                    messageContent: {
                        required: '请输入信息内容',
                    },
                    eventId: {
                        required: '请输入事件ID',
                    },
                    eventContent: {
                        required: '请输入事件内容',
                    },
                    phoneBookId: {
                        required: '请输入联系人ID',
                    },
                    contact: {
                        required: '请输入联系人',
                    },
                    phoneNo: {
                        required: '请输入电话号码',
                    },
                    authCode: {
                        required: '请输入监管平台鉴权码',
                    },
                    serverTCPPort: {
                        digits: pub65535error,
                        min: pub65535error,
                        max: pub65535error
                    },
                    serverUDPPort: {
                        digits: pub65535error,
                        min: pub65535error,
                        max: pub65535error
                    },
                    heartSpace: {
                        digits: numberError
                    },
                    tcpAckTimeOut: {
                        digits: numberError
                    },
                    tcpReUpTimes: {
                        digits: numberError
                    },
                    udpAckTimeOut: {
                        digits: numberError
                    },
                    udpReUpTimes: {
                        digits: numberError
                    },
                    smsAckTimeOut: {
                        digits: numberError
                    },
                    smsReUpTimes: {
                        digits: numberError
                    },
                    wTcpPort: {
                        digits: pub65535error,
                        min: pub65535error,
                        max: pub65535error
                    },
                    wUdpPort: {
                        digits: pub65535error,
                        min: pub65535error,
                        max: pub65535error
                    },
                    wTimeLimit: {
                        digits: numberError
                    },
                    tcpPort: {
                        digits: pub65535error,
                        min: pub65535error,
                        max: pub65535error
                    },
                    udpPort: {
                        digits: pub65535error,
                        min: pub65535error,
                        max: pub65535error
                    },
                    timeLimit: {
                        digits: numberError
                    },
                    defaultDistanceUpSpace: {
                        digits: numberError
                    },
                    defaultTimeUpSpace: {
                        digits: numberError
                    },
                    dormancyUpDistanceSpace: {
                        digits: numberError
                    },
                    dormancyUpTimeSpace: {
                        digits: numberError
                    },
                    emergencyAlarmUpDistanceSpace: {
                        digits: numberError
                    },
                    emergencyAlarmUpTimeSpace: {
                        digits: numberError
                    },
                    driverLoggingOutUpDistanceSpace: {
                        digits: numberError
                    },
                    driverLoggingOutUpTimeSpace: {
                        digits: numberError
                    },
                    platformPhoneNumber: {
                        digits: numberError
                    },
                    resetPhoneNumber: {
                        digits: numberError
                    },
                    reInitialPhoneNumber: {
                        digits: numberError
                    },
                    platformSMSPhoneNumber: {
                        digits: numberError
                    },
                    receiveDeviceSMSTxtAlarmPhoneNumber: {
                        digits: numberError
                    },
                    timesMaxCallTime: {
                        digits: numberError
                    },
                    monthlyMaxCallTime: {
                        digits: numberError
                    },
                    listenPhoneNumber: {
                        digits: numberError
                    },
                    platformPrivilegeSMSNumber: {
                        digits: numberError
                    },
                    timingSpace: {
                        digits: numberError
                    },
                    distanceSpace: {
                        digits: numberError
                    },
                    pictureQuality: {
                        digits: pub10error,
                        min: pub10error,
                        max: pub10error,
                    },
                    luminance: {
                        digits: pub255error,
                        min: pub255error,
                        max: pub255error,
                    },
                    contrast: {
                        digits: pub127error,
                        min: pub127error,
                        max: pub127error,
                    },
                    saturation: {
                        digits: pub127error,
                        min: pub127error,
                        max: pub127error,
                    },
                    chroma: {
                        digits: pub255error,
                        min: pub255error,
                        max: pub255error,
                    },
                    gNSSPositionCollectRate: {
                        digits: numberError,
                    },
                    dataAcceptanceTimeoutTime: {
                        required: pub2000error,
                        digits: pub2000error,
                        min: pub2000error,
                        max: pub2000error,
                    },
                    inflectionPointAdditional: {
                        digits: "请输入0-179之间数字或留空",
                        min: "请输入0-179之间数字或留空",
                        max: "请输入0-179之间数字或留空"
                    },
                    electronicFenceRadius: {
                        digits: "请输入0-65535之间数字或留空",
                        min: "请输入0-65535之间数字或留空",
                        max: "请输入0-65535之间数字或留空"
                    }
                }
            }).form();
        },
        // 下发参数
        sendFuel: function (upgradeType) {
            var url = "/clbs/v/monitoring/commandParam/sendParamByCommandType";
            var parameter = {
                "monitorIds": vid,
                'commandType': commandType,
                "upgradeType": upgradeType === undefined ? '' : upgradeType
            };
            json_ajax("POST", url, "json", true, parameter, commandParamBind.sendFuelCallback);
        },
        sendFuelCallback: function (data) {
            if (data.success) {
                $("#commonWin").modal("hide");
                if (flusendflag) {
                    webSocket.subscribe(headers, "/user/topic/directive_parameter", paramter.updataFenceData, "", null);
                    flusendflag = false;
                }
            } else {
                if (data.msg) {
                    layer.msg(data.msg, {move: false});
                }
            }
            myTable.refresh();
        },
        //读取参数
        getParam: function () {
            isRead = true;
            var url = '/clbs/v/monitoring/commandParam/getParam';
            json_ajax("POST", url, "json", false, {
                "vid": vid,
                "commandType": commandType,
                "videoTactics": $("#videoTactics").val()
            }, commandParamBind.getF3BaseParamCall);
        },
        showErrorMsg: function (msg, inputId) {
            if ($("#error_label_add").is(":hidden")) {
                $("#error_label_add").text(msg);
                $("#error_label_add").insertAfter($("#" + inputId));
                $("#error_label_add").show();
            } else {
                $("#error_label_add").is(":hidden");
            }
        },
        //错误提示信息隐藏
        hideErrorMsg: function () {
            $("#error_label_add").is(":hidden");
            $("#error_label_add").hide();
        },
        // 设置输入框与下拉框禁用状态
        setInputAndSelectDisable: function (parentClass, state) {
            $(parentClass).find('input').prop('disabled', state);
            $(parentClass).find('select').prop('disabled', state);
        },
        connectionControlSH: function () {
            setTimeout(function () {
                if (!($("#connectSpecifyServer").is(":hidden"))) {
                    if ($("#specifyServerConnect").val() == 1) {
                        $(".connectSpecifyInfo").hide();
                    } else {
                        $(".connectSpecifyInfo").show();
                    }
                }
                if (!($("#locationReporting").is(":hidden"))) {
                    if ($("#locationTactics").val() == 0) {
                        $('.allLocationInfo').hide();
                        $('.distanceInfo').hide();
                        $('.timingInfo').show();
                        commandParamBind.setInputAndSelectDisable('.allLocationInfo', true);
                        commandParamBind.setInputAndSelectDisable('.distanceInfo', true);
                        commandParamBind.setInputAndSelectDisable('.timingInfo', false);
                    } else if ($("#locationTactics").val() == 1) {
                        $('.allLocationInfo').hide();
                        $('.distanceInfo').show();
                        $('.timingInfo').hide();
                        commandParamBind.setInputAndSelectDisable('.allLocationInfo', true);
                        commandParamBind.setInputAndSelectDisable('.distanceInfo', false);
                        commandParamBind.setInputAndSelectDisable('.timingInfo', true);
                    } else if ($("#locationTactics").val() == 2) {
                        $('.allLocationInfo').show();
                        $('.distanceInfo').hide();
                        $('.timingInfo').hide();
                        commandParamBind.setInputAndSelectDisable('.allLocationInfo', false);
                        commandParamBind.setInputAndSelectDisable('.distanceInfo', true);
                        commandParamBind.setInputAndSelectDisable('.timingInfo', true);
                    }
                }
                if (!($("#videoCameraParameters").is(":hidden"))) {
                    if ($("#videoTactics").val() == 0) {
                        $('.videoDistance').hide();
                        $('.videoTiming').show();
                    } else if ($("#videoTactics").val() == 1) {
                        $('.videoDistance').show();
                        $('.videoTiming').hide();
                    } else if ($("#videoTactics").val() == 2) {
                        $('.videoDistance').show();
                        $('.videoTiming').show();
                    }
                }
            }, 100);
        },
        //事件设置参考对象功能
        addEventInputOrSelect: function (selectVal) {
            addEventIndex++;
            var html = "<div class='form-group insertDiv' id='eventMain-container_" + addEventIndex + "'>" +
                "<label class='col-md-3 control-label'>" +
                "<label class='text-danger'>*</label> 事件ID：</label>" +
                "<div class='col-md-3'>" +
                "<input type='text' name='eventId' id='eventId_" + addEventIndex + "' value='" + (addEventIndex - 1) + "' class='form-control' onblur='commandParamBind.inputBlur()'></div>" +
                "<label class='col-md-2 control-label'><label class='text-danger'>*</label> 事件内容：</label>" +
                "<div class='col-md-3'>" +
                "<input type='text' name='eventContent' id='eventContent_" + addEventIndex + "' placeholder='请输入事件内容' class='form-control' onblur='commandParamBind.inputBlur()'></div>" +
                "<div class='col-md-1'>";
            if (addEventIndex == '1') {
                html += "<button id='event-add-btn' onclick='commandParamBind.addEventSetting()' type='button' class='btn btn-primary addIcon'><span class='glyphicon glyphiconPlus' aria-hidden='true'></span></button></div></div>";
            }
            // 若操作类型为修改或删除时,事件ID为下拉框
            if (selectVal == '3' || selectVal == '4') {
                html = "<div class='form-group insertDiv' id='eventMain-container_" + addEventIndex + "'><label class='col-md-3 control-label'><label class='text-danger'>*</label> 事件ID：</label><div class='col-md-3'><select id='eventId_" + addEventIndex + "' name='eventId' class='form-control' onchange='commandParamBind.eventSettIdParFn(" + addEventIndex + ")'><option value='1'>1</option><option value='2'>2</option><option value='3'>3</option></select></div><label class='col-md-2 control-label'><label class='text-danger'>*</label> 事件内容：</label><div class='col-md-3'><input type='text' name='eventContent' id='eventContent_" + addEventIndex + "' placeholder='请输入事件内容' class='form-control' onblur='commandParamBind.inputBlur()'></div><div class='col-md-1'>";
                if (addEventIndex == '1') {
                    html += "<button id='event-add-btn' onclick='commandParamBind.addEventSettingIsSelect();' type='button' class='btn btn-primary addIcon'><span class='glyphicon glyphiconPlus' aria-hidden='true'></span></button></div></div>";
                }
            }
            if (addEventIndex > 1) {
                html += "<button type='button' class='btn btn-danger eventSettingDelete deleteIcon'>" +
                    "<span class='glyphicon glyphicon-trash' aria-hidden='true'></span></button>" +
                    "</div></div>";
            }
            $("#eventMain-container").append(html);
            $(".eventSettingDelete").on("click", function () {
                $(this).closest('.insertDiv').remove();
            });
        },
        //事件设置添加事件
        addEventSetting: function () {
            addEventIndex++;
            var html = "<div class='form-group insertDiv' id='eventMain-container_" + addEventIndex + "'><label class='col-md-3 control-label'><label class='text-danger'>*</label> 事件ID：</label><div class='col-md-3'><input type='text' name='eventId' id='eventId_" + addEventIndex + "' value='" + (addEventIndex - 1) + "' class='form-control' onblur='commandParamBind.inputBlur()'></div><label class='col-md-2 control-label'><label class='text-danger'>*</label> 事件内容：</label><div class='col-md-3'><input type='text' name='eventContent' id='eventContent_" + addEventIndex + "' placeholder='请输入事件内容' class='form-control' onblur='commandParamBind.inputBlur()'></div><div class='col-md-1'><button type='button' class='btn btn-danger eventSettingDelete deleteIcon'><span class='glyphicon glyphicon-trash' aria-hidden='true'></span></button></div></div>";
            $("#eventMain-container").append(html);
            $(".eventSettingDelete").on("click", function () {
                $(this).closest('.insertDiv').remove();
            });
        },
        //事件设置添加事件(id为下拉时)
        addEventSettingIsSelect: function () {
            addEventIndex++;
            var html = "<div class='form-group insertDiv' id='eventMain-container_" + addEventIndex + "'><label class='col-md-3 control-label'><label class='text-danger'>*</label> 事件ID：</label><div class='col-md-3'><select id='eventId_" + addEventIndex + "' name='eventId' class='form-control' onchange='commandParamBind.eventSettIdParFn(" + addEventIndex + ")'><option value='1'>1</option><option value='2'>2</option><option value='3'>3</option></select></div><label class='col-md-2 control-label'><label class='text-danger'>*</label> 事件内容：</label><div class='col-md-3'><input type='text' name='eventContent' id='eventContent_" + addEventIndex + "' placeholder='请输入事件内容' class='form-control' onblur='commandParamBind.inputBlur()'></div><div class='col-md-1'><button type='button' class='btn btn-danger eventSettingDelete deleteIcon'><span class='glyphicon glyphicon-trash' aria-hidden='true'></span></button></div></div>";
            $("#eventMain-container").append(html);
            if ($('#eventOperateType').val() == 4) {
                $('.insertDiv').find('label.control-label:eq(1)').hide();
                $('.insertDiv').find('div:eq(1)').hide();
            }
            $(".eventSettingDelete").on("click", function () {
                $(this).closest('.insertDiv').remove();
            });
        },
        //电话本设置参考对象功能
        addPhoneBookInputOrSelect: function (selectVal) {
            addPhoneIndex++;
            var html = "<div id='phoneBook-MainContent_" + addPhoneIndex + "' class='insertDiv'><div class='form-group'><label class='col-md-3 control-label'><label class='text-danger'>*</label> 联系人ID：</label><div class='col-md-3 phoneBookIdInfo'><input type='text' id='phoneBookId_" + addPhoneIndex + "' name='phoneBookId' value='" + (addPhoneIndex - 1) + "' class='form-control' onblur='commandParamBind.inputBlur()'/></div><label class='col-md-2 control-label'><label class='text-danger'>*</label> 联系人：</label><div class='col-md-3'><input type='text' id='phoneBookContact_" + addPhoneIndex + "' name='contact' placeholder='请输入联系人' class='form-control' onblur='commandParamBind.inputBlur()'/></div></div><div class='form-group'><label class='col-md-3 control-label'><label class='text-danger'>*</label> 电话号码：</label><div class='col-md-3'><input type='text' id='phoneBookNumber_" + addPhoneIndex + "' name='phoneNo' placeholder='请输入电话号码' class='form-control' onblur='commandParamBind.inputBlur()' /></div><label class='col-md-2 control-label'><label class='text-danger'>*</label> 呼叫类型：</label><div class='col-md-3'><select id='phoneBookOperateType_" + addPhoneIndex + "' name='callType' class='form-control'><option value='1'>呼入</option><option value='2'>呼出</option><option value='3'>呼入/呼出</option></select></div><div class='col-md-1'>";
            if (addPhoneIndex == '1') {
                html += "<button id='phoneBook-add-btn' type='button' class='btn btn-primary addIcon' onclick='commandParamBind.addPhoneBookEvent()'>" +
                    "<span class='glyphicon glyphiconPlus' aria-hidden='true'></span>" +
                    "</button>" +
                    "</div>" +
                    "</div>";
            }
            // 若操作类型为修改时,联系人ID为下拉框
            if (selectVal == '3') {
                html = "<div class='insertDiv'><div class='form-group'>" +
                    "<label class='col-md-3 control-label'><label class='text-danger'>*</label> 联系人ID：</label>" +
                    "<div class='col-md-3 phoneBookIdInfo'>" +
                    "<select id='phoneBookId_" + addPhoneIndex + "' name='phoneBookId' class='form-control' onchange='commandParamBind.phoneBookSettIdFn()'>" +
                    "<option value='1'>1</option>" +
                    "<option value='2'>2</option>" +
                    "<option value='3'>3</option>" +
                    "</select>" +
                    "</div>" +
                    "<label class='col-md-2 control-label'><label class='text-danger'>*</label> 联系人：</label>" +
                    "<div class='col-md-3'>" +
                    "<input type='text' id='phoneBookContact_" + addPhoneIndex + "' name='contact' placeholder='请输入联系人' class='form-control' onblur='commandParamBind.inputBlur();'/>" +
                    "</div>" +
                    "</div>" +
                    "<div class='form-group'>" +
                    "<label class='col-md-3 control-label'><label class='text-danger'>*</label> 电话号码：</label>" +
                    "<div class='col-md-3'>" +
                    "<input type='text' id='phoneBookNumber_" + addPhoneIndex + "' name='phoneNo' placeholder='请输入电话号码' class='form-control' onblur='commandParamBind.inputBlur();'/>" +
                    "</div>" +
                    "<label class='col-md-2 control-label'><label class='text-danger'>*</label> 呼叫类型：</label>" +
                    "<div class='col-md-3'>" +
                    "<select id='phoneBookOperateType_" + addPhoneIndex + "' name='callType' class='form-control'>" +
                    "<option value='1'>呼入</option>" +
                    "<option value='2'>呼出</option>" +
                    "<option value='3'>呼入/呼出</option>" +
                    "</select>" +
                    "</div>" +
                    "<div class='col-md-1'>";
                if (addPhoneIndex == '1') {
                    html += "<button id='phoneBook-add-btn' type='button' class='btn btn-primary addIcon' onclick='commandParamBind.addPhoneBookEventIsSelect()'>" +
                        "<span class='glyphicon glyphiconPlus' aria-hidden='true'></span>" +
                        "</button>" +
                        "</div>" +
                        "</div>";
                }
            }
            if (addPhoneIndex > 1) {
                html += "<button type='button' class='btn btn-danger phoneBookDelete deleteIcon'>" +
                    "<span class='glyphicon glyphicon-trash' aria-hidden='true'></span></button>" +
                    "</div></div>";
            }
            $("#phoneBook-MainContent").append(html);
            $(".phoneBookDelete").on("click", function () {
                $(this).parent().parent().parent().remove();
            });
        },
        //电话本设置添加事件
        addPhoneBookEvent: function () {
            addPhoneIndex++;
            var html = "<div id='phoneBook-MainContent_" + addPhoneIndex + "' class='insertDiv'><div class='form-group'><label class='col-md-3 control-label'><label class='text-danger'>*</label> 联系人ID：</label><div class='col-md-3 phoneBookIdInfo'><input type='text' id='phoneBookId_" + addPhoneIndex + "' name='phoneBookId' value='" + (addPhoneIndex - 1) + "' class='form-control' onblur='commandParamBind.inputBlur()'/></div><label class='col-md-2 control-label'><label class='text-danger'>*</label> 联系人：</label><div class='col-md-3'><input type='text' id='phoneBookContact_" + addPhoneIndex + "' name='contact' placeholder='请输入联系人' class='form-control' onblur='commandParamBind.inputBlur()'/></div></div><div class='form-group'><label class='col-md-3 control-label'><label class='text-danger'>*</label> 电话号码：</label><div class='col-md-3'><input type='text' id='phoneBookNumber_" + addPhoneIndex + "' name='phoneNo' placeholder='请输入电话号码' class='form-control' onblur='commandParamBind.inputBlur()' /></div><label class='col-md-2 control-label'><label class='text-danger'>*</label> 呼叫类型：</label><div class='col-md-3'><select id='phoneBookOperateType_" + addPhoneIndex + "' name='callType' class='form-control'><option value='1'>呼入</option><option value='2'>呼出</option><option value='3'>呼入/呼出</option></select></div><div class='col-md-1'><button type='button' class='btn btn-danger phoneBookDelete deleteIcon'><span class='glyphicon glyphicon-trash' aria-hidden='true'></span></button></div></div></div>";
            $("#phoneBook-MainContent").append(html);
            $(".phoneBookDelete").on("click", function () {
                $(this).parent().parent().parent().remove();
            });
        },
        //电话本设置添加事件(联系人ID为下拉时)
        addPhoneBookEventIsSelect: function () {
            addPhoneIndex++;
            var html = "<div id='phoneBook-MainContent_" + addPhoneIndex + "' class='insertDiv'><div class='form-group'><label class='col-md-3 control-label'><label class='text-danger'>*</label> 联系人ID：</label><div class='col-md-3 phoneBookIdInfo'><select id='phoneBookId_" + addPhoneIndex + "' name='phoneBookId' class='form-control' onchange='commandParamBind.phoneBookSettIdParFn(" + addPhoneIndex + ")'><option value='1'>1</option><option value='2'>2</option><option value='3'>3</option></select></div><label class='col-md-2 control-label'><label class='text-danger'>*</label> 联系人：</label><div class='col-md-3'><input type='text' id='phoneBookContact_" + addPhoneIndex + "' name='contact' placeholder='请输入联系人' class='form-control' onblur='commandParamBind.inputBlur()'/></div></div><div class='form-group'><label class='col-md-3 control-label'><label class='text-danger'>*</label> 电话号码：</label><div class='col-md-3'><input type='text' id='phoneBookNumber_" + addPhoneIndex + "' name='phoneNo' placeholder='请输入电话号码' class='form-control' onblur='commandParamBind.inputBlur()' /></div><label class='col-md-2 control-label'><label class='text-danger'>*</label> 呼叫类型：</label><div class='col-md-3'><select id='phoneBookOperateType_" + addPhoneIndex + "' name='callType' class='form-control'><option value='1'>呼入</option><option value='2'>呼出</option><option value='3'>呼入/呼出</option></select></div><div class='col-md-1'><button type='button' class='btn btn-danger phoneBookDelete deleteIcon'><span class='glyphicon glyphicon-trash' aria-hidden='true'></span></button></div></div></div>";
            $("#phoneBook-MainContent").append(html);
            $(".phoneBookDelete").on("click", function () {
                $(this).parent().parent().parent().remove();
            });
        },
        //信息点播菜单参考对象功能
        addInfoDemandInputOrSelect: function (selectVal) {
            addInfoDemandIndex++;
            var html = "<div class='insertDiv' id='infoDemand-MainContent_" + addInfoDemandIndex + "'><div class='form-group'>" +
                "<label class='col-md-3 control-label'><label class='text-danger'>*</label> 信息ID：</label>" +
                "<div class='col-md-3'><input type='text' id='infoDemandId_" + addInfoDemandIndex + "' value='" + (addInfoDemandIndex - 1) + "'  name='infoId' class='form-control' onblur='commandParamBind.inputBlur()'/></div>" +
                "<label class='col-md-2 control-label'><label class='text-danger'>*</label> 信息名称：</label>" +
                "<div class='col-md-3'><input type='text' id='infoDemandName_" + addInfoDemandIndex + "'  name='infoContent' placeholder='请输入信息名称' class='form-control' onblur='commandParamBind.inputBlur()'/></div>" +
                "</div>" +
                "<div class='form-group'>" +
                "    <label" +
                "            class='col-md-3 control-label'>" +
                "        <label class='text-danger'>*</label> 信息内容：</label>" +
                "    <div class='col-md-3 infoDemandMenuId'>" +
                "        <input type='text' id='messageContent_" + addInfoDemandIndex + "'" +
                "               name='messageContent' class='form-control'" +
                "               placeholder='请输入信息内容'" +
                "               onblur='commandParamBind.inputBlur()'>" +
                "    </div>" +
                "    <label class='col-md-2 control-label'>" +
                "        <label class='text-danger'>*</label> 自动下发时间(s)：</label>" +
                "    <div class='col-md-3'>" +
                "        <select name='sendFrequency' id='sendFrequency_" + addInfoDemandIndex + "' class='form-control'>" +
                "            <option value='0'>45</option>" +
                "            <option value='1'>60</option>" +
                "            <option value='2'>75</option>" +
                "        </select>" +
                "    </div>" +
                "    <div class='col-md-1'>";
            if (addInfoDemandIndex == '1') {
                html += "<button id='infoDemand-add-btn' type='button' class='btn btn-primary addIcon' onclick='commandParamBind.addInfoDemandEvent()'>" +
                    "<span class='glyphicon glyphiconPlus' aria-hidden='true'></span>" +
                    "</button>" +
                    "</div>" +
                    "</div></div>";
            }
            // 若操作类型为修改时,联系人ID为下拉框
            if (selectVal == '3') {
                html = " class='insertDiv' id='infoDemand-MainContent_" + addInfoDemandIndex + "'<div class='form-group'>" +
                    "<label class='col-md-3 control-label'><label class='text-danger'>*</label> 信息ID：</label>" +
                    "<div class='col-md-3 infoDemandMenuId'>" +
                    "<select id='infoDemandId_" + addInfoDemandIndex + "' name='infoId' class='form-control' onchange='commandParamBind.infoDemandSettIdFn()'>" +
                    "<option value='1'>1</option>" +
                    "<option value='2'>2</option>" +
                    "<option value='3'>3</option>" +
                    "</select>" +
                    "</div>" +
                    "<label class='col-md-2 control-label'><label class='text-danger'>*</label> 信息名称：</label>" +
                    "<div class='col-md-3'>" +
                    "<input type='text' id='infoDemandName_" + addInfoDemandIndex + "' name='infoContent' placeholder='请输入信息名称' class='form-control'  onblur='commandParamBind.inputBlur();'/>" +
                    "</div></div>" +
                    "<div class='form-group'>" +
                    "    <label" +
                    "            class='col-md-3 control-label'>" +
                    "        <label class='text-danger'>*</label> 信息内容：</label>" +
                    "    <div class='col-md-3 infoDemandMenuId'>" +
                    "        <input type='text' id='messageContent_" + addInfoDemandIndex + "'" +
                    "               name='messageContent' class='form-control'" +
                    "               placeholder='请输入信息内容'" +
                    "               onblur='commandParamBind.inputBlur()'>" +
                    "    </div>" +
                    "    <label class='col-md-2 control-label'>" +
                    "        <label class='text-danger'>*</label> 自动下发时间(s)：</label>" +
                    "    <div class='col-md-3'>" +
                    "        <select name='sendFrequency' id='sendFrequency_" + addInfoDemandIndex + "' class='form-control'>" +
                    "            <option value='0'>45</option>" +
                    "            <option value='1'>60</option>" +
                    "            <option value='2'>75</option>" +
                    "        </select>" +
                    "    </div>" +
                    "    <div class='col-md-1'>";
                if (addInfoDemandIndex == '1') {
                    html += "<button id='infoDemand-add-btn' type='button' class='btn btn-primary addIcon' onclick='commandParamBind.addInfoDemandEventIsSelect()'>" +
                        "<span class='glyphicon glyphiconPlus' aria-hidden='true'></span>" +
                        "</button>" +
                        "</div>" +
                        "</div></div>";
                }
            }
            if (addInfoDemandIndex > 1) {
                html += "<button type='button' class='btn btn-danger infoDemandDelete deleteIcon'>" +
                    "<span class='glyphicon glyphicon-trash' aria-hidden='true'></span></button>" +
                    "</div></div></div>";
            }
            $("#infoDemandMenu").append(html);
            $(".infoDemandDelete").on("click", function () {
                $(this).closest('.insertDiv').remove();
            });
        },
        //信息点播菜单添加事件
        addInfoDemandEvent: function () {
            addInfoDemandIndex++;
            var html = "<div class='insertDiv' id='infoDemand-MainContent_" + addInfoDemandIndex + "'><div class='form-group'>" +
                "<label class='col-md-3 control-label'><label class='text-danger'>*</label> 信息ID：</label>" +
                "<div class='col-md-3'><input type='text' id='infoDemandId_" + addInfoDemandIndex + "' value='" + (addInfoDemandIndex - 1) + "'  name='infoId' class='form-control' onblur='commandParamBind.inputBlur()'/></div>" +
                "<label class='col-md-2 control-label'><label class='text-danger'>*</label> 信息名称：</label>" +
                "<div class='col-md-3'>" +
                "<input type='text' id='infoDemandName_" + addInfoDemandIndex + "'  name='infoContent' placeholder='请输入信息名称' class='form-control' onblur='commandParamBind.inputBlur()'/></div>" +
                "</div><div class='form-group'>" +
                "    <label" +
                "            class='col-md-3 control-label'>" +
                "        <label class='text-danger'>*</label> 信息内容：</label>" +
                "    <div class='col-md-3 infoDemandMenuId'>" +
                "        <input type='text' id='messageContent_" + addInfoDemandIndex + "'" +
                "               name='messageContent' class='form-control'" +
                "               placeholder='请输入信息内容'" +
                "               onblur='commandParamBind.inputBlur()'>" +
                "    </div>" +
                "    <label class='col-md-2 control-label'>" +
                "        <label class='text-danger'>*</label> 自动下发时间(s)：</label>" +
                "    <div class='col-md-3'>" +
                "        <select name='sendFrequency' id='sendFrequency_" + addInfoDemandIndex + "' class='form-control'>" +
                "            <option value='0'>45</option>" +
                "            <option value='1'>60</option>" +
                "            <option value='2'>75</option>" +
                "        </select>" +
                "    </div>" +
                "<div class='col-md-1'>" +
                "<button type='button' class='btn btn-danger infoDemandDelete deleteIcon'>" +
                "<span class='glyphicon glyphicon-trash' aria-hidden='true'></span></button>" +
                "</div></div></div>";
            $("#infoDemandMenu").append(html);
            $(".infoDemandDelete").on("click", function () {
                $(this).closest('.insertDiv').remove();
            });
        },
        //信息点播菜单添加事件(信息ID为下拉时)
        addInfoDemandEventIsSelect: function () {
            addInfoDemandIndex++;
            var html = "<div class='insertDiv' id='infoDemand-MainContent_" + addInfoDemandIndex + "'><div class='form-group'>" +
                "<label class='col-md-3 control-label'><label class='text-danger'>*</label> 信息ID：</label>" +
                "<div class='col-md-3'><select id='infoDemandId_" + addInfoDemandIndex + "' name='infoId' class='form-control' onchange='commandParamBind.infoDemandSettIdParFn(" + addInfoDemandIndex + ")'><option value='1'>1</option><option value='2'>2</option><option value='3'>3</option></select></div>" +
                "<label class='col-md-2 control-label'><label class='text-danger'>*</label> 信息名称：</label>" +
                "<div class='col-md-3'><input type='text' id='infoDemandName_" + addInfoDemandIndex + "'  name='infoContent' placeholder='请输入信息名称' class='form-control' onblur='commandParamBind.inputBlur()'/></div>" +
                "</div><div class='form-group'>" +
                "   <label" +
                "            class='col-md-3 control-label'>" +
                "        <label class='text-danger'>*</label> 信息内容：</label>" +
                "    <div class='col-md-3 infoDemandMenuId'>" +
                "        <input type='text' id='messageContent_" + addInfoDemandIndex + "'" +
                "               name='messageContent' class='form-control'" +
                "               placeholder='请输入信息内容'" +
                "               onblur='commandParamBind.inputBlur()'>" +
                "    </div>" +
                "    <label class='col-md-2 control-label'>" +
                "        <label class='text-danger'>*</label> 自动下发时间(s)：</label>" +
                "    <div class='col-md-3'>" +
                "        <select name='sendFrequency' id='sendFrequency_" + addInfoDemandIndex + "' class='form-control'>" +
                "            <option value='0'>45</option>" +
                "            <option value='1'>60</option>" +
                "            <option value='2'>75</option>" +
                "        </select>" +
                "    </div>" +
                "<div class='col-md-1'>" +
                "<button type='button' class='btn btn-danger infoDemandDelete deleteIcon'>" +
                "<span class='glyphicon glyphicon-trash' aria-hidden='true'></span></button>" +
                "</div></div></div>";
            $("#infoDemandMenu").append(html);
            $(".infoDemandDelete").on("click", function () {
                $(this).closest('.insertDiv').remove();
            });
        },
        //基站参数设置 定点时间添加
        addBaseStationEvent: function () {
            var bsfpLength = $("#baseStation-MainContent").find("div.form-group").length;
            var bs = parseInt(bsfpLength) + 1;
            if (bs > 12) {
                layer.msg(commandDesignatedTimeError);
            } else {
                addBaseStationIndex++;
                var html = "<div class='form-group'><label class='col-md-3 control-label'>定点时间：</label><div class='col-md-3'><input type='text' id='baseStationFixedTime_" + addBaseStationIndex + "' name='locationTime' onclick='' class='form-control'/></div><div class='col-md-1'><button type='button' class='btn btn-danger baseStationDelete deleteIcon'><span class='glyphicon glyphicon-trash' aria-hidden='true'></span></button></div><div class='col-md-5'></div></div>";
                $("#baseStation-MainContent").append(html);
                laydate.render({elem: '#baseStationFixedTime_' + addBaseStationIndex, type: 'time', theme: '#6dcff6'});
                $("#baseStationFixedTime_" + addBaseStationIndex).val(loadTime);
                $(".baseStationDelete").on("click", function () {
                    $(this).parent().parent().remove();
                });
            }
        },
        //事件设置  操作类型
        eventSettingOperateType: function () {
            addEventIndex = 2;
            var eventOperateTypeValue = $("#eventOperateType").find("option:selected").val();
            //更新 追加
            $('.insertDiv').show();
            $('.insertDiv *').show();
            if (eventOperateTypeValue == 1 || eventOperateTypeValue == 2) {
                commandParamBind.resetEventSetting();
                var emcLength = $("#eventMain-container").children("div").length;
                if (emcLength > 1) {
                    $("#eventMain-container>div.form-group").each(function (i) {
                        if (i > 0) {
                            $(this).remove();
                        }
                    });
                }
                $(".eventIdInfo").find("input").removeAttr("disabled", "disabled");
                $("#event-add-btn").removeAttr("disabled", "disabled");
                $("#eventContent_2").val("");
            }
            //修改 id为下拉
            else if (eventOperateTypeValue == 3) {
                commandParamBind.eventSettingUpdateOrDel();
            }
            //删除 id为下拉
            else if (eventOperateTypeValue == 4) {
                commandParamBind.eventSettingUpdateOrDel();
                $('.insertDiv').find('label.control-label:eq(1)').hide();
                $('.insertDiv').find('div:eq(1)').hide();
            } else {
                commandParamBind.resetEventSetting();
                $('.insertDiv').hide();
                $(".eventIdInfo").find("input").removeAttr("disabled", "disabled");
                $("#event-add-btn").removeAttr("disabled", "disabled");
            }
        },
        //事件设置  修改删除时调用
        eventSettingUpdateOrDel: function () {
            addEventIndex = 2;
            $("#eventMain-container").html("");
            var html =
                "<div class='form-group insertDiv'>" +
                "<label class='col-md-3 control-label'><label class='text-danger'>*</label> 事件ID：</label>" +
                "<div class='col-md-3 eventIdInfo'>" +
                "<select id='eventId_2' name='eventId' class='form-control' onchange='commandParamBind.eventSettIdFn()'>" +
                "<option value='1'>1</option>" +
                "<option value='2'>2</option>" +
                "<option value='3'>3</option>" +
                "</select>" +
                "</div>" +
                "<label class='col-md-2 control-label'><label class='text-danger'>*</label> 事件内容：</label>" +
                "<div class='col-md-3'><input type='text' id='eventContent_2' name='eventContent' placeholder='请输入事件内容' class='form-control' onblur='commandParamBind.inputBlur()'></div>" +
                "<div class='col-md-1'><button id='event-add-btn' onclick='commandParamBind.addEventSettingIsSelect();' type='button' class='btn btn-primary addIcon'><span class='glyphicon glyphiconPlus' aria-hidden='true'></span></button></div>" +
                "</div>";
            $("#eventMain-container").append(html);
        },
        //事件设置 操作类型其他选项还原
        resetEventSetting: function () {
            $("#eventMain-container").html("");
            var html =
                "<div class='form-group insertDiv'>" +
                "<label class='col-md-3 control-label'><label class='text-danger'>*</label> 事件ID：</label>" +
                "<div class='col-md-3 eventIdInfo'>" +
                "<input id='eventId_2' type='text' name='eventId' value='1' class='form-control' onblur='commandParamBind.inputBlur();'/>" +
                "</div>" +
                "<label class='col-md-2 control-label'><label class='text-danger'>*</label> 事件内容：</label>" +
                "<div class='col-md-3'><input type='text' id='eventContent_2' name='eventContent' placeholder='请输入事件内容' class='form-control' onblur='commandParamBind.inputBlur()'></div>" +
                "<div class='col-md-1'><button id='event-add-btn' onclick='commandParamBind.addEventSetting();' type='button' class='btn btn-primary addIcon'><span class='glyphicon glyphiconPlus' aria-hidden='true'></span></button></div>" +
                "</div>";
            $("#eventMain-container").append(html);
        },
        //事件设置  事件ID选择改变
        eventSettIdFn: function () {
            var eIdVal = $("#eventId_2").find("option:selected").val();
            // layer.msg(eIdVal);
        },
        //事件设置  事件ID选择改变(带参数)
        eventSettIdParFn: function (id) {
            var eIdVal = $("#eventId_" + id).find("option:selected").val();
            // layer.msg(eIdVal);
        },
        //电话本设置 操作类型
        phoneBookSettingOperateType: function () {
            addPhoneIndex = 2;
            var phoneBookOperateTypeValue = $("#phoneBookOperateType").find("option:selected").val();
            //更新  追加
            $('.insertDiv').show();
            if (phoneBookOperateTypeValue == 1 || phoneBookOperateTypeValue == 2) {
                var pbmLength = $("#phoneBook-MainContent").children("div").length;
                if (pbmLength > 2) {
                    $("#phoneBook-MainContent>div").each(function (j) {
                        if (j > 1) {
                            $(this).remove();
                        }
                    });
                }
                commandParamBind.resetPhoneBookSetting();
                $("#phoneBook-add-btn").removeAttr("disabled", "disabled");
                $("#phoneBookContact_2,#phoneBookNumber_2").val("");
            }
            //修改 联系人ID下拉
            else if (phoneBookOperateTypeValue == 3) {
                $("#phoneBook-MainContent").html("");
                var html =
                    "<div class='insertDiv'><div class='form-group'>" +
                    "<label class='col-md-3 control-label'><label class='text-danger'>*</label> 联系人ID：</label>" +
                    "<div class='col-md-3 phoneBookIdInfo'>" +
                    "<select id='phoneBookId_2' name='phoneBookId' class='form-control' onchange='commandParamBind.phoneBookSettIdFn()'>" +
                    "<option value='1'>1</option>" +
                    "<option value='2'>2</option>" +
                    "<option value='3'>3</option>" +
                    "</select>" +
                    "</div>" +
                    "<label class='col-md-2 control-label'><label class='text-danger'>*</label> 联系人：</label>" +
                    "<div class='col-md-3'>" +
                    "<input type='text' id='phoneBookContact_2' name='contact' placeholder='请输入联系人' class='form-control' onblur='commandParamBind.inputBlur();'/>" +
                    "</div>" +
                    "</div>" +
                    "<div class='form-group'>" +
                    "<label class='col-md-3 control-label'><label class='text-danger'>*</label> 电话号码：</label>" +
                    "<div class='col-md-3'>" +
                    "<input type='text' id='phoneBookNumber_2' name='phoneNo' placeholder='请输入电话号码' class='form-control' onblur='commandParamBind.inputBlur();'/>" +
                    "</div>" +
                    "<label class='col-md-2 control-label'><label class='text-danger'>*</label> 呼叫类型：</label>" +
                    "<div class='col-md-3'>" +
                    "<select id='phoneBookOperateType_2' name='callType' class='form-control'>" +
                    "<option value='1'>呼入</option>" +
                    "<option value='2'>呼出</option>" +
                    "<option value='3'>呼入/呼出</option>" +
                    "</select>" +
                    "</div>" +
                    "<div class='col-md-1'>" +
                    "<button id='phoneBook-add-btn' type='button' class='btn btn-primary addIcon' onclick='commandParamBind.addPhoneBookEventIsSelect()'>" +
                    "<span class='glyphicon glyphiconPlus' aria-hidden='true'></span>" +
                    "</button>" +
                    "</div>" +
                    "</div></div>";
                $("#phoneBook-MainContent").append(html);
            }
            //其他选项
            else {
                commandParamBind.resetPhoneBookSetting();
                $('.insertDiv').hide();
                $(".phoneBookIdInfo").find("input").removeAttr("disabled", "disabled");
                $("#phoneBook-add-btn").removeAttr("disabled", "disabled");
            }
        },
        //电话本设置 操作类型其他选项还原
        resetPhoneBookSetting: function () {
            $("#phoneBook-MainContent").html("");
            var html =
                "<div class='insertDiv'>" +
                "<div class='form-group'>" +
                "<label class='col-md-3 control-label'><label class='text-danger'>*</label> 联系人ID：</label>" +
                "<div class='col-md-3 phoneBookIdInfo'>" +
                "<input type='text' id='phoneBookId_2' name='phoneBookId' value='1' class='form-control' onblur='commandParamBind.inputBlur();'/>	" +
                "</div>" +
                "<label class='col-md-2 control-label'><label class='text-danger'>*</label> 联系人：</label>" +
                "<div class='col-md-3'>" +
                "<input type='text' id='phoneBookContact_2' name='contact' placeholder='请输入联系人' class='form-control' onblur='commandParamBind.inputBlur();'/>	" +
                "</div>" +
                "</div>" +
                "<div class='form-group'>" +
                "<label class='col-md-3 control-label'><label class='text-danger'>*</label> 电话号码：</label>" +
                "<div class='col-md-3'>" +
                "<input type='text' id='phoneBookNumber_2' name='phoneNo' placeholder='请输入电话号码' class='form-control' onblur='commandParamBind.inputBlur();'/>" +
                "</div>" +
                "<label class='col-md-2 control-label'><label class='text-danger'>*</label> 呼叫类型：</label>" +
                "<div class='col-md-3'>" +
                "<select id='phoneBookOperateType_2' name='callType' class='form-control'>" +
                "<option value='1'>呼入</option>" +
                "<option value='2'>呼出</option>" +
                "<option value='3'>呼入/呼出</option>" +
                "</select>" +
                "</div>" +
                "<div class='col-md-1'>" +
                "<button id='phoneBook-add-btn' type='button' class='btn btn-primary addIcon' onclick='commandParamBind.addPhoneBookEvent()'>" +
                "<span class='glyphicon glyphiconPlus' aria-hidden='true'></span>" +
                "</button>" +
                "</div>" +
                "</div>";
            "</div>";
            $("#phoneBook-MainContent").append(html);
        },
        //电话本设置 操作类型其他选项还原
        addPhoneBookSetting: function () {
            $("#phoneBook-MainContent").html("");
            var html =
                "<div class='form-group'>" +
                "<label class='col-md-3 control-label'><label class='text-danger'>*</label> 联系人ID：</label>" +
                "<div class='col-md-3 phoneBookIdInfo'>" +
                "<input type='text' id='phoneBookId_2' name='phoneBookId' value='1' class='form-control' onblur='commandParamBind.inputBlur();'/>	" +
                "</div>" +
                "<label class='col-md-2 control-label'><label class='text-danger'>*</label> 联系人：</label>" +
                "<div class='col-md-3'>" +
                "<input type='text' id='phoneBookContact_2' name='contact' placeholder='请输入联系人' class='form-control' onblur='commandParamBind.inputBlur();'/>	" +
                "</div>" +
                "</div>" +
                "<div class='form-group'>" +
                "<label class='col-md-3 control-label'><label class='text-danger'>*</label> 电话号码：</label>" +
                "<div class='col-md-3'>" +
                "<input type='text' id='phoneBookNumber_2' name='phoneNo' placeholder='请输入电话号码' class='form-control' onblur='commandParamBind.inputBlur();'/>" +
                "</div>" +
                "<label class='col-md-2 control-label'><label class='text-danger'>*</label> 呼叫类型：</label>" +
                "<div class='col-md-3'>" +
                "<select id='phoneBookOperateType_2' name='callType' class='form-control'>" +
                "<option value='1'>呼入</option>" +
                "<option value='2'>呼出</option>" +
                "<option value='3'>呼入/呼出</option>" +
                "</select>" +
                "</div>" +
                "<div class='col-md-1'>" +
                "<button id='phoneBook-add-btn' type='button' class='btn btn-primary addIcon' onclick='commandParamBind.addPhoneBookEvent()'>" +
                "<span class='glyphicon glyphiconPlus' aria-hidden='true'></span>" +
                "</button>" +
                "</div>" +
                "</div>";
            $("#phoneBook-MainContent").append(html);
        },
        //电话本设置 联系人ID改变
        phoneBookSettIdFn: function () {
            var pbIdVal = $("#phoneBookId_2").find("option:selected").val();
            // layer.msg("联系人ID-" + pbIdVal);
        },
        //电话本设置 联系人ID改变(带参数)
        phoneBookSettIdParFn: function (id) {
            var pbIdVal = $("#phoneBookId_" + id).find("option:selected").val();
            // layer.msg("联系人ID-" + pbIdVal);
        },
        //信息点播菜单 操作类型
        infoDemandMenuSettingOperateType: function () {
            addInfoDemandIndex = 2;
            var infoDemandOperateTypeValue = $("#infoDemandOperateType").find("option:selected").val();
            $('.insertDiv').show();
            //更新 追加
            if (infoDemandOperateTypeValue == 1 || infoDemandOperateTypeValue == 2) {
                var iddLength = $("#infoDemandMenu").children("div").length;
                if (iddLength > 3) {
                    $("#infoDemandMenu>div").each(function (k) {
                        if (k > 2) {
                            $(this).remove();
                        }
                    });
                }
                commandParamBind.resetInfoDemandMenuSetting();
                $("#infoDemand-add-btn").removeAttr("disabled", "disabled");
                $("#infoDemandName_2").val("");
            }
            //修改 信息id下拉
            else if (infoDemandOperateTypeValue == 3) {
                var idmLength = $("#infoDemandMenu").find("div.form-group").length;
                if (idmLength > 1) {
                    $("#infoDemandMenu>div").each(function (k) {
                        if (k > 1) {
                            $(this).remove();
                        }
                        if (k == 1) {
                            $(this).hide();
                        }
                    });
                    var html =
                        "<div class='insertDiv' id='infoDemand-MainContent'><div class='form-group'>" +
                        "<label class='col-md-3 control-label'><label class='text-danger'>*</label> 信息ID：</label>" +
                        "<div class='col-md-3 infoDemandMenuId'>" +
                        "<select id='infoDemandId_2' name='infoId' class='form-control' onchange='commandParamBind.infoDemandSettIdFn()'>" +
                        "<option value='1'>1</option>" +
                        "<option value='2'>2</option>" +
                        "<option value='3'>3</option>" +
                        "</select>" +
                        "</div>" +
                        "<label class='col-md-2 control-label'><label class='text-danger'>*</label> 信息名称：</label>" +
                        "<div class='col-md-3'>" +
                        "<input type='text' id='infoDemandName_2' name='infoContent' placeholder='请输入信息名称' class='form-control'  onblur='commandParamBind.inputBlur();'/>" +
                        "</div></div>" +
                        "<div class='form-group'>" +
                        "    <label" +
                        "            class='col-md-3 control-label'>" +
                        "        <label class='text-danger'>*</label> 信息内容：</label>" +
                        "    <div class='col-md-3 infoDemandMenuId'>" +
                        "        <input type='text' id='messageContent'" +
                        "               name='messageContent' class='form-control'" +
                        "               placeholder='请输入信息内容'" +
                        "               onblur='commandParamBind.inputBlur()'>" +
                        "    </div>" +
                        "    <label class='col-md-2 control-label'>" +
                        "        <label class='text-danger'>*</label> 自动下发时间(s)：</label>" +
                        "    <div class='col-md-3'>" +
                        "        <select name='sendFrequency' id='sendFrequency' class='form-control'>" +
                        "            <option value='0'>45</option>" +
                        "            <option value='1'>60</option>" +
                        "            <option value='2'>75</option>" +
                        "        </select>" +
                        "    </div>" +
                        "    <div class='col-md-1'>" +
                        "        <button id='infoDemand-add-btn' onclick='commandParamBind.addInfoDemandEventIsSelect()'" +
                        "                type='button' class='btn btn-primary addIcon'><span" +
                        "                class='glyphicon glyphiconPlus' aria-hidden='true'></span></button>" +
                        "    </div>" +
                        "</div></div>";
                    $("#infoDemandMenu").append(html);
                }
            } else {
                commandParamBind.resetInfoDemandMenuSetting();
                $('.insertDiv').hide();
                $(".infoDemandMenuId").find("input").removeAttr("disabled", "disabled");
                $("#infoDemand-add-btn").removeAttr("disabled", "disabled");
            }
        },
        //信息点播菜单  操作类型其他选项还原
        resetInfoDemandMenuSetting: function () {
            var idmLength = $("#infoDemandMenu").find("div.form-group").length;
            if (idmLength > 2) {
                $("#infoDemandMenu>div").each(function (k) {
                    if (k > 0) {
                        $(this).remove();
                    }
                });
                var html =
                    "<div class='insertDiv' id='infoDemand-MainContent'><div class='form-group'>" +
                    "<label class='col-md-3 control-label'><label class='text-danger'>*</label> 信息ID：</label>" +
                    "<div class='col-md-3 infoDemandMenuId'>" +
                    "<input type='text' id='infoDemandId_2' name='infoId' value='1' class='form-control' onblur='commandParamBind.inputBlur();'/>" +
                    "</div>" +
                    "<label class='col-md-2 control-label'><label class='text-danger'>*</label> 信息名称：</label>" +
                    "<div class='col-md-3'>" +
                    "<input type='text' id='infoDemandName_2' name='infoContent' placeholder='请输入信息名称' class='form-control'  onblur='commandParamBind.inputBlur();'/>" +
                    "</div>" +
                    "</div><div class='form-group'>" +
                    "   <label" +
                    "            class='col-md-3 control-label'>" +
                    "        <label class='text-danger'>*</label> 信息内容：</label>" +
                    "    <div class='col-md-3 infoDemandMenuId'>" +
                    "        <input type='text' id='messageContent_" + addInfoDemandIndex + "'" +
                    "               name='messageContent' class='form-control'" +
                    "               placeholder='请输入信息内容'" +
                    "               onblur='commandParamBind.inputBlur()'>" +
                    "    </div>" +
                    "    <label class='col-md-2 control-label'>" +
                    "        <label class='text-danger'>*</label> 自动下发时间(s)：</label>" +
                    "    <div class='col-md-3'>" +
                    "        <select name='sendFrequency' id='sendFrequency_" + addInfoDemandIndex + "' class='form-control'>" +
                    "            <option value='0'>45</option>" +
                    "            <option value='1'>60</option>" +
                    "            <option value='2'>75</option>" +
                    "        </select>" +
                    "    </div>" +
                    "<div class='col-md-1'>" +
                    "<button id='infoDemand-add-btn' type='button' class='btn btn-primary addIcon' onclick='commandParamBind.addInfoDemandEvent()'>" +
                    "<span class='glyphicon glyphiconPlus' aria-hidden='true'></span>" +
                    "</button>" +
                    "</div>" +
                    "</div></div>";
                $("#infoDemandMenu").append(html);
            }
        },
        //信息点播菜单 信息ID改变
        infoDemandSettIdFn: function () {
            var idIdVal = $("#infoDemandId_2").find("option:selected").val();
            // layer.msg("信息ID-" + idIdVal);
        },
        //信息点播菜单 信息ID改变(带参数)
        infoDemandSettIdParFn: function (id) {
            var idIdVal = $("#infoDemandId_" + id).find("option:selected").val();
            // layer.msg("信息ID-" + idIdVal);
        },
        //基站参数设置 上报模式
        baseStationReportModeCheckFn: function () {
            var baseStationReportModeTypeValue = $("#baseStationReportMode").find("option:selected").val();
            if (baseStationReportModeTypeValue == 0) {
                $("#baseStationStartTimePoint,#baseStationReportInterval").removeAttr("disabled", "disabled");
                $("#baseStationFixedTime").attr("disabled", "disabled");
                $("input[name='locationTime']").attr("disabled", "disabled");
                $("#baseStation-add-btn").hide();
            } else if (baseStationReportModeTypeValue == 1) {
                $("#baseStationFixedTime").removeAttr("disabled");
                $("input[name='locationTime']").removeAttr("disabled");
                $("#baseStation-add-btn").show();
                $("#baseStationStartTimePoint,#baseStationReportInterval").attr("disabled", "disabled");
            }
        },


        //当前时间(时分秒)
        getHoursMinuteSeconds: function () {
            var nowDate = new Date();
            loadTime =
                +(nowDate.getHours() < 10 ? "0" + nowDate.getHours() : nowDate.getHours())
                + ":"
                + (nowDate.getMinutes() < 10 ? "0" + nowDate.getMinutes() : nowDate.getMinutes())
                + ":"
                + (nowDate.getSeconds() < 10 ? "0" + nowDate.getSeconds() : nowDate.getSeconds());
            $("#baseStationStartTimePoint,#baseStationFixedTime").val(loadTime);
        },
        inputBlur: function () {
            commandParamBind.hideErrorMsg();
        },

        //处理获取设备上传数据
        getSensor0104Param: function (msg) {
            if (msg == null) {
                return;
            }
            var result = $.parseJSON(msg.body);
            if (vid !== result.desc.monitorId) return;
            isRead = false;
            clearTimeout(_timeout);
            if (layer_time) {
                clearTimeout(layer_time);
            }
            layer.closeAll();
            $("#readButton").html("读取").prop('disabled', false);
            $("#sendButton").prop('disabled', false);
            if (commandType == "23") {
                var params = result.data.msgBody;
                commandParamBind.queryF3BaseParamCall(params);
            } else {
                var params = result.data.msgBody.params;
                if (params) {
                    for (var i = 0; i < params.length; i++) {
                        commandParamBind.queryF3BaseParamCall(params[i]);
                    }
                }
            }
            if (commandType == "17") {
                if (isCameraTimeFlag && isCameraDistanceFlag) {
                    $("#videoTactics").val(2);
                } else if (isCameraTimeFlag && !isCameraDistanceFlag) {
                    $("#videoTactics").val(0);
                } else if (!isCameraTimeFlag && isCameraDistanceFlag) {
                    $("#videoTactics").val(1);
                }
                isCameraTimeFlag = false; //定时定距读取判断
                isCameraDistanceFlag = false;
                commandParamBind.connectionControlSH();
            }
        },

        //基本信息-上报获取基本信息返回处理方法
        queryF3BaseParamCall: function (param) {
            if (commandType == "23") {
                var deviceType = param.deviceType.toString(2);
                var len = deviceType.length;
                for (var i = len; i < 8; i++) {
                    deviceType = "0" + deviceType;
                }
                $("#terminalType0").val(deviceType.substring(7, 8) == 0 ? "不适用客运车辆" : "适用客运车辆");
                $("#terminalType1").val(deviceType.substring(6, 7) == 0 ? "不适用危险品车辆" : "适用危险品车辆");
                $("#terminalType2").val(deviceType.substring(5, 6) == 0 ? "不适用普通货运车辆" : "适用普通货运车辆");
                $("#terminalType3").val(deviceType.substring(4, 5) == 0 ? "不适用出租车辆" : "适用出租车辆");
                $("#terminalType6").val(deviceType.substring(1, 2) == 0 ? "不支持硬盘录像" : "支持硬盘录像");
                $("#terminalType7").val(deviceType.substring(0, 1) == 0 ? "一体机" : "分体机");
                $("#terminalType").val(param.deviceModule);
                $("#manufacturerID").val(param.manufacturerId);
                $("#terminalSIM").val(param.iCCID);
                $("#terminalID").val(param.deviceNumber);
                $("#terminalHardwareVersionNum").val(param.deviceHardwareVersions);
                $("#terminalFirmwareVersionNum").val(param.deviceFirmwareVersions);
                var GNSSModule = param.gNSSModuleParam.toString(2);
                len = GNSSModule.length;
                for (var i = len; i < 4; i++) {
                    GNSSModule = "0" + GNSSModule;
                }
                $("#GNSSModuleAttribute0").val(GNSSModule.substring(3, 4) == 0 ? "不支持 GPS 定位" : "支持 GPS 定位");
                $("#GNSSModuleAttribute1").val(GNSSModule.substring(2, 3) == 0 ? "不支持北斗定位" : "支持北斗定位");
                $("#GNSSModuleAttribute2").val(GNSSModule.substring(1, 2) == 0 ? "不支持 GLONASS 定位" : "支持 GLONASS 定位");
                $("#GNSSModuleAttribute3").val(GNSSModule.substring(0, 1) == 0 ? "不支持 GLONASS 定位" : "支持 GLONASS 定位");
                var communicationModuleParam = param.communicationModuleParam.toString(2);
                len = communicationModuleParam.length;
                for (var i = len; i < 8; i++) {
                    communicationModuleParam = "0" + communicationModuleParam;
                }
                $("#communicationModuleAttribute0").val(communicationModuleParam.substring(7, 8) == 0 ? "不支持GPRS通信" : "支持GPRS通信");
                $("#communicationModuleAttribute1").val(communicationModuleParam.substring(6, 7) == 0 ? "不支持CDMA通信" : "支持CDMA通信");
                $("#communicationModuleAttribute2").val(communicationModuleParam.substring(5, 6) == 0 ? "不支持TD-SCDMA通信" : "支持TD-SCDMA通信");
                $("#communicationModuleAttribute3").val(communicationModuleParam.substring(4, 5) == 0 ? "不支持WCDMA通信" : "支持WCDMA通信");
                $("#communicationModuleAttribute4").val(communicationModuleParam.substring(3, 4) == 0 ? "不支持CDMA2000通信" : "支持CDMA2000通信");
                $("#communicationModuleAttribute5").val(communicationModuleParam.substring(1, 2) == 0 ? "不支持TD-LTE通信" : "支持TD-LTE通信");
                $("#communicationModuleAttribute7").val(communicationModuleParam.substring(0, 1) == 0 ? "不支持其他通信方式" : "支持其他通信方式");
            } else {
                var item = $(".infoItem:visible");
                switch (param.id) {
                    case 0x0001:// 终端心跳发送间隔，单位为秒（s）
                        $("#terminalSendTime").val(param.value);
                        break;
                    case 0x0002:// TCP 消息应答超时时间，单位为秒（s）
                        $("#terminalAnswerTime").val(param.value);
                        break;
                    case 0x0003:// TCP 消息重传次数
                        $("#terminalAnswerTcp").val(param.value);
                        break;
                    case 0x0004:// UDP 消息应答超时时间，单位为秒（s）
                        $("#terminalAnswerUdp").val(param.value);
                        break;
                    case 0x0005:// UDP 消息重传次数
                        $("#terminalUdpNum").val(param.value);
                        break;
                    case 0x0006:// SMS 消息应答超时时间，单位为秒（s）
                        $("#terminalAnswerSms").val(param.value);
                        break;
                    case 0x0007:// SMS 消息重传次数
                        $("#terminalSmsNum").val(param.value);
                        break;
                    case 0x0010:// 主服务器APN，无线通信拨号访问点。若网络制式为CDMA，则该处为PPP 拨号号码
                        $("#reportMainApn").val(param.value);
                        break;
                    case 0x0011:// 主服务器无线通信拨号用户名
                        $("#reportServerAccound").val(param.value);
                        break;
                    case 0x0012:// 主服务器无线通信拨号密码
                        $("#reportServerPwd").val(param.value);
                        break;
                    case 0x0013:// 主服务器地址,IP 或域名
                        $("#reportMainAddress").val(param.value);
                        break;
                    case 0x0014:// 备份服务器APN，无线通信拨号访问点
                        $("#reportBackupApn").val(param.value);
                        break;
                    case 0x0015:// 备份服务器无线通信拨号用户名
                        $("#reportBackupAccound").val(param.value);
                        break;
                    case 0x0016:// 备份服务器无线通信拨号密码
                        $("#reportBackupPwd").val(param.value);
                        break;
                    case 0x0017:// 备份服务器地址,IP 或域名
                        $("#reportBackupAddress").val(param.value);
                        break;
                    case 0x0018:// 服务器TCP 端口
                        $("#reportServerTcp").val(param.value);
                        break;
                    case 0x0019:// 服务器UDP 端口
                        $("#reportServerUdp").val(param.value);
                        break;
                    case 0x0020:// 位置汇报策略，0：定时汇报；1：定距汇报；2：定时和定距汇报
                        $("#locationTactics").val(param.value);
                        commandParamBind.connectionControlSH();
                        break;
                    case 0x0021:// 位置汇报方案，0：根据ACC 状态； 1：根据登录状态和ACC
                        // 状态，先判断登录状态，若登录再根据ACC 状态
                        $("#locationProgram").val(param.value);
                        break;
                    case 0x0022:// 驾驶员未登录汇报时间间隔，单位为秒（s），>0
                        $(".locationNoLogin").val(param.value);
                        break;
                    case 0x0023:// 从服务器APN
                        $("#slaveServerApn").val(param.value);
                        break;
                    case 0x0024:// 从服务器无线通信拨号用户名
                        $("#slaveServerCallUserName").val(param.value);
                        break;
                    case 0x0025:// 从服务器无线通信拨号密码
                        $("#slaveServerCallUserPwd").val(param.value);
                        break;
                    case 0x0026:// 从服务器地址
                        $("#slaveServerAddress").val(param.value);
                        break;
                    case 0x0027:// 休眠时汇报时间间隔，单位为秒（s），>0
                        $(".locationSleep").val(param.value);
                        break;
                    case 0x0028:// 紧急报警时汇报时间间隔，单位为秒（s），>0
                        $(".locationAlarmTime").val(param.value);
                        break;
                    case 0x0029:// 缺省时间汇报间隔，单位为秒（s），>0
                        $(".locationDefaultTime").val(param.value);
                        break;
                    case 0x002C:// 缺省距离汇报间隔，单位为米（m），>0
                        $(".locationDefaultDistance").val(param.value);
                        break;
                    case 0x002D:// 驾驶员未登录汇报距离间隔，单位为米（m），>0
                        $(".locationNoLoginDistance").val(param.value);
                        break;
                    case 0x002E:// 休眠时汇报距离间隔，单位为米（m），>0
                        $(".locationSleepDistance").val(param.value);
                        break;
                    case 0x002F:// 紧急报警时汇报距离间隔，单位为米（m），>0
                        $(".locationAlarmDistance").val(param.value);
                        break;
                    case 0x0030:// 拐点补传角度，<180
                        $("#inflectionPointAdditional").val(param.value);
                        break;
                    case 0x0031:// 电子围栏半径（非法位移阈值），单位为米
                        $("#electronicFenceRadius").val(param.value);
                        break;
                    case 0x0040:// 监控平台电话号码
                        $("#telephoneNumber").val(param.value);
                        break;
                    case 0x0041:// 复位电话号码，可采用此电话号码拨打终端电话让终端复位
                        $("#telephoneResetNumber").val(param.value);
                        break;
                    case 0x0042:// 恢复出厂设置电话号码，可采用此电话号码拨打终端电话让终端恢复出厂设置
                        $("#telephoneSetNumber").val(param.value);
                        break;
                    case 0x0043:// 监控平台SMS 电话号码
                        $("#telephoneSnsNumber").val(param.value);
                        break;
                    case 0x0044:// 接收终端SMS 文本报警号码
                        $("#telephoneSnsAlarm").val(param.value);
                        break;
                    case 0x0045:// 终端电话接听策略，0：自动接听；1：ACC ON 时自动接听，OFF 时手动接听
                        $("#telephoneStrategy").val(param.value);
                        break;
                    case 0x0046:// 每次最长通话时间，单位为秒（s），0 为不允许通话，0xFFFFFFFF 为不限制
                        //                    intValue = (int) obj.get("value");
                        $("#telephoneMaxTime").val(param.value);
                        break;
                    case 0x0047:// 当月最长通话时间，单位为秒（s），0 为不允许通话，0xFFFFFFFF 为不限制
                        //                    intValue = (int) obj.get("value");
                        $("#telephoneMonthTime").val(param.value);
                        break;
                    case 0x0048:// 监听电话号码
                        $("#telephoneMonitor").val(param.value);
                        break;
                    case 0x0049:// 监管平台特权短信号码
                        $("#monitorPrivilege").val(param.value);
                        break;
                    /*    case 0x0050:// 报警屏蔽字，与位置信息汇报消息中的报警标志相对应，相应位为1则相应报警被屏蔽
                     if (obj.get("value") instanceof Integer) {
                     intValue = (int)
                     obj.get("value");
                     text += "报警屏蔽字：" + intValue + "<br/>";
                     } else {
                     longValue = (long)
                     obj.get("value");
                     text += "报警屏蔽字：" + longValue + "<br/>";
                     }
                     break;
                     case 0x0051:// 报警发送文本SMS 开关，与位置信息汇报消息中的报警标志相对应，相应位为1
                     // 则相应报警时发送文本SMS
                     $("#terminalSendTime").val(param.value);
                     break;
                     case 0x0052:// 报警拍摄开关，与位置信息汇报消息中的报警标志相对应，相应位为1 则相应报警时摄像头拍摄
                     $("#terminalSendTime").val(param.value);
                     break;
                     case 0x0053:// 报警拍摄存储标志，与位置信息汇报消息中的报警标志相对应，相应位为1
                     // 则对相应报警时拍的照片进行存储，否则实时上传
                     $("#terminalSendTime").val(param.value);
                     break;
                     case 0x0054:// 关键标志，与位置信息汇报消息中的报警标志相对应，相应位为1 则对相应报警为关键报警
                     $("#terminalSendTime").val(param.value);
                     break;
                     case 0x0055:// 最高速度，单位为公里每小时（km/h）
                     $("#terminalSendTime").val(param.value);
                     break;
                     case 0x0056:// 超速持续时间，单位为秒（s）
                     $("#terminalSendTime").val(param.value);
                     break;
                     case 0x0057:// 连续驾驶时间门限，单位为秒（s）
                     $("#terminalSendTime").val(param.value);
                     break;
                     case 0x0058:// 当天累计驾驶时间门限，单位为秒（s）
                     $("#terminalSendTime").val(param.value);
                     break;
                     case 0x0059:// 最小休息时间，单位为秒（s）
                     $("#terminalSendTime").val(param.value);
                     break;
                     case 0x005A:// 最长停车时间，单位为秒（s）
                     $("#terminalSendTime").val(param.value);
                     break;*/
                    case 0x0064:// 定时拍照控制   转二进制  取位判断
                        isCameraTimeFlag = true;
                        var time = param.value.toString(2);
                        var len = time.length;
                        for (var i = len; i < 32; i++) {
                            //补全二进制
                            time = "0" + time;
                        }
                        $("#videoCameraSwitchOne").val(time.substring(31, 32));
                        $("#videoCameraSwitchTwo").val(time.substring(30, 31));
                        $("#videoCameraSwitchThree").val(time.substring(29, 30));
                        $("#videoCameraSwitchFour").val(time.substring(28, 29));
                        $("#videoCameraSwitchFive").val(time.substring(27, 28));
                        $("#videoCameraStorageOne").val(time.substring(23, 24));
                        $("#videoCameraStorageTwo").val(time.substring(22, 23));
                        $("#videoCameraStorageThree").val(time.substring(21, 22));
                        $("#videoCameraStorageFour").val(time.substring(20, 21));
                        $("#videoCameraStorageFive").val(time.substring(19, 20));
                        $("#videoCameraTimeUnit").val(time.substring(15, 16));
                        //17到31为 转为int  表示值
                        $("#videoCameraTimeInterval").val(parseInt(time.substring(0, 15), 2));
                        break;
                    case 0x0065:// 定距拍照控制   转二进制  取位判断
                        isCameraDistanceFlag = true;
                        var distance = param.value.toString(2);
                        var len = distance.length;
                        for (var i = len; i < 32; i++) {
                            //补全二进制
                            distance = "0" + distance;
                        }
                        $("#videoCameraSwitchMarkOne").val(distance.substring(31, 32));
                        $("#videoCameraSwitchMarkTwo").val(distance.substring(30, 31));
                        $("#videoCameraSwitchMarkThree").val(distance.substring(29, 30));
                        $("#videoCameraSwitchMarkFour").val(distance.substring(28, 29));
                        $("#videoCameraSwitchMarkFive").val(distance.substring(27, 28));
                        $("#videoCameraStorageMarkOne").val(distance.substring(23, 24));
                        $("#videoCameraStorageMarkTwo").val(distance.substring(22, 23));
                        $("#videoCameraStorageMarkThree").val(distance.substring(21, 22));
                        $("#videoCameraStorageMarkFour").val(distance.substring(20, 21));
                        $("#videoCameraStorageMarkFive").val(distance.substring(19, 20));
                        $("#videoCameraDistanceUnit").val(distance.substring(15, 16));
                        //17到31为 转为int  表示值
                        $("#videoCameraDistanceInterval").val(parseInt(distance.substring(0, 15), 2));
                        break;
                    case 0x0070:// 图像/视频质量，1-10，1 最好
                        $("#videoCameraQuality").val(param.value);
                        break;
                    case 0x0071:// 亮度，0-255
                        $("#videoCameraBrightness").val(param.value);
                        break;
                    case 0x0072:// 对比度，0-127
                        $("#videoCameraContrast").val(param.value);
                        break;
                    case 0x0073:// 饱和度，0-127
                        $("#videoCameraSaturation").val(param.value);
                        break;
                    case 0x0074:// 色度，0-255
                        $("#videoCameraChroma").val(param.value);
                        break;
                    /*     case 0x0080:// 车辆里程表读数，1/10km
                     $("#terminalSendTime").val(param.value);
                     break;
                     case 0x0081:// 车辆所在的省域ID
                     $("#terminalSendTime").val(param.value);
                     break;
                     case 0x0082:// 车辆所在的市域ID
                     $("#terminalSendTime").val(param.value);
                     break;
                     case 0x0083:// 公安交通管理部门颁发的机动车号牌
                     $("#terminalSendTime").val(param.value);
                     break;
                     case 0x0084:// 车牌颜色，按照JT/T415-2006 的5.4.12
                     $("#terminalSendTime").val(param.value);
                     break;*/
                    case 0x0090:// GNSS 定位模式，bit0，0：禁用 GPS 定位， 1：启用 GPS 定位；
                        // bit1，0：禁用北斗定位， 1：启用北斗定位；
                        //bit2，0：禁用 GLONASS 定位， 1：启用 GLONASS 定位；
                        //bit3，0：禁用 Galileo 定位， 1：启用 Galileo 定位。
                        var gnss = param.value.toString(2);
                        var len = gnss.length;
                        for (var i = len; i < 4; i++) {
                            gnss = "0" + gnss;
                        }
                        $("#GPSFlag").val(gnss.substring(3, 4));
                        $("#beidouFlag").val(gnss.substring(2, 3));
                        $("#GLONASSFlag").val(gnss.substring(1, 2));
                        $("#GalileoFlag").val(gnss.substring(0, 1));
                        break;
                    case 0x0091:// GNSS 波特率bit0
                        $("#GNSSBaudRate").val(param.value);
                        break;
                    case 0x0092:// GNSS 模块详细定位数据输出频率
                        $("#GNSSPositionOutputRate").val(param.value);
                        break;
                    case 0x0093:// GNSS 模块详细定位数据采集频率，单位为秒，默认为 1。
                        $("#GNSSPositionCollectRate").val(param.value);
                        break;
                    case 0x0094:// GNSS 模块详细定位数据上传方式
                        $("#gnss").val(param.value);
                        break;
                    case 0xF901:
                        var setParameter = param.value.setParameter;
                        if (!param.value || !param.value.setParameter) {
                            return layer.msg('终端数据为空')
                        }
                        $("#RS232serialPortParameters .insertAddr").html('');
                        $("#RS232Numbers").val(setParameter.number);
                        if (setParameter.serialPort.length > 0) {
                            var data = setParameter.serialPort;
                            //排序
                            data.sort(function (a, b) {
                                return a.sum - b.sum;
                            });
                            $("#RS232serialPortNumber").val(data[0].sum);
                            $("#RS232baudRate").val(data[0].baudRate);
                            $("#RS232dataBits").val(data[0].dataPosition);
                            $("#RS232stopBit").val(data[0].stopPosition);
                            $("#RS232parityBit").val(data[0].checkPosition);
                            $("#RS232flowControl").val(data[0].flowControl);
                            $("#RS232dataAcceptanceTimeoutTime").val(data[0].receiveTimeOut);
                            var html = '';
                            for (var i = 1; i < data.length; i++) {
                                var baudRate = data[i].baudRate == 255 ? -1 : data[i].baudRate;
                                var dataPosition = data[i].dataPosition == 255 ? -1 : data[i].dataPosition;
                                var stopPosition = data[i].stopPosition == 255 ? -1 : data[i].stopPosition;
                                var checkPosition = data[i].checkPosition == 255 ? -1 : data[i].checkPosition;
                                var flowControl = data[i].flowControl == 255 ? -1 : data[i].flowControl;
                                html += '<div class="insertDiv">    ' +
                                    '<div class="form-group"><label class="col-md-3 control-label">串口' + i + '参数</label></div>' +
                                    '<div class="form-group">' +
                                    '<label class="col-md-3 control-label"><label class="text-danger">*</label> 串口序号：</label>' +
                                    '<div class="col-md-3">' +
                                    '<input type="text" name="serialPortNumber" class="form-control" readonly value="' + data[i].sum + '">' +
                                    '</div></div>' +
                                    '<div class="form-group">' +
                                    '<label class="col-md-3 control-label">波特率：</label>' +
                                    '<div class="col-md-3">' +
                                    '<select name="baudRate" class="form-control" value="' + baudRate + '">' +
                                    '<option value="1">2400</option>' +
                                    '<option value="2">4800</option>' +
                                    '<option value="3">9600</option>' +
                                    '<option value="4">19200</option>' +
                                    '<option value="5">38400</option>' +
                                    '<option value="6">57600</option>' +
                                    '<option value="7">115200</option>' +
                                    '<option value="-1">不修改波特率</option>' +
                                    '</select>' +
                                    '</div>' +
                                    '<label class="col-md-2 control-label">数据位：</label>' +
                                    '<div class="col-md-3">' +
                                    '<select name="dataBits" class="form-control" value="' + dataPosition + '">' +
                                    '<option value="5">5</option>' +
                                    '<option value="6">6</option>' +
                                    '<option value="7">7</option>' +
                                    '<option value="8">8</option>' +
                                    '<option value="-1">不修改数据位</option>' +
                                    '</select>' +
                                    '</div></div>' +
                                    '<div class="form-group">' +
                                    '<label class="col-md-3 control-label">停止位：</label>' +
                                    '<div class="col-md-3">' +
                                    '<select name="stopBit" class="form-control" value="' + stopPosition + '">' +
                                    '<option value="1">1</option>' +
                                    '<option value="2">2</option>' +
                                    '<option value="-1">不修改停止位</option>' +
                                    '</select>' +
                                    '</div>' +
                                    '<label class="col-md-2 control-label">校验位：</label>' +
                                    '<div class="col-md-3">' +
                                    '<select name="parityBit" class="form-control" value="' + checkPosition + '">' +
                                    '<option value="1">奇校验</option>' +
                                    '<option value="2">偶校验</option>' +
                                    '<option value="3">无校验</option>' +
                                    '<option value="-1">不修改校验位</option>' +
                                    '</select>' +
                                    '</div>' +
                                    '</div>' +
                                    '<div class="form-group">' +
                                    '<label class="col-md-3 control-label">流控：</label>' +
                                    '<div class="col-md-3">' +
                                    '<select name="flowControl" class="form-control" value="' + flowControl + '">' +
                                    '<option value="1">无流控</option>' +
                                    '<option value="2">硬件流控</option>' +
                                    '<option value="3">软件流控</option>' +
                                    '<option value="-1">不修改流控</option>' +
                                    '</select>' +
                                    '</div>' +
                                    '<label class="col-md-2 control-label">数据接收超时(毫秒)：</label>' +
                                    '<div class="col-md-3">' +
                                    '<div class="col-md-15">' +
                                    '<input type="text" name="dataAcceptanceTimeoutTime" id="data232_' + i + '" class="form-control"  value="' + data[i].receiveTimeOut + '">' +
                                    '</div></div></div></div>'
                            }
                            $("#RS232serialPortParameters .insertAddr").html(html);
                            var targetSelect = $(".insertAddr select");
                            for (var i = 0; i < targetSelect.length; i++) {
                                $(targetSelect[i]).val($(targetSelect[i]).attr('value'));
                            }
                        }
                        break;
                    case 0xF902:
                        if (!param.value || !param.value.setParameter) {
                            return layer.msg('终端数据为空')
                        }
                        var setParameter = param.value.setParameter;
                        $("#RS485serialPortParameters .insertAddr").html('');
                        $("#RS485Numbers").val(setParameter.number);
                        if (setParameter.serialPort.length > 0) {
                            var data = setParameter.serialPort;
                            //排序
                            data.sort(function (a, b) {
                                return a.sum - b.sum;
                            });
                            $("#RS485serialPortNumber").val(data[0].sum);
                            $("#RS485baudRate").val(data[0].baudRate);
                            $("#RS485dataBits").val(data[0].dataPosition);
                            $("#RS485stopBit").val(data[0].stopPosition);
                            $("#RS485parityBit").val(data[0].checkPosition);
                            $("#RS485flowControl").val(data[0].flowControl);
                            $("#RS485dataAcceptanceTimeoutTime").val(data[0].receiveTimeOut);
                            var html = '';
                            for (var i = 1; i < data.length; i++) {
                                var baudRate = data[i].baudRate == 255 ? -1 : data[i].baudRate;
                                var dataPosition = data[i].dataPosition == 255 ? -1 : data[i].dataPosition;
                                var stopPosition = data[i].stopPosition == 255 ? -1 : data[i].stopPosition;
                                var checkPosition = data[i].checkPosition == 255 ? -1 : data[i].checkPosition;
                                var flowControl = data[i].flowControl == 255 ? -1 : data[i].flowControl;
                                html += '<div class="insertDiv">' +
                                    '<div class="form-group"><label class="col-md-3 control-label">串口' + i + '参数</label></div>' +
                                    '<div class="form-group">' +
                                    '<label class="col-md-3 control-label"><label class="text-danger">*</label> 串口序号：</label>' +
                                    '<div class="col-md-3">' +
                                    '<input type="text" name="serialPortNumber" class="form-control" readonly value="' + data[i].sum + '">' +
                                    '</div></div>' +
                                    '<div class="form-group">' +
                                    '<label class="col-md-3 control-label">波特率：</label>' +
                                    '<div class="col-md-3">' +
                                    '<select name="baudRate" class="form-control" value="' + baudRate + '">' +
                                    '<option value="1">2400</option>' +
                                    '<option value="2">4800</option>' +
                                    '<option value="3">9600</option>' +
                                    '<option value="4">19200</option>' +
                                    '<option value="5">38400</option>' +
                                    '<option value="6">57600</option>' +
                                    '<option value="7">115200</option>' +
                                    '<option value="-1">不修改波特率</option>' +
                                    '</select>' +
                                    '</div>' +
                                    '<label class="col-md-2 control-label">数据位：</label>' +
                                    '<div class="col-md-3">' +
                                    '<select name="dataBits" class="form-control" value="' + dataPosition + '">' +
                                    '<option value="5">5</option>' +
                                    '<option value="6">6</option>' +
                                    '<option value="7">7</option>' +
                                    '<option value="8">8</option>' +
                                    '<option value="-1">不修改数据位</option>' +
                                    '</select>' +
                                    '</div></div>' +
                                    '<div class="form-group">' +
                                    '<label class="col-md-3 control-label">停止位：</label>' +
                                    '<div class="col-md-3">' +
                                    '<select name="stopBit" class="form-control" value="' + stopPosition + '">' +
                                    '<option value="1">1</option>' +
                                    '<option value="2">2</option>' +
                                    '<option value="-1">不修改停止位</option>' +
                                    '</select>' +
                                    '</div>' +
                                    '<label class="col-md-2 control-label">校验位：</label>' +
                                    '<div class="col-md-3">' +
                                    '<select name="parityBit" class="form-control" value="' + checkPosition + '">' +
                                    '<option value="1">奇校验</option>' +
                                    '<option value="2">偶校验</option>' +
                                    '<option value="3">无校验</option>' +
                                    '<option value="-1">不修改校验位</option>' +
                                    '</select>' +
                                    '</div>' +
                                    '</div>' +
                                    '<div class="form-group">' +
                                    '<label class="col-md-3 control-label">流控：</label>' +
                                    '<div class="col-md-3">' +
                                    '<select name="flowControl" class="form-control" value="' + flowControl + '">' +
                                    '<option value="1">无流控</option>' +
                                    '<option value="2">硬件流控</option>' +
                                    '<option value="3">软件流控</option>' +
                                    '<option value="-1">不修改流控</option>' +
                                    '</select>' +
                                    '</div>' +
                                    '<label class="col-md-2 control-label">数据接收超时(毫秒)：</label>' +
                                    '<div class="col-md-3">' +
                                    '<div class="col-md-15">' +
                                    '<input type="text" name="dataAcceptanceTimeoutTime" id="data485_' + i + '" class="form-control"  value="' + data[i].receiveTimeOut + '">' +
                                    '</div></div></div></div>'
                            }
                            $("#RS485serialPortParameters .insertAddr").html(html);
                            var targetSelect = $(".insertAddr select");
                            for (var i = 0; i < targetSelect.length; i++) {
                                $(targetSelect[i]).val($(targetSelect[i]).attr('value'));
                            }
                        }
                        break;
                    case 0xF903:
                        if (!param.value || !param.value.setParameter) {
                            return layer.msg('终端数据为空')
                        }
                        var setParameter = param.value.setParameter;
                        $("#CANserialPortParameters .insertAddr").html('');
                        $("#CANNumbers").val(setParameter.number);
                        if (setParameter.serialPort.length > 0) {
                            var data = setParameter.serialPort;
                            //排序
                            data.sort(function (a, b) {
                                return a.sum - b.sum;
                            });
                            $("#CANserialPortNumber").val(data[0].sum);
                            $("#CANbaudRate").val(data[0].baudRate);
                            $("#CANdataBits").val(data[0].dataPosition);
                            $("#CANstopBit").val(data[0].stopPosition);
                            $("#CANparityBit").val(data[0].checkPosition);
                            $("#CANflowControl").val(data[0].flowControl);
                            $("#CANdataAcceptanceTimeoutTime").val(data[0].receiveTimeOut);
                            var html = '';
                            for (var i = 1; i < data.length; i++) {
                                var baudRate = data[i].baudRate == 255 ? -1 : data[i].baudRate;
                                var dataPosition = data[i].dataPosition == 255 ? -1 : data[i].dataPosition;
                                var stopPosition = data[i].stopPosition == 255 ? -1 : data[i].stopPosition;
                                var checkPosition = data[i].checkPosition == 255 ? -1 : data[i].checkPosition;
                                var flowControl = data[i].flowControl == 255 ? -1 : data[i].flowControl;
                                html += '<div class="insertDiv">' +
                                    '<div class="form-group"><label class="col-md-3 control-label">串口' + i + '参数</label></div>' +
                                    '<div class="form-group">' +
                                    '<label class="col-md-3 control-label"><label class="text-danger">*</label> 串口序号：</label>' +
                                    '<div class="col-md-3">' +
                                    '<input type="text" name="serialPortNumber" class="form-control" readonly value="' + data[i].sum + '">' +
                                    '</div></div>' +
                                    '<div class="form-group">' +
                                    '<label class="col-md-3 control-label">波特率：</label>' +
                                    '<div class="col-md-3">' +
                                    '<select name="baudRate" class="form-control" value="' + baudRate + '">' +
                                    '<option value="1">2400</option>' +
                                    '<option value="2">4800</option>' +
                                    '<option value="3">9600</option>' +
                                    '<option value="4">19200</option>' +
                                    '<option value="5">38400</option>' +
                                    '<option value="6">57600</option>' +
                                    '<option value="7">115200</option>' +
                                    '<option value="-1">不修改波特率</option>' +
                                    '</select>' +
                                    '</div>' +
                                    '<label class="col-md-2 control-label">数据位：</label>' +
                                    '<div class="col-md-3">' +
                                    '<select name="dataBits" class="form-control" value="' + dataPosition + '">' +
                                    '<option value="5">5</option>' +
                                    '<option value="6">6</option>' +
                                    '<option value="7">7</option>' +
                                    '<option value="8">8</option>' +
                                    '<option value="-1">不修改数据位</option>' +
                                    '</select>' +
                                    '</div></div>' +
                                    '<div class="form-group">' +
                                    '<label class="col-md-3 control-label">停止位：</label>' +
                                    '<div class="col-md-3">' +
                                    '<select name="stopBit" class="form-control" value="' + stopPosition + '">' +
                                    '<option value="1">1</option>' +
                                    '<option value="2">2</option>' +
                                    '<option value="-1">不修改停止位</option>' +
                                    '</select>' +
                                    '</div>' +
                                    '<label class="col-md-2 control-label">校验位：</label>' +
                                    '<div class="col-md-3">' +
                                    '<select name="parityBit" class="form-control" value="' + checkPosition + '">' +
                                    '<option value="1">奇校验</option>' +
                                    '<option value="2">偶校验</option>' +
                                    '<option value="3">无校验</option>' +
                                    '<option value="-1">不修改校验位</option>' +
                                    '</select>' +
                                    '</div>' +
                                    '</div>' +
                                    '<div class="form-group">' +
                                    '<label class="col-md-3 control-label">流控：</label>' +
                                    '<div class="col-md-3">' +
                                    '<select name="flowControl" class="form-control" value="' + flowControl + '">' +
                                    '<option value="1">无流控</option>' +
                                    '<option value="2">硬件流控</option>' +
                                    '<option value="3">软件流控</option>' +
                                    '<option value="-1">不修改流控</option>' +
                                    '</select>' +
                                    '</div>' +
                                    '<label class="col-md-2 control-label">数据接收超时(毫秒)：</label>' +
                                    '<div class="col-md-3">' +
                                    '<div class="col-md-15">' +
                                    '<input type="text" name="dataAcceptanceTimeoutTime" id="CANdata_' + i + '" class="form-control"  value="' + data[i].receiveTimeOut + '">' +
                                    '</div></div></div></div>'
                            }
                            $("#CANserialPortParameters .insertAddr").html(html);
                            var targetSelect = $(".insertAddr select");
                            for (var i = 0; i < targetSelect.length; i++) {
                                $(targetSelect[i]).val($(targetSelect[i]).attr('value'));
                            }
                        }
                        break;
                    case 0xF904:
                        var gnssData = param.value.gnssData;
                        $("#GNSSStatus").val(gnssData.status == 0 ? "定位" : "未定位");
                        var signalStrength = "";
                        if (gnssData.signalStrength == 0) {
                            signalStrength = "强";
                        } else if (gnssData.signalStrength == 1) {
                            signalStrength = "弱";
                        } else if (gnssData.signalStrength == 2) {
                            signalStrength = "无信号";
                        }
                        $("#signalEmphasis").val(signalStrength);
                        var networkType = "";
                        if (gnssData.networkType == 1) {
                            networkType = "2G";
                        } else if (gnssData.networkType == 2) {
                            networkType = "3G";
                        } else if (gnssData.networkType == 3) {
                            networkType = "4G";
                        } else if (gnssData.networkType == 4) {
                            networkType = "无信号";
                        }
                        $("#networkType").val(networkType);
                        $("#SIMStatus").val(gnssData.simStatus == 0 ? "正常" : "不正常");
                        break;
                    case 0xF905:
                        var videoChannel = param.value.videoChannel;
                        for (var i = 0; i < videoChannel.length; i++) {
                            var channal = "";
                            if (videoChannel[i].channelStatus == 0) {
                                channal = "正常";
                            } else if (videoChannel[i].channelStatus == 1) {
                                channal = "视频信号丢失";
                            } else if (videoChannel[i].channelStatus == 2) {
                                channal = "禁用";
                            } else if (videoChannel[i].channelStatus == 3) {
                                channal = "无IPC";
                            }
                            $("#channel" + (videoChannel[i].channelNum)).val(channal);
                        }
                        break;
                    case 0xF906:
                        $(".insertTxt").remove();
                        $(".storageInsertAddr").html('');
                        $(".SDInsertAddr").html('');

                        $("#storageNum").val(param.value.number);
                        $("#SDNum").val(param.value.sdNumber);
                        var hardDisk = param.value.hardDisk;
                        var diskHtml = '';
                        if (hardDisk.length != 0) {
                            diskHtml += '<label class="col-md-3 control-label insertTxt">硬盘1状态：</label>' +
                                '<div class="col-md-3 insertTxt">' +
                                '<input type="text" class="form-control" value="' + commandParamBind.getStatus(hardDisk[0]) + '" readonly>' +
                                '</div>';
                            $('.storageRow').append(diskHtml);
                        }
                        diskHtml = '';
                        if (hardDisk.length > 1) {
                            for (var i = 2; i <= hardDisk.length; i++) {
                                if (i % 2 == 0) {
                                    diskHtml += '<div class="form-group insertDiv">';
                                }
                                diskHtml += '<label class="col-md-3 control-label">硬盘' + i + '状态：</label>' +
                                    '<div class="col-md-3">' +
                                    '<input type="text" class="form-control" value="' + commandParamBind.getStatus(hardDisk[i - 1]) + '" readonly>' +
                                    '</div>';
                                if (i % 2 == 1 || i == hardDisk.length) {
                                    diskHtml += '</div>';
                                }
                            }
                            $(".storageInsertAddr").html(diskHtml);
                        }

                        var sdStatus = param.value.sdStatus;
                        var sdStatusHtml = '';
                        if (sdStatus.length != 0) {
                            sdStatusHtml += '<label class="col-md-3 control-label insertTxt">SD卡1状态：</label>' +
                                '<div class="col-md-3 insertTxt">' +
                                '<input type="text" class="form-control" value="' + commandParamBind.getStatus(sdStatus[0]) + '" readonly>' +
                                '</div>';
                            $('.SDRow').append(sdStatusHtml);
                        }
                        sdStatusHtml = '';
                        if (sdStatus.length > 1) {
                            for (var i = 2; i <= sdStatus.length; i++) {
                                if (i % 2 == 0) {
                                    sdStatusHtml += '<div class="form-group insertDiv">';
                                }
                                sdStatusHtml += '<label class="col-md-3 control-label">SD卡' + i + '状态：</label>' +
                                    '<div class="col-md-3">' +
                                    '<input type="text" class="form-control" value="' + commandParamBind.getStatus(sdStatus[i - 1]) + '" readonly>' +
                                    '</div>';
                                if (i % 2 == 1 || i == sdStatus.length) {
                                    sdStatusHtml += '</div>';
                                }
                            }
                            $(".SDInsertAddr").html(sdStatusHtml);
                        }
                        break;

                }
            }
        },
        getStatus: function (data) {
            var val = '';
            switch (data) {
                case 0:
                    val = '正在录像';
                    break;
                case 1:
                    val = '未录像';
                    break;
                case 2:
                    val = '异常';
                    break;
                case 3:
                    val = '不存在';
                    break;
                default:
                    val = '';
                    break;
            }
            return val;
        },

        //基本信息-下发获取基本信息返回处理方法
        getF3BaseParamCall: function (data) {
            if (!data.success) {
                layer.msg(data.msg);
            } else {
                commandParamBind.clearInputTextValue();
                commandParamBind.createSocket0104InfoMonitor(data.msg);
                if (isRead) {
                    // layer_time = setTimeout(function () {
                    $("#readButton").html("<i class='fa fa-spinner loading-state'></i>").prop('disabled', true);
                    $("#sendButton").prop('disabled', true);
                    // }, 0);
                }
            }
        },

        //创建消息监听
        createSocket0104InfoMonitor: function (msg) {
            clearTimeout(_timeout);
            _timeout = window.setTimeout(function () {
                if (isRead) {
                    isRead = false;
                    layer.closeAll();
                    if (layer_time) {
                        clearTimeout(layer_time);
                    }
                    $("#readButton").html("读取").prop('disabled', false);
                    $("#sendButton").prop('disabled', false);
                    layer.msg("获取设备数据失败!");
                }
            }, 60000);
        },
        /**
         * 多媒体检索
         * */
        goMultimediaRetrieval: function () {
            if (!commandParamBind.validates()) return;
            $('#sendButton').prop('disabled', true);
            var param = {
                "vid": $('#vid11').val(),
                "orderType": '11',
                "type": $('#msType').val(),
                "wayID": $('#wayID').val(),
                "eventCode": $('#eventCode').val(),
                "startTime": $('#msStartTime').val(),
                "endTime": $('#msEndTime').val(),
            };
            json_ajax("POST", "/clbs//v/monitoring/orderMsg", "json", false, param, function (data) {
                if (data.success) {
                    serialNumber = data.obj.serialNumber;
                    $("#mediaRetrieveBtn").html("<i class='fa fa-spinner loading-state'></i>").prop('disabled', true);
                    isMediaRead = true;
                    if (mediaTimeout) {
                        clearTimeout(mediaTimeout);
                    }
                    mediaTimeout = window.setTimeout(function () {
                        if (isMediaRead) {
                            isMediaRead = false;
                            layer.closeAll();
                            $('#sendButton').prop('disabled', true);
                            $("#mediaRetrieveBtn").html('多媒体检索').prop('disabled', false);
                            layer.msg("获取设备数据失败!");
                        }
                    }, 60000);
                    // commandParamBind.mediaWebsocketCallback(mediaMsgBody);
                    commandParamBind.mediaWebsocket();
                } else if (data.msg) {
                    layer.msg(data.msg);
                }
            });
        },
        mediaWebsocket: function () {
            webSocket.subscribe(headers, '/topic/deviceReportLog', function (msg) {
                var result = $.parseJSON(msg.body);
                var msgBody = result.data.msgBody;
                var msgSN = msgBody.msgSN;
                if (msgSN == serialNumber) {// 判断流水号是否相等
                    isMediaRead = false;
                    if (mediaTimeout) {
                        clearTimeout(mediaTimeout);
                    }
                    var searchDatas = msgBody.searchDatas;
                    var html = '';
                    for (var i = 0, len = searchDatas.length; i < len; i++) {
                        var item = searchDatas[i];
                        html += '<option value="' + item.ID + '" data-event="' + item.eventCode + '"' +
                            ' data-type="' + item.type + '" data-channel="' + item.wayID + '">' + item.ID + '</option>';
                    }
                    $('#mediaId').html(html);
                    commandParamBind.mediaIdChange();
                    $("#deleteSign").prop('checked', false);
                    $("#mediaRetrieveBtn").html('多媒体检索').prop('disabled', false);
                    if (searchDatas.length > 0) {
                        $("#sendButton").prop('disabled', false);
                    }
                }
            }, null, null);
        },
        /*mediaWebsocketCallback: function (msgBody) {
            /!* var msgSN = msgBody.msgSN;
             if (msgSN == serialNumber) {// 判断流水号是否相等*!/
            isMediaRead = false;
            if (mediaTimeout) {
                clearTimeout(mediaTimeout);
            }
            var searchDatas = msgBody.searchDatas;
            var html = '';
            for (var i = 0, len = searchDatas.length; i < len; i++) {
                var item = searchDatas[i];
                html += '<option value="' + item.id + '" data-event="' + item.eventCode + '"' +
                    ' data-type="' + item.type + '" data-channel="' + item.wayID + '">' + item.id + '</option>';
            }
            $('#mediaId').html(html);
            commandParamBind.mediaIdChange();
            $("#deleteSign").prop('checked', false);
            $('#sendButton').prop('disabled', true);
            $("#mediaRetrieveBtn").html('多媒体检索').prop('disabled', false);
            if (searchDatas.length > 0) {
                $("#sendButton").prop('disabled', false);
            }
            // }
        },*/
        mediaIdChange: function () {
            var selectOpt = $('#mediaId').find('option:selected');
            var type = selectOpt.attr('data-type');
            var channel = selectOpt.attr('data-channel');
            var event = selectOpt.attr('data-event');

            var eventVal = '';
            switch (event) {
                case '0':
                    eventVal = '平台下发指令';
                    break;
                case '1':
                    eventVal = '定时动作';
                    break;
                case '2':
                    eventVal = '抢劫报警';
                    break;
                case '3':
                    eventVal = '碰撞侧翻报警';
                    break;
                default:
                    eventVal = '';
                    break;
            }
            var typeVal = '';
            switch (type) {
                case '0':
                    typeVal = '图像';
                    break;
                case '1':
                    typeVal = '音频';
                    break;
                case '2':
                    typeVal = '视频';
                    break;
                default:
                    typeVal = '';
                    break;
            }
            $('#mediaType').val(typeVal);
            $('#channelId').val(channel == '0' ? '所有' : channel);
            $('#eventItem').val(eventVal);
        },
        /*
        * 下发升级包相关方法
        * */
        //当前时间
        getsTheCurrentTime: function () {
            var nowDate = new Date();
            var curTime = nowDate.getFullYear()
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
            var hours = nowDate.getHours();
            var minutes = nowDate.getMinutes();
            var seconds = nowDate.getSeconds();
            var minDate = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate())
                + " "
                + (hours < 10 ? "0" + hours : hours)
                + ":"
                + (minutes < 10 ? "0" + minutes : minutes)
                + ":"
                + (seconds < 10 ? "0" + seconds : seconds);
            laydate.render({elem: '#upgradeTime', theme: '#6dcff6', type: 'datetime', min: minDate});
            $("#upgradeTime").val(curTime);
        },
        renderFileSelectHtml: function () {
            var url = '/clbs/v/monitoring/commandParam/deviceUpgradeList';
            json_ajax("POST", url, "json", true, {start: 0, length: 5000}, function (data) {
                if (data.success) {
                    var result = '', resultData = data.records;
                    for (var i = 0; i < resultData.length; i++) {
                        result += '<option value="' + resultData[i].fileName + '" data-upgradeFileId="' + resultData[i].upgradeFileId + '" data-upgradeType="' + resultData[i].upgradeType + '">' + resultData[i].fileName + '</option>';
                    }
                    $('#upgradeFileSelect').html(result);
                    commandParamBind.upgradeFileSelectChange();
                }
            });
        },
        // 文件管理table初始化
        fileTableInit: function () {
            //表格列定义
            var columnDefs = [{
                //第一列，用来显示序号
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];
            var columns = [
                {
                    //第一列，用来显示序号
                    "data": null,
                    "class": "text-center"
                },
                {
                    "data": "uploadTime",
                    "class": "text-center"
                }, {
                    "data": "upgradeType",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        var result = '';
                        switch (data) {
                            case '0x00':
                                result = '终端';
                                break;
                            case '0x0c':
                                result = '道路运输终证IC卡读卡器';
                                break;
                            case '0x34':
                                result = '北斗定位模块';
                                break;
                            case '0x64':
                                result = '驾驶辅助功能模块';
                                break;
                            case '0x65':
                                result = '驾驶行为监测模块';
                                break;
                            case '0x66':
                                result = '胎压监测模块';
                                break;
                            case '0x67':
                                result = '盲区监测模块';
                                break;
                            default:
                                break;
                        }
                        return result;
                    }
                }, {
                    "data": "fileName",
                    "class": "text-center",
                }, {
                    "data": null,
                    "class": "text-center", //最后一列，操作按钮
                    render: function (data, type, row, meta) {
                        // var editUrlPath = myTable.editUrl + row.id + ".gsp"; //修改地址
                        var infoData = JSON.stringify(row).replace(/"/g, "'");
                        var result = '';
                        //修改按钮
                        result += '<a href="#" class="editUpgradePackage" onclick="commandParamBind.showEditFileModal(' + infoData + ')">修改</a>&ensp;';
                        //删除按钮
                        result += '<a href="#" onclick="myFileTable.deleteItem(\'' + row.upgradeFileId + '\')">删除</a>';
                        return result;
                    }
                }
            ];
            //表格setting
            var setting = {
                listUrl: '/clbs/v/monitoring/commandParam/deviceUpgradeList',
                // editUrl: '',
                suffix: ' ',
                deleteUrl: '/clbs/v/monitoring/commandParam/deleteDeviceUpgrade_',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'fileDataTable', //表格
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true
            };
            //创建表格
            myFileTable = new TG_Tabel.createNew(setting);
            //表格初始化
            myFileTable.init();
        },
        //显示文件名称
        showFileName: function () {
            $('#sendButton').prop('disabled', false);
            var filePath = $("#excelPath").val();
            if (filePath) {
                var arr = filePath.split('\\');
                var fileName = arr[arr.length - 1];
                $('#upgradeFileSelect').hide().prop('disabled', true);
                $("#selectFile").val(fileName);
                $("#selectFileBox").show().prop('disabled', false);

                $('#upgradeFileId').val('');
                var reg = /^(.+)_(.+)_(.+)_(.+)_(.+)\.(.+)$/;
                if (reg.test(fileName)) {
                    var newName = fileName.substring(0, fileName.lastIndexOf('.'));
                    var nameArr = newName.split('_');
                    $('#factoryNumber').val(nameArr[1]);
                    $('#equipmentModel').val(nameArr[2]);
                    $('#dependSoftVersion').val(nameArr[3]);
                    $('#softVersion').val(nameArr[4]);
                } else {
                    $('#sendButton').prop('disabled', true);
                    layer.msg('上传文件不符合升级文件命名规范!');
                    $('#factoryNumber').val('');
                    $('#equipmentModel').val('');
                    $('#dependSoftVersion').val('');
                    $('#softVersion').val('');
                }
                $('#upgradeType').prop('disabled', false);
            } else {
                commandParamBind.cancelSlectFile();
            }
        },
        // 取消已选择的升级文件
        cancelSlectFile: function () {
            $("#selectFileBox").hide().prop('disabled', true);
            $("#selectFile").val('');
            $("#excelPath").val('');
            $('#upgradeFileSelect').show().prop('disabled', false);
            $('#upgradeType').prop('disabled', true);
            commandParamBind.upgradeFileSelectChange();
        },
        // 显示修改升级包弹窗
        showEditFileModal: function (data) {
            $('#addUpgradePackage input').val('');
            $('#edit_upgradeType').prop('disabled', true);
            for (key in data) {
                $('#edit_' + key).val(data[key]);
            }
            $('#upgradePackageName').html('修改升级包');
            $('#addUpgradePackage').modal('show');
        },
        upgradeFileSelectChange: function () {
            $('#sendButton').prop('disabled', false);
            var fileName = $('#upgradeFileSelect').val();
            $('#upgradeFileId').val($('#upgradeFileSelect option:selected').attr('data-upgradeFileId'));
            $('#upgradeType').val($('#upgradeFileSelect option:selected').attr('data-upgradeType'));
            var reg = /^(.+)_(.+)_(.+)_(.+)_(.+)\.(.+)$/;
            if (reg.test(fileName)) {
                var newName = fileName.substring(0, fileName.lastIndexOf('.'));
                var nameArr = newName.split('_');
                $('#factoryNumber').val(nameArr[1]);
                $('#equipmentModel').val(nameArr[2]);
                $('#dependSoftVersion').val(nameArr[3]);
                $('#softVersion').val(nameArr[4]);
            } else {
                $('#factoryNumber').val('');
                $('#equipmentModel').val('');
                $('#dependSoftVersion').val('');
                $('#softVersion').val('');
                if (fileName) {
                    layer.msg('当前文件不符合升级文件命名规范!');
                }
                $('#sendButton').prop('disabled', true);
            }
        },
        // 添加升级包弹窗显示文件名称
        addModalshowFileName: function () {
            var filePath = $("#addExcelPath").val();
            var arr = filePath.split('\\');
            var fileName = arr[arr.length - 1];
            $("#edit_fileName").val(fileName);
            $('#addUpgradeButton').prop('disabled', false);
            if (filePath) {
                var reg = /^(.+)_(.+)_(.+)_(.+)_(.+)\.(.+)$/;
                if (reg.test(fileName)) {
                    var newName = fileName.substring(0, fileName.lastIndexOf('.'));
                    var nameArr = newName.split('_');
                    $('#edit_factoryNumber').val(nameArr[1]);
                    $('#edit_equipmentModel').val(nameArr[2]);
                    $('#edit_dependSoftVersion').val(nameArr[3]);
                    $('#edit_softVersion').val(nameArr[4]);
                } else {
                    layer.msg('上传文件不符合升级文件命名规范!');
                    $('#addUpgradeButton').prop('disabled', true);
                    $('#edit_factoryNumber').val('');
                    $('#edit_equipmentModel').val('');
                    $('#edit_dependSoftVersion').val('');
                    $('#edit_softVersion').val('');
                }
            } else {
                $('#edit_factoryNumber').val('');
                $('#edit_equipmentModel').val('');
                $('#edit_dependSoftVersion').val('');
                $('#edit_softVersion').val('');
            }
        },
        // 添加/修改升级包
        addUpgradeFun: function () {
            if (!addUpgradeFlag) return;
            if ($('#edit_fileName').val() === '') {
                layer.msg('请选择升级文件');
                return;
            }
            addUpgradeFlag = false;
            $('#edit_upgradeType').prop('disabled', false);
            $("#addUpgradeForm").ajaxSubmit(function (data) {
                data = JSON.parse(data);
                if (data.success) {
                    commandParamBind.renderFileSelectHtml();
                    myFileTable.refresh();
                    $('#addUpgradePackage').modal('hide');
                    layer.msg($('#upgradePackageName').html() + '成功!');
                } else if (data.msg) {
                    layer.msg(data.msg);
                }
                $('#edit_upgradeType').prop('disabled', true);
                setTimeout(function () {
                    addUpgradeFlag = true;
                }, 310)
            })
        }
    };
    $(function () {
        $('input').inputClear();
        $(".modal-body").css({"height": "auto", "max-height": ($(window).height() - 194) + "px"});
        setTimeout(function () {
            $("#sensorNumber").parent().find("ul.dropdown-menu").hide();
        }, 100);
        commandParamBind.init();
        commandParamBind.getsTheCurrentTime();
        commandParamBind.getHoursMinuteSeconds();

        // 多媒体检索
        $("#mediaRetrieveBtn").on("click", commandParamBind.goMultimediaRetrieval);
        $('#mediaId').on('change', commandParamBind.mediaIdChange);

        $("#baseStation-add-btn").on("click", commandParamBind.addBaseStationEvent);
        $("#sendButton").on("click", commandParamBind.doSubmit);
        $("#readButton").on("click", commandParamBind.getParam);


        /**
         * 下发升级包
         * */
        $('.upgradeStrategy').on('change', function () {
            var upgradeStrategyInfo = $('.upgradeStrategyInfo');
            if ($('.upgradeStrategy:checked').val() == '1') {
                upgradeStrategyInfo.find('input').prop('disabled', false);
                upgradeStrategyInfo.slideDown();
            } else {
                upgradeStrategyInfo.find('input').prop('disabled', true);
                upgradeStrategyInfo.slideUp();
            }
        });
        //选择升级文件
        $("#inpFile").on("change", "input[type='file']", commandParamBind.showFileName);
        $('#newDelIcon').on('click', commandParamBind.cancelSlectFile);
        $('#addFileLink').on('click', function () {
            $('#addUpgradePackage input').val('');
            $('#edit_upgradeType').val('0x00').prop('disabled', false);
            $('#upgradePackageName').html('添加升级包');
            $('#addUpgradePackage').modal('show');
        });
        $('#upgradeFileSelect').on('change', commandParamBind.upgradeFileSelectChange);
        $("#addInpFile").on("change", "input[type='file']", commandParamBind.addModalshowFileName);
        // 添加升级包
        $('#addUpgradeButton').on('click', commandParamBind.addUpgradeFun);

        $('#upgradeTabs').on('click', 'a', function () {
            var currentTab = $(this).closest('li').attr('id');
            if (currentTab == 'remoteUpgradeTab') {
                $('#sendButton').show();
            } else {
                $('#sendButton').hide();
            }
        })
    })
}(window, $));