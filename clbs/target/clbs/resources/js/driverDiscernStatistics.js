/**
 * 终端驾驶员识别统计
 */
;(function (window, $) {
    var vehicleId = '';
    var startTime;
    var endTime;
    // 组织树
    var zTreeIdJson = {};
    var checkFlag = false;
    var size;
    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询
    var ifAllCheck = true; //刚进入页面小于100自动勾选
    var isSearch = true;
    var treeFlag = 'monitor';
    var cardNumber = "";
    var ztreeExtendFlag = true;
    var comparisonResult = '';
    var comparisonType = '';
    var simpleQueryParam = '';
    var swiper1 = null;
    var swiper2 = null;

    driverDiscernStatistics = {
        init: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(1) + "\" />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-text").html(menu_text);
            driverDiscernStatistics.initTree();
            driverDiscernStatistics.searchInitTabData();
        },
        //组织树
        initTree: function () {
            var setting = {
                async: {
                    url: driverDiscernStatistics.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: driverDiscernStatistics.ajaxDataFilter
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
                    beforeClick: driverDiscernStatistics.beforeClickVehicle,
                    onCheck: driverDiscernStatistics.onCheckVehicle,
                    beforeCheck: driverDiscernStatistics.zTreeBeforeCheck,
                    onExpand: driverDiscernStatistics.zTreeOnExpand,
                    beforeAsync: driverDiscernStatistics.zTreeBeforeAsync,
                    //beforeAsync: driverDiscernStatistics.zTreeBeforeAsync,
                    onAsyncSuccess: driverDiscernStatistics.zTreeOnAsyncSuccess,
                    onNodeCreated: driverDiscernStatistics.zTreeOnNodeCreated
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },
        zTreeBeforeAsync: function () {
            return ztreeExtendFlag;
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
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
        searchVehicleTree: function (param) {
            ifAllCheck = false;//模糊查询不自动勾选
            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                driverDiscernStatistics.init();
            } else {
                bflag = true;
                var querySetting = {
                    async: {
                        url: "/clbs/a/search/reportFuzzySearch",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"type": $('#queryType').val(), "queryParam": param, "queryType": "vehicle"},
                        dataFilter: driverDiscernStatistics.ajaxQueryDataFilter
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
                        beforeClick: driverDiscernStatistics.beforeClickVehicle,
                        onCheck: driverDiscernStatistics.onCheckVehicle,
                        onExpand: driverDiscernStatistics.zTreeOnExpand,
                        onNodeCreated: driverDiscernStatistics.zTreeOnNodeCreated,
                    }
                };
                $.fn.zTree.init($("#treeDemo"), querySetting, null);
            }
        },
        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData))
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
            driverDiscernStatistics.getCharSelect(treeObj);

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
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
                        nodes = zTree.getCheckedNodes(true),
                        v = "";
                    var nodesLength = 0;
                    if (treeFlag == 'monitor') {
                        json_ajax("post", "/clbs/m/personalized/ico/getCheckedVehicle",
                            "json", false, {
                                "parentId": treeNode.id,
                                "type": treeNode.type,
                                "webType": 4
                            }, function (data) {
                                if (data.success) {
                                    nodesLength += data.obj;
                                } else {
                                    layer.msg(data.msg);
                                }
                            });
                    } else {
                        json_ajax("post", "/clbs/m/basicinfo/enterprise/professionals/getProfessionalCountByPid",
                            "json", false, {"parentId": treeNode.id}, function (data) {
                                if (data.success) {
                                    nodesLength += data.obj;
                                } else {
                                    layer.msg(data.msg);
                                }
                            });
                    }
                    //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                    var ns = [];
                    //节点id
                    var nodeId;
                    for (var i = 0; i < nodes.length; i++) {
                        nodeId = nodes[i].id;
                        if (nodes[i].type == "people" || nodes[i].type == "vehicle" || nodes[i].type == "thing") {
                            //查询该节点是否在勾选组织或分组下，若在则不记录，不在则记录
                            var nd = zTree.getNodeByParam("tId", nodes[i].tId, treeNode);
                            if (nd == null && $.inArray(nodeId, ns) == -1) {
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength += ns.length;
                } else if (treeNode.type == "people" || treeNode.type == "vehicle" || treeNode.type == "thing") { //若勾选的为监控对象
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;
                    //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                    var ns = [];
                    //节点id
                    var nodeId;
                    for (var i = 0; i < nodes.length; i++) {
                        nodeId = nodes[i].id;
                        if (nodes[i].type == "people" || nodes[i].type == "vehicle" || nodes[i].type == "thing") {
                            if ($.inArray(nodeId, ns) == -1) {
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength = ns.length + 1;
                }
                if (nodesLength > TREE_MAX_CHILDREN_LENGTH) {
                    layer.msg('最多勾选' + TREE_MAX_CHILDREN_LENGTH + '个监控对象');
                    flag = false;
                }
            }
            if (flag) {
                //若组织节点已被勾选，则是勾选操作，改变勾选操作标识
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
            if (treeFlag !== "monitor") {
                return;
            }
            //初始化勾选操作判断表示
            checkFlag = false;
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (treeNode.type == "group") {
                // treeObj.expandNode(treeNode, true, true, true);
                var url = "/clbs/m/functionconfig/fence/bindfence/putMonitorByGroup";
                json_ajax("post", url, "json", false, {
                    "groupId": treeNode.id,
                    "isChecked": treeNode.checked,
                    "monitorType": "monitor"
                }, function (data) {
                    var result = data.obj;
                    if (result != null && result != undefined) {
                        ztreeExtendFlag = false;
                        $.each(result, function (i) {
                            var pid = i; //获取键值
                            var chNodes = result[i];//获取对应的value
                            if (zTreeIdJson[pid] != undefined) {
                                var parentTid = zTreeIdJson[pid][0];
                                var parentNode = treeObj.getNodeByTId(parentTid);
                                if (parentNode.children === undefined) {
                                    treeObj.addNodes(parentNode, chNodes);
                                }
                            }
                        });
                        ztreeExtendFlag = true;
                    }
                })
            }
            //driverDiscernStatistics.getCharSelect(treeObj);
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
                    driverDiscernStatistics.getCheckedNodes();
                    driverDiscernStatistics.validates();
                }, 600);
            }
            crrentSubV = [];
            crrentSubV.push(treeNode.id);
            driverDiscernStatistics.getCharSelect(zTree);
            driverDiscernStatistics.getCheckedNodes();
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
                tMonth = driverDiscernStatistics.doHandleMonth(tMonth + 1);
                tDate = driverDiscernStatistics.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate;
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24
                    * parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = driverDiscernStatistics.doHandleMonth(endMonth + 1);
                endDate = driverDiscernStatistics.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate;
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = driverDiscernStatistics.doHandleMonth(vMonth + 1);
                vDate = driverDiscernStatistics.doHandleMonth(vDate);
                startTime = vYear + "-" + vMonth + "-" + vDate;
                if (day == 1) {
                    endTime = vYear + "-" + vMonth + "-" + vDate;
                } else {
                    var endNum = -1;
                    var vendtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * parseInt(endNum);
                    var dateEnd = new Date();
                    dateEnd.setTime(vendtoday_milliseconds);
                    var vendYear = dateEnd.getFullYear();
                    var vendMonth = dateEnd.getMonth();
                    var vendDate = dateEnd.getDate();
                    vendMonth = driverDiscernStatistics.doHandleMonth(vendMonth + 1);
                    vendDate = driverDiscernStatistics.doHandleMonth(vendDate);
                    endTime = vendYear + "-" + vendMonth + "-" + vendDate;
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
                    : nowDate.getDate());
            endTime = nowDate.getFullYear()
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
        },
        //查询
        inquireClick: function (number, isFirst) {
            if (number == 0) {
                driverDiscernStatistics.getsTheCurrentTime();
            } else if (number == -1) {
                driverDiscernStatistics.startDay(-1)
            } else if (number == -3) {
                driverDiscernStatistics.startDay(-3)
            } else if (number == -7) {
                driverDiscernStatistics.startDay(-7)
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

            if (startTime > endTime) {
                layer.msg('结束日期必须大于开始日期', {move: false});
                return;
            }
            if (!isFirst) {
                driverDiscernStatistics.getCheckedNodes();
            }
            if (!driverDiscernStatistics.validates()) return;
            $('#simpleQueryParam').val('');
            simpleQueryParam = '';
            $("#exportRisk").prop("disabled", false);
            driverDiscernStatistics.searchInitTabData();
        },
        validates: function () {
            return $("#oilist").validate({
                rules: {
                    startTime: {
                        required: true
                    },
                    endTime: {
                        required: true,
                        compareDate: "#timeInterval",
                    },
                    groupSelect: {
                        zTreeChecked: "treeDemo"
                    }
                },
                messages: {
                    startTime: {
                        required: "请选择开始日期！",
                    },
                    endTime: {
                        required: "请选择结束时间！",
                        compareDate: endtimeComStarttime,
                    },
                    groupSelect: {
                        zTreeChecked: vehicleSelectBrand,
                    }
                }
            }).form();
        },
        // 表格数据渲染
        searchInitTabData: function () {
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
                "class": "text-center",
                render: function (result, type, row) {
                    return '<span class="monitorId" data-id="' + row.monitorId + '" data-time="' + row.identificationTimeStr + '">' + result + '</span>'
                }
            }, {
                "data": "orgName",
                "class": "text-center"
            }, {
                "data": "identificationResult",
                "class": "text-center",
                render: function (result, x, row) {
                    return driverDiscernStatistics.renderCompareResult(result);
                }
            }, {
                "data": "matchRate",
                "class": "text-center",
                render: function (result, x, row) {
                    return result ? (result + '%') : '--';
                }
            }, {
                "data": "matchThreshold",
                "class": "text-center",
                render: function (result, x, row) {
                    return result ? (result + '%') : '--';
                }
            }, {
                "data": "identificationType",
                "class": "text-center",
                render: function (result, x, row) {
                    return driverDiscernStatistics.renderCompareType(result);
                }
            }, {
                "data": "faceId",
                "class": "text-center",
                render: function (result, x, row) {
                    return result || '--'
                }
            }, {
                "data": "driverName",
                "class": "text-center",
                render: function (result, x, row) {
                    return result || '--'
                }
            }, {
                "data": "cardNumber",
                "class": "text-center",
                render: function (result, x, row) {
                    return result || '--'
                }
            }, {
                "data": "identificationTimeStr",
                "class": "text-center",
                render: function (result, x, row) {
                    return result || '--'
                }
            }, {
                "data": "photoFlag",
                "class": "text-center",
                render: function (result, x, row) {
                    if (result) {
                        return '<a onclick="driverDiscernStatistics.searchMedia(event,\'' + row.id + '\',\'' + row.monitorName + '\')">查询附件</a>'
                    }
                    return '--'
                }
            }, {
                "data": "address",
                "class": "text-center",
                render: function (result, x, row) {
                    return '<a class="importantTr" onclick="driverDiscernStatistics.getAlarmAddress(\'' + row.latitude + '\',\'' + row.longitude + '\',event)">点击获取位置信息</a>'
                }
            }];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.monitorIds = vehicleId ? vehicleId : '1';
                comparisonResult = $('#compareResult').val();
                comparisonType = $('#compareType').val();
                simpleQueryParam = $('#simpleQueryParam').val();

                d.identificationStartDate = startTime;
                d.identificationEndDate = endTime;
                d.identificationResult = comparisonResult;
                d.identificationType = comparisonType;
                d.simpleQueryParam = simpleQueryParam;
            };
            //表格setting
            var setting = {
                listUrl: "/clbs/m/driver/discern/statistics/pageQuery",
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

            $('.toggle-vis').off('change').on('change', function (e) {
                var column = myTable.dataTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });
        },
        renderPart2: function (srcArr) {
            swiper1 = new Swiper('#vedioSwiperWrapper', {
                srcArr: srcArr,
                indicatorType: 'number',
                height: '300',
                widthRatio: 0.75,
                tagName: 'video',
                empty: '<p>暂无视频</p>'
            })
        },
        renderPart3: function (srcArr) {
            swiper2 = new Swiper('#imgSwiperWrapper', {
                srcArr: srcArr,
                indicatorType: 'number',
                height: '300',
                widthRatio: 0.75,
                empty: '<p>暂无图片</p>'
            })
        },
        // 获取监控对象附件信息
        searchMedia: function (e, id, monitorName) {
            e.stopPropagation();
            json_ajax('get', '/clbs/m/driver/discern/statistics/mediaInfo', 'json', true, {
                "id": id,
            }, function (data) {
                if (data.success) {
                    $('#peopleMediaTitle').html(monitorName);
                    $('#imgMediaModal').modal('show');
                    driverDiscernStatistics.renderPart2(data.obj.videoInfo.videoList);
                    driverDiscernStatistics.renderPart3(data.obj.imageInfo.imageList);
                }
            })
        },
        //刷新
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            simpleQueryParam = '';
            myTable.requestData();
        },
        //搜索
        searchTable: function () {
            simpleQueryParam = $("#simpleQueryParam").val();
            myTable.requestData();
        },
        //导出
        export: function () {
            if ($("#dataTable tbody tr td").hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据,无法导出");
                return;
            }
            if(getRecordsNum() > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var paramer = {
                "monitorIds": vehicleId,
                "identificationStartDate": startTime,
                "identificationEndDate": endTime,
                "identificationResult": comparisonResult,
                "identificationType": comparisonType,
                "simpleQueryParam": simpleQueryParam,
            };
            var url = "/clbs/m/driver/discern/statistics/export";
            exportExcelUsePost(url, paramer);
        },
        nvl: function (val) {
            if (val === undefined || val === null) {
                return '';
            }
            return val;
        },
        closeDetail: function () {
            $('#dataTable tbody tr.active').removeClass('active');
            $('#detail').removeClass('active')
        },
        renderCompareResult: function (result) {
            switch (result) {
                case 0:
                    return '匹配成功';
                case 1:
                    return '匹配失败';
                case 2:
                    return '超时';
                case 3:
                    return '没有启用该功能';
                case 4:
                    return '连接异常';
                case 5:
                    return '无指定人脸图片';
                case 6:
                    return '无人脸库';
                case 7:
                    return '匹配失败,人证不符';
                case 8:
                    return '匹配失败,比对超时';
                case 9:
                    return '匹配失败,无指定人脸信息';
                case 10:
                    return '无驾驶员图片';
                case 11:
                    return '终端人脸库为空';
                default:
                    return '--';
            }
        },
        renderCompareType: function (result) {
            switch (result) {
                case 0:
                    return '插卡比对';
                case 1:
                    return '巡检比对';
                case 2:
                    return '点火比对';
                case 3:
                    return '离开返回比对';
                case 4:
                    return '动态比对';
                default:
                    return '--';
            }
        },
        //解析位置信息
        getAlarmAddress: function (latitude, longitude, e) {
            var url = '/clbs/v/monitoring/address';
            var param = {addressReverse: [latitude, longitude, '', "", 'vehicle']};
            if (e) e.stopPropagation();
            $.ajax({
                type: "POST",//通常会用到两种：GET,POST。默认是：GET
                url: url,//(默认: 当前页地址) 发送请求的地址
                dataType: "json", //预期服务器返回的数据类型。"json"
                async: true, // 异步同步，true  false
                data: param,
                traditional: true,
                timeout: 8000, //超时时间设置，单位毫秒
                success: function (data) {//请求成功
                    if (!e) {
                        $('.driverAddress').html($.isPlainObject(data) ? '未定位' : data);
                    } else {
                        $(e.target).closest('td').html($.isPlainObject(data) ? '未定位' : data);
                    }
                },
            });
        },
        trClick: function (event) {
            event.stopPropagation();
            var monitor = $(this).find('.monitorId');
            if (monitor.length === 0 || $(this).hasClass('active')) return;
            $(this).addClass('active').siblings().removeClass('active');
            json_ajax("get", "/clbs/m/driver/discern/statistics/detail?id=" + monitor.attr('data-id') + "&time=" + monitor.attr('data-time') + "",
                "json", false, null, function (data) {
                    var $tbody = $('#detailTbody');
                    if (data.success) {
                        var result = data.obj;
                        if (result && result.length > 0) {
                            var dataInfo = result[0];
                            if (dataInfo.imageUrl) {
                                $('.driverPthoto').attr('src', dataInfo.imageUrl).show();
                                $('.noDriverImg').hide()
                            } else {
                                $('.driverPthoto').attr('src', '').hide();
                                $('.noDriverImg').show()
                            }
                            if (dataInfo.driverPhotoUrl) {
                                $('.comparePthoto').attr('src', dataInfo.driverPhotoUrl).show();
                                $('.noCompareImg').hide()
                            } else {
                                $('.comparePthoto').attr('src', '').hide();
                                $('.noCompareImg').show()
                            }
                            // $('.driverAddress').html(dataInfo.address);
                            driverDiscernStatistics.getAlarmAddress(dataInfo.latitude, dataInfo.longitude);

                            $('.compareTime').html(dataInfo.identificationTimeStr);
                            $('.compareType').html(driverDiscernStatistics.renderCompareType(dataInfo.identificationType));
                            $('.compareResultInfo').html(driverDiscernStatistics.renderCompareResult(dataInfo.identificationResult));

                            if (dataInfo.identificationResult === 0) {
                                $('.compareResultInfo').attr('class', 'compareResultInfo success');
                            } else if (dataInfo.identificationResult === 1) {
                                $('.compareResultInfo').attr('class', 'compareResultInfo errorInfo');
                            } else {
                                $('.compareResultInfo').attr('class', 'compareResultInfo');
                            }

                            $('#similarity').html((dataInfo.matchRate ? dataInfo.matchRate : 0) + '%');
                            $('#faceId').html(dataInfo.faceId ? dataInfo.faceId : '--');
                            $('#compareName').html(dataInfo.driverName ? dataInfo.driverName : '--');
                            $('#qualificationCard').html(dataInfo.cardNumber ? dataInfo.cardNumber : '--');
                            $('#compareMonitorName').html(dataInfo.monitorName ? dataInfo.monitorName : '--');
                            $('#compareOrgNmae').html(dataInfo.orgName ? dataInfo.orgName : '--');


                            $tbody.empty();
                            for (var i = 0; i < result.length; i++) {
                                var d = result[i];
                                var html = '<tr>';
                                html += '<td>' + (i + 1) + '</td>';
                                html += '<td>' + driverDiscernStatistics.nvl(d.identificationTimeStr) + '</td>';
                                html += '<td>' + driverDiscernStatistics.renderCompareResult(d.identificationResult) + '</td>';
                                html += '<td>' + (d.matchRate ? d.matchRate : 0) + '%</td>';
                                html += '<td>' + driverDiscernStatistics.renderCompareType(d.identificationType) + '</td>';
                                html += '<td>' + driverDiscernStatistics.nvl(d.faceId) + '</td>';
                                html += '<td>' + driverDiscernStatistics.nvl(d.monitorName) + '</td>';
                                html += '</tr>';
                                $tbody.append($(html));
                            }
                        } else {
                            $tbody.append('<tr><td>暂无数据</td></tr>');
                        }

                        $('#detail').addClass('active')

                    } else {
                        $tbody.append('<tr><td>暂无数据</td></tr>');
                        if (data.msg) {
                            layer.msg(data.msg);
                        }
                    }
                });
        },
        getNowDay: function () {
            var date = new Date();
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
            startTime = yesterdate;
            endTime = yesterdate;
            return yesterdate;
        },
        // 查看/隐藏更多参数
        hiddenparameterFn: function () {
            if ($(this).hasClass('active')) return;
            $('.mediaBtn').removeClass('active');
            $(this).addClass('active');
            var target = $(this).attr('id') + '-content';
            $('.mediaContent').hide();
            $('#' + target).show();
        },
    };
    $(function () {
        //时间
        driverDiscernStatistics.getsTheCurrentTime();
        var nowDay = driverDiscernStatistics.getNowDay();
        driverDiscernStatistics.init();
        $('#timeInterval').dateRangePicker({
            dateLimit: 31,
            isOffLineReportFlag: true,
            nowDate: nowDay,
            isShowHMS: false
        });

        // 图片视频附件切换
        $('#imgAttached').on('click', function () {
            $('#imgAttached').css('background', '#20b5f0');
            $('#vedioAttached').css('background', '#6dcff6');
            $('#vedioSwiperWrapper').hide();
            $('#imgSwiperWrapper').show();
        });
        $('#vedioAttached').on('click', function () {
            $('#vedioAttached').css('background', '#20b5f0');
            $('#imgAttached').css('background', '#6dcff6');
            $('#vedioSwiperWrapper').show();
            $('#imgSwiperWrapper').hide();
        });
        $("#imgMediaModal").on('hide.bs.modal', function (e) {
            $('#imgAttached').click();
            swiper1 && swiper1.destroy();
            swiper2 && swiper2.destroy();
        });

        // 查看/隐藏更多参数
        $(".mediaBtn").bind("click", driverDiscernStatistics.hiddenparameterFn);
        //组织树
        $("#groupSelect").bind("click", showMenuContent);
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var param = $("#groupSelect").val();
                driverDiscernStatistics.searchVehicleTree(param);
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
                    driverDiscernStatistics.searchVehicleTree(param);
                }
                isSearch = true;
            }, 500);
        });
        //刷新
        $("#refreshTable").bind("click", driverDiscernStatistics.refreshTable);
        $('#queryType').on('change', function () {
            var param = $("#groupSelect").val();
            driverDiscernStatistics.searchVehicleTree(param);
        });
        //导出
        $("#exportRisk").bind("click", driverDiscernStatistics.export);
        $(document).on('click', '#dataTable tr', driverDiscernStatistics.trClick);
        $(document).on('click', driverDiscernStatistics.closeDetail);
        $('#detail').on('click', function (event) {
            event.stopPropagation();
        });
    })
}(window, $))