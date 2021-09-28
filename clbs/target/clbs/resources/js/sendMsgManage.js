var sendMsgManage;
(function(window,$){
	//显示隐藏列
	var menu_text = "";
	var table = $("#dataTable tr th:gt(1)");
    //单选
    var subChk = $("input[name='subChk']");

    sendMsgManage = {
		//初始化
		init: function(){
			menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(2) +"\" disabled />"+ table[0].innerHTML +"</label></li>"
			for(var i = 1; i < table.length; i++){
			    menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(i+2) +"\" />"+ table[i].innerHTML +"</label></li>"
			};
			$("#Ul-menu-text").html(menu_text);

            sendMsgManage.tableInit();
		},
		tableInit: function(){
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
                        result += '<button type="button" onclick="sendMsgManage.deleteItem(\'' + row.id + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
                        return result;
                    }
                }, {
                    "data": "content",
                    "class": "text-center",
                    render:function(data){
                        return html2Escape(data)
                    }
                }, {
                    "data": "status",
                    "class": "text-center",
                    render:function(data){
                        if(data == 1){
                            return '启用';
                        }else {
                            return '停用';
                        }
                    }
                }, {
                    "data": "createDataTime",
                    "class": "text-center"
                }, {
                    "data": "updateDataTime",
                    "class": "text-center"
                }, {
                    "data": "updateDataUsername",
                    "class": "text-center"
                }];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/lkyw/message/template/templateList',
                editUrl: '/clbs/lkyw/message/template/templateEditPage_',
                /*deleteUrl: '/clbs/v/sensorConfig/vehiclePeripheral/delete_',*/
                deletemoreUrl: '/clbs/lkyw/message/template/deleteTemplate',
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
        //单个删除
        deleteItem: function(id){
            myTable.deleteItems({'ids': id.toString()});
        },
	  	//刷新列表
        refreshTable:function(){
        	$("#simpleQueryParam").val("");
            myTable.requestData();
        },
        //导出
        exportFun:function(){
            if(getRecordsNum() > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var fuzzyParam = $('#simpleQueryParam').val();
            var url = '/clbs/lkyw/message/template/exportTemplate?fuzzyParam='+fuzzyParam;
            window.location.href = url;
        }
	}
	$(function(){
		$('input').inputClear();
		sendMsgManage.init();
		//全选
	    $("#checkAll").bind("click",sendMsgManage.checkAllClick);
	    subChk.bind("click",sendMsgManage.subChkClick);
	    //批量删除
	    $("#del_model").bind("click", sendMsgManage.delModelClick);
	    $("#refreshTable").on("click", sendMsgManage.refreshTable);
	    $('#exportId').on('click', sendMsgManage.exportFun);
	})
})(window,$)