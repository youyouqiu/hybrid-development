var infoScrollBoxW = null; //司机信息栏的宽度
var scrollNum = 3;
var scrollIndex = 0;
var vehicleId;//车id
var driverLists = [];
var lockType = false;//判断是否为插卡司机(true:插卡司机,禁止切换司机)

activeSafety = {
    //获取从业人员列表
    riskInformationDetails: function (id) {
        vehicleId = id;
        json_ajax("POST", '/clbs/adas/v/monitoring/getRiskProfessionalsInfo', "json", true, {vehicleId: id}, activeSafety.listRiskInformationDetails);
    },
    listRiskInformationDetails: function (data) {
        //重置人证对比
        $('#personCompareBtn').removeClass('btn-primary').attr('disabled', 'disabled');
        $('#personComparePhoto').attr('src', '');
        $('#compareName').text('');
        $('#compareCard').text('');

        $('#icDriverInfo').addClass('hide');
        lockType = false;
        infoScrollBoxW = $('#driverInfoDiv').width();
        $('#infoScrollCont').css('left', '0px');
        scrollIndex = 0;
        if (data.success && data.obj.length) {
            var data = data.obj;
            driverLists = data;

            $('#infoScrollCont').css('width', infoScrollBoxW * data.length + 'px');
            scrollNum = data.length;
            var d = new Date();
            var radomNum = d.getTime();
            var html = '';
            var icCardEndDateStr = '';
            for (var i = 0; i < data.length; i++) {
                if (data[i]) {
                    if (data[i].icCardEndDateStr) {
                        icCardEndDateStr = data[i].icCardEndDateStr;
                    }
                    html += '<div class="col-md-12 singleInfoCont" style="width:' + infoScrollBoxW + 'px;">' +
                        '<div class="col-md-2 driverPhoto">' +
                        '<img id="driverPhoto" src=' + activeSafety.handleImgSrc(data[i].photograph) + '?t=' + radomNum + '>' +
                        '</div>' +
                        '<div class="col-md-10 infoCont">' +
                        '<div class="col-md-5">司机姓名：' + activeSafety.handleHtml(data[i].name) + '</div>' +
                        '<div class="col-md-7">岗位类型：' + activeSafety.handleHtml(data[i].type) + '</div>' +
                        '<div class="col-md-5">手机号：' + activeSafety.handleHtml(data[i].phone) + '</div>' +
                        '<div class="col-md-7">身份证号：' + activeSafety.handleHtml(data[i].identity) + '</div>' +
                        '<div class="col-md-12">所属企业：' + activeSafety.handleHtml(data[i].groupName) + '</div>' +
                        '<div class="col-md-12">从业资格证号：' + activeSafety.handleHtml(data[i].cardNumber) + '</div>' +
                        '<div class="col-md-12">从业资格类别：' + activeSafety.handleHtml(data[i].qualificationCategory) + '</div>' +
                        '<div class="col-md-12">发证机关：' + activeSafety.handleHtml(data[i].icCardAgencies) + '</div>' +
                        '<div class="col-md-12">有效期至：' + activeSafety.handleHtml(data[i].icCardEndDateStr) + '</div>' +
                        '</div>' +
                        '</div>';
                } else {
                    html += '<div class="col-md-12 singleInfoCont" style="width:' + infoScrollBoxW + 'px;">' +
                        '<div class="col-md-2 driverPhoto">' +
                        '<img  id="driverPhoto" src="/clbs/resources/img/peoplems.png"/>' +
                        '</div>' +
                        '<div class="col-md-10 infoCont">' +
                        '<div class="col-md-5">司机姓名：未知</div>' +
                        '<div class="col-md-7">岗位类型：未知</div>' +
                        '<div class="col-md-5">手机号：未知</div>' +
                        '<div class="col-md-7">身份证号：未知</div>' +
                        '<div class="col-md-12">所属企业：未知</div>' +
                        '<div class="col-md-12">从业资格证号：未知</div>' +
                        '<div class="col-md-12">从业资格类别：未知</div>' +
                        '<div class="col-md-12">发证机关：未知</div>' +
                        '<div class="col-md-12">有效期至：未知</div>' +
                        '</div>' +
                        '</div>';
                }
            }

            //司机ic卡信息
            if (data.length > 0 && data[0] && data[0].lockType == 1) {//lockType:1代表插卡
                var driverCardNumber = data[0].cardNumber,
                    driverName = data[0].name,
                    identityNumber = data[0].identity;
                lockType = true;
                activeSafety.icCardsProfessionals(identityNumber + '_' + driverName);
                //人证对比
                $('#personCompareBtn').removeAttr('disabled').addClass('btn-primary');
                // var personCompareUrl = $('#driverInfoDiv').eq(0).attr('src');
                $('#personComparePhoto').attr('src', activeSafety.handleImgSrc(data[0].photograph) + '?t=' + radomNum);
                $('#compareName').text(driverName);
                $('#compareCard').text(driverCardNumber);
            }
        } else {
            $('#infoScrollCont').css('width', infoScrollBoxW + 'px');
            scrollNum = 0;

            var html = '<div class="col-md-12 singleInfoCont" style="width:' + infoScrollBoxW + 'px;">' +
                '<div class="col-md-2 driverPhoto">' +
                '<img  id="driverPhoto" src="/clbs/resources/img/peoplems.png"/>' +
                '</div>' +
                '<div class="col-md-10 infoCont">' +
                '<div class="col-md-5">司机姓名：未知</div>' +
                '<div class="col-md-7">岗位类型：未知</div>' +
                '<div class="col-md-5">手机号：未知</div>' +
                '<div class="col-md-7">身份证号：未知</div>' +
                '<div class="col-md-12">所属企业：未知</div>' +
                '<div class="col-md-12">从业资格证号：未知</div>' +
                '<div class="col-md-12">从业资格类别：未知</div>' +
                '<div class="col-md-12">发证机关：未知</div>' +
                '<div class="col-md-12">有效期至：未知</div>' +
                '</div>' +
                '</div>'
        }

        $('#infoScrollCont').html(html);
    },
    /**
     * 司机ic卡信息
     */
    icCardsProfessionals: function (cardNumber) {
        var paramer = {
            vehicleId: vehicleId,
            cardNumber: cardNumber
        };
        json_ajax("POST", '/clbs/m/reportManagement/driverStatistics/getIcCardDriverInfo', "json", true, paramer, activeSafety.getIcCardDriverInfoCB);
    },
    getIcCardDriverInfoCB: function (data) {
        // debugger
        if (data.success && data.obj) {
            var infos = data.obj;
            for (var key in infos) {
                var value = infos[key];
                value = value ? value : '--';
                /*  switch (value){
                      case 'todayLastTravelMile':
                      case 'todayTravelMile':
                      case 'todayTravelMile':
                          value = value.fixed(2);
                          break;
                      default:
                          break
                  }*/

                $('#' + key).text(value);
            }

            $('#icDriverInfo').removeClass('hide');
        }
    },
    handleHtml: function (data) {
        return data ? data : '未知';
    },
    handleImgSrc: function (src) {
        if (src) {
            return src;
        } else {
            return '/clbs/resources/img/peoplems.png';
        }
    },
    scrollLeftAnimate: function () {
        if (scrollIndex < scrollNum - 1) {
            scrollIndex++;
            // activeSafety.getIcNumber(scrollIndex);
            $('#infoScrollCont').css('left', '-' + scrollIndex * infoScrollBoxW + 'px')
        }

    },
    scrollRightAnimate: function () {
        if (scrollIndex > 0) {
            scrollIndex--;
            // activeSafety.getIcNumber(scrollIndex);
            $('#infoScrollCont').css('left', '-' + scrollIndex * infoScrollBoxW + 'px');
        }
    },
    /*getIcNumber: function (index) {
        if (driverLists.length == 0) return;
        var lockType = driverLists[index].lockType;
        var cardNumber = driverLists[index].cardNumber;
        if (lockType == 1) {
            activeSafety.icCardsProfessionals(cardNumber);
        }
    },*/
    inquiryDriverInfo: function (riskId, vehicleId, that, brand, e) {
        window.event ? window.event.cancelBubble = true : e.stopPropagation();
        tableSecurity.setActiveRow(undefined);
        activeSafety.riskInformationDetails(vehicleId);
        if ($('#callSelect').hasClass('active')) {// 关闭对讲功能
            realtimeMonitoringVideoSeparate.callOrder();
        }

        var that = that;
        dataTableOperation.closePopover();


        $("#search_condition").val(brand);

        treeMonitoring.search_condition();

        setTimeout(function () {
            // 左侧组织树
            //为表格添加高亮
            // $(that).parents('tr').addClass('active').siblings().removeClass('active');
            var numberPlate = vehicleId;
            $(".ztree li a").removeClass("curSelectedNode_dbClick");
            $(".ztree li a").removeClass("curSelectedNode");
            //为车辆树添加高亮
            var zTreeDataTables = $.fn.zTree.getZTreeObj("treeDemo");

            var nodes = zTreeDataTables.getNodesByParam("id", numberPlate, null);
            for (var i = 0; i < nodes.length; i++) {
                // zTreeDataTables.checkNode(nodes[i], true, true);
                var ztreeStyleDbclick = nodes[i].tId;
                var $span = $("#" + ztreeStyleDbclick).find("a span:eq(1)");
                setTimeout(function () {
                    $span.dblclick();
                }, 1000)
            }

            /*var dataTabCheckedNum = zTreeDataTables.getCheckedNodes(true);
            for (var i = 0; i < dataTabCheckedNum.length; i++) {
                if (dataTabCheckedNum[i].id == numberPlate) {
                    ztreeStyleDbclick = dataTabCheckedNum[i].tId;
                    if (i == 0){
                        $("#" + ztreeStyleDbclick).children("a").addClass("curSelectedNode_dbClick");
                    }
                }
            }*/
            realtimeMonitoringVideoSeparate.closeTerminalVideo();
            // 判断监控对象是否在线
            var url = "/clbs/v/oilmassmgt/oilcalibration/checkVehicleOnlineStatus";
            var param = {"vehicleId": vehicleId};
            json_ajax("POST", url, "json", false, param, function (data) {
                if (data.success) {
                    //播放视频
                    activeSafety.openVideo(nodes[0] ? nodes[0] : {
                        id: vehicleId,
                        name: brand
                    });
                    TimeFn = setTimeout(function () {
                        var objID = vehicleId;
                        treeMonitoring.centerMarker(objID, 'DBLCLICK');
                    }, 300);
                } else {
                    layer.msg('该监控对象不在线');
                }
            });
        }, 1000)
    },
    openVideo: function (treeNode) {
        var $this = $('#btn-videoRealTime-show').children("i");
        if (!$this.hasClass("active")) {
            $this.addClass('active');
            $(this).addClass("map-active");
            $realTimeVideoReal.addClass("realTimeVideoMove");
            $mapPaddCon.addClass("mapAreaTransform");
            $('#btn-videoRealTime-show').css({right: '680px', transition: 'all 0.6s'})
            m_videoFlag = 1; //标识视频窗口打开
        }


        var vehicleInfo = new Object();
        vehicleInfo.vid = treeNode.id;
        vehicleInfo.brand = treeNode.name;
        vehicleInfo.deviceNumber = treeNode.deviceNumber;
        vehicleInfo.plateColor = treeNode.plateColor;
        vehicleInfo.isVideo = treeNode.isVideo;

        vehicleInfo.simcardNumber = treeNode.simcardNumber

        subscribeVehicleInfo = vehicleInfo; //订阅的车辆信息全局变量

        realTimeVideo.setVehicleInfo(vehicleInfo)

        activeSafety.riskInformationDetails(treeNode.id)
        // authorwjk
        if (m_videoFlag == 1) {
            realtimeMonitoringVideoSeparate.closeTerminalVideo()
            realtimeMonitoringVideoSeparate.initVideoRealTimeShow(vehicleInfo);
        }
    }
}


$('#btn-videoRealTime-show').click(function () {
    // pageLayout.videoRealTimeShow();
    if (subscribeVehicleInfo) {
        if (!dataTableOperation.checkVehicleOnlineStatus(subscribeVehicleInfo.vid)) {
            layer.msg('监控对象离线');
            return;
        }
        if (!$('#realTimeVideoReal').hasClass('realTimeVideoMove')) {
            $('#btn-videoRealTime-show').css({right: '680px', transition: 'all 0.6s'})
        } else {
            $('#btn-videoRealTime-show').css({right: '10px', transition: 'all 0.6s'})
        }
        activeSafety.riskInformationDetails(subscribeVehicleInfo.vid)
    }
})
$('.nextBtnBox').click(function () {
    if (lockType) return;
    activeSafety.scrollLeftAnimate()
})
$('.prevBtnBox').click(function () {
    if (lockType) return;
    activeSafety.scrollRightAnimate()
})

/*$('#toggle-left-button').click(function () {
    setTimeout(function () {
        infoScrollBoxW = $('#infoScrollBox').width();
        $('#infoScrollCont').css('width', infoScrollBoxW * 3 + 'px');
        $('.singleInfoCont').css('width', infoScrollBoxW + 'px');
        $('#infoScrollCont').css('left', '-' + scrollIndex * infoScrollBoxW + 'px')
    }, 500)
})*/
$(function () {
    var adasSwitch = $("#adasSwitch").val();
    if (adasSwitch === "false") {
        $("#activeSafetyTab").hide();
        $("#btn-videoRealTime-show").hide();
    }
})