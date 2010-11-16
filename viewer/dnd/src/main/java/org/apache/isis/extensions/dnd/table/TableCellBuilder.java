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


    package org.apache.isis.extensions.dnd.table;

import org.apache.log4j.Logger;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.exceptions.UnexpectedCallException;
import org.apache.isis.core.commons.exceptions.UnknownTypeException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.progmodel.facets.value.BooleanValueFacet;
import org.apache.isis.core.progmodel.facets.value.ImageValueFacet;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.extensions.dnd.field.CheckboxField;
import org.apache.isis.extensions.dnd.view.Axes;
import org.apache.isis.extensions.dnd.view.Content;
import org.apache.isis.extensions.dnd.view.GlobalViewFactory;
import org.apache.isis.extensions.dnd.view.ObjectContent;
import org.apache.isis.extensions.dnd.view.Toolkit;
import org.apache.isis.extensions.dnd.view.View;
import org.apache.isis.extensions.dnd.view.ViewRequirement;
import org.apache.isis.extensions.dnd.view.ViewSpecification;
import org.apache.isis.extensions.dnd.view.base.BlankView;
import org.apache.isis.extensions.dnd.view.base.FieldErrorView;
import org.apache.isis.extensions.dnd.view.composite.AbstractViewBuilder;
import org.apache.isis.extensions.dnd.view.content.TextParseableContent;
import org.apache.isis.extensions.dnd.view.field.OneToOneFieldImpl;
import org.apache.isis.extensions.dnd.view.field.TextParseableFieldImpl;
import org.apache.isis.extensions.dnd.viewer.basic.UnlinedTextFieldSpecification;


class TableCellBuilder extends AbstractViewBuilder {
    private static final Logger LOG = Logger.getLogger(TableCellBuilder.class);

    private void addField(final View view, final Axes axes, final ObjectAdapter object, final ObjectAssociation field) {
        final ObjectAdapter value = field.get(object);
        View fieldView;
        fieldView = createFieldView(view, axes, object, field, value);
        if (fieldView != null) {
            view.addView(decorateSubview(axes, fieldView));
        } else {
            view.addView(new FieldErrorView("No field for " + value));
        }
    }

    @Override
    public void build(final View view, Axes axes) {
        Assert.assertEquals("ensure the view is complete decorated view", view.getView(), view);

        final Content content = view.getContent();
        final ObjectAdapter object = ((ObjectContent) content).getObject();

        if (view.getSubviews().length == 0) {
            initialBuild(object, view, axes);
        } else {
            updateBuild(object, view, axes);
        }
    }

    private void updateBuild(final ObjectAdapter object, final View view, final Axes axes) {
        final TableAxis viewAxis = (TableAxis) axes.getAxis(TableAxis.class);

        LOG.debug("update view " + view + " for " + object);
        final View[] subviews = view.getSubviews();
        final ObjectSpecification spec = object.getSpecification();
        for (int i = 0; i < subviews.length; i++) {
            final ObjectAssociation field = fieldFromActualSpec(spec, viewAxis.getFieldForColumn(i));
            final View subview = subviews[i];
            final ObjectAdapter value = field.get(object);

            // if the field is parseable then it may have been modified; we need to replace what was
            // typed in with the actual title.
            if (field.getSpecification().isParseable()) {
                final boolean visiblityChange = !field.isVisible(IsisContext.getAuthenticationSession(), object)
                        .isAllowed()
                        ^ (subview instanceof BlankView);
                final ObjectAdapter adapter = subview.getContent().getAdapter();
                final boolean valueChange = value != null && value.getObject() != null
                        && !value.getObject().equals(adapter.getObject());

                if (visiblityChange || valueChange) {
                    final View fieldView = createFieldView(view, axes, object, field, value);
                    view.replaceView(subview, decorateSubview(axes, fieldView));
                }
                subview.refresh();
            } else if (field.isOneToOneAssociation()) {
                final ObjectAdapter existing = ((ObjectContent) subviews[i].getContent()).getObject();
                final boolean changedValue = value != existing;
                if (changedValue) {
                    View fieldView;
                    fieldView = createFieldView(view, axes, object, field, value);
                    if (fieldView != null) {
                        view.replaceView(subview, decorateSubview(axes, fieldView));
                    } else {
                        view.addView(new FieldErrorView("No field for " + value));
                    }
                }
            }
        }
    }

    private ObjectAssociation fieldFromActualSpec(final ObjectSpecification spec, final ObjectAssociation field) {
        final String fieldName = field.getId();
        return spec.getAssociation(fieldName);
    }

    private void initialBuild(final ObjectAdapter object, final View view, final Axes axes) {
        final TableAxis viewAxis = (TableAxis) axes.getAxis(TableAxis.class);
        LOG.debug("build view " + view + " for " + object);
        final int len = viewAxis.getColumnCount();
        final ObjectSpecification spec = object.getSpecification();
        for (int f = 0; f < len; f++) {
            if (f > 3) {
                continue;
            }
            final ObjectAssociation field = fieldFromActualSpec(spec, viewAxis.getFieldForColumn(f));
            addField(view, axes, object, field);
        }
    }

    private View createFieldView(
            final View view,
            final Axes axes,
            final ObjectAdapter object,
            final ObjectAssociation field,
            final ObjectAdapter value) {
        if (field == null) {
            throw new NullPointerException();
        }
        final GlobalViewFactory factory = Toolkit.getViewFactory();
        ViewSpecification cellSpec;
        Content content;
        if (field instanceof OneToManyAssociation) {
            throw new UnexpectedCallException("no collections allowed");
        } else if (field instanceof OneToOneAssociation) {

            final ObjectSpecification fieldSpecification = field.getSpecification();
            if (fieldSpecification.isParseable()) {
                content = new TextParseableFieldImpl(object, value, (OneToOneAssociation) field);
                // REVIEW how do we deal with IMAGES?
                if (content.getAdapter() instanceof ImageValueFacet) {
                    return new BlankView(content);
                }

                if (!field.isVisible(IsisContext.getAuthenticationSession(), object).isAllowed()) {
                    return new BlankView(content);
                }
                if (((TextParseableContent) content).getNoLines() > 0) {
                    /*
                     * TODO remove this after introducing constraints into view specs that allow the parent
                     * view to specify what kind of subviews it can deal
                     */

                    if (fieldSpecification.containsFacet(BooleanValueFacet.class)) {
                        cellSpec = new CheckboxField.Specification();
                    } else {
                        cellSpec = new UnlinedTextFieldSpecification();
                    }
                } else {
                    return factory.createView(new ViewRequirement(content, ViewRequirement.CLOSED));
                }
            } else {
                content = new OneToOneFieldImpl(object, value, (OneToOneAssociation) field);
                if (!field.isVisible(IsisContext.getAuthenticationSession(), object).isAllowed()) {
                    return new BlankView(content);
                }
                return factory.createView(new ViewRequirement(content, ViewRequirement.CLOSED | ViewRequirement.SUBVIEW));

            }

        } else {
            throw new UnknownTypeException(field);
        }

        return cellSpec.createView(content, axes, -1);
    }

}
