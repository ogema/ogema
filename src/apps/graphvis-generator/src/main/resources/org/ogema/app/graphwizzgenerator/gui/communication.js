var path = "/apps/ogema/graphwizzgenerator";

function sendGET() {
    $.get(path, processGET); 
}

function processGET(data, status) {
    alert("Data: " + data + "\nStatus: " + status);
}

function writeAll() {
    $.post(path, "all", processPOST);
}

function writeConnections() {
    $.post(path, "connections", processPOST);
}

function sendPOST(message) {
    $.post(path, message, processPOST);
}

function processPOST(data, status) {
    alert("Data: " + data + "\nStatus: " + status);
}
