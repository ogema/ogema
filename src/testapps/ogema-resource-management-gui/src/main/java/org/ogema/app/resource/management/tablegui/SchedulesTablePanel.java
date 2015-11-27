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
package org.ogema.app.resource.management.tablegui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.ogema.app.resource.management.tablegui.util.Util;
import org.ogema.apps.wicket.ApplicationPanel;

/**
 * 
 * @author Christoph Noelle, Fraunhofer IWES
 * 
 */
public class SchedulesTablePanel extends ApplicationPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public List<String> timeFormats = new ArrayList<String>(Util.getInstance().getTimeFormats());

	@Override
	public void initContent() {

		/******************** Display all schedules *************************/

		final Form<Void> displayForm = new Form<Void>("displaySchedulesForm");
		Label timeHeader = new Label("timeHeader", "Time settings");
		displayForm.add(timeHeader);

		Label currentTime = new Label("currentTime", "Current time: ");

		final Model<String> selectedTimeFormatModel = Model.of("milliseconds");
		LoadableDetachableModel<List<String>> timeFormatModel = new LoadableDetachableModel<List<String>>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5654935158589164742L;

			@Override
			protected List<String> load() {
				return Util.getInstance().getTimeFormats();
			}
		};

		final DropDownChoice<String> timeFormatChoice = new DropDownChoice<String>("timeFormatChoice",
				selectedTimeFormatModel, timeFormatModel);
		displayForm.add(timeFormatChoice);
		displayForm.add(currentTime);
		displayForm.add(new FeedbackPanel("feedback"));
		LoadableDetachableModel<String> timeModel = new LoadableDetachableModel<String>() {
			private static final long serialVersionUID = 5654935158589164742L;

			@Override
			protected String load() {
				return Util.getInstance().getCurrentTime(timeFormatChoice.getDefaultModelObjectAsString());
			}
		};
		Label timeField = new Label("timeField", timeModel);
		displayForm.add(timeField);

		Label displayHeader = new Label("displayHeader", "Existing Schedules");
		displayForm.add(displayHeader);
		final WebMarkupContainer informationBox = new WebMarkupContainer("informationBox");
		informationBox.setOutputMarkupPlaceholderTag(true);
		// informationBox.setVisible(false);

		final List<String[]> resourcesList = new ArrayList<String[]>(Util.getInstance().getSchedules(
				timeFormatChoice.getDefaultModelObjectAsString()));
		ListDataProvider<String[]> listDataProvider = new ListDataProvider<String[]>(resourcesList);
		DataView<String[]> dataView = new DataView<String[]>("rows", listDataProvider) {

			private static final long serialVersionUID = 1023457427626025110L;

			@Override
			protected void populateItem(Item<String[]> item) {
				String[] resourcesArr = item.getModelObject();
				RepeatingView repeatingView = new RepeatingView("dataRow");
				for (int i = 0; i < resourcesArr.length; i++) {
					repeatingView.add(new Label(repeatingView.newChildId(), resourcesArr[i]));
				}
				item.add(repeatingView);
			}
		};

		dataView.setOutputMarkupId(true);
		informationBox.add(dataView);
		// informationBox.add(new PagingNavigator("pagingNavigator", dataView));
		displayForm.add(informationBox);

		AjaxButton updateButton = new AjaxButton("updateButton") {

			private static final long serialVersionUID = -1742112424158094767L;

			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				target.add(form);
				// target.add(orderingForm);
				String format = timeFormatChoice.getDefaultModelObjectAsString();
				resourcesList.clear();
				resourcesList.addAll(Util.getInstance().getSchedules(format));
				// informationBox.setVisible(true);
				target.add(displayForm);
			}

		};
		updateButton.add(new Label("updateButtonLabel", "Update"));
		displayForm.add(updateButton);

		/******************* Display single schedule with all entries ***************/

		//		final Form<Void> displayForm = new Form<Void>("displayForm");	

		Label scheduleSelection = new Label("scheduleSelection", "Select Schedule");
		displayForm.add(scheduleSelection);

		final Model<String> selectedScheduleModel = Model.of("");
		LoadableDetachableModel<List<String>> schedulesModel = new LoadableDetachableModel<List<String>>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5654935158589164742L;

			@Override
			protected List<String> load() {
				return Util.getInstance().getSchedulesList();
			}
		};

		final DropDownChoice<String> scheduleChoice = new DropDownChoice<String>("scheduleChoice",
				selectedScheduleModel, schedulesModel);
		displayForm.add(scheduleChoice);

		final WebMarkupContainer scheduleBox = new WebMarkupContainer("scheduleBox");
		scheduleBox.setOutputMarkupPlaceholderTag(true);
		scheduleBox.setVisible(false);

		Label scheduleGeneralPropsHeader = new Label("scheduleGeneralPropsHeader", "General schedule properties");
		scheduleBox.add(scheduleGeneralPropsHeader);
		Label scheduleValuesHeader = new Label("scheduleValuesHeader", "Schedule values");
		scheduleBox.add(scheduleValuesHeader);
		Label schedId = new Label("schedId", "Schedule location");
		Label schedType = new Label("schedType", "Schedule type");
		Label schedIM = new Label("schedIM", "Interpolation mode");
		Label schedActive = new Label("schedActive", "Active?");
		scheduleBox.add(schedId);
		scheduleBox.add(schedType);
		scheduleBox.add(schedIM);
		scheduleBox.add(schedActive);
		LoadableDetachableModel<String> scheduleLoc = new LoadableDetachableModel<String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5654935158589164742L;

			@Override
			protected String load() {
				return scheduleChoice.getDefaultModelObjectAsString();
			}
		};
		Label schedIdVal = new Label("schedIdVal", scheduleLoc);
		LoadableDetachableModel<String> scheduleType = new LoadableDetachableModel<String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5654935158589164742L;

			@Override
			protected String load() {
				return Util.getInstance().getTypeShortName(scheduleChoice.getDefaultModelObjectAsString());
			}
		};
		Label schedTypeVal = new Label("schedTypeVal", scheduleType);

		LoadableDetachableModel<String> selectedIMModel = new LoadableDetachableModel<String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5654935158589164742L;

			@Override
			protected String load() {
				return Util.getInstance().getInterpolationMode(scheduleChoice.getDefaultModelObjectAsString());
			}
		};
		LoadableDetachableModel<List<String>> imModel = new LoadableDetachableModel<List<String>>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5654935158589164742L;

			@Override
			protected List<String> load() {
				return Util.getInstance().getInterpolationModes();
			}
		};
		final DropDownChoice<String> interpolationChoice = new DropDownChoice<String>("interpolationChoice",
				selectedIMModel, imModel);
		interpolationChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			/**
			 * 
			 */
			private static final long serialVersionUID = -5601307612112315336L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				String mode = interpolationChoice.getDefaultModelObjectAsString();
				info(Util.getInstance().setInterpolationMode(scheduleChoice.getDefaultModelObjectAsString(), mode));
				target.add(displayForm);
			}
		});
		LoadableDetachableModel<String> selectedActiveModel = new LoadableDetachableModel<String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5654935158589164742L;

			@Override
			protected String load() {
				return String.valueOf(Util.getInstance().isActive(scheduleChoice.getDefaultModelObjectAsString()));
			}
		};
		LoadableDetachableModel<List<String>> activeModel = new LoadableDetachableModel<List<String>>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5654935158589164742L;

			@Override
			protected List<String> load() {
				List<String> modes = new ArrayList<String>();
				modes.add("true");
				modes.add("false");
				return modes;
			}
		};
		final DropDownChoice<String> activeChoice = new DropDownChoice<String>("activeChoice", selectedActiveModel,
				activeModel);
		activeChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			/**
			 * 
			 */
			private static final long serialVersionUID = -5601307612112315336L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				String active = activeChoice.getDefaultModelObjectAsString();
				info(Util.getInstance().setActive(scheduleChoice.getDefaultModelObjectAsString(), active));
				target.add(displayForm);
			}
		});

		scheduleBox.add(schedIdVal);
		scheduleBox.add(schedTypeVal);
		scheduleBox.add(interpolationChoice);
		scheduleBox.add(activeChoice);

		Label scheduleValueAdderHeader = new Label("scheduleValueAdderHeader", "Add a value");
		scheduleBox.add(scheduleValueAdderHeader);
		Label newTimeLabel = new Label("newTimeLabel",
				"Timestamp (in milliseconds; preset to current time. Below value in human readable format)");
		scheduleBox.add(newTimeLabel);
		Label newValueLabel = new Label("newValueLabel", "Value, compatibel with the schedule type");
		scheduleBox.add(newValueLabel);
		Label newQualityLabel = new Label("newQualityLabel", "Select quality");
		scheduleBox.add(newQualityLabel);
		final TextField<String> newValueField = new TextField<String>("newValueField", Model.of(""));
		scheduleBox.add(newValueField);
		/*LoadableDetachableModel<String> newTimeModel = new LoadableDetachableModel<String>() {
			
			private static final long serialVersionUID = 5654935158589164742L;

			@Override
			protected String load() {
				return util.getCurrentTime("milliseconds");
			}
		}; */
		String time = Util.getInstance().getCurrentTime("milliseconds");
		Model<String> newTimeModel = Model.of(time);
		final TextField<String> newTimeField = new TextField<String>("newTimeField", newTimeModel);
		scheduleBox.add(newTimeField);
		LoadableDetachableModel<String> convertedTimeModel = new LoadableDetachableModel<String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5654935158589164742L;

			@Override
			protected String load() {
				String time = newTimeField.getDefaultModelObjectAsString();
				try {
					long t = Long.parseLong(time);
					time = Util.getInstance().convertToReadableTime(t);
				} catch (Exception e) {
				}
				return time;
			}
		};
		final Label convertedTime = new Label("convertedTime", convertedTimeModel);
		scheduleBox.add(convertedTime);
		newTimeField.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			/**
			 * 
			 */
			private static final long serialVersionUID = -5601307612112315336L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				convertedTime.modelChanged();
				target.add(displayForm);
			}
		});
		AjaxButton addHourButton = new AjaxButton("addHourButton") {

			private static final long serialVersionUID = -1742112424158094767L;

			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				String time = newTimeField.getDefaultModelObjectAsString();
				try {
					long t = Long.parseLong(time);
					t += 60 * 60 * 1000l;
					time = String.valueOf(t);
				} catch (Exception e) {
				}
				newTimeField.setDefaultModelObject(time);
				target.add(form);
			}

		};
		addHourButton.add(new Label("addHourButtonLabel", "Add 1h"));
		scheduleBox.add(addHourButton);
		AjaxButton addDayButton = new AjaxButton("addDayButton") {

			private static final long serialVersionUID = -1742112424158094767L;

			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				String time = newTimeField.getDefaultModelObjectAsString();
				try {
					long t = Long.parseLong(time);
					t += 24 * 60 * 60 * 1000l;
					time = String.valueOf(t);
				} catch (Exception e) {
				}
				newTimeField.setDefaultModelObject(time);
				target.add(form);
			}

		};
		addDayButton.add(new Label("addDayButtonLabel", "Add 1d"));
		scheduleBox.add(addDayButton);
		AjaxButton addFiveMinButton = new AjaxButton("addFiveMinButton") {

			private static final long serialVersionUID = -1742112424158094767L;

			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				String time = newTimeField.getDefaultModelObjectAsString();
				try {
					long t = Long.parseLong(time);
					t += 5 * 60 * 1000l;
					time = String.valueOf(t);
				} catch (Exception e) {
				}
				newTimeField.setDefaultModelObject(time);
				target.add(form);
			}

		};
		addFiveMinButton.add(new Label("addFiveMinButtonLabel", "Add 5min"));
		scheduleBox.add(addFiveMinButton);
		AjaxButton addWeekButton = new AjaxButton("addWeekButton") {

			private static final long serialVersionUID = -1742112424158094767L;

			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				String time = newTimeField.getDefaultModelObjectAsString();
				try {
					long t = Long.parseLong(time);
					t += 7 * 24 * 60 * 60 * 1000l;
					time = String.valueOf(t);
				} catch (Exception e) {
				}
				newTimeField.setDefaultModelObject(time);
				target.add(form);
			}

		};
		addWeekButton.add(new Label("addWeekButtonLabel", "Add 1 week"));
		scheduleBox.add(addWeekButton);

		LoadableDetachableModel<List<String>> qualityModel = new LoadableDetachableModel<List<String>>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5654935158589164742L;

			@Override
			protected List<String> load() {
				List<String> modes = new ArrayList<String>();
				modes.add("GOOD");
				modes.add("BAD");
				return modes;
			}
		};
		final DropDownChoice<String> newQualityField = new DropDownChoice<String>("newQualityField", Model.of("GOOD"),
				qualityModel);
		scheduleBox.add(newQualityField);

		Label deleteValuesHeader = new Label("deleteValuesHeader", "Delete values");
		scheduleBox.add(deleteValuesHeader);
		final Label deleteStartTime = new Label("deleteStartTime",
				"Start time (in milliseconds; preset to current time)");
		scheduleBox.add(deleteStartTime);
		Label deleteEndTime = new Label("deleteEndTime", "End time (in milliseconds; preset to current time + 5min)");
		scheduleBox.add(deleteEndTime);
		Label deleteAffectedValues = new Label("deleteAffectedValues", "Number of affected values");
		scheduleBox.add(deleteAffectedValues);
		time = Util.getInstance().getCurrentTime("milliseconds");
		Model<String> delTimeModel = Model.of(time);
		final TextField<String> deleteStartTimeField = new TextField<String>("deleteStartTimeField", delTimeModel);
		scheduleBox.add(deleteStartTimeField);
		/*LoadableDetachableModel<String> currentTimePlusFiveModel = new LoadableDetachableModel<String>() {

			private static final long serialVersionUID = 5654935158589164742L;

			@Override
			protected String load() {
				String time = util.getCurrentTime("milliseconds");
				try {
					long t = Long.parseLong(time);
					t += 24*60*60*1000;
					time = String.valueOf(t);
				} catch (Exception e) {}
				return time;
			}
		}; */
		time = Util.getInstance().getCurrentTime("milliseconds");
		try {
			long t = Long.parseLong(time);
			t += 5 * 60 * 1000;
			time = String.valueOf(t);
		} catch (Exception e) {
		}
		Model<String> currentTimePlusFiveModel = Model.of(time);
		final TextField<String> deleteEndTimeField = new TextField<String>("deleteEndTimeField",
				currentTimePlusFiveModel);
		scheduleBox.add(deleteEndTimeField);
		LoadableDetachableModel<String> affectedValuesModel = new LoadableDetachableModel<String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5654935158589164742L;

			@Override
			protected String load() {
				String loc = scheduleChoice.getDefaultModelObjectAsString();
				String startTime = deleteStartTimeField.getDefaultModelObjectAsString();
				String endTime = deleteEndTimeField.getDefaultModelObjectAsString();
				String items = String.valueOf(Util.getInstance().getNrScheduleValues(loc, startTime, endTime));
				if (items == null)
					items = "0";
				return items;
			}
		};
		final Label deleteAffectedValuesField = new Label("deleteAffectedValuesField", affectedValuesModel);
		scheduleBox.add(deleteAffectedValuesField);

		LoadableDetachableModel<String> convertedStartTimeModel = new LoadableDetachableModel<String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5654935158589164742L;

			@Override
			protected String load() {
				String time = deleteStartTimeField.getDefaultModelObjectAsString();
				try {
					long t = Long.parseLong(time);
					time = Util.getInstance().convertToReadableTime(t);
				} catch (Exception e) {
				}
				return time;
			}
		};
		final Label convertedStartTime = new Label("convertedStartTime", convertedStartTimeModel);
		scheduleBox.add(convertedStartTime);
		LoadableDetachableModel<String> convertedEndTimeModel = new LoadableDetachableModel<String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5654935158589164742L;

			@Override
			protected String load() {
				String time = deleteEndTimeField.getDefaultModelObjectAsString();
				try {
					long t = Long.parseLong(time);
					time = Util.getInstance().convertToReadableTime(t);
				} catch (Exception e) {
				}
				return time;
			}
		};
		final Label convertedEndTime = new Label("convertedEndTime", convertedEndTimeModel);
		scheduleBox.add(convertedEndTime);

		AjaxButton addHourStartButton = new AjaxButton("addHourStartButton") {

			private static final long serialVersionUID = -1742112424158094767L;

			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				String time = deleteStartTimeField.getDefaultModelObjectAsString();
				try {
					long t = Long.parseLong(time);
					t += 60 * 60 * 1000l;
					time = String.valueOf(t);
				} catch (Exception e) {
				}
				deleteStartTimeField.setDefaultModelObject(time);
				target.add(form);
			}

		};
		addHourStartButton.add(new Label("addHourStartButtonLabel", "Add 1h"));
		scheduleBox.add(addHourStartButton);
		AjaxButton addDayStartButton = new AjaxButton("addDayStartButton") {

			private static final long serialVersionUID = -1742112424158094767L;

			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				String time = deleteStartTimeField.getDefaultModelObjectAsString();
				try {
					long t = Long.parseLong(time);
					t += 24 * 60 * 60 * 1000l;
					time = String.valueOf(t);
				} catch (Exception e) {
				}
				deleteStartTimeField.setDefaultModelObject(time);
				target.add(form);
			}

		};
		addDayStartButton.add(new Label("addDayStartButtonLabel", "Add 1d"));
		scheduleBox.add(addDayStartButton);
		AjaxButton addFiveMinStartButton = new AjaxButton("addFiveMinStartButton") {

			private static final long serialVersionUID = -1742112424158094767L;

			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				String time = deleteStartTimeField.getDefaultModelObjectAsString();
				try {
					long t = Long.parseLong(time);
					t += 5 * 60 * 1000l;
					time = String.valueOf(t);
				} catch (Exception e) {
				}
				deleteStartTimeField.setDefaultModelObject(time);
				target.add(form);
			}

		};
		addFiveMinStartButton.add(new Label("addFiveMinStartButtonLabel", "Add 5min"));
		scheduleBox.add(addFiveMinStartButton);
		AjaxButton addWeekStartButton = new AjaxButton("addWeekStartButton") {

			private static final long serialVersionUID = -1742112424158094767L;

			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				String time = deleteStartTimeField.getDefaultModelObjectAsString();
				try {
					long t = Long.parseLong(time);
					t += 7 * 24 * 60 * 60 * 1000l;
					time = String.valueOf(t);
				} catch (Exception e) {
				}
				deleteStartTimeField.setDefaultModelObject(time);
				target.add(form);
			}

		};
		addWeekStartButton.add(new Label("addWeekStartButtonLabel", "Add 1 week"));
		scheduleBox.add(addWeekStartButton);

		AjaxButton addHourEndButton = new AjaxButton("addHourEndButton") {

			private static final long serialVersionUID = -1742112424158094767L;

			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				String time = deleteEndTimeField.getDefaultModelObjectAsString();
				try {
					long t = Long.parseLong(time);
					t += 60 * 60 * 1000l;
					time = String.valueOf(t);
				} catch (Exception e) {
				}
				deleteEndTimeField.setDefaultModelObject(time);
				target.add(form);
			}

		};
		addHourEndButton.add(new Label("addHourEndButtonLabel", "Add 1h"));
		scheduleBox.add(addHourEndButton);
		AjaxButton addDayEndButton = new AjaxButton("addDayEndButton") {

			private static final long serialVersionUID = -1742112424158094767L;

			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				String time = deleteEndTimeField.getDefaultModelObjectAsString();
				try {
					long t = Long.parseLong(time);
					t += 24 * 60 * 60 * 1000l;
					time = String.valueOf(t);
				} catch (Exception e) {
				}
				deleteEndTimeField.setDefaultModelObject(time);
				target.add(form);
			}

		};
		addDayEndButton.add(new Label("addDayEndButtonLabel", "Add 1d"));
		scheduleBox.add(addDayEndButton);
		AjaxButton addFiveMinEndButton = new AjaxButton("addFiveMinEndButton") {

			private static final long serialVersionUID = -1742112424158094767L;

			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				String time = deleteEndTimeField.getDefaultModelObjectAsString();
				try {
					long t = Long.parseLong(time);
					t += 5 * 60 * 1000l;
					time = String.valueOf(t);
				} catch (Exception e) {
				}
				deleteEndTimeField.setDefaultModelObject(time);
				target.add(form);
			}

		};
		addFiveMinEndButton.add(new Label("addFiveMinEndButtonLabel", "Add 5min"));
		scheduleBox.add(addFiveMinEndButton);
		AjaxButton addWeekEndButton = new AjaxButton("addWeekEndButton") {

			private static final long serialVersionUID = -1742112424158094767L;

			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				String time = deleteEndTimeField.getDefaultModelObjectAsString();
				try {
					long t = Long.parseLong(time);
					t += 7 * 24 * 60 * 60 * 1000l;
					time = String.valueOf(t);
				} catch (Exception e) {
				}
				deleteEndTimeField.setDefaultModelObject(time);
				target.add(form);
			}

		};
		addWeekEndButton.add(new Label("addWeekEndButtonLabel", "Add 1 week"));
		scheduleBox.add(addWeekEndButton);

		deleteStartTimeField.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			/**
			 * 
			 */
			private static final long serialVersionUID = -5601307612112315336L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				deleteAffectedValuesField.modelChanged();
				convertedStartTime.modelChanged();
				target.add(displayForm);
			}
		});

		deleteEndTimeField.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			/**
			 * 
			 */
			private static final long serialVersionUID = -5601307612112315336L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				deleteAffectedValuesField.modelChanged();
				convertedEndTime.modelChanged();
				target.add(displayForm);
			}
		});

		final List<String[]> scheduleList = new ArrayList<String[]>(Util.getInstance().getScheduleData(
				scheduleChoice.getDefaultModelObjectAsString(), timeFormatChoice.getDefaultModelObjectAsString()));
		ListDataProvider<String[]> scheduleListDataProvider = new ListDataProvider<String[]>(scheduleList);
		DataView<String[]> scheduleDataView = new DataView<String[]>("scheduleRows", scheduleListDataProvider) {

			private static final long serialVersionUID = 1023457427626025110L;

			@Override
			protected void populateItem(Item<String[]> item) {
				String[] resourcesArr = item.getModelObject();
				RepeatingView repeatingView = new RepeatingView("scheduleDataRow");
				for (int i = 0; i < resourcesArr.length; i++) {
					repeatingView.add(new Label(repeatingView.newChildId(), resourcesArr[i]));
				}
				item.add(repeatingView);
			}
		};

		scheduleDataView.setOutputMarkupId(true);
		scheduleBox.add(scheduleDataView);
		// informationBox.add(new PagingNavigator("pagingNavigator", dataView));

		Label delLabel = new Label("delLabel", "Delete values in range");
		scheduleBox.add(delLabel);

		AjaxButton deleteButton = new AjaxButton("deleteButton") {

			private static final long serialVersionUID = -1742112424158094767L;

			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				String startTime = deleteStartTimeField.getDefaultModelObjectAsString();
				String endTime = deleteEndTimeField.getDefaultModelObjectAsString();
				String location = scheduleChoice.getDefaultModelObjectAsString();
				info(Util.getInstance().deleteValues(location, startTime, endTime));
				scheduleList.clear();
				scheduleList.addAll(Util.getInstance().getScheduleData(scheduleChoice.getDefaultModelObjectAsString(),
						timeFormatChoice.getDefaultModelObjectAsString()));
				target.add(form);
			}

		};
		deleteButton.add(new Label("deleteButtonLabel", "Delete"));
		scheduleBox.add(deleteButton);

		AjaxButton addValueButton = new AjaxButton("addValueButton") {

			private static final long serialVersionUID = -1742112424158094767L;

			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				String schedule = scheduleChoice.getDefaultModelObjectAsString();
				String timestamp = newTimeField.getDefaultModelObjectAsString();
				String value = newValueField.getDefaultModelObjectAsString();
				String quality = newQualityField.getDefaultModelObjectAsString();
				info(Util.getInstance().addScheduleValue(schedule, timestamp, value, quality));
				scheduleList.clear();
				scheduleList.addAll(Util.getInstance().getScheduleData(scheduleChoice.getDefaultModelObjectAsString(),
						timeFormatChoice.getDefaultModelObjectAsString()));
				target.add(form);
			}

		};
		addValueButton.add(new Label("addValueButtonLabel", "Add value"));
		scheduleBox.add(addValueButton);

		displayForm.add(scheduleBox);

		scheduleChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			/**
			 * 
			 */
			private static final long serialVersionUID = -5601307612112315336L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				scheduleList.clear();
				String sched = scheduleChoice.getDefaultModelObjectAsString();
				if (!sched.equals("")) {
					scheduleList.addAll(Util.getInstance().getScheduleData(sched,
							timeFormatChoice.getDefaultModelObjectAsString()));
					scheduleBox.setVisible(true);
				}
				else {
					scheduleBox.setVisible(false);
				}
				String time = Util.getInstance().getCurrentTime("milliseconds");
				deleteStartTimeField.setDefaultModelObject(time);
				newTimeField.setDefaultModelObject(time);
				try {
					long t = Long.parseLong(time);
					t += 5 * 60 * 1000;
					time = String.valueOf(t);
				} catch (Exception e) {
				}
				deleteEndTimeField.setDefaultModelObject(time);
				target.add(displayForm);
			}
		});

		add(displayForm);

		timeFormatChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			/**
			 * 
			 */
			private static final long serialVersionUID = -5601307612112315336L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				String format = timeFormatChoice.getDefaultModelObjectAsString();
				resourcesList.clear();
				resourcesList.addAll(Util.getInstance().getSchedules(format));
				scheduleList.clear();
				scheduleList.addAll(Util.getInstance().getScheduleData(scheduleChoice.getDefaultModelObjectAsString(),
						format));
				target.add(displayForm);
			}
		});

	}

	public String getTitle() {
		return "Schedules Table Viewer";
	}

	public String getAppName() {
		return "Resources Table Viewer";
	}

}
