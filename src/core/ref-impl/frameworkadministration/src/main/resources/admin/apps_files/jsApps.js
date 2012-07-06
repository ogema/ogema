/**
 * get the url query value for attribute "store" and put this as current store.
 */
var widthWindow = $(window).width() * 0.8;
var heightWindow = $(window).height() * 0.8;
var curStore = getUrlVals()["store"];
var curApp;
/* var fullAppPerm = []; */// only for perm array
var ind = 10; // index for additional permissions
var currentSelectedResourceNode;
var currentTreeID;

$(function() {
	// sets heading for current store
	setHeading();
	$("#toMainPage").button({
		icons : {
			primary : "ui-icon-home"
		}
	});

	// dialog window for resource actions in resource permissions
	$("#resourceDialog")
			.dialog(
					{
						autoOpen : false,
						draggable : true,
						position : [ 'center', 'center' ],
						dialogClass : 'no-close',

						buttons : [
								{
									text : "Cancel",
									class : "cancelButton",
									click : function() {
										$(this).dialog("close");
										$(this).find("input[type=checkbox]")
												.prop("checked", false);
										$("#testTree" + currentTreeID).jstree(
												"deselect_node",
												currentSelectedResourceNode.id);
									}
								},
								// Save-Button
								{
									text : "Set Actions",
									class : "installButton",
									click : function() {
										var actionsString;
										actionsString = RELOADgetResourceActions();
										currentSelectedResourceNode.original.method = actionsString;
										$(this).dialog("close");
									}
								} ]
					});

	// get apps from server
	if (curStore == "localAppDirectory" || curStore == "remoteFHGAppStore") {

		$("[title]").tooltip(); // initializing tooltips

		/**
		 * get json data (apps for the current store) and put them in dialog
		 * 
		 * @param {String}
		 *            curStore Current appstore
		 */
		$
				.getJSON(
						"/install/apps?name=" + curStore,
						function(json) {

							// insert table of apps - only when there are
							// apps
							$("div#tableWrap")
									.append(
											"<table id='tab1'><thead><tr><th class='select'></th><th>Name</th></tr></thead><tbody></tbody></table>");

							// for each app
							for (var i = 0; i < json.apps.length; i++) {

								// insert app name
								$("tbody")
										// die hier erstellten id's
										// existieren
										// nur in dieser Funktion!!
										.append(
												"<tr onclick ='openDia("
														+ (i + 1)
														+ ")' id='sel"
														+ (i + 1)
														+ "'><td class = 'select'><input name = 'act' type='radio' id='app"
														+ (i + 1)
														+ "'></td><td class='appName'>"
														+ json.apps[i].name
														+ "</td></tr>");

								// insert dialog for this app
								$("body")
										.append(
												"<div class='dia' title='App-Berechtigungen' id='dialog"
														+ (i + 1)
														+ "'><p>F&uuml;r <strong>"
														+ json.apps[i]
														+ "</strong> sind folgende Rechte erforderlich. Bitte w&auml;hlen Sie aus den folgenden Permissions oder editieren Sie:</p><form name='permForm' id='permissionForm'> </form></div>");

							}

							// dialog things
							$(".dia")
									.dialog(
											{
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
													$('.ui-dialog-buttonpane')
															.find(
																	'button:contains("Save settings")')
															.button(
																	{
																		icons : {
																			primary : 'ui-icon-check'
																		}
																	});
													$('.ui-dialog-buttonpane')
															.find(
																	'button:contains("Cancel")')
															.button(
																	{
																		icons : {
																			primary : 'ui-icon-close'
																		}
																	});
												},
												buttons : [
														{
															text : "Cancel",
															class : "cancelButton",
															click : function() {
																$(this)
																		.dialog(
																				"close");
																$('div#header')
																		.css(
																				'opacity',
																				'1.0');
																$('div#wrapper')
																		.css(
																				'opacity',
																				'1.0');
																$('div#footer')
																		.css(
																				'opacity',
																				'1.0');
																$(this)
																		.find(
																				"input[type=checkbox]")
																		.prop(
																				"checked",
																				false);
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
																 * Check the
																 * input for the
																 * content of
																 * this dialog.
																 * 
																 * @param
																 * {Object}
																 * $(this)
																 * Current
																 * dialog
																 * @return {boolean}
																 *         True
																 *         if it
																 *         is
																 *         ok.
																 *         False
																 *         if
																 *         something
																 *         is
																 *         wrong.
																 */
																var checkInput = checkingInput($(this));

																/**
																 * Check
																 * resource.
																 * There must be
																 * a resource
																 * plus
																 * Validation.
																 * 
																 * @param
																 * {Object}
																 * $(this)
																 * Current
																 * dialog
																 * @return {boolean}
																 *         True
																 *         for
																 *         okay.
																 *         False
																 *         if
																 *         something
																 *         is
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
																		 * Get
																		 * JSON-String
																		 * for
																		 * chosen
																		 * permissions
																		 * for
																		 * the
																		 * current
																		 * app.
																		 * 
																		 * @param
																		 * {Object}
																		 * $(this)
																		 * Current
																		 * dialog
																		 * @return {String}
																		 *         JSON-String
																		 *         of
																		 *         permissions.
																		 */
																		var jsonContent = getInputBack($(this));

																		// background
																		// blur
																		$(
																				'div#header')
																				.css(
																						'opacity',
																						'1.0');
																		$(
																				'div#wrapper')
																				.css(
																						'opacity',
																						'1.0');
																		$(
																				'div#footer')
																				.css(
																						'opacity',
																						'1.0');
																		// close
																		// dialog
																		$(this)
																				.dialog(
																						"close");
																		// uncheck
																		// all
																		// checkboxes
																		$(this)
																				.find(
																						"input[type=checkbox]")
																				.prop(
																						"checked",
																						false);

																		/**
																		 * Sends
																		 * chosen
																		 * permissions
																		 * for
																		 * current
																		 * app
																		 * to
																		 * server.
																		 * 
																		 * @param
																		 * {String}
																		 * curApp
																		 * Current
																		 * app.
																		 * @param
																		 * {String}
																		 * jsonContent
																		 * JSON-String
																		 * for
																		 * the
																		 * app.
																		 */
																		sendPermsToServer(
																				curApp,
																				jsonContent);
																	} else {
																		alert("Invalid input.");
																	}
																}
															}
														} ]
											});

						})
				// if there are no apps, so no ajax possible
				.fail(
						function() {
							$("div#wrapper")
									.append(
											"Sorry. No apps found in this appstore. <br> <a href='../admin/mainpage.html'> Zur&uuml;ck </a>");
						});

	}
});

// --------------------------------- F U N C T I O N S
// -------------------------------------

function emptyDia(obj) {
	$(obj).find("form").empty();
}

// set heading for current store
function setHeading() {
	if (curStore == "localAppDirectory" || curStore == "remoteFHGAppStore") {

		$("h1#appstore").addClass("highlightHeading");
		var welcome = "<strong style='font-size: 1.4em;'> Appstore </strong> <br> <em>"
				+ curStore + "</em>";

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
	var hashes = window.location.href.slice(
			window.location.href.indexOf('?') + 1).split('&');
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

	for (i = 0; i < content.length; i++) {
		for (j = 0; j < content[i].length; j++) {
			content[i] = content[i].replace("'", "");
			content[i] = content[i].replace('"', '');
		}
	}

	if (content.length >= 2) {
		var resource = content[1];
		return resource;
	}
}

/**
 * Get permission action(s).
 * 
 * @param {String}
 *            permStr Permission-JSON-string from server.
 * @return {String} methods Method(s)/actions(s) in this permission-JSON-string.
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

	if (content.length >= 3) {

		var methodPre = content[2];

		if (methodPre.indexOf(",") != -1) {
			var methods = methodPre.split(",");
		} else {
			var methods = methodPre;
		}
		return methods;
	}
}

// open the dialog for the app and get data
/**
 * Open the dialog with permissions in it for the chosen app.
 * 
 * @param {number}
 *            rowNum rownumber/appnumber of the chosen app.
 */
function openDia(rowNum) {
	var diaNum = "#dialog" + rowNum;
	var radioNum = "app" + rowNum;
	var appCell = "#sel" + rowNum + " td.appName";
	curApp = $(appCell).html();

	// close other dialogs
	$(".ui-dialog-content").dialog("close");

	try {
		/**
		 * Get the Permissions to fill in the dialog of this app.
		 * 
		 * @param {number}
		 *            rowNum rownumber/appnumber of the current chosen app.
		 */

		var status;
		status = getPermData(rowNum);

		if (status != false) {
			// check the apps radio button
			document.getElementById(radioNum).checked = true;
			// open slowly and blur background
			window.setTimeout(function() {
				$(diaNum).dialog("open");
				$('div#header').css('opacity', '0.3');
				$('div#footer').css('opacity', '0.3');
				$('div#wrapper').css('opacity', '0.3');
			}, 100);

		} else {
			console.log("fail");
			window.open("/loginFile.html", "_self");
		}

	} catch (e) {
		alert(e);
	}
}

/**
 * Get the Permissions to fill in the dialog of this app from the server.
 * 
 * @param {number}
 *            rowNum rownumber/appnumber of the current chosen app.
 */
function getPermData(appNumber) {

	var diaNum = "#dialog" + appNumber;
	var appCell = "#sel" + appNumber + " td.appName";
	var appName = $(appCell).html();
	var permArray = [];

	/**
	 * get permission-JSON-data for the current appstore and the chosen app.
	 * 
	 * @param {String}
	 *            "/install/app?appstore=" + curStore + "&name=" + appName
	 *            appstore and appname.
	 */
	$.getJSON(
			"/install/app?appstore=" + curStore + "&name=" + appName,
			function(json, xhr) {
				// clear the dialog before for-loop
				clearDia(diaNum);
				var isAllPerm;
				// loops through each permission-string
				for (var i = 0; i < json.localePerms.length; i++) {
					// permission-strings as array elements
					permArray[i] = json.localePerms[i];
					// get the single elements of each permission
					/**
					 * Get permission-name.
					 * 
					 * @param {String}
					 *            permArray[i] Each String-element in the
					 *            permission array.
					 * @return {String} permName Permission-name for each
					 *         permission-String of the permission-JSON-String.
					 */
					var permName = getPermName(permArray[i]);
					/**
					 * Get permission-resource.
					 * 
					 * @param {String}
					 *            permArray[i] Each String-element in the
					 *            permission array.
					 * @return {String} permResource Permission-resource for
					 *         each permission-String of the
					 *         permission-JSON-String.
					 */
					var permResource = getPermResource(permArray[i]);
					/**
					 * Get permission-method(s).
					 * 
					 * @param {String}
					 *            permArray[i] Each String-element in the
					 *            permission array.
					 * @return {String} permMethod Permission-method(s) for each
					 *         permission-String of the permission-JSON-String.
					 */
					var permMethod = getPermMethod(permArray[i]);

					// write new elements in dialog
					/**
					 * Write the permissions into the dialog for the current
					 * app.
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
					 *            i Index of the permission concerning all
					 *            permissions for this app. Needed for
					 *            identifying later.
					 * @param {number}
					 *            appNumber Number of the app converning all
					 *            apps in this appstore.
					 * @return {boolean} isAllPerm False: Not AllPerm. Extra
					 *         additional permission-option at the end.
					 */
					writeInDia(diaNum, permName, permResource, permMethod, i,
							appNumber);

				}

				$(diaNum + " form").append(
						"<button onclick='writeEndDia(" + appNumber
								+ ")' type='button'>Add Permission</button>");

				$(diaNum + " form > button").button({
					icons : {
						primary : "ui-icon-plus"
					}
				});
			}).fail(function(jqXHR, textStatus, error) {
		$(".ui-dialog-content").dialog("close");
		var err = textStatus + ", " + error;
		alert("Error occured: " + err);
		return false;
	});
}

// if not AllPermission: additional PermDiv
/**
 * Add an additional permission-option.
 * 
 * @param {number}
 *            appNumber Number of the app (is same as number of dialog)
 *            concerning all apps in appstore.
 * 
 */
function writeEndDia(appNumber) {
	var dia = "#dialog" + appNumber;
	ind++;
	$(dia + " form button")
			.before(
					"<div class='additional'><div class='headLine'><input type='checkbox' onchange='boxCheck("
							+ appNumber
							+ ind
							+ ")' class='p' id='c"
							+ appNumber
							+ ind
							+ "'> <input style='margin-left: 0px; width: 420px !important' type='text' id='text"
							+ appNumber
							+ ind
							+ "' title='Please provide the permission name.'> </input> <br> <input type='checkbox' class='p' id='c"
							+ appNumber
							+ ind
							+ 1
							+ "' onchange = 'checkGrantDeny("
							+ appNumber
							+ ind
							+ 1
							+ ", "
							+ 1
							+ ")'> <label for='c"
							+ appNumber
							+ ind
							+ 1
							+ "'> Grant </label> <input type='checkbox' class='p' id='c"
							+ appNumber
							+ ind
							+ 2
							+ "' onchange = 'checkGrantDeny("
							+ appNumber
							+ ind
							+ 2
							+ ", "
							+ 2
							+ ")'> <label for = 'c"
							+ appNumber
							+ ind
							+ 2
							+ "'> Deny </label> </div> <div class='bodyOfPerm'> Filter: <br> <input type='text' title='Please provide the resource.'> <br> Action: <br> <input type='text' title='Please provide the specific methods.'></input></div></div>");

}

/**
 * Style if checkbox for GRANT or DENY is checked. Uncheck the other box.
 * 
 * @param {number}
 *            idNum Number at the end of the checkbox-id.
 * @param {number}
 *            gdNum Can be 1 or 2. First box (GRANT) is 1 and second box (DENY)
 *            is 2. For identifying, if checked or denied.
 */
function checkGrantDeny(idNum, gdNum) {
	var identify = "c" + idNum;
	if (gdNum == 1) {
		if ($("#" + identify).prop("checked") == true) {
			$("#" + identify).next().css("font-weight", "bold");
			// if GRANT is checked, uncheck DENY
			$("#" + identify).next().next("input[type='checkbox']").prop(
					"checked", false);
			$("#" + identify).next().next().next().css("font-weight", "normal");
		} else {
			$("#" + identify).next().css("font-weight", "normal");
		}
	} else if (gdNum == 2) {
		if ($("#" + identify).prop("checked") == true) {
			$("#" + identify).next().css("font-weight", "bold");
			// if DENY is checked, uncheck GRANT
			$("#" + identify).prev().prev("input[type='checkbox']").prop(
					"checked", false);
			$("#" + identify).prev().css("font-weight", "normal");
		} else {
			$("#" + identify).next().css("font-weight", "normal");
		}
	}
}

/**
 * Clears the dialog, so permissions can be filled in again without doubling.
 * 
 * @param {number}
 *            dialogNum Dialog number.
 */
function clearDia(dialogNum) {
	$(dialogNum + " form").empty();
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
 *            i Index of the permission concerning all permissions for this app.
 *            Needed for identifying later.
 * @param {number}
 *            appNumber Number of the app converning all apps in this appstore.
 * @return {boolean} isAllPerm False: Not AllPerm. Extra additional
 *         permission-option at the end.
 */
function writeInDia(dialogNum, name, resource, method, index, appNumber) {

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
	var isArray;
	if (typeof (method) == "object") {
		isArray = true;
	} else {
		isArray = false;
	}

	var isEndDia;
	if (name == "") {
		isEndDia = true;
	} else {
		isEndDia = false;
	}

	if (isEndDia == false) {
		// only for not-AllPermissions
		if (isAllPerm == false) {
			// only for Not-Array (single) method
			if (isArray == false) {
				if (isResourcePerm == false) { // if it is not an
					// resource permission
					$(dialogNum + " form")
							.append(
									"<div class='wrapsOnePerm'><div class='headLine'><input type='checkbox' onchange='boxCheck("
											+ appNumber
											+ index
											+ ")' class='p' id='c"
											+ appNumber
											+ index
											+ "'> <label class='lb' for='c"
											+ appNumber
											+ index
											+ "'>"
											+ name
											+ "</label> </div>  <div class='bodyOfPerm'> Filter: <br> <input type='text' value='"
											+ resource
											+ "' title='Please provide the resource.'> <br> Action: <br> <input type='checkbox' onchange='singleHighlight("
											+ appNumber
											+ index
											+ ")' class ='m' id='m"
											+ appNumber
											+ index
											+ "'> <label class='lb' for='m"
											+ appNumber
											+ index
											+ "'>"
											+ method
											+ "</label> <div class='conditions'> Condition: <br> <input type='text' id='cond"
											+ appNumber
											+ index
											+ "'> <br> Condition-Arguments: <br> <input type='text' id='conArgs"
											+ appNumber
											+ index
											+ "'> </div> </div> </div>");
				} else { // if it is an resource permission
					$(dialogNum + " form")
							.append(
									"<div class='wrapsOnePerm'><div class='headLine'><input type='checkbox' onchange='boxCheck("
											+ appNumber
											+ index
											+ ")' class='p' id='c"
											+ appNumber
											+ index
											+ "'> <label class='lb' for='c"
											+ appNumber
											+ index
											+ "'>"
											+ name
											+ "</label> </div>  <div class='bodyOfPerm'> Filter: <br> <input type='text' value='"
											+ resource
											+ "' title='Please provide the resource.'> <br> Action: <br> <input type='checkbox' onchange='singleHighlight("
											+ appNumber
											+ index
											+ ")' class ='m' id='m"
											+ appNumber
											+ index
											+ "'> <label class='lb' for='m"
											+ appNumber
											+ index
											+ "'>"
											+ method
											+ "</label> <br> <br> <div class='conditions'> Condition: <br> <input type='text' id='cond"
											+ appNumber
											+ index
											+ "'> <br> Condition-Arguments: <br> <input type='text' id='conArgs"
											+ appNumber
											+ index
											+ "'> </div>  <div class='resDiv' style='display:none;'> <div class='testTree' id='testTree"
											+ appNumber
											+ index
											+ "'> </div> </div> <div id='actions"
											+ appNumber
											+ index
											+ "' style='display: none;'> <br> <p> </p> <div class='wrapActions' style='display:none;'> <input type='checkbox' id='1"
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
											+ "'>delete</label> <input type='checkbox' id='6"
											+ appNumber
											+ index
											+ "' name='activity'><label for='6"
											+ appNumber
											+ index
											+ "'>activity</label> </div> </div> </div>");

				}
			} else { // for array of methods
				if (isResourcePerm == false) { // if it is not an
					// Resource permission
					var txt = "";

					for (var v = 0; v < method.length; v++) { // for each
						// method
						txt = txt
								+ "<input type='checkbox' onchange='singleHighlight("
								+ appNumber + index + v + ")' class='m' id='m"
								+ appNumber + index + v
								+ "'> <label class='lb' for='m" + appNumber
								+ index + v + "'>" + method[v] + "</label>";
						if (v != method.length - 1) {
							txt = txt + "<br>";

						} else {
							txt = txt + "";
						}
					}
					$(dialogNum + " form")
							.append(
									"<div class='wrapsOnePerm'><div class='headLine'><input type='checkbox' onchange='boxCheck("
											+ appNumber
											+ index
											+ ")' class='p' id='c"
											+ appNumber
											+ index
											+ "'> <label class='lb' for='c"
											+ appNumber
											+ index
											+ "'>"
											+ name
											+ "</label> </div>  <div class='bodyOfPerm'> Filter: <br> <input type='text' value='"
											+ resource
											+ "' title='Please provide the resource.'> <br> Action: <br>"
											+ txt
											+ " <div class='conditions'> Condition: <br> <input type='text' id='cond"
											+ appNumber
											+ index
											+ "'> <br> Condition-Arguments: <br> <input type='text' id='conArgs"
											+ appNumber
											+ index
											+ "'> </div> </div> </div>");
				} else { // if it is an resource permission
					var txt = "";

					for (var v = 0; v < method.length; v++) { // for each
						// method
						txt = txt
								+ "<input type='checkbox' onchange='singleHighlight("
								+ appNumber + index + v + ")' class='m' id='m"
								+ appNumber + index + v
								+ "'> <label class='lb' for='m" + appNumber
								+ index + v + "'>" + method[v] + "</label>";
						if (v != method.length - 1) {
							txt = txt + "<br>";

						} else {
							txt = txt + "";
						}
					}
					$(dialogNum + " form")
							.append(
									"<div class='wrapsOnePerm'><div class='headLine'><input type='checkbox' onchange='boxCheck("
											+ appNumber
											+ index
											+ ")' class='p' id='c"
											+ appNumber
											+ index
											+ "'> <label class='lb' for='c"
											+ appNumber
											+ index
											+ "'>"
											+ name
											+ "</label> </div>  <div class='bodyOfPerm'> Filter: <br> <input type='text' value='"
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
											+ "'>delete</label> <input type='checkbox' id='6"
											+ appNumber
											+ index
											+ "' name='activity'><label for='6"
											+ appNumber
											+ index
											+ "'>activity</label> </div> </div> </div>");
				}

			}

		} else if (isAllPerm == true) { // if it is an AllPermission only
			// write
			// its name
			$(dialogNum + " form")
					.append(
							"<div class='wrapsOnePerm'><div class='allPermHeadLine'><input type='checkbox' onchange='boxCheck("
									+ appNumber
									+ index
									+ ")' class='p' id='c"
									+ appNumber
									+ index
									+ "'> <input type='text' id = 'text"
									+ appNumber
									+ index
									+ "' onkeyup = 'allPermSelect("
									+ appNumber
									+ index
									+ ")' value='"
									+ name
									+ "' title='Please provide the permissions name.'></input> </div> <div class='bodyOfPerm' style='display:none;'> Filter: <br> <input type='text' title='Please provide the resource.'> <br> Action: <br> <input type='text' title='Please provide the methods allowed for this permission.'></input> <div class='conditions'> Condition: <br> <input type='text' id='cond"
									+ appNumber
									+ index
									+ "'> <br> Condition-Arguments: <br> <input type='text' id='conArgs"
									+ appNumber
									+ index
									+ "'> </div> </div> </div>");
		}
	}
	$(".dialogActions").dialog({
		autoOpen : false
	});
}

/**
 * Show resource/methods in div or hide, case AllPermission there are no
 * editing-options.
 * 
 * @param {number}
 *            textNumber Identify the AllPermission-text.
 */
function allPermSelect(textNumber) {
	var textId = "#text" + textNumber;
	if ($(textId).val() != "java.security.AllPermission") {
		$(textId).parent().removeClass("allPermHeadLine");
		$(textId).parent().addClass("headLine");
		$(textId).parent().parent().find("div:last-child").show("fast");
	} else {
		$(textId).parent().removeClass("headLine");
		$(textId).parent().addClass("allPermHeadLine");
		$(textId).parent().parent().find("div:last-child").hide("fast");
	}
}

/**
 * (Un)check all child boxes. Executed when a checkbox is changed.
 * 
 * @param {number}
 *            num Identify checkbox.
 */
function boxCheck(num) {
	var identifyCheck = "input#c" + num;
	// if current box is checked
	if ($(identifyCheck).prop("checked")) {
		checkSubBoxes(identifyCheck);
		showRessources(num);
		$(identifyCheck).parent().parent().find("div:last-child").css(
				"background", "#89A5CC");
		$(identifyCheck).parent().parent().find("div:last-child").find(
				"input[type='text']").addClass("highlightInputText");
	} else {
		showRessources(num);

		$("#conArgs" + num).parent().next().hide("fast");
		uncheckSubBoxes(identifyCheck);
		$(identifyCheck).parent().parent().find("div:last-child").css(
				"background", "white");
		$(identifyCheck).parent().parent().find("div:last-child").find(
				"input[type='text']").removeClass("highlightInputText");
	}
}

/**
 * Check child-checkboxes. Style Highlight.
 * 
 * @param {String}
 *            obj String-ID of checkbox.
 */
function checkSubBoxes(obj) {
	var parent = $(obj).parent().parent();
	parent.find("div:last-child>input[type='checkbox'].m")
			.prop('checked', true);
	parent.find("div>input[type='text']").addClass("highlight");
	parent.find("div:last-child").find("label.lb").addClass("highlight");
}

/**
 * Uncheck child-checkboxes. Remove Highlight.
 * 
 * @param {String}
 *            obj String-ID of checkbox.
 */
function uncheckSubBoxes(obj) {
	var parent = $(obj).parent().parent();
	parent.find("div>input[type='checkbox'].m").prop('checked', false);
	parent.find("div>input[type='text']").removeClass("highlight");
	parent.find("div:last-child").find("label.lb").removeClass("highlight");
}

/**
 * Highlight a single checkbox. Executed when a childbox gets changed.
 * 
 * @param {number}
 *            num For identifying single box.
 */
function singleHighlight(num) {
	var identifyCheck = "input#m" + num;
	if (!$(identifyCheck).prop("checked")) {
		$("label[for='m" + num + "']").removeClass("highlight");
	} else {
		$("label[for='m" + num + "']").addClass("highlight");
	}
}

/**
 * get chosen permissions and transform them into a JSON-String for sending to
 * server. Executed when SAVE-button is clicked.
 * 
 * @param {Object}
 *            obj Current dialog. return {String} finalJSON JSON-String of the
 *            chosen permissions.
 */
function getInputBack(obj) {

	var pName = "";
	var pRessource = "";
	var pMethod;
	var pCondition;
	var pCondArgs;
	var countChecked = $("form > div div:first-child input:first-child:checked").length;
	var arrayOfChecked = $("form > div div:first-child input:first-child:checked");
	var permStringArray = [];
	var resPermResources = "";
	var idNumOnly;

	pName = "";
	pRessource = "";
	pMethod = "";
	pCondition = "";
	pCondArgs = "";

	for (var x = 0; x < countChecked; x++) {
		if ($("form > div obj div:first-child input").prop("checked", true)) {
			var eachId = arrayOfChecked[x].id;
			// if only one label in head (label is name)
			if ($("input#" + eachId).parent().find("label").length == 1) {
				pName = $("input#" + eachId).parent().find("label").text();

			} else if ($("input#" + eachId).parent().find("label").length == 0) { // if
				// no
				// label
				// (text-input
				// is
				// name)
				pName = $("input#" + eachId).parent()
						.find("input[type='text']").val();

			} else if ($("input#" + eachId).parent().find("label").length > 1) { // if
				// more
				// than
				// one
				// label
				// (additional
				// perm,
				// first
				// label
				// is
				// name)
				// grant or deny checked
				pName = $("input#" + eachId).parent().find(
						"input:checkbox:checked").next("label").text();
				// get name of text-input
				pName = pName
						+ $("input#" + eachId).parent().find(
								"input[type='text']").val();
			}
			pRessource = $("input#" + eachId).parent().parent().find(
					"div:last-child").find("input[type='text']").val();
			if ($("input#" + eachId).parent().parent().find("div:last-child")
					.find("input[type='checkbox']").length > 0) {
				pMethod = $("input#" + eachId).parent().parent().find(
						"div:last-child").find("input:checkbox:checked").map(
						function() {
							return $(this).next("label").text().split(' ');
						});
			} else {
				pMethod = $("input#" + eachId).parent().parent().find(
						"div:last-child").find("input[type='text']").next()
						.next().next().val();
			}
			if ($("input#" + eachId).parent().next().find("div.conditions").length == 1) {
				pCondition = $("input#" + eachId).parent().next().find(
						"div.conditions").find("input[type='text']").val();
				pCondArgs = $("input#" + eachId).parent().next().find(
						"div.conditions").find("input:last-child").val();
			}

			if ($("input#" + eachId).next().text()
					.indexOf("ResourcePermission") != -1) {
				idNumOnly = eachId.replace("c", "");
				resPermResources = getResources(idNumOnly);
			}

		}
		if (!(pName.indexOf("ResourcePermission") != -1)) {
			permStringArray[x] = transformInput(pName, pRessource, pMethod,
					pCondition, pCondArgs);
		} else if (pName.indexOf("ResourcePermission") != -1) {
			var checkedLen = $("#testTree" + idNumOnly).jstree("get_selected").length;
			if (checkedLen != 0) {
				permStringArray[x] = transformWithResources(pName, pRessource,
						pMethod, pCondition, pCondArgs, resPermResources,
						idNumOnly);
			} else {
				permStringArray[x] = transformInput(pName, pRessource, pMethod,
						pCondition, pCondArgs);
			}
		}
		pName = "";
		pRessource = "";
		pMethod = "";
		pCondition = "";
		pCondArgs = "";
		resPermResources = "";
	}

	// create JSON-form
	var finalJSON = '{"localePerms":[' + permStringArray + ']}';

	// reset
	eachId = [];
	countChecked = 0;
	permStringArray = [];

	return finalJSON;
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
					argString = argString + '"' + argArray[i].replace(" ", "")
							+ '"';
				} else {
					argString = argString + '"' + argArray[i] + '"';
				}
			} else {
				if (argArray[i].indexOf(" ") != -1) {
					argString = argString + '"' + argArray[i].replace(" ", "")
							+ '", ';
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
			permString = '{"mode":"ALLOW", "name":"' + name + '", "filter":"'
					+ ressource + '", "action":"' + methodString
					+ '", "condition":"' + condition + '", "conditionArgs":'
					+ argString + '}';
		} else if (name.indexOf("Deny") != -1) {
			name = name.slice(6);
			permString = '{"mode":"DENY", "name":"' + name + '", "filter":"'
					+ ressource + '", "action":"' + methodString
					+ '", "condition":"' + condition + '", "conditionArgs":'
					+ argString + '}';
		}
	} else {
		permString = '{"mode":"ALLOW", "name":"' + name + '", "filter":"'
				+ ressource + '", "action":"' + methodString
				+ '", "condition":"' + condition + '", "conditionArgs":'
				+ argString + '}';
	}
	// final permission-string for sending to the server
	return permString;
}

function transformWithResources(name, ressource, methods, condition,
		conditionArgs, resPermResources, num) {

	var resourcesAsArray = resPermResources.split(', ');
	var pre = "";
	var resourceName;
	var resourceActions;
	var finalPath = "";
	for (var x = 0; x < resourcesAsArray.length; x++) {
		resourceName = $("#testTree" + num).jstree(true).get_path(
				resourcesAsArray[x]);
		resourceActions = $("#testTree" + num).jstree(true).get_node(
				resourcesAsArray[x]).original.method;
		var oldString = transformInput(name, "path=" + resourceName,
				resourceActions, condition, conditionArgs);
		/*
		 * var stringArray = oldString.split("\", \"action\"");
		 * 
		 * for (var y = 0; y < resourceName.length; y++) { finalPath = finalPath +
		 * "/" + resourceName[y] if (y == resourceName - 1) { finalPath =
		 * finalPath + "/*"; } } var newString = stringArray[0] + ", path=" +
		 * finalPath + "/*\", \"action\"" + stringArray[1];
		 */
		var newString = oldString;

		if (x != resourcesAsArray.length - 1) {
			pre = pre + newString + ", ";
		} else if (x == resourcesAsArray.length - 1) {
			pre = pre + newString;
		}
		finalPath = "";
	}
	return pre;
}

// validate input - at least one checkbox, text-input must be filled
/**
 * Check input, Validation. Executed when SAVE-button is clicked.
 * 
 * @param {Object}
 *            obj Current dialog.
 * @return {boolean} True: okay, False: something is wrong.
 */
function checkingInput(obj) {

	var boxChecked;
	var textFilled;

	// all headers of single permissions
	var identifyCheck = "#" + obj[0].id + " form > div div:first-child";

	// all headers of single permissions, that contain a checked checkbox
	var checkedPerms = $(identifyCheck).find("input[type='checkbox']:checked");

	// count checked permissions
	for (var i = 0; i < ($(identifyCheck)
			.find("input[type='checkbox']:checked").length); i++) {

		// find all content-checkboxes, if there are more than 0, then..
		if ($("#" + checkedPerms[i].id).parent().parent().find(
				"div:last-child input:checkbox:visible").length > 0) {
			if ($("#" + checkedPerms[i].id).parent().parent().find(
					"div:last-child input:checkbox:checked").length > 0) {
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
		if ($("#" + checkedPerms[i].id).parent().parent().find(
				"div:last-child input[type='text']:visible").length > 0) {
			if ($("#" + checkedPerms[i].id).parent().parent().find(
					"div:last-child input[type='text']").val() != "") {
				textFilled = true;
			} else {

				// if it is an additional permission, resource or methods dont
				// have to be given
				if ($("#" + checkedPerms[i].id).parent().parent().parent()
						.find("div.additional").length > 0) {
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

/**
 * Executed when paragraph in accessPerm is clicked. So it shows the div
 * (resources affected).
 * 
 * @param {number}
 *            num Identify the paragraph and toggle next element (=div)
 */
function showRessources(num) {
	var display = $("#conArgs" + num).parent().next().css("display");
	if (display == "none") {
		writeResourceInDia(num);
		$("#conArgs" + num).parent().next().show();
		$("#conArgs" + num).parent().next().next().show();
	} else {
		$("#conArgs" + num).parent().next().hide();
		$("#conArgs" + num).parent().next().next().hide();
		$("#conArgs" + num).parent().next().next().find("input:checkbox").prop(
				"checked", false);
	}
}

function writeResourceInDia(num) {
	// GET to server, for initializing the resource-transmission
	var filter = $("label[for='c" + num + "']").parent().next().find(
			"input[type='text']").val();
	var actions = "";

	if ($("label[for='c" + num + "']").parent().next().find(
			"input:checkbox:checked").length > 1) {
		var preActions = $("label[for='c" + num + "']").parent().next().find(
				"input:checkbox:checked").map(function() {
			return $(this).next("label").text().split(' ');
		});
		for (var i = 0; i < preActions.length; i++) {
			if (i != preActions.length - 1) {
				actions = actions + preActions[i] + ",";
			} else {
				actions = actions + preActions[i];
			}
		}
	} else {
		actions = $("label[for='c" + num + "']").parent().next().find(
				"input:checkbox:checked").next("label").text();
	}

	$.get("/install/resourceperm?filter=" + filter + "&action=" + actions);

	var newNode = "";
	var selectedTopNodes;

	// Properly resource transmission
	$("#testTree" + num).jstree("refresh");
	if (!($("#testTree" + num).hasClass("jstree"))) {
		$("#testTree" + num)
				.jstree({
					"core" : {
						"animation" : 0,
						"check_callback" : true,
						"themes" : {
							"stripes" : true
						},
						'data' : {
							"url" : "/install/resourceperm",
							'data' : function(node) {
								return {
									'id' : node.id
								};
							}
						}
					},
					"checkbox" : {
						"whole_node" : true,
						"keep_selected_style" : false
					},
					"plugins" : [ "checkbox", "wholerow" ]
				})
				.bind(
						"select_node.jstree",
						function(event, data) {
							currentTreeID = num;
							var nodeSelected = data.node.text;
							var nodeSelectedID = data.node.id;
							var parentOfSelected = data.node.parent;
							var parentIsSelected = $("#testTree" + num).jstree(
									"is_selected", data.node.parent);
							var currentNode;
							var currentMethod;

							if (parentIsSelected) {

								var parent = $("#testTree" + num).jstree(true)
										.get_node(parentOfSelected).parent;
								var parentIsSel = $("#testTree" + num).jstree(
										"is_selected", parent);
								if (parentIsSel) {
									while (parentIsSel == true) {
										parent = $("#testTree" + num).jstree(
												true).get_node(parent).parent;
										parentIsSel = $("#testTree" + num)
												.jstree("is_selected", parent);
									}
									currentNode = parent;
								} else {
									currentNode = parentOfSelected;
								}
								currentSelectedResourceNode = $(
										"#testTree" + num).jstree(true)
										.get_node(currentNode);
								currentMethod = $("#testTree" + num).jstree(
										true).get_node(currentNode).original.method;
							} else {
								currentNode = nodeSelectedID;
								currentSelectedResourceNode = $(
										"#testTree" + num).jstree(true)
										.get_node(currentNode);
								currentMethod = $("#testTree" + num).jstree(
										true).get_node(currentNode).original.method;
							}
							$("#resourceDialog").find("input[type='checkbox']")
									.prop("checked", false);
							RELOADselectMethodsForThis(currentMethod);

							$("#resourceDialog").dialog("open");
						})
				.bind("deselect_node.jstree", function(event, data) {

				})
				.bind(
						"hover_node.jstree",
						function(e, data) {
							newNode = data.node.text;
							var newNodeID = data.node.id;
							var display = $("#actions" + num + " .wrapActions")
									.css("display");
							if (display != "none")
								return;

							$("#" + newNodeID).addClass("hasFocus");

							setTimeout(
									function() {
										$("#actions" + num).find(
												" div.justShowActions").find(
												"input[type='checkbox']").prop(
												"checked", false);

										if ($("#" + newNodeID).hasClass(
												"hasFocus")) {
											if ($("#testTree" + num).jstree(
													true).get_node(newNodeID).length != 0) {
												var lastNodeMethod = $(
														"#testTree" + num)
														.jstree(true).get_node(
																newNodeID).original.method;
												var isReady = showPreMethodsForThis(
														lastNodeMethod, num);

												if (isReady == true) {
													var offset = $(
															"#testTree" + num)
															.find(
																	"#"
																			+ newNodeID)
															.offset().top;
													$("#actions" + num).show(
															"fast");
													$("#actions" + num)
															.find(
																	" div.justShowActions")
															.css("position",
																	"fixed");
													$("#actions" + num)
															.find(
																	" div.justShowActions")
															.css("top",
																	offset + 33);
													$("#actions" + num)
															.find(
																	" div.justShowActions")
															.show("50");
												}
											}
										}
									}, 1000);

						}).bind("dehover_node.jstree", function(e, data) {
					var newNodeID = data.node.id;
					$("#" + newNodeID).removeClass("hasFocus");
					$("#actions" + num).find("div.justShowActions").hide("50");
				});
	}

	$("#actions" + num + " div.checkResActions").click(
			function() {
				var actionsString;
				actionsString = getResourceActions();
				currentSelectedResourceNode.original.method = actionsString;
				$("#actions" + num + " .wrapActions").hide(50);
				$("#actions" + num + " p").html("");
				$("#actions" + num + " .wrapActions").find(
						"input[type='checkbox']").prop("checked", false);
			});

}

function RELOADgetResourceActions() {
	if ($("#resourceDialog").find("input:checkbox:checked").length > 1) {
		var actionsArray = $("#resourceDialog").find("input:checkbox:checked")
				.map(function() {
					return $(this).next("label").text().split(' ');
				});
		var methodString = "";
		if (actionsArray != null || actionsArray != "") {
			if (typeof (actionsArray) == 'object') {
				for (var v = 0; v < actionsArray.length; v++) {
					methodString = methodString + actionsArray[v];
					if (v != actionsArray.length - 1) {
						methodString = methodString + ",";
					} else {
						methodString = methodString + "";
					}
				}
			} else {
				methodString = actionsArray;
			}
		}
		return methodString;
	} else {
		var action = $("#resourceDialog").find("input:checkbox:checked").next(
				"label").text();
		return action;
	}
}

function RELOADselectMethodsForThis(currentMethod) {
	if (currentMethod.indexOf(",") != -1) {
		var methods;
		methods = currentMethod.split(",");
		for (var x = 0; x < methods.length; x++) {
			$("#resourceDialog").find("input[name='" + methods[x] + "']").prop(
					"checked", true);
		}
	} else {
		$("#resourceDialog").find("input[name='" + currentMethod + "']").prop(
				"checked", true);
	}

}

function showPreMethodsForThis(currentMethod, num) {
	if (currentMethod.indexOf(",") != -1) {
		var methods;
		methods = currentMethod.split(",");
		for (var x = 0; x < methods.length; x++) {
			$("#actions" + num + " .justShowActions").find(
					"input[name='" + methods[x] + "']").prop("checked", true);
		}
	} else {
		$("#actions" + num + " .justShowActions").find(
				"input[name='" + currentMethod + "']").prop("checked", true);
	}
	return true;
}

function getResources(num) {
	var allSelected = $("#testTree" + num).jstree("get_selected", true);
	var selectedChildren = [];
	var titleOfChildren = [];
	var actionOfChildren = [];
	$.each(allSelected, function(key, value) {
		if (!value.children.length) {
			selectedChildren.push(value.id);
			titleOfChildren.push(value.text);
			actionOfChildren.push(value.original.method);
		}
	});
	var resourceString = selectedChildren.join(', ');
	return resourceString;
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

	for (var i = 0; i < ($(identifyCheck)
			.find("input[type='checkbox']:checked").length); i++) {
		// all headers of single permissions, that contain a checked
		// checkbox
		var checkedPerms = $(identifyCheck).find(
				"input[type='checkbox']:checked");

		var textInput = $("#" + checkedPerms[i].id).parent().parent().find(
				"div:last-child input[type='text']").val();
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
function sendPermsToServer(currentAppName, permContentForApp) {
	console.log('{app: "' + currentAppName + '", permission: "'
			+ permContentForApp + '"}');
	console.log(permContentForApp);
	$.post(
			"/install/permissions?appstore=" + curStore + "&name="
					+ currentAppName,
			{
				permission : "" + permContentForApp
			},
			function(data, status) { // if successfull
				console.log(data);
				console.log(status);
				window.alert("Data send to server for: " + currentAppName
						+ "\nStatus: " + status);
			}).fail(
			function(xhr, textStatus, errorThrown) {
				// if http-post fails
				window.alert("Something went wrong: " + textStatus
						+ "\nError: " + errorThrown);
			});
}

function toMainPage() {
	window.open("/admin/mainpage.html", "_self");
}
// --------------------------------- E N D - F U N C T I O N S
// -------------------------------------
