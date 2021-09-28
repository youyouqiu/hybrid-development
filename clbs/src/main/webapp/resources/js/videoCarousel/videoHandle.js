var VideoHandle = function (options, dependency) {
    this.dependency = dependency;

    this.playTimer = null;// 轮播定时器
    this.initStatus = true;// 是否是首次加载
    this.playTimeRemaining = 30;// 当前视频播放剩余时间
    this.playModel = '2';// 当前视频轮播模式(1:单车,2:多车)
    this.isPlayIndex = 0;// 已播放队列索引
    this.createVideoMap = null;// 已创建的video视频对象集合
    this.playInfoArr = [];
    this.nextScreen = false;// 是否是切换下一屏
    this.videoRequestParam = null; //视频请求参数
};

VideoHandle.prototype.init = function () {
    var _this = this;
    this.getVideoSetting();
    this.videoScreenControlFn();

    this.createVideoMap = new Map();

    // 视频播放或暂停
    $('#videoPlayBtn').on('click', this.videoPlayFun.bind(this));
    // 下一屏
    $('#videoToggleBtn').on('click', this.videoNextScreen.bind(this));
    // 视频参数控制
    $('#videoSettingBtn').on('click', this.videoSetting.bind(this));
    // 全屏效果
    $('#videoFullScreenBtn').on('click', function () {
        var screenView = $('#multiWindowContainer')[0];
        _this.fullScreen(screenView);
    });

    // 单个窗口解除或者锁定
    $('#multiWindowContainer').on('click', '.lock-icon', this.oneWindowLock.bind(this));
    // 全部窗口解除或者锁定
    $('#videoLockBtn').on('click', this.allWindowLock.bind(this));
    // 窗口截图
    $('#multiWindowContainer').on('click', '.snapshots-icon', this.videoPrintScreen.bind(this));

    $('#carouselTime').on('change', this.playTimeChange.bind(this));
    $('.carouselModel').on('change', this.playModelChange.bind(this));
    // 在页面刷新或离开页面时,存储用户修改过的视频播放参数
    document.getElementsByTagName("body").onbeforeunload = this.setVideoSetting.bind(this);
};
// 封装map集合
VideoHandle.prototype.mapVehicle = function () {
    this.elements = [];
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
        this.elements = [];
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
                if (this.elements[i].key === _key) {
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
                if (this.elements[i].key === _key) {
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
                if (this.elements[i].key === _key) {
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
                if (this.elements[i].value === _value) {
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
        var arr = [];
        for (var i = 0, len = this.elements.length; i < len; i++) {
            arr.push(this.elements[i].value);
        }
        return arr;
    };
    //获取MAP中所有KEY的数组（ARRAY）
    this.keys = function () {
        var arr = [];
        for (var i = 0, len = this.elements.length; i < len; i++) {
            arr.push(this.elements[i].key);
        }
        return arr;
    };
};

/**
 * 轮播时长切换
 * */
VideoHandle.prototype.playTimeChange = function () {
    this.playTimeRemaining = $('#carouselTime').val();
};

/**
 * 轮播模式切换
 * */
VideoHandle.prototype.playModelChange = function () {
    this.playModel = $('.carouselModel:checked').val();
};

/**
 * 轮播倒计时
 * */
VideoHandle.prototype.playCountdown = function () {
    if (this.playTimeRemaining > 0) {
        this.playTimeRemaining = this.playTimeRemaining - 1;
    } else {// 倒计时结束,开始下一屏播放
        this.playTimeRemaining = $('#carouselTime').val();
        this.nextScreen = false;
        this.videoStartPlay();
    }
    $('#timeRemaining').html(this.playTimeRemaining + 's');
};
/**
 * 轮播控制
 * */
VideoHandle.prototype.videoPlayFun = function () {
    try {
        if (!RTPMediaPlayer) {
            console.log('RTPMediaPlayer', RTPMediaPlayer)
        }
    } catch (e) {
        layer.msg('本浏览器暂不支持视频轮播功能!');
        return;
    }
    var videoPlayBtn = $('#videoPlayBtn');
    this.isPlayIndex = 0;
    if (videoPlayBtn.hasClass('active')) {// 暂停
        this.videoStopPlay();
    } else {// 播放
        var dataDependency = this.dependency.get('data');
        var videoPlayArray = dataDependency.getVideoPlayArray();// 轮播队列
        var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
        var checkNodes = treeObj.getNodesByFilter(function (node) {
            return node.type !== "group" && node.type !== "assignment" && node.checked;
        }, false);

        var runVidArray = dataDependency.getRunVidArray();
        var stopVidArray = dataDependency.getStopVidArray();

        checkNodes.map((item)=>{
            //保存视频抽查统计记录
            json_ajax(
                'POST',
                '/clbs/realTimeVideo/video/saveVideoSpotCheckRecord',
                'json',
                true,
                {vehicleId: item.id}
            );

            if (runVidArray.indexOf(item.id) == -1 && stopVidArray.indexOf(item.id) == -1) {
                //保存视频巡检记录
                json_ajax(
                    'POST',
                    '/clbs/realTimeVideo/video/saveVideoInspectionRecord',
                    'json',
                    true,
                    {
                        vehicleId: item.id,
                        playStatus: 1,
                        failReason: 1,
                    }
                )
            }
        });


        if (videoPlayArray.length === 0) {
            if (checkNodes.length !== 0) {
                layer.msg('当前勾选的监控对象未设置通道号,请重新勾选');
            } else {
                layer.msg('请先勾选监控对象');
            }
            return;
        }

        var videoPlayArray = dataDependency.getVideoPlayArray();// 轮播队列
        var runVidArray = dataDependency.getRunVidArray();
        var stopVidArray = dataDependency.getStopVidArray();
        var playStatus = false;
        for (var i = 0; i < videoPlayArray.length; i++) {
            var vehicleId = videoPlayArray[i].vehicleId;
            if (runVidArray.indexOf(vehicleId) !== -1 || stopVidArray.indexOf(vehicleId) !== -1) {
                playStatus = true;
                break;
            }
        }
        if (!playStatus) {
            layer.msg('轮播队列中全为离线监控对象,不可轮播');
            return;
        }
        if (this.initStatus) {// 首次播放视频时提示
            this.initStatus = false;
            layer.msg('轮播过程中会为您自动过滤离线监控对象');
        }

        $('#multiWindowContainer .window').removeClass('disabled');
        $('.videoBtn').prop('disabled', false);
        $('.disabledMark').show();
        videoPlayBtn.html('停止 (<span id="timeRemaining">' + this.playTimeRemaining + 's</span>)').addClass('active');
        this.nextScreen = false;
        this.videoStartPlay();
    }
};
// 组装轮播数据
VideoHandle.prototype.setPlayInfo = function () {
    var dataDependency = this.dependency.get('data');
    var startIndex = this.isPlayIndex;// 视频已播放位置索引
    var videoStream = dataDependency.getVideoScreenControl().videoStream;// 播放码流
    var videoPlayArray = dataDependency.getVideoPlayArray();// 轮播队列
    var allWindow = $('#multiWindowContainer .window');// 所有视频窗口
    var lockWindow = dataDependency.getLockWindow();// 已经锁定的视频窗口

    var runVidArray = dataDependency.getRunVidArray();
    var stopVidArray = dataDependency.getStopVidArray();
    console.log(videoPlayArray, 'videoPlayArray')
    if (runVidArray.length === 0 && stopVidArray.length === 0) {
        return [];
    }

    // 组装可播放的视频窗口
    var canPlayWindow = [];
    for (var i = 0; i < allWindow.length; i++) {
        if (lockWindow.indexOf(i) === -1) {
            canPlayWindow.push(i);
        }
    }

    var windowLen = canPlayWindow.length;
    var arrLen = videoPlayArray.length;
    var playInfoArr = [];
    var windowIndex = 0;
    var mapLen = 0;// 遍历次数,当整个轮播队列都循环完毕,依然没组装好当前轮播队列时,直接返回空数组
    if (this.playModel === '2') {// 多车模式
        for (var i = startIndex; i < arrLen; i++) {
            mapLen++;
            var item = videoPlayArray[i];
            var videoKey = item.vehicleId + '-' + item.channelNum;
            if (playInfoArr.length === windowLen || (i == startIndex && playInfoArr.length !== 0)) {
                this.isPlayIndex = i;
                break;
            }
            if (!this.createVideoMap.has(videoKey)) {
                if (videoStream !== null) {
                    item.streamType = videoStream;
                }
                if (runVidArray.indexOf(item.vehicleId) !== -1 || stopVidArray.indexOf(item.vehicleId) !== -1) {
                    item.playWindowIndex = canPlayWindow[windowIndex];// 该视频播放时的窗口索引
                    windowIndex += 1;
                    playInfoArr.push(item);
                }
            }
            if (mapLen === arrLen) {
                this.isPlayIndex = 0;
                break;
            }
            if (i === arrLen - 1) {
                if (mapLen !== arrLen && playInfoArr.length !== windowLen) {// 已循环到队列最后,重新从队列开头开始轮播
                    i = -1;
                } else {
                    this.isPlayIndex = 0;
                }
            }
        }
    } else {// 单车模式
        var currentMonitorId = null;
        for (var i = startIndex; i < arrLen; i++) {
            var item = videoPlayArray[i];
            var vehicleId = item.vehicleId;
            var videoKey = vehicleId + '-' + item.channelNum;
            if (this.createVideoMap.has(videoKey)) continue;

            if (runVidArray.indexOf(vehicleId) === -1 && stopVidArray.indexOf(vehicleId) === -1) {
                continue;
            } else if (currentMonitorId === null) {
                startIndex = i;
                currentMonitorId = vehicleId;
            }

            if (item.vehicleId !== currentMonitorId || playInfoArr.length === windowLen) {
                this.isPlayIndex = i;
                break;
            }
            if (runVidArray.indexOf(vehicleId) !== -1 || stopVidArray.indexOf(vehicleId) !== -1) {
                if (videoStream !== null) {
                    item.streamType = videoStream;
                }
                item.playWindowIndex = canPlayWindow[i - startIndex];// 该视频播放时的窗口索引
                playInfoArr.push(item);
            }
            if (i === arrLen - 1) {
                this.isPlayIndex = 0;
            }
        }
    }
    return playInfoArr;
};
// 暂停轮播
VideoHandle.prototype.videoStopPlay = function () {
    var videoBackground = window.localStorage.getItem('videoBg');
    clearInterval(this.playTimer);
    this.playTimer = null;
    this.playTimeRemaining = $('#carouselTime').val();
    for(var i= 0; i< $("video").length; i++) {
        var vehcileId = 'videoSource_' + i;
        $("#"+ vehcileId).replaceWith('<video id="'+ vehcileId +'" width="100%" height="100%" ' +
            'style="background-image: url('+ videoBackground +')"></video>');
        $("#msg_videoSource_" + i).html('');
    }

    $('#multiWindowContainer .window').removeClass('lock').addClass('disabled');
    $('.lock-icon').attr('title', '锁定');
    $('.videoBtn').prop('disabled', true);
    $('#videoLockBtn').attr('title', '锁定所有窗格').removeClass('active');
    $('.disabledMark').hide();
    $('#videoPlayBtn').html('开始轮播').removeClass('active');

    // 解锁已锁定的视频窗口
    var dataDependency = this.dependency.get('data');
    dataDependency.setLockWindow([]);
    this.closeCurrentVideo();
};
// 开始轮播,发送视频订阅请求
VideoHandle.prototype.videoStartPlay = function () {
    $('.playInfo').html('').hide();
    this.closeCurrentVideo();
    this.playInfoArr = this.setPlayInfo();
    if (this.playInfoArr.length === 0) {
        layer.msg('队列中无在线监控对象可轮播');
        this.videoStopPlay();
        return;
    }
    if ($('#videoLockBtn').hasClass('active')) {
        layer.msg('所有窗格被锁定,无法切换');
        return;
    }
    this.sendVideoPlay();
};
// 下发开始播放指令
VideoHandle.prototype.sendVideoPlay = function () {
    if (webSocket.conFlag) {
        var channelMap = new Map();
        this.playInfoArr.forEach(function (value) {
            if (!channelMap.has(value.vehicleId)) {
                channelMap.set(value.vehicleId, {
                    mobile: value.mobile,
                    channels: []
                });
            }
            var channelInfo = channelMap.get(value.vehicleId).channels;
            channelMap.get(value.vehicleId);
            channelInfo.push({
                number: value.channelNum,
                streamType: value.streamType,
                channelType: value.channelType,
            });
        });
        var param = [];
        channelMap.forEach(function (value, key) {
            param.push({
                "vehicleId": key,
                "mobile": value.mobile,
                "channels": value.channels,
            });
        });

        var _this = this;

        /***
         * 请求获取实时视频订阅参数
         */
        var nodes = [];
        for(var i = 0; i < param.length; i++) {
            var item = param[i];
            $.ajax({
                type: "POST",
                url: '/clbs/v/monitoring/audioAndVideoParameters/' + item.vehicleId,
                dataType: "json",
                async: false,
                data: null,
                success: function (data) {
                    nodes.push(data.obj);
                },
            });
        }

        if (!_this.playTimer || _this.nextScreen) {
            _this.playTimer = setInterval(_this.playCountdown.bind(_this), 1000);
        }
        _this.subscribeVideoDataAssemble(nodes, _this.playInfoArr, true);

    } else {
        setTimeout(function () {
            this.sendVideoPlay();
        }.bind(this), 1000);
    }
}
// 切换下一屏轮播
VideoHandle.prototype.videoNextScreen = function () {
    clearInterval(this.playTimer);
    this.playTimeRemaining = $('#carouselTime').val();
    $('#timeRemaining').html(this.playTimeRemaining + 's');
    this.nextScreen = true;
    this.videoStartPlay();
};
// 关闭当前批次正在播放的视频
VideoHandle.prototype.closeCurrentVideo = function () {
    var dataDependency = this.dependency.get('data');
    var lockWindow = dataDependency.getLockWindow();// 已经锁定的视频窗口
    var videoStream = dataDependency.getVideoScreenControl().videoStream;// 播放码流

    // 关闭视频流socket
    if (this.createVideoMap) {
        var channelMap = new Map();
        this.createVideoMap.forEach(function (value, key, map) {
            var videoObj = value.videoObj;
            var videoDataList = value.videoDataList;
            let vehicleId = videoDataList.vehicleId;
            var playWindowIndex = videoDataList.playWindowIndex;
            if (lockWindow.indexOf(playWindowIndex) === -1) {
                videoObj.closeSocket();
                if (!channelMap.has(vehicleId)) {
                    channelMap.set(vehicleId, []);
                }
                var channelList = channelMap.get(vehicleId);
                channelList.push(videoDataList.channelNum);
                map.delete(key);
            }
        });
        var closeData = [];
        channelMap.forEach(function (value, key) {
            closeData.push({
                vehicleId: key,
                channels: value,
            });
        });
        if (closeData.length > 0) {
            webSocket.send("/app/video/realtime/close", headers, closeData);
        }
    }
};
// 视频订阅接口数据组装
VideoHandle.prototype.subscribeVideoDataAssemble = function (nodes, info, playStatus) {
    var _this = this;
    if (!nodes) {
        layer.msg('视频播放指令下发异常');
        return;
    }
    // var offlineNum = 0;
    var protocol = 'ws://';
    if (document.location.protocol === 'https:') {
        protocol = 'wss://';
    }

    $('#multiWindowContainer video').siblings('img,div').remove();
    for (var i = 0; i < info.length; i++) {
        var value = info[i];
        var vehicleId = value.vehicleId;
        var videoDataList = null;

        var sampleRate, channelCount, audioFormat, userID, deviceID, deviceType;

        for ( var j = 0; j < nodes.length; j++) {
            if(nodes[j].monitorId === vehicleId) {
                sampleRate = nodes[j].samplingRateStr;
                channelCount = nodes[j].vocalTractStr;
                audioFormat = nodes[j].audioFormatStr;
                userID = nodes[j].userUuid;
                deviceID = nodes[j].deviceId;
                deviceType = nodes[j].deviceType;
            }

            if(nodes[j].monitorId === vehicleId && nodes[j].channelNum == value.channelNumber) {
                videoDataList = info[i];
                break;
            }
        }
        var videoId = 'videoSource_' + videoDataList.playWindowIndex;
        var videoData = videoDataList;
        videoData.domId = videoId;
        _this.videoMessage('视频请求中...', videoId);

        if (!$.isEmptyObject(videoData)) {
            //待播放窗口添加状态
            $('#' + videoId).attr('data-channel-up', 'true');
            var simcardNumber = value.mobile;
            var channelNumber = value.channelNum;
            var url = protocol + videoRequestUrl + ':' + videoRequestPort + '/' + simcardNumber + '/' + channelNumber + '/0';
            var subscribeData = {
                videoId: videoId,
                vehicleId: vehicleId, //车id
                simcardNumber: simcardNumber, //终端手机卡号
                channelNumber: value.channelNum, //终端通道号
                sampleRate: sampleRate || '8000', //音频采样率
                channelCount: channelCount || '0', //音频声道数
                audioFormat: audioFormat, //音频编码
                playType: 'REAL_TIME', // 播放类型(实时 REAL_TIME，回放 TRACK_BACK，对讲 BOTH_WAY，监听 UP_WAY，广播 DOWN_WAY)
                dataType: '0', //播放数据类型(0：音视频，1：视频，2：双向对讲，3：监听，4：中心广播，5：透传)
                userID: userID, //用户ID
                deviceID: deviceID, //终端ID
                streamType: JSON.stringify(value.streamType), //码流类型(0：主码流，1：子码流)
                deviceType: deviceType,
            };
            // $('#' + videoId).siblings('img,div').remove();
            var videoObj = new RTPMediaPlayer(
                {
                    domId: videoId,
                    url: url,
                    // codingType: audioCode,
                    data: subscribeData,
                    panoramaType: value && value.panoramic == 1 ? 1 : 0,
                    vrImageSrc: '/clbs/resources/img/qj360.png',
                    onMessage: function($data, $msg) {
                        var code = JSON.parse($msg).data.msgBody.code;
                        var videoId = $data.videoId;
                        var channelNumber = $data.channelNumber;
                        var vehicleId = $data.vehicleId;

                        // 保存视频巡检记录
                        if (code !== 0) {
                            json_ajax(
                                'POST',
                                '/clbs/realTimeVideo/video/saveVideoInspectionRecord',
                                'json',
                                true,
                                {
                                    vehicleId: vehicleId,
                                    channelNumber: channelNumber,
                                    playStatus: code === 1001 ? 0 : 1,
                                    failReason: code !== 1001 ? 3 : null,
                                }
                            );
                        }

                        switch (code) {
                            case -1004:
                                _this.videoMessage('终端未响应', videoId);
                                break;
                            case -1005:
                                _this.videoMessage('无音视频内容', videoId);
                                break;
                            case -1006:
                                layer.msg('终端未响应,请重新订阅', { time: 2000 });
                                _this.videoMessage('加载失败', videoId);
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
                        $("#msg_" + $state.videoId).html('');
                        _this.videoPlaySuccess($state, $this);
                    },
                    // socket关闭成功
                    socketCloseFun: function ($state) {
                        _this.videoCloseSuccess($state);
                    },
                }
            );

            var subVideoId = videoData.vehicleId + '-' + videoData.channelNum;
            var obj = {
                "videoDataList": videoDataList,
                "videoObj": videoObj
            };
            this.createVideoMap.set(subVideoId, obj);
        }
    }
    _this.setWindowInfo();
};

VideoHandle.prototype.videoMessage = function (msg, domId) {
    var id = "msg_" + domId;
    if($("#" + id).length == 0){
        var parent = $("#" + domId).parent();
        var h4 = document.createElement('h4');
        h4.id = id;
        $(h4).css({position: 'absolute',bottom: 10, left: '50%',width: '130px', margin: '0 0 0 -65px','text-align':'center', 'font-weight':'bold', 'font-size': '21px'});
        $(h4).html(msg);
        parent.append(h4);
    }else{
        $("#" + id).html(msg);
    }
};

// 渲染当前窗口正在播放的监控对象及通道号
VideoHandle.prototype.setWindowInfo = function () {
    this.createVideoMap.forEach(function (value) {
        var videoDataList = value.videoDataList;
        var windowIndex = videoDataList.playWindowIndex;
        var playView = $('#multiWindowContainer .window:eq(' + windowIndex + ')').find('.playInfo');
        playView.html('当前播放：' + videoDataList.monitorName + '-' + videoDataList.channelNum + '通道').show();
    });
};
// 视频播放成功事件
VideoHandle.prototype.videoPlaySuccess = function (state, $this) {
    var vehicleId = state.vehicleId;
    var num = state.channelNumber;
    var videoType = state.channelType;
    var videoId = state.videoId;

    // 获取对应通道号的云台是否连接
    var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
    var nodes = treeObj.getNodesByParam("id", vehicleId, null);
    var channelNode = [];
    for (var j = 0; j < nodes.length; j++) {
        if (nodes[j].children !== undefined) {
            channelNode = nodes[j].children;
            break;
        }
    }
    var connectionState;
    for (var i = 0; i < channelNode.length; i++) {
        var value = channelNode[i];
        if (value.logicChannel === num) {
            connectionState = value.connectionFlag;
            break;
        }
    }
    // 给已经播放的视频标签添加属性
    $('#' + videoId).attr('vehicle-id', vehicleId)
        .attr('channel-num', num).attr('channel-type', videoType)
        .attr('connection-state', connectionState);
    this.doVideoScreenControl();
    this.setChannelState(vehicleId, [num], true);
};
// 视频关闭成功事件
VideoHandle.prototype.videoCloseSuccess = function (state) {
    var vehicleId = state.vehicleId;
    var num = state.channelNumber;
    this.setChannelState(vehicleId, [num], false);
};
// 关闭视频、清除视频、暂停视频、清除视频、主子码流切换、静音 下发接口、关闭双向对讲
VideoHandle.prototype.videoHandleFun = function (data) {
    var url = '/clbs/realTimeVideo/video/sendVideoParam';
    json_ajax("POST", url, "json", false, data, function (info) {
        if (info.success) { // 下发成功9102操作
            // layer.msg('下发成功');
        } else {
            layer.msg('下发失败');
        }
    });
};
// 视频请求超时重连
VideoHandle.prototype.videoConnectTimeout = function () {
    this.sendVideoPlay();
};
// 设置订阅通道订阅颜色改变
VideoHandle.prototype.setChannelState = function (id, channelArray, status) {
    var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
    var nodes = treeObj.getNodesByParam('id', id, null);
    for (var x = 0; x < nodes.length; x++) {
        var childrenNodes = nodes[x].children;
        if (!!childrenNodes) {
            for (var y = 0; y < childrenNodes.length; y++) {
                if (channelArray.indexOf(childrenNodes[y].logicChannel.toString()) !== -1) {
                    this.channelSubscribeChangeIco(treeObj, childrenNodes[y], status);
                }
            }
        }
    }
};
// 通道号订阅后图标变化
VideoHandle.prototype.channelSubscribeChangeIco = function (treeObj, node, state) {
    if (state) { // 通道号订阅状态
        node.iconSkin = 'btnImage channel-subscribe';
    } else { // 通道号默认状态
        node.iconSkin = 'channelSkin';
    }
    treeObj.updateNode(node);
};
// 主码流、子码流切换
VideoHandle.prototype.changeStreamType = function (streamType) {
    if (this.createVideoMap.size > 0) {
        var orderType = streamType == '0' ? 20 : 21;
        this.createVideoMap.forEach(function (value) {
            var item = value.videoDataList;
            var data = {
                vehicleId: item.vehicleId,
                orderType: orderType,
                channelNum: item.channelNum,
                control: 1,
                closeVideoType: 0,
                changeStreamType: streamType,
                channelType: item.channelType,
                requestType: 0
            };
            this.videoHandleFun(data);
        });
    }
};
/**
 *  视频窗口截图
 */
VideoHandle.prototype.videoPrintScreen = function (e) {
    var _this = $(e.target);
    var screenWindow = _this.closest('.video-box').find('video');
    var vehicleId = screenWindow.attr('vehicle-id');
    var channelNum = screenWindow.attr('channel-num');
    if (!vehicleId) {
        layer.msg('无视频可用于截图');
        return;
    }
    var canvasElement = document.getElementById('canvasForVideo');
    var canvasCtx = canvasElement.getContext('2d');
    canvasCtx.drawImage(screenWindow[0], 0, 0, 524, 400);
    /**
     * 转换成图像
     */
    var formData = new FormData();
    canvasElement.toBlob(function (blob) {
        formData.append('file', blob);
        formData.append('vehicleId', vehicleId);
        formData.append('channelNum', channelNum);
        var url = '/clbs/lkyw/videoCarousel/saveMedia';
        $.ajax({
            url: url,
            data: formData,
            type: "POST",
            dataType: "json",
            cache: false,//上传文件无需缓存
            processData: false,//用于对data参数进行序列化处理 这里必须false
            contentType: false, //必须
            success: function (data) {
                if (data.success) {
                    layer.msg('截图成功,可在多媒体管理中查看图片');
                } else if (data.msg) {
                    layer.msg(data.msg);
                }
            },
        })
    });
};

/**
 * 锁定视频窗口
 **/
// 单窗口
VideoHandle.prototype.oneWindowLock = function (e) {
    var lockBox = $(e.target).closest('.window');
    var isLock = lockBox.hasClass('lock');
    this.videoWindowLock('', !isLock, lockBox.index('.window'));
    if (isLock) {
        lockBox.removeClass('lock');
        lockBox.find('.lock-icon').attr('title', '锁定');
    } else {
        lockBox.addClass('lock');
        lockBox.find('.lock-icon').attr('title', '解除锁定');
    }
};
// 全部窗口
VideoHandle.prototype.allWindowLock = function (e) {
    var _this = $(e.target);
    var isLock = _this.hasClass('active');
    this.videoWindowLock('allLock', !isLock);
    if (isLock) {
        _this.removeClass('active').attr('title', '锁定所有窗格');
    } else {
        _this.addClass('active').attr('title', '解除所有窗格锁定');
    }
};
/**
 * 锁定视频窗口
 *
 * @param lockType('allLock':锁定全部视频窗口,否则锁定单个视频窗口)
 * @param lockStatus:锁定状态
 * @param index:锁定的视频窗口索引
 * */
VideoHandle.prototype.videoWindowLock = function (lockType, lockStatus, index) {
    var dataDependency = this.dependency.get('data');
    var lockWindow = dataDependency.getLockWindow();// 已经锁定的视频窗口
    var allWindow = $('#multiWindowContainer .window');// 所有视频窗口
    var isAllWindowLock = false;
    var oldAllLock = $('#videoLockBtn').hasClass('active');
    if (lockType === 'allLock') {
        var newArr = [];
        for (var i = 0; i < allWindow.length; i++) {
            if (lockStatus) {
                newArr.push(i);
                isAllWindowLock = true;
                $(allWindow[i]).addClass('lock');
                $(allWindow[i]).find('.lock-icon').attr('title', '解除锁定');
            } else {
                $(allWindow[i]).removeClass('lock');
                $(allWindow[i]).find('.lock-icon').attr('title', '锁定');
            }
        }
        dataDependency.setLockWindow(newArr);
    } else {
        var oldIndex = lockWindow.indexOf(index);
        if (lockStatus) {
            if (oldIndex === -1) {
                lockWindow.push(index);
            }
            if (allWindow.length === lockWindow.length) {
                isAllWindowLock = true;
                $('#videoLockBtn').addClass('active').attr('title', '解除所有窗格锁定');
            }
        } else {
            lockWindow.splice(oldIndex, 1);
            $('#videoLockBtn').removeClass('active').attr('title', '锁定所有窗格');
        }
        dataDependency.setLockWindow(lockWindow);
    }
    if (this.playTimer) {
        if (isAllWindowLock) {// 全部窗口被锁定,停止轮播切换
            clearInterval(this.playTimer);
        } else if (oldAllLock) {// 解除窗口锁定,继续视频轮播
            this.playTimer = setInterval(this.playCountdown.bind(this), 1000);
        }
    }
};

/**
 * 视频画面控制
 * */
// 获取视频参数
VideoHandle.prototype.getVideoSetting = function () {
    var _this = this;
    var url = '/clbs/lkyw/videoCarousel/getVideoSetting';
    json_ajax("POST", url, "json", false, null, function (info) {
        if (info !== '') {
            var settingObj = JSON.parse(info);
            var dataDependency = _this.dependency.get('data');
            dataDependency.setVideoScreenControl(settingObj);
        }
    });
};
// 设置视频参数(刷新页面或者离开页面时进行存储)
VideoHandle.prototype.setVideoSetting = function () {
    if (this.playTimer) clearInterval(this.playTimer);
    var dataDependency = this.dependency.get('data');
    var videoScreenControl = dataDependency.getVideoScreenControl();
    dataDependency.setLockWindow([]);
    var param = {
        "setting": JSON.stringify(videoScreenControl)
    };
    var url = '/clbs/lkyw/videoCarousel/videoSet';
    json_ajax("POST", url, "json", true, param, function (info) {
        console.log('存储数据', info);
    });
    this.closeCurrentVideo();
};
// 显示视频画面控制弹窗
VideoHandle.prototype.videoSetting = function () {
    var winHeight = $(window).height(); //window高度
    $("#videoScreenControl .modal-dialog").css("top", winHeight / 2 - 200 + "px");//位置
    $("#videoScreenControl").modal("show");
};
// 滑动条控制视频参数
VideoHandle.prototype.videoScreenControlFn = function () {
    var _this = this;
    var dataDependency = this.dependency.get('data');
    var videoScreenControl = dataDependency.getVideoScreenControl();
    //亮度
    $('.nsBrightness').attr('data-cur_min', videoScreenControl.brightness).nstSlider({
        "left_grip_selector": ".leftGrip", "value_changed_callback": function (cause, leftValue) {
            $('#brightnessVal').val(leftValue);
            _this.doVideoScreenControl();
            videoScreenControl.brightness = leftValue;
            dataDependency.setVideoScreenControl(videoScreenControl);
        }
    });
    //色度
    $('.nsChroma').attr('data-cur_min', videoScreenControl.chroma).nstSlider({
        "left_grip_selector": ".leftGrip", "value_changed_callback": function (cause, leftValue) {
            $('#chromaVal').val(leftValue);
            _this.doVideoScreenControl();
            videoScreenControl.chroma = leftValue;
            dataDependency.setVideoScreenControl(videoScreenControl);
        }
    });
    //对比度
    $('.nsContrast').attr('data-cur_min', videoScreenControl.contrast).nstSlider({
        "left_grip_selector": ".leftGrip", "value_changed_callback": function (cause, leftValue) {
            $('#contrastVal').val(leftValue);
            _this.doVideoScreenControl();
            videoScreenControl.contrast = leftValue;
            dataDependency.setVideoScreenControl(videoScreenControl);
        }
    });
    //饱和度
    $('.nsSaturation').attr('data-cur_min', videoScreenControl.saturation).nstSlider({
        "left_grip_selector": ".leftGrip", "value_changed_callback": function (cause, leftValue) {
            $('#saturationVal').val(leftValue);
            _this.doVideoScreenControl();
            videoScreenControl.saturation = leftValue;
            dataDependency.setVideoScreenControl(videoScreenControl);
        }
    });
    //音量
    $('.nsVolume').attr('data-cur_min', videoScreenControl.volume).nstSlider({
        "left_grip_selector": ".leftGrip", "value_changed_callback": function (cause, leftValue) {
            $('#volumeVal').val(leftValue);
            videoScreenControl.volume = leftValue;
            dataDependency.setVideoScreenControl(videoScreenControl);
            if (_this.createVideoMap) {
                _this.createVideoMap.forEach(function (value) {
                    value.videoObj.setAudioVoice(leftValue / 100);
                });
            }
        }
    });
    // 码流切换
    $('.videoStream').on('change', function () {
        var videoStream = $(this).val();
        videoScreenControl.videoStream = videoStream;
        dataDependency.setVideoScreenControl(videoScreenControl);
        _this.changeStreamType(videoStream);
        layer.msg('码流切换成功,5秒内禁止再次切换');
        $('.videoStream').prop('disabled', true);
        setTimeout(function () {
            $('.videoStream').prop('disabled', false);
        }, 5000)
    });
};
VideoHandle.prototype.doVideoScreenControl = function () {
    var saturate = $("#saturationVal").val() / 51;
    var hue = $("#chromaVal").val();
    var brightness = $("#brightnessVal").val() / 255;
    var contrast = $("#contrastVal").val() / 51;

    var filters = "saturate(" + saturate + ") hue-rotate(" + hue + "deg) brightness(" + brightness + ") contrast(" + contrast + ")";

    var $video = $("video");
    $video.css('-webkit-filter', filters);
    $video.css('-ms-filter', filters);
    $video.css('-moz-filter', filters);
    $video.css('-o-filter', filters);
    $video.css('filter', filters);
};

/**
 * 全屏功能
 * */
VideoHandle.prototype.fullScreen = function (el) {
    var rfs = el.requestFullScreen || el.webkitRequestFullScreen || el.mozRequestFullScreen || el.msRequestFullScreen,
        wscript;
    if (typeof rfs != "undefined" && rfs) {
        rfs.call(el);
        return;
    }
    if (typeof window.ActiveXObject != "undefined") {
        wscript = new ActiveXObject("WScript.Shell");
        if (wscript) {
            wscript.SendKeys("{F11}");
        }
    } else if ("ActiveXObject" in window) {
        document.body.msRequestFullscreen();
    }
};
