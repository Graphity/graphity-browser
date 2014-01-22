/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphity.client.resource.labelled;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.ResourceConfig;
import java.net.URI;
import java.util.regex.Pattern;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.graphity.client.model.ResourceBase;
import org.graphity.processor.query.SelectBuilder;
import org.graphity.processor.vocabulary.LDP;
import org.graphity.server.model.SPARQLEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Path("/{path}/labelled")
public class Container extends ResourceBase
{
    private static final Logger log = LoggerFactory.getLogger(Container.class);

    private final String searchString;
    
    public Container(@Context UriInfo uriInfo, @Context Request request, @Context HttpHeaders httpHeaders, @Context ResourceConfig resourceConfig, @Context SecurityContext securityContext, @Context HttpContext httpContext,
	    @Context OntModel sitemap, @Context SPARQLEndpoint endpoint,
	    @QueryParam("limit") @DefaultValue("20") Long limit,
	    @QueryParam("offset") @DefaultValue("0") Long offset,
	    @QueryParam("order-by") @DefaultValue("label") String orderBy,
	    @QueryParam("desc") @DefaultValue("false") Boolean desc,
	    @QueryParam("graph") URI graphURI,
	    @QueryParam("mode") URI mode,
            @QueryParam("label") String searchString)
    {
	super(uriInfo, request, httpHeaders, resourceConfig,
		sitemap, endpoint,
		limit, offset, orderBy, desc, graphURI, mode);
	this.searchString = searchString;
	
	if (hasRDFType(LDP.Container))
	{
            SelectBuilder selectBuilder = getQueryBuilder().getSubSelectBuilder();
	    if (searchString != null && selectBuilder != null)
	    {
		selectBuilder.filter(RDFS.label.getLocalName(), Pattern.compile(Pattern.quote(searchString)).toString());
		if (log.isDebugEnabled()) log.debug("Search query: {} QueryBuilder: {}", searchString, getQueryBuilder());
	    }
        }	
    }

    public String getSearchString()
    {
	return searchString;
    }

    @Override
    public UriBuilder getPageUriBuilder()
    {
	if (getSearchString() != null) return super.getPageUriBuilder().queryParam("query", getSearchString());
	
	return super.getPageUriBuilder();
    }

    @Override
    public UriBuilder getPreviousUriBuilder()
    {
	if (getSearchString() != null) return super.getPreviousUriBuilder().queryParam("query", getSearchString());
	
	return super.getPreviousUriBuilder();
    }

    @Override
    public UriBuilder getNextUriBuilder()
    {
	if (getSearchString() != null) return super.getNextUriBuilder().queryParam("query", getSearchString());
	
	return super.getNextUriBuilder();
    }

}