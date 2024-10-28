package resources;

import java.util.List;

import com.azure.core.annotation.PathParam;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import tukano.api.User;
import tukano.impl.rest.RestUsersResource;

@Path("/users")
public class UsersResources {

    private final String USER_ID = "userId";
    private RestUsersResource rus = new RestUsersResource();

    @POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public String createUser(String userId, String pwd, String email, String displayName) {
        User u = new User(userId, pwd, email, displayName);
        return rus.createUser(u);
    }

    @GET
	@Path("/{" + USER_ID+ "}")
    @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public User getUser(@PathParam(USER_ID) String userId, String pwd) {
        return rus.getUser(userId, pwd);
    }

    @PUT
	@Path("/{" + USER_ID+ "}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public User updateUser(@PathParam(USER_ID) String userId, String pwd, User user) {
        return rus.updateUser(userId, pwd, user);
    }

    @DELETE
	@Path("/{" + USER_ID+ "}")
    @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public User deleteUser(@PathParam(USER_ID) String userId, String pwd) {
        return rus.deleteUser(userId, pwd);
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public List<User> searchUsers(String pattern) {
        return rus.searchUsers(pattern);
    }


}
