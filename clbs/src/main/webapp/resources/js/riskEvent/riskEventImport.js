/**
 * 文件导入
 */
(function($,window){
	
	var filePath,arr,fileName;
	
    var msgImport = {
        //Excel文件导入到数据库中
        importEmp: function(){
            //检验导入的文件是否为Excel文件   
            var excelPath = document.getElementById("excelPath").value;   
            if(excelPath == null || excelPath == ''){   
                layer.msg("请选择要上传的Excel文件！", {move: false});
                return false;   
            }else{   
                var fileExtend = excelPath.substring(excelPath.lastIndexOf('.')).toLowerCase();    
                if(fileExtend == '.xls' || fileExtend == '.xlsx'){
                    return true;
                }else{   
                    layer.msg("文件格式需为'.xls' 或 '.xlsx' 的格式！", {move: false});
                    return false;   
                }   
            }   
        },
        validates: function(importFormId){
            return $("#"+importFormId).validate({
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
        doSubmits: function(importFormId, $this){
            if(msgImport.importEmp() && msgImport.validates(importFormId)){
                $('.progress').show();
                var index = 0;
                var successIcon = false;
                $this.attr('disabled','disabled');
                setInterval(function(){
                    index++;
                    if(index > 100){
                        return false;
                    };
                    if(successIcon){
                        $(".progress-bar").attr("aria-valuenow",100).css("width",100 + "%").text(100 + "%");
                    }else{
                        if(index >= 80){
                            $(".progress-bar").attr("aria-valuenow",80).css("width",80 + "%").text(80 + "%");
                        }else{
                            $(".progress-bar").attr("aria-valuenow",index).css("width",index + "%").text(index + "%");
                        }
                    }
                },50);
                $("#"+importFormId).ajaxSubmit(function(message) {
                	data = JSON.parse(message);
                	if(data.success){
	                    successIcon = true;
	                    setTimeout(function(){
	                    	$this.removeAttr('disabled');
	                        $("#commonSmWin").modal("hide");
	                        layer.alert(eval("["+message+"]")[0].msg,{
	                            id:"promptMessage"
	                        });
	                        myTable.refresh();
	                    },1000)
                	}else{
                		$('.progress').hide();
                		layer.msg(data.msg,{move:false});
                	}
                });
            }
        },
        //显示文件名称
        showFileName: function(){
            filePath = $("#excelPath").val();
            arr = filePath.split('\\');
            fileName = arr[arr.length-1];
            $(".fileNameShow").html(fileName);
        },
        
    }

    $(function(){
        $("#doSubmits").on("click",function(){
        	//点击获取from表单id名称
        	var allFormId = $(this).parents("form").attr("id");
        	msgImport.doSubmits(allFormId, $(this));
        });
        $(".inpFilePhoto").on("change","input[type='file']",msgImport.showFileName);
    })
    
})($,window)