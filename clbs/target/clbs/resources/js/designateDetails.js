(function(window,$){
    var designateDetails = {
        init:function(){
            designateDetails.getPeopleNames();
            designateDetails.getDateDuplicateType();
        },
        getPeopleNames:function(){
            var peopleNames = $('#peopleNames').data('value');
            var name = [];
            for(var i=0;i<peopleNames.length;i++){
                var item = peopleNames[i];
                var peopleId = item.peopleId;
                if (peopleId === undefined || peopleId === null || peopleId === "") {
                    continue;
                }
                name.push(item.peopleName);
            }
            $('#peopleNames').text(name.join(','));
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
        //获取任务详情
        showTaskDetail: function(){
            var taskId = $('#taskList').data('value');

            var paramer = {
                "id": taskId
            };
            json_ajax("POST", '/clbs/a/taskManagement/getTaskDetail', "json", true, paramer,designateDetails.taskDetailCB);
        },
        taskDetailCB: function(data){
            var detailDom = $('#schedule-list');
            if(data.success){
                var taskItems = data.obj.taskItems;
                designateDetails.taskDetailDom(taskItems);

                if(detailDom.is(':hidden')){
                    detailDom.slideDown();
                }else{
                    detailDom.slideUp();
                }
            }else{
                layer.msg(data.msg)
            }
        },
        taskDetailDom:function(data){
            var html='';
            for(var i=0,len=data.length;i<len;i++){
                var item = data[i];
                html += '<li class="item">\n' +
                    '                        <div class="zw-title clearfix">\n' +
                    '                            <label class="control-label pull-left"><label\n' +
                    '                                    class="text-danger">*</label> 任务项 </label>\n' +
                    '                        </div>\n' +
                    '                        <div class="form-group">\n' +
                    '                            <label class="col-md-2 control-label"><label\n' +
                    '                                    class="text-danger">*</label> 围栏： </label>\n' +
                    '                            <div class="col-md-10 has-feedback">\n' +
                    '                                <input value="'+item.fenceName+'" readonly="readonly" type="text" class="form-control inputStyleHide" />\n' +
                    '                            </div>\n' +
                    '                        </div>\n' +
                    '\n' +
                    '                        <div class="form-group">\n' +
                    '                            <label class="col-md-2 control-label"><label\n' +
                    '                                    class="text-danger">*</label> 开始时间： </label>\n' +
                    '                            <div class="col-md-4">\n' +
                    '                                <input value="'+item.startTime+'" readonly="readonly" type="text" class="form-control inputStyleHide" />\n' +
                    '                            </div>\n' +
                    '                            <label class="col-md-2 control-label"><label\n' +
                    '                                    class="text-danger">*</label> 结束时间： </label>\n' +
                    '                            <div class="col-md-4">\n' +
                    '                                <input value="'+item.endTime+'" readonly="readonly" type="text" class="form-control inputStyleHide" />\n' +
                    '                            </div>\n' +
                    '                        </div>\n' +
                    '\n' +
                    '                        <div class="form-group">\n' +
                    '                            <label class="col-md-2 control-label"> 关联报警： </label>\n' +
                    '                            <div class="col-md-10">\n' +
                    '                                <label class="checkbox-inline">'+(item.relationAlarm.indexOf('1') != -1 ? '<input checked class="relationAlarm" value="1" type="checkbox" disabled/> 任务未到岗</label>' : '<input class="relationAlarm" value="1" type="checkbox" disabled/> 任务未到岗</label>') +
                    '                                <label class="checkbox-inline">'+(item.relationAlarm.indexOf('2') != -1 ? '<input checked class="relationAlarm" value="2" type="checkbox" disabled/> 任务离岗</label>' : '<input class="relationAlarm" value="1" type="checkbox" disabled/> 任务离岗</label>') +
                    '                            </div>\n' +
                    '                        </div>\n' +
                    '                    </li>';

                $('#schedule-list').html(html);
            }

        }
    };

    $(function(){
        designateDetails.init();
        $('#taskDetail').on('click',designateDetails.showTaskDetail);
    })
})(window,$);