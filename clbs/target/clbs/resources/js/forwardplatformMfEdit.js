(function ($, window) {
    thirdformedit = {
        //初始化
        init: function () {
            var platformListUrl = "/clbs/m/forwardplatform/mf/thirdmVehicelList";
            json_ajax("POST", platformListUrl, "json", true, null, thirdformedit.getPlatformList);
        },
        changeBrand :function () {
            $("#configId").val("")
        },
        doSubmit: function () {
            thirdformedit.hideErrorMsg();
            if ($.trim($("#configId").val()) == "") {
                thirdformedit.showErrorMsg("请选择正确的车辆！", "brand");
                return;
            }
            $("#editForm").ajaxSubmit(function () {
                $("#commonSmWin").modal("hide");
                myTable.refresh();
                thirdForwardManagement.zTreeinit();
            });
        },
        getPlatformList: function (data) {
            var datas = $.parseJSON(data.msg).list;
            var dataList = {value: []}, i = datas.length;
            while (i--) {
                dataList.value.push({
                    name: datas[i].brand,
                    id: datas[i].id
                });
            }
            $("#brand").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                data: dataList,
                effectiveFields: ["name"]
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {

                $("#configId").val(keyword.id);
            }).on('onUnsetSelectValue', function () {
            });
        },showErrorMsg: function(msg, inputId){
            if ($("#error_label_edit").is(":hidden")) {
                $("#error_label_edit").text(msg);
                $("#error_label_edit").insertAfter($("#" + inputId));
                $("#error_label_edit").show();
            } else {
                $("#error_label_edit").is(":hidden");
            }
        },
        //错误提示信息隐藏
        hideErrorMsg: function(){
            $("#error_label_edit").hide();
        }
    }
    $(function () {
    	$('input').inputClear();
        thirdformedit.init();
    })
})($, window)