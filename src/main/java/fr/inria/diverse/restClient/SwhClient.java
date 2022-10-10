package fr.inria.diverse.restClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;


public interface SwhClient {

    @GET
    @Path("/resolve/{swhid}/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    ResolveDto resolve(@PathParam("swhid") String swhid);

    @GET
    @Path("/content/{id}/raw/")
    String getContent(@PathParam("id") String id);
}
