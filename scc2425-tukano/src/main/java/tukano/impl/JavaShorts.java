package tukano.impl;

import static java.lang.String.format;
import static tukano.api.Result.error;
import static tukano.api.Result.errorOrResult;
import static tukano.api.Result.errorOrValue;
import static tukano.api.Result.errorOrVoid;
import static tukano.api.Result.ok;
import static tukano.api.Result.ErrorCode.FORBIDDEN;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Logger;

import main.KeysRecord;
import tukano.api.Blobs;
import tukano.api.Result;
import tukano.api.Short;
import tukano.api.Shorts;
import tukano.api.User;
import tukano.impl.data.Following;
import tukano.impl.data.Likes;
import tukano.impl.rest.TukanoRestServer;
import utils.DB;

public class JavaShorts implements Shorts {

	private static Logger Log = Logger.getLogger(JavaShorts.class.getName());
	
	private static Shorts instance;
	
	private DB dbAccess = new DB(KeysRecord.SHORTSMODE, "shorts", "short");

	synchronized public static Shorts getInstance() {
		if( instance == null )
			instance = new JavaShorts();
		return instance;
	}
	
	private JavaShorts() {}
	
	
	@Override
	public Result<Short> createShort(String uid, String password) {
		Log.info(() -> format("createShort : id = %s, pwd = %s\n", uid, password));

		return errorOrResult( okUser(uid, password), user -> {
			Log.info(() -> format("createShort : GOT HERE %s !\n", dbAccess.dbcontainer));
			
			var id = format("%s+%s", uid, UUID.randomUUID());
			var blobUrl = format("%s/%s/%s", TukanoRestServer.serverURI, Blobs.NAME, id); 
			var shrt = new Short(id, uid, blobUrl);

			return errorOrValue(dbAccess.insertOne(shrt), s -> s.copyWithLikes_And_Token(0));
		});
	}

	@Override
	public Result<Short> getShort(String shortId) {
		Log.info(() -> format("getShort : shortId = %s\n", shortId));

		var queryLikes = format("SELECT COUNT(1) FROM likes l WHERE l.shortId = '%s'", shortId);
		Result<List<Long>> likesResult = dbAccess.query(Long.class, queryLikes);
	
		Long likesCount = likesResult.isOK() && !likesResult.value().isEmpty() ? likesResult.value().get(0) : 0L;
	
		var queryShort = format("SELECT * FROM shorts WHERE shorts.id = '%s'", shortId);
		Result<List<Short>> shortResult = dbAccess.query(Short.class, queryShort);
	
		if (shortResult.isOK() && !shortResult.value().isEmpty()) {
			Short shrt = shortResult.value().get(0).copyWithLikes_And_Token(likesCount);
			return Result.ok(shrt);
		} else {
			return Result.error(shortResult.error());
		}
	}

	
	@Override
	public Result<Void> deleteShort(String shortId, String password) {
		Log.info(() -> format("deleteShort : shortId = %s, pwd = %s\n", shortId, password));
		
		if (dbAccess.mode.equals("COSMOS")) {
			
			return errorOrResult(getShort(shortId), shrt -> {
				return errorOrResult(okUser(shrt.getOwnerId(), password), user -> {
					var query = format("SELECT * FROM likes WHERE likes.id = '%s'", shortId);
					
					List<Likes> lst = dbAccess.sql(query, Likes.class);
					
					for (Likes lk : lst) {
						var res = dbAccess.deleteOne(lk);
						if (!res.isOK()) {
							return Result.<Void>error(res.error());
						}
					}
					
					return JavaBlobs.getInstance().delete(shrt.getBlobUrl(), Token.get());
				});
			});
		} else {
			return errorOrResult( getShort(shortId), shrt -> {
				
				return errorOrResult( okUser( shrt.getOwnerId(), password), user -> {
					return dbAccess.transaction( hibernate -> {

						hibernate.remove( shrt);
						
						var query = format("DELETE Likes l WHERE l.shortId = '%s'", shortId);
						hibernate.createNativeQuery( query, Likes.class).executeUpdate();
						
						JavaBlobs.getInstance().delete(shrt.getBlobUrl(), Token.get() );
					});
				});	
			});
		}
	}

	@Override
	public Result<List<String>> getShorts(String userId) {
		Log.info(() -> format("getShorts : userId = %s\n", userId));

		var query = format("SELECT * FROM shorts s WHERE s.ownerId = '%s'", userId);
		return errorOrValue( okUser(userId), dbAccess.sql( query, Short.class).stream().map(Short::getId).toList());
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
		Log.info(() -> format("follow : userId1 = %s, userId2 = %s, isFollowing = %s, pwd = %s\n", userId1, userId2, isFollowing, password));
	
		
		return errorOrResult( okUser(userId1, password), user -> {
			var f = new Following(userId1, userId2);
			return errorOrVoid( okUser( userId2), isFollowing ? dbAccess.insertOneSpecific( f, "follows", "follow" ) : dbAccess.deleteOneSpecific( f, "follows", "follow" ));	
		});			
	}

	@Override
	public Result<List<String>> followers(String userId, String password) {
		Log.info(() -> format("followers : userId = %s, pwd = %s\n", userId, password));

		var query = format("SELECT * FROM follows f WHERE f.id = '%s'", userId);	
		return errorOrValue( okUser(userId, password), dbAccess.sqlSpecific(query, "follows", "follow", Following.class).stream().map(Following::getFollower).toList());
	}

	@Override
	public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
		Log.info(() -> format("like : shortId = %s, userId = %s, isLiked = %s, pwd = %s\n", shortId, userId, isLiked, password));

		
		return errorOrResult( getShort(shortId), shrt -> {
			var l = new Likes(userId, shortId, shrt.getOwnerId());
			return errorOrVoid( okUser( userId, password), isLiked ? dbAccess.insertOneSpecific( l, "likes", "like" ) : dbAccess.deleteOneSpecific( l, "likes", "like" ));	
		});
	}

	@Override
	public Result<List<String>> likes(String shortId, String password) {
		Log.info(() -> format("likes : shortId = %s, pwd = %s\n", shortId, password));

		return errorOrResult( getShort(shortId), shrt -> {
			
			var query = format("SELECT * FROM likes l WHERE l.id = '%s'", shortId);
			
			return errorOrValue( okUser( shrt.getOwnerId(), password ), dbAccess.sqlSpecific(query, "likes", "like", Likes.class).stream().map(Likes::getUserId).toList());
		});
	}

	@Override
	public Result<List<String>> getFeed(String userId, String password) {
		Log.info(() -> format("getFeed : userId = %s, pwd = %s\n", userId, password));

		final var QUERY_FMT_SHORTS = "SELECT * FROM shorts s WHERE s.ownerId = '%s'";
		final var QUERY_FMT_FOLLOWING = "SELECT * FROM follows f WHERE f.follower = '%s'";

		var userShorts = dbAccess.sqlSpecific(format(QUERY_FMT_SHORTS, userId), "shorts", "short", Short.class).stream().toList();

		var followerIds = dbAccess.sqlSpecific(format(QUERY_FMT_FOLLOWING, userId), "follows", "follow", Following.class).stream()
								.map(Following::getId)
								.toList();

		List<Short> followerShorts = followerIds.stream().flatMap(followerId -> dbAccess.sqlSpecific(format(QUERY_FMT_SHORTS, followerId), "shorts", "short", Short.class).stream()).toList();

		Map<Long, String> combinedShorts = new TreeMap<>();

		for (Short s : userShorts) {
			combinedShorts.put(s.getTimestamp(), s.getId());
		}

		for (Short s : followerShorts) {
			combinedShorts.put(s.getTimestamp(), s.getId());
		}

		List<String> sortedShortIds = new ArrayList<>(combinedShorts.values());
		
		return errorOrValue(okUser(userId, password), sortedShortIds);
	}
		
	protected Result<User> okUser( String id, String pwd) {
		return JavaUsers.getInstance().getUser(id, pwd);
	}
	
	private Result<Void> okUser( String id ) {
		var res = okUser( id, "");
		if( res.error() == FORBIDDEN )
			return ok();
		else
			return error( res.error() );
	}
	
	@Override
	public Result<Void> deleteAllShorts(String userId, String password, String token) {
		Log.info(() -> format("deleteAllShorts : userId = %s, password = %s, token = %s\n", userId, password, token));

		if( ! Token.isValid( token, userId ) )
			return error(FORBIDDEN);
		
		if (dbAccess.mode.equals("COSMOS")) {

			String query1 = format("SELECT * FROM shorts s WHERE s.ownerId = '%s'", userId);
			List<Short> shortsResult = dbAccess.sqlSpecific(query1,"shorts","short",Short.class);
			
			for (Short s : shortsResult) {
				var deleteResult = dbAccess.deleteOne(s);
				if (!deleteResult.isOK()) {
					return Result.error(deleteResult.error());
				}
			}

			String query2 = format("SELECT * FROM follows f WHERE f.follower = '%s' OR f.id = '%s'", userId, userId);
			List<Following> followingResult = dbAccess.sqlSpecific(query2, "follows", "follow",  Following.class);

			for (Following f : followingResult) {
				var deleteResult = dbAccess.deleteOneSpecific(f, "follows", "follow");
				if (!deleteResult.isOK()) {
					return Result.error(deleteResult.error());
				}
			}
		
			String query3 = format("SELECT * FROM likes l WHERE l.ownerId = '%s' OR l.userId = '%s'", userId, userId);
			List<Likes> likesResult = dbAccess.sqlSpecific(query3, "likes", "like", Likes.class);
			for (Likes l : likesResult) {
				var deleteResult = dbAccess.deleteOneSpecific(l, "likes", "like");
				if (!deleteResult.isOK()) {
					return Result.error(deleteResult.error());
				}
			}
		
			return Result.ok();
		} else {
			return dbAccess.transaction( (hibernate) -> {
							
				//delete shorts
				var query1 = format("DELETE Short s WHERE s.ownerId = '%s'", userId);		
				hibernate.createQuery(query1, Short.class).executeUpdate();
				
				//delete follows
				var query2 = format("DELETE Following f WHERE f.follower = '%s' OR f.followee = '%s'", userId, userId);		
				hibernate.createQuery(query2, Following.class).executeUpdate();
				
				//delete likes
				var query3 = format("DELETE Likes l WHERE l.ownerId = '%s' OR l.userId = '%s'", userId, userId);		
				hibernate.createQuery(query3, Likes.class).executeUpdate();
				
			});
		}
	}
	
}