// code übernommen aus "http://codepen.io/agence-web-coheractio/pen/pEnoA"
// The MIT License (MIT)
// Copyright (c) 2013 - AGENCE WEB COHERACTIO

var btn = $.fn.button.noConflict() // reverts $.fn.button to jqueryui btn
$.fn.btn = btn // assigns bootstrap button functionality to $.fn.btn

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

var isInit = false;
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
		width : $("div#sysApps").width(),
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
		width : $("div#sysApps").width(),
		height : $(window).height() - 300
	});
	
	$("#changeDefStartLevel").dialog({
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
		height : 300,
		width: $("div#sysApps").width(),		
		
	});
	
	$("#assignUserToAppstore").dialog({
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
		height : 250,
		width: $("div#userDiv").width(),		
		
	});
	
	$("#userAppstores").dialog({
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
		height : 250,
		width: $("div#userDiv").width(),		
		
	});
	
	$("#appstoreUsers").dialog({
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
		height : 250,
		width: $("div#userDiv").width(),		
		
	});

	$("button#done").button({
		icons : {
			primary : "ui-icon-check"
		},
		text : false,
		disabled : true
	});


	// ------------ APP MANAGEMENT
	
	$("#installed").click(function() {

		if($("#installed").hasClass("hover")){
			$("#content_div").load("partials/installed.html");		
			$("ul#navbar li").not(".hover").toggleClass("hover");
			$("#installed").toggleClass("hover");
		}
		
	});

	$("#newInstall").click(function() {
		
		if($("#newInstall").hasClass("hover")){
			$("#content_div").load("partials/appstores.html");			
			$("ul#navbar li").not(".hover").toggleClass("hover");
			$("#newInstall").toggleClass("hover");
		}
		
	});
	
	$("#users").click(function() {
		
		if($("#users").hasClass("hover")){
			$("#content_div").load("partials/users.html");			
			$("ul#navbar li").not(".hover").toggleClass("hover");
			$("#users").toggleClass("hover");
		}
		
	});
});

// ------------------------------ FUNCTIONS ---------------------------- //

// ------------------ SYSTEM APPS //
function diaAppstores(){
	if (isInit!=true){
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
		isInit = true;
	}
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

}


function showApps(){
	
	$("#content_div").load("partials/installed.html");
	
}


function listSysApps() {

	$("div#buttonSet > button").button();
	$("button#showInfo").button({
		icons : {
			primary : "ui-icon-info"
		}
	});
	$("button#url").button({
		icons : {
			primary : "ui-icon-extlink"
		}
	});
	$("button#update").button({
		icons : {
			primary : "ui-icon-refresh"
		}
	});
	$("button#remove").button({
		icons : {
			primary : "ui-icon-trash"
		}
	});
	$("button#setPerms").button({
		icons : {
			primary : "ui-icon-gear"
		}
	});
	$("button#listAllPerms").button({
		icons : {
			primary : "ui-icon-note"
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
	$("button#defaultPerms").button({
		icons: {
			primary : "ui-icon-script"
		}
	});
	$("button#newPolicy").button({
		icons: {
			primary : "ui-icon-document"
		}
	});
	$("button#changeDefStart").button({
		icons: {
			primary : "ui-icon-wrench"
		}
	});
	
	$("div#sortableApps").html("");
	$.getJSON("/security/config/installedapps?action=list", function(json) {
		for (var v = 0; v < json.length; v++) {
			$("div#sortableApps").append(
					"<div class='column'> <div class='portlet'> <div class='portlet-header' id='" + json[v].id + "' title='" + json[v].description + "'>" +
							"<img class='portlet-images' src='/security/config/geticon?id="+json[v].id+"' ></div> <div class='portlet-content'>"
							+ json[v].name + "</div> </div></div>");
		}
		$("div.portlet-header").tooltip({
			position: {my: "center bottom", at: "center top"}
		});
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
		$("div#sortableApps").append("<ul id='contextMenu'><li onclick='startBundle()'>Start</li><li onclick='stopBundle()'>Stop</li>" +
				 "<li onclick='removeBundle()'>Remove Bundle</li><li onclick='editPerms()'>Edit Permissions</li>" +
				 "<li onclick='listAllPerms()'>List Permissions</li><li onclick='showWebResources()'>Webresources</li>" +
				 "<li onclick='showAppInfo()'>Info</li><li onclick='goToBundleUrl()'>Go to Page</li></ul>")
		
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
		$("#sortableApps").click(function() {
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
		
		if($("#nameFilter").val().length > 0 || $("#levelFilter").val().length > 0 || $("#appFilter").is(":checked") || $("#driverFilter").is(":checked")){
			filterSysApps();
		}	
	});

}


/**
 * Sends a request to the Server to get the IDs of the Bundles that are to be shown after applying the filter.
 * If the ID is not in the response the bundle is hidden, else it is displayed
 */
function filterSysApps(){
	
	var filter = "";	
	if($("#nameFilter").val().length > 0){
		filter = filter + "&name="+$("#nameFilter").val();
	}
	if($("#levelFilter").val().length > 0){
		filter = filter + "&minlvl="+$("#levelFilter").val();
	}
	if($("#appFilter").is(":checked")){
		filter = filter + "&apps";
	}
	if($("#driverFilter").is(":checked")){
		filter = filter + "&drivers";
	}
		
	$.getJSON("/security/config/installedapps?action=filter"+filter, function(json) {
		var headers = $(".portlet-header")
		
		for(i=0; i<headers.length; i++){
			if(json.indexOf(parseInt(headers[i].id)) == -1){
				$(headers[i].parentNode.parentNode).hide();
			}else{
				$(headers[i].parentNode.parentNode).show();
			}
		}	
	});
}


/**
 * fills the display for the chosen system-app with app-information
 * 
 * @param {String}
 *            appName Specific appname of the current chosen app
 */
function fillInfoDisplay(appName, json) {
	$("#appDisplay").html("");
	
	for(var i=0; i<json.length; i++){
		var entry = json[i];
		var keys = Object.keys(entry);
		var key = keys[0];
		
		$("#appDisplay").append("<div id='prop"+i+"' class='prop'>"+key+": "+entry[key]);
	}	
	
	if ($("#appDisplay").html() != "") {
		$("#appDisplay").dialog("open");
	}
}

function clearAppDisplay() { // if not hover
	$("#appDisplay").html("");
}

function listUsersAndAppstores() {
	$("#userTable").html=("");
	$("#selectableUsers").html("");
	$("#appstoreSelect").html("");
	$("#appstoreSelect").selectmenu();
	$.getJSON("/security/config/users", function(json){
		for (var i=0; i<json.length; i++){
			$('#selectableUsers').append('<li class="userEntry entryHover">'+json[i]+'</li>');
		} 
		$(".userEntry").click(function() {
			$(this).toggleClass("selected").siblings().removeClass("selected");
		});
	});
	$.getJSON("/security/config/appstores", function(json) {
		for (var i = 0; i < json.appstores.length; i++) {
			$("#appstoreSelect").append("<option value='"+json.appstores[i]+"'>"+json.appstores[i]+"</option>").selectmenu("refresh");
		}
	});
	
	$("button#getUserInfo").button({
		icons : {
			primary : "ui-icon-info"
		}
	});
	$("button#getAppstoreInfo").button({
		icons : {
			primary : "ui-icon-info"
		}
	});
	$("button#assignUser").button({
		icons : {
			primary : "	ui-icon-person"
		}
	});

}

function listAppstores(){
	$("#installStore").html("");
	$("#installStore").selectmenu();
	$.getJSON("/security/config/appstores", function(json) {
		for (var i = 0; i < json.appstores.length; i++) {
			$("#installStore").append("<option value='"+json.appstores[i]+"'>"+json.appstores[i]+"</option>").selectmenu("refresh");
		}
	});
	$("input#openStore").button({
		icons : {
			//not working for input:submit
			primary : "ui-icon-extlink"
		}
	});
}

// Info-Button clicked starts this
function showAppInfo() {
	// Only if an appPortlet is selected
	if ($(".selectedPortlet")[0] || $("#contextMenu").data('originalElement')!=undefined) {
		if ($(".selectedPortlet")[0]){
			var portletID = $(".selectedPortlet")[0].id;
		}else{
			var portletID = $("#contextMenu").data('originalElement').id;
			$("#contextMenu").removeData();
		}
		$.getJSON("/security/config/installedapps?action=getInfo&app=" + portletID, function(json, xhr) {
			fillInfoDisplay(selectedApp, json);
		}).fail(function(xhr, textStatus, errorThrown) {
			console.log("FAIL : " + textStatus);
		});
	} else {
		alert("Wählen Sie ein Bundle!");
	}
}

function changeDefaultStartLevel(){
	
	$.getJSON("/security/config/frameworkstartlevel", function(json, xhr){
		fillStartLevelDialog(json.defaultlvl, json.currentlvl);
	}).fail(function(xhr, textStatus, errorThrown){
		console.log("FAIL : " + textStatus);
	});
	
}
function appstoreUsersDlg(){
	var appstore = $("#appstoreSelect").val();
	
	$("#appstoreUsers").html("");
	$("#appstoreUsers").dialog({
		title: "Users assigned to the appstore "+appstore
		});
	
	$.getJSON("/security/config/appstoreusers?store="+appstore, function(json){
		if((json[0]!=false)){
			$("#appstoreUsers").append("<ul id='appstoreUserList' class='users-list'></ul>")
			
			for (var i = 0; i<json.length; i++){
				$("#appstoreUserList").append("<li class='userEntry dialogEntry'>"+json[i]+"</li>");
			}
		}else{
			$("#appstoreUsers").append("<p class='nothingFound'>The appstore "+appstore+" has no users assigned.</p>");
		}
		
		$("#appstoreUsers").dialog("open");

	});
}
function userAppstoresDlg(){
	var user = $(".selected").text();
	
	$("#userAppstores").html("");
	$("#userAppstores").dialog({
		title: "Appstores assigned to the user "+user
	});
			
	$.getJSON("/security/config/userappstores?user="+user, function(json){		
		if(json[0]!=false){
			$("#userAppstores").append("<ul id='userAppstoreList' class='users-list'></ul>");
			
			for (var i = 0; i<json.length; i++){
				$("#userAppstoreList").append("<li class='userEntry dialogEntry'>"+json[i]+"</li>");
			}
		}else{
			$("#userAppstores").append("<p class='nothingFound'>The user "+user+" is not assigned to any appstores.</p>");
		}
	});
	
	$("#userAppstores").dialog("open");
	
}

function assignUserDlg(){
	if(!($(".selected").length)){
		alert("Please select an user");
		return;
	}
	var user = $(".selected").text();
	var appstore = $("#appstoreSelect").val();
	
	$("#assignUserToAppstore").html("");
	
	$("#assignUserToAppstore").append("<p>Choose the Password for the User:</p><p id='thisUserName'>"+user+"</p>" +
			"<p> and Appstore:</p><p id='thisAppstore'>"+appstore+"</p>");
	$("#assignUserToAppstore").append("<input id='pwd1' type='password'></input>");
	$("#assignUserToAppstore").append("<input id='pwd2' type='password'></input>");
	$("#assignUserToAppstore").append("<button id='applyAssign' type='button' onclick='applyAssignUser()'>Apply</button>");
	
	$("button#applyAssign").button({
		icons : {
			primary : "	ui-icon-check"
		}
	});
	if($("#assignUserToAppstore").html()!=""){
		$("#assignUserToAppstore").dialog("open");
	}
}

function applyAssignUser(){
	var user = $("#thisUserName").text();
	var appstore = $("#thisAppstore").text();
	if ( $("#pwd1").val() == $("#pwd2").val()){
		if($("#pwd1").val().length){
			var pwd = $("#pwd1").val();
		}else{
			alert("The passwords can not be empty");
			return;
		}
	}else{
		alert("The passwords can not be identical");
		$("#pwd1").val("");
		$("#pwd2").val("");
		return;
	}
	$.getJSON("/security/config/hasappstoreaccess?user="+user+"&store="+appstore, function(json){
		if (json[0]==true){
			var conf = confirm("The user "+user+" already has access to the appstore "+appstore+"\n " +
			"Do you want to change the password?")
			if (conf!=true){
				return;
			}
		}
		
		$.post("/security/config/appstoreuser?user="+user+"&store="+appstore+"&pwd="+pwd,{
			
		}, function(data, status){
			alert("Data send to server: Response: " + data);
			if(data.indexOf("successfull")>-1){
				$("#assignUserToAppstore").dialog("close");
			}else{
				$("#pwd1").val("");
				$("#pwd2").val("");
			}
		}).fail(function(xhr, textStatus, errorThrown) {
			// if http-post fails
			if (textStatus != "" && errorThrown != "") {
				alert("Somthing went wrong: " + textStatus + "\nError: " + errorThrown);
			} else {
				alert("Error.");
			}
		});
	});
}

function fillStartLevelDialog(defaultlvl, currentlvl){
	$("#changeDefStartLevel").html("");
	$("#changeDefStartLevel").append("<p id='currlvl' class='currentlvl'>The current Startlevel for newly installed Bundles is: "+currentlvl+"</p><br>");
	
	$("#changeDefStartLevel").append("<form name='changelvl' id='changelvlform'>Select the Startlevel for all Bundles which are installed after this point:"
			+"<br><br><input id='newStartLevel' type='text' pattern='[0-9]*'></input><br><br>"
			+"<submit id='applychange' onclick='applyStartLevel("+defaultlvl+")'>Change Startlevel</submit></form><br>");
	
	
	if($("#changeDefStartLevel").html()!=""){
		$("#changeDefStartLevel").dialog("open");
		$("submit#applychange").button({
			icons : {
				primary : "ui-icon-arrowrefresh-1-s"
			}
		});
		$("input#newStartLevel").spinner({
			 min : defaultlvl
		});
		$("input#newStartLevel").spinner( "value", currentlvl );
	}
}

function applyStartLevel(defaultlvl){
	var newStartLevel = $("#newStartLevel").val();
	if (newStartLevel>=defaultlvl){
		$.post("/security/config/newstartlevel?level="+newStartLevel,{
			
		}, function(data, status){
			alert("Data send to server: Response: " + data + "\nStatus: " + status);
			$("#changeDefStartLevel").dialog("close");
		}).fail(function(xhr, textStatus, errorThrown) {
			// if http-post fails
			if (textStatus != "" && errorThrown != "") {
				alert("Somthing went wrong: " + textStatus + "\nError: " + errorThrown);
			} else {
				alert("Error.");
			}
		});
	}else{
		alert("The startlevel can not be lower than "+defaultlvl);
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

function removeBundle(){
	
	if ($(".selectedPortlet")[0] || $("#contextMenu").data('originalElement')!=undefined) {
		if ($(".selectedPortlet")[0]){
			var portletID = $(".selectedPortlet")[0].id;
		}else{
			var portletID = $("#contextMenu").data('originalElement').id;
			$("#contextMenu").removeData();
		}
		$.getJSON("/security/config/startlevel?id="+portletID, function(json) {
			if (json.editable == false){
				alert("Bundles that are a part of Ogema Core can not be removed.");
				return;
			}else{
				$.getJSON("/security/config/installedapps?action=delete&app="+portletID, function(json){
					alert(json.statusInfo);
					listSysApps();
				});
			}
		});		
	} else {
		alert("Wählen Sie ein Bundle!");
	}
	
}

function goToBundleUrl(){
	if ($(".selectedPortlet")[0] || $("#contextMenu").data('originalElement')!=undefined) {
		if ($(".selectedPortlet")[0]){
			var portletID = $(".selectedPortlet")[0].id;
		}else{
			var portletID = $("#contextMenu").data('originalElement').id;
			$("#contextMenu").removeData();
		}
		$.getJSON("/security/config/installedapps?action=getURL&app=" + portletID, function(json) {
			if(json.url == null || json.url == "null"){
				alert("Es ist keine Startseite für die App vorhanden!")
			}else{
				var win = window.open(json.url, "_blank");
				win.focus();
			}
			
		});
	} else {
		alert("Wählen Sie ein Bundle!");
	}
}

function startBundle() {

	if ($(".selectedPortlet")[0] || $("#contextMenu").data('originalElement')!=undefined) {
		if ($(".selectedPortlet")[0]){
			var portletID = $(".selectedPortlet")[0].id;
		}else{
			var portletID = $("#contextMenu").data('originalElement').id;
			$("#contextMenu").removeData();
		}
		$.getJSON("/security/config/installedapps?action=start&app=" + portletID, function(json) {
			alert(json.statusInfo);
		});
	} else {
		alert("Wählen Sie ein Bundle!");
	}
}

function stopBundle() {

	if ($(".selectedPortlet")[0] || $("#contextMenu").data('originalElement')!=undefined) {
		if ($(".selectedPortlet")[0]){
			var portletID = $(".selectedPortlet")[0].id;
		}else{
			var portletID = $("#contextMenu").data('originalElement').id;
			$("#contextMenu").removeData();
		}
		$.getJSON("/security/config/installedapps?action=stop&app=" + portletID, function(json) {
			alert(json.statusInfo);
		});
	} else {
		alert("Wählen Sie ein Bundle!");
	}
}

function editPerms() {
	
	if ($(".selectedPortlet")[0] || $("#contextMenu").data('originalElement')!=undefined) {
		if ($(".selectedPortlet")[0]){
			var portletID = $(".selectedPortlet")[0].id;
		}else{
			var portletID = $("#contextMenu").data('originalElement').id;
			$("#contextMenu").removeData();
		}
		$.getJSON("/security/config/startlevel?id="+portletID, function(json) {
			if (json.editable == false){
				alert("Bundles that are a part of Ogema Core can not be edited.");
				return;
			}else{
				var url = "/security-gui/editperms.html?action=editperms&id=" + portletID;
				var win = window.open(url, '_blank');
				win.focus();
			}
		});		
	} else {
		alert("Wählen Sie ein Bundle!");
	}
}

function listAllPerms() {
	if ($(".selectedPortlet")[0] || $("#contextMenu").data('originalElement')!=undefined) {
		if ($(".selectedPortlet")[0]){
			var portletID = $(".selectedPortlet")[0].id;
		}else{
			var portletID = $("#contextMenu").data('originalElement').id;
			$("#contextMenu").removeData();
		}
		var url = "/security-gui/editperms.html?action=listperms&id=" + portletID;
		var win = window.open(url, '_blank');
		win.focus();
	} else {
		alert("Wählen Sie ein Bundle!");
	}
}

function defaultPerms(){
	
		var url = "/security-gui/editperms.html?action=defaultpolicy"
		var win = window.open(url, '_blank');
		win.focus();
	
}

function installPermsByID(portletID) {
	var url = "/security-gui/editperms.html?action=newperms&id=" + portletID;
	var win = window.open(url, '_blank');
	win.focus();
}

function createNewPolicy(){
	$.post("/security/config/newpolicy",{
		
	}, function(data, status) { // if successfull
		alert("Data send to server \nResponse: " + data + "\nStatus: " + status);
	}).fail(function(xhr, textStatus, errorThrown) {
		// if http-post fails
		if (textStatus != "" && errorThrown != "") {
			alert("Somthing went wrong: " + textStatus + "\nError: " + errorThrown);
		} else {
			alert("Error.");
		}
	});		
}

function showWebResources() {
	if ($(".selectedPortlet")[0] || $("#contextMenu").data('originalElement')!=undefined) {
		if ($(".selectedPortlet")[0]){
			var portletID = $(".selectedPortlet")[0].id;
		}else{
			var portletID = $("#contextMenu").data('originalElement').id;
			$("#contextMenu").removeData();
		}
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
function validStartLevel(){
	var startlevel = $("#levelFilter").val();
	if (startlevel.length == 0){
		filterSysApps();
	}else if (isNaN(Number(startlevel))){
		$("#levelFilter").val("");
		return;
	}else{
		filterSysApps();
	}	
}