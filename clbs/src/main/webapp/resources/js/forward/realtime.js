/**
 * author by wanjikun 2018/11/05
 */
;(function (window) {
    const videoLayout = (function () {
        const videoPlayer = document.querySelector("#videoPlayer");
        const template = document.querySelector('#playerWindow');
        let videoShowNumber;

        function initLayout(name, values) {
            if ('content' in document.createElement('template')) {
                for (let i = 0, n = 16; i < n; i++) {
                    let title = '';
                    if (values[i]) {
                        title = name + "-" + values[i].physicsChannel;
                    }
                    createVideoDom(title, i);
                }
                changeVideoViewLayout(values.length);
            }
        }

        function createVideoDom(title, i) {
            const player = document.importNode(template.content, true);
            player.querySelector("p.video-title").textContent = title;
            player.querySelector("video").id = "v_" + i + "_Source";
            videoPlayer.appendChild(player);
        }

        function changeLayout(windowNum) {
            if (windowNum === videoShowNumber) {
                // 没有改变，直接返回
                return;
            }
            changeVideoViewLayout(windowNum);
        }

        // 初始化视频窗口布局
        function changeVideoViewLayout(num) {
            let newNum;
            if (num <= 4) {
                newNum = 4
            }
            if (num > 4 && num <= 6) {
                newNum = 6
            }
            if (num > 6 && num <= 9) {
                newNum = 9
            }
            if (num > 9 && num <= 10) {
                newNum = 10
            }
            if (num > 10 && num <= 16) {
                newNum = 16
            }
            videoShowNumber = num;
            document.getElementById('videoPlayer').className = 'video-list-' + newNum;
            document.querySelectorAll('.pull-left').forEach(function (item, index) {
                item.hidden = (index + 1) > num;
            });
        }

        return {
            initLayout: initLayout,
            changeLayout: changeLayout,
        }
    }());

    const videoPlayer = (function (videoLayout) {
        let videoObjs = []; //保存播放中的实例化对象
        let vehicleId;
        let channelsStr;
        let startTime;
        let stopTime;
        let protocol = 'ws://';
        if (document.location.protocol === 'https:') {
            protocol = 'wss://';
        }
        let playType; //判断播放类型 true:实时视频 false: 视频回放;
        const btnPlay = document.getElementById("btnPlay");

        function doPost(url, data) {
            const params = {
                method: 'POST',
            };
            if (data) {
                params.body = data;
            }
            return fetch(url, params)
              .then(res => res.json())
              .catch(error => console.error('post error:', error));
        }

        /**
         * 获取url参数
         */
        function getQueryVariable(parameter) {
            const query = window.location.search.substring(1);
            const vars = query.split("&");
            for (let i = 0; i < vars.length; i++) {
                let pair = vars[i].split("=");
                if (pair[0] === parameter) {
                    return pair[1];
                }
            }
            return false;
        }

        function startPlay(videoType) {
            startTime = newTimeFun(document.getElementById('startTime').value);
            stopTime = newTimeFun(document.getElementById('endTime').value);

            if (videoType === '2' && (startTime.length === 0 || endTime.length === 0)){
                return;
            }

            btnPlay.disabled = true;
            channelsStr = getQueryVariable('Channel');
            const plateNumber = getQueryVariable('VehPlateNum');
            if (!plateNumber) {
                return;
            }
            const vehicleName = decodeURIComponent(plateNumber);
            const url = '/clbs/v/monitoring/forward/monitorId';
            const data = new FormData();
            data.append('plateNum', vehicleName);

            doPost(url, data).then(response => {
                if (response.success) {
                    vehicleId = response.obj;
                    getVehicleChannel(vehicleName);
                } else {
                    window.alert('地址栏参数传递错误:' + url);
                }
            });
        }

        /**
         * 获取监控对象视频逻辑通道号
         */
        function getVehicleChannel(monitorName) {
            const url = '/clbs/v/monitoring/forward/getChannels';
            const data = new FormData();
            data.append('vehicleId', vehicleId);
            data.append('isChecked', 'false');
            doPost(url, data).then(function (response) {
                if (!response.success) {
                    return;
                }
                const msg = JSON.parse(response.obj);
                if (msg.length === 0) {
                    layer.msg('该车辆未设置通道号')
                } else {
                    const videoChannels = [];
                    msg.forEach(function (val) {
                        if(val.channelType !== 1){
                            videoChannels.push(val);
                        }
                    });

                    videoLayout.initLayout(monitorName, videoChannels);
                    setTimeout(function () {
                        getMediaParams(videoChannels);
                    }, 1000)
                }
            })
        }

        /**
         * 获取音视频参数
         * @param videoChannels 视频通道号列表
         */
        function getMediaParams(videoChannels) {
            playType = !(startTime !== '' && stopTime !== '');
            let videoPlayList = [];
            videoChannels.forEach(function (val) {
                videoPlayList.push({
                    vehicleId: val.vehicleId,
                    requestType: 0,
                    channelNum: val.logicChannel,
                    mobile: val.mobile,
                    orderType: 11,
                    streamType: val.streamType,
                    channelType: val.channelType,
                });
            });

            if(videoPlayList.length === 0) {
                return;
            }
            const url = '/clbs/v/monitoring/forward/audioAndVideoParameters/' + videoPlayList[0].vehicleId;
            doPost(url).then(function (response) {
                if (!response.success) {
                    return;
                }
                playVideo(videoPlayList, response.obj);
            });
        }


        function createRTPMediaPlayer(videoId, url, subscribeData) {
            return new RTPMediaPlayer({
                domId: videoId,
                url: url,
                data: subscribeData,
                imgSrc: false,
                //状态
                onMessage: function($data, $msg) {
                    let code = JSON.parse($msg).data.msgBody.code; //状态
                    let videoDom = document.getElementById($data.videoId);
                    switch (code) {
                        case -1004:
                            videoDom.style.backgroundImage = 'url(/clbs/resources/img/videoPrompt/video4.png)';
                            break;
                        case -1005:
                            videoDom.style.backgroundImage = 'url(/clbs/resources/img/videoPrompt/video7.png)';
                            break;
                        case -1006:
                            videoDom.style.backgroundImage = 'url(/clbs/resources/img/videoPrompt/video9.png)';
                            layer.msg('终端未响应,请重新订阅', {time: 2000});
                            break;
                        default:
                            break;
                    }
                },
                //订阅
                socketOpenFun: function($data, $$this) {
                    /*** socket连接建立成功* 图标提示替换和消息发送*/
                    let setting = {
                        vehicleId: $data.vehicleId, // 车辆ID
                        simcardNumber: $data.simcardNumber, // sim卡号
                        channelNumber: $data.channelNumber, // 通道号
                        sampleRate: $data.sampleRate, // 采样率
                        channelCount: $data.channelCount, // 声道数
                        audioFormat: $data.audioFormat, // 编码格式
                        playType: $data.playType, // 播放类型 实时 REAL_TIME，回放 TRACK_BACK，对讲 BOTH_WAY，监听 UP_WAY，广播 DOWN_WAY
                        dataType: $data.dataType, // 数据类型0：音视频，1：视频，2：双向对讲，3：监听，4：中心广播，5：透传
                        userID: $data.userID, // 用户IDtakeUpChannel
                        deviceID: $data.deviceID, // 终端ID
                        streamType: $data.streamType, // 码流类型0：主码流，1：子码流
                        deviceType: $data.deviceType,
                    };
                    if (!playType) {
                        setting.forwardOrRewind = $data.forwardOrRewind;
                        setting.remoteMode = $data.remoteMode;
                        setting.storageType = $data.storageType;
                        setting.startTime = $data.startTime;
                        setting.endTime = $data.endTime;
                    }

                    $$this.play(setting);
                },
            });
        }

        /**
         * 订阅成功后打开视频
         * @param videoPlayList 播放参数列表
         * @param mediaParams 设备音视频参数
         * @param mediaParams.vehicleId 车id
         * @param mediaParams.simcardNumber 终端手机号
         * @param mediaParams.deviceType 设备协议类型
         * @param mediaParams.channelNumber 终端通道号
         * @param mediaParams.userUuid 用户UUID
         * @param mediaParams.deviceId 终端ID
         * @param mediaParams.audioFormatStr 音频编码
         * @param mediaParams.vocalTractStr 音频声道数
         * @param mediaParams.samplingRateStr 音频采样率
         */
        function playVideo(videoPlayList, mediaParams) {
            videoObjs = [];
            for (let i = 0; i < videoPlayList.length; i++) {
                const value = videoPlayList[i];
                const videoId = 'v_' + i + '_Source';
                const channelNum = value.channelNum;
                const simcardNumber = value.mobile;
                let subscribeData;
                let url;

                url = protocol + videoRequestUrl + ':' + videoResourcePort + '/' + simcardNumber + '/' + channelNum;
                subscribeData = {
                    videoId: videoId,
                    vehicleId: value.vehicleId, //车id
                    simcardNumber: simcardNumber, //终端手机卡号
                    deviceType: mediaParams.deviceType,
                    channelNumber: JSON.stringify(channelNum), //终端通道号
                    userID: mediaParams.userUuid, //用户ID
                    deviceID: mediaParams.deviceId, //终端ID
                    audioFormat: mediaParams.audioFormatStr, //音频编码
                    channelCount: mediaParams.vocalTractStr || '0', //音频声道数
                    sampleRate: mediaParams.samplingRateStr || '8000', //音频采样率
                };
                if (playType) {
                    url = protocol + videoRequestUrl + ':' + videoRequestPort + '/' + simcardNumber + '/' + channelNum + '/0';
                    subscribeData.playType = 'REAL_TIME'; //播放类型(实时 REAL_TIME，回放 TRACK_BACK，对讲 BOTH_WAY，监听 UP_WAY，广播 DOWN_WAY)
                    subscribeData.dataType = '0'; //播放数据类型(0：音视频，1：视频，2：双向对讲，3：监听，4：中心广播，5：透传)
                    subscribeData.streamType = JSON.stringify(value.streamType); //码流类型(0：主码流，1：子码流)
                } else {
                    url = protocol + videoRequestUrl + ':' + videoResourcePort + '/' + simcardNumber + '/' + channelNum;
                    subscribeData.playType = 'TRACK_BACK'; //播放类型(实时 REAL_TIME，回放 TRACK_BACK，对讲 BOTH_WAY，监听 UP_WAY，广播 DOWN_WAY)
                    subscribeData.dataType = '1'; //播放数据类型(0：音视频，1：视频，2：双向对讲，3：监听，4：中心广播，5：透传)
                    subscribeData.streamType = '0'; //码流类型(0：主码流，1：子码流)
                    subscribeData.forwardOrRewind = '3';
                    subscribeData.remoteMode = '1';
                    subscribeData.storageType = '0';
                    subscribeData.startTime = startTime;
                    subscribeData.endTime = stopTime;
                }

                const rtpMediaPlayer = createRTPMediaPlayer(videoId, url, subscribeData);
                videoObjs.push(rtpMediaPlayer);

                const videoObj = document.getElementById(videoId);
                if (videoObj === undefined || videoObj.muted === undefined) {
                    return;
                }
                videoObj.muted = true;
            }
        }

        /**
         * 关闭视频
         */
        function stopPlay() {
            videoObjs.forEach(function (videoObj) {
                videoObj.closeSocket();
            });
            btnPlay.disabled = false;
        }

        /**
         * 开始结束时间格式转换
         */
        function newTimeFun(time) {
            if(time === '') return '';
            const date = new Date(time);
            const year = date.getFullYear();
            const month = joiningFun(date.getMonth() + 1);
            const day = joiningFun(date.getDate());
            const hours = joiningFun(date.getHours());
            const minutes = joiningFun(date.getMinutes());
            const seconds = joiningFun(date.getSeconds());

            return `${year}${month}${day}${hours}${minutes}${seconds}`
        }

        function joiningFun(num){
            return num < 10 ? '0' + num : num;
        }

        return {
            startPlay: startPlay,
            stopPlay: stopPlay,
        }
    }(videoLayout));

    (function () {
        const videoType = document.getElementById('videoType').value;

        if (videoType === '1') {
            document.querySelectorAll('.time-input').forEach(function (item) {
                item.hidden = true;
            });
        } else {
            laydate.render({
                elem: '#startTime',
                type: 'datetime',
            });

            laydate.render({
                elem: '#endTime',
                type: 'datetime',
            });
        }

        videoPlayer.startPlay(videoType);

        document.getElementById("btnPlay").onclick = () => videoPlayer.startPlay();
        document.getElementById("btnStop").onclick = () => videoPlayer.stopPlay();
        document.querySelectorAll(".layout").forEach(function (item) {
            item.onclick = function () {
                const windowsNum = parseInt(this.textContent, 10);
                videoLayout.changeLayout(windowsNum);
            }
        });

        window.onbeforeunload = function (event) {
            videoPlayer.stopPlay();
            event.returnValue = "正在关闭视频";
        }
    }(window));
}(window));