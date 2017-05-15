package org.apache.isis.viewer.wicket.ui.panels;

import java.util.List;

import com.google.common.collect.Lists;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptContentHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ActionPrompt;
import org.apache.isis.viewer.wicket.model.models.ActionPromptProvider;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.FormExecutorContext;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarModelSubscriber2;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract2;
import org.apache.isis.viewer.wicket.ui.components.widgets.formcomponent.FormFeedbackPanel;
import org.apache.isis.viewer.wicket.ui.errors.JGrowlBehaviour;
import org.apache.isis.viewer.wicket.ui.errors.JGrowlUtil;
import org.apache.isis.viewer.wicket.ui.pages.PageAbstract;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;

public abstract class PromptFormAbstract<T extends IModel<ObjectAdapter> & FormExecutorContext>
        extends FormAbstract<ObjectAdapter>
        implements ScalarModelSubscriber2 {

    private static final String ID_OK_BUTTON = "okButton";
    private static final String ID_CANCEL_BUTTON = "cancelButton";

    private static final String ID_FEEDBACK = "feedback";

    protected final List<ScalarPanelAbstract2> paramPanels = Lists.newArrayList();

    private final Component parentPanel;
    private final WicketViewerSettings settings;
    private final T formExecutorContext;

    private final AjaxButton okButton;
    private final AjaxButton cancelButton;

    public PromptFormAbstract(
            final String id,
            final Component parentPanel,
            final WicketViewerSettings settings,
            final T model) {
        super(id, model);
        this.parentPanel = parentPanel;
        this.settings = settings;
        this.formExecutorContext = model;

        setOutputMarkupId(true); // for ajax button
        addParameters();

        FormFeedbackPanel formFeedback = new FormFeedbackPanel(ID_FEEDBACK);
        addOrReplace(formFeedback);

        okButton = addOkButton();
        cancelButton = addCancelButton();
        configureButtons(okButton, cancelButton);
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        response.render(OnDomReadyHeaderItem.forScript(
                String.format("Wicket.Event.publish(Isis.Topic.FOCUS_FIRST_PARAMETER, '%s')", getMarkupId())));

    }

    protected abstract void addParameters();

    protected AjaxButton addOkButton() {

        final IModel<ObjectAdapter> model = getModel();

        AjaxButton okButton = settings.isUseIndicatorForFormSubmit()
                ? new IndicatingAjaxButton(ID_OK_BUTTON, new ResourceModel("okLabel")) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                final ActionPrompt actionPromptIfAny = ActionPromptProvider.Util.getFrom(parentPanel)
                        .getActionPrompt();
                if (actionPromptIfAny != null) {
                    actionPromptIfAny.closePrompt(target);
                }

                final ObjectAdapter targetAdapter = model.getObject();

                onSubmitOf(target, form, this);

                IsisContext.getSessionFactory().getCurrentSession().getPersistenceSession().flush();
                IsisContext.getSessionFactory().getCurrentSession().getPersistenceSession().getPersistenceManager()
                        .flush();
                // update target, since version updated (concurrency checks)
                final Page page = target.getPage();
                if (page instanceof EntityPage) {
                    final EntityPage entityPage = (EntityPage) page;
                    final EntityModel pageModel = entityPage.getUiHintContainerIfAny();
                    pageModel.setObject(targetAdapter);

                    page.visitChildren(new IVisitor<Component, Object>() {
                        @Override
                        public void component(
                                final Component component,
                                final IVisit<Object> visit) {

                            final IModel<?> componentModel = component.getDefaultModel();
                            if (componentModel instanceof ScalarModel) {
                                final ScalarModel scalarModel = (ScalarModel) componentModel;
                                scalarModel.reset();
                            }
                        }
                    });
                }
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                if (settings.isPreventDoubleClickForFormSubmit()) {
                    PanelUtil.disableBeforeReenableOnComplete(attributes, this);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                super.onError(target, form);
                target.add(form);
            }
        }
                : new AjaxButton(ID_OK_BUTTON, new ResourceModel("okLabel")) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                onSubmitOf(target, form, this);
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                if (settings.isPreventDoubleClickForFormSubmit()) {
                    PanelUtil.disableBeforeReenableOnComplete(attributes, this);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                super.onError(target, form);
                target.add(form);
            }
        };
        okButton.add(new JGrowlBehaviour());
        setDefaultButton(okButton);
        add(okButton);
        return okButton;
    }

    protected AjaxButton addCancelButton() {
        final AjaxButton cancelButton = new AjaxButton(ID_CANCEL_BUTTON, new ResourceModel("cancelLabel")) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit(final AjaxRequestTarget target, Form<?> form) {
                final ActionPrompt actionPromptIfAny = ActionPromptProvider.Util.getFrom(parentPanel)
                        .getActionPrompt();
                if (actionPromptIfAny != null) {
                    actionPromptIfAny.closePrompt(target);
                }

                onCancel(target, this);

            }
        };
        // so can submit with invalid content (eg mandatory params missing)
        cancelButton.setDefaultFormProcessing(false);

        add(cancelButton);

        return cancelButton;
    }

    protected void configureButtons(final AjaxButton okButton, final AjaxButton cancelButton) {

        if (formExecutorContext.getPromptStyle() == PromptStyle.INLINE) {
            cancelButton.add(new AbstractDefaultAjaxBehavior() {

                private static final String PRE_JS =
                        "" + "$(document).ready( function() { \n"
                                + "  $(document).bind('keyup', function(evt) { \n"
                                + "    if (evt.keyCode == 27) { \n";
                private static final String POST_JS =
                        "" + "      evt.preventDefault(); \n   "
                                + "    } \n"
                                + "  }); \n"
                                + "});";

                @Override
                public void renderHead(final Component component, final IHeaderResponse response) {
                    super.renderHead(component, response);

                    final String javascript = PRE_JS + getCallbackScript() + POST_JS;
                    response.render(
                            JavaScriptContentHeaderItem.forScript(javascript, null, null));
                }

                @Override
                protected void respond(final AjaxRequestTarget target) {
                    onCancel(target, cancelButton);
                }

            });
        }
    }

    private EntityModel getPageUiHintContainerIfAny() {
        Page page = getPage();
        if (page instanceof EntityPage) {
            EntityPage entityPage = (EntityPage) page;
            return entityPage.getUiHintContainerIfAny();
        }
        return null;
    }

    private void onSubmitOf(
            final AjaxRequestTarget target,
            final Form<?> form,
            final AjaxButton okButton) {

        setLastFocusHint(target, okButton);

        final PromptStyle promptStyle = formExecutorContext.getPromptStyle();
        boolean succeeded = formExecutorContext.getFormExecutor()
                .executeAndProcessResults(target, form, promptStyle);
        if (succeeded) {
            if (promptStyle == PromptStyle.DIALOG) {
                // the Wicket ajax callbacks will have just started to hide the veil
                // we now show it once more, so that a veil continues to be shown until the
                // new page is rendered.
                target.appendJavaScript("isisShowVeil();\n");
            }

            okButton.send(getPage(), Broadcast.EXACT, newCompletedEvent(target, form));

        } else {

            final StringBuilder builder = new StringBuilder();

            // ensure any jGrowl errors are shown
            // (normally would be flushed when traverse to next page).
            String errorMessagesIfAny = JGrowlUtil.asJGrowlCalls(getAuthenticationSession().getMessageBroker());
            builder.append(errorMessagesIfAny);

            // append the JS to the response.
            String buf = builder.toString();
            target.appendJavaScript(buf);
        }

        rebuildGuiAfterInlinePromptDoneIfNec(target);

        target.add(form);

    }

    private void setLastFocusHint(final AjaxRequestTarget target, final AjaxButton ajaxButton) {

        //            String lastFocusedElementId = target.getLastFocusedElementId();
        //            System.out.println("onSubmitOf, lastFocusedElementId = " + lastFocusedElementId);
        //            System.out.println("onSubmitOf, ajaxButton.getPath() = " + ajaxButton.getPath());

        final EntityModel entityModel = getPageUiHintContainerIfAny();
        if (entityModel == null) {
            return;
        }
        ObjectAdapterMemento oam = entityModel.getObjectAdapterMemento();
        if (oam == null) {
            return;
        }
        Component parentPanel = this.parentPanel;
        MarkupContainer parent = parentPanel.getParent();
        if (parent != null) {
            entityModel.setHint(getPage(), PageAbstract.UIHINT_FOCUS, parent.getPageRelativePath());
        }
    }

    protected abstract Object newCompletedEvent(
            final AjaxRequestTarget target,
            final Form<?> form);

    @Override
    public void onError(AjaxRequestTarget target, ScalarPanelAbstract2 scalarPanel) {
        if (scalarPanel != null) {
            // ensure that any feedback error associated with the providing component is shown.
            target.add(scalarPanel);
        }
    }

    public void onCancel(
            final AjaxRequestTarget target,
            final AjaxButton cancelButton) {

        setLastFocusHint(target, cancelButton);

        rebuildGuiAfterInlinePromptDoneIfNec(target);

    }

    private void rebuildGuiAfterInlinePromptDoneIfNec(final AjaxRequestTarget target) {
        final PromptStyle promptStyle = formExecutorContext.getPromptStyle();
        if (promptStyle == PromptStyle.INLINE &&
                formExecutorContext.getInlinePromptContext() != null) {

            formExecutorContext.reset();

            rebuildGuiAfterInlinePromptDone(target);
        }
    }

    private void rebuildGuiAfterInlinePromptDone(final AjaxRequestTarget target) {
        // replace
        final String id = parentPanel.getId();
        final MarkupContainer parent = parentPanel.getParent();

        final WebMarkupContainer replacementPropertyEditFormPanel = new WebMarkupContainer(id);
        replacementPropertyEditFormPanel.setVisible(false);

        parent.addOrReplace(replacementPropertyEditFormPanel);

        // change visibility of inline components
        formExecutorContext.getInlinePromptContext().onCancel();

        // redraw
        MarkupContainer scalarTypeContainer = formExecutorContext.getInlinePromptContext()
                .getScalarTypeContainer();

        if (scalarTypeContainer != null) {
            String markupId = scalarTypeContainer.getMarkupId();
            target.appendJavaScript(
                    String.format("Wicket.Event.publish(Isis.Topic.FOCUS_FIRST_PROPERTY, '%s')",
                            markupId));
        }

        target.add(parent);
    }

    private AjaxButton defaultSubmittingComponent() {
        return okButton;
    }

    // workaround for https://issues.apache.org/jira/browse/WICKET-6364
    @Override
    protected void appendDefaultButtonField() {
        AppendingStringBuffer buffer = new AppendingStringBuffer();
        buffer.append(
                "<div style=\"width:0px;height:0px;position:absolute;left:-100px;top:-100px;overflow:hidden\">");
        buffer.append("<input type=\"text\" tabindex=\"-1\" autocomplete=\"off\"/>");
        Component submittingComponent = (Component) this.defaultSubmittingComponent();
        buffer.append("<input type=\"submit\" tabindex=\"-1\" name=\"");
        buffer.append(this.defaultSubmittingComponent().getInputName());
        buffer.append("\" onclick=\" var b=document.getElementById(\'");
        buffer.append(submittingComponent.getMarkupId());
        buffer.append(
                "\'); if (b!=null&amp;&amp;b.onclick!=null&amp;&amp;typeof(b.onclick) != \'undefined\') {  var r = Wicket.bind(b.onclick, b)(); if (r != false) b.click(); } else { b.click(); };  return false;\" ");
        buffer.append(" />");
        buffer.append("</div>");
        this.getResponse().write(buffer);
    }
}
