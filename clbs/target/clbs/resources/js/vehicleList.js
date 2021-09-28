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
    window.checkMonitorObj = {// 存放勾选的监控对象信息
        id: [],
        name: [],
    };
    vehicleList = {
        //初始化
        init: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(1)");
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            ;
            $("#Ul-menu-text").html(menu_text);

            //表格列定义
            var columnDefs = [{
                //第一列，用来显示序号
                "searchable": false,
                "orderable": false,
                "targets": 0
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
                        result += '<input type="checkbox" name="subChk" data-monitorName="' + row.brand + '"  value="' + row.id + '" />';
                        return result;
                    }
                },
                {
                    "data": null,
                    "class": "text-center", //最后一列，操作按钮
                    render: function (data, type, row, meta) {
                        var editUrlPath = myTable.editUrl + row.id + ".gsp"; //修改地址
                        var detailUrlPath = myTable.detailUrl + row.id + ".gsp"; //修改地址
                        var result = '';
                        //修改按钮
                        result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                        result += '<button href="' + detailUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-sun-o"></i>详情</button>&ensp;';
                        result += '<button type="button" class="editBtn editBtn-info" onclick="vehicleList.haveMaintain(\'' + row.id + '\',\'' + row.vehicleType + '\')"><i class="fa fa-sun-o"></i>已保养</button>&ensp;';
                        result += '<button type="button" onclick="vehicleList.deleteItemAndCheckBond(\'' + row.id + '\', \'' + row.brand + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
                        return result;
                    }
                },
                {
                    "data": "brand",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }

                },
                {
                    "data": "groupName",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "deviceNumber",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "simcardNumber",
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
                    "data": "assign",
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
                    "data": "standard",
                    "class": "text-center",
                    render: function (data) {
                        switch (data) {
                            case 0:
                                return '通用';
                                break;
                            case 1:
                                return '货运';
                                break;
                            case 2:
                                return '工程机械';
                                break;
                            default:
                                return '';
                                break;
                        }
                    }
                }, {
                    "data": "vehicleCategoryName",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "vehType",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "roadTransportNumber",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "licenseNo",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                },
                {
                    "data": "aliases",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "vehicleOwner",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "vehicleOwnerPhone",
                    "class": "text-center", render: function (data, type, row, meta) {
                        if (data != null) {
                            return data;
                        } else {
                            return "";
                        }
                    }
                }, {
                    "data": "vehicleLevel",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                },
                {
                    "data": "phoneCheck",
                    "class": "text-center",
                    render: function (data) {
                        switch (data) {
                            case '-1':
                                return '';
                                break;
                            case '0':
                                return '未校验';
                                break;
                            case '1':
                                return '已校验';
                                break;
                            default:
                                return '';
                                break;
                        }
                    }
                }, {
                    "data": "vehicleColor",
                    "class": "text-center",
                    render: function (data) {
                        switch (data) {
                            case '0':
                                return '黑色';
                                break;
                            case '1':
                                return '白色';
                                break;
                            case '2':
                                return '红色';
                                break;
                            case '3':
                                return '蓝色';
                                break;
                            case '4':
                                return '紫色';
                                break;
                            case '5':
                                return '黄色';
                                break;
                            case '6':
                                return '绿色';
                                break;
                            case '7':
                                return '粉色';
                                break;
                            case '8':
                                return '棕色';
                                break;
                            case '9':
                                return '灰色';
                                break;
                        }
                    }
                }, {
                    "data": "plateColor",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return getPlateColor(data);
                    }

                }, {
                    "data": "fuelType",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return data;
                        } else {
                            return '';
                        }
                    }
                },
                {
                    "data": "areaAttribute",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return data;
                        } else {
                            return "";
                        }
                    }
                }, {
                    "data": "province",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return data;
                        } else {
                            return "";
                        }
                    }
                }, {
                    "data": "city",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return data;
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
                        }
                    }
                }, {
                    "data": "purposeCategory",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return data;
                        } else {
                            return '';
                        }
                    }
                }, {
                    "data": "tradeName",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return data;
                        } else {
                            return '';
                        }
                    }
                }, {
                    "data": "numberLoad",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return data;
                        } else {
                            return '';
                        }
                    }
                }, {
                    "data": "loadingQuality",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return data;
                        } else {
                            return '';
                        }
                    }
                }, {
                    "data": "vehicleTechnologyValidityStr",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return (data != null && data.length >= 10) ? data.substr(0, 10) : "";
                    }
                }, {
                    "data": "stateRepair",
                    "class": "text-center",
                    render: function (data) {
                        switch (data) {
                            case 0:
                                return '否';
                                break;
                            case 1:
                                return '是';
                                break;
                            default:
                                return '';
                                break;

                        }
                    }
                }, {
                    "data": "createDataTimeStr",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return (data != null && data.length >= 10) ? data.substr(0, 10) : "";
                    }
                }, {
                    "data": "updateDataTimeStr",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return (data != null && data.length >= 10) ? data.substr(0, 10) : "";
                    }
                }, {
                    "data": "maintainMileage",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        var result = data;
                        if (data === null || data === undefined) {
                            result = '';
                        }
                        return result;
                    }
                }, {
                    "data": "maintainValidityStr",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        var result = data;
                        if (data === null || data === undefined) {
                            result = '';
                        }
                        return result;
                    }
                }, {
                    "data": "vehiclePlatformInstallDateStr",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        var result = data;
                        if (data === null || data === undefined) {
                            result = '';
                        }
                        return result;
                    }
                }, {
                    "data": "remark",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }
            ];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
                d.groupName = selectTreeId;
                d.groupId = selectTreeId;
                d.groupType = selectTreeType;
                d.drivingLicenseType = $("#xsz").val();
                d.roadTransportType = $("#ysz").val();
                d.maintenanceType = $("#maintenanceType").val();
                // d.tipType = vehicleList.getAdvancedData();
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/m/basicinfo/monitoring/vehicle/list',
                editUrl: '/clbs/m/basicinfo/monitoring/vehicle/edit_',
                detailUrl: '/clbs/m/basicinfo/monitoring/vehicle/vehicleDetail_',
                deleteUrl: '/clbs/m/basicinfo/monitoring/vehicle/delete_',
                deletemoreUrl: '/clbs/m/basicinfo/monitoring/vehicle/deletemore',
                enableUrl: '/clbs/c/user/enable_',
                disableUrl: '/clbs/c/user/disable_',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true
            };
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
        },
        // 已保养功能
        haveMaintain: function (vid, vehicleType) {
            var param = {
                vid: vid,
                vehicleType: vehicleType,
                execute: false,
            };
            json_ajax("POST", '/clbs/m/basicinfo/monitoring/vehicle/maintained', "json", true, param, function (data) {
                if (data.success) {
                    layer.confirm(data.msg, { btn: ["确定", "取消"], icon: 3, title: "操作确认" }, function () {
                        var param = {
                            vid: vid,
                            vehicleType: vehicleType,
                            execute: true,
                        };
                        json_ajax("POST", '/clbs/m/basicinfo/monitoring/vehicle/maintained', "json", true, param, function (data) {
                            layer.closeAll();
                            if (data.success) {
                                var currentPage = myTable.dataTable.page();
                                myTable.dataTable.page(currentPage).draw(false);
                            } else if (data.msg) {
                                layer.msg(data.msg);
                            }
                        });
                    })
                } else if (data.msg) {
                    layer.msg(data.msg);
                }
            });
        },
        vehicleListTree: function () {
            var treeSetting = {
                async: {
                    url: "/clbs/m/basicinfo/enterprise/assignment/assignmentTree",
                    tyoe: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {  // 是否可选  Organization
                        "isOrg": "1"
                    },
                    dataFilter: vehicleList.ajaxDataFilter
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
                    onClick: vehicleList.zTreeOnClick
                }
            };
            $.fn.zTree.init($("#treeDemo"), treeSetting, null);
        },
        //组织树预处理函数
        ajaxDataFilter: function (treeId, parentNode, responseData) {
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
        // 删除操作
        deleteItemAndCheckBond: function (id, brand) {
            $("#transValue").val(brand);
            var url = "/clbs/m/basicinfo/monitoring/vehicle/delete_" + id + ".gsp";
            var data = { "id": id };
            layer.confirm('将删除所有历史数据，并且无法恢复！确认删除？', { btn: ["确定", "取消"], icon: 2, title: "操作确认" }, function () {
                json_ajax("POST", url, "json", true, data, vehicleList.deleteCallBack);
            });
        },
        // 删除操作回调
        deleteCallBack: function (data) {
            if (data.success) {
                if (data != null && data.obj != null && data.obj.infoMsg != null) {
                    layer.confirm(data.obj.infoMsg, { btn: ['确认', '取消'] }, function () {
                        // step1:根据车辆id获取车辆终端绑定表id
                        var vid = data.obj.vehicleId;
                        var url = "/clbs/m/infoconfig/infoinput/getConfigIdByVehicleId";
                        var dataId = { "vehicleId": vid };
                        json_ajax("POST", url, "json", true, dataId, vehicleList.getConfigIdByVehicleIdCallBack);
                    });
                } else {
                    layer.closeAll();
                    myTable.refresh();
                }
            } else {
                layer.msg(data.msg);
            }
        },
        // 根据车辆id获取车辆终端绑定表id的回调
        getConfigIdByVehicleIdCallBack: function (data) {
            if (data.success) {
                // step2:根据绑定表id解除车辆与终端的绑定关系
                var configId = data.obj.configId;
                var url = "/clbs/m/infoconfig/infoinput/delete_" + configId + ".gsp";
                json_ajax("POST", url, "json", true, data, vehicleList.deleteConfigByVidCallBack);
            } else {
                layer.msg(data.msg);
            }
        },
        // 根据车辆id获取信息配置表id并删除绑定关系的回调
        deleteConfigByVidCallBack: function (data) {
            if (data.success) {
                // step3:解除之后，再直接删除车辆
                var vid = data.obj.vehicleId;
                var url = "/clbs/m/basicinfo/monitoring/vehicle/delete_" + vid + ".gsp";
                var dataIds = { "id": vid };
                json_ajax("POST", url, "json", true, dataIds, vehicleList.deleteCallBack);
            } else {
                layer.msg(data.msg);
            }
        },
        //单选
        subChk: function () {
            $("#checkAll").prop("checked", $subChk.length == $subChk.filter(":checked").length ? true : false);
        },
        //单选
        subChkTwo: function () {
            $("#checkAllTwo").prop("checked", $subChkTwo.length == $subChkTwo.filter(":checked").length ? true : false);
        },
        // 查询全部 
        queryAll: function () {
            selectTreeId = "";
            selectTreeType = "";
            $('#simpleQueryParam').val("");
            vehicleList.emptyAdvancedContent();
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.selectNode("");
            zTree.cancelSelectedNode();
            myTable.requestData();
        },
        // 显示图片 
        showPhoto: function (fileName) {
            $('#photoDiv').modal('show');
            $('#photoImg').attr('src', "/clbs/upload/" + fileName);
        },
        //批量删除
        deleteMuch: function () {
            //判断是否至少选择一项
            var chechedNum = $("input[name='subChk']:checked").length;
            if (chechedNum == 0) {
                layer.msg(selectItem, { move: false });
                return
            }
            var checkedList = new Array();
            $("input[name='subChk']:checked").each(function () {
                checkedList.push($(this).val());
            });
            var url = "/clbs/m/basicinfo/monitoring/vehicle/deletemore";
            var data = { "checkedList": checkedList.toString() };
            layer.confirm('将删除所有历史数据，并且无法恢复！确认删除？', { btn: ["确定", "取消"], icon: 2, title: "操作确认" }, function () {
                json_ajax_noTimeout("POST", url, "json", true, data, vehicleList.deleteMuchCallBack);
            });
        },
        // 批量删除回调
        deleteMuchCallBack: function (data) {

            if (data.success) {
                if (data != null && data.obj != null && data.obj.infoMsg != null && data.obj.infoMsg.length > 0) {
                    var boundBrandIds = data.obj.boundBrandIds;
                    layer.confirm("已绑定车辆：" + data.obj.boundBrands + "</br>" + data.obj.infoMsg, { btn: ['确认', '取消'] }, function () {
                        if (boundBrandIds != null && boundBrandIds != "") {
                            var url = "/clbs/m/infoconfig/infoinput/getConfigIdByMonitorIds";
                            var param = { "vehicleId": boundBrandIds };
                            json_ajax_noTimeout("POST", url, "json", true, param, vehicleList.getConfigIdByVehicleIdInMuchCallBack);
                        }
                    }, function () {
                        layer.closeAll();
                        var currentPage = myTable.dataTable.page() - 1;
                        currentPage = currentPage < 0 ? 0 : currentPage;
                        myTable.dataTable.page(currentPage).draw(false);
                    });
                } else {
                    layer.closeAll();
                    var currentPage = myTable.dataTable.page() - 1;
                    currentPage = currentPage < 0 ? 0 : currentPage;
                    myTable.dataTable.page(currentPage).draw(false);
                    layer.msg("删除成功");

                }
            } else {
                layer.msg(data.msg);
            }
        },
        getConfigIdByVehicleIdInMuchCallBack: function (data) {
            if (data.success) {
                // step2:根据绑定表id解除车辆与终端的绑定关系
                var configId = data.obj.configId;
                var param = { "deltems": configId };
                var url = "/clbs/m/infoconfig/infoinput/deletemore";
                json_ajax_noTimeout("POST", url, "json", true, param, vehicleList.deleteConfigByVidInMuchCallBack);
            } else {
                layer.msg(data.msg);
            }
        },
        deleteConfigByVidInMuchCallBack: function (data) {
            if (data.success) {
                // step3:解除之后，再直接删除车辆
                var vid = data.obj.vehicleId;
                var url = "/clbs/m/basicinfo/monitoring/vehicle/deletemore";
                var dataIds = { "checkedList": vid };
                json_ajax_noTimeout("POST", url, "json", true, dataIds, vehicleList.deleteMuchCallBack);
            } else {
                layer.msg(data.msg);
            }
        },
        //加载运营类别列表
        purposeCategory: function () {
            var purposeCategoryQuery = $("#purposeCategoryQuery").val();
            var url = "/clbs/m/basicinfo/monitoring/vehicle/purposeCategoryList";
            var data = { "purposeCategory": purposeCategoryQuery };
            json_ajax("POST", url, "json", false, data, vehicleList.findPurposeCategoryList);
        },
        findPurposeCategoryList: function (data) {
            if (data.success) {
                var result = data.records;
                var dataListArray = [];
                var permission = $("#permission").val();
                for (var i = 0; i < result.length; i++) {
                    if (permission == "true") {
                        if (result[i].codeNum == null) {
                            var list = [
                                i + 1,
                                '<input  type="checkbox" name="subChkTwo"  value="' + result[i].id + '" />',
                                '<button onclick="vehicleList.findPurposeCategoryById(\'' + result[i].id + '\')" data-target="#updateVehiclePurpose" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;<button type="button"  onclick="vehicleList.deleteVehiclePurpose(\'' + result[i].id + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>',
                                result[i].purposeCategory,
                                result[i].description
                            ];
                        } else {
                            var list = [
                                i + 1,
                                '<input   name="subChkTwo" style="display: none" />',
                                '<button disabled type="button" class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>修改</button>&ensp;<button type="button"   class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>删除</button>',
                                result[i].purposeCategory,
                                result[i].description
                            ];
                        }
                    } else {
                        var list = [
                            i + 1,
                            result[i].purposeCategory,
                            result[i].description
                        ];
                    }

                    dataListArray.push(list);
                }
                reloadData(dataListArray);//传入数组，调用公用js，详情请看tg_common.js
            } else {
                layer.msg(publicError);
            }
        },
        //新增运营类别
        add: function () {
            if (vehicleList.addValidates()) {
                var purposeCategory = $("#purposeCategory").val();
                var description = $("#description").val();
                var resubmitToken = purposeCategory + description;
                resubmitToken = resubmitToken.split("").reduce(function (a, b) { a = ((a << 5) - a) + b.charCodeAt(0); return a & a }, 0);
                var url = "/clbs/m/basicinfo/monitoring/vehicle/addVehiclePurpose";
                var data = { "purposeCategory": purposeCategory, "description": description ,resubmitToken};
                json_ajax("POST", url, "json", false, data, vehicleList.addCallback);
            }
        },
        //新增运营类别回调函数
        addCallback: function (data) {
            if (data.success == false) {
                layer.msg(data.msg);
            } else {
                layer.msg("新增成功", { move: false });
                $("#addVehiclePurpose").modal('hide');
                $("#purposeCategoryQuery").val("");
                vehicleList.purposeCategory();
                vehicleList.init();
            }
        },
        addValidates: function () {
            $("#purposeCategory-error").hide();//隐藏上次新增时未清除的validate样式
            $("#description-error").hide();
            return $("#addSubmit").validate({
                rules: {
                    purposeCategory: {
                        required: true,
                        zysCheck: true,
                        maxlength: 20,
                        minlength: 2,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/m/basicinfo/monitoring/vehicle/findVehicleUseType",
                            dataType: "json",
                            data: {
                                vehicleUseType: function () {
                                    return $("#purposeCategory").val();
                                },

                            },
                            dataFilter: function (data) {
                                return data;
                            }
                        }

                    },
                    description: {
                        maxlength: 30,
                    }
                },
                messages: {
                    purposeCategory: {
                        required: vehiclePurposeNull,
                        maxlength: publicSize20,
                        minlength: publicMinLength,
                        remote: vehiclePurposeExists
                    },
                    description: {
                        maxlength: publicSize30
                    }
                }
            }).form();
        },
        //搜索运营类别类别
        searchVehiclePurpose: function () {
            vehicleList.purposeCategory();
        },
        //搜索回车键键盘监听事件
        findType: function (event) {
            if (event.keyCode == 13) {
                vehicleList.purposeCategory();
            }
        },

        findPurposeCategoryById: function (id) {
            pId = id;
            var data = { "id": id };
            var url = "/clbs/m/basicinfo/monitoring/vehicle/findVehiclePurposeCategory";
            json_ajax("POST", url, "json", false, data, vehicleList.findVehiclePurposeCallback);
        },
        findVehiclePurposeCallback: function (result) {
            $("#vehiclePurposeCategory-error").hide();//隐藏上次新增时未清除的validate样式
            $("#vehicleDescriptionn-error").hide();
            $("#vehiclePurposeCategory").val(result.purposeCategory);
            $("#vehicleDescription").val(result.description);
            vehicleUserType = $("#vehiclePurposeCategory").val(); //修改窗体弹出时运营类别
            vehicleUserTypeExpliant = $("#vehicleDescription").val(); // 运营类别描述
        },
        validates: function () {
            $("#vehiclePurposeCategory-error").hide();//隐藏上次新增时未清除的validate样式
            $("#vehicleDescriptionn-error").hide();
            $("#vehiclePurposeCategory").removeData("previousValue");//清空validates的验证缓存
            $("#vehicleDescription").removeData("previousValue");//清空validates的验证缓存
            var updateType = $("#vehiclePurposeCategory").val();// 运营类别
            var explains = $("#vehicleDescription").val();// 描述
            if (updateType == vehicleUserType && explains == vehicleUserTypeExpliant) {
                $("#updateVehiclePurpose").modal('hide');
            } else if (updateType == vehicleUserType && explains != vehicleUserTypeExpliant) { //用户只修改了描述
                return $("#updateSubmit").validate({
                    rules: {
                        vehiclePurposeCategory: {
                            required: true,
                            maxlength: 20,
                            minlength: 2
                        },
                        vehicleDescription: {
                            maxlength: 30
                        }
                    },
                    messages: {
                        vehiclePurposeCategory: {
                            required: vehiclePurposeNull,
                            maxlength: publicSize20,
                            minlength: publicMinLength
                        },
                        vehicleDescription: {
                            maxlength: publicSize30
                        }
                    }
                }).form();
            } else { //用户修改了用途
                return $("#updateSubmit").validate({
                    rules: {
                        vehiclePurposeCategory: {
                            required: true,
                            zysCheck: true,
                            maxlength: 20,
                            minlength: 2,
                            remote: {
                                type: "post",
                                async: false,
                                url: "/clbs/m/basicinfo/monitoring/vehicle/comparison",
                                dataType: "json",
                                data: {
                                    newType: function () {
                                        return $("#vehiclePurposeCategory").val();
                                    },
                                    oldType: function () {
                                        return vehicleUserType;
                                    }

                                },
                                dataFilter: function (data) {
                                    return data;
                                }
                            }
                        },
                        vehicleDescription: {
                            maxlength: 30
                        }
                    },
                    messages: {
                        vehiclePurposeCategory: {
                            required: "运营类别不能为空",
                            maxlength: "长度不能超过20",
                            minlength: publicMinLength,
                            remote: "运营类别已经存在,请勿重复添加"
                        },
                        vehicleDescription: {
                            maxlength: "长度不能超过30"
                        }
                    }
                }).form();
            }
        },
        clearPreviousValue: function () {
            if ($(".remote").data("previousValue")) {
                $(".remote").data("previousValue").old = null;
            }
        },
        updateType: function () {
            if (vehicleList.validates()) {
                $("#vehiclePurposeCategory").removeData("previousValue");//清空validates的验证缓存
                $("#vehicleDescription").removeData("previousValue");//清空validates的验证缓存
                var vehiclePurposeCategory = $("#vehiclePurposeCategory").val();
                var vehicleDescription = $("#vehicleDescription").val();
                var resubmitToken = vehiclePurposeCategory + vehicleDescription;
                resubmitToken = resubmitToken.split("").reduce(function (a, b) { a = ((a << 5) - a) + b.charCodeAt(0); return a & a }, 0);
                var url = "/clbs/m/basicinfo/monitoring/vehicle/updateVehiclePurposeCategory";
                var data = { "id": pId, "purposeCategory": vehiclePurposeCategory, "description": vehicleDescription, resubmitToken };
                json_ajax("POST", url, "json", false, data, vehicleList.updatePurposeCallback);
            }
        },
        updatePurposeCallback: function (data) {
            if (data.success == true) {
                layer.msg("修改成功", { move: false });
                $("#updateVehiclePurpose").modal('hide');
                vehicleList.purposeCategory();
            } else {
                layer.msg(data.msg);
            }
        },
        //根据id删除运营类别
        deleteVehiclePurpose: function (id) {
            layer.confirm(publicDelete, {
                title: '操作确认',
                icon: 3, // 问号图标
                btn: ['确定', '取消'] //按钮
            }, function () {
                var url = "/clbs/m/basicinfo/monitoring/vehicle/deleteVehiclePurpose";
                var data = { "id": id };
                json_ajax("POST", url, "json", false, data, vehicleList.deleteVehiclePurposeCallback);
            });
        },
        //根据id删除运营类别回调函数
        deleteVehiclePurposeCallback: function (result) {
            if (result.success) {
                layer.closeAll('dialog');
                layer.msg("删除成功");
                vehicleList.purposeCategory();
            } else {
                layer.msg(result.msg);
            }
        },
        //批量删除运营类别
        deleteVehiclePurposeMuch: function () {
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
            var url = "/clbs/m/basicinfo/monitoring/vehicle/deleteVehiclePurposeMuch";
            var data = { "ids": ids };
            layer.confirm(publicDelete, {
                title: '操作确认',
                icon: 3, // 问号图标
                btn: ['确定', '取消'] //按钮
            }, function () {
                json_ajax("POST", url, "json", false, data, vehicleList.deleteVehiclePurposeMuchCallback);
            });
        },
        //批量删除运营类别回调函数
        deleteVehiclePurposeMuchCallback: function (result) {
            if (result.success) {
                layer.closeAll('dialog');
                vehicleList.purposeCategory();
            } else {
                layer.msg(result.msg);
            }
        },
        //清除新增运营类别文本框内容
        cleanPueposeVal: function () {
            $("#purposeCategory").val("");
            $("#description").val("");
            $("#purposeCategory-error").hide();//隐藏上次新增时未清除的validate样式
            $("#description-error").hide();
        },
        checkAll: function (e) {
            $("input[name='subChk']").prop("checked", e.checked);
        },
        checkAllTwo: function (e) {
            $("input[name='subChkTwo']").prop("checked", e.checked);
        },
        addId: function () {
            $("#addId").attr("href", "/clbs/m/basicinfo/monitoring/vehicle/add?uuid=" + selectTreepId + "");
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
        //清空高级查询条件
        emptyAdvancedContent: function () {
            $("#xsz").val('0');
            $("#ysz").val('0');
            $("#maintenanceType").val('0');
            $("#simpleQueryParam").val('');
        },
        //高级查询取值
        getAdvancedData: function () {
            var tipType = 0;
            var xszVal = $("#xsz").val();
            var yszVal = $("#ysz").val();
            if (xszVal == "0" && yszVal == "0") {
                tipType = 0;
            }
            else if (xszVal == "1" && yszVal == "0") {
                tipType = 1;
            }
            else if (xszVal == "0" && yszVal == "1") {
                tipType = 2;
            } else {
                tipType = 3;
            }
            return tipType;
        },
        //导出
        exportFun: function () {
            if(getRecordsNum() > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var url = '/clbs/m/basicinfo/monitoring/vehicle/export';
            window.location.href = url;
        },
        // 批量修改
        batchEditFun: function () {
            //判断是否至少选择一项
            var chechedItem = $("input[name='subChk']:checked");
            if (chechedItem.length === 0) {
                layer.msg(selectItem, { move: false });
                return
            }
            checkMonitorObj.id = [];
            checkMonitorObj.name = [];
            for (var i = 0; i < chechedItem.length; i++) {
                var monitorId = $(chechedItem[i]).val();
                var monitorName = $(chechedItem[i]).attr('data-monitorName');
                checkMonitorObj.id.push(monitorId);
                checkMonitorObj.name.push(monitorName);
            }
            $('#batchEditBtn').attr({
                'href': '/clbs/m/basicinfo/monitoring/vehicle/batchEdit.gsp',
                'data-toggle': 'modal',
                'data-target': '#commonWin'
            });
            setTimeout(function () {
                $('#batchEditBtn').attr('href', 'javascript:void(0);');
                $('#batchEditBtn').removeAttr('data-toggle');
                $('#batchEditBtn').removeAttr('data-target');
            }, 300)
        },
    };
    $(function () {
        getTable("dataTables");
        vehicleList.vehicleListTree();
        //动态显示高级查询条件
        var tipType = $.getUrlParam('tipType');
        if (!tipType) {
            $("#advanced_content").slideUp();
        } else {
            $("#advanced_content").slideDown();
            if (tipType == "1") {
                $("#xsz").val("1");
                $("#ysz").val("0");
            } else if (tipType == "2") {
                $("#ysz").val("1");
                $("#xsz").val("0");
            } else {
                $("#xsz").val("0");
                $("#ysz").val("0");
                $("#maintenanceType").val('2');
            }
        }

        vehicleList.init();
        vehicleList.purposeCategory();

        $("#advanced_search").on("click", vehicleList.showAdvancedContent);
        $("#emptyBtn").on("click", vehicleList.emptyAdvancedContent);

        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'search_condition') {
                search_ztree('treeDemo', id, treeSearchType.value);
            }
            ;
        });

        // 输入框输入类型过滤
        inputValueFilter('#purposeCategoryQuery');
        inputValueFilter('#simpleQueryParam');

        //IE9
        if (navigator.appName == "Microsoft Internet Explorer" && navigator.appVersion.split(";")[1].replace(/[ ]/g, "") == "MSIE9.0") {
            var search;
            $("#search_condition").bind("focus", function () {
                search = setInterval(function () {
                    search_ztree('treeDemo', 'search_condition', treeSearchType.value);
                }, 500);
            }).bind("blur", function () {
                clearInterval(search);
            });
        }
        //IE9 end
        //全选(车辆列表)
        $("#checkAll").click(function () {
            $("input[name='subChk']").prop("checked", this.checked);
        });
        //全选(运营类别列表)
        $("#checkAllTwo").click(function () {
            $("input[name='subChkTwo']").prop("checked", this.checked);
        });
        //新增运营类别
        $('#goOverspeedSettings').on("click", vehicleList.add);
        //导出
        $('#exportId').on("click", vehicleList.exportFun);
        // 组织架构模糊搜索
        $("#search_condition").on("input oninput", function () {
            search_ztree('treeDemo', 'search_condition', treeSearchType.value);
        });
        //初始化搜索类型 
        treeSearchType.init();
        treeSearchType.onChange = function (datas) {
            console.log(datas)
            $("#search_condition").attr('placeholder', datas.placeholder)
            search_ztree('treeDemo', 'search_condition', datas.value);
        }
        $subChkTwo.on("click", vehicleList.subChkTwo);
        $('#refreshTable').on("click", vehicleList.queryAll);
        // 批量修改
        $("#batchEditBtn").on("click", vehicleList.batchEditFun);
        // 批量删除
        $("#del_model").on("click", vehicleList.deleteMuch);
        //批量删除运营类别
        $("#del_modelTwo").on("click", vehicleList.deleteVehiclePurposeMuch);
        //运营类别类别搜索
        $("#search_buttonTwo").on("click", vehicleList.searchVehiclePurpose);
        $("#addId").on("click", vehicleList.addId);
        $("#update").on("click", vehicleList.updateType);
        $("#purposeCategory,#description,#vehiclePurposeCategory,#vehicleDescription").on("change", vehicleList.clearPreviousValue);
    })
})($, window)