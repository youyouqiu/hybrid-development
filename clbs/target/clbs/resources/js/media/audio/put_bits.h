#ifndef PUT_BITS_H
#define PUT_BITS_H

#define AV_WB32(p, val) do {                \
    uint32_t d = (val);                     \
    ((uint8_t*)(p))[3] = (d);               \
    ((uint8_t*)(p))[2] = (d)>>8;            \
    ((uint8_t*)(p))[1] = (d)>>16;           \
    ((uint8_t*)(p))[0] = (d)>>24;           \
} while(0)

#define AV_WL32(p, val) do {                \
    uint32_t d = (val);                     \
    ((uint8_t*)(p))[0] = (d);               \
    ((uint8_t*)(p))[1] = (d)>>8;            \
    ((uint8_t*)(p))[2] = (d)>>16;           \
    ((uint8_t*)(p))[3] = (d)>>24;           \
} while(0)

typedef struct PutBitContext {
    uint32_t bit_buf;
    int bit_left;
    uint8_t *buf, *buf_ptr, *buf_end;
    int size_in_bits;
} PutBitContext;

static inline void init_put_bits(PutBitContext *s, uint8_t *buffer, int buffer_size) {
    if (buffer_size < 0) {
        buffer_size = 0;
        buffer      = NULL;
    }

    s->size_in_bits = 8 * buffer_size;
    s->buf          = buffer;
    s->buf_end      = s->buf + buffer_size;
    s->buf_ptr      = s->buf;
    s->bit_left     = 32;
    s->bit_buf      = 0;
}

static inline void put_bits(PutBitContext *s, int n, unsigned int value) {
    unsigned int bit_buf;
    int bit_left;

    bit_buf  = s->bit_buf;
    bit_left = s->bit_left;

    if (n < bit_left) {
        bit_buf     = (bit_buf << n) | value;
        bit_left   -= n;
    } else {
        bit_buf   <<= bit_left;
        bit_buf    |= value >> (n - bit_left);
        if (3 < s->buf_end - s->buf_ptr) {
            AV_WB32(s->buf_ptr, bit_buf);
            s->buf_ptr += 4;
        }
        bit_left   += 32 - n;
        bit_buf     = value;
    }

    s->bit_buf  = bit_buf;
    s->bit_left = bit_left;
}

static inline void put_bits_le(PutBitContext *s, int n, unsigned int value) {
    unsigned int bit_buf;
    int bit_left;

    bit_buf  = s->bit_buf;
    bit_left = s->bit_left;

    bit_buf |= value << (32 - bit_left);
    if (n >= bit_left) {
        if (3 < s->buf_end - s->buf_ptr) {
            AV_WL32(s->buf_ptr, bit_buf);
            s->buf_ptr += 4;
        }
        bit_buf     = value >> bit_left;
        bit_left   += 32;
    }
    bit_left -= n;

    s->bit_buf  = bit_buf;
    s->bit_left = bit_left;
}

static inline void flush_put_bits(PutBitContext *s) {
    while (s->bit_left < 32) {
        *s->buf_ptr++ = s->bit_buf >> 24;
        s->bit_buf  <<= 8;
        s->bit_left  += 8;
    }
    s->bit_left = 32;
    s->bit_buf  = 0;
}

static inline void flush_put_bits_le(PutBitContext *s) {
    while (s->bit_left < 32) {
        *s->buf_ptr++ = s->bit_buf;
        s->bit_buf  >>= 8;
        s->bit_left  += 8;
    }
    s->bit_left = 32;
    s->bit_buf  = 0;
}

#endif  /* PUT_BITS_H */
