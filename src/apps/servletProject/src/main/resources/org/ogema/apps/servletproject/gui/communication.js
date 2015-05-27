
var path = "/org/ogema/apps/servletProject/servlet";
path = path.toLowerCase();

function sendGET() {
    $.get(path,processGET); 
}

function processGET(data, status) {
    alert("Data: " + data + "\nStatus: " + status);
}

function sendPOST() {
    var myData = document.getElementById('postInput').value;
    $.post(path, myData, processPOST);
}

function processPOST(data, status) {
    alert("Data: " + data + "\nStatus: " + status);
}