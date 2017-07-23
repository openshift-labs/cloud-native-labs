/*
 * Copyright 2016-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.openshift.booster;

import java.io.File;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import io.openshift.booster.test.OpenShiftTestAssistant;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.isEmptyString;

public class OpenShiftIT {

    private static OpenShiftTestAssistant assistant = new OpenShiftTestAssistant();

    @BeforeClass
    public static void prepare() throws Exception {
        // Deploy the database and wait until it's ready.
        assistant.deploy("database", new File("src/test/resources/templates/database.yml"));
        assistant.awaitPodReadinessOrFail(
                pod -> "my-database".equals(pod.getMetadata()
                        .getLabels()
                        .get("app"))
        );
        System.out.println("Database ready");

        assistant.deployApplication();
        assistant.awaitApplicationReadinessOrFail();

        await().atMost(5, TimeUnit.MINUTES)
                .until(() -> {
                    try {
                        Response response = get();
                        return response.getStatusCode() < 500;
                    } catch (Exception e) {
                        return false;
                    }
                });

        RestAssured.baseURI = RestAssured.baseURI + "/api/fruits";
    }

    @AfterClass
    public static void cleanup() {
        assistant.cleanup();
    }

    @Test
    public void testPostGetAndDelete() {
        Integer id = given()
                .contentType(ContentType.JSON)
                .body(Collections.singletonMap("name", "Lemon"))
                .when()
                .post()
                .then()
                .statusCode(201)
                .body("id", not(isEmptyString()))
                .body("name", is("Lemon"))
                .extract()
                .response()
                .path("id");

        when().get(id.toString())
                .then()
                .statusCode(200)
                .body("id", is(id))
                .body("name", is("Lemon"));

        when().delete(id.toString())
                .then()
                .statusCode(204);
    }

}
