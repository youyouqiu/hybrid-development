
typedef struct adpcm_state_t {
    short valprev;
    char index;
} adpcm_state;

static int indexTable[16] = {
    -1, -1, -1, -1, 2, 4, 6, 8,
    -1, -1, -1, -1, 2, 4, 6, 8,
};

static int stepsizeTable[89] = {
    7, 8, 9, 10, 11, 12, 13, 14, 16, 17,
    19, 21, 23, 25, 28, 31, 34, 37, 41, 45,
    50, 55, 60, 66, 73, 80, 88, 97, 107, 118,
    130, 143, 157, 173, 190, 209, 230, 253, 279, 307,
    337, 371, 408, 449, 494, 544, 598, 658, 724, 796,
    876, 963, 1060, 1166, 1282, 1411, 1552, 1707, 1878, 2066,
    2272, 2499, 2749, 3024, 3327, 3660, 4026, 4428, 4871, 5358,
    5894, 6484, 7132, 7845, 8630, 9493, 10442, 11487, 12635, 13899,
    15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767
};
    
static void adpcm_encoder(short *indata, char *outdata, int len, adpcm_state *state)
{
    char* outp = outdata;
    short* inp = indata;
    int valpred = state->valprev;
    int index = state->index;
    int step = stepsizeTable[index];
    int bufferstep = 1;
    int val = 0;
    int sign = 0;
    int delta = 0;
    int diff = 0;
    int vpdiff = 0;	
    int outputbuffer = 0;
    for ( ; len > 0 ; len--)
    {
        val = *inp++;
        diff = val - valpred;
        sign = (diff < 0) ? 8 : 0;
        if(sign) 
        {
            diff = (-diff);
        }
        delta = 0;
        vpdiff = (step >> 3);
        if(diff >= step) 
        {
            delta = 4;
            diff -= step;
            vpdiff += step;
        }
        step >>= 1;
        if(diff >= step ) 
        {
            delta |= 2;
            diff -= step;
            vpdiff += step;
        }
        step >>= 1;
        if(diff >= step) 
        {
            delta |= 1;
            vpdiff += step;
        }
        if(sign)
        {
            valpred -= vpdiff;
        }
        else
        {
            valpred += vpdiff;
        }
        if(valpred > 32767)
        {
            valpred = 32767;
        }
        else if(valpred < -32768)
        {
            valpred = -32768;
        }
        delta |= sign;
        index += indexTable[delta];
        if(index < 0) index = 0;
        if(index > 88) index = 88;
        step = stepsizeTable[index];
        if(bufferstep)
        {
            outputbuffer = (delta << 4) & 0xf0;
        } 
        else 
        {
            *outp++ = (delta & 0x0f) | outputbuffer;
        }
        bufferstep = !bufferstep;
    }
    if(!bufferstep)
    {
        *outp++ = outputbuffer;
    }
    state->valprev = valpred;
    state->index = index;
}


static void adpcm_decoder(char *indata, short *outdata, int len, adpcm_state *state)
{
	short* outp = outdata;
	char* inp = indata;
	int valpred = state->valprev;
	int index = state->index;
	int step = stepsizeTable[index];
	int bufferstep = 0;
    int sign = 0;
	int delta = 0;
	int inputbuffer = 0;
    int vpdiff = 0;	

	for ( ; len > 0 ; --len)
    {
		if(bufferstep) 
        {
			delta = inputbuffer & 0xf;
		} 
        else 
        {
			inputbuffer = *inp++;
			delta = (inputbuffer >> 4) & 0xf;
		}
		bufferstep = !bufferstep;
		index += indexTable[delta];
		if(index < 0)
        {
            index = 0;
        }
		if(index > 88) 
        {
            index = 88;
        }
		sign = delta & 8;
		delta = delta & 7;
		vpdiff = step >> 3;
		if(delta & 4) 
        {
            vpdiff += step;
        }
		if(delta & 2) 
        {
            vpdiff += step >> 1;
        }
		if(delta & 1)
        {
            vpdiff += step >> 2;
        }
		if(sign)
        {
			valpred -= vpdiff;
        }
		else
        {
			valpred += vpdiff;
        }
		if(valpred > 32767)
        {
			valpred = 32767;
        }
		else if(valpred < -32768)
        {
			valpred = -32768;
        }
		step = stepsizeTable[index];
		*outp++ = valpred;
	}
	state->valprev = valpred;
	state->index = index;
}

static adpcm_state adpcmState[64] = {0};

void initAdpcmState(index)
{
    adpcmState[index].valprev = 0;
    adpcmState[index].index = 0;
}

void decodeAdpcm(int index, char* indata, short* outdata, int len)
{
    adpcm_decoder(indata, outdata, len, adpcmState + index);
}

void encodeAdpcm(int index, short* indata, char* outdata, int len)
{
    adpcm_encoder(indata, outdata, len, adpcmState + index);
}
