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
// wjk ???????????????????????????
var computingTimeInt;
var computingTimeCallInt;
// var VideoOrPhoneCall = 0; // 0 ??????????????????1?????????2?????????3??????

var markerViewingArea;
var markerOutside;
var markerAllUpdateData;
var isCluster = false; // ????????????
var markerFocus; // ????????????id
var centerMarkerId;// ????????????marker id
var isAreaSearch = false; // ??????????????????
var callTheRollId; // ????????????ID
var markerClickLngLat = null; // ?????????????????????????????????????????????
var curDbSubscribeMOnitor;//???????????????????????????id

var onlineVoiceSettimeOut = null;// ??????????????????????????????(??????)
var onlineSettimeOut = null;// ??????????????????????????????(??????)

var waybillAndPractitionersInfo;//???????????????????????????????????????
var deviceTypeTxt = '';// ????????????
var isCarStateAdapt = false;// ?????????????????????
var isDragFlag = false;// ???????????????????????????
var fenceMapHeight = '';//??????????????????

var activeSafetyHasRiskIds = [];// ??????????????????????????????ID??????

var flagSwitching = true, isCarNameShow = true, icoUpFlag = false;


// ????????????????????????????????????
var username = $('#userName').text();
var jumpPointSetting = window.localStorage.getItem('jumpPointSetting');
if (jumpPointSetting) {
    jumpPointSetting = JSON.parse(jumpPointSetting);
    if (jumpPointSetting[username]) {
        $('.jumpSettingMenu span').removeClass('active');
        $('.jumpSettingMenu span[data-value=' + jumpPointSetting[username] + ']').addClass('active');
    }
}


var mapHeightq, tableHeightq; // table???map??????????????????
var timerq; // ?????????
var mapTypeSwitch = false;
var pageLayout = {
        // ????????????
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
            winHeight = $(window).height();//??????????????????
            headerHeight = $("#header").height();//????????????
            var tabHeight = $myTab.height();//????????????table???????????????
            var tabContHeight = $("#myTabContent").height();//table????????????
            var fenceTreeHeight = winHeight - 193;//???????????????
            $("#fenceZtree").css('height', fenceTreeHeight + "px");//?????????????????????
            //????????????
            newMapHeight = winHeight - headerHeight - tabHeight - 10;
            fenceMapHeight = newMapHeight;
            $MapContainer.css({
                "height": newMapHeight + 'px'
            });
            $('#realTimeVideoReal').css({
                "height": winHeight - 80 + 'px'
            });
            //???????????????
            var newContLeftH = winHeight - headerHeight;
            //sidebar??????
            $(".sidebar").css('height', newContLeftH + 'px');
            //????????????logo??????padding
            logoWidth = $("#header .brand").width();
            btnIconWidth = $("#header .toggle-navigation").width();
            windowWidth = $(window).width();
            newwidth = (logoWidth + btnIconWidth + 46) / windowWidth * 100;
            //?????????????????????
            $contentLeft.css({
                "width": newwidth + "%"
            });
            $contentRight.css({
                "width": 100 - newwidth + "%"
            });
            //???????????????left??????????????????
            $sidebar.attr("class", "sidebar sidebar-toggle");
//        $mainContentWrapper.attr("class", "main-content-wrapper main-content-toggle-left");
            //????????????????????????
            var newTreeH = winHeight - headerHeight - 203;
            $thetree.css({
                "height": newTreeH + "px"
            });
            //?????????????????????
            var mainContentHeight = $contentLeft.height();
            var adjustHeight = $(".adjust-area").height();
            videoHeight = (mainContentHeight - adjustHeight - 65) / 2;
            $(".videoArea").css("height", videoHeight + "px");
            //????????????????????????
            oldMapHeight = $MapContainer.height();
            myTabHeight = $myTab.height();
            wHeight = $(window).height();
            // ??????????????????
            $(".amap-logo").attr("href", "javascript:void(0)").attr("target", "");
            // ?????????????????????????????????
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
                winHeight = $(window).height();//??????????????????
                headerHeight = $("#header").height();//????????????
                var tabHeight = $myTab.height();//????????????table???????????????
                var tabContHeight = $("#myTabContent").height();//table????????????
                tabContHeight = tabContHeight <= 60 ? 0 : tabContHeight;
                var fenceTreeHeight = winHeight - 193;//???????????????
                $("#fenceZtree").css('height', fenceTreeHeight + "px");//?????????????????????
                //????????????
                newMapHeight = winHeight - headerHeight - tabHeight - tabContHeight - 10;
                monitoringObjMapHeight = newMapHeight;
                fenceMapHeight = winHeight - headerHeight - $('#fenceBindTable').height() + 20;
                if ($('#TabCarBox').hasClass('active')) {// ??????????????????????????????
                    newMapHeight = fenceMapHeight;
                }
                $MapContainer.css({
                    "height": newMapHeight + 'px'
                });
                $('#dimensionalMapContainer').css({
                    "height": newMapHeight + 'px'
                });
                //???????????????????????????????????????
                $('#realTimeVideoReal').css({
                    "height": $(window).height() - 80 + 'px'
                });
                //???????????????
                var newContLeftH = winHeight - headerHeight;
                //sidebar??????
                $(".sidebar").css('height', newContLeftH + 'px');
                //????????????logo??????padding
                logoWidth = $("#header .brand").width();
                btnIconWidth = $("#header .toggle-navigation").width();
                windowWidth = $(window).width();
                newwidth = (logoWidth + btnIconWidth + 46) / windowWidth * 100;
                //?????????????????????
                $contentLeft.css({
                    "width": newwidth + "%"
                });
                if ($contentLeft.is(':visible')) {
                    $contentRight.css({
                        "width": 100 - newwidth + "%"
                    });
                }
                //????????????????????????
                var newTreeH = winHeight - headerHeight - 203;
                $thetree.css({
                    "height": newTreeH + "px"
                });
                //?????????????????????
                var mainContentHeight = $contentLeft.height();
                var adjustHeight = $(".adjust-area").height();
                videoHeight = (mainContentHeight - adjustHeight - 65) / 2;
                $(".videoArea").css("height", videoHeight + "px");
            }
            pageLayout.showOperatingAndRepairNum();
        },
        // ????????????????????????????????????
        userSetting: function () {
            var storeKey = $('#userName').text() + '_session';
            var storeValue = window.localStorage.getItem(storeKey);
            if (storeValue) {
                var data = JSON.parse(storeValue);
                flagSwitching = data.flagSwitching !== undefined ? data.flagSwitching : flagSwitching;
                isCarNameShow = data.isCarNameShow !== undefined ? data.isCarNameShow : isCarNameShow;
                // ??????????????????
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

                // ?????????????????????
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
        // ???????????????????????????
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
            // ??????????????????
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
            // ??????????????????????????????
            Array.prototype.contains = function (suArr) {
                for (var i = 0, len = this.length; i < len; i++) {
                    if (this[i] == suArr) {
                        return true;
                    }
                }
                return false;
            }
            // ?????????????????????
            Array.intersect = function (a, b) {
                return a.each(function (o) {
                    return b.contains(o) ? o : null
                });
            };
            // ?????????????????????
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
            // ???????????????????????????????????????
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
            // ??????
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
        // ??????map??????
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
            //??????????????????????????????
            fenceIdList = new pageLayout.mapVehicle();
            AdministrativeRegionsList = new pageLayout.mapVehicle();
            travelLineList = new pageLayout.mapVehicle();
            markerViewingArea = new pageLayout.mapVehicle();
            markerOutside = new pageLayout.mapVehicle();
            markerAllUpdateData = new pageLayout.mapVehicle();
            drivingState = new pageLayout.mapVehicle();

            // ???????????????????????????????????????
            waybillAndPractitionersInfo = new pageLayout.mapVehicle();
        },
        // ??????
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
        // ??????socket????????????
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
                    //         $("#msgTitle").text("????????????");
                    //         $("#goTraceResponse").modal('show');
                    //     }
                    //     if (dataType == 0x9302) {
                    //         $("#answer").val("");
                    //         $("#msgTitle").text("?????????????????????");
                    //         $("#goTraceResponse").modal('show');
                    //     }
                    // }
                }
            }
        },
        // ????????????
        platformMsgAck: function () {
            var answer = $("#answer").val();
            if (answer == "") {
                showErrorMsg("??????????????????", "answer");
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
        //??????????????????????????????
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
        //???????????????????????????
        goHidden: function () {
            $contentLeft.hide();
            $contentRight.attr("class", "col-md-12 content-right");
            $contentRight.css("width", "100%");
            $('#videoCont').css("width", "100%");
            $goShow.show();
        },
        //???????????????????????????
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
        //????????????????????????
        mouseMove: function (e) {
            // e.stopPropagation();
            // ??????????????????div??????
            var headHeight = parseInt($('#header').height(), 10);
            $('#dragDIV2').css({'top': (e.clientY - headHeight - 3) + 'px', border: '1px dashed black'});
        },
        // ??????????????????
        mouseUp: function (e) {
            // ??????mouseup?????????
            // e.stopPropagation();
            // e.preventDefault();
            $("#fixDragDiv").css("display", "none")
            var y = els - e.clientY;
            var newHeight = mapHeight - y;
            var windowHeight = $(window).height();
            var tableHeight = windowHeight - e.clientY - 46;

            // ??????????????????????????????
            $MapContainer.css("height", newHeight + "px");
            $("#dimensionalMapContainer").css("height", newHeight + "px");
            // $('#realTimeVideoReal').css("height", newHeight + "px");
            $(pageLayout.getCurrentActiveTableName()).css("height", tableHeight + "px");

            isDragFlag = true;
            // ??????????????????mouseup?????????
            if (timerq) {
                clearTimeout(timerq);
            }
            timerq = setTimeout(function () {
                $('body').off("mousemove", pageLayout.mouseMove).off("mouseup", pageLayout.mouseUp);
            }, 10);
            // ??????dragDiv??????
            $('#dragDIV').css({backgroundColor: '#ccc'});

            // ????????????div??????
            $('#dragDIV2').remove();

        },
        // ??????map??????
        mapVehicle: function () {
            this.elements = new Array();
            //??????MAP????????????
            this.size = function () {
                return this.elements.length;
            };
            //??????MAP????????????
            this.isEmpty = function () {
                return (this.elements.length < 1);
            };
            //??????MAP????????????
            this.clear = function () {
                this.elements = new Array();
            };
            //???MAP??????????????????key, value)
            this.put = function (_key, _value) {
                this.elements.push({
                    key: _key,
                    value: _value
                });
            };
            //????????????KEY????????????????????????True???????????????False
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
            //????????????KEY????????????VALUE???????????????NULL
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
            //????????????????????????????????????element.key???element.value??????KEY???VALUE??????????????????NULL
            this.element = function (_index) {
                if (_index < 0 || _index >= this.elements.length) {
                    return null;
                }
                return this.elements[_index];
            };
            //??????MAP?????????????????????KEY?????????
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
            //??????MAP?????????????????????VALUE?????????
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
            //??????MAP?????????VALUE????????????ARRAY???
            this.values = function () {
                var arr = new Array();
                for (var i = 0, len = this.elements.length; i < len; i++) {
                    arr.push(this.elements[i].value);
                }
                return arr;
            };
            //??????MAP?????????KEY????????????ARRAY???
            this.keys = function () {
                var arr = new Array();
                for (var i = 0, len = this.elements.length; i < len; i++) {
                    arr.push(this.elements[i].key);
                }
                return arr;
            };
        },
        //??????DIV
        dragDiv: function (e) {
            // ??????head???????????????????????????div??????
            var headHeight = parseInt($('#header').height(), 10);
            // ??????dragDiv??????
            $('#dragDIV').css({backgroundColor: 'deepskyblue'});
            // ??????????????????div
            var dragDIV2Top = e.clientY - headHeight - 3;
            $('#dragDIV').after('<div id="dragDIV2" style="position: absolute;top:' + dragDIV2Top + 'px;z-index: 1999; width: 100%;height:4px; border: 1px dashed black"></div>');
            $("#fixDragDiv").css("display", "block")
            //??????????????????????????????????????? ??????????????????
            // if ($("#scalingBtn").hasClass("fa fa-chevron-down")) {//????????????????????????????????????????????????????????????bug
            tableHeight = $(pageLayout.getCurrentActiveTableName(activeIndex)).height();
            mapHeight = $MapContainer.height();

            els = e.clientY;
            $('body').bind("mousemove", pageLayout.mouseMove).bind("mouseup", pageLayout.mouseUp);
            e.stopPropagation();
            e.preventDefault();
            // }
        },
        /**
         * ?????????????????????????????????
         */
        getCurrentActiveTableName: function (type) {
            return '#myTabContent';
            // if (type === undefined){
            //     return '#realTimeStateTable-div,#alarmTable-div,#obdTable-div,#logTable-div,#securityTable-div';
            // }
            // var id;
            // if (type === 1) {//???????????????
            //     id = '#realTimeStateTable-div';
            // } else if (type === 3) { //???????????????
            //     id = '#alarmTable-div';
            // }
            // if (type === 2) {//OBD??????
            //     id = '#obdTable-div';
            // }
            // if (type === 4) {//??????
            //     id = '#logTable-div';
            // }
            // if (type === 5) {//????????????
            //     id = '#securityTable-div';
            // }
            // return id;
        },
        //????????????
        videoRealTimeShow: function (callback) {
            var $this = $('#btn-videoRealTime-show').children("i");
            if (!$this.hasClass("active")) {

                $realTimeVideoReal.removeClass("realTimeVideoMove");
                $mapPaddCon.removeClass("mapAreaTransform");
                m_videoFlag = 0; //????????????????????????
                if ($('#callSelect').hasClass('active')) {// ??????????????????
                    realtimeMonitoringVideoSeparate.callOrder();
                }
                realtimeMonitoringVideoSeparate.closeTerminalVideo()

            } else {

                // wjk
                $(this).addClass("map-active");
                $realTimeVideoReal.addClass("realTimeVideoMove");
                $mapPaddCon.addClass("mapAreaTransform");

                m_videoFlag = 1; //????????????????????????

                if (subscribeVehicleInfo) {
                    if (m_videoFlag == 1) {
                        realtimeMonitoringVideoSeparate.closeTerminalVideo();
                        realtimeMonitoringVideoSeparate.initVideoRealTimeShow(subscribeVehicleInfo);
                    }
                }

            }
        },
        // wjk ?????????????????????
        phoneCallRealTimeshow: function () {
            //???????????? ??????IE??????
            if (navigator.appName == "Microsoft Internet Explorer") {
                if (parseInt(navigator.appVersion.split(";")[1].replace(/[ ]/g, "").replace("MSIE", "")) < 10) {
                    layer.msg("????????????IE?????????????????????????????????IE10??????????????????");
                } else {
                    var $this = $('#phoneCall').children("i");
                    if (!$this.hasClass("active")) {
                        // wjk ????????????????????????
                        if (!$('#btn-videoRealTime-show').find('i').hasClass('active')) {
                            $realTimeVideoReal.removeClass("realTimeVideoShow");
                            $mapPaddCon.removeClass("mapAreaTransform");
                            m_videoFlag = 0; //????????????????????????
                        }

                        clearInterval(computingTimeCallInt)
                        realTimeVideo.closeAudio();
                    } else {

                        // wjk
                        $(this).addClass("map-active");
                        $realTimeVideoReal.addClass("realTimeVideoShow");
                        $mapPaddCon.addClass("mapAreaTransform");
                        m_videoFlag = 1; //????????????????????????
                        realTimeVideo.windowSet();
                        //????????????????????????????????????
                        setTimeout("realTimeVideo.beventLiveIpTalk(pageLayout.computingTimeCallIntFun)", 5);
                    }
                }
            } else {
                $("#phoneCall i").removeClass("active");
                $("#phoneCall span").removeAttr("style");
                layer.msg("?????????????????????????????????IE????????????????????????IE????????????");
            }
        },
        // ??????????????????
        closeVideo: function () {
            // if ($('#btn-videoRealTime-show i').hasClass('active')) {
            //     $realTimeVideoReal.removeClass("realTimeVideoShow");
            //     $mapPaddCon.removeClass("mapAreaTransform");
            //     $('#btn-videoRealTime-show i').removeClass('active');
            //     $('#btn-videoRealTime-show span').css('color', '#5c5e62');
            // }
        },
        //??????????????????
        showAlarmWindow: function () {
            $showAlarmWinMark.show();
            $("#showAlarmWin").hide();
            $("#callPolice").show();
            $("#ActiveSafetybtn").show();
        },
        //?????????????????????
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
        //??????????????????
        alarmOffSound: function () {
            var openStatus = false;// ??????????????????
            if (navigator.userAgent.indexOf('MSIE') >= 0) {
                //IE?????????
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
                //???????????????
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
        //??????????????????
        alarmOffFlashes: function () {
            var openStatus = false;// ??????????????????
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
        //????????????????????????
        showAlarmInfoSettings: function () {
            pageLayout.closeVideo();
            $("#alarmSettingInfo").modal("show");
            $("#context-menu").removeClass("open");
        },
        //??????????????????
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
        //????????????
        smoothMoveOrlogoDisplayClickFn: function () {
            var id = $(this).attr("id");
            //????????????
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
            //????????????
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
            //????????????
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

            // ????????????????????????????????????
            var storeValue = {
                flagSwitching: flagSwitching,
                isCarNameShow: isCarNameShow,
                icoUpFlag: icoUpFlag,
            };
            pageLayout.setUserStore(storeValue)
        },
        // ????????????????????????????????????
        setUserStore: function (newStore) {
            var storeKey = $('#userName').text() + '_session';
            var oldSet = window.localStorage.getItem(storeKey);
            oldSet = oldSet ? JSON.parse(oldSet) : {};
            window.localStorage.setItem(storeKey, JSON.stringify($.extend(oldSet, newStore)));
        },
        // ??????????????????????????????
        clearBaiduMap: function () {
            // ??????interval?????????ID??????????????????
            const HIGHEST_INTERVAL_ID = setInterval(';');
            for (let i = 0; i < HIGHEST_INTERVAL_ID; i++) {
                clearInterval(i)
            }
            // ???????????????????????????DOM?????????
            const BAIDU_MAPS = document.querySelectorAll('.tangram-suggestion-main');
            BAIDU_MAPS.forEach((item) => {
                document.body.removeChild(item);
            });
            $('.baidumap').remove();
            // ???????????????????????????????????????????????????????????????????????????????????????
            // ??????????????????????????????????????????????????????
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
            // var protocol = document.location.protocol;// ????????????????????????
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
        // ??????????????????????????????
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
        // ?????????????????????
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
        //???????????????
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
                if (map.hasDimensionalMap) {// ???????????????????????????
                    map.hasDimensionalMap.destroy();
                }
                if (map.disableScrollWheelZoom) {// ????????????????????????????????????
                    if (map.disableDrag) map.disableDrag();
                    map.disableScrollWheelZoom();
                    map.hasDimensionalMap = null;
                }
                if (map.currentMap === 'baidu') {// ????????????
                    pageLayout.clearBaiduMap();
                    map.clearOverlays();
                } else if (map.currentMap === 'nglpMap') {// ????????????
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
            if (id !== "aMap" && id !== "baiduMap") {// ?????????/????????????(???????????????????????????????????????)
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
                case "aMap":// ????????????
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
                case "googleMap"://????????????
                    // ??????????????????(?????????????????????????????????????????????)
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
                case "baiduMap":// ????????????
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
                case "tianMap":// ?????????
                    // ?????????????????????????????????????????????????????????????????????UI
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
                case "fourMap":// ????????????
                    // ??????????????????(????????????????????????)
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
            if (saveMapObj) {// ?????????????????????????????????????????????,???????????????????????????????????????????????????
                var username = $('#userName').text();
                window.localStorage.setItem(username + '_defaultMap', JSON.stringify(saveMapObj));
            }
        },
        // ??????????????????
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
                case "standardMap":// ????????????
                    map.standardMap();
                    var googleCheck = $('#googleMap').is(":checked");
                    if (map.currentMap === 'aMap' || map.currentMap === 'baidu' || (map.currentMap === 'google' || googleCheck)) {// ?????????????????????????????????????????????
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
                case "threeDimensionalMap":// 3D??????
                    map.threeDimensionalMap();
                    break;
                case "dimensionalMap":// ????????????
                    map.dimensionalMap();
                    break;
                case "defaultMap":// ????????????
                    map.satelliteMap();
                    // amapOperation.satelliteMapSwitching();
                    break;
                case "satelliteRoadMap":// ????????????
                    map.satelliteRoadMap();
                    break;
                case "terrainMap":// ????????????
                    map.terrainMap();
                    break;
                case "panoramicMap":// ????????????
                    map.panoramicMap();
                    break;
            }
            if (map.currentMap === 'aMap' && $('.trafficBtn').hasClass('preBlue')) {
                realTimeTraffic.hide();
                realTimeTraffic.show();
            }
        },
        // ??????????????????
        mapStyleChange: function () {
            var _this = $(this);
            $('.mapStyleMenu label').removeClass('preBlue');
            $('.mapStyleMenu input').prop('checked', false).removeAttr('checked');
            _this.prop('checked', true);
            _this.next('label').addClass('preBlue');
            if (map.currentMap === 'aMap') {// ??????
                var styleName = "amap://styles/" + _this.val();
                map.setMapStyle(styleName);
            } else if (map.currentMap === 'baidu') {// ??????
                if (_this.val() === 'night') {// ??????
                    map.setMapStyleV2({styleJson: styleJson2});
                } else {// ??????
                    map.setMapStyleV2({styleJson: []});
                }
            } else if (map.currentMap === 'google') {// ??????
                map.setOptions({styles: googleStyles[_this.val()]});
            }
        },
        // ??????????????????????????????(????????????????????????????????????)
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
        //?????????????????????????????????
        getNowFormatDate: function () {
            var url = "/clbs/v/monitoring/getTime";
            json_ajax("POST", url, "json", false, null, function (data) {
                logTime = data;
            });
        },
        // wjk,??????????????????????????????
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
                        m_videoFlag = 0; //????????????????????????
                        realTimeVideo.closeVideo(0);
                        layer.msg('???????????????????????????30s??????')
                    }
                }, 1000)
            }
        },
        //wjk ??????????????????????????????
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
                            m_videoFlag = 0; //????????????????????????
                        }
                        $("#phoneCall i").removeClass("active");
                        $("#phoneCall span").removeAttr("style");
                        realTimeVideo.closeAudio();
                        layer.msg('?????????????????????????????????60s??????')
                    }
                }, 1000)
            }
        },
        //???????????????????????????????????????
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
            // ????????????
            if (id == "distanceMeasuremenLab") {
                amapOperation.distanceMeasuremenEvent();
            }
            // ????????????
            else if (id == "areaMeasurementLab") {
                amapFunCollection.areaMeasurementEvent();
            }
        }
    }
;
if (location.protocol.indexOf('https:') != -1) {
    $("#fourMapLab").parent().hide()
}