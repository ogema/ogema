/**
 * Build the outer HTML container for the permission
 * @param {object} form
 * 			form Object
 * @param {Number} id
 * 			id for the Container (app id + number of permission)
 * @param {String} name
 * 			name of the permission
 * @param {Boolean} listonly
 * 			only list permissions
 * @param {String} method
 * 			filter value
 * @param {Object} resource
 * 			Actions array
 */
function buildHtmlContainer(form, id, name, listonly, method, resource){
	//build the PermContainer
	var permCont = document.createElement("DIV");
	permCont.id = "permContainer"+id;
	permCont.className = "wrapsOnePerm";
	
	//build the Headline
	var headline = buildHtmlHeadline(id, name, listonly);
	
	//build the bodyOfPerm
	var bodyOfPerm = buildHtmlBodyOfPerm(id, name, listonly, method, resource);
	
	//append the headline & the body to the PermContainer
	permCont.appendChild(headline);
	permCont.appendChild(bodyOfPerm);
	
	//append the PermContainer to the Form
	$(form).append(permCont);
}


/**
 * Create the headline of the permission
 * @param {Number} id
 * 			id for the Container (app id + number of permission)
 * @param {String} name
 * 			name of the permission
 * @param {Boolean} listonly
 * 			only list permissions
 * @returns {Object} headline
 * 			HTML Object containing the headline
 */
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
 * Create the body of the Permission
 * @param {Number} id
 * 			id for the Container (app id + number of permission)
 * @param {String} name
 * 			name of the permission
 * @param {Boolean} listonly
 * 			only list permissions
 * @param {String} method
 * 			filter value
 * @returns {Object} bodyOfPerm
 * 			Html Object containing the body
 */
function buildHtmlBodyOfPerm(id, name, listonly, method, resource){
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


/**
 * Create the input for the new filter when customizing a permission
 * @param {number} id
 * 				id of the permission container
 */
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


/**
 * Create the input for additional Actions on customization of a permission
 * @param {number} id
 * 				id of the permission container
 */
function appendNewActions(id){
	var newActions = document.createElement("DIV");
	var newaInput = document.createElement("INPUT")
	
	newActions.innerHTML = "Additional Actions: <br>";
	newActions.id="newActions"+id;
	
	newaInput.type="text";
	newaInput.title="Provide additional actions";
	newaInput.className = "newActionsInput";
	newaInput.id = "newActionsInput"+id;
	
	$(newActions).insertAfter("#newFilterInput"+id);
	$(newaInput).insertAfter("#newActions"+id);
}


/**
 * Create and assign the Functions to the Remove and Customize Buttons
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