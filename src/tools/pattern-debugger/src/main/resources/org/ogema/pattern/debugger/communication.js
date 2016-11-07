var servletPath = "/org/ogema/tools/pattern-debugger/servlet";
//var appsServletPath = "/apps/ogema/framework/gui/installedapps?action=listAll";
// FIXME this will fail if the OTP is activated
var appsServletPath = "/install/installedapps"+"?action=listAll&user="+otusr+"&pw="+otpwd;

;
// callback should have two arguments, data and status
function sendGET(servlet,callback) {
    $.get(servlet, callback); 
}

function processGET(data, status) {
    alert("Data: " + data + "\nStatus: " + status);
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

function processPOST(data, status) {
    alert("Data: " + data + "\nStatus: " + status);
}

var buildModal = function(data, status) {
	if (typeof data === "string")
		data = JSON.parse(data);
	var patterns = data.patterns;
	var modal = $('#modal');
	var table = modal.find("#modal-table");
	var list="<tr><th>Condition</th><th>Path</th><th>Resource type</th><th>Optional</th><th>Satisfied</th><th>Exists</th><th>Active</th><th>Value</th><th>Access mode</th><th>Reference</th></tr>";
	Object.keys(patterns).forEach(function(path) {
		var obj =  patterns[path];
		var satisfied = obj.satisfied;
		var color = "style=\"color:" + (satisfied ? "green" : "red") + ";\"";
		list+="<tr " + color + "><td>" + obj.fieldName  + "</td><td>" + path  + "</td><td>" + obj.type + "</td><td>" + obj.optional + "</td><td>" + satisfied + "</td><td>" + 
			obj.exists+ "</td><td>" + obj.active + "</td><td>" + obj.value + "</td><td>" + obj.accessMode + "</td><td>" + obj.reference + "</td></tr>";
	});
	table.html(list); 
	var title = modal.find("#ModalLabel");
	title.html("Demanded model: " + data.demandedModel);
	var header = modal.find("#ModalHeader");
	header.html("Pattern type: " + data.type);
	modal.modal("show");
};

var getPatternInfo = function(pattern) {
	var app = $('#appSelector').val();
	var listener = $('#listenerSelector').val();
	if (listener === null || typeof listener === "undefined") {
		return;		
	}
	sendGET(servletPath + "?target=patternInfo&app=" + app + "&listener=" + listener + "&pattern=" + pattern, buildModal);
}

var buildPatternTable = function(data, status) {
	if (typeof data === "string")
		data = JSON.parse(data);
	var patterns = data.patterns;
	var tableHtml = "<tr><th>Demanded model</th><th>Resource type</th><th>Satisfied</th></tr>";
	var i;
	for (i=0;i<patterns.length;i++) {
		var pt = patterns[i];
		var satisfied =  pt.satisfied;
		//var color = "style=\"color:" + (satisfied ? "green" : "red") + ";\"";
		var img = "<img src=\"resources/" + (satisfied ? "Dialog-accept.svg" : "Dialog-error-round.svg" ) +  "\"/>";
		tableHtml += "<tr onclick=\"getPatternInfo('" + pt.path + "')\"><td>" + pt.path + "</td><td>" + pt.type + "</td><td>" + img + "</td></tr>";
	}
	var table = $('#patternTable');
	table.html(tableHtml);
	$('#demandedModelField').html(data.demandedModel);
};

function sendPatternRequest() {
	var app = $('#appSelector').val();
	var listener = $('#listenerSelector').val();
	if (listener === null || typeof listener === "undefined") {
		var table = $('#patternTable');
		table.html("");
		return;		
	}
	sendGET(servletPath + "?target=patterns&app=" + app + "&listener=" + listener,buildPatternTable);
}


