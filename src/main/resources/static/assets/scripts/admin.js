async function loginAsAdmin(username) {
    const url = '/api/admin/login';
    const options = {
        method: 'POST',
        headers: {'content-type': 'application/json'},
        body: JSON.stringify({username: username ?? null})
    };

    const response = await fetch(url, options);
    console.log(await response.json());
    if (!response.ok) {
        return;
    }

    window.location.reload();
}

async function deleteAsAdmin(username) {
    const url = '/api/user/delete?username=' + username;
    const options = {
        method: 'DELETE',
        headers: {'content-type': 'application/json'},
    };

    const response = await fetch(url, options);
    console.log(await response.json());
    if (!response.ok) {
        return;
    }

    window.location.reload();
}

// Event listener for login-as-admin buttons
document.addEventListener('DOMContentLoaded', function () {
    const loginButtons = document.querySelectorAll('.login-as-admin');
    loginButtons.forEach(button => {
        button.addEventListener('click', async function () {
            const username = this.getAttribute('data-username');
            await loginAsAdmin(username);
        });
    });

    const deleteButtons = document.querySelectorAll('.delete-as-admin');
    deleteButtons.forEach(button => {
        button.addEventListener('click', async function () {
            const username = this.getAttribute('data-username');
            await deleteAsAdmin(username);
        });
    });

    const deleteSources = document.querySelectorAll('.delete-source');
    deleteSources.forEach(button => {
        button.addEventListener('click', async function () {
            const id = this.getAttribute('data-source-id');
            await deleteSource(id);
        });
    });
});

async function handleConfigUpdate(body) {
    hideError();
    hideSuccess();

    const req = await fetch('/api/config/update', {
        method: 'PUT',
        headers: {'content-type': 'application/json'},
        body: body,
    });
    const json = await req.json();
    if (!req.ok) {
        setError(json.errorMessage)
    }
    setSuccess(json.successMessage)
}

async function saveMaxUsers() {
    const maxUsersInput = document.getElementById('users-max-input');
    const maxUsers = maxUsersInput.value || 8;
    await handleConfigUpdate(`{"maxUsers": ${maxUsers}}`)
}

async function savePort() {
    const portInput = document.getElementById('port-input');
    const port = portInput.value || 50262;
    await handleConfigUpdate(`{"port": ${port}}`)
}

async function saveAllowUserCreation() {
    const allowUserCreationInput = document.getElementById('allow-user-creation-input');
    const allowUserCreation = allowUserCreationInput.checked;
    await handleConfigUpdate(`{"allowUserCreation": ${allowUserCreation}}`)
}

var errorBubbleTimeout;
var successBubbleTimeout;

/**
 * Creates an error message
 * @param {string} error Error reason
 */
function setError(error) {

    clearTimeout(errorBubbleTimeout);
    const errorBubbleElement = document.getElementById("error-bubble");
    const errorMessageElement = document.getElementById("error-message");
    errorBubbleElement.scrollIntoView({behavior: 'smooth'});
    errorBubbleElement.style.display = "flex";
    errorMessageElement.innerText = error;
    errorBubbleTimeout = setTimeout(() => {
        errorBubbleElement.style.display = "none";
    }, 5000)
}

function hideError() {
    const errorBubbleElement = document.getElementById("error-bubble");
    errorBubbleElement.style.display = "none";
}

/**
 * Creates a success message
 * @param {string} success Success reason
 */
function setSuccess(success) {
    clearTimeout(successBubbleTimeout);
    const successBubbleElement = document.getElementById("success-bubble");
    const successBubbleMessage = document.getElementById("success-message");
    successBubbleElement.scrollIntoView({behavior: 'smooth'});
    successBubbleElement.style.display = "flex";
    successBubbleMessage.innerText = success;
    successBubbleTimeout = setTimeout(() => {
        successBubbleElement.style.display = "none";
    }, 5000)
}

function hideSuccess() {
    const errorBubbleElement = document.getElementById("success-bubble");
    errorBubbleElement.style.display = "none";
}


async function deleteSource(id) {
    const response = await fetch(`/api/source/delete?id=${id}`, {
        method: 'DELETE',
        headers: {
            'accept': 'application/json'
        }
    });

    if (!response.ok) {
        return;
    }

    window.location.reload();
}
