#set( $dollar = "$")

var path = "servlet";

function sendGET() {
    ${dollar}.get(path,processGET); 
}

function processGET(data, status) {
    alert("Data: " + data + "\nStatus: " + status);
}

function sendPOST() {
    var myData = document.getElementById('postInput').value;
    ${dollar}.post(path, myData, processPOST);
}

function processPOST(data, status) {
    alert("Data: " + data + "\nStatus: " + status);
}