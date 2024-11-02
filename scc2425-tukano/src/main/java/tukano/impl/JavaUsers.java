package tukano.impl;

import static java.lang.String.format;
import static tukano.api.Result.error;
import static tukano.api.Result.errorOrResult;
import static tukano.api.Result.errorOrValue;
import static tukano.api.Result.ok;
import static tukano.api.Result.ErrorCode.BAD_REQUEST;
import static tukano.api.Result.ErrorCode.FORBIDDEN;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import main.KeysRecord;
import tukano.api.Result;
import tukano.api.User;
import tukano.api.Users;
import utils.DB;

public class JavaUsers implements Users {
	
	private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

	private static Users instance;

	private DB dbAccess = new DB(KeysRecord.USERSMODE, "users", "user");
	
	synchronized public static Users getInstance() {
		if( instance == null )
			instance = new JavaUsers();
		return instance;
	}
	
	private JavaUsers() {}
	
	@Override
	public Result<String> createUser(User user) {
		
		Log.info(() -> format("createUser : %s\n", user));

		if( badUserInfo( user ) )
				return error(BAD_REQUEST);

		return errorOrValue( dbAccess.insertOne( user), user.getId() );
	}

	@Override
	public Result<User> getUser(String id, String pwd) {
		Log.info( () -> format("getUser : id = %s, pwd = %s\n", id, pwd));

		if (id == null)
			return error(BAD_REQUEST);
		
			Result<List<User>> res = dbAccess.query(User.class, String.format("SELECT * FROM users WHERE users.id=\"%s\"", id));
			
			User u = res.value().get(0);

			return validatedUserOrError(Result.ok(u), pwd);
	}

	@Override
	public Result<User> updateUser(String id, String pwd, User other) {
		Log.info(() -> format("updateUser : id = %s, pwd = %s, user: %s\n", id, pwd, other));

		if (badUpdateUserInfo(id, pwd, other))
			return error(BAD_REQUEST);

		Result<List<User>> res = dbAccess.query(User.class, String.format("SELECT * FROM users WHERE users.id=\"%s\"", id));
			
		User u = res.value().get(0);

		return errorOrResult( validatedUserOrError(Result.ok(u), pwd), user -> dbAccess.updateOne( user.updateFrom(other)));
	}

	@Override
	public Result<User> deleteUser(String id, String pwd) {
		Log.info(() -> format("deleteUser : id = %s, pwd = %s\n", id, pwd));

		if (id == null || pwd == null )
			return error(BAD_REQUEST);

		Result<List<User>> res = dbAccess.query(User.class, String.format("SELECT * FROM users WHERE users.id=\"%s\"", id));
			
		User u = res.value().get(0);

		return errorOrResult( validatedUserOrError(Result.ok(u), pwd), user -> {

			// Delete user shorts and related info asynchronously in a separate thread
			Executors.defaultThreadFactory().newThread( () -> {
				JavaShorts.getInstance().deleteAllShorts(id, pwd, Token.get(id));
				JavaBlobs.getInstance().deleteAllBlobs(id, Token.get(id));
			}).start();
			
			return dbAccess.deleteOne( user);
		});
	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {
		Log.info( () -> format("searchUsers : patterns = %s\n", pattern));

		var query = format("SELECT * FROM User u WHERE UPPER(u.id) LIKE '%%%s%%'", pattern.toUpperCase());
		var hits = dbAccess.sql(query, User.class);
				
		Log.info( () -> format("searchUsers : query = %s\n", query));

		Log.info( () -> format("searchUsers : hits = %s\n", hits));

		return ok(hits.stream().map(User::copyWithoutPassword).toList());
	}

	
	private Result<User> validatedUserOrError( Result<User> res, String pwd ) {
		if( res.isOK())
			return res.value().getPwd().equals( pwd ) ? res : error(FORBIDDEN);
		else
			return res;
	}
	
	private boolean badUserInfo( User user) {
		return (user.id() == null || user.pwd() == null || user.displayName() == null || user.email() == null);
	}
	
	private boolean badUpdateUserInfo( String id, String pwd, User info) {
		return (id == null || pwd == null || info.getId() != null && ! id.equals( info.getId()));
	}
}
