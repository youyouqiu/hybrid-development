(function (window, $) {
    var startTime;
    var endTime;
    // 组织树
    var zTreeIdJson = {};
    var checkFlag = false;
    var size;
    var ifAllCheck = true; //刚进入页面小于100自动勾选
    var simpleQueryParam = '';
    var groupId = [];
    var time = null;

    var dbValue = false //树双击判断参数
    MonthData = {
        init: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            for (var i = 0; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            };
            $("#Ul-menu-text").html(menu_text);

            MonthData.initTree();
        },
        //组织树
        initTree: function () {
            var setting = {
                async: {
                    url: MonthData.getParkingReportTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "type": "multiple",
                        "icoType": "0"
                    },
                    dataFilter: MonthData.ajaxDataFilter
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
                    beforeClick: MonthData.beforeClickVehicle,
                    onAsyncSuccess: MonthData.zTreeOnAsyncSuccess,
                    beforeCheck: MonthData.zTreeBeforeCheck,
                    onCheck: MonthData.onCheckVehicle,
                    onNodeCreated: MonthData.zTreeOnNodeCreated,
                    onExpand: MonthData.zTreeOnExpand,
                    onDblClick: MonthData.onDblClickVehicle

                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },
        onDblClickVehicle: function (e, treeId, treeNode) {
            if (!treeNode) return
            dbValue = true
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.checkAllNodes(false);
            MonthData.getCheckedNodes("treeDemo");
            // $("#groupSelect").val(treeNode.name);
            // search_ztree('treeDemo', 'groupSelect', 'group');
            treeObj.checkNode(treeNode, !treeNode.checked, false, true);
        },
        getParkingReportTreeUrl: function (treeId, treeNode) {
            if (treeId == 'treeDemo') {
                return "/clbs/m/basicinfo/enterprise/professionals/tree";
            }
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
            if (size <= 100 && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            MonthData.getCharSelect(treeObj);
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked && !dbValue) {
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                var getCheckNode = treeObj.getCheckedNodes('true');
                var nodes = treeObj.getNodesByFilter(function (node) {
                    return node.type == 'group' && !node.checked;
                }, false, treeNode);
                if (getCheckNode.length + nodes.length + 1 > TREE_MAX_CHILDREN_LENGTH) {
                    layer.msg('最多勾选' + TREE_MAX_CHILDREN_LENGTH + '个企业' + '<br/>双击名称可选中本组织');
                    flag = false;
                }
            }
            return flag;
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            dbValue = false
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                zTree.expandNode(treeNode, true, true, false, false); // 展开节点
                setTimeout(() => {
                    MonthData.getCheckedNodes("treeDemo");
                    MonthData.validates();
                }, 600);
            }
            MonthData.getCheckedNodes("treeDemo");
            MonthData.getCharSelect(zTree);
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
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (treeId == "treeDemo" && nodes[i].type == "group") {
                    groupIds += nodes[i].uuid + ",";
                }
            }
            groupId = groupIds;
        },
        //时间
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
                tMonth = MonthData.doHandleMonth(tMonth + 1);
                tDate = MonthData.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " " +
                    "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 *
                    parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = MonthData.doHandleMonth(endMonth + 1);
                endDate = MonthData.doHandleMonth(endDate);
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
                vMonth = MonthData.doHandleMonth(vMonth + 1);
                vDate = MonthData.doHandleMonth(vDate);
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
                    vendMonth = MonthData.doHandleMonth(vendMonth + 1);
                    vendDate = MonthData.doHandleMonth(vendDate);
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
        renderSelect: function (id) {
            var select = $(id);
            var now = new Date();
            var year = now.getFullYear();
            var month = now.getMonth() + 1;
            var tmpl = '<option value="$name">$name</option>';
            var add0 = function (n) {
                if (n < 10) {
                    return '0' + n.toString();
                }
                return n.toString()
            };
            for (var i = 0; i < 12; i++) {
                if (i < month) {
                    select.append($(tmpl.replace(/\$name/g, year + '-' + add0(month - i))));
                } else {
                    select.append($(tmpl.replace(/\$name/g, (year - 1) + '-' + add0(12 - i + month))));
                }
            }
        },
        // 获取当前月或上一月
        getCurrentMonth: function (parm) {
            var nowDate = new Date();
            var y = nowDate.getFullYear();
            var m = nowDate.getMonth() + 1;

            if (parm) {
                m = nowDate.getMonth();
            }

            if (m < 10) {
                m = '0' + m
            }

            return y + '-' + m;
        },
        //格式化年月
        formatMonth: function (monthStr) {
            var now = new Date(monthStr);
            var year = now.getFullYear();
            var month = now.getMonth() + 1;
            var add0 = function (n) {
                if (n < 10) {
                    return '0' + n.toString();
                }
                return n.toString()
            };
            return year + '-' + add0(month);
        },
        validates: function () {
            return $("#oilist").validate({
                rules: {
                    groupSelect: {
                        required: true
                    }
                },
                messages: {
                    groupSelect: {
                        required: "请至少选择一个企业"
                    }
                }
            }).form();
        },
        //查询
        inquireClick: function (number) {
            if (!MonthData.validates()) {
                return;
            }
            if (number == 1) {
                time = $('#select').val()
            }
            if (number == 'thisMonth') {
                time = MonthData.getCurrentMonth();
                $('#select').val(time)
            }
            if (number == 'lastMonth') {
                var currentMonth = $('#select').val();

                if ($('#select').val() == $("#select option:last").val()) {
                    layer.msg('查询时间范围不超过12个月');
                    return;
                }

                var date = new Date(currentMonth);
                date.setMonth(date.getMonth() - 1);
                var str = MonthData.formatMonth(date);
                $('#select').val(str);
                time = $('#select').val()
            }

            $("#simpleQueryParam").val('');
            MonthData.getMonthDataList();
        },
        getMonthDataList: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            for (var i = 0; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            };
            $("#Ul-menu-text").html(menu_text);
            var columnDefs = [{
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];
            var columns = [{
                "data": null,
                "class": "text-center"
            }, {
                "data": "groupName",
                "class": "text-center"
            }, {
                "data": "vehicleTotal",
                "class": "text-center"
            }, {
                "data": "addNumber",
                "class": "text-center"
            }, {
                "data": "deleteNumber",
                "class": "text-center"
            }, {
                "data": "onlineRate",
                "class": "text-center"
            }, {
                "data": "nerverOnlineNumber",
                "class": "text-center"
            }, {
                "data": "speedNumber",
                "class": "text-center"
            }, {
                "data": "speedRate",
                "class": "text-center"
            }, {
                "data": "tiredNumber",
                "class": "text-center"
            }, {
                "data": "tiredRate",
                "class": "text-center"
            }];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                MonthData.getCheckedNodes("treeDemo");
                d.search = $("#simpleQueryParam").val();
                d.time = time;
                d.groupIds = groupId;
            };
            //表格setting
            var setting = {
                listUrl: "/clbs/s/cargo/monthReport/list",
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                pageable: true, //是否分页
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
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
                "dom": "t" + "<'row'<'col-md-2 col-sm-12 col-xs-12 noPadding'l><'col-md-4 col-sm-12 col-xs-12 noPadding'i><'col-md-6 col-sm-12 col-xs-12 noPadding'p>>",
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
        },
        //搜索
        searchTable: function () {
            if (time == null) {
                return;
            }
            MonthData.getMonthDataList();
        },
        //刷新
        refreshTable: function () {
            MonthData.inquireClick(1);
        },
        //导出
        export: function () {
            if ($("#dataTable tbody td").hasClass('dataTables_empty')) {
                layer.msg('列表无任何数据，无法导出');
                return;
            }
            if (getRecordsNum() > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            simpleQueryParam = $("#simpleQueryParam").val();
            var time = $("#select").val();
            var url = "/clbs/s/cargo/monthReport/export"
            var param = {
                groupIds: groupId,
                time: time,
                search: simpleQueryParam
            }
            exportExcelUsePost(url, param)
            // window.location.href="/clbs/s/cargo/monthReport/export?groupIds="+groupId+"&time="+time+"&search="+simpleQueryParam;
        },
    }
    $(function () {
        MonthData.init();
        MonthData.getTable('#dataTable');
        //时间
        MonthData.getsTheCurrentTime();
        //时间下拉框
        MonthData.renderSelect("#select");
        //组织树
        $("#groupSelect").bind("click", showMenuContent);
        //搜索
        $("#search_button").bind("click", MonthData.searchTable);
        //刷新
        $("#refreshTable").bind('click', MonthData.refreshTable);
        //导出
        $("#exportAlarm").bind("click", MonthData.export);
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                search_ztree('treeDemo', 'groupSelect', 'group');
                treeObj.checkAllNodes(false);
            };
        });

        /**
         * 监控对象树模糊查询
         */
        $("#groupSelect").on('input propertychange', function (value) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.checkAllNodes(false);
            search_ztree('treeDemo', 'groupSelect', 'group');
        });
    })
}(window, $));