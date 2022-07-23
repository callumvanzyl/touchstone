package com.callumvanzyl.touchstone.automation.dsl.steps

import com.callumvanzyl.touchstone.automation.util.ElementLocation
import com.callumvanzyl.touchstone.automation.util.WebTestContext
import com.callumvanzyl.touchstone.automation.util.toLocator
import io.cucumber.java.en.When
import org.openqa.selenium.support.ui.ExpectedConditions

class ButtonStepDefinitions(
    private val webTestContext: WebTestContext
) {

    @When("^I click on \\[(.*)\\](?:'|\")(.*)(?:'|\")\$")
    fun whenIClickAnElementWithTheSelector(method: String, location: ElementLocation) =
        webTestContext.webDriverWaiter.until(
            ExpectedConditions.presenceOfElementLocated(
                location.toLocator(method)
            )
        ).click()
}
