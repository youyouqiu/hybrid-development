(function (window, $) {
    // var simpleQueryParam = '';
    //开始时间
    var startTime;
    //结束时间
    var endTime;
    var answerUser;
    var status;

    inspectionPeople = {
        init: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            for (var i = 0; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-text").html(menu_text);

            $("#timeInterval").dateRangePicker({
                dateLimit: 60,
            });
            inspectionPeople.getShowTime();
            inspectionPeople.tableInit();
        },
        //判断日期是否合法,是否选中车辆
        validates: function () {
            return $("#hourslist").validate({
                rules: {
                    timeInterval: {
                        required: true
                    }
                },
                messages: {
                    timeInterval: {
                        required: '请选择时间'
                    }
                }
            }).form();
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
                tMonth = inspectionPeople.doHandleMonth(tMonth + 1);
                tDate = inspectionPeople.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " "
                    + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24
                    * parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = inspectionPeople.doHandleMonth(endMonth + 1);
                endDate = inspectionPeople.doHandleMonth(endDate);
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
                vMonth = inspectionPeople.doHandleMonth(vMonth + 1);
                vDate = inspectionPeople.doHandleMonth(vDate);
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
                    vendMonth = inspectionPeople.doHandleMonth(vendMonth + 1);
                    vendDate = inspectionPeople.doHandleMonth(vendDate);
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
        getsTheCurrentTime: function () {
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
            var atime = $("#atime").val();
            if (atime != undefined && atime != "") {
                startTime = atime;
            }
        },
        //查询
        inquireClick: function (number) {
            if (number == 0) {
                inspectionPeople.getsTheCurrentTime();
            } else if (number == -1) {
                inspectionPeople.startDay(-1)
            } else if (number == -3) {
                inspectionPeople.startDay(-3)
            } else if (number == -7) {
                inspectionPeople.startDay(-7)
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
            if (!inspectionPeople.validates()) {
                return;
            }
            answerTable.requestData();
        },
        exportAlarm: function (e) {
            if ($("#dataTable tbody tr td").hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据，无法导出");
                return;
            }
            if(getRecordsNum() > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var url = "/clbs/adas/inspectUser/export";
            var parameter = {
                'inspectStartTime': startTime,
                'inspectEndTime': endTime,
                'answerUser': answerUser,
                'status': status,
            };
            exportExcelUseForm(url, parameter);
        },
        columnRenderFun: function (key) {
            switch (key) {
                case 'answerStatusStr':// 应答状态
                    return function (data, type, row, meta) {
                        if (data) {
                            if (row.answerStatus === 0) {
                                return "<button onclick='pagesNav.getAnswerInfo(\"" + row.id + "\")'  type='button' class='editBtn editBtn-info'><i class='fa fa-pencil'></i> 未应答</button>"
                            }
                            if (row.answerStatus === 1) {
                                return "<button onclick='pagesNav.getAnswerInfo(\"" + row.id + "\",true)'  type='button' class='editBtn editBtn-info trueBtn'><img src='/clbs/resources/img/true.png'/> 正常应答</button>"
                            }
                            return data;
                        }
                        return '-';
                    };
                default:
                    return null;
            }
        },
        tableInit: function () {
            //显示隐藏列
            var columnDefs = [{
                "searchable": true,
                "orderable": false,
                "targets": [0, 1, 2, 3]
            }];
            var columnKeys = [
                'answerStatusStr', 'objectTypeStr', 'objectId',
                'inspectTime', 'answerTime', 'ackTime',
                'answerUser', 'answerUserTel', 'answerUserIdentityNumber', 'socialSecurityNumber',
            ];
            var columns = [{
                "data": null,
                "class": "text-center",
            }];
            for (var i = 0; i < columnKeys.length; i++) {
                columns.push({
                    "data": columnKeys[i],
                    "class": "text-center",
                    render: inspectionPeople.columnRenderFun(columnKeys[i]) || function (data) {
                        if (data) return data;
                        return '-';
                    }
                })
            }
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.inspectStartTime = startTime;
                d.inspectEndTime = endTime;
                /* simpleQueryParam = $('#simpleQueryParam').val();
                 d.simpleQueryParam = simpleQueryParam;*/
                answerUser = $('#answerUserParam').val();
                status = $('#answerStatusParam').val();
                d.answerUser = answerUser;
                d.status = status;
            };
            //表格setting
            var setting = {
                listUrl: "/clbs/adas/inspectUser/list",
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
            answerTable = new TG_Tabel.createNew(setting);
            answerTable.init();
            $('.toggle-vis').off().on('change', function (e) {
                var visible = answerTable.dataTable.column($(this).attr('data-column')).visible();
                if (visible) {
                    answerTable.dataTable.column($(this).attr('data-column')).visible(false);
                } else {
                    answerTable.dataTable.column($(this).attr('data-column')).visible(true);
                }
                $(".keep-open").addClass("open");
            });
        },
        //刷新列表
        refreshTable: function () {
            answerTable.requestData();
        },
        // 查询时间默认显示昨天到今天
        getShowTime: function () {
            var day1 = new Date();
            day1.setTime(day1.getTime() - 24 * 60 * 60 * 1000);
            startTime = day1.getFullYear() + "-" + inspectionPeople.doHandleMonth(day1.getMonth() + 1) + "-" + inspectionPeople.doHandleMonth(day1.getDate()) + ' 00:00:00';
            var day2 = new Date();
            day2.setTime(day2.getTime());
            endTime = day2.getFullYear() + "-" + inspectionPeople.doHandleMonth(day2.getMonth() + 1) + "-" + inspectionPeople.doHandleMonth(day2.getDate()) + ' 23:59:59';
            $("#timeInterval").val(startTime + '--' + endTime);
        }
    };
    $(function () {
        //初始化页面
        inspectionPeople.init();
        // $('#patrolAnswerModel').modal('show');

        //导出
        $("#exportAlarm").bind("click", inspectionPeople.exportAlarm);
        $("#refreshTable").bind("click", inspectionPeople.refreshTable);
    })
}(window, $));
