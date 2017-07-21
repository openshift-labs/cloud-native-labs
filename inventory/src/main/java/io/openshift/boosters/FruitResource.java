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

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * @author Heiko Braun
 * @since 17/01/2017
 */
@Path("/fruits")
@ApplicationScoped
public class FruitResource {

    @PersistenceContext(unitName = "MyPU")
    private EntityManager em;

    @GET
    @Produces("application/json")
    public Fruit[] get() {
        return em
                .createNamedQuery("Fruits.findAll", Fruit.class)
                .getResultList()
                .toArray(new Fruit[0]);
    }

    @GET
    @Path("/{id}")
    @Produces("application/json")
    public Fruit getSingle(@PathParam("id") Integer id) {
        return em.find(Fruit.class, id);
    }


    @POST
    @Consumes("application/json")
    @Produces("application/json")
    @Transactional
    public Response create(Fruit fruit) {
        if (fruit.getId() != null) {
            return error(422, "Id was invalidly set on request.");
        }

        try {
            em.persist(fruit);
        } catch (Exception e) {
            return error(500, e.getMessage());
        }
        return Response.ok(fruit).status(201).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes("application/json")
    @Produces("application/json")
    @Transactional
    public Response update(@PathParam("id") Integer id, Fruit fruit) {
        if (fruit.getName() == null) {
            return error(422, "Fruit Name was not set on request.");
        }

        try {
            Fruit entity = em.find(Fruit.class, id);

            if (entity == null) {
                return error(404, "Fruit with id of " + id + " does not exist.");
            }

            entity.setName(fruit.getName());
            em.merge(entity);

            return Response.ok(entity).status(200).build();
        } catch (Exception e) {
            return error(500, e.getMessage());
        }
    }


    @DELETE
    @Path("/{id}")
    @Consumes("text/plain")
    @Transactional
    public Response delete(@PathParam("id") Integer id) {
        try {
            Fruit entity = em.find(Fruit.class, id);
            em.remove(entity);
        } catch (Exception e) {
            return error(500, e.getMessage());
        }
        return Response.status(204).build();
    }

    private Response error(int code, String message) {
        return Response
                .status(code)
                .entity(Json.createObjectBuilder()
                                .add("error", message)
                                .add("code", code)
                                .build()
                )
                .build();
    }
}
