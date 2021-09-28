var travelLineList, AdministrativeRegionsList, fenceIdList,
    administrativeAreaFence = [], district, googleMapLayer, buildings, satellLayer, realTimeTraffic, map, logoWidth,
    btnIconWidth, windowWidth,
    newwidth, els, oldMapHeight, myTabHeight, wHeight, tableHeight, mapHeight, newMapHeight, winHeight, headerHeight,
    dbclickCheckedId, oldDbclickCheckedId,
    onClickVId, oldOnClickVId, zTree, clickStateChar, logTime, operationLogLength, licensePlateInformation,
    groupIconSkin, markerListT = [], markerRealTimeT,
    zoom = 18, requestStrS, cheakNodec = [], realTimeSet = [], alarmSet = [], neverOline = [], lineVid = [],
    zTreeIdJson = {}, cheakdiyuealls = [], lineAr = [],
    lineAs = [], lineAa = [], lineAm = [], lineOs = [], changeMiss = [], diyueall = [], params = [], lineV = [],
    lineHb = [], cluster, fixedPoint = null, fixedPointPosition = null,
    flog = true, mapVehicleTimeW, mapVehicleTimeQ, markerMap, mapflog, mapVehicleNum, infoWindow, paths = null,
    uptFlag = true, flagState = true,
    videoHeight, addaskQuestionsIndex = 2, dbClickHeighlight = false, checkedVehicles = [], runVidArray = [],
    stopVidArray = [], msStartTime, msEndTime,
    videoTimeIndex, voiceTimeIndex, charFlag = true, fanceID = "", newCount = 1, mouseTool, mouseToolEdit,
    clickRectangleFlag = false, isAddFlag = false, isAreaSearchFlag = false, isDistanceCount = false, fenceIDMap,
    PolyEditorMap,
    sectionPointMarkerMap, fenceSectionPointMap, travelLineMap, fenceCheckLength = 0, amendCircle, amendPolygon,
    amendLine, polyFence, changeArray, trid = [], parametersID, brand, clickFenceCount = 0,
    clickLogCount = 0, fenceIdArray = [], fenceOpenArray = [], save, moveMarkerBackData, moveMarkerFenceId,
    monitoringObjMapHeight, carNameMarkerContentMap, carNameMarkerMap, carNameContentLUMap,
    lineSpotMap, isEdit = true, sectionMarkerPointArray, stateName = [],obdName = [], stateIndex = 1,obdIndex = 1, alarmName = [], alarmIndex = 1,
    activeIndex = 1, queryFenceId = [], crrentSubV = [], crrentSubName = [],
    suFlag = true, administrationMap, lineRoute, contextMenu, dragPointMarkerMap, isAddDragRoute = false,
    misstype = false, misstypes = false, alarmString, saveFenceName, saveFenceType, alarmSub = 0, cancelList = [],
    hasBegun = [],
    isDragRouteFlag = false, flagSwitching = true, isCarNameShow = true, notExpandNodeInit, vinfoWindwosClickVid,
    $myTab = $("#myTab"), $MapContainer = $("#MapContainer"), $panDefLeft = $("#panDefLeft"),
    $contentLeft = $("#content-left"), $contentRight = $("#content-right"), $sidebar = $(".sidebar"),
    $mainContentWrapper = $(".main-content-wrapper"), $thetree = $("#thetree"),
    $realTimeRC = $("#realTimeRC"), $goShow = $("#goShow"), $chooseRun = $("#chooseRun"), $chooseNot = $("#chooseNot"),
    $chooseAlam = $("#chooseAlam"), $chooseStop = $("#chooseStop"),
    $chooseOverSeep = $("#chooseOverSeep"), $online = $("#online"), $chooseMiss = $("#chooseMiss"),
    $scrollBar = $("#scrollBar"), $mapPaddCon = $(".mapPaddCon"), $realTimeVideoReal = $(".realTimeVideoReal"),
    $realTimeStateTableList = $("#realTimeStateTable"),$obdTableList = $("#obdInfoTable"), $alarmTable = $("#alarmTable"), $logging = $("#logging"),
    $showAlarmWinMark = $("#showAlarmWinMark"), $alarmFlashesSpan = $(".alarmFlashes span"),
    $alarmSoundSpan = $(".alarmSound span"), $alarmMsgBox = $("#alarmMsgBox"), $alarmSoundFont = $(".alarmSound font"),
    $alarmFlashesFont = $(".alarmFlashes font"), $alarmMsgAutoOff = $("#alarmMsgAutoOff"),
    rMenu = $("#rMenu"), alarmNum = 0, carAddress, msgSNAck, setting, ztreeStyleDbclick,
    $tableCarAll = $("#table-car-all"), $tableCarOnline = $("#table-car-online"),
    $tableCarOffline = $("#table-car-offline"),
    $tableCarRun = $("#table-car-run"), $tableCarStop = $("#table-car-stop"),
    $tableCarOnlinePercent = $("#table-car-online-percent"), longDeviceType, tapingTime, loadInitNowDate = new Date(),
    loadInitTime,drivingState,
    checkFlag = false, fenceZTreeIdJson = {}, fenceSize, bindFenceSetChar, fenceInputChange, scorllDefaultTreeTop,
    stompClientOriginal = null, stompClientSocket = null, hostUrl, DblclickName, objAddressIsTrue = [];
// wjk 实时视频时间定时器
var computingTimeInt;
var computingTimeCallInt;
// var VideoOrPhoneCall = 0; // 0 通话视频无，1视频，2通话，3都有

var markerViewingArea;
var markerOutside;
var markerAllUpdateData;
var isCluster = false; // 是否集合
var markerFocus; // 聚焦跟踪id
var isAreaSearch = false; // 是否区域查询
var callTheRollId; // 点名车辆ID
var markerClickLngLat = null; // 点击监控对象图标后，获取经纬度
var curDbSubscribeMOnitor;//当前双击的监控对象id

//图标向上标记
var icoUpFlag;
var pageLayout = {
        // 页面布局
        init: function () {
            if (document.location.protocol === 'http:') {
                var url = "/clbs/v/monitoring/getHost";
                ajax_submit("POST", url, "json", true, {}, true, function (data) {
                    hostUrl = "http://" + data.obj.host + '/webSocket';
                });
            } else {
                hostUrl = '/proxy/webSocket';
            }
            winHeight = $(window).height();//可视区域高度
            headerHeight = $("#header").height();//头部高度
            // var tabHeight = $myTab.height();//信息列表table选项卡高度
            var tabContHeight = $("#myTabContent").height();//table表头高度
            var fenceTreeHeight = winHeight - 175;//围栏树高度
            $("#fenceZtree").css('height', fenceTreeHeight + "px");//电子围栏树高度
            //地图高度
            newMapHeight = winHeight - headerHeight;
            $MapContainer.css({
                "height": newMapHeight + 'px'
            });
            //车辆树高度
            var newContLeftH = winHeight - headerHeight;
            //sidebar高度
            $(".sidebar").css('height', newContLeftH + 'px');
            //计算顶部logo相关padding
            logoWidth = $("#header .brand").width();
            btnIconWidth = $("#header .toggle-navigation").width();
            windowWidth = $(window).width();
            newwidth = (logoWidth + btnIconWidth + 46) / windowWidth * 100;
            //左右自适应宽度
            $contentLeft.css({
                "width": newwidth + "%"
            });
            $contentRight.css({
                "width": 100 - newwidth + "%"
            });
            //加载时隐藏left同时计算宽度
            $sidebar.attr("class", "sidebar sidebar-toggle");
//        $mainContentWrapper.attr("class", "main-content-wrapper main-content-toggle-left");
            //操作树高度自适应
            var newTreeH = winHeight - headerHeight - 203;
            $thetree.css({
                "height": newTreeH + "px"
            });
            //视频区域自适应
            var mainContentHeight = $contentLeft.height();
            var adjustHeight = $(".adjust-area").height();
            videoHeight = (mainContentHeight - adjustHeight - 65) / 2;
            $(".videoArea").css("height", videoHeight + "px");
            //地图拖动改变大小
            oldMapHeight = $MapContainer.height();
            myTabHeight = $myTab.height();
            wHeight = $(window).height();
            // 页面区域定位
            $(".amap-logo").attr("href", "javascript:void(0)").attr("target", "");
            // 监听浏览器窗口大小变化
            var sWidth = $(window).width();
            if (sWidth < 1200) {
                $("body").css("overflow", "auto");
                $("#content-left,#panDefLeft").css("height", "auto");
                $panDefLeft.css("margin-bottom", "0px");
                if (sWidth <= 414) {
                    $sidebar.removeClass("sidebar-toggle");
                    $mainContentWrapper.removeClass("main-content-toggle-left");
                }
            } else {
                $("body").css("overflow", "hidden");
            }
            ;
            window.onresize = function () {
                winHeight = $(window).height();//可视区域高度
                headerHeight = $("#header").height();//头部高度
                // var tabHeight = $myTab.height();//信息列表table选项卡高度
                var tabContHeight = $("#myTabContent").height();//table表头高度
                var fenceTreeHeight = winHeight - 175;//围栏树高度
                $("#fenceZtree").css('height', fenceTreeHeight + "px");//电子围栏树高度
                //地图高度
                newMapHeight = winHeight - headerHeight ;
                $MapContainer.css({
                    "height": newMapHeight + 'px'
                });

                //右边视频模块保持一样的高度
               /* $('#realTimeVideoReal').css({
                    "height": newMapHeight + 'px'
                });*/

                //车辆树高度
                var newContLeftH = winHeight - headerHeight;
                //sidebar高度
                $(".sidebar").css('height', newContLeftH + 'px');
                //计算顶部logo相关padding
                logoWidth = $("#header .brand").width();
                btnIconWidth = $("#header .toggle-navigation").width();
                windowWidth = $(window).width();
                newwidth = (logoWidth + btnIconWidth + 46) / windowWidth * 100;
                //左右自适应宽度
                $contentLeft.css({
                    "width": newwidth + "%"
                });
                $contentRight.css({
                    "width": 100 - newwidth + "%"
                });
                //操作树高度自适应
                var newTreeH = winHeight - headerHeight - 203;
                $thetree.css({
                    "height": newTreeH + "px"
                });
                //视频区域自适应
                var mainContentHeight = $contentLeft.height();
                var adjustHeight = $(".adjust-area").height();
                videoHeight = (mainContentHeight - adjustHeight - 65) / 2;
                $(".videoArea").css("height", videoHeight + "px");
            }
            pageLayout.showOperatingAndRepairNum();
        },
        // 数组原型链拓展方法
        arrayExpand: function () {
            Array.prototype.isHas = function (a) {
                if (this.length === 0) {
                    return false
                }
                ;
                for (var i = 0, len = this.length; i < len; i++) {
                    if (this[i] === a) {
                        return true
                    }
                }
            };
            // 数组功能扩展
            Array.prototype.each = function (fn) {
                fn = fn || Function.K;
                var a = [];
                var args = Array.prototype.slice.call(arguments, 1);
                for (var i = 0, len = this.length; i < len; i++) {
                    var res = fn.apply(this, [this[i], i].concat(args));
                    if (res != null) a.push(res);
                }
                return a;
            };
            // 数组是否包含指定元素
            Array.prototype.contains = function (suArr) {
                for (var i = 0, len = this.length; i < len; i++) {
                    if (this[i] == suArr) {
                        return true;
                    }
                }
                return false;
            }
            // 两个数组的交集
            Array.intersect = function (a, b) {
                return a.each(function (o) {
                    return b.contains(o) ? o : null
                });
            };
            // 两个数组的差集
            Array.minus = function (a, b) {
                return a.each(function (o) {
                    return b.contains(o) ? null : o
                });
            };
            Array.subtract = function (a, b) {
                return a.filter(function (o) {
                    return b.indexOf(o) === -1
                });
            };
            // 删除数组指定下标或指定对象
            Array.prototype.remove = function (obj) {
                for (var i = 0; i < this.length; i++) {
                    var temp = this[i];
                    if (!isNaN(obj) && obj.length < 4) {
                        temp = i;
                    }
                    if (temp == obj) {
                        for (var j = i; j < this.length; j++) {
                            this[j] = this[j + 1];
                        }
                        this.length = this.length - 1;
                    }
                }
            };
            Array.prototype.removeObj = function (obj) {
                for (var i = 0; i < this.length; i++) {
                    var temp = this[i];
                    if (temp == obj) {
                        for (var j = i; j < this.length; j++) {
                            this[j] = this[j + 1];
                        }
                        this.length = this.length - 1;
                    }
                }
            };
            // 去重
            Array.prototype.unique2 = function () {
                var res = [this[0]];
                for (var i = 1, len = this.length; i < len; i++) {
                    var repeat = false;
                    for (var j = 0, jlen = res.length; j < jlen; j++) {
                        if (this[i].id == res[j].id) {
                            repeat = true;
                            break;
                        }
                    }
                    if (!repeat) {
                        res.push(this[i]);
                    }
                }
                return res;
            };
            Array.prototype.unique3 = function () {
                var res = [];
                var json = {};
                for (var i = 0, len = this.length; i < len; i++) {
                    if (!json[this[i]]) {
                        res.push(this[i]);
                        json[this[i]] = 1;
                    }
                }
                ;
                return res;
            };
        },
        // 创建map集合
        createMap: function () {
            mapVehicleTimeW = new pageLayout.mapVehicle();
            mapVehicleTimeQ = new pageLayout.mapVehicle();
            fenceIDMap = new pageLayout.mapVehicle();
            PolyEditorMap = new pageLayout.mapVehicle();
            fenceSectionPointMap = new pageLayout.mapVehicle();
            markerMap = new pageLayout.mapVehicle();
            mapflog = new pageLayout.mapVehicle();
            mapVehicleNum = new pageLayout.mapVehicle();
            sectionPointMarkerMap = new pageLayout.mapVehicle();
            carNameMarkerMap = new pageLayout.mapVehicle();
            carNameMarkerContentMap = new pageLayout.mapVehicle();
            carNameContentLUMap = new pageLayout.mapVehicle();
            lineSpotMap = new pageLayout.mapVehicle();
            sectionMarkerPointArray = new pageLayout.mapVehicle();
            travelLineMap = new pageLayout.mapVehicle();
            administrationMap = new pageLayout.mapVehicle();
            dragPointMarkerMap = new pageLayout.mapVehicle();
            //创建地图围栏相关集合
            fenceIdList = new pageLayout.mapVehicle();
            AdministrativeRegionsList = new pageLayout.mapVehicle();
            travelLineList = new pageLayout.mapVehicle();
            markerViewingArea = new pageLayout.mapVehicle();
            markerOutside = new pageLayout.mapVehicle();
            markerAllUpdateData = new pageLayout.mapVehicle();
            drivingState = new pageLayout.mapVehicle();
        },
        // 应答
        responseSocket: function () {
            /*setTimeout(function() {
                webSocket.subscribe(headers, '/user/' + $("#userName").text() + '/check', pageLayout.updateTable, "/app/vehicle/inspect", null);
            }, 1000);*/
            pageLayout.isGetSocketLayout();
        },
        isGetSocketLayout: function () {
            setTimeout(function () {
                if (webSocket.conFlag) {
                    webSocket.subscribe(headers, '/user/' + $("#userName").text() + '/check', pageLayout.updateTable, "/app/vehicle/inspect", null);
                } else {
                    pageLayout.isGetSocketLayout();
                }
            }, 2000);
        },
        // 应答socket回掉函数
        updateTable: function (msg) {
            if (msg != null) {
                var json = $.parseJSON(msg.body);
                var msgData = json.data;
                if (msgData != undefined) {
                    var msgId = msgData.msgHead.msgID;
                    // if (msgId == 0x9300) {
                    //     var dataType = msgData.msgBody.dataType;
                    //     $("#msgDataType").val(dataType);
                    //     $("#infoId").val(msgData.msgBody.data.infoId);
                    //     $("#objectType").val(msgData.msgBody.data.objectType);
                    //     $("#objectId").val(msgData.msgBody.data.objectId);
                    //     $("#question").text(msgData.msgBody.data.infoContent);
                    //     if (dataType == 0x9301) {
                    //         $("#answer").val("");
                    //         $("#msgTitle").text("平台查岗");
                    //         $("#goTraceResponse").modal('show');
                    //     }
                    //     if (dataType == 0x9302) {
                    //         $("#answer").val("");
                    //         $("#msgTitle").text("下发平台间报文");
                    //         $("#goTraceResponse").modal('show');
                    //     }
                    // }
                }
            }
        },
        // 应答确定
        platformMsgAck: function () {
            var answer = $("#answer").val();
            if (answer == "") {
                showErrorMsg("应答不能为空", "answer");
                return;
            }
            $("#goTraceResponse").modal('hide');
            var msgDataType = $("#msgDataType").val();
            var infoId = $("#infoId").val();
            var objectType = $("#objectType").val();
            var objectId = $("#objectId").val();
            var url = "/clbs/m/connectionparamsset/platformMsgAck";
            json_ajax("POST", url, "json", false, {
                "infoId": infoId,
                "answer": answer,
                "msgDataType": msgDataType,
                "objectType": objectType,
                "objectId": objectId
            });
        },
        //右边菜单显示隐藏切换
        toggleLeft: function () {
            if ($sidebar.hasClass("sidebar-toggle")) {
                if ($contentLeft.is(":hidden")) {
                    $contentRight.css("width", "100%");
                } else {
                    $contentLeft.css("width", newwidth + "%");
                    $contentRight.css("width", (100 - newwidth) + "%");
                }
            } else {
                if ($contentLeft.is(":hidden")) {
                    $contentRight.css("width", "100%");
                } else {
                    $contentRight.css("width", (100 - newwidth - 5) + "%");
                    $contentLeft.css("width", (newwidth + 5) + "%");
                }
            }
        },
        //左侧操作树点击隐藏
        goHidden: function () {
            $contentLeft.hide();
            $contentRight.attr("class", "col-md-12 content-right");
            $contentRight.css("width", "100%");
            $('#videoCont').css("width", "100%");
            $goShow.show();
        },
        //左侧操作树点击显示
        goShow: function () {
            $('#videoCont').css("width", "100%");

            $contentLeft.show();
            $contentRight.attr("class", "col-md-9 content-right");
            if ($sidebar.hasClass("sidebar-toggle")) {
                $contentRight.css("width", (100 - newwidth) + "%");
                $contentLeft.css("width", newwidth + "%");
            } else {
                $contentRight.css("width", "75%");
                $contentLeft.css("width", "25%");
            }
            $goShow.hide();
        },
        //鼠标按住拖动事件
        mouseMove: function (e) {
            if (els - e.clientY > 0) {
                var y = els - e.clientY;
                var newHeight = mapHeight - y;
                if (newHeight <= 0) {
                    newHeight = 0;
                }
                $MapContainer.css("height", newHeight + "px");

                //右边视频模块保持一样的高度
                // $('#realTimeVideoReal').css("height", newHeight + "px");

                if (newHeight == 0) {
                    return false;
                }
                $(pageLayout.getCurrentActiveTableName()).css("height", (tableHeight + y) + "px");
            } else {
                var dy = e.clientY - els;
                var newoffsetTop = $myTab.offset().top;
                var scrollBodyHeight = $("#realTimeState .dataTables_scrollBody").height();
                if (scrollBodyHeight == 0) {
                    return false;
                }
                if (newoffsetTop <= (wHeight - myTabHeight)) {
                    var newHeight = mapHeight + dy;
                    $MapContainer.css("height", newHeight + "px");

                    //右边视频模块保持一样的高度
                    // $('#realTimeVideoReal').css("height", newHeight + "px");

                    $(pageLayout.getCurrentActiveTableName()).css("height", (tableHeight - dy) + "px");
                }
            }
            e.stopPropagation();
        },
        // 鼠标移除事件
        mouseUp: function () {
            $(document).unbind("mousemove", pageLayout.mouseMove).unbind("mouseup", pageLayout.mouseUp);
        },
        // 封装map集合
        mapVehicle: function () {
            this.elements = new Array();
            //获取MAP元素个数
            this.size = function () {
                return this.elements.length;
            };
            //判断MAP是否为空
            this.isEmpty = function () {
                return (this.elements.length < 1);
            };
            //删除MAP所有元素
            this.clear = function () {
                this.elements = new Array();
            };
            //向MAP中增加元素（key, value)
            this.put = function (_key, _value) {
                this.elements.push({
                    key: _key,
                    value: _value
                });
            };
            //删除指定KEY的元素，成功返回True，失败返回False
            this.remove = function (_key) {
                var bln = false;
                try {
                    for (var i = 0, len = this.elements.length; i < len; i++) {
                        if (this.elements[i].key == _key) {
                            this.elements.splice(i, 1);
                            return true;
                        }
                    }
                } catch (e) {
                    bln = false;
                }
                return bln;
            };
            //获取指定KEY的元素值VALUE，失败返回NULL
            this.get = function (_key) {
                try {
                    for (var i = 0, len = this.elements.length; i < len; i++) {
                        if (this.elements[i].key == _key) {
                            return this.elements[i].value;
                        }
                    }
                } catch (e) {
                    return null;
                }
            };
            //获取指定索引的元素（使用element.key，element.value获取KEY和VALUE），失败返回NULL
            this.element = function (_index) {
                if (_index < 0 || _index >= this.elements.length) {
                    return null;
                }
                return this.elements[_index];
            };
            //判断MAP中是否含有指定KEY的元素
            this.containsKey = function (_key) {
                var bln = false;
                try {
                    for (var i = 0, len = this.elements.length; i < len; i++) {
                        if (this.elements[i].key == _key) {
                            bln = true;
                        }
                    }
                } catch (e) {
                    bln = false;
                }
                return bln;
            };
            //判断MAP中是否含有指定VALUE的元素
            this.containsValue = function (_value) {
                var bln = false;
                try {
                    for (var i = 0, len = this.elements.length; i < len; i++) {
                        if (this.elements[i].value == _value) {
                            bln = true;
                        }
                    }
                } catch (e) {
                    bln = false;
                }
                return bln;
            };
            //获取MAP中所有VALUE的数组（ARRAY）
            this.values = function () {
                var arr = new Array();
                for (var i = 0, len = this.elements.length; i < len; i++) {
                    arr.push(this.elements[i].value);
                }
                return arr;
            };
            //获取MAP中所有KEY的数组（ARRAY）
            this.keys = function () {
                var arr = new Array();
                for (var i = 0, len = this.elements.length; i < len; i++) {
                    arr.push(this.elements[i].key);
                }
                return arr;
            };
        },
        //拖拽DIV
        dragDiv: function (e) {
            //报警记录及日志信息不能拖拽 隐藏不能拖拽
            if ($("#scalingBtn").hasClass("fa fa-chevron-down")) {
                // if (stateName.length > 5) {
                    tableHeight = $(pageLayout.getCurrentActiveTableName(activeIndex)).height();
                    mapHeight = $MapContainer.height();
                    els = e.clientY;
                    $(document).bind("mousemove", pageLayout.mouseMove).bind("mouseup", pageLayout.mouseUp);
                    e.stopPropagation();
                // }
            }
        },
    /**
     * 获取当前可见表格的名称
         */
        getCurrentActiveTableName:function(type){
            return '#myTabContent';
            // if (type === undefined){
            //     return '#realTimeStateTable-div,#alarmTable-div,#obdTable-div,#logTable-div,#securityTable-div';
            // }
            // var id;
            // if (type === 1) {//状态信息车
            //     id = '#realTimeStateTable-div';
            // } else if (type === 3) { //报警信息车
            //     id = '#alarmTable-div';
            // }
            // if (type === 2) {//OBD数据
            //     id = '#obdTable-div';
            // }
            // if (type === 4) {//日志
            //     id = '#logTable-div';
            // }
            // if (type === 5) {//主动安全
            //     id = '#securityTable-div';
            // }
            // return id;
        },
        //实时视频
        videoRealTimeShow: function (callback) {
            var $this = $('#btn-videoRealTime-show').children("i");
            if (!$this.hasClass("active")) {

                $realTimeVideoReal.removeClass("realTimeVideoMove");
                $mapPaddCon.removeClass("mapAreaTransform");
                m_videoFlag = 0; //标识视频窗口关闭

                realtimeMonitoringVideoSeparate.closeTerminalVideo()

            } else {

                // wjk
                $(this).addClass("map-active");
                $realTimeVideoReal.addClass("realTimeVideoMove");
                $mapPaddCon.addClass("mapAreaTransform");

                m_videoFlag = 1; //标识视频窗口打开

                if (subscribeVehicleInfo){
                    if (m_videoFlag == 1) {
                        realtimeMonitoringVideoSeparate.closeTerminalVideo()
                        realtimeMonitoringVideoSeparate.initVideoRealTimeShow(subscribeVehicleInfo);
                    }
                }

            }
        },
        // wjk 对讲，实时通话
        phoneCallRealTimeshow: function () {
            //实时通话 判断IE模式
            if (navigator.appName == "Microsoft Internet Explorer") {
                if (parseInt(navigator.appVersion.split(";")[1].replace(/[ ]/g, "").replace("MSIE", "")) < 10) {
                    layer.msg("亲！您的IE浏览器版本过低，请下载IE10及以上版本！");
                } else {
                    var $this = $('#phoneCall').children("i");
                    if (!$this.hasClass("active")) {
                        // wjk 视频时不关闭画面
                        if (!$('#btn-videoRealTime-show').find('i').hasClass('active')) {
                            $realTimeVideoReal.removeClass("realTimeVideoShow");
                            $mapPaddCon.removeClass("mapAreaTransform");
                            m_videoFlag = 0; //标识视频窗口关闭
                        }

                        clearInterval(computingTimeCallInt)
                        realTimeVideo.closeAudio();
                    } else {

                        // wjk
                        $(this).addClass("map-active");
                        $realTimeVideoReal.addClass("realTimeVideoShow");
                        $mapPaddCon.addClass("mapAreaTransform");
                        m_videoFlag = 1; //标识视频窗口打开
                        realTimeVideo.windowSet();
                        //传入限制单次实时视频回调
                        setTimeout("realTimeVideo.beventLiveIpTalk(pageLayout.computingTimeCallIntFun)", 5);
                    }
                }
            } else {
                $("#phoneCall i").removeClass("active");
                $("#phoneCall span").removeAttr("style");
                layer.msg("亲！实时通话暂时仅支持IE浏览器哟！请使用IE浏览器！");
            }
        },
        // 关闭视频区域
        closeVideo: function () {
            // if ($('#btn-videoRealTime-show i').hasClass('active')) {
            //     $realTimeVideoReal.removeClass("realTimeVideoShow");
            //     $mapPaddCon.removeClass("mapAreaTransform");
            //     $('#btn-videoRealTime-show i').removeClass('active');
            //     $('#btn-videoRealTime-show span').css('color', '#5c5e62');
            // }
        },
        //点击显示报警
        showAlarmWindow: function () {
            $showAlarmWinMark.show();
            $("#showAlarmWin").hide();
        },
        //点击切换状态栏
        showAlarmWinMarkRight: function () {
            $("#TabFenceBox a").click();
            $("#myTab li").removeAttr("class");
            $("#realTtimeAlarm").attr("class", "active");
            $("#operationLogTable").attr("class", "tab-pane fade");
            $("#realTimeState").attr("class", "tab-pane fade");
            $('#activeSafety').attr("class", "tab-pane fade");
            $("#realTimeCall").attr("class", "tab-pane fade active in").siblings('.tab-pane').attr("class", "tab-pane fade");
            $(this).css("background-position", "0px -67px");
            setTimeout(function () {
                $showAlarmWinMark.css("background-position", "0px 0px");
            }, 100)
            $("#realTtimeAlarm").click();
            dataTableOperation.realTtimeAlarmClick();
            dataTableOperation.carStateAdapt(3);
        },
        alarmToolMinimize: function () {
            $("#context-menu").removeAttr("class");
            $("#showAlarmWin").show();
            $showAlarmWinMark.hide();
        },
        //开启关闭声音
        alarmOffSound: function () {
            if (navigator.userAgent.indexOf('MSIE') >= 0) {
                //IE浏览器
                if ($alarmSoundSpan.hasClass("soundOpen")) {
                    $alarmSoundSpan.addClass("soundOpen-off");
                    $alarmSoundSpan.removeClass("soundOpen");
                    $alarmSoundFont.css("color", "#a8a8a8");
                    // $alarmMsgBox.html('<embed id="IEalarmMsg" src=""/>');
                } else {
                    $alarmSoundSpan.removeClass("soundOpen-off");
                    $alarmSoundSpan.addClass("soundOpen");
                    $alarmSoundFont.css("color", "#fff");
                    // $alarmMsgBox.html('<embed id="IEalarmMsg" src="../../file/music/alarm.wav" autostart="true"/>');
                    document.querySelector('#IEalarmMsg').play()
                }
            } else {
                //其他浏览器
                if ($alarmSoundSpan.hasClass("soundOpen")) {
                    $alarmSoundSpan.addClass("soundOpen-off");
                    $alarmSoundSpan.removeClass("soundOpen");
                    $alarmSoundFont.css("color", "#a8a8a8");
                    if (alarmNum > 0) {
                        $("#alarmMsgAutoOff")[0].pause();
                    }
                    $alarmMsgAutoOff.removeAttr("autoplay");
                } else {
                    $alarmSoundSpan.removeClass("soundOpen-off");
                    $alarmSoundSpan.addClass("soundOpen");
                    $alarmSoundFont.css("color", "#fff");
                    if (alarmNum > 0) {
                        $("#alarmMsgAutoOff")[0].play();
                    }
                }
            }
        },
        //开启关闭闪烁
        alarmOffFlashes: function () {
            if ($alarmFlashesSpan.hasClass("flashesOpen")) {
                $alarmFlashesSpan.addClass("flashesOpen-off");
                $alarmFlashesSpan.removeClass("flashesOpen");
                $alarmFlashesFont.css("color", "#a8a8a8");
                $showAlarmWinMark.css("background-position", "0px 0px");
            } else {
                $alarmFlashesSpan.removeClass("flashesOpen-off");
                $alarmFlashesSpan.addClass("flashesOpen");
                $alarmFlashesFont.css("color", "#fff");
                if (alarmNum > 0) {
                    $showAlarmWinMark.css("background-position", "0px -134px");
                    setTimeout(function () {
                        $showAlarmWinMark.css("background-position", "0px 0px");
                    }, 1500)
                } else {
                    $showAlarmWinMark.css("background-position", "0px 0px");
                }
            }
        },
        //显示报警设置详情
        showAlarmInfoSettings: function () {
            pageLayout.closeVideo();
            $("#alarmSettingInfo").modal("show");
            $("#context-menu").removeClass("open");
        },
        //工具图标按钮
        toolClick: function () {
            // var $toolOperateClick = $("#toolOperateClick");
            // if($toolOperateClick.css("margin-right") == "-702px"){
            //     $toolOperateClick.animate({marginRight:"7px"});
            // }else{
            //     $("#disSetMenu,#mapDropSettingMenu").hide();
            //     $toolOperateClick.animate({marginRight:"-702px"});
            //     $("#toolOperateClick i").removeClass('active');
            //     $("#toolOperateClick span").css('color','#5c5e62');
            //     mouseTool.close(true);
            // };

            // wjk
            var $toolOperateClick = $("#toolOperateClick");
            if ($toolOperateClick.css("margin-right") == "-776px") {
                $toolOperateClick.animate({marginRight: "7px"});
            } else {
                $("#disSetMenu,#mapDropSettingMenu").hide();
                $toolOperateClick.animate({marginRight: "-776px"});
                $("#toolOperateClick i").removeClass('active');
                $("#toolOperateClick span").css('color', '#5c5e62');
                mouseTool.close(true);
            }
            ;
        },
        //显示设置
        smoothMoveOrlogoDisplayClickFn: function () {
            var id = $(this).attr("id");
            //平滑移动
            if (id == "smoothMove") {
                if ($("#smoothMove").attr("checked")) {
                    flagSwitching = false;
                    $("#smoothMove").attr("checked", false);
                    $("#smoothMoveLab").removeClass("preBlue");
                } else {
                    flagSwitching = true;
                    $("#smoothMove").attr("checked", true);
                    $("#smoothMoveLab").addClass("preBlue");
                }
            }
            //标识显示
            else if (id == "logoDisplay") {
                if ($("#logoDisplay").attr("checked")) {
                    isCarNameShow = false;
                    $("#logoDisplay").attr("checked", false);
                    $("#logoDisplayLab").removeClass("preBlue");
                } else {
                    isCarNameShow = true;
                    $("#logoDisplay").attr("checked", true);
                    $("#logoDisplayLab").addClass("preBlue");
                }
                amapOperation.carNameState(isCarNameShow);
            }
            //图标向上
            else if (id == "icoUp") {
                if ($("#icoUp").attr("checked")) {
                    icoUpFlag = false;
                    $("#icoUp").attr("checked", false);
                    $("#icoUpLab").removeClass("preBlue");
                } else {
                    icoUpFlag = true;
                    $("#icoUp").attr("checked", true);
                    $("#icoUpLab").addClass("preBlue");
                    var values = carNameMarkerMap.values();
                    console.log(values);
                    for (var i = 0; i < values.length; i++) {
                        values[i].setAngle(0);
                    }
                }
            }
        },
        //地图设置
        mapDropdownSettingClickFn: function () {
            var id = $(this).attr("id");
            //路况开关
            if (id == "realTimeRC") {
                amapOperation.realTimeRC();
            }else{
                amapOperation.showGoogleMapLayers(id);
            }
        },
        //获取当前服务器系统时间
        getNowFormatDate: function () {
            var url = "/clbs/v/monitoring/getTime"
            json_ajax("POST", url, "json", false, null, function (data) {
                logTime = data;
            });
        },
        // wjk,视频时间限制回调函数
        computingTimeIntFun: function () {
            clearInterval(computingTimeInt);
            if (m_isVideo !== 0 && m_videoFlag !== 0) {
                var index = 0;
                computingTimeInt = setInterval(function () {
                    index++;
                    if (index > 30) {
                        clearInterval(computingTimeInt);
                        if (!$('#phoneCall').find('i').hasClass('active')) {
                            $realTimeVideoReal.removeClass("realTimeVideoShow");
                            $mapPaddCon.removeClass("mapAreaTransform");
                        }
                        $("#btn-videoRealTime-show i").removeClass("active");
                        $("#btn-videoRealTime-show span").removeAttr("style");
                        m_videoFlag = 0; //标识视频窗口关闭
                        realTimeVideo.closeVideo(0);
                        layer.msg('单次视频时长已达到30s上限')
                    }
                }, 1000)
            }
        },
        //wjk 通话时间限制回调函数
        computingTimeCallIntFun: function () {
            clearInterval(computingTimeCallInt);
            if (m_videoFlag !== 0) {
                var index = 0;
                computingTimeCallInt = setInterval(function () {
                    index++;
                    if (index > 60) {
                        clearInterval(computingTimeCallInt);
                        if (!$('#btn-videoRealTime-show').find('i').hasClass('active')) {
                            $realTimeVideoReal.removeClass("realTimeVideoShow");
                            $mapPaddCon.removeClass("mapAreaTransform");
                            m_videoFlag = 0; //标识视频窗口关闭
                        }
                        $("#phoneCall i").removeClass("active");
                        $("#phoneCall span").removeAttr("style");
                        realTimeVideo.closeAudio();
                        layer.msg('单次实时通话时长已达到60s上限')
                    }
                }, 1000)
            }
        },
        //显示车辆运营数量和维修数量
        showOperatingAndRepairNum: function () {
            var url = "/clbs/m/basicinfo/monitoring/vehicle/getOperatingAndRepairNum";
            json_ajax("POST", url, "json", true, {}, pageLayout.operatingAndRepairNumCall);
        },
        operatingAndRepairNumCall: function (data) {
            if (data.success) {
                $("#table-car-operating-num").text(data.obj.operatingNum);
                $("#table-car-repair-num").text(data.obj.repairNum);
            }
        }
    }
;