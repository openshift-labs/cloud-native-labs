/*
 * Copyright 2016-2017 Red Hat, Inc, and individual contributors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.openshift.boosters;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.arquillian.CreateSwarm;
import org.wildfly.swarm.arquillian.DefaultDeployment;

/**
 * @author Heiko Braun
 */
@RunWith(Arquillian.class)
@DefaultDeployment
public class FruitServiceTest {

    /**
     * For the unit tests we leverage in in-memory database
     */
    @CreateSwarm
    public static Swarm newContainer() throws Exception {
        return new Swarm().withProfile("local");
    }

    @Test
    @RunAsClient
    public void test_list_fruits() {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:8080")
                .path("/api")
                .path("/fruits");

        Response response = target.request(MediaType.APPLICATION_JSON).get();
        Assert.assertEquals(200, response.getStatus());
        JsonArray values = Json.parse(response.readEntity(String.class)).asArray();
        Assert.assertTrue(values.size() > 0);
    }

    @Test
    @RunAsClient
    public void test_fruit_by_id() {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:8080")
                .path("/api")
                .path("/fruits")
                .path("/1"); // fruit by ID

        Response response = target.request(MediaType.APPLICATION_JSON).get();
        Assert.assertEquals(200, response.getStatus());
        JsonObject value = Json.parse(response.readEntity(String.class)).asObject();
        Assert.assertTrue(value.get("name").asString().equals("Cherry"));
    }

    @Test
    @RunAsClient
    public void test_create_fruit() {
        createNewFruit("Pineapple");
    }

    private JsonObject createNewFruit(String name) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:8080")
                .path("/api")
                .path("/fruits");

        Response response = target.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(
                        new Fruit(name),
                        MediaType.APPLICATION_JSON)
                );
        Assert.assertEquals(201, response.getStatus());
        JsonObject value = Json.parse(response.readEntity(String.class)).asObject();
        Assert.assertTrue(value.get("name").asString().equals(name));
        return value;
    }


    @Test
    @RunAsClient
    public void test_modify_fruit() {

        JsonObject lemon = createNewFruit("Lemon");
        Integer id = lemon.get("id").asInt();

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:8080")
                .path("/api")
                .path("/fruits")
                .path(String.valueOf(id));

        Response response = target.request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(
                        new Fruit("Apricot"),
                        MediaType.APPLICATION_JSON)
                );
        Assert.assertEquals(200, response.getStatus());
        JsonObject value = Json.parse(response.readEntity(String.class)).asObject();
        Assert.assertTrue(value.get("name").asString().equals("Apricot"));
        Assert.assertTrue(value.get("id").asInt() == id);
    }

}

