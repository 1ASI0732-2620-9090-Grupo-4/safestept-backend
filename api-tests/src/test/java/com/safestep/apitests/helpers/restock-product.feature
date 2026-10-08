@ignore
Feature: Reusable helper - an administrator restocks a product so repeated runs never exhaust the stock

  Scenario: Restock
    # expects the arguments: productId and stock
    * def admin = call read('classpath:com/safestep/apitests/helpers/sign-in-admin.feature')
    Given url karate.properties['api.baseUrl']
    And path 'api/v1/commerce/products', productId
    And header Authorization = 'Bearer ' + admin.token
    When method get
    Then status 200
    * def product = response
    * set product.stock = stock
    Given url karate.properties['api.baseUrl']
    And path 'api/v1/commerce/products', productId
    And header Authorization = 'Bearer ' + admin.token
    And request product
    When method put
    Then status 200
    And match response.stock == stock
