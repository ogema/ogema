var curAppId;
var curAppName;
var parentOfDialog = "#wrap";
var formOfDialog = "#wrap form";
var numberOfPermEntry = 0;

var additionalPermButton = "<button id='additionalPermButton' onclick='insertAdditionalMask()' type='button'>Add Permission</button>";
var checkAll = "<input type='checkbox' onchange='checkAllBoxes(this, \"c\", \"true\")' name='chkAll' </input><label class='lb' for='chkAll'>Check All</label>";
var startButton = "<button id='startButton' onclick='startBundle()' type='button'>Start Bundle</button>";
var removeAllButton = "<button id='removeAllButton' onclick='removeAll()' type='button'>Remove All Permissions</button>";
var defaultkey = "defaultPolicy";
var dialogBundleName = "<div id='dialogBundleName'></div>"

function NewPerms() {
	var widthWindow = $(window).width() * 0.8;
	var heightWindow = $(window).height() * 0.8;
	var curStore = getUrlVals()["store"];
	var currentTreeID;

	$(function() {
		pageAction();
	});

	// --------------------------------- F U N C T I O N S
	// -------------------------------------

	function pageAction() {

		/* check if the resourceDialog already exists */
		createActionsdialog();
		
		var action = undefined, appID = undefined; return2url=false; returnUrl = undefined;
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
				else if (arr1[i] == "return2url") {
					return2url = true;
					returnUrl = arr2[i];
				}
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
					getGrantedPerms(curAppId, parentOfDialog, formOfDialog, null);
				} else if (action == "listperms"){ // start listing all granted and
					// demanded permissions
					appendDialog(parentOfDialog);
					getPermList(curAppId, parentOfDialog, formOfDialog);
					$(".installButton").hide();

				}
			}else{
				if (action == "defaultpolicy"){
					curAppId = -1;
					appendDialog(parentOfDialog);
					getDefaultPerms(-1, parentOfDialog, formOfDialog, null);
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
			evalPermData(json, appNumber, dialog, form, false);
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
				$('.ui-dialog-buttonpane').find('button:contains("Quit")').button({
					icons : {
						primary : 'ui-icon-close'
					}
				});
			},
			buttons : [ {
				text : "Quit",
				class : "cancelButton",
				click : function() {
					// $(this).dialog("close");
					// $(this).find("input[type=checkbox]").prop("checked",
					// false);
					window.close();
				}
			},
			// Save-Button
			{
				text : "Save settings",
				class : "installButton",
				click : function() {

					var checkInput = checkingInput($(this));
					var checkInputText = checkingRessourceInput($(this));

					// if validation is ok
					if (checkInput == true) {
						if (checkInputText == true) {
							// if everything is ok, get the filled in input and
							// do something with it....
							var jsonContent = getGrantedBack($(this));
							sendPermsToServerWithRedirect(curAppId, jsonContent, parentOfDialog, formOfDialog, returnUrl);						
						} else {
							alert("Invalid input.");
						}
					}
				}
			} ]
		});
	}
	
/**
 * Get all granted and demanded Permissions to fill the Dialog
 */
	
	function getPermList(appNumber, dialog, form){
		
		$.getJSON("/security/config/listpermissions?id="+appNumber, function(json, xhr){
			
			$(dialog).dialog("option", "title", "Demanded and granted permissions for the bundle " + json.granted.bundlename);
			$(".ui-dialog-titlebar").addClass("permsDialog");
			$(form).empty();
			//display the Bundlename as a header
			if($("#dialogBundleName").length==0){
				$(dialog).prepend(dialogBundleName);
			}else{
				$("#dialogBundleName").empty();
			}
			$("#dialogBundleName").text("Demanded and granted permissions for the bundle " + json.granted.bundlename)
			//granted Permissions
			$(form).append('<div id="granted" class="headline"><label class="lb">Granted permissions:</label>');
			writeGrantedInDia(json.granted, appNumber, dialog, form, true);
			
			//demanded Permissions
			$(form).append('<div id="demanded" class="headline"><label class="lb">Demanded permissions:</label>');
			evalPermData(json.demanded, appNumber, dialog, form, true);
			
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
/**
 * 	Write the demanded Permissions into the Dialog
 * 
 * @param{Boolean}
 * 			listonly: List view or new Installation
 * @param{Object}
 * 			json: json data containing the demanded Permissions
 */
	
	function evalPermData(json, appNumber, dialog, form, listonly) {
		if(!listonly){
			$(dialog).dialog("option", "title", "Permissions demanded by bundle " + json.name);
			$(".ui-dialog-titlebar").addClass("permsDialog");
			$(form).empty();
		}
		
		// loops through each permission-string
		for (var i = 0; i < json.permissions.length; i++) {
			// get the single elements of each permission
			// get permission name
			var permName = getPermName(json.permissions[i]);
			// get permission resource
			var permResource = getPermResource(json.permissions[i]);
			// get permission method
			var permMethod = getPermMethod(json.permissions[i]);
			var id = "" + appNumber + i;
			// write elements in dialog
			writeInDia(permName, permResource, permMethod, id, form, "grant", listonly);
			numberOfPermEntry++;
		}

		if(!listonly){
			$(form).append(additionalPermButton);
			$(form).prepend(checkAll);
			if($("#dialogBundleName").length==0){
				$(dialog).prepend(dialogBundleName);
			}else{
				$("#dialogBundleName").empty();
			}
			$("#dialogBundleName").text("Permissions demanded by bundle " + json.name)
		}
		
		// adding an icon to the AddPermission-button
		$("button#additionalPermButton").button({
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
function getGrantedPerms(appNumber, dialog, form, filtered) {
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
			if(json.editable=="false"){
				alert("Bundles that are a part of Ogema Core can not be edited.")
				return;
			}
			// clear the dialog before for-loop
			$(dialog).dialog("option", "title", "Permissions granted to the bundle " + json.bundlename);
			$(".ui-dialog-titlebar").addClass("permsDialog");
			$(form).empty();
			
			writeGrantedInDia(json, appNumber, dialog, form, false)
			
			//if Permissions were filtered
			if (filtered != null){
				$(form).append('<div id="filtered" class="headline"><label class="lb">The following permissions were filtered:</label>');
				writeFilteredInDia(filtered, appNumber, dialog, form);
			}
			if($("#dialogBundleName").length==0){
				$(dialog).prepend(dialogBundleName);
			}else{
				$("#dialogBundleName").empty();
			}
			$("#dialogBundleName").text("Permissions granted to the bundle " + json.bundlename)
			$(form).append(additionalPermButton);
			$(form).append(startButton);
			$(form).append(removeAllButton);

			// adding an icon to the AddPermission-button
			$("button#additionalPermButton").button({
				icons : {
					primary : "ui-icon-plus"
				}
			});
			$("button#startButton").button({
				icons : {
					primary : "ui-icon-play"
				}
			});
			$("button#removeAllButton").button({
				icons: {
					primary: "ui-icon-trash"
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


function getDefaultPerms(appNumber, dialog, form, filtered){
	$.getJSON("/security/config/defaultpolicy", function(json, xhr) {
		// get permissions from server for the given url
		{
			// clear the dialog before for-loop
			$(dialog).dialog("option", "title", "Permissions granted by " + json.bundlename);
			$(".ui-dialog-titlebar").addClass("permsDialog");
			$(form).empty();
			// loops through each permission-string
			
			writeGrantedInDia(json, -1, dialog, form, false)
			
			//if Permissions were filtered
			if (filtered != null){
				$(form).append('<div id="filtered" class="headline"><label class="lb">The following permissions were filtered:</label>');
				writeFilteredInDia(filtered, appNumber, dialog, form);
			}
			if($("#dialogBundleName").length==0){
				$(dialog).prepend(dialogBundleName);
			}else{
				$("#dialogBundleName").empty();
			}
			$("#dialogBundleName").text("Permissions granted by " + json.bundlename)
			$(form).append(additionalPermButton);
			$(form).append(removeAllButton);

			// adding an icon to the AddPermission-button
			$("button#additionalPermButton").button({
				icons : {
					primary : "ui-icon-plus"
				}
			});
			$("button#removeAllButton").button({
				icons: {
					primary: "ui-icon-trash"
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

/**
 * 	Write the grated Permissions into the Dialog
 * 
 * @param{Boolean}
 * 			listonly: List view or new Installation
 * @param{Object}
 * 			json: json data containing the grated Permissions
 */
function writeGrantedInDia(json, appNumber, dialog, form, listonly){
	
	for (var i = 0; i < json.policies.length; i++) {
				var policy = json.policies[i];
							
				//if the bundle is not default Policy and conditions.length = 0
				if (appNumber!=-1 && policy.conditions.length == 0)
					continue;
				
				var modename = policy.mode.toLowerCase();
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
					var permMethod =entry.actions;
					if(permMethod != undefined){
						permMethod = methodsToArray(permMethod);
					}
					var id = "" + appNumber + i + j;
					// write elements in dialog
					writeInDia(permName, permResource, permMethod, id, form, modename, listonly);
					var selector = "#c" + id;
					changeGrantedHeadline(id, json.policies[i].name, entry, appNumber);
					$(selector).hide();
					numberOfPermEntry++;
				}
				if (j > 0)
					$("#heading").add("<br> <em>Remove or customize granted permissions</em>");
			}
}

/**
 * Write the filtered Permissions into the Dialog
 * 
 * @param {Object}
 * 		 filtered json data containing the filtered Permissions
 * 
 */
function writeFilteredInDia(filtered, appNumber, dialog, form){
	
	for (var i = 0; i<filtered.filtered.length; i++){
		var entry = filtered.filtered[i];
		var mode = entry.mode.toLowerCase();
		var permName = entry.permname;
		var filter = entry.filter;
		var permMethod = entry.actions;
		if (permMethod != undefined){
			permMethod = methodsToArray(permMethod);
		}
		var id = "f"+appNumber+i;
		writeInDia(permName, filter, permMethod, id, form, mode, true)
		
	}
}


/**
 * Write the Permissions into the Dialog
 * 
 * @param {String} name
 * 			Name of the Permission
 * @param {String} resource
 * 			Filter of the Permission
 * @param {Array} method
 * 			Array of Actions of the Permission
 * @param {Number} id
 * 			Bundle ID, -1 for Default Policy
 * @param {String} modename
 * 			Access decision, allow/deny
 * @param {Boolean} listonly
 * 			List-view or New Installation/ Customization
 */
function writeInDia(name, resource, method, id, form, modename, listonly) {

	// adds dialog widget
	$(".dialogActions").dialog({
		autoOpen : false
	});
	
	
	
	if (typeof (method) != "object") {
		var tmpmethod = [];
		if (method != "")
			tmpmethod.push(method);
		method = tmpmethod;
	}
	
	buildHtmlContainer(form, id, name, listonly, method, resource);
	
	// change headline color if the permission is in negative mode	

	var selector = "#c" + id;
	if (modename.toLowerCase() == "deny") {
		$(selector).parent().css('background-color', '#FF6600');
		$(selector).parent().append("<meta name='mode' content='deny'></meta>");
	} else {
		$(selector).parent().append("<meta name='mode' content='allow'></meta>");
	}
}

function buildHtmlContainer(form, id, name, listonly, method, resource){
	//build the PermContainer
	var permCont = document.createElement("DIV");
	permCont.id = "permContainer"+id;
	permCont.className = "wrapsOnePerm";
	
	//build the Headline
	var headline = buildHtmlHeadline(id, name, listonly);
	
	//build the bodyOfPerm
	var bodyOfPerm = buildBodyOfPerm(id, name, listonly, method, resource);
	
	//append the headline & the body to the PermContainer
	permCont.appendChild(headline);
	permCont.appendChild(bodyOfPerm);
	
	//append the PermContainer to the Form
	$(form).append(permCont);
}

function buildBodyOfPerm(id, name, listonly, method, resource){
	//build the bodyOfPerm
	var bodyOfPerm=document.createElement("DIV");
	bodyOfPerm.className="bodyOfPerm";
	
	//if the permission is not AllPermission
	if(name.indexOf("java.security.AllPermission")==-1){
		//create and fill the Filter
		var fInput = document.createElement("INPUT");	
		fInput.type = "text";		
		fInput.setAttribute("value", resource);
		fInput.title="Please provide the resource.";	
		fInput.id="Filter"+id;
		fInput.className="FilterInput";
		
		if(listonly)
			fInput.readOnly=true;	
		
		bodyOfPerm.innerHTML = "Filter:";
		bodyOfPerm.appendChild(document.createElement("br"));
		bodyOfPerm.appendChild(fInput);
		bodyOfPerm.appendChild(document.createElement("br"));
		bodyOfPerm.innerHTML = bodyOfPerm.innerHTML + "Action:"
		bodyOfPerm.appendChild(document.createElement("br"));
		
		//if actions exist
		if(method[0]!=undefined && method[0]!=null){		
			for (var v = 0; v < method.length; v++) { 
				//for each action: add to the bodyOfPerm	
				
				if(!listonly){
					//input for the actions if the permission is to be modified
					var aInput = document.createElement("INPUT");
					
					aInput.id="m"+id+v;
					aInput.type = "checkbox";
					aInput.setAttribute("onchange", "singleHighlight("+id+v+")");
					aInput.className="m";
					
					bodyOfPerm.appendChild(aInput);
				}
				//labels for the actions
				var aLabel = document.createElement("LABEL");
				
				aLabel.className="lb";
				aLabel.htmlFor="m"+id+v;
				aLabel.textContent = method[v];
				
				bodyOfPerm.appendChild(aLabel);

				if (v != method.length - 1) {
					bodyOfPerm.appendChild(document.createElement("br"));
				}
			}
		}
		//ResourceTree
		if(name.indexOf("ResourcePermission")!=-1){
			var resDiv = document.createElement("DIV");
			var testTree = document.createElement("DIV");
			
			resDiv.className = "resDiv";
			resDiv.style.display = "none";
			
			testTree.className = "testTree";
			testTree.id="testTree"+id;			
			
			resDiv.appendChild(testTree);
			bodyOfPerm.appendChild(resDiv);
		}
		
	}
	return bodyOfPerm;
}

function appendNewFilter(id){
	var newFilter = document.createElement("DIV");
	var newfInput = document.createElement("INPUT");
	
	newFilter.innerHTML = "New Filter: <br>";
	newFilter.id="newFilter"+id;
	
	newfInput.type = "text";
	newfInput.title="Please provide the resource.";
	newfInput.innerHTML = newfInput.innerHTML + "<br>";
	newfInput.id="newFilterInput"+id;
	newfInput.className="newFilterInput";
	
	$(newFilter).insertAfter("#Filter"+id);
	$(newfInput).insertAfter("#newFilter"+id);
	
	$("#newFilterInput"+id).change(function(){			
		writeResourceInDia(id, true);
	});
		  
	
	
}

function buildHtmlHeadline(id, name, listonly){
	//build the Headline
	var headline = document.createElement("DIV");
	var hInput = document.createElement("INPUT");
	var hLabel = document.createElement("LABEL");
	
	headline.className="headline";
	
	hInput.id = "c"+id;
	hInput.className = "p";
	hInput.type="checkbox";
	
	if(listonly)
		hInput.style.display = "none";
	
	hLabel.className = "lb";
	hLabel.htmlFor = hInput.id;
	hLabel.textContent = name;
	
	headline.appendChild(hInput);
	headline.appendChild(hLabel);
	
	//enable the selection of the permission and build 
	//the ButtonSet if the permission is to be modified
	if (!listonly){
		id=""+id;
		hInput.setAttribute("onchange", "boxCheck('"+id+"')");
		
		var grantedButtons = document.createElement("DIV");
		grantedButtons.id= "grantedButtons"+id;
		grantedButtons.setAttribute('align', 'right');
		
		//add the Checkbox for resource customization if the 
		//Permission is a ResourcePermission
		if (name.indexOf("ResourcePermission")!=-1){
			var resInput = document.createElement("INPUT");
			var resLabel = document.createElement("LABEL");
			
			resInput.id = "resCheck"+id;
			resInput.type = "checkbox";
			resInput.className = "resCheck";
			resInput.setAttribute("onchange", "displayRessources("+id+")");
			
			resLabel.className = "resCheck";
			resLabel.htmlFor=resInput.id
			resLabel.textContent="No customization of Resource Permission";
			
			grantedButtons.appendChild(resInput);	
			grantedButtons.appendChild(resLabel);				
		}
		headline.appendChild(grantedButtons);
	}
	return headline;	
}

/**
 * Assign the Functions to the Remove and Customize Buttons
 * 
 * @param {Number} id
 * 			ID of the Permission
 * @param {String} policyname
 * 			Name of the Permission
 * @param {String} permDescr
 * 			Filter value of the Permission
 */
function changeGrantedHeadline(id, policyname, permDescr, appNumber) {
	var selector = "#grantedButtons" + id;
	// Create an input type dynamically.
	var removeButton = document.createElement("input");
	var name = [];
	var entry = [];
	name[0]=policyname;
	entry[0]=permDescr;

	removeButton.type = "button";
	removeButton.value = "Remove";
	removeButton.setAttribute("align", "right");
	// Assign different attributes to the element.
	removeButton.onclick = function() { // Note this is a function
		sendRemovePermission(appNumber, name, entry, 1);
	};
	removeButton.className = ".ui-button.cancelButton";
	// Append the element in page (in span).
	$(selector).append(removeButton);

	var customizeButton = document.createElement("input");
	customizeButton.type = "button";
	customizeButton.value = "Customize";
	customizeButton.setAttribute("align", "right");
	var buttonId = 'cB' + id;
	customizeButton.id = buttonId;
	var container = $('#permContainer' + id)
	// Assign different attributes to the element.
	customizeButton.onclick = function() { // Note this is a function
		customizePermission("" + id, container);
	};
	// Append the element in page (in span).
	$(selector).append(customizeButton);
	
	
}

/**
 * Post to Server to remove a Permission
 *
 * @param {String} policyname
 * 			Name of the Permission
 * @param {String} permDescr
 * 			Filter value of the Permission
 * @param {Number} count
 * 			Number of Permissions to be removed (currently always 1)
 */
function sendRemovePermission(appNumber, policyname, permDescr, count) {
	var toBeRemoved = new Object();
	toBeRemoved['id'] = appNumber;
	toBeRemoved['count']=count;
	
	for(var i = 0; i<count; i++){		
		if (policyname[i].indexOf(defaultkey)!=-1){
			toBeRemoved['mode'+i]=policyname[i].substring(policyname[i].indexOf(defaultkey)+defaultkey.length)
		}else{
			toBeRemoved['mode'+i]=null;
		}
		toBeRemoved['policyname'+i] = policyname[i];
		toBeRemoved['permission'+i] = permDescr[i];	
	}

	var content = JSON.stringify(toBeRemoved);

	$.post("/security/config/removepermission?id=" + appNumber+"&count=" + count, {
		remove : content
	}, function(data, status) { // if successfull
		// refresh the view
		if (appNumber!=-1){
			setTimeout(function() {			
				getGrantedPerms(curAppId, parentOfDialog, formOfDialog, null);
				}, 3000);
			}else{
			setTimeout(function() {			
				getDefaultPerms(appNumber, parentOfDialog, formOfDialog, null);
				}, 3000);
			}
		
		alert("Data send to server for appID: " + appNumber + "\nResponse: " + data + "\nStatus: " + status);
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
 * Check all Checkboxes
 * 
 * @param {Object} allcheck
 * 			This Checkbox
 * @param {String} idStr
 * 			prefix of the IDs of the Checkboxes which should be checked/unchecked
 * @param {Boolean} change
 * 			fire onchange event
 */
function checkAllBoxes(allcheck, idStr, change) {
    var checkboxes = $("input:checkbox");
    var changetrue = (change === 'true');
        
    if (allcheck.checked) {
        for (var i = 0; i < checkboxes.length; i++) {
            if (checkboxes[i].id.indexOf(idStr) == 0) {
                checkboxes[i].checked = true;
                if (changetrue==true){
                	checkboxes[i].onchange(); 	
                }
            }
        }
    } else {
        for (var i = 0; i < checkboxes.length; i++) {
            if (checkboxes[i].id.indexOf(idStr) == 0) {
                checkboxes[i].checked = false;
                if (changetrue==true){
                	checkboxes[i].onchange();
                }
            }
        }
    }
}

function startBundle() {
	var portletID = curAppId;
	$.getJSON("/security/config/installedapps?action=start&app=" + portletID, function(json) {
		alert(json.statusInfo);
	});	
}
function removeAll(){
	
	var conf = confirm("Are you sure you want to remove all Permissions?")
	if (conf!=true)
		return;
	
	$.post("/security/config/removeall?id="+curAppId, {
		
	}, function(data, status) { // if successfull
		// refresh the view
		if (curAppId!=-1){
			setTimeout(function() {			
				getGrantedPerms(curAppId, parentOfDialog, formOfDialog, null);
				}, 3000);
			}else{
			setTimeout(function() {			
				getDefaultPerms(curAppId, parentOfDialog, formOfDialog, null);
				}, 3000);
			}
		
		alert("Data send to server for appID: " + curAppId + "\nResponse: " + data + "\nStatus: " + status);
	}).fail(function(xhr, textStatus, errorThrown) {
		// if http-post fails
		if (textStatus != "" && errorThrown != "") {
			alert("Somthing went wrong: " + textStatus + "\nError: " + errorThrown);
		} else {
			alert("Error.");
		}
	});
}

function toMainPage() {
	window.open("/security-gui/index.html", "_self");
}
// --------------------------------- E N D - F U N C T I O N S
// -------------------------------------
