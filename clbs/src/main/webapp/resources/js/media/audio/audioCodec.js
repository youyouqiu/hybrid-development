
class AudioCodec {

    constructor(opt){
        this.defaults = {
            // 解码数据回调
            onDecodeFunc : null,
            // 编码数据回调
            onEncodeFunc : null,
        };

        this.options = Object.assign({}, this.defaults, opt);

        /**
         * 音频所需数据
         */
        this.aacDecoder = null;
        this.g726Coder = null;
        this.adpcmCoder = null;
        this.g711Coder = null;
        this.audioFormat = "";
        this.audioWasm = null;
        try {
            this.memory = this._getMemory();
            this.importObj = this._createWebAssemblyObj();
            this._createWasm();
        } catch (e) {
            console.error(e);
        }
    }


    _createWasm(){
        let audioWasm = null;
        if (typeof WebAssembly !== 'undefined') {
            audioWasm = window.audioWasmObject;
            if (audioWasm === undefined) {
                fetch('/clbs/resources/js/media/audio/audio.wasm').then((response) => response.arrayBuffer())
                    .then((bytes) => WebAssembly.instantiate(bytes, this.importObj))
                    .then((wasm) => {
                            this.audioWasm = wasm;
                            window.audioWasmObject = wasm;
                        }
                    );
            }
        }
        this.audioWasm = audioWasm;
    }
    /**
     * 获取memory
     */
    _getMemory() {
        var memory = null;
        if (typeof WebAssembly !== 'undefined') {
            var storageMemory = window.audioMemoryObject;
            memory = storageMemory === undefined ? new WebAssembly.Memory({initial: 256, maximum: 256}) : storageMemory;
            window.audioMemoryObject = memory;
        }
        return memory;
    }

    /**
     * 创建音频对象
     */
    _createWebAssemblyObj () {
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
     * aac 解码
     * @param rtpFrame
     */
    _aacAudioDecoding(data){
        if(!this.aacDecoder) {
            this.aacDecoder = new AACDecoder();
        }
        this.aacDecoder.decode(data, function(data, sampleRate, channels){
            if (this.options.onDecodeFunc){
                this.options.onDecodeFunc(data, sampleRate, channels);
            }
        }.bind(this));
    }

    /**
     * g711u处理
     */
    _g711uAudioDecoding(data) {
        if (this.g711Coder == null && this.audioWasm != null) {
            this.g711Coder = new G711(this.audioWasm, this.memory);
        }

        if (this.g711Coder != null) {
            let pcm16BitData = this.g711Coder.decodeG711u(data);
            let pcmFloat32Data = Std.shortToFloatData(pcm16BitData);
            if (this.options.onDecodeFunc){
                this.options.onDecodeFunc(pcmFloat32Data, 8000, 1);
            }
        }
    }
    _g711uAudioEncoding(data) {
        if (this.g711Coder == null && this.audioWasm != null) {
            this.g711Coder = new G711(this.audioWasm, this.memory);
        }

        if (this.g711Coder != null) {
            let encodedData = this.g711Coder.encodeG711u(data);
            if (this.options.onEncodeFunc){
                this.options.onEncodeFunc(encodedData);
            }
        }
    }

    /**
     * g711a处理
     */
    _g711aAudioDecoding(data) {
        if (this.g711Coder == null && this.audioWasm != null) {
            this.g711Coder = new G711(this.audioWasm, this.memory);
        }

        if (this.g711Coder != null) {
            let pcm16BitData = this.g711Coder.decodeG711a(data);
            let pcmFloat32Data = Std.shortToFloatData(pcm16BitData);
            if (this.options.onDecodeFunc){
                this.options.onDecodeFunc(pcmFloat32Data, 8000, 1);
            }
        }
    }
    _g711aAudioEncoding(data) {
        if (this.g711Coder == null && this.audioWasm != null) {
            this.g711Coder = new G711(this.audioWasm, this.memory);
        }

        if (this.g711Coder != null) {
            let encodedData = this.g711Coder.encodeG711a(data);
            if (this.options.onEncodeFunc){
                this.options.onEncodeFunc(encodedData);
            }
        }
    }

    /**
     * g726处理
     */
    _g726AudioDecoding(data, bitCount=4) {
        if (this.g726Coder == null && this.audioWasm != null) {
            this.g726Coder = new G726(this.audioWasm, this.memory, bitCount);
        }

        if (this.g726Coder != null) {
            // Std.changeByteEndian(data);
            let pcm16BitData = this.g726Coder.decode(data, 1);
            let pcmFloat32Data = Std.shortToFloatData(pcm16BitData);
            if (this.options.onDecodeFunc){
                this.options.onDecodeFunc(pcmFloat32Data, 8000, 1);
            }
        }
    }
    _g726AudioEncoding(data, bitCount=4) {
        if (this.g726Coder == null && this.audioWasm != null) {
            this.g726Coder = new G726(this.audioWasm, this.memory, bitCount);
        }

        if (this.g726Coder != null) {
            let encodedData = this.g726Coder.encode(data, 1);
            if (this.options.onEncodeFunc){
                this.options.onEncodeFunc(encodedData);
            }
        }
    }


    /**
     * adpcm
     */
    _adpcmAudioDecoding(data) {
        if(this.adpcmCoder == null && this.audioWasm != null) {
            this.adpcmCoder = new Adpcm(this.audioWasm, this.memory);
        }
        if(this.adpcmCoder != null) {
            let adpcmData = data.slice(4, data.byteLength);
            this.adpcmCoder.resetDecodeState(new Adpcm.State(data[0] + (data[1] << 8), data[2]));
            let pcm16BitData = this.adpcmCoder.decode(adpcmData);
            let pcmFloat32Data = Std.shortToFloatData(pcm16BitData);
            if (this.options.onDecodeFunc){
                this.options.onDecodeFunc(pcmFloat32Data, 8000, 1);
            }
        }
    }
    _adpcmAudioEncoding(data){
        if(this.adpcmCoder == null && this.audioWasm != null) {
            this.adpcmCoder = new Adpcm(this.audioWasm, this.memory);
        }
        if(this.adpcmCoder != null) {
            this.adpcmCoder.resetEncodeState(new Adpcm.State(0, 0));
            let adpcmData = this.adpcmCoder.encode(data);
            let encodeState = this.adpcmCoder.getEncodeState();
            let encodedData = new Uint8Array(8 + adpcmData.byteLength);
            encodedData[0] = 0;
            encodedData[1] = 1;
            encodedData[2] = 82;
            encodedData[3] = 0;
            encodedData[4] = 0;
            encodedData[5] = 0;
            encodedData[6] = 0;
            encodedData[7] = 0;
            encodedData.set(adpcmData, 8);

            if (this.options.onEncodeFunc){
                this.options.onEncodeFunc(encodedData);
            }
        }
    }

    encode(data, payloadType){
        switch (payloadType) {
            case 26:
                this._adpcmAudioEncoding(data);
                break;
            case 29:
                this._g726AudioDecoding(data,2);
                break;
            case 30:
                this._g726AudioDecoding(data,3);
                break;
            case 8:
            case 31:
                this._g726AudioDecoding(data,4);
                break;
            case 32:
                this._g726AudioDecoding(data,5);
                break;
            case 6:
                this._g711aAudioEncoding(data);
                break;
            case 7:
                this._g711uAudioEncoding(data);
                break;
            case 19:
                // aac使用g711a编码
                this._g711aAudioEncoding(data);
                break;
            default:
                return null;
        }
    }
    decode(data, payloadType){
        if (data && this.audioWasm && this.memory) {
            // console.log('payloadType='+payloadType+', package size='+(event.data.byteLength-26));
            // 检测是否有海思头(0x00 0x01 ?? 0x00, 第三个字节为后续音频数据长度的一半)，如果有，则去除
            if (data[0] === 0x00 && data[1] === 0x01 && data[3] === 0x00
                && (data[2] & 0xff) === (data.length - 4) / 2) {
                data = data.slice(4);
            }
            if (payloadType === 8) {
                if (this.audioFormat == "G726-16K"){
                    this._g726AudioDecoding(data, 2);
                } else if (this.audioFormat == "G726-24K"){
                    this._g726AudioDecoding(data, 3);
                } else if (this.audioFormat == "G726-32K"){
                    this._g726AudioDecoding(data, 4);
                } else if (this.audioFormat == "G726-40K"){
                    this._g726AudioDecoding(data, 5);
                } else {
                    this._g726AudioDecoding(data);
                }
            } else if (payloadType === 26) {
                this._adpcmAudioDecoding(data);
            } else if (payloadType === 7) {
                this._g711uAudioDecoding(data);
            } else if (payloadType === 6) {
                this._g711aAudioDecoding(data);
            } else if (payloadType === 29) {
                this._g726AudioDecoding(data, 2);
            } else if (payloadType === 30) {
                this._g726AudioDecoding(data, 3);
            } else if (payloadType === 31) {
                this._g726AudioDecoding(data, 4);
            } else if (payloadType === 32) {
                this._g726AudioDecoding(data, 5);
            } else if (payloadType === 19) {
                // AAC
                this._aacAudioDecoding(data);
            } else {
                console.error('unknow audio encoder');
                return '不支持的音频格式:'+payloadType;
            }
            return '';
        }
        return '解码器初始化失败！';
    }

    destroy(){
        this.g726Coder = null;
        this.audioWasm = null;
        this.adpcmCoder = null;
        this.g711Coder = null;
        if (this.aacDecoder !== null){
            this.aacDecoder.close();
            this.aacDecoder = null;
        }
    }

    setAudioFormat(v){this.audioFormat = v;}
}



window.AudioCodec = AudioCodec;