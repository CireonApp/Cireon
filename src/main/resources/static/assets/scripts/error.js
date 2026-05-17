let countdown = 4;
const timer = document.getElementById("redirect-countdown");
const trace = document.getElementById("trace");

const interval = setInterval(() => {
    if (countdown >= 1) {
        timer.innerText = `Redirecting to home page... (${countdown})`;
        countdown--;
    } else {
        window.location.href = "/";
    }
}, 1000);

document.addEventListener("keydown", (e) => {
    if (e.code === "Space") {
        window.enterDebug();
    }
})

window.enterDebug = function () {
    clearInterval(interval);
    timer.remove();
    trace.style.display = "block";
}

window.copyTrace = function () {
    navigator.clipboard.writeText(trace.innerText).then(() => {
        alert("Trace copied to clipboard");
    }).catch(() => {
        alert("Failed to copy trace to clipboard");
    });
};