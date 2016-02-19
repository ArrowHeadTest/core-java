package eu.arrowhead.core.authorization;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.model.messages.InterCloudAuthRequest;
import eu.arrowhead.common.model.messages.IntraCloudAuthRequest;
import eu.arrowhead.common.model.messages.IntraCloudAuthResponse;
import eu.arrowhead.core.authorization.database.ArrowheadCloud;
import eu.arrowhead.core.authorization.database.ArrowheadService;
import eu.arrowhead.core.authorization.database.ArrowheadSystem;
import eu.arrowhead.core.authorization.database.Systems_Services;

@Path("authorization")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthorizationResource {
	
	DatabaseManager databaseManager = new DatabaseManager();
	
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "This is the authorization service!";
    }
    
    /**
     * Returns a list of ArrowheadClouds with the same operator from the database.
     * 
     * @param {String} operatorName
     * @return List<ArrowheadCloud>
     */
    @GET
    @Path("/operator/{operatorName}")
    public List<ArrowheadCloud> getClouds(@PathParam("operatorName") String operatorName){
    	List<ArrowheadCloud> cloudList = new ArrayList<ArrowheadCloud>();
    	cloudList = databaseManager.getClouds(operatorName);
    	
    	return cloudList;
    }
    
    /**
     * Returns an ArrowheadCloud from the database specified by the operatorName and cloudName.
     * 
     * @param {String} operatorName
     * @param {String} cloudName
     * @exception DataNotFoundException
     * @return JAX-RS Response with status code 200 and ArrowheadCloud entity
     */
    @GET
    @Path("/operator/{operatorName}/cloud/{cloudName}")
    public Response getCloud(@PathParam("operatorName") String operatorName, 
    		@PathParam("cloudName") String cloudName){
    	ArrowheadCloud arrowheadCloud = databaseManager.getCloudByName(operatorName, cloudName);
    	return Response.ok(arrowheadCloud).build();
    }
    
    /**
     * Checks whether an external Cloud can use a local Service.
     * 
     * @param {String} operatorName
     * @param {String} cloudName
     * @param {InterCloudAuthRequest} request - POJO with the necessary informations
     * @exception DataNotFoundException
     * @return boolean
     */
    @PUT
    @Path("/operator/{operatorName}/cloud/{cloudName}")
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    public Boolean isCloudAuthorized(@PathParam("operatorName") String operatorName, 
    		@PathParam("cloudName") String cloudName, InterCloudAuthRequest request){
    	eu.arrowhead.common.model.ArrowheadService requestedServiceModel = request.getArrowheadService();
    	ArrowheadService requestedService = new ArrowheadService
    			(requestedServiceModel.getServiceGroup(), requestedServiceModel.getServiceDefinition(), 
    					requestedServiceModel.getInterfaces(), requestedServiceModel.getMetaData());
    	ArrowheadCloud arrowheadCloud = databaseManager.getCloudByName(operatorName, cloudName);
    	if(arrowheadCloud == null)
    		return false;
    	boolean isAuthorized = false;
        List<ArrowheadService> retrievedCloudServices = (List<ArrowheadService>) arrowheadCloud.getServiceList();
        for (int j = 0; j < retrievedCloudServices.size(); j++){
         	if(retrievedCloudServices.get(j).isEqual(requestedService))
         		isAuthorized = true; 
         }

    	return isAuthorized;
    }
    
    /**
     * Adds a new Cloud and its consumable Services to the database.
     * 
     * @param {String} operatorName
     * @param {String} cloudName
     * @param {InterCloudAuthEntry} entry - POJO with the necessary informations
     * @param {UriInfo} uriInfo - JAX-RS object containing URI information
     * @exception DuplicateEntryException
     * @return JAX-RS Response with status code 201 and ArrowheadCloud entity
     */
    @POST
    @Path("/operator/{operatorName}/cloud/{cloudName}")
    public Response addCloudToAuthorized(@PathParam("operatorName") String operatorName, 
    		@PathParam("cloudName") String cloudName, InterCloudAuthEntry entry, 
    		@Context UriInfo uriInfo){
    	ArrowheadCloud arrowheadCloud = new ArrowheadCloud();
    	arrowheadCloud.setOperator(operatorName);
    	arrowheadCloud.setCloudName(cloudName);
    	arrowheadCloud.setAuthenticationInfo(entry.getAuthenticationInfo());
    	arrowheadCloud.setServiceList(entry.getServiceList());
    	
    	ArrowheadCloud authorizedCloud = databaseManager.addCloudToAuthorized(arrowheadCloud);
    	
    	URI uri = uriInfo.getAbsolutePathBuilder().build();
    	return Response.created(uri).entity(authorizedCloud).build();
    }
    
    /**
     * Deletes a Cloud and its consumable Services from the database.
     * 
     * @param {String} operatorName
     * @param {String} cloudName
     * @exception DataNotFoundException
     * @return JAX-RS Response with status code 204
     */
    @DELETE
    @Path("/operator/{operatorName}/cloud/{cloudName}")
    public Response deleteCloudFromAuthorized(@PathParam("operatorName") String operatorName, 
    		@PathParam("cloudName") String cloudName){
    	databaseManager.deleteCloudFromAuthorized(operatorName, cloudName);
    	
    	return Response.noContent().build();
    }
    
    /**
     * Returns the list of consumable Services of a Cloud.
     * 
     * @param {String} operatorName
     * @param {String} cloudName
     * @exception DataNotFoundException
     * @return List<ArrowheadService>
     */
    @GET
    @Path("/operator/{operatorName}/cloud/{cloudName}/services")
    public List<ArrowheadService> getCloudServices(@PathParam("operatorName") String operatorName, 
    		@PathParam("cloudName") String cloudName){
    	ArrowheadCloud arrowheadCloud = databaseManager.getCloudByName(operatorName, cloudName);
    	List<ArrowheadService> serviceList = (List<ArrowheadService>) arrowheadCloud.getServiceList();
    	
    	return serviceList;
    }
    
    /**
     * Adds a list of consumable Services to a Cloud.
     * 
     * @param {String} operatorName
     * @param {String} cloudName
     * @exception DataNotFoundException
     * @return List<ArrowheadService>
     */
    @POST
    @Path("/operator/{operatorName}/cloud/{cloudName}/services")
    public List<ArrowheadService> addCloudServices(@PathParam("operatorName") String operatorName, 
    		@PathParam("cloudName") String cloudName, InterCloudAuthEntry entry){
    	ArrowheadCloud arrowheadCloud = databaseManager.getCloudByName(operatorName, cloudName);
    	if(!entry.getAuthenticationInfo().isEmpty()){
    		arrowheadCloud.setAuthenticationInfo(entry.getAuthenticationInfo());
    	}
    	List<ArrowheadService> serviceList = (List<ArrowheadService>) entry.getServiceList();
    	arrowheadCloud.getServiceList().addAll(serviceList);
    	databaseManager.updateAuthorizedCloud(arrowheadCloud);
    	
    	return serviceList;
    }
    
    /**
     * Checks whether the consumer System can use a Service from a list of provider Systems.
     * 
     * @param {String} systemGroup
     * @param {String} systemName
     * @param {IntraCloudAuthRequest} request - POJO with the necessary informations
     * @exception DataNotFoundException
     * @return IntraCloudAuthResponse - POJO containing a HashMap<ArrowheadSystem, boolean>
     * @problem The extra payload information such as the interfaces of the requested service are lost,
     * 			since relations can only be made between persistent object instances.
     */
    @PUT
    @Path("/systemgroup/{systemGroup}/system/{systemName}")
    public IntraCloudAuthResponse isSystemAuthorized(@PathParam("systemGroup") String systemGroup,
    		@PathParam("systemName") String systemName, IntraCloudAuthRequest request){
    	Systems_Services ss = new Systems_Services();
    	HashMap<eu.arrowhead.common.model.ArrowheadSystem, Boolean> authorizationMap = 
    			new HashMap<eu.arrowhead.common.model.ArrowheadSystem, Boolean>();
    	IntraCloudAuthResponse response = new IntraCloudAuthResponse();
    	ArrowheadSystem consumer = databaseManager.getSystemByName(systemGroup, systemName);
    	if(consumer == null){
    		throw new DataNotFoundException("The Consumer System is not found in the database.");
    	}
    	
    	List<ArrowheadService> serviceList = new ArrayList<ArrowheadService>();
    	serviceList = databaseManager.getServiceByName(request.getArrowheadService()
    			.getServiceGroup(), request.getArrowheadService().getServiceDefinition());
    	if(serviceList.isEmpty()){
    		for(eu.arrowhead.common.model.ArrowheadSystem provider : request.getProviderList()){
    			authorizationMap.put(provider, false);
    		}
    		response.setAuthorizationMap(authorizationMap);
    		return response;
    	}
    	
    	for(eu.arrowhead.common.model.ArrowheadSystem provider : request.getProviderList()){
    		ArrowheadSystem retrievedSystem = databaseManager.getSystemByName(provider.getSystemGroup(), 
    				provider.getSystemName());
    		ss = databaseManager.getSS(consumer, retrievedSystem, serviceList.get(0));
        	if(ss == null){
        		authorizationMap.put(provider, false);
        	}
        	else{
        		authorizationMap.put(provider, true);
        	}
    	}
    	
    	response.setAuthorizationMap(authorizationMap);
    	return response;
    }
    
    /**
     * Creates a relation between local Systems, defining the consumable services between Systems.
     * (Not bidirectional.) OneToMany relation between consumer and providers, OneToMany relation
     * between consumer and services.
     * 
     * @param {String} systemGroup
     * @param {String} systemName
     * @param {IntraCloudAuthEntry} entry - POJO with the necessary informations
     * @exception DuplicateEntryException
     * @return JAX-RS Response with status code 201 and ArrowheadSystem entity (consumer side)
     * @problem Multiple POST requests will result in a DuplicateEntryException, if they have the 
     * 			same provider Systems in their payload. This is because of the cascading in the relation
     * 			table. Without cascading Hibernate won't save the relation (TransientObjectException).
     * 			The workaround for now is to delete the uniqeConstraints in the ArrowheadSystem POJO, 
     * 			allowing duplicate entries in that table.
     */
    @POST
    @Path("/systemgroup/{systemGroup}/system/{systemName}")
    public Response addSystemToAuthorized(@PathParam("systemGroup") String systemGroup,
    		@PathParam("systemName") String systemName, IntraCloudAuthEntry entry){
    	ArrowheadSystem consumerSystem = databaseManager.getSystemByName(systemGroup, systemName);
    	if(consumerSystem == null){
    		ArrowheadSystem consumer = new ArrowheadSystem();
    		consumer.setSystemGroup(systemGroup);
    		consumer.setSystemName(systemName);
    		consumer.setIPAddress(entry.getIPAddress());
    		consumer.setPort(entry.getPort());
    		consumer.setAuthenticationInfo(entry.getAuthenticationInfo());
    		consumerSystem = databaseManager.save(consumer);
    	}
    	
    	
    	ArrowheadSystem retrievedSystem = null;
    	List<ArrowheadService> retrievedServiceList = new ArrayList<ArrowheadService>();
    	Systems_Services ss = new Systems_Services();
    	
    	for (ArrowheadSystem providerSystem : entry.getProviderList()){
    		retrievedSystem = databaseManager.getSystemByName(providerSystem.getSystemGroup(), 
    				providerSystem.getSystemName());
    		if(retrievedSystem == null){
    			databaseManager.save(providerSystem);
    		}
    		for(ArrowheadService service : entry.getServiceList()){
    			retrievedServiceList = databaseManager.getServiceByName(service.getServiceGroup(), 
    					service.getServiceDefinition());
    			if(retrievedServiceList.isEmpty()){
    				databaseManager.save(service);
    			}
    			ss.setConsumer(consumerSystem);
    			ss.setProvider(providerSystem);
    			ss.setService(service);
    			databaseManager.saveRelation(ss);
    		}
    	}
    	
    	return Response.status(Status.CREATED).entity(consumerSystem).build();
    }
    
    /**
     * Deletes all the relations where the given System is the consumer.
     * 
     * @param {String} systemGroup
     * @param {String} systemName
     * @exception DataNotFoundException
     * @return JAX-RS Response with status code 204
     */
    @DELETE
    @Path("/systemgroup/{systemGroup}/system/{systemName}")
    public Response deleteRelationsFromAuthorized(@PathParam("systemGroup") String systemGroup,
    		@PathParam("systemName") String systemName){
    	ArrowheadSystem consumer = databaseManager.getSystemByName(systemGroup, systemName);
    	List<Systems_Services> ssList = new ArrayList<Systems_Services>();
    	ssList = databaseManager.getRelations(consumer);
    	for(Systems_Services ss : ssList){
    		databaseManager.delete(ss);
    	}
    	
    	return Response.noContent().build();
    }
    
}