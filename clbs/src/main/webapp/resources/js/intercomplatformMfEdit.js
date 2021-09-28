(function (window, $) {
    //单选
    var subChk = $("input[name='subChk']");
    var menu_text = "";
    var table = $("#dataTable tr th:gt(1)");
    editForwardManagement = {
        init: function () {
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            ;
            $("#Ul-menu-text").html(menu_text);
            //车辆树
            var setting = {
                async: {
                    url: "/clbs/v/oilmgt/vehicelTree",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple"},
                    dataFilter: editForwardManagement.ajaxDataFilter
                },
                check: {
                    enable: true,
                    chkStyle: "checkbox",
                    chkboxType: {
                        "Y": "s",
                        "N": "s"
                    },
                    radioType: "all"
                },
                view: {
                    dblClickExpand: false,
                    nameIsHTML: true,
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    onCheck: editForwardManagement.onCheckVehicle
                }
            };
            $.fn.zTree.init($("#treeDemo_edit"), setting, null);
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
                        var idStr = row.id;
                        var editUrlPath = myTable.editUrl + idStr; //修改地址
                        var result = '';
                        //修改按钮
                        result += '<button data-target="#commonSmWin" href="' + editUrlPath + '" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                        //删除按钮
                        result += '<button type="button" onclick="editForwardManagement.deleteRole(\'' + row.id + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
                        return result;
                    }
                }, {
                    "data": "brand",
                    "class": "text-center"
                }, {
                    "data": "intercomPlatformName",
                    "class": "text-center"
                }, {
                    "data": "intercomPlatformIP",
                    "class": "text-center",
                }, {
                    "data": "intercomPlatformPort",
                    "class": "text-center",
                }
            ];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/m/intercomplatform/deviceconfig/list',
                editUrl: '/clbs/m/intercomplatform/deviceconfig/edit_',
                deleteUrl: '/clbs/m/intercomplatform/deviceconfig/delete_',
                deletemoreUrl: '/clbs/m/intercomplatform/deviceconfig/deletemore',
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
            var parameterList = '{"id" : ""}';
            var platformListUrl = "/clbs/m/intercomplatform/deviceconfig/platformListUrl";
            json_ajax("POST", platformListUrl, "json", true, parameterList, editForwardManagement.getPlatformList);
        },
        addConfigCheck: function () {
            if ($("#intercomPlatformId_edit").val() == "" || $("#platformName").val() == "") {
                alert("请选择正确的平台");
                return;
            }
            if ($("#vehicleIds_edit").val() == "") {
                alert("请选择车辆");
                return;
            }
            var param = {intercomPlatformId: $("#intercomPlatformId_edit").val(), ids: $("#vehicleIds_edit").val()}

            var platformListUrl = "/clbs/m/intercomplatform/deviceconfig/addConfig";
            json_ajax("POST", platformListUrl, "json", true, param, editForwardManagement.addConfigCallBack);
        },
        addConfigCallBack: function (data) {

        },
        addressOnChange: function () {
            $("#intercomPlatformId_edit").attr("value", "");
        },
        // 删除角色
        deleteRole: function (id) {
            myTable.deleteItem(id);
        },
        getPlatformList: function (data) {
            var datas = data.obj.platformList;
            var dataList = {value: []}, i = datas.length;
            while (i--) {
                dataList.value.push({
                    name: datas[i].platformName,
                    id: datas[i].id
                });
            }
            $("#platformName").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                data: dataList,
                effectiveFields: ["name"]
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                $("#intercomPlatformId_edit").attr("value", keyword.id);
            }).on('onUnsetSelectValue', function () {
            });
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            $("#charSelect").val("").attr("data-id", "").bsSuggest("destroy");
            var zTree = $.fn.zTree.getZTreeObj("treeDemo_edit"),
                nodes = zTree.getCheckedNodes(true)
            var vids = "";
            for (var i = 0; i < nodes.length; i++) {
                if (nodes[i].type == "vehicle") {
                    vids += nodes[i].id + ',';
                }
            }
            $("#vehicleIds_edit").val(vids);
        },
        unique: function (arr) {
            var result = [], hash = {};
            for (var i = 0, elem; (elem = arr[i]) != null; i++) {
                if (!hash[elem]) {
                    result.push(elem);
                    hash[elem] = true;
                }
            }
            return result;
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            if (responseData) {
                var veh = [];
                var vid = [];
                var gourps = [];
                for (var i = 0; i < responseData.length; i++) {
                    responseData[i].checked = true
                    if (responseData[i].type == "group") {
                        gourps.push(responseData[i].name)
                    }
                    var gName = editForwardManagement.unique(gourps)
                    $("#groupSelect_edit").val(gName[0]);
                    if (responseData[i].type == "vehicle") {
                        veh.push(responseData[i].name)
                        vid.push(responseData[i].id)
                    }
                }
                var vehName = editForwardManagement.unique(veh);
                var vehId = editForwardManagement.unique(vid);
                $("#charSelect").empty();
                var deviceDataList = {value: []};
                for (var j = 0; j < vehName.length; j++) {
                    deviceDataList.value.push({
                        name: vehName[j],
                        id: vehId[j]
                    });
                }
                ;
                $("#charSelect").bsSuggest({
                    indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                    indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                    data: deviceDataList,
                    effectiveFields: ["name"]
                }).on('onDataRequestSuccess', function (e, result) {
                }).on("click", function () {
                }).on('onSetSelectValue', function (e, keyword, data) {
                }).on('onUnsetSelectValue', function () {
                });
                $("#charSelect").val(vehName[0]).attr("data-id", vehId[0]);
                return responseData;
            }
        },
        showMenu: function (e) {
            if ($("#menuContent_edit").is(":hidden")) {
                var inpwidth = $("#groupSelect_edit").outerWidth();
                $("#menuContent_edit").css("width", inpwidth + "px");
                $(window).resize(function () {
                    var inpwidth = $("#groupSelect_edit").outerWidth();
                    $("#menuContent_edit").css("width", inpwidth + "px");
                })
                $("#menuContent_edit").slideDown("fast");
            } else {
                $("#menuContent_edit").is(":hidden");
            }
            $("body").bind("mousedown", editForwardManagement.onBodyDown);
        },
        hideMenu: function () {
            $("#menuContent_edit").fadeOut("fast");
            $("body").unbind("mousedown", editForwardManagement.onBodyDown);
        },
        onBodyDown: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "menuContent_edit" || $(
                event.target).parents("#menuContent_edit").length > 0)) {
                editForwardManagement.hideMenu();
            }
        },
        //批量删除
        delModelClick: function () {
            //判断是否至少选择一项
            var chechedNum = $("input[name='subChk']:checked").length;
            if (chechedNum == 0) {
                layer.msg(selectItem, {move: false});
                return;
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
            myTable.filter();
        },
        //全选
        cleckAll: function () {
            $("input[name='subChk']").prop("checked", this.checked);
        },
        //单选
        subChkClick: function () {
            $("#checkAll").prop("checked", subChk.length == subChk.filter(":checked").length ? true : false);
        },
    }
    $(function () {
        $('input').inputClear();
        editForwardManagement.init();
        $("#checkAll").bind("click", editForwardManagement.cleckAll);
        subChk.bind("click", editForwardManagement.subChkClick);
        $("#groupSelect_editSpan_edit,#groupSelect_edit").bind("click", editForwardManagement.showMenu);
        $("#del_model").on("click", editForwardManagement.delModelClick);
        $("#refreshTable").on("click", editForwardManagement.refreshTable);
    })
})(window, $)