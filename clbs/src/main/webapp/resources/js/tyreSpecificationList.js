(function (window, $) {
    //显示隐藏列
    var menu_text = "";
    var table = $("#dataTable tr th:gt(1)");
    //单选
    var subChk = $("input[name='subChk']");
    vehicleTyreSpecification = {
        //初始化
        init: function () {
            menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" />" + table[i].innerHTML + "</label></li>"
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
            var columns = [{
                //第一列，用来显示序号
                "data": null,
                "class": "text-center"
            }, {
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
                        result += '<button href="' + editUrlPath + '" data-target="#commonSmWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                        //删除按钮
                        result += '<button type="button" onclick="myTable.deleteItem(\'' + row.id + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
                        return result;
                    }
                }, {
                    "data": "tireType",
                    "class": "text-center"
                }, {
                    "data": "sizeName",
                    "class": "text-center",
                    render: function (data) {
                        return html2Escape(data)
                    }
                }, {
                    "data": "rollingRadius",
                    "class": "text-center"
                }, {
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
                listUrl: '/clbs/v/meleMonitor/tyreSpecification/list',
                editUrl: '/clbs/v/meleMonitor/tyreSpecification/edit_',
                deleteUrl: '/clbs/v/meleMonitor/tyreSpecification/delete_',
                deletemoreUrl: '/clbs/v/meleMonitor/tyreSpecification/deletemore',
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
        //全选
        checkAllClick: function () {
            $("input[name='subChk']").prop("checked", this.checked);
        },
        subChkClick: function () {
            $("#checkAll").prop("checked", subChk.length == subChk.filter(":checked").length ? true : false);
        },
        //批量删除
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
            layer.confirm(publicDelete, {btn : ["确定", "取消"],icon: 3,title: "操作确认"}, function () {
	        	json_ajax("POST", myTable.deletemoreUrl, "json", true, {'deltems':checkedList.toString()},vehicleTyreSpecification.deleteMoreCallback);
	        });
            //myTable.deleteItems({'deltems': checkedList.toString()});
        },
        // 批量删除回调
	    deleteMoreCallback: function(data){
	    	if(data.success){
	    		if (data.msg != null && data.msg != "") {
					layer.confirm(data.msg, {btn : ["确定"]}, function () {
						layer.closeAll();
						vehicleTyreSpecification.refreshTable();
					});    	
		    	} else {
		    		layer.closeAll();
		    		vehicleTyreSpecification.refreshTable();
		    	}
	    	}else{
	    		layer.msg(data.msg,{move:false});
	    	}
	    	
	    },
        //刷新列表
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            myTable.requestData();
        },
    }
    $(function () {
        $('input').inputClear();
        vehicleTyreSpecification.init();
        //全选
        $("#checkAll").bind("click", vehicleTyreSpecification.checkAllClick);
        subChk.bind("click", vehicleTyreSpecification.subChkClick);
        //批量删除
        $("#del_model").bind("click", vehicleTyreSpecification.delModelClick);
        $("#refreshTable").on("click", vehicleTyreSpecification.refreshTable);
    })
})(window, $)