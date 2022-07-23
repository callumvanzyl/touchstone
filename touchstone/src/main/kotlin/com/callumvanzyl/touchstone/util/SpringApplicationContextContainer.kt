package com.callumvanzyl.touchstone.util

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Configuration

/**
 * This class is used to share the Spring application context with the Cucumber runtime
 */
@Configuration
class SpringApplicationContextContainer : ApplicationContextAware {

    override fun setApplicationContext(context: ApplicationContext) {
        Companion.context = context
    }

    companion object {

        @JvmStatic
        lateinit var context: ApplicationContext
    }
}
