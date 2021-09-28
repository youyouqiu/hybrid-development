var params = [];
//显示隐藏列
var menu_text = "";
var table = $("#dataTable tr th:gt(1)");
var selectGroupId = '';
var selectAssignmentId = '';
var alasendflag = true;
//单选
var subChk = $("input[name='subChk']:checkbox");
var dtext = "";
//	var dn = new Array("交通部JT/T808","移为GV320","天禾","北斗天地协议","康凯斯有线","康凯斯无线","博实结A5","艾赛欧超长待机","F3超长待机");
//	var dv = new Array("-1","2","3","5","6","7","8","9","10");
var dn = new Array("交通部JT/T808-2013", "交通部JT/T808-2019");
var dv = new Array("-1", "11");
videoSetting = {
    init: function () {
        menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>"
        for (var i = 1; i < table.length; i++) {
            menu_text += "<li><label ><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" />" + table[i].innerHTML + "</label></li>"
        }
        ;
        $("#Ul-menu-text").html(menu_text);

        videoSetting.initTable();
        videoSetting.initTree();
        //websocket 连接
        // webSocket.init('/clbs/vehicle');
        // 接收到消息
        // setTimeout(function () {
        //    webSocket.subscribe(headers, '/user/' + $("#userName").text() +'/fencestatus', videoSetting.updataFenceData,null, null);
        // },500);

        //生成定制显示列
        for (var i = 0; i < dn.length; i++) {
            dtext += "<label class='radio-inline' style='margin-left:10px;'><input name=\"deviceCheck\" value=\"" + dv[i] + "\" type=\"radio\" class=\"device\" />" + dn[i] + "</label>";
        }
        ;
        $("#Ul-menu-text-v").html(dtext);
        $("input[value='-1']").prop("checked", true);
        //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
        window.onbeforeunload = function () {
            var cancelStrS = {
                "desc": {
                    "MsgId": 40964,
                    "UserName": $("#userName").text()
                },
                "data": params
            };
            webSocket.unsubscribealarm(headers, "/app/vehicle/unsubscribestatus", cancelStrS);
        }
    },
    //table表
    initTable: function () {
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
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    var result = '';
                    var obj = {};
                    obj.alarmVehicleId = row.id;
                    if (row.paramId != null && row.paramId != "") {
                        obj.paramId = row.paramId;
                    }
                    obj.vehicleId = row.vId;
                    obj.alarmTypeId = row.alarmTypeId;
                    var jsonStr = JSON.stringify(obj)
                    result += "<input  type='checkbox' name='subChk'  value='" + jsonStr + "' onclick='videoSetting.subChkChange(this)'/>";
                    return result;
                }
            },
            {
                "data": null,
                "class": "text-center", //最后一列，操作按钮
                render: function (data, type, row, meta) {
                    var deviceType = $("input[name='deviceCheck']:checked").val();
                    var editUrlPath = myTable.editUrl + row.id + ".gsp" + row.monitorType;
                    var bindUrlPre = /*[[@{/v/oilmgt/fluxsensorbind/bind_{id}.gsp}]]*/'url';
                    var detailUrlPre = /*[[@{/v/oilmgt/fluxsensorbind/detail_{id}.gsp}]]*/'url';
                    var result = '';
                    var paramId = "";
                    if (row.paramId != null && row.paramId != "") {
                        paramId = row.paramId;
                    }
                    if (row.videoSettingVid != null || row.videoChannelVid != null || row.videoSleepVid != null || row.videoRecordingVid != null) {
                        result += '<button href="'
                            + editUrlPath
                            + '" data-toggle="modal" data-target="#commonWin"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                        //删除按钮
                        result += '<button type="button" onclick="myTable.deleteItem(\''
                            + row.id
                            + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>恢复默认</button>&ensp;';
                        // 禁用下发参数
                        result += '<button onclick="videoSetting.sendfuelOne(\'' + row.id + '\')" type="button" class="editBtn editBtn-info"><i class="fa fa-issue"></i>下发参数</button>&ensp;';

                    } else {
                        // 设置
                        result += '<button href="'
                            + editUrlPath
                            + '" data-toggle="modal" data-target="#commonWin"  type="button" class="editBtn editBtn-info"><i class="fa fa-set"></i>设置</button>&ensp;';
                        // 禁用删除按钮
                        result += '<button disabled type="button" onclick="myTable.deleteItem(\''
                            + row.id
                            + '\')" class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>恢复默认</button>&ensp;';
                        // 禁用下发参数
                        result += '<button disabled type="button" class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>下发参数</button>&ensp;';
                    }
                    // deviceType 为1代表交通部协议，联动策略按钮可用，否则不可点击
                    /*	if(row.deviceType == '1'){
                            result += '<button href="'
                                + linkagePath
                                + '" data-toggle="modal" data-target="#commonWin"  type="button" class="editBtn editBtn-info"><i class="fa fa-set"></i>联动策略</button>&ensp;';
                        }else{
                            result += '<button disabled  type="button" class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>联动策略</button>&ensp;';
                        }*/
                    return result;
                }
            },
            {
                "data": "status",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == "0") {
                        return '参数已生效';
                    } else if (data == "1") {
                        return '参数未生效';
                    } else if (data == "2") {
                        return "参数消息有误";
                    } else if (data == "3") {
                        return "参数不支持";
                    } else if (data == "4") {
                        return "参数下发中";
                    } else if (data == "5") {
                        return "终端离线，未下发";
                    } else if (data == "7") {
                        return "终端处理中";
                    } else if (data == "8") {
                        return "参数下发失败";
                    }
                    else {
                        return "";
                    }
                }
            },
            {
                "data": "brand",
                "class": "text-center",
            },
            {
                "data": "deviceType",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return getProtocolName(data);
                }
            },
            {
                "data": "groupName",
                "class": "text-center"
            },
            {
                "data": "vehicleType",
                "class": "text-center"
            }
        ];
        var ajaxDataParamFun = function (d) {
            d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
            d.groupId = selectGroupId;
            d.assignmentId = selectAssignmentId;
            var deviceType = $("input[name='deviceCheck']:checked").val();
            d.deviceType = deviceType ? deviceType : '-1';
        };
        var setting = {
            listUrl: '/clbs/realTimeVideo/videoSetting/list',
            editUrl: '/clbs/realTimeVideo/videoSetting/setting/',
            deleteUrl: '/clbs/realTimeVideo/videoSetting/repristination_',
            deletemoreUrl: '/clbs/realTimeVideo/videoSetting/deletemore',
//				enableUrl : '/clbs/c/user/enable_',
//				disableUrl : '/clbs/c/user/disable_',
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
    //组织树
    initTree: function () {
        var treeSetting = {
            async: {
//					url : "/clbs/m/functionconfig/fence/bindfence/vehicleTreeByDeviceType",
                url: "/clbs/m/basicinfo/enterprise/assignment/assignmentTree",
                type: "post",
                enable: true,
                sync: false,
                autoParam: ["id"],
                dataType: "json",
                otherParam: {  // 是否可选  Organization
                    "isOrg": "1"
                },
                dataFilter: videoSetting.ajaxDataFilter
            },
            view: {
                selectedMulti: false,
                nameIsHTML: true,
                fontCss: setFontCss_ztree
            },
            data: {
                simpleData: {
                    enable: true
                }
            },
            callback: {
                onClick: videoSetting.zTreeOnClick
            }
        };
        $.fn.zTree.init($("#treeDemo"), treeSetting, null);
    },
    ajaxDataFilter: function (treeId, parentNode, responseData) {
//			responseData = JSON.parse(ungzip(responseData));
        var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
        if (responseData) {
            for (var i = 0; i < responseData.length; i++) {
                responseData[i].open = true;
            }
        }
        return responseData;
    },
    //点击节点
    zTreeOnClick: function (event, treeId, treeNode) {
        if (treeNode.type == "assignment") {
            selectAssignmentId = treeNode.id;
            selectGroupId = '';
        } else {
            selectGroupId = treeNode.uuid;
            selectAssignmentId = '';
        }
        myTable.requestData();
    },
    updataFenceData: function (msg) {
        if (msg != null) {
            var result = $.parseJSON(msg.body);
            if (result != null) {
                myTable.refresh();
            }
        }
    },
    //获取第一个根子节点
    getFirstNodes: function (node) {
        if (node.isParent == true) {
            getFirstNodes(node.children[0]);
        } else {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.checkNode(node);
            selectVehicleId = node.id;
            var settingUrl = myTable.editUrl + selectVehicleId + ".gsp"
            $("#settingBtn").attr("href", settingUrl);
        }
        ;
    },
    //全选
    checkAllClickFn: function () {
        $("input[name='subChk']").prop("checked", this.checked);
        var checkedList = new Array();
        $("input[name='subChk']:checked").each(function () {
            var jsonStr = $(this).val();
            var jsonObj = $.parseJSON(jsonStr);
            checkedList.push(jsonObj.vehicleId);
        });
        var deviceType = $("input[name='deviceCheck']:checked").val();
        var settingUrl = settingMoreUrl.replace("{id}.gsp", checkedList.toString() + ".gsp?deviceType=" + deviceType);
        $("#settingMoreBtn").attr("href", settingUrl);
    },
    subChkChange: function (obj) {
        $("#checkAll").prop(
            "checked",
            subChk.length == subChk.filter(":checked").length ? true
                : false);
        var checkedList = new Array();
        $("input[name='subChk']:checked").each(function () {
            var jsonStr = $(this).val();
            var jsonObj = $.parseJSON(jsonStr);
            checkedList.push(jsonObj.vehicleId);
        });
        var deviceType = $("input[name='deviceCheck']:checked").val();
        var settingUrl = settingMoreUrl.replace("{id}.gsp", checkedList.toString() + ".gsp?deviceType=" + deviceType);
        $("#settingMoreBtn").attr("href", settingUrl);
    },
    // 下发参数 （单个）
    sendfuelOne: function (vehicleId) {
        videoSetting.sendFuel(vehicleId);
    },
    // 下发参数
    sendFuel: function (vehicleId) {
        var url = "/clbs/realTimeVideo/videoSetting/sendVideoSetting";
        var parameter = {"vehicleId": vehicleId};
        json_ajax("POST", url, "json", true, parameter, videoSetting.sendFuelCallback);
    },
    // 下发流量回调方法
    sendFuelCallback: function (data) {
        if (alasendflag) {
            webSocket.subscribe(headers, "/user/topic/t808_currency_response", videoSetting.updataFenceData, null, null);
            webSocket.subscribe(headers, '/user/topic/fencestatus', videoSetting.updataFenceData, null, null);
            alasendflag = false;
        }
        if (data != null && data != undefined && data != "") {
            if (data.success) {
                layer.msg(sendCommandComplete, {time: 2000}, function (refresh) {
                    //取消全选勾
                    $("#checkAll").prop('checked', false);
                    $("input[name=subChk]").prop("checked", false);
                    myTable.refresh(); //执行的刷新语句
                    layer.close(refresh);
                });
            } else {
                layer.msg(data.msg, {time: 2000}, function (refresh) {
                    //取消全选勾
                    $("#checkAll").prop('checked', false);
                    $("input[name=subChk]").prop("checked", false);
                    myTable.refresh(); //执行的刷新语句
                    layer.close(refresh);
                });
            }
        }
    },
    //批量删除
    delModelFn: function () {
        //判断是否至少选择一项
        var chechedNum = $("input[name='subChk']:checked").length;
        if (chechedNum == 0) {
            layer.msg(selectItem);
            return
        }
        var checkedList = new Array();
        $("input[name='subChk']:checked").each(function () {
            var jsonStr = $(this).val();
            var jsonObj = $.parseJSON(jsonStr);
            checkedList.push(jsonObj.alarmVehicleId);
        });
        myTable.relieveItems({
            'deltems': checkedList.toString()
        });
    },
    //查询全部
    refreshTableFn: function () {
        selectGroupId = "";
        selectAssignmentId = "";
        $('#simpleQueryParam').val("");
        var zTree = $.fn.zTree.getZTreeObj("treeDemo");
        zTree.selectNode("");
        zTree.cancelSelectedNode();
        myTable.requestData();
    },

}
$(function () {

    $('input').inputClear().on('onClearEvent', function (e, data) {
        var id = data.id;
        if (id == 'search_condition') {
            search_ztree('treeDemo', id, 'assignment');
        }
        ;
    });
    videoSetting.init();
    // 组织架构模糊搜索
    $("#search_condition").on("input oninput", function () {
        search_ztree('treeDemo', 'search_condition', 'assignment');
    });
    //改变勾选框
    $(".device").change(function () {
        //取消全选
        $("#checkAll").prop('checked', false);
        //还原批量设置的地址
        var settingUrl = settingMoreUrl.replace("{id}.gsp", "" + ".gsp");
        $("#settingMoreBtn").attr("href", settingUrl);
        //刷新表格
        myTable.dataTable.draw(true);
    });
    //IE9
    if (navigator.appName == "Microsoft Internet Explorer" && navigator.appVersion.split(";")[1].replace(/[ ]/g, "") == "MSIE9.0") {
        var search;
        $("#search_condition").bind("focus", function () {
            search = setInterval(function () {
                search_ztree('treeDemo', 'search_condition', 'assignment');
            }, 500);
        }).bind("blur", function () {
            clearInterval(search);
        });
    }
    //IE9 end
    //全选
    $("#checkAll").on("click", videoSetting.checkAllClickFn);
    //批量删除
    $("#del_model").on("click", videoSetting.delModelFn);
    //查询全部
    $('#refreshTable').on("click", videoSetting.refreshTableFn);
})
	