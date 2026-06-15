let alreadyUnmuted = false;

function unmuteFirstTime(videoElement) {
    if (!alreadyUnmuted) {
        videoElement.muted = false;
        alreadyUnmuted = true;
    }
}

function handleOnPlay(videoElement) {
    unmuteFirstTime(videoElement);
}

function handleOnError() {
    const errorScreenElement = document.getElementById('error-screen');
    errorScreenElement.style.display = 'block';
}

document.addEventListener('DOMContentLoaded', () => {
    function showLoadingSpinner() {
        loadingSpinner.style.display = "flex";
    }

    function hideLoadingSpinner() {
        loadingSpinner.style.display = 'none';
    }

    video.addEventListener('waiting', () => {
        showLoadingSpinner()
        showControls();
    });
    video.addEventListener('playing', hideLoadingSpinner);
    video.addEventListener('canplay', hideLoadingSpinner);

    let isDragging = false;
    let isMetadataLoaded = false; // GUARD FLAG: Prevents controls from showing early

    function formatSeconds(totalSeconds) {
        if (isNaN(totalSeconds) || !isFinite(totalSeconds)) return "00:00";
        const hours = Math.floor(totalSeconds / 3600);
        const minutes = Math.floor((totalSeconds % 3600) / 60);
        const seconds = Math.floor(totalSeconds % 60);

        const paddedM = String(minutes).padStart(2, '0');
        const paddedS = String(seconds).padStart(2, '0');
        if (hours === 0) return `${paddedM}:${paddedS}`;

        const paddedH = String(hours).padStart(2, '0');
        return `${paddedH}:${paddedM}:${paddedS}`;
    }

    // ── HLS INITIALIZATION ────────────────────────────────────────────
    let hlsInstance = null;

    function initializeStream() {
        const playlistUrl = `/api/streaming/hls/${MEDIA_HASH}/playlist.m3u8`;
        const video = document.getElementById("video");

        if (!video) {
            console.error("Video element not found.");
            return;
        }

        if (Hls.isSupported()) {
            hlsInstance = new Hls({
                progressive: true,
                maxAudioFramesDrift: 1,
                maxFragLookUpTolerance: 0.5,
                nudgeMaxRetry: 5,
                maxStarvationDelay: 2,
                nudgeDuration: 0.1,
                startPosition: START_POSITION,
                maxBufferLength: 90,
                maxMaxBufferLength: 180,
                maxBufferSize: 150 * 1024 * 1024
            });
            hlsInstance.loadSource(playlistUrl);
            hlsInstance.attachMedia(video);

            // Fix duration discrepancies as soon as the media finishes parsing
            hlsInstance.on(Hls.Events.LEVEL_UPDATED, (eventName, data) => {
                if (data.details && data.details.fragments.length > 0) {
                    // Forces the player timeline to match the actual parsed video length
                    const actualDuration = data.details.totalduration;
                    if (video.duration && Math.abs(video.duration - actualDuration) > 0.5) {
                        hlsInstance.refreshCurrentSelection();
                    }
                }
            });

            hlsInstance.on(Hls.Events.MANIFEST_PARSED, function (event, data) {
                video.play().catch(() => console.log("Autoplay prevented by browser policy"));
                const playBtn = document.getElementById("playBtn");
                if (playBtn) playBtn.textContent = "pause";
                if (hlsInstance.levels[hlsInstance.currentLevel]) {
                    hlsInstance.levels[hlsInstance.currentLevel].details.live = true;
                }
            });

            hlsInstance.on(Hls.Events.ERROR, function (event, data) {
                console.error("HLS error", data.type, data.details, data.response?.code ?? "", data.reason ?? "");
                if (data.fatal) {
                    switch (data.type) {
                        case Hls.ErrorTypes.NETWORK_ERROR:
                            console.warn("Network error, trying to recover...");
                            hlsInstance.startLoad();
                            break;
                        case Hls.ErrorTypes.MEDIA_ERROR:
                            console.warn("Media timeline corrupted, attempting to recover...");
                            hlsInstance.recoverMediaError();
                            break;
                        default:
                            console.error("Unrecoverable fatal error, destroying player.");
                            hlsInstance.destroy();
                            if (typeof handleOnError === 'function') handleOnError();
                            break;
                    }
                }
            });

        } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
            // Native Apple fallback (Safari / iOS)
            video.src = playlistUrl;
            video.addEventListener('loadedmetadata', function () {
                video.play().catch(() => console.log("Autoplay prevented"));
                const playBtn = document.getElementById("playBtn");
                if (playBtn) playBtn.textContent = "pause";
            });

            video.addEventListener('error', function () {
                console.error("Native HLS playback failed.");
                if (typeof handleOnError === 'function') handleOnError();
            });
        }
    }


    initializeStream();

    window.addEventListener('beforeunload', () => {
        navigator.sendBeacon(`/api/streaming/stop/${MEDIA_HASH}`);
    });

    // ── Controls visibility ───────────────────────────────────────────
    let isPlaying = false;
    let fadeTimeout = null;
    let controlsShown = false;

    function showControls() {
        // ABORT if the video hasn't loaded its timeline yet
        if (!isMetadataLoaded) return;

        controlsOverlay.classList.remove("opacity-0", "pointer-events-none");
        controlsOverlay.classList.add("opacity-100");
        controlsShown = true;
        playerContainer.style.cursor = "default";

        if (!isPlaying) return;

        clearTimeout(fadeTimeout);
        fadeTimeout = setTimeout(() => {
            controlsOverlay.classList.remove("opacity-100");
            controlsOverlay.classList.add("opacity-0", "pointer-events-none");
            controlsShown = false;
            playerContainer.style.cursor = "none";
        }, 3000);
    }

    playerContainer.addEventListener("mousemove", showControls);
    playerContainer.addEventListener("click", showControls);

    video.addEventListener("play", () => {
        isPlaying = true;
        showControls();
    });
    video.addEventListener("pause", () => {
        isPlaying = false;
        showControls();
    });

    function togglePlay() {
        if (video.paused) {
            video.play();
            playBtn.textContent = "pause";
        } else {
            video.pause();
            playBtn.textContent = "play_arrow";
        }
    }

    playBtn.addEventListener("click", togglePlay);
    middleArea.addEventListener("click", () => {
        if (controlsShown) togglePlay();
    });

    // ── Volume ────────────────────────────────────────────────────────
    let lastNonZeroVolume = video.volume > 0 ? video.volume : 0.5;
    volumeSlider.value = String(video.muted ? 0 : video.volume);

    function updateVolumeSliderVisuals() {
        volumeSlider.style.setProperty("--volume", `${Number(volumeSlider.value) * 100}%`);
    }

    function updateVolumeIcon() {
        if (video.muted || video.volume === 0) {
            volumeBtn.textContent = "volume_off";
            return;
        }
        volumeBtn.textContent = video.volume < 0.5 ? "volume_down" : "volume_up";
    }

    volumeBtn.addEventListener("click", () => {
        if (video.muted || video.volume === 0) {
            video.muted = false;
            video.volume = lastNonZeroVolume > 0 ? lastNonZeroVolume : 0.5;
        } else {
            lastNonZeroVolume = video.volume;
            video.muted = true;
        }
        updateVolumeIcon();
        updateVolumeSliderVisuals();
    });

    volumeSlider.addEventListener("input", () => {
        const v = Number(volumeSlider.value);
        video.volume = v;
        if (v > 0) {
            lastNonZeroVolume = v;
            video.muted = false;
        } else {
            video.muted = true;
        }
        updateVolumeIcon();
        updateVolumeSliderVisuals();
    });

    video.addEventListener("volumechange", () => {
        volumeSlider.value = String(video.muted ? 0 : video.volume);
        updateVolumeIcon();
        updateVolumeSliderVisuals();
    });

    updateVolumeIcon();
    updateVolumeSliderVisuals();

    // ── Seek bar ──────────────────────────────────────────────────────
    function getBufferedPercent() {
        if (!video.duration || video.buffered.length === 0) return 0;
        let maxBuffered = 0;
        for (let i = 0; i < video.buffered.length; i++) {
            if (video.buffered.end(i) > maxBuffered) maxBuffered = video.buffered.end(i);
        }
        return Math.min((maxBuffered / video.duration) * 100, 100);
    }

    function updateSeekBarVisuals() {
        if (!video.duration) return;
        const played = Math.min((Number(seekBar.value) / video.duration) * 100, 100);
        seekBar.style.setProperty("--played", `${played}%`);
        seekBar.style.setProperty("--buffered", `${Math.max(getBufferedPercent(), played)}%`);
    }

    video.addEventListener("loadedmetadata", () => {
        // Trigger the flag so controls are now allowed to show
        isMetadataLoaded = true;

        // Unhide the overlay container structurally
        controlsOverlay.style.display = "flex";

        seekBar.max = video.duration;
        durationText.textContent = formatSeconds(video.duration);
        updateSeekBarVisuals();

        // Fade them in
        showControls();
    });

    video.addEventListener("durationchange", () => {
        seekBar.max = video.duration;
        durationText.textContent = formatSeconds(video.duration);
    });

    video.addEventListener("timeupdate", () => {
        if (isDragging) return;
        currentTimeText.textContent = formatSeconds(video.currentTime);
        seekBar.value = video.currentTime;
        updateSeekBarVisuals();
    });

    video.addEventListener("progress", updateSeekBarVisuals);

    seekBar.addEventListener("input", () => {
        isDragging = true;
        currentTimeText.textContent = formatSeconds(parseFloat(seekBar.value));
        updateSeekBarVisuals();
    });

    seekBar.addEventListener("change", () => {
        isDragging = false;
        video.currentTime = parseFloat(seekBar.value);
    });

    // ── Fullscreen ────────────────────────────────────────────────────
    async function toggleFullscreen() {
        if (!document.fullscreenElement) {
            playerContainer.requestFullscreen();
        } else {
            document.exitFullscreen();
        }
    }

    fullscreenBtn.addEventListener("click", toggleFullscreen);
    middleArea.addEventListener("dblclick", toggleFullscreen);

    document.addEventListener("fullscreenchange", () => {
        fullscreenBtn.textContent = document.fullscreenElement ? "fullscreen_exit" : "fullscreen";
    });

    function togglePictureInPicture() {
        if (document.pictureInPictureElement) {
            document.exitPictureInPicture();
            pictureInPictureBtn.textContent = "pip";
        } else {
            video.requestPictureInPicture();
            pictureInPictureBtn.textContent = "pip_exit";
        }
    }

    pictureInPictureBtn.addEventListener("click", togglePictureInPicture);

    document.addEventListener("keydown", e => {
        if (e.code === "Space") {
            e.preventDefault();
            togglePlay();
            return;
        }
        if (e.code === "KeyF") {
            e.preventDefault();
            toggleFullscreen();
            return;
        }
        if (e.code === "ArrowRight") {
            e.preventDefault();
            video.currentTime = Math.min(video.currentTime + 10, video.duration);
            return;
        }
        if (e.code === "ArrowLeft") {
            e.preventDefault();
            video.currentTime = Math.max(video.currentTime - 10, 0);
        }
    });
});