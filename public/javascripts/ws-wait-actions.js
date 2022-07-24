
function sendMessage(msg) {
    console.log("sending:" + msg);
    socket.send(msg);
}

function onMessage(evt) {
    var received_msg = evt.data;

    if (received_msg == 'start-game') setTimeout(function() {
        window.location.replace(gameUrl);
    }, 500);
    else {
        document.getElementById('screen').innerHTML = received_msg;
        document.querySelector('[autofocus]').focus()
    }
}

function onOpen(evt) {
    sendMessage("joined")
    console.log('evt', evt);
}
