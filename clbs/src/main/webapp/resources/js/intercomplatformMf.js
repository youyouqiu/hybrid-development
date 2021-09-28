(function (window, $) {
    //单选
    var subChk = $("input[name='subChk']");
    var menu_text = "";
    var table = $("#dataTable tr th:gt(1)");
    objForwardManagement = {
        init: function () {
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" />" + table[i].innerHTML + "</label></li>"
            };
            $("#Ul-menu-text").html(menu_text);
            objForwardManagement.ztreeInit();
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
                        //删除按钮
                        result += '<button type="button" onclick="objForwardManagement.deleteRole(\'' + row.id + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
                        return result;
                    }
                },{
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
                },{
                    "data": "intercomPlatformDescription",
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
            json_ajax("POST", platformListUrl, "json", true, parameterList, objForwardManagement.getPlatformList);
        },
        ztreeInit:function(){
        	//车辆树
            var setting = {
                 async: {
                     url: "/clbs/m/intercomplatform/deviceconfig/intercomDeviceVehicelTree",
                     type: "post",
                     enable: true,
                     autoParam: ["id"],
                     dataType: "json",
                     dataFilter: objForwardManagement.ajaxDataFilter
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
                     onCheck: objForwardManagement.onCheckVehicle
                 }
             };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },
        uporganizationanme: "北京中位科技有限公司",
        mobile: "",
        organizationname: "",
        pwd: "",
        userName: "",
        remarks: "",
        email: "",
        getUserCall: function (data) {
            if (data.success) {
                var msg = $.parseJSON(data.msg)
                objForwardManagement.organizationname = msg.organizationname;
                objForwardManagement.mobile = msg.mobile;
                objForwardManagement.pwd = msg.pwd;
                objForwardManagement.userName = msg.userName;
                objForwardManagement.remarks = msg.remarks;
                objForwardManagement.email = msg.email;
                objForwardManagement.uploadOrganization();

            }else{
                layer.msg(data.msg,{move:false});
            }
        },
        // 显示错误提示信息
        showErrorMsg: function(msg, inputId){
            if ($("#error_label").is(":hidden")) {
                $("#error_label").text(msg);
                $("#error_label").insertAfter($("#" + inputId));
                $("#error_label").show();
            } else {
                $("#error_label").is(":hidden");
            }
        },
        myAjax: function (type, url, dataType, async, data, callback, error) {
            $.ajax(
            {
                type: type,//通常会用到两种：GET,POST。默认是：GET
                contentType: "application/x-www-form-urlencoded",
                url: url,//(默认: 当前页地址) 发送请求的地址
                dataType: dataType, //预期服务器返回的数据类型。"json"
                async: async, // 异步同步，true  false
                data: data,
                crossDomain: true,
                timeout: 30000, //超时时间设置，单位毫秒
                success: callback, //请求成功
                error: error,//请求出错
            });
        },
        uploadOrganization: function () {
            var parameterList = {uporganizationname: objForwardManagement.uporganizationanme,organizationname:
                    objForwardManagement.organizationname,linkman: "",contactnumber: ""};
            var platformListUrl = "http://www.airtalking.com/rest/organizationadd/";
            objForwardManagement.myAjax("POST", platformListUrl, "json", true, parameterList, objForwardManagement.uploadOrganizationCall, objForwardManagement.uploadOrganizationErr);
        }, 
        uploadOrganizationCall: function (data) {
            objForwardManagement.uploadUser();
        }, 
        uploadOrganizationErr: function (data) {
            layer.msg("对讲平台连接失败，请联系管理员！");
        },
        uploadUser: function () {
            var parameterList = {organizationname: objForwardManagement.organizationname,accountsname: objForwardManagement.userName,
                pwd: objForwardManagement.pwd,endtime: "",contactnumber: objForwardManagement.mobile,email:objForwardManagement.email,
                remarks: objForwardManagement.remarks};
            var platformListUrl = "http://www.airtalking.com/rest/accountsadd/";
            objForwardManagement.myAjax("POST", platformListUrl, "json", true, parameterList, objForwardManagement.uploadUserCall, objForwardManagement.uploadUserErr);
        }, 
        uploadUserCall: function (data) {
        }, 
        uploadUserErr: function (data) {
            layer.msg("对讲平台连接失败，请联系管理员！");
        },
        //错误提示信息隐藏
        hideErrorMsg: function(){
            $("#error_label").hide();
        },
        intercom:function () {
        	window.open("http://www.airtalking.com/rest/login/?accountsname="+objForwardManagement.userName+"&pwd="+objForwardManagement.pwd+"");
        },
        addConfigCheck: function () {
            objForwardManagement.hideErrorMsg();
            if ($("#intercomPlatformId").val() == "" || $("#ipAddress").val() == "") {
                objForwardManagement.showErrorMsg("请选择正确的平台","ipAddress");
                return;
            }
            if ($("#vehicleIds").val() == "") {
                objForwardManagement.showErrorMsg("请选择车辆","groupSelect");
                return;
            }
            var param = {intercomPlatformId:$("#intercomPlatformId").val(),ids:$("#vehicleIds").val()}
            var platformListUrl = "/clbs/m/intercomplatform/deviceconfig/addConfig";
            json_ajax("POST", platformListUrl, "json", true, param, objForwardManagement.addConfigCallBack);
        },
        uploadDeviceList:null,
        deleteDeviceList:null,
        addConfigCallBack: function (data) {
            if(data.success){
                $("#intercomPlatformId").val("");
                $("#vehicleIds").val("");
                $("#ipAddress").val("");
                $("#groupSelect").val("");
                myTable.refresh();
                objForwardManagement.uploadDeviceList= $.parseJSON(data.msg).list;
                objForwardManagement.ztreeInit();
                $("#ipAddress").siblings('i').remove();
                objForwardManagement.uploadDevice();
            }else {
                layer.msg(data.msg);
            }
        },
        uploadDevice:function () {
            if(objForwardManagement.uploadDeviceList.length>0){
                var device = objForwardManagement.uploadDeviceList[0];
                var param = {organizationname: objForwardManagement.organizationname,terminalsn:device.deviceNumber,
                    terminaluser:objForwardManagement.userName,terminalusercall:objForwardManagement.mobile,simno: device.simcardNumber,
                    remarks: "",terminalusercall: ""};
                var platformListUrl = "http://www.airtalking.com/rest/terminaladd/";
                objForwardManagement.myAjax("POST", platformListUrl, "json", true, param, objForwardManagement.uploadDeviceCall, objForwardManagement.uploadDeviceErr);
            }
        },
        uploadDeviceCall : function (data) {
            var resultmsg = data.resultmsg;
            if(resultmsg="success"){
                objForwardManagement.uploadDeviceList.splice(0,1);
                objForwardManagement.uploadDevice();
            }else {
                layer.msg(data.resultmsg);
            }
        },
        uploadDeviceErr : function (data) {
            objForwardManagement.uploadDevice();
        },
        addressOnChange: function () {
            $("#intercomPlatformId").attr("value", "");
        },
        // 删除角色
        deleteRole: function (id) {
            var param = {id:id}
            var platformListUrl ="/clbs/m/intercomplatform/deviceconfig/delete_";
            layer.confirm("删掉就没啦，请谨慎下手！", {btn : ["确定", "取消"]}, function () {
            	json_ajax("POST", platformListUrl+id+".gsp", "json", true, param, objForwardManagement.deleteCall);
            });
        },
        deleteCall :function (data) {
            if(data.success){
            	layer.msg("删除成功！",{move:false});
                objForwardManagement.deleteDeviceList= $.parseJSON(data.msg).list;
                myTable.refresh();
                objForwardManagement.ztreeInit();
                objForwardManagement.deleteDevice();
            } else {
                layer.closeAll();
                myTable.refresh();
                layer.msg(data.msg,{move:false});
            }
        },
        deleteDevice : function () {
            if(objForwardManagement.deleteDeviceList.length>0){
                var device = objForwardManagement.deleteDeviceList[0];
                var param = {organizationname: objForwardManagement.organizationname,terminalsn:device.deviceNumber};
                var platformListUrl = "http://www.airtalking.com/rest/terminaldel/";
                objForwardManagement.myAjax("POST", platformListUrl, "json", true, param, objForwardManagement.deleteDeviceCll, objForwardManagement.deleteDeviceErr);
            }
        },
        deleteDeviceCll :function (data) {
            var resultmsg = data.resultmsg;
            if(resultmsg="success"){
                objForwardManagement.deleteDeviceList.splice(0,1);
                objForwardManagement.deleteDevice();
            }else {
                layer.msg(data.resultmsg);
            }

        },
        deleteDeviceErr :function (data) {
            objForwardManagement.deleteDevice();
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
            $("#ipAddress").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                data: dataList,
                effectiveFields: ["name"]
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
               $("#intercomPlatformId").val(keyword.id);
            }).on('onUnsetSelectValue', function () {
            });
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            $("#charSelect").val("").attr("data-id", "").bsSuggest("destroy");
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
                    nodes = zTree.getCheckedNodes(true)
            var vids = "";
            var vnames = "";
            for (var i = 0; i < nodes.length; i++) {
                if (nodes[i].type == "vehicle") {
                    vids += nodes[i].id + ',';
                    vnames += nodes[i].name + ',';
                }
            }
            $("#groupSelect").val(vnames);
            $("#vehicleIds").val(vids);
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
                    if (responseData[i].type == "group") {
                        gourps.push(responseData[i].name)
                    }
                    var gName = objForwardManagement.unique(gourps)
                    if (responseData[i].type == "vehicle") {
                        veh.push(responseData[i].name)
                        vid.push(responseData[i].id)
                    }
                }
                var vehName = objForwardManagement.unique(veh);
                var vehId = objForwardManagement.unique(vid);
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
            var param = {'deltems': checkedList.toString()}
            var platformListUrl ="/clbs/m/intercomplatform/deviceconfig/deletemore";
            json_ajax("POST", platformListUrl, "json", true, param, objForwardManagement.deleteCall);
        },
        //加载完成后执行
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            myTable.requestData();
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
        objForwardManagement.init();
        $("#checkAll").bind("click", objForwardManagement.cleckAll);
        subChk.bind("click", objForwardManagement.subChkClick);
        $("#groupSelect").bind("click", showMenuContent);
        $("#del_model").on("click", objForwardManagement.delModelClick);
        $("#refreshTable").on("click", objForwardManagement.refreshTable);
        var uerUrl = "/clbs/m/intercomplatform/deviceconfig/getUser";
        json_ajax("POST", uerUrl, "json", true, null, objForwardManagement.getUserCall);
    })
})(window, $)