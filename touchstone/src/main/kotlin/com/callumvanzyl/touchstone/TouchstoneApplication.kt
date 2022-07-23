package com.callumvanzyl.touchstone

import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TouchstoneApplication

fun main(args: Array<String>) {
    runApplication<TouchstoneApplication>(*args) {
        setBannerMode(Banner.Mode.OFF)
    }
}
