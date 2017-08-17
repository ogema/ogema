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
		$("div#buttonSet > button").button();
		$("button#install").button({
			icons : {
				primary : "ui-icon-arrowthickstop-1-s"
			}
		});
		$("#store").html(curStore);
		$("#market").html("Bundles in the Marketplace "+curStore);
		$("#marketLogin").html("Marketplace "+curStore);
		/**
		 * get json data (apps for the current store) and put them in dialog
		 *
		 * @param {String}
		 *            curStore Current appstore
		 */
		$.getJSON(
				"/security/config/apps?name=" + curStore+"&user=" + otusr + "&pw=" + otpwd,
				function(json) {

					for (var v = 0; v < json.apps.length; v++) {
						$("div#sortableNewApps").append(
								"<div class='column'> <div class='portlet'> <div class='portlet-header' id='" + v + "'>" +
									"<img class='portlet-images' src='/security/config/getappstoreicon?loc="+json.apps[v+1]+"' ></div>" +
									" <div class='portlet-content'>" + json.apps[v] + "</div> </div></div>");
						v++;
					}
					$(".column").sortable({
						connectWith : ".column",
						handle : ".portlet-header",
						cancel : ".portlet-toggle",
						placeholder : "portlet-placeholder ui-corner-all"
					});
					$(".portlet").addClass("ui-widget ui-widget-content ui-helper-clearfix ui-corner-all").find(".portlet-header").addClass(
							"ui-widget-header ui-corner-all").prepend("<span class='ui-icon ui-icon-minusthick portlet-toggle'></span>");
					$(".portlet-toggle").click(function() {
						var icon = $(this);
						icon.toggleClass("ui-icon-minusthick ui-icon-plusthick");
						icon.closest(".portlet").find(".portlet-content").toggle();
					});

					$("div.portlet-header>span").removeClass("ui-icon");

					$("div#sortableNewApps").append("<ul id='contextMenu'><li onclick='install()'>Install Bundle</li></ul>")

					$("#contextMenu").menu({
						select: function(event, ui){
						$("#contextMenu").hide();
						}
					});

					$("div.portlet-header").dblclick(function() {
						$("div.portlet-header").removeClass("selectedPortlet");
						$(this).addClass("selectedPortlet");

						$(".ui-dialog-content").dialog("close");
						selectedApp = $(this).parent().find(".portlet-content").text();
					});

					// Clicking anywhere in
					// sortableAppsWrapper causes
					// deselection
					$("#sortableNewApps").click(function() {
						$("div.portlet-header").removeClass("selectedPortlet");
						selectedApp = "";
						$("#contextMenu").hide();
						$("#contextMenu").removeData();
					});


					$(".portlet-header").on("contextmenu", function (event) {
						$("div.portlet-header").removeClass("selectedPortlet");
						$("#contextMenu").show();
						$("#contextMenu").data('originalElement', this);
				        $("#contextMenu").position({
				        	collision: "none",
				            my: "left top",
				            of: event
				        });
				        return false;
				    });

					$("#sysApps").click(function() {
						$("#contextMenu").hide();
						$("#contextMenu").removeData();
					});


				})
		// if there are no apps, so no ajax possible
		.fail(function() {
			$("#market").html("Sorry. No apps found in this marketplace. <br> <a href='../security-gui/index.html'> Zur&uuml;ck </a>");
		});

		// }
	}
});

// --------------------------------- F U N C T I O N S
// -------------------------------------

function loginRequired(){
	if (curStore!="localAppDirectory"){
		$("#sysApps").hide();
		$("#appsLogin").show();
	}else{
		/*$("#sysApps").show();
		$("#appsLogin").hide();*/
		$("#sysApps").hide();
		$("#appsLogin").show();
	}
}

function submitLogin(){
	var user = $("#user").val();
	var pwd = $("#password").val();

	$.post("/security/config/appstorelogin?usr="+user+"&store="+curStore+"&pwd="+pwd +"&user=" + otusr + "&pw=" + otpwd,{

	}, function(data, status){
		if(data=="Success"){
			$("#sysApps").show();
			$("#appsLogin").hide();
		}else{
			$("#login_failed_div").html(data);
			$("#login_failed_div").show();
		}
		}).fail(function(xhr, textStatus, errorThrown) {
		// if http-post fails
		if (textStatus != "" && errorThrown != "") {
			alert("Somthing went wrong: " + textStatus + "\nError: " + errorThrown);
		} else {
			alert("Error.");
		}
	});

}


function emptyDia(obj) {
	$(obj).find("form").empty();
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
function install(){
	if ($(".selectedPortlet")[0] || $("#contextMenu").data('originalElement')!=undefined) {
		if($(".selectedPortlet")[0]){
			var portlet = $(".selectedPortlet")[0];
		} else {
			var portlet = $("#contextMenu").data('originalElement');
			$("#contextMenu").removeData();
		}
		var portletName =$(portlet).next().html();
		openDia(curStore, portletName);
	} else {
		alert("Wählen Sie ein Bundle!");
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
		$.getJSON("/security/config/app?appstore=" + store + "&name=" + app+"&user=" + otusr + "&pw=" + otpwd, function(json, xhr) {
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
