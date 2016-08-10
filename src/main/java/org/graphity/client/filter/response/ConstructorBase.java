/*
 * Copyright 2015 Martynas Jusevičius <martynas@graphity.org>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.graphity.client.filter.response;

import org.apache.jena.ontology.AllValuesFromRestriction;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.graphity.client.exception.SitemapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.inference.SPINConstructors;
import org.topbraid.spin.util.CommandWrapper;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.util.SPINQueryFinder;
import org.topbraid.spin.vocabulary.SP;
import org.topbraid.spin.vocabulary.SPIN;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class ConstructorBase
{
    private static final Logger log = LoggerFactory.getLogger(ConstructorBase.class);

    public Resource construct(OntClass forClass, Model targetModel)
    {
        if (forClass == null) throw new IllegalArgumentException("OntClass cannot be null");
        if (targetModel == null) throw new IllegalArgumentException("Model cannot be null");

        return addInstance(forClass, SPIN.constructor, targetModel.createResource(), targetModel);
    }

    // workaround for SPIN API limitation: https://groups.google.com/d/msg/topbraid-users/AVXXEJdbQzk/w5NrJFs35-0J
    public OntModel fixOntModel(OntModel ontModel)
    {
        if (ontModel == null) throw new IllegalArgumentException("OntModel cannot be null");

        OntModel fixedModel = ModelFactory.createOntologyModel(ontModel.getSpecification());
        fixedModel.add(ontModel);
        
        List<Statement> toDelete = new ArrayList<>();
        StmtIterator it = fixedModel.listStatements(null, SP.text, (RDFNode)null);
        try
        {
            while (it.hasNext())
            {
                Statement stmt = it.next();
                Resource queryOrTemplateCall = stmt.getSubject();
                StmtIterator propIt = queryOrTemplateCall.listProperties();
                try
                {
                    while (propIt.hasNext())
                    {
                        Statement propStmt = propIt.next();
                        if (!propStmt.getPredicate().equals(RDF.type) && !propStmt.getPredicate().equals(SP.text))
                            toDelete.add(propStmt);
                    }
                }
                finally
                {
                    propIt.close();
                }
            }            
        }
        finally
        {
            it.close();
        }
        
        fixedModel.remove(toDelete);
        
        return fixedModel;
    }
    
    public Resource addInstance(OntClass forClass, Property property, Resource instance, Model targetModel)
    {
        if (forClass == null) throw new IllegalArgumentException("OntClass cannot be null");
        if (property == null) throw new IllegalArgumentException("Property cannot be null");
        if (instance == null) throw new IllegalArgumentException("Resource cannot be null");
        if (targetModel == null) throw new IllegalArgumentException("Model cannot be null");
        
        Statement stmt = getConstructorStmt(forClass, property);
        if (stmt == null || !stmt.getObject().isResource())
        {
            if (log.isErrorEnabled()) log.error("Constructor is invoked but {} is not defined for class '{}'", property, forClass.getURI());
            throw new SitemapException("Constructor is invoked but '" + property.getURI() + "' not defined for class '" + forClass.getURI() +"'");
        }

        List<Resource> newResources = new ArrayList<>();
        Set<Resource> reachedTypes = new HashSet<>();
        OntModel fixedModel = fixOntModel(forClass.getOntModel());
        Map<Resource, List<CommandWrapper>> class2Constructor = SPINQueryFinder.getClass2QueryMap(fixedModel, fixedModel, property, false, false);
        SPINConstructors.constructInstance(fixedModel, instance, forClass, targetModel, newResources, reachedTypes, class2Constructor, null, null, null);
        instance.addProperty(RDF.type, forClass);
        
        // evaluate AllValuesFromRestriction to construct related instances
        ExtendedIterator<OntClass> superClassIt = forClass.listSuperClasses();
        try
        {
            while (superClassIt.hasNext())
            {
                OntClass superClass = superClassIt.next();
                if (superClass.canAs(AllValuesFromRestriction.class))
                {
                    AllValuesFromRestriction avfr = superClass.as(AllValuesFromRestriction.class);
                    if (avfr.getAllValuesFrom().canAs(OntClass.class))
                    {
                        OntClass valueClass = avfr.getAllValuesFrom().as(OntClass.class);
                        if (!valueClass.equals(forClass)) // avoid circular restrictions
                        {
                            Resource value = targetModel.createResource().
                                addProperty(RDF.type, valueClass);
                            instance.addProperty(avfr.getOnProperty(), value);
                        
                            // add inverse properties
                            ExtendedIterator<? extends OntProperty> it = avfr.getOnProperty().listInverseOf();
                            try
                            {
                                while (it.hasNext())
                                {
                                    value.addProperty(it.next(), instance);
                                }
                            }
                            finally
                            {
                                it.close();
                            }

                            addInstance(valueClass, property, value, targetModel);
                        }
                    }
                }
            }
        }
        finally
        {
            superClassIt.close();
        }
        
        return instance;
    }
    
    public Statement getConstructorStmt(Resource cls, Property property)
    {
        if (cls == null) throw new IllegalArgumentException("Resource cannot be null");
        if (property == null) throw new IllegalArgumentException("Property cannot be null");

        Statement stmt = cls.getProperty(property);        
        if (stmt != null) return stmt;
        
        for (Resource superCls : JenaUtil.getAllSuperClasses(cls))
        {
            Statement superClassStmt = getConstructorStmt(superCls, property);
            if (superClassStmt != null) return superClassStmt;
        }
        
        return null;
    }
    
}
