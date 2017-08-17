function Toggle(id) {
	this.id = id;
	this.element = $('#' + id);
	this.state = false;
	
	this.update = function(value) {
		var button = $('#' + id + ' span');
        if(value == false) {
          button.removeClass('on');
          button.addClass('off');
        } else {
          button.removeClass('off');
          button.addClass('on');
        }
	}
}

/* jQuery AJAX: Load data from the server using a HTTP POST request.  */
function toggleButton(id, source) {
	alert(url);
	var button = $('#' + id + ' span');
	var jqxhr;
	if(button.hasClass('on')) {
		$.post(url + "?strName=" + source + '&strType=iSwitch&boolValue=false', function() {});
	} else {
		$.post(url + "?strName=" + source + '&strType=iSwitch&boolValue=true', function() {});
	}
} 