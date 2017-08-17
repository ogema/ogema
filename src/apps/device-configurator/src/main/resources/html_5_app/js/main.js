var intface;
var llDrivers;
var hlDrivers = new Array;
var activeLLDriverIndex = -1;

$(document).ready(function() {
	getLLDrivers();
});

function getLLDrivers() {
	$.getJSON("/servlet/showAllLLDrivers?user=" + otusr + "&pw=" + otpwd, requestLLDrivers);
}

function requestLLDrivers(data) {
	llDrivers = data;
	for (var i = 0; i < data.length; i++) {
		if (data[i].llDriverId != null && data[i].tech != null) {
			var llDriverId = data[i].llDriverId;
			var tech = data[i].tech;
			$("#LLDrivers")
					.append(
							"<h1 id='LLHead_"
									+ i
									+ "' class='LL_tabsHeadline' onclick='showDriverContent("
									+ i + ")'>Low Level Driver:  " + llDriverId
									+ "   (" + tech + ")" + "</h1>");
		}
	}
	getHLDrivers();
}

function getHLDrivers() {
	$.getJSON("/servlet/showAllHLDrivers?user=" + otusr + "&pw=" + otpwd, requestHLDrivers);
}

function requestHLDrivers(data) {
	for (var b = 0; b < data.length; b++) {
		hlDrivers[b] = data[b].hlDriverId;
	}
}
// show/hide tab for the currently chosen LL driver and set activeLLDriverIndex
function showDriverContent(i) {
	var display = $("#LLDrivers_Content .tabsHeadline").css("display");
	if (display == "none") {
		$(".LL_tabsHeadline").css("display", "none");
		$("#LLHead_" + i).css("display", "block");
		$("#LLHead_" + i).addClass("driverChosen");
		activeLLDriverIndex = i;
		$("#LLDrivers_Content .tabsHeadline").show("fast");
		$("#resources").css("display", "none");
	} else {
		$("#LLHead_" + i).removeClass("driverChosen");
		activeLLDriverIndex = -1;
		$("#LLDrivers_Content .tabsHeadline").css("display", "none");
		$("#LLDrivers_Content .tabs").css("display", "none");
		$(".LL_tabsHeadline").css("display", "block");
	}
}

// Toggles clicked tab and closes all other tabs
function showTab(tabName) {
	var display = $("#" + tabName).css("display");
	var p = $("#" + tabName).prev();
	var pp = p.prev();
	var ppp = pp.prev();
	if (display == "block") {
		p.removeClass("active");
		$("#" + tabName).css("display", "none");
	} else {
		$(".tabs").css("display", "none");
		$(".tabsHeadline").removeClass("active");
		// toggle when ready doesnt work!
		var isReady = getData(tabName);

		p.addClass("active");
		$("#" + tabName).css("display", "block");
	}
}

// Toggling one tab initializes the request for data to fill it
function getData(dataRequested) {
	// dataRequested can be 'devices' or 'channels' depends on active tab

	// devices
	if (dataRequested == "devices") {
		$.getJSON("/servlet/showN?user=" + otusr + "&pw=" + otpwd + "&llDriverId="
				+ llDrivers[activeLLDriverIndex].llDriverId, showNetwork);
		// $.getJSON()
	}

	// channels
	if (dataRequested == "channels") {

		$.getJSON("/servlet/showACC?user=" + otusr + "&pw=" + otpwd + "&llDriverId="
				+ llDrivers[activeLLDriverIndex].llDriverId,
				function(data) {

					$("#channels").html("");

					for (var q = 0; q < data.length; q++) {
						$("#channels").append(
								"<br><b>channel: </b>" + data[q].channel);
					}
				});

	}

	// device scan
	if (dataRequested == "deviceScan") {

		$.getJSON("/servlet/scan?user=" + otusr + "&pw=" + otpwd + "&llDriverId="
				+ llDrivers[activeLLDriverIndex].llDriverId, function(data) {
			$("#deviceScan").html("");
			$("#deviceScan").append(data.status);
		});
	}

	//caching devices
		if (dataRequested == "cacheDevices") {

			$.getJSON("/servlet/cache?user=" + otusr + "&pw=" + otpwd + "&llDriverId="
					+ llDrivers[activeLLDriverIndex].llDriverId, function(data) {
						$("#cacheDevices").html("");
						$("#cacheDevices").append(data.status);
			});
		}

	// resources
	if (dataRequested == "resources") {

		$("#jsTree_Resources").html("");
		showResources();
	}

	return true;

} // end function getData(dataRequested)

function showNetwork(data) {

	$("#devices").html(
			"<span class='techHead'>Devices on " + data.driverId + "</span>");

	// each connection
	for (var x = 0; x < data.busses.length; x++) {

		// each device
		if (data.busses[x].devices != null) {
			var countDev = data.busses[x].devices.length;

			for (var i = 0; i < countDev; i++) {
				// $("#devices").addClass(data.busses[x].interfaceName);
				$("#devices")
						.append(
								"<div id='dev"
										+ i
										+ "' class='eachDevice'><h1 id='head"
										+ i
										+ "' class='headDevices' onclick='showDeviceInformation("
										+ i
										+ ")'>"
										+ "Name: "
										+ data.busses[x].devices[i].deviceName
										+ "  Nw-Addr: "
										+ data.busses[x].devices[i].networkAddress
										+ "  MAC: "
										+ data.busses[x].devices[i].physicalAddress
										+ "</h1> <div id='cont"
										+ i
										+ "' class='contentDevices'> </div> </div>");

				$("#cont" + i).append(
						"<div id='detInfo" + i + "' class='detailInformation'>"
								+ "<div id='detailDia_" + i
								+ "' class='detailDialog'></div>"
								+ "<button onclick='showDeviceDetails(" + i
								+ "," + '"' + data.driverId + '"' + "," + '"'
								+ data.busses[x].interfaceName + '"' + ","
								+ '"'
								+ data.busses[x].devices[i].physicalAddress
								+ '"' + ")'>showDeviceDetails</button>"
								+ "</div>");

				$("#detailDia_" + i).dialog({
					autoOpen : false,
					resizeable : true,
					draggable : true,
					minWidth : 430,
					minHeight : 450,
					width : 600,
					height : 450
				});

				$("#cont" + i).append(
						"<div id='genInfo" + i
								+ "' class='generalInformation'><b>Type:</b> "
								+ data.busses[x].devices[i].deviceType
								+ "<br><b>Initialized:</b> "
								+ data.busses[x].devices[i].initialized
								+ "<br><b>Manufacturer Code:</b> "
								+ data.busses[x].devices[i].manufacturerId
								+ " </div>");

				if (data.busses[x].devices[i].Endpoints != null) {
					var countEPoints = data.busses[x].devices[i].Endpoints.length;

					// each endpoint
					for (var l = 0; l < countEPoints; l++) {
						$("#cont" + i)
								.append(
										"<div id='devEPoint"
												+ i
												+ l
												+ "' class='devEndpoints'><b>Profile:</b> "
												+ data.busses[x].devices[i].Endpoints[l].Profile
												+ "<br><b>Endpoint:</b> "
												+ data.busses[x].devices[i].Endpoints[l].Endpoint
												+ "<br><b>DeviceID:</b> "
												+ data.busses[x].devices[i].Endpoints[l].DeviceID
												+ "<br></div>");

						// each cluster
						if (data.busses[x].devices[i].Endpoints[l].Cluster != null) {
							var countClusters = data.busses[x].devices[i].Endpoints[l].Cluster.length;

							for (var m = 0; m < countClusters; m++) {

								$("#devEPoint" + i + l)
										.append(
												"<div id='devEPointCluster"
														+ i
														+ l
														+ m
														+ "'>"
														+ "<br><b>Cluster: </b> <button type='button' onclick='append_showCD_Data("
														+ '"'
														+ data.busses[x].interfaceName
														+ '"'
														+ ","
														+ '"'
														+ data.busses[x].devices[i].physicalAddress
														+ '"'
														+ ","
														+ '"'
														+ data.busses[x].devices[i].Endpoints[l].Endpoint
														+ '"'
														+ ","
														+ '"'
														+ data.busses[x].devices[i].Endpoints[l].DeviceID
														+ '"'
														+ ","
														+ '"'
														+ data.busses[x].devices[i].Endpoints[l].Cluster[m].ID
														+ '"'
														+ ","
														+ '"'
														+ "devEPointCluster"
														+ i
														+ l
														+ m
														+ 'data'
														+ '"'
														+ ","
														+ '"'
														+ i
														+ '"'
														+ ","
														+ '"'
														+ l
														+ '"'
														+ ","
														+ '"'
														+ m
														+ '"'
														+ ")'>"
														+ data.busses[x].devices[i].Endpoints[l].Cluster[m].ID
														+ ' - '
														+ data.busses[x].devices[i].Endpoints[l].Cluster[m].name
														+ "</button>"
														+ "</div>");

								$("#devEPointCluster" + i + l + m).append(
										"<div id='devEPointCluster" + i + l + m
												+ 'data' + "'>" + "</div>");
							}// end cluster
						} // end cluster
					} // end endpoint
				}
			}
		}
	}// end connection
}

function showResources() {
	// destroy old tree and build new one
	$("#jsTree_Resources").jstree("destroy");

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
							"url" : "/service/resourceview?user="+otusr+"&pw="+otpwd,
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
							/*
							 * if (!$("#jsTree_Resources").jstree("is_parent",
							 * data.node))
							 */{

								$
										.getJSON(
												"/service/resourcevalue?id="
														+ currentNode + "&user=" + otusr + "&pw=" + otpwd,
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
													$("#dialog_Resources")
															.parent()
															.find(
																	".ui-dialog-title")
															.text(
																	"Resource Details");

													// fill dialog with the
													// value of the node and add
													// button
													if (fullNode.readOnly == "true") {
														$("#dialog_Resources")
																.html(
																		'<br><span style="width:110px;display:block;float:left;"><b>Name: </b></span>'
																				+ fullNode.path
																				+ '<br><span style="width:110px;display:block;float:left;"><b>Type: </b></span>'
																				+ fullNode.type
																				+ '<br><span style="width:110px;display:block;float:left;"><b>Created by: </b></span>'
																				+ fullNode.owner
																				+ "<br>"
																				+ '<br><span style="width:110px;display:block;float:left;"><b>Reference: </b></span>'
																				+ fullNode.reference
																				+ "<br>"
																				+ '<br><span style="width:110px;display:block;float:left;"><b>Value: </b></span>'
																				+ fullNode.value
																				+ '<br><span style="width:110px;display:block;float:left;"><b>New Value: </b></span>'
																				+ '<input style="background-color: #cccccc;" type="text" id="'
																				+ 'inputnode'
																				+ '" disabled>'
																				+ '<br><button type="button" disabled onclick="writeResource('
																				+ data.node.id
																				+ ')">Write</button>');
													} else {
														$("#dialog_Resources")
																.html(
																		'<br><span style="width:110px;display:block;float:left;"><b>Name: </b></span>'
																				+ fullNode.path
																				+ '<br><span style="width:110px;display:block;float:left;"><b>Type: </b></span>'
																				+ fullNode.type
																				+ '<br><span style="width:110px;display:block;float:left;"><b>Created by: </b></span>'
																				+ fullNode.owner
																				+ "<br>"
																				+ '<br><span style="width:110px;display:block;float:left;"><b>Reference: </b></span>'
																				+ fullNode.reference
																				+ "<br>"
																				+ '<br><span style="width:110px;display:block;float:left;"><b>Value: </b></span>'
																				+ fullNode.value
																				+ '<br><span style="width:110px;display:block;float:left;"><b>New Value: </b></span>'
																				+ '<input type="text" id="'
																				+ 'inputnode'
																				+ '">'
																				+ '<br><button type="button" onclick="writeResource('
																				+ data.node.id
																				+ ')">Write</button>');
													}

													// open the dialog
													$("#dialog_Resources")
															.dialog("open");
												});
							}
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

function append_showCD_Data(interfaceId, device, endpoint, device_id,
		clusterId, div_id, i, l, m) {

	$("#" + div_id).html("");

	$
			.getJSON(
					"/servlet/showCD?user=" + otusr + "&pw=" + otpwd + "&llDriverId="
							+ llDrivers[activeLLDriverIndex].llDriverId
							+ "&interfaceId=" + interfaceId + "&device="
							+ device + "&endpoint=" + endpoint + "&clusterId="
							+ clusterId,
					function(command_attribute_data) {

						// set cluster name
						$("#" + div_id).append(
								command_attribute_data.ClusterName);

						// each command
						if (command_attribute_data.Commands != null) {
							$("#" + div_id).append("<br><b>Commands:</b> ");
							var countCommands = command_attribute_data.Commands.length;

							for (var n = 0; n < countCommands; n++) {
								$("#" + div_id)
										.append(
												"<div id='"
														+ i
														+ l
														+ m
														+ "command_dialog"
														+ n
														+ "' title='command: "
														+ command_attribute_data.Commands[n].Name
														+ "'</div>");

								$("#" + i + l + m + "command_dialog" + n)
										.dialog({
											autoOpen : false,
											resizeable : true,
											draggable : true,
											minWidth : 430,
											minHeight : 450,
											width : 600,
											height : 450
										});

								// construct string for select with all hl
								// driver included and use it in the appends
								if (hlDrivers != null) {
									var select_string = '<option selected="selected">'
											+ hlDrivers[0] + '</option>';
									for (var y = 1; y < hlDrivers.length; y++) {
										select_string = select_string
												+ '<option>' + hlDrivers[y]
												+ '</option>'
									}
								}

								$("#" + i + l + m + "command_dialog" + n).html(
										"");

								$("#" + i + l + m + "command_dialog" + n)
										.append(
												"<br>Device: "
														+ device
														+ "<br>Endpoint: "
														+ endpoint
														+ "<br>Cluster:"
														+ clusterId
														+ "<br><br>select HL driver:   "
														+ '<br><select id="'
														+ 'select'
														+ i
														+ l
														+ m
														+ n
														+ '">'
														+ select_string
														+ '</select>'
														+ "<br><br><button type='button' onclick='createChannel("
														+ '"'
														+ interfaceId
														+ '"'
														+ ","
														+ '"'
														+ device
														+ '"'
														+ ","
														+ '"'
														+ device_id
														+ '"'
														+ ","
														+ '"'
														+ clusterId
														+ ':Command:'
														+ command_attribute_data.Commands[n].Identifier
														+ '"'
														+ ","
														+ '"'
														+ 0
														+ '"'
														+ ","
														+ '"'
														+ "resource"
														+ i
														+ l
														+ m
														+ n
														+ '"'
														+ ","
														+ '"'
														+ endpoint
														+ '"'
														+ ","
														+ '"'
														+ 'select'
														+ i
														+ l
														+ m
														+ n
														+ '"'
														+ ")'>create channel</button>"
														+ "<br><button type='button' onclick='deleteChannel("
														+ '"'
														+ interfaceId
														+ '"'
														+ ","
														+ '"'
														+ device
														+ '"'
														+ ","
														+ '"'
														+ clusterId
														+ ':Command:'
														+ command_attribute_data.Commands[n].Identifier
														+ '"'
														+ ","
														+ '"'
														+ endpoint
														+ '"'
														+ ","
														+ '"'
														+ 'select'
														+ i
														+ l
														+ m
														+ n
														+ '"'
														+ ")'>delete channel</button>"
														+ "<br><button type='button' onclick='writeChannel("
														+ '"'
														+ interfaceId
														+ '"'
														+ ","
														+ '"'
														+ device
														+ '"'
														+ ","
														+ '"'
														+ clusterId
														+ ':Command:'
														+ command_attribute_data.Commands[n].Identifier
														+ '"'
														+ ","
														+ '"'
														+ endpoint
														+ '"'
														+ ","
														+ '"'
														+ 'input'
														+ i
														+ l
														+ m
														+ n
														+ '"'
														+ ","
														+ '"'
														+ 'select'
														+ i
														+ l
														+ m
														+ n
														+ '"'
														+ ")'>write channel</button>"
														+ ' value for write channel: <input type="text" id="'
														+ 'input' + i + l + m
														+ n + '">');

								$("#" + 'select' + i + l + m + n).selectmenu();

								$("#" + div_id)
										.append(
												"<br><button type='button' "
														+ "onclick='"
														+ '$("#'
														+ i
														+ l
														+ m
														+ 'command_dialog'
														+ n
														+ '").dialog("open")'
														+ "'"
														+ ">"
														+ command_attribute_data.Commands[n].Name
														+ "</button>");
							}
						} // end command

						// each attribute
						if (command_attribute_data.Attributes != null) {
							$("#" + div_id).append("<br><b>Attibutes:</b> ");
							var countAttributes = command_attribute_data.Attributes.length;

							for (var p = 0; p < countAttributes; p++) {
								$("#" + div_id)
										.append(
												"<div id='"
														+ i
														+ l
														+ m
														+ "attribute_dialog"
														+ p
														+ "' title='attribute: "
														+ command_attribute_data.Attributes[p].Name
														+ "'</div>");

								$("#" + i + l + m + "attribute_dialog" + p)
										.dialog({
											autoOpen : false,
											resizeable : true,
											draggable : true,
											minWidth : 430,
											minHeight : 450,
											width : 600,
											height : 450
										});

								$("#" + i + l + m + "attribute_dialog" + p)
										.html("");

								$("#" + i + l + m + "attribute_dialog" + p)
										.append(
												"<br>Device: "
														+ device
														+ "<br>Endpoint: "
														+ endpoint
														+ "<br>Cluster:"
														+ clusterId
														+ "<br><br>select HL driver:   "
														+ '<br><select id="'
														+ 'select_attr'
														+ i
														+ l
														+ m
														+ p
														+ '">'
														+ select_string
														+ '</select>'
														+ "<br><br><button type='button' onclick='createChannel("
														+ '"'
														+ interfaceId
														+ '"'
														+ ","
														+ '"'
														+ device
														+ '"'
														+ ","
														+ '"'
														+ device_id
														+ '"'
														+ ","
														+ '"'
														+ clusterId
														+ ':Attribute:'
														+ command_attribute_data.Attributes[p].Identifier
														+ '"'
														+ ","
														+ '"'
														+ 0
														+ '"'
														+ ","
														+ '"'
														+ "resource_attr"
														+ i
														+ l
														+ m
														+ p
														+ '"'
														+ ","
														+ '"'
														+ endpoint
														+ '"'
														+ ","
														+ '"'
														+ 'select_attr'
														+ i
														+ l
														+ m
														+ p
														+ '"'
														+ ")'>create channel</button>"
														+ "<br><button type='button' onclick='deleteChannel("
														+ '"'
														+ interfaceId
														+ '"'
														+ ","
														+ '"'
														+ device
														+ '"'
														+ ","
														+ '"'
														+ clusterId
														+ ':Attribute:'
														+ command_attribute_data.Attributes[p].Identifier
														+ '"'
														+ ","
														+ '"'
														+ endpoint
														+ '"'
														+ ","
														+ '"'
														+ 'select_attr'
														+ i
														+ l
														+ m
														+ p
														+ '"'
														+ ")'>delete channel</button>"
														+ "<br><button type='button' onclick='writeChannel("
														+ '"'
														+ interfaceId
														+ '"'
														+ ","
														+ '"'
														+ device
														+ '"'
														+ ","
														+ '"'
														+ clusterId
														+ ':Attribute:'
														+ command_attribute_data.Attributes[p].Identifier
														+ '"'
														+ ","
														+ '"'
														+ endpoint
														+ '"'
														+ ","
														+ '"'
														+ 'input_attr'
														+ i
														+ l
														+ m
														+ p
														+ '"'
														+ ","
														+ '"'
														+ 'select_attr'
														+ i
														+ l
														+ m
														+ p
														+ '"'
														+ ")'>write channel</button>"
														+ ' value for write channel: <input type="text" id="'
														+ 'input_attr'
														+ i
														+ l
														+ m
														+ p
														+ '">'
														+ "<br><button type='button' onclick='readChannel("
														+ '"'
														+ interfaceId
														+ '"'
														+ ","
														+ '"'
														+ device
														+ '"'
														+ ","
														+ '"'
														+ clusterId
														+ ':Attribute:'
														+ command_attribute_data.Attributes[p].Identifier
														+ '"'
														+ ","
														+ '"'
														+ endpoint
														+ '"'
														+ ","
														+ '"'
														+ 'input_attr_readonly'
														+ i
														+ l
														+ m
														+ p
														+ '"'
														+ ","
														+ '"'
														+ 'select_attr'
														+ i
														+ l
														+ m
														+ p
														+ '"'
														+ ")'>read channel</button>"
														+ ' value read: <input type="text" id="'
														+ 'input_attr_readonly'
														+ i + l + m + p + '"'
														+ 'value="" readonly>');

								$("#" + div_id)
										.append(
												"<br><button type='button' "
														+ "onclick='"
														+ '$("#'
														+ i
														+ l
														+ m
														+ 'attribute_dialog'
														+ p
														+ '").dialog("open")'
														+ "'"
														+ ">"
														+ command_attribute_data.Attributes[p].Name
														+ "</button>");
							}
						} // end attribute

					}); // end $.getJSON()
} // end append_showCD_Data()

function writeResource(resourceId) {
	$.post("/service/writeresource?user=" + otusr + "&pw=" + otpwd + "&resourceId=" + resourceId + "&"
			+ "writeValue=" + $("#inputnode").val());
}

function createChannel(interfaceId, device, device_id, channel, timeout, name,
		endpoint, select_id) {

	var config_data_json = '{"interfaceId":' + '"' + interfaceId + '"' + ","
			+ '"deviceAddress":' + '"' + device + ':' + endpoint + '"' + ","
			+ '"channelAddress":' + '"' + channel + '"' + "," + '"timeout":'
			+ '"' + timeout + '"' + "," + '"resourceName":' + '"' + name + '"'
			+ "," + '"deviceID":' + '"' + device_id + '"' + '}'

	$.post("/servlet/createC?user=" + otusr + "&pw=" + otpwd + "&hlDriverId=" + $("#" + select_id).val(), {
		config_data_json : "" + config_data_json
	});
}

function deleteChannel(interfaceId, device, channel, endpoint, select_id) {

	var config_data_json = '{"interfaceId":' + '"' + interfaceId + '"' + ","
			+ '"deviceAddress":' + '"' + device + ':' + endpoint + '"' + ","
			+ '"channelAddress":' + '"' + channel + '"' + '}'

	$.post("/servlet/deleteC?user=" + otusr + "&pw=" + otpwd+ "&hlDriverId=" + $("#" + select_id).val(), {
		config_data_json : "" + config_data_json
	});
}

function writeChannel(interfaceId, device, channel, endpoint, input_id,
		select_id) {

	var config_data_json = '{"interfaceId":' + '"' + interfaceId + '"' + ","
			+ '"deviceAddress":' + '"' + device + ':' + endpoint + '"' + ","
			+ '"channelAddress":' + '"' + channel + '"' + "," + '"writeValue":'
			+ '"' + $("#" + input_id).val() + '"' + '}'

	$.post("/servlet/writeC?user=" + otusr + "&pw=" + otpwd+ "&hlDriverId=" + $("#" + select_id).val(), {
		config_data_json : "" + config_data_json
	});
}

function readChannel(interfaceId, device, channel, endpoint,
		input_attr_readonly_id, select_id) {

	var config_data_json = '{"interfaceId":' + '"' + interfaceId + '"' + ","
			+ '"deviceAddress":' + '"' + device + ':' + endpoint + '"' + ","
			+ '"channelAddress":' + '"' + channel + '"' + '}'

	$.getJSON("/servlet/readC?user=" + otusr + "&pw=" + otpwd+ "&hlDriverId=" + $("#" + select_id).val(), {
		config_data_json : "" + config_data_json
	}, function(data) {
		$("#" + input_attr_readonly_id).val(data.value);
	});
}

function showDeviceDetails(i, driverId, interface, address) {

	$("#detailDia_" + i).html("");
	$("#detailDia_" + i).append("No device details available at present.")
	$("#detailDia_" + i).dialog("open");
}

function chooseHLDriver(id, driverChosen) {
	var pos1 = id.indexOf("_");
	// ZB or HM
	var tech = id.slice(0, pos1);
	var newID = id.slice(pos1 + 1);
	var pos2 = newID.indexOf("_");
	// attr or comm
	var type = newID.slice(0, pos2);
	var veryNewID = newID.slice(pos2 + 1);
	var pos3 = veryNewID.indexOf("drivers");
	var idNumber = veryNewID.slice(pos3 + 7);

	var numberForDeviceAdd = idNumber.slice(0, 1);
	var deviceAdd = $("#" + tech + "_head" + numberForDeviceAdd).text();

	$.getJSON("/servlet/showCC?user=" + otusr + "&pw=" + otpwd+ "&hlDriverid=" + driverChosen + "&deviceAddress="
			+ deviceAdd, function(json) {
		console.log(json);
		$("#" + tech + "_chanStatus_" + idNumber).html(bspStatus);
	});

	// solange Serverseite noch nicht funktioniert
	var bspStatus = "Exists";
	$("#" + tech + "_chanStatus_" + idNumber).html(bspStatus);
}

// toggles device information for ZB devices and closes others
function showDeviceInformation(idNum) {
	var display = $("#cont" + idNum).css("display");
	if (display == "block") {
		$("#head" + idNum).removeClass("active");
		$("#cont" + idNum).css("display", "none");
	} else {
		$(".contentDevices").css("display", "none");
		$(".headDevices").removeClass("active");
		$("#head" + idNum).addClass("active");
		$("#cont" + idNum).css("display", "block");
	}
}
