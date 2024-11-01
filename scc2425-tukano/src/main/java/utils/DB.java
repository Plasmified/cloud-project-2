package utils;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.hibernate.Session;

import tukano.api.Result;

public class DB {
	
	public String mode;
	public String dbname;
	public String dbcontainer;

	public DB (String mode, String dbname, String dbcontainer) {
		this.mode = mode;
		this.dbname = dbname;
		this.dbcontainer = dbcontainer;
	}

	public <T> List<T> sql(String query, Class<T> clazz) {
		if (mode.equals("COSMOS")) {
			return Cosmos.getInstance(dbname, dbcontainer).query(clazz, query).value();
		} else {
			return Hibernate.getInstance().sql(query, clazz);
		}
	}
	
	public <T> List<T> sql(Class<T> clazz, String fmt, Object ... args) {
		if (mode.equals("COSMOS")) {
			return Cosmos.getInstance(dbname, dbcontainer).query(clazz, String.format(fmt, args)).value();
		} else {
			return Hibernate.getInstance().sql(String.format(fmt, args), clazz);
		}
	}
	
	public <T> Result<T> getOne(String id, Class<T> clazz) {
		if (mode.equals("COSMOS")) {
			return Cosmos.getInstance(dbname, dbcontainer).getOne(id, clazz);
		} else {
			return Hibernate.getInstance().getOne(id, clazz);
		}
	}
	
	// Pode dar problemas, verificar mais tarde.
	@SuppressWarnings("unchecked")
	public <T> Result<T> deleteOne(T obj) {
		if (mode.equals("COSMOS")) {
			return (Result<T>) Cosmos.getInstance(dbname, dbcontainer).deleteOne(obj);
		} else {
			return Hibernate.getInstance().deleteOne(obj);
		}
	}
	
	public <T> Result<T> updateOne(T obj) {
		if (mode.equals("COSMOS")) {
			return Cosmos.getInstance(dbname, dbcontainer).updateOne(obj);
		} else {
			return Hibernate.getInstance().updateOne(obj);
		}
	}

	public <T> Result<T> insertOne( T obj) {
		if (mode.equals("COSMOS")) {
			return Cosmos.getInstance(dbname, dbcontainer).insertOne(obj);
		} else {
			return Result.errorOrValue(Hibernate.getInstance().persistOne(obj), obj);
		}
	}

	public <T> Result<List<T>> query(Class<T> clazz, String queryStr) {
		return Cosmos.getInstance(dbname, dbcontainer).query(clazz, queryStr);
	}

	public <T> Result<List<T>> cosmosTransaction(String query, Class<T> clazz) {
		return Cosmos.getInstance(dbname, dbcontainer).query(clazz, query);
	}
	
	public <T> Result<T> transaction( Consumer<Session> c) {
		return Hibernate.getInstance().execute( c::accept );
	}
	
	public <T> Result<T> transaction( Function<Session, Result<T>> func) {
		return Hibernate.getInstance().execute( func );
	}
}
