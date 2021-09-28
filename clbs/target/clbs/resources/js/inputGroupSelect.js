;(function ($, window, document, undefined) {
    var ButtonGroupPullDown = function (ele) {
        this.$element = ele;
    };
    ButtonGroupPullDown.prototype = {
        initSelectList: function () {
            this.$element.unbind("click").bind("click", function (e) {
                if (e.target.className == "form-control select-value") {
                    var deviceTypeId = $(this).parent().children("input")[0].id;
                    ButtonGroupPullDown.prototype.showSelectList(deviceTypeId);
                } else {
                    var deviceTypeId = $(this).parent().parent().children("input")[0].id;
                    ButtonGroupPullDown.prototype.showSelectList(deviceTypeId);
                }
                e.stopPropagation();
            });
            $(".select-list>li").unbind("click").bind("click", function () {
                var parent = $(this).closest('.select');
                parent.find('.select-value').text($(this).text());
                var deviceTypeId = $(this).parent().parent().children("input")[0].id;
                if (deviceTypeId == "deviceType") {
                    $("#" + deviceTypeId).attr("value", $(this).val());
                    $("#deviceTypeList").removeClass("is-open");
                } else if (deviceTypeId == "speedDeviceType") {
                    $("#" + deviceTypeId).attr("value", $(this).val());
                    $("#speedDeviceTypeList").removeClass("is-open");
                }
            });
            $("body").unbind("click").bind("click", function () {
                $("#speedDeviceTypeList").removeClass("is-open");
                $("#deviceTypeList").removeClass("is-open");
            });
        },
        showSelectList: function (id) {
            if (id == "deviceType") {
                if ($("#deviceTypeList").is(":hidden")) {
                    $("#deviceTypeList").addClass("is-open");
                } else {
                    $("#deviceTypeList").removeClass("is-open");
                }
            } else if (id == "speedDeviceType") {
                if ($("#speedDeviceTypeList").is(":hidden")) {
                    $("#speedDeviceTypeList").addClass("is-open");
                } else {
                    $("#speedDeviceTypeList").removeClass("is-open");
                }
            }
        }
    };
    $.fn.buttonGroupPullDown = function () {
        var buttonGroupPullDown = new ButtonGroupPullDown(this);
        return buttonGroupPullDown.initSelectList();
    }
})(jQuery, window, document);