(function (window, $) {
    //显示隐藏列
    var menu_text = "";
    var table = $("#dataTable tr th:gt(1)");
    //单选
    var subChk = $("input[name='subChk']");
    var vehsendflag = true;
    var clearflag = true;
    var selectGroupId = '';
    var selectAssignmentId = '';
    var dtext = "";
    var dn = new Array("交通部JT/T808-2013", "交通部JT/T808-2019");
    var dv = new Array("1", "11");
    var protocol = 1;
    var flag;
    var tableList;
    vehiclePeripheralPolling = {
        //初始化
        init: function () {
            var params = [];
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
            //数据表格列筛选
            menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" />" + table[i].innerHTML + "</label></li>"
            };
            $("#Ul-menu-text").html(menu_text);
            //生成定制显示列
            for (var i = 0; i < dn.length; i++) {
                dtext += "<label class='radio-inline' style='margin-left:10px;'><input name=\"deviceCheck\" value=\"" + dv[i] + "\" type=\"radio\" class=\"device\" checked/>" + dn[i] + "</label>";
            };
            $("#Ul-menu-text-v").html(dtext);
            $("input.device[value='1']").prop("checked", true);
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
                        var result = '';
                        var obj = {};
                        if (row.sendParamId != null && row.sendParamId != "") {
                            obj.paramId = row.sendParamId;
                        }
                        obj.vehicleId = row.vehicleId;
                        obj.pollingTime = row.pollingTime;
                        var jsonStr = JSON.stringify(obj)
                        if (row.pollingTime != null && row.pollingTime != "") {
                            result += "<input  type='checkbox' name='subChk'  value='" + jsonStr + "'/>";
                        } else {
                            result += '';
                        }
                        return result;
                    }
                },
                {
                    "data": null,
                    "class": "text-center", //最后一列，操作按钮
                    render: function (data, type, row, meta) {
                        return vehiclePeripheralPolling.operationSetting(row);
                        // var editUrlPath = myTable.editUrl + row.vehicleId + ".gsp"; //修改地址
                        // var result = '';
                        // var paramId = "";
                        // if (row.sendParamId != null && row.sendParamId != "") {
                        //     paramId = row.sendParamId;
                        // }
                        // var bottom_name = "设置";
                        // var vehicleId = "setting_" + row.vehicleId;
                        // if (row.pollingTime != null && row.pollingTime != "") {
                        //     bottom_name = "修改";
                        //     //修改按钮
                        //     result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>' + bottom_name + '</button>&ensp;';
                        //     //参数下发
                        //     result += '<button onclick="vehiclePeripheralPolling.sendfuelOneVechice(\'' + paramId + '\',\'' + row.vehicleId + '\',\'' + row.pollingTime + '\')" type="button" class="editBtn editBtn-info"><i class="fa fa-issue"></i>下发参数</button>&ensp;';
                        //     //解除绑定
                        //     // result += '<button type="button" onclick="myTable.deleteItem(\''+ row.vehicleId + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>解除绑定</button>&ensp;';
                        //     //清除轮询
                        //     result += '<button onclick="vehiclePeripheralPolling.clearPolling(\'' + paramId + '\',\'' + row.vehicleId + '\',\'' + row.pollingTime + '\')" type="button" class="editBtn editBtn-info"><i class="fa fa-issue"></i>清除轮询</button>&ensp;';
                        // } else {
                        //     //修改按钮
                        //     result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>' + bottom_name + '</button>&ensp;';
                        //     //禁用下发参数
                        //     result += '<button disabled type="button" class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>下发参数</button>&ensp;';
                        //     //禁用解除绑定按钮
                        //     // result += '<button disabled type="button" class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>解除绑定</button>&ensp;';
                        //     //禁用清除轮询
                        //     result += '<button disabled type="button" class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>清除轮询</button>&ensp;';
                        // }
                        //
                        // return '<span id="'+vehicleId+'">'+ result +'</span>'
                    }
                }, {
                    "data": "sendStatus",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        var id = 'status_' + row.vehicleId;
                        return '<div id="' + id + '">' + vehiclePeripheralPolling.sendStatusRender(row.remark, data) + '</div>';
                    }
                }, {
                    "data": "sendTime", //下发时间
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        var id = 'sendTime_' + row.vehicleId;
                        return '<div id="' + id + '">' + vehiclePeripheralPolling.sendTimeRender(row.remark, data) + '</div>';
                    }
                }, {
                    "data": "plate",
                    "class": "text-center"
                }, {
                    "data": "pollingNames",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return "<a onclick = 'vehiclePeripheralPolling.showLogContent(\"" + data + "\")'>查看详情</a>";
                        } else {
                            return "";
                        }
                    }
                }, {
                    "data": "pollingTime",
                    "class": "text-center"
                }
            ];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
                d.groupId = selectGroupId;
                d.assignmentId = selectAssignmentId;
                d.protocol = $("input[name='deviceCheck']:checked").val();
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/v/sensorConfig/vehiclePoll/list',
                editUrl: '/clbs/v/sensorConfig/vehiclePoll/edit_',
                deleteUrl: '/clbs/v/sensorConfig/vehiclePoll/delete_',
                deletemoreUrl: '/clbs/v/sensorConfig/vehiclePoll/deletemore',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                ajaxCallBack: function (data) {
                    tableList = data;
                }
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            //表格初始化
            myTable.init();
            var treeSetting = {
                async: {
                    url: "/clbs/m/basicinfo/enterprise/assignment/assignmentTree",
                    tyoe: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "isOrg": "1"
                    },
                    dataFilter: vehiclePeripheralPolling.ajaxDataFilter
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
                    onClick: vehiclePeripheralPolling.zTreeOnClick
                }
            };
            $.fn.zTree.init($("#treeDemo"), treeSetting, null);
            // 组织架构模糊搜索
            $("#search_condition").on("input oninput", function () {
                search_ztree('treeDemo', 'search_condition', 'assignmen');
            });
        },


        //操作设置
        operationSetting: function (row) {
            var editUrlPath = myTable.editUrl + row.vehicleId + ".gsp"; //修改地址
            var result = '';
            var paramId = "";
            if (row.sendParamId != null && row.sendParamId != "") {
                paramId = row.sendParamId;
            }
            var bottom_name = "设置";
            var vehicleId = "setting_" + row.vehicleId;
            if (row.pollingTime != null && row.pollingTime != "") {
                bottom_name = "修改";
                //修改按钮
                result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>' + bottom_name + '</button>&ensp;';
                //参数下发
                result += '<button onclick="vehiclePeripheralPolling.sendfuelOneVechice(\'' + paramId + '\',\'' + row.vehicleId + '\',\'' + row.pollingTime + '\')" type="button" class="editBtn editBtn-info"><i class="fa fa-issue"></i>下发参数</button>&ensp;';
                //解除绑定
                // result += '<button type="button" onclick="myTable.deleteItem(\''+ row.vehicleId + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>解除绑定</button>&ensp;';
                //清除轮询
                result += '<button onclick="vehiclePeripheralPolling.clearPolling(\'' + paramId + '\',\'' + row.vehicleId + '\',\'' + row.pollingTime + '\')" type="button" class="editBtn editBtn-info"><i class="fa fa-issue"></i>清除轮询</button>&ensp;';
            } else {
                //修改按钮
                result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>' + bottom_name + '</button>&ensp;';
                //禁用下发参数
                result += '<button disabled type="button" class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>下发参数</button>&ensp;';
                //禁用解除绑定按钮
                // result += '<button disabled type="button" class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>解除绑定</button>&ensp;';
                //禁用清除轮询
                result += '<button disabled type="button" class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>清除轮询</button>&ensp;';
            }

            return '<span id="' + vehicleId + '">' + result + '</span>'
        },

        //组织树预处理函数
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (responseData) {
                for (var i = 0; i < responseData.length; i++) {
                    responseData[i].open = true;
                }
            }
            return responseData;
        },
        //组织架构单击
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
        //全选
        checkAllClick: function () {
            $("input[name='subChk']").prop("checked", this.checked);
        },
        subChkClick: function () {
            $("#checkAll").prop("checked", subChk.length == subChk.filter(":checked").length ? true : false);
        },
        updataFenceData: function (msg) {
            if (msg != null) {
                var result = $.parseJSON(msg.body);
                var id = result.desc.monitorId;
                if (result != null) {
                    if (flag) {
                        vehiclePeripheralPolling.updateColumn(id);
                    } else {
                        var res = result.data.msgBody.result;
                        if (res == 0) {
                            var msgSNACK = result.data.msgBody.msgSNACK;
                            var vehicleId = result.desc.monitorId;
                            if (msgSNACK && vehicleId) {
                                json_ajax("POST", '/clbs/v/sensorConfig/vehiclePoll/getDirectiveStatus', "json", false, {
                                    'swiftNumber': msgSNACK,
                                    'vehicleId': vehicleId
                                }, function (data) {
                                    if (data.success && data.obj == 0) {
                                        setTimeout(function () {
                                            json_ajax("POST", '/clbs/v/sensorConfig/vehiclePoll/delete_' + vehicleId + '.gsp', "json", false, null, null);
                                            vehiclePeripheralPolling.updateColumn(id);
                                        }, 3000)
                                    } else {
                                        vehiclePeripheralPolling.updateColumn(id);
                                    }
                                });
                            } else {
                                vehiclePeripheralPolling.updateColumn(id);
                            }
                        } else {
                            vehiclePeripheralPolling.updateColumn(id);
                        }
                    }
                }
            }
        },
        //批量删除
        delModelClick: function () {
            //判断是否至少选择一项
            var chechedNum = $("input[name='subChk']:checked").length;
            if (chechedNum == 0) {
                layer.msg(selectItem, {
                    move: false
                });
                return
            }
            var checkedList = new Array();
            $("input[name='subChk']:checked").each(function () {
                var jsonStr = $(this).val();
                var jsonObj = $.parseJSON(jsonStr);
                checkedList.push(jsonObj.vehicleId);
            });
            myTable.deleteItems({
                'deltems': checkedList.toString()
            });
        },
        //刷新列表
        refreshTable: function () {
            selectGroupId = '';
            selectAssignmentId = '';
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.selectNode("");
            zTree.cancelSelectedNode();
            $("#simpleQueryParam").val("");
            myTable.requestData();
        },
        //批量下发
        sendModelClick: function () {
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
                if (jsonObj.pollingTime != null && jsonObj.pollingTime != '' && jsonObj.pollingTime != 'null') {
                    checkedList.push(jsonObj);
                }
            });
            // 下发方法调用
            vehiclePeripheralPolling.sendFuel(JSON.stringify(checkedList));
        },
        //批量清除
        sendClearClick: function () {
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
                if (jsonObj.pollingTime != null && jsonObj.pollingTime != '' && jsonObj.pollingTime != 'null') {
                    checkedList.push(jsonObj);
                }
            });
            // 下发方法调用
            vehiclePeripheralPolling.sendClearPolling(JSON.stringify(checkedList));
        },
        //批量下发执行
        sendFuel: function (sendParam) {
            var url = "/clbs/v/sensorConfig/vehiclePoll/sendAlarm";
            var parameter = {
                "sendParam": sendParam
            };
            json_ajax("POST", url, "json", true, parameter, vehiclePeripheralPolling.sendFuelCallback);
        },
        // 下发流量回调方法
        sendFuelCallback: function (data) {
            flag = true;
            if (vehsendflag) {
                webSocket.subscribe(headers, "/user/topic/peripherals_polling", vehiclePeripheralPolling.updataFenceData, "", null);
                vehsendflag = false;
            }
            if (data != null && data != undefined && data != "") {
                if (data.success) {
                    layer.msg(sendCommangComplete, {
                        time: 2000
                    }, function (refresh) {
                        //取消全选勾
                        $("#checkAll").prop('checked', false);
                        $("input[name=subChk]").prop("checked", false);
                        // myTable.refresh(); //执行的刷新语句
                        layer.close(refresh);
                    });
                } else {
                    layer.msg(data.msg, {
                        time: 2000
                    }, function (refresh) {
                        //取消全选勾
                        $("#checkAll").prop('checked', false);
                        $("input[name=subChk]").prop("checked", false);
                        myTable.refresh(); //执行的刷新语句
                        layer.close(refresh);
                    });
                }
            }
        },
        sendfuelOneVechice: function (paramId, vehicleId, pollingTime) { // 下发参数 （单个）
            if (pollingTime == null || pollingTime == '' || pollingTime == 'null') {
                layer.msg(peripheralPollParamNull, {
                    move: false
                });
                return;
            }
            var arr = [];
            var obj = {};
            obj.vehicleId = vehicleId;
            obj.paramId = paramId;
            arr.push(obj);
            var jsonStr = JSON.stringify(arr);
            vehiclePeripheralPolling.sendFuel(jsonStr);
        },
        showLogContent: function (content) { // 显示log详情
            $("#detailShow").modal("show");
            $("#detailContent").html(content);
        },
        clearPolling: function (paramId, vehicleId, pollingTime) { // 下发参数 （单个）
            if (pollingTime == null || pollingTime == '' || pollingTime == 'null') {
                layer.msg(peripheralPollParamNull, {
                    move: false
                });
                return;
            }
            var arr = [];
            var obj = {};
            obj.vehicleId = vehicleId;
            obj.paramId = paramId;
            arr.push(obj);
            var jsonStr = JSON.stringify(arr);
            vehiclePeripheralPolling.sendClearPolling(jsonStr);
        },
        sendClearPolling: function (sendParam) {
            var url = "/clbs/v/sensorConfig/vehiclePoll/clearPolling";
            var parameter = {
                "sendParam": sendParam
            };
            json_ajax("POST", url, "json", false, parameter, vehiclePeripheralPolling.sendClearPollingback);
        },
        sendClearPollingback: function (data) {
            flag = false;
            if (clearflag) {
                webSocket.subscribe(headers, "/user/topic/peripherals_polling", vehiclePeripheralPolling.updataFenceData, "", null);
                clearflag = false;
            }
            if (data != null && data != undefined && data != "") {
                if (data.success) {
                    layer.msg(sendCommangComplete, {
                        time: 2000
                    }, function (refresh) {
                        //取消全选勾
                        $("#checkAll").prop('checked', false);
                        $("input[name=subChk]").prop("checked", false);
                        myTable.refresh(); //执行的刷新语句
                        layer.close(refresh);
                    });
                } else {
                    layer.msg(data.msg, {
                        time: 2000
                    }, function (refresh) {
                        //取消全选勾
                        $("#checkAll").prop('checked', false);
                        $("input[name=subChk]").prop("checked", false);
                        myTable.refresh(); //执行的刷新语句
                        layer.close(refresh);
                    });
                }
            }
        },
        sendStatusRender: function (remark, data) {
            var val = '';
            if (parseInt(remark) == 0xFA00) {
                if (data == "0") {
                    val = '清空轮询已生效';
                } else if (data == "1") {
                    val = '清空轮询未生效';
                } else if (data == "2") {
                    val = "清空轮询消息有误";
                } else if (data == "3") {
                    val = "清空轮询不支持";
                } else if (data == "4") {
                    val = "清空轮询下发中";
                } else if (data == "5") {
                    val = "终端离线，清空轮询未下发";
                } else if (data == "7") {
                    val = "清空轮询终端处理中";
                } else if (data == "8") {
                    val = "清空轮询终端接收失败";
                } else {
                    val = "";
                }
            } else {
                if (data == "0") {
                    val = '参数已生效';
                } else if (data == "1") {
                    val = '参数未生效';
                } else if (data == "2") {
                    val = "参数消息有误";
                } else if (data == "3") {
                    val = "参数不支持";
                } else if (data == "4") {
                    val = "参数下发中";
                } else if (data == "5") {
                    val = "终端离线，未下发";
                } else if (data == "7") {
                    val = "终端处理中";
                } else if (data == "8") {
                    val = "终端接收失败";
                } else {
                    val = "";
                }
            }
            return val;
        },
        sendTimeRender: function (remark, data) {
            console.log(remark, data, 'remark, data');
            var val = '';
            if (data) {
                val = data;
            } else {
                val = '';
            }
            return val;
        },
        updateColumn: function (id) {
            var records = tableList.records;
            for (var i = 0; i < records.length; i++) {
                if (id == records[i].vehicleId) {
                    var url = '/clbs/v/sensorConfig/vehiclePoll/refreshSendStatus?vehicleId=' + id;
                    json_ajax("GET", url, "json", false, null, function (data) {
                        if (data.success) {
                            var remark = data.obj.remark;
                            var sendStatus = data.obj.sendStatus;
                            var sendTime = data.obj.sendTime;
                            console.log(data, 'data');
                            $("#status_" + id).text(vehiclePeripheralPolling.sendStatusRender(remark, sendStatus));
                            $("#sendTime_" + id).text(vehiclePeripheralPolling.sendTimeRender(remark, sendTime))
                            $("#setting_" + id).html(vehiclePeripheralPolling.operationSetting(data.obj));
                        }
                    });
                }
            }
        }
    };
    $(function () {
        vehiclePeripheralPolling.init();
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'search_condition') {
                search_ztree('treeDemo', id, 'assignment');
            };
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
        // 组织架构模糊搜索
        $("#search_condition").on("input oninput", function () {
            search_ztree('treeDemo', 'search_condition', 'assignment');
        });
        //全选
        $("#checkAll").bind("click", vehiclePeripheralPolling.checkAllClick);
        subChk.bind("click", vehiclePeripheralPolling.subChkClick);
        //批量删除
        $("#del_model").bind("click", vehiclePeripheralPolling.delModelClick);
        //刷新
        $("#refreshTable").on("click", vehiclePeripheralPolling.refreshTable);
        //批量下发
        $("#send_model").bind("click", vehiclePeripheralPolling.sendModelClick);

        $("#clear_polling").bind("click", vehiclePeripheralPolling.sendClearClick);

        //改变协议勾选框
        $(".device").change(function () {
            //取消全选
            $("#checkAll").prop('checked', false);
            //刷新表格
            myTable.requestData();
        });
    })
})(window, $)