(function (window, $) {
    var alarmAnalyzePieChart;
    var alarmAnalyzeBarChart;
    var alarmChartBar;
    var riskAnalyzePieChart;
    var timeChart;

    var isCarNameShow = true;
    var markerAllUpdateData;
    var carNameMarkerMap;
    var carNameContentLUMap;
    var monitoringObjLngLatMap; // 订阅监控对象经纬度集合
    var monitoringObjMap; // 订阅监控对象集合
    var monitoringObjNameMap; // 车牌号图标集合
    var oldVehicleID;
    var oldRiskID;
    var mapBounds; // 地图可视区域范围
    var infoWindowOpenState = false; // 信息窗是否开启状态
    var infoContent = [];

    var amapHot;
    var codeLevel;
    var userAreaCode;

    var myTable;
    var activePageNum;
    var ifLoadPageData = true;

    var ifPicOrVideo;
    //多媒体预览
    var mediaIndex = 0;
    var title = [],
        mediaUrl = [],
        mediaType,
        mediaId;
    var protocol = '0'; //监控对象协议类型

    var eventData = [];

    var driverCardIndex;
    var driverCardNum;

    var checkedVehicleInfo;

    var driverData; //司机信息

    var riskEventListOriginal = []; //原始数据
    var riskEventList = []; //排序，筛选过的数据

    var refreshTimer = null;
    var timeStamp;

    var selectedRiskId;
    var selectedRiskNumber;
    var selectedRiskWarningTime;
    var isCarSubscribe = false; //是否有订阅的车辆

    var mapWindowInfo = [];

    //抓拍参数设置初始化
    var initPhotoParam = {
        chroma: 125,
        command: 1,
        contrast: 127,
        distinguishability: 1,
        luminance: 125,
        photographType: 0,
        quality: 5,
        saturability: 127,
        saveSign: 0,
        time: 0,
        wayID: 6,
    };

    window.securityEnterprise = {
        init: function () {

            timeStamp = (new Date()).getTime();

            securityEnterprise.createMap();

            securityEnterprise.initChart();
            securityEnterprise.initMap();

            // securityEnterprise.getRiskTableData();

            securityEnterprise.getRiskCompanyData();

            // securityEnterprise.getOverseeLevelSelect();

            securityEnterprise.isGetSocket();

            securityEnterprise.comTypeTree(); //报警类型筛选

            securityEnterprise.setTimeInterval();
        },
        /**
         * 获取报警督办等级
         */
        /*getOverseeLevelSelect: function () {

            json_ajax('post', '/clbs/oversee/getOverseeLevelMap', 'json', true, {}, function (res) {
                if (res.success) {
                    var data = res.obj;
                    var html = '';
                    data.forEach(function (val) {
                        html += '<option value=' + val.level + '>' + val.levelName + '</option>'
                    });

                    $('#overseeLevelSelect').html(html);
                }
            })
        },*/
        /**
         * socket链接
         */
        isGetSocket: function () {
            if (webSocket.conFlag) {
                setTimeout(function () {
                    webSocket.subscribe(headers, '/user/' + $("#userName").text() + '/riskInfo', function () {
                        if (!$('#riskDetailCont').hasClass('active')) {
                            securityEnterprise.getRiskCompanyData();
                        }
                    }, "/app/vehicle/subscribeStatus", null);
                }, 1000);
            } else {
                setTimeout(function () {
                    securityEnterprise.isGetSocket();
                }, 200);
            }

        },
        getRiskTableData: function () {
            $.ajax({
                type: "POST",
                url: "/clbs/s/riskManage/intelligence/regional/enterprise/ranking",
                dataType: "json",
                async: true,
                data: {
                    pageNum: 1,
                    pageSize: 20,
                    riskIds: ''
                },
                success: function (data) {
                    if (data.success) {
                        // securityEnterprise.updateRiskTableHtml(data.obj)
                        var obj = data.obj;

                        var html = '';
                        obj.forEach(function (ele, ind) {
                            html += '<tr>' +
                                '<td>' + (ind + 1) + '</td>' +
                                '<td>' + securityEnterprise.handleHtml(ele.enterpriseName) + '</td>' +
                                '<td>' + securityEnterprise.handleHtml(ele.areaName) + '</td>' +
                                '<td>' + securityEnterprise.handleHtml(ele.networkVehicleCount) + '</td>' +
                                '<td>' + securityEnterprise.handleHtml(ele.goOnLineVehicleCount) + '</td>' +
                                '<td>' + securityEnterprise.handleHtml(ele.onLineVehicleCount) + '</td>' +
                                '<td>' + securityEnterprise.handleHtml(ele.riskCount) + '</td>' +
                                '<td>' + securityEnterprise.handleHtml(ele.averageRiskCount) + '</td>' +
                                '</tr>'
                        })
                        $('#highRiskCompanyTableBody').html(html)
                    }
                }
            });

        },
        getRiskCompanyData: function (ids) {
            var riskIds = ids ? ids : '';
            $.ajax({
                type: "POST",
                url: "/clbs/adas/s/riskManage/intelligence/getRisks",
                dataType: "json",
                async: true,
                data: {
                    pageNum: 1,
                    pageSize: 20,
                    riskIds: riskIds,
                    enterpriseFlag: true
                },
                success: function (data) {
                    if (data.success) {
                        // securityEnterprise.updateRiskTableHtml(data.obj)
                        riskEventList = riskEventListOriginal = data.obj;
                        activePageNum = 1;
                        securityEnterprise.renderRiskTable();
                    }
                }
            });
        },
        /**
         * 只复制值，避免改变原数组
         */
        cloneArray: function (arr) {
            var clonearr = []
            for (var i = 0; i < arr.length; i++) {
                clonearr.push(arr[i])
            }
            return clonearr;
        },
        /**
         * 渲染风险列表数据
         */
        renderRiskTable: function () {
            var filterRiskEventList = [];
            if (riskEventList != null) {
                //列表筛选，查询
                filterRiskEventList = riskEventList.filter(function (element) {
                    var bool = true;
                    if ($('#riskEvidence').css('display') !== 'none') { //风险证据
                        var _bool = true,
                            val = $('#riskEvidence').attr('fiterval');
                        if (val === "videoAndPic") {
                            if (element.picFlag !== 1 || element.videoFlag !== 1) {
                                bool = false;
                            }
                        }
                        if (val === "video") {
                            if (!(element.picFlag === 0 && element.videoFlag === 1)) {
                                bool = false;
                            }
                        }
                        if (val === "pic") {
                            if (!(element.picFlag === 1 && element.videoFlag === 0)) {
                                bool = false;
                            }
                        }
                        if (val === "noMedia") {
                            if (!(element.picFlag === 0 && element.videoFlag === 0)) {
                                bool = false;
                            }
                        }
                    }
                    if ($('#monitoringBrand').css('display') !== 'none') { //监控对象
                        if (element.brand.indexOf($('#monitoringBrand').attr('fiterval')) === -1) {
                            bool = false;
                        }
                    }
                    if ($('#monitoringGroup').css('display') !== 'none') { //所属企业
                        if (element.groupName.indexOf($('#monitoringGroup').attr('fiterval')) === -1) {
                            bool = false;
                        }
                    }
                    if ($('#operationType').css('display') !== 'none') { //运营类型
                        // if (element.vehiclePurpose != $('#operationType').attr('fiterval') ){
                        //     bool = false;
                        // }
                        if (!element.vehiclePurpose) element.vehiclePurpose = '';
                        if (element.vehiclePurpose.indexOf($('#operationType').attr('fiterval')) === -1) {
                            bool = false;
                        }
                    }
                    if ($('#riskType').css('display') !== 'none') { //风险类型
                        var fiterval = $('#riskType').attr('fiterval');
                        if (fiterval !== '全部') {
                            var arr = element.riskType.split('、');
                            for (var i = 0; i < arr.length; i++) {
                                if (fiterval.indexOf(arr[i]) === -1) {
                                    bool = false;
                                }
                            }
                        }
                    }

                    return bool;
                })
            }

            var html = '';
            for (var i = 0; i < filterRiskEventList.length; i++) {
                var trClass = "";
                if (filterRiskEventList[i].riskId === oldRiskID) {
                    trClass = "riskListTr activeTr"
                } else {
                    trClass = "riskListTr";
                }

                html += '<tr class="' + trClass + '" vehicleId=' + filterRiskEventList[i].vehicleId + ' riskId=' + filterRiskEventList[i].riskId + ' riskNumber=' + filterRiskEventList[i].riskNumber + '>' +
                    '<td>' + (i + 1) + '</td>' +
                    '<td>' +
                    securityEnterprise.ifPicAndVideo(filterRiskEventList[i].picFlag, filterRiskEventList[i].videoFlag, filterRiskEventList[i].riskId, filterRiskEventList[i].riskType) +
                    '</td>' +
                    '<td>' + securityEnterprise.handleHtml(filterRiskEventList[i].brand) + '</td>' +
                    '<td>' + securityEnterprise.handleHtml(filterRiskEventList[i].groupName) + '</td>' +
                    '<td>' + securityEnterprise.handleHtml(filterRiskEventList[i].vehiclePurpose) + '</td>' +
                    '<td>' + securityEnterprise.handleHtml(filterRiskEventList[i].riskType) + '</td>' +
                    '<td>' + securityEnterprise.handleHtmlLevel(filterRiskEventList[i].riskLevel) + '</td>' +
                    '<td>' + securityEnterprise.handleHtml(filterRiskEventList[i].warningTime) + '</td>' +
                    '</tr>'
            }

            if (filterRiskEventList.length === 0) {
                $('#fixedTableHBox').css('width', 'calc(100% - 30px)')
            } else {
                if (securityEnterprise.OSnow() == 'mac') {
                    $('#fixedTableHBox').css('width', 'calc(100% - 30px)')
                }
                if (securityEnterprise.OSnow() == 'windows') {
                    $('#fixedTableHBox').css('width', 'calc(100% - 47px)')
                }
            }

            $('#riskTableBody').html(html);
            $('#displayNoneTable').html(html);

            var viewportHeight = $('#riskTableCont').innerHeight();
            var dataTableHeight = $('#dataTable').height();
            if (dataTableHeight < viewportHeight) {
                $('#fixedTableHBox').css('width', 'calc(100% - 30px)')
            }
        },
        /**
         * 判断电脑系统
         * @constructor
         */
        OSnow: function () {
            var agent = navigator.userAgent.toLowerCase();
            var isMac = /macintosh|mac os x/i.test(navigator.userAgent);
            if (agent.indexOf("win32") >= 0 || agent.indexOf("wow32") >= 0) {
                return 'windows';
            }
            if (agent.indexOf("win64") >= 0 || agent.indexOf("wow64") >= 0) {
                return 'windows';
            }
            if (isMac) {
                return 'mac';
            }
        },
        /**
         * 组装风险列表数据
         * @param data
         */
        updateRiskTableHtml: function (data) {
            var html = '';

            if (data != null) {
                for (var i = 0; i < data.length; i++) {

                    var overseeBtn, subCheckInput, dealDeadTime;
                    if (data[i].overseeStatus == '1') {
                        overseeBtn = '已督办';
                        // subCheckInput = '<input type="checkbox" disabled name="subChk" riskId='+data[i].riskId+'>'
                        dealDeadTime = data[i].dealDeadTime;
                    } else {
                        overseeBtn = '未督办';
                        // subCheckInput = '<input type="checkbox" name="subChk" riskId='+data[i].riskId+'>';
                        dealDeadTime = '-'
                    }


                    html += '<tr class="riskListTr" vehicleId=' + data[i].vehicleId + ' riskId=' + data[i].riskId + '>' +
                        '<td>' + (i + 1) + '</td>' +
                        // '<td>' +
                        // subCheckInput +
                        // '</td>' +
                        '<td>' +
                        overseeBtn +
                        '</td>' +
                        '<td>' + dealDeadTime + '</td>' +
                        '<td>' +
                        securityEnterprise.ifPicAndVideo(data[i].picFlag, data[i].videoFlag, data[i].riskId) +
                        '</td>' +
                        '<td>' + securityEnterprise.handleHtml(data[i].brand) + '</td>' +
                        '<td>' + securityEnterprise.handleHtml(data[i].groupName) + '</td>' +
                        '<td>' + securityEnterprise.handleHtml(data[i].vehiclePurpose) + '</td>' +
                        '<td>' + securityEnterprise.handleHtml(data[i].riskType) + '</td>' +
                        '<td>' + securityEnterprise.handleHtmlLevel(data[i].riskLevel) + '</td>' +
                        '<td>' + securityEnterprise.handleHtml(data[i].warningTime) + '</td>' +
                        '</tr>'
                }
            }


            activePageNum = 1;

            if (!data) {
                $('#fixedTableHBox').css('width', 'calc(100% - 30px)')
            }


            $('#riskTableBody').html(html);

            $('#displayNoneTable').html(html);

            var viewportHeight = $('#riskTableCont').innerHeight();
            var dataTableHeight = $('#dataTable').height();
            if (dataTableHeight < viewportHeight) {
                $('#fixedTableHBox').css('width', 'calc(100% - 30px)')
            }

            $('#riskTableCont').unbind("scroll").bind("scroll", securityEnterprise.activeScrollPage);
        },
        ifPicAndVideo: function (picFlag, videoFlag, id, riskType) {
            var spanHtml = '';
            if (picFlag == 1) {
                spanHtml += '<span class="risk_img" onclick="securityEnterprise.getMedia(\'' + id + '\',0,event,false, \'' + riskType + '\')"></span>';
            } else {
                spanHtml += '<span class="risk_img risk_img_no"></span>';
            }
            if (videoFlag == 1) {
                spanHtml += '<span class="risk_video" onclick="securityEnterprise.getMedia(\'' + id + '\',2,event,false,\'' + riskType + '\')"></span>';
            } else {
                spanHtml += '<span class="risk_video risk_video_no"></span>';
            }
            return spanHtml;
        },
        /**
         * 多媒体预览
         * @param riskId
         * @param type
         * @param e
         * @param flag
         */
        getMedia: function (riskId, type, e, flag) {
            e.stopPropagation();
            multimediaFlag = flag;
            mediaType = type;
            mediaId = riskId;
            var mediaContent = $('.media-content');
            type == 0 ? mediaContent.removeClass('video_show') : mediaContent.addClass('video_show');
            if (flag) {
                securityEnterprise.getEventMediaAjax();
            } else {
                securityEnterprise.getMediaAjax();
            }
        },
        //事件预览方法
        getEventMediaAjax: function () {
            json_ajax('POST', "/clbs/r/riskManagement/disposeReport/getEventMedia", 'json', true, {
                "eventId": mediaId,
                "mediaType": mediaType
            }, function (datas) {
                var data = datas.obj;
                //重置
                mediaIndex = 0;
                title = [];
                mediaUrl = [];
                if (datas.success) {
                    for (var i = 0, len = data.length; i < len; i++) {
                        var item = data[i];
                        title.push(item.riskType + '--' + item.riskEventType);
                        mediaUrl.push(item.mediaUrl);
                    }
                } else {
                    layer.msg('获取数据失败');
                }

                if (mediaType == 0) {
                    if (mediaUrl.length == 0) {
                        $("#img").attr("disabled", true);
                        $("#video").attr("disabled", false);
                        return;
                    } else {
                        $("#img").attr("disabled", false);
                        $("#video").attr("disabled", false);
                    }
                } else if (mediaType == 2) {
                    if (mediaUrl.length == 0) {
                        $("#video").attr("disabled", true);
                        $("#img").attr("disabled", false);
                        return;
                    } else {
                        $("#video").attr("disabled", false);
                        $("#img").attr("disabled", false);
                    }
                }
                $('#myModal').modal('show');
                securityEnterprise.getMediaDom();
            });
        },
        //报警列表预览方法
        getMediaAjax: function () {
            json_ajax('POST', "/clbs/r/riskManagement/disposeReport/getRiskMedia", 'json', true, {
                "riskId": mediaId,
                "mediaType": mediaType
            }, function (datas) {
                var data = datas.obj;
                //重置
                mediaIndex = 0;
                title = [];
                mediaUrl = [];
                if (datas.success) {
                    for (var i = 0, len = data.length; i < len; i++) {
                        var item = data[i];
                        title.push(item.riskType + ' -- ' + item.riskEventType);
                        mediaUrl.push(item.mediaUrl);
                    }
                } else {
                    layer.msg('获取数据失败');
                }

                if (mediaType == 0) {
                    if (mediaUrl.length == 0) {
                        $("#img").attr("disabled", true);
                        $("#video").attr("disabled", false);
                        return;
                    } else {
                        $("#img").attr("disabled", false);
                        $("#video").attr("disabled", false);
                    }
                } else if (mediaType == 2) {
                    if (mediaUrl.length == 0) {
                        $("#video").attr("disabled", true);
                        $("#img").attr("disabled", false);
                        return;
                    } else {
                        $("#video").attr("disabled", false);
                        $("#img").attr("disabled", false);
                    }
                }
                $('#myModal').modal('show');
                securityEnterprise.getMediaDom();
            });
        },
        getMediaDom: function () {
            var html = '',
                src = mediaUrl[mediaIndex];
            if (mediaType == 0) {
                $(".Video button").removeClass("btn-primary").addClass("btn-default");
                $(".Img button").addClass("btn-primary").removeClass("btn-default");
            }
            if (mediaType == 2) {
                $(".Img button").removeClass("btn-primary").addClass("btn-default");
                $(".Video button").addClass("btn-primary").removeClass("btn-default");
            }

            if (mediaType == 2) { //视频
                if (window.navigator.userAgent.toLowerCase().indexOf('ie') >= 0) {
                    html += '<embed id="videoDom" src="' + src + '" autostart=false style="height:100%;"></embed>';
                } else {
                    html = '<video id="videoDom" src="' + src + '" controls="controls" controlsList="nodownload" style="height:100%;"></video>'
                }
            } else if (mediaType == 0) { //图片
                html = '<img id="imgDom" src="' + src + '" style="height:100%"/>';
            }

            $('#media').html(html);
            securityEnterprise.getMediaMsg(true);
        },
        getMediaMsg: function (flag) {
            if (flag) {
                $('#myModalLabel').text(title[0]);
            }
            $('#current').text(mediaIndex + 1);
            $('#count').text(mediaUrl.length);
        },
        videoChange: function () {
            var self = $(this);
            mediaType = self.data('value');

            // if (self.hasClass('active')) {
            //     return;
            // }
            //
            // self.addClass('active').siblings('.btn').removeClass('active');
            if (multimediaFlag) {
                securityEnterprise.getEventMediaAjax();
            } else {
                securityEnterprise.getMediaAjax();
            }

        },
        mediaChange: function () {
            var id = $(this).attr('id');
            var len = mediaUrl.length == 0 ? 0 : mediaUrl.length - 1;

            if (id == 'arrowsLeft') {
                if (mediaIndex == 0) {
                    mediaIndex = 0;
                    layer.msg('已经到头了');
                    return;
                }
                mediaIndex--;
            } else if (id == 'arrowsRight') {
                if (mediaIndex == len) {
                    mediaIndex = len;
                    layer.msg('已经到头了');
                    return;
                }
                mediaIndex++;
            }
            $('#myModalLabel').html(title[mediaIndex]);
            if (mediaType == 2) {
                $('#videoDom').attr('src', mediaUrl[mediaIndex]);
            } else if (mediaType == 0) {
                $('#imgDom').attr('src', mediaUrl[mediaIndex]);
            }
            securityEnterprise.getMediaMsg();
        },
        downloadMedia: function () {
            var url = mediaUrl[mediaIndex];
            if (!url) {
                return;
            }
            $.post('/clbs/r/riskManagement/disposeReport/canDownload', {
                "mediaUrl": url,
            }, function (data) {
                if (data.success) {
                    window.location.href = url;
                } else {
                    if (data.msg) {
                        layer.msg(data.msg);
                    }
                }
            }, 'json');
            return false;
        },
        handleHtml: function (data) {
            return data ? data : '';
        },
        handleAdressHtml: function (data) {
            return data ? data : '未定位';
        },
        handleHtmlLevel: function (data) {
            switch (data) {
                case 1:
                    return '<i style="color: #f3cd34">一般（低）</i>';
                    break;
                case 2:
                    return '<i style="color: #f3cd34">一般（中）</i>';
                    break;
                case 3:
                    return '<i style="color: #f3cd34">一般（高）</i>';
                    break;
                case 4:
                    return '<i style="color: #dd7315">较重（低）</i>';
                    break;
                case 5:
                    return '<i style="color: #dd7315">较重（中）</i>';
                    break;
                case 6:
                    return '<i style="color: #dd7315">较重（高）</i>';
                    break;
                case 7:
                    return '<i style="color: #d80505">严重（低）</i>';
                    break;
                case 8:
                    return '<i style="color: #d80505">严重（中）</i>';
                    break;
                case 9:
                    return '<i style="color: #d80505">严重（高）</i>';
                    break;
                case 10:
                    return '<i style="color: #861d99">特重（低）</i>';
                    break;
                case 11:
                    return '<i style="color: #861d99">特重（中）</i>';
                    break;
                case 12:
                    return '<i style="color: #861d99">特重（高）</i>';
                    break;
            }
        },
        //滚动加载
        activeScrollPage: function () {
            //真实内容的高度
            var pageH = $('#dataTable').height();
            //视窗的高度
            var viewportHeight = $('#riskTableCont').innerHeight();
            //隐藏的高度
            var scrollHeight = $('#riskTableCont').scrollTop();

            if (pageH - viewportHeight - scrollHeight <= 0) {
                if (!ifLoadPageData) {
                    return;
                }
                ifLoadPageData = false;
                activePageNum++;

                var riskIds = ''
                $('.riskListTr').each(function (ind, ele) {
                    riskIds += $(ele).attr('riskId') + ','
                });


                json_ajax('post', '/clbs/adas/s/riskManage/intelligence/getRisks', 'json', true, {
                    pageNum: activePageNum,
                    pageSize: 20,
                    riskIds: riskIds,
                    enterpriseFlag: true
                }, function (res) {
                    var html = '';
                    if (res.success && res.obj) {
                        riskEventListOriginal = riskEventListOriginal.concat(res.obj);

                        riskEventList = riskEventList.concat(res.obj)

                        securityEnterprise.renderRiskTable();
                    }
                    setTimeout(function () {
                        ifLoadPageData = true;
                    }, 100)
                })
            }
        },
        /**
         * 督办策略
         * @param ids
         */
        overseeRisk: function (ids, e) {
            window.event ? window.event.cancelBubble = true : e.stopPropagation();

            $('#riskIds').val(ids)
            $('#overSeeModal').modal('show')
        },
        confirmOverSee: function () {
            if (!securityEnterprise.validates()) {
                return;
            }
            $("#overSeeForm").ajaxSubmit(function (data) {
                var data = JSON.parse(data)
                if (data.success) {
                    layer.msg(securityEnterprise.resoveMsg(data.obj))
                    $("#overSeeModal").modal("hide");
                    securityEnterprise.getRiskCompanyData();
                } else {
                    layer.msg(data.msg);
                }
            });
        },
        /**
         * 批量督办
         */
        batchOverSee: function () {
            var subChk = $('#dataTable input[name=subChk]:checked');
            if (subChk.length <= 0) {
                layer.msg('请至少勾选一项');
                return;
            }
            var ids = ''
            $(subChk).each(function (ind, ele) {
                ids += $(ele).attr('riskId') + ','
            })
            securityEnterprise.overseeRisk(ids)
        },
        checkAll: function () {
            var check = $(this).prop('checked');
            $('#dataTable input[name="subChk"]:not([disabled])').prop('checked', check)
        },
        resoveMsg: function (obj) {
            if (obj == '1') {
                return '督办成功';
            }
            if (obj == '0') {
                return '该风险正在被督办';
            }
            if (obj == '2') {
                return '该风险已经被督办了';
            }
        },
        validates: function () {
            return $("#overSeeForm").validate({
                rules: {
                    overseePeople: {
                        required: true,
                    },
                    validResponseTime: {
                        required: true,
                        digits: true,
                        min: 1
                    },
                    overseePhone: {
                        isMobile: true
                    }
                },
                messages: {
                    overseePeople: {
                        required: '请输入督办人',
                    },
                    validResponseTime: {
                        required: '请输入应答时限',
                        digits: '请输入正整数',
                        min: '请输入正整数'
                    },
                    overseePhone: {
                        isMobile: '请输入正确格式手机号'
                    }
                }
            }).form();
        },
        heatMapZoomChange: function (zoom) {
            if (codeLevel && codeLevel == '1') {
                if (zoom >= 8) {
                    var code = amapHot.returnDistrictAdcode();
                    // var data = amapHot.searchDistrictAdcode(code);
                    var _data = code.join(',');
                    securityEnterprise.drwaHeapMap(_data);
                } else {
                    var data = amapHot.returnCityAdcode();
                    var _data = data.join(',');
                    securityEnterprise.drwaHeapMap(_data);
                }
            }
        },
        initAmapHot: function () {
            json_ajax('post', '/clbs/s/riskManage/intelligence/regional/translate', 'json', true, {}, function (data) {
                if (data.success) {
                    codeLevel = data.obj.codeLevel;
                    userAreaCode = data.obj.userAreaCode;

                    var _data;
                    if (codeLevel == '1') { //省级
                        var data = amapHot.returnCityAdcode();
                        _data = data.join(',');
                    }
                    if (codeLevel == '2') { //市级
                        var data = amapHot.searchDistrictAdcode(userAreaCode);
                        _data = data.join(',');

                        amapHot.setMapZoom(7);
                    }
                    if (codeLevel == '3') { //区级
                        _data = userAreaCode;

                        amapHot.setMapZoom(9);

                    }

                    securityEnterprise.drwaHeapMap(_data);
                }
            })
        },
        /**
         * 获取数据并绘制地图
         */
        drwaHeapMap: function (_data) {
            json_ajax('post', '/clbs/s/riskManage/intelligence/regional/list', 'json', true, {
                areaCodes: _data
            }, function (res) {
                if (res.success) {
                    var data = res.obj;
                    var adcodeCityObj = [];

                    for (var i = 0; i < data.length; i++) {
                        var num = data[i].total;
                        adcodeCityObj.push({
                            adcode: data[i].areaCode,
                            num: num,
                        });
                    }

                    amapHot.districtSearch(adcodeCityObj, 'city');
                }
            })
        },
        initChart: function () {
            securityEnterprise.getAlarmChartData();
            // securityEnterprise.getRiskAlarmBarData();
            securityEnterprise.getTimeAnalysisData();
        },
        /**
         * 获取时段分析图数据
         */
        getTimeAnalysisData: function () {
            json_ajax('post', '/clbs/adas/s/riskManage/intelligence/alarmTimes', 'json', true, {}, function (data) {
                if (data.success) {
                    var chartDataArr = data.obj;

                    var hours = [];
                    var data = [];
                    var tiredArr = [];
                    var distractionArr = [];
                    var exceptionArr = [];
                    var crashArr = [];
                    var acuteArr = [];

                    chartDataArr.forEach(function (val, index) {
                        // console.log('时间段', val.time);
                        hours.push(val.time);
                        var orgRiskList = val.orgRiskList;

                        tiredArr.push([0, index, orgRiskList.tired]);
                        distractionArr.push([1, index, orgRiskList.distraction]);
                        crashArr.push([2, index, orgRiskList.crash]);
                        exceptionArr.push([3, index, orgRiskList.exception]);
                        acuteArr.push([4, index, orgRiskList.acute]);

                    });
                    data = tiredArr.concat(distractionArr, crashArr, exceptionArr, acuteArr);

                    securityEnterprise.drawTimeAnalysisChart(hours, data, chartDataArr)
                }
            })
        },
        drawTimeAnalysisChart: function (hours, data, chartDataArr) {
            var days = ['疑似疲劳', '注意力分散', '碰撞危险', '违规异常', '激烈驾驶'];
            var colorArr = ['#ebcd88', '#85dddd', '#e7968e', '#ed8bcc', '#9dbfe3'];
            var total = 0;
            for (var j = 0; j < chartDataArr.length; j++) {
                var _alarmTotal = 0,
                    _obj = chartDataArr[j].orgRiskList;

                for (var k in _obj) {
                    _alarmTotal += _obj[k];
                }
                total += _alarmTotal;
            }

            var option = {
                tooltip: {
                    position: 'top',
                    formatter: function (params) {
                        var dataIndex = params.dataIndex;
                        var nowTime = chartDataArr[dataIndex].timeStr;
                        var alarmTotal = 0,
                            obj = chartDataArr[dataIndex].orgRiskList;

                        for (var i in obj) {
                            alarmTotal += obj[i];
                        }

                        var chartDataItem = chartDataArr[dataIndex].orgRiskList;
                        var tiredNum = chartDataItem.tired;
                        var crashNum = chartDataItem.crash;
                        var distractionNum = chartDataItem.distraction;
                        var exceptionNum = chartDataItem.exception;
                        var acuteNum = chartDataItem.acute;

                        var relVal = '';
                        relVal += nowTime;
                        relVal += '<br/>报警数总数 ' + alarmTotal + ' ' + ((alarmTotal / total) * 100).toFixed(1) + '%';
                        relVal += '<br/>疑似疲劳报警数 ' + tiredNum + ' ' + ((tiredNum / alarmTotal) * 100).toFixed(1) + '%';
                        relVal += '<br/>注意力分散报警数 ' + distractionNum + ' ' + ((distractionNum / alarmTotal) * 100).toFixed(1) + '%';
                        relVal += '<br/>碰撞危险报警数 ' + crashNum + ' ' + ((crashNum / alarmTotal) * 100).toFixed(1) + '%';
                        relVal += '<br/>违规异常报警数 ' + exceptionNum + ' ' + ((exceptionNum / alarmTotal) * 100).toFixed(1) + '%';
                        relVal += '<br/>激烈驾驶报警数 ' + acuteNum + ' ' + ((acuteNum / alarmTotal) * 100).toFixed(1) + '%';

                        return relVal;
                    }
                },
                title: [],
                singleAxis: [],
                series: [],
            };

            echarts.util.each(days, function (day, idx) {
                option.title.push({
                    textBaseline: 'middle',
                    top: (idx + 0.5) * 100 / 5 + '%',
                    text: day
                });
                option.singleAxis.push({
                    left: 150,
                    type: 'category',
                    boundaryGap: false,
                    data: hours,
                    top: (idx * 100 / 5) + '%',
                    height: (100 / 5 - 10) + '%',
                    axisLabel: {
                        interval: 2
                    }
                });
                option.series.push({
                    singleAxisIndex: idx,
                    coordinateSystem: 'singleAxis',
                    type: 'scatter',
                    data: [],
                    symbolSize: function (dataItem) {
                        var num = dataItem[1] < 0 ? 0 : dataItem[1]
                        var size = (num / total) * 100 > 1 ? 1 : (num / total) * 100;
                        return num === 0 ? 0 : (size * 10 + 5);
                    },
                    itemStyle: {
                        color: colorArr[idx]
                    }
                });
            });

            echarts.util.each(data, function (dataItem) {
                option.series[dataItem[0]].data.push([dataItem[1], dataItem[2]]);
            });

            timeChart = echarts.init(document.getElementById('alarmChartScaleBox'));
            timeChart.setOption(option);
        },
        /**
         * 处理返回风险类型名称
         */
        dealRiskName: function (data) {
            switch (data) {
                case 6401:
                    return '前向碰撞';
                    break;
                case 6402:
                    return '车道偏离';
                    break;
                case 6403:
                    return '车距过近';
                    break;
                case 6404:
                    return '行人碰撞';
                    break;
                case 6405:
                    return '频繁变道';
                    break;
                case 6407:
                    return '障碍物';
                    break;
                case 6408:
                    return '急加/急减/急转弯';
                    break;
                case 6409:
                    return '道路标识超限';
                    break;
                case 6502:
                    return '接打手持电话';
                    break;
                case 6503:
                    return '抽烟';
                    break;
                case 6506:
                    return '闭眼';
                    break;
                case 6507:
                    return '打哈欠';
                    break;
                case 6508:
                    return '长时间不目视前方';
                    break;
                case 6509:
                    return '人证不符';
                    break;
                case 6510:
                    return '驾驶员不在驾驶位置';
                    break;
                case 6511:
                    return '遮挡';
                    break;
                case 6512:
                    return '红外阻断';
                    break;
                case 64081:
                    return '急加速';
                    break;
                case 64082:
                    return '急减速';
                    break;
                case 64083:
                    return '急转弯';
                    break;
                case 64021:
                    return '车道左偏离';
                    break;
                case 64022:
                    return '车道右偏离';
                    break;
                case -1:
                    return '暂无数据';
                    break;
                default:
                    return '不知名';
                // break;
            }
        },
        /**
         * 风险分析右边饼状图
         */
        drawRiskAnalyzePieChart: function (crash, distraction, exception, tired) {

            var option = {
                tooltip: {
                    trigger: 'item',
                    formatter: "{b} : {c} ({d}%)"
                },
                legend: {
                    type: 'plain',
                    orient: 'horizontal',
                    bottom: '15%',
                    data: [{
                        name: '疑似疲劳'
                    },
                        {
                            name: '注意力分散'
                        }, '',
                        {
                            name: '碰撞危险'
                        },
                        {
                            name: '违规异常'
                        }
                    ]
                },
                series: [{
                    name: '姓名',
                    type: 'pie',
                    radius: '70%',
                    center: ['50%', '40%'],
                    label: {
                        normal: {
                            position: 'inner',
                            formatter: '{d}%'
                        }
                    }, //扇形区域内显示文字
                    data: [{
                        value: tired,
                        name: '疑似疲劳'
                    },
                        {
                            value: distraction,
                            name: '注意力分散'
                        },
                        {
                            value: crash,
                            name: '碰撞危险'
                        },
                        {
                            value: exception,
                            name: '违规异常'
                        }
                    ],
                    itemStyle: {
                        emphasis: {
                            shadowBlur: 10,
                            shadowOffsetX: 0,
                            shadowColor: 'rgba(0, 0, 0, 0.5)'
                        }
                    }
                }]
            };

            riskAnalyzePieChart = echarts.init(document.getElementById('riskAnalyzeChartRight'));
            riskAnalyzePieChart.setOption(option);
        },
        /**
         * 风险分析柱状图
         */
        drawAlarmChartBar: function (data) {
            var xAxisData = data.map(function (val) {
                return val.eventType;
            })
            var seriesData = data.map(function (val) {
                return val.value;
            })

            var option = {
                backgroundColor: 'rgba(128, 128, 128, 0)',
                // title: {
                //     text: '报警类型排行',
                //     textStyle: {
                //         fontSize: 16,
                //         fontWeight: 'normal',
                //         color: '#767676'
                //     },
                //     left: 'left',
                //     top: '0px'
                // },
                grid: {
                    left: '25px',
                    right: '20px',
                    bottom: '3%',
                    top: '50px',
                    containLabel: true
                },
                xAxis: {
                    type: 'category',
                    data: xAxisData,
                    axisLabel: { //坐标轴刻度标签
                        show: true,
                        interval: 0,
                        rotate: 30,
                        textStyle: {
                            color: "#2f2f2f",
                            fontSize: 12,
                            fontWeight: 'lighter'
                        }
                    },
                    axisLine: { //坐标轴轴线
                        show: false
                    },
                    axisTick: { //坐标轴刻度
                        show: false
                    }
                },
                yAxis: {
                    type: 'value',
                    position: 'left',
                    offset: 50,
                    show: false
                },
                series: [{
                    data: seriesData,
                    type: 'bar',
                    barMaxWidth: 60, //最大宽度
                    itemStyle: {
                        normal: {
                            color: '#198ef0',
                        }
                    },
                    label: {
                        normal: {
                            show: true,
                            rotate: 30,
                            position: [0, -10],
                        }
                    }
                }]
            };


            alarmChartBar = echarts.init(document.getElementById('alarmChartBarBox'));
            alarmChartBar.setOption(option);
        },
        /**
         * 获取报警分析数据
         */
        getAlarmChartData: function () {
            json_ajax('post', '/clbs/adas/s/riskManage/intelligence/alarmAnalysis', 'json', true, {}, function (data) {
                if (data.success) {
                    var obj = data.obj;

                    var dealed = obj.dealed,
                        undeal = obj.undeal,
                        total = obj.total,
                        riskDataList = obj.riskDataList;

                    //当前报警事件数
                    var html = '';
                    var arr = total.toString().split('');
                    arr.forEach(function (item) {
                        html += '<span class="num">' + item + '</span>';
                    });
                    html += '<span>件</span>';
                    $('#nowRiskNum').html(html);

                    securityEnterprise.drawAlarmAnalyzePieChart(dealed, undeal);
                    securityEnterprise.drawAlarmAnalyzeBarChart(riskDataList, total)
                }
            })
        },
        /**
         * 报警分析左边饼状图
         */
        drawAlarmAnalyzePieChart: function (dealed, undeal) {

            var option = {
                tooltip: {
                    trigger: 'item',
                    formatter: "{b} : {c} ({d}%)"
                },
                legend: {
                    type: 'plain',
                    orient: 'horizontal',
                    bottom: 10,
                    data: [{
                        name: '未处理'
                    },
                        {
                            name: '已处理'
                        },
                    ]
                },
                series: [{
                    name: '姓名',
                    type: 'pie',
                    radius: '55%',
                    center: ['50%', '50%'],
                    label: {
                        normal: {
                            position: 'outside',
                            formatter: '{d}%'
                        }
                    }, //扇形区域内显示文字
                    data: [{
                        value: undeal,
                        name: '未处理',
                        itemStyle: {
                            color: '#9dbfe3'
                        }
                    },
                        {
                            value: dealed,
                            name: '已处理',
                            itemStyle: {
                                color: '#efa99e'
                            }
                        },
                    ],
                    itemStyle: {
                        emphasis: {
                            shadowBlur: 10,
                            shadowOffsetX: 0,
                            shadowColor: 'rgba(0, 0, 0, 0.5)'
                        }
                    }
                }]
            };

            alarmAnalyzePieChart = echarts.init(document.getElementById('alarmAnalyzePieChart'));
            alarmAnalyzePieChart.setOption(option);
        },
        /**
         * 报警分析柱状图
         */
        drawAlarmAnalyzeBarChart: function (riskDataList, total) {
            var crash = {},
                exception = {},
                distraction = {},
                tired = {},
                acute = {};

            riskDataList.forEach(function (val) {
                if (val.riskType == 'crash') {
                    crash = val;
                }
                if (val.riskType == 'exception') {
                    exception = val;
                }
                if (val.riskType == 'distraction') {
                    distraction = val;
                }
                if (val.riskType == 'tired') {
                    tired = val;
                }
                if (val.riskType == 'acute') {
                    acute = val;
                }
            })

            // 指定图表的配置项和数据
            var option = {
                tooltip: {
                    trigger: "axis",
                    formatter: function (val) {
                        var relVal = '';
                        var singleTotal;
                        val.forEach(function (value) {
                            if (value.seriesName == '报警数') {
                                singleTotal = value.value;
                                var percent;
                                if (singleTotal == '0') {
                                    percent = 0
                                } else {
                                    percent = ((singleTotal / total) * 100).toFixed(2)
                                }

                                relVal += '报警数&nbsp;&nbsp;' + singleTotal + ' ' + percent + '%'
                            }
                        })

                        val.forEach(function (value) {

                            if (value.seriesName != '报警数') {

                                var perecnt = singleTotal != 0 ? ((value.value / singleTotal) * 100).toFixed(2) + '%' : '0%'

                                relVal += '<br/>' + value.seriesName + '数  ' + value.value + ' ' + perecnt
                            }
                        })

                        return relVal;
                    }
                },
                legend: {
                    type: 'plain',
                    orient: 'horizontal',
                    selectedMode: false,
                    bottom: 10,
                    data: [{
                        name: '未处理'
                    },
                        {
                            name: '已处理'
                        },
                    ]
                },
                grid: {
                    containLabel: true,
                    bottom: 100,
                },
                xAxis: {
                    data: ["疑似疲劳", "碰撞危险", "注意力分散", "违规异常", "激烈驾驶"],
                    axisLabel: { //坐标轴刻度标签
                        show: true,
                        interval: 0,
                    },
                },
                yAxis: {
                    yAxis: {
                        type: 'value',
                    },
                },
                series: [{
                    name: '未处理',
                    type: 'bar',
                    stack: '使用情况',
                    data: [tired.undeal, crash.undeal, distraction.undeal, exception.undeal, acute.undeal],
                    itemStyle: {
                        color: '#9dbfe3'
                    }
                }, {
                    name: '已处理',
                    type: 'bar',
                    stack: '使用情况',
                    data: [tired.dealed, crash.dealed, distraction.dealed, exception.dealed, acute.dealed],
                    itemStyle: {
                        color: '#efa99e'
                    }
                }, {
                    name: '报警数',
                    type: 'bar',
                    stack: '使用情况',
                    label: {
                        normal: {
                            show: true,
                            position: 'insideBottom',
                            formatter: '{c}',
                            textStyle: {
                                color: '#000'
                            }
                        }
                    },
                    itemStyle: {
                        normal: {
                            color: 'rgba(128, 128, 128, 0)'
                        }
                    },
                    data: [tired.total, crash.total, distraction.total, exception.total, acute.total]
                }]
            };
            alarmAnalyzeBarChart = echarts.init(document.getElementById('alarmAnalyzeBarChart'));
            alarmAnalyzeBarChart.setOption(option);
        },
        /**
         * 图表resize
         */
        chartResize: function () {
            alarmAnalyzePieChart && alarmAnalyzePieChart.resize();
            alarmAnalyzeBarChart && alarmAnalyzeBarChart.resize();
            alarmChartBar && alarmChartBar.resize();
            riskAnalyzePieChart && riskAnalyzePieChart.resize();
            timeChart && timeChart.resize();
        },
        initMap: function () {
            // 创建地图
            map = new AMap.Map('map', {
                zoom: 16, //级别
                // center: [116.397428, 39.90923],//中心点坐标
                resizeEnable: true,
            });

            // securityEnterprise.clusterToCreateMarker()

            // map.setCenter([106.519778,29.539587]);

            // 地图移动或缩放后重新获取地图当前显示区域
            securityEnterprise.getMapArea()
            map.on('moveend', securityEnterprise.getMapArea);
            map.on('zoomchange', securityEnterprise.mapZoomChange);
            map.on('touchend', securityEnterprise.getMapArea);

            infoWindow = new AMap.InfoWindow({
                offset: new AMap.Pixel(0, -10),
                closeWhenClickMap: true,
                autoMove: false
            });
            infoWindow.on('close', function () {
                infoWindowOpenState = false;
            });

            // oldVehicleID = "6c60ff8e-a068-4223-88b4-949f4c02faa3"
            // var vehicleInfo = ["6c60ff8e-a068-4223-88b4-949f4c02faa3","测12345",106.519778,29.539587,225,2,"v_21.png",'2018-02-26','从业人员','里程','终端号','18323254849','10km/h'];
            // securityEnterprise.updateVehicleMap(vehicleInfo);
            // /
        },
        // 监控对象状态信息订阅
        vehicleUpdateStatus: function (id) {
            isCarSubscribe = true;

            var userName = $("#userName").text();
            var requestStrS = {
                "desc": {
                    "MsgId": 40964,
                    "UserName": userName
                },
                "data": [{
                    vehicleID: id
                }]
            };
            webSocket.subscribe(headers, '/user/' + userName + '/cachestatus', securityEnterprise.updateStatusCB, "/app/vehicle/subscribeCacheStatusNew", requestStrS);

        },
        updateStatusCB: function (msg) {
            var dataInfo = JSON.parse(msg.body);
            var data = dataInfo.data[0];
            if (!data || data.length == 0) {
                return;
            }
            // console.log('状态订阅数据', dataInfo);

            var vehicleId = data.vehicleId;
            var brand = data.brand;
            var vehicleStatus = data.vehicleStatus;

            if (monitoringObjLngLatMap.containsKey(vehicleId)) {
                var markerInfo = monitoringObjLngLatMap.get(vehicleId);
                var lngLat = markerInfo[0][0];

                //更新信息弹窗状态
                if (vehicleStatus) {
                    mapWindowInfo[5] = vehicleStatus;
                    securityEnterprise.updateInfowindow(mapWindowInfo);
                }

                //更新车牌状态
                securityEnterprise.carNameEvade(vehicleId, brand, lngLat, markerInfo[0][2], vehicleStatus, false);
            }
        },
        // 监控对象位置信息订阅
        vehicleLoctionFun: function (id) {
            isCarSubscribe = true;

            var userName = $("#userName").text();
            var requestStrS = {
                "desc": {
                    "MsgId": 40964,
                    "UserName": userName
                },
                "data": [id]
            };
            webSocket.subscribe(headers, '/user/topic/location', securityEnterprise.updateRealLocation, "/app/location/subscribe", requestStrS);

        },
        // 监控对象订阅实时信息
        updateRealLocation: function (msg) {
            var data = JSON.parse(msg.body);
            protocol = data.desc.protocol;

            if (data.desc !== "neverOnline") {
                if (data.desc.msgID === 513) {
                    var obj = {};
                    obj.desc = data.desc;

                    var da = {};
                    da.msgHead = data.data.msgHead;
                    da.msgBody = data.data.msgBody.gpsInfo;
                    obj.data = da;
                    // 状态信息
                    securityEnterprise.updateVehicleMap(obj);
                } else {
                    var cid = data.data.msgBody.monitorInfo.monitorId;
                    securityEnterprise.updateVehicleMap(data);
                }
            } else {
                if (monitoringObjMap.containsKey(data.vid)) {
                    monitoringObjMap.remove(data.vid);
                }
                monitoringObjMap.put(data.vid, null);
            }
        },
        // 更新地图信息
        updateVehicleMap: function (data) {
            // console.log('车辆位置信息更新', data);

            var msgBody = data.data.msgBody;
            var msgDesc = data.desc;
            var monitorInfo = data.data.msgBody.monitorInfo;
            var vehicleName = monitorInfo.monitorName; // 监控对象名称
            var time = securityEnterprise.timeTransform(msgBody.gpsTime.toString()); // 定位时间
            var deviceNo = monitorInfo.deviceNumber; // 终端号
            var simNo = monitorInfo.simcardNumber; // 终端手机号
            var driverName = (monitorInfo.professionalsName == '' || monitorInfo.professionalsName == 'null' || monitorInfo.professionalsName == null || monitorInfo.professionalsName == undefined ? '-' : monitorInfo.professionalsName); // 从业人员
            var speed = msgBody.gpsSpeed; // 速度
            var formattedAddress = ((msgBody.positionDescription == '' || msgBody.positionDescription == null || msgBody.positionDescription == 'null') ? '未定位' : msgBody.positionDescription); // 位置信息

            var this_longitude = msgBody.longitude; // 经度
            var this_latitude = msgBody.latitude; // 纬度
            var vehicleIcon = monitorInfo.monitorIcon; // 车辆图标
            var vehicleId = monitorInfo.monitorId; // 监控对象ID
            var stateInfo = msgBody.stateInfo; // 车辆状态
            var distance = msgBody.gpsMileage; // 里程

            // 更新信息
            securityEnterprise.updateMapInfo(vehicleId, vehicleName, this_longitude, this_latitude, vehicleIcon, stateInfo, distance, time);

            // 信息弹窗
            var info = []; // 信息弹窗信息
            info.push(vehicleId); // 监控对象ID
            info.push(time);
            info.push(vehicleName); // 监控对象名称
            info.push(deviceNo); // 终端号
            info.push(simNo); // 终端手机号
            info.push(stateInfo); // 车辆状态
            info.push(speed); // 行驶速度
            info.push(formattedAddress); // 位置信息
            mapWindowInfo = info;

            securityEnterprise.updateInfowindow(info);
        },
        // 更新信息窗口
        updateInfowindow: function (info) {
            infoContent = [];
            infoContent.push('时间：' + info[1]);
            infoContent.push('监控对象：' + info[2]);
            infoContent.push('终端号：' + info[3]);
            infoContent.push('终端手机号：' + info[4]);
            infoContent.push('行驶状态：' + securityEnterprise.stateNameCallBack(info[5]));
            infoContent.push('行驶速度：' + info[6] + 'km/h');
            infoContent.push('位置：' + info[7]);
            infoContent = infoContent.join('<br/>');
            var id = info[0];
            if (infoWindowOpenState) { // 判断信息窗是否开启
                if (monitoringObjMap.containsKey(id)) {
                    var marker = monitoringObjMap.get(id);
                    var position = marker.getPosition();
                    infoWindow.setContent(infoContent);
                    infoWindow.open(map, position);
                }
            }
        },
        // 更新地图位置信息
        updateMapInfo: function (id, name, longitude, latitude, icon, stateInfo, distance, time) {
            var lngLat = [longitude, latitude];
            var localtionInfo = [distance, time];
            // 车辆经纬度集合
            if (monitoringObjLngLatMap.containsKey(id)) {
                var value = monitoringObjLngLatMap.get(id);
                monitoringObjLngLatMap.remove(id);
                value.push([lngLat, localtionInfo, icon]);
                monitoringObjLngLatMap.put(id, value);
            } else {
                monitoringObjLngLatMap.put(id, [
                    [lngLat, localtionInfo, icon]
                ]);
            }
            // 图标位置更新
            if (monitoringObjMap.containsKey(id)) { // 判断是否已经有该ID的车辆marker
                /*经纬度为0判断 测试 start*/
                if (oldVehicleID === id && (longitude != 0 && latitude != 0)) {
                    securityEnterprise.markerMove(id, name, lngLat, icon, stateInfo);
                } else {
                    return;
                }
                /*经纬度为0判断 测试 end*/
            } else {
                securityEnterprise.carNameEvade(id, name, lngLat, icon, stateInfo, true);
            }
        },
        // 地图层级改变触发事件
        mapZoomChange: function () {
            if (oldVehicleID !== undefined) {
                var marker = monitoringObjMap.get(oldVehicleID);
                // console.log('地图数据', marker);
                if (!!marker) {
                    var id = marker.id,
                        name = marker.name,
                        lngLat = marker.getPosition(),
                        icon = marker.icon,
                        stateInfo = marker.stateInfo;
                    securityEnterprise.carNameEvade(id, name, lngLat, icon, stateInfo, false);

                    //更新信息弹框状态
                    if (marker.stateInfo) {
                        mapWindowInfo[5] = marker.stateInfo;
                        securityEnterprise.updateInfowindow(mapWindowInfo);
                    }
                }
                securityEnterprise.getMapArea();
            }
        },
        // marker 移动
        markerMove: function (id, name, lngLat, icon, stateInfo) {
            var value = monitoringObjLngLatMap.get(id);
            if (value.length == 2) {
                var position = [value[0][0], value[1][0]];
                var startInfo = value[0][1];
                var endInfo = value[1][1];
                var mileage = Number(endInfo[0]) - Number(startInfo[0]);
                var startTime = new Date(securityEnterprise.timeTransform(startInfo[1]).replace('-', '/')).getTime();
                var endTime = new Date(securityEnterprise.timeTransform(endInfo[1]).replace('-', '/')).getTime();
                var time = (endTime - startTime) / 1000 / 60 / 60;
                var marker = monitoringObjMap.get(id);
                var speed;
                if (time == 0 || mileage == 0) {
                    speed = 80;
                } else {
                    speed = mileage / time;
                }

                marker.moveTo(position[1], speed);
                // marker 移动过程监听事件
                marker.on('moving', function (info) {
                    securityEnterprise.markerMoving(info, id, name, icon, stateInfo)
                });
                // marker 移动结束事件
                marker.on('moveend', function (info) {
                    securityEnterprise.markerMoveend(id)
                });
            }
        },
        // 移动过程监听事件
        markerMoving: function (info, id, name, icon, stateInfo) {
            var movelnglat = [info.passedPath[1].lng, info.passedPath[1].lat];
            // 判断当前点是否在地图可以范围内
            if (!mapBounds.contains(movelnglat)) {
                map.setCenter(movelnglat);
                securityEnterprise.getMapArea();
            }
            securityEnterprise.carNameEvade(id, name, movelnglat, icon, stateInfo, false);
        },
        // 移动结束事件
        markerMoveend: function (id) {
            var value = monitoringObjLngLatMap.get(id);
            monitoringObjLngLatMap.remove(id);
            value = value.splice(0, 1);
            monitoringObjLngLatMap.put(id, value);
            if (value.length >= 2) {
                securityEnterprise.moveendBeforeCallBack(id);
            }
        },
        // 监控对象未走完，数据先上来
        moveendBeforeCallBack: function (id) {
            var value = monitoringObjLngLatMap.get(id);
            var position = [value[0][0], value[1][0]];
            var startInfo = value[0][1];
            var endInfo = value[1][1];
            var mileage = Number(endInfo[0]) - Number(startInfo[0]);
            var startTime = new Date(securityEnterprise.timeTransform(startInfo[1]).replace('-', '/')).getTime()
            var endTime = new Date(securityEnterprise.timeTransform(endInfo[1]).replace('-', '/')).getTime();
            var time = (endTime - startTime) / 1000 / 60 / 60;
            var marker = monitoringObjMap.get(id);
            var speed;
            if (time == 0 || mileage == 0) {
                speed = 80;
            } else {
                speed = mileage / time;
            }
            marker.moveTo(position, speed);
            // marker 移动过程监听事件
            marker.on('moving', function (info) {
                securityEnterprise.markerMoving(info, id, name, icon, stateInfo)
            });
            // marker 移动结束事件
            marker.on('moveend', function (info) {
                securityEnterprise.markerMoveend(id)
            });
        },
        // 获取map可视区域范围
        getMapArea: function () {
            var southWest = map.getBounds().getSouthWest(); // 西南角
            var northEast = map.getBounds().getNorthEast(); // 东北角
            var neLng = northEast.lng - ((northEast.lng - southWest.lng) * 0.2); // 东北角经度
            var neLat = northEast.lat - ((northEast.lat - southWest.lat) * 0.2); // 东北角纬度
            var swLng = southWest.lng + ((northEast.lng - southWest.lng) * 0.2); // 西南角经度
            var swLat = southWest.lat + ((northEast.lat - southWest.lat) * 0.2); // 西南角纬度
            var southWestValue = [swLng, swLat];
            var northEastValue = [neLng, neLat];
            mapBounds = new AMap.Bounds(southWestValue, northEastValue);
        },
        // 时间转换
        timeTransform: function (sysTime) {
            var serviceSystemTime;
            if (sysTime.length === 12) {
                serviceSystemTime = 20 + sysTime.substring(0, 2) + "-" + sysTime.substring(2, 4) + "-" + sysTime.substring(4, 6) + " " +
                    sysTime.substring(6, 8) + ":" + sysTime.substring(8, 10) + ":" + sysTime.substring(10, 12);
            } else if (sysTime.length === 13) {
                //毫秒级时间戳转换
                var dateTime = new Date(parseInt(sysTime));
                serviceSystemTime = dateTime.getFullYear() + "-" +
                    ((dateTime.getMonth() + 1) < 10 ? "0" + (dateTime.getMonth() + 1) : (dateTime.getMonth() + 1)) +
                    "-" + (dateTime.getDate() < 10 ? "0" + dateTime.getDate() : dateTime.getDate()) +
                    " " +
                    (dateTime.getHours() < 10 ? "0" + dateTime.getHours() : dateTime.getHours()) + ":" +
                    (dateTime.getMinutes() < 10 ? "0" + dateTime.getMinutes() : dateTime.getMinutes()) + ":" +
                    dateTime.getSeconds();
            } else {
                serviceSystemTime = sysTime;
            }

            return serviceSystemTime;
        },
        /**
         * 创建map集合
         */
        createMap: function () {

            markerMap = new securityEnterprise.mapVehicle();
            carNameMarkerMap = new securityEnterprise.mapVehicle();
            carNameMarkerContentMap = new securityEnterprise.mapVehicle();
            carNameContentLUMap = new securityEnterprise.mapVehicle();

            markerViewingArea = new securityEnterprise.mapVehicle();
            markerOutside = new securityEnterprise.mapVehicle();
            markerAllUpdateData = new securityEnterprise.mapVehicle();

            monitoringObjLngLatMap = new securityEnterprise.mapVehicle();
            monitoringObjMap = new securityEnterprise.mapVehicle();
            monitoringObjNameMap = new securityEnterprise.mapVehicle();
        },
        // 封装map集合
        mapVehicle: function () {

            this.elements = {};
            //获取MAP元素个数
            this.size = function () {
                return Object.keys(this.elements).length;
            };
            //判断MAP是否为空
            this.isEmpty = function () {
                return (Object.keys(this.elements).length < 1);
            };
            //删除MAP所有元素
            this.clear = function () {
                this.elements = {};
            };
            //向MAP中增加元素（key, value)
            this.put = function (_key, _value) {
                this.elements[_key] = _value;
            };
            //删除指定KEY的元素，成功返回True，失败返回False
            this.remove = function (_key) {
                delete this.elements[_key];
            };
            //获取指定KEY的元素值VALUE，失败返回NULL
            this.get = function (_key) {
                return this.elements[_key];
            };
            //获取指定索引的元素（使用element.key，element.value获取KEY和VALUE），失败返回NULL
            this.element = function (_index) {
                var keys = Object.keys(this.elements);
                var key = keys[_index];
                return this.elements[key];
            };
            //判断MAP中是否含有指定KEY的元素
            this.containsKey = function (_key) {
                if (this.elements[_key]) {
                    return true;
                }
                return false;

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
                var keys = Object.keys(this.elements);
                for (var i = 0, len = keys.length; i < len; i++) {
                    arr.push(this.elements[keys[i]]);
                }
                return arr;
            };
            //获取MAP中所有KEY的数组（ARRAY）
            this.keys = function () {
                return Object.keys(this.elements);
            };

        },
        /**
         * 车牌号规避
         * @param
         */
        // 车牌避让
        carNameEvade: function (id, name, lngLat, icon, stateInfo, isFirstCreate) {
            var value = lngLat; // 经纬度
            var picWidth; // 图片宽度
            var picHeight; // 图片高度
            var icons; // 图片marker路劲

            //车牌名称过多省略
            if (name.length > 8) {
                name = name.substring(0, 7) + '...';
            }
            var num = 0;
            for (var i = 0; i < name.length; i++) { //判断车牌号含有汉字数量
                if (name[i].match(/^[\u4E00-\u9FA5]{1,}$/)) {
                    num++;
                }
            }
            if (num > 3) {
                name = name.substring(0, 4) + '...';
            }

            if (icon === "null" || icon === undefined || icon == null) {
                icons = "/clbs/resources/img/vehicle.png";
            } else {
                icons = "/clbs/resources/img/vico/" + icon;
            }

            picWidth = 58 / 2;
            picHeight = 26 / 2;

            // 显示对象姓名区域大小
            var nameAreaWidth = 90;
            var nameAreaHeight = 38;

            // 车辆状态判断
            var carState = securityEnterprise.stateCallBack(stateInfo);

            // 监控对象旋转角度
            var markerAngle = 0; //图标旋转角度
            if (monitoringObjMap.containsKey(id)) {
                var thisCarMarker = monitoringObjMap.get(id);
                markerAngle = thisCarMarker.getAngle();
                if (markerAngle > 360) {
                    var i = Math.floor(markerAngle / 360);
                    markerAngle = markerAngle - 360 * i;
                }
            }
            // 将经纬度转为像素
            var pixel = map.lngLatToContainer(value);
            var pixelX = pixel.getX();
            var pixelY = pixel.getY();
            var pixelPX = [pixelX, pixelY];

            // 得到车辆图标四个角的像素点(假设车图标永远正显示)58*26
            var defaultLU = [pixelX - picWidth, pixelY - picHeight]; //左上
            var defaultRU = [pixelX + picWidth, pixelY - picHeight]; //右上
            var defaultLD = [pixelX - picWidth, pixelY + picHeight]; //左下
            var defaultRD = [pixelX + picWidth, pixelY + picHeight]; //右下

            // 计算后PX
            var pixelRD = securityEnterprise.countAnglePX(markerAngle, defaultRD, pixelPX, 1, picWidth, picHeight);
            var pixelRU = securityEnterprise.countAnglePX(markerAngle, defaultRU, pixelPX, 2, picWidth, picHeight);
            var pixelLU = securityEnterprise.countAnglePX(markerAngle, defaultLU, pixelPX, 3, picWidth, picHeight);
            var pixelLD = securityEnterprise.countAnglePX(markerAngle, defaultLD, pixelPX, 4, picWidth, picHeight);

            // 四点像素转为经纬度
            var llLU = map.containTolnglat(new AMap.Pixel(pixelLU[0], pixelLU[1]));
            var llRU = map.containTolnglat(new AMap.Pixel(pixelRU[0], pixelRU[1]));
            var llLD = map.containTolnglat(new AMap.Pixel(pixelLD[0], pixelLD[1]));
            var llRD = map.containTolnglat(new AMap.Pixel(pixelRD[0], pixelRD[1]));

            // 车牌显示位置左上角PX
            var nameRD_LU = [pixelRD[0], pixelRD[1]];
            var nameRU_LU = [pixelRU[0], pixelRU[1] - nameAreaHeight];
            var nameLU_LU = [pixelLU[0] - nameAreaWidth, pixelLU[1] - nameAreaHeight];
            var nameLD_LU = [pixelLD[0] - nameAreaWidth, pixelLD[1]];

            // 分别将上面四点转为经纬度
            var llNameRD_LU = map.containTolnglat(new AMap.Pixel(nameRD_LU[0], nameRD_LU[1]));
            var llNameRU_LU = map.containTolnglat(new AMap.Pixel(nameRU_LU[0], nameRU_LU[1]));
            var llNameLU_LU = map.containTolnglat(new AMap.Pixel(nameLU_LU[0], nameLU_LU[1]));
            var llNameLD_LU = map.containTolnglat(new AMap.Pixel(nameLD_LU[0], nameLD_LU[1]));

            //判断车牌号该显示的区域
            var mapPixel = llRD;
            var LUPX = llNameRD_LU;
            var showLocation = 'carNameShowRD';
            if (isFirstCreate) { // 第一次创建对应ID marker

                // 创建marker
                var marker = new AMap.Marker({
                    position: value,
                    icon: icons,
                    offset: new AMap.Pixel(-picWidth, -picHeight), //相对于基点的位置
                    autoRotation: true, //自动调节图片角度
                    map: map
                });
                // id, name, lngLat, icon, stateInfo, isFirstCreate
                marker.id = id;
                marker.name = name;
                marker.icon = icon;
                marker.stateInfo = stateInfo;
                if (oldVehicleID !== id) {
                    marker.hide();
                } else {
                    map.setCenter(value);
                }

                // marker 绑定点击事件
                marker.on('click', securityEnterprise.markerClick);

                monitoringObjMap.put(id, marker);

                // 创建车牌号marker
                var carContent = "<p class='" + showLocation + "'><i class='" + carState + "'></i>&nbsp;" + name + "</p>";
                if (monitoringObjNameMap.containsKey(id)) {
                    var nameMarker = monitoringObjNameMap.get(id);
                    map.remove([nameMarker]);
                    monitoringObjNameMap.remove(id);
                }
                var thisNameMarker = new AMap.Marker({
                    position: mapPixel,
                    content: carContent,
                    offset: new AMap.Pixel(0, 0),
                    autoRotation: true, //自动调节图片角度
                    map: map,
                    zIndex: 999
                });
                thisNameMarker.setMap(map);
                monitoringObjNameMap.put(id, thisNameMarker);
            } else { // 不是第一次创建
                var carContentHtml = "<p class='" + showLocation + "'><i class='" + carState + "'></i>&nbsp;" + name + "</p>";
                if (monitoringObjNameMap.containsKey(id)) {
                    var nameMarker = monitoringObjNameMap.get(id);
                    nameMarker.show();
                    nameMarker.setContent(carContentHtml);
                    nameMarker.stateInfo = stateInfo;
                    nameMarker.setPosition(llRD);
                    nameMarker.setOffset(new AMap.Pixel(0, 0));
                    if (infoWindowOpenState) {
                        infoWindow.setPosition(lngLat);
                    }
                }
            }
        },
        //车辆标注点击
        markerClick: function (e) {
            if (infoWindowOpenState) { // 关闭窗口
                infoWindow.close();
                infoWindowOpenState = false;
            } else { // 开启窗口
                infoWindow.setContent(infoContent);
                infoWindow.open(map, e.target.getPosition());
                infoWindowOpenState = true;
            }
        },
        //计算车牌号四个定点的像素坐标
        countAnglePX: function (angle, pixel, centerPX, num, picWidth, picHeight) {
            var thisPX;
            var thisX;
            var thisY;
            if ((angle <= 45 && angle > 0) || (angle > 180 && angle <= 225) || (angle >= 135 && angle < 180) || (angle >= 315 && angle < 360)) {
                angle = 0;
            }
            ;
            if ((angle < 90 && angle > 45) || (angle < 270 && angle > 225) || (angle > 90 && angle < 135) || (angle > 270 && angle < 315)) {
                angle = 90;
            }
            ;
            if (angle == 90 || angle == 270) {
                if (num == 1) {
                    thisX = centerPX[0] + picHeight;
                    thisY = centerPX[1] + picWidth;
                }
                ;
                if (num == 2) {
                    thisX = centerPX[0] + picHeight;
                    thisY = centerPX[1] - picWidth;
                }
                ;
                if (num == 3) {
                    thisX = centerPX[0] - picHeight;
                    thisY = centerPX[1] - picWidth;
                }
                ;
                if (num == 4) {
                    thisX = centerPX[0] - picHeight;
                    thisY = centerPX[1] + picWidth;
                }
                ;
            }
            ;
            if (angle == 0 || angle == 180 || angle == 360) {
                thisX = pixel[0];
                thisY = pixel[1];
            }
            ;
            thisPX = [thisX, thisY];
            return thisPX;
        },
        // 监控对象状态名称返回
        stateNameCallBack: function (stateInfo) {
            var state = '在线';
            switch (stateInfo) {
                case 10:
                case 11:
                case 4:
                case 5:
                case 2:
                case 9:
                    state = '在线';
                    break;
                case 3:
                    state = '离线';
                    break;
            }
            ;
            return state;
        },
        // 监控对象状态返回
        stateCallBack: function (stateInfo) {
            var state;
            switch (stateInfo) {
                case 4:
                    state = 'carStateStop';
                    break;
                case 10:
                    state = 'carStateRun';
                    break;
                case 5:
                    state = 'carStateAlarm';
                    break;
                case 2:
                    state = 'carStateMiss';
                    break;
                case 3:
                    state = 'carStateOffLine';
                    break;
                case 9:
                    state = 'carStateOverSpeed';
                    break;
                case 11:
                    state = 'carStateheartbeat';
                    break;
            }
            ;
            return state;
        },
        getNowFormatDate: function () {
            var date = new Date();
            var seperator1 = "-";
            var seperator2 = ":";
            var month = date.getMonth() + 1;
            var strDate = date.getDate();
            if (month >= 1 && month <= 9) {
                month = "0" + month;
            }
            if (strDate >= 0 && strDate <= 9) {
                strDate = "0" + strDate;
            }
            var currentdate = date.getFullYear() + seperator1 + month + seperator1 + strDate +
                " " + date.getHours() + seperator2 + date.getMinutes() +
                seperator2 + date.getSeconds();
            return currentdate;
        },

        navTabToggle: function (e) {
            var id = e.target.id;
            //点击同一个不触发
            // if (id == 'toAlarmAnalyze') {
            // securityEnterprise.chartResize();
            // }


            // securityEnterprise.initChart();

            if (id == 'toAlarmAnalysis') {
                securityEnterprise.initChart();
                securityEnterprise.chartResize();
            }

            // if (id == 'toAlarmAnalyze') {
            //     securityEnterprise.getAlarmChartData();
            // }
            //
            // if (id == 'toRiskAnalyze') {
            //     securityEnterprise.getRiskAlarmBarData();
            // }
            //
            // if (id == 'toTimeAnalyze') {
            //     securityEnterprise.getRiskTableData()
            //     securityEnterprise.getTimeAnalysisData();
            // }
            //
            // if(id == 'toHeatMap'){
            //
            //     securityEnterprise.initAmapHot();
            //
            // }
        },
        videoBtnClick: function () {
            if (!checkedVehicleInfo) {
                layer.msg('请选择要处理的信息后再点击');
                return;
            }

            if ($(this).hasClass('active')) {
                $(this).removeClass('active');
                securityVideoSeparate.closeTerminalVideo()
            } else {
                if (!securityEnterprise.ifVehicleOnline()) {
                    layer.msg('该车辆不在线');
                    return;
                }
                ;
                securityVideoSeparate.closeTerminalVideo();
                setTimeout(function () {
                    securityVideoSeparate.sendParamByBatch();
                }, 500)
            }
        },
        ifVehicleOnline: function () {
            var result;
            json_ajax('post', '/clbs/v/oilmassmgt/oilcalibration/checkVehicleOnlineStatus', 'json', false, {
                vehicleId: checkedVehicleInfo.vid
            }, function (res) {
                result = res.success;
            })
            return result;
        },
        //TTS读播
        toTextInfoSend: function () {
            if (!checkedVehicleInfo) {
                layer.msg('请选择要处理的信息后再点击');
                return;
            }
            if (!securityEnterprise.ifVehicleOnline()) {
                layer.msg('终端已离线');
                return;
            }
            ;
            $('#textInfoSendBrand').html(checkedVehicleInfo.brand);
            $('#textSendVid').val(checkedVehicleInfo.vid);
            $('#textInfoSend').modal('show');
        },
        goTxtSend: function () {
            if (securityEnterprise.txtSendValidate()) {
                $("#txtSend").ajaxSubmit(function (data) {
                    $("#textInfoSend").modal("hide");
                    if (JSON.parse(data).success) {
                        layer.msg("指令下发成功");
                        // setTimeout("dataTableOperation.logFindCilck()", 500);
                    } else {
                        layer.msg(JSON.parse(data).msg)
                    }
                });
            }
        },
        txtSendValidate: function () {
            return $("#txtSend").validate({
                rules: {
                    sendTextContent: {
                        required: true,
                        maxlength: 512
                    }
                },
                messages: {
                    sendTextContent: {
                        required: "请输入文本内容",
                        maxlength: '最多输入512个字符'
                    }
                }
            }).form();
        },
        riskEventClick: function () {
            securityEnterprise.closeVideoAndCall();
            var id = $(this).attr('riskid'),
                vehicleId = $(this).attr('vehicleId');

            if (!$(this).hasClass('activeTr')) {
                $(this).addClass('activeTr').siblings().removeClass('activeTr');

                $('#riskDetailCont').removeClass('active').addClass('active');
                $('#toMap').trigger('click');
                $('#toRealTimeVideo').trigger('click');

                securityEnterprise.getEventDetail(id, vehicleId);
                securityEnterprise.handleVehicleInfo(vehicleId, id);

                var brand = $(this).find('td').eq(2).html();
                checkedVehicleInfo = {
                    vid: vehicleId,
                    brand: brand
                };

                selectedRiskNumber = $(this).attr('riskNumber');
                selectedRiskId = id;
                selectedRiskWarningTime = $(this).find('td').eq(7).text();
                securityVideoSeparate.initVideoRealTimeShow(checkedVehicleInfo);

                //处理风险部分
                securityEnterprise.initRiskDeal(id)
            } else {

                $(this).removeClass('activeTr');

                $('#riskDetailCont').removeClass('active');

                securityEnterprise.clearMapMarker(vehicleId);
                oldRiskID = null;

                checkedVehicleInfo = null;
            }


        },
        initRiskDeal: function (id) {
            $('#dealRiskIds').val(id);
            $('#dealer').val($('#userName').text());

            $('#riskForm input[type = "radio"][value = "1"]').removeAttr('disabled').prop('checked', true);
            $('#riskForm input[type = "radio"]').removeAttr('disabled');
            $('#riskForm input[type = "radio"][value = "0"][name = "riskResult"]').prop('checked', true);
            $('#otherTxt').val('');


            $('#doSubmits').addClass('focus');

        },
        closeVideoAndCall: function () {
            $('#videoSelect').removeClass('active');
            securityVideoSeparate.closeTerminalVideo();
            $('#callSelect').removeClass('active');
            securityVideoSeparate.callClose();
        },
        /**
         * 清除地图车辆
         */
        clearMapMarker: function (_vehicleId) {

            // 隐藏之前的marker信息
            if (monitoringObjMap.containsKey(_vehicleId)) {
                var marker = monitoringObjMap.get(_vehicleId);
                if (marker !== null) {
                    marker.stopMove();
                    marker.hide();
                    map.clearMap();
                }
            }

            if (monitoringObjNameMap.containsKey(_vehicleId)) {
                var nameMarker = monitoringObjNameMap.get(_vehicleId);
                nameMarker.hide();
            }

            // 取消订阅上一个
            monitoringObjLngLatMap.remove(_vehicleId);
            monitoringObjMap.remove(_vehicleId);
            securityEnterprise.unSubScribeVehicle(_vehicleId);
        },
        handleVehicleInfo: function (vehicleId, riskId) {
            if (oldVehicleID !== vehicleId && oldVehicleID !== undefined) {

                securityEnterprise.clearMapMarker(oldVehicleID)
            }

            oldVehicleID = vehicleId;
            oldRiskID = riskId;

            securityEnterprise.vehicleLoctionFun(vehicleId);
            securityEnterprise.vehicleUpdateStatus(vehicleId);
        },
        unSubScribeVehicle: function (id) {
            isCarSubscribe = false;
            oldVehicleID = null;

            // 取消订阅位置信息
            var cancelStrS = {
                "desc": {
                    "MsgId": 40964,
                    "UserName": $("#userName").text()
                },
                "data": [{
                    vehicleID: id
                }]
            };

            webSocket.unsubscribealarm(headers, "/app/location/unsubscribe", cancelStrS);
        },
        getLevel: function (value) {
            if (value === 1) {
                return '低风险';
            } else if (value === 2) {
                return '一般风险';
            } else if (value === 3) {
                return '高风险';
            }
            return '';
        },
        getEventDetail: function (id, vehicleId) {
            json_ajax('post', '/clbs/adas/s/riskManage/intelligence/getEvents', 'json', true, {
                riskId: id
            }, function (data) {
                if (data.success) {
                    var res = data.obj;
                    if (!res || res.length === 0) return;
                    var html = '';
                    eventData = res;
                    res.forEach(function (ele, ind) {
                        var ifActive = ind == 0 ? 'active' : '';
                        var picSpan = ele.picFlag == '1' ? '<span class="risk_img2 media-span"  onclick="securityEnterprise.getMedia(\'' + ele.id + '\',0,event,true,\'' + riskType + '\')"></span>' : '<span class="risk_img2 media-span risk_img_no"></span>';
                        var videoSpan = ele.videoFlag == '1' ? '<span class="risk_video2 media-span" onclick="securityEnterprise.getMedia(\'' + ele.id + '\',2,event,true,\'' + riskType + '\')"></span>' : '<span class="risk_video2 media-span risk_video_no"></span>';
                        //黑标车辆隐藏 状态信息 字段
                        var carSpan = ele.eventProtocolType == '25' ? '<span data-value="' + ele.vehicleStatus + '" class="risk_car media-span"></span>' : '';
                        var level = ele.deviceType === 24 ? securityEnterprise.getLevel(ele.level) : (ele.level + '级');

                        html += '<div class="alarm-event clearfix ' + ifActive + '" _id=' + ele.id + '>' +
                            '<span class="alarm-event-time media-span">' + ele.eventTime + '</span>' +
                            '<div class="alarm-event-des">' +
                            '<span class="media-span">' + ele.eventName + '</span>' +
                            '<span class="media-span"> (' + level + ') </span>' +
                            '<p class="alarm-event-media">' +
                            picSpan +
                            videoSpan +
                            carSpan +
                            '</p>' +
                            '</div>' +
                            '</div>'
                    });

                    $('#alarmEventListCont').html(html);
                    $('.risk_car').on('mouseover', securityEnterprise.vehicleStatusInfo);
                    securityEnterprise.drawEventDetailHtml(res[0]);
                }
            })

            securityEnterprise.getDriversICcard(id, vehicleId)
        },
        getDriversICcard: function (id, vehicleId) {
            json_ajax('post', '/clbs/adas/s/riskManage/intelligence/getDrivers', 'json', true, {
                riskId: id,
                vehicleId: vehicleId
            }, function (data) {
                $('#capture').removeClass('btn-primary').attr('disabled', 'disabled');
                $('.prevCard,.nextCard').hide();
                var html = securityEnterprise.driverEmptyDom();

                if (data.success) {
                    driverData = [];
                    if (data.obj.riskDrivers && data.obj.riskDrivers.length > 0) {
                        driverData = data.obj.riskDrivers;
                        if (driverData.length == 1) { //插卡司机
                            $('.prevCard,.nextCard').hide();
                        } else {
                            $('.prevCard,.nextCard').show();
                        }
                    }

                    var num = driverData.length;
                    if (num > 0) {
                        $('#ICcard-box').css('width', 500 * num + 'px');
                    }

                    driverCardIndex = 0;
                    driverCardNum = num;

                    var driverHtml = '';
                    if (driverData.length > 0) {
                        html = '';
                        $('#capture').addClass('btn-primary').removeAttr('disabled');
                        driverData.forEach(function (value, index) {
                            html += securityEnterprise.driverListDom(value);
                            driverHtml += '<option data-id="' + value.id + '" data-index="' + index + '" data-number="' + value.cardNumber + '">' + value.driverName + '</option>';
                        });
                        $('#driverId').val(driverData[0].id);
                        $('#driverNumber').val(driverData[0].cardNumber);
                    }

                    $('#driverSelect').html(driverHtml);
                    $('#driverName').val($('#driverSelect').val());
                    $('#driverSelect').unbind().bind('change', securityEnterprise.driverSelectChange)
                }

                $('#ICcard-box').html(html);
            })
        },
        getImgSrc: function (src) {
            if (src == null || src == "") {
                return "/clbs/resources/img/photoCard.png";
            } else {
                return src;
            }
        },
        driverListDom: function (value) {
            if (value.address) {
                value.address = value.address.length > 20 ?
                    value.address.substr(0, 20) + '...' :
                    value.address;
            }

            var html = '<div class="ICcard-bg-box">' +
                '<div class="ICcard-bg">' +
                '<div class="ICcard-msg-pic">' +
                '<img style="width: 100%;height: 100%;" src=' + securityEnterprise.getImgSrc(value.pic) + '>' +
                '</div>' +
                '<div class="ICcard-msg-detail">' +
                '<div class="ic-item clearfix">' +
                '<span class="tit">姓&emsp;&emsp;名</span>' +
                '<span class="con">' + securityEnterprise.handleHtml(value.driverName) + '</span>' +
                '</div>' +
                '<div class="ic-item clearfix">' +
                '<span class="tit">从业资格证&emsp;&emsp;号</span>' +
                '<span class="con" title="' + securityEnterprise.handleHtml(value.cardNumber) + '">' + securityEnterprise.handleHtml(value.cardNumber) + '</span>' +
                '</div>' +
                '<div class="ic-item clearfix">' +
                '<span class="tit">住&emsp;&emsp;址</span>' +
                '<span class="address" title="' + securityEnterprise.handleHtml(value.address) + '">' + securityEnterprise.handleHtml(value.address) + '</span>' +
                '</div>' +
                '<div class="ic-item clearfix">' +
                '<span class="tit">从业资格类&emsp;&emsp;别</span>' +
                '<span class="con" title="' + securityEnterprise.handleHtml(value.icCardType) + '">' + securityEnterprise.handleHtml(value.icCardType) + '</span>' +
                '</div>' +
                '<div class="ic-item clearfix">' +
                '<span class="tit">发证机关</span>' +
                '<span class="con" title="' + securityEnterprise.handleHtml(value.icCardAgencies) + '">' + securityEnterprise.handleHtml(value.icCardAgencies) + '</span>' +
                '</div>' +
                '<div class="ic-item clearfix">' +
                '<span class="tit">有效期至</span>' +
                '<span class="con" title="' + securityEnterprise.handleHtml(value.icCardEndDate) + '">' + securityEnterprise.handleHtml(value.icCardEndDate) + '</span>' +
                '</div>' +
                '                                    </div>' +
                '                                </div>' +
                '                            </div>';
            return html;
        },
        driverEmptyDom: function () {
            var html = '<div class="ICcard-bg-box">' +
                '<div class="ICcard-bg">' +
                '<div class="ICcard-msg-pic">' +
                '<img style="width: 100%;height: 100%;" src="/clbs/resources/img/photoCard.png">' +
                '</div>' +
                '<div class="ICcard-msg-detail">' +
                '<table>' +
                '<tr>' +
                '<td>姓&emsp;&emsp;名</td>' +
                '<td></td>' +
                '</tr>' +
                '<tr>' +
                '<td>从业资格证&emsp;&emsp;号</td>' +
                '<td></td>' +
                '</tr>' +
                '<tr>' +
                '<td>住&emsp;&emsp;址</td>' +
                '<td></td>' +
                '</tr>' +
                '<tr>' +
                '<td>从业资格类&emsp;&emsp;别</td>' +
                '<td></td>' +
                '</tr>' +
                '<tr>' +
                '    <td>' +
                '        发证机关' +
                '    </td>' +
                '    <td></td>' +
                '</tr>' +
                '<tr>' +
                '    <td>' +
                '        有效期至' +
                '    </td>' +
                '    <td></td>' +
                '</tr>' +
                '                                        </table>' +
                '                                    </div>' +
                '                                </div>' +
                '                            </div>';
            return html;
        },
        driverSelectChange: function () {
            var select = $('#driverSelect option:selected');
            var driverId = select.data('id');
            var driverNumber = select.data('number');
            $('#driverId').val(driverId);
            $('#driverNumber').val(driverNumber);

            var driverName = $('#driverSelect').val();
            driverData.forEach(function (val, ind) {
                if (val.driverName == driverName) {
                    driverCardIndex = ind;
                    $('#ICcard-box').css('left', -500 * driverCardIndex + 'px');
                }
            })
        },
        alarmEventClick: function () {
            if (!$(this).hasClass('active')) {
                $(this).addClass('active').siblings().removeClass('active');
                var id = $(this).attr('_id');

                eventData.forEach(function (value) {
                    if (id == value.id) {
                        securityEnterprise.drawEventDetailHtml(value);
                    }
                })
            }
        },
        drawEventDetailHtml: function (data, type) {
            if (type && type == '1') {
                $('.alarm-event').removeClass('active');
                $(this).addClass('active')
            }

            $('#alarmBrand').html(data.brand)
            $('#alarmGroup').html(data.groupName)
            $('#alarmBrandType').html(data.vehiclePurpose)
            $('#detailAlarmType').html(data.riskType)
            $('#alarmSpeed').html(data.speed + 'km/h')
            $('#alarmWeather').html(data.weather)
            $('#alarmTimeStr').html(data.eventTime)
            $('#alarmLevel').html(securityEnterprise.handleHtmlLevel(data.riskLevel))
            $('#alarmAddr').html(data.address)

        },
        // dropdownMenuClick: function (e) {
        //     $('#dropdownUl').toggle().css({'left': '150px', 'top': '100px'})
        // },
        prevCard: function () {
            if (driverCardIndex > 0) {
                driverCardIndex--;
                $('#ICcard-box').css('left', -500 * driverCardIndex + 'px');
                securityEnterprise.changeDriverSelect();
            }
        },
        nextCard: function () {
            if (driverCardIndex < driverCardNum - 1) {
                driverCardIndex++;
                $('#ICcard-box').css('left', -500 * driverCardIndex + 'px');
                securityEnterprise.changeDriverSelect();
            }
        },
        changeDriverSelect: function () {
            var select = $('#driverSelect option[data-index=' + driverCardIndex + ']');
            var value = select.text();
            var id = select.data('id');
            var number = select.data('number');

            $('#driverSelect').val(value);
            $('#driverName').val(value);
            $('#driverId').val(id);
            $('#driverNumber').val(number);
        },
        dealRisk: function () {
            var dealRiskIds = $('#dealRiskIds').val();
            $('#riskForm').ajaxSubmit(function (data) {
                var data = JSON.parse(data)
                if (data.success) {
                    layer.msg('处理成功');
                    securityEnterprise.clearMapMarker(oldVehicleID);
                    combatVideoList = '';
                    combatCallList = '';
                    checkedVehicleInfo = null;
                    $('#riskDetailCont').removeClass('active');
                    $('#ICcard-box').css('left', '0px');
                    securityEnterprise.getRiskCompanyData(dealRiskIds);
                } else {
                    if (data.msg) {
                        layer.msg(data.msg);
                    }
                }
            })
        },
        /**
         * 风险列表按时间排序升序
         */
        sortByTime: function (_this, parma) {
            riskEventList = securityEnterprise.cloneArray(riskEventListOriginal);

            if ($(_this).hasClass('active')) {
                $(_this).removeClass('active');
                securityEnterprise.renderRiskTable();
                return;
            }


            $('.table-icon i').removeClass('active');
            $(_this).addClass('active');


            riskEventList = riskEventList.sort(function (a, b) {
                var value1 = (new Date(a['warningTime'])).getTime();
                var value2 = (new Date(b['warningTime'])).getTime();

                if (parma === 'up') {
                    return value1 - value2;
                } else {
                    return value2 - value1;
                }
            })

            securityEnterprise.renderRiskTable();

        },
        /**
         * 风险列表按风险等级排序
         */
        sortByLevel: function (_this, parma) {
            riskEventList = securityEnterprise.cloneArray(riskEventListOriginal);
            if ($(_this).hasClass('active')) {
                $(_this).removeClass('active');
                securityEnterprise.renderRiskTable();
                return;
            }
            $('.table-icon i').removeClass('active');
            $(_this).addClass('active');


            riskEventList = riskEventList.sort(function (a, b) {
                var value1 = a['riskLevel'];
                var value2 = b['riskLevel'];

                if (parma === 'up') {
                    return value1 - value2;
                } else {
                    return value2 - value1;
                }
            })

            securityEnterprise.renderRiskTable();
        },
        /**
         * 排序表头的文字点击
         */
        riskEventSort: function (e) {

            window.event ? window.event.cancelBubble = true : e.stopPropagation();

            if (!$(this).siblings('span').find('i.up').hasClass('active') && !$(this).siblings('span').find('i.down').hasClass('active')) {
                $(this).siblings('span').find('i.up').trigger('click');
                return;
            }
            if ($(this).siblings('span').find('i.up').hasClass('active') && !$(this).siblings('span').find('i.down').hasClass('active')) {
                $(this).siblings('span').find('i.down').trigger('click');
                return;
            }

            if (!$(this).siblings('span').find('i.up').hasClass('active') && $(this).siblings('span').find('i.down').hasClass('active')) {
                riskEventList = securityEnterprise.cloneArray(riskEventListOriginal);
                $('.table-icon i').removeClass('active');
                securityEnterprise.renderRiskTable();
                return;
            }
        },
        /**
         * dropdownmenu点击
         */
        dropdownMenuShow: function (event) {
            var e = event || window.event;
            var mainContMarL = $('.main-content-wrapper').css('marginLeft')
            var id = $(this).attr('id');
            var left = e.clientX - 100 - parseInt(mainContMarL);
            var targetCont = $('.dropdown-menu[aria-labelledby=' + id + ']');
            $('.dropdown-menu:not([aria-labelledby=' + id + '])').hide()
            targetCont.toggle().css({
                'left': left + 'px',
                'top': '100px'
            })
        },
        /**
         * 筛选督办状态和风险证据
         */
        filterStateAndEvidence: function () {
            var id = $(this).attr('id');
            var val = $(this).parent().siblings('.drop-menu-operation').find('input[type="radio"]:checked').val();
            var text = $(this).parent().siblings('.drop-menu-operation').find('input[type="radio"]:checked').parent().text();

            $('.filterCont[target-id=' + id + ']').attr('fiterval', val).show().find('span').html(text);

            $(this).parents('.dropdown-menu').hide()

            securityEnterprise.renderRiskTable();
        },
        /**
         * 筛选其他的
         */
        filterSearch: function () {
            var id = $(this).attr('id');
            var val = $(this).parent().siblings('.drop-menu-operation').find('input').val();

            if (id === 'confirmChooseBrand') {
                val = val.toUpperCase();
            }

            $('.filterCont[target-id=' + id + ']').attr('fiterval', val).show().find('span').text(val);
            if (val === '') {
                $('.filterCont[target-id=' + id + ']').hide();
            }

            $(this).parents('.dropdown-menu').hide()

            securityEnterprise.renderRiskTable();
        },
        /**
         * 风险类型筛选下拉框
         */
        comTypeTree: function () {
            var comChildren = [{
                name: "疑似疲劳",
                value: '1',
            },
                {
                    name: "注意力分散",
                    value: '2',
                },
                {
                    name: "违规异常",
                    value: '3',
                },
                {
                    name: "碰撞危险",
                    value: '4',
                },
                {
                    name: "激烈驾驶",
                    value: '5',
                },
            ];

            var zNodes = [{
                name: "全部",
                value: null,
                open: true,
                children: comChildren
            }];

            var alarmTypeSetting = {
                check: {
                    enable: true,
                    chkStyle: "checkbox",
                    chkboxType: {
                        "Y": "s",
                        "N": "s"
                    },
                    radioType: "all"
                },
                view: {
                    dblClickExpand: false,
                    nameIsHTML: true,
                    countClass: "group-number-statistics",
                    showIcon: false
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    onCheck: securityEnterprise.comTypeValue
                }
            };

            $.fn.zTree.init($('#alarmTypeTree'), alarmTypeSetting, zNodes);
        },
        comTypeValue: function () {
            var comTree = $.fn.zTree.getZTreeObj('alarmTypeTree'),
                nodes = comTree.getCheckedNodes(true),
                nameList = "",
                valueList = [];

            if (nodes.length === 6) {
                nameList = '全部';
            } else {
                for (var i = 0, l = nodes.length; i < l; i++) {
                    var name = nodes[i].name;
                    var value = nodes[i].value;

                    if (name != "全部") {
                        if (nameList != "") {
                            nameList = nameList + "、" + name;
                        } else {
                            nameList += name;
                        }
                        valueList.push(value)
                    }
                }
            }

            $('#alarmTypeChoose').val(nameList)
        },
        refreshRiskList: function () {
            $('.filterCont').hide();
            securityEnterprise.getRiskCompanyData();
        },
        setTimeInterval: function () {
            if (refreshTimer) {
                clearInterval(refreshTimer);
            }
            refreshTimer = setInterval(function () {
                var nowTime = (new Date()).getTime();
                if ((nowTime - timeStamp) > 60000) {
                    if (isCarSubscribe === false) {
                        securityEnterprise.init();
                    } else {
                        securityEnterprise.getRiskCompanyData();
                    }

                }
            }, 5000)
        },
        /**
         * 抓拍设置
         * @returns {jQuery}
         */
        photoValidate: function () {
            return $("#takePhoto").validate({
                rules: {
                    wayID: {
                        required: true
                    },
                    /*time: {
                        required: true,
                        digits: true,
                        range: [0, 65535]
                    },
                    command: {
                        range: [1, 10],
                        required: true
                    },*/
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
                    /*time: {
                        required: alarmSearchIntervalTime,
                        digits: alarmSearchIntervalError,
                        range: alarmSearchIntervalSize
                    },
                    command: {
                        range: alarmSearchPhotoSize,
                        required: alarmSearchPhotoNull
                    },*/
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
        takePhoto: function () {
            if (securityEnterprise.photoValidate()) {
                $("#takePhoto").ajaxSubmit(function (data) {
                    $("#goPhotograph").modal("hide");
                    if (JSON.parse(data).success) {
                        layer.msg('拍照参数保存成功');
                        // setTimeout("securityEnterprise.logFindCilck()", 500);
                    } else {
                        layer.msg(JSON.parse(data).msg);
                    }
                });
            }
        },
        showCaptureSet: function () {
            securityEnterprise.getCaptureSetting(); //获取抓拍设置
            $('#goPhotograph').modal('show');
        },
        getCaptureSetting: function () {
            json_ajax('post', '/clbs/adas/s/riskManage/intelligence/getCurrentUserPhotoParam', 'json', true, null, function (data) {
                if (data.success) {
                    var obj = data.obj;
                    if (!obj) {
                        obj = initPhotoParam;
                        initPhotoParam.wayID = protocol != '0' ? 65 : 6; //除中位协议，其余通道默认为65
                    }

                    for (var key in obj) {
                        var value = obj[key];
                        $('#' + key).val(value);
                    }
                } else {
                    if (data.msg) {
                        layer.msg(data.msg);
                    }
                }
            })
        },
        vehicleStatusInfo: function () {
            var _this = $(this);
            var datas = _this.data('value').split(',');
            var result = [];
            for (var i = 0; i < datas.length; i++) {
                result.push(datas[i].split(':')[1]);
            }

            var content = '<div><ul style="padding:0; margin:0;">' +
                '<li>车辆状态：</li>' +
                '<li>ACC状态：' + '<span>' + securityEnterprise.handleShow(result[0]) + '</span></li>' +
                '<li>左转向灯：' + '<span>' + securityEnterprise.handleShow(result[1]) + '</span></li>' +
                '<li>右转向灯：' + '<span>' + securityEnterprise.handleShow(result[2]) + '</span></li>' +
                '<li>雨刮状态：' + '<span>' + securityEnterprise.handleShow(result[3]) + '</span></li>' +
                '<li>制动状态：' + '<span>' + securityEnterprise.handleShow(result[4]) + '</span></li>' +
                '<li>插卡状态：' + '<span>' + securityEnterprise.handleShow(result[5]) + '</span></li>' +
                '</ul></div>';

            _this.justToolsTip({
                animation: "moveInTop",
                width: "auto",
                contents: content,
                gravity: 'top',
                events: 'mouseover',
                theme: 'toolTips',
            });
        },
        handleShow: function (data) {
            return data ? data : '—';
        },
        /**
         * 抓拍
         */
        captureFunc: function () {
            $('#comparePhoto,#capture').removeClass('btn-primary').attr('disabled', 'disabled'); //按钮点击禁用
            $('#compareLoad').show();
            var currenVehicleId = checkedVehicleInfo.vid,
                currentVehivleBrand = checkedVehicleInfo.brand;
            if (!currenVehicleId || !currentVehivleBrand) {
                $('#compareLoad').hide();
                return
            }
            ;
            //不需要调用checkVehicle方法，走808协议，不判断音视频的端口绑定
            securityEnterprise.getCapturePhoto();
        },
        getCapturePhoto: function () {
            var capture = $('#capture'),
                comparePhoto = $('#comparePhoto');
            var icMediaUrl = $('.ICcard-bg-box').eq(driverCardIndex).find('.ICcard-msg-pic img').attr('src');
            if (icMediaUrl === "null" || icMediaUrl == null || icMediaUrl === '') {
                $('#compareLoad').hide();
                layer.msg("获取ic卡证件照片失败");
                return;
            }
            if (!icMediaUrl) {
                $('#compareLoad').hide();
                return
            }
            ;

            //去掉路径最后的?t=1231535
            icMediaUrl = icMediaUrl.split("?")[0];
            var params = {
                vehicleId: checkedVehicleInfo.vid,
                icMediaUrl: icMediaUrl,
                brand: checkedVehicleInfo.brand
            };
            var timer = 90 * 1000; //90秒超时
            json_ajax('post', '/clbs/adas/v/monitoring/faceMatch/photograph', 'json', true, params, function (data) {
                if (data.success) {
                    var obj = data.obj;
                    if (obj.mediaUrl && obj.address) {
                        var address = obj.address,
                            mediaUrl = obj.mediaUrl;

                        $('#photoMsgCont').css('background-image', 'url(' + mediaUrl + ')');
                        $('#capture,#comparePhoto').addClass('btn-primary').removeAttr('disabled'); //按钮恢复可点击
                        comparePhoto.addClass('btn-primary')
                            .removeAttr('disabled')
                            .data('address', address)
                            .data('mediaUrl', mediaUrl);
                    }

                } else {
                    if (data.msg) {
                        layer.msg(data.msg);
                    }
                    $('#capture').addClass('btn-primary').removeAttr('disabled');
                }
                $('#compareLoad').hide();
            }, timer);
        },
        faceMatchAjax: function () {
            $('#comparePhoto,#capture').removeClass('btn-primary').attr('disabled', 'disabled');
            var self = $(this);
            var icMediaUrl = $('.ICcard-bg-box').eq(driverCardIndex).find('.ICcard-msg-pic img').attr('src');
            var address = self.data('address'),
                mediaUrl = self.data('mediaUrl');

            //去掉路径最后的?t=1231535
            icMediaUrl = icMediaUrl.split("?")[0];
            var params = {
                vehicleId: checkedVehicleInfo.vid,
                address: address,
                mediaUrl: mediaUrl,
                icMediaUrl: icMediaUrl,
            };
            var timer = 90 * 1000; //90秒超时
            json_ajax('post', '/clbs/adas/v/monitoring/faceMatch', 'json', true, params, function (data) {
                if (data.success) {
                    var score = data.obj.score;
                    if (score) {
                        securityEnterprise.macthFaceScoreInfo(score);
                    }
                } else {
                    if (data.msg) {
                        layer.msg(data.msg);
                    }
                }
                $('#capture,#comparePhoto').addClass('btn-primary').removeAttr('disabled'); //按钮恢复可点击
            }, timer)
        },
        macthFaceScoreInfo: function (score) {
            var info = '';
            var compareRes = $('.compare-res');
            compareRes.removeClass('compared');

            switch (score) {
                case score >= 90:
                    info = '同一个人的可能性极高';
                    compareRes.addClass('compared');
                    break;
                case score >= 80:
                    info = '同一个人的可能性高';
                    compareRes.addClass('compared');
                    break;
                case score < 80:
                    info = '同一个人的可能性低';
                    break;
                case score < 50:
                    info = '同一个人的可能性极低';
                    break;
                default:
                    break;
            }

            $('.compare-progress').css('width', score + '%');
            $('#compareScore').text(score);
            $('#compareRes').text(info);
            $('#compareInfo').removeClass('hide');
            $('.compare-res').addClass(icon);
        },
    }
    $(function () {
        // 实时视频自适应
        $(".nav-tabs a").on('click', function (e) {
            var id = $(e.target).attr('id');
            if (id == 'toRealTimeVideo') {
                setTimeout(() => {
                    var width = $("#videoCont").width();
                    $("#videoTool").css('width', 'calc(100% - ' + width + 'px)');
                }, 300);
            }
        });

        $(window).resize(function () {
            var width = $("#videoCont").width();
            $("#videoTool").css('width', 'calc(100% - ' + width + 'px)');
        });


        $('input').inputClear();
        //初始化页面
        securityEnterprise.init();
        //图表自适应
        $(window).resize(function () {
            securityEnterprise.chartResize();
        });
        $('#toggle-left-button').on('click', function () {
            setTimeout(function () {
                var tab0 = $('.nav-tabs li').eq(0);
                if (tab0.hasClass('active')) {
                    securityEnterprise.chartResize();
                }
            }, 500)
        });

        $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
            securityEnterprise.navTabToggle(e)
        });

        $('input[name="intervention"]').on('change', function () {
            var val = $('input[name="intervention"]:checked').val();
            if (val == '0') {
                $('input[name="cooperation"]').attr('disabled', 'disabled');
            } else {
                $('input[name="cooperation"]').removeAttr('disabled');
            }
        });

        $('#videoSelect').on("click", securityEnterprise.videoBtnClick);

        $('#callSelect').on('click', function () {
            if (!checkedVehicleInfo) {
                layer.msg('请选择要处理的信息后再点击');
                return;
            }

            if (!securityEnterprise.ifVehicleOnline()) {
                layer.msg('该车辆不在线');
                return;
            }

            securityVideoSeparate.callOrder(selectedRiskId, selectedRiskNumber, selectedRiskWarningTime);
        });

        $('#toTextInfoSend').on('click', securityEnterprise.toTextInfoSend);

        $("#goTxtSend").bind("click", securityEnterprise.goTxtSend);

        $('#confirmOverSee').on('click', securityEnterprise.confirmOverSee);

        $('#batchOverSee').on('click', securityEnterprise.batchOverSee);

        $('#checkAll').on('click', securityEnterprise.checkAll);

        $('.nextMedia').click(securityEnterprise.nextMedia);
        $('.prevMedia').click(securityEnterprise.prevMedia);

        $('#riskTableBody').on('click', 'tr', securityEnterprise.riskEventClick);

        $('#alarmEventList').on('click', '.alarm-event', securityEnterprise.alarmEventClick);

        $('.prevCardBtn').on('click', securityEnterprise.prevCard);
        $('.nextCardBtn').on('click', securityEnterprise.nextCard);

        $('#riskTableCont').unbind("scroll").bind("scroll", securityEnterprise.activeScrollPage);

        $('#warningInfoEditCancel,#riskDetailCont .riskTable-head').on('click', function () {
            $('#photoMsgCont').removeAttr('style');
            $('#ICcard-box').css('left', '0px');
            $('#riskDetailCont').removeClass('active');
            $('#doSubmits').removeClass('focus');
            $('#comparePhoto').removeClass('btn-primary').attr('disabled', 'disabled');
        })

        $('body').on('click', function (event) {
            var event = event || window.event;

            timeStamp = (new Date()).getTime();

            if (typeof event.target.className === 'object' || event.target.className.indexOf('dropdown-menu') === -1 && event.target.className.indexOf('table-head-icon') === -1) {
                $('#riskTableBox .dropdown-menu').hide();
            }
        })

        $('#doSubmits').on('click', securityEnterprise.dealRisk)
        $('#textInfoSend').on('hidden.bs.modal', function (e) {
            $('#sendTextContent').val('')
        })

        $('#sortTimeUp').on('click', function () {
            var _this = this;
            securityEnterprise.sortByTime(_this, 'up')
        })
        $('#sortTimeDown').on('click', function () {
            var _this = this;
            securityEnterprise.sortByTime(_this, 'down')
        })


        $('#sortLevelUp').on('click', function () {
            var _this = this;
            securityEnterprise.sortByLevel(_this, 'up')
        })

        $('#sortLevelDown').on('click', function () {
            var _this = this;
            securityEnterprise.sortByLevel(_this, 'down')
        })

        $('.clickTh').on('click', securityEnterprise.riskEventSort)

        $('.table-head-icon').on('click', securityEnterprise.dropdownMenuShow);

        $('.dropdown-menu').on('click', function (e) {
            window.event ? window.event.cancelBubble = true : e.stopPropagation();
        })
        $('.cancelFilter').on('click', function () {
            $(this).parents('.dropdown-menu').hide();
        })
        $('.filterCont .close').on('click', function () {
            $(this).parent().hide();
            securityEnterprise.renderRiskTable();
        })

        $('.confirmFilter').on('click', securityEnterprise.filterSearch)

        $('#confirmRiskEvidence,#confirmChooseState').on('click', securityEnterprise.filterStateAndEvidence)

        $('.riskTable-head .icon-refresh').on('click', securityEnterprise.refreshRiskList);

        $(document).on('keydown', function (e) {
            if (e.keyCode == 13 && $('#riskDetailCont').hasClass('active')) {
                $('#doSubmits').trigger('click')
            }
        });
        //多媒体
        $('#download').on('click', securityEnterprise.downloadMedia);
        $('#showMedia .arrows').on('click', securityEnterprise.mediaChange);
        $('.media_tab').on('click', '.btn', securityEnterprise.videoChange);
        $("[data-toggle='tooltip']").tooltip();
        //抓拍设置
        $('#compareSet').on('click', securityEnterprise.showCaptureSet);
        $('#goPhotographs').on('click', securityEnterprise.takePhoto);
        $('#capture').on('click', securityEnterprise.captureFunc);
        $('#comparePhoto').on('click', securityEnterprise.faceMatchAjax);
        $('#goPhotograph').on("hide.bs.modal", function () {
            $('label.error').hide();
        });
        //实时视频画面控制
        $('#videoDimming').on('click', function () {
            if (!checkedVehicleInfo) {
                layer.msg('请选择要处理的信息后再点击');
                return;
            }

            if (!securityEnterprise.ifVehicleOnline()) {
                layer.msg('该车辆不在线');
                return;
            }

            securityVideoSeparate.videoDimmingFn();
        });
        $('#videoYunSett').on('click', function () {
            var self = $(this);
            if (!checkedVehicleInfo) {
                layer.msg('请选择要处理的信息后再点击');
                return;
            }

            if (!securityEnterprise.ifVehicleOnline()) {
                layer.msg('该车辆不在线');
                return;
            }

            securityVideoSeparate.cloudStationFn(self);
        });
    })
}(window, $))