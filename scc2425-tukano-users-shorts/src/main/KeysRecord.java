package main;

public record KeysRecord() {

    //.../impl/cache/RedisCache.java
    public static final String REDIS_HOSTNAME = "copiar aqui o hostname da Redis";
    public static final String REDIS_KEYS = "copiar aqui a chave da Redis";

    //Cosmos.java
    public static final String CONNECTION_URL = "https://scc-proj1.documents.azure.com:443/"; // replace with your own
	public static final String DB_KEY = "w3tOJjGl3InY65zrBqBtWmjAm5SUwuIMewkbdR2ViXhbWQ6AbmfcqRI2LUzVGnUVqwjzT7TPRKnLACDbqPKnfw==";
	public static final String DB_NAME = "users";
	public static final String CONTAINER = "user";
}
