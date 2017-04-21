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

package org.apache.isis.viewer.wicket.ui.components.scalars.isisapplib;

import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.isis.applib.value.DateTime;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelTextFieldDatePickerAbstract;
import org.apache.isis.viewer.wicket.ui.components.scalars.TextFieldValueModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.datepicker.TextFieldWithDateTimePicker;

/**
 * Panel for rendering scalars of type {@link DateTime Isis' applib.DateTime}.
 */
public class IsisDateTimePanel extends ScalarPanelTextFieldDatePickerAbstract<org.apache.isis.applib.value.DateTime> {

    private static final long serialVersionUID = 1L;

    public IsisDateTimePanel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel, org.apache.isis.applib.value.DateTime.class); 
        init(new DateConverterForApplibDateTime(getSettings(), getAdjustBy()));
    }

    protected AbstractTextComponent<DateTime> createTextFieldForRegular(final String id) {
        final TextFieldValueModel<DateTime> textFieldValueModel = new TextFieldValueModel<>(this);
        return new TextFieldWithDateTimePicker<>(id, textFieldValueModel, cls, converter);
    }

    @Override
    protected IModel<String> getScalarPanelType() {
        return Model.of("isisDateTimePanel");
    }
}
