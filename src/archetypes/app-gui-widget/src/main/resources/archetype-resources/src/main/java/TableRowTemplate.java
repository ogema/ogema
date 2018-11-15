/**
 * Copyright 2011-2018 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ${package};

import org.ogema.core.application.ApplicationManager;
import org.ogema.tools.widget.api.WidgetPage;
import org.ogema.tools.widget.html.complextable.ComplexTable;
import org.ogema.tools.widget.html.complextable.RowTemplate;
import org.ogema.tools.widget.html.form.label.Label;
import org.ogema.tools.widget.html.form.button.Button;

/**
 *
 * @author tgries
 */
public class TableRowTemplate extends RowTemplate {

    protected WidgetPage widgetPage;
    protected ApplicationManager appMan;
    protected ComplexTable widgetTable;
    
    public TableRowTemplate(WidgetPage widgetPage, ApplicationManager appMan, ComplexTable widgetTable) {
        super();
        this.widgetPage = widgetPage;
        this.appMan = appMan;
        this.widgetTable = widgetTable;
    }
    
    @Override
    public Row addRow(String lineId) {
        Row row = new Row();
        
        Label label = new Label(widgetPage, "loc_" + lineId);
        label.addStyle(Label.BOOTSTRAP_BLUE);
        label.setText("Label " + lineId);
        row.addCell("colLoc", label);
        
        Button button = new Button(widgetPage, "but_" + lineId);
        button.setText("Button " + lineId);
        row.addCell(button);
        
        return row;
    }
    
}
