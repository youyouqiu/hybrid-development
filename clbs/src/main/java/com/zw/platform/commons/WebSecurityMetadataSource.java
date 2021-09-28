package com.zw.platform.commons;

import com.zw.platform.domain.core.Group;
import com.zw.platform.domain.core.Resource;
import com.zw.platform.service.core.RoleService;
import com.zw.platform.service.core.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * [核心处理逻辑]
 * <p>
 * 资源源数据定义，即定义某一资源可以被哪些角色访问
 * 建立资源与权限的对应关系
 * <p>
 * 也可以直接使用Spring提供的类 DefaultFilterInvocationSecurityMetadataSource
 * @author Fan Lu
 */
@Service
public class WebSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {
    private static Logger log = LogManager.getLogger(WebSecurityMetadataSource.class);

    private static Map<String, Collection<ConfigAttribute>> resourceMap = new HashMap<>();

    @Autowired
    private UserService userCenterService;

    @Autowired
    private RoleService roleService;

    /**
     * 初始化资源配置
     * <p>
     * spring 调用该方法的方式有2种
     * 方式1，方法上加注解：
     * 方式2，配置文件中 init-method 属性指定：
     * <beans:bean id="webSecurityMetadataSource" init-method="initResource"
     * class="com.tavenli.security.WebSecurityMetadataSource"/>
     */
    @PostConstruct
    public void initResource() {
        resourceMap.clear();
        Collection<ConfigAttribute> atts = new ArrayList<>();
        //取得当前系统所有可用角色
        List<Group> roles = this.userCenterService.getAllGroup();
        for (Group role : roles) {
            this.loadRole(role);
            ConfigAttribute ca = new SecurityConfig(role.getName());
            atts.add(ca);
        }
        ConfigAttribute anony = new SecurityConfig("ROLE_ANONYMOUS");
        atts.add(anony);
        //为超级管理员添加所有资源权限
        //		this.initSuperUserResource();

        //为所有用户添加访问首页的权限
        resourceMap.put("/loginPage.gsp", atts);
        resourceMap.put("/", atts);
    }

    /**
     * 根据角色获取资源列表放置到resourceMap
     */
    private void loadRole(Group role) {
        try {
            List<Resource> resources = roleService.getPermissionById(role.getId().toString());
            ConfigAttribute ca = new SecurityConfig(role.getName());
            //取角色有哪些资源的权限
            for (Resource menu : resources) {
                String menuUrl = menu.getPermValue();
                if (StringUtils.isBlank(menuUrl)) {
                    //不是菜单地址，跳过
                    continue;
                }
                //如果是URL资源，则建立角色与资源关系
                if (resourceMap.containsKey(menuUrl)) {
                    resourceMap.get(menuUrl).add(ca);
                } else {
                    Collection<ConfigAttribute> atts = new ArrayList<>();
                    atts.add(ca);
                    resourceMap.put(menuUrl, atts);
                }
            }
        } catch (Exception e) {
            log.error("获取资源列表异常", e);
        }
    }

    private void initSuperUserResource() {
        /*
		// 添加超级管理员角色
		//ROLE_SUPER 这个权限名字也是自己定义的
		ConfigAttribute superCA = new SecurityConfig("ROLE_SUPER");
		// 超级管理员有所有菜单权限
		List<MenuEntity> menus = this.uCenterService.getAllMenus();
		for (MenuEntity menu : menus) {
			String menuUrl = menu.getMenuUrl();
			if (StringUtils.isBlank(menuUrl)) {
				// 不是菜单地址，跳过
				continue;
			}

			if (resourceMap.containsKey(menuUrl)) {

				resourceMap.get(menuUrl).add(superCA);

			} else {

				Collection<ConfigAttribute> atts = new ArrayList<ConfigAttribute>();
				atts.add(superCA);
				resourceMap.put(menuUrl, atts);

			}

		}*/
    }

    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {

        HttpServletRequest request = ((FilterInvocation) object).getRequest();
        initResource();
        //resourceMap为哪些角色可以访问这些路径即资源{/u/users=[ROLE_1, ROLE_SUPER], /u/main=[ROLE_1, ROLE_2, ROLE_SUPER],
        // /u/menus=[ROLE_1, ROLE_SUPER], /u/roles=[ROLE_1, ROLE_SUPER]}
        for (Map.Entry<String, Collection<ConfigAttribute>> entry : resourceMap.entrySet()) {
            //AntPathRequestMatcher : 来自于Ant项目，是一种简单易懂的路径匹配策略。
            //RegexRequestMatcher : 如果 AntPathRequestMatcher 无法满足需求，
            //还可以选择使用更强大的RegexRequestMatcher，它支持使用正则表达式对URL地址进行匹配
            //检查请求url中是否包含resourceMap中的url，如果包含则返回相应的角色列表
            RequestMatcher requestMatcher = new AntPathRequestMatcher(entry.getKey());
            if (requestMatcher.matches(request)) {
                return entry.getValue();
            }
        }
        Collection<ConfigAttribute> attrs = new HashSet<>();
        ConfigAttribute anony = new SecurityConfig("ROLE_ANONYMOUS");
        String url = request.getRequestURI();
        url = url.substring(url.indexOf("/", url.indexOf("/") + 1));
        if (url.equals("/loginPage.gsp") || url.equals("/")) {
            List<Group> roles = this.userCenterService.getAllGroup();
            for (Group role : roles) {
                ConfigAttribute ca = new SecurityConfig(role.getName());
                attrs.add(ca);
            }
        }
        attrs.add(anony);
        return attrs;
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        Set<ConfigAttribute> allAttributes = new HashSet<>();
        for (Map.Entry<String, Collection<ConfigAttribute>> entry : resourceMap.entrySet()) {
            allAttributes.addAll(entry.getValue());
        }
        return allAttributes;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }

    public void reloadResource() {
        this.initResource();
    }

}
