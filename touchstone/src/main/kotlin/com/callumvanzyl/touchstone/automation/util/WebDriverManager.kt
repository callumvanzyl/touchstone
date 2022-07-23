package com.callumvanzyl.touchstone.automation.util

import java.io.OutputStream
import java.util.logging.Level
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.logging.LogType
import org.openqa.selenium.logging.LoggingPreferences
import org.openqa.selenium.remote.CapabilityType

class WebDriverManager {

    companion object {

        private fun createWebDriver() = ChromeDriver(
            ChromeDriverService.createDefaultService().apply {
                sendOutputTo(OutputStream.nullOutputStream())
            },
            // Required for Chrome to be run by a non-root user
            ChromeOptions().apply {
                addArguments("--disable-dev-shm-usage", "--disable-extensions", "--no-sandbox", "--headless", "--disable-web-security", "--window-size=1920,1080")
                setCapability(CapabilityType.LOGGING_PREFS, LoggingPreferences().apply { enable(LogType.BROWSER, Level.ALL) })
            }
        ).apply {
            manage().window().maximize()
        }

        @JvmStatic
        fun reset() {
            webDriver.manage().deleteAllCookies()
            webDriver.quit()
            webDriver = createWebDriver()
        }

        @JvmField
        var webDriver = createWebDriver()
    }
}
