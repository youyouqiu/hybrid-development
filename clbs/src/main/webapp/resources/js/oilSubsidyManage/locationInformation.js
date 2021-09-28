(function (window, $) {
    var myTable;
    var myTable2;
    var size;
    var forwardOrgIds = '';
    var zTreeIdJson = {};
    var checkFlag = false; //判断组织节点是否是勾选操作
    var endTime;
    var startTime;
    var listId = []; // 补传id
    locationInformation = {
        init: function () {
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            for (var i = 0; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-text").html(menu_text);
            var setting = {
                async: {
                    url: locationInformation.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple"},
                    otherParam: {"icoType": "0"},
                    dataFilter: locationInformation.ajaxDataFilter
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
                    beforeClick: locationInformation.beforeClickVehicle,
                    onCheck: locationInformation.onCheckVehicle,
                    onNodeCreated: locationInformation.zTreeOnNodeCreated,
                    onAsyncSuccess: locationInformation.zTreeOnAsyncSuccess
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
            forwardOrgIds= $("#userGroupId").val();
            locationInformation.renderTable();
            locationInformation.setTable('#bcDataTable');
        },

        getTreeUrl: function (treeId, treeNode) {
            if (treeId == 'treeDemo') {
                return "/clbs/m/basicinfo/enterprise/professionals/tree";
            }
        },

        ajaxDataFilter: function (treeId, parentNode, responseData) {
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            size = responseData.length;
            for (var i = 0; i < responseData.length; i++) {
                responseData[i].open = true;
            }
            return responseData;
        },

        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(treeId);
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },

        onCheckVehicle: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(treeId);
            // 若为取消勾选则不展开节点
            if (treeNode.checked) {
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
            }
            locationInformation.getCharSelect(zTree);
            locationInformation.getCheckedNodes(treeId);
        },

        // 获取到选择的节点
        getCheckedNodes: function (treeId) {
            var zTree = $.fn.zTree.getZTreeObj(treeId),
                nodes = zTree.getCheckedNodes(true),
                v = "",
                vid = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (treeId == "treeDemo" && nodes[i].type == "group") {
                    v += nodes[i].name + ",";
                    vid += nodes[i].uuid + ",";
                }
            }
            forwardOrgIds = vid;
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

        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            locationInformation.getCharSelect(treeObj);
        },

        getCharSelect: function (treeObj) {
            var treeId = treeObj.setting.treeId;
            var nodes = treeObj.getCheckedNodes(true);
            var allNodes = treeObj.getNodes();
            if (nodes.length > 0) {
                $("#groupSelect").val(allNodes[0].name);
            } else {
                $("#groupSelect").val("");
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
                tMonth = locationInformation.doHandleMonth(tMonth + 1);
                tDate = locationInformation.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " " +
                    "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 *
                    parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = locationInformation.doHandleMonth(endMonth + 1);
                endDate = locationInformation.doHandleMonth(endDate);
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
                vMonth = locationInformation.doHandleMonth(vMonth + 1);
                vDate = locationInformation.doHandleMonth(vDate);
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
                    vendMonth = locationInformation.doHandleMonth(vendMonth + 1);
                    vendDate = locationInformation.doHandleMonth(vendDate);
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
        bcClick: function () {
            $('#bcModelBox').css('visibility', 'inherit');
            locationInformation.initAjaxBcTable();
        },
        bcCloseClick: function () {
            $('#bcModelBox').css('visibility', 'hidden');
        },
        bcStartClick: function () {
            console.log(listId, 'listId');
            var timeInterval = $('#timeInterval').val().split('--');
            var params = {
                "data": {
                    "startTime": timeInterval[0],
                    "endTime": timeInterval[1],
                    "monitorIds": listId,
                }
            }
            webSocket.subscribe(
                headers,
                '/user/topic/oilSupplement/sendReissueDataRequest',
                locationInformation.bcStartCallback,
                '/app/oilSupplement/sendReissueDataRequest',
                // JSON.stringify(params),
                params,
            );
            for (var i = 0; i < listId.length; i++) {
                $(`#bcjd_${listId[i]}`).text('请求中');
            }
        },
        bcStartCallback: function (msg) {
            console.log(msg, "msg");
            var bcjdTextType = '';
            var bzTextType = '';
            // $('#bcStartClick').prop();
            // bcTableList[i].vehicleId;
            var data = JSON.parse(msg.body);
            if (data && data.monitorId) {
                switch (data.state) {
                    case 0:
                        bcjdTextType = '请求成功';
                        bzTextType = '成功，企业平台即刻补发';
                        break;
                    case 1:
                        bcjdTextType = '请求失败';
                        bzTextType = '成功，企业平台重新请求';
                        break;
                    case 2:
                        bcjdTextType = '请求失败';
                        bzTextType = '请求三次失败';
                        break;
                    case 3:
                        bcjdTextType = '请求失败';
                        bzTextType = '失败，已有无需补发';
                        break;
                    case 4:
                        bcjdTextType = '请求失败';
                        bzTextType = '其他原因';
                        break;
                    case 5:
                        bcjdTextType = '请求失败';
                        bzTextType = '重复补发';
                        break;
                    default: bcjdTextType = '未知状态'; bzTextType = '未知状态';
                }
                $(`#bcjd_${data.monitorId}`).text(bcjdTextType);
                $(`#bz_${data.monitorId}`).text(bzTextType);
            }
        },
        inquireClick: function (number) {
            if(locationInformation.validates()){
                if (number == 0) {
                    locationInformation.getsTheCurrentTime();
                    $('#timeInterval').val(startTime + '--' + endTime);
                } else if (number == -1) {
                    locationInformation.startDay(-1);
                    $('#timeInterval').val(startTime + '--' + endTime);
                } else if (number == -3) {
                    locationInformation.startDay(-3);
                    $('#timeInterval').val(startTime + '--' + endTime);
                } else if (number == -7) {
                    locationInformation.startDay(-7);
                    $('#timeInterval').val(startTime + '--' + endTime);
                }
                locationInformation.renderTable();
            }
        },

        validates: function () {
            return $("#locationInformationList").validate({
                rules: {
                    endTime: {
                        compareDate: "#timeInterval",
                    },
                },
                messages: {
                    endTime: {
                        compareDate: "结束日期必须大于开始日期!",
                    },
                }
            }).form();
        },

        export: function () {
            var url = '/clbs/m/statistics/check/locationInformation/export/locations';
            window.location.href = url;
        },

        renderTable: function (ids) {
            var columnDefs = [{
                //第一列，用来显示序号
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];
            var columns = [
                {
                    //第一列，用来显示序号
                    "data": null,
                    "class": "text-center"
                },
                {
                    "data": "forwardOrgName",
                    "class": "text-center"
                },
                {
                    "data": "startTime",
                    "class": "text-center",
                },
                {
                    "data": "endTime",
                    "class": "text-center",
                },
                {
                    "data": "locationNum",
                    "class": "text-center"
                },
            ];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                var timeInterval = $('#timeInterval').val().split('--');
                d.forwardOrgId = forwardOrgIds;
                d.startTime = timeInterval[0];
                d.endTime = timeInterval[1];
            };
            //表格setting
            var setting = {
                listUrl: "/clbs/m/statistics/check/locationInformation/list",
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                ajaxCallBack: function (data) {
                    var records = data.records;
                    if(records.length != 0){
                        $("#export").prop('disabled', false);
                    }else{
                        $("#export").prop('disabled', true);
                    }
                }
            };
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
        refreshTableClick: function () {
            locationInformation.inquireClick(1);
        },
        // renderBcDataTable: function () {
        //     var columnDefs = [{
        //         //第一列，用来显示序号
        //         "searchable": false,
        //         "orderable": false,
        //         "targets": 0
        //     }];
        //     var columns = [
        //         {
        //             //第一列，用来显示序号
        //             "data": null,
        //             "class": "text-center"
        //         },
        //         {
        //             "data": "brand",
        //             "class": "text-center"
        //         },
        //         {
        //             "data": "startTime",
        //             "class": "text-center",
        //         },
        //         {
        //             "data": "endTime",
        //             "class": "text-center",
        //         },
        //         {
        //             "data": "locationNum",
        //             "class": "text-center"
        //         },
        //     ];
        //     //ajax参数
        //     var ajaxDataParamFun = function (d) {
        //         var timeInterval = $('#timeInterval').val().split('--');
        //         d.orgIds = forwardOrgIds;
        //         d.startTime = timeInterval[0];
        //         d.endTime = timeInterval[1];
        //     };
        //     //表格setting
        //     var setting = {
        //         listUrl: "/clbs/m/subsidy/manage/detail",
        //         columnDefs: columnDefs, //表格列定义
        //         columns: columns, //表格列
        //         dataTableDiv: 'bcDataTable', //表格
        //         ajaxDataParamFun: ajaxDataParamFun, //ajax参数
        //         pageable: false, //是否分页
        //         showIndexColumn: false, //是否显示第一列的索引列
        //         enabledChange: true,
        //         ajaxCallBack: function (data) {
        //             var records = data.obj;
        //             if (records.length != 0) {
        //
        //             } else {
        //
        //             }
        //         }
        //     };
        //     myTable = new TG_Tabel.createNew(setting);
        //     myTable.init();
        // },
        setTable: function (table) {
            $('.toggle-vis').prop('checked', true);
            myTable2 = $(table).DataTable({
                "destroy": true,
                "ordering": false, // 排序功能
                "dom": 'tiprl',// 自定义显示项
                "dom": "t" + "<'row'<'col-md-2 col-sm-12 col-xs-12 noPadding'><'col-md-4 col-sm-12 col-xs-12 noPadding'><'col-md-6 col-sm-12 col-xs-12 noPadding'>>",
                "oLanguage": {// 国际语言转化
                    "oAria": {
                        "sSortAscending": " - click/return to sort ascending",
                        "sSortDescending": " - click/return to sort descending"
                    },
                    "sInfo": "当前显示 _START_ 到 _END_ 条，共 _TOTAL_ 条记录。",
                    "sZeroRecords": "我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？",
                    "sEmptyTable": "我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？",
                    "sLoadingRecords": "正在加载数据-请等待...",
                    "sInfoEmpty": "当前显示0到0条，共0条记录",
                    "sInfoFiltered": "（数据库中共为 _MAX_ 条记录）",
                    "sProcessing": "<img src='../resources/user_share/row_details/select2-spinner.gif'/> 正在加载数据...",
                },
                "order": [
                    [0, null]
                ],// 第一列排序图标改为默认
            });
            myTable2.on('order.dt search.dt', function () {
                myTable2.column(0, {
                    search: 'applied',
                    order: 'applied'
                }).nodes().each(function (cell, i) {
                    cell.innerHTML = i + 1;
                });
            }).draw();
            //显示隐藏列
            $('.toggle-vis').off('change').on('change', function (e) {
                var column = myTable2.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });
        },
        initAjaxBcTable: function () {
            var timeInterval = $('#timeInterval').val().split('--');
            var url = "/clbs/m/subsidy/manage/detail";
            var params = {"orgIds": forwardOrgIds, "startTime": timeInterval[0], "endTime": timeInterval[1]};
            json_ajax("POST", url, "json", true, params, locationInformation.tableCallback);
        },
        tableCallback: function (data) {
            console.log(data, 'data');
            if (data.success == true) {
                var dataListArray = [];//用来储存显示数据
                var dataList = [];
                if (data.obj != null && data.obj.length != 0) {
                    var bcTableList = data.obj;//集合
                    for (var i = 0; i < bcTableList.length; i++) {
                        var num = i + 1;
                        dataList = [
                            num,
                            `<span id="${bcTableList[i].vehicleId}">${bcTableList[i].brand}</span>`,
                            `<span id="bcjd_${bcTableList[i].vehicleId}">待请求</span>`,
                            `<span id="bz_${bcTableList[i].vehicleId}"> </span>`,
                            `<a onClick="locationInformation.delTableList(${bcTableList[i]})">刪除</a>`,
                        ];
                        listId.push(bcTableList[i].vehicleId);
                        dataListArray.push(dataList);
                    }
                    locationInformation.reloadBcTable(dataListArray);
                } else {
                    locationInformation.reloadBcTable(dataListArray);
                }
            } else {
                if (data.msg != null) {
                    layer.msg(data.msg, {move: false});
                }
            }
        },
        reloadBcTable: function (list) {
            console.log(list, 'reloadBcTable');
            var currentPage = myTable2.page();
            myTable2.clear();
            myTable2.rows.add(list);
            myTable2.page(currentPage).draw(false);
        },
        delTableList: function (list) {
            console.log(list, 'list');
        },
    };
    $(function () {
        $('input').inputClear();
        $('#timeInterval').dateRangePicker();
        //当前时间
        locationInformation.getsTheCurrentTime();
        locationInformation.init();
        $("#groupSelect").bind("click", showMenuContent);
        $("#groupSelect").on('input propertychange', function (value) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.checkAllNodes(false);
            search_ztree('treeDemo', 'groupSelect','group');
        });
        $("#refreshTable").bind('click',locationInformation.refreshTableClick);
        $('#closeBox').on('click', function () {
            $('#bcModelBox').css('visibility', 'hidden');
        });
    })
})(window, $);