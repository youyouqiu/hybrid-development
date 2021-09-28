(function (window, $) {
    var selectTreeId = '';
    var selectTreepId = "";
    var selectTreeType = '';

    //显示隐藏列
    var menu_text = "";
    var table = $("#dataTable tr th:gt(1)");
    var menu_text1 = '';
    var table1 = $("#dataTables tr th:gt(1)");
    //单选
    var subChk = $("input[name='subChk']");
    var subChkThree = $("input[name='subChkThree']");

    var adanceFlag = false; // 是否是高级查询

    terminalManagement = {
        init: function () {
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>";
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" />" + table[i].innerHTML + "</label></li>"
            }
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
                        var roleUrlPre = /*[[@{/c/user/roleList_{id}.gsp}]]*/ 'url';
                        var result = '';
                        //修改按钮
                        result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                        //删除按钮
                        result += '<button type="button" onclick="myTable.deleteItem(\'' +
                            row.id +
                            '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
                        return result;
                    }
                }, {
                    "data": "deviceNumber",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return data;
                        } else {
                            return "";
                        }
                    }
                }, {
                    "data": "groupName",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return data;
                        } else {
                            return "";
                        }
                    }
                }, {
                    "data": "deviceType",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return getProtocolName(data);
                    }

                }, {
                    "data": "terminalManufacturer",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return html2Escape(data)
                        } else {
                            return "";
                        }
                    }
                }, {
                    "data": "terminalType",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return html2Escape(data)
                        } else {
                            return "";
                        }
                    }
                }, {
                    "data": "functionalType",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == "1") {
                            return "简易型车机";
                        } else if (data == "2") {
                            return "行车记录仪";
                        } else if (data == "3") {
                            return "对讲设备";
                        } else if (data == "4") {
                            return "手咪设备";
                        } else if (data == "5") {
                            return "超长待机设备";
                        } else if (data == "6") {
                            return "定位终端";
                        } else {
                            return "";
                        }
                    }

                }, {
                    "data": "deviceName",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return html2Escape(data)
                        } else {
                            return "";
                        }
                    }
                }, {
                    "data": "manufacturerId",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return html2Escape(data)
                        } else {
                            return "";
                        }
                    }
                }, {
                    "data": "deviceModelNumber",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return html2Escape(data)
                        } else {
                            return "";
                        }
                    }
                }, {
                    "data": "macAddress",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data ? data : '';
                    }
                }, {
                    "data": "manuFacturer",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return html2Escape(data)
                        } else {
                            return "";
                        }
                    }
                }, {
                    "data": "barCode",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return html2Escape(data)
                        } else {
                            return "";
                        }
                    }
                }, {
                    "data": "isStart",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == 0) {
                            return '停用';
                        } else if (data == 1) {
                            return '启用';
                        } else {
                            return "";
                        }
                    }
                }, {
                    "data": "brand",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return data != "" ? data : "";
                        } else if (row.peopleNumber != null) {
                            return row.peopleNumber != "" ? row.peopleNumber : "";
                        } else if (row.thingNumber != null) {
                            return row.thingNumber != "" ? row.thingNumber : "";
                        } else {
                            return "";
                        }

                    }

                }, {
                    "data": "installTimeStr",
                    "class": "text-center",
                    render: function (data) {
                        return (data != null && data.length >= 10) ? data.substr(0, 10) : "";
                    }
                }, {
                    "data": "procurementTimeStr",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return data;
                        } else {
                            return "";
                        }
                    }

                }, {
                    "data": "createDataTimeStr",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return data;
                        } else {
                            return "";
                        }
                    }

                }, {
                    "data": "updateDataTimeStr",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return data;
                        } else {
                            return "";
                        }
                    }
                }, {
                    "data": "installCompany",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return data;
                        } else {
                            return "";
                        }
                    }
                },
                {
                    "data": "contacts",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return data;
                        } else {
                            return "";
                        }
                    }
                },
                {
                    "data": "telephone",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return data;
                        } else {
                            return "";
                        }
                    }
                },
                {
                    "data": "complianceRequirements",
                    "class": "text-center",
                    render: function (result) {
                        if (result == 1) {
                            return "是"
                        } else if (result == 0) {
                            return "否"
                        } else {
                            return ""
                        }
                    }
                },
                {
                    "data": "remark",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return html2Escape(data)
                        } else {
                            return "";
                        }
                    }
                }
            ];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
                d.groupId = selectTreeId;
                // d.groupName = selectTreeId;
                // d.groupType = selectTreeType;
                if (adanceFlag) { // 高级查询
                    d.deviceType = $('#listDeviceType').val();
                    d.terminalManufacturer = $('#listTerminalManufacturer').val();
                    d.terminalType = $('#listTerminalType').val();
                    d.isStart = $('#listState').val(); //启停状态
                    // adanceFlag = false;
                }
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/m/basicinfo/equipment/device/list',
                editUrl: '/clbs/m/basicinfo/equipment/device/edit_',
                deleteUrl: '/clbs/m/basicinfo/equipment/device/delete_',
                deletemoreUrl: '/clbs/m/basicinfo/equipment/device/deletemore',
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
            terminalManagement.modelTableInit();
        },
        // leftTree: function () {
        //     var treeSetting = {
        //         async: {
        //             url: "/clbs/m/basicinfo/enterprise/assignment/assignmentTree",
        //             tyoe: "post",
        //             enable: true,
        //             autoParam: ["id"],
        //             dataType: "json",
        //             otherParam: {  // 是否可选  Organization
        //                 "isOrg": "1"
        //             },
        //             dataFilter: terminalManagement.groupAjaxDataFilter
        //         },
        //         view: {
        //             selectedMulti: false,
        //             nameIsHTML: true,
        //             fontCss: setFontCss_ztree
        //         },
        //         data: {
        //             simpleData: {
        //                 enable: true
        //             }
        //         },
        //         callback: {
        //             onClick: vehicleList.zTreeOnClick
        //         }
        //     };
        //     $.fn.zTree.init($("#treeDemo"), treeSetting, null);
        // },
        //终端型号表格初始化
        modelTableInit: function () {
            menu_text1 += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table1[0].innerHTML + "</label></li>";
            for (var i = 1; i < table1.length; i++) {
                menu_text1 += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" />" + table1[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-text1").html(menu_text1);

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
                        var editUrlPath = modelTable.editUrl + row.id + ".gsp"; //修改地址
                        var result = '';
                        if (row.id != "default") {
                            result = '<button href="' + editUrlPath + '" type="button" data-target="#commonWin" data-toggle="modal" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;<button type="button" onclick="terminalManagement.deleteData(\'' + row.id + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
                        } else {
                            result = '<button disabled  type="button" class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>修改</button>&ensp;<button disabled type="button"  class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>删除</button>';
                        }
                        return result;
                    }
                }, {
                    "data": "terminalManufacturer", // 终端厂商
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return data
                        } else {
                            return "";
                        }
                    }
                }, {
                    "data": "terminalType", // 终端型号
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return data
                        } else {
                            return "";
                        }
                    }
                }, {
                    "data": "supportPhotoFlag", // 是否支持拍照
                    "class": "text-center",
                    render: function (data) {
                        var supportPhotoFlag = "";
                        if (data == 0) {
                            supportPhotoFlag = "否";
                        }
                        if (data == 1) {
                            supportPhotoFlag = "是";
                        }
                        return supportPhotoFlag;
                    }
                }, {
                    "data": "camerasNumber", // 摄像头个数
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return data
                        } else {
                            return "";
                        }
                    }
                }, {
                    "data": "supportDrivingRecorderFlag", // 是否支持行驶记录仪
                    "class": "text-center",
                    render: function (data) {
                        var supportDrivingRecorderFlag = "";
                        if (data == 0) {
                            supportDrivingRecorderFlag = "否";
                        }
                        if (data == 1) {
                            supportDrivingRecorderFlag = "是";
                        }
                        return supportDrivingRecorderFlag;
                    }
                }, {
                    "data": "supportMonitoringFlag", // 是否支持监听
                    "class": "text-center",
                    render: function (data) {
                        var supportMonitoringFlag = "";
                        if (data == 0) {
                            supportMonitoringFlag = "否";
                        }
                        if (data == 1) {
                            supportMonitoringFlag = "是";
                        }
                        return supportMonitoringFlag;
                    }
                }, {
                    "data": "activeSafety", // 是否支持主动安全
                    "class": "text-center",
                    render: function (data) {
                        var activeSafety = "";
                        if (data == 0) {
                            activeSafety = "否";
                        }
                        if (data == 1) {
                            activeSafety = "是";
                        }
                        return activeSafety;
                    }
                }, {
                    "data": "allInOne", // 是否为一体机
                    "class": "text-center",
                    render: function (data) {
                        var allInOne = "";
                        if (data == 0) {
                            allInOne = "否";
                        }
                        if (data == 1) {
                            allInOne = "是";
                        }
                        return allInOne;
                    }
                }, {
                    "data": "supportVideoFlag", // 是否支持视频
                    "class": "text-center",
                    render: function (data) {
                        var supportVideoFlag = "";
                        if (data == 0) {
                            supportVideoFlag = "否";
                        }
                        if (data == 1) {
                            supportVideoFlag = "是";
                        }
                        return supportVideoFlag;
                    }
                },
                {
                    "data": 'audioFormat', //实时流音频格式
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        switch (data) {
                            case 0:
                                return 'ADPCMA';
                            case 2:
                                return 'G726-16K';
                            case 3:
                                return 'G726-24K';
                            case 4:
                                return 'G726-32K';
                            case 5:
                                return 'G726-40K';
                            case 6:
                                return 'G711a';
                            case 7:
                                return 'G711u';
                            case 8:
                                return 'AAC-ADTS';
                        }
                    }
                },
                {
                    "data": 'samplingRate', //实时流采样率
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        switch (data) {
                            case 0:
                                return '8kHz';
                            case 1:
                                return '22.05kHz';
                            case 2:
                                return '44.1kHz';
                            case 3:
                                return '48kHz';
                        }
                    }
                },
                {
                    "data": 'vocalTract', //实时流声道数
                    "class": "text-center",
                    render: function (data) {
                        switch (data) {
                            case 0:
                                return '单声道';
                            case 1:
                                return '双声道';
                        }
                    }
                },
                {
                    "data": 'storageAudioFormat', //存储流音频格式
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        switch (data) {
                            case 0:
                                return 'ADPCMA';
                            case 2:
                                return 'G726-16K';
                            case 3:
                                return 'G726-24K';
                            case 4:
                                return 'G726-32K';
                            case 5:
                                return 'G726-40K';
                            case 6:
                                return 'G711a';
                            case 7:
                                return 'G711u';
                            case 8:
                                return 'AAC-ADTS';
                        }
                    }
                },
                {
                    "data": 'storageSamplingRate', //存储流采样率
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        switch (data) {
                            case 0:
                                return '8kHz';
                            case 1:
                                return '22.05kHz';
                            case 2:
                                return '44.1kHz';
                            case 3:
                                return '48kHz';
                        }
                    }
                },
                {
                    "data": 'storageVocalTract', //存储流声道数
                    "class": "text-center",
                    render: function (data) {
                        switch (data) {
                            case 0:
                                return '单声道';
                            case 1:
                                return '双声道';
                        }
                    }
                },


                {
                    "data": "channelNumber", // 通道号个数
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return data
                        } else {
                            return "";
                        }
                    }
                }
            ];
            //ajax参数
            var ajaxDataParamFunThree = function (d) {
                d.simpleQueryParam = $('#terminalTypeFuzzyParam').val(); //模糊查询
            };
            //表格subSetting
            var subSetting = {
                listUrl: '/clbs/m/basicinfo/equipment/device/terminalTypeList',
                editUrl: '/clbs/m/basicinfo/equipment/device/terminalEditPage_',
                deleteUrl: '/clbs/m/basicinfo/equipment/device/deleteTerminalType_',
                deletemoreUrl: '/clbs/m/basicinfo/equipment/device/batchDeleteTerminalType', // 批量删除
                enableUrl: '/clbs/c/user/enable_',
                disableUrl: '/clbs/c/user/disable_',
                columnDefs: subColumnDefs, //表格列定义
                columns: subColumns, //表格列
                dataTableDiv: 'dataTables', //表格
                ajaxDataParamFun: ajaxDataParamFunThree, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true
            };
            //创建表格
            modelTable = new TG_Tabel.createNew(subSetting);
            //表格初始化
            modelTable.init();

            // 终端型号列表,显示隐藏列
            $('#Ul-menu-text1 .toggle-vis').unbind("change").on('change', function (e) {
                e.preventDefault();
                var column = modelTable.dataTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(this).parent().parent().parent().parent().addClass("open");
            });
            // 终端列表,显示隐藏列
            $('#Ul-menu-text .toggle-vis').unbind("change").on('change', function (e) {
                e.preventDefault();
                var column = myTable.dataTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(this).parent().parent().parent().parent().addClass("open");
            });
        },
        //删除终端型号
        deleteData: function (id) {
            var url = "/clbs/m/basicinfo/equipment/device/deleteTerminalType_" + id + ".gsp";
            // var data = {"id": id};
            layer.confirm(publicDelete, {
                btn: ["确定", "取消"],
                icon: 3,
                title: "操作确认"
            }, function () {
                json_ajax("GET", url, "json", true, null, terminalManagement.deleteTerminalTypeCallBack);
            });
        },
        deleteTerminalTypeCallBack: function (data) {
            if (data.success) {
                layer.closeAll('dialog');
                layer.msg('删除成功！');
                modelTable.requestData();
            } else {
                layer.msg(data.msg);
            }
        },
        delTerminalType: function () {
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
            modelTable.deleteItems({
                'ids': checkedList.toString()
            });
        },
        exportTerminalType: function () {
            if (getRecordsNum() > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var fuzzyParam = $('#terminalTypeFuzzyParam').val(); //模糊查询
            var exportUrl = "/clbs/m/basicinfo/equipment/device/exportTerminalInfo?fuzzyParam=" + fuzzyParam;
            window.location.href = exportUrl;
        },
        //全选
        modelCleckAll: function () {
            $("input[name='subChkThree']").prop("checked", this.checked);
        },
        //单选
        modelSubChkClick: function () {
            $("#checkAll").prop("checked", subChkThree.length == subChkThree.filter(":checked").length ? true : false);
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
        //加载完成后执行
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            myTable.requestData();
        },
        //刷新表格
        refreshModelTable: function () {
            $("#terminalTypeFuzzyParam").val("");
            modelTable.requestData();
        },
        groupListTree: function () {
            var treeSetting = {
                async: {
                    // url: "/clbs/m/basicinfo/enterprise/assignment/assignmentTree",
                    url: '/clbs/m/tree/org/tree',
                    tyoe: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: { // 是否可选  Organization
                        "searchType": "0", //搜索类型 0:企业; 1:分组; 不传展示所有
                        simpleQueryParamL: $("#search_condition").val()
                    },
                    dataFilter: terminalManagement.groupAjaxDataFilter
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
                    onClick: terminalManagement.zTreeOnClick
                }
            };
            $.fn.zTree.init($("#treeDemo"), treeSetting, null);
        },
        //组织树预处理函数
        groupAjaxDataFilter: function (treeId, parentNode, responseData) {
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
            if (treeNode.type == "group") {
                selectTreepId = treeNode.id;
                selectTreeId = treeNode.uuid;
            } else {
                selectTreepId = treeNode.pId;
                selectTreeId = treeNode.id;
            }
            selectTreeType = treeNode.type;
            myTable.requestData();
        },
        // 获取终端厂商
        getTerminalManufacturer: function () {
            var url = "/clbs/m/basicinfo/equipment/device/TerminalManufacturer";
            json_ajax("GET", url, "json", false, null, terminalManagement.TerminalManufacturerCallBack);
        },
        TerminalManufacturerCallBack: function (data) {
            var result = data.obj.result;
            var str = '<option value="">全部</option>';
            for (var i = 0; i < result.length; i++) {
                /*if (result[i] == '[f]F3') {
                    str += '<option selected="selected" value="' + result[i] + '">' + html2Escape(result[i]) + '</option>'
                } else {*/
                str += '<option  value="' + result[i] + '">' + html2Escape(result[i]) + '</option>'
                // }
            }
            $("#listTerminalManufacturer").html(str);
            terminalManagement.getTerminalType($("#listTerminalManufacturer").val());
        },
        // 获取终端型号
        getTerminalType: function (name) {
            var url = "/clbs/m/basicinfo/equipment/device/getTerminalTypeByName";
            json_ajax("POST", url, "json", false, {
                'name': name
            }, terminalManagement.getTerminalTypeCallback);
        },
        getTerminalTypeCallback: function (data) {
            // var vt = 'F3-default';
            var result = data.obj.result;
            var str = '<option value="">全部</option>';
            if (result != null && result != '') {
                for (var i = 0; i < result.length; i++) {
                    /* if (vt == result[i].terminalType) {
                         str += '<option selected="selected" value="' + result[i].id + '">' + html2Escape(result[i].terminalType) + '</option>'
                     } else {*/
                    str += '<option  value="' + result[i].id + '">' + html2Escape(result[i].terminalType) + '</option>'
                    // }
                }
            }
            $("#listTerminalType").html(str);
            $("#listTerminalTypeId").val($("#listTerminalType").val());
        },
        //高级查询条件是否显示
        showAdvancedContent: function () {
            var advancedContent = $("#advanced_content");
            if (advancedContent.is(':visible')) {
                advancedContent.slideUp();
            } else {
                advancedContent.slideDown();
            }
        },
        // 还原高级查询条件
        emptyAdvancedContent: function () {
            $('#listDeviceType,#listTerminalManufacturer,#listTerminalType,#listState').val('');
            terminalManagement.getTerminalType('');
        },

        // 通讯类型初始化
        agreementType() {
            var url = '/clbs/m/connectionparamsset/protocolList';
            var param = {
                "type": 808
            }
            json_ajax("POST", url, "json", false, param, function (data) {
                var data = data.obj;
                for (var i = 0; i < data.length; i++) {
                    $('#listDeviceType').append(
                        "<option value='" + data[i].protocolCode + "'>" + data[i].protocolName + "</option>"
                    );
                }
            })
        }
    };
    $(function () {
        $('input').inputClear();
        terminalManagement.init();
        terminalManagement.getTerminalManufacturer();
        terminalManagement.groupListTree();
        terminalManagement.agreementType();
        // 组织架构模糊搜索
        $("#search_condition").on("input oninput", function () {
            search_ztree('treeDemo', 'search_condition', 'group');
        });
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'search_condition') {
                search_ztree('treeDemo', id, 'group');
            };
        });
        //IE9
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
        $('#search_button').on('click', function () {
            adanceFlag = false;
            myTable.requestData();
        });
        /**
         * 终端型号列表方法
         * */
        //刷新列表
        $("#refreshModelTable").on("click", terminalManagement.refreshModelTable);
        //全选
        $("#checkAll1").bind("click", terminalManagement.modelCleckAll);
        //单选
        subChkThree.bind("click", terminalManagement.modelSubChkClick);
        // 批量删除
        $("#del_model1").bind("click", terminalManagement.delTerminalType);
        // 导出
        $("#exportIdTerminalType").bind("click", terminalManagement.exportTerminalType);
        // 模糊查询
        $('#terminalTypeFuzzyParam').keyup(function (event) {
            if (event.keyCode == 13) {
                modelTable.requestData();
            }
        });

        // 输入框输入类型过滤
        inputValueFilter('#terminalTypeFuzzyParam', /[^A-Za-z0-9_#\*\u4e00-\u9fa5\-]/g);
        inputValueFilter('#simpleQueryParam');

        // 高级查询
        $("#advanced_search").on("click", terminalManagement.showAdvancedContent);
        $("#inquireClickOne").on("click", function () {
            adanceFlag = true;
            myTable.requestData();
        });
        $("#emptyBtn").on("click", terminalManagement.emptyAdvancedContent);
        $('#listTerminalManufacturer').on("change", function () {
            var terminalManufacturerName = $(this).find("option:selected").attr("value");
            terminalManagement.getTerminalType(terminalManufacturerName);
        });

        /**
         * 终端列表方法
         * */
        //全选
        $("#checkAll").bind("click", terminalManagement.cleckAll);
        //单选
        subChk.bind("click", terminalManagement.subChkClick);
        //批量删除
        $("#del_model").bind("click", terminalManagement.delModel);
        //加载完成后执行
        $("#refreshTable").on("click", terminalManagement.refreshTable);
    })
})(window, $)