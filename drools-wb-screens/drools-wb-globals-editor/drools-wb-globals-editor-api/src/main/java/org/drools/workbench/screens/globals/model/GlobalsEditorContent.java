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

package org.drools.workbench.screens.globals.model;

import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.uberfire.commons.validation.PortablePreconditions;

@Portable
public class GlobalsEditorContent {

    private GlobalsModel model;
    private List<String> fullyQualifiedClassNames;

    public GlobalsEditorContent() {
    }

    public GlobalsEditorContent( final GlobalsModel model,
                                 final List<String> fullyQualifiedClassNames ) {
        this.model = PortablePreconditions.checkNotNull( "model",
                                                         model );
        this.fullyQualifiedClassNames = PortablePreconditions.checkNotNull( "fullyQualifiedClassNames",
                                                                            fullyQualifiedClassNames );
    }

    public GlobalsModel getModel() {
        return this.model;
    }

    public List<String> getFullyQualifiedClassNames() {
        return this.fullyQualifiedClassNames;
    }

}
