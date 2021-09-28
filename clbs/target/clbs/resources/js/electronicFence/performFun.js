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
    var lineVid = [];//在线车辆id
    var allCid = [];
    var missVid = []//离线车辆id
    var lineAndRun = []//在线行驶id;
    var lineAndStop = [];//在线停止id
    var lineAndAlarm = [];//报警
    var lineAndmiss = [];//未定位
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
    pageLayout.getNowFormatDate();
    $("[data-toggle='tooltip']").tooltip();
    //右边菜单显示隐藏切换
    $("#toggle-left").on("click", pageLayout.toggleLeft);
    //左侧操作树点击隐藏
    $("#goHidden").on("click", pageLayout.goHidden);
    //左侧操作树点击显示
    $goShow.on("click", pageLayout.goShow);
    //输入时自动查询
    var inputChange;
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
    $("#searchType").change(treeMonitoring.search_condition);
    //刷新文件树
    $("#refresh").on("click", treeMonitoring.refreshTree);
    $('#originalDataModalClose').on('click', treeMonitoring.modalCloseFun)
    $('#controlGetData').on('click', treeMonitoring.isGetOriginalData);
    // $('#copyOriginalData').on('click', treeMonitoring.copyDataFun);
    $('#clearOriginalData').on('click', treeMonitoring.clearDataFun);

    $("#dragDIV").mousedown(pageLayout.dragDiv);
    //$("#btn-videoRealTime-show").on("click",pageLayout.videoRealTimeShow);
    //报警弹窗显示
    $("#showAlarmWin").on("click", pageLayout.showAlarmWindow);
    //报警数量块单击
    $showAlarmWinMark.bind("click", pageLayout.showAlarmWinMarkRight);
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
    $("#goRecordCollect").bind("click", treeMonitoring.goRecordCollect);
    $("#goRecordSend").bind("click", treeMonitoring.goRecordSend);
    $("#sectionRateLimitingClose").on("click", fenceOperation.sectionRateLimitingClose);

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
    $online.bind("click", {type: 1}, treeMonitoring.onlines);
    $chooseNot.bind("click", {type: 6}, treeMonitoring.onlines);
    $chooseAlam.bind("click", {type: 4}, treeMonitoring.onlines);
    $chooseRun.bind("click", {type: 3}, treeMonitoring.onlines);
    $chooseStop.bind("click", {type: 2}, treeMonitoring.onlines);
    $chooseOverSeep.bind("click", {type: 5}, treeMonitoring.onlines);
    $("#chooseHeartBeat").bind("click", {type: 9}, treeMonitoring.onlines);
    /* $("#chooseMissLine").bind("click",treeMonitoring.misslines);*/
    $("#chooseAll").bind("click", treeMonitoring.alltree);
    /* $("#chooseMiss").bind("click",treeMonitoring.misslines);*/

    var p = 0, t = 0, y = 0;
    $("#thetree").scroll(function () {
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
    });


    //树优化测试代码
    $('#addWayToPoint').on('click', fenceOperation.addWayToPoint);
    $('#lineDragRouteClose').on('click', fenceOperation.lineDragRouteClose);
    $('#lineDragRouteSave').on('click', fenceOperation.lineDragRouteSave);
    //围栏所属企业
    $('#markerFenceEnterprise, #markerFenceEnterprise-select, #lineFenceEnterprise, #lineFenceEnterprise-select, #rectangleFenceEnterprise, #rectangleFenceEnterprise-select, #circleFenceEnterprise, #circleFenceEnterprise-select, #polygonFenceEnterprise, #polygonFenceEnterprise-select, #areaFenceEnterprise, #areaFenceEnterprise-select, #dragRouteFenceEnterprise, #dragRouteFenceEnterprise-select').on('click', treeMonitoring.enterpriseShow);
    //显示设置
    $("#smoothMove,#logoDisplay,#icoUp").on("click", pageLayout.smoothMoveOrlogoDisplayClickFn);
    //地图设置
    $("#realTimeRC,#googleSatelliteMap,#googleGeograpyMap,#amapMap").on("change", pageLayout.mapDropdownSettingClickFn);
    amapOperation.showGoogleMapLayers('googleSatelliteMap');
    $("#fenceToolBtn").on("click", treeMonitoring.fenceToolClickSHFn);
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
        }
        else {
            $(this).siblings(".error").hide();
        }
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

    //获取图标方向
    var icoUrl = '/clbs/m/personalized/ico/getIcodirection';
    json_ajax("POST", icoUrl, "json", false, null, function (data) {
        var icoMsg = data.msg;
        if (icoMsg == 'true') {
            icoUpFlag = true;
            $("#icoUp").attr("checked", true);
            $("#icoUpLab").addClass("preBlue");
        }
        else {
            icoUpFlag = false;
            $("#icoUp").attr("checked", false);
            $("#icoUpLab").removeClass("preBlue");
        }
    });
});
