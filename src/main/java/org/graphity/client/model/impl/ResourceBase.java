/*
 * Copyright (C) 2013 Martynas Jusevičius <martynas@graphity.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graphity.client.model.impl;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.vocabulary.RDF;
import com.sun.jersey.api.core.ResourceContext;
import java.net.URI;
import java.util.List;
import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;
import org.graphity.client.vocabulary.GC;
import org.graphity.processor.vocabulary.LDP;
import org.graphity.server.model.SPARQLEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.vocabulary.SPIN;

/**
 * Base class of generic read-write Graphity Client resources.
 * Supports pagination on containers (implemented using SPARQL query solution modifiers).
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see <a href="http://www.w3.org/TR/sparql11-query/#solutionModifiers">15 Solution Sequences and Modifiers</a>
 */
@Path("/")
public class ResourceBase extends org.graphity.processor.model.impl.ResourceBase
{
    private static final Logger log = LoggerFactory.getLogger(ResourceBase.class);

    private final URI mode;

    /**
     * JAX-RS-compatible resource constructor with injected initialization objects.
     * 
     * @param uriInfo URI information of the current request
     * @param request current request
     * @param httpHeaders HTTP headers of the current request
     * @param servletContext webapp context
     * @param endpoint SPARQL endpoint of this resource
     * @param limit pagination <code>LIMIT</code> (<samp>limit</samp> query string param)
     * @param offset pagination <code>OFFSET</code> (<samp>offset</samp> query string param)
     * @param orderBy pagination <code>ORDER BY</code> variable name (<samp>order-by</samp> query string param)
     * @param desc pagination <code>DESC</code> value (<samp>desc</samp> query string param)
     * @param graphURI target <code>GRAPH</code> name (<samp>graph</samp> query string param)
     * @param mode <samp>mode</samp> query string param
     */
    public ResourceBase(@Context UriInfo uriInfo, @Context SPARQLEndpoint endpoint, @Context OntModel ontModel,
            @Context Request request, @Context ServletContext servletContext, @Context HttpHeaders httpHeaders, @Context ResourceContext resourceContext,
            @QueryParam("limit") Long limit,
	    @QueryParam("offset") Long offset,
	    @QueryParam("order-by") String orderBy,
	    @QueryParam("desc") Boolean desc,
	    @QueryParam("graph") URI graphURI,
	    @QueryParam("mode") URI mode)
    {
	super(uriInfo, endpoint, ontModel,
                request, servletContext, httpHeaders, resourceContext,
		limit, offset, orderBy, desc, graphURI);
	this.mode = mode;
    }

    @Path("sparql")
    @Override
    public Object getSPARQLResource()
    {
        // refactor with selectVariant()?
	MediaType mostAcceptable = getHttpHeaders().getAcceptableMediaTypes().get(0);

	// check formats supported by Jena instead
        // getUserQuery() != null && 
	if (mostAcceptable.isCompatible(org.graphity.server.MediaType.APPLICATION_RDF_XML_TYPE) ||
	    mostAcceptable.isCompatible(org.graphity.server.MediaType.TEXT_TURTLE_TYPE) ||
	    mostAcceptable.isCompatible(org.graphity.server.MediaType.APPLICATION_SPARQL_RESULTS_XML_TYPE))
	{
            return super.getSPARQLResource();
        }
        
        return this;
    }
    
    @Override
    
    public Model describe()
    {
	Model description;
        
        if (getMode() != null && getMatchedOntClass().hasSuperClass(LDP.Container) &&
            (getMode().equals(URI.create(GC.CreateMode.getURI())) || getMode().equals(URI.create(GC.EditMode.getURI()))))
	{
	    if (log.isDebugEnabled()) log.debug("Mode is {}, returning default DESCRIBE Model", getMode());
	    description = getSPARQLEndpoint().loadModel(getQuery(getURI()));
	}
        else
            description = super.describe();

	if (log.isDebugEnabled()) log.debug("OntResource {} gets type of OntClass: {}", this, getMatchedOntClass());
	addProperty(RDF.type, getMatchedOntClass()); // getOntModel().add(description); ?
	
	// set metadata properties after description query is executed
	getQueryBuilder().build(); // sets sp:text value
	if (log.isDebugEnabled()) log.debug("OntResource {} gets explicit spin:query value {}", this, getQueryBuilder());
	addProperty(SPIN.query, getQueryBuilder());

	return description;
    }

    /**
     * Builds a list of acceptable response variants
     * 
     * @return supported variants
     */
    @Override
    public List<Variant> getVariants()
    {
        List<Variant> list = super.getVariants();
        list.add(0, new Variant(MediaType.APPLICATION_XHTML_XML_TYPE, null, null));
        return list;
    }
    
    public URI getMode()
    {
	return mode;
    }

    @Override
    public UriBuilder getPageUriBuilder()
    {
	if (getMode() != null) return super.getPageUriBuilder().queryParam("mode", getMode());
	
	return super.getPageUriBuilder();
    }

    @Override
    public UriBuilder getPreviousUriBuilder()
    {
	if (getMode() != null) return super.getPreviousUriBuilder().queryParam("mode", getMode());
	
	return super.getPreviousUriBuilder();
    }

    @Override
    public UriBuilder getNextUriBuilder()
    {
	if (getMode() != null) return super.getNextUriBuilder().queryParam("mode", getMode());
	
	return super.getNextUriBuilder();
    }
    
}