(function (window, $) {
    //显示隐藏列
    var menu_text = "";
    var table = $("#dataTable tr th:gt(1)");
    //单选
    var subChk = $("input[name='subChk']");
    obdVehicleType = {
        init: function () {
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            ;
            $("#Ul-menu-text").html(menu_text);
            obdVehicleType.initTable();
        },
        initTable: function () {
            //表格列定义
            var columnDefs = [{
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];

            var columns = [
                {
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
                        //删除按钮
                        result += '<button type="button" onclick="myTable.deleteItem(\''
                            + row.id
                            + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
                        return result;
                    }
                }, {
                    "data": "type",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == 0) {
                            return '乘用车';
                        } else {
                            return "商用车";
                        }
                    }
                }, {
                    "data": "name",
                    "class": "text-center",
                }, {
                    "data": "code",
                    "class": "text-center",
                }, {
                    "data": "description",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }

                }];

            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
            };

            var setting = {
                listUrl: '/clbs/v/obdManager/obdVehicleType/list',
                editUrl: '/clbs/v/obdManager/obdVehicleType/edit_',
                deleteUrl: '/clbs/v/obdManager/obdVehicleType/delete_',
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
        },
        //全选
        cleckAll: function () {
            $("input[name='subChk']").prop("checked", this.checked);
        },
        //单选
        subChkClick: function () {
            $("#checkAll").prop("checked", subChk.length == subChk.filter(":checked").length ? true : false);
        },
        //批量删除
        delModel: function () {
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
            myTable.deleteItem(checkedList.toString());
        },
        //加载完成后执行
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            myTable.requestData();
        },

        search: function () {
            $("#query").val($("#simpleQueryParam").val());
            myTable.requestData();
        },

        exportx: function () {
            var url = "/clbs/v/obdManager/obdVehicleType/export?query=" + $("#query").val();
            window.location.href = url;
        }

    }

    $(function () {
        $('input').inputClear();
        obdVehicleType.init();
        $("#search_button").bind("click", obdVehicleType.search);
        //导出  带有模糊查询
        $("#exportId").bind("click", obdVehicleType.exportx);
        //全选
        $("#checkAll").bind("click", obdVehicleType.cleckAll);
        //单选
        subChk.bind("click", obdVehicleType.subChkClick);
        //批量删除
        $("#del_model").bind("click", obdVehicleType.delModel);
        //加载完成后执行
        $("#refreshTable").on("click", obdVehicleType.refreshTable);
    })
})(window, $)