/**
 * Created by AXH on 2017/7/14
 * 高德  => GPS
 */
var ECCENTRICITY_OF_ELLIPSOID = 0.0066934216229659433;
var EARTH_RADIUS = 6378245.0;
var PI = Math.PI;

function gpsDataTranslate(lat, lon) {
	var gcj_latlng = gpsTransform(lat, lon);
	var latlng_lat = lat * 2 - gcj_latlng[0][0];
	var latlng_lng = lon * 2 - gcj_latlng[0][1];
	var latlng = [latlng_lat, latlng_lng];
	return latlng;
};

function gpsTransform(wgLat, wgLon) {
	var latlng = new Array();
	if (isOutOfChina(wgLat, wgLon)) {
        latlng.push([wgLat, wgLon]);
        return latlng;
    }
	var dLat = gpsTransformLat(wgLon - 105.0, wgLat - 35.0);
	var dLon = gpsTransformLon(wgLon - 105.0, wgLat - 35.0);
	var radLat = wgLat / 180.0 * PI;
	var magic = Math.sin(radLat);
	magic = 1 - ECCENTRICITY_OF_ELLIPSOID * magic * magic;
	var sqrtMagic = Math.sqrt(magic);
	dLat = (dLat * 180.0) / ((EARTH_RADIUS * (1 - ECCENTRICITY_OF_ELLIPSOID)) / (magic * sqrtMagic) * PI);
    dLon = (dLon * 180.0) / (EARTH_RADIUS / sqrtMagic * Math.cos(radLat) * PI);
    latlng.push([wgLat + dLat, wgLon + dLon]);
    return latlng;
};

function isOutOfChina(latitude, longitude) {
	return longitude < 72.004 || longitude > 137.8347 || (latitude < 0.8293 || latitude > 55.8271);
};

function gpsTransformLat(x, y) {
	 var num = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
     num += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
     num += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0;
     return num + (160.0 * Math.sin(y / 12.0 * PI) + 320.0 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0;
};

function gpsTransformLon(x, y) {
	var num = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
    num += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
    num += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0;
    return num + (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0;
};
