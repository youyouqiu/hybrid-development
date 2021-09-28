

class Adpcm
{
    constructor(wasm, memory)
    {
        this._wasm = wasm;
        this._memory = new Uint8Array(memory.buffer);
        this._indexDe = Adpcm._currentIndex;
        this._indexEn = Adpcm._currentIndex + 1;
        Adpcm._currentIndex = (Adpcm._currentIndex + 2) & 0x3F;
        this._wasm.instance.exports._initAdpcmState(this._indexDe);
        this._wasm.instance.exports._initAdpcmState(this._indexEn);
    }

    resetDecodeState(state)
    {
        this._wasm.instance.exports._resetAdpcmState(this._indexDe, state.valprev, state.index);
    }

    resetEncodeState(state)
    {
        this._wasm.instance.exports._resetAdpcmState(this._indexEn, state.valprev, state.index);
    }

    getDecodeState()
    {
        this._wasm.instance.exports._getAdpcmState(this._indexDe, 10236, 10238);
        return new Adpcm.State(this._memory[10236] + (this._memory[10237] << 8), this._memory[10238]);
    }

    getEncodeState()
    {
        this._wasm.instance.exports._getAdpcmState(this._indexEn, 20476, 20478);
        return new Adpcm.State(this._memory[20476] + (this._memory[20477] << 8), this._memory[20478]);
    }

    // 100 ~ 10240: 解码前音频  10240 ~ 20480：解码后音频
    decode(data)
    {
        this._memory.set(new Uint8Array(data.buffer, data.byteOffset, data.byteLength), 100);
        this._wasm.instance.exports._decodeAdpcm(this._indexDe, 100, 10240, data.byteLength * 2);
        return new Int16Array(this._memory.buffer, 10240, data.byteLength * 2);
    }

    // 20480 ~ 30960: 编码前音频  30960 ~ 41920：编码后音频
    encode(data)
    {
        this._memory.set(new Uint8Array(data.buffer, data.byteOffset, data.byteLength), 20480);
        this._wasm.instance.exports._encodeAdpcm(this._indexEn, 20480, 30960, data.byteLength / 2);
        return new Uint8Array(this._memory.buffer, 30960, data.byteLength / 4);
    }
}

Adpcm._currentIndex = 0;

Adpcm.State = class
{
    constructor(valprev, index)
    {
        this.valprev = valprev;
        this.index = index;
    }
};

window.Adpcm = Adpcm;