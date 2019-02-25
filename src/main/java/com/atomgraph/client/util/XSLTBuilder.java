/**
 *  Copyright 2012 Martynas Jusevičius <martynas@atomgraph.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.atomgraph.client.util;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * Utility class that simplifies building of XSLT transformations.
 * Uses builder pattern.
 * 
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class XSLTBuilder
{
    
    private static final Logger log = LoggerFactory.getLogger(XSLTBuilder.class) ;

    private final SAXTransformerFactory factory;
    private final Map<String, Object> parameters = new HashMap<>();
    private final Map<String, String> outputProperties = new HashMap<>();
    private Templates templates = null;
    private URIResolver uriResolver = null;
    private Source source = null;
    private Result result = null;
    
    protected XSLTBuilder(SAXTransformerFactory factory)
    {
        if (factory == null) throw new IllegalStateException("SAXTransformerFactory cannot be null");
        
        this.factory = factory;
    }
    
    public static XSLTBuilder newInstance(SAXTransformerFactory factory)
    {
        return new XSLTBuilder(factory);
    }

    public XSLTBuilder document(Source doc)
    {
        if (log.isTraceEnabled()) log.trace("Loading document Source with system ID: {}", doc.getSystemId());
        this.source = doc;
        return this;
    }

    public XSLTBuilder document(Node n)
    {
        document(new DOMSource(n));
        return this;
    }

    public XSLTBuilder document(Node n, String systemId)
    {
        document(new DOMSource(n, systemId));
        return this;
    }

    public XSLTBuilder document(File file)
    {
        document(new StreamSource(file));
        return this;
    }

    public XSLTBuilder document(InputStream is)
    {
        document(new StreamSource(is));
        return this;
    }

    public XSLTBuilder document(InputStream is, String systemId)
    {
        document(new StreamSource(is, systemId));
        return this;
    }

    public XSLTBuilder document(Reader reader)
    {
        document(new StreamSource(reader));
        return this;
    }

    public XSLTBuilder document(Reader reader, String systemId)
    {
        document(new StreamSource(reader, systemId));
        return this;
    }

    public XSLTBuilder document(String systemId)
    {
        document(new StreamSource(systemId));
        return this;
    }

    public XSLTBuilder stylesheet(File stylesheet) throws TransformerConfigurationException
    {
        return stylesheet(new StreamSource(stylesheet));
    }
    
    public XSLTBuilder stylesheet(Source stylesheet) throws TransformerConfigurationException
    {
        return stylesheet(factory.newTemplates(stylesheet));
    }

    public XSLTBuilder stylesheet(Templates templates) throws TransformerConfigurationException
    {
        this.templates = templates;
        return this;
    }

    public XSLTBuilder parameter(String name, Object value)
    {
        if (log.isTraceEnabled()) log.trace("Setting transformer parameter {} with value {}", name, value);
        parameters.put(name, value);
        return this;
    }
    
    public XSLTBuilder resolver(URIResolver uriResolver)
    {
        if (log.isTraceEnabled()) log.trace("Setting URIResolver: {}", uriResolver);
        this.uriResolver = uriResolver;
        return this;
    }

    public XSLTBuilder outputProperty(String name, String value)
    {
        if (log.isTraceEnabled()) log.trace("Setting transformer OutputProperty {} with value {}", name, value);
        outputProperties.put(name, value);
        return this;
    }
    
    protected Transformer getTransformer(Templates templates, Result result, URIResolver uriResolver, Map<String, Object> parameters, Map<String, String> outputProperties) throws TransformerConfigurationException
    {
        TransformerHandler handler = getSAXTransformerFactory().newTransformerHandler(templates);
        handler.setResult(result);
        
        Transformer transformer = handler.getTransformer();
        transformer.setURIResolver(uriResolver);
        
        Iterator<Entry<String, Object>> paramIt = parameters.entrySet().iterator();
        while (paramIt.hasNext())
        {
            Entry<String, Object> param = paramIt.next();
            transformer.setParameter(param.getKey(), param.getValue());
        }

        Iterator<Entry<String, String>> propertyIt = outputProperties.entrySet().iterator();
        while (propertyIt.hasNext())
        {
            Entry<String, String> outputProperty = propertyIt.next();
            transformer.setOutputProperty(outputProperty.getKey(), outputProperty.getValue());
        }
        
        return transformer;
    }
    
    protected Transformer getTransformer() throws TransformerConfigurationException
    {
        return getTransformer(getTemplates(), getResult(), getURIResolver(), getParameters(), getOutputProperties());
    }
    
    public void transform() throws TransformerException
    {       
        getTransformer().transform(getSource(), getResult());
    }

    public XSLTBuilder result(Result result) throws TransformerConfigurationException
    {
        this.result = result;
        return this;
    }
    
    protected SAXTransformerFactory getSAXTransformerFactory()
    {
        return factory;
    }
    
    protected Templates getTemplates()
    {
        return templates;
    }
    
    protected Result getResult()
    {
        return result;
    }
    
    protected URIResolver getURIResolver()
    {
        return uriResolver;
    }
    
    protected Map<String, Object> getParameters()
    {
        return parameters;
    }
    
    protected Map<String, String> getOutputProperties()
    {
        return outputProperties;
    }
    
    protected Source getSource()
    {
        return source;
    }
    
}