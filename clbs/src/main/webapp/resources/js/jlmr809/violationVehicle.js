(function (window, $) {
    var violatVehicleUpNames = [],
        violatVehicleUpIds = [],
        policeIsSearch = true;

    var startTime, endTime; //停运车辆上传记录上报日期 开始 和 结束时间

    var zTreeIdJson = {}, zTreeIdJsonSet = {};
    var checkFlag = false, checkFlagSet = {}; // 判断组织节点是否是勾选操作

    var size;// 当前权限监控对象数量
    var ifAllCheck = true; //刚进入页面小于5000自动勾选
    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询？

    /* 违规车辆设置 */
    var violatVehicleNames = [],
        violatVehicleIds = [],
        settingIsSearch = true;
    var violatVehicleInput = $("#violatVehicleInput"), //组织树violatVehicleTree输入框
        violatVehicleType = $("#violatVehicleType"); //组织树监控对象选择
    var violatVehicleTableObject; //表格对象
    var violatVehicleTable = $("#violatVehicleTable"), //表格
        violatVehicleSearch = $("#violatVehicleSearch"), //表格搜索按钮
        violatVehicleQuery = $("#violatVehicleQuery"), //表格搜索输入框
        violatVehicleRefresh = $("#violatVehicleRefresh"), //表格刷新按钮
        violatVehicleColumn = $("#violatVehicleColumn"); //表格 列
    var violatVehiclePlatform = $("#violatVehiclePlatform");

    /* 违规车辆上传记录 */
    var violatVehicleUpInput = $("#violatVehicleUpInput"), //组织树violatVehicleTree输入框
        violatVehicleUpType = $("#violatVehicleUpType"); //组织树监控对象选择
    var violatVehicleUpExport = $("#violatVehicleUpExport"); //导出按钮
    var violatVehicleUpTime = $("#violatVehicleUpTime"); //日期
    var violatVehicleUpStatus = $("#violatVehicleUpStatus"),
        violatVehicleUpAlarmType = $("#violatVehicleUpAlarmType"); //报警处理状态 / 报警类型

    var violatVehicleUpTableObject; //表格对象

    var violatVehicleUpTable = $("#violatVehicleUpTable"), //表格
        violatVehicleUpSearch = $("#violatVehicleUpSearch"), //表格搜索按钮
        violatVehicleUpQuery = $("#violatVehicleUpQuery"), //表格搜索输入框
        violatVehicleUpRefresh = $("#violatVehicleUpRefresh"), //表格刷新按钮
        violatVehicleUpColumn = $("#violatVehicleUpColumn"); //表格 列

    var violatVehicleUploadForm = $("#violatVehicleUploadForm"); // 弹窗
    var settingTotal = violatVehicleUploadForm.find("#settingTotal"),
        settingSingle = violatVehicleUploadForm.find("#settingSingle");
    var uploadObject = violatVehicleUploadForm.find("#violatVehicleUploadObject"); // 弹窗监控对象 统一设置
    var violatVehicleUploadSubmit = violatVehicleUploadForm.find("#violatVehicleUploadSubmit"); // 弹窗提交按钮

    var timeFlag = true;

    var uploadModel = {
        uploadType: null, // 1 批量上传 2 点击单个上传按钮
        init: function () {
            uploadModel.initTime(); // 初始化时间
            uploadModel.initUploadType(); //初始化上报设置 并根据值设置显示内容
            // console.log(uploadModel.uploadType)
            violatVehicleUploadForm.find("input[name='uploadType']").off().on("click", uploadModel.bindTypeChange);
            violatVehicleUploadSubmit.off().on('click', uploadModel.uploadSubmitCallback)
        },
        initTime: function (div) {
            var nowDay = jlmrCommonFun.getNewTime();
            var container = div ? div : settingTotal;
            var uploadDatetimes = container.find("input[name='uploadDatetime']");
            for (var i = 0, len = uploadDatetimes.length; i < len; i++) {
                var item = uploadDatetimes[i];
                laydate.render({
                    elem: $(item)[0],
                    theme: '#6dcff6',
                    type: 'datetime',
                    format: 'yyyy-MM-dd HH:mm:ss',
                    max: uploadModel.getNowFormatDate(timeFlag)
                });
                $(item).val(nowDay);
            }
        },
        initUploadType: function () {
            var objectName = '';
            violatVehicleUploadForm.find(":radio[name='uploadHandle'][value='1']").prop("checked", "checked");
            violatVehicleUploadForm.find(":radio[name='uploadAlarm'][value='0']").prop("checked", "checked");
            if (uploadModel.uploadType == 2) {
                violatVehicleUploadForm.find(".uploadModalSetting").hide();
                objectName = violatVehicleSetting.currentVehicle.name;
            } else if (uploadModel.uploadType == 1) {
                violatVehicleUploadForm.find(".uploadModalSetting").show();
                violatVehicleUploadForm.find(":radio[name='uploadType'][value='1']").prop("checked", "checked");
                var objectNames = violatVehicleSetting.selectedVehicle.map(function (item, index) {
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
                if (settingSingle.html() == '' && uploadModel.uploadType == 1) {
                    var html = '';
                    for (var i = 0, len = violatVehicleSetting.selectedVehicle.length; i < len; i++) {
                        var item = violatVehicleSetting.selectedVehicle[i];
                        html += '<div class="singleItem" data-id="' + item.id + '">\n' +
                            '                                    <div class="form-group">\n' +
                            '                                        <label class="col-md-3 control-label">监控对象：</label>\n' +
                            '                                        <div class="col-md-9"><div style="padding: 6px 0">' + item.name + '</div></div>\n' +
                            '                                    </div>\n' +
                            '                                    <div class="form-group">\n' +
                            '                                        <label class="col-md-3 control-label"><span class="text-danger">*</span> 违规时间：</label>\n' +
                            '                                        <div class="col-md-4">\n' +
                            '                                            <input style="cursor: pointer; background-color: #fafafa;"\n' +
                            '                                                   class="form-control layer-date laydate-icon"\n' +
                            '                                                   name="uploadDatetime" readonly/>\n' +
                            '                                        </div>\n' +
                            '                                        <label class="col-md-2 control-label"><span class="text-danger">*</span> 违规类型： </label>\n' +
                            '                                        <div class="col-md-3">\n' +
                            '                                            <select class="form-control" name="singleAlarm">\n' +
                            '                                                <option value="1" selected>扭动镜头</option>\n' +
                            '                                                <option value="2">遮挡镜头</option>\n' +
                            '                                                <option value="3">无照片</option>\n' +
                            '                                                <option value="4">无定位数据</option>\n' +
                            '                                                <option value="5" >轨迹异常</option>\n' +
                            '                                                <option value="6">超员</option>\n' +
                            '                                                <option value="7">超速</option>\n' +
                            '                                                <option value="8">脱线运行</option>\n' +
                            '                                            </select>\n' +
                            '                                        </div>\n' +
                            '                                    </div>\n' +
                            '                                </div>'
                    }
                    settingSingle.html(html);
                    uploadModel.initTime(settingSingle);
                }
                settingSingle.fadeIn();
                settingTotal.hide();
            }
        },
        //上报提交
        uploadSubmitCallback: function () {
            $("#violatVehicleUploadSubmit").prop('disabled', true);
            var params;
            var type = violatVehicleUploadForm.find("input[name='uploadType']:checked").val(); // 单个设置 2   统一设置 1
            var monitorIds = [],
                url = uploadModel.uploadType == 2 || type == 1 ? '/clbs/jl/vehicle//violate/single/upload' : '/clbs/jl/vehicle/violate/batch/upload';
            if (uploadModel.uploadType == 2) {
                monitorIds.push(violatVehicleSetting.currentVehicle.id);
            } else {
                for (var i = 0, len = violatVehicleSetting.selectedVehicle.length; i < len; i++) {
                    monitorIds.push(violatVehicleSetting.selectedVehicle[i].id);
                }
            }
            if (type == 1) {
                var type = settingTotal.find("input[name='uploadAlarm']:checked").val();
                var time = settingTotal.find("input[name='uploadDatetime']").val();
                params = {
                    "monitorIds": monitorIds,
                    "violateTime": time,
                    "type": type,
                }
            } else {
                var single = settingSingle.find(".singleItem");
                var paramData = [];
                for (var i = 0, len = single.length; i < len; i++) {
                    var item = $(single[i]);
                    var id = item.attr("data-id");
                    var violateTime = item.find("input[name='uploadDatetime']").val();
                    var type = item.find("select[name='singleAlarm']").val();
                    var param = {
                        "monitorId": id,
                        'violateTime': violateTime,
                        'type': type

                    }
                    paramData.push(param)
                }
                params = paramData;
            }
            json_ajax("POST", url, "json", false, JSON.stringify(params), function (res) {
                if (res.success) {
                    var reason = res.obj && res.obj.msg ? '（' + res.obj.msg + '）' : '';
                    var resdata = res.obj && res.obj.result, restext = resdata == 1 ? '上传成功' : '上传失败' + reason;
                    //由于表格数据切换可能会导致索引超过表格所有数据的问题
                    var trs = violatVehicleTable.find("tbody tr");
                    for (var j = 0, idLen = monitorIds.length; j < idLen; j++) {
                        for (var i = 0, len = trs.length; i < len; i++) {
                            var trItem = $(trs[i]), trId = trItem.find(".uploadBtn").attr("data-id");
                            if (trId == monitorIds[j]) {
                                trItem.find(".uploadStatusText").html(restext);
                            }
                        }
                    }
                    $("#violatVehicleUploadModal").modal("hide");
                } else if (!res.success) {
                    layer.msg('操作失败，请稍后再试！')
                }
                $("#violatVehicleUploadSubmit").prop('disabled', false);
            }, null, 'application/json;charset=utf-8');
        },
        //获取当前时间，格式YYYY-MM-DD
        getNowFormatDate: function (timeFlag) {
            //调用了公共方法
            if (!timeFlag) {
                return jlmrCommonFun.getNewTime(); //YY-MM-DD hh:mm:ss
            } 
                return (jlmrCommonFun.getNowDay() + ' ' + 23 + ':' + 59 + ":00"); //YY-MM-DD
            

        },
    }
    //违规车辆设置
    violatVehicleSetting = {
        requestFlag: false,
        tableListUrl: "/clbs/jl/vehicle/809/dataInteractiveManage/vehicleSetList",
        tableId: "violatVehicleTable",
        tableList: [],
        treeUrl: "/clbs/a/search/reportFuzzySearch",
        treeId: "violatVehicleTree",
        treeData: [],
        selectedVehicle: [],
        currentVehicle: {},
        init: function () {
            $('[data-toggle=\'tooltip\']').tooltip();
            violatVehicleSetting.initTree(); //组织树
            violatVehicleInput.bind("click", showMenuContent); //为组织树的输入框绑定点击事件
            // violatVehicleSetting.initTable();
            violatVehicleSetting.settingTableColumn(); //表格设置列

            // 搜索
            violatVehicleSearch.off().on("click", function () {
                violatVehicleSetting.inquireClick(1);
            });
            // 模糊查询
            violatVehicleQuery.keyup(function (event) {
                if (event.keyCode == 13) {
                    violatVehicleSetting.inquireClick(1);
                }
            });
            // 刷新
            violatVehicleRefresh.bind("click", violatVehicleSetting.refreshTable); //刷新表格绑定点击事件
            //切换平台
            violatVehiclePlatform.bind('change', violatVehicleSetting.changePlatform)

            $('[data-toggle=\'tooltip\']').tooltip();
            violatVehicleColumn.find('.toggle-checkbox').off().on('change', function (e) {
                var column = violatVehicleTableObject.dataTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(this).parent().parent().parent().parent().addClass("open");
            });
        },

        changePlatform: function () {
            $("#violatVehicleInput").val('');
            violatVehicleSetting.initTree();
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
                        "id": $('select[name="violatVehiclePlatform"]').val(),
                        "queryType": violatVehicleType.val(),
                        'queryParam': violatVehicleInput.val()
                    },
                    dataFilter: violatVehicleSetting.ajaxTreeDataFilter
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
                    beforeClick: violatVehicleSetting.beforeClickVehicle,
                    onAsyncSuccess: violatVehicleSetting.zTreeOnAsyncSuccess,
                    beforeCheck: violatVehicleSetting.zTreeBeforeCheck,
                    onCheck: violatVehicleSetting.onCheckVehicle,
                    onNodeCreated: violatVehicleSetting.zTreeOnNodeCreated,
                }
            };
            $.fn.zTree.init($("#" + violatVehicleSetting.treeId), setting, null);
        },
        ajaxTreeDataFilter: function (treeId, parentNode, responseData) {
            var nodesArr = [];
            if (responseData.success) {
                responseData = JSON.parse(ungzip(responseData.obj));
                if (violatVehicleType.val() == "vehicle") {
                    nodesArr = filterQueryResult(responseData, crrentSubV);
                } else {
                    nodesArr = responseData;
                }
                for (var i = 0; i < nodesArr.length; i++) {
                    nodesArr[i].open = true;
                }
            }
            if (nodesArr.length === 0) {
                $('#violatVehicleNotree').html('未查询到匹配项').show();
            } else {
                $('#violatVehicleNotree').html('未查询到匹配项').hide();
            }
            violatVehicleSetting.treeData = nodesArr;
            return nodesArr;
        },
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(violatVehicleSetting.treeId);
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj(violatVehicleSetting.treeId);
            violatVehicleSetting.getCharSelect(treeObj);
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                var nodesLength = 0, checkedLen = [];
                var zTree = $.fn.zTree.getZTreeObj(violatVehicleSetting.treeId), nodes = zTree
                    .getCheckedNodes(true), v = "";
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                    var ns = [];
                    violatVehicleSetting.treeData.forEach(function (item) {
                        if (item.pId == treeNode.id) {
                            ns.push(item.id)
                        }
                    })
                    nodesLength += ns.length;
                } else if (treeNode.type == "people" || treeNode.type == "vehicle") {
                    nodesLength += 1;
                }
                var nodeId = '';
                for (var i = 0; i < nodes.length; i++) {
                    nodeId = nodes[i].id;
                    if (nodes[i].type == "people" || nodes[i].type == "vehicle") {
                        if ($.inArray(nodeId, checkedLen) == -1) {
                            checkedLen.push(nodeId);
                        }
                    }
                }
                nodesLength += checkedLen.length;
                if (nodesLength > TREE_MAX_CHILDREN_LENGTH) {
                    layer.msg("最多勾选" + TREE_MAX_CHILDREN_LENGTH + "个监控对象");
                    flag = false;
                }
            }
            if (flag) {
                //若组织节点已被勾选，则是勾选操作，改变勾选操作标识
                if (treeNode.type == "group" && !treeNode.checked) {
                    checkFlagSet = true;
                }
            }
            return flag;
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(violatVehicleSetting.treeId);
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                settingIsSearch = false;
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    violatVehicleSetting.getCheckedNodes();
                    violatVehicleSetting.validates();
                }, 600);
            }
            violatVehicleSetting.getCharSelect(zTree);
            violatVehicleSetting.getCheckedNodes();
        },
        zTreeOnNodeCreated: function (event, treeId, treeNode) {
            var id = treeNode.id.toString();
            var list = [];
            if (zTreeIdJsonSet[id] == undefined || zTreeIdJsonSet[id] == null) {
                list = [treeNode.tId];
                zTreeIdJsonSet[id] = list;
            } else {
                zTreeIdJsonSet[id].push(treeNode.tId)
            }
        },
        getCharSelect: function (treeObj) {
            var nodes = treeObj.getCheckedNodes(true);
            var allNodes = treeObj.getNodes();
            if (nodes.length > 0) {
                violatVehicleInput.val(allNodes[0].name);
            } else {
                violatVehicleInput.val("");
            }
        },
        getCheckedNodes: function () {
            var checks = jlmrCommonFun.getCheckedNodes(violatVehicleSetting.treeId)
            violatVehicleNames = checks.names;
            violatVehicleIds = checks.ids;
        },
        searchVehicleTree: function (param) {
            ifAllCheck = false;//模糊查询不自动勾选
            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                violatVehicleSetting.initTree();
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
                            "id": $('select[name="violatVehiclePlatform"]').val(),
                            "queryType": violatVehicleType.val(),
                            'queryParam': violatVehicleInput.val()
                        },
                        dataFilter: violatVehicleSetting.ajaxTreeDataFilter
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
                        beforeClick: violatVehicleSetting.beforeClickVehicle,
                        onCheck: violatVehicleSetting.fuzzyOnCheckVehicle,
                        onExpand: violatVehicleSetting.zTreeOnExpand,
                        onNodeCreated: violatVehicleSetting.zTreeOnNodeCreated
                    }
                };
                $.fn.zTree.init($("#" + violatVehicleSetting.treeId), querySetting, null);
            }
        },
        fuzzyOnCheckVehicle: function (e, treeId, treeNode) {
            //获取树结构
            var zTree = $.fn.zTree.getZTreeObj(violatVehicleSetting.treeId);
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
                    violatVehicleSetting.getCheckedNodes();
                    violatVehicleSetting.validates();
                }, 600);
            }
            //获取勾选状态被改变的节点并改变其原来勾选状态（用于5000准确校验）
            for (i = 0; i < changeNodes.length; i++) {
                changeNodes[i].checkedOld = changeNodes[i].checked;
            }
            violatVehicleSetting.getCheckedNodes(); // 记录勾选的节点
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
            violatVehicleColumn.html(html);
        },
        //初始化表格
        initTable: function (searchType) {
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
            }, {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return '<input type="checkbox" name="tdChecked" value="' + row.id + '" data-name="' + row.monitorName + '" />';
                }
            },
                {
                    "data": null,
                    "class": "text-center", //最后一列，操作按钮
                    render: function (data, type, row, meta) {
                        return '<button style="padding: 4px 8px" data-toggle="modal" data-target="#violatVehicleUploadModal" class="btn btn-primary uploadBtn" data-id="' + row.id + '" data-name="' + row.monitorName + '" type="button"><i class="fa fa-upload"></i> 上传</button>';
                    }
                }, {
                    "data": "monitorName",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "uploadStatus",
                    "class": "text-center",
                    render: function (data) {
                        return "<div class='uploadStatusText'>" + "--" + "</div>";
                    }
                }, {
                    "data": "uploadType",
                    "class": "text-center",
                    render: function () {
                        return '报警车辆';
                    }
                }, {
                    "data": "plateColorStr",
                    "class": "text-center"
                }, {
                    "data": "assignmentNames",
                    "class": "text-center"
                }, {
                    'data': 'groupName',
                    "class": "text-center"
                }];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.vehicleIds = violatVehicleIds.length ? violatVehicleIds : '',
                    d.simpleQueryParam = violatVehicleQuery.val(),//模糊查询
                    d.queryType = 2
            };
            //表格setting
            var setting = {
                listUrl: violatVehicleSetting.tableListUrl,
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                lengthMenu: [10, 20, 50],
                dataTableDiv: violatVehicleSetting.tableId, //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                ajaxCallBack: violatVehicleSetting.drawTableCallbackFun,
                drawCallbackFun: function () {
                    var api = violatVehicleTableObject.dataTable;
                    var startIndex = api.context[0]._iDisplayStart;//获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                    });
                    violatVehicleSetting.selectedVehicle = [];
                }
            };
            //创建表格
            violatVehicleTableObject = new TG_Tabel.createNew(setting);
            //表格初始化
            violatVehicleTableObject.init();

            //全选
            violatVehicleTable.on('click', '#checkAll', function () {
                violatVehicleTable.find("input[name='tdChecked']").prop("checked", this.checked);
                if (!this.checked) {
                    violatVehicleSetting.selectedVehicle = [];
                    return;
                }
                var selected = [];
                for (var i = 0, len = violatVehicleSetting.tableList.length; i < len; i++) {
                    var item = violatVehicleSetting.tableList[i];
                    selected.push({
                        id: item.id,
                        name: item.monitorName
                    })
                }
                violatVehicleSetting.selectedVehicle = selected;
            });
            //单选
            violatVehicleTable.on('click', 'input[name="tdChecked"]', function () {
                var _this = $(this),
                    flag = _this.prop("checked"),
                    id = _this.val(),
                    name = _this.attr("data-name");
                if (flag) {
                    violatVehicleSetting.selectedVehicle.push({
                        id: id,
                        name: name
                    })
                } else {
                    var index = jlmrCommonFun.getArrayIndex(id, 'id', violatVehicleSetting.selectedVehicle);
                    violatVehicleSetting.selectedVehicle.splice(index, 1);
                }
                var allLen = violatVehicleTable.find('input[name="tdChecked"]').length,
                    checkedLen = violatVehicleSetting.selectedVehicle.length;
                violatVehicleTable.find("#checkAll").prop("checked", allLen == checkedLen ? true : false);
            });

            //上传按钮
            violatVehicleTable.on('click', '.uploadBtn', function () {
                uploadModel.uploadType = 2;
                violatVehicleSetting.currentVehicle.id = $(this).attr("data-id");
                violatVehicleSetting.currentVehicle.name = $(this).attr("data-name");
            });
        },

        drawTableCallbackFun: function (data) {
            violatVehicleSetting.tableList = [];
            uploadModel.uploadType = null;
            violatVehicleSetting.selectedVehicle = [];
            violatVehicleSetting.currentVehicle = {};
            violatVehicleTable.find("#checkAll").prop("checked", false);
            var checks = violatVehicleColumn.find(".toggle-checkbox");
            for (var i = 0, len = checks.length; i < len; i++) {
                (function (index) {
                    var columnIndex = $(checks[index]).attr('data-column'), checked = $(checks[index]).prop("checked");
                    var column = violatVehicleTableObject.dataTable.column(columnIndex);
                    column.visible(checked);
                }(i))
            }
            if (data.records != null && data.records.length != 0) {
                violatVehicleSetting.requestFlag = false;
                violatVehicleSetting.tableList = data.records.length && data.records;
            }
        },

        //表单验证
        validates: function () {
            return $("#violatVehicleForm").validate({
                rules: {
                    violatVehiclePlatform: {
                        required: true
                    },
                    groupSelect: {
                        zTreeChecked: violatVehicleSetting.treeId
                    }
                },
                messages: {
                    violatVehiclePlatform: {
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
            if (!violatVehicleSetting.validates()) {
                return;
            }
            ;
            violatVehicleSetting.getCheckedNodes();
            var ids = violatVehicleIds.split(",");
            var len = ids.length - 1;
            if (len > TREE_MAX_CHILDREN_LENGTH) {
                layer.msg("最多勾选" + TREE_MAX_CHILDREN_LENGTH + "个监控对象");
                return false;
            }

            violatVehicleSetting.initTable();
            // var parameter = {
            //     "vehicleIds": violatVehicleIds.length ? violatVehicleIds : '',
            //     "simpleQueryParam": violatVehicleQuery.val(),
            //     "queryType": 2
            // };
            // json_ajax("POST", violatVehicleSetting.tableListUrl, "json", true, parameter, violatVehicleSetting.getDataCallback);
        },
        //刷新表格
        refreshTable: function () {
            violatVehicleQuery.val("");
            violatVehicleSetting.inquireClick(1);
        }
    }
    //违规车辆上传记录
    violatVehicleUpData = {
        requestFlag: false,
        tableList: [],
        exportUrl: "/clbs/jl/vehicle/violate/export",
        treeUrl: "/clbs/a/search/reportFuzzySearch",
        treeId: "violatVehicleUpTree",
        tableListUrl: "/clbs/jl/vehicle/violate/page",
        tableId: "violatVehicleUpTable",
        init: function () {
            // 初始渲染表头
            var nowDay = jlmrCommonFun.getNowDay();
            startTime = nowDay;
            endTime = nowDay;
            violatVehicleUpTime.dateRangePicker({isShowHMS: false, nowDate: nowDay, isOffLineReportFlag: true});
            violatVehicleUpData.initTree(); //组织树
            violatVehicleUpInput.bind("click", showMenuContent); //为组织树的输入框绑定点击事件
            // violatVehicleUpData.initTable();
            violatVehicleUpData.settingTableColumn(); //表格设置列
            violatVehicleUpTableObject = jlmrCommonFun.initDataTable('#' + violatVehicleUpData.tableId);
            //搜索
            violatVehicleUpSearch.off().on("click", function () {
                violatVehicleUpData.inquireClick(1);
            });
            // 模糊查询
            violatVehicleUpQuery.keyup(function (event) {
                if (event.keyCode == 13) {
                    violatVehicleUpSearch.click();
                }
            });

            violatVehicleUpRefresh.bind("click", violatVehicleUpData.refreshTable); //刷新表格绑定点击事件

            //导出按钮绑定点击事件
            violatVehicleUpExport.bind("click", function () {
                violatVehicleUpData.getCheckedNodes();

                if (!violatVehicleUpData.tableList.length) {
                    layer.msg("暂未查询到可以导出的数据");
                    return false;
                }

                if(getRecordsNum('violatVehicleUpTable_info') > 60000){
                    return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
                }

                var monitorIds = [];
                var time = violatVehicleUpTime.val().split('--');
                var param = {
                    'monitorIds': violatVehicleUpIds == '' ? '' : monitorIds.push(violatVehicleUpIds),
                    'violateStartDate': time[0],
                    'violateEndDate': time[1],
                    'uploadState': violatVehicleUpStatus.val() == '' ? '' : violatVehicleUpStatus.val(),
                    'type': violatVehicleUpAlarmType.val() == '' ? '' : violatVehicleUpAlarmType.val(),
                    'simpleQueryParam': violatVehicleUpQuery.val() == '' ? '' : violatVehicleUpQuery.val()
                };
                exportExcelUseForm(violatVehicleUpData.exportUrl, param);
            });
        },
        //监控对象组织树
        initTree: function () {
            var setting = {
                async: {
                    url: violatVehicleUpData.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: violatVehicleUpData.ajaxTreeDataFilter
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
                    beforeClick: violatVehicleUpData.beforeClickVehicle,
                    onAsyncSuccess: violatVehicleUpData.zTreeOnAsyncSuccess,
                    beforeCheck: violatVehicleUpData.zTreeBeforeCheck,
                    onCheck: violatVehicleUpData.onCheckVehicle,
                    onNodeCreated: violatVehicleUpData.zTreeOnNodeCreated,
                    onExpand: violatVehicleUpData.zTreeOnExpand
                }
            };
            $.fn.zTree.init($("#" + violatVehicleUpData.treeId), setting, null);
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
            var treeObj = $.fn.zTree.getZTreeObj(violatVehicleUpData.treeId);
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
            var zTree = $.fn.zTree.getZTreeObj(violatVehicleUpData.treeId);
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj(violatVehicleUpData.treeId);
            if (size <= 100 && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            violatVehicleUpData.getCharSelect(treeObj);
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj(violatVehicleUpData.treeId), nodes = zTree
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
                    var zTree = $.fn.zTree.getZTreeObj(violatVehicleUpData.treeId), nodes = zTree
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
                    checkFlag = true;
                }
            }
            return flag;
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(violatVehicleUpData.treeId);
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                policeIsSearch = false;
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
            }
            violatVehicleUpData.getCharSelect(zTree);
            violatVehicleUpData.getCheckedNodes();
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
            var treeObj = $.fn.zTree.getZTreeObj(violatVehicleUpData.treeId);
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
                violatVehicleUpInput.val(allNodes[0].name);
            } else {
                violatVehicleUpInput.val("");
            }
        },
        getCheckedNodes: function () {
            var checks = jlmrCommonFun.getCheckedNodes(violatVehicleUpData.treeId)
            violatVehicleUpNames = checks.names;
            violatVehicleUpIds = checks.ids;
        },
        searchVehicleTree: function (param) {
            ifAllCheck = false;//模糊查询不自动勾选
            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                violatVehicleUpData.initTree();
            } else {
                bflag = true;
                var querySetting = {
                    async: {
                        url: violatVehicleUpData.treeUrl,
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"type": violatVehicleUpType.val(), "queryParam": param},
                        dataFilter: violatVehicleUpData.ajaxQueryTreeFilter
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
                        beforeClick: violatVehicleUpData.beforeClickVehicle,
                        onCheck: violatVehicleUpData.fuzzyOnCheckVehicle,
                        onExpand: violatVehicleUpData.zTreeOnExpand,
                        onNodeCreated: violatVehicleUpData.zTreeOnNodeCreated
                    }
                };
                $.fn.zTree.init($("#" + violatVehicleUpData.treeId), querySetting, null);
            }
        },
        fuzzyOnCheckVehicle: function (e, treeId, treeNode) {
            //获取树结构
            var zTree = $.fn.zTree.getZTreeObj(violatVehicleUpData.treeId);
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
            violatVehicleUpData.getCheckedNodes(); // 记录勾选的节点
        },
        ajaxQueryTreeFilter: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData))
            var nodesArr;
            if (violatVehicleUpType.val() == "vehicle") {
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
        initTable: function (n) {
            var _columns = [
                {data: null},
                {data: "monitorName"},
                {
                    data: "violateTime", render: function (data) {
                        return data ? data : '--'
                    }
                },
                {
                    data: "type", render: function (data) {
                        return data ? data : '--'
                    }
                },
                {
                    data: "plateColor", render: function (data) {
                        return data ? data : '--'
                    }
                },
                {
                    data: "groupName", render: function (data) {
                        return data ? data : '--'
                    }
                },
                {
                    data: "uploadTime", render: function (data) {
                        return data ? data : '--'
                    }
                },
                {data: "uploadState"},
                {data: "operator"},
            ];
            violatVehicleUpData.settingTableColumn(); //表格设置列
            violatVehicleUpTableObject = jlmrCommonFun.initDataTable('#' + violatVehicleUpData.tableId, _columns);
            //搜索
            violatVehicleUpSearch.off().on("click", function () {
                offOperateUpData.inquireClick(1);
            });
            // 模糊查询
            violatVehicleUpQuery.keyup(function (event) {
                if (event.keyCode == 13) {
                    violatVehicleUpSearch.click();
                }
            });
            violatVehicleUpColumn.find('.toggle-checkbox').off().on('change', function (e) {
                var column = violatVehicleUpTableObject.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(this).parent().parent().parent().parent().addClass("open");
            });
        },
        initTableAjax: function () {
            if (violatVehicleUpTableObject && !violatVehicleUpTableObject.dataTable) jlmrCommonFun.destroyTable(violatVehicleUpTableObject);
            var columns = [
                    {data: null},
                    {data: "monitorName"},
                    {
                        data: "violateTime", render: function (data) {
                            return data ? data : '--'
                        }
                    },
                    {
                        data: "type", render: function (data) {
                            return data ? data : '--'
                        }
                    },
                    {
                        data: "plateColor", render: function (data) {
                            return data ? data : '--'
                        }
                    },
                    {
                        data: "groupName", render: function (data) {
                            return data ? data : '--'
                        }
                    },
                    {
                        data: "uploadTime", render: function (data) {
                            return data ? data : '--'
                        }
                    },
                    {data: "uploadState"},
                    {data: "operator"},
                ],
                columnDef = [{
                    //第一列，用来显示序号
                    "searchable": false,
                    "orderable": false,
                    "targets": 0
                }]
            var ajaxDataParamFun = function (param) {
                param.violateStartDate = startTime;
                param.violateEndDate = endTime;
                param.type = violatVehicleUpAlarmType.val() == "" ? null : violatVehicleUpAlarmType.val();
                param.uploadState = violatVehicleUpStatus.val() == "" ? null : violatVehicleUpStatus.val();
                param.monitorIds = violatVehicleUpIds;
                param.simpleQueryParam = violatVehicleUpQuery.val() == "" ? null : violatVehicleUpQuery.val();
            };
            //表格setting
            var setting = {
                // type: "GET",
                lengthMenu: [10, 20, 50],
                listUrl: violatVehicleUpData.tableListUrl,
                columnDefs: columnDef, //表格列定义
                columns: columns, //表格列
                dataTableDiv: violatVehicleUpData.tableId, //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                ajaxCallBack: violatVehicleUpData.drawTableCallbackFun,
                drawCallbackFun: function () {
                    var api = violatVehicleUpTableObject.dataTable;
                    var startIndex = api.context[0]._iDisplayStart;//获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                    });
                }
            };
            violatVehicleUpTableObject = new TG_Tabel.createNew(setting);
            violatVehicleUpTableObject.init();


            violatVehicleUpRefresh.bind("click", violatVehicleUpData.refreshTable); //刷新表格绑定点击事件
            violatVehicleUpColumn.find('.toggle-checkbox').off().on('change', function (e) {
                var column = violatVehicleUpTableObject.dataTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(this).parent().parent().parent().parent().addClass("open");
            });
        },

        drawTableCallbackFun: function (data) {
            // offOperateUpData.tableList = [];
            // offOperateUpExport.removeAttr("disabled");


            // if (data.records != null && data.records.length != 0) {
            //     offOperateUpData.requestFlag = false;
            //     offOperateUpData.tableList = data.records.length && data.records;
            // }


            violatVehicleUpData.tableList = [];
            violatVehicleUpExport.removeAttr("disabled");
            var checks = violatVehicleUpColumn.find(".toggle-checkbox");
            for (var i = 0, len = checks.length; i < len; i++) {
                (function (index) {
                    var columnIndex = $(checks[index]).attr('data-column'), checked = $(checks[index]).prop("checked");
                    var column = violatVehicleUpTableObject.dataTable.column(columnIndex);
                    column.visible(checked);
                }(i))
            }
            if (data.records != null && data.records.length != 0) {
                violatVehicleUpData.requestFlag = false;
                violatVehicleUpData.tableList = data.records.length && data.records;
            }
        },

        //表格显示列设置
        settingTableColumn: function () {
            var columnData = [
                {name: "监控对象", column: 1},
                {name: "违规时间", column: 2},
                {name: "违规类型", column: 3},
                {name: "车牌颜色", column: 4},
                {name: "所属企业", column: 5},
                {name: "上报时间", column: 6},
                {name: "上传状态", column: 7},
                {name: "操作人", column: 8}
            ];
            var html = jlmrCommonFun.settingTableColumn(columnData);
            violatVehicleUpColumn.html(html);
        },
        //点击查询事件
        inquireClick: function (number) {
            var _time = violatVehicleUpTime.val();
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
                violatVehicleUpTime.val(startTime + '--' + endTime);
            } else {
                var timeInterval = _time.split('--');
                startTime = timeInterval[0];
                endTime = timeInterval[1];
            }
            ;
            violatVehicleUpData.getCheckedNodes();
            var ids = violatVehicleUpIds.split(",");
            ids = ids.filter(function (item) {
                return item;
            })
            // if(ids.length > 100){
            //     layer.msg("最大支持选中100个组织，您已勾选" + ids.length + "个组织");
            //     return false;
            // }
            if (!violatVehicleUpData.validates()) {
                return;
            }
            ;
            // 判断时间范围
            if (jlmrCommonFun.compareTime(startTime, endTime) > 31) {
                layer.msg('最多可选择31天！');
                return;
            }
            violatVehicleUpData.initTableAjax();
            return false;
            var parameter = {};
            parameter.violateStartDate = startTime;
            parameter.violateEndDate = endTime;
            parameter.type = violatVehicleUpAlarmType.val() == "" ? null : violatVehicleUpAlarmType.val();
            parameter.uploadState = violatVehicleUpStatus.val() == "" ? null : violatVehicleUpStatus.val();

            json_ajax("POST", violatVehicleUpData.tableListUrl, "json", true, JSON.stringify(parameter), violatVehicleUpData.getDataCallback, null, 'application/json;charset=utf-8');
        },
        getDataCallback: function (data) {
            if (data.success == true) {
                violatVehicleUpExport.removeAttr("disabled");
                if (data.records != null && data.records.length != 0) {
                    violatVehicleUpData.tableList = data.records;
                    violatVehicleUpData.reloadTableData(data.records);
                } else {
                    violatVehicleUpData.tableList = [];
                    violatVehicleUpData.reloadTableData([]);
                }
            } else {
                if (data.msg != null) {
                    layer.msg(data.msg, {move: false});
                }
            }
        },
        //更新表格数据
        reloadTableData: function (dataList) {
            var api = violatVehicleUpTableObject;
            var currentPage = api.page();
            api.clear();
            api.rows.add(dataList);
            api.page(currentPage).draw(false);
        },
        //刷新
        refreshTable: function () {
            violatVehicleUpQuery.val("");
            violatVehicleUpData.inquireClick(1);
        },
        validates: function () {
            return $("#violatVehicleUpForm").validate({
                rules: {
                    violatVehicleUpTime: {
                        required: true
                    }
                },
                messages: {
                    violatVehicleUpTime: {
                        required: '请选择时间'
                    }
                }
            }).form();
        },
    };
    $(function () {
        violatVehicleSetting.init();
        violatVehicleUpData.init();

        //监听组织树输入框清除事件
        $("input[name=groupSelect]").inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            var param = $("#" + id).val();
            if (id == 'violatVehicleUpInput') {
                violatVehicleUpData.searchVehicleTree(param);
            }
            ;
            if (id == 'violatVehicleInput') {
                violatVehicleSetting.searchVehicleTree(param);
            }
            ;
        });

        /**
         * 上传记录 监控对象树模糊查询
         */
        var violatVehicleUpTreeTimer;
        violatVehicleUpInput.on('input propertychange', function (value) {
            if (violatVehicleUpTreeTimer !== undefined) {
                clearTimeout(violatVehicleUpTreeTimer);
            }
            ;
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                policeIsSearch = true;
            }
            ;
            violatVehicleUpTreeTimer = setTimeout(function () {
                if (policeIsSearch) {
                    var param = violatVehicleUpInput.val();
                    violatVehicleUpData.searchVehicleTree(param);
                }
                policeIsSearch = true;
            }, 500);
        });
        violatVehicleUpType.on('change', function () {
            var param = violatVehicleUpInput.val();
            violatVehicleUpData.searchVehicleTree(param);
        });

        /**
         * 设置 监控对象树模糊查询
         */
        var policeSettingTreeTimer;
        violatVehicleInput.on('input propertychange', function (value) {
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
                    var param = violatVehicleInput.val();
                    violatVehicleSetting.searchVehicleTree(param);
                }
                settingIsSearch = true;
            }, 500);
        });
        violatVehicleType.on('change', function () {
            var param = violatVehicleInput.val();
            violatVehicleSetting.searchVehicleTree(param);
        });

        $("#violatVehicleBatchBtn").off().on("click", function () {
            if (!violatVehicleSetting.selectedVehicle.length) {
                layer.msg("请勾选需要上传的行！");
                return false;
            }
            uploadModel.uploadType = 1;
            $("#violatVehicleUploadModal").modal("show");
        });

        //监听上传弹窗显示
        $('#violatVehicleUploadModal').on('show.bs.modal', function () {
            if (!uploadModel.uploadType) return false;
            uploadModel.init();
        }).on('hidden.bs.modal', function () {
            laydatePro.destroy("#violatVehicleUploadModal input[name='uploadDatetime']");
            $("#violatVehicleTimeWrap").html("");
            uploadModel.uploadType = null;
            violatVehicleSetting.currentVehicle.id = '';
            violatVehicleSetting.currentVehicle.name = '';
        })
    })
}(window, $))