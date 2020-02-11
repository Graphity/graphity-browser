<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright 2012 Martynas Jusevičius <martynas@atomgraph.com>

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
    <!ENTITY java   "http://xml.apache.org/xalan/java/">
    <!ENTITY ac     "https://w3id.org/atomgraph/client#">
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs   "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY xsd    "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY owl    "http://www.w3.org/2002/07/owl#">
    <!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
]>
<xsl:stylesheet version="2.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:ac="&ac;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:xsd="&xsd;"
xmlns:owl="&owl;"
xmlns:sparql="&sparql;"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:url="&java;java.net.URLDecoder"
exclude-result-prefixes="#all">

    <xsl:template match="*[@rdf:about]" mode="xhtml:Anchor">
        <xsl:param name="href" select="xs:anyURI(concat('?uri=', encode-for-uri(@rdf:about)))" as="xs:anyURI"/>
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="title" select="@rdf:about" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        
        <xsl:next-match>
            <xsl:with-param name="href" select="$href"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="title" select="$title"/>
            <xsl:with-param name="class" select="$class"/>
        </xsl:next-match>
    </xsl:template>
    
    <xsl:template match="@rdf:resource | sparql:uri">
        <xsl:param name="href" select="xs:anyURI(concat('?uri=', encode-for-uri(if (contains(., '#')) then substring-before(., '#') else .), if (substring-after(., '#')) then concat('#', substring-after(., '#')) else ()))" as="xs:anyURI"/>
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="title" select="." as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        
        <xsl:next-match>
            <xsl:with-param name="href" select="$href"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="title" select="$title"/>
            <xsl:with-param name="class" select="$class"/>
        </xsl:next-match>
    </xsl:template>
    
</xsl:stylesheet>
