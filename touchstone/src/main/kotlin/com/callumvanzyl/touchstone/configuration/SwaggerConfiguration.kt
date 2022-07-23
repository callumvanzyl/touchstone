package com.callumvanzyl.touchstone.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.ApiKey
import springfox.documentation.service.Contact
import springfox.documentation.service.SecurityReference
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Import(BeanValidatorPluginsConfiguration::class)
@EnableSwagger2
@Configuration
class SwaggerConfiguration {

    @Bean
    fun api(): Docket = Docket(DocumentationType.SWAGGER_2)
        .apiInfo(metadata())
        .groupName("v1")
        .securityContexts(listOf(securityContext()))
        .securitySchemes(listOf(ApiKey(WebSecurityConfiguration.TOKEN_PREFIX, WebSecurityConfiguration.HEADER_STRING, "header")))
        .select()
        .apis(RequestHandlerSelectors.basePackage("com.callumvanzyl.touchstone.api.v1.controller"))
        .paths(PathSelectors.ant("/touchstone/api/v1/**"))
        .build()

    private fun metadata(): ApiInfo = ApiInfoBuilder()
        .title("Touchstone API")
        .description("API documentation for Touchstone")
        .termsOfServiceUrl("")
        .contact(Contact("Callum Van Zyl", null, "cy006007@student.reading.ac.uk"))
        .version("v1")
        .build()

    private fun securityContext(): SecurityContext =
        SecurityContext.builder()
            .securityReferences(listOf(SecurityReference(WebSecurityConfiguration.TOKEN_PREFIX, arrayOfNulls(0))))
            .forPaths(PathSelectors.ant("/touchstone/api/**"))
            .build()
}
