#ifndef GET_BITS_H
#define GET_BITS_H

#ifndef INT_MAX
#  define INT_MAX       2147483647
#endif /* INT_MAX */
#define FFMAX(a,b) ((a) > (b) ? (a) : (b))
#define FFMIN(a,b) ((a) > (b) ? (b) : (a))

#define AV_RB32(x)                                \
 (((unsigned int)((const unsigned char*)(x))[0] << 24) |    \
            (((const unsigned char*)(x))[1] << 16) |    \
            (((const unsigned char*)(x))[2] <<  8) |    \
             ((const unsigned char*)(x))[3])

#define AV_RL32(x)                                \
 (((unsigned int)((const unsigned char*)(x))[3] << 24) |    \
            (((const unsigned char*)(x))[2] << 16) |    \
            (((const unsigned char*)(x))[1] <<  8) |    \
             ((const unsigned char*)(x))[0])

#define OPEN_READER_NOSIZE(name, gb)            \
	unsigned int name ## _index = (gb)->index;  \
	unsigned int /*av_unused*/ name ## _cache

#define OPEN_READER(name, gb)                   \
	OPEN_READER_NOSIZE(name, gb);               \
	unsigned int name ## _size_plus8 = (gb)->size_in_bits_plus8

#define CLOSE_READER(name, gb) (gb)->index = name ## _index

#define UPDATE_CACHE_LE(name, gb) name ## _cache = \
        AV_RL32((gb)->buffer + (name ## _index >> 3)) >> (name ## _index & 7)

#define UPDATE_CACHE_BE(name, gb) name ## _cache = \
        AV_RB32((gb)->buffer + (name ## _index >> 3)) << (name ## _index & 7)

#define UPDATE_CACHE(name, gb) UPDATE_CACHE_BE(name, gb)

#define SKIP_COUNTER(name, gb, num) \
	name ## _index = FFMIN(name ## _size_plus8, name ## _index + (num))

#define LAST_SKIP_BITS(name, gb, num) SKIP_COUNTER(name, gb, num)

#define GET_CACHE(name, gb) ((unsigned int) name ## _cache)

#define NEG_USR32(a,s) (((unsigned int)(a))>>(32-(s)))
#define SHOW_UBITS_BE(name, gb, num) NEG_USR32(name ## _cache, num)
#define SHOW_UBITS(name, gb, num) SHOW_UBITS_BE(name, gb, num)

static inline unsigned zero_extend(unsigned val, unsigned bits)
{
    return (val << ((8 * sizeof(int)) - bits)) >> ((8 * sizeof(int)) - bits);
}

#define SHOW_UBITS_LE(name, gb, num) zero_extend(name ## _cache, num)

typedef struct GetBitContext {
    const unsigned char *buffer, *buffer_end;
    int index;
    int size_in_bits;
    int size_in_bits_plus8;
} GetBitContext;

static inline unsigned int get_bits(GetBitContext *s, int n) {
    register unsigned int tmp;
    OPEN_READER(re, s);
    UPDATE_CACHE(re, s);
    tmp = SHOW_UBITS(re, s, n);
    LAST_SKIP_BITS(re, s, n);
    CLOSE_READER(re, s);
    return tmp;
}

static inline unsigned int get_bits_le(GetBitContext *s, int n)
{
    register int tmp;
    OPEN_READER(re, s);
    UPDATE_CACHE_LE(re, s);
    tmp = SHOW_UBITS_LE(re, s, n);
    LAST_SKIP_BITS(re, s, n);
    CLOSE_READER(re, s);
    return tmp;
}

static inline int init_get_bits(GetBitContext *s, const unsigned char *buffer, int bit_size)
{
    int buffer_size;
    int ret = 0;

    if (bit_size >= INT_MAX - FFMAX(7, 64*8) || bit_size < 0 || !buffer) {
        bit_size    = 0;
        buffer      = NULL;
        ret         = -1;
    }

    buffer_size = (bit_size + 7) >> 3;

    s->buffer             = buffer;
    s->size_in_bits       = bit_size;
    s->size_in_bits_plus8 = bit_size + 8;
    s->buffer_end         = buffer + buffer_size;
    s->index              = 0;

    return ret;
}

#endif  /* GET_BITS_H */
