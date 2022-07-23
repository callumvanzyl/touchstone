package com.callumvanzyl.touchstone.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.callumvanzyl.touchstone.configuration.WebSecurityConfiguration
import com.callumvanzyl.touchstone.exception.InvalidCredentialsException
import com.callumvanzyl.touchstone.exception.InvalidOperationException
import com.callumvanzyl.touchstone.model.User
import com.callumvanzyl.touchstone.repository.UserRepository
import com.callumvanzyl.touchstone.util.Outcome
import java.time.Instant
import java.util.Date
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

interface UserService {
    fun register(user: User): Outcome<User>
    fun login(user: User): Outcome<String>
    fun getUsernameFromToken(token: String): Outcome<String>
}

@Service("UserService")
class UserServiceImpl(
    @Value("\${security.jwt.secret}") private val jwtSecret: String,
    @Value("\${security.jwt.expiration-time}") private val jwtExpirationTime: Long,

    private val userRepository: UserRepository,

    private val authentication: AuthenticationManager,
    private val passwordEncoder: BCryptPasswordEncoder
) : UserService {

    override fun register(user: User) =
        userRepository.findByUsername(user.username)?.let {
            Outcome.Error(reason = "A user with the username ${user.username} already exists", exception = InvalidOperationException())
        } ?: Outcome.Success(userRepository.save(user.copy(password = passwordEncoder.encode(user.password))))

    override fun login(user: User): Outcome<String> =
        userRepository.findByUsername(user.username)?.let { userResult ->
            val authAttempt: Authentication = try {
                authentication.authenticate(
                    UsernamePasswordAuthenticationToken(
                        user.username,
                        user.password,
                        arrayListOf()
                    )
                )
            } catch (err: BadCredentialsException) {
                return Outcome.Error(reason = "Bad credentials provided, please try again", exception = InvalidCredentialsException())
            }

            if (authAttempt.isAuthenticated) {
                if (userResult.enabled) {
                    val token = JWT.create()
                        .withSubject(user.username)
                        .withExpiresAt(Date.from(Instant.now().plusSeconds(jwtExpirationTime)))
                        .sign(Algorithm.HMAC512(jwtSecret))
                    Outcome.Success("${WebSecurityConfiguration.TOKEN_PREFIX} $token")
                } else {
                    Outcome.Error(reason = "This user is not enabled, please contact your administrator", exception = InvalidCredentialsException())
                }
            } else {
                Outcome.Error(reason = "Bad credentials provided, please try again", exception = InvalidCredentialsException())
            }
        } ?: Outcome.Error(reason = "A user with the username ${user.username} could not be found", exception = InvalidCredentialsException())

    override fun getUsernameFromToken(token: String): Outcome<String> = try {
        Outcome.Success(JWT.decode(token).subject)
    } catch (err: Exception) {
        Outcome.Error(reason = "Invalid token", exception = InvalidCredentialsException())
    }
}
