function NewPerms() {
	var widthWindow = $(window).width() * 0.8;
	var heightWindow = $(window).height() * 0.8;
	var curStore = getUrlVals()["store"];
	var curAppId;
	var curAppName;
	var parentOfDialog = "#wrap";
	var formOfDialog = "#wrap form";

	var currentTreeID;

	$(function() {
		pageAction();
	});

	// --------------------------------- F U N C T I O N S
	// -------------------------------------

	function pageAction() {

		/* check if the resourceDialog already exists */
		createActionsdialog();

		var action = undefined, appID = undefined;
		var locationSearch = window.location.search.substring(1);
		if (locationSearch != undefined) {
			var arr1 = new Array();
			arr1 = locationSearch.split("&");
			var arr2 = new Array();

			for (var i = 0; i < arr1.length; i++) {
				arr2[i] = arr1[i].toString().substring(arr1[i].toString().indexOf("=") + 1);
				arr1[i] = arr1[i].toString().substring(0, arr1[i].toString().indexOf("="));
				if (arr1[i] == "action")
					action = arr2[i];
				else if (arr1[i] == "id")
					appID = arr2[i];
			}
			if (appID != undefined) {
				curAppId = appID;
				if (action == "newperms")// start process including
				// evaluation of
				// the
				// local permissions
				{
					// dialog = new NewPerms(appID);
					// openDia(parentOfDialog);
					appendDialog(parentOfDialog);
					// get the data from server
					getDesiredPerms(curAppId, parentOfDialog, formOfDialog);
				} else if (action == "editperms") { // start process by getting
					// the
					// already
					// set permission
					// permissionDialog(appID);
					// openDia(parentOfDialog);
					appendDialog(parentOfDialog);
					getGrantedPerms(curAppId, parentOfDialog, formOfDialog);
				}
			}
		}
	}

	function emptyDia(obj) {
		$(obj).find("form").empty();
	}

	// set heading for current store
	function setHeading() {
		if (curStore == "localAppDirectory" || curStore == "remoteFHGAppStore") {

			$("h1#appstore").addClass("highlightHeading");
			var welcome = "<strong style='font-size: 1.4em;'> Appstore </strong> <br> <em>" + curStore + "</em>";

			document.getElementById("appstore").innerHTML = welcome;
			$("div#vorwort").css("display", "inline");

		} else {
			document.getElementById("noAppstore").innerHTML = "Sorry. Unknown appstore.";
		}
	}

	/**
	 * Get the url subquery.
	 * 
	 * @return {Array} vals Array of subquery values
	 */
	function getUrlVals() {

		var vals = [], hash;
		var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
		for (var i = 0; i < hashes.length; i++) {
			hash = hashes[i].split('=');
			vals.push(hash[0]);
			vals[hash[0]] = hash[1];
		}
		return vals;
	}

	/**
	 * Take permission-JSON-string and extract the permission-name.
	 * 
	 * @param {String}
	 *            permStr Permission-JSON-String from server.
	 * @return {String} name Name in this permission-JSON-string.
	 */
	function getPermName(permStr) {
		if (permStr.indexOf("(" || ")") != -1) {
			var lenStr = permStr.length;
			permStr = permStr.slice(1, lenStr - 1);
		}
		var content = permStr.split(" ");

		// array.length >= 1 --> contains name
		if (content.length >= 1) {
			var name = content[0];
			return name;
		}
	}

	/**
	 * Get permission resource/filter.
	 * 
	 * @param {String}
	 *            permStr Permission-JSON-string from server.
	 * @return {String} resource Resource/filter in this permission-JSON-string.
	 */
	function getPermResource(permStr) {
		if (permStr.indexOf("(" || ")") != -1) {
			var lenStr = permStr.length;
			permStr = permStr.slice(1, lenStr - 1);
		}
		var content = permStr.split(" ");

		if (typeof (content) == "object") {
			for (i = 0; i < content.length; i++) {
				for (j = 0; j < content[i].length; j++) {
					content[i] = content[i].replace("'", "");
					content[i] = content[i].replace('"', '');
				}
			}

		}
		// array.length >= 2 --> contains resource
		if (content.length >= 2) {
			var resource = content[1];
			return resource;
		} else {
			var nothing = "";
			return nothing;
		}
	}

	/**
	 * Get permission action(s).
	 * 
	 * @param {String}
	 *            permStr Permission-JSON-string from server.
	 * @return {String} methods Method(s)/actions(s) in this
	 *         permission-JSON-string.
	 */
	function getPermMethod(permStr) {
		if (permStr.indexOf("(" || ")") != -1) {
			var lenStr = permStr.length;
			permStr = permStr.slice(1, lenStr - 1);
		}
		var content = permStr.split(" ");

		for (i = 0; i < content.length; i++) {
			for (j = 0; j < content[i].length; j++) {
				content[i] = content[i].replace("'", "");
				content[i] = content[i].replace('"', '');
			}
		}

		// array.length >= 3 --> contains methods
		if (content.length >= 3) {
			// 3rd array element are the methods
			var methodPre = content[2];
			// if it contains "," it contains more than one method
			if (methodPre.indexOf(",") != -1) {
				// splitting transforms to array
				var methods = methodPre.split(",");
			} else { // if no comma
				// string variable
				var methods = methodPre;
			}
			return methods;
		} else {
			var nothing = "";
			return nothing;
		}
	}

	// open the dialog for the app and get data
	/**
	 * Open the dialog with permissions in it for the chosen app.
	 * 
	 */
	// function openDia(selector) {
	//
	// // var diaNum = "#wrap";
	// // var radioNum = "app" + curAppId;
	// // var appCell = "#sel" + curAppId + " td.appName";
	//
	// // var dialog = diaNum;
	// var form = selector + " form";
	// // needed for sending appname to server
	// // curApp = $(appCell).html();
	//
	// // close other dialogs
	// // $(".ui-dialog-content").dialog("close");
	// /**
	// * Get the Permissions to fill in the dialog of this app.
	// *
	// * @param {number}
	// * rowNum rownumber/appnumber of the current chosen app.
	// */
	// appendDialog(selector);
	// }
	/**
	 * Get the Permissions to fill in the dialog of this app from the server.
	 * 
	 * @param {number}
	 *            rowNum rownumber/appnumber of the current chosen app.
	 */
	function getDesiredPerms(appNumber, dialog, form) {
		/**
		 * get permission-JSON-data for the current appstore and the chosen app.
		 * 
		 * @param {String}
		 *            "/security/config/app?appstore=" + curStore + "&name=" +
		 *            appName appstore and appname.
		 */
		$.getJSON("/security/config/localepermissions?id=" + appNumber, function(json, xhr) {
			evalPermData(json, appNumber, dialog, form);
			window.setTimeout(function() {
				$(dialog).dialog("open");
			}, 100);

		}).fail(function(jqXHR, textStatus, error) {
			$(".ui-dialog-content").dialog("close");
			var err = textStatus + ", " + error;
			alert("Error occured: " + err);
			return false;
		});
	}

	function methodsToArray(content) {
		for (i = 0; i < content.length; i++) {
			content[i] = content[i].replace("'", "");
			content[i] = content[i].replace('"', '');
		}

		// if it contains "," it contains more than one method
		if (content.indexOf(",") != -1) {
			// splitting transforms to array
			var methods = content.split(",");
		} else { // if no comma
			// string variable
			var methods = content;
		}
		return methods;
	}

	/**
	 * Get the Permissions to fill in the dialog of this app from the server.
	 * 
	 * @param {number}
	 *            rowNum rownumber/appnumber of the current chosen app.
	 */
	function getGrantedPerms(appNumber, dialog, form) {
		/**
		 * get permission-JSON-data for the current appstore and the chosen app.
		 * 
		 * @param {String}
		 *            "/security/config/app?appstore=" + curStore + "&name=" +
		 *            appName appstore and appname.
		 */
		$.getJSON("/security/config/grantedpermissions?id=" + appNumber, function(json, xhr) {
			// get permissions from server for the given url
			{
				// clear the dialog before for-loop
				$(dialog).dialog("option", "title", "Permissions demanded by " + json.name);
				$(form).empty();
				// loops through each permission-string
				for (var i = 0; i < json.policies.length; i++) {
					// check if the policy is a default one, in this case ignore
					// it, becasue default policies shouldn't be edited in this
					// context.
					var policy = json.policies[i];
					if (policy.conditions.length == 0)
						continue;
					var permissions = policy.permissions;
					var length = permissions.length;
					for (var j = 0; j < length; j++) {
						var entry = permissions[j];
						// get the single elements of each permission
						// get permission name
						var permName = entry.type;
						// get permission resource
						var permResource = entry.filter;
						// get permission method
						var permMethod = methodsToArray(entry.actions);
						// write elements in dialog
						writeInDia(permName, permResource, permMethod, i, appNumber, form);
					}
				}

				$(form).append("<button onclick='writeEndDia(" + appNumber + "," + '"' + dialog + '"' + ")' type='button'>Add Permission</button>");

				// adding an icon to the AddPermission-button
				$(dialog + " form > button").button({
					icons : {
						primary : "ui-icon-plus"
					}
				});
			}
			window.setTimeout(function() {
				$(dialog).dialog("open");
			}, 100);
		}).fail(function(jqXHR, textStatus, error) {
			$(".ui-dialog-content").dialog("close");
			var err = textStatus + ", " + error;
			alert("Error occured: " + err);
			return false;
		});
	}

	function appendDialog(appid) {

		// insert dialog for this app
		// $('div#wrap').append(
		// "<div class='dia' title='Permissions for " + "here comes the appname"
		// + "' id='dialog" + appid
		// + "'><form name='permForm' id='permissionForm'> </form></div>");

		$("#wrap").dialog({
			autoOpen : false,
			resizeable : false,
			draggable : true,
			height : $(window).height() - 200,
			minWidth : 650,
			minHeight : 400,
			dialogClass : 'no-close',
			close : function() {
				emptyDia($(this));
			},
			open : function() {
				$('.ui-dialog-buttonpane').find('button:contains("Save settings")').button({
					icons : {
						primary : 'ui-icon-check'
					}
				});
				$('.ui-dialog-buttonpane').find('button:contains("Cancel")').button({
					icons : {
						primary : 'ui-icon-close'
					}
				});
			},
			buttons : [ {
				text : "Cancel",
				class : "cancelButton",
				click : function() {
					$(this).dialog("close");
					// $('div#header').css('opacity', '1.0');
					// $('div#wrap').css('opacity', '1.0');
					// $('div#footer').css('opacity', '1.0');
					$(this).find("input[type=checkbox]").prop("checked", false);
				}
			},
			// Save-Button
			{
				text : "Save settings",
				class : "installButton",
				click : function() {

					// validate
					// input
					/**
					 * Check the input for the content of this dialog.
					 * 
					 * @param {Object}
					 *            $(this) Current dialog
					 * @return {boolean} True if it is ok. False if something is
					 *         wrong.
					 */
					var checkInput = checkingInput($(this));

					/**
					 * Check resource. There must be a resource plus Validation.
					 * 
					 * @param {Object}
					 *            $(this) Current dialog
					 * @return {boolean} True for okay. False if something is
					 *         wrong.
					 */
					var checkInputText = checkingRessourceInput($(this));

					// if
					// validation
					// is ok
					if (checkInput == true) {
						if (checkInputText == true) {
							// if
							// everything
							// is
							// ok,
							// get
							// the
							// filled
							// in
							// input
							// and
							// do
							// something
							// with
							// it....
							/**
							 * Get JSON-String for chosen permissions for the
							 * current app.
							 * 
							 * @param {Object}
							 *            $(this) Current dialog
							 * @return {String} JSON-String of permissions.
							 */
							var jsonContent = getInputBack($(this));

							// uncheck all checkboxes
							// $(this).find("input[type=checkbox]").prop("checked",
							// false);

							/**
							 * Sends chosen permissions for current app to
							 * server.
							 * 
							 * @param {String}
							 *            curApp Current app.
							 * @param {String}
							 *            jsonContent JSON-String for the app.
							 */
							sendPermsToServer(curAppId, jsonContent);
							// close dialog
							// $(this).dialog("close");

							setTimeout(function() {
								getGrantedPerms(curAppId, parentOfDialog, formOfDialog);
							}, 3000);
						} else {
							alert("Invalid input.");
						}
					}
				}
			} ]
		});
	}

	// get permissions from server for the given url
	function evalPermData(json, appNumber, dialog, form) {
		// var numberToIdentify;
		// if (diaClass == 'uploadDia') {
		// var diaId = "#dialUpload";
		// uploadApp = appName;
		// numberToIdentify = 2;
		// $(diaId).parent().find(".ui-dialog-titlebar
		// .ui-dialog-title").text("Permissions for " + appName);
		// } else {
		// var diaId = "#dialog" + appNumber;
		// numberToIdentify = 1;
		// }
		// clear the dialog before for-loop
		$(dialog).dialog("option", "title", "Permissions demanded by " + json.name);
		$(form).empty();
		// loops through each permission-string
		for (var i = 0; i < json.permissions.length; i++) {
			// get the single elements of each permission
			// get permission name
			var permName = getPermName(json.permissions[i]);
			// get permission resource
			var permResource = getPermResource(json.permissions[i]);
			// get permission method
			var permMethod = getPermMethod(json.permissions[i]);
			// write elements in dialog
			writeInDia(permName, permResource, permMethod, i, appNumber, form);
		}

		$(form).append("<button onclick='writeEndDia(" + appNumber + "," + '"' + dialog + '"' + ")' type='button'>Add Permission</button>");

		// adding an icon to the AddPermission-button
		$(form + " > button").button({
			icons : {
				primary : "ui-icon-plus"
			}
		});
	}

	/**
	 * Clears the dialog, so permissions can be filled in again without
	 * doubling.
	 * 
	 * @param {number}
	 *            dialogNum Dialog number.
	 */
	function clearDia(dialogId) {
		$(dialogId + " form").empty();
	}

	/**
	 * Write permissions into the dialog.
	 * 
	 * @param {number}
	 *            diaNum DialogNumber
	 * @param {String}
	 *            permName Name of the permission.
	 * @param {String}
	 *            permResource Resource of the permission.
	 * @param {String}
	 *            permMethod Method(s) of the permission.
	 * @param {number}
	 *            i Index of the permission concerning all permissions for this
	 *            app. Needed for identifying later.
	 * @param {number}
	 *            appNumber Number of the app converning all apps in this
	 *            appstore.
	 * @return {boolean} isAllPerm False: Not AllPerm. Extra additional
	 *         permission-option at the end.
	 */
	function writeInDia(name, resource, method, index, appNumber, form) {

		// adds dialog widget
		$(".dialogActions").dialog({
			autoOpen : false
		});

		// is the permission an AllPermission?
		var isAllPerm;
		if (name == "java.security.AllPermission") {
			isAllPerm = true;
		} else {
			isAllPerm = false;
		}

		// is the permission an accesscontrol permission?
		var isResourcePerm;
		if (checkForResourcePerms(name) == true) {
			isResourcePerm = true;
		} else {
			isResourcePerm = false;
		}

		// is it an array of methods or a single method?
		// var isArray;
		if (typeof (method) != "object") {
			var tmpmethod = [];
			tmpmethod.push(method);
			method = tmpmethod;
			// isArray = true;
		} else
		// {
		// isArray = false;
		// }

		if (name == "")
			return;

		// only for not-AllPermissions
		if (isAllPerm) { // if it is an AllPermission only
			// write
			// its name
			appendAllPerm(form, appNumber, index, name);
			return;
		}

		// only for Not-Array (single) method
		if (isResourcePerm == false) { // if it is not an
			// resource permission
			appendAnyPermission(form, appNumber, index, method, resource, name);
		} else { // if it is an resource permission
			appendResourcePerm(form, appNumber, index, method, resource, name);
			// $(form)
			// .append(
			// "<div class='wrapsOnePerm'><div class='headLine'><input
			// type='checkbox' onchange='boxCheck("
			// + appNumber
			// + index
			// + ")' class='p' id='c"
			// + appNumber
			// + index
			// + "'> <label class='lb' for='c"
			// + appNumber
			// + index
			// + "'>"
			// + name
			// + "</label> </div> <div class='bodyOfPerm'> Filter: <br> <input
			// type='text' value='"
			// + resource
			// + "' title='Please provide the resource.'> <br> Action: <br>
			// <input type='checkbox' onchange='singleHighlight("
			// + appNumber
			// + index
			// + ")' class ='m' id='m"
			// + appNumber
			// + index
			// + "'> <label class='lb' for='m"
			// + appNumber
			// + index
			// + "'>"
			// + method
			// + "</label> <br> <br> <div class='conditions'> Condition: <br>
			// <input type='text' id='cond"
			// + appNumber
			// + index
			// + "'> <br> Condition-Arguments: <br> <input type='text'
			// id='conArgs"
			// + appNumber
			// + index
			// + "'> </div> <div class='resDiv' style='display:none;'> <div
			// class='testTree' id='testTree"
			// + appNumber
			// + index
			// + "'> </div> </div> <div id='actions"
			// + appNumber
			// + index
			// + "' style='display: none;'> <br> <p> </p> <div
			// class='wrapActions' style='display:none;'> <input type='checkbox'
			// id='1"
			// + appNumber
			// + index
			// + "' name='read'><label for='1"
			// + appNumber
			// + index
			// + "'>read</label> <input type='checkbox' id='2"
			// + appNumber
			// + index
			// + "' name='write'><label for='2"
			// + appNumber
			// + index
			// + "'>write</label><input type='checkbox' id='3"
			// + appNumber
			// + index
			// + "' name='addSub'><label for='3"
			// + appNumber
			// + index
			// + "'>addSub</label><input type='checkbox' id='4"
			// + appNumber
			// + index
			// + "' name='create'><label for='4"
			// + appNumber
			// + index
			// + "'>create</label><input type='checkbox' id='5"
			// + appNumber
			// + index
			// + "' name='delete'><label for='5"
			// + appNumber
			// + index
			// + "'>delete</label><div class='checkResActions'> Set Actions
			// </div> </div> <div class=justShowActions style='display:none;'>
			// <input type='checkbox' id='1"
			// + appNumber + index + "' name='read'><label for='1" + appNumber +
			// index + "'>read</label> <input type='checkbox' id='2"
			// + appNumber + index + "' name='write'><label for='2" + appNumber
			// + index
			// + "'>write</label><input type='checkbox' id='3" + appNumber +
			// index + "' name='addSub'><label for='3" + appNumber
			// + index + "'>addSub</label><input type='checkbox' id='4" +
			// appNumber + index + "' name='create'><label for='4"
			// + appNumber + index + "'>create</label><input type='checkbox'
			// id='5" + appNumber + index
			// + "' name='delete'><label for='5" + appNumber + index +
			// "'>delete</label> <input type='checkbox' id='6" + appNumber
			// + index + "' name='activity'><label for='6" + appNumber + index +
			// "'>activity</label> </div> </div> </div>");

		}
	}

	function appendAllPerm(form, appNumber, index, name) {
		appendHeadline(form, appNumber, index, name);
		$(form)
				.append(
						"<div class='bodyOfPerm' style='display:none;'> Filter: <br> <input type='text' title='Please provide the resource.'> <br> Action: <br> <input type='text' title='Please provide the methods allowed for this permission.'></input> <div class='conditions'> Condition: <br> <input type='text' id='cond"
								+ appNumber
								+ index
								+ "'> <br> Condition-Arguments: <br> <input type='text' id='conArgs"
								+ appNumber
								+ index
								+ "'> </div> </div> </div>");
	}

	function appendAnyPermission(form, appNumber, index, method, resource, name) { // if
		// it
		// is
		// not an
		// Resource permission
		var txt = "";

		for (var v = 0; v < method.length; v++) { // for each
			// method
			txt = txt + "<input type='checkbox' onchange='singleHighlight(" + appNumber + index + v + ")' class='m' id='m" + appNumber + index + v
					+ "'> <label class='lb' for='m" + appNumber + index + v + "'>" + method[v] + "</label>";
			if (v != method.length - 1) {
				txt = txt + "<br>";

			} else {
				txt = txt + "";
			}
		}
		appendHeadline(form, appNumber, index, name);
		$(form).append(
				"<div class='bodyOfPerm'> Filter: <br> <input type='text' value='" + resource + "' title='Please provide the resource.'> <br> Action: <br>"
						+ txt + " <div class='conditions'> Condition: <br> <input type='text' id='cond" + appNumber + index
						+ "'> <br> Condition-Arguments: <br> <input type='text' id='conArgs" + appNumber + index + "'> </div> </div> </div>");
	}

	function appendResourcePerm(form, appNumber, index, method, resource, name) { // if
		// it
		// is
		// an
		// resource
		// permission
		var txt = "";

		for (var v = 0; v < method.length; v++) { // for each
			// method
			txt = txt + "<input type='checkbox' onchange='singleHighlight(" + appNumber + index + v + ")' class='m' id='m" + appNumber + index + v
					+ "'> <label class='lb' for='m" + appNumber + index + v + "'>" + method[v] + "</label>";
			if (v != method.length - 1) {
				txt = txt + "<br>";

			} else {
				txt = txt + "";
			}
		}
		appendHeadline(form, appNumber, index, name);
		$(form)
				.append(
						"<div class='bodyOfPerm'> Filter: <br> <input type='text' value='"
								+ resource
								+ "' title='Please provide the resource.'> <br> Action: <br>"
								+ txt
								+ "<br> <br> <div class='conditions'> Condition: <br> <input type='text' id='cond"
								+ appNumber
								+ index
								+ "'> <br> Condition-Arguments: <br> <input type='text' id='conArgs"
								+ appNumber
								+ index
								+ "'> </div>  <div class='resDiv' style='display:none;'> <div class='testTree' id='testTree"
								+ appNumber
								+ index
								+ "'> </div>  </div> <div id='actions"
								+ appNumber
								+ index
								+ "' style='display: none;'> <p> </p> <div class='wrapActions' style='display:none;'> <input type='checkbox' id='1"
								+ appNumber
								+ index
								+ "' name='read'><label for='1"
								+ appNumber
								+ index
								+ "'>read</label> <input type='checkbox' id='2"
								+ appNumber
								+ index
								+ "' name='write'><label for='2"
								+ appNumber
								+ index
								+ "'>write</label><input type='checkbox' id='3"
								+ appNumber
								+ index
								+ "' name='addSub'><label for='3"
								+ appNumber
								+ index
								+ "'>addSub</label><input type='checkbox' id='4"
								+ appNumber
								+ index
								+ "' name='create'><label for='4"
								+ appNumber
								+ index
								+ "'>create</label><input type='checkbox' id='5"
								+ appNumber
								+ index
								+ "' name='delete'><label for='5"
								+ appNumber
								+ index
								+ "'>delete</label><div class='checkResActions'> Set Actions </div> </div> <div class=justShowActions  style='display:none;'> <input type='checkbox' id='1"
								+ appNumber + index + "' name='read'><label for='1" + appNumber + index + "'>read</label> <input type='checkbox' id='2"
								+ appNumber + index + "' name='write'><label for='2" + appNumber + index + "'>write</label><input type='checkbox' id='3"
								+ appNumber + index + "' name='addSub'><label for='3" + appNumber + index + "'>addSub</label><input type='checkbox' id='4"
								+ appNumber + index + "' name='create'><label for='4" + appNumber + index + "'>create</label><input type='checkbox' id='5"
								+ appNumber + index + "' name='delete'><label for='5" + appNumber + index + "'>delete</label> <input type='checkbox' id='6"
								+ appNumber + index + "' name='activity'><label for='6" + appNumber + index
								+ "'>activity</label> <input type='radio' id='recursiveyes' name='recursive' value='YES'> <label"
								+ "for='recursiveyes'>YES</label> <input type='radio' id='recursiveno' name='recursive' value='NO'> <label"
								+ "for='recursiveno'>NO</label> </div> </div> </div>");
	}

	function appendHeadline(selector, appNumber, index, name) {
		$(selector).append(
				"<div class='wrapsOnePerm'><div class='headLine'><input type='checkbox' onchange='boxCheck(" + appNumber + index + ")' class='p' id='c"
						+ appNumber + index + "'> <label class='lb' for='c" + appNumber + index + "'>" + name + "</label> </div>");

	}
	function toMainPage() {
		window.open("/security-gui/index.html", "_self");
	}
	// --------------------------------- E N D - F U N C T I O N S
	// -------------------------------------
}
