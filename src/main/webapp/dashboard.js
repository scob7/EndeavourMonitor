/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

$(function(){
    
    
    var data = {
      labels: [],
      datasets: []
    };
    var refreshSensor = function( sensorId )
    {
        $.ajax({
        url: "/rest/api/events/" + sensorId,
        dataType: "json",
        success: function( data ){
            dataset = [];
            for( var i=0; i < data.length; i++ )
            {
                var event = data[i];
                
                dataset.push( event.temp );
            }
            
            window.sensorsChart.update();
        }
    });
    };
    
    var ctx = document.getElementById('canvas').getContext('2d');
    window.sensorsChart = new Chart(ctx, {
        type: 'line',
        data: data,
        options: {
            
        }
    });
});
