(function ($, widnow) {
    var charType = true;
    var parameter;//查询三个标定值
    var charNum;
    var startTime;
    var endTime;
    var table;
    var totalMileage;
    var totalFuelConsumption;
    var co2;
    var speed;
    var bTotalFuelConsumption;
    var bco2;
    var url_oil = "/clbs/v/carbonmgt/basicManagement/getOilSpillPagiInfo";
    var list = [];
    var index = 0;
    var len = 0;
    var start;
    var end;
    var mobileSourceManage = {
        init: function () {
            //车辆树
            var setting = {
                async: {
                    url: "/clbs/m/functionconfig/fence/bindfence/vehicelTree",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple"},
                    dataFilter: mobileSourceManage.ajaxDataFilter
                },
                check: {
                    enable: true,
                    chkStyle: "checkbox",
                    chkboxType: {
                        "Y": "s",
                        "N": "s"
                    },
                    radioType: "all"
                },
                view: {
                    dblClickExpand: false,
                    nameIsHTML: true,
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    onCheck: mobileSourceManage.onCheckVehicle
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },
        endTimeClick: function () {
            var width = $(this).width();
            var offset = $(this).offset();
            var left = offset.left - (207 - width);
            $("#laydate_box").css("left", left + "px");
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            $("#charSelect").val("").attr("data-id", "").bsSuggest("destroy");
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
                nodes = zTree.getCheckedNodes(true)
            var veh = [];
            var vid = [];
            var pname = [];
            for (var i = 0; i < nodes.length; i++) {
                if (nodes[i].type == "vehicle") {
                    veh.push(nodes[i].name)
                    pname.push(nodes[i].getParentNode().name)
                    vid.push(nodes[i].id)
                }
            }
            var pnames = mobileSourceManage.unique(pname);
            var vehName = mobileSourceManage.unique(veh);
            var vehId = mobileSourceManage.unique(vid);
            $("#charSelect").empty();
            var deviceDataList = {value: []};
            for (var j = 0; j < vehName.length; j++) {
                deviceDataList.value.push({
                    name: vehName[j],
                    id: vehId[j]
                });
            }
            ;
            $("#charSelect").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                data: deviceDataList,
                effectiveFields: ["name"]
            }).on('onDataRequestSuccess', function (e, result) {
            }).on("click", function () {
            }).on('onSetSelectValue', function (e, keyword, data) {
            }).on('onUnsetSelectValue', function () {
            });
            $("#charSelect").val(vehName[0]).attr("data-id", vehId[0]);
        },
        unique: function (arr) {
            var result = [], hash = {};
            for (var i = 0, elem; (elem = arr[i]) != null; i++) {
                if (!hash[elem]) {
                    result.push(elem);
                    hash[elem] = true;
                }
            }
            return result;
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            if (responseData) {
                var veh = [];
                var vid = [];
                var gourps = [];
                for (var i = 0; i < responseData.length; i++) {
                    responseData[i].checked = true
                    if (responseData[i].type == "group") {
                        gourps.push(responseData[i].name)
                    }
                    var gName = mobileSourceManage.unique(gourps)
                    $("#groupSelect").val(gName[0]);
                    if (responseData[i].type == "vehicle") {
                        veh.push(responseData[i].name)
                        vid.push(responseData[i].id)
                    }
                }
                var vehName = mobileSourceManage.unique(veh);
                var vehId = mobileSourceManage.unique(vid);
                $("#charSelect").empty();
                var deviceDataList = {value: []};
                for (var j = 0; j < vehName.length; j++) {
                    deviceDataList.value.push({
                        name: vehName[j],
                        id: vehId[j]
                    });
                }
                ;
                $("#charSelect").bsSuggest({
                    indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                    indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                    data: deviceDataList,
                    effectiveFields: ["name"]
                }).on('onDataRequestSuccess', function (e, result) {
                }).on("click", function () {
                }).on('onSetSelectValue', function (e, keyword, data) {
                }).on('onUnsetSelectValue', function () {
                });
                $("#charSelect").val(vehName[0]).attr("data-id", vehId[0]);
                return responseData;
            }
        },
        getDyails: function () {
            mobileSourceManage.oneAreaShow()
        },
        //开始时间
        startDay: function (day) {
            var timeInterval = $('#timeInterval').val().split('--');
            var startValue = timeInterval[0];
            var endValue = timeInterval[1];
            if (startValue == "" || endValue == "") {
                var today = new Date();
                var targetday_milliseconds = today.getTime() + 1000 * 60 * 60
                    * 24 * day;
                today.setTime(targetday_milliseconds); //注意，这行是关键代码
                var tYear = today.getFullYear();
                var tMonth = today.getMonth();
                var tDate = today.getDate();
                tMonth = mobileSourceManage.doHandleMonth(tMonth + 1);
                tDate = mobileSourceManage.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " "
                    + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24
                    * parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = mobileSourceManage.doHandleMonth(endMonth + 1);
                endDate = mobileSourceManage.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate + " "
                    + "23:59:59";
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = mobileSourceManage.doHandleMonth(vMonth + 1);
                vDate = mobileSourceManage.doHandleMonth(vDate);
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
                    vendMonth = mobileSourceManage.doHandleMonth(vendMonth + 1);
                    vendDate = mobileSourceManage.doHandleMonth(vendDate);
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
        //当前时间
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
                + 23
                + ":"
                + 59
                + ":"
                + 59;
        },
        allAreaShow: function (charNum, startTime, endTime) {
            charType = true;
            mobileSourceManage.ajaxList(0, charNum, startTime, endTime);
        },
        //ajax请求数据
        ajaxList: function (type, band, startTime, endTime) {
            var brandName = $("input[name='charSelect']").val();
            if (brandName.length > 8) {
                $("#carName").attr("title", brandName).tooltip('fixTitle');
                brandName = brandName.substring(0, 7) + '...';
            }else{
                $('#carName').removeAttr('data-original-title');
            }
            $("#carName").text(brandName);
            $(".toopTip-btn-left,.toopTip-btn-right").css("display", "inline-block");
            $("#criterionList1").addClass("active");
            $.ajax({
                type: "POST",
                url: "/clbs/v/carbonmgt/basicManagement/getOilInfo",
                data: {
                    "type": type,
                    "band": band,
                    "startTime": startTime,
                    "endTime": endTime
                },
                dataType: "json",
                async: true,
                beforeSend: function () {
                    layer.load(2);
                },
                success: function (data) {
                    if (data.obj == null) {
                        layer.msg("您查询的时间没有数据，请核对后查询！！");
                        layer.closeAll('loading');
                    } else {
                        totalMileage = data.obj.totalMileage;
                        totalFuelConsumption = data.obj.totalFuelConsumption;
                        co2 = data.obj.co2;
                        speed = data.obj.speed;
                        bTotalFuelConsumption = data.obj.bTotalFuelConsumption;
                        bco2 = data.obj.bco2;
                        $("#totalMileage").text(totalMileage + "km");
                        $("#totalFuelConsumption").text(totalFuelConsumption + "L");
                        $("#co2").text(co2 + "吨");
                        $("#speed").text(speed + "km/h");
                        $("#bTotalFuelConsumption").text(bTotalFuelConsumption + "L");
                        $("#bco2").text(bco2 + "吨");
                        $("#oilTable tbody").html("");
                        mobileSourceManage.infoinputTab(url_oil);
                    }
                }
            });
            $('#timeInterval').val(startTime + '--' + endTime);
        },
        add: function () {
            band = $("#charSelect").val();
            $.ajax({
                type: "POST",
                url: "/clbs/v/carbonmgt/basicManagement/add",
                data: {
                    "band": band,
                    "startTime": startTime,
                    "endTime": endTime
                },
                dataType: "json",
                async: true,
                beforeSend: function () {
                    layer.load(2);
                },
                success: function (data) {
                    if (data.success == true) {
                        layer.msg("保存成功");
                    } else {
                        layer.msg("保存失败");
                    }
                    layer.closeAll('loading');
                }
            });
        },
        //上一天
        upDay: function () {
            mobileSourceManage.startDay(1);
            var charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (charNum != "" && groupValue != "") {
                var dateValue = new Date().getTime();
                var startTimeValue = new Date(startTime.split(" ")[0].replace(/-/g, "/")).getTime();
                if (startTimeValue <= dateValue) {
                    mobileSourceManage.allAreaShow(charNum, startTime, endTime);
                    mobileSourceManage.validates()
                } else {
                    layer.msg("暂时没办法穿越，明天我再帮您看吧！");
                }
            } else {
                layer.msg("请选择监控对象！", {move: false});
            }
        },
        // 今天
        todayClick: function () {
            mobileSourceManage.nowDay();
            charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (charNum != "" && groupValue != "") {
                mobileSourceManage.allAreaShow(charNum, startTime, endTime);
                mobileSourceManage.validates();
            } else {
                layer.msg("请选择监控对象！", {move: false});
            }
        },
        //前一天
        yesterdayClick: function () {
            mobileSourceManage.startDay(-1);
            charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (charNum != "" && groupValue != "") {
                mobileSourceManage.allAreaShow(charNum, startTime, endTime);
                mobileSourceManage.validates()
            } else {
                layer.msg("请选择监控对象！", {move: false});
            }
        },
        //近三天
        nearlyThreeDays: function () {
            mobileSourceManage.startDay(-3);
            charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (charNum != "" && groupValue != "") {
                mobileSourceManage.allAreaShow(charNum, startTime, endTime);
                mobileSourceManage.validates()
            } else {
                layer.msg("请选择监控对象！", {move: false});
            }
        },
        //近七天
        nearlySevenDays: function () {
            mobileSourceManage.startDay(-7);
            charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (charNum != "" && groupValue != "") {
                mobileSourceManage.allAreaShow(charNum, startTime, endTime);
                mobileSourceManage.validates()
            } else {
                layer.msg("请选择监控对象！");
            }
        },
        //查询
        inquireClick: function () {
            $("#gsmx").removeClass("active");
            $("#gstj").addClass("active");
            $("#profile1").removeClass("active");
            $("#home1").addClass("active");
            charNum = $("#charSelect").attr("data-id");
            var timeInterval = $('#timeInterval').val().split('--');
            startTime = timeInterval[0];
            endTime = timeInterval[1];
            if (charNum != "") {
                if (mobileSourceManage.validates()) {
                    mobileSourceManage.allAreaShow(charNum, startTime, endTime);
                }
            } else {
                layer.msg("请选择监控对象！");
            }
        },
        getDitalInfo: function (band, startTime, endTime) {
            var charNameID = $("#gsmx");
            charNameID.show().children("a").text(band.split("),")[1].split(",")[0]);
            charType = false;
            var timeInterval = $('#timeInterval').val().split('--');
            startTime = timeInterval[0];
            endTime = timeInterval[1];
            parameter = {"band": band.split("),")[1].split(",")[0]};
            mobileSourceManage.ajaxList(1, $("#charSelect").attr("data-id"), startTime, endTime);
        },
        //查看基准列表
        lookCriterionList: function () {
            $(".criterionList-btn").hide();
            $("#criterionList1").hide();
            $(".tableReturn").show();
            $("#criterionList2").show();
            $(".cutBtn").show();
            band = $("#charSelect").val();
            $("#dateSearchData").hide();
            var timeInterval = $('#timeInterval').val().split('--');
            start = timeInterval[0];
            end = timeInterval[1];
            $.ajax({
                type: "POST",
                url: "/clbs/v/carbonmgt/basicManagement/find",
                data: {
                    "band": band
                },
                dataType: "json",
                async: true,
                beforeSend: function () {
                    layer.load(2);
                },
                success: function (data) {
                    if (data.obj.json.length == 0) {
                        layer.msg("您查询的时间没有数据，请核对后查询！！");
                        layer.closeAll('loading');
                    } else {
                        totalMileage = data.obj.totalMileage.toFixed(2);
                        speed = data.obj.speed.toFixed(2);
                        totalFuelConsumption = data.obj.totalFuelConsumption.toFixed(2);
                        bTotalFuelConsumption = data.obj.bTotalFuelConsumption.toFixed(2);
                        co2 = data.obj.co2.toFixed(2);
                        bco2 = data.obj.bco2.toFixed(2);
                        $("#totalMileage").text(totalMileage + "km");
                        $("#totalFuelConsumption").text(totalFuelConsumption + "L");
                        $("#co2").text(co2 + "吨");
                        $("#speed").text(speed + "km/h");
                        $("#bTotalFuelConsumption").text(bTotalFuelConsumption + "L");
                        $("#bco2").text(bco2 + "吨");
                        list = data.obj.json;
                        mobileSourceManage.criterionList();
                    }
                }
            });
        },
        upTable: function () {
            if (index < len) {
                index += 1;
                mobileSourceManage.criterionList();
            } else {
                layer.msg("亲，已经是最后一条了，要不你再去添加个？！");
            }
        },
        tableClick: function () {
            if (index > 0) {
                index -= 1;
                mobileSourceManage.criterionList();
            } else {
                layer.msg("亲，这是第一条，向右翻翻？！");
            }
        },
        criterionList: function () {
            var obj = JSON.parse(list[index]);
            len = list.length;
            band = $("#charSelect").attr("data-id");
            startTime = obj.startTime;
            endTime = obj.endTime;
            $('#timeInterval').val(startTime + '--' + endTime);
            $.ajax({
                type: "POST",
                url: "/clbs/v/carbonmgt/basicManagement/getOilInfo",
                data: {
                    "band": band,
                    "startTime": startTime,
                    "endTime": endTime
                },
                dataType: "json",
                async: true,
                beforeSend: function () {
                    layer.load(2);
                },
                success: function (data) {
                    $("#oilTableTwo tbody").html("");
                    mobileSourceManage.infoinputTabTwo(url_oil);
                }
            });
        },
        del: function () {
            var bandDel = $("#charSelect").val();
            var timeInterval = $('#timeInterval').val().split('--');
            var startTimeDel = timeInterval[0];
            var endTimeDel = timeInterval[1];
            $.ajax({
                type: "POST",
                url: "/clbs/v/carbonmgt/basicManagement/del",
                data: {
                    "band": bandDel,
                    "startTime": startTimeDel,
                    "endTime": endTimeDel
                },
                dataType: "json",
                async: true,
                beforeSend: function () {
                    layer.load(2);
                },
                success: function (data) {
                    if (data.success == true) {
                        layer.msg("保存成功");
                    } else {
                        layer.msg("保存失败");
                    }
                    layer.closeAll('loading');
                    mobileSourceManage.lookCriterionList();
                }
            });
        },
        lookTrackPlayback: function () {
            var bandLook = $("#charSelect").attr("data-id");
            var startTimeLook = $("#startTime").val();
            var endTimeLook = $("#endTime").val();
            window.open("/clbs/monitoring/trackPlayback?vid=" + bandLook + "&startTime=" + startTimeLook + "&endTime=" + endTimeLook + "");
        },
        //返回列表
        returnList: function () {
            $(".cutBtn").hide();
            $(".tableReturn").hide();
            $("#criterionList2").hide();
            $(".criterionList-btn").show();
            $("#criterionList1").show();
            $("#dateSearchData").show();
            index = 0;
            $('#timeInterval').val(start + '--' + end);
        },
        exampleTable: function (data) {
            table = $('#chedui1').DataTable({
                "destroy": true,
                "dom": 'trlip',// 自定义显示项
                "scrollX": true,
                "data": data,
                "lengthChange": true,// 是否允许用户自定义显示数量
                "bPaginate": true, // 翻页功能
                "bFilter": false, // 列筛序功能
                "searching": true,// 本地搜索
                "ordering": false, // 排序功能
                "Info": true,// 页脚信息
                "autoWidth": true,// 自动宽度
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
                "order": [
                    [0, null]
                ],
                "columnDefs": [{
                    "targets": 0,
                    "class": "text-center",
                }, {
                    "targets": 1,
                    "class": "text-center",
                }, {
                    "targets": 2,
                    "class": "text-center",
                }, {
                    "targets": 3,
                    "class": "text-center",
                }, {
                    "targets": 4,
                    "class": "text-center",
                }, {
                    "targets": 5,
                    "class": "text-center",
                }
                ],
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
        //创建表格
        infoinputTab: function (url) {
            var columnDefs = [{
                //第一列，用来显示序号
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];
            var columns = [{
                //第一列，用来显示序号
                "data": null,
                "class": "text-center"
            }, {
                "data": "brand",
                "class": "text-center"
            }, {
                "data": "groupName",
                "class": "text-center",
            }, {
                "data": "vehicleType",
                "class": "text-center",
            }, {
                "data": "fuelType",
                "class": "text-center",
            }, {
                "data": "vtime",
                "class": "text-center",
            }, {
                "data": "sumGpsMile",
                "class": "text-center",
            }, {
                "data": "stageMileage",
                "class": "text-center",
            }, {
                "data": "sumTotalOilwearOne",
                "class": "text-center",
            }, {
                "data": "stageTotalOilwearOne",
                "class": "text-center",
            }, {
                "data": "airConditionStatus",
                "class": "text-center",
            }, {
                "data": "airConditioningDuration",
                "class": "text-center",
            }
            ];
            //表格setting
            var setting = {
                listUrl: url,
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'oilTable', //表格
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
        },
        //创建表格
        infoinputTabTwo: function (url) {
            var columnDefs = [{
                //第一列，用来显示序号
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];
            var columns = [{
                //第一列，用来显示序号
                "data": null,
                "class": "text-center"
            }, {
                "data": "brand",
                "class": "text-center"
            }, {
                "data": "groupName",
                "class": "text-center",
            }, {
                "data": "vehicleType",
                "class": "text-center",
            }, {
                "data": "fuelType",
                "class": "text-center",
            }, {
                "data": "vtime",
                "class": "text-center",
            }, {
                "data": "sumGpsMile",
                "class": "text-center",
            }, {
                "data": "stageMileage",
                "class": "text-center",
            }, {
                "data": "sumTotalOilwearOne",
                "class": "text-center",
            }, {
                "data": "stageTotalOilwearOne",
                "class": "text-center",
            }, {
                "data": "airConditionStatus",
                "class": "text-center",
            }, {
                "data": "airConditioningDuration",
                "class": "text-center",
            }
            ];
            //表格setting
            var setting = {
                listUrl: url,
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'oilTableTwo', //表格
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
        },
        validates: function () {
            return $("#hourslist").validate({
                rules: {
                    groupId: {
                        required: true
                    },
                    charSelect: {
                        required: true
                    },
                    endTime: {
                        required: true,
                        compareDate: "#timeInterval",
                        compareDateDiff: "#timeInterval"
                    },
                    startTime: {
                        required: true
                    }
                },
                messages: {
                    groupId: {
                        required: "不能为空"
                    },
                    charSelect: {
                        required: "不能为空"
                    },
                    endTime: {
                        required: "不能为空",
                        compareDate: "结束日期必须大于开始日期!",
                        compareDateDiff: "查询的日期必须小于一周"
                    },
                    startTime: {
                        required: "不能为空",
                    }
                }
            }).form();
        },
    };

    $(function () {
        $('input').inputClear();
        mobileSourceManage.init();
        mobileSourceManage.nowDay();
        $('#timeInterval').dateRangePicker();
        $("#todayClick").bind("click", mobileSourceManage.todayClick);
        $("#yesterdayClick,#right-arrow").bind("click", mobileSourceManage.tableClick);
        $("#nearlyThreeDays").bind("click", mobileSourceManage.nearlyThreeDays);
        $("#nearlySevenDays").bind("click", mobileSourceManage.nearlySevenDays);
        $("#inquireClick").bind("click", mobileSourceManage.inquireClick);
        $("#left-arrow").bind("click", mobileSourceManage.upTable);
        $("#lookCriterionList").bind("click", mobileSourceManage.lookCriterionList);
        $("#returnList-btn").bind("click", mobileSourceManage.returnList);
        $("#endTime").bind("click", mobileSourceManage.endTimeClick);
        $("#groupSelect").bind("click", showMenuContent);
        $('#criterionList1').bind("click", mobileSourceManage.allReportClick);
        $('#add').bind("click", mobileSourceManage.add);
        $('#del').bind("click", mobileSourceManage.del);
        $('#lookTrackPlayback').bind("click", mobileSourceManage.lookTrackPlayback);
    });
})($, window)