/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.core.metamodel.facets.param.validate.method;

import java.lang.reflect.Method;

import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.metamodel.commons.StringExtensions;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.MethodFinderUtils;
import org.apache.isis.core.metamodel.facets.MethodLiteralConstants;
import org.apache.isis.core.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.param.validate.ActionParameterValidationFacet;

import lombok.val;

/**
 * Sets up {@link ActionParameterValidationFacet}.
 */
public class ActionParameterValidationFacetViaMethodFactory extends MethodPrefixBasedFacetFactoryAbstract  {

    private static final Can<String> PREFIXES = Can.ofSingleton(MethodLiteralConstants.VALIDATE_PREFIX);

    /**
     * Note that the {@link Facet}s registered are the generic ones from
     * noa-architecture (where they exist)
     */
    public ActionParameterValidationFacetViaMethodFactory() {
        super(FeatureType.PARAMETERS_ONLY, OrphanValidation.VALIDATE, PREFIXES);
    }


    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {

        val cls = processParameterContext.getCls();
        val actionMethod = processParameterContext.getMethod();
        final int paramNum = processParameterContext.getParamNum();
        val paramType = processParameterContext.getParameterType();
        final IdentifiedHolder facetHolder = processParameterContext.getFacetHolder();

        final String capitalizedName = StringExtensions.asCapitalizedName(actionMethod.getName());

        final String validateName = MethodLiteralConstants.VALIDATE_PREFIX + paramNum + capitalizedName;
        final Method validateMethod = MethodFinderUtils.findMethod_returningText(
                cls,
                validateName,
                new Class<?>[]{paramType});
        if (validateMethod == null) {
            return;
        }

        processParameterContext.removeMethod(validateMethod);

        final TranslationService translationService = getMetaModelContext().getTranslationService();
        // sadness: same as in TranslationFactory
        final String translationContext = facetHolder.getIdentifier().toFullIdentityString();
        final Facet facet = new ActionParameterValidationFacetViaMethod(validateMethod, translationService, translationContext, facetHolder);
        super.addFacet(facet);
    }


}
