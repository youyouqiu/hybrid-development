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
    /**
     * 视频播放队列
     * [{name:'监控对象名',vId:'监控对象ID',channelObj:{num:'通道号',mobile...}]
     * */
    this._checkMonitorArray = [];// 已勾选监控对象
    this._videoPlayArray = [];// 视频播放队列
    this._monitorChannelObj = {};// 放置监控对象在视频播放队列中的通道数量{monitorId:channelLen}
    this._videoScreenControl = {// 视频播放控制参数
        'brightness': 255,// 亮度
        'chroma': 0,// 色度
        'contrast': 51,// 对比度
        'saturation': 51,// 饱和度
        'volume': 50,// 音量
        'videoStream': null,// 码流
    };
    this._lockWindow = [];// 锁定视频窗口

    /** 小窗口相关 */
    this._windowCount = 6; // 小窗口个数
};

Data.prototype.setCheckMonitorArray = function (newArr) {
    this._checkMonitorArray = newArr;
};
Data.prototype.getCheckMonitorArray = function () {
    return this._checkMonitorArray;
};
Data.prototype.setVideoPlayArray = function (newArr) {
    this._videoPlayArray = newArr;
};
Data.prototype.getVideoPlayArray = function () {
    return this._videoPlayArray;
};
Data.prototype.setMonitorChannelObj = function (newObj) {
    this._monitorChannelObj = newObj;
};
Data.prototype.getMonitorChannelObj = function () {
    return this._monitorChannelObj;
};

Data.prototype.setVideoScreenControl = function (newObj) {
    this._videoScreenControl = newObj;
};
Data.prototype.getVideoScreenControl = function () {
    return this._videoScreenControl;
};
Data.prototype.setLockWindow = function (newArr) {
    this._lockWindow = newArr;
};
Data.prototype.getLockWindow = function () {
    return this._lockWindow;
};

Data.prototype.setAllCount = function (allCount) {
    this._allCount = allCount;
    this.runEventHandler('allCountChange');
}
Data.prototype.getAllCount = function () {
    return this._allCount;
}


Data.prototype.setOnlineCount = function (onlineCount) {
    this._onlineCount = onlineCount;
    this.runEventHandler('onlineCountChange');
}
Data.prototype.getOnlineCount = function () {
    return this._onlineCount;
}

Data.prototype.setOfflineCount = function (offlineCount) {
    this._offlineCount = offlineCount;
    this.runEventHandler('offlineCountChange');
}
Data.prototype.getOfflineCount = function () {
    return this._offlineCount;
}

Data.prototype.setRunVidArray = function (runVidArray) {
    this._runVidArray = runVidArray;
    this.runEventHandler('runVidArrayChange');
}
Data.prototype.getRunVidArray = function () {
    return this._runVidArray;
}

Data.prototype.setStopVidArray = function (stopVidArray) {
    this._stopVidArray = stopVidArray;
    this.runEventHandler('stopVidArrayChange');
}
Data.prototype.getStopVidArray = function () {
    return this._stopVidArray;
}

Data.prototype.setAllVidArray = function (allVidArray) {
    this._allVidArray = allVidArray;
    this.runEventHandler('allVidArrayChange');
}
Data.prototype.getAllVidArray = function () {
    return this._allVidArray;
}

Data.prototype.setTreeType = function (treeType) {
    this._treeType = treeType;
    this.runEventHandler('treeTypeChange');
}
Data.prototype.getTreeType = function () {
    return this._treeType;
}

Data.prototype.setStatusVidObj = function (vid, status) {
    this._statusVidObj[vid] = status;
    this.runEventHandler('statusVidObjChange', vid, status);
}
Data.prototype.getStatusVidObj = function () {
    return this._statusVidObj;
}

Data.prototype.getSubscribVidArray = function () {
    return Object.keys(this._subscribVidObj);
}
Data.prototype.getSubscribObjArray = function () {
    var ret = [];
    var keys = this.getSubscribVidArray();
    for (var i = 0; i < keys.length; i++) {
        ret.push(this._subscribVidObj[keys[i]]);
    }
    return ret;
}
/**
 *
 * @param vid
 * @param info {windowIndex,}
 */
Data.prototype.addSubscribVid = function (vid, info) {
    this._subscribVidObj[vid] = info;
    this._latestUpdateVid = vid;
    this.runEventHandler('addSubscribVid');
}
Data.prototype.removeSubscribVid = function (vid) {
    this._latestUpdateVid = vid;
    this.runEventHandler('removeSubscribVid');
    delete this._subscribVidObj[vid];
}
Data.prototype.updateSubscribVid = function (vid, info, noReRender) {
    this._subscribVidObj[vid] = info;
    this._latestUpdateVid = vid;
    if (noReRender) {
        return;
    }
    this.runEventHandler('updateSubscribVid');
}
Data.prototype.getSubscribVidInfo = function (vid) {
    return this._subscribVidObj[vid];
}

Data.prototype.setWindowCount = function (windowCount) {
    this._windowCount = windowCount;
    this.runEventHandler('windowCountChange');
}
Data.prototype.getWindowCount = function () {
    return this._windowCount;
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
};
// </editor-fold>


