var professionalsList;
(function ($, window) {
    var selectTreeId = '';
    var isarea = '';
    var orgId = '';
    var zNodes = null;
    var $subChk = $("input[name='subChk']");
    var $subChkTwo = $("input[name='subChk']");
    var pId = "";
    var startpostType;
    var expliant;
    professionalsList = {
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
                        result += '<input  type="checkbox" name="subChk"  value="' + row.id + '" />';
                        return result;
                    }
                },
                {
                    "data": null,
                    "class": "text-center", //最后一列，操作按钮
                    render: function (data, type, row, meta) {
                        var editUrlPath = myTable.editUrl + row.id + ".gsp"; //修改地址
                        var result = '';
                        //修改按钮
                        result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                        //删除按钮
                        result += '<button type="button" onclick="professionalsList.deleteProfessional(\''
                            + row.id
                            + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
                        return result;
                    }
                },
                {
                    "data": "name",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return data
                        } else {
                            return "";
                        }
                    }
                },
                {
                    "data": "groupName",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return data
                        } else {
                            return "";
                        }
                    }
                },
                {
                    "data": "serviceCompany",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            return data
                        } else {
                            return "";
                        }
                    }
                },
                {
                    "data": "type",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                },
                {
                    "data": "identity",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                },
                {
                    "data": "hiredate",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : (data.length >= 10 ? data.substr(0, 10) : "");
                    }
                },
                {
                    "data": "state",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == "0") {
                            return '正常';
                        } else if (data == "1") {
                            return '离职';
                        } else if (data == "2") {
                            return '停用';
                        } else {
                            return "";
                        }
                    }

                },
                {
                    "data": "jobNumber",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : html2Escape(data);
                    }
                },
                {
                    "data": "cardNumber",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : html2Escape(data);
                    }
                },
                {
                    "data": "qualificationCategory",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                },
                {
                    "data": "icCardAgencies",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                },
                {
                    "data": "issueCertificateDate",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                },
                {
                    "data": "icCardEndDate",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                },
                {
                    "data": "gender",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == "1") {
                            return '男';
                        } else if (data == "2") {
                            return '女';
                        } else {
                            return '';
                        }
                    }
                },
                {
                    "data": "birthday",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : (data.length >= 10 ? data.substr(0, 10) : "");
                    }
                }, {
                    "data": "regional",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                }, {
                    "data": "nativePlace",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                }, {
                    "data": "nation",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                }, {
                    "data": "education",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                },
                {
                    "data": "photograph",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data) {
                            var result = "<a onclick='professionalsList.showPhoto(\"" + data
                                + "\")'>查看照片</a>";
                            return result;
                        } else {
                            return '';
                        }
                    }
                }, {
                    "data": "phone",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                }, {
                    "data": "phoneTwo",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                }, {
                    "data": "phoneThree",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                }, {
                    "data": "landline",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                },
                {
                    "data": "emergencyContact",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : html2Escape(data);
                    }
                },
                {
                    "data": "emergencyContactPhone",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                }, {
                    "data": "email",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                }, {
                    "data": "address",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                }, {
                    "data": "operationNumber",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : html2Escape(data);
                    }
                }, {
                    "data": "operationAgencies",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : html2Escape(data);
                    }
                }, {
                    "data": "drivingLicenseNo",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : html2Escape(data);
                    }
                }, {
                    "data": "drivingAgencies",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : html2Escape(data);
                    }
                }, {
                    "data": "drivingType",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            switch (data) {
                                case 0:
                                    data = 'A1(大型客车)';
                                    break;
                                case 1:
                                    data = 'A2(牵引车)';
                                    break;
                                case 2:
                                    data = 'A3(城市公交车)';
                                    break;
                                case 3:
                                    data = 'B1(中型客车)';
                                    break;
                                case 4:
                                    data = 'B2(大型货车)';
                                    break;
                                case 5:
                                    data = 'C1(小型汽车)';
                                    break;
                                case 6:
                                    data = 'C2(小型自动挡汽车)';
                                    break;
                                case 7:
                                    data = 'C3(低速载货汽车)';
                                    break;
                                case 8:
                                    data = 'C4(三轮汽车)';
                                    break;
                                case 9:
                                    data = 'D(普通三轮摩托车)';
                                    break;
                                case 10:
                                    data = 'E(普通二轮摩托车)';
                                    break;
                                case 11:
                                    data = 'F(轻便摩托车)';
                                    break;
                                case 12:
                                    data = 'M(轮式自行机械车)';
                                    break;
                                case 13:
                                    data = 'N(无轨电车)';
                                    break;
                                case 14:
                                    data = 'P(有轨电车)';
                                    break;
                                default:
                                    data = data;
                                    break;
                            }
                        }
                        else {
                            data = '';
                        }
                        return data;
                    }
                }, {
                    "data": "drivingStartDate",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : formatDate(data);
                    }
                }, {
                    "data": "drivingEndDate",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : formatDate(data);
                    }
                }, {
                    "data": "remindDays",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data == null ? "" : data;
                    }
                }, {
                    "data": null,
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        var identityCardPhoto = data.identityCardPhoto;
                        var qualificationCertificatePhoto = data.qualificationCertificatePhoto;
                        var driverLicensePhoto = data.driverLicensePhoto;
                        if ((identityCardPhoto != null && identityCardPhoto != '' && identityCardPhoto != undefined)
                            || (qualificationCertificatePhoto != null && qualificationCertificatePhoto != '' && qualificationCertificatePhoto != undefined)
                            || (driverLicensePhoto != null && driverLicensePhoto != '' && driverLicensePhoto != undefined)
                        ) {
                            return '<span onclick="professionalsList.showPractitionersPhoto(\'' + identityCardPhoto + '\', \'' + qualificationCertificatePhoto + '\', \'' + driverLicensePhoto + '\')" style="color: #6dcff6; cursor: pointer">查看照片</span>';
                        }
                        return '';
                    }
                }, {
                    "data": "faceId",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return data || ""
                    }
                },
            ];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
                d.groupName = selectTreeId;
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/m/basicinfo/enterprise/professionals/list',
                editUrl: '/clbs/m/basicinfo/enterprise/professionals/edit_',
                deleteUrl: '/clbs/m/basicinfo/enterprise/professionals/delete_',
                deletemoreUrl: '/clbs/m/basicinfo/enterprise/professionals/deletemore',
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
        deleteProfessional: function (id) {
            layer.confirm('删掉就没啦，请谨慎下手！', {
                title: '操作确认',
                icon: 3, // 问号图标
                move: false,//禁止拖动
                btn: ['确定', '取消']
            }, function () {
                var url = '/clbs/m/basicinfo/enterprise/professionals/delete_' + id + '.gsp';
                json_ajax("POST", url, "json", false, null, function (data) {
                    layer.closeAll();
                    if (data.success) {
                        myTable.refresh();
                    } else if (data.msg) {
                        layer.confirm(data.msg, {
                            title: '操作确认',
                            icon: 3, // 问号图标
                            move: false,//禁止拖动
                            btn: ['确定', '取消']
                        }, function () {
                            var url = '/clbs/m/basicinfo/enterprise/professionals/confirmDelete';
                            json_ajax("POST", url, "json", false, {id: id}, function (res) {
                                layer.closeAll();
                                if (res.success) {
                                    myTable.refresh();
                                } else if (res.msg) {
                                    layer.msg(res.msg);
                                }
                            });
                        });
                    }
                });
            });
        },
        professionalsTree: function () {
            var treeSetting = {
                async: {
                    url: "/clbs/m/basicinfo/enterprise/professionals/tree",
                    tyoe: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {  // 是否可选  Organization
                        "isOrg": "1"
                    },
                    dataFilter: professionalsList.ajaxDataFilter
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
                    onClick: professionalsList.zTreeOnClick
                }
            };
            $.fn.zTree.init($("#treeDemo"), treeSetting, zNodes);
        },
        //组织树预处理函数
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (responseData) {
                for (var i = 0; i < responseData.length; i++) {
                    responseData[i].open = true;
                }
            }
            orgId = responseData[0].id;
            isarea = responseData[0].isarea;
            return responseData;
        },
        zTreeOnClick: function (event, treeId, treeNode) {
            selectTreeId = treeNode.uuid;
            orgId = treeNode.id;
            isarea = treeNode.isarea;
            myTable.requestData();
        },
        subChk: function () {
            $("#checkAll").prop("checked", $subChk.length == $subChk.filter(":checked").length ? true : false);
        },
        subChkTwo: function () {
            $("input[name='subChkTwo']").prop("checked", this.checked);
            $("#checkAllTwo").prop("checked", $subChkTwo.length == $subChkTwo.filter(":checked").length ? true : false);
        },
        deleteMuth: function () {
            //判断是否至少选择一项
            var chechedNum = $("input[name='subChk']:checked").length;
            if (chechedNum == 0) {
                layer.msg(selectItem);
                return
            }
            var checkedList = new Array();
            $("input[name='subChk']:checked").each(function () {
                checkedList.push($(this).val());
            });
            /*myTable.deleteItems({
                'deltems': checkedList.toString()
            });*/

            layer.confirm('删掉就没啦，请谨慎下手！', {
                title: '操作确认',
                icon: 3, // 问号图标
                move: false,//禁止拖动
                btn: ['确定', '取消']
            }, function () {
                var param = {'deltems': checkedList.toString()};
                var url = '/clbs/m/basicinfo/enterprise/professionals/deletemore';
                json_ajax("POST", url, "json", false, param, function (data) {
                    layer.closeAll();
                    if (data.success) {
                        myTable.refresh();
                        if (data.obj) {
                            var bandPids = data.obj.bandPids;
                            layer.confirm(data.msg, {
                                title: '操作确认',
                                icon: 3, // 问号图标
                                move: false,//禁止拖动
                                btn: ['确定', '取消']
                            }, function () {
                                var url = '/clbs/m/basicinfo/enterprise/professionals/confirmDeleteMore';
                                json_ajax("POST", url, "json", false, {bandPids: bandPids}, function (res) {
                                    layer.closeAll();
                                    if (res.success) {
                                        myTable.refresh();
                                    } else if (res.msg) {
                                        layer.msg(res.msg);
                                    }
                                });
                            });
                        }
                    } else if (data.msg) {
                        layer.msg(res.msg);
                    }
                });
            });
        },
        // 查询全部
        queryAll: function () {
            selectTreeId = "";
            $('#simpleQueryParam').val("");
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.selectNode("");
            zTree.cancelSelectedNode();
            myTable.requestData();
        },
        // 显示图片
        showPhoto: function (fileName) {
            $('#photoDiv').modal('show');
            var d = new Date();
            var radomNum = d.getTime();
            $('#photoImg').attr('src', fileName + "?t=" + radomNum);
        },
        jobType: function () {
            var typeQuery = $("#typeQuery").val();
            var url = "/clbs/m/basicinfo/enterprise/professionals/listType";
            var data = {"professionalstype": typeQuery}
            json_ajax("POST", url, "json", false, data, professionalsList.findList);
        },
        findList: function (data) {
            var result = data.records;
            var dataListArray = [];
            var permission = $("#permission").val();
            for (var i = 0; i < result.length; i++) {
                if (permission == "true") {

                    if (result[i].professionalstype == '驾驶员(IC卡)') {
                        var list = [
                            i + 1,
                            '<input  type="checkbox" name="subChkTwo"  value="' + result[i].id + '" disabled />',
                            '<button disabled  type="button" class="editBtn editBtn-info disableClick"><i class="fa fa-pencil"></i>修改</button>&ensp;<button type="button"  disabled class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>',
                            result[i].professionalstype,
                            result[i].description
                        ];
                    } else {
                        var list = [
                            i + 1,
                            '<input  type="checkbox" name="subChkTwo"  value="' + result[i].id + '" />',
                            '<button onclick="professionalsList.findTypeById(\'' + result[i].id + '\')" data-target="#updateType" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;<button type="button"  onclick="professionalsList.deleteType(\'' + result[i].id + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>',
                            result[i].professionalstype,
                            result[i].description
                        ];
                    }

                } else {
                    var list = [
                        i + 1,
                        result[i].professionalstype,
                        result[i].description
                    ];
                }
                dataListArray.push(list);
            }
            reloadData(dataListArray);
        },

        findTypeById: function (id) {
            professionalsList.closeUpdateClean();//修改窗口弹出时清空
            pId = id;
            var data = {"id": id};
            var url = "/clbs/m/basicinfo/enterprise/professionals/findTypeById";
            json_ajax("POST", url, "json", false, data, professionalsList.findCallback);
        },
        findCallback: function (data) {
            if (data.success == true) {
                $("#jobType").val(data.obj.operation.professionalstype);
                $("#jobDescription").val(data.obj.operation.description);
                startpostType = $("#jobType").val();
                expliant = $("#jobDescription").val();
            } else {
                layer.msg(data.msg, {move: false});
            }
        },
        deleteType: function (id) {
            layer.confirm(publicDelete, {
                title: '操作确认',
                icon: 3, // 问号图标
                btn: ['确定', '取消'] //按钮
            }, function () {
                var url = "/clbs/m/basicinfo/enterprise/professionals/deleteTypeMore";
                var data = {"ids": id};
                json_ajax("POST", url, "json", false, data, professionalsList.deleteTypeCallback);

            });
        },
        deleteTypeCallback: function (data) {
            if (data.success) {
                layer.closeAll('dialog');
                professionalsList.jobType();
                professionalsList.init();
            } else {
                layer.msg(data.msg);
            }
        },
        deleteTypeMore: function () {
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
            var url = "/clbs/m/basicinfo/enterprise/professionals/deleteTypeMore";
            var data = {"ids": ids};
            layer.confirm(publicDelete, {
                title: '操作确认',
                icon: 3, // 问号图标
                btn: ['确定', '取消'] //按钮
            }, function () {
                json_ajax("POST", url, "json", false, data, professionalsList.deleteTypeMoreCallback);
            });
        },
        deleteTypeMoreCallback: function (data) {
            if (data.success) {
                layer.closeAll('dialog');
                professionalsList.jobType();
            } else {
                layer.msg(data.msg);
            }
        },
        searchType: function () {
            professionalsList.jobType();
        },
        findType: function (event) {
            if (event.keyCode == 13) {
                professionalsList.jobType();
            }
        },
        searchPeople: function () {
            myTable.requestData();
        },
        setFormToken: function (data) {
            professionalsList.closeUpdateClean();
            if (data.success) {
                $('#formToken').val(data.msg);
            }
        },
        clearVal: function () {
            professionalsList.closeClean();
            var url = '/clbs/m/basicinfo/enterprise/professionals/generateFormToken';
            json_ajax('post', url, 'json', true, '', professionalsList.setFormToken);

        },
        checkAll: function (e) {
            $("input[name='subChk']").prop("checked", e.checked);
        },
        checkAllTwo: function (e) {
            $("input[name='subChkTwo']:not('input[disabled]')").prop("checked", e.checked);
        },
        addId: function () {
            $("#addId").attr("href", "/clbs/m/basicinfo/enterprise/professionals/add?uuid=" + selectTreeId + "");
        },
        add: function () {
            if (professionalsList.validates()) {
                addHashCode1($("#addLimit"));
                $("#addLimit").ajaxSubmit(function (data) {
                    if (data != null && typeof(data) == "object" &&
                        Object.prototype.toString.call(data).toLowerCase() == "[object object]" &&
                        !data.length) {//判断data是字符串还是json对象,如果是json对象
                        if (data.success == true) {
                            $("#addType").modal("hide");//关闭窗口
                            layer.msg(publicAddSuccess, {move: false});
                            professionalsList.closeClean();//清空文本框
                            $("#typeQuery").val("");
                            professionalsList.jobType();
                            $("#professionalstype").removeData("previousValue");//清空validates的验证缓存
                            $("#addDescription").removeData("previousValue");//清空validates的验证缓存
                        } else {
                            layer.msg(data.msg, {move: false});
                        }
                    } else {//如果data不是json对象
                        var result = $.parseJSON(data);//转成json对象
                        if (result.success == true) {
                            $("#addType").modal("hide");//关闭窗口
                            layer.msg(publicAddSuccess, {move: false});
                            professionalsList.closeClean();//清空文本框
                            $("#typeQuery").val("");
                            professionalsList.jobType();
                            $("#professionalstype").removeData("previousValue");//清空validates的验证缓存
                            $("#addDescription").removeData("previousValue");//清空validates的验证缓存
                        } else {
                            layer.msg(result.msg, {move: false});
                        }
                    }
                });
            }
        },
        updateJobType: function () {
            if (professionalsList.updateValidates()) {
                var jobType = $("#jobType").val();// 岗位类型
                var jobDescription = $("#jobDescription").val();//描述
                var resubmitToken = jobType + jobDescription;
                resubmitToken = resubmitToken.split("").reduce(function (a, b) { a = ((a << 5) - a) + b.charCodeAt(0); return a & a }, 0);

                var data = {"id": pId, "jobType": jobType, "jobDescription": jobDescription, resubmitToken};
                var url = "/clbs/m/basicinfo/enterprise/professionals/editJobType";
                json_ajax("POST", url, "json", true, data, professionalsList.updateCallback);
            }
        },
        updateCallback: function (data) {
            if (data.success == true) {
                $("#updateType").modal('hide');
                layer.msg(publicEditSuccess);
                professionalsList.jobType();
                professionalsList.closeUpdateClean();
                $("#jobType").removeData("previousValue");//清空validates的验证缓存
                $("#jobDescription").removeData("previousValue");//清空validates的验证缓存
            } else {
                layer.msg(data.msg, {move: false});
            }
        },
        validates: function () {//增加岗位类型时的数据验证
            $("#professionalstype").removeData("previousValue");//清空validates的验证缓存
            $("#addDescription").removeData("previousValue");//清空validates的验证缓存
            return $("#addLimit").validate({
                rules: {
                    professionalstype: {
                        required: true,
                        stringCheck: true,
                        maxlength: 20,
                        minlength: 2,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/m/basicinfo/enterprise/professionals/comparisonType",
                            data: {
                                type: function () {
                                    return $("#professionalstype").val();
                                }
                            },
                        }
                    },
                    addDescription: {
                        maxlength: 30,
                        equalToDefault: true
                    }
                },
                messages: {
                    professionalstype: {
                        required: professionalsJobTypeNull,
                        // stringCheck: professionalsJobTypeError,
                        stringCheck: '只能包含中文、英文、数字、下划线和短杠', //wjk
                        maxlength: publicSize20Length,
                        minlength: publicMinSize2Length,
                        remote: professionalsJobTypeExists
                    },
                    addDescription: {
                        maxlength: publicSize30Length
                    }
                }
            }).form();
        },
        updateValidates: function () {//修改岗位类型时的数据验证
            $("#jobType").removeData("previousValue");//清空validates的验证缓存
            $("#jobDescription").removeData("previousValue");//清空validates的验证缓存
            var updateType = $("#jobType").val();// 岗位类型
            var explains = $("#jobDescription").val();// 描述
            if (updateType == startpostType && explains == expliant) {
                $("#updateType").modal('hide');
            } else if (updateType == startpostType && explains != expliant) { //用户修改的时描述
                return $("#updateLimit").validate({
                    rules: {
                        jobType: {
                            required: true,
                            maxlength: 20,
                            minlength: 2
                        },
                        jobDescription: {
                            maxlength: 30,
                            equalToDefault: true
                        }
                    },
                    messages: {
                        jobType: {
                            required: professionalsJobTypeNull,
                            maxlength: publicSize20Length,
                            minlength: publicMinSize2Length
                        },
                        jobDescription: {
                            maxlength: publicSize30Length,
                            // equalToDefault: '描述类型不能与驾驶员(IC卡)描述一致'
                        }
                    }
                }).form();
            } else {
                return $("#updateLimit").validate({
                    rules: {
                        jobType: {
                            required: true,
                            stringCheck: true,
                            maxlength: 20,
                            minlength: 2,
                            remote: {
                                type: "post",
                                async: false,
                                url: "/clbs/m/basicinfo/enterprise/professionals/findPostTypeCompare",
                                dataType: "json",
                                data: {
                                    type: function () {
                                        return $("#jobType").val();
                                    },
                                    startpostType: function () {
                                        return startpostType;
                                    }
                                },
                                dataFilter: function (data) {
                                    var resultData = $.parseJSON(data);
                                    if (resultData.success == true) {
                                        return true;
                                    } else {
                                        return false;
                                    }
                                }
                            }
                        },
                        jobDescription: {
                            maxlength: 30,
                        }
                    },
                    messages: {
                        jobType: {
                            required: professionalsJobTypeNull,
                            stringCheck: professionalsJobTypeError,
                            maxlength: publicSize20Length,
                            minlength: publicMinSize2Length,
                            remote: professionalsJobTypeExists,
                            stringCheck: '只能包含中文、英文、数字、下划线和短杠' //wjk
                        },
                        jobDescription: {
                            maxlength: publicSize30Length
                        }
                    }
                }).form();
            }

        },
        closeClean: function () { //关闭新增窗口
            $("#professionalstype").val("");
            $("#addDescription").val("");
            $("#professionalstype-error").hide();//隐藏上次新增时未清除的validate样式
            $("#addDescription-error").hide();
        },
        closeUpdateClean: function () { //清除validate样式
            $("#jobType-error").hide();
            $("#jobDescription-error").hide();
        },
        clearPreviousValue: function () {
            if ($(".remote").data("previousValue")) {
                $(".remote").data("previousValue").old = null;
            }
        },
        // 展示从业人员身份证、从业资格证和驾驶证
        showPractitionersPhoto: function (identityCardPhoto, qualificationCertificatePhoto, driverLicensePhoto) {
            // var newData = JSON.parse(data);
            // var identityCardPhoto = newData.identityCardPhoto;
            // var qualificationCertificatePhoto = newData.qualificationCertificatePhoto;
            // var driverLicensePhoto = newData.driverLicensePhoto;

            var index = 3;
            if (identityCardPhoto != '' && identityCardPhoto != null && identityCardPhoto != undefined && identityCardPhoto != 'undefined') {
                $('#identityCardImageUrl').attr('src', identityCardPhoto);
            } else {
                $('#identityCardImage').hide();
                $('#identityCardText').hide();
                index -= 1;
            }

            if (qualificationCertificatePhoto != '' && qualificationCertificatePhoto != null && qualificationCertificatePhoto != undefined && qualificationCertificatePhoto != 'undefined') {
                $('#qualificationCertificateImageUrl').attr('src', qualificationCertificatePhoto);
            } else {
                $('#qualificationCertificateImage').hide();
                $('#qualificationCertificateText').hide();
                index -= 1;
            }

            if (driverLicensePhoto != '' && driverLicensePhoto != null && driverLicensePhoto != undefined && driverLicensePhoto != 'undefined') {
                $('#driverLicenseImageUrl').attr('src', driverLicensePhoto);
            } else {
                $('#driverLicenseImage').hide();
                $('#driverLicenseText').hide();
                index -= 1;
            }
            $('#identityCardImage, #qualificationCertificateImage, #driverLicenseImage').attr('class', 'col-md-' + 12 / index);
            $('#practitionersPhotoModal').modal('show');
        }
    }

    $(function () {
        getTable("dataTables");
        professionalsList.professionalsTree();
        professionalsList.jobType();
        professionalsList.init();
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'search_condition') {
                search_ztree('treeDemo', id, 'group');
            }
            ;
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
        //IE9 end
        //全选
        $subChk.on("click", professionalsList.subChk);
        $subChkTwo.on("click", professionalsList.subChkTwo);
        $("#del_model").on("click", professionalsList.deleteMuth);
        $("#del_modelTwo").on("click", professionalsList.deleteTypeMore);
        $("#search_buttonTwo").on("click", professionalsList.searchType);
        $("#add_Type").on("click", professionalsList.clearVal());
        $("#search_button").on("click", professionalsList.searchPeople);
        $('#refreshTable').on("click", professionalsList.queryAll);
        $('#goOverspeedSettings').on("click", professionalsList.add);
        $('#updatePostType').on("click", professionalsList.updateJobType);
        // 组织架构模糊搜索 
        $("#search_condition").on("input oninput", function () {
            search_ztree('treeDemo', 'search_condition', 'group');
        });
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'search_condition') {
                search_ztree('treeDemo', id, 'group');
            }
            ;
        });
        $("#addId").on("click", professionalsList.addId);
        $("#updateButtonClose").on("click", professionalsList.closeUpdateClean);
        $("#addClose").on("click", professionalsList.closeClean);
        $("#professionalstype,#addDescription,#jobType,#jobDescription").on("change", professionalsList.clearPreviousValue);
    })
})($, window)