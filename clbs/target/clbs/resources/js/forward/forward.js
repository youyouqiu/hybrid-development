/**
 * author by wanjikun 2018/11/05
 */
;(function (window, $) {
    var videoName = ["one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen"];//视频分隔类样式
    var mediaPlayers = []; //保存播放中的实例化对象
    var vehicleName;
    const template = document.querySelector('#playerWindow');
    var forward = {
        /**
         * 获取url参数
         */
        getQueryVariable:function(variable){
            var query = window.location.search.substring(1);
            var vars = query.split("&");
            for (var i = 0; i < vars.length; i++) {
                var pair = vars[i].split("=");
                if (pair[0] === variable) {
                    return pair[1];
                }
            }
            return '';
        },
        /**
         * 获取车辆ID
         */
        getVehicleId:function(){
            vehicleName = decodeURIComponent(forward.getQueryVariable('VehPlateNum'));
            if (vehicleName === '') {
                layer.msg('地址栏参数VehPlateNum不能为空');
                return;
            }
            var VehPlateNum =decodeURIComponent(forward.getQueryVariable('VehPlateNum'));
            json_ajax('post','/clbs/v/monitoring/forward/monitorId','json',true,{plateNum:VehPlateNum},function (data) {
                if (data.success){
                    forward.getVehicleChannel(data.obj)
                } else {
                    layer.msg('地址栏参数传递错误')
                }
            })
        },
        /**
         * 获取监控对象视频逻辑通道号
         */
        getVehicleChannel:function (id) {
            json_ajax('post','/clbs/v/monitoring/forward/getChannels','json',true,{vehicleId:id,isChecked:false},function (data) {
                if (data.success) {
                    var channels = JSON.parse(data.obj);
                    if (channels.length === 0){
                        layer.msg('该车辆未设置通道号')
                    } else {
                        var videoChannels = [];
                        channels.forEach((val)=>{
                            if (val.channelType !== 1){
                                videoChannels.push(val);
                            }
                        });
                        forward.setVideoHtml(videoChannels);
                        setTimeout(function () {
                            forward.sendParamByBatch(videoChannels);
                        },1000);
                    }
                }
            })
        },
        setVideoHtml:function(msg){
            if ('content' in document.createElement('template')) {
                for (let i = 0, n = 16; i < n; i++) {
                    let title = '';
                    if (msg[i]) {
                        title = `${vehicleName}-${msg[i].physicsChannel}`;
                    }
                    const player = document.importNode(template.content, true);
                    player.querySelector("p.video-title").textContent = title;
                    player.querySelector("video").id = `v_${i}_Source`;
                    videoPlayer.appendChild(player);
                }
                return;
            }
            let _html = "";
            for (let i = 0; i < msg.length; i++) {
                _html +=
                    `<div class="pull-left video-box v-${videoName[i]}">
                        <p class="video-title">${vehicleName}-${msg[i].physicsChannel}</p>
                        <div style="height:calc(100% - 20px)">
                            <video muted autoplay width="100%" height="100%" id="v_${i}_Source">
                                <source src="" type="video/mp4">
                                <source src="" type="video/ogg">
                                您的浏览器不支持 video 标签。
                            </video>
                        </div>
                    </div>`;
            }
            $("#videoPlayer").html(_html);
        },
        /**
         * 订阅视频
         * @param videoChannels
         */
        sendParamByBatch:function (videoChannels) {
            var jsonArr = [];
            videoChannels.forEach(function (videoChannel) {
                var obj = {
                    vehicleId: videoChannel.vehicleId,
                    requestType: 0,
                    channelNum: videoChannel.logicChannel,
                    mobile: videoChannel.mobile,
                    orderType: 11,
                    streamType: videoChannel.streamType,
                    channelType: videoChannel.channelType,
                };
                jsonArr.push(obj)
            });

            if(jsonArr.length > 0){
                $.ajax({
                    type:'POST',
                    url:'/clbs/v/monitoring/forward/audioAndVideoParameters/' + jsonArr[0].vehicleId,
                    dataType: 'json',
                    async: false,
                    data: null,
                    success: function (data) {
                        forward.openTerminalVideo(jsonArr, data.obj);
                    }
                });
            }
        },

        createRTPMediaPlayer: function(videoId, url, subscribeData) {
            return new RTPMediaPlayer({
                domId: videoId,
                url: url,
                data: subscribeData,
                //状态
                onMessage: function($data, $msg) {
                    let code = JSON.parse($msg).data.msgBody.code;
                    let videoObj = document.getElementById($data.videoId);
                    switch (code) {
                        case -1004:
                            videoObj.style.backgroundImage = 'url(/clbs/resources/img/videoPrompt/video4.png';
                            break;
                        case -1005:
                            videoObj.style.backgroundImage = 'url(/clbs/resources/img/videoPrompt/video7.png';
                            break;
                        case -1006:
                            layer.msg('终端未响应,请重新订阅', {time: 2000});
                            videoObj.style.backgroundImage = 'url(/clbs/resources/img/videoPrompt/video9.png';
                            break;
                        default:
                            break;
                    }
                },
                //订阅
                socketOpenFun: function($data, $$this) {
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
            });
        },

        /**
         * 订阅成功后打开视频
         */
        openTerminalVideo:function (info, node) {
            mediaPlayers = [];
            var protocol = 'ws://';

            if (document.location.protocol === 'https:') {
                protocol = 'wss://';
            }
            let userUuid = node.userUuid;
            // 视频转发页面访问的接口是不鉴权的获取不到用户id 所以随便写一个(无业务逻辑)
            if (userUuid == null) {
                userUuid = "user-id";
            }
            for (var i = 0; i < info.length; i++) {
                var value = info[i];
                var videoId = `v_${i}_Source`;
                var channelNum = value.channelNum;
                var subscribeData = {
                    videoId: videoId,
                    vehicleId: value.vehicleId, //车id
                    simcardNumber: value.mobile, //终端手机卡号
                    channelNumber: JSON.stringify(channelNum), //终端通道号
                    sampleRate: node.samplingRateStr || '8000', //音频采样率
                    channelCount: node.vocalTractStr || '0', //音频声道数
                    audioFormat: node.audioFormatStr, //音频编码
                    playType: 'REAL_TIME', // 播放类型(实时 REAL_TIME，回放 TRACK_BACK，对讲 BOTH_WAY，监听 UP_WAY，广播 DOWN_WAY)
                    dataType: '0', //播放数据类型(0：音视频，1：视频，2：双向对讲，3：监听，4：中心广播，5：透传)
                    userID: userUuid, //用户ID
                    deviceID:  node.deviceId, //终端ID
                    streamType: JSON.stringify(value.streamType), //码流类型(0：主码流，1：子码流)
                    deviceType: node.deviceType,
                };

                var url = protocol + videoRequestUrl + ':' + videoRequestPort + '/' + value.mobile + '/' + value.channelNum + '/0';
                var rtpMediaPlayer = forward.createRTPMediaPlayer(videoId, url, subscribeData);

                mediaPlayers.push(rtpMediaPlayer);

                var videoObj = document.getElementById(videoId);
                if (videoObj.muted === undefined) {
                    return;
                }
                videoObj.muted = true;
            }
        },
        /**
         * 关闭视频
         */
        closeTerminalVideo:function () {
            mediaPlayers.forEach(function (mediaPlayer) {
                mediaPlayer.closeSocket();
            })
        },
    };

    $(function(){
        forward.getVehicleId();
        window.onbeforeunload = function(event) {
            forward.closeTerminalVideo();
            event.returnValue = "正在关闭视频";
        }
    })
}(window, $));
