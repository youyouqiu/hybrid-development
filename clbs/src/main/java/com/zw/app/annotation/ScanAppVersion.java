package com.zw.app.annotation;

import com.zw.app.entity.MyHashMap;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/***
 @Author gfw
 @Date 2018/12/10 14:04
 @Description 加载扫描
 @version 1.0
 **/
@Service
public class ScanAppVersion implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (event.getApplicationContext().getParent() != null) {
            return;
        }

        List<Class> classsFromPackage = getClasssFromPackage("com.zw.app.service");
        for (Class cls : classsFromPackage) {
            Method[] methods = cls.getMethods();
            for (Method method : methods) {
                AppMethodVersion annotation = method.getAnnotation(AppMethodVersion.class);
                List<AppVersionEntity> versionList = new ArrayList<>();
                if (annotation != null) {
                    for (String s : annotation.url()) {
                        if (MyHashMap.getInstance().get(s) == null) {
                            AppVersionEntity app = new AppVersionEntity();
                            app.setVersion(annotation.version().getValue());
                            app.setMethod(method.getName());
                            versionList.add(app);
                            MyHashMap.getInstance().put(s, versionList);
                        } else {
                            List<AppVersionEntity> appVersionEntities =
                                (List<AppVersionEntity>) MyHashMap.getInstance().get(s);
                            AppVersionEntity app = new AppVersionEntity();
                            app.setVersion(annotation.version().getValue());
                            app.setMethod(method.getName());
                            appVersionEntities.add(app);
                            Collections.sort(appVersionEntities);
                        }
                    }
                }
            }

        }
    }

    /**
     * 获得包下面的所有的class
     * @param pack package完整名称
     * @return List包含所有class的实例
     */
    public static List<Class> getClasssFromPackage(String pack) {
        List<Class> clazzs = new ArrayList<Class>();

        // 是否循环搜索子包
        boolean recursive = true;

        // 包名字
        String packageName = pack;
        // 包名对应的路径名称
        String packageDirName = packageName.replace('.', '/');

        Enumeration<URL> dirs;

        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();

                String protocol = url.getProtocol();

                if ("file".equals(protocol)) {
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    findClassInPackageByFile(packageName, filePath, recursive, clazzs);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return clazzs;
    }

    public static void findClassInPackageByFile(String packageName, String filePath, final boolean recursive,
        List<Class> clazzs) {
        File dir = new File(filePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        // 在给定的目录下找到所有的文件，并且进行条件过滤
        File[] dirFiles = dir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File file) {
                boolean acceptDir = recursive && file.isDirectory();// 接受dir目录
                boolean acceptClass = file.getName().endsWith("class");// 接受class文件
                return acceptDir || acceptClass;
            }
        });
        if (ArrayUtils.isEmpty(dirFiles)) {
            return;
        }
        for (File file : dirFiles) {
            if (file.isDirectory()) {
                findClassInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, clazzs);
            } else {
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    clazzs.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + "." + className));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
