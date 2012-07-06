var client = null;
var selectedApp;
var permContentToSend = "";

$(function() {

	//creating dialog
	$("#webResourceDisplay").dialog({
		autoOpen : false,
		resizeable : true,
		draggable : true,
		close : function() {
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

	//creating dialog
	$("#editWebResources").dialog(
			{
				autoOpen : false,
				resizeable : true,
				draggable : true,
				buttons : [ {
					text : "Submit",
					click : function() { // POST settings to server
						var isReady = getPoliciesToSend();
						// wait until getPoliciesToSend() is done
						if (isReady == true) {
							var portletID = $(".selectedPortlet")[0].id;
							$.post(
									"/install/installedapps?action=setPermission&app="
											+ portletID, {
										settings : "" + permContentToSend
									}, function(data, status) { 
										// if successfull
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

	//creating dialog
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

	//creating dialog
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
	
	//creating button
	$("button#done").button({
		icons : {
			primary : "ui-icon-check"
		},
		text : false,
		disabled : true
	});

	//creating button
	$("#initUp").button({
		icons : {
			primary : "ui-icon-arrowthickstop-1-n"
		}
	});
	//creating button
	$('input:text').button().css({
		'text-align' : 'left',
		'outline' : 'none',
		'cursor' : 'text',
		'padding' : '2.5px',
		'padding-left' : '10px',
		'margin-left' : '10px'
	});

	//creating button
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
	//creating button
	$('input[type="button"]').each(function() {
		$(this).hide().after('<button>').next().button({
			label : $(this).val()
		}).click(function(event) {
			event.preventDefault();
			$(this).prev().click();
		});
	});
	//creating button
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
	//creating menu
	$("#menu").menu();
	
	
	
	// ------------------ Site ready -> Do following:
	// ----------------------------------------------------------------
	
	//clicking systemApps-tab initializes listSysApps()
	$("div#systemApps .head").click(function() {
		listSysApps();
	});
	
	//setting value of text-input
	$("#store").val("enter an appstore...");
	//change the text-input color
	changeTextColor();
	
	//get appstores
	$.getJSON("/install/appstores", function(json) {
		for (var i = 0; i < json.appstores.length; i++) {
			//append appstores as <li> items to menu
			$("#menu").append(
					"<li id='" + i + "'>" + json.appstores[i] + "</li>");
		}
		// click on <li> puts name as value of text input
		$("li").click(function() {
			$("input#store").val(json.appstores[this.id]).focus();
			changeTextColor();
			$("ul#menu").toggle("slide", {
				direction : "up"
			});
			$("#wrapArrow").toggle("slide", {
				direction : "up"
			});
		});
	});

	// toggle menu when button clicked
	$("img#button").click(function() {
		$("#menu").toggle("slide", {
			direction : "up"
		});

		$("#wrapArrow").toggle("slide", {
			direction : "up"
		});
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



//--------------------------------------------------------------------------------//
//----------------------------------- FUNCTIONS ----------------------------------//
//--------------------------------------------------------------------------------//


// ----------------------------------- APPSTORE ----------------------------------//
//--------------------------------------------------------------------------------//
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


//----------------------------------- APPUPLOAD ----------------------------------//
//--------------------------------------------------------------------------------//
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

				client.open("POST", "/install/uploadApp");
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
					$("div#file" + number + " #prog span")
							.css("width", p + "%");
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
				client.open("POST", "/install/uploadApp");
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

//----------------------------------- SYSTEMAPP ----------------------------------//
//--------------------------------------------------------------------------------//

//listing all apps installed on the system
function listSysApps() {
	var jsonArr = [];
	//clearing tab content
	$("div#sortableApps").html("");
	$
			.getJSON(
					"/install/installedapps?action=listAll",
					function(json) {
						for (var v = 0; v < json.length; v++) {
							//appends apps as portlets
							$("div#sortableApps")
									.append(
											"<div class='column'> <div class='portlet'> <div class='portlet-header' id='"
													+ json[v].id
													+ "'></div> <div class='portlet-content'>"
													+ json[v].name
													+ "</div> </div></div>");
						}
						
						//declaration of the portlet functionality, cf. jquery-ui-website
						$(".column").sortable({
							connectWith : ".column",
							handle : ".portlet-header",
							cancel : ".portlet-toggle",
							placeholder : "portlet-placeholder ui-corner-all"
						});
						$(".portlet")
								.addClass(
										"ui-widget ui-widget-content ui-helper-clearfix ui-corner-all")
								.find(".portlet-header")
								.addClass("ui-widget-header ui-corner-all")
								.prepend(
										"<span class='ui-icon ui-icon-minusthick portlet-toggle'></span>");
						$(".portlet-toggle")
								.click(
										function() {
											var icon = $(this);
											icon
													.toggleClass("ui-icon-minusthick ui-icon-plusthick");
											icon.closest(".portlet").find(
													".portlet-content")
													.toggle();
										});

						$("div.portlet-header>span").removeClass("ui-icon");

						// Select an appPortlet, when
						// dblClick
						$("div.portlet-header").dblclick(
								function() {
									$("div.portlet-header").removeClass(
											"selectedPortlet");
									$(this).addClass("selectedPortlet");

									$(".ui-dialog-content").dialog("close");
									selectedApp = $(this).parent().find(
											".portlet-content").text();
								});

						// Clicking anywhere in
						// sortableAppsWrapper causes
						// deselection
						$("#sortableApps").click(
								function() {
									$("div.portlet-header").removeClass(
											"selectedPortlet");
									selectedApp = "";
								});

						//creating buttons
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
	//clear dialog
	$("#appDisplay").html("");
	
	//append bundle-name
	$("#appDisplay").append(
			"<span class='appNamePol'>" + appName + "</span> <br><br>");

	for (var i = 0; i < policyArr.length; i++) { // for each policy
		
		//mode-specific content
		if (policyArr[i].mode == "allow") {
			$("#appDisplay")
					.append(
							"<div class='wrapPolicies' id='wrapPol"
									+ i
									+ "'> <strong>Mode:</strong> <span class='GRANTPol'> "
									+ policyArr[i].mode
									+ "</span><br> </div><br>");
		} else {
			$("#appDisplay")
					.append(
							"<div class='wrapPolicies' id='wrapPol"
									+ i
									+ "'> <strong>Mode:</strong> <span class='DENYPol'> "
									+ policyArr[i].mode
									+ "</span><br> </div><br>");
		}
		
		//add one div for permission and one div for conditions
		$("#wrapPol" + i)
		.append(
				"<div class='permissions' id='perm"
						+ i
						+ "'><strong>Permissions:</strong> <br> </div> <div class='conditions' id='cond"
						+ i
						+ "'><strong>Conditions:</strong> <br> </div>");
		
		//for each permission in the current policy
		for (var j = 0; j < policyArr[i].permissions.length; j++) { 

			//create permission-div and add permission information  
			$("#perm" + i).append(
					"<div class='wrapOnePerm' id='wrapOnePerm" + i + j
							+ "'>Actions: "
							+ policyArr[i].permissions[j].actions
							+ "<br>Filter: "
							+ policyArr[i].permissions[j].filter + "<br>Type: "
							+ policyArr[i].permissions[j].type
							+ "<br><br></div>");

		}
		
		//for each condition in the current policy
		for (var k = 0; k < policyArr[i].conditions.length; k++) {
			
			//create condition-div and add condition information  
			$("#cond" + i).append(
					"<div class='wrapOneCond' id='wrapOneCond" + i + k
							+ "'>Type: " + policyArr[i].conditions[k].type
							+ "<br>Arg1: " + policyArr[i].conditions[k].arg1
							+ "<br>Arg2: " + policyArr[i].conditions[k].arg2
							+ "<br><br></div>");
		}
	}
	
	//open dialog
	if ($("#appDisplay").html() != "") {
		$("#appDisplay").dialog("open");
	}
}

//fill editing mode (same as fillAppDisplay, but with additional editing-possibility)
function fillEditDisplay(appName, policyArr) {
	//clear dialog
	$("#editWebResources").html("");
	var countActions;
	var actions;
	var actionForThis;
	for (var i = 0; i < policyArr.length; i++) { // for each policy
		
		// For each Policy one DIV.wrapPolicies with slideCheckbox
		$("#editWebResources")
				.append(
						"<div class='wrapPols' id='wrapPolicy"
								+ i
								+ "'><div class='slide'><input class='slideCheck' type='checkbox' onchange='policyChecked("
								+ i + ")' value='none' id='slide" + i
								+ "' name='check' /><label for='slide" + i
								+ "'></label></div></div><br>");

		// In each policyWrap there is one DIV for permissions and one DIV for
		// conditions
		$("#wrapPolicy" + i)
				.append(
						"<div class='polPermissions' id='polPerm"
								+ i
								+ "'><strong>Permissions</strong><br><br></div> <br> <div class='polConditions' id='polCond"
								+ i
								+ "'><strong>Conditions</strong><br><br></div><br>");
		
		//for each condition
		for (var k = 0; k < policyArr[i].conditions.length; k++) {
			$("#polCond" + i).append(
					"<div class='wrapOnePolCond' id='wrapPolCond" + i + k
							+ "'><input type='text' value='"
							+ policyArr[i].conditions[k].type
							+ "' id='condtype_" + i + k
							+ "' class='condType'><label for='condtype_" + i
							+ k + "'>Type</label>"
							+ "<br><input type='text' value='"
							+ policyArr[i].conditions[k].arg1 + "' id='arg1_"
							+ i + k + "' class='condArg1'><label for='arg1_"
							+ i + k + "'>Arg1</label>"
							+ "<br><input type='text' value='"
							+ policyArr[i].conditions[k].arg2 + "' id='arg2_"
							+ i + k + "' class='condArg2'><label for='arg2_"
							+ i + k + "'>Arg2</label>");
		}

		// Start value of the slideCheckboxes
		if (policyArr[i].mode == "allow") {
			$("#slide" + i).prop("checked", "true");
			$("#slide" + i).parent().css("background", "#454545");
			$("#wrapPolicy" + i).css("opacity", "1.0");
		} else {
			$("#slide" + i).prop("checked", "false");
			$("#slide" + i).parent().css("background", "#E8E8E8");
			$("#wrapPolicy" + i).css("opacity", "0.8");
		}

		//for each permission
		for (var j = 0; j < policyArr[i].permissions.length; j++) { // In one
			
			//actions for current permission
			var actions = policyArr[i].permissions[j].actions;
			if (actions == "") {
				//if there are 0 actions
				countActions = 0;
			} else {
				if (actions.indexOf(",") != -1) {
					//if there are 2+ actions
					countActions = 2;
					//transform into action as array
					actionForThis = actions.split(",");
				} else {
					//if there is 1 action
					countActions = 1;
					//stays action as string
					actionForThis = actions;
				}
			}

			//if 1 action
			if (countActions == 1) {
				$("#polPerm" + i)
						.append(
								"<div class='wrapOnePolPerm' id='wrapPolPerm"
										+ i
										+ j
										+ "'><div class='allowPermission' id='allowP"
										+ i
										+ j
										+ "'><button class='allowPerButton' onclick='closePermission("
										+ i
										+ ", "
										+ j
										+ ")' id='allowPerB"
										+ i
										+ j
										+ "'></button> </div>Actions<br><input type='checkbox' id='action"
										+ i
										+ j
										+ "' name='"
										+ actionForThis
										+ "' checked><label for='action"
										+ i
										+ j
										+ "'>"
										+ policyArr[i].permissions[j].actions
										+ "</label><br><br><input type='text' value='"
										+ policyArr[i].permissions[j].type
										+ "' id='type"
										+ i
										+ j
										+ "' class='permType'><label for='type"
										+ i
										+ j
										+ "'>Type</label>"
										+ "<br><input type='text' value='"
										+ policyArr[i].permissions[j].filter
										+ "' id='filter"
										+ i
										+ j
										+ "' class='permFilter'><label for='filter"
										+ i + j + "'>Filter</label>"
										+ "<br></div>");
				
				//button to delete permission
				//creating button
				$("#allowPerB" + i + j).button({
					icons : {
						primary : "ui-icon-close"
					},
					text : false
				});

			}
			
			//if 0 actions
			if (countActions == 0) {
				$("#polPerm" + i)
						.append(
								"<div class='wrapOnePolPerm' id='wrapPolPerm"
										+ i
										+ j
										+ "'><div class='allowPermission' id='allowP"
										+ i
										+ j
										+ "'><button class='allowPerButton' onclick='closePermission("
										+ i
										+ ", "
										+ j
										+ ")' id='allowPerB"
										+ i
										+ j
										+ "'></button> </div>Actions: none<br><br><input type='text' value='"
										+ policyArr[i].permissions[j].type
										+ "' id='type"
										+ i
										+ j
										+ "' class='permType'><label for='type"
										+ i
										+ j
										+ "'>Type</label>"
										+ "<br><input type='text' value='"
										+ policyArr[i].permissions[j].filter
										+ "' id='filter"
										+ i
										+ j
										+ "' class='permFilter'><label for='filter"
										+ i + j + "'>Filter</label>"
										+ "<br></div>");
				
				//button to delete permission
				//creating button
				$("#allowPerB" + i + j).button({
					icons : {
						primary : "ui-icon-close"
					},
					text : false
				});
			}
			
			//if more actions
			if (countActions == 2) {
				var actionsInput = "";
				
				//creating action-part
				for (var x = 0; x < actionForThis.length; x++) {
					actionsInput = actionsInput
							+ "<input type='checkbox' id='action" + i + j + x
							+ "' name='" + actionForThis[x]
							+ "' checked><label for='action" + i + j + x + "'>"
							+ actionForThis[x] + "</label><br>";
				}
				
				//inserting action-part and do the rest like before
				$("#polPerm" + i)
						.append(
								"<div class='wrapOnePolPerm' id='wrapPolPerm"
										+ i
										+ j
										+ "'><div class='allowPermission' id='allowP"
										+ i
										+ j
										+ "'><button class='allowPerButton' onclick='closePermission("
										+ i
										+ ", "
										+ j
										+ ")' id='allowPerB"
										+ i
										+ j
										+ "'></button> </div>Actions<br>"
										+ actionsInput
										+ "<br><br><input type='text' value='"
										+ policyArr[i].permissions[j].type
										+ "' id='type"
										+ i
										+ j
										+ "' class='permType'><label for='type"
										+ i
										+ j
										+ "'>Type</label>"
										+ "<br><input type='text' value='"
										+ policyArr[i].permissions[j].filter
										+ "' id='filter"
										+ i
										+ j
										+ "' class='permFilter'><label for='filter"
										+ i + j + "'>Filter</label>"
										+ "<br></div>");
				
				//button to delete permission
				//creating button
				$("#allowPerB" + i + j).button({
					icons : {
						primary : "ui-icon-close"
					},
					text : false
				});
			}
		}
	}
	//if the dialog has content afterwards open it
	if ($("#editWebResources").html() != "") {
		$("#editWebResources").dialog("open");
	}
}

//delete permission in editing-mode
function closePermission(i, j) {
	$("#wrapPolPerm" + i + j).remove();
}

//if policy is checked/unchecked change style
function policyChecked(num) {
	var idOfCheckbox = "slide" + num;
	if ($("#" + idOfCheckbox).prop("checked") == true) {
		$("#" + idOfCheckbox).parent().css("background", "#454545");
		$("#wrapPolicy" + num).css("opacity", "1.0");
	} else {
		$("#" + idOfCheckbox).parent().css("background", "#E8E8E8");
		$("#wrapPolicy" + num).css("opacity", "0.8");
	}
}

//extracts values in editing-mode of permissions
//running after clicking submit-button
function getPoliciesToSend() {
	var thisDia = $("div#editWebResources");
	//count policies
	var countPolicies = $("div#editWebResources").find("div.wrapPols").length;
	var countPermsInOnePolicy;
	var countCondsInOnePolicy;

	var permString = "";
	var condString = "";
	var jsonString = "";
	var allPolicies = "";
	var jsonToSend = "";

	// for each policy
	for (var x = 0; x < countPolicies; x++) {
		
		//boolean if current policy is checked
		var isChecked = thisDia.find("div#wrapPolicy" + x).find(".slide").find(
				".slideCheck").prop("checked");

		//count of permission in each policy
		countPermsInOnePolicy = thisDia.find("div#wrapPolicy" + x).find(
				".polPermissions").find(".wrapOnePolPerm").length;
		
		//count of conditions in each policy
		countCondsInOnePolicy = thisDia.find("div#wrapPolicy" + x).find(
				".polConditions").find(".wrapOnePolCond").length;
		
		//array containing permissions 
		var polPerms = thisDia.find("div#wrapPolicy" + x).find(
		".polPermissions").find(".wrapOnePolPerm");

		//if there are permissions
		if (countPermsInOnePolicy != 0) {
			//for each permission
			for (var h = 0; h < countPermsInOnePolicy; h++) {
				
				//id of the current permissions in the current policy
				var idOfPolPerm = polPerms[h].attributes[0].value;
				
				var permType = $("#"+idOfPolPerm).find(
						".permType").val();
				var permFilter = $("#"+idOfPolPerm).find(
						".permFilter").val();
				var actionsAsArray = $("#"+idOfPolPerm).find(
						"input:checkbox:checked").map(function() {
					return $(this).next("label").text().split(' ');
				});

				//transforms array to string
				var actionsAsString = "";
				for (var a = 0; a < actionsAsArray.length; a++) {
					if (actionsAsString != "") {
						actionsAsString = actionsAsString + ","
								+ actionsAsArray[a];
					} else {
						actionsAsString = actionsAsArray[a];
					}
				}

				//creates permission-string
				if (permString == "") {
					permString = permString + '{"type":"' + permType
							+ '", "filter":"' + permFilter + '", "actions":"'
							+ actionsAsString + '"}';
				} else {
					permString = permString + ', {"type":"' + permType
							+ '", "filter":"' + permFilter + '", "actions":"'
							+ actionsAsString + '"}';
				}

				//setting mode-type
				if (isChecked == true) {
					var modeType = "ALLOW";
				} else {
					var modeType = "DENY";
				}
				
				//adding mode and constant string-parts 
				jsonString = '{"mode":"' + modeType + '", "permissions":['
						+ permString + '], "conditions":[';
			}
		} else { //if there are no permissions in the current policy
			if (isChecked == true) {
				var modeType = "ALLOW";
			} else {
				var modeType = "DENY";
			}
			//adding mode and constant string-parts 
			//no permissions -> "permissions":[]
			jsonString = '{"mode":"' + modeType + '", "permissions":[], "conditions":[';
		}

		//for each condition
		for (var m = 0; m < countCondsInOnePolicy; m++) {
			var condType = thisDia.find("div#wrapPolicy" + x).find(
					".polConditions").find("#wrapPolCond" + x + m).find(
					".condType").val();
			var condArg1 = thisDia.find("div#wrapPolicy" + x).find(
					".polConditions").find("#wrapPolCond" + x + m).find(
					".condArg1").val();
			var condArg2 = thisDia.find("div#wrapPolicy" + x).find(
					".polConditions").find("#wrapPolCond" + x + m).find(
					".condArg2").val();

			//creating condition-string
			if (condString == "") {
				condString = condString + '{"type":"' + condType
						+ '", "arg1":"' + condArg1 + '", "arg2":"' + condArg2
						+ '"}';
			} else {
				condString = condString + ', {"type":"' + condType
						+ '", "arg1":"' + condArg1 + '", "arg2":"' + condArg2
						+ '"}';
			}
		}
		//adding condition-string 
		jsonString = jsonString + condString + ']}';
		if (allPolicies == "") {
			allPolicies = allPolicies + jsonString;
		} else {
			allPolicies = allPolicies + ", " + jsonString;
		}

		//finally adding constant string-part
		jsonToSend = '{"policies":[' + allPolicies + ']}';

		jsonString = "";
		condString = "";
		permString = "";
	}
	
	//writing in variable permContentToSend
	permContentToSend = jsonToSend;
	
	//clearing variable jsonToSend
	jsonToSend = "";
	
	return true;
}

//Information-Button clicked starts this
function showAppInfo() {
	// Only if an appPortlet is selected
	if (selectedApp != "") {
		
		//get selected portlet-id
		var portletID = $(".selectedPortlet")[0].id;
		
		//get information for selected portlet
		$.getJSON("/install/installedapps?action=getInfo&app=" + portletID,
				function(json, xhr) {
					//open function fillAppDisplay for this json-data
					fillAppDisplay(selectedApp, json.policies);
				}).fail(function(xhr, textStatus, errorThrown) {
			console.log("FAIL : " + textStatus);
			console.log("FAIL : " + errorThrown);
		});
	} else { //if no appPortlet is selected
		alert("Choose a bundle!");
	}
}

//Update appPortlet 
function updateApp() {
	// Only if an appPortlet is selected
	if (selectedApp != "") {
		
		//get selected portlet-id
		var portletID = $(".selectedPortlet")[0].id;
		
		//update-url for selected portlet
		$.getJSON("/install/installedapps?action=update&app=" + portletID,
				function(json) {
					alert(json.statusInfo);
				});
	} else { //if no appPortlet is selected
		alert("Choose a bundle!");
	}
}

//Edit permissions of appPortlet 
function setPerms() {
	// Only if an appPortlet is selected
	if (selectedApp != "") {
		
		//get selected portlet-id
		var portletID = $(".selectedPortlet")[0].id;
		
		//get information for portlet-id
		$.getJSON("/install/installedapps?action=getInfo&app=" + portletID,
				function(json, xhr) {
					//open function fillEditDisplay for the json-data
					fillEditDisplay(selectedApp, json.policies);
				}).fail(function(xhr, textStatus, errorThrown) {
			console.log("FAIL : " + textStatus);
			console.log("FAIL : " + errorThrown);
		});
	} else { //if no appPortlet is selected
		alert("Choose a bundle!");
	}
}

//show the webresources for selected appPortlet
function showWebResources() {
	// Only if an appPortlet is selected
	if ($(".selectedPortlet")[0]) {
		
		//get selected portlet-id
		var portletID = $(".selectedPortlet")[0].id;
		
		//destroy the last jsTree
		$("#treeWebRes").jstree("destroy");
		
		//if #treeWebRes is no jsTree, transform into jsTree
		if (!($("#treeWebRes").hasClass("jstree"))) {
			$("#treeWebRes")
					.jstree(
							{
								"core" : {
									"animation" : 0,
									"check_callback" : true,
									"themes" : {
										"stripes" : true
									},
									'data' : {
										"url" : "/install/installedapps?action=webResources&app="
												+ portletID,
										'data' : function(node) {
											return {
												'id' : node.id,
												'text' : node.text,
												'alias' : node.alias
											};
										}
									}
								},
								"plugins" : [ "wholerow", "types" ]
							}).bind(
							"select_node.jstree",
							function(event, data) {
								if (!$("#treeWebRes").jstree("is_parent",
										data.node)) {
									window.open(data.node.text, "_blank");
								}
							});
		}
		//open dialog
		$("#webResourceDisplay").dialog("open");
	} else { //if no portlet selected
		alert("Choose a bundle!");
	}
}

//delete selected portlet
function deleteApp() {
	
	// Only if an appPortlet is selected
	if ($(".selectedPortlet")[0]) {
		
		//get selected portlet-id
		var portletID = $(".selectedPortlet")[0].id;
		
		//delete selected portlet
		$.getJSON("/install/installedapps?action=delete&app=" + portletID,
				function(json) {
					var bundleName = $("#sortableApps")
							.find(".selectedPortlet").parent().find(
									".portlet-content").text();
					alert(bundleName + " deleted.");
				});
		//list the apps again
		listSysApps();
	} else { //if no portlet is selected
		alert("Choose a bundle!");
	}
}