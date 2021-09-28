(function (window, $) {
    var addthirdplatform;
    addthirdplatform = {
        doSubmit: function () {
            if (addthirdplatform.addValidate()) {
                $("#doSubmitAdd").prop('disabled', true);
                addHashCode1($("#addForm"));
                $("#addForm").ajaxSubmit(function (result) {
                    var data = JSON.parse(result);
                    if(data.success){
                        layer.msg('数据保存成功');
                        $("#commonWin").modal("hide");
                        myTable.refresh();
                    }else{
                        layer.msg(data.msg);
                    }
                    $("#doSubmitAdd").prop('disabled', false);
                });
            }
        },
        //校验
        addValidate: function () {
            return $("#addForm").validate({
                rules: {
                    name: {
                        required: true,
                    },
                    number: {
                        required: true,
                    },
                    longitude: {
                        required: true,
                        number: true,
                        range: [0, 180],
                        customize: $("#lng").val()
                    },
                    latitude: {
                        required: true,
                        number: true,
                        range: [0, 90],
                        customize: $("#lat").val()
                    },
                },
                messages: {
                    name: {
                        required: '站点名称不能为空'
                    },
                    number: {
                        required: '站点编号不能为空'
                    },
                    longitude: {
                        required: '站点经度不能为空',
                        number: '站点经度必须为数字',
                        range: '站点经度取值范围0-180',
                        customize: '站点经度只能保留6位小数',
                    },
                    latitude: {
                        required: '站点纬度不能为空',
                        number: '站点纬度必须为数字',
                        range: '站点纬度取值范围0-90',
                        customize: '站点纬度只能保留6位小数',
                    },
                }
            }).form();
        },
        init: function () {
            var map = new AMap.Map('stationMannageMap', {
                zooms: [8, 18], //级别
                resizeEnable: true
            });
            AMap.plugin(['AMap.ToolBar', 'AMap.Scale'], function () {
                map.addControl(new AMap.ToolBar());
                map.addControl(new AMap.Scale());
            })
            //输入提示
            var autoOptions = {
                input: "tipinput"
            };
            var auto = new AMap.Autocomplete(autoOptions);
            var placeSearch = new AMap.PlaceSearch({
                map: map
            }); //构造地点查询类
            AMap.event.addListener(auto, "select", select); //注册监听，当选中某条记录时会触发
            function select(e) {
                placeSearch.setCity(e.poi.adcode);
                placeSearch.search(e.poi.name); //关键字查询查询
            }
            map.on('click', function (ev) {
                // 触发事件的地理坐标，AMap.LngLat 类型
                var lnglat = ev.lnglat;
                $('#lng').val(lnglat.lng);
                $('#lat').val(lnglat.lat);
            });


        },
    }
    $(function () {
        $('input').inputClear();
        addthirdplatform.init();
        $("#doSubmitAdd").bind('click', addthirdplatform.doSubmit);

        jQuery.validator.addMethod("customize",function(value, element){
            var returnVal = true;
            var val = value.split('.');
            if(val.length == 2){
                if(val[1].length > 6){
                    returnVal = false;
                    return false;
                }
            }
            return returnVal;
        });
    })
})(window, $);