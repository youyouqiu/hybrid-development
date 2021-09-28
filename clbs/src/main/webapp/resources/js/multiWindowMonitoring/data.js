var Data = function () {
    this._eventHandlerList = {}; // 事件执行函数
    /** 树相关 */
    this._allCount = null; // 全部监控对象数量
    this._onlineCount = null; // 在线监控对象数量
    this._offlineCount = null; // 不在线监控对象数量
    this._runVidArray = null; // 行驶监控对象id数组
    this._stopVidArray = null; // 停止监控对象id数组
    this._allVidArray = null; // 所有监控对象id数组
    this._treeType = null; // 树类型  all:全部；online:在线；offline:不在线；
    // heartBeat:心跳；notPosition:未定位；alarm:报警；run:行驶；stop:停止；overSpeed:超速;search:搜索
    this._statusVidObj = {}; // 当前订阅状态信息监控对象id字典
    this._subscribVidObj = {}; // 当前订阅位置信息监控对象id字典
    this._latestUpdateVid = null; // 最近一个更新了信息的监控对象id

    /** 小窗口相关 */
    this._windowCount = 6; // 小窗口个数
    this._activeWindowIndex = null; // 聚焦小窗口索引
    this._mapInstanceArray = []; // 地图实例数组
    this._visibleSensorKeyArray = null; // 小窗口
    this._mapType = 'amap'; // amap satellite google 地图类型
    this._showTraffic = false; // 是否显示路况
};

Data.prototype.setAllCount = function(allCount){
    this._allCount = allCount;
    this.runEventHandler('allCountChange');
}
Data.prototype.getAllCount = function(){
    return this._allCount;
}


Data.prototype.setOnlineCount = function(onlineCount){
    this._onlineCount = onlineCount;
    this.runEventHandler('onlineCountChange');
}
Data.prototype.getOnlineCount = function(){
    return this._onlineCount;
}

Data.prototype.setOfflineCount = function(offlineCount){
    this._offlineCount = offlineCount;
    this.runEventHandler('offlineCountChange');
}
Data.prototype.getOfflineCount = function(){
    return this._offlineCount;
}

Data.prototype.setRunVidArray = function(runVidArray){
    this._runVidArray = runVidArray;
    this.runEventHandler('runVidArrayChange');
}
Data.prototype.getRunVidArray = function(){
    return this._runVidArray;
}

Data.prototype.setStopVidArray = function(stopVidArray){
    this._stopVidArray = stopVidArray;
    this.runEventHandler('stopVidArrayChange');
}
Data.prototype.getStopVidArray = function(){
    return this._stopVidArray;
}

Data.prototype.setAllVidArray = function(allVidArray){
    this._allVidArray = allVidArray;
    this.runEventHandler('allVidArrayChange');
}
Data.prototype.getAllVidArray = function(){
    return this._allVidArray;
}

Data.prototype.setTreeType = function(treeType){
    this._treeType = treeType;
    this.runEventHandler('treeTypeChange');
}
Data.prototype.getTreeType = function(){
    return this._treeType;
}

Data.prototype.setStatusVidObj = function(vid,status){
    this._statusVidObj[vid] = status;
    this.runEventHandler('statusVidObjChange',vid, status);
}
Data.prototype.getStatusVidObj = function(){
    return this._statusVidObj;
}

Data.prototype.getSubscribVidArray = function(){
    return Object.keys(this._subscribVidObj);
}
Data.prototype.getSubscribObjArray = function(){
    var ret = [];
    var keys = this.getSubscribVidArray();
    for (var i = 0; i < keys.length; i++){
        ret.push(this._subscribVidObj[keys[i]]);
    }
    return ret;
}
/**
 *
 * @param vid
 * @param info {windowIndex,}
 */
Data.prototype.addSubscribVid = function(vid,info){
    this._subscribVidObj[vid] = info;
    this._latestUpdateVid = vid;
    this.runEventHandler('addSubscribVid');
}
Data.prototype.removeSubscribVid = function(vid){
    this._latestUpdateVid = vid;
    this.runEventHandler('removeSubscribVid');
    delete this._subscribVidObj[vid];
}
Data.prototype.updateSubscribVid = function(vid,info, noReRender){
    this._subscribVidObj[vid] = info;
    this._latestUpdateVid = vid;
    if (noReRender){
        return;
    }
    this.runEventHandler('updateSubscribVid');
}
Data.prototype.getSubscribVidInfo = function(vid){
    return this._subscribVidObj[vid];
}

Data.prototype.setWindowCount = function(windowCount){
    this._windowCount = windowCount;
    this.runEventHandler('windowCountChange');
}
Data.prototype.getWindowCount = function(){
    return this._windowCount;
}

Data.prototype.setActiveWindowIndex = function(activeWindowIndex){
    this._activeWindowIndex = activeWindowIndex;
    this.runEventHandler('activeWindowIndexChange');
}
Data.prototype.getActiveWindowIndex = function(){
    return this._activeWindowIndex;
}

Data.prototype.setMapInstanceArray = function(mapInstanceArray){
    this._mapInstanceArray = mapInstanceArray;
    this.runEventHandler('mapInstanceArrayChange');
}
Data.prototype.getMapInstanceArray = function(){
    return this._mapInstanceArray;
}

Data.prototype.setLatestUpdateVid = function(latestUpdateVid){
    this._latestUpdateVid = latestUpdateVid;
    this.runEventHandler('latestUpdateVidChange');
}
Data.prototype.getLatestUpdateVid = function(){
    return this._latestUpdateVid;
}

Data.prototype.setVisibleSensorKeyArray = function(visibleSensorKeyArray){
    this._visibleSensorKeyArray = visibleSensorKeyArray;
    this.runEventHandler('visibleSensorKeyArrayChange');
}
Data.prototype.getVisibleSensorKeyArray = function(){
    return this._visibleSensorKeyArray;
}

Data.prototype.setMapType = function(mapType){
    this._mapType = mapType;
    this.runEventHandler('mapTypeChange');
}
Data.prototype.getMapType = function(){
    return this._mapType;
}

Data.prototype.setShowTraffic = function(showTraffic){
    this._showTraffic = showTraffic;
    this.runEventHandler('showTrafficChange');
}
Data.prototype.getShowTraffic = function(){
    return this._showTraffic;
}

// <editor-fold desc="事件 添加，移除，执行">
Data.prototype.on = function (eventName, eventHandler) {
    if (this._eventHandlerList[eventName] === undefined) {
        this._eventHandlerList[eventName] = [eventHandler];
    } else {
        this._eventHandlerList[eventName].push(eventHandler);
    }
}

Data.prototype.off = function (eventName, eventHandler) {
    var eventList = this._eventHandlerList[eventName];
    if (eventList === undefined || eventList.length === undefined || eventList.length === 0) {
        return false;
    }
    for (var i = 0; i < eventList.length; i++) {
        if (eventList[i] === eventHandler) {
            // 删除该 event handler
            eventList.splice(i, 1);
        }
    }
}

Data.prototype.runEventHandler = function (eventName) {
    var eventList = this._eventHandlerList[eventName];
    if (eventList === undefined || eventList.length === undefined || eventList.length === 0) {
        return false;
    }
    for (var i = 0; i < eventList.length; i++) {
        var eventHandler = eventList[i];
        if (typeof eventHandler === 'function') {
            var result = eventHandler.apply(null, Array.prototype.slice.call(arguments, 1));
            // 如果event handler 返回false，阻止剩余的handler的执行
            if (result === false) {
                break;
            }
        }
    }
}
// </editor-fold>


