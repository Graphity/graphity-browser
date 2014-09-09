/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.graphity.processor.provider;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestTypeInjectableProvider;
import javax.naming.ConfigurationException;
import javax.servlet.ServletContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import org.apache.jena.riot.RDFDataMgr;
import org.graphity.processor.vocabulary.GP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas
 */
@Provider
public class DatasetProvider extends PerRequestTypeInjectableProvider<Context, Dataset> implements ContextResolver<Dataset>
{
    private static final Logger log = LoggerFactory.getLogger(DatasetProvider.class);

    @Context UriInfo uriInfo;
    @Context ServletContext servletContext;

    public DatasetProvider()
    {
        super(Dataset.class);
    }

    @Override
    public Injectable<Dataset> getInjectable(ComponentContext cc, Context a)
    {
	return new Injectable<Dataset>()
	{
	    @Override
	    public Dataset getValue()
	    {
		return getDataset();
	    }
	};
    }
    
    public Dataset getDataset()
    {
        try
        {
            String datasetLocation = getDatasetLocation(getServletContext(), GP.datasetLocation.getURI());
            if (datasetLocation == null)
            {
                if (log.isErrorEnabled()) log.error("Application dataset (gp:datasetLocation) is not configured in web.");
                throw new ConfigurationException("Application dataset (gp:datasetLocation) is not configured in web.xml");
            }
            
            return getDataset(datasetLocation, getUriInfo());
        }
        catch (ConfigurationException ex)
        {
            throw new WebApplicationException(ex);
        }
    }
    
    public String getDatasetLocation(ServletContext servletContext, String property)
    {
        Object datasetLocation = servletContext.getInitParameter(property);
        if (datasetLocation != null) return datasetLocation.toString();
        
        return null;
    }
    
    public Dataset getDataset(String datasetLocation, UriInfo uriInfo) throws ConfigurationException
    {
        if (datasetLocation == null) throw new IllegalArgumentException("Location String cannot be null");
        if (uriInfo == null) throw new IllegalArgumentException("UriInfo cannot be null");
	
        Dataset dataset = DatasetFactory.createMem();
        RDFDataMgr.read(dataset, datasetLocation.toString(), uriInfo.getBaseUri().toString(), null); // Lang.TURTLE
        return dataset;
    }

    public UriInfo getUriInfo()
    {
        return uriInfo;
    }

    public ServletContext getServletContext()
    {
        return servletContext;
    }

    @Override
    public Dataset getContext(Class<?> type)
    {
        return getDataset();
    }
    
}