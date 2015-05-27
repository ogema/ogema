/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
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
