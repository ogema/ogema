function checkingInput(obj) {

	var boxChecked;
	var textFilled;

	// all headers of single permissions
	var identifyCheck = "#" + obj[0].id + " form > div div:first-child";

	// all headers of single permissions, that contain a checked checkbox
	var checkedPerms = $(identifyCheck).find("input[type='checkbox']:checked");

	// count checked permissions
	for (var i = 0; i < ($(identifyCheck).find("input[type='checkbox']:checked").length); i++) {

		// find all content-checkboxes, if there are more than 0, then..
		if ($("#" + checkedPerms[i].id).parent().parent().find("div:last-child input:checkbox:visible").length > 0) {
			if ($("#" + checkedPerms[i].id).parent().parent().find("div:last-child input:checkbox:checked").length > 0) {
				boxChecked = true;
			} else {
				// if not even one box is checked
				boxChecked = false;
				alert("At least one box must be checked.");
			}
		} else {
			boxChecked = true;
		}

		// find content-text-input, if its not empty, then...
		if ($("#" + checkedPerms[i].id).parent().parent().find("div:last-child input[type='text']:visible").length > 0) {
			if ($("#" + checkedPerms[i].id).parent().parent().find("div:last-child input[type='text']").val() != "") {
				textFilled = true;
			} else {

				// if it is an additional permission, resource or methods
				// dont
				// have to be given
				if ($("#" + checkedPerms[i].id).parent().parent().parent().find("div.additional").length > 0) {
					textFilled = true;

				} else { // if text-input is empty
					textFilled = false;
					alert("You have to insert a ressource in text-input.");
				}
			}
		} else {
			textFilled = true;
		}

		// if not checkboxes ok and text-input ok
		if (!(boxChecked == true && textFilled == true)) {
			// return false causes end of this function --> boolean in
			// SAVE-button function
			return false;
		}
	}
	// return true causes end of this function --> boolean in SAVE-button
	// function
	return true;
}

/**
 * Check if it is an AccessPerm. If it is, describe what it does to the system.
 * 
 * @param {String}
 *            name Name of the permission. return {boolean}
 */
function checkForResourcePerms(name) {
	if (name.indexOf(".ResourcePermission") != -1) {
		return true;
	}
}

// checking if the text typed in is a ressource
/**
 * Check resource. There must be a resource plus Validation.
 * 
 * @param {Object}
 *            $(this) Current dialog
 * @return {boolean} True for okay. False if something is wrong.
 */
function checkingRessourceInput(obj) {
	var identifyCheck = "#" + obj[0].id + " form > div div:first-child";
	var checkedPerms = $(identifyCheck).find("input[type='checkbox']:checked");

	for (var i = 0; i < ($(identifyCheck).find("input[type='checkbox']:checked").length); i++) {
		// all headers of single permissions, that contain a checked
		// checkbox
		var checkedPerms = $(identifyCheck).find("input[type='checkbox']:checked");

		var textInput = $("#" + checkedPerms[i].id).parent().parent().find("div:last-child input[type='text']").val();
	}
	return true;
}

/**
 * Send permissions to server for current app.
 * 
 * @param {String}
 *            currentAppName Chosen app-name.
 * @param {String}
 *            permContentForApp Completed JSON-PermissionString for the server.
 */
function sendPermsToServer(appid, permContentForApp) {

	$.post("/security/config/permissions?id=" + appid, {
		permission : permContentForApp
	}, function(data, status) { // if successfull
		alert("Data send to server for appID: " + appid + "\nResponse: " + data + "\nStatus: " + status);
	}).fail(function(xhr, textStatus, errorThrown) {
		// if http-post fails
		if (textStatus != "" && errorThrown != "") {
			alert("Somthing went wrong: " + textStatus + "\nError: " + errorThrown);
		} else {
			alert("Error.");
		}
	});
}

/**
 * get chosen permissions and transform them into a JSON-String for sending to
 * server. Executed when SAVE-button is clicked.
 * 
 * @param {Object}
 *            obj Current dialog. return {String} finalJSON JSON-String of the
 *            chosen permissions.
 */
// function getDemandBack(obj) {
// var recursive = false;
// var pName = "";
// var pRessource = "";
// var pMethod;
// var pCondition;
// var pCondArgs;
// var countChecked = $("form > div div:first-child
// input:first-child:checked").length;
// var arrayOfChecked = $("form > div div:first-child
// input:first-child:checked");
// var permStringArray = [];
// // var resPermResources = "";
// // var idNumOnly;
//
// for (var x = 0; x < countChecked; x++) {
// pName = "";
// pRessource = "";
// pMethod = "";
// pCondition = "";
// pCondArgs = "";
// // resPermResources = "";
// if ($("form > div obj div:first-child input").prop("checked", true)) {
// var eachId = arrayOfChecked[x].id;
// var selector = "input#" + eachId;
// var thisNext = $(selector).next();
// var parentLabel = $(selector).parent().find("label");
// var parentInputText = $(selector).parent().find("input[type='text']");
// var divLastChild = $(selector).parent().parent().find("div:last-child");
// var parentNextDivConds = $(selector).parent().next().find("div.conditions");
//
// // if only one label in head (label is name)
// if (parentLabel.length == 1) {
// pName = parentLabel.text();
// } else if (parentLabel.length == 0) { // if
// // no
// // label
// // (text-input
// // is
// // name)
// pName = parentInputText.val();
// } else if (parentLabel.length > 1) { // if
// // more
// // than
// // one
// // label
// // (additional
// // perm,
// // first
// // label
// // is
// // name)
// // grant or deny checked
// pName =
// $(selector).parent().find("input:checkbox:checked").next("label").text();
// // get name of text-input
// pName = pName + parentInputText.val();
// }
// pRessource = divLastChild.find("input[type='text']").val();
// if (divLastChild.find("input[type='checkbox']").length > 0) {
// pMethod = divLastChild.find("input:checkbox:checked").map(function() {
// return $(this).next("label").text().split(' ');
// });
// } else {
// pMethod = divLastChild.find("input[type='text']").next().next().next().val();
// }
// if (parentNextDivConds.length == 1) {
// pCondition = parentNextDivConds.find("input[type='text']").val();
// pCondArgs = parentNextDivConds.find("input:last-child").val();
// }
//
// if (thisNext.text().indexOf("ResourcePermission") != -1) {
// var treeId = eachId.replace("c", "");
// // resPermResources = getResources(idNumOnly);
// // var checkedLen = $("#testTree" +
// // idNumOnly).jstree("get_selected").length;
// var policy = transformWithResources(pName, pMethod, pCondition, pCondArgs, /*
// resPermResources, */"#testTree" + treeId);
// // if (checkedLen != 0) {
// if (policy != "") {
// permStringArray[x] = policy;
// } else {
// permStringArray[x] = transformInput(pName, pRessource, pMethod, pCondition,
// pCondArgs);
// }
// } else {
// permStringArray[x] = transformInput(pName, pRessource, pMethod, pCondition,
// pCondArgs);
// }
// }
// }
//
// // create JSON-form
// var finalJSON = '{"permissions":[' + permStringArray + ']}';
//
// // reset
// // eachId = [];
// // countChecked = 0;
// // permStringArray = [];
// //
// return finalJSON;
// }
function getGrantedBack(obj) {
	var recursive = false;
	var pName = "";
	var pRessource = "";
	var pMethod;
	var pCondition;
	var pCondArgs;
	var countChecked = $("form > div div:first-child input:first-child:checked").length;
	var arrayOfChecked = $("form > div div:first-child input:first-child:checked");
	var permStringArray = [];

	for (var x = 0; x < countChecked; x++) {
		pName = "";
		pRessource = "";
		pMethod = "";
		pCondition = "";
		pCondArgs = "";
		if ($("form > div obj div:first-child input").prop("checked", true)) {
			var eachId = arrayOfChecked[x].id;
			var selector = "input#" + eachId;
			var thisNext = $(selector).next();
			var parentLabel = $(selector).parent().find("label");
			var parentInputText = $(selector).parent().find("input[type='text']");
			var divLastChild = $(selector).parent().parent().find("div:last-child");
			var parentNextDivConds = $(selector).parent().next().find("div.conditions");

			// if only one label in head (label is name)
			if (parentLabel.length == 1) {
				pName = parentLabel.text();
			} else if (parentLabel.length == 0) { // if
				// no label (text-input is name)
				pName = parentInputText.val();
			} else if (parentLabel.length > 1) { // if
				// more than one label (additional perm, first label is name)
				// grant or deny checked
				pName = $(selector).parent().find("input:checkbox:checked").next("label").text();
				// get name of text-input
				pName = pName + parentInputText.val();
			}
			pRessource = divLastChild.find("input[type='text']").val();
			if (divLastChild.find("input[type='checkbox']").length > 0) {
				pMethod = divLastChild.find("input:checkbox:checked").map(function() {
					return $(this).next("label").text().split(' ');
				});
			} else {
				pMethod = divLastChild.find("input[type='text']").next().next().next().val();
			}
			if (parentNextDivConds.length == 1) {
				pCondition = parentNextDivConds.find("input[type='text']").val();
				pCondArgs = parentNextDivConds.find("input:last-child").val();
			}

			if (thisNext.text().indexOf("ResourcePermission") != -1) {
				var treeId = eachId.replace("c", "");
				var policy = transformWithResources(pName, pMethod, pCondition, pCondArgs, "#testTree" + treeId);
				if (policy != "") {
					permStringArray[x] = policy;
				} else {
					permStringArray[x] = transformInput(pName, pRessource, pMethod, pCondition, pCondArgs);
				}
			} else {
				permStringArray[x] = transformInput(pName, pRessource, pMethod, pCondition, pCondArgs);
			}
		}
	}

	// create JSON-form
	return '{"permissions":[' + permStringArray + ']}';
}

var resourcePerm = function() {
};

resourcePerm.prototype = {
	'id' : undefined,
	'name' : undefined,
	'actions' : undefined,
	'wc' : undefined
};

function transformWithResources(name, methods, condition, conditionArgs, /* resPermResources, */selector) {
	{
		var allSelected = $(selector).jstree("get_checked", true);
		var allTops = $(selector).jstree("get_top_checked", true);
		var selectedProps = [];
		var len = allTops.length;
		for (var j = 0; j < len; j++) {
			var value = allTops[j];
			wrapChildren(value, value.original.method, selectedProps, true, selector);
		}
	}
	var resourcesNumber = selectedProps.length;
	var pre = "";
	var resourceName;
	var resourceActions;
	var first = true;
	for (var x = 0; x < resourcesNumber; x++) {
		if (first) {
			first = false;
		} else
			pre += ",";
		props = selectedProps[x];
		resourceName = $(selector).jstree(true).get_path(props.id, "/");
		// check if the name contains a link
		var linkindex = resourceName.indexOf("->");
		if (linkindex != -1)
			resourceName = resourceName.substring(linkindex + 2);
		if (props.wc)
			resourceName += "/*";
		resourceActions = props.actions; // node.original.method;
		var transformed = transformInput(name, "path=" + resourceName, resourceActions, condition, conditionArgs);
		pre += transformed;
	}
	return pre;
}

function wrapChildren(value, methods, selecteds, isTop, selector) {
	var nodeprops;
	var length = value.children.length;
	var isChecked = $(selector).jstree("is_checked", value);
	if (isTop) {
		nodeprops = new resourcePerm();
		nodeprops.id = value.id;
		nodeprops.name = value.text
		nodeprops.actions = value.original.method;
		nodeprops.wc = value.original.recursive;
		selecteds.push(nodeprops);
		// if (isWildcard(value, selector)) {
		// nodeprops.wc = true;
		// return; // the top selected is wildcarded, nothing todo anymore
		// } else
		// nodeprops.wc = false;
	}
	for (var i = 0; i < length; i++) {
		var child = value.children[i];
		var chNode = $(selector).jstree(true).get_node(child);
		// var method = chNode.original.method;
		isChecked = $(selector).jstree("is_checked", chNode);
		if (isChecked) {
			nodeprops = new resourcePerm();
			nodeprops.id = chNode.id;
			nodeprops.name = chNode.text
			nodeprops.actions = chNode.original.method;
			nodeprops.wc = value.original.recursive;
			selecteds.push(nodeprops);
			// if (isWildcard(chNode, selector)) {
			// nodeprops.wc = true;
			// continue; // dont wrap children, proceed with the sister
			// // node
			// } else
			// nodeprops.wc = false;
		}
		wrapChildren(chNode, methods, selecteds, false, selector);
	}
}

function isWildcard(value, selector) {
	var result = true;
	var length = value.children.length;
	var parentMethods = value.original.method;
	for (var i = 0; i < length; i++) {
		var child = value.children[i];
		var chNode = $(selector).jstree(true).get_node(child);
		var methods = chNode.original.method;

		if (methods != "" && methods != parentMethods) {
			result = false;
			break;
		} else if (isWildcard(chNode, selector)) {
			result = true;
			continue;
		} else {
			result = false;
			break;
		}
	}
	return result;
}

/**
 * Transform permission-name/resource/method(s) to a string.
 * 
 * @param {String}
 *            name Permission name.
 * @param {String}
 *            ressource Permission filter.
 * @param {String}
 *            methods Permission action.
 * @param {String}
 *            condition Class Name
 * @param {String}
 *            conditionArgs Arguments
 * @return {String} permString Permission-String (One permission)
 */
function transformInput(name, ressource, methods, condition, conditionArgs) {
	var methodString = "";
	// count of methods
	if (methods != null || methods != "") {
		if (typeof (methods) == 'object') {
			for (var v = 0; v < methods.length; v++) {
				methodString = methodString + methods[v];
				if (v != methods.length - 1) {
					methodString = methodString + ",";
				} else {
					methodString = methodString + "";
				}
			}
		} else {
			methodString = methods;
		}
	}

	// count of arguments
	var argsMore;
	var argArray;
	var argString = "";
	if (conditionArgs.indexOf(",") != -1) {
		argsMore = true;
	} else {
		argsMore = false;
	}

	// more arguments
	if (argsMore == true) {
		argArray = conditionArgs.split(","); // creates an array
		for (var i = 0; i < argArray.length; i++) {

			if (i == argArray.length - 1) { // last has no comma
				if (argArray[i].indexOf(" ") != -1) {
					argString = argString + '"' + argArray[i].replace(" ", "") + '"';
				} else {
					argString = argString + '"' + argArray[i] + '"';
				}
			} else {
				if (argArray[i].indexOf(" ") != -1) {
					argString = argString + '"' + argArray[i].replace(" ", "") + '", ';
				} else {
					argString = argString + '"' + argArray[i] + '", ';
				}
			}
		}
		argString = '[' + argString + ']';
	} else { // only one argument
		if (conditionArgs == "" || conditionArgs == null) {
			argString = '[]';
		} else {
			argString = '["' + conditionArgs + '"]';
		}
	}

	var permString;
	if (name == "" && ressource == "" && methods == "") {
		// if nothing, no permstring
		permString = "";
	} else if (name.indexOf("Grant") != -1 || name.indexOf("Deny") != -1) {
		if (name.indexOf("Grant") != -1) {
			name = name.slice(7);
			permString = '{"mode":"ALLOW","name":"' + name + '","filter":"' + ressource + '","action":"' + methodString + '","condition":"' + condition
					+ '","conditionArgs":' + argString + '}';
		} else if (name.indexOf("Deny") != -1) {
			name = name.slice(6);
			permString = '{"mode":"DENY","name":"' + name + '","filter":"' + ressource + '","action":"' + methodString + '","condition":"' + condition
					+ '","conditionArgs":' + argString + '}';
		}
	} else {
		permString = '{"mode":"ALLOW","name":"' + name + '","filter":"' + ressource + '","action":"' + methodString + '","condition":"' + condition
				+ '","conditionArgs":' + argString + '}';
	}
	// final permission-string for sending to the server
	return permString;
}

// validate input - at least one checkbox, text-input must be filled
/**
 * Check input, Validation. Executed when SAVE-button is clicked.
 * 
 * @param {Object}
 *            obj Current dialog.
 * @return {boolean} True: okay, False: something is wrong.
 */
