/**
 * Created by Tdz on 2016/10/12.
 */
"use strict";
var webSocket = {
    socket: null,
    conFlag: false,
    subscribeArr: new Map(),
    stompClient: null,
    unsubscribeMap: {},
    /**
     * 初始化websocket连接
     * @param url websocket的URL
     * @param headers stomp消息头数据
     * @param subUrl stomp订阅的URL
     * @param callBack 收到订阅消息后的回调函数
     * @param sendUrl 发送消息的URL
     * @param requestStr 发送的消息
     */
    init: function (url, headers, success, close) {
        var $this = this;
        $this.stompClient = new StompJs.Client({
            connectHeaders: headers,
            webSocketFactory: () => new SockJS(url),
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
            onWebSocketClose (event) {
                console.log('Websocket close observed:', event);
            },
            onWebSocketError (event) {
                console.log('Websocket error observed:', event);
            },
            onStompError (frame) {
                console.log('Stomp error:', frame);
            },
            onConnect () {
                console.log('socket连接上了');
                $this.conFlag = true;
                if (typeof success === 'function') {
                    success();
                }
                $this.reconnection.call($this);
            },
            onDisconnect () {
                console.log('socket断开了');
                $this.conFlag = false;
                if (typeof close === 'function') {
                    close();
                }
            },
            debug (str) {
                // console.log(str);
            },
        });
        $this.stompClient.activate();
    },

    send: function (url, header = {}, data = {}) {
        const skipContentLengthHeader = header['content-length'] === false;
        if (skipContentLengthHeader) {
            delete header['content-length'];
        }
        if (this.stompClient && this.stompClient.connected) {
            this.stompClient.publish({ destination: url, headers: header, body: JSON.stringify(data) });
        }
    },

    /**
     * 订阅消息，然后向指定的URL发送消息
     * @param headers stomp消息头数据
     * @param subUrl stomp订阅的URL
     * @param callBack 收到订阅消息后的回调函数
     * @param sendUrl 发送消息的URL
     * @param requestStr 发送的消息
     * @param state 是否重复订阅
     */
    subscribe: function(headers, subUrl, callBack, sendUrl, requestStr, state) {
        if (this.stompClient.connected) {
            this.subscribeAndSend(subUrl, callBack, sendUrl, headers, requestStr, state);
        }
    },

    /**
     * 订阅消息，然后向指定的URL发送消息
     * @param subUrl stomp订阅的URL
     * @param callBack 收到订阅消息后的回调函数
     * @param sendUrl 发送消息的URL
     * @param headers stomp消息头数据
     * @param requestStr 发送的消息
     * @param state 是否重复订阅
     */
    subscribeAndSend: function (subUrl, callBack, sendUrl, headers, request, state) {
        var isUnSubscribed = !this.subscribeArr.has(subUrl);
        if (subUrl && (isUnSubscribed || state)) {
            if (isUnSubscribed) {
                this.subscribeArr.set(subUrl, {
                    subUrl,
                    callBack,
                    sendUrl,
                    headers,
                    request,
                    state,
                });
            } else if (this.unsubscribeMap[subUrl]) {
                this.unsubscribeMap[subUrl].unsubscribe();
            }
            //深拷贝headers，避免所有的订阅共用同一个header导致无法正常订阅的问题
            this.unsubscribeMap[subUrl] = this.stompClient.subscribe(subUrl, callBack, JSON.parse(JSON.stringify(headers)));
        }
        this.send(sendUrl, headers, request);
    },

    /**
     * 发送消息
     * @param headers stomp消息头数据
     * @param url 发送消息的URL
     * @param requestStr 发送的消息
     */
    unsubscribealarm: function (headers, url, request) {
        this.send(url, headers, request);
        // this.stompClient.send(url, headers, JSON.stringify(requestStr));
    },

    /**
     * 取消订阅
     * @param url 取消订阅的URL
     */
    unsubscribe: function (url) {
        var unsubscribe = this.unsubscribeMap[url];
        if (unsubscribe) {
            unsubscribe.unsubscribe();
        }
        if (this.subscribeArr.has(url)) {
            this.subscribeArr.delete(url);
        }
    },

    // 重连
    reconnection: function() {
        const values = [...this.subscribeArr.values()];
        this.subscribeArr = new Map();
        for (let i = 0; i < values.length; i += 1) {
            const info = values[i];
            this.subscribe(
                info.headers,
                info.subUrl,
                info.callBack,
                info.sendUrl,
                info.request,
                info.state,
            );
        }
    },

    /**
     * 关闭websocket连接
     */
    close: function () {
        if (this.stompClient) {
            this.stompClient.deactivate();
        }
        this.conFlag = false;
        this.subscribeArr = new Map();
        this.stompClient = null;
    },
};
