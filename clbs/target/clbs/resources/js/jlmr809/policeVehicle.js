/* 吉林长春明瑞 809平台数据交互管理  jl/vehicle/list
* */
(function (window, $) {
    var policeVehicleUpNames = [],
        policeVehicleUpIds = [],
        policeIsSearch = true;

    var startTime, endTime; //停运车辆上传记录上报日期 开始 和 结束时间

    var zTreeIdJson = {}, zTreeIdJsonUp = {};
    var checkFlag = false, checkFlagUp = false; // 判断组织节点是否是勾选操作

    var size;// 当前权限监控对象数量
    var ifAllCheck = true; //刚进入页面小于5000自动勾选
    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询？

    /* 报警车辆设置 */
    var policeVehicleNames = [],
        policeVehicleIds = [],
        settingIsSearch = true;
    var policeVehicleInput = $("#policeVehicleInput"), //组织树policeVehicleTree输入框
        policeVehicleType = $("#policeVehicleType"); //组织树监控对象选择
    var policeVehiclePlatform = $("#policeVehicleForm select[name=\"policeVehiclePlatform\"]");
    var policeVehicleTableObject; //表格对象
    var policeVehicleTable = $("#policeVehicleTable"), //表格
        policeVehicleSearch = $("#policeVehicleSearch"), //表格搜索按钮
        policeVehicleQuery = $("#policeVehicleQuery"), //表格搜索输入框
        policeVehicleRefresh = $("#policeVehicleRefresh"), //表格刷新按钮
        policeVehicleColumn = $("#policeVehicleColumn"); //表格 列

    /* 停运车辆上传记录 */
    var policeVehicleUpInput = $("#policeVehicleUpInput"), //组织树policeVehicleTree输入框
        policeVehicleUpType = $("#policeVehicleUpType"); //组织树监控对象选择
    var policeVehicleUpExport = $("#policeVehicleUpExport"); //导出按钮
    var policeVehicleUpTime = $("#policeVehicleUpTime"); //日期
    var policeVehicleUpStatus = $("#policeVehicleUpStatus"),
        policeVehicleUpHandle = $("#policeVehicleUpHandle"),
        policeVehicleUpAlarmType = $("#policeVehicleUpAlarmType"); //报警处理状态 / 报警类型

    var policeVehicleUpTableObject; //表格对象

    var policeVehicleUpTable = $("#policeVehicleUpTable"), //表格
        policeVehicleUpSearch = $("#policeVehicleUpSearch"), //表格搜索按钮
        policeVehicleUpQuery = $("#policeVehicleUpQuery"), //表格搜索输入框
        policeVehicleUpRefresh = $("#policeVehicleUpRefresh"), //表格刷新按钮
        policeVehicleUpColumn = $("#policeVehicleUpColumn"); //表格 列

    var policeVehicleUploadForm = $("#policeVehicleUploadForm"); // 弹窗
    var settingTotal = policeVehicleUploadForm.find("#settingTotal"),
        settingSingle = policeVehicleUploadForm.find("#settingSingle");
    var uploadObject = policeVehicleUploadForm.find("#policeVehicleUploadObject"); // 弹窗监控对象 统一设置
    var policeVehicleUploadSubmit = policeVehicleUploadForm.find("#policeVehicleUploadSubmit"); // 弹窗提交按钮

    var policeVehicleUploadModel = {
        uploadType: null, // 1 批量上传 2 点击单个上传按钮
        init: function () {
            policeVehicleUploadModel.initTime(); // 初始化时间
            policeVehicleUploadModel.initUploadType(); //初始化上报设置 并根据值设置显示内容
            // console.log(policeVehicleUploadModel.uploadType)
            policeVehicleUploadForm.find("input[name='uploadType']").off().on("click", policeVehicleUploadModel.bindTypeChange);
            policeVehicleUploadSubmit.off().on('click', policeVehicleUploadModel.uploadSubmitCallback)
        },
        initTime: function (div) {
            var nowDay = jlmrCommonFun.getNowDay();
            var container = div ? div : settingTotal;
            var uploadDatetimes = container.find("input[name='uploadDatetime']");
            for (var i = 0, len = uploadDatetimes.length; i < len; i++) {
                var item = uploadDatetimes[i];
                $(item).dateRangePicker({
                    isShowHMS: true,
                    nowDate: nowDay,
                    isOffLineReportFlag: true,
                    wrap: '#policeVehicleTimeWrap'
                });
            }
        },
        initUploadType: function () {
            var objectName = '';
            policeVehicleUploadForm.find(":radio[name='uploadHandle'][value='1']").prop("checked", "checked");
            policeVehicleUploadForm.find(":radio[name='uploadAlarm'][value='0']").prop("checked", "checked");
            if (policeVehicleUploadModel.uploadType == 2) {
                policeVehicleUploadForm.find(".uploadModalSetting").hide();
                objectName = policeVehicleSetting.currentVehicle.name;
            } else if (policeVehicleUploadModel.uploadType == 1) {
                policeVehicleUploadForm.find(".uploadModalSetting").show();
                policeVehicleUploadForm.find(":radio[name='uploadType'][value='1']").prop("checked", "checked");
                var objectNames = policeVehicleSetting.selectedVehicle.map(function (item, index) {
                    return item.name;
                })
                objectName = objectNames.join("、")
            }
            uploadObject.val(objectName);
            settingTotal.show();
            settingSingle.hide().html('');
        },
        //上报设置  不同类型设置不同的内容
        bindTypeChange: function () {
            var type = $(this).val();
            if (type == 1) {
                settingTotal.fadeIn();
                settingSingle.hide();
            } else {
                if (settingSingle.html() == '' && policeVehicleUploadModel.uploadType == 1) {
                    var html = '';
                    for (var i = 0, len = policeVehicleSetting.selectedVehicle.length; i < len; i++) {
                        var item = policeVehicleSetting.selectedVehicle[i];
                        html += '<div class="singleItem" data-id="' + item.id + '">\n' +
                            '                                    <div class="form-group">\n' +
                            '                                        <label class="col-md-3 control-label">监控对象：</label>\n' +
                            '                                        <div class="col-md-9"><div style="padding: 6px 0">' + item.name + '</div></div>\n' +
                            '                                    </div>\n' +
                            '                                    <div class="form-group">\n' +
                            '                                        <label class="col-md-3 control-label"><span class="text-danger">*</span> 报警起止时间：</label>\n' +
                            '                                        <div class="col-md-9">\n' +
                            '                                            <input style="cursor: pointer; background-color: #fafafa;"\n' +
                            '                                                   class="form-control layer-date laydate-icon"\n' +
                            '                                                   name="uploadDatetime" readonly/>\n' +
                            '                                        </div>\n' +
                            '                                    </div>\n' +
                            '                                    <div class="form-group">\n' +
                            '                                        <label class="col-md-3 control-label"><span class="text-danger">*</span> 报警类型： </label>\n' +
                            '                                        <div class="col-md-3">\n' +
                            '                                            <select class="form-control" name="singleAlarm">\n' +
                            '                                                <option value="0" selected>紧急报警</option>\n' +
                            '                                                <option value="10">疲劳报警</option>\n' +
                            '                                                <option value="200">禁入报警</option>\n' +
                            '                                                <option value="201">禁出报警</option>\n' +
                            '                                                <option value="210" >偏航报警</option>\n' +
                            '                                                <option value="41">超速报警</option>\n' +
                            '                                                <option value="53">夜间行驶报警</option>\n' +
                            '                                            </select>\n' +
                            '                                        </div>\n' +
                            '                                        <label class="col-md-3 control-label"><span class="text-danger">*</span> 报警处理状态： </label>\n' +
                            '                                        <div class="col-md-3">\n' +
                            '                                            <select class="form-control" name="singleHandle">\n' +
                            '                                                <option value="1" selected>处理中</option>\n' +
                            '                                                <option value="2">已处理完毕</option>\n' +
                            '                                                <option value="3">不作处理</option>\n' +
                            '                                                <option value="4">将来处理</option>\n' +
                            '                                            </select>\n' +
                            '                                        </div>\n' +
                            '                                    </div>\n' +
                            '                                </div>'
                    }
                    settingSingle.html(html);
                    policeVehicleUploadModel.initTime(settingSingle);
                }
                settingSingle.fadeIn();
                settingTotal.hide();
            }
        },
        //上报提交
        uploadSubmitCallback: function () {
            var params;
            var type = policeVehicleUploadForm.find("input[name='uploadType']:checked").val(); // 单个设置 2   统一设置 1
            var monitorIds = [],
                url = policeVehicleUploadModel.uploadType == 2 || type == 1 ? '/clbs/jl/vehicle/alarm/single/upload' : '/clbs/jl/vehicle/alarm/batch/upload';
            if (policeVehicleUploadModel.uploadType == 2) {
                monitorIds.push(policeVehicleSetting.currentVehicle.id);
            } else {
                for (var i = 0, len = policeVehicleSetting.selectedVehicle.length; i < len; i++) {
                    monitorIds.push(policeVehicleSetting.selectedVehicle[i].id);
                }
            }
            if (type == 1) {
                var uploadDatetime = settingTotal.find("input[name='uploadDatetime']").val(),
                    times = uploadDatetime.split("--");
                var start = times[0], end = times[1];
                var handle = settingTotal.find("input[name='uploadHandle']:checked").val();
                var alarm = settingTotal.find("input[name='uploadAlarm']:checked").val();
                params = {
                    "monitorIds": monitorIds,
                    "alarmStartTime": start,
                    "alarmEndTime": end,
                    "alarmType": alarm,
                    "alarmHandleStatus": handle
                }
            } else {
                var single = settingSingle.find(".singleItem");
                var paramData = [];
                for (var i = 0, len = single.length; i < len; i++) {
                    var item = $(single[i]);
                    var id = item.attr("data-id");
                    var uploadDatetime = item.find("input[name='uploadDatetime']").val(),
                        times = uploadDatetime.split("--");
                    var start = times[0], end = times[1];
                    var handle = item.find("select[name='singleHandle']").val();
                    var alarm = item.find("select[name='singleAlarm']").val();
                    var param = {
                        "monitorId": id,
                        "alarmStartTime": start,
                        "alarmEndTime": end,
                        "alarmType": alarm,
                        "alarmHandleStatus": handle
                    }
                    paramData.push(param)
                }
                params = paramData;
            }
            json_ajax("POST", url, "json", false, JSON.stringify(params), function (res) {
                if (res.success) {
                    var reason = res.obj && res.obj.msg ? '（' + res.obj.msg + '）' : '';
                    var resdata = res.obj && res.obj.result, restext = resdata == 1 ? '上传成功' : '上传失败' + reason;
                    var trs = policeVehicleTable.find("tbody tr");
                    for (var j = 0, idLen = monitorIds.length; j < idLen; j++) {
                        for (var i = 0, len = trs.length; i < len; i++) {
                            var trItem = $(trs[i]), trId = trItem.find(".uploadBtn").attr("data-id");
                            if (trId == monitorIds[j]) {
                                trItem.find(".uploadStatusText").html(restext);
                            }
                        }
                    }
                    $("#policeVehicleUploadModal").modal("hide");
                    // policeVehicleSetting.selectedVehicle = [];
                } else {
                    if (res.msg != null) {
                        layer.msg(res.msg, {move: false});
                    }
                }
            }, null, 'application/json;charset=utf-8');
        }
    }
    //报警车辆设置
    policeVehicleSetting = {
        requestFlag: false,
        tableListUrl: "/clbs/jl/vehicle/809/dataInteractiveManage/vehicleSetList",
        tableId: "policeVehicleTable",
        tableList: [],
        treeUrl: "/clbs/a/search/reportFuzzySearch",
        treeId: "policeVehicleTree",
        treeData: [],
        selectedVehicle: [],
        currentVehicle: {},
        init: function () {
            policeVehicleSetting.initTree(); //组织树
            policeVehicleInput.bind("click", showMenuContent); //为组织树的输入框绑定点击事件
            policeVehiclePlatform.on('change', function () {
                // var plateFormId = $(this).val();
                policeVehicleInput.val("");
                policeVehicleSetting.initTree();
            });
            // policeVehicleSetting.initTable();
            policeVehicleSetting.settingTableColumn(); //表格设置列
            policeVehicleTableObject = jlmrCommonFun.initDataTable('#' + policeVehicleSetting.tableId);
            policeVehicleSearch.off().on("click", function () {
                policeVehicleSetting.inquireClick(1);
            });
            // 模糊查询
            policeVehicleQuery.keyup(function (event) {
                if (event.keyCode == 13) {
                    policeVehicleSetting.inquireClick(1);
                }
            });
            policeVehicleRefresh.bind("click", policeVehicleSetting.refreshTable); //刷新表格绑定点击事件
        },
        //监控对象组织树
        initTree: function () {
            var setting = {
                async: {
                    url: '/clbs/jl/vehicle/809/dataInteractiveManage/tree',
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "id": policeVehiclePlatform.val(),
                        "queryType": policeVehicleType.val(),
                        'queryParam': policeVehicleInput.val()
                    },
                    dataFilter: policeVehicleSetting.ajaxTreeDataFilter
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
                    beforeClick: policeVehicleSetting.beforeClickVehicle,
                    onAsyncSuccess: policeVehicleSetting.zTreeOnAsyncSuccess,
                    beforeCheck: policeVehicleSetting.zTreeBeforeCheck,
                    onCheck: policeVehicleSetting.onCheckVehicle,
                    onNodeCreated: policeVehicleSetting.zTreeOnNodeCreated,
                }
            };
            $.fn.zTree.init($("#" + policeVehicleSetting.treeId), setting, null);
        },
        ajaxTreeDataFilter: function (treeId, parentNode, responseData) {
            var nodesArr = jlmrCommonFun.handleTreeDataFilter(responseData, policeVehicleType.val(), crrentSubV);
            if (nodesArr.length === 0) {
                $('#policeVehicleNotree').html('未查询到匹配项').show();
            } else {
                $('#policeVehicleNotree').html('未查询到匹配项').hide();
            }
            policeVehicleSetting.treeData = nodesArr;
            return nodesArr;
        },
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(policeVehicleSetting.treeId);
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj(policeVehicleSetting.treeId);
            policeVehicleSetting.getCharSelect(treeObj);
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                var allChecked = [];
                //groupNodes 选中的企业或组织下的监控对象
                var nodesLength = 0, checkedLen = [], groupNodes = [];
                var zTree = $.fn.zTree.getZTreeObj(policeVehicleSetting.treeId), nodes = zTree
                    .getCheckedNodes(true), v = "";
                var nodeId = '';
                //获取选中的监控对象
                for (var i = 0; i < nodes.length; i++) {
                    nodeId = nodes[i].id;
                    if (nodes[i].type == "people" || nodes[i].type == "vehicle") {
                        if ($.inArray(nodeId, checkedLen) == -1) {
                            checkedLen.push(nodes[i]);
                        }
                    }
                }

                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                    function getIdByArray(arr) {
                        arr.forEach(function (item) {
                            if (item.type == "people" || item.type == "vehicle") {
                                var checkedFlag = false;
                                for (var i = 0, len = checkedLen.length; i < len; i++) {
                                    if (checkedLen[i].pId == item.pId && checkedLen[i].id == item.id) {
                                        checkedFlag = true;
                                    }
                                }
                                if (!checkedFlag) groupNodes.push(item);
                            } else if (treeNode.type == "group" || treeNode.type == "assignment") {
                                if (item.children) getIdByArray(item.children);
                            }
                        })
                    }

                    if (treeNode.children) getIdByArray(treeNode.children);
                } else if (treeNode.type == "people" || treeNode.type == "vehicle") {
                    checkedLen.push(treeNode);
                }
                allChecked = checkedLen.concat(groupNodes);
                var checkedLen = allChecked.length;
                if (checkedLen > 499) {
                    // console.log(checkedLen)
                    layer.msg("最多勾选500个监控对象");
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
        onCheckVehicle: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(policeVehicleSetting.treeId);
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                settingIsSearch = false;
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    policeVehicleSetting.getCheckedNodes();
                    policeVehicleSetting.validates();
                }, 600);
            }
            policeVehicleSetting.getCharSelect(zTree);
            policeVehicleSetting.getCheckedNodes();
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

        getCharSelect: function (treeObj) {
            var nodes = treeObj.getCheckedNodes(true);
            var allNodes = treeObj.getNodes();
            if (nodes.length > 0) {
                policeVehicleInput.val(allNodes[0].name);
            } else {
                policeVehicleInput.val("");
            }
        },
        getCheckedNodes: function () {
            var checks = jlmrCommonFun.getCheckedNodes(policeVehicleSetting.treeId)
            policeVehicleNames = checks.names;
            policeVehicleIds = checks.ids;
        },
        searchVehicleTree: function (param) {
            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                policeVehicleSetting.initTree();
            } else {
                bflag = true;
                var querySetting = {
                    async: {
                        url: '/clbs/jl/vehicle/809/dataInteractiveManage/tree',
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {
                            "id": policeVehiclePlatform.val(),
                            "queryType": policeVehicleType.val(),
                            'queryParam': policeVehicleInput.val()
                        },
                        dataFilter: policeVehicleSetting.ajaxTreeDataFilter
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
                        beforeClick: policeVehicleSetting.beforeClickVehicle,
                        onCheck: policeVehicleSetting.fuzzyOnCheckVehicle,
                        onExpand: policeVehicleSetting.zTreeOnExpand,
                        onNodeCreated: policeVehicleSetting.zTreeOnNodeCreated
                    }
                };
                $.fn.zTree.init($("#" + policeVehicleSetting.treeId), querySetting, null);
            }
        },
        fuzzyOnCheckVehicle: function (e, treeId, treeNode) {
            //获取树结构
            var zTree = $.fn.zTree.getZTreeObj(policeVehicleSetting.treeId);
            //获取勾选状态改变的节点
            var changeNodes = zTree.getChangeCheckedNodes();
            if (treeNode.checked) { //若是取消勾选事件则不触发5000判断
                var checkedNodes = zTree.getCheckedNodes(true);
                var nodesLength = 0;
                for (var i = 0; i < checkedNodes.length; i++) {
                    if (checkedNodes[i].type === "people" || checkedNodes[i].type === "vehicle") {
                        nodesLength += 1;
                    }
                }
                setTimeout(() => {
                    policeVehicleSetting.getCheckedNodes();
                    policeVehicleSetting.validates();
                }, 600);
            }
            //获取勾选状态被改变的节点并改变其原来勾选状态（用于5000准确校验）
            for (i = 0; i < changeNodes.length; i++) {
                changeNodes[i].checkedOld = changeNodes[i].checked;
            }
            policeVehicleSetting.getCheckedNodes(); // 记录勾选的节点
        },
        //表格显示列设置
        settingTableColumn: function () {
            var columnData = [
                {name: "监控对象", column: 3},
                {name: "上传状态", column: 4},
                {name: "上传类型", column: 5},
                {name: "车牌颜色", column: 6},
                {name: "分组", column: 7},
                {name: "所属企业", column: 8}
            ];
            var html = jlmrCommonFun.settingTableColumn(columnData);
            policeVehicleColumn.html(html);
        },
        //初始化表格
        initTableAjax: function () {
            if (policeVehicleTableObject && !policeVehicleTableObject.dataTable) jlmrCommonFun.destroyTable(policeVehicleTableObject);
            var columns = [
                    {data: null},
                    {
                        data: null, render: function (data, type, row, meta) {
                            return '<input type="checkbox" name="tdChecked" value="' + row.id + '" data-name="' + row.monitorName + '" />';
                        }
                    },
                    {
                        data: null, render: function (data, type, row, meta) {
                            return '<button style="padding: 4px 8px" data-toggle="modal" data-target="#policeVehicleUploadModal" class="btn btn-primary uploadBtn" data-id="' + row.id + '" data-name="' + row.monitorName + '" type="button"><i style="font-size: 10px;" class="fa fa-upload"></i> 上传</button>';
                        }
                    },
                    {data: "monitorName"},
                    {
                        data: "uploadStatus", render: function (data) {
                            return "<div class='uploadStatusText'>" + "--" + "</div>";
                        }
                    },
                    {
                        data: "uploadType", render: function () {
                            return '报警车辆';
                        }
                    },
                    {data: "plateColorStr"},
                    {data: "assignmentNames"},
                    {data: "groupName"}
                ],
                columnDef = [{
                    //第一列，用来显示序号
                    "searchable": false,
                    "orderable": false,
                    "targets": 0
                }]
            var ajaxDataParamFun = function (param) {
                param.vehicleIds = policeVehicleIds;
                param.simpleQueryParam = policeVehicleQuery.val();
                param.queryType = 2;
            };
            //表格setting
            var setting = {
                // type: "GET",
                lengthMenu: [10, 20, 50],
                listUrl: policeVehicleSetting.tableListUrl,
                columnDefs: columnDef, //表格列定义
                columns: columns, //表格列
                dataTableDiv: policeVehicleSetting.tableId, //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                ajaxCallBack: policeVehicleSetting.drawTableCallbackFun,
                drawCallbackFun: function () {
                    var api = policeVehicleTableObject.dataTable;
                    var startIndex = api.context[0]._iDisplayStart;//获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                    });
                }
            };
            policeVehicleTableObject = new TG_Tabel.createNew(setting);
            policeVehicleTableObject.init();

            policeVehicleColumn.find('.toggle-checkbox').off().on('change', function (e) {
                var column = policeVehicleTableObject.dataTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(this).parent().parent().parent().parent().addClass("open");
            });

            //全选
            policeVehicleTable.on('click', '#checkAll', function () {
                policeVehicleTable.find("input[name='tdChecked']").prop("checked", this.checked);
                if (!this.checked) {
                    policeVehicleSetting.selectedVehicle = [];
                    return;
                }
                var selected = [];
                for (var i = 0, len = policeVehicleSetting.tableList.length; i < len; i++) {
                    var item = policeVehicleSetting.tableList[i];
                    selected.push({
                        id: item.id,
                        name: item.monitorName
                    })
                }
                policeVehicleSetting.selectedVehicle = selected;
            });
            //单选
            policeVehicleTable.on('click', 'input[name="tdChecked"]', function () {
                var _this = $(this),
                    flag = _this.prop("checked"),
                    id = _this.val(),
                    name = _this.attr("data-name");
                if (flag) {
                    policeVehicleSetting.selectedVehicle.push({
                        id: id,
                        name: name
                    })
                } else {
                    var index = jlmrCommonFun.getArrayIndex(id, 'id', policeVehicleSetting.selectedVehicle);
                    policeVehicleSetting.selectedVehicle.splice(index, 1);
                }
                var allLen = policeVehicleTable.find('input[name="tdChecked"]').length,
                    checkedLen = policeVehicleSetting.selectedVehicle.length;
                policeVehicleTable.find("#checkAll").prop("checked", allLen == checkedLen ? true : false);
            });

            //上传按钮
            policeVehicleTable.on('click', '.uploadBtn', function () {
                policeVehicleUploadModel.uploadType = 2;
                policeVehicleSetting.currentVehicle.id = $(this).attr("data-id");
                policeVehicleSetting.currentVehicle.name = $(this).attr("data-name");
            });
        },
        drawTableCallbackFun: function (data) {
            policeVehicleSetting.tableList = [];
            policeVehicleUploadModel.uploadType = null;
            policeVehicleSetting.selectedVehicle = [];
            policeVehicleSetting.currentVehicle = {};
            policeVehicleTable.find("#checkAll").prop("checked", false);
            var checks = policeVehicleColumn.find(".toggle-checkbox");
            for (var i = 0, len = checks.length; i < len; i++) {
                (function (index) {
                    var columnIndex = $(checks[index]).attr('data-column'), checked = $(checks[index]).prop("checked");
                    var column = policeVehicleTableObject.dataTable.column(columnIndex);
                    column.visible(checked);
                }(i))
            }
            if (data.records != null && data.records.length != 0) {
                policeVehicleSetting.requestFlag = false;
                policeVehicleSetting.tableList = data.records.length && data.records;
            }
        },
        //表单验证
        validates: function () {
            return $("#policeVehicleForm").validate({
                rules: {
                    policeVehiclePlatform: {
                        required: true
                    },
                    groupSelect: {
                        zTreeChecked: policeVehicleSetting.treeId
                    }
                },
                messages: {
                    policeVehiclePlatform: {
                        required: '请选择平台名称'
                    },
                    groupSelect: {
                        zTreeChecked: vehicleSelectBrand
                    }
                }
            }).form();
        },
        //查询点击事件
        inquireClick: function (number) {
            // policeVehicleQuery.val(""); // 删除模糊搜索关键字
            if (!policeVehicleSetting.validates()) {
                return;
            }
            ;
            policeVehicleSetting.getCheckedNodes();
            var ids = policeVehicleIds.split(",");
            ids = ids.filter(function (item) {
                return item;
            })
            if (ids.length > TREE_MAX_CHILDREN_LENGTH) {
                layer.msg("最多勾选" + TREE_MAX_CHILDREN_LENGTH + "个监控对象");
                return false;
            }
            policeVehicleSetting.initTableAjax();
            return false;
            var parameter = {
                "vehicleIds": policeVehicleIds.length ? policeVehicleIds : '',
                "simpleQueryParam": policeVehicleQuery.val(),
                "queryType": 2
            };
            json_ajax("POST", policeVehicleSetting.tableListUrl, "json", true, parameter, policeVehicleSetting.getDataCallback);
        },
        //刷新表格
        refreshTable: function () {
            policeVehicleQuery.val("");
            policeVehicleSetting.inquireClick(1);
        }
    }
    //报警车辆上传记录
    policeVehicleUpData = {
        requestFlag: false,
        tableList: [],
        exportUrl: "/clbs/jl/vehicle/alarm/export",
        treeUrl: "/clbs/a/search/reportFuzzySearch",
        treeId: "policeVehicleUpTree",
        tableListUrl: "/clbs/jl/vehicle/alarm/page",
        tableId: "policeVehicleUpTable",
        init: function () {
            // 初始渲染表头
            var nowDay = jlmrCommonFun.getNowDay();
            startTime = nowDay;
            endTime = nowDay;
            policeVehicleUpTime.dateRangePicker({isShowHMS: false, nowDate: nowDay, isOffLineReportFlag: true});
            policeVehicleUpData.initTree(); //组织树
            policeVehicleUpInput.bind("click", showMenuContent); //为组织树的输入框绑定点击事件
            // policeVehicleUpData.initTable();
            policeVehicleUpData.settingTableColumn(); //表格设置列
            policeVehicleUpTableObject = jlmrCommonFun.initDataTable('#' + policeVehicleUpData.tableId);
            //搜索
            policeVehicleUpSearch.off().on("click", function () {
                policeVehicleUpData.inquireClick(1);
            });
            // 模糊查询
            policeVehicleUpQuery.keyup(function (event) {
                if (event.keyCode == 13) {
                    policeVehicleUpData.inquireClick(1);
                }
            });
            //导出按钮绑定点击事件
            policeVehicleUpExport.bind("click", function () {
                policeVehicleUpData.getCheckedNodes();
                if (!policeVehicleUpData.tableList.length) {
                    layer.msg("暂未查询到可以导出的数据");
                    return false;
                }
                var param = {
                    monitorIdsStr: policeVehicleUpIds,
                    simpleQueryParam: policeVehicleUpQuery.val(),
                    uploadStartDate: startTime,
                    uploadEndDate: endTime
                }
                var status = policeVehicleUpStatus.val(),
                    handleStatus = policeVehicleUpHandle.val(),
                    alarmType = policeVehicleUpAlarmType.val();
                if (status) param.uploadState = status;
                if (handleStatus) param.alarmHandleStatus = handleStatus;
                if (alarmType) param.alarmType = alarmType;

                if(getRecordsNum('policeVehicleUpTable_info') > 60000){
                    return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
                }

                exportExcelUseForm(policeVehicleUpData.exportUrl, param);
            });
            policeVehicleUpRefresh.bind("click", policeVehicleUpData.refreshTable); //刷新表格绑定点击事件
        },
        //监控对象组织树
        initTree: function () {
            var setting = {
                async: {
                    url: policeVehicleUpData.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: policeVehicleUpData.ajaxTreeDataFilter
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
                    beforeClick: policeVehicleUpData.beforeClickVehicle,
                    onAsyncSuccess: policeVehicleUpData.zTreeOnAsyncSuccess,
                    beforeCheck: policeVehicleUpData.zTreeBeforeCheck,
                    onCheck: policeVehicleUpData.onCheckVehicle,
                    onNodeCreated: policeVehicleUpData.zTreeOnNodeCreated,
                    onExpand: policeVehicleUpData.zTreeOnExpand
                }
            };
            $.fn.zTree.init($("#" + policeVehicleUpData.treeId), setting, null);
        },
        //获取tree的URL
        getTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle";
            }
        },
        ajaxTreeDataFilter: function (treeId, parentNode, responseData) {
            var treeObj = $.fn.zTree.getZTreeObj(policeVehicleUpData.treeId);
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
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(policeVehicleUpData.treeId);
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj(policeVehicleUpData.treeId);
            if (size <= 100 && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            policeVehicleUpData.getCharSelect(treeObj);
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj(policeVehicleUpData.treeId), nodes = zTree
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

                    //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                    var ns = [];
                    //节点id
                    var nodeId;
                    for (var i = 0; i < nodes.length; i++) {
                        nodeId = nodes[i].id;
                        if (nodes[i].type == "people" || nodes[i].type == "vehicle") {
                            //查询该节点是否在勾选组织或分组下，若在则不记录，不在则记录
                            var nd = zTree.getNodeByParam("tId", nodes[i].tId, treeNode);
                            if (nd == null && $.inArray(nodeId, ns) == -1) {
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength += ns.length;
                } else if (treeNode.type == "people" || treeNode.type == "vehicle") { //若勾选的为监控对象
                    var zTree = $.fn.zTree.getZTreeObj(policeVehicleUpData.treeId), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;
                    //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                    var ns = [];
                    //节点id
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
                    layer.msg(maxSelectItem);
                    // layer.msg('最多勾选100个监控对象');
                    flag = false;
                }
            }
            if (flag) {
                //若组织节点已被勾选，则是勾选操作，改变勾选操作标识
                if (treeNode.type == "group" && !treeNode.checked) {
                    checkFlagUp = true;
                }
            }
            return flag;
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(policeVehicleUpData.treeId);
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                policeIsSearch = false;
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
            }
            policeVehicleUpData.getCharSelect(zTree);
            policeVehicleUpData.getCheckedNodes();
        },
        zTreeOnNodeCreated: function (event, treeId, treeNode) {
            var id = treeNode.id.toString();
            var list = [];
            if (zTreeIdJsonUp[id] == undefined || zTreeIdJsonUp[id] == null) {
                list = [treeNode.tId];
                zTreeIdJsonUp[id] = list;
            } else {
                zTreeIdJsonUp[id].push(treeNode.tId)
            }
        },
        zTreeOnExpand: function (event, treeId, treeNode) {
            //判断是否是勾选操作展开的树(是则继续执行，不是则返回)
            if (treeNode.type == "group" && !checkFlagUp) {
                return;
            }
            //初始化勾选操作判断表示
            checkFlagUp = false;
            var treeObj = $.fn.zTree.getZTreeObj(policeVehicleUpData.treeId);
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
                            if (zTreeIdJsonUp[pid] != undefined) {
                                var parentTid = zTreeIdJsonUp[pid][0];
                                var parentNode = treeObj.getNodeByTId(parentTid);
                                if (parentNode && parentNode.children === undefined) {
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
                policeVehicleUpInput.val(allNodes[0].name);
            } else {
                policeVehicleUpInput.val("");
            }
        },
        getCheckedNodes: function () {
            var checks = jlmrCommonFun.getCheckedNodes(policeVehicleUpData.treeId)
            policeVehicleUpNames = checks.names;
            policeVehicleUpIds = checks.ids;
        },
        searchVehicleTree: function (param) {
            ifAllCheck = false;//模糊查询不自动勾选
            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                policeVehicleUpData.initTree();
            } else {
                bflag = true;
                var querySetting = {
                    async: {
                        url: policeVehicleUpData.treeUrl,
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"type": policeVehicleUpType.val(), "queryParam": param},
                        dataFilter: policeVehicleUpData.ajaxQueryTreeFilter
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
                        beforeClick: policeVehicleUpData.beforeClickVehicle,
                        onCheck: policeVehicleUpData.fuzzyOnCheckVehicle,
                        onExpand: policeVehicleUpData.zTreeOnExpand,
                        onNodeCreated: policeVehicleUpData.zTreeOnNodeCreated
                    }
                };
                $.fn.zTree.init($("#" + policeVehicleUpData.treeId), querySetting, null);
            }
        },
        fuzzyOnCheckVehicle: function (e, treeId, treeNode) {
            //获取树结构
            var zTree = $.fn.zTree.getZTreeObj(policeVehicleUpData.treeId);
            //获取勾选状态改变的节点
            var changeNodes = zTree.getChangeCheckedNodes();
            if (treeNode.checked) { //若是取消勾选事件则不触发5000判断
                var checkedNodes = zTree.getCheckedNodes(true);
                var nodesLength = 0;
                for (var i = 0; i < checkedNodes.length; i++) {
                    if (checkedNodes[i].type === "people" || checkedNodes[i].type === "vehicle") {
                        nodesLength += 1;
                    }
                }
            }
            //获取勾选状态被改变的节点并改变其原来勾选状态（用于5000准确校验）
            for (i = 0; i < changeNodes.length; i++) {
                changeNodes[i].checkedOld = changeNodes[i].checked;
            }
            policeVehicleUpData.getCheckedNodes(); // 记录勾选的节点
        },
        ajaxQueryTreeFilter: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData))
            var nodesArr;
            if (policeVehicleUpType.val() == "vehicle") {
                nodesArr = filterQueryResult(responseData, crrentSubV);
            } else {
                nodesArr = responseData;
            }
            for (var i = 0; i < nodesArr.length; i++) {
                nodesArr[i].open = true;
            }
            return nodesArr;
        },
        //初始化
        initTableAjax: function () {
            if (policeVehicleUpTableObject && !policeVehicleUpTableObject.dataTable) jlmrCommonFun.destroyTable(policeVehicleUpTableObject);
            var columns = [
                    {data: null},
                    {data: "monitorName"},
                    {
                        data: "alarmStartTime", render: function (data) {
                            return data || '--'
                        }
                    },
                    {
                        data: "alarmEndTime", render: function (data) {
                            return data || '--'
                        }
                    },
                    {
                        data: "alarmTypeStr", render: function (data) {
                            return data || '--'
                        }
                    },
                    {
                        data: "alarmStatusStr", render: function (data) {
                            return data || '--'
                        }
                    },
                    {
                        data: "plateColorStr", render: function (data) {
                            return data || '--'
                        }
                    },
                    {data: "groupName"},
                    {
                        data: "uploadTimeStr", render: function (data) {
                            return data || '--'
                        }
                    },
                    {
                        data: "uploadStateStr", render: function (data) {
                            return data || '--'
                        }
                    },
                    {
                        data: "operator", render: function (data) {
                            return data || '--'
                        }
                    },
                ],
                columnDef = [{
                    //第一列，用来显示序号
                    "searchable": false,
                    "orderable": false,
                    "targets": 0
                }]
            var ajaxDataParamFun = function (param) {
                var status = policeVehicleUpStatus.val(),
                    handleStatus = policeVehicleUpHandle.val(),
                    alarmType = policeVehicleUpAlarmType.val();
                param.simpleQueryParam = policeVehicleUpQuery.val(); //模糊查询
                param.monitorIdsStr = policeVehicleUpIds;
                param.uploadStartDate = startTime;
                param.uploadEndDate = endTime;
                param.uploadState = status || '';
                param.alarmHandleStatus = handleStatus || '';
                param.alarmType = alarmType || '';
            };
            //表格setting
            var setting = {
                lengthMenu: [10, 20, 50],
                listUrl: policeVehicleUpData.tableListUrl,
                columnDefs: columnDef, //表格列定义
                columns: columns, //表格列
                dataTableDiv: policeVehicleUpData.tableId, //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                ajaxCallBack: policeVehicleUpData.drawTableCallbackFun,
                drawCallbackFun: function () {
                    var api = policeVehicleUpTableObject.dataTable;
                    var startIndex = api.context[0]._iDisplayStart;//获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                    });
                }
            };
            policeVehicleUpTableObject = new TG_Tabel.createNew(setting);
            policeVehicleUpTableObject.init();

            policeVehicleUpColumn.find('.toggle-checkbox').off().on('change', function (e) {
                var column = policeVehicleUpTableObject.dataTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(this).parent().parent().parent().parent().addClass("open");
            });
        },
        drawTableCallbackFun: function (data) {
            policeVehicleUpData.tableList = [];
            policeVehicleUpExport.removeAttr("disabled");
            var checks = policeVehicleUpColumn.find(".toggle-checkbox");
            for (var i = 0, len = checks.length; i < len; i++) {
                (function (index) {
                    var columnIndex = $(checks[index]).attr('data-column'), checked = $(checks[index]).prop("checked");
                    var column = policeVehicleUpTableObject.dataTable.column(columnIndex);
                    column.visible(checked);
                }(i))
            }
            if (data.records != null && data.records.length != 0) {
                policeVehicleUpData.requestFlag = false;
                policeVehicleUpData.tableList = data.records.length && data.records;
            }
        },
        //表格显示列设置
        settingTableColumn: function () {
            var columnData = [
                {name: "监控对象", column: 1},
                {name: "报警时间", column: 2},
                {name: "报警解除时间", column: 3},
                {name: "报警类型", column: 4},
                {name: "报警处理状态", column: 5},
                {name: "车牌颜色", column: 6},
                {name: "所属企业", column: 7},
                {name: "上报时间", column: 8},
                {name: "上传状态", column: 9},
                {name: "操作人", column: 10}
            ];
            var html = jlmrCommonFun.settingTableColumn(columnData);
            policeVehicleUpColumn.html(html);
        },
        //点击查询事件
        inquireClick: function (number) {
            var _time = policeVehicleUpTime.val();
            var times;
            if (number == 0) {
                times = jlmrCommonFun.getTheCurrentTime();
            } else if (number == -1) {
                times = jlmrCommonFun.getBeforeDay(-1, _time)
            } else if (number == -3) {
                times = jlmrCommonFun.getBeforeDay(-3, _time)
            } else if (number == -7) {
                times = jlmrCommonFun.getBeforeDay(-7, _time)
            }
            if (number != 1) {
                startTime = times.start;
                endTime = times.end;
                policeVehicleUpTime.val(startTime + '--' + endTime);
            } else {
                var timeInterval = _time.split('--');
                startTime = timeInterval[0];
                endTime = timeInterval[1];
            }
            ;
            policeVehicleUpData.getCheckedNodes();
            // if(ids.length > 100){
            //     layer.msg("最大支持选中100个组织，您已勾选" + ids.length + "个组织");
            //     return false;
            // }
            if (!policeVehicleUpData.validates()) {
                return;
            }
            ;
            // 判断时间范围
            if (jlmrCommonFun.compareTime(startTime, endTime) > 31) {
                layer.msg('最多可选择31天！');
                return;
            }
            policeVehicleUpData.initTableAjax();
            return false;
            var parameter = {};
            var status = policeVehicleUpStatus.val(),
                handleStatus = policeVehicleUpHandle.val(),
                alarmType = policeVehicleUpAlarmType.val();
            parameter.simpleQueryParam = policeVehicleUpQuery.val(); //模糊查询
            parameter.monitorIds = ids;
            parameter.uploadStartDate = startTime;
            parameter.uploadEndDate = endTime;
            // parameter.start = 0;
            // parameter.length = 10;
            parameter.uploadState = status || null;
            parameter.alarmHandleStatus = handleStatus || null;
            parameter.alarmType = alarmType || null;
            json_ajax("POST", policeVehicleUpData.tableListUrl, "json", true, JSON.stringify(parameter), policeVehicleUpData.getDataCallback, null, 'application/json;charset=utf-8');
        },
        //刷新
        refreshTable: function () {
            policeVehicleUpQuery.val("");
            policeVehicleUpData.inquireClick(1);
        },
        validates: function () {
            return $("#policeVehicleUpForm").validate({
                rules: {
                    policeVehicleUpTime: {
                        required: true
                    }
                },
                messages: {
                    policeVehicleUpTime: {
                        required: '请选择时间'
                    }
                }
            }).form();
        },
    }
    $(function () {
        policeVehicleSetting.init();
        policeVehicleUpData.init();

        //监听组织树输入框清除事件
        $("input[name=groupSelect]").inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            var param = $("#" + id).val();
            if (id == 'policeVehicleUpInput') {
                policeVehicleUpData.searchVehicleTree(param);
            }
            ;
            if (id == 'policeVehicleInput') {
                policeVehicleSetting.searchVehicleTree(param);
            }
            ;
        });

        /**
         * 上传记录 监控对象树模糊查询
         */
        var policeVehicleUpTreeTimer;
        policeVehicleUpInput.on('input propertychange', function (value) {
            if (policeVehicleUpTreeTimer !== undefined) {
                clearTimeout(policeVehicleUpTreeTimer);
            }
            ;
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                policeIsSearch = true;
            }
            ;
            policeVehicleUpTreeTimer = setTimeout(function () {
                if (policeIsSearch) {
                    var param = policeVehicleUpInput.val();
                    policeVehicleUpData.searchVehicleTree(param);
                }
                policeIsSearch = true;
            }, 500);
        });
        policeVehicleUpType.on('change', function () {
            var param = policeVehicleUpInput.val();
            policeVehicleUpData.searchVehicleTree(param);
        });

        /**
         * 设置 监控对象树模糊查询
         */
        var policeSettingTreeTimer;
        policeVehicleInput.on('input propertychange', function (value) {
            if (policeSettingTreeTimer !== undefined) {
                clearTimeout(policeSettingTreeTimer);
            }
            ;
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                settingIsSearch = true;
            }
            ;
            policeSettingTreeTimer = setTimeout(function () {
                if (settingIsSearch) {
                    var param = policeVehicleInput.val();
                    policeVehicleSetting.searchVehicleTree(param);
                }
                settingIsSearch = true;
            }, 500);
        });
        policeVehicleType.on('change', function () {
            var param = policeVehicleInput.val();
            policeVehicleSetting.searchVehicleTree(param);
        });

        $("#policeVehicleBatchBtn").off().on("click", function () {
            if (!policeVehicleSetting.selectedVehicle.length) {
                layer.msg("请勾选需要上传的行！");
                return false;
            }
            policeVehicleUploadModel.uploadType = 1;
            $("#policeVehicleUploadModal").modal("show");
        });

        //监听上传弹窗显示
        $('#policeVehicleUploadModal').on('show.bs.modal', function () {
            if (!policeVehicleUploadModel.uploadType) return false;
            policeVehicleUploadModel.init();
        }).on('hidden.bs.modal', function () {
            laydatePro.destroy("#policeVehicleUploadModal input[name='uploadDatetime']");
            $("#policeVehicleTimeWrap").html("");
            policeVehicleUploadModel.uploadType = null;
            // policeVehicleSetting.selectedVehicle = [];
            policeVehicleSetting.currentVehicle.id = '';
            policeVehicleSetting.currentVehicle.name = '';
        })
    })
}(window, $))
