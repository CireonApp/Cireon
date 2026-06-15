/**
 * Handles the login form submission.
 * @returns {Promise<void>}
 */
async function login() {
    hideError();
    const submitButton = document.getElementById("submit-button");
    submitButton.disabled = true;
    submitButton.innerHTML = "Logging in...";
    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;


    const url = '/api/auth/login';
    const options = {
        method: 'POST',
        headers: {'content-type': 'application/json'},
        body: `{"password":"${password}","username":"${username}"}`
    };
    // const options = {method: 'POST', headers: {authorization: `Basic ${auth}`}};

    const response = await fetch(url, options);
    const json = await response.json();
    if (!response.ok) {
        submitButton.innerHTML = "Login";
        setError(json.errorMessage);
        submitButton.disabled = false;
        return;
    }

    window.location.href = "/";
}

let bubbleTimeout;

/**
 * Creates an error message
 * @param {string} error Error reason
 */
function setError(error) {
    clearTimeout(bubbleTimeout);
    const errorBubbleElement = document.getElementById("error-bubble");
    const errorMessageElement = document.getElementById("error-message");
    errorBubbleElement.style.display = "flex";
    errorMessageElement.innerText = error;
    bubbleTimeout = setTimeout(() => {
        errorBubbleElement.style.display = "none";
    }, 5000)
}

function hideError() {
    const errorBubbleElement = document.getElementById("error-bubble");
    errorBubbleElement.style.display = "none";
}

let passwordHidden = true;

function showHidePassword() {
    const password = document.getElementById("password");
    const showHidePasswordButton = document.getElementById("showHidePasswordButton");
    if (passwordHidden) {
        password.type = "text"
        showHidePasswordButton.innerText = "visibility"
    } else {
        password.type = "password"
        showHidePasswordButton.innerText = "visibility_off"

    }
    passwordHidden = !passwordHidden;
}


async function signup() {
    hideError();
    const submitButton = document.getElementById("submit-button");
    submitButton.disabled = true;
    submitButton.innerHTML = "Signing Up...";
    const username = document.getElementById("username").value;
    const displayName = document.getElementById("displayName").value;
    const password = document.getElementById("password").value;


    const url = '/api/user/create';
    const body = JSON.stringify(
        {
            password,
            displayName,
            username,
            allowAdultContent: true
        }
    )
    const options = {
        method: 'POST',
        headers: {'content-type': 'application/json'},
        body
    };


    const response = await fetch(url, options);
    const json = await response.json();
    if (!response.ok) {
        submitButton.innerHTML = "Sign Up";
        setError(json.errorMessage);
        submitButton.disabled = false;
        return;
    }

    window.location.href = `/login?username=${username}`;
}