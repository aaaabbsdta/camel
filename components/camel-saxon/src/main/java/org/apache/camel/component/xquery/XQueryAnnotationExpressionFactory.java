/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.xquery;

import java.lang.annotation.Annotation;

import org.w3c.dom.Node;

import net.sf.saxon.functions.CollectionFn;
import org.apache.camel.CamelContext;
import org.apache.camel.Expression;
import org.apache.camel.support.builder.ExpressionBuilder;
import org.apache.camel.support.language.DefaultAnnotationExpressionFactory;
import org.apache.camel.support.language.LanguageAnnotation;
import org.apache.camel.support.language.NamespacePrefix;
import org.apache.camel.util.ObjectHelper;

public class XQueryAnnotationExpressionFactory extends DefaultAnnotationExpressionFactory {

    @Override
    public Expression createExpression(
            CamelContext camelContext, Annotation annotation,
            LanguageAnnotation languageAnnotation, Class<?> expressionReturnType) {
        String xQuery = getExpressionFromAnnotation(annotation);
        XQueryBuilder builder = XQueryBuilder.xquery(xQuery);
        if (annotation instanceof XQuery) {
            XQuery xQueryAnnotation = (XQuery) annotation;
            builder.setStripsAllWhiteSpace(xQueryAnnotation.stripsAllWhiteSpace());

            String variableName = null;
            String headerName = null;
            String propertyName = null;
            if (ObjectHelper.isNotEmpty(xQueryAnnotation.variableName())) {
                variableName = xQueryAnnotation.variableName();
            }
            if (ObjectHelper.isNotEmpty(xQueryAnnotation.headerName())) {
                headerName = xQueryAnnotation.headerName();
            }
            if (ObjectHelper.isNotEmpty(xQueryAnnotation.propertyName())) {
                propertyName = xQueryAnnotation.propertyName();
            }
            if (variableName != null || headerName != null || propertyName != null) {
                builder.setSource(ExpressionBuilder.singleInputExpression(variableName, headerName, propertyName));
            }

            NamespacePrefix[] namespaces = xQueryAnnotation.namespaces();
            if (namespaces != null) {
                for (NamespacePrefix namespacePrefix : namespaces) {
                    builder = builder.namespace(namespacePrefix.prefix(), namespacePrefix.uri());
                }
            }
        }
        Class<?> resultType = getResultType(annotation);
        if (resultType.equals(Object.class)) {
            resultType = expressionReturnType;
        }
        if (resultType.isAssignableFrom(String.class)) {
            builder.setResultsFormat(ResultFormat.String);
        } else if (resultType.isAssignableFrom(CollectionFn.class)) {
            builder.setResultsFormat(ResultFormat.List);
        } else if (resultType.isAssignableFrom(Node.class)) {
            builder.setResultsFormat(ResultFormat.DOM);
        } else if (resultType.isAssignableFrom(byte[].class)) {
            builder.setResultsFormat(ResultFormat.Bytes);
        }
        return builder;
    }

    protected Class<?> getResultType(Annotation annotation) {
        return (Class<?>) getAnnotationObjectValue(annotation, "resultType");
    }
}
