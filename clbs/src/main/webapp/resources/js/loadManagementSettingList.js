(function (window, $) {
    var params = [];
    //显示隐藏列
    var menu_text = "";
    var table = $("#dataTable tr th:gt(1)");
    //最后一次获取信息想MSGID
    var temp_send_vehicle_msg_id = "";
    var headers = {"UserName": ""};
    var selectGroupId = '';
    var selectAssignmentId = '';
    //单选
    var subChk = $("input[name='subChk']");
    //File
    var filePath, fileName;
    var oilsendflag = true;
    var dtext = "";
    var dn = new Array("交通部JT/T808-2013","交通部JT/T808-2019");
    var dv = new Array("1","11");
    var protocol = 1;
    var oilType;
    var tableList;
    mainObj = {
        init: function () {
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            ;
            $("#Ul-menu-text").html(menu_text);
            //生成定制显示列
            for(var i = 0; i < dn.length; i++){
                dtext +="<label class='radio-inline' style='margin-left:10px;'><input name=\"deviceCheck\" value=\"" + dv[i] +"\" type=\"radio\" class=\"device\" />"+ dn[i] +"</label>";
            };
            $("#Ul-menu-text-v").html(dtext);
            $("input[value='1']").prop("checked",true);
            // webSocket.init('/clbs/vehicle');
            // 请求后台，获取所有订阅的车
            // 接收到消息
            // setTimeout(function () {
            //     webSocket.subscribe(headers, '/topic/fencestatus', mainObj.updateFenceData, null, null);
            // },500);
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
                    dataFilter: mainObj.ajaxDataFilter
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
                    onClick: mainObj.zTreeOnClick,
                    onCheck: mainObj.zTreeOnCheck
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
                            obj.sensorVehicleId = row.id;
                            obj.paramId = row.paramId;
                            obj.vehicleId = row.vehicleId;
                            var jsonStr = JSON.stringify(obj)
                            result += "<input  type='checkbox' name='subChk'  value='" + jsonStr + "' />";
                        }
                        return result;
                    }
                },
                {
                    "data": null,
                    "class": "text-center", //最后一列，操作按钮
                    render: function (data, type, row, meta) {
                        return mainObj.renderButton(row);
                    }
                },
                {
                    "data": "status",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return mainObj.renderStatus(row, false);
                    }
                },
                {
                    "data": "plateNumber",
                    "class": "text-center",
                },
                {
                    "data": "groupName",
                    "class": "text-center",
                },{
                    "data": "monitorType",
                    "class": "text-center",
                    render:function(data){
                        switch (data){
                            case 1:
                            case '1':
                                data = '车';
                                break;
                            case 2:
                            case '2':
                                data = '人';
                                break;
                            case 3:
                            case '3':
                                data = '物';
                                break;
                            default:
                                data = '';
                                break;
                        }
                        return data;
                    }
                },{
                    "data": "sensorSequence",
                    "class": "text-center",
                    render: function (data) {
                        switch (data) {
                            case 0:
                            case '0':
                                data = '1#载重';
                                break;
                            case 1:
                            case '1':
                                data = '2#载重';
                                break;
                            default:
                                data = '';
                                break;
                        }
                        return data;
                    }
                },
                {
                    "data": "personLoadParam",
                    "class": "text-center",
                    render: function (data) {
                        if(data != null){
                            return data.noLoadValue;
                        }else {
                            return '';
                        }
                    }
                },
                {
                    "data": "personLoadParam",
                    "class": "text-center",
                    render: function (data) {
                        if(data != null) {
                            return data.lightLoadValue;
                        }else {
                            return '';
                        }
                    }
                },
                {
                    "data": "personLoadParam",
                    "class": "text-center",
                    render: function (data) {
                        if(data != null) {
                            return data.fullLoadValue;
                        }else {
                            return '';
                        }
                    }
                },
                {
                    "data": "personLoadParam",
                    "class": "text-center",
                    render: function (data) {
                        if(data != null) {
                            return data.overLoadValue;
                        }else {
                            return '';
                        }
                    }
                },{
                    "data": "sensorNumber",
                    "class": "text-center"
                }];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
                d.groupId = selectGroupId;
                d.assignmentId = selectAssignmentId;
                d.protocol = $("input[name='deviceCheck']:checked").val();
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/v/loadmgt/loadvehiclesetting/list',
                editUrl: '/clbs/v/loadmgt/loadvehiclesetting/edit_',
                deleteUrl: '/clbs/v/loadmgt/loadvehiclesetting/deleteWorkHourSettingBind_',
                deletemoreUrl: '/clbs/v/loadmgt/loadvehiclesetting/deleteMoreWorkHourSettingBind',
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
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            //表格初始化
            myTable.init();
        },
        updateFenceData: function (msg) {
            if (msg != null) {
                var result = $.parseJSON(msg.body);
                if (result != null) {
                    mainObj.updateColumn(result.desc.monitorId)
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
        zTreeOnCheck: function (e, treeId, treeNode) {
        },
        //全选
        cleckAll: function () {
            $("input[name='subChk']").prop("checked", this.checked);
        },
        //单选
        subChkClick: function () {
            $("#checkAll").prop("checked", subChk.length == subChk.filter(":checked").length ? true : false);
        },
        // 下发参数 （单个）
        sendfuelOne: function (id, paramId, vehicleId, monitorType) {
            var arr = [];
            var obj = {};
            obj.sensorVehicleId = id;
            obj.paramId = paramId;
            obj.vehicleId = vehicleId;
            arr.push(obj);
            var jsonStr = JSON.stringify(arr);
            mainObj.sendFuel(jsonStr);
        },
        // 下发参数
        sendFuel: function (sendParam) {
            var url = "/clbs/v/loadmgt/loadvehiclesetting/sendWorkHourSetting";
            var parameter = {"sendParam": sendParam};
            json_ajax("POST", url, "json", true, parameter, mainObj.sendFuelCallback);
        },
        // 下发油杆回调方法
        sendFuelCallback: function (data) {
            if (oilsendflag) {
                webSocket.subscribe(headers,"/user/topic/load", mainObj.updateFenceData, null, null);
                oilsendflag = false;
            }
            if (data.success) {
                layer.msg(sendCommandComplete, {closeBtn: 0}, function (refresh) {
                    myTable.refresh(); //执行的刷新语句
                    layer.close(refresh);
                });
            } else if (!data.success && data.msg != null) {
                layer.msg(data.msg, {move: false});
            }
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
            mainObj.sendFuel(JSON.stringify(checkedList));
        },
        //批量删除
        delModel: function () {
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
                checkedList.push(jsonObj.sensorVehicleId);
            });
            myTable.deleteItems({
                'ids': checkedList.toString()
            });
        },
        // 查询全部
        queryAll: function () {
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
            search_ztree('treeDemo', 'search_condition', 'assignmen');
        },

        //操作设置
        renderButton: function (row) {
            var editUrlPath = '/clbs/v/loadmgt/loadvehiclesetting/getLoadSettingEditPage_' + row.vehicleId + '_' + row.sensorSequence + '_' + row.monitorType + ".gsp"; //修改地址
            var bindUrlPre = '/clbs/v/loadmgt/loadvehiclesetting/getLoadSettingBindPage_{id}_' + row.monitorType;
            var detailUrlPre = '/clbs/v/loadmgt/loadvehiclesetting/getWorkHourSettingDetailPage_' + row.vehicleId + '_' + row.sensorSequence+ '_' + row.monitorType + ".gsp";
            var basicInfoUrlPre = '/clbs/v/loadmgt/loadvehiclesetting/basicInfo_{id}.gsp';
            var generalUrlPre = '/clbs/v/loadmgt/loadvehiclesetting/general_{id}.gsp';
            var newsletterUrlPre = '/clbs/v/loadmgt/loadvehiclesetting/newsletter_{id}.gsp';
            var calibrantionUrlPre = '/clbs/v/loadmgt/loadvehiclesetting/calibrantion_{id}.gsp';
            var parametersUrlPre = '/clbs/v/loadmgt/loadvehiclesetting/parameters_{id}.gsp';
            var upgradeUrlPre = '/clbs/v/loadmgt/loadvehiclesetting/upgrade_{id}.gsp';
            var result = '';
            var paramId = "";
            if (row.settingParamId != null && row.settingParamId != "") {
                paramId = row.paramId;
            }
            if (row.id != null && row.id != "") {
                // 修改
                result += '<button href="'
                    + editUrlPath
                    + '" data-toggle="modal" data-target="#commonWin"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                //删除按钮
                result += '<button type="button" onclick="myTable.deleteItem(\''
                    + row.id
                    + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>解除绑定</button>&ensp;';
                //下发参数
                result += '<button onclick="mainObj.sendfuelOne(\'' + row.id + '\',\'' + paramId + '\',\'' + row.vehicleId + '\',' + row.monitorType + ')" type="button" class="editBtn editBtn-info"><i class="fa fa-issue"></i>下发参数</button>&ensp;';
                //其他
                result += '<div class="btn-group"><button class="editBtn editBtn-info" type="button" id="dropdownMenuOther" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">其他<span class="caret"></span></button>' +
                    '<ul class="dropdown-menu" aria-labelledby="dropdownMenuOther" id="dropdownMenuContent" style="width: 405px">' +

                    '<li><a href="' + detailUrlPre.replace("{id}", row.vehicleId) + '" data-toggle="modal" data-target="#commonWin">详情</a></li>' +
                    '<li><a href="' + basicInfoUrlPre.replace("{id}", row.id) + '" data-toggle="modal" data-target="#commonWin">读取基本信息</a></li>' +
                    '<li><a href="' + generalUrlPre.replace("{id}", row.id) + '" data-toggle="modal" data-target="#commonWin">读取常规参数</a></li>' +
                    // '<li><a href="' + newsletterUrlPre.replace("{id}", row.id) + '" data-toggle="modal" data-target="#commonWin">读取通讯参数</a></li>' +
                    '<li><a href="' + calibrantionUrlPre.replace("{id}", row.id) + '" data-toggle="modal" data-target="#commonWin">读取标定数据</a></li>' +
                    '<li><a href="' + parametersUrlPre.replace("{id}", row.id) + '" data-toggle="modal" data-target="#commonWin">私有参数设置</a></li>' +
                    // '<li><a href="' + upgradeUrlPre.replace("{id}", row.id) + '" data-toggle="modal" data-target="#commonWin">传感器远程升级</a></li>' +
                    '</ul></div>';
            } else {
                // 设置
                result += '<button href="' + bindUrlPre.replace("{id}", row.vehicleId) + '"  data-toggle="modal" data-target="#commonWin"  type="button" class="editBtn editBtn-info"><i class="fa fa-set"></i>设置</button>&ensp;';
                //禁用删除按钮
                result += '<button disabled type="button" onclick="myTable.deleteItem(\''
                    + row.id
                    + '\')" class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>解除绑定</button>&ensp;';
                //禁用下发参数
                result += '<button disabled type="button" class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>下发参数</button>&ensp;';
                //其他
                result += '<div class="btn-group"><button class="editBtn editBtn-info" type="button" id="DisDropdownMenuOther" data-toggle="" aria-haspopup="true" aria-expanded="true">其他<span class="caret"></span></button></ul></div>';
            }
            var vehicleId = 'button_' + row.vehicleId;
            return '<div id="'+vehicleId+'">'+ result + '</div>'
        },
        //下发状态
        renderStatus: function (row, flag) {
            var val = '';
            var vehicleId = 'status_' + row.vehicleId;
            var data = row.status;
            if (data == "0") {
                val =  '参数已生效';
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

            if(!flag) {
                return  "<div id= '"+ vehicleId+"'>" + val + "</div>"
            }else{
                return  val;
            }
        },

        //更新列
        updateColumn: function (id){
            for(var i= 0; i< tableList.length; i++) {
                if(id == tableList[i].vehicleId){
                    var url = '/clbs/v/loadmgt/loadvehiclesetting/refreshSendStatus?vehicleId=' + id;
                    json_ajax('GET', url, 'json', false, null, function (messages) {
                        if(messages.success){
                            var data = messages.obj;
                            $("#status_" + id).text(mainObj.renderStatus(data, true));
                        }
                    });
                }
            }
        },
    };
    $(function () {
        mainObj.init();
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'search_condition') {
                search_ztree('treeDemo', id, 'assignment');
            }
            ;
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
        $("#checkAll").bind("click", mainObj.cleckAll);
        subChk.bind("click", mainObj.subChkClick);
        // 批量下发
        $("#send_model").bind("click", mainObj.sendModelClick);
        //批量删除
        $("#del_model").bind("click", mainObj.delModel);
        // 查询全部
        $('#refreshTable').bind("click", mainObj.queryAll);
        // 组织架构模糊搜索
        $("#search_condition").on("input oninput", mainObj.searchCondition);

        //改变协议勾选框
        $(".device").change(function() {
            //取消全选
            $("#checkAll").prop('checked',false);
            //刷新表格
            myTable.requestData();
        });
    })
})(window, $)