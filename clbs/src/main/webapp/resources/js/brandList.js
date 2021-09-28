(function ($, window) {
    var selectTreeId = '';
    var selectTreepId = "";
    var selectTreeType = '';
    var zNodes = null;
    var log, className = "dark";
    var $subChk = $("input[name='subChk']");
    var $subChkTwo = $("input[name='subChkTwo']");
    var pId = "";
    var vehiclevaluelast = $("#purposeCategoryQuery").val();
    var vehicleUserType;
    var vehicleUserTypeExpliant;
    brandList = {
        //初始化
        init: function () {
            //车辆机型表格初始化
            modelTable = $('#dataTable').DataTable({
                /*columnDefs: [{
                    "searchable": false,
                    "targets": 4
                }],*/
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
                "pageLength": 10,
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
            });
        },

        //加载品牌管理列表
        getBrandList: function () {
            var brandName = $("#brandQuery").val();
            var url = "/clbs/m/basicinfo/enterprise/brand/listBrand";
            var data = {"brandName": brandName};
            json_ajax("POST", url, "json", false, data, brandList.brandListCallback);
        },
        brandListCallback: function (data) {
            if (data.success) {
                var result = data.records;
                var dataListArray = [];
                var permission = $("#permission").val();
                for (var i = 0; i < result.length; i++) {
                    if (permission == 'true') {
                        var list = [
                            i + 1,
                            '<input  type="checkbox" name="subChkTwo"  value="' + result[i].id + '" />',
                            '<button href="/clbs/m/basicinfo/enterprise/brand/editBrand_' + result[i].id + '.gsp" data-target="#commonSmWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;<button type="button"  onclick="brandList.deleteBrand(\'' + result[i].id + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>',
                            html2Escape(result[i].brandName),
                            html2Escape(result[i].describtion)
                        ];
                    } else {
                        var list = [
                            i + 1,
                            html2Escape(result[i].brandName),
                            html2Escape(result[i].describtion)
                        ];
                    }
                    dataListArray.push(list);
                }
                var currentPage = getTable.page();
                reloadData(dataListArray);//传入数组，调用公用js，详情请看tg_common.js
                getTable.page(currentPage).draw(false);
            } else {
                layer.msg(publicError);
            }
        },
        //品牌单选
        subChkTwo: function () {
            $("#checkAllTwo").prop("checked", $subChkTwo.length == $subChkTwo.filter(":checked").length ? true : false);
        },
        //搜索车辆品牌
        searchVehicleBrand: function () {
            brandList.getBrandList();
        },
        //搜索回车键键盘监听事件
        findBrand: function (event) {
            if (event.keyCode == 13) {
                brandList.getBrandList();
            }
        },
        //根据id删除车辆品牌
        deleteBrand: function (id) {
            layer.confirm(publicDelete, {
                title: '操作确认',
                icon: 3, // 问号图标
                btn: ['确定', '取消'] //按钮
            }, function () {
                var url = "/clbs/m/basicinfo/enterprise/brand/deleteBrand_" + id + ".gsp";
                var data = {"id": id};
                json_ajax("POST", url, "json", false, data, brandList.deleteBrandCallback);
            });
        },
        //根据id删除车辆品牌回调函数
        deleteBrandCallback: function (result) {
            if (result.success) {
                layer.closeAll('dialog');
                layer.msg("删除成功！");
                brandList.getBrandList();
            } else {
                layer.msg(result.msg);
            }
        },
        //批量删除车辆品牌
        deleteBrandMuch: function () {
            //判断是否至少选择一项
            var chechedNum = $("input[name='subChkTwo']:checked").length;
            if (chechedNum == 0) {
                layer.msg(selectItem);
                return
            }
            var ids = "";
            $("input[name='subChkTwo']:checked").each(function () {
                ids += ($(this).val()) + ",";
            });
            var url = "/clbs/m/basicinfo/enterprise/brand/deleteBrandMore";
            var data = {"ids": ids};
            layer.confirm(publicDelete, {
                title: '操作确认',
                icon: 3, // 问号图标
                btn: ['确定', '取消'] //按钮
            }, function () {
                json_ajax("POST", url, "json", false, data, brandList.deleteBrandMuchCallback);
            });
        },
        //批量删除车辆品牌回调函数
        deleteBrandMuchCallback: function (result) {
            if (result.success) {
                layer.closeAll('dialog');
            }
            if (result.msg != null) {
                layer.msg(result.msg);
            }
            brandList.getBrandList();
        },
        checkAllTwo: function (e) {
            $("input[name='subChkTwo']").prop("checked", e.checked);
        },


        //机型管理
        //机型单选
        subChk: function () {
            $("#checkAll").prop("checked", $subChk.length == $subChk.filter(":checked").length ? true : false);
        },
        //加载机型管理列表
        getModelList: function () {
            var modelName = $("#modelQuery").val();
            var url = "/clbs/m/basicinfo/enterprise/brand/listBrandModels";
            var data = {"simpleQueryParam": modelName};
            json_ajax("POST", url, "json", false, data, brandList.modelListCallback);
        },
        modelListCallback: function (data) {
            if (data.success) {
                var result = data.records;
                var dataListArray = [];
                var permission = $("#permission").val();
                for (var i = 0; i < result.length; i++) {
                    if (permission == 'true') {
                        var list = [
                            i + 1,
                            '<input  type="checkbox" name="subChk"  value="' + result[i].id + '" />',
                            '<button href="/clbs/m/basicinfo/enterprise/brand/editBrandModels_' + result[i].id + '.gsp" data-target="#commonSmWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;<button type="button"  onclick="brandList.deleteModel(\'' + result[i].id + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>',
                            html2Escape(result[i].brandName),
                            html2Escape(result[i].modelName),
                            html2Escape(result[i].describtion)
                        ];
                    }else{
                        var list = [
                            i + 1,
                            html2Escape(result[i].brandName),
                            html2Escape(result[i].modelName),
                            html2Escape(result[i].describtion)
                        ];
                    }
                    dataListArray.push(list);
                }
                var currentPage = modelTable.page();
                brandList.reloadModelData(dataListArray);
                modelTable.page(currentPage).draw(false);
            } else {
                layer.msg(publicError);
            }
        },
        reloadModelData: function (dataList) {
            var currentPage = modelTable.page();
            modelTable.clear();
            modelTable.rows.add(dataList);
            modelTable.page(currentPage).draw(false);
        },
        //搜索车辆机型
        searchVehicleModel: function () {
            brandList.getModelList();
        },
        //搜索回车键键盘监听事件
        findModel: function (event) {
            if (event.keyCode == 13) {
                brandList.getModelList();
            }
        },
        //根据id删除车辆机型
        deleteModel: function (id) {
            layer.confirm(publicDelete, {
                title: '操作确认',
                icon: 3, // 问号图标
                btn: ['确定', '取消'] //按钮
            }, function () {
                var url = "/clbs/m/basicinfo/enterprise/brand/deleteBrandModels_" + id + ".gsp";
                var data = {"id": id};
                json_ajax("POST", url, "json", false, data, brandList.deleteModelCallback);
            });
        },
        //根据id删除车辆机型回调函数
        deleteModelCallback: function (result) {
            if (result.success) {
                layer.closeAll('dialog');
                layer.msg("删除成功！");
                brandList.getModelList();
            } else {
                layer.msg(result.msg);
            }
        },
        //批量删除车辆机型
        deleteModelMuch: function () {
            //判断是否至少选择一项
            var chechedNum = $("input[name='subChk']:checked").length;
            if (chechedNum == 0) {
                layer.msg(selectItem);
                return
            }
            var ids = "";
            $("input[name='subChk']:checked").each(function () {
                ids += ($(this).val()) + ",";
            });
            var url = "/clbs/m/basicinfo/enterprise/brand/deleteBrandModelsMore";
            var data = {"ids": ids};
            layer.confirm(publicDelete, {
                title: '操作确认',
                icon: 3, // 问号图标
                btn: ['确定', '取消'] //按钮
            }, function () {
                json_ajax("POST", url, "json", false, data, brandList.deleteModelMuchCallback);
            });
        },
        //批量删除车辆机型回调函数
        deleteModelMuchCallback: function (result) {
            if (result.success) {
                layer.closeAll('dialog');
                layer.msg("删除成功！");
                brandList.getModelList();
            } else {
                if (result.msg != null) {
                    layer.msg(result.msg);
                }
                brandList.getModelList();
            }
        },
        checkAll: function (e) {
            $("input[name='subChk']").prop("checked", e.checked);
        }
    }
    $(function () {
        getTable("dataTables");
        brandList.init();
        brandList.getBrandList();
        brandList.getModelList();
        $('input').inputClear();

        //全选(车辆品牌列表)
        $("#checkAllTwo").click(function () {
            $("input[name='subChkTwo']").prop("checked", this.checked);
        });
        $subChkTwo.on("click", brandList.subChkTwo);
        //批量删除车辆品牌
        $("#del_modelTwo").on("click", brandList.deleteBrandMuch);
        //车辆品牌搜索
        $("#search_buttonTwo").on("click", brandList.searchVehicleBrand);


        //全选(车辆机型)
        $("#checkAll").click(function () {
            $("input[name='subChk']").prop("checked", this.checked);
        });
        $("#del_model").on("click", brandList.deleteModelMuch);
        $subChk.on("click", brandList.subChkTwo);
        //车辆机型搜索
        $("#search_button").on("click", brandList.searchVehicleModel);
    })
})($, window)