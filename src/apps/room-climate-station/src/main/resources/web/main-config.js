function showResources(roomId) {
	// destroy old tree and build new one
	$("#jsTree_Resources").jstree("destroy");
	$("#jsTree_Resources").html("");

	if (!($("#jsTree_Resources").hasClass("jstree"))) {
		$("#jsTree_Resources")
				.jstree({
					"core" : {
						"animation" : 0,
						"check_callback" : true,
						"themes" : {
							"stripes" : true
						},
						'data' : {
							"url" : "/service/resourceview",
							'data' : function(node) {
								return {
									'id' : node.id,
									'text' : node.text
								};
							}
						}
					},
					"checkbox" : {
						"whole_node" : true
					},
					"plugins" : [ "wholerow", "types" ]
				})
				.bind(
						"select_node.jstree",
						function(event, data) {
							// close the resource-dialog
							$("#dialog_Resources").dialog("close");

							// id of selected node
							var currentNode = data.node.id;
							// get the node by id
							var currentSelectedResourceNode;
							currentSelectedResourceNode = $("#jsTree_Resources")
									.jstree(true).get_node(currentNode);

							// check if the current selected node is NOT a
							// parent node i.e. has NO children
							// if (!$("#jsTree_Resources").jstree("is_parent",
							// data.node)) {

							$
									.getJSON(
											"/service/resourcevalue?id="
													+ currentNode,
											function(json) {

												var curNodeId;
												var fullNode;
												var dataLen = json.length;
												for (var i = 0; i < dataLen; i++) {
													curNodeId = json[i].id;
													if (curNodeId == data.node.id) {
														fullNode = json[i];
													}
												}

												// set title of dialog box
												// $("#dialog_Resources").parent().find(".ui-dialog-title").text(currentSelectedResourceNode.text);
												$("#dialog_Resources")
														.parent()
														.find(
																".ui-dialog-title")
														.text(
																"Resource Details");

												var resourcePath = fullNode.path;

												// fill dialog with the value of
												// the node and add button
												$("#dialog_Resources")
														.html(
																"<br>Selected Resource:"
																		+ '<br><br><span style="width:110px;display:block;float:left;"><b>Name: </b></span>'
																		+ fullNode.path
																		+ '<br><span style="width:110px;display:block;float:left;"><b>Type: </b></span>'
																		+ fullNode.type
																		+ "<br>"
																		+ '<br><span style="width:110px;display:block;float:left;"><b>Reference: </b></span>'
																		+ fullNode.reference
																		+ "<br>"
																		+ '<br><span style="width:110px;display:block;float:left;"><b>Value: </b></span>'
																		+ fullNode.value
																		+ "<br>"
																		+ "<br><button type='button' onclick='setResource4Sensor("
																		+ '"'
																		+ roomId
																		+ '"'
																		+ ","
																		+ '"'
																		+ resourcePath
																		+ '"'
																		+ ")'>Set Room Sensor For...</button>"
																		+ '<br><select id="'
																		+ 'sensor_select'
																		+ '">'
																		+ '<option selected="selected">'
																		+ 'Temperature'
																		+ '</option>'
																		+ '<option>'
																		+ 'Humidity'
																		+ '</option>'
																		+ '<option>'
																		+ 'Light'
																		+ '</option>'
																		+ '<option>'
																		+ 'Light Switch'
																		+ '</option>'
																		+ '<option>'
																		+ 'Light Dimmer'
																		+ '</option>'
																		+ '<option>'
																		+ 'Switch Box'
																		+ '</option>'
																		+ '<option>'
																		+ 'Supply Switch'
																		+ '</option>'
																		+ '</select>');

												// open the dialog
												$("#dialog_Resources").dialog(
														"open");
											});
							// }
						})
	}

	// creates dialog
	$("#dialog_Resources").dialog({
		autoOpen : false,
		draggable : true,
		minWidth : 700,
		minHeight : 300,
		dialogClass : 'no-close',
		clickOutside : true,
		clickOutsideTrigger : ".jstree-anchor"
	});

	// clickoutside closes dialog
	$("#dialog_Resources").bind('clickoutside', function() {
		$("#dialog_Resources").dialog('close');
	});
} // end showResources()

function setResource4Sensor(roomId, resourcePath) {
	$.post("/climate_station_servlet/setResource4Sensor?roomId=" + roomId
			+ "&resourcePath=" + resourcePath + "&sensor="
			+ $("#sensor_select").val());
}
