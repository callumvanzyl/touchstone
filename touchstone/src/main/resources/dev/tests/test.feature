Feature: A test

  Scenario: A test
    Given I navigate to the web page http://www.reading.ac.uk/
    When I click an element with the selector //*[@id="header"]/header/div/div/div/section[2]/ul[1]/li[1]/a
    Then verify the text "CHOOSE A SUBJECT" is on the page