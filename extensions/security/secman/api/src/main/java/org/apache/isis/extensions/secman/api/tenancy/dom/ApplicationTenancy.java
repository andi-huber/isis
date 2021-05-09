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
package org.apache.isis.extensions.secman.api.tenancy.dom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;

import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;

/**
 * @since 2.0 {@index}
 */
public interface ApplicationTenancy {

    String NAMED_QUERY_FIND_BY_NAME = "ApplicationTenancy.findByName";
    String NAMED_QUERY_FIND_BY_PATH = "ApplicationTenancy.findByPath";
    String NAMED_QUERY_FIND_BY_NAME_OR_PATH_MATCHING = "ApplicationTenancy.findByNameOrPathMatching";



    // -- DOMAIN EVENTS

    abstract class PropertyDomainEvent<T> extends IsisModuleExtSecmanApi.PropertyDomainEvent<ApplicationTenancy, T> {}
    abstract class CollectionDomainEvent<T> extends IsisModuleExtSecmanApi.CollectionDomainEvent<ApplicationTenancy, T> {}
    abstract class ActionDomainEvent extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationTenancy> {}

    // -- MODEL

    default String title() {
        return getName();
    }


    // -- NAME

    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Name {
        int MAX_LENGTH = 120;
        int TYPICAL_LENGTH = 20;
    }

    @Name
    String getName();
    void setName(String name);


    // -- PATH

    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Path {
        int MAX_LENGTH = 255;
    }

    @Path
    String getPath();
    void setPath(String path);


    // -- PARENT

    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Parent {
    }

    @Parent
    ApplicationTenancy getParent();
    void setParent(ApplicationTenancy parent);


    // -- CHILDREN

    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Children {
    }

    @Children
    Collection<ApplicationTenancy> getChildren();


}
