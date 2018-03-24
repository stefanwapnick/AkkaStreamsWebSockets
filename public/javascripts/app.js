(function(){

    var websocketUrl = $("body").data("ws-url");
    var ws = new WebSocket(websocketUrl);

    ws.onmessage = function(event){
        console.log("New message received");
        console.log(event.data);

        var eventData = JSON.parse(event.data);

        if($("#resultList-" + eventData.id).length){
            $("#resultList-" + eventData.id).html(eventData.numbers.join(","));
        }else{
            $("#resultList").append("<li id='resultList-" + eventData.id + "'>" + eventData.numbers.join(",") + "</li>");
        }
    };

    ws.onopen = function(event){
        console.log("Web socket open");
        //ws.send(null);
    };

    $("#addDataSetButton").on("click", function(event){
        ws.send(null);
    });

})();