@commerce @coupons @US15 @US40 @US42
Feature: End-to-end SafeCoins coupon flow

  Background:
    * url karate.properties['api.baseUrl']
    * def player = call read('classpath:com/safestep/apitests/helpers/register-and-sign-in.feature')
    * call read('classpath:com/safestep/apitests/helpers/restock-product.feature') { productId: 'mochila-emergencia', stock: 100 }

  Scenario: A player earns SafeCoins, redeems a coupon and uses it in an order
    # 1. Earn coins by completing the same simulation twice (101 SafeCoins each)
    * call read('classpath:com/safestep/apitests/helpers/complete-simulation.feature') { token: '#(player.token)', slug: 'rcp-basico', score: 90 }
    * call read('classpath:com/safestep/apitests/helpers/complete-simulation.feature') { token: '#(player.token)', slug: 'rcp-basico', score: 95 }
    Given path 'api/v1/gamification/summary/me'
    And header Authorization = 'Bearer ' + player.token
    When method get
    Then status 200
    And match response.safeCoins == 202

    # 2. Redeem the 5% coupon, which costs 150 SafeCoins
    Given path 'api/v1/commerce/coupons/cpn-5/redeem'
    And header Authorization = 'Bearer ' + player.token
    And request {}
    When method post
    Then status 201
    And match response.status == 'AVAILABLE'
    And match response.discountPercentage == 5
    * def redeemedId = response.id
    Given path 'api/v1/gamification/summary/me'
    And header Authorization = 'Bearer ' + player.token
    When method get
    Then status 200
    And match response.safeCoins == 52

    # 3. Use the redeemed coupon in an order
    Given path 'api/v1/commerce/cart/items'
    And header Authorization = 'Bearer ' + player.token
    And request { productId: 'mochila-emergencia', quantity: 1 }
    When method post
    Then status 201
    Given path 'api/v1/commerce/orders'
    And header Authorization = 'Bearer ' + player.token
    And request { status: 'PENDING', redeemedCouponExternalId: '#(redeemedId)' }
    When method post
    Then status 201
    And match response.total == 159.9
    And match response.finalTotal == 151.9
    And match response.appliedDiscountPercentage == 5

    # 4. The coupon is now consumed
    Given path 'api/v1/commerce/coupons/redeemed/me'
    And header Authorization = 'Bearer ' + player.token
    When method get
    Then status 200
    And match response[0].status == 'USED'

  Scenario: Redeeming a coupon without enough SafeCoins is refused
    Given path 'api/v1/commerce/coupons/cpn-15/redeem'
    And header Authorization = 'Bearer ' + player.token
    And request {}
    When method post
    Then status 422
    And match response.code == 'BUSINESS_RULE_VIOLATION'

  Scenario: A coupon that does not exist cannot be redeemed
    Given path 'api/v1/commerce/coupons/cpn-ghost/redeem'
    And header Authorization = 'Bearer ' + player.token
    And request {}
    When method post
    Then status 404
