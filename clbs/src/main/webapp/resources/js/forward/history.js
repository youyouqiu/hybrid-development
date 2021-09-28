;(function (window) {
    let stompClient;
    let connected = false;
    let resourceTimeout = null;
    let playingChannelNum;
    let videoData = {};
    function doPost(url, data) {
        return fetch(url, {
            method: 'POST',
            body: data,
        }).then(res => res.json())
            .catch(error => console.error('post error:', error));
    }

    window.videoPlayer = (function () {
        let vehicleId;
        let vehicleName;
        let channelsStr;
        let videoPlayDom;
        let resourcesList;
        let createMseVideo;

        const videoObjs = new Map(); //保存播放中的实例化对象
        const uploadResources = new Map();
        let protocol = 'ws://';
        if (document.location.protocol === 'https:') {
            protocol = 'wss://';
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

        function startPlay() {
            channelsStr = getQueryVariable('Channel');
            const plateNumber = getQueryVariable('VehPlateNum');
            if (!plateNumber) {
                return;
            }
            vehicleName = decodeURIComponent(plateNumber);
            const url = '/clbs/v/monitoring/forward/monitorId';
            const data = new FormData();
            data.append('plateNum', vehicleName);

            doPost(url, data).then(function(response) {
                if (response.success) {
                    vehicleId = response.obj;
                    subscribeResource();
                } else {
                    window.alert('地址栏参数传递错误:' + url);
                }
            });
        }

        /**
         * 订阅成功后打开视频
         */
        function playVideo(subscribeData, videoId, url) {
            createMseVideo = new RTPMediaPlayer({
                domId: videoId,
                url: url,
                data: subscribeData,
                imgSrc:false,
                //状态
                onMessage: function($data, $msg) {
                    var code = JSON.parse($msg).data.msgBody.code; //状态
                    let videoId = $data.videoId;
                    let videoDom = document.getElementById(videoId);
                    switch (code) {
                        case -1004:
                            videoDom.style.backgroundImage = 'url(/clbs/resources/img/videoPrompt/video4.png)!important';
                            break;
                        case -1005:
                            videoDom.style.backgroundImage = 'url(/clbs/resources/img/videoPrompt/video7.png)!important';
                            break;
                        case -1006:
                            layer.msg('终端未响应,请重新订阅', { time: 2000 });
                            videoDom.style.backgroundImage = 'url(/clbs/resources/img/videoPrompt/video9.png)!important';
                            break;
                        default:
                            break;
                    }
                },
                //订阅
                socketOpenFun: function ($data, $$this) {
                    /*** socket连接建立成功* 图标提示替换和消息发送*/
                    const setting = {
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
                        startTime: $data.startTime,
                        endTime: $data.endTime,
                        remoteMode: $data.remoteMode,
                        forwardOrRewind: $data.forwardOrRewind,
                        storageType: $data.storageType,
                        deviceType: $data.deviceType,
                    };
                    $$this.play(setting);
                },
            });
            videoObjs.set(vehicleId, createMseVideo);
        }

        /**
         * 关闭视频
         */
        function stopPlay() {
            videoObjs.forEach(function (videoObj) {
                videoObj.closeSocket();
            });
        }


        function getResourceList() {
            const time = document.getElementById('time').value;
            let parameter = {
                "vehicleId": vehicleId,
                "brand": vehicleName,
                "alarmType": "0",
                "channlNumer": '0',
                "startTime": time + ' 00:00:00',
                "endTime": time + ' 23:59:59',
                "videoType": "0",
                "streamType": "0",
                "storageType": "0"
            };

            /** 下发查询历史终端视频资源*/
            stompClient.publish({ destination: "/app/video/history/day", body: JSON.stringify(parameter)});
        }

        /**
         * 订阅资源列表
         */
        function subscribeResource() {
            subscribe('/user/topic/video/history/day', resourceCallback);
        }

        function subscribeUpload() {
            subscribe('/topic/fileUploadStatus', uploadCallback);
        }

        function subscribe(url, callback) {
            setTimeout(function () {
                if (connected) {
                    stompClient.subscribe(url, callback);
                } else {
                    subscribe(url, callback)
                }
            }, 1500);
        }

        function formatTime(time) {
            return '20' + time.substring(0, 2) + '-' + time.substring(2, 4) + '-' + time.substring(4, 6) + ' ' +
                time.substring(6, 8) + ':' + time.substring(8, 10) + ':' + time.substring(10, 12);
        }

        function resourceCallback(data) {
            const message = JSON.parse(data.body);
            if (!message.success) {
                window.alert(message.msg);
                return;
            }
            if (resourceTimeout) {
                clearTimeout(resourceTimeout)
            }
            let messageObj = message.obj;
            if (messageObj === undefined || messageObj === null || messageObj === '') {
                resourceTimeout = setTimeout(function () {
                    layer.msg('获取终端视频数据失败,请稍后再试!');
                }, 20000);
                return;
            }

            resourcesList = messageObj.data.msgBody.resourcesList;
            videoData = {};

            const url = '/clbs/v/monitoring/forward/audioAndVideoParameters/' + vehicleId;
            doPost(url, url).then(function (response) {
                if(response.success){
                    videoData = response.obj;
                }
            });


            if ('content' in document.createElement('template')) {
                const template = document.querySelector('#resourceRow');
                const tbody = document.getElementById("tbodyList");
                tbody.innerHTML = '';
                for (const res of resourcesList) {
                    const resource = document.importNode(template.content, true);

                    const columns = resource.querySelectorAll("td");
                    columns[1].textContent = res.channelNum;
                    columns[2].textContent = formatTime(res.startTime);
                    columns[3].textContent = formatTime(res.endTime);

                    const buttons = resource.querySelectorAll("span");
                    buttons[0].onclick = videoClick;
                    buttons[1].onclick = videoDownload;
                    buttons[2].onclick = videoClick;

                    tbody.appendChild(resource);
                }
            }
        }

        function uploadCallback(data) {
            const message = JSON.parse(data.body);
            const result = message.data.msgBody.result;
            const msgSNACK = message.data.msgBody.msgSNACK;
            if (result === 1) {
                /** 上传文件失败*/
                return;
            }
            const resource = uploadResources.get(msgSNACK);
            if (resource == null) {
                return;
            }
            uploadResources.delete(msgSNACK);
            const mp4File = resource.channelNum + '_' + resource.startTime + '_' + resource.endTime + '.mp4';
            const link = document.createElement("a");
            if (document.location.protocol === 'https:') {
                link.href = '/mediaserver' + resource.filePath + '/' + mp4File;
            } else {
                link.href = 'http://' + resource.ftpServerIp + ':8799/mediaserver' + resource.filePath + '/' + mp4File;
            }
            link.download = vehicleName + '_' + mp4File;
            link.type = 'video/mp4';
            link.click();
        }

        /**
         * 播放,停止,快进
         */
        function videoClick(e) {
            const text = e.target.textContent;
            videoPlayDom = e.target;
            const row = videoPlayDom.parentElement.parentElement;
            const resource = resourcesList[row.rowIndex - 1];
            const simcardNumber = videoData.simcardNumber;
            playingChannelNum = resource.channelNum;
            const videoId = 'v_0_Source';
            const url = protocol + videoRequestUrl + ':' + videoResourcePort + '/' + simcardNumber + '/' + playingChannelNum;

            if(text === '播放') {
                stopPlay();
                videoObjs.clear();
                const videoPlayer = document.getElementsByClassName('videoPlayer');
                for (let i = 0; i < videoPlayer.length; i++) {
                    videoPlayer[i].innerHTML = '播放'
                }

                const subscribeData = {
                    channelCount: videoData.vocalTractStr || '0',
                    audioFormat: videoData.audioFormatStr,
                    playType: "TRACK_BACK",
                    dataType: "1",
                    userID: videoData.userUuid,
                    deviceID: videoData.deviceId,
                    sampleRate: videoData.samplingRateStr || '8000',
                    vehicleId: vehicleId,
                    channelNumber: JSON.stringify(playingChannelNum),
                    streamType: '0',
                    storageType: '0',
                    remoteMode: '0',
                    forwardOrRewind: '0',
                    startTime: resource.startTime,
                    endTime: resource.endTime,
                    videoId: videoId,
                    simcardNumber: simcardNumber,
                    deviceType: videoData.deviceType,
                };

                document.getElementById('monitorName').textContent = vehicleName + '-' + playingChannelNum;
                playVideo(subscribeData, videoId, url);
                videoPlayDom.textContent = '停止';
            }

            if(text === '停止'){
                createMseVideo.closeSocket();
                videoPlayDom.textContent = '播放';
            }

            if(text === '快进') {
                stopPlay();
                videoObjs.clear();

                const videoPlayer = document.getElementsByClassName('videoPlayer');
                for (let i = 0; i < videoPlayer.length; i++) {
                    videoPlayer[i].innerHTML = '播放'
                }
                const tr = document.getElementById('tbodyList').children[row.rowIndex - 1];
                tr.getElementsByClassName('videoPlayer')[0].textContent = '停止';

                const subscribeData = {
                    channelCount: videoData.vocalTractStr || '0',
                    audioFormat: videoData.audioFormatStr,
                    playType: "TRACK_BACK",
                    dataType: "1",
                    userID: videoData.userUuid,
                    deviceID: videoData.deviceId,
                    sampleRate: videoData.samplingRateStr || '8000',
                    vehicleId: vehicleId,
                    channelNumber: JSON.stringify(playingChannelNum),
                    streamType: '0',
                    storageType: '0',
                    remoteMode: '1',
                    forwardOrRewind: '3',
                    startTime: resource.startTime,
                    endTime: resource.endTime,
                    videoId: videoId,
                    simcardNumber: simcardNumber,
                };
                document.getElementById('monitorName').textContent = vehicleName + '-' + playingChannelNum;
                playVideo(subscribeData, videoId, url);
            }
        }

        /**
         * 下载
         */
        function videoDownload(e) {
            const row = e.target.parentElement.parentElement;
            const resource = resourcesList[row.rowIndex - 1];
            const uploadUrl = '/clbs/v/monitoring/forward/fileUpload';

            subscribeUpload();
            doPost(uploadUrl, getUploadParam(resource)).then(function (response) {
                if (response.success) { /** 上传文件指令下发成功*/
                    resource.filePath = response.obj.filePath;
                    resource.ftpServerIp = response.obj.ftpHost;
                    uploadResources.set(response.obj.msgId, resource);
                    return;
                }
                console.error(response.msg);
            });
        }

        function getUploadParam(resource) {
            const data = new FormData();
            data.append('vehicleId', vehicleId);
            data.append('channelNumber', resource.channelNum);
            data.append('startTime', resource.startTime);
            data.append('endTime', resource.endTime);
            data.append('alarmSign', resource.alarm);
            data.append('resourceType', resource.videoType);
            data.append('streamType', resource.streamType);
            data.append('storageAddress', resource.storageType);
            data.append('filesize', resource.fileSize);
            return data;
        }

        return {
            startPlay: startPlay,
            stopPlay: stopPlay,
            getResourceList: getResourceList,
        }
    }());

    (function () {
        stompClient = new StompJs.Client({
            webSocketFactory: () => new SockJS('/clbs/videoPlaybackForward'),
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
            onConnect () {
                connected = true; /** socket连接成功*/
            },
        });
        stompClient.activate();

        videoPlayer.startPlay();

        window.onbeforeunload = function (event) {
            videoPlayer.stopPlay();
            event.returnValue = "正在关闭视频";
        };

        //获取当前时间
        function getNowFormatDate() {
            var date = new Date();
            var separator = "-";
            var year = date.getFullYear();
            var month = date.getMonth() + 1;
            var strDate = date.getDate();
            if (month >= 1 && month <= 9) {
                month = "0" + month;
            }
            if (strDate >= 0 && strDate <= 9) {
                strDate = "0" + strDate;
            }
            return year + separator + month + separator + strDate;
        }

        document.getElementById("time").textContent = getNowFormatDate();
        laydate.render({
            elem: '#time',
            theme: '#6dcff6',
            type: 'date',
            max: getNowFormatDate(),
        });

        document.getElementById('queryClick').onclick = videoPlayer.getResourceList;
    }(window));
}(window));
