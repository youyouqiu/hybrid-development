(function (window, $) {
    //显示隐藏列
    // var subTable;
    var menu_text = "";
    var table = $("#dataTable tr th:gt(1)");
    var menu_text_two = "";
    var tableTwo = $("#dataTableThree tr th:gt(1)");
    //单选
    // var subChk = $("input[name='subChk']");
    // var subChkTwo = $("input[name='subChkTwo']");
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
            var columns = [{
                    //第一列，用来显示序号
                    "data": null,
                    "class": "text-center"
                },
                // {
                //     "data": null,
                //     "class": "text-center",
                //     render: function (data, type, row, meta) {
                //         var result = '';
                //         if (row.id != "default" && !row.codeNum) {
                //             result += '<input  type="checkbox" name="subChk"  value="' + row.id + '" />';
                //         }
                //         return result;
                //     }
                // },
                {
                    "data": null,
                    "class": "text-center", //最后一列，操作按钮
                    render: function (data, type, row, meta) {
                        // var editUrlPath = myTable.editUrl + row.id + ".gsp"; //修改地址
                        var result = '';
                        //修改按钮
                        result += '<button id="edit_' + row.id + '"  onclick="vehicleModelsManagement.showEidt(\'' + row.id + '\')"   type="button" class="editBtn editBtn-info vehicleManageEditModel"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                        //下载按钮
                        result += '<button onclick="vehicleModelsManagement.downVehicleList(\'' + row.id + '\')"   type="button" class="editBtn editBtn-info"><i class="fa fa-download"></i>下载车辆</button>&ensp;';
                        //删除按钮
                        result += '<button type="button" onclick="vehicleModelsManagement.deleteVehicle(\'' +
                            row.id +
                            '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
                        return result;
                    }
                }, {
                    "data": "dockingCodeOrg",
                    "class": "text-center",
                }, {
                    "data": "forwardingPlatform",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "downloadTimeStr",
                    "class": "text-center",
                }, {
                    "data": "downloadStatus",
                    "class": "text-center",
                    render: function (data) {
                        switch (data) {
                            case 0:
                                data = '下载失败';
                                break;
                            case 1:
                                data = '下载中...';
                                break;
                            case 2:
                                data = '下载成功';
                                break;
                            default:
                                data = '';
                                break;
                        }
                        return data
                    }
                }, {
                    "data": "url",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "userName",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "password",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "dockingCode",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }
            ];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.dockingCodeOrgName = $('#simpleQueryParam').val(); //模糊查询
            };
            //表格setting  车辆下载表格
            var setting = {
                listUrl: '/clbs/m/forward/vehicle/manage/list',
                editUrl: '/clbs/m/forward/vehicle/manage/edit_',
                deleteUrl: '/clbs/m/forward/vehicle/manage/delete_',
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

            //转发车辆下载地址 表头筛选
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


            //表格列定义
            var subColumnDefs = [{
                ////禁止某列参与搜索
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];
            //车辆转发列表
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
                        if (row.id != "default" && !row.codeNum) {
                            result += '<input  type="checkbox" name="subChkThree"  value="' + row.id + '" />';
                        }
                        return result;
                    }
                },
                {
                    "data": null,
                    "class": "text-center", //最后一列，操作按钮
                    render: function (data, type, row, meta) {
                        var editUrlPath = subTable.editUrl + row.id + ".gsp"; //关联路线地址
                        var result = '';
                        if (row.id != "default") {
                            result = '<button href="' + editUrlPath + '" type="button" data-target="#commonWin" data-toggle="modal" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>关联线路</button>&ensp;<button type="button" onclick="vehicleModelsManagement.deleteSubData(\'' + row.id + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>关闭转发</button>';
                        } else {
                            result = '<button disabled  type="button" class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>关联线路</button>&ensp;<button disabled type="button"  class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>关闭转发</button>';
                        }
                        return result;
                    }
                }, {
                    "data": "dockingCodeOrg",
                    "class": "text-center",
                }, {
                    "data": "brand",
                    "class": "text-center",
                }, {
                    "data": "plateColor",
                    "class": "text-center",
                    // render: function (data) {
                    //     switch (data) {
                    //         case 1:
                    //             data = '红色';
                    //             break;
                    //         case 2:
                    //             data = '蓝色';
                    //             break;
                    //         default:
                    //             data = '';
                    //             break;
                    //     }
                    //     return data
                    // }
                }, {
                    "data": "forwardingPlatform",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "industryCategory",
                    "class": "text-center",
                    render: function (data) {
                        switch (data) {
                            case 1:
                                data = '公交';
                                break;
                            case 2:
                                data = '出租';
                                break;
                            case 3:
                                data = '农客';
                                break;
                            default:
                                data = '';
                                break;
                        }
                        return data
                    }
                }, {
                    "data": "vehicleStatus",
                    "class": "text-center",
                    render: function (data) {
                        switch (data) {
                            case 1:
                                data = '正常';
                                break;
                            case 2:
                                data = '停运';
                                break;
                            case 3:
                                data = '注销';
                                break;
                            case 4:
                                data = '删除';
                                break;
                            default:
                                data = '';
                                break;
                        }
                        return data
                    }
                },
                {
                    "data": "matchStatus",
                    "class": "text-center",
                    render: function (data) {
                        switch (data) {
                            case 0:
                                data = '失败';
                                break;
                            case 1:
                                data = '成功';
                                break;
                            default:
                                data = '';
                                break;
                        }
                        if (data === '失败') {
                            return `<span style="color:red">${data}</span>`
                        } else {
                            return data
                        }

                    }
                },
                {
                    "data": "failedReason",
                    "class": "text-center",
                    render: function (data) {
                        switch (data) {
                            case 0:
                                data = '企业未找到对应车辆档案';
                                break;
                            case 1:
                                data = '油补平台没有此辆车';
                                break;
                            case 2:
                                data = '油补平台与企业都无此辆车';
                                break;
                            default:
                                data = '';
                                break;
                        }
                        return `<span style="color:red">${data}</span>`
                    }
                },
                {
                    "data": "matchTimeStr",
                    "class": "text-center",
                },
                {
                    "data": "vehicleCode",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                },
                {
                    "data": "frameNumber",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                },
                {
                    "data": "vehicleOrg",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                },
                {
                    "data": "orgCode",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                },
                {
                    "data": "lineName",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }
            ];
            //ajax参数
            var ajaxDataParamFunThree = function (d) {
                d.searchParam = $('#simpleQueryParamThree').val(); //模糊查询

            };
            //表格subSetting
            var subSetting = {
                listUrl: '/clbs/m/forward/vehicle/manage/vehicle/list',
                editUrl: '/clbs/m/forward/vehicle/manage/bindLine_',
                deleteUrl: '/clbs/m/basicinfo/monitoring/vehicle/type/deleteSubType_',
                deletemoreUrl: '/clbs/m/basicinfo/monitoring/vehicle/type/deleteMoreSubType',
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

            //车辆转发列表  表头筛选
            menu_text_two += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-viss\" data-column=\"" + parseInt(2) + "\" disabled />" + tableTwo[0].innerHTML + "</label></li>"
            for (var i = 1; i < tableTwo.length; i++) {
                if (tableTwo[i].innerHTML == '车辆编码') {
                    menu_text_two += "<li><label><input type=\"checkbox\" checked='checked'  class=\"toggle-viss showfalse\" data-column=\"" + parseInt(i + 2) + "\" />" + tableTwo[i].innerHTML + "</label></li>"
                } else if (tableTwo[i].innerHTML == '车架号') {
                    menu_text_two += "<li><label><input type=\"checkbox\" checked='checked'  class=\"toggle-viss cjhNum\" data-column=\"" + parseInt(i + 2) + "\" />" + tableTwo[i].innerHTML + "</label></li>"
                } else if (tableTwo[i].innerHTML == '企业编码') {
                    menu_text_two += "<li><label><input type=\"checkbox\" checked='checked'  class=\"toggle-viss qybmNum\" data-column=\"" + parseInt(i + 2) + "\" />" + tableTwo[i].innerHTML + "</label></li>"
                } else {
                    menu_text_two += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-viss\" data-column=\"" + parseInt(i + 2) + "\" />" + tableTwo[i].innerHTML + "</label></li>"
                }
            }
            $("#Ul-menu-textThree").html(menu_text_two);
            //显示隐藏列
            $('.toggle-viss').on('change', function (e) {
                var column = subTable.dataTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });
            vehicleModelsManagement.firstColumn()
            vehicleModelsManagement.cjhColumn()
            vehicleModelsManagement.qybmNum()
        },
        //刷新列表
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            myTable.requestData();
        },
        refreshTableThree: function () {
            $("#ajaxDataParamFunThree").val("");
            subTable.requestData();
        },
        firstColumn: function () {
            $('.showfalse').attr('checked', null)
            var column = subTable.dataTable.column($('.showfalse').attr('data-column'));
            column.visible(!column.visible());
        },
        cjhColumn: function () {
            $('.cjhNum').attr('checked', null)
            var column = subTable.dataTable.column($('.cjhNum').attr('data-column'));
            column.visible(!column.visible());
        },
        qybmNum: function () {
            $('.qybmNum').attr('checked', null)
            var column = subTable.dataTable.column($('.qybmNum').attr('data-column'));
            column.visible(!column.visible());
        },
        category: function () {
            var search = $("#search_input").val();
            var url = "/clbs/m/forward/vehicle/manage/list";
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
        // deleteMore: function () {
        //     var chechedNum = $("input[name='subChkTwo']:checked").length;
        //     if (chechedNum == 0) {
        //         layer.msg(selectItem, {
        //             move: false
        //         });
        //         return
        //     }
        //     var checkedList = new Array();
        //     $("input[name='subChkTwo']:checked").each(function () {
        //         checkedList.push($(this).val());
        //     });
        //     var url = "/clbs/m/basicinfo/monitoring/vehicle/type/deleteMoreCategory";
        //     var data = {
        //         "ids": checkedList.toString()
        //     };
        //     layer.confirm(publicDelete, {
        //         btn: ["确定", "取消"]
        //     }, function () {
        //         json_ajax("POST", url, "json", true, data, vehicleModelsManagement.deleteMoreCallBack);
        //     });
        // },
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
        //全选-车辆转发列表
        checkAllSubClick: function (e) {
            $("input[name='subChkThree']").prop("checked", e.checked);
        },
        //单选-车辆转发列表
        subChkSubClick: function () {
            $("#checkAllThree").prop("checked", subChkThree.length == subChkThree.filter(":checked").length ? true : false);
        },
        //车辆转发列表  关闭转发
        deleteSubData: function (id) {
            var url = "/clbs/m/forward/vehicle/manage/deleteVehicle";
            var data = {
                ids: id
            }
            layer.confirm(publicDelete, {
                btn: ["确定", "取消"],
                icon: 3,
                title: "操作确认"
            }, function () {
                json_ajax("POST", url, "json", true, data, vehicleModelsManagement.deleteChildCallBack);
            });
        },
        deleteChildCallBack: function (data) {
            if (data.success) {
                layer.closeAll('dialog');
                layer.msg('删除成功！');
                subTable.requestData();
            } else {
                layer.msg(data.msg);
                subTable.requestData();
            }
        },
        showEidt: function (id) {
            // $('#commonSmWin').modal('hide')
            var url = "/clbs/m/forward/vehicle/manage/canEdit_" + id + ".gsp";
            var data = {
                "id": id
            };
            json_ajax("GET", url, "json", true, data, function (data) {
                if (data.obj) {
                    var curEditBtn = $("#edit_" + id);
                    var editUrl = '/clbs/m/forward/vehicle/manage/edit_';
                    var editUrlPath = editUrl + id + ".gsp"; //修改地址
                    curEditBtn.attr("href", editUrlPath);
                    curEditBtn.attr("data-target", "#commonSmWin");
                    curEditBtn.attr("data-toggle", "modal");
                    curEditBtn.attr("onclick", "");
                    curEditBtn.click();
                } else {
                    layer.msg('已下载转发车辆，为保证数据准确，请不要再编辑我了！');
                }
            }, );
        },
        // showCallBack: function (data) {
        //     console.log(data, 'lllllllll')
        //     if (data.obj) {
        //         var editUrl = '/clbs/m/forward/vehicle/manage/edit_';
        //         var editUrlPath = editUrl + row.id + ".gsp"; //修改地址
        //         window.open(editUrlPath)
        //     } else {
        //         layer.msg(data.msg);
        //     }
        // },
        //批量  关联车辆
        associatedEhicle: function () {
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
            let data = {
                ids: checkedList.toString()
            }

            let url = '/clbs/m/forward/vehicle/manage/bindVehicle'
            layer.confirm("'油补平台没有此辆车'和'油补平台与企业都无此辆车'不会关联。", {
                btn: ["确定", "取消"],
                icon: 3,
                title: "提示信息"
            }, function () {
                json_ajax("POST", url, "json", true, data, vehicleModelsManagement.associatedEhicleCallBack);
            });

            // let url = '/clbs/m/forward/vehicle/manage/bindVehicle'
            // json_ajax("POST", url, "json", true, data, vehicleModelsManagement.associatedEhicleCallBack);
            // subTable.deleteItems({
            //     'vehicleSubTypeIds': checkedList.toString()
            // });
        },
        associatedEhicleCallBack: function (data) {
            if (data.success) {
                layer.closeAll('dialog');
                layer.msg('操作成功！');
                subTable.requestData();
            } else {
                layer.msg(data.msg);
            }
        },
        //批量 关闭转发
        closeOrwarding: function () {
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
            let data = {
                ids: checkedList.toString()
            }
            var url = "/clbs/m/basicinfo/monitoring/vehicle/type/deleteSubType_" + id + ".gsp";
            layer.confirm("关闭转发后，此车运营数据不在报送到油补平台，您确定这么做？", {
                btn: ["确定", "取消"],
                icon: 3,
                title: "关闭转发确认"
            }, function () {
                json_ajax("GET", url, "json", true, data, vehicleModelsManagement.closeOrwardingCallBack);
            });
        },
        closeOrwardingCallBack: function (data) {
            if (data.success) {
                layer.closeAll('dialog');
                layer.msg('关闭成功！');
                subTable.requestData();
            } else {
                layer.msg(data.msg);
            }
        },
        //下载车辆列表
        downVehicleList: function (id) {
            var url = "/clbs/m/forward/vehicle/manage/download_" + id + ".gsp";
            var data = {
                "id": id
            };
            json_ajax("GET", url, "json", true, data, vehicleModelsManagement.downVehicleListCallBack);
        },
        //下载车辆列表回调
        downVehicleListCallBack: function (data) {
            if (data.success) {
                layer.msg('操作成功！');
                myTable.requestData();
            } else {
                layer.msg(data.msg);
            }
        },
        //车辆下载列表 删除
        deleteVehicle: function (id) {
            var url = "/clbs/m/forward/vehicle/manage/delete_" + id + ".gsp";
            var data = {
                "id": id
            };
            layer.confirm('删除下载转发车辆地址时,同时会删除对应组织中转发车辆,请谨慎删除操作,确认删除?', {
                btn: ["确定", "取消"],
                icon: 3,
                title: "删除确认"
            }, function () {
                json_ajax("GET", url, "json", true, data, vehicleModelsManagement.deleteVehicleCallBack);
            });
        },
        //批量关闭 回调
        deleteVehicleCallBack: function (data) {
            if (data.success) {
                layer.closeAll('dialog');
                layer.msg('删除成功！');
                subTable.requestData();
                myTable.requestData();
            } else {
                layer.msg(data.msg);
            }
        },
        //批量删除
        delModelClick: function () {
            //判断是否至少选择一项
            var chechedNum = $("input[name='subChkThree']:checked").length;
            if (chechedNum == 0) {
                layer.msg(selectItem);
                return
            }
            var checkedList = new Array();
            $("input[name='subChkThree']:checked").each(function () {
                checkedList.push($(this).val());
            });

            var url = "/clbs/m/forward/vehicle/manage/deleteVehicle";
            var data = {
                "ids": checkedList.toString()
            };
            layer.confirm('关闭转发后，此车运营数据不在报送到油补平台，您确定这么做?', {
                btn: ["确定", "取消"],
                icon: 3,
                title: "删除确认"
            }, function () {
                json_ajax("POST", url, "json", true, data, vehicleModelsManagement.deleteVehicleCallBack);
            });
        },
    }
    $(function () {
        getTable("dataTableThree");
        $('input').inputClear();
        vehicleModelsManagement.init();
        vehicleModelsManagement.category();
        //单选
        // subChk.bind("click", vehicleModelsManagement.subChkClick);
        // subChkTwo.bind("click", vehicleModelsManagement.subChkTwo);
        subChkThree.bind("click", vehicleModelsManagement.subChkSubClick);

        //批量 关闭转发
        $("#close_orwarding").bind("click", vehicleModelsManagement.delModelClick);
        $("#refreshTable").on("click", vehicleModelsManagement.refreshTable);
        $("#refreshTableThree").on("click", vehicleModelsManagement.refreshTableThree);

        $("#del_modelTwo").on("click", vehicleModelsManagement.deleteMore);
        $("#search").on("click", vehicleModelsManagement.search);

        //批量 关联车辆 
        $("#associated_ehicle").on("click", vehicleModelsManagement.associatedEhicle);

        // 辆转发列表 搜索
        $('#simpleQueryParamThree').keyup(function (event) {
            if (event.keyCode == 13) {
                subTable.requestData()
            };
        });
        // 转发车辆下载地址 搜索
        $('#simpleQueryParam').keyup(function (event) {
            if (event.keyCode == 13) {
                subTable.requestData()
            };
        });
    })
})(window, $)