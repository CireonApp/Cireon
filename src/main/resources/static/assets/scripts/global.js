document.addEventListener('contextmenu', (event) => {
    if (event.target.tagName === "INPUT") return;
    event.preventDefault();
});

document.addEventListener('DOMContentLoaded', () => {

    const sliders = document.querySelectorAll('.horizontal-drag-container');

// 2. Loop through each individual container
    sliders.forEach((slider) => {
        // Each slider gets its own unique tracking variables
        let isDown = false;
        let startX;
        let scrollLeft;

        slider.addEventListener('mousedown', (e) => {
            isDown = true;
            startX = e.pageX - slider.offsetLeft;
            scrollLeft = slider.scrollLeft;
            document.body.style.userSelect = 'none';
        });

        slider.addEventListener('mouseleave', () => {
            isDown = false;
            document.body.style.userSelect = 'auto';
        });

        slider.addEventListener('mouseup', () => {
            isDown = false;
            document.body.style.userSelect = 'auto';
        });

        slider.addEventListener('mousemove', (e) => {
            if (!isDown) return;
            e.preventDefault();

            const x = e.pageX - slider.offsetLeft;
            const walk = (x - startX) ;
            slider.scrollLeft = scrollLeft - walk;
        });
    });
})
