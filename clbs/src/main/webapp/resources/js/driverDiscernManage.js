(function ($, window) {
    var selectTreeId = '';
    var selectTreepId = "";
    var selectTreeType = '';
    var deviceType = '15';
    var dispatchType;
    var records = []
    var cannotCheckedId = []; // 记录不能被勾选的nodeId
    var clickButtonFlag = false; //是否点击过 批量查询 | 批量下发 |下发驾驶员列表 | 按钮中的其中一个
    var toSearchVehicleIds = []; //即将查询的车辆ids
    var serchedVehicleCount = 0; // 已收到查询结果的车辆计数
    var toDispatchVehicleIds = []; //即将下发的车辆ids
    var dispatchedVehicleCount = 0; // 已收到下发结果的车辆计数
    var peopleId = ''
    window.checkedDriver = []// 存放勾选的驾驶员信息
    function countDown(time, fn) {
        var timer = setInterval(function () {
            if (time > 1) {
                --time;
            } else {
                clearInterval(timer);
                fn && fn();
            }
        }, 1000);
        return {
            cancel: function () {
                clearInterval(timer)
            }
        }

    }

    window.driverIdentification = {
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
                    "data": null,
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        var result = '';
                        result += '<input type="checkbox" name="subChk" data-monitorName="' + row.monitorName + '"  value="' + row.monitorId + '" />';
                        return result;
                    }
                },
                {
                    "data": null,
                    "class": "text-center", //操作按钮
                    render: function (data, type, row, meta) {
                        var btnHtml = '';
                        btnHtml += '<button data-rowId="' + row.monitorId + '" class="editBtn editBtn-info" onclick="driverIdentification.showSearchModal(this)" style="text-indent: -10px;">查询</button>&ensp;';
                        btnHtml += '<button data-rowId="' + row.monitorId + '" class="editBtn editBtn-info"  onclick="driverIdentification.showDispatchModal(this)" style="text-indent: -10px;">下发</button>&ensp;';
                        return btnHtml;
                    }
                },
                {
                    "data": "monitorName",
                    "class": "text-center",
                    render: function (data) {
                        return data || '--';
                    }
                }, {
                    "data": "plateColor",
                    "class": "text-center",
                    render: function (data) {
                        return data || '--';
                    }
                }, {
                    "data": "orgName",
                    "class": "text-center",
                    render: function (data) {
                        return data || '--';
                    }
                },
                {
                    "data": "assignmentName",
                    "class": "text-center",
                    render: function (data) {
                        return data || '--';
                    }
                },
                {
                    "data": "driverNum",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data > 0) {
                            return "<span style='color: #01a9ec;cursor: pointer' data-rowId=" + row.monitorId + "  + onclick=driverIdentification.showDriverList(this)>" + data + "</span>"
                        } else if (data == 0) {
                            return "<span>--</span>"
                        }
                        return '--'

                    }
                }, {
                    "data": "querySuccessTimeStr",
                    "class": "text-center",
                    render: function (data) {
                        return data ? new Date(data).Format("yyyy-MM-dd hh:mm") : '--';
                    }
                }, {
                    "data": "latestIssueTimeStr",
                    "class": "text-center",
                    render: function (data) {
                        return data ? new Date(data).Format("yyyy-MM-dd hh:mm") : '--';
                    }
                }, {
                    "data": "issueStatus",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        switch (data) {
                            case 0:
                                return '等待下发';
                            case 1:
                                return '下发失败';
                            case 2:
                                return '下发中';
                            case 3:
                                return '下发成功';
                            default:
                                return '--';
                        }
                    }
                }, {
                    "data": "issueResult",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        switch (data) {
                            case 0:
                                return '终端已应答';
                            case 1:
                                return '终端未应答';
                            case 2:
                                return '终端离线';
                            default:
                                return '--';
                                break;
                        }
                    }
                }, {
                    "data": "issueUsername",
                    "class": "text-center",
                    render: function (data) {
                        return data || '--';
                    }
                },
            ];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
                d.treeId = selectTreeId; //企业uuid/分组id
                d.treeType = selectTreeType; //树节点类型 0:企业; 1:分组
                d.deviceType = deviceType; //通讯类型 15:交通部JT/T808-2013(苏标); 17:交通部JT/T808-2013(吉标); 26:交通部JT/T808-2019(鲁标);27:交通部JT/T808-2013(湘标)
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/m/driver/discern/manage/pageQuery',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: false, //是否显示第一列的索引列
                enabledChange: true,
                ajaxCallBack: function (data) {
                    records = data.records
                }
            };
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
        },
        driverIdentificationTree: function () {
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
                    dataFilter: driverIdentification.ajaxDataFilter
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
                    onClick: driverIdentification.zTreeOnClick,
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
            //树节点类型 0:企业; 1:分组
            selectTreeType = treeNode.type == "group" ? 0 : 1;
            myTable.requestData();
        },
        //单选
        subChk: function () {
            if ($("input[name='subChk']:checked").length == $("input[name='subChk']").length) {
                $("#checkAll").prop("checked", "checked");
            }
        },
        // 刷新
        refreshTable: function () {
            selectTreeId = "";
            selectTreeType = "";
            $('#simpleQueryParam').val("");
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.selectNode("");
            zTree.cancelSelectedNode();
            myTable.requestData();
        },
        //全选
        checkAll: function (e) {
            $("input[name='subChk']").prop("checked", e.checked);
        },
        //输入框验证
        validateInputValue: function () {
            var value = $("#simpleQueryParam").val()
            if (value.length >= 20) {
                $("#simpleQueryParam").val(value.slice(0, 20))
            }

            function validateFastInput() {
                var simpleQueryParam = $("#simpleQueryParam")
                var regularText = /^[^`\s\^\*;'"\\|,/<>\?]*$/
                if (!regularText.test(value)) {
                    if (!simpleQueryParam.hasClass('checkValidate')) {
                        simpleQueryParam.after("<p " +
                            "style='position: absolute;top: 37px;margin: 0;background: #fff;" +
                            "color: #cf0000;font-weight: normal;width: 300px;border: 1px solid #e4e4e4;text-align: center;" +
                            "height: 30px;line-height: 30px;box-shadow: 0px 2px 2px #e2e2e2;'>" +
                            "请不要输入空格、换行和`^*;\'\\\"|, /<>?" +
                            "</p>")
                        simpleQueryParam.addClass('checkValidate')
                    }
                } else {
                    $("#simpleQueryParam ~ p").remove()
                    simpleQueryParam.removeClass('checkValidate')
                }

            }

            validateFastInput()
        },
        //执行驾驶人员 批量查询指令
        doSearch: function () {
            $("#simpleDataBoxSearch tr td:nth-child(3)").html('等待查询')
            $("#simpleDataBoxSearch tr td:nth-child(4)").html('--')
            clickButtonFlag = true;
            $(this).attr('disabled', true)
            var updataTable = function (msg) {
                if (msg != null) {
                    var result = $.parseJSON(msg.body);
                    if (result != null) {
                        if (result.issueStatus == 1 || result.issueStatus == 3) {
                            serchedVehicleCount++
                        }
                        searchTable.updateRow(result.vehicleId, result)
                        if (serchedVehicleCount == toSearchVehicleIds.length) {
                            $("#doSearch").attr('disabled', false)
                            serchedVehicleCount = 0
                        }
                    }
                }
            }
            webSocket.subscribe(headers, "/user/topic/query/batch", updataTable, "/app/query/batch", toSearchVehicleIds);
        },
        //执行驾驶人员 批量下发指令
        doDispatch: function () {
            $("#dispatchBatchModal tr td:nth-child(3)").html('等待下发')
            $("#dispatchBatchModal tr td:nth-child(4)").html('--')
            //获取勾选的驾驶人员信息
            var treeObj = $.fn.zTree.getZTreeObj("driverListTree");
            var nodes = treeObj.getCheckedNodes(true);
            nodes = nodes.filter(function (item) {
                return item.type == 'people'
            })
            if ($("#actionType").val() != '1' && nodes.length == 0) {
                layer.msg('请选择驾驶员！', {move: false});
                return;
            }
            clickButtonFlag = true;
            $(this).attr('disabled', true)
            var updataTable = function (msg) {
                if (msg != null) {
                    var result = $.parseJSON(msg.body);
                    if (result != null) {
                        if (result.issueStatus == 1 || result.issueStatus == 3) {
                            dispatchedVehicleCount++
                        }
                        dispatchTable.updateRow(result.vehicleId, result)
                        if (dispatchedVehicleCount == toDispatchVehicleIds.length) {
                            $("#doDispatch").attr('disabled', false)
                            dispatchedVehicleCount = 0
                        }
                    }
                }
            }
            var requestStr = {}
            requestStr.type = Number($("#actionType").val())
            requestStr.vehicleIds = toDispatchVehicleIds
            requestStr.proIds = nodes.map(function (item) {
                return item.id
            })
            var deviceType = ''
            $('#deviceType input:radio').each(function (a, b) {
                if (b.checked) {
                    deviceType = b.value
                }
            });
            requestStr.deviceType = deviceType
            webSocket.subscribe(headers, "/user/topic/issue/batch", updataTable, "/app/issue/batch", requestStr);
        },
        //执行驾驶人员列表 下发查询指令
        doDriverListDispatch: function () {
            clickButtonFlag = true;
            $(this).attr('disabled', true)
            var trId = $("#driverListBox22 tbody tr").attr('id')
            // var counter = countDown(90,function () {
            //     driverListTable2.modifyTr(trId, 3, '终端未应答')
            // })
            driverListTable2.modifyTr(trId, 2, new Date().Format('yyyy-MM-dd hh:mm:ss'))
            driverListTable2.modifyTr(trId, 3, '--')
            var updataTable = function (msg) {
                if (msg == null) return
                var result = $.parseJSON(msg.body);
                if (result != null) {
                    // counter.cancel() //取消倒计时事件
                    result.querySuccessTimeStr = result.querySuccessTime
                    result.queryResult = result.issueResult
                    driverListTable2.updateRow(result.vehicleId, result)
                    if (result.issueStatus == 1 || result.issueStatus == 3) {
                        $("#doDriverListDispatch").attr('disabled', false)
                    }
                    // 如果通过json_ajax()来加载数据，会出现加载成功后无法再次出弹出layer层的bug,因此通过原生$.ajax()来加载数据
                    setTimeout(function () {
                        if (result.issueStatus == 3) {
                            $.ajax(
                                {
                                    type: "GET",
                                    url: '/clbs/m/driver/discern/manage/detail',
                                    contentType: 'application/x-www-form-urlencoded',
                                    dataType: "json", //预期服务器返回的数据类型。"json"
                                    async: false, // 异步同步，true  false
                                    data: {id: trId},
                                    timeout: 30000, //超时时间设置，单位毫秒
                                    success: function (data) {
                                        if (data.success) {
                                            layer.closeAll()
                                            data.obj.forEach(function (value, index, arr) {
                                                arr[index].monitorId = arr[index].faceId
                                            })
                                            driverListTable.refresh(data.obj)
                                        }
                                    }
                                });
                        }
                    }, 500);
                }
            }
            webSocket.subscribe(headers, "/user/topic/query/batch", updataTable, "/app/query/batch", toSearchVehicleIds);
        },
        //批量查询弹框
        showSearchModal: function (el) {
            var mockData = []
            if (el) { //查询单个人员
                $("#searchBatchModal .modal-title").html("驾驶人员查询")
                var rowId = $(el).attr('data-rowId')
                toSearchVehicleIds = [rowId]
                var rowData = records.find(function (item) {
                    return rowId == item.monitorId
                })
                if (rowData) {
                    mockData = [rowData]
                }
            } else { //批量查询
                var checkedRows = $("input[name='subChk']:checked");
                var checkedNum = checkedRows.length
                if (checkedNum == 0 && !el) {
                    layer.msg('请勾选需要查询的车辆！', {move: false});
                    return
                }
                $("#searchBatchModal .modal-title").html("驾驶人员批量查询")
                toSearchVehicleIds = []
                checkedRows.each(function (row) {
                    var rowId = $(checkedRows[row]).val()
                    toSearchVehicleIds.push(rowId)
                    var rowData = records.find(function (record) {
                        return rowId == record.monitorId
                    })
                    if (rowData) {
                        mockData.push(rowData)
                    }
                })
            }
            $("#searchBatchModal").modal('show')
            var columnDefine = [
                {
                    name: 'monitorName',
                },
                {
                    name: 'issueStatus',
                    render: function (data) {
                        switch (data) {
                            case 0:
                                return '等待查询';
                            case 1:
                                return '查询失败';
                            case 2:
                                return '查询中';
                            case 3:
                                return '查询成功';
                            default:
                                return '等待查询';
                        }
                    }
                },
                {
                    name: 'issueResult',
                    render: function (data) {
                        switch (data) {
                            case 0:
                                return '终端已应答';
                            case 1:
                                return '终端未应答';
                            case 2:
                                return '终端离线';
                            default:
                                return '--';
                        }
                    }
                }
            ]
            searchTable = new SimpleTable('simpleDataBoxSearch')
            mockData.forEach(function (item, index, arr) {
                arr[index].issueStatus = 0
                arr[index].issueResult = -1
            })
            searchTable.render(mockData, columnDefine)
        },
        //批量下发弹框11
        showDispatchModal: function (el) {
            var mockData = []
            if (el) { //下发单个人员
                $("#dispatchBatchModal .modal-title").html("驾驶人员下发")
                var rowId = $(el).attr('data-rowId')
                toDispatchVehicleIds = [rowId]
                var rowData = records.find(function (item) {
                    return rowId == item.monitorId
                })
                if (rowData) {
                    mockData = [rowData]
                }
            } else { //批量下发
                var checkedRows = $("input[name='subChk']:checked");
                var checkedNum = checkedRows.length
                if (checkedNum == 0 && !el) {
                    layer.msg('请勾选需要下发的车辆！', {move: false});
                    return
                }
                $("#dispatchBatchModal .modal-title").html("驾驶人员批量下发")
                toDispatchVehicleIds = []
                checkedRows.each(function (row) {
                    var rowId = $(checkedRows[row]).val()
                    toDispatchVehicleIds.push(rowId)
                    var rowData = records.find(function (record) {
                        return rowId == record.monitorId
                    })
                    if (rowData) {
                        mockData.push(rowData)
                    }
                })
            }
            $("#dispatchBatchModal").modal('show')
            setTimeout(driverIdentification.loadDriverTree, 300)
            dispatchTable = new SimpleTable('simpleDataBox')
            var columnDefine = [
                {
                    name: 'monitorName',
                },
                {
                    name: 'issueStatus',
                    render: function (data) {
                        switch (data) {
                            case 0:
                                return '等待下发';
                            case 1:
                                return '下发失败';
                            case 2:
                                return '下发中';
                            case 3:
                                return '下发成功';
                            default:
                                return '等待下发';
                        }
                    }
                },
                {
                    name: 'issueResult',
                    render: function (data) {
                        switch (data) {
                            case 0:
                                return '终端已应答';
                            case 1:
                                return '终端未应答';
                            case 2:
                                return '终端离线';
                            default:
                                return '--';
                        }
                    }
                }
            ]
            mockData.forEach(function (item, index, arr) {
                arr[index].issueStatus = 0
                arr[index].issueResult = -1
            })
            dispatchTable.render(mockData, columnDefine)
        },
        //驾驶人员列表弹窗
        showDriverList: function (el) {
            var rowId = $(el).attr('data-rowId')
            toSearchVehicleIds = [rowId]
            $("#driverListModal").modal("show")
            // table1
            driverListTable = new SimpleTable('driverListBox')
            var columnDefine = [
                {
                    name: 'faceId',
                },
                {
                    name: 'name'
                },
                {
                    name: 'orgName',
                },
                {
                    name: 'cardNumber'
                },
                {
                    name: 'photograph',
                    render: function (data) {
                        if (!data) {
                            return "<i class='glyphicon glyphicon-picture' style='font-size: 19px;transform: scaleX(1.3);'></i>"
                        }
                        // var baseUrl = 'http://192.168.24.144:8799/mediaserver/profesionalpic/'
                        return "<i class='glyphicon glyphicon-picture' style='color: #30b9ef;font-size: 19px;transform: scaleX(1.3);'>" +
                            "<div style='position: absolute;top: -2px;right: 30px;width: 118px;height: 188px;'>" +
                            "<img src=" + data + ">" +
                            "</div>" +
                            "</i>"


                    }
                },
                {
                    name: 'delete',
                    render: function (data, row) {
                        return "<span style='color: #01a9ec;text-decoration: underline;line-height: 19px;cursor: pointer;' + data-uid=" + row.faceId + ">" +
                            "<button style='width:50px'>" +
                            "删除" +
                            "</button>" +
                            "</span>"
                    }
                }
            ]
            json_ajax("GET", '/clbs/m/driver/discern/manage/detail', "json", false, {id: rowId}, function (data) {
                if (data.success) {
                    data.obj.forEach(function (value, index, arr) {
                        arr[index].monitorId = arr[index].faceId
                    })
                    driverListTable.render(data.obj, columnDefine)
                }
            })

            // table2
            driverListTable2 = new SimpleTable('driverListBox22', {withIndex: false})
            var mockData2 = []
            var columnDefine2 = [
                {
                    name: 'monitorName',
                },
                {
                    name: 'querySuccessTimeStr',
                    render: function (data) {
                        return data ? new Date(data).Format("yyyy-MM-dd hh:mm:ss") : '--';
                    }
                },
                {
                    name: 'latestQueryTimeStr'
                },
                {
                    name: 'queryResult',
                    render: function (data) {
                        switch (data) {
                            case 0:
                                return '终端已应答';
                            case 1:
                                return '终端未应答';
                            case 2:
                                return '终端离线';
                            default:
                                return '--';
                        }
                    }
                }
            ]
            var rowData = records.find(function (item) {
                return rowId == item.monitorId
            })
            mockData2 = [rowData]
            driverListTable2.render(mockData2, columnDefine2)
        },
        //驾驶人员列表弹窗 删除
        deleteDriver: function (e) {
            peopleId = $(e.target).parent().attr("data-uid")
            if (peopleId) {
                $(e.target).css('color', "#8e8e8e")
                layer.confirm('确认是否下发指令删除终端中的该驾驶员？', {
                    title: '操作确认',
                    btn: ['确认', '取消'],
                    icon: 3,
                    cancel: function () {
                        $(e.target).css('color', "#01a9ec")
                    }
                }, function () {
                    clickButtonFlag = true
                    layer.msg('已下发删除指令！', {icon: 1, time: 1000, index: 1000});
                    driverIdentification.doSendDeleteQuery();
                    $(e.target).attr("disabled", true)
                }, function () {
                    $(e.target).attr("disabled", false)
                    $(e.target).css('color', "#01a9ec")
                });
            }
        },
        //发送删除驾驶人员请求
        doSendDeleteQuery: function () {
            var updataTable = function (data) {
                if (data == null) return
                var result = $.parseJSON(data.body);
                if (result != null) {
                    var target = $("#" + peopleId + " " + "span[data-uid='" + peopleId + "']").children()
                    switch (result.issueResult) {
                        case 0:
                            layer.msg('删除成功！', {icon: 1});
                            break;
                        case 1:
                            layer.confirm('删除失败，终端未应答！', {
                                btn: ['确认', '取消'],
                                title: '操作失败',
                                icon: 0
                            });
                            target.attr("disabled", false)
                            target.css('color', "#01a9ec")
                            break;
                        case 2:
                            layer.confirm('删除失败，终端已离线！', {
                                btn: ['确认', '取消'],
                                title: '操作失败',
                                icon: 0
                            });
                            target.attr("disabled", false)
                            target.css('color', "#01a9ec")
                            break;
                    }
                }
            }
            var vehicleId = $("#driverListBox22 tbody tr").attr('id')
            var requestStr = {
                type: 2,
                vehicleIds: [vehicleId],
                proIds: [peopleId],
                deviceType: $('#deviceType input:checked').val()
            }
            webSocket.subscribe(headers, "/user/topic/issue/batch", updataTable, "/app/issue/batch", requestStr);
        },
        //组织树协议类型改变
        deviceChange: function () {
            var selectValue = $(this).val();
            deviceType = selectValue;
            driverIdentification.refreshTable();
        },
        //批量下发弹框 选择类型改变
        actionTypeChange: function () {
            // var value = $(this).val()
            // var treeObj = $.fn.zTree.getZTreeObj("driverListTree");
            // $("#driverListInput").val("")
            // treeObj.checkAllNodes(false);
            // switch (value) {
            //     case '1':
            //         $("#driverBox").hide();
            //         break;
            //     case '2':
            //         $("#driverBox").show();
            //         driverIdentification.driverTreeAllCheckble();
            //         break;
            //     default:
            //         $("#driverBox").show();
            //         driverIdentification.driverTreePartialCheckble(window.cannotCheckedId);
            // }
            // dispatchType = value

            $("#driverListInput").val("")
            $(this).val() == 1 ? $("#driverBox").hide() : $("#driverBox").show()
            dispatchType = $(this).val()
            driverIdentification.loadDriverTree()
        },
        //驾驶人员全部可选
        driverTreeAllCheckble: function () {
            var treeObj = $.fn.zTree.getZTreeObj("driverListTree");
            var rootNode = treeObj.getNodes();
            for (var i = 0, l = rootNode.length; i < l; i++) {
                treeObj.setChkDisabled(rootNode[i], false, true, true);
            }
        },
        //驾驶人员部分可选
        driverTreePartialCheckble: function (cannotCheckIds) {
            var treeObj = $.fn.zTree.getZTreeObj("driverListTree");
            var rootNode = treeObj.getNodes();
            var nodes = treeObj.transformToArray(rootNode);
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (cannotCheckIds && cannotCheckIds.includes(nodes[i].id)) {
                    treeObj.setChkDisabled(nodes[i], true);
                }
            }
        },
        //加载驾驶人员组织树
        loadDriverTree: function (queryString) {
            // $("#driverListInput").val("")
            var zTreeOnAsyncSuccess = function (event, treeId, treeNode, msg) {
                var treeObj = $.fn.zTree.getZTreeObj(treeId);
                treeObj.expandAll(true); // 展开所有节点
            }
            var zTreeOnCheck = function (event, treeId, treeNode) {
                var treeObj = $.fn.zTree.getZTreeObj("driverListTree");
                var nodes = treeObj.getCheckedNodes(true);
                nodes = nodes.filter(function (item) {
                    return item.type == 'people'
                })
                var placeholder = ''
                var tempNameArr = []
                var length = nodes.length
                for (var i = 0; i < length; i++) {
                    if (i < 3) {
                        tempNameArr.push(nodes[i].name.split("(")[0])
                    }
                }
                if (length <= 3) {
                    placeholder = tempNameArr.join('、')
                } else {
                    placeholder = tempNameArr.join('、') + "等" + length + "人"
                }
                $("#driverListInput").val(placeholder)
            };
            //原始数据处理
            var handleRowData = function (treeId, parentNode, responseData) {
                if (responseData.msg) {
                    var obj = JSON.parse(ungzip(responseData.msg));
                    var data;
                    if (obj.tree != null && obj.tree != undefined) {
                        data = obj.tree;
                        size = obj.size;
                    } else {
                        data = obj
                    }
                    for (var i = 0; i < data.length; i++) {
                        data[i].open = true;
                    }
                }
                if (data) {
                    var length = data.length
                    for (var i = 0; i < length; i++) {
                        if (data[i].type != 'people') {
                            continue
                        }
                        var photographFlag = data[i].photograph
                        var cardNumberFlag = data[i].cardNumber
                        if ((!photographFlag || !cardNumberFlag) && $("#actionType").val() != 2) {
                            data[i].chkDisabled = true;
                            //记录不可勾选node 用来在改变后恢复不可勾选状态
                            cannotCheckedId.push(data[i].id)
                        }
                        if (photographFlag && !cardNumberFlag) {
                            data[i].name = data[i].name + "(无资格证、有照片)"
                        }
                        if (!photographFlag && cardNumberFlag) {
                            data[i].name = data[i].name + "(有资格证、无照片)"
                        }
                        if (!photographFlag && !cardNumberFlag) {
                            data[i].name = data[i].name + "(无资格证、无照片)"
                        }
                        if (photographFlag && cardNumberFlag) {
                            data[i].name = data[i].name + "(有资格证、有照片)"
                        }
                    }
                    window.cannotCheckedId = cannotCheckedId
                }
                return data;
            };
            var setting = {
                async: {
                    url: "/clbs/m/basicinfo/enterprise/professionals/getProTree",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    contentType: "application/x-www-form-urlencoded",
                    otherParam: {"name": queryString || ''},
                    dataType: "json",
                    dataFilter: handleRowData
                },
                view: {
                    dblClickExpand: false,
                    nameIsHTML: true,
                    countClass: "group-number-statistics",
                    showIcon: true,
                },
                data: {
                    simpleData: {
                        enable: true
                    },
                },
                callback: {
                    onAsyncSuccess: zTreeOnAsyncSuccess,
                    onCheck: zTreeOnCheck
                },
                check: {
                    enable: true
                }
            };

            $.fn.zTree.init($("#driverListTree"), setting, null);
        },
    }
    //简易表格
    SimpleTable = function (divId, options) {
        if (!divId) throw '请传入id'
        options = options || {}
        this.columnDefine = [];
        this.options = {
            withIndex: options.withIndex === undefined ? true : options.withIndex, //带上序号
        }
        this._table = $("#" + divId);
        this.withIndex = function (data) {
            if (!data) return
            var newData = data.map(function (item, index) {
                var temp = {}
                for (var key in item) {
                    temp[key] = item[key]
                }
                temp.index = index + 1
                return temp
            })
            return newData
        }
        // 渲染
        this.render = function (data, columnDefine) {
            this.columnDefine = columnDefine
            if (!data || !this._table) return;
            if (this.options.withIndex) {
                data = this.withIndex(data)
            }
            var $body = this._table.find('tbody')
            var bodyHtml = ''
            var that = this
            data.map(function (item) {
                bodyHtml += that.renderTr(item)
            })
            $body.html(bodyHtml)
        };
        this.renderTr = function (item) {
            var res = '';
            var allTd = ''
            if (this.columnDefine) {
                this.columnDefine.forEach(function (column) {
                    if (column.render) {
                        allTd += "<td>" + column.render(item[column.name], item) + "</td>"
                    } else if (item[column.name]) {
                        allTd += "<td>" + item[column.name] + "</td>"
                    } else {
                        allTd += "<td>" + "--" + "</td>"
                    }
                })
            }
            if (this.options.withIndex) {
                res = "<tr id=" + item.monitorId + ">"
                    + "<td>" + item.index + "</td>"
                    + allTd
                    + "</tr>";
            } else {
                res = "<tr id=" + item.monitorId + ">"
                    + allTd
                    + "</tr>";
            }

            return res
        };
        // 修改
        this.modifyTr = function (id, tdIndex, html) {
            this._table.find("#" + id + " " + "td").eq(tdIndex).html(html)
        };
        // 修改某一行
        this.updateRow = function (id, data) {
            var that = this
            var keys = Object.keys(data)
            keys.forEach(function (keyItem) {
                var tdIndex = 0
                var column = that.columnDefine.find(function (_item, index) {
                    if (that.options.withIndex) {
                        tdIndex = index + 1
                    } else {
                        tdIndex = index
                    }
                    return _item.name == keyItem
                })
                var td = ''
                if (column) {
                    td = column.render(data[keyItem])
                    that._table.find("#" + id + " " + "td").eq(tdIndex).html(td)
                }
            })
        };
        // 新增
        this.addItem = function (data, index) {
            var res = this.renderTr(data)
            if (index) {
                this._table.find('tr').eq(index).after(res)
            } else {
                this._table.find("tbody").append(res)
            }
        };
        // 删除
        this.del = function (id) {

        };
        // 销毁
        this.destory = function () {
            this._table.remove()
        };
        // 刷新
        this.refresh = function (data) {
            this.render(data, this.columnDefine)
        };
    }
    $(function () {
        getTable("dataTables");
        driverIdentification.driverIdentificationTree();
        driverIdentification.init();
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'search_condition') {
                $("#noData").hide()
                search_ztree('treeDemo', id, 'assignment');
            }
            if (id == 'driverListInput') {
                // search_ztree('driverListTree', id, 'assignment');
                // var treeObj = $.fn.zTree.getZTreeObj("driverListTree");
                // var nodes = treeObj.getNodesByParam("isHidden", true);
                // treeObj.showNodes(nodes);
                // treeObj.checkAllNodes(false);
                driverIdentification.loadDriverTree()
            }
        });
        $('#refreshTable').on("click", driverIdentification.refreshTable);
        $('#searchBatch').on('click', function () {
            driverIdentification.showSearchModal()
        });
        $('#dispatchBatch').on('click', function () {
            driverIdentification.showDispatchModal()
        });
        $("#checkAll").on('click', driverIdentification.checkAll);
        $("#doSearch").on('click', driverIdentification.doSearch);
        $("#doDispatch").on('click', driverIdentification.doDispatch);
        $("#doDriverListDispatch").on('click', driverIdentification.doDriverListDispatch);
        $("input[name='deviceType']").on('change', driverIdentification.deviceChange);
        $("input[name='deviceType']").prop("checked", false);
        $($("input[name='deviceType']")[0]).prop("checked", 'checked');
        $("#actionType").on("change", driverIdentification.actionTypeChange);
        // 组织架构模糊搜索
        $("#search_condition").on("input", function () {
            search_ztree('treeDemo', 'search_condition', 'assignment')
            // console.log($("#treeDemo li:first").is(":visible"))
            if (!$("#treeDemo li:first").is(":visible")) {
                $("#noData").show()
            } else {
                $("#noData").hide()
            }
        });
        // 驾驶人员模糊搜索
        var timer = null
        $("#driverListInput").on("input", function () {
            // search_ztree('driverListTree', 'driverListInput', 'people')
            clearTimeout(timer)
            timer = setTimeout(function () {
                var queryString = $("#driverListInput").val() || ''
                driverIdentification.loadDriverTree(queryString)
            }, 300)
        });
        $("#driverListInput").on("click", function (e) {
            e.stopPropagation()
            var $box = $("#driverListTreeBox")
            if ($box.is(':hidden')) {
                $box.show()
                setTimeout(function () {
                    $(document).on("click", function (e) {
                        if (!$("#driverListTreeBox").parent().get(0).contains(e.target)) {
                            $("#driverListTreeBox").hide()
                        }
                    });
                }, 0)
            }
        })
        $("#driverListBox").on('click', driverIdentification.deleteDriver)
        $("#driverListModal, #searchBatchModal, #dispatchBatchModal").on('hide.bs.modal', function (e) {
            if (clickButtonFlag) {
                myTable.refresh();
                toDispatchVehicleIds = []
                toSearchVehicleIds = []
                clickButtonFlag = false
                webSocket.unsubscribe('/user/topic/query/batch')
                webSocket.unsubscribe('/user/topic/issue/batch')
            }
            toDispatchVehicleIds = []
            toSearchVehicleIds = []
            serchedVehicleCount = 0
            dispatchedVehicleCount = 0
            $("#doSearch").attr('disabled', false)
            $("#doDispatch").attr('disabled', false)
            $("#doDriverListDispatch").attr('disabled', false)
            $("#driverListInput").val('')
            $("#actionType").val('0')
            $("#driverBox").show()
        })
    })
}($, window))