(function (window, $) {
    var selectGroupId = '';
    var selectAssignId = '';
    var selectTreepId = '';
    //显示隐藏列
    var menu_text = "";
    var table = $("#dataTable tr th:gt(1)");
    //单选
    var subChk = $("input[name='subChk']");
    personnelInformation = {
        //初始化
        init: function () {
            menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            ;
            $("#Ul-menu-text").html(menu_text);
            // 组织树结构
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
                    dataFilter: personnelInformation.ajaxDataFilter
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
                    onClick: personnelInformation.zTreeOnClick
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
            var columns = [{
                //第一列，用来显示序号
                "data": null,
                "class": "text-center"
            }, {
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
                        result += '<button href="' + editUrlPath + '" data-target="#commonSmWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                        //删除按钮
                        result += '<button type="button" onclick="personnelInformation.deleteItemAndCheckBond(\'' + row.id + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
                        return result;
                    }
                }, {
                    "data": "peopleNumber",
                    "class": "text-center"
                }, {
                    "data": "groupName",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == "null" || data == null) {
                            return '';
                        } else {
                            return data;
                        }
                    }
                }, {
                    "data": "assign",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == "null" || data == null) {
                            return '';
                        } else {
                            return data;
                        }
                    }
                }, {
                    "data": "name",
                    "class": "text-center"
                }, {
                    "data": "gender",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == "1") {
                            return '男';
                        } else if (data !== "1") {
                            return '女';
                        }
                    }
                }, {
                    "data": "identity",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == "null" || data == null) {
                            return '';
                        } else {
                            return data;
                        }
                    }
                }, {
                    "data": "identityCardPhoto",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        console.log('identityCardPhoto', data);
                        if (data != '' && data != null && data != undefined) {
                          return '<span onclick="personnelInformation.shouIdentityCardPhoto(\'' + data + '\')" style="color: #6dcff6; cursor: pointer">查看照片</span>';
                        }
                        return '';
                    }
                }, {
                    "data": "phone",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == "null" || data == null) {
                            return '';
                        } else {
                            return data;
                        }
                    }
                }, {
                    "data": "email",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == "null" || data == null) {
                            return '';
                        } else {
                            return data;
                        }
                    }
                }, {
                    "data": "deviceNumber",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == "null" || data == null) {
                            return '';
                        } else {
                            return data;
                        }
                    }
                }, {
                    "data": "simcardNumber",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == "null" || data == null) {
                            return '';
                        } else {
                            return data;
                        }
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
                d.groupId = selectGroupId;
                d.assignId = selectAssignId;
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/m/basicinfo/monitoring/personnel/list',
                editUrl: '/clbs/m/basicinfo/monitoring/personnel/edit_',
                deleteUrl: '/clbs/m/basicinfo/monitoring/personnel/delete_',
                deletemoreUrl: '/clbs/m/basicinfo/monitoring/personnel/deletemore',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            //表格初始化
            myTable.init();
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
                selectAssignId = treeNode.id;
                selectTreepId = treeNode.pId;
                selectGroupId = '';
            } else {
                selectGroupId = treeNode.uuid;
                selectTreepId = treeNode.id;
                selectAssignId = '';
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
        // 删除操作
        deleteItemAndCheckBond: function (id) {
            var url = "/clbs/m/basicinfo/monitoring/personnel/delete_" + id + ".gsp";
            var data = {"id": id};
            layer.confirm(publicDelete, {btn: ["确定", "取消"], icon: 3, title: "操作确认"}, function () {
                json_ajax("POST", url, "json", true, data, personnelInformation.deleteCallBack);
            });
        },
        // 删除操作回调
        deleteCallBack: function (data) {
            if (data.success) {
                var vid = data.obj.peopleId;
                if (data != null && data.obj != null && data.obj.infoMsg != null) {
                    layer.confirm(data.obj.infoMsg, {btn: ['确认', '取消']}, function () {
                        // step1:根据车辆id获取人员终端绑定表id
                        var url = "/clbs/m/infoconfig/infoinput/getConfigIdByVehicleId";
                        var data = {"vehicleId": vid};
                        json_ajax("POST", url, "json", true, data, personnelInformation.getConfigIdByPeopleIdCallBack);
                    });
                } else {
                    layer.closeAll();
                    myTable.refresh();
                }
            } else {
                layer.msg(publicError);
            }
        },
        // 根据人员id获取人员和终端绑定表id的回调
        getConfigIdByPeopleIdCallBack: function (data) {
            if (data.success) {
                // step2:根据绑定表id解除人员与终端的绑定关系
                var configId = data.obj.configId;
                var url = "/clbs/m/infoconfig/infoinput/delete_" + configId + ".gsp";
                json_ajax("POST", url, "json", true, data, personnelInformation.deleteConfigByVidCallBack);
            } else {
                layer.msg(publicError);
            }
        },
        // 根据人员id获取信息配置表id并删除绑定关系的回调
        deleteConfigByVidCallBack: function (data) {
            if (data.success) {
                // step3:解除之后，再直接删除人员
                var vid = data.obj.vehicleId;
                var url = "/clbs/m/basicinfo/monitoring/personnel/delete_" + vid + ".gsp";
                var data = {"id": vid};
                json_ajax("POST", url, "json", true, data, personnelInformation.deleteCallBack);
            } else {
                layer.msg(publicError);
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
                checkedList.push($(this).val());
            });
            var url = "/clbs/m/basicinfo/monitoring/personnel/deletemore";
            var data = {"checkedList": checkedList.toString()};
            layer.confirm(publicDelete, {btn: ["确定", "取消"], icon: 3, title: "操作确认"}, function () {
                json_ajax_noTimeout("POST", url, "json", true, data, personnelInformation.deleteMuchCallBack);
            });
        },
        // 批量删除回调
        deleteMuchCallBack: function (data) {
            if (data.success) {
                if (data != null && data.obj != null && data.obj.infoMsg != null && data.obj.infoMsg.length > 0) {
                    var boundBrandIds = data.obj.boundBrandIds;
                    layer.confirm("已绑定人员：" + data.obj.boundBrands + "</br>" + data.obj.infoMsg, {btn: ['确认', '取消']}, function () {
                        if (boundBrandIds != null && boundBrandIds != "") {
                            var url = "/clbs/m/infoconfig/infoinput/getConfigIdByMonitorIds";
                            var data = {"vehicleId": boundBrandIds};
                            json_ajax_noTimeout("POST", url, "json", true, data, personnelInformation.getConfigIdByPersonIdInMuchCallBack);
                        }
                    }, function () {
                        layer.closeAll();
                        myTable.refresh();
                    });
                } else {
                    layer.closeAll();
                    myTable.refresh();
                    layer.msg("删除成功");
                }
            } else {
                layer.msg(publicError);
            }
        },
        getConfigIdByPersonIdInMuchCallBack: function (data) {
            if (data.success) {
                // step2:根据绑定表id解除车辆与终端的绑定关系
                var configId = data.obj.configId;
                var data = {"deltems": configId};
                var url = "/clbs/m/infoconfig/infoinput/deletemore";
                json_ajax_noTimeout("POST", url, "json", true, data, personnelInformation.deleteConfigByPersonIdInMuchCallBack);
            } else {
                layer.msg(data.msg);
            }
        },
        deleteConfigByPersonIdInMuchCallBack: function (data) {
            if (data.success) {
                // step3:解除之后，再直接删除人员
                var vid = data.obj.vehicleId;
                var url = "/clbs/m/basicinfo/monitoring/personnel/deletemore";
                var dataIds = {"checkedList": vid};
                json_ajax_noTimeout("POST", url, "json", true, dataIds, personnelInformation.deleteMuchCallBack);
            } else {
                layer.msg(data.msg);
            }
        },
        //刷新列表
        refreshTable: function () {
            selectGroupId = '';
            selectAssignId = '';
            $("#simpleQueryParam").val("");
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.selectNode("");
            zTree.cancelSelectedNode();
            myTable.requestData();
        },
        addId: function () {
            $("#addId").attr("href", "/clbs/m/basicinfo/monitoring/personnel/add?uuid=" + selectTreepId + "");
        },
        // 展示身份证照片
        shouIdentityCardPhoto: function (photoUrl) {
          $('#idCardPhoto').attr('src', photoUrl);
          $('#identityCardPhotoModal').modal('show');
        }
    }
    $(function () {
        personnelInformation.init();
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

        // 输入框输入类型过滤
        inputValueFilter('#simpleQueryParam');

        //全选
        $("#checkAll").bind("click", personnelInformation.checkAllClick);
        subChk.bind("click", personnelInformation.subChkClick);
        //批量删除
        $("#del_model").bind("click", personnelInformation.delModelClick);
        $("#refreshTable").on("click", personnelInformation.refreshTable);
        $("#addId").on("click", personnelInformation.addId);
    })
})(window, $)