package com.zw.platform.util;

import javax.servlet.http.HttpServletRequest;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Tdz
 * @since  2018-02-27 10:49
 **/
public class IPAddrUtil {
    private static Map<String, String> ipAddrMap;

    private static final String LOCAL_HOST_IP = "127.0.0.1";

    private String serverIp = null;

    private IPAddrUtil() {
        ipAddrMap = new ConcurrentHashMap<>();
    }

    public static IPAddrUtil getInstance() {
        return IPAddrUtilHelper.INSTANCE;
    }

    private static class IPAddrUtilHelper {
        private static final IPAddrUtil INSTANCE = new IPAddrUtil();
    }

    public void putAddr(String userName, String ip) {
        ipAddrMap.put(userName, ip);
    }

    public String getAddr(String userName) {
        String ip = ipAddrMap.get(userName);
        if (ip == null) {
            return LOCAL_HOST_IP;
        }
        return ip;
    }

    /**
     * 获取服务器的IP地址
     */
    public String getServiceIp() {
        if (serverIp != null) {
            return serverIp;
        }
        // 根据网卡取本机配置的IP
        Enumeration<NetworkInterface> allNetInterfaces;  //定义网络接口枚举类
        try {
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();  //获得网络接口
            InetAddress ip; //声明一个InetAddress类型ip地址
            while (allNetInterfaces.hasMoreElements()) { //遍历所有的网络接口
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses(); //同样再定义网络地址枚举类
                while (addresses.hasMoreElements()) {
                    ip = addresses.nextElement();
                    if (ip instanceof Inet4Address) { //InetAddress类包括Inet4Address和Inet6Address
                        if (!ip.getHostAddress().equals(LOCAL_HOST_IP)) {
                            serverIp = ip.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            serverIp = LOCAL_HOST_IP;
        }
        return serverIp;
    }

    public static String getClientIp(HttpServletRequest request) {
        String ip = new GetIpAddr().getIpAddr(request);
        ip = getIpFromHeader(request, ip, "X-FORWARDED-FOR");
        ip = getIpFromHeader(request, ip, "Proxy-Client-IP");
        ip = getIpFromHeader(request, ip, "WL-Proxy-Client-IP");
        ip = getIpFromHeader(request, ip, "X-Real-IP");
        ip = getIpFromHeader(request, ip, "HTTP_CLIENT_IP");
        ip = getIpFromHeader(request, ip, "HTTP_X_FORWARDED_FOR");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = new GetIpAddr().getIpAddr(request);
        }
        // 尝试从cookie取ip
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private static String getIpFromHeader(HttpServletRequest request, String ip, String header) {
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader(header);
        }
        return ip;
    }
}
