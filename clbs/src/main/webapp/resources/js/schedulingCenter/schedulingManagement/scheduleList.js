var myTable;
var scheduleList;
var editStatus = 1;//获取修改状态,默认为未开始
(function(window,$){
    var menu_text = "";
    var table = $("#dataTable tr th:gt(0)");

    scheduleList = {
        init:function(){
            scheduleList.tableLayout();
            scheduleList.initTable();
        },
        tableLayout:function(){
            menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(1) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            };
            $("#Ul-menu-text").html(menu_text);
        },
        initTable:function(){
            var columnDefs = [ {
                //第一列，用来显示序号333
                "searchable" : false,
                "orderable" : false,
                "targets" : 0
            }];
            var columns = [
                {
                    //第一列，用来显示序号
                    "data" : null,
                    "class" : "text-center"
                },
                {
                    "data" : null,
                    "class" : "text-center",
                    render : function(data, type, row, meta) {
                        var status = row.status,//状态
                            isMandatoryTermination = row.isMandatoryTermination;//强制结束

                        var editUrlPath = myTable.editUrl + row.id; //修改地址
                        var detailUrlPath = myTable.detailUrl + row.id; //详情地址

                        var result = '';

                        if((status==1 || status==3) && isMandatoryTermination==0){//未开始，执行中
                            result += '<button onclick="scheduleList.changeSchedule(\''+status+'\')" href="'+editUrlPath+'" data-target="#commonLgWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&nbsp;';
                        }
                        if(status==3 && isMandatoryTermination==0){//执行中
                            result += '<button type="button" onclick="scheduleList.endSchedule(\''+row.id+'\')" class="editBtn editBtn-info"><i class="fa fa-end"></i>结束</button>&nbsp;';
                        }
                        if(status==1){//未开始
                            result += '<button type="button" onclick="scheduleList.deleteSchedule(\''+row.id+'\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>&nbsp;';
                        }

                        if(status==3 && isMandatoryTermination == 1){//强制结束修改,结束按钮置灰
                            result += '<button  type="button" class="editBtn btn-default editBtn-info"><i class="fa fa-ban"></i>修改</button>&nbsp;';
                            result += '<button type="button" class="editBtn btn-default editBtn-info"><i class="fa fa-ban"></i>结束</button>&nbsp;';
                        }

                        result += '<button href="'+detailUrlPath+'" data-target="#commonLgWin" data-toggle="modal" type="button" class=" editBtn editBtn-info"><i class="fa fa-sun-o"></i>详情</button>&nbsp;';

                        return result;
                    }
                },
                {
                    "data" : "scheduledName",
                    "class" : "text-center"
                },
                {
                    "data" : "startDateStr",
                    "class" : "text-center"
                },{
                    "data" : "endDateStr",
                    "class" : "text-center"
                },
                {
                    "data" : "status",
                    "class" : "text-center",
                    render: function(data, type, row){
                        switch (data){
                            case 1:
                                return '未开始';
                                break;
                            case 2:
                                return '已结束';
                                break;
                            case 3:
                                return '执行中';
                                break;
                            default:
                                break;
                        }
                    }
                },{
                    "data" : "groupName",
                    "class" : "text-center"
                },{
                    "data" : "createDataUsername",
                    "class" : "text-center"
                },{
                    "data" : "createDataTime",
                    "class" : "text-center"
                },
                {
                    "data" : "remark",
                    "class" : "text-center",
                },
            ];

            //ajax参数
            var ajaxDataParamFun = function(d) {
               d.simpleQueryParam = $('#simpleQueryParam').val();
            };
            //表格setting
            var setting = {
                listUrl : "/clbs/m/schedulingCenter/schedulingManagement/list",
                detailUrl: '/clbs/m/schedulingCenter/schedulingManagement/getSchedulingDetailPage/',
                editUrl: '/clbs/m/schedulingCenter/schedulingManagement/getSchedulingUpdatePage/',
                deletemoreUrl: '/clbs/m/schedulingCenter/schedulingManagement/deleteScheduling',
                columnDefs : columnDefs, //表格列定义
                columns : columns, //表格列
                dataTableDiv : 'dataTable', //表格
                ajaxDataParamFun : ajaxDataParamFun, //ajax参数
                pageable : true, //是否分页
                showIndexColumn : true, //是否显示第一列的索引列
                enabledChange : true
            };
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
        },
        //排班修改
        changeSchedule: function(status){
            editStatus = status;
        },
        //结束排班
        endSchedule: function(id){
            tg_confirmDialog('操作确认', '结束后,该排班结束日期将自动设为今天.',function(){
                var paramer = {
                    scheduledInfoId: id,
                }
                json_ajax("POST", '/clbs/m/schedulingCenter/schedulingManagement/mandatoryTerminationScheduling', "json", true, paramer, scheduleList.endScheduleCallBack);
            });
        },
        endScheduleCallBack: function(data){
            if(data.success){
                myTable.requestData();
            }else{
                layer.msg(data.msg);
            }
        },
        //删除排班
        deleteSchedule: function(id){
            var paramer = {
                "scheduledInfoId": id,
            }

            myTable.deleteItems(paramer);
        },
        // 刷新table列表
        refreshTable: function () {
            $('#simpleQueryParam').val("");
            myTable.requestData();
        },
    }

    $(function(){
        scheduleList.init();
        $('#refreshTable').on('click',scheduleList.refreshTable);
    })
})(window,$)