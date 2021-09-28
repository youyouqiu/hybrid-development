(function ($, window) {
    var $subChk = $("input[name='subChk']");
    var setResource;
    siteManagement = {
        //初始化
        init: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(1)");
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" />" + table[i].innerHTML + "</label></li>"
            };
            $("#Ul-menu-text").html(menu_text);

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
                },
                {
                    "data": null,
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        var idStr = row.id;
                        var result = '';
                        result += '<input  type="checkbox" name="subChk"  value="' + idStr + '" />';
                        return result;
                    }
                },
                {
                    "data": null,
                    "class": "text-center", //最后一列，操作按钮
                    render: function (data, type, row, meta) {
                        var idStr = row.id;
                        var editUrlPath = myTable.editUrl + idStr + '.gsp'; //修改地址
                        var result = '';
                        // 修改按钮
                        result += '<button data-target="#commonWin" href="' + editUrlPath + '" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                        //删除按钮
                        result += '<button type="button" onclick="siteManagement.deleteSite(\'' + idStr + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
                        return result;
                    }
                }, {
                    "data": "name",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "number",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "longitude",
                    "class": "text-center"
                }, {
                    "data": "latitude",
                    "class": "text-center",
                }, {
                    "data": "describe",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }
                , {
                    "data": "remark",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }
            ];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/m/station/manage/list',
                editUrl: '/clbs/m/station/manage/edit_',
                deleteUrl: '/clbs/m/station/manage/delete_',
                deletemoreUrl: '/clbs/m/station/manage/deleteMore',
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
            //操作权限
            setResource = {
                async: {
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json"
                },
                check: {
                    enable: true,
                    chkStyle: "checkbox",
                    chkboxType: {
                        "Y": "s",
                        "N": "s"
                    },
                    radioType: "all"
                },
                view: {
                    dblClickExpand: false
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    beforeClick: siteManagement.beforeClickResource,
                    onCheck: siteManagement.onCheckResource
                }
            }
        },
        // 删除站点
        deleteSite: function (id) {
            layer.confirm("删除就没啦,请谨慎下手！您确定这么做吗？", {btn: ["确定", "取消"], icon: 3, title: "删除确认"}, function (index) {
                var url = "/clbs/m/station/manage/delete_" + id + '.gsp';
                    json_ajax("POST", url, "json", true, null,function (data) {
                        if(data.success) {
                            layer.msg('站点删除成功');
                            myTable.requestData();
                        }else{
                            layer.msg(data.msg);
                        }
                        layer.close(index);
                    })
            });
        },
        subChk: function () {
            $("#checkAll").prop("checked", $subChk.length == $subChk.filter(":checked").length ? true : false);
        },
        //批量删除
        deleteMuch: function () {
            //判断是否至少选择一项
            var chechedNum = $("input[name='subChk']:checked").length;
            if (chechedNum == 0) {
                layer.confirm("至少选择一条数据哟，不然我不知道您想干什么。", {btn: ["关闭"], icon: 3, title: "提示信息"});
                return;
            }
            var checkedList = new Array();
            var flag = true;
            $("input[name='subChk']:checked").each(function () {
                if ($(this).val() == "cn=ROLE_ADMIN,ou=Groups" || $(this).val() == "cn=POWER_USER,ou=Groups") {
                    flag = false;
                    return false;
                } else {
                    checkedList.push($(this).val());
                }
            });

            if (flag) {
                layer.confirm("删除就没啦,请谨慎下手！您确定这么做吗？", {btn: ["确定", "取消"], icon: 3, title: "删除确认"}, function () {
                    layer.closeAll();
                    var url = '/clbs/m/station/manage/deleteMore';
                    var parameter = {
                        'ids': checkedList.join(",")
                    };
                    json_ajax("POST", url, "json", true, parameter,function (data) {
                        if(data.success) {
                            layer.msg(data.msg);
                            myTable.requestData();
                        }
                    });
                });
            }
        },
        beforeClickResource: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("resourceDemo");
            zTree.checkNode(treeNode, !treeNode.checked, null, true);
            return false;
        },
        onCheckResource: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("resourceDemo"),
                nodes = zTree
                .getCheckedNodes(true),
                v = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                v += nodes[i].name + ",";
            }
        },
        //刷新列表
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            myTable.requestData();
        }
    }
    $(function () {
        $('input').inputClear();
        siteManagement.init();
        //全选
        $("#checkAll").click(function () {
            $("input[name='subChk']").prop("checked", this.checked);
        });
        //单选
        $subChk.on("click", siteManagement.subChk);
        $("#del_model").on("click", siteManagement.deleteMuch);
        myTable.add('commonWin', 'permissionForm', null, null);
        $("#refreshTable").on("click", siteManagement.refreshTable);
    })
})($, window)