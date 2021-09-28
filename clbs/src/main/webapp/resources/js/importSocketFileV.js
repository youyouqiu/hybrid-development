/**
 * 文件导入
 */
(function ($, window) {

    var filePath, arr, fileName;
    var isFirst = true;
    var timer;

    window.importSocketFileV = {
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

        clock: function () {
            if(timer) clearInterval(timer);
            timer = setInterval(() => {
                var m, s;
                if(isFirst){
                    $("#time").text('02:59');
                    isFirst = false;
                }else{
                   m = Number($("#time").text().split(':')[0]);
                   s = Number($("#time").text().split(':')[1]);

                   s--;

                   if(s === 0 && m === 0){
                       clearInterval(timer);
                       var aDom = $("#importError").children()[1];
                       var spanDom = $("#importError").children()[2];
                       aDom.style = 'color:#a9adaf; pointer-events:none';
                       aDom.href = '';
                       spanDom.innerHTML = '(下载链接已失效)';
                   }

                   if(s < 0){
                       m--;
                       s = 59;
                   }

                   if(s >= 0 && s <= 9){
                       s = "0" + s;
                   }

                   if(m === 0 && s >=0){
                        $('#unit').text('秒')
                    }

                   if(m >= 0 && m <= 9){
                       m = "0" + m;
                   }

                   $("#time").text(m +':'+ s)
                }
            },1000);
        },

        doSubmits: function (importFormId, $this, table) {
            if (importSocketFileV.importEmp() && importSocketFileV.validates(importFormId)) {
                $('.progress').show();
                $this.attr('disabled', 'disabled');

                $("#" + importFormId).ajaxSubmit(function (message) {
                    if(message != '' && message != undefined && message != null){
                        var data = JSON.parse(message);
                        if (data.success) {
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
                                    table.requestData();
                                }
                            }, 1000);
                            $("#closebutton").removeAttr("disabled");
                            $(".close").css("display", "block");

                        } else {
                            setTimeout(function () {
                                if(module === 'VEHICLE'){
                                    isFirst = true;
                                    importSocketFileV.clock();
                                }
                                var src = '/clbs/m/import/' + module + '/error';
                                var html = '';

                                if(module === 'VEHICLE') {
                                   html = `<div id="importError" style='padding: 20px 10px; font-size: 15px'>
                                         <span style="color: red">导入失败：</span>
                                         <a style="color: #6dcff6" href="${src}">下载文件</a>
                                         <span>(请尽快下载，<sapn id="time">三</sapn><sapn id="unit">分钟</sapn>后失效)</span>
                                      </div>`
                                }else{
                                    html = "<div style='padding: 20px 80px; font-size: 15px'>"+
                                        '<span style="color:red">导入失败: </span>' +
                                        '<a style="color:#6dcff6"  href='+ src +'>下载文件</a>'
                                        +"</div>";
                                }

                                layer.open({
                                    title:'提示',
                                    content: data.msg || html,
                                    maxWidth: 420,
                                    btn:['确定'],
                                    yes:function (){
                                        if(module === 'VEHICLE'){
                                            clearInterval(timer);
                                        }
                                        layer.closeAll();
                                    },
                                    cancel:function (){
                                        if(module === 'VEHICLE'){
                                            clearInterval(timer);
                                        }
                                    }
                                });

                                $('.progress').hide();
                                $this.removeAttr("disabled");
                                $("#closebutton").removeAttr("disabled");
                                $(".close").css("display", "block");
                            },500);
                        }
                    }
                });
            }
        },
        setProgressBar: function (data) {
            console.log('socket',data);
            if(data.progress.length == 0) return;
            var progress = data.progress[0];
            var percentage = progress.ratio * 100;
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
            importSocketFileV.doSubmits(allFormId, $(this));
        });
        $(".inpFilePhoto").on("change", "input[type='file']", importSocketFileV.showFileName);
        $(".inpFilePhoto").on('click', function () {
            $("#excelPath").val('');
            $(".fileNameShow").html('');
        })
    })

})($, window);