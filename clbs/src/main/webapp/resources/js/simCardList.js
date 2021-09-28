(function (window, $) {

    var selectTreeId = '';
    var selectTreepId = "";
    var selectTreeType = '';

    var params = [];
    //显示隐藏列
    var menu_text = "";
    var subChk = $("input[name='subChk']");
    var simsenflag = true;
    simCardManagement = {
        init: function () {
            // webSocket.init('/clbs/vehicle');
            // 请求后台，获取所有订阅的车
            // setTimeout(function () {
            //     webSocket.subscribe(headers,'/topic/fencestatus', simCardManagement.updataSimData,null, null);
            // },500);
            //列筛选
            var table = $("#dataTable tr th:gt(1)");
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            ;
            $("#Ul-menu-text").html(menu_text);
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
                        result += '<input  type="checkbox" name="subChk"  value="' + row.id + '" />';
                        return result;
                    }
                },
                {
                    "data": null,
                    "class": "text-center", //最后一列，操作按钮
                    render: function (data, type, row, meta) {
                        var editUrlPath = myTable.editUrl + row.id + ".gsp"; //修改地址
                        var result = '';
                        //修改按钮
                        result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                        if (!row.brand) {
                            result += '<button disabled class="editBtn btn-default deleteButton" type="button"><i class=" fa fa-ban"></i>下发参数</button>&ensp;'
                        } else {
                            result += '<button onclick="simCardManagement.sendSimParam(\'' + row.id + '\',\'' + row.vehicleId + '\',\'' + row.configId + '\',\'' + row.paramId + '\')" class="editBtn editBtn-info" type="button"><i class="glyphicon glyphicon-circle-arrow-down"></i>下发参数</button>&ensp;'
                        }
                        //删除按钮
                        result += '<button type="button" onclick="myTable.deleteItem(\'' + row.id + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
                        return result;
                    }
                },
                {
                    "data": "pstatus",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == "0") {
                            return '指令已生效';
                        } else if (data == "1") {
                            return '指令未生效';
                        } else if (data == "2") {
                            return "指令未生效";
                        } else if (data == "3") {
                            return "指令未生效";
                        } else if (data == "4") {
                            return "指令已发出";
                        } else if (data == "5") {
                            return "设备离线，未下发";
                        } else {
                            return "";
                        }
                    }
                }, {
                    "data": "iccid",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                },
                {
                    "data": "imei",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                },
                {
                    "data": "imsi",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "simcardNumber",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                }, {
                    "data": "realId",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "groupName",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                }, {
                    "data": "isStart",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == 0) {
                            return '停用';
                        } else if (data == 1) {
                            return '启用';
                        }
                    }
                },
                {
                    "data": "operator",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                }, {
                    "data": "placementCity",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : html2Escape(data);
                    }
                },
                {
                    "data": "simFlow",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                }, {
                    "data": "dayRealValue",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                }, {
                    "data": "monthRealValue",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                }, {
                    "data": "monthTrafficDeadline",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                }, {
                    "data": "alertsFlow",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                }, {
                    "data": "monthlyStatement",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                }, {
                    "data": "correctionCoefficient",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                }, {
                    "data": "forewarningCoefficient",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                }, {
                    "data": "hourThresholdValue",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                }, {
                    "data": "dayThresholdValue",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                }, {
                    "data": "monthThresholdValue",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                }, {
                    "data": "openCardTime",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data ? data : "";
                    }
                }, {
                    "data": "endTime",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data ? data : "";
                    }
                }, {
                    "data": "deviceNumber",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                }, {
                    "data": "brand",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                }, {
                    "data": "createDataTime",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data ? data : "";
                    }
                }, {
                    "data": "updateDataTime",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data ? data : "";
                    }
                }, {
                    "data": "remark",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
                // d.groupName = selectTreeId;
                d.groupId = selectTreeId;
                // d.groupType = selectTreeType;
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/m/basicinfo/equipment/simcard/list',
                editUrl: '/clbs/m/basicinfo/equipment/simcard/edit_',
                deleteUrl: '/clbs/m/basicinfo/equipment/simcard/delete_',
                deletemoreUrl: '/clbs/m/basicinfo/equipment/simcard/deletemore',
                enableUrl: '/clbs/c/user/enable_',
                disableUrl: '/clbs/c/user/disable_',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列sda
                enabledChange: true
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            //表格初始化
            myTable.init();
        },
        //全选
        checkAll: function () {
            $("input[name='subChk']").prop("checked", this.checked);
        },
        //单选
        subChkClick: function () {
            $("#checkAll").prop("checked", subChk.length == subChk.filter(":checked").length ? true : false);
        },
        sendSimParam: function (id, vid, cid, paramId) {
            var url = "/clbs/m/basicinfo/equipment/simcard/sendSimP";
            json_ajax("POST", url, "json", false, { "simId": id, "vehicleId": vid, "parameterName": cid, "type": 2 }, simCardManagement.sendCallBack);
            layer.msg(sendCommandComplete);
        },
        sendCallBack: function (data) {
            if (simsenflag) {
                webSocket.subscribe(headers, "/user/" +$("#userName").text() +"/alarm_parameter_setting", simCardManagement.updataSimData, null, null);
                simsenflag = false;
            }
            layer.closeAll()
            myTable.refresh();
        },
        updataSimData: function (msg) {
            if (msg != null) {
                var result = $.parseJSON(msg.body);
                if (result != null) {
                    myTable.refresh();
                }
            }
        },
        //批量删除
        delModelClick: function () {
            //判断是否至少选择一项
            var chechedNum = $("input[name='subChk']:checked").length;
            if (chechedNum == 0) {
                layer.msg(selectItem, { move: false });
                return
            }
            var checkedList = new Array();
            $("input[name='subChk']:checked").each(function () {
                checkedList.push($(this).val());
            });
            myTable.deleteItems({
                'deltems': checkedList.toString()
            });
        },
        //刷新
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            //            selectTreeId = '';
            //            selectTreeType = '';
            //            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            //            zTree.selectNode("");
            //            zTree.cancelSelectedNode();
            myTable.requestData();
        },
        groupListTree: function () {
            var treeSetting = {
                async: {
                    // url: "/clbs/m/basicinfo/enterprise/assignment/assignmentTree",
                    url: '/clbs/m/tree/org/tree',
                    tyoe: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {  // 是否可选  Organization
                        "searchType": "0",//搜索类型 0:企业; 1:分组; 不传展示所有
                        simpleQueryParamL: $("#search_condition").val()
                    },
                    dataFilter: simCardManagement.groupAjaxDataFilter
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
                    onClick: simCardManagement.zTreeOnClick
                }
            };
            $.fn.zTree.init($("#treeDemo"), treeSetting, null);
        },
        //组织树预处理函数
        groupAjaxDataFilter: function (treeId, parentNode, responseData) {
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
            if (treeNode.type == "group") {
                selectTreepId = treeNode.id;
                selectTreeId = treeNode.uuid;
            } else {
                selectTreepId = treeNode.pId;
                selectTreeId = treeNode.id;
            }
            selectTreeType = treeNode.type;
            myTable.requestData();
        },
    }
    $(function () {
        $('input').inputClear();
        //初始化
        simCardManagement.init();
        simCardManagement.groupListTree();
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'search_condition') {
                search_ztree('treeDemo', id, 'group');
            };
        });
        //IE9
        if (navigator.appName == "Microsoft Internet Explorer" && navigator.appVersion.split(";")[1].replace(/[ ]/g, "") == "MSIE9.0") {
            var search;
            $("#search_condition").bind("focus", function () {
                search = setInterval(function () {
                    search_ztree('treeDemo', 'search_condition', 'group');
                }, 500);
            }).bind("blur", function () {
                clearInterval(search);
            });
        }

        // 输入框输入类型过滤
        inputValueFilter('#simpleQueryParam');

        //全选
        $("#checkAll").bind("click", simCardManagement.checkAll);
        //单选
        subChk.bind("click", simCardManagement.subChkClick);
        //批量删除
        $("#del_model").bind("click", simCardManagement.delModelClick);
        //刷新
        $("#refreshTable").on("click", simCardManagement.refreshTable);

        // 组织架构模糊搜索
        $("#search_condition").on("input oninput", function () {
            search_ztree('treeDemo', 'search_condition', 'group');
        });
    })
})(window, $)