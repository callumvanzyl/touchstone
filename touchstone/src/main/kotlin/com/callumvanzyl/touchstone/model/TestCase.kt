package com.callumvanzyl.touchstone.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document("test_cases")
data class TestCase(

    @Id
    val id: String? = null,

    @Field("name")
    val name: String,

    @Field("script")
    val script: String = ""
)
