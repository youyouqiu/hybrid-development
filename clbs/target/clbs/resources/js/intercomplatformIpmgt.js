 (function ($, window) {
    var $subChk = $("input[name='subChk']");
    var setResource;
    platformList = {
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
                        var editUrlPath = myTable.editUrl + idStr; //修改地址
                        var result = '';
                        //修改按钮
                        result += '<button data-target="#commonSmWin" href="' + editUrlPath + '" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                        //删除按钮
                        result += '<button type="button" onclick="platformList.deleteRole(\'' + idStr + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
                        return result;
                    }
                }, {
                    "data": "platformName",
                    "class": "text-center",
                }, {
                    "data": "status",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == 1) {
                            return "开";
                        } else {
                            return "关";
                        }
                    }
                }, {
                    "data": "platformIp",
                    "class": "text-center"
                }, {
                    "data": "platformPort",
                    "class": "text-center",
                },{
                    "data": "description",
                    "class": "text-center",
                }
            ];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/m/intercomplatform/addrconfig/list',
                editUrl: '/clbs/m/intercomplatform/addrconfig/edit_',
                deleteUrl: '/clbs/m/intercomplatform/addrconfig/delete_',
                deletemoreUrl: '/clbs/m/intercomplatform/addrconfig/deletemore',
                enableUrl: '/clbs/c/user/enable_',
                disableUrl: '/clbs/c/user/disable_',
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
                    beforeClick: platformList.beforeClickResource,
                    onCheck: platformList.onCheckResource
                }
            }
        },
        // 删除角色
        deleteRole: function (id) {
            myTable.deleteItem(id);
        },
        subChk: function () {
            $("#checkAll").prop("checked",$subChk.length == $subChk.filter(":checked").length ? true: false);
        },
        //批量删除
        deleteMuch: function () {
            //判断是否至少选择一项
            var chechedNum = $("input[name='subChk']:checked").length;
            if (chechedNum == 0) {
                layer.msg(selectItem, {move: false});
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
                myTable.deleteItems({
                    'deltems': checkedList.join(";")
                });
            } else {
                layer.msg("不能删除普通管理员角色！", {move: false});
            }
        },
        // 显示错误提示信息
        showErrorMsg: function(msg, inputId){
            if ($("#error_label").is(":hidden")) {
                $("#error_label").text(msg);
                $("#error_label").insertAfter($("#" + inputId));
                $("#error_label").show();
            } else {
                $("#error_label").is(":hidden");
            }
        },
        //错误提示信息隐藏
        hideErrorMsg: function(){
            $("#error_label").hide();
        },
        beforeClickResource: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("resourceDemo");
            zTree.checkNode(treeNode, !treeNode.checked, null, true);
            return false;
        },
        onCheckResource: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("resourceDemo"), nodes = zTree
                    .getCheckedNodes(true), v = "";
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
        platformList.init();
        //全选
        $("#checkAll").click(function () {
            $("input[name='subChk']").prop("checked", this.checked);
        });
        //单选
        $subChk.on("click", platformList.subChk);
        $("#del_model").on("click", platformList.deleteMuch);
        myTable.add('commonWin', 'permissionForm', null, null);
        $("#refreshTable").on("click", platformList.refreshTable);
    })
})($, window)