(function (window, $) {
    jlmrCommonFun = {
        tablePageLength: [10,20,50], //表格每页设置数据
        columnDef: [{
            //第一列，用来显示序号
            "searchable": false,
            "orderable": false,
            "targets": 0
        }],
        initTreeSetting: function(treeId, dataFilter, beforeClick, asyncSuccess, beforeCheck,onCheck,onNodeCreated,onExpand){
            if(!treeId) return false;
            var setting = {
                async: {
                    url: jlmrCommonFun.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: dataFilter
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
                    beforeClick: beforeClick,
                    onAsyncSuccess: asyncSuccess,
                    beforeCheck: beforeCheck,
                    onCheck: onCheck,
                    onNodeCreated: onNodeCreated,
                    onExpand: onExpand
                }
            }
            $.fn.zTree.init($("#" + treeId), setting, null);
        }, // 停运车辆、报警车辆上传记录，车辆树设置
        getTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle";
            }
        },
        serializeObject: function (form) {
            var a,o,h,i,e;
            a = form.serializeArray();
            o={};
            h=o.hasOwnProperty;
            for(i=0;i<a.length;i++){
                e=a[i];
                if(!h.call(o,e.name)){
                    o[e.name]=e.value;
                }
            }
            return o;
        },
        //获取当前时间
        getTheCurrentTime: function () {
            var _start = '', _end = ''
            var nowDate = new Date();
            _start = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate());
            _end = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate());
            var atime = $("#atime").val();
            if (atime != undefined && atime != "") {
                startTime = atime;
            }
            return {start: _start, end: _end};
        },
        //获取月份
        getTheMonth: function (month) {
            var m = month;
            if (month.toString().length == 1) {
                m = "0" + month;
            }
            return m;
        },
        //获取之前的天数
        getBeforeDay: function (day,times) {
            var _start = '', _end = '';
            var timer = times.split('--');
            var startValue = timer[0];
            var endValue = timer[1];
            if (startValue == "" || endValue == "") {
                var today = new Date();
                var targetday_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * day;
                today.setTime(targetday_milliseconds); // 注意，这行是关键代码
                var tYear = today.getFullYear();
                var tMonth = today.getMonth();
                var tDate = today.getDate();
                tMonth = jlmrCommonFun.getTheMonth(tMonth + 1);
                tDate = jlmrCommonFun.getTheMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate;
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24
                    * parseInt(num);
                today.setTime(end_milliseconds); // 注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = jlmrCommonFun.getTheMonth(endMonth + 1);
                endDate = jlmrCommonFun.getTheMonth(endDate);
                _end = endYear + "-" + endMonth + "-" + endDate;
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = jlmrCommonFun.getTheMonth(vMonth + 1);
                vDate = jlmrCommonFun.getTheMonth(vDate);
                _start = vYear + "-" + vMonth + "-" + vDate;
                if (day == 1) {
                    _end = vYear + "-" + vMonth + "-" + vDate;
                } else {
                    var endNum = -1;
                    var vendtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * parseInt(endNum);
                    var dateEnd = new Date();
                    dateEnd.setTime(vendtoday_milliseconds);
                    var vendYear = dateEnd.getFullYear();
                    var vendMonth = dateEnd.getMonth();
                    var vendDate = dateEnd.getDate();
                    vendMonth = jlmrCommonFun.getTheMonth(vendMonth + 1);
                    vendDate = jlmrCommonFun.getTheMonth(vendDate);
                    _end = vendYear + "-" + vendMonth + "-" + vendDate;
                }
            }
            return {start: _start, end: _end};
        },
        getNowDay: function () {
            var nowT = new Date();
            var yearT = nowT.getFullYear();
            var monthT = nowT.getMonth() + 1;
            var dayT = nowT.getDate();
            monthT = monthT <= 9 ? '0' + monthT : monthT;
            dayT = dayT <= 9 ? '0' + dayT : dayT;
            var nowDay = yearT + '-' + monthT + '-' + dayT;
            return nowDay;
        },
        // 判断时间范围
        compareTime:function(sDate1, sDate2){
            var dateSpan,
                tempDate,
                iDays;
            sDate1 = Date.parse(sDate1);
            sDate2 = Date.parse(sDate2);
            dateSpan = sDate2 - sDate1;
            dateSpan = Math.abs(dateSpan);
            iDays = Math.floor(dateSpan / (24 * 3600 * 1000));
            // return iDays
            return (iDays+1) //加一才是正常天数
        },
        getNewTime: function () {
            var date= new Date();
            var y = date.getFullYear();
            var MM = date.getMonth() + 1;
            MM = MM < 10 ? ('0' + MM) : MM;
            var d = date.getDate();
            d = d < 10 ? ('0' + d) : d;
            var h = date.getHours();
            h = h < 10 ? ('0' + h) : h;
            var m = date.getMinutes();
            m = m < 10 ? ('0' + m) : m;
            var s = date.getSeconds();
            s = s < 10 ? ('0' + s) : s;
            var newTime = y + '-' + MM + '-' + d + ' ' + h + ':' + m + ':' + s;
            return newTime;
        },
        //导出表格数据
        exportTableData: function (url) {
            window.location.href = url;
        },
        initDataTable: function (table,  columns, columnDefs) {
            var tableObject = null;
            tableObject = $(table).DataTable({
                "destroy": true,
                "dom": 'tiprl',// 自定义显示项
                "lengthChange": true,// 是否允许用户自定义显示数量
                "bPaginate": true, // 翻页功能
                "bFilter": false, // 列筛序功能
                "searching": true,// 本地搜索
                "ordering": false, // 排序功能
                "Info": true,// 页脚信息
                "autoWidth": true,// 自动宽度
                "stripeClasses": [],
                "pageLength": 10,
                "lengthMenu": jlmrCommonFun.tablePageLength,
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
                "order": [
                    [0, null]
                ]
            });
            tableObject.on('order.dt search.dt', function () {
                tableObject.column(0, {
                    search: 'applied',
                    order: 'applied'
                }).nodes().each(function (cell, i) {
                    cell.innerHTML = i + 1;
                });
            }).draw();
            //显示隐藏列
            $(table).parents(".panel-body").find('.toggle-checkbox').on('change', function (e) {
                var column = tableObject.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });
            return tableObject;
        },
        destroyTable: function (tableObject) {
            if(tableObject){
                if(tableObject.dataTable){
                    tableObject.dataTable.destroy();
                    tableObject.dataTable.clear();
                    tableObject.dataTable = null;
                    tableObject = null;
                    return false;
                }
                tableObject.destroy();
                tableObject.clear();
                tableObject = null;
            }
        },
        computedNumber: function (number) {
            return Math.round(number * 100) / 100;
        },
        // 查找数组中是否包含
        getArrayIndex: function (value, prop, arr) {
            if(!arr || !prop || !arr.length) return -1;
            for(var i = 0, len = arr.length; i < len; i++){
                if(arr[i][prop] == value) return i;
            }
            return -1;
        },
        //根据树id获取选中的节点
        getCheckedNodes: function (treeId) {
            var zTree = $.fn.zTree.getZTreeObj(treeId), nodes = zTree
                .getCheckedNodes(true), v = "", vid = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == "vehicle") {
                    v += nodes[i].name + ",";
                    vid += nodes[i].id + ",";
                }
            }
            return {names: v, ids: vid}
        },
        //表格的列设置的html 根据列数据
        settingTableColumn: function (columnData) {
            if(!columnData || !columnData.length) return '';
            var table_column = "";
            for (var i = 0; i < columnData.length; i++) {
                table_column += "<li>" +
                    "<label>" +
                    "<input type=\"checkbox\" checked=\"checked\" class=\"toggle-checkbox\" data-column=\"" + parseInt(columnData[i].column) + "\" />"
                    + columnData[i].name + "</label>" +
                    "</li>";
            };
            return table_column;
        },
        //违规车辆和报警车辆 上传设置中的车辆数据获取回调
        handleTreeDataFilter: function (responseData, type, crrentSubV) {
            var nodesArr = [];
            if (responseData.success) {
                responseData = JSON.parse(ungzip(responseData.obj));
                if (type == "vehicle") {
                    nodesArr = filterQueryResult(responseData, crrentSubV);
                } else {
                    nodesArr = responseData;
                }
                for (var i = 0; i < nodesArr.length; i++) {
                    nodesArr[i].open = true;
                }
            }
            return nodesArr;
        },
        //重置表格的数据
        reloadTableData: function (tableObject, dataList) {
            var data = dataList ? dataList : [];
            var currentPage = tableObject.page();
            tableObject.clear();
            tableObject.rows.add(data);
            tableObject.page(currentPage).draw(false);
        },
        initDataTableIndex: function (tableObject) {
            if(!tableObject || !tableObject.dataTable) return false;
            var api = tableObject.dataTable;
            var startIndex = api.context[0]._iDisplayStart;//获取到本页开始的条数
            api.column(0).nodes().each(function (cell, i) {
                cell.innerHTML = startIndex + i + 1;
            });
        }
    }
    $(function () {
        $('input').inputClear();
    })
}(window, $))