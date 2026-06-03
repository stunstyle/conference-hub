package io.jprime.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
public class BadgeResourceTest {

    @Test
    public void testManualRegistrationFlow() {
        given()
          .redirects().follow(false)
          .contentType("application/x-www-form-urlencoded")
          .formParam("firstName", "Test")
          .formParam("lastName", "User")
          .formParam("email", "test.user@example.com")
          .formParam("company", "TestCorp")
        .when()
          .post("/badge/register")
        .then()
          .statusCode(303) // seeOther redirect
          .cookie("attendee_id");
    }
}
