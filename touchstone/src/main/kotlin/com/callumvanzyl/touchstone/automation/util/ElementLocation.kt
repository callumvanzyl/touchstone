package com.callumvanzyl.touchstone.automation.util

import com.callumvanzyl.touchstone.exception.TouchstoneScriptException
import org.openqa.selenium.By

inline class ElementLocation(val location: String)

fun ElementLocation.toLocator(method: String) = when (method) {
    "id" -> By.id(location)
    "xpath" -> By.xpath(location)
    "css" -> By.cssSelector(location)
    else -> throw TouchstoneScriptException("$method is not a valid selection method")
}
