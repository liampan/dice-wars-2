
function sendMessage(msg) {
    console.log("sending:" + msg);
    socket.send(msg);
}

function onMessage(evt) {
    var received_msg = evt.data;

    if (received_msg == 'get-board') sendMessage(received_msg)
    else {
        document.getElementById('screen').innerHTML = received_msg;
        setIcon()
    }
}

function onOpen(evt) {
    sendMessage('get-board')
    console.log('web socket open.')
}

var playNoise = true

function notify() {
    if (playNoise) {
        var audio = document.getElementById("audio");
        audio.play();
    }
}

function toggleNoise() {
    playNoise = !playNoise
    setIcon()
}

function setIcon() {
    let img = document.getElementById('sound');
    if (playNoise) {
        img.src = "/assets/images/speaker.svg"
    } else {
        img.src = "/assets/images/mute.svg.png"
    }
}

// arrow might not be placed right if screen changes size
//window.onresize = () => {
//  location.reload();
//}