(function(window,$){
    var peopleNames = $('#peopleNames').data('value');

    var scheduleDetails = {
        init:function(){
            $('#peopleNames').text(peopleNames.join(','));
            scheduleDetails.getDateDuplicateType();
        },
        getDateDuplicateType: function(){
            var list = $('#dateDuplicateType').val();
            var checkbox = $('input[name="week"]');

            $.each(checkbox,function(){
                if(list.indexOf($(this).val())!=-1){
                    $(this).prop('checked',true);
                }
            })
        },
    }

    $(function(){
        scheduleDetails.init();
    })
})(window,$)