(function(window,$){

    //显示隐藏列
    var menu_text = $("#Ul-menu-text");
    var menu_text2 = $("#Ul-menu-text2");
    var table = $("#dataTable tr th:gt(0)");
    var table2 = $("#dataTable2 tr th:gt(0)");

    ManagementSkills = {
        setColumn: function (){
            var menu_text = "";
            menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(1) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for(var i=1; i<table.length;i++){
                menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-text").html(menu_text);
        },
        init: function(){
            ManagementSkills.setColumn(table, menu_text);
            //表格列定义
            var columnDefs = [{
                //第一列,用来显示序号
                "searchable" : false,
                "orderable" : false,
                "targets" : 0
            }];
            var columns = [
                {
                    //第一列,用来显示序号
                    "data":null,
                    "class":"text-center"
                },
                {
                    "data": null,
                    "class": "text-center", //最后一列，操作按钮
                    render: function (data, type, row, meta) {
                        var editUrlPath = myTable.editUrl+'?id='+row.id; //修改地址
                        var result = '';
                        //修改按钮
                        result += '<button href="' + editUrlPath + '" data-target="#commonSmWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&nbsp;';
                        //删除按钮
                        if (row.id === 'default') {
                            result += '<button disabled type="button"  class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>删除</button>&nbsp;';
                        } else {
                            result += '<button type="button" onclick="ManagementSkills.deleteItemAndCheckBond(\'' + row.id + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
                        }
                        return result;
                    }
                },
                {
                    "data":"name",
                    "class":"text-center"
                },
                {
                    "data": "remark",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/talkback/basicinfo/skill/getSkillsCategories',
                editUrl: '/clbs/talkback/basicinfo/skill/editSkillsCategories',
                deleteUrl:'/clbs/talkback/basicinfo/skill/deleteSkillsCategories',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                drawCallbackFun: function () {
                    var api = myTable.dataTable;
                    var startIndex = api.context[0]._iDisplayStart;//获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                    });
                },
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            //表格初始化
            myTable.init();
            //显示隐藏列
            $('#Ul-menu-text .toggle-vis').on('change', function (e) {
                e.preventDefault();
                var column = myTable.dataTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(this).parent().parent().parent().parent().addClass("open");
            });

            ManagementSkills.modelTableInit();
        },
        //技能列表
        modelTableInit:function(){
            menu_text2 = "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(1) +"\" disabled />"+ table2[0].innerHTML +"</label></li>";
            for(var i = 1; i < table2.length; i++){
                menu_text2 += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i+1) +"\" />"+ table2[i].innerHTML +"</label></li>"
            };
            $("#Ul-menu-text2").html(menu_text2);

            //表格列定义
            var skillcolumnDefs = [{
                //第一列,用来显示序号
                "searchable" : false,
                "orderable" : false,
                "targets" : 0
            }];
            var skillColumns = [
                {
                    //第一列,用来显示序号
                    "data":null,
                    "class":"text-center"
                },
                {
                    "data": null,
                    "class": "text-center", //最后一列，操作按钮
                    render: function (data, type, row, meta) {
                        // var editUrlPath = modelTable.editUrl +'?id='+row.id +'&remark='+row.remark+'&categoriesId='+row.categoriesId+'&name='+row.name;
                        var editUrlPath = modelTable.editUrl+'?id='+row.id; //修改地址
                        var result = '';
                        //修改按钮
                        result += '<button href="' + editUrlPath + '" data-target="#commonSmWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&nbsp;';
                        //删除按钮
                        if (row.id === 'default') {
                            result += '<button disabled type="button"  class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>删除</button>&nbsp;';
                        } else {
                            result += '<button type="button" onclick="ManagementSkills.deleteItemAndSkill(\'' + row.id + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
                        }
                        return result;
                    }
                },
                {
                    "data":"name",
                    "class":"text-center"
                },
                {
                    "data": "categoriesName",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                },{
                    "data": "remark",
                    "class":"text-center"
                }];
            //ajax参数
            var ajaxDataParamFun2 = function (d) {
                d.simpleQueryParam = $("#simpleQueryParam2").val(); //模糊查询
            };
            //表格setting
            var subSetting = {
                listUrl: '/clbs/talkback/basicinfo/skill/getSkills',
                editUrl: '/clbs/talkback/basicinfo/skill/editSkill',
                deleteUrl:'/clbs/talkback/basicinfo/skill/deleteSkill',
                columnDefs: skillcolumnDefs, //表格列定义
                columns: skillColumns, //表格列
                dataTableDiv: 'dataTable2', //表格
                ajaxDataParamFun: ajaxDataParamFun2, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true
            };
            //创建表格
            modelTable = new TG_Tabel.createNew(subSetting);
            //表格初始化
            modelTable.init();
        },

        // 删除技能类别操作
        deleteItemAndCheckBond: function (id) {
            var url = "/clbs/talkback/basicinfo/skill/deleteSkillsCategories";
            var data = {"id": id};
            layer.confirm(publicDelete, {btn: ["确定", "取消"], icon: 3, title: "操作确认"}, function () {
                json_ajax("POST", url, "json", true, data, ManagementSkills.deleteCallBack);
            });
        },
        // 删除操作回调
        deleteCallBack: function (data) {
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
        // 删除技能列表操作
        deleteItemAndSkill: function (id) {
            var url = "/clbs/talkback/basicinfo/skill/deleteSkill";
            var data = {"id": id};
            layer.confirm(publicDelete, {btn: ["确定", "取消"], icon: 3, title: "操作确认"}, function () {
                json_ajax("POST", url, "json", true, data, ManagementSkills.SkilldeleteCallBack);
            });
        },
        //删除技能列表回调
        SkilldeleteCallBack: function (data) {
            if (data.success) {
                layer.closeAll();
                modelTable.refresh();
            } else {
                if (data.msg != null) {
                    layer.msg(data.msg);
                    return;
                }
                layer.msg(publicError);
            }
        },
        //刷新列表
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            myTable.requestData();
        },
        refreshTable2:function () {
            $("#simpleQueryParam2").val("");
            modelTable.requestData();
        }
    };
    $(function(){
        ManagementSkills.init();
        $('input').inputClear();
        $("#refreshTable").on("click", ManagementSkills.refreshTable);
        $("#refreshTable2").on("click",ManagementSkills.refreshTable2);

    })
})(window,$);