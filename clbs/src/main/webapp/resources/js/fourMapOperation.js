//挂载到全局
(function (window) {
    "use strict"
    var allMarker = []
    var overlay
    var map
    var allTool
    var editTool
    var resizeFn = function () {
        $('#fourMapContainer').css({height:$('#MapContainer').css('height')})
    }
    var fakeMap ={
        init:function () {},
        destroy:function () {},
        addMarker:function () {},
        gloabl:function () {},
        moveMarker:function () {},
        setCenter:function () {},
        delOneMarker:function () {},
        delAllMarker:function () {},
        rectZoomIn:function () {},
        rectZoomOut:function () {},
        caluDistance:function () {},
        caluArea:function () {},
        print:function () {},
        drawComplete:function () {},
        setZoomAndCenter:function () {},
    }
    window.fourMapOperation = {
        //初始化
        init:function(mapObj) {
            var height = $('#MapContainer').css('height')
            $('#fourMapContainer').css({height:height,position:'relative'})
            setTimeout(function () {
                $('#fourMapContainer').hide()
                $('.EVPanZoomBar').css({left:"60px"})
            },0)
            window.addEventListener('resize',resizeFn,{ passive: false })
            if(typeof EV == 'undefined'){
                window.fourMapOperation = fakeMap
                return
            }
            // map = new EV.Map("fourMapContainer");
            map = mapObj;
            if(window.initCenterPoint){
                var point = new EV.LngLat(initCenterPoint.lng,initCenterPoint.lat);
                map.setCenter(point,18);
            }
            map.addControl(new EV.MapControl());
            map.addControl(new EV.ScaleControl());
            map.addControl(new EV.OverviewMapControl());
            //隐藏地图缩放控件
            $("#olControlOverviewMapMaximizeButton").hide()
            $("#OpenLayers_Control_minimizeDiv").hide()
            $(".olControlOverviewMapElement").css({padding:'10px',border:'1px solid #bfbfbf',backgroundColor: '#ffffff'})
            allTool = map.toolbar;

            //添加自定义画圆工具
            var circleTool = allTool.addTool("addcircle",{
                text:"画圆",  //显示的文字
                name:"myAddCircle"
            },{
                pointRadius: 4,
                graphicName: "star",
                fillColor: '#00b0ff',
                strokeColor: '#00b0ff',
                fillOpacity:0.4,
                strokeWidth: 2,   //线条宽度
                strokeOpacity: 1, //线条透明度
            });
            circleTool.addEventListener('done', function (e) {
                fourMapOperation.drawComplete(e);
                //停止此工具
                this.deactivate();
                //不添加到地图上
                return false;
            });
            //添加自定义画方工具
            var rectTool = allTool.addTool("addrect",{
                text:"画方",  //显示的文字
                name:"myAddRect"
            },{
                pointRadius: 4,
                graphicName: "star",
                fillColor: '#00b0ff',
                strokeColor: '#00b0ff',
                fillOpacity:0.4,
                strokeWidth: 2,
                strokeOpacity: 1,
            });
            rectTool.addEventListener('done', function (e) {
                fourMapOperation.drawComplete(e);
                //停止此工具
                this.deactivate();
                //不添加到地图上
                return false;
            });

            editTool = allTool.getToolsByName('edit')[0]
        },
        //销毁
        destroy:function() {
            map = null;
            $('#fourMapContainer').empty();
        },
        //添加单个点
        addMarker:function (lngLat,info) {
            fourMapOperation.delOneMarker(info[0])
            var point = new EV.LngLat(lngLat[0],lngLat[1])
            var marker = new EV.Marker(point,{
                externalGraphic:"../../resources/img/vico/" + info[6],
                graphicHeight:30,
                graphicWidth:48,
                rotation:info[4] + 270,
                label:info[1],
                fontColor : "#000",
                fontSize : "12px",
                labelYOffset :-38,
                labelAlign :"l",
                labelXOffset :18,
            })
            // var plateMarker = new EV.Marker(point,{
            //     label:info[1],
            //     fontColor : "#000",
            //     fontSize : "12px",
            //     labelFillColor:'#000',
            //     labelYOffset :-43,
            //     labelAlign :"l",
            //     labelXOffset :36,
            //     externalGraphic:"../../resources/img/carNameShowRD.svg",
            //     graphicHeight:40,
            //     graphicWidth:100,
            //     graphicXOffset:20,
            //     graphicYOffset:25,
            //
            // })
            // marker.addEventListener('click',amapOperation.markerClick);
            allMarker.push({id:info[0],marker:marker})
            map.addOverlay(marker);
            // allMarker.push({id:info[0],marker:plateMarker})
            // map.addOverlay(plateMarker);
        },
        //全国
        gloabl: function(){
            map.zoomToChina()
        },
        //移动点
        moveMarker:function(id,newPosition,rotation){
            if(!id || !newPosition) return;
            var index = allMarker.findIndex(function (item) {
                return item.id == id
            })
            if(allMarker[index]){
                var point = new EV.LngLat(newPosition[0],newPosition[1])
                allMarker[index].marker.setLngLat(point)
                if(rotation){
                    allMarker[index].marker.setRotation(rotation)
                }
            }
        },
        //设置中心点
        setCenter:function(position){
            var point = new EV.LngLat(position[0],position[1])
            map.setCenter(point)
        },
        //删除单个点
        delOneMarker:function (id) {
            if(!id) return;
            // 兼容ie数组没有findIndex方法
            if (!Array.prototype.findIndex) {
                Object.defineProperty(Array.prototype, 'findIndex', {
                    value: function(predicate) {
                        if (this == null) {
                            throw new TypeError('"this" is null or not defined');
                        }
                        var o = Object(this);
                        var len = o.length >>> 0;
                        if (typeof predicate !== 'function') {
                            throw new TypeError('predicate must be a function');
                        }
                        var thisArg = arguments[1];
                        var k = 0;
                        while (k < len) {
                            var kValue = o[k];
                            if (predicate.call(thisArg, kValue, k, o)) {
                                return k;
                            }
                            k++;
                        }
                        return -1;
                    }
                });
            }

            var index = allMarker.findIndex(function (item) {
                return item.id == id
            })
            if(allMarker[index]){
                allMarker[index].marker.remove()
                allMarker.splice(index,1)
            }
        },
        //删除所有点
        delAllMarker:function () {
            allMarker.forEach(function (item) {
                item.marker.remove()
            })
            allMarker = []
        },
        //添加polygon overlay

        //拉框放大
        rectZoomIn:function(){
            map.t_zoomin();
        },
        //拉框缩小
        rectZoomOut:function () {
            map.t_zoomout();
        },
        //距离测算
        caluDistance: function () {
            map.t_mlength();
        },
        //面积测算
        caluArea: function (type) {
            map.t_marea();
            return
            // if(overlay) overlay.remove();
            // editTool.deactivate()
            // if(type == 'circle'){
            //     allTool.activateToolByName("myAddCircle")
            // }else if(type == 'rectangle'){
            //     allTool.activateToolByName("myAddRect")
            // }
        },
        //打印
        print:function () {
            // map.t_print();
            // map.t_screenshot();
        },
        //绘制结束事件
        drawComplete:function(e,type){
            console.log('绘制完成')
            console.log(e)
            overlay = e.overlay
            map.addOverLay(overlay)
            editTool.activate()
        },
        //设置中心点和缩放级别
        setZoomAndCenter: function (zoom,center) {
            map.setLonLatZoom(center.lng,center.lat,zoom)
        },
        // 鹰眼展示
        toggleSmallMap: function () {
            $(".olControlOverviewMapElement").toggle()
        }
    }
})(window)