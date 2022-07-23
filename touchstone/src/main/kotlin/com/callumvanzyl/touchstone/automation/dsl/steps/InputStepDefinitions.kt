package com.callumvanzyl.touchstone.automation.dsl.steps

import com.callumvanzyl.touchstone.automation.util.ElementLocation
import com.callumvanzyl.touchstone.automation.util.WebTestContext
import com.callumvanzyl.touchstone.automation.util.toLocator
import io.cucumber.java.en.When
import org.openqa.selenium.support.ui.ExpectedConditions

class InputStepDefinitions(
    private val webTestContext: WebTestContext
) {

    @When("^I type (?:'|\")(.*)(?:'|\") into \\[(.*)\\](?:'|\")(.*)(?:'|\")\$")
    fun whenITypeTextIntoAnElementWithTheSelector(text: String, method: String, location: ElementLocation) =
        webTestContext.webDriverWaiter.until(
            ExpectedConditions.presenceOfElementLocated(
                location.toLocator(method)
            )
        ).sendKeys(text)
}
