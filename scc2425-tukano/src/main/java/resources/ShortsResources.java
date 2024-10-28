package resources;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import tukano.api.Short;

public class ShortsResources {

    private static final String SHORTS = "/shorts";
    private static final String FEED = "/feed";
    private static final String LIKES = "/likes";
	private static final String FOLLOWERS = "/followers";
    private static final String USER_ID = "userId";
    private static final String SHORT_ID = "shortId";
    private static final String USER_ID1 = "userId1";
	private static final String USER_ID2 = "userId2";
    private static final String PWD = "pwd";
    private static final String TOKEN = "token";

    @POST
	@Path("/{" + USER_ID + "}")
	@Produces(MediaType.APPLICATION_JSON)
    public Short createShort(@PathParam(USER_ID) String userId, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createShort'");
    }

    
    @DELETE
	@Path("/{" + SHORT_ID + "}")
    public void deleteShort(@PathParam(SHORT_ID) String shortId, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteShort'");
    }

    
    @GET
	@Path("/{" + SHORT_ID + "}" )
	@Produces(MediaType.APPLICATION_JSON)
    public Short getShort(@PathParam(SHORT_ID) String shortId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getShort'");
    }

    
    @GET
	@Path("/{" + USER_ID + "}" + SHORTS )
	@Produces(MediaType.APPLICATION_JSON)
    public List<String> getShorts(@PathParam(USER_ID) String userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getShorts'");
    }

    
    @POST
	@Path("/{" + USER_ID1 + "}/{" + USER_ID2 + "}" + FOLLOWERS )
	@Consumes(MediaType.APPLICATION_JSON)
    public void follow(@PathParam(USER_ID1) String userId1, @PathParam(USER_ID2) String userId2, boolean isFollowing, @QueryParam(PWD) String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'follow'");
    }

    
    @GET
	@Path("/{" + USER_ID + "}" + FOLLOWERS )
	@Produces(MediaType.APPLICATION_JSON)
    public List<String> followers(@PathParam(USER_ID) String userId, @QueryParam(PWD) String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'followers'");
    }

    
    @POST
	@Path("/{" + SHORT_ID + "}/{" + USER_ID + "}" + LIKES )
	@Consumes(MediaType.APPLICATION_JSON)
    public void like(@PathParam(SHORT_ID) String shortId, @PathParam(USER_ID) String userId, boolean isLiked, @QueryParam(PWD) String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'like'");
    }

    
    @GET
	@Path("/{" + SHORT_ID + "}" + LIKES )
	@Produces(MediaType.APPLICATION_JSON)
    public List<String> likes(@PathParam(SHORT_ID) String shortId, @QueryParam(PWD) String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'likes'");
    }

    
    @GET
	@Path("/{" + USER_ID + "}" + FEED )
	@Produces(MediaType.APPLICATION_JSON)
    public List<String> getFeed(@PathParam(USER_ID) String userId, @QueryParam(PWD) String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFeed'");
    }

    
    @DELETE
	@Path("/{" + USER_ID + "}" + SHORTS)
    public void deleteAllShorts(@PathParam(USER_ID) String userId, @QueryParam(PWD) String password, @QueryParam(TOKEN) String token) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAllShorts'");
    }
    
}
