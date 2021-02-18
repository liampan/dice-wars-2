var webSocketRetryDelay = 5;

var socket;

function webSocket() {
    var host = window.location.host;
    console.log(window.location.protocol);
    var protocol = (("http:" === window.location.protocol) ? "ws" : "wss");
    var url = protocol + "://" + host + "/ws"
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

function sendMessage(msg) {
    console.log("sending:" + msg);
    socket.send(msg);
}

function onMessage(evt) {
    var received_msg = evt.data;
    console.log(received_msg)
}

function onClose(evt) {
    console.log("Websocket closed, trying to reconnect in "+ webSocketRetryDelay + " seconds")

    setTimeout(function() {
        webSocket();
    }, webSocketRetryDelay * 1000);
}

function onOpen(evt) {
    console.log('evt', evt);
}

webSocket();