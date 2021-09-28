(function (window, $) {
    var remoteUpgradeTaskList = JSON.parse($("#remoteUpgradeTaskList").val());
    sensorName = $("#sensorName").attr('value');//传感器名称
    var curStopMonitor = '';//保存当前被中止的监控对象
    sendRemoteUpgradePage = {
        init: function () {
            // 恢复列表页批量升级设置属性为默认值
            $("#allUpgradeSetting").removeAttr('data-toggle', 'data-target').attr({
                'href': 'javascript:void(0);',
            }).on('click', remoteUpgrade.allUpgradeSetting);
            sensorName = $("#sensorName").attr('value');//传感器名称

            $("#upgradeTitle").html(sensorName + '远程升级');

            sendRemoteUpgradePage.setHtml();

            webSocket.subscribe(headers, '/topic/remoteUpgradeType', sendRemoteUpgradePage.webSocketCallback, null, null);
        },
        // 更新监控对象升级状态
        webSocketCallback: function (msg) {
            var item = JSON.parse(msg.body);
            var targetDiv = $('.div_' + item.monitorId);
            var curButton = targetDiv.find('button');
            var platformProgressBar = targetDiv.find('.platformProgressBar');
            var platformProgressNum = targetDiv.find('.platformProgressNum');
            var platformStatus = targetDiv.find('.platformStatus');
            var f3ProgressBar = targetDiv.find('.f3ProgressBar');
            var f3ProgressNum = targetDiv.find('.f3ProgressNum');
            var f3Status = targetDiv.find('.f3Status');

            if (item.monitorId == curStopMonitor) return;// 该监控对象已中止升级
            var peripheralId = $("#peripheralId").val();
            var peripheralIdInt = parseInt(peripheralId);
            if (item.peripheralId != peripheralIdInt) {
                platformProgressBar.css('width', '0%');
                platformProgressNum.html('');
                f3ProgressBar.css('width', '0%');
                f3ProgressNum.html('');
                f3Status.html('');
                if (item.isStartUpgrade) {
                    platformStatus.html('设备已经在升级中').attr('class', 'floatItem platformStatus failure');
                    curButton.html('开始升级').attr({
                        'class': 'btn btn-outline btn-default startUpgrade',
                        'disabled': true
                    });
                } else {
                    platformStatus.html('');
                    curButton.html('开始升级').attr({'class': 'btn btn-outline btn-primary startUpgrade'}).removeAttr('disabled');
                    // 开始升级
                    curButton.off().on('click', function () {
                        var vid = $(this).attr('data-vid');
                        sendRemoteUpgradePage.startUpgrade(vid);
                    });
                }
                return;
            }

            var platformWidth = item.totalPackageSize != '0' ? parseInt(item.successPackageSize) / parseInt(item.totalPackageSize) : 0;//平台->终端升级进度
            var peripheralsWidth = item.f3ToPeripheralTotalPackageSize != '0' ? parseInt(item.f3ToPeripheralSuccessPackageSize) / parseInt(item.f3ToPeripheralTotalPackageSize) : 0;//终端->外设升级进度
            var packageSize = (item.isStartUpgrade && item.platformToF3Status != '2' && item.platformToF3Status != '4') ? ("" + item.successPackageSize + "/" + item.totalPackageSize + "") : "";//平台->终端升级成功比例
            var f3PackageSize = item.isStartUpgrade ? ("" + item.f3ToPeripheralSuccessPackageSize + "/" + item.f3ToPeripheralTotalPackageSize + "") : "";//终端->外设升级成功比例
            var platformStatus1 = sendRemoteUpgradePage.getPlatformToF3Status(item.platformToF3Status).statusVal;//平台->终端升级状态
            var platformClassName = sendRemoteUpgradePage.getPlatformToF3Status(item.platformToF3Status).classVal;//平台->终端升级状态字体颜色
            var f3Status1 = sendRemoteUpgradePage.getF3ToPeripheralStatus(item.f3ToPeripheralStatus).statusVal;///终端->外设升级状态
            var f3ClassName = sendRemoteUpgradePage.getF3ToPeripheralStatus(item.f3ToPeripheralStatus).classVal;///终端->外设升级状态字体颜色

            var startFlag = (!item.isStartUpgrade && (item.platformToF3Status == '2' || item.platformToF3Status == '4')) ? true : false;
            var stopFlag = (item.isStartUpgrade && peripheralsWidth > 0) ? true : false;
            var btnDisabled = (startFlag || stopFlag) ? true : false;
            curButton.text('开始升级');
            if (platformWidth > 0 && peripheralsWidth == 0) {
                curButton.removeClass('btn-primary startUpgrade').addClass('btn-primary stopUpgrade').text('中止升级');

                // 中止升级
                curButton.off().on('click', function () {
                    var deviceId = $(this).attr('data-deviceId');
                    sendRemoteUpgradePage.stopUpgrade(deviceId, this);
                });
            }
            if (peripheralsWidth > 0 && peripheralsWidth < 1) {
                curButton.removeClass('btn-primary startUpgrade').addClass('btn-default stopUpgrade').text('中止升级');
            }
            if (btnDisabled) {// 控制升级按钮是否禁用
                var btnClassName = curButton.attr('class').replace('btn-primary', 'btn-default');
                curButton.attr({'class': btnClassName, 'disabled': true});
            } else {
                curButton.removeAttr('disabled');
            }

            platformProgressBar.css('width', (platformWidth * 100) + '%');
            platformProgressNum.html(packageSize);
            platformStatus.html(platformStatus1).attr('class', 'floatItem platformStatus ' + platformClassName);
            f3ProgressBar.css('width', (peripheralsWidth * 100) + '%');
            f3ProgressNum.html(f3PackageSize);
            f3Status.html(f3Status1).attr('class', 'floatItem f3Status ' + f3ClassName);

            if ((peripheralsWidth == 1 && item.f3ToPeripheralStatus == 1) || item.platformToF3Status == 3 || item.f3ToPeripheralStatus == 2) {// 已经升级完毕
                curButton.html('开始升级').attr({'class': 'btn btn-outline btn-primary startUpgrade'}).removeAttr('disabled');
                // 开始升级
                curButton.off().on('click', function () {
                    var vid = $(this).attr('data-vid');
                    sendRemoteUpgradePage.startUpgrade(vid);
                });
            }
        },
        // 渲染监控对象升级视图
        setHtml: function () {
            var len = remoteUpgradeTaskList.length;
            var html = '';
            for (var i = 0; i < len; i++) {
                var item = remoteUpgradeTaskList[i];
                var divClassName = 'div_' + item.monitorId;
                var btnClassName = item.isStartUpgrade ? "btn-primary stopUpgrade" : "btn-primary startUpgrade";
                var btnText = item.isStartUpgrade ? "中止升级" : "开始升级";
                var platformWidth = item.totalPackageSize != '0' ? parseInt(item.successPackageSize) / parseInt(item.totalPackageSize) : 0;//平台->终端升级进度
                var peripheralsWidth = item.f3ToPeripheralTotalPackageSize != '0' ? parseInt(item.f3ToPeripheralSuccessPackageSize) / parseInt(item.f3ToPeripheralTotalPackageSize) : 0;//终端->外设升级进度
                var packageSize = (item.isStartUpgrade && item.platformToF3Status != '2' && item.platformToF3Status != '4') ? ("" + item.successPackageSize + "/" + item.totalPackageSize + "") : "";//平台->终端升级成功比例
                var f3PackageSize = item.isStartUpgrade ? ("" + item.f3ToPeripheralTotalPackageSize + "/" + item.totalPackageSize + "") : "";//终端->外设升级成功比例
                var platformClass = sendRemoteUpgradePage.getPlatformToF3Status(item.platformToF3Status).classVal;//成功or失败显示对应字体颜色
                var f3Class = sendRemoteUpgradePage.getF3ToPeripheralStatus(item.f3ToPeripheralStatus).classVal;//成功or失败显示对应字体颜色

                var startFlag = (!item.isStartUpgrade && (item.platformToF3Status == '2' || item.platformToF3Status == '4')) ? true : false;
                var stopFlag = (item.isStartUpgrade && peripheralsWidth > 0) ? true : false;
                var btnDisabled = (startFlag || stopFlag) ? true : false;
                if (btnDisabled) {
                    btnClassName.replace('btn-primary', 'btn-default');
                }

                html += '<div class="form-group targetDiv ' + divClassName + '">' +
                    '                <label class="leftInfo control-label">' + item.plateNumber + '</label>' +
                    '                <div class="col-md-9">' +
                    '                    <div class="col-md-6">' +
                    '                        <div class="progressBox">' +
                    '                            <div class="progress floatItem">' +
                    '                                <div class="progress-bar progress-bar-progress platformProgressBar" role="progressbar" style="width: ' + platformWidth * 100 + '%;"></div>' +
                    '                            </div>' +
                    '                            <div class="floatItem platformProgressNum">' + packageSize + '</div>' +
                    '                            <div class="floatItem platformStatus ' + platformClass + '">' + sendRemoteUpgradePage.getPlatformToF3Status(item.platformToF3Status).statusVal + '</div>' +
                    '                        </div>' +
                    '                    </div>' +
                    '                    <div class="col-md-6">' +
                    '                        <div class="progressBox">' +
                    '                            <div class="progress floatItem">' +
                    '                                <div class="progress-bar progress-bar-progress f3ProgressBar" role="progressbar" style="width: ' + peripheralsWidth * 100 + '%;"></div>' +
                    '                            </div>' +
                    '                            <div class="floatItem f3ProgressNum">' + f3PackageSize + '</div>' +
                    '                            <div class="floatItem f3Status ' + f3Class + '"">' + sendRemoteUpgradePage.getF3ToPeripheralStatus(item.f3ToPeripheralStatus).statusVal + '</div>' +
                    '                        </div>' +
                    '                    </div>' +
                    '                </div>' +
                    '                <div class="col-md-1">';
                if (btnDisabled) {
                    html += '<button type="button" class="btn btn-outline ' + btnClassName + '" disabled data-vid="' + item.monitorId + '" data-deviceId="' + item.deviceId + '">' + btnText + '</button>'

                } else {
                    html += '<button type="button" class="btn btn-outline ' + btnClassName + '" data-vid="' + item.monitorId + '" data-deviceId="' + item.deviceId + '">' + btnText + '</button>'
                }
                html += '</div></div>'
            }
            $(".progressContainer").html(html);

            if (btnText == '中止升级') {
                // 中止升级
                $('.stopUpgrade').off().on('click', function () {
                    var deviceId = $(this).attr('data-deviceId');
                    sendRemoteUpgradePage.stopUpgrade(deviceId, this);
                });
            }
        },
        //平台->终端状态转换
        getPlatformToF3Status: function (data) {
            var statusVal = '';
            var classVal = '';
            switch (data) {
                case 0:
                    statusVal = '传输中';
                    break;
                case 1:
                    statusVal = '传输完成';
                    classVal = 'complete';
                    break;
                case 2:
                    statusVal = '设备已经在升级中';
                    classVal = 'failure';
                    break;
                case 3:
                    statusVal = '传输失败';
                    classVal = 'failure';
                    break;
                case 4:
                    statusVal = '终端离线';
                    classVal = 'failure';
                    break;
                default:
                    statusVal = '';
                    break;
            }
            return {'statusVal': statusVal, 'classVal': classVal};
        },
        // 终端->外设状态转换
        getF3ToPeripheralStatus: function (data) {
            var statusVal = '';
            var classVal = '';
            switch (data) {
                case 0:
                    statusVal = '进行中';
                    break;
                case 1:
                    statusVal = '完成';
                    classVal = 'complete';
                    break;
                case 2:
                    statusVal = '升级失败';
                    classVal = 'failure';
                    break;
                default:
                    statusVal = '';
                    break;
            }
            return {'statusVal': statusVal, 'classVal': classVal};
        },
        // 还原升级状态
        clearHtml: function (targetDiv) {
            targetDiv.find('.platformProgressBar').css('width', '0%');
            targetDiv.find('.platformProgressNum').html('');
            targetDiv.find('.platformStatus').html('').attr('class', 'floatItem platformStatus');
            targetDiv.find('.f3ProgressBar').css('width', '0%');
            targetDiv.find('.f3ProgressNum').html('');
            targetDiv.find('.f3Status').html('').attr('class', 'floatItem f3Status');
        },
        // 开始升级or批量升级
        startUpgrade: function (vid) {
            if (vid) {
                $("#monitorIds").val(vid);
            } else {
                var len = remoteUpgradeTaskList.length;
                var allId = '';
                for (var i = 0; i < len; i++) {
                    allId += remoteUpgradeTaskList[i].monitorId + ",";
                }
                $("#monitorIds").val(allId);
            }
            if ($(".fileName").val() == '') {
                layer.msg('请先选择升级文件');
                return;
            }
            // 文件大小判断
            if (!sendRemoteUpgradePage.fileSizeControl()) {
                layer.msg("升级文件不能大于64kb");
                return;
            }

            curStopMonitor = '';
            if (vid) {
                var targetDiv = $(".div_" + vid);
                sendRemoteUpgradePage.clearHtml(targetDiv);
            } else {
                var statusArr = $('.f3Status');
                var len = statusArr.length;
                for (var i = 0; i < len; i++) {
                    if ($(statusArr[i]).html() == '完成') {
                        var targetDiv = $(statusArr[i]).closest('.form-group');
                        sendRemoteUpgradePage.clearHtml(targetDiv);
                    }
                }
            }
            $("#upgradeForm").ajaxSubmit(function (data) {
                var result = JSON.parse(data);
                if (result.success) {
                    if (!vid) {
                        $('.startUpgrade').html('等待中');
                        $("#allUpgrade").removeClass('btn-primary').addClass('btn-default').prop('disabled', true)
                    } else {
                        var targetDiv = $(".div_" + vid + " .startUpgrade");
                        targetDiv.html('等待中');
                    }
                } else if (result.msg) {
                    layer.msg(result.msg);
                }
            })
        },
        // 文件大小判断
        fileSizeControl: function () {
            var target = $("#excelPath")[0];
            var fileSize = 0;
            if ((!!window.ActiveXObject || "ActiveXObject" in window) && !target.files) {
                var filePath = target.value;
                var fileSystem = new ActiveXObject("Scripting.FileSystemObject");
                var file = fileSystem.GetFile(filePath);
                fileSize = file.Size;
            } else {
                fileSize = target.files[0].size;
            }
            var size = fileSize / 1024;
            if (size > 64) {
                return false;
            }
            return true;
        },
        // 中止升级
        stopUpgrade: function (deviceId, curThis) {
            layer.confirm("是否中止升级,请确认!", {btn: ["确定", "取消"]}, function () {
                var url = "/clbs/v/sensorConfig/sensorUpgrade/getTerminationUpgrade";
                json_ajax("POST", url, "json", false, {'deviceId': deviceId}, function (data) {
                    layer.closeAll('dialog');
                    var result = data;
                    if (result.success) {
                        var vid = $(curThis).attr('data-vid');
                        curStopMonitor = vid;
                        $(curThis).html('开始升级').attr({'class': 'btn btn-outline btn-primary startUpgrade'}).removeAttr('disabled');
                        // 开始升级
                        $(curThis).off().on('click', function () {
                            var vid = $(this).attr('data-vid');
                            sendRemoteUpgradePage.startUpgrade(vid);
                        });
                    } else if (result.msg) {
                        layer.msg(result.msg);
                    }
                });
            });
        },
        //显示文件名称
        showFileName: function () {
            filePath = $("#excelPath").val();
            arr = filePath.split('\\');
            fileName = arr[arr.length - 1];
            $(".fileName").val(fileName);
        }
    };
    $(function () {
        sendRemoteUpgradePage.init();
        // 开始升级
        $(".startUpgrade").off().on('click', function () {
            var vid = $(this).attr('data-vid');
            sendRemoteUpgradePage.startUpgrade(vid);
        });
        //批量升级
        $("#allUpgrade").on('click', function () {
            sendRemoteUpgradePage.startUpgrade(false);
        });

        $("#closeButton").on('click', function () {
            myTable.requestData();
        });

        //选择升级文件
        $(".inpFilePhoto").on("change", "input[type='file']", sendRemoteUpgradePage.showFileName);
    })
})(window, $)