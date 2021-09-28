/**
 * 文件导入
 */
(function ($, window) {
    var filePath, arr, fileName;
    var isSocketPush = 0; // 判断socket是否开始推送
    var isRender = true;  //判断是否渲染进度条
    var pushIng = false;
    var schedule = 0; //校验文件进度
    var interval;

    window.msgImport = {
        init: function (){
            $.ajax({
                type: 'GET',
                url: '/clbs/m/import/CONFIG/progress',
                dataType: 'json',
                success: function (data) {
                    var result = data.obj;
                    console.log('result', result);
                    if(result.length == 0) return;
                    for(var i = 0; i < result.length; i++){
                        var item = result[i];
                        msgImport.statusChange(item.status, i, item.ratio);
                    }
                }
            });
        },

        //Excel文件导入到数据库中
        importEmp: function () {
            //检验导入的文件是否为Excel文件
            var formId = $(".importVehicle.active").attr("id");
            var excelPath = $("#"+formId).find(".excelPath").val();
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
        //显示文件名称
        showFileName: function () {
            var formId = $(".importVehicle.active").attr("id");
            filePath = $("#"+formId).find(".excelPath").val();
            arr = filePath.split('\\');
            fileName = arr[arr.length - 1];
            if(fileName){
                $('.fileIcon').show();
            }
            $(".fileNameShow span").html(fileName);
        },
        //提交
        doSubmits: function (importFormId, $this) {
            isRender = true;
            $("#" + importFormId).ajaxSubmit(function (message) {
                setTimeout(function () {
                    pushIng = false;
                    console.log('true');
                },500);

                $this.prop('disabled', false);
                var data = JSON.parse(message);

                if(data.success){
                    var list = [0, 1, 2, 3, 4, 5];
                    for(var i=0; i<list.length; i++) {
                        var key = list[i];
                        $(".stage" + key).attr("aria-valuenow", 16.6).css("width",  "16.6%");
                        $(".arrow_stage" + key).removeClass('loading').attr('src','../../../resources/img/success.png');
                        $(".nodeText_stage" + key).html(msgImport.handleChangeSuccess(key));
                    }
                    $("#success").attr('src','../../../resources/img/success.png');

                    setTimeout(function () {
                        $("#commonSmWin").modal("hide");
                        if (typeof myTable != 'undefined') {
                            myTable.requestData();
                        }
                    },1000);

                }else{
                    clearInterval(interval);
                    if(data.msg == null){
                        setTimeout(function () {
                            if(isSocketPush == 0){
                                $('.stage5').removeClass('progress-bar-progress').addClass('progress-bar-error');
                                $(".arrow_stage5").removeClass('loading').attr('src','../../../resources/img/error.png');
                                $(".nodeText_stage5").html(msgImport.handleChangeError(5)).css('color','red');
                                $("#errorMessage span").html(msgImport.handleChangeError(5));
                                $("#errorMessage a").attr('href','/clbs/m/import/CONFIG/error').css('display','inline');
                            }
                        },500);
                    }else {
                        isRender = false;
                        // var msg = '<div style="padding: 10px 30px">data.msg</div>';
                        layer.open({
                            title:'提示',
                            content: data.msg,
                            btn:['知道了'],
                            yes:function (){
                                layer.closeAll();
                            }
                        });
                        $(".stage5").css("transition", 'none').attr("aria-valuenow", 0).css("width","0%");
                        if($(".arrow_stage5").hasClass('loading')){
                            $(".arrow_stage5").removeClass('loading').attr('src', '../../../resources/img/rightArrow.png');
                        }
                    }
                }
            });
        },
        //进度条渲染
        setProgressBar: function (body) {
            var progress =  body.progress;
            if(progress.length == 0) return;
            if(body.module !== 'CONFIG') return;
            if(!isRender) return;

            pushIng = true;
            isSocketPush += 1;
            clearInterval(interval);

            $(".arrow_stage5").removeClass('loading').attr('src','../../../resources/img/success.png');
            $(".stage5").css('transition','none').attr("aria-valuenow", 16).css("width", "16.6%");
            $(".nodeText_stage5").html(msgImport.handleChangeSuccess(5));

            for(var i=0; i<progress.length; i++){
                var item = progress[i];
                if(item.stage == 0){
                    msgImport.statusChange(item.status,0,item.ratio);
                }else if(item.stage == 1){
                    msgImport.statusChange(item.status,1,item.ratio);
                }else if(item.stage == 2){
                    msgImport.statusChange(item.status,2,item.ratio);
                }else if(item.stage == 3){
                    msgImport.statusChange(item.status,3,item.ratio);
                }else if(item.stage == 4){
                    msgImport.statusChange(item.status,4,item.ratio);
                }

                if(item.status == 2){
                    return;
                }
            }
        },

        statusChange: function(status, i, ratio){
            if(status == 0){ //进行中
               $(".arrow_stage" + i).addClass('loading').attr('src','../../../resources/img/loading.png');
            }

            if(status == 1){ //成功
                $(".arrow_stage" + i).removeClass('loading').attr('src','../../../resources/img/success.png');
                $(".nodeText_stage"+ i).html(msgImport.handleChangeSuccess(i));
            }

            if(status == 2){ //失败
               $('.stage' + i).removeClass('progress-bar-progress').addClass('progress-bar-error');
               $(".arrow_stage" + i).removeClass('loading').attr('src','../../../resources/img/error.png');
               $(".nodeText_stage" + i).html(msgImport.handleChangeError(i)).css('color','red');
               $("#errorMessage span").html(msgImport.handleChangeError(i));
               $("#errorMessage a").attr('href','/clbs/m/import/CONFIG/error').css('display','inline');
            }

            msgImport.scheduleControl(ratio,i);
        },

        scheduleControl: function(ratio,i){
            if(ratio == 1){
                $(".stage" + i).css("transition", 'width 0.6s ease').attr("aria-valuenow", 16.6).css("width",  "16.6%");
            }else{
                var num = (0.166 * ratio) * 100;
                $(".stage" + i).css("transition", 'width 0.6s ease').attr("aria-valuenow", num).css("width",  num + "%");
            }
        },

        //进度条重置
        progressBarReset: function () {
            var nodeList = [0, 1, 2, 3, 4, 5];
            for(var i=0; i<nodeList.length; i++){
                var item = nodeList[i];
                $('.stage'+ item).removeClass('progress-bar-error').addClass('progress-bar-progress');
                $(".stage"+ item).css('transition','none').attr("aria-valuenow", 0).css("width", "0%");
                $(".arrow_stage" + item).attr('src','../../../resources/img/rightArrow.png').removeClass('loading');
                $('.nodeText_stage'+ item).html(msgImport.handleChangeInit(item)).css('color',"#505050");
            }

            clearInterval(interval);
            isSocketPush = 0;
            schedule = 0;

            $("#success").attr('src','../../../resources/img/rightArrow.png');
            $("#errorMessage span").html('');
            $("#errorMessage a").attr('href','').css('display', 'none');
        },

        handleChangeError:function (type){
            switch (type) {
                case 0:
                    return "导入监控对象失败";
                case 1:
                    return "导入终端信息失败";
                case 2:
                    return "导入SIM卡信息失败";
                case 3:
                    return "导入分组信息失败";
                case 4:
                    return "导入绑定关系失败";
                case 5 :
                    return "校验文件失败";
            }
        },

        handleChangeSuccess: function (type){
            switch (type) {
                case 0:
                    return "导入监控对象成功";
                case 1:
                    return "导入终端信息成功";
                case 2:
                    return "导入SIM卡信息成功";
                case 3:
                    return "导入分组信息成功";
                case 4:
                    return "导入绑定关系成功";
                case 5 :
                    return "校验文件成功";
            }
        },

        handleChangeInit: function (type){
            switch (type) {
                case 0:
                    return "导入监控对象";
                case 1:
                    return "导入终端信息";
                case 2:
                    return "导入SIM卡信息";
                case 3:
                    return "导入分组信息";
                case 4:
                    return "导入绑定关系";
                case 5 :
                    return "校验文件";
            }
        },

        automatic: function () {
            $(".arrow_stage5").addClass('loading').attr('src','../../../resources/img/loading.png');
            interval = setInterval(function () {
                if(schedule >= 13) return;
                schedule += 0.9;
                $(".stage5").css("transition", 'width 0.6s ease').attr("aria-valuenow", schedule).css("width",  schedule + "%");
            },1000);
        },

        resetForm: function (){
            $("#excelPath").val('');
            $(".fileNameShow").show();
            $(".fileNameShow img").hide();
            $(".fileNameShow span").html('请选择要上传的Excel文件');
            msgImport.progressBarReset();
        }
    };

    $(function () {
        // msgImport.init();

        //提交
        $("#doSubmits").on("click", function () {
            //点击获取from表单id名称
            var formId = $(".importVehicle.active").attr("id");
            if (msgImport.importEmp() && msgImport.validates(formId)) {
                $(this).prop('disabled', 'disabled');
                msgImport.progressBarReset();
                msgImport.automatic();
                msgImport.doSubmits(formId, $(this));
            };
        });


        $("#close").on('click',function () {
            if(pushIng){
                var html = "<div style='padding: 10px 20px'>"+
                    '导入任务仍在后台进行,可再次点击 <span style="color:#47a7f9">操作菜单-导入按钮</span>，查看导入进度' +"</div>";

                layer.open({
                    title:'提示',
                    content: html,
                    btn:['知道了'],
                    yes:function (){
                        layer.closeAll();
                    }
                });
            }
        });

        $(".inpFilePhoto").on("change", "input[type='file']", msgImport.showFileName);
        $(".modal-dialog").css("width", "50%");

        $("#importForm").bind('click', msgImport.resetForm);
    });

})($, window);