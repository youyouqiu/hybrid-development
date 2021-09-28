package com.zw.platform.util.common;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UuidUtils {
    private static final Pattern SPLITTER = Pattern.compile(",");

    /**
     * UUId转byte[]数组
     */
    public static byte[] getBytesFromUUID(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        return bb.array();
    }

    /**
     * byte[]数组转UUID
     */
    public static UUID getUUIDFromBytes(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long high = byteBuffer.getLong();
        long low = byteBuffer.getLong();

        return new UUID(high, low);
    }

    /**
     * 字符串id转换为byte数组集合
     */
    public static List<byte[]> filterVid(String vehicleIds) {
        if (StringUtils.isBlank(vehicleIds)) {
            throw new NullPointerException();
        }

        String[] str = SPLITTER.split(vehicleIds);
        HashSet<String> ids = new HashSet<>(Arrays.asList(str));
        return ids.stream().map(UuidUtils::getBytesFromStr).collect(Collectors.toList());
    }

    /**
     * 字符串id转换为byte数组集合
     */
    public static List<String> filterVidStrList(String vehicleIds) {
        if (StringUtils.isBlank(vehicleIds)) {
            return Collections.emptyList();
        }

        String[] str = SPLITTER.split(vehicleIds);
        HashSet<String> hashSet = new HashSet<>(Arrays.asList(str));
        return new ArrayList<>(hashSet);
    }

    /**
     * 监控对象id集合转换为byte[]集合
     */
    public static List<byte[]> batchTransition(Collection<String> vehicleIds) {
        if (CollectionUtils.isEmpty(vehicleIds)) {
            throw new NullPointerException();
        }

        HashSet<String> ids = new HashSet<>(vehicleIds);
        return ids.stream().map(UuidUtils::getBytesFromStr).collect(Collectors.toList());
    }

    /**
     * string转byte
     */
    public static byte[] getBytesFromStr(String id) {
        UUID uuid = UUID.fromString(id);
        return getBytesFromUUID(uuid);
    }

    /**
     * byte[]数组转UUIDString
     */
    public static String getUUIDStrFromBytes(byte[] bytes) {
        UUID uuid = getUUIDFromBytes(bytes);
        return uuid.toString();
    }

    public static List<String> getUUIDStrListFromBytes(List<byte[]> bytes) {
        if (CollectionUtils.isEmpty(bytes)) {
            return Collections.emptyList();
        }
        return bytes.stream().map(UuidUtils::getUUIDStrFromBytes).collect(Collectors.toList());
    }
}
