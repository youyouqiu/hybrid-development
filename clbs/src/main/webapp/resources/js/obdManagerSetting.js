(function (window, $) {
    var params = [];
    //显示隐藏列
    var menu_text = "";
    var table = $("#dataTable tr th:gt(1)");
    var selectGroupId = '';
    var selectAssignmentId = '';
    //单选
    var subChk = $("input[name='subChk']");
    var flusendflag = true;
    var dtext = "";
    var dn = new Array("交通部JT/T808-2013","交通部JT/T808-2019");
    var dv = new Array("1","11");
    var protocol = 1;
    var tableList;
    obdManagerSetting = {
        init: function () {
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
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            ;
            $("#Ul-menu-text").html(menu_text);

            //生成定制显示列
            for (var i = 0; i < dn.length; i++) {
                dtext += "<label class='radio-inline' style='margin-left:10px;'><input name=\"deviceCheck\" value=\"" + dv[i] + "\" type=\"radio\" class=\"device\" />" + dn[i] + "</label>";
            }
            ;
            $("#Ul-menu-text-v").html(dtext);
            $("input[value='1']").prop("checked", true);
            var treeSetting = {
                async: {
                    url: "/clbs/m/basicinfo/enterprise/assignment/assignmentTree",
                    tyoe: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {  // 是否可选  Organization
                        "isOrg": "1"
                    },
                    dataFilter: obdManagerSetting.ajaxDataFilter
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
                    onClick: obdManagerSetting.zTreeOnClick,
                }
            };
            $.fn.zTree.init($("#treeDemo"), treeSetting, null);
            //表格列定义
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
                        if (row.id != null && row.id != "") { //
                            var obj = {};
                            obj.id = row.id;
                            obj.paramId = '';
                            if (row.paramId != null && row.paramId != "") {
                                obj.paramId = row.paramId;
                            }
                            obj.vehicleId = row.vehicleId;
                            var jsonStr = JSON.stringify(obj);
                            if (row.obdVehicleTypeId != null && row.obdVehicleTypeId != "") {
                                result += "<input  type='checkbox' name='subChk'  value='" + jsonStr + "' />";
                            }
                        }
                        return result;
                    }
                },
                {
                    "data": null,
                    "class": "text-center", //最后一列，操作按钮
                    render: function (data, type, row, meta) {
                        var editUrlPath = myTable.editUrl + row.id + '_' + row.vehicleId + ".gsp"; //修改地址
                        var bindUrlPre = '/clbs/v/obdManager/obdManagerSetting/add_{id}.gsp';//设置
                        var detailUrlPre = '/clbs/v/obdManager/obdManagerSetting/detail_{id}_{vid}.gsp';//详情
                        var basicInfoUrlPre = '/clbs/v/obdManager/obdManagerSetting/base_{id}_{vid}'; // 基本信息
                        var generalUrlPre = '/clbs/v/obdManager/obdManagerSetting/getFaultPage_{vid}'; // 读取故障码
                        var newsletterUrlPre = '/clbs/v/obdManager/obdManagerSetting/findOBDParameter_{vid}'; // 查看OBD数据

                        var result = '';
                        var paramId = "";
                        if (row.paramId != null && row.paramId != "") {
                            paramId = row.paramId;
                        }
                        if (row.obdVehicleTypeId != null && row.obdVehicleTypeId != "") { // 修改
                            result += '<button href="'
                                + editUrlPath
                                + '" data-toggle="modal" data-target="#commonWin"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                            // 删除
                            //删除按钮
                            result += '<button type="button" onclick="obdManagerSetting.delModelClick(\''
                                + row.id
                                + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>解除绑定</button>&ensp;';
                            // 下发参数
                            result += '<button onclick="obdManagerSetting.sendfuelOne(\'' + row.id + '\',\'' + paramId + '\',\'' + row.vehicleId + '\')" type="button" class="editBtn editBtn-info"><i class="fa fa-issue"></i>下发参数</button>&ensp;';
                            //其他
                            result += '<div class="btn-group"><button class="editBtn editBtn-info" type="button" id="dropdownMenuOther" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true" style="margin-top:-4px;padding-left:14px!important;">其他<span class="caret"></span></button>' +
                                '<ul class="dropdown-menu" style="width:306px;" aria-labelledby="dropdownMenuOther" id="dropdownMenuContentFluxsensor">' +
                                '<li><a href="' + detailUrlPre.replace("{id}", row.id).replace("{vid}", row.vehicleId) + '" data-toggle="modal" data-target="#commonWin">详情</a></li>' +
                                '<li><a href="' + basicInfoUrlPre.replace("{id}", row.id).replace("{vid}", row.vehicleId) + '" data-toggle="modal" data-target="#commonWin">读取基本信息</a></li>' +
                                '<li><a href="' + generalUrlPre.replace("{vid}", row.vehicleId) + '" data-toggle="modal" data-target="#commonSmWin">读取故障码</a></li>' +
                                '<li><a href="' + newsletterUrlPre.replace("{vid}", row.vehicleId) + '" data-toggle="modal" data-target="#commonSmWin">查看OBD数据</a></li>' +
                                '</ul></div>';
                        } else { // 设置
                            result += '<button href="' + bindUrlPre.replace("{id}", row.vehicleId) + '"  data-toggle="modal" data-target="#commonWin"  type="button" class="editBtn editBtn-info"><i class="fa fa-set"></i>设置</button>&ensp;';
                            // 禁用删除按钮
                            result += '<button disabled type="button" onclick="myTable.relieveItem(\''
                                + row.id
                                + '\')" class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>解除绑定</button>&ensp;';
                            // 禁用下发参数
                            result += '<button disabled type="button" class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>下发参数</button>&ensp;';
                            //其他
                            result += '<div class="btn-group">' +
                                '<button disabled class="editBtn editBtn-info disabled-btn" type="button" id="dropdownMenuOther" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true" style="margin-top:-4px;padding-left:14px!important;">其他<span class="caret"></span></button>' +
                                '</div>';
                        }
                        return result;
                    }
                },
                {
                    "data": "status",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return obdManagerSetting.renderStatus(row, false);
                    }
                },
                {
                    "data": "brand",
                    "class": "text-center",
                },
                {
                    "data": "groupName",
                    "class": "text-center"
                },
                {
                    "data": "monitorType",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == 0) {
                            return '车';
                        } else if (data == 2) {
                            return '物';
                        } else {
                            return '人';
                        }
                    }
                },
                {
                    "data": "vehicleType",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == 0) {
                            return '乘用车';
                        } else if (data == 1) {
                            return '商用车';
                        }
                        return "";
                    }
                },
                {
                    "data": "code",
                    "class": "text-center",
                }, {
                    "data": "obdVehicleName",
                    "class": "text-center"
                }, {
                    "data": "time",
                    "class": "text-center"
                }
            ];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
                d.groupId = selectGroupId;
                d.assignmentId = selectAssignmentId;
                d.protocol =$("input[name='deviceCheck']:checked").val();
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/v/obdManager/obdManagerSetting/list',
                editUrl: '/clbs/v/obdManager/obdManagerSetting/edit_',
                // deleteUrl: '/clbs/v/obdManager/obdManagerSetting/delete_',
                deletemoreUrl: '/clbs/v/obdManager/obdManagerSetting/delete',
                enableUrl: '/clbs/c/user/enable_',
                disableUrl: '/clbs/c/user/disable_',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                ajaxCallBack: function (data) {
                    tableList = data.records;
                }
            };
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();

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
        },
        updataFenceData: function (msg) {
            if (msg != null) {
                var result = $.parseJSON(msg.body);
                if (result != null) {
                    obdManagerSetting.updateColumn(result.desc.monitorId);
                }
            }
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
        //全选
        checkAllClick: function () {
            $("input[name='subChk']").prop("checked", this.checked);
        },
        subChkClick: function () {
            $("#checkAll").prop("checked", subChk.length == subChk.filter(":checked").length ? true : false);
        },
        // 下发参数 （单个）
        sendfuelOne: function (id, paramId,vehicleId) {
            var arr = [];
            var obj = {};
            obj.id = id;
            obj.paramId = paramId;
            obj.vehicleId = vehicleId;
            arr.push(obj);
            var jsonStr = JSON.stringify(arr);
            obdManagerSetting.sendFuel(jsonStr);
        },
        // 下发参数
        sendFuel: function (sendParam) {
            var url = "/clbs/v/obdManager/obdManagerSetting/sendOBDParam";
            var parameter = {"sendParam": sendParam};
            json_ajax("POST", url, "json", true, parameter, obdManagerSetting.sendFuelCallback);
        },
        // 下发流量回调方法
        sendFuelCallback: function (data) {
            if (flusendflag) {
                // webSocket.subscribe(headers, '/topic/fencestatus', obdManagerSetting.updataFenceData, null, null);
                //修復订阅后通用应答上来一直刷list接口问题
                webSocket.subscribe(headers, "/user/topic/obd", obdManagerSetting.updataFenceData, null, null);
                flusendflag = false;
            }
            if (data != null && data.success) {
                layer.msg(sendCommandComplete, {closeBtn: 0}, function (refresh) {
                    myTable.refresh(); //执行的刷新语句
                    layer.close(refresh);
                });
                return;
            }
            layer.msg(data.msg);
        },
        // 批量下发
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
                checkedList.push(jsonObj);
            });
            // 下发
            obdManagerSetting.sendFuel(JSON.stringify(checkedList));
        },
        //批量解除
        delModelClick: function (vid) {
            //判断是否至少选择一项
            var chechedNum = $("input[name='subChk']:checked").length;
            if (vid == 'all' && chechedNum == 0) {
                layer.msg(selectItem);
                return
            }
            var checkedList = new Array();
            $("input[name='subChk']:checked").each(function () {
                var jsonStr = $(this).val();
                var jsonObj = $.parseJSON(jsonStr);
                checkedList.push(jsonObj.id);
            });
            myTable.relieveItems({
                'id': vid != 'all' ? vid : checkedList.toString()
            });
        },
        // 查询全部
        queryAllClick: function () {
            selectGroupId = "";
            selectAssignmentId = "";
            $('#simpleQueryParam').val("");
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.selectNode("");
            zTree.cancelSelectedNode();
            myTable.requestData();
        },
        // 组织架构模糊搜索
        searchCondition: function () {
            search_ztree('treeDemo', 'search_condition', 'assignment');
        },

        renderStatus: function (row, flag) {
            var val = '';
            var vehicleId = 'status_' + row.vehicleId;
            var data = row.status;

            if (data == "0") {
                val =  '参数已生效';
            } else if (data == "1") {
                val =  '参数未生效';
            } else if (data == "2") {
                val =  "参数消息有误";
            } else if (data == "3") {
                val =  "参数不支持";
            } else if (data == "4") {
                val =  "参数下发中";
            } else if (data == "5") {
                val =  "终端离线，未下发";
            } else if (data == "7") {
                val =  "终端处理中";
            } else if (data == "8") {
                val =  "终端接收失败";
            } else {
                val =  "";
            }

            if(!flag) {
                return  "<div id= '"+ vehicleId+"'>" + val + "</div>"
            }else{
                return  val;
            }
        },

        updateColumn: function (id) {
            for(var i= 0; i< tableList.length; i++) {
                if(id == tableList[i].vehicleId){
                    var url = '/clbs/v/obdManager/obdManagerSetting/refreshSendStatus?vehicleId=' + id;
                    json_ajax('GET', url, 'json', false, null, function (messages) {
                        if(messages.success){
                            var data = messages.obj;
                            $("#status_" + id).text(obdManagerSetting.renderStatus(data, true));
                        }
                    });
                }
            }
        }



    }
    $(function () {
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'search_condition') {
                search_ztree('treeDemo', id, 'assignment');
            }
            ;
        });
        obdManagerSetting.init();
        //全选
        $("#checkAll").bind("click", obdManagerSetting.checkAllClick);
        subChk.bind("click", obdManagerSetting.subChkClick);
        // 批量下发
        $("#send_model").bind("click", obdManagerSetting.sendModelClick);
        //批量删除
        $("#del_model").bind("click", function () {
            obdManagerSetting.delModelClick('all');
        });
        // 查询全部
        $('#refreshTable').bind("click", obdManagerSetting.queryAllClick);
        // 组织架构模糊搜索
        $("#search_condition").on("input oninput", obdManagerSetting.searchCondition);

        //改变协议勾选框
        $(".device").change(function() {
            //取消全选
            $("#checkAll").prop('checked',false);
            //刷新表格
            myTable.requestData();
        });
    })
})(window, $)