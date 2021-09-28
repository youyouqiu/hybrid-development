var levelTable;
(function (window, $) {

    //单选
    var riskLevelIds = [];
    riskTypeLevel = {
        //初始化
        init: function () {
            riskTypeLevel.levelInit();
            riskTypeLevel.eventInit();
            // // 表格列定义
            // var columnDefs = [{
            //     // 第一列，用来显示序号
            //     "searchable": false,
            //     "orderable": false,
            //     "targets": 0
            // }];
            // var columns = [{
            //     // 第一列，用来显示序号
            //     "data": null,
            //     "class": "text-center"
            // }, {
            //     "data": "riskEvent",
            //     "class": "text-center"
            // }, {
            //     "data": "riskType",
            //     "class": "text-center",
            // }, {
            //     "data": "description",
            //     "class": "text-center",
            //
            // }
            //
            // ];
            // // ajax参数
            // var ajaxDataParamFun = function (d) {
            //     d.simpleQueryParam = $('#simpleQueryParam').val(); // 模糊查询
            // };
            // // 表格setting
            // var setting = {
            //     listUrl: '/clbs/r/riskManagement/TypeLevel/riskEvent/list',
            //     columnDefs: columnDefs, // 表格列定义
            //     columns: columns, // 表格列
            //     dataTableDiv: 'riskEventDataTable', // 表格
            //     ajaxDataParamFun: ajaxDataParamFun, // ajax参数
            //     pageable: true, // 是否分页
            //     showIndexColumn: true, // 是否显示第一列的索引列
            //     enabledChange: true
            // };
            // // 创建表格
            // myTable = new TG_Tabel.createNew(setting);
            // // 表格初始化
            // myTable.init();
        },
        eventInit: function () {
            var typeQuery = $("#simpleQueryParam").val();
            var url = "/clbs/r/riskManagement/TypeLevel/riskEvent/list";
            var data = {"simpleQueryParam": typeQuery}
            json_ajax("POST", url, "json", false, data, riskTypeLevel.findList);
        },
        findList: function (data) {
            var result = data.records;
            var dataListArray = [];
            for (var i = 0; i < result.length; i++) {
                var list = [i + 1,result[i].functionId, result[i].riskEvent, result[i].riskType, result[i].description];
                dataListArray.push(list)
            }
            riskTypeLevel.getDataTable("#riskEventDataTable", dataListArray);
        },
        getDataTable: function (divId, data) {
            $(divId).DataTable({
                "destroy": true,
                "dom": 'tiprl',// 自定义显示项
                "data": data,
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
                    }
                },
                "order": [
                    [0, null]
                ],
            });
        },
        levelInit: function () {
            //表格列定义
            var columnLevel = [{
                //第一列，用来显示序号
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];
            var columnLevels = [
                {
                    //第一列，用来显示序号
                    "data": null,
                    "class": "text-center"
                },
                /*{
                    "data": null,
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        var riskLevelId = row.riskLevelId;
                        return "<input  type='checkbox' name='subChk'  value='" + riskLevelId + "' onclick='riskTypeLevel.subChkClick(this)'/>";
                    }
                },*/
                {
                    "data": null,
                    "class": "text-center", //最后一列，操作按钮
                    render: function (data, type, row, meta) {
                        var editUrlPath = levelTable.editUrl + row.riskLevelId + ".gsp"; //修改地址
                        var result = '';
                        //修改
                        result += '<button href="' +
                            editUrlPath + '" data-toggle="modal" data-target="#commonWin"  type="button" class="editBtn editBtn-info">' +
                            '<i class="fa fa-pencil"></i>修改</button>&ensp;';
                        //删除按钮
                        /*result += '<button type="button" onclick="levelTable.deleteItem(\''
                            + row.riskLevelId
                            + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';*/
                        return result;
                    }
                },
                {
                    "data": "riskLevel",
                    "class": "text-center"
                },
                {
                    "data": "riskValue",
                    "class": "text-centr"
                },
                {
                    "data": "description",
                    "class": "text-center",
                }
            ];
            var ajaxData = function (d) {
                d.simpleQueryParam = $("#search_input").val();
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/r/riskManagement/TypeLevel/levelList',
                editUrl: '/clbs/r/riskManagement/TypeLevel/editLevel_',
                //deleteUrl: '/clbs/r/riskManagement/TypeLevel/levelDelete_',
                //deletemoreUrl: '/clbs/r/riskManagement/TypeLevel/levelDelete_',
                enableUrl: '/clbs/c/user/enable_',
                disableUrl: '/clbs/c/user/disable_',
                columnDefs: columnLevel, //表格列定义
                columns: columnLevels, //表格列
                dataTableDiv: 'riskLevelDataTable', //表格
                ajaxDataParamFun: ajaxData, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true
            };
            levelTable = new TG_Tabel.createNew(setting);
            levelTable.init();
        },
        levelTableFilter: function (e) {
            if (e.keyCode == 13) {
                riskTypeLevel.levelInit();
            }
        },
        // 列表刷新
        refreshTableClick: function () {
            $("#simpleQueryParam").val("");
            myTable.requestData();
        },
        //全选
        /*checkAllClick: function () {
            $("input[name='subChk']").prop("checked", this.checked);
            riskLevelIds = [];
            $("input[name='subChk']:checked").each(function () {
                riskLevelIds.push(this.value)
            });
        },*/
        /*subChkClick: function (data) {
            var subChk = $("input[name='subChk']");
            $("#checkAll").prop("checked", subChk.length == subChk.filter(":checked").length ? true : false);
            riskLevelIds = [];
            $("input[name='subChk']:checked").each(function () {
                riskLevelIds.push(this.value)
            });
        },*/
        purposeBodyShow: function () {
            if ($("#purposeBody").is(":hidden")) {
                $("#purposeBody").css("display", "block");
                $("#faChevron").removeClass("fa-chevron-up").addClass("fa-chevron-down");
                riskTypeLevel.levelInit();
            } else {
                $("#purposeBody").css("display", "none");
                $("#faChevron").removeClass("fa-chevron-down").addClass("fa-chevron-up");
            }
        },
        /*deleteMore: function () {
            //判断是否至少选择一项
            var chechedNum = $("input[name='subChk']:checked").length;
            if (chechedNum == 0) {
                layer.msg(selectItem, {move: false});
                return
            }
            levelTable.deleteItem(riskLevelIds.toString());
        }*/
    }
    $(function () {
        $('input').inputClear();
        riskTypeLevel.init();
        //全选
        // $("#checkAll").bind("click", riskTypeLevel.checkAllClick);
        //风险等级显示隐藏
        $("#purposeHead").on("click", riskTypeLevel.purposeBodyShow);
        // $("#del_model").on("click", riskTypeLevel.deleteMore);
    })
})(window, $)