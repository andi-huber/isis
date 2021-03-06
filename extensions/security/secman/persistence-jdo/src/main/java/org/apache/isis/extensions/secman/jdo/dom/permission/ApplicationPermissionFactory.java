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
package org.apache.isis.extensions.secman.jdo.dom.permission;


import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.factory.FactoryService;

/**
 * Optional hook for alternative implementations of {@link org.apache.isis.extensions.secman.jdo.dom.permission.ApplicationPermission}.
 *
 * <p>
 *     To use, simply implement the interface and annotate that implementation with {@link org.apache.isis.applib.annotation.DomainService},
 *     for example:
 * </p>
 * <pre>
 *     &#64;DomainService
 *     public class MyApplicationPermissionFactory implements ApplicationPermissionFactory {
 *         public ApplicationPermission newApplicationPermission() {
 *             return container.newTransientInstance(MyApplicationPermission.class);
 *         }
 *
 *         &#64;Inject
 *         RepositoryService repository;
 *     }
 * </pre>
 * <p>
 *     where:
 * </p>
 * <pre>
 *     public class MyApplicationPermission extends ApplicationPermission { ... }
 * </pre>
 */
public interface ApplicationPermissionFactory {

    public ApplicationPermission newApplicationPermission();

    @Service
    @Named("isisExtSecman.ApplicationPermissionFactory.Default")
    @Order(OrderPrecedence.MIDPOINT)
    @Primary
    @Qualifier("Default")
    public static class Default implements ApplicationPermissionFactory {

        @Inject FactoryService factory;
        
        @Override
        public ApplicationPermission newApplicationPermission() {
            return factory.detachedEntity(ApplicationPermission.class);
        }

    }

}
