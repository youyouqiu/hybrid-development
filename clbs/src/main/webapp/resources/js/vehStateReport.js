(function (window, $) {
    //开始时间
    var startTime;
    //结束时间
    var endTime;
    var myTable;
    //判断组织节点是否是勾选操作
    var checkFlag = false;
    //当前权限监控对象数量
    var size;
    var zTreeIdJson = {};
    //刚进入页面小于100自动勾选
    var ifAllCheck = true;
    var groupId = [];
    var maxCheckNum = 200;
    var simpleQueryParam = '';
    //用来储存显示数据
    var dataListArray = [];

    var currentRequestQueue = []; //当前网络请求队列
    var currentRequestResponse = true; //当前网络请求响应状态
    var requestTimes = 0; //网络请求次数
    var responseFailTimes = 0; //网络反馈失败次数
    var responseSuccessTimes = 0; //网络反馈成功次数

    var dbValue = false //树双击判断参数
    shiftRecordReport = {
        init: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            for (var i = 0; i < table.length - 1; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li >"
            }
            menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-viss\" data-column=\"" + parseInt(2) + "\" disabled />" + '操作' + "</label></li>"
            $("#Ul-menu-text").html(menu_text);
            //显示隐藏列
            $('.toggle-vis').on('change', function (e) {
                var column = myCLTable.dataTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });

            // shiftRecordReport.getVehicleCategory()

            //车辆树
            var setting = {
                async: {
                    url: shiftRecordReport.getParkingReportTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "type": "multiple",
                        "icoType": "0"
                    },
                    dataFilter: shiftRecordReport.ajaxDataFilter
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
                    countClass: "group-number-statistics"
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    beforeClick: shiftRecordReport.beforeClickVehicle,
                    onAsyncSuccess: shiftRecordReport.zTreeOnAsyncSuccess,
                    beforeCheck: shiftRecordReport.zTreeBeforeCheck,
                    onCheck: shiftRecordReport.onCheckVehicle,
                    onNodeCreated: shiftRecordReport.zTreeOnNodeCreated,
                    onExpand: shiftRecordReport.zTreeOnExpand,
                    onDblClick: shiftRecordReport.onDblClickVehicle
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },
        onDblClickVehicle: function (e, treeId, treeNode) {
            if (!treeNode) return
            dbValue = true
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.checkAllNodes(false);
            shiftRecordReport.getCheckedNodes("treeDemo");
            // $("#groupSelect").val(treeNode.name);
            // search_ztree('treeDemo', 'groupSelect', 'group');
            treeObj.checkNode(treeNode, !treeNode.checked, false, true);
        },
        getParkingReportTreeUrl: function (treeId, treeNode) {
            if (treeId == 'treeDemo') {
                return "/clbs/m/basicinfo/enterprise/professionals/tree";
            }
        },
        downVehicleList: function (id) {
            console.log(id, 'id')
        },
        doHandleMonth: function (month) {
            var m = month;
            if (month.toString().length == 1) {
                m = "0" + month;
            }
            return m;
        },
        exportAlarm: function () {
            if ($("#dataTable tbody tr td").hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据，无法导出");
                return;
            }
            if (getRecordsNum() > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var url = "/clbs/m/reportManagement/vehState/export"
            var parameter = {
                orgIds: groupId.toString(),
                startDate: startTime,
                endDate: endTime,
            }
            console.log(parameter, 'parameter')
            exportExcelUsePost(url, parameter)
        },

        validates: function () {
            return $("#lifeCycleForm").validate({
                rules: {
                    groupSelect: {
                        zTreeCheckGroup: "treeDemo"
                    }
                },
                messages: {
                    groupSelect: {
                        zTreeCheckGroup: vehicleSelectGroup,
                    },
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
            // if (size <= maxCheckNum && ifAllCheck) {
            //     treeObj.checkAllNodes(true);
            // }
            shiftRecordReport.getCharSelect(treeObj);
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            if (!treeNode.checked && !dbValue) {
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                var isCheckNode = treeObj.getCheckedNodes(true);
                var nodes = treeObj.getNodesByFilter(function (node) {
                    return node.type == 'group' && !node.checked;
                }, false, treeNode);
                if (isCheckNode.length + nodes.length + 1 > maxCheckNum) {
                    layer.msg('最多勾选' + maxCheckNum + '个企业' + '<br/>双击名称可选中本组织');
                    return false;
                }
            }
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            dbValue = false;
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                zTree.expandNode(treeNode, true, true, false, false); // 展开节点
                shiftRecordReport.validates();
            }
            shiftRecordReport.getCheckedNodes("treeDemo");
            shiftRecordReport.getCharSelect(zTree);
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
            var allNodes = treeObj.getNodes();
            if (nodes.length > 0) {
                $("#groupSelect").val(allNodes[0].name);
            } else {
                $("#groupSelect").val("");
            }
        },
        getCheckedNodes: function (treeId) {
            var zTree = $.fn.zTree.getZTreeObj(treeId),
                nodes = zTree.getCheckedNodes(true),
                groupIds = "";
            var datasss = []
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (treeId == "treeDemo" && nodes[i].type == "group") {
                    groupIds += nodes[i].uuid + ",";
                    datasss.push(nodes[i].uuid)
                }
                // groupId.push(nodes[i].uuid)
            }
            groupId = datasss;
        },
        getTable: function (table) {
            $('.toggle-vis').prop('checked', true);
            myTable = $(table).DataTable({
                "columnDefs": [{
                    "targets": [0, 2, 3, 4, 5, 6],
                    "searchable": false
                }],
                "destroy": true,
                /*"dom": 'tiprl',// 自定义显示项*/
                "lengthChange": true, // 是否允许用户自定义显示数量
                "bPaginate": true, // 翻页功能
                "bFilter": false, // 列筛序功能
                "searching": true, // 本地搜索
                "ordering": false, // 排序功能
                "Info": true, // 页脚信息
                "autoWidth": true, // 自动宽度
                "stripeClasses": [],
                "pageLength": 10,
                "lengthMenu": [5, 10, 20, 50, 100, 200],
                "pagingType": "simple_numbers", // 分页样式
                "dom": "t" + "<'row'<'col-md-2 col-sm-12 col-xs-12 noPadding'l><'col-md-4 col-sm-12 col-xs-12 noPadding'i><'col-md-6 col-sm-12  col - xs - 12 noPadding 'p>>",
                "oLanguage": { // 国际语言转化
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
                    //"sInfoFiltered": "（数据库中共为 _MAX_ 条记录）",
                    "sInfoFiltered": "",
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
                ], // 第一列排序图标改为默认

            });
            myTable.on('order.dt search.dt', function () {
                myTable.column(0, {
                    search: 'applied',
                    order: 'applied'
                }).nodes().each(function (cell, i) {
                    cell.innerHTML = i + 1;
                });
            }).draw();
            //显示隐藏列
            $('.toggle-vis').off('change').on('change', function (e) {
                var column = myTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });
            $("#search_button").on("click", shiftRecordReport.searchTable);
        },
        //刷新列表
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            shiftRecordReport.inquireClick(1);
        },
        searchTable: function () {
            simpleQueryParam = $("#simpleQueryParam").val();
            myTable.column(1).search(simpleQueryParam, false, false).draw();
        },
        //开始时间
        startDay: function (day) {
            var timeInterval = $('#timeInterval').val().split('--');
            var startValue = timeInterval[0];
            var endValue = timeInterval[1];
            if (startValue == "" || endValue == "") {
                var today = new Date();
                var targetday_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * day;
                today.setTime(targetday_milliseconds); //注意，这行是关键代码
                var tYear = today.getFullYear();
                var tMonth = today.getMonth();
                var tDate = today.getDate();
                tMonth = shiftRecordReport.doHandleMonth(tMonth + 1);
                tDate = shiftRecordReport.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " " +
                    "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 *
                    parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = shiftRecordReport.doHandleMonth(endMonth + 1);
                endDate = shiftRecordReport.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate + " " +
                    "23:59:59";
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = shiftRecordReport.doHandleMonth(vMonth + 1);
                vDate = shiftRecordReport.doHandleMonth(vDate);
                startTime = vYear + "-" + vMonth + "-" + vDate + " " +
                    "00:00:00";
                if (day == 1) {
                    endTime = vYear + "-" + vMonth + "-" + vDate + " " +
                        "23:59:59";
                } else {
                    var endNum = -1;
                    var vendtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * parseInt(endNum);
                    var dateEnd = new Date();
                    dateEnd.setTime(vendtoday_milliseconds);
                    var vendYear = dateEnd.getFullYear();
                    var vendMonth = dateEnd.getMonth();
                    var vendDate = dateEnd.getDate();
                    vendMonth = shiftRecordReport.doHandleMonth(vendMonth + 1);
                    vendDate = shiftRecordReport.doHandleMonth(vendDate);
                    endTime = vendYear + "-" + vendMonth + "-" + vendDate + " " +
                        "23:59:59";
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
        getsTheCurrentTime: function (flag) {
            var nowDate = new Date();
            if (flag) { // 昨天
                nowDate = new Date(nowDate.getTime() - 1000 * 60 * 60 * 24);
            }
            startTime = nowDate.getFullYear() +
                "-" +
                (parseInt(nowDate.getMonth() + 1) < 10 ? "0" +
                    parseInt(nowDate.getMonth() + 1) :
                    parseInt(nowDate.getMonth() + 1)) +
                "-" +
                (nowDate.getDate() < 10 ? "0" + nowDate.getDate() :
                    nowDate.getDate()) + " " + "00:00:00";
            endTime = nowDate.getFullYear() +
                "-" +
                (parseInt(nowDate.getMonth() + 1) < 10 ? "0" +
                    parseInt(nowDate.getMonth() + 1) :
                    parseInt(nowDate.getMonth() + 1)) +
                "-" +
                (nowDate.getDate() < 10 ? "0" + nowDate.getDate() :
                    nowDate.getDate()) +
                " " +
                ("23") +
                ":" +
                ("59") +
                ":" +
                ("59");
            var atime = $("#atime").val();
            if (atime != undefined && atime != "") {
                startTime = atime;
            }
        },
        /** 所有网络请求得到结果后重置次数 */
        initAfterRequest: function () {
            requestTimes = 0;
            responseFailTimes = 0;
            responseSuccessTimes = 0;
        },
        /** 网络请求队列函数  */
        requestQueue: function () {
            //网络请求队列里还有网络请求，当前请求反馈有结果时才进行下一个请求
            if (currentRequestQueue.length > 0 && currentRequestResponse) {
                shiftRecordReport.updateData(currentRequestQueue.shift());
            }
        },
        /** 请求更新服务器上的数据 */
        updateData: function (params) {
            requestTimes++; //网络请求次数加1
            currentRequestResponse = false; //已发送请求，但未响应	   
            var url = "/clbs/m/reportManagement/vehState/getData";
            json_ajax("GET", url, "json", true, params, shiftRecordReport.getCallback);
        },
        /** 根据网络反馈次数判断当前所有网络请求的结果 */
        requestResult: function () {
            console.log(`网络请求次数:${requestTimes}`, `失败:${responseFailTimes}`, `成功:${responseSuccessTimes}`);
            if (responseSuccessTimes == requestTimes) { //请求次数和成功次数相等
                shiftRecordReport.initAfterRequest();
                console.log('网络请求全部成功');
            } else if (responseFailTimes == requestTimes) { //请求次数和失败次数相等
                shiftRecordReport.initAfterRequest();
                console.log('网络请求全部失败');
            } else if (responseSuccessTimes + responseFailTimes == requestTimes) { //请求次数和反馈次数相等
                shiftRecordReport.initAfterRequest();
                console.log('网络请求部分成功');
            }
        },
        inquireClick: function (number) {
            if (requestTimes !== 0) {
                return
            }
            if (number == 0) {
                shiftRecordReport.getsTheCurrentTime();
            } else if (number == 'yesterday') {
                shiftRecordReport.getsTheCurrentTime(true)
            } else if (number == -1) {
                shiftRecordReport.startDay(-1)
            } else if (number == -3) {
                shiftRecordReport.startDay(-3)
            } else if (number == -7) {
                shiftRecordReport.startDay(-7)
            }
            if (number != 1) {
                $('#timeInterval').val(startTime + '--' + endTime);
                startTime = startTime;
                endTime = endTime;
            } else {
                var timeInterval = $('#timeInterval').val().split('--');
                startTime = timeInterval[0];
                endTime = timeInterval[1];
            }
            shiftRecordReport.getCheckedNodes('treeDemo');
            if (!shiftRecordReport.validates()) {
                return;
            }
            currentRequestQueue = []
            console.log(groupId, 'groupIdgroupIdgroupId')
            groupId.forEach(it => {
                currentRequestQueue.push({
                    orgId: it,
                    startDate: startTime,
                    endDate: endTime
                })
            })
            dataListArray = []
            // console.log(currentRequestQueue, 'currentRequestQueuecurrentRequestQueue')
            shiftRecordReport.requestQueue(currentRequestQueue)
        },
        getCallback: function (date) {
            if (date.success) {
                responseSuccessTimes++; //网络反馈成功次数加1            
                currentRequestResponse = true; //当前网络请求已响应
                if (date.obj) {
                    $("#exportAlarm").removeAttr("disabled");
                    var recordData = date.obj;
                    var model = JSON.stringify(recordData);
                    var dateList = [
                        1,
                        recordData.orgName,
                        recordData.total,
                        recordData.online,
                        recordData.offLine,
                        // recordData.equipmentFailure, equipmentFailure
                        "<span>" + recordData.outOfService + '/' + recordData.equipmentFailure + '/' + recordData.other + "</span>",
                        recordData.overSpeed,
                        recordData.tired,
                        recordData.line,
                        recordData.dawn,
                        recordData.camera,
                        recordData.otherAlarm,
                        "<a onclick='shiftRecordReport.showPreviewModal(" + model + ")'>打印</a>"
                    ];
                    dataListArray.push(dateList);
                    shiftRecordReport.reloadData(dataListArray);
                    $("#simpleQueryParam").val("");
                    shiftRecordReport.requestQueue(); //进行下一个请求.
                    shiftRecordReport.requestResult(); // 所有网络请求结果
                } else {
                    shiftRecordReport.reloadData(dataListArray);
                    $("#simpleQueryParam").val("");
                    $("#exportAlarm").attr("disabled", "disabled");
                }
            } else {
                if (date.msg != null) {
                    layer.msg(date.msg, {
                        move: false
                    });
                    currentRequestQueue.unshift(params); //添加到队列的开头再次请求  
                    responseFailTimes++; //网络反馈失败次数加1
                    currentRequestResponse = true; //当前网络请求已响应
                    shiftRecordReport.requestQueue(); //进行下一个请求
                    shiftRecordReport.requestResult(); // 所有网络请求结果
                }
            }
        },
        reloadData: function (dataList) {
            var currentPage = myTable.page();
            myTable.clear();
            myTable.rows.add(dataList);
            myTable.page(currentPage).draw(false);
            myTable.columns(1).search('', false, false).page(currentPage).draw();
        },
        getYesterDay: function () {
            var nowDate = new Date();
            var date = new Date(nowDate.getTime() - 24 * 60 * 60 * 1000);
            var seperator1 = "-";
            var year = date.getFullYear();
            var month = date.getMonth() + 1;
            var strDate = date.getDate();
            if (month >= 1 && month <= 9) {
                month = "0" + month;
            }
            if (strDate >= 0 && strDate <= 9) {
                strDate = "0" + strDate;
            }
            var yesterdate = year + seperator1 + month + seperator1 + strDate;
            return yesterdate;
        },

        /**
         * 打印功能
         * */

        showPreviewModal: function (data) {
            console.log('data', data)
            $("#orgName").text(data.orgName);
            $("#total").text(data.total);
            $("#online").text(data.online);
            $("#offLine").text(data.offLine);
            $("#outOfService").text(data.outOfService);
            $("#equipmentFailure").text(data.equipmentFailure);
            $("#other").text(data.other);
            $("#overSpeed").text(data.overSpeed);

            $("#tired").text(data.tired);
            $("#line").text(data.line);
            $("#dawn").text(data.dawn);
            $("#camera").text(data.camera);
            $("#otherAlarm").text(data.otherAlarm);

            var url = '/clbs/m/reportManagement/vehState/getPrintInfo?id=' + data.id;
            json_ajax('GET', url, 'json', false, null, function (message) {
                if (message.success) {
                    $("#tiredBrand").text(shiftRecordReport.dataDealwith(message.obj.tiredBrand));
                    $("#overSpeedBrand").text(shiftRecordReport.dataDealwith(message.obj.overSpeedBrand));
                    $("#msgNum").text(message.obj.msgNum);
                    $("#alarmHandled").text(message.obj.alarmHandled);
                    // $("#date").text(message.obj.startDate + '至' + message.obj.endDate);
                    $("#date").text(shiftRecordReport.dateShow(message.obj.startDate) + ' 至 ' + shiftRecordReport.dateShow(message.obj.endDate));
                    $("#handleResult").text(message.obj.handleResult ? message.obj.handleResult : '');
                }
            });
            $("#printPreview").modal('show');
        },

        dateShow: function (text) {
            if (text == undefined && text == null) {
                return '--';
            } else {
                return text;
            }
        },

        dataDealwith: function (text) {
            if (text == undefined && text == null) {
                return '[空]';
            } else {
                return text;
            }

        },

        printClick: function () {
            $("#oldHtml").hide();
            var obdHtml = window.document.body.innerHTML;
            var sprnstr = "<!--startprint-->";
            var eprnstr = "<!--endprint-->";
            var prnhtml = obdHtml.substr(obdHtml.indexOf(sprnstr) + 17);
            var prnhtml = prnhtml.substring(0, prnhtml.indexOf(eprnstr));
            $("#printHtml").html(prnhtml);
            $("#printHtml").show();
            window.print();
            $("#printHtml").html('');
            $("#printHtml").hide();
            $("#oldHtml").show();
        }
    };
    $(function () {
        shiftRecordReport.init();
        shiftRecordReport.getTable('#dataTable');
        // $('input').inputClear();

        // 车辆状态列表 搜索
        $('#simpleQueryParam').keyup(function (event) {
            if (event.keyCode == 13) {
                $("#search_button").click();
            };
        });
        var myDate = new Date()
        var Hours = myDate.getHours(); //获取当前小时数(0-23)
        var Minutes = myDate.getMinutes() > 9 ? myDate.getMinutes() : '0' + myDate.getMinutes(); //获取当前分钟数(0-59)
        var Seconds = myDate.getSeconds() > 9 ? myDate.getSeconds() : '0' + myDate.getSeconds(); //获取当前秒数(0-59)
        var hms = Hours + ':' + Minutes + ":" + Seconds
        $('#timeInterval').dateRangePicker({
            dateLimit: 30,
            // end_time: hms,
            // isOffLineReportFlag: true,
            // nowDate: shiftRecordReport.getYesterDay(),
            isShowHMS: false
        });
        $("#groupSelect").bind("click", showMenuContent);
        //导出
        $("#exportAlarm").bind("click", shiftRecordReport.exportAlarm);
        $("#refreshTable").bind("click", shiftRecordReport.refreshTable);

        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                search_ztree('treeDemo', 'groupSelect', 'group');
                treeObj.checkAllNodes(false);
            }
        });
        /**
         * 监控对象树模糊查询
         */
        $("#groupSelect").on('input propertychange', function (value) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.checkAllNodes(false);
            search_ztree('treeDemo', 'groupSelect', 'group');
        });

        $("#print").on('click', shiftRecordReport.printClick);
    })
})(window, $)