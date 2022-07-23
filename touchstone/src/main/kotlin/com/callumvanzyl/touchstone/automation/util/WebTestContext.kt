package com.callumvanzyl.touchstone.automation.util

import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.support.ui.WebDriverWait

class WebTestContext {
    val webDriver: RemoteWebDriver = WebDriverManager.webDriver
    val webDriverWaiter = WebDriverWait(webDriver, 5)
}
