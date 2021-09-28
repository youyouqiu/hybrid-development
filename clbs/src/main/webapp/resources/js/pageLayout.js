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
    lineSpotMap, isEdit = true, sectionMarkerPointArray, stateName = [], obdName = [], stateIndex = 1, obdIndex = 1,
    alarmName = [], alarmIndex = 1,
    activeIndex = 1, queryFenceId = [], crrentSubV = [], crrentSubName = [],
    suFlag = true, administrationMap, lineRoute, contextMenu, dragPointMarkerMap, isAddDragRoute = false,
    misstype = false, misstypes = false, alarmString, saveFenceName, saveFenceType, alarmSub = 0, cancelList = [],
    hasBegun = [],
    isDragRouteFlag = false, notExpandNodeInit, vinfoWindwosClickVid,
    $myTab = $("#myTab"), $MapContainer = $("#MapContainer"), $panDefLeft = $("#panDefLeft"),
    $contentLeft = $("#content-left"), $contentRight = $("#content-right"), $sidebar = $(".sidebar"),
    $mainContentWrapper = $(".main-content-wrapper"), $thetree = $("#thetree"),
    $realTimeRC = $(".trafficBtn"), $goShow = $("#goShow"), $chooseRun = $("#chooseRun"), $chooseNot = $("#chooseNot"),
    $chooseAlam = $("#chooseAlam"), $chooseStop = $("#chooseStop"),
    $chooseOverSeep = $("#chooseOverSeep"), $online = $("#online"), $chooseMiss = $("#chooseMiss"),
    $scrollBar = $("#scrollBar"), $mapPaddCon = $(".mapPaddCon"), $realTimeVideoReal = $(".realTimeVideoReal"),
    $realTimeStateTableList = $("#realTimeStateTable"), $obdTableList = $("#obdInfoTable"),
    $alarmTable = $("#alarmTable"), $logging = $("#logging"),
    $showAlarmWinMark = $("#showAlarmWinMark"), $alarmFlashesSpan = $(".alarmFlashes span"),
    $alarmSoundSpan = $(".alarmSound span"), $alarmMsgBox = $("#alarmMsgBox"), $alarmSoundFont = $(".alarmSound font"),
    $alarmFlashesFont = $(".alarmFlashes font"), $alarmMsgAutoOff = $("#alarmMsgAutoOff"),
    rMenu = $("#rMenu"), alarmNum = 0, carAddress, msgSNAck, setting, ztreeStyleDbclick,
    $tableCarAll = $("#table-car-all"), $tableCarOnline = $("#table-car-online"),
    $tableCarOffline = $("#table-car-offline"),
    $tableCarRun = $("#table-car-run"), $tableCarStop = $("#table-car-stop"),
    $tableCarOnlinePercent = $("#table-car-online-percent"), longDeviceType, tapingTime, loadInitNowDate = new Date(),
    loadInitTime, drivingState,
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
var centerMarkerId;// 居中显示marker id
var isAreaSearch = false; // 是否区域查询
var callTheRollId; // 点名车辆ID
var markerClickLngLat = null; // 点击监控对象图标后，获取经纬度
var curDbSubscribeMOnitor;//当前双击的监控对象id

var onlineVoiceSettimeOut = null;// 车辆上下线提醒定时器(声音)
var onlineSettimeOut = null;// 车辆上下线提醒定时器(闪烁)

var waybillAndPractitionersInfo;//存放电子运单与从业人员信息
var deviceTypeTxt = '';// 协议类型
var isCarStateAdapt = false;// 列表是否有数据
var isDragFlag = false;// 列表高度是否被拖拽
var fenceMapHeight = '';//地图初始高度

var activeSafetyHasRiskIds = [];// 主动安全列表已有事件ID集合

var flagSwitching = true, isCarNameShow = true, icoUpFlag = false;


// 初始选中平滑移动跳点时间
var username = $('#userName').text();
var jumpPointSetting = window.localStorage.getItem('jumpPointSetting');
if (jumpPointSetting) {
    jumpPointSetting = JSON.parse(jumpPointSetting);
    if (jumpPointSetting[username]) {
        $('.jumpSettingMenu span').removeClass('active');
        $('.jumpSettingMenu span[data-value=' + jumpPointSetting[username] + ']').addClass('active');
    }
}


var mapHeightq, tableHeightq; // table和map拖动时的高度
var timerq; // 延时器
var mapTypeSwitch = false;
var pageLayout = {
        // 页面布局
        init: function () {
            pageLayout.userSetting();
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
            var tabHeight = $myTab.height();//信息列表table选项卡高度
            var tabContHeight = $("#myTabContent").height();//table表头高度
            var fenceTreeHeight = winHeight - 193;//围栏树高度
            $("#fenceZtree").css('height', fenceTreeHeight + "px");//电子围栏树高度
            //地图高度
            newMapHeight = winHeight - headerHeight - tabHeight - 10;
            fenceMapHeight = newMapHeight;
            $MapContainer.css({
                "height": newMapHeight + 'px'
            });
            $('#realTimeVideoReal').css({
                "height": winHeight - 80 + 'px'
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
                var tabHeight = $myTab.height();//信息列表table选项卡高度
                var tabContHeight = $("#myTabContent").height();//table表头高度
                tabContHeight = tabContHeight <= 60 ? 0 : tabContHeight;
                var fenceTreeHeight = winHeight - 193;//围栏树高度
                $("#fenceZtree").css('height', fenceTreeHeight + "px");//电子围栏树高度
                //地图高度
                newMapHeight = winHeight - headerHeight - tabHeight - tabContHeight - 10;
                monitoringObjMapHeight = newMapHeight;
                fenceMapHeight = winHeight - headerHeight - $('#fenceBindTable').height() + 20;
                if ($('#TabCarBox').hasClass('active')) {// 显示的是电子围栏页签
                    newMapHeight = fenceMapHeight;
                }
                $MapContainer.css({
                    "height": newMapHeight + 'px'
                });
                $('#dimensionalMapContainer').css({
                    "height": newMapHeight + 'px'
                });
                //右边视频模块保持一样的高度
                $('#realTimeVideoReal').css({
                    "height": $(window).height() - 80 + 'px'
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
                if ($contentLeft.is(':visible')) {
                    $contentRight.css({
                        "width": 100 - newwidth + "%"
                    });
                }
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
        // 获取前端缓存中的用户设置
        userSetting: function () {
            var storeKey = $('#userName').text() + '_session';
            var storeValue = window.localStorage.getItem(storeKey);
            if (storeValue) {
                var data = JSON.parse(storeValue);
                flagSwitching = data.flagSwitching !== undefined ? data.flagSwitching : flagSwitching;
                isCarNameShow = data.isCarNameShow !== undefined ? data.isCarNameShow : isCarNameShow;
                // 地图显示设置
                if (data.flagSwitching === false) {
                    $('#smoothMove').attr('checked', false).prop('checked', false);
                    $('#smoothMoveLab').removeClass('preBlue');
                    $('.jumpSettingMenu').hide();
                } else {
                    $('#smoothMove').attr('checked', true).prop('checked', true);
                    $('#smoothMoveLab').addClass('preBlue');
                    $('.jumpSettingMenu').show();
                }
                if (data.isCarNameShow === false) {
                    $('#logoDisplayLab').removeClass('preBlue');
                    $('#logoDisplay').attr('checked', false).prop('checked', false);
                }
                if (data.icoUpFlag) {
                    icoUpFlag = true;
                    $('#icoUp').attr('checked', true).prop('checked', true);
                    $('#icoUpLab').addClass('preBlue');
                }

                // 报警声音及闪烁
                if (data.alarmOffSound === false) {
                    $alarmSoundSpan.addClass("soundOpen-off");
                    $alarmSoundSpan.removeClass("soundOpen");
                    $alarmSoundFont.css("color", "#a8a8a8");
                }
                if (data.alarmOffFlashes === false) {
                    $alarmFlashesSpan.addClass("flashesOpen-off");
                    $alarmFlashesSpan.removeClass("flashesOpen");
                    $alarmFlashesFont.css("color", "#a8a8a8");
                    $showAlarmWinMark.css("background-position", "0px 0px");
                }
            }
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

            // 创建电子运单与从业人员集合
            waybillAndPractitionersInfo = new pageLayout.mapVehicle();
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
                    webSocket.subscribe(headers, '/user/topic/check', pageLayout.updateTable, "/app/vehicle/inspect", null);
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
            // e.stopPropagation();
            // 添加一个虚线div样式
            var headHeight = parseInt($('#header').height(), 10);
            $('#dragDIV2').css({'top': (e.clientY - headHeight - 3) + 'px', border: '1px dashed black'});
        },
        // 鼠标移除事件
        mouseUp: function (e) {
            // 防止mouseup不生效
            // e.stopPropagation();
            // e.preventDefault();
            $("#fixDragDiv").css("display", "none")
            var y = els - e.clientY;
            var newHeight = mapHeight - y;
            var windowHeight = $(window).height();
            var tableHeight = windowHeight - e.clientY - 46;

            // 更新地图、表格的高度
            $MapContainer.css("height", newHeight + "px");
            $("#dimensionalMapContainer").css("height", newHeight + "px");
            // $('#realTimeVideoReal').css("height", newHeight + "px");
            $(pageLayout.getCurrentActiveTableName()).css("height", tableHeight + "px");

            isDragFlag = true;
            // 延时器：防止mouseup不生效
            if (timerq) {
                clearTimeout(timerq);
            }
            timerq = setTimeout(function () {
                $('body').off("mousemove", pageLayout.mouseMove).off("mouseup", pageLayout.mouseUp);
            }, 10);
            // 还原dragDiv样式
            $('#dragDIV').css({backgroundColor: '#ccc'});

            // 删除虚线div样式
            $('#dragDIV2').remove();

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
            // 获取head高度：用以更新虚线div位置
            var headHeight = parseInt($('#header').height(), 10);
            // 改变dragDiv颜色
            $('#dragDIV').css({backgroundColor: 'deepskyblue'});
            // 添加一个虚线div
            var dragDIV2Top = e.clientY - headHeight - 3;
            $('#dragDIV').after('<div id="dragDIV2" style="position: absolute;top:' + dragDIV2Top + 'px;z-index: 1999; width: 100%;height:4px; border: 1px dashed black"></div>');
            $("#fixDragDiv").css("display", "block")
            //报警记录及日志信息不能拖拽 隐藏不能拖拽
            // if ($("#scalingBtn").hasClass("fa fa-chevron-down")) {//去掉判断，解决点击箭头收起时，不能拖动的bug
            tableHeight = $(pageLayout.getCurrentActiveTableName(activeIndex)).height();
            mapHeight = $MapContainer.height();

            els = e.clientY;
            $('body').bind("mousemove", pageLayout.mouseMove).bind("mouseup", pageLayout.mouseUp);
            e.stopPropagation();
            e.preventDefault();
            // }
        },
        /**
         * 获取当前可见表格的名称
         */
        getCurrentActiveTableName: function (type) {
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
                if ($('#callSelect').hasClass('active')) {// 关闭对讲功能
                    realtimeMonitoringVideoSeparate.callOrder();
                }
                realtimeMonitoringVideoSeparate.closeTerminalVideo()

            } else {

                // wjk
                $(this).addClass("map-active");
                $realTimeVideoReal.addClass("realTimeVideoMove");
                $mapPaddCon.addClass("mapAreaTransform");

                m_videoFlag = 1; //标识视频窗口打开

                if (subscribeVehicleInfo) {
                    if (m_videoFlag == 1) {
                        realtimeMonitoringVideoSeparate.closeTerminalVideo();
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
            $("#callPolice").show();
            $("#ActiveSafetybtn").show();
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
        showAlarmWinMarkShield: function () {
            $("#TabFenceBox a").click();
            $("#myTab li").removeAttr("class");
            $("#activeSafetyTab").attr("class", "active");
            $("#operationLogTable").attr("class", "tab-pane fade");
            $("#realTimeState").attr("class", "tab-pane fade");
            $('#activeSafety').attr("class", "tab-pane fade");
            $("#activeSafety").attr("class", "tab-pane fade active in").siblings('.tab-pane').attr("class", "tab-pane fade");
            $(this).css("background-position", "0px -67px");
            setTimeout(function () {
                $showAlarmWinMark.css("background-position", "0px 0px");
            }, 100)
            $("#activeSafetyTab").click();
            dataTableOperation.realTtimeAlarmClick();
            dataTableOperation.carStateAdapt(5);
        },
        alarmToolMinimize: function () {
            $("#context-menu").removeAttr("class");
            $("#showAlarmWin").show();
            $showAlarmWinMark.hide();
            $("#callPolice").hide();
            $("#ActiveSafetybtn").hide();
        },
        //开启关闭声音
        alarmOffSound: function () {
            var openStatus = false;// 声音开启状态
            if (navigator.userAgent.indexOf('MSIE') >= 0) {
                //IE浏览器
                if ($alarmSoundSpan.hasClass("soundOpen")) {
                    $alarmSoundSpan.addClass("soundOpen-off");
                    $alarmSoundSpan.removeClass("soundOpen");
                    $alarmSoundFont.css("color", "#a8a8a8");
                    // $alarmMsgBox.html('<embed id="IEalarmMsg" src=""/>');
                } else {
                    openStatus = true;
                    $alarmSoundSpan.removeClass("soundOpen-off");
                    $alarmSoundSpan.addClass("soundOpen");
                    $alarmSoundFont.css("color", "#fff");
                    // $alarmMsgBox.html('<embed id="IEalarmMsg" src="../../file/music/alarm.wav" autostart="true"/>');
                    document.querySelector('#IEalarmMsg').play();
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
                    openStatus = true;
                    $alarmSoundSpan.removeClass("soundOpen-off");
                    $alarmSoundSpan.addClass("soundOpen");
                    $alarmSoundFont.css("color", "#fff");
                    if (alarmNum > 0) {
                        $("#alarmMsgAutoOff")[0].play();
                    }
                }
            }
            pageLayout.setUserStore({alarmOffSound: openStatus});
        },
        //开启关闭闪烁
        alarmOffFlashes: function () {
            var openStatus = false;// 闪烁开启状态
            if ($alarmFlashesSpan.hasClass("flashesOpen")) {
                $alarmFlashesSpan.addClass("flashesOpen-off");
                $alarmFlashesSpan.removeClass("flashesOpen");
                $alarmFlashesFont.css("color", "#a8a8a8");
                $showAlarmWinMark.css("background-position", "0px 0px");
            } else {
                openStatus = true;
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
            pageLayout.setUserStore({alarmOffFlashes: openStatus});
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
            if ($toolOperateClick.css("margin-right") == "-800px") {
                $toolOperateClick.animate({marginRight: "7px"});
            } else {
                $("#disSetMenu,#mapDropSettingMenu,#measurementMenu").hide();
                $('#measurementMenu').hide();
                $toolOperateClick.animate({marginRight: "-800px"});
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
                    $('.jumpSettingMenu').hide();
                    $("#smoothMove").attr("checked", false);
                    $("#smoothMoveLab").removeClass("preBlue");
                } else {
                    flagSwitching = true;
                    $('.jumpSettingMenu').show();
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
                    for (var i = 0; i < values.length; i++) {
                        values[i].setAngle(0);
                    }
                }
            }

            // 将显示设置存储至前端缓存
            var storeValue = {
                flagSwitching: flagSwitching,
                isCarNameShow: isCarNameShow,
                icoUpFlag: icoUpFlag,
            };
            pageLayout.setUserStore(storeValue)
        },
        // 将显示设置存储至前端缓存
        setUserStore: function (newStore) {
            var storeKey = $('#userName').text() + '_session';
            var oldSet = window.localStorage.getItem(storeKey);
            oldSet = oldSet ? JSON.parse(oldSet) : {};
            window.localStorage.setItem(storeKey, JSON.stringify($.extend(oldSet, newStore)));
        },
        // 清除百度地图相关依赖
        clearBaiduMap: function () {
            // 获取interval的最高ID然后遍历清除
            const HIGHEST_INTERVAL_ID = setInterval(';');
            for (let i = 0; i < HIGHEST_INTERVAL_ID; i++) {
                clearInterval(i)
            }
            // 获取百度地图创建的DOM并销毁
            const BAIDU_MAPS = document.querySelectorAll('.tangram-suggestion-main');
            BAIDU_MAPS.forEach((item) => {
                document.body.removeChild(item);
            });
            $('.baidumap').remove();
            // 清空百度地图后，由于会报很多未知的错误，所以进行定时器清除
            // 下面为误伤右上角时间定时器重新初始化
            $("#clockAP").MyDigitClock({
                    fontSize: 15,
                    fontColor: "grey",
                    background: "#fff",
                    fontWeight: "bold",
                    bAmPm: true,
                    timeFormat: ''
                }
            );
            $("#clock").MyDigitClock({
                fontSize: 32,
                fontColor: "grey",
                fontWeight: "bold",
                bAmPm: true,
                background: '#fff',
                timeFormat: '{HH}:{MM}'
            });
            // var protocol = document.location.protocol;// 获取当前协议类型
            // var path = protocol + '//api.map.baidu.com/api?v=2.0&type=webgl&ak=BOUTxUmuP8RMGbHvYuubgGTwWYHmNyFv&callback=mapInitCallBack';
            // var baiduScript = $("script[src='" + path + "']");
            // if (baiduScript.length) {
            //     baiduScript.remove();
            // }
            // var path1 = protocol + '//api.map.baidu.com/getscript?type=webgl&v=1.0&ak=BOUTxUmuP8RMGbHvYuubgGTwWYHmNyFv&services=&t=20210528194133';
            // var baiduScript1 = $("script[src='" + path1 + "']");
            // if (baiduScript1.length) {
            //     baiduScript1.remove();
            // }
            // var path2 = protocol + '//api.map.baidu.com/res/webgl/10/bmap.css';
            // var baiduLink = $("link[href='" + path2 + "']");
            // if (baiduLink.length) {
            //     baiduLink.remove();
            // }
            // var path3 = protocol + '//api.map.baidu.com/res/20/bmap_autocomplete.css';
            // var baiduLink1 = $("link[href='" + path3 + "']");
            // if (baiduLink1.length) {
            //     baiduLink1.remove();
            // }
        },
        // 清除四维地图相关依赖
        clearNglpMap: function () {
            var path = 'http://a.qqearth.com:81/SE_JSAPI?uid=sczwbd';
            var nglpScript = $("script[src='" + path + "']");
            if (nglpScript.length) {
                nglpScript.remove();
            }
            var path1 = 'http://a.qqearth.com:81/map.css';
            var nglpLink = $("link[href='" + path1 + "']");
            if (nglpLink.length) {
                nglpLink.remove();
            }
        },
        // 清除地图覆盖物
        clearAllMarker: function () {
            var allMarker = carNameMarkerMap.values();
            var newArr = allMarker.concat(carNameMarkerContentMap.values());
            map.remove(newArr);
            carNameMarkerMap.clear();
            carNameMarkerContentMap.clear();
            markerViewingArea.clear();
            fenceIDMap.clear();
            fenceIdList.clear();
        },
        //多地图切换
        mapTypeChange: function (e, initMapId) {
            mapTypeSwitch = true;
            window.amapFlag = true;
            var _this = initMapId ? $('#' + initMapId) : $(this);
            var id = _this.attr("id");

            $('.topMenuContainer label,.trafficBtn,.hawkEyeBtn').removeClass('preBlue');
            $('.topMenuContainer input').prop('checked', false).removeAttr('checked');

            $('#standardMap,#normalStyle,#daytimeStyle,#defaultStyle').prop('checked', true);
            $('#standardMapLab,normalStyleLab,#daytimeStyleLab,#defaultStyleLab').addClass('preBlue');

            $('.mapSubMenu p,#national').show();
            $('#MapContainer').removeClass();
            $('#POISearch,.dimensionalItem,#dimensionalMapContainer,.mapStyleMenu,#weatherContainer').hide();
            $('.amapStyleMenu,.baiduMapStyleMenu,.googleMapStyleMenu').hide();

            if (map) {
                pageLayout.clearAllMarker();
                if (map.hasDimensionalMap) {// 移除天地图三维地图
                    map.hasDimensionalMap.destroy();
                }
                if (map.disableScrollWheelZoom) {// 移除天地图的鼠标滚轮事件
                    if (map.disableDrag) map.disableDrag();
                    map.disableScrollWheelZoom();
                    map.hasDimensionalMap = null;
                }
                if (map.currentMap === 'baidu') {// 百度地图
                    pageLayout.clearBaiduMap();
                    map.clearOverlays();
                } else if (map.currentMap === 'nglpMap') {// 四维地图
                    // pageLayout.clearNglpMap();
                }
                if (map && map.destroy) {
                    map.destroy();
                }
                $('#magnifyClick,#shrinkClick,#countClick').removeClass('disabledStyle').off('click').on('click', amapOperation.toolClickList);
            }

            _this.prop('checked', true);
            _this.next('label').addClass('preBlue');

            $('.trafficBtn,.hawkEyeBtn').prop('disabled', false);
            if (id !== "aMap" && id !== "baiduMap") {// 非高德/百度地图(平滑移动、图标向上功能禁用)
                $('#smoothMove,#icoUp').prop('checked', false).prop('disabled', true).removeAttr('checked');
                $('#smoothMoveLab,#icoUpLab').removeClass('preBlue');
                flagSwitching = false;
                icoUpFlag = false;
                $('.jumpSettingMenu').hide();
            } else {
                $('#smoothMove,#icoUp').prop('disabled', false);
                pageLayout.userSetting();
            }
            var saveMapObj = null;
            switch (id) {
                case "aMap":// 高德地图
                    $('#POISearch,.mapStyleMenu,.amapStyleMenu,#weatherContainer').show();
                    $('.dimensionalItem,.panoramicItem,.terrainItem,.baiduMapStyleMenu').hide();
                    $('#normalStyle').prop('checked', true);
                    $('#normalStyleLab').addClass('preBlue');
                    if (!initMapId) {
                        saveMapObj = {
                            type: 'AMap',
                            mapId: 'aMap'
                        };
                        amapOperation.init('AMap');
                    }
                    break;
                case "googleMap"://谷歌地图
                    // 无用功能禁用(鹰眼、拉框放大、拉框缩小、量算)
                    $('.hawkEyeBtn').prop('disabled', true);
                    $('.mapStyleMenu,.googleMapStyleMenu').show();
                    $('.dimensionalItem,.satelliteRoadItem,.terrainItem,.panoramicItem').hide();
                    $('#magnifyClick,#shrinkClick,#countClick').addClass('disabledStyle').off('click');
                    $('#magnifyClick i,#shrinkClick i,#countClick i').removeClass('active');
                    $('#magnifyClick span,#shrinkClick span,#countClick span').css('color', '#5c5e62');
                    if (!initMapId) {
                        saveMapObj = {
                            type: 'GMap',
                            mapId: 'googleMap'
                        };
                        amapOperation.init('GMap');
                    }
                    break;
                case "baiduMap":// 百度地图
                    $('.trafficBtn').prop('disabled', true);
                    $('.dimensionalItem,.baiduMapStyleMenu').show();
                    $('.satelliteRoadItem,.terrainItem,.satelliteItem').hide();
                    $('#standardMapLab').removeClass('preBlue');
                    $('#standardMap').prop('checked', false).removeAttr('checked');
                    $('#dimensionalMap').prop('checked', true);
                    $('#dimensionalLab').addClass('preBlue');
                    $('.hawkEyeBtn').prop('disabled', true);
                    if (!initMapId) {
                        saveMapObj = {
                            type: 'BMap',
                            mapId: 'baiduMap'
                        };
                        amapOperation.init('BMap');
                        amapOperation.LimitedSize(6);
                    }
                    break;
                case "tianMap":// 天地图
                    // 由于天地图未提供拉框放大和缩小功能，故隐藏相应UI
                    $('#magnifyClick,#shrinkClick,#countClick').addClass('disabledStyle').off('click');
                    $('#magnifyClick i,#shrinkClick i,#countClick i').removeClass('active');
                    $('#magnifyClick span,#shrinkClick span,#countClick span').css('color', '#5c5e62');
                    $('.trafficBtn').prop('disabled', true);
                    $('.threeDimensionalItem,.satelliteRoadItem,.terrainItem,.panoramicItem').hide();
                    $('.dimensionalItem').show();
                    $('#dimensionalMap').prop('checked', false);
                    $('#dimensionalLab').removeClass('preBlue');
                    if (!initMapId) {
                        saveMapObj = {
                            type: 'TMap',
                            mapId: 'tianMap'
                        };
                        amapOperation.init('TMap');
                    }
                    break;
                case "fourMap":// 四维地图
                    // 无用功能禁用(路况、鹰眼、量算)
                    $('.trafficBtn,.hawkEyeBtn').prop('disabled', true);
                    $('.dimensionalItem,.threeDimensionalItem,.satelliteRoadItem,.terrainItem,.panoramicItem').hide();
                    $('#countClick').addClass('disabledStyle').off('click');
                    $('#countClick i').removeClass('active');
                    $('#countClick span').css('color', '#5c5e62');
                    if (!initMapId) {
                        saveMapObj = {
                            type: 'NglpMap',
                            mapId: 'fourMap'
                        };
                        amapOperation.init('NglpMap');
                    }
                    break;
            }
            if (saveMapObj) {// 保存用户最后一次切换的地图信息,用于在每次用户登录时默认加载该地图
                var username = $('#userName').text();
                window.localStorage.setItem(username + '_defaultMap', JSON.stringify(saveMapObj));
            }
        },
        // 地图功能设置
        mapSubMenuClickFn: function () {
            var _this = $(this);
            var id = _this.attr("id");
            $('.mapSubMenu label').removeClass('preBlue');
            $('.mapSubMenu input').prop('checked', false).removeAttr('checked');

            _this.prop('checked', true);
            _this.next('label').addClass('preBlue');
            $('.mapStyleMenu').hide();
            $('#normalStyle,#defaultStyle,#daytimeStyle').click();
            switch (id) {
                case "standardMap":// 标准地图
                    map.standardMap();
                    var googleCheck = $('#googleMap').is(":checked");
                    if (map.currentMap === 'aMap' || map.currentMap === 'baidu' || (map.currentMap === 'google' || googleCheck)) {// 高德及百度地图显示切换主题菜单
                        $('.mapStyleMenu').show();
                    }
                    if (map.currentMap === 'aMap') {
                        $('#normalStyle').click();
                    } else if (map.currentMap === 'baidu') {
                        $('#daytimeStyle').click();
                    } else if (map.currentMap === 'google' || googleCheck) {
                        $('#defaultStyle').click();
                    }
                    break;
                case "threeDimensionalMap":// 3D地图
                    map.threeDimensionalMap();
                    break;
                case "dimensionalMap":// 三维地球
                    map.dimensionalMap();
                    break;
                case "defaultMap":// 卫星地图
                    map.satelliteMap();
                    // amapOperation.satelliteMapSwitching();
                    break;
                case "satelliteRoadMap":// 卫星路网
                    map.satelliteRoadMap();
                    break;
                case "terrainMap":// 地形地图
                    map.terrainMap();
                    break;
                case "panoramicMap":// 全景地图
                    map.panoramicMap();
                    break;
            }
            if (map.currentMap === 'aMap' && $('.trafficBtn').hasClass('preBlue')) {
                realTimeTraffic.hide();
                realTimeTraffic.show();
            }
        },
        // 地图主题切换
        mapStyleChange: function () {
            var _this = $(this);
            $('.mapStyleMenu label').removeClass('preBlue');
            $('.mapStyleMenu input').prop('checked', false).removeAttr('checked');
            _this.prop('checked', true);
            _this.next('label').addClass('preBlue');
            if (map.currentMap === 'aMap') {// 高德
                var styleName = "amap://styles/" + _this.val();
                map.setMapStyle(styleName);
            } else if (map.currentMap === 'baidu') {// 百度
                if (_this.val() === 'night') {// 夜晚
                    map.setMapStyleV2({styleJson: styleJson2});
                } else {// 白天
                    map.setMapStyleV2({styleJson: []});
                }
            } else if (map.currentMap === 'google') {// 谷歌
                map.setOptions({styles: googleStyles[_this.val()]});
            }
        },
        // 平滑移动跳点时间设置(按用户名存错至浏览器缓存)
        jumpSettingChange: function () {
            if ($(this).hasClass('active')) return;
            var value = $(this).text();
            $('.jumpSettingMenu span').removeClass('active');
            $(this).addClass('active');
            var username = $('#userName').text();
            var jumpPointSetting = window.localStorage.getItem('jumpPointSetting');
            if (jumpPointSetting) {
                jumpPointSetting = JSON.parse(jumpPointSetting);
            } else {
                jumpPointSetting = {}
            }
            jumpPointSetting[username] = value;
            window.localStorage.setItem('jumpPointSetting', JSON.stringify(jumpPointSetting));
        },
        //获取当前服务器系统时间
        getNowFormatDate: function () {
            var url = "/clbs/v/monitoring/getTime";
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
        },
        measurementEvent: function () {
            var id = $(this).attr("id");
            // 距离量算
            if (id == "distanceMeasuremenLab") {
                amapOperation.distanceMeasuremenEvent();
            }
            // 面积量算
            else if (id == "areaMeasurementLab") {
                amapFunCollection.areaMeasurementEvent();
            }
        }
    }
;
if (location.protocol.indexOf('https:') != -1) {
    $("#fourMapLab").parent().hide()
}