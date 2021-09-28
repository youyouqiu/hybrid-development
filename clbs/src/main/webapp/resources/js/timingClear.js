(function (window, $){
    var myTable;
    var checkedList='1,2,3,4,5';
    var type='';
    timingClear = {
        init: function (dataTableId){
            myTable = $(dataTableId).DataTable({
                "destroy": true,
                "dom": 'tiprl',// 自定义显示项
                "lengthChange": true,// 是否允许用户自定义显示数量
                "bPaginate": false, // 翻页功能
                "bFilter": false, // 列筛序功能
                "searching": true,// 本地搜索
                "ordering": false, // 排序功能
                "Info": true,// 页脚信息
                "autoWidth": true,// 自动宽度
                "stripeClasses": [],
                "pageLength": 10,
                "lengthMenu": [10, 20, 50, 100, 200],
                "pagingType": "simple_numbers", // 分页样式
                "dom": "t" + "<'row'<'col-md-2 col-sm-12 col-xs-12 noPadding'l><'col-md-4 col-sm-12 col-xs-12 noPadding'i><'col-md-6 col-sm-12 col-xs-12 noPadding'p>>",
                "oLanguage": {// 国际语言转化
                    "oAria": {
                        "sSortAscending": " - click/return to sort ascending",
                        "sSortDescending": " - click/return to sort descending"
                    },
                    "sLengthMenu": "显示 _MENU_ 记录",
                    "sInfo": "当前显示 _START_ 到 _END_ 条，共 _TOTAL_ 条记录。",
                    "sZeroRecords": "我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？",
                    "sEmptyTable": "我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？",
                    "sLoadingRecords": "正在加载数据-请等待...",
                    "sInfoEmpty": "当前显示0到0条，共0条记录",
                    "sInfoFiltered": "（数据库中共为 _MAX_ 条记录）",
                    "sProcessing": "<img src='../resources/user_share/row_details/select2-spinner.gif'/> 正在加载数据...",
                    "sSearch": "模糊查询：",
                    "sUrl": "",
                    "oPaginate": {
                        "sFirst": "首页",
                        "sPrevious": " 上一页 ",
                        "sNext": " 下一页 ",
                        "sLast": " 尾页 "
                    }
                },
                "order": [
                    [0, null]
                ],// 第一列排序图标改为默认
                "columns": [
                    { "width": "3%" },
                    { "width": "17%" },
                    { "width": "20%" },
                    { "width": "60%" },
                ]

            });
            var url = '/clbs/m/dataClean/list';
            json_ajax("POST", url, "json", true, null, timingClear.getCallback);
        },

        getCallback: function (data){
            if(data.success){
                if(data.obj != null && data.obj.length != 0){
                    var value = [];
                    var dataListArray = [];
                    for(var i = 2; i < 5; i++) {
                        var obj = {
                            dataType: timingClear.handleName(i),
                            clearRules: timingClear.handleRules(i, data.obj),
                        };
                        value.push(obj);
                    }

                    for(var i = 0; i < value.length; i++) {
                        var number = i + 1;
                        var dataList = [
                            '<input type="checkbox" name="subChk" checked="true" value="'+ number +'" onclick="timingClear.subChkChange(this)" />',
                            timingClear.handleButton(number),
                            value[i].dataType,
                            value[i].clearRules
                        ];
                        dataListArray.push(dataList);
                    }
                    console.log('dataListArray', dataListArray);
                    timingClear.reloadData(dataListArray);
                }
            }
        },

        handleButton: function (number){
            var result = '';
            result += '<button  data-value="'+number+'"  type="button" onclick="timingClear.editRules(this)" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;&ensp;';
            result += '<button  data-value="'+number+'" type="button" onclick="timingClear.restoreDefault(this)"  class="deleteButton editBtn  disableClick"><i class="fa fa fa-trash-o"></i>恢复默认</button>';
            return result;
        },

        handleName: function (index){
          switch (index) {
              case 0:
                  return '定位数据';
              case 1:
                  return '报警数据';
              case 2:
                  return '多媒体数据';
              case 3:
                  return '日志';
              case 4:
                  return '视频抽查记录表';
          }
        },

        handleRules: function (index, data){
            switch (index) {
                case 0:
                    return '存储时长超过' + data.positionalTime + '月';
                case 1:
                    return '存储时长超过' + data.alarmTime + '月';
                case 2:
                    return '存储时长超过' + data.mediaTime + '月';
                case 3:
                    return '存储时长超过' + data.logTime + '月';
                case 4:
                    return '存储时长超过' + data.spotCheckTime + '月';

            }
        },

        //加载列表数据
        reloadData: function (dataList) {
            var currentPage = myTable.page();
            myTable.clear();
            myTable.rows.add(dataList);
            myTable.page(currentPage).draw(true);
            myTable.search('', false, false).page(currentPage).draw();
        },

        editRules: function (event) {
            if($("#isAdmin").val() === 'false'){
                layer.msg('只有admin才有权限');
                return;
            }

            type = $(event).attr('data-value');
            $("#month").val('1');
            $("#editRulesModal").modal('show');
        },

        //定时清理
        clearData: function () {
            if($("#isAdmin").val() === 'false'){
                layer.msg('只有admin才有权限');
                return;
            }

            if(checkedList == ''){
                layer.msg('请选择清理对象');
                return;
            }
            $("#clearModal").modal('show');
        },
        //全选
        checkAllClick: function () {
            var subChk = $(this).closest('table').find("input[name='subChk']");
            subChk.prop("checked", this.checked);
            checkedList = '';

            var checkSubChk = $(this).closest('table').find("input[name='subChk']:checked");
            checkSubChk.each(function () {
                checkedList += $(this).val() + ','
            });
        },
        //单选
        subChkChange: function (event){
            var curTable = $(event).closest('table');
            var subChk = curTable.find("input[name='subChk']");
            curTable.find(".checkAll").prop(
                "checked",
                subChk.length == subChk.filter(":checked").length ? true
                    : false);
             checkedList = '';
            curTable.find("input[name='subChk']:checked").each(function () {
                checkedList+= $(this).val() + ','
            });
        },
        //提交
        doSubmit: function () {
            if(!$("#clearCheck").prop('checked')){
                $("#messages").show();
                return;
            }

            if($('#time').val() == ''){
                layer.msg('请选择定时清理时间');
                return;
            }

            var value = $("#time").val().split("");
            var newValue = '';
            for(var i = 0; i < value.length; i++) {
                if(i == 0 && value[i] == 0) continue;
                if(i == 3 && value[i] == 0) continue;
                newValue += value[i];
            }

            var url = '/clbs/m/dataClean/saveTime';
            var param = {
                time: newValue,
                cleanType: checkedList
            };
            json_ajax("POST", url, "json", true, param, function (data){
                if(data.success){
                    layer.msg('保存成功',{time:2000});
                    $("#clearCheck").attr('checked', false);
                    $("#clearModal").modal('hide');
                }
            });
        },

        editSubmit: function () {
            var url = '/clbs/m/dataClean/setting';
            var param = {
                type: type,
                value: $("#month").val()
            };
            json_ajax('POST', url, 'json', true, param, function (data){
                if(data.success){
                    layer.msg('保存成功');
                    $("#editRulesModal").modal('hide');
                    timingClear.refreshTable();
                }else{
                    layer.msg(data.msg);
                    return;
                }
            });
        },

        messagesChange: function () {
            if($("#clearCheck").prop('checked')){
                $("#messages").hide();
            }
        },

        //刷新
        refreshTable: function () {
            var url = '/clbs/m/dataClean/list';
            json_ajax("POST", url, "json", true, null, timingClear.getCallback);
        },

        //恢复默认
        restoreDefault: function (event) {
            var type = $(event).attr('data-value');
            var url = '/clbs/m/dataClean/default';
            json_ajax('POST', url, 'json', true, {type: type}, function (data) {
                if(data.success){
                    layer.msg('恢复成功');
                    $("#editRulesModal").modal('hide');
                    timingClear.refreshTable();
                }else{
                    layer.msg(data.msg);
                    return;
                }
            })
        }
    };

    $(function (){
        timingClear.init("#dataTable");
        $("#time").val('00:00');
        laydate.render({
            elem: '#time'
            ,type: 'time'
            ,format: 'HH:mm',
        });

        $("#clearClick").bind('click', timingClear.clearData);
        $(".checkAll").bind('click', timingClear.checkAllClick);
        $("#doSubmit").bind('click', timingClear.doSubmit);
        $("#clearCheck").bind('click', timingClear.messagesChange);
        $("#editSubmit").bind('click', timingClear.editSubmit)
    });
})(window, $);