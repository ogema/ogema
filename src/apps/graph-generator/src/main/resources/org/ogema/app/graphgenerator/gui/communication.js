var servletPath = "/apps/ogema/graphgenerator";
var visJsNetwork = undefined;

function sendGET() {
    $.get(path, processGET); 
}

function processGET(data, status) {
    alert("Data: " + data + "\nStatus: " + status);
}

function writeAll() {
	var selectedGenerator = $('#generator').val();
	var successFunction = getSuccessFunctionForGenerator(selectedGenerator);
	var dataType = getExpectedDataType(selectedGenerator);
	var params = ["generator=" + selectedGenerator, "plottype=all"];
	if(selectedGenerator === "visjs") {
		var enablePhysConf = $('#phys_conf').is(':checked');
		params.push("enable_phys_conf=" + enablePhysConf);
	}
    sendPOST(servletPath, params, successFunction, dataType)
}

function destroyVisJsContainer() {
	if(!!visJsNetwork) {
		visJsNetwork.destroy();
	}
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
		tmp += "?";
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
	visJsNetwork = new vis.Network(graph, graphData, options);
}

function processPOST(data, status) {
    alert("Data: " + data + "\nStatus: " + status);
}
