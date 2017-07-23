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

import java.util.Collections;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import io.openshift.booster.service.Fruit;
import io.openshift.booster.service.FruitRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertFalse;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BoosterApplicationTest {

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private FruitRepository fruitRepository;

    @Before
    public void beforeTest() {
        fruitRepository.deleteAll();
        RestAssured.baseURI = String.format("http://localhost:%d/api/fruits", port);
    }

    @Test
    public void testGetAll() {
        Fruit cherry = fruitRepository.save(new Fruit("Cherry"));
        Fruit apple = fruitRepository.save(new Fruit("Apple"));
        when().get()
                .then()
                .statusCode(200)
                .body("id", hasItems(cherry.getId(), apple.getId()))
                .body("name", hasItems(cherry.getName(), apple.getName()));
    }

    @Test
    public void testGetEmptyArray() {
        when().get()
                .then()
                .statusCode(200)
                .body(is("[]"));
    }

    @Test
    public void testGetOne() {
        Fruit cherry = fruitRepository.save(new Fruit("Cherry"));
        when().get(String.valueOf(cherry.getId()))
                .then()
                .statusCode(200)
                .body("id", is(cherry.getId()))
                .body("name", is(cherry.getName()));
    }

    @Test
    public void testGetNotExisting() {
        when().get("0")
                .then()
                .statusCode(404);
    }

    @Test
    public void testPost() {
        given().contentType(ContentType.JSON)
                .body(Collections.singletonMap("name", "Cherry"))
                .when()
                .post()
                .then()
                .statusCode(201)
                .body("id", not(isEmptyString()))
                .body("name", is("Cherry"));
    }

    @Test
    public void testPostWithWrongPayload() {
        given().contentType(ContentType.JSON)
                .body(Collections.singletonMap("id", 0))
                .when()
                .post()
                .then()
                .statusCode(422);
    }

    @Test
    public void testPostWithNonJsonPayload() {
        given().contentType(ContentType.XML)
                .when()
                .post()
                .then()
                .statusCode(415);
    }

    @Test
    public void testPostWithEmptyPayload() {
        given().contentType(ContentType.JSON)
                .when()
                .post()
                .then()
                .statusCode(415);
    }

    @Test
    public void testPut() {
        Fruit cherry = fruitRepository.save(new Fruit("Cherry"));
        given().contentType(ContentType.JSON)
                .body(Collections.singletonMap("name", "Lemon"))
                .when()
                .put(String.valueOf(cherry.getId()))
                .then()
                .statusCode(200)
                .body("id", is(cherry.getId()))
                .body("name", is("Lemon"));

    }

    @Test
    public void testPutNotExisting() {
        given().contentType(ContentType.JSON)
                .body(Collections.singletonMap("name", "Lemon"))
                .when()
                .put("/0")
                .then()
                .statusCode(404);
    }

    @Test
    public void testPutWithWrongPayload() {
        Fruit cherry = fruitRepository.save(new Fruit("Cherry"));
        given().contentType(ContentType.JSON)
                .body(Collections.singletonMap("id", 0))
                .when()
                .put(String.valueOf(cherry.getId()))
                .then()
                .statusCode(422);
    }

    @Test
    public void testPutWithNonJsonPayload() {
        Fruit cherry = fruitRepository.save(new Fruit("Cherry"));
        given().contentType(ContentType.XML)
                .when()
                .put(String.valueOf(cherry.getId()))
                .then()
                .statusCode(415);
    }

    @Test
    public void testPutWithEmptyPayload() {
        Fruit cherry = fruitRepository.save(new Fruit("Cherry"));
        given().contentType(ContentType.JSON)
                .when()
                .put(String.valueOf(cherry.getId()))
                .then()
                .statusCode(415);
    }

    @Test
    public void testDelete() {
        Fruit cherry = fruitRepository.save(new Fruit("Cherry"));
        when().delete(String.valueOf(cherry.getId()))
                .then()
                .statusCode(204);
        assertFalse(fruitRepository.exists(cherry.getId()));
    }

    @Test
    public void testDeleteNotExisting() {
        when().delete("/0")
                .then()
                .statusCode(404);
    }

}
