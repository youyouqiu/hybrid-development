(function (window, $) {
    var min_calibrationSets = 2; //标定组数最小值
    var max_calibrationSets = 50; // 标定组数最大值
    var default_addOilTimeThreshold = 40; // 加油时间阈值默认值
    var default_addOilAmountThreshold = 8; // 加油量阈值默认值
    var default_seepOilTimeThreshold = 35; // 漏油时间阈值默认值
    var default_seepOilAmountThreshold = 6; // 漏油量阈值默认值
    var default_outputCorrectionCoefficientK = 100; // 输出修正系数K默认值
    var default_outputCorrectionCoefficientB = 100; // 输出修正系数B默认值
    var default_calibrationSets = 20; // 标定组数默认值
    var message = "传感器长度过长！应该小于油箱高度减去壁厚！";
    // 车辆list
    var vehicleList = JSON.parse($("#vehicleList").attr("value"));

    var type = $("#oilBoxType").val();

    // 油箱list
    var fuelTankList = JSON.parse($("#fuelTankList").attr("value"));
    // 油杆传感器list
    var rodSensorList = JSON.parse($("#rodSensorList").attr("value"));
    var selectOilBox = $("#selectOilBox").val(); // 选中值
    var selectOilBox2 = $("#selectOilBox2").val(); // 选中值
    var sensorType = $("#selectSensorType").val(); // 选中值
    var sensorType2 = $("#selectSensorType2").val(); // 选中值
    // 初始化车辆数据
    var dataList = {value: []};
    //初始化油箱参数
    var fuelDataList = {value: []};
    var sensorList = {value: []};

    editOilVehicleSetting = {
        init: function () {

			$(".modal-body").addClass("modal-body-overflow");
			$(".modal-body").css({"height":"auto","max-height":($(window).height()-194) +"px"});
			
            // 初始化油箱类型
            if (fuelTankList != null && fuelTankList.length > 0) {
                for (var i = 0; i < fuelTankList.length; i++) {
                    var fuel = {};
                    fuel.id = fuelTankList[i].id;
                    fuel.name = fuelTankList[i].type;
                    fuelDataList.value.push(fuel);
                }
                $("#shape1Hidden").val(fuelTankList[0].shape);
            }
            editOilVehicleSetting.initOilBox(fuelDataList);
            editOilVehicleSetting.initOilBox2(fuelDataList);
            editOilVehicleSetting.initCalibrationSets(); // 给标定组数默认值20组
            editOilVehicleSetting.initPersonalParam(); // 初始化个性参数

            if (rodSensorList != null && rodSensorList.length > 0) {
                for (var i = 0; i < rodSensorList.length; i++) {
                    var rodSensor = {};
                    rodSensor.id = rodSensorList[i].id;
                    rodSensor.name = rodSensorList[i].sensorNumber;
                    sensorList.value.push(rodSensor);
                }

            }
            editOilVehicleSetting.initSensor(sensorList);
            editOilVehicleSetting.initSensor2(sensorList);
            //主油箱
            $("#chooseTemplate").change(function () {
                if (this.value != "" && this.value == "1") { // AD值标定模板
                    window.open("/clbs/file/vas/01.油箱标定导入表-AD值标定法.xlsx");
                } else if (this.value != "" && this.value == "2") { // 标尺标定模板
                    window.open("/clbs/file/vas/02.油箱标定导入表-标尺标定法.xlsx");
                }
            });
            $("#chooseTemplate2").change(function () {
                if (this.value != "" && this.value == "1") { // AD值标定模板
                    window.open("/clbs/file/vas/01.油箱标定导入表-AD值标定法.xlsx");
                } else if (this.value != "" && this.value == "2") { // 标尺标定模板
                    window.open("/clbs/file/vas/02.油箱标定导入表-标尺标定法.xlsx");
                }
            });
            $("#calibrationSets").blur(function () {
                editOilVehicleSetting.initCalculateBtn(); // 默认计算按钮是否可点击
            });
            $("#calibrationSets2").blur(function () {
                editOilVehicleSetting.initCalculateBtn2(); // 默认计算按钮是否可点击
            });
            $("#outputCorrectionCoefficientK").blur(function () {
                editOilVehicleSetting.checkOutputCorrectionCoefficientKModify(this); // 判断当前修正系数是否被修改
            });
            $("#outputCorrectionCoefficientK2").blur(function () {
                editOilVehicleSetting.checkOutputCorrectionCoefficientKModify(this); // 判断当前修正系数是否被修改
            });
            $("#outputCorrectionCoefficientB").blur(function () {
                editOilVehicleSetting.checkOutputCorrectionCoefficientBModify(this); // 判断当前修正系数是否被修改
            });
            $("#outputCorrectionCoefficientB2").blur(function () {
                editOilVehicleSetting.checkOutputCorrectionCoefficientBModify(this); // 判断当前修正系数是否被修改
            });
            if (vehicleList != null && vehicleList.length > 0) {
                var vehicleIds = $("#vehicleId").val();

                for (var i = 0; i < vehicleList.length; i++) {
                    var obj = {};
                    //处理参考车牌只存在一条数据时防止浏览器抛出错误信息
                    if (vehicleList[i] == undefined) {
                        dataList.value.push(obj);
                    } else {
                        obj.id = vehicleList[i].vehicleId;
                        obj.name = vehicleList[i].brand;
                        if (vehicleList[i].vehicleId == vehicleIds) {
                        } else {
                            dataList.value.push(obj);
                        }

                    }

                }
            }
            if (type == 2) {
                $("#TabFenceBox").removeClass("active");
                $("#home1").removeClass("active");
                $("#TabCarBox").addClass("active");
                $("#profile1").addClass("active");
            }
            $("#brands").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                idField: "id",
                keyField: "name",
                effectiveFields: ["name"],
                searchFields: ["id"],
                data: dataList
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                // 当选择参考车牌
                var vehicleId = keyword.id;
                if (vehicleId != null && vehicleId != undefined && vehicleId != "") {
                    // 发送请求
                    var url = "/clbs/v/oilmassmgt/oilvehiclesetting/getBindInfo";
                    var datas = {"vehicleId": vehicleId};
                    $.ajax({
                        type: 'POST',
                        url: url,
                        async: false,
                        dataType: 'json',
                        data: datas,
                        success: function (data) {
                            if (data.success) {
                                var vehicleInfo = data.obj;
                                if (vehicleId === vehicleInfo.vehicleId) {
                                    // 参考油箱id和传感器id
                                    $("#oilBoxId").val(vehicleInfo.type);
                                    $("#formOilBoxId").val(vehicleInfo.oilBoxId);
                                    $("#oilBoxId").attr('data-id', vehicleInfo.oilBoxId);
                                    $("#shape").val(vehicleInfo.shape);
                                    $("#boxLength").val(vehicleInfo.boxLength);
                                    $("#width").val(vehicleInfo.width);
                                    $("#height").val(vehicleInfo.height);
                                    $("#thickness").val(vehicleInfo.thickness);
                                    $("#buttomRadius").val(vehicleInfo.buttomRadius);
                                    $("#topRadius").val(vehicleInfo.topRadius);
                                    $("#theoryVolume").val(vehicleInfo.theoryVolume);
                                    $("#realVolume").val(vehicleInfo.realVolume);

                                    $("#sensorLength").val(vehicleInfo.sensorLength);
                                    $("#filteringFactor").val(vehicleInfo.filteringFactor);
                                    $("#baudRate").val(vehicleInfo.baudRate);
                                    $("#oddEvenCheck").val(vehicleInfo.oddEvenCheck);
                                    $("#compensationCanMake").val(vehicleInfo.compensationCanMake);
                                    $("#sensorType").val(vehicleInfo.sensorType);
                                    $("#sensorNumber").val(vehicleInfo.sensorNumber);
                                    $("#sensorNumber").attr('data-id', vehicleInfo.sensorType);
                                    if (vehicleInfo.calibrationSets !== null && vehicleInfo.calibrationSets !== "") {
                                        $("#calibrationSets").val(vehicleInfo.calibrationSets);
                                    }

                                    editOilVehicleSetting.initOilBox(fuelDataList);
                                    editOilVehicleSetting.initOilBox2(fuelDataList);
                                    editOilVehicleSetting.initSensor(sensorList);
                                    editOilVehicleSetting.initSensor2(sensorList);

                                    if (vehicleInfo.automaticUploadTime !== null && vehicleInfo.automaticUploadTime !== "") {
                                        $("#automaticUploadTime").val(vehicleInfo.automaticUploadTime);
                                    }
                                    if (vehicleInfo.outputCorrectionCoefficientK !== null && vehicleInfo.outputCorrectionCoefficientK !== "") {
                                        $("#outputCorrectionCoefficientK").val(vehicleInfo.outputCorrectionCoefficientK);
                                    }
                                    if (vehicleInfo.outputCorrectionCoefficientB !== null && vehicleInfo.outputCorrectionCoefficientB !== "") {
                                        $("#outputCorrectionCoefficientB").val(vehicleInfo.outputCorrectionCoefficientB);
                                    }
                                    if (vehicleInfo.addOilTimeThreshold !== null && vehicleInfo.addOilTimeThreshold !== "") {
                                        $("#addOilTimeThreshold").val(vehicleInfo.addOilTimeThreshold);
                                    }
                                    if (vehicleInfo.addOilAmountThreshol !== null && vehicleInfo.addOilAmountThreshol !== "") {
                                        $("#addOilAmountThreshol").val(vehicleInfo.addOilAmountThreshol);
                                    }
                                    if (vehicleInfo.seepOilTimeThreshold !== null && vehicleInfo.seepOilTimeThreshold !== "") {
                                        $("#seepOilTimeThreshold").val(vehicleInfo.seepOilTimeThreshold);
                                    }
                                    if (vehicleInfo.seepOilAmountThreshol !== null && vehicleInfo.seepOilAmountThreshol !== "") {
                                        $("#seepOilAmountThreshol").val(vehicleInfo.seepOilAmountThreshol);
                                    }

                                    $("#oilLevelHeights").val(vehicleInfo.oilLevelHeights);
                                    $("#oilValues").val(vehicleInfo.oilValues);
                                    $("#oilBoxId2").val(vehicleInfo.type2);
                                    $("#oilBoxId2").attr('data-id', vehicleInfo.oilBoxId2);
                                    $("#formOilBoxId2").val(vehicleInfo.oilBoxId2);
                                    $("#shape2").val(vehicleInfo.shape2);
                                    $("#boxLength2").val(vehicleInfo.boxLength2);
                                    $("#width2").val(vehicleInfo.width2);
                                    $("#height2").val(vehicleInfo.height2);
                                    $("#thickness2").val(vehicleInfo.thickness2);
                                    $("#buttomRadius2").val(vehicleInfo.buttomRadius2);
                                    $("#topRadius2").val(vehicleInfo.topRadius2);
                                    $("#theoryVolume2").val(vehicleInfo.theoryVolume2);
                                    $("#realVolume2").val(vehicleInfo.realVolume2);

                                    $("#sensorLength2").val(vehicleInfo.sensorLength2);
                                    $("#filteringFactor2").val(vehicleInfo.filteringFactor2);
                                    $("#baudRate2").val(vehicleInfo.baudRate2);
                                    $("#oddEvenCheck2").val(vehicleInfo.oddEvenCheck);
                                    $("#compensationCanMake2").val(vehicleInfo.compensationCanMake2);
                                    $("#sensorType2").val(vehicleInfo.sensorType2);
                                    $("#sensorNumber2").val(vehicleInfo.sensorNumber2);
                                    $("#sensorNumber2").attr('data-id', vehicleInfo.sensorType2);

                                    if (vehicleInfo.calibrationSets2 !== null && vehicleInfo.calibrationSets2 !== "") {
                                        $("#calibrationSets2").val(vehicleInfo.calibrationSets2);
                                    }
                                    if (vehicleInfo.automaticUploadTime2 !== null && vehicleInfo.automaticUploadTime2 !== "") {
                                        $("#automaticUploadTime2").val(vehicleInfo.automaticUploadTime2);
                                    }
                                    if (vehicleInfo.outputCorrectionCoefficientK2 !== null && vehicleInfo.outputCorrectionCoefficientK2 !== "") {
                                        $("#outputCorrectionCoefficientK2").val(vehicleInfo.outputCorrectionCoefficientK2);
                                    }
                                    if (vehicleInfo.outputCorrectionCoefficientB2 !== null && vehicleInfo.outputCorrectionCoefficientB2 !== "") {
                                        $("#outputCorrectionCoefficientB2").val(vehicleInfo.outputCorrectionCoefficientB2);
                                    }
                                    if (vehicleInfo.addOilTimeThreshold2 !== null && vehicleInfo.addOilTimeThreshold2 !== "") {
                                        $("#addOilTimeThreshold2").val(vehicleInfo.addOilTimeThreshold2);
                                    }
                                    if (vehicleInfo.addOilAmountThreshol2 !== null && vehicleInfo.addOilAmountThreshol2 !== "") {
                                        $("#addOilAmountThreshol2").val(vehicleInfo.addOilAmountThreshol2);
                                    }
                                    if (vehicleInfo.seepOilTimeThreshold2 !== null && vehicleInfo.seepOilTimeThreshold2 !== "") {
                                        $("#seepOilTimeThreshold2").val(vehicleInfo.seepOilTimeThreshold2);
                                    }
                                    if (vehicleInfo.seepOilAmountThreshol2 !== null && vehicleInfo.seepOilAmountThreshol2 !== "") {
                                        $("#seepOilAmountThreshol2").val(vehicleInfo.seepOilAmountThreshol2);
                                    }
                                    $("#oilLevelHeights2").val(vehicleInfo.oilLevelHeights2);
                                    $("#oilValues2").val(vehicleInfo.oilValues2);
                                }
                            }
                        },
                        error: function () {
                            layer.msg(systemError, {move: false});
                        }
                    });

                }
                editOilVehicleSetting.initCalculateBtn();
                editOilVehicleSetting.initCalculateBtn2();
            }).on('onUnsetSelectValue', function () {
            });
            // 解决IE浏览器下拉表默认展开问题
            setTimeout(function () {
                $('.dropdown-menu-right').hide();
            }, 500);
        },

        //初始化油箱判断是否隐藏字段
        initOilBoxShowHide: function () {
            var shape = $("#shape").val();
            if (shape == "1") {
                $("#cuboidForm").show();
            } else {
                $("#cuboidForm").hide();
            }
            shape = $("#shape2").val();
            if (shape == "1") {
                $("#cuboidForm2").show();
            } else {
                $("#cuboidForm2").hide();
            }
        },

        initOilBox: function (publicList) {//主油箱传感器油箱型号
            if (publicList.value.length == 0 || editOilVehicleSetting.compareLength()) {//油箱高度与油杆传感器长度判断，
                editOilVehicleSetting.clearOil();
            }
            $("#oilBoxId").bsSuggest("destroy"); // 销毁事件
            $("#oilBoxId").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                idField: "id",
                keyField: "name",
                effectiveFields: ["name"],
                searchFields: ["id"],
                data: publicList
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                editOilVehicleSetting.oilBoxIdBlur()
                var fuel1Id = keyword.id;
                for (var i = 0; i < fuelTankList.length; i++) {
                    if (fuel1Id == fuelTankList[i].id) {
                        $("#formOilBoxId").val(fuelTankList[i].id);
                        $("#shape").val(fuelTankList[i].shape);
                        $("#shape1Hidden").val(fuelTankList[i].shape);
                        $("#boxLength").val(fuelTankList[i].boxLength);
                        $("#width").val(fuelTankList[i].width);
                        $("#height").val(fuelTankList[i].height);
                        $("#thickness").val(fuelTankList[i].thickness);
                        $("#theoryVolume").val(fuelTankList[i].theoryVolume);
                        $("#realVolume").val(fuelTankList[i].realVolume);
                        $("#buttomRadius").val(fuelTankList[i].buttomRadius);
                        $("#topRadius").val(fuelTankList[i].topRadius);

                        var list = {value: []};
                        for (var j = 0; j < rodSensorList.length; j++) {
                            var height = fuelTankList[i].height;
                            var sensorSize = rodSensorList[j].sensorLength;
                            if (parseInt(height - 100) <= parseInt(sensorSize) && parseInt(sensorSize) < parseInt(height)) {
                                var rodSensor = {};
                                rodSensor.id = rodSensorList[j].id;
                                rodSensor.name = rodSensorList[j].sensorNumber;
                                list.value.push(rodSensor);
                            }

                        }
                        editOilVehicleSetting.initSensor(list);
                    }
                }
                var shape = $("#shape").val();
                if (shape == "1") {
                    $("#cuboidForm").show();
                } else {
                    $("#cuboidForm").hide();
                }
                $("#error_label_add").hide();
                editOilVehicleSetting.initCalculateBtn(); // 默认计算按钮是否可点击（主油箱）
            }).on('onUnsetSelectValue', function () {
                $("#formOilBoxId").val("");
            }).on('onClearSelectValue', function () {
                editOilVehicleSetting.oilBoxIdBlur()
            })
        },

        initOilBox2: function (publicList) {//副油箱传感器型号
            if (publicList.value.length == 0 || editOilVehicleSetting.compareLength2()) {//油箱高度与油杆传感器长度判断，
                editOilVehicleSetting.clearOil2();
            }
            $("#oilBoxId2").bsSuggest("destroy"); // 销毁事件
            setTimeout(function () {
                $("#oilBoxId2").bsSuggest({
                    indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                    indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                    idField: "id",
                    keyField: "name",
                    effectiveFields: ["name"],
                    searchFields: ["id"],
                    data: publicList
                }).on('onDataRequestSuccess', function (e, result) {
                }).on('onSetSelectValue', function (e, keyword, data) {
                    editOilVehicleSetting.oilBoxIdBlur2()
                    var fuel1Id = keyword.id;
                    for (var i = 0; i < fuelTankList.length; i++) {
                        if (fuel1Id == fuelTankList[i].id) {
                            $("#formOilBoxId2").val(fuelTankList[i].id);
                            $("#shape2").val(fuelTankList[i].shape);
                            $("#shape2Hidden").val(fuelTankList[i].shape);
                            $("#boxLength2").val(fuelTankList[i].boxLength);
                            $("#width2").val(fuelTankList[i].width);
                            $("#height2").val(fuelTankList[i].height);
                            $("#thickness2").val(fuelTankList[i].thickness);
                            $("#theoryVolume2").val(fuelTankList[i].theoryVolume);
                            $("#realVolume2").val(fuelTankList[i].realVolume);
                            $("#buttomRadius2").val(fuelTankList[i].buttomRadius);
                            $("#topRadius2").val(fuelTankList[i].topRadius);

                            var list = {value: []};
                            for (var j = 0; j < rodSensorList.length; j++) {
                                var height = fuelTankList[i].height;
                                var sensorSize = rodSensorList[j].sensorLength;
                                if (parseInt(height - 100) <= parseInt(sensorSize) && parseInt(sensorSize) < parseInt(height)) {
                                    var rodSensor = {};
                                    rodSensor.id = rodSensorList[j].id;
                                    rodSensor.name = rodSensorList[j].sensorNumber;
                                    list.value.push(rodSensor);
                                }

                            }
                            editOilVehicleSetting.initSensor2(list);
                        }
                    }
                    var shape = $("#shape2").val();
                    if (shape == "1") {
                        $("#cuboidForm2").show();
                    } else {
                        $("#cuboidForm2").hide();
                    }
                    $("#error_label_add").hide();
                    editOilVehicleSetting.initCalculateBtn2(); // 默认计算按钮是否可点击（副油箱）
                }).on('onUnsetSelectValue', function () {
                    $("#formOilBoxId2").val("");
                }).on('onClearSelectValue', function () {
                    editOilVehicleSetting.oilBoxIdBlur2()
                })
            }, 1000)
        },
        initSensor: function (publicList) {//主油箱传感器下拉选
            if (publicList.value.length == 0 || editOilVehicleSetting.compareLength()) {//油箱高度与油杆传感器长度判断，
                editOilVehicleSetting.clearSensor();
            }
            $("#sensorNumber").bsSuggest("destroy"); // 销毁事件
            $("#sensorNumber").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                idField: "id",
                keyField: "name",
                effectiveFields: ["name"],
                searchFields: ["id"],
                data: publicList
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                editOilVehicleSetting.sensorBlur()
                var sensorId = keyword.id;
                for (var i = 0; i < rodSensorList.length; i++) {
                    if (sensorId == rodSensorList[i].id) {
                        $("#sensorType").val(sensorId);
                        $("#sensorLength").val(rodSensorList[i].sensorLength);
                        $("#filteringFactor").val(rodSensorList[i].filteringFactor);
                        $("#baudRate").val(rodSensorList[i].baudRate);
                        $("#oddEvenCheck").val(rodSensorList[i].oddEvenCheck);
                        $("#compensationCanMake").val(rodSensorList[i].compensationCanMake);
                        var list = {value: []};
                        for (var j = 0; j < fuelTankList.length; j++) {
                            var height = fuelTankList[j].height;
                            var sensorSize = rodSensorList[i].sensorLength;
                            if (parseInt(height - 100) <= parseInt(sensorSize) && parseInt(sensorSize) < parseInt(height)) {
                                var fuel = {};
                                fuel.id = fuelTankList[j].id;
                                fuel.name = fuelTankList[j].type;
                                list.value.push(fuel);
                            }
                        }
                        editOilVehicleSetting.initOilBox(list);
                    }
                }
                $("#error_label_add").hide();
                editOilVehicleSetting.initCalculateBtn(); // 默认计算按钮是否可点击（主油箱）
            }).on('onUnsetSelectValue', function () {
                $("#sensorType").val("");
            }).on('onClearSelectValue', function () {
                editOilVehicleSetting.sensorBlur()
            })
        },
        initSensor2: function (publicList) {//副油箱传感器下拉选
            if (publicList.value.length == 0 || editOilVehicleSetting.compareLength2()) {
                editOilVehicleSetting.clearSensor2();
            }
            $("#sensorNumber2").bsSuggest("destroy"); // 销毁事件
            $("#sensorNumber2").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                idField: "id",
                keyField: "name",
                effectiveFields: ["name"],
                searchFields: ["id"],
                data: publicList
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                editOilVehicleSetting.sensorBlur2()
                var sensorId2 = keyword.id;
                for (var i = 0; i < rodSensorList.length; i++) {
                    if (sensorId2 == rodSensorList[i].id) {
                        $("#sensorType2").val(sensorId2);
                        $("#sensorLength2").val(rodSensorList[i].sensorLength);
                        $("#filteringFactor2").val(rodSensorList[i].filteringFactor);
                        $("#baudRate2").val(rodSensorList[i].baudRate);
                        $("#oddEvenCheck2").val(rodSensorList[i].oddEvenCheck);
                        $("#compensationCanMake2").val(rodSensorList[i].compensationCanMake);
                        var list = {value: []};
                        for (var j = 0; j < fuelTankList.length; j++) {
                            var height = fuelTankList[j].height;
                            var sensorSize = rodSensorList[i].sensorLength;
                            if (parseInt(height - 100) <= parseInt(sensorSize) && parseInt(sensorSize) < parseInt(height)) {
                                var fuel = {};
                                fuel.id = fuelTankList[j].id;
                                fuel.name = fuelTankList[j].type;
                                list.value.push(fuel);
                            }
                        }
                        editOilVehicleSetting.initOilBox2(list);
                    }
                }
                $("#error_label_add").hide();
                editOilVehicleSetting.initCalculateBtn2(); // 默认计算按钮是否可点击（副油箱）
            }).on('onUnsetSelectValue', function () {
                $("#sensorType2").val("");

            }).on('onClearSelectValue', function () {
                editOilVehicleSetting.sensorBlur2()
            })
        },
        oilBoxIdBlur: function () {//油箱型号失去焦点后、重新加载下拉选
            var oilValue = $("#oilBoxId").val();
            var sensorValue = $("#sensorNumber").val();
            if (oilValue == "" && sensorValue == "") {//当油箱型号与传感器型号都为空时重新加载
                editOilVehicleSetting.initOilBox(fuelDataList);
                editOilVehicleSetting.initSensor(sensorList);
            }
            if (oilValue == "" && sensorValue != "") {
                var sensorLength = $("#sensorLength").val();
                var list = {value: []};
                for (var j = 0; j < fuelTankList.length; j++) {
                    var height = fuelTankList[j].height;
                    if (parseInt(height - 100) <= parseInt(sensorLength) && parseInt(sensorLength) < parseInt(height)) {
                        var fuel = {};
                        fuel.id = fuelTankList[j].id;
                        fuel.name = fuelTankList[j].type;
                        list.value.push(fuel);
                    }
                }
                editOilVehicleSetting.initOilBox(list);
            }
            $("#oilchance").hide();
            $("#sensorchance").hide();
        },
        oilBoxIdBlur2: function () {//油箱型号失去焦点后、重新加载下拉选
            var oilValue2 = $("#oilBoxId2").val();
            var sensorValue2 = $("#sensorNumber2").val();
            if (oilValue2 == "" && sensorValue2 == "") {//当油箱型号与传感器型号都为空时重新加载
                editOilVehicleSetting.initOilBox2(fuelDataList);
                editOilVehicleSetting.initSensor2(sensorList);
            }
            if (oilValue2 == "" && sensorValue2 != "") {
                var sensorLength2 = $("#sensorLength2").val();
                var list = {value: []};
                for (var j = 0; j < fuelTankList.length; j++) {
                    var height = fuelTankList[j].height;
                    if (parseInt(height - 100) <= parseInt(sensorLength2) && parseInt(sensorLength2) < parseInt(height)) {
                        var fuel = {};
                        fuel.id = fuelTankList[j].id;
                        fuel.name = fuelTankList[j].type;
                        list.value.push(fuel);
                    }
                }
                editOilVehicleSetting.initOilBox2(list);
            }
            $("#oilchance2").hide();
            $("#sensorchance2").hide();
        },
        sensorBlur: function () {//传感器型号失去焦点后、重新加载下拉选
            var oilValue = $("#oilBoxId").val();
            var sensorValue = $("#sensorNumber").val();
            if (oilValue == "" && sensorValue == "") {//当油箱型号与传感器型号都为空时重新加载
                editOilVehicleSetting.initOilBox(fuelDataList);
                editOilVehicleSetting.initSensor(sensorList);
            }
            var height = $("#height").val();
            if (oilValue != "" && sensorValue == "") {
                var list = {value: []};
                for (var j = 0; j < rodSensorList.length; j++) {
                    var sensorSize = rodSensorList[j].sensorLength;
                    if (parseInt(height - 100) <= parseInt(sensorSize) && parseInt(sensorSize) < parseInt(height)) {
                        var rodSensor = {};
                        rodSensor.id = rodSensorList[j].id;
                        rodSensor.name = rodSensorList[j].sensorNumber;
                        list.value.push(rodSensor);
                    }
                }
                editOilVehicleSetting.initSensor(list);
            }
            $("#oilchance").hide();
            $("#sensorchance").hide();
        },
        sensorBlur2: function () {//传感器型号失去焦点后、重新加载下拉选
            var oilValue2 = $("#oilBoxId2").val();
            var sensorValue2 = $("#sensorNumber2").val();
            if (oilValue2 == "" && sensorValue2 == "") {//当油箱型号与传感器型号都为空时重新加载
                editOilVehicleSetting.initOilBox2(fuelDataList);
                editOilVehicleSetting.initSensor2(sensorList);
            }
            var height2 = $("#height2").val();
            if (oilValue2 != "" && sensorValue2 == "") {
                var list = {value: []};
                for (var j = 0; j < rodSensorList.length; j++) {
                    var sensorSize = rodSensorList[j].sensorLength;
                    if (parseInt(height2 - 100) <= parseInt(sensorSize) && parseInt(sensorSize) < parseInt(height2)) {
                        var rodSensor = {};
                        rodSensor.id = rodSensorList[j].id;
                        rodSensor.name = rodSensorList[j].sensorNumber;
                        list.value.push(rodSensor);
                    }
                }
                editOilVehicleSetting.initSensor2(list);
            }
            $("#oilchance2").hide();
            $("#sensorchance2").hide();
        },
        compareLength: function () {//油箱型号与传感器型号比较
            var height = $("#height").val();
            var sensorLength = $("#sensorLength").val();
            if (parseInt(height - 100) <= parseInt(sensorLength) && parseInt(sensorLength) < parseInt(height)) {
                return false;
            } else {
                return true;
            }
        },
        compareLength2: function () {//油箱型号与传感器型号比较
            var height2 = $("#height2").val();
            var sensorLength2 = $("#sensorLength2").val();
            if (parseInt(height2 - 100) <= parseInt(sensorLength2) && parseInt(sensorLength2) < parseInt(height2)) {
                return false;
            } else {
                return true;
            }
        },
        clearSensor: function () {// 清空传感器型号信息
            $("#sensorLength").val("");
            $("#filteringFactor").val("");
            $("#baudRate").val("");
            $("#oddEvenCheck").val("");
            $("#compensationCanMake").val("");
            $("#sensorType").val("");
            $("#sensorNumber").val("");
            $("#sensorNumber").attr("data-id", "");
        },
        clearSensor2: function () {// 清空传感器型号信息
            $("#sensorLength2").val("");
            $("#filteringFactor2").val("");
            $("#baudRate2").val("");
            $("#oddEvenCheck2").val("");
            $("#compensationCanMake2").val("");
            $("#sensorType2").val("");
            $("#sensorNumber2").val("");
            $("#sensorNumber2").attr("data-id", "");
        },
        clearOil: function () {//清空油箱信息
            $("#formOilBoxId").val("");
            $("#shape").val("");
            $("#shape1Hidden").val("");
            $("#boxLength").val("");
            $("#width").val("");
            $("#height").val("");
            $("#thickness").val("");
            $("#buttomRadius").val("");
            $("#topRadius").val("");
            $("#theoryVolume").val("");
            $("#realVolume").val("");
            $("#oilBoxId").val("");
            $("#oilBoxId").attr("data-id", "");
        },
        clearOil2: function () {//清油箱信息
            $("#formOilBoxId2").val("");
            $("#shape2").val("");
            $("#shape1Hidden2").val("");
            $("#boxLength2").val("");
            $("#width2").val("");
            $("#height2").val("");
            $("#thickness2").val("");
            $("#buttomRadius2").val("");
            $("#topRadius2").val("");
            $("#theoryVolume2").val("");
            $("#realVolume2").val("");
            $("#oilBoxId2").val("");
            $("#oilBoxId2").attr("data-id", "");
        },
        clearAll: function () {
            editOilVehicleSetting.clearSensor();
            editOilVehicleSetting.clearSensor2();
            editOilVehicleSetting.clearOil();
            editOilVehicleSetting.clearOil2();
            $("#tankBasisInfo-content").hide();
            $("#sensorBasisInfo-content").hide();
            $("#tankBasisInfo font").html("显示更多");
            $("#tankBasisInfo font").html("显示更多");
        },
        // 判断修正系数K是否被修改
        checkOutputCorrectionCoefficientKModify: function (ele) {
            if (ele.value != '' && !isNaN(ele.value) && parseFloat(ele.value) != default_outputCorrectionCoefficientK) {
                layer.confirm("修正系数K值一经修改可能导致传感器工作异常，是否确认修改？",
                    {btn: ['确定', '取消']}, function () {
                        layer.closeAll();
                    }, function () {
                        ele.value = default_outputCorrectionCoefficientK;
                    });
            }
        },
        // 判断修正系数B是否被修改
        checkOutputCorrectionCoefficientBModify: function (ele) {
            if (ele.value != '' && !isNaN(ele.value) && parseFloat(ele.value) != default_outputCorrectionCoefficientB) {
                layer.confirm("修正系数B值一经修改可能导致传感器工作异常，是否确认修改？",
                    {btn: ['确定', '取消']},
                    function () {
                        layer.closeAll();
                    }, function () {
                        ele.value = default_outputCorrectionCoefficientB;
                    });
            }
        },
        //默认计算按钮是否可点击（主油箱）
        initCalculateBtn: function () {
            var l = $("#boxLength").val();
            var w = $("#width").val();
            var h = $("#height").val();
            var t = $("#thickness").val();
            var r1 = $("#buttomRadius").val();
            var r2 = $("#topRadius").val();
            var s = $("#shape").find("option:selected").val();
            var sensorLength = $("#sensorLength").val();
            var calibrationSets = $("#calibrationSets").val();
            if (l != "" && w != "" && h != "" && t != "" && s != "" && sensorLength != "" && calibrationSets != ""
                && (s != "1" || (r1 != "" && r2 != ""))) {
                $("#calculateBtn").attr("disabled", false);
            } else {
                $("#calculateBtn").attr("disabled", true);
            }
        },
        //默认计算按钮是否可点击（副油箱）
        initCalculateBtn2: function () {
            var l = $("#boxLength2").val();
            var w = $("#width2").val();
            var h = $("#height2").val();
            var t = $("#thickness2").val();
            var r1 = $("#buttomRadius2").val();
            var r2 = $("#topRadius2").val();
            var s = $("#shape2").find("option:selected").val();
            var sensorLength = $("#sensorLength2").val();
            var calibrationSets = $("#calibrationSets2").val();
            if (l != "" && w != "" && h != "" && t != "" && s != "" && sensorLength != "" && calibrationSets != ""
                && (s != "1" || (r1 != "" && r2 != ""))) {
                $("#calculateBtn2").attr("disabled", false);
            } else {
                $("#calculateBtn2").attr("disabled", true);
            }
        },
        // 切换主油箱和副油箱的时候，给curBox赋值
        setValue: function (value) {
            $("#curBox").val(value);
        },
        initCalibrationSets: function () {
            if ($("#calibrationSets").val() == "") {
                $("#calibrationSets").val(default_calibrationSets);
            }
            if ($("#calibrationSets2").val() == "") {
                $("#calibrationSets2").val(default_calibrationSets);
            }
        },
        // 初始化个性参数
        initPersonalParam: function () {
            if ($("#outputCorrectionCoefficientK").val() == "") {
                $("#outputCorrectionCoefficientK").val(default_outputCorrectionCoefficientK);
            }
            if ($("#outputCorrectionCoefficientK2").val() == "") {
                $("#outputCorrectionCoefficientK2").val(default_outputCorrectionCoefficientK);
            }
            if ($("#outputCorrectionCoefficientB").val() == "") {
                $("#outputCorrectionCoefficientB").val(default_outputCorrectionCoefficientK);
            }
            if ($("#outputCorrectionCoefficientB2").val() == "") {
                $("#outputCorrectionCoefficientB2").val(default_outputCorrectionCoefficientK);
            }
            if ($("#addOilTimeThreshold").val() == "") {
                $("#addOilTimeThreshold").val(default_addOilTimeThreshold);
            }
            if ($("#addOilTimeThreshold2").val() == "") {
                $("#addOilTimeThreshold2").val(default_addOilTimeThreshold);
            }
            if ($("#addOilAmountThreshol").val() == "") {
                $("#addOilAmountThreshol").val(default_addOilAmountThreshold);
            }
            if ($("#addOilAmountThreshol2").val() == "") {
                $("#addOilAmountThreshol2").val(default_addOilAmountThreshold);
            }
            if ($("#seepOilTimeThreshold").val() == "") {
                $("#seepOilTimeThreshold").val(default_seepOilTimeThreshold);
            }
            if ($("#seepOilTimeThreshold2").val() == "") {
                $("#seepOilTimeThreshold2").val(default_seepOilTimeThreshold);
            }
            if ($("#seepOilAmountThreshol").val() == "") {
                $("#seepOilAmountThreshol").val(default_seepOilAmountThreshold);
            }
            if ($("#seepOilAmountThreshol2").val() == "") {
                $("#seepOilAmountThreshol2").val(default_seepOilAmountThreshold);
            }
        },
        // 计算(主油箱)
        calculate1: function () {
            var oilBoxId = $("#oilBoxId").attr('data-id');

            var boxLength = $("#boxLength").val();
            var width = $("#width").val();
            var height = $("#height").val();
            var thickness = $("#thickness").val();
            var shape = $("#shape").val();
            var sensorLength = $("#sensorLength").val();
            var calibrationSets = $("#calibrationSets").val();
            var realVolume = $("#realVolume").val();
            var theoryVolume = $("#theoryVolume").val();
            var id = $("#id").val();
            var boxNum = "1";
            var r1 = "";
            var r2 = "";
            if (shape == "1") {
                r1 = $("#buttomRadius").val();
                r2 = $("#topRadius").val();
            }
            if (parseInt(calibrationSets) > max_calibrationSets || parseInt(calibrationSets) < min_calibrationSets) {
                layer.msg("标定组数介于" + min_calibrationSets + "-" + max_calibrationSets + "之间");
                return;
            } else if (oilBoxId == "") {
                layer.msg("请选择油箱型号");
                return;
            } else if (sensorLength == "") {
                layer.msg("请选择传感器型号");
                return;
            }
            layer.confirm("当前已有标定数据，是否需要重新计算？", {btn: ["是的", "不用"], icon: 3, title: "操作确认"}, function () {
                $.ajax({
                    type: 'POST',
                    url: '/clbs/v/oilmassmgt/fueltankmgt/calCalibration',
                    async: false,
                    dataType: 'json',
                    data: {
                        "boxLength": boxLength,
                        "width": width,
                        "height": height,
                        "thickness": thickness,
                        "shape": shape,
                        "sensorLength": sensorLength,
                        "calibrationSets": calibrationSets,
                        "realVolume": realVolume,
                        "theoryVolume": theoryVolume,
                        "id": id,
                        "oilBoxType": boxNum,
                        "buttomRadius": r1,
                        "topRadius": r2
                    },
                    success: function (data) {
                        if (data.success) {
                            layer.msg("计算成功");
                        }
                        if (data != null && data.obj != null && data.obj.result != null) {
                            var oilLevelHeights = "";
                            var oilValues = "";
                            var boxId = data.obj.result[0].oilBoxId;
                            for (var i = 0; i < data.obj.result.length; i++) {
                                oilLevelHeights += data.obj.result[i].oilLevelHeight + ",";
                                oilValues += data.obj.result[i].oilValue + ",";
                            }
                            if (oilLevelHeights.length > 0) {
                                oilLevelHeights = oilLevelHeights.substr(0, oilLevelHeights.length - 1);
                            }
                            if (oilValues.length > 0) {
                                oilValues = oilValues.substr(0, oilValues.length - 1);
                            }
                            $("#oilLevelHeights").val(oilLevelHeights);
                            $("#oilValues").val(oilValues);
                            $("#boxId").val(boxId);
                        }
                    },
                    error: function () {
                        layer.msg(systemError, {move: false});
                    }
                });
            });

        },
        // 计算(副油箱)
        calculate2: function () {
            var oilBoxId2 = $("#oilBoxId2").attr('data-id');
            var boxLength = $("#boxLength2").val();
            var width = $("#width2").val();
            var height = $("#height2").val();
            var thickness = $("#thickness2").val();
            var shape = $("#shape2").val();
            var sensorLength = $("#sensorLength2").val();
            var calibrationSets = $("#calibrationSets2").val();
            var realVolume = $("#realVolume2").val();
            var theoryVolume = $("#theoryVolume2").val();
            var id = $("#id2").val();
            var newId2 = $("#newId2").val();
            var boxNum = "2";
            var r1 = "";
            var r2 = "";
            if (shape == "1") {
                r1 = $("#buttomRadius2").val();
                r2 = $("#topRadius2").val();
            }
            if (id == null || id == "" || id == "undefined") {
                id = newId2;
            }
            if (parseInt(calibrationSets) > max_calibrationSets || parseInt(calibrationSets) < min_calibrationSets) {
                layer.msg("标定组数介于" + min_calibrationSets + "-" + max_calibrationSets + "之间");
                return;
            } else if (oilBoxId2 == "") {
                layer.msg("请选择油箱型号");
                return;
            } else if (sensorLength == "") {
                layer.msg("请选择传感器型号");
                return;
            }
            $.ajax({
                type: 'POST',
                url: '/clbs/v/oilmassmgt/fueltankmgt/calCalibration',
                async: false,
                dataType: 'json',
                data: {
                    "boxLength": boxLength, "width": width, "height": height, "thickness": thickness, "shape": shape,
                    "sensorLength": sensorLength, "calibrationSets": calibrationSets,
                    "realVolume": realVolume, "theoryVolume": theoryVolume, "id": id, "oilBoxType": boxNum,
                    "buttomRadius": r1, "topRadius": r2
                },
                success: function (data) {
                    if (data.success) {
                        layer.msg("计算成功");
                    }
                    if (data != null && data.obj != null && data.obj.result != null) {
                        var oilLevelHeights = "";
                        var oilValues = "";
                        var boxId = data.obj.result[0].oilBoxId;
                        for (var i = 0; i < data.obj.result.length; i++) {
                            oilLevelHeights += data.obj.result[i].oilLevelHeight + ",";
                            oilValues += data.obj.result[i].oilValue + ",";
                        }
                        if (oilLevelHeights.length > 0) {
                            oilLevelHeights = oilLevelHeights.substr(0, oilLevelHeights.length - 1);
                        }
                        if (oilValues.length > 0) {
                            oilValues = oilValues.substr(0, oilValues.length - 1);
                        }
                        $("#oilLevelHeights2").val(oilLevelHeights);
                        $("#oilValues2").val(oilValues);
                        $("#boxId2").val(boxId);
                    }
                },
                error: function () {
                    layer.msg(systemError, {move: false});
                }
            });
        },
        editCheckSelect: function () {
            var checkVFlag = true;
            var dataIdVal2 = $("#oilBoxId2").val();
            var sensorNumberVal2 = $("#sensorNumber2").val();
            //选择框为空清楚对应隐藏框的值
            if (!dataIdVal2) {
                $("#formOilBoxId2").val('');
            }
            if (!sensorNumberVal2) {
                $("#sensorType2").val('');
            }
            //清空之后再去取隐藏框的值
            var hiddenOilBoxId2 = $("#formOilBoxId2").val();
            var hiddenSensorType2 = $("#sensorType2").val();
            //两个选择框都没有值时候，清空隐藏框
            if (!(dataIdVal2 || sensorNumberVal2)) {
                $("#formOilBoxId2").val('');
                $("#sensorType2").val('');
            } else {
                //验证
                if (!hiddenOilBoxId2) {
                    editOilVehicleSetting.showErrorMsg("请选择已有的油箱型号", "oilBoxId2");
                    checkVFlag = false;
                }
                if (!hiddenSensorType2) {
                    editOilVehicleSetting.showErrorMsg("请选择已有的油箱型号", "sensorNumber2");
                    checkVFlag = false;
                }

            }
            return checkVFlag;


        },
        // 提交
        doSubmit: function () {
            editOilVehicleSetting.hideErrorMsg();
            var dataId = $("#oilBoxId").attr("data-id");
            var sensorNumber = $("#sensorNumber").attr("data-id");
            // 标定组数-主油箱
            var calibrationSets = $("#calibrationSets").val();
            // 标定组数-副油箱
            var calibrationSets2 = $("#calibrationSets2").val();
            if (parseInt(calibrationSets) < min_calibrationSets || parseInt(calibrationSets) > max_calibrationSets) {
                layer.msg("标定组数应介于" + min_calibrationSets + "到" + max_calibrationSets + "之间");
                return;
            } else if (!dataId) {
                editOilVehicleSetting.showErrorMsg("请选择已有的油箱型号", "oilBoxId");
                return;
            } else if (!sensorNumber) {
                editOilVehicleSetting.showErrorMsg("请选择已有的传感器型号", "sensorNumber");
                return;
            } else if (parseInt(calibrationSets2) < min_calibrationSets || parseInt(calibrationSets2) > max_calibrationSets) {
                layer.msg("标定组数应介于" + min_calibrationSets + "到" + max_calibrationSets + "之间");
                return;
            } else {
                //进行两个选择下拉框验证验证
                var checksFlag = editOilVehicleSetting.editCheckSelect();
                if (!checksFlag) {
                    return;
                }
                if (editOilVehicleSetting.validates()) {
                    if ($("#oilBoxId2").val() != "" && $("#oilBoxId2").val() != null) {
                        if ($("#sensorNumber2").val() == null || $("#sensorNumber2").val() == "") {
                            editOilVehicleSetting.showErrorMsg("请选择已有的传感器型号", "sensorNumber2");
                            return;
                        }
                    }
                    addHashCode($("#editForm"));
                    $("#editForm").ajaxSubmit(function (data) {
                        if (data != null) {
                            var result = $.parseJSON(data)
                                /* 关闭弹窗 */;
                            if (result.success) {
                                $("#commonWin").modal("hide");
                                editOilVehicleSetting.clearAll();
                                layer.msg("修改成功！", {move: false});
                            } else {
                                editOilVehicleSetting.showErrorMsg("请选择已有的油箱型号", "oilBoxId");
                            }
                        }
                        myTable.refresh();
                    });
                }
            }
        },
        closeWindow: function () {
            editOilVehicleSetting.clearAll();
        },
        showErrorMsg: function (msg, inputId) {
            if ($("#error_label_add").is(":hidden")) {
                $("#error_label_add").text(msg);
                $("#error_label_add").insertAfter($("#" + inputId));
                $("#error_label_add").show();
            } else {
                $("#error_label_add").is(":hidden");
            }
        },
        //错误提示信息隐藏
        hideErrorMsg: function () {
            $("#error_label_add").is(":hidden");
            $("#error_label_add").hide();
        },
        validates: function () {
            return $("#editForm").validate({
                rules: {
                    vehicleBrand: {
                        required: true,
                    },
                    oilBoxId: {
                        required: true,
                    },
                    automaticUploadTime: {
                        isInteger: true,
                        max: 300
                    },
                    outputCorrectionCoefficientK: {
                        isInteger: true,
                        range: [1, 200]
                    },
                    outputCorrectionCoefficientB: {
                        isInteger: true,
                        range: [0, 200]
                    },
                    addOilTimeThreshold: {
                        isInt1to400: true,
                        maxlength: 10
                    },
                    addOilAmountThreshol: {
                        isInt1to60: true,
                        maxlength: 10
                    },
                    seepOilTimeThreshold: {
                        isInt1to400: true,
                        maxlength: 10
                    },
                    seepOilAmountThreshol: {
                        isInt1to60: true,
                        maxlength: 10
                    },
                    sensorType: {
                        required: true,
                    },
                    automaticUploadTime2: {
                        isInteger: true,
                        max: 300
                    },
                    outputCorrectionCoefficientK2: {
                        isInteger: true,
                        range: [1, 200]
                    },
                    outputCorrectionCoefficientB2: {
                        isInteger: true,
                        range: [0, 200]
                    },
                    addOilTimeThreshold2: {
                        isInt1to400: true,
                        maxlength: 10
                    },
                    addOilAmountThreshol2: {
                        isInt1to60: true,
                        maxlength: 10
                    },
                    seepOilTimeThreshold2: {
                        isInt1to400: true,
                        maxlength: 10
                    },
                    seepOilAmountThreshol2: {
                        isInt1to60: true,
                        maxlength: 10
                    }
                },
                messages: {
                    vehicleBrand: {
                        required: publicNull,
                    },
                    oilBoxId: {
                        required:publicNull,
                    },
                    automaticUploadTime: {
                        isInteger: publicNumberInt,
                        max: oilvehicleSettingMax300Length
                    },
                    outputCorrectionCoefficientK: {
                        isInteger: publicNumberInt,
                        range:oilvehicleSetting201Length
                    },
                    outputCorrectionCoefficientB: {
                        isInteger: publicNumberInt,
                        range: oilvehicleSetting200Length
                    },
                    addOilTimeThreshold: {
                        required: publicNull,
                        maxlength: publicSize10
                    },
                    addOilAmountThreshol: {
                        required: publicNull,
                        maxlength: publicSize10
                    },
                    seepOilTimeThreshold: {
                        required: publicNull,
                        maxlength:publicSize10
                    },
                    seepOilAmountThreshol: {
                        required:publicNull,
                        maxlength:publicSize10
                    },
                    sensorType: {
                        required:publicNull
                    },
                    automaticUploadTime2: {
                        isInteger: publicNumberInt,
                        max: oilvehicleSettingMax300Length
                    },
                    outputCorrectionCoefficientK2: {
                        isInteger:publicNumberInt,
                        range: oilvehicleSetting201Length
                    },
                    outputCorrectionCoefficientB2: {
                        isInteger: publicNumberInt,
                        range: oilvehicleSetting200Length
                    },
                    addOilTimeThreshold2: {
                        required: publicNull,
                        maxlength: publicSize10
                    },
                    addOilAmountThreshol2: {
                        required: publicNull,
                        maxlength: publicSize10
                    },
                    seepOilTimeThreshold2: {
                        required: publicNull,
                        maxlength: publicSize10
                    },
                    seepOilAmountThreshol2: {
                        required:publicNull,
                        maxlength:publicSize10
                    }
                }
            }).form();
        },

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
    }
    $(function () {
        $('input').inputClear();
        editOilVehicleSetting.init();
        editOilVehicleSetting.initOilBoxShowHide(); //初始化油箱判断是否隐藏字段
        editOilVehicleSetting.initCalculateBtn();
        editOilVehicleSetting.initCalculateBtn2();
        $("#doSubmitBtn").bind("click", editOilVehicleSetting.doSubmit);
        $("#closeWindow,#closeModal").bind("click", editOilVehicleSetting.closeWindow);
        $("#sensorBasisInfoTwo,#tankBasisInfoTwo,#sensorBasisInfo,#tankBasisInfo").bind("click", editOilVehicleSetting.showTankOrSensorInfoFn);
    })
})(window, $)