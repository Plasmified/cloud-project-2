package tukano.impl;

import static java.lang.String.format;
import static tukano.api.Result.error;
import static tukano.api.Result.ErrorCode.FORBIDDEN;
// import static tukano.api.Result.ErrorCode.NOT_FOUND;

// import java.util.Base64;
import java.util.logging.Logger;

import jakarta.ws.rs.core.Cookie;
import tukano.api.Blobs;
import tukano.api.Result;
import tukano.auth.Authentication;
// import tukano.azure.BlobStorageImpl;
// import tukano.impl.cache.RedisCache;
import tukano.impl.rest.TukanoRestServer;
import tukano.impl.storage.BlobStorage;
import tukano.impl.storage.FilesystemStorage;
import utils.Hash;
import utils.Hex;

public class JavaBlobs implements Blobs {
	
	private static Blobs instance;
	private static Logger Log = Logger.getLogger(JavaBlobs.class.getName());

	public String baseURI;
	private BlobStorage storage;
	
	synchronized public static Blobs getInstance() {
		if( instance == null )
			instance = new JavaBlobs();
		return instance;
	}
	
	private JavaBlobs() {
		storage = new FilesystemStorage();
		baseURI = String.format("%s/%s/", TukanoRestServer.serverURI, Blobs.NAME);
	}
	
	@Override
	public Result<Void> upload(String blobId, String userId, byte[] bytes, String token, Cookie cookie) {
		Log.info(() -> format("upload : blobId = %s, sha256 = %s, token = %s\n", blobId, Hex.of(Hash.sha256(bytes)), token));

		try {
			Authentication.validateSession(cookie, userId);
		} catch (Exception e) {
			return error(FORBIDDEN);
		}

		if (!validBlobId(blobId, token))
			return error(FORBIDDEN);

		return storage.write( toPath( blobId ), bytes);
		
	}

	@Override
	public Result<byte[]> download(String blobId, String userId, String token, Cookie cookie) {
		Log.info(() -> format("download : blobId = %s, token=%s\n", blobId, token));

		try {
			Authentication.validateSession(cookie, userId);
		} catch (Exception e) {
			return error(FORBIDDEN);
		}

		if( ! validBlobId( blobId, token ) )
			return error(FORBIDDEN);

		return storage.read( toPath( blobId ) );
	}

	@Override
	public Result<Void> delete(String blobId, String userId, String token, Cookie cookie) {
		Log.info(() -> format("delete : blobId = %s, token=%s\n", blobId, token));
	
		try {
			Authentication.validateSession(cookie, userId);
		} catch (Exception e) {
			Log.info(e.getMessage());
			return error(FORBIDDEN);
		}

		if( ! validBlobId( blobId, token ) )
			return error(FORBIDDEN);

		return storage.delete( toPath(blobId));
	}
	
	@Override
	public Result<Void> deleteAllBlobs(String userId, String token, Cookie cookie) {
		Log.info(() -> format("deleteAllBlobs : userId = %s, token=%s\n", userId.split("\\+")[0], token));

		try {
			Authentication.validateSession(cookie, userId.split("\\+")[0]);
		} catch (Exception e) {
			Log.info(e.getMessage());
			return error(FORBIDDEN);
		}

		if( ! Token.isValid( token, userId ) )
			return error(FORBIDDEN);

		return storage.delete(toPath(userId));
	}
	
	private boolean validBlobId(String blobId, String token) {		
		return Token.isValid(token, blobId);
	}

	private String toPath(String blobId) {
		return blobId.replace("+", "/");
	}
}
