package main;
import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import generated.PallierType;
import generated.ProductType;
import main.Services;

@Path("generic")
public class Webservice {
    Services services;

    public Webservice() {
        services = new Services();
    }

    @GET
    @Path("world")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getWorld(@Context HttpServletRequest request) {
        String username = request.getHeader("X-user");
        return Response.ok(services.getWorld(username)).build();
    }

    @PUT
    @Path("product")
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean putProduct(@Context HttpServletRequest request, ProductType product) {
        String username = request.getHeader("X-user");
        return services.updateProduct(username, product);
    }

    @PUT
    @Path("manager")
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean putManager(@Context HttpServletRequest request, PallierType manager) {
        String username = request.getHeader("X-user");
        return services.updateManager(username, manager);
    }
    
    @PUT
    @Path("upgrade")
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean putUpgrades(@Context HttpServletRequest request, PallierType upgrade) {
    	String username = request.getHeader("X-user");
    	return services.applyUpgrades(username, upgrade);
    }
    
    @DELETE
    @Path("world")
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean resetWorld(@Context HttpServletRequest request) {
    	String username = request.getHeader("X-user");
    	return services.applyReset(username);
    }
    
    @PUT
    @Path("angelupgrade")
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean putAngelUpgrade(@Context HttpServletRequest request, PallierType angelupgrade) {
    	String username = request.getHeader("X-user");
    	return services.applyAngelUpgrade(username, angelupgrade);
    }
    
    
    
}
