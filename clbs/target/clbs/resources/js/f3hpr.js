(function ($, window) {
    var startTime;
    var endTime;

    var myChart; // echarts对象
    var zTreeIdJson = {};
    var size;//当前权限监控对象数量
    var checkFlag = false;

    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询
    var ifAllCheck = true; //刚进入页面小于100自动勾选
    //用于ie浏览器判断选择节点树时是否需要调用搜索功能
    var isSearch = true;
    var legendSelected = { // echarts: 默认不展示的图例
        // '电压有效性': false,
        'ACC': true,
        '发动机状态': false,
        '行驶状态': false,
        "速度": true,
        "电压": true,
        "电量": true,
        "气压": true,
    };
    var TREE_MAX_CHILDREN_LENGTH=5000; // 组织树：最多勾选5000个对象
    var groupId=[]; // 组织树：选中查询对象id集合
    var groupName = null; // 组织树：选中的组织树名
    var tableListArray = []; // table：列表数据
    var myTable = null; // table：列表对象

    var xName = []; // echarts: 存储全局的x轴值；
    var opeName = []; // echarts: 存储全局的运营商值
    var comName = []; // echarts: 存储全局的通讯类型值

    var timerqq = null; // 延时器
    oilstatiscal = {
        // 组织树
        treeInit: function () {
            var setting = {
                async: {
                    url: oilstatiscal.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: oilstatiscal.ajaxDataFilter
                },
                check: {
                    enable: true,
                    // chkStyle: "checkbox",
                    chkStyle: "radio",
                    radioType: "all",
                    chkboxType: {
                        "Y": "s",
                        "N": "s"
                    }
                },
                view: {
                    dblClickExpand: false,
                    nameIsHTML: true,
                    countClass: "group-number-statistics"
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    beforeClick: oilstatiscal.beforeClickVehicle,
                    beforeCheck: oilstatiscal.zTreeBeforeCheck, // 勾选前判断，存储节点id
                    onCheck: oilstatiscal.onCheckVehicle, // 组织树对象勾选，更新input值
                    // onAsyncSuccess: oilstatiscal.zTreeOnAsyncSuccess, // 全选
                    onClick: oilstatiscal.ztreeOnClick, // 可点击文字选择
                    onExpand: oilstatiscal.zTreeOnExpand,
                    onNodeCreated: oilstatiscal.zTreeOnNodeCreated
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
            $("[data-toggle='tooltip']").tooltip();
        },
        //模糊查询树
        searchVehicleTree: function (param) {
            ifAllCheck = false;//模糊查询不自动勾选

            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                oilstatiscal.treeInit();
            } else {
                bflag = true;
                var querySetting = {
                    async: {
                        url: "/clbs/a/search/reportFuzzySearch",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"type": $('#queryType').val(), "queryParam": param, "queryType": "vehicle"},
                        dataFilter: oilstatiscal.ajaxQueryDataFilter
                    },
                    check: {
                        enable: true,
                        // chkStyle: "checkbox", // 多选
                        chkStyle: "radio", // 单选
                        radioType: "all",
                        chkboxType: {
                            "Y": "s",
                            "N": "s"
                        }
                    },
                    view: {
                        dblClickExpand: false,
                        nameIsHTML: true,
                        countClass: "group-number-statistics"
                    },
                    data: {
                        simpleData: {
                            enable: true
                        }
                    },
                    callback: {
                        onCheck: oilstatiscal.onCheckVehicle,
                        onClick: oilstatiscal.ztreeOnClick,
                        onExpand: oilstatiscal.zTreeOnExpand,
                        onNodeCreated: oilstatiscal.zTreeOnNodeCreated
                    }
                };
                $.fn.zTree.init($("#treeDemo"), querySetting, null);
            }
        },
        // 组织树：获取组织树地址
        getTreeUrl: function (treeId, treeNode) {

            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle";
            }
        },
        // 组织树：数据过滤（初始树）
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            if (responseData.msg) {
                var obj = JSON.parse(ungzip(responseData.msg));
                var data;
                if (obj.tree != null && obj.tree != undefined) {
                    data = obj.tree;
                    size = obj.size;
                } else {
                    data = obj
                }
                for (var i = 0; i < data.length; i++) {
                    data[i].open = true;
                    if(data[i].type !== "vehicle") {
                        data[i].nocheck = true;
                    }
                }

                return data;
            }
        },
        // 组织树：数据过滤（模糊查询树）
        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData));
            var nodesArr;
            if ($('#queryType').val() == "vehicle") {
                nodesArr = filterQueryResult(responseData, crrentSubV);
            } else {
                nodesArr = responseData;
            }
            for (var i = 0; i < nodesArr.length; i++) {
                nodesArr[i].open = true;
                if(nodesArr[i].type !== "vehicle") {
                    nodesArr[i].nocheck = true;
                }
            }
            return nodesArr;
        },
        // 组织树：是否全选
        // zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
        //     var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
        //     if (size <= TREE_MAX_CHILDREN_LENGTH && ifAllCheck) {
        //         treeObj.checkAllNodes(true);
        //
        //         var nodes = treeObj.getNodes();
        //
        //         $("#groupSelect").val(nodes[0].name); // 更新input框显示值
        //
        //         var allnodes = treeObj.getCheckedNodes(true);
        //
        //         groupId = []; // 每次清空
        //
        //         allnodes.forEach(function(d) {
        //
        //             if(d.type === "vehicle") {
        //                 // console.log(d.id)
        //                 groupId.push(d.id);
        //             }
        //
        //         });
        //
        //     }
        //     oilstatiscal.getCharSelect(treeObj);
        //
        // },
        // 组织树：获取id
        getGroupId: function() {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            var allnodes = treeObj.getCheckedNodes(true);
            groupId = []; // 每次清空

            allnodes.forEach(function(d) {

                if(d.type === "vehicle") {
                    groupId.push(d.id);
                    groupName = d.name;
                }

            });

        },
        // 组织树：是否点击文字就可进行选择
        ztreeOnClick: function(e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(treeId);
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            // 第2个参数为true：点击组织group时，全部勾选或取消
            // 第3个参数为null：点击组织group时，仅选择勾选对象，不包括子组织

            return false;
        },
        // 组织树：创建节点后的操作
        zTreeOnNodeCreated: function (event, treeId, treeNode) {
            var id = treeNode.id.toString();
            var list = [];
            if (zTreeIdJson[id] == undefined || zTreeIdJson[id] == null) {
                list = [treeNode.tId];
                zTreeIdJson[id] = list;
            } else {
                zTreeIdJson[id].push(treeNode.tId)
            }
        },
        // 组织树：check
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        // 组织树：勾选前判断
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;

                    json_ajax("post", "/clbs/m/personalized/ico/getCheckedVehicle",
                        "json", false, {"parentId": treeNode.id, "type": treeNode.type}, function (data) {
                            if (data.success) {
                                nodesLength += data.obj;
                            } else {
                                layer.msg(data.msg);
                            }
                        });

                    //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                    var ns = [];
                    //节点id
                    var nodeId;
                    for (var i = 0; i < nodes.length; i++) {
                        nodeId = nodes[i].id;
                        if (nodes[i].type == "people" || nodes[i].type == "vehicle" || nodes[i].type == "thing") {
                            //查询该节点是否在勾选组织或分组下，若在则不记录，不在则记录
                            var nd = zTree.getNodeByParam("tId", nodes[i].tId, treeNode);
                            if (nd == null && $.inArray(nodeId, ns) == -1) {
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength += ns.length;
                } else if (treeNode.type == "people" || treeNode.type == "vehicle" || treeNode.type == "thing") { //若勾选的为监控对象
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;
                    //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                    var ns = [];
                    //节点id
                    var nodeId;
                    for (var i = 0; i < nodes.length; i++) {
                        nodeId = nodes[i].id;
                        if (nodes[i].type == "people" || nodes[i].type == "vehicle" || nodes[i].type == "thing") {
                            if ($.inArray(nodeId, ns) == -1) {
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength = ns.length + 1;
                }
                if (nodesLength > TREE_MAX_CHILDREN_LENGTH) {
                    layer.msg(maxSelectItem);
                    flag = false;
                }
            }
            if (flag) {
                //若组织节点已被勾选，则是勾选操作，改变勾选操作标识
                if (treeNode.type == "group" && !treeNode.checked) {
                    checkFlag = true;
                }
            }
            return flag;
        },
        // 组织树：check，更新groupId
        onCheckVehicle: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            var dom = $('#groupSelect');
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                isSearch = false;
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    oilstatiscal.validates();
                }, 600);
            }
            var nodes = zTree.getCheckedNodes(true);
            crrentSubV = [];
            crrentSubV.push(treeNode.id);
            groupId = []; // 每次清空
            nodes.forEach(function(d) {
                if(d.type === "vehicle") {
                    groupId.push(d.id); // 存储选中的组织树id
                    groupName = d.name; // 存储选择的组织树名
                }
            });
            if(nodes.length > 0) {
                dom.val(treeNode.name);
            } else {
                dom.val("");
            }
        },
        // 组织树：展开
        zTreeOnExpand: function (event, treeId, treeNode) {
            //判断是否是勾选操作展开的树(是则继续执行，不是则返回)
            if (treeNode.type == "group" && !checkFlag) {
                return;
            }
            //初始化勾选操作判断表示
            checkFlag = false;
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (treeNode.type == "group") {
                var url = "/clbs/m/functionconfig/fence/bindfence/putMonitorByGroup";
                json_ajax("post", url, "json", false, {
                    "groupId": treeNode.id,
                    "isChecked": treeNode.checked,
                    "monitorType": "vehicle"
                }, function (data) {
                    var result = data.obj;
                    if (result != null && result != undefined) {
                        $.each(result, function (i) {
                            var pid = i; //获取键值

                            var parentTid = zTreeIdJson[pid][0];
                            var parentNode = treeObj.getNodeByTId(parentTid);
                            if (parentNode.children === undefined) {
                                treeObj.addNodes(parentNode, []);
                            }
                        });
                    }
                })
            }
        },
        // 组织树下拉框
        showMenu: function (e) {
            if ($("#menuContent").is(":hidden")) {
                var inpwidth = $("#groupSelect").outerWidth();
                $("#menuContent").css("width", inpwidth + "px");
                $(window).resize(function () {
                    var inpwidth = $("#groupSelect").outerWidth();
                    $("#menuContent").css("width", inpwidth + "px");
                })
                $("#menuContent").slideDown("fast");
            } else {
                $("#menuContent").is(":hidden");
            }
            $("body").bind("mousedown", oilstatiscal.onBodyDown);
        },
        // 隐藏Menu
        hideMenu: function () {
            $("#menuContent").fadeOut("fast");
            $("body").unbind("mousedown", oilstatiscal.onBodyDown);
        },
        // 判断隐藏menu
        onBodyDown: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "menuContent" || $(event.target).parents("#menuContent").length > 0)) {
                oilstatiscal.hideMenu();
            }
        },
        // 开始时间
        startDay: function (day) {
            var timeInterval = $('#timeInterval').val().split('--');
            var startValue = timeInterval[0];
            var endValue = timeInterval[1];
            if (startValue == "" || endValue == "") {
                var today = new Date();
                var targetday_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * day;
                today.setTime(targetday_milliseconds); // 注意，这行是关键代码
                var tYear = today.getFullYear();
                var tMonth = today.getMonth();
                var tDate = today.getDate();
                tMonth = oilstatiscal.doHandleMonth(tMonth + 1);
                tDate = oilstatiscal.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " " + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * parseInt(num);
                today.setTime(end_milliseconds); // 注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = oilstatiscal.doHandleMonth(endMonth + 1);
                endDate = oilstatiscal.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate + " " + "23:59:59";
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = oilstatiscal.doHandleMonth(vMonth + 1);
                vDate = oilstatiscal.doHandleMonth(vDate);
                startTime = vYear + "-" + vMonth + "-" + vDate + " "
                    + "00:00:00";
                if (day == 1) {
                    endTime = vYear + "-" + vMonth + "-" + vDate + " "
                        + "23:59:59";
                } else {
                    var endNum = -1;
                    var vendtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * parseInt(endNum);
                    var dateEnd = new Date();
                    dateEnd.setTime(vendtoday_milliseconds);
                    var vendYear = dateEnd.getFullYear();
                    var vendMonth = dateEnd.getMonth();
                    var vendDate = dateEnd.getDate();
                    vendMonth = oilstatiscal.doHandleMonth(vendMonth + 1);
                    vendDate = oilstatiscal.doHandleMonth(vendDate);
                    endTime = vendYear + "-" + vendMonth + "-" + vendDate + " "
                        + "23:59:59";
                }
            }
        },
        doHandleMonth: function (month) {
            var m = month;
            if (month.toString().length == 1) {
                m = "0" + month;
            }
            return m;
        },
        // 当前时间
        nowDay: function () {
            var nowDate = new Date();
            startTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate()) + " " + "00:00:00";
            endTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate())
                + " "
                + ("23")
                + ":"
                + ("59")
                + ":"
                + ("59");
        },
        // 获取时间
        getToday: function () {
            var nowDate = new Date();
            // var date = new Date(nowDate.getTime() - 24*60*60*1000); // 昨天
            var date = new Date(nowDate.getTime()); // 今天
            var seperator1 = "-";
            var year = date.getFullYear();
            var month = date.getMonth()+1;
            var strDate = date.getDate();
            if (month >= 1 && month <= 9) {
                month = "0" + month;
            }
            if (strDate >= 0 && strDate <= 9) {
                strDate = "0" + strDate;
            }
            var today = year + seperator1 + month + seperator1 + strDate;
            startTime = today + " 00:00:00";
            endTime = today + " 23:59:59";
            // return yesterdate;
        },
        // 查询
        inquireClick: function (number) {
            timeIntervalDom = $('#timeInterval');
            // 时间参数获取
            if(number==-1){ // 前一天
                oilstatiscal.startDay(-1);
                timeIntervalDom.val(startTime + '--' + endTime);
            }else if(number==-3){ // 前三天
                oilstatiscal.startDay(-3);
                timeIntervalDom.val(startTime + '--' + endTime);
            }else if(number ==-7){ // 前七天
                oilstatiscal.startDay(-7);
                timeIntervalDom.val(startTime + '--' + endTime);
                // console.log(startTime,endTime)
            }else if(number == 0) { // 今天
                var today = oilstatiscal.getToday();
                // console.log(startTime, endTime);
                timeIntervalDom.val(startTime + '--' + endTime);
            }else if(!number) { // 查询按钮
                var timeInterval = $('#timeInterval').val().split('--');
                startTime = timeInterval[0];
                endTime = timeInterval[1];
            }

            if (!oilstatiscal.validates()) {
                console.log('查询校验不通过');
                return;
            }

            var vehicleId = groupId.join(',');
            var ajaxDataParam = {
                startTime,
                endTime,
                "vehicleId": vehicleId,
                // startTime: "2019-11-21 00:00:00", // 目前只有这个参数有数据 先写死
                // endTime: "2019-11-21 23:59:59",
                // "vehicleId":'b64b53dd-4b4a-413d-a95c-e9ea52dfbeb8'
            };

            var url = "/clbs/v/oilmgt/f3hpr/getF3HighPrecisionReport";
            json_ajax("POST", url, "json", true, ajaxDataParam, oilstatiscal.getCallback);
            oilstatiscal.getVoltageThreshold(vehicleId);
        },
        // 数据校验
        validates: function () {
            return $("#oilist").validate({
                rules: {
                    groupSelect:{
                        regularChar: true,
                        // required: true
                        zTreeChecked: "treeDemo"
                    },
                    timeInterval:{
                        required: true
                    }
                },
                messages: {
                    groupSelect:{
                        zTreeChecked: vehicleSelectBrand
                    },
                    timeInterval:{
                        required: "请选择日期"
                    }
                }
            }).form();
        },
        // 查询回调
        getCallback: function (data) {
            if(!data.msg){return;}
            tableListArray = []; // 列表数据
            var axisX = []; // x轴数据;
            var axisY = {}; // y轴数据;

            if (data.success == true) {
                if(timerqq) {
                    clearTimeout(timerqq);
                };
                // 有时候会没展开，所以使用延时器
                setTimeout(function() {
                    $('#graphShowq-body').show();
                }, 500);
                var obj = JSON.parse(ungzip(data.msg));
                // console.log('高精度报表数据', obj);

                if(obj && obj.length > 0) {
                    // 获取列表数据
                    var voltageValid = [], // 电压有效性
                        accValid= [], // ACC
                        travelVoltage= [], // 电压
                        deviceElectricity= [], // 电量
                        airPressure= [], // 气压值
                        speed= [], // 速度
                        engineCondition= [], // 发动机状态
                        travelState= [], // 行驶状态
                        operatorStr = [], // 运营商
                        communicationTypeStr = []; // 通讯类型

                    obj.forEach(function(item, index) {
                        var th = [
                            obj.length - index, // 序号
                            groupName, // 监控对象
                            item.assignment ? item.assignment : "-", // 分组
                            item.time ? item.time : "-", // 时间
                            item.speedStr ? parseFloat(item.speedStr) + ' km/h' : "-", // 速度
                            item.travelVoltageStr ? parseFloat(item.travelVoltageStr) + ' V' : "-", // 电压
                            item.deviceElectricityStr ? parseFloat(item.deviceElectricityStr) + '%' : "-", // 电量
                            item.airPressureStr ? parseFloat(item.airPressureStr) + ' Pa' : "-", // 气压值
                            item.voltageValidStr ? item.voltageValidStr : "-", // 电压有效性
                            item.accValidStr ? item.accValidStr : "-",// acc有效性
                            item.engineConditionStr ? item.engineConditionStr : "-", // 发动机状态
                            item.travelStateStr ? item.travelStateStr : "-", // 行驶状态
                            item.communicationTypeStr ? item.communicationTypeStr : "-", // 通讯类型列表展现
                            item.operatorStr ? item.operatorStr : "-", // 运营商
                        ];
                        tableListArray.push(th);
                        // 获取echarts图标数据
                        axisX.push(item.time); // x轴数据

                        voltageValid.push(oilstatiscal.isValidData(item.voltageValid)); // 电压有效性
                        accValid.push(oilstatiscal.isValidData(item.accValidReverse)); // ACC
                        travelVoltage.push(oilstatiscal.isValidData(item.travelVoltage)); // 电压
                        deviceElectricity.push(oilstatiscal.isValidData(item.deviceElectricity)); // 电量
                        airPressure.push(oilstatiscal.isValidData(item.airPressure)); // 气压值
                        speed.push(oilstatiscal.isValidData(item.speed)); // 速度
                        engineCondition.push(oilstatiscal.isValidData(item.engineCondition)); // 发动机状态
                        travelState.push(oilstatiscal.isValidData(item.travelState)); // 行驶状态
                        operatorStr.push(item.operatorStr); // 运营商
                        communicationTypeStr.push(item.communicationTypeStr); // 通讯类型
                    })
                }

                axisY = {
                    // voltageValid, // 电压有效性
                    accValid, // ACC
                    travelVoltage, // 电压
                    deviceElectricity, // 电量
                    airPressure, // 气压值
                    speed, // 速度
                    engineCondition, // 发动机状态
                    travelState, // 行驶状态
                    operatorStr, // 运营商
                    communicationTypeStr, // 通讯类型
                };

                // 更新表格渲染数据
                tableListArray.reverse();
                oilstatiscal.updateCallback(tableListArray);
                // 更新echarts数据

                oilstatiscal.initEcharts('sjcontainer', axisX, axisY);

            } else {
                oilstatiscal.initEcharts('sjcontainer', [], {});
                if (data.msg != null) {
                    layer.msg(data.msg, {move: false});
                }
            }
        },
        // table：表格渲染
        initTable: function(dom) {
            myTable = dom.DataTable({
                "destroy": true,
                "lengthChange": true,// 是否允许用户自定义显示数量
                "bPaginate": true, // 翻页功能
                "bFilter": false, // 列筛序功能
                "searching": true,// 本地搜索
                "ordering": false, // 排序功能
                "Info": true,// 页脚信息
                "autoWidth": true,// 自动宽度
                "stripeClasses": [],
                "pageLength": 10,
                "lengthMenu": [5,10, 20, 50, 100, 200],
                "pagingType": "simple_numbers", // 分页样式
                "dom": "t" + "<'row'<'col-md-2 col-sm-12 col-xs-12 noPadding'l><'col-md-4 col-sm-12 col-xs-12 noPadding'i><'col-md-6 col-sm-12 col-xs-12 noPadding'p>>",
                "oLanguage": {// 国际语言转化
                    "oAria": {
                        "sSortAscending": " - click/return to sort ascending",
                        "sSortDescending": " - click/return to sort descending"
                    },
                    "sLengthMenu": "显示 _MENU_ 记录",
                    "sInfo": "当前显示 _START_ 到 _END_ 条，共 _TOTAL_ 条记录。",
                    "sZeroRecords": "我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？",
                    "sEmptyTable": "我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？",
                    "sLoadingRecords": "正在加载数据-请等待...",
                    "sInfoEmpty": "当前显示0到0条，共0条记录",
                    "sInfoFiltered": "（数据库中共为 _MAX_ 条记录）",
                    "sProcessing": "<img src='../resources/user_share/row_details/select2-spinner.gif'/> 正在加载数据...",
                    "sSearch": "模糊查询：",
                    "sUrl": "",
                    "oPaginate": {
                        "sFirst": "首页",
                        "sPrevious": " 上一页 ",
                        "sNext": " 下一页 ",
                        "sLast": " 尾页 "
                    }
                },
                "columnDefs": [{
                    "targets": [0, 2, 3, 4],
                    "searchable": false
                }],
                "drawCallback": function(settings) {
                },
                "order": [
                    [0, null]
                ],// 第一列排序图标改为默认

            });

            myTable.on('order.dt search.dt', function () {
                myTable.column(0, {
                    search: 'applied',
                    order: 'applied'
                }).nodes().each(function (cell, i) {
                    cell.innerHTML = i + 1;
                });
            }).draw();
        },
        // table：表格更新
        updateCallback: function(tableListArray) {
            var currentPage = myTable.page();
            // $("#simpleQueryParam"+tabInx).val(""); // 列表模糊搜索功能
            myTable.clear();
            myTable.rows.add(tableListArray);
            myTable.column(1).search('', false, false).page(currentPage).draw();
        },
        // echarts：配置项option
        echartsOption: {
            color: ['#c23531','#2f4554', '#bda29a', '#d48265', '#91c7ae','#749f83',  '#ca8622', '#61a0a8','#6e7074', '#546570'],
            tooltip: {
                trigger: 'axis',
                textStyle: {
                    fontSize: 14
                },
                formatter: function (a) {
                    var relVal = ""; // tooltip展示的数据
                    var type = ""; // 运营商
                    var com = ""; // 通讯类型

                    if (!a && a.length <= 0) {
                        relVal = "无相关数据";
                    } else if(Array.isArray(a)){ // 当a为数组：不为标线时

                        if(xName.length <= 0)  { // 当全局存储的x轴名（xName） 为空时，无数据返回
                            relVal = "无相关数据";
                        }else {
                            relVal = a[0].name;

                            for (var i = 0; i < a.length; i++) {
                                var value = a[i].value;
                                var color = a[i].color;

                                if (a[i].seriesName == "电压"){
                                    value = (value !== '' && value !== null && value !== undefined) ? value + "V" : "-";
                                }
                                else if(a[i].seriesName == "电量"){
                                    value = (value !== '' && value !== null && value !== undefined) ? value + "%" : "-";
                                }
                                else if(a[i].seriesName == "气压"){
                                    value = (value !== '' && value !== null && value !== undefined) ? value + "Pa" :"-";
                                }
                                else if(a[i].seriesName == "速度"){
                                    value = (value !== '' && value !== null && value !== undefined) ? value + "km/h" : "-";
                                }
                                /*else if(a[i].seriesName == "电压有效性"){
                                    var value = null;
                                    if(a[i].value == 0) {
                                        value = "有效"
                                    }else if(a[i].value == 1) {
                                        value = "无效"
                                    }else if(a[i].value == 2) {
                                        value = "未确定"
                                    }else {
                                        value = "-"
                                    }

                                } */
                                else if(a[i].seriesName == "ACC"){
                                    if(value == 0) {
                                        value = '未确认'
                                    }else if(value == 1) {
                                        value = '未接线'
                                    }else if(value == 2) {
                                        value = '接常电'
                                    }else if(value == 3) {
                                        value = '接线正常'
                                    }else {
                                        value = '-'
                                    }
                                } else if(a[i].seriesName == "发动机状态"){
                                    var value = null;
                                    if(a[i].value == 0) {
                                        value = "熄火"
                                    }else if(a[i].value == 1) {
                                        value = "打火"
                                    }else {
                                        value = "-"
                                    }
                                } else if(a[i].seriesName == "行驶状态"){
                                    if(a[i].value == 0) {
                                        value = "停止"
                                    }else if(a[i].value == 1) {
                                        value = "行驶"
                                    }else {
                                        value = "-"
                                    }
                                }
                                relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background:"+ color +"'></span>" + a[i].seriesName + "：" + value

                                var inx = a[i].dataIndex;//获取table表数据索引
                                type = "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;'></span>通讯类型：" + (comName && comName[inx] ? comName[inx] : "-");
                                com = "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;'></span>运营商：" + (opeName && opeName[inx] ? opeName[inx] : "-");

                            }
                            relVal += type + com;
                        }

                    }else if(a && !Array.isArray(a)) {
                        // 为标线markline时
                        relVal = "<span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;'></span>" + a.name + " : " + a.value + ' V';

                    }
                    return relVal;
                }

            },
            grid: {
                left: 200,
                right: 250,
            },
            legend: {
                data: [
                    // {name: '电压有效性'},
                    {name: "ACC"},
                    {name: "电压"},
                    {name: "电量"},
                    {name: "气压"},
                    {name: "发动机状态"},
                    {name: "行驶状态"},
                    {name: "速度"},
                ],
                left: 'auto',
                selected: legendSelected,
            },
            toolbox: {
                show: false
            },
            xAxis: {
                type: 'category',
                boundaryGap: false,
                data: ["a","b","c"]
            },
            yAxis: [
                /*{
                    type: "value",
                    min: 0,
                    max: 2,
                    name: '电压有效性',
                    position: 'left',
                    offset: 200,
                    boundaryGap: false,
                    axisLabel: {
                        interval: 0,
                        formatter: function(value) {
                            if(value == 0) {
                                return '未确认';
                            }else if(value == 1) {
                                return '无效';
                            }else if(value == 2) {
                                return '有效';
                            }
                        }
                    },
                    splitLine: {
                        show: false
                    }
                },*/
                {
                    type: 'value',
                    min: 0,
                    max: 3,
                    splitNumber: 3,
                    name: 'ACC',
                    position: 'left',
                    offset: 140,
                    boundaryGap: false,
                    axisLabel: {
                        interval: 0,
                        formatter: function(value) {
                            if(value == 3) {
                                return '接线正常'
                            }else if(value == 2) {
                                return '接常电'
                            }else if(value == 1) {
                                return '未接线'
                            }else if(value == 0) {
                                return '未确认'
                            }
                        }
                    },
                    splitLine: {
                        show: false
                    }
                },
                {
                    type: 'value',
                    name: '电压(V)',
                    min: 0,
                    // max: 'dataMax',
                    scale: true,
                    splitNumber: 4,
                    position: 'left',
                    offset: 75,
                    boundaryGap: false,
                    axisLabel: {
                        formatter: '{value}',
                    },
                    splitLine: {
                        show: false
                    }
                },
                {
                    type: 'value',
                    name: '电量(%)',
                    min: 0,
                    max: 100,
                    scale: true,
                    splitNumber: 5,
                    position: 'left',
                    boundaryGap: false,
                    axisLabel: {
                        formatter: '{value}',
                    },
                    splitLine: {
                        show: false
                    },
                },
                {
                    type: 'value',
                    name: '气压(pa)',
                    scale: true,
                    splitNumber: 4,
                    position: 'right',
                    boundaryGap: false,
                    min: function(value) {
                        var min = Math.floor((value.min - 1000)/100) * 100;
                        return min;
                    },
                    max: function(value) {
                        var max = Math.ceil((value.max + 1000)/100) * 100;
                        return max;
                    },
                    axisLabel: {
                        formatter: '{value}'
                    },
                    splitLine: {
                        show: false
                    }
                },
                {
                    type: 'value',
                    name: '速度(km/h)',
                    scale: true,
                    splitNumber: 4,
                    position: 'right',
                    offset: 75,
                    boundaryGap: false,
                    max:  function (value) {
                        return value.max > 240 ? value.max : 240;
                    },
                    min: 0,
                    axisLabel: {
                        formatter: '{value}',
                    },
                    splitLine: {
                        show: false
                    }
                },
                {
                    type: 'value',
                    min: 0,
                    max: 1,
                    name: '发动机状态',
                    position: 'right',
                    offset: 150,
                    boundaryGap: false,
                    axisLabel: {
                        interval: 0,
                        formatter: function(value) {
                            if(value == 0) {
                                return '熄火'
                            }else if(value == 1) {
                                return '打火'
                            }
                        }
                    },
                    splitLine: {
                        show: false
                    },
                    axisTick:{
                        show:false
                    },
                },
                {
                    type: 'value',
                    min: 0,
                    max: 1,
                    // data: ["停止", "行驶"],
                    name: '行驶状态',
                    position: 'right',
                    offset: 215,
                    boundaryGap: false,
                    axisLabel: {
                        interval: 0,
                        formatter: function(value) {
                            if(value == 0) {
                                return '停止'
                            }else if(value == 1) {
                                return '行驶'
                            }
                        }
                    },
                    splitLine: {
                        show: false
                    },
                    axisTick:{
                        show:false
                    },
                }
            ],
            dataZoom: [
                {
                    type: 'inside'
                },
                {
                    start: 0,
                    end: 10,
                    handleIcon: 'M10.7,11.9v-1.3H9.3v1.3c-4.9,0.3-8.8,4.4-8.8,9.4c0,5,3.9,9.1,8.8,9.4v1.3h1.3v-1.3c4.9-0.3,8.8-4.4,8.8-9.4C19.5,16.3,15.6,12.2,10.7,11.9z M13.3,24.4H6.7V23h6.6V24.4z M13.3,19.6H6.7v-1.4h6.6V19.6z',
                    handleSize: '80%',
                    handleStyle: {
                        color: '#fff',
                        shadowBlur: 3,
                        shadowColor: 'rgba(0, 0, 0, 0.6)',
                        shadowOffsetX: 2,
                        shadowOffsetY: 2
                    }
                }
            ],
            series: [
                /*{
                    name: '电压有效性',
                    yAxisIndex: 0,
                    type: 'line',
                    smooth: true,
                    symbol: 'circle',
                    symbolSize: 5,
                    sampling: 'average',
                    data: [0,0,null],
                    connectNulls: false,
                    itemStyle: {
                        normal: {
                            color: 'rgb(248, 123, 0)'
                        }
                    },
                    areaStyle: {
                        normal: {
                            color: 'rgb(248, 123, 0)'
                        }
                    }
                },*/

        {
                    name: 'ACC',
                    yAxisIndex: 0,
                    type: 'line',
                    smooth: true,
                    symbol: 'circle',
                    symbolSize: 5,
                    sampling: 'average',
                    data: [2,null,2],
                    connectNulls: false,
                    itemStyle: {
                        normal: {
                            // color: 'rgb(109, 207, 246)'
                        }
                    },
                    areaStyle: {
                        normal: {
                            // color: 'rgb(109, 207, 246)'
                        }
                    }
                },
                {
                    name: '电压',
                    yAxisIndex: 1,
                    type: 'line',
                    smooth: true,
                    symbol: 'none',
                    symbolSize: 5,
                    sampling: 'average',
                    data: [1, 100, 200],
                    itemStyle: {
                        normal: {
                            // color: '#C37DD3'
                        }
                    },
                },
                {
                    name: '电量',
                    yAxisIndex: 2,
                    type: 'line',
                    smooth: true,
                    symbol: 'none',
                    symbolSize: 5,
                    sampling: 'average',
                    data: [100,200,40],
                    itemStyle: {
                        normal: {
                            // color: 'yellow'
                        }
                    },
                },
                {
                    name: '气压',
                    yAxisIndex: 3,
                    type: 'line',
                    smooth: true,
                    symbol: 'none',
                    symbolSize: 5,
                    sampling: 'average',
                    data: [60000, 70000, 90000],
                    itemStyle: {
                        normal: {
                            // color: 'rgb(150, 78, 7)'
                        }
                    },
                },
                {
                    name: '速度',
                    yAxisIndex: 4,
                    type: 'line',
                    smooth: true,
                    symbol: 'none',
                    symbolSize: 5,
                    sampling: 'average',
                    data: [0,2,100],
                    itemStyle: {
                        normal: {
                            // color: 'rgb(145, 218, 0)'
                        }
                    },
                },
                {
                    name: '发动机状态',
                    yAxisIndex: 5,
                    type: 'line',
                    smooth: true,
                    symbol: 'circle',
                    symbolSize: 5,
                    sampling: 'average',
                    data: [null, 1,1],
                    itemStyle: {
                        normal: {
                            // color: '#6e7074'
                        }
                    },
                    areaStyle: {
                        normal: {
                            // color: '#6e7074'
                        }
                    }
                },
                {
                    name: '行驶状态',
                    yAxisIndex: 6,
                    type: 'line',
                    smooth: true,
                    symbol: 'circle',
                    symbolSize: 5,
                    sampling: 'average',
                    data: [1,1,1],
                    itemStyle: {
                        normal: {
                            // color: '#546570'
                        }
                    },
                    areaStyle: {
                        normal: {
                            // color: '#546570'
                        }
                    }
                },
            ]
        },
        // echarts: 图形初始化
        initEcharts: function(id, axisX, axisY) {
            myChart = echarts.init(document.getElementById(id));

            oilstatiscal.updateEcharts(axisX, axisY);

        },
        // echarts：图形渲染更新
        updateEcharts: function(axisX, axisY) {
            myChart.clear();

            var {
                // voltageValid, // 电压有效性
                accValid, // ACC
                travelVoltage, // 电压
                deviceElectricity, // 电量
                airPressure, // 气压值
                speed, // 速度
                engineCondition, // 发动机状态
                travelState, // 行驶状态
                operatorStr, // 运营商
                communicationTypeStr, // 通讯类型
            } = axisY;

            xName = axisX; // 存储全局的x轴值；
            opeName = operatorStr; // 存储全局的运营商值
            comName = communicationTypeStr; // 存储全局的通讯类型值

            var option = oilstatiscal.echartsOption;
            option.series[1].markLine = {}; // 清空markline设置
            option.xAxis.data = axisX; // 更新x轴数据

            // 更新y轴数据
            // option.series[0].data = voltageValid; // 电压有效性
            option.series[0].data = accValid; // ACC
            option.series[1].data = travelVoltage; // 电压
            option.series[2].data = deviceElectricity; // 电量
            option.series[3].data = airPressure; // 气压
            option.series[4].data = speed; // 速度
            option.series[5].data = engineCondition; // 发动机状态
            option.series[6].data = travelState; // 行驶状态

            myChart.setOption(option);
            window.onresize = myChart.resize;

            // 图例点击事件
            myChart.on('legendselectchanged', function (obj) {// 监听legend点击事件
                var name = obj.name; // 当前点击对象
                var selected = obj.selected; // 当前图例状态
                legendSelected =  obj.selected;
                // 四种状态互斥（当一种显示时，其他三条隐藏）
                if(selected[name] == true) {
                    /*if(name == "电压有效性") {
                        legendSelected['ACC'] = false;
                        legendSelected['发动机状态'] = false;
                        legendSelected['行驶状态'] = false;
                    }else */
                    if(name == "ACC") {
                        // legendSelected['电压有效性'] = false;
                        legendSelected['发动机状态'] = false;
                        legendSelected['行驶状态'] = false;
                    }else if(name == "发动机状态") {
                        legendSelected = obj.selected;
                        // legendSelected['电压有效性'] = false;
                        legendSelected['ACC'] = false;
                        legendSelected['行驶状态'] = false;
                    }else if(name == "行驶状态") {
                        legendSelected = obj.selected;
                        // legendSelected['电压有效性'] = false;
                        legendSelected['发动机状态'] = false;
                        legendSelected['ACC'] = false;
                    }
                }
                var option = oilstatiscal.echartsOption;

                option.legend.selected = legendSelected;
                myChart.setOption(option);
            });
        },
        // echarts: 设置电压阈值
        setEcharts: function() {
            // 当myChart存在时,操作设置
            if(myChart) {
                // 获取设置的阈值
                var set1 = $('#mileageq1').val(); // 打火电压阈值
                var set2 = $('#fuelq1').val(); // 熄火电压阈值

                var option = oilstatiscal.echartsOption;
                var markLine =  {
                    symbol: 'none',
                    data: [
                        {
                            yAxis: parseInt(set1),
                            name: '打火电压阈值',
                            symbol: 'none',
                            lineStyle: {
                                normal: {
                                    color: 'darkgreen',
                                    type: 'solid'
                                }
                            },
                            label: {
                                normal: {
                                    show: false
                                }
                            }
                        },
                        {
                            yAxis: parseInt(set2),
                            name: '熄火电压阈值',
                            symbol: 'none',

                            lineStyle: {
                                normal: {
                                    color: 'darkorange',
                                    type: 'solid'
                                }
                            },
                            label: {
                                normal: {
                                    show: false
                                }
                            }
                        }
                    ]
                };

                option.series[1].markLine = markLine;
                myChart.clear();
                myChart.setOption(option);
                window.onresize = myChart.resize;

                // 更新页面内值
                $('#mileageq').html(set1);
                $('#fuelq').html(set2);

                myChart.on('datazoom', function (params){
                    // console.log(params);
                    //获得起止位置百分比
                    var startPercent = myChart.getModel().option.dataZoom[1].start;
                    var endPercent = myChart.getModel().option.dataZoom[1].end;

                    option.dataZoom[1].start = startPercent;
                    option.dataZoom[1].end = endPercent;
                    myChart.setOption(option);

                });

            }

        },
        //列表数据导出
        exportFun: function () {
            if ($('.table:visible tbody tr td').hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据，无法导出");
                return;
            }
            if(getRecordsNum('oilTable_info') > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var url = "/clbs/v/oilmgt/f3hpr/export";

            var timeInterval = $('#timeInterval').val().split('--');

            startTime = timeInterval[0];
            endTime = timeInterval[1];
            oilstatiscal.getGroupId(); // 更新groupId
            var parameter = {
                "startTime": startTime,
                "endTime": endTime,
                "vehicleId": groupId.join(','),
            };
            exportExcelUseForm(url, parameter);
        },
        //获取电压阈值
        getVoltageThreshold: function(vehicleId){
            var param = {
                vehicleId: vehicleId
            };

            json_ajax("post", "/clbs/v/oilmgt/f3hpr/getVoltageInfo",
                "json", false, param, function (data) {
                    if (data.success) {
                        if(data.obj){
                            var obj = data.obj;
                            var startValue = obj.startValue ? obj.startValue + ' V' : '- V',//打火电压阈值
                                stopValue = obj.stopValue ? obj.stopValue + ' V' : '- V',//熄火电压阈值
                                vh = obj.vh ? obj.vh + ' V' : '- V',//电压最高值
                                vl = obj.vl ? obj.vl + ' V' : '- V';//电压最低值

                            $('#mileageq').html(startValue);
                            $('#fuelq').html(stopValue);
                            $('#travelTimeq').html(vh);
                            $('#idleTimeq').html(vl);
                        }

                    } else {
                        if(data.msg){
                            layer.msg(data.msg);
                        }
                    }
                });
        },
        isValidData: function(value){
            if(value ==='' || value === null || value === undefined || isNaN(value)){
                return null;
            }

            return value;
        }
    };
    $(function () {
        // 组织树初始化
        oilstatiscal.treeInit();

        // 构造数组方法：未使用（但是不敢删除，怕影响其他文件）
        Array.prototype.isHas = function (a) {
            if (this.length === 0) {
                return false
            };
            for (var i = 0; i < this.length; i++) {
                if (this[i].seriesName === a) {
                    return true
                }
            }
        };

        // 时间设置
        oilstatiscal.nowDay();
        $('#timeInterval').dateRangePicker({
            dateLimit: 31
        });

        // 组织下拉显示
        $("#groupSelectSpan,#groupSelect").bind("click", oilstatiscal.showMenu);

        // 导出
        $("#exportBtn").bind("click", oilstatiscal.exportFun);

        // 组织树模糊查询
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var param = $("#groupSelect").val();
                oilstatiscal.searchVehicleTree(param);
            };
        });

        /**
         * 监控对象树模糊查询
         */
        var inputChange;
        $("#groupSelect").on('input propertychange', function (value) {
            if (inputChange !== undefined) {
                clearTimeout(inputChange);
            }
            ;
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                isSearch = true;
            }
            $("#charSelect").val("").attr("data-id", "").bsSuggest("destroy");
            inputChange = setTimeout(function () {
                if (isSearch) {
                    var param = $("#groupSelect").val();
                    oilstatiscal.searchVehicleTree(param);
                }
                isSearch = true;
            }, 500);
        });
        // $('#queryType').on('change', function () {
        //     var param = $("#groupSelect").val();
        //     oilstatiscal.searchVehicleTree(param);
        // });

        // table:初始化列表
        oilstatiscal.initTable($('#oilTable'));

        // echarts:设置电压阈值
        /*$('#model-setq').on('click', function() {
            oilstatiscal.setEcharts();
        });*/

        // echarts：清空电压阈值
        $('#model-closeq').on('click', function() {
            $('#mileageq1').val('');
            $('#fuelq1').val('');
        });


        $("#graphShowq-chevron").unbind("click");

        $('#queryType').on('change', function () {
            var param = $("#groupSelect").val();
            oilstatiscal.searchVehicleTree(param);
        });
    });
}($, window));
