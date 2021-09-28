// 原本lifecycleExpireRepot.js拷贝 防止代码互相污染
(function (window, $) {
    //开始时间
    var queryDateStr;
    var myTable;

    //刚进入页面小于100自动勾选
    var ifAllCheck = true;
    var groupId = [];

    var listId; // 列表请求详情id
    var myChart; // 存储echarts对象
    var searchAfter; // es查询下页带的角标参数,分页使用（第一次不传
    var isGoOn = true; // 当角标后端返回为null时，isGoOn改为false，阻止继续滚动更新
    var isRender = false; // 是否重复渲染筛选列框$("#dropDown-q")

    var trColor; // tr颜色切换
    var scrollTimer = null;

    var vehicleIds = []; // 存储批量导出车辆id
    var count = 1; // 用以计算明细列表序号

    var defaultSort = [[9, 'desc']]; //第一列正序
    var isUseParam = false; // 日均排行排序时，是否使用模糊搜索的参数

    // 去掉小数点后多余的0
    var noZero = function(data) {
        var regexp=/(?:\.0*|(\.\d+?)0+)$/;
        return data.replace(regexp,'$1');
    };
    var isA,isB; // 判断概况+明细是否有数据返回
    // dataTable 自定义排序(时分秒）
    var getSecondFromText = function (text) {
        var hourReg = /(\d+)小时/;
        var miReg = /(\d+)分/;
        var sReg = /(\d+)秒/;
        var hourMatch = text.match(hourReg);
        var miMatch = text.match(miReg);
        var sMatch = text.match(sReg);

        var second = 0;
        if (hourMatch !== null) {
            second += parseInt(hourMatch[0]) * 3600;
        }
        if (miMatch !== null) {
            second += parseInt(miMatch[0]) * 60;
        }
        if (sMatch !== null) {
            second += parseInt(sMatch[0]);
        }

        return second;
    };
    // 页面逻辑
    lifeCycleExpire = {
        init: function () {
            // 渲染订制表头的dom结构
            lifeCycleExpire.customTableHead();
            // 组织树渲染
            lifeCycleExpire.treeInit();
            // 监控对象评分统计-数据详情
            lifeCycleExpire.dataClear();
        },
        // 渲染订制表头dom结构
        customTableHead: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");

            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(1) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }

            $("#Ul-menu-text").html(menu_text);
        },
        // 组织树渲染
        treeInit: function () {
            //车辆树
            var setting = {
                async: {
                    url: lifeCycleExpire.getParkingReportTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: { // 是否可选  Organization
                        "isOrg": "1"
                    },
                    dataFilter: lifeCycleExpire.ajaxDataFilter
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
                    selectedMulti: false,
                    nameIsHTML: true,
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    onAsyncSuccess: lifeCycleExpire.zTreeOnAsyncSuccess,
                    onCheck: lifeCycleExpire.onCheckVehicle,
                    onClick: lifeCycleExpire.ztreeOnClick
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },
        ztreeOnClick: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, null, true);
            return false;
        },
        // 获取组织树请求地址
        getParkingReportTreeUrl: function (treeId, treeNode) {
            if (treeId == 'treeDemo') {
                return "/clbs/m/basicinfo/enterprise/professionals/tree";
            }
        },
        // 数据筛选
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            if (treeId == "treeDemo") {
                return responseData;
            }
        },
        // 节点树获取成功后操作
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (ifAllCheck) {
                var nodes = treeObj.getNodes();
                treeObj.checkNode(nodes[0]); // 默认勾选单个节点
                $("#groupSelect").val(nodes[0].name); // 更新input框显示值

                for (var i = 0; i < nodes.length; i++) { //设置节点展开
                    treeObj.expandNode(nodes[i], true, true, true);
                }

            }
        },
        // 组织树勾选操作
        onCheckVehicle: function () {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            var nodes = treeObj.getCheckedNodes(true);
            if (nodes.length > 0) {
                $("#groupSelect").val(nodes[0].name);
                lifeCycleExpire.validates();
            } else {
                $("#groupSelect").val("");
            }
        },
        // 批量导出
        exportAll: function () {
            if ($("#dataTable tbody tr td").hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据，无法导出");
                return;
            }
            if(getRecordsNum() > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            lifeCycleExpire.getCheckedNodes("treeDemo");
            if (!lifeCycleExpire.validates()) {
                return;
            }

            var timeInterval = $('#timeInterval').val();

            var reg = /-/g;
            var time = timeInterval.replace(reg, '');

            var ids = [];
            // 循环input获取当前勾选的值
            $("input[name=objCheck-q]:checkbox").each(function () {
                if($(this).prop("checked")) {
                    ids.push($(this).val())
                }
            });
            var parameter = {
                "vehicleIds": ids.join(','),
                'time': parseInt(time, 10),
            };

            var url = "/clbs/adas/v/monitoring/score/batchExport";
            exportExcelUseForm(url, parameter); // post方法
            // exportExcelUseFormGet(url, parameter)  // get方法
        },
        // 导出功能所有列表
        exportAlarm: function () {
            if ($("#dataTable tbody tr td").hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据，无法导出");
                return;
            }
            if(getRecordsNum() > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            lifeCycleExpire.getCheckedNodes("treeDemo");
            if (!lifeCycleExpire.validates()) {
                return;
            }

            var timeInterval = $('#timeInterval').val();

            var reg = /-/g;
            var time = timeInterval.replace(reg, '');

            var parameter = {
                "groupId": groupId,
                'time': parseInt(time, 10),
            };
            var url = "/clbs/adas/v/monitoring/score/exportMonitorScoreList";

            exportExcelUseFormGet(url, parameter)  // get方法
        },
        // 导出单个详情
        exportDetail: function () {
            if ($("#dataTable tbody tr td").hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据，无法导出");
                return;
            }
            lifeCycleExpire.getCheckedNodes("treeDemo");
            if (!lifeCycleExpire.validates()) {
                return;
            }

            var timeInterval = $('#panel-head-q-date').html();

            var reg = /-/g;
            var time = timeInterval.replace(reg, '');
            var parameter = {
                "vehicleId": listId,
                'time': parseInt(time, 10),
            };

            var url = "/clbs/adas/v/monitoring/score/export";

            exportExcelUseFormGet(url, parameter)
        },
        // 参数检测
        validates: function () {
            return $("#lifeCycleForm").validate({
                rules: {
                    queryDateStr: {
                        required: true
                    },
                    groupSelect: {
                        zTreeCheckGroup: "treeDemo"
                    }
                },
                messages: {
                    queryDateStr: {
                        required: "请选择日期！",
                    },
                    groupSelect: {
                        zTreeCheckGroup: "请至少选择一个企业"
                    }
                }
            }).form();
        },
        // 获取选择tree对象
        getCheckedNodes: function (treeId) {
            var zTree = $.fn.zTree.getZTreeObj(treeId),
                nodes = zTree.getCheckedNodes(true);

            // 此处单选
            if (nodes && nodes.length > 0) {
                groupId = nodes[0].uuid;
            } else {
                if (!lifeCycleExpire.validates()) {
                    return;
                }
                console.log('未选择组织机构');
            }
        },
        // 表格绘制
        getTable: function (table) {

            $('.toggle-vis').prop('checked', true);
            myTable = $(table).DataTable({
                "columnDefs": [
                    {
                        "targets": 0,
                        "searchable": false // 禁止第一列参与搜索
                    },
                    {
                        "targets": [0, 1, 2, 7],
                        "orderable": false
                    }
                ],
                "destroy": true,
                "lengthChange": true,// 是否允许用户自定义显示数量
                "bPaginate": true, // 翻页功能
                "searching": true,// 本地搜索
                "Info": true,// 页脚信息
                "autoWidth": true,// 自动宽度
                "stripeClasses": [],
                "pageLength": 10,
                "lengthMenu": [5, 10, 20, 50, 100],
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
                "order": defaultSort,// 第一列排序图标改为默认
                "drawCallback": function (settings) {
                    // 重绘表格+更新筛选框
                    select.renderSelectList();
                    // 重绘筛选框后,将隐藏的行显示
                    $("#dataTable tbody tr").css({display: 'table-row'});
                },
                // 列表自定义排序（时分秒）
                customSort:function(a, b, direction, col, test){
                    if (col === 5){
                        var aValue ,bValue;
                        if (a === '-小时-分-秒'){
                            if (direction === 'asc'){
                                aValue = Number.MAX_VALUE;
                            } else{
                                aValue = -10000;
                            }
                        }else{
                            aValue = getSecondFromText(a)
                        }

                        if (b === '-小时-分-秒'){
                            if (direction === 'asc'){
                                bValue = Number.MAX_VALUE;
                            } else{
                                bValue = -10000;
                            }
                        }else{
                            bValue = getSecondFromText(b)
                        }

                        return aValue - bValue;
                    }
                    return test;
                }
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
            $("#search_button").on("click", function () {
                // 将筛选的数据全部展示
                $("#dataTable tbody tr").css({display: 'table-row'});

                var queryCondition = $("#simpleQueryParam").val();
                isUseParam = queryCondition ? true : false;
                myTable.column(1).search(queryCondition, false, false).draw();
            });

        },
        //刷新列表
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            var queryCondition = $("#simpleQueryParam").val();
            myTable.column(1).search(queryCondition, false, false).draw();
        },
        //获取当前时间，格式YYYY-MM-DD
        getNowFormatDate: function () {
            var date = new Date();
            var separator = "-";
            var year = date.getFullYear();
            var month = date.getMonth() + 1;
            var strDate = date.getDate();
            if (month >= 1 && month <= 9) {
                month = "0" + month;
            }
            if (strDate >= 0 && strDate <= 9) {
                strDate = "0" + strDate;
            }

            return (year + separator + month);
        },
        //获取上月时间，格式YYYY-MM
        getYesterMonth: function (del, needSecond) {
            var getNow = lifeCycleExpire.getNow;
            var myDate = new Date();
            var year = myDate.getFullYear();
            var month = myDate.getMonth() + 1;
            var date = myDate.getDate();
            var h = myDate.getHours();
            var m = myDate.getMinutes();
            var s = myDate.getSeconds();
            var now;
            if(month == 1){
                year = year -1;
                month = 12;
            }else {
                month = month - del; // 几月前
            }
            if (needSecond) {
                now = year + "-" + getNow(month) + "-" + getNow(date) + " " + getNow(h) + ":" + getNow(m) + ":" + getNow(s);
            } else {
                now = year + "-" + getNow(month)
            }
            return now;
        },
        getNow: function (s) {
            return s < 10 ? "0" + s : s;
        },
        // 查询
        inquireClick: function (number, type) {
            if (number === 0) {
                queryDateStr = lifeCycleExpire.getNowFormatDate();
                $('#timeInterval').val(queryDateStr);
            } else if (number === 1) {  // 查询
                queryDateStr = $('#timeInterval').val()
            }
            lifeCycleExpire.getCheckedNodes('treeDemo');
            if (!lifeCycleExpire.validates()) {
                return;
            }

            var reg = /-/g;
            var time = queryDateStr.replace(reg, '')
            var ajaxDataParam = {
                "groupId": groupId,
                "time": parseInt(time, 10)
            };

            var url = "/clbs/adas/v/monitoring/score/list";

            json_ajax("POST", url, "json", true, ajaxDataParam, lifeCycleExpire.getCallback);

        },
        // 表格数据回调
        getCallback: function (date) {
            if (date.success == true) {
                var obj = date.obj;

                var {monitorAggregateInfo, monitorScoreList} = obj;

                $('#stretch3-body').slideDown();
                // 监控对象评分统计
                lifeCycleExpire.updateInfo(monitorAggregateInfo);
                // 数据列表
                lifeCycleExpire.renderList(monitorScoreList);

            } else {
                if (date.msg != null) {
                    layer.msg(date.msg, {move: false});
                }
            }
        },
        // 更新info数据,主页内
        updateInfo: function (data) {
            if (data) {
                // 监控对象的综合平均得分(单位 + 四舍五入）
                var score = {
                    data: data.score,
                    dom: '#score-q1',
                    isRound: true,
                    isPositive: true
                };
                lifeCycleExpire.getNumber(score);

                // <监控对象综合得分环比+评语
                lifeCycleExpire.getRemark(data.scoreRingRatio, '#score-static-numberq1', '#score-static-numberqq1');

                lifeCycleExpire.getAlarm(data.alarmRingRatio, '#score-comment-statusq1');

                // 得分分布
                $('#score-comment-numberq1').removeClass('pad-q');
                $('#score-comment-numberq1').html(data.scoreDistributionStr);
                $('#score-comment-numberq1').css("color", "green");
                // 监控对象总数
                var monitorSize = {
                    data: data.monitorSize,
                    dom: '#monitorSize-q',
                };
                lifeCycleExpire.getNumber(monitorSize);

                // 日均行驶时长(保留两位小数）
                var averageTravelTime = {
                    data: data.averageTravelTime,
                    dom: '#averageTravelTime-q',
                };
                lifeCycleExpire.getNumber(averageTravelTime);

                // 触发报警次数
                var alarmTotal = {
                    data: data.alarmTotal,
                    dom: '#alarmTotal-q',
                };
                lifeCycleExpire.getNumber(alarmTotal);

                // 报警次数环比(保留两位小数/+-号，颜色）
                lifeCycleExpire.getRemark3(data.alarmRingRatio, '#alarmRingRatio-q');

                // 百公里触发报警数
                var averageTravelTime = {
                    data: data.hundredsAlarmTotal,
                    dom: '#hundredsAlarmTotal-q',
                    isFixed: true
                };
                lifeCycleExpire.getNumber(averageTravelTime);

                // 百公里触发报警数-环比
                lifeCycleExpire.getRemark3(data.hundredsAlarmRingRatio, '#hundredsAlarmRingRatio-q');

                // 平均车速
                var travelSpeed = {
                    data: data.travelSpeed,
                    dom: '#travelSpeed-q',
                    isFixed: true
                };
                $('#travelSpeed-q').attr('title', noZero(data.travelSpeed.toString()) + 'km/h');
                lifeCycleExpire.getNumber(travelSpeed);
            } else {
                lifeCycleExpire.dataClear();
            }
        },
        /**
         * 格式化数据（保留两位小数)
         * param: data: 数据
         *         dom： dom对象选择器
         *         unit：数据单位
         *         isFixed: 是否保留两位小数
         *         isRound: 是否四舍五入
         *         isPositive: 是否不要负数
         */
        getNumber: function (param) {
            var {data, dom, unit, isFixed, isRound, isPositive} = param;
            var finalData = data;
            if (data || data == 0) {
                if (isFixed) {
                    finalData = finalData.toFixed(2);
                }

                if (isRound) {
                    finalData = Math.round(finalData);
                }

                if (isPositive) {
                    finalData = finalData > 0 ? finalData : 0
                }

                // 当数据超出9999999展示9999999.99+
                if( dom === '#alarmTotal-q'
                    || dom === "#hundredsAlarmTotal-q"
                    || dom === "#travelSpeed-q"
                ) {
                    if(finalData > 9999999) {
                        finalData = "9999999.99+"
                    }
                }else if(dom === "#monitorSize-q") {
                    if(finalData > 9999999) {
                        finalData = "9999999+"
                    }
                }
                // 数据超出10位显示9999999.99+ 并拉宽数据栏
                if(dom === '#hundredsAlarmTotal-q2'
                    || dom === '#monitorSize-q2'
                ) {
                    if(finalData > 9999999) {
                        finalData = "9999999.99+";
                        $('#fenceUrl-q2').css('width', '360px');
                    }
                }else {
                    $('#fenceUrl-q2').css('width', '340px');
                }

                if (unit) {
                    finalData = noZero(finalData.toString()) + unit;
                }else {
                    finalData = noZero(finalData.toString());
                }


                $(dom).html(finalData);
            } else {
                $(dom).html('-' + unit);
            }

        },
        /**
         * 格式化数据（保留两位小数)
         * param: data: 数据
         *         dom： dom对象选择器
         *         isFixed: 是否保留两位小数
         *         isRound: 是否四舍五入
         *         isPositive: 是否不要负数
         */
        getNumber2: function (param) {
            var {data, dom, isFixed, isRound, isPositive} = param;
            var finalData = data;
            if (data || data == 0) {
                if (isFixed) {
                    finalData = finalData.toFixed(2);
                }

                if (isRound) {
                    finalData = Math.round(finalData);
                }

                if (isPositive) {
                    finalData = finalData > 0 ? finalData : 0
                }

                if (data == 0) {
                    finalData = "0";
                }

                if(dom === '#alarmTotal-q2') {
                    if(finalData > 9999999) {
                        finalData = "9999999.99+";
                        $('#fenceUrl-q2').css('width', '360px');
                    }
                }else {
                    $('#fenceUrl-q2').css('width', '340px');
                }

                $(dom).html(noZero(finalData.toString()));
            } else {
                $(dom).html('-');
            }

        },
        // 监控对象评语 四种结果返回："-", "0", "+1", "-1"
        getRemark: function (data, dom, dom2) {
            var content, color, content2;
            if (data == "-") {
                content = data
                content2 = '';
                color = "#5D5F63";
                $(dom).addClass('pad-q ');
            } else if (data == 0 || data == "0.00") {
                content = "与上月持平";
                content2 = "，请继续加强管理力度";
                color = "#5D5F63";
                $(dom).removeClass('pad-q ');
            } else {
                var reg1 = /\-/;
                var reg2 = /\+/;
                if (reg1.test(data)) {
                    data = data.replace(reg1, '');
                    content = "下降" + noZero(data) + '%';
                    content2 = "，请加强管理力度";
                    color = "red";
                    $(dom).removeClass('pad-q ');

                } else {
                    data = data.replace(reg2, '');
                    content = "上升" + noZero(data) + '%';
                    content2 = "，请继续加强管理力度";
                    color = "green";
                    $(dom).removeClass('pad-q ');

                }
                if (reg2.test(data)) {
                    data = data.replace(reg2, '');
                    content = "上升" + noZero(data) + '%';
                    content2 = "，请继续加强管理力度";
                    color = "green";
                    $(dom).removeClass('pad-q ');

                }
            }
            $(dom).html(content);
            $(dom).css({color});
            $(dom2).html(content2);
        },
        // 监控对象得分百分比+"评语" 四种结果返回："-", "0", "+1", "-1";
        // (控制评语变化，颜色变化，字间间隔变化）
        getRemark2: function (data, dom, dom2) {
            var content, content2;
            var color = '';
            var className = 'pad-q';
            if (data == "-") {
                content = data;
                $(dom).addClass(className);
            } else if (data == "0" || data == "0.00") {
                content = "与上月持平";
                content2 = "，请继续加强管理力度！"
                $(dom).removeClass(className);
            } else {
                var reg1 = /\-/;
                var reg2 = /\+/;
                if (reg1.test(data)) {
                    data = data.replace(reg1, '');
                    content = "下降" + noZero(data) + '%';
                    content2 = "，请加强管理力度！";
                    color = "red";
                    $(dom).removeClass(className);

                } else {
                    data = data.replace(reg2, '');
                    content = "上升" + noZero(data) + '%';
                    content2 = "，请继续加强管理力度！";
                    color = "green";
                    $(dom).removeClass(className);

                }
                if (reg2.test(data)) {
                    data = data.replace(reg2, '');
                    content = "上升" + noZero(data) + '%';
                    content2 = "，请继续加强管理力度！";
                    color = "green";
                    $(dom).removeClass(className);

                }
            }
            $(dom).html(content);
            $(dom).css({color});

            $(dom2).html(content2);
        },
        // 监控对象得分环比 四种结果返回："-", "0", "+1", "-1";
        // (控制颜色变化，增加正负号）
        getRemark3: function (data, dom) {
            var content;
            var color = '';

            if (data == "-") {
                content = data;
                color = "#5D5F63";
            } else if (data == "0" || data == "0.00") {
                content = "0%";
                color = "#5D5F63";

            } else {
                var reg1 = /\-/;
                var reg2 = /\+/;

                if (reg1.test(data)) {
                    data = data.replace(reg1, '');
                    // content = "-" + data + '%';
                    content = "-" + noZero(data) + '%';
                    color = "green";
                } else {

                    data = data.replace(reg2, '');
                    // content = "+" + data + '%';
                    content = "+" + noZero(data) + '%';
                    color = "red";

                }
                if (reg2.test(data)) {
                    data = data.replace(reg2, '');
                    // content = data + '%';
                    content = noZero(data) + '%';
                    color = "red";
                }
            }

            $(dom).html(content);
            $(dom).css({color});

        },
        // 报警数评语 四种结果返回："-", "0", "+1", "-1"
        getAlarm: function (data, dom) {
            var content, color;
            if (data == "-") {
                content = data
                color = "#5D5F63";
                $(dom).addClass('pad-q');
            } else if (data == '0' || data == "0.00") {
                content = "与上月持平";
                color = "#5D5F63";
                $(dom).removeClass('pad-q');
            } else {
                var reg1 = /\-/;
                var reg2 = /\+/;
                if (reg1.test(data)) {
                    data = data.replace(reg1, '');
                    // content = "下降" + data + '%';
                    content = "下降" + noZero(data) + '%';
                    color = "green";
                    $(dom).removeClass('pad-q');

                } else {

                    data = data.replace(reg2, '');
                    // content = "上升" + data + '%';
                    content = "上升" + noZero(data) + '%';
                    color = "red";
                    $(dom).removeClass('pad-q');
                }
                if (reg2.test(data)) {
                    data = data.replace(reg2, '');
                    // content = "上升" + data + '%';
                    content = "上升" + noZero(data) + '%';
                    color = "red";
                    $(dom).removeClass('pad-q');
                }
            }
            $(dom).html(content);
            $(dom).css({color});

        },
        // 清空主页详情数据
        dataClear: function () {
            // 监控对象的综合平均得分
            $('#score-q1').html('-');
            $('#score-q2').html('-');
            $('#score-spanq').addClass('pad-q2');
            $('#score-spanq').html('-');
            $('#score-spanq1').html('');

            // <监控对象综合得分环比+评语
            $('#score-static-numberq1').html('-');
            $('#score-static-numberq1').addClass('pad-q ');
            $('#score-static-numberq1').css('color', '#5D5F63');
            $('#score-static-numberqq1').html('');
            $('#score-static-numberq2').html('-');
            $('#score-static-numberq2').css("color", '#5D5F63');
            $('#score-static-numberq2').removeClass('pad-q');
            $('#score-static-numberq3').html('');

            // 触发报警数
            $('#score-comment-statusq1').html('-');
            $('#score-comment-statusq1').css("color", "#5D5F63");
            $('#score-comment-statusq1').addClass('pad-q ');

            $('#score-comment-percentq1').html('-');

            $('#score-comment-statusq2').html('-');
            $('#score-comment-statusq2').css("color", "#5D5F63");
            $('#score-comment-percentq2').html('-');

            // 得分分布
            $('#score-comment-numberq1').addClass('pad-q');
            $('#score-comment-numberq1').html('-');
            $('#score-comment-numberq1').css("color", "#5D5F63");

            // 监控对象总数
            $('#monitorSize-q').html('-');
            $('#monitorSize-q2').html('-');
            // 日均行驶时长
            $('#averageTravelTime-q').html('-');
            // 触发报警数环比
            $('#averageTravelTime-q2').html('-');
            $('#averageTravelTime-q2').css("color", '#5D5F63');

            // 触发报警次数
            $('#alarmTotal-q').html('-');
            $('#alarmTotal-q2').html('-');
            // 报警次数环比
            $('#alarmRingRatio-q').html('-');
            $('#alarmRingRatio-q').css("color", '#5D5F63');
            $('#alarmRingRatio-q2').html('-');
            $('#alarmRingRatio-q2').css("color", '#5D5F63');
            // 百公里触发报警数
            $('#hundredsAlarmTotal-q').html('-');
            $('#hundredsAlarmTotal-q2').html('-');
            // 百公里触发报警数-环比
            $('#hundredsAlarmRingRatio-q').html('-');
            $('#hundredsAlarmRingRatio-q').css("color", '#5D5F63');

            $('#hundredsAlarmRingRatio-q2').html('-');
            // 平均车速
            $('#travelSpeed-q').html('-');
        },
        // 清空详情弹框页数据
        dataClear2: function () {
            // 监控对象的综合平均得分
            $('#score-q2').html('-');
            $('#score-spanq').html('-');
            $("#score-spanq").addClass('pad-q2');
            $('#score-spanq1').html('');

            // <监控对象综合得分环比+评语
            $('#score-static-numberq2').html('-');
            $('#score-static-numberq2').css("color", '#5D5F63');
            $('#score-static-numberq2').removeClass('pad-q');
            $('#score-static-numberq3').html('');

            // 触发报警数
            $('#score-comment-statusq2').html('-');
            $('#score-comment-statusq2').css("color", "#5D5F63");
            $('#score-comment-percentq2').html('-');

            // 监控对象总数
            $('#monitorSize-q2').html('-');
            // 触发报警数环比
            $('#averageTravelTime-q2').html('-');
            $('#averageTravelTime-q2').css("color", '#5D5F63');

            // 百公里触发报警数
            $('#alarmTotal-q2').html('-');
            // 百公里触发报警数环比
            $('#alarmRingRatio-q2').html('-');
            $('#alarmRingRatio-q2').css("color", '#5D5F63');
            // 百公里触发报警数
            $('#hundredsAlarmTotal-q2').html('-');
            // 百公里触发报警数-环比
            $('#hundredsAlarmRingRatio-q2').html('-');

            // 概况下：车辆详情
            // 监控对象
            $('#brand-q').html('-');
            // 车牌颜色
            $('#plateColor-q').html('-');
            // 测试企业
            $('#groupName-q').html('-');
            // 车架号
            $('#chassisNumber-q').html('-');
            // 发证日日期
            $('#licenseIssuanceDate-q').html('-');
            // 运营类型
            $('#purposeCategory-q').html('-');
            // 车辆状态
            $('#isStart-q').html('-');
            // 使用性质
            $('#usingNature-q').html('-');
            // 行驶有效期至
            $('#registrationEndDate-q').html('-')
        },
        // 保留两位小时、正负号、颜色切换
        toFormat: function (data, dom) {
            if (data || data == 0) {
                var innerH;
                if (data == 0) {
                    $(dom).html('0.00%');
                } else if (data > 0 || data < 0) {
                    innerH = data;
                    if (data > 0) {
                        $(dom).html('+' + innerH + '%');
                        $(dom).css({color: 'green'});
                    } else if (data < 0) {
                        $(dom).html(innerH + '%');
                        $(dom).css({color: 'red'});
                    }

                } else {
                    $(dom).html(data);
                }
            } else {
                $(dom).html('-')
            }
        },
        // 列表数据加载
        renderList: function (data, bool) {
            var dataListArray = [];//用来储存显示数据
            if (data != null && data.length != 0) {
                $("#exportAlarm-q2").removeAttr("disabled");
                for (var i = 0; i < data.length; i++) {
                    var recordData = data[i];

                    var dateList =
                        [
                            i + 1,
                            recordData.vehicleName ? recordData.vehicleName : "-", // 监控对象
                            recordData.purposeCategoryName ? recordData.purposeCategoryName : "-", // 运营类型
                            recordData.travelMile, // 行驶里程
                            recordData.travelNum, // 行驶次数
                            recordData.averageTravelTime, // 平均行驶时长
                            recordData.travelSpeed, // 平均速度
                            recordData.groupName ? recordData.groupName : "-", // 所属企业
                            recordData.alarmTotal, // 报警数
                            Math.round(recordData.score), // 综合得分
                            recordData.vehicleId // 车辆id：用以点击事件
                        ];
                    dataListArray.push(dateList);

                }
                lifeCycleExpire.reloadData(dataListArray);
                if(!bool) {
                    $("#simpleQueryParam").val("");
                }

            } else {
                lifeCycleExpire.reloadData(dataListArray);
                $("#simpleQueryParam").val("");

                $("#exportAlarm-q2").attr("disabled", "disabled");

            }
        },
        // table数据重新加载
        reloadData: function (dataList) {
            var currentPage = myTable.page();
            myTable.clear();
            myTable.rows.add(dataList);
            myTable.page(currentPage).draw(false);
            myTable.column(1).search('', false, false).page(currentPage).draw();
        },
        // 列表点击事件
        listClick: function () {
            event.stopPropagation();
            // 初始化为概况激活状态
            $('#general-q1').show();
            $('#general-q2').show();

            $('#echarts-q').hide();
            $('#echarts-q3').hide();

            $('#panel-toggle-q1').addClass('active');
            $('#panel-toggle-q2').removeClass('active');

            var data = myTable.row(this).data();

            // tr点击切换颜色
            if (!trColor) { // 初次点击，当前点击对象激活，弹框出现,trColor存值
                $(this).addClass('active');
                $('#popup-q').addClass('show');
                trColor = data[10];

            } else { // 非初次点击，trColor 已经存值
                if (trColor == data[10]) { // 点击的同一条时,点击对象关闭，弹框消失，trColor置空
                    $('#dataTable tbody tr').removeClass('active');
                    $('#popup-q').removeClass('show');
                    trColor = "";

                } else { // 点击不同条时：激活点击那条，其他关闭，弹框更新；
                    $(this).siblings().removeClass('active');
                    $(this).addClass('active');
                    $('#popup-q').addClass('show');
                    trColor = data[10];

                }
            }

            if (data && data.length > 0) {
                // 获取车辆id
                listId = data[10]; // 全局存储listId
                // 获取查询时间
                var time = $('#timeInterval').val();
                $('#panel-head-q-date').html(time); // 更新时间
                var reg = /-/g;
                var time1 = time.replace(reg, '');

                lifeCycleExpire.listDetailAjax(listId, time1);
            }
        },
        // 列表详情请求ajax + 明细列表请求
        listDetailAjax: function (vehicleId, time) {
            var param = {
                vehicleId: vehicleId,
                "time": time
            };
            // 列表详情查询
            var url = "/clbs/adas/v/monitoring/score/scoreInfo";
            json_ajax("POST", url, "json", true, param, lifeCycleExpire.listClickCallback);

        },
        // 列表点击回调
        listClickCallback: function (data) {
            if (data.success == true) {
                var obj = data.obj;
                if (obj) {
                    // 数据详情清空
                    lifeCycleExpire.dataClear2();
                    // 概况上
                    lifeCycleExpire.updateGeneralUp(obj);
                    // 概况下
                    lifeCycleExpire.updateGeneralLower(obj);

                    // echarts
                    var alarmMap = obj.alarmMap;
                    // 数据格式化： 形如 [{name: 'a', value: 1}]
                    var echartsData = [];

                    if (alarmMap && Object.keys(alarmMap).length > 0) {
                        Object.keys(alarmMap).map(function (d, i) {
                            echartsData.push({name: d, value: alarmMap[d]})
                        });

                        // 排序
                        function compare(property) {
                            return function (a, b) {
                                var value1 = a[property];
                                var value2 = b[property];
                                return value2 - value1;
                            }
                        }

                        echartsData.sort(compare('value'));
                        // echarts渲染
                        lifeCycleExpire.echartsInit($('#echarts-q1'), echartsData);
                    } else {
                        lifeCycleExpire.echartsInit($('#echarts-q1'), []);
                    }

                    isA = true; // 概况有数据
                } else {
                    // 数据详情清空
                    lifeCycleExpire.dataClear2();
                    // echarts清空
                    if(myChart) {
                        myChart.clear();
                        myChart.setOption(
                            lifeCycleExpire.echartsUpdate([])
                        );
                    }else {
                        console.log('myChart对象出问题啦');
                        myChart = echarts.init($('#echarts-q1').get(0));
                        myChart.clear();
                        myChart.setOption(
                            lifeCycleExpire.echartsUpdate([])
                        );
                    }
                    isA = false; // 概况无数据
                }
            } else {

                // 数据详情清空
                lifeCycleExpire.dataClear2();
                // echarts清空
                if(myChart) {
                    myChart.clear();
                    myChart.setOption(
                        lifeCycleExpire.echartsUpdate([])
                    );
                }else {
                    console.log('myChart对象出问题啦：'+ myChart)
                    myChart = echarts.init($('#echarts-q1').get(0));
                    myChart.clear();
                    myChart.setOption(
                        lifeCycleExpire.echartsUpdate([])
                    );
                }
            }
            // 明细列表调用
            drawListDetail.getListData();

        },
        // 弹框内详情：概况上
        updateGeneralUp: function (obj) {
            // 监控对象的综合平均得分(单位 + 四舍五入）
            if (!obj) {
                return;
            }
            var score = {
                data: obj.score,
                dom: '#score-q2',
                isRound: true,
                isPositive: true
            };
            lifeCycleExpire.getNumber(score);

            // <监控对象综合得分环比+评语
            lifeCycleExpire.getRemark2(obj.scoreRingRatio, '#score-static-numberq2', "#score-static-numberq3");

            // 触发报警数+评语
            lifeCycleExpire.getAlarm(obj.alarmRingRatio, '#score-comment-statusq2');

            // 监控对象的综合平均得分(单位 + 四舍五入）
            var score = {
                data: obj.score,
                dom: '#score-spanq',
                unit: '分',
                isRound: true,
                isPositive: true
            };
            lifeCycleExpire.getNumber(score);
            $('#score-spanq').removeClass('pad-q2');
            $('#score-spanq1').html('，要继续争优哦！定期检查保养有助于提高车辆安全性能。');

            // 触发报警数
            var monitorSize = {
                data: obj.alarmTotal,
                dom: '#monitorSize-q2',
            };
            lifeCycleExpire.getNumber(monitorSize);

            // 触发报警数环比
            lifeCycleExpire.getRemark3(obj.alarmRingRatio, '#averageTravelTime-q2');

            // 百公里触发报警数（返回数字）
            var alarmTotal = {
                data: obj.hundredsAlarmTotal,
                isFixed: true,
                dom: '#alarmTotal-q2',
            };
            lifeCycleExpire.getNumber2(alarmTotal);

            // 百公里触发报警数环比(保留两位小数/+-号，颜色）
            lifeCycleExpire.getRemark3(obj.hundredsAlarmRingRatio, '#alarmRingRatio-q2');
            // 行驶总里程数
            var travelMile = {
                data: obj.travelMile,
                dom: '#hundredsAlarmTotal-q2',
            };
            $('#hundredsAlarmTotal-q2').attr("title", noZero(obj.travelMile.toString()) + 'km/h');
            lifeCycleExpire.getNumber(travelMile);
            // 行驶时长
            var averageTravelTime = {
                data: obj.averageTravelTime,
                dom: '#hundredsAlarmRingRatio-q2',
            };
            lifeCycleExpire.getNumber(averageTravelTime);
        },
        // 概况下
        updateGeneralLower: function (obj) {
            if (!obj) {
                return;
            }
            // 监控对象
            obj.brand ? $('#brand-q').html(obj.brand) : $('#brand-q').html("-");
            // 车牌颜色
            obj.plateColorStr ? $('#plateColor-q').html(obj.plateColorStr) : $('#plateColor-q').html('-');
            // 所属企业
            obj.groupName ? $('#groupName-q').html(obj.groupName) : $('#groupName-q').html('-');
            // 车架号
            obj.chassisNumber ? $('#chassisNumber-q').html(obj.chassisNumber) : $('#chassisNumber').html('-');
            // 发证日日期
            obj.licenseIssuanceDate ?
                $('#licenseIssuanceDate-q').html(obj.licenseIssuanceDate) : $('#licenseIssuanceDate-q').html('-');
            // 运营类型
            obj.purposeCategory ?
                $('#purposeCategory-q').html(obj.purposeCategory) : $('#purposeCategory-q').html('-');
            // 车辆状态
            obj.startStatus ? $('#isStart-q').html(obj.startStatus) : $('#isStart-q').html('-');
            // 使用性质
            obj.usingNature ? $('#usingNature-q').html(obj.usingNature) : $('#usingNature-q').html('-');
            // 行驶有效期至
            obj.registrationEndDate ? $('#registrationEndDate-q').html(obj.registrationEndDate) : $('#registrationEndDate-q').html('-');

            // 图片地址 如无返回使用默认地址
            obj.vehiclePhoto ? $('.panel-display-q1').css({
                    "background-image": "url(" + obj.vehiclePhoto + ")",
                    "background-size": "200px 200px"
                }) :
                $('.panel-display-q1').css({
                    "background-image": "url(/clbs/resources/img/defaultCar.svg)",
                    "background-size": "80px 80px"
                });
        },
        // 比对时间
        getIntervalMonth: function (startDateStr, endDateStr) {
            if (startDateStr == null || endDateStr == null) {
                return;
            }
            var startDate = new Date(startDateStr);
            var endDate = new Date(endDateStr);
            var startMonth = startDate.getMonth();
            var endMonth = endDate.getMonth();
            var intervalMonth = (endDate.getFullYear() * 12 + endMonth) - (startDate.getFullYear() * 12 + startMonth);
            return intervalMonth;
        },
        // 月份切换控件：初始化
        monthToggleInit: function (dom) {
            var date = new Date();

            var year = date.getFullYear();
            var month = date.getMonth() + 1; // 因日期中的月份表示为0-11，所以要显示正确的月份，需要 + 1

            month = month < 10 ? "0" + month : month;
            var newDate = year + '-' + month; // "2019-03"

            dom.html(newDate);
        },
        // 月份切换控件：改变
        monthToggleChange: function (dom, type) {
            var date = dom.html();
            var currentDate = new Date(date);
            var lastDate;
            if (type == "+") {
                lastDate = currentDate.setMonth(currentDate.getMonth() + 1); // 输出日期格式为毫秒形式1551398400000
            } else if (type == "-") {
                lastDate = currentDate.setMonth(currentDate.getMonth() - 1); // 输出日期格式为毫秒形式1551398400000
            } else {
                console.log('请指明操作类别');
                return;
            }
            lastDate = new Date(lastDate);
            var lastYear = lastDate.getFullYear();
            var lastMonth = lastDate.getMonth() + 1; // 因日期中的月份表示为0-11，所以要显示正确的月份，需要 + 1

            lastMonth = lastMonth < 10 ? "0" + lastMonth : lastMonth;

            lastDate = lastYear + '-' + lastMonth; // "2019-03"
            var timeControl = lifeCycleExpire.getNowFormatDate();
            var control = lifeCycleExpire.getIntervalMonth(lastDate, timeControl);

            if (control == 0) {
                console.log('不能往后点了！')
                // $('#panel-head-q-detail').css({'cursor': 'not-allowed'});
                return;
            } else {
                $('#panel-head-q-detail').css({'cursor': 'pointer'});
            }
            dom.html(lastDate);

            // 更新数据
            var reg = /-/g;
            var time = lastDate.replace(reg, '');
            lifeCycleExpire.listDetailAjax(listId, time);
        },
        // echarts图表option
        echartsOption: {
            color: ['#3398DB'],
            tooltip: {
                trigger: 'axis',
                axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                    type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                },
                confine: true
            },
            grid: {
                containLabel: true,
                width: '100%',
                left: 0,
                right: 40,
                bottom: 100,
            },
            xAxis: {
                type: 'category',
                data: [], // 形如：['a', 'b']
                axisLabel: {
                    show: true,
                    interval: 0,
                    rotate: -60,
                    textStyle: {
                        color: "#2f2f2f",
                        fontSize: 12,
                        fontWeight: 'lighter',
                    },
                },
                axisLine: { //坐标轴轴线
                    show: false
                },
                axisTick: { //坐标轴刻度
                    show: false
                }
            },
            yAxis: {
                type: 'value'
            },
            series: [
                {
                    name: '报警数据排行',
                    type: 'bar',
                    barWidth: 50,
                    data: [], // 形如 [10, 52]
                    itemStyle: {
                        normal: {
                            color: '#198ef0'
                        }
                    },
                    label: {
                        normal: {
                            show: true,
                            position: 'top',
                        }
                    }
                }
            ]
        },
        // echarts数据更新
        echartsUpdate: function (data) {
            var option = lifeCycleExpire.echartsOption;
            var names = [];
            var values = [];
            if (data && data.length >= 0) {
                data.map(function (d, i) {

                    names.push(d.name);
                    values.push(d.value);
                });
                option.xAxis.data = names;
                option.series[0].data = values;
                // 数据大于十条有滚动条
                if (data.length > 10) {
                    // option.dataZoom = {
                    //     show: true,
                    //     realtime: true,
                    //     y: "95%",
                    //     height: 10,
                    //     // start: 0,
                    //     // end: 100
                    //     startValue: 0,
                    //     endValue: 9
                    // };
                    // var width = $('#echarts-q1').width();
                    var newWidth = data.length * 95;

                    $('#echarts-q1').width(newWidth);

                } else {
                    // option.dataZoom = null
                    $('#echarts-q1').width(770);
                }
            }
            return option;
        },
        // echarts初始化
        echartsInit: function (dom, data) {

            myChart = echarts.init(dom.get(0));
            myChart.clear();
            myChart.setOption(
                lifeCycleExpire.echartsUpdate(data)
            );
            myChart.resize();
        },
    };

    // 自定义选择监控对象
    select = {
        checkall: function (that) {
            if (that.prop("checked")) {
                $("input[name=objCheck-q]:checkbox").each(function () {
                    $(this).prop("checked", true);
                });
                $("#dataTable tbody tr").css({display: 'table-row'});

            } else {
                $("input[name=objCheck-q]:checkbox").each(function () {
                    $("input[name=objCheck-q]").removeAttr("checked")
                });
                $("#dataTable tbody tr").css({display: 'none'});
            }
        },
        chose: function () {
            //显示隐藏列
            $('.toggle-viq').off('change').on('change', function (e) {

                var nowDat = e.target.value;

                var table = $("#dataTable tr td:nth-child(2)");

                for (var i = 0; i < table.length; i++) {
                    var thisTr = $(table[i]).parents('tr');
                    var data = myTable.row(thisTr).data();

                    if (nowDat == data[10]) {
                        if (thisTr.css('display') == 'table-row') {
                            thisTr.css({display: 'none'});
                        } else {
                            thisTr.css({display: 'table-row'});
                        }
                    }
                }
            });
        },
        renderSelectList: function () {
            var menu_text = "";

            var table = $("#dataTable tr td:nth-child(2)");

            var vehis = []; // 车辆id（每次置空防止累加）
            if (table.length == 0) {
                menu_text = "<li>暂无数据</li>"
            } else {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"choose-all\" data-column=\"" + parseInt(1) + "\"  />全部</label></li>"
                for (var i = 0; i < table.length; i++) {
                    var thisTr = $(table[i]).parents('tr');
                    var data = myTable.row(thisTr).data();
                    menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-viq\" name=\"objCheck-q\" value=\"" + data[10] + "\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"

                    vehis.push(data[10])
                }
            }
            vehicleIds = vehis;
            $("#dropDown-q").html(menu_text);
        },
        showDropDown: function () {
            event.stopPropagation();
            if ($('#dropDown-q').hasClass('show')) {
                $('#dropDown-q').removeClass('show');
            } else {
                // 不要重复渲染
                if (!isRender) {
                    //显示隐藏列
                    select.renderSelectList();
                    isRender = true; // 禁止重复渲染
                }
                $('#dropDown-q').addClass('show');
                select.chose();
                // 全选功能
                $('.choose-all').bind('click', function () {
                    select.checkall($(this));
                });
            }
        }
    };
    // 渲染明细列表
    drawListDetail = {
        // 初始化
        init: function (dom) {
            dom.html('<tr class="" style="pointer-events: none">' +
                '<td valign="top" colspan="12" class="dataTables_empty" height="143px" style="pointer-events: none">我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？</td>' +
                '</tr>');
        },
        // 获取列表数据
        getListData: function () {
            var time = $('#panel-head-q-date').html();
            var reg = /-/g;
            time = time.replace(reg, '');

            var param = {
                searchAfter: "", // es查询下页带的角标参数,分页使用（第一次不传
                limit: 10, // 每页条数
                time,
                vehicleId: listId,
            };

            // 列表详情查询
            var url = "/clbs/adas/v/monitoring/score/monitorAlarmInfo";
            json_ajax("POST", url, "json", true, param, function (data) {
                if (data.success == true) {
                    count = -1; // 此时count为0
                    var obj = data.obj;

                    var monitorAlarmInfoList = obj.monitorAlarmInfoList;
                    if(monitorAlarmInfoList.length > 0) {
                        isB = true; // 明细有数据
                    } else {
                        isB = false; // 明细无数据
                    }
                    searchAfter = obj.searchAfter.join(','); // 存角标
                    isGoOn = obj.searchAfter[0] === null ? false : true;

                    var deal = drawListDetail.dealData(monitorAlarmInfoList);
                    drawListDetail.updateList($('#dataTable2-q>tbody'), deal);

                } else {
                    console.log('请求失败')
                }

            });

            if(!isA && !isB) {
                // 概况+明细均无数据时，导出按钮禁用
                $("#exportAlarm-q3").css({"cursor": 'not-allowed', "background": "#ccc"});
                $("#exportAlarm-q3").attr("disabled", "disabled");

            }else {
                // 概况+明细都有数据时，导出按钮正常使用
                $("#exportAlarm-q3").css({"cursor": 'pointer', "background": "#6dcff6"});
                $("#exportAlarm-q3").attr("disabled", false);
            }
        },
        secondRender: function () {
            var time = $('#panel-head-q-date').html();

            var reg = /-/g;
            time = time.replace(reg, '');
            var page = searchAfter ? searchAfter : "";

            var param = {
                searchAfter: page, // es查询下页带的角标参数,分页使用（第一次不传
                limit: 10, // 每页条数
                time,
                vehicleId: listId,
            };

            // 列表详情查询
            var url = "/clbs/adas/v/monitoring/score/monitorAlarmInfo";
            json_ajax("POST", url, "json", true, param, function (data) {
                if (data.success == true) {
                    var obj = data.obj;
                    var monitorAlarmInfoList = obj.monitorAlarmInfoList;
                    searchAfter = obj.searchAfter.join(','); // 存角标
                    isGoOn = obj.searchAfter[0] === null ? false : true; // 当searchAfter为空时，停止滚动搜索

                    var deal = drawListDetail.dealData(monitorAlarmInfoList);

                    drawListDetail.addList($('#dataTable2-q>tbody'), deal);

                } else {
                    console.log('请求失败')
                }

            });
        },
        addList: function (dom, data) {
            var trAvant = dom.html();

            var trs = "";

            if (data && data.length > 0) {
                data.map(function (d, i) {
                    var tds = "";
                    if (d && d.length > 0) {
                        d.map(function (dd) {
                            tds += '<td>' + dd + '</td>';
                        });
                    }
                    trs += '<tr class="">' + tds + '</tr>';
                });
                dom.html(trAvant + trs);
            } else {
                dom.html(trAvant);
            }
        },
        // 格式化列表数据
        dealData: function (data) {
            var dataList = [];//用来储存显示数据
            if (data != null && data.length != 0) {
                $("#exportAlarm-q2").removeAttr("disabled");
                count++;
                for (var i = 0; i < data.length; i++) {
                    var recordData = data[i];

                    var dateList =
                        [
                            (i + 1) + (count * 10),
                            recordData.driverName ? recordData.driverName : "-", // 驾驶员
                            recordData.eventType ? recordData.eventType : "-", // 报警事件
                            recordData.riskLevel ? recordData.riskLevel : "-", // 风险等级
                            recordData.eventTime ? recordData.eventTime : "-", // 报警时间
                            recordData.speed ? recordData.speed : "-", // 车辆速度
                            recordData.address ? recordData.address : "-", // 报警位置
                        ];
                    dataList.push(dateList);
                }
            } else {
                console.log('暂无数据')
            }

            return dataList
        },
        // 更新列表
        updateList: function (dom, data) {
            var trs = "";
            if (data && data.length > 0) {
                data.map(function (d, i) {
                    var tds = "";
                    if (d && d.length > 0) {
                        d.map(function (dd) {
                            tds += '<td style="">' + dd + '</td>';
                        });
                    }
                    trs += '<tr class="">' + tds + '</tr>';
                });
                dom.html(trs);
            } else {
                dom.html('<tr class="" style="pointer-events: none">' +
                    '<td valign="top" colspan="12" class="dataTables_empty" height="143px">我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？</td>' +
                    '</tr>');
            }
        },
    };

    $(function () {
        // 初始化
        lifeCycleExpire.init();
        // 时间框初始化渲染
        laydate.render({
            elem: '#timeInterval',
            theme: '#6dcff6',
            format: 'yyyy-MM',
            type: "month",
            max: lifeCycleExpire.getYesterMonth(1, true),
            // 点击月份关闭弹框
            ready: function (date) {
                $("#layui-laydate1").off('click').on('click', '.laydate-month-list li', function () {
                    $("#layui-laydate1").remove();
                });
            },
            // 点击月份立即改变input值
            change: function (value, dates, edate) {
                var final;
                // 获取选择的年月
                var year = dates.year;
                var month = dates.month;
                // 获取当前实际的年月
                var getNow = lifeCycleExpire.getNow;
                var myDate = new Date();
                var nowYear = myDate.getFullYear();
                var nowMonth = myDate.getMonth() + 1;

                if (year > nowYear) {
                    final = lifeCycleExpire.getYesterMonth(1);
                } else if (year == nowYear) {
                    if (month >= nowMonth) {
                        final = lifeCycleExpire.getYesterMonth(1)
                    } else {
                        final = value;
                    }
                } else {
                    final = value;
                }

                $('#timeInterval').val(final);
            },
            btns: ['clear', 'confirm'],
        });

        // 时间框时间格式化
        $("#timeInterval").val(lifeCycleExpire.getYesterMonth(1));

        // 组织树input快速清空功能
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;

            if (id == 'groupSelect') {
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                search_ztree('treeDemo', 'groupSelect', 'group');
                var nodes = treeObj.getCheckedNodes(true);
                treeObj.checkNode(nodes[0], false, null);
            }
        });
        // 组织树下拉展示
        $("#groupSelect").bind("click", showMenuContent);

        // 搜索结果模糊查询
        $("#groupSelect").on('input propertychange', function (value) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.checkAllNodes(false);
            search_ztree('treeDemo', 'groupSelect', 'group');
        });

        // 主页面列表
        lifeCycleExpire.getTable('#dataTable');
        // 主页面刷新列表数据
        $("#refreshTable").bind("click", lifeCycleExpire.refreshTable);
        // 列表：点击每列出现详情
        $('#dataTable').on('click', 'tbody tr', lifeCycleExpire.listClick);

        // 列表：关闭详情页
        $("#panel-head-q-close1, #panel-final-close").on('click', function () {
            var timer;
            $('#popup-q').removeClass('show');
            $("#dataTable tbody tr").removeClass('active');

            trColor = ""; // 置空存储id
            if(timer) {
                clearTimeout(timer);
            }
            timer = setTimeout(function() {
                // 初始化为概况激活状态
                $('#general-q1').show();
                $('#general-q2').show();

                $('#echarts-q').hide();
                $('#echarts-q3').hide();

                $('#panel-toggle-q1').addClass('active');
                $('#panel-toggle-q2').removeClass('active');
            }, 800);
        });

        // 点击页面其他区域 监控对象弹框隐藏
        $(document).bind("click", function (event) {
            if (!$('#dropDown-q').is(event.target) && $('#dropDown-q').has(event.target).length == 0) {
                $('#dropDown-q').removeClass('show');
            }
            if (!$('#popup-q').is(event.target) && $('#popup-q').has(event.target).length == 0) {
                var timer;
                trColor = ""; // 置空存储id
                $('#popup-q').removeClass('show');
                $('#dataTable tbody tr').removeClass('active');
                if(timer) {
                    clearTimeout(timer);
                }
                timer = setTimeout(function() {
                    // 初始化为概况激活状态
                    $('#general-q1').show();
                    $('#general-q2').show();

                    $('#echarts-q').hide();
                    $('#echarts-q3').hide();

                    $('#panel-toggle-q1').addClass('active');
                    $('#panel-toggle-q2').removeClass('active');
                }, 800)

            }
        });
        // 弹框内：日期初始化
        lifeCycleExpire.monthToggleInit($('#panel-head-q-date'));
        // 弹框内：月份切换
        $("#panel-head-q-general").bind('click', function () {
            $('#echarts-q2').scrollTop(0);
            lifeCycleExpire.monthToggleChange($('#panel-head-q-date'), "-");
        });
        $("#panel-head-q-detail").bind('click', function () {
            $('#echarts-q2').scrollTop(0);
            lifeCycleExpire.monthToggleChange($('#panel-head-q-date'), "+");
        });

        // 切换明细概况
        $('.panel-toggle-q').bind('click', function () {
            $('.panel-toggle-q').removeClass('active');
            $(this).addClass('active');

        });
        $('#panel-toggle-q2').bind('click', function () {
            $('#general-q1').hide();
            $('#general-q2').hide();
            $('#echarts-q').show();
            $('#echarts-q3').show();
            $('#echarts-q2').scrollTop(0);
            console.log(1111)
        });

        $('#panel-toggle-q1').bind('click', function () {
            $('#general-q1').show();
            $('#general-q2').show();

            $('#echarts-q').hide();
            $('#echarts-q3').hide();
        });

        // 弹框内列表 初始化 滚动到底部请求
        drawListDetail.init($('#dataTable2-q>tbody'));

        // $('#echarts-q2').scroll(function () {
        $('#echarts-q2').on("scroll", function () {
            console.log('scroll')
            // 当滚动到260像素时， 加载新内容
            var scrollT = $(this).scrollTop();
            var tableT = $('#dataTable2-q').height();
            var wrapT = $('#echarts-q2').height();

            // if (scrollT + wrapT >= tableT) {
            if(scrollT > (tableT - wrapT) - 50 ) {
                console.log(2)
                if (scrollTimer) {
                    clearTimeout(scrollTimer);
                    scrollTimer = null;
                    console.log(3)
                }
                if (isGoOn) {
                    console.log(4)
                    // 二次滚动加载时，当角标不为空时，继续加载
                    scrollTimer = setTimeout(function () {
                        console.log(1,'scroll')
                        drawListDetail.secondRender();
                    }, 310)
                }

            }
        });

        //导出
        $("#exportAlarm-q1").bind("click", lifeCycleExpire.exportAll); // 批量导出
        $("#exportAlarm-q2").bind("click", lifeCycleExpire.exportAlarm); // 导出所有列表
        $("#exportAlarm-q3").bind("click", lifeCycleExpire.exportDetail); // 导出单个监控对象

        $(document).ready(function () {
            $('table#dataTable').dataTable({
                //跟数组下标一样，第一列从0开始，这里表格初始化时，第四列默认降序
                "order": [[3, "desc"]]
            });
        });
    })
})(window, $)



