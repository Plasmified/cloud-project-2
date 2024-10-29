package main;

public record KeysRecord() {

    //.../impl/cache/RedisCache.java
    public static final String REDIS_HOSTNAME = "copiar aqui o hostname da Redis";
    public static final String REDIS_KEYS = "copiar aqui a chave da Redis";

    //Cosmos.java
    public static final String CONNECTION_URL = "https://sc2425smd.documents.azure.com:443/"; // replace with your own
	public static final String DB_KEY = "s4kZfLOhVrkLPvIHXtPNgw8MShc7ttCdExTh4ga8WwYqxMoGLP7qWW1sKlQKwZk6hcliXrj3NS13ACDbjROLQA==";
	public static final String DB_NAME = "scc2425lab3";
	public static final String CONTAINER = "users";
}
