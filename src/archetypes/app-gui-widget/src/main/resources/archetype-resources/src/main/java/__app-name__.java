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
package ${package};

import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Reference;

import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.tools.widget.api.OgemaOsgiWidgetService;
import org.ogema.tools.widget.api.WidgetApp;
import org.ogema.tools.widget.api.WidgetPage;
import org.ogema.tools.widget.html.form.button.Button;
import org.ogema.tools.widget.html.form.label.Label;
import org.ogema.tools.widget.html.form.slider.Slider;
import org.ogema.tools.widget.html.form.textfield.TextField;
import org.ogema.tools.widget.html.chart.C3DataRow;
import org.ogema.tools.widget.html.chart.c3.LineChart;
import org.ogema.tools.widget.api.constants.TriggeredAction;
import org.ogema.tools.widget.api.constants.TriggeringAction;
import org.ogema.tools.widget.html.complextable.ComplexTable;


@Component(specVersion = "1.2", immediate=true)
@Service(Application.class)
public class ${app-name} implements Application {

	protected OgemaLogger logger;
	protected ApplicationManager appMan;
	protected ResourceManagement resMan;
	protected ResourceAccess resAcc;

        @Reference
        OgemaOsgiWidgetService widgetService;

	@Override
	public void start(ApplicationManager appManager) {
		this.appMan = appManager;
		this.logger = appManager.getLogger();
		this.resMan = appManager.getResourceManagement();
		this.resAcc = appManager.getResourceAccess();

		logger.debug("{} started", getClass().getName());

                String appNameLowerCase = "${app-name}".toLowerCase();

                String webResourcePackage = "${package}".replace(".", "/");
                String webResourceBrowserPath = appManager.getWebAccessManager().registerWebResourcePath("/" , webResourcePackage + "/gui");

                /**
                 * register widgets and create a widget page.
                 */

                WidgetApp wApp = new WidgetApp(appNameLowerCase,webResourceBrowserPath, widgetService,appManager.getWebAccessManager(), logger);
                WidgetPage mainPage = new WidgetPage(wApp);

                /**
                 * Buttons
                 */

                Button button1 = new Button(mainPage, "ogemaButton1") {

                    @Override
                    public String onPOST(String data, HttpServletRequest req) {
                        System.out.println("Widget-Button <" + id
                                + "> was clicked.");
                        return super.onPOST(data, req);
                    }
                };
                button1.setText("I am a blue button");
                button1.setStyle(Button.BOOTSTRAP_BLUE);
                
                Button button2 = new Button(mainPage, "ogemaButton2") {

                    @Override
                    public String onPOST(String data, HttpServletRequest req) {
                        System.out.println("Widget-Button <" + id
                                + "> was clicked.");
                        return super.onPOST(data, req);
                    }
                };
                button2.setText("I am a default button");
                
                Button button3 = new Button(mainPage, "ogemaButton3") {

                    @Override
                    public String onPOST(String data, HttpServletRequest req) {
                        System.out.println("Widget-Button <" + id
                                + "> was clicked.");
                        return super.onPOST(data, req);
                    }
                };
                button3.setText("I am a orange button");
                button3.setStyle(Button.BOOTSTRAP_ORANGE);
                
                /**
                 * Labels
                 */

                Label label1 = new Label(mainPage, "ogemaLabel1");
                label1.setText("I am a green label");
                label1.setStyle(Label.BOOTSTRAP_GREEN);
                
                Label label2 = new Label(mainPage, "ogemaLabel2");
                label2.setText("I am a default label");
                
                Label label3 = new Label(mainPage, "ogemaLabel3");
                label3.setText("I am a red label");
                label3.setStyle(Label.BOOTSTRAP_RED);

                /**
                 * Slider
                 */

                Slider slider = new Slider(mainPage, "ogemaSlider", 1000, 2000, 1500);
                slider.setSendValueOnChange(true);

                /**
                 * TextField and Button
                 */

                TextField textField = new TextField(mainPage, "ogemaTextField", "enter text here and press button") {

                    @Override
                    public String onPOST(String data, HttpServletRequest req) {
                        System.out.println("<ogemaTextField> on post data: " + data);
                        return super.onPOST(data, req);
                    }
                    
                };
                
                Button textFieldButton = new Button(mainPage, "ogemaButtonForTextField", "Here! Click me!");
                textFieldButton.triggerAction("ogemaButtonForTextField", TriggeringAction.ON_CLICK, TriggeredAction.POST_REQUEST);
                
                /**
                 * Chart
                 */

                LineChart lineChart = (LineChart) new LineChart(mainPage, "ogemaChart")
                    .withZoom(true)
                    .add(new C3DataRow("Data 1").withList(Arrays.asList(10, 25, 50,
                                            100)))
                    .add(new C3DataRow("Data 2").withList(
                                    Arrays.asList(65, 35, 20, 99)).withType(
                                    C3DataRow.DataRowType.AREA_LINE))
                    .add(new C3DataRow("Data 3").withList(
                                    Arrays.asList(20, 10, 18, 23, 45, 13)).withType(
                                    C3DataRow.DataRowType.SPLINE))
                    .add(new C3DataRow("Data 4").withList(
                                    Arrays.asList(80, 100, 110, 90)).withType(
                                    C3DataRow.DataRowType.AREA_SPLINE))
                    .add(new C3DataRow("Data 5").withList(
                                    Arrays.asList(90, 30, 58, 70, 90, 73)).withType(
                                    C3DataRow.DataRowType.STEP))
                    .add(new C3DataRow("Data 6").withList(
                                    Arrays.asList(20, 10, 30, 60, 10, 30)).withType(
                                    C3DataRow.DataRowType.AREA_STEP));

                /**
                 * Table
                 */
                                    
                ComplexTable complexTable = new ComplexTable(mainPage, "ogemaTable");
                TableRowTemplate tableRowTemplate = new TableRowTemplate(mainPage, appMan, complexTable);
                complexTable.setRowTemplate(tableRowTemplate);

                complexTable.addRow("Row1");
                complexTable.addRow("Row2");
                complexTable.addRow("Row3");

	}

        @Override
	public void stop(AppStopReason reason) {
		appMan.getWebAccessManager().unregisterWebResourcePath("/");
	}

}