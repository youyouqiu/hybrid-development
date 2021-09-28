(function ($, window) {
    var endTime;
    var startTime;
    var zTreeIdJson = {};
    var size;//当前权限监控对象数量
    var checkFlag = false;
    var bflag = true;
    var setting;
    var shieldMap;
    var shieldMarker;
    var crrentSubV = [];
    var geocoder;

    var lastInputVal = '';

    var trackValidReport = {
        // 初始化地图
        mapInit: function () {
            shieldMap = new AMap.Map("amapArea", {
                resizeEnable: true,
                scrollWheel: true,
                zoom: 18
            });

            geocoder = new AMap.Geocoder({
                radius: 1000,
                extensions: "all"
            });
        },
        // 初始化监控对象树
        treeInit: function () {
            setting = {
                async: {
                    url: trackValidReport.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "single"},
                    dataFilter: trackValidReport.ajaxDataFilter
                },
                check: {
                    enable: true,
                    chkStyle: "radio"
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
                    onCheck: trackValidReport.onCheck,
                    onAsyncSuccess: trackValidReport.zTreeOnAsyncSuccess,
                    onClick: trackValidReport.zTreeOnClick,
                    onExpand: trackValidReport.zTreeOnExpand,
                    beforeAsync: trackValidReport.zTreeBeforeAsync,
                    onNodeCreated: trackValidReport.zTreeOnNodeCreated,
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },

        getTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/functionconfig/fence/bindfence/getTreeByMonitorCount";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=monitor";
            }
        },

        ajaxDataFilter: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData.msg));
            if (responseData) {
                for (var i = 0; i < responseData.length; i++) {
                    if (responseData[i].iconSkin != "assignmentSkin")
                        {responseData[i].open = true;}
                }
            }
            return responseData;
        },

        //对象树勾选
        onCheck: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.selectNode(treeNode, false, true);
            var id = treeNode.id;
            var name = treeNode.name;
            $('#groupSelect').val(name);
            var monitorType;
            switch (treeNode.type) {
                case 'vehicle':
                    monitorType = 0;
                    break;
                case 'people':
                    monitorType = 1;
                    break;
                case 'thing':
                    monitorType = 2;
                    break;
            }
            $('#groupSelect').attr('data-type', monitorType);
            $('#charSelect').val(id);
            crrentSubV = [];
            crrentSubV.push(treeNode.id);
            $('#groupSelect').siblings('label').hide();
        },

        //对象树加载成功
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var vUuid = $('#vid').val();
            var parentId = $('#pid').val();
            if (parentId != "") {
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                var allNode = treeObj.getNodes();
                var parNode = treeObj.getNodesByParam("id", parentId, null);
                treeObj.expandNode(parNode[0], true, true, true, true); // 展开节点
                if (vUuid != "") {
                    var node = treeObj.getNodesByParam("id", vUuid, null);
                    if (node != null && node != undefined && node.length > 0) {
                        for (var i = 0, len = node.length; i < len; i++) {
                            treeObj.checkNode(node[i], true, true);
                            if (crrentSubV.length == 0) { // 存入勾选数组
                                crrentSubV.push(node[i].id);
                            }
                            var parentNode = node[i].getParentNode();
                        }
                        ;
                        var cityObj = $("#citySel");
                        cityObj.val(node[0].name);
                        var type = node[0].type;
                        worldType = type;
                    }
                    // trackPlayback.getActiveDate(vUuid, nowMonth, afterMonth);
                }
            }
            bflag = false;
            var zTree = $.fn.zTree.getZTreeObj(treeId);
            // 更新节点数量
            zTree.updateNodeCount(treeNode);
            // 默认展开200个节点
            var initLen = 0;
            notExpandNodeInit = zTree.getNodesByFilter(assignmentNotExpandFilter);
            for (i = 0; i < notExpandNodeInit.length; i++) {
                zTree.expandNode(notExpandNodeInit[i], true, true, false, true);
                initLen += notExpandNodeInit[i].children.length;
                if (initLen >= 200) {
                    break;
                }
            }
        },

        //对象树点击
        zTreeOnClick: function (event, treeId, treeNode) {
            if (treeNode.type == "vehicle" || treeNode.type == "people" || treeNode.type == 'thing') {
                var id = treeNode.id;
                $("#charSelect").val(id);
                var name = treeNode.name;
                $("#groupSelect").val(name);
                var monitorType;
                switch (treeNode.type) {
                    case 'vehicle':
                        monitorType = 0;
                        break;
                    case 'people':
                        monitorType = 1;
                        break;
                    case 'thing':
                        monitorType = 2;
                        break;
                }
                $('#groupSelect').attr('data-type', monitorType);
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                var nodes = treeObj.getCheckedNodes(true);
                for (var i = 0, l = nodes.length; i < l; i++) {
                    treeObj.checkNode(nodes[i], false, true);
                }
                treeObj.selectNode(treeNode, false, true);
                treeObj.checkNode(treeNode, true, true);
                $('#groupSelect').siblings('label').hide();
            }
        },

        zTreeOnExpand: function (event, treeId, treeNode) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (treeNode.type == "assignment" && treeNode.children === undefined) {
                var url = "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign";
                json_ajax("post", url, "json", false, {
                    "assignmentId": treeNode.id,
                    "isChecked": treeNode.checked,
                    "monitorType": "monitor"
                }, function (data) {
                    var result = JSON.parse(ungzip(data.msg));
                    if (result != null && result.length > 0) {
                        treeObj.addNodes(treeNode, result);
                        //trackPlayback.checkCurrentNodes();
                    }
                })
            }
        },

        zTreeBeforeAsync: function () {
            return bflag;
        },

        zTreeOnNodeCreated: function (event, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            var id = treeNode.id.toString();
            var list = [];
            if (zTreeIdJson[id] == undefined || zTreeIdJson[id] == null) {
                list = [treeNode.tId];
                zTreeIdJson[id] = list;
            } else {
                zTreeIdJson[id].push(treeNode.tId)
            }
        },

        searchVehicleTree: function (param) {
            crrentSubV = [];
            $('#charSelect').val('');
            if (param == null || param == undefined || param == '') {
                bflag = true;
                $.fn.zTree.init($("#treeDemo"), setting, null);
            } else {
                bflag = true;
                var querySetting = {
                    async: {
                        url: "/clbs/a/search/reportFuzzySearch",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"type": $('#queryType').val(), "queryParam": param, "queryType": "multiple"},
                        dataFilter: trackValidReport.ajaxQueryDataFilter
                    },
                    check: {
                        enable: true,
                        chkStyle: "radio"
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
                        onCheck: trackValidReport.onCheck,
                        onAsyncSuccess: trackValidReport.zTreeOnAsyncSuccess,
                        onClick: trackValidReport.zTreeOnClick,
                        onExpand: trackValidReport.zTreeOnExpand,
                        beforeAsync: trackValidReport.zTreeBeforeAsync,
                        onNodeCreated: trackValidReport.zTreeOnNodeCreated,
                    }
                };
                $.fn.zTree.init($("#treeDemo"), querySetting, null);
            }
        },

        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData));
            return filterQueryResult(responseData, crrentSubV);
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
                tMonth = trackValidReport.doHandleMonth(tMonth + 1);
                tDate = trackValidReport.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " " + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * parseInt(num);
                today.setTime(end_milliseconds); // 注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = trackValidReport.doHandleMonth(endMonth + 1);
                endDate = trackValidReport.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate + " " + "23:59:59";
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = trackValidReport.doHandleMonth(vMonth + 1);
                vDate = trackValidReport.doHandleMonth(vDate);
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
                    vendMonth = trackValidReport.doHandleMonth(vendMonth + 1);
                    vendDate = trackValidReport.doHandleMonth(vendDate);
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
        //上一天(前段没有此按钮)
        upDay: function () {
            trackValidReport.startDay(1);
            var charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (charNum != "" && groupValue != "") {
                var dateValue = new Date().getTime();
                var startTimeValue = new Date(startTime.split(" ")[0].replace(/-/g, "/")).getTime();
                if (startTimeValue <= dateValue) {
                    trackValidReport.ajaxList(charNum, startTime, endTime);
                    trackValidReport.validates()
                } else {
                    layer.msg("暂时没办法穿越，明天我再帮您看吧！");
                }
            } else {
                layer.msg("请选择监控对象！", {move: false});
            }
        },
        // 今天
        todayClick: function () {
            if (trackValidReport.validates()) {
                trackValidReport.nowDay();
                var time = startTime + '--' + endTime;
                $('#timeInterval').val(time);
                trackValidReport.getData();
            }
        },
        // 前一天
        yesterdayClick: function () {
            if (trackValidReport.validates()) {
                trackValidReport.startDay(-1);
                var time = startTime + '--' + endTime;
                $('#timeInterval').val(time);
                trackValidReport.getData();
            }
        },
        // 查询
        inquireClick: function () {
            if (trackValidReport.validates()) {
                trackValidReport.getData();
            }
        },
        // 获取查询数据
        getData: function () {
            // 获取监控对象ID
            var id = $('#charSelect').val();
            // 获取开始时间和结束时间
            var timeInterval = $('#timeInterval').val().split('--');
            var startTime = timeInterval[0];
            var endTime = timeInterval[1];

            var url = '/clbs/m/trackValidReport/getTrackValidListByF3Pass';
            var data = {
                monitorId: id,
                startTime: startTime,
                endTime: endTime,
            };
            ajax_submit('POST', url, 'json', true, data, true, trackValidReport.dataCallBack);
        },
        // 查询数据回调函数
        dataCallBack: function (data) {
            if (data.success) {
                shieldMap.clearMap();
                $('#exportData').removeAttr('disabled');
                // 轨迹位置经纬度组装
                var trajectoryPositions = [];// 正常点经纬度集合
                var shieldData = [];// 异常点经纬度集合
                var tableList = [];
                var trackValidReports = data.obj;
                var shieldNum = 0;
                for (var j = 0; j < trackValidReports.length; j++) {
                    if (tableList.length == 0) {
                        tableList.push([
                            j + 1,
                            trackValidReports[j].brand,
                            trackValidReports[j].groupName,
                            trackValidReports[j].assignmentName,
                            trackValidReports[j].color,
                            trackValidReports[j].vehicleType == 'null' ? '其他车辆' : trackValidReports[j].vehicleType,
                            0,
                        ]);
                    }
                    if (trackValidReports[j].trackValid == '0') {// 异常点
                        shieldNum++;
                        shieldData.push([
                            [
                                trackValidReports[j].longtitude,
                                trackValidReports[j].latitude
                            ],
                            [
                                trackValidReports[j + 1].longtitude,
                                trackValidReports[j + 1].latitude
                            ]
                        ]);
                    } else {// 正常点
                        trajectoryPositions.push([trackValidReports[j].longtitude, trackValidReports[j].latitude]);
                    }
                }
                if (tableList.length > 0) {
                    tableList[0][6] = shieldNum;
                }
                trackValidReport.mapTrajectory(trajectoryPositions, shieldData);
                trackValidReport.tableShieldData(tableList);
                // 数据列表绑定点击事件
                trackValidReport.tableClickFun();
            }
        },
        // 地图轨迹显示
        mapTrajectory: function (lineArr, shieldData) {
            lineArr = lineArr.reverse();
            shieldData = shieldData.reverse();
            if (lineArr.length > 0) {
                new AMap.Marker({
                    map: shieldMap,
                    position: lineArr[0],
                    offset: new AMap.Pixel(-16, -43), //相对于基点的位置
                    icon: new AMap.Icon({
                        size: new AMap.Size(40, 40), //图标大小
                        image: "/clbs/resources/img/start.svg",
                        imageOffset: new AMap.Pixel(0, 0)
                    })
                });

                new AMap.Marker({
                    map: shieldMap,
                    position: lineArr[lineArr.length - 1],
                    offset: new AMap.Pixel(-16, -43), //相对于基点的位置
                    icon: new AMap.Icon({
                        size: new AMap.Size(40, 40), //图标大小
                        image: "/clbs/resources/img/end.svg",
                        imageOffset: new AMap.Pixel(0, 0)
                    })
                });

                new AMap.Polyline({
                    map: shieldMap,
                    path: lineArr,
                    strokeColor: "#3366ff", //线颜色
                    strokeOpacity: 0.9, //线透明度
                    strokeWeight: 6, //线宽
                    strokeStyle: "solid", //线样式
                    showDir: true
                });

                for (var i = 0; i < shieldData.length; i++) {
                    new AMap.Polyline({
                        map: shieldMap,
                        path: shieldData[i],
                        strokeColor: "#d81e06", //线颜色
                        strokeOpacity: 0.9, //线透明度
                        strokeWeight: 6, //线宽
                        strokeStyle: "solid", //线样式
                        showDir: false
                    });
                }

                shieldMap.setFitView();
            }
        },
        // table表数据加载
        tableShieldData: function (data) {
            var table = $('#shieldTable').DataTable({
                "destroy": true,
                "dom": 'trlip',// 自定义显示项
                // "scrollX": true,
                "data": data,
                "lengthChange": true,// 是否允许用户自定义显示数量
                "bPaginate": true, // 翻页功能
                "bFilter": false, // 列筛序功能
                "searching": true,// 本地搜索
                "ordering": false, // 排序功能
                "Info": true,// 页脚信息
                "autoWidth": true,// 自动宽度
                "pageLength": 10,
                "lengthMenu": [5, 10, 20, 50, 100, 200],
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
                "drawCallback": function (settings) {
                    // 数据列表绑定点击事件
                    trackValidReport.tableClickFun();
                },
               /* "rowCallback": function (row, data) {
                    console.log('位置', data);
                    var lnglatXY = [data[13], data[14]];
                    var address;
                    geocoder.getAddress(lnglatXY, function (status, result) {
                        if (status === 'complete' && result.info === 'OK') {
                            address = result.regeocode.formattedAddress; //返回地址描述
                        } else {
                            address = '未定位';
                        }
                        $(row).find('td:nth-child(16)').text(address);
                    });
                },*/
                "order": [
                    [0, null]
                ],// 第一列排序图标改为默认

            });
            table.on('order.dt search.dt', function () {
                table.column(0, {
                    search: 'applied',
                    order: 'applied'
                }).nodes().each(function (cell, i) {
                    cell.innerHTML = i + 1;
                });
            }).draw();
        },
        // 数据列表点击事件
        tableClickFun: function () {
            $('#shieldTable tbody tr').off('click').on('click', function () {
                if (!!shieldMarker) {
                    shieldMap.remove([shieldMarker]);
                }
                var lng = $(this).find('td:nth-child(10)').text();
                var lat = $(this).find('td:nth-child(11)').text();
                var position = [lng, lat];
                shieldMarker = new AMap.Marker({
                    map: shieldMap,
                    position: position,
                    // offset: new AMap.Pixel(0, 0), //相对于基点的位置
                    animation: 'AMAP_ANIMATION_BOUNCE'
                });
            })
        },
        validates: function () {
            return $("#oilist").validate({
                rules: {
                    objName: {
                        required: true,
                        objName1: true
                    },
                    interruptTime: {
                        required: true,
                        digits: true,
                        range: [30, 86400],
                    },

                },
                messages: {
                    objName: {
                        required: vehicleSelectBrand
                    },
                    interruptTime: {
                        required: '请输入中断时长',
                        digits: '中断时长必须为正整数',
                        range: '中断时长范围为30到86400',
                    },
                }
            }).form();
        },
        // 导出数据
        exportDataFun: function () {
            if(getRecordsNum('shieldTable_info') > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var exportUrl = "/clbs/m/trackValidReport/exportTrackValidList";
            window.location.href = exportUrl;
        },
    }
    $(function () {
        trackValidReport.mapInit();
        trackValidReport.treeInit();
        $('#timeInterval').dateRangePicker({dateLimit: 1});
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var param = $("#groupSelect").val();
                trackValidReport.searchVehicleTree(param);
            }
            ;
        });
        ;
        $("#todayClick").bind("click", trackValidReport.todayClick);
        $("#yesterdayClick").bind("click", trackValidReport.yesterdayClick);
        $("#inquireClick").bind("click", trackValidReport.inquireClick);
        $("#groupSelect").bind("click", showMenuContent); //组织下拉显示
        /**
         * 监控对象树滚动展开
         */
        $("#menuContent").scroll(function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTreeScroll(zTree, this);
        });
        /**
         * 监控对象树模糊查询
         */
        var inputChange;
        $("#groupSelect").on('input propertychange', function (value) {

            if ($("#groupSelect").val() == lastInputVal && lastInputVal == '') {
                return;
            } 
                lastInputVal = $("#groupSelect").val()
            

            if (inputChange !== undefined) {
                clearTimeout(inputChange);
            }
            ;
            inputChange = setTimeout(function () {
                var param = $("#groupSelect").val();
                trackValidReport.searchVehicleTree(param);
            }, 500);
        });

        $('#queryType').on('change', function () {
            var param = $("#groupSelect").val();
            trackValidReport.searchVehicleTree(param);
        });

        /**
         * 导出数据
         */
        $('#exportData').on('click', trackValidReport.exportDataFun);
    });
}($, window));

jQuery.validator.addMethod("objName1", function (value, element) {
    var id = $('#charSelect').val();
    return id && value;
}, "请选择监控对象");
