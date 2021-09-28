/**
 * 文件导入
 */
(function ($, window) {

    var filePath, arr, fileName;

    window.msgImport = {
        //Excel文件导入到数据库中
        importEmp: function () {
            //检验导入的文件是否为Excel文件   
            var excelPath = document.getElementById("excelPath").value;
            if (excelPath == null || excelPath == '') {
                layer.msg("请选择要上传的Excel文件！", {move: false});
                return false;
            } else {
                var fileExtend = excelPath.substring(excelPath.lastIndexOf('.')).toLowerCase();
                if (fileExtend == '.xls' || fileExtend == '.xlsx') {
                    return true;
                } else {
                    layer.msg("文件格式需为'.xls' 或 '.xlsx' 的格式！", {move: false});
                    return false;
                }
            }
        },
        validates: function (importFormId) {
            return $("#" + importFormId).validate({
                rules: {
                    file: {
                        required: true
                    }
                },
                messages: {
                    file: {
                        required: "请选择文件"
                    }
                }
            }).form();
        },
        doSubmits: function (importFormId, $this, table) {
            if (msgImport.importEmp() && msgImport.validates(importFormId)) {
                $('.progress').show();
                $this.attr('disabled', 'disabled');
                var interval = setInterval(function () {
                    $.ajax({
                        url: "/clbs/m/infoconfig/infoinput/importProgress",
                        dataType: "json",
                        success: function (data) {
                            if (data > 100) {
                                return;
                            }
                            msgImport.setProgressBar(data);
                        }
                    })
                }, 1000);
                $("#" + importFormId).ajaxSubmit(function (message) {
                    var data = JSON.parse(message);
                    if (data.success) {
                        clearInterval(interval);
                        msgImport.setProgressBar(100);
                        setTimeout(function () {
                            $this.removeAttr('disabled');
                            $("#commonSmWin").modal("hide");
                            if (eval("[" + message + "]")[0].msg != null) {
                                layer.alert(eval("[" + message + "]")[0].msg, {
                                    id: "promptMessage"
                                });
                            }
                            if (typeof myTable != 'undefined') {
                                myTable.requestData();
                            }
                            if (table) {
                                if (typeof table === 'function') {
                                    table();
                                } else {
                                    table.requestData();
                                }
                            }
                        }, 1000)
                        $("#closebutton").removeAttr("disabled");
                        $(".close").css("display", "block");

                    } else {
                        clearInterval(interval);
                        msgImport.setProgressBar(0);
                        $('.progress').hide();
                        //layer.msg(data.msg,{move:false});
                        if (eval("[" + message + "]")[0].msg != null) {
                            layer.alert(eval("[" + message + "]")[0].msg, {
                                id: "promptMessage"
                            });
                        }
                        $this.removeAttr("disabled");
                        $("#closebutton").removeAttr("disabled");
                        $(".close").css("display", "block");

                    }
                });
            }
        },
        setProgressBar: function (percentage) {
            $(".progress-bar").attr("aria-valuenow", percentage).css("width", percentage + "%").text(percentage + "%");
            $("#closebutton").attr("disabled", "disabled");
            $(".close").css("display", "none");
        },
        //显示文件名称
        showFileName: function () {
            filePath = $("#excelPath").val();
            arr = filePath.split('\\');
            fileName = arr[arr.length - 1];
            $(".fileNameShow").html(fileName);
        }
    };

    $(function () {
        $("#doSubmits").on("click", function () {
            //点击获取from表单id名称
            var allFormId = $(this).parents("form").attr("id");
            msgImport.doSubmits(allFormId, $(this));
        });
        $(".inpFilePhoto").on("change", "input[type='file']", msgImport.showFileName);
    })

})($, window);