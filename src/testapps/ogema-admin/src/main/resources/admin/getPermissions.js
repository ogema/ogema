//get permissions from server for the given url
function getPerms(json, appName, appNumber, diaClass) {
	var numberToIdentify;
	if (diaClass == 'uploadDia') {
		var diaId = "#dialUpload";
		uploadApp = appName;
		numberToIdentify = 2;
		$(diaId).parent().find(".ui-dialog-titlebar .ui-dialog-title").text("Permissions for " + appName);
	} else {
		var diaId = "#dialog" + appNumber;
		numberToIdentify = 1;
	}
	// clear the dialog before for-loop
	$(diaId + " form").empty();
	// loops through each permission-string
	for (var i = 0; i < json.localePerms.length; i++) {
		// get the single elements of each permission
		// get permission name
		var permName = getPermName(json.localePerms[i]);
		// get permission resource
		var permResource = getPermResource(json.localePerms[i]);
		// get permission method
		var permMethod = getPermMethod(json.localePerms[i]);
		// write elements in dialog
		writeInDia(diaId, permName, permResource, permMethod, i, appNumber);
	}
	
	$(diaId + " form").append(
			"<button onclick='writeEndDia(" + appNumber + ", " + "" + numberToIdentify + "" 
					+ ")' type='button'>Add Permission</button>");

	// adding an icon to the AddPermission-button
	$(diaId + " form > button").button({
		icons : {
			primary : "ui-icon-plus"
		}
	});
}