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


package org.apache.isis.extensions.dnd.tree;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.extensions.dnd.view.Axes;
import org.apache.isis.extensions.dnd.view.Content;
import org.apache.isis.extensions.dnd.view.SubviewDecorator;
import org.apache.isis.extensions.dnd.view.View;
import org.apache.isis.extensions.dnd.view.ViewRequirement;
import org.apache.isis.extensions.dnd.view.base.Layout;
import org.apache.isis.extensions.dnd.view.border.SelectObjectBorder;
import org.apache.isis.extensions.dnd.view.composite.ObjectFieldBuilder;
import org.apache.isis.extensions.dnd.view.composite.StackLayout;


/**
 * Specification for a tree node that will display an open object as a root node or within an object.
 * 
 * @see org.apache.isis.extensions.dnd.tree.ClosedObjectNodeSpecification for displaying a closed collection
 *      within an object.
 */
public class OpenObjectNodeSpecification extends CompositeNodeSpecification {
    private SubviewDecorator decorator = new SelectObjectBorder.Factory();

    public OpenObjectNodeSpecification() {
        builder = new ObjectFieldBuilder(this);
    }
    
    public Layout createLayout(Content content, Axes axes) {
        return new StackLayout();
    }

    /**
     * This is only used to control root nodes. Therefore a object tree can only be displayed for an object
     * with fields that are collections.
     */
    public boolean canDisplay(ViewRequirement requirement) {
        if (requirement.isObject() && requirement.hasReference()) {
            final ObjectAdapter object = requirement.getAdapter();
            final ObjectAssociation[] fields = object.getSpecification().getAssociations(
                    ObjectAssociationFilters.dynamicallyVisible(IsisContext.getAuthenticationSession(), object));
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].isOneToManyAssociation()) {
                    return true;
                }
            }
        }

        return false;
    }
    
    protected View createNodeView(Content content, Axes axes) {
        return decorator.decorate(axes, super.createNodeView(content, axes));
    }

    @Override
    public int canOpen(final Content content) {
        return CAN_OPEN;
    }

    @Override
    public boolean isOpen() {
        return true;
    }
    
    @Override
    public boolean isSubView() {
        return false;
    }

    public String getName() {
        return "Object tree node - open";
    }
}
