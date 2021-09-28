(function (window, $) {
    //车辆列表
    var vehicleList = [];
    //车辆id列表
    var vehicleId = [];

    var myTable;
    var tableData = []
    var checkedPrimaryKeys = []
    var checkFlag = false; //判断组织节点是否是勾选操作
    var size;//当前权限监控对象数量
    var zTreeIdJson = {};

    var currentPK = ''

    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询
    var ifAllCheck = true; //刚进入页面小于TREE_MAX_CHILDREN_LENGTH自动勾选
    var searchFlag = true;

    equipmentRepair = {
        init: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(2) +"\" disabled />"+ table[0].innerHTML +"</label></li>"
            for (var i = 1; i < table.length - 1; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            menu_text += "<li><label><input type=\"checkbox\"  class=\"toggle-vis\" data-column=\"" + parseInt(table.length) + "\" />" + table[i].innerHTML + "</label></li>"
            $("#Ul-menu-text").html(menu_text);
            var setting = {
                async: {
                    url: '/clbs/m/basicinfo/enterprise/professionals/tree',
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    dataFilter: equipmentRepair.ajaxDataFilter,
                    otherParam: {
                        "type": "multiple",
                        "icoType": "0"
                    },
                },
                check: {
                    enable: true,
                    chkStyle: "radio",
                    chkboxType: {
                        "Y": "s",
                        "N": "s"
                    },
                    radioType: "all"
                },
                view: {
                    dblClickExpand: false,
                    nameIsHTML: true,
                    countClass: "group-number-statistics"
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    onAsyncSuccess: equipmentRepair.zTreeOnAsyncSuccess,
                    onCheck: equipmentRepair.onCheckVehicle,
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },

        //组织树预处理加载函数
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            var userGroupId = $("#userGroupId").val()
            var userGroupName = $("#userGroupName").val()
            var newData = responseData.map(function (item) {
                if(item.uuid == userGroupId){
                    $("#groupSelect").val(userGroupName)
                    item.checked = true
                }
                return item
            })
            return newData;
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
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
            return data;
        },
        //判断日期是否合法,是否选中车辆
        validates: function () {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            var nodes = treeObj.getCheckedNodes(true);
            if (nodes.length == 0) {
                $('#groupSelect').after('<p class="random_jhsdkjahs" style="color: #fff;background: #b94a48;border: solid thin #fff;padding: 3px 5px;position: absolute;font-weight: bold;z-index: 10;">请选择组织</p>')
                return false
            } else {
                $('#groupSelect').parent().find('.random_jhsdkjahs').remove()
                return true
            }
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.expandAll(true);
            return
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (size <= TREE_MAX_CHILDREN_LENGTH && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            equipmentRepair.getCharSelect(treeObj);
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            var nodes = treeObj.getCheckedNodes(true);
            if (nodes.length > 0) {
                $("#groupSelect").val(nodes[0].name);
            } else {
                $("#groupSelect").val("");
            }
            equipmentRepair.validates()
        },
        getTable: function (table) {
            var columns = [
                {
                    data: null,
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return '<input type="checkbox" name="subChk" value="' + row.primaryKey + '" id="' + row.primaryKey + '"/>';
                    }
                },
                {
                    "data": null,
                    "class": "text-center", //操作按钮
                    render: function (data, type, row, meta) {
                        var btnConfirmHtml = ''
                        var btnFinishHtml = ''
                        if(row.handleStatus == 1 || row.handleStatus == 2){
                            btnConfirmHtml = '<button disabled="disabled" class=" editBtn btn-default" style="padding-left: 13px !important;"><img src="/clbs/resources/img/comfirm_finish.svg" style="width:14px;margin-top:-2px;margin-right: 2px;"/>确认</button>&ensp;';
                        }else {
                            btnConfirmHtml = '<button class="editBtn editBtn-info" onclick="equipmentRepair.showConfirmModal(\'' + row.primaryKey+ '\')" style="padding-left: 13px !important;"><img src="/clbs/resources/img/comfirm_finish.svg" style="width:14px;margin-top:-2px;margin-right: 2px;"/>确认</button>&ensp;';
                        }
                        if(row.handleStatus == 0 || row.handleStatus == 2 || row.handleStatus == 3){
                            btnFinishHtml = '<button disabled="disabled" class=" editBtn btn-default"  style="padding-left: 13px !important;"><img src="/clbs/resources/img/comfirm_finish.svg" style="width:14px;margin-top:-2px;margin-right: 2px;"/>完成</button>&ensp;';
                        }else {
                            btnFinishHtml = '<button class="editBtn editBtn-info"  onclick="equipmentRepair.showFinishModal(\'' + row.primaryKey+ '\')" style="padding-left: 13px !important;"><img src="/clbs/resources/img/comfirm_finish.svg" style="width:14px;margin-top:-2px;margin-right: 2px;"/>完成</button>&ensp;';
                        }
                        return btnConfirmHtml + btnFinishHtml;
                    }
                },
                {
                    "data": "orgName",
                    "class": "text-center",
                    render: function (data) {
                        return data || '-';
                    }
                }, {
                    "data": "monitorName",
                    "class": "text-center",
                    render: function (data) {
                        return data || '-';
                    }
                }, {
                    "data": "terminalVendor",
                    "class": "text-center",
                    render: function (data) {
                        return data || '-';
                    }
                },
                {
                    "data": "deviceNumber",
                    "class": "text-center",
                    render: function (data) {
                        return data || '-';
                    }
                },
                {
                    "data": "terminalType",
                    "class": "text-center",
                    render: function (data) {
                        return data || '-';
                    }
                }, {
                    "data": "reportRepairTime",
                    "class": "text-center",
                    render: function (data) {
                        return data || '-';
                    }
                }, {
                    "data": "faultType",
                    "class": "text-center",
                    render: function (data) {
                        switch (data) {
                            case -1:
                                return '全部';
                            case 0:
                                return '主存储器异常';
                            case 1:
                                return '备用存储器异常';
                            case 2:
                                return '卫星信号异常';
                            case 3:
                                return '通信信号异常';
                            case 4:
                                return '备用电池欠压';
                            case 5:
                                return '备用电池失效';
                            case 6:
                                return 'IC卡从业资格证模块故障';
                            default :
                                return '-';
                        }
                    }
                },
                {
                    "data": "handleStatus",
                    "class": "text-center",
                    render: function (data) {
                        switch (data) {
                            case -1:
                                return '全部';
                            case 0:
                                return '未确认';
                            case 1:
                                return '已确认';
                            case 2:
                                return '已完成';
                            case 3:
                                return '误报';
                            default :
                                return '-';
                        }
                    }
                },
                {
                    "data": "repairDate",
                    "class": "text-center",
                    render: function (data) {
                        return data || '-';
                    }
                },
                {
                    "data": 'primaryKey',
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return '<button class="editBtn editBtn-info" onclick="equipmentRepair.showResultModal(\'' + data+ '\')" style="padding: 3px 8px !important;"><img src="/clbs/resources/img/lookUpWhite.svg" style="width:14px;margin-top:-2px;margin-right: 2px;">查看</button>'
                    }
                },
                {
                    "data": 'remark',
                    "class": "text-center",
                    render: function (data) {
                        return data || '-';
                    }
                },
            ];
            var handleRowData = function (json) {
                tableData = json.records
            }
            //ajax参数
            var ajaxDataParamFun = function (params) {
                var searchParams = equipmentRepair.getSearchParams()
                params.simpleQueryParam = searchParams.simpleQueryParam;
                params.reportStartTime = searchParams.reportStartTime;
                params.reportEndTime = searchParams.reportEndTime;
                params.orgId = searchParams.orgId;
                params.faultType = searchParams.faultType;
                params.handleStatus = searchParams.handleStatus;
                params.repairStartTime = searchParams.repairStartTime;
                params.repairEndTime = searchParams.repairEndTime;
            };
            var drawCallback = function (s) {
                if(!$('#Ul-menu-text input:last').prop('checked')){
                    myTable.dataTable.column(12).visible(false)
                }
            }
            //表格setting
            var setting = {
                // /clbs/adas/equipmentRepair/list   /clbs/m/driver/discern/manage/pageQuery
                listUrl: '/clbs/adas/equipmentRepair/list',
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                ajaxCallBack: handleRowData, //服务器数据预处理
                drawCallbackFun: drawCallback, //回调
                pageable: true, //是否分页
                showIndexColumn: false, //是否显示第一列的索引列
                enabledChange: true,
            };
            myTable = new TG_Tabel.createNew(setting);
            // return
            myTable.init();
        },
        reloadData: function (dataList) {
            var currentPage = myTable.page()
            myTable.clear()
            myTable.rows.add(dataList)
            myTable.page(currentPage).draw(false);
            myTable.search('', false, false).page(currentPage).draw();
        },
        //刷新列表
        refreshTable: function () {
            $('#simpleQueryParam').val('')
            equipmentRepair.search()
        },
        // 显示查询结果弹窗
        showResultModal:function(primaryKey){
            var data = tableData.find(function (item) {
                return item.primaryKey == primaryKey
            })
            if(!data) return
            var param = {
                orgId:data.orgId,
                date:data.reportRepairTime,
                vehicleId:data.monitorId,
                dataSource:1,
            }
            json_ajax("POST", '/clbs/adas/vehicleDeviceState/single', "json", true, param, function(data){
                if(data.success){
                    $("#resultModal").modal('show')
                    var setting = [
                        {
                            title: '位置',
                            dataIndex: 'address',
                        },
                        {
                            title: '卫星高程',
                            dataIndex: 'altitude',
                            render:function (data) {
                                return data + '米'
                            }
                        },
                        {
                            title: '卫星速度',
                            dataIndex: 'speed',
                            render:function (data) {
                                return data + 'km/h'
                            }
                        },
                        {
                            title: '卫星方向',
                            dataIndex: 'direction',
                            render:function (data) {
                                return data + '°'
                            }
                        },
                        {
                            title: '原车速度',
                            dataIndex: 'vehicleSpeed',
                            render:function (data) {
                                return data + 'km/h'
                            }
                        },
                        {
                            title: '时间',
                            dataIndex: 'time',
                        },
                        {
                            title: 'X轴加速度',
                            dataIndex: 'axisAccelerationX',
                        },
                        {
                            title: 'Y轴加速度',
                            dataIndex: 'axisAccelerationY',
                        },
                        {
                            title: 'Z轴加速度',
                            dataIndex: 'axisAccelerationZ',
                        },
                        {
                            title: 'X轴角速度',
                            dataIndex: 'axisAngularX',
                        },
                        {
                            title: 'Y轴角速度',
                            dataIndex: 'axisAngularY',
                        },
                        {
                            title: 'Z轴角速度',
                            dataIndex: 'axisAngularZ',
                        },
                        {
                            title: '制动状态',
                            dataIndex: 'brakingStatus',
                            render:function (data) {
                                return data == 0 ? '无制动' : '制动'
                            }
                        },
                        {
                            title: '转向灯状态',
                            dataIndex: 'turnSignalStatus',
                            render:function (data) {
                                if(data == 0) return '未开方向灯'
                                return data == 1 ? '左转方向灯' : '右转方向灯'
                            }
                        },
                        {
                            title: '远光灯状态',
                            dataIndex: 'highBeamStatus',
                            render:function (data) {
                                return data == 0 ? '关' : '开'
                            }
                        },
                        {
                            title: '近光灯状态',
                            dataIndex: 'lowBeamStatus',
                            render:function (data) {
                                return data == 0 ? '关' : '开'
                            }
                        },
                        {
                            title: '档位状态',
                            dataIndex: 'gearStatus',
                            render:function (data) {
                                switch (data) {
                                    case 0:
                                        return '空挡';
                                    case 16:
                                        return '倒挡';
                                    case 17:
                                        return '驻车档';
                                    default :
                                        return data + '档'
                                }
                            }
                        },
                        {
                            title: '加速踏板行程值',
                            dataIndex: 'acceleratorPedalValue',
                        },
                        {
                            title: '制动踏板行程值',
                            dataIndex: 'brakePedalValue',
                        },
                        {
                            title: '发动机转速',
                            dataIndex: 'engineSpeed',
                        },
                        {
                            title: '方向盘角度',
                            dataIndex: 'steeringWheelAngle',
                        },
                        {
                            title: '空调状态',
                            dataIndex: 'airStatus',
                            render:function (data) {
                                return data == 0 ? '关' : '开'
                            }
                        },
                        {
                            title: '加热器状态',
                            dataIndex: 'heaterStatus',
                            render:function (data) {
                                return data == 0 ? '关' : '开'
                            }
                        },
                        {
                            title: '离合器状态',
                            dataIndex: 'clutchStatus',
                            render:function (data) {
                                return data == 0 ? '关' : '开'
                            }
                        },
                        {
                            title: 'ABS状态',
                            dataIndex: 'absStatus',
                            render:function (data) {
                                return data == 0 ? '关' : '开'
                            }
                        },
                        {
                            title: '示廓灯状态',
                            dataIndex: 'clearanceLampStatus',
                            render:function (data) {
                                return data == 0 ? '关' : '开'
                            }
                        },
                    ]
                    var html = equipmentRepair.renderResult(data.obj,setting)
                    $('#resultBody').html(html)
                }
            });
        },
        renderResult:function (data,setting) {
            var html = ''
            setting.forEach(function (item) {
                var dataHtml = ''
                if(typeof item.render == 'function'){
                    dataHtml = item.render(data[item.dataIndex],data)
                }else {
                    dataHtml = data[item.dataIndex] || '-'
                }
                html += '<div class="col-md-6" style="padding: 0;margin-bottom: 11px;">\n' +
                    '<strong class="col-md-6 text-right" style="padding: 0;padding-right: 12px;">' + item.title +': </strong>\n' +
                    '<p class="col-md-6" style="padding: 0">' + dataHtml +'</p>\n' +
                    '</div>'
            })
            return html
        },

        inquireClick: function (number) {
            var referDate = $('#timeInterval').val().split('--')
            var endDate = equipmentRepair.getDate(-1,referDate[0].split(' ')[0])
            var startDate = equipmentRepair.getDate(number + 1,endDate)
            var startTime = number == 0 ? referDate[0].split(' ')[1] : '00:00:00'
            var endTime = number == 0 ? referDate[1].split(' ')[1] : '23:59:59'
            if(number == 'today'){
                startDate = equipmentRepair.getDate(0,new Date())
                endDate = equipmentRepair.getDate(0,new Date())
            }
            if(number != 0){
                $('#timeInterval').val(startDate  + ' ' + startTime + '--' + endDate  + ' ' + endTime);
            }
            equipmentRepair.query()
        },
        getDate:function(number,referDate){
            if(referDate){
                referDate = new Date(referDate).Format('yyyy-MM-dd')
            }else {
                referDate = new Date().Format('yyyy-MM-dd')
            }
            var targetDate = new Date(referDate).getTime() + 24*60*60*1000*number
            return new Date(targetDate).Format('yyyy-MM-dd')
        },
        exportAlarm: function () {
            if ($("#dataTable tbody tr td").hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据，无法导出");
                return;
            }
            var params = equipmentRepair.getSearchParams()
            json_ajax("POST", '/clbs/adas/equipmentRepair/export', "json", true, params, function(data){
                if(data.success){
                    layer.confirm('已加入到导出队列，请注意查看导出管理消息提醒', {
                        title: '操作确认',
                        icon: 3,
                        btn: ['确定', '导出管理']
                    }, function () {
                        layer.closeAll();
                    }, function () {
                        layer.closeAll();
                        pagesNav.showExportManager();
                    });
                }
            });
        },

        // 显示批量确认弹窗
        showBatchConfirmModal:function () {
            var subChk = $('#dataTable input[name=subChk]:checked');
            if (subChk.length <= 0) {
                layer.msg('至少勾选一行');
                return;
            }
            checkedPrimaryKeys = []
            subChk.each(function(t){ checkedPrimaryKeys.push($(this).val())})
            var filterData = tableData.filter(function (item) {
                return checkedPrimaryKeys.includes(item.primaryKey)
            })
            // 只允许处理状态为：未确认、误报 的数据
            var onlyOneOrThree = filterData.every(function (item) {
                return item.handleStatus == 0 || item.handleStatus == 3
            })
            if(!onlyOneOrThree){
                layer.msg('只能处理未确认、误报数据');
                return
            }
            var isAllOne = filterData.every(function (item) {
                return item.handleStatus == 0
            })
            var isAllThree = filterData.every(function (item) {
                return item.handleStatus == 3
            })
            // 以上两种状态(未确认、误报)，只允许包含的其中一种类型
            if(!(isAllOne || isAllThree)){
                layer.msg('单次只能处理一种状态的数据');
                return
            }
            $("#batchConfirmModal").modal('show')
            if(filterData){
                var filteredNames = filterData.map(function (item) {
                    return item.monitorName
                })
                $('#batchConfirmInput').val(filteredNames.join(','))
                $('#batchConfirmRadioBox input[type="radio"]').prop('checked',false)
                if(filterData[0].handleStatus == 0){
                    $('#batchConfirmRadio_confirm').prop('checked',true)
                }else if(filterData[0].handleStatus == 3) {
                    $('#batchConfirmRadio_mistake').prop('checked',true)
                }
            }
        },
        // 显示批量完成弹窗
        showBatchFinishModal:function () {
            var subChk = $('#dataTable input[name=subChk]:checked');
            if (subChk.length <= 0) {
                layer.msg('至少勾选一行');
                return;
            }
            checkedPrimaryKeys = []
            subChk.each(function(t){ checkedPrimaryKeys.push($(this).val())})
            var filterData = tableData.filter(function (item) {
                return checkedPrimaryKeys.includes(item.primaryKey)
            })
            // 只允许处理状态为：已确认  的报修数据
            var isAllOne = filterData.every(function (item) {
                return item.handleStatus == 1
            })
            if(!isAllOne){
                layer.msg('只能处理状态为"已确认"的数据');
                return
            }
            $("#batchFinishModal").modal('show')
            if(filterData){
                var filteredNames = filterData.map(function (item) {
                    return item.monitorName
                })
                $('#batchFinishInput').val(filteredNames.join(','))
            }
            var minDate = 0;
            filterData.forEach(function (item) {
                if(item.reportRepairTime){
                    if(minDate < new Date(item.reportRepairTime)){
                        minDate = new Date(item.reportRepairTime)
                    }
                }
            })
            laydate.render({
                value: new Date().Format('yyyy-MM-dd'),
                elem: '#repairDateBatch',
                theme: '#6dcff6',
                max:new Date().Format('yyyy-MM-dd'),
                min:new Date(minDate).Format('yyyy-MM-dd'),
                btns:[ 'now', 'confirm']
            });
        },
        // 显示单条确认弹窗
        showConfirmModal:function (primaryKey) {
            $("#confirmModal").modal('show')
            currentPK = primaryKey
            var data = tableData.find(function (item) {
                return item.primaryKey == primaryKey
            })
            var setting = [
                {
                    title:'所属组织',
                    dataIndex:'orgName'
                },
                {
                    title:'车牌号',
                    dataIndex:'monitorName'
                },
                {
                    title:'设备厂商名',
                    dataIndex:'terminalVendor'
                },
                {
                    title:'设备ID号',
                    dataIndex:'deviceNumber'
                },
                {
                    title:'设备型号',
                    dataIndex:'terminalType'
                },
                {
                    title:'报修日期',
                    dataIndex:'reportRepairTime'
                },
                {
                    title:'故障类型',
                    dataIndex:'faultTypeName'
                },
                {
                    title:'维修日期',
                    dataIndex: 'repairDate'
                },
                {
                    title:'选择状态',
                    dataIndex: 'handleStatus',
                    render:function (data) {
                        if(data ==3){
                            return '<div style="position: relative;top: 9px">' + '<input id="confirmRadio_confirm" type="radio"  value="0" name="confirmRadio"/><label for="confirmRadio_confirm" style="margin: 0 23px 0 6px;">已确认</label>\n' +
                                '<input id="confirmRadio_mistake" type="radio" checked value="1" name="confirmRadio"/><label for="confirmRadio_mistake" style="margin: 0 23px 0 6px;">误报</label>' +
                                '</div>'
                        }else {
                            return '<div style="position: relative;top: 9px">' + '<input id="confirmRadio_confirm" type="radio" checked value="0" name="confirmRadio"/><label for="confirmRadio_confirm" style="margin: 0 23px 0 6px;">已确认</label>\n' +
                                '<input id="confirmRadio_mistake" type="radio" value="1" name="confirmRadio"/><label for="confirmRadio_mistake" style="margin: 0 23px 0 6px;">误报</label>' +
                                '</div>'
                        }
                    }
                },
                {
                    title:'备注',
                    dataIndex: 'remark',
                    render:function (data) {
                        //data.replace(/</g, "&lt")
                        if(data){
                            console.log(data)
                            return '<input id="confirmModal_remark" value=\'' + data + '\' type="text" class="Inlinesearch form-control"  placeholder="请输入备注" style="width:100%;padding-right: 30px;">'
                        }else {
                            return '<input id="confirmModal_remark" type="text" class="Inlinesearch form-control"  placeholder="请输入备注" style="width:100%;padding-right: 30px;">'
                        }
                    }
                },
            ]
            var html = equipmentRepair.modalRender(data,setting)
            $('#confirmBody').html(html)
            $('#confirmModal_remark').on('input propertychange',function (e) {
                $(this).val($(this).val().substring(0,50))
                equipmentRepair.validateInput($(this))
            })
        },
        // 显示单条完成弹窗
        showFinishModal:function (primaryKey) {
            $("#finishModal").modal('show')
            currentPK = primaryKey
            var data = tableData.find(function (item) {
                return item.primaryKey == primaryKey
            })
            var setting = [
                {
                    title:'所属组织',
                    dataIndex:'orgName'
                },
                {
                    title:'车牌号',
                    dataIndex:'monitorName'
                },
                {
                    title:'设备厂商名',
                    dataIndex:'terminalVendor'
                },
                {
                    title:'设备ID号',
                    dataIndex:'deviceNumber'
                },
                {
                    title:'设备型号',
                    dataIndex:'terminalType'
                },
                {
                    title:'报修日期',
                    dataIndex:'reportRepairTime'
                },
                {
                    title:'故障类型',
                    dataIndex:'faultTypeName'
                },
                {
                    title:'维修日期',
                    dataIndex: null,
                    render:function (data) {
                        return '<input id="repairDate" readonly="readonly"  type="text" class="form-control layer-date laydate-icon" autocomplete="off" style="cursor: pointer; background-color: rgb(250, 250, 250);">'
                    }
                },
                {
                    title:'状态',
                    dataIndex: 'handleStatus',
                    render:function (data) {
                        switch (data) {
                            case -1:
                                data = '全部';
                                break;
                            case 0:
                                data = '未确认';
                                break;
                            case 1:
                                data = '已确认';
                                break;
                            case 2:
                                data = '已完成';
                                break;
                            case 3:
                                data = '误报';
                                break;
                            default :
                                data = '-';
                        }
                        return '<input type="text" class="form-control" value="' + data +'  " readonly="readonly">'
                    }
                },
                {
                    title:'备注',
                    dataIndex: 'remark',
                    render:function (data) {
                        if(data){
                            return '<input id="finishModal_remark" type="text" class="Inlinesearch form-control" value=' + data +'  placeholder="请输入备注" style="width:100%;padding-right: 30px;">'
                        }else {
                            return '<input id="finishModal_remark" type="text" class="Inlinesearch form-control" value=""  placeholder="请输入备注" style="width:100%;padding-right: 30px;">'
                        }
                    }
                },
            ]
            var html = equipmentRepair.modalRender(data,setting)
            $('#finishBody').html(html)
            laydate.render({
                value: new Date().Format('yyyy-MM-dd'),
                elem: '#repairDate',
                theme: '#6dcff6',
                max: new Date().Format('yyyy-MM-dd'),
                min: new Date(new Date(data.reportRepairTime)).Format('yyyy-MM-dd'),
                btns:['now','confirm']
            });
            $('#finishModal_remark').on('input',function (e) {
                $(this).val($(this).val().substring(0,50))
                equipmentRepair.validateInput($(this))
            })
        },
        modalRender:function (data,setting) {
            var html = ''
            setting.forEach(function (item) {
                var dataHtml = ''
                if(typeof item.render == 'function'){
                    dataHtml = item.render(data[item.dataIndex],item)
                    // dataHtml = '<input  name="deviceNumber" type="text"  class="form-control" value="' + dataHtml + '" readonly="readonly">'
                }else {
                    var ss = data[item.dataIndex] || '-'
                    dataHtml = '<input  type="text"  class="form-control" value="' + ss + '" readonly="readonly">'
                }
                html +=
                    '<div>' +
                        '<div class="col-md-6" style="padding: 0;margin-bottom: 11px;">' +
                            '<label class="control-label col-md-4 text-right" style="padding: 0;padding-right: 12px;">' + item.title +': </label>' +
                                '<div class="col-md-8" style="position: relative;top: -9px;">' +
                                    dataHtml +
                                '</div>' +
                        '</div>' +
                    '</div>'
            })
            return html
        },
        // 批量确认提交
        submitBatchConfirm:function () {
            var btn = $(this)
            btn.prop('disabled','disabled')
            setTimeout(function () {
                btn.prop('disabled','')
            },5000)
            var sendData = function () {
                var param = {
                    primaryKeys: checkedPrimaryKeys.join(','),
                    confirmStatus: $('#batchConfirmRadioBox input[type="radio"]:checked').val()
                }
                json_ajax("POST", '/clbs/adas/equipmentRepair/confirm/batch', "json", true, param, function(data){
                    if(data.success){
                        layer.msg('提交成功');
                        $('#batchConfirmModal').modal('hide');
                        myTable.requestData();
                    }else{
                        layer.msg('提交失败');
                    }
                });
            }
            if($('#batchConfirmRadio_confirm').prop('checked')){
                layer.confirm('确认提交？提交后将数据上报政府平台', {
                    title: '确认提示',
                    btn: ['确定', '取消'],
                    icon: 3,
                }, function () {
                    sendData()
                });
            }else {
                sendData()
            }
        },
        // 批量完成提交
        submitBatchFinish:function () {
            var btn = $(this)
            btn.prop('disabled','disabled')
            setTimeout(function () {
                btn.prop('disabled','')
            },5000)
            var sendData = function () {
                var param = {
                    primaryKeys: checkedPrimaryKeys.join(','),
                    repairDate: $('#repairDateBatch').val()
                }
                json_ajax("POST", '/clbs/adas/equipmentRepair/finish/batch', "json", true, param, function(data){
                    if(data.success){
                        layer.msg('提交成功');
                        $('#batchFinishModal').modal('hide');
                        myTable.requestData();
                    }else{
                        layer.msg('提交失败');
                    }
                });
            }
            layer.confirm('确认提交？提交后将数据上报政府平台', {
                title: '确认提示',
                btn: ['确定', '取消'],
                icon: 3,
            }, function () {
                sendData()
            });
        },
        // 单条确认提交
        submitConfirm:function () {
            if($('#confirmModal input[type="radio"]:checked').length == 0){
                layer.msg('请选择状态')
                return
            }
            if(!equipmentRepair.validateInput($('#confirmModal_remark'))){
                return
            }
            var btn = $(this)
            btn.prop('disabled','disabled')
            setTimeout(function () {
                btn.prop('disabled','')
            },5000)
            var sendData = function () {
                var param = {
                    confirmStatus:$('#confirmModal input[type="radio"]:checked').val(), //状态
                    remark: $('#confirmModal_remark').val(), //备注
                    primaryKey: currentPK, //主键
                }
                json_ajax("POST", '/clbs/adas/equipmentRepair/confirm', "json", true, param, function(data){
                    if(data.success){
                        layer.msg('提交成功');
                        $('#confirmModal').modal('hide');
                        myTable.requestData();
                    }else{
                        layer.msg('提交失败');
                    }
                });
            }
            if($('#confirmRadio_confirm').prop('checked')){
                layer.confirm('确认提交？提交后将数据上报政府平台', {
                    title: '确认提示',
                    btn: ['确定', '取消'],
                    icon: 3,
                }, function () {
                    sendData()
                });
            }else {
                sendData()
            }
        },
        // 单条完成提交
        submitFinish:function () {
            if(!equipmentRepair.validateInput($('#finishModal_remark'))){
                return
            }
            var btn = $(this)
            btn.prop('disabled','disabled')
            setTimeout(function () {
                btn.prop('disabled','')
            },5000)
            var sendData = function () {
                var param = {
                    repairDate:$('#repairDate').val(), // 维修日期
                    remark:$('#finishModal_remark').val(),  // 备注
                    primaryKey:currentPK //主键
                }
                json_ajax("POST", '/clbs/adas/equipmentRepair/finish', "json", true, param, function(data){
                    if(data.success){
                        layer.msg('提交成功');
                        $('#finishModal').modal('hide');
                        myTable.requestData();
                    }else{
                        layer.msg('提交失败');
                    }
                });
            }
            layer.confirm('确认提交？提交后将数据上报政府平台', {
                title: '确认提示',
                btn: ['确定', '取消'],
                icon: 3,
            }, function () {
                sendData()
            });
        },

        validateInput:function(simpleQueryParam) {
            var regularText = /^[^`\s\^\*;'"\\|,/<>\?]*$/
            if (!regularText.test(simpleQueryParam.val())) {
                if (!simpleQueryParam.hasClass('checkValidate')) {
                    // simpleQueryParam.after("<p " +
                    //     "style='position: absolute;top: 37px;margin: 0;background: #fff;" +
                    //     "color: #cf0000;font-weight: normal;right: 15px;width: 279px;border: 1px solid #e4e4e4;text-align: center;" +
                    //     "height: 30px;line-height: 30px;box-shadow: 0px 2px 2px #e2e2e2;'>" +
                    //     "请不要输入空格、换行和`^*;\'\\\"|, /<>?" +
                    //     "</p>")
                    simpleQueryParam.after("<p " +
                        "style='position: absolute;margin: 0;background: #b94a48;color: #fff;font-weight: normal;width: 232px;text-align: center;height: 25px;line-height: 25px;font-weight: bold;font-size: 12px;'>" +
                        "请不要输入空格、换行和`^*;\'\\\"|, /<>?" +
                        "</p>")
                    simpleQueryParam.addClass('checkValidate')
                }
                return false
            } else {
                simpleQueryParam.next().remove()
                simpleQueryParam.removeClass('checkValidate')
                return true
            }
        },

        // 搜索
        search:function () {
            myTable.requestData()
        },
        // 查询
        query:function () {
            if(!equipmentRepair.validates()) return
            myTable.requestData()
        },
        // 获取查询参数
        getSearchParams:function(){
            var reportStartTime,reportEndTime,repairStartTime,repairEndTime,simpleQueryParam = $('#simpleQueryParam').val(),faultType = $('#deviceType').val(),handleStatus = $('#state').val()
            var timeRange = $('#timeInterval').val()
            if(timeRange){
                reportStartTime = timeRange.split('--')[0]
                reportEndTime = timeRange.split('--')[1]
            }
            var timeRange2 = $('#timeInterval2').val()
            if(timeRange2){
                repairStartTime = timeRange2.split('--')[0]
                repairEndTime = timeRange2.split('--')[1]
            }
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            var nodes = treeObj.getCheckedNodes(true);
            return {
                simpleQueryParam:simpleQueryParam,
                reportStartTime:reportStartTime,
                reportEndTime:reportEndTime,
                orgId:nodes[0] ? nodes[0].uuid : $('#userGroupId').val(),
                faultType:faultType, // 故障类型  -1或空:全部  0:主存储器异常  1:备用存储器异常  2:卫星信号异常  3:通信信息号异常  4:备用电池欠压  5:备用电池失效  6:IC卡从业资格证模块故障
                handleStatus:handleStatus, // 故障处理状态 空：全部 -1：全部 0:未确认 1:已确认 2:已完成 3:误报
                repairStartTime:repairStartTime,
                repairEndTime:repairEndTime,
            }
        },

    }

    $(function () {
        //初始化页面
        equipmentRepair.init();
        $('input').inputClear();
        $('#timeInterval').dateRangePicker({
            dateLimit: 60,
        });
        var today = new Date().Format('yyyy-MM-dd') + ' ' + '23:59:59'
        var yesterday = new Date(new Date() - 24*60*60*1000).Format('yyyy-MM-dd') + ' ' + '00:00:00'
        $('#timeInterval').val(yesterday + '--' + today)

        $('#timeInterval2').one('click',function () {
            $('#timeInterval2').dateRangePicker({
                dateLimit: 60,
                isShowHMS: false,
                clearBtn: true,
            });
        })
        equipmentRepair.getTable('#dataTable');
        // 导出
        $('#exportAlarm').on('click',equipmentRepair.exportAlarm)

        //组织下拉显示
        $("#groupSelect").bind("click", showMenuContent);
        //刷新
        $("#refreshTable").bind("click", equipmentRepair.refreshTable);
        // 搜索
        $('#search_button').on('click',equipmentRepair.search)
        // 查询
        $('#query_button').on('click',equipmentRepair.query)

        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                var nodes = treeObj.getCheckedNodes(true);
                treeObj.checkNode( nodes[0], false, false);
            }
        });

        /**
         * 监控对象树模糊查询
         */
        $("#groupSelect").on('input propertychange', function (value) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.checkAllNodes(false);
            search_ztree('treeDemo', 'groupSelect', 'group');
        });

        // 全选
        $("#checkAll").on('change', function () {
            $("input[name='subChk']:not(:disabled)").prop("checked", this.checked)
        })
        //
        $('#batchConfirmBtn').on('click',equipmentRepair.showBatchConfirmModal)
        $('#batchFinishBtn').on('click',equipmentRepair.showBatchFinishModal)
        $('#batchConfirmSubmitBtn').on('click',equipmentRepair.submitBatchConfirm)
        $('#batchFinishSubmitBtn').on('click',equipmentRepair.submitBatchFinish)

        $('#confirmBtn').on('click',equipmentRepair.showConfirmModal)
        $('#finishBtn').on('click',equipmentRepair.showFinishModal)
        $('#confirmSubmitBtn').on('click',equipmentRepair.submitConfirm)
        $('#finishSubmitBtn').on('click',equipmentRepair.submitFinish)
        $("#confirmModal, #finishModal,#batchFinishModal,#batchConfirmModal").on('hide.bs.modal',function (e) {
            $('#confirmSubmitBtn').prop('disabled','')
            $('#finishSubmitBtn').prop('disabled','')
            $('#batchConfirmSubmitBtn').prop('disabled','')
            $('#batchFinishSubmitBtn').prop('disabled','')
        })
    })
})(window, $)