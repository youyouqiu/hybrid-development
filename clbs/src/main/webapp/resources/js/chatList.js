(function (window, $) {
    var myTable;
    //开始时间
    var startTime;
    //结束时间
    var endTime;
    chatSearch = {
        init: function () {
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
                tMonth = chatSearch.doHandleMonth(tMonth + 1);
                tDate = chatSearch.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " "
                    + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24
                    * parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = chatSearch.doHandleMonth(endMonth + 1);
                endDate = chatSearch.doHandleMonth(endDate);
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
                vMonth = chatSearch.doHandleMonth(vMonth + 1);
                vDate = chatSearch.doHandleMonth(vDate);
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
                    vendMonth = chatSearch.doHandleMonth(vendMonth + 1);
                    vendDate = chatSearch.doHandleMonth(vendDate);
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
        inquireClick: function (number) {
            if (number == 0) {
                chatSearch.getsTheCurrentTime();
            } else if (number == -1) {
                chatSearch.startDay(-1)
            } else if (number == -3) {
                chatSearch.startDay(-3)
            } else if (number == -7) {
                chatSearch.startDay(-7)
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
            ;
            if (!chatSearch.validates()) {
                return;
            }
            ;
            var url = "/clbs/cb/chat/list";
            var parameter = {
                "chatContent": $("#chatContent").val(),
                "fromUserName": $("#fromUserName").val(),
                "startTime": startTime,
                "endTime": endTime
            };
            json_ajax("POST", url, "json", true, parameter, chatSearch.getCallback);
        },
        exportAlarm: function () {
            if (!chatSearch.validates()) {
                return;
            }
            if(getRecordsNum() > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var timeInterval = $('#timeInterval').val().split('--');
            startTime = timeInterval[0];
            endTime = timeInterval[1];
            var url = "/clbs/cb/chat/export";
            var parameter = {
                "chatContent": $("#chatContent").val(),
                "fromUserName": $("#fromUserName").val(),
                "startTime": startTime,
                "endTime": endTime
            };
            json_ajax("POST", url, "json", true, parameter, chatSearch.exportCallback);
        },
        validates: function () {
            return $("#chatlist").validate({
                rules: {
                    startTime: {
                        required: true
                    },
                    endTime: {
                        required: true,
                        compareDate: "#timeInterval",
                    }
                },
                messages: {
                    startTime: {
                        required: "请选择开始日期！",
                    },
                    endTime: {
                        required: "请选择结束时间！",
                        compareDate: endtimeComStarttime,
                    }
                }
            }).form();
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
        getCallback: function (date) {
            console.log(date);
            if (date.success == true) {
                dataListArray = [];//用来储存显示数据
                if (date.obj != null && date.obj.length != 0) {
                    var chat = date.obj;
                    var reg = new RegExp('<span>', 'g');
                    var reg2 = new RegExp('</span>', 'g');
                    var regBr = new RegExp('<br>', 'g');
                    for (var i = 0; i < chat.length; i++) {
                        var html2EscapeStr = chat[i].chatContent == null ? '-' : chat[i].chatContent.replace(reg, '').replace(reg2, '').replace(regBr, '');
                        if (html2EscapeStr.toString().length > 50) {
                            html2EscapeStr = '<span class="demo demoUp" alt="' + html2Escape(html2Escape(html2EscapeStr)) + '">' + html2Escape(html2EscapeStr.substring(0, 50)) + "..." + '</span>';
                        }

                        var toTypeName = chatSearch.htmlspecialchars(chat[i].toTypeName);

                        var dateList =
                            [
                                i + 1,
                                chat[i].createDataTime,
                                chat[i].createDataUsername,
                                toTypeName,
                                html2EscapeStr
                            ];
                        dataListArray.push(dateList);
                    }

                    chatSearch.reloadData(dataListArray);
                } else {
                    chatSearch.reloadData(dataListArray);
                }
            } else {
                layer.msg(date.msg, {move: false});
            }
            // $(".demoUp").parent().bind("mouseover", chatSearch.drawCallbackFun);
        },
        htmlspecialchars: function (str) {
          var s = "";
          if (str.length == 0) return "";
          for   (var i=0; i<str.length; i++)
          {
            switch (str.substr(i,1))
            {
              case "<": s += "&lt;"; break;
              case ">": s += "&gt;"; break;
              case "&": s += "&amp;"; break;
              case " ":
                if(str.substr(i + 1, 1) == " "){
                  s += " &ensp;";
                  i++;
                } else s += " ";
                break;
              case "\"": s += "&quot;"; break;
              case "\n": s += "<br>"; break;
              default: s += str.substr(i,1); break;
            }
          }
          return s;
        },
        exportCallback: function (date) {
            if (date == true) {
                var url = "/clbs/cb/chat/export";
                window.location.href = url;
            } else {
                layer.msg(exportFail);
            }
        },
        goBack: function (GeocoderResult) {
            msgArray = GeocoderResult;
            var $dataTableTbody = $("#dataTable tbody");
            var dataLength = $dataTableTbody.children("tr").length;
            for (var i = 0; i < dataLength; i++) {
                if (msgArray[i] != undefined) {
                    $dataTableTbody.children("tr:nth-child(" + (i + 1) + ")").children("td:nth-child(6)").text(msgArray[i][0]);
                    $dataTableTbody.children("tr:nth-child(" + (i + 1) + ")").children("td:nth-child(9)").text(msgArray[i][1]);
                }
            }
        },
        getTable: function (table) {
            myTable = $(table).DataTable({
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
            //显示隐藏列
            $('.toggle-vis').on('change', function (e) {
                var column = myTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });
            $("#search_button").on("click", function () {
                var tsval = $("#simpleQueryParam").val()
                myTable.search(tsval, false, false).draw();
            });
        },
        reloadData: function (dataList) {
            var currentPage = myTable.page()
            myTable.clear()
            myTable.rows.add(dataList)
            myTable.page(currentPage).draw(false);
            myTable.search('', false, false).page(currentPage).draw();
        },
        drawCallbackFun: function () {//鼠标移入后弹出气泡显示单元格内容；
            $("#dataTable").on('mouseover','.demoUp',function () {
                var _this = $(this);
                if (_this.attr("alt")) {
                    _this.justToolsTip({
                        animation: "moveInTop",
                        width: "auto",
                        contents: _this.attr("alt"),
                        gravity: 'top'
                    });
                }
            })
        }
    }
    $(function () {
        //初始化页面
        chatSearch.init();
        $('input').inputClear();
        //当前时间
        chatSearch.getsTheCurrentTime();
        chatSearch.getTable('#dataTable');
        $('#timeInterval').dateRangePicker();
        //导出
        $("#exportAlarm").bind("click", chatSearch.exportAlarm);
        chatSearch.inquireClick(0);

        chatSearch.drawCallbackFun();
    })
})(window, $)