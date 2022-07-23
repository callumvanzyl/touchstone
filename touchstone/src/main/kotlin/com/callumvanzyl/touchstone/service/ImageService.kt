package com.callumvanzyl.touchstone.service

import com.callumvanzyl.touchstone.util.Outcome
import java.io.File
import java.io.InputStream
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.gridfs.GridFsOperations
import org.springframework.data.mongodb.gridfs.GridFsTemplate
import org.springframework.stereotype.Service

interface ImageService {
    fun storeImage(file: File): Outcome<String>
    fun getImage(imageName: String): Outcome<InputStream>
}

@Service("ImageService")
class ImageServiceImpl(
    private val template: GridFsTemplate,
    private val operations: GridFsOperations
) : ImageService {

    override fun storeImage(file: File): Outcome<String> =
        Outcome.Success(template.store(file.inputStream(), file.name, "image/png").toString())

    override fun getImage(imageName: String): Outcome<InputStream> =
        template.findOne(Query(Criteria.where("filename").isEqualTo(imageName))).let { Outcome.Success(operations.getResource(it).inputStream) }
}
