$(function() {
    
        var path = "/apps/ogema/logdatavisualization";
        var resourceDataParam = "?dataType=resourceData";
        var scheduleDataParam = "?dataType=scheduleData";
        
        //$.get(path+resourceDataParam, processGET);
    
        //default: get the resource data
        $( "#resourceData").click( function(){$.get(path+resourceDataParam, processGET)} );
        $( "#sheduleData").click( function(){$.get(path+scheduleDataParam, processGET)} );
        $( "#resourceData").click();
        
        function processGET(data, status) {
            
            var datasets = JSON.parse(data);

            var i = 0;
            $.each(datasets, function(key, val) {
                    val.color = i;
                    ++i;
            });

            // insert checkboxes
            $("#choices").empty();
            var choiceContainer = $("#choices");
            $.each(datasets, function(key, val) {
                    
                    choiceContainer.append("<br/><input type='checkbox' name='" + key +
                            "' checked='checked' id='id" + key + "'></input>" +
                            "<label for='id" + key + "' title='" +key +"'>"
                            + val.label + "</label>");
                    //enable jquerry-ui tooltips from title attr
                    //$("#id"+ key +"").tooltip("enable");
            });

            
        
            choiceContainer.find("input").click(plotAccordingToChoices);

            function plotAccordingToChoices() {

                    var data = [];

                    choiceContainer.find("input:checked").each(function () {
                            var key = $(this).attr("name");
                            if (key && datasets[key]) {
                                    data.push(datasets[key]);
                            }
                    });

                    var options = {
                                    yaxis: {
                                            tickFormatter: function(val, axis) { return val < axis.max ? val.toFixed(2) : "Values"; }
                                    },
                                    xaxis: {
                                            mode: "time",
                                            tickLength: 5
                                    },
                                    selection: {
                                        mode : "x"
                                    }
                    };

                    var plot = $.plot("#placeholder", data, options);

                    var overviewOptions = {
                            xaxis: {
                                    ticks: [],
                                    mode: "time"
                            },
                            yaxis: {
                                    ticks: [],
                                    autoscaleMargin: 0.1
                            },
                            selection: {
                                    mode: "x"
                            }
                    };

                    var overview = $.plot("#overview", data, overviewOptions);

                    // now connect the two

                    $("#placeholder").bind("plotselected", function (event, ranges) {

                            // do the zooming
                            $.each(plot.getXAxes(), function(_, axis) {
                                    var opts = axis.options;
                                    opts.min = ranges.xaxis.from;
                                    opts.max = ranges.xaxis.to;
                            });
                            plot.setupGrid();
                            plot.draw();
                            plot.clearSelection();

                            // don't fire event on the overview to prevent eternal loop

                            overview.setSelection(ranges, true);
                    });

                    $("#overview").bind("plotselected", function (event, ranges) {
                            plot.setSelection(ranges);
                    });   
            }

            plotAccordingToChoices();
            
            //bind an onclick funktion to the download button
            $("#csvDownloadButton").click(function(){
                
                var zip = new JSZip();
                var csvDelimiterChar = ';';
                var csvLineBreak = "\n";
                
                //for each dataset entry (logged resource)
                $.each(datasets, function(key, val){
                    
                     var csvData = "";
                     
                    //for each datapoint (array with 2 values) add the array
                    $.each(val.data, function(logPairKey, logValuePair){
                           
                        //replace . with , for german conventions
                        var value = logValuePair[1].toString();
                        value = value.replace(".", ",");
                        
                        //write the whole data for one resource into csvData
                        csvData += logValuePair[0] + csvDelimiterChar + value + csvDelimiterChar + csvLineBreak;
                    });
                    //create a zip file with fitting folder hierarchy
                    zip.file( key + ".csv", csvData);;
                });
               
               //create download link
                var content = zip.generate({type:"blob"});
                $(this).attr("download", "logged-resources.zip");
                $(this).attr("href", window.URL.createObjectURL(content));
            });
        }
        
});


