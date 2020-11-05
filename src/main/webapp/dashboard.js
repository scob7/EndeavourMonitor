
$(function () {

    var sensors = [];
    var sensorColors = ["#50b13f", "#2597ce", "#ce8325", "#5e25ce", "#b425ce", "#ce2563", "#ffcc00", "#ff9900", "#ff6699"];
    var timeseriesEndPadding = 0.05;

    var timescale_minutes = "minute";
    var timescale_hourly = "hour";
    var timescale_daily = "day";
    var timescale_weekly = "week";
    var timescale_monthly = "month";
    var timescale_yearly = "year";

    var $timeseries = $("#timeseries");
    var $timeinfo = $("#timeinfo");
    var sensorData = {
        labels: [],
        datasets: []
    };
    var ctx = document.getElementById("sensorChart").getContext("2d");
    var chart = new Chart(ctx, {
        type: 'line',
        data: sensorData,
        options: {
            maintainAspectRatio: false,
            //animation: false,
            scales: {
                yAxes: [{
                        ticks: {
                            //beginAtZero: true,
                            //min: -20,
                            //max: 110
                        },

                    }]
            }
        }
    });

    var $sensors = $("#sensors");
    var $sensorTemplate = $("#sensorTemplate");
    var refreshSensors = function () {
        return $.ajax({
            url: "/rest/api/sensors",
            success: function (result) {
                sensors = result;
                renderSensors();
                return result;
            }
        });
    };

    $sensors.on("click", ".sensor", function () {
        $(this).toggleClass("showOnGraph");
        refreshChart();
    });

    var $sensorChart = $("#sensorChart");
    var scrollWait = true;
    var scrollInterval = setInterval(function () {
        scrollWait = false;
    }, 500);
    //var scrollPreviousX;
    var scrollStartX;
    
    var scrollChart = function( x )
    {
        var scroll =  x - scrollStartX;
        //scrollPreviousX = x;
        //console.info("previous X: " + scrollPreviousX);
        //console.info("current X: " + x );
        console.info("Scroll: " + scroll);
        var scrollPercent = (scroll / $sensorChart.width());
        console.info("Scroll %: " + (scrollPercent * 100));
        if (!scrollWait && (scrollPercent >= 0.1 || scrollPercent <= -0.1)) {
            //var scroll = e.movementX;
            //if( chartInterval )
            //    clearInterval( chartInterval );
            scrollWait = true;
            scrollStartX = x;
            refreshChart(scrollPercent);
        }
    };
    $sensorChart.on("touchstart", function (evt) {
        console.info("touchstart");
        var touch = evt.touches[0];
        scrollStartX = touch.pageX;
        $sensorChart.on("touchmove", function (evt) {
            var touch = evt.touches[0];
            scrollChart( touch.pageX );
        });
        
        $(document).on("touchend", function () {
            console.info("touchend");
            $sensorChart.off("touchmove");
        });
    });
    
    $sensorChart.on("mousedown", function ( evt ) {
        console.info("mousedown");
        scrollStartX = evt.pageX;
        $sensorChart.on("mousemove", function (evt) {
            scrollChart( evt.pageX );
        });

        $(document).on("mouseup", function () {
            console.info("mouseup");
            $sensorChart.off("mousemove");
        });
    });

    var renderSensors = function () {
        //$sensors.empty();
        for (var i = 0; i < sensors.length; i++)
        {
            var sensor = sensors[i];
            var $sensor = $sensors.find("#" + sensor.id);
            //var append = false;
            if ($sensor.length === 0)
            {
                $sensor = $sensorTemplate.clone();
                $sensor.removeAttr("style");
                $sensor.attr("id", sensor.id);
                $sensor.css("background-color", sensorColors[i]);
                $sensors.append($sensor);
                $sensor.addClass("showOnGraph");
                //$sensors.find(".ui.checkbox").checkbox();
            }
            $sensor.find(".name").text(sensor.name);
            if (sensor.type === 1)
            {
                $sensor.find(".sensorValue").text(sensor.temp);
                $sensor.find(".sensorUnits").text("Celcius");
                if (sensor.min !== null || sensor.max !== null)
                {
                    $sensor.find(".min").text(sensor.min);
                    $sensor.find(".max").text(sensor.max);
                    if (sensor.min !== null && sensor.temp <= sensor.min)
                        $sensor.addClass("alert");
                    else if (sensor.max !== null && sensor.temp >= sensor.max)
                        $sensor.addClass("alert");
                    else
                        $sensor.removeClass("alert");
                }
            }
        }
    };

    var chartBegin;
    var chartEnd;

    var refreshChart = function (scroll)
    {
        var $activeTimeseries = $timeseries.find(".active.button");
        var interval = $activeTimeseries.attr("data-interval");
        var timeseries = $activeTimeseries.attr("data-timeseries");
        var beginInterval = $activeTimeseries.attr("data-begin");

        var format = $activeTimeseries.attr("data-format");

        refreshServerTime( function( date ){
            var beginOffset = 0;
            var endOffset = 0;
            if (!scroll) {
                console.info("Reset dates to now");
                chartBegin = new Date();
                chartEnd = new Date();
                beginOffset = beginInterval;
                endOffset = Math.round(beginInterval * timeseriesEndPadding);
            } 
            else
            {
                beginOffset = Math.round(beginInterval * scroll);
                endOffset = -1 * Math.round(beginInterval * scroll);
            }

            console.info("Begin offset: " + beginOffset + " " + timeseries);
            console.info("End offset: " + endOffset + " " + timeseries);
            if (timeseries === timescale_minutes)
            {
                chartBegin.setMinutes(chartBegin.getMinutes() - beginOffset);
                chartEnd.setMinutes(chartEnd.getMinutes() + endOffset);

            } else if (timeseries === timescale_hourly)
            {
                chartBegin.setHours(chartBegin.getHours() - beginOffset);
                chartEnd.setHours(chartEnd.getHours() + endOffset);

            } else if (timeseries === timescale_daily)
            {
                chartBegin.setDate(chartBegin.getDate() - beginOffset);
                chartEnd.setDate(chartEnd.getDate() + endOffset);

            } else if (timeseries === timescale_weekly)
            {
                chartBegin.setDate(chartBegin.getDate() - (beginOffset * 7));
                chartEnd.setDate(chartEnd.getDate() + (endOffset * 7));
            } else if (timeseries === timescale_monthly)
            {
                chartBegin.setMonth(chartBegin.getMonth() - beginOffset);
                chartEnd.setMonth(chartEnd.getMonth() + endOffset);
            } else if (timeseries === timescale_yearly)
            {
                chartBegin.setYear(chartBegin.getYesr() - beginOffset);
                chartEnd.setYesr(chartEnd.getYear() + endOffset);
            }

            console.info("begin: " + chartBegin.toString());
            console.info("end: " + chartEnd.toString());

            var promises = [];
            for (var i = 0; i < sensors.length; i++)
            {
                var sensor = sensors[i];
                var $sensor = $sensors.find("#" + sensor.id);
                if ($sensor.hasClass("showOnGraph"))
                {
                    promises.push(refreshSensorData(sensor, interval, timeseries, chartBegin, chartEnd, format));
                } else
                {
                    for (var j = 0; j < sensorData.datasets.length; j++)
                    {
                        var dataset = sensorData.datasets[j];
                        if (dataset.sensorId === sensor.id)
                            sensorData.datasets.splice(j, 1);
                    }
                }
            }
            $.when.apply($, promises).then(function () {
                chart.update(0);
                $timeinfo.text("Averaging data every " + interval + " " + timeseries + (interval > 1 ? "s" : ""));
            });
        });
    };

    $timeseries.on("click", ".button", function () {
        $timeseries.find(".active.button").removeClass("active");
        $(this).addClass("active");
        chartInterval = setInterval(refreshChart, 1000 * 60);
        refreshChart();
    });

    var getSensorDataset = function (sensor)
    {
        for (var i = 0; i < sensorData.datasets.length; i++)
        {
            var dataset = sensorData.datasets[i];
            if (dataset.sensorId === sensor.id)
                return dataset;
        }
        var color = $sensors.find("#" + sensor.id).css("background-color");
        var dataset = {
            label: sensor.name,
            data: [],
            fill: false,
            sensorId: sensor.id,
            borderColor: color,
            backgroundColor: color
        };
        sensorData.datasets.push(dataset);
        return dataset;
    };

    var refreshSensorData = function (sensor, interval, timeseries, begin, end, format)
    {
        //var begin = getBeginDate();
        //var end = getEndDate();
        //var timeseries = getTimeSeries();
        //var interval = getTimeInterval();

        var url = "/rest/api/sensor/" + sensor.id
                + "/timeseries/" + interval + "/" + timeseries
                + "?begin=" + begin.toISOString()
                + "&end=" + end.toISOString();

        return $.ajax({
            url: url,
            dataType: "json",
            success: function (timeseries) {
                //data.datasets = [];
                var dataset = getSensorDataset(sensor);
//                            var alert = {
//                               label: "Sensor Alert",
//                               data: [],
//                               fill: false,
//                               borderColor: "#ff2323"
//                            };
                //json
                dataset.data = [];
                sensorData.labels = [];
                for (var i = 0; i < timeseries.length; i++)
                {
                    var point = timeseries[i];
                    sensorData.labels.push(moment(point.timestamp).format(format));
                    dataset.data.push(point.temp);
                    //alert.data.push(45);
                }
                //window.sensorsChart.update();
            }
        });
    };

    var chartInterval;
    var sensorsInterval;
    refreshSensors().then(function () {
        refreshChart();
    }).then(function () {
        chartInterval = setInterval(refreshChart, 1000 * 60);
        sensorsInterval = setInterval(refreshSensors, 1000 * 5);
    });

    var $unregisteredMessage = $("#unregisteredMessage");
    var $registerSensorsModal = $("#registerSensorsModal").modal();
    var $unregisteredSensors = $registerSensorsModal.find(".sensors");
    var $registerSensorMessage = $registerSensorsModal.find(".message");
    
    var $editSensors = $("#editSensors");
    var $editSensorsModal = $("#editSensorsModal").modal();
    var $editSensorList = $editSensorsModal.find(".sensors");
    var $editSensorMessage = $editSensorsModal.find(".message");
    
    var $editSensorTemplate = $("#editSensorTemplate");
        
    $editSensorsModal.on( "click", ".done.button", function(){
        $editSensorsModal.modal("hide");
    });
    
    $editSensors.click( function( event ){
        $editSensorMessage.attr("style", "display:none");
        $editSensorList.empty();
        for( var i=0; i < sensors.length; i++ )
        {
            var sensor = sensors[i];
            var $sensor = $editSensorTemplate.clone();
            $sensor.removeAttr("style");
            $sensor.attr("data-id", sensor.id);
            $sensor.attr("data-serial", sensor.serial );
            $sensor.find(".serial").text(sensor.serial);
            $sensor.find(".value").text(sensor.temp);
            $sensor.find("input[name=name]").val( sensor.name );
            $sensor.find("input[name=min]").val( sensor.min );
            $sensor.find("input[name=max]").val( sensor.max );

            $editSensorList.append($sensor);
        }
       $editSensorsModal.modal("show");
       event.preventDefault();
       return false;
    });
 
    $editSensorsModal.on("click", ".saveSensor", function () {
        var $this = $(this);
        var $sensor = $this.closest(".sensor");
        var id = $sensor.attr("data-id");
        var serial = $sensor.attr("data-serial");
        var name = $sensor.find("input[name=name]").val();
        var min = $sensor.find("input[name=min]").val();
        var max = $sensor.find("input[name=max]").val();
        var type = 1;//$sensor.attr("data-type");

        $.ajax({
            method: "PUT",
            url: "/rest/api/sensor/" + id + "?serial=" + serial + "&name=" + name + "&type=" + type + "&min=" + min + "&max=" + max,
            dataType: "json",
            success: function (sensor) {
                $editSensorMessage.removeClass("error").addClass("success");
                $editSensorMessage.text(sensor.name + " updated!");
                $editSensorMessage.removeAttr("style");
            },
            error: function (jqXHR, textStatus, errorThrown) {
                $editSensorMessage.removeClass("success").addClass("error");
                $editSensorMessage.text("Failed to update " + name + ".\nCause: " + errorThrown);
                $editSensorMessage.removeAttr("style");
            }
        });
    });
    
    var refreshUnregisteredSensors = function () {
        $.ajax({
            url: "/rest/api/sensors/unregistered",
            success: function (unregistered) {
                if (unregistered.length > 0)
                {
                    $unregisteredMessage.text("You have " + unregistered.length + " unregistered sensor" + (unregistered.length > 0 ? "s" : "") + ". Click here to configure.");
                    $unregisteredMessage.removeAttr("style");
                    
                    $unregisteredSensors.empty();
                    for (var i = 0; i < unregistered.length; i++)
                    {
                        var sensor = unregistered[i];
                        var $sensor = $editSensorTemplate.clone();
                        $sensor.removeAttr("style");
                        $sensor.attr("data-serial", sensor.serial);
                        $sensor.find(".serial").text(sensor.serial);
                        $sensor.find(".value").text(sensor.temp);
                        //$sensor.find(".saveSensor");

                        $unregisteredSensors.append($sensor);
                    }
                } else
                    $unregisteredMessage.attr("style", "display:none");
            }
        });
    };
    
    refreshUnregisteredSensors();
    
    $unregisteredMessage.click(function () {
        $registerSensorMessage.attr("style", "display:none");
        $registerSensorsModal.modal("show");
    });

    $registerSensorsModal.find("#refreshSensors").click(function () {
        refreshUnregisteredSensors();
    });

    $registerSensorsModal.on("click", ".saveSensor", function () {
        var $this = $(this);
        var $sensor = $this.closest(".sensor");
        var serial = $sensor.attr("data-serial");
        var name = $sensor.find("input[name=name]").val();
        var min = $sensor.find("input[name=min]").val();
        var max = $sensor.find("input[name=max]").val();
        var type = 1;//$sensor.attr("data-type");

        $.ajax({
            method: "POST",
            url: "/rest/api/sensor/register/" + serial + "?name=" + name + "&type=" + type + "&min=" + min + "&max=" + max,
            dataType: "json",
            success: function (sensor) {
                $registerSensorMessage.removeClass("error").addClass("success");
                $registerSensorMessage.text(sensor.name + " registered!");
                $registerSensorMessage.removeAttr("style");
                refreshUnregisteredSensors();
            },
            error: function (jqXHR, textStatus, errorThrown) {
                $registerSensorMessage.removeClass("success").addClass("error");
                $registerSensorMessage.text("Failed to register " + name + ".\nCause: " + errorThrown);
                $registerSensorMessage.removeAttr("style");
                refreshUnregisteredSensors();
            }
        });
    });
    
    $registerSensorsModal.on( "click", ".done.button", function(){
        $registerSensorsModal.modal("hide");
    });
    var $serverTime = $("#serverTime");
    
    var refreshServerTime = function ( callback )
    {
        //var begin = getBeginDate();
        //var end = getEndDate();
        //var timeseries = getTimeSeries();
        //var interval = getTimeInterval();

        var url = "/rest/api/servertime/";
        
        return $.ajax({
            url: url,
            dataType: "text",
            success: function( data ){
                
                var datetime = moment(data);
                
                $serverTime.text( datetime.format("h:mm:ss a ddd MMM Do YYYY") );
                if( datetime.year() <= 1970 )
                    $serverTime.addClass("warning");
                else
                    $serverTime.removeClass("warning");
                if( callback )
                    callback.call( this, datetime.toDate() );
            },
            error: function (jqXHR, textStatus, errorThrown) {
                $serverTime.text("Error getting server time!");
                $serverTime.addClass("warning");
            }
        });
    };
});