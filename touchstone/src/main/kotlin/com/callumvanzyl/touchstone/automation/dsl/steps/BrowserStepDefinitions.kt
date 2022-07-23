package com.callumvanzyl.touchstone.automation.dsl.steps

import com.callumvanzyl.touchstone.annotation.TouchstoneDefinition
import com.callumvanzyl.touchstone.annotation.TouchstoneStep
import com.callumvanzyl.touchstone.automation.util.ElementLocation
import com.callumvanzyl.touchstone.automation.util.WebTestContext
import com.callumvanzyl.touchstone.automation.util.toLocator
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import java.time.Duration
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions

@TouchstoneDefinition(name = "Browser", description = "Automate browser operations")
class BrowserStepDefinitions(
    private val webTestContext: WebTestContext
) {

    @TouchstoneStep(
        name = "Navigate to a web page",
        description = "",
        example = """
            Given I navigate to the web page http://www.reading.ac.uk/
            Then verify the text "View courses" is on the page
        """
    )
    @Given("^I navigate to the (?:web page|webpage)? (.*)\$")
    fun givenINavigateToTheWebPage(url: String) =
        webTestContext.webDriver.get(url)

    @Then("^wait for (.*) seconds\$")
    fun waitForSeconds(seconds: Number) =
        Thread.sleep(Duration.ofSeconds(seconds.toLong()).toMillis())

    @TouchstoneStep(
        name = "Verify text is on the page",
        description = "",
        example = """
            Given I navigate to the web page http://www.reading.ac.uk/
            Then verify the text "View courses" is on the page
        """
    )
    @Then("^verify the text (?:'|\")(.*)(?:'|\") is on the page\$")
    fun thenVerifyTheTextIsOnThePage(text: String): WebElement? =
        webTestContext.webDriverWaiter.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//*[contains(text(), '$text')]")
                )
        )

    @Then("^verify the element \\[(.*)\\](?:'|\")(.*)(?:'|\") is on the page\$")
    fun thenVerifyTheElementIsOnThePage(method: String, location: ElementLocation): WebElement? =
        webTestContext.webDriverWaiter.until(
            ExpectedConditions.presenceOfElementLocated(
                location.toLocator(method)
            )
        )
}
