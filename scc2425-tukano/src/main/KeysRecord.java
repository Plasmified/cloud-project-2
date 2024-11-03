package main;

public record KeysRecord() {

    //.../impl/cache/RedisCache.java
    public static final String REDIS_HOSTNAME = "scc2425cache57974.redis.cache.windows.net";
    public static final String REDIS_KEYS = "lAKhjRWfb0Nx8acBZEOBbRzjRG6w9FnX4AzCaDCtShw=";

    //Cosmos.java
    public static final String CONNECTION_URL = "https://scc-proj1.documents.azure.com:443/"; // replace with your own
	public static final String DB_KEY = "w3tOJjGl3InY65zrBqBtWmjAm5SUwuIMewkbdR2ViXhbWQ6AbmfcqRI2LUzVGnUVqwjzT7TPRKnLACDbqPKnfw==";
	public static final String DB_NAME = "users";
	public static final String CONTAINER = "user";

    public static final String USERSMODE = "COSMOS";
    public static final String SHORTSMODE = "COSMOS";

    public static final String SERVER_URL = "http://localhost:8080/tukano-1/rest";
}
