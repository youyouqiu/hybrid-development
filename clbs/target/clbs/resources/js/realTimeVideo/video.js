define(['common'], function (Common) {
    var Tree;
    var Table;
    var subscribeVideoModule = new Common.map(); // 视频订阅通道信息集合
    var waitTimeTimeout; // 9101或9102操作计时器
    var createChannelVoice = null; //  创建的通道号声音对象id
    var videoName = ["one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen"]; //视频分隔类样式
    var photoImageFormData; // 抓拍图片对象数据
    var video = {
        init: function () {
            layer.msg('请选择监控对象',{
                time: 0,
                zIndex: 9,
            });
            this.getTimeout();
        },
        /**
         * 视频通道号订阅前的数据组装
         */
        subscribeVideoChannel: function (treeNode) {
            console.log(treeNode, 'treeNode');
            var $this = this;
            var channels = [];
            var monitorId;
            var simcardNumber;
            var monitorName;
            var videoChannelCount = 0; // 音视频通道数据
            /**
             * 判断treeNode是否是通道号节点
             */
            if (treeNode.type === 'channel') {
                monitorId = treeNode.vehicleId;
                /**
                 * 过滤掉音频通道
                 */
                var _id = monitorId + '-' + treeNode.logicChannel;
                if (treeNode.channelType !== 1) {
                    videoChannelCount++;
                    if (!subscribeVideoModule.has(_id)) {
                        channels.push({
                            channelNumber: treeNode.logicChannel,
                            streamType: treeNode.streamType,
                            channelType: treeNode.channelType,
                            vehicleId: monitorId,
                            panoramic: treeNode.panoramic,
                        });
                    }
                }
                var treeObj = $.fn.zTree.getZTreeObj("vTreeList");
                var node = treeObj.getNodeByParam('id', treeNode.pId, null);
                simcardNumber = node.simcardNumber;
                monitorName = node.name;
            } else {
                monitorId = treeNode.id;
                simcardNumber = treeNode.simcardNumber; // 电话号码
                monitorName = treeNode.name; // 车牌号
                var nodes = treeNode.children;
                for (var i = 0; i < nodes.length; i += 1) {
                    var _id = monitorId + '-' + nodes[i].logicChannel;
                    if (nodes[i].channelType !== 1) {
                        videoChannelCount++;
                        if (!subscribeVideoModule.has(_id)) {
                            channels.push({
                                channelNumber: nodes[i].logicChannel,
                                streamType: nodes[i].streamType,
                                channelType: nodes[i].channelType,
                                vehicleId: monitorId,
                                panoramic: nodes[i].panoramic,
                            });
                        }
                    }
                }
            };

            if (treeNode.type !== 'channel' && treeNode.children.length === 0) {
                layer.msg(monitorName + ' 未设置通道号');
                return false;
            };

            /**
             * 监控对象没有音视频和视频类型通道号
             */
            if (videoChannelCount === 0) {
                layer.msg('请设置或双击音视频/视频类型通道号');
                return false;
            }

            if (channels.length > 0) {
                /**
                 * 视频布局样式
                 */
                $this.openVideoArea(channels.length);

                /**
                 * 向数据库请求获取视频订阅的参数
                 */
                json_ajax('POST', '/clbs/v/monitoring/audioAndVideoParameters/' + monitorId, 'json', true, null, function (d) {
                    if (d.success) {
                        var obj = d.obj;
                        $this.connectVideoFlow(channels, simcardNumber, monitorId, obj);
                    }
                });
            }
        },
        /**
         * 视频通道连接视频服务器
         */
        connectVideoFlow: function (channels, simcardNumber, monitorId, audioVideoData) {
            $(".pull-left h4").html('');
            layer.closeAll();
            var $this = this;
            var protocol = 'ws://';
            var userId = $('#userId').val();
            if (document.location.protocol === 'https:') {
                protocol = 'wss://';
            };
            $('#video-module video').siblings('img,div').remove();
            for (var i = 0; i < channels.length; i++) {
                var value = channels[i];
                var vehicleId = value.vehicleId;
                var videoId;
                $('#video-module video').each(function () {
                    if ($(this).attr('data-channel-up') !== 'true') {
                        videoId = this.id;
                        return false;
                    }
                });
                this.videoMessage('视频请求中...', videoId);
                var subscribeData = {
                    vehicleId: vehicleId,
                    channelNum: value.channelNumber,
                    channelType: value.channelType,
                    mobile: simcardNumber,
                    streamType: value.streamType,
                    domId: videoId,
                    sampleRate: audioVideoData.samplingRateStr || 8000,
                    channelCount: audioVideoData.vocalTractStr || 0,
                    audioFormat: audioVideoData.audioFormatStr,
                    playType: 'REAL_TIME',
                    dataType: '0',
                    userID: audioVideoData.userUuid,
                    deviceID: audioVideoData.deviceId,
                    deviceType: audioVideoData.deviceType,
                };

                // 待播放窗口添加状态
                $('#' + videoId).attr('data-channel-up', 'true');
                // $('#' + videoId).siblings('img,div').remove();
                var url = protocol + videoRequestUrl + ':' + videoRequestPort + '/' + simcardNumber + '/' + value.channelNumber + '/0';

                var videoObj = new RTPMediaPlayer(
                    {
                        domId: videoId,
                        url: url,
                        data: subscribeData,
                        panoramaType: value && value.panoramic,
                        vrImageSrc: '/clbs/resources/img/qj360.png',
                        onMessage: function($data, $msg) {
                            $this.videoPlayErrorFn($data, $msg);
                        },
                        socketOpenFun: function ($data, $$this) {
                            /**
                             * socket连接建立成功
                             * 图标提示替换和消息发送
                             */
                            $('#' + $data.domId).css('background-image', 'url(../../../clbs/resources/img/videoPrompt/video3.png) !important');
                            var setting = {
                                vehicleId: $data.vehicleId,                         // 车辆ID
                                simcardNumber: $data.mobile,                        // sim卡号
                                channelNumber: JSON.stringify($data.channelNum),    // 通道号
                                sampleRate: JSON.stringify($data.sampleRate),       // 采样率
                                channelCount: JSON.stringify($data.channelCount),   // 声道数
                                audioFormat: $data.audioFormat,                     // 编码格式
                                playType: $data.playType,                           // 播放类型 实时 REAL_TIME，回放 TRACK_BACK，对讲 BOTH_WAY，监听 UP_WAY，广播 DOWN_WAY
                                dataType: JSON.stringify($data.dataType),           // 数据类型0：音视频，1：视频，2：双向对讲，3：监听，4：中心广播，5：透传
                                userID: $data.userID,                               // 用户ID
                                deviceID: $data.deviceID,                           // 终端ID
                                streamType: JSON.stringify($data.streamType),       // 码流类型0：主码流，1：子码流
                                deviceType: $data.deviceType,
                            };
                            $$this.play(setting);
                        },
                        // 开始播放
                        onPlaying: function ($state, $$this) {
                            $("#msg_" + $state.domId).html('');
                            $this.updateWaitTime();
                            $this.videoPlaySuccess($state, $$this);
                        },
                        // socket关闭成功
                        socketCloseFun: function ($state) {
                            $this.videoCloseSuccess($state);
                        },
                    }
                );
                /**
                 * 视频通道订阅信息添加到集合
                 */
                var id = vehicleId + '-' + value.channelNumber;
                if (!subscribeVideoModule.has(id)) {
                    subscribeVideoModule.set(id, { data: subscribeData, playing: false, mediaPlayer: videoObj })
                }
            }
            /**
             * 改变视频通道号状态
             */
            Tree.tree.updateChannelStatus(monitorId);
        },
        /**
         * 音视频播放异常对应视频窗口提示
         */
        videoPlayErrorFn: function (data, msg) {
            var info = JSON.parse(msg);
            var code = info.data.msgBody.code;
            // 保存视频巡检记录
            if (code !== 0) {
                json_ajax(
                    'POST',
                    '/clbs/realTimeVideo/video/saveVideoInspectionRecord',
                    'json',
                    true,
                    {
                        vehicleId: data.vehicleId,
                        channelNumber: data.channelNum,
                        playStatus: code === 1001 ? 0 : 1,
                        failReason: code !== 1001 ? 3 : null,
                    }
                );
            }

            var domId = data.domId;
            if (code === -1004) { // 终端未响应
                layer.msg('终端未响应，请重新订阅');
                video.videoMessage('终端未响应', domId);
            } else if (code === -1005) { // 无视频资源
                video.videoMessage('无音频内容', domId);
            } else if (code === -1006) {
                video.videoMessage('加载失败', domId);
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
         * 视频播放成功事件
         */
        videoPlaySuccess: function (data, videoObj) {
            var vehicleId = data.vehicleId;
            var num = data.channelNum;
            var videoType = data.channelType;
            var videoId = data.domId;

            /**
             * 将播放成功的视频对象存入集合中
             */
            var id = vehicleId + '-' + num
            if (subscribeVideoModule.has(id)) {
                var obj = subscribeVideoModule.get(id);
                obj.playing = true;
                obj.mediaPlayer = videoObj;
            }

            /**
             * 云台连接状态
             */
            var connectionState;
            var treeObj = $.fn.zTree.getZTreeObj("vTreeList");
            var nodes = treeObj.getNodesByParam('id', vehicleId, null);
            for (var i = 0; i < nodes.length; i += 1) {
                var node = nodes[i];
                if (node.children) {
                    for (var j = 0; j < node.children.length; j += 1) {
                        var channelNode = node.children[j];
                        if (channelNode.logicChannel === num) {
                            connectionState = channelNode.connectionFlag;
                            break;
                        }
                    }
                }
            }

            /**
             * 给已经播放的视频标签添加属性
             */
            $('#' + videoId).attr('vehicle-id', vehicleId)
                .attr('channel-num', num).attr('channel-type', videoType)
                .attr('connection-state', connectionState);
            $('#' + videoId).parent().append('<div class="video-modal ' + vehicleId + '"></div>');
            // 视频点击选中事件，用于云台下发
            $('.video-modal').off('click').on('click', this.videoModuleClickFn);
            // 视频双击事件，视频方法为1屏
            $('.video-modal').off('dblclick').on('dblclick', this.videoFullShow);
            // 视频右键绑定
            $('.video-modal').off('contextmenu').on('contextmenu', this.videoRightFun);
            // 鼠标按下
            $('.video-modal').off('pointerdown').on('pointerdown', this.videoMouseDown);
            // 鼠标移动
            $('.video-modal').off('pointermove').on('pointermove', this.videoMouseMove);
            // 鼠标抬起
            $('.video-modal').off('pointerup').on('pointerup', this.videoMouseUp);

            Table.table.logFindCilck();
        },
        videoMouseDown: function (e) {
            var channelNum = $(this).siblings('video').attr('channel-num');
            var id = $(this).siblings('video').attr('vehicle-id');
            var videoId = id + '-' + channelNum;
            if (subscribeVideoModule.has(videoId)) {
                var obj = subscribeVideoModule.get(videoId);
                obj.mediaPlayer.MouseDown360(e.originalEvent);
            }
            e.currentTarget.setPointerCapture(e.originalEvent.pointerId);
        },
        videoMouseMove: function (e) {
            var channelNum = $(this).siblings('video').attr('channel-num');
            var id = $(this).siblings('video').attr('vehicle-id');
            var videoId = id + '-' + channelNum;
            if (subscribeVideoModule.has(videoId)) {
                var obj = subscribeVideoModule.get(videoId);
                obj.mediaPlayer.MouseMove360(e.originalEvent);
            }
        },
        videoMouseUp: function (e) {
            var channelNum = $(this).siblings('video').attr('channel-num');
            var id = $(this).siblings('video').attr('vehicle-id');
            var videoId = id + '-' + channelNum;
            if (subscribeVideoModule.has(videoId)) {
                var obj = subscribeVideoModule.get(videoId);
                obj.mediaPlayer.MouseUp360(e.originalEvent);
            }
            e.currentTarget.releasePointerCapture(e.originalEvent.pointerId);
        },
        /**
         * 视频点击开启声音函数
         */
        videoModuleClickFn: function () {
            /**
             * 添加类样式  区分当前点击的video
             */
            $("#video-module").find("div").removeClass("this-click");
            $(this).addClass("this-click");
            /**
             * 获取当前点击video通道号相关信息
             */
            var channelNum = $(this).siblings('video').attr('channel-num');
            var connectState = $(this).siblings('video').attr('connection-state');
            /**
             * 云台功能视频通道号赋值
             */
            Tree.tree.setHaeundaeChannelNum(channelNum);
            var treeObj = $.fn.zTree.getZTreeObj("vTreeList");
            var id = $(this).siblings('video').attr('vehicle-id');
            var node = treeObj.getNodeByParam('id', id, null);
            $('#haeundaeBrand').text(node.name + '-' + channelNum);
            $('#connectState').attr('value', connectState);
            video.openChannelVoice(id, channelNum);
        },
        /**
         * 开启对应通道号声音
         */
        openChannelVoice: function (id, cNum) {
            /**
             * 先关闭当前打开的声音
             */
            if (createChannelVoice) {
                if (subscribeVideoModule.has(createChannelVoice)) {
                    var obj = subscribeVideoModule.get(createChannelVoice);
                    obj.mediaPlayer.closeVideoVoice();
                }
            }

            /**
             * 打开声音
             * @type {string}
             */
            var videoId = id + '-' + cNum;
            if (subscribeVideoModule.has(videoId)) {
                var obj = subscribeVideoModule.get(videoId);
                obj.mediaPlayer.openVideoVoice();
                createChannelVoice = videoId;
            }
        },
        /**
         * 视频全屏显示
         */
        videoFullShow: function () {
            if ($(this).parent('div').hasClass('full-video')) {
                $(this).parent('div').removeClass('full-video');
            } else {
                $(this).parent('div').addClass('full-video');
            }
        },
        /**
         * 视频右键点击事件
         */
        videoRightFun: function (e) {
            var domId = $(this).siblings('video').attr('id');
            var y = e.clientY;
            var x = e.clientX;
            var id = $('#' + domId).attr('vehicle-id');
            if (id !== '') {
                $('#videoMenu li').show();
                if (Tree.tree.getSelectNodeProtocol() === '23') {// 报批稿协议隐藏主码流、子码流菜单
                    $('#mainCodeStream').hide();
                    $('#subcodeFlow').hide();
                }
                var channelNum = $('#' + domId).attr('channel-num');
                var videoType = $('#' + domId).attr('channel-type');

                $('#videoVehicleId').val(id);
                $('#videoChannelNum').val(channelNum);
                $('#videoChannelType').val(videoType);
                $('#videoDomId').val(domId);
                $('#scaleRight').css('opacity', 0);
                $('#videoMenu').css({top: y + 'px', left: x + 'px', display: 'block'});
            }
            e.preventDefault();
        },
        /**
         * 视频关闭成功事件
         */
        videoCloseSuccess: function (data) {
            var videoId = data.domId;
            var vehicleId = data.vehicleId;
            var num = data.channelNum;

            var id = vehicleId + '-' + num;
            subscribeVideoModule.remove(id);
            if (subscribeVideoModule.keys().length === 0) {
                layer.msg('请选择监控对象', { time: 0, zIndex: 9, });
            }

            /**
             * 关闭视频订阅通道
             */
            var closeData = [{
                vehicleId: vehicleId,
                channels: [num],
            }];
            $('#' + videoId).parent('div').removeClass('full-video');
            $('#' + videoId).siblings('div.video-modal').remove();
            /**
             * 判断是否为异常关闭，异常关闭的情况下不恢复默认
             */
            var cssStr = $('#' + videoId).attr('style');
            if (cssStr.indexOf('video4.png') === -1 && cssStr.indexOf('video7.png') === -1 && cssStr.indexOf('video7.png') === -1) {
                $('#' + videoId).replaceWith('<video id="'+ videoId +'" width="100%" height="100%"></video>');
            }

            Table.table.logFindCilck();

            /**
             * 视频通道恢复默认
             */
            Tree.tree.recoverChannelNode(vehicleId, num);
        },
        /**
         * 获取操作超时时间
         */
        getTimeout: function () {
            var surl = '/clbs/realTimeVideo/video/getDiskInfo';
            json_ajax('POST', surl, 'json', true, {}, function (data) {
                if (data.success) {
                    // 视频播放缺省时间
                    handleTime = Number(data.obj.videoPlayTime) * 1000;
                    // 视频空闲时间
                    videoFreeTime = Number(data.obj.videoStopTime) * 1000;
                } else {
                    handleTime = 300000;
                    videoFreeTime = 30000;
                }
            })
        },
        /**
         * 9102或9101操作后，更新监听时间
         */
        updateWaitTime: function () {
            var $this = this;
            clearTimeout(waitTimeTimeout);
            if (subscribeVideoModule.size() === 0) {
                return false;
            }
            waitTimeTimeout = setTimeout(function () {
                /**
                 * 判断是否有视频通道订阅，若无则不进行弹窗提醒
                 */
                if (subscribeVideoModule.size() === 0) {
                    return false;
                }

                var fiveSecoundTimeout;
                var isConfirmHandle = false;
                var timeText = handleTime / 1000;

                layer.confirm('您已' + timeText + '秒无任何操作，是否继续查看音视频？', {
                    title: '提示',
                    btn: ['YES，继续看', 'NO，关闭 (<span id="timeUpdate">' + 30 + '</span>)'], //按钮
                    btnAlign: 'c',
                    fixed: false,
                    // 弹出成功
                    success: function () {
                        var index = 30;
                        fiveSecoundTimeout = setInterval(function () {
                            index -= 1;
                            $('#timeUpdate').text(index);
                            if (index === 0) {
                                layer.closeAll();
                            }
                        }, 1000);
                    },
                    // 确定
                    yes: function () {
                        isConfirmHandle = true;
                        clearInterval(fiveSecoundTimeout);
                        $this.updateWaitTime();
                        layer.closeAll();
                    },
                    // 关闭
                    cancel: function () {
                        clearInterval(fiveSecoundTimeout);
                        $this.clearAllVideo5Min();
                    },
                    // 关闭
                    end: function () {
                        if (!isConfirmHandle) {
                            clearInterval(fiveSecoundTimeout);
                            $this.clearAllVideo5Min();
                        }
                    },
                })
            }, handleTime);
        },
        /**
         * 设置时间内未做任何操作取消视频订阅
         */
        clearAllVideo5Min: function () {
            var keys = subscribeVideoModule.keys();
            for (var i = 0; i < keys.length; i += 1) {
                var data = subscribeVideoModule.get(keys[i]);
                if (data.mediaPlayer) {
                    data.mediaPlayer.cmdCloseVideo();
                    data.mediaPlayer.closeSocket();
                }
            }
        },
        /**
         * 返回订阅视频集合
         */
        getSubscribeVideoModule: function () {
            return subscribeVideoModule;
        },
        /**
         * 打开视频布局样式
         */
        openVideoArea: function (n) {
            var size = subscribeVideoModule.size();
            var count = size + n;
            if (count > 4 && count <= 6) {
                $("#videoSix").removeClass("video-six-check");
                this.videoSeparatedSix('videoSix');
            } else if (count > 6 && count <= 9) {
                $('#videoNine').removeClass("video-nine-check");
                this.videoSeparatedNine('videoNine');
            } else if (count > 9 && count <= 10) {
                $("#videoTen").removeClass("video-ten-check");
                this.videoSeparatedTen('videoTen');
            } else if (count > 10 && count <= 16) {
                $('#videoSixteen').removeClass("video-sixteen-check");
                this.videoSeparatedSixteen('videoSixteen');
            } else if (count > 16) {
                $("#videoSeparated").find("i").removeClass("video-four-check video-six-check video-nine-check video-ten-check");
                $('#videoSixteen').addClass("video-sixteen-check");
                this.createVideoDom(count);
            }
        },
        /**
         * 视频分隔4屏
         */
        videoSeparatedFour: function (_thisId) {
            $("#videoSeparated").find("i").removeClass("video-six-check video-nine-check video-ten-check video-sixteen-check");
            var videoWindow = $("#" + _thisId);
            if (!(videoWindow.hasClass("video-four-check"))) {
                //添加高亮
                videoWindow.addClass("video-four-check");
                //移除已添加的视频
                $("#video-module>div:nth-child(4)").nextAll().hide();
                //高宽度
                var vwidth = 100 / 2;
                if ($("#mapAllShow").children().hasClass("fa fa-chevron-right")) {
                    var vheight = $("#map-module").height() / 2;
                } else {
                    vheight = $("#video-module").height() / 2;
                }
                $("#video-module>div").css({
                    "width": vwidth + "%",
                    "height": "calc(100% - (100% - " + vheight + "px))"
                });
            }
        },
        //视频分隔6屏
        videoSeparatedSix: function (_thisId) {
            $("#videoSeparated").find("i").removeClass("video-four-check video-nine-check video-ten-check video-sixteen-check");
            var videoWindows = $("#" + _thisId);
            var videoModule = $("#video-module");
            var vwidth;
            if (videoWindows.hasClass("video-six-check")) { // 恢复默认
                //移除高亮
                videoWindows.removeClass("video-six-check");
                //移除已添加的视频
                $("#video-module>div:nth-child(4)").nextAll().hide();
                //高宽度
                vwidth = 100 / 2;
                //判断视频模块是否隐藏
                if ($("#mapAllShow").children().hasClass("fa fa-chevron-right")) {
                    var vheight = $("#map-module").height() / 2;
                } else {
                    vheight = videoModule.height() / 2;
                }
                $("#video-module>div").css({
                    "width": vwidth + "%",
                    "height": "calc(100% - (100% - " + vheight + "px))"
                });
                //4屏按钮高亮
                $("#videoFour").addClass("video-four-check");
            } else { // 6个video
                //添加高亮
                videoWindows.addClass("video-six-check");
                //移除已添加的视频
                $("#video-module>div").show();
                $("#video-module>div:nth-child(6)").nextAll().hide();
                //定义视频
                var videoLength = $('#video-module>div').length;
                var _html = "";
                for (var i = videoLength; i < 6; i++) {
                    _html +=
                        '<div class="pull-left v-' + videoName[i] + '">' +
                        '<video autoplay width="100%" height="100%" id="v_' + i + '_Source">' +
                        '<source src="" type="video/mp4">' +
                        '<source src="" type="video/ogg">' +
                        '您的浏览器不支持 video 标签。' +
                        '</video>' +
                        '</div>';
                }
                videoModule.append(_html);
                //高宽度
                vwidth = 100 / 3;
                //判断视频模块是否隐藏
                if ($("#mapAllShow").children().hasClass("fa fa-chevron-right")) {
                    vheight = $("#map-module").height() / 3;
                } else {
                    vheight = videoModule.height() / 3;
                }
                $("#video-module>div").css({
                    "width": vwidth + "%",
                    "height": "calc(100% - (100% - " + vheight + "px))"
                });
                //第一个视频高宽度
                var vOneWidth = vwidth * 2;
                var vOneHeight = vheight * 2;
                $("#video-module div.v-one").css({
                    "width": vOneWidth + "%",
                    "height": "calc(100% - (100% - " + (vOneHeight - 0.1) + "px))"
                });
            }
            videoModule = null;
            videoWindows = null;
        },
        /**
         * 视频分隔9屏
         */
        videoSeparatedNine: function (_thisId) {
            $("#videoSeparated").find("i").removeClass("video-four-check video-six-check video-ten-check video-sixteen-check");
            var videoWindow = $("#" + _thisId);
            var videoModule = $("#video-module");
            var vwidth;
            if (videoWindow.hasClass("video-nine-check")) {
                //移除高亮
                videoWindow.removeClass("video-nine-check");
                //移除已添加的视频
                $("#video-module>div:nth-child(4)").nextAll().hide();
                //高宽度
                vwidth = 100 / 2;
                //判断视频模块是否隐藏
                if ($("#mapAllShow").children().hasClass("fa fa-chevron-right")) {
                    var vheight = $("#map-module").height() / 2;
                } else {
                    vheight = videoModule.height() / 2;
                }
                $("#video-module>div").css({
                    "width": vwidth + "%",
                    "height": "calc(100% - (100% - " + vheight + "px))"
                });
                //4屏按钮高亮
                $("#videoFour").addClass("video-four-check");
            } else {
                //添加高亮
                videoWindow.addClass("video-nine-check");
                //移除已添加的视频
                $("#video-module>div").show();
                $("#video-module>div:nth-child(9)").nextAll().hide();
                //定义视频
                var videoLength = $('#video-module>div').length;
                var _html = "";
                for (var i = videoLength; i < 9; i++) {
                    _html +=
                        '<div class="pull-left v-' + videoName[i] + '">' +
                        '<video autoplay width="100%" height="100%" id="v_' + i + '_Source">' +
                        '<source src="" type="video/mp4">' +
                        '<source src="" type="video/ogg">' +
                        '您的浏览器不支持 video 标签。' +
                        '</video>' +
                        '</div>';
                }
                videoModule.append(_html);
                // 视频右键绑定
                $('#video-module video').off('contextmenu').on('contextmenu', this.videoRightFun);
                //高宽度
                vwidth = 100 / 3;
                //判断视频模块是否隐藏
                if ($("#mapAllShow").children().hasClass("fa fa-chevron-right")) {
                    vheight = $("#map-module").height() / 3;
                } else {
                    vheight = videoModule.height() / 3;
                }
                $("#video-module>div").css({
                    "width": vwidth + "%",
                    "height": "calc(100% - (100% - " + vheight + "px))"
                });
            }
            videoWindow = null;
            videoModule = null;
        },
        /**
         * 视频分隔10屏
         */
        videoSeparatedTen: function (_thisId) {
            $("#videoSeparated").find("i").removeClass("video-four-check video-six-check video-nine-check video-sixteen-check");
            var videoWindow = $("#" + _thisId);
            var videoModule = $("#video-module");
            var vwidth
            if (videoWindow.hasClass("video-ten-check")) {
                //移除高亮
                videoWindow.removeClass("video-ten-check");
                //移除已添加的视频
                $("#video-module>div:nth-child(4)").nextAll().hide();
                //高宽度
                vwidth = 100 / 2;
                //判断视频模块是否隐藏
                if ($("#mapAllShow").children().hasClass("fa fa-chevron-right")) {
                    var vheight = $("#map-module").height() / 2;
                } else {
                    vheight = videoModule.height() / 2;
                }
                $("#video-module>div").css({
                    "width": vwidth + "%",
                    "height": "calc(100% - (100% - " + vheight + "px))"
                });
                //4屏按钮高亮
                $("#videoFour").addClass("video-four-check");
            } else {
                //添加高亮
                videoWindow.addClass("video-ten-check");
                //移除已添加的视频
                $("#video-module>div").show();
                $("#video-module>div:nth-child(10)").nextAll().hide();
                //定义视频
                var videoLength = $('#video-module>div').length;
                var _html = "";
                for (var i = videoLength; i < 10; i++) {
                    _html +=
                        '<div class="pull-left v-' + videoName[i] + '">' +
                        '<video autoplay width="100%" height="100%" id="v_' + i + '_Source">' +
                        '<source src="" type="video/mp4">' +
                        '<source src="" type="video/ogg">' +
                        '您的浏览器不支持 video 标签。' +
                        '</video>' +
                        '</div>';
                }
                videoModule.append(_html);
                //高宽度
                vwidth = 100 / 5;
                //判断视频模块是否隐藏
                if ($("#mapAllShow").children().hasClass("fa fa-chevron-right")) {
                    vheight = $("#map-module").height() / 5;
                } else {
                    vheight = videoModule.height() / 5;
                }
                $("#video-module>div").css({
                    "width": vwidth + "%",
                    "height": "calc(100% - (100% - " + vheight + "px))"
                });
                //第一个视频高宽度
                var vOneWidth = vwidth * 4;
                var vOneHeight = vheight * 4;
                $("#video-module div.v-one").css({
                    "width": vOneWidth + "%",
                    "height": "calc(100% - (100% - " + (vOneHeight - 0.1) + "px))"
                });
            };
            videoWindow = null;
            videoModule = null;
        },
        /**
         * 视频分隔16屏
         */
        videoSeparatedSixteen: function (_thisId) {
            $("#videoSeparated").find("i").removeClass("video-four-check video-six-check video-nine-check video-ten-check");
            var videoWindow = $("#" + _thisId);
            var videoModule = $("#video-module");
            var vwidth;
            if (videoWindow.hasClass("video-sixteen-check")) {
                //移除高亮
                videoWindow.removeClass("video-sixteen-check");
                //移除已添加的视频
                $("#video-module>div:nth-child(4)").nextAll().hide();
                //高宽度
                vwidth = 100 / 2;
                //判断视频模块是否隐藏
                if ($("#mapAllShow").children().hasClass("fa fa-chevron-right")) {
                    var vheight = $("#map-module").height() / 2;
                } else {
                    vheight = videoModule.height() / 2;
                }
                $("#video-module>div").css({
                    "width": vwidth + "%",
                    "height": "calc(100% - (100% - " + vheight + "px))"
                });
                //4屏按钮高亮
                $("#videoFour").addClass("video-four-check");
            } else {
                //添加高亮
                videoWindow.addClass("video-sixteen-check");
                //移除已添加的视频
                $("#video-module>div").show();
                if (subscribeVideoModule.size() > 16) {
                    $('#video-module>div:nth-child(' + subscribeVideoNum + ')').nextAll().hide();
                } else {
                    $("#video-module>div:nth-child(16)").nextAll().hide();
                }
                // 超出16个窗口，显示滚动条
                $('#video-main-content .video-module').css('overflow', 'auto');
                //定义视频
                var videoLength = $('#video-module>div').length;
                var _html = "";
                for (var i = videoLength; i < 16; i++) {
                    _html +=
                        '<div class="pull-left v-' + videoName[i] + '">' +
                        '<video autoplay width="100%" height="100%" id="v_' + i + '_Source">' +
                        '<source src="" type="video/mp4">' +
                        '<source src="" type="video/ogg">' +
                        '您的浏览器不支持 video 标签。' +
                        '</video>' +
                        '</div>';
                }
                videoModule.append(_html);
                //高宽度
                vwidth = 100 / 4;
                //判断视频模块是否隐藏
                if ($("#mapAllShow").children().hasClass("fa fa-chevron-right")) {
                    vheight = $("#map-module").height() / 4;
                } else {
                    vheight = videoModule.height() / 4;
                }
                $("#video-module>div").css({
                    "width": vwidth + "%",
                    "height": "calc(100% - (100% - " + vheight + "px))"
                });
            }
            videoWindow = null;
            videoModule = null;
        },
        /**
         * 创建video标签
         */
        createVideoDom: function (count) {
            var size = subscribeVideoModule.size();
            var _html = '';
            for (var i = size; i < count; i++) {
                _html +=
                    '<div class="pull-left">' +
                    '<video autoplay width="100%" height="100%" id="v_' + i + '_Source">' +
                    '<source src="" type="video/mp4">' +
                    '<source src="" type="video/ogg">' +
                    '您的浏览器不支持 video 标签。' +
                    '</video>' +
                    '</div>';
            }
            var videoModule = $("#video-module");
            videoModule.append(_html);
            //高宽度
            //判断视频模块是否隐藏
            if ($("#mapAllShow").children().hasClass("fa fa-chevron-right")) {
                var videoHeight = videoModule.height() / 4;
            } else {
                videoHeight = videoModule.height() / 4;
            }
            $("#video-module>div").css({
                "width": "25%",
                "height": "calc(100% - (100% - " + videoHeight + "px))"
            });
            // 超出16个窗口后，出现滚动条
            $('#video-main-content .video-module').css('overflow', 'auto');
        },
        /**
         * 视频显示模块分隔
         */
        videoSeparatedFn: function () {
            // 去掉视频窗口出现的滚动条
            $('#video-main-content .video-module').css('overflow', 'hidden');
            var _thisId = $(this).attr("id");
            //4屏
            if (_thisId === "videoFour") {
                video.videoSeparatedFour(_thisId);
            }
            //6屏
            else if (_thisId === "videoSix") {
                video.videoSeparatedSix(_thisId);
            }
            //9屏
            else if (_thisId === "videoNine") {
                video.videoSeparatedNine(_thisId);
            }
            //10屏
            else if (_thisId === "videoTen") {
                video.videoSeparatedTen(_thisId);
            }
            //16屏
            else if (_thisId === "videoSixteen") {
                video.videoSeparatedSixteen(_thisId);
            }
        },
        /**
         * 取消视频订阅
         * 单个视频通道或者监控对象的多个视频通道
         */
        videoCloseFun: function (id, type, channel) {
            $(".pull-left h4").html('');
            if (type === 'monitor') {
                var treeObj = $.fn.zTree.getZTreeObj('vTreeList');
                var nodes = treeObj.getNodesByParam('id', id, null);
                if (nodes.length > 0) {
                    for (var i = 0; i < nodes.length; i += 1) {
                        var channels = nodes[i].children;
                        if (channels) {
                            for (let i = 0; i < channels.length; i += 1) {
                                var _id = id + '-' + channels[i].logicChannel;
                                if (subscribeVideoModule.has(_id)) {
                                    var data = subscribeVideoModule.get(_id);
                                    /**
                                     * 视频通道号-关闭sockets
                                     */
                                    if (data.mediaPlayer) {
                                        data.mediaPlayer.cmdCloseVideo();
                                        data.mediaPlayer.closeSocket();
                                    }
                                    /**
                                     * 这是一段垃圾代码，为了在切换监控双击订阅视频时，视频通道显示保持一致
                                     */
                                    subscribeVideoModule.remove(_id);
                                }
                            }
                        } else { // 针对订阅视频后的监控对象，但是未请求通道数据的情况下
                            var keys = subscribeVideoModule.keys();
                            for (let i = 0; i < keys.length; i += 1) {
                                if (keys[i].indexOf(id) !== -1) {
                                    var data = subscribeVideoModule.get(keys[i]);
                                    /**
                                     * 视频通道号-关闭sockets
                                     */
                                    if (data.mediaPlayer) {
                                        data.mediaPlayer.cmdCloseVideo();
                                        data.mediaPlayer.closeSocket();
                                    }
                                    /**
                                     * 这是一段垃圾代码，为了在切换监控双击订阅视频时，视频通道显示保持一致
                                     */
                                    subscribeVideoModule.remove(keys[i]);
                                }
                            }
                        }
                    }
                } else { // 针对订阅监控对象后，组织树进行模糊搜索后无法查询到对应节点
                    var keys = subscribeVideoModule.keys();
                    for (let i = 0; i < keys.length; i += 1) {
                        if (keys[i].indexOf(id) !== -1) {
                            var data = subscribeVideoModule.get(keys[i]);
                            /**
                             * 视频通道号-关闭sockets
                             */
                            if (data.mediaPlayer) {
                                data.mediaPlayer.cmdCloseVideo();
                                data.mediaPlayer.closeSocket();
                            }
                            /**
                             * 这是一段垃圾代码，为了在切换监控双击订阅视频时，视频通道显示保持一致
                             */
                            subscribeVideoModule.remove(keys[i]);
                        }
                    }
                }
            } else if (type === 'channel') {
                var _id = id + '-' + channel;
                if (subscribeVideoModule.has(_id)) {
                    var data = subscribeVideoModule.get(_id);
                    /**
                     * 视频通道号-关闭sockets
                     */
                    if (data.mediaPlayer) {
                        data.mediaPlayer.cmdCloseVideo();
                        data.mediaPlayer.closeSocket();
                    }
                    /**
                     * 这是一段垃圾代码，为了在切换监控双击订阅视频时，视频通道显示保持一致
                     */
                    subscribeVideoModule.remove(_id);
                }
            } else { // 关闭所有视频
                var keys = subscribeVideoModule.keys();
                for (var i = 0; i < keys.length; i += 1) {
                    var data = subscribeVideoModule.get(keys[i]);
                    /**
                     * 视频通道号-关闭sockets
                     */
                    if (data.mediaPlayer) {
                        data.mediaPlayer.cmdCloseVideo();
                        data.mediaPlayer.closeSocket();
                    }
                    /**
                     * 这是一段垃圾代码，为了在切换监控双击订阅视频时，视频通道显示保持一致
                     */
                    subscribeVideoModule.remove(keys[i]);
                }
            }
            $('#videoMenu').hide();
        },
        /**
         * 点击关闭单个视频数据组装
         */
        singleVideoClose: function () {
            var vehicleId = $('#videoVehicleId').val();
            var channelNum = $('#videoChannelNum').val();
            this.videoCloseFun(vehicleId, 'channel', channelNum);
        },
        /**
         * 主码流
         */
        mainCodeStreamFun: function () {
            var id = $('#videoVehicleId').val();
            var channelNum = $('#videoChannelNum').val();
            var type = $('#videoChannelType').val();
            var key = id + '-' + channelNum;
            if (subscribeVideoModule.has(key)) {
                var videoObj = subscribeVideoModule.get(key);
                videoObj.mediaPlayer.cmdSwitchStream('0');
            }
            $('#videoMenu').hide();
        },
        /**
         * 子码流
         */
        subcodeFlowFun: function () {
            var id = $('#videoVehicleId').val();
            var channelNum = $('#videoChannelNum').val();
            var type = $('#videoChannelType').val();
            var key = id + '-' + channelNum;
            if (subscribeVideoModule.has(key)) {
                var videoObj = subscribeVideoModule.get(key);
                videoObj.mediaPlayer.cmdSwitchStream('1');
            }
            $('#videoMenu').hide();
        },
        /**
         * 视频显示比例显示
         */
        scaleAreaShow: function () {
            if ($('#scaleRight').css('opacity') === 1) {
                $('#scaleRight').animate({'opacity': 0});
            } else {
                $('#scaleRight').animate({'opacity': 1});
            }
        },
        /**
         * 默认比例
         */
        defaultScaleFun: function () {
            var videoId = $('#videoDomId').val();
            $('#' + videoId).css('height', '100%');
            $('#videoMenu').hide();
        },
        /**
         * 4:3
         */
        twoScaleFun: function () {
            var videoId = $('#videoDomId').val();
            $('#' + videoId).css('height', '75%');
            $('#videoMenu').hide();
        },
        /**
         * 16:9
         */
        threeScaleFun: function () {
            var videoId = $('#videoDomId').val();
            $('#' + videoId).css('height', '56%');
            $('#videoMenu').hide();
        },
        /**
         * 抓拍
         */
        takePhotoFun: function () {
            $('#snapText').val('');
            var vid = $('#videoVehicleId').attr('value');
            var cNumber = $('#videoChannelNum').attr('value');
            var id = vid + '-' + cNumber;
            var videoObj = subscribeVideoModule.get(id);
            photoImageFormData = videoObj.mediaPlayer.videoScreenshots('canvasForVideo', 524, 400);
            $('#videoSnap .modal-dialog').css('width', '600px');
            $("#videoSnap .modal-body").css({"height": "auto", "max-height": ($(window).height() - 194) + "px"});
            $('#videoSnap').modal('show');
            var treeObj = $.fn.zTree.getZTreeObj("vTreeList");
            var node = treeObj.getNodeByParam("id", vid, null);
            $('#photoObjName').text(node.name);
            $('#photoObjColor').text(getPlateColor(node.plateColor));
            $('#photoObjNumber').text(cNumber);
            $('#vehicleId').attr('value', vid);
            $('#videoMenu').hide();
        },
        /**
         * 隐藏左侧及底部列表
         */
        videoWinFn: function () {
            if ($(this).hasClass("video-win")) {
                //图标改变
                $(this).removeClass("video-win").addClass("video-win-check");
                //列表隐藏
                video.tableListHide();
                //左侧隐藏
                $(".video-main-left").hide();
                $(".video-main-right").css("width", "100%");
                // //注销全屏绑定事件
                $("#videoFullScreen").unbind();
            } else {
                //图标改变
                $(this).removeClass("video-win-check").addClass("video-win");
                //左侧显示
                $(".video-main-left").show();
                $(".video-main-right").css("width", "calc(100% - 315px)");
                //音视频模块全屏显示
                $("#videoFullScreen").on("click", video.videoFullScreenFn);
            }
        },
        /**
         * 抓拍提交
         */
        snapSubmitFun: function () {
            var cNumber = $('#photoObjNumber').text();
            var snapText = $('#snapText').val();
            var id = $('#vehicleId').attr('value');
            var url = '/clbs/realTimeVideo/video/screenshot';
            photoImageFormData.append('vehicleId', id);
            photoImageFormData.append('wayId', cNumber);
            photoImageFormData.append('description', snapText);

            $.ajax({
                url: url,
                data: photoImageFormData,
                type: "Post",
                dataType: "json",
                cache: false, // 上传文件无需缓存
                processData: false, // 用于对data参数进行序列化处理 这里必须false
                contentType: false, // 必须
            })
        },
        /**
         * 音视频模块全屏显示
         */
        videoFullScreenFn: function () {
            // 如果视频被隐藏，点击全屏按钮不操作
            var isVideoHide = !$('#mapAllShow').children().hasClass("fa fa-chevron-left");
            if (isVideoHide) {
                return;
            }
            if ($(this).hasClass("video-full-screen")) {
                // 图标改变
                $(this).removeClass("video-full-screen").addClass("video-full-screen-check");
                // 地图隐藏
                $("#map-module").hide();
                $("#video-module").removeClass("col-md-9").addClass("col-md-12");
                // 列表隐藏
                video.tableListHide();
                // 左侧隐藏
                $(".video-main-left").hide();
                $(".video-main-right").css("width", "100%");
                $('#mainContent').addClass('main-full-screen');
                // 头部隐藏
                $("#header").hide();
                // 左侧及底部隐藏及tab选项卡向下隐藏显示函数绑定事件注销
                $("#videoWin, #scalingBtn").unbind();
            } else {
                // 图标改变
                $(this).removeClass("video-full-screen-check").addClass("video-full-screen");
                // 地图显示
                $("#map-module").show();
                $("#video-module").removeClass("com-md-12").addClass("col-md-9");
                // 左侧显示
                $(".video-main-left").show();
                $(".video-main-right").css("width", "calc(100% - 315px)");
                $('#mainContent').removeClass('main-full-screen');
                // 头部显示
                $("#header").show();
                // 隐藏左侧及底部列表
                $("#videoWin").on("click", video.videoWinFn);
                // tab选项卡向下隐藏显示函数
                $("#scalingBtn").on("click", Table.table.tableStateChange.bind(Table.table));
            }
            setTimeout(function () {
                Table.table.windowResize();
            }, 300)
        },
        /**
         * 列表隐藏
         */
        tableListHide: function () {
            $('#videoRightTop').css('height', 'calc(100% - 44px)');
            this.videoSeparatedAdaptHide();//左下侧隐藏及全屏显示时视频分隔自适应计算函数
            $("#scalingBtn").removeClass("fa fa-chevron-down").addClass("fa fa-chevron-up");
        },
        /**
         * 左下侧隐藏及全屏显示时视频分隔自适应计算函数
         */
        videoSeparatedAdaptHide: function () {
            //判断当前屏幕分隔数 区分屏幕分隔高宽度
            var rightHeight = $('#videoMainRight').height();
            if ($("#videoFour").hasClass("video-four-check")) {
                //高宽度
                var vwidth = 100 / 2;
                //区分全屏
                var vheight = (rightHeight - 42) / 2;
                $("#video-module>div").css({
                    "width": vwidth + "%",
                    "height": "calc(100% - (100% - " + vheight + "px))"
                });
            }
            else if ($("#videoSix").hasClass("video-six-check")) {
                //高宽度
                vwidth = 100 / 3;
                //区分全屏
                vheight = (rightHeight - 42) / 3;
                $("#video-module>div").css({
                    "width": vwidth + "%",
                    "height": "calc(100% - (100% - " + vheight + "px))"
                });
                //第一个视频高宽度
                var vOneWidth = vwidth * 2;
                var vOneHeight = vheight * 2;
                $("#video-module div.v-one").css({
                    "width": vOneWidth + "%",
                    "height": "calc(100% - (100% - " + (vOneHeight - 0.1) + "px))"
                });
            }
            else if ($("#videoNine").hasClass("video-nine-check")) {
                //高宽度
                vwidth = 100 / 3;
                //区分全屏
                vheight = (rightHeight - 42) / 3;
                $("#video-module>div").css({
                    "width": vwidth + "%",
                    "height": "calc(100% - (100% - " + vheight + "px))"
                });
            }
            else if ($("#videoTen").hasClass("video-ten-check")) {
                //高宽度
                vwidth = 100 / 5;
                //区分全屏
                vheight = (rightHeight - 42) / 5;
                $("#video-module>div").css({
                    "width": vwidth + "%",
                    "height": "calc(100% - (100% - " + vheight + "px))"
                });
                //第一个视频高宽度
                vOneWidth = vwidth * 4;
                vOneHeight = vheight * 4;
                $("#video-module div.v-one").css({
                    "width": vOneWidth + "%",
                    "height": "calc(100% - (100% - " + (vOneHeight - 0.1) + "px))"
                });
            }
            else if ($("#videoSixteen").hasClass("video-sixteen-check")) {
                //高宽度
                vwidth = 100 / 4;
                //区分全屏
                vheight = (rightHeight - 42) / 4;
                $("#video-module>div").css({
                    "width": vwidth + "%",
                    "height": "calc(100% - (100% - " + vheight + "px))"
                });
            }
        },
        /**
         * body点击关闭视频右键菜单
         */
        onBodyMouseDown: function (event) {
            if (!(event.target.id === "rMenu" || $(event.target).parents("#rMenu").length > 0)) {
                $('#rMenu').css({"visibility": "hidden"});
            }
            if (!(event.target.id === "videoMenu" || $(event.target).parents("#videoMenu").length > 0)) {
                $('#videoMenu').css('display', 'none');
            }
        },
        setTreeModule: function (module) {
            Tree = module;
        },
        setTableModule: function (module) {
            Table = module;
        },
        /**
         * 鼠标移动监听事件
         */
        mousemoveFn: function () {
            this.updateWaitTime();
        }
    };

    $(function () {
        // 视频右键菜单-关闭视频
        $('#closeVideo').on('click', video.singleVideoClose.bind(video));
        // 视频右键菜单-关闭所有视频
        $('#closeAllVideo').on('click', video.videoCloseFun.bind(video));
        // 视频右键菜单-主码流
        $('#mainCodeStream').on('click', video.mainCodeStreamFun.bind(video));
        // 视频右键菜单-子码流
        $('#subcodeFlow').on('click', video.subcodeFlowFun.bind(video));
        // 视频右键菜单-显示比例
        $('#scaleShow').on('click', video.scaleAreaShow);
        // 视频右键菜单-显示比例-原始比例
        $('#defaultScale').on('click', video.defaultScaleFun);
        // 视频右键菜单-显示比例-4:3
        $('#twoScale').on('click', video.twoScaleFun);
        // 视频右键菜单-显示比例-16:9
        $('#threeScale').on('click', video.threeScaleFun);
        // 视频右键菜单-抓拍
        $('#takePhoto').on('click', video.takePhotoFun);
        $("#videoFour, #videoSix, #videoNine, #videoTen, #videoSixteen").on("click", video.videoSeparatedFn);//视频显示模块分隔
        $("#videoWin").on("click", video.videoWinFn);//隐藏左侧及底部列表
        $('#snapSubmit').on('click', video.snapSubmitFun); // 抓拍提交
        $("#videoFullScreen").on("click", video.videoFullScreenFn);//音视频模块全屏显示
        $("body").on("mousedown", video.onBodyMouseDown);
        $('body').on('mousemove', video.mousemoveFn.bind(video));
    });

    return {
        video: video,
    };
});