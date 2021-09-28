(function ($, window) {
    var selectTreeId = '';
    var subChk = $("input[name='subChk']");
    var assignmentList = {
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
                    tyoe: "post",
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
                        var editUrlPath = myTable.editUrl + row.id + ".gsp"; //修改地址
                        var AssignmentUrlPre = "/clbs/m/basicinfo/enterprise/assignment/assignmentPer_{id}.gsp";
                        var removeVehicleUrlPre = "/clbs/m/basicinfo/enterprise/assignment/vehiclePer_{id}.gsp";
                        var result = '';
                        //修改按钮
                        result += '<button href="' + editUrlPath + '" data-target="#commonLgWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                        // 分配监控人员 
                        result += '<button href="' +
                            AssignmentUrlPre.replace("{id}", row.id) +
                            '" data-target="#commonSmWin" class="editBtn editBtn-info" type="button" data-toggle="modal"><i class="fa fa-edit"></i>分配监控人员</button>&ensp;'
                        // 移除监控对象 
                        result += '<button href="' +
                            removeVehicleUrlPre.replace("{id}", row.id) +
                            '" data-target="#commonWin" class="editBtn editBtn-info" type="button" data-toggle="modal"><i class="fa fa-object"></i>分配监控对象 </button>&ensp;'
                        //删除按钮
                        result += '<button type="button" onclick="myTable.deleteItem(\'' +
                            row.id +
                            '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
                        return result;
                    }
                },
                {
                    "data": "name",
                    "class": "text-center"
                },
                {
                    "data": "groupName",
                    "class": "text-center",
                },
                {
                    "data": "contacts",
                    "class": "text-center",
                },
                {
                    "data": "telephone",
                    "class": "text-center",
                },
                {
                    "data": "description",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }
            ];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
                d.groupId = selectTreeId;
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/m/basicinfo/enterprise/assignment/list',
                editUrl: '/clbs/m/basicinfo/enterprise/assignment/edit_',
                deleteUrl: '/clbs/m/basicinfo/enterprise/assignment/delete_',
                deletemoreUrl: '/clbs/m/basicinfo/enterprise/assignment/deletemore',
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
        //批量删除
        delModelClick: function () {
            //判断是否至少选择一项
            var chechedNum = $("input[name='subChk']:checked").length;
            if (chechedNum == 0) {
                layer.msg(selectItem);
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
            $("#addId").attr("href", "/clbs/m/basicinfo/enterprise/assignment/add?uuid=" + selectTreeId + "");
        },
        exportTable: function () {
            var url = "/clbs/m/basicinfo/enterprise/assignment/export"
            var param = {
                groupId: selectTreeId,
                simpleQueryParam: $('#simpleQueryParam').val()
            };
            exportExcelUseFormGet(url, param)
        }
    }
    $(function () {
        var myTable;
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
        $("#exportId").click(function () {
            assignmentList.exportTable()
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