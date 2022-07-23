package com.callumvanzyl.touchstone.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.callumvanzyl.touchstone.configuration.WebSecurityConfiguration
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter

class JWTAuthorizationFilter(
    authenticationManager: AuthenticationManager
) : BasicAuthenticationFilter(authenticationManager) {

    private val jwtSecret: String = environment.getProperty("security.jwt.secret")!!

    @Throws(IOException::class, ServletException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        request.getHeader(WebSecurityConfiguration.HEADER_STRING)?.let { header ->
            if (header.startsWith("${WebSecurityConfiguration.TOKEN_PREFIX} ")) {
                SecurityContextHolder.getContext().authentication =
                    JWT.require(Algorithm.HMAC512(jwtSecret))
                        .build()
                        .verify(header.replace("${WebSecurityConfiguration.TOKEN_PREFIX} ", ""))
                        .subject
                        .let { UsernamePasswordAuthenticationToken(it, null, arrayListOf()) }
            }
        }

        chain.doFilter(request, response)
    }
}
