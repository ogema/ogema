/* Helper function to hide and show the different views of a layout */
function loadView(view) {
	var oldFrame = $('.active');
	var newFrame = $('#'+view);
	
	if(oldFrame.attr("id") != newFrame.attr("id")) {
		oldFrame.fadeOut('slow', function() {
			newFrame.fadeIn();
			newFrame.removeClass('inactive');
			newFrame.addClass('active');
			oldFrame.addClass('inactive');
			oldFrame.removeClass('active');
		});
	}
}

/* Formatter to remove axis labels if the attribute description="none" is set */
function MyFormatter(v, axis) {
	return ' ';
}

function updateGraph(graphAndSource) {    	
	data = adapter.get(graphAndSource[0], 0, graphAndSource[3], graphAndSource[2]);  /*  ["wind", plot, 1.0, 1000]  */
	if(data != null) {
		graphAndSource[1].setData([data]);        
		graphAndSource[1].draw();
	}
}
    
function updateVerticalBar(barAndSource) { 	
	data = adapter.get(barAndSource[0], 0, barAndSource[3], barAndSource[2]);
	if(data != null) {
		barAndSource[1].setData([[[1,data[0][1]]]]);
		barAndSource[1].draw();
	}
}

function updateHorizontalBar(barAndSource) {
	data = adapter.get(barAndSource[0], 0, barAndSource[3], barAndSource[2]);
	if(data != null) {
		barAndSource[1].setData([[[data[0][1],1]]]);
		barAndSource[1].draw();
	}
}
    
function updateLabel(labelAndSource) {
  /* ["solar", label, 1.0, 1] */
	dataLabel = adapter.getLast(labelAndSource[0]); 
	if(dataLabel != null) {
		labelAndSource[1].update(dataLabel);
	}
}

function updateToggle(toggleAndSource) {
	data = adapter.get(toggleAndSource[0], 0, toggleAndSource[2], 1);
	if(data != null) {
		toggleAndSource[1].update(data[0][1]);
	}
}

function bootstrap() {
	/* Specified width */
	$.each($('.widthDP'), function() {
	    width = $(window).width() * ( parseInt( $(this).attr('data-width').replace('dp','') ) / 500 );
	    $(this).width(width);
	});

	/* Specified height */
	$.each($('.heightDP'), function() {
	    height = $(window).height() * ( parseInt( $(this).attr('data-height').replace('dp','') ) / 500 );
	    $(this).height(height);
	});
}