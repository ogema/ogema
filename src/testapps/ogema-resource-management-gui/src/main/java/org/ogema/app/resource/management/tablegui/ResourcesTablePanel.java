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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.extensions.ajax.markup.html.autocomplete.DefaultCssAutoCompleteTextField;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.string.Strings;
import org.ogema.app.resource.management.gui.ResManagementGuiActivator;
import org.ogema.app.resource.management.tablegui.util.Util;
import org.ogema.apps.wicket.ApplicationPanel;

/**
 * 
 * @author cnoelle
 * 
 */
@SuppressWarnings(value = { "rawtypes" })
public class ResourcesTablePanel extends ApplicationPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2958943720938920409L;
	private FileUploadField fileUpload;
	private String fileName = "temp/resourcesExport";
	private String extension = ".xml";
	private String downloadAddress = fileName + extension;
	private List<Map<Integer, Boolean>> ordering;
	final private Map<Integer, String> ORDERINGITEMS = new HashMap<Integer, String>();
	final private static boolean SIMPLETYPES = false;
	final private static boolean LONGTYPES = true;
	private boolean selectedTypesLength = SIMPLETYPES;
	final private List<String> menues = new ArrayList<String>();
	final private Map<String, WebMarkupContainer> menuesMap = new HashMap<String, WebMarkupContainer>();

	/** FIXME: delete file after import */
	private String UPLOAD_FOLDER = "temp/";
	private String selectedType = "";

	public void initContent() {
		ORDERINGITEMS.clear();
		ORDERINGITEMS.put(Integer.valueOf(0), "Resource tree depth");
		ORDERINGITEMS.put(1, "Resource Type");
		ORDERINGITEMS.put(2, "Active");
		ORDERINGITEMS.put(3, "References");
		// default ordering.
		ordering = new ArrayList<Map<Integer, Boolean>>();
		/*Map<Integer, Boolean> item0 = new HashMap<Integer, Boolean>();
		item0.put(0, true);
		Map<Integer, Boolean> item1 = new HashMap<Integer, Boolean>();
		item1.put(1, true);
		Map<Integer, Boolean> item2 = new HashMap<Integer, Boolean>();
		item2.put(2, true);
		Map<Integer, Boolean> item3 = new HashMap<Integer, Boolean>();
		item3.put(3, true);
		ordering.add(item0);
		ordering.add(item1);
		ordering.add(item2);
		ordering.add(item3);*/
		menuesMap.clear();
		Util.getInstance().initializeClassLoaders();
		Util.getInstance().initializeTypes();

		/***************************** Display Resources **************************/

		final Form<Void> displayForm = new Form<Void>("displayResourcesForm");
		/**
		 * displayForm.setDefaultModel(new CompoundPropertyModel<CreateUseCasePanel>( this));
		 */
		Label displayHeader = new Label("displayHeader", "Existing Resources");
		displayForm.add(displayHeader);
		final WebMarkupContainer informationBox = new WebMarkupContainer("informationBox");
		informationBox.setOutputMarkupPlaceholderTag(true);
		// informationBox.setVisible(false);

		final List<String[]> resourcesList = new ArrayList<String[]>(Util.getInstance().getAllResources(ordering,
				selectedTypesLength));
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

		add(displayForm);

		/***************************** Import/Export Resources **************************/

		final Form<?> form = new Form<Void>("form") {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3114296503280427877L;

			@Override
			protected void onSubmit() {

				final FileUpload uploadedFile = fileUpload.getFileUpload();
				if (uploadedFile != null) {

					// check type (only extension)
					if (!uploadedFile.getClientFileName().endsWith(".xml")) {
						info("Please upload an xml file (.xml). Filename: " + uploadedFile.getClientFileName());
						return;
					}

					// write to a new file
					File newFile = new File(UPLOAD_FOLDER + uploadedFile.getClientFileName());

					if (newFile.exists()) {
						newFile.delete();
					}

					try {
						newFile.createNewFile();
						uploadedFile.writeTo(newFile);

						// info("saved file: " + uploadedFile.getClientFileName());
					} catch (Exception e) {
						info("Error");
					}
					String fbFileUpload = Util.getInstance().readXMLInput(newFile);
					info(fbFileUpload);
					/*
					 * for (String fb : fbFileUpload) { info(fb); }
					 */
				}

			}

		};

		AjaxButton updateButton = new AjaxButton("updateButton") {

			private static final long serialVersionUID = -1742112424158094767L;

			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				target.add(form);
				// target.add(orderingForm);
				resourcesList.clear();
				resourcesList.addAll(Util.getInstance().getAllResources(ordering, selectedTypesLength));
				// informationBox.setVisible(true);
				target.add(displayForm);
			}

		};
		updateButton.add(new Label("updateButtonLabel", "Update resources view"));
		form.add(updateButton);

		form.add(new FeedbackPanel("feedback"));

		// Enable multipart mode (need for uploads file)
		form.setMultiPart(true);

		// max upload size, 5MB
		form.setMaxSize(Bytes.kilobytes(5000));

		final WebMarkupContainer importContainer = new WebMarkupContainer("importContainer");
		importContainer.setOutputMarkupPlaceholderTag(true);
		String impHeader = "Resources file import/export";
		Label importHeader = new Label("importHeader", impHeader);
		importContainer.add(importHeader);
		menuesMap.put("Resources file import/export", importContainer);
		importContainer.setVisible(false);
		importContainer.add(fileUpload = new FileUploadField("fileUpload"));
		Label importFileLabel = new Label("importFileLabel",
				"Import resources from file \n (currently only .xml supported)");
		importContainer.add(importFileLabel);
		Label downloadFileLabel = new Label("downloadFileLabel", "Download resources table in .xml format");
		importContainer.add(downloadFileLabel);

		/** causes an exception, in connection with the file upload. */

		IModel fileModel = new AbstractReadOnlyModel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 9130123564430626726L;

			public Object getObject() {
				try {
					long timestamp = ResManagementGuiActivator.getAppManager().getFrameworkTime();
					Calendar c = Calendar.getInstance();
					c.setTimeInMillis(timestamp);
					String tStamp = String.valueOf(c.get(Calendar.YEAR)) + "-"
							+ String.valueOf(c.get(Calendar.MONTH) + 1) + "-" + String.valueOf(c.get(Calendar.DATE))
							+ "_" + String.valueOf(c.get(Calendar.HOUR_OF_DAY)) + "h"
							+ String.valueOf(c.get(Calendar.MINUTE)) + "m" + String.valueOf(c.get(Calendar.SECOND))
							+ "s";
					downloadAddress = fileName + tStamp + ".xml";
					return Util.getInstance().generateXMLFile(ordering, downloadAddress);
					// return Util.getInstance().generateExcelFile(ordering, downloadAddress);
				} catch (FileNotFoundException e) {
					return null;
				}
			}
		};

		@SuppressWarnings("unchecked")
		final DownloadLink downloadlink = new DownloadLink("downloadLink", fileModel, downloadAddress) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 3300414797316333830L;

			public void onClick() {
				// doesn't work - file not updated. Instead, generate file upon
				// selection of resource type.
				/*
				 * try { file2 = Util.getInstance().generateFile(downloadAddress,
				 * resTypeChoice.getDefaultModelObjectAsString()); } catch (FileNotFoundException e) { file2= null; //
				 * TODO Auto-generated catch block }
				 */
				File file2 = getModelObject();
				if (file2 == null) {
					return;
				}
				try {
					IResourceStream resourceStream = new FileResourceStream(new org.apache.wicket.util.file.File(file2));
					ResourceStreamRequestHandler t = new ResourceStreamRequestHandler(resourceStream, file2.getName());
					getRequestCycle().scheduleRequestHandlerAfterCurrent(t);
				} catch (Exception ee) {
					info("Internal error. Did you select a resource type?");
				}
			}
		};

		downloadlink.setDeleteAfterDownload(true);
		downloadlink.setOutputMarkupId(true);
		// doesn't work
		// downloadlink.setDeleteAfterDownload(true);

		importContainer.add(downloadlink);
		form.add(importContainer);

		/***************************** Order Resources Displayed **************************/

		class OrderingClass implements Serializable {
			/**
			 * 
			 */
			private static final long serialVersionUID = -7270578912749115148L;
			public String id;
		}

		final List<OrderingClass> orderingList = new ArrayList<OrderingClass>();
		for (String orderingItem : ORDERINGITEMS.values()) {
			OrderingClass item = new OrderingClass();
			item.id = orderingItem;
			orderingList.add(item);
		}
		final ListModel orderingModel = new ListModel<OrderingClass>(orderingList);
		List<OrderingClass> initialOrdering = new ArrayList<OrderingClass>();
		/*Iterator<Map<Integer, Boolean>> iter = ordering.iterator();
		while (iter.hasNext()) {
			List<Integer> list = new ArrayList<Integer>(iter.next().keySet());
			Integer integ = list.get(0);
			String entry = ORDERINGITEMS.get(integ);
			for (OrderingClass object : orderingList) {
				if (object.id.equals(entry)) {
					initialOrdering.add(object);
					break;
				}
			}
		}*/

		final WebMarkupContainer orderingContainer = new WebMarkupContainer("orderingContainer");
		orderingContainer.setOutputMarkupPlaceholderTag(true);
		menuesMap.put("Resources viewer configuration", orderingContainer);
		orderingContainer.setVisible(false);
		String ordHeader = "Resources viewer configuration";
		Label orderingHeader = new Label("orderingHeader", ordHeader);
		orderingContainer.add(orderingHeader);
		final List<OrderingClass> selectedOrderings = new ArrayList<OrderingClass>(initialOrdering);
		IChoiceRenderer<OrderingClass> renderOrderings = new ChoiceRenderer<OrderingClass>("id", "id");
		@SuppressWarnings("unchecked")
		final Palette<OrderingClass> orderingField = new Palette<OrderingClass>("orderings",
				new ListModel<OrderingClass>(selectedOrderings), orderingModel, renderOrderings, 5, true);
		orderingContainer
				.add(new Label(
						"orderingsLabel",
						"Orderings (order of items relevant). \n "
								+ "The last sorting criterion is always the alphabetical resource path, so if no items are selected, the list will "
								+ "be ordered alphabetically."));
		orderingField.setOutputMarkupId(true);
		orderingContainer.add(orderingField);
		// TODO update orderings list on change of selectedOrderings
		final List<OrderingClass> selectedInvertedOrderings = new ArrayList<OrderingClass>();
		@SuppressWarnings("unchecked")
		final Palette<OrderingClass> invertedOrderingField = new Palette<OrderingClass>("invertedOrderings",
				new ListModel<OrderingClass>(selectedInvertedOrderings), orderingModel, renderOrderings, 5, true);
		orderingContainer.add(new Label("invertedOrderingsLabel", "Inverted orderings (order of items irrelevant)"));
		invertedOrderingField.setOutputMarkupId(true);
		orderingContainer.add(invertedOrderingField);
		// TODO update orderings list on change of selectedInvertedOrderings

		final Model<String> selectedSettingsTypeModel = Model.of("All");
		LoadableDetachableModel<List<String>> settingsTypeModel = new LoadableDetachableModel<List<String>>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5654935158589164742L;

			@Override
			protected List<String> load() {
				List<String> resourceTypes = new ArrayList<String>();
				resourceTypes.add("All");
				Util.getInstance().initializeTypes();
				resourceTypes.addAll(Util.getInstance().getAvailableTypes(LONGTYPES));
				return resourceTypes;
			}
		};
		@SuppressWarnings("unchecked")
		final DropDownChoice settingsTypeField = new DropDownChoice("settingsTypeField", selectedSettingsTypeModel,
				settingsTypeModel);
		orderingContainer.add(new Label("settingsTypeLabel", "Choose resource type to be displayed"));
		// settingsTypeField.setOutputMarkupId(true);
		orderingContainer.add(settingsTypeField);

		final CheckBox longResTypeNames = new CheckBox("longResTypeNames", Model.of(Boolean.FALSE));
		orderingContainer.add(new Label("longResTypeNamesLabel", "Show long resource type names?"));
		orderingContainer.add(longResTypeNames);
		// required to update model on change of value
		longResTypeNames.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 5833321729212821383L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(form);
			}
		});

		final CheckBox showSubResources = new CheckBox("showSubResources", Model.of(Boolean.TRUE));
		final Label showSubResourcesLabel = new Label("showSubResourcesLabel", "Show subresources?");
		showSubResourcesLabel.setOutputMarkupPlaceholderTag(true);
		showSubResourcesLabel.setVisible(false);
		orderingContainer.add(showSubResourcesLabel);
		orderingContainer.add(showSubResources);
		showSubResources.setOutputMarkupPlaceholderTag(true);
		showSubResources.setVisible(false);
		// required to update model on change of value
		showSubResources.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 533031978336635457L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(form);
			}
		});

		// required to update model on change of value
		settingsTypeField.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			/**
			 * 
			 */
			private static final long serialVersionUID = -2140904993357856803L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				// target.add(orderingContainer);
				if (!settingsTypeField.getDefaultModelObjectAsString().equals("All")) {
					showSubResources.setVisible(true);
					showSubResourcesLabel.setVisible(true);
				}
				else {
					showSubResources.setVisible(false);
					showSubResourcesLabel.setVisible(false);
				}
				target.add(form);
				//target.add(displayForm);
			}
		});

		AjaxButton orderingsSaveButton = new AjaxButton("orderingsSaveButton") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 6585830537025088534L;

			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				target.add(form);
				ordering.clear();
				for (OrderingClass object : selectedOrderings) {
					String id = object.id;
					Integer entry = Util.getInstance().getKey(ORDERINGITEMS, id);
					Boolean value = true;
					for (OrderingClass invertedItem : selectedInvertedOrderings) {
						if (invertedItem.id.equals(id)) {
							value = false;
							break;
						}
					}
					Map<Integer, Boolean> map = new HashMap<Integer, Boolean>();
					map.put(entry, value);
					ordering.add(map);
				}
				// target.add(orderingContainer);

				boolean length = longResTypeNames.getModelObject();
				selectedTypesLength = length;
				resourcesList.clear();
				String type = settingsTypeField.getDefaultModelObjectAsString();
				if (type.equals("All")) {
					resourcesList.addAll(Util.getInstance().getAllResources(ordering, selectedTypesLength));
				}
				else {
					boolean subRes = showSubResources.getModelObject();
					resourcesList.addAll(Util.getInstance().getResources(ordering, selectedTypesLength, type, subRes));
				}
				target.add(displayForm);
				target.add(form);
			}
		};
		orderingsSaveButton.add(new Label("orderingButtonLabel", "Save settings"));
		orderingContainer.add(orderingsSaveButton);

		form.add(orderingContainer);

		/***************************** Create Resources *************************************/

		final WebMarkupContainer creationContainer = new WebMarkupContainer("creationContainer");
		creationContainer.setOutputMarkupPlaceholderTag(true);
		menuesMap.put("Resource creator", creationContainer);
		creationContainer.setVisible(false);
		String creHeader = "Resource creation";
		Label creationHeader = new Label("creationHeader", creHeader);
		creationContainer.add(creationHeader);

		final Model<String> selectedParentModel = Model.of("Create top-level resource");
		LoadableDetachableModel<List<String>> parentsModel = new LoadableDetachableModel<List<String>>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5654935158589164742L;

			@Override
			protected List<String> load() {
				List<String> paths = new ArrayList<String>();
				paths.add("Create top-level resource");
				paths.addAll(Util.getInstance().getResourcesPaths());
				return paths;
			}
		};
		@SuppressWarnings("unchecked")
		final DropDownChoice creationParentField = new DropDownChoice("creationParent", selectedParentModel,
				parentsModel);
		creationContainer.add(new Label("creationParentLabel", "Choose parent resource"));
		creationParentField.setOutputMarkupId(true);
		creationContainer.add(creationParentField);

		final Model<String> creationTypeModel = Model.of("org.ogema.core.model.");
		final DefaultCssAutoCompleteTextField<String> creationTypeField = new DefaultCssAutoCompleteTextField<String>(
				"creationType", creationTypeModel) {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3425395992747279606L;

			// works, but not displayed
			@Override
			protected Iterator<String> getChoices(String input) {
				if (Strings.isEmpty(input)) {
					List<String> emptyList = Collections.emptyList();
					return emptyList.iterator();
				}

				List<String> choices = new ArrayList<String>(10);

				List<String> allTypes = Util.getInstance().getAvailableTypes(LONGTYPES);

				for (final String mType : allTypes) {

					if (mType.toUpperCase().startsWith(input.toUpperCase())) {
						choices.add(mType);
						if (choices.size() == 10) {
							break;
						}
					}
				}
				return choices.iterator();
			}
		};

		final Model<String> creationNameModel = Model.of("");

		final DefaultCssAutoCompleteTextField<String> creationNameField = new DefaultCssAutoCompleteTextField<String>(
				"creationName", creationNameModel) {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3425395992747279606L;

			@Override
			protected Iterator<String> getChoices(String input) {
				/*
				 * if (Strings.isEmpty(input)) { List<String> emptyList = Collections.emptyList(); return
				 * emptyList.iterator(); }
				 */

				List<String> choices = new ArrayList<String>(10);
				String selectedParent = creationParentField.getDefaultModelObjectAsString();
				List<String> optionalElements = Util.getInstance().getUnavailableOptionalElements(selectedParent);
				// FIXME
				System.out.println("Found elements: " + optionalElements);
				if (input.replaceAll("\\s", "").equals(""))
					return optionalElements.iterator();
				for (final String mType : optionalElements) {

					if (mType.toUpperCase().startsWith(input.toUpperCase())) {
						choices.add(mType);
						if (choices.size() == 10) {
							break;
						}
					}
				}
				return choices.iterator();
			}
		};

		final Model<String> creationValueModel = Model.of("");
		final TextField<String> creationValueField = new TextField<String>("creationValue", creationValueModel);
		final Label creationLabel = new Label("creationValueLabel",
				"Enter a value appropriate for the chosen resource type");
		creationLabel.setOutputMarkupId(true);
		creationLabel.setVisible(false);
		creationContainer.add(creationLabel);
		creationValueField.setVisible(false);
		creationValueField.setOutputMarkupId(true);

		/*
		 * final TextField<String> creationNameField = new TextField<String>("creationName", creationNameModel);
		 */
		creationContainer.add(new Label("creationNameLabel", "Enter a name for your resource"));
		creationNameField.setOutputMarkupId(true);
		creationContainer.add(creationNameField);
		// required to update model on change of value
		creationNameField.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			private static final long serialVersionUID = 3604136075374815092L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				String parent = creationParentField.getDefaultModelObjectAsString();
				String name = creationNameField.getDefaultModelObjectAsString();
				if (!parent.startsWith("Create top") && !name.replaceAll("\\s", "").equals("")) {
					String type = Util.getInstance().getSubresourceType(parent, name);
					if (type != null) {
						creationTypeModel.setObject(type);
						selectedType = type;
						if (Util.getInstance().isSimpleResource(type)) {
							creationValueField.setVisible(true);
							creationLabel.setVisible(true);
							if (Util.getInstance().resourceExists(parent + "/" + name)) {
								creationValueModel.setObject(Util.getInstance().getSimpleValueAsString(
										parent + "/" + name));
							}
						}
						else {
							creationValueModel.setObject("");
							creationValueField.setVisible(false);
							creationLabel.setVisible(false);
						}
					}
					else {
						creationValueModel.setObject("");
					}
				}
				target.add(form);
			}
		});

		creationContainer.add(creationValueField);
		// required to update model on change of value
		creationValueField.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			/**
			 * 
			 */
			private static final long serialVersionUID = -1479115711583613369L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(form);
			}
		});

		creationContainer.add(new Label("creationTypeLabel",
				"Choose resource type (format org.ogema.core.model.Resource)"));
		creationTypeField.setOutputMarkupId(true);
		creationContainer.add(creationTypeField);

		// required to update model on change of value
		creationParentField.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 6472109939069382098L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(form);
			}
		});

		final Model<String> selectedReferenceModel = Model.of("No reference");
		LoadableDetachableModel<List<String>> referenceModel = new LoadableDetachableModel<List<String>>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5654935158589164742L;

			@Override
			protected List<String> load() {
				List<String> paths = new ArrayList<String>();
				paths.add("No reference");
				// String type = creationTypeField.getDefaultModelObjectAsString();
				paths.addAll(Util.getInstance().getResourcesPaths(selectedType));
				return paths;
			}
		};
		@SuppressWarnings("unchecked")
		final DropDownChoice creationReferenceField = new DropDownChoice("creationReference", selectedReferenceModel,
				referenceModel);
		creationContainer.add(new Label("creationReferenceLabel", "Choose resource to be referenced"));
		creationReferenceField.setOutputMarkupId(true);
		creationContainer.add(creationReferenceField);
		// required to update model on change of value
		creationReferenceField.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			/**
			 * 
			 */
			private static final long serialVersionUID = -108092668172170320L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(creationContainer);
				String resource = creationReferenceField.getDefaultModelObjectAsString();
				String type = Util.getInstance().getTypeLongName(resource);
				if (type != null) {
					creationTypeModel.setObject(type);
				}
				boolean isSimple = Util.getInstance().isSimpleResource(type);
				if (isSimple) {
					creationValueModel.setObject(Util.getInstance().getSimpleValueAsString(resource));
					creationValueField.setVisible(true);
					creationLabel.setVisible(true);
				}
				else if (resource.startsWith("No reference")) {
					creationValueModel.setObject("");
					target.add(form);
					return;
				}
				else {
					creationValueModel.setObject("");
					creationValueField.setVisible(false);
					creationLabel.setVisible(false);
				}
				target.add(form);
			}
		});

		creationTypeField.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 5228400578390934896L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				// target.add(creationContainer);
				String type = creationTypeField.getDefaultModelObjectAsString();
				selectedType = type;
				boolean isSimple = Util.getInstance().isSimpleResource(type);
				if (isSimple) {
					creationValueModel.setObject("");
					creationValueField.setVisible(true);
					creationLabel.setVisible(true);
				}
				else {
					creationValueModel.setObject("");
					creationValueField.setVisible(false);
					creationLabel.setVisible(false);
				}
				if (!Util.getInstance().classFound(type)) {
					selectedReferenceModel.setObject("No reference");
				}

				target.add(form);

			}
		});

		final CheckBox chk0 = new CheckBox("creationActiveCheckbox", Model.of(Boolean.TRUE));
		creationContainer.add(new Label("creationActiveLabel", "Create active?"));
		creationContainer.add(chk0);
		// required to update model on change of value
		chk0.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 939554832109035463L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(form);
			}
		});

		AjaxButton createButton = new AjaxButton("createButton") {

			private static final long serialVersionUID = -5270043320429147888L;

			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				// super.onSubmit(target, form);
				// target.add(orderingContainer);
				String parent = creationParentField.getDefaultModelObjectAsString();
				if (parent.startsWith("Create top"))
					parent = "";
				else
					parent = parent + "/";
				String name = creationNameField.getDefaultModelObjectAsString();
				String path = parent + Util.getInstance().getValidName(name);
				if (Util.getInstance().resourceExists(path)) {
					info("Resource " + path + " exists already.");
					target.add(form);
					return;
				}
				String type = creationTypeField.getDefaultModelObjectAsString();
				String location = creationReferenceField.getDefaultModelObjectAsString();
				if (location.startsWith("No ref"))
					location = path;
				String value = creationValueField.getDefaultModelObjectAsString();
				boolean active = (Boolean) chk0.getDefaultModelObject();
				// System.out.println("  Trying to create resource with name " + name + ", path = " +
				// path + ", location = " + location + ", type = " + type + ", value = " + value);
				if (Util.getInstance().createResource(path, type, location, active, value)) {
					info("Resource " + path + " has been created.");
					resourcesList.clear();
					resourcesList.addAll(Util.getInstance().getAllResources(ordering, selectedTypesLength));

					String actualType = Util.getInstance().getTypeLongName(path);
					if (actualType == null)
						info("Error: type is null.");
					else if (!actualType.equals(type)) {
						info("Type of created resource differs from input. Expected: " + type + "; found: "
								+ actualType);
					}
				}
				else {
					info("Error creating resource " + path);
				}
				target.add(displayForm);
				target.add(form);
			}
		};
		createButton.add(new Label("createButtonLabel", "Create resource"));
		creationContainer.add(createButton);

		form.add(creationContainer);

		/***************************** Manipulate Resources *************************************/
		final WebMarkupContainer manipulationContainer = new WebMarkupContainer("manipulationContainer");
		manipulationContainer.setOutputMarkupPlaceholderTag(true);
		menuesMap.put("Resource manipulation", manipulationContainer);
		manipulationContainer.setVisible(false);
		String manHeader = "Resource manipulation";
		Label manipulationHeader = new Label("manipulationHeader", manHeader);
		manipulationContainer.add(manipulationHeader);
		final Model<String> manipulationValueModel = Model.of("");
		final TextField<String> manipulationValueField = new TextField<String>("manipulationValueField",
				manipulationValueModel);
		manipulationValueField.setOutputMarkupPlaceholderTag(true);
		manipulationValueField.setVisible(false);
		manipulationContainer.add(manipulationValueField);
		manipulationValueField.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 3179495888439559053L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(form);
			}
		});
		final Label manipulationValueLabel = new Label("manipulationValueLabel",
				"Enter new value compatible with given resource type");
		manipulationValueLabel.setOutputMarkupPlaceholderTag(true);
		manipulationValueLabel.setVisible(false);
		manipulationContainer.add(manipulationValueLabel);

		final Model<String> selectedManipulatableResourceModel = Model.of("");
		LoadableDetachableModel<List<String>> manipulateModel = new LoadableDetachableModel<List<String>>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5654935158589164742L;

			@Override
			protected List<String> load() {
				List<String> paths = new ArrayList<String>();
				paths.add("");
				paths.addAll(Util.getInstance().getResourcesPaths());
				return paths;
			}
		};
		@SuppressWarnings("unchecked")
		final DropDownChoice manipulationResourceField = new DropDownChoice("manipulationResource",
				selectedManipulatableResourceModel, manipulateModel);
		manipulationContainer.add(new Label("manipulationResourceLabel", "Choose resource to be edited"));
		manipulationResourceField.setOutputMarkupId(true);
		manipulationContainer.add(manipulationResourceField);
		// required to update model on change of value
		LoadableDetachableModel<String> manipulationResTypeModel = new LoadableDetachableModel<String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5654935158589164742L;

			@Override
			protected String load() {
				String resource = manipulationResourceField.getDefaultModelObjectAsString();
				if (resource == null || resource.replaceAll("\\s", "").equals(""))
					return "";
				return Util.getInstance().getTypeLongName(resource);
			}
		};
		Label manipulationResourceType = new Label("manipulationResTypeLabel", manipulationResTypeModel);
		manipulationResourceType.setOutputMarkupPlaceholderTag(true);
		manipulationContainer.add(manipulationResourceType);

		final AjaxButton manipulateActivationToggler = new AjaxButton("manipulateActivationToggler") {

			private static final long serialVersionUID = -5270043320429147888L;

			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				target.add(manipulationContainer);
				target.add(orderingContainer);

				String resource = manipulationResourceField.getDefaultModelObjectAsString();
				Util.getInstance().toggleActivation(resource);

				resourcesList.clear();
				resourcesList.addAll(Util.getInstance().getAllResources(ordering, selectedTypesLength));
				target.add(displayForm);
				target.add(manipulationContainer);
				target.add(form);
			}
		};
		manipulateActivationToggler.setOutputMarkupId(true);
		LoadableDetachableModel<String> manipulationActivatorToggleModel = new LoadableDetachableModel<String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5654935158589164742L;

			@Override
			protected String load() {
				String resource = manipulationResourceField.getDefaultModelObjectAsString();
				return Util.getInstance().getActivationTogglerLabel(resource);
			}
		};
		manipulateActivationToggler
				.add(new Label("manipulateActivationTogglerLabel", manipulationActivatorToggleModel));
		manipulateActivationToggler.setVisible(false);
		manipulationContainer.add(manipulateActivationToggler);
		final Label manipulateActivationLabel = new Label("manipulateActivationLabel", "Toggle Activation");
		manipulateActivationLabel.setOutputMarkupPlaceholderTag(true);
		manipulateActivationLabel.setVisible(false);
		manipulationContainer.add(manipulateActivationLabel);

		final AjaxButton sendManipulationValues = new AjaxButton("sendManipulationValues") {

			private static final long serialVersionUID = -5270043320429147888L;

			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				// super.onSubmit(target, form);
				// target.add(manipulationContainer);
				// target.add(orderingContainer);
				String path = manipulationResourceField.getDefaultModelObjectAsString();
				if (path == null || path.replaceAll("\\s", "").equals("")) {
					return;
				}
				String value = manipulationValueField.getDefaultModelObjectAsString();
				info(Util.getInstance().setSimpleValue(path, value));

				resourcesList.clear();
				resourcesList.addAll(Util.getInstance().getAllResources(ordering, selectedTypesLength));
				target.add(displayForm);
				target.add(form);
			}
		};
		sendManipulationValues.add(new Label("sendManipulationValuesLabel", "Set value"));
		sendManipulationValues.setOutputMarkupPlaceholderTag(true);
		sendManipulationValues.setVisible(false);
		manipulationContainer.add(sendManipulationValues);

		manipulationResourceField.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			/**
			 * 
			 */
			private static final long serialVersionUID = -6716820035767490442L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				// target.add(manipulationContainer);
				String path = manipulationResourceField.getDefaultModelObjectAsString();
				if (path == null || path.replaceAll("\\s", "").equals("")) {
					manipulationValueField.setDefaultModelObject("");
					manipulateActivationToggler.setVisible(false);
					manipulationValueField.setVisible(false);
					manipulationValueLabel.setVisible(false);
					manipulateActivationLabel.setVisible(false);
					sendManipulationValues.setVisible(false);
					target.add(form);
					return;
				}
				else {
					manipulateActivationToggler.setVisible(true);
					manipulateActivationLabel.setVisible(true);
					// sendManipulationValues.setVisible(true);
				}
				String type = Util.getInstance().getTypeLongName(path);
				if (type == null) {
					manipulationValueField.setDefaultModelObject("");
					manipulationValueField.setVisible(false);
					manipulationValueLabel.setVisible(false);
					manipulateActivationLabel.setVisible(false);
					sendManipulationValues.setVisible(false);
					target.add(form);
					return;
				}
				if (Util.getInstance().isSimpleResource(type)) {
					String value = Util.getInstance().getSimpleValueAsString(path);
					manipulationValueField.setDefaultModelObject(value);
					manipulationValueField.setVisible(true);
					manipulationValueLabel.setVisible(true);
					sendManipulationValues.setVisible(true);
				}
				else {
					manipulationValueField.setVisible(false);
					manipulationValueLabel.setVisible(false);
					sendManipulationValues.setVisible(false);
				}
				target.add(form);
			}
		});

		form.add(manipulationContainer);

		add(form);

		menues.clear();
		menues.addAll(menuesMap.keySet());

		final Model<String> selectedMenuModel = Model.of("");
		LoadableDetachableModel<List<String>> menuesModel = new LoadableDetachableModel<List<String>>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5654935158589164742L;

			@Override
			protected List<String> load() {
				List<String> menuesList = new ArrayList<String>();
				menuesList.add("");
				menuesList.addAll(menues);
				return menuesList;
			}
		};
		@SuppressWarnings("unchecked")
		final DropDownChoice menuSelection = new DropDownChoice("menuSelection", selectedMenuModel, menuesModel);
		form.add(new Label("menuSelectionLabel", "Choose menu"));
		menuSelection.setOutputMarkupPlaceholderTag(true);
		form.add(menuSelection);
		menuSelection.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			/**
			 * 
			 */
			private static final long serialVersionUID = -5601307612112315336L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				// target.add(form);
				for (String menu : menues) {
					WebMarkupContainer wmc = menuesMap.get(menu);
					wmc.setVisible(false);
				}
				String selectedMenu = menuSelection.getDefaultModelObjectAsString();
				if (!selectedMenu.equals("")) {
					WebMarkupContainer wmc = menuesMap.get(selectedMenu);
					wmc.setVisible(true);
				}
				target.add(form);
			}
		});

		form.add(new Label("resourcesNumberLabel", "Resources found:"));
		LoadableDetachableModel<String> resourcesNumberModel = new LoadableDetachableModel<String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5654935158589164742L;

			@Override
			protected String load() {
				String number = String.valueOf(resourcesList.size());
				return number;
			}
		};
		final Label resourcesNumberField = new Label("resourcesNumber", resourcesNumberModel);
		resourcesNumberField.setOutputMarkupPlaceholderTag(true);
		form.add(resourcesNumberField);

		add(form);

	}

	public String getTitle() {
		return "Resources Table Viewer";
	}

	public String getAppName() {
		return "Resources Table Viewer";
	}

}
