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
var appstoreListHint = "Click the arrow to get the list of the registered marketplaces.";

$(function() {

	$("#store").focus(function() {
		$("#store").parent().css("border", "1px solid #38B5F9");
	});
	$("#store").blur(function() {
		$("#store").parent().css("border", "1px solid grey");
	});

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

	// ------------ APP MANAGEMENT
	$("#store").val(appstoreListHint);
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
		$("button#installPerms").button({
			icons : {
				primary : "ui-icon-wrench"
			}
		});
		$("button#webResources").button({
			icons : {
				primary : "ui-icon-folder-collapsed"
			}
		});
		$("button#start").button({
			icons : {
				primary : "ui-icon-play"
			}
		});
		$("button#stop").button({
			icons : {
				primary : "ui-icon-stop"
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
	} else {
		alert("Wählen Sie ein Bundle!");
	}
}

function startBundle() {

	if ($(".selectedPortlet")[0]) {
		var portletID = $(".selectedPortlet")[0].id;
		$.getJSON("/security/config/installedapps?action=start&app=" + portletID, function(json) {
			alert(json.statusInfo);
		});
	} else {
		alert("Wählen Sie ein Bundle!");
	}
}

function stopBundle() {

	if ($(".selectedPortlet")[0]) {
		var portletID = $(".selectedPortlet")[0].id;
		$.getJSON("/security/config/installedapps?action=stop&app=" + portletID, function(json) {
			alert(json.statusInfo);
		});
	} else {
		alert("Wählen Sie ein Bundle!");
	}
}

function editPerms() {
	if ($(".selectedPortlet")[0]) {
		var portletID = $(".selectedPortlet")[0].id;
		var url = "/security-gui/editperms.html?action=editperms&id=" + portletID;
		var win = window.open(url, '_blank');
		win.focus();
	} else {
		alert("Wählen Sie ein Bundle!");
	}
}

function installPerms() {
	if ($(".selectedPortlet")[0]) {
		var portletID = $(".selectedPortlet")[0].id;
		var url = "/security-gui/editperms.html?action=newperms&id=" + portletID;
		var win = window.open(url, '_blank');
		win.focus();
	} else {
		alert("Wählen Sie ein Bundle!");
	}
}

function installPermsByID(portletID) {
	var url = "/security-gui/editperms.html?action=newperms&id=" + portletID;
	var win = window.open(url, '_blank');
	win.focus();
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
	if ($("#store").val() == appstoreListHint) {
		$("#store").css("font-style", "italic");
		$("#store").css("color", "#ADADAD");
	} else {
		$("#store").css("font-style", "normal");
		$("#store").css("color", "black");
	}
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
						installPermsByID(json.id);
						setTimeout(function() {
							$("#diaUpload").dialog("close");
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
						getPerms(json, file.name, "1", "uploadDia");
						setTimeout(function() {
							$("#diaUpload").dialog("close");
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