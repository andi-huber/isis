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
package org.apache.isis.core.commons.internal.concurrent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * <h1>- internal use only -</h1>
 * 
 * Provides limited thread-safe access to a list object, allowing add/replace/clear/snapshot.
 * <p>
 * Serializable
 * 
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
public class _ConcurrentListWrapper<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private final Object $lock = new Object[0]; // serializable
    private List<T> list = new ArrayList<>(); // serializable
    
    public List<T> snapshot() {
        synchronized ($lock) {
            return new ArrayList<>(list);    
        }
    }

    public void replace(@Nullable List<T> list) {
        synchronized ($lock) {
            this.list = list!=null 
                    ? new ArrayList<>(list)
                            : new ArrayList<>();
        }
    }
    
    public void clear() {
        synchronized ($lock) {
            this.list = new ArrayList<>(); // allow garbage collection of the old one
        }
    }
    
    public void add(@Nullable T element) {
        if(element==null) {
            return; // noop
        }
        synchronized ($lock) {
            this.list.add(element);
        }
    }

}
