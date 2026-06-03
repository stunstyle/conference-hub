package io.jprime.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
public class ScheduleResourceTest {

    @Test
    public void testSchedulePageLoads() {
        given()
          .when().get("/schedule")
          .then()
             .statusCode(200)
             .body(containsString("Conference Agenda"))
             .body(containsString("Hall A"))
             .body(containsString("Workshops"));
    }

    @Test
    public void testSurvivalGuidePageLoads() {
        given()
          .when().get("/info")
          .then()
             .statusCode(200)
             .body(containsString("Survival Guide"))
             .body(containsString("Sofia Tech Park"))
             .body(containsString("Parking Guide"));
    }
}
