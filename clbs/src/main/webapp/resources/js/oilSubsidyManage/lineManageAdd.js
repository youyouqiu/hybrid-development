(function ($, window) {
    var submissionFlag = false;
    var stationTable = [{
        order: 1,
        station: '',
        longitude: '',
        latitude: '',
        remark: '',
    }] //下行
    var stationTableUp = [{
        order: 1,
        station: '',
        longitude: '',
        latitude: '',
        remark: '',
    }] //上行

    var resultStation = [];
    var assignmentAdd = {
        //初始化
        init: function () {
            var url = "/clbs/m/station/manage/getAll";
            var data = {}
            json_ajax("POST", url, "json", false, data, assignmentAdd.getSelectDataCallBack);
            var setting = {
                async: {
                    url: "/clbs/m/basicinfo/enterprise/professionals/tree",
                    tyoe: "post",
                    enable: true,
                    autoParam: ["id"],
                    contentType: "application/json",
                    dataType: "json",
                    dataFilter: assignmentAdd.ajaxDataFilter
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
                    beforeClick: assignmentAdd.beforeClick,
                    onClick: assignmentAdd.onClick,
                    onAsyncSuccess: assignmentAdd.zTreeOnAsyncSuccess
                }
            };
            $.fn.zTree.init($("#ztreeDemo"), setting, null);
        },
        zTreeOnNodeCreated: function (event, treeId, treeNode) {
            var id = treeNode.uuid.toString();
            if (id == $("#groupIdAdd").val()) {
                var treeObj = $.fn.zTree.getZTreeObj("ztreeDemo");
                treeObj.selectNode(treeNode, true, true);
            }
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            treeObj.expandAll(true); // 展开所有节点
        },
        beforeClick: function (treeId, treeNode) {
            var check = (treeNode);
            return check;
        },
        onClick: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("ztreeDemo"),
                nodes = zTree
                .getSelectedNodes(),
                v = "";
            n = "";
            nodes.sort(function compare(a, b) {
                return a.id - b.id;
            });
            for (var i = 0, l = nodes.length; i < l; i++) {
                n += nodes[i].name;
                v += nodes[i].uuid + ",";
            }
            if (v.length > 0)
                v = v.substring(0, v.length - 1);
            var cityObj = $("#zTreeCitySel");
            $("#groupIdAdd").val(v);
            cityObj.val(n);
            $("#selectGroup").val(v);
            $("#zTreeContent").hide();
        },
        showMenu: function (e) {
            if ($("#zTreeContent").is(":hidden")) {
                var width = $(e).parent().width();
                $("#zTreeContent").css("width", width + "px");
                $(window).resize(function () {
                    var width = $(e).parent().width();
                    $("#zTreeContent").css("width", width + "px");
                })
                $("#zTreeContent").show();
            } else {
                $("#zTreeContent").hide();
            }
            $("body").bind("mousedown", assignmentAdd.onBodyDown);
        },
        hideMenu: function () {
            $("#zTreeContent").fadeOut("fast");
            $("body").unbind("mousedown", assignmentAdd.onBodyDown);
        },
        onBodyDown: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "zTreeContent" || $(event.target).parents("#zTreeContent").length > 0)) {
                assignmentAdd.hideMenu();
            }
        },
        //组织树预处理函数
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            assignmentAdd.hideErrorMsg(); //清除错误提示样式
            // var isAdminStr = $("#isAdmin").attr("value"); // 是否是admin
            // var isAdmin = isAdminStr == 'true';
            // var userGroupId = $("#userGroupId").attr("value"); // 用户所属组织 id
            // var userGroupName = $("#userGroupName").attr("value"); // 用户所属组织 name
            //如果根企业下没有节点,就显示错误提示(根企业下不能创建分组)
            if (responseData != null && responseData != "" && responseData != undefined && responseData.length >= 1) {
                if ($("#groupIdAdd").val() == "") {
                    var uuid = responseData[0].uuid;
                    var name = responseData[0].name;
                    for (var i = 0, len = responseData.length; i < len; i++) {
                        if (responseData[i].id == responseData[0].pid) {
                            uuid = responseData[i].uuid;
                            name = responseData[i].name;
                            break;
                        }
                    }
                    $("#groupIdAdd").val(uuid);
                    $("#zTreeCitySel").val(name);
                }
                return responseData;
            } else {
                assignmentAdd.showErrorMsg("您需要先新增一个组织", "zTreeCitySel");
            }

        },
        doSubmit: function () {
            if (submissionFlag) { // 防止重复提交
                return;
            } else {
                if (assignmentAdd.validates()) {
                    submissionFlag = false;
                    var stationUp = []
                    var stationDown = []
                    for (var i = 0; i < stationTable.length; i++) {
                        let a = $(`#station-line-manage-table .station-body:nth-child(${i+2})>.station-line select[name='station-select']`).children('option:selected').val()
                        stationUp.push(a)
                    }
                    for (var i = 0; i < stationTableUp.length; i++) {
                        let b = $(`#station-line-manage-table-up .station-body:nth-child(${i+2})>.station-line select[name='station-select-up']`).children('option:selected').val()
                        stationDown.push(b)
                    }
                    // stationTable.forEach((it) => {
                    //     stationUp.push(it.station)
                    // })
                    // stationTableUp.forEach(it => {
                    //     stationDown.push(it.station)
                    // })
                    if (stationUp.length < 2) {
                        layer.msg('下行站点信息最少选择两个');
                        return
                    }
                    if (stationDown.length < 2) {
                        layer.msg('上行站点信息最少选择两个');
                        return
                    }
                    for (var i = 0; i < stationUp.length; i++) {
                        if (!stationUp[i]) {
                            layer.msg('下行站点信息有未选择的站点');
                            return
                        }
                    }
                    for (var i = 0; i < stationDown.length; i++) {
                        if (!stationUp[i]) {
                            layer.msg('上行站点信息有未选择的站点');
                            return
                        }
                    }
                    var directionData = [{}, {}]
                    directionData[1]['directionType'] = 1
                    directionData[1]['mile'] = $("input[name='mile']").val()
                    directionData[1]['distance'] = $("input[name='distance']").val()
                    directionData[1]['firstStationId'] = $("select[name='firstStationId']").val()
                    directionData[1]['lastStationId'] = $("select[name='lastStationId']").val()

                    directionData[1]['summerFirstTrainTime'] = $("input[name='summerFirstTrainTime']").val()
                    directionData[1]['summerLastTrainTime'] = $("input[name='summerLastTrainTime']").val()
                    directionData[1]['winterFirstTrainTime'] = $("input[name='winterFirstTrainTime']").val()
                    directionData[1]['winterLastTrainTime'] = $("input[name='winterLastTrainTime']").val()
                    directionData[1]['morningPeakStartTime'] = $("input[name='morningPeakStartTime']").val()
                    directionData[1]['morningPeakEndTime'] = $("input[name='morningPeakEndTime']").val()
                    directionData[1]['eveningPeakStartTime'] = $("input[name='eveningPeakStartTime']").val()
                    directionData[1]['eveningPeakEndTime'] = $("input[name='eveningPeakEndTime']").val()
                    directionData[1]['peakDepartureInterval'] = Number($("input[name='peakDepartureInterval']").val())
                    directionData[1]['offPeakDepartureInterval'] = Number($("input[name='offPeakDepartureInterval']").val())
                    directionData[1]['stationIds'] = stationUp


                    directionData[0]['directionType'] = 0
                    directionData[0]['mile'] = $("input[name='mile2']").val()
                    directionData[0]['distance'] = $("input[name='distance2']").val()
                    directionData[0]['firstStationId'] = $("select[name='firstStationId2']").val()
                    directionData[0]['lastStationId'] = $("select[name='lastStationId2']").val()
                    directionData[0]['summerFirstTrainTime'] = $("input[name='summerFirstTrainTime2']").val()
                    directionData[0]['summerLastTrainTime'] = $("input[name='summerLastTrainTime2']").val()
                    directionData[0]['winterFirstTrainTime'] = $("input[name='winterFirstTrainTime2']").val()
                    directionData[0]['winterLastTrainTime'] = $("input[name='winterLastTrainTime2']").val()
                    directionData[0]['morningPeakStartTime'] = $("input[name='morningPeakStartTime2']").val()
                    directionData[0]['morningPeakEndTime'] = $("input[name='morningPeakEndTime2']").val()
                    directionData[0]['eveningPeakStartTime'] = $("input[name='eveningPeakStartTime2']").val()
                    directionData[0]['eveningPeakEndTime'] = $("input[name='eveningPeakEndTime2']").val()
                    directionData[0]['peakDepartureInterval'] = Number($("input[name='peakDepartureInterval2']").val())
                    directionData[0]['offPeakDepartureInterval'] = Number($("input[name='offPeakDepartureInterval2']").val())
                    directionData[0]['stationIds'] = stationDown
                    $('#direction').val(JSON.stringify(directionData))
                    var url = "/clbs/m/line/manage/add";

                    if (directionData.length != 2) {
                        layer.msg('请补充完上行下行数据');
                        return
                    }
                    var data = {
                        direction: directionData,
                        dockingCodeOrgId: $("input[name='dockingCodeOrgId']").val(),
                        lineType: $("#lineType").val(),
                        identify: $("input[name='identify']").val(),
                        name: $("input[name='name']").val(),
                        remark: $("#remarkAdd").val(),
                        avoidRepeatSubmitToken: $("input[name='avoidRepeatSubmitToken']").val(),
                    }
                    json_ajax("POST", url, "json", false, JSON.stringify(data), function (data) {
                        if (data.success) {
                            $("#commonWin").modal("hide");
                            /* 关闭弹窗 */
                            myTable.requestData();
                        } else {
                            layer.msg(data.msg);
                            submissionFlag = false;
                        }
                    }, 30000, 'application/json');
                }
            }
        },
        validates: function () {
            return $("#addForm").validate({
                rules: {
                    dockingCodeOrgName: {
                        required: true,
                    },
                    identify: {
                        required: true,
                        isABCNumber: true,
                    },
                    name: {
                        required: true,
                        maxlength: 20
                    },
                    remark: {
                        required: false,
                        maxlength: 100
                    },
                    lineType: {
                        required: true,
                    },
                    mile: {
                        required: true,
                        number: true,
                        decimalTwo: true,
                        range: [0, 100.99]
                    },
                    distance: {
                        required: true,
                        number: true,
                        decimalTwo: true,
                        range: [0, 20000.99]
                    },
                    firstStationId: {
                        required: true,
                    },
                    lastStationId: {
                        required: true,
                    },
                    summerFirstTrainTime: {
                        required: true
                    },
                    summerLastTrainTime: {
                        required: true
                    },
                    winterFirstTrainTime: {
                        required: true
                    },
                    winterLastTrainTime: {
                        required: true
                    },
                    morningPeakStartTime: {
                        required: true
                    },
                    morningPeakEndTime: {
                        required: true
                    },
                    eveningPeakStartTime: {
                        required: true
                    },
                    eveningPeakEndTime: {
                        required: true
                    },
                    peakDepartureInterval: {
                        required: true,
                        number: true
                    },
                    offPeakDepartureInterval: {
                        required: true,
                        number: true
                    },
                    mile2: {
                        required: true,
                        number: true,
                        decimalTwo: true,
                        range: [0, 100.99]
                    },
                    distance2: {
                        required: true,
                        number: true,
                        decimalTwo: true,
                        range: [0, 20000.99]
                    },
                    firstStationId2: {
                        required: true,
                    },
                    lastStationId2: {
                        required: true,
                    },
                    summerFirstTrainTime2: {
                        required: true
                    },
                    summerLastTrainTime2: {
                        required: true
                    },
                    winterFirstTrainTime2: {
                        required: true
                    },
                    winterLastTrainTime2: {
                        required: true
                    },
                    morningPeakStartTime2: {
                        required: true
                    },
                    morningPeakEndTime2: {
                        required: true
                    },
                    eveningPeakStartTime2: {
                        required: true
                    },
                    eveningPeakEndTime2: {
                        required: true
                    },
                    peakDepartureInterval2: {
                        required: true,
                        number: true
                    },
                    offPeakDepartureInterval2: {
                        required: true,
                        number: true
                    },
                },
                messages: {
                    dockingCodeOrgName: {
                        required: '请选择对接码组织',
                    },
                    identify: {
                        required: '请输入线路标识',
                        isABCNumber: '只能输入数字和字母'
                    },
                    name: {
                        required: '请输入线路名称',
                    },
                    lineType: {
                        required: '请选择线路类别',
                    },
                    mile: {
                        required: '请输入里程',
                        number: '只能输入数字',
                        range: '只能输入0-100',
                    },
                    distance: {
                        required: '请输入站距',
                        number: '只能输入数字',
                        range: '只能输入0-20000',
                    },
                    firstStationId: {
                        required: '请选择下行起点站名',
                    },
                    lastStationId: {
                        required: '请选择下行终点站名',
                    },
                    summerFirstTrainTime: {
                        required: '请选择夏季首班发车时间',
                    },
                    summerLastTrainTime: {
                        required: '请选择夏季末班发车时间',
                    },
                    winterFirstTrainTime: {
                        required: '请选择冬季首班发车时间',
                    },
                    winterLastTrainTime: {
                        required: '请选择冬季末班发车时间',
                    },
                    morningPeakStartTime: {
                        required: '请选择早高峰起始时间',
                    },
                    morningPeakEndTime: {
                        required: '请选择早高峰结束时间',
                    },
                    eveningPeakStartTime: {
                        required: '请选择晚高峰起始时间',
                    },
                    eveningPeakEndTime: {
                        required: '请选择晚高峰结束时间',
                    },
                    peakDepartureInterval: {
                        required: '请输入高峰发车间隔',
                        number: '只能输入数字'
                    },
                    offPeakDepartureInterval: {
                        required: '请输入非高峰发车间隔',
                        number: '只能输入数字'
                    },
                    mile2: {
                        required: '请输入里程',
                        number: '只能输入数字',
                        range: '只能输入0-100',

                    },
                    distance2: {
                        required: '请输入站距',
                        number: '只能输入数字',
                        range: '只能输入0-20000',
                    },
                    firstStationId2: {
                        required: '请选择下行起点站名',
                    },
                    lastStationId2: {
                        required: '请选择下行终点站名',
                    },
                    summerFirstTrainTime2: {
                        required: '请选择夏季首班发车时间',
                    },
                    summerLastTrainTime2: {
                        required: '请选择夏季末班发车时间',
                    },
                    winterFirstTrainTime2: {
                        required: '请选择冬季首班发车时间',
                    },
                    winterLastTrainTime2: {
                        required: '请选择冬季末班发车时间',
                    },
                    morningPeakStartTime2: {
                        required: '请选择早高峰起始时间',
                    },
                    morningPeakEndTime2: {
                        required: '请选择早高峰结束时间',
                    },
                    eveningPeakStartTime2: {
                        required: '请选择晚高峰起始时间',
                    },
                    eveningPeakEndTime2: {
                        required: '请选择晚高峰结束时间',
                    },
                    peakDepartureInterval2: {
                        required: '请输入高峰发车间隔',
                        number: '只能输入数字'
                    },
                    offPeakDepartureInterval2: {
                        required: '请输入非高峰发车间隔',
                        number: '只能输入数字'
                    },
                }
            }).form();
        },
        showErrorMsg: function (msg, inputId) {
            if ($("#error_label_add").is(":hidden")) {
                $("#error_label_add").text(msg);
                $("#error_label_add").insertAfter($("#" + inputId));
                $("#error_label_add").show();
            } else {
                $("#error_label_add").is(":hidden");
            }
        },
        //错误提示信息隐藏
        hideErrorMsg: function () {
            $("#error_label_add").is(":hidden");
            $("#error_label_add").hide();
        },
        //点击显示隐藏信息
        hiddenparameterFn: function () {
            var clickId = $(this).attr('id');
            if (!($("#" + clickId + "-content").is(":hidden"))) {
                $("#" + clickId + "-content").slideUp();
                $("#" + clickId).children("font").text("显示更多");
                $("#" + clickId).children("span").removeAttr("class").addClass("fa fa-chevron-down");
            } else {
                $("#" + clickId + "-content").slideDown();
                $("#" + clickId).children("font").text("隐藏信息");
                $("#" + clickId).children("span").removeAttr("class").addClass("fa fa-chevron-up");
            }
        },
        //下行表格新增
        addtable: function () {
            let lastNum = $(`#station-line-manage-table .station-body:last-child>.orderNumber`).text()
            if (lastNum == 200) {
                layer.msg('最多新增200个站点');
                return
            }
            let lineNum = $(this)[0].id.replace(/[^0-9]/ig, "")
            let data = ''
            stationTable.push({
                order: stationTable.length + 1,
                station: '',
                longitude: '',
                latitude: '',
                remark: '',
            })
            let str = "<option style='display: none'></option>";
            for (var i = 0; i < resultStation.length; i++) {
                str += '<option  value="' + resultStation[i].id + '">' + resultStation[i].name + '(' + resultStation[i].number + ')' + '</option>'
            }
            data = `<div class="station-body" id=${"stationBodyId"+stationTable[stationTable.length-1].order}>
                <div class="station-line">
                <span class="add-station-line"  id=${'add-station-manage-line'+stationTable[stationTable.length-1].order}>添加行</span>
                <span class="delete-station-line" id=${"delete-station-manage-line"+stationTable[stationTable.length-1].order}>删除</span>
                </div>
                <div class="station-line orderNumber">${stationTable[stationTable.length-1].order}</div>
                <div class="station-line">
                   <select name="station-select"   id=${'stationSelect'+stationTable[stationTable.length-1].order} placeholder="请选择站点信息"
                    class="form-control">
                    ${str}
                    </select>
                </div>
                <div class="station-line"  id=${'jd'+stationTable[stationTable.length-1].order}></div>
                <div class="station-line" id=${'wd'+stationTable[stationTable.length-1].order}></div>
                <div class="station-line" id=${'ms'+stationTable[stationTable.length-1].order}></div>
            </div>
            `
            $('#stationBodyId' + lineNum).after(data)
            // assignmentAdd.addSelectData()
            $("select[name='station-select']").change(function () {
                var lineNum = $(this)[0].id.replace(/[^0-9]/ig, "")
                var selected = $(this).children('option:selected').val()
                for (let i = 0; i < resultStation.length; i++) {
                    if (selected == resultStation[i].id) {
                        $('#stationSelect' + lineNum).value = selected
                        $('#jd' + lineNum).text(resultStation[i].longitude)
                        $('#wd' + lineNum).text(resultStation[i].latitude)
                        if (resultStation[i].describe !== null) {
                            $('#ms' + lineNum).text(resultStation[i].describe)
                        } else {
                            $('#ms' + lineNum).text('')
                        }
                        stationTable[lineNum - 1]['station'] = selected
                        stationTable[lineNum - 1]['longitude'] = resultStation[i].longitude
                        stationTable[lineNum - 1]['latitude'] = resultStation[i].latitude
                        stationTable[lineNum - 1]['remark'] = resultStation[i].describe
                    }
                }
            })
            for (let i = 0; i < stationTable.length; i++) {
                $(`#station-line-manage-table .station-body:nth-child(${i+2})>.orderNumber`).text(i + 1)
            }
            $('#add-station-manage-line' + stationTable[stationTable.length - 1].order).on('click', assignmentAdd.addtable)
            $('#delete-station-manage-line' + stationTable[stationTable.length - 1].order).on('click', assignmentAdd.deleteLine)

        },
        //下项表格删除一行
        deleteLine: function () {
            let lineNum = $(this)[0].id.replace(/[^0-9]/ig, "")
            $("#stationBodyId" + lineNum).remove()
            stationTable = stationTable.filter(it => it.order != lineNum)
            // console.log(stationTable, 'stationTablestationTable')
            for (let i = 0; i < stationTable.length; i++) {
                $(`#station-line-manage-table .station-body:nth-child(${i+2})>.orderNumber`).text(i + 1)
            }
        },
        //下行初始化表格:
        renderTable: function () {
            let data = ''
            for (let i = 0; i < stationTable.length; i++) {
                data = `<div class="station-body" id=${"stationBodyId"+stationTable[i].order}>
                    <div class="station-line">
                    <span class="add-station-line"  id='add-station-manage-line1'>添加行</span>
                    </div>
                    <div class="station-line orderNumber">${stationTable[i].order}</div>
                    <div class="station-line">
                       <select name="station-select"   id=${'stationSelect'+stationTable[i].order} placeholder="请选择站点信息"
                        class="form-control">
                        </select>
                    </div>
                    <div class="station-line"  id=${'jd'+stationTable[i].order}>${stationTable[i].longitude}</div>
                    <div class="station-line" id=${'wd'+stationTable[i].order}>${stationTable[i].latitude}</div>
                    <div class="station-line" id=${'ms'+stationTable[i].order}>${stationTable[i].remark}</div>
                </div>
                `
            }
            $('#station-line-manage-table').append(data)
            $("select[name='station-select']").change(function () {
                var lineNum = $(this)[0].id.replace(/[^0-9]/ig, "")
                var selected = $(this).children('option:selected').val()
                for (let i = 0; i < resultStation.length; i++) {
                    if (selected == resultStation[i].id) {
                        $('#jd' + lineNum).text(resultStation[i].longitude)
                        $('#wd' + lineNum).text(resultStation[i].latitude)
                        $('#wd' + lineNum).text(resultStation[i].latitude)
                        if (resultStation[i].describe !== null) {
                            $('#ms' + lineNum).text(resultStation[i].describe)
                        } else {
                            $('#ms' + lineNum).text('')
                        }
                        stationTable[lineNum - 1]['station'] = selected
                        stationTable[lineNum - 1]['longitude'] = resultStation[i].longitude
                        stationTable[lineNum - 1]['latitude'] = resultStation[i].latitude
                        stationTable[lineNum - 1]['remark'] = resultStation[i].describe
                    }
                }
            })
            $('#add-station-manage-line1').on('click', assignmentAdd.addtable)
        },
        //下拉选项渲染
        addSelectData: function () {
            var str = "<option style='display: none'></option>";
            for (var i = 0; i < resultStation.length; i++) {
                str += '<option  value="' + resultStation[i].id + '">' + resultStation[i].name + '(' + resultStation[i].number + ')' + '</option>'
            }
            $("select[name='station-select']").html(str);
        },
        getSelectDataCallBack: function (data) {
            let dataList = data.obj
            resultStation = dataList
            var str = "<option style='display: none'></option>";
            for (var i = 0; i < resultStation.length; i++) {
                str += '<option  value="' + resultStation[i].id + '">' + resultStation[i].name + '(' + resultStation[i].number + ')' + '</option>'
            }
            $("select[name='firstStationId']").html(str);
            $("select[name='lastStationId']").html(str);
            $("select[name='firstStationId2']").html(str);
            $("select[name='lastStationId2']").html(str);
        },
        //上行下拉选项渲染
        addUpSelectData: function () {
            var str = "<option style='display: none'></option>";
            for (var i = 0; i < resultStation.length; i++) {
                str += '<option  value="' + resultStation[i].id + '">' + resultStation[i].name + '(' + resultStation[i].number + ')' + '</option>'
            }
            $("select[name='station-select-up']").html(str);
        },
        ///////////////////////////////上行内容///////////////////////////////
        //上行初始化表格:
        renderUpTable: function () {
            let data = ''
            for (let i = 0; i < stationTableUp.length; i++) {
                data = `<div class="station-body" id=${"stationBodyIdUp"+stationTableUp[i].order}>
            <div class="station-line">
            <span class="add-station-line"  id='add-up-station-manage-line1'>添加行</span>
            </div>
            <div class="station-line orderNumber">${stationTableUp[i].order}</div>
            <div class="station-line">
               <select name="station-select-up"   id=${'stationSelectUp'+stationTableUp[i].order} placeholder="请选择站点信息"
                class="form-control">
                </select>
            </div>
            <div class="station-line"  id=${'jdup'+stationTableUp[i].order}>${stationTableUp[i].longitude}</div>
            <div class="station-line" id=${'wdup'+stationTableUp[i].order}>${stationTableUp[i].latitude}</div>
            <div class="station-line" id=${'msup'+stationTableUp[i].order}>${stationTableUp[i].remark}</div>
        </div>
        `
            }
            $('#station-line-manage-table-up').append(data)
            $("select[name='station-select-up']").change(function () {
                var lineNum = $(this)[0].id.replace(/[^0-9]/ig, "")
                var selected = $(this).children('option:selected').val()
                for (let i = 0; i < resultStation.length; i++) {
                    if (selected == resultStation[i].id) {
                        $('#stationSelectUp' + lineNum).value = selected
                        $('#jdup' + lineNum).text(resultStation[i].longitude)
                        $('#wdup' + lineNum).text(resultStation[i].latitude)
                        if (resultStation[i].describe !== null) {
                            $('#msup' + lineNum).text(resultStation[i].describe)
                        } else {
                            $('#msup' + lineNum).text('')
                        }
                        stationTable[lineNum - 1]['station'] = selected
                        stationTable[lineNum - 1]['longitude'] = resultStation[i].longitude
                        stationTable[lineNum - 1]['latitude'] = resultStation[i].latitude
                        stationTableUp[lineNum - 1]['remark'] = resultStation[i].describe
                    }
                }
            })
            $('#add-up-station-manage-line1').on('click', assignmentAdd.addUptable)
        },
        //上行表格新增
        addUptable: function () {
            let lastNum = $(`#station-line-manage-table-up .station-body:last-child>.orderNumber`).text()
            if (lastNum == 200) {
                layer.msg('最多新增200个站点');
                return
            }
            let lineNum = $(this)[0].id.replace(/[^0-9]/ig, "")
            let data = ''
            stationTableUp.push({
                order: stationTableUp.length + 1,
                station: '',
                longitude: '',
                latitude: '',
                remark: '',
            })
            // console.log(stationTableUp, 'stationTableUp')
            let str = "<option style='display: none'></option>";
            for (var i = 0; i < resultStation.length; i++) {
                str += '<option  value="' + resultStation[i].id + '">' + resultStation[i].name + '(' + resultStation[i].number + ')' + '</option>'
            }
            data = `<div class="station-body" id=${"stationBodyIdUp"+stationTableUp[stationTableUp.length-1].order}>
                <div class="station-line">
                <span class="add-station-line"  id=${'add-station-manage-line-up'+stationTableUp[stationTableUp.length-1].order}>添加行</span>
                <span class="delete-station-line" id=${"delete-station-manage-line-up"+stationTableUp[stationTableUp.length-1].order}>删除</span>
                </div>
                <div class="station-line orderNumber">${stationTableUp[stationTableUp.length-1].order}</div>
                <div class="station-line">
                   <select name="station-select-up"   id=${'stationSelectUp'+stationTableUp[stationTableUp.length-1].order} placeholder="请选择站点信息"
                    class="form-control">
                    ${str}
                    </select>
                </div>
                <div class="station-line"  id=${'jdup'+stationTableUp[stationTableUp.length-1].order}></div>
                <div class="station-line" id=${'wdup'+stationTableUp[stationTableUp.length-1].order}></div>
                <div class="station-line" id=${'msup'+stationTableUp[stationTableUp.length-1].order}></div>
            </div>
            `
            $('#stationBodyIdUp' + lineNum).after(data)
            $("select[name='station-select-up']").change(function () {
                var lineNum = $(this)[0].id.replace(/[^0-9]/ig, "")
                var selected = $(this).children('option:selected').val()
                for (let i = 0; i < resultStation.length; i++) {
                    if (selected == resultStation[i].id) {
                        $('#jdup' + lineNum).text(resultStation[i].longitude)
                        $('#wdup' + lineNum).text(resultStation[i].latitude)
                        if (resultStation[i].describe !== null) {
                            $('#msup' + lineNum).text(resultStation[i].describe)
                        } else {
                            $('#msup' + lineNum).text('')
                        }
                        stationTableUp[lineNum - 1]['station'] = selected
                        stationTableUp[lineNum - 1]['longitude'] = resultStation[i].longitude
                        stationTableUp[lineNum - 1]['latitude'] = resultStation[i].latitude
                        stationTableUp[lineNum - 1]['remark'] = resultStation[i].describe
                    }
                }
            })
            for (let i = 0; i < stationTableUp.length; i++) {
                $(`#station-line-manage-table-up .station-body:nth-child(${i+2})>.orderNumber`).text(i + 1)
            }
            $('#add-station-manage-line-up' + stationTableUp[stationTableUp.length - 1].order).on('click', assignmentAdd.addUptable)
            $('#delete-station-manage-line-up' + stationTableUp[stationTableUp.length - 1].order).on('click', assignmentAdd.deleteUpLine)

        },
        //上行 表格删除一行
        deleteUpLine: function () {
            let lineNum = $(this)[0].id.replace(/[^0-9]/ig, "")
            $("#stationBodyIdUp" + lineNum).remove()
            stationTableUp = stationTableUp.filter(it => it.order != lineNum)
            for (let i = 0; i < stationTableUp.length; i++) {
                $(`#station-line-manage-table-up .station-body:nth-child(${i+2})>.orderNumber`).text(i + 1)
            }
        },
        //上行点击显示隐藏信息
        hiddenparameterFnUp: function () {
            var clickId = $(this).attr('id');
            if (!($("#" + clickId + "-content").is(":hidden"))) {
                $("#" + clickId + "-content").slideUp();
                $("#" + clickId).children("font").text("显示更多");
                $("#" + clickId).children("span").removeAttr("class").addClass("fa fa-chevron-down");
            } else {
                $("#" + clickId + "-content").slideDown();
                $("#" + clickId).children("font").text("隐藏信息");
                $("#" + clickId).children("span").removeAttr("class").addClass("fa fa-chevron-up");
            }
        },
        ///////////////////////////////上行内容结束///////////////////////////////
    }
    $(function () {
        assignmentAdd.init();
        assignmentAdd.renderTable(); //加载下行表格
        assignmentAdd.renderUpTable() //加载上行表格
        assignmentAdd.addSelectData() //下拉选项渲染
        assignmentAdd.addUpSelectData() //上行下拉选项渲染
        //下行时间弹框
        laydate.render({
            elem: '#summerFirstTrainTime',
            theme: '#6dcff6',
            type: 'time',
            format: 'HH:mm'
        });
        laydate.render({
            elem: '#summerLastTrainTime',
            theme: '#6dcff6',
            type: 'time',
            format: 'HH:mm'
        });
        laydate.render({
            elem: '#winterFirstTrainTime',
            theme: '#6dcff6',
            type: 'time',
            format: 'HH:mm'
        });
        laydate.render({
            elem: '#winterLastTrainTime',
            theme: '#6dcff6',
            type: 'time',
            format: 'HH:mm'
        });
        laydate.render({
            elem: '#morningPeakStartTime',
            theme: '#6dcff6',
            type: 'time',
            format: 'HH:mm'
        });
        laydate.render({
            elem: '#morningPeakEndTime',
            theme: '#6dcff6',
            type: 'time',
            format: 'HH:mm'
        });
        laydate.render({
            elem: '#morningPeakStartTime',
            theme: '#6dcff6',
            type: 'time',
            format: 'HH:mm'
        });
        laydate.render({
            elem: '#eveningPeakStartTime',
            theme: '#6dcff6',
            type: 'time',
            format: 'HH:mm'
        });
        laydate.render({
            elem: '#eveningPeakEndTime',
            theme: '#6dcff6',
            type: 'time',
            format: 'HH:mm'
        });
        //上行时间弹框
        laydate.render({
            elem: '#summerFirstTrainTime2',
            theme: '#6dcff6',
            type: 'time',
            format: 'HH:mm'
        });
        laydate.render({
            elem: '#summerLastTrainTime2',
            theme: '#6dcff6',
            type: 'time',
            format: 'HH:mm'
        });
        laydate.render({
            elem: '#winterFirstTrainTime2',
            theme: '#6dcff6',
            type: 'time',
            format: 'HH:mm'
        });
        laydate.render({
            elem: '#winterLastTrainTime2',
            theme: '#6dcff6',
            type: 'time',
            format: 'HH:mm'
        });
        laydate.render({
            elem: '#morningPeakStartTime2',
            theme: '#6dcff6',
            type: 'time',
            format: 'HH:mm'
        });
        laydate.render({
            elem: '#morningPeakEndTime2',
            theme: '#6dcff6',
            type: 'time',
            format: 'HH:mm'
        });
        laydate.render({
            elem: '#morningPeakStartTime2',
            theme: '#6dcff6',
            type: 'time',
            format: 'HH:mm'
        });
        laydate.render({
            elem: '#eveningPeakStartTime2',
            theme: '#6dcff6',
            type: 'time',
            format: 'HH:mm'
        });
        laydate.render({
            elem: '#eveningPeakEndTime2',
            theme: '#6dcff6',
            type: 'time',
            format: 'HH:mm'
        });
        $('input').inputClear();
        $("#doSubmitAdd").on("click", assignmentAdd.doSubmit);
        // $("#zTreeCitySel").on("click", function () {
        //     assignmentAdd.showMenu(this)
        // });
        // $("#zTreeCitySel").on('input propertychange', function (value) {
        //     var treeObj = $.fn.zTree.getZTreeObj("ztreeDemo");
        //     treeObj.checkAllNodes(false);
        //     search_ztree('ztreeDemo', 'zTreeCitySel', 'group');
        // });
        $("#zTreeCitySel").on("click", function () {
            assignmentAdd.showMenu(this)
        });
        // 组织树input框的模糊搜索
        $("#zTreeCitySel").on('input propertychange', function (value) {
            var treeObj = $.fn.zTree.getZTreeObj("ztreeDemo");
            treeObj.checkAllNodes(false);
            search_ztree('ztreeDemo', 'zTreeCitySel', 'group');
        });
        //显示隐藏
        $(".info-span").on("click", assignmentAdd.hiddenparameterFn);
        $(".info-span-up").on("click", assignmentAdd.hiddenparameterFnUp);

        // $('input').inputClear().on('onClearEvent', function (e, data) {
        //     var id = data.id;
        //     var treeObj
        //     if (id == 'zTreeCitySel') {
        //         search_ztree('ztreeDemo', 'zTreeCitySel', 'group');
        //         treeObj = $.fn.zTree.getZTreeObj("ztreeDemo");
        //     }
        //     treeObj.checkAllNodes(false)
        // });
    });
})($, window)