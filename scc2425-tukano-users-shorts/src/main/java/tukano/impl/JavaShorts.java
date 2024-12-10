package tukano.impl;

import static java.lang.String.format;
import static tukano.api.Result.error;
import static tukano.api.Result.errorOrResult;
import static tukano.api.Result.errorOrValue;
import static tukano.api.Result.errorOrVoid;
import static tukano.api.Result.ok;
import static tukano.api.Result.ErrorCode.BAD_REQUEST;
import static tukano.api.Result.ErrorCode.FORBIDDEN;
import static utils.DB.getOne;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.hibernate.Session;

import tukano.api.Result;
import tukano.api.Short;
import tukano.api.Shorts;
import tukano.api.User;
import tukano.clients.rest.RestBlobsClient;
import tukano.impl.data.Following;
import tukano.impl.data.Likes;
import utils.DB;
import utils.Hibernate;

public class JavaShorts implements Shorts {

	private static Logger Log = Logger.getLogger(JavaShorts.class.getName());
	private RestBlobsClient rbc;
	private String serverURI = "http://blobs-service:8080/tukano-blobs-1/rest";
	private static Shorts instance;
	
	synchronized public static Shorts getInstance() {
		if( instance == null )
			instance = new JavaShorts();
		return instance;
	}
	
	private JavaShorts() {
		rbc  = new RestBlobsClient(serverURI);
	}
	
	
	@Override
	public Result<Short> createShort(String userId, String password) {
		Log.info(() -> format("createShort : userId = %s, pwd = %s\n", userId, password));

		return errorOrResult( okUser(userId, password), user -> {
			
			var shortId = format("%s+%s", userId, UUID.randomUUID());
			var blobUrl = format("%s", shortId);
			var shrt = new Short(shortId, userId, blobUrl);

			return errorOrValue(DB.insertOne(shrt), s -> s.copyWithLikes_And_Token(0));
		});
	}

	@Override
	public Result<Short> getShort(String shortId) {
		Log.info(() -> format("getShort : shortId = %s\n", shortId));

		if( shortId == null )
			return error(BAD_REQUEST);

		var query = format("SELECT count(*) FROM Likes l WHERE l.shortId = '%s'", shortId);
		var likes = DB.sql(query, Long.class);
		return errorOrValue( getOne(shortId, Short.class), shrt -> shrt.copyWithLikes_And_Token( likes.get(0)));
	}

	
	@Override
	public Result<Void> deleteShort(String shortId, String password) {
		Log.info(() -> format("deleteShort : shortId = %s, pwd = %s\n", shortId, password));
		
		return errorOrResult( getShort(shortId), shrt -> {
			
			return errorOrResult( okUser( shrt.getOwnerId(), password), user -> {
				String sId = shrt.getBlobUrl().split("\\?token=")[0];
				var res = rbc.deleteAllBlobs(sId, Token.get(sId));
				Log.info(() -> format("DELETING BLOB WITH URL : %s", sId));
				Log.info(() -> format("DELETING BLOB : %s", res.isOK()));
				return DB.transaction( hibernate -> {

					hibernate.remove( shrt);
					
					var query = format("DELETE FROM Likes WHERE shortId = '%s'", shortId);
					hibernate.createNativeQuery(query, Likes.class).executeUpdate();
				});
			});	
		});
	}

	@Override
	public Result<List<String>> getShorts(String userId) {
		Log.info(() -> format("getShorts : userId = %s\n", userId));

		var query = format("SELECT s.shortId FROM Short s WHERE s.ownerId = '%s'", userId);
		return errorOrValue( okUser(userId), DB.sql( query, String.class));
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
		Log.info(() -> format("follow : userId1 = %s, userId2 = %s, isFollowing = %s, pwd = %s\n", userId1, userId2, isFollowing, password));
	
		
		return errorOrResult( okUser(userId1, password), user -> {
			var f = new Following(userId1, userId2);
			return errorOrVoid( okUser( userId2), isFollowing ? DB.insertOne( f ) : DB.deleteOne( f ));	
		});			
	}

	@Override
	public Result<List<String>> followers(String userId, String password) {
		Log.info(() -> format("followers : userId = %s, pwd = %s\n", userId, password));

		var query = format("SELECT f.follower FROM Following f WHERE f.followee = '%s'", userId);		
		return errorOrValue( okUser(userId, password), DB.sql(query, String.class));
	}

	@Override
	public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
		Log.info(() -> format("like : shortId = %s, userId = %s, isLiked = %s, pwd = %s\n", shortId, userId, isLiked, password));

		
		return errorOrResult( getShort(shortId), shrt -> {
			var l = new Likes(userId, shortId, shrt.getOwnerId());
			return errorOrVoid( okUser( userId, password), isLiked ? DB.insertOne( l ) : DB.deleteOne( l ));	
		});
	}

	@Override
	public Result<List<String>> likes(String shortId, String password) {
		Log.info(() -> format("likes : shortId = %s, pwd = %s\n", shortId, password));

		return errorOrResult( getShort(shortId), shrt -> {
			
			var query = format("SELECT l.userId FROM Likes l WHERE l.shortId = '%s'", shortId);					
			
			return errorOrValue( okUser( shrt.getOwnerId(), password ), DB.sql(query, String.class));
		});
	}

	@Override
	public Result<List<String>> getFeed(String userId, String password) {
		Log.info(() -> format("getFeed : userId = %s, pwd = %s\n", userId, password));

		final var QUERY_FMT = """
								SELECT shortId, timestamp 
								FROM (
									SELECT s.shortId, s.timestamp 
									FROM Short s 
									WHERE s.ownerId = '%s'
									
									UNION
									
									SELECT s.shortId, s.timestamp 
									FROM Short s 
									JOIN Following f ON f.followee = s.ownerId 
									WHERE f.follower = '%s'
								) AS unioned_shorts
								ORDER BY timestamp DESC
							  """;

		return errorOrValue( okUser( userId, password), DB.sql( format(QUERY_FMT, userId, userId), String.class));		
	}
		
	protected Result<User> okUser( String userId, String pwd) {
		return JavaUsers.getInstance().getUser(userId, pwd);
	}
	
	private Result<Void> okUser( String userId ) {
		var res = okUser( userId, "");
		if( res.error() == FORBIDDEN )
			return ok();
		else
			return error( res.error() );
	}
	
	@Override
	public Result<Void> deleteAllShorts(String userId, String password, String token) {
		Log.info(() -> format("deleteAllShorts : userId = %s, password = %s, token = %s\n", userId, password, token));
	
		// Check if the token is valid
		if (!Token.isValid(token, userId)) {
			return error(FORBIDDEN);
		}
	
		// Run the deletion in a background thread using Executors
		Executors.newSingleThreadExecutor().submit(() -> {
			try (Session session = Hibernate.getInstance().sessionFactory.openSession()) {
				// Start a transaction
				session.beginTransaction();
	
				// Delete shorts
				String query1 = "DELETE FROM Short WHERE ownerId = :userId";
				session.createQuery(query1) // Don't specify entity class here
					   .setParameter("userId", userId)
					   .executeUpdate();
	
				// Delete follows
				String query2 = "DELETE FROM Following WHERE follower = :userId OR followee = :userId";
				session.createQuery(query2) // Don't specify entity class here
					   .setParameter("userId", userId)
					   .executeUpdate();
	
				// Delete likes
				String query3 = "DELETE FROM Likes WHERE ownerId = :userId OR userId = :userId";
				session.createQuery(query3) // Don't specify entity class here
					   .setParameter("userId", userId)
					   .executeUpdate();
	
				// Commit the transaction
				session.getTransaction().commit();
			} catch (Exception e) {
				// Rollback the transaction if there was an exception
				e.printStackTrace();
				// Handle exception, possibly mark the operation as failed
			}
		});
	
		// Since this method is asynchronous, we immediately return success
		return ok(null);
	}
	

	
}