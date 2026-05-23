document.addEventListener('contextmenu', (event) => {
    if (event.target.tagName === "INPUT") return;
    event.preventDefault();
});