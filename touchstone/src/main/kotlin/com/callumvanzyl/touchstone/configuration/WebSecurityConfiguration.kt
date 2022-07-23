package com.callumvanzyl.touchstone.configuration

import com.callumvanzyl.touchstone.security.InternalUserDetailsService
import com.callumvanzyl.touchstone.security.JWTAuthorizationFilter
import org.springframework.context.annotation.Bean
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@EnableWebSecurity
class WebSecurityConfiguration(
    private val internalUserDetailsService: InternalUserDetailsService
) : WebSecurityConfigurerAdapter() {

    override fun configure(builder: AuthenticationManagerBuilder) {
        builder
            .userDetailsService(internalUserDetailsService)
            .passwordEncoder(passwordEncoder())
    }

    override fun configure(http: HttpSecurity) {
        http.cors().and().csrf().disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            .authorizeRequests()
                .antMatchers("/api/v1/login").permitAll()
                .antMatchers("/api/v1/register").permitAll()
                .antMatchers("/v2/api-docs").permitAll()
                .antMatchers("/swagger-resources/**").permitAll()
                .antMatchers("/swagger-ui.html").permitAll()
                .antMatchers("/swagger-ui/**").permitAll()
                .anyRequest().authenticated()
                .and()
            .addFilter(JWTAuthorizationFilter(authenticationManager()))
    }

    @Bean
    fun authentication(): AuthenticationManager = authenticationManager()

    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder = BCryptPasswordEncoder()

    companion object {

        const val TOKEN_PREFIX = "Bearer"
        const val HEADER_STRING = "Authorization"
    }
}
