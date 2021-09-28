package com.zw.platform.service.intercomplatform.impl;

import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.Personalized;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.repository.modules.PersonalizedDao;
import com.zw.platform.service.intercomplatform.PersonalizedService;
import lombok.extern.log4j.Log4j2;
import net.sf.image4j.codec.ico.ICODecoder;
import net.sf.image4j.codec.ico.ICOEncoder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Log4j2
@Service
public class PersonalizedServiceImpl implements PersonalizedService {
    @Autowired
    private PersonalizedDao personalizedDao;

    @Autowired
    private OrganizationService organizationService;

    @Override
    public boolean updateLogo(Personalized personalized) {
        personalized.setUpdateDataUsername(SystemHelper.getCurrentUsername()); // 修改人
        personalized.setUpdateDataTime(new Date()); // 修改时间
        return personalizedDao.updateLogo(personalized);
    }

    @Override
    public boolean update(Personalized newConfig, String rootPath, String groupId, String name) throws IOException {
        String logoPath = rootPath + "resources/img/logo/";
        String bgPath = rootPath + "resources/img/home/";
        boolean succeed;
        Personalized presentConfig = personalizedDao.find(groupId);
        if (null == presentConfig) {
            newConfig.setGroupId(groupId);
            succeed = add(newConfig);
        } else {
            newConfig.setGroupId(groupId);
            // 已设置后修改
            succeed = updateLogo(newConfig);
            if ("defult".equals(groupId) && name != null) {
                String filePath = logoPath + name;
                Files.deleteIfExists(Paths.get(filePath));
            }
        }
        if (succeed && null != presentConfig) {
            Personalized defultP = personalizedDao.find("defult");
            //删除原登录页Logo图片
            if (StringUtils.isNotBlank(presentConfig.getLoginLogo())
                    && !presentConfig.getLoginLogo().equals(newConfig.getLoginLogo())
                    && !presentConfig.getLoginLogo().equals(defultP.getLoginLogo())) {
                Files.deleteIfExists(Paths.get(logoPath + presentConfig.getLoginLogo()));
            }
            //删除原首页Logo图片
            if (StringUtils.isNotBlank(presentConfig.getHomeLogo())
                    && !presentConfig.getHomeLogo().equals(newConfig.getHomeLogo())
                    && !presentConfig.getHomeLogo().equals(defultP.getHomeLogo())) {
                Files.deleteIfExists(Paths.get(logoPath + presentConfig.getHomeLogo()));
            }
            //删除原视频窗口背景图
            if (StringUtils.isNotBlank(presentConfig.getVideoBackground())
                    && !presentConfig.getVideoBackground().equals(newConfig.getVideoBackground())
                    && !presentConfig.getVideoBackground().equals(defultP.getVideoBackground())) {

                Files.deleteIfExists(Paths.get(bgPath + presentConfig.getVideoBackground()));
            }
            //删除原网页标题ico
            if (StringUtils.isNotBlank(presentConfig.getWebIco())
                    && !presentConfig.getWebIco().equals(newConfig.getWebIco())
                    && !presentConfig.getWebIco().equals(defultP.getWebIco())) {
                Files.deleteIfExists(Paths.get(logoPath + presentConfig.getWebIco()));
            }
            //删除原登录页背景图
            if (StringUtils.isNotBlank(presentConfig.getLoginBackground())
                    && !presentConfig.getLoginBackground().equals(newConfig.getLoginBackground())
                    && !presentConfig.getLoginBackground().equals(defultP.getLoginBackground())) {
                Files.deleteIfExists(Paths.get(logoPath + presentConfig.getLoginBackground()));
            }
        }

        return succeed;
    }

    @Override
    public Personalized find(String groupUuid) {
        return personalizedDao.find(groupUuid);
    }

    @Override
    public Personalized findOrDefault(String groupUuid) {
        Personalized personalize = null;
        if (groupUuid == null) {
            personalize = personalizedDao.find("defult");
            personalize.setFrontPage("");
            return personalize;
        }
        // 通过UUID查询组织
        OrganizationLdap organization = organizationService.getOrganizationByUuid(groupUuid);
        if (organization != null && organization.getId() != null) {
            String groupId = organization.getId().toString();
            String[] strs = groupId.split(",");
            // 递归查询个性化配置，这一级往上一级找，如果都没有设置个性化信息，直到找到系统默认数据
            String uuid = groupUuid;
            for (int i = 0; i < strs.length; i++) {
                personalize = personalizedDao.find(uuid);
                if (personalize != null) {
                    break;
                }
                int beginIndex = groupId.indexOf(","); // 获取组织id(根据用户id得到用户所在部门)
                groupId = groupId.substring(beginIndex + 1); // 上级组织id
                uuid = organizationService.getOrgByEntryDn(groupId, false).getUuid(); // 上级组织的uuid
            }
            if (null == personalize) {
                personalize = personalizedDao.find("defult"); // 查询默认图标
                personalize.setFrontPage("");
            }
        }
        return personalize;
    }

    @Override
    public Personalized findByPermission(String groupId, List<String> roleIds) {
        if (StringUtils.isNotBlank(groupId) && roleIds != null && !roleIds.isEmpty()) {
            return personalizedDao.findByPermission(groupId, roleIds);
        }
        return null;
    }

    /**
     * 根据平台网址（platformSite）查询平台个性化设置参数
     */
    @Override
    public Personalized findByPlatformSite(String platformSite) {
        List<Personalized> byPlatformSite = personalizedDao.findByPlatformSite(platformSite);
        if (null != byPlatformSite && !byPlatformSite.isEmpty()) {
            return byPlatformSite.get(0);
        }
        return null;
    }

    @Override
    public boolean add(Personalized personalized) {

        personalized.setCreateDataUsername(SystemHelper.getCurrentUsername());
        personalized.setCreateDataTime(new Date());
        return personalizedDao.add(personalized);
    }

    @Override
    public String uploadImage(String path, MultipartFile file, List<String> validSuffixes) {
        try {
            // 文件保存路径
            Files.createDirectories(Paths.get(path));
            // 获取文件后缀名
            String suffix = getSuffix(file.getOriginalFilename().toLowerCase());
            // 判断文件后缀名是否有效
            if (validSuffixes.indexOf(suffix) == -1) {
                // 返回0 前端判断图片类型
                return "0";
            }
            String newName = String.format("%d%d.%s", new Date().getTime(), new Random().nextInt(100), suffix);
            if ("svg".equals(suffix) || "gif".equals(suffix)) {
                saveFile(path, file, newName);
                return newName;
            }
            BufferedImage bufferedImage = getBufferedImage(file.getInputStream(), suffix);
            if (bufferedImage == null || bufferedImage.getHeight() == 0 || bufferedImage.getWidth() == 0) {
                // 没有高度或宽度属性,非图片文件
                return "0";
            }
            saveImage(bufferedImage, path + newName, suffix);
            return newName;
        } catch (IOException e) {
            log.error("上传文件失败", e);
            return "0";
        }
    }

    /**
     * 获得缓存图像信息
     * ico文件单独获取
     * @param inputStream 输入流
     * @param suffix      后缀
     * @return BufferedImage
     */
    private BufferedImage getBufferedImage(InputStream inputStream, String suffix) throws IOException {
        BufferedImage bufferedImage = null;
        if ("ico".equals(suffix)) {
            List<BufferedImage> bufferedImageList = ICODecoder.read(inputStream);
            if (CollectionUtils.isNotEmpty(bufferedImageList)) {
                bufferedImage = bufferedImageList.get(0);
            }
        } else {
            bufferedImage = ImageIO.read(inputStream);
        }
        return bufferedImage;
    }

    private void saveFile(String path, MultipartFile file, String newName) throws IOException {
        file.transferTo(Paths.get(path + newName).toFile());
    }

    // 重新绘制图片，避免用户上传的图片文件中带有非法代码或病毒
    private void saveImage(BufferedImage srcImg, String newFileName, String suffix) {
        BufferedImage buffImg = redrawImage(srcImg, suffix);
        try (OutputStream os = new FileOutputStream(newFileName)) {
            if ("ico".equals(suffix)) {
                ICOEncoder.write(buffImg, os);
                return;
            }
            ImageIO.write(buffImg, suffix, os);
        } catch (IOException e) {
            log.error("保存图片失败", e);
        }
    }

    private BufferedImage redrawImage(BufferedImage srcImg, String suffix) {
        int imageType = BufferedImage.TYPE_INT_RGB;
        if (Objects.equals(suffix, "png")) {
            imageType = BufferedImage.TYPE_INT_ARGB;
        }
        if (Objects.equals(suffix, "ico")) {
            imageType = srcImg.getType();
        }
        BufferedImage buffImg = new BufferedImage(srcImg.getWidth(), srcImg.getHeight(), imageType);
        // 得到画笔对象
        Graphics2D g = buffImg.createGraphics();
        g.drawImage(srcImg, 0, 0, null);
        g.dispose();
        return buffImg;
    }

    private static String getSuffix(String filename) {
        int index = filename.lastIndexOf('.');
        if (index == -1) {
            return "";
        }
        return filename.substring(index + 1);
    }

}
