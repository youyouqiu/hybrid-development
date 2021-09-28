var videoName = ["one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen"];//视频分隔类样式
var videoPlayList = []; //保存播放中的数据

var createMseVideoObjArr = []; //保存播放中的实例化对象

var combatVideoList = '';
var combatCallList = '';//对讲通道
var callPlayListMap;//对讲打开的通道(方便后面关闭)
var talkStartTime = null;//对讲开始时间
var videoNum = null;

var checkedBrand;
realtimeMonitoringVideoSeparate = {
    /**
     * 视频显示模块分隔
     */
    videoSeparatedFn: function () {
        // 去掉视频窗口出现的滚动条
        $('#video-main-content .videoplayer').css('overflow', 'hidden');
        var _thisId = $(this).attr("id");
        //4屏
        if (_thisId === "videoFour") {
            realtimeMonitoringVideoSeparate.videoSeparatedFour(_thisId);
        }
        //6屏
        else if (_thisId === "videoSix") {
            realtimeMonitoringVideoSeparate.videoSeparatedSix(_thisId);
        }
        //9屏
        else if (_thisId === "videoNine") {
            realtimeMonitoringVideoSeparate.videoSeparatedNine(_thisId);
        }
        //10屏
        else if (_thisId === "videoTen") {
            realtimeMonitoringVideoSeparate.videoSeparatedTen(_thisId);
        }
        //16屏
        else if (_thisId === "videoSixteen") {
            realtimeMonitoringVideoSeparate.videoSeparatedSixteen(_thisId, videoNum);
        }
    },
    /**
     * 视频分隔4屏
     */
    videoSeparatedFour: function (_thisId) {
        $("#videoSeparated").find("i").removeClass("video-check");
        if (!($("#" + _thisId).hasClass("video-check"))) {
            //添加高亮
            $("#" + _thisId).addClass("video-check");
            //移除已添加的视频
            $("#videoplayer>div:nth-child(4)").nextAll().hide();
            //高宽度
            var vwidth = 100 / 2;

            var vheight = $("#videoplayer").height() / 2;

            $("#videoplayer>div").css({
                "width": vwidth + "%",
                "height": "calc(100% - (100% - " + vheight + "px))"
            });
        }
    },
    /**
     * 视频分隔6屏
     */
    videoSeparatedSix: function (_thisId) {

        if ($("#" + _thisId).hasClass("video-check")) { // 恢复默认
            $("#videoSeparated").find("i").removeClass("video-check");
            //移除高亮
            $("#" + _thisId).removeClass("video-check");
            //移除已添加的视频
            $("#videoplayer>div:nth-child(4)").nextAll().hide();
            //高宽度
            var vwidth = 100 / 2;
            //判断视频模块是否隐藏
            var vheight = $("#videoplayer").height() / 2;
            $("#videoplayer>div").css({
                "width": vwidth + "%",
                "height": "calc(100% - (100% - " + vheight + "px))"
            });
            //4屏按钮高亮
            $("#videoFour").addClass("video-check");
        } else { // 6个video
            $("#videoSeparated").find("i").removeClass("video-check");
            //添加高亮
            $("#" + _thisId).addClass("video-check");
            //移除已添加的视频
            $("#videoplayer>div").show();
            $("#videoplayer>div:nth-child(6)").nextAll().hide();
            //定义视频
            var videoLength = $('#videoplayer>div').length;
            var _html = "";
            for (var i = videoLength; i < 6; i++) {
                _html +=
                    '<div class="pull-left video-box v-' + videoName[i] + '">' +
                    '<video autoplay width="100%" height="100%" id="v_' + i + '_Source">' +
                    '<source src="" type="video/mp4">' +
                    '<source src="" type="video/ogg">' +
                    '您的浏览器不支持 video 标签。' +
                    '</video>' +
                    '</div>';
            }
            $("#videoplayer").append(_html);
            //高宽度
            var vwidth = 100 / 3;
            var vheight = $("#videoplayer").height() / 3;

            $("#videoplayer>div").css({
                "width": vwidth + "%",
                "height": "calc(100% - (100% - " + vheight + "px))"
            });
            //第一个视频高宽度
            var vOneWidth = vwidth * 2;
            var vOneHeight = vheight * 2;
            $("#videoplayer div.v-one").css({
                "width": vOneWidth + "%",
                "height": "calc(100% - (100% - " + (vOneHeight - 0.1) + "px))"
            });
        }
    },
    /**
     * 视频分隔9屏
     */
    videoSeparatedNine: function (_thisId) {
        if ($("#" + _thisId).hasClass("video-check")) {
            $("#videoSeparated").find("i").removeClass("video-check");
            //移除高亮
            $("#" + _thisId).removeClass("video-check");
            //移除已添加的视频
            $("#videoplayer>div:nth-child(4)").nextAll().hide();
            //高宽度
            var vwidth = 100 / 2;
            var vheight = $("#videoplayer").height() / 2;
            $("#videoplayer>div").css({
                "width": vwidth + "%",
                "height": "calc(100% - (100% - " + vheight + "px))"
            });
            //4屏按钮高亮
            $("#videoFour").addClass("video-check");
        } else {
            $("#videoSeparated").find("i").removeClass("video-check");
            //添加高亮
            $("#" + _thisId).addClass("video-check");
            //移除已添加的视频
            $("#videoplayer>div").show();
            $("#videoplayer>div:nth-child(9)").nextAll().hide();
            //定义视频
            var videoLength = $('#videoplayer>div').length;
            var _html = "";
            for (var i = videoLength; i < 9; i++) {
                _html +=
                    '<div class="pull-left video-box v-' + videoName[i] + '">' +
                    '<video autoplay width="100%" height="100%" id="v_' + i + '_Source">' +
                    '<source src="" type="video/mp4">' +
                    '<source src="" type="video/ogg">' +
                    '您的浏览器不支持 video 标签。' +
                    '</video>' +
                    '</div>';
            }
            $("#videoplayer").append(_html);
            //高宽度
            var vwidth = 100 / 3;
            //判断视频模块是否隐藏
            var vheight = $("#videoplayer").height() / 3;
            $("#videoplayer>div").css({
                "width": vwidth + "%",
                "height": "calc(100% - (100% - " + vheight + "px))"
            });
        }
    },
    /**
     * 视频分隔10屏
     */
    videoSeparatedTen: function (_thisId) {
        if ($("#" + _thisId).hasClass("video-check")) {
            $("#videoSeparated").find("i").removeClass("video-check");
            //移除高亮
            $("#" + _thisId).removeClass("video-check");
            //移除已添加的视频
            $("#videoplayer>div:nth-child(4)").nextAll().hide();
            //高宽度
            var vwidth = 100 / 2;
            //判断视频模块是否隐藏
            var vheight = $("#videoplayer").height() / 2;
            $("#videoplayer>div").css({
                "width": vwidth + "%",
                "height": "calc(100% - (100% - " + vheight + "px))"
            });
            //4屏按钮高亮
            $("#videoFour").addClass("video-check");
        } else {
            $("#videoSeparated").find("i").removeClass("video-check");
            //添加高亮
            $("#" + _thisId).addClass("video-check");
            //移除已添加的视频
            $("#videoplayer>div").show();
            $("#videoplayer>div:nth-child(10)").nextAll().hide();
            //定义视频
            var videoLength = $('#videoplayer>div').length;
            var _html = "";
            for (var i = videoLength; i < 10; i++) {
                _html +=
                    '<div class="pull-left video-box v-' + videoName[i] + '">' +
                    '<video autoplay width="100%" height="100%" id="v_' + i + '_Source">' +
                    '<source src="" type="video/mp4">' +
                    '<source src="" type="video/ogg">' +
                    '您的浏览器不支持 video 标签。' +
                    '</video>' +
                    '</div>';
            }
            $("#videoplayer").append(_html);
            //高宽度
            var vwidth = 100 / 5;
            //判断视频模块是否隐藏
            var vheight = $("#videoplayer").height() / 5;
            $("#videoplayer>div").css({
                "width": vwidth + "%",
                "height": "calc(100% - (100% - " + vheight + "px))"
            });
            //第一个视频高宽度
            var vOneWidth = vwidth * 4;
            var vOneHeight = vheight * 4;
            $("#videoplayer div.v-one").css({
                "width": vOneWidth + "%",
                "height": "calc(100% - (100% - " + (vOneHeight - 0.1) + "px))"
            });
        }
    },
    /**
     * 视频分隔16屏
     */
    videoSeparatedSixteen: function (_thisId, num) {
        if ($("#" + _thisId).hasClass("video-check")) {
            $("#videoSeparated").find("i").removeClass("video-check");
            //移除高亮
            $("#" + _thisId).removeClass("video-check");
            //移除已添加的视频
            $("#videoplayer>div:nth-child(4)").nextAll().hide();
            //高宽度
            var vwidth = 100 / 2;
            var vheight = $("#videoplayer").height() / 2;
            $("#videoplayer>div").css({
                "width": vwidth + "%",
                "height": "calc(100% - (100% - " + vheight + "px))"
            });
            //4屏按钮高亮
            $("#videoFour").addClass("video-check");
        } else {
            $("#videoSeparated").find("i").removeClass("video-check");
            //添加高亮
            $("#" + _thisId).addClass("video-check");
            //移除已添加的视频
            $("#videoplayer>div").show();
            $("#videoplayer>div:nth-child(16)").nextAll().hide();
            // 超出16个窗口，显示滚动条
            $('#video-main-content .videoplayer').css('overflow', 'auto');
            //定义视频
            var videoLength = $('#videoplayer>div').length;
            var _html = "";
            var channelNum = num ? num : 16;
            for (var i = videoLength; i < channelNum; i++) {
                _html +=
                    '<div class="pull-left video-box v-' + (videoName[i] ? videoName[i] : i) + '">' +
                    '<video autoplay width="100%" height="100%" id="v_' + i + '_Source">' +
                    '<source src="" type="video/mp4">' +
                    '<source src="" type="video/ogg">' +
                    '您的浏览器不支持 video 标签。' +
                    '</video>' +
                    '</div>';
            }
            $("#videoplayer").append(_html);
            //高宽度
            var vwidth = 100 / 4;
            var vheight = $("#videoplayer").height() / 4;
            $("#videoplayer>div").css({
                "width": vwidth + "%",
                "height": "calc(100% - (100% - " + vheight + "px))"
            });
            if (num) {
                $("#videoplayer").css({
                    'overflow-y': 'auto'
                })
            }
        }
    },
    /**
     * 音视频模块全屏显示
     */
    videoFullScreenFn: function () {
        // 如果视频被隐藏，点击全屏按钮不操作
        var isVideoHide = !$('#mapAllShow').children().hasClass("fa fa-chevron-left");
        if (isVideoHide && $('#mapAllShow').length > 0) {
            return;
        }
        if ($(this).hasClass("video-full-screen")) {
            //图标改变
            $(this).removeClass("video-full-screen").addClass("video-full-screen-check");
            $('.videoplayer-box').css('height', 'calc(100% - 50px)');


            $('#videoToolCopy').html($('#videoTool').html()).css('display', 'inline-block');
            $('#videoTool').html('').hide();
            // 先设置具体宽度为了添加过渡效果
            var realTimeVideoRealW = $('#realTimeVideoReal').width();
            $('#videoCont').css('width', realTimeVideoRealW + 'px');

            var windowW = $(window).width();
            $('#videoCont').addClass('pos-fix').css('width', windowW + 'px');
            realtimeMonitoringVideoSeparate.videoSeparatedAdaptShow();
        } else {
            //图标改变
            $(this).removeClass("video-full-screen-check").addClass("video-full-screen");
            $('.videoplayer-box').css('height', '500px');
            $('#videoCont,.videoplayer-box').removeAttr('style');

            var realTimeVideoRealW = $('#realTimeVideoReal').width();
            $('#videoCont').removeClass('pos-fix').css('width', realTimeVideoRealW + 'px');

            $('#videoTool').html($('#videoToolCopy').html()).css('display', 'inline-block');
            $('#videoToolCopy').html('').hide();
            realtimeMonitoringVideoSeparate.videoSeparatedAdaptShow()
        }
        // 主动安全视频播放下的视频控制功能
        $('#videoSelect').on("click", dataTableOperation.videoBtnClick);
        // 主动安全视频播放下的对讲功能
        $('#callSelect').on('click', dataTableOperation.callSelectFun);
    },
    /**
     * 左下侧显示及取消全屏显示时视频分隔自适应计算函数
     */
    videoSeparatedAdaptShow: function () {
        //判断当前屏幕分隔数 区分屏幕分隔高宽度
        if ($("#videoFour").hasClass("video-check")) {
            //高宽度
            var vwidth = 100 / 2;
            var vheight = $("#videoplayer").height() / 2;
            $("#videoplayer>div").css({
                "width": vwidth + "%",
                "height": "calc(100% - (100% - " + vheight + "px))"
            });
        }
        else if ($("#videoSix").hasClass("video-check")) {
            //高宽度
            var vwidth = 100 / 3;
            var vheight = $("#videoplayer").height() / 3;
            $("#videoplayer>div").css({
                "width": vwidth + "%",
                "height": "calc(100% - (100% - " + vheight + "px))"
            });
            //第一个视频高宽度
            var vOneWidth = vwidth * 2;
            var vOneHeight = vheight * 2;
            $("#videoplayer div.v-one").css({
                "width": vOneWidth + "%",
                "height": "calc(100% - (100% - " + (vOneHeight - 0.1) + "px))"
            });
        }
        else if ($("#videoNine").hasClass("video-check")) {
            //高宽度
            var vwidth = 100 / 3;
            var vheight = $("#videoplayer").height() / 3;
            $("#videoplayer>div").css({
                "width": vwidth + "%",
                "height": "calc(100% - (100% - " + vheight + "px))"
            });
        }
        else if ($("#videoTen").hasClass("video-check")) {
            //高宽度
            var vwidth = 100 / 5;
            var vheight = $("#videoplayer").height() / 5;
            $("#videoplayer>div").css({
                "width": vwidth + "%",
                "height": "calc(100% - (100% - " + vheight + "px))"
            });
            //第一个视频高宽度
            var vOneWidth = vwidth * 4;
            var vOneHeight = vheight * 4;
            $("#videoplayer div.v-one").css({
                "width": vOneWidth + "%",
                "height": "calc(100% - (100% - " + (vOneHeight - 0.1) + "px))"
            });
        }
        else if ($("#videoSixteen").hasClass("video-check")) {
            //高宽度
            var vwidth = 100 / 4;
            var vheight = $("#videoplayer").height() / 4;
            $("#videoplayer>div").css({
                "width": vwidth + "%",
                "height": "calc(100% - (100% - " + vheight + "px))"
            });
        }
    },
    /**
     * 初始化视频
     */
    initVideoRealTimeShow: function (vehicleInfo) {
        checkedBrand = vehicleInfo.brand;
        var vehicle_Id = vehicleInfo.vid, vehicle_name = vehicleInfo.brand;
        json_ajax('post', '/clbs/realTimeVideo/video/getChannels', 'json', true, {
            vehicleId: vehicle_Id,
            isChecked: false
        }, function (data) {
            // console.log(data)
            if (data.success) {
                var msg = JSON.parse(ungzip(data.msg));
                if (msg.length == 0) {
                    layer.msg(vehicle_name + '未设置通道号');
                    combatVideoList = [];
                    combatCallList = [];
                } else {
                    //所有视频通道
                    combatVideoList = msg.filter(function (item) {
                        return item.channelType !== 1;
                    });
                    //所有音频通道
                    combatCallList = msg.filter(function (item) {
                        return item.channelType === 1;
                    });
                    realtimeMonitoringVideoSeparate.sendParamByBatch();
                }
            }
        })
    },
    /**
     * 订阅实时视频
     */
    sendParamByBatch: function () {
        if (!combatVideoList || combatVideoList.length === 0) {
            layer.msg(checkedBrand + '未设置通道号');
            return;
        }

        var jsonArr = [];

        videoPlayList = [];

        var subscribeInfo = {vehicleId: combatVideoList[0].vehicleId, channels: []};
        combatVideoList.forEach(function (val) {
            if (val.channelType !== 1) {
                subscribeInfo.channels.push({
                    number: val.logicChannel,
                    streamType: val.streamType,
                    channelType: val.channelType,
                });
            }
        });

        //请求获取实时视频订阅参数
        json_ajax('POST', '/clbs/v/monitoring/audioAndVideoParameters/' + combatVideoList[0].vehicleId, 'json', true, null, function (result) {
            if(result.success) {
                videoPlayList = combatVideoList;
                videoRequestParam = result.obj;
                realtimeMonitoringVideoSeparate.openTerminalVideo(combatVideoList, result.obj);
            }
        });
    },
    /*---------------------------------对讲-----------------------------------*/
    /**
     * 对讲事件
     */
    callOrder: function () {
        var self = $('#callSelect');

        if (combatCallList === '' && !self.hasClass('active')) {
            layer.msg('该监控对象没有对讲通道,不支持对讲功能');
            return;
        }
        if (combatCallList.length === 0) {
            layer.msg('当前监控对象没有音频通道号');
            return;
        }
        if (!self.hasClass('active')) {
            self.addClass('active');
            json_ajax('POST', '/clbs/v/monitoring/audioAndVideoParameters/' + combatCallList[0].vehicleId, 'json', true, null, function (message) {
                if(message.success) {
                    realtimeMonitoringVideoSeparate.callPlay(message.obj);//打开
                }
            });
        } else {
            realtimeMonitoringVideoSeparate.callClose();//关闭
            self.removeClass('active');
        }
    },
    /**
     * 打开对讲
     */
    callPlay: function (audioData) {
        if (combatCallList.length <= 0) {
            return;
        }

        var vehicleId= combatCallList[0].vehicleId;
        var channelNum= combatCallList[0].logicChannel;
        var streamType= combatCallList[0].streamType;
        var channelType= combatCallList[0].channelType;
        var simCardNumber = combatCallList[0].mobile;
        var simCardLength = simCardNumber.length;

        if (simCardLength < 12) {
            for (var i = 0; i < 12 - simCardLength; i++) {
                simCardNumber = '0' + simCardNumber;
            }
        }

        var protocol = 'ws://';
        if (document.location.protocol === 'https:') {
            protocol = 'wss://';
        }

        var url = protocol + videoRequestUrl + ':' + audioRequestPort + '/' + simCardNumber + '/' + channelNum + '/2';

        //保存对讲通道数据
        var callPlayData = {
            vehicleId: combatCallList[0].vehicleId,
            channelNum: combatCallList[0].logicChannel,
            channelType: combatCallList[0].channelType,
            mobile: combatCallList[0].mobile,
            streamType: combatCallList[0].streamType,
        };

        var data = {
            vehicleId: vehicleId,
            simcardNumber: simCardNumber,
            channelNumber: JSON.stringify(channelNum),
            sampleRate: audioData.samplingRateStr || '8000',
            channelCount: audioData.vocalTractStr || '0',
            audioFormat: audioData.audioFormatStr,
            playType: 'BOTH_WAY',
            dataType: '2',
            userID: audioData.userUuid,
            deviceID: audioData.deviceId,
            streamType: JSON.stringify(streamType),
            deviceType: audioData.deviceType,
        };

        //存储对讲实例对象
        callPlayData.audioMse = new RTPMediaPlayer({
            url: url,
            type: 'BOTH_WAY',
            data: data,
            audioEnabled: true,
            videoEnabled: false,
            recordEnabled: true,
            onMessage: function($data, $msg) {
                console.log('9999999999999999999999999999999999')
                var info = JSON.parse($msg);
                if (info.data.msgBody.code == -1004 ||
                    info.data.msgBody.code == -1005 ||
                    info.data.msgBody.code == -1006 ||
                    info.data.msgBody.code == -1008
                ) {
                    layer.msg(info.data.msgBody.msg);
                }
            },
            socketOpenFun: function ($data, $$this) {
                var setting = {
                    vehicleId: $data.vehicleId, // 车辆ID
                    simcardNumber: $data.simcardNumber, // sim卡号
                    channelNumber: $data.channelNumber, // 通道号
                    sampleRate: JSON.stringify($data.sampleRate), // 采样率
                    channelCount: JSON.stringify($data.channelCount), // 声道数
                    audioFormat: $data.audioFormat, // 编码格式
                    playType: $data.playType, // 播放类型 实时 REAL_TIME，回放 TRACK_BACK，对讲 BOTH_WAY，监听 UP_WAY，广播 DOWN_WAY
                    dataType: JSON.stringify($data.dataType), // 数据类型0：音视频，1：视频，2：双向对讲，3：监听，4：中心广播，5：透传
                    userID: $data.userID, // 用户ID
                    deviceID: $data.deviceID, // 终端ID
                    streamType: $data.streamType, // 码流类型0：主码流，1：子码流
                    deviceType: $data.deviceType,
                };
                $$this.play(setting);
            },
            openAudioSuccess: function () {
                // talkBackLoad.talkBackCarriedOutFn();
            },
            openAudioFail: function (msg) {
                layer.msg(msg);
            },
            // socket关闭成功
            socketCloseFun: function ($state) {
                console.log('关闭成功')
            },
        });

        callPlayListMap = callPlayData;
    },

    /**
     * 关闭对讲
     */
    callClose: function (riskId, warningTime) {
        var item = callPlayListMap;

        if (!item) {
            return;
        }

        if(item.audioMse) {
            item.audioMse.closeSocket();//关闭
        }
    },
    /**
     * 订阅成功后打开视频
     */
    openTerminalVideo: function (info, videoRequestParam) {
        $(".video-box h4").html('');
        videoNum = null;
        // 根据通道数量显示视频窗口数
        if (info.length <= 4 && !($("#videoFour").hasClass("video-check"))) {
            $('#videoFour').trigger('click')
        }
        if (info.length > 4 && info.length <= 6 && !($("#videoSix").hasClass("video-check"))) {
            $('#videoSix').trigger('click')
        }
        if (info.length > 6 && info.length <= 9 && !($("#videoNine").hasClass("video-check"))) {
            $('#videoNine').trigger('click')
        }
        if (info.length > 9 && info.length <= 10 && !($("#videoTen").hasClass("video-check"))) {
            $('#videoTen').trigger('click')
        }
        if (info.length > 10 && info.length <= 16 && !($("#videoSixteen").hasClass("video-check"))) {
            $('#videoSixteen').trigger('click')
        }
        if (info.length > 16 && !($("#videoSixteen").hasClass("video-check"))) {
            // info.length = 16;
            videoNum = info.length;
            $('#videoSixteen').trigger('click');
        }
        createMseVideoObjArr = [];//实例化对象的数组

        var protocol = 'ws://';
        if (document.location.protocol === 'https:') {
            protocol = 'wss://';
        }

        $('#videoplayer video').siblings('img,div').remove();
        for (var i = 0; i < info.length; i++) {
            var value = info[i];
            var domId = 'v_' + i + '_Source';
            var simcardNumber = value.mobile;
            var channelNumber = value.physicsChannel;
            var url = protocol + videoRequestUrl + ':' + videoRequestPort + '/' + simcardNumber + '/' + channelNumber + '/0';
            realtimeMonitoringVideoSeparate.videoMessage('视频请求中...', domId);
            var subscribeData = {
                domId:domId,
                vehicleId: value.vehicleId, //车id
                simcardNumber: videoRequestParam.simcardNumber, //终端手机卡号
                channelNumber: JSON.stringify(channelNumber), //终端通道号
                sampleRate: videoRequestParam.samplingRateStr || '8000', //音频采样率
                channelCount: videoRequestParam.vocalTractStr || '0', //音频声道数
                audioFormat: videoRequestParam.audioFormatStr, //音频编码
                playType: 'REAL_TIME', // 播放类型(实时 REAL_TIME，回放 TRACK_BACK，对讲 BOTH_WAY，监听 UP_WAY，广播 DOWN_WAY)
                dataType: '0', //播放数据类型(0：音视频，1：视频，2：双向对讲，3：监听，4：中心广播，5：透传)
                userID:videoRequestParam.userUuid, //用户ID
                deviceID: videoRequestParam.deviceId, //终端ID
                streamType: JSON.stringify(value.streamType), //码流类型(0：主码流，1：子码流)
                deviceType: videoRequestParam.deviceType,
            };
            // $('#' + domId).siblings('img,div').remove();
            var createMseVideo = new RTPMediaPlayer(
                {
                    domId: domId,
                    url: url,
                    data: subscribeData,
                    panoramaType: value && value.panoramic === true ? 1 : 0,
                    vrImageSrc: '/clbs/resources/img/qj360.png',
                    //异常
                    onMessage: function($data, $msg) {
                        var code = JSON.parse($msg).data.msgBody.code;
                        var domId = $data.domId;
                        switch (code) {
                            case -1004:
                                layer.msg('终端未响应,请重新订阅', { time: 2000 });
                                realtimeMonitoringVideoSeparate.videoMessage('终端未响应',domId);
                                break;
                            case -1005:
                                realtimeMonitoringVideoSeparate.videoMessage('无音视频内容',domId);
                                break;
                            case -1006:
                                realtimeMonitoringVideoSeparate.videoMessage('加载失败',domId);
                                break;
                        }
                    },
                    socketOpenFun: function ($data, $$this) {
                        /**
                         * socket连接建立成功
                         * 图标提示替换和消息发送
                         */

                        var setting = {
                            vehicleId: $data.vehicleId,
                            simcardNumber: $data.simcardNumber,
                            channelNumber: $data.channelNumber,
                            sampleRate: $data.sampleRate,
                            channelCount: $data.channelCount,
                            audioFormat: $data.audioFormat,
                            playType: $data.playType,
                            dataType: $data.dataType,
                            userID: $data.userID,
                            deviceID: $data.deviceID,
                            streamType: $data.streamType,
                            deviceType: $data.deviceType,
                        };
                        $$this.play(setting);
                    },
                    // 开始播放
                    onPlaying: function ($state, $this) {
                        $("#msg_" + $state.domId).html('');
                        $('#videoSelect').addClass('active');
                        realtimeMonitoringVideoSeparate.videoPlaySuccess($state, $this);
                    },
                    //播放失败
                    openVideoFail: function ($state, $this){
                        console.log('播放失败', $state);
                    },
                }
            );

            var obj = {};
            obj.vehicleId = value.vehicleId;
            obj.channelNumber = channelNumber;
            obj.createMseVideoFun = createMseVideo;
            obj.domId = domId;

            createMseVideoObjArr.push(obj)

        }
    },

    videoMessage: function(msg, domId) {
        var id = "msg_" + domId;
        if($("#" + id).length == 0){
            var parent = $("#" + domId).parent();
            var h4 = document.createElement('h4');
            h4.id = id;
            $(h4).css({position: 'absolute',bottom: 10, left: '50%',width: '130px', margin: '0 0 0 -65px','text-align':'center', 'font-weight':'bold'});
            $(h4).html(msg);
            parent.append(h4);
        }else{
            $("#" + id).html(msg);
        }
    },

    /**
     * 关闭视频
     */
    closeTerminalVideo: function () {
        var videoBackground = window.localStorage.getItem('videoBg');
        if(createMseVideoObjArr.length > 0) {
            $(".video-box h4").html('');
            createMseVideoObjArr.forEach(function (value) {
                $("#"+ value.domId).replaceWith('<video id="'+ value.domId +'" width="100%" height="100%" ' +
                    'style="background-image: url(' + videoBackground + ')"></video>');
                value.createMseVideoFun.closeSocket();
                $('#videoSelect').removeClass('active');
                console.log('关闭成功');
            })
        }
    },
    /**
     * 视频播放成功事件
     * @param $state
     * @param $this
     */
    videoPlaySuccess: function ($state, $this) {
        // 视频双击事件，视频方法为1屏
        $('.video-box').off('dblclick').on('dblclick', realtimeMonitoringVideoSeparate.videoFullShow);
    },
    /**
     * 视频全屏显示
     */
    videoFullShow: function () {
        var $this = $(this);
        if ($this.hasClass('full-video')) {
            $this.removeClass('full-video');
        } else {
            $this.addClass('full-video');
        }
    },
    /**
     * 视频播放应答订阅
     */
    videoPlayAnswerSubscribe: function () {
        setTimeout(function () {
            if (webSocket.conFlag) {
                webSocket.subscribe(headers, "/user/topic/video/realtime", function (data) {
                    realtimeMonitoringVideoSeparate.videoStartPlay($.parseJSON(data.body));
                }, null, null);
            } else {
                realtimeMonitoringVideoSeparate.videoPlayAnswerSubscribe();
            }
        }, 2000);
    },
    videoStartPlay: function (body) {
        if (body) {
            videoPlayList = body;
            realtimeMonitoringVideoSeparate.openTerminalVideo(body)
        }
    },
};


$(function () {
    $('#videoFour,#videoSix,#videoNine,#videoTen,#videoSixteen').on('click', realtimeMonitoringVideoSeparate.videoSeparatedFn);
    $("#videoFullScreen").on("click", realtimeMonitoringVideoSeparate.videoFullScreenFn);//音视频模块全屏显示
})