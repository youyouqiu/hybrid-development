#include <limits.h>
#include <stdint.h>
#include <stdlib.h>
#include <emscripten.h>
#include "get_bits.h"
#include "put_bits.h"

/**
 * G.726 11-bit float.
 * G.726 Standard uses rather odd 11-bit floating point arithmetic for
 * numerous occasions. It's a mystery to me why they did it this way
 * instead of simply using 32-bit integer arithmetic.
 */
typedef struct Float11 {
    uint8_t sign;   /**< 1 bit sign */
    uint8_t exp;    /**< 4 bits exponent */
    uint8_t mant;   /**< 6 bits mantissa */
} Float11;

static inline unsigned av_mod_uintp2(unsigned a, unsigned p)
{
    return a & ((1U << p) - 1);
}

static inline int av_clip(int a, int amin, int amax)
{
    if      (a < amin) return amin;
    else if (a > amax) return amax;
    else               return a;
}

/**
 * Clip a signed integer into the -(2^p),(2^p-1) range.
 * @param  a value to clip
 * @param  p bit position to clip at
 * @return clipped value
 */
static inline int av_clip_intp2(int a, int p)
{
    if (((unsigned)a + (1 << p)) & ~((2 << p) - 1))
        return (a >> 31) ^ ((1 << p) - 1);
    else
        return a;
}

static const uint8_t ff_log2_tab[256]={
        0,0,1,1,2,2,2,2,3,3,3,3,3,3,3,3,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,
        5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,
        6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
        6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
        7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
        7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
        7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
        7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7
};

static inline int av_log2_16bit(unsigned int v)
{
    int n = 0;
    if (v & 0xff00) {
        v >>= 8;
        n += 8;
    }
    n += ff_log2_tab[v];

    return n;
}

static inline Float11* i2f(int i, Float11* f)
{
    f->sign = (i < 0);
    if (f->sign)
        i = -i;
    f->exp = av_log2_16bit(i) + !!i;
    f->mant = i? (i<<6) >> f->exp : 1<<5;
    return f;
}

static inline int16_t mult(Float11* f1, Float11* f2)
{
    int res, exp;

    exp = f1->exp + f2->exp;
    res = (((f1->mant * f2->mant) + 0x30) >> 4);
    res = exp > 19 ? res << (exp - 19) : res >> (19 - exp);
    return (f1->sign ^ f2->sign) ? -res : res;
}

static inline int sgn(int value)
{
    return (value < 0) ? -1 : 1;
}

typedef struct G726Tables {
    const int* quant;         /**< quantization table */
    const int16_t* iquant;    /**< inverse quantization table */
    const int16_t* W;         /**< special table #1 ;-) */
    const uint8_t* F;         /**< special table #2 */
} G726Tables;

typedef struct G726Context {
    G726Tables tbls;    /**< static tables needed for computation */

    Float11 sr[2];      /**< prev. reconstructed samples */
    Float11 dq[6];      /**< prev. difference */
    int a[2];           /**< second order predictor coeffs */
    int b[6];           /**< sixth order predictor coeffs */
    int pk[2];          /**< signs of prev. 2 sez + dq */

    int ap;             /**< scale factor control */
    int yu;             /**< fast scale factor */
    int yl;             /**< slow scale factor */
    int dms;            /**< short average magnitude of F[i] */
    int dml;            /**< long average magnitude of F[i] */
    int td;             /**< tone detect */

    int se;             /**< estimated signal for the next iteration */
    int sez;            /**< estimated second order prediction */
    int y;              /**< quantizer scaling factor for the next iteration */
    int code_size;
    const int* quant;
    const int16_t* iquant;
    const int16_t* W;
    const uint8_t* F;
} G726Context;

static const int quant_tbl16[] =                  /**< 16kbit/s 2 bits per sample */
           { 260, INT_MAX };
static const int16_t iquant_tbl16[] =
           { 116, 365, 365, 116 };
static const int16_t W_tbl16[] =
           { -22, 439, 439, -22 };
static const uint8_t F_tbl16[] =
           { 0, 7, 7, 0 };

static const int quant_tbl24[] =                  /**< 24kbit/s 3 bits per sample */
           {  7, 217, 330, INT_MAX };
static const int16_t iquant_tbl24[] =
           { INT16_MIN, 135, 273, 373, 373, 273, 135, INT16_MIN };
static const int16_t W_tbl24[] =
           { -4,  30, 137, 582, 582, 137,  30, -4 };
static const uint8_t F_tbl24[] =
           { 0, 1, 2, 7, 7, 2, 1, 0 };

static const int quant_tbl32[] =                  /**< 32kbit/s 4 bits per sample */
           { -125,  79, 177, 245, 299, 348, 399, INT_MAX };
static const int16_t iquant_tbl32[] =
         { INT16_MIN,   4, 135, 213, 273, 323, 373, 425,
                 425, 373, 323, 273, 213, 135,   4, INT16_MIN };
static const int16_t W_tbl32[] =
           { -12,  18,  41,  64, 112, 198, 355, 1122,
            1122, 355, 198, 112,  64,  41,  18, -12};
static const uint8_t F_tbl32[] =
           { 0, 0, 0, 1, 1, 1, 3, 7, 7, 3, 1, 1, 1, 0, 0, 0 };

static const int quant_tbl40[] =                  /**< 40kbit/s 5 bits per sample */
           { -122, -16,  67, 138, 197, 249, 297, 338,
              377, 412, 444, 474, 501, 527, 552, INT_MAX };
static const int16_t iquant_tbl40[] =
         { INT16_MIN, -66,  28, 104, 169, 224, 274, 318,
                 358, 395, 429, 459, 488, 514, 539, 566,
                 566, 539, 514, 488, 459, 429, 395, 358,
                 318, 274, 224, 169, 104,  28, -66, INT16_MIN };
static const int16_t W_tbl40[] =
           {   14,  14,  24,  39,  40,  41,   58,  100,
              141, 179, 219, 280, 358, 440,  529,  696,
              696, 529, 440, 358, 280, 219,  179,  141,
              100,  58,  41,  40,  39,  24,   14,   14 };
static const uint8_t F_tbl40[] =
           { 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 3, 4, 5, 6, 6,
             6, 6, 5, 4, 3, 2, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0 };

static const G726Tables G726Tables_pool[] =
           {{ quant_tbl16, iquant_tbl16, W_tbl16, F_tbl16 },
            { quant_tbl24, iquant_tbl24, W_tbl24, F_tbl24 },
            { quant_tbl32, iquant_tbl32, W_tbl32, F_tbl32 },
            { quant_tbl40, iquant_tbl40, W_tbl40, F_tbl40 }};


/**
 * Paragraph 4.2.2 page 18: Adaptive quantizer.
 */
static inline uint8_t quant(G726Context* c, int d)
{
    int sign, exp, i, dln;

    sign = i = 0;
    if (d < 0) {
        sign = 1;
        d = -d;
    }
    exp = av_log2_16bit(d);
    dln = ((exp<<7) + (((d<<7)>>exp)&0x7f)) - (c->y>>2);

    // while (c->tbls.quant[i] < INT_MAX && c->tbls.quant[i] < dln)
    while (c->quant[i] < INT_MAX && c->quant[i] < dln)
        ++i;

    if (sign)
        i = ~i;
    if (c->code_size != 2 && i == 0) /* I'm not sure this is a good idea */
        i = 0xff;

    return i;
}

EM_JS(void, call_log_decode, (int I, int dql, int dex, int dqt), {
    console.log('I:', I, 'dql:', dql, 'dex:', dex, 'dqt:', dqt);
});

EM_JS(void, call_log_result, (int result), {
    console.log('decode result:', result);
});

/**
 * Paragraph 4.2.3 page 22: Inverse adaptive quantizer.
 */
static inline int16_t inverse_quant(G726Context* c, int i)
{
    int dql, dex, dqt;

    // dql = c->tbls.iquant[i] + (c->y >> 2);
    dql = c->iquant[i] + (c->y >> 2);
    dex = (dql>>7) & 0xf;        /* 4-bit exponent */
    dqt = (1<<7) + (dql & 0x7f); /* log2 -> linear */
    //call_log_decode(i, dql, c->iquant[i], G726Tables_pool[3].iquant[i]);
    return (dql < 0) ? 0 : ((dqt<<dex) >> 7);
}

static int16_t g726_decode(G726Context* c, int I)
{
    int dq, re_signal, pk0, fa1, i, tr, ylint, ylfrac, thr2, al, dq0;
    Float11 f;
    int I_sig= I >> (c->code_size - 1);

    dq = inverse_quant(c, I);

    /* Transition detect */
    ylint = (c->yl >> 15);
    ylfrac = (c->yl >> 10) & 0x1f;
    thr2 = (ylint > 9) ? 0x1f << 10 : (0x20 + ylfrac) << ylint;
    tr= (c->td == 1 && dq > ((3*thr2)>>2));

    if (I_sig)  /* get the sign */
        dq = -dq;
    re_signal = (int16_t)(c->se + dq);

    /* Update second order predictor coefficient A2 and A1 */
    pk0 = (c->sez + dq) ? sgn(c->sez + dq) : 0;
    dq0 = dq ? sgn(dq) : 0;
    if (tr) {
        c->a[0] = 0;
        c->a[1] = 0;
        for (i=0; i<6; i++)
            c->b[i] = 0;
    } else {
        /* This is a bit crazy, but it really is +255 not +256 */
        fa1 = av_clip_intp2((-c->a[0]*c->pk[0]*pk0)>>5, 8);

        c->a[1] += 128*pk0*c->pk[1] + fa1 - (c->a[1]>>7);
        c->a[1] = av_clip(c->a[1], -12288, 12288);
        c->a[0] += 64*3*pk0*c->pk[0] - (c->a[0] >> 8);
        c->a[0] = av_clip(c->a[0], -(15360 - c->a[1]), 15360 - c->a[1]);

        for (i=0; i<6; i++)
            c->b[i] += 128*dq0*sgn(-c->dq[i].sign) - (c->b[i]>>8);
    }

    /* Update Dq and Sr and Pk */
    c->pk[1] = c->pk[0];
    c->pk[0] = pk0 ? pk0 : 1;
    c->sr[1] = c->sr[0];
    i2f(re_signal, &c->sr[0]);
    for (i=5; i>0; i--)
        c->dq[i] = c->dq[i-1];
    i2f(dq, &c->dq[0]);
    c->dq[0].sign = I_sig; /* Isn't it crazy ?!?! */

    c->td = c->a[1] < -11776;

    /* Update Ap */
    // c->dms += (c->tbls.F[I]<<4) + ((- c->dms) >> 5);
    // c->dml += (c->tbls.F[I]<<4) + ((- c->dml) >> 7);
    c->dms += (c->F[I]<<4) + ((- c->dms) >> 5);
    c->dml += (c->F[I]<<4) + ((- c->dml) >> 7);
    if (tr)
        c->ap = 256;
    else {
        c->ap += (-c->ap) >> 4;
        if (c->y <= 1535 || c->td || abs((c->dms << 2) - c->dml) >= (c->dml >> 3))
            c->ap += 0x20;
    }

    /* Update Yu and Yl */
    // c->yu = av_clip(c->y + c->tbls.W[I] + ((-c->y)>>5), 544, 5120);
    c->yu = av_clip(c->y + c->W[I] + ((-c->y)>>5), 544, 5120);
    c->yl += c->yu + ((-c->yl)>>6);

    /* Next iteration for Y */
    al = (c->ap >= 256) ? 1<<6 : c->ap >> 2;
    c->y = (c->yl + (c->yu - (c->yl>>6))*al) >> 6;

    /* Next iteration for SE and SEZ */
    c->se = 0;
    for (i=0; i<6; i++)
        c->se += mult(i2f(c->b[i] >> 2, &f), &c->dq[i]);
    c->sez = c->se >> 1;
    for (i=0; i<2; i++)
        c->se += mult(i2f(c->a[i] >> 2, &f), &c->sr[i]);
    c->se >>= 1;

    call_log_result(re_signal);
    return av_clip(re_signal * 4, -0xffff, 0xffff);
}

static int g726_reset(G726Context *c)
{
    int i;

    c->tbls = G726Tables_pool[c->code_size - 2];
    switch (c->code_size) {
        case 2:
            c->quant = quant_tbl16;
            c->iquant = iquant_tbl16;
            c->W = W_tbl16;
            c->F = F_tbl16;
            break;
        case 3:
            c->quant = quant_tbl24;
            c->iquant = iquant_tbl24;
            c->W = W_tbl24;
            c->F = F_tbl24;
            break;
        case 5:
            c->quant = quant_tbl40;
            c->iquant = iquant_tbl40;
            c->W = W_tbl40;
            c->F = F_tbl40;
            break;
        case 4:
        default:
            c->quant = quant_tbl32;
            c->iquant = iquant_tbl32;
            c->W = W_tbl32;
            c->F = F_tbl32;
            break;
    }
    for (i=0; i<2; i++) {
        c->sr[i].mant = 1<<5;
        c->pk[i] = 1;
    }
    for (i=0; i<6; i++) {
        c->dq[i].mant = 1<<5;
    }
    c->yu = 544;
    c->yl = 34816;

    c->y = 544;

    return 0;
}

static int16_t g726_encode(G726Context* c, int16_t sig)
{
    uint8_t i;

    i = av_mod_uintp2(quant(c, sig/4 - c->se), c->code_size);
    g726_decode(c, i);
    return i;
}

static G726Context context[2] = {0};

EMSCRIPTEN_KEEPALIVE void initG726State(int index, int code_size) {
    G726Context *c = context + index;
    c->code_size = code_size;
    g726_reset(c);
}

EMSCRIPTEN_KEEPALIVE int decodeG726(int index, uint8_t* g726_data, int g726_bytes, int16_t* outData, int little_endian) {
    G726Context *c = context + index;
    int i = 0;
    int samples = 0;
    GetBitContext gb;

    int out_samples = g726_bytes * 8 / c->code_size;
    init_get_bits(&gb, g726_data, g726_bytes * 8);
    while(out_samples--) {
        uint8_t code = little_endian ?
                        get_bits_le(&gb, c->code_size) :
                        get_bits(&gb, c->code_size);
        uint16_t sl = g726_decode(c, code);
        *(outData + samples++) = sl;
        call_log_result(sl);
    }
    return samples;
}

EMSCRIPTEN_KEEPALIVE int encodeG726(int index, int16_t* inData, int len, uint8_t* g726_data, int little_endian) {
    G726Context *c = context + index;
    int g726_bytes = 0;
    uint8_t code = 0;
    PutBitContext pb;

    g726_bytes = (len * c->code_size + 7) / 8;
    init_put_bits(&pb, g726_data, len);
    for (int i = 0;  i < len;  i++) {
        code = g726_encode(c, inData[i]);
        if (little_endian) {
            put_bits_le(&pb, c->code_size, code);
        } else {
            put_bits(&pb, c->code_size, code);
        }
    }

    if (little_endian) {
        flush_put_bits_le(&pb);
    } else {
        flush_put_bits(&pb);
    }
    return g726_bytes;
}
