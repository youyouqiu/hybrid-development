(function ($, window) {
    var selectTreeId = '';
    var zNodes = null;
    var log, className = "dark";
    var newCount = 1;
    var columnDefs;
    var columns;
    var setting;
    var treeSetting;
    var idStr;
    var OperationId;
    var selectTreeIdAdd = "";
    var startOperation;// 点击运营资质类别的修改按钮时，弹出界面时运营资质类别文本的内容
    var expliant;// 点击运营资质类别的修改按钮时，弹出界面时说明文本的内容
    var vagueSearchlast = $("#operationType").val();

    window.checkUserObj = {// 存放勾选的用户信息
        id: [],
        name: [],
    };
    groupUserManage = {
        init: function () {
            // 显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(1)");
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            ;
            $("#Ul-menu-text").html(menu_text);
            // 表格列定义
            columnDefs = [{
                // 第一列，用来显示序号
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];
            columns = [
                {
                    // 第一列，用来显示序号
                    "data": null,
                    "class": "text-center"
                },
                {
                    "data": null,
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        var userId = $("#currentUserId").val();
                        var idStr = row.id;
                        // var arrayObj = row.id.all;
                        // arrayObj.reverse();
                        // var idStr = arrayObj.join(",");
                        if (idStr != userId) {
                            var result = '';
                            result += '<input  type="checkbox" name="subChk" data-monitorName="' + row.username + '" value="' + idStr + '" />';
                            return result;
                        } else {
                            var result = '';
                            result += '<input  type="checkbox" name="subChk" disabled/>';
                            return result;
                        }
                    }
                },
                {
                    "data": null,
                    "class": "text-center", // 最后一列，操作按钮
                    render: function (data, type, row, meta) {
                        var idStr = row.id;
                        // var arrayObj = row.id.all;
                        // var idStr = arrayObj.join(",");
                        var editUrlPath = myTable.editUrl + idStr + ".gsp"; // 修改地址
                        var roleUrlPre = '/clbs/c/user/roleList_{id}.gsp';
                        var VehicleUrlPre = '/clbs/c/user/vehiclePer_{id}.gsp';
                        var result = '';
                        var userId = $("#currentUserId").val();
                        var userId2 = $("#currentUserId").attr("value");
                        // 修改按钮
                        result += '<button href="' + editUrlPath + '" data-target="#commonLgWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                        if (idStr != userId) {
                            // 分配角色
                            result += '<button href="'
                                + roleUrlPre.replace("{id}", idStr)
                                + '" data-target="#commonSmWin" class="editBtn editBtn-info" type="button" data-toggle="modal"><i class="fa fa-edit"></i>分配角色</button>&ensp;'
                            // 授权
                            result += '<button href="'
                                + VehicleUrlPre.replace("{id}", idStr)
                                + '" data-target="#commonSmWin" class="editBtn editBtn-info" type="button" data-toggle="modal"><i class="fa faEdit"></i>分配分组</button>&ensp;'
                            // 删除按钮
                            result += '<button type="button" onclick="groupUserManage.deleteRole(\'' + idStr + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
                        } else {
                            // 分配角色
                            result += '<button disabled href="'
                                + roleUrlPre.replace("{id}", idStr)
                                + '" data-target="#commonSmWin" class="editBtn btn-default deleteButton" type="button" data-toggle="modal"><i class="fa fa-ban"></i>分配角色</button>&ensp;'
                            // 授权
                            result += '<button disabled href="'
                                + VehicleUrlPre.replace("{id}", idStr)
                                + '" data-target="#commonSmWin" class="editBtn btn-default deleteButton" type="button" data-toggle="modal"><i class="fa fa-ban"></i>分配分组</button>&ensp;'
                            // 删除按钮
                            result += '<button disabled type="button" onclick="groupUserManage.deleteRole(\'' + idStr + '\')" class="btn-default editBtn deleteButton"><i class="fa fa-ban"></i>删除</button>';
                        }
                        return result;
                    }
                },
                {
                    "data": "username",
                    "class": "text-center"
                },
                {
                    "data": "fullName",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == "null" || data == null || data == undefined) {
                            data = "";
                        }
                        return data;
                    }
                }, {
                    "data": "identityNumber",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == "null" || data == null || data == undefined) {
                            data = "";
                        }
                        return data;
                    }
                },
                {
                    "data": "gender",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == "1") {
                            return '男';
                        } else if (data == "2") {
                            return '女';
                        } else {
                            return '';
                        }
                    }
                },
                {
                    "data": "mobile",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == "null" || data == null || data == undefined) {
                            data = "";
                        }
                        return data;
                    }
                },
                {
                    "data": "mail",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == "null" || data == null || data == undefined) {
                            data = "";
                        }
                        return data;
                    }
                }, {
                    "data": "identity",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == "null" || data == null || data == undefined) {
                            data = "";
                        }
                        return data;
                    }
                }, {
                    "data": "industryName",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == "null" || data == null || data == undefined) {
                            data = "";
                        }
                        return data;
                    }
                }, {
                    "data": "duty",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == "null" || data == null || data == undefined) {
                            data = "";
                        }
                        return data;
                    }
                }, {
                    "data": "administrativeOffice",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == "null" || data == null || data == undefined) {
                            data = "";
                        }
                        return data;
                    }
                },
                {
                    "data": "state",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == "1") {
                            return '启用';
                        } else if (data == "0") {
                            return '停用';
                        } else {
                            return '';
                        }
                    }
                },
                {
                    "data": "authorizationDate",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == "null" || data == null || data == undefined) {
                            data = "";
                        }
                        return data;
                    }
                },
                {
                    "data": "groupName",
                    "class": "text-center"
                }, {
                    "data": "socialSecurityNumber",
                    "class": "text-center"
                },
                {
                    "data": "roleName",
                    "class": "text-center"
                },
            ];
            // 表格setting
            setting = {
                listUrl: "/clbs/c/user/list",
                editUrl: "/clbs/c/user/edit_",
                deleteUrl: "/clbs/c/user/delete_",
                deletemoreUrl: "/clbs/c/user/deletemore",
                enableUrl: "/clbs/c/user/enable_",
                disableUrl: "/clbs/c/user/disable_",
                columnDefs: columnDefs, // 表格列定义
                columns: columns, // 表格列
                dataTableDiv: 'dataTable', // 表格
                ajaxDataParamFun: groupUserManage.ajaxDataParamFun, // ajax参数
                pageable: true, // 是否分页
                showIndexColumn: true, // 是否显示第一列的索引列
                enabledChange: true
            };
            // 创建表格
            myTable = new TG_Tabel.createNew(setting);
            // 表格初始化
            myTable.init();
        },
        userTree: function () {
            // 初始化文件树
            treeSetting = {
                async: {
                    url: "/clbs/m/basicinfo/enterprise/professionals/tree",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {  // 是否可选 Organization
                        "isOrg": "1"
                    },
                    dataFilter: groupUserManage.ajaxDataFilter
                },
                view: {
                    addHoverDom: groupUserManage.addHoverDom,
                    removeHoverDom: groupUserManage.removeHoverDom,
                    selectedMulti: false,
                    nameIsHTML: true,
                    fontCss: setFontCss_ztree
                },
                edit: {
                    enable: true,
                    editNameSelectAll: true,
                    showRemoveBtn: groupUserManage.showRemoveBtn,
                    showRenameBtn: false,
                    removeTitle: '删除'
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    beforeDrag: groupUserManage.beforeDrag,
                    beforeEditName: groupUserManage.beforeEditName,
                    beforeRemove: groupUserManage.beforeRemove,
                    beforeRename: groupUserManage.beforeRename,
                    onRemove: groupUserManage.onRemove,
                    onRename: groupUserManage.onRename,
                    onClick: groupUserManage.zTreeOnClick
                }
            };
            $.fn.zTree.init($("#treeDemo"), treeSetting, zNodes);
            var treeObj = $.fn.zTree.getZTreeObj('treeDemo');
            treeObj.expandAll(true);


        },
        beforeDrag: function (treeId, treeNodes) {
            return false;
        },
        beforeEditName: function (treeId, treeNode) {
            className = (className === "dark" ? "" : "dark");
            groupUserManage.showLog("[ " + groupUserManage.getTime() + " beforeEditName ]&ensp;&ensp;&ensp;&ensp; "
                + treeNode.name);
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.selectNode(treeNode);
            return tg_confirmDialog(null, userGroupDeleteConfirm);
        },
        // 组织树预处理函数
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (responseData) {
                for (var i = 0; i < responseData.length; i++) {
                    responseData[i].open = true;
                }
            }
            return responseData;
        },
        beforeRemove: function (treeId, treeNode) {
            className = (className === "dark" ? "" : "dark");
            groupUserManage.showLog("[ " + groupUserManage.getTime() + " beforeRemove ]&ensp;&ensp;&ensp;&ensp; "
                + treeNode.name);
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.selectNode(treeNode, false, true);
            var result;
            layer.confirm(userGroupDeleteConfirm, {
                title: '操作确认',
                icon: 3, // 问号图标
                btn: ['确认', '取消'] // 按钮
            }, function (index) {
                layer.close(index, {move: false});
                selectTreeIdAdd = "";
                var nodes = zTree.getSelectedNodes();
                var preNode = nodes[0].getPreNode();
                var nextNode = nodes[0].getNextNode();
                var parentNode = nodes[0].getParentNode();
                $.ajax({
                    type: 'POST',
                    url: '/clbs/c/group/delete',
                    data: {"id": treeNode.id},
                    async: true,
                    dataType: 'json',
                    success: function (data) {
                        var flag = data.success;
                        if (flag == false && data.msg) {
                            layer.msg(data.msg, {move: false});
                        }
                        if (flag == true) {
                            $('#treeDemo').html(
                                '<p style="text-align: center;">' +
                                '<i class="fa fa-spinner loading-state" style="margin-right: 5px;"></i>' +
                                '<span>加载中，请稍候</span>' +
                                '</p>'
                            );
                            $('#simpleQueryParam').val("");
                            selectTreeId = "";
                            $.ajax({
                                type: 'POST',
                                url: '/clbs/m/basicinfo/enterprise/professionals/tree',
                                data: {"isOrg": "1"},
                                async: true,
                                dataType: 'json',
                                success: function (data) {
                                    var addData = JSON.parse(data);
                                    var nodeName;
                                    if (preNode != null) {
                                        nodeName = preNode.name;
                                    } else if (nextNode != null) {
                                        nodeName = nextNode.name;
                                    } else {
                                        nodeName = parentNode.name;
                                    }
                                    ;
                                    $.fn.zTree.init($("#treeDemo"), treeSetting, addData);
                                    search_ztree('treeDemo', 'search_condition', 'group');

                                    var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                                    var nodes = treeObj.getNodes();
                                    for (var j = 0; j < nodes.length; j++) {
                                        zTree.expandNode(nodes[j], true, true, true);
                                    }
                                },
                            });
                        }
                        layer.close(index, {move: false});
                    },
                    error: function () {
                        layer.msg(systemError, {move: false});
                    }
                });
            }, function (index) {
                layer.close(index, {move: false});
            });
            return false;
        },
        onRemove: function (e, treeId, treeNode) {
            selectTreeIdAdd = "";
            groupUserManage.showLog("[ " + groupUserManage.getTime() + " onRemove ]&ensp;&ensp;&ensp;&ensp; "
                + treeNode.name);
        },
        beforeRename: function (treeId, treeNode, newName, isCancel) {
            className = (className === "dark" ? "" : "dark");
            groupUserManage.showLog((isCancel ? "<span style='color:red'>" : "") + "[ " + groupUserManage.getTime()
                + " beforeRename ]&ensp;&ensp;&ensp;&ensp; " + treeNode.name
                + (isCancel ? "</span>" : ""));
            if (newName.length == 0) {
                layer.msg(userNodeNameNull, {move: false});
                var zTree = $.fn.zTree.getZTreeObj("treeDemo");
                setTimeout(function () {
                    zTree.editName(treeNode)
                }, 10);
                return false;
            }
            return true;
        },
        onRename: function (e, treeId, treeNode, isCancel) {
            groupUserManage.showLog((isCancel ? "<span style='color:red'>" : "") + "[ " + groupUserManage.getTime()
                + " onRename ]&ensp;&ensp;&ensp;&ensp; " + treeNode.name
                + (isCancel ? "</span>" : ""));
        },
        showRemoveBtn: function (treeId, treeNode) {
            //return treeNode.children==undefined;
            return true;
        },
        showRenameBtn: function (treeId, treeNode) {
            return !treeNode.isLastNode;
        },
        showLog: function (str) {
            if (!log)
                log = $("#log");
            log.append("<li class='" + className + "'>" + str + "</li>");
            if (log.children("li").length > 8) {
                log.get(0).removeChild(log.children("li")[0]);
            }
        },
        getTime: function () {
            var now = new Date(), h = now.getHours(), m = now.getMinutes(), s = now
                .getSeconds(), ms = now.getMilliseconds();
            return (h + ":" + m + ":" + s + " " + ms);
        },
        addOrgCallback: function (treeId) {
            var oldData;
            $.ajax({
                url: '/clbs/m/basicinfo/enterprise/professionals/tree',
                type: 'POST',
                data: {"isOrg": "1"},
                async: false,
                dataType: 'json',
                success: function (data) {
                    var addData = JSON.parse(data);
                    oldData = addData.length;
                },
            });
            var windowId = 'commonWin';
            $("#" + windowId).on("hidden.bs.modal", function (data) {
                $(this).removeData("bs.modal");
                $.ajax({
                    url: '/clbs/m/basicinfo/enterprise/professionals/tree',
                    type: 'POST',
                    data: {"isOrg": "1"},
                    async: false,
                    dataType: 'json',
                    success: function (data) {
                        var addData = JSON.parse(data);
                        if (addData.length !== oldData) {
                            $.fn.zTree.init($("#treeDemo"), treeSetting, addData);
                            var treeObj = $.fn.zTree.getZTreeObj(treeId);
                            treeObj.expandAll(true);
                        }
                    },
                    error: function () {
                        layer.msg(systemError, {move: false});
                    }
                });
            });
            return true;
        },
        addHoverDom: function (treeId, treeNode) {
            if (!treeNode.parentTId && $("#editBtn_treeDemo_1").length > 0) return;
            var nodeSpanObj = $("#" + treeNode.tId + "_span"),
                addBtnObj = $("#addBtn_" + treeNode.tId),
                editBntObj = $('#editBtn_' + +treeNode.tId),
                sObj = nodeSpanObj,
                sEdit = nodeSpanObj,
                sDetails = nodeSpanObj,
                sInsert = nodeSpanObj;
            if (treeNode.editNameFlag || addBtnObj.length > 0 || editBntObj.length > 0)
                return;
            var id = (100 + newCount);
            var pid = treeNode.id;
            pid = window.encodeURI(window.encodeURI(pid));
            if (treeNode.id !== 'ou=organization') {
                var addStr = "<span class='button add' id='addBtn_"
                    + treeNode.tId
                    + "' title='新增' href='/clbs/c/group/add.gsp?id="
                    + id
                    + "&pid="
                    + pid
                    + "' data-target='#commonWin' data-toggle='modal'></span>";
            }
            var editStr = "<span class='button edit' id='editBtn_"
                + treeNode.tId
                + "' title='修改' href='/clbs/c/group/edit.gsp?pid="
                + pid
                + "' data-target='#commonWin' data-toggle='modal'></span>";
            var detailsStr = "<span class='button details' id='detailsBtn_"
                + treeNode.tId
                + "' title='详情'  href='/clbs/c/group/detail.gsp?pid="
                + pid
                + "' data-target='#commonWin' data-toggle='modal'</span>";
            var insertStr = "<span class='button insert' id='insertBtn_"
                + treeNode.tId
                + "' title='插入'  href='/clbs/c/group/insert.gsp?pid="
                + pid
                + "' data-target='#commonWin' data-toggle='modal'</span>";

            var importStr = "<span class='button import' pid='" + treeNode.id + "' id='importBtn_"
                + treeNode.tId
                + "' title='导入' data-target='#import_modal' data-toggle='modal'   </span>";
            nodeSpanObj.after(importStr);
            nodeSpanObj.after(insertStr);
            nodeSpanObj.after(detailsStr);
            nodeSpanObj.after(editStr);
            nodeSpanObj.after(addStr);

            $("#treeDemo .import").click(function (e) {
                // e.stopPropagation()
                const pid = $(this).attr('pid');
                $("#import_pid").val(pid);
                return true
            })

            /* var btn = addBtnObj;
             if (btn)
                 btn.bind("click", function () {
                     groupUserManage.addOrgCallback(treeId); 
                 });
             var insertBtnObj = $("#insertBtn_" + treeNode.tId);
             if (insertBtnObj) {
                 insertBtnObj.bind("click", function () {
                     groupUserManage.addOrgCallback(treeId);
                 });
             }
             var editBtn = $("#editBtn_" + treeNode.tId);
             if (editBtn)
                 editBtn.bind("click", function () {
                     var windowId = 'commonWin';
                     $("#" + windowId).on("hidden.bs.modal", function (data) {
                         var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                         var nodes = treeObj.getSelectedNodes();
                         $(this).removeData("bs.modal");
                         $.ajax({
                             url: '/clbs/m/basicinfo/enterprise/professionals/tree',
                             type: 'POST',
                             data: {"isOrg": "1"},
                             async: false,
                             dataType: 'json',
                             success: function (data) {
                                 var addData = JSON.parse(data);
                                 $.fn.zTree.init($("#treeDemo"), treeSetting, addData);
                                 var treeObjNew = $.fn.zTree.getZTreeObj("treeDemo");
                                 if (nodes != null && nodes.length > 0) {
                                     var treenode = treeObjNew.getNodeByParam("id", nodes[0].id, null);
                                     //treeObj.expandAll(true);
                                     /!* treeObj.expandNode(treenode, false, false, false);
                                      treeObj.selectNode(treenode);*!/
                                 }
                                 treeObj.expandAll(true);
                             },
                             error: function () {
                                 layer.msg(systemError, {move: false});
                             }
                         });
                     });
                     return true;
                 });*/
            //var detBtn = $("#detailsBtn_" + treeNode.tId);
        },
        // 导入文件框的初始化
        importFileModalInit: function () {
            $("#closebutton").click(function () {
                $("#import_modal").modal('hide');// 关闭弹框
                $(".progress-bar").attr("aria-valuenow", 0).css("width", "0%");
                $("#excelPath").val('');
                $("#fileNameShow").html('');

            });
            $("#close").click(function () {
                $("#import_modal").modal('hide');// 关闭弹框
                $(".progress-bar").attr("aria-valuenow", 0).css("width", "0%");
                $("#excelPath").val('');
                $("#fileNameShow").html('');
            });

            $("#submitImport").click(function (e) {
                msgImport.doSubmits('importForm', $("#submitImport"), groupUserManage.userTree);
            })

            return
            let file = null;
            // importFileName
            $("#fileImport").click(function () {
                return
                $("#import_fileInput").click();
                // console.log('点击上传')
            })
            $("#import_fileInput").change(function (e) {
                file = e.target.files[0];
                $("#importFileName").text(file.name);
            })
            $("#submitImport").click(function (e) {
                var accept = ['xls', 'xlsx'];
                var nameType = file.name.split(',').slice(-1)[0];
                var isPass = false;
                for (let i = 0; i < accept.length; i++) {
                    if (accept[i] == nameType) {
                        isPass = true;
                        break
                    }
                }
                if (!isPass) {
                    const pid = $("#import_pid").val();
                    let data = new FormData();
                    data.append('file', file);
                    data.append('pid', pid)
                    json_ajax("POST", '/clbs/c/group/import', "json", true, data, function (res) {
                        // console.log(res)
                        if (res.success) {
                            layer.msg('导入成功')
                        } else {
                            layer.msg(res.msg)
                        }
                    }, 1000 * 60 * 10, 'multipart/form-data');
                } else {
                    layer.msg('文件类型不正确，请选择' + accept.join(',') + '格式的文件')
                }

            })

        },

        removeHoverDom: function (treeId, treeNode) {
            $("#addBtn_" + treeNode.tId).unbind().remove();
            $("#editBtn_" + treeNode.tId).unbind().remove();
            $("#detailsBtn_" + treeNode.tId).unbind().remove();
            $("#insertBtn_" + treeNode.tId).unbind().remove();
            $("#importBtn_" + treeNode.tId).unbind().remove();
        },
        selectAll: function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.treeSetting.edit.editNameSelectAll = $("#selectAll").attr("checked");
        },
        //点击节点
        zTreeOnClick: function (event, treeId, treeNode) {
            selectTreeId = treeNode.id;
            selectTreeIdAdd = treeNode.uuid;
            $('#simpleQueryParam').val("");
            myTable.requestData();
            var orgElement = document.getElementById(treeNode.tId + '_a');
            var offsetLeft = orgElement.offsetLeft + orgElement.offsetWidth + 18; //显示层级的虚线元素宽度为18
            var treeObj = document.getElementById(treeId);
            treeObj.scrollLeft += offsetLeft - treeObj.offsetWidth - treeObj.scrollLeft;
        },
        // ajax参数
        ajaxDataParamFun: function (d) {
            d.simpleQueryParam = $('#simpleQueryParam').val(); // 模糊查询
            d.groupName = selectTreeId;
        },
        // 删除用户
        deleteRole: function (id) {
            if (id == "uid=admin,ou=organization") {
                layer.msg(userSupermanagerDeleteTip, {move: false});
            } else {
                myTable.deleteItem(id);
            }
        },
        // 查询全部
        refreshTable: function () {
            selectTreeId = "";
            $('#simpleQueryParam').val("");
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.selectNode("");
            zTree.cancelSelectedNode();
            myTable.requestData();
        },
        // 批量删除
        delModel: function () {
            // 判断是否至少选择一项
            var chechedNum = $("input[name='subChk']:checked").length;
            if (chechedNum == 0) {
                layer.msg(userDeleteChooseNull, {move: false});
                return
            }
            var checkedList = new Array();
            var flag = true;
            $("input[name='subChk']:checked").each(function () {
                if ($(this).val() == "uid=admin,ou=organization") {
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
                layer.msg(userSupermanagerDeleteTip, {move: false});
            }
        },
        findOperation: function () {
            var vagueSearch = $("#operationType").val();
            var url = "/clbs/c/group/findOperations";
            var data = {"type": vagueSearch};
            json_ajax("POST", url, "json", true, data, groupUserManage.findCallback);
        },
        findCallback: function (data) {
            if (data.success) {
                var operations = [];
                if (data.obj.operation != null || data.obj.operation.length > 0) {
                    var calldata = data.obj.operation;
                    var s = 0;
                    for (var i = 0; i < calldata.length; i++) {
                        var list = [
                            ++s,
                            '<input type="checkbox" id="checkAllTwo" name="subChkTwo" value="' + calldata[i].id + '">',
                            '<button onclick="groupUserManage.findOperationById(\'' + calldata[i].id + '\')" data-target="#updateType" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;<button type="button"  onclick="groupUserManage.deleteType(\'' + calldata[i].id + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>',
                            calldata[i].operationType,
                            calldata[i].explains
                        ];
                        operations.push(list);
                    }
                }
                reloadData(operations);
            } else {
                layer.msg(data.msg);
            }
        },
        getTable: function (table, operations) {
            myTableTwo = $(table).DataTable({
                "destroy": true,
                "dom": 'tiprl',// 自定义显示项
                "data": operations,
                "lengthChange": true,// 是否允许用户自定义显示数量
                "bPaginate": true, // 翻页功能
                "bFilter": false, // 列筛序功能
                "searching": true,// 本地搜索
                "ordering": false, // 排序功能
                "Info": true,// 页脚信息
                // "autoWidth": true,// 自动宽度
                "stripeClasses": [],
                "pageLength": 10,
                "lengthMenu": [5, 10, 20, 50, 100, 200],
                "pagingType": "simple_numbers", // 分页样式
                "dom": "t" + "<'row'<'col-md-2 col-sm-12 col-xs-12 noPadding'l><'col-md-4 col-sm-12 col-xs-12 noPadding'i><'col-md-6 col-sm-12 col-xs-12 noPadding'p>>",
                "oLanguage": {// 国际语言转化
                    "oAria": {
                        "sSortAscending": " - click/return to sort ascending",
                        "sSortDescending": " - click/return to sort descending"
                    },
                    "sLengthMenu": "显示 _MENU_ 记录",
                    "sInfo": "当前显示 _START_ 到 _END_ 条，共 _TOTAL_ 条记录。",
                    "sZeroRecords": "我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？",
                    "sEmptyTable": "我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？",
                    "sLoadingRecords": "正在加载数据-请等待...",
                    "sInfoEmpty": "当前显示0到0条，共0条记录",
                    "sInfoFiltered": "（数据库中共为 _MAX_ 条记录）",
                    "sProcessing": "<img src='../resources/user_share/row_details/select2-spinner.gif'/> 正在加载数据...",
                    "sSearch": "模糊查询：",
                    "sUrl": "",
                    "oPaginate": {
                        "sFirst": "首页",
                        "sPrevious": " 上一页 ",
                        "sNext": " 下一页 ",
                        "sLast": " 尾页 "
                    },
                    "columnDefs": [
                        {'width': "40%", "targets": 0},
                        {'width': "30%", "targets": 1},
                        {'width': "30%", "targets": 2},
                    ],
                },
                "order": [
                    [0, null]
                ],// 第一列排序图标改为默认

            });
        },
        doSubmit: function () {
            if (groupUserManage.validates()) {
                addHashCode($("#eadOperation"));
                $("#eadOperation").ajaxSubmit(function (data) {
                    if (data != null && typeof (data) == "object" &&
                        Object.prototype.toString.call(data).toLowerCase() == "[object object]" &&
                        !data.length) {//判断data是字符串还是json对象,如果是json对象
                        if (data.success == true) {
                            $("#addType").modal("hide");//关闭窗口
                            layer.msg(publicAddSuccess, {move: false});
                            groupUserManage.closeClean();//清空文本框
                            $("#operationType").val("");
                            groupUserManage.findOperation();
                        } else {
                            layer.msg(data.msg, {move: false});
                        }
                    } else {//如果data不是json对象
                        var result = $.parseJSON(data);//转成json对象
                        if (result.success == true) {
                            $("#addType").modal("hide");//关闭窗口
                            layer.msg(publicAddSuccess, {move: false});
                            $("#operationType").val("");
                            groupUserManage.closeClean();//清空文本框
                            groupUserManage.findOperation();
                        } else {
                            layer.msg(result.msg, {move: false});
                        }
                    }
                });
            }
        },
        updateDoSubmit: function () {
            groupUserManage.init();
            if (groupUserManage.upDateValidates()) {
                var operationType = $("#updateOperationType").val();// 运营资质类型
                var explains = $("#updateDescription").val();// 说明
                var resubmitToken = operationType + explains;
                resubmitToken = resubmitToken.split("").reduce(function (a, b) {
                    a = ((a << 5) - a) + b.charCodeAt(0);
                    return a & a
                }, 0);
                var data = {"id": OperationId, "operationType": operationType, "explains": explains, resubmitToken};
                var url = "/clbs/c/group/updateOperation";
                json_ajax("POST", url, "json", true, data, groupUserManage.updateCallback);
            }
        },
        closeClean: function () {
            $("#addproperationtype").val("");
            $("#adddescription").val("");
            $("#addproperationtype-error").hide();//隐藏上次新增时未清除的validate样式
            $("#adddescription-error").hide();
        },
        updateClean: function () {
            $("#updateOperationType-error").hide();
            $("#updateDescription-error").hide();
        },
        findOperationById: function (id) {
            OperationId = id;
            var data = {"id": OperationId};
            var url = "/clbs/c/group/findOperationById";
            json_ajax("POST", url, "json", true, data, groupUserManage.findByIdback);
        },
        findByIdback: function (data) {
            if (data.success == true) {
                $("#updateOperationType").val(data.obj.operation.operationType);
                $("#updateDescription").val(data.obj.operation.explains);
                startOperation = $("#updateOperationType").val();
                expliant = $("#updateDescription").val();
            } else {
                layer.msg(data.msg, {move: false});
            }
        },
        updateCallback: function (data) {
            if (data.success == true) {
                $("#updateType").modal('hide');
                layer.msg("修改成功");
                groupUserManage.findOperation();
            } else {
                layer.msg(data.msg, {move: false});
            }
        },
        deleteType: function (id) {
            layer.confirm(publicDelete, {
                title: '操作确认',
                icon: 3, // 问号图标
                btn: ['确定', '取消'] // 按钮
            }, function () {
                var url = "/clbs/c/group/deleteOperation";
                var data = {"id": id}
                json_ajax("POST", url, "json", false, data, groupUserManage.deleteCallback);
            });
        },
        deleteCallback: function (data) {
            if (data.success == true) {
                layer.closeAll('dialog');
                groupUserManage.findOperation();
            } else {
                layer.msg(publicError, {move: false});
            }
        },
        deleteTypeMore: function () {
            // 判断是否至少选择一项
            var chechedNum = $("input[name='subChkTwo']:checked").length;
            if (chechedNum == 0) {
                layer.msg(selectItem);
                return
            }
            var ids = "";
            $("input[name='subChkTwo']:checked").each(function () {
                ids += ($(this).val()) + ",";
            });
            var url = "/clbs/c/group/deleteOperationMore";
            var data = {"ids": ids};
            layer.confirm(publicDelete, {
                title: '操作确认',
                icon: 3, // 问号图标
                btn: ['确定', '取消'] // 按钮
            }, function () {
                json_ajax("POST", url, "json", false, data, groupUserManage.deleteOperationMoreCallback);
                layer.closeAll('dialog');
            });
        },
        deleteOperationMoreCallback: function (data) {
            if (data.success) {
                layer.msg(publicDeleteSuccess);
                groupUserManage.findOperation();
            } else {
                layer.msg(data.msg, {move: false});
            }
        },
        findOperationByVague: function () {
            groupUserManage.findOperation();
        },
        findDownKey: function (event) {
            if (event.keyCode == 13) {
                groupUserManage.findOperation();
            }
        },
        checkAll: function (e) {
            var userName = $('#userName').text();
            var checkedState = e.checked;
            $("input[name='subChk']").each(function () {
                var text = $(this).parents('tr').children('td:nth-child(4)').text();
                if (userName != text) {
                    $(this).prop("checked", checkedState);
                }
            });
        },
        checkAllTwo: function (e) {
            $("input[name='subChkTwo']").prop("checked", e.checked);
        },
        addId: function () {
            $("#addId").attr("href", "/clbs/c/user/newuser?uuid=" + selectTreeIdAdd + "");
        },
        validates: function () {//增加运营资质类别时的数据验证
            return $("#eadOperation").validate({
                rules: {
                    addproperationtype: {
                        required: true,
                        stringCheck: true,
                        maxlength: 20,
                        minlength: 2,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/c/group/findOperationByoperation",
                            data: {
                                type: function () {
                                    return $("#addproperationtype").val();
                                }
                            },
                        }
                    },
                    adddescription: {
                        stringCheck: true,
                        maxlength: 30,
                    }
                },
                messages: {
                    addproperationtype: {
                        required: userQualificationNull,
                        stringCheck: publicPerverseData,
                        maxlength: publicSize20,
                        minlength: publicMinSize2Length,
                        remote: userQualificationExists
                    },
                    adddescription: {
                        stringCheck: publicPerverseData,
                        maxlength: publicSize30
                    }
                }
            }).form();
        },
        upDateValidates: function () {//修改运营资质类别时的数据验证
            var operationType = $("#updateOperationType").val();// 运营资质类型
            var explains = $("#updateDescription").val();// 说明
            if (operationType == startOperation && explains == expliant) {
                $("#updateType").modal('hide');
            } else if (operationType == startOperation && explains != expliant) {
                return $("#editOperation").validate({
                    rules: {
                        updateOperationType: {
                            required: true,
                            maxlength: 20,
                            minlength: 2
                        },
                        updateDescription: {
                            stringCheck: true,
                            maxlength: 30,
                        }
                    },
                    messages: {
                        updateOperationType: {
                            required: userQualificationNull,
                            maxlength: publicSize20,
                            minlength: publicMinSize2Length
                        },
                        updateDescription: {
                            stringCheck: publicPerverseData,
                            maxlength: publicSize30
                        }
                    }
                }).form();
            } else {
                return $("#editOperation").validate({
                    rules: {
                        updateOperationType: {
                            required: true,
                            stringCheck: true,
                            maxlength: 20,
                            remote: {
                                type: "post",
                                async: false,
                                url: "/clbs/c/group/findOperationCompare",
                                data: {
                                    type: function () {
                                        return $("#updateOperationType").val();
                                    },
                                    recomposeType: function () {
                                        return startOperation;
                                    }
                                },
                                dataFilter: function (data) {
                                    var resultData = $.parseJSON(data);
                                    if (resultData.success == true) {
                                        return true;
                                    } else {
                                        return false;
                                    }
                                }
                            }
                        },
                        updateDescription: {
                            stringCheck: true,
                            maxlength: 30,
                        }
                    },
                    messages: {
                        updateOperationType: {
                            required: userQualificationNull,
                            stringCheck: publicPerverseData,
                            maxlength: publicSize20,
                            remote: userQualificationExists
                        },
                        updateDescription: {
                            stringCheck: publicPerverseData,
                            maxlength: publicSize30
                        }
                    }
                }).form();
            }
        },
        // 批量修改
        batchEditFun: function () {
            //判断是否至少选择一项
            var chechedItem = $("input[name='subChk']:checked");
            if (chechedItem.length === 0) {
                layer.msg(selectItem, {move: false});
                return
            }
            checkUserObj.id = [];
            checkUserObj.name = [];
            for (var i = 0; i < chechedItem.length; i++) {
                var monitorId = $(chechedItem[i]).val();
                var monitorName = $(chechedItem[i]).attr('data-monitorName');
                checkUserObj.id.push(monitorId);
                checkUserObj.name.push(monitorName);
            }
            $('#batchEditBtn').attr({
                'href': '/clbs/c/user/batchEdit.gsp',
                'data-toggle': 'modal',
                'data-target': '#commonLgWin'
            });
            setTimeout(function () {
                $('#batchEditBtn').attr('href', 'javascript:void(0);');
                $('#batchEditBtn').removeAttr('data-toggle');
                $('#batchEditBtn').removeAttr('data-target');
            }, 300)
        },
    };
    $(function () {
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'search_condition') {
                search_ztree('treeDemo', id, 'group');
            }
            ;
        });
        var myTable;
        groupUserManage.userTree();

        getTable('dataTables');
        groupUserManage.init();
        groupUserManage.importFileModalInit();// 导入文件框初始化
        groupUserManage.findOperation();
        // IE9
        if (navigator.appName == "Microsoft Internet Explorer" && navigator.appVersion.split(";")[1].replace(/[ ]/g, "") == "MSIE9.0") {
            groupUserManage.refreshTable();
            var search;
            $("#search_condition").bind("focus", function () {
                search = setInterval(function () {
                    search_ztree('treeDemo', 'search_condition', 'group');
                }, 500);
            }).bind("blur", function () {
                clearInterval(search);
            });
        }
        // IE9 end
        $("#selectAll").bind("click", groupUserManage.selectAll);
        // 组织架构模糊搜索
        $("#search_condition").on("input oninput", function () {
            search_ztree('treeDemo', 'search_condition', 'group');
        });
        // 查询全部
        $('#refreshTable').on("click", groupUserManage.refreshTable);
        $("input[name='subChkTwo']").click(function () {
            $("#checkAllTwo").prop("checked", subChkTwo.lenght == subChkTwo.filter(":checked").length ? true : false);
        });
        // 全选
        $("input[name='subChk']").click(function () {
            $("#checkAll").prop("checked", subChk.length == subChk.filter(":checked").length ? true : false);
        });
        // 批量修改
        $("#batchEditBtn").on("click", groupUserManage.batchEditFun);
        // 批量删除
        $("#del_model").on("click", groupUserManage.delModel);
        $("#addoperation").on("click", groupUserManage.doSubmit);
        $("#deleteOperation").on("click", groupUserManage.deleteType);
        $("#updateOperation").on("click", groupUserManage.updateDoSubmit);
        $("#del_modelTwo").on("click", groupUserManage.deleteTypeMore);
        $("#search_operation").on("click", groupUserManage.findOperationByVague);
        $("#addId").on("click", groupUserManage.addId);
        $("#closeAddDialog").on("click", groupUserManage.closeClean);
        $("#updateClose").on("click", groupUserManage.updateClean);
    })
})($, window)