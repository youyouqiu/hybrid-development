$(function () {
    $('input').inputClear().on('onClearEvent', function (e, data) {
        var id = data.id;
        if (id == 'search_condition') {
            treeMonitoring.alltree();
        }
        ;
        if (id == 'searchVehicle') {
            fenceOperation.initBindFenceTree();
        }
        ;
        if (id == 'searchFence') {
            search_ztree('fenceDemo', id, 'fence');
        }
        ;
        if (id == 'vFenceSearch') {
            search_ztree('vFenceTree', id, 'fence');
        }
        ;
        if (id == 'startPoint' || id == 'endPoint' || id.indexOf('wayPoint') != -1) {
            $('#' + id).attr('data-address', '').removeAttr('data-lnglat');
        }
        ;
    });
    //地图
    var lineVid = []; //在线车辆id
    var allCid = [];
    var missVid = [] //离线车辆id
    var lineAndRun = [] //在线行驶id;
    var lineAndStop = []; //在线停止id
    var lineAndAlarm = []; //报警
    var lineAndmiss = []; //未定位
    var offLineTable = [];
    var overSpeed = [];
    var vnodesId = [];
    var vnodemId = [];
    var vnodelmId = [];
    var vnoderId = [];
    var vnodeaId = [];
    var vnodespId = [];
    var markerRealTime;
    var lineArr = [];
    var pathsTwo = null;
    var myTable;
    var nmoline;
    //初始化页面
    pageLayout.init();
    pageLayout.arrayExpand();
    pageLayout.createMap();
    pageLayout.responseSocket();
    fenceOperation.init();
    fenceOperation.fenceBindList();
    // fenceOperation.fenceEnterprise();
    amapOperation.init();
    treeMonitoring.init();
    $("[data-toggle='tooltip']").tooltip();
    //右边菜单显示隐藏切换
    $("#toggle-left").on("click", pageLayout.toggleLeft);
    //左侧操作树点击隐藏
    $("#goHidden").on("click", pageLayout.goHidden);
    //左侧操作树点击显示
    $goShow.on("click", pageLayout.goShow);
    //输入时自动查询
    var inputChange;
    //显示别名、显示车辆树
    var aliasesVal = $('#showAliases input').is(':checked') ? 1 : 0;
    var showTreeCountVal = $('#monitorCountInput').is(':checked') ? 1 : 0;


    // $("#search_condition").unbind("focus");
    $("#search_condition").on('input propertychange', function (value) {
        if (inputChange !== undefined) {
            clearTimeout(inputChange);
        }
        inputChange = setTimeout(function () {
            // search
            treeMonitoring.search_condition();
        }, 500);
    });
    //右键显示菜单节点跳动问题
    scorllDefaultTreeTop = 0;
    $("#thetree").scroll(function () {
        scorllDefaultTreeTop = $("#thetree").scrollTop();
    });
    // 搜索类型下拉框change事件
    $("#searchType").on('change', function () {
        if ($('#search_condition').val() != '') {
            treeMonitoring.search_condition();
        }
    });
    //刷新文件树
    $("#refresh").on("click", treeMonitoring.refreshTree);
    $('#originalDataModalClose').on('click', treeMonitoring.modalCloseFun)
    $('#controlGetData').on('click', treeMonitoring.isGetOriginalData);
    // $('#copyOriginalData').on('click', treeMonitoring.copyDataFun);
    $('#clearOriginalData').on('click', treeMonitoring.clearDataFun);
    //状态信息可以拖动
    $("#realTimeStatus").on("click", function () {
        flagState = true;
        activeIndex = 1;
        dataTableOperation.closePopover();
        dataTableOperation.carStateAdapt(activeIndex);
        dataTableOperation.enableRowUpdate('state');
    });
    //报警记录不拖动
    $("#realTtimeAlarm").on("click", function () {
        flagState = false;
        activeIndex = 3;
        dataTableOperation.closePopover();
        dataTableOperation.carStateAdapt(activeIndex);
        dataTableOperation.enableRowUpdate('alarm');
    });
    //日志点击不拖动
    $("#operationLog").on("click", function () {
        flagState = false;
        activeIndex = 4;
        dataTableOperation.closePopover();
        dataTableOperation.carStateAdapt(activeIndex);
        // dataTableOperation.logFindCilck(true)
    });

    //主动安全不拖动
    $("#activeSafetyTab").on("click", function () {
        flagState = false;
        activeIndex = 5;
        dataTableOperation.carStateAdapt(activeIndex)
    });
    //OBD数据不拖动
    $("#obdInfoBoxTab").on("click", function () {
        flagState = false;
        activeIndex = 2;
        dataTableOperation.closePopover();
        dataTableOperation.carStateAdapt(activeIndex);
        dataTableOperation.enableRowUpdate('obd');
    });

    $("#dragDIV").mousedown(pageLayout.dragDiv);
    //$("#btn-videoRealTime-show").on("click",pageLayout.videoRealTimeShow);
    //报警弹窗显示
    $("#showAlarmWin").on("click", pageLayout.showAlarmWindow);
    //报警数量块单击
    // $showAlarmWinMark.bind("click", pageLayout.showAlarmWinMarkRight);
    $('#ActiveSafetybtn').bind('click', pageLayout.showAlarmWinMarkRight);
    $("#callPolice").bind('click', pageLayout.showAlarmWinMarkShield);
    //屏蔽浏览器右键菜单
    $(".contextMenuContent,#showAlarmWin").bind("contextmenu", function (e) {
        return false;
    });
    $showAlarmWinMark.contextmenu();
    //最小化
    $(".alarmSettingsSmall").bind("click", pageLayout.alarmToolMinimize);
    //关闭声音
    $(".alarmSound").bind("click", pageLayout.alarmOffSound);
    //关闭闪烁
    $(".alarmFlashes").bind("click", pageLayout.alarmOffFlashes);
    // 应答确定
    $('#parametersResponse').on('click', pageLayout.platformMsgAck);
    //点击显示报警设置详情
    $(".alarmSettingsBtn").bind("click", pageLayout.showAlarmInfoSettings);
    $("ul.dropdown-menu").on("click", function (e) {
        e.stopPropagation();
    });
    // 电子围栏  树结构搜索
    $("#searchFence").bind('input oninput', fenceOperation.searchFenceCarSearch);
    //电子围栏搜索-刷新功能
    $("#refreshFence").on('click', function () {
        $("#searchFence").val('');
        fenceOperation.searchFenceCarSearch();
    });
    $("#vFenceSearch").bind('input oninput', treeMonitoring.vsearchFenceCarSearch);
    $("#fixSpan").mouseover(function () {
        $("#recentlyC").removeClass("hidden")
    });
    $("#fixSpan").mouseout(function () {
        $("#recentlyC").mouseover(function () {
            $("#recentlyC").removeClass("hidden");
        });
        $("#recentlyC").mouseout(function () {
            $("#recentlyC").addClass("hidden");
        });
    });
    $("#warningManageClose").on("click", function () {
        $("#warningManage").modal('hide')
    });
    $("#warningManageListening").bind("click", dataTableOperation.getListen);
    $("#warningManagePhoto").bind("click", dataTableOperation.photo);
    $("#warningManageSend").bind("click", dataTableOperation.send);
    $("#warningManageAffirm").bind("click", function () {
        dataTableOperation.handleAlarm("人工确认报警")
    });
    $("#warningManageCancel").bind("click", function () {
        dataTableOperation.handleAlarm("不做处理")
    });
    $("#warningManageFuture").bind("click", function () {
        dataTableOperation.handleAlarm("将来处理")
    });
    // $("#magnifyClick, #shrinkClick, #countClick, #queryClick, #defaultMap, #realTimeRC, #btn-videoRealTime-show, #displayClick,#mapDropSetting").on("click",amapOperation.toolClickList);

    // wjk
    //先注释掉
    // $("#magnifyClick, #shrinkClick, #countClick, #queryClick, #defaultMap, #realTimeRC, #btn-videoRealTime-show, #displayClick,#mapDropSetting,#phoneCall").on("click",amapOperation.toolClickList);
    $("#magnifyClick, #shrinkClick, #countClick, #queryClick, #defaultMap, #realTimeRC, #btn-videoRealTime-show, #displayClick,#mapDropSetting").on("click", amapOperation.toolClickList);

    $("#toolClick").on("click", pageLayout.toolClick);
    $("#save").bind("click", fenceOperation.doSubmits1);

    //清空添加关键点表单
    $("#monitoringTagClose").bind("click", function () {
        $("#addMonitoringTag input").each(function (index, input) {
            $(input).val('');
        })
        $("#addMonitoringTag #description").val('');
    });

    $("#searchCarClose").on("click", fenceOperation.searchCarClose);
    $("#annotatedSave").on("click", fenceOperation.annotatedSave);
    $("#threadSave").on("click", fenceOperation.threadSave);
    $("#rectangleSave").on("click", fenceOperation.rectangleSave);
    $("#polygonSave").on("click", fenceOperation.polygonSave);
    $("#roundSave").on("click", fenceOperation.roundSave);
    $(".modalClose").on("click", fenceOperation.clearErrorMsg);
    $("#searchVehicle").bind('input oninput', fenceOperation.searchVehicleSearch);
    // 滚动展开绑定围栏的监控对象树结构
    // 滚动展开
    $("#bindVehicleTreeDiv").scroll(function () {
        var zTree = $.fn.zTree.getZTreeObj("treeDemoFence");
        zTreeScroll(zTree, this);
    });
    // 点击添加(按围栏 )
    $("#addBtn").bind("click", fenceOperation.addBtnClick);
    $("#tableCheckAll").bind("click", fenceOperation.tableCheckAll);
    //点击移除围栏
    $("#removeBtn").bind("click", fenceOperation.removeBtnClick);
    //check选择
    $("#checkAll").bind("click", fenceOperation.checkAllClick);
    //依例全设
    $("#setAll").bind("click", fenceOperation.setAllClick);
    // 提交(按围栏)
    $("#fenceSaveBtn").bind("click", fenceOperation.fenceSaveBtnClick);
    // 围栏绑定-取消按钮
    $("#fenceCancelBtn").bind("click", fenceOperation.fenceCancelBtnClick);
    // 批量下发
    $("#send_model").bind("click", fenceOperation.sendModelClick);
    //批量删除
    $("#del_model").bind("click", fenceOperation.delModelClick);
    // 模糊搜索围栏绑定列表
    $("#search_button").bind("click", fenceOperation.searchBindTable);
    $("body").bind("click", fenceOperation.bodyClickEvent);
    $("#hourseSelect tr td").bind("click", fenceOperation.hourseSelectClick);
    $("#minuteSelect tr td").bind("click", fenceOperation.minuteSelectClick);
    $("#secondSelect tr td").bind("click", fenceOperation.secondSelectClick);
    //切换电子围栏
    $("#TabCarBox").bind("click", fenceOperation.TabCarBox);
    //切换监控对象
    $("#TabFenceBox").bind("click", fenceOperation.TabFenceBox);
    $("#rectangleEditClose").bind("click", fenceOperation.rectangleEditClose);
    //围栏取消
    $("#markFenceClose").bind("click", fenceOperation.markFenceClose);
    $("#saveSection").bind("click", fenceOperation.doSubmitsMonitor);
    $("#lineEditClose").bind("click", fenceOperation.lineEditClose);
    $("#circleFenceClose").bind("click", fenceOperation.circleFenceClose);
    $("#polygonFenceClose").bind("click", fenceOperation.polygonFenceClose);
    $("#bingListClick").bind("click", fenceOperation.bingListClick);
    $("#fenceDemo").bind('contextmenu', function (event) {
        return false
    });
    $("#parameters").bind("click", function () {
        treeMonitoring.parameter(parametersID)
    });
    $("#askQuestions-add-btn").on("click", fenceOperation.addaskQuestions);
    $("#goPhotographs").bind("click", dataTableOperation.takePhoto);
    $("#goRecordCollect").bind("click", treeMonitoring.goRecordCollect);
    $("#goRecordSend").bind("click", treeMonitoring.goRecordSend);
    $("#goListeningForAlarm").bind("click", dataTableOperation.listenForAlarm);
    $("#goPhotographsForAlarm").bind("click", dataTableOperation.takePhotoForAlarm);
    $("#goVideotapes").bind("click", dataTableOperation.getVideo);
    $("#goRegularReport").bind("click", dataTableOperation.goRegularReport);
    $("#goDistanceReport").bind("click", dataTableOperation.goDistanceReport);
    $("#goTimeInterval").bind("click", dataTableOperation.goTimeInterval);
    $("#parametersPlate").bind("click", dataTableOperation.parametersPlate);
    $("#toSetOBD").bind("click", dataTableOperation.toSetOBD);
    $("#gpListening").bind("click", dataTableOperation.gpListening);
    $("#goOverspeedSettings").bind("click", dataTableOperation.goOverspeedSettings);
    $("#emergency").bind("click", dataTableOperation.emergency);
    $("#displayTerminalDisplay").bind("click", dataTableOperation.displayTerminalDisplay);
    $("#tts").bind("click", dataTableOperation.tts);
    $("#advertisingDisplay").bind("click", dataTableOperation.advertisingDisplay);
    $("#goTxtSend").bind("click", dataTableOperation.goTxtSend);
    $("#goTxtSendForAlarm").bind("click", dataTableOperation.goTxtSendForAlarm);
    $("#emergency1").bind("click", dataTableOperation.emergency1);
    $("#tts1").bind("click", dataTableOperation.tts1);
    $("#advertisingDisplay1").bind("click", dataTableOperation.advertisingDisplay1);
    $("#goSendQuestion").bind("click", dataTableOperation.goSendQuestion);
    $("#goTelBack").bind("click", dataTableOperation.goTelBack);
    $("#goMultimediaRetrieval").bind("click", dataTableOperation.goMultimediaRetrieval);
    $("#deleteSign").bind("click", dataTableOperation.deleteSign);
    $("#goMultimediaUploads").bind("click", dataTableOperation.goMultimediaUploads);
    $("#goRecordUpload").bind("click", dataTableOperation.goRecordUpload);
    $("#goOriginalOrder").bind("click", dataTableOperation.goOriginalOrder);
    //录像时间轴
    $("#videoPlay").on("click", dataTableOperation.recordingTimelinePlay);
    //录音时间轴
    $("#voicePlay").on("click", dataTableOperation.tapingTimelinePlay);
    $("#sectionRateLimitingClose").on("click", fenceOperation.sectionRateLimitingClose);
    $("#goInfoService").bind("click", dataTableOperation.goInfoService);
    //上报频率设置
    $("#reportSet").bind("click", dataTableOperation.reportSet);
    //定点和校时
    $("#goInfofixedPointAndTiming").bind("click", dataTableOperation.goInfofixedPointAndTiming);
    //位置跟踪
    $("#positionTrailing").bind("click", dataTableOperation.positionTrailing);
    //透传指令
    $("#goThroughOrder").bind("click", dataTableOperation.goThroughOrder);

    $('.lngLat_show').on('click', fenceOperation.lngLatTextShow);
    $('#province, #city, #district, #street').on('change', function () {
        fenceOperation.administrativeAreaSelect(this)
    });
    $('#administrativeSave').on('click', fenceOperation.administrativeSave);
    $('#administrativeClose').on('click', fenceOperation.administrativeClose);
    $('#tableCheckAll').on('click', function () {
        $("input[name='subChk']").prop("checked", this.checked);
    });
    //树优化测试代码
    $online.bind("click", {
        type: 1
    }, treeMonitoring.onlines);
    $chooseNot.bind("click", {
        type: 6
    }, treeMonitoring.onlines);
    $chooseAlam.bind("click", {
        type: 4
    }, treeMonitoring.onlines);
    $chooseRun.bind("click", {
        type: 3
    }, treeMonitoring.onlines);
    $chooseStop.bind("click", {
        type: 2
    }, treeMonitoring.onlines);
    $chooseOverSeep.bind("click", {
        type: 5
    }, treeMonitoring.onlines);
    $("#chooseHeartBeat").bind("click", {
        type: 9
    }, treeMonitoring.onlines);
    /* $("#chooseMissLine").bind("click",treeMonitoring.misslines);*/
    $("#chooseAll").bind("click", treeMonitoring.alltree);
    /* $("#chooseMiss").bind("click",treeMonitoring.misslines);*/

    var p = 0,
        t = 0,
        y = 0;
    /*$("#thetree").scroll(function () {
        var zTree = $.fn.zTree.getZTreeObj("treeDemo");
        p = $(this).scrollTop();
        // console.log('p', p);
        if (t <= p) {//下滚
            // 获取没有展开的分组节点
            var notExpandNodes = zTree.getNodesByFilter(assignmentNotExpandFilter);
            if (notExpandNodes != undefined && notExpandNodes.length > 0) {
                for (var i = 0; i < notExpandNodes.length; i++) {
                    var node = notExpandNodes[i];
                    var tid = node.tId + "_a";
                    var divHeight = $("#thetree").offset().top;
                    var nodeHeight = $("#" + tid).offset().top;
                    if (nodeHeight - divHeight > 696) {
                        break;
                    }
                    if (nodeHeight - divHeight > 0 && nodeHeight - divHeight < 696) {
                        var zTree = $.fn.zTree.getZTreeObj("treeDemo");
                        zTree.expandNode(node, true, true, false, true);
                        node.children[0].open = true;
                    }
                }
            }
        }
        setTimeout(function () {
            t = p;
        }, 0);
    });*/


    //树优化测试代码
    $('#addWayToPoint').on('click', fenceOperation.addWayToPoint);
    $('#lineDragRouteClose').on('click', fenceOperation.lineDragRouteClose);
    $('#lineDragRouteSave').on('click', fenceOperation.lineDragRouteSave);
    //围栏所属企业
    $('#markerFenceEnterprise, #markerFenceEnterprise-select, #lineFenceEnterprise, #lineFenceEnterprise-select, #rectangleFenceEnterprise, #rectangleFenceEnterprise-select, #circleFenceEnterprise, #circleFenceEnterprise-select, #polygonFenceEnterprise, #polygonFenceEnterprise-select, #areaFenceEnterprise, #areaFenceEnterprise-select, #dragRouteFenceEnterprise, #dragRouteFenceEnterprise-select').on('click', treeMonitoring.enterpriseShow);
    //显示设置
    $("#smoothMove,#logoDisplay,#icoUp").on("click", pageLayout.smoothMoveOrlogoDisplayClickFn);
    //地图设置
    $(".mapSubMenu input").on("change", pageLayout.mapSubMenuClickFn);
    $(".mapStyleMenu input").on("change", pageLayout.mapStyleChange);
    $(".mapTypeRadio").on("change", pageLayout.mapTypeChange);
    $(".jumpSettingMenu span").on("click", pageLayout.jumpSettingChange);
    $("#fenceToolBtn").on("click", treeMonitoring.fenceToolClickSHFn);
    // 路况
    $(".trafficBtn").on("click", amapOperation.realTimeRC);
    // 鹰眼
    $(".hawkEyeBtn").on("click", amapFunCollection.overViewFun);
    $("#TabCarBox,#TabFenceBox").on("click", treeMonitoring.fenceAndVehicleFn);
    // 跳转至轨迹回放
    $("#treeToTrackBlack").on("click", treeMonitoring.treeToTrackBlack);


    //关闭并还原提问下发表单
    $("#closeSendQuestion").on("click", function () {
        $("#askQuestionsIssued input[type='text']").val('');
        $("#askQuestionsIssued .error").hide();
        $('#answer-add-content div[id^="answer-add"]').remove();
    })
    //监听提问下发表单中input的value变化
    var askQuestionsIssued = $("#askQuestionsIssued");
    askQuestionsIssued.on("input propertychange change", 'input', function (event) {
        var inputVal = $(this).val();
        if (inputVal == "") {
            $(this).siblings(".error").show();
        } else {
            $(this).siblings(".error").hide();
        }
        //dataTableOperation.sendQuestionValidateTwo();
    });
    $("#askQuestionsIssued input").inputClear().on('onClearEvent', function (e) {
        $(this).siblings(".error").show();
    });

    //点击弹窗中的取消按钮后隐藏表单验证报错信息
    $("button").on("click", function () {
        var thisMiss = $(this).data("dismiss");
        if (thisMiss == "modal") {
            $("label.error").hide();
        }
    })
    dataTableOperation.checkAlarmRole();

    //车辆上下线(声音、光提醒下拉菜单控制)
    $('#onlineSetting').on('click', function () {
        var onlineSettingMenu = $('#onlineSettingMenu');
        if (onlineSettingMenu.is(':hidden')) {
            onlineSettingMenu.slideDown();
        } else {
            onlineSettingMenu.slideUp();
        }
    });
    $('.onlineMenuSelect').on('change', function () {
        var _this = $(this);
        if (_this.is(":checked")) {
            _this.siblings('label').addClass('preBlue');
        } else {
            _this.siblings('label').removeClass('preBlue');
            if (_this.attr('id') == 'flashing') { // 关闭闪烁
                $('#onlineWaves').hide();
            } else { // 关闭声音
                if (navigator.userAgent.indexOf('MSIE') >= 0) {
                    if ($("#IEalarmMsg").length > 0) {
                        $("#IEalarmMsg")[0].pause();
                    }
                } else {
                    if ($("#alarmMsgAutoOff").length > 0) {
                        $("#alarmMsgAutoOff")[0].pause();
                    }
                }
            }
        }
    });
    // 量算
    $("#distanceMeasuremenLab, #areaMeasurementLab").on('click', pageLayout.measurementEvent);
    $('#toSearchFence').on('click', treeMonitoring.toSearchFence);
    //人证对比
    $('#personCompareBtn, #compareReset').on('click', treeMonitoring.comparePersonFun);
    $('#compareClose').on('click', treeMonitoring.compareCloseFun);
    // 主动安全视频播放下的TTS读播
    $('#toTextInfoSend').on('click', treeMonitoring.textInfoSend);
    // 主动安全视频播放下的视频控制功能
    $('#videoSelect').on("click", dataTableOperation.videoBtnClick);
    // 主动安全视频播放下的对讲功能
    $('#callSelect').on('click', dataTableOperation.callSelectFun);
    // 高德地图天气
    $('#weatherBtn').on('click', function () {
        if (map.getMapCenterCity) {
            map.getMapCenterCity();
        }
    });
    // 监控对象别名显示控制
    $('#showAliases').on('change', function () {
        if ($('#showAliases input').is(':checked')) {
            // $('#showAliases i').css('opacity', 1);
            $('.aliasesStyle').show();
            aliasesClass = 'aliasesStyle aliasesShow';
            aliasesVal = 1;

        } else {
            // $('#showAliases i').css('opacity', 0);
            $('.aliasesStyle').hide();
            aliasesClass = 'aliasesStyle';
            aliasesVal = 0;
        }

        var param = {
            aliasesFlag: aliasesVal,
            showTreeCountFlag: showTreeCountVal,
        };
        var url = '/clbs/v/monitoring/setTreeShow';
        json_ajax("POST", url, "json", false, param, function (data) {
            console.log('data', data);
        });
    });
    // 监控对象数量显示控制
    $('#monitorCountInput').on('change', function () {
        if ($('#monitorCountInput').is(':checked')) {
            $('.countStyle').addClass('countShow').show();
            countClass = 'countStyle countShow';
            showTreeCountVal = 1;
        } else {
            $('.countStyle').removeClass('countShow').hide();
            countClass = 'countStyle';
            showTreeCountVal = 0;
        }

        var url = '/clbs/v/monitoring/setTreeShow';
        var param = {
            aliasesFlag: aliasesVal,
            showTreeCountFlag: showTreeCountVal,
        };
        json_ajax("POST", url, "json", false, param, function (data) {
            console.log('data', data);
        })
    });
    setTimeout(function () {
        pageLayout.getNowFormatDate();
        //获取图标方向
        var icoUrl = '/clbs/m/personalized/ico/getIcodirection';
        /*json_ajax("POST", icoUrl, "json", false, null, function (data) {
            var icoMsg = data.msg;
            if (icoMsg == 'true') {
                icoUpFlag = true;
                $("#icoUp").attr("checked", true);
                $("#icoUpLab").addClass("preBlue");
            } else {
                icoUpFlag = false;
                $("#icoUp").attr("checked", false);
                $("#icoUpLab").removeClass("preBlue");
            }
        });*/
    }, 0);
})