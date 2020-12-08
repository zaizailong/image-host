package com.tuituidan.oss.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static springfox.documentation.builders.PathSelectors.regex;

/**
 * ApiDocConfig.
 *
 * @author zhujunhan
 * @version 1.0
 * @date 2020/8/4
 */
@Configuration
@EnableSwagger2
public class ApiDocConfig {

    /**
     * 通过配置控制是否开启.
     */
    @Value("${swagger.show:true}")
    private boolean swaggerShow;

    /**
     * Docket docket.
     *
     * @return the docket
     */
    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .enable(swaggerShow)
                .groupName("API_1.0")
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.tuituidan.oss.controller"))
                .paths(regex("/api/v1.*"))
                .build();
    }

    /**
     * ApiInfo.
     *
     * @return ApiInfo
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("图床API")
                .description("图床API。")
                .contact(new Contact("推推蛋", "", "tuituidan@163.com"))
                .termsOfServiceUrl("https://github.com/tuituidan/image-host")
                .version("1.0.0")
                .build();
    }
}
