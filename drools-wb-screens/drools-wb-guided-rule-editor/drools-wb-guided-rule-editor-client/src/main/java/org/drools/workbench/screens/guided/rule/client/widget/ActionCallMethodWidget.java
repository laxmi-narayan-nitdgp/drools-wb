/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.workbench.screens.guided.rule.client.widget;

import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import org.drools.workbench.models.datamodel.oracle.DropDownData;
import org.drools.workbench.models.datamodel.oracle.MethodInfo;
import org.drools.workbench.models.datamodel.rule.ActionCallMethod;
import org.drools.workbench.models.datamodel.rule.ActionFieldFunction;
import org.drools.workbench.models.datamodel.rule.ActionInsertFact;
import org.drools.workbench.models.datamodel.rule.FactPattern;
import org.drools.workbench.models.datamodel.rule.SingleFieldConstraint;
import org.drools.workbench.screens.guided.rule.client.editor.MethodParameterValueEditor;
import org.drools.workbench.screens.guided.rule.client.editor.RuleModeller;
import org.drools.workbench.screens.guided.rule.client.resources.GuidedRuleEditorResources;
import org.drools.workbench.screens.guided.rule.client.resources.images.GuidedRuleEditorImages508;
import org.drools.workbench.screens.guided.rule.client.util.FieldNatureUtil;
import org.kie.workbench.common.widgets.client.datamodel.AsyncPackageDataModelOracle;
import org.kie.workbench.common.widgets.client.resources.HumanReadable;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.client.common.DirtyableFlexTable;
import org.uberfire.client.common.FormStylePopup;
import org.uberfire.client.common.SmallLabel;

/**
 * This widget is for modifying facts bound to a variable.
 */
public class ActionCallMethodWidget extends RuleModellerWidget {

    final private ActionCallMethod model;
    final private DirtyableFlexTable layout = new DirtyableFlexTable();
    private final AsyncPackageDataModelOracle oracle;
    private boolean isBoundFact = false;

    private String[] fieldCompletionTexts;
    private String[] fieldCompletionValues;
    private String variableClass;

    private boolean readOnly;

    private boolean isFactTypeKnown = false;
    private static final String READ_ONLY_STYLE_NAME = "editor-disabled-widget";

    public ActionCallMethodWidget( final RuleModeller mod,
                                   final EventBus eventBus,
                                   final ActionCallMethod actionCallMethod,
                                   final Boolean readOnly ) {
        super( mod,
               eventBus );
        model = actionCallMethod;
        oracle = this.getModeller().getDataModelOracle();

        layout.setStyleName( "model-builderInner-Background" ); // NON-NLS

        getMethodInfos();
        checkIfReadOnly(readOnly);

        initWidget( layout );
    }

    private void getMethodInfos() {
        if ( oracle.isGlobalVariable( model.getVariable() ) ) {
            getMethodInfosForGlobalVariable();
        } else {
            getMethodInfosForBasicFactType();
        }
    }

    private void checkIfReadOnly(Boolean readOnly) {
        if (readOnly == null) {
            this.readOnly = !this.isFactTypeKnown;
        } else {
            this.readOnly = readOnly;
        }

        if (this.readOnly) {
            layout.addStyleName(READ_ONLY_STYLE_NAME);
        } else {
            layout.removeStyleName(READ_ONLY_STYLE_NAME);
        }

        doLayout();
    }

    private void getMethodInfosForBasicFactType() {

        String factType = getFactTypeLHS();

        if (factType == null) {
            factType = getFactTypeFromRHS(factType);
        }

        if (factType == null) {
            factType = getFactTypeFromLHSField();
        }

        if (factType != null) {
            setMethodInfos(factType);
        } else {
            checkIfReadOnly(null);
        }
    }

    private String getFactTypeFromLHSField() {
        SingleFieldConstraint lhsBoundField = modeller.getModel().getLHSBoundField(model.getVariable());
        if (lhsBoundField != null) {
            return oracle.getFieldClassName(lhsBoundField.getFactType(), lhsBoundField.getFieldName());
        } else {
            return null;
        }
    }

    private String getFactTypeFromRHS(String factType) {
        ActionInsertFact rhsBoundFact = modeller.getModel().getRHSBoundFact(model.getVariable());
        if (rhsBoundFact != null) {
            factType = rhsBoundFact.getFactType();
        }
        return factType;
    }

    private String getFactTypeLHS() {

        FactPattern lhsBoundFact = modeller.getModel().getLHSBoundFact(model.getVariable());
        if (lhsBoundFact != null) {
            return lhsBoundFact.getFactType();
        }

        return null;
    }

    private void setMethodInfos(final String factType) {
        oracle.getMethodInfos(factType,
                               new Callback<List<MethodInfo>>() {
                                   @Override
                                   public void callback( final List<MethodInfo> methodInfos ) {

                                       checkIfFactTypeKnown(methodInfos);
                                       checkIfReadOnly(null);

                                       setMethodInfos(methodInfos, factType);

                                   }
                               } );
    }

    private void checkIfFactTypeKnown(List<MethodInfo> methodInfos) {
        if (methodInfos != null) {
            isFactTypeKnown = true;
        }
        if (!isFactTypeKnown) {
            this.isFactTypeKnown = oracle.isFactTypeRecognized(this.variableClass);
        }
        fireEvent(new FactTypeKnownValueChangeEvent());
    }

    private void setMethodInfos(List<MethodInfo> methodInfos, String factType) {
        this.fieldCompletionTexts = new String[ methodInfos.size() ];
        this.fieldCompletionValues = new String[ methodInfos.size() ];
        int i = 0;
        for ( MethodInfo methodInfo : methodInfos ) {
            this.fieldCompletionTexts[ i ] = methodInfo.getName();
            this.fieldCompletionValues[ i ] = methodInfo.getNameWithParameters();
            i++;
        }
        this.variableClass = factType;
        this.isBoundFact = true;
    }

    private void getMethodInfosForGlobalVariable() {
        oracle.getMethodInfosForGlobalVariable( model.getVariable(),
                                                new Callback<List<MethodInfo>>() {
                                                    @Override
                                                    public void callback( final List<MethodInfo> infos ) {
                                                        if ( infos != null ) {
                                                            ActionCallMethodWidget.this.fieldCompletionTexts = new String[ infos.size() ];
                                                            ActionCallMethodWidget.this.fieldCompletionValues = new String[ infos.size() ];
                                                            int i = 0;
                                                            for ( MethodInfo info : infos ) {
                                                                ActionCallMethodWidget.this.fieldCompletionTexts[ i ] = info.getName();
                                                                ActionCallMethodWidget.this.fieldCompletionValues[ i ] = info.getNameWithParameters();
                                                                i++;
                                                            }

                                                            ActionCallMethodWidget.this.variableClass = oracle.getGlobalVariable( model.getVariable() );

                                                        } else {
                                                            ActionCallMethodWidget.this.fieldCompletionTexts = new String[ 0 ];
                                                            ActionCallMethodWidget.this.fieldCompletionValues = new String[ 0 ];
                                                            checkIfReadOnly(true);
                                                        }
                                                    }
                                                } );
    }

    private void doLayout() {
        layout.clear();
        layout.setWidget( 0,
                          0,
                          getSetterLabel() );
        DirtyableFlexTable inner = new DirtyableFlexTable();
        for ( int i = 0; i < model.getFieldValues().length; i++ ) {
            ActionFieldFunction val = model.getFieldValue( i );

            inner.setWidget( i,
                             0,
                             fieldSelector( val ) );
            inner.setWidget( i,
                             1,
                             valueEditor( val ) );

        }
        layout.setWidget( 0,
                          1,
                          inner );
    }

    private Widget getSetterLabel() {
        HorizontalPanel horiz = new HorizontalPanel();

        if ( model.getState() == ActionCallMethod.TYPE_UNDEFINED ) {
            Image edit = GuidedRuleEditorImages508.INSTANCE.AddFieldToFact();
            edit.setAltText( GuidedRuleEditorResources.CONSTANTS.AddAnotherFieldToThisSoYouCanSetItsValue() );
            edit.setTitle( GuidedRuleEditorResources.CONSTANTS.AddAnotherFieldToThisSoYouCanSetItsValue() );
            edit.addClickHandler( new ClickHandler() {

                public void onClick( ClickEvent event ) {
                    Widget w = (Widget) event.getSource();
                    showAddFieldPopup( w );

                }
            } );
            horiz.add( new SmallLabel( HumanReadable.getActionDisplayName( "call" ) + " [" + model.getVariable() + "]" ) ); // NON-NLS
            if ( !this.readOnly ) {
                horiz.add( edit );
            }

        } else {
            horiz.add( new SmallLabel( HumanReadable.getActionDisplayName( "call" ) + " [" + model.getVariable() + "." + model.getMethodName() + "]" ) ); // NON-NLS
        }

        return horiz;
    }

    protected void showAddFieldPopup( Widget w ) {

        final AsyncPackageDataModelOracle oracle = this.getModeller().getDataModelOracle();

        final FormStylePopup popup = new FormStylePopup( GuidedRuleEditorImages508.INSTANCE.Wizard(),
                                                         GuidedRuleEditorResources.CONSTANTS.ChooseAMethodToInvoke() );
        final ListBox box = new ListBox();
        box.addItem( "..." );

        for ( int i = 0; i < fieldCompletionTexts.length; i++ ) {
            box.addItem( fieldCompletionValues[ i ],
                         fieldCompletionTexts[ i ] );
        }

        box.setSelectedIndex( 0 );

        popup.addAttribute( GuidedRuleEditorResources.CONSTANTS.ChooseAMethodToInvoke(),
                            box );
        box.addChangeHandler( new ChangeHandler() {

            public void onChange( ChangeEvent event ) {

                final String methodName = box.getValue( box.getSelectedIndex() );
                final String methodNameWithParams = box.getItemText( box.getSelectedIndex() );

                model.setMethodName( methodName );
                model.setState( ActionCallMethod.TYPE_DEFINED );

                oracle.getMethodParams( variableClass,
                                        methodNameWithParams,
                                        new Callback<List<String>>() {
                                            @Override
                                            public void callback( final List<String> methodParameters ) {
                                                int i = 0;
                                                for ( String methodParameter : methodParameters ) {
                                                    model.addFieldValue( new ActionFieldFunction( methodName,
                                                                                                  null,
                                                                                                  methodParameter ) );
                                                    i++;
                                                }
                                            }
                                        } );

                getModeller().refreshWidget();
                popup.hide();
            }
        } );
        popup.setPopupPosition( w.getAbsoluteLeft(),
                                w.getAbsoluteTop() );
        popup.show();

    }

    private Widget valueEditor( final ActionFieldFunction val ) {

        AsyncPackageDataModelOracle oracle = this.getModeller().getDataModelOracle();

        String type = "";
        if ( oracle.isGlobalVariable( this.model.getVariable() ) ) {
            type = oracle.getGlobalVariable( this.model.getVariable() );
        } else {
            type = this.getModeller().getModel().getLHSBindingType( this.model.getVariable() );
            if ( type == null ) {
                type = this.getModeller().getModel().getRHSBoundFact( this.model.getVariable() ).getFactType();
            }
        }

        DropDownData enums = oracle.getEnums( type,
                                              val.getField(),
                                              FieldNatureUtil.toMap( this.model.getFieldValues() ) );

        return new MethodParameterValueEditor( val,
                                               enums,
                                               this.getModeller(),
                                               val.getType(),
                                               new Command() {

                                                   public void execute() {
                                                       setModified( true );
                                                   }
                                               } );
    }

    private Widget fieldSelector( final ActionFieldFunction val ) {
        return new SmallLabel( val.getType() );
    }

    /**
     * This returns true if the values being set are on a fact.
     */
    public boolean isBoundFact() {
        return isBoundFact;
    }

    public boolean isDirty() {
        return layout.hasDirty();
    }

    @Override
    public boolean isReadOnly() {
        return this.readOnly;
    }

    @Override
    public boolean isFactTypeKnown() {
        return this.isFactTypeKnown;
    }

}
