function showResources(roomId) {
	// destroy old tree and build new one
	$("#jsTree_Resources").jstree("destroy");
	$("#jsTree_Resources").html("");

	if (!($("#jsTree_Resources").hasClass("jstree"))) {
		$("#jsTree_Resources").jstree({
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
		}).bind("select_node.jstree", function(event, data) {
			selectNode(event, data, roomId);
		})
	}

	// creates dialog
	$("#dialog_Resources").dialog({
		autoOpen : false,
		resizable : false,
		modal : true,
		width : 'auto',
		dialogClass : 'no-close',
		clickOutside : true,
		clickOutsideTrigger : ".jstree-anchor"
	});

	// clickoutside closes dialog
	$("#dialog_Resources").bind('clickoutside', function() {
		$("#dialog_Resources").dialog('close');
	});
} // end showResources()

function selectNode(event, data, roomId) {
	// close the resource-dialog
	$("#dialog_Resources").dialog("close");

	// id of selected node
	var currentNode = data.node.id;
	// get the node by id
	var currentSelectedResourceNode;
	currentSelectedResourceNode = $("#jsTree_Resources").jstree(true).get_node(currentNode);

	// check if the current selected node is NOT a
	// parent node i.e. has NO children
	// if (!$("#jsTree_Resources").jstree("is_parent",
	// data.node)) {

	$.getJSON("/service/resourcevalue?id=" + currentNode, function(data) {
		resourceDialog(data, currentNode, roomId);
	});
	// }
}

function resourceDialog(json, currentNode, roomId) {

	var curNodeId;
	var fullNode;
	var dataLen = json.length;
	for (var i = 0; i < dataLen; i++) {
		curNodeId = json[i].id;
		if (curNodeId == currentNode) {
			fullNode = json[i];
		}
	}

	// set title of dialog box
	// $("#dialog_Resources").parent().find(".ui-dialog-title").text(currentSelectedResourceNode.text);
	$("#dialog_Resources").parent().find(".ui-dialog-title").text("Resource Details");

	// var resourcePath = fullNode.path;

	$.getJSON("/rcsservice/matchType2Sensors?type=" + fullNode.type, function(data) {
		createDialog(data, roomId, fullNode);
	});
	// fill dialog with the value of
	// the node and add button
}

function createDialog(data, roomId, fullNode) {
	var reference = fullNode.reference;

	var unit = '';
	if (fullNode.unit != undefined)
		unit = fullNode.unit;
	if (reference != undefined)
		reference = '<span class="resDialog"><b>References to: </b></span>' + reference;
	else
		reference = "";

	var setButton = "<br><button type='button' onclick='setResource4Sensor(" + '"' + roomId + '"' + "," + '"' + fullNode.path + '"'
			+ ")'>Set as Room Sensor For ...</button>";

	var resetButton = "<button align='center' type='button' onclick='resetRoomSensors(" + '"' + roomId + '"' + ")'>Reset Room Sensors</button>";

	var dialogContent = "<br>Selected Resource:" + '<br><br><span class="resDialog"><b>Name: </b></span>' + fullNode.path
			+ '<br><span class="resDialog"><b>Type: </b></span>' + fullNode.type + "<br>" + '<br>' + reference + "<br>"
			+ '<br><span class="resDialog"><b>Value: </b></span>' + fullNode.value + ' ' + unit + "<br>" + setButton + resetButton + '<br><select id="'
			+ 'sensor_select' + '">';

	var i = 0;
	for ( var entry in data) {
		dialogContent += '<option>';
		dialogContent += data[i++];
		dialogContent += '</option>';
	}
	dialogContent += '</select>';

	$("#dialog_Resources").html(dialogContent);
	// open the dialog
	$("#dialog_Resources").dialog("open");

}

function setResource4Sensor(roomId, resourcePath) {
	$.post("/rcsservice/setResource4Sensor?roomId=" + roomId + "&resourcePath=" + resourcePath + "&sensor=" + $("#sensor_select").val(), function(data) {
		alert(data);
	});
}

function resetRoomSensors(roomId) {
	$.post("/rcsservice/resetRoomSensors?roomId=" + roomId, function(data) {
		alert(data);
	});
}
