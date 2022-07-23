package com.callumvanzyl.touchstone.security

import com.callumvanzyl.touchstone.repository.UserRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class InternalUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails =
        userRepository.findByUsername(username)?.let { user ->
            User(user.username, user.password, emptyList())
        } ?: throw UsernameNotFoundException("User $username does not exist")
}
