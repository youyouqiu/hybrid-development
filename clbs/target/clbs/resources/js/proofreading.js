(function (window, $) {
    var headers = {"UserName": $("#userName").text()};
    $("#vNumber").text($("#vNum").val())
    var flag = true;

    //车载终端数据
    var terminalSim, terminalIccid, terminalNum, terminalImei, terminalImsi, terminalDay, terminalMonth,
        terminalCorrection,
        terminalWarning, terminalHourFlow, terminalDayFlow, terminalMonthFlow, terminalMonthSettlement,
        terminalmonthTrafficDeadline;
    //F3平台数据
    var FThreeSim, FThreeIccid, FThreeNum, FThreeImei, FThreeImsi, FThreeDay, FThreeMonth, FThreeCorrection,
        FThreeWarning,
        FThreeHourFlow, FThreeDayFlow, FThreeMonthFlow, FThreeMonthSettlement, FThreemonthTrafficDeadline;
    //物联网卡平台
    var cardSim, cardIccid, cardNum, cardImei, cardImsi, cardDay, cardMonth, cardMonthSettlement,
        cradMonthTrafficDeadline;
    var realTimeProofreading = {
        init: function () {
            var sendUrl = "/clbs/m/basicinfo/equipment/simcard/sendSimP";
            var data = {
                "simId": $("#sid").val(),
                "vehicleId": $("#vid").val(),
                "parameterName": $("#bindId").val(),
                "type": 1
            };
            json_ajax("POST", sendUrl, "json", false, data, realTimeProofreading.getSCallBack);
            //设置模态框宽度
            $("#prooFreadingForm").parent().parent().css("width", "90%");

        },
        //修正
        btnCorrection: function () {
            var monthTrafficDeadline = $("#terminalmonthTrafficDeadline").val();// 车载终端的流量最后更新时间
            var cradMonthTrafficDeadline = $("#cradMonthTrafficDeadline").val(); // 物联网卡平台的流量最后更想时间
            if (monthTrafficDeadline !== undefined && cradMonthTrafficDeadline !== undefined) {
                // 车载终端的流量最后更新时间
                var trafficDeadlineDate = realTimeProofreading.timeStampString(monthTrafficDeadline).getTime();
                // 物理网平台的流量最后更新时间
                var cradMonthTrafficDeadlineDate = realTimeProofreading.timeStampString(cradMonthTrafficDeadline).getTime();
                // 如果不是同一天,不能修正下发.
                // 修正数据车载终端当月流量 = 卡平台当月流量(截止昨天的流量) + 车载终端的当日流量.所以要求两个日期都在同一天才能进行修正
                if (trafficDeadlineDate !== cradMonthTrafficDeadlineDate) {
                    layer.msg("当前流量数据不是最新，不能下发修正");
                } else {
                    realTimeProofreading.alrtCard();
                }
            }
        },
        refreshSimInfo: function () {
            terminalSim = $("#terminalSim").val(); 							//SIM卡号
            terminalIccid = $("#terminalIccid").val();						//ICCID
            terminalNum = $("#terminalNum").val(); 							//终端编号
            terminalImei = $("#terminalImei").val();						//IMEI
            terminalImsi = $("#terminalImsi").val();						//IMSI
            terminalDay = $("#terminalDay").val();							//当日流量
            terminalMonth = $("#terminalMonth").val();						//当月流量
            terminalCorrection = $("#terminalCorrection").val();			//修正系数
            terminalWarning = $("#terminalWarning").val();					//预警系数
            terminalHourFlow = $("#terminalHourFlow").val();				//小时流量阈值
            terminalDayFlow = $("#terminalDayFlow").val();					//日流量阈值
            terminalMonthFlow = $("#terminalMonthFlow").val();				//月流量阈值
            terminalMonthSettlement = $("#terminalMonthSettlement").val();	//流量月结日
            terminalmonthTrafficDeadline = $("#terminalmonthTrafficDeadline").val(); //流量最后更新时间
            //F3平台数据
            FThreeSim = $("#FThreeSim").val(); 								//终端手机号
            FThreeIccid = $("#FThreeIccid").val();							//ICCID
            FThreeNum = $("#FThreeNum").val(); 								//终端编号
            FThreeImei = $("#FThreeImei").val();							//IMEI
            FThreeImsi = $("#FThreeImsi").val();							//IMSI
            FThreeDay = $("#FThreeDay").val();								//当日流量
            FThreeMonth = $("#FThreeMonth").val();							//当月流量
            FThreeCorrection = $("#FThreeCorrection").val();				//修正系数
            FThreeWarning = $("#FThreeWarning").val();						//预警系数
            FThreeHourFlow = $("#FThreeHourFlow").val();					//小时流量阈值
            FThreeDayFlow = $("#FThreeDayFlow").val();						//日流量阈值
            FThreeMonthFlow = $("#FThreeMonthFlow").val();					//月流量阈值
            FThreeMonthSettlement = $("#FThreeMonthSettlement").val();		//流量月结日
            FThreemonthTrafficDeadline = $("#FThreemonthTrafficDeadline").val(); //流量最后更新时间
            //物联网卡平台
            cardSim = $("#cardSim").val(); 									//终端手机号
            cardIccid = $("#cardIccid").val();								//ICCID
            cardNum = $("#cardNum").val(); 									//终端编号
            cardImei = $("#cardImei").val();								//IMEI
            cardImsi = $("#cardImsi").val();								//IMSI
            cardDay = $("#cardDay").val();									//当日流量
            cardMonth = $("#cardMonth").val();								//当月流量
            cardMonthSettlement = $("#cardMonthSettlement").val();			//流量月结日
            cradMonthTrafficDeadline = $("#cradMonthTrafficDeadline").val(); //流量最后更新时间
        },
        alrtCard: function () {
            //车载终端数据
            realTimeProofreading.refreshSimInfo();
            cardSim = realTimeProofreading.formatSimCardNumber(cardSim);
            //验证卡平台数据
            if (cardImsi === "" || cardSim === "" || cardMonth === "") {
                layer.confirm('物联网卡平台未发现此卡，是否继续修正~', {
                    btn: ['继续', '取消'] //按钮
                }, function (carryOn) {
                    //将F3平台中部分数据追加到车载终端数据(修正系数以下参数)
                    if (FThreeCorrection !== "") {
                        $("#terminalCorrection").val(FThreeCorrection);		//车载终端-修正系数
                    } else {
                        $("#terminalCorrection").val();
                    }
                    if (FThreeWarning !== "") {
                        $("#terminalWarning").val(FThreeWarning);			//车载终端-预警系数
                    } else {
                        $("#terminalWarning").val();
                    }
                    if (FThreeHourFlow !== "") {
                        $("#terminalHourFlow").val(FThreeHourFlow);			//车载终端-小时流量阈值
                    } else {
                        $("#terminalHourFlow").val();
                    }
                    if (FThreeDayFlow !== "") {
                        $("#terminalDayFlow").val(FThreeDayFlow);			//车载终端-日流量阈值
                    } else {
                        $("#terminalDayFlow").val();
                    }
                    if (FThreeMonthFlow !== "") {
                        $("#terminalMonthFlow").val(FThreeMonthFlow);		//车载终端-月流量阈值
                    } else {
                        $("#terminalMonthFlow").val();
                    }
                    $("#FThreeImei").val(terminalImei);
                    $("#cardImei").val(terminalImei);
                    if (terminalNum !== "") {
                        $("#cardNum").val(terminalNum);
                    }
                    //流量月结日
                    if (FThreeMonthSettlement !== "") {
                        $("#terminalMonthSettlement").val(FThreeMonthSettlement);
                    } else {
                        $("#terminalMonthSettlement").val(1);
                    }
                    //日流量
                    //IMSI
                    if (terminalImsi !== "") {
                        $("#FThreeImsi").val(terminalImsi);
                    } else {
                        $("#FThreeImsi").val();
                    }
                    //ICCID
                    if (terminalIccid !== "") {
                        $("#FThreeIccid").val(terminalIccid);
                    } else {
                        $("#FThreeIccid").val();
                    }
                    if (terminalMonthSettlement !== "") {
                        $("#FThreeMonthSettlement").val(terminalMonthSettlement);
                    }
                    if (cradMonthTrafficDeadline !== "") {
                        $("#FThreemonthTrafficDeadline").val(cradMonthTrafficDeadline);
                    }
                    //去掉背景
                    $("#FThreemonthTrafficDeadline,#FThreeMonth,#FThreeDay,#FThreeImsi,#FThreeImei,#FThreeIccid,#FThreeSim,#terminalCorrection,#terminalWarning,#terminalHourFlow,#terminalDayFlow,#terminalMonthFlow,#terminalSim").css("background-color", "#fafafa");
                    $("#btn-issued").attr("disabled", false);
                    layer.close(carryOn);
                    return true;
                }, function (cancel) {
                    layer.close(cancel);
                    return false;
                });
            } else {
                if (cardMonth !== "" && terminalDay !== "") {
                    //月流量，日流量
                    var realMonth = (parseFloat(cardMonth) + parseFloat(terminalDay)).toFixed(2);
                    $("#FThreeMonth").val(realMonth);
                    $("#terminalMonth").val(realMonth);
                }
                if (terminalDay !== "") {
                    $("#FThreeDay").val(terminalDay);
                }
                //ICCID\IMSI
                if (cardIccid !== "" && cardImsi !== "") {
                    //如果卡平台两者都存在，则写入车载终端
                    $("#terminalIccid").val(cardIccid);	//ICCID
                    //再写入F3数据平台
                    $("#FThreeIccid").val(cardIccid);	//ICCID
                    $("#FThreeImsi").val(terminalImsi);		//IMSI
                } else {
                    //卡平台不存在ICCID及IMSI，则从车载终端直接写入F3数据平台
                    $("#FThreeIccid").val(terminalIccid);	//ICCID
                    $("#FThreeImsi").val(terminalImsi);		//IMSI
                }
                //流量月结日
                if (cardMonthSettlement !== "") {
                    $("#terminalMonthSettlement").val(cardMonthSettlement);		//卡平台存在写入车载终端
                    $("#FThreeMonthSettlement").val(1);		//再写入F3
                } else {
                    //如果不存在，检测F3是否存在
                    if (FThreeMonthSettlement !== "") {
                        $("#terminalMonthSettlement").val(FThreeMonthSettlement);		//F3存在写入车载终端
                    }
                }
                //IMEI
                $("#FThreeImei").val(terminalImei);
                $("#cardImei").val(terminalImei);
                if (terminalNum !== "") {
                    $("#cardNum").val(terminalNum);
                }
                //将F3平台中部分数据追加到车载终端数据(修正系数以下参数)
                if (FThreeCorrection !== "") {
                    $("#terminalCorrection").val(FThreeCorrection);		//车载终端-修正系数
                } else {
                    $("#terminalCorrection").val();
                }
                if (FThreeWarning !== "") {
                    $("#terminalWarning").val(FThreeWarning);			//车载终端-预警系数
                } else {
                    $("#terminalWarning").val();
                }
                if (FThreeHourFlow !== "") {
                    $("#terminalHourFlow").val(FThreeHourFlow);			//车载终端-小时流量阈值
                } else {
                    $("#terminalHourFlow").val();
                }
                if (FThreeDayFlow !== "") {
                    $("#terminalDayFlow").val(FThreeDayFlow);			//车载终端-日流量阈值
                } else {
                    $("#terminalDayFlow").val();
                }
                if (FThreeMonthFlow !== "") {
                    $("#terminalMonthFlow").val(FThreeMonthFlow);		//车载终端-月流量阈值
                } else {
                    $("#terminalMonthFlow").val();
                }
                if (terminalMonthSettlement !== "") {
                    $("#FThreeMonthSettlement").val(terminalMonthSettlement);
                }
                if (cradMonthTrafficDeadline !== "") {
                    $("#FThreemonthTrafficDeadline").val(cradMonthTrafficDeadline);
                }
                //去掉背景
                $("#FThreemonthTrafficDeadline,#FThreeMonth,#FThreeDay,#FThreeImsi,#FThreeImei,#FThreeIccid,#terminalCorrection,#terminalWarning,#terminalHourFlow,#terminalDayFlow,#terminalMonthFlow").css("background-color", "#fafafa");
                $("#btn-issued").attr("disabled", false);
            }
        },
        updateSimCardInfo: function () {
            var data = {
                "form": "{'sid':'" + $("#sid").val() + "','simcardNumber':'" + $("#FThreeSim").val() +
                    "','iccid':'" + $("#FThreeIccid").val() + "','imei':'" + $("#FThreeImei").val() + "','imsi':'" +
                    $("#FThreeImsi").val() + "','dayRealValue':'" + $("#FThreeDay").val() + "','monthRealValue':'" +
                    $("#FThreeMonth").val() + "','correctionCoefficient':'" + $("#FThreeCorrection").val() +
                    "','forewarningCoefficient':'" + $("#FThreeWarning").val() + "','hourThresholdValue':'" + $("#FThreeHourFlow").val() +
                    "','dayThresholdValue':'" + $("#FThreeDayFlow").val() + "','monthThresholdValue':'" + $("#FThreeMonthFlow").val() +
                    "','monthTrafficDeadline':'" + FThreemonthTrafficDeadline + "','monitorType':'" + $("#monitorType").val() + "'}"
            };
            var updateUrl = "/clbs/m/basicinfo/equipment/simcard/updataSimCradInfo";
            json_ajax("POST", updateUrl, "json", false, data, realTimeProofreading.updataSimCallBack);
        },
        //下发
        btnIssued: function () {
            // var checkUrl="/clbs/m/infoconfig/infoinput/checkSimIsBound";
            // json_ajax("POST",checkUrl,"json",false,{"sid":$("#FThreeSim").val()},realTimeProofreading.checkCallBack);
            var sendUrl = "/clbs/m/basicinfo/equipment/simcard/sendSimP";
            var obj = {
                "simId": $("#sid").val(), "vehicleId": $("#vid").val(), "parameterName": $("#bindId").val(),
                "type": 0, "upTime": $("#cradMonthTrafficDeadline").val(), "realId": $("#cardSim").val()
            };
            json_ajax("POST", sendUrl, "json", false, obj, realTimeProofreading.sendSCallBack);
            // 将物联网卡平台返回的终端手机号保存到对应的平台终端手机号信息的真实卡号字段中
            var url = "/clbs/m/basicinfo/equipment/simcard/updateRealId";
            var data = {"simCardId": $("#sid").val(), "realCard": $("#cardSim").val()};
            json_ajax("POST", url, "json", false, data, null);
            // 记录日志
            var logUrl = "/clbs/m/basicinfo/equipment/simcard/simIssueLog";
            var logData = {"vid": $("#vid").val()};
            json_ajax("POST", logUrl, "json", false, logData, null);

        },
        btnRefresh: function () {
            flag = true;
            var sendUrl = "/clbs/m/basicinfo/equipment/simcard/sendSimP";
            var data = {
                "simId": $("#sid").val(),
                "vehicleId": $("#vid").val(),
                "parameterName": $("#bindId").val(),
                "type": 1
            };
            json_ajax("POST", sendUrl, "json", false, data, realTimeProofreading.getSCallBack);
        },
        simCallBack: function (data) {
            layer.closeAll('loading');
            if (data.code === "0") {
                $("#cardSim").val(data.result.simNum);
                $("#cardIccid").val(data.result.iccid);
                $("#cardImsi").val(data.result.imsi);
                $("#cardDay").val((Number(data.result.trafficUsedThisDay) / 1024).toFixed(2));
                $("#cardMonth").val((Number(data.result.trafficUsedThisMonth) / 1024).toFixed(2));
                $("#cradMonthTrafficDeadline").val(data.result.updateTime);
                if ($("#FThreemonthTrafficDeadline").val() !== $("#cradMonthTrafficDeadline").val()) {
                    $("#FThreemonthTrafficDeadline").css("background-color", "#ffe6e6");
                }
                if ($("#FThreeMonth").val() !== (parseFloat($("#cardMonth").val()) + parseFloat($("#terminalDay").val())).toFixed(2)) {
                    $("#FThreeMonth").css("background-color", "#ffe6e6");
                }
            } else {
                layer.msg("同学，来张我们的物联网卡呗，行走江湖，居家必备哟~")
            }
        },
        updataSimCallBack: function (data) {
            if (data.success === false) {
                layer.msg("修正失败，请联系管理员。")
            }
        },
        sendSCallBack: function (data) {
            if (data.success === true) {
                layer.msg("下发成功，终端手机号有修正请耐心等待。");
                realTimeProofreading.updateSimCardInfo();
            } else {
                layer.msg("下发失败")
            }
        },
        getSCallBack: function (data) {
            emsgACK = parseInt(data.msg);
            // webSocket.subscribe(headers, '/topic/fencestatus', realTimeProofreading.getLInfoCallBack, null, null);
            webSocket.subscribe(headers, "/user/" + $("#userName").text() + "/alarm_parameter_setting",
                realTimeProofreading.getLInfoCallBack, null, null);
            if (data.success !== true) {
                $("#btn-correction").attr("disabled", "disabled");
                $("#btn-issued").attr("disabled", "disabled");
                layer.msg("获取设备数据失败")
            }

        },
        getLInfoCallBack: function (data) {
            var message = JSON.parse(data.body);
            var r = message.data.msgBody.result;
            var mnumber = message.data.msgHead.mobile;
            var msgSNACK = message.data.msgBody.msgSNACK;
            if (r === 1 && $("#terminalSim").val() === mnumber) {
                flag = false;
                $("#btn-correction").attr("disabled", "disabled");
                $("#btn-issued").attr("disabled", "disabled");
                layer.closeAll('loading');
                layer.msg("获取设备数据失败")
            } else if (r === 0 && emsgACK === msgSNACK) {
                var url = "/clbs/v/oilmassmgt/oilcalibration/getLatestOilData";
                var obj = {"vehicleId": $("#vid").val(), "type": "0"};
                json_ajax_p("POST", url, "json", false, obj, realTimeProofreading.getDCallBack);
            }
        },
        getDCallBack: function (data) {
            cmsgSN = parseInt(data.obj.msgSN);
            var params = [{"vehicleID": $("#vid").val()}];
            var requestStrS = {
                "desc": {
                    "MsgId": 40964,
                    "UserName": $("#userName").text(),
                    "cmsgSN": cmsgSN
                },
                "data": params
            };
            if (data.obj.msgSN != null && data.obj.msgSN !== "") {
                webSocket.subscribe(headers, "/user/topic/realLocationP", realTimeProofreading.setDeviceData, "/app/vehicle/realLocationP", requestStrS);
            } else {
                $("#btn-correction").attr("disabled", "disabled");
                $("#btn-issued").attr("disabled", "disabled");
                layer.msg("获取设备数据失败")
            }
        },
        closeM: function () {
            var sendUrl = "/clbs/m/basicinfo/equipment/simcard/sendSimP";
            var data = {
                "simId": $("#sid").val(),
                "vehicleId": $("#vid").val(),
                "parameterName": $("#bindId").val(),
                "type": 3
            };
            json_ajax("POST", sendUrl, "json", false, data, realTimeProofreading.closeACK);
        },
        closeACK: function () {

        },
        setDeviceData: function (data) {
            var msgObj = JSON.parse(data.body);
            if (cmsgSN === msgObj.data.msgBody.msgSNAck) {
                var simData = msgObj.data.msgBody.gpsInfo.simCrad;
                var simNumber = msgObj.data.msgHead.mobile;
                var deviceNumber = msgObj.desc.deviceNo;
                var vtime = msgObj.data.msgBody.gpsInfo.time;
                if (simData !== undefined && simData.length !== 0) {
                    $("#terminalSim").val(simNumber);
                    $("#terminalIccid").val(simData[0].iccid);
                    $("#terminalNum").val(deviceNumber);
                    $("#terminalImei").val(simData[0].imei);
                    $("#terminalImsi").val(simData[0].imsi);
                    $("#terminalDay").val(simData[0].dayRealValue);
                    $("#terminalMonth").val(simData[0].monthRealValue);
                    $("#terminalCorrection").val(simData[0].correctionCoefficient);
                    $("#terminalWarning").val(simData[0].forewarningCoefficient);
                    $("#terminalHourFlow").val(simData[0].hourThresholdValue);
                    $("#terminalDayFlow").val(simData[0].dayThresholdValue);
                    $("#terminalMonthFlow").val(simData[0].monthThresholdValue);
                    $("#terminalmonthTrafficDeadline").val("20" + vtime.toString().substring(0, 2) + "-" + vtime.toString().substring(2, 4) + "-" +
                        vtime.toString().substring(4, 6) + " " + vtime.toString().substring(6, 8) + ":" + vtime.toString().substring(8, 10) + ":" + vtime.toString().substring(10, 12));
                    var simUrl = 'http://new.lbsdream.com/sim/simInfo';
                    getJsonForCross('get', simUrl, {
                        "key": "zwkj",
                        "iccid": simData[0].iccid
                    }, 'jsonp', false, "callbackparamzwkj", "callbackparamzwkj", realTimeProofreading.simCallBack);
                    realTimeProofreading.refreshSimInfo();
                    //车载终端数据与物联网卡平台对比
                    if (terminalIccid !== FThreeIccid) {
                        $("#FThreeIccid").css("background-color", "#ffe6e6");
                    }
                    if (terminalImei !== FThreeImei) {
                        $("#FThreeImei").css("background-color", "#ffe6e6");
                    }
                    if (terminalImsi !== FThreeImsi) {
                        $("#FThreeImsi").css("background-color", "#ffe6e6");
                    }
                    //车载终端数据与F3平台数据对比
                    if (terminalNum !== FThreeNum) {
                        $("#terminalNum").css("background-color", "#ffe6e6");
                    }
                    if (terminalCorrection !== FThreeCorrection) {
                        $("#terminalCorrection").css("background-color", "#ffe6e6");
                    }
                    if (terminalWarning !== FThreeWarning) {
                        $("#terminalWarning").css("background-color", "#ffe6e6");
                    }
                    if (terminalHourFlow !== FThreeHourFlow) {
                        $("#terminalHourFlow").css("background-color", "#ffe6e6");
                    }
                    if (terminalDayFlow !== FThreeDayFlow) {
                        $("#terminalDayFlow").css("background-color", "#ffe6e6");
                    }
                    if (terminalMonthFlow !== FThreeMonthFlow) {
                        $("#terminalMonthFlow").css("background-color", "#ffe6e6");
                    }
                    //验证车载终端数据
                    $("#btn-correction").attr("disabled", false);
                    $("#btn-issued").attr("disabled", "disabled");
                } else {
                    flag = false;
                    $("#btn-correction").attr("disabled", "disabled");
                    $("#btn-issued").attr("disabled", "disabled");
                    layer.closeAll('loading');
                    layer.msg("获取设备数据失败")
                }
            }
        },
        // 将字符串日期"2018-03-05 16:12:30" 转换为日期格式 2018/03/05
        timeStampString: function (data) {
            var time = data.toString();
            var array = time.split(" "); // 根据空格将字符串日期分割为数据
            array.pop();//删除数组最后一个元素
            time = array.join(' ');
            var startTimeIndex = time.replace("-", "/").replace("-", "/");
            var val = Date.parse(startTimeIndex);
            return new Date(val);
        },
        formatSimCardNumber: function (number) {
            if (number.length === 13) {
                return number.substr(0, 1) + number.substr(3);
            }
            return number;
        }
    };
    $(function () {
        realTimeProofreading.init();
        //修正
        $("#btn-correction").bind("click", realTimeProofreading.btnCorrection);
        //下发
        $("#btn-issued").bind("click", realTimeProofreading.btnIssued);
        //刷新
        $("#btn-refresh").bind("click", realTimeProofreading.btnRefresh);
        $("#close").bind("click", realTimeProofreading.closeM);
        $("#xclose").bind("click", realTimeProofreading.closeM);
    })
}(window, $))