package tukano.impl;

import static java.lang.String.format;
import static tukano.api.Result.error;
import static tukano.api.Result.ErrorCode.FORBIDDEN;
import static tukano.api.Result.ErrorCode.INTERNAL_ERROR;
import static tukano.api.Result.ErrorCode.NOT_FOUND;

import java.util.Base64;
import java.util.logging.Logger;

import main.KeysRecord;
import tukano.api.Blobs;
import tukano.api.Result;
import tukano.api.Result.ErrorCode;
import tukano.azure.BlobStorageImpl;
import tukano.impl.cache.RedisCache;
import tukano.impl.storage.BlobStorage;
import tukano.impl.storage.FilesystemStorage;
import utils.Hash;
import utils.Hex;

public class JavaBlobs implements Blobs {
	
	private static Blobs instance;
	private static Logger Log = Logger.getLogger(JavaBlobs.class.getName());

	public String baseURI;
	private BlobStorageImpl azureStorage = new BlobStorageImpl();
	private BlobStorage storage;
	
	synchronized public static Blobs getInstance() {
		if( instance == null )
			instance = new JavaBlobs();
		return instance;
	}
	
	private JavaBlobs() {
		storage = new FilesystemStorage();
		baseURI = String.format("%s/%s/", KeysRecord.SERVER_URL, Blobs.NAME);
	}
	
	@Override
	public Result<Void> upload(String blobId, byte[] bytes, String token) {
		Log.info(() -> format("upload : blobId = %s, sha256 = %s, token = %s\n", blobId, Hex.of(Hash.sha256(bytes)), token));

		if (!validBlobId(blobId, token))
			return error(FORBIDDEN);

		try {

			//Tentar guardar na cache redis antes de guardar na blob storage
			try (var jedis = RedisCache.getCachePool().getResource()) {
				var key = "blob:" + blobId;
				// Converter byte[] para String porque Redis nao suporta byte[]
				String value = Base64.getEncoder().encodeToString(bytes);
				jedis.set(key, value);
			} catch(Exception e) {
				e.printStackTrace();
			}

			azureStorage.UploadToBlobStorage(blobId, bytes);

			return storage.write( toPath( blobId ), bytes);
		} catch (Exception e) {
			Log.info(() -> e.getMessage());
			return error(INTERNAL_ERROR);
		}

		
	}

	@Override
	public Result<byte[]> download(String blobId, String token) {
		Log.info(() -> format("download : blobId = %s, token=%s\n", blobId, token));

		if( ! validBlobId( blobId, token ) )
			return error(FORBIDDEN);
		
		//Ver primeiro se o Blob está em cache
		try( var jedis = RedisCache.getCachePool().getResource()) {
			var key = "blob:" + blobId;
			String value = jedis.get(key);
			if( value != null ) {
				// voltar a converter de string para byte[]
				byte [] bytes = Base64.getDecoder().decode(value);
				return Result.ok(bytes);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

		// Se não estiver no cache, ir buscar ao blob storage
		byte[] res = azureStorage.DownloadFromBlobStorage(blobId);
		if (res == null) {
			return error(NOT_FOUND);
		}

		// Armazenar o blob recuperado (que não estava em cache) no cache redis
		try (var jedis = RedisCache.getCachePool().getResource()) {
			var key = "blob:" + blobId;
			String value = Base64.getEncoder().encodeToString(res);
			jedis.set(key, value);
		} catch (Exception e) {
			e.printStackTrace();
		}

    	return Result.ok(res);
	}

	@Override
	public Result<Void> delete(String blobId, String token) {
		Log.info(() -> format("delete : blobId = %s, token=%s\n", blobId, token));
	
		if( ! validBlobId( blobId, token ) )
			return error(FORBIDDEN);

		// remover blob do cache redis
		try (var jedis = RedisCache.getCachePool().getResource()) {
			var key = "blob:" + blobId;
			jedis.del(key);
		} catch (Exception e) {
			e.printStackTrace();
		}

		azureStorage.DeleteFromBlobStorage(blobId);

		return Result.ok();
	}
	
	@Override
	public Result<Void> deleteAllBlobs(String userId, String token) {
		Log.info(() -> format("deleteAllBlobs : userId = %s, token=%s\n", userId, token));

		if( ! Token.isValid( token, userId ) )
			return error(FORBIDDEN);
		
		try (var jedis = RedisCache.getCachePool().getResource()) {
			var keys = jedis.keys(userId);
			for (String key : keys) {
				jedis.del(key);
	
				Log.info( () -> "Chave : " + key + "\n");

				try {
					azureStorage.DeleteFromBlobStorage(key);
				} catch (Exception e) {
					Log.info( () -> e.getMessage());
					return Result.error(ErrorCode.INTERNAL_ERROR);
				}
			}
		} catch (Exception e) {
			Log.info( () -> e.getMessage());
			e.printStackTrace();
			return error(INTERNAL_ERROR);
		}

		return Result.ok();
	}
	
	private boolean validBlobId(String blobId, String token) {		
		return Token.isValid(token, blobId);
	}

	private String toPath(String blobId) {
		return blobId.replace("+", "/");
	}
}
