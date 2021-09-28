/**
 *
 */

(function (window, $) {
    var vehicleIds;
    var videoChannels;
    var lasrchannel = 1;
    var ifclickbrand = 0;
    //参考车辆
    var consulbrand = [];
    // 车辆list
    var videopareedit = {
        init: function () {
            var vehiclelistvalue = $("#vehicleList").attr("value");
            if (vehiclelistvalue) {
                var vehicleList = JSON.parse(vehiclelistvalue);
                // 初始化车辆数据
                var dataList = {
                    value: []
                };
                vehicleIds = $("#vehicleId").val();
                if (vehicleList != null && vehicleList.length > 0) {

                    for (var i = 0; i < vehicleList.length; i++) {
                        var obj = {};
                        //处理参考车牌只存在一条数据时防止浏览器抛出错误信息
                        if (vehicleList[i] == undefined) {
                            dataList.value.push(obj);
                        } else {
                            obj.id = vehicleList[i].id;
                            obj.name = vehicleList[i].brand;
                            if (vehicleList[i].vehicleId == vehicleIds) {
                            } else {
                                dataList.value.push(obj);
                            }
                        }
                    }
                }

                $("#brands").bsSuggest({
                    indexId: 1, //data.value 的第几个数据，作为input输入框的内容
                    indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                    idField: "id",
                    keyField: "name",
                    effectiveFields: ["name"],
                    searchFields: ["id"],
                    data: dataList
                }).on('onDataRequestSuccess', function (e, result) {
                }).on('onSetSelectValue', function (e, keyword, data) {
                    // 当选择参考车牌
                    ifclickbrand = 1;
                    var vId = keyword.id;
                    $("label").remove(".error").remove(".error2");

                    json_ajax("GET", '/clbs/realTimeVideo/videoSetting/view/' + vId, "json", true, null, videopareedit.getconsulbrand);
                    $("#dropdownul").hide();
                }).on('onUnsetSelectValue', function () {
                });
            }
            //获取当前车辆的音视频参数
            json_ajax("GET", '/clbs/realTimeVideo/videoSetting/view/' + vehicleIds, "json", true, null, videopareedit.getvideoinfo);

            $("#branchselect").on("change", function () {
                if ($("#branch").is(":checked")) {
                    videopareedit.changelogicsaveparam();
                }

            })

        },
        //切换车牌号绑定参考车牌参数
        getconsulbrand: function (data) {

            consulbrand = data.obj.videoSettings;
            if (data.obj.videoSettings != [] && data.obj.videoSettings != null && data.obj.videoSettings != "") {
                videopareedit.setvideoinfo(data);
            } else {
                videopareedit.osdvalueischecked(27);
            }
            //音视频通道
            if (data.obj.videoChannels != [] && data.obj.videoChannels != null && data.obj.videoChannels != "") {
                videopareedit.setvideoChannels(data);
            } else {
                $("#profile1").find(".addAudioandvideochannel").each(function () {
                    $(this).remove();
                })
                $(".addAudioandvideochannelfirst").css("display", "block");
                $(".Logicalchannelnumber").val("1");
            }
            //报警参数
            if (data.obj.vedioAlarms != [] && data.obj.vedioAlarms != null && data.obj.vedioAlarms != "") {
                videopareedit.alarminfo(data);
            }
            //休眠唤醒
            if (data.obj.videoSleep != [] && data.obj.videoSleep != null && data.obj.videoSleep != "") {

                videopareedit.Dormantwakeup(data);
            }
        },
        //根据车辆id查询音视频参数
        getvideoinfo: function (data) {

            //音视频参数
            if (data.obj.videoSettings != [] && data.obj.videoSettings != null && data.obj.videoSettings != "") {
                videopareedit.setvideoinfo(data);
            } else {
                videopareedit.osdvalueischecked(27);
            }
            //音视频通道
            if (data.obj.videoChannels != [] && data.obj.videoChannels != null && data.obj.videoChannels != "") {
                videopareedit.setvideoChannels(data);
            } else {
                $("#profile1").find(".addAudioandvideochannel").each(function () {
                    $(this).remove();
                })
                $(".addAudioandvideochannelfirst").css("display", "block");
                $(".Logicalchannelnumber").val("1");
            }
            //报警参数
            if (data.obj.vedioAlarms != [] && data.obj.vedioAlarms != null && data.obj.vedioAlarms != "") {
                videopareedit.alarminfo(data);
            }
            //休眠唤醒
            if (data.obj.videoSleep != [] && data.obj.videoSleep != null && data.obj.videoSleep != "") {

                videopareedit.Dormantwakeup(data);
            }
        },
        //音视频参数
        setvideoinfo: function (data) {
            var videoSettings = data.obj.videoSettings; //音视频参数
            if (videoSettings == undefined || videoSettings == "" || videoSettings == []) {

            } else if (videoSettings[0].allChannel == "1") {
                //清空所有input select
                $("#audiooutput").show();
                $("#alldata").prop("checked", true);
                //实时流设置
                $("#branchselect").val(1);
                $("#Realtimestreamcodingmode").val(videoSettings[0].realCodeSchema);
                $("#automaticUploadTime").val(videoSettings[0].realResolutionRatio);
                $("#Keyframeintervalofrealtimeflow").val(videoSettings[0].realKeyframeEvery);
                $("#Framerateofrealtimeflowtarget").val(videoSettings[0].realFrameRate);
                $("#Targetrateofrealtimeflow").val(videoSettings[0].realCodeRate);
                //存储流设置
                $("#Storagestreamcodingmode").val(videoSettings[0].saveCodeSchema);
                $("#Memoryflowresolution").val(videoSettings[0].saveResolutionRatio);
                $("#Keyframeintervalofstorageflow").val(videoSettings[0].saveKeyframeEvery);
                $("#Storagestreamtargetframerate ").val(videoSettings[0].saveFrameRate);
                $("#Memorystreamtargetbitrate").val(videoSettings[0].saveCodeRate);
                //osd字幕叠加设置

                if (videoSettings[0].osd !== "" && videoSettings[0].osd !== [] && videoSettings[0].osd !== undefined && videoSettings[0].osd !== null) {
                    videopareedit.osdvalueischecked(videoSettings[0].osd);
                }
                //是否启用音频输出
                $("#Audiooutput").find("input[type='radio'][value=" + videoSettings[0].audioSettings + "]").prop('checked', true);

            } else if (videoSettings[0].allChannel == "0") {
                videoSettings.sort(function (a, b) {
                    return a.logicChannel - b.logicChannel;
                });
                $("#audiooutput").hide();
                //清空所有input select
                $("#branch").prop("checked", true);
                var logicChannels = [];
                for (var i = 1; i < 17; i++) {
                    for (var j = 0; j < videoSettings.length; j++) {
                        if (i == parseInt(videoSettings[j].logicChannel))
                            $("#branchselect").find("option").eq(i - 1).attr("name", JSON.stringify(videoSettings[j]));
                    }
                }
                lasrchannel = videoSettings[0].logicChannel;
                $("#branchselect").val(videoSettings[0].logicChannel)
                $("#Realtimestreamcodingmode").val(videoSettings[0].realCodeSchema);
                $("#automaticUploadTime").val(videoSettings[0].realResolutionRatio);
                $("#Keyframeintervalofrealtimeflow").val(videoSettings[0].realKeyframeEvery);
                $("#Framerateofrealtimeflowtarget").val(videoSettings[0].realFrameRate);
                $("#Targetrateofrealtimeflow").val(videoSettings[0].realCodeRate);
                //存储流设置
                $("#Storagestreamcodingmode").val(videoSettings[0].saveCodeSchema);
                $("#Memoryflowresolution").val(videoSettings[0].saveResolutionRatio);
                $("#Keyframeintervalofstorageflow").val(videoSettings[0].saveKeyframeEvery);
                $("#Storagestreamtargetframerate ").val(videoSettings[0].saveFrameRate);
                $("#Memorystreamtargetbitrate").val(videoSettings[0].saveCodeRate);
                //osd字幕叠加设置
                if (videoSettings[0].osd !== "" && videoSettings[0].osd !== [] && videoSettings[0].osd !== undefined && videoSettings[0].osd !== null) {
                    videopareedit.osdvalueischecked(videoSettings[0].osd);
                }
                $("#Audiooutput").find("input[type='radio'][value=" + videoSettings[0].audioSettings + "]").prop('checked', true);

            }
        },
        //osd赋值
        osdvalueischecked: function (osd) {

            if (osd !== null && osd !== undefined && osd !== "") {
                if (osd != "0" && osd != "1" && osd != 0 && osd != 1) {
                    var osdlength = osd.toString(2).split("").length;
                    switch (osdlength) {
                        case 1:
                            var osdlist = "000000" + osd.toString(2);
                            break;
                        case 2:
                            var osdlist = "00000" + osd.toString(2);
                            break;
                        case 3:
                            var osdlist = "0000" + osd.toString(2);
                            break;
                        case 4:
                            var osdlist = "000" + osd.toString(2);
                            break;
                        case 5:
                            var osdlist = "00" + osd.toString(2);
                            break;
                        case 6:
                            var osdlist = "0" + osd.toString(2);
                            break;
                        default:
                            var osdlist = osd.toString(2);
                    }
                } else {
                    if (osd == "0" || osd == 0) {
                        var osdlist = "0000000"
                    } else {
                        var osdlist = "0000001";
                    }
                }
            }

            osdlist = osdlist.split("").reverse();
            osdname = ["radioone", "radiotwo", "radiothree", "radiofour", "radiofive", "radiosix", "radioseven"]
            for (var i = 0; i < osdlist.length; i++) {
                $('#tankBasisInfo-content').find("input[name=" + osdname[i] + "][value=" + osdlist[i] + "]").prop("checked", true);
            }
        },
        //切换逻辑通道号选择
        changelogic: function () {
            if (ifclickbrand == 1) {
                var logicChannelvalue = $("#branchselect option:selected").attr("value");
                for (var i = 1; i < consulbrand.length; i++) {
                    if (logicChannelvalue == consulbrand[i].logicChannel) {
                        $("#Realtimestreamcodingmode").val(consulbrand[i].realCodeSchema);
                        $("#automaticUploadTime").val(consulbrand[i].realResolutionRatio);
                        $("#Keyframeintervalofrealtimeflow").val(consulbrand[i].realKeyframeEvery);
                        $("#Framerateofrealtimeflowtarget").val(consulbrand[i].realFrameRate);
                        $("#Targetrateofrealtimeflow").val(consulbrand[i].realCodeRate);
                        //存储流设置
                        $("#Storagestreamcodingmode").val(consulbrand[i].saveCodeSchema);
                        $("#Memoryflowresolution").val(consulbrand[i].saveResolutionRatio);
                        $("#Keyframeintervalofstorageflow").val(consulbrand[i].saveKeyframeEvery);
                        $("#Storagestreamtargetframerate ").val(consulbrand[i].saveFrameRate);
                        $("#Memorystreamtargetbitrate").val(consulbrand[i].saveCodeRate);
                        //osd字幕叠加设置
                        if (consulbrand[i].osd) {
                            videopareedit.osdvalueischecked(consulbrand[i].osd);
                        }
                        //是否启用音频输出
                        $("#Audiooutput").find("input[type='radio'][value=" + consulbrand[i].audioSettings + "]").prop('checked', true);
                    } else {
                        $("#Realtimestreamcodingmode").val(0);
                        $("#automaticUploadTime").val(0);
                        $("#Keyframeintervalofrealtimeflow").val(8);
                        $("#Framerateofrealtimeflowtarget").val(8);
                        $("#Targetrateofrealtimeflow").val(329);
                        //存储流设置
                        $("#Storagestreamcodingmode").val(0);
                        $("#Memoryflowresolution").val(0);
                        $("#Keyframeintervalofstorageflow").val(8);
                        $("#Storagestreamtargetframerate ").val(8);
                        $("#Memorystreamtargetbitrate").val(329);
                        videopareedit.osdvalueischecked(27);
                    }
                }
            } else {
                var param = {
                    vehicleId: vehicleIds,
                    logicChannel: $("#branchselect option:selected").attr("value")
                }
                json_ajax("POST", "/clbs/realTimeVideo/videoSetting/videoParam", "json", true, param, videopareedit.setvideodata);
            }
            lasrchannel = $("#branchselect option:selected").attr("value");

        },
        setvideodata: function (data) {
            var obj = data.obj;

            //obj=JSON.parse(data);
            //实时流设置
            if (data.obj) {
                $("#Realtimestreamcodingmode").val(obj.realCodeSchema);
                $("#automaticUploadTime").val(obj.realResolutionRatio);
                $("#Keyframeintervalofrealtimeflow").val(obj.realKeyframeEvery);
                $("#Framerateofrealtimeflowtarget").val(obj.realFrameRate);
                $("#Targetrateofrealtimeflow").val(obj.realCodeRate);
                //存储流设置
                $("#Storagestreamcodingmode").val(obj.saveCodeSchema);
                $("#Memoryflowresolution").val(obj.saveResolutionRatio);
                $("#Keyframeintervalofstorageflow").val(obj.saveKeyframeEvery);
                $("#Storagestreamtargetframerate ").val(obj.saveFrameRate);
                $("#Memorystreamtargetbitrate").val(obj.saveCodeRate);
                //osd字幕叠加设置
                videopareedit.osdvalueischecked(obj.osd);
                //是否启用音频输出
                $("#Audiooutput").find("input[type='radio'][value=" + obj.audioSettings + "]").prop('checked', true);
            } else {
                $("#Realtimestreamcodingmode").val(0);
                $("#automaticUploadTime").val(0);
                $("#Keyframeintervalofrealtimeflow").val(8);
                $("#Framerateofrealtimeflowtarget").val(8);
                $("#Targetrateofrealtimeflow").val(329);
                //存储流设置
                $("#Storagestreamcodingmode").val(0);
                $("#Memoryflowresolution").val(0);
                $("#Keyframeintervalofstorageflow").val(8);
                $("#Storagestreamtargetframerate ").val(8);
                $("#Memorystreamtargetbitrate").val(329);
                videopareedit.osdvalueischecked(27);
            }
        },

        //显示更多
        showTankOrSensorInfoFn: function () {
            var clickId = $(this).context.id;
            if (!($("#" + clickId + "-content").is(":hidden"))) {
                $("#" + clickId + "-content").slideUp();
                $("#" + clickId).children("font").text("显示更多");
                $("#" + clickId).children("span").removeAttr("class").addClass("fa fa-chevron-down");
            } else {
                $("#" + clickId + "-content").slideDown();
                $("#" + clickId).children("font").text("隐藏参数");
                $("#" + clickId).children("span").removeAttr("class").addClass("fa fa-chevron-up");
            }
        },
        //绑定音视频通道参数
        setvideoChannels: function (data) {
            console.log(data, 'setvideoChannels');
            $(".addAudioandvideochannelfirst").nextAll().remove();

            var videoChannels = data.obj.videoChannels; //音视频通道
            videoChannels.sort(function (a, b) {
                return a.sort - b.sort;
            });
            $(".Physicalchannelnumber").eq(0).attr("lastvalue", '1');
            $(".Logicalchannelnumber").eq(0).attr("lastvalue", '1');
            if (videoChannels.length == 1) {
                $(".addAudioandvideochannelfirst").css("display", "block");
                $(".Physicalchannelnumber").val(videoChannels[0].physicsChannel);
                $(".Logicalchannelnumber").val(videoChannels[0].logicChannel);
                $(".audiofrequency").eq(0).val(videoChannels[0].channelType);
                $(".streamType").eq(0).val(videoChannels[0].streamType);
                if (videoChannels[0].logicChannel.toString() == "33" || videoChannels[0].logicChannel.toString() == "36" || videoChannels[0].logicChannel.toString() == "37") {
                    $(".audiofrequency").attr("disabled", "disabled");
                }
                /*if (videoChannels[0].channelType == "1") {
                    $(".audiofrequency").eq(0).parent().nextAll().hide();
                } else {
                    $(".audiofrequency").eq(0).parent().nextAll().show();
                }*/
                var newPanoramic = videoChannels[0].panoramic === true ? 1 : 0;
                $("#profile1").find("input[type='radio'][name='connectplatform'][value=" + videoChannels[0].connectionFlag + "]").prop('checked', true);
                $("#profile1").find("input[type='radio'][name='360form'][value=" + newPanoramic + "]").prop('checked', true);
            } else if (videoChannels.length > 1) {
                var newPanoramic = videoChannels[0].panoramic === true ? 1 : 0;
                $(".addAudioandvideochannelfirst").css("display", "block");
                $(".Physicalchannelnumber").val(videoChannels[0].physicsChannel);
                $(".Logicalchannelnumber").val(videoChannels[0].logicChannel);
                $(".audiofrequency").eq(0).val(videoChannels[0].channelType);
                $(".streamType").eq(0).val(videoChannels[0].streamType);
                $("#profile1").find("input[type='radio'][name='connectplatform'][value=" + videoChannels[0].connectionFlag + "]").prop('checked', true);
                $("#profile1").find("input[type='radio'][name='360form'][value=" + newPanoramic + "]").prop('checked', true);
                /*if (videoChannels[0].channelType == "1") {
                    $(".audiofrequency").eq(0).parent().nextAll().hide();
                } else {
                    $(".audiofrequency").eq(0).parent().nextAll().show();
                }*/
                if (videoChannels[0].logicChannel.toString() == "33" || videoChannels[0].logicChannel.toString() == "36" || videoChannels[0].logicChannel.toString() == "37") {
                    $(".audiofrequency").eq(0).attr("disabled", "disabled");
                }
                for (var i = 1; i < videoChannels.length; i++) {
                    videopareedit.addAudioandvideochannel2(i);
                    $(".Physicalchannelnumber").eq(i).val(videoChannels[i].physicsChannel);
                    $(".Physicalchannelnumber").eq(i).attr("lastvalue", videoChannels[i].physicsChannel);
                    $(".Logicalchannelnumber").eq(i).val(videoChannels[i].logicChannel);
                    $(".Logicalchannelnumber").eq(i).attr("lastvalue", videoChannels[i].logicChannel);
                    $(".audiofrequency").eq(i).val(videoChannels[i].channelType);
                    $(".streamType").eq(i).val(videoChannels[i].streamType);
                    if (videoChannels[i].logicChannel.toString() == "33" || videoChannels[i].logicChannel.toString() == "36" || videoChannels[i].logicChannel.toString() == "37") {
                        $(".audiofrequency").eq(i).attr("disabled", "disabled");
                    }
                    /*if (videoChannels[i].channelType == "1") {
                        $(".audiofrequency").eq(i).parent().nextAll().hide();
                    } else {
                        $(".audiofrequency").eq(i).parent().nextAll().show();
                    }*/
                    var j = $("#profile1").find(".Physicalchannelnumber:last").val();
                    $("#profile1").find(".isconnectplatform:last").find("input[type='radio']").each(function () {
                        $(this).attr("name", "connectplatform" + j + "");
                    })
                    $("#profile1").find(".isconnectplatform:last").find("input[type='radio'][value=" + videoChannels[i].connectionFlag + "]").prop('checked', true);

                    $("#profile1").find(".is360form:last").find("input[type='radio']").each(function () {
                        $(this).attr("name", "360form" + j + "");
                    })
                    var newPanoramic = videoChannels[i].panoramic === true ? 1 : 0;
                    $("#profile1").find(".is360form:last").find("input[type='radio'][value=" + newPanoramic + "]").prop('checked', true);
                }
            } else {
                $(".addAudioandvideochannel").remove();
                $(".addAudioandvideochannelfirst").css("display", "block");
                $(".Logicalchannelnumber").val("1");
            }
            videopareedit.setLinkShow();
        },
        //设置连接云台单选按钮是否显示
        setLinkShow: function () {
            var audiofrequency = $(".audiofrequency");
            var len = audiofrequency.length;
            for (var i = 0; i < len; i++) {
                var thisVal = $(audiofrequency[i]).val();
                var targetDiv = $(audiofrequency[i]).parents('.form-group').siblings().find('.linkPlatform');
                if (thisVal == '1') {
                    targetDiv.slideUp();
                } else {
                    targetDiv.slideDown();
                }
            }
        },
        //附加音频通道信息
        addAudioandvideochannel2: function () {
            // if ($(".addAudioandvideochannel").length < 20) {
                var t = $("#addAudioandvideochannel").children().clone();
                $("#profile1").children().append(t);
                $("#profile1").find(".addAudioandvideochannel:last").css("display", "block");

                $(".edit_Delete").on("click", function () {
                    $(this).parent().parent().parent().remove();
                });
                $(".audiofrequency").on('change', function () {
                    var thisVal = $(this).val();
                    var targetDiv = $(this).parents('.form-group').siblings().find('.linkPlatform');
                    if (thisVal == '1') {
                        targetDiv.slideUp();
                        // $(this).parent().nextAll().hide();
                    } else {
                        targetDiv.slideDown();
                        // $(this).parent().nextAll().show();
                    }
                });
                //物理通道号重复
                var beforePhysicalchannelnumber = [];
                var a = 0;
                $(".Physicalchannelnumber").each(function () {
                    beforePhysicalchannelnumber.push($(".Physicalchannelnumber").eq(a).val());
                    a++;
                })

                $(".Physicalchannelnumber").unbind("change").on("change", function () {
                    var lastvalue = $(this).attr("lastvalue"); //这次改变之前的值
                    var Physicalchannelnumber = [];
                    var b = 0;
                    $(".Physicalchannelnumber").each(function () {
                        Physicalchannelnumber.push($(".Physicalchannelnumber").eq(b).val());
                        b++;
                    })

                    function isRepeat(arr) {

                        var hash = {};

                        for (var i in arr) {

                            if (hash[arr[i]])

                                return true;

                            hash[arr[i]] = true;

                        }

                        return false;

                    }

                    var Physicalchannelnumber2 = [];
                    for (var i = 0; i < Physicalchannelnumber.length - 1; i++) {
                        Physicalchannelnumber2.push(Physicalchannelnumber[i]);
                    }
                    for (var i = 0; i < Physicalchannelnumber2.length; i++) {
                        if (Physicalchannelnumber2[i] == "" || typeof(Physicalchannelnumber2[i]) == "undefined" || Physicalchannelnumber2[i] == null) {
                            Physicalchannelnumber2.splice(i, 1);
                            i = i - 1;

                        }

                    }
                    if (isRepeat(Physicalchannelnumber2)) {
                        layer.msg("请选择未设置的通道号");
                        $(this).val(lastvalue);
                        //return false;
                        var j = 0;
                    } else {
                        $(this).attr("lastvalue", $(this).val());
                    }
                })
                //逻辑通道号重复

                $(".Logicalchannelnumber").unbind("change").on("change", function () {
                    var lastvalue = $(this).attr("lastvalue"); //这次改变之前的值
                    if ($(this).val() == "33" || $(this).val() == "36" || $(this).val() == "37") {
                        $(this).parent().parent().parent().parent().find(".audiofrequency").val("1").attr("disabled", "disabled");
                        // $(this).parent().parent().parent().parent().find(".audiofrequency").parent().nextAll().hide();
                    } else {
                        $(this).parent().parent().parent().parent().find(".audiofrequency").val("0").removeAttr("disabled");
                        // $(this).parent().parent().parent().parent().find(".audiofrequency").parent().nextAll().show();
                    }

                    var Logicalchannelnumber = [];
                    var c = 0;
                    $(".Logicalchannelnumber").each(function () {
                        if ($(".Logicalchannelnumber").eq(c).val() != "0") {
                            Logicalchannelnumber.push($(".Logicalchannelnumber").eq(c).val());
                        }
                        c++;
                    })

                    function isRepeat(arr) {

                        var hash = {};

                        for (var i in arr) {

                            if (hash[arr[i]])

                                return true;

                            hash[arr[i]] = true;

                        }

                        return false;

                    }

                    var Logicalchannelnumber2 = [];
                    for (var i = 0; i < Logicalchannelnumber.length - 1; i++) {
                        Logicalchannelnumber2.push(Logicalchannelnumber[i]);
                    }
                    if (isRepeat(Logicalchannelnumber2)) {
                        layer.msg("请选择未设置的通道号");
                        $(this).val(lastvalue);
                    } else {
                        $(this).attr("lastvalue", $(this).val());
                        var thisVal = $(this).val();
                        var targetDiv = $(this).parents('.form-group').siblings().find('.linkPlatform');
                        if (thisVal == "33" || thisVal == "36" || thisVal == "37") {
                            targetDiv.slideUp();

                            // $(this).parent().nextAll().hide();
                        } else {
                            targetDiv.slideDown();

                            // $(this).parent().nextAll().show();
                        }
                    }
                })
            // }
        },
        //点击增加音视频通道
        addAudioandvideochannel: function () {
            var Physicalchannelnumberdatalist = ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35"];
            var Physicalchannelnumberdata = [];
            $("#profile1").find(".Physicalchannelnumber").each(function () {
                Physicalchannelnumberdata.push($(this).val());
            })
            for (var j = 0; j < Physicalchannelnumberdata.length; j++) {
                Physicalchannelnumberdatalist.remove(Physicalchannelnumberdata[j]);
            }
            var k = parseInt($(".Physicalchannelnumber").eq($(".Physicalchannelnumber").length - 2).val());
            if ($(".addAudioandvideochannel").length < 35) {
                var t = $("#addAudioandvideochannel").children().clone();
                $("#profile1").children().append(t);
                $("#profile1").find(".addAudioandvideochannel:last").css("display", "block");
                if (Physicalchannelnumberdatalist[0] == "34" || Physicalchannelnumberdatalist[0] == "35") {
                    $("#profile1").find(".Logicalchannelnumber:last").val(parseInt(Physicalchannelnumberdatalist[0]) + 2);
                    $("#profile1").find(".Logicalchannelnumber:last").attr("lastvalue", parseInt(Physicalchannelnumberdatalist[0]) + 2);
                } else {
                    $("#profile1").find(".Logicalchannelnumber:last").val(Physicalchannelnumberdatalist[0]);
                    $("#profile1").find(".Logicalchannelnumber:last").attr("lastvalue", Physicalchannelnumberdatalist[0]);
                }
                $("#profile1").find(".Physicalchannelnumber:last").val(Physicalchannelnumberdatalist[0]);
                $("#profile1").find(".Physicalchannelnumber:last").attr("lastvalue", Physicalchannelnumberdatalist[0]);
                //aa++;
                $("#profile1").find(".isconnectplatform:last").find("input[type='radio']").each(function () {

                    $(this).attr("name", "connectplatform" + Physicalchannelnumberdatalist[0] + "");
                });
                $("#profile1").find(".is360form:last").find("input[type='radio']").each(function () {
                    $(this).attr("name", "360form" + Physicalchannelnumberdatalist[0] + "");
                });
                $(".edit_Delete").on("click", function () {
                    $(this).parent().parent().parent().remove();
                })
                $(".audiofrequency").on('change', function () {
                    var thisVal = $(this).val();
                    var targetDiv = $(this).parents('.form-group').siblings().find('.linkPlatform');
                    if (thisVal == '1') {
                        targetDiv.slideUp();
                        // $(this).parent().nextAll().hide();
                    } else {
                        targetDiv.slideDown();
                        // $(this).parent().nextAll().show();
                    }
                });
                //物理通道号重复
                var beforePhysicalchannelnumber = [];
                var k = 0;
                $(".Physicalchannelnumber").each(function () {
                    beforePhysicalchannelnumber.push($(".Physicalchannelnumber").eq(k).val());
                    k++;
                })
                $(".Physicalchannelnumber").unbind("change").on("change", function () {
                    var lastvalue = $(this).attr("lastvalue");
                    var Physicalchannelnumber = [];
                    var i = 0;
                    $(".Physicalchannelnumber").each(function () {
                        Physicalchannelnumber.push($(".Physicalchannelnumber").eq(i).val());
                        i++;
                    })

                    function isRepeat(arr) {

                        var hash = {};

                        for (var i in arr) {

                            if (hash[arr[i]])

                                return true;

                            hash[arr[i]] = true;

                        }
                        return false;
                    }

                    var Physicalchannelnumber2 = [];
                    for (var i = 0; i < Physicalchannelnumber.length - 1; i++) {
                        Physicalchannelnumber2.push(Physicalchannelnumber[i]);
                    }
                    if (isRepeat(Physicalchannelnumber2)) {
                        layer.msg("请选择未设置的通道号");
                        $(this).val(lastvalue);
                        //var j=0;

                    } else {
                        $(this).attr("lastvalue", $(this).val());
                    }
                })
                //逻辑通道号重复
                $(".Logicalchannelnumber").unbind("change").on("change", function () {
                    var lastvalue = $(this).attr("lastvalue"); //这次改变之前的值
                    if ($(this).val() == "33" || $(this).val() == "36" || $(this).val() == "37") {
                        $(this).parent().parent().parent().parent().find(".audiofrequency").val("1").attr("disabled", "disabled");
                        // $(this).parent().parent().parent().parent().find(".audiofrequency").parent().nextAll().hide();
                    } else {
                        $(this).parent().parent().parent().parent().find(".audiofrequency").val("0").removeAttr("disabled");
                        // $(this).parent().parent().parent().parent().find(".audiofrequency").parent().nextAll().show();
                    }
                    var Logicalchannelnumber = [];
                    var i = 0;
                    $(".Logicalchannelnumber").each(function () {
                        if ($(".Logicalchannelnumber").eq(i).val() != "0") {
                            Logicalchannelnumber.push($(".Logicalchannelnumber").eq(i).val());
                        }
                        i++;
                    })

                    function isRepeat(arr) {

                        var hash = {};

                        for (var i in arr) {

                            if (hash[arr[i]])

                                return true;

                            hash[arr[i]] = true;

                        }

                        return false;

                    }

                    var Logicalchannelnumber2 = [];
                    for (var i = 0; i < Logicalchannelnumber.length - 1; i++) {
                        Logicalchannelnumber2.push(Logicalchannelnumber[i]);
                    }
                    if (isRepeat(Logicalchannelnumber2)) {
                        layer.msg("请选择未设置的通道号");
                        $(this).val(lastvalue);
                    } else {
                        $(this).attr("lastvalue", $(this).val());
                        var thisVal = $(this).val();
                        var targetDiv = $(this).parents('.form-group').siblings().find('.linkPlatform');
                        if (thisVal == "33" || thisVal == "36" || thisVal == "37") {
                            targetDiv.slideUp();
                        } else {
                            targetDiv.slideDown();
                        }
                    }
                })
            }

        },
        alarminfo: function (data) {
            var data = data.obj.vedioAlarms;
            data.sort(function (pre, next) {
                return pre.pos - next.pos;
            })
            for (var i = 0; i < data.length; i++) {
                $(".selectvalue").eq(i).val(data[i].alarmPush);
                /*if (data[i].ignore == "1" || data[i].ignore == 1) {
                    $(".ignore").eq(i).prop("checked", true);
                } else {
                    $(".ignore").eq(i).prop("checked", false);
                }*/
            }
            $("#passengercarover").val(data[4].parameterValue);
            $("#Abnormaldrivingbehavior").val(data[5].parameterValue);
            videopareedit.selectPosition();
        },

        //定位滑块位置
        selectPosition: function () {
            $(".selectvalue").each(function () {
                var selectvalueno = $(this).val();
                selectvalueno = parseInt(selectvalueno);
                if (parseInt(selectvalueno) == 0) {
                    $(this).siblings('.selectbutton').css("left", "57px");
                } else if (parseInt(selectvalueno) == 1) {
                    $(this).siblings('.selectbutton').css("left", "105px");
                }else if (parseInt(selectvalueno) == -1) {
                    $(this).siblings('.selectbutton').css("left", "11px");
                } else {
                    $(this).siblings('.selectbutton').css("left", "152px");
                }
            })
        },
        //滑块
        selectSwitch: function () {
            $(".leftselectbutton span").on("click", function () {
                var leftbutton = $(this).siblings(".selectbutton").css("left");
                var left = $(this).css("left");
                var classname = $(this).attr("class");
                if (classname == "button1") {
                    $(this).parent().find(".selectvalue").val("0");
                } else if (classname == "button0") {
                    $(this).parent().find(".selectvalue").val("-1");
                } else if (classname == "button2") {
                    $(this).parent().find(".selectvalue").val("1");
                } else {
                    $(this).parent().find(".selectvalue").val("2");
                }
                $(this).siblings(".selectbutton").animate({
                    left: left
                }, "fast")
            })
        },
        //头部点击
        topswitch: function () {
            $("#profile2").find(".shielding").on("click", function () {
                $(this).parent().parent().find(".selectbutton").animate({
                    left: "11px"
                }, "fast");
                $(this).parent().parent().find(".selectvalue").val(-1);
            });
            $("#profile2").find(".noneset").on("click", function () {
                $(this).parent().parent().find(".selectbutton").animate({
                    left: "57px"
                }, "fast");
                $(this).parent().parent().find(".selectvalue").val(0);
            });
            $("#profile2").find(".partset").on("click", function () {
                $(this).parent().parent().find(".selectbutton").animate({
                    left: "105px"
                }, "fast");
                $(this).parent().parent().find(".selectvalue").val(1);
            });
            $("#profile2").find(".wholeset").on("click", function () {
                $(this).parent().parent().find(".selectbutton").animate({
                    left: "152px"
                }, "fast");
                $(this).parent().parent().find(".selectvalue").val(2);
            })
        },
        //获取休眠唤醒
        Dormantwakeup: function (data) {
            $(".wakeuptime1").removeAttr("disabled");
            $('.Conditionawakening').removeAttr("checked");
            $('.layer-date').removeAttr("disabled");
            var vedioRecording = data.obj.vedioRecording;
            var videoSleep = data.obj.videoSleep;
            if (vedioRecording != "" && vedioRecording != [] && vedioRecording != null) {
                $("#Overlimitofalarmparameters1").val(vedioRecording.thresholdValue);
                $("#Overlimitofalarmparameters2").val(vedioRecording.keepTime);
                $("#Overlimitofalarmparameters3").val(vedioRecording.startTime);
            }
            $("#Manualwake").val(videoSleep.wakeupHandSign);
            $("#Conditionawakening").val(videoSleep.wakeupConditionSign);
            $("#Timedwakeup").val(videoSleep.wakeupTimeSign);
            //条件唤醒
            if (videoSleep.wakeupConditionSign == "1") {
                $(".Conditionawakening-content").show();
                var Conditionawakening = videoSleep.wakeupCondition;
                videopareedit.conditioncheck(Conditionawakening);
            } else {
                $(".Conditionawakening-content").hide();
            }

            //定时唤醒
            if (videoSleep.wakeupTimeSign == "1") {
                $(".Timedwake-up-content").show();

                //var Timingwakesetting=videoSleep.wakeupTime.toString(2);
                var wakeupTime = videoSleep.wakeupTime;
                videopareedit.settimeparam1(wakeupTime);
                if (videoSleep.wakeupTimeFlag) {
                    var wakeup_time_flag = videoSleep.wakeupTimeFlag
                    videopareedit.settimeparam2(wakeup_time_flag);
                }
                $("#starttime1").val(videoSleep.wakeupTime1);
                $("#starttime2").val(videoSleep.wakeupTime2);
                $("#starttime3").val(videoSleep.wakeupTime3);
                $("#starttime4").val(videoSleep.wakeupTime4);
                $("#endtime1").val(videoSleep.wakeupClose1);
                $("#endtime2").val(videoSleep.wakeupClose2);
                $("#endtime3").val(videoSleep.wakeupClose3);
                $("#endtime4").val(videoSleep.wakeupClose4);
            } else {
                $(".Timedwake-up-content").hide();
            }
        },
        //条件唤醒复选框赋值
        conditioncheck: function (data) {
            datalist = data.toString(2);
            var datalength = datalist.length;
            switch (datalength) {
                case 1:
                    var datalist = "00" + data.toString(2);
                    break;
                case 2:
                    var datalist = "0" + data.toString(2);
                    break;
                default:
                    var datalist = data.toString(2);
            }
            datalist = datalist.split("").reverse();
            for (var i = 0; i < datalist.length; i++) {
                if (datalist[i] == "1") {
                    $(".Conditionawakening").eq(i).prop("checked", true);
                } else {
                    $(".Conditionawakening").eq(i).prop("checked", false);
                }
            }
        },
        //定时唤醒日设置
        settimeparam1: function (data) {
            if (data !== null && data !== undefined && data !== "") {
                //console.log("osd"+videoSettings[0].osd.toString(2));
                if (data != "0" && data != "1") {
                    var datalength = data.toString(2).split("").length;
                    switch (datalength) {
                        case 1:
                            var datalist = "000000" + data.toString(2);
                            break;
                        case 2:
                            var datalist = "00000" + data.toString(2);
                            break;
                        case 3:
                            var datalist = "0000" + data.toString(2);
                            break;
                        case 4:
                            var datalist = "000" + data.toString(2);
                            break;
                        case 5:
                            var datalist = "00" + data.toString(2);
                            break;
                        case 6:
                            var datalist = "0" + data.toString(2);
                            break;
                        default:
                            var datalist = data.toString(2);
                    }
                } else {
                    if (data == "0") {
                        var datalist = "0000000"
                    } else {
                        var datalist = "0000001";
                    }
                }

                datalist = datalist.split("").reverse();
                for (var i = 0; i < datalist.length; i++) {
                    if (datalist[i] == "1") {
                        $(".Timingwakesetting").eq(i).prop("checked", true);
                    } else {
                        $(".Timingwakesetting").eq(i).prop("checked", false);
                    }
                }
            }
        },
        //时间段唤醒时间
        settimeparam2: function (data) {
            if (data !== null && data !== undefined && data !== "") {
                //console.log("osd"+videoSettings[0].osd.toString(2));
                if (data != "0" && data != "1") {
                    var datalength = data.toString(2).split("").length;
                    switch (datalength) {
                        case 1:
                            var datalist = "000" + data.toString(2);
                            break;
                        case 2:
                            var datalist = "00" + data.toString(2);
                            break;
                        case 3:
                            var datalist = "0" + data.toString(2);
                            break;
                        default:
                            var datalist = data.toString(2);
                    }
                } else {
                    if (data == "0") {
                        var datalist = "0000"
                    } else {
                        var datalist = "0001";
                    }
                }

                datalist = datalist.split("").reverse();
                for (var i = 0; i < datalist.length; i++) {
                    if (datalist[i] == "1") {
                        $(".wakeuptime1").eq(i).prop("checked", true);
                    } else {
                        $(".wakeuptime1").eq(i).prop("checked", false);
                    }
                }
            }
        },
        leabelClickFn: function () {

            var left = $(this).parent().parent().parent().find(".selectbutton").css("left");
            if (left == "57px") {
                $(this).parent().parent().parent().find(".selectbutton").animate({
                    left: "105px"
                }, "fast");
                $(this).parent().parent().parent().find(".selectvalue").val(2);
            } else if (left == "152px") {
                $(this).parent().parent().parent().find(".selectbutton").animate({
                    left: "11px"
                }, "fast");
                $(this).parent().parent().parent().find(".selectvalue").val(-1);
            } else if (left == "11px") {
                $(this).parent().parent().parent().find(".selectbutton").animate({
                    left: "57px"
                }, "fast");
                $(this).parent().parent().parent().find(".selectvalue").val(0);
            } else {
                $(this).parent().parent().parent().find(".selectbutton").animate({
                    left: "152px"
                }, "fast");
                $(this).parent().parent().parent().find(".selectvalue").val(1);
            }
        },
        //判断是否有时间重复
        judgetimerepeat: function () {
            var startTimeArr = [];
            var endTimeArr = [];
            var timeE = '',
                timeS = '';
            for (var i = 0, len = $('.startTime').length; i < len; i++) {
                if ($('.startTime').eq(i).parent().parent().find('input[type="checkbox"]').is(":checked")) {
                    timeS = $('.startTime').eq(i).val();
                    startTimeArr.push(timeS);
                }
            }
            for (var j = 0, len = $('.endTime').length; j < len; j++) {
                if ($('.endTime').eq(j).parent().parent().find('input[type="checkbox"]').is(":checked")) {
                    timeE = $('.endTime').eq(j).val();
                    endTimeArr.push(timeE);
                }
            }
            var begin = startTimeArr.sort();
            var over = endTimeArr.sort();
            for (var k = 1; k < begin.length; k++) {
                if (begin[k] <= over[k - 1]) {
                    layer.msg("时间段存在重叠！");
                    return false;
                }
            }
            return true;
        },
        doSubmits: function () {

            videopareedit.validates();
            videopareedit.ischeckedlogicchangel();
            if (!$(".Logicalchannelnumber").nextAll().hasClass("error2") && videopareedit.validates()) {
                if (videopareedit.judgetimerepeat()) {

                    var videoSetting = [{}];
                    var videoChannelSettings = [];
                    var alarmParams = [];
                    var recordingSetting = {};
                    var videoSleepSetting = {};
                    //音视频参数
                    videoSetting[0].allChannel = ($("#alldata").is(":checked")) ? 1 : 0;
                    videoSetting[0].audioSettings = $("input[name=isenabled]:checked").val();
                    videoSetting[0].logicChannel = ($("#branch").is(":checked")) ? $("#branchselect").val() : 0;
                    var t = "";
                    $("#tankBasisInfo-content").find("input[type=radio]:checked").each(function () {
                        t += $(this).val();
                    })
                    t = t.split("").reverse().join("");
                    videoSetting[0].osd = parseInt(t, 2);
                    videoSetting[0].realCodeRate = $("#Targetrateofrealtimeflow").val(); //   实时流目标码率
                    videoSetting[0].realCodeSchema = $("#Realtimestreamcodingmode").val(); // 实时流编码模式
                    videoSetting[0].realFrameRate = $("#Framerateofrealtimeflowtarget").val(); // 实时流目标帧率
                    videoSetting[0].realKeyframeEvery = $("#Keyframeintervalofrealtimeflow").val(); // 实时流关键帧间隔
                    videoSetting[0].realResolutionRatio = $("#automaticUploadTime").val(); // 实时流分辨率
                    videoSetting[0].saveCodeRate = $("#Memorystreamtargetbitrate").val(); // 存储流目标码率
                    videoSetting[0].saveCodeSchema = $("#Storagestreamcodingmode").val(); // 存储流编码模式
                    videoSetting[0].saveFrameRate = $("#Storagestreamtargetframerate").val(); // 存储流目标帧率
                    videoSetting[0].saveKeyframeEvery = $("#Keyframeintervalofstorageflow").val(); // 存储流关键帧间隔
                    videoSetting[0].saveResolutionRatio = $("#Memoryflowresolution").val(); // 存储流分辨率
                    //音视频通道
                    for (var i = 0; i < $("#profile1").find(".audiofrequency").length; i++) {
                        videoChannelSettings[i] = {};
                        videoChannelSettings[i].streamType = $(".streamType").eq(i).val(); //码流类型
                        videoChannelSettings[i].channelType = $(".audiofrequency").eq(i).val(); // 通道类型
                        videoChannelSettings[i].physicsChannel = $("#profile1").find(".Physicalchannelnumber").eq(i).val(); // 物理通道号
                        videoChannelSettings[i].logicChannel = $("#profile1").find(".Logicalchannelnumber").eq(i).val(); // 逻辑通道号
                        videoChannelSettings[i].connectionFlag = $("#profile1").find(".isconnectplatform").eq(i).find("input[type='radio']:checked").val(); // 是否连接云台
                        var newPanoramic = $("#profile1").find(".is360form").eq(i).find("input[type='radio']:checked").val(); // 是否360
                        videoChannelSettings[i].panoramic = newPanoramic == 1 ? true : false;
                        videoChannelSettings[i].sort = i;
                    }
                    //报警参数
                    for (var i = 0; i < $(".selectvalue").length; i++) {
                        alarmParams[i] = {};
                        if (i == 4) {
                            alarmParams[i].paramValue = $("#passengercarover").val(); // 客运车核定载客人数
                        }
                        if (i == 5) {
                            alarmParams[i].paramValue = $("#Abnormaldrivingbehavior").val(); // 疲劳驾驶报警阈值
                        }
                        // alarmParams[i].ignore = ($(".ignore").eq(i).is(":checked")) ? "1" : "0"; // 是否屏蔽
                        alarmParams[i].alarmPush = $(".selectvalue").eq(i).val();
                        alarmParams[i].alarmName = $(".typeName").eq(i).text(); // 报警名称
                        alarmParams[i].pos = 125 + i;
                    }
                    //录像参数设置
                    recordingSetting.thresholdValue = $("#Overlimitofalarmparameters1").val(); // 报警录像占用主存储器存储阈值百分比
                    recordingSetting.keepTime = $("#Overlimitofalarmparameters2").val(); // 报警录像的最长持续时间
                    recordingSetting.startTime = $("#Overlimitofalarmparameters3").val(); // 报警发生前进行标记的录像时间

                    //休眠唤醒
                    videoSleepSetting.wakeupHandSign = $("#Manualwake").val(); // 手动唤醒
                    videoSleepSetting.wakeupConditionSign = $("#Conditionawakening").val(); // 唤醒条件
                    var m = ""
                    $(".Conditionawakening").each(function () {
                        if ($(this).is(":checked")) {
                            m += "1";
                        } else {
                            m += "0";
                        }

                    })
                    m = m.split("").reverse().join("");

                    videoSleepSetting.wakeupCondition = parseInt(m, 2); //
                    videoSleepSetting.wakeupTimeSign = $("#Timedwakeup").val(); // 定时唤醒
                    var n = "";
                    $(".Timingwakesetting").each(function () {
                        if ($(this).is(":checked")) {
                            n += "1";
                        } else {
                            n += "0";
                        }

                    })
                    n = n.split("").reverse().join("");
                    videoSleepSetting.wakeupTime = parseInt(n, 2); // 定时唤醒日
                    var x = ""
                    $(".wakeuptime1").each(function () {
                        if ($(this).is(":checked")) {
                            x += "1";
                        } else {
                            x += "0";
                        }

                    })
                    x = x.split("").reverse().join("");
                    videoSleepSetting.wakeupTimeFlag = parseInt(x, 2);
                    if ($("#wakeuptime1").is(":checked")) {
                        videoSleepSetting.wakeupTime1 = $("#starttime1").val(); //  时间段1唤醒时间
                        videoSleepSetting.wakeupClose1 = $("#endtime1").val(); //  时间段1关闭时间
                    }
                    if ($("#wakeuptime2").is(":checked")) {
                        videoSleepSetting.wakeupTime2 = $("#starttime2").val(); // 时间段2唤醒时间
                        videoSleepSetting.wakeupClose2 = $("#endtime2").val(); // 时间段2关闭时间
                    }
                    if ($("#wakeuptime3").is(":checked")) {
                        videoSleepSetting.wakeupTime3 = $("#starttime3").val(); // 时间段3唤醒时间
                        videoSleepSetting.wakeupClose3 = $("#endtime3").val(); // 时间段3关闭时间
                    }
                    if ($("#wakeuptime4").is(":checked")) {
                        videoSleepSetting.wakeupTime4 = $("#starttime4").val(); // 时间段4唤醒时间
                        videoSleepSetting.wakeupClose4 = $("#endtime4").val(); // 时间段4关闭时间
                    }
                    var data = {};
                    data.vehicleId = vehicleIds;
                    if (ifclickbrand == 0) {
                        data.videoSetting = videoSetting;
                    } else {
                        for (var i = 0; i < consulbrand.length; i++) {
                            delete consulbrand[i].id;
                        }
                        for (var i = 0; i < consulbrand.length; i++) {
                            if (videoSetting.logicChannel == consulbrand[i].logicChannel) {
                                consulbrand.remove(consulbrand[i]);

                            }
                        }
                        consulbrand.push(videoSetting[0]);
                        data.videoSetting = consulbrand;
                    }
                    data.videoChannelSettings = videoChannelSettings;
                    data.alarmParams = alarmParams;
                    data.recordingSetting = recordingSetting;
                    data.videoSleepSetting = videoSleepSetting;
                    var monitorType = $("#monitorType").val(); // 对象类型
                    data.monitorType = monitorType;
                    var videoParam = JSON.stringify(data);
                    console.log(data, 'videoParam');
                    var param = {
                        videoParam: videoParam
                    }
                    json_ajax("POST", '/clbs/realTimeVideo/videoSetting/save', "json", true, param, videopareedit.saveinfo);
                }
            }

        },
        saveinfo: function (data) {

            if (data.success == true) {
                layer.msg("保存成功");
                $("#commonWin").modal("hide");
                myTable.refresh();
            } else {
                layer.msg(data.msg);
            }

        },
        saveinfo2: function (data) {

            if (data.success == true) {
                layer.msg("保存成功");
                myTable.refresh();
            } else {
                layer.msg(data.msg);
            }

        },
        //验证
        validates: function () {
            return $("#settingForm").validate({
                ignore: [],
                rules: {
                    Keyframeintervalofrealtimeflow: {
                        required: true,
                        range: [1, 1000]
                    },
                    Framerateofrealtimeflowtarget: {
                        required: true,
                        range: [1, 120]
                    },
                    Targetrateofrealtimeflow: {
                        required: true,
                        range: [1, 4294967295]
                    },
                    Keyframeintervalofstorageflow: {
                        required: true,
                        range: [1, 1000]
                    },
                    Storagestreamtargetframerate: {
                        required: true,
                        range: [1, 120]
                    },
                    Memorystreamtargetbitrate: {
                        required: true,
                        range: [1, 4294967295]
                    },
                    passengercarover: {
                        range: [1, 100]
                    },
                    Abnormaldrivingbehavior: {
                        range: [0, 100]
                    },
                    Overlimitofalarmparameters1: {
                        range: [1, 99]
                    },
                    Overlimitofalarmparameters2: {
                        range: [1, 60]
                    },
                    Overlimitofalarmparameters3: {
                        range: [1, 60]
                    },
                    starttime1: {
                        isCheckedRequested2: "#wakeuptime1,#Timedwakeup",
                    },
                    starttime2: {
                        isCheckedRequested2: "#wakeuptime2,#Timedwakeup",
                    },
                    starttime3: {
                        isCheckedRequested2: "#wakeuptime3,#Timedwakeup",
                    },
                    starttime4: {
                        isCheckedRequested2: "#wakeuptime4,#Timedwakeup",
                    },
                    endtime1: {
                        isCheckedRequested2: "#wakeuptime1,#Timedwakeup",
                        isCheckedtime: "#wakeuptime1,#Timedwakeup,#starttime1,#endtime1"
                    },
                    endtime2: {
                        isCheckedRequested2: "#wakeuptime2,#Timedwakeup",
                        isCheckedtime: "#wakeuptime2,#Timedwakeup,#starttime2,#endtime2"
                    },
                    endtime3: {
                        isCheckedRequested2: "#wakeuptime3,#Timedwakeup",
                        isCheckedtime: "#wakeuptime3,#Timedwakeup,#starttime3,#endtime3"
                    },
                    endtime4: {
                        isCheckedRequested2: "#wakeuptime4,#Timedwakeup",
                        isCheckedtime: "#wakeuptime4,#Timedwakeup,#starttime4,#endtime4"
                    },

                },
                messages: {
                    Keyframeintervalofrealtimeflow: {
                        required: "不能为空",
                        range: "范围1~1000",
                    },
                    Framerateofrealtimeflowtarget: {
                        required: "不能为空",
                        range: "范围1~120",
                    },
                    Targetrateofrealtimeflow: {
                        required: "不能为空",
                        range: "范围1~4294967295",
                    },
                    Keyframeintervalofstorageflow: {
                        required: "不能为空",
                        range: "范围1~1000",
                    },
                    Storagestreamtargetframerate: {
                        required: "不能为空",
                        range: "范围1~120",
                    },
                    Memorystreamtargetbitrate: {
                        required: "不能为空",
                        range: "范围1~4294967295",
                    },
                    passengercarover: {
                        range: "范围1~100",
                    },
                    Abnormaldrivingbehavior: {
                        range: "范围0~100",
                    },
                    Overlimitofalarmparameters1: {
                        range: "范围1~99",
                    },
                    Overlimitofalarmparameters2: {
                        range: "范围1~60",
                    },
                    Overlimitofalarmparameters3: {
                        range: "范围1~60",
                    },
                    starttime1: {
                        isCheckedRequested2: "请选择时间",
                    },
                    starttime2: {
                        isCheckedRequested2: "请选择时间",
                    },
                    starttime3: {
                        isCheckedRequested2: "请选择时间",
                    },
                    starttime4: {
                        isCheckedRequested2: "请选择时间",
                    },
                    endtime1: {
                        isCheckedRequested2: "请选择时间",
                        isCheckedtime: "请选择在唤醒时间之后的时间"
                    },
                    endtime2: {
                        isCheckedRequested2: "请选择时间",
                        isCheckedtime: "请选择在唤醒时间之后的时间"
                    },
                    endtime3: {
                        isCheckedRequested2: "请选择时间",
                        isCheckedtime: "请选择在唤醒时间之后的时间"
                    },
                    endtime4: {
                        isCheckedRequested2: "请选择时间",
                        isCheckedtime: "请选择在唤醒时间之后的时间"
                    },
                }

            }).form();

        },
        //切换逻辑通道号时保存参数
        changelogicsaveparam: function () {
            layer.confirm('当前逻辑通道号参数有变更，是否保存?', {
                title: '操作确认',
                icon: 3, // 问号图标
                move: false, //禁止拖动
                btn: ['确定', '取消']
            }, function () {
                videopareedit.changelogicsaveparam2();
                setTimeout(videopareedit.changelogic(), 500);
            }, function () {

                videopareedit.changelogic();
            });
        },
        //保存前一个通道号的设置
        changelogicsaveparam2: function () {
            var videoSetting = {};
            //音视频参数
            videoSetting.allChannel = ($("#alldata").is(":checked")) ? 1 : 0;
            videoSetting.audioSettings = $("input[name=isenabled]:checked").val();
            videoSetting.logicChannel = lasrchannel;
            var t = "";
            $("#tankBasisInfo-content").find("input[type=radio]:checked").each(function () {
                t += $(this).val();
            })
            videoSetting.osd = parseInt(t, 2);
            videoSetting.realCodeRate = $("#Targetrateofrealtimeflow").val();
            videoSetting.realCodeSchema = $("#Realtimestreamcodingmode").val();
            videoSetting.realFrameRate = $("#Framerateofrealtimeflowtarget").val();
            videoSetting.realKeyframeEvery = $("#Keyframeintervalofrealtimeflow").val();
            videoSetting.realResolutionRatio = $("#automaticUploadTime").val();
            videoSetting.saveCodeRate = $("#Memorystreamtargetbitrate").val();
            videoSetting.saveCodeSchema = $("#Storagestreamcodingmode").val();
            videoSetting.saveFrameRate = $("#Storagestreamtargetframerate").val();
            videoSetting.saveKeyframeEvery = $("#Keyframeintervalofstorageflow").val();
            videoSetting.saveResolutionRatio = $("#Memoryflowresolution").val();
            videoSetting.vehicleId = vehicleIds;
            var videoParam = JSON.stringify(videoSetting);
            var param = {
                videoParam: videoParam
            }
            json_ajax("POST", '/clbs/realTimeVideo/videoSetting/saveVideoParam', "json", true, param, videopareedit.saveinfo2);
            if (ifclickbrand == 1) {
                for (var i = 0; i < consulbrand.length; i++) {
                    if (videoSetting.logicChannel == consulbrand[i].logicChannel) {
                        consulbrand.remove(consulbrand[i]);

                    }
                }
                consulbrand.push(videoSetting);
            }

        },
        //判断定是唤醒日是否设置，如果选择则时间段复选框可选，如果不选择时间断复选框不可选
        checkdata: function () {
            var SelectFalse = false; //用于判断是否被选择条件
            var CheckBox = $('input[name="Timingwakesetting"]'); //得到所的复选框
            for (var i = 0; i < CheckBox.length; i++) {
                if (CheckBox[i].checked) //如果有1个被选中时
                {
                    SelectFalse = true;
                    //chboxValue.push(CheckBox[i].value)//将被选择的值追加到
                }
            }
            if (!SelectFalse) {
                $(".wakeuptime1").each(function () {
                    $(this).attr("disabled", "disabled");
                    $(this).prop("checked", false);
                    $(".startTime").each(function () {
                        $(this).attr("disabled", "disabled");
                        $(this).val("");
                    })
                    $(".endTime").each(function () {
                        $(this).attr("disabled", "disabled");
                        $(this).val("");
                    })
                })
                return false
            } else {
                $(".wakeuptime1").each(function () {
                    $(this).removeAttr("disabled", "disabled");
                    $(".startTime").each(function () {
                        $(this).removeAttr("disabled", "disabled");
                    })
                    $(".endTime").each(function () {
                        $(this).removeAttr("disabled", "disabled");
                    })
                })
            }
        },
        //检测逻辑通道号是否选择
        ischeckedlogicchangel: function () {
            $(".Logicalchannelnumber").each(function () {
                if ($(this).val() == "0") {
                    $(this).nextAll().remove();
                    $(this).after("<label class='error2'>不能为空</label>");

                } else {
                    $(this).nextAll().remove();
                    return true;
                }
            })
            $(".Logicalchannelnumber").on("change", function () {
                if ($(this).val == "0") {

                } else {
                    $(this).nextAll().remove();
                }
            })

        },
        //判断关闭时间不能早于开始时间
        checkstarttime: function () {

        }

    };
    $(function () {
        Array.prototype.indexOf = function (val) {
            for (var i = 0; i < this.length; i++) {
                if (this[i] == val) return i;
            }
            return -1;
        };
        Array.prototype.remove = function (val) {
            var index = this.indexOf(val);
            if (index > -1) {
                this.splice(index, 1);
            }
        };
        //var $label = $("#profile2 label");
        $('input').inputClear();
        $(".modal-body").css({"height": "auto", "max-height": ($(window).height() - 194) + "px"});
        videopareedit.init();
        $("input[name='channel']").on("change", function () {
            if ($("#alldata").is(":checked")) {
                $("#audiooutput").show();
            } else {
                $("#audiooutput").hide();
            }
        });
        $("[data-toggle='tooltip']").tooltip();
        videopareedit.selectPosition();
        videopareedit.selectSwitch();
        videopareedit.topswitch();
        $(".typeName").bind("click", videopareedit.leabelClickFn);
        $(".typeName").mouseover(function () {
            $(this).css("color", "#6dcff6");
        });
        $(".typeName").mouseleave(function () {
            $(this).css("color", "#5D5F63");
        });
        $("#textinfo span").mouseover(function () {
            $(this).css("color", "#6dcff6");
        });
        $("#textinfo span").mouseleave(function () {
            $(this).css("color", "#5D5F63");
        });
        //videopareedit.getalarminfo();
        $("#Conditionawakening").on("change", function () {
            if ($("#Conditionawakening").find("option:selected").val() == "1") {
                $(".Conditionawakening-content").css("display", "block");
            } else {
                $(".Conditionawakening-content").css("display", "none");
            }
        })
        $("#Timedwakeup").on("change", function () {
            if ($("#Timedwakeup").find("option:selected").val() == "1") {
                $(".Timedwake-up-content").slideDown("fast");
                videopareedit.checkdata();
                $(".Timingwakesetting").each(function () {
                    $(this).on("change", videopareedit.checkdata);
                })
                //videopareedit.checkdata();
            } else {
                $(".Timedwake-up-content").slideUp("fast");
            }
        })
        $("#doSubmits").bind("click", videopareedit.doSubmits);
        $("#Realtimeflowsetting,#Storagestreamsetting,#tankBasisInfo").bind("click", videopareedit.showTankOrSensorInfoFn);
        $("#edit-add-btn").on("click", videopareedit.addAudioandvideochannel);
        lay('.layer-date').each(function () {
            var that = $(this);
            $(this).on("click", function () {
                that.next().remove();
            })
            laydate.render({
                elem: this,
                type: 'time',
                format: 'HH:mm',
                theme: '#6dcff6',
                trigger: 'click',
                done: function (value, date) {
                    if (value) {
                        that.next().remove();
                    } else {
                        that.nextAll().remove();
                        that.after('<label class="error" >请选择时间</label>');
                    }
                }
            });
        })
        $(".addAudioandvideochannelfirst").find(".Logicalchannelnumber").on("change", function () {
            if ($(this).val() == "33" || $(this).val() == "36" || $(this).val() == "37") {
                $(this).parent().parent().parent().parent().find(".audiofrequency").val("1").attr("disabled", "disabled");
                // $(this).parent().parent().parent().parent().find(".audiofrequency").parent().nextAll().hide();
            } else {
                $(this).parent().parent().parent().parent().find(".audiofrequency").val("0").removeAttr("disabled");
                // $(this).parent().parent().parent().parent().find(".audiofrequency").parent().nextAll().show();
            }

        })
        /*$(".addAudioandvideochannelfirst").find(".audiofrequency").on("change", function () {
            if ($(this).val() == "1") {
                //$(this).parent().parent().parent().parent().find(".audiofrequency").parent().nextAll().hide();
                $(this).parent().parent().parent().parent().find(".form-group:nth-child(3)").children().hide();
            } else {
                //$(this).parent().parent().parent().parent().find(".audiofrequency").parent().nextAll().show();
                $(this).parent().parent().parent().parent().find(".form-group:nth-child(3)").children().show();
            }

        })*/
        $(".Timingwakesetting").each(function () {
            $(this).on("change", videopareedit.checkdata);
        });
        //设置连接云台是否显示
        $(".Logicalchannelnumber").on('change', function () {
            var thisVal = $(this).val();
            var targetDiv = $(this).parents('.form-group').siblings().find('.linkPlatform');
            if (thisVal == "33" || thisVal == "36" || thisVal == "37") {
                targetDiv.slideUp();
            } else {
                targetDiv.slideDown();
            }
        });
        $(".audiofrequency").on('change', function () {
            var thisVal = $(this).val();
            var targetDiv = $(this).parents('.form-group').siblings().find('.linkPlatform');
            if (thisVal == '1') {
                targetDiv.slideUp();

                // $(this).parent().nextAll().hide();
            } else {
                targetDiv.slideDown();

                // $(this).parent().nextAll().show();
            }
        });
        videopareedit.setLinkShow();
    });
})(window, $);