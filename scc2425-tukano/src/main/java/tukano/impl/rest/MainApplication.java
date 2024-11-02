package tukano.impl.rest;

import java.util.HashSet;
import java.util.Set;

import jakarta.ws.rs.core.Application;
import tukano.impl.Token;
import utils.Props;

public class MainApplication extends Application
{
	private Set<Object> singletons = new HashSet<>();
	private Set<Class<?>> resources = new HashSet<>();

	public MainApplication() {
		Token.setSecret("supersecretni$$an");
		resources.add(RestBlobsResource.class);
		resources.add(RestShortsResource.class);
		resources.add(RestUsersResource.class);
		//singletons.add(new BlobsResources());
		
		Props.load("azurekeys-region.props"); //place the props file in resources folder under java/main
	}

	@Override
	public Set<Class<?>> getClasses() {
		return resources;
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}
}
