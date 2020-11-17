<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright 2019 Martynas Jusevičius <martynas@atomgraph.com>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->
<!DOCTYPE xsl:stylesheet [
    <!ENTITY ac     "https://w3id.org/atomgraph/client#">
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs   "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY geo    "http://www.w3.org/2003/01/geo/wgs84_pos#">
    <!ENTITY ldt    "https://www.w3.org/ns/ldt#">
    <!ENTITY foaf   "http://xmlns.com/foaf/0.1/">
]>
<xsl:stylesheet version="2.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:ac="&ac;"
xmlns:rdf="&rdf;"
xmlns:ldt="&ldt;"
xmlns:geo="&geo;"
xmlns:foaf="&foaf;"
xmlns:bs2="http://graphity.org/xsl/bootstrap/2.3.2"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
exclude-result-prefixes="#all">

    <!-- BLOCK MODE -->

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:Block">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>

        <div>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>

            <xsl:apply-templates select="." mode="bs2:Header"/>

            <xsl:apply-templates select="." mode="bs2:PropertyList"/>
        </div>
    </xsl:template>

    <!-- inline blank node resource if there is only one property except foaf:primaryTopic having it as object -->
    <xsl:template match="@rdf:nodeID[key('resources', .)][count(key('predicates-by-object', .)[not(self::foaf:primaryTopic)]) = 1]" mode="bs2:Block" priority="2">
        <xsl:param name="inline" select="true()" as="xs:boolean" tunnel="yes"/>

        <xsl:choose>
            <xsl:when test="$inline">
                <xsl:apply-templates select="key('resources', .)" mode="#current">
                    <xsl:with-param name="display" select="$inline" tunnel="yes"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- hide inlined blank node resources from the main block flow -->
    <xsl:template match="*[*][key('resources', @rdf:nodeID)][count(key('predicates-by-object', @rdf:nodeID)[not(self::foaf:primaryTopic)]) = 1]" mode="bs2:Block" priority="1">
        <xsl:param name="display" select="false()" as="xs:boolean" tunnel="yes"/>
        
        <xsl:if test="$display">
            <xsl:next-match/>
        </xsl:if>
    </xsl:template>
    
    <!-- HEADER MODE -->
    
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:Header">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'well header'" as="xs:string?"/>

        <div>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>

            <xsl:apply-templates select="." mode="bs2:Image"/>
            
            <xsl:apply-templates select="." mode="bs2:Actions"/>

            <h2>
                <xsl:apply-templates select="." mode="xhtml:Anchor"/>
            </h2>
            
            <p>
                <xsl:apply-templates select="." mode="ac:description"/>
            </p>

            <xsl:apply-templates select="." mode="bs2:TypeList"/>
        </div>
    </xsl:template>
    
    <!-- LIST MODE -->
    
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:List">
        <xsl:param name="active" as="xs:boolean?"/>
        
        <li>
            <xsl:if test="$active">
                <xsl:attribute name="class">active</xsl:attribute>
            </xsl:if>

            <xsl:apply-templates select="." mode="xhtml:Anchor"/>
        </li>
    </xsl:template>
    
    <!-- ACTIONS MODE (Create/Edit buttons) -->

    <xsl:template match="*[@rdf:about]" mode="bs2:Actions" priority="1">
        <div class="pull-right">
            <form action="{ac:document-uri(@rdf:about)}?_method=DELETE" method="post">
                <button class="btn btn-primary btn-delete" type="submit">
                    <xsl:apply-templates select="key('resources', '&ac;Delete', document('&ac;'))" mode="ac:label" use-when="system-property('xsl:product-name') = 'SAXON'"/>
                    <xsl:text use-when="system-property('xsl:product-name') eq 'Saxon-JS'">Delete</xsl:text> <!-- TO-DO: cache ontologies in localStorage -->
                </button>
            </form>
        </div>

        <div class="pull-right">
            <a class="btn btn-primary" href="?uri={encode-for-uri(ac:document-uri(@rdf:about))}&amp;mode={encode-for-uri('&ac;EditMode')}">
                <xsl:apply-templates select="key('resources', '&ac;EditMode', document('&ac;'))" mode="ac:label" use-when="system-property('xsl:product-name') = 'SAXON'"/>
                <xsl:text use-when="system-property('xsl:product-name') eq 'Saxon-JS'">Edit</xsl:text> <!-- TO-DO: cache ontologies in localStorage -->
            </a>
        </div>
    </xsl:template>
    
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:Actions"/>
    
    <!-- IMAGE MODE -->

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:Image">
        <xsl:variable name="prelim-images" as="item()*">
            <xsl:apply-templates mode="ac:image"/>
        </xsl:variable>
        <xsl:variable name="images" select="$prelim-images/self::*" as="element()*"/>

        <xsl:if test="$images">
            <div class="carousel slide">
                <div class="carousel-inner">
                    <xsl:for-each select="$images">
                        <div class="item">
                            <xsl:if test="position() = 1">
                                <xsl:attribute name="class">active item</xsl:attribute>
                            </xsl:if>
                            <xsl:copy-of select="."/>
                        </div>
                    </xsl:for-each>
                    <a class="carousel-control left" onclick="$(this).parents('.carousel').carousel('prev');">&#8249;</a>
                    <a class="carousel-control right" onclick="$(this).parents('.carousel').carousel('next');">&#8250;</a>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
    
    <!-- TYPE MODE -->
        
    <xsl:template match="*[@rdf:about or @rdf:nodeID][rdf:type/@rdf:resource]" mode="bs2:TypeList" priority="1">
        <ul class="inline">
            <xsl:for-each select="rdf:type/@rdf:resource">
                <xsl:sort select="ac:object-label(.)" order="ascending" lang="{$ldt:lang}" use-when="system-property('xsl:product-name') = 'SAXON'"/>
                <xsl:sort select="ac:object-label(.)" order="ascending" use-when="system-property('xsl:product-name') eq 'Saxon-JS'"/>
                
                <xsl:choose use-when="system-property('xsl:product-name') = 'SAXON'">
                    <xsl:when test="doc-available(ac:document-uri(.))">
                        <xsl:apply-templates select="key('resources', ., document(ac:document-uri(.)))" mode="bs2:TypeListItem"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="."/>
                    </xsl:otherwise>
                </xsl:choose>
                
                <li use-when="system-property('xsl:product-name') eq 'Saxon-JS'">
                    <span title="{.}" class="btn btn-type">
                        <xsl:apply-templates select="." mode="xhtml:Anchor"/>
                    </span>
                </li>
            </xsl:for-each>
        </ul>
    </xsl:template>

    <xsl:template match="*" mode="bs2:TypeList"/>

    <xsl:template match="*[@rdf:about]" mode="bs2:TypeListItem">
        <li>
            <span title="{@rdf:about}" class="btn btn-type">
               <xsl:apply-templates select="."/>
            </span>
        </li>
    </xsl:template>

    <!-- PROPERTY LIST MODE -->

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:PropertyList">
        <xsl:variable name="properties" as="element()*">
            <xsl:apply-templates mode="#current">
                <xsl:sort select="ac:property-label(.)" data-type="text" order="ascending" lang="{$ldt:lang}"/>
            </xsl:apply-templates>
        </xsl:variable>

        <xsl:if test="$properties">
            <dl class="dl-horizontal">
                <xsl:copy-of select="$properties"/>
            </dl>
        </xsl:if>
    </xsl:template>
    
    <!-- FORM CONTROL MODE -->
    
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:FormControl" use-when="system-property('xsl:product-name') = 'SAXON'">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="legend" select="if (@rdf:about) then true() else not(key('predicates-by-object', @rdf:nodeID))" as="xs:boolean"/>
        <xsl:param name="violations" select="key('violations-by-root', (@rdf:about, @rdf:nodeID))" as="element()*"/>
        <xsl:param name="template-doc" select="if ($ldt:ontology) then ac:construct-doc($ldt:ontology, $ac:forClass, $ldt:base) else ()" as="document-node()?"/>
        <xsl:param name="template" select="$template-doc/rdf:RDF/*[@rdf:nodeID][every $type in rdf:type/@rdf:resource satisfies current()/rdf:type/@rdf:resource = $type]" as="element()*"/>
        <xsl:param name="traversed-ids" select="@rdf:*" as="xs:string*" tunnel="yes"/>

        <fieldset>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>

            <xsl:apply-templates select="$violations" mode="bs2:Violation"/>

            <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="#current"/>

            <xsl:if test="not($template)">
                <xsl:message>Template is not defined for resource '<xsl:value-of select="@rdf:about | @rdf:nodeID"/>' with types '<xsl:value-of select="rdf:type/@rdf:resource"/>'</xsl:message>
            </xsl:if>
            <xsl:apply-templates select="* | $template/*[not(concat(namespace-uri(), local-name(), @xml:lang, @rdf:datatype) = current()/*/concat(namespace-uri(), local-name(), @xml:lang, @rdf:datatype))]" mode="#current">
                <xsl:sort select="ac:property-label(.)"/>
                <xsl:with-param name="violations" select="$violations"/>
                <xsl:with-param name="traversed-ids" select="$traversed-ids" tunnel="yes"/>
            </xsl:apply-templates>
        </fieldset>
    </xsl:template>
    
</xsl:stylesheet>