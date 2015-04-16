/* */
function Label(id, unit, format) {
	this.id = id;
	this.unit = unit;
	this.format = format;
	this.element = $('#' + id);
	
	this.update = function(value) {
		var text = $.formatNumber(value, {format: this.format, locale: 'de'});
		this.element.text(text + ' ' + this.unit);
	}
}