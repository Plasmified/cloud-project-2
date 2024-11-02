package utils;

import static java.lang.String.format;

import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;

import main.KeysRecord;
import tukano.api.Result;

public class Cosmos {

    private static final String CONNECTION_URL = KeysRecord.CONNECTION_URL;
    private static final String DB_KEY = KeysRecord.DB_KEY;
    
    private static Cosmos instance;

    private static Logger Log = Logger.getLogger(Cosmos.class.getName());

    private CosmosClient client;
    private CosmosDatabase db;
    private CosmosContainer container;
    
    private String dbName;
    private String containerName;

    private Cosmos(CosmosClient client, String dbName, String containerName) {
        this.client = client;
        this.dbName = dbName;
        this.containerName = containerName;
    }

    public static synchronized Cosmos getInstance(String dbName, String containerName) {
        if (instance != null) {
            if (!instance.dbName.equals(dbName) || !instance.containerName.equals(containerName)) {
                instance.close();
                instance = null;
            } else {
                return instance;
            }
        }

        CosmosClient client = new CosmosClientBuilder()
                .endpoint(CONNECTION_URL)
                .key(DB_KEY)
                .gatewayMode()    // use .directMode() for better performance
                .consistencyLevel(ConsistencyLevel.SESSION)
                .connectionSharingAcrossClientsEnabled(true)
                .contentResponseOnWriteEnabled(true)
                .buildClient();

        instance = new Cosmos(client, dbName, containerName);
        return instance;
    }
    
    private synchronized void init() {
        if (db != null) return;
        
        db = client.getDatabase(dbName);
        container = db.getContainer(containerName);
    }

    public void close() {
        client.close();
    }
    
    public <T> Result<T> getOne(String id, Class<T> clazz) {
        return tryCatch(() -> container.readItem(id, new PartitionKey(id), clazz).getItem());
    }
    
    public <T> Result<?> deleteOne(T obj) {
        return tryCatch(() -> container.deleteItem(obj, new CosmosItemRequestOptions()).getItem());
    }
    
    public <T> Result<T> updateOne(T obj) {
        return tryCatch(() -> container.upsertItem(obj).getItem());
    }
    
    public <T> Result<T> insertOne(T obj) {
        return tryCatch(() -> container.createItem(obj).getItem());
    }
    
    public <T> Result<List<T>> query(Class<T> clazz, String queryStr) {
        return tryCatch(() -> {
            var res = container.queryItems(queryStr, new CosmosQueryRequestOptions(), clazz);
            return res.stream().toList();
        });
    }
    
    <T> Result<T> tryCatch(Supplier<T> supplierFunc) {
        try {
            init();
            return Result.ok(supplierFunc.get());
        } catch (CosmosException ce) {
            Log.info(() -> format("COSMOS : %s\n", ce.getMessage()));
            return Result.error(errorCodeFromStatus(ce.getStatusCode()));
        } catch (Exception x) {
            Log.info(() -> format("COSMOS : %s\n", x.getMessage()));
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }
    
    static Result.ErrorCode errorCodeFromStatus(int status) {
        return switch (status) {
            case 200 -> Result.ErrorCode.OK;
            case 404 -> Result.ErrorCode.NOT_FOUND;
            case 409 -> Result.ErrorCode.CONFLICT;
            default -> Result.ErrorCode.INTERNAL_ERROR;
        };
    }
}
