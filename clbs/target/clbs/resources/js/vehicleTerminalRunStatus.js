(function (window, $) {
    var orgId = '';
    var checkFlag = false; //判断组织节点是否是勾选操作
    var size;//当前权限监控对象数量
    var zTreeIdJson = {};
    var simpleQueryParam = '';
    var queryDateStr = '';
    var initStatus = true;

    vehicleTerminalRunStatus = {
        init: function () {
            //车辆树
            var setting = {
                async: {
                    url: "/clbs/m/basicinfo/enterprise/professionals/tree",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: vehicleTerminalRunStatus.ajaxDataFilter
                },
                check: {
                    enable: true,
                    chkStyle: "radio",
                    chkboxType: {
                        "Y": "s",
                        "N": "s"
                    },
                    radioType: "all"
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
                    beforeClick: vehicleTerminalRunStatus.beforeClickVehicle,
                    onAsyncSuccess: vehicleTerminalRunStatus.zTreeOnAsyncSuccess,
                    onCheck: vehicleTerminalRunStatus.onCheckVehicle,
                    onNodeCreated: vehicleTerminalRunStatus.zTreeOnNodeCreated,
                    onExpand: vehicleTerminalRunStatus.zTreeOnExpand
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);

            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            for (var i = 0; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-text").html(menu_text);
        },
        doHandleMonth: function (month) {
            var m = month;
            if (month.toString().length == 1) {
                m = "0" + month;
            }
            return m;
        },
        //判断日期是否合法,是否选中车辆
        validates: function () {
            return $("#hourslist").validate({
                rules: {
                    groupSelect: {
                        zTreeCheckGroup: "treeDemo"
                    },
                    timeInterval: {
                        required: true
                    }
                },
                messages: {
                    groupSelect: {
                        zTreeCheckGroup: '请选择组织',
                    },
                    timeInterval: {
                        required: '请选择时间'
                    }
                }
            }).form();
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            if (treeId == "treeDemo") {
                size = responseData.length;
                return responseData;
            }
        },
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.expandAll(true);
            if (initStatus) {
                initStatus = false;
                var nodes = treeObj.getNodes();
                if (nodes[0]) {
                    orgId = nodes[0].uuid;
                    treeObj.checkNode(nodes[0], true, true);
                }
                vehicleTerminalRunStatus.getTable('#dataTable');
            }
            vehicleTerminalRunStatus.getCharSelect(treeObj);
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                zTree.expandNode(treeNode, true, true, false, false); // 展开节点
                setTimeout(() => {
                    vehicleTerminalRunStatus.getCheckedNodes();
                    vehicleTerminalRunStatus.validates();
                }, 600);
            }
            vehicleTerminalRunStatus.getCheckedNodes();
            vehicleTerminalRunStatus.getCharSelect(zTree);
        },
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
        zTreeOnExpand: function (event, treeId, treeNode) {
            //判断是否是勾选操作展开的树(是则继续执行，不是则返回)
            if (treeNode.type == "group" && !checkFlag) {
                return;
            }
            //初始化勾选操作判断表示
            checkFlag = false;
        },
        getCharSelect: function (treeObj) {
            var nodes = treeObj.getCheckedNodes(true);
            if (nodes.length > 0) {
                $("#groupSelect").val(nodes[0].name);
            } else {
                $("#groupSelect").val("");
            }
        },
        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
                nodes = zTree.getCheckedNodes(true);
            orgId = nodes[0] ? nodes[0].uuid : '';
        },
        //获取当前时间，格式YYYY-MM-DD
        getNowFormatDate: function () {
            var date = new Date();
            var separator = "-";
            var year = date.getFullYear();
            var month = date.getMonth() + 1;
            var strDate = date.getDate();
            var hour = date.getHours();
            var minutes = date.getMinutes();
            var seconds = date.getSeconds();
            if (hour >= 1 && hour <= 9) {
                hour = "0" + hour;
            }
            if (minutes >= 1 && minutes <= 9) {
                minutes = "0" + minutes;
            }
            if (seconds >= 1 && seconds <= 9) {
                seconds = "0" + seconds;
            }
            if (month >= 1 && month <= 9) {
                month = "0" + month;
            }
            if (strDate >= 0 && strDate <= 9) {
                strDate = "0" + strDate;
            }
            return (year + separator + month + separator + strDate + ' ' + hour + ':' + minutes + ':' + seconds);
        },
        //设置时间
        setNewDay: function (day) {
            var timeInterval = $('#timeInterval').val();

            var startTimeIndex = timeInterval.replace(/-/g, "/");
            var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
            var dateList = new Date();
            dateList.setTime(vtoday_milliseconds);
            var vYear = dateList.getFullYear();
            var vMonth = dateList.getMonth();
            var vDate = dateList.getDate();
            vMonth = vehicleTerminalRunStatus.doHandleMonth(vMonth + 1);
            vDate = vehicleTerminalRunStatus.doHandleMonth(vDate);
            var hour = dateList.getHours();
            var minutes = dateList.getMinutes();
            var seconds = dateList.getSeconds();
            if (hour >= 1 && hour <= 9) {
                hour = "0" + hour;
            }
            if (minutes >= 1 && minutes <= 9) {
                minutes = "0" + minutes;
            }
            if (seconds >= 1 && seconds <= 9) {
                seconds = "0" + seconds;
            }
            var startTime = vYear + "-" + vMonth + "-" + vDate + ' ' + hour + ':' + minutes + ':' + seconds;
            $('#timeInterval').val(startTime);
        },
        inquireClick: function (number) {
            $(".ToolPanel").css("display", "block");
            if (number != 1 && number != 0) {
                vehicleTerminalRunStatus.setNewDay(number);
            }
            if (number == 0) {
                $('#timeInterval').val(vehicleTerminalRunStatus.getNowFormatDate());
            }
            vehicleTerminalRunStatus.getCheckedNodes();
            if (!vehicleTerminalRunStatus.validates()) {
                return;
            }
            $('#simpleQueryParam').val('');
            vehicleTerminalRunStatus.searchInitTabData();
            //是否可以导出
            $("#exportAlarm").prop("disabled", false);
        },
        //数据列表
        searchInitTabData: function () {
            myTable.requestData();
        },
        exportAlarm: function (e) {
            if ($("#dataTable tbody tr td").hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据，无法导出");
                return;
            }
            if(getRecordsNum() > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var url = "/clbs/adas/vehicleDeviceState/export";
            var parameter = {
                "orgId": orgId,
                "date": queryDateStr,
                "simpleQueryParam": simpleQueryParam
            };
            json_ajax("post", url, "json", true, parameter, function (result) {
                if (result.success) {
                    layer.confirm(exportTitle, {
                        title: '操作确认',
                        icon: 3, // 问号图标
                        btn: ['确定', '导出管理'] //按钮
                    }, function () {
                        layer.closeAll();
                    }, function () {
                        layer.closeAll();
                        // 打开导出管理弹窗
                        pagesNav.showExportManager();
                    });
                } else if (result.msg) {
                    layer.msg(result.msg);
                }
            });
            // exportExcelUseForm(url, parameter);
        },
        columnRenderFun: function (key) {
            switch (key) {
                case 'brakingStatus':// 制动状态(0:无制动 1:制动)
                    return function (data) {
                        if (data === 1) return '制动';
                        if (data === 0) return '无制动';
                        return '-';
                    };
                case 'turnSignalStatus':// 转向灯状态(0:未开方向灯 1:左转方向灯 2:右转方向灯)
                    return function (data) {
                        if (data === 0) return '未开方向灯';
                        if (data === 1) return '左转方向灯';
                        if (data === 2) return '右转方向灯';
                        return '-';
                    };
                case 'highBeamStatus'://远光状态
                case 'lowBeamStatus':// 近光状态
                case 'airStatus':// 空调状态
                case 'heaterStatus':// 加热器状态
                case 'clutchStatus':// 离合器状态
                case 'absStatus':// ABS状态
                case 'clearanceLampStatus':// 示廓灯状态
                    return function (data) {
                        if (data === 1) return '开';
                        if (data === 0) return '关';
                        return '-';
                    };
                case 'gearStatus':// 档位状态 0:空挡 1-9:档位 10:倒挡 17:驻车档
                    return function (data) {
                        if (data === 0) return '空挡';
                        if (data === 16) return '倒挡';
                        if (data === 17) return '驻车档';
                        if (data) return data + '档';
                        return '-';
                    };
                case 'powerStatus':// 主电源状态 0:正常 1:欠压 2:掉电
                    return function (data) {
                        if (data === 0) return '正常';
                        if (data === 1) return '欠压';
                        if (data === 2) return '掉电';
                        return '-';
                    };
                case 'spareBatteryStatus':// 备用电源状态 0:正常 1:欠压 2:失效
                    return function (data) {
                        if (data === 0) return '正常';
                        if (data === 1) return '欠压';
                        if (data === 2) return '失效';
                        return '-';
                    };
                case 'gpsStatus':// 卫星定位模块状态 0:正常 1:故障
                case 'ttsStatus':// TTS模块状态  0:正常 1:故障
                case 'memoryStatus':// 存储器状态 0:正常 1:故障
                case 'spareMemoryStatus':// 备用存储器状态  0:正常 1:故障
                    return function (data) {
                        if (data === 0) return '正常';
                        if (data === 1) return '故障';
                        return '-';
                    };
                default:
                    return null;
            }
        },
        getTable: function (table) {
            //显示隐藏列
            var columnDefs = [{
                "searchable": true,
                "orderable": false,
                "targets": [0, 1, 2, 3]
            }];
            var columnKeys = [
                'groupName', 'monitorName', 'address', 'altitude', 'speed',
                'direction', 'vehicleSpeed', 'time', 'axisAccelerationX', 'axisAccelerationY', 'axisAccelerationZ',
                'axisAngularX', 'axisAngularY', 'axisAngularZ', 'brakingStatus', 'turnSignalStatus',
                'highBeamStatus', 'lowBeamStatus', 'gearStatus', 'acceleratorPedalValue', 'brakePedalValue',
                'engineSpeed', 'steeringWheelAngle', 'airStatus', 'heaterStatus', 'clutchStatus',
                'absStatus', 'clearanceLampStatus', 'obdSpeed', 'powerStatus', 'spareBatteryStatus', 'gpsStatus',
                'ttsStatus', 'spareMemoryStatus',
            ];
            var columns = [{
                "data": null,
                "class": "text-center",
            }];
            for (var i = 0; i < columnKeys.length; i++) {
                columns.push({
                    "data": columnKeys[i],
                    "class": "text-center",
                    render: vehicleTerminalRunStatus.columnRenderFun(columnKeys[i]) || function (data) {
                        if (data) return data;
                        return '-';
                    }
                })
            }
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.orgId = orgId;
                queryDateStr = $('#timeInterval').val();
                d.date = queryDateStr;
                simpleQueryParam = $('#simpleQueryParam').val();
                d.simpleQueryParam = simpleQueryParam;
            };
            //表格setting
            var setting = {
                listUrl: "/clbs/adas/vehicleDeviceState/list",
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                lengthMenu: [5, 10, 20, 50, 100],
                pageable: true, //是否分页
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
            $('.toggle-vis').off().on('change', function (e) {
                var visible = myTable.dataTable.column($(this).attr('data-column')).visible();
                if (visible) {
                    myTable.dataTable.column($(this).attr('data-column')).visible(false);
                } else {
                    myTable.dataTable.column($(this).attr('data-column')).visible(true);
                }
                $(".keep-open").addClass("open");
            });
        },
        //刷新列表
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            simpleQueryParam = '';
            vehicleTerminalRunStatus.inquireClick(1);
        },
    };
    $(function () {
        //初始化页面
        vehicleTerminalRunStatus.init();
        laydate.render({
            elem: '#timeInterval',
            theme: '#6dcff6',
            type: 'datetime',
            max: vehicleTerminalRunStatus.getNowFormatDate()
        });
        $("#timeInterval").val(vehicleTerminalRunStatus.getNowFormatDate());
        //组织下拉显示
        $("#groupSelect").bind("click", showMenuContent);
        //导出
        $("#exportAlarm").bind("click", vehicleTerminalRunStatus.exportAlarm);
        $("#refreshTable").bind("click", vehicleTerminalRunStatus.refreshTable);

        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                search_ztree('treeDemo', 'groupSelect', 'group');
                var nodes = treeObj.getCheckedNodes(true);
                if (nodes[0]) {
                    treeObj.checkNode(nodes[0], false, true);
                }
            }
        });

        /**
         * 监控对象树模糊查询
         */
        $("#groupSelect").on('input propertychange', function (value) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            search_ztree('treeDemo', 'groupSelect', 'group');
        });
    })
}(window, $))