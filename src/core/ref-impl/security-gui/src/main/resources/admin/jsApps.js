var widthWindow = $(window).width() * 0.8;
var heightWindow = $(window).height() * 0.8;
var curStore = getUrlVals()["store"];
var curApp;
var ind = 10; // index for additional permissions
var currentSelectedResourceNode;
var currentTreeID;

$(function() {
	if (window.location.href.indexOf('apps') != -1) {
		// sets heading for current store
		setHeading();
		$("#toMainPage").button({
			icons : {
				primary : "ui-icon-home"
			}
		});

		/**
		 * get json data (apps for the current store) and put them in dialog
		 * 
		 * @param {String}
		 *            curStore Current appstore
		 */
		$.getJSON(
				"/security/config/apps?name=" + curStore,
				function(json) {

					// insert table of apps - only when there are
					// apps
					$("div#tableWrap").append("<table id='tab1'><thead><tr><th class='select'></th><th>Name</th></tr></thead><tbody></tbody></table>");

					// for each app
					for (var i = 0; i < json.apps.length; i++) {

						// insert app name
						// die hier erstellten id's
						// existieren
						// nur in dieser Funktion!!
						var id = "sel" + i + 1;
						$("tbody").append(
								"<tr id='" + id + "'><td class = 'select'><input name = 'act' type='radio' id='app" + (i + 1) + "'></td><td class='appName'>"
										+ json.apps[i] + "</td></tr>");
					//	var name = json.apps[i];
						document.getElementById(id).addEventListener("click", function() {
							var name = jQuery(this).find('.appName').text();
							openDia(curStore, name);
						});

						// insert dialog for this app
						$("body").append(
								"<div class='dia' title='Permissions for " + json.apps[i] + "' id='dialog" + (i + 1)
										+ "'><form name='permForm' id='permissionForm'> </form></div>");

					}
				})
		// if there are no apps, so no ajax possible
		.fail(function() {
			$("div#wrapper").append("Sorry. No apps found in this marketplace. <br> <a href='../admin/mainpage.html'> Zur&uuml;ck </a>");
		});

		// }
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
		var welcome = "<strong style='font-size: 1.4em;'> Marketplace </strong> <br> <em>" + curStore + "</em>";

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
 * @param {number}
 *            rowNum rownumber/appnumber of the chosen app.
 */
function openDia(store, app) {
	/**
	 * Get the Permissions to fill in the dialog of this app.
	 * 
	 * @param {number}
	 *            rowNum rownumber/appnumber of the current chosen app.
	 */

	{
		/**
		 * get permission-JSON-data for the current appstore and the chosen app.
		 * 
		 * @param {String}
		 *            "/security/config/app?appstore=" + curStore + "&name=" +
		 *            appName appstore and appname.
		 */
		$.getJSON("/security/config/app?appstore=" + store + "&name=" + app, function(json, xhr) {
			installPermsByID(json.id);
		}).fail(function(jqXHR, textStatus, error) {
			var err = textStatus + ", " + error;
			alert("Error occured: " + err);
			return false;
		});
	}
}

/**
 * Clears the dialog, so permissions can be filled in again without doubling.
 * 
 * @param {number}
 *            dialogNum Dialog number.
 */
function clearDia(dialogId) {
	$(dialogId + " form").empty();
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

function toMainPage() {
	window.open("/security-gui/index.html", "_self");
}
// --------------------------------- E N D - F U N C T I O N S
// -------------------------------------
