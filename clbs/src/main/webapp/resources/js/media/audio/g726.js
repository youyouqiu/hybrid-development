
class G726
{
    constructor(wasm, memory, bitCount)
    {
        this._wasm = wasm;
        this._memory = new Uint8Array(memory.buffer);
        this._bitCount = bitCount;
        this._indexDe = G726._currentIndex;
        this._indexEn = G726._currentIndex + 1;
        G726._currentIndex = (G726._currentIndex + 2) & 0x3F;
        this.initG726State = this._wasm.instance.exports._initG726State;
        this.decodeG726 = this._wasm.instance.exports._decodeG726;
        this.encodeG726 = this._wasm.instance.exports._encodeG726;
        this.reset();
    }

    reset() {
        this.initG726State(this._indexDe, this._bitCount);
        this.initG726State(this._indexEn, this._bitCount);
    }

    // 100 ~ 10240：解码前音频  10240 ~ 20480：解码后音频
    decode(data, little_endian)
    {
        this._memory.set(new Uint8Array(data.buffer, data.byteOffset, data.byteLength), 100);
        const size = this.decodeG726(this._indexDe, 100, data.byteLength, 10240, little_endian);
        return new Int16Array(this._memory.buffer, 10240, size);
    }

    // 20480 ~ 30960：编码前音频  30960 ~ 41920：编码后音频
    encode(data, little_endian)
    {
        this._memory.set(new Uint8Array(data.buffer, data.byteOffset, data.byteLength), 20480);
        const size = this.encodeG726(this._indexEn, 20480, data.byteLength / 2, 30960, little_endian);
        return new Uint8Array(this._memory.buffer, 30960, size);
    }
}

G726._currentIndex = 0;

window.G726 = G726;