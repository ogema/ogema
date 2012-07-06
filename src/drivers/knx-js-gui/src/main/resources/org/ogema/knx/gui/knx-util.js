var servletPath = "/apps/ogema/knx";
var imagePath = "/ogema/knx"
	
function addDevice() {
	// gather data from fields ...
	var selectedInterface = $("#interfaces").val();
	if(!selectedInterface) {
		// TODO show error msg on page not via alert...
		alert("Please choose an interface");
		return false;
	}
	
	var name = $("#name").val();
	// TODO regex matching... from wicket page: name.add(new PatternValidator("[0-9a-zA-Z]+"));
	if(!name) {
		alert("Missing name!");
		return false;
	}
	
	var groupAddress = $("#groupAdr").val();
	// TODO regex matching... from wicket page: groupAdr.add(new PatternValidator("[0-9]+/[0-9]+/[0-9]+"));
	if(!groupAddress) {
		alert("Missing group address!");
		return false;
	}
	
	var physAddress = $("#physAdr").val();
	// TODO regex matching... from wicket page: groupAdr.add(new PatternValidator("[0-9]+/[0-9]+/[0-9]+"));
	if(!physAddress) {
		alert("Missing physical address!");
		return false;
	}
	
	var timeInterval = $("#time_int").val();
	if(!timeInterval || !(timeInterval >= 0) || !(timeInterval <= 100)) {
		alert("Invalid time interval! Allowed values: [0, 100]");
		return false;
	}
	
	var device = $("#devices").val();
	if(!device) {
		alert("Missing device!");
		return false;
	}
	
	$("#wait_overlay").show();
	sendPOST("add&selectedInterface=" + selectedInterface + "&name=" + name +"&groupAddress=" + groupAddress
			+ "&physicalAddress=" + physAddress + "&timeInterval=" + timeInterval + "&device=" + device, handleError)
	return false;
}

function sendPOST(params, successFunction) {
	var path = servletPath;
	if(params) {
		path += "?" + params
	}
    $.post(path, null, successFunction);
}

function getImage(img) {
	return imagePath + "/" + img;
}

function handleError(msg) {
	if(!!msg) {
		// msg is not empty, null or undefined
		console.log("handleError: " + msg);
		alert("Error occured: " + msg)
	} else {
		// no err occured ... reload page ...
		location.reload();
	}
	
	$("#wait_overlay").hide();
}

function searchInterfaces() {
	$("#wait_overlay").show();
	sendPOST("search", updateAvailableInterfacesPost, "json");
}

function updateAvailableInterfacesPost(availableInterfaces) {
	updateAvailableInterfaces(availableInterfaces);
	$("#wait_overlay").hide();
}

function updateAvailableInterfaces(availableInterfaces) {
	// if select item contained options before we will remove it here ...
	$('#interfaces').find('option').remove();
	
	$.each( availableInterfaces, function( i, item ) {
		$('#interfaces').append($('<option/>', { 
	        value: item,
	        text : item 
	    }));
	});
}

function updateAvailableTypes(availableTypes) {
	// if select item contained options before we will remove it here ...
	$('#devices').find('option').remove();
	
	$.each( availableTypes, function( i, item ) {
		$('#devices').append($('<option/>', { 
	        value: item,
	        text : item 
	    }));
	});
}

function updateConnectionInfos(connectionInfos) {
	// if tbody already contains connections we will remove them and then add all new connection infos
	$("#connectionTable > tbody").html("");
	
	$.each( connectionInfos, function( i, item ) {
		var tableRowString = "<tr><td>" + item.id + "</td><td>" + item.interfaceName
			+ "</td><td>" + item.knxRouter + "</td><td>" + item.groupAddress
			+ "</td><td>" + item.physicalAddress + "</td><td>" + item.name
			+ "</td><td>" + item.type + "</td><td>"
		$('#connectionTable > tbody:last').append(tableRowString);
	});
}