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


package org.apache.isis.runtime.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.core.runtime.context.IsisContext;


public abstract class EmbeddedWebServerAbstract implements EmbeddedWebServer {

    @SuppressWarnings("unused")
    private final static Logger LOG = Logger.getLogger(EmbeddedWebServerAbstract.class);

    private List<WebAppSpecification> specifications = new ArrayList<WebAppSpecification>();

    // ///////////////////////////////////////////////////////
    // WebApp Specifications
    // ///////////////////////////////////////////////////////

    /**
     * Must be called prior to {@link #init() initialization}.
     */
    public void addWebAppSpecification(WebAppSpecification specification) {
        specifications.add(specification);
    }

    protected List<WebAppSpecification> getSpecifications() {
        return specifications;
    }

    // ///////////////////////////////////////////////////////
    // init, shutdown
    // ///////////////////////////////////////////////////////

    public void init() {
    // does nothing
    }

    public void shutdown() {
    // does nothing
    }

    // ///////////////////////////////////////////////////////
    // Dependencies (from context)
    // ///////////////////////////////////////////////////////

    protected static IsisConfiguration getConfiguration() {
        return IsisContext.getConfiguration();
    }

}

