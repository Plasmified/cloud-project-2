package tukano.clients.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tukano.api.Blobs;
import tukano.api.Result;
import tukano.api.rest.RestBlobs;

public class RestBlobsClient extends RestClient implements Blobs {

	public RestBlobsClient(String serverURI) {
		super(serverURI, RestBlobs.PATH);
	}



	private Result<Void> _upload(String blobURL, String userId, byte[] bytes, String token, Cookie cookie) {
		return super.toJavaResult(
				client.target( blobURL )
				.queryParam(RestBlobs.USER_ID, "a5e1ac82a0c03e76ef08ca47d7ee0ec2")
				.queryParam(RestBlobs.TOKEN, token)
				.request()
				.cookie(cookie)
				.post( Entity.entity(bytes, MediaType.APPLICATION_OCTET_STREAM_TYPE)));
	}

	private Result<byte[]> _download(String blobURL, String userId, String token, Cookie cookie) {
		return super.toJavaResult(
				client.target( blobURL )
				.queryParam(RestBlobs.USER_ID, "a5e1ac82a0c03e76ef08ca47d7ee0ec2")
				.queryParam(RestBlobs.TOKEN, token)
				.request()
				.accept(MediaType.APPLICATION_OCTET_STREAM_TYPE)
				.cookie(cookie)
				.get(), byte[].class);
	}

	private Result<Void> _delete(String blobURL, String userId, String token, Cookie cookie) {
		return super.toJavaResult(
				client.target( blobURL )
				.queryParam(RestBlobs.USER_ID, "a5e1ac82a0c03e76ef08ca47d7ee0ec2")
				.queryParam( RestBlobs.TOKEN, token )
				.request()
				.cookie(cookie)
				.delete());
	}
	
	private Result<Void> _deleteAllBlobs(String userId, String token, Cookie cookie) {
		return super.toJavaResult(
				target.path(userId)
				.path(RestBlobs.BLOBS)
				.queryParam( RestBlobs.TOKEN, token )
				.request()
				.cookie(cookie)
				.delete());
	}

	public Response login(String username, String password) {
		return target.path("login")
					 .queryParam("username", username)
					 .queryParam("password", password)
					 .request()
					 .post(null);
	}
	
	@Override
	public Result<Void> upload(String blobId, String userId, byte[] bytes, String token, Cookie cookie) {
		return super.reTry( () -> _upload(blobId, userId, bytes, token, cookie));
	}

	@Override
	public Result<byte[]> download(String blobId, String userId, String token, Cookie cookie) {
		return super.reTry( () -> _download(blobId, userId, token, cookie));
	}

	@Override
	public Result<Void> delete(String blobId, String userId, String token, Cookie cookie) {
		return super.reTry( () -> _delete(blobId, userId, token, cookie));
	}
	
	@Override
	public Result<Void> deleteAllBlobs(String userId, String password, Cookie cookie) {
		return super.reTry( () -> _deleteAllBlobs(userId, password, cookie));
	}
}
