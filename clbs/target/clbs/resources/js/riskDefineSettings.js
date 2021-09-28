var riskDefineSettings;
(function (window, $) {
    var selectGroupId = '';
    var selectAssignId = '';
    var selectTreepId = '';
    var settingMoreUrl = "/clbs/r/riskManagement/DefineSettings/settingmore_";
    //显示隐藏列
    var menu_text = "";
    var risksendflag = true;

    var dtext = "";
    var adanceFlag = false;
    var terminalCategory = '';
    var sendStatus = '';
    var statusInfo = '';
    var updateTime = null;
    var protocol = '21';// 默认协议中位(2019)
    var heiProtocol = '25';// 黑标协议值
    var LU_PROTOCOL_VALUE = '26' //鲁标
    var XIANG_PROTOCOL_VALUE = '27' //湘标
    var YUE_PROTOCOL_VALUE = '28' //粤标

    var menuTextCache = {}

    riskDefineSettings = {
        //初始化
        init: function () {
            //生成组织架构下的协议显示列
            var url = "/clbs/m/connectionparamsset/getActiveSafetyProtocol";
            json_ajax('GET', url, 'json', false, null, function (data) {
                var data = data.obj;
                for (var i = 0; i < data.length; i++) {
                    var item = data[i];
                    $('#Ul-menu-text-v').append(
                        "<label class='radio-inline' style='margin-left:10px;'><input name=\"deviceCheck\" value=\"" + item.protocolCode + "\" type=\"radio\" class=\"device\" />" + riskDefineSettings.getMenuText(item.protocolCode, item.protocolName) + "</label>"
                    )
                }
            });


            $("input.device[value='21']").prop("checked", true);

            riskDefineSettings.showColSetting(1);
            $('#advancedBox').hide();
            riskDefineSettings.deviceChange('21');
            riskDefineSettings.ztreeInit();

            webSocket.subscribe(headers, "/user/topic/active_security", riskDefineSettings.updataFenceData, null, null);
        },

        getMenuText: function (code, protocolName) {
            if (code == 1) {
                return protocolName + '(中位)';
            }
            return protocolName;

        },

        // 定制显示列
        showColSetting: function () {
            var table = $("#dataTable tr th:gt(1)");//中位标准
            var suTable = $("#suDataTable tr th:gt(1)"); // 苏标
            var publicDataTable = $("#publicDataTable tr th:gt(1)"); //川标 桂标
            var shanDataTable = $("#shanDataTable tr th:gt(1)"); //川标 桂标
            var jiDataTable = $("#jiDataTable tr th:gt(1)"); //冀标
            var huDataTable = $("#huDataTable tr th:gt(1)"); //沪标
            var jingDataTable = $("#jingDataTable tr th:gt(1)"); //京标
            var heiljDataTable = $("#heiDataTable tr th:gt(1)"); //黑标
            var luDataTable = $("#luDataTable tr th:gt(1)"); //鲁标
            var xiangDataTable = $("#xiangDataTable tr th:gt(1)"); //湘标
            var yueDataTable = $("#yueDataTable tr th:gt(1)"); //粤标

            var num = 0;
            menu_text = '';
            menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>";

            //中位
            if (protocol == '1') {
                for (var i = 1; i < table.length; i++) {
                    menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" />" + table[i].innerHTML + "</label></li>";
                }
            }

            switch (protocol) {
                //黑标
                case heiProtocol:
                    table = heiljDataTable;
                    break;
                //川标 桂标
                case '12':
                case '14':
                    table = publicDataTable;
                    break;
                //苏标 中位标准(2019)
                case '15':
                case '21':
                    table = suTable;
                    break;
                //冀标
                case '13':
                    table = jiDataTable;
                    break;
                //浙标 吉标 陕标
                case '16':
                case '17':
                case '18':
                case '19':
                    table = shanDataTable;
                    break;
                case '20':
                    table = huDataTable;
                    break;
                case '24':
                    table = jingDataTable;
                    break;
                case LU_PROTOCOL_VALUE:
                    table = luDataTable; //鲁标
                    break;
                case XIANG_PROTOCOL_VALUE:
                    table = xiangDataTable; //湘标
                    break;
                case YUE_PROTOCOL_VALUE:
                    table = yueDataTable; //粤标
                    break;
                default:
                    break;
            }

            if (!menuTextCache.hasOwnProperty(protocol)) {
                for (var i = 1; i < table.length; i++) {
                    if (i == 5) continue;
                    if (i > 5) {
                        num = i + 1;
                        menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(num) + "\" />" + table[i].innerHTML + "</label></li>";
                    } else {
                        menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" />" + table[i].innerHTML + "</label></li>";
                    }
                }
                menuTextCache[protocol] = menu_text
            } else {
                menu_text = menuTextCache[protocol]
            }

            $("#Ul-menu-text").html(menu_text);
        },
        // 中位标准(2013)表格初始化
        zwTableInit: function () {
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
            }, {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return '<input type="checkbox" name="subChk" value="' + row.vehicleId + '" onclick="riskDefineSettings.subChkChange(this)" /> ';
                }
            },
                {
                    "data": null,
                    "class": "text-center", //最后一列，操作按钮
                    render: function (data, type, row, meta) {
                        var editUrlPath = myTable.editUrl + row.vehicleId + "_" + row.brand + ".gsp"; //修改地址
                        var perInfoUrlPre = '/clbs/r/riskManagement/DefineSettings/readPerInfo_{id}_{brand}.gsp';
                        var readPerStateUrlPre = '/clbs/r/riskManagement/DefineSettings/readPerState_{id}_{brand}.gsp';
                        var readDsmInfoUrlPre = '/clbs/r/riskManagement/DefineSettings/readDsmInfo_{id}_{brand}.gsp';
                        var readAdasInfoUrlPre = '/clbs/r/riskManagement/DefineSettings/readAdasInfo_{id}_{brand}.gsp';
                        var upgradeUrlPre = '/clbs/r/riskManagement/DefineSettings/upgrade_{id}_{brand}.gsp';

                        var result = '';
                        //设置按钮
                        if (row.vehicleConfigId != null && row.vehicleConfigId != '') {
                            result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                        } else {
                            result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-set"></i>设置</button>&ensp;';
                        }
                        // 删除
                        if (row.id != null && row.id != '' && row.vehicleConfigId != null && row.vehicleConfigId != '') {
                            result += '<button type="button" onclick="myTable.deleteItem(\''
                                + row.vehicleId
                                + '\')"  class="deleteButton editBtn  disableClick"><i class="fa fa fa-trash-o"></i>恢复默认</button>&ensp;';
                        } else {//禁用恢复默认按钮
                            result += '<button type="button" disabled class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>恢复默认</button>&ensp;';
                        }
                        // 下发
                        if (row.vehicleConfigId != null && row.vehicleConfigId != '') {
                            result += '<button id="sendParamButton_' + row.vehicleId + '"  onclick="riskDefineSettings.sendParam(\'' + row.vehicleId + '\')" type="button" class="editBtn editBtn-info"><i class="fa fa-issue"></i>下发参数</button>&ensp;';
                        } else {
                            result += '<button id="sendParamButton_' + row.vehicleId + '" disabled type="button" class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>下发参数</button>&ensp;';
                        }
                        //其他
                        if (row.vehicleConfigId != null && row.vehicleConfigId != '') {
                            result += '<div class="btn-group"><button class="editBtn editBtn-info" type="button" id="dropdownMenuOther" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">其他<span class="caret"></span></button>' +
                                '<ul class="dropdown-menu" style="width:410px;" aria-labelledby="dropdownMenuOther" id="dropdownMenuContent">' +
                                '<li><a href="' + perInfoUrlPre.replace("{id}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonWin">读取外设基本信息</a></li>' +
                                '<li><a href="' + readPerStateUrlPre.replace("{id}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonWin">读取外设状态</a></li>' +
                                '<li><a href="' + readDsmInfoUrlPre.replace("{id}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonWin">读取驾驶员监测系统(DMS)参数</a></li>' +

                                '<li style="width: 114px;"><a href="' + upgradeUrlPre.replace("{id}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonWin">传感器远程升级</a></li>' +
                                '<li><a href="' + readAdasInfoUrlPre.replace("{id}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonWin">读取前向监测系统(FMS)参数</a></li>' +
                                '</ul></div>';
                        } else {
                            result += '<button class="editBtn editBtn-info" type="button" id="DisDropdownMenuOther" data-toggle="" aria-haspopup="true" aria-expanded="true">其他<span class="caret"></span></button>';
                        }
                        return result;
                    }
                }, {
                    "data": "status",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return riskDefineSettings.renderSendStatus(data);
                    }
                }, {
                    "data": "brand",
                    "class": "text-center"
                }, {
                    "data": "groupName",
                    "class": "text-center"
                }];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
                d.groupId = selectGroupId;
                d.assignmentId = selectAssignId;
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/r/riskManagement/DefineSettings/list',
                // bindUrl: '/clbs/m/switching/signal/bind_',
                editUrl: '/clbs/r/riskManagement/DefineSettings/setting_',
                deleteUrl: '/clbs/r/riskManagement/DefineSettings/delete_',
                deletemoreUrl: '/clbs/r/riskManagement/DefineSettings/deletemore',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                lengthMenu: [5, 10, 20, 50, 100],
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            //表格初始化
            myTable.init();
        },
        // 川标、桂标
        publicTableInit: function () {
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
            }, {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return '<input type="checkbox" name="subChk" value="' + row.vehicleId + '" onclick="riskDefineSettings.subChkChange(this)" /> ';
                }
            }, {
                "data": null,
                "class": "text-center", //最后一列，操作按钮
                render: function (data, type, row, meta) {
                    var editUrlPath = myTable.editUrl + row.vehicleId + ".gsp"; //修改地址
                    var perInfoUrlPre = '/clbs/adas/standard/param/readPerInfo_{id}_{brand}.gsp?protocolType=' + protocol;//读取外设基本信息
                    var readPerStateUrlPre = '/clbs/adas/standard/param/readPerState_{id}_{brand}.gsp?protocolType=' + protocol;//读取外设状态
                    var readDsmInfoUrlPre = '/clbs/adas/standard/param/readParamInfo_{id}_{brand}.gsp?protocolType=' + protocol;//读取外设参数设置

                    var result = '';
                    //设置按钮
                    if (row.bindId != null && row.bindId != '') {
                        result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                    } else {
                        result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-set"></i>设置</button>&ensp;';
                    }
                    // 恢复默认
                    if (row.bindId != null && row.bindId != '') {
                        result += '<button type="button" onclick="myTable.deleteItem(\''
                            + row.vehicleId
                            + '\')"  class="deleteButton editBtn  disableClick"><i class="fa fa fa-trash-o"></i>恢复默认</button>&ensp;';
                    } else {//禁用恢复默认按钮
                        result += '<button type="button" disabled class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>恢复默认</button>&ensp;';
                    }
                    // 下发
                    if (row.bindId != null && row.bindId != '') {
                        result += '<button id="sendParamButton_' + row.vehicleId + '"  onclick="riskDefineSettings.pubSendParam(\'' + row.vehicleId + '\')" type="button" class="editBtn editBtn-info"><i class="fa fa-issue"></i>下发参数</button>&ensp;';
                    } else {
                        result += '<button id="sendParamButton_' + row.vehicleId + '" disabled type="button" class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>下发参数</button>&ensp;';
                    }
                    //其他
                    if (row.bindId != null && row.bindId != '') {
                        result += '<div class="btn-group"><button class="editBtn editBtn-info" type="button" id="dropdownMenuOther" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">其他<span class="caret"></span></button>' +
                            '<ul class="dropdown-menu" style="width:330px;" aria-labelledby="dropdownMenuOther" id="dropdownMenuContent">' +
                            '<li><a href="' + perInfoUrlPre.replace("{id}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonSmWin">读取外设基本信息</a></li>' +
                            '<li><a href="' + readPerStateUrlPre.replace("{id}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonSmWin">读取外设状态</a></li>' +
                            '<li><a href="' + readDsmInfoUrlPre.replace("{id}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonWin">读取外设参数设置</a></li>' +
                            '</ul></div>';
                    } else {
                        result += '<button class="editBtn editBtn-info" type="button" id="DisDropdownMenuOther" data-toggle="" aria-haspopup="true" aria-expanded="true">其他<span class="caret"></span></button>';
                    }
                    return result;
                }
            }, {
                "data": "brand",
                "class": "text-center"
            }, {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (protocol == '12') {
                        return '交通部JT/T808-2013(川标)';
                    } /*else if(protocol == '13') {
                        return '交通部JT/T808-2013(冀标)';
                    }*/ else if (protocol == '14') {
                        return '交通部JT/T808-2013(桂标)';
                    }
                    /*else if(protocol == '15'){
                                            return '交通部JT/T808-2013(苏标)';
                                        }*/
                }
            }, {
                "data": "groupName",
                "class": "text-center"
            }, {
                "data": "onLineStatus",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == "3") {
                        return '离线';
                    }
                    return '在线';

                }
            }, {
                "data": "forward",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return riskDefineSettings.renderSendStatus(data);
                }
            },
                {
                    "data": "driverBehavior",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return riskDefineSettings.renderSendStatus(data);
                    }
                }, {
                    "data": "intenseDriving",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return riskDefineSettings.renderSendStatus(data);
                    }
                }, {
                    "data": "blindDetection",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return riskDefineSettings.renderSendStatus(data);
                    }
                }, {
                    "data": "tirePressure",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return riskDefineSettings.renderSendStatus(data);
                    }
                },
            ];

            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
                d.groupId = selectGroupId;
                d.assignmentId = selectAssignId;
                d.protocol = protocol;
                if (adanceFlag) {
                    d.terminalCategory = terminalCategory;
                    d.sendStatus = sendStatus;
                    d.statusInfo = statusInfo;
                } else {
                    d.terminalCategory = '';
                    d.sendStatus = '';
                    d.statusInfo = '';
                }
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/adas/standard/param/list',
                editUrl: '/clbs/adas/standard/param/setting_' + protocol + '_',
                deleteUrl: '/clbs/adas/standard/param/delete_',
                deletemoreUrl: '/clbs/adas/standard/param/deletemore',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'publicDataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                lengthMenu: [5, 10, 20, 50, 100],
            };

            myTable = new TG_Tabel.createNew(setting);
            myTable.init();

            // 激烈驾驶列显示隐藏
            var column = myTable.dataTable.column(9);
            if (protocol == '13' || protocol == '15' || protocol == '21') {
                column.visible(false);
            } else {
                column.visible(true);
            }
        },
        // 黑标
        hljTableInit: function () {
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
            }, {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return '<input type="checkbox" name="subChk" value="' + row.vehicleId + '" onclick="riskDefineSettings.subChkChange(this)" /> ';
                }
            }, {
                "data": null,
                "class": "text-center", //最后一列，操作按钮
                render: function (data, type, row, meta) {
                    var editUrlPath = myTable.editUrl + row.vehicleId + ".gsp"; //修改地址
                    var readDsmInfoUrlPre = '/clbs/adas/standard/param/readParamInfo_{id}_{brand}.gsp?protocolType=' + protocol;//读取外设参数设置

                    var result = '';
                    //设置按钮
                    if (row.bindId != null && row.bindId != '') {
                        result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                    } else {
                        result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-set"></i>设置</button>&ensp;';
                    }
                    // 恢复默认
                    if (row.bindId != null && row.bindId != '') {
                        result += '<button type="button" onclick="myTable.deleteItem(\''
                            + row.vehicleId
                            + '\')"  class="deleteButton editBtn  disableClick"><i class="fa fa fa-trash-o"></i>恢复默认</button>&ensp;';
                    } else {//禁用恢复默认按钮
                        result += '<button type="button" disabled class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>恢复默认</button>&ensp;';
                    }
                    // 下发
                    if (row.bindId != null && row.bindId != '') {
                        result += '<button id="sendParamButton_' + row.vehicleId + '"  onclick="riskDefineSettings.pubSendParam(\'' + row.vehicleId + '\')" type="button" class="editBtn editBtn-info"><i class="fa fa-issue"></i>下发参数</button>&ensp;';
                    } else {
                        result += '<button id="sendParamButton_' + row.vehicleId + '" disabled type="button" class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>下发参数</button>&ensp;';
                    }
                    //其他
                    if (row.bindId != null && row.bindId != '') {
                        result += '<div class="btn-group"><button class="editBtn editBtn-info" type="button" id="dropdownMenuOther" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">其他<span class="caret"></span></button>' +
                            '<ul class="dropdown-menu" style="width:330px;" aria-labelledby="dropdownMenuOther" id="dropdownMenuContent">' +
                            '<li><a href="' + readDsmInfoUrlPre.replace("{id}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonWin">读取外设参数设置</a></li>' +
                            '</ul></div>';
                    } else {
                        result += '<button class="editBtn editBtn-info" type="button" id="DisDropdownMenuOther" data-toggle="" aria-haspopup="true" aria-expanded="true">其他<span class="caret"></span></button>';
                    }
                    return result;
                }
            }, {
                "data": "brand",
                "class": "text-center"
            }, {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return '交通部JT/T808-2019(黑标)';
                }
            }, {
                "data": "groupName",
                "class": "text-center"
            }, {
                "data": "onLineStatus",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == "3") {
                        return '离线';
                    }
                    return '在线';

                }
            }, {
                "data": "heiDriverIdentification",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return riskDefineSettings.renderSendStatus(data);
                }
            },
                {
                    "data": "heiVehicleOperationMonitoring",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return riskDefineSettings.renderSendStatus(data);
                    }
                }, {
                    "data": "heiDriverDrivingBehavior",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return riskDefineSettings.renderSendStatus(data);
                    }
                }, {
                    "data": "heiEquipmentFailureMonitoring",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return riskDefineSettings.renderSendStatus(data);
                    }
                }
            ];

            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
                d.groupId = selectGroupId;
                d.assignmentId = selectAssignId;
                d.protocol = protocol;
                if (adanceFlag) {
                    d.terminalCategory = terminalCategory;
                    d.sendStatus = sendStatus;
                    d.statusInfo = statusInfo;
                } else {
                    d.terminalCategory = '';
                    d.sendStatus = '';
                    d.statusInfo = '';
                }
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/adas/standard/param/list',
                editUrl: '/clbs/adas/standard/param/setting_' + protocol + '_',
                deleteUrl: '/clbs/adas/standard/param/delete_',
                deletemoreUrl: '/clbs/adas/standard/param/deletemore',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'heiDataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                lengthMenu: [5, 10, 20, 50, 100],
            };

            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
        },
        // 苏标、中位标准2019
        suTableInit: function () {
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
            }, {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return '<input type="checkbox" name="subChk" value="' + row.vehicleId + '" onclick="riskDefineSettings.subChkChange(this)" /> ';
                }
            }, {
                "data": null,
                "class": "text-center", //最后一列，操作按钮
                render: function (data, type, row, meta) {
                    var editUrlPath = myTable.editUrl + row.vehicleId + ".gsp"; //修改地址
                    var perInfoUrlPre = '/clbs/adas/standard/param/readPerInfo_{id}_{brand}.gsp?protocolType=' + protocol;//读取外设基本信息
                    var readPerStateUrlPre = '/clbs/adas/standard/param/readPerState_{id}_{brand}.gsp?protocolType=' + protocol;//读取外设状态
                    var readDsmInfoUrlPre = '/clbs/adas/standard/param/readParamInfo_{id}_{brand}.gsp?protocolType=' + protocol;//读取外设参数设置

                    var result = '';
                    //设置按钮
                    if (row.bindId != null && row.bindId != '') {
                        result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                    } else {
                        result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-set"></i>设置</button>&ensp;';
                    }
                    // 恢复默认
                    if (row.bindId != null && row.bindId != '') {
                        result += '<button type="button" onclick="myTable.deleteItem(\''
                            + row.vehicleId
                            + '\')"  class="deleteButton editBtn  disableClick"><i class="fa fa fa-trash-o"></i>恢复默认</button>&ensp;';
                    } else {//禁用恢复默认按钮
                        result += '<button type="button" disabled class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>恢复默认</button>&ensp;';
                    }
                    // 下发
                    if (row.bindId != null && row.bindId != '') {
                        result += '<button id="sendParamButton_' + row.vehicleId + '"  onclick="riskDefineSettings.pubSendParam(\'' + row.vehicleId + '\')" type="button" class="editBtn editBtn-info"><i class="fa fa-issue"></i>下发参数</button>&ensp;';
                    } else {
                        result += '<button id="sendParamButton_' + row.vehicleId + '" disabled type="button" class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>下发参数</button>&ensp;';
                    }
                    //其他
                    if (row.bindId != null && row.bindId != '') {
                        result += '<div class="btn-group"><button class="editBtn editBtn-info" type="button" id="dropdownMenuOther" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">其他<span class="caret"></span></button>' +
                            '<ul class="dropdown-menu" style="width:330px;" aria-labelledby="dropdownMenuOther" id="dropdownMenuContent">' +
                            '<li><a href="' + perInfoUrlPre.replace("{id}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonSmWin">读取外设基本信息</a></li>' +
                            '<li><a href="' + readPerStateUrlPre.replace("{id}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonSmWin">读取外设状态</a></li>' +
                            '<li><a href="' + readDsmInfoUrlPre.replace("{id}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonWin">读取外设参数设置</a></li>';
                        var upgradeUrlPre = '/clbs/adas/standard/param/zwUpgrade_{id}_{brand}.gsp';
                        if (protocol == '21') {
                            result += '<li><a href="' + upgradeUrlPre.replace("{id}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonWin">传感器远程升级/恢复</a></li>';
                        }
                        result += '</ul></div>';

                    } else {
                        result += '<button class="editBtn editBtn-info" type="button" id="DisDropdownMenuOther" data-toggle="" aria-haspopup="true" aria-expanded="true">其他<span class="caret"></span></button>';
                    }
                    return result;
                }
            }, {
                "data": "brand",
                "class": "text-center"
            }, {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    /*if (protocol == '12') {
                        return '交通部JT/T808-2013(川标)';
                    } else*/
                    if (protocol == '13') {
                        return '交通部JT/T808-2013(冀标)';
                    } /*else if(protocol == '14'){
                        return '交通部JT/T808-2013(桂标)';
                    }*/ else if (protocol == '15') {
                        return '交通部JT/T808-2013(苏标)';
                    } else if (protocol == '21') {
                        return '交通部JT/T808-2019(中位)';
                    }
                }
            }, {
                "data": "groupName",
                "class": "text-center"
            }, {
                "data": "onLineStatus",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == "3") {
                        return '离线';
                    }
                    return '在线';

                }
            }, {
                "data": "forward",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return riskDefineSettings.renderSendStatus(data);
                }
            },
                {
                    "data": "driverBehavior",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return riskDefineSettings.renderSendStatus(data);
                    }
                },
                {
                    "data": "blindDetection",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return riskDefineSettings.renderSendStatus(data);
                    }
                },
                {
                    "data": "tirePressure",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return riskDefineSettings.renderSendStatus(data);
                    }
                },
            ];

            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
                d.groupId = selectGroupId;
                d.assignmentId = selectAssignId;
                d.protocol = protocol;
                if (adanceFlag) {
                    d.terminalCategory = terminalCategory;
                    d.sendStatus = sendStatus;
                    d.statusInfo = statusInfo;
                } else {
                    d.terminalCategory = '';
                    d.sendStatus = '';
                    d.statusInfo = '';
                }
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/adas/standard/param/list',
                editUrl: '/clbs/adas/standard/param/setting_' + protocol + '_',
                deleteUrl: '/clbs/adas/standard/param/delete_',
                deletemoreUrl: '/clbs/adas/standard/param/deletemore',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'suDataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                lengthMenu: [5, 10, 20, 50, 100],
            };

            myTable = new TG_Tabel.createNew(setting);
            myTable.init();

            /*
            // 激烈驾驶列显示隐藏
            var column = myTable.dataTable.column(9);
            if (protocol == '13' || protocol == '15') {
                column.visible(false);
            } else {
                column.visible(true);
            }
            */
        },
        // 冀标
        jiTableInit: function () {
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
            }, {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return '<input type="checkbox" name="subChk" value="' + row.vehicleId + '" onclick="riskDefineSettings.subChkChange(this)" /> ';
                }
            }, {
                "data": null,
                "class": "text-center", //最后一列，操作按钮
                render: function (data, type, row, meta) {
                    var editUrlPath = myTable.editUrl + row.vehicleId + ".gsp"; //修改地址
                    var perInfoUrlPre = '/clbs/adas/standard/param/readPerInfo_{id}_{brand}.gsp?protocolType=' + protocol;//读取外设基本信息
                    var readPerStateUrlPre = '/clbs/adas/standard/param/readPerState_{id}_{brand}.gsp?protocolType=' + protocol;//读取外设状态
                    var readDsmInfoUrlPre = '/clbs/adas/standard/param/readParamInfo_{id}_{brand}.gsp?protocolType=' + protocol;//读取外设参数设置

                    var result = '';
                    //设置按钮
                    if (row.bindId != null && row.bindId != '') {
                        result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                    } else {
                        result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-set"></i>设置</button>&ensp;';
                    }
                    // 恢复默认
                    if (row.bindId != null && row.bindId != '') {
                        result += '<button type="button" onclick="myTable.deleteItem(\''
                            + row.vehicleId
                            + '\')"  class="deleteButton editBtn  disableClick"><i class="fa fa fa-trash-o"></i>恢复默认</button>&ensp;';
                    } else {//禁用恢复默认按钮
                        result += '<button type="button" disabled class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>恢复默认</button>&ensp;';
                    }
                    // 下发
                    if (row.bindId != null && row.bindId != '') {
                        result += '<button id="sendParamButton_' + row.vehicleId + '"  onclick="riskDefineSettings.pubSendParam(\'' + row.vehicleId + '\')" type="button" class="editBtn editBtn-info"><i class="fa fa-issue"></i>下发参数</button>&ensp;';
                    } else {
                        result += '<button id="sendParamButton_' + row.vehicleId + '" disabled type="button" class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>下发参数</button>&ensp;';
                    }
                    //其他
                    if (row.bindId != null && row.bindId != '') {
                        result += '<div class="btn-group"><button class="editBtn editBtn-info" type="button" id="dropdownMenuOther" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">其他<span class="caret"></span></button>' +
                            '<ul class="dropdown-menu" style="width:330px;" aria-labelledby="dropdownMenuOther" id="dropdownMenuContent">' +
                            '<li><a href="' + perInfoUrlPre.replace("{id}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonSmWin">读取外设基本信息</a></li>' +
                            '<li><a href="' + readPerStateUrlPre.replace("{id}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonSmWin">读取外设状态</a></li>' +
                            '<li><a href="' + readDsmInfoUrlPre.replace("{id}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonWin">读取外设参数设置</a></li>' +
                            '</ul></div>';
                    } else {
                        result += '<button class="editBtn editBtn-info" type="button" id="DisDropdownMenuOther" data-toggle="" aria-haspopup="true" aria-expanded="true">其他<span class="caret"></span></button>';
                    }
                    return result;
                }
            }, {
                "data": "brand",
                "class": "text-center"
            }, {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    /*if (protocol == '12') {
                        return '交通部JT/T808-2013(川标)';
                    } else*/
                    if (protocol == '13') {
                        return '交通部JT/T808-2013(冀标)';
                    } /*else if(protocol == '14'){
                        return '交通部JT/T808-2013(桂标)';
                    }*/ else if (protocol == '15') {
                        return '交通部JT/T808-2013(苏标)';
                    } else if (protocol == '21') {
                        return '交通部JT/T808-2019(中位)';
                    }
                }
            }, {
                "data": "groupName",
                "class": "text-center"
            }, {
                "data": "onLineStatus",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == "3") {
                        return '离线';
                    }
                    return '在线';

                }
            }, {
                "data": "forward",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return riskDefineSettings.renderSendStatus(data);
                }
            },
                {
                    "data": "driverBehavior",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return riskDefineSettings.renderSendStatus(data);
                    }
                },
            ];

            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
                d.groupId = selectGroupId;
                d.assignmentId = selectAssignId;
                d.protocol = protocol;
                if (adanceFlag) {
                    d.terminalCategory = terminalCategory;
                    d.sendStatus = sendStatus;
                    d.statusInfo = statusInfo;
                } else {
                    d.terminalCategory = '';
                    d.sendStatus = '';
                    d.statusInfo = '';
                }
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/adas/standard/param/list',
                editUrl: '/clbs/adas/standard/param/setting_' + protocol + '_',
                deleteUrl: '/clbs/adas/standard/param/delete_',
                deletemoreUrl: '/clbs/adas/standard/param/deletemore',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'jiDataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                lengthMenu: [5, 10, 20, 50, 100],
            };

            myTable = new TG_Tabel.createNew(setting);
            myTable.init();

            /*
            // 激烈驾驶列显示隐藏
            var column = myTable.dataTable.column(9);
            if (protocol == '13' || protocol == '15') {
                column.visible(false);
            } else {
                column.visible(true);
            }
            */
        },
        // 浙标、吉标、陕标
        shanTableInit: function () {
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
            }, {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return '<input type="checkbox" name="subChk" value="' + row.vehicleId + '" onclick="riskDefineSettings.subChkChange(this)" /> ';
                }
            }, {
                "data": null,
                "class": "text-center", //最后一列，操作按钮
                render: function (data, type, row, meta) {
                    var editUrlPath = myTable.editUrl + row.vehicleId + ".gsp"; //修改地址
                    var perInfoUrlPre = '/clbs/adas/standard/param/readPerInfo_{id}_{brand}.gsp?protocolType=' + protocol;//读取外设基本信息
                    var readPerStateUrlPre = '/clbs/adas/standard/param/readPerState_{id}_{brand}.gsp?protocolType=' + protocol;//读取外设状态
                    var readDsmInfoUrlPre = '/clbs/adas/standard/param/readParamInfo_{id}_{brand}.gsp?protocolType=' + protocol;//读取外设参数设置

                    var result = '';
                    //设置按钮
                    if (row.bindId != null && row.bindId != '') {
                        result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                    } else {
                        result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-set"></i>设置</button>&ensp;';
                    }
                    // 恢复默认
                    if (row.bindId != null && row.bindId != '') {
                        result += '<button type="button" onclick="myTable.deleteItem(\''
                            + row.vehicleId
                            + '\')"  class="deleteButton editBtn  disableClick"><i class="fa fa fa-trash-o"></i>恢复默认</button>&ensp;';
                    } else {//禁用恢复默认按钮
                        result += '<button type="button" disabled class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>恢复默认</button>&ensp;';
                    }
                    // 下发
                    if (row.bindId != null && row.bindId != '') {
                        result += '<button id="sendParamButton_' + row.vehicleId + '"  onclick="riskDefineSettings.pubSendParam(\'' + row.vehicleId + '\')" type="button" class="editBtn editBtn-info"><i class="fa fa-issue"></i>下发参数</button>&ensp;';
                    } else {
                        result += '<button id="sendParamButton_' + row.vehicleId + '" disabled type="button" class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>下发参数</button>&ensp;';
                    }
                    //其他
                    if (row.bindId != null && row.bindId != '') {
                        result += '<div class="btn-group"><button class="editBtn editBtn-info" type="button" id="dropdownMenuOther" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">其他<span class="caret"></span></button>' +
                            '<ul class="dropdown-menu" style="width:330px;" aria-labelledby="dropdownMenuOther" id="dropdownMenuContent">' +
                            '<li><a href="' + perInfoUrlPre.replace("{id}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonSmWin">读取外设基本信息</a></li>' +
                            '<li><a href="' + readPerStateUrlPre.replace("{id}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonSmWin">读取外设状态</a></li>' +
                            '<li><a href="' + readDsmInfoUrlPre.replace("{id}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonWin">读取外设参数设置</a></li>' +
                            '</ul></div>';
                    } else {
                        result += '<button class="editBtn editBtn-info" type="button" id="DisDropdownMenuOther" data-toggle="" aria-haspopup="true" aria-expanded="true">其他<span class="caret"></span></button>';
                    }
                    return result;
                }
            }, {
                "data": "brand",
                "class": "text-center"
            }, {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (protocol == '16') {
                        return '交通部JT/T808-2013(浙标)';
                    } else if (protocol == '17') {
                        return '交通部JT/T808-2013(吉标)';
                    } else if (protocol == '18') {
                        return '交通部JT/T808-2013(陕标)';
                    } else if (protocol == '19') {
                        return '交通部JT/T808-2013(赣标)';
                    }
                }
            }, {
                "data": "groupName",
                "class": "text-center"
            }, {
                "data": "onLineStatus",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == "3") {
                        return '离线';
                    }
                    return '在线';

                }
            }, {
                "data": "forward",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return riskDefineSettings.renderSendStatus(data);
                }
            },
                {
                    "data": "driverBehavior",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return riskDefineSettings.renderSendStatus(data);
                    }
                },
                {
                    "data": "blindDetection",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return riskDefineSettings.renderSendStatus(data);
                    }
                },
            ];

            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
                d.groupId = selectGroupId;
                d.assignmentId = selectAssignId;
                d.protocol = protocol;
                if (adanceFlag) {
                    d.terminalCategory = terminalCategory;
                    d.sendStatus = sendStatus;
                    d.statusInfo = statusInfo;
                } else {
                    d.terminalCategory = '';
                    d.sendStatus = '';
                    d.statusInfo = '';
                }
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/adas/standard/param/list',
                editUrl: '/clbs/adas/standard/param/setting_' + protocol + '_',
                deleteUrl: '/clbs/adas/standard/param/delete_',
                deletemoreUrl: '/clbs/adas/standard/param/deletemore',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'shanDataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                lengthMenu: [5, 10, 20, 50, 100],
            };

            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
        },
        //沪标
        huTableInit: function () {
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
            }, {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return '<input type="checkbox" name="subChk" value="' + row.vehicleId + '" onclick="riskDefineSettings.subChkChange(this)" /> ';
                }
            }, {
                "data": null,
                "class": "text-center", //最后一列，操作按钮
                render: function (data, type, row, meta) {
                    var editUrlPath = myTable.editUrl + row.vehicleId + ".gsp"; //修改地址
                    var perInfoUrlPre = '/clbs/adas/standard/param/readPerInfo_{id}_{brand}.gsp?protocolType=' + protocol;//读取外设基本信息
                    var readPerStateUrlPre = '/clbs/adas/standard/param/readPerState_{id}_{brand}.gsp?protocolType=' + protocol;//读取外设状态
                    var readDsmInfoUrlPre = '/clbs/adas/standard/param/readParamInfo_{id}_{brand}.gsp?protocolType=' + protocol;//读取外设参数设置

                    var result = '';
                    //设置按钮
                    if (row.bindId != null && row.bindId != '') {
                        result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                    } else {
                        result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-set"></i>设置</button>&ensp;';
                    }
                    // 恢复默认
                    if (row.bindId != null && row.bindId != '') {
                        result += '<button type="button" onclick="myTable.deleteItem(\''
                            + row.vehicleId
                            + '\')"  class="deleteButton editBtn  disableClick"><i class="fa fa fa-trash-o"></i>恢复默认</button>&ensp;';
                    } else {//禁用恢复默认按钮
                        result += '<button type="button" disabled class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>恢复默认</button>&ensp;';
                    }
                    // 下发
                    if (row.bindId != null && row.bindId != '') {
                        result += '<button id="sendParamButton_' + row.vehicleId + '"  onclick="riskDefineSettings.pubSendParam(\'' + row.vehicleId + '\')" type="button" class="editBtn editBtn-info"><i class="fa fa-issue"></i>下发参数</button>&ensp;';
                    } else {
                        result += '<button id="sendParamButton_' + row.vehicleId + '" disabled type="button" class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>下发参数</button>&ensp;';
                    }
                    //其他
                    if (row.bindId != null && row.bindId != '') {
                        result += '<div class="btn-group"><button class="editBtn editBtn-info" type="button" id="dropdownMenuOther" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">其他<span class="caret"></span></button>' +
                            '<ul class="dropdown-menu" style="width:330px;" aria-labelledby="dropdownMenuOther" id="dropdownMenuContent">' +
                            '<li><a href="' + perInfoUrlPre.replace("{id}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonSmWin">读取外设基本信息</a></li>' +
                            '<li><a href="' + readPerStateUrlPre.replace("{id}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonSmWin">读取外设状态</a></li>' +
                            '<li><a href="' + readDsmInfoUrlPre.replace("{id}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonWin">读取外设参数设置</a></li>' +
                            '</ul></div>';
                    } else {
                        result += '<button class="editBtn editBtn-info" type="button" id="DisDropdownMenuOther" data-toggle="" aria-haspopup="true" aria-expanded="true">其他<span class="caret"></span></button>';
                    }
                    return result;
                }
            }, {
                "data": "brand",
                "class": "text-center"
            }, {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (protocol == '20') {
                        return "交通部JT/T808-2019(沪标)"
                    }
                }
            }, {
                "data": "groupName",
                "class": "text-center"
            }, {
                "data": "onLineStatus",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == "3") {
                        return '离线';
                    }
                    return '在线';

                }
            }, {
                "data": "forward",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return riskDefineSettings.renderSendStatus(data);
                }
            },
                {
                    "data": "driverBehavior",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return riskDefineSettings.renderSendStatus(data);
                    }
                },
                {
                    "data": "blindDetection",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return riskDefineSettings.renderSendStatus(data);
                    }
                },
                {
                    "data": "overcrowd",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return riskDefineSettings.renderSendStatus(data);
                    }
                },
            ];

            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
                d.groupId = selectGroupId;
                d.assignmentId = selectAssignId;
                d.protocol = protocol;
                if (adanceFlag) {
                    d.terminalCategory = terminalCategory;
                    d.sendStatus = sendStatus;
                    d.statusInfo = statusInfo;
                } else {
                    d.terminalCategory = '';
                    d.sendStatus = '';
                    d.statusInfo = '';
                }
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/adas/standard/param/list',
                editUrl: '/clbs/adas/standard/param/setting_' + protocol + '_',
                deleteUrl: '/clbs/adas/standard/param/delete_',
                deletemoreUrl: '/clbs/adas/standard/param/deletemore',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'huDataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                lengthMenu: [5, 10, 20, 50, 100],
            };

            myTable = new TG_Tabel.createNew(setting);
            myTable.init();

            /*
            // 激烈驾驶列显示隐藏
            var column = myTable.dataTable.column(9);
            if (protocol == '13' || protocol == '15') {
                column.visible(false);
            } else {
                column.visible(true);
            }
            */
        },
        //京标
        jingTableInit: function () {
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
            }, {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return '<input type="checkbox" name="subChk" value="' + row.vehicleId + '" onclick="riskDefineSettings.subChkChange(this)" /> ';
                }
            }, {
                "data": null,
                "class": "text-center", //最后一列，操作按钮
                render: function (data, type, row, meta) {
                    var editUrlPath = myTable.editUrl + row.vehicleId + ".gsp"; //修改地址
                    var perInfoUrlPre = '/clbs/adas/standard/param/readTerminalInfo_{vehicleId}_{brand}.gsp'; //查询终端参数
                    var readDsmInfoUrlPre = '/clbs/adas/standard/param/readParamInfo_{id}_{brand}.gsp?protocolType=' + protocol;//读取外设参数设置

                    var result = '';
                    //设置按钮
                    if (row.bindId != null && row.bindId != '') {
                        result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                    } else {
                        result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-set"></i>设置</button>&ensp;';
                    }
                    // 恢复默认
                    if (row.bindId != null && row.bindId != '') {
                        result += '<button type="button" onclick="myTable.deleteItem(\''
                            + row.vehicleId
                            + '\')"  class="deleteButton editBtn  disableClick"><i class="fa fa fa-trash-o"></i>恢复默认</button>&ensp;';
                    } else {//禁用恢复默认按钮
                        result += '<button type="button" disabled class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>恢复默认</button>&ensp;';
                    }
                    // 下发
                    if (row.bindId != null && row.bindId != '') {
                        result += '<button id="sendParamButton_' + row.vehicleId + '"  onclick="riskDefineSettings.pubSendParam(\'' + row.vehicleId + '\')" type="button" class="editBtn editBtn-info"><i class="fa fa-issue"></i>下发参数</button>&ensp;';
                    } else {
                        result += '<button id="sendParamButton_' + row.vehicleId + '" disabled type="button" class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>下发参数</button>&ensp;';
                    }
                    //其他
                    if (row.bindId != null && row.bindId != '') {
                        result += '<div class="btn-group"><button class="editBtn editBtn-info" type="button" id="dropdownMenuOther" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">其他<span class="caret"></span></button>' +
                            '<ul class="dropdown-menu" style="width:217px;" aria-labelledby="dropdownMenuOther" id="dropdownMenuContent">' +
                            '<li><a href="' + perInfoUrlPre.replace("{vehicleId}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonSmWin">查询终端参数</a></li>' +
                            '<li><a href="' + readDsmInfoUrlPre.replace("{id}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonWin">读取外设参数设置</a></li>' +
                            '</ul></div>';
                    } else {
                        result += '<button class="editBtn editBtn-info" type="button" id="DisDropdownMenuOther" data-toggle="" aria-haspopup="true" aria-expanded="true">其他<span class="caret"></span></button>';
                    }
                    return result;
                }
            }, {
                "data": "brand",
                "class": "text-center"
            }, {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (protocol == '24') {
                        return "交通部JT/T808-2019(京标)"
                    }
                }
            }, {
                "data": "groupName",
                "class": "text-center"
            }, {
                "data": "onLineStatus",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == "3") {
                        return '离线';
                    }
                    return '在线';

                }
            }, {
                "data": "jingDriverBehavior",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return riskDefineSettings.renderSendStatus(data);
                }
            },
                {
                    "data": "vehicleOperation",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return riskDefineSettings.renderSendStatus(data);
                    }
                },
            ];

            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
                d.groupId = selectGroupId;
                d.assignmentId = selectAssignId;
                d.protocol = protocol;
                if (adanceFlag) {
                    d.terminalCategory = terminalCategory;
                    d.sendStatus = sendStatus;
                    d.statusInfo = statusInfo;
                } else {
                    d.terminalCategory = '';
                    d.sendStatus = '';
                    d.statusInfo = '';
                }
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/adas/standard/param/list',
                editUrl: '/clbs/adas/standard/param/setting_' + protocol + '_',
                deleteUrl: '/clbs/adas/standard/param/delete_',
                deletemoreUrl: '/clbs/adas/standard/param/deletemore',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'jingDataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                lengthMenu: [5, 10, 20, 50, 100],
            };

            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
        },
        //鲁标
        luTableInit: function () {
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
            }, {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return '<input type="checkbox" name="subChk" value="' + row.vehicleId + '" onclick="riskDefineSettings.subChkChange(this)" /> ';
                }
            }, {
                "data": null,
                "class": "text-center", //最后一列，操作按钮
                render: function (data, type, row, meta) {
                    var editUrlPath = myTable.editUrl + row.vehicleId + ".gsp"; //修改地址
                    var readBasicInfoUrlPre = '/clbs/adas/standard/param/readPerInfo_{id}_{brand}.gsp?protocolType=' + protocol;//读取外设基本信息
                    var readStatusInfoUrlPre = '/clbs/adas/standard/param/readPerState_{id}_{brand}.gsp?protocolType=' + protocol;//读取外设状态信息
                    var readDsmInfoUrlPre = '/clbs/adas/standard/param/readParamInfo_{id}_{brand}.gsp?protocolType=' + protocol;//读取外设参数设置

                    var result = '';
                    //设置按钮
                    if (row.bindId != null && row.bindId != '') {
                        result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                    } else {
                        result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-set"></i>设置</button>&ensp;';
                    }
                    // 恢复默认
                    if (row.bindId != null && row.bindId != '') {
                        result += '<button type="button" onclick="myTable.deleteItem(\''
                            + row.vehicleId
                            + '\')"  class="deleteButton editBtn  disableClick"><i class="fa fa fa-trash-o"></i>恢复默认</button>&ensp;';
                    } else {//禁用恢复默认按钮
                        result += '<button type="button" disabled class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>恢复默认</button>&ensp;';
                    }
                    // 下发
                    if (row.bindId != null && row.bindId != '') {
                        result += '<button id="sendParamButton_' + row.vehicleId + '"  onclick="riskDefineSettings.pubSendParam(\'' + row.vehicleId + '\')" type="button" class="editBtn editBtn-info"><i class="fa fa-issue"></i>下发参数</button>&ensp;';
                    } else {
                        result += '<button id="sendParamButton_' + row.vehicleId + '" disabled type="button" class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>下发参数</button>&ensp;';
                    }
                    //其他
                    if (row.bindId != null && row.bindId != '') {
                        result += '<div class="btn-group"><button class="editBtn editBtn-info" type="button" id="dropdownMenuOther" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">其他<span class="caret"></span></button>' +
                            '<ul class="dropdown-menu" style="width:357px;" aria-labelledby="dropdownMenuOther" id="dropdownMenuContent">' +
                            '<li><a href="' + readBasicInfoUrlPre.replace("{id}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonSmWin">读取外设基本信息</a></li>' +
                            '<li><a href="' + readStatusInfoUrlPre.replace("{id}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonSmWin">读取外设状态信息</a></li>' +
                            '<li><a href="' + readDsmInfoUrlPre.replace("{id}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonWin">读取外设参数设置</a></li>' +
                            '</ul></div>';
                    } else {
                        result += '<button class="editBtn editBtn-info" type="button" id="DisDropdownMenuOther" data-toggle="" aria-haspopup="true" aria-expanded="true">其他<span class="caret"></span></button>';
                    }
                    return result;
                }
            }, {
                "data": "brand",
                "class": "text-center"
            }, {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (protocol == '26') {
                        return "交通部JT/T808-2019(鲁标)"
                    }
                }
            }, {
                "data": "groupName",
                "class": "text-center"
            }, {
                "data": "onLineStatus",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == "3") {
                        return '离线';
                    }
                    return '在线';

                }
            }, {
                "data": "forward", //高级驾驶辅助
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return riskDefineSettings.renderSendStatus(data)
                }
            }, {
                "data": "driverBehavior", //驾驶员状态监测
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return riskDefineSettings.renderSendStatus(data)
                }
            }, {
                "data": "tirePressure",//胎压监测
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return riskDefineSettings.renderSendStatus(data)
                }
            }, {
                "data": "blindDetection", //盲点监测
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return riskDefineSettings.renderSendStatus(data)
                }
            }, {
                "data": "driverComparison",//驾驶员比对
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return riskDefineSettings.renderSendStatus(data)
                }
            },
            ];

            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
                d.groupId = selectGroupId;
                d.assignmentId = selectAssignId;
                d.protocol = protocol;
                if (adanceFlag) {
                    d.terminalCategory = terminalCategory;
                    d.sendStatus = sendStatus;
                    d.statusInfo = statusInfo;
                } else {
                    d.terminalCategory = '';
                    d.sendStatus = '';
                    d.statusInfo = '';
                }
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/adas/standard/param/list',
                editUrl: '/clbs/adas/standard/param/setting_' + protocol + '_',
                deleteUrl: '/clbs/adas/standard/param/delete_',
                deletemoreUrl: '/clbs/adas/standard/param/deletemore',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'luDataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                lengthMenu: [5, 10, 20, 50, 100],
            };

            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
        },
        //湘标
        xiangTableInit: function () {
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
            }, {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return '<input type="checkbox" name="subChk" value="' + row.vehicleId + '" onclick="riskDefineSettings.subChkChange(this)" /> ';
                }
            }, {
                "data": null,
                "class": "text-center", //最后一列，操作按钮
                render: function (data, type, row, meta) {
                    var editUrlPath = myTable.editUrl + row.vehicleId + ".gsp"; //修改地址
                    var readDsmInfoUrlPre = '/clbs/adas/standard/param/readParamInfo_{id}_{brand}.gsp?protocolType=' + protocol;//读取外设参数设置

                    var result = '';
                    //设置按钮
                    if (row.bindId != null && row.bindId != '') {
                        result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                    } else {
                        result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-set"></i>设置</button>&ensp;';
                    }
                    // 恢复默认
                    if (row.bindId != null && row.bindId != '') {
                        result += '<button type="button" onclick="myTable.deleteItem(\''
                            + row.vehicleId
                            + '\')"  class="deleteButton editBtn  disableClick"><i class="fa fa fa-trash-o"></i>恢复默认</button>&ensp;';
                    } else {//禁用恢复默认按钮
                        result += '<button type="button" disabled class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>恢复默认</button>&ensp;';
                    }
                    // 下发
                    if (row.bindId != null && row.bindId != '') {
                        result += '<button id="sendParamButton_' + row.vehicleId + '"  onclick="riskDefineSettings.pubSendParam(\'' + row.vehicleId + '\')" type="button" class="editBtn editBtn-info"><i class="fa fa-issue"></i>下发参数</button>&ensp;';
                    } else {
                        result += '<button id="sendParamButton_' + row.vehicleId + '" disabled type="button" class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>下发参数</button>&ensp;';
                    }
                    //其他
                    if (row.bindId != null && row.bindId != '') {
                        result += '<div class="btn-group"><button class="editBtn editBtn-info" type="button" id="dropdownMenuOther" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">其他<span class="caret"></span></button>' +
                            '<ul class="dropdown-menu" style="width:130px;" aria-labelledby="dropdownMenuOther" id="dropdownMenuContent">' +
                            '<li><a href="' + readDsmInfoUrlPre.replace("{id}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonWin">读取外设参数设置</a></li>' +
                            '</ul></div>';
                    } else {
                        result += '<button class="editBtn editBtn-info" type="button" id="DisDropdownMenuOther" data-toggle="" aria-haspopup="true" aria-expanded="true">其他<span class="caret"></span></button>';
                    }
                    return result;
                }
            }, {
                "data": "brand",
                "class": "text-center"
            }, {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (protocol == '27') {
                        return "交通部JT/T808-2013(湘标)"
                    }
                }
            }, {
                "data": "groupName",
                "class": "text-center"
            }, {
                "data": "onLineStatus",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == "3") {
                        return '离线';
                    }
                    return '在线';

                }
            }, {
                "data": "forward",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return riskDefineSettings.renderSendStatus(data);
                }
            }, {
                "data": "driverBehavior",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return riskDefineSettings.renderSendStatus(data);
                }
            }, {
                "data": "overcrowd",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return riskDefineSettings.renderSendStatus(data);
                }
            }, {
                "data": "driverComparison",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return riskDefineSettings.renderSendStatus(data);
                }
            },
            ];

            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
                d.groupId = selectGroupId;
                d.assignmentId = selectAssignId;
                d.protocol = protocol;
                if (adanceFlag) {
                    d.terminalCategory = terminalCategory;
                    d.sendStatus = sendStatus;
                    d.statusInfo = statusInfo;
                } else {
                    d.terminalCategory = '';
                    d.sendStatus = '';
                    d.statusInfo = '';
                }
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/adas/standard/param/list',
                editUrl: '/clbs/adas/standard/param/setting_' + protocol + '_',
                deleteUrl: '/clbs/adas/standard/param/delete_',
                deletemoreUrl: '/clbs/adas/standard/param/deletemore',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'xiangDataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                lengthMenu: [5, 10, 20, 50, 100],
            };

            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
        },
        //粤标
        yueTableInit: function () {
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
            }, {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return '<input type="checkbox" name="subChk" value="' + row.vehicleId + '" onclick="riskDefineSettings.subChkChange(this)" /> ';
                }
            }, {
                "data": null,
                "class": "text-center", //最后一列，操作按钮
                render: function (data, type, row, meta) {
                    var editUrlPath = myTable.editUrl + row.vehicleId + ".gsp"; //修改地址
                    var readBasicInfoUrlPre = '/clbs/adas/standard/param/readPerInfo_{id}_{brand}.gsp?protocolType=' + protocol;//读取外设基本信息
                    var readStatusInfoUrlPre = '/clbs/adas/standard/param/readPerState_{id}_{brand}.gsp?protocolType=' + protocol;//读取外设状态信息
                    var readDsmInfoUrlPre = '/clbs/adas/standard/param/readParamInfo_{id}_{brand}.gsp?protocolType=' + protocol;//读取外设参数设置

                    var result = '';
                    //设置按钮
                    if (row.bindId != null && row.bindId != '') {
                        result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                    } else {
                        result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-set"></i>设置</button>&ensp;';
                    }
                    // 恢复默认
                    if (row.bindId != null && row.bindId != '') {
                        result += '<button type="button" onclick="myTable.deleteItem(\''
                            + row.vehicleId
                            + '\')"  class="deleteButton editBtn  disableClick"><i class="fa fa fa-trash-o"></i>恢复默认</button>&ensp;';
                    } else {//禁用恢复默认按钮
                        result += '<button type="button" disabled class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>恢复默认</button>&ensp;';
                    }
                    // 下发
                    if (row.bindId != null && row.bindId != '') {
                        result += '<button id="sendParamButton_' + row.vehicleId + '"  onclick="riskDefineSettings.pubSendParam(\'' + row.vehicleId + '\')" type="button" class="editBtn editBtn-info"><i class="fa fa-issue"></i>下发参数</button>&ensp;';
                    } else {
                        result += '<button id="sendParamButton_' + row.vehicleId + '" disabled type="button" class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>下发参数</button>&ensp;';
                    }
                    //其他
                    if (row.bindId != null && row.bindId != '') {
                        result += '<div class="btn-group"><button class="editBtn editBtn-info" type="button" id="dropdownMenuOther" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">其他<span class="caret"></span></button>' +
                            '<ul class="dropdown-menu" style="width:357px;" aria-labelledby="dropdownMenuOther" id="dropdownMenuContent">' +
                            '<li><a href="' + readBasicInfoUrlPre.replace("{id}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonSmWin">读取外设基本信息</a></li>' +
                            '<li><a href="' + readStatusInfoUrlPre.replace("{id}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonSmWin">读取外设状态信息</a></li>' +
                            '<li><a href="' + readDsmInfoUrlPre.replace("{id}", row.vehicleId).replace("{brand}", row.brand) + '" data-toggle="modal" data-target="#commonWin">读取外设参数设置</a></li>' +
                            '</ul></div>';
                    } else {
                        result += '<button class="editBtn editBtn-info" type="button" id="DisDropdownMenuOther" data-toggle="" aria-haspopup="true" aria-expanded="true">其他<span class="caret"></span></button>';
                    }
                    return result;
                }
            }, {
                "data": "brand",
                "class": "text-center"
            }, {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (protocol == '28') {
                        return "交通部JT/T808-2019(粤标)"
                    }
                }
            }, {
                "data": "groupName",
                "class": "text-center"
            }, {
                "data": "onLineStatus",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == "3") {
                        return '离线';
                    }
                    return '在线';

                }
            }, {
                "data": "forward",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return riskDefineSettings.renderSendStatus(data);
                }
            }, {
                "data": "driverBehavior",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return riskDefineSettings.renderSendStatus(data);
                }
            }, {
                "data": "tirePressure",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return riskDefineSettings.renderSendStatus(data);
                }
            }, {
                "data": "blindDetection",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return riskDefineSettings.renderSendStatus(data);
                }
            },
            ];

            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
                d.groupId = selectGroupId;
                d.assignmentId = selectAssignId;
                d.protocol = protocol;
                if (adanceFlag) {
                    d.terminalCategory = terminalCategory;
                    d.sendStatus = sendStatus;
                    d.statusInfo = statusInfo;
                } else {
                    d.terminalCategory = '';
                    d.sendStatus = '';
                    d.statusInfo = '';
                }
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/adas/standard/param/list',
                editUrl: '/clbs/adas/standard/param/setting_' + protocol + '_',
                deleteUrl: '/clbs/adas/standard/param/delete_',
                deletemoreUrl: '/clbs/adas/standard/param/deletemore',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'yueDataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                lengthMenu: [5, 10, 20, 50, 100],
            };

            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
        },
        // 组织架构树初始化
        ztreeInit: function () {
            var treeSetting = {
                async: {
                    url: "/clbs/m/basicinfo/enterprise/assignment/assignmentTree",
                    tyoe: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: { // 是否可选  Organization
                        "isOrg": "1"
                    },
                    dataFilter: riskDefineSettings.ajaxDataFilter
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
                    onClick: riskDefineSettings.zTreeOnClick
                }
            };
            $.fn.zTree.init($("#treeDemo"), treeSetting, null);
        }
        ,
        updataFenceData: function (msg) {
            if (msg != null) {
                var result = $.parseJSON(msg.body);
                if (result != null) {
                    if (updateTime != null) return;
                    updateTime = setTimeout(function () {
                        myTable.refresh();
                        clearTimeout(updateTime);
                        updateTime = null;
                    }, 2000);
                }
            }
        }
        ,
        //组织树预处理函数
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (responseData) {
                for (var i = 0; i < responseData.length; i++) {
                    responseData[i].open = true;
                }
            }
            return responseData;
        }
        ,
        //点击节点
        zTreeOnClick: function (event, treeId, treeNode) {
            if (treeNode.type == "assignment") {
                selectAssignId = treeNode.id;
                selectTreepId = treeNode.pId;
                selectGroupId = '';
            } else {
                selectGroupId = treeNode.uuid;
                selectTreepId = treeNode.id;
                selectAssignId = '';
            }
            myTable.requestData();
        }
        ,
        //全选
        checkAllClick: function () {
            var subChk = $(this).closest('table').find("input[name='subChk']");
            subChk.prop("checked", this.checked);

            var checkedList = new Array();
            checkSubChk = $(this).closest('table').find("input[name='subChk']:checked");
            checkSubChk.each(function () {
                checkedList.push($(this).val());
            });
            var settingUrl = settingMoreUrl + checkedList.toString() + ".gsp";
            if (protocol != 1) {
                settingMoreUrl = "/clbs/adas/standard/param/setting_";
                settingUrl = settingMoreUrl + protocol + '_' + checkedList.toString() + ".gsp";
            }
            $("#settingMoreBtn").attr("href", settingUrl);

        },
        subChkChange: function (event) {
            var curTable = $(event).closest('table');
            var subChk = curTable.find("input[name='subChk']");
            curTable.find(".checkAll").prop(
                "checked",
                subChk.length == subChk.filter(":checked").length ? true
                    : false);
            var checkedList = new Array();
            curTable.find("input[name='subChk']:checked").each(function () {
                checkedList.push($(this).val());
            });
            var settingUrl = settingMoreUrl + checkedList.toString() + ".gsp";
            if (protocol != 1) {
                settingMoreUrl = "/clbs/adas/standard/param/setting_";
                settingUrl = settingMoreUrl + protocol + '_' + checkedList.toString() + ".gsp";
            }
            $("#settingMoreBtn").attr("href", settingUrl);
        }
        ,
        pubSendParam: function (vehicleIds) {
            var url = '';
            if (protocol == 24) {
                url = "/clbs/adas/standard/param/jing/sendParamSet";
            } else {
                url = "/clbs/adas/standard/param/sendParamSet";
            }

            var parameter = {"vehicleIds": vehicleIds};
            json_ajax("POST", url, "json", true, parameter, riskDefineSettings.sendParamCallback);
        },

        sendParam: function (vehicleIds) {
            var url = "/clbs//adas/paramSetting/sendParameter";
            if (protocol != '1') {// 除了黑标
                url = "/clbs/adas/standard/param/sendParamSet";
            }
            if (protocol == '24') {
                url = "/clbs/adas/standard/param/jing/sendParamSet";
            }
            var parameter = {"vehicleIds": vehicleIds};
            json_ajax("POST", url, "json", true, parameter, riskDefineSettings.sendParamCallback);
        },
        // 下发流量回调方法
        sendParamCallback: function (data) {
            if (risksendflag) {
                webSocket.subscribe(headers, "/user/topic/t808_currency_response", riskDefineSettings.updataFenceData, null, null);
                risksendflag = false;
            }
            if (data != null && data != undefined && data != "") {
                if (data.success) {
                    layer.msg("下发完成！", {time: 2000}, function (refresh) {
                        //取消全选勾
                        $(".checkAll").prop('checked', false);
                        $("input[name=subChk]").prop("checked", false);
                        myTable.refresh(); //执行的刷新语句
                        layer.close(refresh);
                    });
                } else {
                    var msg = data.msg || '下发失败';
                    layer.msg(msg, {time: 2000}, function (refresh) {
                        //取消全选勾
                        $(".checkAll").prop('checked', false);
                        $("input[name=subChk]").prop("checked", false);
                        myTable.refresh(); //执行的刷新语句
                        layer.close(refresh);
                    });
                }
            }
        }
        ,
        //批量删除
        delModelClick: function () {
            /*var checkedInput = [];
            if(protocol == '12' || protocol == '14'){//川桂
                checkedInput = $("#publicDataTable input[name='subChk']:checked");
            }else if(protocol == '13' || protocol == '15'){//苏、冀
                checkedInput = $("#suDataTable input[name='subChk']:checked");
            }else if(protocol == '16' || protocol == '17' || protocol == '18'){//浙，吉，陕
                checkedInput = $("#shanDataTable input[name='subChk']:checked");
            }else{
                checkedInput = $("#dataTable input[name='subChk']:checked");
            }*/

            var checkedInput = $("input[name='subChk']:checked");
            if (checkedInput.length == 0) {
                layer.msg("至少选择一项！");
                return
            }

            var checkedList = new Array();
            for (var i = 0, len = checkedInput.length; i < len; i++) {
                checkedList.push($(checkedInput[i]).val());
                if ($("#sendParamButton_" + $(checkedInput[i]).val()).attr("class").indexOf("editBtn-info") < 0) {
                    checkedList = new Array();
                    layer.msg("有勾选项没有设置参数!");
                    return;
                }
            }
            myTable.deleteItems({
                'deltems': checkedList.toString()
            });
            /*去除批量设置未选择,但会弹出*/
            $("#settingMoreBtn").attr("href", settingMoreUrl + ".gsp");
        }
        ,
        //批量下发
        sendModelClick: function () {
            //判断是否至少选择一项
            var chechedNum = $("input[name='subChk']:checked").length;
            if (chechedNum == 0) {
                layer.msg("至少选择一项！");
                return
            }
            var checkedList = new Array();
            $("input[name='subChk']:checked").each(function () {
                checkedList.push($(this).val());
                if ($("#sendParamButton_" + $(this).val()).attr("class").indexOf("editBtn-info") < 0) {
                    checkedList = new Array();
                    layer.msg("有勾选项没有设置参数!");
                    return false;
                }
            });
            if (checkedList.length > 0) {
                riskDefineSettings.sendParam(checkedList.toString());
                /*去除批量设置未选择,但会弹出*/
                $("#settingMoreBtn").attr("href", settingMoreUrl + ".gsp");
            }
        }
        ,
        //刷新列表
        refreshTable: function () {
            selectGroupId = '';
            selectAssignId = '';
            $("#simpleQueryParam").val("");
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.selectNode("");
            zTree.cancelSelectedNode();
            adanceFlag = false;
            myTable.requestData();
        }
        ,
        //高级查询条件是否显示
        showAdvancedContent: function () {
            var data = [ //加入index字段是因为有的协议的value跟平台是一样的，但是名字跟平台有出入。如index14和index1是同一个value，但是name字段不一样
                {index: 1, name: '前向监测', value: 1},
                {index: 2, name: '驾驶员行为监测', value: 2},
                {index: 3, name: '激烈驾驶', value: 3},
                {index: 4, name: '盲区监测', value: 4},
                {index: 5, name: '胎压监测', value: 5},
                {index: 6, name: '上下客及超员', value: 6},
                {index: 7, name: '驾驶员行为', value: 7},
                {index: 8, name: '车辆运行监测', value: 8},
                {index: 9, name: '驾驶员身份识别', value: 9},
                {index: 10, name: '车辆运行监测', value: 10},
                {index: 11, name: '驾驶行为监测', value: 11},
                {index: 12, name: '设备失效监测', value: 12},
                {index: 13, name: '驾驶员比对', value: 13},
                {index: 14, name: '高级驾驶辅助', value: 1},
                {index: 15, name: '驾驶员状态监测', value: 2},
                {index: 16, name: '盲点监测', value: 4},
                {index: 17, name: '车辆监测', value: 6},
            ]
            var toRenderOptions = []
            switch (Number(protocol)) {
                case 21:
                case 15:
                    // 中位 苏
                    toRenderOptions = [1, 2, 4, 5]
                    break;
                case 13:
                    // 冀
                    toRenderOptions = [1, 2]
                    break;
                case 12:
                case 14:
                    // 川 桂
                    toRenderOptions = [1, 2, 3, 4, 5]
                    break
                case 16:
                case 17:
                case 18:
                case 19:
                    // 浙 吉 陕 赣
                    toRenderOptions = [1, 2, 4]
                    break;
                case 20:
                    // 沪
                    toRenderOptions = [1, 2, 4, 6]
                    break;
                case 24:
                    // 京
                    toRenderOptions = [7, 8]
                    break;
                case 25:
                    // 黑
                    toRenderOptions = [9, 10, 11, 12]
                    break;
                case 26:
                    // 鲁
                    toRenderOptions = [14, 15, 16, 5, 13]
                    break;
                case 27:
                    // 湘
                    toRenderOptions = [14, 15, 17, 13]
                    break;
                case 28:
                    // 粤
                    toRenderOptions = [14, 15, 5, 16]
                    break;
            }
            var optionsHtml = '<option value="0">全部</option>'
            toRenderOptions.forEach(function (value) {
                var res = data.find(function (item) {
                    return item.index == value
                })
                optionsHtml += '<option value="' + res.value + '">' + res.name + '</option>'
            })
            $('#deviceType').html(optionsHtml)
            var advancedContent = $("#advanced_content");
            if (advancedContent.is(':visible')) {
                advancedContent.slideUp();
            } else {
                advancedContent.slideDown();
            }
        },
        // 还原高级查询条件
        emptyAdvancedContent: function () {
            $('#deviceType,#sendState,#stateInfo').val('0');
            $('#sendState').val('');
        },
        // 协议类型切换
        deviceChange: function (defaultProtocol) {
            adanceFlag = false;
            protocol = (defaultProtocol instanceof Object) ? $(this).val() : defaultProtocol;
            $('#dataTable').hide();
            $('#dataTable_wrapper').hide();
            $('#publicDataTable_wrapper').hide();
            $("#suDataTable_wrapper").hide();
            $("#shanDataTable_wrapper").hide();
            $('#advancedBox').hide();
            $("#jiDataTable_wrapper").hide();
            $('.chuanInfo').hide();
            $('.jiTh').hide();
            $('#advanced_content').hide();
            $("#huDataTable_wrapper").hide();
            $("#jingDataTable_wrapper").hide();
            $('#heiDataTable_wrapper').hide();
            $('#luDataTable_wrapper').hide()
            $('#xiangDataTable_wrapper').hide()
            $('#yueDataTable_wrapper').hide()
            switch (protocol) {
                case '1':// 中位标准(2013)
                    $('#dataTable').show();
                    $('.jiInfo').show();
                    settingMoreUrl = "/clbs/r/riskManagement/DefineSettings/settingmore_";
                    riskDefineSettings.showColSetting();
                    riskDefineSettings.zwTableInit();
                    break;
                case '14':// 桂标
                case '12':// 川标
                    $('#publicDataTable').show();
                    $('#advancedBox').show();
                    $('.chuanInfo').show();
                    $('.jiInfo').show();
                    settingMoreUrl = "/clbs/adas/standard/param/setting_";
                    riskDefineSettings.showColSetting();
                    riskDefineSettings.publicTableInit();
                    break;
                case heiProtocol:// 黑标
                    $('#heiDataTable').show();
                    $('#advancedBox').show();
                    $('.chuanInfo').show();
                    settingMoreUrl = "/clbs/adas/standard/param/setting_";
                    riskDefineSettings.showColSetting();
                    riskDefineSettings.hljTableInit();
                    break;
                case '15':// 苏标
                case '21':// 中位标准(2019)
                    $("#suDataTable").show();
                    $('#advancedBox').show();
                    $('.jiTh').show();
                    $('.jiInfo').show();
                    settingMoreUrl = "/clbs/adas/standard/param/setting_";
                    riskDefineSettings.showColSetting();
                    riskDefineSettings.suTableInit();
                    break;
                case '13':// 冀标
                    $('#jiDataTable').show();
                    $('#advancedBox').show();
                    $('.jiTh').show();
                    settingMoreUrl = "/clbs/adas/standard/param/setting_";
                    riskDefineSettings.showColSetting();
                    riskDefineSettings.jiTableInit();
                    break;
                case '16':// 浙标
                case '17':// 吉标
                case '18':// 陕标
                case '19':// 赣标
                    $("#shanDataTable").show();
                    $('#advancedBox').show();
                    $('.jiTh').show();
                    $('.jiInfo').show();
                    settingMoreUrl = "/clbs/adas/standard/param/setting_";
                    riskDefineSettings.showColSetting();
                    riskDefineSettings.shanTableInit();
                    break;
                case '20': //沪标
                    $("#huDataTable").show();
                    $('#advancedBox').show();
                    $('.jiTh').show();
                    $('.jiInfo').show();
                    settingMoreUrl = "/clbs/adas/standard/param/setting_";
                    riskDefineSettings.showColSetting();
                    riskDefineSettings.huTableInit();
                    break;
                case '24': //京标
                    $('#jingDataTable').show();
                    $('#advancedBox').show();
                    $('.jiTh').show();
                    settingMoreUrl = "/clbs/adas/standard/param/setting_";
                    riskDefineSettings.showColSetting();
                    riskDefineSettings.jingTableInit();
                    break;
                case LU_PROTOCOL_VALUE: // 鲁标
                    $('#luDataTable').show();
                    $('#advancedBox').show();
                    settingMoreUrl = "/clbs/adas/standard/param/setting_";
                    riskDefineSettings.showColSetting();
                    riskDefineSettings.luTableInit();
                    break;
                case XIANG_PROTOCOL_VALUE: // 湘标
                    $('#xiangDataTable').show();
                    $('#advancedBox').show();
                    settingMoreUrl = "/clbs/adas/standard/param/setting_";
                    riskDefineSettings.showColSetting();
                    riskDefineSettings.xiangTableInit();
                    break;
                case YUE_PROTOCOL_VALUE: // 粤标
                    $('#yueDataTable').show();
                    $('#advancedBox').show();
                    settingMoreUrl = "/clbs/adas/standard/param/setting_";
                    riskDefineSettings.showColSetting();
                    riskDefineSettings.yueTableInit();
                    break;
                default:
                    break;
            }
            riskDefineSettings.emptyAdvancedContent();
            //取消全选
            $(".checkAll").prop('checked', false);
            //还原批量设置的地址
            if (protocol != 1) {
                settingMoreUrl = "/clbs/adas/standard/param/setting_";
                settingMoreUrl = settingMoreUrl + protocol + '_';
            }
            $("#settingMoreBtn").attr("href", settingMoreUrl + '.gsp');
            //全选
            $(".checkAll").bind("click", riskDefineSettings.checkAllClick);

            var heightNum = 628;
            if (protocol != '1') {
                heightNum = 670;
            }
            $('.leftTreeBox .panelCarBg').css('height', heightNum + 'px');
            $('.leftTreeBox #treeDemo').css('height', heightNum - 15 + 'px');
        },
        /* deviceChange: function (defaultProtocol) {
             adanceFlag = false;
             protocol = (defaultProtocol instanceof Object) ? $(this).val() : defaultProtocol;
             $('#publicDataTable_wrapper').hide();
             $("#suDataTable_wrapper").hide();
             $("#shanDataTable_wrapper").hide();
             $('#advancedBox').hide();
             $("#jiDataTable_wrapper").hide();
             $('.chuanInfo').hide();
             $('.jiTh').hide();
             $('#advanced_content').hide();
             $("#huDataTable_wrapper").hide();
             $("#jingDataTable_wrapper").hide();
             $('#heiDataTable_wrapper').hide();
             switch (protocol) {
                 case '1':// 中位标准(2013)
                     $('#dataTable').show();
                     $('#publicDataTable').hide();
                     $('#publicDataTable').next('.row').hide();
                     $("#suDataTable").hide();
                     $("#suDataTable").next('.row').hide();
                     $("#shanDataTable").hide();
                     $('#shanDataTable').next('.row').hide();
                     $('#advancedBox').hide();
                     $("#jiDataTable").hide();
                     $('#jiDataTable').next('.row').hide();
                     $('.chuanInfo').hide();
                     $('.jiTh').hide();
                     $('#advanced_content').hide();
                     $("#huDataTable").hide();
                     $("#huDataTable").next('.row').hide();
                     $("#jingDataTable").hide();
                     $("#jingDataTable").next('.row').hide();
                     $('.jiInfo').show();
                     settingMoreUrl = "/clbs/r/riskManagement/DefineSettings/settingmore_";
                     riskDefineSettings.showColSetting();
                     riskDefineSettings.zwTableInit();
                     break;
                 case '14':// 桂标
                 case '12':// 川标
                     $('#publicDataTable').show();
                     $('#advancedBox').show();
                     $('.chuanInfo').show();
                     $('.jiTh').hide();
                     $('#dataTable').hide();
                     $('#dataTable').next('.row').hide();
                     $("#suDataTable").hide();
                     $('#suDataTable').next('.row').hide();
                     $("#shanDataTable").hide();
                     $('#shanDataTable').next('.row').hide();
                     $("#jiDataTable").hide();
                     $('#jiDataTable').next('.row').hide();
                     $("#huDataTable").hide();
                     $("#huDataTable").next('.row').hide();
                     $("#jingDataTable").hide();
                     $("#jingDataTable").next('.row').hide();
                     $('.jiInfo').show();
                     settingMoreUrl = "/clbs/adas/standard/param/setting_";
                     riskDefineSettings.showColSetting();
                     riskDefineSettings.publicTableInit();
                     break;
                 case heiProtocol:// 黑标
                     $('#heiDataTable').show();
                     $('#publicDataTable').hide();
                     $('#advancedBox').show();
                     $('.chuanInfo').show();
                     $('.jiTh').hide();

                     $('#dataTable').hide();
                     $('#dataTable').next('.row').hide();
                     $("#suDataTable").hide();
                     $('#suDataTable').next('.row').hide();
                     $("#shanDataTable").hide();
                     $('#shanDataTable').next('.row').hide();
                     $("#jiDataTable").hide();
                     $('#jiDataTable').next('.row').hide();
                     $("#huDataTable").hide();
                     $("#huDataTable").next('.row').hide();
                     $("#jingDataTable").hide();
                     $("#jingDataTable").next('.row').hide();
                     $('.jiInfo').hide();
                     settingMoreUrl = "/clbs/adas/standard/param/setting_";
                     riskDefineSettings.showColSetting();
                     riskDefineSettings.hljTableInit();
                     break;
                 case '15':// 苏标
                 case '21':// 中位标准(2019)
                     $("#suDataTable").show();
                     $('#advancedBox').show();
                     $('.jiTh').show();
                     $('.chuanInfo').hide();
                     $('#dataTable').hide();
                     $('#dataTable').next('.row').hide();
                     $('#publicDataTable').hide();
                     $('#publicDataTable').next('.row').hide();
                     $("#shanDataTable").hide();
                     $('#shanDataTable').next('.row').hide();
                     $("#jiDataTable").hide();
                     $('#jiDataTable').next('.row').hide();
                     $("#huDataTable").hide();
                     $("#huDataTable").next('.row').hide();
                     $("#jingDataTable").hide();
                     $("#jingDataTable").next('.row').hide();
                     $('.jiInfo').show();
                     settingMoreUrl = "/clbs/adas/standard/param/setting_";
                     riskDefineSettings.showColSetting();
                     riskDefineSettings.suTableInit();
                     break;
                 case '13':// 冀标
                     $('#jiDataTable').show();
                     $('#advancedBox').show();
                     $('.jiTh').show();
                     $('.chuanInfo').hide();
                     $('#dataTable').hide();
                     $('#dataTable').next('.row').hide();
                     $('#publicDataTable').hide();
                     $('#publicDataTable').next('.row').hide();
                     $("#shanDataTable").hide();
                     $('#shanDataTable').next('.row').hide();
                     $("#suDataTable").hide();
                     $('#suDataTable').next('.row').hide();
                     $("#huDataTable").hide();
                     $("#huDataTable").next('.row').hide();
                     $("#jingDataTable").hide();
                     $("#jingDataTable").next('.row').hide();
                     $('.jiInfo').hide();
                     settingMoreUrl = "/clbs/adas/standard/param/setting_";
                     riskDefineSettings.showColSetting();
                     riskDefineSettings.jiTableInit();
                     break;
                 case '16':// 浙标
                 case '17':// 吉标
                 case '18':// 陕标
                 case '19':// 赣标
                     $("#shanDataTable").show();
                     $('#advancedBox').show();
                     $('.jiTh').show();
                     $('.chuanInfo').hide();
                     $('#dataTable').hide();
                     $('#dataTable').next('.row').hide();
                     $('#publicDataTable').hide();
                     $('#publicDataTable').next('.row').hide();
                     $("#suDataTable").hide();
                     $('#suDataTable').next('.row').hide();
                     $("#jiDataTable").hide();
                     $('#jiDataTable').next('.row').hide();
                     $("#huDataTable").hide();
                     $("#huDataTable").next('.row').hide();
                     $("#jingDataTable").hide();
                     $("#jingDataTable").next('.row').hide();
                     $('.jiInfo').show();
                     settingMoreUrl = "/clbs/adas/standard/param/setting_";
                     riskDefineSettings.showColSetting();
                     riskDefineSettings.shanTableInit();
                     break;
                 case '20': //沪标
                     $("#huDataTable").show();
                     $('#advancedBox').show();
                     $('.jiTh').show();
                     $('.chuanInfo').hide();
                     $('#dataTable').hide();
                     $('#dataTable').next('.row').hide();
                     $('#publicDataTable').hide();
                     $('#publicDataTable').next('.row').hide();
                     $("#shanDataTable").hide();
                     $('#shanDataTable').next('.row').hide();
                     $("#jiDataTable").hide();
                     $('#jiDataTable').next('.row').hide();
                     $("#suDataTable").hide();
                     $("#suDataTable").next('.row').hide();
                     $("#jingDataTable").hide();
                     $("#jingDataTable").next('.row').hide();
                     $('.jiInfo').show();
                     settingMoreUrl = "/clbs/adas/standard/param/setting_";
                     riskDefineSettings.showColSetting();
                     riskDefineSettings.huTableInit();
                     break;
                 case '24': //京标
                     $('#jingDataTable').show();
                     $('#advancedBox').show();
                     $('.jiTh').show();
                     $('.chuanInfo').hide();
                     $('#dataTable').hide();
                     $('#dataTable').next('.row').hide();
                     $('#publicDataTable').hide();
                     $('#publicDataTable').next('.row').hide();
                     $("#shanDataTable").hide();
                     $('#shanDataTable').next('.row').hide();
                     $("#suDataTable").hide();
                     $('#suDataTable').next('.row').hide();
                     $("#huDataTable").hide();
                     $("#huDataTable").next('.row').hide();
                     $('#jiDataTable').hide();
                     $("#jiDataTable").next('.row').hide();
                     $('.jiInfo').hide();
                     settingMoreUrl = "/clbs/adas/standard/param/setting_";
                     riskDefineSettings.showColSetting();
                     riskDefineSettings.jingTableInit();
                     break;
                 default:
                     break;
             }
             $('#advanced_content').hide();
             riskDefineSettings.emptyAdvancedContent();
             //取消全选
             $(".checkAll").prop('checked', false);
             //还原批量设置的地址
             if (protocol != 1) {
                 settingMoreUrl = "/clbs/adas/standard/param/setting_";
                 settingMoreUrl = settingMoreUrl + protocol + '_';
             }
             $("#settingMoreBtn").attr("href", settingMoreUrl + '.gsp');
             //全选
             $(".checkAll").bind("click", riskDefineSettings.checkAllClick);

             var heightNum = 628;
             if (protocol != '1') {
                 heightNum = 670;
             }
             $('.leftTreeBox .panelCarBg').css('height', heightNum + 'px');
             $('.leftTreeBox #treeDemo').css('height', heightNum - 15 + 'px');
         },*/
        // 高级查询
        advancedInquire: function () {
            adanceFlag = true;
            terminalCategory = $('#deviceType').val();
            sendStatus = $('#sendState').val();
            statusInfo = $('#stateInfo').val();
            myTable.requestData();
        },
        // 渲染下发状态
        renderSendStatus: function (data) {
            switch (data) {
                case 0:
                    return '参数已生效';
                case 1:
                    return '参数未生效';
                case 2:
                    return '参数消息有误';
                case 3:
                    return '参数不支持';
                case 4:
                    return '参数下发中';
                case 5:
                    return '终端离线，未下发';
                case 7:
                    return '终端处理中';
                case 8:
                    return '终端接收失败';
                default:
                    return '';
            }
        }
    };
    $(function () {
        riskDefineSettings.init();
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'search_condition') {
                search_ztree('treeDemo', id, 'assignment');
            }
        });
        //IE9
        if (navigator.appName == "Microsoft Internet Explorer" && navigator.appVersion.split(";")[1].replace(/[ ]/g, "") == "MSIE9.0") {
            var search;
            $("#search_condition").bind("focus", function () {
                search = setInterval(function () {
                    search_ztree('treeDemo', 'search_condition', 'assignment');
                }, 500);
            }).bind("blur", function () {
                clearInterval(search);
            });
        }
        //IE9 end
        // 组织架构模糊搜索
        $("#search_condition").on("input oninput", function () {
            search_ztree('treeDemo', 'search_condition', 'assignment');
        });
        // 每个参数读取模态框关闭时取消所有订阅
        // 如果在读取参数的界面订阅的地址中（格式："/user/topic/per" + type + "Info"）的type没有包含在以下页面，那么请添加在下面数组中
        var socketTypes = [62308, 62309, 62320, 62311, 62310, 57656, 57657, 57664, 57665, 61673, 62312, 61665, 61666, 61668, 61667, 61921, 61922, 61924, 61923]
        $('#commonWin').on('hidden.bs.modal', function (e) {
            for (var i = 0; i < socketTypes.length; i++) {
                webSocket.unsubscribe("/user/topic/per" + socketTypes[i] + "Info")
            }
        })
        //全选
        $(".checkAll").bind("click", riskDefineSettings.checkAllClick);
        //批量删除
        $("#del_model").bind("click", riskDefineSettings.delModelClick);
        $("#refreshTable").on("click", riskDefineSettings.refreshTable);
        // 批量下发
        $("#sendMoreBtn").on("click", riskDefineSettings.sendModelClick);

        //切换组织架构中的协议类型
        $(".device").on('change', riskDefineSettings.deviceChange);

        $("#search_button").on("click", function () {
            adanceFlag = false;
            myTable.requestData();
        });
        // 高级查询
        $("#advanced_search").on("click", riskDefineSettings.showAdvancedContent);
        $("#inquireClickOne").on("click", riskDefineSettings.advancedInquire);
        $("#emptyBtn").on("click", riskDefineSettings.emptyAdvancedContent);
    })
}(window, $))