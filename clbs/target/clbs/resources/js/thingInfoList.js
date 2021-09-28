(function(window,$){
    //显示隐藏列
    var menu_text = "";
    var table = $("#dataTable tr th:gt(1)");
    //单选
    var subChk = $("input[name='subChk']");
    var selectGroupId = '';
    var selectAssignId = '';
    var selectTreeId = '';
    var selectTreepId = "";
    var selectTreeType = '';
    itemInformation = {
        //初始化
        init: function(){
            menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(2) +"\" disabled />"+ table[0].innerHTML +"</label></li>"
            for(var i = 1; i < table.length; i++){
                menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(i+2) +"\" />"+ table[i].innerHTML +"</label></li>"
            };
            $("#Ul-menu-text").html(menu_text);
            var treeSetting = {
                async : {
                    url : "/clbs/m/basicinfo/enterprise/assignment/assignmentTree",
                    tyoe : "post",
                    enable : true,
                    autoParam : [ "id" ],
                    dataType : "json",
                    otherParam : { // 是否可选  Organization
                        "isOrg" : "1"
                    },
                    dataFilter: itemInformation.ajaxDataFilter
                },
                view : {
                    selectedMulti : false,
                    nameIsHTML: true,
                    fontCss: setFontCss_ztree
                },
                data : {
                    simpleData : {
                        enable : true
                    }
                },
                callback : {
                    onClick : itemInformation.zTreeOnClick
                }
            };
            $.fn.zTree.init($("#treeDemo"), treeSetting, null);
            //表格列定义
            var columnDefs = [ {
                //第一列，用来显示序号
                "searchable" : false,
                "orderable" : false,
                "targets" : 0
            } ];
            var columns = [{
                //第一列，用来显示序号
                "data" : null,
                "class" : "text-center"
            }, {
                //第一列，用来显示序号
                "data" : null,
                "class" : "text-center",
                render: function (data, type, row, meta) {
                    var result = '';
                    result += '<input  type="checkbox" name="subChk"  value="' + row.id + '" />';
                    return result;
                }
            },{
                "data" : null,
                "class" : "text-center", //最后一列，操作按钮
                render : function(data, type, row, meta) {
                    var editUrlPath = myTable.editUrl + row.id + ".gsp"; //修改地址
                    var result = '';
                    //修改按钮
                    result += '<button href="'+editUrlPath+'" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                    //删除按钮
                    result += '<button type="button" onclick="itemInformation.deleteItemAndCheckBond(\'' + row.id + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
                    return result;
                }
            }, {
                "data" : "thingNumber",
                "class" : "text-center"
            }, {
                "data" : "name",
                "class" : "text-center",
                render:function(data){
                    return html2Escape(data)
                }
            },{
                "data": "groupName",
                "class": "text-center"
            },{
                "data": "deviceNumber",
                "class": "text-center",
                render : function(data, type, row, meta) {
                    var result = data;
                    if(data === null || data === undefined){
                        result = '';
                    }
                    return result;
                }
            }, {
                "data": "simcardNumber",
                "class": "text-center",
                render : function(data, type, row, meta) {
                    var result = data;
                    if(data === null || data === undefined){
                        result = '';
                    }
                    return result;
                }
            }, {
                "data": "assign",
                "class": "text-center",
                render : function(data, type, row, meta) {
                    var result = data;
                    if(data === null || data === undefined){
                        result = '';
                    }
                    return result;
                }
            },{
                "data" : "category",
                "class" : "text-center",
                render : function(data, type, row, meta) {
                    var result = "-";
                    $(thingCategoryList).each(function(e,q){
                        if (q.code == row.category){
                            result = q.value;
                            return;
                        }
                    })
                    return result;
                }
            },{
                "data" : "type",
                "class" : "text-center",
                render : function(data, type, row, meta) {
                    var result = "-";
                    $(thingTypeList).each(function(e,q){
                        if (q.code == row.type){
                            result = q.value;
                            return;
                        }
                    })
                    return result;
                }
            },{
                "data" : "label",
                "class" : "text-center",
                render:function(data){
                    return html2Escape(data)
                }

            },{
                "data" : "model",
                "class" : "text-center",
                render:function(data){
                    return html2Escape(data)
                }

            },{
                "data" : "material",
                "class" : "text-center",
                render:function(data){
                    return html2Escape(data)
                }

            },{
                "data" : "weight",
                "class" : "text-center",
                render : function(data, type, row, meta) {
                    var result = data;
                    if(data === null || data === undefined){
                        result = '';
                    }
                    return result;
                }

            },{
                "data" : "spec",
                "class" : "text-center",
                render:function(data){
                    return html2Escape(data)
                }

            },{
                "data" : "manufacture",
                "class" : "text-center",
                render:function(data){
                    return html2Escape(data)
                }

            },{
                "data" : "dealer",
                "class" : "text-center",
                render:function(data){
                    return html2Escape(data)
                }

            },{
                "data" : "place",
                "class" : "text-center",
                render:function(data){
                    return html2Escape(data)
                }

            },{
                "data" : "productDateStr",
                "class" : "text-center",
                render:function(data){
                    return html2Escape(data)
                }

            },{
                "data" : "createDataTime",
                "class" : "text-center",
                render:function(data){
                    if (data) {
                        return new Date(data).Format('yyyy-MM-dd hh:mm:ss');
                    }
                    
                        return '';
                    
                }
            },{
                "data" : "updateDataTime",
                "class" : "text-center",
                render:function(data){
                    if (data) {
                        console.log(new Date(data).Format('yyyy-MM-dd hh:mm:ss'))
                        return new Date(data).Format('yyyy-MM-dd hh:mm:ss');
                    }
                    
                        return '';
                    
                }
            },{
                "data" : "remark",
                "class" : "text-center",
                render:function(data){
                    if (data != null && data != '') {
                        if (data.length > 20) {
                            return '<span class="demo demoUp" alt="' + html2Escape(data) + '">' + html2Escape(data).substring(0, 20) + "..." + '</span>';
                        }
                        
                            return html2Escape(data);
                        
                    }
                    
                        return '';
                    
                }
            },{
                "data" : "thingPhoto",
                "class" : "text-center",
                render:function(data){
                    if (data != null && data != '') {
                        return '<a href="javascript:itemInformation.icoPhotoShow(\''+data+'\')">查看照片</a>';
                    }
                    return '';
                }
            }];
            //ajax参数
            var ajaxDataParamFun = function(d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
                d.groupName = selectTreeId;
                d.groupType = selectTreeType;
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/m/basicinfo/monitoring/ThingInfo/list',
                editUrl : "/clbs/m/basicinfo/monitoring/ThingInfo/edit_",
                deleteUrl : "/clbs/m/basicinfo/monitoring/ThingInfo/delete_",
                deletemoreUrl: '/clbs/m/basicinfo/monitoring/ThingInfo/deletemore',
                enableUrl : '/clbs/c/user/enable_',
                disableUrl : '/clbs/c/user/disable_',
                columnDefs : columnDefs, //表格列定义
                columns : columns, //表格列
                dataTableDiv : 'dataTable', //表格
                ajaxDataParamFun : ajaxDataParamFun, //ajax参数
                pageable : true, //是否分页
                showIndexColumn : true, //是否显示第一列的索引列
                enabledChange : true,
                drawCallbackFun: function () {//鼠标移入后弹出气泡显示单元格内容；
                    $(".demoUp").mouseover(function () {
                        var _this = $(this);
                        if (_this.attr("alt")) {
                            _this.justToolsTip({
                                animation: "moveInTop",
                                width: "auto",
                                contents: _this.attr("alt"),
                                gravity: 'top',
                                distance: 20
                            });
                        }
                    })
                }
            }
            //创建表格ter
            myTable = new TG_Tabel.createNew(setting);
            //表格初始化
            myTable.init();
        },
        //组织树预处理函数
        ajaxDataFilter: function(treeId, parentNode, responseData){
            if (responseData) {
                for (var i = 0; i < responseData.length; i++) {
                    responseData[i].open = true;
                }
            }
            return responseData;
        },
        icoPhotoShow: function (ico) {
            var icoLogo =  ico;
            $("#icoLogo").attr('src', icoLogo);
            $("#icoPhotoShow").modal("show");
        },
        //点击节点
        zTreeOnClick: function(event, treeId, treeNode){
            if (treeNode.type == "group") {
                selectTreepId = treeNode.id;
                selectTreeId = treeNode.uuid;
            } else {
                selectTreepId = treeNode.pId;
                selectTreeId = treeNode.id;
            }
            selectTreeType = treeNode.type;
            myTable.requestData();
        },
        //全选
        checkAllClick: function(){
            $("input[name='subChk']").prop("checked",this.checked);
        },
        //单选
        subChkClick: function(){
            $("#checkAll").prop("checked",subChk.length ==subChk.filter(":checked").length ? true:false );
        },
        //批量删除
        delModelClick: function(){
            //判断是否至少选择一项
            var chechedNum=$("input[name='subChk']:checked").length;
            if(chechedNum==0){
                layer.msg(selectItem,{move:false});
                return
            }
            var checkedList = new Array();
            $("input[name='subChk']:checked").each(function () {
                checkedList.push($(this).val());
            });
            // myTable.deleteItems({'deltems':checkedList.toString()});

            var url = "/clbs/m/basicinfo/monitoring/ThingInfo/deletemore";
            var data = {"deltems": checkedList.toString()};
            layer.confirm(publicDelete, {btn: ["确定", "取消"], icon: 3, title: "操作确认"}, function () {
                json_ajax_noTimeout("POST", url, "json", true, data, itemInformation.deleteMuchCallBack);
            });
        },
        // 批量删除回调
        deleteMuchCallBack: function (data) {
            if (data.success) {
                if (data != null && data.obj != null && data.obj.infoMsg != null && data.obj.infoMsg.length > 0) {
                    var boundBrandIds = data.obj.boundThingIds;
                    layer.confirm("已绑定物品：" + data.obj.boundThingNumbers + "</br>" + data.obj.infoMsg, {btn: ['确认', '取消']}, function () {
                        if (boundBrandIds != null && boundBrandIds != "") {
                            var url = "/clbs/m/infoconfig/infoinput/getConfigIdByMonitorIds";
                            var data = {"vehicleId": boundBrandIds};
                            json_ajax_noTimeout("POST", url, "json", true, data, itemInformation.getConfigIdByThingIdInMuchCallBack);
                        }
                    }, function () {
                        layer.closeAll();
                        myTable.refresh();
                    });
                } else {
                    layer.closeAll();
                    myTable.refresh();
                    layer.msg("删除成功");
                }
            } else {
                layer.msg(data.msg);
            }
        },
        getConfigIdByThingIdInMuchCallBack:function(data) {
            if (data.success) {
                // step2:根据绑定表id解除车辆与终端的绑定关系
                var configId = data.obj.configId;
                var data = {"deltems":configId};
                var url = "/clbs/m/infoconfig/infoinput/deletemore";
                json_ajax_noTimeout("POST", url, "json", true, data, itemInformation.deleteConfigByTingIdInMuchCallBack);
            } else {
                layer.msg(data.msg);
            }
        },
        deleteConfigByTingIdInMuchCallBack: function (data) {
            if (data.success) {
                // step3:解除之后，再直接删除车辆
                var vid = data.obj.vehicleId;
                var url = "/clbs/m/basicinfo/monitoring/ThingInfo/deletemore";
                var dataIds = {"deltems": vid};
                json_ajax_noTimeout("POST", url, "json", true, dataIds, itemInformation.deleteMuchCallBack);
            } else {
                layer.msg(data.msg);
            }
        },
        // 删除操作
        deleteItemAndCheckBond: function (id) {
            var url = "/clbs/m/basicinfo/monitoring/ThingInfo/delete_" + id + ".gsp";
            var data = {"id": id};
            layer.confirm(publicDelete, {btn: ["确定", "取消"], icon: 3, title: "操作确认"}, function () {
                json_ajax("POST", url, "json", true, data, itemInformation.deleteCallBack);
            });
        },
        // 删除操作回调
        deleteCallBack: function (data) {
            if (data.success) {
                if (data != null && data.obj != null && data.obj.infoMsg != null) {
                    layer.confirm(data.obj.infoMsg, {btn: ['确认', '取消']}, function () {
                        // step1:根据车辆id获取车辆终端绑定表id
                        var vid = data.obj.thingId;
                        var url = "/clbs/m/infoconfig/infoinput/getConfigIdByVehicleId";
                        var dataId = {"vehicleId": vid};
                        json_ajax("POST", url, "json", true, dataId, itemInformation.getConfigIdByThingIdCallBack);
                    });
                } else {
                    layer.closeAll();
                    myTable.refresh();
                }
            } else {
                layer.msg(data.msg);
            }
        },
        // 根据车辆id获取车辆终端绑定表id的回调
        getConfigIdByThingIdCallBack: function (data) {
            console.log(data)
            if (data.success) {
                // step2:根据绑定表id解除车辆与终端的绑定关系
                var configId = data.obj.configId;
                var url = "/clbs/m/infoconfig/infoinput/delete_" + configId + ".gsp";
                json_ajax("POST", url, "json", true, data, itemInformation.deleteConfigByTidCallBack);
            } else {
                layer.msg(data.msg);
            }
        },
        // 根据车辆id获取信息配置表id并删除绑定关系的回调
        deleteConfigByTidCallBack: function (data) {
            if (data.success) {
                // step3:解除之后，再直接删除车辆
                var vid = data.obj.vehicleId;
                var newVid = vid.split(',');

                var url = "/clbs/m/basicinfo/monitoring/ThingInfo/delete_" + newVid[0] + ".gsp";
                var dataIds = {"id": vid};
                json_ajax("POST", url, "json", true, dataIds, itemInformation.deleteCallBack);
            } else {
                layer.msg(data.msg);
            }
        },
        //刷新列表
        refreshTable:function(){
            selectTreeId = "";
            selectTreeType = "";
            $("#simpleQueryParam").val("");
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.selectNode("");
            zTree.cancelSelectedNode();
            myTable.requestData();
        }
    }
    $(function(){
        $('input').inputClear();
        itemInformation.init();
        //全选
        $("#checkAll").bind("click",itemInformation.checkAllClick);
        //单选
        subChk.bind("click",itemInformation.subChkClick);
        //批量删除
        $("#del_model").bind("click",itemInformation.delModelClick);
        $("#refreshTable").on("click",itemInformation.refreshTable);


        $('input').inputClear().on('onClearEvent',function(e,data){
            var id = data.id;
            if(id == 'search_condition'){
                search_ztree('treeDemo', id,'assignment');
            };
        });
        //IE9
        if(navigator.appName=="Microsoft Internet Explorer" && navigator.appVersion.split(";")[1].replace(/[ ]/g,"")=="MSIE9.0") {
            var search;
            $("#search_condition").bind("focus",function(){
                search = setInterval(function(){
                    search_ztree('treeDemo', 'search_condition','assignment');
                },500);
            }).bind("blur",function(){
                clearInterval(search);
            });
        }
        //IE9 end

        // 输入框输入类型过滤
        inputValueFilter('#simpleQueryParam');

        // 组织架构模糊搜索
        $("#search_condition").on("input oninput",function(){
            search_ztree('treeDemo', 'search_condition', 'assignment');
        });
    })
}(window,$))
