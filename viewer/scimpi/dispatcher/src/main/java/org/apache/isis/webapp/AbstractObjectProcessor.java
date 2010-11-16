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


package org.apache.isis.webapp;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.webapp.processor.Request;

public abstract class AbstractObjectProcessor extends AbstractElementProcessor {

    public void process(Request request) {
        String id = request.getOptionalProperty(OBJECT);
        ObjectAdapter object = request.getContext().getMappedObjectOrResult(id);

        String field = request.getOptionalProperty(FIELD);
        if (field != null) {
            ObjectAssociation objectField = object.getSpecification().getAssociation(field);
            String error = checkFieldType(objectField);
            if (error != null) {
                throw new ScimpiException("Field " + objectField.getId() + " " + error);
            }
            IsisContext.getPersistenceSession().resolveField(object, objectField);
            object = objectField.get(object);
        }

        process(request, object);
    }
    
    protected String checkFieldType(ObjectAssociation objectField) {
        return null;
    }
    
    protected abstract void process(Request request, ObjectAdapter object);

}


