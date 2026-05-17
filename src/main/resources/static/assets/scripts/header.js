window.logout = async function logout() {
    const url = '/api/auth/logout';
    const options = {method: 'POST'};

    try {
        await fetch(url, options);
        window.location.reload();
    } catch (error) {
    }
};
