var servletPath = "/apps/ogema/graphgenerator?user=" + otusr + "&pw=" + otpwd;
var visJsNetwork = undefined;

// callback should have two arguments, data and status
function sendGET(callback) {
    $.get(servletPath, callback);
}

function processGET(data, status) {
    alert("Data: " + data + "\nStatus: " + status);
}

function writeAll() {
	var selectedGenerator = $('#generator').val();
	var selectedType = $('#resourceType').val();
	if (typeof selectedType==='undefined' || selectedType.length === 0) {
		console.warn("Could not determine ResourceType");
		selectedType = "org.ogema.core.model.Resource";
	}
	// FIXME
	console.log("Sending a request for type " + selectedType);
	var successFunction = getSuccessFunctionForGenerator(selectedGenerator);
	var dataType = getExpectedDataType(selectedGenerator);
	var params = ["generator=" + selectedGenerator, "plottype=all", "resourceType=" + selectedType];
	if(selectedGenerator === "visjs") {
		var enablePhysConf = $('#phys_conf').is(':checked');
		params.push("enable_phys_conf=" + enablePhysConf);
	}
    sendPOST(servletPath, params, successFunction, dataType);
}

function destroyVisJsContainer() {
	try {
		if(!!visJsNetwork) {
			visJsNetwork.destroy();
		}
	}catch (e) {}
}

function writeConnections() {
	var selectedGenerator = $('#generator').val();
	var successFunction = getSuccessFunctionForGenerator(selectedGenerator);
	var dataType = getExpectedDataType(selectedGenerator);
	var params = ["generator=" + selectedGenerator, "plottype=all"];
	if(selectedGenerator === "visjs") {
		params.push("enable_phys_conf=" + $('#phys_conf').is(':checked'));
	}
	sendPOST(servletPath, params, successFunction, dataType)
}

function getSuccessFunctionForGenerator(generator) {
	if(generator === "visjs") {
		return drawVisJsGraph;
	} else {
		return undefined;
	}
}

function getExpectedDataType(generator) {
	if(generator === "visjs") {
		return "json";
	} else {
		return "text";
	}
}

function sendPOST(path, params, successFunction, dataType) {
	var tmp = path;
	if (typeof params !== 'undefined' && params.length > 0) {
	    // the array is defined and has at least one element
		tmp += "&";
		for	(index = 0; index < params.length; index++) {
		    tmp += params[index];
		    if(index+1 < params.length) {
		    	tmp += "&";
		    }
		}
	}

    $.post(tmp, null, successFunction, dataType);
}

function drawVisJsGraph(data, status) {
	destroyVisJsContainer();
	var graph = document.getElementById('resource_graph');
	var graphData = {
			nodes: data.nodes,
			edges: data.edges
	};
	var options = data.options;
	try {
		visJsNetwork = new vis.Network(graph, graphData, options);
	} catch (e) {
		console.log("Error initialising visJS",e);
	}
}

function processPOST(data, status) {
    alert("Data: " + data + "\nStatus: " + status);
}
