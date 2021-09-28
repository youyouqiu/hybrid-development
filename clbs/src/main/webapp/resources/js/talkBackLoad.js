//# sourceURL=talkBackLoad.js
;(function (window, $) {
    var talkBackTimeIndex, talkBackTime;
    var audioSocket = null;
    var talkBackLoad = {
        init: function () {
            //菜单隐藏
            $('#rMenu').css({"visibility": "hidden"});
            //模态框添加类样式
            $(".modal-body").addClass("modal-body-overflow");
            $(".modal-dialog").css("width", "300px");
            talkBackLoad.audioSendInstruct();
        },

        //对讲执行函数
        talkBackCarriedOutFn: function () {
            //替换暂停图标
            $("#talkBackControl").attr("src", "/clbs/resources/img/realTimeVideo/talkBack_pause.svg");
            //监听时间
            if (talkBackTime) {
                clearInterval(talkBackTime);
            }
            talkBackTimeIndex = 1;
            talkBackTime = setInterval(function () {
                $("#talkBackMsg").html("正在对讲 时间：" + (talkBackTimeIndex++) + "秒"); //替换显示状态
            }, 1000);
            //对讲后 添加类样式 区分是否成功
            $("#talkBackControl").removeAttr("class");
            $("#talkBackControl").addClass("talk-back-success");
        },

        // 关闭对讲
        closeWebsocket: function () {
            clearInterval(talkBackTime);
            talkBackTimeIndex = 1;
            $("#talkBackMsg").html('数据等待中...');
            if (audioSocket) {
                audioSocket.cmdCloseVideo();
                audioSocket.closeSocket();
            }
        },

        // 音频下发接口
        audioSendInstruct: function () {
            var vehicleId = $('#objId').attr('value'); // 20237
            var simcardNumber = $('#simcardNumberValue').attr('value');
            var channelNum = $('#channelValue').attr('value');
            var streamType = $("#streamType").attr('value');
            var channelNumType = $('#channelNumType').attr('value');
            json_ajax('POST', '/clbs/v/monitoring/audioAndVideoParameters/' + vehicleId, 'json', true, null, function (d) {
                if (d.success) {
                    talkBackLoad.openAudioTalkback(d.obj, simcardNumber, channelNum, vehicleId, streamType);
                }
            });
        },
        // 对讲socket连接
        openAudioTalkback: function (audioData, sNumber, cNum, vehicleId, streamType) {
            var simCardLength = sNumber.length;
            if (simCardLength < 12) {
                for (var i = 0; i < 12 - simCardLength; i++) {
                    sNumber = '0' + sNumber;
                }
            }

            var protocol = 'ws://';
            if (document.location.protocol === 'https:') {
                protocol = 'wss://';
            }
            console.log('对讲');
            var url = protocol + videoRequestUrl + ':' + audioRequestPort + '/' + sNumber + '/' + cNum + '/2';
            var data = {
                vehicleId: vehicleId,
                simcardNumber: sNumber,
                channelNumber: cNum,
                sampleRate: audioData.samplingRateStr || 8000,
                channelCount: audioData.vocalTractStr || 0,
                audioFormat: audioData.audioFormatStr,
                playType: 'BOTH_WAY',
                dataType: 2,
                userID: audioData.userUuid,
                deviceID: audioData.deviceId,
                streamType: streamType,
                deviceType: audioData.deviceType,
            };
            audioSocket = new RTPMediaPlayer({
                url: url,
                playType: 'BOTH_WAY',
                dataType: 2,
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
                        vehicleId: $data.vehicleId,                         // 车辆ID
                        simcardNumber: $data.simcardNumber,                        // sim卡号
                        channelNumber: $data.channelNumber,    // 通道号
                        sampleRate: JSON.stringify($data.sampleRate),       // 采样率
                        channelCount: JSON.stringify($data.channelCount),   // 声道数
                        audioFormat: $data.audioFormat,                     // 编码格式
                        playType: $data.playType,                           // 播放类型 实时 REAL_TIME，回放 TRACK_BACK，对讲 BOTH_WAY，监听 UP_WAY，广播 DOWN_WAY
                        dataType: JSON.stringify($data.dataType),           // 数据类型0：音视频，1：视频，2：双向对讲，3：监听，4：中心广播，5：透传
                        userID: $data.userID,                               // 用户ID
                        deviceID: $data.deviceID,                           // 终端ID
                        streamType: $data.streamType,       // 码流类型0：主码流，1：子码流
                        deviceType: $data.deviceType,
                    };
                    $$this.play(setting);
                },
                openAudioSuccess: function () {
                    talkBackLoad.talkBackCarriedOutFn();
                },
                openAudioFail: function (msg) {
                    layer.msg(msg);
                },
                // socket关闭成功
                socketCloseFun: function ($state) {
                    $('#commonSmWin').modal('hide');
                    if (talkBackTime) {
                        clearInterval(talkBackTime);
                        talkBackTime = null;
                    }
                },
            });
        }
    };
    $(function () {
        talkBackLoad.init();
        $('#closeAudio, #audioCloseBtn').on('click', talkBackLoad.closeWebsocket);
    })

})(window, $);