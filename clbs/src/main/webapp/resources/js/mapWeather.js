/**
 * 天气
 */
var mapWeather;
(function(window,$){
    var gMap;//map地图实例对象

    var provinceMarkerList=[],
        provinceMarkerMap=[],
        cityMarkerList=[],
        cityMarkerMap=[];

    var centerCity=['哈尔滨市','长春市','沈阳市','济南市','石家庄市','呼和浩特市','太原市','郑州市','合肥市','南京市',
        '杭州市','福州市','南昌市','武汉市','银川市','西安市','长沙市','广州市','海口市','南宁市','贵阳市','成都市','兰州市',
        '西宁市','昆明市','拉萨市','乌鲁木齐市','重庆市','北京市','上海市','天津市','澳门特别行政区','香港特别行政区'];//中心城市

    //天气详情信息窗体
    var infoWindow = new AMap.InfoWindow({
        offset: new AMap.Pixel(10, 0),
        // closeWhenClickMap: true
    });

    mapWeather={
        /**
         * 获取中国省市天气
         */
        getWeatherDatas:function(){
            json_ajax("GET", '/clbs/adas/lb/common/getChinaWeather', "json", true, {}, function (data) {
                if(data.success){
                    data = data.obj;

                    for(var i=0;i<data.length;i++){
                        var item = data[i];
                        if(item.city == item.province) continue;

                        var weatherMarker = mapWeather.setWeatherMarker(item);
                        if(centerCity.indexOf(item.city)!=-1){
                            provinceMarkerList.push(weatherMarker);
                            provinceMarkerMap=provinceMarkerList;
                        }else{
                            cityMarkerList.push(weatherMarker);
                            cityMarkerMap=cityMarkerList;
                        }
                    }
                }
            });
        },
        /**
         * 根据层级显示天气
         * @param map:地图实例
         * @param mapZoom:地图层级
         * @param isWeatherShow:显示天气开关
         */
        showWeather:function(map,mapZoom,isWeatherShow){
            gMap = map;
            if(mapZoom<5 || mapZoom>8 || !isWeatherShow){
                mapWeather.clearWeather(map,provinceMarkerMap);
                provinceMarkerMap=[];
            }else{
                provinceMarkerMap=provinceMarkerList;
                map.add(provinceMarkerMap);
            }

            if(mapZoom<7 || mapZoom>8 || !isWeatherShow){
                mapWeather.clearWeather(map,cityMarkerMap);
                cityMarkerMap=[];
            }else{
                cityMarkerMap=cityMarkerList;
                map.add(cityMarkerMap);
            }
        },
        /**
         * 清除天气
         * @param map
         * @param data
         */
        clearWeather:function(map,data){
            map.remove(data);
        },
        /**
         * 创建天气点图标
         * @param item
         * @returns {AMap.Marker}
         */
        setWeatherMarker:function(item){
            var iconUrl = mapWeather.getWeartherIcon(item.weather);
            var lnglat = item.center.split(',');

            var icon = new AMap.Icon({
                size: new AMap.Size(26, 26),
                image: iconUrl,
                imageSize: new AMap.Size(26, 26)
            });


            var marker = new AMap.Marker({
                position: lnglat,
                offset: new AMap.Pixel(0, 0),
                icon: icon,
            });

            //天气图标点击显示详情
            marker.content = mapWeather.creatDetail(item);
            // marker.on('click', mapWeather.showDetail);
            marker.on('mouseover', mapWeather.showDetail);
            marker.on('mouseout', mapWeather.hideDetail);

            return marker;
        },
        //天气详情展示
        creatDetail:function(item){
            var html = '<div>\n' +
                '<div>城市/区：'+ item.city +'</div>\n' +
                '<div>天气：'+ item.weather +'</div>\n' +
                '<div>温度：'+ item.temperature +'°C</div>\n' +
                '<div>风力：'+ item.windpower +'</div>\n' +
                '<div>更新时间：'+ item.reporttime +'</div>\n' +
                '</div>';

            return html;
        },
        showDetail:function(e){
            infoWindow.setContent(e.target.content);
            infoWindow.open(gMap, e.target.getPosition());
        },
        hideDetail:function(e){
            infoWindow.close(gMap, e.target.getPosition());
        },
        /**
         * 天气对应显示的图标
         * @param weather
         * @returns {string}
         */
        getWeartherIcon:function(weather){
            var iconUrl='';
            switch (weather){
                case '晴':
                    iconUrl='/clbs/resources/img/weather/w1.png';
                    break;
                case '多云':
                    iconUrl='/clbs/resources/img/weather/w2.png';
                    break;
                case '雾':
                    iconUrl='/clbs/resources/img/weather/w3.png';
                    break;
                case '阴':
                    iconUrl='/clbs/resources/img/weather/w4.png';
                    break;
                case '阵雨':
                    iconUrl='/clbs/resources/img/weather/w5.png';
                    break;
                case '雷阵雨并伴有冰雹':
                    iconUrl='/clbs/resources/img/weather/w6.png';
                    break;
                case '冻雨':
                    iconUrl='/clbs/resources/img/weather/w7.png';
                    break;
                case '雨夹雪':
                    iconUrl='/clbs/resources/img/weather/w8.png';
                    break;
                case '飑':
                    iconUrl='/clbs/resources/img/weather/w9.png';
                    break;
                case '雨':
                case '小雨':
                    iconUrl='/clbs/resources/img/weather/w10.png';
                    break;
                case '小雨-中雨':
                case '中雨':
                    iconUrl='/clbs/resources/img/weather/w11.png';
                    break;
                case '中雨-大雨':
                case '大雨':
                    iconUrl='/clbs/resources/img/weather/w12.png';
                    break;
                case '大雨-暴雨':
                case '暴雨':
                    iconUrl='/clbs/resources/img/weather/w13.png';
                    break;
                case '暴雨-大暴雨':
                case '大暴雨':
                    iconUrl='/clbs/resources/img/weather/w14.png';
                    break;
                case '大暴雨-特大暴雨':
                case '特大暴雨':
                    iconUrl='/clbs/resources/img/weather/w14_1.png';
                    break;
                case '阵雪':
                    iconUrl='/clbs/resources/img/weather/w15.png';
                    break;
                case '小雪':
                    iconUrl='/clbs/resources/img/weather/w16.png';
                    break;
                case '小雪-中雪':
                case '中雪':
                    iconUrl='/clbs/resources/img/weather/w17.png';
                    break;
                case '中雪-大雪':
                case '大雪':
                    iconUrl='/clbs/resources/img/weather/w18.png';
                    break;
                case '大雪-暴雪':
                case '暴雪':
                    iconUrl='/clbs/resources/img/weather/w19.png';
                    break;
                case '弱高吹雪':
                    iconUrl='/clbs/resources/img/weather/w20.png';
                    break;
                case '沙尘暴':
                    iconUrl='/clbs/resources/img/weather/w21.png';
                    break;
                case '龙卷风':
                    iconUrl='/clbs/resources/img/weather/w21_1.png';
                    break;
                case '浮尘':
                    iconUrl='/clbs/resources/img/weather/w22.png';
                    break;
                case '扬沙':
                    iconUrl='/clbs/resources/img/weather/w23.png';
                    break;
                case '强沙尘暴':
                    iconUrl='/clbs/resources/img/weather/w24.png';
                    break;
                case '雷阵雨':
                    iconUrl='/clbs/resources/img/weather/w25.png';
                    break;
                case '霾':
                    iconUrl='/clbs/resources/img/weather/w25.png';
                    break;
                default:
                    break;
            }
            return iconUrl;
        },
    }
})(window,$)