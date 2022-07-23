package com.callumvanzyl.touchstone.repository

import com.callumvanzyl.touchstone.model.User
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : MongoRepository<User, String> {

    @Query("{ 'username' : ?0 }")
    fun findByUsername(username: String): User?
}
