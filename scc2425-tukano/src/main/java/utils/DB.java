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
	private Cosmos cosmos;

	public DB (String mode, String dbname, String dbcontainer) {
		this.mode = mode;
		this.dbname = dbname;
		this.dbcontainer = dbcontainer;
		this.cosmos = Cosmos.getInstance(dbname, dbcontainer);
	}

	public <T> List<T> sql(String query, Class<T> clazz) {
		if (mode.equals("COSMOS")) {
			return cosmos.query(clazz, query).value();
		} else {
			return Hibernate.getInstance().sql(query, clazz);
		}
	}

	public <T> List<T> sqlSpecific(String query, String name, String cont, Class<T> clazz) {
		if (mode.equals("COSMOS")) {
			return Cosmos.getInstance(name, cont).query(clazz, query).value();
		} else {
			return Hibernate.getInstance().sql(query, clazz);
		}
	}
	
	public <T> List<T> sql(Class<T> clazz, String fmt, Object ... args) {
		if (mode.equals("COSMOS")) {
			return cosmos.query(clazz, String.format(fmt, args)).value();
		} else {
			return Hibernate.getInstance().sql(String.format(fmt, args), clazz);
		}
	}
	
	public <T> Result<T> getOne(String id, Class<T> clazz) {
		if (mode.equals("COSMOS")) {
			return cosmos.getOne(id, clazz);
		} else {
			return Hibernate.getInstance().getOne(id, clazz);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> Result<T> deleteOne(T obj) {
		if (mode.equals("COSMOS")) {
			return (Result<T>) cosmos.deleteOne(obj);
		} else {
			return Hibernate.getInstance().deleteOne(obj);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> Result<T> deleteOneSpecific(T obj, String name, String cont) {
		if (mode.equals("COSMOS")) {
			return (Result<T>) Cosmos.getInstance(name, cont).deleteOne(obj);
		} else {
			return Hibernate.getInstance().deleteOne(obj);
		}
	}
	
	public <T> Result<T> updateOne(T obj) {
		if (mode.equals("COSMOS")) {
			return cosmos.updateOne(obj);
		} else {
			return Hibernate.getInstance().updateOne(obj);
		}
	}

	public <T> Result<T> insertOne( T obj) {
		if (mode.equals("COSMOS")) {
			return cosmos.insertOne(obj);
		} else {
			return Result.errorOrValue(Hibernate.getInstance().persistOne(obj), obj);
		}
	}

	public <T> Result<T> insertOneSpecific( T obj, String name, String cont) {
		if (mode.equals("COSMOS")) {
			return Cosmos.getInstance(name, cont).insertOne(obj);
		} else {
			return Result.errorOrValue(Hibernate.getInstance().persistOne(obj), obj);
		}
	}

	public <T> Result<List<T>> query(Class<T> clazz, String queryStr) {
		return cosmos.query(clazz, queryStr);
	}

	public <T> Result<List<T>> cosmosTransaction(String query, Class<T> clazz) {
		return cosmos.query(clazz, query);
	}
	
	public <T> Result<T> transaction( Consumer<Session> c) {
		return Hibernate.getInstance().execute( c::accept );
	}
	
	public <T> Result<T> transaction( Function<Session, Result<T>> func) {
		return Hibernate.getInstance().execute( func );
	}
}
