(function (window, $) {
    //显示隐藏列
    // var subTable;
    var menu_text = "";
    var table = $("#dataTable tr th:gt(1)");
    //单选
    var subChk = $("input[name='subChk']");
    var subChkTwo = $("input[name='subChkTwo']");
    var subChkThree = $("input[name='subChkThree']");
    var search2;
    vehicleModelsManagement = {
        //初始化
        init: function () {
            //表格列定义
            var columnDefs = [{
                //禁止某列参与搜索
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];
            var col = {
                sTitle: '序号',
                data: null,
                className: 'text-center whiteSpace',
                render: function (data, type, row, meta) {
                    return meta.row + 1 +
                        meta.settings._iDisplayStart;
                }
            }
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
                        if (row.id != "default" && !row.codeNum) {
                            result += '<input  type="checkbox" name="subChk"  value="' + row.id + '" />';
                        }
                        return result;
                    }
                },
                {
                    "data": null,
                    "class": "text-center", //最后一列，操作按钮
                    render: function (data, type, row, meta) {
                        var editUrlPath = myTable.editUrl + row.id + ".gsp"; //修改地址
                        var result = '';
                        if (row.id != "default" && !row.codeNum) {
                            //修改按钮
                            result += '<button href="' + editUrlPath + '" data-target="#commonSmWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                            //删除按钮
                            result += '<button type="button" onclick="myTable.deleteItem(\'' +
                                row.id +
                                '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
                        } else {
                            // 禁用修改删除按钮
                            result += '<button disabled type="button" class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>修改</button>&ensp;';
                            result += '<button disabled type="button"  class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>删除</button>&ensp;';
                        }
                        return result;
                    }
                }, {
                    "data": "category",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "vehicleType",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "serviceCycle",
                    "class": "text-center",
                }, {
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
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/m/basicinfo/monitoring/vehicle/type/list',
                editUrl: '/clbs/m/basicinfo/monitoring/vehicle/type/edit_',
                deleteUrl: '/clbs/m/basicinfo/monitoring/vehicle/type/delete_',
                deletemoreUrl: '/clbs/m/basicinfo/monitoring/vehicle/type/deletemore',
                enableUrl: '/clbs/c/user/enable_',
                disableUrl: '/clbs/c/user/disable_',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                drawCallbackFun: function () {
                    var api = myTable.dataTable;
                    var startIndex = api.context[0]._iDisplayStart; //获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                    });
                },
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            //表格初始化
            myTable.init();

            menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-text").html(menu_text);
            //显示隐藏列
            $('.toggle-vis').on('change', function (e) {
                var column = myTable.dataTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });


            //车辆子类型表格初始化
            //表格列定义
            var subColumnDefs = [{
                ////禁止某列参与搜索
                "searchable": false,
                "orderable": false,
                "targets": [0, 1, 2, 3, 4, 6, 7, 8]
            }];
            var subColumns = [{
                    //第一列，用来显示序号
                    "data": null,
                    "class": "text-center"
                },
                {
                    "data": null,
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        var result = '';
                        if (row.id != "default") {
                            result += '<input  type="checkbox" name="subChkThree"  value="' + row.id + '" />';
                        }
                        return result;
                    }
                },
                {
                    "data": null,
                    "class": "text-center", //最后一列，操作按钮
                    render: function (data, type, row, meta) {
                        var editUrlPath = subTable.editUrl + row.id + ".gsp"; //修改地址
                        var result = '';
                        if (row.id != "default") {
                            result = '<button href="' + editUrlPath + '" type="button" data-target="#commonWin" data-toggle="modal" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;<button type="button" onclick="vehicleModelsManagement.deleteSubData(\'' + row.id + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
                        } else {
                            result = '<button disabled  type="button" class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>修改</button>&ensp;<button disabled type="button"  class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>删除</button>';
                        }
                        return result;
                    }
                }, {
                    "data": "category",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "vehicleType",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "vehicleSubtypes",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "drivingWay",
                    "class": "text-center",
                    render: function (data) {
                        switch (data) {
                            case '0':
                                data = '自行';
                                break;
                            case '1':
                                data = '运输';
                                break;
                            default:
                                data = '';
                                break;
                        }
                        return data
                    }
                }, {
                    "data": "icoName",
                    "class": "text-center",
                    render: function (data) {
                        return data ? '<a href="javascript:vehicleModelsManagement.icoPhotoShow(\'' + data + '\')">查看照片</a>' : '';
                    }
                }, {
                    "data": "description",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }
            ];
            //ajax参数
            var ajaxDataParamFunThree = function (d) {
                d.simpleQueryParam = $('#simpleQueryParamThree').val(); //模糊查询
            };
            //表格subSetting
            var subSetting = {
                listUrl: '/clbs/m/basicinfo/monitoring/vehicle/type/findVehicleSubType',
                editUrl: '/clbs/m/basicinfo/monitoring/vehicle/type/updateSubType_',
                deleteUrl: '/clbs/m/basicinfo/monitoring/vehicle/type/deleteSubType_',
                deletemoreUrl: '/clbs/m/basicinfo/monitoring/vehicle/type/deleteMoreSubType',
                enableUrl: '/clbs/c/user/enable_',
                disableUrl: '/clbs/c/user/disable_',
                columnDefs: subColumnDefs, //表格列定义
                columns: subColumns, //表格列
                dataTableDiv: 'dataTableThree', //表格
                ajaxDataParamFun: ajaxDataParamFunThree, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true
            };
            //创建表格
            subTable = new TG_Tabel.createNew(subSetting);
            //表格初始化
            subTable.init();

            /*
                        subTable = $('#dataTableThree').DataTable({
                            columnDefs: [{
                                "searchable": false,
                                "targets": 5
                            }],
                            "destroy": true,
                            "dom": 'tiprl',// 自定义显示项
                            "lengthChange": true,// 是否允许用户自定义显示数量
                            "bPaginate": true, // 翻页功能
                            "bFilter": false, // 列筛序功能
                            "searching": true,// 本地搜索
                            "ordering": false, // 排序功能
                            "Info": true,// 页脚信息
                            "autoWidth": true,// 自动宽度
                            "stripeClasses": [],
                            "lengthMenu": [5,10, 20, 50, 100, 200],
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
                            },
                            "order": [
                                [0, null]
                            ],
                        });*/
        },
        //全选-车型列表
        checkAllClick: function (e) {
            $("input[name='subChk']").prop("checked", e.checked);
        },
        //单选-车型列表
        subChkClick: function () {
            $("#checkAll").prop("checked", subChk.length == subChk.filter(":checked").length ? true : false);
        },
        //单选-车辆类别
        subChkTwo: function () {
            $("#checkAllTwo").prop("checked", $subChkTwo.length == $subChkTwo.filter(":checked").length ? true : false);
        },
        //全选-车辆类别
        checkAllTwo: function (e) {
            $("input[name='subChkTwo']").prop("checked", e.checked);
        },
        //批量删除
        delModelClick: function () {
            //判断是否至少选择一项
            var chechedNum = $("input[name='subChk']:checked").length;
            if (chechedNum == 0) {
                layer.msg(selectItem, {
                    move: false
                });
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
        //刷新列表
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            myTable.requestData();
        },
        category: function () {
            var search = $("#search_input").val();
            var url = "/clbs/m/basicinfo/monitoring/vehicle/type/listCategory";
            var data = {
                "vehicleCategory": search
            };
            json_ajax("POST", url, "json", false, data, vehicleModelsManagement.categoryCallBack);
        },
        search: function () {
            vehicleModelsManagement.category();
        },
        categoryCallBack: function (data) {
            if (data.success) {
                var result = data.records;
                var dataListArray = [];
                var permission = $("#permission").val();
                for (var i = 0; i < result.length; i++) {
                    var str = "";
                    var checkboxHide = "";
                    if (result[i].id == 'default') {
                        str = '<button disabled  type="button" class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>修改</button>&ensp;<button disabled type="button"  class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>删除</button>';
                    } else {
                        str = '<button href="/clbs/m/basicinfo/monitoring/vehicle/type/editLogo?cid=' + result[i].id + '" type="button" data-target="#commonWin" data-toggle="modal" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;<button type="button" onclick="vehicleModelsManagement.deleteCategory(\'' + result[i].id + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
                        checkboxHide = '<input  type="checkbox" name="subChkTwo"  value="' + result[i].id + '" />';
                    }
                    switch (result[i].standard) {
                        case 0:
                            result[i].standard = '通用';
                            break;
                        case 1:
                            result[i].standard = '货运';
                            break;
                        case 2:
                            result[i].standard = '工程机械';
                            break;
                        default:
                            result[i].standard = '通用';
                            break;
                    }
                    if (result[i].vehicleCategory == '其他车辆') {
                        result[i].standard = '通用';
                    }
                    if (permission == "true") {
                        var list = [
                            i + 1,
                            checkboxHide,
                            str,
                            html2Escape(result[i].standard),
                            result[i].vehicleCategory,
                            '<a href="javascript:vehicleModelsManagement.icoPhotoShow(\'' + result[i].icoName + '\')">查看照片</a>',
                            html2Escape(result[i].description)
                        ];
                    } else {
                        var list = [
                            i + 1,
                            result[i].standard,
                            html2Escape(result[i].vehicleCategory),
                            '<a href="javascript:vehicleModelsManagement.icoPhotoShow(\'' + result[i].icoName + '\')">查看照片</a>',
                            html2Escape(result[i].description)
                        ];
                    }
                    dataListArray.push(list);
                }
                var currentPage = getTable.page();
                reloadData(dataListArray);
                getTable.page(currentPage).draw(false);
            } else {
                layer.msg(publicError);
            }
        },
        icoPhotoShow: function (ico) {
            var icoLogo = "/clbs/resources/img/vico/" + ico;
            $("#icoLogo").attr('src', icoLogo);
            $("#icoPhotoShow").modal("show");
        },
        deleteCategory: function (id) {
            var url = "/clbs/m/basicinfo/monitoring/vehicle/type/deleteCategory";
            var data = {
                "id": id
            };
            layer.confirm(publicDelete, {
                btn: ["确定", "取消"],
                icon: 3,
                title: "操作确认"
            }, function () {
                json_ajax("POST", url, "json", true, data, vehicleModelsManagement.deleteCallBack);
            });
        },
        deleteCallBack: function (data) {
            if (data.success) {
                if (data.msg == "false") {
                    layer.msg(categoryBind);
                } else {
                    layer.closeAll('dialog');
                    vehicleModelsManagement.category();
                }
            } else {
                layer.msg(data.msg);
            }
        },
        deleteMore: function () {
            var chechedNum = $("input[name='subChkTwo']:checked").length;
            if (chechedNum == 0) {
                layer.msg(selectItem, {
                    move: false
                });
                return
            }
            var checkedList = new Array();
            $("input[name='subChkTwo']:checked").each(function () {
                checkedList.push($(this).val());
            });
            var url = "/clbs/m/basicinfo/monitoring/vehicle/type/deleteMoreCategory";
            var data = {
                "ids": checkedList.toString()
            };
            layer.confirm(publicDelete, {
                btn: ["确定", "取消"]
            }, function () {
                json_ajax("POST", url, "json", true, data, vehicleModelsManagement.deleteMoreCallBack);
            });
        },
        deleteMoreCallBack: function (data) {
            if (data.success) {
                vehicleModelsManagement.category();
                if (data.msg != "") {
                    layer.msg(categoryBind);
                } else {
                    layer.closeAll('dialog');
                }
            } else {
                layer.msg(data.msg);
            }
        },
        findType: function (event) {
            if (event.keyCode == 13) {
                vehicleModelsManagement.category();
            }
        },

        //全选-车辆子类型列表
        checkAllSubClick: function (e) {
            $("input[name='subChkThree']").prop("checked", e.checked);
        },
        //单选-车辆子类型列表
        subChkSubClick: function () {
            $("#checkAllThree").prop("checked", subChkThree.length == subChkThree.filter(":checked").length ? true : false);
        },

        //车辆子类型模块方法
        //删除子类型
        deleteSubData: function (id) {
            var url = "/clbs/m/basicinfo/monitoring/vehicle/type/deleteSubType_" + id + ".gsp";
            var data = {
                "id": id
            };
            layer.confirm(publicDelete, {
                btn: ["确定", "取消"],
                icon: 3,
                title: "操作确认"
            }, function () {
                json_ajax("GET", url, "json", true, data, vehicleModelsManagement.deleteChildCallBack);
            });
        },
        deleteChildCallBack: function (data) {
            if (data.success) {
                if (data.msg == "false") {
                    layer.msg('该车辆子类型已绑定车辆，请先解除绑定');
                } else {
                    layer.closeAll('dialog');
                    layer.msg('删除成功！');
                    subTable.requestData();
                }
            } else {
                layer.msg(data.msg);
            }
        },
        //批量删除子类型
        deleteChildMore: function () {
            //判断是否至少选择一项
            var chechedNum = $("input[name='subChkThree']:checked").length;
            if (chechedNum == 0) {
                layer.msg(selectItem, {
                    move: false
                });
                return
            }
            var checkedList = new Array();
            $("input[name='subChkThree']:checked").each(function () {
                checkedList.push($(this).val());
            });
            subTable.deleteItems({
                'vehicleSubTypeIds': checkedList.toString()
            });
        },
    }
    $(function () {
        getTable("dataTables");
        //getTable("dataTableThree");
        $('input').inputClear();
        vehicleModelsManagement.init();
        vehicleModelsManagement.category();
        //vehicleModelsManagement.getChildList();
        //单选
        subChk.bind("click", vehicleModelsManagement.subChkClick);
        subChkTwo.bind("click", vehicleModelsManagement.subChkTwo);
        subChkThree.bind("click", vehicleModelsManagement.subChkSubClick);

        //批量删除
        $("#del_model").bind("click", vehicleModelsManagement.delModelClick);
        $("#refreshTable").on("click", vehicleModelsManagement.refreshTable);
        $("#del_modelTwo").on("click", vehicleModelsManagement.deleteMore);
        $("#search").on("click", vehicleModelsManagement.search);

        //车辆子类型模块绑定方法
        $("#del_modelThree").on("click", vehicleModelsManagement.deleteChildMore);

        // 车辆子类型搜索框键盘事件
        $('#simpleQueryParamThree').keyup(function (event) {
            if (event.keyCode == 13) {
                subTable.requestData()
            };
        });
    })
})(window, $)