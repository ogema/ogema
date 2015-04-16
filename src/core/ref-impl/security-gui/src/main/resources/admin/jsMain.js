// code übernommen aus "http://codepen.io/agence-web-coheractio/pen/pEnoA"
// The MIT License (MIT)
// Copyright (c) 2013 - AGENCE WEB COHERACTIO

$.widget("ui.dialog", $.ui.dialog, {
	options : {
		clickOutside : false, // Determine if clicking outside
		// the dialog shall close it
		clickOutsideTrigger : "" // Element (id or class)
	// that triggers the dialog
	// opening
	},

	open : function() {
		var clickOutsideTriggerEl = $(this.options.clickOutsideTrigger);
		var that = this;

		if (this.options.clickOutside) {
			// Add document wide click handler for the current
			// dialog namespace
			$(document).on("click.ui.dialogClickOutside" + that.eventNamespace, function(event) {
				if ($(event.target).closest($(clickOutsideTriggerEl)).length == 0 && $(event.target).closest($(that.uiDialog)).length == 0) {
					that.close();
				}
			});
		}

		this._super(); // Invoke parent open method
	},

	close : function() {
		var that = this;

		// Remove document wide click handler for the current
		// dialog
		$(document).off("click.ui.dialogClickOutside" + that.eventNamespace);

		this._super(); // Invoke parent close method
	},

});

var client = null;
var selectedApp;
var permContentToSend = "";
var uploadApp = "";

$(function() {

	$("#store").focus(function() {
		$("#store").parent().css("border", "1px solid #38B5F9");
	});
	$("#store").blur(function() {
		$("#store").parent().css("border", "1px solid grey");
	});

	$("#dialUpload").dialog({
		autoOpen : false,
		resizeable : true,
		draggable : true,
		close : function() {
			clearAppDisplay();
			$('div#wrap').css('opacity', '1.0');
			$('div#footer').css('opacity', '1.0');
			$('div#header').css('opacity', '1.0');
		},
		open : function() {
			$('div#wrap').css('opacity', '0.3');
			$('div#footer').css('opacity', '0.3');
			$('div#header').css('opacity', '0.3');
		},
		buttons : [ {
			text : "Submit",
			click : function() {
				var jsonContent = getInputBack($(this));
				$(this).dialog("close");
				// function in jsApps.js
				console.log(uploadApp);
				sendPermsToServer(uploadApp, jsonContent, "localTempDirectory");
				uploadApp = "";
			}
		} ],
		minWidth : 300,
		minHeight : 100,
		width : $("div#wrap").width(),
		height : $(window).height() - 500
	});
	$("#dialUpload").html("<form></form>");

	$("#webResourceDisplay").dialog({
		autoOpen : false,
		resizeable : true,
		draggable : true,
		close : function() {
			clearAppDisplay();
			$('div#wrap').css('opacity', '1.0');
			$('div#footer').css('opacity', '1.0');
			$('div#header').css('opacity', '1.0');
		},
		open : function() {
			$('div#wrap').css('opacity', '0.3');
			$('div#footer').css('opacity', '0.3');
			$('div#header').css('opacity', '0.3');
		},
		minWidth : 300,
		minHeight : 100,
		width : $("div#wrap").width(),
		height : $(window).height() - 500
	});

	$("#editWebResources").dialog({
		autoOpen : false,
		resizeable : true,
		draggable : true,
		buttons : [ {
			text : "Submit",
			click : function() {
				// POST settings to server
				var isReady = getPermContentToSend();
				// wait until getPermContentToSend() is done
				if (isReady == true) {
					var portletID = $(".selectedPortlet")[0].id;
					$.post("/security/config/installedapps?action=setPermission&app=" + portletID, {
						settings : "" + permContentToSend
					}, function(data, status) { // if
						// successfull
						alert(status);
					}).fail(function(xhr) {
						alert(xhr);
					});
					$(this).dialog("close");
				}
			}
		} ],
		close : function() {
			$(this).html("");
			permContentToSend = "";
			$('div#wrap').css('opacity', '1.0');
			$('div#footer').css('opacity', '1.0');
			$('div#header').css('opacity', '1.0');
		},
		open : function() {
			$('div#wrap').css('opacity', '0.3');
			$('div#footer').css('opacity', '0.3');
			$('div#header').css('opacity', '0.3');
		},
		minWidth : 300,
		minHeight : 100,
		width : $("div#wrap").width(),
		height : $(window).height() - 500
	});

	$("#appDisplay").dialog({
		autoOpen : false,
		resizeable : true,
		draggable : true,
		close : function() {
			$(this).html("");
			$('div#wrap').css('opacity', '1.0');
			$('div#footer').css('opacity', '1.0');
			$('div#header').css('opacity', '1.0');
		},
		open : function() {
			$('div#wrap').css('opacity', '0.3');
			$('div#footer').css('opacity', '0.3');
			$('div#header').css('opacity', '0.3');
		},
		minWidth : 300,
		minHeight : 100,
		width : $("div#wrap").width(),
		height : $(window).height() - 300
	});

	$("button#done").button({
		icons : {
			primary : "ui-icon-check"
		},
		text : false,
		disabled : true
	});

	$("#initUp").button({
		icons : {
			primary : "ui-icon-arrowthickstop-1-n"
		}
	});
	$("#diaUpload").dialog({
		autoOpen : false,
		resizable : false,
		draggable : true,
		minWidth : 513,
		minHeight : 300,
		close : function() {
			$('div#wrap').css('opacity', '1.0');
			$('div#footer').css('opacity', '1.0');
			$('div#header').css('opacity', '1.0');
		},
		open : function() {
			$('div#wrap').css('opacity', '0.3');
			$('div#footer').css('opacity', '0.3');
			$('div#header').css('opacity', '0.3');
		}
	});
	// ------------------------------------ SYSTEM APPS
	// ------------------------------------ //
	$("div#systemApps .head").click(function() {
		listSysApps();
	});

	$("div#resources .head").click(function() {
		var display = $("div#resources .part").css("display");
		if (display == "block") {
			$("#jsTree_Resources").jstree("destroy");
		} else {
			showResources();
		}
	});

	// ------------ APP MANAGEMENT
	$("#store").val("enter an appstore...");
	changeTextColor();
	$('input:text').button().css({
		'text-align' : 'left',
		'outline' : 'none',
		'cursor' : 'text',
		'padding' : '2.5px',
		'padding-left' : '10px',
		'margin-left' : '10px'
	});

	$('input[type="submit"]').each(function() {
		$(this).hide().after('<button>').next().button({
			icons : {
				primary : 'ui-icon-search'
			},
			label : $(this).val()
		}).click(function(event) {
			event.preventDefault();
			$(this).prev().click();
		});
	});
	$('input[type="button"]').each(function() {
		$(this).hide().after('<button>').next().button({
			label : $(this).val()
		}).click(function(event) {
			event.preventDefault();
			$(this).prev().click();
		});
	});
	$('input[type="file"]').each(function() {
		$(this).hide().after('<button id="uploadButton">').next().button({
			icons : {
				primary : 'ui-icon-plus'
			},
			label : "Select File"
		}).click(function(event) {
			event.preventDefault();
			$(this).prev().click();
		});
	});

	$("#menu").menu();

	function showAppstores() {
		$("#menu").html("");
		$.getJSON("/security/config/appstores", function(json) {
			for (var i = 0; i < json.appstores.length; i++) {
				$("#menu").append("<li id='" + i + "'>" + json.appstores[i] + "</li>");
			}
			// click on <li> puts name as val
			$("li").click(function() {
				$("input#store").val(json.appstores[this.id]).focus();
				changeTextColor();
				$("ul#menu").toggle("slide", {
					direction : "up"
				});
				// $("#wrapArrow").toggle("slide", {
				// direction : "up"
				// });
			});
		});
	}

	// toggle menu when button clicked
	$("img#button").click(function() {
		var displayAppstores = $("#menu").css("display");

		if (displayAppstores == "none") {
			showAppstores();
			$("#menu").css("display", "block");
		} else {
			$("#menu").html("");
			$("#menu").css("display", "none");
		}

		// $("#menu").toggle("slide", {
		// direction : "up"
		// });

		// $("#wrapArrow").toggle("slide", {
		// direction : "up"
		// });
	});

	// click on head toggles its part
	$("div p.head").click(function() {
		$(this).parent().siblings().find("div.part").hide();
		$(this).parent().find("div.part").toggle("slide", {
			direction : "up"
		}, function() {
			$('html, body').animate({
				"scrollTop" : $(this).offset().top
			}, 'fast')
		});
	});

//	pageAction();
});

// ------------------------------ FUNCTIONS ---------------------------- //

// ------------------ SYSTEM APPS //
function listSysApps() {
	var jsonArr = [];
	$("div#sortableApps").html("");
	$.getJSON("/security/config/installedapps?action=listAll", function(json) {
		for (var v = 0; v < json.length; v++) {
			$("div#sortableApps").append(
					"<div class='column'> <div class='portlet'> <div class='portlet-header' id='" + json[v].id + "'></div> <div class='portlet-content'>"
							+ json[v].name + "</div> </div></div>");
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

		// Select an appPortlet, when
		// dblClick
		$("div.portlet-header").dblclick(function() {
			$("div.portlet-header").removeClass("selectedPortlet");
			$(this).addClass("selectedPortlet");

			$(".ui-dialog-content").dialog("close");
			selectedApp = $(this).parent().find(".portlet-content").text();
		});

		// Clicking anywhere in
		// sortableAppsWrapper causes
		// deselection
		$("#sortableApps").click(function() {
			$("div.portlet-header").removeClass("selectedPortlet");
			selectedApp = "";
		});

		$("div#buttonSet > button").button();
		$("button#showInfo").button({
			icons : {
				primary : "ui-icon-info"
			}
		});
		$("button#update").button({
			icons : {
				primary : "ui-icon-refresh"
			}
		});
		$("button#setPerms").button({
			icons : {
				primary : "ui-icon-gear"
			}
		});
		$("button#deleteApp").button({
			icons : {
				primary : "ui-icon-trash"
			}
		});
		$("button#webResources").button({
			icons : {
				primary : "ui-icon-folder-collapsed"
			}
		});
	});

}
/**
 * fills the display for the chosen system-app with app-information
 * 
 * @param {String}
 *            appName Specific appname of the current chosen app
 */
function fillAppDisplay(appName, policyArr) {
	$("#appDisplay").html("");
	$("#appDisplay").append("<p class='appNamePol'>" + appName + "</p> <br>");

	for (var i = 0; i < policyArr.length; i++) { // Anzahl POLICIES
		if (policyArr[i].mode == "allow") {
			$("#appDisplay").append(
					"<div class='wrapPolicies' id='wrapPol" + i + "'> <strong>Mode:</strong> <span class='GRANTPol'> " + policyArr[i].mode
							+ "</span><br> </div><br>");

			$("#wrapPol" + i).append(
					"<div class='permissions' id='perm" + i + "'><strong>Permissions:</strong> <br> </div> <div class='conditions' id='cond" + i
							+ "'><strong>Conditions:</strong> <br> </div>");
		} else {
			$("#appDisplay").append(
					"<div class='wrapPolicies' id='wrapPol" + i + "'> <strong>Mode:</strong> <span class='DENYPol'> " + policyArr[i].mode
							+ "</span><br> </div><br>");

			$("#wrapPol" + i).append(
					"<div class='permissions' id='perm" + i + "'><strong>Permissions:</strong> <br> </div> <div class='conditions' id='cond" + i
							+ "'><strong>Conditions:</strong> <br> </div>");

		}
		for (var j = 0; j < policyArr[i].permissions.length; j++) { // Zu jeder
			// Policy
			// Anzahl
			// PERMISSIONS

			$("#perm" + i).append(
					"<div class='wrapOnePerm' id='wrapOnePerm" + i + j + "'>Actions: " + policyArr[i].permissions[j].actions + "<br>Filter: "
							+ policyArr[i].permissions[j].filter + "<br>Type: " + policyArr[i].permissions[j].type + "<br></div>");

		}
		for (var k = 0; k < policyArr[i].conditions.length; k++) {
			$("#cond" + i).append(
					"<div class='wrapOneCond' id='wrapOneCond" + i + k + "'>Type: " + policyArr[i].conditions[k].type + "<br>Arg1: "
							+ policyArr[i].conditions[k].arg1 + "<br>Arg2: " + policyArr[i].conditions[k].arg2 + "<br></div>");
		}
	}
	if ($("#appDisplay").html() != "") {
		$("#appDisplay").dialog("open");
	}
}

function fillEditDisplay(appName, policyArr) {
	$("#editWebResources").html("");
	var countActions;
	var actions;
	var actionForThis;
	for (var i = 0; i < policyArr.length; i++) { // Anzahl POLICIES
		// For each Policy one DIV.wrapPolicies with slideCheckbox
		$("#editWebResources").append(
				"<div class='wrapPols' id='wrapPolicy" + i + "'><div class='slide'><input class='slideCheck' type='checkbox' onchange='policyChecked(" + i
						+ ")' value='none' id='slide" + i + "' name='check' /><label for='slide" + i + "'></label></div></div><br>");

		// In each policyWrap there is one DIV for permissions and one DIV for
		// conditions
		$("#wrapPolicy" + i).append(
				"<div class='polPermissions' id='polPerm" + i + "'><strong>Permissions</strong><br><br></div> <br> <div class='polConditions' id='polCond" + i
						+ "'><strong>Conditions</strong><br><br></div><br>");
		for (var k = 0; k < policyArr[i].conditions.length; k++) {
			$("#polCond" + i).append(
					"<div class='wrapOnePolCond' id='wrapPolCond" + i + k + "'><input type='text' value='" + policyArr[i].conditions[k].type
							+ "' id='condtype_" + i + k + "' class='condType'><label for='condtype_" + i + k + "'>Type</label>"
							+ "<br><input type='text' value='" + policyArr[i].conditions[k].arg1 + "' id='arg1_" + i + k
							+ "' class='condArg1'><label for='arg1_" + i + k + "'>Arg1</label>" + "<br><input type='text' value='"
							+ policyArr[i].conditions[k].arg2 + "' id='arg2_" + i + k + "' class='condArg2'><label for='arg2_" + i + k + "'>Arg2</label>");
		}

		// Start value of the slideCheckboxes
		if (policyArr[i].mode == "allow") {
			$("#slide" + i).prop("checked", "true");
			$("#slide" + i).parent().css("background", "#454545");
		} else {
			$("#slide" + i).prop("checked", "false");
			$("#slide" + i).parent().css("background", "#E8E8E8");
		}

		for (var j = 0; j < policyArr[i].permissions.length; j++) { // In one
			// Policy
			// Anzahl an
			// PERMISSIONS

			var actions = policyArr[i].permissions[j].actions;
			if (actions == "") {
				countActions = 0;
			} else {
				if (actions.indexOf(",") != -1) {
					countActions = 2;
					actionForThis = actions.split(",");
				} else {
					countActions = 1;
					actionForThis = actions;
				}
			}

			if (countActions == 1) {
				$("#polPerm" + i).append(
						"<div class='wrapOnePolPerm' id='wrapPolPerm" + i + j + "'><button class='allowPerButton' onclick='deletePermission(" + i + "," + j
								+ ")'></button>Actions<br><input type='checkbox' id='action" + i + j + "' name='" + actionForThis
								+ "' checked><label for='action" + i + j + "'>" + policyArr[i].permissions[j].actions
								+ "</label><br><br><input type='text' value='" + policyArr[i].permissions[j].type + "' id='type" + i + j
								+ "' class='permType'><label for='type" + i + j + "'>Type</label>" + "<br><input type='text' value='"
								+ policyArr[i].permissions[j].filter + "' id='filter" + i + j + "' class='permFilter'><label for='filter" + i + j
								+ "'>Filter</label>" + "<br></div>");
			}
			if (countActions == 0) {
				$("#polPerm" + i).append(
						"<div class='wrapOnePolPerm' id='wrapPolPerm" + i + j + "'><button class='allowPerButton' onclick='deletePermission(" + i + "," + j
								+ ")'></button>Actions: none<br><br><input type='text' value='" + policyArr[i].permissions[j].type + "' id='type" + i + j
								+ "' class='permType'><label for='type" + i + j + "'>Type</label>" + "<br><input type='text' value='"
								+ policyArr[i].permissions[j].filter + "' id='filter" + i + j + "' class='permFilter'><label for='filter" + i + j
								+ "'>Filter</label>" + "<br></div>");
			}
			if (countActions == 2) {
				var actionsInput = "";
				for (var x = 0; x < actionForThis.length; x++) {
					actionsInput = actionsInput + "<input type='checkbox' id='action" + i + j + x + "' name='" + actionForThis[x]
							+ "' checked><label for='action" + i + j + x + "'>" + actionForThis[x] + "</label><br>";
				}
				$("#polPerm" + i).append(
						"<div class='wrapOnePolPerm' id='wrapPolPerm" + i + j + "'><button class='allowPerButton' onclick='deletePermission(" + i + "," + j
								+ ")'></button>Actions<br>" + actionsInput + "<br><br><input type='text' value='" + policyArr[i].permissions[j].type
								+ "' id='type" + i + j + "' class='permType'><label for='type" + i + j + "'>Type</label>" + "<br><input type='text' value='"
								+ policyArr[i].permissions[j].filter + "' id='filter" + i + j + "' class='permFilter'><label for='filter" + i + j
								+ "'>Filter</label>" + "<br></div>");
			}
		}
	}
	$(".allowPerButton").button({
		icons : {
			primary : "ui-icon-close"
		},
		text : false
	});
	if ($("#appDisplay").html() != "") {
		$("#appDisplay").dialog("open");
	}
}

function deletePermission(i, j) {
	var permId = "wrapPolPerm" + i + j;
	$("#" + permId).remove();
}

function policyChecked(num) {
	var idOfCheckbox = "slide" + num;
	if ($("#" + idOfCheckbox).prop("checked") == true) {
		$("#" + idOfCheckbox).parent().css("background", "#454545");
	} else {
		$("#" + idOfCheckbox).parent().css("background", "#E8E8E8");
	}
}

function getPermContentToSend() {
	var thisDia = $("div#editWebResources");
	var countPolicies = $("div#editWebResources").find("div.wrapPols").length;
	var countPermsInOnePolicy;
	var countCondsInOnePolicy;

	var permString = "";
	var condString = "";
	var jsonString = "";
	var allPolicies = "";
	var jsonToSend = "";
	var mode = "";

	// for each POLICY
	for (var x = 0; x < countPolicies; x++) {
		var isChecked = thisDia.find("div#wrapPolicy" + x).find(".slide").find(".slideCheck").prop("checked");
		if (isChecked == true) {
			mode = "ALLOW";
		} else {
			mode = "DENY";
		}
		countPermsInOnePolicy = thisDia.find("div#wrapPolicy" + x).find(".polPermissions").find(".wrapOnePolPerm").length;
		countCondsInOnePolicy = thisDia.find("div#wrapPolicy" + x).find(".polConditions").find(".wrapOnePolCond").length;

		for (var h = 0; h < countPermsInOnePolicy; h++) {
			var currentPerm = thisDia.find("div#wrapPolicy" + x).find(".polPermissions").find(".wrapOnePolPerm")[h].id;
			var permType = $("#" + currentPerm).find(".permType").val();
			var permFilter = $("#" + currentPerm).find(".permFilter").val();
			var actionsAsArray = $("#" + currentPerm).find("input:checkbox:checked").map(function() {
				return $(this).next("label").text().split(' ');
			});

			var actionsAsString = "";
			for (var a = 0; a < actionsAsArray.length; a++) {
				if (actionsAsString != "") {
					actionsAsString = actionsAsString + "," + actionsAsArray[a];
				} else {
					actionsAsString = actionsAsArray[a];
				}
			}

			if (permString == "") {
				permString = permString + '{"type":"' + permType + '", "filter":"' + permFilter + '", "actions":"' + actionsAsString + '"}';
			} else {
				permString = permString + ', {"type":"' + permType + '", "filter":"' + permFilter + '", "actions":"' + actionsAsString + '"}';
			}
			jsonString = '{"mode":"' + mode + '", "permissions":[' + permString + '], "conditions":[';

		}

		for (var m = 0; m < countCondsInOnePolicy; m++) {
			var condType = thisDia.find("div#wrapPolicy" + x).find(".polConditions").find("#wrapPolCond" + x + m).find(".condType").val();
			var condArg1 = thisDia.find("div#wrapPolicy" + x).find(".polConditions").find("#wrapPolCond" + x + m).find(".condArg1").val();
			var condArg2 = thisDia.find("div#wrapPolicy" + x).find(".polConditions").find("#wrapPolCond" + x + m).find(".condArg2").val();

			if (condString == "") {
				condString = condString + '{"type":"' + condType + '", "arg1":"' + condArg1 + '", "arg2":"' + condArg2 + '"}';
			} else {
				condString = condString + ', {"type":"' + condType + '", "arg1":"' + condArg1 + '", "arg2":"' + condArg2 + '"}';
			}
		}
		jsonString = jsonString + condString + ']}';
		if (allPolicies == "") {
			allPolicies = allPolicies + jsonString;
		} else {
			allPolicies = allPolicies + ", " + jsonString;
		}

		jsonToSend = '{"policies":[' + allPolicies + ']}';

		jsonString = "";
		condString = "";
		permString = "";
	}
	permContentToSend = jsonToSend;
	jsonToSend = "";
	return true;
}

function clearAppDisplay() { // if not hover
	$("#appDisplay").html("");
}

// Info-Button clicked starts this
function showAppInfo() {
	// Only if an appPortlet is selected
	if ($(".selectedPortlet")[0]) {
		var portletID = $(".selectedPortlet")[0].id;
		$.getJSON("/security/config/installedapps?action=getInfo&app=" + portletID, function(json, xhr) {
			fillAppDisplay(selectedApp, json.policies);
		}).fail(function(xhr, textStatus, errorThrown) {
			console.log("FAIL : " + textStatus);
		});
	} else {
		alert("Wählen Sie ein Bundle!");
	}
}

// Update appPortlet (name and permissions)
function updateApp() {

	if ($(".selectedPortlet")[0]) {
		var portletID = $(".selectedPortlet")[0].id;
		$.getJSON("/security/config/installedapps?action=update&app=" + portletID, function(json) {
			alert(json.statusInfo);
		});
		// var xName = "Beispielname";
		// $("#sortableApps").find(".selectedPortlet").parent().find(
		// ".portlet-content").text(xName);
		// selectedApp = xName;
	} else {
		alert("Wählen Sie ein Bundle!");
	}
}

// Edit appPortlets permissionSettings
function setPerms() {
	if ($(".selectedPortlet")[0]) {
		var portletID = $(".selectedPortlet")[0].id;
		$.getJSON("/security/config/installedapps?action=getInfo&app=" + portletID, function(json, xhr) {
			fillEditDisplay(selectedApp, json.policies);
			$("#editWebResources").dialog("open");
		}).fail(function(xhr, textStatus, errorThrown) {
			console.log("FAIL : " + textStatus);
		});
	} else {
		alert("Wählen Sie ein Bundle!");
	}
}

function showWebResources() {
	if ($(".selectedPortlet")[0]) {
		var portletID = $(".selectedPortlet")[0].id;
		$("#treeWebRes").jstree("destroy");
		if (!($("#treeWebRes").hasClass("jstree"))) {
			$("#treeWebRes").jstree({
				"core" : {
					"animation" : 0,
					"check_callback" : true,
					"themes" : {
						"stripes" : true
					},
					'data' : {
						"url" : "/security/config/installedapps?action=webResources&app=" + portletID,
						'data' : function(node) {
							return {
								'id' : node.id,
								'text' : node.id
							};
						}
					}
				},
				"plugins" : [ "wholerow", "types" ]
			}).bind("load_node.jstree", function(event, data) {
				for (var x = 0; x < data.node.children.length; x++) {
					var thisNode = $("#treeWebRes").jstree(true).get_node(data.node.children[x]);
					var oldName = thisNode.text;
					var position = oldName.lastIndexOf("/");
					var oldName_length = oldName.length;
					var newName = "";
					if (position != oldName_length - 1) {
						newName = oldName.slice(position + 1);
						$("#treeWebRes").jstree('rename_node', thisNode, newName);
					} else {
						newName = oldName.slice(0, position);
						var newPosition = newName.lastIndexOf("/");
						newName = newName.slice(newPosition + 1);
						$("#treeWebRes").jstree('rename_node', thisNode, newName);
					}
				}
			}).bind("select_node.jstree", function(event, data) {

				if (!$("#treeWebRes").jstree("is_parent", data.node)) {
					// for "/web" instead of "/example"
					var url = $("#treeWebRes").attr("aria-activedescendant");
					window.open(url, "_blank");
				}
			});
		}

		$("#webResourceDisplay").dialog("open");
	} else {
		alert("Wählen Sie ein Bundle!");
	}
}

function deleteApp() {
	if ($(".selectedPortlet")[0]) {
		var portletID = $(".selectedPortlet")[0].id;
		$.getJSON("/security/config/installedapps?action=delete&app=" + portletID, function(json) {
			var bundleName = $("#sortableApps").find(".selectedPortlet").parent().find(".portlet-content").text();
			alert(bundleName + " deleted.");
			listSysApps();
		});
	} else {
		alert("Wählen Sie ein Bundle!");
	}
}

// ------------------ APPSTORE //
// text-input style, if focused or not
function changeTextColor() {
	if ($("#store").val() == "enter an appstore...") {
		$("#store").css("font-style", "italic");
		$("#store").css("color", "#ADADAD");
	} else {
		$("#store").css("font-style", "normal");
		$("#store").css("color", "black");
	}
}

// ------------------ RESOURCES //
// when tab is clicked
function showResources() {
	// destroy old tree and build new one
	$("#jsTree_Resources").jstree("destroy");
	if (!($("#jsTree_Resources").hasClass("jstree"))) {
		$("#jsTree_Resources").jstree({
			"core" : {
				"animation" : 0,
				"check_callback" : true,
				"themes" : {
					"stripes" : true
				},
				'data' : {
					"url" : "/service/resourceview",
					'data' : function(node) {
						return {
							'id' : node.id,
							'text' : node.text
						};
					}
				}
			},
			"checkbox" : {
				"whole_node" : true
			},
			"plugins" : [ "wholerow", "types" ]
		}).bind(
				"select_node.jstree",
				function(event, data) {
					// close the resource-dialog
					$("#dialog_Resources").dialog("close");

					// id of selected node
					var currentNode = data.node.id;
					// get the node by id
					var currentSelected;
					currentSelected = $("#jsTree_Resources").jstree(true).get_node(currentNode);

					// check if the current selected node is NOT a
					// parent node (has NO children)
					if (!$("#jsTree_Resources").jstree("is_parent", data.node)) {
						$("#dialog_Resources").parent().find(".ui-dialog-title").text(currentSelected.text);
						// fill dialog with the value of the node
						$("#dialog_Resources").html(
								"<span><b>Value: </b></span><span>" + currentSelected.original.value + "</span>" + "<br><span><b>Type: </b></span><span>"
										+ currentSelected.original.type + "</span>");
						// open the dialog
						$("#dialog_Resources").dialog("open");
					}
				})
	}

	// creates dialog
	$("#dialog_Resources").dialog({
		autoOpen : false,
		draggable : true,
		position : [ 'center', 'center' ],
		minWidth : 450,
		minHeight : 200,
		dialogClass : 'no-close',
		clickOutside : true,
		clickOutsideTrigger : ".jstree-anchor"
	});

	// clickoutside closes dialog
	$("#dialog_Resources").bind('clickoutside', function() {
		$("#dialog_Resources").dialog('close');
	});
}

// ------------------ APP UPLOAD //
var count = 0;
var fileArr = [];
var file;
var fileNew;
// File-Upload

// if file changed to no file
function fileChange() {
	count++;
	if (count > 1) {
		var num = count - 1;
		$("div#file" + num).removeClass("highlightFile");
	}
	// FileList Object existing of input with ID "fileA"
	var fileList = document.getElementById("fileA").files;

	// File Object (first element of FileList)
	file = fileList[0];

	fileArr.push(file);
	// File Object not available = no data chosen
	if (!file) {
		return;
	}

	$("div#wrapFiles")
			.append(
					"<div id='file"
							+ count
							+ "' class='singleFile' onClick='setFile("
							+ count
							+ ")'> <span class='fileName'></span><span class='fileSize'></span> <br> <div id='prog' class='progress-bar blue stripes'><span style='width: 0%'></span> </div> </div>");

	$("div#file" + count + " span.fileName").html("Name: " + file.name);
	$("div#file" + count + " span.fileSize").html("Size: " + file.size + "B");
	$("div#file" + count).addClass("highlightFile");

}

var client = null;
var preAbort;

function uploadFile() {
	preAbort = true;

	client = new XMLHttpRequest();

	client.onabort = function(e) {
		return;
	}

	client.onprogress = function(e) {
		var p = Math.round(100 / e.total * e.loaded);
		$("div#file" + count + " #prog span").css("width", p + "%");
	};

	setTimeout(function() {// again File Object
		if (fileChanged == false) {
			if (preAbort == true) {
				var file = document.getElementById("fileA").files[0];
				// new FormData Object
				var formData = new FormData();
				if (!file)
					return;

				// adds file object to formData object
				formData.append("datei", file, file.name);

				client.onerror = function(e) {
					alert("Error!");

				};

				client.open("POST", "/security/config/uploadApp");
				client.send(formData);
				client.onreadystatechange = function() {
					if (client.readyState == 4 && client.status == 200) {
						var json = JSON.parse(client.response);
						// clear the dialog before for-loop
						$("dialUpload").empty();
						getPerms(json, file.name, "1", "uploadDia");
						setTimeout(function() {
							$("#diaUpload").dialog("close");
							$("#dialUpload").dialog("open");
						}, 1000);
					}
				}
			}

		} else {
			if (preAbort == true) {

				client.onprogress = function(e) {
					var p = Math.round(100 / e.total * e.loaded);
					$("div#file" + number + " #prog span").css("width", p + "%");
				};
				// new FormData Object
				var formData = new FormData();
				if (!fileNew)
					return;

				// adds file object to formData object
				formData.append("datei", fileNew, fileNew.name);

				client.onerror = function(e) {
					alert("Error!");
				};
				client.open("POST", "/security/config/uploadApp");
				client.send(formData);
				client.onreadystatechange = function() {
					if (client.readyState == 4 && client.status == 200) {
						var json = JSON.parse(client.response);
						// clear the dialog before for-loop
						$("dialUpload").empty();
						getPerms(json, file.name, "1", "uploadDia");
						setTimeout(function() {
							$("#diaUpload").dialog("close");
							$("#dialUpload").dialog("open");
						}, 1000);
					}
				}
			}
		}
	}, 1000);
}

function uploadAbort() {
	if (client instanceof XMLHttpRequest) {
		client.abort();
		preAbort = false;
	}
}

// Open the uploader (dialogWidget)
function openUploader() {
	$("#diaUpload").dialog("open");
}

// Selection of a file in the uploader
var number;
var fileChanged = false;
function setFile(num) {
	fileChanged = true;
	var fileID = "file" + num;
	if (!($("#" + fileID).hasClass("highlightFile"))) {
		$("div.singleFile").removeClass("highlightFile");
		$("#" + fileID).addClass("highlightFile");
	}
	var arrPos = num - 1;
	fileNew = fileArr[arrPos];
	number = num;
}