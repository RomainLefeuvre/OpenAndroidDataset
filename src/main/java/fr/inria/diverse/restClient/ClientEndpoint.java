package fr.inria.diverse.restClient;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

public class ClientEndpoint {
    private static SwhClient swhClient;

    public static SwhClient swhClient() {
        if (swhClient == null) {
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target("https://archive.softwareheritage.org/api/1/");
            ResteasyWebTarget rtarget = (ResteasyWebTarget) target;

            ClientEndpoint.swhClient = rtarget.proxy(SwhClient.class);
        }
        return swhClient;
    }

}
