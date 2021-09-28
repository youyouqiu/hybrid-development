(function (window, $) {
    //显示隐藏列
    var menu_textOne = $("#Ul-menu-text");
    var menu_textTwo = $("#Ul-menu-textTwo");
    var table = $("#dataTable tr th:gt(1)");
    var tableTwo = $("#dataTableTwo tr th:gt(1)");
    //单选
    var subChk = $("input[name='subChk']");
    var subChkTwo = $("input[name='subChkTwo']");
    dangerousGoods = {
        //自定义显示列
        setColumn: function (tab, targetDom) {
            var menu_text = '';
            menu_text += '<li><label><input type=\"checkbox\" checked=\"checked\" class="toggle-vis" data-column="' + parseInt(2) + '" disabled />' + tab[0].innerHTML + '</label></li>';
            for (var i = 1; i < tab.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" />" + tab[i].innerHTML + "</label></li>"
            }
            ;
            targetDom.html(menu_text);
        },
        //初始化
        init: function () {
            dangerousGoods.setColumn(table, menu_textOne);

            //品名表格初始化
            //表格列定义
            var columnDefs = [{
                //禁止某列参与搜索
                "searchable": false,
                "orderable": false,
                "targets": [0, 1, 3, 4, 5]
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
                            result += '<button href="' + editUrlPath + '" data-target="#commonSmWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
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
                    "data": "name",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "dangerType",
                    "class": "text-center",
                    render: function (data) {
                        var typeList = {
                            11: '危险货物1类1项',
                            12: '危险货物1类2项',
                            13: '危险货物1类3项',
                            14: '危险货物1类4项',
                            15: '危险货物1类5项',
                            16: '危险货物1类6项',
                            21: '危险货物2类1项',
                            22: '危险货物2类2项',
                            23: '危险货物2类3项',
                            3: '危险货物3类',
                            41: '危险货物4类1项',
                            42: '危险货物4类2项',
                            43: '危险货物4类3项',
                            51: '危险货物5类1项',
                            52: '危险货物5类2项',
                            61: '危险货物6类1项',
                            62: '危险货物6类2项',
                            7: '危险货物7类',
                            8: '危险货物8类',
                            9: '危险货物9类'
                        };
                        if (data != null && data != '') {
                            return typeList[data] ? typeList[data] : '';
                        } else {
                            return '';
                        }
                    }
                }, {
                    "data": "unit",
                    "class": "text-center",
                    render: function (data) {
                        if (data != null && data != '') {
                            switch (data) {
                                case 1:
                                    data = 'kg';
                                    break;
                                case 2:
                                    data = 'L';
                                    break;
                                default:
                                    data = '';
                                    break;
                            }
                        }
                        else {
                            data = '';
                        }
                        return data
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
                d.name = $('#simpleQueryParam').val(); //模糊查询
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/m/monitoring/vehicle/itemName/searchItemName',
                editUrl: '/clbs/m/monitoring/vehicle/itemName/edit_',
                deleteUrl: '/clbs/m/monitoring/vehicle/itemName/deleteItemNameById/delete_',
                deletemoreUrl: '/clbs/m/monitoring/vehicle/itemName/deleteItemName',
                enableUrl: '/clbs/c/user/enable_',
                disableUrl: '/clbs/c/user/disable_',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                drawCallbackFun: function () {
                    var api = myTable.dataTable;
                    var startIndex = api.context[0]._iDisplayStart;//获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                    });

                    //鼠标移入后弹出气泡显示单元格内容；
                    $(".demoUp").mouseover(function () {
                        var _this = $(this);
                        if (_this.attr("alt")) {
                            _this.justToolsTip({
                                animation: "moveInTop",
                                width: "auto",
                                contents: _this.attr("alt"),
                                gravity: 'top'
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
            //显示隐藏列
            $('#Ul-menu-text .toggle-vis').on('change', function (e) {
                e.preventDefault();
                var column = myTable.dataTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(this).parent().parent().parent().parent().addClass("open");
            });


            //趟次列表表格初始化
            dangerousGoods.setColumn(tableTwo, menu_textTwo);
            //表格列定义
            var subColumnDefs = [{
                ////禁止某列参与搜索
                "searchable": false,
                "orderable": false,
                "targets": [0, 1, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16]
            }];
            var subColumns = [
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
                            result += '<input  type="checkbox" name="subChkTwo"  value="' + row.id + '" />';
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
                            result = '<button href="' + editUrlPath + '" type="button" data-target="#commonWin" data-toggle="modal" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;<button type="button" onclick="subTable.deleteItem(\'' + row.id + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
                        } else {
                            result = '<button disabled  type="button" class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>修改</button>&ensp;<button disabled type="button"  class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>删除</button>';
                        }
                        return result;
                    }
                }, {
                    "data": "vehicleId",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "itemNameId",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "dangerType",
                    "class": "text-center",
                    render: function (data) {
                        var typeList = {
                            11: '危险货物1类1项',
                            12: '危险货物1类2项',
                            13: '危险货物1类3项',
                            14: '危险货物1类4项',
                            15: '危险货物1类5项',
                            16: '危险货物1类6项',
                            21: '危险货物2类1项',
                            22: '危险货物2类2项',
                            23: '危险货物2类3项',
                            3: '危险货物3类',
                            41: '危险货物4类1项',
                            42: '危险货物4类2项',
                            43: '危险货物4类3项',
                            51: '危险货物5类1项',
                            52: '危险货物5类2项',
                            61: '危险货物6类1项',
                            62: '危险货物6类2项',
                            7: '危险货物7类',
                            8: '危险货物8类',
                            9: '危险货物9类'
                        };
                        if (data != null && data != '') {
                            return typeList[data] ? typeList[data] : '';
                        } else {
                            return '';
                        }
                    }
                }, {
                    "data": "count",
                    "class": "text-center"
                }, {
                    "data": "unit",
                    "class": "text-center",
                    render: function (data) {
                        if (data != null && data != '') {
                            switch (data) {
                                case 1:
                                    data = 'kg';
                                    break;
                                case 2:
                                    data = 'L';
                                    break;
                                default:
                                    data = '';
                                    break;
                            }
                        }
                        else {
                            data = '';
                        }
                        return data
                    }
                }, {
                    "data": "transportType",
                    "class": "text-center",
                    render: function (data) {
                        if (data != null && data != '') {
                            switch (data) {
                                case 1:
                                    data = '营运性危险货物运输';
                                    break;
                                case 2:
                                    data = '非营运性危险货物运输';
                                    break;
                                default:
                                    data = '';
                                    break;
                            }
                        }
                        else {
                            data = '';
                        }
                        return data
                    }
                }, {
                    "data": "transportDate",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "startSite",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "viaSite",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "aimSite",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "professinoalId",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "professinoalNumber",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "phone",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
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
            var ajaxDataParamFunTwo = function (d) {
                d.vehicleNumber = $('#simpleQueryParamTwo').val(); //模糊查询
            };
            //表格subSetting
            var subSetting = {
                listUrl: '/clbs/m/monitoring/vehicle/transport/searchTransport',
                editUrl: '/clbs/m/monitoring/vehicle/transport/edit_',
                deleteUrl: '/clbs/m/monitoring/vehicle/transport/delete_',
                deletemoreUrl: '/clbs/m/monitoring/vehicle/transport/deleteTransport',
                enableUrl: '/clbs/c/user/enable_',
                disableUrl: '/clbs/c/user/disable_',
                columnDefs: subColumnDefs, //表格列定义
                columns: subColumns, //表格列
                dataTableDiv: 'dataTableTwo', //表格
                ajaxDataParamFun: ajaxDataParamFunTwo, //ajax参数
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
            subTable = new TG_Tabel.createNew(subSetting);
            //表格初始化
            subTable.init();
        },
        //全选-品名列表
        checkAllClick: function (e) {
            $("input[name='subChk']").prop("checked", e.checked);
        },
        //单选-品名列表
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
                'idList': checkedList.toString()
            });
        },
        //刷新列表
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            myTable.requestData();
        },
        //品名导出
        proExport: function () {
            if(getRecordsNum() > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var proName = $("#simpleQueryParam").val();
            var url = "/clbs/m/monitoring/vehicle/itemName/exportEnterpriseList?name=" + proName;
            window.location.href = url;
        },


        /******趟次列表方法********/
        //全选-趟次列表
        tripCheckAll: function (e) {
            $("input[name='subChkTwo']").prop("checked", e.checked);
        },
        //单选-趟次列表
        tripSubChk: function () {
            $("#checkAllTwo").prop("checked", subChkTwo.length == subChkTwo.filter(":checked").length ? true : false);
        },
        //批量删除趟次
        delModelTrip: function () {
            //判断是否至少选择一项
            var chechedNum = $("input[name='subChkTwo']:checked").length;
            if (chechedNum == 0) {
                layer.msg(selectItem, {move: false});
                return
            }
            var checkedList = new Array();
            $("input[name='subChkTwo']:checked").each(function () {
                checkedList.push($(this).val());
            });
            subTable.deleteItems({
                'idList': checkedList.toString()
            });
        },
        //刷新趟次列表
        refreshTripTable: function () {
            $("#simpleQueryParamTwo").val("");
            subTable.requestData();
        },
        //趟次列表导出
        tripExport: function () {
            if(getRecordsNum('dataTableTwo_info') > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var proName = $("#simpleQueryParamTwo").val();
            var url = "/clbs/m/monitoring/vehicle/transport/exportEnterpriseList?vehicleNumber=" + proName;
            window.location.href = url;
        }
    };
    $(function () {
        $('input').inputClear();
        dangerousGoods.init();

        /*******品名列表*******/
        //单选
        subChk.bind("click", dangerousGoods.subChkClick);
        //批量删除
        $("#del_model").bind("click", dangerousGoods.delModelClick);
        $("#exportId").bind("click", dangerousGoods.proExport);
        $("#refreshTable").on("click", dangerousGoods.refreshTable);


        /*******趟次列表*******/
        //单选
        subChkTwo.bind("click", dangerousGoods.tripSubChk);
        //批量删除
        $("#del_modelTwo").bind("click", dangerousGoods.delModelTrip);
        $("#exportIdTwo").bind("click", dangerousGoods.tripExport);
        $("#refreshTableTwo").on("click", dangerousGoods.refreshTripTable);
    })
})(window, $)