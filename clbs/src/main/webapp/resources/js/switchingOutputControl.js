(function (window, $) {
    //显示隐藏列
    var menu_text = "";
    var table = $("#dataTable tr th:gt(0)");
    //单选
    var subChk = $("input[name='subChk']");
    var selectGroupId = '';
    var selectAssignmentId = '';

    var dtext = "";
    var dn = new Array("交通部JT/T808-2013", "交通部JT/T808-2019");
    var dv = new Array("1", "11");
    var protocol = 1;
    switchingOutputControl = {
        //初始化
        init: function () {
            //数据表格列筛选
            menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(1) + "\" disabled />" + table[0].innerHTML + "</label></li>";
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            ;
            $("#Ul-menu-text").html(menu_text);
            //生成定制显示列
            for (var i = 0; i < dn.length; i++) {
                dtext += "<label class='radio-inline' style='margin-left:10px;'><input name=\"deviceCheck\" value=\"" + dv[i] + "\" type=\"radio\" class=\"device\" />" + dn[i] + "</label>";
            }
            ;
            $("#Ul-menu-text-v").html(dtext);
            $("input[value='1']").prop("checked", true);
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
                    "class": "text-center", //最后一列，操作按钮
                    render: function (data, type, row, meta) {
                        var result = '<button type="button" class="editBtn editBtn-info" onclick="switchingOutputControl.controlSetting(\'' + row.moId + '\',\'IO控制设置\')">I/O控制</button>&ensp;' +
                            '<button type="button" class="editBtn editBtn-info" onclick="switchingOutputControl.controlSetting(\'' + row.moId + '\',\'断油电设置\')">断油电</button>&ensp;' +
                            '<button type="button" class="editBtn editBtn-info" onclick="switchingOutputControl.controlSetting(\'' + row.moId + '\',\'模拟量控制设置\')">模拟量控制</button>&ensp;'
                            // + '<button type="button" class="deleteButton editBtn btn-default" onclick="switchingOutputControl.closeControl(\'' + row.moId + '\')">关闭</button>';

                        return result;
                    }
                }, {
                    "data": "moName",
                    "class": "text-center"
                }, {
                    "data": "downTimeStr",
                    "class": "text-center"
                },{
                    "data": "statusStr",
                    "class": "text-center"
                },{
                    "data": "orgName",
                    "class": "text-center"
                }, {
                    "data": "vehicleType",
                    "class": "text-center"
                }];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
                d.groupId = selectGroupId;
                d.assignmentId = selectAssignmentId;
                d.protocol = $("input[name='deviceCheck']:checked").val();
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/m/switching/outputControl/pageList',
                bindUrl: '/clbs/m/switching/signal/bind_',
                editUrl: '/clbs/m/switching/signal/edit_',
                deleteUrl: '/clbs/m/switching/signal/delete_',
                deletemoreUrl: '/clbs/m/switching/signal/deletemore',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            //表格初始化
            myTable.init();
            //组织树
            var treeSetting = {
                async: {
                    url: "/clbs/m/basicinfo/enterprise/assignment/assignmentTree",
                    tyoe: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "isOrg": "1"
                    },
                    dataFilter: switchingOutputControl.ajaxDataFilter
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
                    onClick: switchingOutputControl.zTreeOnClick
                }
            };
            //组织树初始化
            $.fn.zTree.init($("#treeDemo"), treeSetting, null);
        },
        /**
         * 打开操作设置弹窗
         * @param monitorId:监控对象id
         * @param title
         * */
        controlSetting: function (monitorId, title) {
            if (title === '模拟量控制设置') {
                $('#analogInfo input').prop('disabled', false);
                $('#kzzt input').prop('disabled', true);
                $('#analogInfo').show();
                $('#kzzt').hide();
            } else {
                $('#analogInfo input').prop('disabled', true);
                $('#kzzt input').prop('disabled', false);
                $('#analogInfo').hide();
                $('#kzzt').show();
            }
            $('#peripheralId').val('144');
            $('#outletSet').val('0');
            $('#settingModal input').val('');
            $('#kzzt1').val('0');
            $('#kzzt2').val('1');
            $('#kzzt1').prop('checked', true);
            $('#settingModalTitle').html(title);
            $('#vehicleId').val(monitorId);
            $('#settingModal label.error').hide();
            $('#settingModal').modal('show');
        },
        /**
         * 关闭输出控制
         * @param monitorId:监控对象id
         * */
        closeControl: function (monitorId) {
            var protocolType = $("input[name='deviceCheck']:checked").val();
            layer.confirm("确认关闭IO输出?", {btn: ["确定", "取消"], icon: 3, title: "操作确认"}, function () {
                json_ajax("POST", "/clbs/m/switching/outputControl/close", "json", false, {
                    vehicleId: monitorId,
                    protocolType: protocolType
                }, function (data) {
                    layer.closeAll();
                    if (data.success) {
                        layer.msg('关闭成功');
                    } else if (data.msg) {
                        layer.msg(data.msg);
                    }
                })
            });
        },
        // 提交修改后的设置
        settingSubmit: function () {
            if (switchingOutputControl.settingValidates()) {
                var currentModal = $('#settingModalTitle').text();
                var param = {};
                var paramArr = $('#settingForm').serializeArray();
                for (var i = 0; i < paramArr.length; i++) {
                    param[paramArr[i].name] = paramArr[i].value;
                }
                if (currentModal === 'IO控制设置') {
                    param.controlSubtype = 1;
                } else if (currentModal === '断油电设置') {
                    param.controlSubtype = 2;
                } else if (currentModal === '模拟量控制设置') {
                    param.controlSubtype = 3;
                }
                var protocolType = $("input[name='deviceCheck']:checked").val();
                param.protocolType = protocolType;

                let value = '';
                for(var key in param) {
                    value += param[key];
                }
                value = value.split("").reduce(function (a, b) { a = ((a << 5) - a) + b.charCodeAt(0); return a & a }, 0);
                param.resubmitToken = value;
                $('#settingModal').modal('hide');
                if (currentModal === '模拟量控制设置') {
                    var url = '/clbs/m/switching/outputControl/send8500';
                    json_ajax("POST", url, "json", true, param, function (data) {
                        if (data.success) {
                            // $('#settingModal').modal('hide');
                            myTable.refresh();
                        } else if (data.msg) {
                            layer.msg(data.msg);
                        }
                    })
                } else {
                    var url2 = '/clbs/m/switching/outputControl/send8500';
                    webSocket.subscribe(
                        headers,
                        '/user/topic/ioMonitoring/send8500',
                        switchingOutputControl.IOkzCallback,
                        '/app/ioMonitoring/send8500',
                        // JSON.stringify(param),
                        {data: param},
                    );
                }
            }
        },
        IOkzCallback: function (msg) {
            if (msg) {
                // $('#settingModal').modal('hide');
                myTable.refresh();
            }
        },
        settingValidates: function () {
            return $('#settingForm').validate({
                rules: {
                    controlTime: {
                        integerRange: [1, 65534]
                    },
                    analogOutputRatio: {
                        required: true,
                        decimalOne: true,
                        decimalRange: [0, 100]
                    }
                },
                messages: {
                    controlTime: {
                        integerRange: '请输入1~65534间的正整数'
                    },
                    analogOutputRatio: {
                        required: '请输入模拟量输出比例',
                        decimalOne: '请输入0~100之间的数字，最多保留一位小数',
                        decimalRange: '请输入0~100之间的数字，最多保留一位小数'
                    }
                }
            }).form();
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
        //组织架构单击
        zTreeOnClick: function (event, treeId, treeNode) {
            if (treeNode.type == "assignment") {
                selectAssignmentId = treeNode.id;
                selectGroupId = '';
            } else {
                selectGroupId = treeNode.uuid;
                selectAssignmentId = '';
            }
            myTable.requestData();
        },
        //全选
        checkAllClick: function () {
            $("input[name='subChk']").prop("checked", this.checked);
        },
        subChkClick: function () {
            $("#checkAll").prop("checked", subChk.length == subChk.filter(":checked").length ? true : false);
        },
        updataFenceData: function (msg) {
            if (msg != null) {
                var result = $.parseJSON(msg.body);
                if (result != null) {
                    myTable.refresh();
                }
            }
        },
        //批量解除
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
            myTable.relieveItems({'deltems': checkedList.toString()});
        },
        //刷新列表
        refreshTable: function () {
            selectGroupId = '';
            selectAssignmentId = '';
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.selectNode("");
            zTree.cancelSelectedNode();
            $("#simpleQueryParam").val("");
            myTable.requestData();
        },
    }
    $(function () {
        switchingOutputControl.init();
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'search_condition') {
                search_ztree('treeDemo', id, 'assignment');
            }
            ;
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
        //全选
        $("#checkAll").bind("click", switchingOutputControl.checkAllClick);
        subChk.bind("click", switchingOutputControl.subChkClick);
        //批量删除
        $("#restore_model").bind("click", switchingOutputControl.delModelClick);
        //刷新
        $("#refreshTable").on("click", switchingOutputControl.refreshTable);

        //改变协议勾选框
        $(".device").change(function () {
            //取消全选
            $("#checkAll").prop('checked', false);
            //刷新表格
            myTable.requestData();
        });

        $('#doSubmitSetting').on('click', switchingOutputControl.settingSubmit);
    })
})(window, $);