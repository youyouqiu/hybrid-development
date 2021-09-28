(function (window, $) {
    var params = [];
    //显示隐藏列
    var menu_text = "";
    var table = $("#dataTable tr th:gt(1)");
    var headers = {"UserName": ""};
    var selectGroupId = '';
    var selectAssignmentId = '';
    //单选
    var subChk = $("input[name='subChk']");
    var dtext = "";
    var dn = new Array("交通部JT/T808-2013","交通部JT/T808-2019");
    var dv = new Array("1","11");

    var treeData = [];//传感器组织树数据
    var sensorId = '';//传感器id
    var sensorName = '';//传感器类型
    var hiddenNodes = '';
    var firstNode = true;

    remoteUpgrade = {
        init: function () {
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" />" + table[i].innerHTML + "</label></li>"
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
            //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
            window.onbeforeunload = function () {
                var cancelStrS = {
                    "desc": {
                        "MsgId": 40964,
                        "UserName": $("#userName").text()
                    },
                    "data": params
                };
                webSocket.unsubscribealarm(headers, "/app/vehicle/unsubscribestatus", cancelStrS);
            }
            remoteUpgrade.tableInit();
            remoteUpgrade.getTreeData();
        },
        tableInit: function () {
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
                        // if (row.sensorId != null && row.sensorId != "") {
                        var obj = {};
                        obj.sensorId = row.sensorId;
                        obj.deviceId = row.deviceId;
                        obj.vehicleId = row.vehicleId;
                        var jsonStr = JSON.stringify(obj);
                        result += "<input  type='checkbox' name='subChk'  value='" + jsonStr + "' />";
                        // }
                        return result;
                    }
                },
                {
                    "data": null,
                    "class": "text-center", //最后一列，操作按钮
                    render: function (data, type, row, meta) {
                        var readUrlPath = '/clbs/v/sensorConfig/sensorUpgrade/basicInfo_{id}_' + row.sensorId + '.gsp'; //修改地址
                        var bindUrlPre = '/clbs/v/sensorConfig/sensorUpgrade/getSendRemoteUpgradePage_' + row.vehicleId + '_' + row.sensorId;
                        var result = '';
                        var paramId = "";
                        if (row.settingParamId != null && row.settingParamId != "") {
                            paramId = row.paramId;
                        }
                        var sensorId = row.sensorId;
                        if (sensorId == "0x80" || sensorId == "0x81" || sensorId == "0x70"
                            || sensorId == "0x71" || sensorId == "0x45" || sensorId == "0x46"
                            || sensorId == "0x41" || sensorId == "0x42" || sensorId == "0x43" || sensorId == "0x44") {
                            result += '<button href="' + bindUrlPre.replace("{id}", row.vehicleId) + '"  data-toggle="modal" data-target="#commonWin"  type="button" class="editBtn editBtn-info"><i class="fa fa-set"></i>升级设置</button>&ensp;';
                            result += '<button href="' + readUrlPath.replace("{id}", row.vehicleId) + '"  data-toggle="modal" data-target="#commonWin"  type="button" class="editBtn editBtn-info"><i class="fa fa-set"></i>读取基本信息</button>&ensp;';
                        } else {
                            result += '<button href="' + bindUrlPre.replace("{id}", row.vehicleId) + '"  data-toggle="modal" data-target="#commonWin"  type="button" class="editBtn editBtn-info"><i class="fa fa-set"></i>升级设置</button>&ensp;';
                        }
                        return result;
                    }
                },
                {
                    "data": "sensorUpgradeDateStr",
                    "class": "text-center",
                },
                {
                    "data": "sensorUpgradeStatus",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == "0") {
                            return '进行中';
                        } else if (data == "1") {
                            return '升级成功';
                        } else if (data == "2") {
                            return "<span style='color: red'>升级失败</span>";
                        } else if (data == "3") {
                            return "<span style='color: red'>终端存储区擦除失败</span>";
                        } else if (data == "4") {
                            return "<span style='color: red'>数据校验失败</span>";
                        } else if (data == "5") {
                            return "<span style='color: red'>下发升级指令失败</span>";
                        } else if (data == "6") {
                            return "<span style='color: red'>外设升级超时</span>";
                        } else {
                            return "";
                        }
                    }
                },
                {
                    "data": "sensorId",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return sensorName;
                    }
                },
                {
                    "data": "brand",
                    "class": "text-center",
                }, {
                    "data": "groupName",
                    "class": "text-center"
                }, {
                    "data": "assagnName",
                    "class": "text-center"
                },
            ];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                var simpleQueryParam = $('#simpleQueryParam').val();
                var type = $("#searchType").val();
                d.paramSign = type;
                d.fuzzyParam = simpleQueryParam;
                d.sensorId = sensorId;
                d.deviceType = $("input[name='deviceCheck']:checked").val();
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/v/sensorConfig/sensorUpgrade/getBindInfo',
                editUrl: '/clbs/v/loadmgt/loadvehiclesetting/edit_',
                enableUrl: '/clbs/c/user/enable_',
                disableUrl: '/clbs/c/user/disable_',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                lengthMenu: [10, 20, 50],
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            //表格初始化
            myTable.init();
        },
        treeInit: function () {
            var treeSetting = {
                view: {
                    showIcon: false,
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
                    onClick: remoteUpgrade.zTreeOnClick,
                    onNodeCreated: remoteUpgrade.zTreeOnNodeCreated,
                }
            };

            $.fn.zTree.init($("#treeDemo"), treeSetting, treeData);
        },
        zTreeOnNodeCreated: function (event, treeId, treeNode) {
            if (firstNode) {
                firstNode = false;
                var id = treeNode.id;
                sensorId = id;
                sensorName = treeNode.name;
                $("#sensorName").attr('value',treeNode.name);
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                treeObj.selectNode(treeNode, true, true);
                myTable.requestData();
            } else if (sensorId != '' && treeNode.id == sensorId) {
                sensorName = treeNode.name;
                $("#sensorName").attr('value',treeNode.name);
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                treeObj.selectNode(treeNode, true, true);
                myTable.requestData()
            }
        },
        // 树结构模糊搜索
        searchTree: function (txtObj) {
            if (txtObj.value.length > 0) {
                var zTreeObj = $.fn.zTree.getZTreeObj("treeDemo");
                //显示上次搜索后背隐藏的结点
                if (hiddenNodes != '')
                    zTreeObj.showNodes(hiddenNodes);

                //查找不符合条件的叶子节点
                function filterFunc(node) {
                    var _keywords = txtObj.value;
                    if (node.isParent) {
                        return false
                    }
                    if (node.name.indexOf(_keywords.toUpperCase()) != -1) return false;
                    return true;
                };

                //获取不符合条件的叶子结点
                hiddenNodes = zTreeObj.getNodesByFilter(filterFunc);

                //隐藏不符合条件的叶子结点
                zTreeObj.hideNodes(hiddenNodes);
            } else {
                firstNode = true;
                remoteUpgrade.treeInit();
            }
        },
        //点击节点
        zTreeOnClick: function (event, treeId, treeNode) {
            if (treeNode.isParent) {
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                treeObj.cancelSelectedNode(treeNode);
                return;
            }
            sensorId = treeNode.id;
            sensorName = treeNode.name;
            $("#sensorName").attr('value',treeNode.name);
            $('#simpleQueryParam').val('').attr('placeholder', '请输入监控对象关键字');
            $('#searchType').val('0');
            myTable.requestData();
        },
        getTreeData: function () {
            var url = '/clbs/v/sensorConfig/sensorUpgrade/getSensorList';
            json_ajax("GET", url, "json", true, null, function (data) {
                if (data.success) {
                    var dataArr = data.obj.sensorData;
                    treeData = [];
                    var nameArr = [];
                    var len = dataArr.length;
                    for (var i = 0; i < len; i++) {
                        var name = dataArr[i].modelName;
                        if (nameArr.indexOf(name) == -1) {
                            var obj = {
                                "name": name,
                                "isParent": true,
                                "open": true,
                                "children": [{
                                    "id": dataArr[i].sensorId,
                                    "name": dataArr[i].sensorName
                                }]
                            };
                            nameArr.push(name);
                            treeData.push(obj);
                        } else {
                            for (var j = 0; j < treeData.length; j++) {
                                if (name == treeData[j].name) {
                                    treeData[j].children.push({
                                        "id": dataArr[i].sensorId,
                                        "name": dataArr[i].sensorName
                                    })
                                }
                            }
                        }
                    }
                    remoteUpgrade.treeInit();
                }
            });
        },
        updataFenceData: function (msg) {
            if (msg != null) {
                var result = $.parseJSON(msg.body);
                if (result != null) {
                    myTable.refresh();
                }
            }
        },
        //全选
        cleckAll: function () {
            $("input[name='subChk']").prop("checked", this.checked);
        },
        //单选
        subChkClick: function () {
            $("#checkAll").prop("checked", subChk.length == subChk.filter(":checked").length ? true : false);
        },
        // 查询全部
        queryAll: function () {
            selectGroupId = "";
            selectAssignmentId = "";
            $('#simpleQueryParam').val("");
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.selectNode("");
            zTree.cancelSelectedNode();
            myTable.requestData();
        },
        // 组织架构模糊搜索
        searchCondition: function () {
            search_ztree('treeDemo', 'search_condition', 'assignmen');
        },
        // 批量升级设置
        allUpgradeSetting: function () {
            var sensorId = "";
            var checkedList = [];
            $("input[name='subChk']:checked").each(function () {
                var jsonStr = $(this).val();
                var jsonObj = $.parseJSON(jsonStr);
                checkedList.push(jsonObj.vehicleId);
                sensorId = jsonObj.sensorId;
            });
            if (checkedList.length > 0) {
                var settingUrl = '/clbs/v/sensorConfig/sensorUpgrade/getSendRemoteUpgradePage_' + checkedList.toString() + "_" + sensorId;
                $("#allUpgradeSetting").attr({
                    'href': settingUrl,
                    'data-toggle': 'modal',
                    'data-target': '#commonWin'
                }).off().click();
            } else {
                layer.msg(selectItem);
            }
            /*$("#allUpgradeSetting").removeAttr('data-toggle','data-target').attr({
                'href': 'javascript:void(0);',
            }).on('click', remoteUpgrade.allUpgradeSetting);*/
        }
    }
    $(function () {
        remoteUpgrade.init();
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'search_condition') {
                firstNode = true;
                remoteUpgrade.treeInit();
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
        $("#checkAll").bind("click", remoteUpgrade.cleckAll);
        subChk.bind("click", remoteUpgrade.subChkClick);
        // 查询全部
        $('#refreshTable').bind("click", remoteUpgrade.queryAll);
        // 组织架构模糊搜索
        $("#search_condition").on("input oninput", remoteUpgrade.searchCondition);
        // 批量升级设置
        $("#allUpgradeSetting").on('click', remoteUpgrade.allUpgradeSetting);

        //改变协议勾选框
        $(".device").change(function() {
            //取消全选
            $("#checkAll").prop('checked',false);
            //刷新表格
            myTable.requestData();
        });
    })
})(window, $)