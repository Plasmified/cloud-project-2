package tukano.azure;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BlobStorageImpl {
    private static final String LOCAL_STORAGE_PATH = "/data/";

    public void UploadToBlobStorage(String blobId, byte[] bytes) {
        try {
            // Save the file to the local persistent volume
            File file = new File(LOCAL_STORAGE_PATH + blobId);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(bytes);
            }
            System.out.println("File uploaded to local storage: " + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] DownloadFromBlobStorage(String blobId) {
        try {
            // Read the file from the local persistent volume
            String filePath = LOCAL_STORAGE_PATH + blobId;
            return Files.readAllBytes(Paths.get(filePath));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

