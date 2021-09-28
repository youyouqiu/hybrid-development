package com.zw.platform.basic.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * 文件工具
 */
public class FileUtil {
    private static final Logger logger = LogManager.getLogger(FileUtil.class);

    /**
     * 文件删除工具
     * @param filePath 文件路径
     * @param fileName 文件名称
     * @return 是否删除成功
     */
    public static boolean deleteFile(String filePath, String fileName) {
        File file = new File(filePath + fileName);
        return file.delete();
    }

    /**
     * 图标删除
     * @param file      上传文件
     * @param filePath  上传的路径
     * @param maxWith   上传允许的图片最大宽度
     * @param maxHeight 上传允许的图片最大高度
     * @param suffix    支持的后最名称
     * @return 上传结果
     */
    public static Map<String, Object> uploadImg(MultipartFile file, String filePath, int maxWith, int maxHeight,
        String suffix) {
        File newFile = new File(filePath);
        Map<String, Object> result = new HashMap<>(16);
        result.put("state", -1);
        result.put("imgName", "");
        //检查文件夹是否存在，不存在进行创建
        if (!newFile.exists() && !newFile.mkdirs()) {
            return result;
        }

        //获取文件名后缀,并进行校验
        String fileName = file.getOriginalFilename();
        String fileSuffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (!Objects.equals(fileSuffix, suffix)) {
            result.put("state", "1");
            return result;
        }
        //生成新的文件名
        String newFileName = System.currentTimeMillis() + "" + new Random().nextInt(100) + "." + fileSuffix;
        result.put("imgName", newFileName);

        try {
            //进行文件转存
            file.transferTo(new File(filePath + newFileName));
            //进行图标大小校验
            try (FileInputStream fileInputStream = new FileInputStream(filePath + newFileName)) {
                BufferedImage bufferedImg = ImageIO.read(fileInputStream);
                if (Objects.isNull(bufferedImg)) {
                    return result;
                }
                int imgWidth = bufferedImg.getWidth();
                int imgHeight = bufferedImg.getHeight();
                if (imgWidth <= maxWith && imgHeight <= maxHeight) {
                    result.put("state", "0");
                    result.put("width", imgWidth);
                    result.put("height", imgHeight);
                } else {
                    result.put("state", "1");
                    File deleteFile = new File(filePath + newFileName);
                    if (!deleteFile.delete()) {
                        logger.error("删除文件异常{}", filePath + newFileName);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("文件[{}]转存到[{}]失败", fileName, filePath + newFileName, e);
            return result;
        }
        return result;
    }
}
