/* 吉林长春明瑞 809平台数据交互管理  jl/vehicle/list
* */
(function (window, $) {

    var offOperateUpNames = [],
        offOperateUpIds = [],
        operateIsSearch = true;

    var startTime, endTime; //停运车辆上传记录上报日期 开始 和 结束时间

    var zTreeIdJson = {};
    var checkFlag = false; // 判断组织节点是否是勾选操作

    var size;// 当前权限监控对象数量
    var ifAllCheck = true; //刚进入页面小于5000自动勾选
    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询？

    /* 停运车辆设置 */
    var offOperateTableObject; //表格对象
    var offOperateForm = $("#offOperateForm");
    var offOperateTime = $("#offOperateTime"); // 停运车辆查询日期
    var offOperateTable = $("#offOperateTable"), //表格
        offOperateSearch = $("#offOperateSearch"), //表格搜索按钮
        offOperateQuery = $("#offOperateQuery"), //表格搜索输入框
        offOperateRefresh = $("#offOperateRefresh"), //表格刷新按钮
        offOperateColumn = $("#offOperateColumn"); //表格 列

    var vehicleStatusOffline = $("input[name='vehicleStatusOffline']"),
        vehicleStatusUnloc = $("input[name='vehicleStatusUnloc']");

    /* 停运车辆上传记录 */
    var offOperateUpInput = $("#offOperateUpInput"), //组织树offOperateTree输入框
        offOperateUpType = $("#offOperateUpType"); //组织树监控对象选择
    var offOperateUpExport = $("#offOperateUpExport"); //导出按钮
    var offOperateUpTime = $("#offOperateUpTime"); //日期
    var offOperateUpStatus = $("#offOperateUpStatus"),
        offOperateUpSusReason = $("#offOperateUpSusReason"); //上传状态 报停原因

    var offOperateUpTableObject; //表格对象

    var offOperateUpTable = $("#offOperateUpTable"), //表格
        offOperateUpSearch = $("#offOperateUpSearch"), //表格搜索按钮
        offOperateUpQuery = $("#offOperateUpQuery"), //表格搜索输入框
        offOperateUpRefresh = $("#offOperateUpRefresh"), //表格刷新按钮
        offOperateUpColumn = $("#offOperateUpColumn"); //表格 列

    var offOperateUploadForm = $("#offOperateUploadForm"); // 弹窗
    var settingTotal = offOperateUploadForm.find("#settingTotal"), settingSingle = offOperateUploadForm.find("#settingSingle");
    var offOperateUploadObject = offOperateUploadForm.find("#offOperateUploadObject"); // 弹窗监控对象 统一设置
    var offOperateUploadSubmit = offOperateUploadForm.find("#offOperateUploadSubmit"); // 弹窗提交按钮

    var offOperateUploadModel = {
        uploadType: null, // 1 批量上传 2 点击单个上传按钮
        init: function () {
            offOperateUploadModel.initTime(); // 初始化时间
            offOperateUploadModel.initUploadType(); //初始化上报设置 并根据值设置显示内容
            offOperateUploadForm.find("input[name='uploadType']").off().on("click", offOperateUploadModel.bindTypeChange);
            offOperateUploadSubmit.off().on('click', offOperateUploadModel.uploadSubmitCallback)
        },
        //初始化时间
        initTime: function (div) {
            var nowDay = jlmrCommonFun.getNowDay();
            var container = div ? div : settingTotal;
            var uploadDatetimes = container.find("input[name='uploadDatetime']");
            for(var i = 0, len = uploadDatetimes.length; i < len; i++){
                var item = uploadDatetimes[i];
                $(item).dateRangePicker({isShowHMS: false, nowDate: nowDay, isOffLineReportFlag: true, wrap: '#offOperateTimeWrap'});
            }
        },
        //初始化上传弹窗部分表单的数据   以及根据上报设置 初始化不同的样式
        initUploadType: function () {
            offOperateUploadForm.find(":radio[name='uploadReason'][value='9']").prop("checked", "checked");
            var objectName = '';
            if(offOperateUploadModel.uploadType == 2){
                offOperateUploadForm.find(".uploadModalSetting").hide();
                objectName = offOperateSetting.currentVehicle.name;
            }else if(offOperateUploadModel.uploadType == 1){
                offOperateUploadForm.find(".uploadModalSetting").show();
                offOperateUploadForm.find(":radio[name='uploadType'][value='1']").prop("checked", "checked");
                var objectNames = offOperateSetting.selectedVehicle.map(function (item, index) {
                    return item.name;
                })
                objectName = objectNames.join("、")
            }
            offOperateUploadObject.val(objectName);
            settingTotal.show();
            settingSingle.hide().html('');
        },
        //上报设置  不同类型设置不同的内容
        bindTypeChange: function () {
            var type = $(this).val();
            if(type == 1){
                settingTotal.fadeIn();
                settingSingle.hide();
            }else{
                if(settingSingle.html() == '' && offOperateUploadModel.uploadType == 1){
                    var html = '';
                    for(var i = 0, len = offOperateSetting.selectedVehicle.length; i < len; i++){
                        var item = offOperateSetting.selectedVehicle[i];
                        html += '<div class="singleItem" data-id="' + item.monitorId + '">\n' +
                            '                                    <div class="form-group">\n' +
                            '                                        <label class="col-md-3 control-label">监控对象：</label>\n' +
                            '                                        <div class="col-md-9"><div style="padding: 6px 0">' + item.name + '</div></div>\n' +
                            '                                    </div>\n' +
                            '                                    <div class="form-group">\n' +
                            '                                        <label class="col-md-3 control-label"><span class="text-danger">*</span> 停运起止时间：</label>\n' +
                            '                                        <div class="col-md-4">\n' +
                            '                                            <input style="cursor: pointer; background-color: #fafafa;"\n' +
                            '                                                   class="form-control layer-date laydate-icon"\n' +
                            '                                                   name="uploadDatetime" readonly/>\n' +
                            '                                        </div>\n' +
                            '                                        <label class="col-md-2 control-label"><span class="text-danger">*</span> 报停原因：</label>\n' +
                            '                                        <div class="col-md-3">\n' +
                            '                                            <select class="form-control" name="singleReason">\n' +
                            '                                                <option value="1">天气原因</option>\n' +
                            '                                                <option value="2">车辆故障</option>\n' +
                            '                                                <option value="3">路阻</option>\n' +
                            '                                                <option value="4">终端报修</option>\n' +
                            '                                                <option value="9" selected>其他</option>\n' +
                            '                                            </select>\n' +
                            '                                        </div>\n' +
                            '                                    </div>\n' +
                            '                                </div>'
                    }
                    settingSingle.html(html);
                    offOperateUploadModel.initTime(settingSingle);
                }
                settingSingle.fadeIn();
                settingTotal.hide();
            }
        },
        //上报提交
        uploadSubmitCallback: function () {
            //    [{"monitorId":"123","startDate":"2020-06-14","endDate":"2020-06-15","stopCauseCode":"1"}]
            var params = [];
            var type = $("input[name='uploadType']:checked").val(); // 单个设置 2   统一设置 1
            if(type == 1){
                var uploadDatetime = settingTotal.find("input[name='uploadDatetime']").val(),
                    times = uploadDatetime.split("--");
                var start = times[0], end = times[1];
                var reason = settingTotal.find("input[name='uploadReason']:checked").val();
                if(offOperateUploadModel.uploadType == 2){
                    params.push({
                        "monitorId": offOperateSetting.currentVehicle.monitorId,
                        "startDate": start,
                        "endDate": end,
                        "stopCauseCode": reason
                    });
                }else{
                    for(var i = 0, len = offOperateSetting.selectedVehicle.length; i < len; i++){
                        params.push({
                            "monitorId": offOperateSetting.selectedVehicle[i].monitorId,
                            "startDate": start,
                            "endDate": end,
                            "stopCauseCode": reason
                        });
                    }
                }
            }else{
                var single = settingSingle.find(".singleItem");
                for(var i = 0, len = single.length; i < len; i++){
                    var item = $(single[i]);
                    var id = item.attr("data-id");
                    var uploadDatetime = item.find("input[name='uploadDatetime']").val(),
                        times = uploadDatetime.split("--");
                    var start = times[0], end = times[1];
                    var reason = item.find("select[name='singleReason']").val();
                    params.push({
                        "monitorId": id,
                        "startDate": start,
                        "endDate": end,
                        "stopCauseCode": reason
                    })
                }
            }
            if(params.length){
                var url = '/clbs/jl/vehicle/stopped/upload';
                var param = JSON.stringify(params);
                json_ajax("POST", url, "json", false, {str: param}, function (res) {
                    if (res.success) {
                        var reason = res.obj && res.obj.msg ? '（' + res.obj.msg + '）' : '';
                        var resdata = res.obj && res.obj.result, restext = resdata == 1 ? '上传成功' : '上传失败' + reason;

                        var trs = offOperateTable.find("tbody tr");
                        for(var j = 0, idLen = params.length; j < idLen; j++){
                            for(var i = 0, len = trs.length; i < len; i++){
                                var trItem = $(trs[i]), trId = trItem.find(".uploadBtn").attr("data-id");
                                if(trId == params[j].monitorId){
                                    trItem.find(".uploadStatusText").html(restext);
                                }
                            }
                        }
                        $("#offOperateUploadModal").modal("hide");
                        // offOperateSetting.selectedVehicle = [];
                    } else {
                        if (res.msg != null) {
                            layer.msg(res.msg, {move: false});
                        }
                    }
                });
            }
        }
    }
    //停运车辆设置
    offOperateSetting = {
        requestFlag: false,
        tableListUrl: "/clbs/jl/vehicle/stopped/page",
        tableId: "offOperateTable",
        tableList: [],
        selectedVehicle: [],
        currentVehicle: {},
        init: function () {
            var nowDay = jlmrCommonFun.getNowDay();
            laydate.render({elem: '#offOperateTime', theme: '#6dcff6', value: nowDay});
            offOperateTime.val(nowDay);
            $('[data-toggle=\'tooltip\']').tooltip();
            // offOperateSetting.initTable();
            offOperateSetting.settingTableColumn(); //表格设置列
            offOperateTableObject = jlmrCommonFun.initDataTable('#' + offOperateSetting.tableId);
            //搜索
            offOperateSearch.off().on("click", function () {
                offOperateSetting.inquireClick(1);
            });
            // 模糊查询
            offOperateQuery.keyup(function (event) {
                if (event.keyCode == 13) {
                    offOperateSetting.inquireClick(1);
                }
            });
            offOperateRefresh.bind("click", offOperateSetting.refreshTable); //刷新表格绑定点击事件
        },
        //表格显示列设置
        settingTableColumn: function () {
            var columnData = [
                {name: "监控对象", column: 3},
                {name: "上传状态", column: 4},
                {name: "上传类型", column: 5},
                {name: "车牌颜色", column: 6},
                {name: "车辆状态", column: 7},
                {name: "定位数", column: 8},
                {name: "无效定位数", column: 9},
                {name: "所属企业", column: 10}
            ];
            var html = jlmrCommonFun.settingTableColumn(columnData);
            offOperateColumn.html(html);
        },
        //初始化表格
        initTableAjax: function () {
            if(offOperateTableObject && !offOperateTableObject.dataTable) jlmrCommonFun.destroyTable(offOperateTableObject);
            var columns = [
                    { data: null },
                    { data: null, render: function (data, type, row, meta) {
                        return '<input type="checkbox" name="tdChecked" value="' + row.monitorId + '" data-name="' + row.monitorName + '" />';
                    } },
                    { data: null, render: function (data, type, row, meta) {
                        return '<button style="padding: 4px 8px" data-toggle="modal" data-target="#offOperateUploadModal" class="btn btn-primary uploadBtn" data-id="' + row.monitorId + '" data-name="' + row.monitorName + '" type="button"><i style="font-size: 10px;" class="fa fa-upload"></i> 上传</button>';
                    } },
                    { data: "monitorName"},
                    { data: "uploadStatus", render: function (data) {
                        return "<div class='uploadStatusText'>" + "--" + "</div>";
                    } },
                    { data: "uploadType", render: function () {
                        return '停运车辆';
                    } },
                    { data: "plateColor", render: function (data) {
                        return getPlateColor(data);
                    } },
                    { data: "status", render: function (data) {
                        return data == 0 ? '正常' : data == 1 ? '未定位' : '离线';
                    } },
                    { data: "totalNum", render: function (data) {
                        return data || 0
                    } },
                    { data: "invalidNum", render: function (data) {
                        return data ? data : 0;
                    }},
                    { data: "groupName" }
                ];
            var ajaxDataParamFun = function (param) {
                var time = offOperateTime.val(), status = 0;
                var vehicleOffline = vehicleStatusOffline.prop('checked'),
                    vehicleUnloc = vehicleStatusUnloc.prop('checked');
                if(vehicleOffline) {
                    if(vehicleUnloc){
                        status = 2;
                    }else{
                        status = 3;
                    }
                }else{
                    if(vehicleUnloc){
                        status = 1;
                    }else{
                        status = 0;
                    }
                }
                param.platformId = offOperateForm.find("select[name='offOperatePlatform']").val();
                param.simpleQueryParam = offOperateQuery.val(); //模糊查询
                param.date = time.replace(/\-/g, "");
                //查询类型 （0 全部 (正常 + 未定位 + 离线)  1	未定位  2	未定位 + 离线  3离线）
                param.type = status;
            };
            //表格setting
            var setting = {
                type: "GET",
                lengthMenu: [10, 20, 50],
                listUrl: offOperateSetting.tableListUrl,
                columnDefs: jlmrCommonFun.columnDef, //表格列定义
                columns: columns, //表格列
                dataTableDiv: offOperateSetting.tableId, //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                ajaxCallBack: offOperateSetting.drawTableCallbackFun,
                drawCallbackFun: function () {
                    jlmrCommonFun.initDataTableIndex(offOperateTableObject)
                }
            };
            offOperateTableObject = new TG_Tabel.createNew(setting);
            offOperateTableObject.init();

            offOperateColumn.find('.toggle-checkbox').off().on('change', function (e) {
                var column = offOperateTableObject.dataTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(this).parent().parent().parent().parent().addClass("open");
            });
            //全选
            offOperateTable.on('click', '#checkAll', function () {
                offOperateTable.find("input[name='tdChecked']").prop("checked", this.checked);
                if(!this.checked){
                    offOperateSetting.selectedVehicle = [];
                    return;
                }
                var selected = [];
                for(var i = 0, len = offOperateSetting.tableList.length; i < len; i++){
                    var item = offOperateSetting.tableList[i];
                    selected.push({
                        monitorId: item.monitorId,
                        name: item.monitorName
                    })
                }
                offOperateSetting.selectedVehicle = selected;
            });
            //单选
            offOperateTable.on('click', 'input[name="tdChecked"]', function () {
                var _this = $(this),
                    flag = _this.prop("checked"),
                    id = _this.val(),
                    name = _this.attr("data-name");
                if(flag) {
                    offOperateSetting.selectedVehicle.push({
                        monitorId: id,
                        name: name
                    })
                }else{
                    var index = jlmrCommonFun.getArrayIndex(id, 'monitorId', offOperateSetting.selectedVehicle);
                    offOperateSetting.selectedVehicle.splice(index, 1);
                }
                var allLen = offOperateTable.find('input[name="tdChecked"]').length,
                    checkedLen = offOperateSetting.selectedVehicle.length;
                offOperateTable.find("#checkAll").prop("checked", allLen == checkedLen ? true : false);
            });
            //上传按钮
            offOperateTable.on('click', '.uploadBtn', function () {
                offOperateUploadModel.uploadType = 2;
                offOperateSetting.currentVehicle.monitorId = $(this).attr("data-id");
                offOperateSetting.currentVehicle.name = $(this).attr("data-name");
            });
        },
        drawTableCallbackFun: function (data) {
            offOperateSetting.tableList = [];
            offOperateUploadModel.uploadType = null;
            offOperateSetting.selectedVehicle = [];
            offOperateSetting.currentVehicle = {};
            offOperateTable.find("#checkAll").prop("checked", false);
            var checks = offOperateColumn.find(".toggle-checkbox");
            for(var i = 0, len = checks.length; i < len; i++){
                (function (index) {
                    var columnIndex = $(checks[index]).attr('data-column'), checked = $(checks[index]).prop("checked");
                    var column = offOperateTableObject.dataTable.column(columnIndex);
                    column.visible(checked);
                }(i))
            }
            if (data.records != null && data.records.length != 0) {
                offOperateSetting.requestFlag = false;
                offOperateSetting.tableList = data.records.length && data.records;
            }
        },
        //表单验证
        validates: function () {
            return $("#offOperateForm").validate({
                rules: {
                    offOperatePlatform: {
                        required: true
                    },
                    offOperateTime: {
                        required: true
                    }
                },
                messages: {
                    offOperatePlatform: {
                        required: '请选择平台名称'
                    },
                    offOperateTime: {
                        required: '请选择日期'
                    }
                }
            }).form();
        },
        //查询点击事件
        inquireClick: function (number) {
            if (!offOperateSetting.validates()) {
                return;
            };
            var parameter = {};
            var time = offOperateTime.val(), status = 0;
            var vehicleOffline = vehicleStatusOffline.prop('checked'),
                vehicleUnloc = vehicleStatusUnloc.prop('checked');
            if(vehicleOffline) {
                if(vehicleUnloc){
                    status = 0;
                }else{
                    status = 3;
                }
            }else{
                if(vehicleUnloc){
                    status = 1;
                }else{
                    status = 2;
                }
            }
            offOperateSetting.initTableAjax();
            return false;
            parameter.platformId = offOperateForm.find("select[name='offOperatePlatform']").val();
            parameter.simpleQueryParam = offOperateQuery.val(); //模糊查询
            parameter.date = time.replace(/\-/g, "");
            //查询类型 （0 全部 (正常 + 未定位 + 离线)  1	未定位  2	未定位 + 离线  3离线）
            parameter.type = status;
            json_ajax("GET", offOperateSetting.tableListUrl, "json", true, parameter, offOperateSetting.getDataCallback);
        },
        //刷新表格
        refreshTable: function () {
            offOperateQuery.val("");
            offOperateTableObject.requestData();
        }
    }
    //停运车辆上传记录
    offOperateUpData = {
        requestFlag: false,
        tableList: [],
        exportUrl: "/clbs/jl/vehicle/export/stopped/recordPage",
        treeUrl: "/clbs/a/search/reportFuzzySearch",
        treeId: "offOperateUpTree",
        tableListUrl: "/clbs/jl/vehicle/stopped/recordPage",
        tableId: "offOperateUpTable",
        init: function () {
            // 初始渲染表头
            var nowDay = jlmrCommonFun.getNowDay();
            startTime = nowDay;
            endTime = nowDay;
            offOperateUpTime.dateRangePicker({dateLimit: 31, isShowHMS: false, nowDate: nowDay, isOffLineReportFlag: true});
            offOperateUpData.initTree(); //组织树
            offOperateUpInput.bind("click", showMenuContent); //为组织树的输入框绑定点击事件
            // offOperateUpData.initTable();
            offOperateUpData.settingTableColumn(); //表格设置列
            offOperateUpTableObject = jlmrCommonFun.initDataTable('#' + offOperateUpData.tableId);
            //搜索
            offOperateUpSearch.off().on("click", function () {
                offOperateUpData.inquireClick(1);
            });
            // 模糊查询
            offOperateUpQuery.keyup(function (event) {
                if (event.keyCode == 13) {
                    offOperateUpData.inquireClick(1);
                }
            });
            //导出按钮绑定点击事件
            offOperateUpExport.bind("click", function () {
                offOperateUpData.getCheckedNodes();
                if(!offOperateUpData.tableList.length){
                    layer.msg("暂未查询到可以导出的数据");
                    return false;
                }

                if(getRecordsNum('offOperateUpTable_info') > 60000){
                    return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
                }

                var param = {
                    ids: offOperateUpIds,
                    uploadDateStart: startTime + " 00:00:00",
                    uploadDateEnd: endTime + " 23:59:59"
                }
                var status = offOperateUpStatus.val(),
                    reason = offOperateUpSusReason.val();
                if(status != -1) param.state = status;
                if(reason != -1) param.stopCauseCode = reason;
                exportExcelUseForm(offOperateUpData.exportUrl,param);
            });
            offOperateUpRefresh.bind("click", offOperateUpData.refreshTable); //刷新表格绑定点击事件
        },
        //监控对象组织树
        initTree: function () {
            jlmrCommonFun.initTreeSetting(offOperateUpData.treeId, offOperateUpData.ajaxTreeDataFilter, offOperateUpData.beforeClickVehicle, offOperateUpData.zTreeOnAsyncSuccess, offOperateUpData.zTreeBeforeCheck, offOperateUpData.onCheckVehicle, offOperateUpData.zTreeOnNodeCreated, offOperateUpData.zTreeOnExpand);
            // var setting = {
            //     async: {
            //         url: offOperateUpData.getTreeUrl,
            //         type: "post",
            //         enable: true,
            //         autoParam: ["id"],
            //         dataType: "json",
            //         otherParam: {"type": "multiple", "icoType": "0"},
            //         dataFilter: offOperateUpData.ajaxTreeDataFilter
            //     },
            //     check: {
            //         enable: true,
            //         chkStyle: "checkbox",
            //         chkboxType: {
            //             "Y": "s",
            //             "N": "s"
            //         },
            //         radioType: "all"
            //     },
            //     view: {
            //         dblClickExpand: false,
            //         nameIsHTML: true,
            //         countClass: "group-number-statistics"
            //     },
            //     data: {
            //         simpleData: {
            //             enable: true
            //         }
            //     },
            //     callback: {
            //         beforeClick: offOperateUpData.beforeClickVehicle,
            //         onAsyncSuccess: offOperateUpData.zTreeOnAsyncSuccess,
            //         beforeCheck: offOperateUpData.zTreeBeforeCheck,
            //         onCheck: offOperateUpData.onCheckVehicle,
            //         onNodeCreated: offOperateUpData.zTreeOnNodeCreated,
            //         onExpand: offOperateUpData.zTreeOnExpand
            //     }
            // };
            // $.fn.zTree.init($("#" + offOperateUpData.treeId), setting, null);
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
            var zTree = $.fn.zTree.getZTreeObj(offOperateUpData.treeId);
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj(offOperateUpData.treeId);
            if (size <= 100 && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            offOperateUpData.getCharSelect(treeObj);
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj(offOperateUpData.treeId), nodes = zTree
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
                    var zTree = $.fn.zTree.getZTreeObj(offOperateUpData.treeId), nodes = zTree
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
            var zTree = $.fn.zTree.getZTreeObj(offOperateUpData.treeId);
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                operateIsSearch = false;
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
            }
            offOperateUpData.getCharSelect(zTree);
            offOperateUpData.getCheckedNodes();
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
            var treeObj = $.fn.zTree.getZTreeObj(offOperateUpData.treeId);
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
                offOperateUpInput.val(allNodes[0].name);
            } else {
                offOperateUpInput.val("");
            }
        },
        getCheckedNodes: function () {
            var checks = jlmrCommonFun.getCheckedNodes(offOperateUpData.treeId)
            offOperateUpNames = checks.names;
            offOperateUpIds = checks.ids;
        },
        searchVehicleTree: function (param) {
            ifAllCheck = false;//模糊查询不自动勾选
            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                offOperateUpData.initTree();
            } else {
                bflag = true;
                var querySetting = {
                    async: {
                        url: offOperateUpData.treeUrl,
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"type": offOperateUpType.val(), "queryParam": param},
                        dataFilter: offOperateUpData.ajaxQueryTreeFilter
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
                        beforeClick: offOperateUpData.beforeClickVehicle,
                        onCheck: offOperateUpData.fuzzyOnCheckVehicle,
                        onExpand: offOperateUpData.zTreeOnExpand,
                        onNodeCreated: offOperateUpData.zTreeOnNodeCreated
                    }
                };
                $.fn.zTree.init($("#" + offOperateUpData.treeId), querySetting, null);
            }
        },
        fuzzyOnCheckVehicle: function(e, treeId, treeNode) {
            //获取树结构
            var zTree = $.fn.zTree.getZTreeObj(offOperateUpData.treeId);
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
            offOperateUpData.getCheckedNodes(); // 记录勾选的节点
        },
        ajaxQueryTreeFilter: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData))
            var nodesArr;
            if(offOperateUpType.val() == "vehicle"){
                nodesArr = filterQueryResult(responseData, crrentSubV);
            }else {
                nodesArr = responseData;
            }
            for (var i=0;i<nodesArr.length;i++){
                nodesArr[i].open = true;
            }
            return nodesArr;
        },
        //表格显示列设置
        settingTableColumn: function () {
            var columnData = [
                {name: "监控对象", column: 1},
                {name: "停运开始日期", column: 2},
                {name: "停运结束日期", column: 3},
                {name: "报停原因", column: 4},
                {name: "车牌颜色", column: 5},
                {name: "所属企业", column: 6},
                {name: "上报时间", column: 7},
                {name: "上传状态", column: 8},
                {name: "操作人", column: 9}
            ];
            var html = jlmrCommonFun.settingTableColumn(columnData);
            offOperateUpColumn.html(html);
        },
        //点击查询事件
        inquireClick: function (number) {
            var _time = offOperateUpTime.val();
            var times;
            if (number == 0) {
                times = jlmrCommonFun.getTheCurrentTime();
            } else if (number == -1) {
                times = jlmrCommonFun.getBeforeDay(-1,_time)
            } else if (number == -3) {
                times = jlmrCommonFun.getBeforeDay(-3,_time)
            } else if (number == -7) {
                times = jlmrCommonFun.getBeforeDay(-7,_time)
            }
            if (number != 1) {
                startTime = times.start;
                endTime = times.end;
                offOperateUpTime.val(startTime + '--' + endTime);
            } else {
                var timeInterval = _time.split('--');
                startTime = timeInterval[0];
                endTime = timeInterval[1];
            };
            offOperateUpData.getCheckedNodes();
            var ids = offOperateUpIds.split(",");
            ids = ids.filter(function (item) {
                return item;
            })
            if (!offOperateUpData.validates()) {
                return;
            };
            // 判断时间范围
            if (jlmrCommonFun.compareTime(startTime,endTime) > 31) {
                layer.msg('最多只能查询31天范围的数据');
                return;
            }
            offOperateUpData.initTableAjax();
            return false;
            var parameter = {};
            var status = offOperateUpStatus.val(),
                reason = offOperateUpSusReason.val();
            parameter.simpleQueryParam = offOperateUpQuery.val(); //模糊查询
            parameter.ids = offOperateUpIds.length ? offOperateUpIds : '';
            parameter.uploadDateStart = startTime + " 00:00:00";
            parameter.uploadDateEnd = endTime + " 23:59:59";
            if(status != -1) parameter.state = status;
            if(reason != -1) parameter.stopCauseCode = reason;
            json_ajax("POST", offOperateUpData.tableListUrl, "json", true, parameter, offOperateUpData.getDataCallback);
        },
        initTableAjax: function () {
            if(offOperateUpTableObject && !offOperateUpTableObject.dataTable) jlmrCommonFun.destroyTable(offOperateUpTableObject);
            var columns = [
                { data: null },
                { data: "monitorName" },
                { data: "startDateStr", render: function (data) {
                    return data || '--'
                } },
                { data: "endDateStr", render: function (data) {
                    return data || '--'
                } },
                { data: "stopCauseCodeStr", render: function (data) {
                    return data || '--'
                }},
                { data: "plateColorStr", render: function (data) {
                    return data || '--'
                } },
                { data: "groupName" },
                { data: "uploadTimeStr", render: function (data) {
                    return data || '--'
                }},
                { data: "uploadStateStr", render: function (data) {
                    return data || '--'
                } },
                { data: "operator", render: function (data) {
                    return data || '--'
                } },
            ];
            var ajaxDataParamFun = function (param) {
                var status = offOperateUpStatus.val(),
                    reason = offOperateUpSusReason.val();
                param.simpleQueryParam = offOperateUpQuery.val(); //模糊查询
                param.ids = offOperateUpIds.length ? offOperateUpIds : '';
                param.uploadDateStart = startTime + " 00:00:00";
                param.uploadDateEnd = endTime + " 23:59:59";
                if(status != -1) param.state = status;
                if(reason != -1) param.stopCauseCode = reason;
            };
            //表格setting
            var setting = {
                type: "POST",
                lengthMenu: [10, 20, 50],
                listUrl: offOperateUpData.tableListUrl,
                columnDefs: jlmrCommonFun.columnDef, //表格列定义
                columns: columns, //表格列
                dataTableDiv: offOperateUpData.tableId, //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                ajaxCallBack: offOperateUpData.drawTableCallbackFun,
                drawCallbackFun: function () {
                    jlmrCommonFun.initDataTableIndex(offOperateUpTableObject)
                }
            };
            offOperateUpTableObject = new TG_Tabel.createNew(setting);
            offOperateUpTableObject.init();

            offOperateUpColumn.find('.toggle-checkbox').off().on('change', function (e) {
                var column = offOperateUpTableObject.dataTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(this).parent().parent().parent().parent().addClass("open");
            });
        },
        drawTableCallbackFun: function (data) {
            offOperateUpData.tableList = [];
            offOperateUpExport.removeAttr("disabled");
            var checks = offOperateUpColumn.find(".toggle-checkbox");
            for(var i = 0, len = checks.length; i < len; i++){
                (function (index) {
                    var columnIndex = $(checks[index]).attr('data-column'), checked = $(checks[index]).prop("checked");
                    var column = offOperateUpTableObject.dataTable.column(columnIndex);
                    column.visible(checked);
                }(i))
            }
            if (data.records != null && data.records.length != 0) {
                offOperateUpData.requestFlag = false;
                offOperateUpData.tableList = data.records.length && data.records;
            }
        },
        //刷新
        refreshTable: function () {
            offOperateUpQuery.val("");
            offOperateUpTableObject.requestData();
        },
        validates: function () {
            return $("#offOperateUpForm").validate({
                rules: {
                    offOperateUpTime: {
                        required: true
                    }
                },
                messages: {
                    offOperateUpTime: {
                        required: '请选择时间'
                    }
                }
            }).form();
        },
    }
    $(function () {
        offOperateSetting.init();
        offOperateUpData.init();
        //监听组织树输入框清除事件
        $("input[name=groupSelect]").inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            var param = $("#" + id).val();
            if (id == 'offOperateUpInput') {
                offOperateUpData.searchVehicleTree(param);
            };
        });

        /**
         * 监控对象树模糊查询
         */
        var offOperateUpTreeTimer;
        offOperateUpInput.on('input propertychange', function (value) {
            if (offOperateUpTreeTimer !== undefined) {
                clearTimeout(offOperateUpTreeTimer);
            };
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                operateIsSearch = true;
            }
            ;
            offOperateUpTreeTimer = setTimeout(function () {
                if (operateIsSearch) {
                    var param = offOperateUpInput.val();
                    offOperateUpData.searchVehicleTree(param);
                }
                operateIsSearch = true;
            }, 500);
        });
        offOperateUpType.on('change', function () {
            var param = offOperateUpInput.val();
            offOperateUpData.searchVehicleTree(param);
        });

        $("#offOperateBatchBtn").off().on("click", function () {
            if(!offOperateSetting.selectedVehicle.length){
                layer.msg("请勾选需要上传的行！");
                return false;
            }
            offOperateUploadModel.uploadType = 1;
            $("#offOperateUploadModal").modal("show");
        });

        //监听上传弹窗显示
        $('#offOperateUploadModal').on('show.bs.modal', function () {
            if(!offOperateUploadModel.uploadType) return false;
            offOperateUploadModel.init();
        }).on('hidden.bs.modal', function () {
            laydatePro.destroy("#offOperateUploadModal input[name='uploadDatetime']");
            $("#offOperateTimeWrap").html("");
            offOperateUploadModel.uploadType = null;
            // offOperateSetting.selectedVehicle = [];
            offOperateSetting.currentVehicle.monitorId = '';
            offOperateSetting.currentVehicle.name = '';
        })
    })
}(window, $))
