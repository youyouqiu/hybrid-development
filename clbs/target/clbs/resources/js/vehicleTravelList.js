(function (window, $) {
    //显示隐藏列
    // var subTable;
    var menu_text = "";
    var table = $("#dataTable tr th:gt(1)");
    //单选
    var subChk = $("input[name='subChk']");
    vehicleTravel = {
        //初始化
        init: function () {
            menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            ;
            $("#Ul-menu-text").html(menu_text);
            //表格列定义
            var columnDefs = [{
                //禁止某列参与搜索
                "searchable": false,
                "orderable": false,
                "targets": [0, 1, 2, 5, 6, 7, 8, 9]
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
                        var result = '';
                        if (row.id != "default") {
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
                        if (row.id != "default") {
                            //修改按钮
                            result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                            //删除按钮
                            result += '<button type="button" onclick="myTable.deleteItem(\''
                                + row.id
                                + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
                        } else {
                            // 禁用修改删除按钮
                            result += '<button disabled type="button" class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>修改</button>&ensp;';
                            result += '<button disabled type="button"  class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>删除</button>&ensp;';
                        }
                        return result;
                    }
                }, {
                    "data": "travelId",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "brand",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "startTime",
                    "class": "text-center"
                }, {
                    "data": "endTime",
                    "class": "text-center"
                }, {
                    "data": "address",
                    "class": "text-center",
                    render: function (data) {
                        if (data != null && data != '') {
                            return html2Escape(data);
                        }
                        else {
                            return '';
                        }
                    }
                }, {
                    "data": "travelContent",
                    "class": "text-center",
                    render: function (data) {
                        if (data != null && data != '') {
                            if (data.length > 20) {
                                return '<span class="demo demoUp" alt="' + html2Escape(data) + '">' + html2Escape(data).substring(0, 20) + "..." + '</span>';
                            }
                            else {
                                return html2Escape(data);
                            }
                        }
                        else {
                            return '';
                        }
                    }
                }, {
                    "data": "remark",
                    "class": "text-center",
                    render: function (data) {
                        if (data != null && data != '') {
                            if (data.length > 20) {
                                return '<span class="demo demoUp" alt="' + html2Escape(data) + '">' + html2Escape(data).substring(0, 20) + "..." + '</span>';
                            }
                            else {
                                return html2Escape(data);
                            }
                        }
                        else {
                            return '';
                        }
                    }
                }];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.brand = $('#simpleQueryParam').val(); //模糊查询
                d.travelId = $('#simpleQueryParam').val(); //模糊查询
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/cb/vehicle/travel/list',
                editUrl: '/clbs/cb/vehicle/travel//edit_',
                deleteUrl: '/clbs/cb/vehicle/travel/delete_',
                deletemoreUrl: '/clbs/cb/vehicle/travel/deleteMore',
                enableUrl: '/clbs/c/user/enable_',
                disableUrl: '/clbs/c/user/disable_',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                drawCallbackFun: function () {//鼠标移入后弹出气泡显示单元格内容；
                    $(".demoUp").mouseover(function () {
                        var _this = $(this);
                        if (_this.attr("alt")) {
                            _this.justToolsTip({
                                animation: "moveInTop",
                                width: "auto",
                                contents: _this.attr("alt"),
                                gravity: 'top',
                                distance: 20
                            });
                        }
                    })
                },
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            //表格初始化
            myTable.init();
        },
        //全选-车型列表
        checkAllClick: function (e) {
            $("input[name='subChk']").prop("checked", e.checked);
        },
        //单选-车型列表
        subChkClick: function () {
            $("#checkAll").prop("checked", subChk.length == subChk.filter(":checked").length ? true : false);
        },
        //批量删除
        delModelClick: function () {
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
            myTable.deleteItems({
                'ids': checkedList.toString()
            });
        },
        //刷新列表
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            myTable.requestData();
        },
        //行程列表导出
        travelExport: function () {
            if(getRecordsNum() > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var proName = $("#simpleQueryParam").val();
            var url = "/clbs/cb/vehicle/travel/export?brand=" + proName+"&travelId="+proName;
            window.location.href = url;
        }
    }
    $(function () {
        $('input').inputClear();
        vehicleTravel.init();

        $("#advanced_search").on("click", vehicleTravel.showAdvancedContent);
        $("#emptyBtn").on("click", vehicleTravel.emptyAdvancedContent);

        //单选
        subChk.bind("click", vehicleTravel.subChkClick);

        //批量删除
        $("#del_model").bind("click", vehicleTravel.delModelClick);
        $("#exportId").bind("click", vehicleTravel.travelExport);
        $("#refreshTable").on("click", vehicleTravel.refreshTable);
    })
})(window, $)