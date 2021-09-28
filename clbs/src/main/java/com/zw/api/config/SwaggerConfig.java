package com.zw.api.config;

import com.google.common.base.Predicates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.context.request.async.DeferredResult;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ImplicitGrantBuilder;
import springfox.documentation.builders.OAuthBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.GrantType;
import springfox.documentation.service.LoginEndpoint;
import springfox.documentation.service.OAuth;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.ApiKeyVehicle;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.List;
import java.util.Locale;

import static com.google.common.collect.Lists.newArrayList;


/*
* Restful API 访问路径: 
* http://IP:port/{context-path}/api-ui.html
* eg:http://localhost:8080/jd-config-web/swagger-ui.html 
*/
@EnableSwagger2
@Configuration
public class SwaggerConfig {
    @Autowired
    private MessageSource messageSource;

    @Bean
    public Docket appApi() {
        return new Docket(DocumentationType.SWAGGER_2)
            .groupName("mobile-api")
            .genericModelSubstitutes(DeferredResult.class)
            .useDefaultResponseMessages(false)
            .forCodeGeneration(true)
            .pathMapping("/app")
            .apiInfo(apiInfo())
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.zw.app.controller"))
            .build()
            .securitySchemes(newArrayList(securitySchema()))
            .securityContexts(newArrayList(securityContext()));
    }

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
            .groupName("clbs-api")
            .genericModelSubstitutes(DeferredResult.class)
            .useDefaultResponseMessages(false)
            .forCodeGeneration(true)
            .pathMapping("/")
            .apiInfo(apiInfo())
            .select()
            .apis(Predicates.or(RequestHandlerSelectors.basePackage("com.zw.api.controller"),
                    RequestHandlerSelectors.basePackage("com.zw.api2.controller")))
            .build()
            .securitySchemes(newArrayList(securitySchema()))
            .securityContexts(newArrayList(securityContext()));
    }

    private ApiInfo apiInfo() {
        Locale locale = LocaleContextHolder.getLocale();
        final String title = messageSource.getMessage("swagger.title", null, locale);
        final String contactName = messageSource.getMessage("swagger.contact.name", null, locale);
        final String contactUrl = messageSource.getMessage("swagger.contact.url", null, locale);
        final String contactEmail = messageSource.getMessage("swagger.contact.email", null, locale);
        final String version = messageSource.getMessage("web.version", null, locale);
        return new ApiInfoBuilder()
            .title(title)
            .contact(new Contact(contactName, contactUrl, contactEmail))
            .termsOfServiceUrl("")
            .version(version)
            .build();
    }

    @Bean
    public SecurityContext securityContext() {
        return SecurityContext.builder()
            .securityReferences(defaultAuth())
            .forPaths(PathSelectors.regex("/*.*"))
            .build();
    }

    List<SecurityReference> defaultAuth() {
        final AuthorizationScope authorizationScope
            = new AuthorizationScope("global", "所有权限");
        final AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return newArrayList(
            new SecurityReference("mykey", authorizationScopes));
    }

    @Bean
    SecurityScheme apiKey() {
        return new ApiKey("Authentication Token", "Authorization", "header");
    }

    public static final String securitySchemaOAuth2 = "oauth2schema";

    private OAuth securitySchema() {
        return new OAuth(securitySchemaOAuth2, newArrayList(scopes()), newArrayList(grantTypes()));
    }

    @Bean
    SecurityScheme oauths() {
        return new OAuthBuilder().name("mykey").grantTypes(grantTypes()).scopes(scopes()).build();
    }

    private List<AuthorizationScope> scopes() {
        return newArrayList(new AuthorizationScope("write", "可写"), new AuthorizationScope("read", "可读"),
            new AuthorizationScope("trust", "信任"));
    }

    private List<GrantType> grantTypes() {
        final GrantType grantType = new ImplicitGrantBuilder().loginEndpoint(
            new LoginEndpoint("/clbs/oauth/authorize")).build();
        return newArrayList(grantType);
    }

    @Bean
    public SecurityConfiguration security() {
        return new SecurityConfiguration("mobile_1", "secret_1", "clbs-realm", "clbs-api", "AuthenticationToken",
            ApiKeyVehicle.HEADER,
            "Authorization", ",");
    }
} 