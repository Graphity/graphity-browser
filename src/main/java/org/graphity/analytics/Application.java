/*
 * Copyright (C) 2012 Martynas Jusevičius <martynas@graphity.org>
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

package org.graphity.analytics;

import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import org.graphity.util.manager.DataManager;
import org.graphity.provider.ModelProvider;
import org.graphity.provider.RDFResourceXSLTWriter;
import org.graphity.util.manager.OntDataManager;
import org.graphity.vocabulary.Graphity;
import org.graphity.vocabulary.SIOC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class Application extends javax.ws.rs.core.Application
{
    private static final Logger log = LoggerFactory.getLogger(Application.class);
    private Set<Class<?>> classes = new HashSet<Class<?>>();
    private Set<Object> singletons = new HashSet<Object>();
    @Context private ServletContext context = null;
    //@Context private UriInfo uriInfo = null;
    
    @PostConstruct
    public void init() // initialize locally cached ontologies
    {
	log.debug("Application.init() ServletContext: {}", context);
	try
	{
	    // http://www4.wiwiss.fu-berlin.de/lodcloud/state/#terms
	    // http://incubator.apache.org/jena/documentation/ontology/#compound_ontology_documents_and_imports_processing
	    
	    FileManager.get().addLocatorFile(context.getRealPath("/WEB-INF/"));
	    FileManager.get().addLocatorFile(context.getRealPath("/WEB-INF/owl/"));
	    
	    //log.debug("FileManager.get(): {}", FileManager.get());
	    log.debug("OntDataManager.getInstance().getFileManager(): {}", OntDataManager.getInstance().getFileManager());
	    //FileManager.setGlobalFileManager(DataManager.get());
	    //FileManager.get().addLocator(new LocatorLinkedData());
	    //removeLocatorURL(DataManager.get()).addLocator(new LocatorLinkedData());
	    DataManager.get().setModelCaching(true);
	    OntDataManager.getInstance().setFileManager(DataManager.get());
	    log.debug("OntDataManager is caching Models: {}", OntDataManager.getInstance().getCacheModels());
	    log.debug("FileManager.get(): {}", FileManager.get());
	    log.debug("DataManager.get(): {}", DataManager.get());
	    log.debug("DataManager.get().getLocationMapper(): {}", DataManager.get().getLocationMapper());
	    log.debug("OntDataManager.getInstance().getFileManager(): {}", OntDataManager.getInstance().getFileManager());
	    
	    // load and cache following ontologies (with RDFS inference)
	    OntDataManager.getInstance().getOntology(RDF.getURI(), OntModelSpec.OWL_MEM_RDFS_INF);
	    OntDataManager.getInstance().getOntology(RDFS.getURI(), OntModelSpec.OWL_MEM_RDFS_INF);
	    OntDataManager.getInstance().getOntology(OWL2.getURI(), OntModelSpec.OWL_MEM_RDFS_INF);
	    OntDataManager.getInstance().getOntology(DC.getURI(), OntModelSpec.OWL_MEM_RDFS_INF);
	    OntDataManager.getInstance().getOntology(DCTerms.getURI(), OntModelSpec.OWL_MEM_RDFS_INF);
	    OntDataManager.getInstance().getOntology(FOAF.getURI(), OntModelSpec.OWL_MEM_RDFS_INF);
	    OntDataManager.getInstance().getOntology(SIOC.getURI(), OntModelSpec.OWL_MEM_RDFS_INF);
	    OntDataManager.getInstance().getOntology(Graphity.getURI(), OntModelSpec.OWL_MEM_RDFS_INF);
	    OntDataManager.getInstance().getOntology("http://rdfs.org/ns/void#", OntModelSpec.OWL_MEM_RDFS_INF);
	    OntDataManager.getInstance().getOntology("http://dbpedia.org/ontology/", OntModelSpec.OWL_MEM_RDFS_INF);
	    OntDataManager.getInstance().getOntology("http://graph.facebook.com/schema/user#", OntModelSpec.OWL_MEM_RDFS_INF);

	    /*
	    OntDataManager.getInstance().addModel(RDF.getURI(), DataManager.get().loadModel(RDF.getURI()));
	    OntDataManager.getInstance().addModel(RDFS.getURI(), DataManager.get().loadModel(RDFS.getURI()));
	    OntDataManager.getInstance().addModel(OWL2.getURI(), DataManager.get().loadModel(OWL2.getURI()));
	    OntDataManager.getInstance().addModel(DC.getURI(), DataManager.get().loadModel(DC.getURI()));
	    OntDataManager.getInstance().addModel(DCTerms.getURI(), DataManager.get().loadModel(DCTerms.getURI()));
	    OntDataManager.getInstance().addModel(FOAF.getURI(), DataManager.get().loadModel(FOAF.getURI()));
	    OntDataManager.getInstance().addModel(SIOC.getURI(), DataManager.get().loadModel(SIOC.getURI()));
	    OntDataManager.getInstance().addModel(Graphity.getURI(), DataManager.get().loadModel(Graphity.getURI()));
	    OntDataManager.getInstance().addModel("http://rdfs.org/ns/void#", DataManager.get().loadModel("http://rdfs.org/ns/void#"));
	    OntDataManager.getInstance().addModel("http://dbpedia.org/ontology/", DataManager.get().loadModel("http://dbpedia.org/ontology/"));
	    OntDataManager.getInstance().addModel("http://graph.facebook.com/schema/user#", DataManager.get().loadModel("http://graph.facebook.com/schema/user#"));
	     */
	} catch (Exception ex)
	{
	    log.warn("Could not load ontology", ex);
	}

    }
    
    @Override
    public Set<Class<?>> getClasses()
    {
        classes.add(Resource.class);
        classes.add(FrontPageResource.class);
        classes.add(OAuthResource.class);
	
	//classes.add(ModelProvider.class);
	//classes.add(ResourceXSLTWriter.class);

        return classes;
    }

    @Override
    public Set<Object> getSingletons()
    {
	//singletons.add(new OAuthResource());
	singletons.add(new RDFResourceXSLTWriter());
	singletons.add(new ModelProvider());

	return singletons;
    }
}
