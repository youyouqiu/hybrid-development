var Map = function (mapId, options, dependency) {
    this.dependency = dependency;
    this.map = new AMap.Map(mapId, {
        resizeEnable: true,		//是否监控地图容器尺寸变化
        zoom: 18,				//地图显示的缩放级别
    });
    AMap.plugin(['AMap.ToolBar', 'AMap.Scale', 'AMap.HawkEye'], function () {
        this.map.addControl(new AMap.ToolBar());
        this.map.addControl(new AMap.Scale());
    }.bind(this));

    //地图工具实例
    this.mouseTool = new AMap.MouseTool(this.map);
    // this.mouseTool.on("draw", Map.prototype.createSuccess.bind(this));
    // 卫星地图
    this.satellLayer = new AMap.TileLayer.Satellite();
    this.satellLayer.setMap(this.map);
    this.satellLayer.hide();
    //实时路况
    this.realTimeTraffic = new AMap.TileLayer.Traffic();
    this.realTimeTraffic.setMap(this.map);
    this.realTimeTraffic.hide();
    // 实例化3D楼块图层
    this.buildings = new AMap.Buildings();
    this.buildings.setMap(this.map);
    //谷歌地图
    this.googleMapLayer = null;

    /*地图工具绑定*/
    $('#toolOperateClick').on('click', '.fenceA', Map.prototype.toolClickArea.bind(this));
    $('#mapDropSettingMenu').on('change', '.monitoringSelect', Map.prototype.mapDropdownSettingClickFn.bind(this))
    $('#measurementMenu').on('click', '.item', Map.prototype.distanceMeasuremenEvent.bind(this))
}

/**
 * 地图工具
 */
Map.prototype.toolClickArea = function (e) {
    var id = e.currentTarget.id,
        $id = $('#'+id);
    $id.toggleClass('active').siblings('.fenceA').removeClass('active');
    $('.dropDown').hide();

    var isActive = $id.hasClass('active');
    this.mouseTool.close(true);
    switch (id){
        case 'previewImage'://预览图片
            this.htmlTocanvas();
            break;
        case 'queryClick'://区域查询(todo)
            if(isActive){
                this.mouseTool.rectangle();
            }
            break;
        case 'magnifyClick'://拉框放大
            if(isActive){
                this.mouseTool.rectZoomIn();
            }
            break;
        case 'shrinkClick'://拉框缩小
            if(isActive){
                this.mouseTool.rectZoomOut();
            }
            break;
        case 'displayClick'://显示设置
            var disSetMenu = $('#disSetMenu');
            if(isActive){
                disSetMenu.slideDown();
            }else{
                disSetMenu.slideUp();
            }
            break;
        case 'mapDropSetting'://地图设置
            var mapDropSettingMenu = $('#mapDropSettingMenu');
            if(isActive){
                mapDropSettingMenu.slideDown();
            }else{
                mapDropSettingMenu.slideUp();
            }
            break;
        case 'countClick'://量算
            var measurementMenu = $('#measurementMenu');
            if(isActive){
                measurementMenu.slideDown();
            }else{
                measurementMenu.slideUp();
            }
            break;
        default:
            break;
    }
};
Map.prototype.htmlTocanvas = function(){
    if (!window.Promise) {
        layer.msg('本浏览器暂不支持该功能!');
        return;
    }
    layer.load(2);

    setTimeout(function () {
        html2canvas(document.querySelector(".amap-maps"), {
            useCORS: true,
            backgroundColor: null,
        }).then(function (canvas) {
            layer.closeAll('loading');
            // 将整个页面图片转成Base64位
            var dataURL = canvas.toDataURL("image/jpg");
            $('#picImg').attr('src', dataURL);
            localStorage.setItem('printPicBase64', dataURL);
            $('#mapPicModal').modal('show')
        });
    }, 10)
}
Map.prototype.mapDropdownSettingClickFn = function (e) {
    var id = e.currentTarget.id;
    var checked = $('#'+id).prop('checked');
    console.log('id',id)
    switch (id){
        case 'realTimeRCInput'://路况开关
            if(checked){
                $("#googleMapInput").prop("checked", false);
                if(this.googleMapLayer){
                    this.googleMapLayer.setMap(null);
                }
                this.realTimeTraffic.show();
            }else{
                this.realTimeTraffic.hide();
            }
            break;
        case 'defaultMapInput'://卫星地图
            if(checked){
                this.buildings.setMap(null);
                $("#googleMapInput").prop("checked", false);
                if (this.googleMapLayer) {
                    this.googleMapLayer.setMap(null);
                }
                this.satellLayer.show();
            }else{
                this.satellLayer.hide();
                this.buildings.setMap(this.map);
            }
            break;
        case 'googleMapInput'://谷歌地图
            if(checked){
                $("#realTimeRCInput,#defaultMapInput").prop("checked", false);
                this.realTimeTraffic.hide();
                this.satellLayer.hide();

                this.googleMapLayer = new AMap.TileLayer({
                    tileUrl: 'http://mt{1,2,3,0}.google.cn/vt/lyrs=m@142&hl=zh-CN&gl=cn&x=[x]&y=[y]&z=[z]&s=Galil',
                    zIndex: 100 //设置Google层级与高德相同h;
                });
                this.googleMapLayer.setMap(this.map);
                this.buildings.setMap(this.map);
            }else{
                this.googleMapLayer.setMap(null);
                this.buildings.setMap(null);
            }
            break;
        default:
            break;
    }
}
Map.prototype.distanceMeasuremenEvent = function (e) {
    var id = e.currentTarget.id;
    var $id = $('#'+id);
    $id.toggleClass('active').siblings('.item').removeClass('active');

    var isActive = $id.hasClass('active');
    mouseTool.close(true);
    switch (id){
        case 'distanceMeasuremenLab'://距离量算
            if(isActive){
                this.mouseTool.rule({zIndex: 50000, bubble: true, map: this.map});
            }else{

            }
            break;
        case 'areaMeasurementLab'://面积量算
            if(isActive){

            }else{

            }
            break;
        default:
            break;
    }
}

