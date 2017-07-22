package com.redhat.cloudnative.inventory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.wildfly.swarm.health.Health;
import org.wildfly.swarm.health.HealthStatus;
import java.util.Date;

@Path("/")
@ApplicationScoped
public class InventoryResource {
    @PersistenceContext(unitName = "MyPU")
    private EntityManager em;

    @GET
    @Path("/inventory/{itemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Inventory getAvailability(@PathParam("itemId") String itemId) {
        return em.find(Inventory.class, itemId);
    }

    @GET
    @Path("/health")
    @Health
    public HealthStatus health() {
        return HealthStatus
                .named("Inventory Health")
                .up()
                .withAttribute("date", new Date().toString());
    }
}
