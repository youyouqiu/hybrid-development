function HJEventService() {
  var ServerSocket = {
    CONNECT_SUCCESS: 0,
    CONNECT_FIAL: 4,
    RECEIVE_DATA: 1,
    CONNECT_CLOSE: 2,
    CONNECT_LOST: 3,

    connecting: false,
    connected: false,
    voiceWebSocket: null,
    voiceWebSocketCallback: null,
    connect: function (callback) {
      if (this.connecting) {
        return;
      }
      console.log('connecting .....');
      var _this = this;
      if (this.connected) {
        this.disConnect();
      }
      this.connecting = true;
      this.connected = false;
      this.voiceWebSocketCallback = callback;
      try {
        var path = 'wss://' + globalConfig.serverIP + ':' + globalConfig.serverPort;
        if (globalConfig.projectName != '') {
          path += '/' + globalConfig.projectName;
        }
        path += '/serverSocket.do';
        this.voiceWebSocket = new WebSocket(path);
      } catch (e) {
        this.connecting = false;
        this.connected = false;
        console.log('-------------连接异常---------------');
        console.log(e);
        console.log('-------------连接异常---------------');
        if (this.voiceWebSocketCallback != null) {
          var res = {};
          res.type = _this.CONNECT_FIAL;
          res.context = 'onerror';
          this.voiceWebSocketCallback(res);
        }
        return;
      }
      this.voiceWebSocket.onopen = function () {
        console.log('onopen');
        _this.connecting = false;
        _this.connected = true;
        if (_this.voiceWebSocketCallback != null) {
          var res = {};
          res.type = _this.CONNECT_SUCCESS;
          res.context = 'success';
          _this.voiceWebSocketCallback(res);
        }
      };
      this.voiceWebSocket.onmessage = function (event) {
        //console.log(event);
        if (_this.voiceWebSocketCallback != null) {
          try {
            var res = {};
            res.type = _this.RECEIVE_DATA;
            res.context = event.data;
            _this.voiceWebSocketCallback(res);
          } catch (e) {
            console.log('-------------处理异常---------------');
            console.log(e);
            console.log('-------------处理异常---------------');
          }
        }
      };
      this.voiceWebSocket.onclose = function () {
        console.log('onclose');
        _this.connecting = false;
        _this.connected = false;
        if (_this.voiceWebSocketCallback != null) {
          var res = {};
          res.type = _this.CONNECT_CLOSE;
          res.context = 'onclose';
          _this.voiceWebSocketCallback(res);
        }
      };
      this.voiceWebSocket.onerror = function () {
        console.log('onerror');
        _this.connecting = false;
        _this.connected = false;
        if (_this.voiceWebSocketCallback != null) {
          var res = {};
          res.type = _this.CONNECT_LOST;
          res.context = 'onerror';
          _this.voiceWebSocketCallback(res);
        }
      };
    },
    send: function (msg) {
      if (this.voiceWebSocket != null) {
        try {
          if (this.connected) {
            this.voiceWebSocket.send(msg);
            return true;
          } else {
            if (this.voiceWebSocketCallback != null) {
              var res = {};
              res.type = this.CONNECT_CLOSE;
              res.context = 'onclose';
              this.voiceWebSocketCallback(res);
            }
            return false;
          }
        } catch (e) {
          if (this.voiceWebSocketCallback != null) {
            var res = {};
            res.type = this.CONNECT_CLOSE;
            res.context = 'onerror';
            this.voiceWebSocketCallback(res);
          }
          console.log(e);
          return false;
        }
      }
    },
    setWebSocketCallback: function (callback) {
      this.voiceWebSocketCallback = callback;
    },
    isConnected: function () {
      return this.connected;
    },
    disConnect: function () {
      try {
        if (this.connected) {
          this.voiceWebSocket.close();
          this.connecting = false;
          this.connected = false;
        }
      } catch (e) {
        return;
      }
    }
  };

  var eventEngine = null;
  var serverSocket = ServerSocket;
  var globalConfig = {
    ip: '127.0.0.1',
    port: 8080,
    projectName: 'IOTPM'
  };
  HJEventService.prototype.onLoginResponse = null;//登陆响应事件
  HJEventService.prototype.onLogout = null;//登出事件
  HJEventService.prototype.onCustomerUpdateEvent = null;//客户信息更新事件（添加/更新/删除）
  HJEventService.prototype.onGroupUpdateEvent = null;//群组信息更新事件（添加/更新/删除）
  HJEventService.prototype.onUserUpdateEvent = null;//用户信息更新事件（添加/更新/删除）
  HJEventService.prototype.onUserStatusUpdateEvent = null;//用户状态更新事件
  HJEventService.prototype.onUserDefaultGroupUpdateEvent = null;//用户默认组更新事件

  HJEventService.prototype.init = function (config) {
    eventEngine = this;
    globalConfig.serverIP = config.serverIP;
    globalConfig.serverPort = config.serverPort;
    if (config.projectName && config.projectName != '')
      globalConfig.projectName = config.projectName;
    globalConfig.loginName = config.loginName;
    globalConfig.password = config.password;
  };
  HJEventService.prototype.login = function () {
    serverSocket.connect(serverConnectCallback);
  };
  HJEventService.prototype.logout = function () {
    serverSocket.disConnect();
    if (this.onLogout != null) {
      this.onLogout();
    }
  };

  function serverConnectCallback(result) {
    switch (result.type) {
      case ServerSocket.CONNECT_SUCCESS: {//连接成功执行登录
        loginReq();
      }
        break;
      case ServerSocket.RECEIVE_DATA: {//收到数据执行解析
        onReceiveData(result.context);
      }
        break;
      case ServerSocket.CONNECT_FIAL: {//连接失败
        if (eventEngine.onLoginResponse) {
          var result = {
            event: {
              result: LoginResultEnum.LOGIN_RESULT_FAILURE_4_NETWORK
            }
          };
          eventEngine.onLoginResponse(result);
        }
      }
        break;
      case ServerSocket.CONNECT_CLOSE: {//连接关闭
        if (eventEngine.onLogout) {
          eventEngine.onLogout();
        }
      }
        break;
      case ServerSocket.CONNECT_LOST: {//连接断开
        if (eventEngine.onLogout) {
          eventEngine.onLogout();
        }
      }
        break;
    }
  }

  function onReceiveData(data) {
    var d = JSON.parse(data);
    switch (d.head.type) {
      case 'LOGIN_RSP':
        onLoginRsp(d.data);
        break;
      case 'LOGOUT':
        onLogout();
        break;
      case 'CUST_UPDATE':
        onCustUpdate(d.data);
        break;
      case 'GROUP_UPDATE':
        onGroupUpdate(d.data);
        break;
      case 'USER_UPDATE':
        onUserUpdate(d.data);
        break;
      case 'USER_AUDIO_STATUS_UPDATE':
        onUserStatusUpdate(d.data);
        break;
      case 'USER_DEFAULT_GROUP_UPDATE':
        onUserDefaultGroupUpdate(d.data);
        break;
    }
  }

  function onLoginRsp(data) {
    var result = {
      event: {
        result: data.result,
        custId: data.custId,
        userId: data.userId
      }
    };
    if (eventEngine.onLoginResponse) {
      eventEngine.onLoginResponse(result);
    }
  }

  function onLogout() {
    if (eventEngine.onLogout) {
      eventEngine.onLogout();
    }
  }

  function onCustUpdate(data) {
    if (eventEngine.onCustomerUpdateEvent) {
      var result = {
        event: {
          opType: data.opType,
          custId: data.custId
        }
      };
      eventEngine.onCustomerUpdateEvent(result);
    }
  }

  function onGroupUpdate(data) {
    if (eventEngine.onGroupUpdateEvent) {
      var result = {
        event: {
          opType: data.opType,
          custId: data.custId,
          groupId: data.groupId
        }
      };
      eventEngine.onGroupUpdateEvent(result);
    }
  }

  function onUserUpdate(data) {
    if (eventEngine.onUserUpdateEvent) {
      var result = {
        event: {
          opType: data.opType,
          custId: data.custId,
          userId: data.msId
        }
      };
      eventEngine.onUserUpdateEvent(result);
    }
  }

  function onUserStatusUpdate(data) {
    if (eventEngine.onUserStatusUpdateEvent) {
      var result = {
        event: {
          custId: data.custId,
          userId: data.msId,
          audioStatus: data.audioStatus,
          videoStatus: data.videoStatus
        }
      };
      eventEngine.onUserStatusUpdateEvent(result);
    }
  }

  function onUserDefaultGroupUpdate(data) {
    if (eventEngine.onUserDefaultGroupUpdateEvent) {
      var result = {
        event: {
          custId: data.custId,
          userId: data.msId,
          defaultGroupId: data.defaultGroupId
        }
      };
      eventEngine.onUserDefaultGroupUpdateEvent(result);
    }
  }

  function loginReq() {
    var data = {
      head: {type: 'LOGIN_REQ'},
      data: {loginName: globalConfig.loginName, password: globalConfig.password}
    };
    serverSocket.send(JSON.stringify(data));
  }

};

var hjEventService = {
  createEngine: function (config) {
    var hjs = new HJEventService();
    hjs.init(config);
    return hjs;
  }
};
