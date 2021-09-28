package com.zw.platform.util;

/**
 * <p>
 * Title:博实结协议伪IP生成规则
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年06月13日 17:56
 */
public class BSJFakeIPUtil {

    public static String integerMobileIPAddress(String ssim) {
        try {
            int[] stemp = new int[4];
            String[] sip = new String[4];
            int ihigt;
            if (ssim.length() == 13 && ssim.startsWith("106")) {
                ssim = "1" + ssim.substring(3);
            }
            if (ssim.length() == 11) {
                stemp[0] = Integer.parseInt(ssim.substring(3, 5));
                stemp[1] = Integer.parseInt(ssim.substring(5, 7));
                stemp[2] = Integer.parseInt(ssim.substring(7, 9));
                stemp[3] = Integer.parseInt(ssim.substring(9, 11));
                ihigt = Integer.parseInt(ssim.substring(1, 3));
                if (ihigt > 45) {
                    ihigt -= 46;
                } else {
                    ihigt -= 30;
                }
            } else if (ssim.length() == 10) {
                stemp[0] = Integer.parseInt(ssim.substring(2, 4));
                stemp[1] = Integer.parseInt(ssim.substring(4, 6));
                stemp[2] = Integer.parseInt(ssim.substring(6, 8));
                stemp[3] = Integer.parseInt(ssim.substring(8, 10));
                ihigt = Integer.parseInt(ssim.substring(0, 2));
                if (ihigt > 45) {
                    ihigt -= 46;
                } else {
                    ihigt -= 30;
                }
            } else if (ssim.length() == 9) {
                stemp[0] = Integer.parseInt(ssim.substring(1, 3));
                stemp[1] = Integer.parseInt(ssim.substring(3, 5));
                stemp[2] = Integer.parseInt(ssim.substring(5, 7));
                stemp[3] = Integer.parseInt(ssim.substring(7, 9));
                ihigt = Integer.parseInt(ssim.substring(0, 1));
            } else if (ssim.length() < 9) {
                switch (ssim.length()) {
                    case 8:
                        ssim = "140" + ssim;
                        break;
                    case 7:
                        ssim = "1400" + ssim;
                        break;
                    case 6:
                        ssim = "14000" + ssim;
                        break;
                    case 5:
                        ssim = "140000" + ssim;
                        break;
                    case 4:
                        ssim = "1400000" + ssim;
                        break;
                    case 3:
                        ssim = "14000000" + ssim;
                        break;
                    case 2:
                        ssim = "140000000" + ssim;
                        break;
                    case 1:
                        ssim = "1400000000" + ssim;
                        break;
                    default:
                        break;
                }
                stemp[0] = Integer.parseInt(ssim.substring(3, 5));
                stemp[1] = Integer.parseInt(ssim.substring(5, 7));
                stemp[2] = Integer.parseInt(ssim.substring(7, 9));
                stemp[3] = Integer.parseInt(ssim.substring(9, 11));
                ihigt = Integer.parseInt(ssim.substring(1, 3));
                if (ihigt > 45) {
                    ihigt -= 46;
                } else {
                    ihigt -= 30;
                }
            } else {
                return "";
            }

            if ((ihigt & 0x8) != 0) {
                sip[0] = String.valueOf((stemp[0] | 128));
            } else {
                sip[0] = String.valueOf(stemp[0]);
            }
            if ((ihigt & 0x4) != 0) {
                sip[1] = String.valueOf(stemp[1] | 128);
            } else {
                sip[1] = String.valueOf(stemp[1]);
            }
            if ((ihigt & 0x2) != 0) {
                sip[2] = String.valueOf(stemp[2] | 128);
            } else {
                sip[2] = String.valueOf(stemp[2]);
            }
            if ((ihigt & 0x1) != 0) {
                sip[3] = String.valueOf(stemp[3] | 128);
            } else {
                sip[3] = String.valueOf(stemp[3]);
            }
            return sip[0] + sip[1] + sip[2] + sip[3];
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * 伪IP转SIM卡号
     * @param fi1
     * @param fi2
     * @param fi3
     * @param fi4
     * @return
     */
    public static String fakeIpToSim(int fi1, int fi2, int fi3, int fi4) {
        int[] sip = new int[] { fi1, fi2, fi3, fi4 };
        String[] stemp = new String[4];
        if ((sip[0] & 127) < 10) {
            stemp[0] = "0" + String.valueOf(sip[0] & 127);
        } else {
            stemp[0] = String.valueOf(sip[0] & 127);
        }
        if ((sip[1] & 127) < 10) {
            stemp[1] = "0" + String.valueOf(sip[1] & 127);
        } else {
            stemp[1] = String.valueOf(sip[1] & 127);
        }
        if ((sip[2] & 127) < 10) {
            stemp[2] = "0" + String.valueOf(sip[2] & 127);
        } else {
            stemp[2] = String.valueOf(sip[2] & 127);
        }
        if ((sip[3] & 127) < 10) {
            stemp[3] = "0" + String.valueOf(sip[3] & 127);
        } else {
            stemp[3] = String.valueOf(sip[3] & 127);
        }
        String ihigt = "";
        if ((sip[0] & 128) != 0) {
            ihigt += "1";
        } else {
            ihigt += "0";
        }
        if ((sip[1] & 128) != 0) {
            ihigt += "1";
        } else {
            ihigt += "0";
        }
        if ((sip[2] & 128) != 0) {
            ihigt += "1";
        } else {
            ihigt += "0";
        }
        if ((sip[3] & 128) != 0) {
            ihigt += "1";
        } else {
            ihigt += "0";
        }
        int head = binaryToDecimal(Integer.parseInt(ihigt)) + 30;
        if (head < 40) {
            head += 16;
        }
        return "106" + head + stemp[0] + stemp[1] + stemp[2] + stemp[3];
    }

    /**
     * 二进制转换为十进制
     * @param binaryNumber
     * @return
     */
    public static int binaryToDecimal(int binaryNumber) {

        int decimal = 0;
        int p = 0;
        while (true) {
            if (binaryNumber == 0) {
                break;
            } else {
                int temp = binaryNumber % 10;
                decimal += temp * Math.pow(2, p);
                binaryNumber = binaryNumber / 10;
                p++;
            }
        }
        return decimal;
    }

}
