(function ($, window) {
    var monitorType = $("#monitorType").val();
    var msgDetails = {
        //初始化
        init: function () {
            if (monitorType == 1) {
                $(".monitoring-car").hide();
                $(".monitoring-thing").hide();
                json_ajax("GET", "/clbs/m/infoconfig/infoinput/getGroups", "json", true, {}, msgDetails.getGroupsCallback);
                json_ajax("GET", "/clbs/m/infoconfig/infoinput/getParentGroup", "json", true, {}, msgDetails.getParentGroupCallback);
            } else if (monitorType == 0) {
                $(".monitoring-people").hide();
                $(".monitoring-thing").hide();
                json_ajax("GET", "/clbs/m/infoconfig/infoinput/getprofessionals", "json", true, {}, msgDetails.getprofessionalsCallback);
                json_ajax("GET", "/clbs/m/infoconfig/infoinput/getGroups", "json", true, {}, msgDetails.getGroupsCallback);
                json_ajax("GET", "/clbs/m/infoconfig/infoinput/getParentGroup", "json", true, {}, msgDetails.getParentGroupCallback);
            }else if(monitorType == 2){
                $(".monitoring-car").hide();
                $(".monitoring-people").hide();
               /* $('#professionalsDetails').show();*/
                json_ajax("GET", "/clbs/m/infoconfig/infoinput/getGroups", "json", true, {}, msgDetails.getGroupsCallback);
                json_ajax("GET", "/clbs/m/infoconfig/infoinput/getprofessionals", "json", true, {}, msgDetails.getprofessionalsCallback);
                json_ajax("GET", "/clbs/m/infoconfig/infoinput/getParentGroup", "json", true, {}, msgDetails.getParentGroupCallback);
            }
        },
        getprofessionalsCallback: function (data) {
            for (var i = 0; i < data.length; i++) {
                var phone = data[i].phone;
                $("#configProfessionals").append(
                    "<tr>" +
                    "<td>" + data[i].name + "</td>" +
                    "<td>" + msgDetails.getPositionTypeValue(data[i].type) + "</td>" +
                    "<td>" + (data[i].identity == null ? "" : data[i].identity) + "</td>" +
                    "<td>" + (data[i].jobNumber == null ? "" : data[i].jobNumber ) + "</td>" +
                    "<td>" + (data[i].cardNumber == null ? "" : data[i].cardNumber) + "</td>" +
                    "<td id='gender" + i + "'>" + data[i].gender + "</td>" +
                    "<th>" + (data[i].birthday == null ? "" : (data[i].birthday.length > 10 ? data[i].birthday.substr(0, 10) : "")) + "</th>" +
                    "<th>" + (phone == null || phone === undefined ? "" : phone) + "</th>" +
                    "<th>" + (data[i].email == null ? "" : data[i].email) + "</th>" +
                    "</tr>"
                )
                if ($("#gender" + i).text() == "1") {
                    $("#gender" + i).text("男")
                } else {
                    $("#gender" + i).text("女")
                }
            }
        },
        getGroupsCallback: function (data) {
            for (var i = 0; i < data.length; i++) {
                $("#groups").append(
                    "<tr>" +
                    "<td>" + (data[i].name == null ? "" : data[i].name) + "</td>" +
                    "<td>" + (data[i].groupName == null ? "" : data[i].groupName) + "</td>" +
                    "<td>" + (data[i].contacts == null ? "" : data[i].contacts) + "</td>" +
                    "<td>" + (data[i].telephone == null ? "" : data[i].telephone) + "</td>" +
                    "</tr>"
                )
            }
        },
        getParentGroupCallback: function (data) {
            for (var i = 0; i < data.length; i++) {
                $("#belongs_groups").append(
                    "<tr>" +
                    "<td>" + (data[i].name == null ? "" : data[i].name) + "</td>" +
                    "<td>" + (data[i].principal == null ? "" : data[i].principal) + "</td>" +
                    "<td>" + (data[i].phone == null ? "" : data[i].phone) + "</td>" +
                    "<td>" + (data[i].address == null ? "" : data[i].address) + "</td>" +
                    "<td>" + (data[i].description == null ? "" : data[i].description) + "</td>" +
                    "</tr>"
                )
            }
        },
        // 获取岗位类型实际值
        getPositionTypeValue: function (positionTypeIntValue) {
            if (positionTypeIntValue != null) {
                return positionTypeIntValue;
            } else {
                return "";
            }
        },
    }
    $(function () {
        msgDetails.init();
        var roles = {};
        var messages = {};
        myTable.add('commonWin', 'detailForm', roles, messages);
    })
})($, window)