(function (window, $) {
    var myTable;
    var begin;
    var over;
    logSearch = {
        init: function () {
            //表格列定义
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
                },
                {
                    "data": "eventDate",
                    "class": "text-center"
                },
                {
                    "data": "ipAddress",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == null || data == undefined || data == 'null') {
                            return "-";
                        }
                        return data;

                    }
                },
                {
                    "data": "username",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == null || data == undefined || data == 'null') {
                            return "-";
                        }
                        return data;

                    }
                },
                {
                    "data": "brand",
                    "class": "text-center"
                },
                {
                    "data": "plateColor",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return getPlateColor(data);
                    }
                },
                {
                    "data": "message",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (row.logSource == "1") {
                            return "<a onclick = 'logSearch.showLogContent(\"" + html2Escape(html2Escape(data, true), true) + "\")'>" + row.monitoringOperation + "</a>";
                        } else if (row.module == 'more') {
                            return "<a onclick = 'logSearch.showLogContent(\"" + html2Escape(html2Escape(data, true), true) + "\")'>" + row.monitoringOperation + "</a>";
                        } else if (row.module == 'batch') { //批量删除
                            return "<a onclick = 'logSearch.showLogContent(\"" + html2Escape(html2Escape(data, true), true) + "\")'>" + row.monitoringOperation + "</a>";
                        } else if (data.indexOf("修改绑定关系") != -1) {
                            return "<a onclick = 'logSearch.showLogContent(\"" + html2Escape(html2Escape(data, true), true) + "\")'>" + "信息列表：修改绑定关系" + "</a>";
                        }
                        if (data.indexOf("<br/>") > 0) {
                            data = data.replace('<br/>', '');
                            return '<div class="hasNewline">' + html2Escape(data) + '</div>';
                        }
                        return '<div class="hasNewline">' + html2Escape(data) + '</div>';


                    }

                },
                {
                    "data": "logSource",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == "1") {
                            return '终端上传';
                        } else if (data == "2") {
                            return '平台下发';
                        } else if (data == "3") {
                            return '平台操作';
                        } else if (data == "4") {
                            return 'APP操作';
                        }
                    }
                }
            ];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                var timeInterval = $('#timeInterval').val().split('--');
                d.startTime = timeInterval[0];
                d.endTime = timeInterval[1];
                d.username = $('#usernameSearch').val();
                d.message = $('#messageSearch').val();
                d.logSource = $('#logSource').val();

            };
            //表格setting
            var setting = {
                listUrl: "/clbs/m/reportManagement/logSearch/list",
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true
            };
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
        },
        export: function () {
            var timeInterval = $('#timeInterval').val().split('--');
            var startTime = timeInterval[0];
            var endTime = timeInterval[1];
            var username = $('#usernameSearch').val();
            var message = $('#messageSearch').val();
            var logSource = $('#logSource').val();

            var data = {
                "startTime": startTime,
                'endTime': endTime,
                "username": username,
                "message": message,
                "logSource": logSource,
            };
            var url = "/clbs/m/reportManagement/logSearch/export";

            if (getRecordsNum() > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }

            json_ajax("POST", url, "json", false, data, logSearch.exportCallback); //发送请求
        },
        exportCallback: function (reuslt) {
            if (reuslt == true) {
                var url = "/clbs/m/reportManagement/logSearch/export";
                window.location.href = url;
            } else {
                layer.msg(exportFail, {
                    move: false
                });
            }
        },
        //开始时间
        startDay: function (day) {
            var timeInterval = $('#timeInterval').val().split('--');
            var startValue = timeInterval[0];
            var endValue = timeInterval[1];
            if (startValue == "" || endValue == "") {
                var today = new Date();
                var targetday_milliseconds = today.getTime() + 1000 * 60 * 60 *
                    24 * day;

                today.setTime(targetday_milliseconds); //注意，这行是关键代码
                var tYear = today.getFullYear();
                var tMonth = today.getMonth();
                var tDate = today.getDate();
                tMonth = logSearch.doHandleMonth(tMonth + 1);
                tDate = logSearch.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " " +
                    "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 *
                    parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = logSearch.doHandleMonth(endMonth + 1);
                endDate = logSearch.doHandleMonth(endDate);
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
                vMonth = logSearch.doHandleMonth(vMonth + 1);
                vDate = logSearch.doHandleMonth(vDate);
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
                    vendMonth = logSearch.doHandleMonth(vendMonth + 1);
                    vendDate = logSearch.doHandleMonth(vendDate);
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
        getsTheCurrentTime: function () {
            var nowDate = new Date();
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
        inquireClick: function (number) {
            if (number == 0) {
                logSearch.getsTheCurrentTime();
                $('#timeInterval').val(startTime + '--' + endTime);
            } else if (number == -1) {
                logSearch.startDay(-1);
                $('#timeInterval').val(startTime + '--' + endTime);
            } else if (number == -3) {
                logSearch.startDay(-3)
                $('#timeInterval').val(startTime + '--' + endTime);
            } else if (number == -7) {
                logSearch.startDay(-7)
                $('#timeInterval').val(startTime + '--' + endTime);
            }
            var timeInterval = $('#timeInterval').val().split('--');
            startTime = timeInterval[0];
            endTime = timeInterval[1];
            if (logSearch.validates()) {
                myTable.requestData();
            }
            $("#dataTable_first").click();
        },
        validates: function () {
            return $("#logSearchList").validate({
                rules: {
                    endTime: {
                        compareDate: "#timeInterval",
                    }
                },
                messages: {
                    endTime: {
                        compareDate: "结束日期必须大于开始日期!",
                    }
                }
            }).form();
        },
        showLogContent: function (content) { // 显示log详情
            $("#logDetailDiv").modal("show");
            $(".modal-open").css("overflow-y", "auto")
            $("#logContent").html(content);
        },
        /**
         * 打印
         */
        print: function () {
            if (!window.Promise) {
                layer.msg('本浏览器暂不支持该功能!');
                return;
            }
            html2canvas(document.querySelector("#dataTable"), {
                useCORS: true,
                backgroundColor: null,
            }).then(function (canvas) {
                console.log('截图成功');
                // 将整个页面图片转成Base64位
                var dataURL = canvas.toDataURL("image/jpg");
                $('#picImg').attr('src', dataURL);
                localStorage.setItem('printPicBase64', dataURL);
                $('#mapPicModal').modal('show')

            }).catch(function () {
                console.log('异常');
            });
        },
        /**
         * 打印图片
         */
        printMapPic: function () {
            // var printHtml=document.getElementById('mapPic').innerHTML,
            //     newWindow=window.open("",'newWindow');
            // newWindow.document.body.innerHTML = printHtml;
            // newWindow.print();
            window.newWindow = window.open("/clbs/m/web/print/list.html", '_blank');

            //判断iframe是否存在，不存在则创建iframe
            // var iframe=document.getElementById("print-iframe");
            // if(!iframe){
            //     var el = document.getElementById("mapPic");
            //     iframe = document.createElement('IFRAME');
            //     var doc = null;
            //     iframe.setAttribute("id", "print-iframe");
            //     iframe.setAttribute('style', 'position:absolute;width:0px;height:0px;left:-500px;top:-500px;');
            //     document.body.appendChild(iframe);
            //     doc = iframe.contentWindow.document;
            //     //这里可以自定义样式
            //     //doc.write("<LINK rel="stylesheet" type="text/css" href="css/print.css">");
            //     doc.write('<div>' + el.innerHTML + '</div>');
            //     doc.close();
            //     iframe.contentWindow.focus();
            // }
            // iframe.contentWindow.print();
            // if (navigator.userAgent.indexOf("MSIE") > 0){
            //     document.body.removeChild(iframe);
            // }
        },
    }
    $(function () {
        $('input').inputClear();
        $('#timeInterval').dateRangePicker({dateLimit: 31});
        //当前时间
        logSearch.getsTheCurrentTime();
        //初始化页面
        logSearch.init();
        //查询
        $('#printBtn').on('click', logSearch.print);

        $('#print').on('click', logSearch.printMapPic);

        $(window).unload(function () {
            if (window.newWindow) {
                window.newWindow.close();
            }
        })
        window.onbeforeunload = function () {
            if (window.newWindow) {
                window.newWindow.close();
            }
        }
    })
}(window, $))