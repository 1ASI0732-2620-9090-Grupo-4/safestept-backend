@ignore
Feature: Reusable helper - complete a medical simulation attempt

  Scenario: Complete a simulation
    # expects the arguments: token, slug and score
    # toISOString() is required: the Karate JS engine turns java.time.Instant into a JS Date, whose toString is not ISO-8601
    * def finishedAt = new Date().toISOString()
    * def startedAt = new Date(Date.now() - 300000).toISOString()
    Given url karate.properties['api.baseUrl']
    And path 'api/v1/simulations', slug, 'attempts'
    And header Authorization = 'Bearer ' + token
    And request { mode: 'evaluation', startedAt: '#(startedAt)', completedAt: '#(finishedAt)', score: '#(score)', totalSteps: 5, correctSteps: 4, timeElapsed: 300, errors: [] }
    When method post
    Then status 201
