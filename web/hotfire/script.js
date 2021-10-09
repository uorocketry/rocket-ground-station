// Get hash parameters
{
    const hash = window.location.hash.slice(1);
    const params = hash.split(",");
    const outer = document.getElementById("outer");
    if (params[0] && !isNaN(params[0])) {
        outer.style.width = params[0] + "%";
    }
    if (params[1] && !isNaN(params[1])) {
        outer.style.height = params[1] + "%";
    }
}

const maxStates = 8;
const stateNames = [
    "Initializing",
    "Initializing",
    "Software Ready",
    "Filling",
    "Filling Complete",
    "Ignition",
    "Full Burn",
    "Final Venting",
    "Complete",
    "Filling Paused",
    "Abort"
]
const stateIcons = [
    "fa-spinner",
    "fa-spinner",
    "fa-check",
    "fa-tint",
    "fa-check",
    "fa-fire",
    "fa-rocket",
    "fa-wind",
    "fa-check",
    "fa-pause",
    "fa-ban"
]

const stateProgress = document.querySelector("#stateProgress > span");
const overlayText = document.querySelector("#overlayText");

const stateOverlay = document.querySelector("#state");
const stateText = document.querySelector("#state .stateText");
const stateIcon = document.querySelector("#state i.nextIcon");

const lastStateOverlay = document.querySelector("#lastState");
const lastStateText = document.querySelector("#lastState .stateText");
const lastStateIcon = document.querySelector("#lastState i");


let lastState = -1;

init();
async function init() {
    const webSocket = new WebSocket("ws://localhost:4534");
    webSocket.onopen = console.log;
    webSocket.onerror = console.log;

    const updateMarkerDiv = document.getElementById("updateMarker");

    let lastUpdateMarker = Date.now();
    webSocket.onmessage = (event) => {
        const startTime = Date.now();
        try {
            const data = JSON.parse(event.data);
            const state = data.state;

            if (state !== lastState) {
                lastState = state;

                stateProgress.style.width = Math.max(Math.min(state / maxStates * 100, 100), 1) + "%"  
                if (state > maxStates) stateProgress.classList.add("red");
                else stateProgress.classList.remove("red");

                stateOverlay.style.animation = "none";
                lastStateOverlay.style.animation = "none";

                // Wait until next update cycle to retrigger animation
                setTimeout(() => {
                    stateText.innerText = stateNames[state];

                    stateIcon.classList.remove(...stateIcons);
                    stateIcon.classList.add(stateIcons[state]);

                    stateOverlay.style.animation = "fadeIn 0.5s, slideIn 0.5s";
                    lastStateOverlay.style.animation = "fadeOut 0.5s, slideOut 0.5s";

                    lastStateText.innerText = stateNames[state - 1];

                    lastStateIcon.classList.remove(...stateIcons);
                    lastStateIcon.classList.add(stateIcons[state - 1]);

                }, 10);
            }

            if (startTime - lastUpdateMarker > 5000) {
                lastUpdateMarker = startTime;
                if (updateMarkerDiv.innerText.length === 0) {
                    updateMarkerDiv.innerText = ".";
                } else {
                    updateMarkerDiv.innerText = "";
                }
            }
        } catch(e) { console.log(e)}
    };
}