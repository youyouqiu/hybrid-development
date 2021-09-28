class AudioCollect {
  constructor (opt) {
    /**
     * 插件默认参数
     * @type {{}}
     */
    this.defaults = {
      /**
       * 连接超时方法
       */
      socketTimeoutFun: null,
      /**
       * 音频打开成功事件
       */
      openAudioSuccess: null,
      /**
       * 音频打开失败事件
       */
      openAudioFail: null,
      /**
       * socket关闭后事件
       */
      socketCloseFun: null,
      /**
       * 操作类型
       * BOTH_WAY 对讲，UP_WAY 监听，DOWN_WAY 广播
       * 默认对讲
       */ 
      type: 'BOTH_WAY',
      /**
       * 音频编码类型
       * 8 g726
       * 26 adpcm
       * 1 g721
       */
      codingType: null,

      /**
       * 外部传入data
       */
      data: null,

      /**
       * 缺少设备回调函数
       */
      onDevicePresent: null,
    };

    this.addAssign();

    this.options = Object.assign({}, this.defaults, opt);
    this.CODEC_TYPE = {
      NONE: 'none',
      ADPCMA: 'adpcma',
      G726: 'g726',
      G711a: 'g711a',
      G711u: 'g711u',
      AAC: 'aac'
    };

    /**
     * G726所需用到的全局变量
     */
    this.pcmPlayer = new PCMPlayer(1, 8000);
    this.memory = this.getMemory();
    this.importObj = this.createWebAssemblyObj();
    this.rtp = new Rtp();
    this.g726TempBuffer = null;
    this.audioWasm = null;
    this.audioCoder = null;
    this.audioEncoder = null;
    this.coderType = this.CODEC_TYPE.NONE;

    /**
     * 媒体流对象
     * @type {null}
     */
    this.mediaStream = null;
    this.audioCtx = null;
    this.audioInput = null;
    this.recorder = null;
    this.websocket = null;
    this.timeoutObj = null;

    /**
     * 时间戳同步
     * @type {null}
     */
    this.recvPackageTimeArr = new Array();
    this.recvPackageSampleCountArr = new Array();

    /**
     * 心跳
     */
    this.heartbeat = null;

    this.mediaEncapsulation();
    this.mediaDevices();
  }

  /**
   * 获取memory
   */
  getMemory() {
    var memory = null;
    if (typeof WebAssembly !== 'undefined') {
      var storageMemory = window.videoMemoryObject;
      memory = storageMemory === undefined ? new WebAssembly.Memory({initial: 256, maximum: 256}) : storageMemory;
      window.videoMemoryObject = memory
    }
    return memory;
  }

  /**
   * 判断浏览器是否支持Object.assign方法
   * 不支持就进行手动添加
   */
  addAssign () {
    if (typeof Object.assign !== 'function') {
      Object.defineProperty(Object, "assign", {
        value: function assign(target, varArgs) {
          'use strict';
          if (target == null) {
            throw new TypeError('Cannot convert undefined or null to object');
          }

          var to = Object(target);

          for (var index = 1; index < arguments.length; index++) {
            var nextSource = arguments[index];

            if (nextSource != null) {
              for (var nextKey in nextSource) {
                if (Object.prototype.hasOwnProperty.call(nextSource, nextKey)) {
                  to[nextKey] = nextSource[nextKey];
                }
              }
            }
          }
          return to;
        },
        writable: true,
        configurable: true
      });
    }
  }

  /**
   * 创建音频对象
   */
  createWebAssemblyObj () {
    var webAssemblyObj = null;
    if (typeof WebAssembly !== 'undefined') {
      var wObj = window.webAssemblyObject;
      webAssemblyObj = wObj === undefined ?
          {
            env: {
              abortStackOverflow: () => { throw new Error('overflow'); },
              table: new WebAssembly.Table({ initial: 0, maximum: 0, element: 'anyfunc' }),
              __table_base: 0,
              memory: this.memory,
              __memory_base: 102400,
              STACKTOP: 0,
              STACK_MAX: this.memory.buffer.byteLength,
            }
          } : wObj;
      window.webAssemblyObject = webAssemblyObj;
    } else {
      layer.msg('浏览器版本太低，无法打开音频功能');
    }
    return webAssemblyObj;
  }

  /**
   * 判断浏览器是否支持getUserMedia
   * 针对支持getUserMedia的浏览器进行封装
   */
  mediaEncapsulation() {
    /**
     * 针对旧浏览器未实现mediaDevices，先设置一个空对象
     */
    if (navigator.mediaDevices === undefined) {
      navigator.mediaDevices = {};
    }

    /**
     * 一些浏览器部分支持 mediaDevices，我们不能直接给对象设置 getUserMedia
     * 因为这样可能会覆盖已有的属性，这里我们只会在没有getUserMedia属性的时候添加它。
     */
    if (navigator.mediaDevices.getUserMedia === undefined) {

      navigator.mediaDevices.getUserMedia = (constraints) => {
        /**
         * 首先，如果有getUserMedia的话，就获得它
         */
        var getUserMedia = navigator.webkitGetUserMedia || navigator.mozGetUserMedia;

        /**
         * 一些浏览器根本没实现它 - 那么就返回一个error到promise的reject来保持一个统一的接口
         */
        if (!getUserMedia) {
          return Promise.reject(new Error('getUserMedia is not implemented in this browser'));
        }

        /**
         * 否则，为老的navigator.getUserMedia方法包裹一个Promise
         */
        return new Promise(function(resolve, reject) {
          getUserMedia.call(navigator, constraints, resolve, reject);
        });
      }
    }
  }

  /**
   * 初始化音频采集
   */
  mediaDevices() {
    if (typeof WebAssembly !== 'undefined') {
      navigator.mediaDevices.getUserMedia({ audio: true })
        .then((stream) => {
          var audioWasm = window.audioWasmObject;
          if (audioWasm === undefined) {
            fetch('/clbs/resources/js/media/codec/audio.wasm').then((response) => response.arrayBuffer())
              .then((bytes) => WebAssembly.instantiate(bytes, this.importObj))
              .then((wasm) => {
                this.audioWasm = wasm;
                this.mediaStream = stream;
                window.audioWasmObject = wasm;
                /**
                 * 用户允许打开麦克风
                 * 然后进行一些操作
                 * 连接weboscket
                 */
                this.websocketInit();
              }
            );
          } else {
            this.audioWasm = audioWasm;
            this.mediaStream = stream;
            this.websocketInit();
          }
        })
        .catch((err) => {
          console.error(err.name + ": " + err.message);
          if (err.name === 'NotFoundError') {
            layer.msg('请插入音频对讲设备');
          }
          if (this.options.onDevicePresent !== null && typeof this.options.onDevicePresent === 'function') {
            this.options.onDevicePresent();
          }
        }
      );
    } else {
      layer.msg('浏览器版本太低，无法打开音频功能');
      throw new Error('浏览器不支持WebAssembly对象');
    }
  }

  /**
   * 初始化websocket
   */
  websocketInit() {
    if ('WebSocket' in window && this.options.url !== '') {
      this.websocket = new WebSocket(this.options.url);
      this.websocket.binaryType = 'arraybuffer';
      this.websocket.onopen = this.socketOpen.bind(this);
      this.websocket.onmessage = this.socketMessage.bind(this);
      this.websocket.onerror = this.socketError.bind(this);
      this.websocket.onclose = this.socketClose.bind(this);
    } else {
      throw new Error('浏览器不支持websocket或url为空');
    }
  }

  /**
   * socket连接成功
   */
  socketOpen() {
    this.socketTimeout();
    var that = this;

    this.heartbeat = setInterval(() => {
      that.websocket.send("0");
    }, 5000);
  }

  /**
   * socket接收数据
   */
  socketMessage(event) {
    this.audioDataParsing(event.data);
  }
  
  audioDataParsing(audioData) {
    let rtpFrame = this.rtp.parse(audioData);
    if (rtpFrame.errorCode === Result.ErrorCode.SUCCESS && rtpFrame.type === Result.Type.AUDIO) {


      if (this.audioCoder == null && this.audioWasm != null) {
        this.audioCoder = this.createAudioCoder();
        if (!this.audioCoder){
          if (this.options.openAudioFail){
            this.options.openAudioFail('解码器初始化失败!');
          }
          return;
        } else {
          // 开启成功回调方法
          if (this.options.openAudioSuccess) {
            this.options.openAudioSuccess(this.options.data);
          }
        }
        // 判断是否是对讲或广播
        if (this.options.type === 'BOTH_WAY' || this.options.type === 'DOWN_WAY') {
          this.createAudioContext();
        }
      }

      if (this.audioCoder != null) {
        this.decode(rtpFrame.data,function(pcmFloat32Data, sampleRate= 8000, channels= 1){
          // 记录当前包开始时间及采样数量
          this.recvPackageTimeArr.push(rtpFrame.time);
          this.recvPackageSampleCountArr.push(pcmFloat32Data.length*8000/sampleRate);
          // 判断是否是对讲或监听
          if (this.options.type === 'BOTH_WAY' || this.options.type === 'UP_WAY') {
            // 音频播放
            if (this.pcmPlayer){
              this.pcmPlayer.setChannels(channels);
              this.pcmPlayer.setSampleRate(sampleRate);
              this.pcmPlayer.feed(pcmFloat32Data);
            }
          }
        }.bind(this));
      }
    } else{

    }
  }

  /**
   * 创建编/解码器
   * @returns {Adpcm|G726|G711|null}
   */
  createAudioCoder() {
    if (this.rtp.getPayloadType() === 8) {
      this.coderType = this.CODEC_TYPE.G726;
      return new G726(this.audioWasm, this.memory, 4);
    } else if (this.rtp.getPayloadType() === 26){
      this.coderType = this.CODEC_TYPE.ADPCMA;
      return new Adpcm(this.audioWasm, this.memory);
    } else if (this.rtp.getPayloadType() === 6) {
      this.coderType = this.CODEC_TYPE.G711a;
      return new G711(this.audioWasm, this.memory);
    } else if (this.rtp.getPayloadType() === 7) {
      this.coderType = this.CODEC_TYPE.G711u;
      return new G711(this.audioWasm, this.memory);
    } else if (this.rtp.getPayloadType() === 29) {
      this.coderType = this.CODEC_TYPE.G726;
      return new G726(this.audioWasm, this.memory, 2);
    } else if (this.rtp.getPayloadType() === 30) {
      this.coderType = this.CODEC_TYPE.G726;
      return new G726(this.audioWasm, this.memory, 3);
    } else if (this.rtp.getPayloadType() === 31) {
      this.coderType = this.CODEC_TYPE.G726;
      return new G726(this.audioWasm, this.memory, 4);
    } else if (this.rtp.getPayloadType() === 32) {
      this.coderType = this.CODEC_TYPE.G726;
      return new G726(this.audioWasm, this.memory, 5);
    } else if (this.rtp.getPayloadType() === 19) {
      // AAC AAC
      this.audioEncoder = new G711(this.audioWasm, this.memory);
      this.coderType = this.CODEC_TYPE.AAC;
      return new AACDecoder();
    } else {
      console.error('unknow audio encoder');
    }
    return null;
  }

  /**
   * 对音频数据进行解码
   * @param data 音频数
   */
  decode(data, callback) {
    let pcm16BitData;
    switch (this.coderType) {
      case this.CODEC_TYPE.ADPCMA:
        let adpcmData = data.slice(8, data.byteLength);
        this.audioCoder.resetDecodeState(new Adpcm.State(data[4] + (data[5] << 8), data[6]));
        pcm16BitData = this.audioCoder.decode(adpcmData);
        callback(Std.shortToFloatData(pcm16BitData));
        break;
      case this.CODEC_TYPE.G726:
        // Std.changeByteEndian(data);
        pcm16BitData = this.audioCoder.decode(data, 1);
        callback(Std.shortToFloatData(pcm16BitData));
        break;
      case this.CODEC_TYPE.G711a:
        pcm16BitData = this.audioCoder.decodeG711a(data);
        callback(Std.shortToFloatData(pcm16BitData));
        break;
      case this.CODEC_TYPE.G711u:
        pcm16BitData = this.audioCoder.decodeG711u(data);
        callback(Std.shortToFloatData(pcm16BitData));
        break;
      case this.CODEC_TYPE.AAC:
        this.audioCoder.decode(data, function(data, sampleRate, channels){
          callback(data, sampleRate, channels);
        }.bind(this));
        break;
      default:
        break;
    }
    return;
  }

  /**
   * socket连接报错
   */
  socketError(err) {
    console.log(err);
  }

  /**
   * socket关闭
   */
  socketClose() {
    this.destructionAudioContext();
    if (this.options.socketCloseFun) {
      this.options.socketCloseFun();
    }
    if (this.audioCtx) {
      var that = this;
      this.audioCtx.close().then(function () {
        that.destroy();
      })
    } else {
      this.destroy();
    }
  }

  /**
   * 创建AudioContext对象
   */
  createAudioContext() {
    let AudioContext = window.AudioContext || window.webkitAudioContext;
    /**
     * 实例化AudioContext
     */
    this.audioCtx = new AudioContext();

    /**
     * 创建一个MediaStreamAudioSourceNode对象
     */
    this.audioInput = this.audioCtx.createMediaStreamSource(this.mediaStream);

    /**
     * 创建一个ScriptProcessorNode，
     * 用于通过JavaScript直接处理音频
     */
    var createScript = this.audioCtx.createScriptProcessor || this.audioCtx.createJavaScriptNode;
    this.recorder = createScript.apply(this.audioCtx, [4096, 1, 1]);

    this.startCollectAudio();

    /**
     * 采集每一段音频流成功后的回调方法
     */
    this.recorder.onaudioprocess = this.audioProcessFun.bind(this)
  }

  /**
   * 开始音频采集
   */
  startCollectAudio() {
    this.audioInput.connect(this.recorder);
    this.recorder.connect(this.audioCtx.destination);
  }

  /**
   * 采集成功后回调方法
   */
  audioProcessFun(audioEvent) {
    /**
     * 每一段数据流
     * @type {Float32Array}
     */
    if (this.audioCtx != null) {
      let inputBuffer = audioEvent.inputBuffer.getChannelData(0);
      let pcmFloat32Data = Std.downsampleBuffer(new Float32Array(inputBuffer.buffer), 8000, this.audioCtx.sampleRate);
      let pcm16BitData = Std.floatToShortData(pcmFloat32Data);
      let g726OffsetBuffer = this.g726TempBuffer;
      this.g726TempBuffer = this.g726TempBuffer == null ? new Int16Array(pcm16BitData.length) : new Int16Array(pcm16BitData.length + g726OffsetBuffer.length);
      if(g726OffsetBuffer != null) {
        this.g726TempBuffer.set(g726OffsetBuffer);
        this.g726TempBuffer.set(pcm16BitData, g726OffsetBuffer.length);
      } else {
        this.g726TempBuffer.set(pcm16BitData);
      }
      let i = 0;
      for(; i < this.g726TempBuffer.length; ) {
        if (this.recvPackageSampleCountArr.length<=0) {
          break;
        }
        if (this.recvPackageSampleCountArr[0]>(this.g726TempBuffer.length-i)) {
          break;
        }
        let step = this.recvPackageSampleCountArr.shift();

        let pcmPacketData = this.g726TempBuffer.slice(i, i + step);
        let rtpData = this.encode(pcmPacketData);

        if(rtpData.errorCode === Result.ErrorCode.SUCCESS) {
          // 音频片段发送给socket
          if (this.websocket.readyState === 1) {
            this.websocket.send(rtpData.data);
          }
        }
        i += step
      }
      if(this.g726TempBuffer.length - i > 0) {
        if (i>0) {
          g726OffsetBuffer = this.g726TempBuffer;
          this.g726TempBuffer = new Int16Array(g726OffsetBuffer.length - i);
          this.g726TempBuffer.set(g726OffsetBuffer.slice(i, g726OffsetBuffer.length));
        }
      } else {
        this.g726TempBuffer = null;
      }
    }
  }

  encode(pcmPacketData) {
    let encodedData;
    let payloadType = -1;
    switch (this.coderType) {
      case this.CODEC_TYPE.ADPCMA:
        this.audioCoder.resetEncodeState(new Adpcm.State(0, 0));
        let adpcmData = this.audioCoder.encode(pcmPacketData);
        let encodeState = this.audioCoder.getEncodeState();
        encodedData = new Uint8Array(8 + adpcmData.byteLength);
        encodedData[0] = 0;
        encodedData[1] = 1;
        encodedData[2] = 82;
        encodedData[3] = 0;
        encodedData[4] = 0;
        encodedData[5] = 0;
        encodedData[6] = 0;
        encodedData[7] = 0;
        encodedData.set(adpcmData, 8);
        break;
      case this.CODEC_TYPE.G726:
        encodedData = this.audioCoder.encode(pcmPacketData, 1);
        // Std.changeByteEndian(encodedData);
        break;
      case this.CODEC_TYPE.G711a:
        encodedData = this.audioCoder.encodeG711a(pcmPacketData);
        break;
      case this.CODEC_TYPE.G711u:
        encodedData = this.audioCoder.encodeG711u(pcmPacketData);
        break;
      case this.CODEC_TYPE.AAC:
        encodedData = this.audioEncoder.encodeG711a(pcmPacketData);
        payloadType = 6;
        break;
      default:
        return null;
    }


    let timestamp = this.recvPackageTimeArr.shift();
    return this.rtp.makeAudio(encodedData, timestamp,payloadType);
  }

  /**
   * 关闭socket
   */
  closeWebsocket() {
    if (this.websocket) {
      this.websocket.close();
    }
  }

  /**
   * socket超时连接
   */
  socketTimeout() {
    this.timeoutObj = setTimeout(() => {
      if ((!this.audioCoder && this.coderType !== this.CODEC_TYPE.NONE) && this.options.socketTimeoutFun) {
        if (this.audioCtx) {
          this.audioCtx.close().then(() => {
            this.destroy();
          })
        } else {
          this.destroy();
        }
        this.options.socketTimeoutFun();
      }
    }, 8000);
  }   

  /**
   * 插件数据重置
   */
  destroy() {
    if (this.heartbeat) {
      clearInterval(this.heartbeat);
      this.heartbeat = null;
    }
    this.g726TempBuffer = null;
    this.audioWasm = null;
    this.mediaStream = null;
    this.audioCtx = null;
    this.audioInput = null;
    this.recorder = null;
    this.websocket = null;
    this.audioCoder = null;
    clearTimeout(this.timeoutObj);
    this.timeoutObj = null;
    this.pcmPlayer = null;
  }

  /**
   * 设置音频的播放声音
   */
  setAudioVoice(index) {
    if (this.pcmPlayer !== null) {
      this.pcmPlayer.setVolume(index);
    }
  }

  /**
   * 手动销毁音频声音
   */
  destructionAudioContext() {
    if (this.pcmPlayer !== null) {
      this.pcmPlayer.close();
    }
  }
}
window.AudioCollect = AudioCollect;