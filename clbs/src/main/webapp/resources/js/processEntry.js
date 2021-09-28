(function (window, $) {
    var cid = '';
    var brandValB;
    var deviceValB;
    var simValB;
    var groupingNum = 2;
    var people = 2
    var intervalFlag = true;
    var id = 2;
    var enterFlag = false;
    var flag1 = false; // 选择还是录入的车牌号
    var flag2 = true; // 选择还是录入的终端号
    var flag3 = true; // 选择还是录入的终端手机号
    var datas;
    var objType = 0;
    // 第一次进页面默认查询的数据
    var vehicleInfoList = [];
    var peopleInfoList = [];
    var thingInfoList = [];
    var deviceInfoList = [];
    var deviceInfoListForPeople = [];
    var simCardInfoList = [];
    var professionalsInfoList = [];
    var professionalDataList;
    var orgId = "";
    var orgName = "";
    var processInput = {
        //初始化
        init: function () {
            var setting = {
                async: {
                    url: "/clbs/m/basicinfo/enterprise/professionals/tree",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    contentType: "application/json",
                    dataType: "json",
                },
                view: {
                    dblClickExpand: false
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    beforeClick: processInput.beforeClick,
                    onClick: processInput.onClick,
                }
            };
            var setting1 = {
                async: {
                    url: "/clbs/m/infoconfig/infoinput/tree",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    contentType: "application/json",
                    dataType: "json",
                    dataFilter: processInput.ajaxDataFilter
                },
                view: {
                    dblClickExpand: false
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    beforeClick: processInput.beforeClick,
                    onClick: processInput.onClick1,
                }
            };
            $.fn.zTree.init($("#ztreeDemo"), setting, null);
            $.fn.zTree.init($("#ztreeDemo1"), setting1, null); // 分组初始化
            $("#brandVal").attr("value", $("#brands").val());
            json_ajax("POST", "/clbs/m/infoconfig/infoinput/addlist_", "json", true, {"id": ""}, processInput.InitCallback);
            //json_ajax("POST","/clbs/m/basicinfo/monitoring/vehicle/addList","json",false,{},processInput.InitCallback1);
            var urlFuelType = "/clbs/m/basicinfo/monitoring/vehicle/findAllFuelType";
            json_ajax("POST", urlFuelType, "json", false, null, processInput.getFuelTypeCallback);
            $("[data-toggle='tooltip']").tooltip();
            //加载日历控件
            laydate.render({elem: '#openCardTime', theme: '#6dcff6'});
            laydate.render({elem: '#endTime', theme: '#6dcff6'});
        },
        //燃料类型
        getFuelTypeCallback: function (data) {
            for (var i = 0; i < data.obj.FuelTypeList.length; i++) {
                if ("柴油" == data.obj.FuelTypeList[i].fuelCategory) {
                    $("#dieselOil").append("<option value=" + data.obj.FuelTypeList[i].id + ">" + data.obj.FuelTypeList[i].fuelType + "</option>");
                } else if ("汽油" == data.obj.FuelTypeList[i].fuelCategory) {
                    $("#gasoline").append("<option value=" + data.obj.FuelTypeList[i].id + ">" + data.obj.FuelTypeList[i].fuelType + "</option>");
                } else if ("天然气" == data.obj.FuelTypeList[i].fuelCategory) {
                    $("#naturalGas").append("<option value=" + data.obj.FuelTypeList[i].id + ">" + data.obj.FuelTypeList[i].fuelType + "</option>");
                }
            }
        },
        /*InitCallback1: function(data){
            var dataLength = data.obj.VehicleTypeList.length;
            for (var i = 0; i < dataLength; i++){
                $("#vehicleType").append("<option value=" + data.obj.VehicleTypeList[i].id + ">" + data.obj.VehicleTypeList[i].vehicleType + "</option>")
            }
        },*/
        InitCallback: function (data) {
            if (data.success) {
                datas = data.obj;
                vehicleInfoList = datas.vehicleInfoList;
                peopleInfoList = datas.peopleInfoList;
                thingInfoList = datas.thingInfoList;
                deviceInfoList = datas.deviceInfoList;
                simCardInfoList = datas.simCardInfoList;
                deviceInfoListForPeople = datas.deviceInfoListForPeople;
                professionalsInfoList = datas.professionalsInfoList;
                orgId = datas.orgId;
                orgName = datas.orgName;
                processInput.putConfigValue();
            } else {
                layer.msg(data.msg);
            }
            /* $("#chooseCar").bind("click", processInput.chooseCar);
             $("#choosePeople").bind("click", processInput.choosePeople);
             $("#choosePeopleLab").on("click", processInput.choosePeopleLabClick);
             $("#chooseCarLab").on("click", processInput.chooseCarLabClick);*/
            $("label.monitoringSelect").on("click", processInput.chooseLabClick);
            $("#objectMonitoring").removeClass('disabled loading-state-button').find('i').attr("class", 'caret');
        },
        putConfigValue: function () {
            processInput.setDefaultOrgValue(orgId, orgName); // 设置默认的所属企业
            var dataList = {
                value: []
            };
            $("#brands").bsSuggest("destroy"); // 销毁事件
            if (objType == 0) {
                var i = vehicleInfoList.length;
                while (i--) {
                    dataList.value.push({
                        name: vehicleInfoList[i].brand,
                        id: vehicleInfoList[i].id,
                        type: vehicleInfoList[i].monitorType,
                    });
                }
            } else if (objType == 1) {
                var i = peopleInfoList.length;
                while (i--) {
                    dataList.value.push({
                        name: peopleInfoList[i].brand,
                        id: peopleInfoList[i].id,
                        type: peopleInfoList[i].monitorType,
                    });
                }
            } else if (objType == 2) {
                var i = thingInfoList.length;
                while (i--) {
                    dataList.value.push({
                        name: thingInfoList[i].brand,
                        id: thingInfoList[i].id,
                        type: thingInfoList[i].monitorType,
                    });
                }
            }
            $("#brands").bsSuggest({
                indexId: 1, //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                keyField: "name",
                effectiveFields: ["name"],
                searchFields: ["name"],
                data: dataList
            }).on('onDataRequestSuccess', function (e, result) {
                $('#brands').removeAttr('disabled');
            }).on('onSetSelectValue', function (e, keyword, data) {
                $(".charNew").hide(); // 选择的下拉框的值，隐藏新增模块
                $("#charBtn").hide();
                $("#detailBtn").show();
                $("#brandVal").attr("value", keyword.id);
                // 根据选择的车牌号id，查询车辆详情，并在页面显示
                if (objType == 0) {
                    processInput.getVehicleInfoDetailById(keyword.id);
                } else if (objType == 1) {
                    processInput.getPeopleInfoDetailById(keyword.id);
                } else if (objType == 2) {
                    processInput.getThingInfoDetailById(keyword.id);
                }
                processInput.checkIsBound("brands", keyword.name); // 校验当前车牌号是否已经被绑定，两个人同时操作的时候可能会出现
                processInput.hideErrorMsg();
                flag1 = true;
            }).on('onUnsetSelectValue', function () {
                $(".charNew").show(); // 手动填写的值，显示新增模块
                $(".detailMessage").hide();
                $("#brandVal").attr("value", "");
                flag1 = false;
            });
            var deviceDataList = {value: []};
            $("#devices").bsSuggest("destroy"); // 销毁事件
            /*if (objType == 0) {*/
            var j = deviceInfoList.length;
            while (j--) {
                deviceDataList.value.push({
                    name: deviceInfoList[j].deviceNumber,
                    id: deviceInfoList[j].id,
                });
            }
            /*} else if (objType == 1) {
                var j = deviceInfoListForPeople.length;
                while (j--) {
                    deviceDataList.value.push({
                        name: deviceInfoListForPeople[j].deviceNumber,
                        id: deviceInfoListForPeople[j].id,
                    });
                }
            }*/
            $("#devices").bsSuggest({
                indexId: 1, //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                data: deviceDataList,
                effectiveFields: ["name"]
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                $(".terminalNew").hide(); // 选择的下拉框的值，隐藏新增模块
                $("#terminalBtn").hide();
                $("#devDetailBtn").show();
                $("#deviceVal").attr("value", keyword.id);
                // 根据选择的终端id，查询终端详情，并在页面显示
                processInput.getDeviceInfoDetailById(keyword.id);
                processInput.checkIsBound("devices", keyword.name); // 校验当前终端编号号是否已经被绑定，两个人同时操作的时候可能会出现
                processInput.hideErrorMsg();
                flag2 = true;
            }).on('onUnsetSelectValue', function () {
                $(".terminalNew").show(); // 手动填写的值，显示新增模块
                $(".equipmentMessage").hide();
                $("#deviceVal").attr("value", "");
                flag2 = false;
            });
            var simDataList = {value: []}, k = simCardInfoList.length;
            while (k--) {
                simDataList.value.push({
                    name: simCardInfoList[k].simcardNumber,
                    id: simCardInfoList[k].id,
                });
            }
            $("#sims").bsSuggest("destroy"); // 销毁事件
            $("#sims").bsSuggest({
                indexId: 1, //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                data: simDataList,
                effectiveFields: ["name"]
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                $(".SIMNew").hide(); // 选择的下拉框的值，隐藏新增模块
                $("#SIMBtn").hide();
                $("#SIMDetailBtn").show();
                $("#simVal").attr("value", keyword.id);
                // 根据选择的终端手机号id，查询终端手机号详情，并在页面显示
                processInput.getSimCardInfoDetailById(keyword.id);
                processInput.checkIsBound("sims", keyword.name); // 校验当前终端手机号是否已经被绑定，两个人同时操作的时候可能会出现
                processInput.hideErrorMsg();
                flag3 = true;
            }).on('onUnsetSelectValue', function () {
                $(".SIMNew").show(); // 手动填写的值，显示新增模块
                $(".SIMMessage").hide();
                $("#simVal").attr("value", "")
                flag3 = false;
            });
            professionalDataList = {value: []}, l = professionalsInfoList.length;
            while (l--) {
                professionalDataList.value.push({
                    name: professionalsInfoList[l].name,
                    id: professionalsInfoList[l].id,
                });
            }
            $("#professionals").bsSuggest("destroy"); // 销毁事件
            $("#professionals").bsSuggest({
                indexId: 1, //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                data: professionalDataList,
                effectiveFields: ["name"]
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                if (processInput.checkDoubleChoosePro(keyword.id)) {// 校验是否重复选择了从业人员
                    $("#professionalsidVal").attr("value", keyword.id);
                    $(this).siblings("input").val(keyword.id);
                    // 根据选择的从业人员id，查询从业人员详情，并在页面显示
                    processInput.getProfessionalDetailById(keyword.id, "0", "");
                } else {
                    processInput.clearValues(this);
                }
            }).on('onUnsetSelectValue', function () {
                $("#professionalsidVal").attr("value", "");
                $(this).siblings("input").val("");
            });
            $(".peopleListMessage").click(function () {
                $(this).parent().parent().next().toggle();
            });

            processInput.inputValueChange();
        },
        // 文本框复制事件处理
        inputOnPaste: function (eleId) {
            if (eleId == "brands") {
                $("#brandVal").attr("value", "");
                flag1 = false;
            }
            if (eleId == "devices") {
                $("#deviceVal").attr("value", "");
                flag2 = false;
            }
            if (eleId == "sims") {
                $("#simVal").attr("value", "");
                flag3 = false;
            }
        },
        charBtn: function () {
            $(".detailMessage").hide();
            $(".charNew").toggle();
        },
        backSelect: function () {
            $("#addNew").removeClass("hidden");
            $(".detailMessage").show();
            $("#brandVal").attr("value", brandValB);
            $(".charNew").css("display", "none");
        },
        terminalBtn: function () {
            $(".equipmentMessage").hide();
            $(".terminalNew").toggle();
        },
        backSelect2: function () {
            $(".equipmentMessage").show();
            $("#addNew2").removeClass("hidden");
            $("#deviceVal").attr("value", deviceValB);
            $(".terminalNew").css("display", "none");
        },
        //分组新增
        groupingAdd: function (e) {
            $("#menuContent").appendTo($("#zTreeGroupArea"));
            var message = $("#groupMessage").clone(true);
            message.attr("id", "groupMessage" + groupingNum);
            //var html = '<div id="groupingNum' + groupingNum + '" class="form-group added_group"><label class="col-sm-3 col-md-3 control-label"><label class="text-danger">*</label> 分组：</label><div class="has-feedback col-sm-5 col-md-3"><input id="citySel' + groupingNum + '" name="citySel" class="form-control zTreeCommon" readonly="readonly" style="background-color: #fafafa; cursor: pointer;" placeholder="请选择分组" value="" type="text"><input id="groupPid ' + groupingNum + '" name="groupPid" value="" type="hidden"></div><div class="col-md-2 topSpace"><button type="button" class="btn btn-danger grouping_Delete deleteIcon"><span class="glyphicon glyphicon-trash" aria-hidden="true"></span></button> <button type="button"class="btn btn-primary optionalListMessage detailIcon"><span class="glyphicon glyphicon-eye-open"></span>详情</span></button></div></div>';
            var html = '<div id="groupingNum' + groupingNum + '" class="form-group added_group"><label class="col-sm-3 col-md-3 control-label"><label class="text-danger">*</label> 分组：</label><div class="has-feedback col-sm-5 col-md-3"><div class="searchListSelect has-feedback"><input id="citySel' + groupingNum + '" name="citySel" class="form-control zTreeCommon" readonly="readonly" style="background-color: #fafafa; cursor: pointer;" placeholder="请选择分组" value="" type="text"><input class="groupPid" id="groupPid ' + groupingNum + '" name="groupPid" value="" type="hidden"><span class="fa fa-chevron-down form-control-feedback" style="top: 0; right: 10px!important;cursor:pointer;" aria-hidden="true"></span></div></div><div class="col-md-2 topSpace"><button type="button" class="btn btn-danger grouping_Delete deleteIcon"><span class="glyphicon glyphicon-trash" aria-hidden="true"></span></button> <button type="button"class="btn btn-primary optionalListMessage detailIcon"><span class="glyphicon glyphicon-eye-open"></span>详情</span></button></div>';
            $("#addGrouping").append(html);
            message.insertAfter($("#" + "groupingNum" + groupingNum));
            // 复制详情时将原先的值清空
            $("#" + "groupMessage" + groupingNum).find("#group_name").html("");
            $("#" + "groupMessage" + groupingNum).find("#group_parent").html("");
            $("#" + "groupMessage" + groupingNum).find("#group_principal").html("");
            $("#" + "groupMessage" + groupingNum).find("#group_phone").html("");
            // 克隆组织时，清除其原先的值
            $(".optionalListMessage").unbind("click").click(function (event) {
                $(this).parent().parent().next().toggle();
                event.stopPropagation();
            });
            $(".zTreeCommon").unbind("click").on("click", function () {
                processInput.showMenu1(this)
            });
            $(".zTreeCommon").siblings(".form-control-feedback").on("click", function () {
                $(this).siblings(".zTreeCommon").click()
            });
            groupingNum++;
            $(".grouping_Delete").unbind("click").click(function () {
                $("#menuContent").appendTo($("#zTreeGroupArea"));
                var id = $(this).parent().parent().attr("id");
                var idNum = id.replace("groupingNum", "");
                var listId = "groupMessage" + idNum;
                $("#" + listId).remove();
                $(this).parent().parent().remove();
            });
        },
        peopleAdd: function () {
            var obj = $("#workPeople").clone(true);
            var message = $("#peopleMessage").clone(true);
            message.attr("id", "peopleMessage" + people);
            message.attr("id", "peopleMessage" + people).css("margin-bottom", 30 + "px");
            var html = obj.html() + '<div class="peopleMessage-style"><button type="button" class="btn btn-danger people_list_Delete deleteIcon"><span class="glyphicon glyphicon-trash" aria-hidden="true"></span></button> <button type="button"class="btn btn-primary peopleListMessage detailIcon"><span class="glyphicon glyphicon-eye-open"></span>详情</span></button></div>';
            obj.attr("id", "peopleNum" + people).html(html);
            obj.attr("class", "form-group added_group");
            obj.children().children().children("input").attr("id", "professionals" + people);
            obj.children().children().children("input").attr("value", "");
            obj.children("input").attr("id", "professionalsidVal" + people);
            obj.children("input").attr("value", "");
            obj.appendTo($("#people-add-area"));
            message.insertAfter($("#" + "peopleNum" + people));
            // 清除克隆从业人员详情的值
            processInput.clearCloneProfessionalValue(obj.attr("id"));
            var objInput = obj.children().children().children("input");
            if (objInput.val() == "") {
                objInput.siblings("i").hide();
            }
            $("#professionals" + people).bsSuggest({
                indexId: 1, //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                data: professionalDataList,
                effectiveFields: ["name"]
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                var nump = people - 1;
                if (processInput.checkDoubleChoosePro(keyword.id)) {// 校验是否重复选择了从业人员
                    $("#professionalsidVal" + nump).attr("value", keyword.id);
                    $(this).siblings("input").val(keyword.id);
                    // 根据选择的从业人员id，查询从业人员详情，并在页面显示
                    processInput.getProfessionalDetailById(keyword.id, "1", obj.attr("id"));
                } else {
                    processInput.clearValues(this);
                }
            }).on('onUnsetSelectValue', function () {
                $("#professionalsidVal").attr("value", "");
                $(this).siblings("input").val("");
            });
            $(".peopleListMessage").off("click").click(function (event) {
                event.stopPropagation();
                $(this).parent().parent().next().toggle();
            });
            people++;
            $(".people_list_Delete").click(function () {
                var id = $(this).parent().parent().attr("id");
                var idNum = id.replace("peopleNum", "");
                var listId = "peopleMessage" + idNum;
                $("#" + listId).remove();
                $(this).parent().parent().remove();
            });
        },
        //终端手机号
        SIMBtn: function () {
            $(".SIMMessage").hide();
            $(".SIMNew").toggle();
        },
        backSelect3: function () {
            $(".SIMMessage").show();
            $("#addNew3").removeClass("hidden");
            $("#simVal").attr("value", simValB);
            $(".SIMNew").css("display", "none");
        },
        addRight: function () {
            var $options = $("#select1 input:checked");
            $options.parents(".checkbox").appendTo($("#select2"));
            var value = $("#select2 input").length
            if (value == 0) {
                value = "";
            }
            $("#checkNum").attr("value", value);
        },
        addLeft: function () {
            var $options = $("#select2 input:checked");
            $options.parents(".checkbox").appendTo($("#select1"));
            var value = $("#select2 input").length
            if (value == 0) {
                value = "";
            }
            $("#checkNum").attr("value", value);
        },
        numberBtn: function () {
            var obj = $("#number1").clone(true);
            var ht = "编号" + id + ":";
            obj.children("label").html(ht);
            var html = obj.html() + '<button type="button" class="btn btn-danger number_Delete addNumber">删除</button>';
            obj.attr("id", "number" + id).html(html);
            obj.insertBefore($("#addArea"));
            id++;
            $(".number_Delete").click(function () {
                $(this).parent().remove();
            })
        },
        //流程验证
        validates: function () {
            return $("#addForm1").validate({
                rules: {
                    car_groupName: {
                        required: true
                    },
                    vehicleNumber: {
                        required: false,
                        maxlength: 20
                    },
                    vehicleOwner: {
                        checkPeopleName: true
                    },
                    vehicleOwnerPhone: {
                        isLandline: true
                    },
                    aliases: {
                        maxlength: 20
                    },
                    vehicleType: {
                        required: true
                    },
                    chassisNumber: {
                        required: false,
                        maxlength: 50
                    },
                    engineNumber: {
                        required: false,
                        maxlength: 20
                    },
                    plateColor: {
                        required: false
                    },
                    areaAttribute: {
                        required: false
                    },
                    city: {
                        required: false
                    },
                    province: {
                        required: false
                    },
                    fuelType: {
                        required: false
                    },
                    citySel: {
                        required: true
                    },
                    //人员验证
                    peo_groupName: {
                        required: true
                    },
                    name: {
                        checkPeopleName: true
                    },
                    identity: {
                        isIdCardNo: true,
                    },
                    phone: {
                        isTel: true
                    },
                    email: {
                        email: true,
                        maxlength: 20
                    },
                    // 终端验证
                    device_groupName: {
                        required: true
                    },
                    deviceName: {
                        required: false,
                        maxlength: 50
                    },
                    deviceType: {
                        required: true
                    },
                    functionalType: {
                        required: true
                    },
                    isVideo: {
                        required: false
                    },
                    barCode: {
                        required: false,
                        maxlength: 64
                    },
                    channelNumber: {
                        required: false
                    },
                    isStart: {
                        required: true
                    },
                    manuFacturer: {
                        required: false,
                        maxlength: 100
                    },

                    // sim验证
                    sim_groupName: {
                        required: true
                    },
                    isStart: {
                        required: false,
                        maxlength: 6
                    },
                    operator: {
                        required: false,
                        maxlength: 50
                    },
                    openCardTime: {
                        required: false
                    },
                    capacity: {
                        required: false,
                        maxlength: 20
                    },
                    simFlow: {
                        required: false,
                        maxlength: 20
                    },
                    useFlow: {
                        maxlength: 20
                    },
                    alertsFlow: {
                        required: false,
                        maxlength: 20
                    },
                    endTime: {
                        required: false,
                        compareDate: "#openCardTime"
                    },
                    correctionCoefficient: {
                        required: false,
                        isRightNumber: true,
                        isInt1tov: 200,
                    },
                    forewarningCoefficient: {
                        required: false,
                        isRightNumber: true,
                        isInt1tov: 200,
                    },
                    monthThresholdValue: {
                        isFloat: true
                    },
                    hourThresholdValue: {
                        range: [0, 6553]
                    },
                    dayThresholdValue: {
                        range: [0, 429496729]
                    },
                    monthThresholdValue: {
                        range: [0, 429496729]
                    },
                    iccid: {
                        required: false,
                        checkICCID: true
                    },
                    imsi: {
                        required: false,
                        maxlength: 50
                    },
                    imei: {
                        required: false,
                        maxlength: 20
                    },
                    dueDate: {
                        compareDate: "#billingDate"
                    },
                    //物品校验
                    thingName: {
                        maxlength: 20
                    },
                    thingModel: {
                        maxlength: 20
                    },
                    thingManufacturer: {
                        maxlength: 20
                    }
                },
                messages: {
                    car_groupName: {
                        required: groupNameNull
                    },
                    vehicleNumber: {
                        required: vehicleBrandNull,
                        maxlength: vehicleNumberMaxlength
                    },
                    vehicleOwner: {
                        checkPeopleName: "只能输入最多8位的中英文字符"
                    },
                    vehicleOwnerPhone: {
                        isLandline: telPhoneError
                    },
                    aliases: {
                        maxlength: vehicleAlisasMaxlength
                    },
                    vehicleType: {
                        required: '请选择车辆类型'
                    },
                    chassisNumber: {
                        required: vehiclChassisNumberNull,
                        maxlength: vehicleChassisMaxlength
                    },
                    engineNumber: {
                        required: vehicleEngineNumber,
                        maxlength: vehicleEngineNumberMaxlength
                    },
                    plateColor: {
                        required: vehiclePlateColorNull
                    },
                    areaAttribute: {
                        required: vehicleAreaAttributeNull
                    },
                    city: {
                        required: vehicleCityNull
                    },
                    province: {
                        required: vehicleProvinceNull
                    },
                    fuelType: {
                        required: vehicleFuelTypeNull
                    },
                    citySel: {
                        required: groupNameNull
                    },
                    // 人员验证
                    peo_groupName: {
                        required: groupNameNull
                    },
                    name: {
                        checkPeopleName: "只能输入最多8位的中英文字符"
                    },
                    identity: {
                        isIdCardNo: "请输入正确的身份证号",
                    },
                    phone: {
                        isTel: telPhoneError
                    },
                    email: {
                        email: "邮件格式错误",
                        maxlength: publicSize20
                    },
                    // 终端验证
                    device_groupName: {
                        required: deviceGroupNameNul
                    },
                    deviceName: {
                        required: deviceNumberNull,
                        maxlength: deviceNameMaxlength
                    },
                    deviceType: {
                        required: deviceTypeNull
                    },
                    functionalType: {
                        required: "功能类型不能为空"
                    },
                    isVideo: {
                        required: deviceIsVideoNull
                    },
                    barCode: {
                        required: deviceBarCodeNull,
                        maxlength: deviceBarCodeMaxlength
                    },
                    channelNumber: {
                        required: deviceChannelNumberNull
                    },
                    isStart: {
                        required: deviceIsStartNull
                    },
                    manuFacturer: {
                        required: deviceManuFacturerNull,
                        maxlength: deviceManuFacturerMaxlength
                    },
                    // sim验证
                    sim_groupName: {
                        required: simNumberNull
                    },
                    isStart: {
                        required: "不能为空",
                        maxlength: "长度不超过6位"
                    },
                    operator: {
                        required: "不能为空",
                        maxlength: "长度不超过50位"
                    },
                    openCardTime: {
                        required: "不能为空",
                    },
                    capacity: {
                        required: "不能为空",
                        maxlength: "长度不超过20位"
                    },
                    simFlow: {
                        required: "不能为空",
                        maxlength: "长度不超过20位"
                    },
                    useFlow: {
                        maxlength: "长度不超过20位"
                    },
                    alertsFlow: {
                        required: "不能为空",
                        maxlength: "长度不超过20位"
                    },
                    endTime: {
                        required: "不能为空",
                        compareDate: "到期时间要大于激活日期"
                    },
                    forewarningCoefficient: {
                        isRightNumber: "请输入正整数",
                        isInt1tov: "超过了系数范围，请输入1到200的整数",
                    },
                    monthThresholdValue: {
                        isFloat: "请输入数字"
                    },
                    correctionCoefficient: {
                        isRightNumber: "请输入正整数",
                        isInt1tov: "超过了系数范围，请输入1到200的整数",
                    },
                    hourThresholdValue: {
                        range: "输入的数字必须在0-6553之间"
                    },
                    dayThresholdValue: {
                        range: "输入的数字必须在0-429496729之间"
                    },
                    monthThresholdValue: {
                        range: "输入的数字必须在0-429496729之间"
                    },
                    iccid: {
                        },
                    imsi: {
                        maxlength: "长度不超过50位"
                    },
                    imei: {
                        maxlength: "长度不超过20位"
                    },
                    //物品校验
                    thingName: {
                        maxlength: publicSize20
                    },
                    thingModel: {
                        maxlength: publicSize20
                    },
                    thingManufacturer: {
                        maxlength: publicSize20
                    }
                }
            }).form();
        },
        //车辆验证上一步、下一步和取消
        nextBtnBrand: function () {
            $("#brandNumber").text($("#brands").val());
            $("#people_Number").text($("#brands").val());
            $("#thing_Number").text($("#brands").val());
            processInput.monitoringObjToShow();
            if ($("#addNew").is(":hidden")) {
                if (objType == 0) { // 车
                    if ((!flag1 && !processInput.check_brand()) || !processInput.checkRightBrand("brands")
                        || processInput.checkIsBound("brands", $("#brands").val()) || !processInput.validates()) {
                        return;
                    } else {
                        $("#vehicleType_show").text($("#vehicleType").find("option:selected").text());
                    }
                }
                if (objType == 1) { // 人
                    if ((!flag1 && !processInput.check_people_number()) || !processInput.checkRightPeopleNumber()
                        || processInput.checkIsBound("brands", $("#brands").val()) || !processInput.validates()) {
                        return;
                    } else {
                        $("#peopleGender").text($("#gender").find("option:selected").text());
                    }
                }
                if (objType == 2) { // 物
                    if ((!flag1 && !processInput.check_thing()) || !processInput.checkRightBrand("brands")
                        || processInput.checkIsBound("brands", $("#brands").val()) || !processInput.validates()) {
                        return;
                    } else {
                        $("#thing_type_value").text($("#thing_type").text());
                        $("#thing_name1").text($("#thing_name").text());
                    }
                }
            } else {
                if (objType == 0) {//车
                    if ((!flag1 && !processInput.check_brand()) || !processInput.checkRightBrand("brands")
                        || processInput.checkIsBound("brands", $("#brands").val()) || !processInput.validates()) {
                        return;
                    } else {
                        $("#vehicleType_show").text($("#vehicleType").find("option:selected").text());
                        datas = $('#brandVal').val();
                        json_ajax("POST", "/clbs/m/infoconfig/infoinput/getVehicleInfoById", "json", true, {"vehicleId": datas}, processInput.carCallback);
                    }
                }
                if (objType == 1) { // 人
                    if ((!flag1 && !processInput.check_people_number()) || !processInput.checkRightPeopleNumber()
                        || processInput.checkIsBound("brands", $("#brands").val()) || !processInput.validates()) {
                        return;
                    } else {
                        $("#peopleGender").text($("#gender").find("option:selected").text());
                        datas = $('#brandVal').val();
                        json_ajax("POST", "/clbs/m/infoconfig/infoinput/getPeopleInfoById", "json", true, {"peopleId": datas}, processInput.peopleCallback);
                    }
                }
                if (objType == 2) { // 物
                    if ((!flag1 && !processInput.check_thing()) || !processInput.checkRightBrand("brands")
                        || processInput.checkIsBound("brands", $("#brands").val()) || !processInput.validates()) {
                        return;
                    } else {
                        $("#thing_type_value").text($("#thingType").find("option:selected").text());
                        $("#thing_name1").text($("#thingName").val());
                        datas = $('#brandVal').val();
                        json_ajax("POST", "/clbs/m/infoconfig/infoinput/getThingInfoById", "json", true, {"thingId": datas}, processInput.thingCallback);
                    }
                }
            }
            $(this).parents(".step-pane").removeClass("active").next().addClass("active");
            $(this).parents(".step-content").siblings(".wizard").children(".steps").children(".active").attr("class", "complete").next().addClass("active").children(".badge").attr("class", "badge badge-info");
            $(".complete").children(".badge").attr("class", "badge badge-success");

            /*if (objType == 0) { // 车
                if ($("#addNew").is(":hidden")) {
                    if ((!flag1 && !processInput.check_brand()) || !processInput.checkRightBrand("brands")
                        || processInput.checkIsBound("brands", $("#brands").val()) || !processInput.validates()) {
                        return;
                    } else {
                        $("#vehicleType_show").text($("#vehicleType").find("option:selected").text());
                        $(this).parents(".step-pane").removeClass("active").next().addClass("active");
                        $(this).parents(".step-content").siblings(".wizard").children(".steps").children(".active").attr("class", "complete").next().addClass("active").children(".badge").attr("class", "badge badge-info");
                        $(".complete").children(".badge").attr("class", "badge badge-success");
                    }
                } else {
                    if ((!flag1 && !processInput.check_brand()) || !processInput.checkRightBrand("brands")
                        || processInput.checkIsBound("brands", $("#brands").val()) || !processInput.validates()) {
                        return;
                    } else {
                        $("#vehicleType_show").text($("#vehicleType").find("option:selected").text());
                        datas = $('#brandVal').val();
                        json_ajax("POST", "/clbs/m/infoconfig/infoinput/getVehicleInfoById", "json", true, {"vehicleId": datas}, processInput.carCallback);
                        $(this).parents(".step-pane").removeClass("active").next().addClass("active");
                        $(this).parents(".step-content").siblings(".wizard").children(".steps").children(".active").attr("class", "complete").next().addClass("active").children(".badge").attr("class", "badge badge-info");
                        $(".complete").children(".badge").attr("class", "badge badge-success");
                    }
                }
            } else if (objType == 1) { // 人
                if ($("#addNew").is(":hidden")) {
                    if ((!flag1 && !processInput.check_people_number()) || !processInput.checkRightPeopleNumber()
                        || processInput.checkIsBound("brands", $("#brands").val()) || !processInput.validates()) {
                        return;
                    } else {
                        $("#peopleGender").text($("#gender").find("option:selected").text());
                        $(this).parents(".step-pane").removeClass("active").next().addClass("active");
                        $(this).parents(".step-content").siblings(".wizard").children(".steps").children(".active").attr("class", "complete").next().addClass("active").children(".badge").attr("class", "badge badge-info");
                        $(".complete").children(".badge").attr("class", "badge badge-success");
                    }
                } else {
                    if ((!flag1 && !processInput.check_people_number()) || !processInput.checkRightPeopleNumber()
                        || processInput.checkIsBound("brands", $("#brands").val()) || !processInput.validates()) {
                        return;
                    } else {
                        $("#peopleGender").text($("#gender").find("option:selected").text());
                        datas = $('#brandVal').val();
                        json_ajax("POST", "/clbs/m/infoconfig/infoinput/getPeopleInfoById", "json", true, {"peopleId": datas}, processInput.peopleCallback);
                        $(this).parents(".step-pane").removeClass("active").next().addClass("active");
                        $(this).parents(".step-content").siblings(".wizard").children(".steps").children(".active").attr("class", "complete").next().addClass("active").children(".badge").attr("class", "badge badge-info");
                        $(".complete").children(".badge").attr("class", "badge badge-success");
                    }
                }
            } else if (objType == 2) { // 物
                if ($("#addNew").is(":hidden")) {
                    if ((!flag1 && !processInput.check_thing()) || !processInput.checkRightBrand("brands")
                        || processInput.checkIsBound("brands", $("#brands").val()) || !processInput.validates()) {
                        return;
                    } else {
                        $("#thing_type").text($("#thingType").find("option:selected").text());
                        $(this).parents(".step-pane").removeClass("active").next().addClass("active");
                        $(this).parents(".step-content").siblings(".wizard").children(".steps").children(".active").attr("class", "complete").next().addClass("active").children(".badge").attr("class", "badge badge-info");
                        $(".complete").children(".badge").attr("class", "badge badge-success");
                    }
                } else {
                    if ((!flag1 && !processInput.check_thing()) || !processInput.checkRightBrand("brands")
                        || processInput.checkIsBound("brands", $("#brands").val()) || !processInput.validates()) {
                        return;
                    } else {
                        datas = $('#brandVal').val();
                        $("#thing_type").text($("#thingType").find("option:selected").text());
                        json_ajax("POST", "/clbs/m/infoconfig/infoinput/getThingInfoById", "json", true, {"thingId": datas}, processInput.thingCallback);
                        $(this).parents(".step-pane").removeClass("active").next().addClass("active");
                        $(this).parents(".step-content").siblings(".wizard").children(".steps").children(".active").attr("class", "complete").next().addClass("active").children(".badge").attr("class", "badge badge-info");
                        $(".complete").children(".badge").attr("class", "badge badge-success");
                    }
                }
            }*/
        },
        carCallback: function (data) {
            if (data != null && data.obj != null && data.obj.vehicleInfo != null) {
                $("#vehicleOwner").text(processInput.converterToBlank(data.obj.vehicleInfo.vehicleOwner));
                $("#vehicleOwnerPhone").text(processInput.converterToBlank(data.obj.vehicleInfo.vehicleOwnerPhone));
                $("#vehicleType_show").text(processInput.converterToBlank(data.obj.vehicleInfo.vehiclet));
            }
        },
        peopleCallback: function (data) {
            if (data != null && data.obj != null && data.obj.peopleInfo != null) {
                $("#peopleIdentity").text(processInput.converterToBlank(data.obj.peopleInfo.identity));
                var checkGender = data.obj.peopleInfo.gender;
                if (checkGender == "1") {
                    checkGender = "男";
                } else if (checkGender == "2") {
                    checkGender = "女";
                } else {
                    return "";
                }
                $("#peopleGender").text(processInput.converterToBlank(checkGender));
                $("#peoplePhone").text(processInput.converterToBlank(data.obj.peopleInfo.phone));
            }
        },
        thingCallback: function (data) {
            if (data != null && data.obj != null && data.obj.thingInfo != null) {
                var thing = data.obj.thingInfo;
                $("#thing_groupName").text(processInput.converterNullToBlank(thing.groupName));
                $("#thing_name").text(processInput.converterNullToBlank(thing.name));
                $("#thing_type").text(processInput.converterNullToBlank(thing.type));
            }
        },
        peoples: function () {
            return $("#addForm2").validate({
                debug: true,
                rules: {
                    peoples: {
                        required: true,
                    },
                    name: {
                        required: true,
                        minlength: 2,
                        maxlength: 30
                    },
                    birthday: {
                        required: true,
                    },
                    identity: {
                        required: true,
                        isIdCardNo: true
                    },
                    gender: {
                        required: true
                    },
                    phone: {
                        required: true,
                        isPhone: true
                    },
                    email: {
                        email: true,
                        maxlength: 20
                    }
                },
                messages: {
                    peoples: {
                        required: "不能为空",
                    },
                    name: {
                        required: "不能为空",
                        minlength: "至少两个字符",
                        maxlength: "最多30个字符"
                    },
                    birthday: "不能为空",
                    identity: {
                        required: "不能为空",
                        isIdCardNo: "请输入正确的身份证号码！"
                    },
                    phone: {
                        required: "不能为空",
                        isPhone: "请输入正确的电话号码！"
                    },
                    email: {
                        email: emailError,
                        maxlength: publicSize20
                    }
                }
            }).form();
        },
        //人员验证上一步、下一步和取消
        nextBtnPeople: function () {
            if (processInput.peoples()) {
                $(this).parents(".step-pane").removeClass("active").next().addClass("active");
                $(this).parents(".step-content").siblings(".wizard").children().children(".active").attr("class", "complete").next().addClass("active").children(".badge").attr("class", "badge badge-info");
                $(".complete").children(".badge").attr("class", "badge badge-success");
            }
        },
        things: function () {
            return $("#addForm3").validate({
                debug: true,
                rules: {
                    things: {
                        required: true,
                    },
                    name: {
                        required: true,
                    },
                    thingNumber: {
                        required: true,
                        maxlength: 20
                    },
                    weight: {
                        required: true,
                        maxlength: 20
                    },
                    volume: {
                        required: true
                    }
                },
                messages: {
                    things: {
                        required: "不能为空"
                    },
                    name: "不能为空",
                    thingNumber: "不能为空",
                    weight: "不能为空"
                }
            }).form();
        },
        //物品验证上一步、下一步和取消
        nextBtnThing: function () {
            if (processInput.things()) {
                $(this).parents(".step-pane").removeClass("active").next().addClass("active");
                $(this).parents(".step-content").siblings(".wizard").children().children(".active").attr("class", "complete").next().addClass("active").children(".badge").attr("class", "badge badge-info");
                $(".complete").children(".badge").attr("class", "badge badge-success");
            }
        },
        //组织验证上一步、下一步和取消
        nextBtnGroup: function () {
            var groupids = '';
            var groupnames = '';
            if (processInput.validates()) {

                var groups = $("#step2").find("input[type='text']");
                groups.each(function (i) {
                    groupids += $(this).attr("value") + ";"
                    if ($(this).val() != "") {
                        groupnames += $(this).val() + "（" + $(this).siblings(".groupPid").val() + "）" + "<br/>";
                    }
                });
                if (groupids.length > 0) groupids = groupids.substr(0, groupids.length - 1);
                $("#citySelidVal").val(groupids);
                $("#comName").html(groupnames);
                $(this).parents(".step-pane").removeClass("active").next().addClass("active");
                $(this).parents(".step-content").siblings(".wizard").children().children(".active").attr("class", "complete").next().addClass("active").children(".badge").attr("class", "badge badge-info");
                $(".complete").children(".badge").attr("class", "badge badge-success");
            }
        },
        //终端验证上一步、下一步和取消
        nextBtnDevice: function () {
            $("#deviceNumber").text($("#devices").val());
            processInput.terminalToShow();
            if ($("#addNew2").is(":hidden")) {
                if ((!flag2 && !processInput.check_device())
                    || !processInput.checkIsEmpty("deviceTypeIn", deviceNumberSelect) || !processInput.checkRightDevice()
                    || processInput.checkIsBound("devices", $("#devices").val())
                    || !processInput.validates()) {
                    return;
                }
                $("#deviceType").text($('#devices').val());
                $("#functionalType").text($("#functionalTypeIn").find("option:selected").text());
                $(this).parents(".step-pane").removeClass("active").next().addClass("active");
                $(this).parents(".step-content").siblings(".wizard").children().children(".active").attr("class", "complete").next().addClass("active").children(".badge").attr("class", "badge badge-info");
                $(".complete").children(".badge").attr("class", "badge badge-success");
            } else {
                if ((!flag2 && !processInput.check_device())
                    || !processInput.checkIsEmpty("deviceTypeIn", deviceNumberSelect) || !processInput.checkRightDevice()
                    || processInput.checkIsBound("devices", $("#devices").val())
                    || !processInput.validates()) {
                    return;
                }
                $("#deviceType").text($("#deviceTypeIn").find("option:selected").text());
                $("#functionalType").text($("#functionalTypeIn").find("option:selected").text());
                datas = $('#devices').val();
                json_ajax("POST", "/clbs/m/infoconfig/infoinput/getDeviceInfoByDeviceNumber", "json", true, {"deviceNumber": datas}, processInput.terminalCallback);
                $(this).parents(".step-pane").removeClass("active").next().addClass("active");
                $(this).parents(".step-content").siblings(".wizard").children().children(".active").attr("class", "complete").next().addClass("active").children(".badge").attr("class", "badge badge-info");
                $(".complete").children(".badge").attr("class", "badge badge-success");

            }
        },
        terminalCallback: function (data) {
            if (data != null && data.obj != null && data.obj.deviceInfo != null) {
                $("#deviceNumber").text(processInput.converterToBlank(data.obj.deviceInfo.deviceNumber));
                $("#deviceName").text(processInput.converterToBlank(data.obj.deviceInfo.deviceName));
                $("#deviceType").text(processInput.getDeviceTypeValue(processInput.converterToBlank(data.obj.deviceInfo.deviceType)));
                $("#functionalType").text(processInput.getFunctionalTypeValue(processInput.converterToBlank(data.obj.deviceInfo.functionalType)));
                $("#manuFacturer").text(processInput.converterToBlank(data.obj.deviceInfo.manuFacturer));
            }
        },
        //Sim验证上一步、下一步和取消
        nextBtnSim: function () {
            processInput.SIMToShow();
            if ($("#addNew3").is(":hidden")) {
                if ((!flag3 && !processInput.check_sim())
                    || !processInput.checkIsEmpty("sims", simNumberNull) || !processInput.checkRightSim()
                    || processInput.checkIsBound("sims", $("#sims").val())
                    || !processInput.validates()) {
                    return;
                } else {
                    $("#simcardNumber").text($("#sims").val());
                    $("#iccid_show").text($("#iccid").text());
                    $("#groupName_show").text($("#simcard_groupName").text());
                    $("#operator").text($("#simcard_operator").text());
                    $(this).parents(".step-pane").removeClass("active").next().addClass("active");
                    $(this).parents(".step-content").siblings(".wizard").children().children(".active").attr("class", "complete").next().addClass("active").children(".badge").attr("class", "badge badge-info");
                    $(".complete").children(".badge").attr("class", "badge badge-success");
                }
            } else {
                if ((!flag3 && !processInput.check_sim())
                    || !processInput.checkIsEmpty("sims", simNumberNull) || !processInput.checkRightSim()
                    || processInput.checkIsBound("sims", $("#sims").val())
                    || !processInput.validates()) {
                    return;
                } else {
                    var data_id = $("#sims").attr("data-id");
                    if (data_id != undefined && data_id != "") { // 是选择的终端手机号
                        $("#simcardNumber").text($("#sims").val());
                        $("#iccid_show").text($("#iccid").text());
                        $("#groupName_show").text($("#simcard_groupName").text());
                        $("#operator").text($("#simcard_operator").text());
                    } else { // 新增终端手机号
                        $("#simcardNumber").text($("#sims").val());
                        $("#iccid_show").text($("#iccid_add").val());
                        $("#groupName_show").text($("#sim_groupName").val());
                        $("#operator").text($("#operatorIn").find("option:selected").text());
                    }
                    $(this).parents(".step-pane").removeClass("active").next().addClass("active");
                    $(this).parents(".step-content").siblings(".wizard").children().children(".active").attr("class", "complete").next().addClass("active").children(".badge").attr("class", "badge badge-info");
                    $(".complete").children(".badge").attr("class", "badge badge-success");
                }
            }
        },
        //外设验证
        nextBtnPeripheral: function () {
            var peripherals = $("#select2");
            var spans = peripherals.find("span");
            var pValue = "";
            var li = "";
            if (spans != null && spans.length > 0) {
                for (var i = 0; i < spans.length; i++) {
                    pValue += peripherals.find("span:eq(" + i + ")").text() + ",";
                    li += "<li>" + peripherals.find("span:eq(" + i + ")").text() + "</li>";
                }
            }
            if (pValue.length > 0)
                pValue = pValue.substr(0, pValue.length - 1);
            $("#peripheralsId").val(pValue);
            var ulHtml = $(".object-area-select");
            ulHtml.html(li);
            $(this).parents(".step-pane").removeClass("active").next().addClass("active");
            $(this).parents(".step-content").siblings(".wizard").children().children(".active").attr("class", "complete").next().addClass("active").children(".badge").attr("class", "badge badge-info");
            $(".complete").children(".badge").attr("class", "badge badge-success");
        },
        //日期验证上一步、下一步和取消
        nextBtnData: function () {
            var timeInterval = $("#timeInterval").val();
            var serveData = timeInterval.split("--");
            var sTime = serveData[0].substr(0, 10);
            var eTime = serveData[1].substr(0, 10);
            $("#billingDate").val(sTime);
            $("#dueDate").val(eTime);
            if (processInput.validates()) {
                $("#jiData").text($("#billingDate").val());
                $("#daoData").text($("#dueDate").val());
                if (objType == 0) {
                    $(this).parents(".step-pane").removeClass("active").next().addClass("active");
                    $(this).parents(".step-content").siblings(".wizard").children().children(".active").attr("class", "complete").next().addClass("active").children(".badge").attr("class", "badge badge-info");
                    $(".complete").children(".badge").attr("class", "badge badge-success");
                } else if (objType == 1 || objType == 2) {
                    $(this).parents(".step-pane").removeClass("active");
                    $(this).parents(".step-content").siblings(".wizard").children().children(".active").attr("class", "complete")
                    $(".complete").children(".badge").attr("class", "badge badge-success");
                    $("#allShowList").addClass('active').children('span.badge').attr('class', 'badge badge-info');
                    $("#step7").addClass("active");
                }
            }
        },
        //从业人员
        nextBtnPeoList: function () {
            var inputList = $("#step6 input[name='professionals']");
            var peopleList = [];
            if (inputList != null && inputList.length > 0) {
                for (var i = 0; i < inputList.length; i++) {
                    if (inputList[i].value != "") {
                        peopleList.push(inputList[i].value);
                    }
                }
            }
            //判断第一个从业人员是否有值，没有则将其name置空，字段不提交
            if ($("#professionals").val() == '') {
                $("#professionals").attr("name", '');
                $("#professionalsID").attr("name", '');
            }
            var html = "";
            if (peopleList.length > 0 && peopleList != null) {
                for (var i = 0; i < peopleList.length; i++) {
                    html += '<div><label class="col-sm-3 col-md-3 control-label conLabLeft">从业人员：</label><div class="col-sm-9 col-md-9 textShow"><span>' + peopleList[i] + '</span></div></div>';
                }
                ;
            }
            $("#showPeopleList").html(html);
            $(this).parents(".step-pane").removeClass("active").next().addClass("active");
            $(this).parents(".step-content").siblings(".wizard").children().children(".active").attr("class", "complete").next().addClass("active").children(".badge").attr("class", "badge badge-info");
            $(".complete").children(".badge").attr("class", "badge badge-success");
        },
        commits: function () {
            return $("#addForm8").validate({
                rules: {
                    professionals: {
                        required: true,
                    }
                },
                messages: {
                    professionals: {
                        required: "不能为空"
                    }
                }
            }).form();
        },
        upBtn: function () {
            //点击上一步时将第一个从业人员的name值还原
            $("#professionals").attr("name", 'professionals');
            $("#professionalsID").attr("name", 'professionalsID');
            if (objType == 0) {
                $(this).parents(".step-pane").removeClass("active").prev().addClass("active");
                $(this).parents(".step-content").siblings(".wizard").children(".steps").children(".active").removeClass("active").prev().attr("class", "active").children(".badge").attr("class", "badge badge-info");
                $(".steps").children(".active").next().children(".badge").removeClass("badge-info");
            } else if (objType == 1 || objType == 2) {
                var $this = $(this).parents(".step-content").siblings(".wizard").children(".steps").children(".active");
                if ($this.prev().hasClass('cancelChoose')) {
                    $this.removeClass("active").prev().prev().attr("class", "active").children(".badge").attr("class", "badge badge-info");
                    $(".steps").children(".active").next().next().children(".badge").removeClass("badge-info");
                    $(this).parents(".step-pane").removeClass("active").prev().prev().addClass("active");
                } else {
                    $(this).parents(".step-pane").removeClass("active").prev().addClass("active");
                    $(this).parents(".step-content").siblings(".wizard").children(".steps").children(".active").removeClass("active").prev().attr("class", "active").children(".badge").attr("class", "badge badge-info");
                    $(".steps").children(".active").next().children(".badge").removeClass("badge-info");
                }
            }
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            if (responseData) {
                for (var i = 0; i < responseData.length; i++) {
                    responseData[i].open = true;
                }
            }
            return responseData;
        },
        beforeClick: function (treeId, treeNode) {
            var check = (treeNode);
            return check;
        },
        onClick: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("ztreeDemo"),
                nodes = zTree.getSelectedNodes(),
                v = "";
            var t = "";
            nodes.sort(function compare(a, b) {
                return a.id - b.id;
            });
            for (var i = 0, l = nodes.length; i < l; i++) {
                t += nodes[i].name;
                v += nodes[i].uuid + ",";
            }
            if (v.length > 0) v = v.substring(0, v.length - 1);
            var cityObj = $("#" + cid);
            cityObj.attr("value", v);
            cityObj.val(t);
            cityObj.siblings().val(v);
            processInput.getCurGroupDetail(v, cid, cityObj.attr("id"));
            $("#menuContent").hide();
        },
        // 分组下拉点击事件
        onClick1: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("ztreeDemo1"),
                nodes = zTree.getSelectedNodes(),
                v = "";
            var t = "";
            var pName = "";
            nodes.sort(function compare(a, b) {
                return a.id - b.id;
            });

            var type = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == "assignment") { // 选择的是分组，才组装值
                    type = nodes[i].type;
                    t += nodes[i].name;
                    v += nodes[i].id + ",";
                    pName += nodes[i].pName;
                }
            }
            if (v.length > 0) v = v.substring(0, v.length - 1);
            // 校验是否重复选择了分组; 校验当前分组下面的车辆数是否已经达到上限
            if (type == "assignment" && processInput.checkDoubleChooseAssignment(v) && processInput.checkMaxVehicleCountOfAssignment(v, t)) { // 点击的是分组，才往下执行
                var cityObj = $("#" + cid);
                cityObj.attr("value", v);
                cityObj.val(t);
                cityObj.siblings('input').val(pName);
                processInput.getCurGroupDetail(v, cid, cityObj.attr("id"));
                $("#menuContent").hide();
                processInput.clearErrorMsg();
            }
        },
        // 校验是否重复选择了分组
        checkDoubleChooseAssignment: function (curValue) {
            var model = $("#groupingArea");
            var added = $("#addGrouping");
            var flag = true;
            if (model != null && model != undefined && model != 'undefined' && model.length > 0) {
                //新增 第一个分组下拉框的value值
                var attr = model.children("div").children("div").children("input:first-child").attr("value");
                //原有获取第一个分组下拉框的value值位置不对,新增或者条件判断
                if (curValue == model.children("div").children("input:first-child").attr("value") || curValue == attr) {
                    layer.msg(repeateChooseAssignment, {
                        time: 1500,
                    });
                    flag = false;
                }
            }
            if (added != null && added != undefined && added != 'undefined' && added.length > 0) {
                added.children(".added_group").each(function (i) {
                    if (curValue == $(this).children("div").children("div").children("input:first-child").attr("value")) {
                        layer.msg(repeateChooseAssignment, {
                            time: 1500,
                        });
                        flag = false;
                    }
                });
            }
            return flag;
        },
        // 校验当前分组下的车辆数是否已经达到最大值（最大值目前设定为：100台）
        checkMaxVehicleCountOfAssignment: function (assignmentId, assignmentName) {
            var b = true;
            $.ajax({
                type: 'POST',
                url: '/clbs/m/infoconfig/infoinput/checkMaxVehicleCount',
                data: {"assignmentId": assignmentId, "assignmentName": assignmentName},
                dataType: 'json',
                async: false,
                success: function (data) {
                    b = data.obj.success;
                    if (!data.obj.success) {
                        layer.msg("【分组:" + assignmentName + "】" + assignmentMaxCarNum, {
                            time: 1500,
                        });
                    }
                },
                error: function () {
                    layer.msg(systemError, {
                        time: 1500,
                    });
                    b = false;
                }
            });
            return b;
        },
        // 获取当前点击组织的详情
        getCurGroupDetail: function (curId, curInputId, clickInputId) {
            $.ajax({
                type: 'POST',
                url: '/clbs/m/infoconfig/infoinput/getGroupDetail',
                data: {"groupId": curId},
                dataType: 'json',
                success: function (data) {
                    //var detailDiv = $("#" + clickInputId).parent().parent().next();
                    var detailDiv = $("#" + clickInputId).parents('.added_group').next();
                    if (data != null && data.obj != null && data.obj.groupInfo != null) {
                        if (clickInputId == "citySel") { // 模板
                            $("#group_name").text(processInput.converterNullToBlank(data.obj.groupInfo.name));
                            $("#group_parent").text(processInput.converterNullToBlank(data.obj.groupInfo.groupName));
                            $("#group_principal").text(processInput.converterNullToBlank(data.obj.groupInfo.contacts));
                            $("#group_phone").text(processInput.converterNullToBlank(data.obj.groupInfo.telephone));
                        } else { // 新增
                            var spans = detailDiv;
                            spans.find("span:eq(0)").text(processInput.converterNullToBlank(data.obj.groupInfo.name));
                            spans.find("span:eq(1)").text(processInput.converterNullToBlank(data.obj.groupInfo.groupName));
                            spans.find("span:eq(2)").text(processInput.converterNullToBlank(data.obj.groupInfo.contacts));
                            spans.find("span:eq(3)").text(processInput.converterNullToBlank(data.obj.groupInfo.telephone));
                        }
                    }
                },
                error: function () {
                    layer.msg(systemError, {
                        time: 1500,
                    });
                }
            });
        },
        // 克隆组织时，清除详细信息的值
        clearCloneGroupValue: function (clickInputId) {
            var detailDiv = $("#" + clickInputId).parent().parent().next();
            var spans = detailDiv;
            spans.find("span:eq(0)").text("");
            spans.find("span:eq(1)").text("");
            spans.find("span:eq(2)").text("");
            spans.find("span:eq(3)").text("");
            spans.find("span:eq(4)").text("");
            spans.find("span:eq(5)").text("");
        },
        // 根据组织id获取其父组织名称-----无用了
        getGroupParentName: function (groupid) {
            var tempParentName = "";
            var groupidArr = groupid.split(",");
            if (groupidArr != null && groupidArr.length > 0) {
                if (groupidArr.length == 1) {
                    tempParentName = groupidArr[0].split("=")[1];
                } else {
                    tempParentName = groupidArr[1].split("=")[1];
                }
            }
            return tempParentName;
        },
        //将null转成空字符串
        converterNullToBlank: function (nullValue) {
            if (nullValue == null || nullValue == undefined || nullValue == "null" || nullValue == "")
                return "";
            else
                return nullValue;
        },
        // 所属企业下拉框
        showMenu: function (e) {
            $("#ztreeDemo").show();
            $("#ztreeDemo1").hide();
            var v_id = e.id;
            cid = v_id;
            if ($("#menuContent").is(":hidden")) {
                var width = $(e).parent().width();
                $("#menuContent").css("width", width + "px");
                $(window).resize(function () {
                    var width = $(e).parent().width();
                    $("#menuContent").css("width", width + "px");
                })
                $("#menuContent").insertAfter($("#" + cid));
                $("#menuContent").show();
            } else {
                $("#menuContent").is(":hidden");
            }
            $("body").bind("mousedown", processInput.onBodyDown);
        },
        // 分组下拉框
        showMenu1: function (e) {
            $("#ztreeDemo").hide();
            $("#ztreeDemo1").show();
            var v_id = e.id;
            cid = v_id;
            if ($("#menuContent").is(":hidden")) {
                var width = $(e).parent().width();
                $("#menuContent").css("width", width + "px");
                $(window).resize(function () {
                    var width = $(e).parent().width();
                    $("#menuContent").css("width", width + "px");
                })
                $("#menuContent").insertAfter($("#" + cid));
                $("#menuContent").show();
            } else {
                $("#menuContent").is(":hidden");
            }
            $("body").bind("mousedown", processInput.onBodyDown);
        },
        hideMenu: function () {
            $("#menuContent").fadeOut("fast");
            $("body").unbind("mousedown", processInput.onBodyDown);
        },
        onBodyDown: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "menuContent" || $(event.target).parents("#menuContent").length > 0)) {
                processInput.hideMenu();
            }
        },
        doSubmits: function () {
            layer.load(2);
            var str = "";
            if (processInput.checkIsBound("brands", $("#brands").val())) {
                str += "车牌号[" + $("#brands").val() + "], ";
            }
            if (processInput.checkIsBound("devices", $("#devices").val())) {
                str += "终端号[" + $("#devices").val() + "], ";
            }
            if (processInput.checkIsBound("sims", $("#sims").val())) {
                str += "终端手机号[" + $("#sims").val() + "], ";
            }
            if (str.length > 0) {
                str = str.substr(0, str.length - 1);
                layer.closeAll();
                layer.msg("不好意思，你来晚了！【" + str + "】已被别人抢先一步绑定了");
                return;
            }
            // sim卡默认启用
            let simIsStart = $("input[name='isStart_sim']").val();
            if (simIsStart === undefined || simIsStart == null || simIsStart == "") {
                $("input[name='isStart_sim']").val(1)
            }
            $("#addForm1").ajaxSubmit(function (message) {
                var json = eval("(" + message + ")");
                if (json.success) {
                    history.back();
                } else {
                    layer.closeAll('loading');
                    layer.msg(json.msg);
                }
            });
        },
        submits: function () {
            var str = "";
            if (processInput.checkIsBound("brands", $("#brands").val())) {
                str += "车牌号[" + $("#brands").val() + "], ";
            }
            if (processInput.checkIsBound("devices", $("#devices").val())) {
                str += "终端号[" + $("#devices").val() + "], ";
            }
            if (processInput.checkIsBound("sims", $("#sims").val())) {
                str += "终端手机号[" + $("#sims").val() + "], ";
            }
            if (str.length > 0) {
                str = str.substr(0, str.length - 1);
                layer.msg("不好意思，你来晚了！【" + str + "】已被别人抢先一步绑定了");
                return;
            }
            layer.confirm(confirmMsg, {btn: [confirmSureBtn, confirmCancleBtn]}, processInput.doSubmits);
        },
        wizard: function () {
            var oldClass = $(this).attr("class");
            if (oldClass == "complete") {
                $(this).attr("class", "active");
                $(this).children("span:first-child").attr("class", "badge badge-info");
                $(this).nextAll().removeClass("active");
                $(this).nextAll().removeClass("complete");
                $(this).nextAll().children("span:first-child").attr("class", "badge");
                var num = $(this).children("span:first-child").text();
                var idNum = "step" + num;
                $("#addForm1 div").removeClass("active");
                $("#" + idNum).addClass("active");
            }
            ;
        },
        //新增详情切换公用方法
        switchBtn: function (inputID, tableID, charID, detailID) {
            var value = $("#" + inputID).val();
            //取得当前下拉框里的所有值
            var tableValue = $("#" + tableID).children("table").children("tbody").children("tr").children("td");
            if (tableValue.length == 0) {
                $("#" + detailID).hide();
                $("#" + charID).show();
            } else {
                tableValue.each(function () {
                    if ($(this).text() == value) {
                        $("#" + charID).hide();
                        $("#" + detailID).show();
                    } else {
                        $("#" + detailID).hide();
                        $("#" + charID).show();
                    }
                });
            }
        },
        //IE9 -- setTimeout(监听input内容变化)
        switchBtnIE9: function (inputID, tableID, charID, detailID) {
            var sameFlag = false;
            var interval = setInterval(function () {
                if (intervalFlag == false) {
                    clearInterval(interval);
                }
                ;
                var value = $("#" + inputID).val();
                $("#" + tableID).children("table").children("tbody").children("tr").children("td").each(function () {
                    if ($(this).text() == value) {
                        sameFlag = true;
                    }
                });
                if (sameFlag) {
                    $("#" + charID).hide();
                    $("#" + detailID).show();
                    sameFlag = false;
                } else {
                    $("#" + detailID).hide();
                    $("#" + charID).show();
                }
                ;
            }, 300);
        },
        completionList: function () {
            $("#brandVal").val($(this).text());
            $("#brandVal").attr("value", $(this).attr("value"));
            $("#brands").val($(this).attr("value"));
            $("#queryBrands").addClass("hidden")
        },
        detailToggle: function (property1, property2) {
            $("#" + property1).hide();
            $("." + property2).toggle();
        },
        // 显示错误提示信息
        showErrorMsg: function (msg, inputId) {
            if ($("#error_label").is(":hidden")) {
                $("#error_label").text(msg);
                $("#error_label").insertAfter($("#" + inputId));
                $("#error_label").show();
            } else {
                $("#error_label").is(":hidden");
            }
        },
        hideErrorMsg: function () {
            $("#error_label").hide();
        },
        // 校验车牌号是否已存在
        checkBrand: function () {
            var tempFlag = true;
            $.ajax({
                type: 'POST',
                url: '/clbs/m/basicinfo/monitoring/vehicle/repetition',
                data: {"brand": $("#brands").val()},
                dataType: 'json',
                async: false,
                success: function (data) {
                    if (!data) {
                        processInput.showErrorMsg(vehicleBrandExists, "brands");
                        tempFlag = false;
                    } else {
                        processInput.hideErrorMsg();
                        tempFlag = true;
                    }
                },
                error: function () {
                    layer.msg("车牌号校验异常！", {
                        time: 1500,
                    });
                    tempFlag = false;
                }
            });
            return tempFlag;
        },
        // 校验人员编号是否已存在
        checkPeopleNumber: function () {
            var tempFlag = true;
            $.ajax({
                type: 'POST',
                url: '/clbs/m/basicinfo/monitoring/personnel/repetitionAdd',
                data: {"peopleNumber": $("#brands").val()},
                dataType: 'json',
                async: false,
                success: function (data) {
                    if (!data) {
                        processInput.showErrorMsg(personnelNumberExists, "brands");
                        tempFlag = false;
                    } else {
                        processInput.hideErrorMsg();
                        tempFlag = true;
                    }
                },
                error: function () {
                    layer.msg("人员编号校验异常！", {
                        time: 1500,
                    });
                    tempFlag = false;
                }
            });
            return tempFlag;
        },
        // 校验物品是否已存在
        checkThing: function () {
            var tempFlag = true;
            $.ajax({
                type: 'POST',
                url: '/clbs/m/basicinfo/monitoring/ThingInfo/checkThingNumberSole',
                data: {"thingNumber": $("#brands").val()},
                dataType: 'json',
                async: false,
                success: function (data) {
                    if (!data) {
                        processInput.showErrorMsg(thingExists, "brands");
                        tempFlag = false;
                    } else {
                        processInput.hideErrorMsg();
                        tempFlag = true;
                    }
                },
                error: function () {
                    layer.msg("物品编号校验异常！");
                    tempFlag = false;
                }
            });
            return tempFlag;
        },
        // 校验终端编号是否已存在
        checkDevice: function () {
            var tempFlag = true;
            $.ajax({
                type: 'POST',
                url: '/clbs/m/basicinfo/equipment/device/repetition',
                data: {"deviceNumber": $("#devices").val()},
                dataType: 'json',
                async: false,
                success: function (data) {
                    if (!data) {
                        processInput.showErrorMsg(deviceNumberExists, "devices");
                        tempFlag = false;
                    } else {
                        processInput.hideErrorMsg();
                        tempFlag = true;
                    }
                },
                error: function () {
                    layer.msg("终端号校验异常！", {
                        time: 1500,
                    });
                    tempFlag = false;
                }
            });
            return tempFlag;
        },
        // 校验终端手机号是否已存在
        checkSIM: function () {
            var tempFlag = true;
            $.ajax({
                type: 'POST',
                url: '/clbs/m/basicinfo/equipment/simcard/repetition',
                data: {"simcardNumber": $("#sims").val()},
                dataType: 'json',
                async: false,
                success: function (data) {
                    if (!data) {
                        processInput.showErrorMsg(simNumberExists, "sims");
                        tempFlag = false;
                    } else {
                        processInput.hideErrorMsg();
                        tempFlag = true;
                    }
                },
                error: function () {
                    layer.msg("终端手机号校验异常！", {
                        time: 1500,
                    });
                    tempFlag = false;
                }
            });
            return tempFlag;
        },
        // 清除从业人员信息
        clearValues: function (element) {
            $("#" + element.id).val("");
            $("#" + element.id).next().val("");
            processInput.clearProDetail(element.id);
        },
        // 清除当前选择框的详细信息
        clearProDetail: function (curId) {
            var detailDivId = $("#" + curId).parent().parent().parent().next().attr("id");
            if (curId == "professionals") {
                detailDivId = "peopleMessage";
            }
            var spans = $("#" + detailDivId);
            spans.find("span:eq(0)").text("");
            spans.find("span:eq(1)").text("");
            spans.find("span:eq(2)").text("");
            spans.find("span:eq(3)").text("");
            spans.find("span:eq(4)").text("");
            spans.find("span:eq(5)").text("");
            spans.find("span:eq(6)").text("");
            spans.find("span:eq(7)").text("");
            spans.find("span:eq(8)").text("");
        },
        // 校验是否重复选择从业人员
        checkDoubleChoosePro: function (curProId) {
            var model = $("#workPeople");
            var added = $("#people-add-area");
            var flag = true;
            if (model != null && model != undefined && model != 'undefined' && model.length > 0) {
                if (curProId == model.children("div").children("div").children("input:first-child").next().attr("value")) {
                    layer.msg(repeateChooseProfession, {
                        time: 1500,
                    });
                    flag = false;
                }
            }
            if (added != null && added != undefined && added != 'undefined' && added.length > 0) {
                added.children(".added_group").each(function (i) {
                    if (curProId == $(this).children("div").children("div").children("input:first-child").next().attr("value")) {
                        layer.msg(repeateChooseProfession, {
                            time: 1500,
                        });
                        flag = false;
                    }
                });
            }
            return flag;
        },
        // 设置默认的所属企业值
        setDefaultOrgValue: function (orgId, orgName) {
            $("#car_groupName").attr("value", orgName);
            $("#car_groupId").attr("value", orgId);
            $("#monitoringObjPeopleAdd").attr("value", orgName);
            $("#peo_groupId").attr("value", orgId);
            $("#monitoringObjThingAdd").attr("value", orgName);
            $("#thing_groupId").attr("value", orgId);
            $("#device_pgroupName").attr("value", orgName);
            $("#device_groupId").attr("value", orgId);
            $("#sim_groupName").attr("value", orgName);
            $("#sim_groupId").attr("value", orgId);
        },
        getVehicleInfoDetailById: function (vehicleid) {
            $.ajax({
                type: 'POST',
                url: '/clbs/m/infoconfig/infoinput/getVehicleInfoById',
                data: {
                    "vehicleId": vehicleid
                },
                dataType: 'json',
                success: function (data) {
                    if (data != null && data.obj != null && data.obj.vehicleInfo != null) {
                        $("#vehicle_groupName").text(processInput.converterNullToBlank(data.obj.vehicleInfo.groupName));
                        $("#vehicle_plate_color").text(getPlateColor(data.obj.vehicleInfo.plateColor));
                        $("#vehicle_owner").text(processInput.converterNullToBlank(data.obj.vehicleInfo.vehicleOwner));
                        $("#vehicle_owner_phone").text(processInput.converterNullToBlank(data.obj.vehicleInfo.vehicleOwnerPhone));
                        $("#vehicle_aliases").text(processInput.converterNullToBlank(data.obj.vehicleInfo.aliases));
                        $("#vehicle_vehicle_type").text(processInput.converterNullToBlank(data.obj.vehicleInfo.vehiclet));
                        $("#vehicle_chassis_number").text(processInput.converterNullToBlank(data.obj.vehicleInfo.chassisNumber));
                        $("#vehicle_engine_number").text(processInput.converterNullToBlank(data.obj.vehicleInfo.engineNumber));
                        $("#vehicle_area_attribute").text(processInput.converterNullToBlank(data.obj.vehicleInfo.areaAttribute));
                        $("#vehicle_province").text(processInput.converterNullToBlank(data.obj.vehicleInfo.province));
                        $("#vehicle_city").text(processInput.converterNullToBlank(data.obj.vehicleInfo.city));
                        $("#vehicle_fuel_type").text(processInput.converterNullToBlank(data.obj.vehicleInfo.fuelType));
                    }
                },
                error: function () {
                    layer.msg(systemError, {
                        time: 1500,
                    });
                }
            });
        },
        getPeopleInfoDetailById: function (peopleId) { // 查询人员详情
            $.ajax({
                type: 'POST',
                url: '/clbs/m/basicinfo/monitoring/personnel/getPeopleById',
                data: {
                    "id": peopleId
                },
                dataType: 'json',
                success: function (data) {
                    if (data != null && data.obj != null) {
                        var people = data.obj;
                        $("#peo_number").text(processInput.converterNullToBlank(people.peopleNumber));
                        $("#peo_groupName").text(processInput.converterNullToBlank(people.groupName));
                        $("#peo_name").text(processInput.converterNullToBlank(people.name));
                        $("#peo_identity").text(processInput.converterNullToBlank(people.identity));
                        $("#peo_gender").text(processInput.converterNullToBlank(function () {
                            if (people.gender == "1") {
                                return "男";
                            } else if (people.gender == "2") {
                                return "女";
                            } else {
                                return "";
                            }
                        }));
                        $("#peo_phone").text(processInput.converterNullToBlank(people.phone));
                        $("#peo_email").text(processInput.converterNullToBlank(people.email));
                    }
                },
                error: function () {
                    layer.msg(systemError, {
                        time: 1500,
                    });
                }
            });
        },
        getThingInfoDetailById: function (thingId) { // 查询人员详情
            $.ajax({
                type: 'POST',
                url: '/clbs/m/infoconfig/infoinput/getThingInfoById',
                data: {
                    "thingId": thingId
                },
                dataType: 'json',
                success: function (data) {
                    if (data != null && data.obj != null) {
                        var thing = data.obj.thingInfo;
                        $("#thing_groupName").text(processInput.converterNullToBlank(thing.groupName));
                        $("#thing_name").text(processInput.converterNullToBlank(thing.name));
                        $("#thing_category").text(processInput.converterNullToBlank(thing.categoryName));
                        $("#thing_type").text(processInput.converterNullToBlank(thing.typeName));
                        $("#thing_model").text(processInput.converterNullToBlank(thing.model));
                        $("#thing_manufacturer").text(processInput.converterNullToBlank(thing.manufacture));
                    }
                },
                error: function () {
                    layer.msg(systemError, {
                        time: 1500,
                    });
                }
            });
        },
        getDeviceInfoDetailById: function (deviceId) {
            $.ajax({
                type: 'POST',
                url: '/clbs/m/infoconfig/infoinput/getDeviceInfoDetailById',
                data: {
                    "deviceId": deviceId
                },
                dataType: 'json',
                success: function (data) {
                    if (data != null && data.obj != null && data.obj.deviceInfo != null) {
                        $("#device_groupName").text(processInput.converterNullToBlank(data.obj.deviceInfo.groupName));
                        $("#device_device_name").text(processInput.converterNullToBlank(data.obj.deviceInfo.deviceName));
                        $("#device_is_start").text(processInput.getIsStartValue(data.obj.deviceInfo.isStart));
                        $("#device_device_type").text(processInput.getDeviceTypeValue(data.obj.deviceInfo.deviceType));
                        $("#device_functional_type").text(processInput.getFunctionalTypeValue(data.obj.deviceInfo.functionalType));
                        $("#device_channel_number").text(processInput.getChannelNumberValue(data.obj.deviceInfo.channelNumber));
                        $("#device_is_video").text(processInput.getIsVideoValue(data.obj.deviceInfo.isVideo));
                        $("#device_bar_code").text(processInput.converterNullToBlank(data.obj.deviceInfo.barCode));
                        $("#device_manu_facturer").text(processInput.converterNullToBlank(data.obj.deviceInfo.manuFacturer));
                    }
                },
                error: function () {
                    layer.msg(systemError, {
                        time: 1500,
                    });
                }
            });
        },
        getSimCardInfoDetailById: function (simcardId) {
            $.ajax({
                type: 'POST',
                url: '/clbs/m/infoconfig/infoinput/getSimCardInfoDetailById',
                data: {
                    "simcardId": simcardId
                },
                dataType: 'json',
                success: function (data) {
                    if (data != null && data.obj != null && data.obj.simcardInfo != null) {
                        $("#simcard_groupName").text(processInput.converterNullToBlank(data.obj.simcardInfo.groupName));
                        $("#simcard_is_start").text(processInput.getIsStartValue(data.obj.simcardInfo.isStart));
                        $("#iccid").text(processInput.converterNullToBlank(data.obj.simcardInfo.iCCID));
                        $("#imsi").text(processInput.converterNullToBlank(data.obj.simcardInfo.iMSI));
                        $("#imei").text(processInput.converterNullToBlank(data.obj.simcardInfo.imei));
                        $("#simcard_operator").text(processInput.converterNullToBlank(data.obj.simcardInfo.operator));
                        $("#simcard_network_type").text(processInput.converterNullToBlank(data.obj.simcardInfo.networkType));
                        $("#simcard_open_card_time").text(processInput.converterNullToBlank(data.obj.simcardInfo.openCardTime).length > 10 ? data.obj.simcardInfo.openCardTime.substr(0, 10) : "");
                        $("#simcard_end_time").text(processInput.converterNullToBlank(data.obj.simcardInfo.endTime).length > 10 ? data.obj.simcardInfo.endTime.substr(0, 10) : "");
                        $("#simcard_capacity").text(processInput.converterNullToBlank(data.obj.simcardInfo.capacity));
                        $("#simcard_sim_flow").text(processInput.converterNullToBlank(data.obj.simcardInfo.simFlow));
                        $("#simcard_use_flow").text(processInput.converterNullToBlank(data.obj.simcardInfo.useFlow));
                        $("#simcard_alerts_flow").text(processInput.converterNullToBlank(data.obj.simcardInfo.alertsFlow));
                        $("#correction_coefficient").text(processInput.converterNullToBlank(data.obj.simcardInfo.correctionCoefficient));
                        $("#forewarning_coefficient").text(processInput.converterNullToBlank(data.obj.simcardInfo.forewarningCoefficient));
                        $("#hour_threshold_value").text(processInput.converterNullToBlank(data.obj.simcardInfo.hourThresholdValue));
                        $("#day_threshold_value").text(processInput.converterNullToBlank(data.obj.simcardInfo.dayThresholdValue));
                        $("#month_threshold_value").text(processInput.converterNullToBlank(data.obj.simcardInfo.monthThresholdValue));
                    }
                },
                error: function () {
                    layer.msg(systemError, {
                        time: 1500,
                    });
                }
            });
        },
        getProfessionalDetailById: function (professionalId, type, ele) {
            $.ajax({
                type: 'POST',
                url: '/clbs/m/infoconfig/infoinput/getProfessionalDetailById',
                data: {"professionalId": professionalId},
                dataType: 'json',
                success: function (data) {
                    if (data != null && data.obj != null && data.obj.professionalInfo != null) {
                        if (type == "0") { // 模板
                            $("#people_name").text(processInput.converterNullToBlank(data.obj.professionalInfo.name));
                            $("#people_gender").text(processInput.getGenderValue(data.obj.professionalInfo.gender));
                            $("#people_birthday").text(processInput.converterNullToBlank(data.obj.professionalInfo.birthday).length > 10 ? data.obj.professionalInfo.birthday.substr(0, 10) : "");
                            $("#people_identity").text(processInput.converterNullToBlank(data.obj.professionalInfo.identity));
                            $("#people_phone").text(processInput.converterNullToBlank(data.obj.professionalInfo.phone));
                            $("#people_email").text(processInput.converterNullToBlank(data.obj.professionalInfo.email));
                            $("#people_position_type").text(processInput.getPositionTypeValue(data.obj.professionalInfo.positionType));
                            $("#people_job_number").text(processInput.converterNullToBlank(data.obj.professionalInfo.jobNumber));
                            $("#people_card_number").text(processInput.converterNullToBlank(data.obj.professionalInfo.cardNumber));
                        } else { // 后面新增的
                            var messageDivId = $("#" + ele).next().attr("id");
                            var spans = $("#" + messageDivId);
                            spans.find("span:eq(0)").text(processInput.converterNullToBlank(data.obj.professionalInfo.name));
                            spans.find("span:eq(1)").text(processInput.getGenderValue(data.obj.professionalInfo.gender));
                            spans.find("span:eq(2)").text(processInput.converterNullToBlank(data.obj.professionalInfo.birthday).length > 10 ? data.obj.professionalInfo.birthday.substr(0, 10) : "");
                            spans.find("span:eq(3)").text(processInput.converterNullToBlank(data.obj.professionalInfo.identity));
                            spans.find("span:eq(4)").text(processInput.converterNullToBlank(data.obj.professionalInfo.phone));
                            spans.find("span:eq(5)").text(processInput.converterNullToBlank(data.obj.professionalInfo.email));
                            spans.find("span:eq(6)").text(processInput.getPositionTypeValue(data.obj.professionalInfo.positionType));
                            spans.find("span:eq(7)").text(processInput.converterNullToBlank(data.obj.professionalInfo.jobNumber));
                            spans.find("span:eq(8)").text(processInput.converterNullToBlank(data.obj.professionalInfo.cardNumber));
                        }
                    }
                },
                error: function () {
                    layer.msg(systemError, {
                        time: 1500,
                    });
                }
            });
        },
        // 清除克隆从业人员详情的值
        clearCloneProfessionalValue: function (ele) {
            var messageDivId = $("#" + ele).next().attr("id");
            var spans = $("#" + messageDivId);
            spans.find("span:eq(0)").text("");
            spans.find("span:eq(1)").text("");
            spans.find("span:eq(2)").text("");
            spans.find("span:eq(3)").text("");
            spans.find("span:eq(4)").text("");
            spans.find("span:eq(5)").text("");
            spans.find("span:eq(6)").text("");
            spans.find("span:eq(7)").text("");
            spans.find("span:eq(8)").text("");
        },
        // 启停状态
        getIsStartValue: function (isStartIntVal) {
            if (isStartIntVal == 1) {
                return "启用";
            } else if (isStartIntVal == 0) {
                return "停用";
            } else {
                return "启用";
            }
        },
        // 是否视频
        getIsVideoValue: function (isVideoIntVal) {
            if (isVideoIntVal == 1) {
                return "是";
            } else if (isVideoIntVal == 0) {
                return "否";
            } else {
                return "是";
            }
        },
        // 通讯类型
        getDeviceTypeValue: function (deviceTypeIntVal) {
            if (deviceTypeIntVal == 0) {
                return "交通部JT/T808-2011(扩展)";
            } else if (deviceTypeIntVal == 1) {
                return "交通部JT/T808-2013";
            } else if (deviceTypeIntVal == 11) {
                return "交通部JT/T808-2019";
            } else if (deviceTypeIntVal == 2) {
                return "移为";
            } else if (deviceTypeIntVal == 3) {
                return "天禾";
            } else if (deviceTypeIntVal == 5) {
                return "BDTD-SM";
            } else if (deviceTypeIntVal == 6) {
                return "KKS";
            } else if (deviceTypeIntVal == 7) {
                return "";
            } else if (deviceTypeIntVal == 8) {
                return "BSJ-A5";
            } else if (deviceTypeIntVal == 9) {
                return "ASO";
            } else if (deviceTypeIntVal == 10) {
                return "F3超长待机";
            } else {
                return "";
            }
        },
        // 功能类型
        getFunctionalTypeValue: function (functionalTypeIntVal) {
            if (functionalTypeIntVal == 1) {
                return "简易型车机";
            } else if (functionalTypeIntVal == 2) {
                return "行车记录仪";
            } else if (functionalTypeIntVal == 3) {
                return "对讲设备";
            } else if (functionalTypeIntVal == 4) {
                return "手咪设备";
            } else if (functionalTypeIntVal == 5) {
                return "超长待机设备";
            } else if (functionalTypeIntVal == 6) {
                return "定位终端";
            } else {
                return "";
            }
        },
        // 性别
        getGenderValue: function (genderIntVal) {
            if (genderIntVal == 1) {
                return "男";
            } else {
                return "女";
            }
        },
        // 岗位类型
        getPositionTypeValue: function (positionTypeIntVal) {
            var newVal = '';
            var typeQuery = "";
            var url = "/clbs/m/basicinfo/enterprise/professionals/listType";
            var data = {"professionalstype": typeQuery};
            json_ajax("POST", url, "json", false, data, function (data) {
                var result = data.records;
                for (var i = 0; i < result.length; i++) {
                    if (result[i].id == positionTypeIntVal) {
                        newVal = html2Escape(result[i].professionalstype);
                    }
                }
            });
            return newVal;
        },
        // 通道数
        getChannelNumberValue: function (channelNumberIntVal) {
            if (channelNumberIntVal = 1) {
                return "4";
            } else if (channelNumberIntVal = 2) {
                return "5";
            } else if (channelNumberIntVal = 3) {
                return "8";
            } else if (channelNumberIntVal = 4) {
                return "16";
            } else {
                return "";
            }
        },
        // 转换null为空字符串
        converterToBlank: function (value) {
            if (value == null || value == "null" || value == "undefined") {
                return "";
            } else {
                return value + "";
            }
        },
        inputBlur: function () {
            processInput.hideErrorMsg();
            if ($(this).val() != "" && !flag1 && !processInput.check_brand()) {
                return;
            }
        },
        // 校验车辆信息
        check_brand: function () {
            var elementId = "brands";
            // wjk
            var errorMsg1 = '监控对象不能为空';

            if (processInput.checkIsEmpty(elementId, errorMsg1)
                && processInput.checkRightBrand(elementId)
                && processInput.checkBrand()) {
                return true;
            } else {
                return false;
            }
        },
        //不能全是横杠
        checkISdhg: function (elementId) {
            var value = $("#" + elementId).val();
            var regIfAllheng = /^[-]*$/;
            if (regIfAllheng.test(value)) {
                processInput.showErrorMsg('不能全是横杠', elementId);
                return false;
            } else {
                processInput.hideErrorMsg();
                return true;
            }
        },
        // 校验人员信息
        check_people_number: function () {
            var elementId = "brands";
            var maxLength = 8;
            var errorMsg1 = personnelNumberNull;
            var errorMsg2 = publicSize8Length;
            var errorMsg3 = personnelNumberError;
            var errorMsg4 = personnelNumberExists;
            var reg = /^[A-Za-z0-9\u4e00-\u9fa5_-]{2,20}$/;
            if (processInput.checkIsEmpty(elementId, errorMsg1)
                && processInput.checkIsLegal(elementId, reg, null, errorMsg3)
                && processInput.checkPeopleNumber()) {
                return true;
            } else {
                return false;
            }
        },
        // 校验物品信息
        check_thing: function () {
            var elementId = "brands";
            // wjk
            var errorMsg1 = '监控对象不能为空';

            if (processInput.checkIsEmpty(elementId, errorMsg1)
                && processInput.checkRightBrand(elementId)
                && processInput.checkThing()) {
                return true;
            } else {
                return false;
            }
        },
        // 校验车牌号是否填写规范或者回车时不小心输入了异常字符
        checkRightBrand: function (id) {
            // var errorMsg3 = vehicleBrandError;

            // wjk
            var errorMsg3 = '请输入汉字、字母、数字或短横杠，长度2-20位';
            if (checkBrands(id)) {
                processInput.hideErrorMsg();
                return true;
            } else {
                processInput.showErrorMsg(errorMsg3, id);
                return false;
            }
        },
        // 校验人员编号
        checkRightPeopleNumber: function () {
            var reg = /^[A-Za-z0-9\u4e00-\u9fa5_-]{2,20}$/;
            var errorMsg3 = personnelNumberError;
            return processInput.checkIsLegal("brands", reg, null, errorMsg3);
        },
        //校验终端信息
        check_device: function () {
            var elementId = "devices";
            var maxLength = 20;
            var errorMsg1 = deviceNumberSelect;
            var errorMsg2 = deviceNumberMaxlength;
            // var errorMsg3 = deviceNumberError;
            var errorMsg3 = '请输入字母、数字，长度7-20位';
            var errorMsg4 = deviceNumberExists;
            var reg = /^[A-Za-z0-9]{7,20}$/;
            // if (objType == 0) {
            //     // reg = /^[A-Za-z0-9_-]{7,15}$/;
            //     reg = /^[A-Za-z0-9]{7,15}$/;
            // } else {
            //     reg = /^[0-9a-zA-Z]{1,20}$/;
            // }
            if (processInput.checkIsEmpty(elementId, errorMsg1)
                && processInput.checkLength(elementId, maxLength, errorMsg2)
                && processInput.checkIsLegal(elementId, reg, null, errorMsg3)
                && processInput.checkDevice()) {
                return true;
            } else {
                return false;
            }

        },
        // 校验终端是否填写规范或者回车时不小心输入了异常字符
        checkRightDevice: function () {
            var reg =  /^[A-Za-z0-9]{7,20}$/;
            // if (objType == 0) {
            //     // reg = /^[A-Za-z0-9_-]{7,15}$/;
            //     reg = /^[A-Za-z0-9]{7,15}$/;
            // } else {
            //     reg = /^[0-9a-zA-Z]{1,20}$/;
            // }
            var errorMsg3 = deviceNumberError;
            if (processInput.checkIsLegal("devices", reg, null, errorMsg3)) {
                return true;
            } else {
                return false;
            }
        },
        //校验终端手机号信息
        check_sim: function () {
            var elementId = "sims";
            var maxLength = 14;
            var errorMsg1 = simNumberNull;
            var errorMsg2 = simNumberMaxlength;
            var errorMsg3 = simNumberError;
            var errorMsg4 = simNumberExists;
            var reg = /^((13[0-9]{1})|(14[5,7,9]{1})|(15[^4]{1})|(166)|(18[0-9]{1})|(19[8-9]{1})|(17[0,1,3,5,6,7,8]{1}))+\d{8}$/;
            var reg1 = /^((\d{3,4}-\d{7,9})|([1-9]{1}\d{6,12}))$/g;
            if (processInput.checkIsEmpty(elementId, errorMsg1)
                && processInput.checkLength(elementId, maxLength, errorMsg2)
                && processInput.checkIsLegal(elementId, reg, reg1, errorMsg3)
                && processInput.checkSIM()) {
                return true;
            } else {
                return false;
            }
        },
        // 校验终端手机号是否填写规范或者回车时不小心输入了异常字符
        checkRightSim: function () {
            var errorMsg3 = simNumberError;
            var reg = /^((13[0-9]{1})|(14[5,7,9]{1})|(15[^4]{1})|(166)|(18[0-9]{1})|(19[8-9]{1})|(17[0,1,3,5,6,7,8]{1}))+\d{8}$/;
            var reg1 = /^((\d{3,4}-\d{7,9})|([1-9]{1}\d{6,12}))$/g;
            if (processInput.checkIsLegal("sims", reg, reg1, errorMsg3)) {
                return true;
            } else {
                return false;
            }
        },
        // 校验是否为空
        checkIsEmpty: function (elementId, errorMsg) {
            var value = $("#" + elementId).val();
            if (value == "") {
                processInput.hideErrorMsg();
                processInput.showErrorMsg(errorMsg, elementId);
                return false;
            } else {
                processInput.hideErrorMsg();
                return true;
            }
        },
        // 校验是否已存在
        checkIsExists: function (attr, elementId, requestUrl, errorMsg) {
            var tempFlag = true;
            $.ajax({
                type: 'POST',
                url: requestUrl,
                data: {"device": $("#" + elementId).val()},
                dataType: 'json',
                success: function (data) {
                    if (!data) {
                        processInput.showErrorMsg(errorMsg, elementId);
                        tempFlag = false;
                    } else {
                        processInput.hideErrorMsg();
                        tempFlag = true;
                    }
                },
                error: function () {
                    processInput.showErrorMsg("校验异常", elementId);
                    tempFlag = false;
                }
            });
            return tempFlag;
        },
        // 校验是否已被绑定
        checkIsBound: function (elementId, elementValue) {
            var tempFlag = false;
            var url = "/clbs/m/infoconfig/infoinput/checkIsBound";
            var data = "";
            if (elementId == "brands") {
                data = {"monitorType": objType, "inputId": "brands", "inputValue": $("#" + elementId).val()}
            } else if (elementId == "devices") {
                data = {"inputId": "devices", "inputValue": $("#" + elementId).val()}
            } else if (elementId == "sims") {
                data = {"inputId": "sims", "inputValue": $("#" + elementId).val()}
            }
            $.ajax({
                type: 'POST',
                url: url,
                data: data,
                dataType: 'json',
                async: false,
                success: function (data) {
                    if (null != data && data.obj != null && data.obj.isBound) {
                        layer.msg("不好意思，你来晚了！【" + data.obj.boundName + "】已被别人抢先一步绑定了");
                        tempFlag = true;
                    } else {
                        tempFlag = false;
                    }
                },
                error: function () {
                    processInput.showErrorMsg("校验异常", elementId);
                    tempFlag = false;
                }
            });
            return tempFlag;
        },
        // 校验填写数据的合法性
        checkIsLegal: function (elementId, reg, reg1, errorMsg) {
            var value = $("#" + elementId).val();
            if (reg1 != null) {
                if (!reg.test(value) && !reg1.test(value)) {
                    processInput.showErrorMsg(errorMsg, elementId);
                    return false;
                } else {
                    processInput.hideErrorMsg();
                    return true;
                }
            } else {
                if (!reg.test(value)) {
                    processInput.showErrorMsg(errorMsg, elementId);
                    return false;
                } else {
                    processInput.hideErrorMsg();
                    return true;
                }
            }
        },
        // 校验长度
        checkLength: function (elementId, maxLength, errorMsg) {
            var value = $("#" + elementId).val();
            if (value.length > parseInt(maxLength)) {
                processInput.showErrorMsg(errorMsg, elementId);
                return false;
            } else {
                processInput.hideErrorMsg();
                return true;
            }
        },
        // 清除错误信息
        clearErrorMsg: function () {
            $("label.error").hide();
        },
        keydownEvent: function (e) {
            var key = e.which;
            if (key == 13) {
                if (!$("#layui-layer6").is(":hidden") && enterFlag) {
                    $(".layui-layer-btn0").click();
                }
                ;
                var dataTarget
                $('ul.steps li').each(function () {
                    if ($(this).hasClass('active')) {
                        dataTarget = $(this).attr('data-target');
                    }
                    ;
                });
                if (dataTarget == '#step1') {
                    $(".nextBtnBrand").click();
                }
                ;
                if (dataTarget == '#step2') {
                    $(".nextBtnGroup").click();
                }
                ;
                if (dataTarget == '#step3') {
                    $(".nextBtnDevice").click();
                }
                ;
                if (dataTarget == '#step4') {
                    $(".nextBtnSim").click();
                }
                ;
                if (dataTarget == '#step5') {
                    $(".nextBtnData").click();
                }
                ;
                if (dataTarget == '#step6') {
                    $(".nextBtnPeoList").click();
                }
                ;
                if (dataTarget == '#step7' && !enterFlag) {
                    enterFlag = true;
                    $("#submits").click();
                }
                ;
            }
            ;
        },
        objectMonitoring: function () {
            var html = $('#charNumList table').length;
            if (html == 0) {
                return false;
            }
        },
        //监控车
        chooseCar: function () {
            objType = 0;
            processInput.showType(objType);
            $("#brands").val("");
            $("#devices").val("");
            $("#deviceTypeIn").val("");
            $("#functionalTypeIn").val("");
            $("#sims").val("");
            processInput.putConfigValue();
            $("#monitorType").val("0");
            processInput.DeviceChange(objType);
        },
        //监控人
        choosePeople: function () {
            objType = 1;
            processInput.showType(objType);
            $("#brands").val("");
            $("#devices").val("");
            $("#deviceTypeIn").val("");
            $("#functionalTypeIn").val("");
            $("#sims").val("");
            processInput.putConfigValue();
            $("#monitorType").val("1");
            processInput.DeviceChange(objType);
        },
        showType: function (index) {
            if (index == 0) { //车
                $(".monitoringObj-car-details").show();
                $(".monitoringObj-car-add").show();
                $(".monitoringObj-people-details").hide();
                $(".monitoringObj-people-add").hide();
                $(".monitoringObj-thing-details").hide();
                $(".monitoringObj-thing-add").hide();
                $("#personList").removeClass("cancelChoose");
                $("#allShowPeopleArea").show();
                $("#peopleDetails").hide();
                $("#thingDetails").hide();
                $("#carDetails").show();
            } else if (index == 1) { //人
                $(".monitoringObj-car-details").hide();
                $(".monitoringObj-car-add").hide();
                $(".monitoringObj-thing-details").hide();
                $(".monitoringObj-thing-add").hide();
                $(".monitoringObj-people-details").show();
                $(".monitoringObj-people-add").show();
                $("#personList").addClass("cancelChoose");
                $("#allShowPeopleArea").hide();
                $("#professionals").val('');
                $("#professionalsID").val('');
                $("#peopleDetails").show();
                $("#carDetails").hide();
                $("#thingDetails").hide();
            } else if (index == 2) {
                $(".monitoringObj-thing-details").show();
                $(".monitoringObj-thing-add").show();
                $(".monitoringObj-car-add").hide();
                $(".monitoringObj-car-details").hide();
                $(".monitoringObj-people-add").hide();
                $(".monitoringObj-people-details").hide();
                $("#personList").addClass("cancelChoose");
                $("#allShowPeopleArea").hide();
                $("#professionals").val('');
                $("#professionalsID").val('');
                $("#peopleDetails").hide();
                $("#carDetails").hide();
                $("#thingDetails").show();
            }
            $(".detailMessage").hide();
            $("#charBtn").show();
            $("#detailBtn").hide();
            $(".delIcon").hide();
        },
        //
        inputValueChange: function () {
            $("#brands").bind("paste", processInput.inputOnPaste("brands"));
            $("#brands").bind("focus", function () {
                if ($("#brands").val() == "" && flag1) {
                    intervalFlag = true;
                    processInput.switchBtnIE9("brands", "charNumList", "charBtn", "detailBtn");
                    $(".charNew").show(); // 手动填写的值，显示新增模块
                    $(".detailMessage").hide();
                    $("#brandVal").attr("value", "")
                    flag1 = false;
                }
            });
            $("#devices").bind("paste", processInput.inputOnPaste("devices"));
            $("#devices").bind("focus", function () {
                if ($("#devices").val() == "" && flag2) {
                    intervalFlag = true;
                    processInput.switchBtnIE9("devices", "devicesList", "terminalBtn", "devDetailBtn");
                    $(".terminalNew").show(); // 手动填写的值，显示新增模块
                    $(".equipmentMessage").hide();
                    $("#deviceVal").attr("value", "");
                    flag2 = false;
                }
            });
            $("#sims").bind("paste", processInput.inputOnPaste("sims"));
            $("#sims").bind("focus", function () {
                if ($("#sims").val() == "" && flag3) {
                    intervalFlag = true;
                    processInput.switchBtnIE9("sims", "SIMList", "SIMBtn", "SIMDetailBtn");
                    $(".SIMNew").show(); // 手动填写的值，显示新增模块
                    $(".SIMMessage").hide();
                    $("#simVal").attr("value", "")
                    flag3 = false;
                }
            });
            //IE9 不支持 input和propertychange
            if (navigator.appName == "Microsoft Internet Explorer" && navigator.appVersion.split(";")[1].replace(/[ ]/g, "") == "MSIE9.0") {
                $("#brands").bind("focus", function () {
                    intervalFlag = true;
                    processInput.switchBtnIE9("brands", "charNumList", "charBtn", "detailBtn")
                })
                    .bind("blur", function () {
                        intervalFlag = false;
                    });
                $("#devices").bind("focus", function () {
                    intervalFlag = true;
                    processInput.switchBtnIE9("devices", "devicesList", "terminalBtn", "devDetailBtn")
                })
                    .bind("blur", function () {
                        intervalFlag = false;
                    });
                $("#sims").bind("focus", function () {
                    intervalFlag = true;
                    processInput.switchBtnIE9("sims", "SIMList", "SIMBtn", "SIMDetailBtn")
                })
                    .bind("blur", function () {
                        intervalFlag = false;
                    });
            }
            else {
                $("#brands").bind("input propertychange", function () {
                    processInput.switchBtn("brands", "charNumList", "charBtn", "detailBtn")
                });
                $("#devices").bind("input propertychange", function () {
                    processInput.switchBtn("devices", "devicesList", "terminalBtn", "devDetailBtn")
                });
                $("#sims").bind("input propertychange", function () {
                    processInput.switchBtn("sims", "SIMList", "SIMBtn", "SIMDetailBtn")
                });
            }
        },
        //根据监控对象类型改变终端类型及功能类型
        DeviceChange: function (type) {
            /*if (type == 0) {*/
            $("#deviceTypeIn").html(
                '<option value="0">交通部JT/T808-2011(扩展)</option>' +
                '<option value="1" selected>交通部JT/T808-2013</option>' +
                '<option value="11">交通部JT/T808-2019</option>' +
                '<option value="2">移为</option>' +
                '<option value="3">天禾</option>' +
                '<option value="5">BDTD-SM</option>' +
                '<option value="6">KKS</option>' +
                '<option value="8">BSJ-A5</option>' +
                '<option value="9">ASO</option>' +
                '<option value="10">F3超长待机</option>'
            );
            $("#functionalTypeIn").html(
                '<option value="1">简易型车机</option>' +
                '<option value="2">行车记录仪</option>' +
                '<option value="3">对讲设备</option>' +
                '<option value="4">手咪设备</option>' +
                '<option value="5">超长待机设备</option>' +
                '<option value="6">定位终端</option>'
            );
            /*} else {
                $("#deviceTypeIn").html(
                    '<option value="5">BDTD-SM</option>'
                );
                $("#functionalTypeIn").html(
                    '<option value="4">手咪设备</option>'
                );
            }*/
        },
        //监控 --综合展示
        monitoringObjToShow: function () {
            //objType
            if (objType == 0) {
                $("#brandNumber").text($("#brand").val());
                $("#vehicleOwner").text($("#vehicleOwnerIn").val());
                $("#vehicleOwnerPhone").text($("#vehicleOwnerPhoneIn").val());
                $("#vehicleType_show").text($("#vehicleType").find("option:selected").text());
            } else if (objType == 1) {
                $("#people_Number").text($("#brand").val());
                $("#peopleIdentity").text($("#identity").val());
                $("#peoplePhone").text($("#phone").val());
            } else if (objType == 2) {
                $("#thing_Number").text($("#brand").val());
                $("#thing_enterprise").text($("#monitoringObjThingAdd").val());
                $("#thing_name1").text($("#thingName").val());
                $("#thing_model1").text($("#thingType").val());
            }
        },
        //终端 -- 综合展示
        terminalToShow: function () {
            $("#deviceNumber").text($("#devices").val());
            $("#deviceName").text($("#deviceNameIn").val());
        },
        //SIM -- 综合展示
        SIMToShow: function () {
            $("#simcardNumber").text($("#sims").val());
            $("#iccid_show").text($("input[name='iccid']").val());
        },
        //车、人、物点击tab切换
        chooseLabClick: function () {
            if (!$(this).hasClass('activeIcon')) {
                flag2 = true, flag3 = true;
                $("#devices").css('backgroundColor', '#fff');
                $("#sims").css('backgroundColor', '#fff');
                $("ul.dropdown-menu").css("display", "none");
                $(this).parents('.lab-group').find('input').attr("checked", false);
                $(this).siblings('input').attr("checked", true);
                $(this).parents('.lab-group').find('label.monitoringSelect').removeClass("activeIcon");
                $(this).addClass('activeIcon');

                //隐藏终端、终端手机号详情按钮，显示新增
                $("#terminalBtn").show();
                $("#devDetailBtn").hide();
                $("#SIMBtn").show();
                $("#SIMDetailBtn").hide();
                //隐藏终端、终端手机号详情和新增信息
                $(".terminalNew").hide();
                $(".equipmentMessage").hide();
                $(".SIMMessage").hide();
                $(".SIMNew").hide();
                //隐藏错误提示
                processInput.hideErrorMsg();

                objType = $(this).siblings('input').val();
                processInput.showType(objType);
                $("#brands").val("");
                $("#devices").val("");
                $("#deviceTypeIn").val("");
                $("#functionalTypeIn").val("");
                $("#sims").val("");
                processInput.putConfigValue();
                $("#monitorType").val("0");
                processInput.DeviceChange(objType);
            }
        },

        //选择人
        choosePeopleLabClick: function () {
            if ($("#chooseCar").attr("checked")) {
                $("#chooseCar").removeAttr("checked", "checked");
                $("#chooseCarLab").removeClass("activeIcon");
                $("#choosePeople").attr("checked", true);
                $("#choosePeopleLab").addClass('activeIcon');
                //隐藏终端、终端手机号详情按钮，显示新增
                $("#terminalBtn").show();
                $("#devDetailBtn").hide();
                $("#SIMBtn").show();
                $("#SIMDetailBtn").hide();
                //隐藏终端、终端手机号详情和新增信息
                $(".terminalNew").hide();
                $(".equipmentMessage").hide();
                $(".SIMMessage").hide();
                $(".SIMNew").hide();
                //隐藏错误提示
                processInput.hideErrorMsg();
            }
        },
        //选择车
        chooseCarLabClick: function () {
            if ($("#choosePeople").attr("checked")) {
                $("#choosePeople").removeAttr("checked", "checked");
                $("#choosePeopleLab").removeClass("activeIcon");
                $("#chooseCar").attr("checked", true);
                $("#chooseCarLab").addClass('activeIcon');
                //隐藏终端、终端手机号详情按钮，显示新增
                $("#terminalBtn").show();
                $("#devDetailBtn").hide();
                $("#SIMBtn").show();
                $("#SIMDetailBtn").hide();
                //隐藏终端、终端手机号详情和新增信息
                $(".terminalNew").hide();
                $(".equipmentMessage").hide();
                $(".SIMMessage").hide();
                $(".SIMNew").hide();
                //隐藏错误提示
                processInput.hideErrorMsg();
            }
        },
        CurentTime: function () {
            var now = new Date();

            var year = now.getFullYear();       //年
            var month = now.getMonth() + 1;     //月
            var day = now.getDate();            //日

            var hh = now.getHours();            //时
            var mm = now.getMinutes();          //分

            var clock = year + "-";

            if (month < 10)
                clock += "0";

            clock += month + "-";

            if (day < 10)
                clock += "0";

            clock += day + " ";

            if (hh < 10)
                clock += "0";

            clock += hh + ":";
            if (mm < 10) clock += '0';
            clock += mm;
            return (clock);
        },
        nextyeartime: function () {
            var now = new Date();

            var year = now.getFullYear() + 1;       //年
            var month = now.getMonth() + 1;     //月
            var day = now.getDate();            //日

            var hh = now.getHours();            //时
            var mm = now.getMinutes();          //分

            var clock = year + "-";

            if (month < 10)
                clock += "0";

            clock += month + "-";

            if (day < 10)
                clock += "0";

            clock += day + " ";

            if (hh < 10)
                clock += "0";

            clock += hh + ":";
            if (mm < 10) clock += '0';
            clock += mm;
            return (clock);
        },
        //获取车辆类别
        getVehicleCategory: function () {
            var url = "/clbs/m/basicinfo/monitoring/vehicle/type/findAllVehicleCategoryHasBindingVehicleType.gsp";
            var data = {"vehicleCategory": ""}
            json_ajax("GET", url, "json", false, data, processInput.categoryCallBack);
        },
        categoryCallBack: function (data) {
            var result = data.obj.result;
            console.log(result);
            var str = "";
            for (var i = 0; i < result.length; i++) {
                if (result[i].vehicleCategory == '其他车辆') {
                    str += '<option selected="selected" value="' + result[i].id + '">' + html2Escape(result[i].vehicleCategory) + '</option>'
                } else {
                    str += '<option  value="' + result[i].id + '">' + html2Escape(result[i].vehicleCategory) + '</option>'
                }
            }
            $("#category").html(str);
        },
        //获取车辆类型
        getVehicleType: function (id) {
            var url = "/clbs/m/basicinfo/monitoring/vehicle/type/findCategoryById_" + id + ".gsp";
            json_ajax("GET", url, "json", false, null, processInput.getTypeCallback);
        },
        getTypeCallback: function (data) {
            var result = data.obj.vehicleTypeList;
            var str = "";
            if (result != null && result != '') {
                for (var i = 0; i < result.length; i++) {
                    if (result[i].vehicleType == '其他车辆') {
                        str += '<option selected="selected" value="' + result[i].id + '">' + html2Escape(result[i].vehicleType) + '</option>'
                    } else {
                        str += '<option  value="' + result[i].id + '">' + html2Escape(result[i].vehicleType) + '</option>'
                    }
                }
            }
            $("#vehicleType").html(str);
        }
    }
    $(function () {
        processInput.init();

        processInput.getVehicleCategory();
        //获取车辆类型
        var curId = $("#category").val();
        processInput.getVehicleType(curId);
        $("#category").on("change", function () {
            var curId = $(this).val();
            processInput.getVehicleType(curId);
        })

        //默认展示车相关联的信息
        processInput.showType(objType);
        $('input').inputClear();
        var start_data = processInput.CurentTime();
        var end_data = processInput.nextyeartime();
        $('#timeInterval').dateRangePicker({
            'type': 'after',
            'format': 'YYYY-MM-DD',
            'start_date': start_data,
            'end_date': end_data,
            'element': '.nextBtnData'
        });
        $("#charBtn").on("click", processInput.charBtn);
        $("#backSelect").on("click", processInput.backSelect);
        $("#terminalBtn").on("click", processInput.terminalBtn);
        $("#backSelect2").on("click", processInput.backSelect2);
        $("#peopleBtn").on("click", function () {
            $(".peopleNew").show()
        });
        $("#thingsBtn").on("click", function () {
            $(".thingsNew").show()
        });
        $(".optionalListMessage").on("click", function () {
            $(this).parent().parent().next().toggle()
        });
        $("#grouping-add-btn").on("click", processInput.groupingAdd);
        $("#people-add-btn").on("click", processInput.peopleAdd);
        $("#SIMBtn").on("click", processInput.SIMBtn);
        $("#backSelect3").on("click", processInput.backSelect3);
        $("#addRight").on("click", processInput.addRight);
        $("#addLeft").on("click", processInput.addLeft);
        $("#numberBtn").on("click", processInput.numberBtn);
        //车辆验证上一步、下一步和取消
        $(".nextBtnBrand").on("click", processInput.nextBtnBrand);
        //人员验证上一步、下一步和取消
        $(".nextBtnPeople").on("click", processInput.nextBtnPeople);
        //物品验证上一步、下一步和取消
        $(".nextBtnThing").on("click", processInput.nextBtnThing);
        //组织验证上一步、下一步和取消
        $(".nextBtnGroup").on("click", processInput.nextBtnGroup);
        //终端验证上一步、下一步和取消
        $(".nextBtnDevice").on("click", processInput.nextBtnDevice);
        //Sim验证上一步、下一步和取消
        $(".nextBtnSim").on("click", processInput.nextBtnSim);
        //外设验证
        $(".nextBtnPeripheral").on("click", processInput.nextBtnPeripheral);
        //日期验证上一步、下一步和取消
        $(".nextBtnData").on("click", processInput.nextBtnData);
        //从业人员
        $(".nextBtnPeoList").on("click", processInput.nextBtnPeoList);
        $(".upBtn").on("click", processInput.upBtn);
        $("#myWizard ul li").on("click", processInput.wizard);
        $('.completionList').on("click", 'ul li', processInput.completionList);
        //详情
        $("#detailBtn").on("click", function () {
            processInput.detailToggle("charNew", "detailMessage")
        });
        $("#devDetailBtn").on("click", function () {
            processInput.detailToggle("terminalNew", "equipmentMessage")
        });
        $("#SIMDetailBtn").on("click", function () {
            processInput.detailToggle("SIMNew", "SIMMessage")
        });
        $("#gender").change(function () {
            $("#peopleGender").text($("#gender").find("option:selected").text());
        });
        $("#deviceTypeIn").change(function () {
            $("#deviceType").text($("#deviceTypeIn").val());
        });
        $("#deviceFunctionalIn").change(function () {
            $("#functionalType").text($("#deviceFunctionalIn").val());
        });
        $("#manuFacturerIn").bind('input oninput', function () {
            $("#manuFacturer").text($("#manuFacturerIn").val());
        });
        $("#operatorIn").change(function () {
            $("#operator").text($("#operatorIn").val());
        });
        $("#simFlowIn").bind('input oninput', function () {
            $("#simFlow").text($("#simFlowIn").val());
        });
        $("#useFlowIn").bind('input oninput', function () {
            $("#useFlow").text($("#useFlowIn").val());
        });
        $("#brands,#devices,#sims").blur(processInput.inputBlur);
        $("#car_groupName,#device_pgroupName,#sim_groupName,#monitoringObjPeopleAdd,#monitoringObjThingAdd").on("click", function () {
            processInput.showMenu(this)
        });
        $("#citySel").on("click", function () {
            processInput.showMenu1(this)
        });
        $("#citySel").siblings(".form-control-feedback").on("click", function () {
            $("#citySel").click()
        });
        $("#submits").on("click", processInput.submits);

        $("#forewarningCoefficient").bind('input oninput', function () {
            $("#alertsFlow").val((Number($("#forewarningCoefficient")[0].value) / 100 * Number($("#monthThresholdValue")[0].value)).toFixed(2))
            if ($("#alertsFlow").val() == "NaN") {
                $("#alertsFlow").val(0)
            }
        });
        $("#monthThresholdValue").bind('input oninput', function () {
            $("#alertsFlow").val((Number($("#forewarningCoefficient")[0].value) / 100 * Number($("#monthThresholdValue")[0].value)).toFixed(2))
            if ($("#alertsFlow").val() == "NaN") {
                $("#alertsFlow").val(0)
            }
        });
        $(document).bind('keydown', processInput.keydownEvent);
        $('#objectMonitoring').bind('click', processInput.objectMonitoring);

        // 终端新增时，给"是否视频"赋值
        $("#isVideos").val(1); // 默认1
        $("#isVideo_yes").bind("click", function () {
            $("#isVideos").val(1);
        });
        $("#isVideo_no").bind("click", function () {
            $("#isVideos").val(0);
        });
        // 终端手机号新增时，给"是否启用"赋值
        $("#isStart_sim").val(1); // 默认1
        $("#isRadio").bind("click", function () {
            $("input[name='isStart_sim']").val(1);
        });
        $("#noRadio").bind("click", function () {
            $("input[name='isStart_sim']").val(0);
        });
        $("#correctionCoefficient").val("100");
        $("#forewarningCoefficient").val("90");
        $("#monthlyStatement").val("01");
        // 默认值
        $("#monitorType").val("0");
    })
})(window, $)
