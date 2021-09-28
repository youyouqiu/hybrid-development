#!/bin/bash

emcc g726.c -Os -s WASM=1 -s SIDE_MODULE=1 -s ONLY_MY_CODE=1 -o g726.wasm
emcc g711.c -Os -s WASM=1 -s SIDE_MODULE=1 -s ONLY_MY_CODE=1 -o g711.wasm
emcc adpcm.c -Os -s WASM=1 -s SIDE_MODULE=1 -s ONLY_MY_CODE=1 -o adpcm.wasm
emcc g726.c g711.c adpcm.c -Os -s WASM=1 -s SIDE_MODULE=1 -s ONLY_MY_CODE=1 -s "EXPORTED_FUNCTIONS=['_initAdpcmState','_resetAdpcmState','_getAdpcmState','_decodeAdpcm','_encodeAdpcm','_decodeG711a','_decodeG711u','_encodeG711a','_encodeG711u','_initG726State','_decodeG726','_encodeG726']" -o audio.wasm
