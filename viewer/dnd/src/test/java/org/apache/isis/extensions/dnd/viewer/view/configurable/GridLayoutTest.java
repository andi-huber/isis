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


package org.apache.isis.extensions.dnd.viewer.view.configurable;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.extensions.dnd.drawing.Location;
import org.apache.isis.extensions.dnd.drawing.Size;
import org.apache.isis.extensions.dnd.view.View;
import org.apache.isis.extensions.dnd.view.composite.GridLayout;
import org.apache.isis.runtime.testsystem.TestProxyConfiguration;


public class GridLayoutTest {
    private static final int CONTAINER_HEIGHT = 100;
    private static final int CONTAINER_WIDTH = 200;
    private View container;
    private JUnit4Mockery mockery;
    private int viewIndex = 1;
    private GridLayout layout;

    @Before
    public void setup() {
        IsisContext.setConfiguration(new TestProxyConfiguration());

        mockery = new JUnit4Mockery();

        layout = new GridLayout();
        container = mockery.mock(View.class, "container");
    }

    @After
    public void complete() {
    // mockery.assertIsSatisfied();
    }

    private void createContainer(final View[] views) {
        mockery.checking(new Expectations() {
            {
                one(container).getSubviews();                 will(returnValue(views));
            }
        });
    }

    private View createView(final int width, final int height) {
        final View subview = mockery.mock(View.class, "view" + viewIndex++);
        mockery.checking(new Expectations() {
            {
                exactly(1).of(subview).getRequiredSize(new Size(Integer.MAX_VALUE, Integer.MAX_VALUE));
                will(returnValue(new Size(width, height)));
            }
        });
        return subview;
    }

    @Test
    public void noContentSize() {
        createContainer(new View[0]);
        Assert.assertEquals(new Size(), layout.getRequiredSize(container));
    }

    @Test
    public void sizeEqualToTheOnlySubview() {
        createContainer(new View[] { createView(20, 40) });
        Assert.assertEquals(new Size(20, 40), layout.getRequiredSize(container));
    }

    @Test
    public void positionVertically() {
        final View view1 = createLayoutView(0, 0, 20, 10);
        final View view2 = createLayoutView(0, 10, 20, 10);
        final View view3 = createLayoutView(0, 20, 20, 10);
        final View view4 = createLayoutView(0, 30, 20, 10);
        createContainer(new View[] { view1, view2, view3, view4 });

        layout.layout(container, new Size(CONTAINER_WIDTH, CONTAINER_HEIGHT));
    }

    @Test
    public void positionVerticallyOver2Colums() {
        final View view1 = createLayoutView(0, 0, 20, 10);
        final View view2 = createLayoutView(20, 0, 20, 10);
        final View view3 = createLayoutView(0, 10, 20, 10);
        final View view4 = createLayoutView(20, 10, 20, 10);
        final View view5 = createLayoutView(0, 20, 20, 10);
            createContainer(new View[] { view1, view2, view3, view4, view5 });

        layout.setOrientation(GridLayout.COLUMNS);
        layout.setSize(2);
        layout.layout(container, new Size(CONTAINER_WIDTH, CONTAINER_HEIGHT));
    }

    @Test
    public void positionVerticallyOver3Colums() {
        final View view1 = createLayoutView(0, 0, 20, 10);
        final View view2 = createLayoutView(20, 0, 20, 10);
        final View view3 = createLayoutView(40, 0, 20, 10);
        final View view4 = createLayoutView(0, 10, 20, 10);
        final View view5 = createLayoutView(20, 10, 20, 10);
            createContainer(new View[] { view1, view2, view3, view4, view5 });

        layout.setOrientation(GridLayout.COLUMNS);
        layout.setSize(3);
        layout.layout(container, new Size(CONTAINER_WIDTH, CONTAINER_HEIGHT));
    }

    @Test
    public void positionHorizontallyOver2Rows() {
        final View view1 = createLayoutView(0, 0, 20, 10);
        final View view2 = createLayoutView(0, 10, 20, 10);
        final View view3 = createLayoutView(20, 0, 20, 10);
        final View view4 = createLayoutView(20, 10, 20, 10);
        final View view5 = createLayoutView(40, 0, 20, 10);
        createContainer(new View[] { view1, view2, view3, view4, view5 });

        layout.setOrientation(GridLayout.ROWS);
        layout.setSize(2);
        layout.layout(container, new Size(CONTAINER_WIDTH, CONTAINER_HEIGHT));
    }

    @Test
    public void positionHorizontall() {
        final View view1 = createLayoutView(0, 0, 20, 10);
        final View view2 = createLayoutView(20, 0, 20, 10);
        final View view3 = createLayoutView(40, 0, 20, 10);
        final View view4 = createLayoutView(60, 0, 20, 10);
        createContainer(new View[] { view1, view2, view3, view4 });

        layout.setOrientation(GridLayout.ROWS);
        layout.layout(container, new Size(CONTAINER_WIDTH, CONTAINER_HEIGHT));
    }

    private View createLayoutView(
            final int x,
            final int y,
            final int width,
            final int height) {
        final View view = mockery.mock(View.class, "view" + viewIndex++);
        mockery.checking(new Expectations() {
            {
                exactly(1).of(view).layout();
                exactly(2).of(view).getRequiredSize(new Size(CONTAINER_WIDTH, CONTAINER_HEIGHT));
                will(returnValue(new Size(width, height)));
                exactly(1).of(view).setSize(new Size(width, height));
                exactly(1).of(view).setLocation(new Location(x, y));
            }
        });
        return view;
    }

    @Test
    public void heightSumOfTwoViews() {
        createContainer(new View[] { createView(10, 40), createView(10, 30) });
        Assert.assertEquals(70, layout.getRequiredSize(container).getHeight());
    }

    @Test
    public void widthSumOfTwoViews() {
        layout.setOrientation(GridLayout.ROWS);
        createContainer(new View[] { createView(20, 10), createView(30, 10) });
        Assert.assertEquals(50, layout.getRequiredSize(container).getWidth());
    }
  
    @Test
    public void widthMaxTwoViews() {
        createContainer(new View[] { createView(25, 10), createView(20, 10) });
        Assert.assertEquals(25, layout.getRequiredSize(container).getWidth());
    }

    @Test
    public void heightMaxOfTwoViews() {
        layout.setOrientation(GridLayout.ROWS);
        createContainer(new View[] { createView(10, 40), createView(10, 30) });
        Assert.assertEquals(40, layout.getRequiredSize(container).getHeight());
    }


    @Test
    public void sizeOver2Columns() {
        final View view1 = createView(30, 10);
        final View view2 = createView(20, 10);
        final View view3 = createView(20, 10);
        createContainer(new View[] { view1, view2, view3 });

        layout.setOrientation(GridLayout.COLUMNS);
        layout.setSize(2);
        Assert.assertEquals(new Size(50, 20), layout.getRequiredSize(container));
    }

    @Test
    public void sizeOver3Columns() {
        final View view1 = createView(30, 10);
        final View view2 = createView(20, 10);
        final View view3 = createView(20, 10);
        final View view4 = createView(40, 10);
        createContainer(new View[] { view1, view2, view3, view4 });

        layout.setOrientation(GridLayout.COLUMNS);
        layout.setSize(2);
        Assert.assertEquals(new Size(70, 20), layout.getRequiredSize(container));
    }

    @Test
    public void sizeOver2Rows() {
        final View view1 = createView(20, 10);
        final View view2 = createView(20, 10);
        final View view3 = createView(20, 10);
        createContainer(new View[] { view1, view2, view3 });

        layout.setOrientation(GridLayout.ROWS);
        layout.setSize(2);
        Assert.assertEquals(new Size(40, 20), layout.getRequiredSize(container));
    }

}

