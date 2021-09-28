(function (window, $) {
    var selectTreeId = '';
    var selectTreepId = "";
    var selectTreeType = '';

    //显示隐藏列
    var menu_text = "";
    var subChk = $("input[name='subChk']");

    intercomObject = {
        init: function () {
            //列筛选
            var table = $("#dataTable tr th:gt(1)");
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-text").html(menu_text);

            intercomObject.groupListTree();
            intercomObject.intercomTableInit();

            var isAdmin = $('#isAdmin').val();
            if (isAdmin == 'false') {// 非admin用户隐藏是否录音列
                var recordEnableCol = $("#Ul-menu-text li:last-child");
                recordEnableCol.find('input').click();
                recordEnableCol.hide();
                $('#customizeColumns').click();
            }
        },
        intercomTableInit: function () {
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
                        var editUrlPath = myTable.editUrl + row.id; //修改地址
                        var friendSetPath = '/clbs/m/intercom/intercomObject/getAddFriendsPage_multiple_' + row.id;// 好友设置地址
                        var result = '';
                        //修改按钮
                        result += '<button href="' + editUrlPath + '" data-target="#commonSmWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&nbsp;';
                        if (!row.configId) {
                            result += '<button disabled class="editBtn btn-default deleteButton" type="button"><i class="fa fa-edit"></i>生成</button>&nbsp;';
                        } else {
                            result += '<button onclick="intercomObject.generatorIntercomInfo(\'' + row.id + '\')" class="editBtn editBtn-info" type="button"><i class="fa fa-edit"></i>生成</button>&nbsp;';
                        }
                        if (!row.userId || row.maxFriendNum <= 0) {
                            // 是对讲兑现, 并且好友数量大于0
                            result += '<button disabled class="editBtn btn-default deleteButton" type="button"><i class="fa faEdit"></i>好友设置</button>&nbsp;'
                        } else {
                            result += '<button  href="' + friendSetPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa faEdit"></i>好友设置</button>&nbsp;'
                        }
                        //删除按钮
                        result += '<button type="button" onclick="intercomObject.deleteIntercomInfo(\'' + row.id + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>移除</button>';
                        return result;
                    }
                },
                {
                    "data": "monitorName",
                    "class": "text-center"
                }, {
                    "data": "status",
                    "class": "text-center",
                    render: function (data) {
                        if (data == '0') {
                            return '';
                        } else if (data == '1') {
                            return '已生成';
                        } else {
                            return '生成失败';
                        }
                    }
                }, {
                    "data": "groupName",
                    "class": "text-center",
                    render: function (data) {
                        return data ? data : '';
                    }
                },
                {
                    "data": "deviceNumber",
                    "class": "text-center",
                    render: function (data) {
                        return data ? data : '';
                    }
                },
                {
                    "data": "intercomDeviceId",
                    "class": "text-center",
                    render: function (data) {
                        return data ? data : '';
                    }
                }, {
                    "data": "modelId",
                    "class": "text-center",
                    render: function (data) {
                        return data ? data : '';
                    }
                }, {
                    "data": "intercomModelName",
                    "class": "text-center",
                    render: function (data) {
                        return data ? data : '';
                    }
                }, {
                    "data": "simcardNumber",
                    "class": "text-center",
                    render: function (data) {
                        return data ? data : '';
                    }
                },
                {
                    "data": "priority",
                    "class": "text-center",
                    render: function (data) {
                        return data ? data : '';
                    }
                },
                {
                    "data": "customerCode",
                    "class": "text-center",
                    render: function (data) {
                        return data ? data : '';
                    }
                }, {
                    "data": "number",
                    "class": "text-center",
                    render: function (data) {
                        return data ? data : '';
                    }
                }, {
                    "data": "maxGroupNum",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        var currentGroupNum = row.currentGroupNum ? row.currentGroupNum : 0;
                        var maxGroupNum = data ? data : 0;
                        return currentGroupNum.toString() + '/' + maxGroupNum.toString();
                    }
                }, {
                    "data": "recordEnable",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == '0') {
                            if (!row.userId) {
                                return '<div class="mySwitch openRecordEnable disabledRecord">已关闭</div>';
                            } else {
                                return '<div class="mySwitch openRecordEnable" onclick="intercomObject.updateRecordStatus(\'' + row.monitorName + '\',\'' + row.userId + '\',\'' + row.recordEnable + '\',\'' + row.id + '\')">已关闭</div>';
                            }
                        } else {
                            if (!row.userId) {
                                return '<div class="mySwitch openRecordEnable disabledRecord">已开启</div>';
                            } else {
                                return '<div class="mySwitch" onclick="intercomObject.updateRecordStatus(\'' + row.monitorName + '\',\'' + row.userId + '\',\'' + row.recordEnable + '\',\'' + row.id + '\')">已开启</div>';
                            }
                        }
                    }
                }];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.assignmentId = selectTreeId;
                d.organizationId = selectTreepId;
                d.status = $('#status').val();
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/m/intercom/intercomObject/list',
                editUrl: '/clbs/m/intercom/intercomObject/edit_',
                // deleteUrl: '/clbs/m/basicinfo/equipment/simcard/delete_',
                deletemoreUrl: '/clbs/m/intercom/intercomObject/delete',
                enableUrl: '/clbs/c/user/enable_',
                disableUrl: '/clbs/c/user/disable_',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                "lengthMenu": [10],
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
        // 生成对讲对象
        generatorIntercomInfo: function (id) {
            var url = '/clbs/m/intercom/intercomObject/generatorIntercomInfo';
            json_ajax("POST", url, "json", false, {
                "intercomInfoId": id,
            }, function (data) {
                if (data.success) {
                    if (data.msg) {
                        layer.msg(data.msg);
                    }
                    myTable.refresh();
                }else {
                    layer.msg('生成失败');
                }
            });
        },
        //批量生成对讲对象
        delMooreIntercomInfo: function () {
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
            var url = '/clbs/m/intercom/intercomObject/generatorIntercomInfoBatch';
            json_ajax("POST", url, "json", false, {
                "intercomInfoIds": checkedList.toString(),
            }, function (data) {
                if (data.success) {
                    if (data.msg) {
                        layer.msg(data.msg);
                    }
                    myTable.refresh();
                } else if (data.msg) {
                    layer.msg(data.msg);
                } else {
                    layer.msg('生成失败');
                }
            });
        },
        // 移除对讲对象
        deleteIntercomInfo: function (id) {
            layer.confirm("删掉就没啦，请谨慎下手！", {icon: 3, btn: ["确定", "取消"]}, function () {
                var url = '/clbs/m/intercom/intercomObject/delete';
                json_ajax("POST", url, "json", false, {
                    "intercomInfoIds": id,
                }, function (data) {
                    layer.closeAll();
                    if (data.success) {
                        myTable.refresh();
                    } else {
                        layer.msg('删除失败');
                    }
                });
            });
        },
        // 修改录音状态
        updateRecordStatus: function (monitorName, userId, recordEnable, id) {
            layer.confirm("确认修改录音状态?", {btn: ["确定", "取消"], icon: 7, title: "操作确认"}, function () {
                var url = '/clbs/m/intercom/intercomObject/updateRecordStatus';
                json_ajax("POST", url, "json", false, {
                    "id": id,
                    "monitorName": monitorName,
                    "userId": userId,
                    "recordEnable": recordEnable == '0' ? 1 : 0,
                }, function (data) {
                    layer.closeAll();
                    if (data.success) {
                        myTable.refresh();
                    } else {
                        layer.msg('修改录音状态失败');
                    }
                });
            });

        },
        //全选
        checkAll: function () {
            $("input[name='subChk']").prop("checked", this.checked);
        },
        //单选
        subChkClick: function () {
            $("#checkAll").prop("checked", subChk.length == subChk.filter(":checked").length ? true : false);
        },
        //批量移除对讲对象
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
            myTable.deleteItems({
                'intercomInfoIds': checkedList.toString()
            });
        },
        //刷新列表
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            myTable.requestData();
        },
        /**
         * 左侧组织架构树
         * */
        groupListTree: function () {
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
                    dataFilter: intercomObject.groupAjaxDataFilter
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
                    onClick: intercomObject.zTreeOnClick
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
                selectTreeId = '';
            } else {
                selectTreepId = '';
                selectTreeId = treeNode.id;
            }
            selectTreeType = treeNode.type;
            myTable.requestData();
        },
    };
    $(function () {
        $('input').inputClear();
        //初始化
        intercomObject.init();
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'search_condition') {
                search_ztree('treeDemo', id, 'assignment');
            }
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
        // 组织架构模糊搜索
        $("#search_condition").on("input oninput", function () {
            search_ztree('treeDemo', 'search_condition', 'assignment');
        });

        //全选
        $("#checkAll").bind("click", intercomObject.checkAll);
        //单选
        subChk.bind("click", intercomObject.subChkClick);
        //批量生成
        $("#generateId").bind("click", intercomObject.delMooreIntercomInfo);
        //批量删除
        $("#del_model").bind("click", intercomObject.delModelClick);
        //刷新
        $("#refreshTable").on("click", intercomObject.refreshTable);
    })
})(window, $);