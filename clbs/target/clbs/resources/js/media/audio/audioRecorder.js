class AudioRecorder {
    constructor(opt){

        this.defaults = {
            // 录音数据回调
            onDataFunc : null,
            onDevicePresentFunc : null,
            onReadyFunc:null,
        };

        this.options = Object.assign({}, this.defaults, opt);


        this.mediaStream = null;
        this.audioCtx = null;
        this.recorder = null;
        this.timeoutObj = null;

        this.recvPackageSampleCountArr = new Array();

        this._mediaEncapsulation();
        this._mediaDevices();
    }


    /**
     * 判断浏览器是否支持getUserMedia
     * 针对支持getUserMedia的浏览器进行封装
     */
    _mediaEncapsulation() {
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
    _mediaDevices() {
        navigator.mediaDevices.getUserMedia({ audio: true })
            .then((stream) => {
                this.mediaStream = stream;
                if (this.options.onReadyFunc){
                    this.options.onReadyFunc();
                }
            })
            .catch((err) => {
                    console.error(err.name + ": " + err.message);
                    if (err.name === 'NotFoundError') {
                        layer.msg('请插入音频对讲设备');
                    }
                    if (this.options.onDevicePresentFunc !== null && typeof this.options.onDevicePresentFunc === 'function') {
                        this.options.onDevicePresentFunc();
                    }
                }
            );
    }
    /**
     * 开始音频采集
     */
    _startCollectAudio() {
        this.audioInput.connect(this.recorder);
        this.recorder.connect(this.audioCtx.destination);
    }

    /**
     * 采集成功后回调方法
     */
    _audioProcessFun(audioEvent) {
        if (!this.audioCtx || !this.options.onDataFunc)
            return;

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
        let step = 320;     // 每次320采样
        for(; i < this.g726TempBuffer.length; ) {
            // if (this.recvPackageSampleCountArr.length<=0) {
            //     break;
            // }
            if (step>(this.g726TempBuffer.length-i)) {
                break;
            }
            // let step = this.recvPackageSampleCountArr.shift();

            let pcmPacketData = this.g726TempBuffer.slice(i, i + step);
            this.options.onDataFunc(pcmPacketData);

            i += step;
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

    startRecord(){
        if (!this.mediaStream){
            return false;
        }

        let AudioContext = window.AudioContext || window.webkitAudioContext;
        if (!AudioContext){
            return false;
        }
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
        if (!createScript){
            return false;
        }
        this.recorder = createScript.apply(this.audioCtx, [4096, 1, 1]);
        if (!this.recorder){
            return false;
        }

        this._startCollectAudio();

        /**
         * 采集每一段音频流成功后的回调方法
         */
        this.recorder.onaudioprocess = this._audioProcessFun.bind(this)
        return true;
    }

    hasMediaStream(){
        return this.mediaStream;
    }

    pushAudioFrameSampleCount(sampleCount){
        this.recvPackageSampleCountArr.push(sampleCount);
    }

    stopRecord(){
        this.mediaStream = null;
        this.audioCtx = null;
        this.recorder = null;
        this.timeoutObj = null;
    }
}

window.AudioRecorder = AudioRecorder;