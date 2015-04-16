first = function first(myJsonObject) {

	var w = 600, h = 600, x = d3.scale.linear().range([ 0, w ]), y = d3.scale
			.linear().range([ 0, h ]);

	var vis = d3.select("#radmin").append("div").attr("class", "chart").style(
			"width", w + "px").style("height", h + "px").append("svg:svg")
			.attr("width", w).attr("height", h);

	var partition = d3.layout.partition().value(function(d) {
		return d.size;
	});

	root = myJsonObject;

	var g = vis.selectAll("g").data(partition.nodes(root)).enter().append(
			"svg:g").attr("transform", function(d) {
		return "translate(" + x(d.y) + "," + y(d.x) + ")";
	}).on("click", click);

	var kx = w / root.dx, ky = h / 1;

	g.append("svg:rect").attr("width", root.dy * kx).attr("height",
			function(d) {
				return d.dx * ky;
			}).attr("class", function(d) {
		return d.children ? "parent" : "child";
	});

    var text = g.append("svg:text");
	text.attr("transform", transform).attr("dy", ".0em")
			.style("opacity", function(d) {
				return d.dx * ky > 12 ? 1 : 0;
			});
    text.append("svg:tspan").attr("x", "0").attr("dy", "0.1em").text(
        function(d){ return d.name; } );
    text.append("svg:tspan").attr("x", "0").attr("dy", "1.2em").text(
        function(d){ return d.messwert==undefined? "" : d.messwert; } );

    /*
	g.append("svg:text").attr("transform", transform).attr("dy", "1em")
			.style("opacity", function(d) {
				return d.dx * ky > 12 ? 1 : 0;
			}).text(function(d) {
				if (d.messwert==undefined)
				return;
				return d.messwert;
			})
	*/
	d3.select(window).on("click", function() {
		click(root);
	})

	function click(d) {
		if (!d.children){
			//if a child is clicked
		//	  window.alert(getChilds());
			return;
		}
		kx = (d.y ? w - 40 : w) / (1 - d.y);
		ky = h / d.dx;
		x.domain([ d.y, 1 ]).range([ d.y ? 40 : 0, w ]);
		y.domain([ d.x, d.x + d.dx ]);

		var t = g.transition().duration(d3.event.altKey ? 7500 : 750).attr(
				"transform", function(d) {
					return "translate(" + x(d.y) + "," + y(d.x) + ")";
				});

		t.select("rect").attr("width", d.dy * kx).attr("height", function(d) {
			return d.dx * ky;
		});

		t.select("text").attr("transform", transform).style("opacity",
				function(d) {
					return d.dx * ky > 12 ? 1 : 0;
				});

		d3.event.stopPropagation();
	}

	function transform(d) {
		return "translate(8," + d.dx * ky / 2 + ")";
	}
	
	
	
}

//goes over all child elements (leafs)
getChilds = function getChilds(){
	var list = document.getElementsByClassName("child");
	document.getElementBy
	var name;
	for (var i = 0; i < list.length; i++) {
	    // list[i] is a node with the desired class name
		if (i==0)name=list[i].__data__.name;
		if (i!=0)name=name + list[i].__data__.name;
		var messwert=list[i].__data__.messwert;
		list[i].__data__.messwert="lila";
		list[i].render;
		
		Console.log(list[i].__data__.messwert);
	}
	
	
	return string;
	
	
}
