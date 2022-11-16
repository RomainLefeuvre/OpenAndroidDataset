package fr.inria.diverse.restClient;

import fr.inria.diverse.model.ResolveDto;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

public interface SwhClient {

    @GET
    @Path("/resolve/{swhid}/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    ResolveDto resolve(@HeaderParam("Authorization") String authorization, @PathParam("swhid") String swhid);

    @GET
    @Path("/content/{id}/raw/")
    String getContent(@HeaderParam("Authorization") String authorization, @PathParam("id") String id);
}
