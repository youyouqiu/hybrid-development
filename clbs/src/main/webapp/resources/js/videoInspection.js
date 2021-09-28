var videoInspection;

;(function (window, $) {
    var vehicleId = '';
    var startTime;
    var endTime;
    var searchFlag = 1;
    var detailId = null;
    // 组织树
    var zTreeIdJson = {};
    var checkFlag = false;
    var size;
    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询
    var ifAllCheck = true; //刚进入页面小于100自动勾选
    var isSearch = true;
    var treeFlag = 'monitor';
    var simpleQueryParam = '';
    var cardNumber = "";
    var ztreeExtendFlag = true;

    videoInspection = {
        init: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(1) + "\" />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            ;
            $("#Ul-menu-text").html(menu_text);

            videoInspection.initTree();
        },
        //组织树
        initTree: function () {
            var setting = {
                async: {
                    url: videoInspection.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple"},
                    otherParam: {"icoType": "0"},
                    dataFilter: videoInspection.ajaxDataFilter
                },
                check: {
                    enable: true,
                    chkStyle: "checkbox",
                    radioType: "all",
                    chkboxType: {
                        "Y": "s",
                        "N": "s"
                    }
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
                    beforeClick: videoInspection.beforeClickVehicle,
                    onCheck: videoInspection.onCheckVehicle,
                    beforeCheck: videoInspection.zTreeBeforeCheck,
                    onExpand: videoInspection.zTreeOnExpand,
                    // beforeAsync: videoInspection.zTreeBeforeAsync,
                    onAsyncSuccess: videoInspection.zTreeOnAsyncSuccess,
                    onNodeCreated: videoInspection.zTreeOnNodeCreated
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },
        zTreeBeforeAsync: function () {
            return ztreeExtendFlag;
        },
        searchVehicleTree: function (param) {
            ifAllCheck = false;//模糊查询不自动勾选
            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                videoInspection.init();
            } else {
                bflag = true;
                var querySetting = {
                    async: {
                        url: "/clbs/a/search/reportFuzzySearch",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"type": $('#queryType').val(), "queryParam": param},
                        dataFilter: videoInspection.ajaxQueryDataFilter
                    },
                    check: {
                        enable: true,
                        chkStyle: "checkbox",
                        radioType: "all",
                        chkboxType: {
                            "Y": "s",
                            "N": "s"
                        }
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
                        beforeClick: videoInspection.beforeClickVehicle,
                        onCheck: videoInspection.onCheckVehicle,
                        onExpand: videoInspection.zTreeOnExpand,
                        onNodeCreated: videoInspection.zTreeOnNodeCreated,
                        // beforeCheck: videoInspection.zTreeBeforeCheck,
                    }
                };
                $.fn.zTree.init($("#treeDemo"), querySetting, null);
            }
        },
        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            // var responseData;
            if (responseData.success) {
                responseData = JSON.parse(ungzip(responseData.msg));
            } else {
                responseData = JSON.parse(ungzip(responseData));
            }
            var nodesArr;
            if ($('#queryType').val() == "vehicle") {
                nodesArr = filterQueryResult(responseData, crrentSubV);
            } else {
                nodesArr = responseData;
            }
            for (var i = 0; i < nodesArr.length; i++) {
                nodesArr[i].open = true;
            }
            return nodesArr;
        },
        getTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle";
            }
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (size <= 100 && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            videoInspection.getCharSelect(treeObj);

            bflag = false;
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
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { // 若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj(treeId), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;
                    json_ajax("post", "/clbs/m/personalized/ico/getVehicleCount",
                        "json", false, {"parentId": treeNode.id, "type": treeNode.type}, function (data) {
                            if (data.success) {
                                nodesLength += data.obj;
                            } else {
                                layer.msg(data.msg);
                            }
                        });

                    // 存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                    var ns = [];
                    // 节点id
                    var nodeId;
                    for (var i = 0; i < nodes.length; i++) {
                        nodeId = nodes[i].id;
                        if (nodes[i].type == "people" || nodes[i].type == "vehicle") {
                            // 查询该节点是否在勾选组织或分组下，若在则不记录，不在则记录
                            var nd = zTree.getNodeByParam("tId", nodes[i].tId, treeNode);
                            if (nd == null && $.inArray(nodeId, ns) == -1) {
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength += ns.length;
                } else if (treeNode.type == "people" || treeNode.type == "vehicle") { // 若勾选的为监控对象
                    var zTree = $.fn.zTree.getZTreeObj(treeId), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;
                    // 存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                    var ns = [];
                    // 节点id
                    var nodeId;
                    for (var i = 0; i < nodes.length; i++) {
                        nodeId = nodes[i].id;
                        if (nodes[i].type == "people" || nodes[i].type == "vehicle") {
                            if ($.inArray(nodeId, ns) == -1) {
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength = ns.length + 1;
                }
                if (nodesLength > 5000) {
                    // layer.msg(maxSelectItem);
                    layer.msg('最多勾选5000个监控对象');
                    flag = false;
                }
            }
            if (flag) {
                // 若组织节点已被勾选，则是勾选操作，改变勾选操作标识
                if (treeNode.type == "group" && !treeNode.checked) {
                    checkFlag = true;
                }
            }
            return flag;
        },
        zTreeOnExpand: function (event, treeId, treeNode) {
            //判断是否是勾选操作展开的树(是则继续执行，不是则返回)
            if (treeNode.type == "group" && !checkFlag) {
                return;
            }
            //初始化勾选操作判断表示
            checkFlag = false;
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            if (treeNode.type == "group") {
                var url = "/clbs/m/functionconfig/fence/bindfence/putMonitorByGroup";
                json_ajax("post", url, "json", false, {
                    "groupId": treeNode.id,
                    "isChecked": treeNode.checked,
                    "monitorType": "vehicle"
                }, function (data) {
                    var result = data.obj;
                    if (result != null && result != undefined) {
                        $.each(result, function (i) {
                            var pid = i; //获取键值
                            var chNodes = result[i];//获取对应的value
                            if (zTreeIdJson[pid] != undefined) {
                                var parentTid = zTreeIdJson[pid][0];
                                var parentNode = treeObj.getNodeByTId(parentTid);
                                if (parentNode.children === undefined) {
                                    treeObj.addNodes(parentNode, []);
                                }
                            }
                        });
                    }
                })
            }
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
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (responseData.msg) {
                var obj = JSON.parse(ungzip(responseData.msg));
                var data;
                if (obj.tree != null && obj.tree != undefined) {
                    data = obj.tree;
                    size = obj.size;
                } else {
                    data = obj
                }
                for (var i = 0; i < data.length; i++) {
                    data[i].open = true;
                }
            }
            return data;
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                isSearch = false;
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    videoInspection.getCheckedNodes();
                    videoInspection.validates();
                }, 600);
            }
            crrentSubV = [];
            crrentSubV.push(treeNode.id);
            videoInspection.getCharSelect(zTree);
            videoInspection.getCheckedNodes();
        },
        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
                nodes = zTree.getCheckedNodes(true),
                vid = "",
                Cnb = "";

            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == "vehicle" || nodes[i].type == "people" || nodes[i].type == "thing") {
                    vid += nodes[i].id + ",";
                    if (treeFlag != "monitor") {
                        Cnb += nodes[i].cardNumber + "_" + nodes[i].name + ",";
                    }
                }
            }
            cardNumber = Cnb;
            vehicleId = vid;
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
                tMonth = videoInspection.doHandleMonth(tMonth + 1);
                tDate = videoInspection.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate
                    /* + " " + "00:00:00"*/;
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24
                    * parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = videoInspection.doHandleMonth(endMonth + 1);
                endDate = videoInspection.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate + " "
                    /*+ "23:59:59"*/;
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = videoInspection.doHandleMonth(vMonth + 1);
                vDate = videoInspection.doHandleMonth(vDate);
                startTime = vYear + "-" + vMonth + "-" + vDate + " "
                    /*+ "00:00:00"*/;
                if (day == 1) {
                    endTime = vYear + "-" + vMonth + "-" + vDate + " "
                        /*+ "23:59:59"*/;
                } else {
                    var endNum = -1;
                    var vendtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * parseInt(endNum);
                    var dateEnd = new Date();
                    dateEnd.setTime(vendtoday_milliseconds);
                    var vendYear = dateEnd.getFullYear();
                    var vendMonth = dateEnd.getMonth();
                    var vendDate = dateEnd.getDate();
                    vendMonth = videoInspection.doHandleMonth(vendMonth + 1);
                    vendDate = videoInspection.doHandleMonth(vendDate);
                    endTime = vendYear + "-" + vendMonth + "-" + vendDate + " "
                        /*+ "23:59:59"*/;
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
            startTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate())/* + " " + "00:00:00"*/;
            endTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate())
                /*+ " "
                + ("23")
                + ":"
                + ("59")
                + ":"
                + ("59")*/;
            var atime = $("#atime").val();
            if (atime != undefined && atime != "") {
                startTime = atime;
            }
        },
        //查询
        inquireClick: function (number, isFirst) {
            if (number == 0) {
                videoInspection.getsTheCurrentTime();
            } else if (number == -1) {
                videoInspection.startDay(-1)
            } else if (number == -3) {
                videoInspection.startDay(-3)
            } else if (number == -7) {
                videoInspection.startDay(-7)
            }
            if (number != 1) {
                $('#timeInterval').val(startTime + '--' + endTime);
                startTime = startTime.trim();
                endTime = endTime.trim();
            } else {
                var timeInterval = $('#timeInterval').val().split('--');
                startTime = timeInterval[0].trim();
                endTime = timeInterval[1].trim();
            }

            if (startTime > endTime) {
                layer.msg('结束日期必须大于开始日期', {move: false});
                return;
            }
            if (!isFirst) {
                videoInspection.getCheckedNodes();
            }
            // if (vehicleId == "") {
            //     /*if ($('#queryType').val() === 'vehicle'){
            //         layer.msg(vehicleSelectBrand);
            //     } else{
            //         layer.msg(vehicleSelectBrand);
            //     }*/
            //     layer.msg(vehicleSelectBrand);
            //     return;
            // }
            if (!videoInspection.validates()) return;
            $('#simpleQueryParam').val('');
            simpleQueryParam = '';
            searchFlag = 1;
            videoInspection.searchInitTabData();
            //是否可以导出
            $("#exportRisk, #batchExportClick").prop("disabled", false);
        },
        validates: function () {
            return $("#oilist").validate({
                rules: {
                    groupSelect: {
                        zTreeChecked: "treeDemo"
                    },
                    endTime: {
                        required: true,
                        compareDate: "#timeInterval",
                        compareDateDiff: "#timeInterval"
                    },
                    startTime: {
                        required: true
                    },
                },
                messages: {
                    groupSelect: {
                        zTreeChecked: vehicleSelectBrand
                    },
                    endTime: {
                        required: "请选择结束日期！",
                        compareDate: "结束日期必须大于开始日期!",
                        compareDateDiff: "查询的日期必须小于一周"
                    },
                    startTime: {
                        required: "请选择开始日期！",
                    },
                }
            }).form();
        },
        //数据列表
        searchInitTabData: function () {
            var columnDefs = [{
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];
            var columns = [{
                "data": null,
                "class": "text-center",
            }, {
                "data": "monitorName",
                "class": "text-center",
                render:function(data, type, row){
                    var value = data + '<input type="hidden" value="'+ row.monitorId +'" class="rowId"/>';
                    return value;
                }
            }, {
                "data": "color",
                "class": "text-center"
            }, {
                "data": "objectType",
                "class": "text-center"
            }, {
                "data": "groupName",
                "class": "text-center"
            }, {
                "data": "totalNum",
                "class": "text-center"
            }, {
                "data": "totalSucNum",
                "class": "text-center"
            }];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.monitorIds = vehicleId;
                d.flag = searchFlag;
                d.monitorName = simpleQueryParam;
                d.startTime = startTime;
                d.endTime = endTime;
            };
            //表格setting
            var setting = {
                listUrl: "/clbs/lkyw/report/videoCarouselReport/list",
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                lengthMenu: [5, 10, 20, 50, 100],
                pageable: true, //是否分页
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                drawCallbackFun: function () {
                    var api = myTable.dataTable;
                    var startIndex = api.context[0]._iDisplayStart;//获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                    });
                },
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
        },
        //刷新
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            simpleQueryParam = '';
            searchFlag = 1;
            myTable.requestData();
        },
        //搜索
        searchTable: function () {
            simpleQueryParam = $("#simpleQueryParam").val();
            searchFlag = 2;
            myTable.requestData();
        },
        //导出
        export: function () {
            if ($("#dataTable tbody tr td").hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据,无法导出");
                return;
            }
            var paramer = {
                "flag": searchFlag,
                "monitorName": simpleQueryParam,
                "monitorIds": vehicleId,
                "startTime": startTime,
                "endTime": endTime,
            };
            var url = "/clbs/lkyw/report/videoCarouselReport/export";
            if(getRecordsNum() > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            exportExcelUseForm(url, paramer);
        },
        //批量导出明细
        batchExport: function () {
            if ($("#dataTable tbody tr td").hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据,无法导出");
                return;
            }
            if(getRecordsNum('detailDataTable_info') > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var paramer = {
                "startTime": startTime,
                "endTime": endTime,
                "monitorName": cardNumber,
                "monitorIds": vehicleId
            };
            var url = "/clbs/lkyw/report/videoCarouselReport/batchExport";
            exportExcelUseForm(url, paramer);
        },
        //tr点击
        tdClick:function (event) {
            var self = $(this);
            event.stopPropagation();
            if(self.hasClass('dataTables_empty')){
                return;
            }

            var detail = $('#detail');
            var  $tr= $(event.currentTarget).closest('tr');
            $tr.toggleClass('active').siblings('tr').removeClass('active');

            if($tr.hasClass('active')){
               detailId = $tr.find('.rowId').val();
                $('#detailStatus').val('');
                videoInspection.getDetail(detailId);
                detail.addClass('active');
                $('body').addClass('modal-open');
                $('#bodyMask').show();
            }else{
                detail.removeClass('active');
            }
        },
        //获取详情
        getDetail: function(monitorId){
            var columnDefs = [{
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];
            var columns = [{
                "data": null,
                "class": "text-center"
            }, {
                "data": "monitorName",
                "class": "text-center"
            }, {
                "data": "color",
                "class": "text-center"
            }, {
                "data": "objectType",
                "class": "text-center"
            }, {
                "data": "groupName",
                "class": "text-center"
            }, {
                "data": "channelNum",
                "class": "text-center"
            }, {
                "data": "startTime",
                "class": "text-center"
            }, {
                "data": "statusStr",
                "class": "text-center"
            }, {
                "data": "failReasonStr",
                "class": "text-center"
            }];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.monitorIds = monitorId;
                d.status = $('#detailStatus').val();
                d.startTime = startTime;
                d.endTime = endTime;
            };
            //表格setting
            var setting = {
                listUrl: "/clbs/lkyw/report/videoCarouselReport/detail",
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'detailDataTable', //表格
                lengthMenu: [5, 10, 20, 50, 100],
                pageable: true, //是否分页
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
            };
            //创建表格
            detailTable = new TG_Tabel.createNew(setting);
            detailTable.init();
        },
        closeDetail:function(){
            $('#dataTable tbody tr').removeClass('active');
            $('#detail').removeClass('active');
            $('body').removeClass('modal-open');
            $('#bodyMask').hide();
            $('#detailStatus').val('');
        },
        detailSearch:function(){
            detailTable.requestData();
        },
        detailExport:function () {
            var detailStatus = $('#detailStatus').val();
            if (
                detailStatus == ''
                && $("#detailDataTable tbody tr td").hasClass("dataTables_empty")
            ) {
                layer.msg("列表无任何数据,无法导出");
                return;
            }

            var paramer = {
                "startTime": startTime,
                "endTime": endTime,
                "status": '',
                "monitorIds": detailId
            };
            var url = "/clbs/lkyw/report/videoCarouselReport/exportDetail";
            exportExcelUseForm(url, paramer);
        }
    };
    $(function () {
        //时间
        videoInspection.getsTheCurrentTime();
        $('#timeInterval').dateRangePicker({
            'isShowHMS': false,
            dateLimit: 31
        });
        $('input').inputClear();
        videoInspection.init();
        //组织树
        $("#groupSelect").bind("click", showMenuContent);
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var param = $("#groupSelect").val();
                videoInspection.searchVehicleTree(param);
            }
        });
        var inputChange;
        $("#groupSelect").on('input propertychange', function (value) {
            if (inputChange !== undefined) {
                clearTimeout(inputChange);
            }
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                isSearch = true;
            }
            inputChange = setTimeout(function () {
                if (isSearch) {
                    var param = $("#groupSelect").val();
                    videoInspection.searchVehicleTree(param);
                }
                isSearch = true;
            }, 500);
        });
        //刷新
        $("#refreshTable").bind("click", videoInspection.refreshTable);
        $('#queryType').on('change', function () {
            if ($(this).val() == 'name') {
                treeFlag = 'driver';
            } else {
                treeFlag = 'monitor';
            }
            $("#groupSelect").val('');
            videoInspection.initTree();
        });
        //导出
        $("#exportRisk").bind("click", videoInspection.export);
        $('#tableContainer').on('click','td',videoInspection.tdClick);
        $(document).on('click', videoInspection.closeDetail);
        $('#detail').on('click', function (event) {
            event.stopPropagation();
        });
        //详情搜索
        $('#detailSearch').on('click', videoInspection.detailSearch);
        $('#detailExport').on('click', videoInspection.detailExport);

    })
}(window, $))