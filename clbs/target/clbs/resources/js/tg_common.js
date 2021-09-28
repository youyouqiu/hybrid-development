// 弹出窗口
toastr.options = {
    closeButton: true,
    debug: false,
    progressBar: true,
    positionClass: 'toast-top-right',
    timeOut: 2000
};

// 操作成功
function tg_alertSuccess(title, message) {
    //toastr.success(!title ? '操作成功' : title, !message ? '恭喜您，操作成功！' : message);
}

// 系统消息
function tg_alertInfo(title, message) {
    toastr.info(!title ? '系统消息' : title, !message ? '系统消息!' : message);
}

// 系统警告
function tg_alertWarning(title, message) {
    toastr.warning(!title ? '系统警告' : title, !message ? '系统警告!' : message);
}

// 系统错误
function tg_alertError(title, message) {
    tg_confirmDialog(!title ? '系统错误' : title, !message ? '对不起，操作失败！' : message);
    //  toastr.error(!title ? '系统错误' : title, !message ? '对不起，操作失败！' : message);
}

// tg_confirmDialog
function tg_confirmDialog(title, message, okCallbackFun, cancelCallbackFun) {
    layer.confirm(!message ? '您确定要执行该操作吗？！' : message, {
        title: !title ? '操作确认' : title,
        icon: 3, // 问号图标
        move: false, //禁止拖动
        btn: ['确定', '取消']
    }, function () {
        // 确定按钮操作
        if (okCallbackFun) {
            okCallbackFun();
        }
        layer.closeAll(); // 关闭layer
    }, function () {
        // 取消按钮操作
        if (cancelCallbackFun) {
            cancelCallbackFun();
        }
    });
}

// 基本表单ajax 提交
function tg_baseFormAjaxSubmit(formId, rules, messages, sucCallbackFun, failCallbackFun) {
    var sucCallback = function (responseText, statusText) {
        if (responseText.success) {
            if (sucCallbackFun) {
                sucCallbackFun(); // 回调函数
            } else {
                tg_alertSuccess();
            }
        } else {
            form.attr("checkSubmitFlag", false);
            if (failCallbackFun) {
                failCallbackFun(); // 回调函数
            } else {
                tg_alertError('操作失败', responseText.msg);
            }
        }
    };
    var options = {
        success: sucCallback, // 提交后的回调函数
        dataType: 'json', // 接受服务端返回的类型
        clearForm: true, // 成功提交后，清除所有表单元素的值
        resetForm: true, // 成功提交后，重置所有表单元素的值
        timeout: 5000
    };
    var form = $("#" + formId);
    // 表单验证
    var validator = form.validate({
        rules: !rules ? {} : rules,
        messages: !messages ? {} : messages,
        ignore: rules && rules.ignore ? true : '',
        // errorPlacement : function(error, element) {
        // error.insertBefore(element.parent());
        // },
        submitHandler: function (f) {
            if (!form.attr("checkSubmitFlag")) {
                form.attr("checkSubmitFlag", true);
                form.ajaxSubmit(options);
            }
        }
    });
    return false; // 阻止表单默认提交
}

// 表单窗口ajax 提交
function tg_formWinAjaxSubmit(formModalId, formId, rules, messages, sucCallbackFun, failCallbackFun) {
    var formModal = $("#" + formModalId);
    var form = $("#" + formId);
    var callbackFun = function () {
        tg_alertSuccess();
        form[0].reset();
        formModal.modal("hide");
        formModal.removeData("bs.modal");
        if (sucCallbackFun) {
            sucCallbackFun(); // 回调函数
        }
    };
    tg_baseFormAjaxSubmit(formId, rules, messages, callbackFun, failCallbackFun);
}

// 简单Ajax Post操作数据
function tg_simpleAjaxPost(url, parms, sucCallbackFun, failCallbackFun, type) {
    $.ajax({
        url: url,
        type: type ? type : 'POST',
        data: parms,
        error: function () {
            tg_alertError();
        },
        success: function (d) {
            var result = $.parseJSON(d);
            if (result.success) {
                if (sucCallbackFun) {
                    sucCallbackFun(d); // 回调函数
                } else {
                    tg_alertSuccess();
                }
            } else {
                if (failCallbackFun) {
                    failCallbackFun(d); // 回调函数
                } else {
                    tg_alertError('操作失败', result.msg);
                }
            }
        }
    });
}

// 删除
function tg_dleteItem(url, sucCallbackFun, failCallbackFun, type) {
    var okCallbackFun = function () {
        tg_simpleAjaxPost(url, null, sucCallbackFun, failCallbackFun, type);
    };
    if (url.substr(0, url.lastIndexOf("/")) == "/clbs/m/infoconfig/infoinput" ||
        url.substr(0, url.lastIndexOf("/")) == "/clbs/m/functionconfig/fence/bindfence" ||
        url.substr(0, url.lastIndexOf("/")) == "/clbs/v/workhourmgt/vbbind" ||
        url.substr(0, url.lastIndexOf("/")) == "/clbs/v/oilmgt/fluxsensorbind" ||
        url.substr(0, url.lastIndexOf("/")) == "/clbs/v/oilmassmgt/oilvehiclesetting" ||
        url.substr(0, url.lastIndexOf("/")) == "/clbs/v/workhourmgt/workhoursetting")
        tg_confirmDialog(null, "您确定要解除此绑定关系吗？", okCallbackFun);
    else if (url.indexOf("/clbs/adas/standard/param/delete") != -1 || url.indexOf("/clbs/r/riskManagement/DefineSettings/delete_") != -1) {
        tg_confirmDialog(null, "确定要恢复默认值，请谨慎下手！", okCallbackFun);
    } else {
        tg_confirmDialog(null, "删掉就没啦，请谨慎下手！", okCallbackFun);
    }
}

//批量删除
function tg_dleteItems(url, parms, sucCallbackFun, failCallbackFun, type) {
    var okCallbackFun = function () {
        tg_simpleAjaxPost(url, parms, sucCallbackFun, failCallbackFun, type);
    };
    if (url.substr(0, url.lastIndexOf("/")) == "/clbs/m/infoconfig/infoinput" ||
        url.substr(0, url.lastIndexOf("/")) == "/clbs/m/functionconfig/fence/bindfence" ||
        url.substr(0, url.lastIndexOf("/")) == "/clbs/v/workhourmgt/vbbind" ||
        url.substr(0, url.lastIndexOf("/")) == "/clbs/v/oilmgt/fluxsensorbind" ||
        url.substr(0, url.lastIndexOf("/")) == "/clbs/v/oilmassmgt/oilvehiclesetting" ||
        url.substr(0, url.lastIndexOf("/")) == "/clbs/v/obdManager/obdManagerSetting" ||
        url.substr(0, url.lastIndexOf("/")) == "/clbs/v/workhourmgt/workhoursetting")
        tg_confirmDialog(null, "您确定要解除所选绑定关系吗？", okCallbackFun);
    else if (url.indexOf("/clbs/realTimeVideo/videoSetting/deletemore") != -1 ||
        url.indexOf("/clbs/adas/standard/param/delete") != -1 ||
        url.indexOf("/clbs/r/riskManagement/DefineSettings/deletemore") != -1 ||
        url.indexOf("/clbs/r/riskManagement/DefineSettings/delete_") != -1) {
        tg_confirmDialog(null, "您确定要批量恢复所选数据吗？", okCallbackFun);
    } else
        tg_confirmDialog(null, "删掉就没啦，请谨慎下手！", okCallbackFun);
}

// 修改是否可用
function tg_changeEnabled(c, id, enableUrl, disableUrl, sucCallbackFun, type) {
    if (c.checked) { // 原来禁用 现在启用
        // 操作失败还原按钮状态
        var failCallbackFun1 = function () {
            // $(c).trigger("click");
            c.checked = false;
            $(c).bootstrapToggle('destroy');
            $(c).bootstrapToggle();
        };
        // 确定操作
        var okCallbackFun1 = function () {
            tg_simpleAjaxPost(enableUrl, null, sucCallbackFun, failCallbackFun1, type);
        };
        tg_confirmDialog(null, "您确定要启用该条数据吗？", okCallbackFun1, failCallbackFun1);
    } else { // 原来启用 现在停用
        // 操作失败还原按钮状态
        var failCallbackFun = function () {
            c.checked = true;
            $(c).bootstrapToggle('destroy');
            $(c).bootstrapToggle();
        };
        // 确定操作
        var okCallbackFun = function () {
            tg_simpleAjaxPost(disableUrl, null, sucCallbackFun, failCallbackFun, type);
        };
        tg_confirmDialog(null, "您确定要停用该条数据吗？", okCallbackFun, failCallbackFun);
    }
}

// 创建表格
var myDataTable;
var pageNumber = null;
var setPageNumber = null;

function tg_createTable(tg_table, option) {
    layer.load(2);
    var pageable = tg_table.pageable;
    var lengthChange = true;
    var info = true;
    if (!pageable) {
        lengthChange = false;
        info = false;
    }
    if (setPageNumber != null) {
        lengthChange = setPageNumber;
    }
    var pagingType = tg_table.pagingType ? tg_table.pagingType : 'simple_numbers';
    myDataTable = $('#' + tg_table.dataTableDiv).DataTable({
        // 语言
        "language": {
            "search": "搜索:",
            "processing": "处理中...",
            "loadingRecords": "加载中...",
            "lengthMenu": "每页 _MENU_ 条记录",
            "info": "第 _START_ 至 _END_ 条记录，共 _TOTAL_ 条",
            "infoEmpty": "当前显示0到0条，共0条记录",
            "infoFiltered": "(共 _MAX_ 条)",
            "emptyTable": "我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？",
            "zeroRecords": "我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？",
            "paginate": {
                "first": "首页",
                "previous": "上一页",
                "next": "下一页",
                "last": "末页"
            }
        },
        "dom": "t" + "<'row'<'col-md-2 col-sm-12 col-xs-12 noPadding'l><'col-md-4 col-sm-12 col-xs-12 noPadding'i><'col-md-6 col-sm-12 col-xs-12 noPadding'p>>",
        /* "scrollX": true,
         "bAutoWidth": false,  //是否自适应宽度*/
        "searching": false, // 搜索
        // 分页相关
        "paging": pageable,
        "destroy": true,
        "pagingType": pagingType, // 分页样式
        "lengthChange": lengthChange, // 切换每页数据大小
        "info": info,
        "pageLength": pageNumber == null ? 10 : pageNumber, // 默认每页数据量
        "lengthMenu": tg_table.lengthMenu ? tg_table.lengthMenu : [5, 10, 20, 50, 100, 200],
        "ordering": false, // 禁用排序
        // 服务端
        "processing": false,
        "serverSide": true,
        "drawCallback": function (settings) {
            layer.closeAll('loading');
        },
        "ajax": {
            "url": tg_table.listUrl,
            // "type": "POST", // post方式请求
            "type": tg_table.type, // url请求方式
            "data": tg_table.ajaxDataParamFun,
            beforeSend: function () {
                layer.load(2);
            }, //发送请求
            "complete": function (r) {
                layer.closeAll('loading');
                if (r.responseText.indexOf("<form id=\"loginForm") > 0) {
                    window.location.replace("/clbs/login?type=expired");
                    return;
                }
                if (tg_table.sync_address) {
                    //如果地址为空,就执行异步查询地址
                    var riskDisposeRecords = r.responseJSON.records;
                    var formattedAddress;
                    for (var i = 0, n = riskDisposeRecords.length; i < n; i++) {
                        formattedAddress = riskDisposeRecords[i].formattedAddress;
                        if (formattedAddress == null || formattedAddress === '') {
                            setTimeout((function (j) {
                                formatted_address(tg_table.dataTableDiv, riskDisposeRecords[j], tg_table.address_index, j + 1);
                            })(i), 30);
                        }
                    }
                }
            },
            //  "error":error,
            //"dataSrc" : "records"
            "dataSrc": function (json) {
                if (!json.success) {
                    if (json.message) {
                        layer.msg(json.message);
                    } else {
                        layer.msg("系统响应异常，请稍后再试或联系管理员！");
                    }
                    return [];
                }

                if (tg_table.ajax_callback != '') {
                    tg_table.ajax_callback(json);
                }

                if (tg_table.dataTableDiv == 'dataTableBind') {
                    if ($('#TabCarBox').hasClass('active')) {
                        var dataLength = json.records.length;
                        var wHeight = $(window).height();
                        if (dataLength == 0) {
                            $("#MapContainer").animate({
                                'height': (wHeight - 80 - 44 - 220) + 'px'
                            });
                            $('#bingListClick i').attr('class', 'fa fa-chevron-down');
                        } else {
                            $("#MapContainer").animate({
                                'height': (wHeight - 80 - dataLength * 46 - 220) + 'px'
                            });
                            $('#bingListClick i').attr('class', 'fa fa-chevron-down');
                        }
                        ;
                    }
                    ;
                }
                ;
                //sync_address 判断这个条件防止风险处置记录重复发送
                if (tg_table.getAddress && json.records && !tg_table.sync_address) {
                    setTimeout(function () {
                        geocoder_CallBack(tg_table.dataTableDiv, json.records, tg_table.address_index);
                    }, 30);
                }
                ;
                return json.records
            }
        },
        "columnDefs": tg_table.columnDefs,
        "columns": tg_table.columns
    });
    // 把dataTable赋给tg_table
    tg_table.dataTable = myDataTable;
    // 渲染事件
    myDataTable.on('draw', function (e, settings, json) {
        // 修改是否可用
        if (tg_table.enabledChange) {
            $('.js-switch').bootstrapToggle();
        }
        // 第一列索引列
        if (tg_table.showIndexColumn) {
            var info = myDataTable.page.info();
            myDataTable.column(0, {
                search: 'applied',
                order: 'applied'
            }).nodes().each(function (cell, i) {
                cell.innerHTML = info.start + i + 1;
            });
        }
        // 回调
        if (tg_table.drawCallbackFun) {
            tg_table.drawCallbackFun(e, settings, json);
        }
    });
    var columnsDiv = option.columnsDiv;
    var toggleNode = $('.toggle-vis');
    if (columnsDiv) {
        toggleNode = $(columnsDiv + ' .toggle-vis');
    }
    //显示隐藏列
    toggleNode.on('change', function (e) {
        e.preventDefault();
        var column = myDataTable.column($(this).attr('data-column'));
        column.visible(!column.visible());
        // $(".keep-open").addClass("open");
        $(this).parent().parent().parent().parent().addClass("open");
    });
    //权限显示
    var flag = $('#permission').val();
    if (flag == "false") {
        var hasText = $("#" + tg_table.dataTableDiv + " thead tr th").eq(1).html()
        if (hasText != '操作设置' && hasText.indexOf("checkbox") == -1) {
            return;
        }
        if (hasText == '操作设置') { //判断第二列是不是操作设置,如果是的话只隐藏这一列
            var columnTr = myDataTable.column(1);
            columnTr.visible(!columnTr.visible());
            return;
        }
        for (var i = 1; i < 3; i++) {
            var columnTr = myDataTable.column(i);
            columnTr.visible(!columnTr.visible());
        }
    }
    /*$('#AddDelete').click(function() {

    });*/
}

// 创建公共表格
var TG_Tabel = {
    createNew: function (option) {
        if (option.pageNumber != undefined) {
            pageNumber = option.pageNumber;
        }

        if (option.setPageNumber != undefined) {
            setPageNumber = option.setPageNumber;
        }

        var tg_table = {};
        tg_table.listUrl = option.listUrl; // 请求url
        tg_table.pagingType = option.pagingType; // 分页样式
        tg_table.editUrl = option.editUrl; // 修改url
        tg_table.detailUrl = option.detailUrl; //详情
        tg_table.deleteUrl = option.deleteUrl; // 删除url
        tg_table.deletemoreUrl = option.deletemoreUrl; //批量删除url
        tg_table.enableUrl = option.enableUrl; // 启用url
        tg_table.disableUrl = option.disableUrl; // 停用
        tg_table.columnDefs = option.columnDefs; // 列定义
        tg_table.columns = option.columns; // 列
        tg_table.lengthMenu = option.lengthMenu;
        tg_table.ajaxDataParamFun = option.ajaxDataParamFun; // 列
        tg_table.dataTableDiv = option.dataTableDiv; // 渲染表格的div
        tg_table.showIndexColumn = option.showIndexColumn; // 是否显示第一列的索引列
        tg_table.pageable = option.pageable; // 是否分页
        tg_table.enabledChange = option.enabledChange; // 可用状态修改
        tg_table.suffix = !option.suffix ? '.gsp' : option.suffix; // 后缀，默认.gsp
        tg_table.getAddress = option.getAddress == undefined ? false : option.getAddress; //是否逆地理编码
        tg_table.address_index = option.address_index == undefined ? '' : option.address_index;
        tg_table.sync_address = option.sync_address == undefined ? '' : option.sync_address;
        tg_table.type = option.type == undefined ? 'POST' : option.type;
        tg_table.drawCallbackFun = option.drawCallbackFun == undefined ? '' : option.drawCallbackFun; //是否回调
        tg_table.ajax_callback = option.ajaxCallBack == undefined ? '' : option.ajaxCallBack;

        // 成功后回调
        var drawCallbackFun = function () {
            if (option.drawCallbackFun) {
                option.drawCallbackFun();
            }
        };
        tg_table.drawCallbackFun = drawCallbackFun;
        // 初始化
        tg_table.init = function () {
            tg_createTable(tg_table, option); // 创建表格
        };
        // checked 取消
        tg_table.checkedCancel = function () {
            //取消全选勾
            $("#checkAll").prop('checked', false);
            $("#tableCheckAll").prop('checked', false);
            $("input[name=subChk]").prop("checked", false);
        }
        // 刷新
        tg_table.refresh = function () {
            tg_table.checkedCancel();
            tg_table.dataTable.draw(false); // 重新加载数据
        };
        // 重新渲染表格
        tg_table.requestData = function () {
            tg_table.checkedCancel();
            tg_table.dataTable.draw(true); // 重新加载数据
        };
        // 过滤
        tg_table.filter = function () {
            tg_table.refresh(); // 重新加载数据
        };
        // 新增
        tg_table.add = function (windowId, formId, rules, messages) {
            tg_formWinAjaxSubmit(windowId, formId, rules, messages, tg_table.requestData);
        };
        //修改
        tg_table.edit = function (windowId, formId, rules, messages) {
            tg_formWinAjaxSubmit(windowId, formId, rules, messages, tg_table.filter);
        };
        // 删除
        tg_table.deleteItem = function (id) {
            var deleteUrlPath = tg_table.deleteUrl + id + tg_table.suffix;
            var allLen = $("#" + tg_table.dataTableDiv + " tbody").children('tr').length;
            var currPage = parseInt($("#" + tg_table.dataTableDiv + "_wrapper").find(".paginate_button.active a").text());

            var sucCallbackfun = function (data) {
                if (data.msg) {
                    layer.msg(data.msg);
                }
                tg_alertSuccess();
                tg_table.refresh();
                if (allLen == 1 && currPage != 1) {
                    tg_table.dataTable.page(currPage - 2).draw(false);
                }
            };
            var failCallbackFun = function (data) {
                var data = JSON.parse(data);
                if (data.msg) {
                    tg_alertError('操作失败', data.msg);
                }
            };

            tg_dleteItem(deleteUrlPath, sucCallbackfun, failCallbackFun, tg_table.type);
        };
        tg_table.deleteItemTwo = function (id) {
            var deleteUrlPath = tg_table.deleteUrl + id + tg_table.suffix;
            var sucCallbackfun = function () {
                tg_alertSuccess();
                tg_table.refresh();
            };
            var failCallbackFun = function (data) {
                var data = JSON.parse(data);
                if (data.msg) {
                    tg_alertError('操作失败', data.msg);
                }
            };
            tg_dleteItem(deleteUrlPath, sucCallbackfun, failCallbackFun, tg_table.type);
        };
        //批量删除
        tg_table.deleteItems = function (parms) {
            var thisParams;
            for (key in parms) {
                thisParams = parms[key];
            }
            var deletemoreUrlPath = tg_table.deletemoreUrl;
            var delLen = thisParams.split(',').length;
            var currPage = parseInt($("#" + tg_table.dataTableDiv + "_wrapper").find(".paginate_button.active a").text());
            var allLen = $("#" + tg_table.dataTableDiv + " tbody").children('tr').length;
            var sucCallbackfun = function () {
                tg_alertSuccess();
                //取消全选勾
                $("#" + tg_table.dataTableDiv + " thead input:checkbox").prop('checked', false);

                tg_table.refresh();
                if (currPage != 1 && delLen == allLen) {
                    tg_table.dataTable.page(currPage - 2).draw(false);
                }
            };
            var failCallBackFun = function (d) {
                var result = $.parseJSON(d);
                tg_table.refresh();
                tg_alertError('操作失败', result.msg);
                //取消全选勾
                $("#" + tg_table.dataTableDiv + " thead input:checkbox").prop('checked', false);
            };
            tg_dleteItems(deletemoreUrlPath, parms, sucCallbackfun, failCallBackFun, tg_table.type);
        };

        // 单个解除
        tg_table.relieveItem = function (id) {
            var deleteUrlPath = tg_table.deleteUrl + id + tg_table.suffix;
            var sucCallbackfun = function () {
                tg_alertSuccess();
                tg_table.refresh();
            };
            var failCallbackFun = function (data) {
                var data = JSON.parse(data);
                if (data.msg) {
                    tg_alertError('操作失败', data.msg);
                }
            };

            tg_dleteItem(deleteUrlPath, sucCallbackfun, failCallbackFun, tg_table.type);
        };
        //批量解除
        tg_table.relieveItems = function (parms) {
            var deletemoreUrlPath = tg_table.deletemoreUrl;
            var sucCallbackfun = function () {
                tg_alertSuccess();
                //取消全选勾
                $("#" + tg_table.dataTableDiv + " input:checkbox").prop('checked', false);

                tg_table.refresh();
            };
            var failCallBackFun = function (d) {
                var result = $.parseJSON(d);
                if (result.msg) {
                    layer.msg(result.msg);
                }
                tg_table.refresh();
                tg_alertError('操作失败', result.msg);
            };
            tg_dleteItems(deletemoreUrlPath, parms, sucCallbackfun, failCallBackFun, tg_table.type);
        };
        // 报警参数设置批量删除
        tg_table.deleteAlarmSettingsItems = function (parms, errorcallBack) {
            var deletemoreUrlPath = tg_table.deletemoreUrl;
            var sucCallbackfun = function () {
                var checkedList = new Array();
                var settingUrl = settingMoreUrl.replace("{id}.gsp", checkedList.toString() + ".gsp?deviceType=0");
                $("#settingMoreBtn").attr("href", settingUrl);
                tg_alertSuccess();
                tg_table.refresh();
            };
            var failCallbackFun = function (data) {
                var data = JSON.parse(data);
                if (data.msg) {
                    tg_alertError('操作失败', data.msg);
                } else if (errorcallBack) {
                    errorcallBack();
                }
            };
            tg_dleteItems(deletemoreUrlPath, parms, sucCallbackfun, failCallbackFun, tg_table.type);
        };
        // 报警参数设置删除
        tg_table.deleteAlarmSettingsItem = function (id) {
            var deleteUrlPath = tg_table.deleteUrl + id + tg_table.suffix;
            var sucCallbackfun = function () {
                var checkedList = new Array();
                var settingUrl = settingMoreUrl.replace("{id}.gsp", checkedList.toString() + ".gsp?deviceType=0");
                $("#settingMoreBtn").attr("href", settingUrl);
                tg_alertSuccess();
                tg_table.refresh();
            };
            var failCallbackFun = function (data) {
                var data = JSON.parse(data);
                if (data.msg) {
                    tg_alertError('操作失败', data.msg);
                }
            };

            tg_dleteItem(deleteUrlPath, sucCallbackfun, failCallbackFun, tg_table.type);
        };

        // 修改是否可用
        tg_table.changeEnabled = function (c, id) {
            var enableUrlPath = tg_table.enableUrl + id + tg_table.suffix;
            var disableUrlPath = tg_table.disableUrl + id + tg_table.suffix;
            tg_changeEnabled(c, id, enableUrlPath, disableUrlPath, tg_table.type);
        };
        return tg_table;
    }
};
// 锁定成功，弹出解锁对话框
var lockSucPrompt = function (logoutUrl, unlockUrl) {
    layer.prompt({
        formType: 1,
        closeBtn: 0,
        btn: ['解除锁定', '重新登录'], // 可以无限个按钮
        title: "系统已锁定，请输入密码解锁！",
        btn2: function () {
            window.location.href = logoutUrl;
            return false;
        },
    }, function (value, index, elem) {
        if (value) {
            if (value.length < 6) {
                tg_alertError("解锁失败", "密码太短，请重新输入！");
            } else if (value.length > 25) {
                tg_alertError("解锁失败", "密码太长，请重新输入！");
            } else {
                var parms = {
                    userPass: value
                };
                tg_simpleAjaxPost(unlockUrl, parms, function () {
                    tg_alertSuccess("解锁成功", "密码正确，解锁成功！");
                    layer.close(index);
                }, function () {
                    tg_alertError("解锁失败", "密码不正确，请重新输入！");
                    return false;
                }, tg_table.type);
            }
        }
    });
};

// 锁定系统屏幕
function tg_lock(lockUrl, logoutUrl, unlockUrl) {
    // 成功回调操作
    var sucCallbackFun = function () {
        lockSucPrompt(logoutUrl, unlockUrl);
    };
    tg_simpleAjaxPost(lockUrl, null, sucCallbackFun, null, tg_table.type);
}

// 检查锁定系统屏幕
function tg_checkLock(checkLockUrl, logoutUrl, unlockUrl) {
    // 锁定回调操作
    var sucCallbackFun = function () {
        lockSucPrompt(logoutUrl, unlockUrl);
    };
    // 未锁定回调操作
    var falseCallbackFun = function () {
    };
    tg_simpleAjaxPost(checkLockUrl, null, sucCallbackFun, falseCallbackFun, tg_table.type);
}

//后台分页逆地理编码
var lngLatIndex = 0;

function geocoder_CallBack(this_id, msg, address_index) {
    lngLatIndex = 0;
    var addressLngLatArray = [];
    for (var i = 0, len = msg.length; i < len; i++) {
        var addressMsg = [];
        var address = msg[i].longtitude + "," + msg[i].latitude;
        //经纬度正则表达式
        var Reg = /^(180\.0{4,7}|(\d{1,2}|1([0-7]\d))\.\d{4,20})(,)(90\.0{4,8}|(\d|[1-8]\d)\.\d{4,20})$/;
        if (address != null && Reg.test(address)) {
            addressMsg = [msg[i].longtitude, msg[i].latitude];
        } else {
            addressMsg = ["124.411991", "29.043817"];
        }
        addressLngLatArray.push(addressMsg);
    }
    ;

    if (addressLngLatArray.length > 0) {
        getAddress(this_id, addressLngLatArray, address_index);
    }
};

function formatted_address(this_id, msg, address_index, index) {

    var addressMsg = [];
    var address = msg.longtitude + "," + msg.latitude;
    //经纬度正则表达式
    var Reg = /^(180\.0{4,7}|(\d{1,2}|1([0-7]\d))\.\d{4,20})(,)(90\.0{4,8}|(\d|[1-8]\d)\.\d{4,20})$/;
    if (address != null && Reg.test(address)) {
        addressMsg = [msg.longtitude, msg.latitude];
    } else {
        addressMsg = ["124.411991", "29.043817"];
    }
    $.ajax({
        type: "post",
        url: "/clbs/v/monitoring/getAddress",
        dataType: "json",
        async: true,
        data: {
            lnglatXYs: addressMsg
        },
        traditional: true,
        timeout: 30000,
        success: function (data) {
            if (data.success == undefined) {
                var this_address = data;
                if (this_address == "AddressNull") {
                    var geocoder = new AMap.Geocoder({
                        radius: 1000,
                        extensions: "base"
                    });
                    geocoder.getAddress(addressMsg);
                    AMap.event.addListener(geocoder, "complete", function (GeocoderResult) {
                        var addressValue;
                        if (GeocoderResult.info == 'NO_DATA') {
                            addressValue = '未定位';
                        } else {
                            addressValue = GeocoderResult.regeocode.formattedAddress;
                            var addressParticulars = getaddressParticulars(GeocoderResult, addressMsg[0], addressMsg[1]);
                            $.ajax({
                                type: "POST",
                                url: "/clbs/v/monitoring/setAddress",
                                dataType: "json",
                                async: true,
                                data: {
                                    "addressNew": addressParticulars
                                },
                                traditional: false,
                                timeout: 30000,
                            });
                        }
                        ;
                        $("#" + this_id).children("tbody").children("tr:nth-child(" + index + ")").children("td:nth-child(" + address_index + ")").text(addressValue);
                    });
                } else {
                    var addressValue = this_address;
                    $("#" + this_id).children("tbody").children("tr:nth-child(" + index + ")").children("td:nth-child(" + address_index + ")").text(addressValue);
                }
            } else {
                var addressValue = '未定位';
                $("#" + this_id).children("tbody").children("tr:nth-child(" + index + ")").children("td:nth-child(" + address_index + ")").text(addressValue);
            }
        }
    })
};

function getAddress(this_id, msg, address_index) {
    var lngLatValue = msg[lngLatIndex];
    $.ajax({
        type: "post",
        url: "/clbs/v/monitoring/getAddress",
        dataType: "json",
        async: true,
        data: {
            lnglatXYs: lngLatValue
        },
        traditional: true,
        timeout: 30000,
        success: function (data) {
            if (data.success == undefined) {
                var this_address = data;
                if (this_address == "AddressNull") {
                    var geocoder = new AMap.Geocoder({
                        radius: 1000,
                        extensions: "base"
                    });
                    geocoder.getAddress(lngLatValue);
                    AMap.event.addListener(geocoder, "complete", function (GeocoderResult) {
                        lngLatIndex++;
                        var addressValue;
                        if (GeocoderResult.info == 'NO_DATA') {
                            addressValue = '未定位';
                        } else {
                            addressValue = GeocoderResult.regeocode.formattedAddress;
                            var addressParticulars = getaddressParticulars(GeocoderResult, lngLatValue[0], lngLatValue[1]);
                            $.ajax({
                                type: "POST",
                                url: "/clbs/v/monitoring/setAddress",
                                dataType: "json",
                                async: true,
                                data: {
                                    "addressNew": addressParticulars
                                },
                                traditional: false,
                                timeout: 30000,
                            });
                        }
                        ;
                        $("#" + this_id).children("tbody").children("tr:nth-child(" + lngLatIndex + ")").children("td:nth-child(" + address_index + ")").text(addressValue);
                        if (lngLatIndex < msg.length) {
                            getAddress(this_id, msg, address_index);
                        }
                        ;
                    });
                } else {
                    lngLatIndex++;
                    var addressValue = this_address;
                    $("#" + this_id).children("tbody").children("tr:nth-child(" + lngLatIndex + ")").children("td:nth-child(" + address_index + ")").text(addressValue);
                    if (lngLatIndex < msg.length) {
                        getAddress(this_id, msg, address_index);
                    }
                    ;
                }
                ;
            } else {
                lngLatIndex++;
                var addressValue = '未定位';
                $("#" + this_id).children("tbody").children("tr:nth-child(" + lngLatIndex + ")").children("td:nth-child(" + address_index + ")").text(addressValue);
                if (lngLatIndex < msg.length) {
                    getAddress(this_id, msg, address_index);
                }
                ;
            }
            ;
        }
    })
};

function getaddressParticulars(AddressNew, longitude, latitude) {
    var addressParticulars = {
        "longitude": longitude.substring(0, longitude.lastIndexOf(".") + 4),
        "latitude": latitude.substring(0, latitude.lastIndexOf(".") + 4),
        "adcode": AddressNew.regeocode.addressComponent.adcode, //区域编码
        "building": AddressNew.regeocode.addressComponent.building, //所在楼/大厦
        "buildingType": AddressNew.regeocode.addressComponent.buildingType,
        "city": AddressNew.regeocode.addressComponent.city,
        "cityCode": AddressNew.regeocode.addressComponent.citycode,
        "district": AddressNew.regeocode.addressComponent.district, //所在区
        "neighborhood": AddressNew.regeocode.addressComponent.neighborhood, //所在社区
        "neighborhoodType": AddressNew.regeocode.addressComponent.neighborhoodType, //社区类型
        "province": AddressNew.regeocode.addressComponent.province, //省
        "street": AddressNew.regeocode.addressComponent.street, //所在街道
        "streetNumber": AddressNew.regeocode.addressComponent.streetNumber, //门牌号
        "township": AddressNew.regeocode.addressComponent.township, //所在乡镇
        "crosses": "",
        "pois": "",
        "roads": AddressNew.regeocode.roads.name, //道路名称
        "formattedAddress": AddressNew.regeocode.formattedAddress, //格式化地址
    };
    return JSON.stringify(addressParticulars);
};
//此方法初始化页面表格，与reloadData()方法互用，用于平台初始化table，只传入数组，
var getTable;

function getTable(id) {
    getTable = $('#' + id + '').DataTable({
        "destroy": true,
        "dom": 'tiprl', // 自定义显示项
        "lengthChange": true, // 是否允许用户自定义显示数量
        "bPaginate": true, // 翻页功能
        "bFilter": false, // 列筛序功能
        "searching": true, // 本地搜索
        "ordering": false, // 排序功能
        "Info": true, // 页脚信息
        "autoWidth": true, // 自动宽度
        "stripeClasses": [],
        "pageLength": 10,
        "lengthMenu": [5, 10, 20, 50, 100, 200],
        "pagingType": "simple_numbers", // 分页样式
        "dom": "t" + "<'row'<'col-md-2 col-sm-12 col-xs-12 noPadding noPadding'l><'col-md-4 col-sm-12 col-xs-12 noPadding'i><'col-md-6 col-sm-12 col-xs-12 noPadding'p>>",
        "oLanguage": { // 国际语言转化
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
}

//此方法区别于tg_table的ajax请求数据，而是页面请求直接传入数组list，用于页面多表格，或是单表格创建datatable，
function reloadData(dataList) {
    var currentPage = getTable.page();
    getTable.clear();
    getTable.rows.add(dataList);
    getTable.page(currentPage).draw(false);
}