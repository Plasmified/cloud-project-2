package tukano.api.rest;


import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import tukano.auth.Authentication;

@Path(RestBlobs.PATH)
public interface RestBlobs {
	
	String PATH = "/blobs";
	String BLOB_ID = "blobId";
	String TOKEN = "token";
	String BLOBS = "blobs";
	String USER_ID = "userId";

 	@POST
 	@Path("/{" + BLOB_ID +"}")
 	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	void upload(@PathParam(BLOB_ID) String blobId, @QueryParam(USER_ID) String userId, byte[] bytes, @QueryParam(TOKEN) String token, @CookieParam(Authentication.COOKIE_KEY) Cookie cookie);


 	@GET
 	@Path("/{" + BLOB_ID +"}") 	
 	@Produces(MediaType.APPLICATION_OCTET_STREAM)
 	byte[] download(@PathParam(BLOB_ID) String blobId, @QueryParam(USER_ID) String userId, @QueryParam(TOKEN) String token, @CookieParam(Authentication.COOKIE_KEY) Cookie cookie);
 	
 	
	@DELETE
	@Path("/{" + BLOB_ID + "}")
	void delete(@PathParam(BLOB_ID) String blobId, @QueryParam(USER_ID) String userId, @QueryParam(TOKEN) String token, @CookieParam(Authentication.COOKIE_KEY) Cookie cookie);		

	@DELETE
	@Path("/{" + USER_ID + "}/" + BLOBS)
	void deleteAllBlobs(@PathParam(USER_ID) String userId, @QueryParam(TOKEN) String token, @CookieParam(Authentication.COOKIE_KEY) Cookie cookie);		
}
