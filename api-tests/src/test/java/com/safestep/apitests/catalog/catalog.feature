@catalog @US30 @US31
Feature: Store catalogue API (/api/v1/commerce)

  Background:
    * url karate.properties['api.baseUrl']
    * def player = call read('classpath:com/safestep/apitests/helpers/register-and-sign-in.feature')

  Scenario: A player lists the product catalogue
    Given path 'api/v1/commerce/products'
    And header Authorization = 'Bearer ' + player.token
    When method get
    Then status 200
    And match response == '#[_ > 29]'
    And match each response contains { id: '#string', name: '#string', price: '#number', stock: '#number' }

  Scenario: A player opens one product
    Given path 'api/v1/commerce/products/mochila-emergencia'
    And header Authorization = 'Bearer ' + player.token
    When method get
    Then status 200
    And match response.id == 'mochila-emergencia'
    And match response.price == 159.9

  Scenario: An unknown product is not found
    Given path 'api/v1/commerce/products/does-not-exist'
    And header Authorization = 'Bearer ' + player.token
    When method get
    Then status 404

  Scenario Outline: Reference catalogues are available - <resource>
    Given path 'api/v1/commerce/<resource>'
    And header Authorization = 'Bearer ' + player.token
    When method get
    Then status 200
    And match response == '#[_ > 0]'

    Examples:
      | resource   |
      | categories |
      | kits       |
      | coupons    |

  Scenario: The coupon catalogue only offers the two supported coupon types
    Given path 'api/v1/commerce/coupons'
    And header Authorization = 'Bearer ' + player.token
    When method get
    Then status 200
    And match each response contains { id: '#string', costCoins: '#number', discountPercentage: '#number' }
    And match each response[*].type == '#regex PERCENTAGE_OFF|PERCENTAGE_OFF_MIN_PURCHASE'
