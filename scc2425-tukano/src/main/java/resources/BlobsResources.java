package resources;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import tukano.impl.rest.RestBlobsResource;

@Path("/blobs")
public class BlobsResources {
    
    public final String BLOB_ID = "blobId";
	public final String TOKEN = "token";
    RestBlobsResource rbs = new RestBlobsResource();
    

    @POST
 	@Path("/{" + BLOB_ID +"}")
 	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public void uploadBlob(@PathParam(BLOB_ID) String blobId, byte[] bytes, @QueryParam(TOKEN) String token) {
        rbs.upload(blobId, bytes, token);
    }

    @GET
    @Path("/test")
    public String test() {
        return "Hello World!";
    }
}
