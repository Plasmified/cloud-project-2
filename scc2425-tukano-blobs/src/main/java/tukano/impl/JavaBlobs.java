package tukano.impl;

import static java.lang.String.format;
import static tukano.api.Result.error;
import static tukano.api.Result.ErrorCode.FORBIDDEN;
// import static tukano.api.Result.ErrorCode.NOT_FOUND;

// import java.util.Base64;
import java.util.logging.Logger;

import tukano.api.Blobs;
import tukano.api.Result;
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
	// private BlobStorageImpl azureStorage;
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
	public Result<Void> upload(String blobId, byte[] bytes, String token) {
		Log.info(() -> format("upload : blobId = %s, sha256 = %s, token = %s\n", blobId, Hex.of(Hash.sha256(bytes)), token));

		if (!validBlobId(blobId, token))
			return error(FORBIDDEN);

		//azureStorage.UploadToBlobStorage(blobId, bytes);

		return storage.write( toPath( blobId ), bytes);

		// try {

		// 	//Tentar guardar na cache redis antes de guardar na blob storage
		// 	try (var jedis = RedisCache.getCachePool().getResource()) {
		// 		var key = "blob:" + blobId;
		// 		// Converter byte[] para String porque Redis nao suporta byte[]
		// 		String value = Base64.getEncoder().encodeToString(bytes);
		// 		jedis.set(key, value);
		// 	} catch(Exception e) {
		// 		e.printStackTrace();
		// 	}

		// 	azureStorage.UploadToBlobStorage(blobId, bytes);

		// 	return storage.write( toPath( blobId ), bytes);
		// } catch (Exception e) {
		// 	return error(INTERNAL_ERROR);
		// }

		
	}

	@Override
	public Result<byte[]> download(String blobId, String token) {
		Log.info(() -> format("download : blobId = %s, token=%s\n", blobId, token));

		if( ! validBlobId( blobId, token ) )
			return error(FORBIDDEN);

		return storage.read( toPath( blobId ) );
		
		// // Verificar isto, solucao temporaria para usar ao Storage.
		// if (azureStorage.DownloadFromBlobStorage(blobId) != res.value())
		// 	return error(FORBIDDEN);
		
		//Ver primeiro se o Blob está em cache
		// try( var jedis = RedisCache.getCachePool().getResource()) {
		// 	var key = "blob:" + blobId;
		// 	String value = jedis.get(key);
		// 	if( value != null ) {
		// 		// voltar a converter de string para byte[]
		// 		byte [] bytes = Base64.getDecoder().decode(value);
		// 		return Result.ok(bytes);
		// 	}
		// } catch(Exception e) {
		// 	e.printStackTrace();
		// }

		// Se não estiver no cache, ir buscar ao blob storage
		// byte[] res = azureStorage.DownloadFromBlobStorage(blobId);
		// if (res == null) {
		// 	return error(NOT_FOUND);
		// }

		// // Armazenar o blob recuperado (que não estava em cache) no cache redis
		// try (var jedis = RedisCache.getCachePool().getResource()) {
		// 	var key = "blob:" + blobId;
		// 	String value = Base64.getEncoder().encodeToString(res);
		// 	jedis.set(key, value);
		// } catch (Exception e) {
		// 	e.printStackTrace();
		// }

    	// return Result.ok(res);
	}

	@Override
	public Result<Void> delete(String blobId, String token) {
		Log.info(() -> format("delete : blobId = %s, token=%s\n", blobId, token));
	
		if( ! validBlobId( blobId, token ) )
			return error(FORBIDDEN);

		// // remover blob do cache redis
		// try (var jedis = RedisCache.getCachePool().getResource()) {
		// 	var key = "blob:" + blobId;
		// 	jedis.del(key);
		// } catch (Exception e) {
		// 	e.printStackTrace();
		// }

		return storage.delete( toPath(blobId));
	}
	
	@Override
	public Result<Void> deleteAllBlobs(String userId, String token) {
		Log.info(() -> format("deleteAllBlobs : userId = %s, token=%s\n", userId, token));

		if( ! Token.isValid( token, userId ) )
			return error(FORBIDDEN);
		
		//REVER IMPLEMENTAÇÃO DO DELETEALL BLOBS -> por causa do toPath() que recebe um blobID e estamos a manda rum UserID
		// try (var jedis = RedisCache.getCachePool().getResource()) {
		// 	// Tenho de, com a userId, conseguir ir buscar a lista de blobs e respetivas chaves
		// 	var keys = jedis.keys(??);
		// 	// Para cada blob encontrado, apaga da cache e do armazenamento
		// 	for (String key : keys) {
		// 		// Remove o blob da cache Redis
		// 		jedis.del(key);
	
		// 		// Extrai o blobId da chave e remove-o do armazenamento
		// 		String blobId = key.substring(("blob:" + userId + ":").length());
		// 		storage.delete(toPath(blobId));
		// 	}
		// } catch (Exception e) {
		// 	e.printStackTrace();
		// 	return error(INTERNAL_ERROR);
		// }

		return storage.delete(toPath(userId));
	}
	
	private boolean validBlobId(String blobId, String token) {		
		return Token.isValid(token, blobId);
	}

	private String toPath(String blobId) {
		return blobId.replace("+", "/");
	}
}
