(function ($, window) {
    var selectTreeId = '';
    var subChk = $("input[name='subChk']");
    assignmentList = {
        //初始化
        init: function () {
            // 显示隐藏列   
            var menu_text = "";
            var table = $("#dataTable tr th:gt(1)");
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" />" + table[i].innerHTML + "</label></li>"
            };
            $("#Ul-menu-text").html(menu_text);
            var treeSetting = {
                async: {
                    url: "/clbs/m/basicinfo/enterprise/professionals/tree",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: { // 是否可选  Organization
                        "isOrg": "1"
                    },
                    dataFilter: assignmentList.ajaxDataFilter
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
                    onClick: assignmentList.zTreeOnClick
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
                        var detailPath = myTable.detailUrl + row.id; //详情地址
                        var result = '';
                        //修改按钮
                        result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                        // 详情 
                        result += '<button href="' + detailPath + '" data-target="#commonWin" class="editBtn editBtn-info" type="button" data-toggle="modal"><i class="fa fa-object"></i>详情 </button>&ensp;'
                        //删除按钮
                        // result += '<button type="button" onclick="assignmentList.deleteLine(\'' +
                        //     row.id +
                        //     '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
                        result += '<button type="button" onclick="assignmentList.deleteLine(\'' +
                            row.id +
                            '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
                        return result;
                    }
                },
                {
                    "data": "orgName",
                    "class": "text-center"
                },
                {
                    "data": "name",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                },
                {
                    "data": "identify",
                    "class": "text-center",
                },
                {
                    "data": "lineType",
                    "class": "text-center",
                    render: function (data) {
                        switch (data) {
                            case 0:
                                data = '干线线路';
                                break;
                            case 1:
                                data = '支线线路';
                                break;
                            case 2:
                                data = '快线线路';
                                break;
                            case 3:
                                data = '高峰线路';
                                break;
                            case 4:
                                data = '夜间线路';
                                break;
                            case 5:
                                data = '快速公共汽车线路';
                                break;
                            case 9:
                                data = '其他';
                                break;
                            default:
                                data = '';
                                break;
                        }
                        return data

                    }
                },
                {
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
                d.orgId = selectTreeId;
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/m/line/manage/list',
                editUrl: '/clbs/m/line/manage/edit_',
                detailUrl: '/clbs/m/line/manage/detail_',
                deleteUrl: '/clbs/m/line/manage/delete_',
                deletemoreUrl: '/clbs/m/line/manage/deleteBatch',
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
            selectTreeId = treeNode.uuid;
            myTable.requestData();
        },
        //单选
        subChk: function () {
            $("#checkAll").prop("checked", subChk.length == subChk.filter(":checked").length ? true : false);
        },
        deleteLine: function (id) {
            var url = "/clbs/m/line/manage/delete_" + id;
            var data = {
                "id": id
            };
            layer.confirm('删掉就没啦，请谨慎下手！您确定这么做吗？', {
                btn: ["确定", "取消"],
                icon: 3,
                title: "删除确认"
            }, function () {
                json_ajax("GET", url, "json", true, data, assignmentList.deleteLineCallBack);
            });
        },
        deleteLineCallBack: function (data) {
            if (data.success) {
                layer.closeAll('dialog');
                if (data.obj) {
                    layer.msg('删除成功');
                } else {
                    layer.msg(data.msg);
                }
                myTable.requestData();
            } else {
                layer.msg(data.msg);
            }
            myTable.requestData();
        },
        //批量删除
        delModelClick: function () {
            //判断是否至少选择一项
            var chechedNum = $("input[name='subChk']:checked").length;
            if (chechedNum == 0) {
                layer.msg('不能因为你长的好看，就可以什么都不选吧，怎么也要选一个吧:)');
                return
            }
            var checkedList = new Array();
            $("input[name='subChk']:checked").each(function () {
                checkedList.push($(this).val());
            });

            var url = "/clbs/m/line/manage/deleteBatch";
            var data = {
                "ids": checkedList.toString()
            };
            layer.confirm('删掉就没啦，请谨慎下手！您确定这么做吗？?', {
                btn: ["确定", "取消"],
                icon: 3,
                title: "删除确认"
            }, function () {
                json_ajax("POST", url, "json", true, data, assignmentList.deleteVehicleCallBack);
            });
            // myTable.deleteItems({
            //     'deltems': checkedList.toString()
            // });
        },
        deleteVehicleCallBack: function (data) {
            if (data.success) {
                layer.closeAll('dialog');
                if (data.obj) {
                    layer.msg('删除成功');
                } else {
                    layer.msg(data.msg);
                }
                myTable.requestData();
            } else {
                layer.msg(data.msg);
                myTable.requestData();
            }

        },
        // 查询全部
        queryAll: function () {
            selectTreeId = "";
            $('#simpleQueryParam').val("");
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.selectNode("");
            zTree.cancelSelectedNode();
            myTable.requestData();
        },
        addId: function () {
            $("#addId").attr("href", "/clbs/m/line/manage/add?uuid=" + selectTreeId + "");
        },
    }
    $(function () {
        assignmentList.init();
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'search_condition') {
                search_ztree('treeDemo', id, 'group');
            };
        });
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
        $("#checkAll").click(function () {
            $("input[name='subChk']").prop("checked", this.checked);
        });
        subChk.on("click", assignmentList.subChk);
        $("#del_model").on("click", assignmentList.delModelClick);
        $('#refreshTable').on("click", assignmentList.queryAll);
        // 组织架构模糊搜索
        $("#search_condition").on("input oninput", function () {
            search_ztree('treeDemo', 'search_condition', 'group');
        });
        $("#addId").on("click", assignmentList.addId);
    })
})($, window)