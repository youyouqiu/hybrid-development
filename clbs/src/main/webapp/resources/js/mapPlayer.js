/**
 * 地图播放器
 */
var mapPlayer = function () {
    this.map = null;
    this.len = 0;
    this.pos = 0;
    this.speed = 0;
    this.callBack = null;
    this.interval = null;
    this.start = false;
}

mapPlayer.prototype.play = function () {
    if(mapPlayer.map !== null && mapPlayer.map != undefined){
        mapPlayer.interval = setInterval('mapPlayer.writeMap()', mapPlayer.speed);
        mapPlayer.start = true;
    }
}
mapPlayer.prototype.init = function (positions, speed) {
    mapPlayer.map = positions;
    mapPlayer.len = positions.length;
    mapPlayer.speed = speed;
}
mapPlayer.prototype.writeMap = function (positions, speed) {
    if (mapPlayer.length < mapPlayer.pos) {
        mapPlayer.start = false;
        clearInterval(mapPlayer.interval);
    }
    mapPlayer.pos++;
}
//暂停
mapPlayer.prototype.pause = function () {
    if(mapPlayer.start){
        clearInterval(mapPlayer.interval);
    }
}

//加速
mapPlayer.prototype.speedUp = function () {
    if(mapPlayer.start){
        clearInterval(mapPlayer.interval);
        mapPlayer.interval = setInterval('mapPlayer.writeMap()', (mapPlayer.speed / 2));
    }
}
//停止
mapPlayer.prototype.stop = function () {
    if(mapPlayer.start) {
        mapPlayer.pos = 0;
        mapPlayer.start = false;
        clearInterval(mapPlayer.interval);
    }
}
//前进
mapPlayer.prototype.up = function () {
    if(mapPlayer.start) {
        if (mapPlayer.len - mapPlayer.pos > 5) {
            mapPlayer.pos += 5;
        } else {
            mapPlayer.pos = mapPlayer.len - 1;
        }
        clearInterval(mapPlayer.interval);
        mapPlayer.interval = setInterval('mapPlayer.writeMap()', mapPlayer.speed);
    }
}
//后退
mapPlayer.prototype.down = function () {
    if(mapPlayer.start) {
        if (mapPlayer.pos > 5) {
            mapPlayer.pos -= 5;
        } else {
            mapPlayer.pos = 0;
        }
        clearInterval(mapPlayer.interval);
        mapPlayer.interval = setInterval('mapPlayer.writeMap()', mapPlayer.speed);
    }
}