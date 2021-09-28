(function(window,$){
    //显示隐藏列
    var menu_text = "";
    var table = $("#dataTable tr th:gt(1)");
    //单选
    var subChk = $("input[name='subChk']");
    talkbackType = {
        //初始化
        init: function(){
            menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(2) +"\" disabled />"+ table[0].innerHTML +"</label></li>"
            for(var i = 1; i < table.length; i++){
                menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(i+2) +"\" />"+ table[i].innerHTML +"</label></li>"
            };
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
            }, {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    var result = '';
                    if(row.id == 'default6'){

                    }else {
                        result += '<input  type="checkbox" name="subChk"  value="' + row.id + '" />';
                    }
                    return result;
                }
            },{
                "data": null,
                "class": "text-center", //最后一列，操作按钮
                render: function (data, type, row, meta) {
                    //var editUrlPath = myTable.editUrl + row.id + ".gsp"; //修改地址
                    var editUrlPath = myTable.editUrl + row.id;
                    var result = '';
                    if(row.id == 'default6'){
                        //禁止修改按钮
                        result += '<button disabled data-target="#commonWin" data-toggle="modal" type="button" class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>修改</button>&nbsp;';
                        //禁止删除按钮
                        result += '<button disabled type="button" class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>删除</button>';
                    }else{
                        //修改按钮
                        result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&nbsp;';
                        //删除按钮
                        result += '<button type="button" onclick="talkbackType.deleteItemAndline(\'' + row.id + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
                    }
                    return result;
                }
            }, {
                "data": "intercomName",
                "class": "text-center"
            },  {
                "data": "modelId",
                "class": "text-center"
            }, {
                "data": "videoAbility",
                "class": "text-center",
                render: function (data, type, row, meta){
                    if(data == 1){
                        return "支持"
                    }else if(data == 0){
                        return "不支持"
                    }
                }
            }, {
                "data": "videoFuncEnable",
                "class": "text-center",
                render: function (data, type, row, meta){
                    if(data == 1){
                        return "支持"
                    }else if(data == 0){
                        return "不支持"
                    }
                }
            }, {
                "data": "chanlsNum",
                "class": "text-center"
            }, {
                "data": "knobNum",
                "class": "text-center"
            }, {
                "data": "maxGroupNum",
                "class": "text-center"
            }, {
                "data": "maxFriendNum",
                "class": "text-center"
            },{
                "data": "interceptEnable",
                "class": "text-center",
                render: function (data, type, row, meta){
                    if(data == 1){
                        return "支持"
                    }else if(data == 0){
                        return "不支持"
                    }
                }
            },
            //     {
            //     "data": "tempGroupEnable",
            //     "class": "text-center",
            //     render: function (data, type, row, meta){
            //         if(data == 1){
            //             return "支持"
            //         }else if(data == 0){
            //             return "不支持"
            //         }
            //     }
            // },
                {
                "data": "sensorAbility",
                "class": "text-center",
                render: function (data, type, row, meta){
                    if(data == 1){
                        return "支持"
                    }else if(data == 0){
                        return "不支持"
                    }
                }
            },{
                "data": "videoCallEnable",
                "class": "text-center",
                render: function (data, type, row, meta){
                    if(data == 1){
                        return "支持"
                    }else if(data == 0){
                        return "不支持"
                    }
                }
            },{
                "data": "videoConferenceEnable",
                "class": "text-center",
                render: function (data, type, row, meta){
                    if(data == 1){
                        return "支持"
                    }else if(data == 0){
                        return "不支持"
                    }
                }
            }];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/talkback/intercomplatform/intercommodel/list',
                editUrl: "/clbs/talkback/intercomplatform/intercommodel/edit_",
                deleteUrl: '/clbs/talkback/intercomplatform/intercommodel/deleteIntercomModel',
                deletemoreUrl: '/clbs/talkback/intercomplatform/intercommodel/deleteIntercomModels',
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
        },
        //删除
        deleteItemAndline: function(id){
            var url = "/clbs/talkback/intercomplatform/intercommodel/deleteIntercomModel";
            var data = {"id": id};
            layer.confirm(publicDelete, {btn: ["确定", "取消"], icon: 3, title: "操作确认"}, function () {
                json_ajax("POST", url, "json", true, data, talkbackType.deleteCallBack);
            });
        },
        //删除操作回调
        deleteCallBack: function (data){
            if (data.success) {
                layer.closeAll();
                myTable.refresh();
            } else {
                if (data.msg != null) {
                    layer.msg(data.msg);
                    return;
                }
                layer.msg(publicError);
            }
        },
        //全选
        checkAllClick: function(){
            $("input[name='subChk']").prop("checked", this.checked);
        },
        subChkClick: function(){
            $("#checkAll").prop("checked", subChk.length == subChk.filter(":checked").length ? true : false);
        },
        //批量删除
        delModelClick: function(){
            //判断是否至少选择一项
            var chechedNum = $("input[name='subChk']:checked").length;
            if (chechedNum == 0) {
                layer.msg(selectItem,{move:false});
                return
            }
            var checkedList = new Array();
            $("input[name='subChk']:checked").each(function () {
                checkedList.push($(this).val());
            });
            myTable.deleteItems({'ids': checkedList.toString()});
        },
        //刷新列表
        refreshTable:function(){
            $("#simpleQueryParam").val("");
            myTable.requestData();
        },
    }
    $(function(){
        $('input').inputClear();
        talkbackType.init();
        //全选
        $("#checkAll").bind("click",talkbackType.checkAllClick);
        subChk.bind("click",talkbackType.subChkClick);
        //批量删除
        $("#del_model").bind("click",talkbackType.delModelClick);
        $("#refreshTable").on("click",talkbackType.refreshTable);
    })
})(window,$);