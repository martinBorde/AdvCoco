package main;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

@Component
@ApplicationPath("/adventurecommunist")
public class JerseyConfig extends ResourceConfig {
    
    public JerseyConfig() {
        register(Webservice.class);
        register(CORSResponseFilter.class);
    }
}

