let wsUrl = "/" + document.getElementById("ws-js").getAttribute("location")
let gameUrl = document.getElementById("ws-js").getAttribute("gameUrl")

var webSocketRetryDelay = 5;

var socket;

function webSocket() {
    var host = window.location.host;
    let protocol = (("http:" == window.location.protocol) ? "ws" : "wss");
    let url = protocol + "://" + host + wsUrl
    console.log(url);
    socket = new WebSocket(url);
    console.log(socket)

    socket.onopen = function(evt) { onOpen(evt) };
    socket.onmessage = function(evt) { onMessage(evt) };
    socket.onclose = function() { onClose() };

    socket.onerror=function(){
        console.log("Error setting up websocket. Will not try again");
    }
}

function onClose(evt) {
    console.log("Websocket closed, trying to reconnect in "+ webSocketRetryDelay + " seconds")

    setTimeout(function() {
        webSocket();
    }, webSocketRetryDelay * 1000);
}

webSocket();