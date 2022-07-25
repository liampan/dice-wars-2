
function sendMessage(msg) {
    console.log("sending:" + msg);
    socket.send(msg);
}

function onMessage(evt) {
    var received_msg = evt.data;

    if (received_msg == 'get-board') sendMessage(received_msg)
    else {
        document.getElementById('screen').innerHTML = received_msg;
    }
}

function onOpen(evt) {
    sendMessage('get-board')
    console.log('web socket open.')
}

// arrow might not be placed right if screen changes size
//window.onresize = () => {
//  location.reload();
//}