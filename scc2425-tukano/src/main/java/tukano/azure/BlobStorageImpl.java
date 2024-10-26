package tukano.azure;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;


public class BlobStorageImpl {
    private static final String BLOBS_CONTAINER_NAME = "shortsdata";
    private static final String STORAGE_CONNECTION_KEY = "DefaultEndpointsProtocol=https;AccountName=scc2425storage;AccountKey=SbvrU8OUKSKbV5rinvqK+Rkpo2v2xuPt3UnfKDWnhJ+5D+eC8JuRKOL9rza5p+T2Jfs+ql1MSll8+AStMvvtKw==;EndpointSuffix=core.windows.net";
    
    public void UploadToBlobStorage(String blobId, byte[] bytes) {
        try {
			BinaryData data = BinaryData.fromBytes(bytes);

			// Get container client
			BlobContainerClient containerClient = new BlobContainerClientBuilder()
														.connectionString(STORAGE_CONNECTION_KEY)
														.containerName(BLOBS_CONTAINER_NAME)
														.buildClient();

			BlobClient blob = containerClient.getBlobClient(blobId);

			blob.upload(data);
			
		} catch( Exception e) {
			e.printStackTrace();
		}
    }

    public byte[] DownloadFromBlobStorage(String blobId) {
        byte[] arr = null;

        try {
			// Get container client
			BlobContainerClient containerClient = new BlobContainerClientBuilder()
														.connectionString(STORAGE_CONNECTION_KEY)
														.containerName(BLOBS_CONTAINER_NAME)
														.buildClient();

			BlobClient blob = containerClient.getBlobClient(blobId);

			BinaryData data = blob.downloadContent();
			
			arr = data.toBytes();
		} catch( Exception e) {
			e.printStackTrace();
		}

        return arr;
    }
}
