package tukano.auth;

import java.util.UUID;
import java.util.logging.Logger;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import tukano.auth.cookies.*;
import tukano.clients.rest.RestUsersClient;

@Path(Authentication.PATH)
public class Authentication {
	static final String PATH = "login";
	static final String USER = "username";
	static final String PWD = "password";
	public static final String COOKIE_KEY = "scc:session";
	private static final int MAX_COOKIE_AGE = 3600;
	private static Logger Log = Logger.getLogger(Authentication.class.getName());

	@POST
	public Response login( @QueryParam(USER) String user, @QueryParam(PWD) String password ) {
		RestUsersClient rUsersClient = new RestUsersClient("http://tukano-service:8081/tukano-users-shorts-1/rest");
		Log.info(String.format("USER LOGIN CREDENTIALS : %s & %s", user, password));
		var res = rUsersClient.getUser(user, password);
		Log.info(String.format("USER LOGIN RESULT : %s", res));
		boolean pwdOk = res.isOK();

		if (pwdOk) {
			String uid = UUID.randomUUID().toString();
			var cookie = new NewCookie.Builder(COOKIE_KEY)
					.value(uid).path("/")
					.comment("sessionid")
					.maxAge(MAX_COOKIE_AGE)
					.secure(false)
					.httpOnly(true)
					.build();
			
			FakeRedisLayer.getInstance().putSession( new Session( uid, user));	
			
            return Response.ok()
                    .cookie(cookie) 
                    .build();
		} else
			throw new NotAuthorizedException("Incorrect login");
	}
	
	static public Session validateSession(String userId) throws NotAuthorizedException {
		var cookies = RequestCookies.get();
		return validateSession( cookies.get(COOKIE_KEY ), userId );
	}
	
	static public Session validateSession(Cookie cookie, String userId) throws NotAuthorizedException {

		if (cookie == null )
			throw new NotAuthorizedException("No session initialized");
		
		var session = FakeRedisLayer.getInstance().getSession( cookie.getValue());
		if( session == null )
			throw new NotAuthorizedException("No valid session initialized");
			
		if (session.user() == null || session.user().length() == 0) 
			throw new NotAuthorizedException("No valid session initialized");
		
		if (!session.user().equals(userId))
			throw new NotAuthorizedException("Invalid user : " + session.user());
		
		return session;
	}
}
