package io.jprime.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
public class DirectoryResourceTest {

    @Test
    public void testDirectoryPageLoads() {
        given()
          .when().get("/directory")
          .then()
             .statusCode(200)
             .body(containsString("Attendee Directory"))
             .body(containsString("Connect and Network"));
    }
}
