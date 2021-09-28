package com.zw.platform.util.ffmpeg;

import com.zw.platform.util.FileType;
import org.junit.Test;

import static org.junit.Assert.*;

public class FileTypeTest {
    @Test
    public void testMp3() {
        assertEquals("mp3", FileType.match("49443304000000000023545353450000"));
    }

    @Test
    public void testMp4() {
        assertEquals("mp4", FileType.match("000000206674797069736F6D00000200"));
    }

    @Test
    public void testAvi() {
        assertEquals("avi", FileType.match("5249464666278F00415649204C495354"));
    }

    @Test
    public void testWav() {
        assertEquals("wav", FileType.match("52494646A099000057415645666D7420"));
    }

    @Test
    public void testJpg() {
        assertEquals("jpg", FileType.match("FFD8FFE000104A464946000101010060"));
    }

    @Test
    public void testWmv() {
        assertEquals("wmv", FileType.match("3026B2758E66CF11A6D900AA0062CE6C"));
    }

    @Test
    public void testFlv() {
        assertEquals("flv", FileType.match("464C5601050000000900000000120001"));
    }

    @Test
    public void testGif() {
        assertEquals("gif", FileType.match("47494638396110001000A2FF00FFFFFF"));
    }

    @Test
    public void testTif() {
        assertEquals("tif", FileType.match("4D4D002A00016F10803FE04FF004160D"));
    }

    @Test
    public void testMov() {
        assertEquals("mov", FileType.match("00000020667479707174202020050300"));
    }

}
