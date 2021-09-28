(function(window, $){
    var Interval = "";
    OriginalModels={
        importProgress: function(){
             Interval = setInterval(function(){
                $.ajax({
                    type:"get",
                    ansync:false,
                    url:"/clbs/talkback/intercomplatform/intercommodel/importProgress",
                    data:null,
                    success: function(data){
                        data= JSON.parse(data);
                        console.log(data);
                        var progress = data.progress;
                        var total = data.total;
                        var move = parseInt(progress/total*100);
                        $("#progress").animate({left:move+'%'});
                        $("#progress").html(progress);
                        $("#total").html(total);
                        $(".progress-bar").attr("aria-valuenow", parseInt(move)).css("width", parseInt(move) + "%");
                        if(progress == total && progress != 0){
                            clearInterval(Interval);
                        }
                    }
                });
            },1000);
            $.ajax({
                type:"post",
                ansync:false,
                url:"/clbs/talkback/intercomplatform/intercommodel/synchronizeOriginalModel",
                data:null,
                success: function(data){
                    console.log(data);
                }
            });
        }
    };
    $(function(){
        OriginalModels.importProgress();
        $("#commonWin").on('hidden.bs.modal', function () {
            clearInterval(Interval);
        })
    })
})(window,$);