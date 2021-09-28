(function (window, $) {
    var message = "传感器长度过长！应该小于油箱高度减去壁厚！";
    // 标定组数最小值
    var min_calibrationSets = 2;
    // 标定组数最大值
    var max_calibrationSets = 50;
    // 修正系数K默认值
    var default_outputCorrectionCoefficientK = 100;
    // 修正系数B默认值
    var default_outputCorrectionCoefficientB = 100;
    // 车辆list
    var vehicleList = JSON.parse($("#vehicleList").attr("value"));
    // 油箱list
    var fuelTankList = JSON.parse($("#fuelTankList").attr("value"));
    // 油杆传感器list
    var rodSensorList = JSON.parse($("#rodSensorList").attr("value"));
    // 初始化车辆数据
    var dataList = {value: []};
    //初始化油箱参数
    var fuelDataList = {value: []};
    var sensorList = {value: []};

     bindOilVehicleSetting = {
        init: function () {
        	
			$(".modal-body").addClass("modal-body-overflow");
			$(".modal-body").css({"height":"auto","max-height":($(window).height()-194) +"px"});
        	
            // 初始化油箱类型
            if (fuelTankList !== null && fuelTankList.length > 0) {
                for (var i = 0; i < fuelTankList.length; i++) {
                    var fuel = {};
                    fuel.id = fuelTankList[i].id;
                    fuel.name = fuelTankList[i].type;

                    fuelDataList.value.push(fuel);
                }
            }
            bindOilVehicleSetting.initOilBox(fuelDataList);
            bindOilVehicleSetting.initOilBox2(fuelDataList);
            // 初始化传感器类型
            if (rodSensorList !== null && rodSensorList.length > 0) {
                for (i = 0; i < rodSensorList.length; i++) {
                    var rodSensor = {};
                    rodSensor.id = rodSensorList[i].id;
                    rodSensor.name = rodSensorList[i].sensorNumber;
                    sensorList.value.push(rodSensor);
                }
            }
            bindOilVehicleSetting.initSensor(sensorList);
            bindOilVehicleSetting.initSensor2(sensorList);
            //主油箱
            $("#chooseTemplate").change(function () {
                if (this.value !== "" && this.value === "1") { // AD值标定模板
                    window.open("/clbs/file/vas/01.油箱标定导入表-AD值标定法.xlsx");
                } else if (this.value !== "" && this.value === "2") { // 标尺标定模板
                    window.open("/clbs/file/vas/02.油箱标定导入表-标尺标定法.xlsx");
                }
            });
            // 副油箱
            $("#chooseTemplate2").change(function () {
                if (this.value !== "" && this.value === "1") { // AD值标定模板
                    window.open("/clbs/file/vas/01.油箱标定导入表-AD值标定法.xlsx");
                } else if (this.value !== "" && this.value === "2") { // 标尺标定模板
                    window.open("/clbs/file/vas/02.油箱标定导入表-标尺标定法.xlsx");
                }
            });
            $("#calibrationSets").blur(function () {
                bindOilVehicleSetting.initCalculateBtn(); // 默认计算按钮是否可点击
            });
            $("#calibrationSets2").blur(function () {
                bindOilVehicleSetting.initCalculateBtn2(); // 默认计算按钮是否可点击
            });
            $("#outputCorrectionCoefficientK").blur(function () {
                bindOilVehicleSetting.checkOutputCorrectionCoefficientKModify(this); // 判断当前修正系数是否被修改
            });
            $("#outputCorrectionCoefficientK2").blur(function () {
                bindOilVehicleSetting.checkOutputCorrectionCoefficientKModify(this); // 判断当前修正系数是否被修改
            });
            $("#outputCorrectionCoefficientB").blur(function () {
                bindOilVehicleSetting.checkOutputCorrectionCoefficientBModify(this); // 判断当前修正系数是否被修改
            });
            $("#outputCorrectionCoefficientB2").blur(function () {
                bindOilVehicleSetting.checkOutputCorrectionCoefficientBModify(this); // 判断当前修正系数是否被修改
            });
            if (vehicleList !== null && vehicleList.length > 0) {
                var vehicleId = $("#vehicleId").val();
                for (i = 0; i < vehicleList.length; i++) {
                    var obj = {};
                    //删除相同车牌信息
                    if (vehicleList[i].vehicleId === vehicleId) {
                        vehicleList.splice(vehicleList[i].vehicleId.indexOf(vehicleId), 1);
                    }
                    //处理参考车牌只存在一条数据时防止浏览器抛出错误信息
                    if (vehicleList[i] === undefined) {
                        dataList.value.push(obj);
                    } else {
                        obj.id = vehicleList[i].vehicleId;
                        obj.name = vehicleList[i].brand;
                        dataList.value.push(obj);
                    }
                }
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
                // 从服务器请求选中的参考车牌的监控对象的信息
                // 当选择参考车牌
                var vehicleId = keyword.id;
                if (vehicleId != null && vehicleId != undefined && vehicleId != "") {
                    // 发送请求
                    var url = "/clbs/v/oilmassmgt/oilvehiclesetting/getBindInfo";
                    var datas = {"vehicleId":vehicleId};
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
                                    $("#sensorNumber").attr('data-id',vehicleInfo.sensorType);
                                    if (vehicleInfo.calibrationSets !== null && vehicleInfo.calibrationSets !== "") {
                                        $("#calibrationSets").val(vehicleInfo.calibrationSets);
                                    }

                                    bindOilVehicleSetting.initOilBox(fuelDataList);
                                    bindOilVehicleSetting.initOilBox2(fuelDataList);
                                    bindOilVehicleSetting.initSensor(sensorList);
                                    bindOilVehicleSetting.initSensor2(sensorList);

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
                                    $("#sensorNumber2").attr('data-id',vehicleInfo.sensorType2);

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
                bindOilVehicleSetting.initCalculateBtn(); // 默认计算按钮是否可点击（主油箱）
                bindOilVehicleSetting.initCalculateBtn2(); // 默认计算按钮是否可点击
            }).on('onUnsetSelectValue', function () {
            });
            // 解决IE浏览器下拉表默认展开问题
            setTimeout(function () {
                $('.dropdown-menu-right').hide();
            }, 0);
        },
        //初始化油箱型号
        initOilBox: function (publicList) {
            if (publicList.value.length === 0 || bindOilVehicleSetting.compareLength()) {//油箱高度与油杆传感器长度判断，
                bindOilVehicleSetting.clearOil();
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
            	bindOilVehicleSetting.oilBoxIdBlur()
                var fuel1Id = keyword.id;
                for (var i = 0; i < fuelTankList.length; i++) {
                    if (fuel1Id === fuelTankList[i].id) {
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
                        bindOilVehicleSetting.initSensor(list);
                    }
                }
                var shape = $("#shape").val();
                if (shape == "1") {
                	$("#cuboidForm").show();
                } else {
                	$("#cuboidForm").hide();
                }
                $("#error_label_add").hide();
                bindOilVehicleSetting.initCalculateBtn(); // 默认计算按钮是否可点击（主油箱）
            }).on('onUnsetSelectValue', function () {
            	
            }).on('onClearSelectValue', function () {
            	bindOilVehicleSetting.oilBoxIdBlur()
            })
        },
        //初始化油箱传感器型号
        initOilBox2: function (publicList) {
            if (publicList.value.length === 0 || bindOilVehicleSetting.compareLength2()) {//油箱高度与油杆传感器长度判断，
                bindOilVehicleSetting.clearOil2();
            }
            $("#oilBoxId2").bsSuggest("destroy"); // 销毁事件
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
            	bindOilVehicleSetting.oilBoxIdBlur2()
                var fuel1Id = keyword.id;
                for (var i = 0; i < fuelTankList.length; i++) {
                    if (fuel1Id === fuelTankList[i].id) {
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
                        bindOilVehicleSetting.initSensor2(list);
                    }
                }
                var shape = $("#shape2").val();
                if (shape == "1") {
                	$("#cuboidForm2").show();
                } else {
                	$("#cuboidForm2").hide();
                }
                $("#error_label_add").hide();
                bindOilVehicleSetting.initCalculateBtn2(); // 默认计算按钮是否可点击（副油箱）
            }).on('onUnsetSelectValue', function () {
            }).on('onClearSelectValue', function () {
            	bindOilVehicleSetting.oilBoxIdBlur2()
            })
        },
        initSensor: function (publicList) {

            if (publicList.value.length === 0 || bindOilVehicleSetting.compareLength()) {//油箱高度与油杆传感器长度判断，
                bindOilVehicleSetting.clearSensor();
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
            	bindOilVehicleSetting.sensorBlur()
                var sensorId = keyword.id;
                for (var i = 0; i < rodSensorList.length; i++) {
                    if (sensorId === rodSensorList[i].id) {
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
                        bindOilVehicleSetting.initOilBox(list);
                    }
                }
                $("#error_label_add").hide();
                bindOilVehicleSetting.initCalculateBtn(); // 默认计算按钮是否可点击（主油箱）
            }).on('onUnsetSelectValue', function () {
            }).on('onClearSelectValue', function () {
            	bindOilVehicleSetting.sensorBlur()
            })
        },
        initSensor2: function (publicList) {
            if (publicList.value.length === 0 || bindOilVehicleSetting.compareLength2()) {//油箱高度与油杆传感器长度判断，
                bindOilVehicleSetting.clearSensor2();
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
            	bindOilVehicleSetting.sensorBlur2()
                var sensorId2 = keyword.id;
                for (var i = 0; i < rodSensorList.length; i++) {
                    if (sensorId2 === rodSensorList[i].id) {
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
                        bindOilVehicleSetting.initOilBox2(list);
                    }
                }
                $("#error_label_add").hide();
                bindOilVehicleSetting.initCalculateBtn2(); // 默认计算按钮是否可点击（副油箱）
            }).on('onUnsetSelectValue', function () {

            }).on('onClearSelectValue', function () {
            	bindOilVehicleSetting.sensorBlur2()
            })
        },
        oilBoxIdBlur: function () {//油箱型号失去焦点后、重新加载下拉选
            var oilValue = $("#oilBoxId").val();
            var sensorValue = $("#sensorNumber").val();
            if (oilValue === "" && sensorValue === "") {//当油箱型号与传感器型号都为空时重新加载
                bindOilVehicleSetting.initOilBox(fuelDataList);
                bindOilVehicleSetting.initSensor(sensorList);
            }
            if (oilValue === "" && sensorValue !== "") {
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
                bindOilVehicleSetting.initOilBox(list);
            }
//            $("#oilchance").hide();
//            $("#sensorchance").hide();
        },
        oilBoxIdBlur2: function () {//油箱型号失去焦点后、重新加载下拉选
            var oilValue2 = $("#oilBoxId2").val();
            var sensorValue2 = $("#sensorNumber2").val();
            if (oilValue2 === "" && sensorValue2 === "") {//当油箱型号与传感器型号都为空时重新加载
                bindOilVehicleSetting.initOilBox2(fuelDataList);
                bindOilVehicleSetting.initSensor2(sensorList);
                $("#oilchance2").hide();
                $("#sensorchance2").hide();
            }
            if (oilValue2 === "" && sensorValue2 !== "") {
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
                bindOilVehicleSetting.initOilBox2(list);
            }
            $("#oilchance2").hide();
            $("#sensorchance2").hide();
        },
        sensorBlur: function () {//传感器型号失去焦点后、重新加载下拉选
            var oilValue = $("#oilBoxId").val();
            var sensorValue = $("#sensorNumber").val();
            if (oilValue === "" && sensorValue === "") {//当油箱型号与传感器型号都为空时重新加载
                bindOilVehicleSetting.initOilBox(fuelDataList);
                bindOilVehicleSetting.initSensor(sensorList);
            }
            var height = $("#height").val();
            if (oilValue !== "" && sensorValue === "") {
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
                bindOilVehicleSetting.initSensor(list);
            }
            $("#oilchance").hide();
            $("#sensorchance").hide();
        },
        sensorBlur2: function () {//传感器型号失去焦点后、重新加载下拉选
            var oilValue2 = $("#oilBoxId2").val();
            var sensorValue2 = $("#sensorNumber2").val();
            if (oilValue2 === "" && sensorValue2 === "") {//当油箱型号与传感器型号都为空时重新加载
                bindOilVehicleSetting.initOilBox2(fuelDataList);
                bindOilVehicleSetting.initSensor2(sensorList);
            }
            var height2 = $("#height2").val();
            if (oilValue2 !== "" && sensorValue2 === "") {
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
                bindOilVehicleSetting.initSensor2(list);
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
            $("#sensorNumber").attr("data-id","");
        },
        clearSensor2: function () {// 清空传感器型号信息
            $("#sensorLength2").val("");
            $("#filteringFactor2").val("");
            $("#baudRate2").val("");
            $("#oddEvenCheck2").val("");
            $("#compensationCanMake2").val("");
            $("#sensorType2").val("");
            $("#sensorNumber2").val("");
            $("#sensorNumber2").attr("data-id","");
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
            $("#oilBoxId").attr("data-id","");
        },
        clearOil2: function () {//清空油箱信息
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
            $("#oilBoxId2").attr("data-id","");
        },
        clearAll : function(){
            bindOilVehicleSetting.clearSensor();
            bindOilVehicleSetting.clearSensor2();
            bindOilVehicleSetting.clearOil();
            bindOilVehicleSetting.clearOil2();
            $("#tankBasisInfo-content").hide();
            $("#sensorBasisInfo-content").hide();
            $("#tankBasisInfo font").html("显示更多");
            $("#tankBasisInfo font").html("显示更多");
        },
        // 判断修正系数K是否被修改
        checkOutputCorrectionCoefficientKModify: function (ele) {
            if (ele.value !== '' && !isNaN(ele.value) && parseFloat(ele.value) !== default_outputCorrectionCoefficientK) {
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
            if (ele.value !== '' && !isNaN(ele.value) && parseFloat(ele.value) !== default_outputCorrectionCoefficientB) {
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
            if (l !== "" && w !== "" && h !== "" && t !== "" && s !== "" && sensorLength !== "" && calibrationSets !== ""
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
            if (l !== "" && w !== "" && h !== "" && t !== "" && s !== "" && sensorLength !== "" && calibrationSets !== ""
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
            } else if (oilBoxId === "") {
                layer.msg("请选择油箱型号");
                return;
            } else if (sensorLength === "") {
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
                    "buttomRadius" : r1, "topRadius" : r2
                },
                success: function (data) {
                    if (data.success) {
                        layer.msg("计算成功");
                    }
                    if (data !== null && data.obj !== null && data.obj.result !== null) {
                        var oilLevelHeights = "";
                        var oilValues = "";
                        var boxId = data.obj.result[0].id;
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
            var boxNum = "2";
            var r1 = "";
			var r2 = "";
			if (shape == "1") {
				r1 = $("#buttomRadius2").val();
				r2 = $("#topRadius2").val();
			}
            if (parseInt(calibrationSets) > max_calibrationSets || parseInt(calibrationSets) < min_calibrationSets) {
                layer.msg("标定组数介于" + min_calibrationSets + "-" + max_calibrationSets + "之间");
                return;
            } else if (oilBoxId2 === "") {
                layer.msg("请选择油箱型号");
                return;
            } else if (sensorLength === "") {
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
                    "buttomRadius" : r1, "topRadius" : r2
                },
                success: function (data) {
                    if (data.success) {
                        layer.msg("计算成功");
                    }
                    if (data !== null && data.obj !== null && data.obj.result !== null) {
                        var oilLevelHeights = "";
                        var oilValues = "";
                        var boxId = data.obj.result[0].id2;
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
        // 提交
        doSubmit: function () {
        	bindOilVehicleSetting.hideErrorMsg();
            // 标定组数-主油箱
           var dataId=$("#oilBoxId").attr("data-id");
           var sensorNumber=$("#sensorNumber").attr("data-id");
            var calibrationSets = $("#calibrationSets").val();
            // 标定组数-副油箱
            var calibrationSets2 = $("#calibrationSets2").val();
            if (parseInt(calibrationSets) < min_calibrationSets || parseInt(calibrationSets) > max_calibrationSets) {
                layer.msg("【主油箱】标定组数应介于" + min_calibrationSets + "到" + max_calibrationSets + "之间");
                return;
            } else if (!dataId) {
                bindOilVehicleSetting.showErrorMsg("请选择已有的油箱型号", "oilBoxId");
                return;
            } else if (!sensorNumber) {
                bindOilVehicleSetting.showErrorMsg("请选择已有的传感器型号", "sensorNumber");
                return;
            } else if (parseInt(calibrationSets2) < min_calibrationSets || parseInt(calibrationSets2) > max_calibrationSets) {
                layer.msg("【副油箱】标定组数应介于" + min_calibrationSets + "到" + max_calibrationSets + "之间");
                return;
            } else {

                var dataId2=$("#oilBoxId2").attr("data-id");
                var sensorNumber2=$("#sensorNumber2").attr("data-id");
                var dataIdVal2=$("#oilBoxId2").val();
                var sensorNumberVal2=$("#sensorNumber2").val();
                //判断副油箱如果邮箱型号和传感器型号其中一个有值的时候，进行条件判断
                if(dataIdVal2 || sensorNumberVal2) {
                    if (!dataId2) {
                        bindOilVehicleSetting.showErrorMsg("请选择已有的油箱型号", "oilBoxId2");
                        return;
                    }
                    if (!sensorNumber2) {
                        bindOilVehicleSetting.showErrorMsg("请选择已有的传感器型号", "sensorNumber2");
                        return;
                    }
                }else{
                    $("#formOilBoxId2").val("");
                    $("#sensorNumber2").val("");

                }

                if (bindOilVehicleSetting.validates()) {
                    if ($("#oilBoxId2").val() !== "" && $("#oilBoxId2").val() !== null) {
                        if ($("#sensorNumber2").val() === null || $("#sensorNumber2").val() === "") {
                            bindOilVehicleSetting.showErrorMsg("请选择已有的传感器型号", "sensorNumber2");
                            return;
                        }
                    }
                    addHashCode($("#bindForm"));
                    $("#bindForm").ajaxSubmit(function (data) {

                        if (data !== null) {
                            var result = JSON.parse(data)
                                /* 关闭弹窗 */;
                            if (result.success) {
                                $("#commonWin").modal("hide");
                                bindOilVehicleSetting.clearAll();
                                layer.msg("绑定成功！", {move: false});
                            } else {
                                bindOilVehicleSetting.showErrorMsg("主油箱请选择已有的油箱型号", "oilBoxId");
                            }
                        }
                        myTable.refresh()
                    });
                }
            }
        },
         closeWindow : function () {
             bindOilVehicleSetting.clearAll();
         },
        showErrorMsg: function(msg, inputId){
            if ($("#error_label_add").is(":hidden")) {
                $("#error_label_add").text(msg);
                $("#error_label_add").insertAfter($("#" + inputId));
                $("#error_label_add").show();
            } else {
                $("#error_label_add").is(":hidden");
            }
        },
        //错误提示信息隐藏
        hideErrorMsg: function(){
            $("#error_label_add").is(":hidden");
            $("#error_label_add").hide();
        },
        validates: function () {
            return $("#bindForm").validate({
                rules: {
                    vehicleBrand: {
                        required: true
                    },
                    oilBoxId: {
                        required: true
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
                    calibrationSets: {
                        required: true
                    },
                    sensorNumber: {
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
                        required: publicNull
                    },
                    oilBoxId: {
                        required: publicNull
                    },
                    automaticUploadTime: {
                        isInteger: publicNumberInt,
                        max: oilvehicleSettingMax300Length
                    },
                    outputCorrectionCoefficientK: {
                        isInteger: publicNumberInt,
                        range: oilvehicleSetting201Length
                    },
                    outputCorrectionCoefficientB: {
                        isInteger:publicNumberInt,
                        range:oilvehicleSetting200Length
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
                        maxlength: publicSize10
                    },
                    seepOilAmountThreshol: {
                        required: publicNull,
                        maxlength: publicSize10
                    },
                    calibrationSets: {
                        required: publicNull
                    },
                    sensorNumber: {
                        required: publicNull
                    },
                    automaticUploadTime2: {
                        isInteger:publicNumberInt,
                        max: oilvehicleSettingMax300Length
                    },
                    outputCorrectionCoefficientK2: {
                        isInteger: publicNumberInt,
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
                        required: publicNull,
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
        bindOilVehicleSetting.init();
        bindOilVehicleSetting.initCalculateBtn(); // 默认计算按钮是否可点击（主油箱）
        bindOilVehicleSetting.initCalculateBtn2(); // 默认计算按钮是否可点击
        $("#doSubmit").bind("click", bindOilVehicleSetting.doSubmit);
        $("#closeWindow,#closeModal").bind("click", bindOilVehicleSetting.closeWindow);
        $("#sensorBasisInfoTwo,#tankBasisInfoTwo,#sensorBasisInfo,#tankBasisInfo").bind("click", bindOilVehicleSetting.showTankOrSensorInfoFn);
    })
})(window, $);