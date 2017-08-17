var currentSelectedResourceNode;

function createActionsdialog() {
	// DIALOG
	// dialog window for resource actions in resource permissions
	$("#resourceDialog").dialog({
		autoOpen : false,
		draggable : true,
		dialogClass : 'no-close',

		buttons : [ {
			text : "Cancel",
			class : "cancelButton",
			click : function() {
				$(this).dialog("close");
				$(this).find("input[type=checkbox]").prop("checked", false);
				$("#testTree" + currentTreeID).jstree("deselect_node", currentSelectedResourceNode.id);
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
				currentSelectedResourceNode.original.recursive = resourceActionsRecursive();
				$(this).dialog("close");
			}
		} ]
	});
}

/**
 * (Un)check all child boxes. Executed when a checkbox is changed.
 *
 * @param {number}
 *            num Identify checkbox.
 */
function boxCheck(num) {
	var identifyCheck = "input#c" + num;
	var show;
	var container = $(identifyCheck).parent().parent();
	var locationSearch = window.location.search.substring(1);
	// if current box is checked
	var resCheck = "resCheck" + num;
	var resChecked = document.getElementById(resCheck);
	if (resChecked != null){
		show = resChecked.checked;
	}else{
		show = false;
	}


	if ($(identifyCheck).prop("checked")) {

		// check sub boxes
		checkSubBoxes(container);
		// show the resources
		if(!show){
			showRessources(num, true);
		}
		container.find("div:last-child").css("background", "#89A5CC");
		container.find("div:last-child").find("input[type='text']").addClass("highlightInputText");
	} else {
		showRessources(num, false);

		// $("#conArgs" + num).parent().next().hide("fast");
		uncheckSubBoxes(container);
		container.find("div:last-child").css("background", "#E9E9E9");
		container.find("div:last-child").find("input[type='text']").removeClass("highlightInputText");
	}
}

function customizePermission(num, permContainer) {
	// var identifyCheck = '#' + buttonID;
	// check sub boxes
	var resCheck = "resCheck" + num;
	var resChecked = document.getElementById(resCheck);
	if (!(typeof resChecked === undefined) && resChecked!=null){
		show = resChecked.checked;
	}else{
		show = false;
	}
	checkSubBoxes(permContainer);
	// show the resources
	if(!show){
		showRessources(num, true);
	}
	// create input for new Filter if it does not already exist
	if($("#newFilter"+num).length==0){
		appendNewFilter(num);
	}
	if($("#newActions"+num).length==0){
		appendNewActions(num);
	}
	$("#Filter"+num).attr("readonly", "readonly");
	permContainer.find("div:last-child").css("background", "#89A5CC");
	permContainer.find("div:last-child").find("input[type='text']").addClass("highlightInputText");

	// Check the hidden checkbox as flag for later usage
	$("input#c" + num).prop("checked", true);
}

/**
 * Check child-checkboxes. Style Highlight.
 *
 * @param {String}
 *            obj String-ID of checkbox.
 */
function checkSubBoxes(parent) {
	parent.find("div:last-child>input[type='checkbox'].m").prop('checked', true);
	parent.find("div>input[type='text']").addClass("highlight");
	parent.find("div:last-child").find("label.lb").addClass("highlight");
}
function displayRessources(num){
	var locationSearch = window.location.search.substring(1);
	if (locationSearch.indexOf("newperms")!=-1 && !document.getElementById("c"+num).checked){
		return;
	}else if (locationSearch.indexOf("defaultpolicy")!=-1){
		if(typeof num == "number" && num>-2){
			num = num+1+"-1";
		}
	}

	var checked = !document.getElementById("resCheck"+num).checked
	showRessources(num, checked);
}

/**
 * Uncheck child-checkboxes. Remove Highlight.
 *
 * @param {String}
 *            obj String-ID of checkbox.
 */
function uncheckSubBoxes(parent) {
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
 * Executed when paragraph in accessPerm is clicked. So it shows the div
 * (resources affected).
 *
 * @param {number}
 *            num Identify the paragraph and toggle next element (=div)
 */
function showRessources(num, check) {
	var display = $("#conArgs" + num).parent().next().css("display");
	if (check) {
		writeResourceInDia(num, false);
		$("#testTree"+num).show();
	} else {
		$("#testTree"+num).hide();
		$("#conArgs" + num).parent().next().hide();
		$("#conArgs" + num).parent().next().next().hide();
		$("#conArgs" + num).parent().next().next().find("input:checkbox").prop("checked", false);
	}
}

function writeResourceInDia(num, newFilter) {
	// GET to server, for initializing the resource-transmission
	var filter;
	if ($("label[for='c" + num + "']").parent().next().find("input.newFilterInput[type='text']").length == 0){
		filter = $("label[for='c" + num + "']").parent().next().find("input.FilterInput[type='text']").val();;
	}else{
		if ($("label[for='c" + num + "']").parent().next().find("input.newFilterInput[type='text']").val().length == 0){
			filter = $("label[for='c" + num + "']").parent().next().find("input.FilterInput[type='text']").val();
		}else{
			filter = $("label[for='c" + num + "']").parent().next().find("input.newFilterInput[type='text']").val();
		}
	}
	if (newFilter){
		$.jstree.destroy();
	}
	var actions = "";

	var preActions = $("label[for='c" + num + "']").parent().next().find("input:checkbox:checked").map(function() {
		return $(this).next("label").text().split(' ');
	});
	for (var i = 0; i < preActions.length; i++) {
		if (i != 0) {
			actions = actions + ",";
		}
		actions = actions + preActions[i];
	}

	if (filter == undefined)
		filter = "";
	var newNode = "";
	var selectedTopNodes;
	var arr1 = new Array();
	arr1 = filter.split(",");
	var length = arr1.length;
	var url = "/service/filteredresources?user=" + otusr + "&pw=" + otpwd +"&";

	for (var i = 0; i < length; i++) {
		if (i > 0)
			url += "&";
		url += arr1[i];
	}
	url += "&action=" + actions;

	// Properly resource transmission
	$("#testTree" + num).jstree("refresh");
	if (!($("#testTree" + num).hasClass("jstree"))) {
		$("#testTree" + num).jstree({
			"core" : {
				"animation" : 0,
				"check_callback" : true,
				"themes" : {
					"stripes" : true
				},
				'data' : {
					// "url" : "/service/resourceperm",
					"url" : url,
					'data' : function(node) {
						return {
							'id' : node.id
						};
					}
				}
			},
			"types" : {
				"toplevel" : {
					"icon" : "images/tree_small.png"
				},
				"leaf" : {
					"icon" : "images/leaf_small.png"
				},
				"default" : {
					"icon" : "images/branch_small.png"
				},
				"reference" : {
					"icon" : "images/link_small.png"
				}
			},
			"checkbox" : {
				"whole_node" : true,
				"keep_selected_style" : false,
				"three_state" : false
			},
			"plugins" : [ "checkbox", "wholerow", "types" ]
		}).bind("select_node.jstree", function(event, data) {
			currentTreeID = num;
			var nodeSelected = data.node.text;
			var nodeSelectedID = data.node.id;
			var parentOfSelected = data.node.parent;
			var parentIsSelected = $("#testTree" + num).jstree("is_selected", data.node.parent);
			var currentNode;
			var currentMethod;

			currentNode = nodeSelectedID;
			currentSelectedResourceNode = $("#testTree" + num).jstree(true).get_node(currentNode);
			currentMethod = $("#testTree" + num).jstree(true).get_node(currentNode).original.method;
			$("#resourceDialog").find("input[type='checkbox']").prop("checked", false);
			RELOADselectMethodsForThis(currentMethod);

			$("#resourceDialog").dialog("open");
		}).bind("deselect_node.jstree", function(event, data) {

		}).bind("hover_node.jstree", function(e, data) {
			newNode = data.node.text;
			var newNodeID = data.node.id;
			var display = $("#actions" + num + " .wrapActions").css("display");
			if (display != "none")
				return;

			$("#" + newNodeID).addClass("hasFocus");

			setTimeout(function() {
				$("#actions" + num).find(" div.justShowActions").find("input[type='checkbox']").prop("checked", false);

				if ($("#" + newNodeID).hasClass("hasFocus")) {
					var tree = $("#testTree" + num);
					var node = $("#testTree" + num).jstree(true).get_node(newNodeID);
					var checkbox = $.jstree.plugins.checkbox;
					if (node.length != 0 /* && checkbox.is_checked(node) */) {
						var lastNodeMethod = node.original.method;
						var recursive = node.original.recursive;
						var isReady = showPreMethodsForThis(lastNodeMethod, num, recursive);

						var bodyRect = document.body.getBoundingClientRect();
						var elemRect = tree[0].getBoundingClientRect();
						var offset = elemRect.top - bodyRect.top;

						if (isReady == true) {
							// var offset = $("#testTree" + num).find("#" +
							// newNodeID).offset().top;
							$("#actions" + num).show("fast");
							$("#actions" + num).find(" div.justShowActions").css("position", "fixed");
							$("#actions" + num).find(" div.justShowActions").css("top", offset - 55);
							$("#actions" + num).find(" div.justShowActions").show("50");
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

	$("#actions" + num + " div.checkResActions").click(function() {
		var actionsString;
		actionsString = getResourceActions();
		currentSelectedResourceNode.original.method = actionsString;
		$("#actions" + num + " .wrapActions").hide(50);
		$("#actions" + num + " p").html("");
		$("#actions" + num + " .wrapActions").find("input[type='checkbox']").prop("checked", false);
	});
	$("#testTree" + num).parent().show();
	// $("#testTree" + num).show();
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

var ind = 10; // index for additional permissions
var additionalPermMask = "<div class='additional' id='additionalMask'>" + "<div class='headLine'>"
		+ "<input type='button' onclick='additionalPrepareBoxCheck()' class='p' id='addPermCheck' value='Apply'>"
		+ " <input style='margin-left: 0px; width: 420px !important' type='text' id='addPermName' title='Please provide the permission name.'> </input>"
		+ " <br> <input type='radio' name='grantdeny' class='p' id='addGrantCheck' onchange = 'checkGrant()'>"
		+ " <label for='addPermGrantLabel'> Grant </label>" + " <input type='radio' name='grantdeny' class='p' id='addDenyCheck' onchange = 'checkDeny()'> "
		+ "<label for = 'addPermDenyLabel'> Deny </label> " + "</div>" + " <div class='bodyOfPerm'> Filter: <br> "
		+ "<input type='text' title='Please provide the resource.' id='addPermFilter'>"
		+ " <br> Action: <br> <input type='text' title='Please provide the specific methods.' id='addPermActions'></input>" + "</div></div>";
var additionalPerMaskBussy = false;
function insertAdditionalMask() {
	if (!additionalPerMaskBussy) {
		$("#additionalPermButton").before(additionalPermMask);
		$("input#addGrantCheck").prop("checked", true);
		additionalPerMaskBussy = true;
	}
}

function removeAdditionalMask() {
	$("#additionalMask").remove();
	$("#additionalPermButton").remove();
	additionalPerMaskBussy = false;
}

function checkGrant() {
	var identify = "addGrantCheck";
	if ($("#" + identify).prop("checked") == true) {
		$("#" + identify).next().css("font-weight", "bold");
		// if GRANT is checked, uncheck DENY
		$("#" + identify).next().next("input[type='checkbox']").prop("checked", false);
		$("#" + identify).next().next().next().css("font-weight", "normal");
	} else {
		$("#" + identify).next().css("font-weight", "normal");
	}
}

function checkDeny() {
	var identify = "addDenyCheck";
	if ($("#" + identify).prop("checked") == true) {
		$("#" + identify).next().css("font-weight", "bold");
		// if DENY is checked, uncheck GRANT
		$("#" + identify).prev().prev("input[type='checkbox']").prop("checked", false);
		$("#" + identify).prev().css("font-weight", "normal");
	} else {
		$("#" + identify).next().css("font-weight", "normal");
	}
}

function additionalPrepareBoxCheck() {
	var permName = $("input#addPermName").val();
	var filter = $("input#addPermFilter").val();
	var actions = $("input#addPermActions").val();
	var mode = $("input#addGrantCheck").prop("checked");
	var modename = "grant";
	if (!mode)
		modename = "deny";
	removeAdditionalMask();
	writeInDia(permName, filter, actions, numberOfPermEntry + "" + curAppId, formOfDialog, modename);
	var checkID = "c" + numberOfPermEntry++ + "" + curAppId;
	var cb = document.getElementById(checkID);
	cb.checked=true;
	cb.onchange();
	$(formOfDialog).append(additionalPermButton);
	$("button#additionalPermButton").button({
		icons : {
			primary : "ui-icon-plus"
		}
	});
}

function RELOADselectMethodsForThis(currentMethod) {
	if (currentMethod.indexOf(",") != -1) {
		var methods;
		methods = currentMethod.split(",");
		for (var x = 0; x < methods.length; x++) {
			$("#resourceDialog").find("input[name='" + methods[x] + "']").prop("checked", true);
		}
	} else {
		$("#resourceDialog").find("input[name='" + currentMethod + "']").prop("checked", true);
	}

}

function showPreMethodsForThis(currentMethod, num, recursive) {
	if (currentMethod.indexOf(",") != -1) {
		var methods;
		methods = currentMethod.split(",");
		for (var x = 0; x < methods.length; x++) {
			$("#actions" + num + " .justShowActions").find("input[name='" + methods[x] + "']").prop("checked", true);
		}
	} else {
		$("#actions" + num + " .justShowActions").find("input[name='" + currentMethod + "']").prop("checked", true);
	}
	if (recursive)
		$("#actions" + num + " .justShowActions").find("input[id='recursiveyes']").prop("checked", true);
	else
		$("#actions" + num + " .justShowActions").find("input[id='recursiveno']").prop("checked", true);
	return true;
}

function RELOADgetResourceActions() {
	if ($("#resourceDialog").find("input:checkbox:checked").length > 1) {
		var actionsArray = $("#resourceDialog").find("input:checkbox:checked").map(function() {
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
		var action = $("#resourceDialog").find("input:checkbox:checked").next("label").text();
		return action;
	}
}

function resourceActionsRecursive() {
	var radios = $("#resourceDialog").find("input:radio");
	var radio = $("#resourceDialog").find("input:radio[id='recursiveyes']");
	return recursive = radio[0].checked;
}