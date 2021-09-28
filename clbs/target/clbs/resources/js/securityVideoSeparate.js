// (function (window,$) {
var winHeight = $(window).height(); //window高度

//视频
var videoName = ["one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen"];//视频分隔类样式
var videoPlayList = []; //视频数据
var createMseVideoObjArr = []; //视频实例对象
var combatVideoList = '';

//当前监控对象
var checkVehicleInfo;
var checkedBrand;

//对讲
var combatCallList = '';//对讲通道
var callPlayListMap;//对讲打开的通道(方便后面关闭)
var talkStartTime = null;//对讲开始时间

//云台
var videoChannelsInfo = [];//视频通道信息
var createChannelVoice = null; //创建的通道号声音对象id
var haeundaeChannelNum;// 云台功能使用车辆通道号
var speedZoomParameter ,// 云台速度快慢值
    waitTimeTimeout ;// 9101或9102操作计时器

securityVideoSeparate={
    /**
     * 视频显示模块分隔
     */
    videoSeparatedFn: function () {
        // 去掉视频窗口出现的滚动条
        $('#video-main-content .videoplayer').css('overflow', 'hidden');
        var _thisId = $(this).attr("id");
        //4屏
        if (_thisId === "videoFour") {
            securityVideoSeparate.videoSeparatedFour(_thisId);
        }
        //6屏
        else if (_thisId === "videoSix") {
            securityVideoSeparate.videoSeparatedSix(_thisId);
        }
        //9屏
        else if (_thisId === "videoNine") {
            securityVideoSeparate.videoSeparatedNine(_thisId);
        }
        //10屏
        else if (_thisId === "videoTen") {
            securityVideoSeparate.videoSeparatedTen(_thisId);
        }
        //16屏
        else if (_thisId === "videoSixteen") {
            securityVideoSeparate.videoSeparatedSixteen(_thisId);
        }
    },
    /**
     * 视频分隔4屏
     */
    videoSeparatedFour: function (_thisId) {
        $("#videoSeparated").find("i").removeClass("video-check");
        if (!($("#" + _thisId).hasClass("video-check"))) {

            //高宽度
            var vwidth = 100 / 2;

            var vheight = $("#videoplayer").height() / 2;

            //添加高亮
            $("#" + _thisId).addClass("video-check");
            //移除已添加的视频
            $("#videoplayer>div:nth-child(4)").nextAll().hide();


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

            //高宽度
            vwidth = 100 / 3;
            vheight = $("#videoplayer").height() / 3;

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

            //高宽度
            vwidth = 100 / 3;
            //判断视频模块是否隐藏
            vheight = $("#videoplayer").height() / 3;

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

            //高宽度
            vwidth = 100 / 5;
            //判断视频模块是否隐藏
            vheight = $("#videoplayer").height() / 5;

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

            $("#videoplayer>div").css({
                "width": vwidth + "%",
                // "height": "calc(100% - (100% - " + vheight + "px))"
                "height":  vheight + "px"
            });
            //第一个视频高宽度
            var vOneWidth = vwidth * 4;
            var vOneHeight = vheight * 4;
            $("#videoplayer div.v-one").css({
                "width": vOneWidth + "%",
                "height": (vOneHeight - 0.1) + "px"
            });
        }
    },
    /**
     * 视频分隔16屏
     */
    videoSeparatedSixteen: function (_thisId) {
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

            //高宽度
            vwidth = 100 / 4;
            vheight = $("#videoplayer").height() / 4;

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
            for (var i = videoLength; i < 16; i++) {
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

            $("#videoplayer>div").css({
                "width": vwidth + "%",
                "height": "calc(100% - (100% - " + vheight + "px))"
            });
        }
    },
    /**
     * 音视频模块全屏显示
     */
    videoFullScreenFn: function () {
        if ($(this).hasClass("video-full-screen")) {
            //图标改变
            $(this).removeClass("video-full-screen").addClass("video-full-screen-check");

            $('#realTimeVideoBox').addClass('pos-fix');
            $("#videoTool").css('width','15%');
            securityVideoSeparate.videoSeparatedAdaptShow()
        } else {
            //图标改变
            $(this).removeClass("video-full-screen-check").addClass("video-full-screen");

            $('#realTimeVideoBox').removeClass('pos-fix');
            var width = $("#videoCont").width();
            $("#videoTool").css('width','calc(100% - ' + width +'px)');
            securityVideoSeparate.videoSeparatedAdaptShow()
        }
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
            vwidth = 100 / 3;
            vheight = $("#videoplayer").height() / 3;
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
            vwidth = 100 / 3;
            vheight = $("#videoplayer").height() / 3;
            $("#videoplayer>div").css({
                "width": vwidth + "%",
                "height": "calc(100% - (100% - " + vheight + "px))"
            });
        }
        else if ($("#videoTen").hasClass("video-check")) {
            //高宽度
            vwidth = 100 / 5;
            vheight = $("#videoplayer").height() / 5;
            $("#videoplayer>div").css({
                "width": vwidth + "%",
                "height": "calc(100% - (100% - " + vheight + "px))"
            });
            //第一个视频高宽度
            vOneWidth = vwidth * 4;
            vOneHeight = vheight * 4;
            $("#videoplayer div.v-one").css({
                "width": vOneWidth + "%",
                "height": "calc(100% - (100% - " + (vOneHeight - 0.1) + "px))"
            });
        }
        else if ($("#videoSixteen").hasClass("video-check")) {
            //高宽度
            vwidth = 100 / 4;
            vheight = $("#videoplayer").height() / 4;
            $("#videoplayer>div").css({
                "width": vwidth + "%",
                "height": "calc(100% - (100% - " + vheight + "px))"
            });
        }
    },
    /**
     * 初始化视频
     */
    initVideoRealTimeShow:function (vehicleInfo) {
        checkVehicleInfo = vehicleInfo;//保存当前监控对象信息
        var vehicle_Id = vehicleInfo.vid;

        json_ajax('post','/clbs/realTimeVideo/video/getChannels','json',true,{vehicleId:vehicle_Id,isChecked:false},function (data) {
            if (data.success) {
                var msg = JSON.parse(ungzip(data.msg));
                videoChannelsInfo = msg;
                if (msg.length === 0){
                    //设置通道下拉
                    securityVideoSeparate.setSelectOption([]);
                    combatVideoList=[];
                    combatCallList=[];
                } else {

                    //所有视频通道
                    combatVideoList = msg.filter(function (item) {
                        return item.channelType !== 1;
                    });
                    //所有音频通道
                    combatCallList = msg.filter(function (item) {
                        return item.channelType === 1;
                    });

                    //设置通道下拉
                    securityVideoSeparate.setSelectOption(combatVideoList);
                }
            }
        })
    },
    /**
     *设置通道下拉
     */
    setSelectOption:function(combatVideoList){
        var html = '<option value="0">所有</option>';
        var wrap = $('#channel');

        combatVideoList.forEach(function(item){
            var channel = item.logicChannel;
            html += '<option value='+channel+'>'+channel+'</option>';
        });

        wrap.html(html);
    },
    /**
     * 订阅实时视频
     */
    sendParamByBatch:function () {
        if (!combatVideoList || combatVideoList.length === 0){
            layer.msg(checkedBrand+'未设置通道号');
            return;
        }
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
        if (subscribeInfo.channels.length > 0) {
            $('#videoSelect').removeClass('active').addClass('active');
            json_ajax('POST', '/clbs/v/monitoring/audioAndVideoParameters/' + combatVideoList[0].vehicleId, 'json', true, null, function (result) {
                if(result.success) {
                    videoPlayList = combatVideoList;
                    securityVideoSeparate.openTerminalVideo(combatVideoList, result.obj);
                }
            });
        }
    },
    /**
     * 视频播放应答订阅
     */
    videoPlayAnswerSubscribe: function () {
        setTimeout(function () {
            if (webSocket.conFlag) {
                webSocket.subscribe(headers, "/user/topic/video/realtime", function (data) {
                    securityVideoSeparate.videoStartPlay($.parseJSON(data.body));
                }, null, null);
            } else {
                securityVideoSeparate.videoPlayAnswerSubscribe();
            }
        }, 2000);
    },
    videoStartPlay: function (body) {
        if (body) {
            videoPlayList = body;
            securityVideoSeparate.openTerminalVideo(body)
        }
    },
    /*---------------------------------对讲-----------------------------------*/
    /**
     * 对讲事件
     * @param riskId 对讲的风险的UUID
     * @param riskNumber 对讲的风险的编号
     * @param warningTime 对讲的风险的报警时间,格式为 yy-MM-DD HH:MM:ss
     */
    callOrder: function(riskId, riskNumber, warningTime){
        var self =$('#callSelect');

        if (!riskId || !riskNumber || !warningTime) {
            console.error('风险ID、风险编号或报警时间为空，对讲失败');
            return;
        }

        if(combatCallList === '' && !self.hasClass('active')){
            layer.msg('该监控对象没有对讲通道,不支持对讲功能');
            return;
        }
        if (combatCallList.length === 0) {
            layer.msg('当前监控对象没有音频通道号');
            return;
        }

        if(!self.hasClass('active')){
            self.addClass('active');
            json_ajax('POST', '/clbs/v/monitoring/audioAndVideoParameters/' + combatCallList[0].vehicleId, 'json', true, null, function (message) {
                if(message.success) {
                    securityVideoSeparate.callPlay(message.obj);//打开
                }
            });
        }else {
            securityVideoSeparate.callClose(riskId, warningTime);//关闭
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
    callClose: function () {
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
    openTerminalVideo:function (info, nodes) {
        // 根据通道数量显示视频窗口数
        if (info.length <= 4 && !($("#videoFour").hasClass("video-check"))){
            $('#videoFour').trigger('click')
        }
        if (info.length > 4 && info.length <= 6 && !($("#videoSix").hasClass("video-check"))){
            $('#videoSix').trigger('click')
        }
        if (info.length > 6 && info.length <= 9 && !($("#videoNine").hasClass("video-check"))){
            $('#videoNine').trigger('click')
        }
        if (info.length > 9 && info.length <= 10 && !($("#videoTen").hasClass("video-check"))){
            $('#videoTen').trigger('click')
        }
        if (info.length > 10 && info.length <= 16 && !($("#videoSixteen").hasClass("video-check"))){
            $('#videoSixteen').trigger('click')
        }
        if (info.length > 16 && !($("#videoSixteen").hasClass("video-check"))){
            info.length = 16;
            $('#videoSixteen').trigger('click')
        }

        createMseVideoObjArr = [];//实例化对象的数组

        var protocol = 'ws://';
        if (document.location.protocol === 'https:') {
            protocol = 'wss://';
        }
        for (var i = 0; i < info.length; i++) {
            var value = info[i];
            var domId = 'v_' + i + '_Source';

            var url = protocol + videoRequestUrl + ':' + videoRequestPort + '/' + value.mobile + '/' + value.physicsChannel + '/0';

            var subscribeData = {
                domId:domId,
                vehicleId: value.vehicleId, //车id
                simcardNumber: value.mobile, //终端手机卡号
                channelNumber: JSON.stringify(value.physicsChannel), //终端通道号
                sampleRate: nodes.samplingRateStr || '8000', //音频采样率
                channelCount: nodes.vocalTractStr || '0', //音频声道数
                audioFormat: nodes.audioFormatStr, //音频编码
                playType: 'REAL_TIME', // 播放类型(实时 REAL_TIME，回放 TRACK_BACK，对讲 BOTH_WAY，监听 UP_WAY，广播 DOWN_WAY)
                dataType: '0', //播放数据类型(0：音视频，1：视频，2：双向对讲，3：监听，4：中心广播，5：透传)
                userID: nodes.userUuid, //用户ID
                deviceID: nodes.deviceId, //终端ID
                streamType: JSON.stringify(value.streamType), //码流类型(0：主码流，1：子码流)
                deviceType: nodes.deviceType,
            };

            var createMseVideo = new RTPMediaPlayer(
                {
                    domId: domId,
                    url: url,
                    data: subscribeData,
                    imgSrc: false,
                    //异常
                    onMessage: function($data, $msg) {
                        var code = JSON.parse($msg).data.msgBody.code;
                        var domId = $data.domId;
                        switch (code) {
                            case -1004:
                                $('#' + domId).attr('style', 'width:100%; height:100%;background-image: url(/clbs/resources/img/videoPrompt/video4.png)!important;');
                                break;
                            case -1005:
                                $('#' + domId).attr('style', 'width:100%; height:100%;background-image: url(/clbs/resources/img/videoPrompt/video7.png)!important;');
                                break;
                            case -1006:
                                layer.msg('终端未响应,请重新订阅', { time: 2000 });
                                $('#' + domId).attr('style', 'width:100%; height:100%;background-image: url(/clbs/resources/img/videoPrompt/video9.png)!important;');
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
                        console.log('开始播放');
                        securityVideoSeparate.videoPlaySuccess($state, $this);
                    },
                }
            );

            var obj = {};
            obj.vehicleId = value.vehicleId;
            obj.channelNumber = value.physicsChannel;
            obj.createMseVideoFun = createMseVideo;
            obj.domId = domId;

            createMseVideoObjArr.push(obj)
        }
    },
    /**
     * 关闭视频
     */
    closeTerminalVideo:function () {
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
     */
    videoPlaySuccess:function (state, $this) {
        // 视频双击事件，视频方法为1屏
        $('.video-box').off('dblclick').on('dblclick', securityVideoSeparate.videoFullShow);

        //云台下发
        var vehicleId = state.vehicleId;
        var num = state.channelNum;
        var videoType = state.channelType;
        var videoId = state.domId;

        // 云台是否连接
        var connectionState;
        for (var i = 0; i < videoChannelsInfo.length; i++) {
            var value = videoChannelsInfo[i];
            if (value.logicChannel == num) {
                connectionState = value.connectionFlag;
                break;
            }
        }

        $('#' + videoId).attr('vehicle-id', vehicleId)
            .attr('channel-num', num).attr('channel-type', videoType)
            .attr('connection-state', connectionState);
        $('#' + videoId).parent().append('<div class="video-modal ' + vehicleId + '"></div>');
        $('.video-modal').off('click').on('click', securityVideoSeparate.videoModuleClickFn);
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
     * 视频画面设置
     */
    videoDimmingFn: function () {
        if ($("#videoDimming").hasClass("video-dimming")) {
            $("#videoDimming").removeClass("video-dimming");
            $("#videoDimming").addClass("video-dimming-check");
            $("#videoScreenControl").modal("show");
            $("#videoScreenControl .modal-dialog").css("top", (winHeight - 268) / 2 + "px");//位置
        } else {
            $("#videoDimming").removeClass("video-dimming-check");
            $("#videoDimming").addClass("video-dimming");
        }
    },
    doVideoScreenControl: function () {
        var saturate = $("#saturationVal").val() / 51;
        var hue = $("#chromaVal").val();
        var brightness = $("#brightnessVal").val() / 255;
        var contrast = $("#contrastVal").val() / 51;

        var filters = "saturate(" + saturate + ") hue-rotate(" + hue + "deg) brightness(" + brightness + ") contrast(" + contrast + ")";

        $("video").css('-webkit-filter', filters);
        $("video").css('-ms-filter', filters);
        $("video").css('-moz-filter', filters);
        $("video").css('-o-filter', filters);
        $("video").css('filter', filters);
    },
    videoScreenControlFn: function () {
        //视频画面控制start
        $('.nsBrightness').nstSlider({
            "left_grip_selector": ".leftGrip", "value_changed_callback": function (cause, leftValue) {
                $('#brightnessVal').val(leftValue);
                securityVideoSeparate.doVideoScreenControl();
            }
        });//亮度

        $('.nsChroma').nstSlider({
            "left_grip_selector": ".leftGrip", "value_changed_callback": function (cause, leftValue) {
                $('#chromaVal').val(leftValue);
                securityVideoSeparate.doVideoScreenControl();
            }
        });//色度

        $('.nsContrast').nstSlider({
            "left_grip_selector": ".leftGrip", "value_changed_callback": function (cause, leftValue) {
                $('#contrastVal').val(leftValue);
                securityVideoSeparate.doVideoScreenControl();
            }
        });//对比度

        $('.nsSaturation').nstSlider({
            "left_grip_selector": ".leftGrip", "value_changed_callback": function (cause, leftValue) {
                $('#saturationVal').val(leftValue);
                securityVideoSeparate.doVideoScreenControl();
            }
        });//饱和度

        $('.nsVolume').nstSlider({
            "left_grip_selector": ".leftGrip", "value_changed_callback": function (cause, leftValue) {
                $('#volumeVal').val(leftValue);
                var videoList = createMseVideoObjArr;

                for (var i = 0; i < videoList.length; i++) {
                    var item = videoList[i].createMseVideoFun;
                    item.setAudioVoice(leftValue / 100);
                }
            }
        });//音量
        //视频画面控制end

        //云台控制start
        $('.haeundaeNstSlider').nstSlider({
            "left_grip_selector": ".leftGrip", "value_changed_callback": function (cause, leftValue) {
                securityVideoSeparate.cloudSilderValueFn(leftValue);
            }
        });//云台速度滑块
        $('#haeundaeModal').on('hidden.bs.modal', securityVideoSeparate.cloudStationCloseFn);//云台窗口关闭后执行函数
        $("#haeundaeLight").on("click", securityVideoSeparate.haeundaeLightCheckedFn);//灯光开关监听函数
        $("#haeundaeWipers").on("click", securityVideoSeparate.haeundaeWipersCheckedFn);//雨刷开关监听函数
        $("#zoomPlus,#zoomLess,#doublePlus,#doubleLess,#aperturePlus,#apertureLess").on("click", securityVideoSeparate.zoomDoubleApertureFn);//变焦 变倍 光圈控制函数
        $("#haeundaeLeftTop,#haeundaeTop,#haeundaeRightTop,#haeundaeLeft,#haeundaeCenter,#haeundaeRight,#haeundaeLeftBottom,#haeundaeBottom,#haeundaeRightBottom").on("click", securityVideoSeparate.haeundaeCameraPathFn);//云台控制摄像头方向函
        //云台控制end
    },
    listenVideoScreenControlOff: function () {
        $('#videoScreenControl').on('hidden.bs.modal', function (e) {
            $("#videoDimming").removeClass("video-dimming-check");
            $("#videoDimming").addClass("video-dimming");
        });
    },
    /**
     * 云台控制
     */
    cloudStationFn: function (self) {
        // $("#haeundaeModal").modal("show");
        if ($("#videoplayer div").hasClass("this-click")) {
            var connectState = $('#connectState').val();

            if (connectState == 1) { // 云台连接打开
                if (self.hasClass("video-yun-sett")) {
                    //图标改变
                    self.removeClass("video-yun-sett").addClass("video-yun-sett-check");
                    $("#haeundaeModal").modal("show");
                    //云台宽度
                    $("#haeundaeModal>.modal-dialog").css("width", "500px");
                    // $("#haeundaeModal .modal-dialog").css("top", (winHeight - 308) / 2 + "px");//位置
                    $("#haeundaeModal .modal-dialog").css("top", (winHeight) / 2 + "px");//位置
                } else {
                    //图标改变
                    self.removeClass("video-yun-sett-check").addClass("video-yun-sett");
                }
            } else {
                layer.msg('该通道号云台未连接');
            }
        } else {
            layer.msg("请先选中一个音视频窗口");
        }
    },
    //视频点击开启声音函数
    videoModuleClickFn: function () {
        $("#videoplayer").find("div").removeClass("this-click");
        $(this).addClass("this-click");

        //获取当前点击video通道号相关信息
        var channelNum = $(this).siblings('video').attr('channel-num');
        var connectState = $(this).siblings('video').attr('connection-state');
        if (channelNum == undefined) {
            haeundaeChannelNum = "";
        } else {
            haeundaeChannelNum = channelNum;
        }

        var vehicle_Id = checkVehicleInfo.vid;
        $('#haeundaeBrand').text(checkVehicleInfo.brand + '-' + channelNum);
        $('#connectState').attr('value', connectState);
        securityVideoSeparate.openChannelVoice(vehicle_Id, channelNum);
    },
    // 开启对应通道号声音
    openChannelVoice: function (vehicleId, channelNum) {
        if(createChannelVoice != null){
            for(var i=0;i<createMseVideoObjArr.length;i++){
                var item = createMseVideoObjArr[i];

                if (item.vehicleId === createChannelVoice.vehicleId && item.channelNumber === createChannelVoice.channelNumber){
                    item.createMseVideoFun.closeVideoVoice();
                }
            }
        }else{
            for(var i=0;i<createMseVideoObjArr.length;i++){
                var item = createMseVideoObjArr[i];

                if (item.vehicleId === vehicleId && item.channelNumber === channelNum){
                    item.createMseVideoFun.openVideoVoice();
                    createChannelVoice = {
                        vehicleId: vehicleId,
                        channelNumber: channelNum
                    };
                }
            }
        }
    },
    //云台关闭
    cloudStationCloseFn: function () {
        $("#videoYunSett").removeClass("video-yun-sett-check").addClass("video-yun-sett");
        $("#haeundaeModal").modal("hide");
    },
    //云台速度滑块控制函数
    cloudSilderValueFn: function (_thisVal) {
        //获取云台速度变倍参数值
        speedZoomParameter = _thisVal;
    },
    //灯光
    haeundaeLightCheckedFn: function () {
        var control;
        //开启
        if ($(this).is(":checked")) {
            layer.msg("灯光开启");
            control = 1;
        } else {
            layer.msg("灯光关闭");
            control = 0;
        }
        if (control != null && control != undefined) {
            var vehicleId = checkVehicleInfo.vid;
            var data = {
                "vehicleId": vehicleId,
                "channelNum": haeundaeChannelNum,
                "type": 4,
                "control": control
            };
            securityVideoSeparate.haeundaeCallback(data);
        }
    },
    //雨刷
    haeundaeWipersCheckedFn: function () {
        var control;
        //开启
        if ($(this).is(":checked")) {
            layer.msg("雨刷开启");
            control = 1;
        } else {
            layer.msg("雨刷关闭");
            control = 0;
        }
        if (control != null && control != undefined) {
            var vehicleId = checkVehicleInfo.vid;
            var data = {
                "vehicleId": vehicleId,
                "channelNum": haeundaeChannelNum,
                "type": 3,
                "control": control
            };
            securityVideoSeparate.haeundaeCallback(data);
        }
    },
    //变焦 变倍 光圈
    zoomDoubleApertureFn: function () {
        var control; // 0:调大 1：调小
        var type; // 1 调焦 2 调光 5 变倍
        var clickId = $(this).attr("id");
        //变焦 加
        if (clickId === "zoomPlus") {
            layer.msg("变焦 加");
            type = 1;
            control = 0;

        }
        //变焦 减
        else if (clickId === "zoomLess") {
            layer.msg("变焦 减");
            type = 1;
            control = 1;
        }
        //变倍 加
        else if (clickId === "doublePlus") {
            layer.msg("变倍 加");
            type = 5;
            control = 0;
        }
        //变倍 减
        else if (clickId === "doubleLess") {
            layer.msg("变倍 减");
            type = 5;
            control = 1;
        }
        //光圈  加
        else if (clickId === "aperturePlus") {
            layer.msg("光圈  加");
            type = 2;
            control = 0;
        }
        //光圈 减
        else if (clickId === "apertureLess") {
            layer.msg("光圈 减");
            type = 2;
            control = 1;
        }
        if (type != undefined && control != undefined) {
            var vehicleId = checkVehicleInfo.vid;
            var data = {
                "vehicleId": vehicleId,
                "channelNum": haeundaeChannelNum,
                "type": type,
                "control": control
            };
            securityVideoSeparate.haeundaeCallback(data);
        }
    },
    //摄像头方向
    haeundaeCameraPathFn: function () {
        var direction = [];
        var _thisId = $(this).attr("id");
        //左上  1 & 3
        if (_thisId === "haeundaeLeftTop") {
            // 下发两次 上和左
            direction = [1, 3];
        }
        //上 1
        else if (_thisId === "haeundaeTop") {
            direction = [1];
        }
        //右上 1 & 4
        else if (_thisId === "haeundaeRightTop") {
            direction = [1, 4];
        }
        //左 3
        else if (_thisId === "haeundaeLeft") {
            direction = [3];
        }
        //中(停止) 0
        else if (_thisId === "haeundaeCenter") {
            direction = [0];
        }
        //右 4
        else if (_thisId === "haeundaeRight") {
            direction = [4];
        }
        //左下 2 & 3
        else if (_thisId === "haeundaeLeftBottom") {
            direction = [2, 3];
        }
        //下 2
        else if (_thisId === "haeundaeBottom") {
            direction = [2];
        }
        //右下 2 & 4
        else if (_thisId === "haeundaeRightBottom") {
            direction = [2, 4];
        }

        if (direction.length > 0) {
            /**
             * 0:云台旋转 0x9301
             * 1:云台调整焦距控制 0x9302
             * 2:云台调整光圈控制 0x9303
             * 3:云台雨刷控制 0x9304
             * 4:红外补光控制 0x9305
             * 5:云台变倍控制 0x9306
             * @type {number}
             */
            var vehicleId = checkVehicleInfo.vid;
            for (var i = 0; i < direction.length; i++) {
                var control = direction[i]; // 方向

                var data = {
                    "vehicleId": vehicleId,
                    "channelNum": haeundaeChannelNum,
                    "type": 0,
                    "speed": speedZoomParameter,
                    "control": control
                };
                securityVideoSeparate.haeundaeCallback(data);
            }
        }
    },
    haeundaeCallback: function(data){
        json_ajax("post", "/clbs/cloudTerrace/sendParam", "json", false, data, function (data) {
            if (data.success) {
            }
        });
    },
};


$(function () {
    securityVideoSeparate.videoScreenControlFn();
    securityVideoSeparate.listenVideoScreenControlOff();
    // securityVideoSeparate.videoPlayAnswerSubscribe();

    $('#videoFour,#videoSix,#videoNine,#videoTen,#videoSixteen').on('click',securityVideoSeparate.videoSeparatedFn);
    $("#videoFullScreen").on("click", securityVideoSeparate.videoFullScreenFn);//音视频模块全屏显示
});
// })(window,$)