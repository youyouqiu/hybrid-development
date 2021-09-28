define(['common'], function (Common) {
    var Tree;
    var alarmDataMap = new Common.map(); // 报警数据集合
    var deviceTypeTxt; // 协议类型
    var logTime; // 日志时间（用户第一次进入界面时的时间）
    var alarmRole; // 检测是否有处理报警的权限
    var logFlag = $("#logFlag").val();
    var table = {
        init: function () {
            var $this = this;
            /**
             * 报警socket订阅
             */
            $this.subscribeAlarm();
            /*webSocket.subscribe(headers,
                '/user/topic/alarm',
                this.subscribeAlarmFn.bind(this),
                '/app/vehicle/subscribeStatus',
                null
            );*/
            /**
             * 日志订阅  配置文件配置开关
             */
            if (logFlag === "true") {
                this.getFormatTime(function () {
                    webSocket.subscribe(headers, '/topic/deviceReportLog', $this.logFindCilck.bind($this), null, null);
                });
            }
            this.checkAlarmRole();
        },
        /**
         * 报警订阅
         */
        subscribeAlarm: function () {
            var $this = this;
            setTimeout(function () {
                if (webSocket.conFlag) {
                    webSocket.send('/app/vehicle/subscribeStatus', headers, null);
                } else {
                    $this.subscribeAlarm();
                }
            }, 500);
        },
        /**
         * 报警信息订阅接收事件
         */
        subscribeAlarmFn: function (data) {
            var obj = JSON.parse(data.body);
            var protocolType = obj.data.msgBody.protocolType; //协议类型
            /**
             * 判断用户是否有监控对象的权限
             */
            var diyueall = Tree.tree.getUserMonitorIds();
            if ($.inArray(obj.desc.monitorId, diyueall) !== -1
                && protocolType !== undefined && protocolType !== null
                && this.isValidVideoProtocol(Number(protocolType))) {
                this.alarmDataAssembly(obj.data.msgBody);
            }
        },
        /**
         * 验证协议类型
         */
        isValidVideoProtocol: function (protocolType) {
            return [1, 11, 12, 13, 14, 15, 16, 17, 18, 20, 21, 22, 23].indexOf(protocolType) !== -1;
        },
        /**
         * 报警信息组装
         */
        alarmDataAssembly: function (data) {
            var alarmTime = data.gpsTime.length === 12 ? this.getTime(data.gpsTime) : '时间异常';
            var assignmentName = !!data.monitorInfo.assignmentName ? '未绑定分组' : data.monitorInfo.assignmentName;
            var alarmData = {
                monitorName: data.monitorInfo.monitorName, // 监控对象名称
                alarmTime: alarmTime, // 报警时间
                assignmentName: assignmentName, // 所属分组
                monitorType: this.monitorType(data.monitorInfo.monitorType), // 对象类型
                plateColor: getPlateColor(data.monitorInfo.plateColor), // 车牌颜色
                alarmType: data.alarmName, // 报警类型
                speed: data.speed, // 报警开始速度
                recorderSpeed: data.recorderSpeed, // 行车记录仪速度
                professionalsName: data.monitorInfo.professionalsName, // 从业人员
                fenceType: data.fenceType, // 围栏类型
                fenceName: data.fenceName, // 围栏名称
                longitude: data.longitude, // 经度
                latitude: data.latitude, // 纬度
                monitorId: data.monitorInfo.monitorId, // 监控对象ID
                earlyAlarmStartTime: data.earlyAlarmStartTimeStr,
                globalAlarmSet: data.globalAlarmSet, // 报警编号
                alarmSource: data.alarmSource,
                simcardNumber: data.monitorInfo.simcardNumber, // sim卡号
                deviceNumber: data.monitorInfo.deviceNumber, // 终端编号
                swiftNumber: data.swiftNumber, // 流水号
                protocolType: data.protocolType, // 协议类型
            };
            this.updateAlarmData(alarmData);
        },
        /**
         * 报警信息渲染到table表格
         */
        updateAlarmData: function (data) {
            var $this = this;
            /**
             * 同一个监控对象的报警类型需要进行叠加显示
             * 新增报警，渲染到表格第一行
             * 已存在的报警，对数据更新刷新
             */
            var status = false;
            if (alarmDataMap.has(data.monitorId)) {
                status = true;
                var alarmData = alarmDataMap.get(data.monitorId);
                if (alarmData.alarmType.indexOf(data.alarmType) === -1) {
                    data.alarmType = alarmData.alarmType + ',' + data.alarmType;
                }
            }
            alarmDataMap.set(data.monitorId, data);

            /**
             * 报警处理权限
             */
            var alarmStr;
            if (data.protocolType !== '22' && alarmRole) {
                alarmStr = '<td id="alarm' + data.monitorId + 'handle" style="color:#2ca2d1">未处理</td>';
            } else {
                alarmStr = '<td>未处理</td>'
            }
            let speed = data.speed;
            let recorderSpeed = data.recorderSpeed;
            let professionalsName = data.professionalsName;
            let fenceType = data.fenceType;
            let fenceName = data.fenceName;
            var html = '<td></td>' +
                '<td>' + data.monitorName + '</td>' +
                '<td>' + data.alarmTime + '</td>' +
                alarmStr +
                '<td>' + data.assignmentName + '</td>' +
                '<td>' + data.monitorType + '</td>' +
                '<td>' + data.plateColor + '</td>' +
                '<td>' + data.alarmType + '</td>' +
                '<td>' + (speed == null ? "" : speed) + '</td>' +
                '<td>' + (recorderSpeed == null ? "" : recorderSpeed) + '</td>' +
                '<td>' + (professionalsName == null ? "" : professionalsName) + '</td>' +
                '<td>' + (fenceType == null ? "" : fenceType) + '</td>' +
                '<td>' + (fenceName == null ? "" : fenceName) + '</td>' +
                '<td id="alarm' + data.monitorId + 'address"><a id="alarmAddress' + data.monitorId + '">点击获取位置信息</a></td>';
            if (status) {
                $('#alarm' + data.monitorId).html(html);
            } else {
                var str = '<tr id="alarm' + data.monitorId + '">' + html + '</tr>';
                $('#alarmRecordDataTable tbody').prepend(str);
                this.onAlarmTableDblclick(data.monitorId);
                this.layoutChange();
            }
            this.alarmTableSort();
            /**
             * 报警处理绑定点击事件
             */
            $('#alarm' + data.monitorId + 'handle').unbind('click').bind('click', function () {
                $this.warningManage(data.monitorId);
            });
            /**
             * 获取位置信息绑定点击事件
             */
            $('#alarmAddress' + data.monitorId).unbind('click').bind('click', function () {
                $this.getAlarmAddress(data.monitorId);
            })
        },
        /**
         * 报警数据进行序列号排序
         */
        alarmTableSort: function () {
            $('#alarmRecordDataTable tbody tr').each(function (index) {
                $(this).children('td:nth-child(1)').text(index + 1);
            });
        },
        /**
         * 报警数据绑定双击事件
         * @param row
         */
        onAlarmTableDblclick: function (id) {
            $('#alarm' + id).unbind('dblclick').bind('dblclick', function () {
                var alarmData = alarmDataMap.get(id);
                var alarmTime = alarmData.alarmTime;
                var earlyAlarmStartTime = alarmData.earlyAlarmStartTime;
                var alarmTypeArr = alarmData.alarmType;
                /**
                 * 判断是否有报警查询的菜单权限
                 */
                var alarmFlag = false;
                var urls = $('#permissionUrls').val();
                if (urls !== null && urls !== undefined) {
                    var urlList = urls.split(',');
                    if (urlList.indexOf("/a/search/list") > -1) {
                        alarmFlag = true;
                        //跳转
                        if (!earlyAlarmStartTime) {
                            earlyAlarmStartTime = alarmTime;
                        }
                        location.href = '/clbs/a/search/list?avid=' + alarmVid + '&atype=0' + '&atime=' + earlyAlarmStartTime + '&alarmTypeArr=' + alarmTypeArr;
                    }
                }
                if (!alarmFlag) {
                    layer.msg('无操作权限，请联系管理员');
                }
            });
        },
        /**
         * 时间格式转换
         */
        getTime: function (time) {
            return 20 + time.substring(0, 2) + "-" + time.substring(2, 4) + "-" + time.substring(4, 6) + " " +
                time.substring(6, 8) + ":" + time.substring(8, 10) + ":" + time.substring(10, 12);
        },
        /**
         * 监控对象-获取对象类型
         */
        monitorType: function (type) {
            switch (type) {
                case 0:
                    return '车';
                    break;
                case 1:
                    return '人';
                    break;
                case 2:
                    return '物';
                    break;
                default:
                    return '';
                    break;
            }
        },
        tableStateChange: function () {
            if ($('#scalingBtn').hasClass('fa fa-chevron-down')) {
                //列表隐藏
                this.tableContentHide();
            } else {
                //列表显示
                this.tableContentShow();
            }
        },
        /**
         * table区域显示
         */
        tableContentHide: function () {
            $('#videoRightTop').css('height', 'calc(100% - 44px)');
            this.videoSeparatedAdaptHide();
            $('#scalingBtn').removeClass('fa fa-chevron-down').addClass('fa fa-chevron-up');
        },
        /**
         * table区域隐藏
         */
        tableContentShow: function () {
            this.videoSeparatedAdaptShow();
            $('#scalingBtn').removeClass('fa fa-chevron-up').addClass('fa fa-chevron-down');
            this.layoutChange();
        },
        /**
         * 报警记录 or 日志记录添加后改变布局
         */
        layoutChange: function () {
            if (!$('#scalingBtn').hasClass('fa-chevron-up')) {
                var length;
                if ($('#warnRecord').hasClass('active')) {
                    length = $('#alarmRecordDataTable tbody tr').length;
                    $('#alarmRecord').css({
                        'overflow': 'auto',
                        'max-height': '250px'
                    });
                } else if ($('#operationLog').hasClass('active')) {
                    length = $('#loggingDataTable tbody tr').length;
                    $('#logging').css({
                        'overflow': 'auto',
                        'max-height': '250px'
                    });
                }

                if (length <= 5) {
                    var changePx = (85 + length * 41 + 13 + 15) + 'px';
                    $('#videoRightTop').css('height', 'calc(100% - ' + changePx + ')');
                } else {
                    changePx = (85 + 5 * 41 + 13 + 15) + 'px';
                    $('#videoRightTop').css('height', 'calc(100% - ' + changePx + ')');
                }
            }
        },
        //获取底部高度
        getBottomHeight: function () {
            var length;
            if ($('#warnRecord').hasClass('active')) {
                length = $('#alarmRecordDataTable tbody tr').length;

            } else if ($('#operationLog').hasClass('active')) {
                length = $('#loggingDataTable tbody tr').length;
            }

            var changePx;
            if (length <= 5) {
                changePx = 85 + length * 41 + 13 + 15;
            } else {
                changePx = 85 + 5 * 41 + 13 + 15;
            }
            return changePx;
        },
        /**
         * 左下侧隐藏及全屏显示时视频分隔自适应计算函数
         */
        videoSeparatedAdaptHide: function () {
            //判断当前屏幕分隔数 区分屏幕分隔高宽度
            var rightHeight = $('#videoMainRight').height();
            if ($("#videoFour").hasClass("video-four-check")) {
                //高宽度
                var vwidth = 100 / 2;
                //区分全屏
                var vheight = (rightHeight - 42) / 2;
                $("#video-module>div").css({
                    "width": vwidth + "%",
                    "height": "calc(100% - (100% - " + vheight + "px))"
                });
            }
            else if ($("#videoSix").hasClass("video-six-check")) {
                //高宽度
                vwidth = 100 / 3;
                //区分全屏
                vheight = (rightHeight - 42) / 3;
                $("#video-module>div").css({
                    "width": vwidth + "%",
                    "height": "calc(100% - (100% - " + vheight + "px))"
                });
                //第一个视频高宽度
                var vOneWidth = vwidth * 2;
                var vOneHeight = vheight * 2;
                $("#video-module div.v-one").css({
                    "width": vOneWidth + "%",
                    "height": "calc(100% - (100% - " + (vOneHeight - 0.1) + "px))"
                });
            }
            else if ($("#videoNine").hasClass("video-nine-check")) {
                //高宽度
                vwidth = 100 / 3;
                //区分全屏
                vheight = (rightHeight - 42) / 3;
                $("#video-module>div").css({
                    "width": vwidth + "%",
                    "height": "calc(100% - (100% - " + vheight + "px))"
                });
            }
            else if ($("#videoTen").hasClass("video-ten-check")) {
                //高宽度
                vwidth = 100 / 5;
                //区分全屏
                vheight = (rightHeight - 42) / 5;
                $("#video-module>div").css({
                    "width": vwidth + "%",
                    "height": "calc(100% - (100% - " + vheight + "px))"
                });
                //第一个视频高宽度
                vOneWidth = vwidth * 4;
                vOneHeight = vheight * 4;
                $("#video-module div.v-one").css({
                    "width": vOneWidth + "%",
                    "height": "calc(100% - (100% - " + (vOneHeight - 0.1) + "px))"
                });
            }
            else if ($("#videoSixteen").hasClass("video-sixteen-check")) {
                //高宽度
                vwidth = 100 / 4;
                //区分全屏
                vheight = (rightHeight - 42) / 4;
                $("#video-module>div").css({
                    "width": vwidth + "%",
                    "height": "calc(100% - (100% - " + vheight + "px))"
                });
            }
        },
        /**
         * 左下侧显示及取消全屏显示时视频分隔自适应计算函数
         */
        videoSeparatedAdaptShow: function () {
            //判断当前屏幕分隔数 区分屏幕分隔高宽度
            var rightHeight = $('#videoMainRight').height();
            var bottomHeight = this.getBottomHeight();
            if ($("#videoFour").hasClass("video-four-check")) {
                //高宽度
                var vwidth = 100 / 2;
                var vheight = (rightHeight - bottomHeight) / 2;
                $("#video-module>div").css({
                    "width": vwidth + "%",
                    "height": "calc(100% - (100% - " + vheight + "px))"
                });
            }
            else if ($("#videoSix").hasClass("video-six-check")) {
                //高宽度
                vwidth = 100 / 3;
                vheight = (rightHeight - bottomHeight) / 3;
                $("#video-module>div").css({
                    "width": vwidth + "%",
                    "height": "calc(100% - (100% - " + vheight + "px))"
                });
                //第一个视频高宽度
                var vOneWidth = vwidth * 2;
                var vOneHeight = vheight * 2;
                $("#video-module div.v-one").css({
                    "width": vOneWidth + "%",
                    "height": "calc(100% - (100% - " + (vOneHeight - 0.1) + "px))"
                });
            }
            else if ($("#videoNine").hasClass("video-nine-check")) {
                //高宽度
                vwidth = 100 / 3;
                vheight = (rightHeight - bottomHeight) / 3;
                $("#video-module>div").css({
                    "width": vwidth + "%",
                    "height": "calc(100% - (100% - " + vheight + "px))"
                });
            }
            else if ($("#videoTen").hasClass("video-ten-check")) {
                //高宽度
                vwidth = 100 / 5;
                vheight = (rightHeight - bottomHeight) / 5;
                $("#video-module>div").css({
                    "width": vwidth + "%",
                    "height": "calc(100% - (100% - " + vheight + "px))"
                });
                //第一个视频高宽度
                vOneWidth = vwidth * 4;
                vOneHeight = vheight * 4;
                $("#video-module div.v-one").css({
                    "width": vOneWidth + "%",
                    "height": "calc(100% - (100% - " + (vOneHeight - 0.1) + "px))"
                });
            }
            else if ($("#videoSixteen").hasClass("video-sixteen-check")) {
                //高宽度
                vwidth = 100 / 4;
                vheight = (rightHeight - bottomHeight) / 4;
                $("#video-module>div").css({
                    "width": vwidth + "%",
                    "height": "calc(100% - (100% - " + vheight + "px))"
                });
            }
        },
        //报警处理
        warningManage: function (id) {
            var $this = this;
            var alarmData = alarmDataMap.get(id);

            $('.sendTextFooter').hide();
            $('.takePicturesFooter').hide();
            $("#alarm-remark").show();
            $("#smsTxt").val("");
            $("#time").val("");
            $("#alarmRemark").val("");

            layer.closeAll();
            $('#warningManage').modal('show');
            var url = "/clbs/v/monitoring/getDeviceTypeByVid";
            var postData = {"vehicleId": id};
            json_ajax("POST", url, "json", true, postData, function (result) {
                var warningType = result.obj.warningType;
                deviceTypeTxt = result.obj.deviceType;
                var alarmType = alarmData.globalAlarmSet.split(',');
                if (deviceTypeTxt === '11' || deviceTypeTxt === '20' || deviceTypeTxt === '21') {
                    $('.newDeviceInfo').show();
                    $('.oldDeviceInfo').hide();
                    $('#minResolution, #maxResolution').show();
                    $('#defaultValue').attr('selected', false);
                } else {
                    $('.newDeviceInfo').hide();
                    $('.oldDeviceInfo').show();
                    $('#minResolution, #maxResolution').hide();
                    $('#defaultValue').attr('selected', true);
                }

                var color = $("#color");
                for (var i = 0; i < alarmType.length; i++) {
                    var flag = $.inArray(alarmType[i], alarmTypeList);
                    if (flag !== -1) {
                        $("#warningManageListening").attr("disabled", "disabled");
                        $("#warningManagePhoto").attr("disabled", "disabled");
                        $("#warningManageSend").attr("disabled", "disabled");
                        $("#warningManageAffirm").attr("disabled", "disabled");
                        $("#warningManageFuture").attr("disabled", "disabled");
                        $("#warningManageCancel").attr("disabled", "disabled");
                        color.show();
                        color.text(alarmDisabled);
                        break;
                    }
                }
                var url1 = "/clbs/a/search/findEndTime";
                var data1 = {"vehicleId": id, "type": alarmData.globalAlarmSet, "startTime": alarmData.alarmTime};
                layer.load(2);
                json_ajax("POST", url1, "json", true, data1, function (result) {
                    if (result.success === true) {
                        if (result.msg === "0") {
                            color.show();
                            color.text(alarmError);
                            $("#warningManageListening").attr("disabled", "disabled");
                            $("#warningManagePhoto").attr("disabled", "disabled");
                            $("#warningManageSend").attr("disabled", "disabled");
                            $("#warningManageAffirm").attr("disabled", "disabled");
                            $("#warningManageFuture").attr("disabled", "disabled");
                            $("#warningManageCancel").attr("disabled", "disabled");
                        } else {
                            color.show();
                            color.text(alarmDisabled);
                        }
                    } else {
                        $("#warningManageListening").removeAttr("disabled");
                        $("#warningManagePhoto").removeAttr("disabled");
                        $("#warningManageSend").removeAttr("disabled");
                        $("#warningManageAffirm").removeAttr("disabled");
                        $("#warningManageFuture").removeAttr("disabled");
                        $("#warningManageCancel").removeAttr("disabled");
                        color.hide();
                        $("#colorMore").hide();
                        layer.closeAll();
                    }
                });
                $("#listeningContent,#takePicturesContent,#sendTextMessages").hide();
                if (warningType === true || alarmData.monitorType === "人" || alarmData.alarmSource === "1") {
                    $("#warningHiden").removeAttr("style");
                    $("#warningManageListening").hide();
                    $("#warningManagePhoto").hide();
                    $("#warningManageSend").hide();
                    $("#sno").val("0");
                } else {
                    $("#warningHiden").attr("style", "text-align:center");
                    $("#warningManageListening").show();
                    $("#warningManagePhoto").show();
                    $("#warningManageSend").show();
                    $("#sno").val(alarmData.swiftNumber);
                }
                $("#warningCarName").text(alarmData.monitorName);
                $("#warningTime").text(alarmData.alarmTime);
                $("#warningGroup").text(alarmData.assignmentName);
                $("#warningDescription").text(alarmData.alarmType);
                $("#warningPeo").text(alarmData.professionalsName);
                $("#simcard").val(alarmData.simcardNumber);
                $("#device").val(alarmData.deviceNumber);
                $("#warningType").val(alarmData.globalAlarmSet);
                $("#vUuid").val(id);
                url = "/clbs/v/monitoring/getAlarmParam";
                var parameter = {"vehicleId": id, "alarm": alarmData.alarmType};
                json_ajax("POST", url, "json", true, parameter, $this.getAlarmParam);
            });
        },
        // 获取报警信息
        getAlarmParam: function (data) {
            $(".warningDeal").hide();
            var len = data.obj.length;
            var valueList = data.obj;
            if (len !== 0) {
                for (var i = 0; i < len; i++) {
                    var name = valueList[i].name;
                    var value = valueList[i].parameterValue;
                    var paramCode = valueList[i].paramCode;
                    if (name === "超速预警") {
                        $("#overSpeedGap").show();
                        $("#overSpeedGapValue").text(value);
                    }

                    if (name === "疲劳驾驶预警") {
                        $("#tiredDriveGap").show();
                        $("#tiredDriveGapValue").text(value);
                    }

                    if (name === "碰撞预警") {
                        $("#crashWarning").show();
                        if (paramCode === "param1") {
                            $("#crashTime").text(value);
                        } else if (paramCode === "param2") {
                            $("#crashSpeed").text(value);
                        }
                    }

                    if (name === "侧翻预警") {
                        $("#turnOnWarning").show();
                        $("#turnOnValue").text(value);
                    }

                    if (name === "超速报警") {
                        $("#overSpeeds").show();
                        if (paramCode === "param1") {
                            $("#warningSpeed").text(value);
                        } else if (paramCode === "param2") {
                            $("#warningAllTime").text(value);
                        }
                    }

                    if (name === "疲劳驾驶") {
                        $("#tiredDrive").show();
                        if (paramCode === "param1") {
                            $("#continuousDriveTime").text((value && value !== "null") ? value : "");
                        } else if (paramCode === "param2") {
                            $("#breakTime").text(value);
                        }
                    }

                    if (name === "当天累积驾驶超时") {
                        $("#addUpDrive").show();
                        $("#addUpDriveTime").text(value);
                    }

                    if (name === "超时停车") {
                        $("#overTimeStop").show();
                        $("#overTimeStopTime").text(value);
                    }

                    if (name === "凌晨2-5点行驶报警") {
                        $("#earlyRun").show();
                        $("#earlyRunValue").text(value);
                    }

                    if (name === "车辆非法位移") {
                        $("#displacementCar").show();
                        $("#displacementCarDistance").text(value);
                    }

                    if (name === "车机疑似屏蔽报警") {
                        $("#shieldWarning").show();
                        if (paramCode === "param1") {
                            $("#offLineTime").text(value);
                        } else if (paramCode === "param2") {
                            $("#offLineStartTime").text(value);
                        } else if (paramCode === "param3") {
                            $("#offLineEndTime").text(value);
                        }
                    }
                }
            }
        },
        /**
         * 获取报警位置
         */
        getAlarmAddress: function (id) {
            var alarmData = alarmDataMap.get(id);
            var url = '/clbs/v/monitoring/address';
            var param = {addressReverse: [alarmData.latitude, alarmData.longitude, '', "", 'vehicle']};

            $.ajax({
                type: "POST",
                url: url,
                dataType: "json",
                async: true,
                data: param,
                traditional: true,
                timeout: 8000,
                success: function (data) {
                    $('#alarm' + data.monitorId + 'address').html($.isPlainObject(data) ? '未定位' : data);
                },
            });
        },
        /**
         * 监听
         */
        goListen: function () {
            /**
             * 监听参数显示隐藏
             */
            var listeningContent = $("#listeningContent");
            if (listeningContent.is(":hidden")) {
                listeningContent.slideDown();
                $('.listenFooter').show();
                $('#takePicturesContent').hide();
                $('.takePicturesFooter').hide();
                $('#sendTextMessages').hide();
                $('.sendTextFooter').hide();
            } else {
                listeningContent.slideUp();
                $('.listenFooter').hide();
            }
            this.logFindCilck()
        },
        /**
         * 日志记录
         */
        logFindCilck: function () {
            if (logFlag === "true" && logTime) {
                var data = {eventDate: logTime, webType: 2};
                address_submit("POST", '/clbs/m/reportManagement/logSearch/findLog', "json", true, data, true, this.logFind.bind(this));
            }
        },
        /**
         * 查询日志列表数据响应事件
         */
        logFind: function (data) {
            var loggingDataTable = $("#loggingDataTable");
            loggingDataTable.children('tbody').empty();
            var html = '';
            var logType;
            var content;
            for (var i = 0; i < data.length; i++) {
                if (data[i].logSource === "1") {
                    logType = '终端上传';
                    content = "<a style='cursor: pointer' data-message='" + data[i].message + "'>" + data[i].monitoringOperation + "</a>";
                } else if (data[i].logSource === "2") {
                    logType = '平台下发';
                    content = data[i].message;
                } else {
                    logType = '平台操作';
                    if (data[i].monitoringOperation === "批量文本信息下发") {
                        content = "<a style='cursor: pointer' data-message='" + data[i].message + "'>" + data[i].monitoringOperation + "</a>";
                    } else {
                        content = data[i].message;
                    }
                }
                html += "<tr>" +
                    "<td>" + (i + 1) + "</td>" +
                    "<td>" + data[i].eventDate + "</td>" +
                    "<td>" + (data[i].ipAddress != null ? data[i].ipAddress : "") + "</td>" +
                    "<td>" + (data[i].username != null ? data[i].username : "") + "</td>" +
                    "<td>" + data[i].brand + "</td>" +
                    "<td>" + (data[i].plateColorStr === "" ? "-" : data[i].plateColorStr) + "</td>" +
                    "<td>" + content + "</td>" +
                    "<td>" + logType + "</td>" +
                    "</tr>";
            }
            loggingDataTable.children("tbody").append(html);
            $('#loggingDataTable tbody a').unbind('click').bind('click', this.showLogContent);
            if ($('#scalingBtn').hasClass('fa-chevron-down')) {
                this.videoSeparatedAdaptShow();
            }
            this.layoutChange();
        },
        /**
         * 日志详情弹窗展示
         */
        showLogContent: function () {
            var message = $(this).attr('data-message');
            $("#logDetailDiv").modal("show");
            $("#logContent").html(message);
        },
        /**
         * 拍照
         */
        photo: function () {
            /**
             * 拍照参数显示隐藏
             */
            var takePicturesContent = $("#takePicturesContent");
            if (takePicturesContent.is(":hidden")) {
                takePicturesContent.slideDown();
                $('.takePicturesFooter').show();
                $("#sendTextMessages").hide();
                $('.sendTextFooter').hide();
                $("#listeningContent").hide();
                $('.listenFooter').hide();
            } else {
                takePicturesContent.slideUp();
                $('.takePicturesFooter').hide();
            }
            this.logFindCilck();
            $('#videoMenu').hide();
        },
        /**
         * 下发短信界面
         */
        send: function () {
            var sendTextMessages = $("#sendTextMessages");
            if (sendTextMessages.is(":hidden")) {
                sendTextMessages.slideDown();
                $('.sendTextFooter').show();
                $("#takePicturesContent").hide();
                $('.takePicturesFooter').hide();
                $("#listeningContent").hide();
                $('.listenFooter').hide();
            } else {
                sendTextMessages.slideUp();
                $('.sendTextFooter').hide();
            }
            this.logFindCilck();
        },
        handleAlarm: function (handleType) {
            var startTime = $("#warningTime").text();
            var warningCarName = $("#warningCarName");
            var plateNumber = warningCarName.text();
            var description = $("#warningDescription").text();
            var vehicleId = $("#vUuid").val();
            var simcard = $('#simcard').val();
            var device = $("#device").val();
            var sno = $("#sno").val();
            var alarm = $("#warningType").val();
            var remark = $("#alarmRemark").val();
            var url = "/clbs/v/monitoring/handleAlarm";
            var data = {
                "vehicleId": vehicleId,
                "plateNumber": plateNumber,
                "alarm": alarm,
                "description": description,
                "handleType": handleType,
                "startTime": startTime,
                "simcard": simcard,
                "device": device,
                "sno": sno,
                "webType": 2,
                "remark": remark
            };
            json_ajax("POST", url, "json", true, data, function () {
                // 报警处理完毕后，延迟3秒进行结果查询
                setTimeout(pagesNav.gethistoryno, 3000);
            });
            $("#warningManage").modal('hide');
            this.updateHandleStatus(vehicleId);
            this.logFindCilck();
            layer.closeAll();
        },
        /**
         * 下发短信
         */
        goTxtSendForAlarm: function () {
            var vehicleId = $("#vUuid").val();
            $("#vidSendTxtForAlarm").val(vehicleId);
            var warningCarName = $("#warningCarName");
            $("#brandTxt").val(warningCarName.text());
            $("#alarmTxt").val($("#warningType").val());
            $("#startTimeTxt").val($("#warningTime").text());

            $("#simcardTxt").val($('#simcard').val());
            $("#deviceTxt").val($("#device").val());
            $("#snoTxt").val($("#sno").val());
            $("#handleTypeTxt").val("下发短信");
            $("#deviceTypeTxt").val(deviceTypeTxt);
            $("#description-Txt").val($("#warningDescription").text());
            $("#remark-Txt").val($("#alarmRemark").val());

            var smsTxt = $("#smsTxt").val();
            if (smsTxt == null || smsTxt.length === 0) {
                this.showErrorMsg("下发内容不能为空", "smsTxt");
                return;
            }
            if (smsTxt.length > 512) {
                layer.msg("下发内容不能超过512个字符");
                return;
            }
            var goTxtSendForAlarm = $("#goTxtSendForAlarm");
            goTxtSendForAlarm.attr("disabled", "disabled");
            goTxtSendForAlarm.removeAttr("disabled");
            $("#warningManage").modal('hide');
            this.updateHandleStatus(vehicleId);
            this.logFindCilck();
        },
        showErrorMsg: function (msg, inputId) {
            var errorLabel = $("#error_label");
            if (errorLabel.is(":hidden")) {
                errorLabel.text(msg);
                errorLabel.insertAfter($("#" + inputId));
                errorLabel.show();
            } else {
                errorLabel.is(":hidden");
            }
        },
        /**
         * 报警处理拍照下发
         */
        takePhotoForAlarm: function () {
            var goPhotographsForAlarm = $("#goPhotographsForAlarm");
            if (this.photoValidateForAlarm()) {
                var vehicleId = $("#vUuid").val();
                $("#vidforAlarm").val(vehicleId);
                var warningCarName = $("#warningCarName");
                $("#brandPhoto").val(warningCarName.text());
                $("#alarmPhoto").val($("#warningType").val());
                $("#startTimePhoto").val($("#warningTime").text());

                $("#simcardPhoto").val($('#simcard').val());
                $("#devicePhoto").val($("#device").val());
                $("#snoPhoto").val($("#sno").val());
                $("#handleTypePhoto").val("拍照");
                $("#description-photo").val($("#warningDescription").text());
                $("#remark-photo").val($("#alarmRemark").val());
                goPhotographsForAlarm.attr("disabled", "disabled");
                // 根据需求, 此处无需等待响应成功
                $("#warningManage").modal('hide');
                this.updateHandleStatus(vehicleId);
                this.logFindCilck();
            }
            goPhotographsForAlarm.removeAttr("disabled");
        },
        /**
         * 报警处理拍照下发数据校验
         */
        photoValidateForAlarm: function () {
            return $("#takePhotoForAlarm").validate({
                rules: {
                    wayID: {
                        required: true
                    },
                    time: {
                        required: true,
                        digits: true,
                        range: [0, 65535]
                    },
                    command: {
                        range: [0, 10],
                        required: true
                    },
                    saveSign: {
                        required: true
                    },
                    distinguishability: {
                        required: true
                    },
                    quality: {
                        range: [1, 10],
                        required: true
                    },
                    luminance: {
                        range: [0, 255],
                        required: true
                    },
                    contrast: {
                        range: [0, 127],
                        required: true
                    },
                    saturability: {
                        range: [0, 127],
                        required: true
                    },
                    chroma: {
                        range: [0, 255],
                        required: true
                    },
                },
                messages: {
                    wayID: {
                        required: alarmSearchChannelID
                    },
                    time: {
                        required: alarmSearchIntervalTime,
                        digits: alarmSearchIntervalError,
                        range: alarmSearchIntervalSize
                    },
                    command: {
                        range: alarmSearchPhotoSize,
                        required: alarmSearchPhotoNull
                    },
                    saveSign: {
                        required: alarmSearchSaveNull
                    },
                    distinguishability: {
                        required: alarmSearchResolutionNull
                    },
                    quality: {
                        range: alarmSearchMovieSize,
                        required: alarmSearchMovieNull
                    },
                    luminance: {
                        range: alarmSearchBrightnessSize,
                        required: alarmSearchBrightnessNull
                    },
                    contrast: {
                        range: alarmSearchContrastSize,
                        required: alarmSearchContrastNull
                    },
                    saturability: {
                        range: alarmSearchSaturatedSize,
                        required: alarmSearchSaturatedNull
                    },
                    chroma: {
                        range: alarmSearchColorSize,
                        required: alarmSearchColorNull
                    }
                }
            }).form();
        },
        /**
         * 监听下发
         */
        listenForAlarm: function () {
            var goListeningForAlarm = $("#goListeningForAlarm");
            if (this.listenValidate()) {
                var vehicleId = $("#vUuid").val();
                $("#vidforAlarmListen").val(vehicleId);
                $("#brandListen").val($("#warningCarName").text());
                $("#alarmListen").val($("#warningType").val());
                $("#startTimeListen").val($("#warningTime").text());

                $("#simcardListen").val($('#simcard').val());
                $("#deviceListen").val($("#device").val());
                $("#snoListen").val($("#sno").val());
                $("#handleTypeListen").val("监听");
                $("#descriptionListen").val($("#warningDescription").text());
                $("#remarkListen").val($("#alarmRemark").val());
                goListeningForAlarm.attr("disabled", "disabled");
                $("#listeningAlarm").ajaxSubmit(function () {
                    $("#warningManage").modal('hide');
                    goListeningForAlarm.removeAttr("disabled");
                });
            }
            goListeningForAlarm.removeAttr("disabled");
        },
        listenValidate: function () {
            return $("#listeningAlarm").validate({
                rules: {
                    monitorPhone: {
                        isNewTel: true,
                        required: true
                    },
                },
                messages: {
                    monitorPhone: {
                        required: '请输入电话号码'
                    },
                }
            }).form();
        },
        /**
         * 更新报警处理状态
         */
        updateHandleStatus: function (monitorId) {
            $('#alarm' + monitorId + 'handle').unbind('click').text("已处理").removeAttr("style");
        },
        /**
         * 获取当前服务器系统时间
         */
        getFormatTime: function (fn) {
            var url = '/clbs/v/monitoring/getTime';
            json_ajax('POST', url, 'json', true, null, function (data) {
                logTime = data;
                fn();
            });
        },
        /**
         * 自适应计算
         */
        layoutResize: function () {
            var $this = this;
            setTimeout(function () {
                $this.layoutChange();
                $this.windowResize();
            }, 30);
        },
        /**
         * 窗口大小改变后响应式
         */
        windowResize: function () {
            var $this = this;
            if ($('#scalingBtn').hasClass('fa fa-chevron-down')) {
                $this.videoSeparatedAdaptShow();//左下侧显示及取消全屏显示时视频分隔自适应计算函数
            } else {
                $this.videoSeparatedAdaptHide();//左下侧隐藏及全屏显示时视频分隔自适应计算函数
            }
        },
        /**
         * 检测是否有处理报警的权限
         */
        checkAlarmRole: function () {
            var surl = '/clbs/v/monitoring/checkAlarmRole';
            json_ajax("POST", surl, "json", true, {}, function (data) {
                if (data.success) {
                    alarmRole = true;
                }
            })
        },
        setTreeModule: function (module) {
            Tree = module;
        },
        //特殊参数设置表单显示隐藏函数
        specialParamSetShow: function () {
            var surl = '/clbs/realTimeVideo/video/getDiskInfo';
            $('#diskSaveModal label.error').remove();
            json_ajax("POST", surl, "json", true, {}, function (data) {
                if (data.success) {
                    // 视频播放缺省时间
                    var videoPlayTime = data.obj.videoPlayTime;
                    // 视频空闲断开时间
                    var videoStopTime = data.obj.videoStopTime;
                    // FTP存储空间
                    var memoryRate = data.obj.memoryRate;
                    // FTP音视频存储状态
                    var memory = data.obj.memory;
                    var lineColor;
                    if (memory <= 60) {
                        lineColor = 'green';
                    } else if (memory > 60 || memory <= 90) {
                        lineColor = 'yellow';
                    } else {
                        lineColor = 'red';
                    }
                    // 存储空间满后处理类型：0：空间满后自动覆盖； 1：空间满后停止录制
                    var memoryType = data.obj.memoryType;
                    $('#videoPlayTime').val(videoPlayTime);
                    $('#videoRequestTime').val(videoStopTime);
                    $('#ftpStorage').val(memoryRate);
                    $('#videoMemory').text(memory + '%');
                    $('#videoSaveState').val(memory);
                    $('#videoMemoryProgress').css({
                        'width': memory + '%',
                        'background-color': lineColor
                    });
                    $("#specialParamSet input:radio[name='type'][value=" + memoryType + "]").prop('checked', 'true');
                }
            });
            $('#diskSaveModal').modal('show')
        },
        //特殊参数表单提交
        doSubmit: function () {
            var $this = this;
            if ($this.validates()) {
                $("#specialParamSet").ajaxSubmit(function (data) {
                    if (data != null) {
                        var result = $.parseJSON(data);
                        if (result.success) {
                            $('#diskSaveModal').modal('hide');
                            $this.getTimeout();

                        } else {
                            layer.msg(result.msg, {move: false});
                        }
                    }
                });
            }
        },
        //特殊参数表单验证
        validates: function () {
            return $("#specialParamSet").validate({
                rules: {
                    videoPlayTime: {
                        required: true,
                        digits: true,
                        range: [30, 86400]
                    },
                    videoStopTime: {
                        required: true,
                        digits: true,
                        range: [30, 600]
                    },
                    memoryRate: {
                        required: true,
                        digits: true,
                        range: [1, 100]
                    }
                },
                messages: {
                    videoPlayTime: {
                        required: "缺省时间未设置",
                        digits: "必须是正整数哦！",
                        range: "输入值范围为30-86400"
                    },
                    videoStopTime: {
                        required: "断开时间未设置",
                        digits: "必须是正整数哦！",
                        range: "输入值范围为30-600"
                    },
                    memoryRate: {
                        required: "FTP存储空间未设置",
                        digits: "必须是正整数哦！",
                        range: "输入值范围为1-100"
                    }
                }
            }).form();
        },
        // 获取操作超时时间
        getTimeout: function () {
            var surl = '/clbs/realTimeVideo/video/getDiskInfo';
            json_ajax("POST", surl, "json", true, {}, function (data) {
                if (data.success) {
                    // 视频播放缺省时间
                    handleTime = Number(data.obj.videoPlayTime) * 1000;
                    // 视频空闲时间
                    videoFreeTime = Number(data.obj.videoStopTime) * 1000;
                } else {
                    handleTime = 300000;
                    videoFreeTime = 30000;
                }
            })
        }
    }

    window.logFindCilck = function () {
        table.logFindCilck();
    };

    $(function () {
        $('#scalingBtn').on('click', table.tableStateChange.bind(table));
        $('#warningManageListening').on('click', table.goListen.bind(table));
        $("#warningManagePhoto").bind("click", table.photo.bind(table));
        $("#warningManageSend").bind("click", table.send.bind(table));
        $("#warningManageAffirm").bind("click", function () {
            table.handleAlarm("人工确认报警")
        });
        $("#warningManageCancel").bind("click", function () {
            table.handleAlarm("不做处理")
        });
        $("#warningManageFuture").bind("click", function () {
            table.handleAlarm("将来处理")
        });
        /**
         * 报警处理模态框关闭事件
         */
        $("#warningManageClose").on("click", function () {
            $("#warningManage").modal('hide')
        });
        $("#goTxtSendForAlarm").bind("click", table.goTxtSendForAlarm);
        $("#goPhotographsForAlarm").bind("click", table.takePhotoForAlarm);
        $("#goListeningForAlarm").bind("click", table.listenForAlarm);
        $("#operationLog").on("click", table.layoutResize.bind(table));
        $('#warnRecord').on('click', table.layoutResize.bind(table));
        $(window).resize(function () {
            table.windowResize();
        });
        //判断用户账号是否为admin
        var username = $("#userName").text();
        if (username === "admin") {
            $("#setBtn").show();
            //特殊参数设置弹窗显示
            $("#setBtn").on("click", table.specialParamSetShow);
            $('#submitBtn').on('click', table.doSubmit.bind(table));
        } else {
            $("#setBtn").hide();
        }
    });

    return {
        table: table
    };
});