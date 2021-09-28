(function (window, $) {
    //显示隐藏列
    var menu_text = "";
    var table = $("#dataTable tr th:gt(0)");
    //单选
    var subChk = $("input[name='subChk']");
    job = {
        //初始化
        init: function () {
            menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(1) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
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
            }, /*{
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    var result = '';
                    result += '<input  type="checkbox" name="subChk"  value="' + row.id + '" />';
                    return result;
                }
            },*/
                {
                    "data": null,
                    "class": "text-center", //最后一列，操作按钮
                    render: function (data, type, row, meta) {
                        var editUrlPath = myTable.editUrl + row.id + ".gsp"; //修改地址
                        var result = '';
                        //修改按钮
                        result += '<button href="' + editUrlPath + '" data-target="#commonSmWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&nbsp;';
                        //删除按钮
                        if (row.id === 'default') {
                            result += '<button disabled type="button"  class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>删除</button>&nbsp;';
                        } else {
                            result += '<button type="button" onclick="job.deleteItemAndCheckBond(\'' + row.id + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
                        }
                        return result;
                    }
                }, {
                    "data": "jobName",
                    "class": "text-center"
                }, {
                    "data": "jobIconName",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data === "null" || data == null) {
                            return '';
                        } else {
                            return '<a href="#" onclick="job.viewImage(\'' + data + '\')">查看</a>';
                        }
                    }
                }, {
                    "data": "remark",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }];

            var flag = $('#permission').val();
            if (flag == "false") {
                columns.splice(1,1);
            }
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/talkback/basicinfo/monitoring/job/list',
                editUrl: '/clbs/talkback/basicinfo/monitoring/job/edit_',
                deleteUrl: '/clbs/talkback/basicinfo/monitoring/job/delete_',
                deletemoreUrl: '/clbs/talkback/basicinfo/monitoring/job/deletemore',
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
        // 放大显示图片
        viewImage: function (url) {
            $('#modalImage').attr('src', url);
            $('#imageModal').modal('show');
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
            var url = "/clbs/talkback/basicinfo/monitoring/job/delete";
            var data = {"id": id};
            layer.confirm(publicDelete, {btn: ["确定", "取消"], icon: 3, title: "操作确认"}, function () {
                json_ajax("POST", url, "json", true, data, job.deleteCallBack);
            });
        },
        // 删除操作回调
        deleteCallBack: function (data) {
            if (data.success) {
                layer.closeAll();
                myTable.refresh();
            } else {
                if (data.msg != null) {
                    layer.msg(data.msg);
                    return;
                }
                layer.msg(publicError);
            }
        },
        //刷新列表
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            myTable.requestData();
        }
    };
    $(function () {
        job.init();
        $('input').inputClear();

        //全选
        $("#checkAll").bind("click", job.checkAllClick);
        subChk.bind("click", job.subChkClick);
        $("#refreshTable").on("click", job.refreshTable);
    })
})(window, $)