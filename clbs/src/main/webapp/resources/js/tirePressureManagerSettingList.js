(function (window, $) {
    //显示隐藏列
    var menu_text = "";
    var table = $("#dataTable tr th:gt(1)");
    //单选
    var subChk = $("input[name='subChk']");
    var selectGroupId = '';
    var selectAssignmentId = '';
    var temsendflag = true;
    var dtext = "";
    var dn = new Array("交通部JT/T808-2013","交通部JT/T808-2019");
    var dv = new Array("1","11");
    var protocol = 1;
    var tableList;
    tirePressureSensorSet = {
        //初始化
        init: function () {
            //websocket 连接
            // webSocket.init('/clbs/vehicle');
            var params = [];
            // 接收到消息
            // setTimeout(function () {
            //     webSocket.subscribe(headers,'/topic/fencestatus', tirePressureSensorSet.updataFenceData,"", null);
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
            //数据表格列筛选
            menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" />" + table[i].innerHTML + "</label></li>"
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

            tirePressureSensorSet.initTable();
            tirePressureSensorSet.initTree();
        },
        //初始化表格
        initTable: function () {
            var columnDefs = [{
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
                    if (row.sensorId != null && row.sensorId != "") {
                        var obj = {};
                        obj.id = row.id;
                        obj.paramId = row.paramId;
                        obj.vehicleId = row.vehicleId;
                        var jsonStr = JSON.stringify(obj);
                        result += "<input  type='checkbox' name='subChk'  value='" + jsonStr + "'/>";
                    }
                    return result;
                }
            },
                {
                    "data": null,
                    "class": "text-center", //最后一列，操作按钮
                    render: function (data, type, row, meta) {
                        var editUrlPath = myTable.editUrl + row.id + "_" + row.vehicleId; //修改地址
                        var bindUrlPre = '/clbs/v/tyrepressure/setting/bind_{id}';//设置地址
                        var detailUrlPre = '/clbs/v/tyrepressure/setting/detail_{id}';
                        var basicInfoUrlPre = '/clbs/v/tyrepressure/setting/basic_{id}';
                        var generalUrlPre = '/clbs/v/tyrepressure/setting/general_{id}';
                        var paramId = "";
                        if (row.paramId != null && row.paramId != "") {
                            paramId = row.paramId;
                        }
                        var result = '';
                        if (row.sensorName == null || row.sensorName == "") {
                            // 设置
                            result += '<button href="' + bindUrlPre.replace("{id}", row.vehicleId) + '"  data-toggle="modal" data-target="#commonWin"  type="button" class="editBtn editBtn-info"><i class="fa fa-set"></i>设置</button>&ensp;';
                            // 禁用删除按钮
                            result += '<button disabled type="button" onclick="myTable.deleteItem(\''
                                + row.id
                                + '\')" class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>解除绑定</button>&ensp;';
                            // 禁用下发参数
                            result += '<button disabled type="button" class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>下发参数</button>&ensp;';
                            //其他
                            result += '<div class="btn-group"><button class="editBtn editBtn-info" type="button" id="DisDropdownMenuOther" data-toggle="" aria-haspopup="true" aria-expanded="true">其他<span class="caret"></span></button></ul></div>';
                            // 详细信息
                           /* result += '<button disabled href="' + detailUrlPre.replace("{id}", row.id) + '" data-toggle="modal" data-target="#commonWin" type="button" class="editBtn  btn-default deleteButton"><i class="fa fa-eye"></i>详情</button>&ensp;';*/
                        } else {
                            // 修改
                            result += '<button href="'
                                + editUrlPath
                                + '" data-toggle="modal" data-target="#commonWin"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                            //删除按钮
                            result += '<button type="button" onclick="myTable.deleteItem(\'' + row.id + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>解除绑定</button>&ensp;';
                            // 下发参数
                            result += '<button onclick="tirePressureSensorSet.sendfuelOneVechice(\'' + row.id + '\',\'' + paramId + '\',\'' + row.vehicleId + '\')" type="button" class="editBtn editBtn-info"><i class="fa fa-issue"></i>下发参数</button>&ensp;';
                            result += '<div class="btn-group"><button class="editBtn editBtn-info" type="button" id="dropdownMenuOther" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">其他<span class="caret"></span></button>' +
                                '<ul class="dropdown-menu" aria-labelledby="dropdownMenuOther" id="dropdownMenuContent" style="width: 230px">' +
                                '<li><a href="' + detailUrlPre.replace("{id}", row.id) + '" data-toggle="modal" data-target="#commonWin">详情</a></li>' +
                                '<li><a href="' + basicInfoUrlPre.replace("{id}", row.id) + '" data-toggle="modal" data-target="#commonWin">读取基本信息</a></li>' +
                                '<li><a href="' + generalUrlPre.replace("{id}", row.id) + '" data-toggle="modal" data-target="#commonWin">读取常规参数</a></li>' +
                                '</ul></div>';
                            // 详细信息
                           /* result += '<button href="' + detailUrlPre.replace("{id}", row.id) + '" data-toggle="modal" data-target="#commonWin" type="button" class="editBtn editBtn-info"><i class="fa fa-eye"></i>详情</button>&ensp;';*/
                        }
                        return result;
                    }
                }, {
                    "data": "status",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return tirePressureSensorSet.renderStatus(row, false);
                    }
                }, {
                    "data": "brand",
                    "class": "text-center"
                }, {
                    "data": "groupName",
                    "class": "text-center"
                }, {
                    "data": "vehicleType",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == "" || data == null || data == undefined) {
                            return '-';
                        }
                        return data;
                    }
                }, {
                    "data": "sensorName",
                    "class": "text-center",
                }, {
                    "data": "compensate",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (!data) return '';
                        if (data == '1') {
                            return '使能';
                        } else if (data == '2') {
                            return '禁用';
                        }
                    }
                }, {
                    "data": "filterFactor",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        switch (data) {
                            case 1:
                                return '实时';
                                break;
                            case 2:
                                return '平滑';
                                break;
                            case 3:
                                return '平稳';
                                break;
                            default:
                                return '';
                                break;
                        }
                    }
                }, {
                    "data": "tyrePressureParameter",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (!data) return '';
                        if (data != null) {
                            var autoTime = data.automaticUploadTime;
                            switch (autoTime) {
                                case 1:
                                    return '被动';
                                    break;
                                case 2:
                                    return 10;
                                    break;
                                case 3:
                                    return 20;
                                    break;
                                case 4:
                                    return 30;
                                    break;
                                default:
                                    return '';
                                    break;
                            }
                        }
                    }
                }, {
                    "data": "tyrePressureParameter",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (!data) return '';
                        return data.compensationFactorK ? data.compensationFactorK : '';
                    }
                }, {
                    "data": "tyrePressureParameter",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (!data) return '';
                        return data.compensationFactorB!=null ? data.compensationFactorB : '';
                    }
                }, {
                    "data": "tyrePressureParameter",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (!data) return '';
                        return data.pressure!=null ? data.pressure : '';
                    }
                }, {
                    "data": "tyrePressureParameter",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == "" || data == null || data == undefined) return '';
                        return data.pressureThreshold!=null ? data.pressureThreshold : '';
                    }
                }, {
                    "data": "tyrePressureParameter",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == "" || data == null || data == undefined) return '';
                        return data.slowLeakThreshold!=null ? data.slowLeakThreshold : '';
                    }
                }, {
                    "data": "tyrePressureParameter",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == "" || data == null || data == undefined) return '';
                        return data.highTemperature!=null ? data.highTemperature : '';
                    }
                }, {
                    "data": "tyrePressureParameter",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (!data) return '';
                        return data.lowPressure!=null ? data.lowPressure : '';
                    }
                }, {
                    "data": "tyrePressureParameter",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (!data) return '';
                        return data.heighPressure!=null ? data.heighPressure : '';
                    }
                }, {
                    "data": "tyrePressureParameter",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (!data) return '';
                        return data.electricityThreshold!=null ? data.electricityThreshold : '';
                    }
                }];

            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
                d.protocol = $("input[name='deviceCheck']:checked").val();
                d.groupId = selectGroupId;
                d.assignmentId = selectAssignmentId;
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/v/tyrepressure/setting/list',
                editUrl: '/clbs/v/tyrepressure/setting/edit_',
                deleteUrl: '/clbs/v/tyrepressure/setting/delete_',
                deletemoreUrl: '/clbs/v/tyrepressure/setting/deleteMore',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                ajaxCallBack: function (data){
                    tableList = data.records;
                }
            };

            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
        },
        //初始化组织树
        initTree: function () {
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
                    dataFilter: tirePressureSensorSet.ajaxDataFilter
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
                    onClick: tirePressureSensorSet.zTreeOnClick
                }
            };
            $.fn.zTree.init($("#treeDemo"), treeSetting, null);
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
                if (result != null) {
                    tirePressureSensorSet.updateColumn(result.desc.monitorId);
                }
            }
        },
        //批量删除
        delModelClick: function () {
            //判断是否至少选择一项
            var chechedNum = $("input[name='subChk']:checked").length;
            if (chechedNum == 0) {
                layer.msg(selectItem, {move: false});
                return
            }
            var checkedList = new Array();
            $("input[name='subChk']:checked").each(function () {
                var jsonStr = $(this).val();
                var jsonObj = $.parseJSON(jsonStr);
                checkedList.push(jsonObj.id);
            });
            myTable.deleteItems({'ids': checkedList.toString()});
        },
        //刷新列表
        refreshTable: function () {
            selectGroupId = "";
            selectAssignmentId = "";
            $('#simpleQueryParam').val("");
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.selectNode("");
            zTree.cancelSelectedNode();
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
                if (jsonObj.id != null && jsonObj.id != 'null') {
                    checkedList.push(jsonObj);
                }
            });
            // 下发方法调用
            tirePressureSensorSet.sendFuel(JSON.stringify(checkedList));
        },
        sendFuel: function (sendParam) {//批量下发执行
            var url = "/clbs/v/tyrepressure/setting/send";
            var parameter = {"sendParam": sendParam};
            json_ajax("POST", url, "json", true, parameter, tirePressureSensorSet.sendFuelCallback);
        },
        // 下发流量回调方法
        sendFuelCallback: function (data) {
            if (temsendflag) {
                webSocket.subscribe(headers, "/user/topic/tire_pressure_monitoring", tirePressureSensorSet.updataFenceData, "", null);
                temsendflag = false;
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
        sendfuelOneVechice: function (id, paramId, vehicleId) {// 下发参数 （单个）
            var arr = [];
            var obj = {};
            obj.id = id;
            obj.paramId = paramId;
            obj.vehicleId = vehicleId;
            arr.push(obj);
            var jsonStr = JSON.stringify(arr);
            tirePressureSensorSet.sendFuel(jsonStr);
        },

        renderStatus: function (row, flag){
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
                    var url = '/clbs/v/tyrepressure/setting/refreshSendStatus?vehicleId=' + id;
                    json_ajax('GET', url, 'json', false, null, function (messages) {
                        if(messages.success){
                            var data = messages.obj;
                            $("#status_" + id).text(tirePressureSensorSet.renderStatus(data, true));
                        }
                    });
                }
            }
        }

    };
    $(function () {
        tirePressureSensorSet.init();
        // 组织架构模糊搜索
        $("#search_condition").on("input oninput", function () {
            search_ztree('treeDemo', 'search_condition', 'assignmen');
        });
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
        //全选
        $("#checkAll").bind("click", tirePressureSensorSet.checkAllClick);
        subChk.bind("click", tirePressureSensorSet.subChkClick);
        //批量删除
        $("#del_model").bind("click", tirePressureSensorSet.delModelClick);
        //刷新
        $("#refreshTable").on("click", tirePressureSensorSet.refreshTable);
        //批量下发
        $("#send_model").bind("click", tirePressureSensorSet.sendModelClick);

        //改变协议勾选框
        $(".device").change(function() {
            //取消全选
            $("#checkAll").prop('checked',false);
            //刷新表格
            myTable.requestData();
        });
    })
})(window, $)