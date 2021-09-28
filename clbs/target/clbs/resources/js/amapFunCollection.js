var overView;
var amapLayer;
var drawMouseTool;
var textMarker;
var circleEditor;
var drawRectangle;
var wms;
var fenceLayerSelect;
var amapFunCollection = {
    init: function () {
        overView = map.hawkEye({visible: false});
        if (overView) {
            map.addControl(overView);
        }

        amapLayer = map.imageLayer({
            url: 'https://amappc.cn-hangzhou.oss-pub.aliyun-inc.com/lbs/static/img/dongwuyuan.jpg',
            bounds: map.bounds(
                [116.327911, 39.939229],
                [116.342659, 39.946275]
            ),
            zooms: [2, 18],
            visible: false,
            map: map,
        });
        //
        // map.setLayers([map.tileLayer(), amapLayer]);

        wms = map.wmts({
            url: 'https://services.arcgisonline.com/arcgis/rest/services/Demographics/USA_Population_Density/MapServer/WMTS/',
            blend: false,
            tileSize: 256,
            params: {'Layer': '0', Version: '1.0.0', Format: 'image/png'},
            visible: false,
        });
        //
        wms.setMap(map);

        drawMouseTool = map.mouseTool(map, true);
        drawMouseTool.on('draw', function (e) {
            amapFunCollection.drawComplete(e);
        });
    },

    toggleOverViewShow: function () {
        overView.show();
        overView.open();
    },

    toggleOverViewHide: function () {
        overView.hide();
        overView.close();
    },

    layerOneShow: function () {
        amapLayer.show();
        map.setCenter([116.33719, 39.942384])
        map.setZoom(15);
    },

    layerOneHide: function () {
        amapLayer.hide();
    },

    layerTwoShow: function () {
        wms.show();
        map.setCenter([-99.241291, 39.51401])
        map.setZoom(4);
    },

    layerTwoHide: function () {
        wms.hide();
    },

    drawGraphics: function (e) {
        console.log(e);
        if (e.target.className != 'areaMeasurementList') {
            $('#drawGraphics p').removeClass('active');
            $(e.target).addClass('active');
            if (drawMouseTool) {
                drawMouseTool.close(true);
            }
            ;
            if (textMarker) {
                if (textMarker.labelMarker) {
                    map.remove([textMarker.labelMarker]);
                }
                map.remove([textMarker]);
                textMarker = null;
            }
            if (drawRectangle) {
                map.remove([drawRectangle]);
            }
            if (circleEditor) {
                circleEditor.close();
            }
            ;
            // amapFunCollection.removeCircleTool();
            // var type = e.target.dataset.type;
            var type = $(e.target).attr('data-type');
            if (type == 'circle') {
                amapFunCollection.init()
                drawMouseTool.circle({
                    fillColor: '#00b0ff',
                    strokeColor: '#80d8ff'
                });
            } else if (type == 'rectangle') {
                amapFunCollection.init()
                drawMouseTool.rectangle({
                    fillColor: '#00b0ff',
                    strokeColor: '#80d8ff'
                });
            }
        }
    },

    drawComplete: function (e) {
        drawMouseTool.close();
        var overlay = e.obj;
        var type = e.obj.CLASS_NAME;
        console.log(type);
        if (type == 'Overlay.Circle') {
            circleEditor = map.circleEditor(map, overlay);
            circleEditor.open();
            amapFunCollection.createCircleText(overlay);
            // 拖拽圆心
            circleEditor.on('move', function () {
                amapFunCollection.createCircleText(overlay);
            });
            // 半径调整
            circleEditor.on('adjust', function () {
                amapFunCollection.createCircleText(overlay);
            })
        } else if (type == 'Overlay.Rectangle') {
            var bounds = overlay.getBounds();
            drawMouseTool.close(true);
            drawRectangle = map.rectangle({
                bounds: bounds,
                fillColor: '#00b0ff',
                strokeColor: '#80d8ff',
                fillOpacity: 0.4,
                map: map
            });
            circleEditor = map.rectangleEditor(map, drawRectangle)
            circleEditor.open();
            amapFunCollection.createRectangleText(drawRectangle);
            // 半径调整
            circleEditor.on('adjust', function () {
                amapFunCollection.createRectangleText(drawRectangle);
            })
        }
    },

    // 创建显示圆的直径、周长和面积文本标注
    createCircleText: function (overlay) {
        var center = overlay.getCenter();
        if (map.lnglatTransToAmap) {// 将经纬度转换为高德的
            var newPoint = map.lnglatTransToAmap(center.lng, center.lat);
            center = {
                lng: newPoint[0],
                lat: newPoint[1]
            }
        }
        var radius = overlay.getRadius();
        var diameter = (radius * 2).toFixed(2);
        var perimeter = (diameter * 3.14159).toFixed(2);
        var area = (radius * radius * 3.14159).toFixed(2);

        var text = '<div class="text-marker">'
            + '<span>直径：' + diameter + 'm</span><br>'
            + '<span>周长：' + perimeter + 'm</span><br>'
            + '<span>面积：' + area + 'm²</span>'
            + '</div>';

        if (textMarker == null) {
            textMarker = map.marker({
                position: center,
                // text: text,
                offset: map.pixel(5, 5),
                map: map,
                label: {
                    content: text,
                    direction: 'right',
                    offset: map.pixel(0, 30),
                },
                icon: '/clbs/resources/img/destroy.png',
            });
            textMarker.on('click', function () {
                amapFunCollection.removeCircleTool();
            })
        } else {
            textMarker.setPosition(center);
            textMarker.setLabel({
                content: text,
            });
        }

        // var text = '<div class="text-marker">'
        //     + '<i class="draw-del-icon" onClick="amapFunCollection.removeCircleTool()"></i>'
        //     + '<span>直径：' + diameter + 'm</span><br>'
        //     + '<span>周长：' + perimeter + 'm</span><br>'
        //     + '<span>面积：' + area + 'm²</span>'
        //     + '</div>';
        //
        // if (textMarker == null) {
        //   textMarker = map.text({
        //     position: center,
        //     text: text,
        //     offset: map.pixel(75, 45),
        //     map: map
        //   });
        // } else {
        //   textMarker.setPosition(center);
        //   textMarker.setText(text);
        // }
    },

    // 创建显示矩形的周长和面积文本标注
    createRectangleText: function (overlay) {
        var paths = overlay.getBounds();
        var x, y, northPoint, southPoint;
        if (paths.northEast) {
            var northEast = paths.northEast;
            var southWest = paths.southWest;
            x = northEast.pos[0] - southWest.pos[0];
            y = northEast.pos[1] - southWest.pos[1];
            northPoint = {lng: paths.northEast.lng, lat: paths.northEast.lat};
            southPoint = {lng: paths.southWest.lng, lat: paths.southWest.lat};
        } else {
            x = map.lngLat(paths.Lq.lng, paths.Lq.lat).distanceTo(map.lngLat(paths.kq.lng, paths.Lq.lat));
            y = map.lngLat(paths.kq.lng, paths.kq.lat).distanceTo(map.lngLat(paths.kq.lng, paths.Lq.lat));
            northPoint = {lng: paths.Lq.lng, lat: paths.Lq.lat};
            southPoint = {lng: paths.kq.lng, lat: paths.kq.lat};
        }

        var perimeter = (2 * (x + y)).toFixed(2);

        var text = '<div class="text-marker">'
            + '<span>周长：' + perimeter + 'm</span><br>'
            + '<span>面积：' + (x * y).toFixed(2) + 'm²</span>'
            + '</div>';

        if (textMarker == null) {
            textMarker = map.marker({
                position: {lng: northPoint.lng, lat: southPoint.lat},
                // text: text,
                label: {
                    content: text,
                    direction: 'right',
                    offset: map.pixel(-2, 24),
                },
                offset: map.pixel(0, 0),
                map: map,
                zIndex: 999999,
                icon: '/clbs/resources/img/destroy.png',
            });
            textMarker.on('click', function () {
                amapFunCollection.removeCircleTool();
            })
        } else {
            textMarker.setPosition({lng: northPoint.lng, lat: southPoint.lat});
            textMarker.setLabel({
                content: text,
            });
        }
    },

    // 删除画圆
    removeCircleTool: function () {
        if (drawMouseTool) {
            drawMouseTool.close(true);
        }
        ;
        if (textMarker) {
            if (textMarker.labelMarker) {
                map.remove([textMarker.labelMarker]);
            }
            map.remove([textMarker]);
            if (textMarker.markerLabel) {
                map.remove([textMarker.markerLabel]);
            }
            textMarker = null;
        }
        if (circleEditor) {
            circleEditor.close();
        }
        ;
        if (drawRectangle) {
            map.remove([drawRectangle]);
        }
        $('#drawGraphics').hide();
        $('#drawGraphics p.active').removeClass('active');
        $('#areaMeasurementLab').removeClass('preBlue');
    },

    // 面积量算
    areaMeasurementEvent: function () {
        $('#toolOperateClick a i').removeClass('active');
        $('#toolOperateClick a span').css('color', 'rgb(92, 94, 98)');
        $('#distanceMeasuremenLab').removeClass('preBlue');
        $('#areaMeasurementList p.active').removeClass('active');
        mouseTool.close(true);
        if ($('#areaMeasurementLab').hasClass('preBlue')) {
            $('#areaMeasurementLab').removeClass('preBlue');
            $('#drawGraphics').hide();
            // $('#areaMeasurementLab i').removeClass('active');
            amapFunCollection.removeCircleTool();
        } else {
            $('#drawGraphics').show();
            $('#areaMeasurementLab').addClass('preBlue');
            // $('#areaMeasurementLab i').addClass('active');
        }
    },

    overViewFun: function () {
        var $overViewSetting = $('.hawkEyeBtn');
        if ($overViewSetting.hasClass("preBlue")) {
            amapFunCollection.toggleOverViewHide();
            $overViewSetting.removeClass("preBlue");
        } else {
            amapFunCollection.toggleOverViewShow();
            $overViewSetting.addClass("preBlue");
        }
    },

    amapFenceFun: function (node, checked) {
        console.log(node);
        console.log(checked);
        if (node.id == 'zw_m_travel_layer') {
            if (checked) {
                amapFunCollection.layerTwoShow();
                amapFunCollection.layerOneShow();
            } else {
                amapFunCollection.layerTwoHide();
                amapFunCollection.layerOneHide();
            }
        }

        if (node.pId == 'zw_m_travel_layer') {
            if (node.name == '图层1') {
                if (checked) {
                    amapFunCollection.layerOneShow();
                } else {
                    amapFunCollection.layerOneHide();
                }
            } else if (node.name == '图层2') {
                if (checked) {
                    amapFunCollection.layerTwoShow();
                } else {
                    amapFunCollection.layerTwoHide();
                }
            }
        }
    },
}

$(function () {
    var timer = setInterval(function () {
        if (map) {
            clearInterval(timer);
            amapFunCollection.init();
        }
    }, 300);
    $('#drawGraphics').on('click', amapFunCollection.drawGraphics);
})